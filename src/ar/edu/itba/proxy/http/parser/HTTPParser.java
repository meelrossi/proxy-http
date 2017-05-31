
	package ar.edu.itba.proxy.http.parser;

	import java.nio.BufferOverflowException;
	import java.nio.ByteBuffer;
	import java.nio.CharBuffer;
	import java.nio.charset.Charset;
	import java.nio.charset.CharsetDecoder;
	import java.nio.charset.CharsetEncoder;
	import java.util.regex.Pattern;

	import ar.edu.itba.leet.LeetTransformation;
	import ar.edu.itba.proxy.http.HTTPSpecification;
	import ar.edu.itba.proxy.http.parser.state.HTTPInvalidParsing;
	import ar.edu.itba.proxy.http.parser.state.HTTPServiceLine;
	import ar.edu.itba.proxy.http.syntax.HTTPRegex;
	import ar.edu.itba.proxy.http.syntax.HTTPStatusCode;

	public final class HTTPParser {

		// Encoder/Decoder por defecto
		public static Charset charset = Charset.forName(HTTPSpecification.DEFAULT_CHARSET);
		public static CharsetDecoder decoder = charset.newDecoder();
		public static CharsetEncoder encoder = charset.newEncoder();

		// Maquinas
		public static Pattern request			= Pattern.compile(HTTPRegex.REQUEST_HEADER);
		public static Pattern response			= Pattern.compile(HTTPRegex.RESPONSE_HEADER);
		public static Pattern header			= Pattern.compile(HTTPRegex.HTTP_HEADER);
		public static Pattern delimiter			= Pattern.compile(HTTPRegex.DELIMITER);
		public static Pattern contentCharset	= Pattern.compile(HTTPRegex.CHARSET);
		public static Pattern chunkHeader		= Pattern.compile(HTTPRegex.CHUNK_HEADER);
		public static Pattern chunkedTransfer	= Pattern.compile(HTTPRegex.CHUNKED_TRANSFER);

		private HTTPState state = new HTTPServiceLine();
		private ByteBuffer remanent = ByteBuffer.allocate(0);
		private CharBuffer mainBuffer;

		private int mainBase = 0;
		private int mainWrite = 0;
		private int mainLimit = 0;
		private boolean update = false;
		private boolean firstTime = true;

		// Debe colapsar los headers del tipo 'Accept-encoding'
		private boolean transform = LeetTransformation.isActive();

		public HTTPParser(int headerBufferSize) {

			mainBuffer = CharBuffer.allocate(headerBufferSize);
		}

		public HTTPState feed(ByteBuffer buffer) {

			try {

				// Colapsar el buffer
				if (transform) LeetTransformation.colapse(buffer);

				CharBuffer charBuffer = decode(buffer);

				for (int i = 0; i < charBuffer.length(); ++i) {

					update = true;
					if (charBuffer.charAt(i) == '\n') {

						update = false;
						firstTime = true;

						mainBuffer.append(charBuffer, 0, i + 1);
						charBuffer.position(charBuffer.position() + i + 1);

						backtrack();
						state = state.nextState(mainBuffer);
						advance();

						if (state.isFinal()) {

							// El remanente comienza en otra posicion,
							// utilizando el mismo de entrada
							int remaining = encode(charBuffer).remaining();
							buffer.position(buffer.limit() - remaining);
							remanent = buffer;
							return state;
						}

						i = -1;
					}
				}

				if (firstTime && update) {

					mainBase = mainBuffer.position();
					update = firstTime = false;
				}

				mainBuffer.append(charBuffer);
				return state;
			}
			catch (BufferOverflowException exception) {

				return new HTTPInvalidParsing(HTTPStatusCode.REQUEST_ENTITY_TOO_LARGE);
			}
		}

		public void reset() {

			state = new HTTPServiceLine();
			remanent = ByteBuffer.allocate(0);
			transform = LeetTransformation.isActive();

			mainBuffer.clear();

			mainWrite = mainBase = mainLimit = 0;
			update = false;
			firstTime = true;
		}

		public ByteBuffer getRemanent() {

			return remanent;
		}

		private void backtrack() {

			mainWrite = mainBuffer.position();
			mainLimit = mainBuffer.limit();
			mainBuffer.limit(mainWrite);
			mainBuffer.position(mainBase);
		}

		private void advance() {

			mainBuffer.limit(mainLimit);
			mainBuffer.position(mainWrite);
			mainBase = mainWrite;
		}

		public static CharBuffer decode(ByteBuffer buffer) {

			int position = buffer.position();
			try {

				CharBuffer decodedBuffer = decoder.decode(buffer);
				buffer.position(position);
				return decodedBuffer;
			}
			catch (Exception exception) {

				buffer.position(position);
				return CharBuffer.allocate(0);
			}
		}

		public static ByteBuffer encode(CharBuffer buffer) {

			int position = buffer.position();
			try {

				ByteBuffer encodedBuffer = encoder.encode(buffer);
				buffer.position(position);
				return encodedBuffer;
			}
			catch (Exception exception) {

				buffer.position(position);
				return ByteBuffer.allocate(0);
			}
		}

		// Utilizado para debuggear
		public static void print(CharBuffer buffer) {

			for (int i = buffer.position(); i < buffer.limit(); ++i) {

				char c = buffer.array()[i];
				if (c == '\n') System.out.print("\\n\n");
				else if (c == '\r') System.out.print("\\r");
				else if (c < ' ' || c > '~') System.out.print("(" + ((int) c) + ")");
				else System.out.print(c);
			}
		}

		public static void print(ByteBuffer buffer) {
			print(decode(buffer));
		}
	}

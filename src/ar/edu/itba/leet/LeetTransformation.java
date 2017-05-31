
	package ar.edu.itba.leet;

	import java.nio.ByteBuffer;
	import java.nio.CharBuffer;
	import java.nio.charset.CharacterCodingException;
	import java.nio.charset.Charset;
	import java.nio.charset.CharsetDecoder;
	import java.nio.charset.CharsetEncoder;
	import java.util.HashMap;

	import ar.edu.itba.protocol.configurator.Configuration;
	import ar.edu.itba.proxy.http.HTTPMessage;

	/*
	**	Aplica la transformacion L33T al buffer de datos
	**	entrante, solo cuando este es del tipo 'text/plain'.
	**
	**	Ademas, el flujo de bytes no debe ser del tipo 'chunked'
	**	y no debe estar comprimido de ninguna forma.
	**
	**	Para evitar que el host destino genere un flujo de datos
	**	con algun encoding distinto a 'identity', se captura el
	**	header 'Accept-encoding' durante una request, y se vacía
	**	su contenido (con espacios, para no modificar la extension
	**	del buffer procesado, y complicar la logica del proxy).
	*/

	public class LeetTransformation {

		private static HashMap<HTTPMessage, byte []> remanentBytes = new HashMap<HTTPMessage, byte []>();

		// Identificador de fin de linea
		private static final byte LINE_FEED = 10;
		private static final byte COLON = 58;
		private static final byte SPACE = 32;
		private static final byte TAB = 9;

		public static boolean isActive() {

			return Configuration.getConfiguration().isTextTransformation();
		}

		public static boolean canTransform(HTTPMessage message) {

			if (message.isText()) return message.isIdentical();
			return false;
		}

		public static void colapse(ByteBuffer buffer) {

			byte [] data = buffer.array();
			int limit = buffer.limit();
			int base = -1;

			for (int i = buffer.position(); i < limit; ++i) {

				if (base != -1) {

					int state = 0, j = base + 1;
					while (j < limit) {

						state = nextState(state, toLower(data[j]));
						if (state == Integer.MAX_VALUE) {

							// Encontramos un segmento que debe ser colapsado
							erase(data, base, j);
							break;
						}
						else if (state != -1);
						else break;
						++j;
					}
					i = j;
					base = -1;
				}
				if (i < limit && data[i] == LINE_FEED) base = i;
			}
		}

		public static boolean transform(ByteBuffer buffer, HTTPMessage message) {

			try {

				int position = buffer.position();
				ByteBuffer leetBuffer = applyLeet(buffer, message);
				buffer.position(position);

				// Este limite deberia ser equivalente al de antes, o al menos,
				// no excederse de la capacidad maxima del buffer. En caso de
				// que el encoding haya sido parcial, podria ser menor.
				buffer.limit(Math.min(position + leetBuffer.remaining(), buffer.capacity()));

				// Fallaba en este lugar (BufferOverflowException):
				buffer.put(leetBuffer);

				buffer.position(position);
			}
			catch (CharacterCodingException exception) {

				exception.printStackTrace();
				return false;
			}
			return true;
		}

		private static byte toLower(byte code) {

			// Si es mayuscula (A-Z), lo convertimos
			if (64 < code && code < 91) return (byte) (code + 32);
			return code;
		}

		private static boolean isSpace(byte code) {

			// Un tab o un espacio
			return code == TAB || code == SPACE;
		}

		// "Accept-encoding", como secuencia de byte's
		private static byte [] header = {97, 99, 99, 101, 112,
				116, 45, 101, 110, 99, 111, 100, 105, 110, 103};

		private static int nextState(int state, byte code) {

			if (state == 0) {

				if (isSpace(code)) return 0;
				else if (code == header[0]) return 1;
			}
			else if (0 < state && state < header.length) {

				if (code == header[state]) return state + 1;
			}
			else if (state == header.length) {

				if (isSpace(code)) return state;
				if (code == COLON) return state + 1;
			}
			else if (state == header.length + 1) {

				if (code != LINE_FEED) return state;
				return Integer.MAX_VALUE;
			}
			return -1;
		}

		private static void erase(byte [] buffer, int base, int end) {

			int i = base + 1;
			while (i < end && buffer[i] != COLON) ++i;
			++i;
			while (i < end) buffer[i++] = SPACE;
		}

		private static ByteBuffer applyLeet(ByteBuffer buffer, HTTPMessage message) throws CharacterCodingException {

			// Inicializo el decoder y encoder para el charset del mensaje
			Charset charset = Charset.forName(message.getCharset());
			CharsetEncoder encoder = charset.newEncoder();
			CharsetDecoder decoder = charset.newDecoder();

			// Duplico el buffer para no cambiar los valores
			ByteBuffer bb = buffer.duplicate();

			// Obtengo posibles bytes que quedaron pendientes del mensaje
			byte [] bytes = remanentBytes.get(message);

			if (bytes != null) {

				ByteBuffer aux = bb;
				bb = ByteBuffer.allocate(aux.capacity() + bytes.length);
				bb.put(bytes);
				bb.put(aux);
				bb.flip();
				remanentBytes.remove(message);
			}

			CharBuffer s = CharBuffer.allocate(bb.capacity());
			decoder.decode(bb, s, true);

			if (bb.remaining() != 0) {

				byte [] unusedBytes = new byte[bb.remaining()]; 
				bb.get(unusedBytes, 0, bb.remaining());
				remanentBytes.put(message, unusedBytes);
			}

			int charsDecoded = s.position();
			s.position(0);

			for (int i = 0; i < charsDecoded; ++i) {

				switch (s.charAt(i)) {

					case 'a': s.put(i, '4'); break;
					case 'c': s.put(i, '<'); break;
					case 'e': s.put(i, '3'); break;
					case 'i': s.put(i, '1'); break;
					case 'o': s.put(i, '0'); break;
					default:
				}
			}

			s.limit(charsDecoded);
			ByteBuffer answer = ByteBuffer.allocate(bb.capacity());
			encoder.encode(s, answer, true);

			int overflowBytes = (4 * 1024) - answer.position();
			if (overflowBytes <= 0) {

				byte [] overflowArray = new byte[Math.abs(overflowBytes)];
				int newPosition = (4 * 1024) - 1;
				answer.position(newPosition);
				answer.get(overflowArray);
				remanentBytes.put(message, overflowArray);
			}

			answer.limit(answer.position());
			answer.position(0);
			return answer;
		}
	}

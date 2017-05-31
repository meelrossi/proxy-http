
	package ar.edu.itba.proxy.http.transfer;

	import java.nio.ByteBuffer;
import java.util.regex.Matcher;

import ar.edu.itba.leet.LeetTransformation;
import ar.edu.itba.proxy.http.HTTPMessage;
import ar.edu.itba.proxy.http.HTTPTransfer;
import ar.edu.itba.proxy.http.parser.HTTPParser;
import ar.edu.itba.proxy.http.syntax.HTTPStatusCode;

	public final class ChunkedTransfer implements HTTPTransfer {

		// La configuracion dura para toda la transferencia
		// (esto lo hace mas consistente)
		private boolean transform = LeetTransformation.isActive();

		private ChunkedState state = ChunkedState.WAITING;
		private ByteBuffer remanent = ByteBuffer.allocate(0);
		private HTTPMessage message = null;

		public HTTPStatusCode consume(ByteBuffer buffer) {

			int position = buffer.position();
			while (0 < buffer.remaining()) {

				HTTPStatusCode result = state.consume(buffer);

				switch (result) {

					case CONTINUE: {

						ChunkedState newState = state.getState();
						if (newState != state) {

							newState.transfer(state, transform, message);
							state = newState;
						}
						break;
					}
					case OK: {

						remanent = buffer;
						return HTTPStatusCode.OK;
					}
					case BAD_REQUEST: {

						restart();
						buffer.position(position);
						HTTPStatusCode status = getBadStatus();
						return status;
					}
					default: {

						restart();
						buffer.position(position);
						return result;
					}
				}
			}

			buffer.position(position);
			return HTTPStatusCode.CONTINUE;
		}

		public ByteBuffer getRemanent() {

			return remanent;
		}

		public HTTPTransfer getTransfer() {

			return null;
		}

		public HTTPMessage getMessage() {

			return message;
		}

		public void setMessage(HTTPMessage message) {

			this.message = message;
			transform &= LeetTransformation.canTransform(message);
		}

		private void restart() {

			ChunkedState.WAITING.reset();
			state = ChunkedState.WAITING;
			remanent = null;
		}

		private HTTPStatusCode getBadStatus() {

			if (message.isRequest()) {
				return HTTPStatusCode.BAD_REQUEST;
			}
			return HTTPStatusCode.BAD_RESPONSE;
		}

		private enum ChunkedState {

			// Consumiendo un chunk
			CONSUMING {

				@Override
				public HTTPStatusCode consume(ByteBuffer buffer) {

					int bytes = buffer.remaining();
					if (remaining > bytes) {

						// Consumo todos los bytes que quedan
						remaining -= bytes;
						buffer.position(buffer.limit());
					}
					else {

						// Consumo lo que necesito, y cambio de estado
						state = ChunkedState.ENDING_CHUNK;
						buffer.position(buffer.position() + remaining);
						remaining = 0;
					}
					return HTTPStatusCode.CONTINUE;
				}
			},

			// Esperando hasta cerra un chunk
			ENDING_CHUNK {

				@Override
				public HTTPStatusCode consume(ByteBuffer buffer) {

					byte next = buffer.get();
					header += (char) next;

					if (header.length() == 2) {

						if (header.equals("\r\n")) {

							// Terminamos con un chunk, pero puede haber mas
							state = ChunkedState.WAITING;
						}
						else {

							return HTTPStatusCode.BAD_REQUEST;
						}
					}
					return HTTPStatusCode.CONTINUE;
				}
			},

			// Esperando a cerrar el ultimo chunk
			LAST_CHUNK {

				@Override
				public HTTPStatusCode consume(ByteBuffer buffer) {

					byte next = buffer.get();
					header += (char) next;

					if (header.length() == 2) {

						if (header.equals("\r\n")) {

							// Terminamos con la transferencia
							return HTTPStatusCode.OK;
						}
						else {

							return HTTPStatusCode.BAD_REQUEST;
						}
					}
					return HTTPStatusCode.CONTINUE;
				}
			},

			// Esperando para consumir un header
			WAITING {

				// Permite aplicar una optimizacion
				protected boolean readingSize = true;

				@Override
				public void reset() {

					super.reset();
					readingSize = true;
				}

				@Override
				public HTTPStatusCode consume(ByteBuffer buffer) {

					byte next = buffer.get();
					header += (char) next;

					if (next != LINE_FEED) {

						// Consumimos hasta que termine la linea
						if (readingSize && (next == SEMICOLON || next == CARRIAGE_RETURN)) {

							readingSize = false;
						}
						if (readingSize && !isHex(next)) {

							// Gracias a esto podemos determinar con anterioridad
							// la correctitud del mensaje (si el chunk-size es invalido)
							readingSize = true;
							return HTTPStatusCode.BAD_REQUEST;
						}
					}
					else {

						// Parseamos y verificamos la correctitud
						readingSize = true;
						Matcher result = HTTPParser.chunkHeader.matcher(header);

						if (result.matches()) {

							try {

								// Ya tenemos la longitud del proximo chunk
								// teniendo en cuenta que esta en hexadecimal
								remaining = Integer.parseInt(result.group(1), 16);
							}
							catch (Exception exception) {

								// El buffer especificado es mayor a 2 Gb
								return HTTPStatusCode.REQUEST_ENTITY_TOO_LARGE;
							}
							if (remaining == 0) {

								// Este es el ultimo chunk
								state = ChunkedState.LAST_CHUNK;
							}
							else {

								// Comenzamos a consumir bytes
								state = ChunkedState.CONSUMING;
							}
						}
						else return HTTPStatusCode.BAD_REQUEST;
					}
					return HTTPStatusCode.CONTINUE;
				}
			};

			protected static final byte LINE_FEED = 10;
			protected static final byte CARRIAGE_RETURN = 13;
			protected static final byte SEMICOLON = 59;

			protected String header = "";
			protected int remaining = 0;
			protected ChunkedState state = this;
			protected boolean transform = false;
			protected HTTPMessage message = null;

			public void reset() {

				header = "";
				remaining = 0;
				state = this;
			}

			public void transfer(ChunkedState state, boolean transform, HTTPMessage message) {

				header = "";
				state.header = "";
				remaining = state.remaining;
				state.remaining = 0;

				this.transform = transform;
				this.message = message;
			}

			public ChunkedState getState() {

				ChunkedState nextState = state;
				state = this;
				return nextState;
			}

			// Cada estado implementa la forma en la que consume bytes
			public abstract HTTPStatusCode consume(ByteBuffer buffer);

			protected boolean isHex(byte digit) {

				return (47 < digit && digit < 58)			// De '0' a '9'
						|| (64 < digit && digit < 71)		// De 'A' a 'F'
						|| (96 < digit && digit < 103);		// De 'a' a 'f'
			}
		}
	}

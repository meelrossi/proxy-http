
	package ar.edu.itba.proxy.http.parser.state;

	import java.nio.Buffer;
import java.nio.CharBuffer;
import java.util.regex.Matcher;

import ar.edu.itba.proxy.http.HTTPMessage;
import ar.edu.itba.proxy.http.parser.HTTPParser;
import ar.edu.itba.proxy.http.parser.HTTPState;
import ar.edu.itba.proxy.http.syntax.HTTPStatusCode;
import ar.edu.itba.proxy.http.transfer.ChunkedTransfer;
import ar.edu.itba.proxy.http.transfer.NonPersistentTransfer;
import ar.edu.itba.proxy.http.transfer.ProgressiveTransfer;

	public final class HTTPUserAgentHeader implements HTTPState {

		private HTTPMessage message;

		public HTTPUserAgentHeader(HTTPMessage message) {

			this.message = message;
		}

		public HTTPState nextState(Buffer buffer) {

			CharBuffer charBuffer = (CharBuffer) buffer;

			Matcher result = HTTPParser.header.matcher(charBuffer);

			if (result.matches()) {

				message.addHeader(result.group(1), result.group(2));
				return this;
			}
			else {

				result = HTTPParser.delimiter.matcher(charBuffer);
				if (result.matches()) {

					/*
					** [RFC.2616] HTTP/1.1, Section 4.4: "Message Length"
					*/

					if (message.mustHaveNullBody()) {

						// No es posible identificar un "HEAD Response", y por lo tanto,
						// es necesario agregar un mecanismo adicional (hint).
						message.setTransfer(null);
					}
					else if (message.isChunked()) {

						// Se utiliza un "Transfer-encoding: chunked", y este tiene la
						// mayor prioridad, segun la especificacion HTTP/1.1.
						message.setTransfer(new ChunkedTransfer());
					}
					else {

						int length = message.getLength();
						if (length > 0) {

							// Existe un "Content-length" que define el buffer a recibir.
							message.setTransfer(new ProgressiveTransfer(length));
						}
						else if (length == 0) {

							// El request/response indica explicitamente que no posee body.
							message.setTransfer(null);
						}
						else if (message.isRequest()) {

							// En este caso, "length = -1", y por lo tanto se debe verificar
							// que el servidor envie datos hasta cerrar la conexion,
							// pero solo durante un "response".
							return new HTTPInvalidParsing(HTTPStatusCode.LENGTH_REQUIRED);
						}
						else {

							// En este caso no se puede determinar el buffer a
							// transferir, y por lo tanto se utiliza solo un buffer
							// de paso no-persistente.
							message.setTransfer(new NonPersistentTransfer());
						}
					}
					return new HTTPMessageReady(message);
				}

				// En esta instancia ya determinamos el header
				// de servicio y por lo tanto podemos determinar
				// si se trata de un request o de un response.
				if (message.isRequest()) {
					return new HTTPInvalidParsing(HTTPStatusCode.BAD_REQUEST);
				}
				else {
					return new HTTPInvalidParsing(HTTPStatusCode.BAD_RESPONSE);
				}
			}
		}

		public boolean isFinal() {

			return false;
		}

		public boolean isValid() {

			return true;
		}
	}

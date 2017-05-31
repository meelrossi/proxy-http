
	package ar.edu.itba.proxy.http;

	import java.nio.ByteBuffer;

	import ar.edu.itba.proxy.http.parser.HTTPParser;
	import ar.edu.itba.proxy.http.syntax.HTTPStatusCode;

	/*
	**	La siguiente clase representa un framework que permite
	**	procesar mensajes HTTP/1.1 de forma dinamica y no-bloqueante.
	**
	**	Para ello, provee un unico metodo (consume), el cual requiere
	**	el buffer a consumir. Durante la consumicion del mismo, se
	**	dispararan uno o mas eventos (trigger's).
	*/

	public abstract class HTTPSkeleton implements HTTPTrigger {

		protected HTTPHint hint = HTTPHint.NO_HINT;
		protected HTTPTransfer transfer = null;

		public HTTPSkeleton() {

			return;
		}

		public void consume(ByteBuffer buffer) {

			if (transfer == null) {

				// Iniciamos una nueva cadena
				// de transferencias
				transfer = onRestart(null);
			}

			// Consumimos el buffer, y lo devolvemos
			// al estado inicial
			int position = buffer.position();
			transfer = nextTransfer(transfer, buffer);
			buffer.position(position);
		}

		public HTTPTransfer onContinue(HTTPTransfer transfer) {

			// Continuamos con el procesamiento
			//
			// En el caso de que el HTTPTransfer sea
			// del tipo "NonPersistentTransfer", el proxy
			// debe forzar un onRestart() cuando el host
			// destino cierre la conexion abruptamente.
			return transfer;
		}

		public void onAcceptance(HTTPTransfer transfer) {

			// En este punto, se pudo obtener informacion
			// del request recibido (en el caso de que sea
			// un request). Es posible en esta instancia
			// determinar que el response podria requerir
			// de un indicio (hint) adicional, en el caso
			// de que se corresponda con un "HEAD Request".
			//
			// En ese caso es necesario asignar el hint
			// de tipo "HTTPHint.HEAD_RESPONSE" sobre el
			// mensaje asociado al transfer de ese response.

			if (hint == HTTPHint.HEAD_RESPONSE) {

				// Este hint indica que por ser un HEAD response,
				// no deberia tener body.
				HTTPMessage message = transfer.getMessage();
				if (message != null) message.setTransfer(null);
			}
			return;
		}

		public HTTPTransfer onRestart(HTTPTransfer transfer) {

			// Generamos un nuevo buffer pero, en
			// algunos casos, arrastramos un remanente
			hint = HTTPHint.NO_HINT;
			HTTPTransfer nextTransfer = new HTTPBuffer();
			if (transfer != null) {

				return onTransition(nextTransfer, transfer);
			}
			return nextTransfer;
		}

		public HTTPTransfer onTransition(HTTPTransfer nextTransfer, HTTPTransfer transfer) {

			// Consumimos el remanenete en el
			// siguiente "transfer" de la cadena

			ByteBuffer buf = transfer.getRemanent();

			return nextTransfer(nextTransfer, transfer.getRemanent());
		}

		public HTTPTransfer onStuck(HTTPTransfer transfer) {

			// Propagamos el mismo "transfer" que
			// dice saber que hacer con el mensaje
			return transfer;
		}

		public HTTPTransfer onError(HTTPTransfer transfer, HTTPStatusCode statusCode) {

			// No usamos el remanente, porque
			// vamos a cerrar la conexion con este host
			return null;
		}

		public HTTPTransfer onUnexpected(HTTPTransfer transfer, HTTPStatusCode statusCode) {

			// Tambien cerramos la conexion,
			// porque hubo un error interno (desconocido)
			return null;
		}

		protected HTTPTransfer nextTransfer(HTTPTransfer transfer, ByteBuffer buffer) {

			HTTPStatusCode statusCode = transfer.consume(buffer);
			if (statusCode == HTTPStatusCode.CONTINUE) {

				// Se necesita consumir mas informacion
				// para determinar la estructura del mensaje
				return onContinue(transfer);
			}
			else if (statusCode == HTTPStatusCode.OK) {

				// Se pudo construir un request/response y
				// es necesario finalizar o construir el body
				onAcceptance(transfer);
				HTTPTransfer nextTransfer = transfer.getTransfer();

				if (nextTransfer == null) {

					// El mensaje no posee body y por
					// lo tanto ya esta completo
					onComplete(transfer);
					return onRestart(transfer);
				}
				else if (nextTransfer != transfer) {

					// Se cargo la primera parte del mensaje y
					// ahora es necesario cambiar de "transfer"
					return onTransition(nextTransfer, transfer);
				}
				else {

					// Se cargo una parte pero el mismo "transfer"
					// sabe como parsear la siguiente (poco comun)
					return onStuck(nextTransfer);
				}
			}
			else if (statusCode.isError()) {

				// El mensaje posee una estructura invalida
				// o se produjo un error dentro del sistema
				return onError(transfer, statusCode);
			}
			else {

				// Se produjo un error, pero es imposible
				// determinar el tipo del mismo
				return onUnexpected(transfer, statusCode);
			}
		}
	}

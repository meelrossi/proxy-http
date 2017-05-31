
	package ar.edu.itba.proxy.http.transfer;

	import java.nio.ByteBuffer;

	import ar.edu.itba.leet.LeetTransformation;
	import ar.edu.itba.proxy.http.HTTPMessage;
	import ar.edu.itba.proxy.http.HTTPTransfer;
	import ar.edu.itba.proxy.http.syntax.HTTPStatusCode;

	public class NonPersistentTransfer implements HTTPTransfer {

		// La configuracion dura para toda la transferencia
		// (esto lo hace mas consistente)
		private boolean transform = LeetTransformation.isActive();

		private HTTPMessage message = null;

		public HTTPStatusCode consume(ByteBuffer buffer) {

			// La transferencia existe mientras haya
			// una recepcion continua de bytes.
			//
			// En algun momento, el proxy determina que
			// la conexion fue cerrada, y genera un evento
			// del tipo onRestart() dentro de onContinue().
			return leet(buffer);
		}

		public ByteBuffer getRemanent() {

			// Nunca tiene remanente
			return ByteBuffer.allocate(0);
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

		private HTTPStatusCode leet(ByteBuffer buffer) {

			if (transform) {

				if (!LeetTransformation.transform(buffer, message)) {

					return HTTPStatusCode.INTERNAL_SERVER_ERROR;
				}
			}
			return HTTPStatusCode.CONTINUE;
		}
	}

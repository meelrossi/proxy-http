
	package ar.edu.itba.proxy.http.transfer;

	import java.nio.ByteBuffer;

import ar.edu.itba.leet.LeetTransformation;
import ar.edu.itba.proxy.http.HTTPMessage;
import ar.edu.itba.proxy.http.HTTPTransfer;
import ar.edu.itba.proxy.http.syntax.HTTPStatusCode;

	public class ProgressiveTransfer implements HTTPTransfer {

		private HTTPMessage message = null;
		private ByteBuffer remanent = ByteBuffer.allocate(0);
		private int remaining = 0;

		// La configuracion dura para toda la transferencia
		// (esto lo hace mas consistente)
		private boolean transform = LeetTransformation.isActive();

		public ProgressiveTransfer(int remaining) {

			this.remaining = remaining;
		}

		public HTTPStatusCode consume(ByteBuffer buffer) {

			int bytes = buffer.remaining();
			if (remaining > bytes) {

				remaining -= bytes;
				return leet(buffer);
			}
			else if (remaining == 0) {

				// Solo admite una "sobre-escritura". El resto
				// se considera un error de transferencia.
				if (message.isRequest()) {
					return HTTPStatusCode.BAD_REQUEST;
				}
				else {
					return HTTPStatusCode.BAD_RESPONSE;
				}
			}
			else {

				HTTPStatusCode statusCode = leet(buffer);

				remanent = buffer;
				remanent.position(buffer.limit() + remaining - bytes);
				remaining = 0;

				if (statusCode == HTTPStatusCode.CONTINUE) {

					return HTTPStatusCode.OK;
				}
				return HTTPStatusCode.INTERNAL_SERVER_ERROR;
			}
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

		private HTTPStatusCode leet(ByteBuffer buffer) {

			if (transform) {

				if (!LeetTransformation.transform(buffer, message)) {

					return HTTPStatusCode.INTERNAL_SERVER_ERROR;
				}
			}
			return HTTPStatusCode.CONTINUE;
		}
	}


	package ar.edu.itba.proxy.http.parser.state;

	import java.nio.Buffer;

	import ar.edu.itba.proxy.http.HTTPMessage;
	import ar.edu.itba.proxy.http.parser.HTTPState;
	import ar.edu.itba.proxy.http.syntax.HTTPStatusCode;

	public final class HTTPMessageReady implements HTTPState {

		private HTTPMessage message;

		public HTTPMessageReady(HTTPMessage message) {

			this.message = message;
		}

		public HTTPState nextState(Buffer buffer) {

			return new HTTPInvalidParsing(HTTPStatusCode.INTERNAL_SERVER_ERROR);
		}

		public HTTPMessage getMessage() {

			return message;
		}

		public boolean isFinal() {

			return true;
		}

		public boolean isValid() {

			return true;
		}
	}

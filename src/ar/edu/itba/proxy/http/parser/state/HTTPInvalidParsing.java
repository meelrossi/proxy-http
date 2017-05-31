
	package ar.edu.itba.proxy.http.parser.state;

	import java.nio.Buffer;

	import ar.edu.itba.proxy.http.parser.HTTPState;
	import ar.edu.itba.proxy.http.syntax.HTTPStatusCode;

	public final class HTTPInvalidParsing implements HTTPState {

		private HTTPStatusCode statusCode = HTTPStatusCode.UNKNOWN;

		public HTTPInvalidParsing(HTTPStatusCode statusCode) {

			this.statusCode = statusCode;
		}

		public HTTPState nextState(Buffer buffer) {

			return null;
		}

		public boolean isFinal() {

			return true;
		}

		public boolean isValid() {

			return false;
		}

		public HTTPStatusCode getStatusCode() {

			return statusCode;
		}
	}

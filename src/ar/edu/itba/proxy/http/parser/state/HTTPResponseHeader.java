
	package ar.edu.itba.proxy.http.parser.state;

	import java.nio.Buffer;

	import ar.edu.itba.proxy.http.HTTPResponse;
	import ar.edu.itba.proxy.http.parser.HTTPState;

	public final class HTTPResponseHeader implements HTTPState {

		private HTTPResponse response;

		public HTTPResponseHeader(HTTPResponse response) {

			this.response = response;
		}

		public HTTPState nextState(Buffer buffer) {

			// Como no procesamos el buffer, lo forwardeamos
			HTTPState state = new HTTPUserAgentHeader(response);
			return state.nextState(buffer);
		}

		public boolean isFinal() {

			return false;
		}

		public boolean isValid() {

			return true;
		}
	}

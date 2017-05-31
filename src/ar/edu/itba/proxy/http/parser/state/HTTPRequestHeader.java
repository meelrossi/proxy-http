
	package ar.edu.itba.proxy.http.parser.state;

	import java.nio.Buffer;

	import ar.edu.itba.proxy.http.HTTPRequest;
	import ar.edu.itba.proxy.http.parser.HTTPState;

	public final class HTTPRequestHeader implements HTTPState {

		private HTTPRequest request;

		public HTTPRequestHeader(HTTPRequest request) {

			this.request = request;
		}

		public HTTPState nextState(Buffer buffer) {

			// Como no procesamos el buffer, lo forwardeamos
			HTTPState state = new HTTPUserAgentHeader(request);
			return state.nextState(buffer);
		}

		public boolean isFinal() {

			return false;
		}

		public boolean isValid() {

			return true;
		}
	}

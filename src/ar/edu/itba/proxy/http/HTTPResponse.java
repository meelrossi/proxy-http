
	package ar.edu.itba.proxy.http;

	import ar.edu.itba.proxy.http.syntax.HTTPStatusCode;

	public final class HTTPResponse extends HTTPMessage {

		private HTTPStatusCode statusCode = HTTPStatusCode.UNKNOWN;

		public HTTPStatusCode getStatusCode() {

			return statusCode;
		}

		public void setStatusCode(String statusCode) {

			this.statusCode = HTTPStatusCode.getStatusCode(statusCode);
		}

		@Override
		public String toString() {

			return "RESPONSE\n\n\t" + super.toString()
					+ "\n\tStatus Code: " + getStatusCode().getName()
					+ " (" + getStatusCode().getStatusCode() + ")";
		}

		@Override
		public boolean isRequest() {

			return false;
		}

		@Override
		public boolean mustHaveNullBody() {

			// No es posible identificar un "HEAD Response", y por lo tanto,
			// es necesario agregar un mecanismo adicional (hint).
			if (hint == HTTPHint.HEAD_RESPONSE) return true;
			if (statusCode == HTTPStatusCode.NO_CONTENT) return true;
			if (statusCode == HTTPStatusCode.NOT_MODIFIED) return true;
			if (statusCode.isInformational()) return true;
			return false;
		}
	}

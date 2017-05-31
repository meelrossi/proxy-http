
	package ar.edu.itba.proxy.http.syntax;

	public enum HTTPStatusCode {

		//Informational:
		CONTINUE						("100", "Continue"),

		// Successful:
		OK								("200", "Ok"),
		NO_CONTENT						("204", "No Content"),

		// Redirection's:
		NOT_MODIFIED					("304", "Not Modified"),

		// Client Error:
		BAD_REQUEST						("400", "Bad Request"),
		METHOD_NOT_ALLOWED				("405", "Method Not Allowed"),
		REQUEST_TIMEOUT					("408", "Request Timeout"),
		LENGTH_REQUIRED					("411", "Length Required"),
		REQUEST_ENTITY_TOO_LARGE		("413", "Request Entity Too Large"),

		// Server Error:
		INTERNAL_SERVER_ERROR			("500", "Internal Server Error"),
		NOT_IMPLEMENTED					("501", "Not Implemented"),
		GATEWAY_TIMEOUT					("504", "Gateway Timeout"),
		HTTP_VERSION_NOT_SUPPORTED		("505", "HTTP Version Not Supported"),

		// Proxy-Defined Error:
		BAD_RESPONSE					("900", "Bad Response"),

		UNKNOWN							("0", "Unknown Status Code");

		private String statusCode;
		private String name;

		private HTTPStatusCode(String statusCode, String name) {

			this.statusCode = statusCode;
			this.name = name;
		}

		public String getName() {

			return name;
		}

		public int getNumericalStatusCode() {

			return Integer.parseInt(statusCode);
		}

		public String getStatusCode() {

			return statusCode;
		}

		public boolean isError() {

			if (statusCode.startsWith("4")) return true;
			if (statusCode.startsWith("5")) return true;
			if (statusCode.startsWith("9")) return true;
			return false;
		}

		public boolean isInformational() {

			if (statusCode.startsWith("1")) return true;
			return false;
		}

		public static HTTPStatusCode getStatusCode(String statusCode) {

			for (HTTPStatusCode code : HTTPStatusCode.values()) {

				if (code.getStatusCode().equals(statusCode)) {

					return code;
				}
			}
			return UNKNOWN;
		}
	}

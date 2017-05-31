
	package ar.edu.itba.proxy.http.syntax;

	public enum HTTPMethod {

		// Solo soportamos estos metodos:
		GET			("GET"),
		HEAD		("HEAD"),
		POST		("POST"),

		UNKNOWN		("UNKNOWN");

		private String name;

		private HTTPMethod(String name) {

			this.name = name;
		}

		public String getName() {

			return name;
		}

		public static HTTPMethod getMethod(String methodName) {

			for (HTTPMethod method : HTTPMethod.values()) {

				if (method.getName().equals(methodName)) {

					return method;
				}
			}
			return UNKNOWN;
		}
	}

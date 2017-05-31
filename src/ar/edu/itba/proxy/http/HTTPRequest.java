
	package ar.edu.itba.proxy.http;

	import ar.edu.itba.proxy.http.syntax.HTTPHeader;
	import ar.edu.itba.proxy.http.syntax.HTTPMethod;

	public final class HTTPRequest extends HTTPMessage {

		private HTTPMethod method = HTTPMethod.UNKNOWN;
		private String URL = "/";
		private String host = "";
		private int port = HTTPSpecification.HTTP_PORT;

		public String getHost() {

			if (!host.equals("")) return host;
			return getValue(HTTPHeader.HOST);
		}

		public int getPort() {

			return port;
		}

		public HTTPMethod getMethod() {

			return method;
		}

		public String getURL() {

			return URL;
		}
		
		public String getCompleteHost() {
			String host = headers.get("host");
			if(host == null) {
				return "";
			}
			return host;
		}

		public void setMethod(String methodName) {

			method = HTTPMethod.getMethod(methodName);
		}

		public void setURL(String URL) {

			this.URL = URL;
		}

		public void setHost(String host) {

			if (host != null) this.host = host;
			else this.host = "";
		}

		public void setPort(String port) {

			if (port != null) {

				try {

					int value = Integer.parseInt(port);
					if (value > 0 && value < 65536) {

						this.port = value;
						return;
					}
				}
				catch (Exception exception) {}
			}
			this.port = HTTPSpecification.HTTP_PORT;
		}

		@Override
		public String toString() {

			return "REQUEST\n\n\t" + super.toString()
					+ "\n\tURL: " + getURL()
					+ "\n\tHost: " + getHost()
					+ "\n\tPort: " + getPort()
					+ "\n\tHTTP Method: " + getMethod().getName();
		}

		@Override
		public boolean isRequest() {

			return true;
		}

		@Override
		public boolean mustHaveNullBody() {

			if (method == HTTPMethod.POST) return false;
			return true;
		}
	}

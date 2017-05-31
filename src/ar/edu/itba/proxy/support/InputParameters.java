
	package ar.edu.itba.proxy.support;

	public final class InputParameters {

		private String IP;
		private int port;

		public InputParameters() {

			port = 80;
			IP = "0.0.0.0";
		}

		public String getIP() {

			return IP;
		}

		public int getPort() {

			return port;
		}

		public void setIP(String IP) {

			this.IP = IP;
		}

		public void setPort(int port) {

			this.port = port;
		}
	}

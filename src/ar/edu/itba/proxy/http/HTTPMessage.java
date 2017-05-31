
	package ar.edu.itba.proxy.http;

	import java.util.HashMap;
	import java.util.regex.Matcher;

	import ar.edu.itba.proxy.http.parser.HTTPParser;
	import ar.edu.itba.proxy.http.syntax.HTTPHeader;

	public abstract class HTTPMessage {

		protected HashMap<String, String> headers = new HashMap<String, String>();
		protected HTTPHint hint = HTTPHint.NO_HINT;
		protected HTTPTransfer transfer = null;

		public HTTPHint getHint() {

			return hint;
		}

		public String getValue(HTTPHeader header) {

			String result = headers.get(header.getName());
			if (result != null) return result;
			return "";
		}

		public int getLength() {

			String value = getValue(HTTPHeader.CONTENT_LENGTH);
			if (value.equals("")) return -1;
			try {

				return Integer.parseInt(value);
			}
			catch (Exception exception) {

				return -1;
			}
		}

		public HTTPTransfer getTransfer() {

			return transfer;
		}

		public String getCharset() {

			String value = getValue(HTTPHeader.CONTENT_TYPE).toLowerCase();
			Matcher result = HTTPParser.contentCharset.matcher(value);
			if (result.matches()) return result.group(1);
			return HTTPSpecification.DEFAULT_CHARSET;
		}

		public void setHint(HTTPHint hint) {

			this.hint = hint;
		}

		public void setTransfer(HTTPTransfer transfer) {

			if (transfer != null) {

				transfer.setMessage(this);
			}
			this.transfer = transfer;
		}

		public void addHeader(String header, String value) {

			headers.put(header.toLowerCase(), value);
		}

		public boolean isPersistent() {

			String value = getValue(HTTPHeader.CONNECTION).toLowerCase();
			if (value.equals("keep-alive")) return true;
			return false;
		}

		public boolean isChunked() {

			String value = getValue(HTTPHeader.TRANSFER_ENCODING).toLowerCase();
			if (value.equals("")) return false;
			return value.contains("chunked");
		}

		public boolean isText() {

			String value = getValue(HTTPHeader.CONTENT_TYPE).toLowerCase();
			return value.contains("text/plain");
		}

		public boolean isIdentical() {

			String value = getValue(HTTPHeader.CONTENT_ENCODING);
			if (value.equals("")) {

				// Si no se especifica un Content-encoding, debemos
				// verificar el header Transfer-encoding
				if (isChunked()) {

					value = getValue(HTTPHeader.TRANSFER_ENCODING);
					Matcher result = HTTPParser.chunkedTransfer.matcher(value);
					if (result.matches()) return true;
				}
				else return true;
			}
			else if (value.contains("identity")) return true;
			return false;
		}

		@Override
		public String toString() {

			return "\n\tisPersistent: " + isPersistent()
					+ "\n\tisChunked: " + isChunked()
					+ "\n\tisText: " + isText()
					+ "\n\tCharset: " + getCharset()
					+ "\n\tLength: " + getLength();
		}

		public abstract boolean isRequest();
		public abstract boolean mustHaveNullBody();
	}

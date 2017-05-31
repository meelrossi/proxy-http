
	package ar.edu.itba.proxy.http.parser.state;

	import java.nio.Buffer;
import java.nio.CharBuffer;
import java.util.regex.Matcher;

import ar.edu.itba.proxy.http.HTTPRequest;
import ar.edu.itba.proxy.http.HTTPResponse;
import ar.edu.itba.proxy.http.parser.HTTPParser;
import ar.edu.itba.proxy.http.parser.HTTPState;
import ar.edu.itba.proxy.http.syntax.HTTPMethod;
import ar.edu.itba.proxy.http.syntax.HTTPStatusCode;

	public final class HTTPServiceLine implements HTTPState {

		public HTTPServiceLine() {

			return;
		}

		public HTTPState nextState(Buffer buffer) {

			CharBuffer charBuffer = (CharBuffer) buffer;

			Matcher result = HTTPParser.request.matcher(charBuffer);

			if (result.matches()) {

				HTTPRequest request = new HTTPRequest();
				request.setMethod(result.group(1));
				request.setHost(result.group(2));
				request.setPort(result.group(3));
				request.setURL(result.group(4));

				if (request.getMethod() == HTTPMethod.UNKNOWN) {
					return new HTTPInvalidParsing(HTTPStatusCode.METHOD_NOT_ALLOWED);
				}
				return new HTTPRequestHeader(request);
			}
			else {

				result = HTTPParser.response.matcher(charBuffer);

				if (result.matches()) {

					HTTPResponse response = new HTTPResponse();
					response.setStatusCode(result.group(1));
					return new HTTPResponseHeader(response);
				}
				return new HTTPInvalidParsing(HTTPStatusCode.BAD_REQUEST);
			}
		}

		public boolean isFinal() {

			return false;
		}

		public boolean isValid() {

			return true;
		}
	}

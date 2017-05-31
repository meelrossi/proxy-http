
package ar.edu.itba.proxy.http;

import java.nio.ByteBuffer;

import ar.edu.itba.proxy.http.parser.HTTPParser;
import ar.edu.itba.proxy.http.parser.HTTPState;
import ar.edu.itba.proxy.http.parser.state.HTTPInvalidParsing;
import ar.edu.itba.proxy.http.parser.state.HTTPMessageReady;
import ar.edu.itba.proxy.http.syntax.HTTPStatusCode;

public final class HTTPBuffer implements HTTPTransfer {

	private HTTPParser parser = new HTTPParser(HTTPSpecification.HEADER_BUFFER_SIZE);
	private HTTPMessage message = null;

	public HTTPStatusCode consume(ByteBuffer buffer) {

		HTTPState state = parser.feed(buffer);
		if (state.isFinal()) {

			if (state.isValid()) {

				message = ((HTTPMessageReady) state).getMessage();
				return HTTPStatusCode.OK;
			} else {

				parser.reset();
				message = null;
				return ((HTTPInvalidParsing) state).getStatusCode();
			}
		}
		return HTTPStatusCode.CONTINUE;
	}

	public ByteBuffer getRemanent() {

		return parser.getRemanent();
	}

	public HTTPTransfer getTransfer() {

		if (message != null) {

			return message.getTransfer();
		}
		return null;
	}

	public HTTPMessage getMessage() {

		return message;
	}

	public void setMessage(HTTPMessage message) {

		this.message = message;
	}
}

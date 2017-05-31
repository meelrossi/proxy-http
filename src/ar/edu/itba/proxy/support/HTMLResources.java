package ar.edu.itba.proxy.support;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import ar.edu.itba.proxy.http.syntax.HTTPStatusCode;

/*
 **	Esta clase permite precargar un conjunto de
 **	paginas HTML-5, las cuales seran utilizadas por
 **	el sistema en caso de que se presente la necesidad
 **	de reflejarle a un cliente determinado el tipo de
 **	error o el estado de las operaciones realizadas.
 **
 **	Basicamente, permite transformar un HTTPStatusCode, en
 **	un documento HTML-5 que lo describa (almacenado dentro
 **	de un ByteBuffer).
 **
 **	Debido a que utiliza metodos bloqueantes, el mapa de
 **	recursos debe instalarse antes de que el proxy comience
 **	a aceptar conexiones entrantes.
 */

public final class HTMLResources {

	private HashMap<HTTPStatusCode, ByteBuffer> resources = new HashMap<HTTPStatusCode, ByteBuffer>();

	public HTMLResources() {
		load();
		return;
	}

	private boolean load() {
		String folder = System.getProperty("user.dir") + "/resource/";

		try {

			for (HTTPStatusCode statusCode : HTTPStatusCode.values()) {

				String filename = folder + getFilename(statusCode);
				ByteBuffer buffer = read(filename);

				resources.put(statusCode, buffer);
			}
		} catch (Exception exception) {
			return false;
		}
		return true;
	}

	public ByteBuffer getHTML(HTTPStatusCode statusCode) {

		ByteBuffer buffer = resources.get(statusCode);
		buffer.position(0);
		buffer.limit(buffer.capacity());
		return buffer;
	}

	public ByteBuffer getMessage(HTTPStatusCode statusCode) {

		int length;
		if (statusCode == HTTPStatusCode.NO_CONTENT || statusCode == HTTPStatusCode.NOT_MODIFIED
				|| statusCode.isInformational())
			length = 0;
		else {

			ByteBuffer buffer = getHTML(statusCode);
			length = buffer.remaining();
		}

		String message = "HTTP/1.1 " + statusCode.getStatusCode() + " " + statusCode.getName() + "\r\n"
				+ "Server: ITBA-Proxy HTTP/1.1 (2015)\r\n" + "Content-length: " + length + "\r\n"
				+ "Content-type: text/html; charset=utf-8\r\n\r\n";

		return ByteBuffer.wrap(message.getBytes());
	}

	public ByteBuffer getProxyResponse(HTTPStatusCode statusCode) {
		ByteBuffer headers = getMessage(statusCode);
		ByteBuffer body = getHTML(statusCode);

		ByteBuffer response = ByteBuffer.allocate(headers.limit() + body.limit());

		return response.put(headers).put(body);
	}

	private String getFilename(HTTPStatusCode statusCode) {

		return statusCode.name() + ".html";
	}

	private ByteBuffer read(String filename) throws IOException {

		Path path = FileSystems.getDefault().getPath("", filename);
		ByteBuffer buffer = ByteBuffer.wrap(Files.readAllBytes(path));
		return buffer;
	}
}

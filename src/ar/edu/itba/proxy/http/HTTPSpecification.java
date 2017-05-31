
	package ar.edu.itba.proxy.http;

	public interface HTTPSpecification {

		// Puerto reservado para el protocolo HTTP/1.1
		public static final int HTTP_PORT = 80;

		// Buffer maximo reservado para un mensaje HTTP
		public static final int HEADER_BUFFER_SIZE = 4096;

		// Carpeta de paginas HTML-5 asociadas a cada HTTPStatusCode
		public static final String HTML_STATUS_CODES = "resource/";

		// Encoding estandar utilizado por el protocolo para los header's
		public static final String DEFAULT_CHARSET = "ISO-8859-1";
	}


	package ar.edu.itba.proxy.http.syntax;

	public enum HTTPHeader {

		ACCEPT_ENCODING		("accept-encoding"),		// Indica que formatos soporta el cliente
		CONNECTION			("connection"),				// Especifica si la conexion es persistente
		CONTENT_ENCODING	("content-encoding"),		// Especifica el formato (encoding) del body
		CONTENT_LENGTH		("content-length"),			// Indica el tamao de la seccion de datos
		CONTENT_TYPE		("content-type"),			// Especifica el MIME Type
		HOST				("host"),					// Indica el nombre del servidor al cual conectarse
		TRANSFER_ENCODING	("transfer-encoding"),		// Especifica si la seccion de datos se transfiere por partes

		UNKNOWN				("Unknown Header");

		private String name;

		private HTTPHeader(String name) {

			this.name = name;
		}

		public String getName() {

			return name;
		}
	}

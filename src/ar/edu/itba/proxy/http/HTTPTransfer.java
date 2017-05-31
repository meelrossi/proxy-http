
	package ar.edu.itba.proxy.http;

	import java.nio.ByteBuffer;

	import ar.edu.itba.proxy.http.syntax.HTTPStatusCode;

	/*
	**	Todos los metodos definidos a continuacion deben
	**	asegurar la propiedad "Non-Blocking" de la transferencia,
	**	es decir, que las siguientes operaciones son no-bloqueantes.
	*/

	public interface HTTPTransfer {

		public HTTPStatusCode consume(ByteBuffer buffer);

		public ByteBuffer getRemanent();
		public HTTPTransfer getTransfer();
		public HTTPMessage getMessage();

		// Permite conectar un HTTPMessage con su HTTPTransfer
		public void setMessage(HTTPMessage message);
	}

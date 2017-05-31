
	package ar.edu.itba.proxy.http;

	import ar.edu.itba.proxy.http.syntax.HTTPStatusCode;

	/*
	**	La siguiente interfaz permite desacoplar todo
	**	el sistema de parsing del resto de las aplicaciones,
	**	y representar cada uno de los estados del automata
	**	mediante un metodo especifico (trigger).
	*/

	public interface HTTPTrigger {

		// Este evento asume activo el estado HTTPStatusCode.CONTINUE
		public HTTPTransfer onContinue(HTTPTransfer transfer);

		// Estos eventos asumen activo el estado HTTPStatusCode.OK
		public void onAcceptance(HTTPTransfer transfer);
		public void onComplete(HTTPTransfer transfer);
		public HTTPTransfer onRestart(HTTPTransfer transfer);
		public HTTPTransfer onTransition(HTTPTransfer nextTransfer, HTTPTransfer transfer);
		public HTTPTransfer onStuck(HTTPTransfer transfer);

		// Este evento asume activo algun estado de error
		public HTTPTransfer onError(HTTPTransfer transfer, HTTPStatusCode statusCode);

		// Este evento asume que el estado no se puede reconocer
		public HTTPTransfer onUnexpected(HTTPTransfer transfer, HTTPStatusCode statusCode);
	}

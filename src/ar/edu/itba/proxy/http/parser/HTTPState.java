
	package ar.edu.itba.proxy.http.parser;

	import java.nio.Buffer;

	/*
	**	Un estado posee un unico metodo, que genera otro estado
	**	nuevo (el siguiente) en base a un buffer de entrada.
	**
	**	Como regla general, si un estado no procesa el buffer recibido
	**	debe pasarlo (forwarding) al siguiente estado.
	*/

	public interface HTTPState {

		public HTTPState nextState(Buffer buffer);

		public boolean isFinal();
		public boolean isValid();
	}

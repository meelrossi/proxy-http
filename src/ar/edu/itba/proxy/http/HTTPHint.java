
	package ar.edu.itba.proxy.http;

	/*
	**	Permite especificar un indicio para que
	**	cierto proceso o metodo pueda resolver
	**	una ambiguedad.
	*/

	public enum HTTPHint {

		// Indica que se esta procesando la respuesta
		// a un request del metodo HEAD, y por lo tanto
		// debe ocurrir que "mustHaveNullBody() = true".
		HEAD_RESPONSE,

		// No es necesario insinuar nada, debido a que
		// no deberia existir ambiguedad.
		NO_HINT;
	}

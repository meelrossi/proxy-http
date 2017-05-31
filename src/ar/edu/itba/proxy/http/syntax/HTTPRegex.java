
	package ar.edu.itba.proxy.http.syntax;

	/*
	** [RFC.7230] HTTP/1.1 Message Syntax & Routing
	*/

	public interface HTTPRegex {

		public static final String CHARSET				= ".*;\\s*charset\\s*=\\s*([^ \t;]+)\\s*(?:;.*)*";
		public static final String CHUNKED_TRANSFER		= "(?:\\s*identity\\s*,)?\\s*chunked\\s*";
		public static final String CHUNK_HEADER			= "([0-9a-fA-F]+)\\s*(?:;[ \t!-~]*)?\r\n";
		public static final String COLON				= ":";
		public static final String DELIMITER			= "(?:\r?\n)";
		public static final String HEADER_NAME			= "([!-9;-~]+)";
		public static final String HEADER_VALUE			= "([ \t!-~]+)";
		public static final String METHOD				= "([A-Z]+)";
		public static final String OPTIONAL_SPACE		= "\\s*";
		public static final String REASON_PHRASE		= "[ \t!-~]*";
		public static final String SPACE				= "\\s+";
		public static final String STATUS_CODE			= "([0-9]{3})";
		public static final String URL					= "(?:http:/+([!-.0-9;-~]+)(?::([0-9]+))?)?(/[!-~]*)";
		public static final String VERSION				= "HTTP/1\\.(?:0|1)";

		public static final String REQUEST_HEADER =
				OPTIONAL_SPACE + METHOD + SPACE + URL
				+ SPACE + VERSION + OPTIONAL_SPACE + DELIMITER;

		public static final String RESPONSE_HEADER =
				OPTIONAL_SPACE + VERSION + SPACE + STATUS_CODE
				+ SPACE + REASON_PHRASE + OPTIONAL_SPACE + DELIMITER;

		public static final String HTTP_HEADER =
				OPTIONAL_SPACE + HEADER_NAME + OPTIONAL_SPACE + COLON
				+ OPTIONAL_SPACE + HEADER_VALUE + OPTIONAL_SPACE + DELIMITER;
	}

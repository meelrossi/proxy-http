
	package ar.edu.itba.proxy.support;

	public interface NetworkRegex {

		public static String PORT_REGEX =
				"(6[0-5]{2}[0-3][0-5]|[1-5]\\d{1,4}|[1-9]\\d{0,3}|0)";

		public static String IPv4_REGEX =
				"((?:(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|\\d)\\.){3}"
				+ "(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|\\d)"
				+ "|localhost)";
	}

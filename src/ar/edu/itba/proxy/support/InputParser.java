
	package ar.edu.itba.proxy.support;

	import java.util.regex.Matcher;
	import java.util.regex.Pattern;

	public final class InputParser {

		public static boolean load(String [] arguments, InputParameters parameters) {

			/*
			** Ejemplo: proxy 192.168.0.10 80
			*/

			if (arguments.length != 2) return false;

			Matcher [] results = {

				Pattern.compile(NetworkRegex.IPv4_REGEX).matcher(arguments[0]),
				Pattern.compile(NetworkRegex.PORT_REGEX).matcher(arguments[1])
			};

			boolean valid = true;
			for (Matcher result : results) {

				valid &= result.matches();
			}
			if (valid) {

				parameters.setIP(results[0].group(1));
				parameters.setPort(Integer.parseInt(results[1].group(1)));
			}

			return valid;
		}
	}

package ar.edu.itba.protocol;

import java.util.regex.Pattern;

public class CommandsPattern {
	public static final Pattern HELP = Pattern.compile("HELP");
	public static final Pattern AUTHENTICATION = Pattern.compile("USER \\S+:\\S+");
	public static final Pattern EXIT = Pattern.compile("EXIT");
	
	/*Metrics*/
	public static final Pattern METRICS = Pattern.compile("METRICS");
	public static final Pattern METRICS_JSON = Pattern.compile("METRICS JSON");
	public static final Pattern CONNECTIONS_SERVER = Pattern.compile("CONNECTIONS SERVER");
	public static final Pattern CONNECTIONS_CLIENT = Pattern.compile("CONNECTIONS CLIENT");
	public static final Pattern CONNECTIONS_METRICS = Pattern.compile("CONNECTIONS METRICS");
	public static final Pattern CONNECTIONS_CONFIGURATION = Pattern.compile("CONNECTIONS CONFIGURATION");
	public static final Pattern BYTES_CLIENT = Pattern.compile("BYTES CLIENT");
	public static final Pattern BYTES_SERVER = Pattern.compile("BYTES SERVER");
	public static final Pattern TRANSFORMATIONS = Pattern.compile("TRANSFORMATIONS");
	
	/*Configuration*/
	public static final Pattern LEET_ON = Pattern.compile("L33T ON");
	public static final Pattern LEET_OFF = Pattern.compile("L33T OFF");
	public static final Pattern CONFIGURATION = Pattern.compile("CONFIGURATION");
	public static final Pattern LOG_DEBUG = Pattern.compile("LOG DEBUG");
	public static final Pattern LOG_INFO = Pattern.compile("LOG INFO");
	public static final Pattern LOG_ERROR = Pattern.compile("LOG ERROR");
	public static final Pattern LOG_FATAL = Pattern.compile("LOG FATAL");
	public static final Pattern LOG_OFF = Pattern.compile("LOG OFF");
	public static final Pattern LOG_WARN = Pattern.compile("LOG WARN");
	
}

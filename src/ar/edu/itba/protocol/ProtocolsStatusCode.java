package ar.edu.itba.protocol;

public enum ProtocolsStatusCode {
	OK("20", "OK"),
	UNDECLARED_METHOD("30","Unable to recognize method"),
	ERROR("50", "ERROR"), 
	NOT_AUTHENTICATED("60", "Authenticate yourself: USER username:password"),
	AUTHENTIFICATION_SUCCESS("20", "Your authentiation finished successfully. Welcome"),
	AUTHENTIFICATION_FAIL("50", "Not able to authenticate. Try again"),
	EXIT("20", "ByeBye"),
	
	/*Configuration protocol*/
	LEET_ON("20", "L33t transformation is now on"), 
	LEET_OFF("20", "L33t transformation is now off"), 
	LOG_DEBUG("20", "Logger now set on debug"),
	LOG_ERROR("20", "Logger now set on error"),
	LOG_INFO("20", "Logger now set on info"),
	LOG_FATAL("20", "Logger now set on fatal"),
	LOG_OFF("20", "Logger now off"),
	LOG_WARN("20", "Logger now set on warn");

	private String statusCode;
	private String name;

	private ProtocolsStatusCode(String statusCode, String name) {

		this.statusCode = statusCode;
		this.name = name;
	}

	public String getDescription() {
		return "Status code " + statusCode + " - " + name + "\r\n";
	}
}

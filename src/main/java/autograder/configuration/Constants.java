package autograder.configuration;

public interface Constants {

	public interface TaConfiguration {
		public static final String TA_NAME_HEADER = "name";
		public static final String TA_EMAIL_HEADER = "email";
		public static final String TA_HOURS_HEADER = "hours";
		public static final String[] TA_CSV_HEADERS = new String[] {TA_NAME_HEADER,TA_EMAIL_HEADER, TA_HOURS_HEADER};
	}

	public String DEFAULT_CONFIGURATION = "configuration.properties";
}

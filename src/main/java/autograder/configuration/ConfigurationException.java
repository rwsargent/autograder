package autograder.configuration;

@SuppressWarnings("serial")
public class ConfigurationException extends RuntimeException {
	private Exception mCause;
	public ConfigurationException(String message) {
		super(message);
	}
	
	public ConfigurationException(Exception cause) {
		mCause = cause;
	}
	
	public Exception maybeGetCause() {
		return mCause;
	}
	
	@Override
	public String getMessage() {
		if(mCause != null) {
			return mCause.getMessage();
		}
		return super.getMessage();
	}
}

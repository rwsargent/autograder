package autograder.configuration;

public class StudentIdRegistry {
	private static StudentIdRegistry mInstance;
	
	private StudentIdRegistry(){};
	
	public static StudentIdRegistry getInstance() {
		if (mInstance == null) {
			configure();
		}
		return mInstance;
	}

	private static void configure() {
		
	}
}

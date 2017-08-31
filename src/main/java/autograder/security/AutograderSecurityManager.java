package autograder.security;

public class AutograderSecurityManager extends SecurityManager {
	
	@Override
	public void checkExit(int status) {
		throw new SecurityException("An invalid call to System.exit() was made.");
	}
	
}

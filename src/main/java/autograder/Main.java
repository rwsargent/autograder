package autograder;

import com.google.inject.Guice;
import com.google.inject.Injector;

import autograder.modules.DefaultModule;

public class Main {
	
	public static void main(String... args ) {
		
		Injector injector = Guice.createInjector(getModule(args));
		
		Autograder autograder = injector.getInstance(Autograder.class);
		autograder.setup(args);
		autograder.run();
	}

	private static DefaultModule getModule(String... args) {
		return new DefaultModule();
	}
}

package autograder.modules;

import com.google.inject.AbstractModule;

import autograder.configuration.Configuration;

public class DefaultModule extends AbstractModule {

	@Override
	public void configure() {
		bind(Configuration.class).asEagerSingleton();
		
		
	}

}

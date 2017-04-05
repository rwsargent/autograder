package autograder.modules;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import autograder.configuration.Configuration;
import autograder.grading.Grader;
import autograder.phases.two.Worker;

public class DefaultModule extends AbstractModule {

	@Override
	public void configure() {
		bind(Configuration.class).asEagerSingleton();
		Multibinder<Worker> workerBinder = Multibinder.newSetBinder(binder(), Worker.class);
		
		workerBinder.addBinding().to(Grader.class);
	}
}

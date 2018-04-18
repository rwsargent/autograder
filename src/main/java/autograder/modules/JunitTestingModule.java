package autograder.modules;

import com.google.inject.multibindings.Multibinder;

import autograder.configuration.Configuration;
import autograder.phases.two.Worker;
import autograder.phases.two.workers.InternalJavaCompiler;
import autograder.phases.two.workers.JUnitGrader;

public class JunitTestingModule extends TestingModule {

	public JunitTestingModule(Configuration configuration) {
		super(configuration);
	}
	
	@Override
	protected void addPhaseTwoWorkers(Multibinder<Worker> workerBinder) {
		workerBinder.addBinding().to(InternalJavaCompiler.class);
		workerBinder.addBinding().to(JUnitGrader.class);
	}

}

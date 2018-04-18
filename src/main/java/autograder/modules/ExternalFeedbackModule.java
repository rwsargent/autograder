package autograder.modules;

import com.google.inject.multibindings.Multibinder;

import autograder.configuration.Configuration;
import autograder.phases.two.Worker;
import autograder.phases.two.workers.ExternalAutograderUtilsProcess;
import autograder.phases.two.workers.InternalJavaCompiler;

public class ExternalFeedbackModule extends FeedbackModule {

	public ExternalFeedbackModule(Configuration configuration) {
		super(configuration);
	}
	
	@Override
	protected void addPhaseTwoWorkers(Multibinder<Worker> workerBinder) {
		workerBinder.addBinding().to(InternalJavaCompiler.class);
		workerBinder.addBinding().to(ExternalAutograderUtilsProcess.class);
	}

}

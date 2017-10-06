package autograder.modules;

import org.mockito.Mockito;

import com.google.inject.multibindings.Multibinder;

import autograder.configuration.Configuration;
import autograder.filehandling.PredefinedPartition;
import autograder.filehandling.SubmissionPartitioner;
import autograder.mailer.Mailer;
import autograder.phases.three.AssignmentUploader;
import autograder.phases.three.SubmissionUploader;
import autograder.phases.three.uploaders.EmailBundleToTasUploader;
import autograder.phases.three.uploaders.FullAssignmentUpload;
import autograder.phases.three.uploaders.WrapUpGraderRun;
import autograder.phases.two.Worker;
import autograder.phases.two.workers.ExternalAutograderUtilsProcess;
import autograder.phases.two.workers.InternalJavaCompiler;

public class TestingModule extends DefaultModule {

	public TestingModule(Configuration configuration) {
		super(configuration);
	}
	
	@Override
	public void configure() {
		super.configure();
		bind(SubmissionPartitioner.class).to(PredefinedPartition.class);
		bind(Mailer.class).toInstance(Mockito.mock(Mailer.class)); // don't want to mail anything
		bind(WrapUpGraderRun.class).toInstance(Mockito.mock(WrapUpGraderRun.class));
	}
	
	@Override
	protected void addPhaseTwoWorkers(Multibinder<Worker> workerBinder) {
		workerBinder.addBinding().to(InternalJavaCompiler.class);
		workerBinder.addBinding().to(ExternalAutograderUtilsProcess.class);
	}
	
	@Override
	protected void addSubmissionUploaders(Multibinder<SubmissionUploader> submissionUploaders) {
		submissionUploaders.addBinding().toInstance(sub -> {
			System.out.println(sub + ": " + sub.getResult().getTestResults());
		});
	}
	
	@Override
	protected void addAssignmentUploaders(Multibinder<AssignmentUploader> assignmentUploaders) {
		super.addAssignmentUploaders(assignmentUploaders);
		assignmentUploaders.addBinding().to(FullAssignmentUpload.class);
		assignmentUploaders.addBinding().to(EmailBundleToTasUploader.class);
	}
}

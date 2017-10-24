package autograder.modules;

import org.mockito.Mockito;

import com.google.inject.multibindings.Multibinder;

import autograder.configuration.Configuration;
import autograder.filehandling.PredefinedPartition;
import autograder.filehandling.SubmissionPartitioner;
import autograder.mailer.Mailer;
import autograder.phases.three.AssignmentUploader;
import autograder.phases.three.SubmissionUploader;
import autograder.phases.three.uploaders.AssignmentMetricsUploader;
import autograder.phases.three.uploaders.FullAssignmentUpload;
import autograder.phases.three.uploaders.GradeSubmissionUploader;
import autograder.phases.two.Worker;
import autograder.phases.two.workers.ExternalAutograderUtilsProcess;
import autograder.phases.two.workers.InternalJavaCompiler;

/**
 * This module is designed for a single-run instance of the Autograder, 
 * when you want to post a final grade for a students submission. 
 * @author ryansargent
 */
public class GradingModule extends DefaultModule {

	public GradingModule(Configuration configuration) {
		super(configuration);
	}
	
	@Override
	public void configure() {
		super.configure();
		bind(SubmissionPartitioner.class).to(PredefinedPartition.class);
		bind(Mailer.class).toInstance(Mockito.mock(Mailer.class));
	}
	
	@Override
	protected void addPhaseTwoWorkers(Multibinder<Worker> workerBinder) {
		workerBinder.addBinding().to(InternalJavaCompiler.class);
		workerBinder.addBinding().to(ExternalAutograderUtilsProcess.class);
	}
	
	@Override
	protected void addSubmissionUploaders(Multibinder<SubmissionUploader> submissionUploaders) {
		super.addSubmissionUploaders(submissionUploaders);
		submissionUploaders.addBinding().to(GradeSubmissionUploader.class);
	}
	
	@Override
	protected void addAssignmentUploaders(Multibinder<AssignmentUploader> assignmentUploaders) {
		super.addAssignmentUploaders(assignmentUploaders);
		assignmentUploaders.addBinding().to(FullAssignmentUpload.class);
		assignmentUploaders.addBinding().to(AssignmentMetricsUploader.class);
	}
}

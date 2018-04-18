package autograder.modules;

import org.mockito.Mockito;

import com.google.inject.multibindings.Multibinder;

import autograder.configuration.Configuration;
import autograder.filehandling.AlphabeticPartitioner;
import autograder.filehandling.SubmissionPartitioner;
import autograder.phases.three.AssignmentUploader;
import autograder.phases.three.SubmissionUploader;
import autograder.phases.three.uploaders.AssignmentMetricsUploader;
import autograder.phases.three.uploaders.CreateStudentToScoreTSV;
import autograder.phases.three.uploaders.EmailBundleToTasUploader;
import autograder.phases.three.uploaders.FullAssignmentUpload;
import autograder.phases.three.uploaders.PostFeedbackCommentsUploader;
import autograder.phases.three.uploaders.SaveResultUploader;
import autograder.phases.three.uploaders.ScaledGraderUploader;
import autograder.phases.three.uploaders.WrapUpGraderRun;
import autograder.phases.three.uploaders.feedback.CommentContentGetter;
import autograder.phases.three.uploaders.feedback.FullFeedbackComment;
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
		bind(SubmissionPartitioner.class).to(AlphabeticPartitioner.class);
//		bind(Mailer.class).toInstance(Mockito.mock(Mailer.class));
		bind(WrapUpGraderRun.class).toInstance(Mockito.mock(WrapUpGraderRun.class));
		bind(CommentContentGetter.class).to(FullFeedbackComment.class);
//		bind(GradeCalculator.class).to(ScaledGradeCalculator.class);	
	}
	
	@Override
	protected void addPhaseTwoWorkers(Multibinder<Worker> workerBinder) {
		workerBinder.addBinding().to(InternalJavaCompiler.class);
		workerBinder.addBinding().to(ExternalAutograderUtilsProcess.class);
	}
	
	@Override
	protected void addSubmissionUploaders(Multibinder<SubmissionUploader> submissionUploaders) {
		super.addSubmissionUploaders(submissionUploaders);
		submissionUploaders.addBinding().to(SaveResultUploader.class);
		submissionUploaders.addBinding().to(PostFeedbackCommentsUploader.class);
		submissionUploaders.addBinding().to(ScaledGraderUploader.class);
	}
	
	@Override
	protected void addAssignmentUploaders(Multibinder<AssignmentUploader> assignmentUploaders) {
		assignmentUploaders.addBinding().to(FullAssignmentUpload.class);
		assignmentUploaders.addBinding().to(AssignmentMetricsUploader.class);
		assignmentUploaders.addBinding().to(CreateStudentToScoreTSV.class);
		assignmentUploaders.addBinding().to(EmailBundleToTasUploader.class);
//		assignmentUploaders.addBinding().to(GroupAndDistribute.class);
	}
}

package autograder.modules;

import com.google.inject.multibindings.Multibinder;

import autograder.configuration.Configuration;
import autograder.phases.three.AssignmentUploader;
import autograder.phases.three.SubmissionUploader;
import autograder.phases.three.uploaders.AssignmentMetricsUploader;
import autograder.phases.three.uploaders.GradeSubmissionUploader;

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
	protected void addSubmissionUploaders(Multibinder<SubmissionUploader> submissionUploaders) {
		super.addSubmissionUploaders(submissionUploaders);
		submissionUploaders.addBinding().to(GradeSubmissionUploader.class);
	}
	
	@Override
	protected void addAssignmentUploaders(Multibinder<AssignmentUploader> assignmentUploaders) {
		super.addAssignmentUploaders(assignmentUploaders);
		assignmentUploaders.addBinding().to(AssignmentMetricsUploader.class);
	}
}

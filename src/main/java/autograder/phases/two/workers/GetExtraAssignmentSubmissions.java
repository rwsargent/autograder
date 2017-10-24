package autograder.phases.two.workers;

import javax.inject.Inject;

import autograder.canvas.CanvasConnection;
import autograder.canvas.responses.Attachment;
import autograder.canvas.responses.Submission;
import autograder.configuration.Configuration;
import autograder.filehandling.SubmissionParser;
import autograder.phases.two.Worker;
import autograder.student.AutograderSubmission;

/**
 * A bit of a hack to get extra assignments downloaded
 * @author ryans
 *
 */
public class GetExtraAssignmentSubmissions implements Worker {
	
	private CanvasConnection portal;
	private SubmissionParser parser;
	private Configuration config;

	@Inject
	public GetExtraAssignmentSubmissions(CanvasConnection portal, SubmissionParser parser, Configuration configuration) {
		this.portal = portal;
		this.parser = parser;
		this.config = configuration;
	}
	
	@Override
	public void doWork(AutograderSubmission submission) {
		for(String assignmentId : config.extraAssignments) {
			Submission downloadedSubmission = portal.getUserSubmissions(submission.studentInfo, assignmentId);
			
			if(downloadedSubmission.attachments != null) {
				for(Attachment attachment: downloadedSubmission.attachments) {
					parser.writeAttatchmentToDisk(submission, attachment);
				}
			}
		}
	}
}

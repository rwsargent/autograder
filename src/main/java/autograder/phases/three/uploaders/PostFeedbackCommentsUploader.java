package autograder.phases.three.uploaders;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autograder.Constants;
import autograder.configuration.Configuration;
import autograder.phases.three.SubmissionUploader;
import autograder.portal.PortalConnection;
import autograder.student.AutograderSubmission;

public class PostFeedbackCommentsUploader implements SubmissionUploader {
	private final Logger LOGGER = LoggerFactory.getLogger(PostFeedbackCommentsUploader.class);
	
	private PortalConnection portal;
	private Configuration config;

	@Inject
	public PostFeedbackCommentsUploader(PortalConnection portal, Configuration configuration) {
		this.portal = portal;
		this.config = configuration;
	}
	
	@Override
	public void upload(AutograderSubmission submission) {
		LOGGER.info("Posting feedback comments to " + submission);
		Map<String, String> data = new HashMap<>();
		data.put(Constants.Canvas.SUBISSION_COMMENT, submission.getResult().getFeedback());
		portal.gradeStudentSubmission(Integer.toString(submission.studentInfo.id), config.canvasAssignmentId, data);
	}

}

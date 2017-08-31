package autograder.phases.three.uploaders;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
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
		data.put(Constants.CanvasApi.SUMBISSION_COMMENT_TEXT, getSubmissionCommentText(submission));
		portal.gradeStudentSubmission(Integer.toString(submission.studentInfo.id), config.canvasAssignmentId, data);
	}

	private String getSubmissionCommentText(AutograderSubmission submission) {
		String feedback = "";
		if(submission.getResult() != null && submission.getResult().getFeedback() != null) {
			feedback = submission.getResult().getFeedback();
		}
		
		if(submission.getProperty(Constants.SubmissionProperties.COMPILED, "").equals("false")) {
			File compErrorOutput = new File(submission.getDirectory(), Constants.COMPILE_ERROR_FILENAME);
			if(compErrorOutput.exists()) {
				try {
					String comperrors = new String(Files.readAllBytes(compErrorOutput.toPath()));
					feedback += "\nYour source files would not compile. Here's the compilation errors the autograder ran into:\n" + comperrors;
				} catch (IOException e) {
					LOGGER.error("Tried to get compilation text for " + submission, e);
				}
			}
		}
		
		if(StringUtils.isEmpty(feedback)) {
			feedback = "The autograder didn't produce any feedback for you. Contact the TAs with this information:\n"
					+ submission + " - " +  new SimpleDateFormat(Constants.BUNDLED_TIMESTAMP).format(Date.from(Instant.now())); 
		}
		
		return feedback;
	}
}

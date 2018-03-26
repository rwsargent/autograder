package autograder.phases.three.uploaders;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autograder.Constants;
import autograder.configuration.Configuration;
import autograder.mailer.Mailer;
import autograder.phases.three.SubmissionUploader;
import autograder.phases.three.uploaders.feedback.CommentContentGetter;
import autograder.portal.PortalConnection;
import autograder.student.AutograderSubmission;

public class PostFeedbackCommentsUploader implements SubmissionUploader {
	private final Logger LOGGER = LoggerFactory.getLogger(PostFeedbackCommentsUploader.class);
	
	private PortalConnection portal;
	private Configuration config;
	private Mailer mailer;

	private CommentContentGetter commentGetter;

	@Inject
	public PostFeedbackCommentsUploader(PortalConnection portal, Configuration configuration, Mailer emailer, CommentContentGetter commentContent) {
		this.portal = portal;
		this.config = configuration;
		this.mailer = emailer;
		this.commentGetter = commentContent;
	}
	
	@Override
	public void upload(AutograderSubmission submission) {
		LOGGER.info("Posting feedback comments to " + submission);
		Map<String, String> data = new HashMap<>();
		data.put(Constants.CanvasApi.SUMBISSION_COMMENT_TEXT, getSubmissionCommentText(submission));
		data.put(Constants.CanvasApi.GROUP_COMMENT, "true");
		
		portal.gradeStudentSubmission(Integer.toString(submission.studentInfo.id), config.canvasAssignmentId, data);
	}

	protected String getSubmissionCommentText(AutograderSubmission submission) {
		String feedback = "";
		
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
		
		if(submission.getResult() != null && submission.getResult().getFeedback() != null) {
			feedback += commentGetter.getComment(submission);
		}
		
		if(StringUtils.isEmpty(feedback)) {
			mailer.sendMail(config.senderEmail, "Autograder Error", 
					"Student name: " + submission.studentInfo.name + "\n" + 
					"Student Id:" + submission.studentInfo.id + ", " + submission.studentInfo.sis_user_id + "\n" + 
				    "Student UID: " + submission.studentInfo.sis_user_id +
				    "Submission: " + submission);
			
			feedback = "The autograder didn't produce any feedback for you. This is likely caused by an unexpected error while grading your assignment. " + 
					"Double check that your package, class, and files names are all as expected, and that all necessary files have been submitted. "
					+ "This includes .java files supplied for the assignment." 
					+ "If you have received this message multiple times, contact the administrator."; 
		}
		
		return feedback + "\n-AutoGrader";
	}
}

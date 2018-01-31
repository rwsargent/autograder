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
import autograder.student.GradeCalculator;
import autograder.student.LatePenalty;

/**
 * Uploads 
 * @author ryansargent
 */
public class GradeSubmissionUploader implements SubmissionUploader {

	private final Logger LOGGER = LoggerFactory.getLogger(GradeSubmissionUploader.class);
	private Configuration configuration;
	private PortalConnection portal;
	private GradeCalculator calculator;
	private LatePenalty latePenalty;

	@Inject
	public GradeSubmissionUploader(Configuration configuration, PortalConnection protal, GradeCalculator graderCalculator, LatePenalty latePenalty) {
		this.configuration = configuration;
		this.portal = protal;
		this.calculator = graderCalculator;
		this.latePenalty = latePenalty;
	}
	
	@Override
	public void upload(AutograderSubmission submission) {
		LOGGER.info("Posting grade to " + submission);
		Map<String, String> data = new HashMap<>();
		double gradePercentage = calculator.calculateGrade(submission);
		String canvasGrade = gradePercentage+ "%";
		
		data.put(Constants.CanvasApi.GRADE, canvasGrade);
		data.put(Constants.CanvasApi.SUMBISSION_COMMENT_TEXT, gradingComment(submission, canvasGrade));
		data.put(Constants.CanvasApi.GROUP_COMMENT, "true");
		
		portal.gradeStudentSubmission(Integer.toString(submission.studentInfo.id), configuration.canvasAssignmentId, data);
	}

	private String gradingComment(AutograderSubmission submission, String finalScore) {
		String comment = "Your final score is " + finalScore + " of the assignment's total.\n";
		if(submission.submissionInfo.late) {
			comment = latePenalty.addLateComment(comment, submission);
		}
		comment += submission.getResult().getTestResults();
		return comment;
	}
}
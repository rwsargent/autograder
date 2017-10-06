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

/**
 * Uploads 
 * @author ryansargent
 */
public class GradeSubmissionUploader implements SubmissionUploader {

	private final Logger LOGGER = LoggerFactory.getLogger(GradeSubmissionUploader.class);
	private Configuration configuration;
	private PortalConnection portal;
	private GradeCalculator calculator;

	@Inject
	public GradeSubmissionUploader(Configuration configuration, PortalConnection protal, GradeCalculator graderCalculator) {
		this.configuration = configuration;
		this.portal = protal;
		this.calculator = graderCalculator;
	}
	
	@Override
	public void upload(AutograderSubmission submission) {
		LOGGER.info("Posting grade to " + submission);
		Map<String, String> data = new HashMap<>();
		
		data.put(Constants.CanvasApi.SUMBISSION_COMMENT_TEXT, gradingComment(submission));
		data.put(Constants.CanvasApi.GROUP_COMMENT, "true");
		
		double gradePercentage = calculator.calculateGrade(submission);
		String canvasGrade = gradePercentage+ "%";
		data.put(Constants.CanvasApi.GRADE, canvasGrade);
		
		portal.gradeStudentSubmission(Integer.toString(submission.studentInfo.id), configuration.canvasAssignmentId, data);
	}

	private String gradingComment(AutograderSubmission submission) {
		int outOf = calculator.calculateOutOf(submission.getResult());
		String comment = "This assignment was graded out of " + outOf + " tests.\n";
		if(submission.submissionInfo.late) {
			comment += "A 10% late penalty has been applied.\n";
		}
		comment += submission.getResult().getTestResults();
		return comment;
	}
}
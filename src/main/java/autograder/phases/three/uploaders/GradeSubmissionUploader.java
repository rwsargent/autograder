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
import autograderutils.AutograderResult;

/**
 * Uploads 
 * @author ryansargent
 */
public class GradeSubmissionUploader implements SubmissionUploader {

	private final Logger LOGGER = LoggerFactory.getLogger(GradeSubmissionUploader.class);
	private Configuration configuration;
	private PortalConnection portal;

	@Inject
	public GradeSubmissionUploader(Configuration configuration, PortalConnection protal) {
		this.configuration = configuration;
		this.portal = protal;
	}
	
	@Override
	public void upload(AutograderSubmission submission) {
		LOGGER.info("Posting grade to " + submission);
		Map<String, String> data = new HashMap<>();
		
		data.put(Constants.CanvasApi.SUMBISSION_COMMENT_TEXT, gradingComment(submission));
		
		double gradePercentage = calculateGrade(submission);
		String canvasGrade = gradePercentage+ "%";
		data.put(Constants.CanvasApi.GRADE, canvasGrade);
		
		portal.gradeStudentSubmission(Integer.toString(submission.studentInfo.id), configuration.canvasAssignmentId, data);
	}

	private String gradingComment(AutograderSubmission submission) {
		int outOf = calculateOutOf(submission.getResult());
		String comment = "This assignment was graded out of " + outOf + " tests.\n";
		if(submission.submissionInfo.late) {
			comment += "A 10% late penalty has been applied.\n";
		}
		comment += submission.getResult().buildTestResults();
		return comment;
	}

	private double calculateGrade(AutograderSubmission submission) {
		double total = calculateOutOf(submission.getResult());
		double score = ((double)submission.getResult().getScore() / total) * 100;
		if(submission.submissionInfo.late) {
			score -= 10;
			if(score < 0) { // possible to get negative percentages here, bound it at 0.
				score = 0;
			}
		}
		return score;
	}
	
	private int calculateOutOf(AutograderResult result) {
		return configuration.adjustedTotal == 0 ? result.getTotal() : configuration.adjustedTotal;
	}
}
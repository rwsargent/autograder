package autograder.phases.three.uploaders.feedback;

import javax.inject.Inject;

import autograder.configuration.Configuration;
import autograder.student.AutograderSubmission;
import autograder.student.GradeCalculator;

/**
 * Limit the amount of feedback given. 
 * @author ryans
 */
public class RateLimitedFeedback implements CommentContentGetter {

	private GradeCalculator gradeCalculator;
	private Configuration config;

	@Inject
	public RateLimitedFeedback(Configuration configuration,GradeCalculator gradeCalculator) {
		this.gradeCalculator = gradeCalculator;	
		this.config = configuration;
	}
	
	@Override
	public String getComment(AutograderSubmission submission) {
		double grade = gradeCalculator.calculateGrade(submission);
		if(canGiveFeedback(submission, grade)) {
			int submissionsLeft = config.submissionFeedbackThreshold - submission.submissionInfo.attempt;
			String submissionsRemaining = "";
			if(submissionsLeft >= 0 ) {
				submissionsRemaining = "You have " + submissionsLeft + " graded submission" + (submissionsLeft == 1 ? "" : "s") + " left\n";
			}
			return submissionsRemaining + submission.getResult().getFeedback();
		} else {
			return "Your submission was successfully graded with a score greater than " + config.scoreFeedbackThreshold; 
		}
	}

	private boolean canGiveFeedback(AutograderSubmission submission, double grade) {
		return submission.submissionInfo.attempt <= config.submissionFeedbackThreshold || grade <= config.scoreFeedbackThreshold;
	}
}

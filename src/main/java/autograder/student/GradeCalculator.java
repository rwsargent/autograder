package autograder.student;

import javax.inject.Inject;

import autograder.configuration.Configuration;
import autograderutils.results.AutograderResult;

public class GradeCalculator {
	
	protected Configuration configuration;
	private LatePenalty latePenalty;

	@Inject
	public GradeCalculator(Configuration configuration, LatePenalty latePenalty) {
		this.configuration = configuration;
		this.latePenalty = latePenalty;
	}
	
	/**
	 * Calculates the student's percentage for the assignment. A 10% 
	 * late penalty is automatically subtracted if the submission is late. 
	 * @param submission
	 * @return a double between 0 and 100. 
	 */
	public double calculateGrade(AutograderSubmission submission) {
		if(submission.getResult() == null) {
			return 0;
		}
		double total = calculateOutOf(submission.getResult());
		double score = ((double) submission.getResult().getScore() / total) * 100;
		if(Double.isNaN(score)) {
			score = 0;
		}
		score = latePenalty.applyLatePenalty(score, submission);
				
//		if (submission.submissionInfo.late) {
//			score -= 10;
//			if (score < 0) { // possible to get negative percentages here, bound it at 0.
//				score = 0;
//			}
//		}
		return score;
	}
	
	/**
	 * Returns the total points for the assignment, either defined by the 
	 * grader suite, or configuration.
	 * @param result
	 * @return
	 */
	public int calculateOutOf(AutograderResult result) {
		if(result != null) {
			return configuration.adjustedTotal == 0 ? result.getTotal() : configuration.adjustedTotal;
		}
		return 0;
	}
}

package autograder.student;

public class NoLatePenalty implements LatePenalty {

	@Override
	public double applyLatePenalty(double score, AutograderSubmission submission) {
		return score;
	}

	@Override
	public String addLateComment(String comment, AutograderSubmission submission) {
		return comment;
	}

}

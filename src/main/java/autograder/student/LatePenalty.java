package autograder.student;

public interface LatePenalty {
	public double applyLatePenalty(double score, AutograderSubmission submission);
	public String addLateComment(String comment, AutograderSubmission submission);
}

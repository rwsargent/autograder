package autograder.canvas.responses;

public class QuizSubmission {
	public String id, quiz_id, user_id, submission_id;
	public double score, fudge_points;
	public String workflow_state;
}

package autograder.canvas.responses;

public class Submission extends BaseReponse {
	public int assignment_id, attempt, user_id, grader_id;
	public double score;
	public boolean excused, late, user;
	public String assignment, body, submission_type, submitted_at;
	
	public Attachment[] attachments;
}

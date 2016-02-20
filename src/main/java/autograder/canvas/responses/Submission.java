package autograder.canvas.responses;

/**
 * POJO to represent a Submission object from the Cavans API
 * @author Ryan
 *
 */
public class Submission {
	public int assignment_id, attempt, user_id, grader_id;
	public double score;
	public boolean excused, late, user;
	public String assignment, body, submission_type, submitted_at;
	
	public Attachment[] attachments;
}

package autograder.phases.three.uploaders.feedback;

import autograder.student.AutograderSubmission;

public class FullFeedbackComment implements CommentContentGetter {

	@Override
	public String getComment(AutograderSubmission submission) {
		return submission.getResult().getSummary();
	}
	
}

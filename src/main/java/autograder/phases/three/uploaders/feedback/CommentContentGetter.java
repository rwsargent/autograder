package autograder.phases.three.uploaders.feedback;

import autograder.student.AutograderSubmission;

public interface CommentContentGetter {
	String getComment(AutograderSubmission submission);
}

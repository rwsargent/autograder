package autograder.phases.three;

import autograder.student.AutograderSubmission;

public interface SubmissionUploader {
	public void upload(AutograderSubmission submission);
}

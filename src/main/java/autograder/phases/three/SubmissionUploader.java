package autograder.phases.three;

import autograder.student.AutograderSubmission;

@FunctionalInterface
public interface SubmissionUploader {
	public void upload(AutograderSubmission submission);
}

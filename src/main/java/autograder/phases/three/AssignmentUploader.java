package autograder.phases.three;

import autograder.student.AutograderSubmissionMap;

public interface AssignmentUploader {
	public void upload(AutograderSubmissionMap submissions);
}

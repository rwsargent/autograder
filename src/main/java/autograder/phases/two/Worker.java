package autograder.phases.two;

import autograder.student.AutograderSubmission;

public interface Worker {
	public void doWork(AutograderSubmission submission);
}

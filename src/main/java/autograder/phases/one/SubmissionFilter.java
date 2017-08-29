package autograder.phases.one;

import autograder.canvas.responses.Submission;

public interface SubmissionFilter {
	public boolean allow(Submission submission);
}

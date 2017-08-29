package autograder.phases.one;

import autograder.canvas.responses.Submission;

public class OnlyAttemptedSubmissions implements SubmissionFilter {

	@Override
	public boolean allow(Submission submission) {
		return submission.attempt > 0;
	}

}

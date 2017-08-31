package autograder.phases.one;

import java.util.List;

import autograder.canvas.responses.Submission;

public interface SubmissionDownloader {
	public List<Submission> downloadSubmissions();
}

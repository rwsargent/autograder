package autograder.phases.one;

import java.util.Map;

import autograder.canvas.responses.User;
import autograder.student.StudentMap;

public interface SubmissionDownloader {

	public StudentMap parseSubmission(Map<Integer, User> users);
}

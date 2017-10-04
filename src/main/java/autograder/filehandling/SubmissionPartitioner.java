package autograder.filehandling;

import java.util.List;
import java.util.Map;

import autograder.student.AutograderSubmission;
import autograder.student.AutograderSubmissionMap;

public interface SubmissionPartitioner {

	public Map<String, List<AutograderSubmission>> partition(AutograderSubmissionMap submissions);
}

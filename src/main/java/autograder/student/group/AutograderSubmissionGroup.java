package autograder.student.group;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import autograder.student.AutograderSubmission;

public class AutograderSubmissionGroup {

	private List<AutograderSubmission> submissions;
	private Optional<AutograderSubmission> submitter;
	
	public AutograderSubmissionGroup() {
		submissions = new ArrayList<>();
		submitter = Optional.empty();
	}
	
	public AutograderSubmissionGroup(List<AutograderSubmission> submissions, AutograderSubmission submitter) {
		this.submissions = submissions;
		if(!submissions.contains(submitter)) {
			throw new IllegalArgumentException("The submitter is not part of this groups submissions");
		}
		this.submitter = Optional.of(submitter);
	}
	
	public void addSubmission(AutograderSubmission submission) {
		if(submission == null) {
			throw new NullPointerException();
		}
		this.submissions.add(submission);
	}
	
	public void addSubmitter(AutograderSubmission submitter) {
		this.submissions.add(submitter);
		this.submitter = Optional.of(submitter);
	}
	
	public Optional<AutograderSubmission> getSubmitterOption() {
		return submitter;
	}
}

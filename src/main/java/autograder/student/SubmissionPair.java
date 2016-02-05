package autograder.student;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SubmissionPair {

	private Set<Student> students;
	public Student submitter;
	public Student partner;
	public boolean sorted;
	
	public SubmissionPair(Student... students) {
		this.students = new HashSet<>();
		this.students.addAll(Arrays.asList(students));
		if(students[0].assignProps.submitted && students[1].assignProps.submitted) {
			// they're both idiots.
			this.submitter = students[0];
			this.partner = students[1];
		} else if (students[0].assignProps.submitted) {
			sorted = true;
			this.submitter = students[0];
			this.partner = students[1];
		} else if (students[0].assignProps.submitted) {
			sorted = true;
			this.submitter = students[1];
			this.partner = students[0];
		} else {
			// they're still idiots
			this.submitter = students[0];
			this.partner = students[1];
		}
	}
	
	public Set<Student> getStudents() {
		return students;
	}
	
	@Override
	public boolean equals(Object obj) {
		SubmissionPair pair;
		if(!(obj instanceof SubmissionPair)) {
			return false;
		}
		pair = (SubmissionPair)obj;
		return pair.students.equals(this.students);
	}
	
	@Override
	public int hashCode() {
		int hash = (submitter.studentInfo.id + partner.studentInfo.id);
		return (hash << 5) - hash ; // hash * 31
	}
}

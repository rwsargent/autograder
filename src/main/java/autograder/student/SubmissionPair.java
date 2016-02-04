package autograder.student;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SubmissionPair {

	private Set<Student> students;
	public Student submitter;
	public Student partner;
	public boolean sorted;
	
//	public SubmissionPair(Student submitter, Student partner) {
//		this.submitter = submitter;
//		this.partner = partner;
//	}
	
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
	
	@Override
	public boolean equals(Object obj) {
		SubmissionPair pair;
		if(!(obj instanceof SubmissionPair)) {
			return false;
		}
		pair = (SubmissionPair)obj;
		return pair.students.equals(this.students);
//		if(pair.sorted && this.sorted) {
//			return pair.partner.equals(this.partner) && 
//					pair.submitter.equals(this.submitter);
//		} else {
//			return ((submitter.equals(pair.submitter) || submitter.equals(pair.partner)) &&
//				(partner.equals(pair.submitter) || partner.equals(pair.partner)));
//		}
	}
	
	@Override
	public int hashCode() {
		int hash = (submitter.studentInfo.id + partner.studentInfo.id);
		return (hash << 5) - hash ; // hash * 31
	}
}

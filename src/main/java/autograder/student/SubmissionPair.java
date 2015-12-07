package autograder.student;

public class SubmissionPair {

	public Student submitter;
	public Student partner;
	
	public SubmissionPair(Student submitter, Student partner) {
		this.submitter = submitter;
		this.partner = partner;
	}
	
	@Override
	public boolean equals(Object obj) {
		SubmissionPair pair;
		if(!(obj instanceof SubmissionPair)) {
			return false;
		}
		pair = (SubmissionPair)obj;
		return pair.partner.canvasId == this.partner.canvasId && 
				pair.submitter.canvasId == this.submitter.canvasId;
	}
	
	@Override
	public int hashCode() {
		int hash = (submitter.canvasId + partner.canvasId);
		return (hash << 5) - hash ; // hash * 31
	}
}

package autograder.student;

import java.util.Set;
import java.util.TreeSet;

public class SubmissionSet {

	private static SubmissionSet mInstance = new SubmissionSet();
	private Set<SubmissionPair> partnerships;
	
	public static SubmissionSet getInstance() {
		if (mInstance == null) {
			mInstance = new SubmissionSet();
		}
		return mInstance;
	}
	
	private SubmissionSet() {
		partnerships = new TreeSet<>();
	}
	
	public void addPair(SubmissionPair pair) {
		partnerships.add(pair);
	}
	
}

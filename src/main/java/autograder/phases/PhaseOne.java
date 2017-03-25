package autograder.phases;

import java.io.File;
import java.util.HashMap;

import com.google.inject.Inject;

import autograder.canvas.responses.User;
import autograder.phases.one.StudentDownloader;
import autograder.phases.one.SubmissionDownloader;
import autograder.student.StudentMap;

/**
 * The Setup
 * @author ryans 2017
 *
 */
public class PhaseOne {
	protected StudentDownloader mStudentDownloader;
	protected SubmissionDownloader mSubmissionDownloader;
	protected SubmissionBuilder mSubmissionBuilder;
	
	@Inject
	public PhaseOne(StudentDownloader studentDownloader, SubmissionDownloader submissionDownloader, SubmissionBuilder subBuilder) {
		mStudentDownloader = studentDownloader;
		mSubmissionDownloader = submissionDownloader;
		mSubmissionBuilder = subBuilder;
	}
	
	public StudentMap phaseOne() {
		if(!new File("submission").exists()) {
			User[] users = mStudentDownloader.getStudents();
			
			HashMap<Integer, User> map = new HashMap<>();
			for(User user : users) {
				map.put(user.id, user);
			}
			return mSubmissionDownloader.parseSubmission(map);
		} else {
			return mSubmissionBuilder.build();
		}
		
	}
}

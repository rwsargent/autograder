package autograder.phases;

import java.io.File;
import java.util.HashMap;

import com.google.inject.Inject;

import autograder.AutograderSaver;
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
	private AutograderSaver mSaver;
	
	@Inject
	public PhaseOne(StudentDownloader studentDownloader, SubmissionDownloader submissionDownloader, SubmissionBuilder subBuilder, AutograderSaver saver) {
		mStudentDownloader = studentDownloader;
		mSubmissionDownloader = submissionDownloader;
		mSubmissionBuilder = subBuilder;
		mSaver = saver;
	}
	
	public StudentMap setupSubmissions() {
		User[] users = mStudentDownloader.getStudents();
		HashMap<Integer, User> map = new HashMap<>();
		for(User user : users) {
			map.put(user.id, user);
		}
		
		File saved = new File(AutograderSaver.AUTOGRADER_SAVE_FILENAME);
		if(saved.exists()) {
			mSaver.readFile(saved);
			if(mSaver.isCurrentAssignemnt()) {
				return mSubmissionBuilder.build(map, new File("submissions"));
			} else {
				if(!saved.delete()) {
					System.out.println("Saved file didn't delete");
				}
			}
		}
		
		StudentMap parseSubmission = mSubmissionDownloader.parseSubmission(map);
		mSaver.writeSavedFile(saved);
		return parseSubmission;
	}
}

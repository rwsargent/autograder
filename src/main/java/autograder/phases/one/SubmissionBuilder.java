package autograder.phases;

import java.io.File;
import java.util.HashMap;

import autograder.canvas.responses.User;
import autograder.student.AutograderSubmission;
import autograder.student.AutograderSubmissionMap;

/**
 * The if all submission files are downloaded, we shouldn't have to re-download the submissions. If we have the the user map, we can
 * build a student map. 
 * @author ryansargent 2017
 */
public class SubmissionBuilder {
	public AutograderSubmissionMap build(HashMap<Integer,User> users, File submissions) {
		AutograderSubmissionMap studentMap = new AutograderSubmissionMap();
		if(!submissions.exists()) {
			throw new IllegalStateException("Can't find submission file");
		}
		
		for(File studentDir : submissions.listFiles(f -> f.isDirectory())) {
			String dirName = studentDir.getName();
			Integer canvasId = Integer.parseInt(dirName.substring(dirName.lastIndexOf('_')+1));
			User studentUser = users.get(canvasId);
			AutograderSubmission student = new AutograderSubmission(studentDir, studentUser);
			student.createAssignmentProperties();
			studentMap.addSubmission(student);
		}
		
		return studentMap;
	}
}

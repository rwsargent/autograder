package autograder.phases;

import java.io.File;
import java.util.HashMap;

import autograder.canvas.responses.User;
import autograder.student.Student;
import autograder.student.StudentMap;

/**
 * The if all submission files are downloaded, we shouldn't have to re-download the submissions. If we have the the user map, we can
 * build a student map. 
 * @author ryansargent 2017
 */
public class SubmissionBuilder {
	public StudentMap build(HashMap<Integer,User> users, File submissions) {
		StudentMap studentMap = new StudentMap();
		
		for(File studentDir : submissions.listFiles(File::isDirectory)) {
			String dirName = studentDir.getName();
			Integer canvasId = Integer.parseInt(dirName.substring(dirName.lastIndexOf('_')));
			User studentUser = users.get(canvasId);
			Student student = new Student(studentDir, studentUser);
			student.createAssignmentProperties();
			studentMap.addStudent(student);
		}
		
		return studentMap;
	}
}

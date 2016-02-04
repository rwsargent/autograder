package autograder.student;

import java.io.File;
import java.util.logging.Logger;

import autograder.canvas.responses.User;
import autograder.configuration.AssignmentProperties;
import autograder.configuration.ConfigurationException;

public class Student {
	private static Logger LOGGER = Logger.getLogger(Student.class.getName());
	public File studentDirectory;
	public File sourceDirectory;
	public User studentInfo;
	
	public AssignmentProperties assignProps;

	public Student() {
	}
	
	public Student(User student) {
		studentInfo = student;
		studentDirectory = new File(String.format("submissions/%s_%d", student.sortableName, student.id));
		studentDirectory.mkdirs();
		sourceDirectory = new File(studentDirectory.getAbsolutePath() + "/source");
		sourceDirectory.mkdir();
	}
	
	public String getSourceDirectoryPath() {
		return studentDirectory.getAbsolutePath() + "/source";
	}
	
	public String toString() {
		return studentInfo.sortableName;
	}

	public void createAssignmentProperties() {
		try {
			assignProps = new AssignmentProperties(studentDirectory.getAbsolutePath() + "/assignment.properties");
		} catch (ConfigurationException e) {
			LOGGER.warning(studentInfo.name + " does not have a valid assignment.properties file. " + e.getMessage());
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Student)) {
			return false;
		}
		Student rhs = (Student)obj;
		return studentInfo.id == studentInfo.id;
	}
	
	@Override
	public int hashCode() {
		return studentInfo.id;
	}
}
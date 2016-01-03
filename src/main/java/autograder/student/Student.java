package autograder.student;

import java.io.File;
import java.util.logging.Logger;

import autograder.configuration.AssignmentProperties;
import autograder.configuration.ConfigurationException;

public class Student {
	private static Logger LOGGER = Logger.getLogger(Student.class.getName());
	public File studentDirectory;
	public String name;
	public int canvasId;
	public boolean isLate;
	
	public AssignmentProperties assignProps;

	public Student() {
	}
	
	public Student(String name, int canvasId) {
		this.canvasId = canvasId;
		studentDirectory = new File(String.format("submissions/%s_%d", name, canvasId));
		studentDirectory.mkdirs();
		new File(studentDirectory.getAbsolutePath() + "/source").mkdir();
	}
	
	public Student(String name, int canvasId, boolean late) {
		this(name, canvasId);
		this.isLate = late;
	}
	
	public String getSourceDirectoryPath() {
		return studentDirectory.getAbsolutePath() + "/source";
	}
	
	public String toString() {
		return name;
	}

	public void createAssignmentProperties() {
		try {
			assignProps = new AssignmentProperties(studentDirectory.getAbsolutePath() + "/assignment.properties");
		} catch (ConfigurationException e) {
			LOGGER.warning(this.name + " does not have a valid assignment.properties file");
		}
	}
}
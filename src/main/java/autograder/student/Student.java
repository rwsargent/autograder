package autograder.student;

import java.io.File;

import autograder.configuration.AssignmentProperties;
import autograder.configuration.ConfigurationException;

public class Student {
	public File studentDirectory;
	public String name;
	public int canvasId;
	public boolean isLate;
	
	AssignmentProperties assignProps;

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

	public AssignmentProperties createAssignmentProperties() {
		try {
			assignProps = new AssignmentProperties(studentDirectory.getAbsolutePath() + "/assignment.properties");
		} catch (ConfigurationException e) {
			StudentErrorRegistry.getInstance().addInvalidAssignmentProperties(this);
		}
		return assignProps;
	}
}
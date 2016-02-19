package autograder.student;

import java.io.File;
import java.util.logging.Logger;

import autograder.canvas.responses.User;
import autograder.configuration.AssignmentProperties;
import autograder.configuration.ConfigurationException;

public class Student {
	private static Logger LOGGER = Logger.getLogger(Student.class.getName());
	public File studentDirectory;
	private File sourceDirectory;
	public User studentInfo;
	
	public AssignmentProperties assignProps;

	public Student() {
	}
	
	public Student(User student) {
		studentInfo = student;
		student.sortableName = student.sortableName.replaceAll(", ", "_");
		studentDirectory = new File(String.format("submissions/%s_%d", student.sortableName, student.id));
		studentDirectory.mkdirs();
		
	}
	
	public String getSourceDirectoryPath() {
		return studentDirectory.getAbsolutePath() + "/source";
	}
	
	/**
	 * This file will create the directory for the Java source code if it hasn't been made already.
	 * Use this method if you intend to write to this directory. If you want to read from it, use {@link Student#getSourceDirectory()}
	 * @return The File of the source directory that was created. 
	 */
	public File createSourceDirectory() {
		if(sourceDirectory == null) {
			sourceDirectory = new File(studentDirectory.getAbsolutePath() + "/source");
			sourceDirectory.mkdir();
		}
		return sourceDirectory;
	}
	
	/**
	 * Use this method if you want to read from the source directory. If you are looking to write to it, use {@link Student#createSourceDirectory()}
	 * @return The directory that holds the Java source.
	 */
	public File getSourceDirectory() {
		return sourceDirectory;
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
		return studentInfo.id == rhs.studentInfo.id;
	}
	
	@Override
	public int hashCode() {
		return studentInfo.id;
	}
}
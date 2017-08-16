package autograder.student;

import java.io.File;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import autograder.canvas.responses.Submission;
import autograder.canvas.responses.User;
import autograder.configuration.AssignmentProperties;
import autograder.configuration.ConfigurationException;

public class AutograderSubmission {
	private static Logger LOGGER = Logger.getLogger(AutograderSubmission.class.getName());
	public File directory;
	private File sourceDirectory;
	public User studentInfo;
	public Submission submissionInfo;
	public Properties properties = new Properties();
	public AssignmentProperties assignProps;

	public AutograderSubmission(Submission submission, File parentDir) {
		submissionInfo = submission;
		directory = new File(parentDir, generateDirectoryName());
		if(directory.mkdir()) {
			LOGGER.log(Level.INFO, "Created AutograderSubmission: " + directory.getName());
		} else {
			LOGGER.log(Level.WARNING, "Failed to create AutograderSubmission: " + directory.getName());
		}
	}
	
	private String generateDirectoryName() {
		return submissionInfo.user_id + "_" + submissionInfo.submitted_at;
	}

	public AutograderSubmission(User student) {
		studentInfo = student;
		student.sortableName = student.sortableName.replaceAll(", ", "_");
		directory = new File(String.format("submissions/%s_%d", student.sortableName, student.id));
		directory.mkdirs();
	}
	
	public AutograderSubmission(File basedir, User user) {
		directory = basedir;
		studentInfo = user;
	}
	
	public String getSourceDirectoryPath() {
		return directory.getAbsolutePath() + "/source";
	}
	
	public File getClassesDirectory() {
		return Paths.get(directory.getAbsolutePath(), "source", "classes").toFile();
	}
	/**
	 * This file will create the directory for the Java source code if it hasn't been made already.
	 * Use this method if you intend to write to this directory. If you want to read from it, use {@link AutograderSubmission#getSourceDirectory()}
	 * @return The File of the source directory that was created. 
	 */
	public File createSourceDirectory() {
		if(sourceDirectory == null) {
			sourceDirectory = new File(directory.getAbsolutePath() + "/source");
			sourceDirectory.mkdir();
		}
		return sourceDirectory;
	}
	
	/**
	 * Use this method if you want to read from the source directory. If you are looking to write to it, use {@link AutograderSubmission#createSourceDirectory()}
	 * @return The directory that holds the Java source.
	 */
	public File getSourceDirectory() {
		return sourceDirectory;
	}
	
	public String toString() {
		return directory.getName();
	}

	public void createAssignmentProperties() {
		try {
			assignProps = new AssignmentProperties(directory.getAbsolutePath() + "/assignment.properties");
		} catch (ConfigurationException e) {
			LOGGER.warning(studentInfo.name + " does not have a valid assignment.properties file. " + e.getMessage());
		}
	}
	
	public void setProperty(String key, String value) {
		properties.setProperty(key, value);
	}
	
	public String getProperty(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof AutograderSubmission)) {
			return false;
		}
		AutograderSubmission rhs = (AutograderSubmission)obj;
		return (this.submissionInfo.user_id  == rhs.submissionInfo.user_id) && 
			   (this.submissionInfo.assignment_id == rhs.submissionInfo.assignment_id);
	}
	
	@Override
	public int hashCode() {
		return this.submissionInfo.assignment_id * this.submissionInfo.user_id * 7;
	}

	public void addUser(User user) {
		this.studentInfo = user;
	}
}
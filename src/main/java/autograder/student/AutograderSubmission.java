package autograder.student;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;

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
	private File classesDirectory;

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
		if(classesDirectory == null) {
			classesDirectory = Paths.get(directory.getAbsolutePath(), "source", "classes").toFile();
			classesDirectory.mkdirs();
		}
		return classesDirectory;
	}
	/**
	 * This file will create the directory for the Java source code if it hasn't been made already.
	 * Use this method if you intend to write to this directory. If you want to read from it, use {@link AutograderSubmission#getSourceDirectory()}
	 * @return The File of the source directory that was created. 
	 */
	public File getSourceDirectory() {
		if(sourceDirectory == null) {
			sourceDirectory = new File(directory.getAbsolutePath() + "/source");
			sourceDirectory.mkdir();
		}
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

	public AutograderSubmission writeMetaData() {
		Gson gson = new Gson();
		File metaDir = new File(directory, "meta");
		metaDir.mkdir();
		try {
			FileUtils.writeStringToFile(new File(metaDir, "studentInfo.json"), gson.toJson(this.studentInfo));
			FileUtils.writeStringToFile(new File(metaDir, "submission.json"), gson.toJson(this.submissionInfo));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	public File getDirectory() {
		return directory;
	}
}
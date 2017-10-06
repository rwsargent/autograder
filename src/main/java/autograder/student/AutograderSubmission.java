package autograder.student;

import static autograder.Constants.MetaData.STUDENT_INFO;
import static autograder.Constants.MetaData.SUBMISSION;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import autograder.canvas.responses.Submission;
import autograder.canvas.responses.User;
import autograder.configuration.AssignmentProperties;
import autograder.configuration.ConfigurationException;
import autograderutils.results.AutograderResult;
public class AutograderSubmission {

	private static final Logger LOGGER = LoggerFactory.getLogger(AutograderSubmission.class);
	
	public File directory;
	private File sourceDirectory;
	public User studentInfo;
	public Submission submissionInfo;
	public Properties properties = new Properties();
	public AssignmentProperties assignProps;
	private File classesDirectory;
	private AutograderResult result;

	public AutograderSubmission(Submission submission, File parentDir) {
		Assert.assertNotNull(submission);
		submissionInfo = submission;
		directory = new File(parentDir, generateDirectoryName());
		
		if(!directory.exists()) {
			if(directory.mkdirs()) {
				LOGGER.info("Created AutograderSubmission: " + directory.getName());
			} else {
				LOGGER.warn("Failed to create AutograderSubmission: " + directory.getName() + " mkdir failed.");
			}
		}
	}
	
	private String generateDirectoryName() {
		return submissionInfo.user_id + "_" + submissionInfo.attempt;
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
			LOGGER.warn(studentInfo.name + " does not have a valid assignment.properties file. " + e.getMessage());
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

	public void setUser(User user) {
		this.studentInfo = user;
	}

	public AutograderSubmission writeMetaData() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		File metaDir = new File(directory, "meta");
		metaDir.mkdir();
		try {
			FileUtils.writeStringToFile(new File(metaDir, STUDENT_INFO), gson.toJson(this.studentInfo), Charset.defaultCharset());
			FileUtils.writeStringToFile(new File(metaDir, SUBMISSION), gson.toJson(this.submissionInfo), Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	public File getDirectory() {
		return directory;
	}

	public void setResult(AutograderResult result) {
		this.result = result;
	}
	
	public AutograderResult getResult() {
		return this.result;
	}
}
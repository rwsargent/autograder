package autograder.filehandling;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.google.inject.Inject;

import autograder.canvas.CanvasConnection;
import autograder.canvas.responses.Attachment;
import autograder.canvas.responses.Submission;
import autograder.canvas.responses.User;
import autograder.configuration.AssignmentProperties;
import autograder.configuration.Configuration;
import autograder.configuration.ConfigurationException;
import autograder.student.Student;
import autograder.student.StudentErrorRegistry;
import autograder.student.StudentMap;

/**
 * SubmissionParser will create a student for every submission it receives,
 * and write whatever attachemnts it has to disk. 
 * @author ryansargent
 *
 */
public class SubmissionParser {
	
	private Pattern mExpludePattern;
	protected Configuration mConfig;
	protected CanvasConnection connection;
	protected StudentErrorRegistry errorRegistry;
	private SeenSubmissions seenSubmissions;
	
	@Inject
	public SubmissionParser(Configuration configuration, CanvasConnection connection, StudentErrorRegistry errorRegistry, SeenSubmissions seenSubmissions) {
		mConfig = configuration;
		this.connection = connection;
		this.errorRegistry = errorRegistry;
		this.seenSubmissions = seenSubmissions;
	}
	
	public StudentMap parseSubmissions(Map<Integer, User> users, Submission[] submissions) {
		StudentMap studentMap = new StudentMap();
		for (Submission sub : submissions) {
			if(seenSubmissions.hasSeenSubmission(Integer.toString(sub.assignment_id))) {
				continue;
			}
			Student student = new Student(users.get(sub.user_id));
			studentMap.addStudent(student);
			if (sub.attachments == null) {
				continue;
			}
			mExpludePattern = createPattern();
			for (Attachment attachment : sub.attachments) {
				if(mExpludePattern.matcher(attachment.filename).find()) {
					continue;
				}
				if (attachment.filename.endsWith(".zip")) {
					handleZipFile(attachment.url, student.studentDirectory.getAbsolutePath(), student);
				} else if (attachment.filename.contains(".properties")) {
					handlePropertieFile(attachment.url, student);
				} else if (attachment.filename.contains(".pdf")) {
					handlePdf(attachment.url, student, attachment.filename);
				} else if (attachment.filename.contains(".java")) {
					handleSourceFile(attachment.url, student, attachment.filename);
				}
			}
		}
		return studentMap;
	}

	private Pattern createPattern() {
		if(mConfig.ignorePattern != null) {
			return Pattern.compile(mConfig.ignorePattern);
		}
		return Pattern.compile("$^"); // matches empty strings.
	}
	

	private void handleZipFile(String url, String submissionDir, Student student) {
		try (ZipInputStream zipStream = new ZipInputStream(
				new ByteArrayInputStream(connection.downloadFile(url)))) {
			ZipEntry entry = null;
			byte[] byteBuff = new byte[4096];
			while ((entry = zipStream.getNextEntry()) != null) {
				String entryName = FilenameUtils.getName(entry.getName());
				if(mExpludePattern.matcher(entryName).find()) {
					System.out.println("Excluding " + entryName);
					zipStream.closeEntry();
					continue;
				}
				if (entryName.contains(".java")) {
					File javaFile = new File(student.createSourceDirectory().getAbsolutePath() + "/" + entryName);
					try (FileOutputStream out = new FileOutputStream(javaFile)) {
						int bytesRead = 0;
						while ((bytesRead = zipStream.read(byteBuff)) > 0) {
							out.write(byteBuff, 0, bytesRead);
						}
					}
				} else if (entryName.contains(".properties")) {
					try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
						int bytesRead = 0;
						while ((bytesRead = zipStream.read(byteBuff)) > 0) {
							out.write(byteBuff, 0, bytesRead);
						}
						try (ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray())) {
							createSubmissionRecord(in, student);
						}
					}
				} else if (!invalidFile(entryName)) {
					try (FileOutputStream out = new FileOutputStream(
							new File(student.studentDirectory.getAbsolutePath() + "/" + entryName))) {
						int bytesRead = 0;
						while ((bytesRead = zipStream.read(byteBuff)) > 0) {
							out.write(byteBuff, 0, bytesRead);
						}
					}
				}
				zipStream.closeEntry();
			}
		} catch (IOException | IllegalArgumentException e) {
			StudentErrorRegistry.getInstance().addInvalidSubmission(student);
		} 			
	}

	private boolean invalidFile(String entryName) {
		if (entryName.contains(".class") || entryName.startsWith(".") || entryName.isEmpty() || entryName.startsWith("org.")) {
			return true;
		}
		return false;
	}

	private void handlePropertieFile(String url, Student student) {
		try (ByteArrayInputStream bis = new ByteArrayInputStream(connection.downloadFile(url))) {
			createSubmissionRecord(bis, student);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void handlePdf(String url, Student student, String filename) {
		try {
			FileUtils.writeByteArrayToFile(new File(student.studentDirectory.getAbsolutePath() + "/" + filename),
					connection.downloadFile(url));
		} catch (IOException e) {
			System.out.println("Could not write pdf to file from " + student + ". Reason: " + e.getMessage());
		}
	}
	
	private void handleSourceFile(String url, Student student, String filename) {
		try {
			FileUtils.writeByteArrayToFile(new File(student.createSourceDirectory().getAbsolutePath() + "/" + filename), connection.downloadFile(url));
		} catch (IOException e) {
			System.out.println("Could not write " + filename + " to file from " + student + ". Reason: " + e.getMessage());
		}
	}

	private void createSubmissionRecord(ByteArrayInputStream bis, Student student) {
		try {
			Properties props = new Properties();
			props.load(bis);
			try(OutputStream out = new FileOutputStream(student.studentDirectory.getAbsolutePath() + "/assignment.properites")) {
				props.store(out, "Saving assignment.properites file");
			}
			student.assignProps = new AssignmentProperties(props);
		} catch (IOException | ConfigurationException | IllegalArgumentException e) {
			errorRegistry.addInvalidAssignmentProperties(student);
			System.out.println(student + " messed up on assignment property file. Reason: " + e.getMessage());
		}
	}
}

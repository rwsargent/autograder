package autograder.filehandling;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FilenameUtils;

import autograder.canvas.CanvasConnection;
import autograder.canvas.responses.Attachment;
import autograder.canvas.responses.Submission;
import autograder.canvas.responses.User;
import autograder.configuration.AssignmentProperties;
import autograder.configuration.ConfigurationException;
import autograder.student.Student;
import autograder.student.StudentErrorRegistry;
import autograder.student.StudentMap;

public class SubmissionDownloader {
	
	public StudentMap downloadSubmissions(Map<Integer, User> students, boolean onlyLate) {
		Submission[] submissions = CanvasConnection.getAllSubmissions();
		StudentMap studentMap = new StudentMap();
		for(Submission sub : submissions) {
			if(onlyLate && !sub.late) { // skip all non-late submissions
				continue;
			}
			Student student = new Student(students.get(sub.user_id));
			studentMap.addStudent(student);
			if(sub.attachments == null) {
				continue;
			}
			for(Attachment attachment : sub.attachments) {
				if(attachment.filename.endsWith(".zip")) {
					handleZipFile(attachment.url, student.studentDirectory.getAbsolutePath(), student);
				} else if (attachment.filename.contains(".properties")) {
					handlePropertieFile(attachment.url, student);
				}
			}
		}
		return studentMap;
	}

	private void handleZipFile(String url, String submissionDir, Student student) { 
		try(ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(CanvasConnection.downloadFile(url)))) {
			ZipEntry entry = null;
			byte[] byteBuff = new byte[4096];
			while ((entry = zipStream.getNextEntry()) != null) {
				String entryName = FilenameUtils.getName(entry.getName());
				if(entryName.contains(".java")) {
					File javaFile = new File(student.sourceDirectory.getAbsolutePath() + "/" + entryName);
					try(FileOutputStream out = new FileOutputStream(javaFile)) {
						int bytesRead = 0;
						while ((bytesRead = zipStream.read(byteBuff)) > 0) {
							out.write(byteBuff, 0, bytesRead);
						}
					}
				} else if (entryName.contains(".properties")) {
					try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
						int bytesRead = 0;
						while ((bytesRead = zipStream.read(byteBuff)) > 0) {
							out.write(byteBuff, 0, bytesRead);
						}
						try(ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray())) {
							createSubmissionRecord(in, student);
						}
					}
				} else if(!invalidFile(entryName)) {
					try(FileOutputStream out = new FileOutputStream(new File(student.studentDirectory.getAbsolutePath() + "/" + entryName))) {
						int bytesRead = 0;
						while ((bytesRead = zipStream.read(byteBuff)) > 0) {
							out.write(byteBuff, 0, bytesRead);
						}
					}
				}
				zipStream.closeEntry();
			}	
		} catch (IOException e) {
			StudentErrorRegistry.getInstance().addInvalidSubmission(student);
		}
	}
	
	private boolean invalidFile(String entryName) {
		if(entryName.contains(".class") || entryName.startsWith(".")) {
			return true;
		}
		return false;
	}

	private void handlePropertieFile(String url, Student student) {
		try(ByteArrayInputStream bis = new ByteArrayInputStream(CanvasConnection.downloadFile(url))) {
			createSubmissionRecord(bis, student);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void createSubmissionRecord(ByteArrayInputStream bis, Student student) {
		try {
			Properties props = new Properties();
			props.load(bis);
			student.assignProps = new AssignmentProperties(props);
		} catch (IOException | ConfigurationException e) {
			StudentErrorRegistry.getInstance().addInvalidAssignmentProperties(student);
		}
	}
}

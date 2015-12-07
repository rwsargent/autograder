package autograder.filehandling;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;

import autograder.Constants;
import autograder.configuration.ConfigurationException;
import autograder.student.Student;
import autograder.student.StudentSubmissionRegistry;


public class SubmissionReader {
	private static Logger LOGGER = Logger.getLogger(SubmissionReader.class.getName());
	private byte[] buffer = new byte[1024]; // no need to allocate this more than once an object;
	private Pattern submissionPattern = Pattern.compile(Constants.FILE_REGEX);
	private StudentSubmissionRegistry submissionRegistry = StudentSubmissionRegistry.getInstance();
	
	public void unzipSubmissions(String filePath) {
		ZipFile submissionZip;
		System.setProperty("sun.zip.disableMemoryMapping", "true");
		try {
			submissionZip = new ZipFile(filePath);
			unzip("submissions", submissionZip, null);
		} catch (IOException e) {
			throw new ConfigurationException("Could not find the submission file. " + e.getMessage());
		}
	}
	
	private void unzip(String destDirectory, ZipFile zip, Student student) throws ZipException, IOException {
		Enumeration<? extends ZipEntry> entries = zip.entries();
		while(entries.hasMoreElements()) {
			ZipEntry zipEntry = entries.nextElement();
			String entryName = zipEntry.getName();
			if(student == null) {
				Matcher match = submissionPattern.matcher(entryName);
				if(match.matches()) {
					String studentName = match.group(Constants.NAME_GROUP);
					Student studentSubmission = submissionRegistry.getStudentByName(studentName);
					String fileName = match.group(Constants.SUBMISSION_NAME_GROUP);
					if(studentSubmission == null) {
						studentSubmission = submissionRegistry.createStudentSubmission(studentName, match.group(Constants.CANVAS_ID_GROUP));
					}
					if(isValidFile(fileName)) {
						writeSubmissionToFile(new File(studentSubmission.studentDirectory.getPath() + "/" + fileName), zipEntry, zip);
					} 
					else if (fileName.endsWith(".zip")) {
						File tempFile = new File("temp_" + studentName);
						writeSubmissionToFile(tempFile, zipEntry, zip);
						unzip(destDirectory + "/" + studentName, new ZipFile(tempFile), studentSubmission);
						tempFile.delete();
					}
				} else {
					LOGGER.info(entryName + " did not match.");
				}
			} else {
				if (zipEntry.getName().endsWith(".java")) {
					String fileName = FilenameUtils.getName(zipEntry.getName());
					writeSubmissionToFile(new File(student.getSourceDirectoryPath() +"/" +  fileName), zipEntry, zip);
				}
			}
		}
	}

	private void writeSubmissionToFile(File file, ZipEntry zipEntry, ZipFile zipFile) {
		try(FileOutputStream fos = new FileOutputStream(file); 
				InputStream zin = zipFile.getInputStream(zipEntry)) {
			int length;
			while((length = zin.read(buffer)) > 0) {
				fos.write(buffer, 0, length);
			}
		} catch ( IOException e) {
			LOGGER.severe("Could not write to " + file.getName()+ ": " + e.getMessage());
		}
	}

	private boolean isValidFile(String fileName) {
		for(String validExtenstion : Constants.VALID_FILE_TYPES) {
			if(fileName.endsWith(validExtenstion)) {
				return true;
			}
		}
		return false;
	}
}

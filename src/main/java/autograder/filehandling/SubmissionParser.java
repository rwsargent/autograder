package autograder.filehandling;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;

import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.google.inject.Inject;

import autograder.canvas.CanvasConnection;
import autograder.canvas.responses.Attachment;
import autograder.canvas.responses.Submission;
import autograder.canvas.responses.User;
import autograder.configuration.AssignmentProperties;
import autograder.configuration.Configuration;
import autograder.configuration.ConfigurationException;
import autograder.phases.one.FileDirector;
import autograder.phases.one.Unzipper;
import autograder.student.AutograderSubmission;
import autograder.student.AutograderSubmissionMap;
import autograder.student.StudentErrorRegistry;

/**
 * SubmissionParser will create a student for every submission it receives,
 * and write whatever attachemnts it has to disk. 
 * @author ryansargent
 *
 */
public class SubmissionParser {
	
	private Pattern excludePattern;
	protected Configuration config;
	protected CanvasConnection connection;
	protected StudentErrorRegistry errorRegistry;
	private SeenSubmissions seenSubmissions;
	protected Unzipper unzipper;
	private String assignment;
	
	private Logger LOGGER = Logger.getLogger(SubmissionParser.class.getName());
	private Map<String, FileDirector> fileDirectors;
	
	@Inject
	public SubmissionParser(Configuration configuration, CanvasConnection connection, StudentErrorRegistry errorRegistry, SeenSubmissions seenSubmissions, @Named String assignment, Unzipper unzipper, Map<String, FileDirector> fileDirectors) {
		this.config = configuration;
		this.connection = connection;
		this.errorRegistry = errorRegistry;
		this.seenSubmissions = seenSubmissions;
		this.assignment = assignment;
		this.unzipper = unzipper;
		this.excludePattern = createExcludePattern();
		this.fileDirectors = fileDirectors;
	}
	
	public AutograderSubmission parseAndCreateSubmission(Submission submission) {
		AutograderSubmission autoSubmission = new AutograderSubmission(submission, new File(assignment));
		for(Attachment attachment : submission.attachments) {
			writeAttatchmentToDisk(autoSubmission, attachment);
		}
		return autoSubmission;
	}
	
	@Deprecated
	public AutograderSubmissionMap parseSubmissions(Map<Integer, User> users, Submission[] submissions) {
		AutograderSubmissionMap studentMap = new AutograderSubmissionMap();
		for (Submission sub : submissions) {
			if(seenSubmissions.alreadySeenSubmission(sub)) {
				continue;
			}
			AutograderSubmission student = new AutograderSubmission(users.get(sub.user_id));
			studentMap.addSubmission(student);
			if (sub.attachments == null) {
				continue;
			}
		}
		return studentMap;
	}

	private void writeAttatchmentToDisk(AutograderSubmission submission, Attachment attachment) {
		if (excludePattern.matcher(attachment.filename).find()) {
			return;
		}
		byte[] filedata = connection.downloadFile(attachment.url);
		try (ByteArrayInputStream bais = new ByteArrayInputStream(filedata)) {
			// special case for zip files...oh well.
			if (canBeUnzipped(attachment)) {
				ZipInputStream zipStream = new ZipInputStream(bais);
				unzipper.unzipForSubmission(zipStream, submission);
				IOUtils.closeQuietly(zipStream);
			} else {
				FileDirector fileDirector = fileDirectors.get(FilenameUtils.getExtension(attachment.filename));
				if(fileDirector == null) {
					LOGGER.log(Level.FINE, "Skipping " + attachment.filename + " as it has an non-configured extension.");
				}
				fileDirector.directFile(bais, submission, attachment.filename);
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Couldn't unzip attachment for " + submission, e);
		}
	}

	private boolean canBeUnzipped(Attachment attachment) {
		return attachment.filename.endsWith(".zip") || attachment.filename.endsWith(".jar");
	}
	
	private void handlePropertieFile(String url, AutograderSubmission student) {
		try (ByteArrayInputStream bis = new ByteArrayInputStream(connection.downloadFile(url))) {
			createSubmissionRecord(bis, student);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void handlePdf(String url, AutograderSubmission student, String filename) {
		try {
			FileUtils.writeByteArrayToFile(new File(student.directory.getAbsolutePath() + "/" + filename),
					connection.downloadFile(url));
		} catch (IOException e) {
			System.out.println("Could not write pdf to file from " + student + ". Reason: " + e.getMessage());
		}
	}
	
	private void handleSourceFile(String url, AutograderSubmission student, String filename) {
		try {
			FileUtils.writeByteArrayToFile(new File(student.getSourceDirectory().getAbsolutePath() + "/" + filename), connection.downloadFile(url));
		} catch (IOException e) {
			System.out.println("Could not write " + filename + " to file from " + student + ". Reason: " + e.getMessage());
		}
	}

	private void createSubmissionRecord(ByteArrayInputStream bis, AutograderSubmission student) {
		try {
			Properties props = new Properties();
			props.load(bis);
			try(OutputStream out = new FileOutputStream(student.directory.getAbsolutePath() + "/assignment.properites")) {
				props.store(out, "Saving assignment.properites file");
			}
			student.assignProps = new AssignmentProperties(props);
		} catch (IOException | ConfigurationException | IllegalArgumentException e) {
			errorRegistry.addInvalidAssignmentProperties(student);
			System.out.println(student + " messed up on assignment property file. Reason: " + e.getMessage());
		}
	}
	
	private Pattern createExcludePattern() {
		if(config.ignorePattern != null) {
			return Pattern.compile(config.ignorePattern);
		}
		return Pattern.compile("$^"); // matches empty strings.
	}
}

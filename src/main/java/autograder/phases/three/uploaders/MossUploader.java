package autograder.phases.three.uploaders;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import autograder.canvas.responses.Submission;
import autograder.canvas.responses.User;
import autograder.configuration.Configuration;
import autograder.filehandling.SubmissionParser;
import autograder.mailer.Mailer;
import autograder.phases.three.AssignmentUploader;
import autograder.portal.PortalConnection;
import autograder.settingsfiles.MossSettings;
import autograder.student.AutograderSubmission;
import autograder.student.AutograderSubmissionMap;
import it.zielke.moji.MossException;
import it.zielke.moji.SocketClient;

/**
 * Leverages a Java wrapper around the simple MOSS (Standford) client interface. More info https://zielke.it/moji/
 * 
 * Requires a separate Settings file to operate. Can pull from multiple different assignments and courses, so long as
 * they use the same default porta. 
 * 
 * Results are emailed out to recipients specified in the settings file. 
 * @author ryans
 */
public class MossUploader implements AssignmentUploader {
	private static final String MOSS_DIRNAME = "moss";
	private MossSettings mossSettings;
	private PortalConnection portalConnection;
	private Configuration config;
	private SubmissionParser parser;
	List<String> errors = new ArrayList<>();
	private Mailer emailer;
	private SocketClient socketClient;
	private static final Logger LOGGER = LoggerFactory.getLogger(MossUploader.class);

	@Inject
	public MossUploader(MossSettings mossSettings, Configuration configuration, PortalConnection portalConnection, SubmissionParser parser, Mailer emailer, SocketClient socketClient) {
		this.mossSettings = mossSettings;
		this.portalConnection = portalConnection;
		this.config = configuration;
		this.parser = parser;
		this.emailer = emailer;
		this.socketClient = socketClient;
	}
	
	@Override
	public void upload(AutograderSubmissionMap submissions) {
		LOGGER.info("Running MOSS Uploader");
		File mossDir = createTempMossDirectory();
		String results = "No Result Obtained";
		addExtraSubmissions(submissions);
		
		// move source files into mossdir
		LOGGER.info("Downloading Extra Submissions");
		for(AutograderSubmission submission : submissions.listStudents()) {
			String filename = getFilename(submission);
			File studentDir = new File(mossDir, filename);
			studentDir.mkdir();
			try {
				FileUtils.copyDirectory(submission.getSourceDirectory(), studentDir);
			} catch (IOException e) {
				logError("Copying from " + submission.getSourceDirectory() + " to " + studentDir + " failed.", e);
			}
		}
		
		socketClient.setUserID(mossSettings.userId);
		try {
			socketClient.setLanguage(mossSettings.language);
		} catch (MossException e) {
			logError("Looks like you configured an unknown language" + "(" + mossSettings.language + ")" + "moss. " + e.getMessage(), e);
			sendResult(null);
			return;
		}
		
		try {
			socketClient.run();
		} catch (MossException | IOException e1) {
			logError("Failure when running Moss Client. " + e1.getMessage(), e1);
			sendResult(results);
			return;
		}
		
		LOGGER.info("Submitting files to MOSS");
		File baseDir = new File(mossSettings.baseFileDir);
		for(File baseFile : baseDir.listFiles(filename -> filename.getName().endsWith(".java"))) {
			if(!baseFile.exists()) {
				logError("Base file specified at " + baseFile + " does not exist!");
			} else {
				try {
					socketClient.uploadBaseFile(baseFile);
				} catch (IOException e) {
					logError(baseFile.toString(), e);
				}
			}
		}
		
		Collection<File> submittableFiles = FileUtils.listFiles(mossDir, new String[]{"java"}, true); //recursively searches for all java files 
		for(File submittableFile : submittableFiles) {
			try {
				socketClient.uploadFile(submittableFile);
			} catch (IOException e) {
				logError("Could not upload " + submittableFile, e);
			}
		}
		
		try {
			socketClient.sendQuery();
			results = socketClient.getResultURL().toURI().toString();
		} catch (MossException | IOException | URISyntaxException e) {
			logError("Failure when sending Query to Moss. " + e.getMessage(), e);
			sendResult(results);
			return;
		}
		
		sendResult(results);
		LOGGER.info("Moss Result: " + results);
	}

	private String getFilename(AutograderSubmission submission) {
		if(submission.studentInfo == null || submission.studentInfo.sortableName == null) {
			return Integer.toString(submission.submissionInfo.user_id);
		}
		return User.forFileName(submission.studentInfo);
	}

	private void sendResult(String resultMessage) {
		if (mossSettings.recipients.length == 0){
			LOGGER.error("No recipients specified in Moss Settings File");
		}
		
		StringBuilder body = new StringBuilder();
		body.append("Here is the result from running MOSS on " + config.assignment + ":\n");
		body.append(resultMessage).append("\n");
		
		if(!errors.isEmpty()) {
			body.append("The following errors occured.\n");
			for(String error : errors) {
				body.append(error).append('\n');
			}
		}
		
		body.append("Sincerely,\nThe AutoGrader");
			
		for(String recipient : mossSettings.recipients) {
			emailer.sendMail(recipient, config.assignment + " MOSS Results", body.toString());
		}
	}

	private void logError(String basePath, Exception e) {
		String errMsg = "Failed to upload " + basePath + " to moss.";
		errors.add(errMsg);
		LOGGER.warn(errMsg, e);
	}
	
	private void logError(String basePath) {
		String errMsg = "Failed to upload " + basePath + " to moss.";
		errors.add(errMsg);
		LOGGER.warn(errMsg);
	}

	private void addExtraSubmissions(AutograderSubmissionMap submissions) {
		for( Entry<String, String[]> courseToAssignemnt : mossSettings.courseToAssignments.entrySet()) {
			String courseId = courseToAssignemnt.getKey();
			for(String assignment : courseToAssignemnt.getValue()) {
				Submission[] extraSubmissions = portalConnection.getAllSubmissions(courseId, assignment);
				for(Submission extraSubmission : extraSubmissions) {
					try {
						AutograderSubmission extraAutograderSubmission = parser.parseAndCreateSubmission(extraSubmission);
						extraAutograderSubmission.setUser(portalConnection.getStudentById(Integer.toString(extraSubmission.user_id)));
						submissions.addSubmission(extraAutograderSubmission);
					} catch(Exception e) {
						LOGGER.info(e.getMessage(), e);
					}
				}
			}
		}
	}

	private File createTempMossDirectory() {
		File mossDir = new File(MOSS_DIRNAME);
		if(mossDir.exists()) {
			try {
				FileUtils.cleanDirectory(mossDir);
			} catch (IOException e1) {
				LOGGER.debug("Cleaning the directory failed with an exception. However, the code will still attempt to continue.", e1);
			}
		} else {
			mossDir.mkdirs();
		}
		return mossDir;
	}
}

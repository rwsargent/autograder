package autograder.phases.two.workers;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import autograder.canvas.responses.User;
import autograder.configuration.Configuration;
import autograder.phases.two.Worker;
import autograder.student.AutograderSubmission;

public abstract class ExternalProcessWorker implements Worker {
	
	protected Configuration config;
	protected AutograderSubmission submission;
	protected Redirect stdRedirect, errorRedirect;
	
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Inject
	public ExternalProcessWorker(Configuration configuration) {
		config = configuration;
	}
	
	@Override
	public void doWork(AutograderSubmission submission) {
		LOGGER.info("Starting external process on " + submission);
		this.submission = submission;
		setRedirects();
		Process process = buildAndStartProcess();
		boolean timedOut = false;
		try {
			timedOut = !process.waitFor(config.processTimeoutSeconds, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			LOGGER.error(submission + " was interrupted while waiting on external work.", e);
		}
		
		onExit(process, timedOut);
		
		// clean up
		if(FileUtils.sizeOf(errorRedirect.file()) == 0) {
			errorRedirect.file().delete();
		}
		process.destroy();
	}
	
	protected abstract String[] buildProcessCommand();
	
	protected abstract void onExit(Process processs, boolean timedOut);
	
	protected void setRedirects() {
		stdRedirect = getStandardOutRedirect();
		errorRedirect = getErrorRedirect();
	}

	protected Redirect getErrorRedirect() {
		return Redirect.to(new File(submission.getDirectory(), "error.result"));
	}

	protected Redirect getStandardOutRedirect() {
		String name = User.forFileName(submission.studentInfo) + "_" + submission.submissionInfo.user_id + ".result";
		return Redirect.to(new File(submission.getDirectory(), name));
	}
	
	protected String findFileAsPath(String filepath) {
		File grader = new File(filepath);
		if (grader.exists()) {
			return grader.getAbsolutePath();
		} 
		return getClass().getResource(filepath).toString();
	}
	
	protected Process buildAndStartProcess() {
		String[] command = buildProcessCommand();
		LOGGER.debug("External command: " + Arrays.toString(command).replace(",", ""));
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.directory(currentWorkingDirectory());
		if(errorRedirect != null) {
			processBuilder.redirectError(errorRedirect);
		}
		if(stdRedirect != null) {
			processBuilder.redirectOutput(stdRedirect);
		}
		try {
			return processBuilder.start();
		} catch (IOException e) {
			throw new IllegalStateException();
		}
	}
	
	protected File currentWorkingDirectory() {
		return submission.getDirectory();
	}
	
	protected String generateClassPathString() {
		File libs = new File(config.extraClassPathFiles);// System.getProperty("java.class.path");
		StringBuilder sb = new StringBuilder();
		for(File jar : libs.listFiles((file, name) -> FilenameUtils.getExtension(name).equals("jar"))) {
			sb.append(jar.getAbsolutePath()).append(File.pathSeparatorChar);
		}
		sb.setLength(sb.length() -1); // remove the last path separator
		return sb.toString();
	}
}

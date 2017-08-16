package autograder.phases.two.workers;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;

import com.google.inject.Inject;

import autograder.configuration.Configuration;
import autograder.grading.Grader;
import autograder.phases.two.Worker;
import autograder.student.AutograderSubmission;

public abstract class ExternalProcessWorker implements Worker {
	
	protected Configuration mConfig;

	@Inject
	public ExternalProcessWorker(Configuration configuration) {
		mConfig = configuration;
	}
	protected AutograderSubmission mStudent;
	protected File output, error;
	protected Logger logger = Logger.getLogger(Grader.class.getName() + " " + Thread.currentThread().getName());
	
	public void setRedirects(File output, File error) {
		this.output = output;
		this.error = error;
	}
	
	protected String findFile(String filepath) {
		File grader = new File(filepath);
		if (grader.exists()) {
			return grader.getAbsolutePath();
		}
		return getClass().getResource(filepath).toString();
	}
	
	
	protected Process buildAndStartProcess() {
		ProcessBuilder processBuilder = new ProcessBuilder(buildProcessCommand());
		processBuilder.directory(currentWorkingDirectory());
		if(error != null) {
			processBuilder.redirectError(Redirect.appendTo(error));
		}
		if(output != null) {
			processBuilder.redirectOutput(Redirect.appendTo(output));
		}
		try {
			return processBuilder.start();
		} catch (IOException e) {
			throw new IllegalStateException();
		}
	}
	
	/**
	 * @param process
	 * @return The return value of the process 
	 */
	protected int waitOnProccess(Process process) {
		int compCode;
		try {
			compCode = process.waitFor();
		} catch (InterruptedException e) {
			compCode = -1;
			e.printStackTrace();
		}
		return compCode;
	}
	
	protected abstract String[] buildProcessCommand();
	
	protected File currentWorkingDirectory() {
		return mStudent.getSourceDirectory();
	}


	public AutograderSubmission getStudent() {
		return mStudent;
	}

	public void setStudent(AutograderSubmission student) {
		this.mStudent = student;
	}
	
	protected String generateClassPathString() {
		File libs = new File(mConfig.extraClassPathFiles);// System.getProperty("java.class.path");
		StringBuilder sb = new StringBuilder();
		for(File jar : libs.listFiles((file, name) -> FilenameUtils.getExtension(name).equals("jar"))) {
			sb.append(jar.getAbsolutePath()).append(File.pathSeparatorChar);
		}
		sb.setLength(sb.length() -1); // remove the last path separator
		return sb.toString();
	}
	
	protected String messageOnProcessFailure() {
		return "The proccessed exited with non-zero status.\nRan command: " + buildProcessCommand().toString();
	}
}

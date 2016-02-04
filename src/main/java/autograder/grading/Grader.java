package autograder.grading;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Queue;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;

import autograder.configuration.Configuration;
import autograder.student.Student;

public class Grader extends Thread {
	Student mStudent;
	String mGraderClassName, mGraderPath;
	ProcessBuilder processBuilder;
	Queue<WorkJob> workQueue;
	Logger logger;
	public Grader(Queue<WorkJob> queue) {
		this();
		workQueue = queue;
	}
	
	public Grader() {
		mGraderClassName = Configuration.getConfiguration().graderClassName;
		mGraderPath = findFile(Configuration.getConfiguration().graderFile);
		logger = Logger.getLogger(Grader.class.getName() + " " + Thread.currentThread().getName());
	}
	
	private String findFile(String configPath) {
		File grader = new File(configPath);
		if (grader.exists()) {
			return grader.getAbsolutePath();
		}
		return getClass().getResource(configPath).toString();
	}

	@Override
	public void run() {
		WorkJob job = null;
		while((job = workQueue.poll()) != null) {
			try {
				compileAndRunGrader(job.getStudent());
			} catch (Exception e ) {
				logger.severe(e.getMessage());
			}
		}
	}
	
	public void compileAndRunGrader(Student student) {
		mStudent = student;
		try {
			if(compile()) {
				runGrader();
			}
		} catch (IOException | InterruptedException e) {
			outputExecption("execution", e);
		}
	}

	private void outputExecption(String step, Exception e) {
		System.out.println(String.format("There was an error in %s of the test for %s. %s was thrown, with message %s", new Object[] {step, mStudent, e.getClass().getName(), e.getMessage()}));
	}
	
	private boolean compile() throws IOException, InterruptedException {
		File src = new File(mStudent.getSourceDirectoryPath()); 
		createClassesDirectoryInSourceDir();
		File compErrorFile = new File(mStudent.studentDirectory.getAbsolutePath() + "/comp_error.rws");
		
		String command = createJavacCommand();
		processBuilder = new ProcessBuilder(command.split(" ")); 
		processBuilder.directory(src);
		processBuilder.redirectError(Redirect.appendTo(compErrorFile));
		Process compilation = processBuilder.start();
		int compCode = compilation.waitFor();
		if(compCode == 1) { 
			logger.info(mStudent.studentInfo.name + " failed compilation. Check comp_error.txt for full details");
			return false;
		} else if (compCode == 2) {
			logger.info("Something is wrong with this javac command: " + command);
			return false;
		} else {
			if(!compErrorFile.delete()) {
				compErrorFile.deleteOnExit();
			}
		}
		compilation.destroy();
		return true;
	}

	private String createJavacCommand() {
		File source = mStudent.sourceDirectory;
		String classPath = System.getProperty("java.class.path");
		StringBuilder sb = new StringBuilder("javac -d classes -cp ");
		sb.append(classPath);
		sb.append(' ');
		for(File sourceFile : source.listFiles((file, name) -> FilenameUtils.getExtension(name).equals("java"))) {
			sb.append(sourceFile.getName());
			sb.append(' ');
		}
		sb.append(mGraderPath);
		return sb.toString();
	}

	private void createClassesDirectoryInSourceDir() {
		File classes = new File(mStudent.getSourceDirectoryPath() + "/classes");
		if (!classes.exists()) {
			if(!classes.mkdir()) {
				throw new RuntimeException("Could not create classes directory");
			}
		}
	}
	
	private void runGrader() throws IOException, InterruptedException {
		String[] testCommand = generateJavaGraderCommand().split(" ");
		File errorFile = new File(mStudent.studentDirectory.getAbsolutePath() + "/grader_output_error.rws");
		
		processBuilder.command(testCommand);
		processBuilder.directory(mStudent.studentDirectory);
		processBuilder.redirectOutput(Redirect.to(new File(mStudent.studentDirectory.getAbsolutePath() + "/grader_output.rws")));
		processBuilder.redirectError(Redirect.to(errorFile));
		Process test = processBuilder.start();
		int returnCode = test.waitFor();
		if(returnCode != 0 ) {
			throw new RuntimeException("Java didn't work: " + test.exitValue());
		} else {
			if(!errorFile.delete()) {
				errorFile.deleteOnExit();
			}
		}
		test.destroy();
	}

	private String generateJavaGraderCommand() {
		StringBuilder sb = new StringBuilder("java -cp ");
		sb.append("./source/classes");
		sb.append(File.pathSeparatorChar);
		sb.append(Configuration.getConfiguration().extraClassPathFiles);
		sb.append(' ');
		sb.append(mGraderClassName);
		return sb.toString();
	}
}
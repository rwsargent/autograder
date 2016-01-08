package autograder.grading;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
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
				compileAndRunTester(job.getStudent());
			} catch (Exception e ) {
				logger.severe(e.getMessage());
			}
		}
	}
	
	public void compileAndRunTester(Student student) {
		mStudent = student;
		try {
			if(compile()) {
				runTester();
			} else {
				
			}
		} catch (IOException | InterruptedException e) {
			outputExecption("execution", e);
		}
//		cleanUp();
	}

	private void cleanUp() {
		File errorFile = new File("/output" + mStudent + "_error");
		if(errorFile.canWrite()) {
			System.out.println("Can write...");
			if(!errorFile.delete()) {
				System.out.println("Can't delete!!");
			}	
		}
	}

	private void outputExecption(String step, Exception e) {
		System.out.println(String.format("There was an error in %s of the test for %s. %s was thrown, with message %s", new Object[] {step, mStudent, e.getClass().getName(), e.getMessage()}));
	}
	
	private boolean compile() throws IOException, InterruptedException {
		File src = new File(mStudent.getSourceDirectoryPath()); 
		createClassesDirectoryInSourceDir();
		String command = createJavacCommand();
		processBuilder = new ProcessBuilder(command.split(" ")); 
		processBuilder.directory(src);
		processBuilder.redirectError(Redirect.appendTo(new File(mStudent.studentDirectory.getAbsolutePath() + "/comp_error.txt")));
		Process compilation = processBuilder.start();
		if(compilation.waitFor(30, TimeUnit.SECONDS)) {
			if(compilation.exitValue() == 1) { 
				logger.info(mStudent.name + " failed compilation. Check comp_error.txt for full details");
				return false;
			} else if (compilation.exitValue() == 2) {
				logger.info("Something is wrong with this javac command: " + command);
				return false;
			}
		} else {
			throw new RuntimeException("Compiliation timed out on " + mStudent);
		}
		compilation.destroy();
		return false;
	}

	private String createJavacCommand() {
		File source = mStudent.sourceDirectory;
		StringBuilder sb = new StringBuilder("javac -d classes ");
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
	
	private void runTester() throws IOException, InterruptedException {
		String[] testCommand = ("java -cp ./classes " + mGraderClassName).split(" ");
		processBuilder.command(testCommand);
		processBuilder.redirectOutput(new File("./output/" + mStudent));
		processBuilder.redirectError(new File("./output/" + mStudent + "_error"));
		Process test = processBuilder.start();
		if(test.waitFor() != 0 ) {
			throw new RuntimeException("Java didn't work: " + test.exitValue());
		}
		test.destroy();
	}
}
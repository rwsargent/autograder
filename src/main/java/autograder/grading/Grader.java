package autograder.grading;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFileFilter;

import com.sun.org.glassfish.external.probe.provider.annotations.Probe;

import autograder.configuration.Configuration;
import autograder.student.Student;

public class Grader extends Thread {
	Student mStudent;
	String mTestClassName;
	ProcessBuilder processBuilder;
	Queue<WorkJob> workQueue;
	Logger logger;
	public Grader(Queue<WorkJob> queue) {
		workQueue = queue;
	}
	
	public Grader() {
		mTestClassName = Configuration.getConfiguration().graderName;
		logger = Logger.getLogger(Grader.class.getName() + " " + Thread.currentThread().getName());
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
			compile();
		} catch (Exception e) {
			outputExecption("compilation", e);
			return;
		}
		try {
			runTester();
		} catch (IOException | InterruptedException e) {
			outputExecption("execution", e);
		}
		cleanUp();
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
	
	private void compile() throws IOException, InterruptedException {
		File src = new File(mStudent.getSourceDirectoryPath()); 
		createClassesDirectoryInSourceDir();
		processBuilder = new ProcessBuilder(createJavacCommand().split(" ")); 
		processBuilder.directory(src);
		processBuilder.redirectError(Redirect.appendTo(new File(mStudent.studentDirectory.getAbsolutePath() + "/comp_error.txt")));
		Process compilation = processBuilder.start();
		if(compilation.waitFor(30, TimeUnit.SECONDS)) {
			if (compilation.exitValue() != 0) {
				throw new RuntimeException("Compilation failed on " + mStudent + " with error code: " + compilation.exitValue());
			}
		} else {
			throw new RuntimeException("Compiliation timed out on " + mStudent);
		}
		compilation.destroy();
	}

	private String createJavacCommand() {
		File source = mStudent.sourceDirectory;
		StringBuilder sb = new StringBuilder("javac -d classes ");
		sb.append(source.listFiles(file -> FilenameUtils.getExtension(file.getName()).equals("java")));
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
		String[] testCommand = ("java -cp ./classes " + mTestClassName).split(" ");
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
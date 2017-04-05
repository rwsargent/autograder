package autograder.grading;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import autograder.configuration.Configuration;
import autograder.phases.two.Worker;
import autograder.student.Student;

/**
 * {@link Grader} pulls job from a supplied workqueue of {@link WorkJob}, and compiles and grades the student
 * that in the WorkJob against the grader file as specified by Configuration. 
 * @author Ryans
 *
 */
public class Grader implements Worker {
	Student mStudent;
	String mGraderClassName, mGraderPath;
	ProcessBuilder processBuilder;
	Queue<WorkJob> workQueue;
	Logger logger;
	
	protected Configuration mConfig;
	
	@Inject
	public Grader(Configuration configuration) {
		 mConfig = configuration;
//		 mJarrer = jarrer;
		 mGraderClassName = mConfig.graderClassName;
		mGraderPath = findFile(mConfig.graderFile);
//			mJunitPluginPath = findFile(Configuration.getConfiguration().junitPlugin);
		logger = Logger.getLogger(Grader.class.getName() + " " + Thread.currentThread().getName());
	}
	
	public void setWorkQueue(Queue<WorkJob> queue) {
		workQueue = queue;
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
		System.out.println("Thread: " + currentThread() + " is starting to grade");
		while(!workQueue.isEmpty()) {
			job = workQueue.poll();
			if(job == null) {
				continue;
			}
			try {
				System.out.println("Grading: " + job.getStudent());
				compileAndRunGrader(job.getStudent());
			} catch (Exception e ) {
				logger.severe(e.getMessage());
			}
		}
		System.out.println("Thread: " + currentThread() + " finished grading.");
	}
	
	public void compileAndRunGrader(Student student) {
		mStudent = student;
		try {
			if(compile()) {
				runGrader();
				if(StringUtils.isNotBlank(mConfig.mainClass)) {
				}
			}
		} catch (IOException | InterruptedException e) {
			outputExecption("execution", e);
		}
	}

	private void outputExecption(String step, Exception e) {
		System.out.println(String.format("There was an error in %s of the test for %s. %s was thrown, with message %s", new Object[] {step, mStudent, e.getClass().getName(), e.getMessage()}));
	}
	
	private boolean compile() throws IOException, InterruptedException {
		File compErrorFile = new File(mStudent.studentDirectory.getAbsolutePath() + "/comp_error.rws");
		
		String command = createJavacCommand();
		if(command == null) { // we didn't find any java files
			return false;
		}
		createClassesDirectoryInSourceDir();
		processBuilder = new ProcessBuilder(command.split(" ")); 
		processBuilder.directory(mStudent.getSourceDirectory());
		processBuilder.redirectError(Redirect.appendTo(compErrorFile));
		Process compilation = processBuilder.start();
		int compCode = compilation.waitFor();
		if(compCode == 1) { 
			logger.info(mStudent.studentInfo.name + " failed compilation. Check comp_error.txt for full details");
			return true;
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
		File source = mStudent.getSourceDirectory();
		if(source == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder("javac -d classes -cp ");
		sb.append(generateClassPathString());
		sb.append(' ');
		boolean foundJavaFile = false;
		for(File sourceFile : source.listFiles((file, name) -> FilenameUtils.getExtension(name).equals("java"))) {
			sb.append(sourceFile.getName());
			sb.append(' ');
			foundJavaFile = true;
		}
		if(!foundJavaFile) {
			return null;
		}
		sb.append(mGraderPath);
		sb.append(" ").append(findFile("graders/SortGrader.java"));
		/* THIS IS ADDED FOR JUNIT */
		return sb.toString();
	}

	private String generateClassPathString() {
		File libs = new File(mConfig.extraClassPathFiles);// System.getProperty("java.class.path");
		StringBuilder sb = new StringBuilder();
		for(File jar : libs.listFiles((file, name) -> FilenameUtils.getExtension(name).equals("jar"))) {
			sb.append(jar.getAbsolutePath()).append(File.pathSeparatorChar);
		}
		sb.setLength(sb.length() -1); // remove the last path separator
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
		boolean timeout = test.waitFor(60, TimeUnit.SECONDS);
		if(timeout) {
			int returnCode = test.exitValue();
			if(returnCode != 0 ) {
				throw new RuntimeException("Java didn't work: " + test.exitValue() + ".\nJava Command: " + Arrays.toString(testCommand) + "\nOn student" + mStudent);
			} else {
				if(!errorFile.delete()) {
					errorFile.deleteOnExit();
				}
			}
		} else {
			try(FileWriter fw = new FileWriter(errorFile, true)) {
				logger.severe(mStudent + " timed out.");
				fw.write(mStudent + " took too long to grade.");
				fw.close();
			}
			test.destroyForcibly();
		}
		if(test.isAlive()) {
			test.destroy();
		}
	}

	private String generateJavaGraderCommand() {
		StringBuilder sb = new StringBuilder("java -cp ");
		sb.append("./source/classes");
		sb.append(File.pathSeparatorChar);
		sb.append(findFile(mConfig.extraClassPathFiles));
		sb.append(File.pathSeparatorChar);
		sb.append(generateClassPathString());
		sb.append(' ');
		sb.append("-Xms1024m -Xmx2048m").append(' ');
		sb.append(mConfig.graderClassName); // changed for JUnit
//		sb.append("graders.ProgrammingChallengeGrader"); // changed for JUnit
		sb.append(' ').append(buildArguments());
		return sb.toString();
	}

	private String buildArguments() {
		return "assignment05.SortGrader";
	}

	@Override
	public void doWork(Student student) {
		// TODO Auto-generated method stub
		
	}
}
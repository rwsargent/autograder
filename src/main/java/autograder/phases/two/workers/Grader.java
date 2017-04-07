package autograder.phases.two.workers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import autograder.configuration.Configuration;
import autograder.student.Student;

public class Grader extends ExternalProcessWorker {

	String graderArguments = "";
	@Inject
	public Grader(Configuration configuration, @Named("args")String arguments) {
		super(configuration);
		graderArguments = arguments; 
	}

	@Override
	public void doWork(Student student) {
		setStudent(student);
		setRedirects(new File(mStudent.studentDirectory.getAbsolutePath() + "/grader_output.rws"), 
				new File(mStudent.studentDirectory.getAbsolutePath() + "/grader_output_error.rws"));
		waitOnProccess(buildAndStartProcess());
	}
	
	@Override
	protected int waitOnProccess(Process process) {
		boolean timeout = false;
		int returnCode = 0;
		try {
			timeout = process.waitFor(Long.valueOf(mConfig.timeout), TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			this.logger.severe(e1.getMessage());
			return -1; 
		}
		if(timeout) {
			returnCode = process.exitValue();
			if(returnCode != 0 ) {
				throw new IllegalStateException("Java didn't work: " + process.exitValue() + ".\nJava Command: " + Arrays.toString(buildProcessCommand()) + "\nOn student " + mStudent.studentInfo.name);
			} else {
				if(!error.delete()) {
					error.deleteOnExit();
				}
			}
		} else {
			try(FileWriter fw = new FileWriter(error, true)) {
				logger.severe(mStudent + " timed out.");
				fw.write(mStudent + " took too long to grade.");
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			process.destroyForcibly();
		}
		if(process.isAlive()) {
			process.destroy();
		}
		return returnCode;
	}

	@Override
	protected String[] buildProcessCommand() {
		StringBuilder sb = new StringBuilder("java -cp ");
		sb.append("./source/classes");
		sb.append(File.pathSeparatorChar);
		sb.append(findFile(mConfig.extraClassPathFiles));
		sb.append(File.pathSeparatorChar);
		sb.append(generateClassPathString());
		sb.append(' ');
		sb.append(mConfig.graderJVMOptions).append(' '); // "-Xms1024m -Xmx2048m"
		sb.append(mConfig.graderClassName); // changed for JUnit
//		sb.append("graders.ProgrammingChallengeGrader"); // changed for JUnit
		sb.append(' ').append(graderArguments);
		return sb.toString().split(" ");
	}
}

package autograder.grader;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Grader {
	String mStudent;
	String mTestClassName;
	ProcessBuilder processBuilder;
	public Grader(String studentName, String mainClass) {
		mStudent = studentName;
		mTestClassName = mainClass;
	}
	
	public void compileAndRunTester() {
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
		File src = createClassesDirectoryInSourceDir();
		processBuilder = new ProcessBuilder(("javac *.java -d classes").split(" ")); 
		processBuilder.directory(src);
		Process compilation = processBuilder.start();
		if(compilation.waitFor(30, TimeUnit.SECONDS)) {
			if (compilation.exitValue() != 0) {
				throw new RuntimeException("Compilation failed with error code: " + compilation.exitValue());
			}
		} else {
			throw new RuntimeException("Compiliation timed out");
		}
	}

	private File createClassesDirectoryInSourceDir() {
		File src = new File(mStudent + "_src");
		File classes = new File(src.getAbsolutePath() + "/classes");
		if (!classes.exists()) {
			if(!classes.mkdir()) {
				throw new RuntimeException("Could not create classes directory");
			}
		}
		return src;
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
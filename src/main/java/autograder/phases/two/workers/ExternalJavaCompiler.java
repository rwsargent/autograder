package autograder.phases.two.workers;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

import com.google.inject.Inject;

import autograder.configuration.Configuration;
import autograder.student.AutograderSubmission;

/**
 * This worker will take all Java files from the students source directory and 
 * compile them into a classes directory inside the source directory. The tree looks like: 
 * Student/
 *   source/
 *      classes/
 *        Class1.class
 *        Class2.class
 * 
 * No data is put onto the Student after
 *      
 * @author ryansargent
 *
 */
public class ExternalJavaCompiler extends ExternalProcessWorker {

	@Inject
	public ExternalJavaCompiler(Configuration configuration) {
		super(configuration);
	}

	@Override
	public void doWork(AutograderSubmission student) {
		createClassesDirectoryInSourceDir();
		super.doWork(student);
	}

	@Override
	protected String[] buildProcessCommand() {
		File source = submission.getSourceDirectory();
		if(source == null) {
			return new String[]{"echo", "\"No Source Directory\""};
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
		sb.append(findFileAsPath(config.graderFile));
		return sb.toString().split(" ");
	}
	
	private void createClassesDirectoryInSourceDir() {
		File classes = submission.getClassesDirectory();
		if (!classes.exists()) {
			if(!classes.mkdirs()) {
				throw new RuntimeException("Could not create classes directory");
			}
		}
	}

	@Override
	protected void onExit(Process processs, boolean timedOut) {
		// no-op
	}
}

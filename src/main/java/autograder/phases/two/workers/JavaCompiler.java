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
public class JavaCompiler extends ExternalProcessWorker {

	@Inject
	public JavaCompiler(Configuration configuration) {
		super(configuration);
	}

	@Override
	public void doWork(AutograderSubmission student) {
		setStudent(student);
		setRedirects(null, new File(mStudent.directory.getAbsolutePath() + "/compile_error.txt"));
		createClassesDirectoryInSourceDir();
		int retvalue = waitOnProccess(buildAndStartProcess());
		if(retvalue != 0) {
			//compilation failed.
		}
	}

	@Override
	protected String[] buildProcessCommand() {
		File source = mStudent.getSourceDirectory();
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
		sb.append(findFile(mConfig.graderFile));
		return sb.toString().split(" ");
	}
	
	private void createClassesDirectoryInSourceDir() {
		File classes = new File(mStudent.getSourceDirectoryPath() + "/classes");
		if (!classes.exists()) {
			if(!classes.mkdir()) {
				throw new RuntimeException("Could not create classes directory");
			}
		}
	}
}

package autograder.phases.two.workers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;

import com.google.inject.Inject;

import autograder.configuration.Configuration;
import autograderutils.results.AutograderResult;
import autograderutils.results.JUnitAutograderResultFromFile;

/**
 * Autograder-Utils has main method that will run the Autograder. 
 * It takes two parameters: the qualified name of the grader suite class,
 * and the path to the result file. 
 * @author ryans
 */
public class ExternalAutograderUtilsProcess extends ExternalProcessWorker {

	@Inject
	public ExternalAutograderUtilsProcess(Configuration configuration) {
		super(configuration);
	}

	@Override
	protected void onExit(Process processs, boolean timedOut) {
		if(timedOut) {
			LOGGER.warn(submission + " timed out while executing.");
			try {
				FileUtils.write(stdRedirect.file(), "\nPROCESS TIMED OUT\n", Charset.defaultCharset(), true);
			} catch (IOException e) {
				LOGGER.error(submission + " errored while writing a process time out message.", e);
			}
		}
		// build result from file and assign to student
		try(FileInputStream fis = new FileInputStream(stdRedirect.file())) {
			AutograderResult result = new JUnitAutograderResultFromFile(fis);
			submission.setResult(result);
		} catch (IOException e) {
			LOGGER.error(submission + " couldn't read from std out file while trying to construct autograder result", e);
		}
	}
	
	@Override
	protected String[] buildProcessCommand() {
		StringBuilder sb = new StringBuilder("java -cp ");
		sb.append("./source/classes")
			.append(File.pathSeparatorChar)
			.append(findFileAsPath(config.extraClassPathFiles))
			.append(File.pathSeparatorChar)
			.append(generateClassPathString())
			
			.append(' ')
			.append(extraJVMOptions()).append(' ')
			
			.append("autograderutils.JUnitPlugin")
			.append(' ')
			.append(config.graderClassName);
		return sb.toString().split(" ");
	}

	private String extraJVMOptions() {
		String options = "";
		if(System.getProperty("java.security.policy") != null) {
			options += " -Djava.security.policy=" + findFileAsPath(System.getProperty("java.security.policy"));
		}
		String configOptions = config.graderJVMOptions == null ? "" : config.graderJVMOptions;
		return configOptions + options;
	}
}

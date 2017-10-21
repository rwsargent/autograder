package autograder.phases.two.workers;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autograder.configuration.Configuration;
import autograder.phases.two.Worker;
import autograder.student.AutograderSubmission;
import autograderutils.JUnitPlugin;
import autograderutils.results.JUnitAutograderResult;

/**
 * This class uses the Autograder-Utils {@link JUnitPlugin}, with a URLClassLoader that is
 * pointed at the classes directory of the submission 
 * @author ryans
 */
public class JUnitGrader implements Worker {
	
	private Configuration configuration;
	public static final Logger LOGGER = LoggerFactory.getLogger(JUnitGrader.class); 

	@Inject
	public JUnitGrader(Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void doWork(AutograderSubmission submission) {
		LOGGER.info("Grading " + submission.studentInfo.name + "(" + submission.toString() + ")");
		List<URL> javaBinaryUrls = new ArrayList<>();
		try {
			javaBinaryUrls.add(submission.getClassesDirectory().toURI().toURL());
			javaBinaryUrls.addAll(getUrlsFromExtraClassPath());
		} catch (MalformedURLException e) {
			LOGGER.error("Weird malformed URL while trying to grade " + submission, e);
			return;
		}
		
		URL[] urls = javaBinaryUrls.toArray(new URL[javaBinaryUrls.size()]);
		try(URLClassLoader classLoader = new URLClassLoader(urls, getClass().getClassLoader())) {
			JUnitPlugin grader = null;
			try {
				@SuppressWarnings("unchecked")
				Class<JUnitPlugin> clazz = (Class<JUnitPlugin>) classLoader.loadClass(JUnitPlugin.class.getName());
				grader = clazz.newInstance();
			} catch (ClassNotFoundException | IllegalAccessException e) {
				LOGGER.error("This should never happen.", e);
				return;
			} catch (InstantiationException e) {
				LOGGER.error("Can't instantiate grader", e);
			}
			
			try {
				Class<?> loadClass = classLoader.loadClass(configuration.graderClassName);
				// students code will run inside "Grade"
				PrintStream stdOut = System.out;
				System.setOut(new PrintStream(new NullOutputStream()));
				JUnitAutograderResult result = grader.grade(loadClass);
				System.setOut(stdOut);
				if(result == null) {
					LOGGER.warn("Result is null for " + submission);
				}
				submission.setResult(result);
				submission.setProperty("result", "true");
			} catch (ClassNotFoundException e) {
				LOGGER.error("Could not load " + configuration.graderClassName + " to grade the submission for " + submission, e);
			}
			
		} catch (IOException e1) {
			LOGGER.error("Trouble closing the IOStream for " + submission, e1);
		};
		
	}

	private List<URL> getUrlsFromExtraClassPath() throws MalformedURLException {
		File extraClassPathDir = new File(configuration.extraClassPathFiles);
		File[] jars = extraClassPathDir.listFiles(file -> FilenameUtils.getExtension(file.getName()).equals("jar"));
		List<URL> jarUrls = new ArrayList<>();
		for(File jar : jars) {
			jarUrls.add(jar.toURI().toURL());
		}
		return jarUrls;
	}
}

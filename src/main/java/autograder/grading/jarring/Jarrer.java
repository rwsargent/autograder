package autograder.grading.jarring;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import autograder.configuration.Configuration;
import autograder.phases.two.workers.ExternalProcessWorker;
import autograder.student.AutograderSubmission;

/**
 * This will build a jar for a student and his or her compiled source code.
 * @param student
 * @return
 */
public class Jarrer extends ExternalProcessWorker {
	
	@Inject
	public Jarrer(Configuration configuration) {
		super(configuration);
	}
	
	@Override
	protected String[] buildProcessCommand() {
		StringBuilder jarCommand = new StringBuilder();
		jarCommand.append("jar ");
		jarCommand.append("cfe ");
		jarCommand.append(outputFile(submission)).append(' ');
		
		try {
			String entryPoint = resolveEntryPoint(submission.getClassesDirectory());
			jarCommand.append(entryPoint).append(' ');
		} catch (IllegalStateException | IOException e) {
			LOGGER.error("Could not build execuable jar " + e.getMessage(), e);
			throw new IllegalStateException();
		}
		
		jarCommand.append("-C classes . ");
		return jarCommand.toString().split(" ");
	}
	
	@Override
	protected void onExit(Process processs, boolean timedOut) {
		//no-op
	}

	private String outputFile(AutograderSubmission student) {
		return student.directory.getAbsolutePath() + File.separatorChar + config.assignment + ".jar";
	}
	
	/**
	 * Tries to get the full, qualifed name of the Main class. If the supplied Main Class matches, 
	 * use that one. If not, try to find any class with the same name as the one in configuration,
	 * then resolve it's full qualified name.
	 * @param classes
	 * @return - qualified name of main class;
	 * @throws IOException
	 * @throws IllegalStateException
	 */
	private String resolveEntryPoint(File classes) throws IOException, IllegalStateException {
		Path classesPath = Paths.get(classes.toURI());
		
		// If we know the package and name:
		String relativeMainClass = config.mainClass.replace('.', '/') + ".class";
		if(classesPath.resolve(relativeMainClass).toFile().exists()) {
			return config.mainClass;
		}
		
		// Student didn't follow directions, let's try to find a file with the same name.
		int lastDot = config.mainClass.lastIndexOf('.');
		String mainClassName = config.mainClass;
		if(lastDot > 0) {
			mainClassName = mainClassName.substring(lastDot);
		}
		mainClassName += ".class";
		final String mainClass = mainClassName; // final to make streams happy. Ugh. 
		List<Path> possibleMatches = Files.walk(classesPath)
			.filter( path -> path.getFileName().toString().equals(mainClass))
			.collect(Collectors.toList());
				
		if(possibleMatches.size() == 1) {
			String mainClassPath = possibleMatches.get(0).toFile().getAbsolutePath();
			String relativeToClasses = StringUtils.difference(classes.getAbsolutePath() + File.separatorChar, mainClassPath);
			return FilenameUtils.removeExtension(relativeToClasses).replace(File.separatorChar, '.');
		} else {
			throw new IllegalStateException("Too many files matched the main class");
		}
	}
}

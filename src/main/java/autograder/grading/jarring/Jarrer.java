package autograder.grading.jarring;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import autograder.configuration.Configuration;
import autograder.student.Student;

public class Jarrer {
	private static final Logger LOGGER = Logger.getLogger(Jarrer.class.getName());
	
	protected Configuration mConfiguration;
	public Jarrer(Configuration configuration) {
		mConfiguration = configuration;
	}
	
	/**
	 * This will build a jar for a student and his or her compiled source code.
	 * @param student
	 * @return
	 */
	public boolean buildJarFor(Student student) {
		File file = student.getClassesDirectory();
		if (!file.exists()) {
			System.out.println("The classes directory in student " + student + " does not exist.");
			return false;
		}

		StringBuilder jarCommand = new StringBuilder();
		jarCommand.append("jar ");
		jarCommand.append("cfe ");
		jarCommand.append(outputFile(student)).append(' ');
		
		try {
			String entryPoint = resolveEntryPoint(student.getClassesDirectory());
			jarCommand.append(entryPoint).append(' ');
		} catch (IllegalStateException | IOException e) {
			LOGGER.info("Could not build execuable jar " + e.getMessage());
			return false;
		}
		
		jarCommand.append("-C classes . ");
		
		return executeCommand(student, jarCommand);
	}

	private boolean executeCommand(Student student, StringBuilder jarCommand) {
		ProcessBuilder processBuilder = new ProcessBuilder(jarCommand.toString().split(" ")); 
		processBuilder.directory(student.getSourceDirectory());
		Process jarring;
		try {
			jarring = processBuilder.start();
			int retValue = jarring.waitFor();
			if(retValue != 0){
				LOGGER.severe("Jarring completed, but exited with error code: " + retValue + " for " + student);
			}
		} catch (IOException | InterruptedException e) {
			LOGGER.severe("Jarring failed for " + student + ". "  + e.getMessage());
			return false;
		}
		return true;
	}

	private String outputFile(Student student) {
		return student.studentDirectory.getAbsolutePath() + File.separatorChar + mConfiguration.assignment + ".jar";
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
		String relativeMainClass = mConfiguration.mainClass.replace('.', '/') + ".class";
		if(classesPath.resolve(relativeMainClass).toFile().exists()) {
			return mConfiguration.mainClass;
		}
		
		// Student didn't follow directions, let's try to find a file with the same name.
		int lastDot = mConfiguration.mainClass.lastIndexOf('.');
		String mainClassName = mConfiguration.mainClass;
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

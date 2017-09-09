package autograder.phases.two.workers;

import static autograder.Constants.SubmissionProperties.COMPILED;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autograder.Constants;
import autograder.configuration.Configuration;
import autograder.phases.two.Worker;
import autograder.student.AutograderSubmission;

public class InternalJavaCompiler implements Worker {

	private static final Logger LOGGER = LoggerFactory.getLogger(InternalJavaCompiler.class);
	private Configuration configuration;
	
	@Inject
	public InternalJavaCompiler(Configuration configuration) {
		this.configuration = configuration; 
	}
	
	@Override
	public void doWork(AutograderSubmission submission) {
		LOGGER.debug("Compiling " + submission.submissionInfo.user_id);
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if(compiler == null) {
			LOGGER.error("Could not find a default Java Compiler from the tool provider.");
			return;
		}
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, Locale.getDefault(), Charset.defaultCharset());
		File[] sourceFiles = submission.getSourceDirectory().listFiles(file -> FilenameUtils.getExtension(file.getName()).equals("java"));
		if(sourceFiles == null || sourceFiles.length == 0) {
			LOGGER.debug("Skipping student " + submission + " for not having any source files to compile!");
			submission.setProperty("compiled", "false");
			return;
		}
		Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(sourceFiles);
		
		List<String> compilerOptions = Arrays.asList("-cp", generateClassPathString(), "-d", submission.getClassesDirectory().getAbsolutePath());
		Writer writer = new OutputStreamWriter(System.err);
		JavaCompiler.CompilationTask task = compiler.getTask(writer, fileManager, diagnostics, compilerOptions, null,
				compilationUnits);
		boolean compiled = task.call();
		submission.setProperty(COMPILED, Boolean.toString(compiled));
		
		if(!compiled) {
			try(FileWriter fw = new FileWriter(new File(submission.getDirectory(), Constants.COMPILE_ERROR_FILENAME));
					BufferedWriter bw = new BufferedWriter(fw)) {
				for(Diagnostic<? extends JavaFileObject> error : diagnostics.getDiagnostics()) {
					bw.write(error.toString());
					LOGGER.error(error.getMessage(Locale.getDefault()));
				}
			} catch (IOException e) {
				LOGGER.error("Writing compiliation error for submission " + submission, e);
			}
		}
	}
	
	protected String generateClassPathString() {
		File libs = new File(configuration.extraClassPathFiles);// System.getProperty("java.class.path");
		StringBuilder sb = new StringBuilder();
		for(File jar : libs.listFiles((file, name) -> FilenameUtils.getExtension(name).equals("jar"))) {
			sb.append(jar.getAbsolutePath()).append(File.pathSeparatorChar);
		}
		sb.setLength(sb.length() -1); // remove the last path separator
		return sb.toString();
	}
}

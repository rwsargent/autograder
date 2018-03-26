package autograder.filehandling;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autograder.Constants;
import autograder.configuration.Configuration;
import autograder.student.AutograderSubmission;
import autograder.student.SubmissionPair;

public class Bundler {
	private  byte[] buffer = new byte[1024];
	private List<String> validFileExtensions;
	private List<String> validFileNames;
	
	private Configuration config;
	
	private final Logger LOGGER = LoggerFactory.getLogger(Bundler.class);
	
	@Inject
	public Bundler(Configuration configuration) {
		validFileExtensions = Arrays.asList(configuration.validFileExtensions);
		validFileNames = Arrays.asList(configuration.validFileNames);
		
		config = configuration;
	}
	
	public Map<String, File> bundle(Map<String, List<AutograderSubmission>> groups) {
		LOGGER.info("Bundling for " + groups.keySet().toString());
		Map<String, File> output = new HashMap<>();
		for(String group : groups.keySet()) {
			List<AutograderSubmission> submissionsInGroup = groups.get(group);
			File destination = new File(buildOutputDestinationFilename(group));
			destination.getParentFile().mkdirs();
			output.put(group, destination);
			try(Zipper zipper = new Zipper()) {
				zipper.init(destination);
				for(AutograderSubmission submission : submissionsInGroup) {
					String topLevel = submission.studentInfo.sortableName;
//					zipper.addEntry(topLevel);
					
					//add base files 
					for(File file : submission.getDirectory().listFiles()) {
						if(!file.isDirectory() && FileUtils.sizeOf(file) > 0) {
							zipper.addEntry(topLevel +"/" + file.getName(), file);
						}
					}
					
					//add source files
					File sourceDirectory = submission.getSourceDirectory();
					if(sourceDirectory != null && sourceDirectory.list().length != 0 ) {
						zipper.addEntry(topLevel + "/src/");
						for(File sourceFile : sourceDirectory.listFiles()) {
							if(!sourceFile.isDirectory()) {
								zipper.addEntry(topLevel + "/src/" + sourceFile.getName(), sourceFile);
							}
						}
					}
				}
			} catch (IOException e) {
				LOGGER.error("While trying to bundle for: " + group, " an error occured.", e);
			}
		}
		return output;
	}

	public Map<String, File> bundlePair(Map<String, List<SubmissionPair>> studentToTaMap) {
		HashMap<String, File> taToBigZipMap = new HashMap<>();
		for(String ta: studentToTaMap.keySet()) {
			System.out.println("Bundling " + ta + "'s students");
			File taZipFile = new File(buildOutputDestinationFilename(ta));
			taZipFile.getParentFile().mkdirs();
			try(FileOutputStream fout = new FileOutputStream(taZipFile);
					ZipOutputStream zipWriter = new ZipOutputStream(new BufferedOutputStream(fout))) {
				writeAssignmentFileToZip(zipWriter);
				writeExtraFilesToZip(zipWriter);
				for(SubmissionPair pair : studentToTaMap.get(ta)) {
					try {
						String parentDirectory = getParentDirectoryName(pair);
						zipWriter.putNextEntry(new ZipEntry(parentDirectory));
						zipWriter.closeEntry();
						writeFilesToZip(pair.partner, zipWriter, parentDirectory);
						writeFilesToZip(pair.submitter, zipWriter, parentDirectory);
					} catch (ZipException e) {
						System.out.println(e.getMessage());
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			} 
			taToBigZipMap.put(ta, taZipFile);
		}
		return taToBigZipMap;
	}

	private String buildOutputDestinationFilename(String group) {
		return String.format("%s/%s/%s-grading.zip", Constants.ZIPS, group, config.assignment);
	}

	private String getParentDirectoryName(SubmissionPair pair) {
		if(pair.partner.studentInfo.sortableName.startsWith("invalid")) {
			return pair.submitter.studentInfo.sortableName + "/";
		} else {
			return pair.submitter.studentInfo.sortableName +"--" + pair.partner.studentInfo.sortableName + "/";
		}
	}

	private void writeExtraFilesToZip(ZipOutputStream zipWriter) throws IOException {
		if(StringUtils.isBlank(config.extraBundledFilesCsv)) {
			return;
		}
		for(String dependency : config.extraBundledFilesCsv.split(",")) {
			String dependencyFileName = FilenameUtils.getName(dependency);
			File dependencyFile = new File(dependency);
			if(dependencyFile.isDirectory()) {
				for(File file : dependencyFile.listFiles()) {
					writeZipEntry(zipWriter, new ZipEntry(dependencyFileName + "/" + FilenameUtils.getName(file.getName())), file);
				}
			} else {
				writeZipEntry(zipWriter, new ZipEntry(dependencyFileName), dependencyFile);
			}
		}
	}

	private void writeAssignmentFileToZip(ZipOutputStream zipWriter) throws IOException {
		String graderFileName = FilenameUtils.getName(config.graderFile);
		if(StringUtils.isNotBlank(graderFileName)) {
			writeZipEntry(zipWriter, new ZipEntry(graderFileName), new File(config.graderFile));
		}
	}

	private void writeFilesToZip(AutograderSubmission student, ZipOutputStream zipWriter, String parentDirectory) throws IOException {
		if(student.studentInfo.name.equals("placeholder") || student.studentInfo.name.startsWith("invalid")) { // skip nonexistant students
			return; 
		}
		try {
			zipWriter.putNextEntry(new ZipEntry(parentDirectory + student.studentInfo.name + "/")); // create directories
			zipWriter.closeEntry();
			File sourceDirectory = student.getSourceDirectory();
			if(sourceDirectory != null && sourceDirectory.list().length != 0 ) {
				zipWriter.putNextEntry(new ZipEntry(parentDirectory + student.studentInfo.name + "/src/"));
				zipWriter.closeEntry();
			}
		} catch (ZipException e) {
			if(e.getMessage().contains("uplicate")) {
				System.out.println("Duplicate entry on " + student);
			} else {
				System.out.println(e.getMessage());
			}
		}
		recursiveWriteToZip(zipWriter, student.directory, parentDirectory +student.studentInfo.name);
	}

	private void recursiveWriteToZip(ZipOutputStream zipWriter, File studentRoot, String name) throws IOException {
		for(File file : studentRoot.listFiles()) {
			if(file.isDirectory()) {
				recursiveWriteToZip(zipWriter, file, name);
				continue;
			}
			if(file.getName().endsWith(".java")) {
				writeZipEntry(zipWriter, new ZipEntry(name + "/src/" + file.getName()), file);
			} else if(validFileExtensions.contains(FilenameUtils.getExtension(file.getName())) ||
					validFileNames.contains(FilenameUtils.getBaseName(file.getName()))) {
				writeZipEntry(zipWriter, new ZipEntry(name + "/" + file.getName()), file);
			}
		}
	}
	
	private void writeZipEntry(ZipOutputStream zipWriter, ZipEntry entry, File file) throws IOException {
		try(FileInputStream in = new FileInputStream(file.getAbsolutePath())) {
			zipWriter.putNextEntry(entry);
			int len;
			while ((len = in.read(buffer)) > 0) {
				zipWriter.write(buffer, 0, len);
			}
			zipWriter.closeEntry();
		} catch (ZipException e) {
			if(e.getMessage().contains("uplicate")) {
				System.out.println("Duplicate entry on " + file.getName());
			} else {
				System.out.println(e.getMessage());
			}
		}
	}
}

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
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import autograder.Constants;
import autograder.configuration.Configuration;
import autograder.student.AutograderSubmission;
import autograder.student.SubmissionPair;

public class Bundler {
	private  byte[] buffer = new byte[1024];
	private List<String> validFileExtensions = Arrays.asList("pdf", "rws", "txt", "jar", "md");
	private List<String> validFileNames = Arrays.asList("i_worked_with", "README", "readme", "partner_evaluation");
	
	private Configuration mConfig;
	
	public Bundler(Configuration config) {
		validFileExtensions = Arrays.asList(config.validFileExtensions.split(","));
		validFileNames = Arrays.asList(config.validFileNames.split(","));
		
		mConfig = config;
	}
	
	public Map<String, File> bundleStudents(HashMap<String, Set<SubmissionPair>> studentToTaMap) {
		HashMap<String, File> taToBigZipMap = new HashMap<>();
		for(String ta: studentToTaMap.keySet()) {
			System.out.println("Bundling " + ta + "'s students");
			File taZipFile = new File(String.format("%s/%s/%s-grading.zip", Constants.ZIPS, ta, mConfig.assignment));
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

	private String getParentDirectoryName(SubmissionPair pair) {
		if(pair.partner.studentInfo.sortableName.startsWith("invalid")) {
			return pair.submitter.studentInfo.sortableName + "/";
		} else {
			return pair.submitter.studentInfo.sortableName +"--" + pair.partner.studentInfo.sortableName + "/";
		}
	}

	private void writeExtraFilesToZip(ZipOutputStream zipWriter) throws IOException {
		if(StringUtils.isBlank(mConfig.extraBundledFilesCsv)) {
			return;
		}
		for(String dependency : mConfig.extraBundledFilesCsv.split(",")) {
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
		String graderFileName = FilenameUtils.getName(mConfig.graderFile);
		writeZipEntry(zipWriter, new ZipEntry(graderFileName), new File(mConfig.graderFile));
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
		}
	}
}

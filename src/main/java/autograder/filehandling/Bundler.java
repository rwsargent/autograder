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
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import autograder.Constants;
import autograder.configuration.Configuration;
import autograder.student.Student;
import autograder.student.SubmissionPair;

public class Bundler {
	private static byte[] buffer = new byte[1024];
	private static List<String> validFiles = Arrays.asList("pdf", "rws", "txt");
	
	public static Map<String, File> bundleStudents(HashMap<String, Set<SubmissionPair>> studentToTaMap) {
		HashMap<String, File> taToBigZipMap = new HashMap<>();
		for(String ta: studentToTaMap.keySet()) {
			System.out.println("Bundling " + ta + "'s students");
			File taZipFile = new File(String.format("%s/%s/%s-grading.zip", Constants.ZIPS, ta, Configuration.getConfiguration().assignment));
			taZipFile.getParentFile().mkdirs();
			try(FileOutputStream fout = new FileOutputStream(taZipFile);ZipOutputStream zipWriter = new ZipOutputStream(new BufferedOutputStream(fout))) {
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

	private static String getParentDirectoryName(SubmissionPair pair) {
		if(pair.partner.studentInfo.sortableName.startsWith("invalid")) {
			return pair.submitter.studentInfo.sortableName + "/";
		} else {
			return pair.submitter.studentInfo.sortableName +"--" + pair.partner.studentInfo.sortableName + "/";
		}
	}

	private static void writeExtraFilesToZip(ZipOutputStream zipWriter) throws IOException {
		if(Configuration.getConfiguration().extraBundledFilesCsv == null) {
			return;
		}
		for(String dependency : Configuration.getConfiguration().extraBundledFilesCsv.split(",")) {
			String dependencyFileName = FilenameUtils.getName(dependency);
			File dependencyFile = new File(dependency);
			if(dependencyFile.isDirectory()) {
				for(File file : dependencyFile.listFiles()) {
					writeZip(zipWriter, new ZipEntry(dependencyFileName + "/" + FilenameUtils.getName(file.getName())), file);
				}
			} else {
				writeZip(zipWriter, new ZipEntry(dependencyFileName), dependencyFile);
			}
		}
	}

	private static void writeAssignmentFileToZip(ZipOutputStream zipWriter) throws IOException {
		String graderFileName = FilenameUtils.getName(Configuration.getConfiguration().graderFile);
		writeZip(zipWriter, new ZipEntry(graderFileName), new File(Configuration.getConfiguration().graderFile));
	}

	private static void writeFilesToZip(Student student, ZipOutputStream zipWriter, String parentDirectory) throws IOException {
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
		recursiveWriteToZip(zipWriter, student.studentDirectory, parentDirectory +student.studentInfo.name);
	}

	private static void recursiveWriteToZip(ZipOutputStream zipWriter, File studentRoot, String name) throws IOException {
		for(File file : studentRoot.listFiles()) {
			if(file.isDirectory()) {
				recursiveWriteToZip(zipWriter, file, name);
				continue;
			}
			if(file.getName().endsWith(".java")) {
				writeZip(zipWriter, new ZipEntry(name + "/src/" + file.getName()), file);
			} else if(validFiles.contains(FilenameUtils.getExtension(file.getName()))) {
				writeZip(zipWriter, new ZipEntry(name + "/" + file.getName()), file);
			}
		}
	}
	
	private static void writeZip(ZipOutputStream zipWriter, ZipEntry entry, File file) throws IOException {
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

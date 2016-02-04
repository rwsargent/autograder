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

import org.apache.commons.io.FilenameUtils;

import autograder.Constants;
import autograder.configuration.Configuration;
import autograder.student.Student;
import autograder.student.SubmissionPair;

public class Bundler {
	private static byte[] buffer = new byte[1024];
	private static List<String> validFiles = Arrays.asList("pdf", "rws");
	
	public static Map<String, File> bundleStudents(HashMap<String, Set<SubmissionPair>> studentToTaMap) {
		HashMap<String, File> taToBigZipMap = new HashMap<>();
		for(String ta: studentToTaMap.keySet()) {
			File taZipFile = new File(String.format("%s/%s/%s-grading.zip", Constants.ZIPS, ta, Configuration.getConfiguration().assignment));
			taZipFile.getParentFile().mkdirs();
			try(FileOutputStream fout = new FileOutputStream(taZipFile);ZipOutputStream zipWriter = new ZipOutputStream(new BufferedOutputStream(fout))) {
				for(SubmissionPair pair : studentToTaMap.get(ta)) {
					writeFilesToZip(pair.partner, zipWriter);
					writeFilesToZip(pair.submitter, zipWriter);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			taToBigZipMap.put(ta, taZipFile);
		}
		return taToBigZipMap;
	}

	private static void writeFilesToZip(Student student, ZipOutputStream zipWriter) throws IOException {
		if(student.studentInfo.name.equals("placeholder")) { // skip nonexistant students
			return; 
		}
		zipWriter.putNextEntry(new ZipEntry(student.studentInfo.name));
		zipWriter.closeEntry();
		zipWriter.putNextEntry(new ZipEntry(student.studentInfo.name + "/src/"));
		zipWriter.closeEntry();
		
		recursiveWriteToZip(zipWriter, student.studentDirectory, student.studentInfo.name);
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

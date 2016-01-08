package autograder.mailer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import autograder.Constants;
import autograder.configuration.Configuration;
import autograder.student.SubmissionPair;

public class Bundler {
	
	public List<ZipFile> bundleStudents(HashMap<String, Set<SubmissionPair>> studentToTaMap) {
		for(String ta: studentToTaMap.keySet()) {
			for(SubmissionPair pair : studentToTaMap.get(ta)) {
				File file = new File(String.format("%s/%s/%s-grading.zip", Constants.ZIPS, ta, Configuration.getConfiguration().assignment));
				file.getParentFile().mkdirs();
				File zip = creatZipFile(pair, ta); 
			}
		}
		return null;
	}

	private File creatZipFile(SubmissionPair pair, String ta) {
		String filename = String.format("%s/%s/%s-grading.zip", Constants.ZIPS, ta, Configuration.getConfiguration().assignment);
		File tempFile = new File(filename);
		tempFile.getParentFile().mkdirs();
		try(FileOutputStream fout = new FileOutputStream(tempFile);ZipOutputStream zipWriter = new ZipOutputStream(new BufferedOutputStream(fout))) {
			File studentRoot = new File(pair.partner.getSourceDirectoryPath());
			recursiveWriteToZip(zipWriter, studentRoot);
			
		} catch (IOException e) {
			
		}
		return null;
	}

	private void recursiveWriteToZip(ZipOutputStream zipWriter, File studentRoot) {
		for(File file : studentRoot.listFiles()) {
			if(file.isDirectory()) {
				recursiveWriteToZip(zipWriter, file);
				continue;
			}
		}
	}
}

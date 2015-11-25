package autograder.filehandling;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import autograder.configuration.ConfigurationException;


public class ZipReader {

	public void unzipSubmissions(String filePath) {
		ZipFile submissionZip;
		try {
			submissionZip = new ZipFile(filePath);
		} catch (IOException e) {
			throw new ConfigurationException("Could not find the submission file. " + e.getMessage());
		}
		Enumeration<? extends ZipEntry> entries = submissionZip.entries();

	    while(entries.hasMoreElements()){
	        ZipEntry entry = entries.nextElement();
	        String extension = getExtension(entry.getName());
	        if(entry.isDirectory()) {
	        	
	        }
	        if(!extension.contains("java")) {
	        	continue;
	        }
	        
	        InputStream stream = submissionZip.getInputStream(entry);
	    } 
	}
	
	private String getExtension(String fileName) {
		int lastDotIndex = fileName.lastIndexOf('.');
		return fileName.substring(lastDotIndex);
	}
}

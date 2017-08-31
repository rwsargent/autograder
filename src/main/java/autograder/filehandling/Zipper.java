package autograder.filehandling;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Zipper implements Closeable, AutoCloseable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Zipper.class);
	private FileOutputStream fout;
	private ZipOutputStream zipWriter;
	byte[] buffer;

	public void init(File destination) throws IOException { 
		fout = new FileOutputStream(destination);
		zipWriter = new ZipOutputStream(fout);
		buffer = new byte[1024];
	}
	
	public void addEntry(String entryName, File data) throws IOException {
		ZipEntry newEntry = new ZipEntry(entryName);
		try(FileInputStream in = new FileInputStream(data.getAbsolutePath())) {
			zipWriter.putNextEntry(newEntry);
			int len;
			while ((len = in.read(buffer)) > 0) {
				zipWriter.write(buffer, 0, len);
			}
			zipWriter.closeEntry();
		} catch (ZipException e) {
			if(e.getMessage().contains("uplicate")) {
				//if there's a duplicate entry, I'm ok with it.
				LOGGER.warn("Duplicate entry while zipping " + entryName);
			}
		}
	}

	@Override
	public void close() throws IOException {
		fout.flush();
		zipWriter.flush();
		zipWriter.close();
	}

	public void zipDirectory(File assignmentRootDir, Predicate<String> filenameFilter) throws IOException {
		List<File> files = listFiles(new ArrayList<>(), assignmentRootDir, filenameFilter);
		createZipFile(files, assignmentRootDir);
	}
	
	public void zipDirRecur(String level, File entry, Predicate<String> filenameFilter) throws IOException {
		String entryname = level + "/" + entry.getName();
		if(entry.isDirectory()) {
			ZipEntry newEntry = new ZipEntry(entryname);
			zipWriter.putNextEntry(newEntry);
			zipWriter.closeEntry();
			for(File file : entry.listFiles()) {
				zipDirRecur(entryname, file, filenameFilter);
			}
		} else {
			if(filenameFilter != null) {
				if(filenameFilter.test(entry.getName())) {
					addEntry(entryname, entry);
				}
			} else {
				addEntry(entryname, entry);
			}
		}
	}
	
	private List<File> listFiles(List<File> listFiles, File inputDirectory, Predicate<String> filenameFilter) throws IOException {
        File[] allFiles = inputDirectory.listFiles();
        for (File file : allFiles) {
            if (file.isDirectory()) {
                listFiles(listFiles, file, filenameFilter);
            } else {
                listFiles.add(file);
            }
        }
        return listFiles;
    }
	
	private void createZipFile(List<File> listFiles, File inputDirectory) throws IOException {
		for (File file : listFiles) {
			String filePath = file.getCanonicalPath();
			int lengthDirectoryPath = inputDirectory.getCanonicalPath().length();
			int lengthFilePath = file.getCanonicalPath().length();

			// Get path of files relative to input directory.
			String zipFilePath = filePath.substring(lengthDirectoryPath + 1, lengthFilePath);
			addEntry(zipFilePath, file);
		}
	}
}

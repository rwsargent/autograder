package autograder.filehandling;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Zipper implements AutoCloseable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Zipper.class);
	private FileOutputStream fout;
	private ZipOutputStream zipWriter;
	byte[] buffer;
	
	public Zipper(File destination) throws FileNotFoundException {
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
	public void close() throws Exception {
		fout.flush();
		zipWriter.flush();
		zipWriter.close();
	}
}

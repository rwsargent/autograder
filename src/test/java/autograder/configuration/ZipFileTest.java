package autograder.configuration;

import java.io.File;
import java.util.List;

import org.junit.Test;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

public class ZipFileTest {

	@Test
	public void testzip4j() {
		try {
			ZipFile zip = new net.lingala.zip4j.core.ZipFile(new File("src/test/resources/submissions.zip"));
			List<FileHeader> fileHeaders = zip.getFileHeaders();
			for(FileHeader header : fileHeaders) {
				if (header.getFileName().endsWith(".zip")) {
					
				}
			}
		} catch (ZipException e) {
		}
	}
}	

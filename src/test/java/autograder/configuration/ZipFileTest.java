package autograder.configuration;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.Test;

public class ZipFileTest {

	@Test
	public void test() {
		try {
			@SuppressWarnings("resource")
			ZipFile zip = new ZipFile(new File("src/test/resources/submissions.zip"));
			Enumeration<? extends ZipEntry> entries = zip.entries();
			while(entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				System.out.println(entry.getName());
			}
					
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}

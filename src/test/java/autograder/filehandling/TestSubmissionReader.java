package autograder.filehandling;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import autograder.Constants;

public class TestSubmissionReader {

	@Before
	public void setup() {
		File subDir = new File(Constants.SUBMISSIONS);
		if(subDir.exists()) {
			try {
				FileUtils.cleanDirectory(subDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			subDir.mkdirs();
		}
	}
	
	@AfterClass
	public void cleanUp() {
		File subDir = new File(Constants.SUBMISSIONS);
		if(subDir.exists()) {
			try {
				FileUtils.deleteDirectory(subDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Test
	public void test() {
		SubmissionReader sr = new SubmissionReader();
		String filePath = getClass().getClassLoader().getResource("submissions.zip").getPath();
		sr.unzipSubmissions(filePath);
	}

}

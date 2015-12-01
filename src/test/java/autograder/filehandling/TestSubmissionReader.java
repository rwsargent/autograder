package autograder.filehandling;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import autograder.Constants;

public class TestSubmissionReader {

	@BeforeClass
	public static void init() {
		System.setProperty("sun.zip.disableMemoryMapping", "true");
	}
	
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
	public static void cleanUp() {
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
	public void testFull() {
		runTest("submissions.zip");
	}
	
	@Test
	public void testHalf() {
		runTest("half_submissions.zip");
	}
	
	@Test
	public void testTwo() {
		runTest("only_two.zip");
	}
	
	@Test
	public void testA_through_D() {
		runTest("A_through_D.zip");
	}

	private void runTest(String fileName) {
		SubmissionReader sr = new SubmissionReader();
		String filePath = getClass().getClassLoader().getResource(fileName).getPath();
		sr.unzipSubmissions(filePath);
	}
}

package autograder.filehandling;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
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
	
//	@AfterClass
//	public static void cleanUp() {
//		File subDir = new File(Constants.SUBMISSIONS);
//		if(subDir.exists()) {
//			try {
//				FileUtils.deleteDirectory(subDir);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//	}
	
	@Test
	public void testCustom() {
		runTest("custom_submissions.zip");
	}
	
	@Test
	public void testFull() {
		runTest("submissions.zip");
	}
	
	@Test
	@Ignore
	public void testHalf() {
		runTest("half_submissions.zip");
	}
	
	@Test
	@Ignore
	public void testTwo() {
		runTest("only_two.zip");
	}
	
	@Test
	@Ignore
	public void testA_through_D() {
		runTest("A_through_D.zip");
	}

	private void runTest(String fileName) {
		SubmissionReader sr = new SubmissionReader();
		String filePath = getClass().getClassLoader().getResource(fileName).getPath();
		sr.unzipSubmissions(filePath);
	}
}

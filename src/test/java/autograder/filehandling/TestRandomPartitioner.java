package autograder.filehandling;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import autograder.configuration.Configuration;
import autograder.student.AutograderSubmission;
import autograder.student.AutograderSubmissionMap;
import autograder.tas.TAInfo;
import autograder.testutils.TestUtils;

public class TestRandomPartitioner {

	RandomizedPartitioner partitioner;
	private Configuration loadConfiguration;
	private Map<String, TAInfo> tas;
	@Before
	public void setup(){
		loadConfiguration = TestUtils.loadConfiguration();
		tas = readTAFile();
		
		partitioner = new RandomizedPartitioner(tas, loadConfiguration);
	}
	
	@Test
	public void evenSplit() {
		AutograderSubmissionMap submissions = TestUtils.fillMapWithMocks(20);
		Map<String, List<AutograderSubmission>> result = partitioner.partition(submissions);
		assertEquals(2, result.size());
		for(Entry<String, List<AutograderSubmission>> entry : result.entrySet()) {
			assertEquals(10, entry.getValue().size());
		}
	}
	
	@Test
	public void unevenSplit() {
		AutograderSubmissionMap submissions = TestUtils.fillMapWithMocks(21);
		tas.get("ta1").hours = 1;
		tas.get("ta2").hours = 2;
		
		Map<String, List<AutograderSubmission>> result = partitioner.partition(submissions);
		assertEquals(7, result.get(tas.get("ta1").email).size());
		assertEquals(14, result.get(tas.get("ta2").email).size());
		
	}
	
	@Test
	public void roundRobinPartition() {
		AutograderSubmissionMap submissions = TestUtils.fillMapWithMocks(23);
		tas.get("ta1").hours = 1;
		tas.get("ta2").hours = 2;
		
		Map<String, List<AutograderSubmission>> result = partitioner.partition(submissions);
		assertEquals(23, result.get(tas.get("ta1").email).size() + result.get(tas.get("ta2").email).size());
	}
	
	@Test
	public void randomized() {
		AutograderSubmissionMap submissions = TestUtils.fillMapWithMocks(20);
		
		Map<String, List<AutograderSubmission>> result = partitioner.partition(submissions);
		assertFalse(isSorted(result.get(tas.get("ta1").email)));
		assertFalse(isSorted(result.get(tas.get("ta2").email)));
	}
	
	private boolean isSorted(List<AutograderSubmission> list) {
		AutograderSubmission prev = list.get(0);
		for(AutograderSubmission submission : list) {
			if (submission.getDirectory().compareTo(prev.getDirectory()) < 0) {
				return false;
			}
			prev = submission;
		}
		return true;
	}

	private Map<String, TAInfo> readTAFile() {
		Gson gson = new Gson();
		try(InputStream file = getClass().getClassLoader().getResourceAsStream("testTa.json");
				InputStreamReader reader = new InputStreamReader(file)) {
			TypeToken<Map<String, TAInfo>> type = new TypeToken<Map<String, TAInfo>>(){};
			return gson.fromJson(reader, type.getType());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}

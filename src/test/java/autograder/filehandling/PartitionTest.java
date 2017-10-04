package autograder.filehandling;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import autograder.tas.TAInfo;

public class PartitionTest {

	@Test
	public void testGsonReadInFile() {
		Gson gson = new Gson();
		try(InputStream file = getClass().getClassLoader().getResourceAsStream("testTa.json");
				InputStreamReader reader = new InputStreamReader(file)) {
			TypeToken<Map<String, TAInfo>> type = new TypeToken<Map<String, TAInfo>>(){};
			Map<String, TAInfo> info = gson.fromJson(reader, type.getType());
			System.out.println(info.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
	}
}

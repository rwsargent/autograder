package autograder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import autograder.configuration.Configuration;

public class AutograderSaver {
	public static final String AUTOGRADER_SAVE_FILENAME = "saved.autograder";
	
	private Configuration mConfig;
	
	private String assignment;
	
	public AutograderSaver(Configuration configuration) {
		mConfig = configuration;
	}
	
	public void readFile(File saved) {
		try(FileReader fr = new FileReader(saved);
				BufferedReader br = new BufferedReader(fr)) {
			assignment = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getAssignment() {
		if (assignment == null) {
			throw new IllegalStateException("You need to read in a save file first!");
		}
		return assignment;
	}

	public boolean isCurrentAssignemnt() {
		return mConfig.assignment.equals(assignment);
	}

	public void writeSavedFile(File outfile) {
		try(FileWriter fw = new FileWriter(outfile);
				BufferedWriter bf = new BufferedWriter(fw)) {
			fw.write(mConfig.assignment);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}


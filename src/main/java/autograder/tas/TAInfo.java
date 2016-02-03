package autograder.tas;

import java.io.File;
import java.util.List;

public class TAInfo {
	public String name;
	public String email;
	public double hours;
	public int assignmentsToGrade;
	public List<String> students;
	
	public File taDirectory;

	public TAInfo(String name, String email, double hours) {
		this.name = name;
		this.email = email;
		this.hours = hours;
	}
	
	public double getHours() {
		return this.hours;
	}
}
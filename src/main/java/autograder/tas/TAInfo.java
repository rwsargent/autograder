package autograder.tas;

import java.util.List;

/**
 * POJO for TA csv file
 * @author Ryan
 *
 */
public class TAInfo {
	public String name;
	public String email;
	public double hours;
	public int assignmentsToGrade;
	public List<String> students;
	
	public TAInfo(String name, String email, double hours) {
		this.name = name;
		this.email = email;
		this.hours = hours;
	}
}
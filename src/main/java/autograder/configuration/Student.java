package autograder.configuration;

import java.io.File;

public class Student {
	public File studentDirectory;
	public String name;
	public int canvasId;
	public boolean isLate;

	public Student() {
	}
	
	public Student(String name, int canvasId) {
		this.canvasId = canvasId;
		studentDirectory = new File(String.format("submissions/%s_%d", name, canvasId));
		studentDirectory.mkdirs();
		new File(studentDirectory.getAbsolutePath() + "/source").mkdir();
	}
	
	public Student(String name, int canvasId, boolean late) {
		this(name, canvasId);
		this.isLate = late;
	}
	
	public String getSourceDirectoryPath() {
		return studentDirectory.getAbsolutePath() + "/source";
	}
	
	public String toString() {
		return name;
	}
}
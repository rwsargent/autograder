package autograder.configuration;

public @interface PropertyType {
	public enum DataType{ 
		BOOLEAN, STRING, INT, NUMBER;
	}
	
	DataType type() default DataType.STRING;
}

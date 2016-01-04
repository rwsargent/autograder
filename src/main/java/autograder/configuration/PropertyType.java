package autograder.configuration;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface PropertyType {
	public enum DataType{ 
		BOOLEAN, STRING, INT, NUMBER;
	}
	
	DataType type() default DataType.STRING;
}

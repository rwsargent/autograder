package autograder.configuration;

import java.util.Properties;

import autograder.configuration.PropertyType.DataType;

/**
 * The assignment.properties file that students submit help keep track of who has submitted the code, as well has who has partnered with whom.
 * @author Ryan
 *
 */
public class AssignmentProperties extends AbstractProperties {
	
	public String name,uid;
	
	@Optional(defaultValue = "default")
	public String partner_name;
	@Optional(defaultValue = "-1")
	public String partner_uid;
	
	@PropertyType(type = DataType.BOOLEAN)
	public boolean submitted;
	
	public AssignmentProperties(String filePath) {
		mapProperties(filePath);	
	}
	
	public AssignmentProperties(Properties propmMap) {
		fillProperties(propmMap, this);
	}

	@Override
	protected String getDefaultPropertiesLocation() {
		return "";
	}
}

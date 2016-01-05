package autograder.configuration;

import autograder.configuration.PropertyType.DataType;

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

	@Override
	protected String getDefaultPropertiesLocation() {
		return "";
	}
}

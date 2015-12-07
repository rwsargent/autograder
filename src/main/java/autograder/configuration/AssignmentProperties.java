package autograder.configuration;

import autograder.configuration.PropertyType.DataType;

public class AssignmentProperties extends AbstractProperties {
	
	public String name,uid;
	public String partner_name, partner_uid;
	
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

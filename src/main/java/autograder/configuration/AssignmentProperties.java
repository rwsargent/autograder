package autograder.configuration;

public class AssignmentProperties extends AbstractProperties {
	
	public String name,uid;
	public String partner_name, partner_uid;
	
	@IsBoolean
	public boolean submitted;
	
	public AssignmentProperties(String filePath) {
		mapProperties(filePath);	
	}

	@Override
	protected String getDefaultPropertiesLocation() {
		return "";
	}
}

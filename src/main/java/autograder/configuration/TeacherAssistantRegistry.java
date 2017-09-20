package autograder.configuration;

import java.util.Map;

import autograder.tas.TAInfo;

/**
 * This class parses the csv file that is found at the specified location in Configuration,
 * and keeps track of the TAs, their hours, and their email. 
 * @author Ryan
 *
 */
public class TeacherAssistantRegistry {
	Map<String, TAInfo> tas;
}

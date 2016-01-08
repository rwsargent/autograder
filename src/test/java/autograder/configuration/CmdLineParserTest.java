package autograder.configuration;

import static org.junit.Assert.*;

import org.apache.commons.cli.CommandLine;
import org.junit.Test;

public class CmdLineParserTest {

	private CmdLineParser clParser = new CmdLineParser();
	
	@Test
	public void testHelp() {
		CommandLine cl = clParser.parse("-h -t something".split(" "));
		assertTrue((cl.hasOption("h")));
	}
	
}

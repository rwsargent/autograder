package autograder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import autograder.configuration.Configuration;
import autograder.phases.PhaseOne;
import autograder.phases.PhaseThree;
import autograder.phases.PhaseTwo;
import autograder.student.AutograderSubmissionMap;

public class Autograder {
	
	public static Logger LOGGER = LoggerFactory.getLogger(Autograder.class);
	private Configuration config;
	
	private PhaseOne phaseOne;
	private PhaseTwo phaseTwo;
	private PhaseThree phaseThree;
	
	@Inject
	public Autograder(Configuration configuration, PhaseOne phaseOne, PhaseTwo phaseTwo, PhaseThree phaseThree) {
		this.phaseOne = phaseOne;
		this.config = configuration;
		this.phaseTwo = phaseTwo;
		this.phaseThree = phaseThree;
	}
	
	public void execute() {
		LOGGER.info("Starting autograder run");
		AutograderSubmissionMap studentMap = phaseOne.setupSubmissions(config.assignment, config.rerun);
		try {
			this.phaseTwo.phaseTwo(studentMap);
		} catch (InterruptedException e) {
			LOGGER.error("Phase two was interrupted", e);
			//Mail me?
			return; 
		}
		this.phaseThree.wrapItUp(studentMap);
		LOGGER.info("Finished!");
	}
}

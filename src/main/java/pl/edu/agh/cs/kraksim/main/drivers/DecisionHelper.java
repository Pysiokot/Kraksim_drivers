/**
 *
 */
package pl.edu.agh.cs.kraksim.main.drivers;

import org.apache.log4j.Logger;

import java.util.Random;

public class DecisionHelper {
	private static final Logger LOGGER = Logger.getLogger(DecisionHelper.class);
	private final Random randomGenerator;
	private final int threshold;

	// TODO:
	public DecisionHelper(Random randomGen, int threashold) {
		randomGenerator = randomGen;
		threshold = threashold;
	}

	public boolean decide() {
		boolean decision = randomGenerator.nextInt(100) < threshold;
		LOGGER.trace(decision);

		return decision;
	}
}

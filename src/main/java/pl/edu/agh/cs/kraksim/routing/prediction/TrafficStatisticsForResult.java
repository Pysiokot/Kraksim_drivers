package pl.edu.agh.cs.kraksim.routing.prediction;

import java.util.HashMap;
import java.util.Map;

public class TrafficStatisticsForResult {
	private final Map<String, Double> levelOccurances;

	public TrafficStatisticsForResult() {
		levelOccurances = new HashMap<>();
	}

	public void incrementCounterForLevel(TrafficLevel level) {
		if (levelOccurances.containsKey(level.toString())) {
			Double counter = levelOccurances.get(level.toString());
			levelOccurances.remove(level.toString());
			levelOccurances.put(level.toString(), counter + 1);
		} else {
			levelOccurances.put(level.toString(), 1.0);
		}
	}

	public double getCounterForLevel(TrafficLevel level) {
		Double counter = levelOccurances.get(level.toString());
		if (counter == null) {
			return 0;
		} else {
			return counter;
		}
	}

	public double getProbabilityForLevel(TrafficLevel level) {
		double levelCount = getCounterForLevel(level);
		int sum = 0;
		for (Double value : levelOccurances.values()) {
			sum += value;
		}
		return levelCount / sum;
	}

	public String getNameOfMostFrequentLevel() {
		String result = null;
		double maxOccurences = -1;
		for (String name : levelOccurances.keySet()) {
			double temp = levelOccurances.get(name);
			if (temp > maxOccurences) {
				result = name;
				maxOccurences = temp;
			}
		}
		return result;
	}

	/**
	 * Performs the ageing process on traffic levels
	 *
	 * @param ageingRate rate with ageing shall happen of range (0, 1]
	 */
	public void ageResults(double ageingRate) {
		if (ageingRate == 1.0) {
			return;
		}
		for (String name : levelOccurances.keySet()) {
			Double temp = levelOccurances.get(name);
			temp *= ageingRate;
			levelOccurances.put(name, temp);
		}
	}
}

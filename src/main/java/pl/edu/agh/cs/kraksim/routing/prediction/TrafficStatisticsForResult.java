package pl.edu.agh.cs.kraksim.routing.prediction;

import java.util.HashMap;
import java.util.Map;

public class TrafficStatisticsForResult {
	private final Map<String, Double> levelOccurrences;

	public TrafficStatisticsForResult() {
		levelOccurrences = new HashMap<>();
	}

	public void incrementCounterForLevel(TrafficLevel level) {
		if (levelOccurrences.containsKey(level.toString())) {
			Double counter = levelOccurrences.get(level.toString());
			levelOccurrences.remove(level.toString());
			levelOccurrences.put(level.toString(), counter + 1);
		} else {
			levelOccurrences.put(level.toString(), 1.0);
		}
	}

	public double getCounterForLevel(TrafficLevel level) {
		Double counter = levelOccurrences.get(level.toString());
		if (counter == null) {
			return 0;
		} else {
			return counter;
		}
	}

	public double getProbabilityForLevel(TrafficLevel level) {
		double levelCount = getCounterForLevel(level);
		int sum = 0;
		for (Double value : levelOccurrences.values()) {
			sum += value;
		}
		return levelCount / sum;
	}

	public String getNameOfMostFrequentLevel() {
		String result = null;
		double maxOccurrences = -1;
		for (String name : levelOccurrences.keySet()) {
			double temp = levelOccurrences.get(name);
			if (temp > maxOccurrences) {
				result = name;
				maxOccurrences = temp;
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
		for (String name : levelOccurrences.keySet()) {
			Double temp = levelOccurrences.get(name);
			temp *= ageingRate;
			levelOccurrences.put(name, temp);
		}
	}
}

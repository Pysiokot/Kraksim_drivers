package pl.edu.agh.cs.kraksim.weka.statistics;

import java.util.*;

public class DurationLevelArchive implements Iterable<Integer> {
	private final List<Integer> turnList = new ArrayList<>();
	private final Map<Integer, List<Double>> congestionList = new HashMap<>();

	public void storeStatistics(int turn, double[] durationLevelTable) {
		turnList.add(0, turn);
		List<Double> onePeriodList = new ArrayList<>();
		for (double durationLevel : durationLevelTable) {
			onePeriodList.add(durationLevel);
		}
		congestionList.put(turn, onePeriodList);
	}

	public Double getCongestionByTimeDistance(int timeDistance, int linkNumber) {
		int turn = turnList.get(timeDistance);
		return congestionList.get(turn).get(linkNumber);
	}

	@Override
	public Iterator<Integer> iterator() {
		return turnList.iterator();
	}

	@Override
	public String toString() {
		StringBuilder text = new StringBuilder();
		for (Integer turn : turnList) {
			text.append(turn).append(", ");
			for (double congestion : congestionList.get(turn)) {
				text.append(congestion).append(", ");
			}
			text.append('\n');
		}
		return text.toString();
	}
}

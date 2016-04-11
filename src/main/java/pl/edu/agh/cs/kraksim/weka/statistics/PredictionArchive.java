package pl.edu.agh.cs.kraksim.weka.statistics;

import pl.edu.agh.cs.kraksim.weka.data.LinkInfo;

import java.util.*;

public class PredictionArchive implements Iterable<Integer> {
	private final List<Integer> turnList = new ArrayList<>();
	private final Map<Integer, Set<LinkInfo>> predictingCongestionLinksMap = new HashMap<>();

	public void storePrediction(int turn, Set<LinkInfo> predictedLinks) {
		turnList.add(0, turn);
		predictingCongestionLinksMap.put(turn, predictedLinks);
	}

	@Override
	public Iterator<Integer> iterator() {
		return turnList.iterator();
	}

	public Set<LinkInfo> getDurationListByTurn(int turn) {
		return predictingCongestionLinksMap.get(turn);
	}

	@Override
	public String toString() {
		StringBuilder text = new StringBuilder();
		for (Integer turn : turnList) {
			text.append(turn).append(", ");
			for (LinkInfo pred : predictingCongestionLinksMap.get(turn)) {
				text.append(pred.linkId).append(", ");
			}
			text.append('\n');
		}
		return text.toString();
	}

	public void clear() {
		turnList.clear();
		predictingCongestionLinksMap.clear();
	}
}

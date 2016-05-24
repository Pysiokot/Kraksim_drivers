package pl.edu.agh.cs.kraksim.weka.statistics;

import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.weka.data.LinkInfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CurrentPredictionContainer {
	private Map<Integer, Set<LinkInfo>> predictionMap = new HashMap<>();

	public void addPrediction(LinkInfo linkInfo, int congestionTimePrediction) {
		Set<LinkInfo> congestionLinks = predictionMap.get(congestionTimePrediction);
		if (congestionLinks == null) {
			congestionLinks = new HashSet<>();
			predictionMap.put(congestionTimePrediction, congestionLinks);
		}
		congestionLinks.add(linkInfo);
	}

	public Set<LinkInfo> getPredictionForCurrentPeriod() {
		Set<LinkInfo> congestionLinks = predictionMap.get(0);
		if (congestionLinks == null) {
			congestionLinks = new HashSet<>();
		}
		return congestionLinks;
	}

	public void nextPeriod() {
		Map<Integer, Set<LinkInfo>> newPredictionMap = new HashMap<>();
		for (Map.Entry<Integer, Set<LinkInfo>> predictionMapEntry : predictionMap.entrySet()) {
			if (predictionMapEntry.getKey() != 0) {
				newPredictionMap.put(predictionMapEntry.getKey() - 1, predictionMapEntry.getValue());
			}
		}
		predictionMap = newPredictionMap;
	}

	public boolean willAppearTrafficJam(Link link) {
		for (Integer timePrediction : predictionMap.keySet()) {
			Set<LinkInfo> congestionLinks = predictionMap.get(timePrediction);
			LinkInfo testedLink = new LinkInfo(link.getLinkNumber(), "", -1);
			if (congestionLinks.contains(testedLink)) {
				return true;
			}
		}
		return false;
	}
}

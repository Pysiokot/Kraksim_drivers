package pl.edu.agh.cs.kraksim.weka.timeSeries;

import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.weka.PredictionSetup;
import pl.edu.agh.cs.kraksim.weka.WekaPredictor;
import pl.edu.agh.cs.kraksim.weka.data.AssociatedWorldState;
import pl.edu.agh.cs.kraksim.weka.data.History;
import pl.edu.agh.cs.kraksim.weka.data.IntersectionInfo;
import pl.edu.agh.cs.kraksim.weka.data.LinkInfo;
import pl.edu.agh.cs.kraksim.weka.utils.AbstractMovingAverage;
import pl.edu.agh.cs.kraksim.weka.utils.Neighbours;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TimeSeriesPredictor implements WekaPredictor {
	private static final Logger LOGGER = Logger.getLogger(TimeSeriesPredictor.class);

	private final PredictionSetup setup;
	private Map<LinkInfo, ClassifiersInfo> linkClassifiers = new HashMap<>();
	private Map<IntersectionInfo, ClassifiersInfo> intersectionClassifiers = new HashMap<>();
	private final TimeSeriesStatistics statistics;
	private final TimeSeriesTransactionCreator transactionCreator;
	private final History history;

	public TimeSeriesPredictor(PredictionSetup setup) {
		this.setup = setup;
		transactionCreator = new TimeSeriesTransactionCreator(setup);
		statistics = new TimeSeriesStatistics(setup, this, transactionCreator);
		history = new History(setup.getNeighbourArray().keySet(), setup.getIntersectionNeighbours().keySet());
	}

	@Override
	public void addWorldState(int turn, AssociatedWorldState worldState) {
		AbstractMovingAverage movingAverage = setup.getMovingAverage();
		worldState = movingAverage.computeAverage(worldState);
		history.add(turn, worldState);
		statistics.addStatistics(turn, worldState);
	}

	@Override
	public void createClassifiers() {
		generateClassifierMap();
		history.clear();
	}

	@Override
	public void predictCongestions(int turn) {
		statistics.predictCongestions(turn);
	}

	@Override
	public void makeEvaluation(int turn) {
		Set<LinkInfo> predictableLinks = getPredictableLinks();
		statistics.computePartialResult(predictableLinks, turn);
	}

	private Set<LinkInfo> getPredictableLinks() {
		Set<LinkInfo> predictable = new HashSet<>();
		for (LinkInfo linkInfo : linkClassifiers.keySet()) {
			if (linkClassifiers.get(linkInfo) != null) {
				predictable.add(linkInfo);
			}
		}
		return predictable;
	}

	private void generateClassifierMap() {
		TimeSeriesTrainer creator = new TimeSeriesTrainer(setup, transactionCreator);
		LOGGER.debug("Create road classifiers");
		linkClassifiers = new HashMap<>();
		Map<LinkInfo, Neighbours> neighboursArray = setup.getNeighbourArray();
		for (LinkInfo linkInfo : neighboursArray.keySet()) {
			ClassifiersInfo classifiersInfo;
			if (setup.getPredictionSize() > 0) {
				classifiersInfo = creator.generateClassifiers(history, linkInfo);
			} else {
				classifiersInfo = creator.generateOnlyClassDataClassifier(history, linkInfo);
			}
			linkClassifiers.put(linkInfo, classifiersInfo);
		}

		if (setup.getPredictionSize() > 0) {
			LOGGER.debug("Create intersection classifiers");
			intersectionClassifiers = new HashMap<>();
			Map<IntersectionInfo, Neighbours> intersectionNeighbours = setup.getIntersectionNeighbours();
			for (IntersectionInfo intersectionInfo : intersectionNeighbours.keySet()) {
				ClassifiersInfo classifiersInfo = creator.generateClassifiers(history, intersectionInfo);
				intersectionClassifiers.put(intersectionInfo, classifiersInfo);
			}
		}
	}

	@Override
	public boolean willAppearTrafficJam(Link link) {
		return statistics.willAppearTrafficJam(link);
	}

	public Map<LinkInfo, ClassifiersInfo> getClassifierMap() {
		return linkClassifiers;
	}

	public Map<IntersectionInfo, ClassifiersInfo> getIntersectionClassifiers() {
		return intersectionClassifiers;
	}

	public long getFalseNegativeCongestions() {
		return statistics.getFalseNegativeCongestions();
	}

	public long getTotalItemsAmount() {
		return statistics.getTotalItemsAmount();
	}

	public long getFalsePositiveCongestions() {
		return statistics.getFalsePositiveCongestions();
	}

	public long getTotalCongestionsAmount() {
		return statistics.getTotalCongestionsAmount();
	}

	public long getTruePositiveCongestions() {
		return statistics.getTruePositiveCongestions();
	}
}

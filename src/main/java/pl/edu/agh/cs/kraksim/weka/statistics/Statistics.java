package pl.edu.agh.cs.kraksim.weka.statistics;

import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.weka.PredictionSetup;
import pl.edu.agh.cs.kraksim.weka.data.AssociatedWorldState;
import pl.edu.agh.cs.kraksim.weka.data.History;
import pl.edu.agh.cs.kraksim.weka.data.LinkInfo;
import pl.edu.agh.cs.kraksim.weka.utils.Discretiser;

import java.util.Set;

public abstract class Statistics {
	private static final Logger LOGGER = Logger.getLogger(Statistics.class);
	private final PredictionArchive predictionsArchive;
	private final ResultCreator resultCreator;
	private final ResultWriter resultWriter;
	private final ErrorResultCreator errorResultCreator;
	protected PredictionSetup setup;
	protected Discretiser discretiser;
	protected Archive<Boolean> congestionsArchive;
	protected Archive<Double> classDataArchive;
	protected Archive<Double> classDataPredictionArchive;
	protected CurrentPredictionContainer currentPredictionContainer;
	protected History historyArchive;

	protected Statistics(PredictionSetup setup) {
		this.setup = setup;
		discretiser = setup.getDiscretiser();
		predictionsArchive = new PredictionArchive();
		congestionsArchive = new Archive<>();
		classDataArchive = new Archive<>();
		classDataPredictionArchive = new Archive<>();
		historyArchive = new History(setup.getNeighbourArray().keySet(), setup.getIntersectionNeighbours().keySet());
		currentPredictionContainer = new CurrentPredictionContainer();
		resultCreator = new ResultCreator(setup, congestionsArchive, predictionsArchive);
		resultWriter = new ResultWriter(setup, congestionsArchive, predictionsArchive);
		errorResultCreator = new ErrorResultCreator(setup, classDataArchive, classDataPredictionArchive);
	}

	public void predictCongestions(int turn) {
		if (turn >= setup.getTimeSeriesUpdatePeriod()) {
			predict(turn);
		}
		if (turn == setup.getTimeSeriesUpdatePeriod()) {
			currentPredictionContainer.nextPeriod();
		}
		if (turn > setup.getTimeSeriesUpdatePeriod()) {
			storePredictionInArchive(turn);
		}
	}

	public void addStatistics(int turn, AssociatedWorldState worldState) {
		if (turn > setup.getTimeSeriesUpdatePeriod() - 5000) {
			add(turn, worldState);
		}
	}

	public void computePartialResult(Set<LinkInfo> predictableLinks, int turn) {
		if (turn > setup.getTimeSeriesUpdatePeriod()) {
			errorResultCreator.computePartialResults(turn);
			resultCreator.computePartialResults(predictableLinks);
		}
		if (turn >= setup.getStatisticsDumpTime() && turn < setup.getStatisticsDumpTime() + setup.getWorldStateUpdatePeriod()) {
			LOGGER.error("Turn: " + turn);
			String result = resultCreator.getResultText();
			result += errorResultCreator.getResultText(turn);
			LOGGER.debug("Write result to file");
			resultWriter.writeResult(result);
		}
	}

	public abstract void predict(int turn);

	public abstract void add(int turn, AssociatedWorldState worldState);

	void storePredictionInArchive(int turn) {
		Set<LinkInfo> predictedLinks = currentPredictionContainer.getPreditionForCurrentPeriod();
		currentPredictionContainer.nextPeriod();
		predictionsArchive.storePrediction(turn, predictedLinks);
	}

	public long getFalseNegativeCongestions() {
		return resultCreator.getFalseNegativeCongestions();
	}

	public long getTotalItemsAmount() {
		return resultCreator.getTotalItemsAmount();
	}

	public long getFalsePositiveCongestions() {
		return resultCreator.getFalsePositiveCongestions();
	}

	public long getTotalCongestionsAmount() {
		return resultCreator.getTotalCongestionsAmount();
	}

	public long getTruePositiveCongestions() {
		return resultCreator.getTruePositiveCongestions();
	}
}

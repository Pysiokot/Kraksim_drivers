package pl.edu.agh.cs.kraksim.weka;

import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.core.City;
import pl.edu.agh.cs.kraksim.core.Lane;
import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.iface.Clock;
import pl.edu.agh.cs.kraksim.iface.carinfo.CarInfoIView;
import pl.edu.agh.cs.kraksim.iface.eval.EvalIView;
import pl.edu.agh.cs.kraksim.ministat.MiniStatEView;
import pl.edu.agh.cs.kraksim.simpledecision.SimpleDecisionEView;
import pl.edu.agh.cs.kraksim.weka.data.AssociatedWorldState;

public class WekaPredictionModule {
	private static final Logger LOGGER = Logger.getLogger(WekaPredictionModule.class);
	private final Clock clock;

	private long worldStateLastUpdate = -1;
	private long predictionLastUpdate = -1;
	private long classifiersLastUpdate = 0;
	private long evaluationLastUpdate = 0;

	private final PredictionSetup setup;
	private final WekaPredictor predictor;

	private final double[] lastPeriodAvgDurationTable;
	private final DataPicker dataPicker;

	private final double timeTableMultiplier;
	private final double evaluationMultiplier;

	public WekaPredictionModule(City city, MiniStatEView statView, CarInfoIView carInfoView, Clock clock) {
		this.clock = clock;
		dataPicker = new DataPicker(city, clock, statView, carInfoView);
		setup = new PredictionSetup(city);
		predictor = setup.getPredictor();
		lastPeriodAvgDurationTable = new double[city.linkCount()];
		timeTableMultiplier = setup.getTimeTableMultiplier();
		evaluationMultiplier = setup.getEvaluationMultiplier();
	}

	public void turnEnded() {
		int turn = clock.getTurn();
		if (needAddWorldState()) {
			LOGGER.debug("Add world State");
			dataPicker.refreshDurationTable(lastPeriodAvgDurationTable);
			AssociatedWorldState associatedWorldState = dataPicker.createWorldState();
			predictor.addWorldState(turn, associatedWorldState);
		}
		if (needClassifiers()) {
			LOGGER.debug("Create classifiers");
			predictor.createClassifiers();
		}
		if (needPrediction()) {
			LOGGER.debug("Predict congestiosn");
			predictor.predictCongestions(turn);
		}
		if (needEvaluation()) {
			LOGGER.debug("Make evaluation");
			predictor.makeEvaluation(turn);
		}
	}

	private boolean needEvaluation() {
		boolean refresh = false;

		int currentTime = clock.getTurn();
		long difference = currentTime - evaluationLastUpdate;

		if (difference >= setup.getTimeSeriesUpdatePeriod()) {
			evaluationLastUpdate = currentTime;
			refresh = true;
		}

		return refresh;
	}

	private boolean needPrediction() {
		boolean refresh = false;
		int currentTime = clock.getTurn();

		if (predictionLastUpdate < 0) {
			refresh = true;
			predictionLastUpdate = currentTime;
		} else {
			long difference = currentTime - predictionLastUpdate;

			if (difference >= setup.getWorldStateUpdatePeriod()) {
				predictionLastUpdate = currentTime;
				refresh = true;
			}
		}

		return refresh;
	}

	boolean needAddWorldState() {
		boolean refresh = false;
		int currentTime = clock.getTurn();

		if (worldStateLastUpdate < 0) {
			refresh = true;
			worldStateLastUpdate = currentTime;
		} else {
			long difference = currentTime - worldStateLastUpdate;

			if (difference >= setup.getWorldStateUpdatePeriod()) {
				worldStateLastUpdate = currentTime;
				refresh = true;
			}
		}
		return refresh;
	}

	boolean needClassifiers() {
		boolean refresh = false;

		int currentTime = clock.getTurn();
		long difference = currentTime - classifiersLastUpdate;

		if (difference >= setup.getTimeSeriesUpdatePeriod()) {
			classifiersLastUpdate = currentTime;
			refresh = true;
		}

		return refresh;
	}

	public double predictAvgDuration(Link link, double avgDuration) {
		if (predictor.willAppearTrafficJam(link)) {
			avgDuration *= timeTableMultiplier;
		}
		return avgDuration;
	}

	public float getEvaluationMultiplier(Lane lane, float evaluation) {
		if (predictor.willAppearTrafficJam(lane.getOwner())) {
			evaluation *= evaluationMultiplier;
		}
		return evaluation;
	}

	public double getLastPeriodAvgDurationForLink(int linkNumber) {
		return lastPeriodAvgDurationTable[linkNumber];
	}

	public void setEvalView(EvalIView evalView) {
		dataPicker.setEvalView(evalView);
	}

	public void setSimpleDecisionView(SimpleDecisionEView simpleDecisionView) {
		dataPicker.setSimpleDecisionView(simpleDecisionView);
	}
}

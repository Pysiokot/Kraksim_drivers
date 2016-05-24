package pl.edu.agh.cs.kraksim.weka.statistics;

import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.weka.PredictionSetup;

import java.util.List;

public class ErrorResultCreator {
	private static final Logger LOGGER = Logger.getLogger(ErrorResultCreator.class);
	private final Archive<Double> classData;
	private final Archive<Double> classDataPrediction;
	private long count = 0;
	private double mapSum = 0;

	public ErrorResultCreator(PredictionSetup setup, Archive<Double> classData, Archive<Double> classDataPrediction) {
		this.classData = classData;
		this.classDataPrediction = classDataPrediction;
	}

	public void computePartialResults(int actualTurn) {
		LOGGER.debug("Compute error partial results");
		for (Integer turn : classDataPrediction) {
			if (turn <= actualTurn) {
				List<Double> congestionList = classData.getCongestionListByTurn(turn);
				List<Double> predictionList = classDataPrediction.getCongestionListByTurn(turn);

				for (int i = 0; i < congestionList.size(); i++) {
					Double realValue = congestionList.get(i);
					Double predictedValue = predictionList.get(i);

					if (realValue != 0.0) {
						count++;
						mapSum += Math.abs((realValue - predictedValue) / realValue);
					}
				}
			}
		}
//		LOGGER.debug("Write to excel");
//		PredictionsToExcel pte = new PredictionsToExcel(setup);
//		pte.writeToExcel(actualTurn, classData, classDataPrediction);
		LOGGER.debug("Clear data history");
		classDataPrediction.clear();
		classData.clear();
	}

	public String getResultText(int actualTurn) {
		double map = mapSum / count * 100;
		return "MAPE: " + map + '\n';
	}
}

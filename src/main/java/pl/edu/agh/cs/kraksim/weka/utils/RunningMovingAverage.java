package pl.edu.agh.cs.kraksim.weka.utils;

import pl.edu.agh.cs.kraksim.weka.data.AssociatedWorldState;

public class RunningMovingAverage extends AbstractMovingAverage {
	public RunningMovingAverage(int weight) {
		queueSize = 2;
	}

	@Override
	protected AssociatedWorldState computeAverage() {
//		int stateSize = stateQueue.getFirst().getCarsDensityTable().length;
//		double[] durationLevelTable;
//		double[] carsLeavingLinkTable;
//		double[] carsDensityTable;
//		if (stateQueue.size() > 1) {
//			durationLevelTable = computeDurationLevelRMA(stateSize);
//			carsLeavingLinkTable = computeCarsLeavingRMA(stateSize);
//			carsDensityTable = computeCarsDensityRMA(stateSize);
//		} else {
//			WorldStateRoads soleState = stateQueue.getFirst();
//			durationLevelTable = soleState.getDurationLevelTable();
//			carsLeavingLinkTable = soleState.getCarsOutLinkTable();
//			carsDensityTable = soleState.getCarsDensityTable();
//		}
//		double[] maxLinkEvaluationTable = stateQueue.getFirst().getEvaluationTable();
//		double[] greenDurationTable = stateQueue.getFirst().getGreenDurationTable();
//		double[] carsDensityMovingAvgTable = stateQueue.getFirst().getCarsMovingAvgTable();
//		double[] durationLevelMovingAvgTable = stateQueue.getFirst().getDurationLevelMovingAvgTable();
		//TODO add averages to state object
		return new AssociatedWorldState();
	}

	private double[] computeCarsDensityRMA(int stateSize) {
		//		for (int i = 0; i < stateSize; i++) {
//			double rmaYesterday = stateQueue.get(1).getCarsDensityTable()[i];
//			double currentValue = stateQueue.get(0).getCarsDensityTable()[i];
//			carsDensityTable[i] = (weight - 1) * rmaYesterday + currentValue;
//			carsDensityTable[i] /= weight;
//		}
		return new double[stateSize];
	}

	private double[] computeCarsLeavingRMA(int stateSize) {
		//		for (int i = 0; i < stateSize; i++) {
//			double rmaYesterday = stateQueue.get(1).getCarsOutLinkTable()[i];
//			double currentValue = stateQueue.get(0).getCarsOutLinkTable()[i];
//			carsLeavingLinkTable[i] = (weight - 1) * rmaYesterday + currentValue;
//			carsLeavingLinkTable[i] /= weight;
//		}
		return new double[stateSize];
	}

	private double[] computeDurationLevelRMA(int stateSize) {
		//		for (int i = 0; i < stateSize; i++) {
//			double rmaYesterday = stateQueue.get(1).getDurationLevelTable()[i];
//			double currentValue = stateQueue.get(0).getDurationLevelTable()[i];
//			durationLevelTable[i] = (weight - 1) * rmaYesterday + currentValue;
//			durationLevelTable[i] /= weight;
//		}
		return new double[stateSize];
	}
}

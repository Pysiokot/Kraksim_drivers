package pl.edu.agh.cs.kraksim.weka;

import java.util.LinkedList;

public class MovingAverage {
	private final LinkedList<double[]> stateQueue = new LinkedList<>();
	private final int queueSize;

	public MovingAverage(int queueSize) {
		this.queueSize = queueSize;
	}

	public double[] computeAverage(double[] carsDensity) {
		stateQueue.addFirst(carsDensity);
		if (stateQueue.size() > queueSize) {
			stateQueue.removeLast();
		}
		return computeAverage();
	}

	private double[] computeAverage() {
		int stateSize = stateQueue.getFirst().length;
		double[] averageTable = new double[stateSize];

		for (double[] carsDensity : stateQueue) {
			for (int i = 0; i < stateSize; i++) {
				averageTable[i] += carsDensity[i];
			}
		}
		int queueSize = stateQueue.size();
		for (int i = 0; i < stateSize; i++) {
			averageTable[i] /= queueSize;
		}

		return averageTable;
	}
}

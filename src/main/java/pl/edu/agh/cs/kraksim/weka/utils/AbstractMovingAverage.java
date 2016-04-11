package pl.edu.agh.cs.kraksim.weka.utils;

import pl.edu.agh.cs.kraksim.weka.data.AssociatedWorldState;

import java.util.LinkedList;

public abstract class AbstractMovingAverage {
	protected LinkedList<AssociatedWorldState> stateQueue = new LinkedList<>();
	protected int queueSize;

	public AssociatedWorldState computeAverage(AssociatedWorldState worldState) {
		stateQueue.addFirst(worldState);
		if (stateQueue.size() > queueSize) {
			stateQueue.removeLast();
		}
		return computeAverage();
	}

	protected abstract AssociatedWorldState computeAverage();
}
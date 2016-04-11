package pl.edu.agh.cs.kraksim.routing.prediction;

import java.util.Map;
import java.util.Queue;
import java.util.Set;

public interface ITrafficPredictor {

	void appendWorldState(WorldState state);

	void adjustCurrentWeightsOfLink(double[] weightsOfLinks);

	void setup(ITrafficPredictionSetup setup);

	//TODO Delete in the future;
	@Deprecated
	Map<Integer, Set<Integer>> getNeighborsArray();

	@Deprecated
	Queue<WorldState> getHistory();
}

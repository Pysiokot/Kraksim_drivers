package pl.edu.agh.cs.kraksim.weka;

import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.weka.data.AssociatedWorldState;

public interface WekaPredictor {
	boolean willAppearTrafficJam(Link link);

	void addWorldState(int turn, AssociatedWorldState associatedWorldState);

	void createClassifiers();

	void predictCongestions(int turn);

	void makeEvaluation(int turn);
}

package pl.edu.agh.cs.kraksim.main.gui;

import pl.edu.agh.cs.kraksim.sna.GraphVisualizator;

public interface Controllable extends Runnable {
	void doStep();

	void doRun();

	void doPause();

	void setControler(OptionsPanel panel);

	SimulationVisualizator getVisualizator();

	void setGraphVisualizator(GraphVisualizator graphVisualizator);
}

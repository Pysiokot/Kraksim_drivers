package pl.edu.agh.cs.kraksim.main.gui;

import pl.edu.agh.cs.kraksim.sna.GraphVisualizer;

public interface Controllable extends Runnable {
	void doStep();

	void doRun();

	void doPause();

	void setController(OptionsPanel panel);

	SimulationVisualizer getVisualizer();

	void setGraphVisualizer(GraphVisualizer graphVisualizer);
}

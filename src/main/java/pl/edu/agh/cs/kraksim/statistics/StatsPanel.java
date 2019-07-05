package pl.edu.agh.cs.kraksim.statistics;

import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.main.UpdateHook;
import pl.edu.agh.cs.kraksim.main.gui.Controllable;
import pl.edu.agh.cs.kraksim.main.gui.GUISimulationVisualizer;
import pl.edu.agh.cs.kraksim.main.gui.SimulationVisualizer;
import pl.edu.agh.cs.kraksim.ministat.CityMiniStatExt;
import pl.edu.agh.cs.kraksim.statistics.charts.NumberChart;
import pl.edu.agh.cs.kraksim.statistics.charts.TooSlowCarsNumberChart;

import javax.swing.*;
import java.awt.*;

public class StatsPanel extends JPanel {
	public static final Logger LOGGER = Logger.getLogger(StatsPanel.class);

	public StatsPanel(Controllable sim) {
		//carNumberChart = new NumberChart("Car count");

		GridLayout grid = new GridLayout(0, 2);
		grid.setHgap(10);
		grid.setVgap(10);
		setLayout(grid);

		SimulationVisualizer vis = sim.getVisualizer();

		if (vis instanceof GUISimulationVisualizer) {
			final GUISimulationVisualizer visPanel = ((GUISimulationVisualizer) sim.getVisualizer());

			final NumberChart cnc = new NumberChart("Car count", visPanel.cityStat) {
				@Override
				public void refresh() {
					addData(cityStat.getCarCount());
				}
			};

			add(cnc);
			final NumberChart cvc = new NumberChart("Average velocity", visPanel.cityStat) {
				@Override
				public void refresh() {
					addData(cityStat.getAvgVelocity());
				}
			};

			cvc.setRange(0, 5);
			add(cvc);
			final NumberChart csc = new NumberChart("Avarage current velocity", visPanel.cityStat) {
				@Override
				public void refresh() {
					addData(cityStat.getAvgCarSpeed());
				}
			};

			csc.setRange(0, 5);
			add(csc);

			final NumberChart wcc = new NumberChart("Cars waiting on red light", visPanel.cityStat) {
				@Override
				public void refresh() {
					addData(cityStat.getAllCarsOnRedLight());
				}
			};


			wcc.setRange(0, 500);
			add(wcc);

			final NumberChart carVelocityBelowValue = new TooSlowCarsNumberChart("Too slow cars number", visPanel.cityStat, visPanel);

			carVelocityBelowValue.setRange(0, 1000);
			add(carVelocityBelowValue);

			visPanel.addUpdateHook(new UpdateHook() {
				@Override
				public void onUpdate(CityMiniStatExt cityStat) {
					cnc.refresh();
					cvc.refresh();
					csc.refresh();
					wcc.refresh();
					carVelocityBelowValue.refresh();
					//LOGGER.info(visPanel.cityStat.getAllCarsOnRedLight());
				}
			});
		}
	}

	public void addPointToChart(double x, double y) {
//		carNumberChart.addPoint(x, y);
	}
}
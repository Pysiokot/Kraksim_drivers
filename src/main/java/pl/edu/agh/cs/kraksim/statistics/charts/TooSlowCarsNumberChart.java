package pl.edu.agh.cs.kraksim.statistics.charts;

import pl.edu.agh.cs.kraksim.main.gui.GUISimulationVisualizator;
import pl.edu.agh.cs.kraksim.ministat.CityMiniStatExt;
import pl.edu.agh.cs.kraksim.real.RealSimulationParams;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TooSlowCarsNumberChart extends NumberChart {
	private final GUISimulationVisualizator simView;
	private JTextField valueLabel;
	private JButton confirmButton;
	private Float value;

	public TooSlowCarsNumberChart(String title, CityMiniStatExt cityStat, GUISimulationVisualizator simView) {
		super(title, cityStat);
		this.simView = simView;
		init();
		addListeners();
	}

	private void init() {
		setRange(1, 1000);
		value = RealSimulationParams.convertFromKMToSpeed(20);
		valueLabel = new JTextField("20");
		confirmButton = new JButton("Confirm");

		FlowLayout flow = new FlowLayout();
		JPanel panel = new JPanel();
		panel.setLayout(flow);
		panel.add(valueLabel);
		panel.add(confirmButton);

		add(panel);
	}

	private void addListeners() {
		confirmButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				value = RealSimulationParams.convertFromKMToSpeed(Float.valueOf(valueLabel.getText()));
			}
		});
	}

	@Override
	public void refresh() {
		addData(simView.getNumberOfCarBelowValue(value));
	}
}

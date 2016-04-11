/**
 *
 */
package pl.edu.agh.cs.kraksim.main.gui;

import com.google.common.collect.ImmutableList;
import javafx.scene.control.Alert;
import org.apache.log4j.Priority;
import pl.edu.agh.cs.kraksim.KraksimConfigurator;
import pl.edu.agh.cs.kraksim.main.CarMoveModel;
import pl.edu.agh.cs.kraksim.sna.centrality.CentrallityCalculator;
import pl.edu.agh.cs.kraksim.sna.centrality.MeasureType;
import pl.edu.agh.cs.kraksim.sna.centrality.SNADistanceType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksimcitydesigner.AppRunner;

public class SetUpPanel extends JPanel {
	private static final long serialVersionUID = -4635082252841397559L;

	private static final List<String> availableMoveModels = ImmutableList.of(CarMoveModel.MODEL_NAGLE, CarMoveModel.MODEL_VDR, CarMoveModel.MODEL_BRAKELIGHT, CarMoveModel.MODEL_MULTINAGLE);

	private InputPanel cityMapLocation;
	private InputPanel travellingSchemeLocation;
	private InputPanel statsOutputLocation;
	private InputPanel algorithm;
	private InputPanel yellowTransition;
	private JButton designer;

	private JFrame myFrame = null;

	JButton init = new JButton("Load");

	JPanel filesPane = null;

	MainVisualisationPanel parent = null;

	private ButtonGroup votingAlgorithmsGroup = null;
	private ButtonGroup zoneAwareGroup = null;
	private JComboBox<SNADistanceType> metricTypeComboBox = null;
	private JSpinner numberOfWinnersSpinner = null;

	private Properties params;
	private Properties lastSessionParams;
	private String carMoveModel;

	public SetUpPanel(MainVisualisationPanel parent, Properties params) {
		this.parent = parent;
		initParams(params);
		initLayout();
	}

	public SetUpPanel(MainVisualisationPanel parent) {
		this.parent = parent;
		initParams(new Properties());
		initLayout();
	}

	private void initParams(Properties params) {
		this.params = params;
		lastSessionParams = new Properties();

		if (this.params.getProperty("lastSessionFile") != null) {
			try {
				lastSessionParams.load(new FileInputStream(params.getProperty("lastSessionFile")));
			} catch (Exception e) {
                Logger.getLogger(SetUpPanel.class).log(Priority.WARN, "Last session configuration not found. Using default.");
                lastSessionParams = KraksimConfigurator.createDefaultSessionConfig();
			}
		}
	}

	private String getParam(String name) {
		if (lastSessionParams != null && lastSessionParams.getProperty(name) != null) {
			return lastSessionParams.getProperty(name);
		} else {
			return params.getProperty(name);
		}
	}

	private void storeParam(String key, String value) {
		lastSessionParams.put(key, value);
		String lastSessionFile = params.getProperty("lastSessionFile");
		if (lastSessionFile != null) {
			try {
				lastSessionParams.store(new FileOutputStream(lastSessionFile), null);
			} catch (Exception ignored) {
			}
		}
	}

	public final void initLayout() {
		if (myFrame == null) {
			myFrame = new JFrame("Simulation settings");
			myFrame.setSize(370, 280);
			myFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			createLayout();
			myFrame.add(this);
		}
		init.setVisible(true);
		init.setEnabled(true);
		myFrame.setVisible(true);
		myFrame.pack();
	}

	private void createLayout() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		final JFileChooser fc = new JFileChooser();
		String workDir = getParam("workDir");
		fc.setCurrentDirectory(new File(workDir));

		filesPane = new JPanel();
		filesPane.setLayout(new GridLayout(0, 1));
		filesPane.setBorder(BorderFactory.createTitledBorder("Parameters"));

		String fileLocation = getParam("cityMapFile");
		cityMapLocation = new InputPanel("City map", fileLocation, 20, fc);
		fileLocation = getParam("travelSchemeFile");
		travellingSchemeLocation = new InputPanel("Traffic model", fileLocation, 20, fc);
		fileLocation = getParam("statOutFile");
		statsOutputLocation = new InputPanel("Statistics", fileLocation, 20, fc);
		algorithm = new InputPanel("Algorithm", getParam("algorithm"), 20, null);
		yellowTransition = new InputPanel("Yellow Duration", "3", 20, null);
		designer = new JButton("OpenCityDesigner");

		designer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						AppRunner.createAndShowGUI();
					}
				});
			}
		});

		filesPane.add(designer);
		filesPane.add(cityMapLocation);
		filesPane.add(travellingSchemeLocation);
		filesPane.add(statsOutputLocation);
		filesPane.add(algorithm);
		filesPane.add(yellowTransition);

		add(filesPane);

		// Miary
		JPanel measurePanel = new JPanel();
		measurePanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		measurePanel.setBorder(BorderFactory.createTitledBorder("Measures"));
		measurePanel.setPreferredSize(new Dimension(600, 55));
		measurePanel.setMinimumSize(new Dimension(600, 55));
		measurePanel.setMaximumSize(new Dimension(1600, 55));

		JComboBox<MeasureType> types = new JComboBox<>();
		types.addItem(MeasureType.PageRank);
		types.addItem(MeasureType.BetweenesCentrallity);

		types.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				CentrallityCalculator.measureType = (MeasureType) cb.getSelectedItem();
			}
		});

		measurePanel.add(types);
		add(measurePanel);

		JPanel moveModelPane = new JPanel();
		moveModelPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		moveModelPane.setBorder(BorderFactory.createTitledBorder("Move model settings"));
		JComboBox<String> moveModels = new JComboBox<>();
		moveModels.addItem(getParam("carMoveModel"));
		moveModels.addItem(CarMoveModel.MODEL_NAGLE + ":decProb=0.2");
		moveModels.addItem(CarMoveModel.MODEL_VDR + ":zeroProb=0.9,movingProb=0.2");
		moveModels.addItem(CarMoveModel.MODEL_BRAKELIGHT + ":zeroProb=0.9,movingProb=0.2,brakeProb=0.2,threshold=5");
		moveModels.addItem(CarMoveModel.MODEL_MULTINAGLE + ":decProb=0.2");
		moveModels.setEditable(true);
		moveModels.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox<String> source = (JComboBox<String>) e.getSource();
				carMoveModel = (String) source.getSelectedItem();
				int c = carMoveModel.indexOf(':');
				String check = null;
				if (c == -1) {
					check = carMoveModel;
				} else {
					check = carMoveModel.substring(0, c);
				}
				if (!availableMoveModels.contains(check)) {
					source.setSelectedIndex(0);
					JOptionPane.showMessageDialog(source.getParent(), "Unknown move model : " + check, "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		moveModelPane.add(moveModels);

		String lastMoveModel = getParam("carMoveModel");
		moveModels.setSelectedItem(lastMoveModel);

		add(moveModelPane);

		JPanel commandsPane = new JPanel();
		commandsPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		commandsPane.setBorder(BorderFactory.createTitledBorder("Commands"));
		commandsPane.setPreferredSize(new Dimension(600, 55));
		commandsPane.setMinimumSize(new Dimension(600, 55));
		commandsPane.setMaximumSize(new Dimension(1600, 55));

		// synchronize buttons first
		init.setEnabled(true);

		init.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				Properties props = new Properties(params);
				props.setProperty("cityMapFile", cityMapLocation.getText());
				storeParam("cityMapFile", cityMapLocation.getText());

                System.out.println(cityMapLocation.getText());

				props.setProperty("travelSchemeFile", travellingSchemeLocation.getText());
				storeParam("travelSchemeFile", travellingSchemeLocation.getText());
				props.setProperty("statOutFile", statsOutputLocation.getText());
				storeParam("statOutFile", statsOutputLocation.getText());
				props.setProperty("algorithm", algorithm.getText());
				storeParam("algorithm", algorithm.getText());
				props.setProperty("yellowTransition", yellowTransition.getText());
				storeParam("yellowTransition", yellowTransition.getText());
				storeParam("workDir", fc.getCurrentDirectory().toString());

				String algStr = votingAlgorithmsGroup.getSelection().getActionCommand() + ':' +
						metricTypeComboBox.getSelectedItem();
				props.setProperty("centralNodesAlgMod", algStr);
				storeParam("centralNodesAlgMod", algStr);
				props.setProperty("numberOfWiners", numberOfWinnersSpinner.getValue().toString());
				props.setProperty("carMoveModel", carMoveModel);
				storeParam("carMoveModel", carMoveModel);

				props.setProperty("visualization", "true");

				props.setProperty("zone_awareness", zoneAwareGroup.getSelection().getActionCommand());
				storeParam("zone_awareness", zoneAwareGroup.getSelection().getActionCommand());

				// read some parameters from file
				Properties fileProps = KraksimConfigurator.getPropertiesFromFile();
				String realModule = fileProps.getProperty("realModule");
				if(fileProps.getProperty("realModule") != null) {
					props.setProperty("realModule", realModule);
				}

				String switchTime = fileProps.getProperty("switchTime");
				if(switchTime != null){
					props.setProperty("switchTime", switchTime);
				}

				String minSafeDistance = fileProps.getProperty("minSafeDistance");
				if(minSafeDistance != null) {
					props.setProperty("minSafeDistance", minSafeDistance);
				}

				parent.initializeSimulation(props);
				init.setEnabled(false);
				myFrame.setVisible(false);
			}
		});

		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				myFrame.setVisible(false);
			}
		});

		commandsPane.add(init);
		commandsPane.add(cancel);

		initVotingAlgorithmPanel();
		initZoneAwarnessPanel();

		add(commandsPane, BorderLayout.NORTH);
	}

	// TODO
	private void initVotingAlgorithmPanel() {
		JPanel votingAlgorithmsPanel = new JPanel();
		votingAlgorithmsPanel.setBorder(BorderFactory.createTitledBorder("Voting algorithm"));
		votingAlgorithmsPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
		votingAlgorithmsPanel.setPreferredSize(new Dimension(600, 55));
		votingAlgorithmsPanel.setMinimumSize(new Dimension(600, 55));
		votingAlgorithmsPanel.setMaximumSize(new Dimension(1600, 55));

		JRadioButton noneAlgorithmRadioButton = new JRadioButton("None");
		noneAlgorithmRadioButton.setSelected(true);
		noneAlgorithmRadioButton.setActionCommand("none");

		JRadioButton simpleAlgorithmRadioButton = new JRadioButton("Simple");
		simpleAlgorithmRadioButton.setSelected(false);
		simpleAlgorithmRadioButton.setActionCommand("simple");

		votingAlgorithmsGroup = new ButtonGroup();
		votingAlgorithmsGroup.add(noneAlgorithmRadioButton);
		votingAlgorithmsGroup.add(simpleAlgorithmRadioButton);

		votingAlgorithmsPanel.add(noneAlgorithmRadioButton);
		votingAlgorithmsPanel.add(simpleAlgorithmRadioButton);

		metricTypeComboBox = new JComboBox<>();
		metricTypeComboBox.addItem(SNADistanceType.CrossroadsNumber);
		metricTypeComboBox.addItem(SNADistanceType.Weight);
		metricTypeComboBox.addItem(SNADistanceType.Load);
		metricTypeComboBox.setEnabled(false);
		votingAlgorithmsPanel.add(metricTypeComboBox);

		noneAlgorithmRadioButton.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					metricTypeComboBox.setEnabled(false);
					numberOfWinnersSpinner.setEnabled(false);
				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
					metricTypeComboBox.setEnabled(true);
					numberOfWinnersSpinner.setEnabled(true);
				}
			}
		});

		numberOfWinnersSpinner = new JSpinner();
		numberOfWinnersSpinner.setModel(new SpinnerNumberModel(1, 1, 10, 1));
		numberOfWinnersSpinner.setEnabled(false);
		votingAlgorithmsPanel.add(numberOfWinnersSpinner);

		add(votingAlgorithmsPanel);
	}

	private void initZoneAwarnessPanel() {
		JPanel zoneAwarnessPanel = new JPanel();
		zoneAwarnessPanel.setBorder(BorderFactory.createTitledBorder("Zone awareness"));
		zoneAwarnessPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
		zoneAwarnessPanel.setPreferredSize(new Dimension(600, 55));
		zoneAwarnessPanel.setMinimumSize(new Dimension(600, 55));
		zoneAwarnessPanel.setMaximumSize(new Dimension(1600, 55));

		JRadioButton zoneEnabledButton = new JRadioButton("Enabled");
		zoneEnabledButton.setSelected(true);
		zoneEnabledButton.setActionCommand("enabled");

		JRadioButton zoneDisabledButton = new JRadioButton("Disabled");
		zoneDisabledButton.setSelected(false);
		zoneDisabledButton.setActionCommand("disabled");

		zoneAwareGroup = new ButtonGroup();
		zoneAwareGroup.add(zoneEnabledButton);
		zoneAwareGroup.add(zoneDisabledButton);

		zoneAwarnessPanel.add(zoneEnabledButton);
		zoneAwarnessPanel.add(zoneDisabledButton);

		add(zoneAwarnessPanel);
	}

	public void end() {
		init.setEnabled(true);
	}
}

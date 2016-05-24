/**
 *
 */
package pl.edu.agh.cs.kraksim.main.gui;

import pl.edu.agh.cs.kraksim.main.Simulation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

//TODO: refactor, divide into 2 layers
/**
 * @author Bartosz Rybacki
 *
 */

/**
 * @author Lukasz Dziewonski
 */
public class OptionsPanel extends JPanel {
	private static final long serialVersionUID = -4635082252841397559L;

	private InputPanel cityMapLocation;
	private InputPanel travellingSchemeLocation;
	private InputPanel statsOutputLocation;
	private InputPanel algorithm;
	private InputPanel yellowTransition;
	private transient Controllable sim = null;

	private final JButton run = new JButton("Run");
	private final JButton init = new JButton("Init");
	private final JButton step = new JButton("Step");
	private final JButton pause = new JButton("Pause");

	private JPanel filesPane = null;

	private Properties params;

	/**
	 * Create new OptionsPanel.
	 */
	public OptionsPanel() {
		initLayout();
	}

	private String getParam(String name) {
		return params.getProperty(name);
	}

	private void initLayout() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new File(getParam("workDir")));

		filesPane = new JPanel();
		filesPane.setLayout(new GridLayout(0, 1));
		filesPane.setBorder(BorderFactory.createTitledBorder("Parameters"));

		String fileLocation = getParam("cityMapFile");
		cityMapLocation = new InputPanel("Mapa miasta", fileLocation, 20, fc);
		fileLocation = getParam("travelSchemeFile");
		travellingSchemeLocation = new InputPanel("Schemat ruchu", fileLocation, 20, fc);
		fileLocation = getParam("statOutFile");
		statsOutputLocation = new InputPanel("Statystyki", fileLocation, 20, fc);
		algorithm = new InputPanel("Algorithm", getParam("algorithm"), 20, null);
		yellowTransition = new InputPanel("Yellow Duration", "3", 20, null);

		filesPane.add(cityMapLocation);
		filesPane.add(travellingSchemeLocation);
		filesPane.add(statsOutputLocation);
		filesPane.add(algorithm);
		filesPane.add(yellowTransition);

		add(filesPane);

		JPanel commandsPane = new JPanel();
		commandsPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		commandsPane.setBorder(BorderFactory.createTitledBorder("Commands"));
		commandsPane.setPreferredSize(new Dimension(600, 55));
		commandsPane.setMinimumSize(new Dimension(600, 55));
		commandsPane.setMaximumSize(new Dimension(1600, 55));

		//synchronize buttons first
		init.setEnabled(true);
		run.setEnabled(false);
		step.setEnabled(false);
		pause.setEnabled(false);

		run.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (sim != null) {
					sim.doRun();

					init.setEnabled(false);
					run.setEnabled(false);
					step.setEnabled(false);
					pause.setEnabled(true);
				}
			}
		});

		init.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				String visualization = getParam("visualization");
				String routing = getParam("dynamicRouting");

				boolean visualize = visualization.equals("true");
				List<String> paramsList = new ArrayList<>();
				if (visualize) {
					paramsList.add("-v");
				}

				paramsList.add("-t");
				paramsList.add(yellowTransition.getText());
				paramsList.add("-s");
				paramsList.add("975");
				paramsList.add("-S");
				paramsList.add("1298");

				String minimalSpeedUsingPrediction = getParam("minimalSpeedUsingPrediction");
				if ("true".equals(minimalSpeedUsingPrediction)) {
					paramsList.add("-m");
				}

				if (params.getProperty("globalUpdateInterval") != null) {
					paramsList.add("-u");
					paramsList.add(params.getProperty("globalUpdateInterval"));
				}

				if (!empty(routing)) {
					//          System.out.println( "routing " + routing );
					paramsList.add("-r");
					paramsList.add(routing);
					paramsList.add("-d");
					paramsList.add("100");
					paramsList.add("-k");
					paramsList.add("100");
				}

				if (!empty(statsOutputLocation.getText())) {
					paramsList.add("-o");
					paramsList.add(statsOutputLocation.getText());
				}

				paramsList.add(algorithm.getText());
				paramsList.add(cityMapLocation.getText());
				paramsList.add(travellingSchemeLocation.getText());

				sim = new Simulation(paramsList.toArray(new String[0]));

				SimulationVisualizer vis = sim.getVisualizer();
				sim.setController(OptionsPanel.this);

				if (vis instanceof GUISimulationVisualizer) {
					GUISimulationVisualizer simPanel = ((GUISimulationVisualizer) sim.getVisualizer());
					hideOptions();
					addSimPanel(simPanel.getVisualizerComponent());
					addControlPanel(simPanel.getControlPane());
				}

				Thread runner = new Thread(sim);
				runner.start();

				init.setEnabled(false);
				run.setEnabled(true);
				step.setEnabled(true);
				pause.setEnabled(false);
			}

			private boolean empty(String text) {
				return text == null || text.isEmpty();
			}
		});

		step.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (sim != null) {
					sim.doStep();

					init.setEnabled(false);
					run.setEnabled(true);
					step.setEnabled(true);
					pause.setEnabled(false);
				}
			}
		});

		pause.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (sim != null) {
					sim.doPause();

					init.setEnabled(false);
					run.setEnabled(true);
					step.setEnabled(true);
					pause.setEnabled(false);
				}
			}
		});

		commandsPane.add(init);
		commandsPane.add(run);
		commandsPane.add(step);
		commandsPane.add(pause);

		//add(commandsPane, BorderLayout.NORTH);
		add(commandsPane);

		//    String args = "-v -t %4 -s 975 -S 1298 -o tests/results/%1-%2-%3-trans%4.txt %1 tests/model-%2.xml tests/traffic-%3.xml";

	}

	private void hideOptions() {
		remove(filesPane);
	}

	private void addControlPanel(Component ctrlPanel) {
		add(ctrlPanel);//, BorderLayout.SOUTH);
	}

	private void addSimPanel(Component simPanel) {
		JScrollPane scroller = new JScrollPane(simPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroller.setPreferredSize(new Dimension(600, 400));
		scroller.setMinimumSize(new Dimension(600, 100));
		scroller.setMaximumSize(new Dimension(1600, 1200));

		add(scroller);//, BorderLayout.CENTER);
	}

	public static void main(String[] args) {
		String lookAndFeel = null;
		lookAndFeel = UIManager.getSystemLookAndFeelClassName();
		try {
			UIManager.setLookAndFeel(lookAndFeel);
		} catch (Exception e) {
			// on error, we get default swing look and feel 
		}

		InputStream inStream = OptionsPanel.class.getClassLoader().getResourceAsStream("config.properties");
		final Properties props = new Properties();

		try {
			props.load(inStream);
		} catch (IOException e) {
			e.printStackTrace();
		}

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				JFrame frame = new JFrame("Test");
				frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

				frame.getContentPane().add(new MainVisualisationPanel(props));
				frame.setSize(370, 270);
				//        frame.setResizable( false );
				frame.setVisible(true);
			}
		});
	}

	public void end() {
		init.setEnabled(true);
		run.setEnabled(false);
		step.setEnabled(false);
		pause.setEnabled(false);
	}
}

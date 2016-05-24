package pl.edu.agh.cs.kraksim.main.gui;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;

import pl.edu.agh.cs.kraksim.KraksimConfigurator;
import pl.edu.agh.cs.kraksim.core.*;
import pl.edu.agh.cs.kraksim.main.Simulation;
import pl.edu.agh.cs.kraksim.real.RealSimulationParams;
import pl.edu.agh.cs.kraksim.sna.GraphVisualizer;
import pl.edu.agh.cs.kraksim.sna.centrality.CentralityCalculator;
import pl.edu.agh.cs.kraksim.sna.centrality.KmeansClustering;
import pl.edu.agh.cs.kraksim.sna.centrality.MeasureType;
import pl.edu.agh.cs.kraksim.statistics.StatsPanel;
import pl.edu.agh.cs.kraksim.util.MeasuresExcelWriter;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

public class MainVisualisationPanel extends JPanel implements GraphVisualizer {
	private static final long serialVersionUID = 2195425331247205783L;
	private static final Logger LOGGER = Logger.getLogger(MainVisualisationPanel.class);

	private JMenuItem load;

	private JPanel commandsPane = null;
	private final JButton run = new JButton("Run");
	private final JButton step = new JButton("Step");
	private final JButton pause = new JButton("Pause");
	// Do grafu
	private final JPanel measures = new JPanel();
	private final JPanel graphPanel = new JPanel();
	private SetUpPanel setUpPanel = null;
	private VisualizationViewer<Node, Link> vv;
	private transient Controllable sim = null;
	private JPanel simPanel = null;
	private Component ctrlPane = null;
	/**
	 * Pola do wprowadzania poziomow kolorowania
	 */
	private final JTextField redLevelField = new JTextField(Float.toString(RealSimulationParams.getRedLevel()));
	private final JTextField orangeLevelField = new JTextField(Float.toString(RealSimulationParams.getOrangeLevel()));
	private final JButton setNewColoringLevelsButton = new JButton("Save");
	private final MeasuresExcelWriter excelWriter = new MeasuresExcelWriter();
	private Properties params = new Properties();

	private void initLayout() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JMenuBar bar = new JMenuBar();
		JMenu file = new JMenu("File");
		bar.add(file);

		load = new JMenuItem("Settings");
		load.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				pause.doClick();
				SetUpPanel panel = setUpPanel;
				if (panel == null) {
					panel = new SetUpPanel(MainVisualisationPanel.this, params);
					setUpPanel = panel;
				} else {
					panel.initLayout();
				}
			}
		});

		JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(1);
			}
		});
		file.add(load);
		file.add(exit);
		add(bar, BorderLayout.NORTH);

		commandsPane = new JPanel();
		commandsPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		commandsPane.setBorder(BorderFactory.createTitledBorder("Commands"));
		commandsPane.setPreferredSize(new Dimension(600, 55));
		commandsPane.setMinimumSize(new Dimension(600, 55));
		commandsPane.setMaximumSize(new Dimension(1600, 55));

		// synchronize buttons first
		run.setEnabled(false);
		step.setEnabled(false);
		pause.setEnabled(false);

		run.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (sim != null) {
					sim.doRun();

					run.setEnabled(false);
					step.setEnabled(false);
					pause.setEnabled(true);
				}
			}
		});

		step.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (sim != null) {
					sim.doStep();

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

					run.setEnabled(true);
					step.setEnabled(true);
					pause.setEnabled(false);
				}
			}
		});

		setNewColoringLevelsButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				RealSimulationParams.setOrangeLevel(new Float(orangeLevelField.getText()));
				RealSimulationParams.setRedLevel(new Float(redLevelField.getText()));
			}
		});

		commandsPane.add(new JLabel("Orange level"));
		commandsPane.add(orangeLevelField);
		commandsPane.add(new JLabel("Red level"));
		commandsPane.add(redLevelField);
		commandsPane.add(setNewColoringLevelsButton);
		commandsPane.add(new JSeparator(SwingConstants.VERTICAL));

		commandsPane.add(run);
		commandsPane.add(step);
		commandsPane.add(pause);

		commandsPane.setVisible(false);
	}

	public void initializeSimulation(Properties params) {
		setProperties(params);

		LOGGER.info("Simulation is to be initialized");

		if (sim != null) {
			sim = null;
			remove(simPanel);
			simPanel = null;
		}
		KmeansClustering.setProperties(params);

		sim = new Simulation(KraksimConfigurator.prepareInputParametersForSimulation(params));
		sim.setGraphVisualizer(this);
		SimulationVisualizer vis = sim.getVisualizer();

		if (vis instanceof GUISimulationVisualizer) {
			GUISimulationVisualizer simPanel = ((GUISimulationVisualizer) sim.getVisualizer());
			addSimPanel(simPanel.getVisualizerComponent());
			addControlPanel(simPanel.getControlPane());
		}

		Thread runner = new Thread(sim);
		runner.start();

		run.setEnabled(true);
		step.setEnabled(true);
		pause.setEnabled(false);
		initGraph();
	}

	private void setProperties(Properties params) {
		this.params.putAll(params);
	}

	private void addSimPanel(Component simPanel) {
		JPanel pane = null;
		if (this.simPanel != null) {
			this.simPanel.removeAll();
			pane = this.simPanel;
		} else {
			pane = new JPanel();
			pane.setLayout(new BorderLayout());
			pane.add(commandsPane, BorderLayout.NORTH);
			this.simPanel = pane;
		}

		JScrollPane scroller = new JScrollPane(simPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroller.setPreferredSize(new Dimension(600, 400));
		scroller.setMinimumSize(new Dimension(600, 100));
		scroller.setMaximumSize(new Dimension(1600, 1200));

		JTabbedPane tabbedPane = new JTabbedPane();
		graphPanel.setLayout(new BorderLayout());
		tabbedPane.addTab("Simulation", null, scroller, "");
		JScrollPane graphScroller = new JScrollPane(graphPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tabbedPane.addTab("Graph", graphScroller);

		// statistics (graphs...) panel is created and added here
		JPanel statsPanel = new StatsPanel(sim);

		JScrollPane statsScroller = new JScrollPane(statsPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tabbedPane.addTab("Statistics", statsScroller);

		commandsPane.setVisible(true);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(tabbedPane, BorderLayout.CENTER);
		panel.add(new JScrollPane(measures, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.LINE_END);

		// pane.add(scroller, BorderLayout.CENTER);
		pane.add(panel, BorderLayout.CENTER);

		add(pane, BorderLayout.CENTER);
	}

	private void addControlPanel(Component ctrlPanel) {
		if (ctrlPane != null) {
			remove(ctrlPane);
		}
		ctrlPane = ctrlPanel;
		add(ctrlPanel, BorderLayout.SOUTH);
		ctrlPanel.setVisible(true);
	}

	private void initGraph() {
		Graph<Node, Link> graph = getSimulation().getModules().getGraph();
		KmeansClustering.clusterGraph(graph);

		Layout<Node, Link> layout = new FRLayout<>(graph);
		layout.setSize(new Dimension(900, 600));
		double maxX = 0, maxY = 0;
		for (Node node : graph.getVertices()) {
			if (node.getPoint().getX() > maxX) {
				maxX = node.getPoint().getX();
			}
			if (node.getPoint().getY() > maxY) {
				maxY = node.getPoint().getY();
			}
		}
		layout.setSize(new Dimension((int) maxX, (int) maxY));
		for (Node node : graph.getVertices()) {
			Point2D p = node.getPoint();
			layout.setLocation(node, p);
		}
		// System.out.println(maxX + " - " + maxY);
		vv = new VisualizationViewer<>(layout);
		vv.setPreferredSize(new Dimension(900, 600));
		vv.getRenderContext().setVertexFillPaintTransformer(new Transformer<Node, Paint>() {

			                                                    public Paint transform(Node node) {
				                                                    Color[] colors = {Color.yellow, Color.blue, Color.red, Color.pink, Color.white, Color.cyan, Color.orange, Color.magenta, Color.gray, Color.black};
				                                                    if (node.isGateway()) {
					                                                    return Color.GREEN;
				                                                    }

				                                                    Collection<Set<Node>> colection = KmeansClustering.currentClustering.values();
				                                                    // kolor klastra
				                                                    Iterator<Set<Node>> iterator = colection.iterator();
				                                                    for (int i = 0; i < colection.size(); i++) {
					                                                    if (iterator.next().contains(node)) {
						                                                    return colors[i];
					                                                    }
				                                                    }
				                                                    return Color.white;
			                                                    }
		                                                    }
		);
		vv.getRenderContext().setVertexLabelTransformer(new Transformer<Node, String>() {

			                                                public String transform(Node node) {
				                                                return node.getId();
			                                                }
		                                                }
		);

		vv.getRenderContext().setVertexShapeTransformer(new Transformer<Node, Shape>() {

			                                                @Override
			                                                public Shape transform(Node arg0) {
				                                                if (arg0.isGateway()) {
					                                                new Ellipse2D.Double(-15, -15, 30, 30);
				                                                }
				                                                Collection<Set<Node>> colection = KmeansClustering.currentClustering.values();
				                                                Set<Node> meansColection = KmeansClustering.currentClustering.keySet();

				                                                Iterator<Set<Node>> iterator = colection.iterator();
				                                                for (int i = 0; i < colection.size(); i++) {
					                                                if (iterator.next().contains(arg0)) {
						                                                if (meansColection.contains(arg0)) {
							                                                return new Rectangle(-15, -15, 30, 30);
						                                                }
						                                                return new Ellipse2D.Double(-15, -15, 30, 30);
					                                                }
				                                                }
				                                                return new Ellipse2D.Double(-15, -15, 30, 30);
			                                                }
		                                                }
		);
		vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
		gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		vv.setGraphMouse(gm);
		graphPanel.add(vv, BorderLayout.CENTER);

		measures.setLayout(new GridLayout(graph.getVertexCount(), 2));
		List<Node> nodes = new ArrayList<>(graph.getVertices());
		refreshMeasures(nodes);
	}

	public Simulation getSimulation() {
		return (Simulation) sim;
	}

	private void refreshMeasures(List<Node> nodes) {
		Collections.sort(nodes, new Comparator<Node>() {

			public int compare(Node o1, Node o2) {
				return new Double(o2.getMeasure()).compareTo(o1.getMeasure());
			}
		});
		measures.removeAll();
		for (Node n : nodes) {
			measures.add(new JLabel(n.getId() + " : "));
			measures.add(new JLabel(String.format("%1$.5f    ", n.getMeasure())));
		}
		excelWriter.persistIteration(nodes);
	}

	public MainVisualisationPanel(Properties props) {
		initParams(props);
		initLayout();
		load.doClick();
	}

	private void initParams(Properties params) {
		this.params = params;
	}

	@Override
	public void refreshGraph() {
		CentralityCalculator.calculateCentrality(getSimulation().getModules().getGraph(), MeasureType.PageRank, 3);
		refreshMeasures(new ArrayList<>(getSimulation().getModules().getGraph().getVertices()));
		refreshGraphCoolors();
	}

	private void refreshGraphCoolors() {
		Graph<Node, Link> graph = getSimulation().getModules().getGraph();
		vv.getRenderContext().setVertexFillPaintTransformer(new Transformer<Node, Paint>() {

			                                                    public Paint transform(Node node) {
				                                                    Color[] colors = {Color.yellow, Color.blue, Color.red, Color.pink, Color.white, Color.cyan, Color.orange, Color.magenta, Color.gray, Color.black};
				                                                    if (node.isGateway()) {
					                                                    return Color.GREEN;
				                                                    }

				                                                    Collection<Set<Node>> colection = KmeansClustering.currentClustering.values();
				                                                    // kolor klastra
				                                                    Iterator<Set<Node>> iterator = colection.iterator();
				                                                    for (int i = 0; i < colection.size(); i++) {
					                                                    if (iterator.next().contains(node)) {
						                                                    return colors[i];
					                                                    }
				                                                    }
				                                                    return Color.white;
			                                                    }
		                                                    }
		);
		vv.getRenderContext().setVertexShapeTransformer(new Transformer<Node, Shape>() {

			                                                @Override
			                                                public Shape transform(Node arg0) {
				                                                if (arg0.isGateway()) {
					                                                new Ellipse2D.Double(-15, -15, 30, 30);
				                                                }
				                                                Collection<Set<Node>> colection = KmeansClustering.currentClustering.values();
				                                                Set<Node> meansColection = KmeansClustering.currentClustering.keySet();

				                                                Iterator<Set<Node>> iterator = colection.iterator();
				                                                for (int i = 0; i < colection.size(); i++) {
					                                                if (iterator.next().contains(arg0)) {
						                                                if (meansColection.contains(arg0)) {
							                                                return new Rectangle(-15, -15, 30, 30);
						                                                }
						                                                return new Ellipse2D.Double(-15, -15, 30, 30);
					                                                }
				                                                }
				                                                return new Ellipse2D.Double(-15, -15, 30, 30);
			                                                }
		                                                }
		);
		vv.repaint();
	}
}
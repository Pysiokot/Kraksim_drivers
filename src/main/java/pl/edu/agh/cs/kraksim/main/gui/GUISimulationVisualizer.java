package pl.edu.agh.cs.kraksim.main.gui;

import org.apache.log4j.Logger;

import pl.edu.agh.cs.kraksim.KraksimConfigurator;
import pl.edu.agh.cs.kraksim.core.City;
import pl.edu.agh.cs.kraksim.core.Lane;
import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.iface.block.BlockIView;
import pl.edu.agh.cs.kraksim.iface.carinfo.CarInfoCursor;
import pl.edu.agh.cs.kraksim.iface.carinfo.CarInfoIView;
import pl.edu.agh.cs.kraksim.iface.carinfo.LaneCarInfoIface;
import pl.edu.agh.cs.kraksim.main.UpdateHook;
import pl.edu.agh.cs.kraksim.ministat.*;
import pl.edu.agh.cs.kraksim.sna.SnaConfigurator;
import pl.edu.agh.cs.kraksim.sna.centrality.CentrallityStatistics;
import pl.edu.agh.cs.kraksim.statistics.StatsPanel;
import pl.edu.agh.cs.kraksim.visual.VisualizerComponent;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

@SuppressWarnings("serial")
public class GUISimulationVisualizer implements SimulationVisualizer {
	private static final Logger LOGGER = Logger.getLogger(GUISimulationVisualizer.class);
	private static final Logger LOGGER2 = Logger.getLogger(StatsPanel.class);
	private static final Logger LOGGER3 = Logger.getLogger(CentrallityStatistics.class);
	private static final Logger LOGGER4 = Logger.getLogger(GatewayMiniStatExt.class);
	private static final Logger LOGGER5 = Logger.getLogger(LastPeriodAvgDuration.class);
	private static final Logger LOGGER6 = Logger.getLogger(LastPeriodAvgVelocity.class);
	private static final Logger LOGGER7 = Logger.getLogger(LastPeriodCarCount.class);
	private static final Logger LOGGER8 = Logger.getLogger(LinkMiniStatExt.class);
	private static final Logger LOGGER9 = Logger.getLogger(MiniStatModuleCreator.class);
	private static final Logger LOGGER10 = Logger.getLogger(RouteStat.class);
	private final VisualizerComponent visualizerComponent;
	private final List<UpdateHook> hooks;
	private final City city;
	private final CarInfoIView carInfoView;
	public transient CityMiniStatExt cityStat;
	Container controlPane;
	private JLabel phaseDisp;
	private JLabel turnDisp;
	private JLabel carCountDisp;
	private JLabel travelCountDisp;
	private JLabel avgVelocityDisp;
	private int refreshPeriod;
	private int turnDelay;

	public GUISimulationVisualizer(City city, CarInfoIView carInfoView, BlockIView blockView, MiniStatEView statView) {
		// setToolTipText( "kraksim" );
		this.city = city;
		this.carInfoView = carInfoView;

		cityStat = statView.ext(city);

		visualizerComponent = createVisualizator();
		controlPane = createControlPane(visualizerComponent);

		visualizerComponent.loadMap(city, carInfoView, blockView, statView);

		hooks = new LinkedList<>();
	}

	/**
	 * @return
	 */
	private static VisualizerComponent createVisualizator() {
		VisualizerComponent visComp = new VisualizerComponent();
		JScrollPane scroller = new JScrollPane(visComp, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroller.setPreferredSize(new Dimension(600, 400));
		scroller.setMinimumSize(new Dimension(600, 100));
		scroller.setMaximumSize(new Dimension(1600, 1200));

		return visComp;
	}

	/**
	 * @return
	 */
	private Container createControlPane(final VisualizerComponent visualizerComponent) {
		Container ctrllPane = Box.createHorizontalBox();
		ctrllPane.setPreferredSize(new Dimension(600, 55));
		ctrllPane.setMinimumSize(new Dimension(600, 55));
		ctrllPane.setMaximumSize(new Dimension(1600, 55));

		phaseDisp = new JLabel("START", SwingConstants.CENTER);
		turnDisp = new JLabel();
		carCountDisp = new JLabel();
		travelCountDisp = new JLabel();
		avgVelocityDisp = new JLabel();
		resetStats();

		ctrllPane.add(wrap("phase", phaseDisp));
		ctrllPane.add(wrap("turn", turnDisp));
		ctrllPane.add(wrap("car count", carCountDisp));
		ctrllPane.add(wrap("travel count", travelCountDisp));
		ctrllPane.add(wrap("avg. V (of ended travels)", avgVelocityDisp));

		ctrllPane.add(Box.createVerticalGlue());

		JSlider zoomSlider = new JSlider(new DefaultBoundedRangeModel(40, 0, 20, 400));
		zoomSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider slider = (JSlider) e.getSource();
				float zoom = slider.getValue() / 100.0f;
				visualizerComponent.setScale(zoom);
			}
		});

		ctrllPane.add(wrap("zoom", zoomSlider));

		JSlider refreshPeriodSlider = new JSlider(new DefaultBoundedRangeModel(1, 0, 1, 100));
		refreshPeriodSlider.setToolTipText("period between refreshes (smaller is faster)");
		refreshPeriodSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider slider = (JSlider) e.getSource();
				refreshPeriod = slider.getValue();
			}
		});
		ctrllPane.add(wrap("refresh period", refreshPeriodSlider));
		refreshPeriod = 1;	//for testing may be 100

		JSlider turnDelaySlider = new JSlider(new DefaultBoundedRangeModel(25, 0, 0, 1000));
		turnDelaySlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider slider = (JSlider) e.getSource();
				turnDelay = slider.getValue();
			}
		});
		ctrllPane.add(wrap("turn delay", turnDelaySlider));
		turnDelay = 25;

		return ctrllPane;
	}

	private void resetStats() {
		turnDisp.setText("0");
		carCountDisp.setText("0");
		travelCountDisp.setText("0");
		avgVelocityDisp.setText("-");
	}

	private static Box wrap(String title, JComponent component) {
		component.setToolTipText(title);
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(component);
		box.add(Box.createHorizontalGlue());
		box.setBorder(BorderFactory.createTitledBorder(title));
		return box;
	}

	public void startLearningPhase(int phaseNum) {
		phaseDisp.setText("LEARNING " + (phaseNum + 1));
	}

	public void startTestingPhase() {
		phaseDisp.setText("TESTING");
	}

	public void endPhase() {
		resetStats();
	}

	public void end(long elapsed) {
	}

	public void update(int turn) {
		if (turnDelay > 0) {
			try {
				Thread.sleep(turnDelay);
			} catch (InterruptedException e) {
				LOGGER.error("InterruptedException", e);
			}
		}

		if (turn % refreshPeriod == 0) {
			visualizerComponent.update();
			turnDisp.setText(String.valueOf(turn));
			carCountDisp.setText(String.valueOf(cityStat.getCarCount()));
			travelCountDisp.setText(String.valueOf(cityStat.getTravelCount()));
			avgVelocityDisp.setText(String.format("%5.2f", cityStat.getAvgTurnCarVelocity()));
			runUpdateHooks(cityStat);
		}

		if (turn % Integer.parseInt(KraksimConfigurator.getProperty("statisticsDumpToFile")) == 0) {
			cityStat.getAvgCarSpeed();
			//LOGGER.info(turn + ";" + cityStat.getAvgVelocity() + ";"  + cityStat.getAvgCarSpeed());
			LOGGER.info(turn + "," + cityStat.getAvgVelocity());
			LOGGER2.info(turn + "," + cityStat.getAllCarsOnRedLight());
			LOGGER3.info(turn + "," + cityStat.getCarCount());
			LOGGER4.info(turn + "," + cityStat.getAvgEmergencyVehiclesVelocity());
			LOGGER5.info(turn + "," + cityStat.getAvgNormalCarsVelocity());
			LOGGER6.info(turn + "," + cityStat.getEmergencyVehiclesOnRedLight());
			LOGGER7.info(turn + "," + cityStat.getNormalCarsOnRedLight());
			LOGGER8.info(turn + "," + cityStat.getEmergencyVehiclesCount());
			LOGGER9.info(turn + "," + cityStat.getNormalCarsCount());
			LOGGER10.info(turn + "," + cityStat.getAvgTurnCarVelocity());
		}

		//Centrallity stats
		if (turn % SnaConfigurator.getSnaRefreshInterval() == 0) {
			try {
				CentrallityStatistics.writeTravelTimeData(cityStat, turn);
				CentrallityStatistics.writeKlasteringInfo(turn);
			} catch (Exception e) {
				LOGGER.error("Cannot update statistics.", e);
			}
		}
	}

	private void runUpdateHooks(CityMiniStatExt cityStat) {
		for (UpdateHook h : hooks) {
			h.onUpdate(cityStat);
		}
	}

	public void addUpdateHook(UpdateHook h) {
		hooks.add(h);
	}

	public void createWindow() {
		String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
		try {
			UIManager.setLookAndFeel(lookAndFeel);
		} catch (Exception e) {
			// on error, we get default swing look and feel
		}

		final JPanel panel = new JPanel();
		panel.add(controlPane);
		panel.add(visualizerComponent);

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame("Test");
				frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				frame.getContentPane().add(panel);
				frame.setSize(350, 250);
				frame.setVisible(true);
			}
		});
	}

	public Container getControlPane() {
		return controlPane;
	}

	public VisualizerComponent getVisualizerComponent() {
		return visualizerComponent;
	}

	public long getNumberOfCarBelowValue(double value) {
		long carWithVelocityBelow = 0;
		Iterator<Link> iterator = city.linkIterator();
		while (iterator.hasNext()) {
			Link link = iterator.next();
			for (int lineNum = 0; lineNum < link.laneCount(); lineNum++) {
				Lane lane = link.getLaneAbs(lineNum);
				LaneCarInfoIface laneCarInfo = carInfoView.ext(lane);
				//LaneBlockIface laneBlock = blockView.ext(lane);

				// Liczenie zwykłej średniej prędkości.
				CarInfoCursor infoForwardCursor = laneCarInfo.carInfoForwardCursor();
				while (infoForwardCursor != null && infoForwardCursor.isValid()) {
					try {
						if (infoForwardCursor.currentVelocity() < value) {
							carWithVelocityBelow++;
						}
					} catch (NoSuchElementException e) {
						LOGGER.error("NoSuchElementException", e);
					}
					infoForwardCursor.next();
				}
			}
		}

		return carWithVelocityBelow;
	}
}

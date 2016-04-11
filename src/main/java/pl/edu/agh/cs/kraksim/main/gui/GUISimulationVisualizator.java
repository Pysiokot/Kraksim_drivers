package pl.edu.agh.cs.kraksim.main.gui;

import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.core.City;
import pl.edu.agh.cs.kraksim.core.Lane;
import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.iface.block.BlockIView;
import pl.edu.agh.cs.kraksim.iface.carinfo.CarInfoCursor;
import pl.edu.agh.cs.kraksim.iface.carinfo.CarInfoIView;
import pl.edu.agh.cs.kraksim.iface.carinfo.LaneCarInfoIface;
import pl.edu.agh.cs.kraksim.main.UpdateHook;
import pl.edu.agh.cs.kraksim.ministat.CityMiniStatExt;
import pl.edu.agh.cs.kraksim.ministat.MiniStatEView;
import pl.edu.agh.cs.kraksim.sna.SnaConfigurator;
import pl.edu.agh.cs.kraksim.sna.centrality.CentrallityStatistics;
import pl.edu.agh.cs.kraksim.visual.VisualizatorComponent;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

@SuppressWarnings("serial")
public class GUISimulationVisualizator implements SimulationVisualizator {
	private static final Logger LOGGER = Logger.getLogger(GUISimulationVisualizator.class);
	private final VisualizatorComponent visualizatorComponent;
	private final List<UpdateHook> hooks;
	private final City city;
	private final CarInfoIView carInfoView;
	public transient CityMiniStatExt cityStat;
	Container controllPane;
	private JLabel phaseDisp;
	private JLabel turnDisp;
	private JLabel carCountDisp;
	private JLabel travelCountDisp;
	private JLabel avgVelocityDisp;
	private int refreshPeriod;
	private int turnDelay;

	public GUISimulationVisualizator(City city, CarInfoIView carInfoView, BlockIView blockView, MiniStatEView statView) {
		// setToolTipText( "kraksim" );
		this.city = city;
		this.carInfoView = carInfoView;

		cityStat = statView.ext(city);

		visualizatorComponent = createVisualizator();
		controllPane = createControlPane(visualizatorComponent);

		visualizatorComponent.loadMap(city, carInfoView, blockView, statView);

		hooks = new LinkedList<>();
	}

	/**
	 * @return
	 */
	private static VisualizatorComponent createVisualizator() {
		VisualizatorComponent visComp = new VisualizatorComponent();
		JScrollPane scroller = new JScrollPane(visComp, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroller.setPreferredSize(new Dimension(600, 400));
		scroller.setMinimumSize(new Dimension(600, 100));
		scroller.setMaximumSize(new Dimension(1600, 1200));

		return visComp;
	}

	/**
	 * @return
	 */
	private Container createControlPane(final VisualizatorComponent visualizatorComponent) {
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
				visualizatorComponent.setScale(zoom);
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
			visualizatorComponent.update();
			turnDisp.setText(String.valueOf(turn));
			carCountDisp.setText(String.valueOf(cityStat.getCarCount()));
			travelCountDisp.setText(String.valueOf(cityStat.getTravelCount()));
			avgVelocityDisp.setText(String.format("%5.2f", cityStat.getAvgVelocity()));
			runUpdateHooks(cityStat);
		}

		if (turn % 100 == 0) {
			//LOGGER.info(turn + ";" + cityStat.getAvgVelocity() + ";"  + cityStat.getAvgCarSpeed());
			LOGGER.info(turn + "," + cityStat.getAvgVelocity());
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
		panel.add(controllPane);
		panel.add(visualizatorComponent);

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

	public Container getControllPane() {
		return controllPane;
	}

	public VisualizatorComponent getVisualizatorComponent() {
		return visualizatorComponent;
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

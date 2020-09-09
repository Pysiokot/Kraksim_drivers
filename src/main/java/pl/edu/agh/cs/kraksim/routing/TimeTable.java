package pl.edu.agh.cs.kraksim.routing;

import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.core.City;
import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.iface.Clock;
import pl.edu.agh.cs.kraksim.ministat.LinkMiniStatExt;
import pl.edu.agh.cs.kraksim.ministat.MiniStatEView;
import pl.edu.agh.cs.kraksim.routing.prediction.ITrafficPredictor;
import pl.edu.agh.cs.kraksim.routing.prediction.TrafficPredictionFactory;
import pl.edu.agh.cs.kraksim.routing.prediction.WorldState;

import java.util.Iterator;

public class TimeTable implements ITimeTable {
	private static final Logger LOGGER = Logger.getLogger(TimeTable.class);
	private static final int MINUTE = 60;
	private double[] timeArray;
	private double[] worldState;
	private final MiniStatEView statView;
	private final Clock clock;
	// private Object refreshLock = new Object();
	private long lastUpdate = -1;
	private long updatePeriod = MINUTE * 5;
	private final City city;
	private final ITrafficPredictor predictor;

	public TimeTable(City city, MiniStatEView statView, Clock clock, long updatePeriod) {
		this.city = city;
		this.statView = statView;
		this.clock = clock;
		this.updatePeriod = updatePeriod/3;
		predictor = TrafficPredictionFactory.getTrafficPredictor();
	}

	public double getTime(Link v) {
		// synchronized (refreshLock) {
		if (refreshNeeded()) {
			refreshAll();
		}
		// }
		// float dur = statView.ext( v ).getAvgDuration();
		// return v.getLength() * 2 + dur;
		return getLinkTime(v) + 10;
	}

	public double getLinkTime(Link v) {
		// Double value = timeMap.get( v.getId() );
		return timeArray[v.getLinkNumber()];
	}

	private void refreshAll() {
		LOGGER.info("refreshing timetable");
		timeArray = new double[city.linkCount()];
		worldState = new double[city.linkCount()];

		for (Iterator<Link> it = city.linkIterator(); it.hasNext(); ) {
			Link link = it.next();
			worldState[link.getLinkNumber()] = -1;
			refreshLink(link);
		}

		LOGGER.debug("* Predykcja " + clock.getTurn());
		predictor.appendWorldState(new WorldState(worldState));
		predictor.adjustCurrentWeightsOfLink(timeArray);
	}

	private void refreshLink(Link link) {
		LinkMiniStatExt lmse = statView.ext(link);
		int linkNo = link.getLinkNumber();

		double avgDuration = lmse.getLastPeriodAvgDuration();

		if (worldState[linkNo] != -1) {
			avgDuration = avgDuration + timeArray[linkNo];
			avgDuration /= 2;
		}

		timeArray[linkNo] = worldState[linkNo] = avgDuration;
		// avgDuration==0. if no cars passed recently - so we shall
		// set it manually to the minimal value
		if (avgDuration == 0.) {
			avgDuration = link.getLength() / link.getSpeedLimit();

			timeArray[linkNo] = avgDuration;
			// but if no cars had yet entered this road, it shall be implied
			// by negative value in the world state (for prediction)
			if (lmse.getDriveCount() < 1) {
				worldState[linkNo] = -1.;
			} else {
				worldState[linkNo] = avgDuration;
			}
		}
	}

	private boolean refreshNeeded() {
		boolean refresh = false;
		int currentTime = clock.getTurn();

		if (lastUpdate < 0) {
			refresh = true;
			lastUpdate = currentTime;
		} else {
			long difference = currentTime - lastUpdate;

			if (difference > updatePeriod) {
				lastUpdate = currentTime;
				refresh = true;
			}
		}

		return refresh;
	}
}

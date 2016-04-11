package pl.edu.agh.cs.kraksim.routing;

import pl.edu.agh.cs.kraksim.core.City;
import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.iface.Clock;
import pl.edu.agh.cs.kraksim.weka.WekaPredictionModule;

import java.util.Iterator;

public class TimeTableRules implements ITimeTable {
	private static final long timeArrayUpdatePeriod = 300;

	private double[] timeArray;
	private final Clock clock;

	private final City city;
	private long timeArrayLastUpdate = -1;
	private final WekaPredictionModule predictor;

	public TimeTableRules(City city, Clock clock, WekaPredictionModule predictionModule) {
		this.city = city;
		this.clock = clock;
		predictor = predictionModule;
	}

	public double getTime(Link link) {
		if (timeArrayRefreshNeeded()) {
			refreshTimeArray();
		}
		return getLinkTime(link);
	}

	/*
	 * Refresh array of values used in dijktry algorithm
	 */
	private void refreshTimeArray() {
		timeArray = new double[city.linkCount()];

		for (Iterator<Link> it = city.linkIterator(); it.hasNext(); ) {
			Link link = it.next();
			refreshLink(link);
		}
	}

	private void refreshLink(Link link) {
		int linkNumber = link.getLinkNumber();
		double avgDuration = predictor.getLastPeriodAvgDurationForLink(linkNumber);
		// LinkMiniStatExt lmse = statView.ext(link);
		// double avgDuration = lmse.getLastPeriodAvgDuration();

		avgDuration = avgDuration + timeArray[linkNumber];
		avgDuration /= 2;

		timeArray[linkNumber] = avgDuration;
		// avgDuration==0. if no cars passed recently - so we shall
		// set it manually to the minimal value
		if (avgDuration == 0.) {
			avgDuration = link.getLength() / link.getSpeedLimit();
			timeArray[linkNumber] = avgDuration;
		}
	}

	private boolean timeArrayRefreshNeeded() {
		boolean refresh = false;
		int currentTime = clock.getTurn();

		if (timeArrayLastUpdate < 0) {
			refresh = true;
			timeArrayLastUpdate = currentTime;
		} else {
			long difference = currentTime - timeArrayLastUpdate;

			if (difference > timeArrayUpdatePeriod) {
				timeArrayLastUpdate = currentTime;
				refresh = true;
			}
		}

		return refresh;
	}

	public double getLinkTime(Link link) {
		return predictor.predictAvgDuration(link, timeArray[link.getLinkNumber()]);
	}
}

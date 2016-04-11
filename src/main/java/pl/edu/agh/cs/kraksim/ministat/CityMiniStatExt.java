package pl.edu.agh.cs.kraksim.ministat;

import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.core.City;
import pl.edu.agh.cs.kraksim.core.Gateway;
import pl.edu.agh.cs.kraksim.core.Link;

import java.util.Iterator;

public class CityMiniStatExt {
	private static final Logger LOGGER = Logger.getLogger(CityMiniStatExt.class);
	private final City city;
	private final MiniStatEView ev;
	private final StatHelper helper;

	private long allCarsOnRedLight;

	CityMiniStatExt(City city, MiniStatEView ev, StatHelper helper) {
		this.city = city;
		this.ev = ev;
		this.helper = helper;
		LOGGER.trace(" ");
	}

	public void clear() {
		LOGGER.trace(" ");
		helper.clear();

		for (Iterator<Gateway> iter = city.gatewayIterator(); iter.hasNext(); ) {
			ev.ext(iter.next()).clear();
		}

		for (Iterator<Link> iter = city.linkIterator(); iter.hasNext(); ) {
			ev.ext(iter.next()).clear();
		}
	}

	public int getCarCount() {
		LOGGER.trace("CarCount=" + helper.getCityCarCount());
		return helper.getCityCarCount();
	}

	public int getTravelCount() {
		LOGGER.trace("TravelCount=" + helper.getCityTravelCount());
		return helper.getCityTravelCount();
	}

	public float getAvgVelocity() {
		LOGGER.trace("AverageVelocity=" + helper.getCityAvgVelocity());
		LOGGER.info(helper.getCityAvgVelocity()); //TODO
		return helper.getCityAvgVelocity();
	}

	//TODO:
	public float getTravelLength() {
		return helper.getCityTravelLength();
	}

	public float getTravelDuration() {
		return helper.getCityTravelDuration();
	}

	public float getAvgCarSpeed() {
		return helper.getCityAvgCarSpeed();
	}

	public double getAvgCarLoad() {
		double avgLoad = 0;
		Iterator<Link> iter = city.linkIterator();
		while (iter.hasNext()) {
			avgLoad += iter.next().getLoad();
		}
		return avgLoad / city.linkCount();
	}

	public long getAllCarsOnRedLight() {
		return allCarsOnRedLight;
	}

	public void setAllCarsOnRedLight(long allCarsOnRedLight) {
		this.allCarsOnRedLight = allCarsOnRedLight;
	}
}

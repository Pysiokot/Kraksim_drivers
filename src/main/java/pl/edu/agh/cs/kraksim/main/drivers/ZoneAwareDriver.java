package pl.edu.agh.cs.kraksim.main.drivers;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.real_extended.DriverType;
import pl.edu.agh.cs.kraksim.routing.NoRouteException;
import pl.edu.agh.cs.kraksim.routing.Router;
import pl.edu.agh.cs.kraksim.traffic.TravellingScheme;

import java.util.ListIterator;

public class ZoneAwareDriver extends Driver {
	private static final Logger LOGGER = Logger.getLogger(ZoneAwareDriver.class);
	private final DriverZones allowedZones;
	private final DecisionHelper decisionHelper;

	public ZoneAwareDriver(int id, TravellingScheme scheme, Router router, boolean emergency, DriverType driverType, DecisionHelper decisionHelper, DriverZones allowedZones) {
		super(id, scheme, router, emergency, driverType);
		this.allowedZones = allowedZones;
		this.decisionHelper = decisionHelper;
	}

	public ListIterator<Link> updateRouteFrom(Link sourceLink) {
		if (router != null){//changeRouteTh(driverType.getRouteChangeProbability()*100)) {
			try {
			return router.getRouteIncludingZone(sourceLink, destGateway(), allowedZones).linkIterator();
		} catch (NoRouteException e) {
			LOGGER.warn("No route", e);
		}}
		return null;
	}

	private boolean changeRoute() {
		boolean decision = false;

		if (decisionHelper != null) {
			decision = decisionHelper.decide();
		}

		return decision;
	}

	private boolean changeRouteTh(double th) {
		boolean decision = false;

		if (decisionHelper != null) {
			decision = decisionHelper.decide((int)th);
		}

		return decision;
	}

	@Override
	public DriverZones getAllowedZones() {
		return allowedZones;
	}
}

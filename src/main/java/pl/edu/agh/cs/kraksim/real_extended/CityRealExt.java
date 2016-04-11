package pl.edu.agh.cs.kraksim.real_extended;

import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.core.City;
import pl.edu.agh.cs.kraksim.core.Gateway;
import pl.edu.agh.cs.kraksim.core.Intersection;
import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.iface.block.CityBlockIface;
import pl.edu.agh.cs.kraksim.iface.sim.CitySimIface;
import pl.edu.agh.cs.kraksim.iface.sim.Route;
import pl.edu.agh.cs.kraksim.iface.sim.TravelEndHandler;
import pl.edu.agh.cs.kraksim.main.drivers.Driver;

import java.util.Iterator;

class CityRealExt implements CitySimIface, CityBlockIface {
	private static final Logger LOGGER = Logger.getLogger(CityRealExt.class);

	private final City city;
	private final RealEView evalView;

	CityRealExt(City city, RealEView evalView) {
		LOGGER.trace("Constructing City.");
		this.city = city;
		this.evalView = evalView;
	}

	public void setCommonTravelEndHandler(final TravelEndHandler handler) {
		for (Iterator<Gateway> iter = city.gatewayIterator(); iter.hasNext(); ) {
			evalView.ext(iter.next()).setTravelEndHandler(handler);
		}
	}

	public void insertTravel(final Driver driver, final Route route, final boolean rerouting) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace(route);
		}
// TODO: routing configuration
		evalView.ext(route.getSource()).enqueueCar(new Car(driver, route, rerouting));
	}

	public void simulateTurn() {
		LOGGER.trace("TURN STARTED");
		LOGGER.trace("TURN - PREPARE");
		for (Iterator<Link> iter = city.linkIterator(); iter.hasNext(); ) {
			evalView.ext(iter.next()).prepareTurnSimulation();
		}

		LOGGER.trace("TURN - FIND CARS");
		for (Iterator<Intersection> iter = city.intersectionIterator(); iter.hasNext(); ) {
			evalView.ext(iter.next()).findApproachingCars();
		}

		LOGGER.trace("TURN - SIMULATE on Gateways");
		for (Iterator<Gateway> iter = city.gatewayIterator(); iter.hasNext(); ) {
			evalView.ext(iter.next()).simulateTurn();
		}

		LOGGER.trace("TURN - SIMULATE on Links");
		for (Iterator<Link> iter = city.linkIterator(); iter.hasNext(); ) {
			evalView.ext(iter.next()).simulateTurn();
		}

		LOGGER.trace("TURN - FINALIZE on Links");
		for (Iterator<Link> iter = city.linkIterator(); iter.hasNext(); ) {
			evalView.ext(iter.next()).finalizeTurnSimulation();
		}

		LOGGER.trace("TURN - FINALIZE on Gateways");
		for (Iterator<Gateway> iter = city.gatewayIterator(); iter.hasNext(); ) {
			evalView.ext(iter.next()).finalizeTurnSimulation();
		}
	}

	public void blockAllLinks() {
		for (Iterator<Link> iter = city.linkIterator(); iter.hasNext(); ) {
			evalView.ext(iter.next()).block();
		}
	}

	public void unblockAllLinks() {
		for (Iterator<Link> iter = city.linkIterator(); iter.hasNext(); ) {
			evalView.ext(iter.next()).unblock();
		}
	}
}

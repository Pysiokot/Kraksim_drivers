package pl.edu.agh.cs.kraksim.real_extended;

import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.AssumptionNotSatisfiedException;
import pl.edu.agh.cs.kraksim.core.Action;
import pl.edu.agh.cs.kraksim.core.Lane;
import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.core.exceptions.ExtensionCreationException;
import pl.edu.agh.cs.kraksim.iface.block.LinkBlockIface;
import pl.edu.agh.cs.kraksim.iface.mon.CarDriveHandler;
import pl.edu.agh.cs.kraksim.iface.mon.LinkMonIface;

import java.util.List;

class LinkRealExt implements LinkBlockIface, LinkMonIface {
	private static final Logger LOGGER = Logger.getLogger(LinkRealExt.class);
	protected final Link link;
	protected final RealEView ev;

	LinkRealExt(Link link, RealEView ev, RealSimulationParams params) throws ExtensionCreationException {
		LOGGER.trace("Creating.");
		if (link.getLength() < params.priorLaneTimeHeadway * params.maxVelocity) {
			throw new ExtensionCreationException(String.format("real module requires link ls at least %d cells long", link.getLength()));
		}

		this.link = link;
		this.ev = ev;
	}

	void prepareTurnSimulation() {
		LOGGER.trace(link);

		for (int i = 0; i < laneCount(); i++) {
			laneExt(i).prepareTurnSimulation();
		}
	}

	private int laneCount() {
		return link.laneCount();
	}

	/* in absolute numbering, from left to right, starting fom 0 */
	private LaneRealExt laneExt(int n) {
		return ev.ext(link.getLaneAbs(n));
	}

	/* intersection link only */
	public void findApproachingCars() {
		LOGGER.trace(link);

		for (int i = 0; i < laneCount(); i++) {
			laneExt(i).findApproachingCar();
		}
	}

	/* assumption: stepsDone < stepsMax */
	boolean enterCar(Car car, int stepsMax, int stepsDone) {
		LOGGER.trace(car + " stepsDone:" + stepsDone + " stepsMax: " + stepsMax);
		// obtaining next goal of the entered car
		Link nextLink = null;
		if (car.hasNextTripPoint()) {
			nextLink = car.peekNextTripPoint();
		} else {
			// if there is no next point, it means, that the car
			// is heading to a gateway. If this link does not lead 
			// to a gateway - time to throw some exception...

			if (!link.getEnd().isGateway()) {
				throw new AssumptionNotSatisfiedException();
			}
		}

		// obtaining list of actions heading to the given destination
		List<Action> actions = link.findActions(nextLink);

		MultiLaneRoutingHelper laneHelper = new MultiLaneRoutingHelper(ev);

		// choosing the best action from the given list
		Action nextAction = laneHelper.chooseBestAction(actions, link);
		// choosing the best lane to enter in order to get to lane given in action 
		Lane nextLane = laneHelper.chooseBestLaneForAction(nextAction, link);

		// if no such a lane, just put it into the main lane...
		if (nextLane == null) {
			nextLane = link.getMainLane(0);
		}

		LaneRealExt l = ev.ext(nextLane);
		if (l.hasCarPlace()) {
			car.refreshTripRoute();

			if (!car.hasNextTripPoint()) {
				car.setAction(null);
			} else {
				car.nextTripPoint();
				car.setAction(nextAction);
				car.setPreferableAction(nextAction);
			}

			return l.pushCar(car, stepsMax, stepsDone);
		} else {
			return false;
		}
	}

	void simulateTurn() {
		LOGGER.trace(link);

		for (int i = 0; i < laneCount(); i++) {
			laneExt(i).simulateTurn();
		}
	}

	void finalizeTurnSimulation() {
		LOGGER.trace(link);

		for (int i = 0; i < laneCount(); i++) {
			laneExt(i).finalizeTurnSimulation();
		}
	}

	public void block() {
		for (int i = 0; i < laneCount(); i++) {
			laneExt(i).block();
		}
	}

	public void unblock() {
		for (int i = 0; i < laneCount(); i++) {
			laneExt(i).unblock();
		}
	}

	public void installInductionLoops(int line, CarDriveHandler handler) throws IndexOutOfBoundsException {
		if (line < 0 || line > link.getLength()) {
			throw new IndexOutOfBoundsException("line = " + line);
		}

		for (int i = 0; i < laneCount(); i++) {
			LaneRealExt l = laneExt(i);
			if (line >= l.getOffset()) {
				laneExt(i).installInductionLoop(line, handler);
			}
		}
	}
}

package pl.edu.agh.cs.kraksim.real_extended;

import pl.edu.agh.cs.kraksim.AssumptionNotSatisfiedException;
import pl.edu.agh.cs.kraksim.core.Action;
import pl.edu.agh.cs.kraksim.core.Gateway;
import pl.edu.agh.cs.kraksim.core.Lane;
import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.iface.mon.CarEntranceHandler;
import pl.edu.agh.cs.kraksim.iface.mon.CarExitHandler;
import pl.edu.agh.cs.kraksim.iface.mon.GatewayMonIface;
import pl.edu.agh.cs.kraksim.iface.sim.GatewaySimIface;
import pl.edu.agh.cs.kraksim.iface.sim.TravelEndHandler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

class GatewayRealExt extends NodeRealExt implements GatewaySimIface, GatewayMonIface {
	private final Gateway gateway;
	private final List<CarEntranceHandler> entranceHandlers;
	private final List<CarExitHandler> exitHandlers;
	private final LinkedList<Car> cars;
	private TravelEndHandler travelEndHandler;
	private int enqueuedCarCount;
	// CHANGE: MZA: to enable multiple lanes and multiple cars
	// leaving each link
	private List<Car> acceptedCars = null;

	GatewayRealExt(Gateway gateway, RealEView ev) {
		super(ev);
		this.gateway = gateway;
		entranceHandlers = new ArrayList<>();
		exitHandlers = new ArrayList<>();

		cars = new LinkedList<>();

		enqueuedCarCount = 0;
		// CHANGE: MZA: to enable multiple lanes
		acceptedCars = new LinkedList<>();
	}

	void enqueueCar(Car car) {
		cars.add(car);
		enqueuedCarCount++;
	}

	public void setTravelEndHandler(TravelEndHandler handler) {
		travelEndHandler = handler;
	}
	
	void simulateTurn() {
		ListIterator<Car> iter = cars.listIterator(cars.size());
		while (enqueuedCarCount > 0) {
			Object d = iter.previous().getDriver();
			for (CarEntranceHandler h : entranceHandlers) {	// no clue what CarEntranceHandler does
				h.handleCarEntrance(d);	// handles new car entrance for each new car in "cars" list
			}

			enqueuedCarCount--;
		}
		for(int i=0; i<gateway.getOutboundLink().mainLaneCount(); i++) {
			Car car = cars.peek();
			// adds 1 car per turn if possible
			if (car != null && gateway.getOutboundLink() != null) {
				Lane targetLaneNormal = gateway.getOutboundLink().getMainLane(i);
				LaneRealExt targetLane;
				targetLane = ev.ext(targetLaneNormal);
				if(targetLane.canAddCarToLane(car)) {
					targetLane.addCarToLane(car);
					car.setCurrentLane(targetLane);
					car.refreshTripRoute();
					
					Link nextLink = null;
					if (car.hasNextTripPoint()) {
						nextLink = car.peekNextTripPoint();
					} else {
					}
					List<Action> actions = gateway.getOutboundLink().findActions(nextLink);
					MultiLaneRoutingHelper laneHelper = new MultiLaneRoutingHelper(ev);
					Action nextAction = laneHelper.chooseBestAction(actions);
					
					if (!car.hasNextTripPoint()) {
						car.setActionForNextIntersection(null);
					} else {
						car.nextTripPoint();
						car.setActionForNextIntersection(nextAction);
					}
					cars.poll();
					ev.ext(gateway.getOutboundLink()).fireAllEntranceHandlers(car);
				}
			}
		}
	}

	public TravelEndHandler getTravelEndHandler() {
		return travelEndHandler;
	}

	void acceptCar(Car car) {
		// CHANGE: MZA: to enable multiple lanes
		acceptedCars.add(car);
	}

	void finalizeTurnSimulation() {
		// CHANGE: MZA: to enable multiple lanes
		if (!acceptedCars.isEmpty()) {
			for (Car car : acceptedCars) {
				ev.ext(gateway.getInboundLink()).fireAllExitHandlers(car);
				for (CarExitHandler h : exitHandlers) {
					h.handleCarExit(car.getDriver());
				}

				if (travelEndHandler != null) {
					travelEndHandler.handleTravelEnd(car.getDriver());
				}
			}

			acceptedCars.clear();
		}
	}

	public void blockInboundLinks() {
		ev.ext(gateway.getInboundLink()).block();
	}

	public void unblockInboundLinks() {
		ev.ext(gateway.getInboundLink()).unblock();
	}

	public void installEntranceSensor(CarEntranceHandler handler) {
		entranceHandlers.add(handler);
	}

	public void installExitSensor(CarExitHandler handler) {
		exitHandlers.add(handler);
	}
}

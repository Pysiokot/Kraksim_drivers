package pl.edu.agh.cs.kraksim.real;

import pl.edu.agh.cs.kraksim.core.Gateway;
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
			for (CarEntranceHandler h : entranceHandlers) {
				h.handleCarEntrance(d);
			}

			enqueuedCarCount--;
		}

		Car car = cars.peek();
		if (car != null && gateway.getOutboundLink() != null && ev.ext(gateway.getOutboundLink()).enterCar(car, 1, 0)) {
			cars.poll();
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

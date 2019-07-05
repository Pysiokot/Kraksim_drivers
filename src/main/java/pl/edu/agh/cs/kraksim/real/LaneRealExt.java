package pl.edu.agh.cs.kraksim.real;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.KraksimConfigurator;
import pl.edu.agh.cs.kraksim.core.Action;
import pl.edu.agh.cs.kraksim.core.Lane;
import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.core.Node;
import pl.edu.agh.cs.kraksim.iface.block.LaneBlockIface;
import pl.edu.agh.cs.kraksim.iface.carinfo.CarInfoCursor;
import pl.edu.agh.cs.kraksim.iface.carinfo.LaneCarInfoIface;
import pl.edu.agh.cs.kraksim.iface.mon.CarDriveHandler;
import pl.edu.agh.cs.kraksim.iface.mon.LaneMonIface;
import pl.edu.agh.cs.kraksim.main.CarMoveModel;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class LaneRealExt implements LaneBlockIface, LaneCarInfoIface, LaneMonIface {
	private static final Logger LOGGER = Logger.getLogger(LaneRealExt.class);
	private final String emergencyVehiclesConfiguration;

	private final Lane lane;
	private final RealEView realView;
	private final RealSimulationParams params;
	private final int offset;
	// MZA: multi-lanes. It had to be changed.
	private final List<Car> enteringCars = new LinkedList<>();
	private final List<InductionLoop> loops;
	private final int speedLimit;
	private final int emergencySpeedLimit;
	private final int emergencySpeedLimitTimesHigher;
	private final int emergencyAcceleration;
	private final CarMoveModel carMoveModel;
	private boolean blocked;
	private int firstCarPos;
	private boolean carApproaching;
	private boolean wait;
	LinkedList<Car> cars;

	LaneRealExt(Lane lane, RealEView ev, RealSimulationParams params) {
		LOGGER.trace("Constructing LaneRealExt ");
		emergencyVehiclesConfiguration = KraksimConfigurator.getPropertiesFromFile().getProperty("emergencyVehiclesConfiguration");
		Properties properties = new Properties();
		try {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(emergencyVehiclesConfiguration));
			properties.load(bis);
			bis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.emergencySpeedLimitTimesHigher = Integer.valueOf(properties.getProperty("emergencySpeedLimitTimesHigher"));
		this.emergencyAcceleration = Integer.valueOf(properties.getProperty("emergencyAcceleration"));
		this.lane = lane;
		realView = ev;
		this.params = params;
		speedLimit = lane.getSpeedLimit();
		emergencySpeedLimit = speedLimit * emergencySpeedLimitTimesHigher;
		carMoveModel = params.carMoveModel;

		offset = lane.getOffset();// linkLength() - lane.getLength();
		cars = new LinkedList<>();
		blocked = false;
		loops = new ArrayList<>(0);
	}

	public CarMoveModel getCarMoveModel() {
		return carMoveModel;
	}

	int getOffset() {
		return offset;
	}

	private int absoluteNumber() {
		return lane.getAbsoluteNumber();
	}

	private Node linkEnd() {
		return owner().getEnd();
	}

	/*
	 * Return <0 if lane represented by this object lies on the left of l, 0 if
	 * they are the same lane, >0 if lane represented by this object lies on the
	 * right of l.
	 */
	private int compareLanePositionTo(LaneRealExt l) {
		return absoluteNumber() - l.absoluteNumber();
	}

	private LaneRealExt leftNeighbor() {
		return realView.ext(owner().getLaneAbs(absoluteNumber() - 1));
	}

	private LaneRealExt rightNeighbor() {
		return realView.ext(owner().getLaneAbs(absoluteNumber() + 1));
	}

	public void prepareTurnSimulation() {
		LOGGER.trace(lane);
		Car car = cars.peek();
		if (car != null) {
			firstCarPos = car.getPosition();
		} else {
			firstCarPos = Integer.MAX_VALUE;
		}
	}

	/* intersection lane only */
	public void findApproachingCar() {
		LOGGER.trace(lane);
		if (blocked || cars.isEmpty() || wait) {
			carApproaching = false;
			return;
		}

		Car car = cars.getLast();
		carApproaching = (car.getPosition() + Math.max(car.getVelocity(), 1) * params.priorLaneTimeHeadway >= linkLength());
	}

	private int linkLength() {
		return owner().getLength();
	}

	private Link owner() {
		return lane.getOwner();
	}

	boolean hasCarPlace() {
		// CHANGE: MZA - to disable multiple cars entering the same lane 
		return enteringCars.isEmpty() && firstCarPos > offset;
	}

	/* assumption: stepsDone < stepsMax */
	boolean pushCar(Car car, int stepsMax, int stepsDone) {
		LOGGER.trace(car + " on " + lane);
//		if(getCarMoveModel().getName().equals(CarMoveModel.MODEL_MULTINAGLE)){
//			if(hasCarPlace()){
//				//put car in the lane
//				carEnterLane(car, offset-1, firstCarPos-1, stepsMax, stepsDone);
//				return true;
//			}
//			else{
//				return false;
//			}
//		}
//		else{
		if (car.getPosition() > firstCarPos) {
			firstCarPos = car.getPosition();
		}
		if (hasCarPlace()) {
			driveCar(car, car.getPosition() - 1, firstCarPos - 1, stepsMax, stepsDone, new InductionLoopPointer(), true);
			return true;
		} else {
			return false;
		}
//		}
	}

	/*
	 * previous element to ilp.current() (if exists) should be an induction loop
	 * with line <= startPos.
	 * 
	 * the same pointer can be used to the next car on this lane (above
	 * assumption will be true)
	 */
	private boolean driveCar(Car car, int startPos, int freePos, int stepsMax, int stepsDone, InductionLoopPointer ilp, boolean entered) {
		LOGGER.trace("CARTURN " + car + "on " + lane);
		int range = startPos + stepsMax - stepsDone;
		int pos;
		boolean stay = false;

		Action action = car.getAction();
		LaneRealExt sourceLane = getSourceLane(action);

		/* last line of this link crossed by the car in this turn */
		int lastCrossedLine;

		if (!equals(sourceLane)) {
			int laneChangePos = Math.max(sourceLane.offset - 1, car.getPosition());
			pos = Math.min(Math.min(range, freePos), laneChangePos);

			if (pos == range || pos < laneChangePos || !sourceLane.pushCar(car, stepsMax, stepsDone + pos - startPos)) {
				stay = true;
			}
			lastCrossedLine = pos;
		} else {
			int lastPos = linkLength() - 1;
			pos = Math.min(Math.min(range, freePos), lastPos);
			if (pos == range || pos < lastPos || blocked || !handleCarAction(car, stepsMax, stepsDone + pos - startPos)) {
				stay = true;
				lastCrossedLine = pos;
			} else {
				lastCrossedLine = pos + 1;
			}
		}

		if (stay) {
			if (car.getPosition() < pos) {
				car.setPosition(pos);
			}
			car.setVelocity(stepsDone + pos - startPos);
			if (car.getVelocity() < 0) {
				car.setVelocity(0);
			}
			if (entered) {
				enteringCars.add(car);
			}
		}

		LOGGER.trace("CARTURN " + car + " crossed " + lastCrossedLine);
		/* We fire all induction loops in the range (startPos; lastCrossedLine] */
		while (!ilp.atEnd() && ilp.current().line <= lastCrossedLine) {
			if (ilp.current().line > startPos) {
				LOGGER.trace(">>>>>>> INDUCTION LOOP before " + startPos + " and " + lastCrossedLine + " for " + lane);
				ilp.current().handler.handleCarDrive(car.getVelocity(), car.getDriver());
			}

			ilp.forward();
		}

		LOGGER.trace("CARTURN " + car + "on " + lane);
		return stay;
	}

	private LaneRealExt getSourceLane(Action action) {
		LaneRealExt sourceLane;
		if (action != null) {
			sourceLane = realView.ext(action.getSource());
			int x = compareLanePositionTo(sourceLane);
			if (x < 0) {
				sourceLane = rightNeighbor();
			} else if (x > 0) {
				sourceLane = leftNeighbor();
			}
		} else {
			sourceLane = this;
		}
		return sourceLane;
	}

	/* assumption: stepsDone < stepsMax */
	private boolean handleCarAction(Car car, int stepsMax, int stepsDone) {
		LOGGER.trace(car + " on " + lane);
		Action action = car.getAction();

		if (action == null) {
			car.setVelocity(stepsMax);
			((GatewayRealExt) realView.ext(linkEnd())).acceptCar(car);
			return true;
		}

		if (wait) {
			/* we are waiting one turn */
			wait = false;
			return false;
		} else {
			/* we are approaching an intersection */
			Lane[] pl = action.getPriorLanes();
			// int i;
			for (Lane aPl : pl) {
				if (realView.ext(aPl).carApproaching) {
					if (checkDeadlock(action.getSource(), aPl)) {
						LOGGER.warn(lane + "DEADLOCK situation.");
						deadLockRecovery();
					}
					return false;
				}
			}
			LinkRealExt l = realView.ext(action.getTarget());
			car.setPosition(0);

			return l.enterCar(car, stepsMax, stepsDone);
		}
	}

	private void deadLockRecovery() {
		// ev.ext( lane.getOwner()).
		if (params.getRandomGenerator().nextFloat() < params.victimProb) {
			LOGGER.trace("Deadlock victim: " + lane + " - recovering.");
			wait = true;
		}
		LOGGER.trace("Deadlock: " + lane + " won't be a victim.");
	}

	private boolean checkDeadlock(Lane begin, Lane next) {
		LOGGER.trace("Check for deadlock: " + begin);
		return checkDeadlock(next, Sets.newHashSet(begin));
	}

	private boolean checkDeadlock(Lane next, Set<Lane> visited) {
		LOGGER.trace("Check for deadlock: " + next);
		if (visited.contains(next)) {
			LOGGER.trace(visited);
			return true;
		}
		visited.add(next);

		for (Iterator<Action> it = next.actionIterator(); it.hasNext(); ) {
			Action ac = it.next();
			for (Lane aPl : ac.getPriorLanes()) {
				if (realView.ext(aPl).carApproaching && checkDeadlock(aPl, visited)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Nagel-Schreckenberg
	 */
	void simulateTurn() {
		LOGGER.trace(lane);
		ListIterator<Car> carIterator = cars.listIterator();
		if (carIterator.hasNext()) {
			InductionLoopPointer ilp = new InductionLoopPointer();
			Car car = carIterator.next();
			Car nextCar;

			do {
				nextCar = carIterator.hasNext() ? carIterator.next() : null;

				// remember starting point
				car.setBeforeLane(lane);
				car.setBeforePos(car.getPosition());

				// 1. Init velocity variable
				boolean velocityZero = car.getVelocity() <= 0;//VDR - check for v = 0	(slow start)

				// 2. Acceleration
				int velocity = car.getVelocity();
				if (car.isEmergency()) {
					if (velocity < emergencySpeedLimit) {
						velocity += emergencyAcceleration;
					}
				} else {
					if (velocity < speedLimit) {
						velocity += 1;
					}
				}

				float decisionChance = params.getRandomGenerator().nextFloat();

				// 3. Deceleration when nagle
				switch (carMoveModel.getName()) {
					case CarMoveModel.MODEL_NAGLE:
						if (decisionChance < carMoveModel.getFloatParameter(CarMoveModel.MODEL_NAGLE_MOVE_PROB)) {
							velocity--;
						}
						break;
					case CarMoveModel.MODEL_MULTINAGLE:
						if (decisionChance < carMoveModel.getFloatParameter(CarMoveModel.MODEL_MULTINAGLE_MOVE_PROB)) {
							velocity--;
						}

						setActionMultiNagle(car);
						break;
					// deceleration if vdr
					case CarMoveModel.MODEL_VDR:
						//if v = 0 => different (greater) chance of deceleration
						if (velocityZero) {
							if (decisionChance < carMoveModel.getFloatParameter(CarMoveModel.MODEL_VDR_0_PROB)) {
								--velocity;
							}
						} else {
							if (decisionChance < carMoveModel.getFloatParameter(CarMoveModel.MODEL_VDR_MOVE_PROB)) {
								--velocity;
							}
						}
						break;
					//Brake light model
					case CarMoveModel.MODEL_BRAKELIGHT:
						if (velocityZero) {
							if (decisionChance < carMoveModel.getFloatParameter(CarMoveModel.MODEL_BRAKELIGHT_0_PROB)) {
								--velocity;
							}
						} else {
							if (nextCar != null && nextCar.isBraking()) {
								int threshold = carMoveModel.getIntParameter(CarMoveModel.MODEL_BRAKELIGHT_DISTANCE_THRESHOLD);
								double ts = (threshold < velocity) ? threshold : velocity;
								double th = (nextCar.getPosition() - car.getPosition()) / (double) velocity;
								if (th < ts) {
									if (decisionChance < carMoveModel.getFloatParameter(CarMoveModel.MODEL_BRAKELIGHT_BRAKE_PROB)) {
										--velocity;
										car.setBraking(true, car.isEmergency());
									} else {
										car.setBraking(false, car.isEmergency());
									}
								}
							} else {
								if (decisionChance < carMoveModel.getFloatParameter(CarMoveModel.MODEL_BRAKELIGHT_MOVE_PROB)) {
									--velocity;
									car.setBraking(true, car.isEmergency());
								} else {
									car.setBraking(false, car.isEmergency());
								}
							}
						}
						break;
					default:
						throw new RuntimeException("unknown model! " + carMoveModel.getName());
				}

				// 4. Drive (Move the car)
				int freePos = Integer.MAX_VALUE;
				if (nextCar != null) {
					freePos = nextCar.getPosition() - 1;
				}

				boolean stay = driveCar(car, car.getPosition(), freePos, velocity, 0, ilp, false);

				if (!stay) {
					if (nextCar != null) {
						carIterator.previous();
					}
					carIterator.previous();
					carIterator.remove();
					if (nextCar != null) {
						carIterator.next();
					}
					Car cx = cars.peek();
					if (cx != null) {
						firstCarPos = cx.getPosition();
					} else {
						firstCarPos = Integer.MAX_VALUE;
					}
				}

				// remember this car as next (we are going backwards)
				car = nextCar;
			} while (car != null);
		}
	}

	private void setActionMultiNagle(Car car) {
		Action sourceAction = car.getAction();
		if (sourceAction != null && car.getPosition() < (linkLength() - 5)) {
			Action newAction = new Action(sourceAction.getSource(), sourceAction.getTarget(), sourceAction.getPriorLanes());
			LaneRealExt sourceLane = realView.ext(newAction.getSource());
			int c = newAction.getSource().getOwner().laneCount();
			int s = newAction.getSource().getAbsoluteNumber();
			float prob = params.getRandomGenerator().nextFloat();
			if (prob < 0.1) {
				if (s < (c - 1)) {
					newAction.setSource(sourceLane.rightNeighbor().lane);
				}
			} else if (prob < 0.2 && s > 0) {
				newAction.setSource(sourceLane.leftNeighbor().lane);
			}
			car.setAction(newAction);
		}
	}

	void finalizeTurnSimulation() {
		LOGGER.trace(lane);
		if (!enteringCars.isEmpty()) {
			for (Car c : enteringCars) {
				if (c.getAction() != null && c.getAction().getTarget().equals(owner())) {
					c.setPosition(0);
					c.setVelocity(0);
				}
				Iterator<Car> it = cars.iterator();
				LinkedList<Car> newC = new LinkedList<>();
				boolean ins = false;
				while (it.hasNext()) {
					Car c1 = it.next();
					newC.add(c1);
					if (c1.getPosition() < c.getPosition() && !ins) {
						newC.add(c);
						ins = true;
					}
				}
				if (!ins) {
					newC.addFirst(c);
				}
				cars = newC;
//				this.cars.addFirst(c);
			}
			enteringCars.clear();
		}
	}

	/**
	 * Gets the position of the nearest car from the entrance to the lane
	 * (how much space there is on the lane to enter)
	 *
	 * @return distance from the nearest car
	 * @author Maciej Zalewski
	 */
	public int getFirstCarPosition() {
		// if there are no cars, return length of the lane
		if (getAllCarsNumber() == 0) {
			return lane.getLength();
		}

		// if there are newly entered cars - show lack of space
		if (!enteringCars.isEmpty()) {
			return -1;
		}

		// in any other case, return position of the nearest car on the lane
		return cars.peek().getPosition();
	}

	/**
	 * This method returns a total number of cars on the lane
	 * (both "inside" and those that had just entered)
	 *
	 * @return number of cars on the lane
	 * @author Maciej Zalewski
	 */
	public int getAllCarsNumber() {
		return cars.size() + enteringCars.size();
	}

	public boolean anyEmergencyCarsOnLane() {
		for (Car car : cars) {
			if (car.isEmergency()) {
				return true;
			}
		}
		return false;
	}

	public int getEmergencyCarsOnLaneNr() {
		int counter = 0;
		for (Car car : cars) {
			if (car.isEmergency()) {
				counter++;
			}
		}
		return counter;
	}

	public synchronized int getClosestEmergencyCarDistance() {
		int closestDistance = linkLength();
		for (Car car : cars) {
			if (car.isEmergency()) {
				int distance = linkLength() - 1 - car.getPosition();
				if (distance < closestDistance) {
					closestDistance = distance;
				}
			}
		}
		return closestDistance;
	}

	public CarInfoCursor carInfoForwardCursor() {
		return new CarInfoCursorForwardImpl();
	}

	public CarInfoCursor carInfoBackwardCursor() {
		return new CarInfoCursorBackwardImpl();
	}

	public boolean isBlocked() {
		return blocked;
	}

	public void block() {
		blocked = true;
	}

	public void unblock() {
		blocked = false;
	}

	public void installInductionLoop(int line, CarDriveHandler handler) throws IndexOutOfBoundsException {
		LOGGER.trace("Instaling IL ona lane " + lane + " at distance: " + line);
		if (line < 0 || line > linkLength()) {
			throw new IndexOutOfBoundsException("line = " + line);
		}

		InductionLoop loop = new InductionLoop(line, handler);

		int i;
		for (i = 0; i < loops.size() && loops.get(i).line <= line; i++) {
		}
		loops.add(i, loop);
	}

	private static class InductionLoop {
		private final int line;
		private final CarDriveHandler handler;

		private InductionLoop(int line, CarDriveHandler handler) {
			this.line = line;
			this.handler = handler;
		}
	}

	private abstract class CarInfoUniCursorImpl implements CarInfoCursor {
		protected Car car;

		public Lane currentLane() {
			if (car == null) {
				throw new NoSuchElementException();
			}
			return lane;
		}

		public int currentPos() {
			if (car == null) {
				throw new NoSuchElementException();
			}
			return car.getPosition() - offset;
		}

		public int currentVelocity() {
			if (car == null) {
				throw new NoSuchElementException();
			}
			return car.getVelocity();
		}

		public Object currentDriver() {
			if (car == null) {
				throw new NoSuchElementException();
			}
			return car.getDriver();
		}

		public Lane beforeLane() {
			if (car == null) {
				throw new NoSuchElementException();
			}
			return car.getBeforeLane();
		}

		public int beforePos() {
			if (car == null) {
				throw new NoSuchElementException();
			}

			return car.getBeforePos() - offset;
		}

		public boolean isValid() {
			return (car != null);
		}
	}

	private class CarInfoCursorForwardImpl extends CarInfoUniCursorImpl {

		private final Iterator<Car> cit;

		private CarInfoCursorForwardImpl() {
			cit = cars.iterator();
			if (cit.hasNext()) {
				car = cit.next();
			} else {
				car = null;
			}
		}

		public void next() {
			if (!cit.hasNext()) {
				car = null;
			} else {
				car = cit.next();
			}
		}
	}

	private class CarInfoCursorBackwardImpl extends CarInfoUniCursorImpl {
		private final ListIterator<Car> cit;

		private CarInfoCursorBackwardImpl() {
			cit = cars.listIterator(cars.size());
			if (cit.hasPrevious()) {
				car = cit.previous();
			} else {
				car = null;
			}
		}

		public void next() {
			if (!cit.hasPrevious()) {
				car = null;
			} else {
				car = cit.previous();
			}
		}
	}

	private class InductionLoopPointer {
		private int i;

		private InductionLoopPointer() {
			i = 0;
		}

		private boolean atEnd() {
			return i == loops.size();
		}

		private InductionLoop current() {
			return loops.get(i);
		}

		private void forward() {
			if (i < loops.size()) {
				i++;
			}
		}
	}
}

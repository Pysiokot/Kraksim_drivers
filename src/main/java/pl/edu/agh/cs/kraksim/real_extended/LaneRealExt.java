package pl.edu.agh.cs.kraksim.real_extended;

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
	private final double laneChangeDesire;
	private final double rightLaneChangeDesire;
	private final CarMoveModel carMoveModel;
	private boolean blocked;
	private int firstCarPos;
	private boolean carApproaching;
	private boolean wait;

	final int SWITCH_TIME;
	final int MIN_SAFE_DISTANCE;

	private LinkedList<Car> cars;
	private ListIterator<Car> carIterator;

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
		this.laneChangeDesire = Double.valueOf(properties.getProperty("laneChangeDesire"));
		this.rightLaneChangeDesire = Double.valueOf(properties.getProperty("rightLaneChangeDesire"));
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

		SWITCH_TIME = params.getSwitchTime();
		MIN_SAFE_DISTANCE = params.getMinSafeDistance();
		
		// block cells
		//this.addNewObstaclesFromCorelane();
		//this.finalizeTurnSimulation();
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

	Node linkEnd() {
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

	LaneRealExt leftNeighbor() {
		return realView.ext(owner().getLaneAbs(absoluteNumber() - 1));
	}

	LaneRealExt rightNeighbor() {
		return realView.ext(owner().getLaneAbs(absoluteNumber() + 1));
	}
	
	boolean hasLeftNeighbor() {
		return absoluteNumber() - 1 >= 0;
	}
	
	boolean hasRightNeighbor() {
		return absoluteNumber() + 1 < realView.ext(owner()).link.getLanes().length;
	}

	public void prepareTurnSimulation() {
		LOGGER.trace(lane);
		Car car = cars.peek();
		if (car != null) {
			firstCarPos = car.getPosition();
		} else {
			firstCarPos = Integer.MAX_VALUE;
		}
		this.carIterator = this.cars.listIterator();
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

	int linkLength() {
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
		System.out.println("driveCar :: pushCar (sourceLane) :: hasCarPlace() = " + hasCarPlace());
		if (car.getPosition() > firstCarPos) {
			firstCarPos = car.getPosition();
		}
		if (hasCarPlace()) {
			car.drive(this,car.getPosition() - 1, firstCarPos - 1, stepsMax, stepsDone, new InductionLoopPointer(), true);
			car.setCurrentLane(this);
			return true;
		} else {
			return false;
		}
	}

	LaneRealExt getSourceLane(Action action) {
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

	void deadLockRecovery() {
		// ev.ext( lane.getOwner()).
		if (params.getRandomGenerator().nextFloat() < params.victimProb) {
			LOGGER.trace("Deadlock victim: " + lane + " - recovering.");
			setWait(true);
		}
		LOGGER.trace("Deadlock: " + lane + " won't be a victim.");
	}

	boolean checkDeadlock(Lane begin, Lane next) {
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
	
	@Deprecated
	void simulateTurn() {
		LOGGER.trace(lane);
		System.out.println("============================");
//		ListIterator<Car> carIteratorTemp = cars.listIterator();
//		while(carIteratorTemp.hasNext()) {
//			Car c = carIteratorTemp.next();
//			try {
//				System.out.println("pos " + c.pos + " abs " + c.getAction().getSource().getAbsoluteNumber() + " rel " + c.getAction().getSource().getRelativeNumber()
//						+ " cel: " + c.getAction().getTarget().getId()
//						+"\n\tabs Pref" + c.getPreferableAction().getSource().getAbsoluteNumber() + " rel Pref " + c.getPreferableAction().getSource().getRelativeNumber()
//						+ " cel: Pref " + c.getPreferableAction().getTarget().getId());
//			} catch(Exception e) {
//				
//			}
//		}
		System.out.println("XX");
		ListIterator<Car> carIterator = cars.listIterator();
		if (carIterator.hasNext()) {
			InductionLoopPointer ilp = new InductionLoopPointer();	// idk what it does <yet?>
			Car car = carIterator.next();
			Car nextCar;

			do {	// ~ for car in carIterator
				nextCar = carIterator.hasNext() ? carIterator.next() : null;
				// carIterator on next for nextCar || on next of next of car
				
				if(car.isObstacle()) {
					car = nextCar;
					continue;
				}
				System.out.println(car.toString());
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

				// 3. Deceleration when nagel
				switch (carMoveModel.getName()) {
					case CarMoveModel.MODEL_NAGEL:
						if (decisionChance < carMoveModel.getFloatParameter(CarMoveModel.MODEL_NAGEL_MOVE_PROB)) {
							velocity--;
						}
						break;
					case CarMoveModel.MODEL_MULTINAGEL:	// is used by default
						if (decisionChance < carMoveModel.getFloatParameter(CarMoveModel.MODEL_MULTINAGEL_MOVE_PROB)) {
							velocity--;
						}
						System.out.println("setActionMultiNagel");
						setActionMultiNagel(car);
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
				//	drive car to MIN(car.pos + car.vel , freePos)
				boolean stay = car.drive(this, car.getPosition(), freePos, velocity, 0, ilp, false);

				// carIterator is on next for nextCar || on next of next of car -> [c] [n] [*] where * -> iterator or [c] * if on the end
				// stay -> if stay on the same lane
				if (!stay) {
					if (nextCar != null) {
						carIterator.previous();	//	[c] [n*] []	
					}
					carIterator.previous();	// [c*] ...
					// remove car from lane if it is not longer on it
					carIterator.remove();
					if (nextCar != null) {
						carIterator.next();
					}
					Car cx = cars.peek();
					// update lane firstCarPos 
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

	/**
	 * Nagel-Schreckenberg
	 */
	void simulateTurn(Car car) {
		LOGGER.trace(lane);
		if(!this.equals(car.getCurrentLane())) {
			throw new RuntimeException("asas");
		}

		InductionLoopPointer ilp = new InductionLoopPointer();	// idk what it does <yet?>
		Car nextCar;

		nextCar = getFrontCar(car);
		// carIterator on next for nextCar || on next of next of car

		if(car.isObstacle()) {
			return;
		}

		// remember starting point
		car.setBeforeLane(lane);
		car.setBeforePos(car.getPosition());

		// 1. Init velocity variable
		boolean velocityZero = car.getVelocity() <= 0;	//VDR - check for v = 0	(slow start)

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

		// 3. Deceleration when nagel
		switch (carMoveModel.getName()) {
			case CarMoveModel.MODEL_NAGEL:
				if (decisionChance < carMoveModel.getFloatParameter(CarMoveModel.MODEL_NAGEL_MOVE_PROB)) {
					velocity--;
				}
				break;
			case CarMoveModel.MODEL_MULTINAGEL:	// is used by default
				if (decisionChance < carMoveModel.getFloatParameter(CarMoveModel.MODEL_MULTINAGEL_MOVE_PROB)) {
					velocity--;
				}
				System.out.println("setActionMultiNagel");
				setActionMultiNagel(car);
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
		//	drive car to MIN(car.pos + car.vel , freePos)
		boolean stay = car.drive(this, car.getPosition(), freePos, velocity, 0, ilp, false);

		// carIterator is on next for nextCar || on next of next of car -> [c] [n] [*] where * -> iterator or [c] * if on the end
		// stay -> if stay on the same lane
		if (!stay) {
			this.carIterator.remove();
			Car cx = cars.peek();
			// update lane firstCarPos
			if (cx != null) {
				firstCarPos = cx.getPosition();
			} else {
				firstCarPos = Integer.MAX_VALUE;
			}
		}

	}

	private void setActionMultiNagel(Car car) {
		Action sourceAction = car.getAction();
		if (sourceAction != null && car.getPosition() < (linkLength() - 5)) {
			Action newAction = new Action(sourceAction.getSource(), sourceAction.getTarget(), sourceAction.getPriorLanes());
			System.out.println("switchLanes");
			car.switchLanes(newAction, this, this.realView);
			car.setAction(newAction);

		}
	}

	void finalizeTurnSimulation() {
		LOGGER.trace(lane);
		this.removeExpiredObstacleFromCorelane();
		this.addNewObstaclesFromCorelane();
		if (!enteringCars.isEmpty()) {
			for (Car enteringCar : enteringCars) {
				if (enteringCar.getAction() != null && enteringCar.getAction().getTarget().equals(owner())) {	// was always FALSE during our tests
					System.out.println("setVelocity(0) # 1");
					enteringCar.setPosition(0);
					enteringCar.setVelocity(0);
				}
				if(enteringCar.pos==0) {
					System.out.println("this.lane.getAbsoluteNumber() " + this.lane.getAbsoluteNumber());
					try {
						Car c = enteringCar;
						System.out.println("NEW CAR!!!\n\tabs:" + c.getAction().getSource().getAbsoluteNumber() + " rel " + c.getAction().getSource().getRelativeNumber()
								+ " cel: " + c.getAction().getTarget().getId()
								+"\n\tabs Pref" + c.getPreferableAction().getSource().getAbsoluteNumber() + " rel Pref " + c.getPreferableAction().getSource().getRelativeNumber()
								+ " cel: Pref " + c.getPreferableAction().getTarget().getId());
					} catch(Exception e) {
						
					}
				}
				Iterator<Car> it = cars.iterator();
				LinkedList<Car> newCarList = new LinkedList<>();
				boolean ins = false;
				while (it.hasNext()) {
					Car iterCar = it.next();
					if(enteringCar.isObstacle()) {
						// if we have obstacle at cell 3 and cars at ->[c_2]->[c_3]->[c_5] then we need ->[c_2]->[o_3]->[c_3]->[c_5]
						if (iterCar.getPosition() >= enteringCar.getPosition() && !ins) {
							newCarList.add(enteringCar);
							ins = true;
						}
					} else {
						// cars are added 
						if (iterCar.getPosition() > enteringCar.getPosition() && !ins) {
							newCarList.add(enteringCar);
							ins = true;
						}
					}
					newCarList.add(iterCar);						
				}
				if (!ins) {
					newCarList.add(enteringCar);
				}
				cars = newCarList;
//				this.cars.addFirst(c);
				//if(c.getPosition() > 0) System.out.println("c.getPosition() " + c.getPosition()+ "\t" + c.getVelocity());
			}
			enteringCars.clear();
		}
//		Iterator<Car> it = cars.iterator();
//		System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXX");
//		while (it.hasNext()) {
//			Car c1 = it.next();
//			System.out.print(c1.getPosition() + " ");
//		}
//		System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZz");
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
	 * Choose lane to switch to.
	 * @param sourceLane line car is currently in
	 * @return
	 */
	LaneSwitch getLaneToSwitch(Car car, Lane sourceLane){
		int laneCount = sourceLane.getOwner().laneCount();
		int laneAbsoluteNumber = sourceLane.getAbsoluteNumber();

		LaneSwitch direction;
		float prob = params.getRandomGenerator().nextFloat();

		if(car.getPreferableAction().getSource().getAbsoluteNumber() == car.getAction().getSource().getAbsoluteNumber()){
			if(car.isEmergency()) {
				if(prob < laneChangeDesire){
					prob = params.getRandomGenerator().nextFloat();

					if (prob < rightLaneChangeDesire && laneAbsoluteNumber < (laneCount - 1))  direction = LaneSwitch.RIGHT;
					else if (prob > rightLaneChangeDesire && laneAbsoluteNumber > 0) direction = LaneSwitch.LEFT;
					else direction = LaneSwitch.NO_CHANGE;
					System.out.println("LOSOWANIE EMERGENCY: Czy chce zmienic pas? Chce, dostalem " + direction);
				} else {
					System.out.println("LOSOWANIE EMERGENCY: Czy chce zmienic pas? Nie chce.");
					direction = LaneSwitch.NO_CHANGE;
				}
			} else if(prob < 0.3){
				prob = params.getRandomGenerator().nextFloat();

				if (prob < 0.5 && laneAbsoluteNumber < (laneCount - 1))  direction = LaneSwitch.RIGHT;
				else if (prob > 0.5 && laneAbsoluteNumber > 0) direction = LaneSwitch.LEFT;
				else direction = LaneSwitch.NO_CHANGE;
				System.out.println("Losowanie zwykłe: Czy chce zmienic pas? Chce. Dostałem " + direction);
			} else {
				System.out.println("Losowanie zwykłe: Czy chce zmienic pas? Nie chce i nie zmieniam kierunku");
				direction = LaneSwitch.NO_CHANGE;
			}
		} else {
			if(lane.getAbsoluteNumber()-1 == car.getPreferableAction().getSource().getAbsoluteNumber()){
				System.out.println("GETTING RIGHT");
				direction = LaneSwitch.RIGHT;
			}
			else if(lane.getAbsoluteNumber()+1 == car.getPreferableAction().getSource().getAbsoluteNumber()){
				System.out.println("GETTING LEFT");
				direction = LaneSwitch.LEFT;
			}
			else {
				direction = LaneSwitch.NO_CHANGE;
				System.out.println("Koncowy else: nie zmieniam kierunku");
			}
		}

		return direction;
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

	public Car getBehindCar(Car car) {
		return getBehindCar(car.getPosition());
	}
	
	public Car getBehindCar(int pos) {
		int minPositiveDistance = Integer.MAX_VALUE;
		Car behindCar = null;
		List<Car> carsOnLane = this.getCars();

		for (Car _car : carsOnLane) {
			int carsDistance = pos - _car.getPosition();
			if (minPositiveDistance > carsDistance && carsDistance > 0) {
				minPositiveDistance = carsDistance;
				behindCar = _car;
			}
		}
		return behindCar;
	}
	
	public Car getFrontCar(Car car) {
		return getFrontCar(car.getPosition());
	}
	
	public Car getFrontCar(int pos) {
		int minPositiveDistance = Integer.MAX_VALUE;
		Car nextCar = null;
		List<Car> carsOnLane = this.getCars();

		for (Car _car : carsOnLane) {
			int carsDistance = _car.getPosition() - pos;
			if (minPositiveDistance > carsDistance && carsDistance > 0) {
				minPositiveDistance = carsDistance;
				nextCar = _car;
			}
		}
		return nextCar;
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

	// 2019
	/**
	 * Use this.carIterator to add new Car
	 * It will be added behind current position of Iterator
	 * Iterator will be pointing at newly added car
	 * 1. as
	 * 2. asd
	 */
	public void addCarToLane(Car car) {
		while(this.carIterator.hasPrevious()) {
			if(this.carIterator.previous().getPosition() < car.getPosition()) {
				// too far, 
			}
		}
	}
	
	Lane getLane() {
		return lane;
	}

	LinkedList<Car> getCars() {
		return cars;
	}

	List<Car> getEnteringCars() {
		return enteringCars;
	}

	RealEView getRealView() {
		return realView;
	}

	public RealSimulationParams getParams() {
		return params;
	}

	boolean getCarApproaching() {
		return carApproaching;
	}

	boolean getWait() {
		return wait;
	}

	void setWait(boolean wait) {
		this.wait = wait;
	}
	
	ListIterator<Car> getCarsIterator() {
		return carIterator;
	}
	// 2019 end

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
	
	private void addNewObstaclesFromCorelane() {
		List<Integer> cellList = lane.getRecentlyActivatedBlockedCellsIndexList(); 	
		for(Integer blickedCell : cellList) {
			System.out.println("new blocked " + blickedCell);
			enteringCars.add(new Obstacle(blickedCell, this));
		}
	}
	
	private void removeExpiredObstacleFromCorelane() {
		List<Integer> cellList = lane.getRecentlyExpiredBlockedCellsIndexList(); 
		Iterator<Car> it = cars.iterator();
		while (it.hasNext()) {
			Car car = it.next();
			if(car.isObstacle() && cellList.contains(car.getPosition())) {
				System.out.println("old remove " + car);
				it.remove();
			}
		}
	}
	
	/////////////////////////////////////////////////////////////////////

	static class InductionLoop {
		final int line;
		final CarDriveHandler handler;

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
			if(car != null && car instanceof Obstacle) {
				next();
			}
		}

		public void next() {
			if (!cit.hasNext()) {
				car = null;
			} else {
				car = cit.next();
			}
			if(car != null && car instanceof Obstacle) {
				next();
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
			if(car != null && car instanceof Obstacle) {
				next();
			}
		}

		public void next() {
			if (!cit.hasPrevious()) {
				car = null;
			} else {
				car = cit.previous();
			}
			if(car != null && car instanceof Obstacle) {
				next();
			}
		}
	}

	class InductionLoopPointer {
		private int i;

		private InductionLoopPointer() {
			i = 0;
		}

		boolean atEnd() {
			return i == loops.size();
		}

		InductionLoop current() {
			return loops.get(i);
		}

		void forward() {
			if (i < loops.size()) {
				i++;
			}
		}
	}
}

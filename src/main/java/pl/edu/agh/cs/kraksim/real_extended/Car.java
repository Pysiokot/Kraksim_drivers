package pl.edu.agh.cs.kraksim.real_extended;

import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.KraksimConfigurator;
import pl.edu.agh.cs.kraksim.core.Action;
import pl.edu.agh.cs.kraksim.core.Lane;
import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.core.Node;
import pl.edu.agh.cs.kraksim.iface.sim.Route;
import pl.edu.agh.cs.kraksim.main.CarMoveModel;
import pl.edu.agh.cs.kraksim.main.Simulation;
import pl.edu.agh.cs.kraksim.main.drivers.Driver;
import pl.edu.agh.cs.kraksim.real_extended.LaneRealExt.InductionLoopPointer;

import java.awt.*;
import java.util.*;
import java.util.List;

class Car {
	private static final Logger LOGGER = Logger.getLogger(Car.class);
	private final boolean isTEST2013Enabled;
	private final Driver driver;
	private List<Node> TEST2013intersectionsList = new LinkedList<>();
	private Map<Node, List<Link>> TEST2013linkIntersectionsList = new HashMap<>();
	/*
	 * Iterator through route's link. linkIterator.next() is the next (not
	 * current!) link, the car will drive.
	 */
	private ListIterator<Link> linkIterator;
	private Action action;
	private Action preferableAction;
	protected int pos;
	private int velocity;
	//  private ListIterator<Link> copyLinkIterator;
	private long TEST2013waitCounter = 0;
	private long TEST2013waitLimit;
	private boolean braking = false;
	private int enterPos = -1;
	private Lane beforeLane;
	private int beforePos;
	private boolean rerouting = false;

	// 2019
	private LaneRealExt currentLane = null;
	private int updateInTurn = -1;

	private LaneSwitch switchToLane = LaneSwitch.NO_CHANGE;

	Car(Driver driver, Route route, boolean rerouting) {
		// == reading TEST2013 configuration
		Properties prop = KraksimConfigurator.getPropertiesFromFile();
		String test2013enabled = prop.getProperty("TEST2013Enabled");
		if (test2013enabled != null && test2013enabled.trim().equals("true")) {
			isTEST2013Enabled = true;

			String test2013intersectionVisitor = prop.getProperty("TEST2013IntersectionVisitor");
			if (test2013intersectionVisitor != null && test2013intersectionVisitor.trim().equals("true")) {
				boolean isTEST2013IntersectionVisitorEnabled = true;
				TEST2013intersectionsList = new LinkedList<>();
			} else {
				boolean isTEST2013IntersectionVisitorEnabled = false;
			}


			String test2013intersectionLinkVisitor = prop.getProperty("TEST2013IntersectionLinkVisitor");

			if (test2013intersectionLinkVisitor != null && test2013intersectionLinkVisitor.trim().equals("true")) {
				TEST2013linkIntersectionsList = new HashMap<>();
			}

			String test2013waitLimit = prop.getProperty("TEST2013WaitLimit");
			TEST2013waitLimit = Integer.valueOf(test2013waitLimit);
		} else {
			isTEST2013Enabled = false;
		}
		// =end of= reading TEST2013 configuration

		this.driver = driver;
		this.rerouting = rerouting;
		linkIterator = route.linkIterator();
		// copyLinkIterator = route.linkIterator();
		// Important. See the notice above.
		linkIterator.next();
		// copyLinkIterator.next();

		beforeLane = null;
		beforePos = 0;

		LOGGER.trace("\n Driver= " + driver + "\n rerouting= " + rerouting);
	}
	
	public Car() {
		this.isTEST2013Enabled = false;
		this.driver = null;
	}

	public Driver getDriver() {
		return driver;
	}

	public int getPosition() {
		return pos;
	}

	public void setPosition(int pos) {
		this.pos = pos;
	}

	public int getVelocity() {
		return velocity;
	}

	public void setVelocity(int velocity) {
		this.velocity = velocity;
	}

	public int getEnterPos() {
		return enterPos;
	}

	public void setEnterPos(int enterPos) {
		this.enterPos = enterPos;
	}

	public boolean isEmergency() {
		return getDriver().isEmergency();
	}
	
	public boolean isObstacle() {
		return false;
	}

	public boolean isBraking() {
		return braking;
	}

	public void setBraking(boolean braking, boolean emergency) {
		if (emergency) {
			if (braking) {
				driver.setCarColor(Color.BLACK);
			} else {
				driver.setCarColor(Color.BLUE);
			}
		} else {
			if (braking) {
				driver.setCarColor(Color.RED);
			} else {
				driver.setCarColor(Color.YELLOW);
			}
		}
		this.braking = braking;
	}

	public void TEST2013updateCarPosition(int position) {
		if (isTEST2013Enabled) {
			if (pos == position) {
				TEST2013waitCounter++;
				if (TEST2013waitCounter > TEST2013waitLimit) {
					LOGGER.info(String.format("%s hasn't move for %d turns.", toString(), TEST2013waitCounter));
				}
			} else {
				TEST2013waitCounter = 0;
			}
		}
	}

	public String toString() {
		if(currentLane == null) {
			return driver + " in [ CAR bPos=" + beforePos + ",cPos=" + pos + ",v=" + velocity + " lane: " + "null"+ " switch: " + this.switchToLane.toString() +   ']';
			
		}
		return driver + " in [ CAR bPos=" + beforePos + ",cPos=" + pos + ",v=" + velocity + " lane: " + this.currentLane.getLane().getAbsoluteNumber()+ " switch: " + this.switchToLane.toString() + ']';
	}

	int getBeforePos() {
		return beforePos;
	}

	void setBeforePos(int beforePos) {
		this.beforePos = beforePos;
	}

	// TODO: change this
	public boolean hasNextTripPoint() {
		return linkIterator.hasNext();
	}

	public Link nextTripPoint() {
		// copyLinkIterator.next();
		return linkIterator.next();
	}

	public Link peekNextTripPoint() {
		// copyLinkIterator.next();
		if (linkIterator.hasNext()) {
			Link result = linkIterator.next();
			linkIterator.previous();
			return result;
		} else {
			return null;
		}
	}

	public Action getAction() {
		LOGGER.trace("\n Action= " + action + "\n Driver= " + driver);
		return action;
	}

	public void setAction(Action action) {
		if (isTEST2013Enabled) {
			TEST2013onNewAction(action);
		}

		LOGGER.trace("\n Action= " + action + "\n Driver= " + driver);
		this.action = action;
	}

	public Action getPreferableAction(){
		return this.preferableAction;
	}
	
	public void setPreferableAction(Action preferableAction){
		this.preferableAction = preferableAction;
	}
	
	protected void TEST2013onNewAction(Action action) {
		if (action == null || action.getSource() == null) {
			return;
		}

		Link sourceLink = action.getSource().getOwner();
		Node nextIntersection = action.getTarget().getBeginning();

		// check if this intersection has been visited before
		// from this particular link
		if (TEST2013linkIntersectionsList.containsKey(sourceLink)) {
			List<Link> links = TEST2013linkIntersectionsList.get(nextIntersection);
			if (links.contains(sourceLink)) {
				LOGGER.fatal(String.format("Vehicle has already visited intersection (id:%s) from link (%s). This should _NEVER_ happen", nextIntersection.getId(), sourceLink.getId()));
			} else {
				links.add(sourceLink);
			}
		} else {
			TEST2013linkIntersectionsList.put(nextIntersection, Lists.newArrayList(sourceLink));
		}

		// check if this intersection has been visited before
		if (TEST2013intersectionsList.contains(nextIntersection)) {
			LOGGER.warn(String.format("Vehicle has already been at intersection (id:%s)", nextIntersection.getId()));
		} else {
			TEST2013intersectionsList.add(nextIntersection);
		}
	}

	public void refreshTripRoute() {
		// TODO: make it configurable from properties file
		// ListIterator<Link> copyLinkIter = linkIterator;
		if (!linkIterator.hasNext()) {
			return;
		}
		ListIterator<Link> newlinkIterator;
		if (rerouting) {
			newlinkIterator = driver.updateRouteFrom(linkIterator.next());
			linkIterator.previous();
			if (newlinkIterator != null) {
				linkIterator = newlinkIterator;

				LOGGER.trace("New Route ");
			} else {
				LOGGER.trace("OLD Route ");
			}
		}

		// int li = 0;
		// System.err.println( "\n-----" );
		// System.err.println( "Distance:  " );
		// for (Iterator<Link> iter = copyLinkIter; iter.hasNext();) {
		// Link element = iter.next();
		// li++;
		// System.err.print( element.getId() + " " );
		//
		// }
		// while ( li-- > 0 ) {
		// copyLinkIter.previous();
		// }
		// System.err.println( " " );
		//
		// System.err.println( "    Time:  " );
		// for (Iterator<Link> iter = newlinkIterator; iter.hasNext();) {
		// Link element = iter.next();
		// li++;
		// System.err.print( element.getId() + " " );
		// }
		// while ( li-- > 0 ) {
		// newlinkIterator.previous();
		// }
		//
	}

	public Lane getBeforeLane() {
		return beforeLane;
	}

	public void setBeforeLane(Lane beforeLane) {
		this.beforeLane = beforeLane;
	}

    public LaneSwitch getLaneSwitch() {
        return switchToLane;
    }

	public void setLaneSwitch(LaneSwitch lane){
		this.switchToLane = lane;
	}

	// 2019

	public LaneRealExt getCurrentLane() {
		return currentLane;
	}

	public void setCurrentLane(LaneRealExt currentLane) {
		this.currentLane = currentLane;
	}
	
	//////////////////////////////////////////////////////////////////////////
	//	SYMULACJA
	
	/////////////////////////////////////////////////////////////////
	//	Lane Changes
	
	/**
	 * set lane switch state <br>
	 * {@link Car#switchLaneAlgorithm} for lane switch alforithm
	 * @return target lane
	 */
	private LaneRealExt setSwitchToLaneStateForAlgorithm() {
		if(this.switchToLane == LaneSwitch.WANTS_LEFT || this.switchToLane == LaneSwitch.WANTS_RIGHT) {
			// car already has an action and will try to do it this turn
			return getLaneFromLaneSwitchState();
		}
		
		int switchLaneForceLeft = this.currentLane.hasLeftNeighbor()	? this.switchLaneAlgorithm(this.currentLane.leftNeighbor())  : -1;
		int switchLaneForceRight = this.currentLane.hasRightNeighbor() 	? this.switchLaneAlgorithm(this.currentLane.rightNeighbor()) : -1;
		
		//	Choose best lane to switch base on gap to next car
		if(switchLaneForceLeft > switchLaneForceRight) {
			this.switchToLane = LaneSwitch.LEFT;	// left is better
		} else if(switchLaneForceLeft < switchLaneForceRight) {
			this.switchToLane = LaneSwitch.RIGHT;	// right is better
		} else if(switchLaneForceLeft == switchLaneForceRight && switchLaneForceLeft > 0) {
			this.switchToLane = LaneSwitch.RIGHT;	// its the same -> better to go right
		} else {
			this.switchToLane = LaneSwitch.NO_CHANGE;	// there are no good lanes to switch
		}
		return getLaneFromLaneSwitchState();
	}
	
	private LaneRealExt getLaneFromLaneSwitchState() {
		// risk : we should check if correct lanes exist	:: TODO
		switch(this.switchToLane) {
		case NO_CHANGE:
			return this.currentLane;
		case LEFT:
			return this.currentLane.leftNeighbor();
		case RIGHT:
			return this.currentLane.rightNeighbor();
		case WANTS_LEFT:
			return this.currentLane.leftNeighbor();
		case WANTS_RIGHT:
			return this.currentLane.rightNeighbor();
		default:
			throw new RuntimeException("wrong LaneSwitchState : " + this.switchToLane + " : no lane in this direction");
		}
	}
	
	
	///////////////////////////////////////////////////////////
	//		Switch Lane Algorithm
	/**
	 * check if lane neiLane is good to switch to and return its score
	 */
	private int switchLaneAlgorithm(LaneRealExt neiLane) {
		if(neiLane == null) return -1;
		Car neiCarBehind = neiLane.getBehindCar(this.pos);
		Car neiCarFront = neiLane.getFrontCar(this.pos);
		Car thisCarFront = this.currentLane.getFrontCar(this.pos);
		// gap - number of free cells : [c] [] [] [c] -> gap == 2
		int gapNeiFront = 	neiCarFront != null  ? neiCarFront.getPosition() - this.pos - 1  : neiLane.linkLength() - this.pos -1;

		if(isMyLaneBad(thisCarFront) && isOtherLaneBetter(thisCarFront, neiCarFront, neiLane) && canSwitchLanesToOther(neiCarBehind, neiCarFront, neiLane)) {
			return gapNeiFront;	// score for this lane switch
		}
		return -1;
	}
	
	private boolean isMyLaneBad(Car carInFront) {
		int gapThisFront = 	carInFront != null ? carInFront.getPosition() - this.pos - 1 : this.currentLane.linkLength() - this.pos -1;
		return gapThisFront <= this.velocity;
	}
	
	private boolean isOtherLaneBetter(Car carInFront, Car otherCarFront, LaneRealExt otherLane) {
		int gapThisFront = 	carInFront != null ? carInFront.getPosition() - this.pos - 1 : this.currentLane.linkLength() - this.pos -1;
		System.out.println(otherCarFront);
		int gapNeiFront = 	otherCarFront != null  ? otherCarFront.getPosition() - this.pos - 1  : otherLane.linkLength() - this.pos -1;
		return gapNeiFront > gapThisFront;
	}
	
	private boolean canSwitchLanesToOther(Car otherCarBehind, Car otherCarFront, LaneRealExt otherLane) {
		int gapNeiFront = 	otherCarFront != null  ? otherCarFront.getPosition() - this.pos - 1  : otherLane.linkLength() - this.pos -1;
		int gapNeiBehind = 	otherCarFront != null ? this.pos - otherCarFront.getPosition() - 1 : this.pos;
		int crashFreeTurns = 1;	// turns until crash, gap must be bigger than velocity * crashFreeTurns
		boolean spaceInFront = gapNeiFront > this.velocity * crashFreeTurns;
		boolean spaceBehind = otherCarBehind!=null ? gapNeiBehind > otherCarBehind.getVelocity() * crashFreeTurns : true;
		return spaceInFront && spaceBehind;
	}
	//		[end] Switch Lane Algorithm
	///////////////////////////////////////////////////////////
	
	/**
	 * uses part of Switch Lane Algorithm 
	 * @return true if car can switch lane in given direction
	 */
	private boolean checkIfCanSwitchToDirection(LaneSwitch direction) {
		LaneRealExt otherLane;
		if(direction == LaneSwitch.LEFT || direction == LaneSwitch.WANTS_LEFT) {
			otherLane = this.currentLane.leftNeighbor();
		} else if(direction == LaneSwitch.RIGHT || direction == LaneSwitch.WANTS_RIGHT) {
			otherLane = this.currentLane.rightNeighbor();
		} else {
			return true;	// can always switch if there is not switch
		}
		Car carBehind = otherLane.getBehindCar(this.pos);
		Car carFront = otherLane.getFrontCar(this.pos);
		return canSwitchLanesToOther(carBehind, carFront, otherLane);
		
	}

	/**
	 * Check if there is need to switch lanes (obstacle, emergency etc) <br>
	 * If not check switchLaneAlgorithms on all neighbors lanes <br>
	 * Action for obstacle or emergency have priority <br>
	 * TODO: poprawić zmiane pasow przed przeszkodami i przed karetkami 
	 * @param action action for car that will be switching lanes
	 * @param lane switchLaneAlgorithm
	 * @param ev
	 * @return correct switch lane state
	 */
	void switchLanesState(){
		//	if car is not emergency or car behind me is emergency - go right
		// 	if obstacle in front - try to switch
		//	if car dont want to switch - check setSwitchToLaneStateForAlgorithm - maybe it will switch
		
		//LaneRealExt newTargetLane;	// we can check if our target lane contains obstacles, emergency and prevent switch if needed

		// calculate distance to nearest obstacle, must be not more than obstacleVisibility param
		int obstacleVisibility = Integer.parseInt(KraksimConfigurator.getPropertiesFromFile().getProperty("obstacleVisibility"));
		int distanceToNextObstacle = Integer.MAX_VALUE;
		for(Integer obstacleIndex : this.currentLane.getLane().getActiveBlockedCellsIndexList()) {
			int dist = obstacleIndex - getPosition();	// [C] --> [o]
			if(dist < 0) continue;
			distanceToNextObstacle = Math.min(distanceToNextObstacle, dist);
		}
		
		//	check for obstacles
		if(distanceToNextObstacle <= obstacleVisibility) { 
			// obstacle in range, must change lane, prefers right, but if cant, will try to left
			System.out.println("Przeszkoda?!?! o nie!!	QQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ");
			if(checkIfCanSwitchToDirection(LaneSwitch.RIGHT)) {
				this.switchToLane = LaneSwitch.RIGHT;
			} else if(checkIfCanSwitchToDirection(LaneSwitch.LEFT)) {
				this.switchToLane = LaneSwitch.LEFT;
			} else {
				this.switchToLane = LaneSwitch.NO_CHANGE;
			}
				
		}
		else if (!isEmergency() && this.currentLane.getBehindCar(this)!= null && this.currentLane.getBehindCar(this).isEmergency()) {
			if(checkIfCanSwitchToDirection(LaneSwitch.RIGHT)) {
				this.switchToLane = LaneSwitch.RIGHT;
			} else {
				this.switchToLane = LaneSwitch.NO_CHANGE;
			}
		} else {
			this.setSwitchToLaneStateForAlgorithm();
		}
		
		if(!checkIfCanSwitchToDirection(this.switchToLane)) {	// only to make sure it works
			throw new RuntimeException("FALSE :: checkIfCanSwitchToDirection(this.switchToLane)");		///////////////////////////////////////////////////////////////////////////////////////////////
		}
		
		//newTargetLane = this.getLaneFromLaneSwitchState();	// use to prevent from switching if needed
	}
	
	/**
	 * Check if there is need to switch lanes (obstacle, emergency etc) <br>
	 * If not check switchLaneAlgorithms on all neighbors lanes <br>
	 * Action for obstacle or emergency have priority 
	 * @param action action for car that will be switching lanes
	 * @param lane switchLaneAlgorithm
	 * @param ev
	 * @return correct switch lane state
	 * @deprecated
	 */
	void switchLanes(Action action, LaneRealExt lane, RealEView ev){
		//	action.getSource() - current line
		//	action.getTarget() - line to switch to
		Lane sourceLane = action.getSource();
		LaneRealExt sourceLaneReal = ev.ext(sourceLane);
		LaneSwitch direction;
		//	if car is not emergency or car behind me is emergency - go right
		//	if car dont want to switch - check getLaneToSwitch - maybe it will switch
		//	if car want to switch - switch to that line
		if(sourceLaneReal.getFrontCar(this) != null) System.out.println("in front: " + sourceLaneReal.getFrontCar(this).toString());

		// if obstacle is close
		int obstacleVisibility = Integer.parseInt(KraksimConfigurator.getPropertiesFromFile().getProperty("obstacleVisibility"));
		int distanceToNextObstacle = Integer.MAX_VALUE;
		for(Integer obstacleIndex : lane.getLane().getActiveBlockedCellsIndexList()) {
			int dist = obstacleIndex - getPosition();	// [C] --> [o]
			if(dist < 0) continue;
			distanceToNextObstacle = Math.min(distanceToNextObstacle, dist);
		}
		if(distanceToNextObstacle <= obstacleVisibility) { // if next is obstacle "this.getFrontCar(car, sourceLane) != null && this.getFrontCar(car, sourceLane).isObstacle()"
			System.out.println("Przeszkoda?!?! o nie!!	QQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ");
			float obstacleSwitchRandom = lane.getParams().getRandomGenerator().nextFloat();
			if(obstacleSwitchRandom < 0.5) {
				direction = LaneSwitch.LEFT;
			} else {
				direction = LaneSwitch.RIGHT;
			}
		}
		else {
//			if ((!isEmergency()) && sourceLaneReal.getBehindCar(this)!= null && sourceLaneReal.getBehindCar(this).isEmergency()) {
//			direction = LaneSwitch.RIGHT;
//		} else {
			this.setSwitchToLaneStateForAlgorithm();//lane.getLaneToSwitch(this, sourceLane);
			direction = this.getLaneSwitch();
		}
		LaneRealExt sourceLaneExt = lane.getRealView().ext(sourceLane);

		/* check if lane can be switched to, if so, switch */
		List<Car> neighbourCars;	// cars on new lane (if not switched it is not importatnt)
		Lane newSourceLane;

		int laneCount = sourceLane.getOwner().laneCount();
		int laneAbsouteNumber = sourceLane.getAbsoluteNumber();	// lane number in line (not index in list)

		if (direction == LaneSwitch.RIGHT) {
			if(laneAbsouteNumber + 1 > laneCount - 1){	// if there is no line on right cant switch to right
				System.out.println("no can do #1");
				setLaneSwitch(LaneSwitch.NO_CHANGE);
				return;
			}
			neighbourCars = sourceLaneExt.rightNeighbor().getCars();
			newSourceLane = sourceLaneExt.rightNeighbor().getLane();
		} else if (direction == LaneSwitch.LEFT) {
			if(laneAbsouteNumber - 1 < 0){
				System.out.println("no can do #1.5 <left>");
				setLaneSwitch(LaneSwitch.NO_CHANGE);
				return;
			}
			neighbourCars = sourceLaneExt.leftNeighbor().getCars();
			newSourceLane = sourceLaneExt.leftNeighbor().getLane();
		} else return; // do not switch lanes

		// find car right after and behind current car in neighbouring lane
		Car behindCar = null, afterCar = null;

		// cars not necessarily must be listed in any order on the lane
		int minPositiveDistance = Integer.MAX_VALUE;
		int minNegativeDistance = Integer.MIN_VALUE;
		System.out.println("carsDistance");
		for (Car _car : neighbourCars) {
			int carsDistance = getPosition() - _car.getPosition();
			System.out.print(carsDistance + " ");
			if (minPositiveDistance > carsDistance && carsDistance >= 0 ) {
				minPositiveDistance = carsDistance;
				behindCar = _car;
				System.out.print("behind set ");
			} else if (minNegativeDistance < carsDistance && carsDistance < 0){
				minNegativeDistance = carsDistance;
				afterCar = _car;
				System.out.print("after set ");
			}
		}
		System.out.println("carsDistance END");

		int distance, vRelative, vCurrentCar = getVelocity();
		double crashTime;

		boolean behindCond = true, afterCond = true;

		// car behind current car
		if(behindCar != null) {
			distance = minPositiveDistance;
			vRelative = vCurrentCar - behindCar.getVelocity();

			crashTime = vRelative != 0 ? distance/(double)vRelative : 0;
			System.out.println("SWITCH_TIME " + lane.SWITCH_TIME + " MIN_SAFE_DISTANCE " + lane.MIN_SAFE_DISTANCE +" crashTime " + crashTime + " minPositiveDistance " + minPositiveDistance);
			if (Math.abs(crashTime) < lane.SWITCH_TIME || minPositiveDistance < lane.MIN_SAFE_DISTANCE) {
				System.out.println("no can do #2");
				behindCond = false;

				// TODO:  velocity correction, car has to increase its speed in order to attempt lane switching in next turn
				setVelocity(getVelocity()-1);
			}
		}
		// switch if there will be no crash
		if (afterCar != null) {
			distance = minNegativeDistance;
			vRelative = vCurrentCar - afterCar.getVelocity();

			crashTime = vRelative != 0 ? distance/(double)vRelative : Integer.MAX_VALUE;
			if(Math.abs(crashTime) < lane.SWITCH_TIME || Math.abs(minNegativeDistance) < lane.MIN_SAFE_DISTANCE){
				System.out.println("no can do #3");
				afterCond = false;

				// velocity correction
				setVelocity(getVelocity() - 1);
			}
		}

		if (afterCond && behindCond) {
			action.setSource(newSourceLane);
			setLaneSwitch(LaneSwitch.NO_CHANGE); // lanes switched
		} else {
			System.out.println("no can do #4");
			System.out.println("Switch lane failed in " + direction.toString());
			System.out.println(afterCar + " ||| " + behindCar);
			//setLaneSwitch(direction);
		}
		System.out.println("EOF");
	}
	
	//	[end] Lane Changes
	/////////////////////////////////////////////////////////////////
	
	/**
	 * Nagel-Schreckenberg
	 */
	void simulateTurn() {
		LOGGER.trace("car simulation : " + this);
		
		if(this.isObstacle()) {	// dont simulate obstacles
			return;
		}
		if(!this.canMoveThisTurn()) {	// car already did this turn
			return;
		}
		
		InductionLoopPointer ilp = this.currentLane.new InductionLoopPointer();
		Car nextCar = this.currentLane.getFrontCar(this);
		
		// remember starting point
		this.setBeforeLane(this.currentLane.getLane());
		this.setBeforePos(this.getPosition());
		
		// Acceleration
		if (this.isEmergency()) {
			if (this.velocity < this.currentLane.getEmergencySpeedLimit()) {
				this.velocity += this.currentLane.getEmergencyAcceleration();
			}
		} else {
			if (this.velocity < this.currentLane.getSpeedLimit()) {
				this.velocity += 1;
			}
		}
		
		handleCorrectModel(nextCar);
		
		driveCar(nextCar);
		
	}
	
	/**	Perform action based on current car move model
	 */
	private void handleCorrectModel(Car nextCar) {
		boolean velocityZero = this.getVelocity() <= 0; // VDR - check for v = 0 (slow start)
		float decisionChance = this.currentLane.getParams().getRandomGenerator().nextFloat();
		CarMoveModel carMoveModel = this.currentLane.getCarMoveModel();
		switch (carMoveModel.getName()) {
		case CarMoveModel.MODEL_NAGEL:
			if (decisionChance < carMoveModel.getFloatParameter(CarMoveModel.MODEL_NAGEL_MOVE_PROB)) {
				velocity--;
			}
			break;
		case CarMoveModel.MODEL_MULTINAGEL: // is used by default
			if (decisionChance < carMoveModel.getFloatParameter(CarMoveModel.MODEL_MULTINAGEL_MOVE_PROB)) {
				velocity--;
			}
			System.out.println("setActionMultiNagel");
			//setActionMultiNagel();
			this.switchLanesState();
			break;
		// deceleration if vdr
		case CarMoveModel.MODEL_VDR:
			// if v = 0 => different (greater) chance of deceleration
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
		// Brake light model
		case CarMoveModel.MODEL_BRAKELIGHT:
			if (velocityZero) {
				if (decisionChance < carMoveModel.getFloatParameter(CarMoveModel.MODEL_BRAKELIGHT_0_PROB)) {
					--velocity;
				}
			} else {
				if (nextCar != null && nextCar.isBraking()) {
					int threshold = carMoveModel.getIntParameter(CarMoveModel.MODEL_BRAKELIGHT_DISTANCE_THRESHOLD);
					double ts = (threshold < velocity) ? threshold : velocity;
					double th = (nextCar.getPosition() - this.getPosition()) / (double) velocity;
					if (th < ts) {
						if (decisionChance < carMoveModel.getFloatParameter(CarMoveModel.MODEL_BRAKELIGHT_BRAKE_PROB)) {
							--velocity;
							this.setBraking(true, this.isEmergency());
						} else {
							this.setBraking(false, this.isEmergency());
						}
					}
				} else {
					if (decisionChance < carMoveModel.getFloatParameter(CarMoveModel.MODEL_BRAKELIGHT_MOVE_PROB)) {
						--velocity;
						this.setBraking(true, this.isEmergency());
					} else {
						this.setBraking(false, this.isEmergency());
					}
				}
			}
			break;
		default:
			throw new RuntimeException("unknown model! " + carMoveModel.getName());
		}

	}

	void driveCar(Car nextCar) {
		if(this.switchToLane != LaneSwitch.NO_CHANGE) {
			this.changeLanes(this.getLaneFromLaneSwitchState());
			nextCar = this.currentLane.getFrontCar(this);	// nextCar changed
		}
		int freeCellsInFront;
		if (nextCar != null) {
			freeCellsInFront = nextCar.getPosition() - this.pos - 1 - 1;
		} else {
			freeCellsInFront = this.currentLane.linkLength() - this.pos -1;
		}
		
		//	move car velocity forward if lane ended  do intersection function
		int distanceTraveled = 0;
		if(freeCellsInFront >= this.velocity) {	// simple move forward
			distanceTraveled = this.velocity;
		} else if (nextCar != null) {	// there is car in front
			distanceTraveled = freeCellsInFront;
		} else if(this.getPreferableAction() != null){	// road ended, interaction
			distanceTraveled = freeCellsInFront;
			// TODO: interaction crossing
		} else {	// road ended, gateway
			((GatewayRealExt) this.currentLane.getRealView().ext(this.currentLane.linkEnd())).acceptCar(this);
			this.currentLane.removeCarFromLane(this);
			return;
		}
		this.setPosition(this.pos + distanceTraveled);
	}

	/**
	 * previous element to ilp.current() (if exists) should be an induction loop
	 * with line <= startPos.
	 *
	 * the same pointer can be used to the next car on this lane (above assumption
	 * will be true)
	 */
	boolean drive(LaneRealExt lane, int startPos, int freePos, int stepsMax, int stepsDone,
			LaneRealExt.InductionLoopPointer ilp, boolean entered) {
		LOGGER.trace("CARTURN " + this + "on " + lane.getLane());
		int range = startPos + stepsMax - stepsDone;
		int pos;
		boolean stay = false;
		Action action = getAction();
		LaneRealExt sourceLane = lane.getSourceLane(action);

		/* last line of this link crossed by the car in this turn */
		int lastCrossedLine;

		if (!lane.equals(sourceLane)) {
			System.out.println("new lane :: at road" + sourceLane.getLane().getOwner().getId() + " lane : "
					+ sourceLane.getLane().getAbsoluteNumber());
			int laneChangePos = Math.max(sourceLane.getOffset() - 1, getPosition());
			pos = Math.min(Math.min(range, freePos), laneChangePos);

			if (pos == range || pos < laneChangePos
					|| !sourceLane.pushCar(this, stepsMax, stepsDone + pos - startPos)) {
				stay = true;
			}
			lastCrossedLine = pos;
		} else {
			int lastPos = lane.linkLength() - 1;
			pos = Math.min(Math.min(range, freePos), lastPos);
			if (pos == range || pos < lastPos || lane.isBlocked()
					|| !handleCarAction(lane, stepsMax, stepsDone + pos - startPos)) {
				stay = true;
				lastCrossedLine = pos;
			} else {
				lastCrossedLine = pos + 1;
			}
		}

		if (stay) {
			if (getPosition() < pos) {
				setPosition(pos);
			}
			setVelocity(stepsDone + pos - startPos);
			if (getVelocity() < 0) {
				setVelocity(0);
			}
			if (entered) {
				System.out.println("entered :: at road " + sourceLane.getLane().getOwner().getId() + " lane : "
						+ sourceLane.getLane().getAbsoluteNumber());
				lane.getEnteringCars().add(this);
			}
		}

		LOGGER.trace("CARTURN " + this + " crossed " + lastCrossedLine);
		/* We fire all induction loops in the range (startPos; lastCrossedLine] */
		while (!ilp.atEnd() && ilp.current().line <= lastCrossedLine) {
			if (ilp.current().line > startPos) {
				LOGGER.trace(">>>>>>> INDUCTION LOOP before " + startPos + " and " + lastCrossedLine + " for "
						+ lane.getLane());
				ilp.current().handler.handleCarDrive(getVelocity(), getDriver());
			}

			ilp.forward();
		}

		LOGGER.trace("CARTURN " + this + "on " + lane.getLane());
		this.switchToLane = LaneSwitch.NO_CHANGE;
		return stay;
	}

	/* assumption: stepsDone < stepsMax */
	/**
	 * moves cars across intersection
	 */
	private boolean handleCarAction(LaneRealExt lane, int stepsMax, int stepsDone) {
		LOGGER.trace(this + " on " + lane.getLane());
		Action action = getAction();

		if (action == null) {
			setVelocity(stepsMax);
			((GatewayRealExt) lane.getRealView().ext(lane.linkEnd())).acceptCar(this);
			return true;
		}

		if (lane.getWait()) {
			/* we are waiting one turn */
			lane.setWait(false);
			return false;
		} else {
			/* we are approaching an intersection */
			Lane[] pl = action.getPriorLanes();
			// int i;
			for (Lane aPl : pl) {
				if (lane.getRealView().ext(aPl).getCarApproaching()) {
					if (lane.checkDeadlock(action.getSource(), aPl)) {
						LOGGER.warn(lane.getLane() + "DEADLOCK situation.");
						lane.deadLockRecovery();
					}
					return false;
				}
			}
			LinkRealExt l = lane.getRealView().ext(action.getTarget());
			setPosition(0);

			return l.enterCar(this, stepsMax, stepsDone);
		}
	}

	/**
	 * removes car from current lane and adds it to otherLane
	 * changes this.currentLane
	 * @param otherLane
	 *            new lane for this car
	 */
	public void changeLanes(LaneRealExt otherLane) {
		this.currentLane.removeCarFromLane(this);
		otherLane.addCarToLane(this);
		Car cx = this.currentLane.getCars().peek();
		// update lane firstCarPos for old lane
		if (cx != null) {
			this.currentLane.setFirstCarPos(cx.getPosition());
		} else {
			this.currentLane.setFirstCarPos(Integer.MAX_VALUE);
		}
		this.currentLane = otherLane;
	}

	/**
	 * removes car from current lane and moves it across the intersection
	 * changes this.currentLane
	 * @param
	 */
	public void crossIntersection(LaneRealExt otherLane) {
		this.currentLane.removeCarFromLane(this);
		otherLane.addCarToLane(this);
		this.currentLane = otherLane;
	}

	/**
	 * Has to be fired after move is simulated
	 */
	public void updateTurnNumber() {
		this.updateInTurn = Simulation.turnNumber;
	}

	/**
	 * @return true if car can move this turn
	 * @return false if car was already moved this turn
	 */
	public boolean canMoveThisTurn() {
		return this.updateInTurn < Simulation.turnNumber;
	}
}

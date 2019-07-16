package pl.edu.agh.cs.kraksim.real_extended;

import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.KraksimConfigurator;
import pl.edu.agh.cs.kraksim.core.Action;
import pl.edu.agh.cs.kraksim.core.Lane;
import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.core.Node;
import pl.edu.agh.cs.kraksim.iface.sim.Route;
import pl.edu.agh.cs.kraksim.main.drivers.Driver;

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
			return driver + " in [ CAR bPos=" + beforePos + ",cPos=" + pos + ",v=" + velocity + " lane: " + "null"+ ']';
			
		}
		return driver + " in [ CAR bPos=" + beforePos + ",cPos=" + pos + ",v=" + velocity + " lane: " + this.currentLane.getLane().getAbsoluteNumber()+ ']';
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
	
	/*
	 * set lane switch state 
	 */
	private void setSwitchToLaneState() {
		if(this.switchToLane == LaneSwitch.WANTS_LEFT || this.switchToLane == LaneSwitch.WANTS_RIGHT) {
			// car already has an action and will try to do it this turn
			return;
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

	}
	
	/*
	 * check if lane neiLane is good to switch to and return its score
	 */
	private int switchLaneAlgorithm(LaneRealExt neiLane) {
		if(neiLane == null) return -1;
		Car neiCarBehind = neiLane.getBehindCar(this.pos);
		Car neiCarFront = neiLane.getFrontCar(this.pos);
		Car thisCarFront = this.currentLane.getFrontCar(this.pos);
		int gapNeiBehind = 	neiCarBehind != null ? this.pos - neiCarBehind.getPosition() : this.pos;
		int gapNeiFront = 	neiCarFront != null  ? neiCarFront.getPosition() - this.pos  : neiLane.linkLength() - this.pos -1;
		int gapThisFront = 	thisCarFront != null ? thisCarFront.getPosition() - this.pos : this.currentLane.linkLength() - this.pos -1;
		
		int weight1;	// is my lane bad and other lane better
		if(gapThisFront < this.velocity && gapNeiFront > gapThisFront) {
			weight1 = 1;
		} else {
			weight1 = 0;
		}
		int weight2 = this.velocity - gapNeiFront;	// I dont have to slow down on nei Lane
		int weight3 = neiCarBehind != null ? neiCarBehind.getVelocity() - gapNeiBehind : -1;	// will car behind me crash into me
		// int weight4 
		System.out.println("swLnAlgo :: gapNeiBehind " + gapNeiBehind + " neiCarFront " + gapNeiFront + " thisCarFront " + gapThisFront
				+ "\n\tweight1 " + weight1 + " weight2 " + weight2 + " weight3 " + weight3 + " result " + (weight1 > weight2 && weight1 > weight3));
		if(weight1 > 0 && weight1 > weight2 && weight1 > weight3) {
			return gapNeiFront;	// score for this lane switch
		}
		return -1;
	}

	/*
	 * previous element to ilp.current() (if exists) should be an induction loop
	 * with line <= startPos.
	 *
	 * the same pointer can be used to the next car on this lane (above
	 * assumption will be true)
	 */
	boolean drive(LaneRealExt lane, int startPos, int freePos, int stepsMax, int stepsDone, LaneRealExt.InductionLoopPointer ilp, boolean entered) {
		LOGGER.trace("CARTURN " + this + "on " + lane.getLane());
		int range = startPos + stepsMax - stepsDone;
		int pos;
		boolean stay = false;
		Action action = getAction();
		LaneRealExt sourceLane = lane.getSourceLane(action);

		/* last line of this link crossed by the car in this turn */
		int lastCrossedLine;

		if (!lane.equals(sourceLane)) {
			int laneChangePos = Math.max(sourceLane.getOffset() - 1, getPosition());
			pos = Math.min(Math.min(range, freePos), laneChangePos);

			if (pos == range || pos < laneChangePos || !sourceLane.pushCar(this, stepsMax, stepsDone + pos - startPos)) {
				stay = true;
			}
			lastCrossedLine = pos;
		} else {
			int lastPos = lane.linkLength() - 1;
			pos = Math.min(Math.min(range, freePos), lastPos);
			if (pos == range || pos < lastPos || lane.isBlocked() || !handleCarAction(lane, stepsMax, stepsDone + pos - startPos)) {
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
				lane.getEnteringCars().add(this);
			}
		}

		LOGGER.trace("CARTURN " + this + " crossed " + lastCrossedLine);
		/* We fire all induction loops in the range (startPos; lastCrossedLine] */
		while (!ilp.atEnd() && ilp.current().line <= lastCrossedLine) {
			if (ilp.current().line > startPos) {
				LOGGER.trace(">>>>>>> INDUCTION LOOP before " + startPos + " and " + lastCrossedLine + " for " + lane.getLane());
				ilp.current().handler.handleCarDrive(getVelocity(), getDriver());
			}

			ilp.forward();
		}

		LOGGER.trace("CARTURN " + this + "on " + lane.getLane());
		return stay;
	}

	/* assumption: stepsDone < stepsMax */
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
	 * Check if there is need to switch lanes and, if it's possible, do so.
	 * @param action action for car that will be switching lanes
	 * @param lane
	 * @deprecated Use {@link #switchLanes(Action,LaneRealExt,RealEView)} instead
	 */
	void switchLanes(Action action, LaneRealExt lane){
		switchLanes(action, lane, null);
	}

	/**
	 * Check if there is need to switch lanes and, if it's possible, do so.
	 * @param action action for car that will be switching lanes
	 * @param lane
	 * @param ev
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
		else if ((!isEmergency()) && sourceLaneReal.getBehindCar(this)!= null && sourceLaneReal.getBehindCar(this).isEmergency()) {
			direction = LaneSwitch.RIGHT;
		} else {
			this.setSwitchToLaneState();//lane.getLaneToSwitch(this, sourceLane);
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
			setLaneSwitch(direction);
		}
		System.out.println("EOF");
	}
}

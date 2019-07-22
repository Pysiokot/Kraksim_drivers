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
	private Action actionForNextIntersection;
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
	// 	random value for each turn of choosing correct lane switch method, 
	//	if < switchLaneActionProbability -> algorithm
	//	if > switchLaneActionProbability -> switch lane for intersection
	private double switchLaneMethodRandom;	
	private int numOfTurnsInWantedSwitchLane = 0;	// number of turns car is in LaneSwitch.WANT_ ... -> used to reduce speed to 0 if needed

	private LaneSwitch switchToLane = LaneSwitch.NO_CHANGE;
	
	private enum SwitchLaneMethod  { INTERSECTION_LANE, LOCAL_TRAFIC_ALGORITHM }
	private SwitchLaneMethod switchLaneMethod;
	
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
	
	/** @return min( velocity + 1 , Speed Limit )	 */
	public int getFutureVelocity() {
		return Math.min(velocity + 1, this.currentLane.getSpeedLimit());
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
		System.out.println("nextTripPoint ");
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

	@Deprecated
	public Action getAction() {
		LOGGER.trace("\n Action= " + action + "\n Driver= " + driver);
		return action;
	}

	@Deprecated
	public void setAction(Action action) {
		if (isTEST2013Enabled) {
			TEST2013onNewAction(action);
		}

		LOGGER.trace("\n Action= " + action + "\n Driver= " + driver);
		this.action = action;
	}

	public Action getActionForNextIntersection(){
		return this.actionForNextIntersection;
	}
	
	public void setActionForNextIntersection(Action actionForNextIntersection){
		this.actionForNextIntersection = actionForNextIntersection;
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
//	SIMULATION
	
/////////////////////////////////////////////////////////////////
//	Lane Changes Methods
	
	/** formula for calculating probability based on current position <br>
	 *   ~(distance_traveled / lane_length) with sharp limit at the end <br>
	 *   more if car is closer to the end
	 */
	private double switchLaneActionProbability() {
		int d_linkLength = this.currentLane.linkLength();
		int d_intersection = d_linkLength - this.getPosition() -1;
		int maxSpeed = this.isEmergency() ? this.currentLane.getEmergencySpeedLimit() : this.currentLane.getSpeedLimit();
		int t_limitDistancetoIntersection = this.currentLane.INTERSECTION_LANE_SWITCH_TURN_LIMIT;	// const from config file
		int d_limitDistancetoIntersection = t_limitDistancetoIntersection * maxSpeed;	// limit distance to intersection
		int t_currentToIntersectionMaxSpeed = Math.floorDiv(d_intersection, maxSpeed);
		if(t_limitDistancetoIntersection >= t_currentToIntersectionMaxSpeed)	return 1;	// we are too close to intersection
		double prob = (double)(this.getPosition() - d_limitDistancetoIntersection) / (double)(d_linkLength - d_limitDistancetoIntersection);
		System.out.println("prob " + prob + " d_limitDistancetoIntersection " + d_limitDistancetoIntersection + " d_linkLength " + d_linkLength + " t_limitDistancetoIntersection " + t_limitDistancetoIntersection);
		return Math.pow(prob, this.currentLane.PROBABILITY_POWER_VALUE);
	}
	
	/**
	 * @return true if current lane allows to cross intersection
	 */
	private boolean isThisLaneGoodForNextIntersection() {
		return isGivenLaneGoodForNextIntersection(this.currentLane.getLane());
	}
	
	/**
	 * @return true if given lane allows to cross intersection
	 */
	private boolean isGivenLaneGoodForNextIntersection(Lane givenLane) {
		Action actionIntersection = this.getActionForNextIntersection();
		int laneIntersectionAbs = actionIntersection.getSource().getAbsoluteNumber();
		int laneIntersectionRel = actionIntersection.getSource().getRelativeNumber();
		int currentLaneAbs = givenLane.getAbsoluteNumber();
		int currentLaneRel = givenLane.getRelativeNumber();
		if(laneIntersectionRel == 0) {	// main lane is good, can be on any lane in main group
			return currentLaneRel == 0;
		} else if(laneIntersectionRel < 0) {	// left turn
			return currentLaneRel < 0;
		} else {	// right turn
			return currentLaneRel > 0;
		}
	}
	
	/**
	 * @return true if direction will change lane to good one
	 * @return true if direction will move car closer to good lane
	 */
	private boolean isDirectionBetterForNextIntersection(LaneSwitch direction) {
		LaneRealExt targetLane = this.getLaneFromDirection(direction);
		//System.out.println("isDirectionBetterForNextIntersection\n\t direction " + direction + " targetLane " + targetLane.getLane().getAbsoluteNumber() + " : " + targetLane.getLane().getRelativeNumber());
		return isLaneBetterForNextIntersection(targetLane);
	}
	
	/**
	 * @return true if targetLane is good for intersection
	 * @return true if targetLane will move car closer to good lane
	 */
	private boolean isLaneBetterForNextIntersection(LaneRealExt targetLane) {
		if(targetLane == null)	return false;	// no lane cant be better
		Action actionIntersection = this.getActionForNextIntersection();
		int laneIntersectionAbs = actionIntersection.getSource().getAbsoluteNumber();
		int laneIntersectionRel = actionIntersection.getSource().getRelativeNumber();
		System.out.println("isLaneBetterForNextIntersection\n\t targetLane " + targetLane.getLane().getAbsoluteNumber() + " : " + targetLane.getLane().getRelativeNumber() 
				+" goal " + laneIntersectionAbs + " : " + laneIntersectionRel
				+ " target dif " + Math.abs(targetLane.getLane().getAbsoluteNumber() - laneIntersectionAbs) + " now dif " + Math.abs(this.currentLane.getLane().getAbsoluteNumber() - laneIntersectionAbs));
		return (this.isGivenLaneGoodForNextIntersection(targetLane.getLane())
				|| Math.abs(targetLane.getLane().getAbsoluteNumber() - laneIntersectionAbs) < Math.abs(this.currentLane.getLane().getAbsoluteNumber() - laneIntersectionAbs)
				);
	}
	
	/**
	 * set lane switch state to move car closer to correct lane for next intersection
	 */
	private void setSwitchToLaneStateForIntersection() {
		System.out.println("setSwitchToLaneStateForIntersection\n\tleft? " + isDirectionBetterForNextIntersection(LaneSwitch.LEFT) + " " + checkIfCanSwitchToDirection(LaneSwitch.LEFT)
				+ "\tright? " + isDirectionBetterForNextIntersection(LaneSwitch.RIGHT) + " " + checkIfCanSwitchToDirection(LaneSwitch.RIGHT));
		if(isThisLaneGoodForNextIntersection()) {
			this.switchToLane = LaneSwitch.NO_CHANGE;	// current lane is good
			return;
		}
		if(isDirectionBetterForNextIntersection(LaneSwitch.LEFT)) {
			// left is better
			if(this.checkIfCanSwitchToDirection(LaneSwitch.LEFT)) {
				this.switchToLane = LaneSwitch.LEFT;
			} else {
				if(this.currentLane.getParams().getRandomGenerator().nextDouble() < this.switchLaneActionProbability()) {
					this.switchToLane = LaneSwitch.WANTS_LEFT;	
					numOfTurnsInWantedSwitchLane = 0;
				} else {
					this.switchToLane = LaneSwitch.NO_CHANGE;	
				}
			}
		} else if(isDirectionBetterForNextIntersection(LaneSwitch.RIGHT)) {
			// left is better
			if(this.checkIfCanSwitchToDirection(LaneSwitch.RIGHT)) {
				this.switchToLane = LaneSwitch.RIGHT;
			} else {
				if(this.currentLane.getParams().getRandomGenerator().nextDouble() < this.switchLaneActionProbability()) {
					this.switchToLane = LaneSwitch.WANTS_RIGHT;
					numOfTurnsInWantedSwitchLane = 0;
				} else {
					this.switchToLane = LaneSwitch.NO_CHANGE;	
				}
			}
		} else {
			throw new RuntimeException("no good action for next intersection");			
		}
		
		if(!this.getLaneFromLaneSwitchState().getLane().existsAtThisPosition(pos)) {
			// Target lane starts later, nothing to do, we are on best possible lane
			this.switchToLane = LaneSwitch.NO_CHANGE;
		}
	}
	
	/**
	 * set lane switch state looking at local situation on the road <br>
	 * reacts differently if in intersection lane switch action
	 * {@link Car#switchLaneAlgorithm} for lane switch algorithm
	 */
	private void setSwitchToLaneStateForAlgorithm() {
		if(this.switchToLane == LaneSwitch.WANTS_LEFT || this.switchToLane == LaneSwitch.WANTS_RIGHT) {
			// car already has an action and will try to do it this turn
			return;
		}
		if(velocity == 0) throw new RuntimeException("vel == 0");
		int switchLaneForceLeft = -1;
		int switchLaneForceRight = -1;

		if(this.currentLane.hasLeftNeighbor()
			&& !(this.currentLane.leftNeighbor().getLane().isMainLane() ^ this.currentLane.getLane().isMainLane())	//nXOR
			&& (this.currentLane.leftNeighbor().getLane().isMainLane() || this.isGivenLaneGoodForNextIntersection(this.currentLane.leftNeighbor().getLane()))
			&& (this.switchLaneMethod == SwitchLaneMethod.LOCAL_TRAFIC_ALGORITHM || this.isLaneBetterForNextIntersection(this.currentLane.leftNeighbor()))
			) {
			// switch to left if left lane exists, is a main lane and left lane is correct for next interaction if it needs to be (distance based probability)
			switchLaneForceLeft = this.switchLaneAlgorithm(this.currentLane.leftNeighbor());	
		}
		
		if(this.currentLane.hasRightNeighbor()
			&& !(this.currentLane.rightNeighbor().getLane().isMainLane() ^ this.currentLane.getLane().isMainLane())	//nXOR
			&& (this.currentLane.rightNeighbor().getLane().isMainLane() || this.isGivenLaneGoodForNextIntersection(this.currentLane.rightNeighbor().getLane()))
			&& (this.switchLaneMethod == SwitchLaneMethod.LOCAL_TRAFIC_ALGORITHM || this.isLaneBetterForNextIntersection(this.currentLane.rightNeighbor()))
			) {
				// switch to left if right lane exists, is a main lane and right lane is correct for next interaction if it needs to be (distance based probability)
			switchLaneForceRight = this.switchLaneAlgorithm(this.currentLane.rightNeighbor());	
			}
		
		System.out.println("switchLaneForceLeft " + switchLaneForceLeft);
		System.out.println("switchLaneForceRight " + switchLaneForceRight);
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
	
	/** @return lane car want to switch to */
	private LaneRealExt getLaneFromLaneSwitchState() {
		return getLaneFromDirection(this.switchToLane);
	}
	
	/** @return lane in given direction from current */
	private LaneRealExt getLaneFromDirection(LaneSwitch direction) {
		try {
			switch(direction) {
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
		} catch (java.lang.ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}
	
	
///////////////////////////////////////////////////////////
//		Switch Lane Algorithm
	/**
	 * check if lane neiLane is good to switch to and return its score
	 */
	private int switchLaneAlgorithm(LaneRealExt neiLane) {
		if(neiLane == null) return -1;
		Car neiCarBehind = neiLane.getBehindCar(this.pos+1);
		Car neiCarFront = neiLane.getFrontCar(this.pos-1);
		Car thisCarFront = this.currentLane.getFrontCar(this.pos);
		// gap - number of free cells : [c] [] [] [c] -> gap == 2
		int gapNeiFront = neiCarFront != null ? neiCarFront.getPosition() - this.pos - 1 : neiLane.linkLength() - this.pos -1;
		System.out.println("switchLaneAlgorithm front " + neiCarFront + "\n back " + neiCarBehind+"\n\t to : " + neiLane.getLane().getAbsoluteNumber() + " :: " + isMyLaneBad(thisCarFront) + isOtherLaneBetter(thisCarFront, neiCarFront, neiLane) + canSwitchLaneToOther(neiCarBehind, neiCarFront, neiLane));
		if(isMyLaneBad(thisCarFront) && isOtherLaneBetter(thisCarFront, neiCarFront, neiLane) && canSwitchLaneToOther(neiCarBehind, neiCarFront, neiLane)) {
			return gapNeiFront;	// score for this lane switch
		}
		return -1;
	}
	/** is distance to next car less than my speed  */
	private boolean isMyLaneBad(Car carInFront) {
		int gapThisFront = 	carInFront != null ? carInFront.getPosition() - this.pos - 1 : this.currentLane.linkLength() - this.pos -1;
		return gapThisFront <= this.velocity;
	}
	
	/** other lane better if it has more space to next car in front */
	private boolean isOtherLaneBetter(Car carInFront, Car otherCarFront, LaneRealExt otherLane) {
		int gapThisFront = carInFront != null	? carInFront.getPosition() - this.pos - 1	: this.currentLane.linkLength() - this.pos -1;
		int gapNeiFront = otherCarFront != null	? otherCarFront.getPosition() - this.pos - 1	: otherLane.linkLength() - this.pos -1;
		return (gapNeiFront-1) > gapThisFront;
	}
	
	/** is it safe to switch lanes, tests my speed, others speed, gaps between cars, niceness of lane switch (how much space do I need) */
	private boolean canSwitchLaneToOther(Car otherCarBehind, Car otherCarFront, LaneRealExt otherLane) {
		int gapNeiFront =	otherCarFront != null	? otherCarFront.getPosition() - this.pos - 1 	: otherLane.linkLength() - this.pos -1;
		int gapNeiBehind =	otherCarBehind != null	? this.pos - otherCarBehind.getPosition() - 1	: this.pos - 1;
		double crashFreeTurns = this.currentLane.CRASH_FREE_TIME;	// turns until crash, gap must be bigger than velocity * crashFreeTurns, == 1 -> after this turn it will look good
		double crashFreeDivider = Math.log(numOfTurnsInWantedSwitchLane);
		boolean spaceInFront = gapNeiFront >= (this.getVelocity()-1) * (crashFreeTurns / crashFreeDivider);
		boolean spaceBehind =	otherCarBehind != null	? gapNeiBehind > otherCarBehind.getFutureVelocity() * Math.max((crashFreeTurns-1)/crashFreeDivider, 0)	: true;
		return spaceInFront && spaceBehind && (otherLane.getOffset() <= this.getPosition());
	}	
//		[end] Switch Lane Algorithm
///////////////////////////////////////////////////////////
	
	/**
	 * uses part of Switch Lane Algorithm to check if car can switch lanes (based on gap, velocity)
	 * @return true if car can switch lane in given direction
	 */
	private boolean checkIfCanSwitchToDirection(LaneSwitch direction) {
		System.out.println("checkIfCanSwitchToDirection 0");
		LaneRealExt otherLane;
		if(direction == LaneSwitch.LEFT) {
			if(this.currentLane.hasLeftNeighbor())
				otherLane = this.currentLane.leftNeighbor();
			else
				return false;
		} else if(direction == LaneSwitch.RIGHT) {
			if(this.currentLane.hasRightNeighbor())
				otherLane = this.currentLane.rightNeighbor();
			else
				return false;
		} else {
			return true;	// can always switch if there is not switch
		}
		Car carBehind = otherLane.getBehindCar(this.pos+1);
		Car carFront = otherLane.getFrontCar(this.pos-1);
		return canSwitchLaneToOther(carBehind, carFront, otherLane);
	}

	/**
	 * Check if there is need to switch lanes (obstacle, emergency etc) <br>
	 * If not check switchLaneAlgorithms on all neighbors lanes <br>
	 * Action for obstacle or emergency have priority <br>
	 * TODO: poprawić zmiane pasow przed przeszkodami i przed karetkami 
	 * @return correct switch lane state
	 */
	void switchLanesState(){
		//	if car is not emergency or car behind me is emergency - go right
		// 	if obstacle in front - try to switch
		//	if car dont want to switch - check setSwitchToLaneStateForAlgorithm - maybe it will switch

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
			System.out.println("switchLanesState 1");
			// obstacle in range, must change lane, prefers right, but if cant, will try left
			System.out.println("Przeszkoda?!?! o nie!!	QQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ");
			if(checkIfCanSwitchToDirection(LaneSwitch.RIGHT)) {
				this.switchToLane = LaneSwitch.RIGHT;
			} else if(checkIfCanSwitchToDirection(LaneSwitch.LEFT)) {
				this.switchToLane = LaneSwitch.LEFT;
			} else {
				this.switchToLane = LaneSwitch.NO_CHANGE;
			}
				
		}
		else if (!isEmergency() && this.currentLane.getBehindCar(this) != null && this.currentLane.getBehindCar(this).isEmergency()) {
			System.out.println("switchLanesState 2");
			if(checkIfCanSwitchToDirection(LaneSwitch.RIGHT)) {
				this.switchToLane = LaneSwitch.RIGHT;
			} else {
				this.switchToLane = LaneSwitch.NO_CHANGE;
			} 
		} 
		// if car wanted to switch lanes in previous turn, check if its possible now
		else if(this.switchToLane == LaneSwitch.WANTS_LEFT) {
			if(checkIfCanSwitchToDirection(LaneSwitch.LEFT)) {
				this.switchToLane = LaneSwitch.LEFT;
				numOfTurnsInWantedSwitchLane = 0;
			} else {
				numOfTurnsInWantedSwitchLane++;
			}
			
		} 
		else if(this.switchToLane == LaneSwitch.WANTS_RIGHT) {
			if(checkIfCanSwitchToDirection(LaneSwitch.RIGHT)) {
				this.switchToLane = LaneSwitch.RIGHT;
				numOfTurnsInWantedSwitchLane = 0;
			} else {
				numOfTurnsInWantedSwitchLane++;
			}
		} 
		else {	
			System.out.println("switchLanesState 3");
			this.switchLaneMethodRandom = this.currentLane.getParams().getRandomGenerator().nextDouble();
			if(this.getActionForNextIntersection() != null) {
				int intersectionSwitchMultiplier =  Math.max(0, Math.abs(this.currentLane.getLane().getAbsoluteNumber() - this.getActionForNextIntersection().getSource().getAbsoluteNumber()));
				if(this.switchLaneMethodRandom * intersectionSwitchMultiplier < this.switchLaneActionProbability()) {
					this.switchLaneMethod = SwitchLaneMethod.INTERSECTION_LANE;
				} else {
					this.switchLaneMethod = SwitchLaneMethod.LOCAL_TRAFIC_ALGORITHM;
				}
			} else {
				this.switchLaneMethod = SwitchLaneMethod.LOCAL_TRAFIC_ALGORITHM;
			}
			if(this.switchLaneMethod == SwitchLaneMethod.INTERSECTION_LANE) {
				if(this.isThisLaneGoodForNextIntersection()) {
					System.out.println("setSwitchToLaneStateForAlgorithm With Intersection");
					this.setSwitchToLaneStateForAlgorithm(); // behaves differently based on switchLaneMethod
				} else {
					System.out.println("setSwitchToLaneStateForIntersection");
					this.setSwitchToLaneStateForIntersection();
				}
			} else {
				System.out.println("setSwitchToLaneStateForAlgorithm solo");
				this.setSwitchToLaneStateForAlgorithm();	// behaves differently based on switchLaneMethod
			}
		}
		
		if(!checkIfCanSwitchToDirection(this.switchToLane)) {	// only to make sure it works
			throw new RuntimeException("FALSE :: checkIfCanSwitchToDirection(this.switchToLane) " + this);		///////////////////////////////////////////////////////////////////////////////////////////////
		}
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

		// find car right after and behind current car in neighboring lane
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
	
//	[end] Lane Changes Methods
/////////////////////////////////////////////////////////////////
	
	/**
	 * Nagel-Schreckenberg <br>
	 * Perform all necessary actions for car
	 */
	void simulateTurn() {
		LOGGER.trace("car simulation : " + this);
		System.out.println("simulateTurn for " + this);
		System.out.println("action " + this.getActionForNextIntersection());
		
		if(this.isObstacle()) {	// dont simulate obstacles
			return;
		}
		if(!this.canMoveThisTurn()) {	// car already did this turn
			return;
		}
		
		Car nextCar = this.currentLane.getFrontCar(this);
		
		// remember starting point
		this.setBeforeLane(this.currentLane.getLane());
		this.setBeforePos(this.getPosition());
		
		// Acceleration
		if (this.isEmergency()) {
			this.velocity = Math.min(this.currentLane.getEmergencySpeedLimit(), this.velocity+this.currentLane.getEmergencyAcceleration());
		} else {
			this.velocity = Math.min(this.currentLane.getSpeedLimit(), this.velocity+1);
		}
		
		System.out.println("drive with " + this);
		handleCorrectModel(nextCar);
		
		
		driveCar(nextCar);
		
		fireAllInductionLoopPointers();
		
		this.updateTurnNumber();
		
	}
	
	/** fire all InductionLoopPointer for current (and previous late if lane was switched)	 */
	private void fireAllInductionLoopPointers() {
		InductionLoopPointer ilpBeforeLane = this.currentLane.getRealView().ext(this.beforeLane).new InductionLoopPointer();
		InductionLoopPointer ilpCurrentLane;
		if(this.beforeLane.equals(this.currentLane.getLane())) {
			ilpCurrentLane = null;
		} else {
			ilpCurrentLane = this.currentLane.new InductionLoopPointer();			
		}
		int lastCrossedLineForBefore;
		int lastCrossedLineForCurrent = -1;
		if(this.beforeLane.equals(this.currentLane.getLane())) {	// car didn't switch lanes this turn
			lastCrossedLineForBefore = this.getPosition();
		} else {	// we did change lanes
			if(this.beforeLane.getOwner().equals(this.currentLane.getLane().getOwner())) {	// car passed intersection
				/* last line of this link crossed by the car in this turn */
				lastCrossedLineForBefore = this.beforeLane.getLength()-1;
				lastCrossedLineForCurrent = this.getPosition();
			} else {	// Normal lane switch, on the same road
				lastCrossedLineForBefore = this.beforeLane.getLength();
				lastCrossedLineForCurrent = this.getPosition();
			}
			
		}
		LOGGER.trace("CARTURN " + this + " crossed " + lastCrossedLineForBefore);
		System.out.println("lastCrossedLineForBefore " + lastCrossedLineForBefore + " getBeforePos "+ getBeforePos() + " lastCrossedLineForCurrent " + lastCrossedLineForCurrent);
		while (!ilpBeforeLane.atEnd() && ilpBeforeLane.current().line <= lastCrossedLineForBefore) {
			if (ilpBeforeLane.current().line > this.getBeforePos()) {
				System.out.println("fire before");
				LOGGER.trace(">>>>>>> INDUCTION LOOP before " + this.getBeforePos() + " and " + lastCrossedLineForBefore + " for "	+ this.currentLane.getLane());
				ilpBeforeLane.current().handler.handleCarDrive(getVelocity(), getDriver());
			}
			ilpBeforeLane.forward();
		}
		if(lastCrossedLineForCurrent != -1) {
			LOGGER.trace("CARTURN " + this + " crossed " + lastCrossedLineForCurrent);
			while (!ilpCurrentLane.atEnd() && ilpCurrentLane.current().line <= lastCrossedLineForCurrent) {
				if (ilpCurrentLane.current().line > this.getBeforePos()) {
					System.out.println("fire current");
					LOGGER.trace(">>>>>>> INDUCTION LOOP before " + this.getBeforePos() + " and " + lastCrossedLineForCurrent + " for "	+ this.currentLane.getLane());
					ilpCurrentLane.current().handler.handleCarDrive(getVelocity(), getDriver());
				}
				ilpCurrentLane.forward();
			}
		}
	}
	
	/**	Perform action based on current car move model	
	 * changes speed and sets lane switch (if NS model)
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
			if (decisionChance < carMoveModel.getFloatParameter(CarMoveModel.MODEL_MULTINAGEL_MOVE_PROB) && velocity > 1) {
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
			throw new RuntimeException("Unknown model! " + carMoveModel.getName());
		}

	}

	/**
	 * perform lane switch if set <br>
	 * move car base on its speed and car in front
	 * @param nextCar car in front of this
	 */
	void driveCar(Car nextCar) {
		if(this.switchToLane != LaneSwitch.NO_CHANGE && this.switchToLane != LaneSwitch.WANTS_LEFT && this.switchToLane != LaneSwitch.WANTS_RIGHT) {
			this.changeLanes(this.getLaneFromLaneSwitchState());
			nextCar = this.currentLane.getFrontCar(this);	// nextCar changed
		} else if(this.switchToLane == LaneSwitch.WANTS_LEFT || this.switchToLane == LaneSwitch.WANTS_RIGHT) {
			System.out.println("numOfTurnsInWantedSwitchLane " + this.numOfTurnsInWantedSwitchLane);
			this.setVelocity(Math.max(this.getVelocity()-1, 1));	
		}
		int freeCellsInFront;
		if (nextCar != null) {
			freeCellsInFront = nextCar.getPosition() - this.pos - 1;
		} else {
			freeCellsInFront = this.currentLane.linkLength() - this.pos -1;
		}
		
		//	move car forward |velocity| squares if lane ended do intersection/gateway function
		int distanceTraveled = 0;
		int distanceTraveledOnPreviousLane = 0;	// used in intersection crossing
		if(freeCellsInFront >= this.velocity) {	// simple move forward
			distanceTraveled = this.velocity;
		} else if (nextCar != null) {	// there is car in front, will crash
			distanceTraveled = freeCellsInFront;
		} else if(this.getActionForNextIntersection() != null){	// road ended, interaction
			distanceTraveled = freeCellsInFront;
			boolean crossed = this.crossIntersection();
			System.out.println("Cross intersection " + crossed);
			if(crossed) {
				nextCar = this.currentLane.getFrontCar(this);	// nextCar changed
				if (nextCar != null) {	// distance to new car also
					freeCellsInFront = nextCar.getPosition() - this.pos - 1;
				} else {
					freeCellsInFront = this.currentLane.linkLength() - this.pos -1;
				}
				distanceTraveledOnPreviousLane = distanceTraveled;
				distanceTraveled += 
						Math.max(
								Math.min(
										Math.min(
											freeCellsInFront, this.getVelocity() - distanceTraveledOnPreviousLane - 1)
											, this.currentLane.getSpeedLimit()
										)
								,0);
			}
			// TODO: interaction crossing
		} else {	// road ended, gateway
			System.out.println("(this.hasNextTripPoint() " + this.hasNextTripPoint());
			((GatewayRealExt) this.currentLane.getRealView().ext(this.currentLane.linkEnd())).acceptCar(this);
			this.currentLane.removeCarFromLaneWithIterator(this);
			return;
		}
		this.setPosition(this.pos + distanceTraveled - distanceTraveledOnPreviousLane);
		this.setVelocity(distanceTraveled);
		
		
	}

	/**
	 * previous element to ilp.current() (if exists) should be an induction loop
	 * with line <= startPos.
	 *
	 * the same pointer can be used to the next car on this lane (above assumption
	 * will be true)
	 * @deprecated
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
	 * @deprecated
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
	 * @param otherLane new lane for this car
	 */
	public void changeLanes(LaneRealExt otherLane) {
		this.currentLane.removeCarFromLaneWithIterator(this);
		otherLane.addCarToLaneWithIterator(this);
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
	 * removes car from current lane and moves it across the intersection <br>
	 * changes this.currentLane, sets position to 0
	 * @return true if car moved across intersection
	 */
	public boolean crossIntersection() {
		LinkRealExt targetLink = this.currentLane.getRealView().ext(this.actionForNextIntersection.getTarget());
		Lane targetLaneNormal = targetLink.getLaneToEnter(this);
		if(targetLaneNormal == null) {
			return false;
		}
		LaneRealExt targetLane = this.currentLane.getRealView().ext(targetLink.getLaneToEnter(this));
		System.out.println("targetLane " + targetLane.getLane().getAbsoluteNumber());
		if(!targetLane.canAddCarToLaneOnPosition(0)) {
			return false;
		}
		this.currentLane.removeCarFromLaneWithIterator(this);
		this.setPosition(0);
		targetLane.addCarToLaneWithIterator(this);
		this.currentLane = targetLane;
		if(this.hasNextTripPoint()) {
			this.nextTripPoint();
		}
		return true;
	}

	/**
	 * Has to be called after move is simulated
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
	
	
	//////////////////////////////////////////////////////////////////
	//	TEST2013 methods
	
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

}

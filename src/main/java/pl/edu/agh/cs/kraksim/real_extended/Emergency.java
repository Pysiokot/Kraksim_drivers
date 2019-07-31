package pl.edu.agh.cs.kraksim.real_extended;

import org.apache.commons.collections15.bag.SynchronizedSortedBag;

import pl.edu.agh.cs.kraksim.KraksimConfigurator;
import pl.edu.agh.cs.kraksim.core.Lane;
import pl.edu.agh.cs.kraksim.iface.sim.Route;
import pl.edu.agh.cs.kraksim.main.drivers.Driver;

public class Emergency extends Car {
	private final String swapPenaltyMode;
	private final double swapPenaltyValue;
	
	public Emergency(Driver driver, Route route, boolean rerouting) {
		super(driver, route, rerouting);
		this.setAcceleration((int) Math.round(this.getAcceleration() * Double.parseDouble((KraksimConfigurator.getProperty("emergency_accelerationMultiplier")))));
		swapPenaltyMode = KraksimConfigurator.getProperty("emergency_swapReduceMode");
		swapPenaltyValue = Double.parseDouble(KraksimConfigurator.getProperty("emergency_swapReduceValue"));
	}
	
	/** formula for calculating probability based on current position <br>
	 *   ~(distance_traveled / lane_length) with sharp limit at the end <br>
	 *   more if car is closer to the end
	 */
	@Override
	protected double switchLaneActionProbability() {
		return 0;
	}
		
	///////////////////////////////////////////////////////////
	// Switch Lane Algorithm
	/**
	 * check if lane neiLane is good to switch to and return its score <br>
	 * only looks at obstacles
	 */
	@Override
	protected int switchLaneAlgorithm(LaneRealExt neiLane) {
		if (neiLane == null)
			return -1;
		// look only at Emergency or Obstacle
		Car neiCarBehind = neiLane.getBehindCar(this.pos + 1);
		// skip until Emergency or Obstacle, emergency is ignoring normal cars
		while(neiCarBehind!=null && !(neiCarBehind instanceof Emergency  || neiCarBehind instanceof Obstacle)) {
			neiCarBehind = neiLane.getBehindCar(neiCarBehind.getPosition());
		}
		
		Car neiCarFront = neiLane.getFrontCar(this.pos - 1);
		// skip until Emergency or Obstacle, emergency is ignoring normal cars
		while(neiCarFront!=null && !(neiCarFront instanceof Emergency  || neiCarFront instanceof Obstacle)) {
			neiCarFront = neiLane.getFrontCar(neiCarFront.getPosition());
		}
		
		Car thisCarFront = this.getCurrentLane().getFrontCar(this.pos);
		int gapNeiFront = neiCarFront != null ? neiCarFront.getPosition() - this.pos - 1
				: neiLane.linkLength() - this.pos - 1;
		if (this.isMyLaneBad(thisCarFront) && isOtherLaneBetter(thisCarFront, neiCarFront, neiLane)
				&& canSwitchLaneToOther(neiCarBehind, neiCarFront, neiLane)) {
			return gapNeiFront; // score for this lane switch
		}
		return -1;
	}

	/** other lane better if it has more space to next car in front */
	@Override
	protected boolean isOtherLaneBetter(Car carInFront, Car otherCarFront, LaneRealExt otherLane) {
		int gapThisFront = carInFront != null	? carInFront.getPosition() - this.pos - 1	: this.getCurrentLane().linkLength() - this.pos - 1;
		int gapNeiFront = otherCarFront != null	? otherCarFront.getPosition() - this.pos - 1	: otherLane.linkLength() - this.pos - 1;
		// calculate distance to nearest obstacle, must be not more than obstacleVisibility param
		boolean obstacleClose = false;
		for(Integer obstacleIndex : otherLane.getLane().getActiveBlockedCellsIndexList()) {
			int dist = obstacleIndex - getPosition();	// [C] --> [o]
			if(dist < 0) continue;
			else if(dist < Integer.parseInt(KraksimConfigurator.getProperty("obstacleVisibility"))) {
				obstacleClose = true;
				break;
			}
		}
		// better lane must be main lane and must be on the left from current
		return (gapNeiFront-1) > gapThisFront // is better
				&& this.getCurrentLane().hasLeftNeighbor() && this.getCurrentLane().leftNeighbor().equals(otherLane)	// in left lane
				&& otherLane.getLane().isMainLane()	// is main
				&& !obstacleClose;	// no obstacle on lane
	}
	
	/**
	 * uses part of Switch Lane Algorithm to check if car can switch lanes (based on gap, velocity) <br>
	 * ignores obstacles and normal cars (emergency rules)
	 * @return true if car can switch lane in given direction
	 */
	@Override
	protected boolean checkIfCanSwitchToIgnoreObstacles(LaneSwitch direction) {
		LaneRealExt otherLane;
		if(direction == LaneSwitch.LEFT) {
			if(this.getCurrentLane().hasLeftNeighbor())
				otherLane = this.getCurrentLane().leftNeighbor();
			else
				return false;
		} else if(direction == LaneSwitch.RIGHT) {
			if(this.getCurrentLane().hasRightNeighbor())
				otherLane = this.getCurrentLane().rightNeighbor();
			else
				return false;
		} else {
			return true;	// can always switch if there is not switch
		}
		Car carBehind = otherLane.getBehindCar(this.pos+1);
		// skip until Emergency or Obstacle, emergency is ignoring normal cars
		while(carBehind!=null && !(carBehind instanceof Emergency  || carBehind instanceof Obstacle)) {
			carBehind = otherLane.getBehindCar(carBehind.getPosition());
		}
		Car carFront = otherLane.getFrontCar(this.pos-1);
		// skip until Emergency or Obstacle, emergency is ignoring normal cars
		while(carFront!=null && !(carFront instanceof Emergency  || carFront instanceof Obstacle)) {
			carFront = otherLane.getFrontCar(carFront.getPosition());
		}
		if(carBehind instanceof Obstacle) carBehind = null;
		if(carFront instanceof Obstacle) carFront = null;
		return canSwitchLaneToOther(carBehind, carFront, otherLane);
	}

	// [end] Switch Lane Algorithm
	///////////////////////////////////////////////////////////
	
	/**
	 * uses part of Switch Lane Algorithm to check if car can switch lanes (based on gap, velocity) <br>
	 * ignores normal cars (emergency rules)
	 * @return true if car can switch lane in given direction
	 */
	@Override
	protected boolean checkIfCanSwitchTo(LaneSwitch direction) {
		LaneRealExt otherLane;
		if(direction == LaneSwitch.LEFT) {
			if(this.getCurrentLane().hasLeftNeighbor())
				otherLane = this.getCurrentLane().leftNeighbor();
			else
				return false;
		} else if(direction == LaneSwitch.RIGHT) {
			if(this.getCurrentLane().hasRightNeighbor())
				otherLane = this.getCurrentLane().rightNeighbor();
			else
				return false;
		} else {
			return true;	// can always switch if there is not switch
		}
		Car carBehind = otherLane.getBehindCar(this.pos+1);
		// skip until Emergency or Obstacle, emergency is ignoring normal cars
		while(carBehind!=null && !(carBehind instanceof Emergency  || carBehind instanceof Obstacle)) {
			carBehind = otherLane.getBehindCar(carBehind.getPosition());
		}
		Car carFront = otherLane.getFrontCar(this.pos-1);
		// skip until Emergency or Obstacle, emergency is ignoring normal cars
		while(carFront!=null && !(carFront instanceof Emergency  || carFront instanceof Obstacle)) {
			carFront = otherLane.getFrontCar(carFront.getPosition());
		}
		return canSwitchLaneToOther(carBehind, carFront, otherLane);
	}
	

/////////////////////////////////////////////////////////////////////////////////////////////
//	DRIVE CAR methods
/*
 * INFO: only last call of _drive_driveForward can change position and velocity at the end, rest must return -1 to prevent it
 */

	/** very local method */
	@Override
	protected void _drive_forceStopIntersection() {
		// never stop
	}

	/** very local method @return distanceTraveled */
	@Override
	protected int _drive_moveNextClose(int freeCellsInFront, Car nextCar, int distDrivenTotal) {
		int distanceTraveled = freeCellsInFront;
		Car nextNextCar = this.getCurrentLane().getFrontCar(nextCar);
		if((nextCar instanceof Obstacle || nextCar instanceof Emergency)
				|| (nextNextCar!=null && nextNextCar.getPosition() == nextCar.getPosition() 
							&&  nextNextCar instanceof Emergency
				)) {
			// nextCar might share cell with Emergency, it this case swap is not possible
			// we cant swap -> we cant move forward -> end move
			return distanceTraveled;
			
		} else {
			
			// swap -> reduce car speed using correct method from config file
			if(this.swapPenaltyMode.equals("subtract")) {
				this.setVelocity(Math.max(0, (int) (this.getVelocity() - swapPenaltyValue)));
			} else {	// divide
				this.setVelocity((int) (this.getVelocity()/swapPenaltyValue));				
			}
			nextCar.setVelocity(0);
			
			// swap with nextCar
			this.swap(nextCar);	// OMG, this this the one... the great... our savior... the mighty SWAP?!
			
			// continue travel with reduced speed, increased distDrivenTotal param
			this._drive_driveForward(distDrivenTotal + distanceTraveled + 1);	
			// distance in previous driveForward + distance in this (freeCellsInFront) + 1 for swap				
			return -1; 	// only last driveForward can change position and velocity
		}
	}
	
	/**
	 * This is it, the most awesome method in this class, performs the great swap with given car, 
	 * inserting emergency on the same position but in front when looking at simulation car list
	 * @param nextCar normal car that is being swapped
	 */
	private void swap(Car nextCar) {
		this.getCurrentLane().removeCarFromLaneWithIterator(this);
		this.setPosition(nextCar.getPosition());
		this.getCurrentLane().addCarToLaneWithIterator(this);	// inserts in correct spot
	}
	
	/**
	 * removes car from current lane and moves it across the intersection <br>
	 * changes this.currentLane, sets position to 0 <br>
	 * Emergency ignores red lights
	 * @return true if car moved across intersection
	 */
	public boolean crossIntersection() {
		LinkRealExt targetLink = this.getCurrentLane().getRealView().ext(this.getActionForNextIntersection().getTarget());
		Lane targetLaneNormal = targetLink.getLaneToEnter(this);	// sets this.actionForNextIntersection
		if(targetLaneNormal == null) {
			// only in case of emergency full traffic jam
			return false;	// no good lanes after intersection
		}
		// we are good to cross intersection 
		LaneRealExt targetLane = this.getCurrentLane().getRealView().ext(targetLaneNormal);		
		this.getCurrentLane().removeCarFromLaneWithIterator(this);
		this.setPosition(0);
		targetLane.addCarToLaneWithIterator(this);
		if(this.hasNextTripPoint()) {
			this.nextTripPoint();
		}
		targetLink.fireAllEntranceHandlers(this);
		this.getCurrentLane().getRealView().ext(this.getCurrentLane().getLane().getOwner()).fireAllExitHandlers(this);
		this.setCurrentLane(targetLane);
		return true;
	}
	
	/** Includes emergency multiplier */
	public int getSpeedLimit() {
		return (int) Math.round(this.getCurrentLane().getSpeedLimit() 
				* Double.parseDouble(KraksimConfigurator.getProperty("emergency_speedLimitMultiplier")));
	}
	
	@Override
	public String toString() {
		if(this.getCurrentLane() == null) {
			return this.getDriver() + " in [ EMERGENCY bPos=" + this.getBeforePos() + ",cPos=" + pos + ",v=" + this.getVelocity() + " lane: " + "null"+ ']';
			
		}
		return this.getDriver() + " in [ EMERGENCY bPos=" + this.getBeforePos() + ",cPos=" + pos + ",v=" + this.getVelocity() + " lane: " + this.getCurrentLane().getLane().getAbsoluteNumber() +']';
	}
}

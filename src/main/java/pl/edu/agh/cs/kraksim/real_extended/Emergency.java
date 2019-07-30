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
		while(neiCarBehind!=null && !(neiCarBehind instanceof Emergency  || neiCarBehind instanceof Obstacle)) {
			neiCarBehind = neiLane.getBehindCar(neiCarBehind.getPosition());
		}
		
		Car neiCarFront = neiLane.getFrontCar(this.pos - 1);
		while(neiCarFront!=null && !(neiCarFront instanceof Emergency  || neiCarFront instanceof Obstacle)) {
			neiCarFront = neiLane.getFrontCar(neiCarFront.getPosition());
		}
		
		Car thisCarFront = this.getCurrentLane().getFrontCar(this.pos);
		//System.out.println(this+"\n\tneiCarBehind " + neiCarBehind + "\n\tneiCarFront " + neiCarFront + "\n\tthisCarFront " + thisCarFront);
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
		return (gapNeiFront-1) > gapThisFront // is better
				&& this.getCurrentLane().hasLeftNeighbor() && this.getCurrentLane().leftNeighbor().equals(otherLane)	// in left lane
				&& otherLane.getLane().isMainLane()	// is main
				&& !obstacleClose;	// no obstacle on lane
	}
	
	/** is it safe to switch lanes, tests my speed, others speed, gaps between cars, niceness of lane switch (how much space do I need) */
	@Override
	protected boolean canSwitchLaneToOther(Car otherCarBehind, Car otherCarFront, LaneRealExt otherLane) {
		int gapNeiFront =	otherCarFront != null	? otherCarFront.getPosition() - this.pos - 1 	: otherLane.linkLength() - this.pos - 1;
		int gapNeiBehind =	otherCarBehind != null	? this.pos - otherCarBehind.getPosition() - 1	: this.pos - 1;
		double crashFreeTurns = this.getCurrentLane().CRASH_FREE_TIME;	// turns until crash, gap must be bigger than velocity * crashFreeTurns, == 1 -> after this turn it will look good
		double crashFreeMultiplier;
		if(Double.parseDouble(KraksimConfigurator.getProperty("turnsToIgnoreCrashRules")) == 0) {
			crashFreeMultiplier = 0;
		} else {
			crashFreeMultiplier = Math.max(1 - switchLaneUrgency / Double.parseDouble(KraksimConfigurator.getProperty("turnsToIgnoreCrashRules")), 0);
		}
		boolean spaceInFront = gapNeiFront >= Math.round((this.getVelocity() - this.getAcceleration()) * crashFreeTurns * crashFreeMultiplier);
		boolean spaceBehind = otherCarBehind == null || gapNeiBehind >= Math.round((otherCarBehind.getFutureVelocity()-(this.getVelocity() - this.getAcceleration())) * (crashFreeTurns - 1) * crashFreeMultiplier);
//		System.out.println("spaceInFront && spaceBehind " + spaceInFront +""+ spaceBehind);
//		System.out.println("otherCarBehind " + otherCarBehind);
//		System.out.println("otherCarFront " + otherCarFront);
		return spaceInFront && spaceBehind && (otherLane.getOffset() <= this.getPosition());
	}	
	
	/**
	 * uses part of Switch Lane Algorithm to check if car can switch lanes (based on gap, velocity)
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
		while(carBehind!=null && !(carBehind instanceof Emergency  || carBehind instanceof Obstacle)) {
			carBehind = otherLane.getBehindCar(carBehind.getPosition());
		}
		Car carFront = otherLane.getFrontCar(this.pos-1);
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
	 * uses part of Switch Lane Algorithm to check if car can switch lanes (based on gap, velocity)
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
		while(carBehind!=null && !(carBehind instanceof Emergency  || carBehind instanceof Obstacle)) {
			carBehind = otherLane.getBehindCar(carBehind.getPosition());
		}
		Car carFront = otherLane.getFrontCar(this.pos-1);
		while(carFront!=null && !(carFront instanceof Emergency  || carFront instanceof Obstacle)) {
			carFront = otherLane.getFrontCar(carFront.getPosition());
		}
		return canSwitchLaneToOther(carBehind, carFront, otherLane);
	}
	
	/**
	 * perform lane switch if set <br>
	 * move car base on its speed and car in front
	 * @param nextCar car in front of this
	 */
	void driveCar(Car nextCar) {
		//System.out.println(this.switchToLane);
		if(this.switchToLane == LaneSwitch.LEFT || this.switchToLane == LaneSwitch.RIGHT) {
			this.changeLanes(this.getLaneFromLaneSwitchState());
			nextCar = this.getCurrentLane().getFrontCar(this);	// nextCar changed
			this.setVelocity(Math.max(this.getVelocity()-1, 0));	// Reduce by 1
			this.switchLaneUrgency = 1;
			
		} else if(this.switchToLane == LaneSwitch.WANTS_LEFT || this.switchToLane == LaneSwitch.WANTS_RIGHT) {
			// cancel this.acceleration
			this.setVelocity(Math.max(this.getVelocity()-this.getAcceleration(), 1));	// by default reduce speed to 1 if looking for a lane switch	
			this.switchLaneUrgency*=2;
		}
		
		driveForward(0, 0);
		
	}
	
	private void driveForward(int distDrivenTotal, int distDrivenThisDrive) {
		Car nextCar = this.getCurrentLane().getFrontCar(this);
		//System.out.println("\tnextCar " + nextCar);
		boolean continueTravel;
		int freeCellsInFront;
		if (nextCar != null) {
			freeCellsInFront = nextCar.getPosition() - this.pos - 1;
		} else {
			freeCellsInFront = this.getCurrentLane().linkLength() - this.pos -1;
		}
		freeCellsInFront = Math.max(freeCellsInFront,  0);
		
		//	move car forward |velocity| squares if lane ended do intersection/gateway function
		int distanceTraveled = 0;
		int distanceTraveledOnPreviousLane = 0;	// used in intersection crossing
		if(freeCellsInFront >= this.getVelocity() - distDrivenTotal) {	// simple move forward
			distanceTraveled = Math.max(0, this.getVelocity() - distDrivenTotal);
			continueTravel = false;	// last move in this turn
		} else if (nextCar != null) {	// there is car in front, will crash, distance is less than velocity
			distanceTraveled = freeCellsInFront;
			continueTravel = true;
		} else if(this.getActionForNextIntersection() != null){	// road ended, interaction
			continueTravel = false;
			distanceTraveled = freeCellsInFront;
			boolean crossed = this.crossIntersection();
			if(crossed) {
				this.setVelocity(this.getVelocity() - distanceTraveled);
				this.driveForward(distDrivenTotal + distanceTraveled + 1, 0);	// distance in previous driveForward + distance in this (freeCellsInFront) + 1 for swap				
				return;
			} 
		} else {	// road ended, gateway
			continueTravel = false;
			((GatewayRealExt) this.getCurrentLane().getRealView().ext(this.getCurrentLane().linkEnd())).acceptCar(this);				
			this.getCurrentLane().removeCarFromLaneWithIterator(this);
			distanceTraveled = freeCellsInFront + 2;
		}
		
		if(continueTravel) {
			Car nextNextCar = this.getCurrentLane().getFrontCar(nextCar);
			if((nextCar instanceof Obstacle || nextCar instanceof Emergency)
					|| (nextNextCar!=null && nextNextCar.getPosition() == nextCar.getPosition() 
								&& (nextNextCar instanceof Obstacle || nextNextCar instanceof Emergency)
					)) {
				// cant swap
				continueTravel = false;	// change, we cant swap -> we cant move forward
			} else {
				if(this.swapPenaltyMode.equals("subtract")) {
					this.setVelocity(Math.max(0, (int) (this.getVelocity() - swapPenaltyValue)));
					//nextCar.setVelocity(Math.max(0, (int) (nextCar.getVelocity() - swapPenaltyValue)));
					nextCar.setVelocity(0);
				} else {	// divide
					this.setVelocity((int) (this.getVelocity()/swapPenaltyValue));				
					//nextCar.setVelocity((int) (nextCar.getVelocity()/swapPenaltyValue));	
					nextCar.setVelocity(0);
				}
				//System.out.println("\tdriveForward new Vel " + this.getVelocity());
				// swap with nextCar
				this.swap(nextCar);
				this.driveForward(distDrivenTotal + distanceTraveled + 1, 0);	
				// distance in previous driveForward + distance in this (freeCellsInFront) + 1 for swap				
				return;
			}
		} 
		if(!continueTravel) {
			//System.out.println("this.getPosition() + distanceTraveled, "+ this.getPosition() +" "+ distanceTraveled);
			this.setPosition(this.getPosition() + distanceTraveled);
			this.setVelocity(distanceTraveled + distDrivenTotal);			
		}
		//System.out.println("\tdriveForward end " + this);
	}
	
	private void swap(Car nextCar) {
//		System.out.println("SWAP ------------ " + this.getPosition());
//		for(Car cPrint : this.getCurrentLane().getCars()) {
//			System.out.print(cPrint.getPosition()+"="+(cPrint instanceof Emergency) + " "); 
//		}
		this.getCurrentLane().removeCarFromLaneWithIterator(this);
		this.setPosition(nextCar.getPosition());
		this.getCurrentLane().addCarToLaneWithIterator(this);
//		for(Car cPrint : this.getCurrentLane().getCars()) {
//			System.out.print(cPrint.getPosition()+"="+(cPrint instanceof Emergency) + " "); 
//		}
//		System.out.println("====-------- " + this.getPosition());
	}
	
	/**
	 * removes car from current lane and moves it across the intersection <br>
	 * changes this.currentLane, sets position to 0
	 * @return true if car moved across intersection
	 */
	public boolean crossIntersection() {
		LinkRealExt targetLink = this.getCurrentLane().getRealView().ext(this.getActionForNextIntersection().getTarget());
		Lane targetLaneNormal = targetLink.getLaneToEnter(this);	// sets this.actionForNextIntersection
		if(targetLaneNormal == null) {
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

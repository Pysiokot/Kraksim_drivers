package pl.edu.agh.cs.kraksim.real_extended;

import java.awt.Color;
import java.util.List;
import java.util.ListIterator;

import com.google.common.collect.Lists;

import pl.edu.agh.cs.kraksim.core.Action;
import pl.edu.agh.cs.kraksim.core.Lane;
import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.core.Node;
import pl.edu.agh.cs.kraksim.main.drivers.Driver;

public class Obstacle extends Car {

	Obstacle(int position, LaneRealExt lane) {
		super();
		this.pos = position;
		setCurrentLane(lane);
	}
	
	public Driver getDriver() {
		return null;
	}
	
	public int getVelocity() {
		return 0;
	}
	
	public void setVelocity(int velocity) {
		throw new RuntimeException("Wrong action on stationary object");
	}
	
	public String toString() {
		return "[ Obstacle Pos=" + this.getPosition() + ",v=" + this.getVelocity() + " ]";
	}
	
	public boolean isEmergency() {
		return false;
	}
	
	public boolean isObstacle() {
		return true;
	}
	
	public boolean isBraking() {
		return false;
	}
	
	public void setBraking(boolean braking, boolean emergency) {
		throw new RuntimeException("Wrong action on stationary object");
	}

	public void setPosition(int pos) {
		throw new RuntimeException("Wrong action on stationary object");
	}
	
	int getBeforePos() {
		throw new RuntimeException("Wrong action on stationary object");
	}
	
	void setBeforePos(int beforePos) {
		throw new RuntimeException("Wrong action on stationary object");
	}
	
	public boolean hasNextTripPoint() {
		throw new RuntimeException("Wrong action on stationary object");
	}
	
	public Link nextTripPoint() {
		throw new RuntimeException("Wrong action on stationary object");
	}
	
	public Link peekNextTripPoint() {
		throw new RuntimeException("Wrong action on stationary object");
	}

	public Action getAction() {
		//throw new RuntimeException("Wrong action on stationary object");
		// have to return null for finalizeTurnSimulation()
		return null;
	}

	public void setAction(Action action) {
		throw new RuntimeException("Wrong action on stationary object");
	}

	public Action getActionForNextIntersection(){
		throw new RuntimeException("Wrong action on stationary object");
	}
	
	public void setActionForNextIntersection(Action preferableAction){
		throw new RuntimeException("Wrong action on stationary object");
	}
	
	protected void TEST2013onNewAction(Action action) {
		throw new RuntimeException("Wrong action on stationary object");
	}

	public void refreshTripRoute() {
		throw new RuntimeException("Wrong action on stationary object");
	}

	public Lane getBeforeLane() {
		throw new RuntimeException("Wrong action on stationary object");
	}

	public void setBeforeLane(Lane beforeLane) {
		throw new RuntimeException("Wrong action on stationary object");
	}

    public LaneSwitch getLaneSwitch() {
    	throw new RuntimeException("Wrong action on stationary object");
    }

	public void setLaneSwitch(LaneSwitch lane){
		throw new RuntimeException("Wrong action on stationary object");
	}
}

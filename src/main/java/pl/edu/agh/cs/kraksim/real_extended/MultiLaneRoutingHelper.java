package pl.edu.agh.cs.kraksim.real_extended;

import pl.edu.agh.cs.kraksim.core.Action;
import pl.edu.agh.cs.kraksim.core.Lane;
import pl.edu.agh.cs.kraksim.core.Link;

import java.util.List;

/**
 * This class is used to help in lane-level routing
 *
 * @author Maciej Zalewski
 */
public class MultiLaneRoutingHelper {
	RealEView ev = null;

	/**
	 * Constructor
	 *
	 * @param ev object of a type RealEView, used in class methods.
	 * @author Maciej Zalewski
	 */
	MultiLaneRoutingHelper(RealEView ev) {
		this.ev = ev;
	}


	// 2019 - changed order of conditions

	/**
	 * This method chooses the best action from the given list
	 *
	 * @param actions list of actions to choose from
	 * @return action that is the best to set for the car
	 * @author Maciej Zalewski
	 */
	public Action chooseBestAction(List<Action> actions) {

		// the answer may be obvious...
		if (actions.isEmpty()) {
			return null;
		}
		if (actions.size() < 2) {
			return actions.iterator().next();
		}

		// well, it is not. Let's start the search
		Action result = null;
		int lowestCarsCount = Integer.MAX_VALUE;
		int nearestCarPosition = 0;

		// for each lane (contained by the action)...
		for (Action action : actions) {
			Lane lane = action.getSource();
			LaneRealExt laneRE = ev.ext(lane);
			// ... check it's load
			int lSize = laneRE.getAllCarsNumber();
			int lDist = laneRE.getFirstCarPosition();
			// ... and, if there is more space to the nearest car
			if (nearestCarPosition < lDist) {
				// ... set it as the best.
				result = action;
				lowestCarsCount = lSize;
				nearestCarPosition = lDist;
				continue;
			}
			// ... or there is the same distance to the nearest car, but
			// there is less car on the lane
			if ((lDist == nearestCarPosition) && lSize < lowestCarsCount) {
				// ... set it as the best.
				result = action;
				lowestCarsCount = lSize;
				nearestCarPosition = lDist;
			}
		}
		return result;
	}

	/**
	 * Given the next action, method chooses the best lane from the link given
	 * by the second parameter.
	 * WARNING: if action==null, the best lane from the main ones is picked
	 *
	 * @param action the action to be taken on current link
	 * @param link   current link to choose lanes from
	 * @param car 
	 * @return the best lane to make the given action or null if no appropriate lane can be entered
	 */
	public Lane chooseBestLaneForAction(Action action, Link link, Car car) {
		Lane result = null;
		// if action is not null, it means that some action has already been chosen
		// the best thing would be to put the car on the lane that is source of an action
		// If that lane does not start from the intersection, we shall choose main lane
		// nearest to it
		if (action != null) {
			int destinationLaneNo = action.getSource().getAbsoluteNumber();
			int destinationLaneType = action.getSource().getRelativeNumber();
			Lane lane = link.getLaneAbs(destinationLaneNo);
			LaneRealExt laneRealExt = ev.ext(lane);
			// check if chosen lane can be entered
			if(lane.getOffset() == 0 && laneRealExt.canAddCarToLaneOnPosition(0)){
				result = lane;
			}
			// try finding most appropriate lane
			else{
				// start from left lanes
				if(destinationLaneType < 0){
					destinationLaneNo = 0;
				}
				// start from right lanes
				else if(destinationLaneType > 0){
					destinationLaneNo = link.laneCount() - 1;
				}
				// check only main lanes
				else{
					destinationLaneNo = link.getLeftMainLaneNum();
				}
				while(destinationLaneNo >= 0 && destinationLaneNo < link.laneCount()){

					lane = link.getLaneAbs(destinationLaneNo);
					laneRealExt = ev.ext(lane);
					// end conditions - incorrect lanes
					if(destinationLaneType == 0 && lane.getRelativeNumber() != 0){
						break;
					}
					else if(destinationLaneType * lane.getRelativeNumber() < 0){
						break;
					}

					// check if lane can be entered
					if(car instanceof Emergency) {
						if(lane.getOffset() == 0 && laneRealExt.canAddEmergencyToLaneOnPosition(0)){
							result = lane;
							break;
						}
					} else {
						if(lane.getOffset() == 0 && laneRealExt.canAddCarToLaneOnPosition(0)){
							result = lane;
							break;
						}
					}

					// going from left to right for left and main lanes
					// going from right to left for right lanes
					if(destinationLaneType > 0){
						destinationLaneNo--;
					}
					else{
						destinationLaneNo++;
					}
				}
			}
		} else {
			// otherwise, we are heading to a gateway and have no action set. So, we shall
			// choose the least occupied lane
			int lowestCarsCount = Integer.MAX_VALUE;
			int nearestCarPosition = 0;

			// yuk... copy-paste source. Unfortunately, we're looking for lane, not
			// for an action this time. For each of main lanes...
			for (Lane lane : link.getMainLanes()) {
				LaneRealExt laneRE = ev.ext(lane);
				// ... check it's load
				int lSize = laneRE.getCars().size();
				int lDist = Integer.MAX_VALUE;
				for(Car carIter : laneRE.getCars()) {
					if(car instanceof Emergency) {
						if(carIter instanceof Emergency) {
							lDist = carIter.getPosition();
							break;
						}
					} else {
						lDist = carIter.getPosition();
						break;
					}
					
				}
				if (lSize > 0) {
				}
				// ... and, if there is more space to the nearest car
				if (nearestCarPosition < lDist) {
					// ... set it as the best.
					result = lane;
					lowestCarsCount = lSize;
					nearestCarPosition = lDist;
					continue;
				}
				// ... or there is the same distance to the nearest car (greater than 0), but
				// there is less cars on the lane
				if (lDist == nearestCarPosition && nearestCarPosition > 0 && lSize < lowestCarsCount) {
					// ... set it as the best.
					result = lane;
					lowestCarsCount = lSize;
					nearestCarPosition = lDist;
				}
			}
		}
		return result;
	}
}

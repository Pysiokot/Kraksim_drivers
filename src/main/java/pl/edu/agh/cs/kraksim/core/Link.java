package pl.edu.agh.cs.kraksim.core;

import com.google.common.base.Preconditions;
import org.apache.commons.collections15.iterators.ArrayIterator;
import pl.edu.agh.cs.kraksim.core.visitors.ElementVisitor;
import pl.edu.agh.cs.kraksim.core.visitors.VisitingException;
import pl.edu.agh.cs.kraksim.iface.mon.CarDriveHandler;
import pl.edu.agh.cs.kraksim.parser.RoadInfo;
import pl.edu.agh.cs.kraksim.real_extended.BlockedCellsInfo;
import pl.edu.agh.cs.kraksim.real_extended.Obstacle;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class Link extends Element {
	private final String id;
	/**
	 * node the link begins in
	 */
	private final Node beginning;
	/**
	 * node the link ends in
	 */
	private final Node end;
	private final String streetName;
	/**
	 * lanes owned by (belonging to) the link
	 */
	protected final Lane[] lanes;
	/**
	 * absolute number (counting from 0, starting from the left) of the main
	 * lane
	 */
	/**
	 * number of first from left main number
	 */
	// private final int mainLaneNum;
	private final int leftMainLaneNum;
	private final int rightMainLaneNum;
	/**
	 * number of main lanes
	 */
	private final int numberOfMainLanes;
	private final int linkNumber;
	private final int speedLimit;
	private final double minimalSpeed;
	private final ZoneInfo zoneInfo;
	private String direction;
	
	private Map<String, Map<Integer, List<BlockedCellsInfo>>> blockedCellsInfo; // blocked cells details in format <road_type> -> <line_num> -> <num_of_blicked_cell>
	private List<CarDriveHandler> entranceCarHandlers; // Handler fired on car entry to this lane
	private List<CarDriveHandler> exitCarHandlers; // Handler fired on car exit 
	private double weight;
	private double load;

	/**
	 * Throws IllegalArgumentException if lanes length's are not decreasing from
	 * inside to outside.
	 * <p/>
	 * See City.createLink()
	 */
	Link(Core core, RoadInfo roadInfo, int[] leftLaneLens, int mainLaneLen, int numberOfLanes, int[] rightLaneLens, Map<String, Map<Integer, List<BlockedCellsInfo>>> linkBlockedCellsInfo)
			throws IllegalArgumentException {
		super(core);
		linkNumber = core.getNextNumber();
		id = roadInfo.getLinkId();
		beginning = roadInfo.getFrom();
		end = roadInfo.getTo();
		streetName = roadInfo.getStreet();
		speedLimit = roadInfo.getSpeedLimit();
		minimalSpeed = roadInfo.getMinimalSpeed();
		numberOfMainLanes = numberOfLanes;
		zoneInfo = roadInfo.getZoneInfo();
		this.blockedCellsInfo = linkBlockedCellsInfo;

		int laneCount = leftLaneLens.length + numberOfMainLanes + rightLaneLens.length;
		lanes = new Lane[laneCount];
		leftMainLaneNum = leftLaneLens.length;
		rightMainLaneNum = leftMainLaneNum + (numberOfMainLanes - 1);
		this.entranceCarHandlers = new ArrayList<>();
		this.exitCarHandlers = new ArrayList<>();

		initializeLeftLanes(core, leftLaneLens, mainLaneLen, this.blockedCellsInfo.get("left"));
		initializeMainLane(core, mainLaneLen, this.blockedCellsInfo.get("main"));
		initializeRightLanes(core, rightLaneLens, mainLaneLen, this.blockedCellsInfo.get("right"));
	}

	private void initializeRightLanes(Core core, int[] rightLaneLens, int mainLaneLen, Map<Integer, List<BlockedCellsInfo>> map) {
		for (int i = 0; i < rightLaneLens.length; i++) {
			if (rightLaneLens[i] <= 0) {
				throw new IllegalArgumentException("length of lane must be positive");
			}
			if (rightLaneLens[i] >= (i == rightLaneLens.length - 1 ? mainLaneLen : rightLaneLens[i + 1])) {
				throw new IllegalArgumentException("an outer lane must be shorter than an inner lane");
			}
			int laneNum = rightMainLaneNum + i + 1;
			lanes[laneNum] = new Lane(core, this, laneNum, i + 1, rightLaneLens[i], speedLimit, minimalSpeed
					, (map == null || map.get(i) == null) ? new ArrayList<BlockedCellsInfo>() : map.get(i));
		}
	}
	
	private void initializeMainLane(final Core core, final int mainLaneLen, Map<Integer, List<BlockedCellsInfo>> map) {
		Preconditions.checkArgument(mainLaneLen > 0, "length of lane must be positive");

		for (int i = leftMainLaneNum; i <= rightMainLaneNum; i++) {
			lanes[i] = new Lane(core, this, i, 0, mainLaneLen, speedLimit, minimalSpeed
					, (map == null || map.get(i) == null) ? new ArrayList<BlockedCellsInfo>() : map.get(i));
		}
	}

	private void initializeLeftLanes(final Core core, final int[] leftLaneLens, final int mainLaneLen, Map<Integer, List<BlockedCellsInfo>> map) {
		for (int i = 0; i < leftLaneLens.length; i++) {
			Preconditions.checkArgument(leftLaneLens[i] > 0, "length of lane must be positive");
			Preconditions.checkArgument(leftLaneLens[i] < (i == 0 ? mainLaneLen : leftLaneLens[i - 1]), "an outer lane must be shorter than an inner lane");

			int laneNum = leftMainLaneNum - i - 1;
			lanes[laneNum] = new Lane(core, this, laneNum, -i - 1, leftLaneLens[i], speedLimit, minimalSpeed
					, (map == null || map.get(i) == null) ? new ArrayList<BlockedCellsInfo>() : map.get(i));
		}
	}

	public Color getColor() {
		return zoneInfo.getZoneColor();
	}
	
	public String getId() {
		return id;
	}

	public Node getBeginning() {
		return beginning;
	}

	public Node getEnd() {
		return end;
	}

	public String getStreetName() {
		return streetName;
	}

	public int laneCount() {
		return lanes.length;
	}

	public Iterator<Lane> laneIterator() {
		return new ArrayIterator(lanes);
	}

	public int leftLaneCount() {
		return leftMainLaneNum;
	}

	public int rightLaneCount() {
		return lanes.length - rightMainLaneNum - 1;
	}

	public int mainLaneCount() {
		return numberOfMainLanes;
	}

	public Lane getMainLane(int n) {
		Preconditions.checkElementIndex(n, numberOfMainLanes, id + ' ' + n);

		return lanes[leftMainLaneNum + n];
	}

	/* Throws IndexOutOfBoundsException */
	public Lane getLeftLane(int n) {
		Preconditions.checkElementIndex(n, leftMainLaneNum, id + ' ' + n);

		return lanes[leftMainLaneNum - n - 1];
	}

	/* Throws IndexOutOfBoundsException */
	public Lane getRightLane(int n) {
		if (n < 0 || n > lanes.length - rightMainLaneNum - 2) {
			throw new IndexOutOfBoundsException();
		}

		return lanes[rightMainLaneNum + n + 1];
	}

	/*
	 * Get lane by absolute number.
	 * 
	 * Absolute numbering start from 0, counting from the left.
	 * 
	 * Throws IndexOutOfBoundsException
	 */
	public Lane getLaneAbs(int n) {
		return lanes[n];
	}

	public int getLength() {
		return lanes[leftMainLaneNum].getLength();
	}

	public String getZoneName() {
		return zoneInfo.getZoneName();
	}

	/*
	 * Returns the action to target which source belongs to the link or null if
	 * the link goes to a gateway.
	 */
	@Deprecated
	public Action findAction(final Link target) {
		Action action = null;
		for (Lane lane : lanes) {
			action = lane.findAction(target);
			if (action != null) {
				break;
			}
		}

		return action;
	}

	/*
	 * Returns an iterator over empty sequence if the link goes to a gateway,
	 * because actionIterator() for all link's lanes returns an iterator over
	 * empty sequence
	 */
	public Iterator<Link> reachableLinkIterator() {
		return new ReachableLinkIterator();
	}

	private class ReachableLinkIterator implements Iterator<Link> {
		private int i;
		private Iterator<Action> actionIter;

		private ReachableLinkIterator() {
			i = 0;
			if (lanes.length > 0) {
				actionIter = lanes[i].actionIterator();
				preNext();
			}
		}

		private void preNext() {
			while (i < lanes.length && !actionIter.hasNext()) {
				if (++i < lanes.length) {
					actionIter = lanes[i].actionIterator();
				}
			}
		}

		public boolean hasNext() {
			return i < lanes.length;
		}

		public Link next() {
			if (i >= lanes.length) {
				throw new NoSuchElementException();
			}
			Link link = actionIter.next().getTarget();
			preNext();

			return link;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Class getExtensionClass(final Module module) {
		return module.getExtensionClasses().getLinkClass();
	}

	/* Should not be used directly. Use City.applyElementVisitor() */
	protected void applyElementVisitor(ElementVisitor visitor) throws VisitingException {
		visitor.visit(this);
		for (Lane lane : lanes) {
			lane.applyElementVisitor(visitor);
		}
	}

	/* used in exception messages */
	@Override
	public String toString() {
		return "[link " + id + " street: " + streetName + ")]";
	}

	public int getLinkNumber() {
		return linkNumber;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String dir) {
		// System.out.println(id+": dir="+ dir );
		direction = dir;
	}

	/**
	 * @return list of main lanes (from the RIGHTMOST)
	 * @author Maciej Zalewski
	 * Returns the list of main lanes in INVERTED ORDER!
	 */
	public List<Lane> getMainLanes() {
		List<Lane> result = new LinkedList<>();
		for (int i = 0; i < mainLaneCount(); i++) {
			result.add(getMainLane(i));
		}
		return result;
	}

	/**
	 * Method returns list of actions leading to the given link
	 * WARNING: actions are ordered from the RIGHTMOST lane
	 *
	 * @param target target link
	 * @return list of actions to the target link
	 * @author Maciej Zalewski
	 */
	public List<Action> findActions(Link target) {
		List<Action> result = new LinkedList<>();
		if (target == null) {
			return result;
		}
		Action action = null;
		for (int i = lanes.length - 1; i > -1; i--) {
			action = lanes[i].findAction(target);
			if (action != null) {
				result.add(action);
			}
		}
		return result;
	}

	/**
	 * @return the speedLimit
	 */
	public int getSpeedLimit() {
		return speedLimit;
	}

	public int getLeftMainLaneNum() {
		return leftMainLaneNum;
	}

	public int getRightMainLaneNum() {
		return rightMainLaneNum;
	}

	public double getWeight() {
		return weight;
	}

	public void calculateWeight(double load) {
		if(this.load-load!=0){
			//System.out.println("Load New");
		}
		this.load = load;
		
		if(weight -2 * mainLaneCount() + leftMainLaneNum + rightMainLaneNum + 0.01 * minimalSpeed + 0.01 * speedLimit + 0.1 * load!=0){
			//System.out.println(2 * mainLaneCount() + leftMainLaneNum + rightMainLaneNum + 0.01 * minimalSpeed + 0.01 * speedLimit + 0.1 * load);
		}
		weight = 2 * mainLaneCount() + leftMainLaneNum + rightMainLaneNum + 0.01 * minimalSpeed + 0.01 * speedLimit + 0.1 * load;
	}

	public double getLoad() {
		return load;
	}

	// 2019
	public Lane[] getLanes(){
		return lanes;
	}

	public List<CarDriveHandler> getEntranceCarHandlers() {
		return entranceCarHandlers;
	}

	public void setEntranceCarHandlers(List<CarDriveHandler> entranceCarHandlers) {
		this.entranceCarHandlers = entranceCarHandlers;
	}

	public List<CarDriveHandler> getExitCarHandlers() {
		return exitCarHandlers;
	}

	public void setExitCarHandlers(List<CarDriveHandler> exitCarHandlers) {
		this.exitCarHandlers = exitCarHandlers;
	}

	public List<Integer> getActiveBlockedCellsIndexList() {
		List<Integer> blockedList = new ArrayList<>();
		for(Lane lane : Arrays.asList(this.lanes)) {
			blockedList.addAll(lane.getActiveBlockedCellsIndexList());
		}
		return blockedList;
	}
}

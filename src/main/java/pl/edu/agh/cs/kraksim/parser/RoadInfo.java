package pl.edu.agh.cs.kraksim.parser;

import pl.edu.agh.cs.kraksim.core.Node;
import pl.edu.agh.cs.kraksim.core.ZoneInfo;

public class RoadInfo {
	private Node from;
	private Node to;
	private final String street;
	private final int speedLimit;
	private final double minimalSpeed;
	private final ZoneInfo zoneInfo;

	public RoadInfo(String street, Node from, Node to, int speedLimit, double minimalSpeed, ZoneInfo zoneName) {
		this.street = street;
		this.from = from;
		this.to = to;
		this.speedLimit = speedLimit;
		this.minimalSpeed = minimalSpeed;
		this.zoneInfo = zoneName;
	}

	public String getLinkId() {
		return from.getId() + to.getId();
	}
	public Node getFrom() {
		return from;
	}

	public String getStreet() {
		return street;
	}

	public Node getTo() {
		return to;
	}

	public int getSpeedLimit() {
		return speedLimit;
	}

	public double getMinimalSpeed() {
		return minimalSpeed;
	}

	public ZoneInfo getZoneInfo() {
		return zoneInfo;
	}

	//we need to swap begining and ending
	public void setReversed() {
		Node tmp = from;
		from = to;
		to = tmp;
	}
}

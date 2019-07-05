package pl.edu.agh.cs.kraksim.iface.block;

public interface LaneBlockIface {

	boolean isBlocked();

	void block();

	void unblock();

	boolean anyEmergencyCarsOnLane();

	int getEmergencyCarsOnLaneNr();

	int getClosestEmergencyCarDistance();
}

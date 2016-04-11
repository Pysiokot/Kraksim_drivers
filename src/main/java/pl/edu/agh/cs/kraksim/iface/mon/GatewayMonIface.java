package pl.edu.agh.cs.kraksim.iface.mon;

public interface GatewayMonIface {

	void installEntranceSensor(CarEntranceHandler handler);

	void installExitSensor(CarExitHandler handler);
}

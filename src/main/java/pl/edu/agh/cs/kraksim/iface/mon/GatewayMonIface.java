package pl.edu.agh.cs.kraksim.iface.mon;

import java.util.LinkedList;

import pl.edu.agh.cs.kraksim.real_extended.Car;

public interface GatewayMonIface {

	void installEntranceSensor(CarEntranceHandler handler);

	void installExitSensor(CarExitHandler handler);
	
	LinkedList<Car> getEnteringCars();
}

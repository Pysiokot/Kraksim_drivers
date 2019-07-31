package pl.edu.agh.cs.kraksim.iface.carinfo;

import pl.edu.agh.cs.kraksim.core.Lane;
import pl.edu.agh.cs.kraksim.main.drivers.Driver;

public interface CarInfoCursor {

	Lane currentLane();

	int currentPos();

//  public int currentAbsolutePos();

	int currentVelocity();

	Driver currentDriver();

	Lane beforeLane();

	int beforePos();

	boolean isValid();

	void next();
}

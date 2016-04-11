package pl.edu.agh.cs.kraksim.iface.carinfo;

import pl.edu.agh.cs.kraksim.core.Lane;

public interface CarInfoCursor {

	Lane currentLane();

	int currentPos();

//  public int currentAbsolutePos();

	int currentVelocity();

	Object currentDriver();

	Lane beforeLane();

	int beforePos();

	boolean isValid();

	void next();
}

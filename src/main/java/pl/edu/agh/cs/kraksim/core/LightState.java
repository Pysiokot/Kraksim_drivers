package pl.edu.agh.cs.kraksim.core;

public class LightState {
	private final String arm;
	private final int lane;
	private boolean green = false;

	public LightState(String arm, int lane, boolean isGreen) {
		this.arm = arm;
		this.lane = lane;
		green = isGreen;
	}

	@Override
	public String toString() {
		return "(arm=" + arm + ", lane=" + lane + ", color=" + (green ? "green" : "red") + ')';
	}

	public boolean isGreen() {
		return green;
	}
}
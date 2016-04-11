package pl.edu.agh.cs.kraksim.rlcommon;

import pl.edu.agh.cs.kraksim.core.Lane;

public final class TransitionStat {
	public static final int M = 10;

	public final Lane destLane;
	public final int destPos;
	public int count;
	//probability of transition
	public float t;
	public float deltaT;
	public float r;
	public float deltaR;

	public TransitionStat(Lane destLane, int destPos, int count) {
		this.destLane = destLane;
		this.destPos = destPos;
		this.count = count;
		t = deltaT = r = deltaR = 0;
	}

	public void updateDeltaT(int nm, int krDelta) {
		nm = nm < M ? nm : M;
		deltaT = (float) ((1.0 / ((float) nm + 1.0)) * ((float) krDelta - t));
	}

	public void updateT() {
		t += deltaT;
	}

	public void updateDeltaR(int nm, int rr) {
		nm = nm < M ? nm : M;
		deltaR = (float) ((1.0 / ((float) nm + 1.0)) * ((float) rr - r));
	}

	public void updateR() {
		r += deltaR;
	}
}
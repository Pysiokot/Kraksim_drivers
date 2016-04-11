package pl.edu.agh.cs.kraksim.traffic;

import java.util.Random;

public class NormalDistribution implements Distribution {
	private final float y;
	private final float dev;

	public NormalDistribution(float y, float dev) {
		this.y = y;
		this.dev = dev;
	}

	public float draw(Random rg) {
		return y + (float) rg.nextGaussian() * dev;
	}
}

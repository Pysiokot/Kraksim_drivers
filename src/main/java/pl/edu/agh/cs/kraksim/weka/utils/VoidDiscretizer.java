package pl.edu.agh.cs.kraksim.weka.utils;

import java.util.List;

public class VoidDiscretizer extends Discretizer {
	private double levelValue = 1;

	public VoidDiscretizer(double levelValue) {
		this.levelValue = levelValue;
	}

	@Override
	public double discretizeDurationLevel(double durationLevel) {
		return durationLevel;
	}

	@Override
	public double discretizeCarsLeavingLink(double carsLeavingLink) {
		return carsLeavingLink;
	}

	@Override
	public double discretizeCarsDensity(double carsOnLink) {
		return carsOnLink;
	}

	@Override
	public List<Double> getPossibleClassList() {
		return null;
	}

	@Override
	public boolean classBelongsToCongestionClassSet(double value) {
		return value > levelValue;
	}

	@Override
	public boolean classBelongsToHighTrafficClassSet(double value) {
		return 1.0d == value;
	}
}

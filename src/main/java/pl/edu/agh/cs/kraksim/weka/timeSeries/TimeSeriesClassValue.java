package pl.edu.agh.cs.kraksim.weka.timeSeries;

import pl.edu.agh.cs.kraksim.weka.PredictionSetup;
import pl.edu.agh.cs.kraksim.weka.data.*;
import pl.edu.agh.cs.kraksim.weka.utils.Discretizer;

import java.util.ArrayList;
import java.util.List;

public class TimeSeriesClassValue {
	private final PredictionSetup setup;

	public TimeSeriesClassValue(PredictionSetup setup) {
		this.setup = setup;
	}

	public List<Double> createAttributeValuesWithClassValue(Info info, AssociatedWorldState headState2, String classifierType) {
		List<Double> attributeValues = new ArrayList<>();
		if (info instanceof LinkInfo) {
			WorldStateRoads headState = headState2.roads;
			LinkInfo classRoad = (LinkInfo) info;
			switch (classifierType) {
				case "carsDensity": {
					double classValue = headState.getCarsDensity(classRoad.linkNumber);
					attributeValues.add(classValue);
					break;
				}
				case "carsOut": {
					double classValue = headState.getCarsOutLink(classRoad.linkNumber);
					attributeValues.add(classValue);
					break;
				}
				case "carsIn": {
					double classValue = headState.getCarsInLink(classRoad.linkNumber);
					attributeValues.add(classValue);
					break;
				}
				case "carsOn": {
					double classValue = headState.getCarsOnLink(classRoad.linkNumber);
					attributeValues.add(classValue);
					break;
				}
				case "durationLevel": {
					double classValue = headState.getDurationLevel(classRoad.linkNumber);
					attributeValues.add(classValue);
					break;
				}
				case "evaluation": {
					double classValue = headState.getEvaluation(classRoad.linkNumber);
					attributeValues.add(classValue);
					break;
				}
				case "greenDuration": {
					double classValue = headState.getGreenDuration(classRoad.linkNumber);
					attributeValues.add(classValue);
					break;
				}
			}
		} else if (info instanceof IntersectionInfo) {
			IntersectionInfo classRoad = (IntersectionInfo) info;
			WorldStateIntersections headState = headState2.intersections;
			switch (classifierType) {
				case "phase": {
					double classValue = headState.getActualPhase(classRoad.intersectionId);
					attributeValues.add(classValue);
					break;
				}
				case "phaseWillLast": {
					double classValue = headState.getPhaseWillLast(classRoad.intersectionId);
					attributeValues.add(classValue);
					break;
				}
				case "phaseLast": {
					double classValue = headState.getPhaseLast(classRoad.intersectionId);
					attributeValues.add(classValue);
					break;
				}
			}
		}
		return attributeValues;
	}

	public Double[] getClassValues(WorldStateRoads worldState) {
		Discretizer discretizer = setup.getDiscretizer();
		String classDataType = setup.getRegressionDataType();
		Double[] classes = null;
		switch (classDataType) {
			case "carsDensity":
				double[] carsDensityTable = worldState.getCarsDensityTable();
				classes = new Double[carsDensityTable.length];
				for (int i = 0; i < carsDensityTable.length; i++) {
					classes[i] = discretizer.discretizeCarsDensity(carsDensityTable[i]);
				}
				break;
			case "carsOut":
				double[] carsOutTable = worldState.getCarsOutLinkTable();
				classes = new Double[carsOutTable.length];
				for (int i = 0; i < carsOutTable.length; i++) {
					classes[i] = discretizer.discretizeCarsLeavingLink(carsOutTable[i]);
				}
				break;
			case "carsIn":
				double[] carsInTable = worldState.getCarsInLinkTable();
				classes = new Double[carsInTable.length];
				for (int i = 0; i < carsInTable.length; i++) {
					classes[i] = discretizer.discretizeCarsLeavingLink(carsInTable[i]);
				}
				break;
			case "carsOn":
				double[] carsOnTable = worldState.getCarsOnLinkTable();
				classes = new Double[carsOnTable.length];
				for (int i = 0; i < carsOnTable.length; i++) {
					classes[i] = discretizer.discretizeCarsLeavingLink(carsOnTable[i]);
				}
				break;
			case "durationLevel":
				double[] durationLevelTable = worldState.getDurationLevelTable();
				classes = new Double[durationLevelTable.length];
				for (int i = 0; i < durationLevelTable.length; i++) {
					classes[i] = discretizer.discretizeDurationLevel(durationLevelTable[i]);
				}
				break;
		}

		return classes;
	}
}

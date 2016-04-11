package pl.edu.agh.cs.kraksim.weka.timeSeries;

import pl.edu.agh.cs.kraksim.weka.PredictionSetup;
import pl.edu.agh.cs.kraksim.weka.data.*;
import pl.edu.agh.cs.kraksim.weka.utils.Discretiser;

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
			if (classifierType.equals("carsDensity")) {
				double classValue = headState.getCarsDensity(classRoad.linkNumber);
				attributeValues.add(classValue);
			} else if (classifierType.equals("carsOut")) {
				double classValue = headState.getCarsOutLink(classRoad.linkNumber);
				attributeValues.add(classValue);
			} else if (classifierType.equals("carsIn")) {
				double classValue = headState.getCarsInLink(classRoad.linkNumber);
				attributeValues.add(classValue);
			} else if (classifierType.equals("carsOn")) {
				double classValue = headState.getCarsOnLink(classRoad.linkNumber);
				attributeValues.add(classValue);
			} else if (classifierType.equals("durationLevel")) {
				double classValue = headState.getDurationLevel(classRoad.linkNumber);
				attributeValues.add(classValue);
			} else if (classifierType.equals("evaluation")) {
				double classValue = headState.getEvaluation(classRoad.linkNumber);
				attributeValues.add(classValue);
			} else if (classifierType.equals("greenDuration")) {
				double classValue = headState.getGreenDuration(classRoad.linkNumber);
				attributeValues.add(classValue);
			}
		} else if (info instanceof IntersectionInfo) {
			IntersectionInfo classRoad = (IntersectionInfo) info;
			WorldStateIntersections headState = headState2.intersections;
			if (classifierType.equals("phase")) {
				double classValue = headState.getActualPhase(classRoad.intersectionId);
				attributeValues.add(classValue);
			} else if (classifierType.equals("phaseWillLast")) {
				double classValue = headState.getPhaseWillLast(classRoad.intersectionId);
				attributeValues.add(classValue);
			} else if (classifierType.equals("phaseLast")) {
				double classValue = headState.getPhaseLast(classRoad.intersectionId);
				attributeValues.add(classValue);
			}
		}
		return attributeValues;
	}

	public Double[] getClassValues(WorldStateRoads worldState) {
		Discretiser discretiser = setup.getDiscretiser();
		String classDataType = setup.getRegressionDataType();
		Double[] classes = null;
		if (classDataType.equals("carsDensity")) {
			double[] carsDensityTable = worldState.getCarsDensityTable();
			classes = new Double[carsDensityTable.length];
			for (int i = 0; i < carsDensityTable.length; i++) {
				classes[i] = discretiser.discretiseCarsDensity(carsDensityTable[i]);
			}
		} else if (classDataType.equals("carsOut")) {
			double[] carsOutTable = worldState.getCarsOutLinkTable();
			classes = new Double[carsOutTable.length];
			for (int i = 0; i < carsOutTable.length; i++) {
				classes[i] = discretiser.discretiseCarsLeavingLink(carsOutTable[i]);
			}
		} else if (classDataType.equals("carsIn")) {
			double[] carsInTable = worldState.getCarsInLinkTable();
			classes = new Double[carsInTable.length];
			for (int i = 0; i < carsInTable.length; i++) {
				classes[i] = discretiser.discretiseCarsLeavingLink(carsInTable[i]);
			}
		} else if (classDataType.equals("carsOn")) {
			double[] carsOnTable = worldState.getCarsOnLinkTable();
			classes = new Double[carsOnTable.length];
			for (int i = 0; i < carsOnTable.length; i++) {
				classes[i] = discretiser.discretiseCarsLeavingLink(carsOnTable[i]);
			}
		} else if (classDataType.equals("durationLevel")) {
			double[] durationLevelTable = worldState.getDurationLevelTable();
			classes = new Double[durationLevelTable.length];
			for (int i = 0; i < durationLevelTable.length; i++) {
				classes[i] = discretiser.discretiseDurationLevel(durationLevelTable[i]);
			}
		}

		return classes;
	}
}

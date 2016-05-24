package pl.edu.agh.cs.kraksim.weka.utils;

import pl.edu.agh.cs.kraksim.weka.PredictionSetup;
import pl.edu.agh.cs.kraksim.weka.data.AssociatedWorldState;
import pl.edu.agh.cs.kraksim.weka.data.LinkInfo;
import pl.edu.agh.cs.kraksim.weka.data.WorldStateRoads;

import java.util.ArrayList;
import java.util.List;

public class ClassValue {
	private final PredictionSetup setup;

	public ClassValue(PredictionSetup setup) {
		this.setup = setup;
	}

	public List<Double> createAttributeValuesWithClassValue(LinkInfo classRoad, AssociatedWorldState headState) {
		List<Double> attributeValues = new ArrayList<>();
		WorldStateRoads roads = headState.roads;
		String classDataType = setup.getRegressionDataType();
		switch (classDataType) {
			case "carsDensity": {
				double classValue = roads.getCarsDensity(classRoad.linkNumber);
				classValue = setup.getDiscretizer().discretizeCarsDensity(classValue);
				attributeValues.add(classValue);
				break;
			}
			case "carsOut": {
				double classValue = roads.getCarsOutLink(classRoad.linkNumber);
				classValue = setup.getDiscretizer().discretizeCarsLeavingLink(classValue);
				attributeValues.add(classValue);
				break;
			}
			case "carsIn": {
				double classValue = roads.getCarsOutLink(classRoad.linkNumber);
				classValue = setup.getDiscretizer().discretizeCarsLeavingLink(classValue);
				attributeValues.add(classValue);
				break;
			}
			case "carsOn": {
				double classValue = roads.getCarsOutLink(classRoad.linkNumber);
				classValue = setup.getDiscretizer().discretizeCarsLeavingLink(classValue);
				attributeValues.add(classValue);
				break;
			}
			case "durationLevel": {
				double classValue = roads.getDurationLevel(classRoad.linkNumber);
				classValue = setup.getDiscretizer().discretizeDurationLevel(classValue);
				attributeValues.add(classValue);
				break;
			}
		}
		return attributeValues;
	}

	public Double[] getClassValues(AssociatedWorldState worldState) {
		WorldStateRoads roads = worldState.roads;
		Discretizer discretizer = setup.getDiscretizer();
		String classDataType = setup.getRegressionDataType();
		Double[] classes = null;
		switch (classDataType) {
			case "carsDensity":
				double[] carsDensityTable = roads.getCarsDensityTable();
				classes = new Double[carsDensityTable.length];
				for (int i = 0; i < carsDensityTable.length; i++) {
					classes[i] = discretizer.discretizeCarsDensity(carsDensityTable[i]);
				}
				break;
			case "carsOut": {
				double[] carsLeavingTable = roads.getCarsOutLinkTable();
				classes = new Double[carsLeavingTable.length];
				for (int i = 0; i < carsLeavingTable.length; i++) {
					classes[i] = discretizer.discretizeCarsLeavingLink(carsLeavingTable[i]);
				}
				break;
			}
			case "carsIn": {
				double[] carsLeavingTable = roads.getCarsOutLinkTable();
				classes = new Double[carsLeavingTable.length];
				for (int i = 0; i < carsLeavingTable.length; i++) {
					classes[i] = discretizer.discretizeCarsLeavingLink(carsLeavingTable[i]);
				}
				break;
			}
			case "carsOn": {
				double[] carsLeavingTable = roads.getCarsOutLinkTable();
				classes = new Double[carsLeavingTable.length];
				for (int i = 0; i < carsLeavingTable.length; i++) {
					classes[i] = discretizer.discretizeCarsLeavingLink(carsLeavingTable[i]);
				}
				break;
			}
			case "durationLevel":
				double[] durationLevelTable = roads.getDurationLevelTable();
				classes = new Double[durationLevelTable.length];
				for (int i = 0; i < durationLevelTable.length; i++) {
					classes[i] = discretizer.discretizeDurationLevel(durationLevelTable[i]);
				}
				break;
		}

		return classes;
	}
}

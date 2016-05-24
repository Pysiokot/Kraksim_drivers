package pl.edu.agh.cs.kraksim.weka.timeSeries;

import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.weka.PredictionSetup;
import pl.edu.agh.cs.kraksim.weka.data.*;
import pl.edu.agh.cs.kraksim.weka.utils.Neighbours;
import pl.edu.agh.cs.kraksim.weka.utils.TransactionCreator;
import weka.core.Instance;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TimeSeriesTransactionCreator extends TransactionCreator {
	private static final Logger LOGGER = Logger.getLogger(TimeSeriesTransactionCreator.class);
	private final TimeSeriesClassValue classValue;

	public TimeSeriesTransactionCreator(PredictionSetup setup) {
		super(setup);
		classValue = new TimeSeriesClassValue(setup);
	}

	public TransactionTable generateNewTransactionsForRoad(History worldStateHistory, Info classRoad, String classifierType) {
		TransactionTable transactionTable = new TransactionTable();

		History history = new History(worldStateHistory);
		List<String> attributeNames = createAttributeNames(history, classRoad, classifierType);
		transactionTable.setAttributeNames(attributeNames);

		while (history.depth() > setup.getMaxNumberOfInfluencedTimesteps()) {
			Transaction t = createTrainingTransaction(history, classRoad, classifierType);
			transactionTable.addTransaction(t);
		}

		return transactionTable;
	}

	private Transaction createTrainingTransaction(History history, Info classRoad, String classifierType) {
		AssociatedWorldState headState = history.poll();

		List<Double> attributeValues = classValue.createAttributeValuesWithClassValue(classRoad, headState, classifierType);

		int minHistoryDepth = setup.getMinNumberOfInfluencedTimesteps() - 1;
		int maxHistoryDepth = setup.getMaxNumberOfInfluencedTimesteps();
		addNoClassAttributeValues(history, classRoad, attributeValues, minHistoryDepth, maxHistoryDepth);
		return new Transaction(attributeValues);
	}

	public Transaction createTestTransaction(History historyArchive, Info classRoad) {
		List<Double> attributeValues = new ArrayList<>();
		double valueForClassAttribute = Instance.missingValue();
		attributeValues.add(valueForClassAttribute);
		int historyDepth = setup.getMaxNumberOfInfluencedTimesteps() - setup.getMinNumberOfInfluencedTimesteps() + 1;
		addNoClassAttributeValues(historyArchive, classRoad, attributeValues, 0, historyDepth);

		LOGGER.debug("Test transaction: " + attributeValues);
		return new Transaction(attributeValues);
	}

	protected void addNoClassAttributeValues(History history, Info classRoad, List<Double> attributeValues, int min, int max) {
		for (int depth = min; depth < max; depth++) {
			AssociatedWorldState worldState = history.getByDepth(depth);
			Neighbours neighbours = null;
			if (classRoad instanceof LinkInfo) {
				neighbours = neighboursArray.get(classRoad);
				LinkInfo linkInfo = (LinkInfo) classRoad;
				addRoadDataToAttributeValues(worldState, linkInfo, attributeValues);
			} else if (classRoad instanceof IntersectionInfo) {
				neighbours = intersectionsNeighbours.get(classRoad);
				IntersectionInfo intersectionInfo = (IntersectionInfo) classRoad;
				addIntersectionDataToAttributeValues(worldState, intersectionInfo.intersectionId, attributeValues);
			}

			for (LinkInfo neighbour : neighbours.roads) {
				addRoadDataToAttributeValues(worldState, neighbour, attributeValues);
			}
			for (String intersection : neighbours.intersections) {
				addIntersectionDataToAttributeValues(worldState, intersection, attributeValues);
			}
		}
	}

	private void addIntersectionDataToAttributeValues(AssociatedWorldState worldState, String intersection, List<Double> attributeValues) {
		WorldStateIntersections intersections = worldState.intersections;
		if (setup.getPhase()) {
			attributeValues.add((double) intersections.getActualPhase(intersection));
		}
		if (setup.getPhaseWillLast()) {
			attributeValues.add((double) intersections.getPhaseWillLast(intersection));
		}
		if (setup.getPhaseLast()) {
			attributeValues.add((double) intersections.getPhaseLast(intersection));
		}
	}

	private void addRoadDataToAttributeValues(AssociatedWorldState worldState, LinkInfo road, List<Double> attributeValues) {
		WorldStateRoads roads = worldState.roads;
		if (setup.getCarsDensity()) {
			attributeValues.add(roads.getCarsDensity(road.linkNumber));
		}
		if (setup.getCarsOut()) {
			attributeValues.add(roads.getCarsOutLink(road.linkNumber));
		}
		if (setup.getCarsIn()) {
			attributeValues.add(roads.getCarsInLink(road.linkNumber));
		}
		if (setup.getCarsOn()) {
			attributeValues.add(roads.getCarsOnLink(road.linkNumber));
		}
		if (setup.getDurationLevel()) {
			attributeValues.add(roads.getDurationLevel(road.linkNumber));
		}
		if (setup.getEvaluation()) {
			attributeValues.add(roads.getEvaluation(road.linkNumber));
		}
		if (setup.getGreenDuration()) {
			attributeValues.add(roads.getGreenDuration(road.linkNumber));
		}
		if (setup.getCarsDensityMovingAvg()) {
			attributeValues.add(roads.getCarsDensityMovingAvg(road.linkNumber));
		}
		if (setup.getDurationLevelMovingAvg()) {
			attributeValues.add(roads.getDurationLevelMovingAvg(road.linkNumber));
		}
	}

	private List<String> createAttributeNames(History worldStateHistory, Info classRoad, String classifierType) {
		List<String> attributeNames = new LinkedList<>();
		attributeNames.add(classRoad.getId() + '_' + classifierType);

		for (int depth = setup.getMinNumberOfInfluencedTimesteps() - 1; depth < setup.getMaxNumberOfInfluencedTimesteps(); depth++) {
			String attributeName = String.format("%s[%d][%d]", classRoad.getId(), 0, depth + 1);

			Neighbours neighbours = null;
			if (classRoad instanceof LinkInfo) {
				neighbours = neighboursArray.get(classRoad);
				if (setup.getCarsDensity()) {
					attributeNames.add(attributeName + "_carsDensity");
				}
				if (setup.getCarsOut()) {
					attributeNames.add(attributeName + "_carsOut");
				}
				if (setup.getCarsIn()) {
					attributeNames.add(attributeName + "_carsIn");
				}
				if (setup.getCarsOn()) {
					attributeNames.add(attributeName + "_carsOn");
				}
				if (setup.getDurationLevel()) {
					attributeNames.add(attributeName + "_durationLevel");
				}
				if (setup.getEvaluation()) {
					attributeNames.add(attributeName + "_evaluation");
				}
				if (setup.getGreenDuration()) {
					attributeNames.add(attributeName + "_greenDuration");
				}
				if (setup.getCarsDensityMovingAvg()) {
					attributeNames.add(attributeName + "_carsDensityMovingAvg");
				}
				if (setup.getDurationLevelMovingAvg()) {
					attributeNames.add(attributeName + "_durationLevelMovingAvg");
				}
			} else if (classRoad instanceof IntersectionInfo) {
				neighbours = intersectionsNeighbours.get(classRoad);
				if (setup.getPhase()) {
					attributeNames.add(attributeName + "_phase");
				}
				if (setup.getPhaseWillLast()) {
					attributeNames.add(attributeName + "_phaseWillLast");
				}
				if (setup.getPhaseLast()) {
					attributeNames.add(attributeName + "_phaseLast");
				}
			}
			for (LinkInfo neighbour : neighbours.roads) {
				attributeName = String.format("%s[%d][%d][%d]0", neighbour.linkId, neighbour.linkNumber, neighbour.numberOfHops, depth + 1);

				if (setup.getCarsDensity()) {
					attributeNames.add(attributeName + "_carsDensity");
				}
				if (setup.getCarsOut()) {
					attributeNames.add(attributeName + "_carsOut");
				}
				if (setup.getCarsIn()) {
					attributeNames.add(attributeName + "_carsIn");
				}
				if (setup.getCarsOn()) {
					attributeNames.add(attributeName + "_carsOn");
				}
				if (setup.getDurationLevel()) {
					attributeNames.add(attributeName + "_durationLevel");
				}
				if (setup.getEvaluation()) {
					attributeNames.add(attributeName + "_evaluation");
				}
				if (setup.getGreenDuration()) {
					attributeNames.add(attributeName + "_greenDuration");
				}
				if (setup.getCarsDensityMovingAvg()) {
					attributeNames.add(attributeName + "_carsDensityMovingAvg");
				}
				if (setup.getDurationLevelMovingAvg()) {
					attributeNames.add(attributeName + "_durationLevelMovingAvg");
				}
			}
			for (String intersection : neighbours.intersections) {
				attributeName = String.format("%s[%d]", intersection, depth + 1);
				if (setup.getPhase()) {
					attributeNames.add(attributeName + "_phase");
				}
				if (setup.getPhaseWillLast()) {
					attributeNames.add(attributeName + "_phaseWillLast");
				}
				if (setup.getPhaseLast()) {
					attributeNames.add(attributeName + "_phaseLast");
				}
			}
		}
		return attributeNames;
	}
}

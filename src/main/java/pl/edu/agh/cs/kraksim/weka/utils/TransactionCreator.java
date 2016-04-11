package pl.edu.agh.cs.kraksim.weka.utils;

import pl.edu.agh.cs.kraksim.weka.PredictionSetup;
import pl.edu.agh.cs.kraksim.weka.data.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TransactionCreator {
	protected PredictionSetup setup;
	private final ClassValue classValue;
	protected Map<LinkInfo, Neighbours> neighboursArray;
	protected Map<IntersectionInfo, Neighbours> intersectionsNeighbours;

	public TransactionCreator(PredictionSetup setup) {
		this.setup = setup;
		classValue = new ClassValue(setup);
		neighboursArray = setup.getNeighbourArray();
		intersectionsNeighbours = setup.getIntersectionNeighbours();
	}

	public TransactionTable generateNewTransactionsForRoad(History worldStateHistory, LinkInfo roadInfo) {
		Neighbours neighbours = neighboursArray.get(roadInfo);
		TransactionTable transactionTable = new TransactionTable();
		if (roadHasEnoughNeighbours(neighbours.roads)) {
			transactionTable = createTransactionTable(worldStateHistory, roadInfo);
			if (transactionTableHasNOTEnoughInterestingValues(transactionTable)) {
				transactionTable.clear();
			}
		}

		return transactionTable;
	}

	private TransactionTable createTransactionTable(History worldStateHistory, LinkInfo classRoad) {
		TransactionTable transactionTable = new TransactionTable();
		transactionTable.clear();

		History history = new History(worldStateHistory);
		List<String> attributeNames = createAttributeNames(history, classRoad);
		transactionTable.setAttributeNames(attributeNames);

		while (history.depth() > setup.getMaxNumberOfInfluencedTimesteps()) {
			Transaction t = createTraningTransaction(history, classRoad);
			transactionTable.addTransaction(t);
		}

		return transactionTable;
	}

	private Transaction createTraningTransaction(History history, LinkInfo classRoad) {
		AssociatedWorldState headState = history.poll();

		List<Double> attributeValues = classValue.createAttributeValuesWithClassValue(classRoad, headState);

		int minHistoryDepth = setup.getMinNumberOfInfluencedTimesteps() - 1;
		int maxHistoryDepth = setup.getMaxNumberOfInfluencedTimesteps();
		addNoClassAttributeValues(history, classRoad, attributeValues, minHistoryDepth, maxHistoryDepth);
		return new Transaction(attributeValues);
	}

	protected void addNoClassAttributeValues(History history, LinkInfo classRoad, List<Double> attributeValues, int min, int max) {
		for (int depth = min; depth < max; depth++) {
			AssociatedWorldState worldState = history.getByDepth(depth);
			addRoadDataToAttributeValues(worldState, classRoad, attributeValues);
			for (LinkInfo neighbour : neighboursArray.get(classRoad).roads) {
				addRoadDataToAttributeValues(worldState, neighbour, attributeValues);
			}
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

	private List<String> createAttributeNames(History worldStateHistory, LinkInfo classRoad) {
		ArrayList<String> attributeNames = new ArrayList<>();
		attributeNames.add(classRoad.linkId);

		for (int depth = setup.getMinNumberOfInfluencedTimesteps() - 1; depth < setup.getMaxNumberOfInfluencedTimesteps(); depth++) {
			String attributeName = String.format("%s[%d][%d][%d]", classRoad.linkId, classRoad.linkNumber, 0, depth + 1);

			if (setup.getCarsDensity()) {
				attributeNames.add(attributeName + "_carsDensity");
			}
			if (setup.getCarsOut()) {
				attributeNames.add(attributeName + "_carsLeaving");
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
			for (LinkInfo neighbour : neighboursArray.get(classRoad).roads) {
				attributeName = String.format("%s[%d][%d][%d]", neighbour.linkId, neighbour.linkNumber, neighbour.numberOfHops, depth + 1);

				if (setup.getCarsDensity()) {
					attributeNames.add(attributeName + "_carsDensity");
				}
				if (setup.getCarsOut()) {
					attributeNames.add(attributeName + "_carsLeaving");
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
		}
		return attributeNames;
	}

	private static boolean roadHasEnoughNeighbours(Set<LinkInfo> neighbours) {
		return neighbours != null && !neighbours.isEmpty();
	}

	private boolean transactionTableHasNOTEnoughInterestingValues(TransactionTable transactionTable) {
		int congestionPeriods = 0;
		int allPeriods = 0;
		for (Transaction transaction : transactionTable) {
			double classAttributeValue = transaction.getTransacation().get(0);
			Discretiser discretiser = setup.getDiscretiser();
			if (discretiser.classBelongsToCongestionClassSet(classAttributeValue)) {
				congestionPeriods++;
			}
			allPeriods++;
		}
		return allPeriods / 10 > congestionPeriods || allPeriods / 10 > allPeriods - congestionPeriods;
	}
}

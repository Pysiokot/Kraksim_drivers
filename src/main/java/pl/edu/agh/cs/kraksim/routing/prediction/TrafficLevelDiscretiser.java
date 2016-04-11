package pl.edu.agh.cs.kraksim.routing.prediction;

import com.google.common.collect.Lists;

import java.util.List;

public class TrafficLevelDiscretiser {
	private final List<TrafficLevel> levels;
	private double[] defaultsForColumns;

	private static final double DEFAULT_LOW_U_LIMIT = 1.20;
	private static final double DEFAULT_MED_U_LIMIT = 1.60;

	/**
	 *
	 */
	public TrafficLevelDiscretiser() {
		levels = Lists.newArrayList();
	}

	/**
	 * @param value value to be discretised
	 * @return discrete value - in percents
	 * @throws TrafficPredictionException if the value does not lay in any of discretisation levels
	 */
	private TrafficLevel getLevelForValue(double value) throws TrafficPredictionException {
		for (TrafficLevel level : levels) {
			if ((value >= level.getLowerLevel()) && (value < level.getUpperLevel())) {
				return level;
			}
		}
		throw new TrafficPredictionException("Discretisation definition is incomplete: no value for " + value);
	}

	public void addTrafficLevel(TrafficLevel newLevel) throws TrafficPredictionException {
		if (newLevel == null) {
			throw new TrafficPredictionException("Unable to set null level");
		}
		// testing if there is a level by the given name
		TrafficLevel temp = null;
		try {
			temp = getLevelByName(newLevel.getDescription());
		} catch (TrafficPredictionException e) {
			// if an exception is caught then there is no such named level 
		}
		if (temp != null) {
			// if there is one - exception
			throw new TrafficPredictionException("Level by the name " + newLevel.getDescription() + " already exists");
		}
		// checking if no ranges are covering
		double loA = newLevel.getLowerLevel();
		double hiA = newLevel.getUpperLevel();
		for (TrafficLevel myLevel : levels) {
			double loB = myLevel.getLowerLevel();
			double hiB = myLevel.getUpperLevel();
			if ((loA >= loB) && (loA < hiB)) {
				throw new TrafficPredictionException("Levels " + newLevel + " is in conflict with " + myLevel);
			}
			if ((loB >= loA) && (loB < hiA)) {
				throw new TrafficPredictionException("Levels " + newLevel + " is in conflict with " + myLevel);
			}
		}
		levels.add(newLevel);
	}

	/**
	 * This method fills the list of levels with defaults
	 */
	public void populateTrafficLevels() {
		TrafficLevel lowLevel = new TrafficLevel(Double.MIN_VALUE, DEFAULT_LOW_U_LIMIT);
		TrafficLevel mediumLevel = new TrafficLevel(DEFAULT_LOW_U_LIMIT, DEFAULT_MED_U_LIMIT);
		TrafficLevel highLevel = new TrafficLevel(DEFAULT_MED_U_LIMIT, Double.MAX_VALUE);

		lowLevel.setPredecessor(null);
		lowLevel.setProceeder(mediumLevel);

		mediumLevel.setPredecessor(lowLevel);
		mediumLevel.setProceeder(highLevel);

		highLevel.setPredecessor(mediumLevel);
		highLevel.setProceeder(null);

		lowLevel.setDescription("Empty");
		mediumLevel.setDescription("Occupied");
		highLevel.setDescription("Stuck");

		lowLevel.setMaxInfluence(-0.5);
		mediumLevel.setMaxInfluence(+0.1);
		highLevel.setMaxInfluence(+1.5);

		try {
			addTrafficLevel(lowLevel);
			addTrafficLevel(mediumLevel);
			addTrafficLevel(highLevel);
		} catch (TrafficPredictionException e) {
			e.printStackTrace();
		}
			/*
			levels.add(lowLevel);
			levels.add(mediumLevel);
			levels.add(highLevel);
			*/
	}

	public int getNumberOfLevels() {
		return levels.size();
	}

	public TrafficLevel getLevel(int levelNumber) {
		return levels.get(levelNumber);
	}

	/**
	 * Discretises the value for the given column (relates it to the
	 * default value for this column
	 *
	 * @param column number of column the value lies in (link number)
	 * @param value  the value to bi discretised
	 * @return traffic level representing that value
	 * @throws TrafficPredictionException if value does not lie within any valid range
	 */
	public TrafficLevel getLevelForValueInColumn(int column, double value) throws TrafficPredictionException {
		return getLevelForValue(value / defaultsForColumns[column]);
	}

	/**
	 * @param defaultsForColumns the defaultsForColumns to set
	 */
	public void setDefaultsForColumns(double[] defaultsForColumns) {
		this.defaultsForColumns = defaultsForColumns;
	}

	public TrafficLevel getLevelByName(String name) throws TrafficPredictionException {
		for (TrafficLevel temp : levels) {
			if (temp.getDescription().equals(name)) {
				return temp;
			}
		}
		throw new TrafficPredictionException("Discretisation definition is incomplete: no level of name " + name);
	}
}

package pl.edu.agh.cs.kraksim.routing.prediction;

public class TrafficPredictionContainerPath {
	private int linkNumber;
	private TrafficLevel level;
	private int timestepNumber;
	private int destinationLinkNumber;

	/**
	 * @return the linkNumber
	 */
	public int getLinkNumber() {
		return linkNumber;
	}

	/**
	 * @param linkNumber the linkNumber to set
	 */
	public void setLinkNumber(int linkNumber) {
		this.linkNumber = linkNumber;
	}

	/**
	 * @return the level
	 */
	public TrafficLevel getLevel() {
		return level;
	}

	/**
	 * @param level the level to set
	 */
	public void setLevel(TrafficLevel level) {
		this.level = level;
	}

	/**
	 * @return the timestepsNumber
	 */
	public int getTimestepNumber() {
		return timestepNumber;
	}

	/**
	 * @param timestepsNumber the timestepsNumber to set
	 */
	public void setTimestepNumber(int timestepsNumber) {
		timestepNumber = timestepsNumber;
	}

	/**
	 * @return the destinationLinkNumber
	 */
	public int getDestinationLinkNumber() {
		return destinationLinkNumber;
	}

	/**
	 * @param destinationLinkNumber the destinationLinkNumber to set
	 */
	public void setDestinationLinkNumber(int destinationLinkNumber) {
		this.destinationLinkNumber = destinationLinkNumber;
	}

	@Override
	public String toString() {
		return linkNumber + "->" + level + "->" + destinationLinkNumber + "->" + timestepNumber;
	}

	@Override
	public TrafficPredictionContainerPath clone() {
		TrafficPredictionContainerPath result = new TrafficPredictionContainerPath();
		result.destinationLinkNumber = destinationLinkNumber;
		result.level = level;
		result.linkNumber = linkNumber;
		result.timestepNumber = timestepNumber;
		return result;
	}
}

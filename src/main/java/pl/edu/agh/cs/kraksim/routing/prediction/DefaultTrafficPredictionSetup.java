package pl.edu.agh.cs.kraksim.routing.prediction;

import pl.edu.agh.cs.kraksim.core.City;

public class DefaultTrafficPredictionSetup implements ITrafficPredictionSetup {
	private int numberOfInfluencedTimesteps;
	private City city;
	private int numberOfInfluencedLinks;
	private TrafficLevelDiscretizer discretiser;
	private double cutOutProbability;
	private int cutOutMinimumCounter;
	private double ageingRate;

	/**
	 * @return the numberOfInfluencedTimesteps
	 */
	public int getNumberOfInfluencedTimesteps() {
		return numberOfInfluencedTimesteps;
	}

	/**
	 * @param numberOfInfluencedTimesteps the numberOfInfluencedTimesteps to set
	 */
	public ITrafficPredictionSetup setNumberOfInfluencedTimesteps(int numberOfInfluencedTimesteps) {
		this.numberOfInfluencedTimesteps = numberOfInfluencedTimesteps;
		return this;
	}

	/**
	 * @return the city
	 */
	public City getCity() {
		return city;
	}

	/**
	 * @param city the city to set
	 */
	public ITrafficPredictionSetup setCity(City city) {
		this.city = city;
		return this;
	}

	/**
	 * @return the numberOfInfluencedLinks
	 */
	public int getNumberOfInfluencedLinks() {
		return numberOfInfluencedLinks;
	}

	/**
	 * @param numberOfInfluencedLinks the numberOfInfluencedLinks to set
	 */
	public ITrafficPredictionSetup setNumberOfInfluencedLinks(int numberOfInfluencedLinks) {
		this.numberOfInfluencedLinks = numberOfInfluencedLinks;
		return this;
	}

	/**
	 * @return the discretizer
	 */
	public TrafficLevelDiscretizer getDiscretizer() {
		return discretiser;
	}

	/**
	 * @param discretizer the discretizer to set
	 */
	public ITrafficPredictionSetup setDiscretizer(TrafficLevelDiscretizer discretizer) {
		this.discretiser = discretizer;
		return this;
	}

	/**
	 * @return the cutOutProbability
	 */
	public double getCutOutProbability() {
		return cutOutProbability;
	}

	/**
	 * @param cutOutProbability the cutOutProbability to set
	 */
	public ITrafficPredictionSetup setCutOutProbability(double cutOutProbability) {
		this.cutOutProbability = cutOutProbability;
		return this;
	}

	/**
	 * @return the cutOutMinimumCounter
	 */
	public int getCutOutMinimumCounter() {
		return cutOutMinimumCounter;
	}

	/**
	 * @param cutOutMinimumCounter the cutOutMinimumCounter to set
	 */
	public ITrafficPredictionSetup setCutOutMinimumCounter(int cutOutMinimumCounter) {
		this.cutOutMinimumCounter = cutOutMinimumCounter;
		return this;
	}

	@Override
	public ITrafficPredictionSetup setAgeingRate(double ageingRate) {
		this.ageingRate = ageingRate;
		return this;
	}

	@Override
	public double getAgeingRate() {
		return ageingRate;
	}
}

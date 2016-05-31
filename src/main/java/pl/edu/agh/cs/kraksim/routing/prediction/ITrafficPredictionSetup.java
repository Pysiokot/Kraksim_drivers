package pl.edu.agh.cs.kraksim.routing.prediction;

import pl.edu.agh.cs.kraksim.core.City;

import java.util.Iterator;

public interface ITrafficPredictionSetup {
	int getNumberOfInfluencedTimesteps();

	ITrafficPredictionSetup setNumberOfInfluencedTimesteps(int numberOfInfluencedTimesteps);

	City getCity();

	ITrafficPredictionSetup setCity(City city);

	int getNumberOfInfluencedLinks();

	ITrafficPredictionSetup setNumberOfInfluencedLinks(int numberOfInfluencedLinks);

	TrafficLevelDiscretizer getDiscretizer();

	ITrafficPredictionSetup setDiscretizer(TrafficLevelDiscretizer discretizer);

	double getCutOutProbability();

	ITrafficPredictionSetup setCutOutProbability(double cutOutProbability);

	int getCutOutMinimumCounter();

	ITrafficPredictionSetup setCutOutMinimumCounter(int cutOutMinimumCounter);

	ITrafficPredictionSetup setAgeingRate(double ageingRate);

	double getAgeingRate();
}

package pl.edu.agh.cs.kraksim.routing.prediction;

import pl.edu.agh.cs.kraksim.core.City;

public interface ITrafficPredictionSetup {
	int getNumberOfInfluencedTimesteps();

	void setNumberOfInfluencedTimesteps(int numberOfInfluencedTimesteps);

	City getCity();

	void setCity(City city);

	int getNumberOfInfluencedLinks();

	void setNumberOfInfluencedLinks(int numberOfInfluencedLinks);

	TrafficLevelDiscretizer getDiscretizer();

	void setDiscretizer(TrafficLevelDiscretizer discretizer);

	double getCutOutProbability();

	void setCutOutProbability(double cutOutProbability);

	int getCutOutMinimumCounter();

	void setCutOutMinimumCounter(int cutOutMinimumCounter);

	void setAgeingRate(double ageingRate);

	double getAgeingRate();
}

package pl.edu.agh.cs.kraksim.routing.prediction;

import pl.edu.agh.cs.kraksim.core.City;

public class TrafficPredictionFactory {
	private static ITrafficPredictionSetup setup = null;
	private static boolean isCitySet = false;
	private static boolean arePropertiesSet = false;

	public static ITrafficPredictor getTrafficPredictor() {
		return DefaultTrafficPredictor.getInstance();
	}

	public static void setCityForPredictionSetup(City city) {
		if (isCitySet) {
			return;
		}
		if (setup == null) {
			setup = new DefaultTrafficPredictionSetup();
		}
		setup.setCity(city);

		if (arePropertiesSet) {
			getTrafficPredictor().setup(setup);
		}
		isCitySet = true;
	}

	public static void setPropertiesForPredictionSetup(ITrafficPredictionSetup newSetup) {
		if (setup == null) {
			setup = new DefaultTrafficPredictionSetup();
		}

		setup.setCutOutProbability(newSetup.getCutOutProbability());
		setup.setCutOutMinimumCounter(newSetup.getCutOutMinimumCounter());
		setup.setDiscretiser(newSetup.getDiscretiser());
		setup.setNumberOfInfluencedLinks(newSetup.getNumberOfInfluencedLinks());
		setup.setNumberOfInfluencedTimesteps(newSetup.getNumberOfInfluencedTimesteps());
		setup.setAgeingRate(newSetup.getAgeingRate());

		if (isCitySet) {
			getTrafficPredictor().setup(setup);
		}
		arePropertiesSet = true;
	}
}

package pl.edu.agh.cs.kraksim.weka;

import pl.edu.agh.cs.kraksim.KraksimConfigurator;
import pl.edu.agh.cs.kraksim.core.City;
import pl.edu.agh.cs.kraksim.weka.data.IntersectionInfo;
import pl.edu.agh.cs.kraksim.weka.data.LinkInfo;
import pl.edu.agh.cs.kraksim.weka.timeSeries.TimeSeriesPredictor;
import pl.edu.agh.cs.kraksim.weka.timeSeries.algorithms.*;
import pl.edu.agh.cs.kraksim.weka.utils.*;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class PredictionSetup {
	private final Map<LinkInfo, Neighbours> neighboursArray;
	private final Map<IntersectionInfo, Neighbours> intersectionNeighbours;

	private WekaPredictor prediction;

	private long worldStateUpdatePeriod;
	private long timeSeriesUpdatePeriod;
	private long statisticsDumpTime;
	private String outputMainFolder;

	private int maxNumberOfInfluencedTimesteps;
	private int minNumberOfInfluencedTimesteps;
	private int maxNumberOfInfluencedLinks;

	private Discretiser discretiser;
	private IClassifierCreator classifierCreator;

	private String trafficFileName;
	private AbstractMovingAverage movingAverage;
	private String timeSeriesFolder;
	private boolean pca;

	private int predictionSize;
	private String regressionDataType;
	private Boolean carsDensity;
	private boolean carsOut;
	private boolean carsOn;
	private boolean carsIn;
	private boolean durationLevel;
	private boolean evaluation;
	private boolean greenDuration;
	private boolean carsDensityMovingAvg;
	private boolean durationLevelMovingAvg;
	private boolean phase;
	private Boolean phaseWillLast;
	private Boolean phaseLast;
	private double timeTableMultiplier;
	private double evaluationMultiplier;
	private boolean writeDataSetToFile;

	public PredictionSetup(City city) {
		readSimpleProperties();
		neighboursArray = NeighbourArrayCreator.createNeighbourArray(city, maxNumberOfInfluencedLinks);
		NeighbourArrayCreator.addAdjacentIntersectionRoads(neighboursArray, city);
		intersectionNeighbours = NeighbourArrayCreator.createIntersectionsArray(city);
		createMainClasses();
	}

	public PredictionSetup(Map<LinkInfo, Neighbours> neighboursArray, Map<IntersectionInfo, Neighbours> intersectionNeighbours) {
		readSimpleProperties();
		this.neighboursArray = neighboursArray;
		this.intersectionNeighbours = intersectionNeighbours;
		createMainClasses();
	}

	private void readSimpleProperties() {
		Properties properties = new Properties();

		try {
			String path = getConfigPath(); //HERE
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path));
			properties.load(bis);
			bis.close();

			worldStateUpdatePeriod = Long.valueOf(properties.getProperty("worldStateUpdatePeriod"));
			timeSeriesUpdatePeriod = Long.valueOf(properties.getProperty("timeSeriesUpdatePeriod"));
			statisticsDumpTime = Long.valueOf(properties.getProperty("statisticsDumpTime"));
			maxNumberOfInfluencedLinks = Integer.valueOf(properties.getProperty("maxNumberOfInfluencedLinks"));
			maxNumberOfInfluencedTimesteps = Integer.valueOf(properties.getProperty("maxNumberOfInfluencedTimesteps"));
			minNumberOfInfluencedTimesteps = Integer.valueOf(properties.getProperty("minNumberOfInfluencedTimesteps"));

			outputMainFolder = properties.getProperty("outputMainFolder");
			writeDataSetToFile = Boolean.valueOf(properties.getProperty("writeDataSetToFile"));

			regressionDataType = properties.getProperty("regressionDataType");
			carsDensity = Boolean.valueOf(properties.getProperty("carsDensity"));
			carsOut = Boolean.valueOf(properties.getProperty("carsOut"));
			carsIn = Boolean.valueOf(properties.getProperty("carsIn"));
			carsOn = Boolean.valueOf(properties.getProperty("carsOn"));
			durationLevel = Boolean.valueOf(properties.getProperty("durationLevel"));
			evaluation = Boolean.valueOf(properties.getProperty("evaluation"));
			greenDuration = Boolean.valueOf(properties.getProperty("greenDuration"));
			carsDensityMovingAvg = Boolean.valueOf(properties.getProperty("carsDensityMovingAvg"));
			durationLevelMovingAvg = Boolean.valueOf(properties.getProperty("durationLevelMovingAvg"));

			phase = Boolean.valueOf(properties.getProperty("phase"));
			phaseWillLast = Boolean.valueOf(properties.getProperty("phaseWillLast"));
			phaseLast = Boolean.valueOf(properties.getProperty("phaseLast"));

			timeSeriesFolder = properties.getProperty("timeSeriesFolder");
			pca = Boolean.valueOf(properties.getProperty("pca"));
			predictionSize = Integer.valueOf(properties.getProperty("predictionSize"));

			timeTableMultiplier = Double.valueOf(properties.getProperty("timeTableMultiplier"));
			evaluationMultiplier = Double.valueOf(properties.getProperty("evaluationMultiplier"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createMainClasses() {
		Properties properties = new Properties();

		try {
			String path = getConfigPath();
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path));
			properties.load(bis);
			bis.close();

			createMovingAverage(properties);
			createDiscretiser(properties);
			createPrediction(properties);
			createClassifier(properties);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getConfigPath() {
		return KraksimConfigurator.getPropertiesFromFile().getProperty("predictionConfiguration");
	}

	private void createMovingAverage(Properties properties) {
		String averageType = properties.getProperty("average");
		switch (averageType) {
			case "simple":
				int averageSize = Integer.valueOf(properties.getProperty("averageSize"));
				movingAverage = new SimpleMovingAverage(averageSize);
				break;
			case "running":
				int averageWeight = Integer.valueOf(properties.getProperty("averageWeight"));
				movingAverage = new RunningMovingAverage(averageWeight);
				break;
			default:
				movingAverage = new VoidMovingAverage();
				break;
		}
	}

	private void createPrediction(Properties properties) {
		String predictionSubmodule = properties.getProperty("submodule");
		if (predictionSubmodule.equals("timeSeries")) {
			prediction = new TimeSeriesPredictor(this);
		}
	}

	private void createClassifier(Properties properties) {
		String reasonerType = properties.getProperty("regressionAlgorithm");
		switch (reasonerType) {
			case "kstar":
				classifierCreator = new KStarCreator();
				break;
			case "m5rules":
				classifierCreator = new M5RulesCreator();
				break;
			case "smoreg":
				classifierCreator = new SMOregCreator();
				break;
			case "repTree":
				classifierCreator = new RepTreeCreator();
				break;
			case "ibk":
				Integer ibkNeighbours = Integer.valueOf(properties.getProperty("ibkNeighbours"));
				classifierCreator = new IbkCreator(ibkNeighbours);
				break;
			case "m5p":
				classifierCreator = new M5PCreator();
				break;
		}
	}

	private void createDiscretiser(Properties properties) {
		String voidLevelValue = properties.getProperty("congestionValue");
		discretiser = new VoidDiscretiser(Double.valueOf(voidLevelValue));
	}

	public long getWorldStateUpdatePeriod() {
		return worldStateUpdatePeriod;
	}

	public long getTimeSeriesUpdatePeriod() {
		return timeSeriesUpdatePeriod;
	}

	public long getStatisticsDumpTime() {
		return statisticsDumpTime;
	}

	public int getMaxNumberOfInfluencedTimesteps() {
		return maxNumberOfInfluencedTimesteps;
	}

	public int getMinNumberOfInfluencedTimesteps() {
		return minNumberOfInfluencedTimesteps;
	}

	public int getMaxNumberOfInfluencedLinks() {
		return maxNumberOfInfluencedLinks;
	}

	public Discretiser getDiscretiser() {
		return discretiser;
	}

	public String getOutputMainFolder() {
		return outputMainFolder;
	}

	public WekaPredictor getPredictor() {
		return prediction;
	}

	public String getTrafficFileName() {
		return trafficFileName;
	}

	public Map<LinkInfo, Neighbours> getNeighbourArray() {
		return neighboursArray;
	}

	public Map<IntersectionInfo, Neighbours> getIntersectionNeighbours() {
		return intersectionNeighbours;
	}

	public AbstractMovingAverage getMovingAverage() {
		return movingAverage;
	}

	public IClassifierCreator getClassifierCreator() {
		return classifierCreator;
	}

	public String getTimeSeriesFolder() {
		return timeSeriesFolder;
	}

	public boolean getPCA() {
		return pca;
	}

	public String getRegressionDataType() {
		return regressionDataType;
	}

	public Boolean getCarsDensity() {
		return carsDensity;
	}

	public boolean getCarsOut() {
		return carsOut;
	}

	public boolean getDurationLevel() {
		return durationLevel;
	}

	public boolean getEvaluation() {
		return evaluation;
	}

	public boolean getGreenDuration() {
		return greenDuration;
	}

	public boolean getCarsDensityMovingAvg() {
		return carsDensityMovingAvg;
	}

	public boolean getDurationLevelMovingAvg() {
		return durationLevelMovingAvg;
	}

	public int getPredictionSize() {
		return predictionSize;
	}

	public boolean getCarsIn() {
		return carsIn;
	}

	public boolean getCarsOn() {
		return carsOn;
	}

	public boolean getPhase() {
		return phase;
	}

	public Boolean getPhaseWillLast() {
		return phaseWillLast;
	}

	public Boolean getPhaseLast() {
		return phaseLast;
	}

	public double getTimeTableMultiplier() {
		return timeTableMultiplier;
	}

	public double getEvaluationMultiplier() {
		return evaluationMultiplier;
	}

	public boolean getWriteDataSetToFile() {
		return writeDataSetToFile;
	}
}

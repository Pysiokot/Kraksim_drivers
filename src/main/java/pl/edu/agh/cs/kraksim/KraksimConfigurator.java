package pl.edu.agh.cs.kraksim;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import pl.edu.agh.cs.kraksim.parser.PredictionConfigurationXmlHandler;
import pl.edu.agh.cs.kraksim.routing.prediction.DefaultTrafficPredictionSetup;
import pl.edu.agh.cs.kraksim.routing.prediction.ITrafficPredictionSetup;
import pl.edu.agh.cs.kraksim.routing.prediction.TrafficLevelDiscretizer;
import pl.edu.agh.cs.kraksim.routing.prediction.TrafficPredictionFactory;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class KraksimConfigurator {
	private static final Logger LOGGER = Logger.getLogger(KraksimConfigurator.class);
	private static String CONFIG_PATH = "configuration/kraksim.properties";

    public static void setConfigPath(String configPath){
        CONFIG_PATH = configPath;
    }

	public static Properties getPropertiesFromFile() {
		String configPath = CONFIG_PATH;

		File f = new File(configPath);
		if (!f.canRead()) {
			LOGGER.error("The config file " + f.getAbsolutePath() + " cannot be read");
			System.exit(-1);
		}

		Properties result = new Properties();

		try {
			InputStream inStream = new FileInputStream(f);
			result.load(inStream);
		} catch (FileNotFoundException e) {
			LOGGER.error("No file found: " + f.getAbsolutePath());
			System.exit(-1);
		} catch (IOException e) {
			LOGGER.error("Invalid file format: File " + f.getAbsolutePath());
			System.exit(-1);
		}

		return result;
	}

	public static String[] prepareInputParametersForSimulation(Properties params) {
		List<String> paramsList = new ArrayList<>();

		String visualization = params.getProperty("visualization");
		if (visualization.equals("true")) {
			paramsList.add("-v");
		} else {
			paramsList.add("-g");
		}

		if ("enabled".equals(params.getProperty("zone_awareness"))) {
			paramsList.add("-z");
		}

		String carMoveModel;
		paramsList.add("-X");
		if ((carMoveModel = params.getProperty("carMoveModel")) != null) {
			paramsList.add(carMoveModel);
		} else {
			paramsList.add("nagle");
		}

        // select phys module
        String realModel = params.getProperty("realModule");
        if (realModel != null){
            paramsList.add("-Q");
            paramsList.add(realModel);
        }  // ignore other module settings

		String switchTime = params.getProperty("switchTime");
		if (switchTime != null){
			paramsList.add("-Z");
			paramsList.add(switchTime);
		}

		String minSafeDistance = params.getProperty("minSafeDistance");
		if(minSafeDistance != null) {
			paramsList.add("-Y");
			paramsList.add(minSafeDistance);
		}

		String minimalSpeedUsingPrediction = params.getProperty("minimalSpeedUsingPrediction");
		if ("true".equals(minimalSpeedUsingPrediction)) {
			paramsList.add("-m");
		}

		if (params.getProperty("globalUpdateInterval") != null) {
			paramsList.add("-u");
			paramsList.add(params.getProperty("globalUpdateInterval"));
		}

		paramsList.add("-t");
		paramsList.add(params.getProperty("yellowTransition"));
		paramsList.add("-s");
		paramsList.add("975");
		paramsList.add("-S");
		paramsList.add("1298");

		String routing = params.getProperty("dynamicRouting");
		if ((routing != null) && !(routing.isEmpty())) {
			paramsList.add("-r");
			paramsList.add(routing);
			paramsList.add("-d");
			paramsList.add("100");
			paramsList.add("-k");
			paramsList.add("100");
		}

		String enablePrediction = params.getProperty("enablePrediction");
		if ((enablePrediction != null) && !(enablePrediction.isEmpty())) {
			paramsList.add("-e");
			paramsList.add(enablePrediction);
		} else {
			paramsList.add("-e");
			paramsList.add("false");
		}

		String predictionModule = params.getProperty("predictionModule");
		if ((predictionModule != null) && !(predictionModule.isEmpty())) {
			paramsList.add("-a");
			paramsList.add(predictionModule);
		} else {
			paramsList.add("-a");
			paramsList.add("false");
		}

		String statOutFileParam = params.getProperty("statOutFile");
		if ((statOutFileParam != null) && !(statOutFileParam.isEmpty())) {
			paramsList.add("-o");
			paramsList.add(statOutFileParam);
		}

		paramsList.add(params.getProperty("algorithm"));
		paramsList.add(params.getProperty("cityMapFile"));
		paramsList.add(params.getProperty("travelSchemeFile"));

		return paramsList.toArray(new String[paramsList.size()]);
	}

	public static void configurePrediction(String configFile) {
		if (configFile == null) {
			createDefaultPrediction();
			return;
		}
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			sp.parse(configFile, new PredictionConfigurationXmlHandler());
		} catch (ParserConfigurationException | SAXException e) {
			disablePrediction();
			LOGGER.error("Error when parsing configuration.", e);
		} catch (IOException e) {
			disablePrediction();
			LOGGER.error(String.format("File: %s couldn't be read.", configFile), e);
		}
	}

	private static void createDefaultPrediction() {
		ITrafficPredictionSetup predictionSetup = new DefaultTrafficPredictionSetup()
				.setCutOutProbability(0.75)
				.setCutOutMinimumCounter(3)
				.setDiscretizer(createDefaultDiscretiser())
				.setNumberOfInfluencedLinks(3)
				.setNumberOfInfluencedTimesteps(3);
		TrafficPredictionFactory.setPropertiesForPredictionSetup(predictionSetup);
	}

	public static void disablePrediction() {
		ITrafficPredictionSetup predictionSetup = new DefaultTrafficPredictionSetup()
				.setCutOutProbability(1.5)
				.setCutOutMinimumCounter(Integer.MAX_VALUE)
				.setDiscretizer(new TrafficLevelDiscretizer())
				.setNumberOfInfluencedLinks(0)
				.setNumberOfInfluencedTimesteps(0);
		TrafficPredictionFactory.setPropertiesForPredictionSetup(predictionSetup);
	}

	public static TrafficLevelDiscretizer createDefaultDiscretiser() {
		TrafficLevelDiscretizer result = new TrafficLevelDiscretizer();
		result.populateTrafficLevels();
		return result;
	}

	public static String getSNADistanceType() {
		return getPropertiesFromFile().getProperty("snaDistanceType");
	}

    public static Properties createDefaultSessionConfig(){
        Properties sessionProperties = new Properties();
        sessionProperties.setProperty("workDir", ".");
        sessionProperties.setProperty("cityMapFile", "");
        sessionProperties.setProperty("travelSchemeFile", "");
        sessionProperties.setProperty("centralNodesAlgMod", "none\\:Lack");
        sessionProperties.setProperty("algorithm", "sotl");
        sessionProperties.setProperty("carMoveModel", "nagle:decProb=0.2");
        sessionProperties.setProperty("yellowTransition", "3");
        sessionProperties.setProperty("statOutFile", "output/statistics/stats.txt");
        sessionProperties.setProperty("zone_awareness", "enabled");

        return  sessionProperties;
    }
}

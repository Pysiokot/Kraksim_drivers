package pl.edu.agh.cs.kraksim.parser;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import pl.edu.agh.cs.kraksim.routing.prediction.*;

public class PredictionConfigurationXmlHandler extends DefaultHandler {
	private static final Logger LOGGER = Logger.getLogger(PredictionConfigurationXmlHandler.class);
	private Level level = Level.INIT;
	private TrafficLevelDiscretiser discretiser = null;
	private ITrafficPredictionSetup setup = null;

	private enum Level {
		INIT, PREDICTION, TRAFFIC_CONF, LEVEL
	}

	public void startDocument() {
		setup = new DefaultTrafficPredictionSetup();
		discretiser = new TrafficLevelDiscretiser();
		setup.setDiscretiser(discretiser);
	}

	/**
	 * End document.
	 */
	@Override
	public void endDocument() {
		if (discretiser.getNumberOfLevels() < 1) {
			discretiser.populateTrafficLevels();
		}
		setup.setDiscretiser(discretiser);
		TrafficPredictionFactory.setPropertiesForPredictionSetup(setup);
	}

	public void startElement(String namespaceURI, String localName, String rawName, Attributes attrs) {

		switch (level) {
			case INIT:
				if (rawName.equals("prediction")) {
					level = Level.PREDICTION;
					configurePrediction(rawName, attrs);
				}
				break;
			case PREDICTION:
				if (rawName.equals("trafficLevels")) {
					level = Level.TRAFFIC_CONF;
				}
				break;
			case TRAFFIC_CONF:
				if (rawName.equals("level")) {
					level = Level.LEVEL;
					appendLevel(rawName, attrs);
				}
				break;
		}
	}

	private void configurePrediction(String rawName, Attributes attrs) {
		String cop = attrs.getValue("cutOutProbability");
		String comn = attrs.getValue("cutOutMinimumNumber");
		String r = attrs.getValue("neighborhoodSize");
		String h = attrs.getValue("influencedTimesteps");
		String age = attrs.getValue("ageingRate");

		double cutOutProp = Double.parseDouble(cop);
		int cutOutMinNo = Integer.parseInt(comn);
		int ngbSize = Integer.parseInt(r);
		int deltaT = Integer.parseInt(h);
		double ageingRate = 1.0;
		ageingRate = Double.parseDouble(age);

		setup.setCutOutProbability(cutOutProp);
		setup.setCutOutMinimumCounter(cutOutMinNo);
		setup.setNumberOfInfluencedLinks(ngbSize);
		setup.setNumberOfInfluencedTimesteps(deltaT);
		setup.setAgeingRate(ageingRate);
	}

	private void appendLevel(String rawName, Attributes attrs) {
		TrafficLevel trLvl = new TrafficLevel();
		String description = attrs.getValue("description");
		String lowerBound = attrs.getValue("lowerBound");
		String upperBound = attrs.getValue("upperBound");
		String influence = attrs.getValue("influence");
		String prevDescription = attrs.getValue("prevDescription");
		String nextDescription = attrs.getValue("nextDescription");

		double lBound = Double.parseDouble(lowerBound);
		double uBound = Double.parseDouble(upperBound);
		double maxInfluence = Double.parseDouble(influence);

		trLvl.setDescription(description);
		trLvl.setLowerLevel(lBound);
		trLvl.setUpperLevel(uBound);
		trLvl.setMaxInfluence(maxInfluence);

		try {
			discretiser.addTrafficLevel(trLvl);
		} catch (TrafficPredictionException e) {
			LOGGER.error(e);
			return;
		}

		try {
			TrafficLevel temp = discretiser.getLevelByName(prevDescription);
			temp.setProceeder(trLvl);
			trLvl.setPredecessor(temp);
		} catch (TrafficPredictionException ex) {
		}

		try {
			TrafficLevel temp = discretiser.getLevelByName(nextDescription);
			trLvl.setProceeder(trLvl);
			temp.setPredecessor(temp);
		} catch (TrafficPredictionException ex) {
		}
	}

	/**
	 * End element.
	 */
	@Override
	public void endElement(String namespaceURI, String localName, String rawName) {
		switch (level) {
			case INIT:
				break;
			case PREDICTION:
				if (rawName.equals("prediction")) {
					level = Level.INIT;
				}
				break;
			case TRAFFIC_CONF:
				if (rawName.equals("trafficLevels")) {
					level = Level.PREDICTION;
					setup.setDiscretiser(discretiser);
				}
				break;
			case LEVEL:
				if (rawName.equals("level")) {
					level = Level.TRAFFIC_CONF;
				}
				break;
		}
	}

	@Override
	public void warning(SAXParseException ex) {
		LOGGER.error("[Warning]: " + ex.getMessage());
	}

	@Override
	public void error(SAXParseException ex) {
		LOGGER.error("[Error]: " + ex.getMessage());
	}

	@Override
	public void fatalError(SAXParseException ex) throws SAXException {
		LOGGER.error("[Fatal Error]: " + ex.getMessage());
		throw ex;
	}
}

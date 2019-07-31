package pl.edu.agh.cs.kraksim.parser;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import pl.edu.agh.cs.kraksim.core.City;
import pl.edu.agh.cs.kraksim.core.Gateway;
import pl.edu.agh.cs.kraksim.main.StartupParameters;
import pl.edu.agh.cs.kraksim.traffic.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TrafficDataXmlHandler extends DefaultHandler {
	private static final Logger LOGGER = Logger.getLogger(TrafficDataXmlHandler.class);
	public static final int TRAFFIC_LEVEL = 0;
	public static final int SCHEME_LEVEL = 2;
	public static final int GATEWAY_LEVEL = 3;
	public static final int ZONE_DRIVERS_LEVEL = 4;

	private final City c;
	private final StartupParameters parameters;
	private List<Gateway> gateways;
	private List<Distribution> departureDists;
	private List<TravellingScheme> schemes = null;
	private List<Pair<String, Double>> zones;
	private int level = 0;
	private int count;
	private Color driverColor = null;
	private Color emergencyVehicleColor = null;

	public TrafficDataXmlHandler(City c, StartupParameters parameters) {
		this.c = c;
		this.parameters = parameters;
	}

	@Override
	public void startDocument() {
		schemes = new ArrayList<>();
	}

	@Override
	public void startElement(String namespaceURI, String localName, String rawName, Attributes attrs) {
		switch (level) {
			case GATEWAY_LEVEL:
				createDistribution(rawName, attrs);
				break;

			case SCHEME_LEVEL:
				if (rawName.equals("gateway")) {
					String id = attrs.getValue("id");
					gateways.add((Gateway) c.findNode(id));
					// check for errors
					level = GATEWAY_LEVEL;
				}
				if(rawName.equals("zone")){
					String name = attrs.getValue("name");
					double percent = Double.parseDouble(attrs.getValue("percent"));
					zones.add(Pair.of(name, percent));
				}
				break;

			default:
				// jeszcze nie ustalony stan
				if (rawName.equals("scheme")) {
					gateways = new ArrayList<>();
					departureDists = new ArrayList<>();
					zones = new ArrayList<>();
					count = Integer.parseInt(attrs.getValue("count"));
					driverColor = Color.YELLOW;
					emergencyVehicleColor = Color.BLUE;

					level = SCHEME_LEVEL;
				}
				if (rawName.equals("traffic")) {
					// level = TrafficDataXmlHandler.SCHEME_LEVEL;
					level = 0;
				}
				break;
		}
	}

	@Override
	public void endElement(String namespaceURI, String localName, String rawName) {
		switch (level) {
			case SCHEME_LEVEL:
				if (rawName.equals("scheme")) {
					Gateway[] gws = new Gateway[0];
					Distribution[] ds = new Distribution[0];

					gws = gateways.toArray(gws);
					ds = departureDists.toArray(ds);
					TravellingScheme ts = new TravellingScheme(parameters, count, gws, ds, zones, driverColor, emergencyVehicleColor);
					schemes.add(ts);
					level = 0;// traffic
				}
				break;

			case GATEWAY_LEVEL:
				if (rawName.equals("gateway")) {
					level = SCHEME_LEVEL;
				}
				break;

			default:
				break;
		}
	} // endElement(String)

	@Override
	public void warning(SAXParseException ex) {
		LOGGER.error("[Warning] " + getLocationString(ex) + ": " + ex.getMessage());
	}

	@Override
	public void error(SAXParseException ex) {
		LOGGER.error("[Error] " + getLocationString(ex) + ": " + ex.getMessage());
	}

	@Override
	public void fatalError(SAXParseException ex) throws SAXException {
		LOGGER.error("[Fatal Error] " + getLocationString(ex) + ": " + ex.getMessage());
		throw ex;
	}

	private void createDistribution(String rawName, Attributes attrs) {
		Distribution distribution = null;
		if (rawName.equals("point")) {
			float y = Float.parseFloat(attrs.getValue("y"));
			distribution = new PointDistribution(y);
		}
		if (rawName.equals("uniform")) {
			float a = Float.parseFloat(attrs.getValue("a"));
			float b = Float.parseFloat(attrs.getValue("b"));
			distribution = new UniformDistribution(a, b);
		}
		if (rawName.equals("normal")) {
			float dev = Float.parseFloat(attrs.getValue("dev"));
			float y = Float.parseFloat(attrs.getValue("y"));
			distribution = new NormalDistribution(y, dev);
		}
		departureDists.add(distribution);
	}

	/**
	 * Zamienia łańcuch znaków na Color.
	 * Na razie obsługuje tylko format #RRGGBB.
	 */
	private static Color parseColor(String colorStr) {
		if (colorStr == null) {
			return null;
		}

		int r, g, b;
		try {
			if (colorStr.length() != 7 || !colorStr.startsWith("#")) {
				throw new RuntimeException();
			}
			r = Integer.parseInt(colorStr.substring(1, 3), 16);
			g = Integer.parseInt(colorStr.substring(3, 5), 16);
			b = Integer.parseInt(colorStr.substring(5, 7), 16);
		} catch (Exception e) {
			LOGGER.error(String.format("[ERROR] cannot parse string '%s' as carColor", colorStr));
			return null;
		}
		return new Color(r, g, b);
	}

	/**
	 * Returns a string of the location.
	 */
	private static String getLocationString(SAXParseException ex) {
		StringBuilder str = new StringBuilder();

		String systemId = ex.getSystemId();
		if (systemId != null) {
			int index = systemId.lastIndexOf('/');
			if (index != -1) {
				systemId = systemId.substring(index + 1);
			}
			str.append(systemId);
		}
		str.append(':').append(ex.getLineNumber()).append(':').append(ex.getColumnNumber());

		return str.toString();
	} // getLocationString(SAXParseException):String

	/*
	   * ArrayList<TravellingScheme> getSchemes() { schemes.toArray(new
	   * TravellingScheme[0]);
	   *
	   * return schemes; }
	   */
	public Collection<TravellingScheme> getSchemes() {
		return schemes;
	}

	// TravellingScheme getScheme() {
	// return ts;
	// }
	// TODO: log wielopoziomowy
	// TODO: levelUp
	// TODO: levelDown
	// TODO: checkLevel
	// TODO: setLevel
	// TODO: profilig
}

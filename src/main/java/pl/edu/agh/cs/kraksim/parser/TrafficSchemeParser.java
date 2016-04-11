package pl.edu.agh.cs.kraksim.parser;

import pl.edu.agh.cs.kraksim.core.City;
import pl.edu.agh.cs.kraksim.main.StartupParameters;
import pl.edu.agh.cs.kraksim.traffic.TravellingScheme;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.Collection;

public class TrafficSchemeParser {

	public static Collection<TravellingScheme> parse(String fileName, City city, StartupParameters parameters) throws IOException, ParsingException {
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			TrafficDataXmlHandler tdXmlhandler = new TrafficDataXmlHandler(city, parameters);

			sp.parse(fileName, tdXmlhandler);

			return tdXmlhandler.getSchemes();
		} catch (Exception e) {
			throw new ParsingException("Parsing exception : ", e);
		}
	}
}

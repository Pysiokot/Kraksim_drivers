package pl.edu.agh.cs.kraksim.parser;

import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.core.Core;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;

public class ModelParser {
	private static final Logger LOGGER = Logger.getLogger(ModelParser.class.getName());

	public static Core parse(String fileName) throws IOException, ParsingException {
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			RoadNetXmlHandler rnXmlhandler = new RoadNetXmlHandler();

			sp.parse(fileName, rnXmlhandler);

			return rnXmlhandler.getCore();
		} catch (Exception e) {
			throw new ParsingException("Parsing exception : ", e);
		}
	}
}

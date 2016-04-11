package pl.edu.agh.cs.kraksim.weka.utils;

import jxl.common.Logger;
import weka.associations.Apriori;
import weka.associations.FPGrowth.AssociationRule;
import weka.associations.ItemSet;
import weka.associations.Tertius;
import weka.associations.tertius.SimpleLinkedList;
import weka.core.FastVector;
import weka.core.Instances;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class RulesWriter {
	private static final Logger LOGGER = Logger.getLogger(RulesWriter.class);
	private final String outputMainFolder;

	public RulesWriter(String outputMainFolder) {
		this.outputMainFolder = outputMainFolder;
	}

	public void writeDataSetToFile(Instances instances) {
		String fileName = instances.attribute(0).name() + ".arff";
		writeToFile(fileName, instances.toString());
	}

	public void writeAprioriRulesToFile(Apriori apriori) {
		FastVector[] allRules = apriori.getAllTheRules();
		Instances onlyClass = apriori.getInstancesOnlyClass();
		Instances instances = apriori.getInstancesNoClass();
		StringBuilder text = new StringBuilder();
		for (int i = 0; i < allRules[0].size(); i++) {
			text.append(((ItemSet) allRules[0].elementAt(i)).toString(instances)).append(" ==> ").append(((ItemSet) allRules[1].elementAt(i)).toString(onlyClass));
			text.append('\n');
		}

		String fileName = onlyClass.attribute(0).name() + ".txt";
		writeToFile(fileName, text.toString());
	}

	public void writeFpGrowthRulesToFile(List<AssociationRule> allRulesFromAlgorithm, Instances instances) {
		StringBuilder text = new StringBuilder();
		for (AssociationRule rule : allRulesFromAlgorithm) {
			text.append(rule);
			text.append('\n');
		}
		String fileName = instances.attribute(0).name() + ".txt";
		writeToFile(fileName, text.toString());
	}

	public void writeTertiusRulesToFile(Tertius tertius, Instances instances) {
		SimpleLinkedList allRules = tertius.getResults();
		String fileName = instances.attribute(0).name() + ".txt";
		writeToFile(fileName, allRules.toString());
	}

	private void writeToFile(String fileName, String text) {
		String path = createFolderPath();
		String filePath = path + File.separator + fileName;
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
			writer.write(text);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			LOGGER.error(e);
			e.printStackTrace();
		}
	}

	private String createFolderPath() {
		int counter = 0;
		String dir = "output" + File.separator + outputMainFolder + File.separator + counter + File.separator;
		File folder = new File(dir);
		if (!folder.isDirectory()) {
			folder.mkdirs();
		}
		return dir;
	}
}

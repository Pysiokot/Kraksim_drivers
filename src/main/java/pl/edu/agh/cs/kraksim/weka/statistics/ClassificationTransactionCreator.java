package pl.edu.agh.cs.kraksim.weka.statistics;

import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.weka.PredictionSetup;
import pl.edu.agh.cs.kraksim.weka.data.History;
import pl.edu.agh.cs.kraksim.weka.data.LinkInfo;
import pl.edu.agh.cs.kraksim.weka.data.Transaction;
import pl.edu.agh.cs.kraksim.weka.utils.TransactionCreator;
import weka.core.Instance;

import java.util.ArrayList;

public class ClassificationTransactionCreator extends TransactionCreator {
	private static final Logger LOGGER = Logger.getLogger(ClassificationTransactionCreator.class);

	public ClassificationTransactionCreator(PredictionSetup setup) {
		super(setup);
	}

	public Transaction createTestTransaction(History historyArchive, LinkInfo classRoad) {
		ArrayList<Double> attributeValues = new ArrayList<>();
		attributeValues.add(Instance.missingValue());
		int historyDepth = setup.getMaxNumberOfInfluencedTimesteps() - setup.getMinNumberOfInfluencedTimesteps() + 1;
		addNoClassAttributeValues(historyArchive, classRoad, attributeValues, 0, historyDepth);

		LOGGER.debug("Test transaction: " + attributeValues);
		return new Transaction(attributeValues);
	}
}

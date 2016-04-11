package pl.edu.agh.cs.kraksim.weka.statistics;

import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.weka.PredictionSetup;
import pl.edu.agh.cs.kraksim.weka.data.LinkInfo;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResultCreator {
	private static final Logger LOGGER = Logger.getLogger(ResultCreator.class);
	private final LinkStats linkStats;
	private final Archive<Boolean> congestionArchive;
	private final PredictionArchive predictionArchive;
	private long totalItemsAmount;
	private long totalCongestionsAmount;
	private long totalPredictableCongestionsAmount;
	private long falsePositiveCongestions;
	private long falseNegativeCongestions;
	private long truePositiveCongestions;
	private long trueNegativeCongestions;

	public ResultCreator(PredictionSetup setup, Archive<Boolean> congestionArchive, PredictionArchive predictionArchive) {
		linkStats = new LinkStats(setup);
		this.congestionArchive = congestionArchive;
		this.predictionArchive = predictionArchive;
	}

	private void resetResults() {
		totalItemsAmount = 0;
		totalCongestionsAmount = 0;
		totalPredictableCongestionsAmount = 0;
		falsePositiveCongestions = 0;
		falseNegativeCongestions = 0;
		truePositiveCongestions = 0;
		trueNegativeCongestions = 0;
	}

	public void computePartialResults(Set<LinkInfo> predictableLinks) {
		LOGGER.debug("Congestions: " + congestionArchive);
		LOGGER.debug("Predictions: " + predictionArchive);
		for (Integer turn : predictionArchive) {
			List<Boolean> congestionList = congestionArchive.getCongestionListByTurn(turn);
			Set<LinkInfo> predictingCongestionLinks = predictionArchive.getDurationListByTurn(turn);
			Set<Integer> predictingLinkNumbers = linkIdsToLinkNumbers(predictingCongestionLinks);
			for (int linkNumber = 0; linkNumber < congestionList.size(); linkNumber++) {
				boolean congestion = congestionList.get(linkNumber);
				boolean isLinkPredictingCongestion = predictingLinkNumbers.contains(linkNumber);

				totalItemsAmount++;
				if (congestion) {
					totalCongestionsAmount++;
				}

				if (predictableLinks.contains(new LinkInfo(linkNumber, "link", 0))) {
					if (congestion) {
						linkStats.countCongestionOnLink(linkNumber);
						totalPredictableCongestionsAmount++;
					}

					if (congestion && isLinkPredictingCongestion) {
						linkStats.countTruePositive(linkNumber);
						truePositiveCongestions++;
					}
					if (congestion && !isLinkPredictingCongestion) {
						linkStats.countFalseNegative(linkNumber);
						falseNegativeCongestions++;
					}
					if (!congestion && isLinkPredictingCongestion) {
						linkStats.countFalsePositive(linkNumber);
						falsePositiveCongestions++;
					}
					if (!congestion && !isLinkPredictingCongestion) {
						linkStats.countTrueNegative(linkNumber);
						trueNegativeCongestions++;
					}
				}
			}
		}
		congestionArchive.clear();
		predictionArchive.clear();
	}

	Set<Integer> linkIdsToLinkNumbers(Set<LinkInfo> linkIds) {
		Set<Integer> linkNumbers = new HashSet<>();
		for (LinkInfo linkInfo : linkIds) {
			linkNumbers.add(linkInfo.linkNumber);
		}
		return linkNumbers;
	}

	public long getTotalCongestionsAmount() {
		return totalCongestionsAmount;
	}

	public long getFalsePositiveCongestions() {
		return falsePositiveCongestions;
	}

	public long getFalseNegativeCongestions() {
		return falseNegativeCongestions;
	}

	public long getTruePositiveCongestions() {
		return truePositiveCongestions;
	}

	public long getTotalItemsAmount() {
		return totalItemsAmount;
	}

	public String getResultText() {
		StringBuilder builder = new StringBuilder();

		builder.append("Time: ").append(new Date()).append('\n');
		builder.append("Total Items: ").append(totalItemsAmount).append('\n');
		builder.append("Total Congestion: ").append(totalCongestionsAmount).append('\n');
		builder.append("Total predictable congestions: ").append(totalPredictableCongestionsAmount).append('\n');
		builder.append("True Positive: ").append(truePositiveCongestions).append('\n');
		builder.append("True Negative: ").append(trueNegativeCongestions).append('\n');
		builder.append("False Negative ").append(falseNegativeCongestions).append('\n');
		builder.append("False Positive ").append(falsePositiveCongestions).append('\n');
		builder.append(congestionsPercentage());
		builder.append(nonCongestionsPercentage());

		builder.append(linkStats);
		return builder.toString();
	}

	private String nonCongestionsPercentage() {
		long nonCongestions = falsePositiveCongestions + trueNegativeCongestions;
		double predicted = (double) trueNegativeCongestions / nonCongestions;
		double notPredicted = (double) falsePositiveCongestions / nonCongestions;

		DecimalFormat df = new DecimalFormat("#.###");
		String text = "\nNon congestion class\n";
		text += "Positive: " + df.format(predicted) + '\n';
		text += "Negative: " + df.format(notPredicted) + '\n';
		return text;
	}

	private String congestionsPercentage() {
		long congestions = truePositiveCongestions + falseNegativeCongestions;
		double predicted = (double) truePositiveCongestions / congestions;
		double notPredicted = (double) falseNegativeCongestions / congestions;

		DecimalFormat df = new DecimalFormat("#.###");
		String text = "\nCongestion class\n";
		text += "Positive: " + df.format(predicted) + '\n';
		text += "Negative: " + df.format(notPredicted) + '\n';
		return text;
	}
}

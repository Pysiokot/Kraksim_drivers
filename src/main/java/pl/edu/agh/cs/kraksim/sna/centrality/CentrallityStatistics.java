package pl.edu.agh.cs.kraksim.sna.centrality;

import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.core.Node;
import pl.edu.agh.cs.kraksim.ministat.CityMiniStatExt;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Klasa pomocnicza do zapisywania wybranych statystyk
 */
public class CentrallityStatistics {
	private static final Logger LOGGER = Logger.getLogger(CentrallityStatistics.class);

	private static final String STATS_DIR = "output/centrallity_stat/";
	private static final String TRAVEL_FILE_NAME = "travelTimes";
	private static final String CLUSTER_FILE_NAME = "clusteringData";

	private static Map<Node, Integer> clusterCounter;
	private static List<int[]> clusterSizes;

	public static void writeTravelTimeData(CityMiniStatExt stat, int turn) {
		Date now = Calendar.getInstance().getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
		String filePath = STATS_DIR + TRAVEL_FILE_NAME + sdf.format(now) + ".txt";
		File file = new File(filePath);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file, true);
			fw.write(turn + " " + stat.getTravelDuration() + ' ' + stat.getAvgVelocity() + ' ' + stat.getAvgCarSpeed() + ' ' + stat.getAvgCarLoad() + ' ' + stat.getTravelLength() + ' ' + stat.getCarCount() + "\r\n");
			fw.flush();
			fw.close();
		} catch (IOException e) {
			LOGGER.error("IOException", e);
		}
	}

	public static void writeKlasteringInfo(int turn) {
		Map<Node, Set<Node>> clusters = KmeansClustering.currentClustering;

		if (clusterCounter == null) {
			clusterCounter = new LinkedHashMap<>();
		}
		if (clusterSizes == null) {
			clusterSizes = new ArrayList<>();
		}

		String filePath = STATS_DIR + CLUSTER_FILE_NAME + turn + ".txt";
		File file = new File(filePath);

		try {
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			FileWriter fw = new FileWriter(file, false);

			int[] sizes = new int[KmeansClustering.getClaster_number()];
			int i = 0;
			for (Map.Entry<Node, Set<Node>> bossWithCluster : clusters.entrySet()) {
				int count = clusterCounter.containsKey(bossWithCluster.getKey()) ? clusterCounter.get(bossWithCluster.getKey()) + 1: 1;
				clusterCounter.put(bossWithCluster.getKey(), count);
				sizes[i++] = bossWithCluster.getValue().size();
			}

			clusterSizes.clear(); //show only last votes (not all)
			clusterSizes.add(sizes);

			for (Map.Entry<Node, Integer> bossWithCluster : clusterCounter.entrySet()) {
				fw.write(bossWithCluster.getKey().getId() + ':' + bossWithCluster.getValue() + "\t");
				
			}
			fw.write("\r\n");
			for (int[] s : clusterSizes) {
				for (int value : s) {
					fw.write(value + "\t");
				}
				//fw.write("\r\n");
			}


			fw.flush();
			fw.close();
		} catch (Exception e) {
			LOGGER.error("IOException", e);
		}
	}
}

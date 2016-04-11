package pl.edu.agh.cs.kraksim.sna.centrality;

import com.google.common.collect.Sets;
import edu.uci.ics.jung.graph.Graph;

import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.KraksimConfigurator;
import pl.edu.agh.cs.kraksim.core.Gateway;
import pl.edu.agh.cs.kraksim.core.Intersection;
import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.core.Node;
import pl.edu.agh.cs.kraksim.sna.SnaConfigurator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Klastrowanie kmeans
 */
public class KmeansClustering {
	private static final Logger LOGGER = Logger.getLogger(KmeansClustering.class);

	public static Map<Node, Set<Node>> currentClustering;
	private static Properties properties = null;
	private static Map<Map<Node, Node>, Integer> linkloadMap = new HashMap<>();

	public static Properties getProperties() {
		return properties;
	}

	public static void setProperties(Properties properties) {
		KmeansClustering.properties = properties;
	}

	public static List<Set<Node>> clusterGraph(Graph<Node, Link> graph) {
		List<Node> allNodes = new ArrayList<>(graph.getVertices());
		List<Node> mainNodes = getMainNodes(allNodes, graph);
		List<Set<Node>> clusters = new ArrayList<>();

		// wrzucamy glowne wezly jako oddzielne clustry
		for (Node main : mainNodes) {
			clusters.add(Sets.newHashSet(main));
		}
		String centralityAlgMod = properties.getProperty("centralNodesAlgMod");
		String[] algElem = new String[2];
		if (centralityAlgMod != null) {
			algElem = centralityAlgMod.split(":");
		} else {
			algElem[0] = KraksimConfigurator.getSNADistanceType();
		}
		SNADistanceType type = SNADistanceType.Lack;
		if (algElem[0].equals("simple")) {
			try {
				type = SNADistanceType.valueOf(algElem[1]);
			} catch (Exception e) {
				// Intentially empty
			}
		}
		for (Node node : allNodes) {
			if (mainNodes.contains(node)) {
				continue;
			}
			
			Node closestMean = findClosestMean(node, mainNodes, graph, type);

			for (Set<Node> cluster : clusters) {
				if (cluster.contains(closestMean)) {
					cluster.add(node);
					break;
				}
			}
		}

		// wype�nianie mapy srodek clastra - claster
		currentClustering = new LinkedHashMap<>();
		for (Node n : mainNodes) {
			for (Set<Node> cluster : clusters) {
				if (cluster.contains(n)) {
					currentClustering.put(n, cluster);
					break;
				}
			}
		}

		StringBuilder sb = new StringBuilder();
		for (Node n : mainNodes) {
			if (sb.length() != 0)
				sb.append(", ");
			sb.append(n.getId());
		}
		LOGGER.info("Nodes in clusters: " + sb);
		printClusters(clusters);

		return clusters;
	}

	private static Node findClosestMean(Node node, List<Node> mainNodes,
			Graph<Node, Link> graph, SNADistanceType type) {
		Node closest = null;
		double minDist = Double.MAX_VALUE;
		double distance;
		for (Node mean : mainNodes) {
			if(type != null && type != SNADistanceType.Lack) { //w przypadku glosowania, zgodnie z typem wybranym w parametrze
				distance = getDistance(node, mean, graph, type);
			} else { //w przypadku bez glosowania najblizsze glowne skrzyzowanie wybieramy przez miare euklidesowa
				distance = node.getPoint().distance(mean.getPoint());
			} if (distance < minDist) {
				minDist = distance;
				closest = mean;
			}
		}

		return closest;
	}

	public static int getClaster_number() {
		return SnaConfigurator.getSnaClusters();
	}

	/**
	 * Wyznacza odlęgłość między dwoma sąsiadującymi węzłami w grafie.
	 *
	 * @param nodeA
	 * @param nodeB
	 * @param graph
	 * @param type  Sposób obliczania odległości mięzy węzłami.
	 * @return
	 */
	private static int distanceToAdd(Node nodeA, Node nodeB, Graph<Node, Link> graph, SNADistanceType type) {
		switch (type) {
			case CrossroadsNumber:
				return 1;
			case Weight:
				Map<Node, Node> p=new HashMap<Node, Node>();
				p.put(nodeA, nodeB);
				linkloadMap.put(p,(int) graph.findEdge(nodeA, nodeB).getLoad());
				return (int) graph.findEdge(nodeA, nodeB).getWeight() * 100;
			case Load:
				Map<Node, Node> p2=new HashMap<Node, Node>();
				p2.put(nodeA, nodeB);
				linkloadMap.put(p2,(int) graph.findEdge(nodeA, nodeB).getLoad());
				return (int) graph.findEdge(nodeA, nodeB).getLoad() * 100;
			default:
				break;
		}
		return 0;
	}

	/**
	 * Oblicza wartość najkrótszej ścieżki w grafie między dwoma wezłami. Koszt
	 * krawędzi zalezy od typu podanego jako parametr 'type'. Wykorzystuje
	 * algorytm Dijkstry.
	 *
	 * @param nA
	 * @param nB
	 * @param graph
	 * @param type  Sposób obliczania odległości miedzy wezłami.
	 * @return
	 */
	private static int getDistance(Node nA, Node nB, Graph<Node, Link> graph, SNADistanceType type) {
		if (nA == nB) {
			return 0;
		}

		HashMap<Node, Integer> distance = new HashMap<>();
		for (Node n : graph.getVertices()) {
			distance.put(n, Integer.MAX_VALUE);
		}
		distance.put(nA, 0);

		List<Node> visited = new LinkedList<>();
		Node current = nA;
		while (!visited.contains(nB)) {
			visited.add(current);
			for (Node neig : graph.getNeighbors(current)) { // aktualizaja
				// odległości
				if (!visited.contains(neig)) {
					// obliczanie odległosci miedzy sąsiadami - "current" i
					// "neig"
					int dist = distanceToAdd(current, neig, graph, type);
					// doliczanie odległosci
					distance.put(neig, distance.get(current) + dist);
				}
			}

			double minMeasure = Double.MAX_VALUE; // nowy current
			Node minNode = null;
			for (Node n : distance.keySet()) {
				if (!visited.contains(n) && distance.get(n) < minMeasure) {
					minMeasure = distance.get(n);
					minNode = n;
				}
			}
			current = minNode;
		}
		return distance.get(nB);
	}

	/**
	 * Dla danego węzła znajduje kandydatów, na których może on głosować. W
	 * obecnej wersji kazdy z węzłów(nie będący typu Gateway) posiada bierne
	 * prawo wyborcze. Oczywiście węzeł nie może głosować na samego siebie.
	 *
	 * @param node  Wezeł dla którego wyznaczamy kandydatów
	 * @param nodes
	 * @param graph
	 * @return Lista wezłów na które można głosować
	 */
	private static List<Node> getCandidates(Node node, List<Node> nodes, Graph<Node, Link> graph) {
		List<Node> candidates = new LinkedList<>(nodes);
		candidates.remove(node);
		synchronized (candidates) {
			Iterator<Node> it = candidates.iterator();
			while (it.hasNext()) {
				Node n = it.next();
				if (n instanceof Gateway) {
					it.remove();
				}
			}
		}
		return candidates;
	}

	/**
	 * Na podstawie dwóch list - listy węzłów i odpowiadających im
	 * współczynników atrakcyjności wybierany jest jeden węzeł o największej
	 * wartości. W modelu tym każdy z węzłów wybiera jednego zwycięzcę. Listy
	 * wejściowe sa równoliczne.
	 *
	 * @param candidates      Lista węzłów
	 * @param results         Obliczona "atrakcyjność" danego węzła
	 * @param numberOfWinners Liczba wezlow wskazanych jako zwyciezcy
	 * @return Lista ze zwycięzcami głosowania. Zawiera jeden element
	 */
	private static List<Node> getWinners(List<Node> candidates, List<Double> results, int numberOfWinners) {
		List<Node> winners = new LinkedList<>();
		for (int j = 0; j < numberOfWinners; j++) {
			Double max = results.get(0);
			Node winner = candidates.get(0);
			for (int i = 1; i < candidates.size(); i++) {
				if (results.get(i) > max) {
					max = results.get(i);
					winner = candidates.get(i);
				}
			}
			winners.add(winner);
			results.set(candidates.indexOf(winner), Double.MIN_VALUE);
		}
		return winners;
	}

	/**
	 * Procedura głosowania. Kazdy z wezłów głosuje na jenego kandydata. Wyniki
	 * sa zliczane, zwycięża ten z największa liczbą głosów.
	 *
	 * @param nodes Wezły
	 * @param graph
	 * @param type
	 * @return Lista zawierająca zwycięzców - główne węzły w klastrach. Liczba
	 * zwycięzców wyznaczana przez "getClaster_number".
	 */
	private static List<Node> voting(List<Node> nodes, Graph<Node, Link> graph, SNADistanceType type, int numberOfWinners) {
		//System.out.println();
		List<Node> winners = new LinkedList<>();
		// struktura do zliczania głosów
		HashMap<Node, Integer> results = new HashMap<>();
		for (Node n : nodes) {
			if (n instanceof Intersection) {
				results.put(n, 0);
			} else {
				results.put(n, Integer.MIN_VALUE);
			}
		}

		// każdy wezeł głosuje
		for (Node node : nodes) {
			List<Node> localWinners = null;
			List<Node> candidates = getCandidates(node, nodes, graph);
			List<Double> values = new LinkedList<>();
			for (Node candidate : candidates) {
				double measure = candidate.getMeasure();
				double distance = getDistance(node, candidate, graph, type);
				double value = measure / distance;
				values.add(value);
			}
			writeStats(node, candidates, values, type, numberOfWinners);
			localWinners = getWinners(candidates, values, numberOfWinners);
			for (Node n : localWinners) {
				results.put(n, results.get(n) + 1);
			}
			
		}
		// po głosowaniu, wyłaniamy zwycięzców
		for (int i = 0; i < getClaster_number(); i++) {
			int maxMeasure = Integer.MIN_VALUE;
			Node maxNode = null;
			for (Node n : results.keySet()) {
				if (results.get(n) > maxMeasure && n instanceof Intersection) {
					maxMeasure = results.get(n);
					maxNode = n;
				}
			}
			winners.add(maxNode);
			results.put(maxNode, Integer.MIN_VALUE); // żeby nie wybrać ponownie
			// tego samego
		}
		return winners;
	}

	/**
	 * Funkcja wypisuje statystyki głosowania dla każdego węzła
	 * @param node wezel dla ktorego piszemy statystyki
	 * @param candidates węzły na które może głosować
	 * @param values wartości głosów
	 * @param type typ wylicznai wartości
	 * @param numberOfWinners liczba głosów które może oddać
	 */
	private static void writeStats(Node node, List<Node> candidates,List<Double> values, SNADistanceType type, int numberOfWinners) {
		String file_path="output\\vote_stat\\"+node.getId()+"_"+type.toString()+"_"+Integer.toString(numberOfWinners)+".txt";
		try {
			FileWriter file=new FileWriter(file_path, true);
			BufferedWriter bw=new BufferedWriter(file);
			for(int i=0;i<candidates.size();i++){
				double val=values.get(i);
				Node can=candidates.get(i);
				String info=can.getId()+":"+Double.toString(val)+"\r\n";
				bw.write(info);
			}
			bw.write("\r\n");
			bw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * Glosowanie z wykorzystaniem "sumienia". Dany wezel nie może wygrac
	 * wyborow wiecej niz okreslana ilosc razy.
	 *
	 * @param nodes
	 * @param graph
	 * @param type
	 * @return
	 */
	private static List<Node> votingWithConscience(List<Node> nodes, Graph<Node, Link> graph, SNADistanceType type) {
		// TODO : Future works
		return null;
	}

	/**
	 * W zależności od sposobu wyboru głównych węzłów w grafie(ustawione w pliku
	 * konfiguracyjnym) przechodzi do procedury głosowania lub metody opartej
	 * wyłącznie o wagi wezłów.
	 *
	 * @param nodes
	 * @param graph
	 * @return
	 */
	private static List<Node> getMainNodes(List<Node> nodes, Graph<Node, Link> graph) {
		String centralityAlgMod = properties.getProperty("centralNodesAlgMod");
		String[] algElem = new String[2];
		if (centralityAlgMod != null) {
			algElem = centralityAlgMod.split(":");
		} else {
			algElem[0] = KraksimConfigurator.getSNADistanceType();
		}
		if (algElem[0].equals("none")) {
			return getMainNodes(nodes);
		} else if (algElem[0].equals("simple")) {
			SNADistanceType type = SNADistanceType.Lack;
			try {
				type = SNADistanceType.valueOf(algElem[1]);
			} catch (Exception e) {
				// Intentially empty
			}
			if (type == null || type == SNADistanceType.Lack) {
				return getMainNodes(nodes);
			}
			Integer numberOfWiners = null;
			try {
				numberOfWiners = Integer.parseInt(properties.getProperty("numberOfWiners"));
			} catch (Exception e) {
				// Intentially empty
			}
			return voting(nodes, graph, type, numberOfWiners); // TODO
		}
		return null;
	}

	/**
	 * Wyznaczanie głównych węzłów bez użycia algorytmu głosujacego.
	 *
	 * @param nodes
	 * @return
	 */
	private static List<Node> getMainNodes(List<Node> nodes) {
		List<Node> mainNodes = new ArrayList<>();
		for (int i = 0; i < getClaster_number(); i++) {
			double maxMeasure = Double.MIN_VALUE;
			Node maxNode = null;
			for (Node node : nodes) {
				double measure = node.getMeasure();
				if (!mainNodes.contains(node) && measure > maxMeasure) {
					maxMeasure = node.getMeasure();
					maxNode = node;
				}
			}
			if (maxNode != null) {
				mainNodes.add(maxNode);
			}
		}
		return mainNodes;
	}

	private static void printClusters(List<Set<Node>> clusters) {
		int i = 1;
		for (Set<Node> cluster : clusters) {
			String sbBeginning = "Cluster nr. " + i + ": ";
			StringBuilder sb = new StringBuilder("Cluster nr. " + i + ": ");
			for (Node node : cluster) {
				if (sb.length() != sbBeginning.length())
					sb.append(", ");
				sb.append(node.getId());
			}
			LOGGER.info(sb.toString());
			i++;
		}
	}

	public static Set<Node> findMyCluster(Node node) {
		for (Set<Node> cluster : currentClustering.values()) {
			for (Node n : cluster) {
				if (n == node) {
					return cluster;
				}
			}
		}

		return null;
	}

	public static Node findMyMainNode(Node node) {
		for (Node boss : currentClustering.keySet()) {
			for (Node n : currentClustering.get(boss)) {
				if (n == node) {
					return boss;
				}
			}
		}

		return null;
	}
}

package pl.edu.agh.cs.kraksim.sna.centrality;

import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.scoring.HITS;
import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.graph.AbstractGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.core.*;
import pl.edu.agh.cs.kraksim.iface.carinfo.CarInfoCursor;
import pl.edu.agh.cs.kraksim.iface.carinfo.CarInfoIView;

import java.util.*;

/**
 * Klasa pomocnicza obliczajaca odpowiednie miary dla grafu
 */
public class CentralityCalculator {
	private static final Logger LOGGER = Logger.getLogger(CentralityCalculator.class);

	public static CarInfoIView carInfoView = null;
	public static MeasureType measureType = MeasureType.PageRank;

	public static void calculateCentrality(City city, MeasureType type) {
		Hypergraph<Node, Link> graph = cityToGraph(city);

		switch (type) {
			case PageRank:
				PageRank<Node, Link> pageRank = new PageRank<>(graph, 100);
				pageRank.evaluate();

				for (Node n : graph.getVertices()) {
					System.out.println(n.getId() + " - " + pageRank.getVertexScore(n));
				}
				break;
			case BetweenesCentrallity:
				BetweennessCentrality<Node, Link> betweenness = new BetweennessCentrality<>((Graph<Node, Link>) graph);
				for (Node n : graph.getVertices()) {
					System.out.println(n.getId() + " - " + betweenness.getVertexScore(n));
				}
				break;
			case HITS:
				HITS<Node, Link> hits = new HITS<>((Graph<Node, Link>) graph);
				for (Node n : graph.getVertices()) {
					System.out.println(n.getId() + " - " + hits.getVertexScore(n).authority);
				}
				break;
		}
	}

	/**
	 * Metoda obliczajca wartoci miar dla grafu wg miary wskazanej jako type
	 */
	public static void calculateCentrality(Graph<Node, Link> graph, MeasureType type, int subGraphsNumber) {
		double max = 0;
		type = measureType;

		if (carInfoView != null) {
			CarInfoCursor cursor;
			List<Link> links = new ArrayList<>(graph.getEdges());
			for (Link link : links) {
				Iterator<Lane> it = link.laneIterator();
				double cars = 0;
				while (it.hasNext()) {
					cursor = carInfoView.ext(it.next()).carInfoForwardCursor();
					while (cursor.isValid()) {
						cars++;
						cursor.next();
					}
				}
			}
		}

		Map<Link, Double> weights = new HashMap<>();
		Transformer<Link, Double> trans = new Transformer<Link, Double>() {
			public Double transform(Link arg0) {
				// TODO Auto-generated method stub
				return arg0.getWeight();
			}
		};

		for (Link l : graph.getEdges()) {
			weights.put(l, l.getWeight());
		}

		switch (type) {

			case PageRank:
				//PageRank<Node, Link> pageRank = new PageRank<Node, Link>(graph, 0);
				PageRank<Node, Link> pageRank = new PageRank<>(graph, trans, 0.95);
				pageRank.evaluate();
				LOGGER.info("PageRank:\r\n----------------------");
	
				for (Node n : graph.getVertices()) {
					double measure = pageRank.getVertexScore(n);
					n.setMeasure(measure);
					LOGGER.info(n.getId() + " score:\t" + measure);
					LOGGER.info("--------------------------------------------");
					if (measure > max) {
						max = measure;
					}
				}
				break;
			case BetweenesCentrallity:
				BetweennessCentrality<Node, Link> betweenness = new BetweennessCentrality<>(graph, trans);
				LOGGER.info("PageRank:\r\n----------------------");
				for (Node n : graph.getVertices()) {
					double measure = betweenness.getVertexScore(n);
					n.setMeasure(measure);
					LOGGER.info(n.getId() + " score:\t" + measure);
					LOGGER.info("--------------------------------------------");
					if (measure > max) {
						max = measure;
					}
				}
				break;
			case HITS:
				HITS<Node, Link> hits = new HITS<>(graph);
				for (Node n : graph.getVertices()) {
					double measure = hits.getVertexScore(n).hub;
					n.setMeasure(measure);
					if (measure > max) {
						max = measure;
					}
				}
				break;
		}
		LOGGER.info("");
		LOGGER.info("");
		
		double interval = max / subGraphsNumber;
		for (Node n : graph.getVertices()) {
			int nr = (int) (n.getMeasure() / interval);
			if (nr >= subGraphsNumber) {
				nr = subGraphsNumber - 1;
			}
			n.setSubGraphNumber(nr);
		}

		calculateSubGraphs(graph, subGraphsNumber);
		normalizeMeasures(graph);

		KmeansClustering.clusterGraph(graph);
	}

	public static List<Graph<Node, Link>> getSubGraphs(City city, int subGraphsCount, MeasureType type) {
		return null; // todo: Make this not null or delete the whole metod, unfinished
	}

	public static int subGraphNr(Graph<Node, Link> graph, int subGraphsNumber, Node node) {
		List<Node> nodes = (List<Node>) graph.getVertices();
		double min = Double.MAX_VALUE;
		double max = 0;
		for (Node n : nodes) {
			if (n.getMeasure() < min) {
				min = n.getMeasure();
			}
			if (n.getMeasure() > max) {
				max = n.getMeasure();
			}
		}
		double interval = max / subGraphsNumber;//(max - min) / subGraphsNumber;
		return (int) (node.getMeasure() / interval);
	}

	public static void calculateSubGraphs(Graph<Node, Link> graph, int subGraphsNumber) {
		List<Node> nodes = new ArrayList<>(graph.getVertices());
		List<Node> sortedNodes = new ArrayList<>();
		int count = nodes.size();
		for (int i = 0; i < count; i++) {
			double min = Double.MAX_VALUE;
			int indx = -1;
			for (int j = 0; j < nodes.size(); j++) {
				if (nodes.get(j).getMeasure() < min) {
					min = nodes.get(j).getMeasure();
					indx = j;
				}
			}
			sortedNodes.add(nodes.get(indx));
			nodes.remove(indx);
		}
		int interval = sortedNodes.size() / subGraphsNumber;
		for (int i = 0; i < sortedNodes.size(); i++) {
			int nr = i / interval;
			if (nr >= subGraphsNumber) {
				nr = subGraphsNumber - 1;
			}
			sortedNodes.get(i).setSubGraphNumber(nr);
		}
	}

	/**
	 * Metoda przeksztalcajca strukture City w strukture grafow
	 */
	public static AbstractGraph<Node, Link> cityToGraph(City city) {
		AbstractGraph<Node, Link> graph = new SparseGraph<>();

		Iterator<Gateway> gIter = city.gatewayIterator();
		while (gIter.hasNext()) {
			graph.addVertex(gIter.next());
		}

		Iterator<Intersection> iIter = city.intersectionIterator();
		while (iIter.hasNext()) {
			graph.addVertex(iIter.next());
		}

		Iterator<Link> lIter = city.linkIterator();
		while (lIter.hasNext()) {
			Link link = lIter.next();
			graph.addEdge(link, link.getBeginning(), link.getEnd(), EdgeType.DIRECTED);
//			LOGGER.debug(String.format("Adding link : %s - %s", link.getBeginning().getId(), link.getEnd().getId()));
		}

		for (Node n : graph.getVertices()) {
			if (n.isGateway()) {
				Iterator<Link> linkIter = n.outboundLinkIterator();
				Link link = linkIter.next();
//				LOGGER.debug(String.format("Gateway connected : %s - %s", link.getBeginning().getId(), link.getEnd().getId()));
			}
		}

		return graph;
	}

	private static void normalizeMeasures(Graph<Node, Link> graph) {
		Collection<Node> nodes = graph.getVertices();
		double min = Collections.min(nodes, prepareNodeComparator()).getMeasure();
		double max = Collections.max(nodes, prepareNodeComparator()).getMeasure();
		if (max - min > 0.00000001) { //i.e. max - min != 0
			for (Node n : nodes) {
				n.setMeasure((n.getMeasure() - min) / (max - min));
			}
		}
	}

	private static Comparator<Node> prepareNodeComparator() {
		return new Comparator<Node>() {

			public int compare(Node o1, Node o2) {
				return new Double(o1.getMeasure()).compareTo(o2.getMeasure());
			}
		};
	}
}
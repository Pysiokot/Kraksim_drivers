package pl.edu.agh.cs.kraksim.routing;

import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.core.City;
import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.core.Node;
import pl.edu.agh.cs.kraksim.iface.sim.Route;
import pl.edu.agh.cs.kraksim.main.drivers.DriverZones;

import java.util.*;
import java.util.Map.Entry;

public class StaticRouter extends Router {
	private static final Logger LOGGER = Logger.getLogger(StaticRouter.class);

	private final City city;
	// source -> (target -> route)
	private final Map<Link, Map<Node, DijkstraRoute>> routes = new HashMap<>();

	public StaticRouter(City city) {
		this.city = city;
	}
	
	public Route getRouteIncludingZone(Link sourceLink, Node targetNode, DriverZones zones) throws NoRouteException {
		Node sourceNode = sourceLink.getBeginning();
		//    RoutesMap rm = new SparseRoutesMap();
		//
		//    Dijkstra dj = new Dijkstra( rm );
		//    dj.execute( sourceGateway, targetGateway );
		//
		//    System.err.println( targetGateway );
		//    Node n = dj.getPredecessor( targetGateway );
		//    while ( n != null ) {
		//      System.err.println( n );
		//
		//      n = dj.getPredecessor( n );
		//    }

		validateArgument(sourceLink, targetNode, sourceNode);

		Map<Node, DijkstraRoute> sourceRoutes = routes.get(sourceLink);
		if (sourceRoutes == null) {
			// obliczanie tras gdy pierwszy raz jest potrzebna trasa z danego wezla

			Map<Link, List<Link>> routeMap = dijkstra(sourceLink, zones);
			sourceRoutes = new HashMap<>(routeMap.size());

			for (Entry<Link, List<Link>> entry : routeMap.entrySet()) {
				Link targetLink = entry.getKey();
				assert targetLink != null;
				sourceRoutes.put(targetLink.getEnd(), new DijkstraRoute(entry.getValue()));
			}
			//TODO
			routes.put(sourceLink, sourceRoutes);

			logDebugInfo();
		}

		DijkstraRoute route = sourceRoutes.get(targetNode);
		if (route == null) {
			throw new NoRouteException("from " + sourceNode.getId() + " to " + targetNode.getId());
		}

		assert route.getSource() == sourceNode;
		assert route.getTarget() == targetNode;

		return route;
	}

	@Override
	public Route getRoute(Link sourceLink, Node targetNode) throws NoRouteException {
		return getRouteIncludingZone(sourceLink, targetNode, null);
	}

	public double getLinkDistance(Link link){
		return link.getLength();
	}

	private Map<Link, List<Link>> dijkstra(Link s, DriverZones zones) {
		LOGGER.trace("START\n" + s);

		final int setSize = city.linkCount();

		Map<Link, DijkstraDistance> dMap = new HashMap<>(setSize);
		Map<Link, Link> prevMap = new HashMap<>(setSize);

		Set<Link> setQ = new HashSet<>(setSize);
		Set<Link> setS = new HashSet<>(setSize);

		for (Iterator<Link> iter = city.linkIterator(); iter.hasNext(); ) {
			Link v = iter.next();
			if (v != null) {
				dMap.put(v, new DijkstraDistance());
				setQ.add(v);
			}
		}
		dMap.get(s).distance = 0.0;

		while (!setQ.isEmpty()) {
			double min = Double.MAX_VALUE;
			Link u = null;

			// szukanie najmniejszego w setQ

			for (Link x : setQ) {
				assert x != null;
				if (dMap.get(x).distance <= min) {
					min = dMap.get(x).distance;
					u = x;
				}
			}
			assert u != null;
			// u jest najmniejszy

			setQ.remove(u);

			setS.add(u);

			// aktualizacja wezłów
			Iterator<Link> iter = u.reachableLinkIterator();
			while (iter.hasNext()) {
				Link v = iter.next();
				if (dMap.get(v).distance > dMap.get(u).distance + v.getLength() && canEnterZone(v, zones)) {
					dMap.get(v).distance = dMap.get(u).distance + v.getLength();
					prevMap.put(v, u);
				}
			}
		}

		return generateRoutes(dMap, prevMap);
	}

	private void logDebugInfo() {
		if (!LOGGER.isDebugEnabled()) {
			return;
		}

		for (Entry<Link, Map<Node, DijkstraRoute>> element : routes.entrySet()) {
			if (element != null) {
				LOGGER.info("\nSOURCE LINK = " + element.getKey().getId());
				for (Entry<Node, DijkstraRoute> route : element.getValue().entrySet()) {
					LOGGER.info("-> " + route.getKey().getId() + " = " + route.getValue());
				}
			} else {
				LOGGER.info("LINK and ROUTE NULL");
			}
		}
	}

	private static Map<Link, List<Link>> generateRoutes(Map<Link, DijkstraDistance> dMap, Map<Link, Link> prevMap) {
		LOGGER.trace("START\n" + dMap + '\n' + prevMap);
		Map<Link, List<Link>> routeMap = new HashMap<>(dMap.size());

		for (Entry<Link, DijkstraDistance> entry : dMap.entrySet()) {
			if (entry.getValue().distance < Double.MAX_VALUE) {
				// jest droga
				routeMap.put(entry.getKey(), generateRoute(prevMap, entry.getKey()));
			}
		}

		LOGGER.trace("END\n " + routeMap);
		return routeMap;
	}
}

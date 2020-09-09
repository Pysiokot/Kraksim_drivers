package pl.edu.agh.cs.kraksim.routing;

import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.core.City;
import pl.edu.agh.cs.kraksim.core.Gateway;
import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.core.Node;
import pl.edu.agh.cs.kraksim.iface.sim.Route;
import pl.edu.agh.cs.kraksim.main.drivers.DriverZones;

import java.util.*;
import java.util.Map.Entry;

public class TimeBasedRouter extends Router {
	private static final Logger LOGGER = Logger.getLogger(TimeBasedRouter.class);

	private final City city;
	private final ITimeTable timeTable;

	// Todo:IMPORTANT, work on this
	public TimeBasedRouter(City city, ITimeTable timeTable) {
		this.city = city;
		this.timeTable = timeTable;
	}

	@Override
	public Route getRouteIncludingZone(Link sourceLink, Node targetNode, DriverZones allowedZones) throws NoRouteException {
		Node sourceNode = sourceLink.getBeginning();

		validateArgument(sourceLink, targetNode, sourceNode);

		List<Link> routeList = dijkstra(sourceLink, (Gateway) targetNode, allowedZones);

		if (routeList == null) {
			throw new NoRouteException("from " + sourceNode.getId() + " to " + targetNode.getId());
		}

		return new DijkstraRoute(routeList);
	}

	@Override
	public Route getRoute(Link sourceLink, Node targetNode) throws NoRouteException {
		return getRouteIncludingZone(sourceLink, targetNode, null);
	}

	public double getLinkDistance(Link link){
		return timeTable.getTime(link);
	}

	private List<Link> dijkstra(Link sourceLink, Gateway targetNode, DriverZones allowedZones) {
		LOGGER.trace("Dijkstra from " + sourceLink + " to " + targetNode + '\n');

		assert timeTable != null;
		assert sourceLink != null;
		assert targetNode != null;
		assert city != null;

		Link currentlyProcessedLink = sourceLink;
		double distanceSoFar = 0.;
		Map<Link, DijkstraDistance> notReachedLinks = new HashMap<>();
		Map<Link, DijkstraDistance> reachedLinks = new HashMap<>();
		Map<Link, Link> pathRecovery = new HashMap<>();

		// initialisation of set of not reached links
		Link temp = null;
		for (Iterator<Link> linkIter = city.linkIterator(); linkIter.hasNext(); ) {
			temp = linkIter.next();
			if (temp.getId().equals(sourceLink.getId())) {
				continue;
			}

			notReachedLinks.put(temp, new DijkstraDistance());
		}
		reachedLinks.put(currentlyProcessedLink, new DijkstraDistance(0));

		// processing the closest link 
		while (currentlyProcessedLink != null) {
			// maybe it ends in the desired gateway?
			if (currentlyProcessedLink.getEnd().getId().equals(targetNode.getId())) {
				List<Link> result = generateRoute(pathRecovery, currentlyProcessedLink);
				notReachedLinks.clear();
				pathRecovery.clear();
				return result;
			}

			// if not, update the closest ways from it to each of the links it reaches 
			for (Iterator<Link> linkIter = currentlyProcessedLink.reachableLinkIterator(); linkIter.hasNext(); ) {
				temp = linkIter.next();
				if (reachedLinks.containsKey(temp)) {
					continue;
				}
				double distanceFromCurrentLink = distanceSoFar + timeTable.getTime(temp);
				if (notReachedLinks.get(temp).distance > distanceFromCurrentLink && canEnterZone(temp, allowedZones)) {
					notReachedLinks.get(temp).distance = distanceFromCurrentLink;
					pathRecovery.put(temp, currentlyProcessedLink);
				}
			}

			// now, let's search for the next nearest link
			double minDistance = Double.MAX_VALUE;
			Link nearestLink = null;
			for (Entry<Link, DijkstraDistance> entry : notReachedLinks.entrySet()) {
				if (entry.getValue().distance < minDistance) {
					minDistance = entry.getValue().distance;
					nearestLink = entry.getKey();
				}
			}

			currentlyProcessedLink = nearestLink;
			notReachedLinks.remove(currentlyProcessedLink);
			reachedLinks.put(currentlyProcessedLink, new DijkstraDistance(minDistance));
			distanceSoFar = minDistance;
		}

		// if nothing found, return null
		return null;
	}
}

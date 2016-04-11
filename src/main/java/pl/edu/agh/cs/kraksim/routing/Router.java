package pl.edu.agh.cs.kraksim.routing;

import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.core.Node;
import pl.edu.agh.cs.kraksim.iface.sim.Route;
import pl.edu.agh.cs.kraksim.main.drivers.DriverZones;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class Router {
	public abstract Route getRoute(Link sourceLink, Node destGateway) throws NoRouteException;

	public abstract Route getRouteIncludingZone(Link sourceLink, Node destGateway, DriverZones zones) throws NoRouteException;

	protected void validateArgument(Link sourceLink, Node targetNode, Node sourceNode) {
		if (sourceNode == null) {
			throw new IllegalArgumentException("null source");
		}
		if (targetNode == null) {
			throw new IllegalArgumentException("null target");
		}

		assert sourceLink != null;
		assert sourceLink.getBeginning() == sourceNode;
	}

	protected static List<Link> generateRoute(Map<Link, Link> prevMap, Link target) {
		List<Link> result = new LinkedList<>();
		result.add(0, target);
		for (Link prev = prevMap.get(target); prev != null; prev = prevMap.get(prev)) {
			result.add(0, prev);
		}
		return result;
	}

	protected static boolean canEnterZone(Link link, DriverZones allowedZones) {
		//if allowedZones is null it meant we don't include zone info in routing
		return allowedZones == null || allowedZones.contains(link.getZoneName());
	}
}

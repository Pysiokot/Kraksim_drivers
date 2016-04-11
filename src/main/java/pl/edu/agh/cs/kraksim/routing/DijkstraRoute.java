package pl.edu.agh.cs.kraksim.routing;

import pl.edu.agh.cs.kraksim.core.Gateway;
import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.iface.sim.Route;

import java.util.List;
import java.util.ListIterator;

final class DijkstraRoute implements Route {
	private final List<Link> route;

	DijkstraRoute(List<Link> route) {
		this.route = route;
	}

	public Gateway getSource() {
		return (Gateway) route.get(0).getBeginning();
	}

	public Gateway getTarget() {
		return (Gateway) route.get(route.size() - 1).getEnd();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getSource().getId());

		for (Link lnk : route) {
			sb.append('-').append(lnk.getEnd().getId());
		}
		return sb.toString();
		//      return getSource().getId() + " -> " + route.get( route.size() - 1 ).getEnd().getId();//getTarget().getId();
	}

	public ListIterator<Link> linkIterator() {
		return route.listIterator();
	}
}

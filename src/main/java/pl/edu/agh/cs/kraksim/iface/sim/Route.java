package pl.edu.agh.cs.kraksim.iface.sim;

import pl.edu.agh.cs.kraksim.core.Gateway;
import pl.edu.agh.cs.kraksim.core.Link;

import java.util.ListIterator;

public interface Route {
	Gateway getSource();

	Gateway getTarget();

	ListIterator<Link> linkIterator();
}

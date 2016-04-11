package pl.edu.agh.cs.kraksim.routing;

import pl.edu.agh.cs.kraksim.core.Link;

public interface ITimeTable {
	double getTime(Link v);

	double getLinkTime(Link v);
}

package pl.edu.agh.cs.kraksim.real_extended;

import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.core.Intersection;
import pl.edu.agh.cs.kraksim.core.Link;

import java.util.Iterator;

class IntersectionRealExt extends NodeRealExt {
	private static final Logger LOGGER = Logger.getLogger(IntersectionRealExt.class);
	private final Intersection intersection;

	IntersectionRealExt(Intersection intersection, RealEView ev) {
		super(ev);
		LOGGER.trace("Constructing.");
		this.intersection = intersection;
	}

	void findApproachingCars() {
		LOGGER.trace(intersection.getId());
		for (Iterator<Link> iter = intersection.inboundLinkIterator(); iter.hasNext(); ) {
			ev.ext(iter.next()).findApproachingCars();
		}
	}

	public void blockInboundLinks() {
		for (Iterator<Link> iter = intersection.inboundLinkIterator(); iter.hasNext(); ) {
			ev.ext(iter.next()).block();
		}
	}

	public void unblockInboundLinks() {
		for (Iterator<Link> iter = intersection.inboundLinkIterator(); iter.hasNext(); ) {
			ev.ext(iter.next()).unblock();
		}
	}
}

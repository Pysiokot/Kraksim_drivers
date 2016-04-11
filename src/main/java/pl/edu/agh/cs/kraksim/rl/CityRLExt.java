package pl.edu.agh.cs.kraksim.rl;

import pl.edu.agh.cs.kraksim.core.City;
import pl.edu.agh.cs.kraksim.core.Lane;
import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.core.PostCreateOp;
import pl.edu.agh.cs.kraksim.core.exceptions.ExtensionCreationException;
import pl.edu.agh.cs.kraksim.iface.eval.CityEvalIface;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class CityRLExt implements CityEvalIface, PostCreateOp {
	private final City city;
	private final RLEView ev;
	private final RLParams params;

	private final List<LaneRLExt> toGatewayExts;
	private final List<LaneRLExt> toIsectExts;
	private int halveAfter;

	CityRLExt(City city, RLEView ev, RLParams params) {
		this.city = city;
		this.ev = ev;
		this.params = params;
		toGatewayExts = new ArrayList<>();
		toIsectExts = new ArrayList<>();

		if (params.halvePeriod > 0) {
			halveAfter = params.halvePeriod;
		}
	}

	public void turnEnded() {
		if ((params.halvePeriod > 0) && (--halveAfter == 0)) {
			for (LaneRLExt l : toIsectExts) {
				l.halveCounters();
			}

			halveAfter = params.halvePeriod;
		}

		for (LaneRLExt l : toGatewayExts) {
			l.updateStatsToGateway();
		}
		for (LaneRLExt l : toIsectExts) {
			l.updateStatsToIsect();
		}
		for (LaneRLExt l : toIsectExts) {
			l.updateValues1();
		}
		for (LaneRLExt l : toIsectExts) {
			l.updateValues2();
		}
		for (LaneRLExt l : toIsectExts) {
			l.makeEvaluation();
		}
	}

	public void postCreate() throws ExtensionCreationException {
		for (Iterator<Link> linkIter = city.linkIterator(); linkIter.hasNext(); ) {
			Link link = linkIter.next();
			boolean toGateway = link.getEnd().isGateway();

			for (Iterator<Lane> laneIter = link.laneIterator(); laneIter.hasNext(); ) {
				Lane lane = laneIter.next();
				if (toGateway) {
					toGatewayExts.add(ev.ext(lane));
				} else {
					toIsectExts.add(ev.ext(lane));
				}
			} // for laneIter
		}// for linkIter
	}
}

package pl.edu.agh.cs.kraksim.rlcd;

import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.core.PostCreateOp;
import pl.edu.agh.cs.kraksim.core.City;
import pl.edu.agh.cs.kraksim.core.Lane;
import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.core.exceptions.ExtensionCreationException;
import pl.edu.agh.cs.kraksim.iface.eval.CityEvalIface;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CityRLCDExt implements CityEvalIface, PostCreateOp {
	private static final Logger LOGGER = Logger.getLogger(CityRLCDExt.class);
	private final City city;
	private final RLCDEView ev;
	private final RLCDParams params;
	private final List<LaneRLCDExt> toGatewayExts;
	private final List<LaneRLCDExt> toIsectExts;
	private int halveAfter;

	CityRLCDExt(City city, RLCDEView ev, RLCDParams params) {
		this.city = city;
		this.ev = ev;
		this.params = params;
		toGatewayExts = new ArrayList<>();
		toIsectExts = new ArrayList<>();

		if (params.halvePeriod > 0) {
			halveAfter = params.halvePeriod;
		}

		LOGGER.trace("");
	}

	public void turnEnded() {
		if ((params.halvePeriod > 0) && (--halveAfter == 0)) {
			for (LaneRLCDExt l : toIsectExts) {
				l.halveCounters();
			}

			halveAfter = params.halvePeriod;
		}

		for (LaneRLCDExt l : toGatewayExts) {
			l.updateStatsToGateway();
		}
		for (LaneRLCDExt l : toIsectExts) {
			l.updateStatsToIsect();
		}
		for (LaneRLCDExt l : toIsectExts) {
			l.updateValues1();
		}
		for (LaneRLCDExt l : toIsectExts) {
			l.updateValues2();
		}
		for (LaneRLCDExt l : toIsectExts) {
			l.changeModel();
		}
		for (LaneRLCDExt l : toIsectExts) {
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

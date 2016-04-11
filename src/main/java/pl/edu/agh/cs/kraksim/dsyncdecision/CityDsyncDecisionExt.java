package pl.edu.agh.cs.kraksim.dsyncdecision;

import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.core.City;
import pl.edu.agh.cs.kraksim.core.Intersection;
import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.iface.Clock;
import pl.edu.agh.cs.kraksim.iface.decision.CityDecisionIface;
import pl.edu.agh.cs.kraksim.ministat.MiniStatEView;
import pl.edu.agh.cs.kraksim.optapo.algo.AgentFarm;
import pl.edu.agh.cs.kraksim.optapo.algo.agent.Agent;
import pl.edu.agh.cs.kraksim.optapo.algo.agent.AgentInfo;
import pl.edu.agh.cs.kraksim.optapo.algo.agent.Direction;

import java.util.Iterator;

class CityDsyncDecisionExt implements CityDecisionIface {
	private static final Logger LOGGER = Logger.getLogger(CityDsyncDecisionExt.class);
	private final City city;
	private final DSyncDecisionEView ev;
	private final AgentFarm farm;
	private final MiniStatEView statView;
	private final Clock clock;
	private boolean dynamic = false;

	CityDsyncDecisionExt(City city, Clock clock, DSyncDecisionEView ev, MiniStatEView statView, boolean dynamic) {
		this.city = city;
		this.clock = clock;
		this.ev = ev;
		this.statView = statView;
		this.dynamic = dynamic;
		farm = new AgentFarm();
	}

	public void initialize() {
		for (Iterator<Intersection> iter = city.intersectionIterator(); iter.hasNext(); ) {
			Intersection intersection = iter.next();
			IntersectionDsyncDecisionExt isectView = ev.ext(intersection);

			Agent agent = createAgentFor(intersection);
			isectView.setAgent(agent);
		}

		for (Iterator<Intersection> iter = city.intersectionIterator(); iter.hasNext(); ) {
			ev.ext(iter.next()).initialize();
		}

		farm.init();
	}

	public void turnEnded() {
		// run simulation.. and mediation
		for (Iterator<Intersection> iter = city.intersectionIterator(); iter.hasNext(); ) {
			Intersection intersection = iter.next();
			//      IntersectionDsyncDecisionExt isectView = ev.ext( intersection );

			updateAgentFor(intersection);
			//      Collection<AgentInfo> neiList = isectView.getAgent().getNeighborList();
		}

		if ((clock.getTurn() % 120) == 0) {
			runSimulation();
		}

		LOGGER.trace("Changing Lights");

		for (Iterator<Intersection> iter = city.intersectionIterator(); iter.hasNext(); ) {
			ev.ext(iter.next()).makeDecision();
		}
	}

	private void updateAgentFor(Intersection intersection) {
		IntersectionDsyncDecisionExt isectView = ev.ext(intersection);
		Agent agent = isectView.getAgent();

		for (Iterator<Link> linkIter = intersection.inboundLinkIterator(); linkIter.hasNext(); ) {
			Link link = linkIter.next();
			int incoming = getIncoming(link);
			String nodeName = link.getBeginning().getId();

			AgentInfo ag = agent.getNeighbor(nodeName);
			ag.setIncoming(incoming);
			//new AgentInfo( nodeName, Direction.valueOf( link.getDirection() ), 0 );
			//agent.addNeighbor( ag );
		}
	}

	private int getIncoming(Link link) {
		//    for (Iterator<Lane> iter = link.laneIterator(); iter.hasNext();){
		//      Lane lane = iter.next();
		//
		//       laneStat = statView.ext( lane );
		//
		//      ret += laneEval.getEvaluation();
		//    }

		return statView.ext(link).getCarCount();
	}

	private Agent createAgentFor(Intersection intersection) {
		LOGGER.trace("new Agent for: " + intersection.getId());
		Agent agent = new Agent(farm, intersection.getId(), Direction.NS);

		for (Iterator<Link> linkIter = intersection.inboundLinkIterator(); linkIter.hasNext(); ) {
			Link link = linkIter.next();
			String nodeName = link.getBeginning().getId();

			AgentInfo ag = new AgentInfo(nodeName, Direction.valueOf(link.getDirection()), 0);
			agent.addNeighbor(ag);
		}

		farm.addAgent(agent);

		return agent;
	}

	private void runSimulation() {
		if (dynamic) {
			try {
				farm.cycle();
			} catch (InterruptedException e) {
				LOGGER.error("InterruptedException", e);
			}
		}
	}
}

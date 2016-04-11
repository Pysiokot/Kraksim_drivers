package pl.edu.agh.cs.kraksim.core;

import pl.edu.agh.cs.kraksim.AssumptionNotSatisfiedException;
import pl.edu.agh.cs.kraksim.core.exceptions.LinkAttachmentException;
import pl.edu.agh.cs.kraksim.core.visitors.ElementVisitor;
import pl.edu.agh.cs.kraksim.core.visitors.VisitingException;
import pl.edu.agh.cs.kraksim.sna.centrality.KmeansClustering;
import pl.edu.agh.cs.kraksim.sna.centrality.OptimalizationInfo;

import java.awt.geom.Point2D;
import java.util.*;

public class Intersection extends Node {

	/**
	 * list of (directed) links ending in the intersection
	 */
	private final List<Link> inboundLinks;

	/**
	 * list of (directed) links beginning in the intersection
	 */
	private final List<Link> outboundLinks;

	/**
	 * light plan for intersection
	 */
	// private final ArrayList<Phase> trafficLightsSchedule;
	// private String mainPlanName = "";
	public final List<Phase> phases;
	public final Map<String, List<PhaseTiming>> timingPlans;

	//Dla organizacji ruchu
	public List<OptimalizationInfo> optimalisationInfos;
	public OptimalizationInfo selfOptimalisationInfo;
	public boolean selfCalculate = false;

	public Intersection(Core core, String id, Point2D point) {
		super(core, id, point);
		inboundLinks = new ArrayList<>();
		outboundLinks = new ArrayList<>();
		phases = new LinkedList<>();
		timingPlans = new HashMap<>();
		//ja pierdole co ja tu za g�wno odpierdalam!
		optimalisationInfos = new ArrayList<>();
	}

	@Override
	public Iterator<Phase> trafficLightPhaseIterator() {
		return phases.iterator();
	}

	public List<Phase> trafficLightPhases() {
		return phases;
	}

	@Override
	public void attachInboundLink(Link link) throws LinkAttachmentException {
		inboundLinks.add(link);
	}

	@Override
	public void detachInboundLink(Link link) {
		if (!inboundLinks.remove(link)) {
			throw new AssumptionNotSatisfiedException("trying to detach link, which has never been attached; intersection: " + id + "; link street: " + link.getStreetName());
		}
	}

	@Override
	void attachOutboundLink(Link link) {
		outboundLinks.add(link);
	}

	@Override
	void detachOutboundLink(Link link) {
		if (!outboundLinks.remove(link)) {
			throw new AssumptionNotSatisfiedException("trying to detach link, which has never been attached; intersection: " + id + "; link street: " + link.getStreetName());
		}
	}

	@Override
	public Iterator<Link> inboundLinkIterator() {
		return inboundLinks.iterator();
	}

	@Override
	public Iterator<Link> outboundLinkIterator() {
		return outboundLinks.iterator();
	}

	@Override
	public boolean isIntersection() {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Class getExtensionClass(Module module) {
		return module.getExtensionClasses().getIntersectionClass();
	}

	/**
	 * Should not be used directly. Use City.applyElementVisitor()
	 */
	@Override
	void applyElementVisitor(ElementVisitor visitor) throws VisitingException {
		visitor.visit(this);
		for (Link link : inboundLinks) {
			link.applyElementVisitor(visitor);
		}
	}

	/* used in exception messages */
	@Override
	public String toString() {
		return "<intersection " + id + '>' + phases;
	}

	@Override
	public void addTrafficLightsPhases(List<Phase> schedule) {
		if (!schedule.isEmpty()) {
			phases.addAll(schedule);
		}
	}

	public List<PhaseTiming> getTimingPlanFor(String direction) {
		return timingPlans.get(direction);
	}

	public void addTimingPlanFor(List<PhaseTiming> timingPlan, String direction) {
		// this.timingPlan = timingPlan;
		timingPlans.put(direction, timingPlan);
	}

	//Rozszerzenie do optymalizacji ruchu
	public void optimalizeLights() {
		//Ustalanie �wiate� po wymianie informaji i przed
		if (!selfCalculate) {
			selfCalculate = true;

			Link maxLink = getMaxLink();
			if (maxLink == null) {
				return;
			}
			selfOptimalisationInfo = new OptimalizationInfo();

			//Map<Link, Integer> toChange = new LinkedHashMap<Link, Integer>();
			//toChange.put(maxLink, 10);
			//selfOptimalisationInfo.setGreenLightChanges(toChange);
			selfOptimalisationInfo.setLink(maxLink);
			selfOptimalisationInfo.setChange(10);
			selfOptimalisationInfo.setIntersection(this);
			exchangeOptimalizationInfo();
		} else {
			for (OptimalizationInfo optInfo : optimalisationInfos) {
				if (areNeighbours(this, optInfo.getIntersection())) {
					optimalisationNegotiation(selfOptimalisationInfo, optInfo);
				}
			}
			optimalisationInfos.clear();
			selfCalculate = false;
			propagateOptInfoDown();
		}
	}

	public void propagateOptInfoDown() {
		for (Node n : KmeansClustering.findMyCluster(this)) {
			if (n != this) {
				((Intersection) n).addOptimalisationInfo(selfOptimalisationInfo);
			}
		}
	}

	public void minorLightOptimalization() {
		if (KmeansClustering.findMyMainNode(this) == this) {
			return;
		}
		int far = howFarFromMain();
		if (far == -1) {
			return;
		}

		Link maxLink = getMaxLink();
		if (maxLink == null) {
			return;
		}
		selfOptimalisationInfo = new OptimalizationInfo();

		selfOptimalisationInfo.setLink(maxLink);
		selfOptimalisationInfo.setChange(optimalisationInfos.get(0).getChange() / (far * 2));
		selfOptimalisationInfo.setIntersection(this);
	}

	public int howFarFromMain() {
		Node main = KmeansClustering.findMyMainNode(this);
		if (areNeighbours(this, (Intersection) main)) {
			return 1;
		}
		for (Link lane : outboundLinks) {
			Intersection neighbour;
			if (lane.getBeginning() == this) {
				if (lane.getEnd().isGateway()) {
					continue;
				}
				neighbour = (Intersection) lane.getEnd();
			} else {
				if (lane.getBeginning().isGateway()) {
					continue;
				}
				neighbour = (Intersection) lane.getBeginning();
			}
			if (areNeighbours(neighbour, (Intersection) main)) {
				return 2;
			}
		}

		return -1;
	}

	public void exchangeOptimalizationInfo() {
		for (Node n : KmeansClustering.currentClustering.keySet()) {
			if (n != this) {
				((Intersection) n).addOptimalisationInfo(selfOptimalisationInfo);
			}
		}
	}

	public void optimalisationNegotiation(OptimalizationInfo self, OptimalizationInfo foerign) {
		selfOptimalisationInfo.setChange(self.getChange() + (foerign.getChange() / 5));
	}

	public static boolean areNeighbours(Intersection i1, Intersection i2) {
		for (Link lane : i1.outboundLinks) {
			if (lane.getBeginning() == i2 || lane.getEnd() == i2) {
				return true;
			}
		}
		return false;
	}

	public void addOptimalisationInfo(OptimalizationInfo info) {
		optimalisationInfos.add(info);
	}

	public void sendInfoToKlusterNeighbours(OptimalizationInfo info) {
		Set<Node> myCluster = KmeansClustering.findMyCluster(this);
		if (myCluster == null) {
			return;
		}

		for (Node n : myCluster) {
			if (n != this) {
				((Intersection) n).addOptimalisationInfo(selfOptimalisationInfo);
			}
		}
	}

	private Link getMaxLink() {
		Link maxLink = null;
		double maxCars = -1;

		Iterator<Link> links = inboundLinkIterator();
		while (links.hasNext()) {
			Link link = links.next();
			if (link.getLoad() > maxCars) {
				maxCars = link.getLoad();
				maxLink = link;
			}
		}
		return maxLink;
	}
}

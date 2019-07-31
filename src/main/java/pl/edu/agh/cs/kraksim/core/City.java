package pl.edu.agh.cs.kraksim.core;

import com.google.common.collect.Iterators;
import pl.edu.agh.cs.kraksim.core.exceptions.DuplicateIdentifierException;
import pl.edu.agh.cs.kraksim.core.exceptions.LinkAttachmentException;
import pl.edu.agh.cs.kraksim.core.visitors.ElementVisitor;
import pl.edu.agh.cs.kraksim.core.visitors.VisitingException;
import pl.edu.agh.cs.kraksim.parser.RoadInfo;
import pl.edu.agh.cs.kraksim.real_extended.BlockedCellsInfo;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * City - an entry element to the city topology
 */
public class City extends Element {

	/**
	 * maps gateway ids' to nodes
	 */
	private final Map<String, Gateway> gatewayMap;
	/**
	 * maps gateway ids' to nodes
	 */
	private final Map<String, Intersection> intersectionMap;
	private final Map<String, Link> linkMap;

	public City(Core core) {
		super(core);
		gatewayMap = new HashMap<>();
		intersectionMap = new HashMap<>();
		linkMap = new HashMap<>();
	}

	/**
	 * Gateway Factory.
	 *
	 * @throws pl.edu.agh.cs.kraksim.core.exceptions.DuplicateIdentifierException if node with specified id already exists.
	 */
	public Gateway createGateway(String id, Point2D point) throws DuplicateIdentifierException {
		if (gatewayMap.containsKey(id) || intersectionMap.containsKey(id)) {
			throw new DuplicateIdentifierException("node with id " + id + " already exists");
		}

		Gateway g = new Gateway(core, id, point);
		gatewayMap.put(id, g);

		return g;
	}

	/**
	 * Intersection Factory.
	 *
	 * @throws DuplicateIdentifierException if node with specified id already exists.
	 */
	public Intersection createIntersection(String id, Point2D point) throws DuplicateIdentifierException {
		if (gatewayMap.containsKey(id) || intersectionMap.containsKey(id)) {
			throw new DuplicateIdentifierException("node with id " + id + " already exists");
		}

		Intersection is = new Intersection(core, id, point);
		intersectionMap.put(id, is);

		return is;
	}

	/**
	 * Link Factory. streetName does not have to be unique in general.
	 * (Directed) links connecting two nodes should have unique streetNames.
	 * <p/>
	 * Lanes length's must decrease from inside to outside. Throws
	 * IllegalArgumentException otherwise.
	 * <p/>
	 * Arrays of lane lengths are indexed from 0 (the lane nearest to the main
	 * lane).
	 * @throws LinkAttachmentException 
	 * @throws IllegalArgumentException 
	 *
	 * @throws DuplicateIdentifierException                                        if link with specified id already exists.
	 * @throws pl.edu.agh.cs.kraksim.core.exceptions.LinkAttachmentException. See Gateway.attach*Link().
	 */
	public Link createLink(RoadInfo roadInfo, int[] leftLaneLens, int mainLaneLen, int numberOfLanes, int[] rightLaneLens, Map<String, Map<Integer, List<BlockedCellsInfo>>> linkBlockedCellsInfo)
			throws DuplicateIdentifierException, IllegalArgumentException, LinkAttachmentException {
		if (linkMap.containsKey(roadInfo.getLinkId())) {
			throw new DuplicateIdentifierException(String.format("link with id %s already exists", roadInfo.getLinkId()));
		}

		Link link = new Link(core, roadInfo, leftLaneLens, mainLaneLen, numberOfLanes, rightLaneLens, linkBlockedCellsInfo);

		// If this method throws an exception, no cleanup is needed.
		roadInfo.getFrom().attachOutboundLink(link);
		try {
			roadInfo.getTo().attachInboundLink(link); // If an exception is thrown, we must detach, what we have just attached.
		} catch (LinkAttachmentException e) {
			roadInfo.getFrom().detachOutboundLink(link);
			throw e;
		}

		linkMap.put(roadInfo.getLinkId(), link);

		return link;
	}

	/* returns null if not found */
	public Node findNode(String id) {
		Node node = gatewayMap.get(id);
		if (node != null) {
			return node;
		} else {
			return intersectionMap.get(id);
		}
	}

	public int nodeCount() {
		return gatewayMap.size() + intersectionMap.size();
	}

	public Iterator<Node> nodeIterator() {
		return Iterators.concat(gatewayIterator(), intersectionIterator());
	}

	public int gatewayCount() {
		return gatewayMap.size();
	}

	public Iterator<Gateway> gatewayIterator() {
		return gatewayMap.values().iterator();
	}

	public int intersectionCount() {
		return intersectionMap.size();
	}

	public Iterator<Intersection> intersectionIterator() {
		return intersectionMap.values().iterator();
	}

	/* returns null if not found */
	public Link findLink(String id) {
		return linkMap.get(id);
	}

	public int linkCount() {
		return linkMap.size();
	}

	public Iterator<Link> linkIterator() {
		return linkMap.values().iterator();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Class getExtensionClass(Module module) {
		return module.getExtensionClasses().getCityClass();
	}

	/**
	 * Visits all elements (that is objects of Element class or subclass) and
	 * calls a visitor method depending of an element type.
	 * <p/>
	 * See ElementVisitor and VisitingException.
	 */
	public void applyElementVisitor(ElementVisitor visitor) throws VisitingException {
		visitor.visit(this);

		for (Gateway gateway : gatewayMap.values()) {
			gateway.applyElementVisitor(visitor);
		}
		for (Intersection intersection : intersectionMap.values()) {
			intersection.applyElementVisitor(visitor);
		}
	}
}

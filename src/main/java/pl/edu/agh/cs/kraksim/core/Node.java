package pl.edu.agh.cs.kraksim.core;

import pl.edu.agh.cs.kraksim.core.exceptions.LinkAttachmentException;
import pl.edu.agh.cs.kraksim.core.visitors.ElementVisitor;
import pl.edu.agh.cs.kraksim.core.visitors.VisitingException;

import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.List;

/* Base class for gateway and intersection. */
public abstract class Node extends Element {

	protected final String id;

	/*
	 * coordinates of the node.
	 *
	 * Only for informative purposes (to allow visualisation). Link and lane
	 * lengths are not based on these coords.
	 */
	protected final Point2D point;

	private double measure;
	private int subGraphNumber;

	protected Node(Core core, String id, Point2D point) {
		super(core);
		this.id = id;
		this.point = point;
	}

	public String getId() {
		return id;
	}

	public Point2D getPoint() {
		return point;
	}

	public abstract void attachInboundLink(Link link) throws LinkAttachmentException;

	public abstract void detachInboundLink(Link link);

	abstract void attachOutboundLink(Link link) throws LinkAttachmentException;

	abstract void detachOutboundLink(Link link);

	public abstract Iterator<Link> inboundLinkIterator();

	public abstract Iterator<Link> outboundLinkIterator();

	public abstract Iterator<Phase> trafficLightPhaseIterator();

	public abstract void addTrafficLightsPhases(List<Phase> schedule);

	public boolean isGateway() {
		return false;
	}

	public boolean isIntersection() {
		return false;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		if (getClass() == o.getClass()) {
			return false;
		}
		Node n = (Node) o;
		return n.id.equals(n.id);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Class getExtensionClass(Module module) {
		return module.getExtensionClasses().getNodeClass();
	}

	/* Should not be used directly. Use City.applyElementVisitor() */
	abstract void applyElementVisitor(ElementVisitor visitor) throws VisitingException;

	public double getMeasure() {
		return measure;
	}

	public void setMeasure(double measure) {
		this.measure = measure;
	}

	public int getSubGraphNumber() {
		return subGraphNumber;
	}

	public void setSubGraphNumber(int subGraphNumber) {
		this.subGraphNumber = subGraphNumber;
	}
}

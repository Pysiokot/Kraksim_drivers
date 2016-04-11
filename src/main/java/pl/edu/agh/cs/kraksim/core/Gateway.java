package pl.edu.agh.cs.kraksim.core;

import com.google.common.collect.ImmutableList;
import pl.edu.agh.cs.kraksim.AssumptionNotSatisfiedException;
import pl.edu.agh.cs.kraksim.core.exceptions.LinkAttachmentException;
import pl.edu.agh.cs.kraksim.core.visitors.ElementVisitor;
import pl.edu.agh.cs.kraksim.core.visitors.VisitingException;

import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Gateway extends Node {

	/* (directed) link ending in the gateway */
	private Link inboundLink;

	/* (directed) link beginning in the gateway */
	private Link outboundLink;

	Gateway(Core core, String id, Point2D point) {
		super(core, id, point);
	}

	/*
	   * Throws LinkAttachementException if an inbound link has been already attached.
	   */
	@Override
	public void attachInboundLink(Link link) throws LinkAttachmentException {
		if (inboundLink != null) {
			throw new LinkAttachmentException("trying to attach more than one inbound link; gateway: " + id);
		}

		inboundLink = link;
	}

	@Override
	public void detachInboundLink(Link link) {
		if (link != inboundLink) {
			throw new AssumptionNotSatisfiedException("trying to detach link, which has never been attached; gateway: " + id + "; link street: " + link.getStreetName());
		}
		inboundLink = null;
	}

	/*
	   * Throws LinkAttachementException if an outbound link has been already attached.
	   */
	@Override
	void attachOutboundLink(Link link) throws LinkAttachmentException {
		if (outboundLink != null) {
			throw new LinkAttachmentException("trying to attach more than one outbound link; gateway: " + id);
		}

		outboundLink = link;
	}

	@Override
	void detachOutboundLink(Link link) {
		if (link != inboundLink) {
			throw new AssumptionNotSatisfiedException("trying to detach link, which has never been attached; gateway: " + id + "; link street: " + link.getStreetName());
		}
		outboundLink = null;
	}

	public Link getInboundLink() {
		return inboundLink;
	}

	public Link getOutboundLink() {
		return outboundLink;
	}

	@Override
	public Iterator<Link> inboundLinkIterator() {
		if (inboundLink == null) {
			return ImmutableList.<Link>of().iterator();
		} else {
			return ImmutableList.of(inboundLink).iterator();
		}
	}

	@Override
	public Iterator<Link> outboundLinkIterator() {
		if (outboundLink == null) {
			return ImmutableList.<Link>of().iterator();
		} else {
			return ImmutableList.of(outboundLink).iterator();
		}
	}

	@Override
	public boolean isGateway() {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Class getExtensionClass(Module module) {
		return module.getExtensionClasses().getGatewayClass();
	}

	/* Should not be used directly. Use City.applyElementVisitor() */
	@Override
	void applyElementVisitor(ElementVisitor visitor) throws VisitingException {
		visitor.visit(this);
		inboundLink.applyElementVisitor(visitor);
	}

	/* used in exception messages */
	@Override
	public String toString() {
		return String.format("<gateway %s>", id);
	}

	@Override
	public Iterator<Phase> trafficLightPhaseIterator() {
		return ImmutableList.<Phase>of().iterator();
	}

	@Override
	public void addTrafficLightsPhases(List<Phase> schedule) {
		// TODO Auto-generated method stub
		
	}
}

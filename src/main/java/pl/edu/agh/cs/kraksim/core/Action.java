package pl.edu.agh.cs.kraksim.core;

/**
 * (Intersection) action.
 * <p/>
 * Assumptions about actions (checked in method Lane.addAction()):
 * <ol>
 * <li> Action can only be assigned to an intersection
 * <li> The end of source link is the beginning of target link
 * <li> Every priority lane must belong to some inbound link
 * (relative to source.getEnd()) other than source
 * <li>Limits of actions for one link<ul><li> there can be at most one action for the whole link (all its lanes)
 * with specified target link
 * <li> if two actions from different links have the same target link,
 * then one of them must be in 'prior to' relation to the other's source lane.
 * </ul></ol>
 * It is not an element subclass (and thus cannot be extended in modules), because
 * we do not believe there is any application for extending an action.
 */
public class Action {

	/* action moves car from source lane... */
	private Lane source;

	public void setSource(Lane source) {
		this.source = source;
	}

	/* ...and puts it on target link (on its main lane) */
	private final Link target;

	/* ...provided that all priority lanes are free */
	private final Lane[] priorLanes;

	/* we make a shallow copy of priorLanes array */
	public Action(Lane source, Link target, Lane[] priorLanes) {
		this.source = source;
		this.target = target;
		this.priorLanes = new Lane[priorLanes.length];
		System.arraycopy(priorLanes, 0, this.priorLanes, 0, priorLanes.length);
	}

	public Lane getSource() {
		return source;
	}

	public Link getTarget() {
		return target;
	}

	public Lane[] getPriorLanes() {
		return priorLanes;
	}

	/* used in exception messages */
	@Override
	public String toString() {
		return "/action from " + source + " to " + target + '/';
	}
}

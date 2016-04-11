package pl.edu.agh.cs.kraksim.iface.decision;

import pl.edu.agh.cs.kraksim.core.Intersection;
import pl.edu.agh.cs.kraksim.core.Phase;
import pl.edu.agh.cs.kraksim.iface.Clock;
import pl.edu.agh.cs.kraksim.iface.block.BlockIView;

public abstract class IntersectionDecisionExt {
	protected enum State {
		INIT, TRANSITION, GREEN
	}

	protected final Intersection intersection;
	protected final BlockIView blockView;
	protected final Clock clock;
	protected final int transitionDuration;

	protected State state;
	protected long stateEndMinTurn;
	protected Phase nextPhase;

	protected IntersectionDecisionExt(Intersection intersection, BlockIView blockView, Clock clock, int transitionDuration) {
		this.intersection = intersection;
		this.blockView = blockView;
		this.clock = clock;
		this.transitionDuration = transitionDuration;
	}

	public void initialize() {
		blockView.ext(intersection).blockInboundLinks();
		state = State.INIT;
		stateEndMinTurn = 0;
	}

	protected void prolongCurrentPhase(final int duration) {
		stateEndMinTurn = clock.getTurn() + duration;
	}

	protected boolean isPhaseFinished() {
		return clock.getTurn() >= stateEndMinTurn;
	}

	protected void changeToGreen(final Phase phase) {
		nextPhase = phase;
		if (transitionDuration > 0) {
			setTransition();
		} else {
			setGreen(phase);
		}
	}

	private void setTransition() {
		state = State.TRANSITION;
		stateEndMinTurn = clock.getTurn() + transitionDuration;
	}

	protected abstract void setGreen(Phase phase);
}

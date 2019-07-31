package pl.edu.agh.cs.kraksim.dsyncdecision;

import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.core.*;
import pl.edu.agh.cs.kraksim.iface.Clock;
import pl.edu.agh.cs.kraksim.iface.block.BlockIView;
import pl.edu.agh.cs.kraksim.iface.decision.IntersectionDecisionExt;
import pl.edu.agh.cs.kraksim.optapo.algo.agent.Agent;

import java.util.Iterator;
import java.util.List;

class IntersectionDsyncDecisionExt extends IntersectionDecisionExt {
	private static final Logger LOGGER = Logger.getLogger(IntersectionDsyncDecisionExt.class);

	//  private final EvalIView    evalView;
	//  private Lane greenLane;
	private Agent agent;

	//  private PhaseTiming        selectedTiming;

	IntersectionDsyncDecisionExt(Intersection intersection,
	                             //      Agent agent,
	                             //      EvalIView evalView,
	                             BlockIView blockView, Clock clock, int transitionDuration) {
		super(intersection, blockView, clock, transitionDuration);
		//    this.agent = agent;
		//    this.evalView = evalView;
	}

	public void setAgent(Agent agent) {
		this.agent = agent;
	}

	void makeDecision() {
		LOGGER.trace(state + " phase:" + nextPhase + ", countdown:" + (stateEndMinTurn - clock.getTurn()));

		switch (state) {
			case INIT:
				initFirstPhase();
				break;

			case TRANSITION:
				setNextPhase();
				break;
			case GREEN:
				if (isPhaseFinished()) {
					switchPhase();
				}
				break;
			default:
				break;
		}
	}

	private void switchPhase() {
		//  final Lane chosenLane = choseLane();
		//  if ( LOGGER.isTraceEnabled() ) {
		//    LOGGER.trace( chosenLane );
		//  }

		//  if ( chosenLane != null ) {
		//  final Phase chosenPhase = getPhaseForLane( chosenLane );
		//    final Phase chosenPhase = getPhaseForDirection( chooseDirection() );
		Phase chosenPhase = getNextPhaseForDirection(chooseDirection());

//    System.err.println( chosenPhase );
		if (chosenPhase != null && !chosenPhase.equals(nextPhase)) {
			blockView.ext(intersection).blockInboundLinks();
			changeToGreen(chosenPhase);
		} else {
			prolongCurrentPhase(10);
		}
	}

	private void setNextPhase() {
		if (isPhaseFinished()) {
			setGreen(nextPhase);
            for (Iterator<Link> iter = intersection.inboundLinkIterator(); iter.hasNext(); ) {
                Link link = iter.next();
                for (Iterator<Lane> laneIter = link.laneIterator(); laneIter.hasNext(); ) {
                    Lane lane = laneIter.next();
					if (blockView.ext(lane).anyEmergencyCarsOnLane()) {
						Lane bestLane = getMostEmergencyLane();
						synchronized (this) {
							if (blockView.ext(bestLane).isBlocked()) {
								for (Iterator<Link> it = intersection.inboundLinkIterator(); it.hasNext(); ) {
									Link link1 = it.next();
									for (Iterator<Lane> laneIt = link1.laneIterator(); laneIt.hasNext(); ) {
										Lane lane1 = laneIt.next();
										blockView.ext(lane1).block();
									}
								}
								Link bestLink = bestLane.getOwner();
								blockView.ext(bestLink).unblock();
								initFirstPhase();
							}
						}
					}
                }
            }
		}
	}

	private void initFirstPhase() {
		//    final Lane chosenLane = choseLane();
		//    if ( LOGGER.isTraceEnabled() ) {
		//      LOGGER.trace( chosenLane );
		//    }

		//    if ( chosenLane != null ) {
		//      final Phase chosenPhase = getPhaseForLane( chosenLane );
		final Phase chosenPhase = getPhaseForDirection(chooseDirection());
		if (chosenPhase != null) {
			nextPhase = chosenPhase;
			changeToGreen(chosenPhase);
		}
		//    }
	}

	private Phase getNextPhaseForDirection(String direction) {
		//    Phase selected  =
		// TODO: remember OFFSET,
		int id = nextPhase.getId();
		int max = intersection.trafficLightPhases().size();
		int newId = (id) % max;
		//    id = newId;
		//    System.out.println( "was " + (id-1) + ", is " + newId );
		Phase selected = intersection.trafficLightPhases().get(newId);

		List<PhaseTiming> timing = intersection.getTimingPlanFor(direction);

		PhaseTiming selectedTiming = null;
		if (timing != null) {
			for (PhaseTiming phaseTiming : timing) {
				// TODO: by name
				if (phaseTiming.getName().equals(selected.getName())) {
					selectedTiming = phaseTiming;
					break;
				}
			}
		}

		if (selectedTiming != null) {
			selected.setDuration(selectedTiming.getDuration());
		} else {
//      System.err.println( ", NULL TIMING; " );
			selected.setDuration(20);
		}

		return selected;
	}

	// TODO: needs refactor
	private Phase getPhaseForDirection(String direction) {
		Phase selected = null;
		List<PhaseTiming> timing = intersection.getTimingPlanFor(direction);
		//    System.out.println( timing );
		for(Phase phase : intersection.trafficLightPhases()){
			//        String dir = phase.getSyncDirection();

			//        if ( direction.equals( dir ) ) {
			// TODO: check timing for this plan
			selected = phase;
			break;
			//        }

		}

		PhaseTiming selectedTiming = null;
		if (timing != null) {
			for (PhaseTiming phaseTiming : timing) {
				if (phaseTiming.getPhaseId() == selected.getId()) {
					selectedTiming = phaseTiming;
					break;
				}
			}
		}

		if (selectedTiming != null) {
			selected.setDuration(selectedTiming.getDuration());
		} else {
			selected.setDuration(20);
		}
		return selected;
	}

	//  private Phase getPhaseForLane(final Lane l) {
	//    Iterator<Phase> it = intersection.trafficLightPhaseIterator();
	//    Phase selected = null;
	//
	//    while ( it.hasNext() ) {
	//      Phase phase = it.next();
	//      String id = l.getOwner().getBeginning().getId();
	//      int laneNum = l.getRelativeNumber();
	//      LightState config = phase.getConfigurationFor( id, laneNum );
	//      if ( config == null ) {
	//        continue;
	//      }
	//      if ( LOGGER.isDebugEnabled() ) {
	//        LOGGER.debug( "getPhase for " + l );
	//      }
	//
	//      if ( config.isGreen() ) {
	//        selected = phase;
	//        break;
	//      }
	//    }
	//
	//    return selected;
	//  }

	protected void setGreen(Phase phase) {
		int duration = 0;
		int counter = 0;

		for (Iterator<Link> iter = intersection.inboundLinkIterator(); iter.hasNext(); ) {
			Link link = iter.next();
			String arm = link.getBeginning().getId();

			for (Iterator<Lane> laneIter = link.laneIterator(); laneIter.hasNext(); ) {
				Lane lane = laneIter.next();
				LightState light = phase.getConfigurationFor(arm, lane.getRelativeNumber());

				if (light.isGreen()) {
					blockView.ext(lane).unblock();
					duration += phase.getGreenDuration();
					//          duration += evalView.ext( lane ).getMinGreenDuration();
					counter++;
				} else {
					blockView.ext(lane).block();
				}
			}
		}

		state = State.GREEN;
		// TODO: LDZ WIELEPASOW!!!
		// Dzielenie przez ilosc zielonych swiatel nie dziala
		// i czemu po dodaniu wielu pasow czasem jest ich 0, co psuje algorytm...
		// stateEndMinTurn = clock.getTurn() + (duration / counter);
		stateEndMinTurn = clock.getTurn() + (duration / (counter == 0 ? 1 : counter));

		//    stateEndMinTurn = clock.getTurn() + ((duration / counter) + maxDuration) / 2;
		//    stateEndMinTurn = maxDuration;
	}

	//  private void changeToGreen(Lane l) {
	//    greenLane = l;
	//
	//    if ( transitionDuration > 0 ) {
	//      setTransition();
	//    }
	//    else {
	//      setGreen( l );
	//    }
	//
	//  }

	//  private void setGreen(Lane l) {
	//    blockView.ext( l ).unblock();
	//    state = State.GREEN;
	//    stateEndMinTurn = clock.getTurn() + evalView.ext( l ).getMinGreenDuration();
	//  }

	private String chooseDirection() {
		//    System.err.print( intersection.getId() + ":" + chosenDirection + " , " );
		//evalView(intersectin)
		return agent.getChosenDirection();
	}

	public Agent getAgent() {
		return agent;
	}

	//  private Lane choseLane() {
	//    Lane chosenLane = null;
	//    float chosenEvaluation = Float.NEGATIVE_INFINITY;
	//    //    float chosenEvaluation = -1.0f;
	//    for (Iterator<Link> linkIter = intersection.inboundLinkIterator(); linkIter.hasNext();) {
	//      Link link = linkIter.next();
	//      if ( LOGGER.isTraceEnabled() ) {
	//        LOGGER.trace( link );
	//      }
	//
	//      for (Iterator<Lane> laneIter = link.laneIterator(); laneIter.hasNext();) {
	//        Lane lane = laneIter.next();
	//
	//        float evaluation = evalView.ext( lane ).getEvaluation();
	//        if ( LOGGER.isTraceEnabled() ) {
	//          LOGGER.trace( lane + " " + evaluation + ", chosen= " + chosenEvaluation );
	//        }
	//        if ( evaluation > chosenEvaluation ) {
	//          chosenLane = lane;
	//          chosenEvaluation = evaluation;
	//        }
	//      }
	//    }
	//
	//    if ( LOGGER.isTraceEnabled() ) {
	//      LOGGER.trace( chosenLane );
	//    }
	//
	//    // TODO: this is tricky, it should only work wll for SOTL
	//    //if ( chosenEvaluation == 0.0f ) {
	//    //  return null;
	//    //}
	//    // ---
	//    //    if ( chosenLane == null ) {
	//    //      if ( ok ) {
	//    //        ok = false;
	//    //        LOGGER.error( "PROBLEM on" + intersection );
	//    //        LOGGER.setLevel( Level.TRACE );
	//    //        makeDecision();
	//    //      }
	//    //      else {
	//    //
	//    //        System.exit( 0 );
	//    //      }
	//    //    }
	//    // ---
	//    return chosenLane;
	//  }

	public Lane getMostEmergencyLane() {
		Lane bestLane = null;
		int biggestEmergencyCarsNr = 0;
		int closestDistance = Integer.MAX_VALUE;
		int emergencyCarsNr;
		int distance;

		for (Iterator<Link> iter = intersection.inboundLinkIterator(); iter.hasNext(); ) {
			Link link = iter.next();
			for (Iterator<Lane> laneIter = link.laneIterator(); laneIter.hasNext(); ) {
				Lane lane = laneIter.next();
				if (blockView.ext(lane).anyEmergencyCarsOnLane()) {
					emergencyCarsNr = blockView.ext(lane).getEmergencyCarsOnLaneNr();
					distance = blockView.ext(lane).getClosestEmergencyCarDistance();
					if ((bestLane == null) || (emergencyCarsNr > biggestEmergencyCarsNr)
							|| (emergencyCarsNr == biggestEmergencyCarsNr && distance < closestDistance)) {
						bestLane = lane;
					}
				}
			}
		}

		return bestLane;
	}
}

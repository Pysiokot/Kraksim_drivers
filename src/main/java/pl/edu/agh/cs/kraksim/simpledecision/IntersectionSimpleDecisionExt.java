package pl.edu.agh.cs.kraksim.simpledecision;

import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.core.*;
import pl.edu.agh.cs.kraksim.iface.Clock;
import pl.edu.agh.cs.kraksim.iface.block.BlockIView;
import pl.edu.agh.cs.kraksim.iface.carinfo.CarInfoCursor;
import pl.edu.agh.cs.kraksim.iface.carinfo.CarInfoIView;
import pl.edu.agh.cs.kraksim.iface.carinfo.LaneCarInfoIface;
import pl.edu.agh.cs.kraksim.iface.decision.IntersectionDecisionExt;
import pl.edu.agh.cs.kraksim.iface.eval.EvalIView;
import pl.edu.agh.cs.kraksim.ministat.MiniStatEView;
import pl.edu.agh.cs.kraksim.routing.ITimeTable;
import pl.edu.agh.cs.kraksim.sna.SnaConfigurator;
import pl.edu.agh.cs.kraksim.weka.WekaPredictionModuleHandler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

public class IntersectionSimpleDecisionExt extends IntersectionDecisionExt {
	private static final Logger LOGGER = Logger.getLogger(IntersectionSimpleDecisionExt.class);

	private final EvalIView evalView;
	private final CarInfoIView carInfoView;
	private final MiniStatEView miniStatEView;
	private final WekaPredictionModuleHandler wekaPredictionHandler;
	private final ITimeTable timeTable;
	private long turnOfPhaseChange;
	private final List<Lane> greenLane = new ArrayList<>();
	private final Map<Link, Double> linksEvaluationMultipliersMap = new HashMap<>();
	private final Map<Link, Double> linksPrevAvgVelocityMap = new HashMap<>();

	private Lane interruptingLane = null;
	boolean interruptable = true;

	/**
	 * maximum duration time for green light on one lane *
	 */
	private static final int maxGreenLightDuration = 90;
	private static final double maxMultiplier = 5.0;
	private static final double minCarCountToSwitch = 30.0;

	// TODO: remove debug
	static {
		// clearing file
		try {
			BufferedWriter evaluationOut = new BufferedWriter(new FileWriter("evaluationModifier.txt"));
			evaluationOut.write("");
			evaluationOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	IntersectionSimpleDecisionExt(Intersection intersection, EvalIView evalView, BlockIView blockView, CarInfoIView carInfoView, MiniStatEView miniStatEView, WekaPredictionModuleHandler wekaPredictionHandler, ITimeTable timeTable, Clock clock, int transitionDuration) {
		super(intersection, blockView, clock, transitionDuration);
		this.evalView = evalView;
		this.carInfoView = carInfoView;
		this.wekaPredictionHandler = wekaPredictionHandler;
		this.miniStatEView = miniStatEView;
		this.timeTable = timeTable;
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
				if ((interruptable && ((interruptingLane = interruptGreen(chooseMinimalSpeedLanes())) != null)) || isPhaseFinished()) {
					switchPhase();
				}
				break;
			default:
				break;
		}
	}

	private void switchPhase() {
        Lane chosenLane;
		if (interruptingLane != null) {
			chosenLane = interruptingLane;
			interruptingLane = null;
		} else {
			chosenLane = choseLane();
		}

		LOGGER.trace(chosenLane);

		if (chosenLane != null) {
			Phase chosenPhase = getPhaseForLane(chosenLane);

			if (chosenPhase != null && !chosenPhase.equals(nextPhase)) {
				blockView.ext(intersection).blockInboundLinks();
				changeToGreen(chosenPhase);
			} else {
				if (!SnaConfigurator.getSnaEnabled()) {
					prolongCurrentPhase(10);
				} else {
					if (intersection.selfOptimizationInfo != null) {
						prolongCurrentPhase(intersection.selfOptimizationInfo.getChange());
					}
				}
			}
			if (blockView.ext(chosenLane).anyEmergencyCarsOnLane()) {
				Lane bestLane = getMostEmergencyLane();
				synchronized (this) {
					if (blockView.ext(bestLane).isBlocked()) {
						for (Iterator<Link> iter = intersection.inboundLinkIterator(); iter.hasNext(); ) {
							Link link = iter.next();
							for (Iterator<Lane> laneIter = link.laneIterator(); laneIter.hasNext(); ) {
								Lane lane = laneIter.next();
								blockView.ext(lane).block();
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

	private void setNextPhase() {
		if (isPhaseFinished()) {
			turnOfPhaseChange = clock.getTurn();
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
		final Lane chosenLane = choseLane();
		LOGGER.trace(chosenLane);

		if (chosenLane != null) {
			final Phase chosenPhase = getPhaseForLane(chosenLane);
			if (chosenPhase != null) {
				nextPhase = chosenPhase;
				changeToGreen(chosenPhase);
			}
		}
	}

	public Phase getPhaseForLane(final Lane l) {
		Iterator<Phase> it = intersection.trafficLightPhaseIterator();
		Phase selected = null;

		while (it.hasNext()) {
			Phase phase = it.next();
			String id = l.getOwner().getBeginning().getId();
			int laneNum = l.getRelativeNumber();
			LightState config = phase.getConfigurationFor(id, laneNum);

			if (config == null) {
				continue;
			}
			if (LOGGER.isDebugEnabled()) {
				//LOGGER.info("getPhase for " + l);
			}

			if (config.isGreen()) {
				selected = phase;
				break;
			}
		}

		return selected;
	}

	protected void setGreen(Phase phase) {
		int durationSum = 0;
		int counter = 0;

		greenLane.clear();
		for (Iterator<Link> iter = intersection.inboundLinkIterator(); iter.hasNext(); ) {
			Link link = iter.next();
			String arm = link.getBeginning().getId();

			for (Iterator<Lane> laneIter = link.laneIterator(); laneIter.hasNext(); ) {
				Lane lane = laneIter.next();

				int laneNum = lane.getRelativeNumber();
				LightState light = phase.getConfigurationFor(arm, laneNum);

				if (light.isGreen()) {
					blockView.ext(lane).unblock();
					greenLane.add(lane);

					double minGreenDuration = evalView.ext(lane).getMinGreenDuration();

					counter++;
					if (!SnaConfigurator.getSnaEnabled()) {
						durationSum += minGreenDuration;
					} else {
						if (intersection.selfOptimizationInfo == null) {
							continue;
						}
						durationSum += intersection.selfOptimizationInfo.getChange();
					}
				} else {
					blockView.ext(lane).block();
				}
			}
		}
		state = State.GREEN;
		// counts average duration for all green lanes in this phase
		int laneAvgDuration = durationSum / counter;
		if (laneAvgDuration > maxGreenLightDuration) {
			stateEndMinTurn = clock.getTurn() + maxGreenLightDuration;
		} else {
			stateEndMinTurn = clock.getTurn() + laneAvgDuration;
		}
		// stateEndMinTurn = clock.getTurn() + ((duration / counter) +
		// maxDuration) / 2;
	}

	/**
	 * Oblicza ile powinno się świecić zielone światło aby udało się
	 * zachować minimalne prędkości. Korzysta z mapy która przechowuje
	 * aktualny mnożnik oraz mapy przechowującej poprzednią średnią
	 * prędkość na drodze aby wykryć zmianę prędkości średniej czyli
	 * nowy pomiar.
	 *
	 * @param lane
	 * @return
	 */
	private double calculateEvaluationMultiplier(Lane lane) {
		if (lane.getMinimalSpeed() > 0.0) {
			Link link = lane.getOwner();
			Double currentMultiplier = linksEvaluationMultipliersMap.get(link);

			// jak przekazany jest 'timeTable' to używa czasu z tego,
			// uwzględnia predykcję
			double currentSpeed;
			String predMethod;
			if (timeTable != null) {
				currentSpeed = ((double) link.getLength() / timeTable.getLinkTime(link));
				predMethod = "z predykcją";
			} else {
				currentSpeed = miniStatEView.ext(link).getLastPeriodAvgVelocity();
				predMethod = "bez predykcji";
			}

			if (currentMultiplier == null || currentSpeed == -1.0f) {
				currentMultiplier = 1.0;
			} else {
				Double prevSpeed = linksPrevAvgVelocityMap.get(link);

				// jeżeli prędkość dla linku się zmieniła czyli był nowy
				// pomiar
				if (prevSpeed == null || prevSpeed != currentSpeed) {
					linksPrevAvgVelocityMap.put(link, currentSpeed);

					if (currentSpeed < lane.getMinimalSpeed() + 0.2) {
						currentMultiplier *= 1.2;// (lane.getMinimalSpeed()+0.2)/currentSpeed;//multiplierMultiplierInc;
					} else {
						currentMultiplier *= 0.9;// (lane.getMinimalSpeed()+0.2)/currentSpeed;//multiplierMultiplierDec;
					}
					if (currentMultiplier > maxMultiplier) {
						currentMultiplier = maxMultiplier;
					} else if (currentMultiplier < 1.0) {
						currentMultiplier = 1.0;
					}

					//bylo: double speedWithoutPred = miniStatEView.ext(link).getLastPeriodAvgVelocity();
					//bylo zapisywanie: "link " + link.getId() + " newMulti " + currentMultiplier + " speed " + currentSpeed + " speedWithoutPred " + speedWithoutPred + " " + predMethod;
				}
			}
			linksEvaluationMultipliersMap.put(link, currentMultiplier);
			return currentMultiplier;
		} else {
			return 1.0;
		}
	}


	// private void changeToGreen(Lane l) {
	// greenLane = l;
	//
	// if ( transitionDuration > 0 ) {
	// setTransition();
	// }
	// else {
	// setGreen( l );
	// }
	//
	// }

	// private void setGreen(Lane l) {
	// blockView.ext( l ).unblock();
	// state = State.GREEN;
	// stateEndMinTurn = clock.getTurn() + evalView.ext( l
	// ).getMinGreenDuration();
	// }

	private Lane choseLane() {
		greenLane.clear();
		if (!interruptable) {
			interruptable = true;
			Lane lane = interruptGreen(chooseMinimalSpeedLanes());
			if (lane != null) {
				return lane;
			}
		}

		Lane chosenLane = null;
		float chosenEvaluation = Float.NEGATIVE_INFINITY;
		// float chosenEvaluation = -1.0f;
		for (Iterator<Link> linkIter = intersection.inboundLinkIterator(); linkIter.hasNext(); ) {

			Link link = linkIter.next();
			// System.out.println( intersection.getId() + "  " +
			// link.getEnd().getId() + " b "
			// + link.getBeginning().getId() );
			LOGGER.trace(link);

			for (Iterator<Lane> laneIter = link.laneIterator(); laneIter.hasNext(); ) {
				Lane lane = laneIter.next();

				float evaluation = evalView.ext(lane).getEvaluation();

				evaluation = maxEvaluationIfBlockedLane(lane, evaluation);
				evaluation = wekaPredictionHandler.adjustEvalToPrediction(lane, evaluation);

				LOGGER.trace(lane + " " + evaluation + ", chosen= " + chosenEvaluation);
				if (evaluation > chosenEvaluation) {
					chosenLane = lane;
					chosenEvaluation = evaluation;
				}
			}
		}

		LOGGER.trace(chosenLane);

		// TODO: this is tricky, it should only work wll for SOTL
		// o co tutaj chodzi? TODO: moze ma byc przekazane skads?
		boolean isSotl = true;
		if (isSotl && chosenEvaluation == 0.0f) {
			return null;
		}
		return chosenLane;
	}

	private float maxEvaluationIfBlockedLane(Lane lane, float evaluationFromLightsControl) {
		LaneCarInfoIface laneCarInfo = carInfoView.ext(lane);
		CarInfoCursor infoForwardCursor = laneCarInfo.carInfoForwardCursor();
		if (laneIsFull(infoForwardCursor)) {
			return Float.MAX_VALUE;
		} else {
			return evaluationFromLightsControl;
		}
	}

	private boolean laneIsFull(CarInfoCursor infoForwardCursor) {
		return anyCarsOnLane(infoForwardCursor) && lastCarOnLaneIsNotMoving(infoForwardCursor);
	}

	private static boolean anyCarsOnLane(CarInfoCursor infoForwardCursor) {
		return infoForwardCursor != null && infoForwardCursor.isValid();
	}

	private static boolean lastCarOnLaneIsNotMoving(CarInfoCursor infoForwardCursor) {
		return infoForwardCursor.currentVelocity() == 0.0f && infoForwardCursor.currentPos() == 0;
	}

	private class Pair {
		public Pair(Lane lane2, double multiplier2) {
			lane = lane2;
			multiplier = multiplier2;
		}

		public Lane lane;
		public double multiplier;
	}

	class Comparer implements Comparator<Pair> {

		@Override
		public int compare(Pair o1, Pair o2) {
			return (int) -((o1.multiplier - o2.multiplier) * 1000);
		}
	}

	private List<Pair> chooseMinimalSpeedLanes() {
		List<Pair> chosenLane = new ArrayList<>();
		// List<Lane> result = new ArrayList<Lane>();
		double bestMultiplier = 1.0;

		for (Iterator<Link> linkIter = intersection.inboundLinkIterator(); linkIter.hasNext(); ) {

			Link link = linkIter.next();
			// System.out.println( intersection.getId() + "  " +
			// link.getEnd().getId() + " b "
			// + link.getBeginning().getId() );
			LOGGER.trace(link);

			for (Iterator<Lane> laneIter = link.laneIterator(); laneIter.hasNext(); ) {
				Lane lane = laneIter.next();
				double multiplier = calculateEvaluationMultiplier(lane);
				if (multiplier > bestMultiplier) {
					chosenLane.add(new Pair(lane, multiplier));
				}
			}
		}

		// sprawdzic czy dobrze sortuje
		// Collections.sort(chosenLane, new Comparer());
		/*
		 * if(chosenLane.size() > 0){ List<Pair> help = new ArrayList<Pair>();
		 * for(Pair pair : chosenLane){ help.add(pair); } help.add(new
		 * Pair(help.get(0).lane,10)); help.add(new Pair(help.get(0).lane,12));
		 * help.add(new Pair(help.get(0).lane,1));
		 * 
		 * Collections.sort(help, new Comparer()); int indeks = 0; for(Pair pair
		 * : help){ System.out.println(indeks + ": " + pair.multiplier);
		 * ++indeks; } } /*for(Pair pair : chosenLane){ result.add(pair.lane);
		 * }//
		 */
		return chosenLane;
	}

	private Lane interruptGreen(List<Pair> lanes) {
		Lane bestLane = null;
		double bestEval = 0.0;
		for (Pair pair : lanes) {
			Lane lane = pair.lane;
			if (greenLane.contains(lane)) {
				return null;
			}
			Link owner = lane.getOwner();
			double multiplier = pair.multiplier;
			int carCount = miniStatEView.ext(owner).getCarCount();
			if (carCount >= minCarCountToSwitch / multiplier) {
				double evaluation = evalView.ext(lane).getEvaluation();
				if (evaluation > bestEval) {
					bestEval = evaluation;
					bestLane = lane;
					// System.out.println(carCount);
				}
				// System.out.println("interruptuje: " + lane.toString() +
				// " mulitplier: " + multiplier + " carCount: " + carCount);
				// zrobic pas wybrany w ten sposob nieinterruptowalnym
				interruptable = false;
				// return lane;
			}
		}
		return bestLane;
	}

	public Phase getPhase() {
		return nextPhase;
	}

	public long getStateEndTurn() {
		return stateEndMinTurn + transitionDuration;
	}

	public long getTurnOfLastPhaseChange() {
		return turnOfPhaseChange;
	}

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

package pl.edu.agh.cs.kraksim.rlcd;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.core.Lane;
import pl.edu.agh.cs.kraksim.iface.block.BlockIView;
import pl.edu.agh.cs.kraksim.iface.block.LaneBlockIface;
import pl.edu.agh.cs.kraksim.iface.carinfo.CarInfoCursor;
import pl.edu.agh.cs.kraksim.iface.carinfo.CarInfoIView;
import pl.edu.agh.cs.kraksim.iface.carinfo.LaneCarInfoIface;
import pl.edu.agh.cs.kraksim.iface.eval.LaneEvalIface;
import pl.edu.agh.cs.kraksim.rlcommon.CellStat;
import pl.edu.agh.cs.kraksim.rlcommon.TransitionStat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class LaneRLCDExt implements LaneEvalIface {
	private static final Logger LOGGER = Logger.getLogger(LaneRLCDExt.class);
	private static final int ZONELENGHT = 20;
	private static final float omega = (float) 0.5;
	private static final float p = (float) 1;
	private static final float lambda = (float) 0.1;
	private final Lane lane;
	private final RLCDEView ev;
	private final RLCDParams params;
	private final LaneCarInfoIface laneCarInfoExt;
	private final int n;
	private final float[] V;
	private final String id;
	private LaneBlockIface laneBlockExt;
	private float[] Qblocked;
	private float[] Qunblocked;
	//private CellStat[]          cells;
	//list modeli
	private final List<Model> models = new ArrayList<>();
	//model, ktory jest aktualnie w uzyciu
	//TODO sprawdzic, czy jak sie aktualizuje model, to jego odpowiednik w models tez sie aktualizuje
	private Model model;
	private float evaluation;
	private int queueLength;

	LaneRLCDExt(Lane lane, RLCDEView ev, CarInfoIView carInfoView, BlockIView blockView, RLCDParams params) {
		this.lane = lane;
		this.ev = ev;
		this.params = params;

		LogManager.shutdown();

		n = lane.getLength();
		id = lane.getOwner().getId() + ':' + lane.getAbsoluteNumber() + ':' + n;
		V = new float[n];
		/* for lanes going to gateway, we may assume that V[i] = 0.0 for all i */

		laneCarInfoExt = carInfoView.ext(lane);

		if (lane.getOwner().getEnd().isIntersection()) {
			laneBlockExt = blockView.ext(lane);

			Qblocked = new float[n];
			Qunblocked = new float[n];

			model = new Model(new CellStat[n]);
			models.add(model);
			for (int pos = 0; pos < n; pos++) {
				model.cells[pos] = new CellStat();
			}
		}

		LOGGER.trace(lane + ", lengt (n)=" + n);
	}

	void halveCounters() {
		int n = lane.getLength();
		for (int pos = 0; pos < n; pos++) {
			model.cells[pos].halveCounters();
		}
	}

	void updateStatsToGateway() {
		CarInfoCursor cursor = laneCarInfoExt.carInfoForwardCursor();
		if (cursor.isValid()) {
			Lane l = cursor.beforeLane();
			if (l != null && l != lane) {
				ev.ext(l).countCar(cursor.beforePos(), lane, cursor.currentPos());
			}
		}
	}

	private void countCar(int pos, Lane destLane, int destPos) {
		LOGGER.trace(id + ": from=" + pos + ", to lane " + destLane + " newPos" + destPos);
		try {
			if (laneBlockExt.isBlocked()) {
				model.cells[pos].countCarOnBlocked(destLane, destPos);
			} else {
				model.cells[pos].countCarOnUnblocked(destLane, destPos);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	void updateStatsToIsect() {
//	    CarInfoCursor cursor = laneCarInfoExt.carInfoForwardCursor();
		CarInfoCursor cursor = laneCarInfoExt.carInfoBackwardCursor();
		while (cursor.isValid()) {
			Lane l = cursor.beforeLane();
			// optimalization of the frequent case: don't use module view when the
			// car hasn't changed the lane
			int pos = cursor.currentPos();
			if ((n - pos) > ZONELENGHT) {
				break;
			}
			if (l == lane) {
				countCar(cursor.beforePos(), lane, pos);
			} else if (l != null) {
				ev.ext(l).countCar(cursor.beforePos(), lane, pos);
			}
			cursor.next();
		}
	}

	private Pair<Float, Float> countQ(CellStat c, float pos, boolean useBlocked) {
		float q = 0.0f;
		float errorDelta = 0;
		TransitionStat[] stats = useBlocked ? c.transOnBlockedStats : c.transOnUnblockedStats;

		for (TransitionStat ts : stats) {
			float p = (float) ts.count / (float) c.carOnBlockedCount;
			if (ts.t == 0) {
				//ts.t = (float) ts.count / (float) c.carOnBlockedCount;
				//ts.t = (float) ts.count / (float) c.transOnBlockedStats.length;
				ts.t = p;
			}
			//float p = ts.t;
			LOGGER.trace("percentage: " + p + "ts.t: " + ts.t);
			// optimalization of the frequent case: don't use module view when the
			// car hasn't changed the lane
			float v = 1.0f;
			if (ts.destLane == lane) {
				v = params.discount * V[ts.destPos];
				if (ts.destPos == pos) {
					if (useBlocked) {
						v += 1.0f;
					} else {
						q += 1.0f;
					}
				}
			} else {
				try {
					v = params.discount * ev.ext(ts.destLane).V[ts.destPos];
				} catch (Exception e) {
					//TODO
				}
			}
			// TODO: lepsza metoda karania
			//LOGGER.trace("mytrace p: " + p + "v: " + v + "ts.r: " + ts.r);
			//v += ts.r;
			int krDelta = 0;
			int rr = 0;
			if (ts.destLane == lane && ts.destPos == pos) {
				krDelta = 1;
				rr = 1;
			}
			ts.updateDeltaT(c.carOnBlockedCount, krDelta);
			ts.updateDeltaR(c.carOnBlockedCount, rr);
			ts.updateT();
			ts.updateR();

			float cm = ((float) c.carOnBlockedCount / (float) TransitionStat.M) * ((float) c.carOnBlockedCount / (float) TransitionStat.M);
			errorDelta += cm * (omega * ts.deltaR * ts.deltaR + (1 - omega) * ts.deltaT);
			q += p * v;

		}
		return Pair.of(q, errorDelta);
	}

	void updateValues1() {
		float error = 0;
		for (int pos = 0; pos < n; pos++) {
			CellStat c = model.cells[pos];


			if (c.carOnBlockedCount == 0) {
				Qblocked[pos] = 0.0f;
			} else {
				Pair<Float, Float> qWithError = countQ(c, pos, true);
				Qblocked[pos] = qWithError.getLeft();
				error += qWithError.getRight();
			}

			if (c.carOnUnblockedCount == 0) {
				Qunblocked[pos] = 0.0f;
			} else {
				Pair<Float, Float> qWithError =countQ(c, pos, false);
				Qunblocked[pos] = qWithError.getLeft();
				error += qWithError.getRight();
			}
		}
		for (Model m : models) {
			m.error = m.error + p * (error - m.error);
		}
	}

	void updateValues2() {
		for (int pos = 0; pos < n; pos++) {
			CellStat c = model.cells[pos];

			int totalCount = c.carOnBlockedCount + c.carOnUnblockedCount;

			if (totalCount > 0) {
				float pb = (float) c.carOnBlockedCount / (float) totalCount;
				V[pos] = pb * Qblocked[pos] + (1 - pb) * Qunblocked[pos];
			} else {
				V[pos] = 0.0f;
			}
		}
	}

	void changeModel() {
		if (model.error > lambda) {
			Model bestmodel = model;
			for (Model m : models) {
				if (m.error < bestmodel.error) {
					bestmodel = m;
				}
			}
			if (bestmodel.error < lambda) {
				model = bestmodel;
				LOGGER.trace("oldmodel");
			} else {
				LOGGER.trace("newmodel");
				model = new Model(new CellStat[n]);
				for (int i = 0; i < n; ++i) {
					model.cells[i] = new CellStat();
				}
				models.add(model);
			}
		} else {
			for (Model m : models) {
				if (m.error < model.error) {
					model = m;
				}
			}
		}
		LOGGER.trace("models.size(): " + models.size() + "model.error: " + model.error);
	}

	void makeEvaluation() {
		evaluation = 0.0f;

		int queueEnd = lane.getLength();
		LOGGER.trace(id + ", blocked=" + laneBlockExt.isBlocked() + ", queueEnd=" + queueEnd);
		CarInfoCursor cursor = laneCarInfoExt.carInfoBackwardCursor();

		while (cursor.isValid()) {
			// TODO: currentPos?
			//      int pos = cursor.currentPos();

			int pos = cursor.currentPos();
//	      System.out.println( n + ", " + pos + " = " + (n - pos) );
			if ((n - pos) > ZONELENGHT) {
				break;
			}

			LOGGER.trace("      position=" + pos);
			if (pos + 1 < queueEnd) {
				break;
			}

			queueEnd = pos;

			//      try {
			evaluation += Qblocked[pos] - Qunblocked[pos];
			LOGGER.trace("      queueEnd := position=" + pos);
			LOGGER.trace("      evaluation=" + evaluation + ": Qb=" + Qblocked[pos] + " - Qu" + Qunblocked[pos]);
			//        if ( evaluation < 0.0 ) {
			//          System.out.println( lane + "  " + evaluation );
			//        }
			//      }
			//      catch (Exception e) {
			//        LOGGER.warn( e.getMessage() );
			//      }

			cursor.next();
		}

		queueLength = lane.getLength() - queueEnd;
		LOGGER.trace("      queueLength=" + queueLength);
	}

	public float getEvaluation() {
		LOGGER.trace(id + " queue=" + queueLength + ", evaluation=" + evaluation + ", blocked=" + laneBlockExt.isBlocked());
		return evaluation;
	}

	public int getMinGreenDuration() {
		int ret = (int) ((queueLength - 1) * (float) params.carStartDelay + (queueLength / (float) params.carMaxVelocity));
		return Math.max(ret, params.minimumGreen);
	}

	private static final class Model {
		private final CellStat[] cells;
		private float error = 0;

		Model(CellStat[] cells) {
			this.cells = Arrays.copyOf(cells, cells.length);
		}
	}
}

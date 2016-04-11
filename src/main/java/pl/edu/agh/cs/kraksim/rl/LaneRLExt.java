package pl.edu.agh.cs.kraksim.rl;

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

class LaneRLExt implements LaneEvalIface {
	private static final Logger LOGGER = Logger.getLogger(LaneRLExt.class);

	private static final int ZONELENGHT = 20;

	private final Lane lane;

	private final RLEView ev;
	private final RLParams params;

	private final LaneCarInfoIface laneCarInfoExt;
	private LaneBlockIface laneBlockExt;

	private final int n;
	private final float[] V;
	private float[] Qblocked;
	private float[] Qunblocked;
	private CellStat[] cells;
	private float evaluation;
	private int queueLength;

	private final String id;

	LaneRLExt(Lane lane, RLEView ev, CarInfoIView carInfoView, BlockIView blockView, RLParams params) {
		this.lane = lane;
		this.ev = ev;
		this.params = params;

		n = lane.getLength();
		id = lane.getOwner().getId() + ':' + lane.getAbsoluteNumber() + ':' + n;
		V = new float[n];
		/* for lanes going to gateway, we may assume that V[i] = 0.0 for all i */

		laneCarInfoExt = carInfoView.ext(lane);

		if (lane.getOwner().getEnd().isIntersection()) {
			laneBlockExt = blockView.ext(lane);

			Qblocked = new float[n];
			Qunblocked = new float[n];

			cells = new CellStat[n];
			for (int pos = 0; pos < n; pos++) {
				cells[pos] = new CellStat();
			}
		}

		LOGGER.trace(lane + ", lengt (n)=" + n);
	}

	private void countCar(int pos, Lane destLane, int destPos) {
		LOGGER.trace(id + ": from=" + pos + ", to lane " + destLane + " newPos" + destPos);
		try {
			if (laneBlockExt.isBlocked()) {
				cells[pos].countCarOnBlocked(destLane, destPos);
			} else {
				cells[pos].countCarOnUnblocked(destLane, destPos);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	void halveCounters() {
		int n = lane.getLength();
		for (int pos = 0; pos < n; pos++) {
			cells[pos].halveCounters();
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

	void updateStatsToIsect() {
		// CarInfoCursor cursor = laneCarInfoExt.carInfoForwardCursor();
		CarInfoCursor cursor = laneCarInfoExt.carInfoBackwardCursor();
		while (cursor.isValid()) {
			Lane l = cursor.beforeLane();
			// optimalization of the frequent case: don't use module view when
			// the
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

	void updateValues1() {
		for (int pos = 0; pos < n; pos++) {
			CellStat c = cells[pos];

			if (c.carOnBlockedCount == 0) {
				Qblocked[pos] = 0.0f;
			} else {
				float q = 0.0f;
				for (int i = 0; i < c.transOnBlockedStats.length; i++) {
					TransitionStat ts = c.transOnBlockedStats[i];
					float p = (float) ts.count / (float) c.carOnBlockedCount;
					float v;
					// optimalization of the frequent case: don't use module
					// view when the
					// car hasn't changed the lane
					v = 1.0f;
					if (ts.destLane == lane) {
						v = params.discount * V[ts.destPos];
						if (ts.destPos == pos) {
							v += 1.0f;
						}
					} else {
						try {
							v = params.discount * ev.ext(ts.destLane).V[ts.destPos];
						} catch (Exception e) {
							// TODO
						}
					}
					// TODO: lepsza metoda karania
					q += p * v;
				}
				Qblocked[pos] = q;
			}

			if (c.carOnUnblockedCount == 0) {
				Qunblocked[pos] = 0.0f;
			} else {
				float q = 0.0f;
				for (int i = 0; i < c.transOnUnblockedStats.length; i++) {
					TransitionStat ts = c.transOnUnblockedStats[i];
					float p = (float) ts.count / (float) c.carOnUnblockedCount;
					float v;
					// optimalization of the frequent case: don't use module
					// view when the
					// car hasn't changed the lane
					v = 1.0f;
					if (ts.destLane == lane) {
						v = params.discount * V[ts.destPos];
						if (ts.destPos == pos) {
							q += 1.0f;
						}
					} else {
						try {
							v = params.discount * ev.ext(ts.destLane).V[ts.destPos];
						} catch (Exception e) {
							// TODO
						}
					}
					// TODO: lepsza metoda karania
					q += p * v;
				}
				Qunblocked[pos] = q;
			}
		}
	}

	void updateValues2() {
		for (int pos = 0; pos < n; pos++) {
			CellStat c = cells[pos];

			int totalCount = c.carOnBlockedCount + c.carOnUnblockedCount;

			if (totalCount > 0) {
				float pb = (float) c.carOnBlockedCount / (float) totalCount;
				V[pos] = pb * Qblocked[pos] + (1 - pb) * Qunblocked[pos];
			} else {
				V[pos] = 0.0f;
			}
		}
	}

	void makeEvaluation() {
		evaluation = 0.0f;

		int queueEnd = lane.getLength();
		LOGGER.trace(id + ", blocked=" + laneBlockExt.isBlocked() + ", queueEnd=" + queueEnd);
		CarInfoCursor cursor = laneCarInfoExt.carInfoBackwardCursor();

		while (cursor.isValid()) {
			// TODO: currentPos?
			// int pos = cursor.currentPos();

			int pos = cursor.currentPos();
			// System.out.println( n + ", " + pos + " = " + (n - pos) );
			if ((n - pos) > ZONELENGHT) {
				break;
			}

			LOGGER.trace("      position=" + pos);
			if (pos + 1 < queueEnd) {
				break;
			}

			queueEnd = pos;

			// try {
			evaluation += Qblocked[pos] - Qunblocked[pos];
			LOGGER.trace("      queueEnd := position=" + pos);
			LOGGER.trace("      evaluation=" + evaluation + ": Qb=" + Qblocked[pos] + " - Qu" + Qunblocked[pos]);
			// if ( evaluation < 0.0 ) {
			// System.out.println( lane + "  " + evaluation );
			// }
			// }
			// catch (Exception e) {
			// LOGGER.warn( e.getMessage() );
			// }

			cursor.next();
		}

		queueLength = lane.getLength() - queueEnd;
		LOGGER.trace("      queueLength=" + queueLength);
	}

	public float getEvaluation() {
		//+ laneBlockExt.isBlocked() is null
		LOGGER.trace(id + " queue=" + queueLength + ", evaluation=" + evaluation + ", blocked=" );
		return evaluation;
	}

	public int getMinGreenDuration() {
		int ret = (int) ((queueLength - 1) * (float) params.carStartDelay + (queueLength / (float) params.carMaxVelocity));
		return Math.max(ret, params.minimumGreen);
	}
}

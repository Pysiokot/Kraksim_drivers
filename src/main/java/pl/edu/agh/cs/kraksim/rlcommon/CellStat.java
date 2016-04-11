package pl.edu.agh.cs.kraksim.rlcommon;

import pl.edu.agh.cs.kraksim.core.Lane;

public final class CellStat {
	public int carOnBlockedCount;
	public TransitionStat[] transOnBlockedStats;
	public int carOnUnblockedCount;
	public TransitionStat[] transOnUnblockedStats;

	public void countCarOnBlocked(Lane destLane, int destPos) {
		carOnBlockedCount++;

		if (transOnBlockedStats != null) {
			for (TransitionStat ts : transOnBlockedStats) {
				if (ts.destLane == destLane && ts.destPos == destPos) {
					ts.count++;
					return;
				}
			}
		}

		transOnBlockedStats = extend(transOnBlockedStats, new TransitionStat(destLane, destPos, 1));
	}

	public void countCarOnUnblocked(Lane destLane, int destPos) {
		carOnUnblockedCount++;

		if (transOnUnblockedStats != null) {
			for (TransitionStat ts : transOnUnblockedStats) {
				if (ts.destLane == destLane && ts.destPos == destPos) {
					ts.count++;
					return;
				}
			}
		}

		transOnUnblockedStats = extend(transOnUnblockedStats, new TransitionStat(destLane, destPos, 1));
	}

	public void halveCounters() {
		if (carOnBlockedCount > 0) {
			carOnBlockedCount = 0;
			for (TransitionStat transOnBlockedStat : transOnBlockedStats) {
				transOnBlockedStat.count /= 2;
				carOnBlockedCount += transOnBlockedStat.count;
			}
		}

		if (carOnUnblockedCount > 0) {
			carOnUnblockedCount = 0;
			for (TransitionStat transOnUnblockedStat : transOnUnblockedStats) {
				transOnUnblockedStat.count /= 2;
				carOnUnblockedCount += transOnUnblockedStat.count;
			}
		}
	}

	public static TransitionStat[] extend(TransitionStat[] a, TransitionStat ts) {
		if (a == null) {
			return new TransitionStat[]{ts};
		}
		TransitionStat[] b = new TransitionStat[a.length + 1];
		System.arraycopy(a, 0, b, 0, a.length);
		b[a.length] = ts;
		return b;
	}
}
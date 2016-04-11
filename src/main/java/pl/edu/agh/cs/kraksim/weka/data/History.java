package pl.edu.agh.cs.kraksim.weka.data;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class History {
	private final LinkedList<AssociatedWorldState> history;
	private final LinkedList<Integer> turns;
	private final String[] linkNameTable;

	public History(Set<LinkInfo> set, Set<IntersectionInfo> intersections) {
		history = new LinkedList<>();
		turns = new LinkedList<>();
		linkNameTable = createLinkNameTable(set);
	}

	public History(History history) {
		this.history = new LinkedList<>(history.history);
		turns = new LinkedList<>(history.turns);
		linkNameTable = history.linkNameTable.clone();
	}

	public double getCongestionByTimeDistance(int timeDistance, int linkNumber) {
		return history.get(timeDistance).roads.getCarsDensity(linkNumber);
	}

	public void add(int turn, AssociatedWorldState associatedWorldState) {
		turns.add(0, turn);
		history.add(0, associatedWorldState);
	}

	public void remove() {
		turns.remove();
		history.remove();
	}

	public void clear() {
		turns.clear();
		history.clear();
	}

	public int depth() {
		return history.size();
	}

	public AssociatedWorldState poll() {
		return history.poll();
	}

	public AssociatedWorldState getByDepth(int depth) {
		return history.get(depth);
	}

	public String[] getLinkNameTable() {
		return linkNameTable;
	}

	public List<Integer> getTurns() {
		return turns;
	}

	public History addAll(History history2) {
		turns.addAll(history2.turns);
		history.addAll(history2.history);
		return this;
	}

	private static String[] createLinkNameTable(Set<LinkInfo> set) {
		String[] linkNameTable = new String[set.size()];

		for (LinkInfo linkInfo : set) {
			int linkNumber = linkInfo.linkNumber;
			linkNameTable[linkNumber] = linkInfo.linkId;
		}
		return linkNameTable;
	}
}

package pl.edu.agh.cs.kraksim.optapo.algo;

import com.google.common.collect.Lists;
import pl.edu.agh.cs.kraksim.optapo.algo.agent.Direction;

import java.util.HashMap;
import java.util.List;

/**
 * This class is used to store a particular solution to a
 * constraint problem between multiple agents
 * <p/>
 * Created: Tue Aug 20 14:14:41 2002
 *
 * @author Roger Mailler
 */
public class Solution extends HashMap<String, Direction> {
	private static final long serialVersionUID = 3852870293097308715L;

	double effect = 0.0;
	double cost = 0.0;

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public double getEffect() {
		return effect;
	}

	public void setEffect(double val) {
		effect = val;
	}

	public boolean equals(Object o1) {
		int found = 0;
		Solution other = (Solution) o1;
		if (size() != other.size()) {
			return false;
		}
		for (String name : keySet()) {
			if (other.get(name).equals(get(name))) {
				found++;
			}
		}
		return found == size();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String name : keySet()) {
			sb.append(name).append('-').append(get(name));
			sb.append(',');
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	/**
	 * Determines if one solution is a subset of another
	 */
	public boolean subList(Solution soln) {
		for (String agent : soln.keySet()) {
			Direction value = soln.get(agent);
			if (!containsKey(agent) || !get(agent).equals(value)) {
				return false;
			}
		}
		return true;
	}

	public Object clone() {
		Solution newOne = new Solution();
		for (String name : keySet()) {
			newOne.put(name, get(name));
		}
		newOne.effect = effect;
		return newOne;
	}

	/**
	 * Checks to see if an agent is contained in the solution
	 *
	 * @param name the name of the agent to check for
	 * @return true if the solution contains the agent
	 */
	public boolean containsAgent(String name) {
		return containsKey(name);
	}

	/**
	 * Checks to see if an agent is contained in the solution
	 *
	 * @param name the name of the agent to check for
	 * @return true if the solution contains the agent
	 */
	public List<String> getAgents() {
		return Lists.newArrayList(keySet());
	}
} // Solution

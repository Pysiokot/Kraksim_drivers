package pl.edu.agh.cs.kraksim.optapo.algo;

import pl.edu.agh.cs.kraksim.optapo.algo.agent.Agent;
import pl.edu.agh.cs.kraksim.optapo.algo.agent.Conflict;
import pl.edu.agh.cs.kraksim.optapo.algo.agent.Direction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Solver {

	double currInnerBound = Double.MAX_VALUE;
	double currOuterBound = Double.MAX_VALUE;
	double initialBound = Double.MAX_VALUE;

	Direction[] currSoln;
	Direction[] bestSoln;

	List<Agent> goodlist;
	// ArrayList<Agent> agent_view;
	// HashMap<String, Agent> agent_view;
	Map<String, Map<Direction, Double>> costTable;
	private AgentFarm farm;

	/**
	 * Given the preferences and the known interaction structure,
	 * find the best possible solution
	 */
	public Solution findBestSoln(AgentFarm farm, List<Agent> goodlist, Map<String, Map<Direction, List<Conflict>>> preferences, double lowerBound, double upperBound) {
		this.farm = farm;
		Solution soln = new Solution();

		currInnerBound = upperBound;
		currOuterBound = Integer.MAX_VALUE;
		initialBound = lowerBound;

//    System.out.println( "Solver.findBestSoln()" + currInnerBound + " " + currOuterBound + " "
//                        + initialBound + " " + goodlist );
		currSoln = new Direction[goodlist.size()];
		bestSoln = new Direction[goodlist.size()];

		this.goodlist = goodlist;

		createCostTable(preferences);

		findSoln(0, 0, 0);

		for (int i = 0; i < bestSoln.length; i++) {
			soln.put(goodlist.get(i).getName(), bestSoln[i]);
		}
		soln.setEffect(currInnerBound + currOuterBound);
		soln.setCost(currInnerBound);

		return soln;
	}

	/**
	 * Does a two criteria branch and bound search for the best
	 * solution
	 */
	private void findSoln(int agent, double innerCount, double outerCount) {
//    System.out.println( currInnerBound + " - " + innerCount + " - " + outerCount );
		// is this the bottom of the recursion?
		if (agent == goodlist.size()) {
			// all variables assigned, found a better assignment
			System.arraycopy(currSoln, 0, bestSoln, 0, bestSoln.length);

			currInnerBound = innerCount;
			currOuterBound = outerCount;

			return;
		}

		// String thisAName = goodlist.get( agent );
		Agent thisA = goodlist.get(agent);
		String thisAName = thisA.getName();
		// ArrayList<String> domain = thisA.getDomain();

		Direction[] domain = Direction.values();

		for (Direction domVal : domain) {
			double thisCost = 0;

			for (Agent neigh : thisA.getNeighbours()) {
				int neighIndex = goodlist.indexOf(neigh);

				if (neighIndex > -1 && neighIndex < agent) {
					// Constraint constraint = thisA.getConstraint(
					// neighName );
					thisCost += thisA.checkRelation(domVal, currSoln[neighIndex], neigh);
				}
			}

			if (innerCount + thisCost < currInnerBound) {
				double thisOutCost = 0;

				// check to see if better than the outerbound
				if (costTable.containsKey(thisAName)) {
					Map<Direction, Double> prefs = costTable.get(thisAName);
					Double violates = prefs.get(domVal);
					thisOutCost += violates;
				}

				// if (innerCount + thisCost < currInnerBound
				// || (innerCount + thisCost == currInnerBound &&
				// outerCount
				// + thisOutCost < currOuterBound)) {

				currSoln[agent] = domVal;

				findSoln(agent + 1, innerCount + thisCost, outerCount + thisOutCost);

				// if (currInnerBound == initialBound && currOuterBound
				// == 0)

				if (currInnerBound == initialBound) {
					return;
				}

				// }
			}
		}
	}

	/**
	 * Get the total outer cost of choosing the value which leads
	 * to this list of violations
	 *
	 * @parm violates A list of agent that will be violated with
	 * their associated value
	 */
	public double getCostViolates(List<Conflict> violates, List<Agent> goodlist) {
		double totalCost = 0;
		for (Conflict conf : violates) {
			if (!goodlist.contains(farm.getAgent(conf.getName()))) {
				totalCost += conf.getCost();
			}
		}

		return totalCost;
	}

	/**
	 * This function creates a cost table that is used to determine
	 * the outside costs for forcing an agent to change their
	 * domain value
	 *
	 * @param preferences
	 */
	// private void createCostTable(HashMap<String, HashMap<String,
	// HashMap<String, Integer>>> preferences)
	private void createCostTable(Map<String, Map<Direction, List<Conflict>>> preferences) {
		costTable = new HashMap<>();

		for (String agentName : preferences.keySet()) {
			Map<Direction, List<Conflict>> prefs = preferences.get(agentName);

			HashMap<Direction, Double> costs = new HashMap<>();
			for (Direction domVal : prefs.keySet()) {
				costs.put(domVal, getCostViolates(prefs.get(domVal), goodlist));
			}

			costTable.put(agentName, costs);
		}
	}
}

package pl.edu.agh.cs.kraksim.optapo.algo.agent;

import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.optapo.algo.AgentFarm;
import pl.edu.agh.cs.kraksim.optapo.algo.Solution;
import pl.edu.agh.cs.kraksim.optapo.algo.Solver;

import java.util.*;
import java.util.Map.Entry;

public class Agent {
	private static final Logger LOGGER = Logger.getLogger(Agent.class);
	private static final Random rg = new Random();
	private final AgentFarm farm;
	private final Collection<Agent> good_list = new HashSet<>();
	private final Map<String, AgentInfo> neighList = new HashMap<>();
	private final Solver solver = new Solver();
	// d_i
	private Direction value;
	private String name;
	private int priority;
	private double lowerBound = 0.0;
	private double cost = 0.0;
	private State state = State.INITIALIZE;
	//  private List<Agent>            good_list  = new ArrayList<Agent>();
	// TODO: change it
	private boolean mediate = false;
	private Mediate m = Mediate.NONE;
	private int step = 0;

	private enum Mediate {
		ACTIVE,
		PASSIVE,
		NONE
	}

	public Agent(AgentFarm farm, String name, Direction direction) {
		this.farm = farm;
		this.name = name;
		value = direction;
	}

	public boolean pulse() {
		LOGGER.trace("Agent[" + name + "].pulse()");
		boolean finished = false;

		switch (step) {
			case 0:
				checkAgentView();
				step++;
				break;

			case 1:
				checkMediate();
				step++;
				break;

			case 2:
				if (Mediate.ACTIVE.equals(m)) {
					LOGGER.trace("ACTIVE MEDIATION");
					//        System.out.println( "ACTIVE MEDIATION: " + name );
					mediate();
				}
				step++;
				// send Messages, and then receive responses....

				break;

			case 3:
				LOGGER.trace("MEDIATION 2 RECEIVING RESPONSES");
				// send Messages, and then receive responses....
				chooseSolution();
				step = 0;
				break;

			default:
				break;
		}

		return finished;
	}

	// mediate and choose solution
	private void mediate() {
		boolean trialSucceeded = tryLocalChange();
		Map<String, Map<Direction, List<Conflict>>> preferences = new HashMap<>();
		// TODO: check if local trial was success, and do not
		// mediate!!!!
		//    System.out.println( "TRIAL SUCcess: " + trialSucceded );
		if (!trialSucceeded) {
			for (Agent agent : good_list) {
				Map<Direction, List<Conflict>> conflictsMap = agent.evaluateAsk(this);

				// conflictsMap.size() == 0 means wait
				if (!conflictsMap.isEmpty()) {
					preferences.put(agent.name, conflictsMap);
				}
			}

			LOGGER.trace(preferences);

			// System.out.println( preferences );
			//      System.out.println( "PREFERENCEF ARE :" );
			//      printConflictingAgents( preferences );

			Solution solution = findSolution(preferences);

//      System.out.println( "Agent: " + name + ", solution: " + solution );
//      System.out.println( "\teffect: " + solution.getEffect() + ", cost:  "
//                          + solution.getCost() );
			for (Map.Entry<String, Direction> sol : solution.entrySet()) {
				String agentName = sol.getKey();
				Direction newValue = sol.getValue();
				if (newValue != null) {
//          System.out.println( agentName + "] " + name + "->val=" + newValue );
					farm.getAgent(agentName).acceptRequest(newValue, this);
				}
			}
		}
		//    farm.drawSituation();
//    System.out.println( "AFTER:" );
		addConflictingAgents(preferences);
	}

	private void acceptRequest(Direction newValue, Agent agent) {
		m = Mediate.NONE;
		mediate = false;
		if (!newValue.equals(value)) {
			//           System.out.println( agent.getName() + "] " + name + "->val=" + newValue );
			value = newValue;
		}
		// TODO? checkAgentView?
	}

	private void addConflictingAgents(Map<String, Map<Direction, List<Conflict>>> preferences) {
//    System.out.println( "Agent: " + name + ", good_list: " + good_list );
//    System.out.println( "Conflicts for agent: " + name );

		for (Map.Entry<String, Map<Direction, List<Conflict>>> element : preferences.entrySet()) {
//      System.out.print( element.getKey() );
			Map<Direction, List<Conflict>> agentConflicts = element.getValue();

			for (Map.Entry<Direction, List<Conflict>> dirConflicts : agentConflicts.entrySet()) {
//        System.out.println( "\t" + dirConflicts.getKey() + " -> "
//                            + dirConflicts.getValue() );
				List<Conflict> conflicts = dirConflicts.getValue();

				for (Conflict conflict : conflicts) {
					double conflictCost = conflict.getCost();
					String conflictingAgent = conflict.getName();
					if (conflictCost > 0) {
						addAgent(conflictingAgent);
					}
				}
			}
		}
	}

	private void addAgent(String conflictingAgent) {
		Agent ag = farm.getAgent(conflictingAgent);
		addAgent(ag);
		ag.addAgent(this);

//    System.out.println( "Agent.addAgent(): " + conflictingAgent );
	}

	public void addAgent(Agent a) {
		if (!a.name.equals(name)) {
			good_list.add(a);
		}
	}

	private static void printConflictingAgents(Map<String, Map<Direction, List<Conflict>>> preferences) {
//    System.out.println( "Agent: " + name + ", good_list: " + good_list );
//    System.out.println( "Conflicts for agent: " + name );
		for (Map.Entry<String, Map<Direction, List<Conflict>>> element : preferences.entrySet()) {
			String agentName = element.getKey();
			Map<Direction, List<Conflict>> agentConflicts = element.getValue();
//      System.out.print( agentName );

			for (Map.Entry<Direction, List<Conflict>> dirConflicts : agentConflicts.entrySet()) {
//        System.out.println( "\t" + dirConflicts.getKey() + " -> "
//                            + dirConflicts.getValue() );
			}
		}
	}

	private Solution findSolution(Map<String, Map<Direction, List<Conflict>>> preferences) {

		List<Agent> list = new ArrayList<>(good_list);
		list.add(this);
		Solution sol = solver.findBestSoln(farm, list, preferences, lowerBound, cost + 1.0);
		// update lowerBound, check The Effect
		lowerBound = sol.getCost();

		// System.out.println( "Agent.findSolution() COST=" +
		// sol.getCost() );
		// System.out.println( "Agent.findSolution() EFF=" +
		// sol.getEffect() );
		// System.out.println( "Agent[" + name + "].findSolution()" +
		// name + "->" + sol );
		// farm.drawSituation();

		return sol;
		// LOGGER.trace(name + "->" + sol );
	}

	private Map<Direction, List<Conflict>> evaluateAsk(Agent xj) {
		//   TODO:  cache?
		Map<Direction, List<Conflict>> valueConflictMap = new HashMap<>();
		// TODO: waitFor...
		// check if there is an agent that wants mediation, with
		// higher priority than xj
		// boolean isAnotherActive = false;
		// if ( (mediate || isAnotherActive) &&
		// (Mediate.ACTIVE.equals( xj.getM() )) ) {
		// // s
		// System.out.println( "WAIT WAIT" );
		// }
		// else {
		//    System.out.println( "Agent[" + name + "].evaluateAsk() " );
		Direction[] array = Direction.values();
		for (Direction anArray : array) {
			List<Conflict> conflicts = evaluateDirectionCost(anArray);
			valueConflictMap.put(anArray, conflicts);
		}
		// }

		//    for (Map.Entry<Direction, List<Conflict>> dirConflicts : valueConflictMap.entrySet()) {
		//      System.out.println( "\t" + dirConflicts.getKey() + " -> " + dirConflicts.getValue() );
		//    }

		return valueConflictMap;
	}

	private boolean tryLocalChange() {
		boolean trialSucceeded = false;
		if (value.equals(Direction.NS)) {
			value = Direction.WE;
			double tempCost = evaluate(this);
			LOGGER.trace("Agent.tryLocalChange() tmp:" + tempCost + " current:" + cost);
			if (tempCost > lowerBound) {
				value = Direction.NS;
			} else {
				trialSucceeded = true;
			}
		} else {
			value = Direction.NS;
			double tempCost = evaluate(this);
			LOGGER.trace("Agent.tryLocalChange() tmp:" + tempCost + " current:" + cost);
			if (tempCost > lowerBound) {

				value = Direction.WE;
			} else {
				trialSucceeded = true;
			}
		}

		return trialSucceeded;
	}

	private void chooseSolution() {
		// TODO Auto-generated method stub

	}

	// private Mediate getM() {
	// return m;
	// }

	/**
	 *
	 */
	private void checkAgentView() {
		m = Mediate.NONE;
		mediate = false;
		LOGGER.trace("Agent[" + name + "].pulse()");
		cost = evaluate(this);
		LOGGER.trace(" System COST " + cost);

		if (cost > lowerBound) {
			// TODO:
			mediate = true;
		}
	}

	// -------------------------------------------------------------------------
	// INITIALIZE
	// -------------------------------------------------------------------------
	public void init() {
		LOGGER.trace("Agent[" + name + "].init()");
		good_list.addAll(getNeighbours());
		//    System.out.println( "Agent.init() :: " + good_list );
		// d_i
		value = getInitialValue();
		// F_i*
		lowerBound = 0.0;
		// p_i
		priority = totalIncomingVehicles();
		// func = getInitialFunc();

		// good_list.add( this );

		mediate = false;
	}

	public List<Agent> getNeighbours() {
		List<Agent> result = new ArrayList<>();

		for (Entry<String, AgentInfo> agentName : neighList.entrySet()) {
			Agent agent = farm.getAgent(agentName.getKey());
			if (agent == null) {
				//        System.out.println( name + " trying to find:" + agentName + "NULL" );
			} else {
				result.add(agent);
			}
		}

		return result;
	}

	private Direction getInitialValue() {
		Direction initValue = null;
		if (value == null) {
			Direction[] dm = Direction.values();
			int i = rg.nextInt(dm.length - 1);
			initValue = dm[i];
		}
		initValue = value;
		return initValue;
	}

	private int totalIncomingVehicles() {

		Collection<AgentInfo> neigh = neighList.values();

		int total = 0;
		for (AgentInfo agent : neigh) {
			if (good_list.contains(farm.getAgent(agent.getName()))) {
				total += agent.getIncoming();
			}
		}

		return total;
	}

	private String getInitialName() {
		return name;
	}

	private void checkMediate() {
		// [[[
		if (cost > lowerBound) {
			// TODO: check this function with article
			if (isTheHighestPriority()) {
				m = Mediate.ACTIVE;
			} else {
				m = Mediate.PASSIVE;
			}
			LOGGER.trace("Agent[" + name + "].checkAgentView() MEDIATE:" + m);
			//      System.out.println( "Agent[" + name + "].checkAgentView() MEDIATE:" + m );
		}
	}

	public boolean wantsMediation() {
		return mediate;
	}

	private boolean isTheHighestPriority() {
		boolean highest = true;
		// TODO: nullpointer!!!! why
		for (Agent agent : good_list) {
			//      if ( agent == null ) {
			//        System.out.println( "Agent.isTheHighestPriority() :::" + good_list );
			//      }
			//      System.out.println( "Agenbt---" + agent );
			if (agent.wantsMediation()) {
				int prior = agent.priority;
				if (prior > priority) {
					highest = false;
				}
			}
		}

		return highest;
	}

	private int getPriority() {
		return priority;
	}

	public Collection<Agent> getGoodList() {
		return good_list;
	}

	public List<Agent> getNeighbours(Direction ns) {
		List<Agent> result = new ArrayList<>();

		for (Entry<String, AgentInfo> agentName : neighList.entrySet()) {
			if (ns.equals(agentName.getValue().getDir())) {
				LOGGER.trace("Agent[" + name + "].getNeighbours()" + ns + ' ' + agentName);
				Agent agent = farm.getAgent(agentName.getKey());
				if (agent == null) {
					LOGGER.trace("NULL " + agentName.getKey());
				} else {
					result.add(agent);
				}
			}
		}

		return result;
	}

	public Direction getValue() {
		return value;
	}

	public void setValue(Direction value) {
		this.value = value;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	// private Direction higherTraffic;

	public double evaluate(Agent agent_i) {
		double globalCost = 0;
		Collection<Agent> goodList = agent_i.good_list;

		if (incomingVehicles(Direction.NS) >= incomingVehicles(Direction.WE)) {
			// higherTraffic = Direction.NS;
			List<Agent> j_list = agent_i.getNeighbours(Direction.NS);
			LOGGER.trace("NS" + j_list);
			for (Agent agent_j : j_list) {
				if (goodList.contains(agent_j)) {
					globalCost += agent_i.relation(agent_j, Direction.NS);
				}
			}
		} else {
			// higherTraffic = Direction.WE;
			List<Agent> j_list = agent_i.getNeighbours(Direction.WE);
			LOGGER.trace("WE" + j_list);
			for (Agent agent_j : j_list) {
				if (goodList.contains(agent_j)) {
					globalCost += agent_i.relation(agent_j, Direction.WE);
				}
			}
		}

		return globalCost;
	}

	public List<Conflict> evaluateDirectionCost(Direction value) {
		// double globalCost = 0;
		Direction higherTraffic;
		//    System.out.println( "\tAgent[" + name + "].evaluateDirectionCost( " + value + " )" );
		Collection<Agent> goodList = good_list;
		List<Conflict> conflicts = new ArrayList<>();

		if (incomingVehicles(Direction.NS) >= incomingVehicles(Direction.WE)) {
			higherTraffic = Direction.NS;
			findConflictsForDir(value, higherTraffic, goodList, conflicts);
		} else {
			higherTraffic = Direction.WE;
			findConflictsForDir(value, higherTraffic, goodList, conflicts);
		}

		//    System.out.println( "\t\tConflicts: " + conflicts );
		return conflicts;
	}

	private void findConflictsForDir(Direction value, Direction higherTraffic, Collection<Agent> goodList, List<Conflict> conflicts) {
		List<Agent> j_list = getNeighbours(higherTraffic);
		for (Agent agent_j : j_list) {
			if (goodList.contains(agent_j)) {
				double tmpCost = relation(value, agent_j, higherTraffic);
				//        System.out.println( "\t\t\tRelation for agent " + agent_j.getName() + " = " + tmpCost );
				conflicts.add(new Conflict(agent_j.name, tmpCost));
			}
		}
	}

	public double relation(Agent agent_j, Direction hiTrafficDir) {
		double relationCost = getRelationCost(value, agent_j, hiTrafficDir);
		LOGGER.trace("                COST: " + relationCost);

		return relationCost;
	}

	public double relation(Direction value, Agent agent_j, Direction hiTrafficDir) {
		double relationCost = getRelationCost(value, agent_j, hiTrafficDir);
		LOGGER.trace("                COST: " + relationCost);

		return relationCost;
	}

	private double getRelationCost(Direction value, Agent agent_j, Direction hiTrafficDir) {
		double relationCost = 0;

		if (value.equals(hiTrafficDir)) {
			if (value.equals(agent_j.value)) {
				relationCost = 0;
			} else {
				int totalInc = totalIncomingVehicles();
				relationCost = ((totalInc == 0) ? 0 : (double) incomingVehiclesFrom(agent_j) / totalInc);
			}
		} else {
			int totalInc = totalIncomingVehicles();
			relationCost = ((totalInc == 0) ? 0 : 2.0 * incomingVehiclesFrom(agent_j) / totalInc);
		}

		return relationCost;
	}

	public double checkRelation(Direction value_i, Direction value_j, Agent agent_j) {
		Direction hiTrafficDir;
		if (incomingVehicles(Direction.NS) >= incomingVehicles(Direction.WE)) {
			hiTrafficDir = Direction.NS;
		} else {
			hiTrafficDir = Direction.WE;
		}

		double relationCost = 0;
		// TODO: refactor
		if (value_i.equals(hiTrafficDir)) {
			if (value_i.equals(value_j)) {
				relationCost = 0;
			} else {
				int totalInc = totalIncomingVehicles();
				relationCost = ((totalInc == 0) ? 0 : (double) incomingVehiclesFrom(agent_j) / totalInc);
			}
		} else {
			int totalInc = totalIncomingVehicles();
			relationCost = ((totalInc == 0) ? 0 : 2.0 * incomingVehiclesFrom(agent_j) / totalInc);
		}
		LOGGER.trace("                COST: " + relationCost);
		return relationCost;
	}

	private int incomingVehicles(Direction direction) {
		Collection<AgentInfo> neigh = neighList.values();
		int total = 0;
		for (AgentInfo agent : neigh) {
			if (direction.equals(agent.getDir()) && good_list.contains(farm.getAgent(agent.getName()))) {
				total += agent.getIncoming();
			}
		}

		return total;
	}

	private int incomingVehiclesFrom(Agent agent_j) {
		LOGGER.trace("Agent[" + name + "].incomingVehiclesFrom()" + agent_j);
		return neighList.get(agent_j.name).getIncoming();
	}

	public String getName() {
		return name;
	}

	public void addNeighbor(AgentInfo ag) {
		LOGGER.trace("Agent[" + name + "] adding info: " + ag);
		neighList.put(ag.getName(), ag);
	}

	public AgentInfo getNeighbor(String name) {
		return neighList.get(name);
	}

	public Collection<AgentInfo> getNeighborList() {
		return neighList.values();
	}

	@Override
	public String toString() {
		return name;
	}

	public String getChosenDirection() {
		return value.toString();
	}
}

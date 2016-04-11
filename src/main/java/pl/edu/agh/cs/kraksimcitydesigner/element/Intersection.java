package pl.edu.agh.cs.kraksimcitydesigner.element;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import pl.edu.agh.cs.kraksimcitydesigner.element.DisplaySettings;
import pl.edu.agh.cs.kraksimcitydesigner.helpers.MathHelper;

public class Intersection extends Node {
    private static Logger log = Logger.getLogger(Intersection.class);
    
    public static class ArmActions implements Comparable<ArmActions> {
        private Node arm;
        private String dir;
        private Set<Action> actions = new TreeSet<Action>();
        private Intersection intersection;
        
        /**
         * Instantiates a new arm actions.
         * 
         * @param parent the parent
         * @param armNode the arm node
         * @param dir the dir
         */
        public ArmActions(Intersection parent, Node armNode, String dir) {
            this.arm = armNode;
            this.dir = dir;
            this.intersection = parent;
        }
        
        /**
         * Instantiates a new arm actions.
         * 
         * @param org the org
         * @param parentIntersection the parent intersection
         */
        public ArmActions(ArmActions org, Intersection parentIntersection) {
            this.arm = org.arm;
            this.dir = org.dir;
            this.intersection = parentIntersection;
            for (Action action : org.getActions()) {
                this.actions.add(new Action(action, this));
            }
        }
        
        /**
         * Adds the action.
         * 
         * @param exitNode the exit node
         * @param line the line
         * 
         * @return the action
         */
        public Action addAction(Node exitNode, int line) {
            Action newAction = new Action(this,exitNode,line);
            this.actions.add(newAction);
            
            // adding traffic lights state, if it was not already set for this incoming lane
            // UPDATED it must be set for all, because Kraksim works that way
            /*
            TrafficLightsSchedule trafficLightsSchedule = intersection.getTrafficLightsSchedule();
            for (Integer phaseNum : trafficLightsSchedule.getPhases().keySet()) {
                Phase phase = trafficLightsSchedule.getPhases().get(phaseNum);
                phase.addLightStateOnIncomingLane(this.getArm(), newAction.getLineNum(), LightState.GREEN);
            }
            */
            
            return newAction;
        }
        
        /**
         * Gets the actions.
         * 
         * @return the actions
         */
        public Set<Action> getActions() {
            return actions;
        }
        
        /**
         * Gets the arm.
         * 
         * @return the arm
         */
        public Node getArm() {
            return arm;
        }
        
        /**
         * Gets the dir.
         * 
         * @return the dir
         */
        public String getDir() {
            return dir;
        }
        
        /**
         * Delete.
         * 
         * @param currentAction the current action
         */
        public void delete(Action currentAction) {
            actions.remove(currentAction);
        }
        
        /**
         * Gets the intersection.
         * 
         * @return the intersection
         */
        public Intersection getIntersection() {
            return intersection;
        }
        
        /**
         * Contain.
         * 
         * @param action the action
         * 
         * @return true, if successful
         */
        public boolean contain(Action action) {
            return actions.contains(action);
        }
        
        /**
         * Contain.
         * 
         * @param laneNum the lane num
         * @param reachableNode the reachable node
         * 
         * @return true, if successful
         */
        public boolean contain(int laneNum, Node reachableNode) {
            for (Action action : this.actions) {
                if (action.getLineNum() == laneNum && action.getExitNode() == reachableNode) {
                    return true;
                }
            }
            return false;
        }
        
        /**
         * Gets the lines nums that have action.
         * 
         * @return the lines nums that have action
         */
        public Set<Integer> getLinesNumsThatHaveAction() {
            Set<Integer> result = new HashSet<Integer>();
            for (Action action : getActions()) {
                result.add(action.getLineNum());
            }
            return result;
        }

        @Override
        public int compareTo(ArmActions o) {
            return this.getArm().getId().compareTo(o.getArm().getId());
        }

        @Override
        public boolean equals(Object obj) {
            if (! (obj instanceof ArmActions)) {
                return false;
            }
            ArmActions aa = (ArmActions) obj;
            return this.getArm().getId().equals(aa.getArm());
        }
    }
    
    public static class Action implements Comparable<Action> {
        private Node exit;
        private int line; // i think that: -1 is left, 0 center, 1 right
        private ArmActions armActionsParent;
        private Set<IncomingLane> privileged = new TreeSet<IncomingLane>();
        
        /**
         * Instantiates a new action.
         * 
         * @param armActionsParent the arm actions parent
         * @param exitNode the exit node
         * @param line2 the line2
         */
        public Action(ArmActions armActionsParent, Node exitNode, int line2) {
            this.armActionsParent = armActionsParent;
            this.exit = exitNode;
            this.line = line2;
        }

        /**
         * Instantiates a new action.
         * 
         * @param org the org
         * @param parent the parent
         */
        public Action(Action org, ArmActions parent) {
            this.exit = org.exit;
            this.line = org.line;
            this.armActionsParent = parent;
            
            // IncomingLanes are immutable
            this.privileged = new TreeSet<IncomingLane>(org.privileged);
        }

        /**
         * Removes the privileged.
         * 
         * @param privilegedLane the privileged lane
         */
        public void removePrivileged(IncomingLane privilegedLane) {
            this.privileged.remove(privilegedLane);
        }  
        
        /**
         * Gets the line num.
         * 
         * @return the line num
         */
        public int getLineNum() {
            return line;
        }
        
        /**
         * Gets the exit node.
         * 
         * @return the exit node
         */
        public Node getExitNode() {
            return exit;
        }
        
        /**
         * Gets the arm actions.
         * 
         * @return the arm actions
         */
        public ArmActions getArmActions() {
            return armActionsParent;
        }
        
        /**
         * Gets the privileged.
         * 
         * @return the privileged
         */
        public Set<IncomingLane> getPrivileged() {
            return this.privileged;
        }
        
        /**
         * Adds the privileged lane.
         * 
         * @param entranceNode the entrance node
         * @param ruleLaneNum the rule lane num
         */
        public void addPrivilegedLane(Node entranceNode, int ruleLaneNum) {
            this.privileged.add(new IncomingLane(entranceNode,ruleLaneNum));
        }

        /**
         * Contain privileged incoming lane.
         * 
         * @param newIncomingLane the new incoming lane
         * 
         * @return true, if successful
         */
        public boolean containPrivilegedIncomingLane(IncomingLane newIncomingLane) {
            for (IncomingLane incomingLane : this.privileged) {
                if (incomingLane.getFromNode() == newIncomingLane.getFromNode()
                        && incomingLane.getLaneNum() == newIncomingLane.getLaneNum()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int compareTo(Action o) {
            
            int result = this.getExitNode().getId().compareTo(o.getExitNode().getId());
            if (result == 0) {
                result = (new Integer(this.getLineNum())).compareTo(o.getLineNum());
            }
            return result;
        }
    }
    
    /**
     * Represent lane of the Link that is come into Intersection
     * It is necessary to represent <rule ...> tag, because one rule apply to more than one action.
     */
    public static class IncomingLane implements Comparable<IncomingLane> {
        
        private Node fromNode;
        private int laneNum;
        
        /**
         * Instantiates a new incoming lane.
         * 
         * @param fromNode the from node
         * @param laneNum the lane num
         */
        public IncomingLane(Node fromNode, int laneNum) {
            this.fromNode = fromNode;
            this.laneNum = laneNum;  
        }

        /**
         * Gets the from node.
         * 
         * @return the from node
         */
        public Node getFromNode() {
            return fromNode;
        }

        /**
         * Gets the lane num.
         * 
         * @return the lane num
         */
        public int getLaneNum() {
            return laneNum;
        }
 
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((fromNode == null) ? 0 : fromNode.hashCode());
            result = prime * result + laneNum;
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            IncomingLane other = (IncomingLane) obj;
            if (fromNode == null) {
                if (other.fromNode != null)
                    return false;
            } else if (!fromNode.equals(other.fromNode))
                return false;
            if (laneNum != other.laneNum)
                return false;
            return true;
        }

        @Override
        public int compareTo(IncomingLane o) {
            int result = this.getFromNode().getId().compareTo(o.getFromNode().getId());
            if (result == 0) {
                result = (new Integer(this.getLaneNum())).compareTo(o.getLaneNum());
            }
            return result;
        }
        
        
    }
    
    public static class Plan {

    }
    
    public static enum LightState {
        GREEN,YELLOW,RED;
    }
    
    public static class Phase {
        
        public static int DEFAULT_DURATION = 10;
        
        private int num;
        private String name;
        private Integer duration;
        //private TrafficLightsSchedule trafficLightSchedule;
        private Map<IncomingLane,LightState> lightsStates = new HashMap<IncomingLane, LightState>();
        
        /**
         * Instantiates a new phase.
         * 
         * @param trafficLightSchedule the traffic light schedule
         * @param num the num
         * @param name the name
         * @param duration the duration
         */
        public Phase(int num, String name, Integer duration) {
            //this.trafficLightSchedule = trafficLightSchedule;
            this.num = num;
            this.name = name;
            this.duration = duration;
        }
        
        /**
         * Instantiates a new phase.
         * 
         * @param org the org
         * @param trafficLightsSchedule the traffic lights schedule
         */
        public Phase(Phase org) {
            
            this.num = org.num;
            this.name = org.name;
            this.duration = org.duration;
            
            for (IncomingLane incomingLane : org.lightsStates.keySet()) {
                LightState lightState = org.lightsStates.get(incomingLane);
                this.updateIncomingLane(incomingLane, lightState);
            }
        }

        /**
         * Update incoming lane.
         * 
         * @param incomingLane the incoming lane
         * @param lightState the light state
         */
        public void updateIncomingLane(IncomingLane incomingLane, LightState lightState) {
            this.lightsStates.put(incomingLane, lightState);
        }

        /**
         * Adds the light state on incoming lane.
         * 
         * @param arm the arm
         * @param lane the lane
         * @param lightState the light state
         */
        public void addLightStateOnIncomingLane(Node arm, int lane,
                LightState lightState) {
            
            if (findLightStateByArmAndLane(arm,lane) == null) {
                lightsStates.put(new IncomingLane(arm,lane), lightState);
            }
        }
        
        /**
         * Adds the light state on incoming lane.
         * 
         * @param incomingLane the incoming lane
         * @param lightState the light state
         */
        public void addLightStateOnIncomingLane(IncomingLane incomingLane,
                LightState lightState) {
            
            if (!lightsStates.containsKey(incomingLane)) {
                lightsStates.put(incomingLane, lightState);
            }
        }

        /**
         * Find light state by arm and lane.
         * 
         * @param arm the arm
         * @param lane the lane
         * 
         * @return the light state
         */
        private LightState findLightStateByArmAndLane(Node arm, int lane) {
            for (IncomingLane incomingLane : lightsStates.keySet()) {
                if (incomingLane.getFromNode() == arm && incomingLane.getLaneNum() == lane) {
                    return lightsStates.get(incomingLane);
                }
            }
            return null;
        }

        /**
         * Gets the lights states.
         * 
         * @return the lights states
         */
        public Map<IncomingLane, LightState> getLightsStates() {
            LinkedHashMap<IncomingLane, LightState> result = new LinkedHashMap<IncomingLane, LightState>();
            List<IncomingLane> sortedKey = new LinkedList<IncomingLane>(lightsStates.keySet());
            Collections.sort(sortedKey);
            
            for (IncomingLane incomingLane : sortedKey) {
                result.put(incomingLane, lightsStates.get(incomingLane));
            }
            return result;
        }

        /**
         * Gets the num.
         * 
         * @return the num
         */
        public int getNum() {
            return num;
        }

        /**
         * Gets the name.
         * 
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Gets the duration.
         * 
         * @return the duration
         */
        public Integer getDuration() {
            return duration;
        }

        /**
         * Sets the num.
         * 
         * @param newNum the new num
         */
        public void setNum(int newNum) {
            this.num = newNum;
        }

        /**
         * Sets the name.
         * 
         * @param newName the new name
         */
        public void setName(String newName) {
            this.name = newName;
        }

        /**
         * Sets the duration.
         * 
         * @param duration the new duration
         */
        public void setDuration(int duration) {
            this.duration = duration;
        }
    }
    
    public class TrafficLightsSchedule {
        private Intersection intersection;
        private Map<Integer,Phase> phases = new TreeMap<Integer,Phase>();
        private List<Plan> plans = new LinkedList<Plan>();
        
        /**
         * Instantiates a new traffic lights schedule.
         * 
         * @param intersection the intersection
         */
        public TrafficLightsSchedule(Intersection intersection) {
            this.intersection = intersection;
        }
        
        /**
         * Instantiates a new traffic lights schedule.
         * 
         * @param org the org
         * @param intersection the intersection
         */
        public TrafficLightsSchedule(TrafficLightsSchedule org, Intersection intersection) {
            this.intersection = intersection;
            
            for (Integer phaseNum : org.phases.keySet()) {
                Phase phase = org.phases.get(phaseNum);
                assert phase != null;
                this.addPhase(phaseNum, new Phase(phase));
            }
        }
        
        /**
         * Adds the phase.
         * 
         * @param num the num
         * @param phase the phase
         */
        public void addPhase(int num, Phase phase) {
            this.phases.put(num,phase);
        }

        /**
         * Gets the phases.
         * 
         * @return the phases
         */
        public Map<Integer, Phase> getPhases() {
            return phases;
        }

        /**
         * Gets the plans.
         * 
         * @return the plans
         */
        public List<Plan> getPlans() {
            return plans;
        }
        
        /**
         * Gets the intersection.
         * 
         * @return the intersection
         */
        public Intersection getIntersection() {
            return intersection;
        }

        /**
         * Adds the all green phase.
         * 
         * @param num the num
         */
        public void addAllGreenPhase(int num) {
            
                String name = "phase_"+num;
                Phase newPhase = new Phase(num,name,Phase.DEFAULT_DURATION);
                for (Link incomingLink : intersection.getIncomingLinks()) {
                    for (IncomingLane incomingLane : incomingLink.getIncomingLanes()) {
                        // adding light state for all incoming lanes (Kraksim needs this)
                        newPhase.addLightStateOnIncomingLane(incomingLane, LightState.GREEN);
                    }
                }
                this.phases.put(num,newPhase);
        }

        /**
         * Try to delete unused light states.
         * 
         * @param arm the arm
         * @param lineNum the line num
         */
        public void tryToDeleteUnusedLightStates(Node arm, int lineNum) {
            for (Integer phaseNum : phases.keySet()) {
                
                Phase phase = phases.get(phaseNum);
                
                LinkedList<IncomingLane> incompingLanes = new LinkedList<IncomingLane>();
                for (IncomingLane incomingLane : phase.getLightsStates().keySet()) {
                    incompingLanes.add(incomingLane);
                }
                
                for (IncomingLane incomingLane : incompingLanes) {
                    
                }
            }
        }
    }
    
    private Set<ArmActions> armActionsList;
    private TrafficLightsSchedule trafficLightsSchedule;

	/**
	 * Instantiates a new intersection.
	 * 
	 * @param id the id
	 * @param x the x
	 * @param y the y
	 * @param ds the ds
	 */
	public Intersection(String id, int x, int y, DisplaySettings ds) {
	    super(id, x, y, ds);
	    this.armActionsList = new TreeSet<ArmActions>();
	    this.trafficLightsSchedule = new TrafficLightsSchedule(this);
	}
	
	/**
	 * Instantiates a new intersection.
	 * 
	 * @param org the org
	 */
	public Intersection(Intersection org) {
	    super((Node)org);
	    
	    this.armActionsList = new TreeSet<ArmActions>();
	    for (Intersection.ArmActions armActions : org.getArmActionsList()) {
	        this.armActionsList.add(new ArmActions(armActions,this));
	    }
	    this.trafficLightsSchedule = new TrafficLightsSchedule(org.getTrafficLightsSchedule(),this);    
	}

    /**
     * Copy intersection state.
     * 
     * @param changedIntersection the changed intersection
     */
    public void copyIntersectionState(Intersection changedIntersection) {
        this.copyNodeState(changedIntersection);
        
        this.armActionsList = changedIntersection.armActionsList;
        this.trafficLightsSchedule = changedIntersection.trafficLightsSchedule;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return String.format("Intersection id = %s, x = %d, y = %d, width = %d, height = %d",getId(),(int)getX(),(int)getY(),(int)getWidth(),(int)getHeight());
    }

    /**
     * Adds the arm actions, if armAction already exists it is removed.
     * 
     * @param armNode the arm node
     * @param dir the dir
     * 
     * @return the arm actions
     */
    public ArmActions addArmActions(Node armNode, String dir) {
        log.debug("addArmActions");
        
        ArmActions aa = new ArmActions(this,armNode, dir);
        this.armActionsList.remove(aa);
        this.armActionsList.add(aa);
        return aa;
    }

    /**
     * Gets the arm actions list.
     * 
     * @return the arm actions list
     */
    public Set<ArmActions> getArmActionsList() {
        return armActionsList;
    }

    /**
     * Gets the action by arm and lane and exit.
     * 
     * @param armNode the arm node
     * @param laneNum the lane num
     * @param exitNode the exit node
     * 
     * @return the action by arm and lane and exit
     */
    public Action getActionByArmAndLaneAndExit(Node armNode, int laneNum,
            Node exitNode) {
        
        for (ArmActions armActions : armActionsList) {
            if (armActions.getArm() == armNode) {
                for (Intersection.Action action : armActions.getActions()) {
                    if (action.getExitNode() == exitNode && action.getLineNum() == laneNum) {
                        return action;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Gets the traffic lights schedule.
     * 
     * @return the traffic lights schedule
     */
    public TrafficLightsSchedule getTrafficLightsSchedule() {
        return this.trafficLightsSchedule;
    }
   
    /* (non-Javadoc)
     * @see pl.edu.agh.cs.kraksimcitydesigner.element.Node#breakConnection(pl.edu.agh.cs.kraksimcitydesigner.element.Road)
     */
    @Override
    public void breakConnection(Road road){
        
        Link incomingLink = null;
        Link outcomingLink = null;
        if (road.getUplink() != null) {
            if (road.getUplink().getEndNode() == this) {
                incomingLink = road.getUplink();
            } else {
                outcomingLink = road.getUplink();
            }
        }
        if (road.getDownlink() != null) {
            if (road.getDownlink().getEndNode() == this) {
                incomingLink = road.getDownlink();
            } else {
                outcomingLink = road.getDownlink();
            }
        }
        this.removeIncomingLink(incomingLink);
        this.removeOutcomingLink(outcomingLink);
        this.removeIncomingLinksLanesFromTrafficLightsSchedule(incomingLink);
        
        Node otherNode = road.getOtherNode(this);

        ArmActions affectedArmActions = null;
        for (ArmActions armActions : armActionsList) {
            if (armActions.getArm() == otherNode) {
                affectedArmActions = armActions;
            }
        }
        if (affectedArmActions != null) {
            removeArmActions(affectedArmActions);
        }
        removeAllActionsThatLeadsToNode(otherNode);
        removeAllPrivilegedActionsThatComesFromNode(otherNode);

    }

    /**
     * Removes the all privileged actions that comes from node.
     * 
     * @param otherNode the other node
     */
    private void removeAllPrivilegedActionsThatComesFromNode(Node otherNode) {
        
        for (ArmActions armActions : armActionsList) {
            Iterator<Action> actionsIterator = armActions.getActions().iterator();
            while (actionsIterator.hasNext()) {
                Action action = actionsIterator.next();
                
                Iterator<IncomingLane> privilegedIncomingLanesIter = action.getPrivileged().iterator();
                while (privilegedIncomingLanesIter.hasNext()) {
                    IncomingLane privilegedIncomingLane = privilegedIncomingLanesIter.next();
                    if (privilegedIncomingLane.getFromNode() == otherNode) {
                        privilegedIncomingLanesIter.remove();
                    }
                }
            }
        }
    }

    /**
     * Removes the all actions that leads to node.
     * 
     * @param otherNode the other node
     */
    private void removeAllActionsThatLeadsToNode(Node otherNode) {
        
        for (ArmActions armActions : armActionsList) {
            Iterator<Action> actionsIterator = armActions.getActions().iterator();
            while (actionsIterator.hasNext()) {
                Action action = actionsIterator.next();
                if (action.getExitNode() == otherNode) {
                    actionsIterator.remove();
                }
            }
        }
    }

    /**
     * Removes the incoming links lanes from traffic lights schedule.
     * 
     * @param incomingLink the incoming link
     */
    private void removeIncomingLinksLanesFromTrafficLightsSchedule(
            Link incomingLink) {
        
        for (Integer phaseNum : trafficLightsSchedule.getPhases().keySet()) {
            Phase phase = trafficLightsSchedule.getPhases().get(phaseNum);
            
            for (IncomingLane lane : incomingLink.getIncomingLanes()) {
                LightState state = phase.getLightsStates().remove(lane);
                assert state != null;
            }
        }
    }

    /**
     * Removes the arm actions.
     * 
     * @param affectedArmActions the affected arm actions
     */
    private void removeArmActions(ArmActions affectedArmActions) {
        this.armActionsList.remove(affectedArmActions);
    }

    /**
     * Sets the traffic lights schedule.
     * 
     * @param trafficLightsSchedule the new traffic lights schedule
     */
    public void setTrafficLightsSchedule(TrafficLightsSchedule trafficLightsSchedule) {
        this.trafficLightsSchedule = trafficLightsSchedule;
    }

    /**
     * Checks for arm to.
     * 
     * @param connectedNode the connected node
     * 
     * @return true, if successful
     */
    public boolean hasArmTo(Node connectedNode) {
        
        for (ArmActions armActions : this.getArmActionsList()) {
            if (armActions.getArm() == connectedNode) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find incoming link by source node.
     * 
     * @param arm the arm
     * 
     * @return the link
     */
    public Link findIncomingLinkBySourceNode(Node arm) {
        for (Link incomingLink : this.getIncomingLinks()) {
            if (incomingLink.getStartNode() == arm) {
                return incomingLink;
            }
        }
        return null;
    }
    
    /**
     * Check if there are some incoming lane that doesn't have a state set for a phase.
     * If yes, it sets it to GREEN.
     */
    public void updateTrafficLights() {

        // setting lights states for all phases to GREEN
        for (Integer phaseNum : this.getTrafficLightsSchedule().getPhases().keySet()) {
            Phase phase = this.getTrafficLightsSchedule().getPhases().get(phaseNum);
            
            for (Link incomingLink : getIncomingLinks()) {
                for (IncomingLane incomingLane : incomingLink.getIncomingLanes()) {
                    if (phase.getLightsStates().get(incomingLane) == null) {
                        phase.getLightsStates().put(incomingLane, LightState.GREEN);        
                    }
                }
            }
        }  
    }
    
    /* (non-Javadoc)
     * @see pl.edu.agh.cs.kraksimcitydesigner.element.Node#addIncomingLink(pl.edu.agh.cs.kraksimcitydesigner.element.Link)
     */
    public void addIncomingLink(Link incoming) {
        super.addIncomingLink(incoming);
        
        // setting lights states for all phases to GREEN
        updateTrafficLights();
    }

    /**
     * Checks if each incoming Lane is used in some action.
     * @return
     */
    public boolean allIncomingLanesUsed() {
        
        List<IncomingLane> allIncomingLanesNotUsed = new LinkedList<IncomingLane>();
        for (Link incomingLink : this.getIncomingLinks()) {
            allIncomingLanesNotUsed.addAll(incomingLink.getIncomingLanes());
        }
        for (ArmActions armActions : this.getArmActionsList()) {
            for (Action action : armActions.getActions()) {
                IncomingLane laneUsed = new IncomingLane(armActions.getArm(),action.getLineNum());
                allIncomingLanesNotUsed.remove(laneUsed);
            }
        }
        
        if (allIncomingLanesNotUsed.size() == 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean allOutcomingLinksUsed() {
        
        List<Node> unreachableNodes = new LinkedList<Node>();
        for (Link outcomingLink : this.getOutcomingLinks()) {
            unreachableNodes.add(outcomingLink.getEndNode());
        }
        for (ArmActions armActions : this.getArmActionsList()) {
            for (Action action : armActions.getActions()) {
                unreachableNodes.remove(action.getExitNode());
            }
        }
        if (unreachableNodes.size() == 0) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Checks if all roads that are connected to this intersection are bidirectional
     * @return true if yes, false if no
     */
    public boolean connectedToBidirectionalRoads() {
        
        boolean bidirectional = true;
        
        Set<Link> outcomingLinks = this.getOutcomingLinks();
        Set<Link> incomingLinks = this.getIncomingLinks();
        
        List<Node> destinationNodes = new LinkedList<Node>();
        for (Link outcomingLink : outcomingLinks) {
            destinationNodes.add(outcomingLink.getEndNode());
        }
        for (Link incomingLink : incomingLinks) {
            Node sourceNode = incomingLink.getStartNode();
            if (! destinationNodes.contains(sourceNode)) {
                bidirectional = false;
            }
        }
        
        return bidirectional;
    }
    
    /**
     * For
     * <ul>
     *  <li>intersection with three bidirectional roads coming in where two of them are almost in straight line</li>
     *  <li>... TODO: </li>
     * </ul>
     * it creates default actions (as described in manual).
     * @return
     */
    public boolean createDefaultActions() {
        return false;
    }
    
    /**
     * Works the same as createDefaultActions() but also set light schedule.
     * @see pl.edu.agh.cs.kraksimcitydesigner.element.Intersection#createDefaultActions()
     * @return
     */
    public boolean createDefaultActionsWithLightsSchedule() {
        return createDefaultActions();
    }

    public boolean createDefaultActions3WaySimpleAngles(boolean overrideActions) {
              
        if (checkRoadsFor3WaySimple() == false) {
            return false;
        }
             
        List<Node> orderedNodes = checkAnglesFor3WaySimple();
        if (orderedNodes == null) {
            return false;
        }
        
        if (overrideActions == false && this.containsAnyActions() == true) {
            return false;
        }
        this.getArmActionsList().clear();
        
        Node leftNode = orderedNodes.get(0);
        Node downNode = orderedNodes.get(1);
        Node rightNode = orderedNodes.get(2);
        
        ArmActions leftArmActions = this.addArmActions(leftNode, "NS");
        Action leftToRightAction = leftArmActions.addAction(rightNode, 0);
        leftToRightAction.addPrivilegedLane(downNode, 0);
        leftArmActions.addAction(downNode, 0);
        
        ArmActions rightArmActions = this.addArmActions(rightNode, "NS");
        rightArmActions.addAction(leftNode, 0);
        Action rightToDownAction = rightArmActions.addAction(downNode, 0);
        rightToDownAction.addPrivilegedLane(leftNode, 0);
        
        ArmActions downArmActions = this.addArmActions(downNode, "NS");
        Action downToLeftAction = downArmActions.addAction(leftNode, 0);
        downToLeftAction.addPrivilegedLane(rightNode, 0);
        downArmActions.addAction(rightNode, 0);
        
        this.getTrafficLightsSchedule().addAllGreenPhase(1);
        
        return true;
    }
    
    /**
     * Checks if this intersection and nodes given as arguments form 3WaySimple intersection i.e. two nodes are almost in straight line and third node
     * form angle of 90 degree to this line.<br>
     * @return list of nodes in counter-clockwise order if intersection is OK, or null
     */
    public List<Node> checkAnglesFor3WaySimple() {
        
        List<Node> nodes = getReachableNodes();
        Node node1 = nodes.get(0);
        Node node2 = nodes.get(1);
        Node node3 = nodes.get(2);
        
        Node inlineNode1, inlineNode2, oppositeNode;
        if (areInLineWithMe(node1,node2)) {
            inlineNode1 = node1;
            inlineNode2 = node2;
            oppositeNode = node3;
        }
        else if (areInLineWithMe(node1,node3)) {
            inlineNode1 = node1;
            inlineNode2 = node3;
            oppositeNode = node2;
        }
        else if (areInLineWithMe(node2,node3)) {
            inlineNode1 = node2;
            inlineNode2 = node3;
            oppositeNode = node1;
        }
        else {
            //System.out.println("lack of straight");
            return null;
        }
        double angle1 = calculateAngleInDegree(oppositeNode, inlineNode1);
        double angle2 = calculateAngleInDegree(oppositeNode, inlineNode2);
        
        double half = (angle1 + angle2) / 2;
        
        /*
        System.out.println("angle1 = "+angle1);
        System.out.println("angle2 = "+angle2);
        System.out.println("half = "+half);
        */
        
        if ( (angle1 < (half - 20) || angle1 > (half + 20)) ||
             (angle2 < (half - 20) || angle2 > (half + 20))
           ) {
            return null;
        }
        
        List<Node> result = new LinkedList<Node>();

        // -y because y-values increases in wrong direction
        double[] vector_inlineNode1 = new double[] { (inlineNode1.getX()-this.getX()), -(inlineNode1.getY()-this.getY()) };
        double[] vector_oppositeNode = new double[] { (oppositeNode.getX()-this.getX()), -(oppositeNode.getY()-this.getY()) };
        
        /*
        System.out.println("Node1 x = "+inlineNode1.getX()+" y = "+inlineNode1.getY());
        System.out.println("Opposite x = "+oppositeNode.getX()+" y = "+oppositeNode.getY());
        System.out.println("this x = "+this.getX()+" y = "+this.getY());
        
        System.out.println("vector_inline x = "+vector_inlineNode1[0]+" y = "+vector_inlineNode1[0]);
        System.out.println("vector_opposite x = "+vector_oppositeNode[0]+" y = "+vector_oppositeNode[1]);
        */
        
        if (MathHelper.leftOrientation(vector_inlineNode1[0], vector_inlineNode1[1],
                vector_oppositeNode[0], vector_oppositeNode[1])) {
            //System.out.println("left orientation");
            result.add(inlineNode1);
            result.add(oppositeNode);
            result.add(inlineNode2);      
        } else {
            //System.out.println("right orientation");
            result.add(inlineNode2);
            result.add(oppositeNode);
            result.add(inlineNode1);      
        }
        
        return result;
    }
    
    /**
     * Calculate angle that is opposite 'a' side
     * @param a
     * @param b
     * @param c
     * @return
     */
    private double calculateAngle(double a, double b, double c) {
        double value = (b*b + c*c - a*a) /(2*b*c);

        if (value < -1.0) {
            value = -1.0;
        }
        if (value > 1.0) {
            value = 1.0;
        }

        //System.out.println("value = "+value);
        double angle = Math.acos(value);
        //System.out.println("angle = "+angle);
        return angle;
    }
    
    private double calculateAngleInDegree(Node node1, Node node2) {
        
        double a_distance = node1.calculateDistance(node2);
        double b_distance = this.calculateDistance(node1);
        double c_distance = this.calculateDistance(node2);
        
        /*
        System.out.println("a_distance = "+a_distance);
        System.out.println("b_distance = "+b_distance);
        System.out.println("c_distance = "+c_distance);
        */
        
        
        double angle = calculateAngle(a_distance, b_distance, c_distance);
        double angle_in_degree = (180 *angle) / Math.PI;
        
        return angle_in_degree;
    }
    
    public boolean areInLineWithMe(Node node1, Node node2) {

        double angle_in_degree = calculateAngleInDegree(node1, node2);
        //System.out.println("angle straight = "+angle_in_degree);
        if (angle_in_degree > 150) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkRoadsFor3WaySimple() {
        
        if (this.getOutcomingLinks().size() != 3) {
            //System.out.println("outcomingLinks = "+this.getOutcomingLinks().size());
            return false;
        }
        
        // checking if roads are ok
        if (this.connectedToBidirectionalRoads() == false) {
            return false;
        }
        
        // checking if there is no 'left' or 'right' lanes
        for (Link incomingLink : getIncomingLinks()) {
            for (IncomingLane incomingLane : incomingLink.getIncomingLanes()) {
                if (incomingLane.getLaneNum() == 1 || incomingLane.getLaneNum() == -1) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public boolean checkFor3WaySimpleAngles() {
        
        if (checkRoadsFor3WaySimple() == false || checkAnglesFor3WaySimple() == null) {
            return false;
        } else {
            return true;
        }
        
    }
    
    /**
     * Checks if at least one action is defined.
     * @return
     */
    public boolean containsAnyActions() {

        for (Intersection.ArmActions armActions : this.getArmActionsList()) {
            for (Intersection.Action action : armActions.getActions()) {
                return true;
            }
        }
        return false;
    }
    
    private boolean checkForNWaySimple(int numOfRoads) {
        
        if (getReachableNodes().size() != numOfRoads) {
            return false;
        }
        if (! connectedToBidirectionalRoads()) {
            return false;
        }
        // checking if there is no 'left' or 'right' lanes
        for (Link incomingLink : getIncomingLinks()) {
            for (IncomingLane incomingLane : incomingLink.getIncomingLanes()) {
                if (incomingLane.getLaneNum() == 1 || incomingLane.getLaneNum() == -1) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean checkFor4WaySimple() {
        return checkForNWaySimple(4);
    }
    
    private boolean checkFor3WaySimple() {
        return checkForNWaySimple(3);
    }

    /**
     * Creates default actions and simple traffic lights schedule (all green).
     * Actions are overridden if override parameter is set to true, but light states are always overridden.
     * @param override
     * @return
     */
    public boolean createDefaultActions4WaySimple(boolean override) {
        
        if (checkFor4WaySimple() == false) {
            System.out.println("checkFor4WaySimple = false");
            return false;
        }
        if (containsAnyActions() && !override) {
            System.out.println("containsAnyActions error");
            return false;
        }
        
        List<Node> orderedNodes = this.getOrderedReachableNodes();
        int nodesNbr = orderedNodes.size();
        
        for (int i = 0; i < nodesNbr; i++ ) {
            
            //System.out.println("adding to node");
            int leftNodeNum = (i + 1) % nodesNbr;
            int rightNodeNum = (nodesNbr + (i - 1)) % nodesNbr;
            int upNodeNum = (i + 2) % nodesNbr;
            
            Node leftNode = orderedNodes.get(leftNodeNum);
            Node rightNode = orderedNodes.get(rightNodeNum);
            Node upNode = orderedNodes.get(upNodeNum);
            Node downNode = orderedNodes.get(i);
            
            ArmActions downArmActions = this.addArmActions(downNode, "NS");
            
            downArmActions.addAction(rightNode, 0);
            
            Action downToUpAction = downArmActions.addAction(upNode, 0);
            downToUpAction.addPrivilegedLane(rightNode, 0);
            
            Action downToLeftAction = downArmActions.addAction(leftNode, 0);
            downToLeftAction.addPrivilegedLane(rightNode, 0);
            downToLeftAction.addPrivilegedLane(upNode, 0);
            
            this.getTrafficLightsSchedule().getPhases().clear();
            this.getTrafficLightsSchedule().addAllGreenPhase(1);
        }
        return true;
    }
    
    public boolean createDefaultActions3WaySimple(boolean override) {
        
        if (checkFor3WaySimple() == false) {
            System.out.println("checkFor3WaySimple = false");
            return false;
        }
        if (containsAnyActions() && !override) {
            System.out.println("containsAnyActions error");
            return false;
        }
        
        List<Node> orderedNodes = this.getOrderedReachableNodes();
        int nodesNbr = orderedNodes.size();
        
        for (int i = 0; i < nodesNbr; i++ ) {
            
            //System.out.println("adding to node");
            int leftNodeNum = (i + 1) % nodesNbr;
            int rightNodeNum = (nodesNbr + (i - 1)) % nodesNbr;
            
            Node leftNode = orderedNodes.get(leftNodeNum);
            Node rightNode = orderedNodes.get(rightNodeNum);
            Node downNode = orderedNodes.get(i);
            
            ArmActions downArmActions = this.addArmActions(downNode, "NS");
            
            downArmActions.addAction(rightNode, 0);
            
            Action downToLeftAction = downArmActions.addAction(leftNode, 0);
            downToLeftAction.addPrivilegedLane(rightNode, 0);
                      
            this.getTrafficLightsSchedule().getPhases().clear();
            this.getTrafficLightsSchedule().addAllGreenPhase(1);
        }
        return true;
    }

    /**
     * TODO: dokumentacja
     * @return
     */
    public List<Node> getOrderedReachableNodes() {
        
        final Map<Node,Double> anglesOfNodes = new HashMap<Node, Double>();
        
        List<Node> reachableNodes = getReachableNodes();
        for (Node node : reachableNodes) {
            double vector_x = node.getX()-this.getX();
            double vector_y = node.getY()-this.getY();
            
            double angle = MathHelper.calculateAngleForPoint(vector_x,vector_y);
            anglesOfNodes.put(node, angle);
        }
        
        Collections.sort(reachableNodes, new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                return anglesOfNodes.get(o1).compareTo(anglesOfNodes.get(o2));
            }
        });
        
        /*
        System.out.println("--- ordered nodes ---");
        for (Node node : reachableNodes) {
            System.out.println("node: "+node.getId());
        }
        System.out.println("------");
        */
        
        return reachableNodes;
    }

}

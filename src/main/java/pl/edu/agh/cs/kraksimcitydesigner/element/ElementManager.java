package pl.edu.agh.cs.kraksimcitydesigner.element;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import pl.edu.agh.cs.kraksimcitydesigner.element.Link.LinkType;
import pl.edu.agh.cs.kraksimcitydesigner.element.DisplaySettings;
import pl.edu.agh.cs.kraksimcitydesigner.inf.CityElement;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;


// TODO: Auto-generated Javadoc
public class ElementManager {
    private static Logger log = Logger.getLogger(ElementManager.class);

    private List<Intersection> intersections = Collections.synchronizedList(new LinkedList<Intersection>());	
    private List<Gateway> gateways = Collections.synchronizedList(new LinkedList<Gateway>());	
    //private List<Road> roads = Collections.synchronizedList(new LinkedList<Road>());
    private Set<Road> roads = Collections.synchronizedSet(new TreeSet<Road>());

    //private List<Link> links = Collections.synchronizedList(new LinkedList<Link>());
    private List<Node> nodes = Collections.synchronizedList(new LinkedList<Node>());

    private DisplaySettings displaySettings;
    private RoadsSettings roadsSettings;

    private static class Helpers {

        /**
         * Creates the link from element.
         * 
         * @param linkType the link type
         * @param link the link
         * 
         * @return the element
         */
        public static Element createLinkFromElement(Link.LinkType linkType, Link link) {
            String linkTypeStr = (linkType == Link.LinkType.UPLINK) ? "uplink" : "downlink";
            Element linkElement = new Element(linkTypeStr);

            // lefts
            for (Integer leftLength : link.getLeftLines()) {
                Element leftElement = new Element("left");
                leftElement.setAttribute("length",leftLength.toString());       
                linkElement.addContent(leftElement);
            }
            // rights
            for (Integer rightLength : link.getRightLines()) {
                Element rightElement = new Element("right");
                rightElement.setAttribute("length",rightLength.toString());       
                linkElement.addContent(rightElement);
            }
            // main
            Element mainElement = new Element("main");
            mainElement.setAttribute("length", String.valueOf(link.getLength()));
            mainElement.setAttribute("numberOfLanes", String.valueOf(link.getNumberOfLines()));           
            linkElement.addContent(mainElement);

            return linkElement;
        }
    }

    /**
     * Instantiates a new element manager.
     * 
     * @param ds the ds
     */
    public ElementManager(DisplaySettings ds, RoadsSettings rs) {
        this.displaySettings = ds;
        this.roadsSettings = rs;
    }

    /**
     * Clear.
     */
    public void clear() {
        this.gateways.clear();
        this.intersections.clear();
        this.roads.clear();
        this.nodes.clear();
    }

    /**
     * Gets the intersections.
     * 
     * @return the intersections
     */
    public List<Intersection> getIntersections() {
        return intersections;
    }

    /**
     * Gets the intersection by id.
     * 
     * @param id the id
     * 
     * @return the intersection by id
     */
    public Intersection getIntersectionById(String id) {
        for (Intersection intersection : this.intersections) {
            if (intersection.getId().equals(id)) {
                return intersection;
            }
        }
        return null;
    }

    /**
     * Gets the gateways.
     * 
     * @return the gateways
     */
    public List<Gateway> getGateways() {
        return gateways;
    }

    /**
     * Generate name.
     * 
     * @param prefix the prefix
     * @param listOfElementWithPrefix the list of element with prefix
     * 
     * @return the string
     */
    private static <E extends CityElement> String generateName(String prefix, Collection<E> listOfElementWithPrefix) {
        String name = "";
        boolean exists = true;
        int i = 0;
        while (exists) {
            name = prefix + i;
            exists = false;
            for (E element : listOfElementWithPrefix) {
                if (element.getId().equals(name)) {
                    exists = true;
                }
            }
            i++;
        }
        return name;
    }

    /**
     * Adds the gateway.
     * 
     * @param id the id
     * @param x the x
     * @param y the y
     * 
     * @return the gateway
     */
    public Gateway addGateway(String id, int x, int y) {    
        log.trace(String.format("addGateway(x=%d,y=%d)",x,y));
        id = (id == null) ? generateName("G", gateways) : id;
        Gateway newGateway = new Gateway(id,x,y,displaySettings);
        this.gateways.add(newGateway);
        this.nodes.add(newGateway);
        return newGateway;
    }

    /**
     * Adds the intersection.
     * 
     * @param id the id
     * @param x the x
     * @param y the y
     * 
     * @return the intersection
     */
    public Intersection addIntersection(String id, int x, int y) {
        log.trace(String.format("addIntersection(x=%d,y=%d)",x,y));
        id = (id == null) ? generateName("I", intersections) : id;
        Intersection newIntersection = new Intersection(id,x,y,displaySettings);
        this.intersections.add(newIntersection);
        this.nodes.add(newIntersection);
        return newIntersection;
    }

    /**
     * Adds the road.
     * 
     * @param id the id
     * @param street the street
     * @param uplink the uplink
     * @param downlink the downlink
     */
    public void addRoad(String id, String street, Link uplink, Link downlink) {
        log.trace("adding roads");
        Road newRoad = new Road(id,street,uplink,downlink);
        for (Road road : roads) {
            if (road.getFromNode().getId().equals(newRoad.getToNode().getId())
                    && road.getToNode().getId().equals(newRoad.getFromNode().getId())) {
                return;
            }
        }
        if (this.roads.add(newRoad) == true) {

            if (uplink != null) {
                uplink.getEndNode().addIncomingLink(uplink);
                uplink.getStartNode().addOutcomingLink(uplink);
                uplink.setRoad(newRoad);
            }
    
            if (downlink != null) {
                downlink.getEndNode().addIncomingLink(downlink);
                downlink.getStartNode().addOutcomingLink(downlink);
                downlink.setRoad(newRoad);
            }
        }
    }

    /**
     * Adds the road.
     * 
     * @param startNode the start node
     * @param endNode the end node
     */
    public void addRoad(Node startNode, Node endNode) {
        String id = generateName("roads_", roads);
        String street = id;

        int distanceOnMap = startNode.calculateDistance(endNode);
        Link uplink = new Link(LinkType.UPLINK,distanceOnMap,1,new LinkedList<Integer>(),new LinkedList<Integer>(),startNode,endNode,displaySettings);
        Link downlink = new Link(LinkType.DOWNLINK,distanceOnMap,1,new LinkedList<Integer>(),new LinkedList<Integer>(),startNode,endNode,displaySettings);

        addRoad(id,street,uplink,downlink);
    }

    /**
     * Gets the roads.
     * 
     * @return the roads
     */
    public Set<Road> getRoads() {
        return this.roads;
    }

    /**
     * Gets the nodes.
     * 
     * @return the nodes
     */
    public List<Node> getNodes() {
        return nodes;
    }

    /**
     * Find node by id.
     * 
     * @param arm the arm
     * 
     * @return the node
     */
    public Node findNodeById(String arm) {
        for (Node node : this.nodes) {
            if (node.getId().equals(arm)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Delete road.
     * 
     * @param road the road
     */
    public void deleteRoad(Road road){

        Link link = (road.getUplink() != null) ? (road.getUplink()) : (road.getDownlink());

        Node affectedNode1 = link.getStartNode();
        Node affectedNode2 = link.getEndNode();

        affectedNode1.breakConnection(road);
        affectedNode2.breakConnection(road);

        this.roads.remove(road);
    }

    /**
     * Delete node.
     * 
     * @param node the node
     */
    public void deleteNode(Node node) {

        Set<Road> affectedRoads = new HashSet<Road>();
        for (Link incomingLink : node.getIncomingLinks()) {
            affectedRoads.add(incomingLink.getRoad());
        }
        for (Link outcomingLink : node.getOutcomingLinks()) {
            affectedRoads.add(outcomingLink.getRoad());
        }

        for (Road affectedRoad : affectedRoads) {
            deleteRoad(affectedRoad);
        }
        this.nodes.remove(node);
        if(node instanceof Intersection) {
            this.intersections.remove(node);
        } else if (node instanceof Gateway) {
            this.gateways.remove(node);
        }
    }

    /**
     * Recalculates distances of links to Euclidean distance of Nodes.
     */
    public void recalculateDistancesOfLinks() {
        for (Road road : roads) {
            road.recalculateLinksDistances();
        }
    }

    /**
     * For intersections that has only two incoming links and two outcoming links and have not actions yet, it sets defaults actions and traffic lights schedule will be created.
     */
    public void createDefaultActionsAndTrafficSchedules() {

        for (Intersection intersection : intersections) {

            Set<Link> outcomingLinks = intersection.getOutcomingLinks();
            Set<Link> incomingLinks = intersection.getIncomingLinks();

            if ( incomingLinks.size()  == 2 && outcomingLinks.size() == 2) {

                boolean bidirectional = true;

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

                boolean hasAtLeastOneAction = false;

                for (Intersection.ArmActions armActions : intersection.getArmActionsList()) {
                    for (Intersection.Action action : armActions.getActions()) {
                        hasAtLeastOneAction = true;
                        break;
                    }
                }

                if (bidirectional && !hasAtLeastOneAction) {

                    intersection.getArmActionsList().clear();
                    Node firstNode = destinationNodes.get(0);
                    Node secondNode = destinationNodes.get(1);

                    Intersection.ArmActions armActions;

                    armActions = intersection.addArmActions(firstNode, "NS");
                    armActions.addAction(secondNode, 0);

                    armActions = intersection.addArmActions(secondNode, "NS");
                    armActions.addAction(firstNode, 0);                    

                    intersection.getTrafficLightsSchedule().addAllGreenPhase(1);
                }
            }
        }
    }

    /**
     * Fill dom.
     *
     * @param document the document
     */
    public Document modelToDocument() {
        Document document = new Document();
        ElementManager elementManager = this;


        Element root = new Element("RoadNet");
        document.addContent(root);

        // nodes
        Element nodesElement = new Element("nodes");
        for (Gateway gateway : elementManager.getGateways()) {
            Element gatewayElement = new Element("gateway");
            gatewayElement.setAttribute("id", gateway.getId());
            gatewayElement.setAttribute("x",String.valueOf((int)gateway.getX()));
            gatewayElement.setAttribute("y",String.valueOf((int)gateway.getY()));

            nodesElement.addContent(gatewayElement);
        }
        for (Intersection intersection : elementManager.getIntersections()) {
            Element intersectionElement = new Element("intersection");
            intersectionElement.setAttribute("id", intersection.getId());
            intersectionElement.setAttribute("x",String.valueOf((int)intersection.getX()));
            intersectionElement.setAttribute("y",String.valueOf((int)intersection.getY()));

            nodesElement.addContent(intersectionElement);
        }
        root.addContent(nodesElement);

        // roads
        Element roadsElement = new Element("roads");
        roadsElement.setAttribute("defaultSpeedLimit", String.valueOf(roadsSettings.getDefaultSpeedLimit()));
        for (Road road : elementManager.getRoads()) {
            Element roadElement = new Element("road");
            roadElement.setAttribute("id", road.getId());
            roadElement.setAttribute("street",road.getStreet());
            roadElement.setAttribute("from",road.getUplink().getStartNode().getId());
            roadElement.setAttribute("to",road.getUplink().getEndNode().getId());

            Link uplink = road.getUplink();
            Link downlink = road.getDownlink();
            if (uplink != null) {
                Element uplinkElement = Helpers.createLinkFromElement(Link.LinkType.UPLINK, uplink);
                roadElement.addContent(uplinkElement);
            }
            if (downlink != null) {
                Element downlinkElement = Helpers.createLinkFromElement(Link.LinkType.DOWNLINK, downlink);
                roadElement.addContent(downlinkElement);
            }
            roadsElement.addContent(roadElement);
        }
        root.addContent(roadsElement);

        // intersectionDescriptions
        Element intersectionDescriptionsElement = new Element("intersectionDescriptions");
        for (Intersection intersection : elementManager.getIntersections()) {
            Element intersectionElement = new Element("intersection");
            intersectionElement.setAttribute("id", intersection.getId());

            for (Intersection.ArmActions armActions : intersection.getArmActionsList()) {
                Element armActionsElement = new Element("armActions");
                armActionsElement.setAttribute("arm",armActions.getArm().getId());
                armActionsElement.setAttribute("dir",armActions.getDir());

                for (Intersection.Action action : armActions.getActions()) {
                    Element actionElement = new Element("action");
                    actionElement.setAttribute("lane", String.valueOf(action.getLineNum()));
                    actionElement.setAttribute("exit", action.getExitNode().getId());

                    for (Intersection.IncomingLane rule : action.getPrivileged()) {
                        Element ruleElement = new Element("rule");
                        ruleElement.setAttribute("entrance", rule.getFromNode().getId());
                        ruleElement.setAttribute("lane", String.valueOf(rule.getLaneNum()));

                        actionElement.addContent(ruleElement);
                    }
                    armActionsElement.addContent(actionElement);
                }
                intersectionElement.addContent(armActionsElement);               
            }
            // trafficLightsSchedule
            Element trafficLightsScheduleElement = new Element("trafficLightsSchedule");
            Map<Integer, Intersection.Phase> intersectionPhases = intersection.getTrafficLightsSchedule().getPhases();
            for (Integer phaseNum : intersectionPhases.keySet()) {

                Intersection.Phase phase = intersectionPhases.get(phaseNum);
                Element phaseElement = new Element("phase");
                phaseElement.setAttribute("num", String.valueOf(phase.getNum()));
                phaseElement.setAttribute("name", phase.getName());
                phaseElement.setAttribute("duration", String.valueOf(phase.getDuration()));

                for(Intersection.IncomingLane inlaneIncomingLane : phase.getLightsStates().keySet()) {
                    Intersection.LightState inlaneLightState = phase.getLightsStates().get(inlaneIncomingLane);
                    assert inlaneLightState != null;
                    Element inlaneElement = new Element("inlane");
                    inlaneElement.setAttribute("arm", inlaneIncomingLane.getFromNode().getId());
                    inlaneElement.setAttribute("lane", String.valueOf(inlaneIncomingLane.getLaneNum()));
                    inlaneElement.setAttribute("state", inlaneLightState.toString().toLowerCase());

                    phaseElement.addContent(inlaneElement);
                }

                trafficLightsScheduleElement.addContent(phaseElement);
            }
            intersectionElement.addContent(trafficLightsScheduleElement);          
            intersectionDescriptionsElement.addContent(intersectionElement);
        }
        root.addContent(intersectionDescriptionsElement);

        return document;
    }

    public int createDefaultActions3WaySimpleForIntersections() {
        
        int changed = 0;
        for (Intersection intersection : intersections) {
            boolean ok = intersection.createDefaultActions3WaySimple(false);
            if (ok == true) {
                changed++;
            }
        }
        return changed;
    }
    
    public int createDefaultActions4WaySimpleForIntersections() {
        
        int changed = 0;
        for (Intersection intersection : intersections) {
            boolean ok = intersection.createDefaultActions4WaySimple(false);
            if (ok == true) {
                changed++;
            }
        }
        return changed;
    }

    public void setDisplaySettings(DisplaySettings displaySettings) {
        this.displaySettings = displaySettings;
    }

    public DisplaySettings getDisplaySettings() {
        return this.displaySettings;
    }

    public RoadsSettings getRoadsSettings() {
        return roadsSettings;
    }
}

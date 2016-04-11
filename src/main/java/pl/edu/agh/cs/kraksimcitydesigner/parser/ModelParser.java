package pl.edu.agh.cs.kraksimcitydesigner.parser;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import pl.edu.agh.cs.kraksimcitydesigner.element.DisplaySettings;
import pl.edu.agh.cs.kraksimcitydesigner.element.ElementManager;
import pl.edu.agh.cs.kraksimcitydesigner.element.Intersection;
import pl.edu.agh.cs.kraksimcitydesigner.element.Link;
import pl.edu.agh.cs.kraksimcitydesigner.element.Node;
import pl.edu.agh.cs.kraksimcitydesigner.element.Intersection.LightState;
import pl.edu.agh.cs.kraksimcitydesigner.element.Intersection.TrafficLightsSchedule;
import pl.edu.agh.cs.kraksimcitydesigner.element.Link.LinkType;

// TODO: Auto-generated Javadoc
public class ModelParser {
    private static Logger log = Logger.getLogger(ModelParser.class);
    
    public static class Util {
        
        /**
         * Gets the children.
         * 
         * @param element the element
         * 
         * @return the children
         */
        @SuppressWarnings("unchecked")
        public static List<Element> getChildren(Element element) {
            return element.getChildren();
        }
        
        /**
         * Gets the children by name.
         * 
         * @param element the element
         * @param name the name
         * 
         * @return the children by name
         */
        @SuppressWarnings("unchecked")
        public static List<Element> getChildrenByName(Element element,String name) {
            return element.getChildren(name);
        }
    }

    /**
     * Parses the.
     * 
     * @param em the em
     * @param file the file
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ParsingException the parsing exception
     */
    public static void parse(ElementManager em, File file) throws IOException, ParsingException {
      
      SAXBuilder builder = new SAXBuilder();
      //builder.setValidation(true);
      builder.setIgnoringElementContentWhitespace(true);
      try {
          Document doc = builder.build(file);
          Element root = doc.getRootElement();
          Element nodes = root.getChild("nodes");
          for (Object element : nodes.getChildren()) {
              Element el = (Element) element;
              String id = el.getAttributeValue("id");
              int x = Integer.parseInt(el.getAttributeValue("x"));
              int y = Integer.parseInt(el.getAttributeValue("y"));
              log.debug("creating ("+el.getName()+") "+id+" "+x+","+y);
              if (el.getName().equals("intersection")) {
                  em.addIntersection(id, x, y);
              } else if (el.getName().equals("gateway")) {
                  em.addGateway(id, x, y);
              }
          }
          Element roads = root.getChild("roads");
          for (Element road : Util.getChildren(roads)) {
              String id = road.getAttributeValue("id");
              String street = road.getAttributeValue("street");
              String from = road.getAttributeValue("from");
              String to = road.getAttributeValue("to");
              
              if (from.equals(to)) {
                  continue;
              }
              
              Node fromNode = em.findNodeById(from);
              Node toNode = em.findNodeById(to);
              if (fromNode == null || toNode == null) {
                  throw new ParsingException();
              }
              
              Link uplink = null;
              Link downlink = null;
              
              for (Element linkElement : Util.getChildren(road)) {                 
                  if (linkElement.getName().equals("uplink")) {
                      uplink = createLink(linkElement,LinkType.UPLINK,fromNode,toNode,em.getDisplaySettings());
                      //toNode.addIncomingLink(uplink);
                  }
                  else if (linkElement.getName().equals("downlink")) {
                      downlink = createLink(linkElement,LinkType.DOWNLINK,fromNode,toNode,em.getDisplaySettings());
                      //fromNode.addIncomingLink(downlink);
                  }
              }
              if (uplink == null && downlink == null) {
                  throw new ParsingException();
              }
              em.addRoad(id,street,uplink,downlink);
          }
          
          Element intersectionDescriptions = root.getChild("intersectionDescriptions");
          for (Object intersectionObj : intersectionDescriptions.getChildren("intersection")) {
              
              Element intersectionEl = (Element) intersectionObj;
              log.debug("intersectionEl");
              String id = intersectionEl.getAttributeValue("id");
              Intersection intersection = em.getIntersectionById(id);
              if (intersection == null) {
                  throw new ParsingException("Intersection id not found");
              }
              for (Object armActionsObj : intersectionEl.getChildren("armActions")) {
                  Element armActionsEl = (Element) armActionsObj;
                  String arm = armActionsEl.getAttributeValue("arm");
                  String dir = armActionsEl.getAttributeValue("dir");
                  Node armNode = em.findNodeById(arm);
                  if (armNode == null) {
                      throw new ParsingException("Arm not found");
                  }
                  Intersection.ArmActions armActions = intersection.addArmActions(armNode,dir);
                  
                  for (Object actionObj : armActionsEl.getChildren("action")) {
                      Element actionEl = (Element) actionObj;
                      int line = Integer.parseInt(actionEl.getAttributeValue("lane"));
                      String exitId = actionEl.getAttributeValue("exit");
                      Node exitNode = em.findNodeById(exitId);
                      armActions.addAction(exitNode,line);
                      // can't add privileged actions yet because not all have been created
                  }
              }
              
              for (Element armActionsEl : Util.getChildrenByName(intersectionEl, "armActions")) {
                  for (Element actionEl : Util.getChildrenByName(armActionsEl, "action")) {
                      if (actionEl.getChildren().size() > 0){
                          String arm = armActionsEl.getAttributeValue("arm");
                          String exit = actionEl.getAttributeValue("exit");
                          
                          Node armNode = em.findNodeById(arm);
                          Node exitNode = em.findNodeById(exit);
                          int laneNum = Integer.parseInt(actionEl.getAttributeValue("lane"));
                          
                          Intersection.Action action = intersection.getActionByArmAndLaneAndExit(armNode,laneNum,exitNode);
                          for (Element ruleEl : Util.getChildrenByName(actionEl, "rule")) {
                              String entrance = ruleEl.getAttributeValue("entrance");
                              int ruleLaneNum = Integer.parseInt(ruleEl.getAttributeValue("lane"));
                              Node entranceNode = em.findNodeById(entrance);
                              
                              /*
                              Link fromEntranceNodeToIntersectionLink = em.findLinkConnecting(entranceNode,intersection);                   
                              if (  (ruleLaneNum == -1 && fromEntranceNodeToIntersectionLink.getLeftLines() == null) 
                                 || (ruleLaneNum == 1 && fromEntranceNodeToIntersectionLink.getRightLines() == null)) {
                                  throw new ParsingException("Rule that affect non existing lane");
                              }
                              */
                              
                              //Intersection.Action actionRule = intersection.getActionByArmAndLane(entranceNode,ruleLaneNum);
                              action.addPrivilegedLane(entranceNode,ruleLaneNum);    
                          }
                      }
                  }
              }
              
              // LIGHT TRAFFIC
              Element trafficLightsScheduleEl = intersectionEl.getChild("trafficLightsSchedule");
              TrafficLightsSchedule trafficLightSchedule = intersection.new TrafficLightsSchedule(intersection);
              for (Element phaseEl : Util.getChildrenByName(trafficLightsScheduleEl, "phase")) {
                  int num = Integer.parseInt(phaseEl.getAttributeValue("num")); // stored in two places
                  String name = phaseEl.getAttributeValue("name");
                  
                  String durationStr = phaseEl.getAttributeValue("duration");
                  Integer duration = null;
                  if (durationStr != null) {
                      duration = Integer.parseInt(durationStr);
                  }
                  Intersection.Phase phase = new Intersection.Phase(num,name,duration);
                  for (Element inlaneEl : Util.getChildrenByName(phaseEl, "inlane")) {
                      int lane = Integer.parseInt(inlaneEl.getAttributeValue("lane"));
                      String armId = inlaneEl.getAttributeValue("arm");
                      Node arm = em.findNodeById(armId);
                      LightState lightState = inlaneEl.getAttributeValue("state").equals("green") ? LightState.GREEN : LightState.RED;
                      
                      phase.addLightStateOnIncomingLane(arm,lane,lightState);
                  }
                  trafficLightSchedule.addPhase(num, phase);
              }
              intersection.setTrafficLightsSchedule(trafficLightSchedule);
          }     
      }
      catch (JDOMException e) {
          e.printStackTrace();
          throw new ParsingException();
      }
  }
  /*
  public static void main (String[] args) {
      try {
          parse(null,"trafficConfigs/model-D.xml");
      }
      catch (Exception e) {
          e.printStackTrace();
      }
      System.out.println("Tutaj");
      
  }
  */

    /**
   * Creates the link.
   * 
   * @param linkElement the link element
   * @param linkType the link type
   * @param startNode the start node
   * @param endNode the end node
   * 
   * @return the link
   */
  private static Link createLink(Element linkElement, LinkType linkType, Node startNode, Node endNode, DisplaySettings displaySettings) {
        Element main = (Element) linkElement.getChild("main");
        int length = Integer.valueOf(main.getAttributeValue("length"));
        int numberOfLanes = Integer.valueOf(main.getAttributeValue("numberOfLanes"));
        LinkedList<Integer> leftLines = new LinkedList<Integer>();
        for (Object leftObject : linkElement.getChildren("left")) {
            leftLines.add(Integer.valueOf(((Element)leftObject).getAttributeValue("length")));
        }
        LinkedList<Integer> rightLines = new LinkedList<Integer>();
        for (Object rightObject : linkElement.getChildren("right")) {
            rightLines.add(Integer.valueOf(((Element)rightObject).getAttributeValue("length")));
        }
        return new Link(linkType,length,numberOfLanes,leftLines,rightLines,startNode,endNode,displaySettings);
    }
}


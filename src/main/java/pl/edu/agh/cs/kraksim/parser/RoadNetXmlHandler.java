package pl.edu.agh.cs.kraksim.parser;

import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import pl.edu.agh.cs.kraksim.KraksimRuntimeException;
import pl.edu.agh.cs.kraksim.core.*;
import pl.edu.agh.cs.kraksim.core.exceptions.DuplicateIdentifierException;
import pl.edu.agh.cs.kraksim.core.exceptions.InvalidActionException;
import pl.edu.agh.cs.kraksim.core.exceptions.LinkAttachmentException;
import pl.edu.agh.cs.kraksim.core.exceptions.UnsupportedLinkOperationException;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Zmiany 080115:
 * - dodano parsowanie numberOfLanes (funkcja createLane)
 *
 * @author Lukasz Dziewonski
 */
public class RoadNetXmlHandler extends DefaultHandler {
	private static final Logger LOGGER = Logger.getLogger(RoadNetXmlHandler.class);

	private enum Level {
		INIT, ROADS, ROAD, UPLINK, DOWNLINK, LEFT, CENTER, RIGHT, NODES, INTERSECTIONS, INTERSECTION, ARM_ACTIONS, ACTION, ACTION_RULE, TRAFFIC_LIGHTS_SCHEDULE, PHASE_LEVEL, TIMING_PLAN_LEVEL
	}

	private Level level = Level.INIT;

	private Stack<RoadInfo> roadStack = null;
	private List<Integer> leftLaneLenTab;
	private List<Integer> rightLaneLenTab;
	private int mainLaneLen;
	/**
	 * @author Lukasz Dziewonski
	 */
	private int numberOfLanes;
	private String lastLaneType;	// type of last seen lane, values: main/left/right
	private Map<String, Map<String, List<Integer>>> linkBlockedCellsInfo;	// map with blocked cells in format main/left/right -> <lane_number> -> <num_of_cell>, important only during line creation
			//	road_type -> lane_num -> list_of_blicked_cells

	private Core core;
	private City city;
	private Phase phase;
	private PhaseTiming phaseTiming;

	private String intersectionName;
	private String armFromName;

	private final List<Lane> lanes = new LinkedList<>();
	private Link il = null;
	//	Lane lane = null;
	private Link ol = null;
	private LinkedList<Lane> ll = new LinkedList<>();
	private LinkedList<Phase> phasesSet;
	private List<PhaseTiming> timingPlan;

	private String timingPlanName;
	//  private Locator         locator;
	private int defaultSpeedLimit = 2;

	private final ZoneSupervisor zoneSuperviser = new ZoneSupervisor();
	//  private String      trafficPlanName;

	public RoadNetXmlHandler() {
	}

	@Override
	public void startDocument() {
		roadStack = new Stack<>();
		leftLaneLenTab = new ArrayList<>();
		rightLaneLenTab = new ArrayList<>();
		linkBlockedCellsInfo = new HashMap<>();
		core = new Core();
		city = core.getCity();
	}

	private String outAttribs(Attributes attrs) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < attrs.getLength(); i++) {
			sb.append(attrs.getValue(i));
		}
		return sb.toString();
	}

	@Override
	public void startElement(String namespaceURI, String localName, String rawName, Attributes attrs) {
		LOGGER.trace(level + " -> " + localName + ' ' + outAttribs(attrs));
		//System.out.println("rawName " + rawName + "level " + level.toString());
		switch (level) {
			case ROADS:
				createRoad(rawName, attrs);
				break;

			case ROAD: {
				if (rawName.equals("downlink")) {
					level = Level.DOWNLINK;
				}
				if (rawName.equals("uplink")) {
					level = Level.UPLINK;
				}
			}
			break;

			case UPLINK:
				if(rawName.equals("blocked")) {
					createLaneBlockCells(rawName, attrs);
				} else {
					createLane(rawName, attrs);
				}
				break;

			case DOWNLINK:
				createLane(rawName, attrs);
				break;

			case NODES: {
				if (rawName.equals("gateway")) {
					createGatewayNode(attrs);
				}

				if (rawName.equals("intersection")) {
					createIntersectionNode(attrs);
				}
			}
			break;

			case INTERSECTIONS:
				createIntersectionDescription(rawName, attrs);
				break;

			case INTERSECTION: {
				if (rawName.equals("armActions")) {
					createArmAction(attrs);
				}

				if (rawName.equals("trafficLightsSchedule")) {
					createTrafficLightsSchedule(attrs);
				}
			}
			break;

			case ARM_ACTIONS:
				createAction(rawName, attrs);
				break;

			case ACTION:
				createRule(rawName, attrs);
				break;

			case TRAFFIC_LIGHTS_SCHEDULE:
				if (rawName.equals("plan")) {
					createTrafficPlan(attrs);
				}

				if (rawName.equals("phase")) {
					createPhase(attrs);
				}
				break;

			case TIMING_PLAN_LEVEL:
				if (rawName.equals("phase")) {
					createTrafficPhaseTiming(attrs);
				}
				break;

			case PHASE_LEVEL:
				createInlaneState(attrs);
				break;

			default: {
				// jeszcze nie ustalony stan
				if (rawName.equals("roads")) {
					level = Level.ROADS;
					String speedLimit = attrs.getValue("defaultSpeedLimit");
					defaultSpeedLimit = (speedLimit == null) ? 2 : Integer.parseInt(speedLimit);
				}

				if (rawName.equals("nodes")) {
					level = Level.NODES;
				}

				if (rawName.equals("intersectionDescriptions")) {
					level = Level.INTERSECTIONS;
				}

				if (rawName.equals("RoadNet")) {
					// Just the beggining of the Road Network Document
				}
			}
			break;
		}
	}

	// ===================================================
	// STARTing ELEMENTs
	// ===================================================
	private void createInlaneState(Attributes attrs) {
		String arm = attrs.getValue("arm");
		int lane = Integer.parseInt(attrs.getValue("lane"));
		boolean green = (attrs.getValue("state").equals("green"));

		phase.addConfiguration(arm, lane, green);
	}

	private void createTrafficPhaseTiming(Attributes attrs) {
		String name = attrs.getValue("name");
		int phaseId = Integer.parseInt(attrs.getValue("num"));
		int phaseDuration = Integer.parseInt(attrs.getValue("duration"));

		phaseTiming = new PhaseTiming(phaseId, name, phaseDuration);
	}

	private void createTrafficPlan(Attributes attrs) {
		level = Level.TIMING_PLAN_LEVEL;
		String name = attrs.getValue("name");
		timingPlan = new LinkedList<>();
		timingPlanName = name;
	}

	private void createPhase(Attributes attrs) {
		String name = attrs.getValue("name");
		level = Level.PHASE_LEVEL;
		int phaseId = Integer.parseInt(attrs.getValue("num"));
		// optapo
		String dir = attrs.getValue("syncDir");
		phase = new Phase(name, phaseId, dir);
	}

	private void createTrafficLightsSchedule(Attributes attrs) {
		level = Level.TRAFFIC_LIGHTS_SCHEDULE;
		phasesSet = new LinkedList<>();
	}

	/**
	 * @param rawName
	 * @param attrs
	 */
	private void createRule(String rawName, Attributes attrs) {
		String nodeNme = "";
		try {
			if (rawName.equals("rule")) {

				nodeNme = attrs.getValue("entrance");
				Link ilTmp = city.findLink(nodeNme + intersectionName);

				if (ilTmp == null) {
					LOGGER.error("cannot find Link " + nodeNme + intersectionName);
				}
				int laneNr = Integer.parseInt(attrs.getValue("lane"));

				if (laneNr < 0) {
					ll.add(ilTmp.getLeftLane(Math.abs(laneNr) - 1));
				} else if (laneNr > 0) {
					ll.add(ilTmp.getRightLane(laneNr - 1));
				} else if (laneNr == 0) {
					for (int i = 0; i < ilTmp.mainLaneCount(); i++) {
						ll.add(ilTmp.getMainLane(i));
					}
				}
				// System.out.println("ACTION " + rawName);
			}
		} catch (Exception e) {
			LOGGER.error(nodeNme + intersectionName, e);
		}
	}

	/**
	 * @param rawName
	 * @param attrs
	 */
	private void createAction(String rawName, Attributes attrs) {
		if (rawName.equals("action")) {
			try {
				level = Level.ACTION;
				String armToName = attrs.getValue("exit");
				int laneNr = Integer.parseInt(attrs.getValue("lane"));
				String linkName = intersectionName + armToName;

				ol = city.findLink(linkName);
				lanes.clear();
				if (laneNr < 0) {
//					lane = il.getLeftLane(laneNr + 1);
					lanes.add(il.getLeftLane(Math.abs(laneNr) - 1));
				} else if (laneNr > 0) {
//					lane = il.getRightLane(laneNr - 1);
					lanes.add(il.getRightLane(laneNr - 1));
				} else if (laneNr == 0) {
					// TODO: LDZ WIELEPASOW!!!
//					lane = il.getMainLane(laneNr);
					for (int i = 0; i < il.mainLaneCount(); i++) {
						lanes.add(il.getMainLane(i));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			// System.out.println("ARM_ACTIONS_LEVEL " + rawName);
		}
	}

	/**
	 * @param attrs
	 */
	private void createArmAction(Attributes attrs) {
		level = Level.ARM_ACTIONS;
		armFromName = attrs.getValue("arm");// TODO, zmienic
		String direction = attrs.getValue("dir");
		String linkName = armFromName + intersectionName;
		// rozbic na X1 x2, i zawsze to ktore jest rowne intersection
		// id,
		// bedzie na kocu, czyli drugie
		il = city.findLink(linkName);

		try {
			il.setDirection(direction);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// armFrom = intersection.inboundLinkIterator();
		// il = armFrom.getInboundLink();
		// System.out.println("INTERSECTION " + rawName);
	}

	/**
	 * @param rawName
	 * @param attrs
	 */
	private void createIntersectionDescription(String rawName, Attributes attrs) {
		if (rawName.equals("intersection")) {
			level = Level.INTERSECTION;
			intersectionName = attrs.getValue("id");
			city.findNode(intersectionName);
		}
	}

	/**
	 * @param attrs
	 */
	private void createIntersectionNode(Attributes attrs) {
		String id = attrs.getValue("id");
		Double x = Double.parseDouble(attrs.getValue("x"));
		Double y = Double.parseDouble(attrs.getValue("y"));
		try {
			// Intersection is =
			city.createIntersection(id, new Point2D.Double(x, y));
		} catch (DuplicateIdentifierException e) {
			e.printStackTrace();
		}

		// level = Level.INTERSECTION_LEVEL;
		// System.out.println("NODES " + rawName);
	}

	/**
	 * @param attrs
	 */
	private void createGatewayNode(Attributes attrs) {
		String id = attrs.getValue("id");
		Double x = Double.parseDouble(attrs.getValue("x"));
		Double y = Double.parseDouble(attrs.getValue("y"));
		try {
			// Gateway gw =
			city.createGateway(id, new Point2D.Double(x, y));
		} catch (DuplicateIdentifierException e) {
			e.printStackTrace();
		}
		// level = Level.GATEWAY_LEVEL;
		// System.out.println("NODES " + rawName);
	}

	/**
	 * @param rawName
	 * @param attrs
	 */
	private void createLane(String rawName, Attributes attrs) {
		if (rawName.equals("main")) {
			lastLaneType = "main";
			mainLaneLen = Integer.parseInt(attrs.getValue("length"));
			String numberOfLanesStr = attrs.getValue("numberOfLanes");
			numberOfLanes = Integer.parseInt(numberOfLanesStr == null ? "1" : numberOfLanesStr);
			
		}
		if (rawName.equals("left")) {
			lastLaneType = "left";
			leftLaneLenTab.add(Integer.parseInt(attrs.getValue("length")));
		}
		if (rawName.equals("right")) {
			lastLaneType = "right";
			rightLaneLenTab.add(Integer.parseInt(attrs.getValue("length")));
		}
	}
	
	// parse info about block cells in this.lastLaneType lane
	private void createLaneBlockCells(String rawName, Attributes attrs) {
		if(rawName.equals("blocked")) {
			String laneNumber = attrs.getValue("laneNumber");
			Integer cellNumber = Integer.parseInt(attrs.getValue("cell"));
			
			if(laneNumber == null || cellNumber == null || cellNumber >= mainLaneLen) {
				LOGGER.error("wrong cell blocking details, ignoring one entry");
			} else {
				Map<String, List<Integer>> blockedCellsInfoList = this.linkBlockedCellsInfo.get(this.lastLaneType); // laneNumber + ":" + cellNumber);
				if(blockedCellsInfoList == null) {
					ArrayList<Integer> cellList = new ArrayList<Integer>();
					cellList.add(cellNumber);
					HashMap<String, List<Integer>> laneNumMap = new HashMap<>();
					laneNumMap.put(laneNumber, cellList);
					this.linkBlockedCellsInfo.put(this.lastLaneType, laneNumMap);
				} else {
					List<Integer> cellList = blockedCellsInfoList.get(laneNumber);
					if(cellList == null) {
						cellList = new ArrayList<>();
						cellList.add(cellNumber);
						blockedCellsInfoList.put(laneNumber, cellList);
					} else {
						cellList.add(cellNumber);
					}
				}
			}
		}	
	}

	/**
	 * @param rawName
	 * @param attrs
	 */
	private void createRoad(String rawName, Attributes attrs) {
		if (rawName.equals("road")) {
			String id = attrs.getValue("id");
			String street = attrs.getValue("street");
			String from = attrs.getValue("from");
			String to = attrs.getValue("to");
			String limit = attrs.getValue("speedLimit");
			String minimalSpeedStr = attrs.getValue("minimalSpeed");
			String zoneName = attrs.getValue("zone");
			//System.out.println("id " + id + " street " + street + " limit " + limit + " minimalSpeedStr " + minimalSpeedStr + " zoneName " + zoneName);

			zoneName = (zoneName == null) ? "" : zoneName;
			int speedLimit = (limit == null) ? defaultSpeedLimit : Integer.parseInt(limit);
			double minimalSpeed = (minimalSpeedStr == null) ? 0.0 : Double.parseDouble(minimalSpeedStr);
			Node fromNode = city.findNode(from);
			Node toNode = city.findNode(to);

			if (fromNode == null) {
				throw new KraksimRuntimeException("Bad Model, node " + from + " not found");
			}

			if (toNode == null) {
				throw new KraksimRuntimeException("Bad Model, node " + to + " not found");
			}

			roadStack.push(new RoadInfo(street, fromNode, toNode, speedLimit, minimalSpeed, zoneSuperviser.getZoneInfo(zoneName)));
			level = Level.ROAD;
		}
	}

	// ===================================================
	// STARTing ELEMENTs - end block
	// ===================================================

	/**
	 * End element.
	 */
	@Override
	public void endElement(String namespaceURI, String localName, String rawName) {
		switch (level) {
			case ROADS:
				if (rawName.equals("roads")) {
					level = Level.INIT;
				}
				break;

			case ROAD:
				if (rawName.equals("road")) {
					roadStack.pop();
					level = Level.ROADS;
				}
				break;

			case UPLINK:
				if (rawName.equals("uplink")) {
					int[] l = new int[leftLaneLenTab.size()];
					int[] r = new int[rightLaneLenTab.size()];
					for (int i = 0; i < r.length; i++) {
						r[i] = rightLaneLenTab.get(i);
					}
					for (int i = 0; i < l.length; i++) {
						l[i] = leftLaneLenTab.get(i);
					}
					RoadInfo ri = roadStack.peek();
					try {
						city.createLink(ri, l, mainLaneLen, numberOfLanes, r, this.linkBlockedCellsInfo);
					} catch (DuplicateIdentifierException | IllegalArgumentException | LinkAttachmentException e) {
						e.printStackTrace();
					}

					leftLaneLenTab = new ArrayList<>();
					rightLaneLenTab = new ArrayList<>();
					this.linkBlockedCellsInfo = new HashMap<>();
					level = Level.ROAD;
				}
				break;

			case DOWNLINK:
				if (rawName.equals("downlink")) {
					int[] l = new int[leftLaneLenTab.size()];
					int[] r = new int[rightLaneLenTab.size()];
					for (int i = 0; i < r.length; i++) {
						r[i] = rightLaneLenTab.get(i);
					}
					for (int i = 0; i < l.length; i++) {
						l[i] = leftLaneLenTab.get(i);
					}
					RoadInfo ri = roadStack.peek();
					try {
						ri.setReversed();
						city.createLink(ri, l, mainLaneLen, numberOfLanes, r, this.linkBlockedCellsInfo);
					} catch (DuplicateIdentifierException | LinkAttachmentException | IllegalArgumentException e) {
						e.printStackTrace();
					}

					leftLaneLenTab = new ArrayList<>();
					rightLaneLenTab = new ArrayList<>();
					this.linkBlockedCellsInfo = new HashMap<>();
					level = Level.ROAD;
				}
				break;

			case NODES:
				if (rawName.equals("nodes")) {
					level = Level.INIT;
				}
				break;

		/*
		 * case Level.GATEWAY_LEVEL: if (rawName.equals("gateway")) {
		 * //System.out.println("END GATEWAY_LEVEL " + rawName); level =
		 * Level.NODES; } break;
		 *//*
		 * case Level.INTERSECTION_LEVEL: if
		 * (rawName.equals("intersection")) { //System.out.println("END
		 * INTERSECTION_LEVEL " + rawName); level =
		 * Level.NODES; } break;
		 */
			case INTERSECTIONS:
				if (rawName.equals("intersectionDescriptions")) {
					level = Level.INIT;
				}
				break;

			case INTERSECTION:
				if (rawName.equals("intersection")) {
					level = Level.INTERSECTIONS;
				}
				break;

			case TRAFFIC_LIGHTS_SCHEDULE:
				//      if ( rawName.equals( "plan" ) ) {
				//        city.findNode( intersectionName ).addTrafficLightsPlan( trafficPlanName, schedule );
				//        schedule = new LinkedList<Phase>();
				//      }

				if (rawName.equals("trafficLightsSchedule")) {
					level = Level.INTERSECTION;
					city.findNode(intersectionName).addTrafficLightsPhases(phasesSet);
					phasesSet = null;
				}
				break;

			case PHASE_LEVEL:
				if (rawName.equals("phase")) {
					level = Level.TRAFFIC_LIGHTS_SCHEDULE;
					phasesSet.add(phase);
				}
				break;

			case TIMING_PLAN_LEVEL:
				if (rawName.equals("phase")) {
					timingPlan.add(phaseTiming);
				}

				if (rawName.equals("plan")) {
					level = Level.TRAFFIC_LIGHTS_SCHEDULE;
					((Intersection) city.findNode(intersectionName)).addTimingPlanFor(timingPlan, timingPlanName);
				}
				break;
			case ARM_ACTIONS:
				if (rawName.equals("armActions")) {
					level = Level.INTERSECTION;
					il = null;
					armFromName = null;
				}
				break;

			case ACTION:
				if (rawName.equals("action")) {
					try {
						for (Lane lane : lanes) {
							Lane[] priorities = getPriorityLanesForLane(lane, ll);
							lane.addAction(ol, priorities);
						}
					} catch (UnsupportedLinkOperationException | InvalidActionException e) {
						e.printStackTrace();
					}

					ol = null;
					ll = new LinkedList<>();
					level = Level.ARM_ACTIONS;
				}
				break;

			case ACTION_RULE:
				if (rawName.equals("rule")) {
					level = Level.ACTION;
				}
				break;

			default:
				break;
		}
	}

	/**
	 * This method creates an array of priority lanes for the given lane - only within current
	 * link. The list includes those that are more on right.
	 *
	 * @param lane            a lane to produce a priority array for
	 * @param outerPriorities list of priorities from other links
	 * @return priority lanes array for the given lane
	 */
	private Lane[] getPriorityLanesForLane(Lane lane, List<Lane> outerPriorities) {
		Link parent = lane.getOwner();
		int laneNo = lane.getAbsoluteNumber();
		int leftmostMainLane = parent.leftLaneCount();
		int rightmostMainLane = parent.leftLaneCount() + parent.mainLaneCount() - 1;

		// if the given lane is among main lanes ...
		if ((laneNo >= rightmostMainLane) && (laneNo <= leftmostMainLane)) {
			// ... then we shall add some main lanes to the list.

			for (int i = rightmostMainLane; i > laneNo; i--) {
				ll.addLast(parent.getLaneAbs(i));
			}
		}
		// now, convert it into an array
		return ll.toArray(new Lane[0]);
	}

	@Override
	public void warning(SAXParseException ex) {
		LOGGER.error("[Warning] " + getLocationString(ex) + ": " + ex.getMessage());
	}

	@Override
	public void error(SAXParseException ex) {
		LOGGER.error("[Error] " + getLocationString(ex) + ": " + ex.getMessage());
	}

	@Override
	public void fatalError(SAXParseException ex) throws SAXException {
		LOGGER.error("[Fatal Error] " + getLocationString(ex) + ": " + ex.getMessage());
		throw ex;
	}

	/**
	 * Returns a string of the location.
	 */
	private static String getLocationString(SAXParseException ex) {
		StringBuilder str = new StringBuilder();

		String systemId = ex.getSystemId();
		if (systemId != null) {
			int index = systemId.lastIndexOf('/');
			if (index != -1) {
				systemId = systemId.substring(index + 1);
			}
			str.append(systemId);
		}
		str.append(':');
		str.append(ex.getLineNumber());
		str.append(':');
		str.append(ex.getColumnNumber());

		return str.toString();
	}

	public Core getCore() {
		return core;
	}
	// TODO: log wielopoziomowy
}

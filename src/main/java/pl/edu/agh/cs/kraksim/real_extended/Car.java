package pl.edu.agh.cs.kraksim.real_extended;

import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import pl.edu.agh.cs.kraksim.KraksimConfigurator;
import pl.edu.agh.cs.kraksim.core.Action;
import pl.edu.agh.cs.kraksim.core.Lane;
import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.core.Node;
import pl.edu.agh.cs.kraksim.iface.sim.Route;
import pl.edu.agh.cs.kraksim.main.drivers.Driver;

import java.awt.*;
import java.util.*;
import java.util.List;

class Car {
	private static final Logger LOGGER = Logger.getLogger(Car.class);
	private final boolean isTEST2013Enabled;
	private final Driver driver;
	private List<Node> TEST2013intersectionsList = new LinkedList<>();
	private Map<Node, List<Link>> TEST2013linkIntersectionsList = new HashMap<>();
	/*
	 * Iterator through route's link. linkIterator.next() is the next (not
	 * current!) link, the car will drive.
	 */
	private ListIterator<Link> linkIterator;
	private Action action;
	private Action preferableAction;
	protected int pos;
	private int velocity;
	//  private ListIterator<Link> copyLinkIterator;
	private long TEST2013waitCounter = 0;
	private long TEST2013waitLimit;
	private boolean braking = false;
	private int enterPos = -1;
	private Lane beforeLane;
	private int beforePos;
	private boolean rerouting = false;

    private LaneSwitch switchToLane = LaneSwitch.NO_CHANGE;

	Car(Driver driver, Route route, boolean rerouting) {
		// == reading TEST2013 configuration
		Properties prop = KraksimConfigurator.getPropertiesFromFile();
		String test2013enabled = prop.getProperty("TEST2013Enabled");
		if (test2013enabled != null && test2013enabled.trim().equals("true")) {
			isTEST2013Enabled = true;

			String test2013intersectionVisitor = prop.getProperty("TEST2013IntersectionVisitor");
			if (test2013intersectionVisitor != null && test2013intersectionVisitor.trim().equals("true")) {
				boolean isTEST2013IntersectionVisitorEnabled = true;
				TEST2013intersectionsList = new LinkedList<>();
			} else {
				boolean isTEST2013IntersectionVisitorEnabled = false;
			}


			String test2013intersectionLinkVisitor = prop.getProperty("TEST2013IntersectionLinkVisitor");

			if (test2013intersectionLinkVisitor != null && test2013intersectionLinkVisitor.trim().equals("true")) {
				TEST2013linkIntersectionsList = new HashMap<>();
			}

			String test2013waitLimit = prop.getProperty("TEST2013WaitLimit");
			TEST2013waitLimit = Integer.valueOf(test2013waitLimit);
		} else {
			isTEST2013Enabled = false;
		}
		// =end of= reading TEST2013 configuration

		this.driver = driver;
		this.rerouting = rerouting;
		linkIterator = route.linkIterator();
		// copyLinkIterator = route.linkIterator();
		// Important. See the notice above.
		linkIterator.next();
		// copyLinkIterator.next();

		beforeLane = null;
		beforePos = 0;

		LOGGER.trace("\n Driver= " + driver + "\n rerouting= " + rerouting);
	}
	
	public Car() {
		this.isTEST2013Enabled = false;
		this.driver = null;
	}

	public Driver getDriver() {
		return driver;
	}

	public int getPosition() {
		return pos;
	}

	public void setPosition(int pos) {
		this.pos = pos;
	}

	public int getVelocity() {
		return velocity;
	}

	public void setVelocity(int velocity) {
		this.velocity = velocity;
	}

	public int getEnterPos() {
		return enterPos;
	}

	public void setEnterPos(int enterPos) {
		this.enterPos = enterPos;
	}

	public boolean isEmergency() {
		return getDriver().isEmergency();
	}

	public boolean isBraking() {
		return braking;
	}

	public void setBraking(boolean braking, boolean emergency) {
		if (emergency) {
			if (braking) {
				driver.setCarColor(Color.BLACK);
			} else {
				driver.setCarColor(Color.BLUE);
			}
		} else {
			if (braking) {
				driver.setCarColor(Color.RED);
			} else {
				driver.setCarColor(Color.YELLOW);
			}
		}
		this.braking = braking;
	}

	public void TEST2013updateCarPosition(int position) {
		if (isTEST2013Enabled) {
			if (pos == position) {
				TEST2013waitCounter++;
				if (TEST2013waitCounter > TEST2013waitLimit) {
					LOGGER.info(String.format("%s hasn't move for %d turns.", toString(), TEST2013waitCounter));
				}
			} else {
				TEST2013waitCounter = 0;
			}
		}
	}

	public String toString() {
		return driver + " in [ CAR bPos=" + beforePos + ",cPos=" + pos + ",v=" + velocity + ']';
	}

	int getBeforePos() {
		return beforePos;
	}

	void setBeforePos(int beforePos) {
		this.beforePos = beforePos;
	}

	// TODO: change this
	public boolean hasNextTripPoint() {
		return linkIterator.hasNext();
	}

	public Link nextTripPoint() {
		// copyLinkIterator.next();
		return linkIterator.next();
	}

	public Link peekNextTripPoint() {
		// copyLinkIterator.next();
		if (linkIterator.hasNext()) {
			Link result = linkIterator.next();
			linkIterator.previous();
			return result;
		} else {
			return null;
		}
	}

	public Action getAction() {
		LOGGER.trace("\n Action= " + action + "\n Driver= " + driver);
		return action;
	}

	public void setAction(Action action) {
		if (isTEST2013Enabled) {
			TEST2013onNewAction(action);
		}

		LOGGER.trace("\n Action= " + action + "\n Driver= " + driver);
		this.action = action;
	}

	public Action getPreferableAction(){
		return this.preferableAction;
	}
	
	public void setPreferableAction(Action preferableAction){
		this.preferableAction = preferableAction;
	}
	
	protected void TEST2013onNewAction(Action action) {
		if (action == null || action.getSource() == null) {
			return;
		}

		Link sourceLink = action.getSource().getOwner();
		Node nextIntersection = action.getTarget().getBeginning();

		// check if this intersection has been visited before
		// from this particular link
		if (TEST2013linkIntersectionsList.containsKey(sourceLink)) {
			List<Link> links = TEST2013linkIntersectionsList.get(nextIntersection);
			if (links.contains(sourceLink)) {
				LOGGER.fatal(String.format("Vehicle has already visited intersection (id:%s) from link (%s). This should _NEVER_ happen", nextIntersection.getId(), sourceLink.getId()));
			} else {
				links.add(sourceLink);
			}
		} else {
			TEST2013linkIntersectionsList.put(nextIntersection, Lists.newArrayList(sourceLink));
		}

		// check if this intersection has been visited before
		if (TEST2013intersectionsList.contains(nextIntersection)) {
			LOGGER.warn(String.format("Vehicle has already been at intersection (id:%s)", nextIntersection.getId()));
		} else {
			TEST2013intersectionsList.add(nextIntersection);
		}
	}

	public void refreshTripRoute() {
		// TODO: make it configurable from properties file
		// ListIterator<Link> copyLinkIter = linkIterator;
		if (!linkIterator.hasNext()) {
			return;
		}
		ListIterator<Link> newlinkIterator;
		if (rerouting) {
			newlinkIterator = driver.updateRouteFrom(linkIterator.next());
			linkIterator.previous();
			if (newlinkIterator != null) {
				linkIterator = newlinkIterator;

				LOGGER.trace("New Route ");
			} else {
				LOGGER.trace("OLD Route ");
			}
		}

		// int li = 0;
		// System.err.println( "\n-----" );
		// System.err.println( "Distance:  " );
		// for (Iterator<Link> iter = copyLinkIter; iter.hasNext();) {
		// Link element = iter.next();
		// li++;
		// System.err.print( element.getId() + " " );
		//
		// }
		// while ( li-- > 0 ) {
		// copyLinkIter.previous();
		// }
		// System.err.println( " " );
		//
		// System.err.println( "    Time:  " );
		// for (Iterator<Link> iter = newlinkIterator; iter.hasNext();) {
		// Link element = iter.next();
		// li++;
		// System.err.print( element.getId() + " " );
		// }
		// while ( li-- > 0 ) {
		// newlinkIterator.previous();
		// }
		//
	}

	public Lane getBeforeLane() {
		return beforeLane;
	}

	public void setBeforeLane(Lane beforeLane) {
		this.beforeLane = beforeLane;
	}

    public LaneSwitch getLaneSwitch() {
        return switchToLane;
    }

	public void setLaneSwitch(LaneSwitch lane){
		this.switchToLane = lane;
	}
}

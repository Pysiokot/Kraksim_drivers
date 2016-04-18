package pl.edu.agh.cs.kraksimcitydesigner.element;

import java.awt.Shape;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import pl.edu.agh.cs.kraksimcitydesigner.ThickLine;
import pl.edu.agh.cs.kraksimcitydesigner.element.Intersection.IncomingLane;
import pl.edu.agh.cs.kraksimcitydesigner.inf.Clickable;
import pl.edu.agh.cs.kraksimcitydesigner.inf.CityElement;

// TODO: Auto-generated Javadoc
public class Link implements CityElement, Clickable {
    private static Logger log = Logger.getLogger(Link.class);
	
	private int numberOfLanes;

    private int length;
	private List<Integer> leftLanes;
	private List<Integer> rightLanes;
	private Shape shape;
	private DisplaySettings displaySettings;

	private LinkType linkType;

    private Node startNodeOfRoad;
    private Node endNodeOfRoad;
    
    private List<IncomingLane> incomingLanes;
    private Road road;
	
	public static enum LinkType {
	    UPLINK, DOWNLINK
	}
	
	/**
	 * Instantiates a new link.
	 * 
	 * @param linkType the link type
	 * @param length the length
	 * @param numOfLanes the num of lanes
	 * @param leftLanes the left lanes
	 * @param rightLanes the right lanes
	 * @param startNodeOfRaod the start node of raod
	 * @param endNodeOfRoad the end node of road
	 */
	public Link(LinkType linkType, int length, int numOfLanes,
	        List<Integer> leftLanes, List<Integer> rightLanes,
	        Node startNodeOfRaod, Node endNodeOfRoad, DisplaySettings displaySettings) {

		this.numberOfLanes = numOfLanes;
		this.length = length;
		this.leftLanes = leftLanes;
		this.rightLanes = rightLanes;
		this.startNodeOfRoad = startNodeOfRaod;
		this.endNodeOfRoad = endNodeOfRoad;
		this.linkType = linkType;
		this.displaySettings = displaySettings;
		
		this.incomingLanes = new LinkedList<IncomingLane>();
		if (leftLanes.size() > 0) {
		    this.incomingLanes.add(new IncomingLane(getStartNode(),-1));
		}
		if (rightLanes.size() > 0) {
		    this.incomingLanes.add(new IncomingLane(getStartNode(),1));
		}
		if (numOfLanes > 0) {
		    this.incomingLanes.add(new IncomingLane(getStartNode(),0));
		}
		
	}
	
	/**
	 * Sets the incoming lanes.
	 */
	private void setIncomingLanes() {
	    this.incomingLanes = new LinkedList<IncomingLane>();
        if (leftLanes.size() > 0) {
            this.incomingLanes.add(new IncomingLane(getStartNode(),-1));
        }
        if (rightLanes.size() > 0) {
            this.incomingLanes.add(new IncomingLane(getStartNode(),1));
        }
        if (numberOfLanes > 0) {
            this.incomingLanes.add(new IncomingLane(getStartNode(),0));
        }
	}
	
	/**
	 * Copy link.
	 * 
	 * @return the link
	 */
	public Link copyLink() {
        List<Integer> leftLanes = new LinkedList<Integer>(this.leftLanes);
        List<Integer> rightLanes = new LinkedList<Integer>(this.rightLanes);;

        return new Link(linkType, length, numberOfLanes, leftLanes, rightLanes, startNodeOfRoad, endNodeOfRoad, displaySettings);
	}
	
	/**
	 * Make shape.
	 */
	private void makeShape() {
		
		double x1 = startNodeOfRoad.getX();
		double y1 = startNodeOfRoad.getY();
		double x2 = endNodeOfRoad.getX();
		double y2 = endNodeOfRoad.getY();
		
		double distance = startNodeOfRoad.getPoint().distance(endNodeOfRoad.getPoint());
		
        double[] vectorAB = new double[] { (x2-x1) / distance, (y2-y1) / distance };
        double[] vectorOrtogonal = new double[] { -vectorAB[1], vectorAB[0] };
		
        double new_x1,new_x2,new_y1,new_y2;
		if (linkType.equals(LinkType.UPLINK)) {
		    new_x1 = x1 + vectorOrtogonal[0] * 5;
		    new_x2 = x2 + vectorOrtogonal[0] * 5;
		    new_y1 = y1 + vectorOrtogonal[1] * 5;
		    new_y2 = y2 + vectorOrtogonal[1] * 5;
		    
		} else {
            new_x1 = x1 - vectorOrtogonal[0] * 5;
            new_x2 = x2 - vectorOrtogonal[0] * 5;
            new_y1 = y1 - vectorOrtogonal[1] * 5;
            new_y2 = y2 - vectorOrtogonal[1] * 5;   
		}
		
		ThickLine tl = new ThickLine((int)new_x1,(int)new_y1,(int)new_x2,(int)new_y2,this.numberOfLanes+1);
				
		this.shape = tl.getShape();
	}
	
	/**
	 * Gets the shape.
	 * 
	 * @return the shape
	 */
	public Shape getShape() {
	    makeShape();
		return this.shape;
	}
	
	
	/* (non-Javadoc)
	 * @see pl.edu.agh.cs.kraksimcitydesigner.inf.Clickable#contain(double, double)
	 */
	public boolean contain(double x, double y) {
		return this.shape.contains(x,y);
	}

    /* (non-Javadoc)
     * @see pl.edu.agh.cs.kraksimcitydesigner.inf.Element#getId()
     */
    @Override
    public String getId() {
        if (this.linkType == LinkType.UPLINK) {
            return startNodeOfRoad.getId()+"_"+endNodeOfRoad.getId();
        }
        else {
            return endNodeOfRoad.getId()+"_"+startNodeOfRoad.getId();
        }
    }

    /**
     * Gets the start node.
     * 
     * @return the start node
     */
    public Node getStartNode() {
        if (this.linkType == LinkType.UPLINK) {
            return startNodeOfRoad;
        } else {
            return endNodeOfRoad;
        }
    }

    /**
     * Gets the end node.
     * 
     * @return the end node
     */
    public Node getEndNode() {
        if (this.linkType == LinkType.UPLINK) {
            return endNodeOfRoad;
        } else {
            return startNodeOfRoad;
        }
    }

    /**
     * Sets the length.
     * 
     * @param length the new length
     */
    public void setLength(int length) {
        this.length = length;
    }

    /**
     * Gets the length.
     * 
     * @return the length
     */
    public int getLength() {
        return length;
    }

    /**
     * Gets the left lanes.
     * 
     * @return the left lanes
     */
    public List<Integer> getLeftLanes() {
        return leftLanes;
    }

    /**
     * Gets the right lanes.
     * 
     * @return the right lanes
     */
    public List<Integer> getRightLanes() {
        return rightLanes;
    }
    
    /**
     * Gets the number of lanes.
     * 
     * @return the number of lanes
     */
    public int getNumberOfLanes() {
        return this.numberOfLanes;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "<Link ("+getId()+") from = "+getStartNode().getId()+" to = "+getEndNode().getId()+">";
    }

    /**
     * Sets the road.
     * 
     * @param road the new road
     */
    public void setRoad(Road road) {
        this.road = road;
    }

    /**
     * Gets the road.
     * 
     * @return the road
     */
    public Road getRoad() {
        return road;
    }
    
    /**
     * Gets the link type.
     * 
     * @return the link type
     */
    public LinkType getLinkType() {
        return linkType;
    }

    /**
     * Sets the number of lanes.
     * 
     * @param numberOfLanes the new number of lanes
     */
    public void setNumberOfLanes(int numberOfLanes) {
        this.numberOfLanes = numberOfLanes;
        setIncomingLanes();
    }

    /**
     * Sets the left lanes.
     * 
     * @param leftLanes the new left lanes
     */
    public void setLeftLanes(List<Integer> leftLanes) {
        this.leftLanes = leftLanes;
        setIncomingLanes();
    }

    /**
     * Sets the right lanes.
     * 
     * @param rightLanes the new right lanes
     */
    public void setRightLanes(List<Integer> rightLanes) {
        this.rightLanes = rightLanes;
        setIncomingLanes();
    }

    /**
     * Gets the lane nums.
     * 
     * @return the lane nums
     */
    public List<Integer> getLaneNums() {
        List<Integer> result = new LinkedList<Integer>();
        for (IncomingLane laneNum : incomingLanes) {
            result.add(laneNum.getLaneNum());
        }
        return result;
    }

    /**
     * Gets the incoming lanes.
     * 
     * @return the incoming lanes
     */
    public List<IncomingLane> getIncomingLanes() {
        return incomingLanes;
    }

    /**
     * Calculate Euclidean distance between nodes 
     * and set it as distance for this link.
     */
    public void recalculateDistance() {
        
        /*
        List<Integer> newLeftLanes = new LinkedList<Integer>();
        for (Integer left : leftLanes) {
            int newValue = (int) (left * displaySettings.getCellsPerPixel());
            if (newValue)
            newLeftLanes.add
        }
        */
        
        int newValue = getStartNode().calculateDistance(getEndNode());
        if (newValue < 8) { newValue = 8; }
        this.length = newValue;
    }
    
    /**
     * Two links are equals if they connect the same nodes and have the same type.
     */
    @Override
    public boolean equals(Object o) {
        if (! (o instanceof Link)) {
            return false;
        }
        Link l = (Link)o;
        if (this.startNodeOfRoad.equals(l.startNodeOfRoad) && 
                this.endNodeOfRoad.equals(l.endNodeOfRoad) &&
                this.linkType.equals(l.linkType)) {
            return true;
        }
        return false;
    }
    
    public int hashCode() {
        return this.startNodeOfRoad.hashCode()+this.endNodeOfRoad.hashCode();
    }
}

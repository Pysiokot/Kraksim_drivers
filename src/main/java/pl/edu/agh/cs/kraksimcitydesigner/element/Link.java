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
	
	private int numberOfLines;

    private int length;
	private List<Integer> leftLines;
	private List<Integer> rightLines;
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
	 * @param numOfLines the num of lines
	 * @param leftLines the left lines
	 * @param rightLines the right lines
	 * @param startNodeOfRaod the start node of raod
	 * @param endNodeOfRoad the end node of road
	 */
	public Link(LinkType linkType, int length, int numOfLines,
	        List<Integer> leftLines, List<Integer> rightLines, 
	        Node startNodeOfRaod, Node endNodeOfRoad, DisplaySettings displaySettings) {

		this.numberOfLines = numOfLines;
		this.length = length;
		this.leftLines = leftLines;
		this.rightLines = rightLines;
		this.startNodeOfRoad = startNodeOfRaod;
		this.endNodeOfRoad = endNodeOfRoad;
		this.linkType = linkType;
		this.displaySettings = displaySettings;
		
		this.incomingLanes = new LinkedList<IncomingLane>();
		if (leftLines.size() > 0) {
		    this.incomingLanes.add(new IncomingLane(getStartNode(),-1));
		}
		if (rightLines.size() > 0) {
		    this.incomingLanes.add(new IncomingLane(getStartNode(),1));
		}
		if (numOfLines > 0) {
		    this.incomingLanes.add(new IncomingLane(getStartNode(),0));
		}
		
	}
	
	/**
	 * Sets the incoming lanes.
	 */
	private void setIncomingLanes() {
	    this.incomingLanes = new LinkedList<IncomingLane>();
        if (leftLines.size() > 0) {
            this.incomingLanes.add(new IncomingLane(getStartNode(),-1));
        }
        if (rightLines.size() > 0) {
            this.incomingLanes.add(new IncomingLane(getStartNode(),1));
        }
        if (numberOfLines > 0) {
            this.incomingLanes.add(new IncomingLane(getStartNode(),0));
        }
	}
	
	/**
	 * Copy link.
	 * 
	 * @return the link
	 */
	public Link copyLink() {
        List<Integer> leftLines = new LinkedList<Integer>(this.leftLines);
        List<Integer> rightLines = new LinkedList<Integer>(this.rightLines);;

        return new Link(linkType, length, numberOfLines, leftLines, rightLines, startNodeOfRoad, endNodeOfRoad, displaySettings);
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
		
		ThickLine tl = new ThickLine((int)new_x1,(int)new_y1,(int)new_x2,(int)new_y2,this.numberOfLines+1);
				
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
     * Gets the left lines.
     * 
     * @return the left lines
     */
    public List<Integer> getLeftLines() {
        return leftLines;
    }

    /**
     * Gets the right lines.
     * 
     * @return the right lines
     */
    public List<Integer> getRightLines() {
        return rightLines;
    }
    
    /**
     * Gets the number of lines.
     * 
     * @return the number of lines
     */
    public int getNumberOfLines() {
        return this.numberOfLines;
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
     * Sets the number of lines.
     * 
     * @param numberOfLines the new number of lines
     */
    public void setNumberOfLines(int numberOfLines) {
        this.numberOfLines = numberOfLines;
        setIncomingLanes();
    }

    /**
     * Sets the left lines.
     * 
     * @param leftLines the new left lines
     */
    public void setLeftLines(List<Integer> leftLines) {
        this.leftLines = leftLines;
        setIncomingLanes();
    }

    /**
     * Sets the right lines.
     * 
     * @param rightLines the new right lines
     */
    public void setRightLines(List<Integer> rightLines) {
        this.rightLines = rightLines;
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
        List<Integer> newLeftLines = new LinkedList<Integer>();
        for (Integer left : leftLines) {
            int newValue = (int) (left * displaySettings.getCellsPerPixel());
            if (newValue)
            newLeftLines.add
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

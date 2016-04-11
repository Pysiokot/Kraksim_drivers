package pl.edu.agh.cs.kraksimcitydesigner.element;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import pl.edu.agh.cs.kraksimcitydesigner.inf.Clickable;
import pl.edu.agh.cs.kraksimcitydesigner.element.DisplaySettings;
import pl.edu.agh.cs.kraksimcitydesigner.inf.CityElement;

// TODO: Auto-generated Javadoc
public abstract class Node implements CityElement, Clickable {

    protected String id;
	private int x;
	private int y;
	private boolean selected = false;
    private DisplaySettings ds;
    
    private Set<Link> incomingLinks = new HashSet<Link>();
    private Set<Link> outcomingLinks = new HashSet<Link>();
   
	/**
	 * Instantiates a new node.
	 * 
	 * @param id the id
	 * @param x the x
	 * @param y the y
	 * @param ds the ds
	 */
	public Node(String id, int x, int y, DisplaySettings ds) {
	    this.id = id;
		this.x = x;
		this.y = y;
		this.ds = ds;
	}
	
	/**
	 * Instantiates a new node.
	 * 
	 * @param org the org
	 */
	public Node(Node org) {
	    this.id = org.id;
	    this.x = org.x;
	    this.y = org.y;
	    this.selected = org.selected;
	    this.ds = org.ds;
	    this.incomingLinks = new HashSet<Link>(org.incomingLinks);
	    this.outcomingLinks = new HashSet<Link>(org.outcomingLinks);
	}
	
	/**
	 * Copy node state.
	 * 
	 * @param changedNode the changed node
	 */
	public void copyNodeState(Node changedNode) {
	    this.id = changedNode.id;
	    this.x = changedNode.x;
	    this.y = changedNode.y;
	    this.selected = changedNode.selected;
	    this.ds = changedNode.ds;
	    this.incomingLinks = changedNode.incomingLinks;
	}

	/**
	 * Gets the x.
	 * 
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * Gets the y.
	 * 
	 * @return the y
	 */
	public int getY() {
		return y;
	}

	/**
	 * Gets the width.
	 * 
	 * @return the width
	 */
	public double getWidth() {
		return ds.getNodeWidth();
	}

	/**
	 * Gets the height.
	 * 
	 * @return the height
	 */
	public double getHeight() {
		return ds.getNodeHeight();
	}
	
	/**
	 * Gets the point.
	 * 
	 * @return the point
	 */
	public Point2D getPoint() {
	    return new Point2D.Double(x,y);
	}

	/* (non-Javadoc)
	 * @see pl.edu.agh.cs.kraksimcitydesigner.inf.Clickable#contain(double, double)
	 */
	public boolean contain(double x, double y) {
		if (x < (this.x - getWidth() / 2.0) || x > (this.x + getWidth() / 2.0)) {
			return false;
		}
		if (y < (this.y - getHeight() / 2.0) || y > (this.y + getHeight() / 2.0)) {
			return false;
		}		
		return true;
	}

    /**
     * Sets the x.
     * 
     * @param x the new x
     */
    public void setX(int x) {
        this.x = x;  
    }
    
    /**
     * Sets the y.
     * 
     * @param y the new y
     */
    public void setY(int y) {
        this.y = y;  
    }
    
    /**
     * Sets the selected.
     * 
     * @param selected the new selected
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    /**
     * Gets the selected.
     * 
     * @return the selected
     */
    public boolean getSelected() {
        return this.selected;
    }

    /* (non-Javadoc)
     * @see pl.edu.agh.cs.kraksimcitydesigner.inf.Element#getId()
     */
    @Override
    public String getId() {
        return this.id;
    }

    /**
     * Sets the id.
     * 
     * @param id the new id
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Gets the display settings.
     * 
     * @return the display settings
     */
    public DisplaySettings getDisplaySettings() {
        return this.ds;
    }
    
    // Model specific
    
    /**
     * Adds the incoming link.
     * 
     * @param incoming the incoming
     */
    public void addIncomingLink(Link incoming) {
        this.incomingLinks.add(incoming);
    }
    
    /**
     * Removes the incoming link.
     * 
     * @param incoming the incoming
     * 
     * @return true, if successful
     */
    public boolean removeIncomingLink(Link incoming) {
        return this.incomingLinks.remove(incoming);
    }
    
    /**
     * Gets the incoming links.
     * 
     * @return the incoming links
     */
    public Set<Link> getIncomingLinks() {
        return this.incomingLinks;
    }
    
    /**
     * Adds the outcoming link.
     * 
     * @param link the link
     */
    public void addOutcomingLink(Link link) {
        this.outcomingLinks.add(link);
    }
    
    /**
     * Removes the outcoming link.
     * 
     * @param link the link
     * 
     * @return true, if successful
     */
    public boolean removeOutcomingLink(Link link) {
        return this.outcomingLinks.remove(link);
    }
    
    /**
     * Gets the outcoming links.
     * 
     * @return the outcoming links
     */
    public Set<Link> getOutcomingLinks() {
        return this.outcomingLinks;
    }
    
    /**
     * Break connection.
     * 
     * @param road the road
     */
    abstract public void breakConnection(Road road);

    /**
     * Calculate Euclidean distance between this Node and endNode.
     * 
     * @param endNode the endNode
     */
    public int calculateDistance(Node endNode) {
        
        Node a = this;
        Node b = endNode;
        int result = (int) (ds.getCellsPerPixel() * 
                Math.sqrt(Math.pow(a.getX()-b.getX(), 2)+Math.pow(a.getY()-b.getY(), 2)));
        return result;
    }
    
    /**
     * Returns nodes to which exists link.
     * @return
     */
    public List<Node> getReachableNodes() {
        
        List<Node> reachableNodes = new ArrayList<Node>(outcomingLinks.size());
        for (Link outcomingLink : outcomingLinks) {
            reachableNodes.add(outcomingLink.getEndNode());
        }
        return reachableNodes; 
    }
}

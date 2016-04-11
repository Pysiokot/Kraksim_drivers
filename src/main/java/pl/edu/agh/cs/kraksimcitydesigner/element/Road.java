package pl.edu.agh.cs.kraksimcitydesigner.element;

import pl.edu.agh.cs.kraksimcitydesigner.inf.Clickable;
import pl.edu.agh.cs.kraksimcitydesigner.inf.CityElement;
import pl.edu.agh.cs.kraksimcitydesigner.inf.Selectable;

// TODO: Auto-generated Javadoc
public class Road implements CityElement,Clickable,Selectable,Comparable<Road> {
	
	private Link uplink;
    private Link downlink;
	private String id;
	private String street;
	private boolean selected = false;
	
	/**
	 * Instantiates a new road.
	 * 
	 * @param id the id
	 * @param street the street
	 * @param uplink the uplink
	 * @param downlink the downlink
	 */
	public Road(String id, String street, Link uplink, Link downlink) {
		this.id = id;
		this.street = street;
		this.uplink = uplink;
		this.downlink = downlink;
	}
	
	/**
	 * Copy road.
	 * 
	 * @return the road
	 */
	public Road copyRoad() {
	    String id = new String(this.id);
	    String street = new String(this.street);
	    Link uplink = this.uplink.copyLink();
	    Link downlink = this.downlink.copyLink();
	    
	    return new Road(id, street, uplink, downlink);
	}
	
	/* (non-Javadoc)
	 * @see pl.edu.agh.cs.kraksimcitydesigner.inf.Clickable#contain(double, double)
	 */
	public boolean contain(double x, double y) {
		return (uplink != null && uplink.contain(x,y)) || (downlink != null && downlink.contain(x,y));
	}

	/**
	 * Sets the uplink.
	 * 
	 * @param uplink the new uplink
	 */
	public void setUplink(Link uplink) {
        this.uplink = uplink;
    }

    /**
     * Sets the downlink.
     * 
     * @param downlink the new downlink
     */
    public void setDownlink(Link downlink) {
        this.downlink = downlink;
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
     * Sets the street.
     * 
     * @param street the new street
     */
    public void setStreet(String street) {
        this.street = street;
    }
	
	/**
	 * Gets the uplink.
	 * 
	 * @return the uplink
	 */
	public Link getUplink() {
		return uplink;
	}

	/**
	 * Gets the downlink.
	 * 
	 * @return the downlink
	 */
	public Link getDownlink() {
		return downlink;
	}

    /* (non-Javadoc)
     * @see pl.edu.agh.cs.kraksimcitydesigner.inf.Element#getId()
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * Gets the street.
     * 
     * @return the street
     */
    public String getStreet() {
        return street;
    }

    /**
     * Gets the other node.
     * 
     * @param node the node
     * 
     * @return the other node
     */
    public Node getOtherNode(Node node) {
        
        if (uplink != null) {
            if (uplink.getStartNode() == node) {
                return uplink.getEndNode();
            } else {
                return uplink.getStartNode();
            }
        }
        if (downlink != null) {
            if (downlink.getStartNode() == node) {
                return downlink.getEndNode();
            } else {
                return downlink.getStartNode();
            }
        }
        assert false;
        return null;
    }

    /* (non-Javadoc)
     * @see pl.edu.agh.cs.kraksimcitydesigner.inf.Selectable#setSelected(boolean)
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /* (non-Javadoc)
     * @see pl.edu.agh.cs.kraksimcitydesigner.inf.Selectable#isSelected()
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Calculate Euclidean distance between nodes 
     * and set it as distance for uplink and downlink.
     */
    public void recalculateLinksDistances() {
        if (uplink != null) {
            uplink.recalculateDistance();
        }
        if (downlink != null) {
            downlink.recalculateDistance();
        }
    }
    
    public Node getFromNode() {
        if (uplink != null) {
            return uplink.getStartNode();
        }
        else if (downlink != null) {
            return downlink.getEndNode();
        }
        assert false;
        return null;
    }
    
    public Node getToNode() {
        if (uplink != null) {
            return uplink.getEndNode();
        }
        else if (downlink != null) {
            return downlink.getStartNode();
        }
        assert false;
        return null;
    }

    @Override
    public int compareTo(Road o) {
      
        int result = this.getFromNode().getId().compareTo(o.getFromNode().getId());
        if (result == 0) {
            result = this.getToNode().getId().compareTo(o.getToNode().getId());
        }
        return result;
    }
    
    @Override
    public boolean equals(Object o) {

        if (! (o instanceof Road)) {
            return false;
        }
        else {
            if (this.compareTo((Road)o) == 0) {
                return true;
            }
        }
        return false;
        
    }
    
    @Override
    public int hashCode() {
        //return getFromNode().getId().hashCode()+getToNode().getId().hashCode();
        return 1;
    }

}

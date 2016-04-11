package pl.edu.agh.cs.kraksimcitydesigner.element;

import pl.edu.agh.cs.kraksimcitydesigner.inf.Clickable;
import pl.edu.agh.cs.kraksimcitydesigner.element.DisplaySettings;

// TODO: Auto-generated Javadoc
public class Gateway extends Node {

	/**
	 * Instantiates a new gateway.
	 * 
	 * @param id the id
	 * @param x the x
	 * @param y the y
	 * @param ds the ds
	 */
	public Gateway(String id,int x, int y,DisplaySettings ds) {
		super(id, x, y, ds);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
	    return String.format("Gateway id = %s, x = %d, y = %d, width = %d, height = %d",getId(),(int)getX(),(int)getY(),(int)getWidth(),(int)getHeight());
	}

    /* (non-Javadoc)
     * @see pl.edu.agh.cs.kraksimcitydesigner.element.Node#breakConnection(pl.edu.agh.cs.kraksimcitydesigner.element.Road)
     */
    @Override
    public void breakConnection(Road road) {
        
    }
}

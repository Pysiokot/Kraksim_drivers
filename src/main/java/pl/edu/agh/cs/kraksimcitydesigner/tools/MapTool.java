/**
 * 
 */
package pl.edu.agh.cs.kraksimcitydesigner.tools;

import java.awt.Cursor;

import javax.swing.event.MouseInputListener;

/**
 * @author anna.bizon
 *
 */
public interface MapTool extends MouseInputListener {
    
    /**
     * Gets the cursor.
     * 
     * @return the cursor
     */
    public Cursor getCursor();
}

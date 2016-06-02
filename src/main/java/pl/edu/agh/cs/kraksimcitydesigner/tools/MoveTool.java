package pl.edu.agh.cs.kraksimcitydesigner.tools;

import java.awt.Cursor;

import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import pl.edu.agh.cs.kraksimcitydesigner.EditorPanel;


// TODO: Auto-generated Javadoc
/**
 * @author anna.bizon
 *
 */
public class MoveTool implements MapTool{
    private final Logger log = Logger.getLogger(this.getClass().getName());   
    Integer x;
    Integer y;
    private EditorPanel editorPanel = null;
  
    /**
     * Instantiates a new move tool.
     * 
     * @param editorPanel the editor panel
     */
    public MoveTool(EditorPanel editorPanel){
        super();
        this.editorPanel= editorPanel;
        log.debug("Created");
    }
    
    /* (non-Javadoc)
     * @see pl.edu.agh.cs.kraksimcitydesigner.tools.MapTool#getCursor()
     */
    public Cursor getCursor() {
        return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent arg0) {
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent e) {       
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent e) {       
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent e) {
    	if(SwingUtilities.isLeftMouseButton(e)){
    	x=e.getX();
        y=e.getY();      
    	}
        log.debug("mousePressed "+ x+ ", "+ y);
       
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e) {
    	if(SwingUtilities.isLeftMouseButton(e)){
        editorPanel.scroll(x-e.getX(),y-e.getY());
        x=e.getX();
        y=e.getY();
    	}
        log.debug("mouseRealesed "+ x+ ", "+ y);
        
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    public void mouseDragged(MouseEvent e) {
    	if(SwingUtilities.isLeftMouseButton(e)){
        editorPanel.scroll(x-e.getX(),y-e.getY());
        x=e.getX();
        y=e.getY();
    	}
        log.debug("mouseDragged "+ x+ ", "+ y);
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    public void mouseMoved(MouseEvent e) {    
    }
}

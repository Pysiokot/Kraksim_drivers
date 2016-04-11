package pl.edu.agh.cs.kraksimcitydesigner;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;

import pl.edu.agh.cs.kraksimcitydesigner.element.DisplaySettings;
import pl.edu.agh.cs.kraksimcitydesigner.element.Gateway;
import pl.edu.agh.cs.kraksimcitydesigner.element.Intersection;
import pl.edu.agh.cs.kraksimcitydesigner.element.Node;
import pl.edu.agh.cs.kraksimcitydesigner.element.Road;
import pl.edu.agh.cs.kraksimcitydesigner.propertiesdialogs.IntersectionPropertiesDialog;
import pl.edu.agh.cs.kraksimcitydesigner.propertiesdialogs.RoadPropertiesDialog;

public class EditorPanel extends JPanel {
	private static Logger log = Logger.getLogger(EditorPanel.class);
	private static final long serialVersionUID = 7950900026576040274L;
	
	public static final Dimension defaultDimension = new Dimension(640, 480);
	private MainFrame mf;
	//private final IntersectionPropertiesDialog intersectionPropertiesDialog;
	private EditorMode editorMode;
	private IntersectionPropertiesDialog intersectionPropertiesDialog;
	//	private final RoadPropertiesDialog roadPropertiesDialog;
	
	private Image backgorundMap;
	private int x;
	private int y;
	private int lastmouseclick_x;
	private int lastmouseclick_y;
	private Rectangle graphicsExtent;
	
	private JScrollPane parentScrollPane;
	private JPopupMenu intersectionPopup;
	private JPopupMenu gatewayPopup;
	private JPopupMenu roadPopup;
	
    private DisplaySettings ds;

    private Node startNode;
    private Node endNode;
    private Node selectedNode;
    // used to avoid checking again if something was clicked on mouseReleased
    private Node clickedNode = null;
    private Road selectedRoad = null;
    
    class EditorMouseAdapter extends MouseAdapter {
		
	    /**
    	 * Maybe show popup.
    	 * 
    	 * @param e the e
    	 */
    	private void maybeShowPopup(MouseEvent e) {
	        if (e.isPopupTrigger() && clickedNode != null) {
	            if (clickedNode instanceof Intersection) {
	                intersectionPopup.show(e.getComponent(),e.getX(), e.getY());
	            }
	            else {
	                gatewayPopup.show(e.getComponent(),e.getX(), e.getY());
	            }
	        } else if (e.isPopupTrigger() && selectedRoad != null) {
	            roadPopup.show(e.getComponent(),e.getX(), e.getY());
	        }
	    }
	    
	    /* (non-Javadoc)
    	 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
    	 */
    	@Override
	    public void mouseReleased(MouseEvent e) {
	        maybeShowPopup(e);
	    }

		/* (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
		 */
		@Override
		public void mousePressed(MouseEvent e) {
		    int x = e.getX();
		    int y = e.getY();
		    lastmouseclick_x = x - parentScrollPane.getHorizontalScrollBar().getValue();
		    lastmouseclick_y = y - parentScrollPane.getVerticalScrollBar().getValue();;
		    
		    List<Node> nodes = mf.getElementManager().getNodes();
		    
		    clickedNode = null;
		    
		    System.out.println("Mouse pressed");

			if (editorMode == EditorMode.SELECTING) {	
				log.trace("mousePressed");
				
				boolean nodeClicked = false;
                for (Node node : nodes) {
                    if (node.contain(x, y)) {
                        nodeClicked = true;
                        node.setSelected(true);
                        selectedNode = node;
                        clickedNode = node;
                        mf.getInfoPanel().setNameInfo(node.getId());
                    } else {
                        node.setSelected(false);
                    }
                }
                for (Road road : mf.getElementManager().getRoads()) {
                    if (road.contain(x, y) && !nodeClicked) {
                        
                        selectedRoad = road;
                        road.setSelected(true);
                        
                    } else {
                        road.setSelected(false);
                    }
                }               
			}
			else if (editorMode == EditorMode.INSERTING_ROAD_STARTPOINT) {
				startNode = findNodeThatContain(e.getX(), e.getY());
				if (startNode != null) {
					setMode(EditorMode.INSERTING_ROAD_ENDPOINT);
				}
			}
			else if (editorMode == EditorMode.MOVING) {
                if (selectedNode == null) {
                    boolean nodeClicked = false;
                    for (Node node : nodes) {
                        if (node.contain(x, y)) {
                            nodeClicked = true;
                            node.setSelected(true);
                            selectedNode = node;
                            clickedNode = node;
                            mf.getInfoPanel().setNameInfo(node.getId());
                        } else {
                            node.setSelected(false);
                        }
                    }
                    for (Road road : mf.getElementManager().getRoads()) {
                        if (road.contain(x, y) && !nodeClicked) {
                            
                            selectedRoad = road;
                            road.setSelected(true);
                            
                        } else {
                            road.setSelected(false);
                        }
                    }                              
                    System.err.println("Zaznacz punkt poczatkowy");
                }
                else {
                    mf.setProjectChanged(true);
                    selectedNode.setX(e.getX());
                    selectedNode.setY(e.getY());
                    selectedNode.setSelected(false);
                    selectedNode = null;
 //                   editorMode = EditorMode.SELECTING;
                    log.trace("Node was moved");
                }
                //setMode(EditorMode.SELECTING);
			}
			else if (e.getButton() == MouseEvent.BUTTON1) {
			    // INSERTING
        		if (editorMode == EditorMode.INSERTING_ROAD_ENDPOINT) {
        			endNode = findNodeThatContain(e.getX(), e.getY());
        			if (endNode != null) {
        				if (startNode == null) { log.error("startNode null, endNode not null"); }
        				log.trace("Inserting road");
        				mf.getElementManager().addRoad(startNode,endNode);;
        				setMode(EditorMode.INSERTING_ROAD_STARTPOINT);
        			}
        		}
        		else if (editorMode == EditorMode.INSERTING_GATEWAY) {
        		    mf.setProjectChanged(true);
        			mf.getElementManager().addGateway(null, e.getX(),e.getY());	
        		} 
        		else if (editorMode == EditorMode.INSERTING_INTERSECTION) {
        		    mf.setProjectChanged(true);
        			mf.getElementManager().addIntersection(null, e.getX(),e.getY());					
        		}
			}
			
			if (e.getButton() != MouseEvent.BUTTON1) {
			    editorMode = EditorMode.SELECTING;
			}
	        
			maybeShowPopup(e);
	         
			repaint();
			System.out.println("repaint ");
		}
		
	}
		
	public static enum EditorMode {
		INSERTING_INTERSECTION, INSERTING_GATEWAY, SELECTING, DELETING,
		INSERTING_ROAD_STARTPOINT, INSERTING_ROAD_ENDPOINT,
		MOVING
	}
	
	private class MoveActionListener implements ActionListener {
        
        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            editorMode = EditorMode.MOVING;
        } 
	}
	
    private class NodePropertiesActionListener implements ActionListener {
        
        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            IntersectionPropertiesDialog intersectionPropertiesDialog = new IntersectionPropertiesDialog(mf);
            intersectionPropertiesDialog.setPosition(lastmouseclick_x, lastmouseclick_y);
            assert clickedNode != null;
            
            if (clickedNode instanceof Intersection) {
                intersectionPropertiesDialog.setIntersection((Intersection)clickedNode);
                intersectionPropertiesDialog.init();
                intersectionPropertiesDialog.setVisible(true);
                intersectionPropertiesDialog.scrollToBegining();
            }
            else {
                
            }
        }
    }
    
    private class NodeDeleteActionListener implements ActionListener{
    	
	    public void actionPerformed(ActionEvent e) {
    		mf.getElementManager().deleteNode(selectedNode);
    		mf.getEditorPanel().repaint();
    	}
    }
    
    private class SetDefaultActionsActionListener implements ActionListener{
        
        public void actionPerformed(ActionEvent e) {
            
            Intersection clickedIntersection = (Intersection)selectedNode;
            
            if (clickedIntersection.checkRoadsFor3WaySimple() == false) {
                JOptionPane.showMessageDialog(mf, "Can't create default actions. Roads");
                return;
            }
            if (clickedIntersection.checkAnglesFor3WaySimple() == null) {
                JOptionPane.showMessageDialog(mf, "Can't create default actions. Angles");
                return;
            }

            clickedIntersection.createDefaultActions3WaySimpleAngles(true);
            repaint();
        }
    }
    
    private class RoadPropertiesActionListener implements ActionListener{
        
        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            RoadPropertiesDialog roadPropertiesDialog = new RoadPropertiesDialog(mf);
            roadPropertiesDialog.setPosition(lastmouseclick_x, lastmouseclick_y);
            roadPropertiesDialog.initByRoad(selectedRoad);
            roadPropertiesDialog.setVisible(true);       
        }
    }
    
    private class RoadDeleteActionListener implements ActionListener {

        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            
            int returnVal = JOptionPane.showConfirmDialog(EditorPanel.this.getParent(), "Road will be deleted. Continue?" , "Confirmation", JOptionPane.YES_NO_OPTION);
            if (returnVal == JOptionPane.YES_OPTION) {
                mf.getElementManager().deleteRoad(selectedRoad);
                mf.getEditorPanel().repaint();
                mf.setProjectChanged(true);
            }
        }
        
    }
    
    private class RecalculateDistanceActionListener implements ActionListener {

        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            selectedRoad.recalculateLinksDistances();
        }
    }
		
	/**
	 * Instantiates a new editor panel.
	 * 
	 * @param mainFrame the main frame
	 * @param parentScrollPane the parent scroll pane
	 * @param ds the ds
	 */
	public EditorPanel(MainFrame mainFrame, JScrollPane parentScrollPane, DisplaySettings ds) {
		super();
		
		this.mf = mainFrame;
		this.editorMode = EditorMode.SELECTING;
		setPreferredSize(defaultDimension);
		setMaximumSize(defaultDimension);
		this.addMouseListener(new EditorMouseAdapter());
		this.backgorundMap = (Toolkit.getDefaultToolkit().getImage(BgImageData.getDefault().getFileName()));
		this.x=0;
		this.y=0;
		this.graphicsExtent = this.getBounds();
		this.parentScrollPane = parentScrollPane;
		this.ds = ds;
		//this.intersectionPropertiesDialog = new IntersectionPropertiesDialog(mf);
		//this.roadPropertiesDialog = new RoadPropertiesDialog(mf);
		
		//this.intersectionPropertiesDialog = new IntersectionPropertiesDialog(mf);
		JMenuItem menuItem;
		intersectionPopup = new JPopupMenu();
	    menuItem = new JMenuItem("Move");
	    menuItem.addActionListener(new MoveActionListener());
	    intersectionPopup.add(menuItem);
	    menuItem = new JMenuItem("Properties");
	    menuItem.addActionListener(new NodePropertiesActionListener());
	    intersectionPopup.add(menuItem);
	    menuItem = new JMenuItem("Delete");
	    menuItem.addActionListener(new NodeDeleteActionListener());
	    intersectionPopup.add(menuItem);
	    menuItem = new JMenuItem("Set default actions");
	    menuItem.addActionListener(new SetDefaultActionsActionListener());
	    intersectionPopup.add(menuItem);
	    
	    gatewayPopup = new JPopupMenu();
	    menuItem = new JMenuItem("Move");
	    menuItem.addActionListener(new MoveActionListener());
	    gatewayPopup.add(menuItem);
	    menuItem = new JMenuItem("Delete");
	    menuItem.addActionListener(new NodeDeleteActionListener());
	    gatewayPopup.add(menuItem);

	    JMenuItem roadMenuItem;
	    roadPopup = new JPopupMenu();
	    roadMenuItem = new JMenuItem("Properties");
	    roadMenuItem.addActionListener(new RoadPropertiesActionListener());
	    roadPopup.add(roadMenuItem);
	    roadMenuItem = new JMenuItem("Recalculate distance");
	    roadMenuItem.addActionListener(new RecalculateDistanceActionListener());
	    roadPopup.add(roadMenuItem);
	    roadMenuItem = new JMenuItem("Delete");
	    roadMenuItem.addActionListener(new RoadDeleteActionListener());
	    roadPopup.add(roadMenuItem);
	}
	
	/**
	 * Sets the mode.
	 * 
	 * @param editorMode the new mode
	 */
	public void setMode(EditorMode editorMode) {
		this.editorMode = editorMode;
		mf.getInfoPanel().setModeInfo(editorMode.toString());
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);		
		Graphics2D g2d = (Graphics2D) g;
		
		//drawing map
		drawBackgroundMap(g2d);
		log.debug("repaint");
		
		// drawing roads
		for (Road road : mf.getElementManager().getRoads()) {
		    drawRoad(g2d,road);
		}
		
		// drawing intersections
		for (Intersection intersection : mf.getElementManager().getIntersections()) {
			drawIntersection(g2d,intersection);
		}
		// drawing gateways
		for (Gateway gateway : mf.getElementManager().getGateways()) {
			drawGateway(g2d,gateway);
		}
	}
	
	/**
	 * Draw road.
	 * 
	 * @param g the g
	 * @param road the road
	 */
	public void drawRoad(Graphics2D g, Road road) {
		log.trace("Drawing road");
		
		g.setColor( road.isSelected() ? Color.GREEN : ds.getLinkColor() );
		if (road.getUplink() != null) {
			log.trace("Drawing uplink");
			g.fill(road.getUplink().getShape());			
		}
		if (road.getDownlink() != null) {
			log.trace("Drawing downlink");
			g.fill(road.getDownlink().getShape());
		}
	}

	/**
	 * Draw intersection.
	 * 
	 * @param g the g
	 * @param inter the inter
	 */
	public void drawIntersection(Graphics2D g, Intersection inter) {
	    	    
		double x = inter.getX() - (inter.getWidth() / 2.0);
		double y = inter.getY() - (inter.getHeight() / 2.0);
		Rectangle2D rect = new Rectangle2D.Double(x,y,inter.getWidth(),inter.getHeight());
		
		if (inter.getSelected()) {
		    g.setColor(Color.GREEN);		    
		} else if (inter.allIncomingLanesUsed() == true && inter.allOutcomingLinksUsed() == true) {
            g.setColor(ds.getIntersectionColor());
        } else {
            g.setColor(ds.getNotConfiguredintersectionColor());
        }
	    g.fill(rect);   

	    FontRenderContext frc = g.getFontRenderContext();
	    Font f = new Font("Arial",Font.BOLD, 25);
	    String s = new String(inter.getId());
	    TextLayout tl = new TextLayout(s, f, frc);
	    g.setColor(Color.red);
	    tl.draw(g, (float)x+30, (float)y+18);
	}
	
	/**
	 * Draw gateway.
	 * 
	 * @param g the g
	 * @param gateway the gateway
	 */
	public void drawGateway(Graphics2D g, Gateway gateway) {
		double x = gateway.getX() - (gateway.getWidth() / 2.0);
		double y = gateway.getY() - (gateway.getHeight() / 2.0);
		Rectangle2D rect = new Rectangle2D.Double(x,y,gateway.getWidth(),gateway.getHeight());	
	    g.setColor( gateway.getSelected() ? Color.GREEN : ds.getGatewayColor() );
	    g.fill(rect);   
	    
	    FontRenderContext frc = g.getFontRenderContext();
	    Font f = new Font("Arial",Font.BOLD, 25);
	    String s = new String(gateway.getId());
	    TextLayout tl = new TextLayout(s, f, frc);
	    g.setColor(Color.red);
	    tl.draw(g, (float)x+30, (float)y+18);
	}
	
	/**
	 * Draw background map.
	 * 
	 * @param g the g
	 */
	private void drawBackgroundMap(Graphics g){
		Graphics2D g2d = (Graphics2D) g;
		
		if(this.backgorundMap != null) {
			this.setPreferredSize(new Dimension(backgorundMap.getWidth(this), backgorundMap.getHeight(this)));
			g2d.drawImage(this.backgorundMap, 0, 0, this);
		}
	}
	
	/**
	 * Find node that contain.
	 * 
	 * @param x the x
	 * @param y the y
	 * 
	 * @return the node
	 */
	private Node findNodeThatContain(int x, int y) {

		for (Node node : mf.getElementManager().getNodes()) {
			if (node.contain(x, y)) {
			    return node;
			}
		}
		return null;
	}
	
	/**
	 * Sets the backgroun image.
	 * 
	 * @param image the new backgroun image
	 */
	public void setBackgrounImage(Image image) {
		this.backgorundMap = image;
	}
	
	/**
	 * Translate.
	 * 
	 * @param dx the dx
	 * @param dy the dy
	 */
	public void translate(int dx, int dy) {
		this.x += dx;// *scaleFactor;
		this.y += dy;// *scaleFactor;
      
        if(this.x > 0) this.x =0;
        if(this.y > 0) this.y =0;
        if( this.x < -backgorundMap.getWidth(this)+graphicsExtent.x){
        	this.x = -backgorundMap.getWidth(this)+graphicsExtent.x;
        }
     
        if(this.y < -backgorundMap.getHeight(this)+graphicsExtent.y){
        	this.y = -backgorundMap.getHeight(this)+graphicsExtent.y;
        }
 
        this.repaint();
	}
	
	/**
	 * Scroll.
	 * 
	 * @param dx the dx
	 * @param dy the dy
	 */
	public void scroll(int dx, int dy){
//		this.parentScrollPane.getHorizontalScrollBar().setValueIsAdjusting(true);
		this.parentScrollPane.getHorizontalScrollBar().setValue(this.parentScrollPane.getHorizontalScrollBar().getValue() - dx);
//		this.parentScrollPane.getHorizontalScrollBar().setValueIsAdjusting(false);
//		this.parentScrollPane.getVerticalScrollBar().setValueIsAdjusting(true);
        this.parentScrollPane.getVerticalScrollBar().setValue(this.parentScrollPane.getVerticalScrollBar().getValue() - dy);
//		this.parentScrollPane.getVerticalScrollBar().setValueIsAdjusting(false);

       
        this.repaint();
	}
}



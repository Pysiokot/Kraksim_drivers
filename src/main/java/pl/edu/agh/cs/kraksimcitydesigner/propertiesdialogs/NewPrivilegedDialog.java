package pl.edu.agh.cs.kraksimcitydesigner.propertiesdialogs;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.*;

import pl.edu.agh.cs.kraksimcitydesigner.element.Intersection;
import pl.edu.agh.cs.kraksimcitydesigner.element.Link;
import pl.edu.agh.cs.kraksimcitydesigner.element.Node;
import pl.edu.agh.cs.kraksimcitydesigner.element.Intersection.IncomingLane;

// TODO: Auto-generated Javadoc
public class NewPrivilegedDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    
    private ActionPropertiesDialog actionPropertiesDialog;
    
    /**
     * Instantiates a new new privileged dialog.
     * 
     * @param owner the owner
     */
    public NewPrivilegedDialog(ActionPropertiesDialog owner) {
        
        super(owner,true);
        actionPropertiesDialog = owner;  
    }
    
    /**
     * Inits the.
     */
    public void init() {
        this.getContentPane().removeAll();
        JPanel mainPanel = new JPanel();
      
        final Intersection.Action action = actionPropertiesDialog.getAction();
        final Intersection parentIntersection = action.getArmActions().getIntersection();
        
        final Map<String, List<Intersection.IncomingLane>> armIncomingLanesStrings = new TreeMap<String, List<Intersection.IncomingLane>>();
        final Map<String, Node> armNameToNode = new HashMap<String, Node>();
        
        for (Link incomingLink : parentIntersection.getIncomingLinks()) {
            
            List<Intersection.IncomingLane> availableIncomingLane = new LinkedList<Intersection.IncomingLane>();
            if (incomingLink.getStartNode() != action.getArmActions().getArm()) {
                
                for (IncomingLane incomingLane : incomingLink.getIncomingLanes()) {
                    if (!action.containPrivilegedIncomingLane(incomingLane)) {
                        availableIncomingLane.add(incomingLane);
                    } 
                }
            }           
            String armName = incomingLink.getStartNode().getId();
            if (availableIncomingLane.size() > 0) {
                armIncomingLanesStrings.put(armName, availableIncomingLane);
                armNameToNode.put(armName, incomingLink.getStartNode());
            }
        }
        final JComboBox incomingLanesComboBox = new JComboBox();
        final JComboBox armsComboBox = new JComboBox(armIncomingLanesStrings.keySet().toArray());
        incomingLanesComboBox.setPreferredSize(new Dimension(230,20));
        armsComboBox.setPreferredSize(new Dimension(150,20));
        
        armsComboBox.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                incomingLanesComboBox.removeAllItems();
                List<Intersection.IncomingLane> availableIncomingLanes = armIncomingLanesStrings.get(armsComboBox.getSelectedItem());
                for (Intersection.IncomingLane availableIncomingLane : availableIncomingLanes) {
                    incomingLanesComboBox.addItem(availableIncomingLane.getLaneNum());
                }

            }
        });
        if (armsComboBox.getItemCount() > 0) {
            armsComboBox.getActionListeners()[0].actionPerformed(null);
        }
            
        JButton addPrivilegedActionButton = new JButton("Add privileged action");
        JButton cancelButton = new JButton("Cancel");
        
        addPrivilegedActionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (armsComboBox.getItemCount() > 0 && incomingLanesComboBox.getItemCount() > 0) {
                    
                    Node fromNode = armNameToNode.get(armsComboBox.getSelectedItem());
                    int lane = Integer.parseInt(incomingLanesComboBox.getSelectedItem().toString());
                    
                    Intersection.IncomingLane chosenIncomingLane = new Intersection.IncomingLane(fromNode,lane);
                    action.getPrivileged().add(chosenIncomingLane);
                    setVisible(false);
                    
                    actionPropertiesDialog.recreate();
                    actionPropertiesDialog.getIntersectionPropertiesDialog().refresh();
                }      
            } 
        });
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        
        Container horizontalComboBoxContainer = Box.createHorizontalBox();
        horizontalComboBoxContainer.add(new JLabel("from "));
        horizontalComboBoxContainer.add(armsComboBox);
        horizontalComboBoxContainer.add(new JLabel("  line "));
        horizontalComboBoxContainer.add(incomingLanesComboBox);
        
        Container horizontalButtonsContainer = Box.createHorizontalBox();
        horizontalButtonsContainer.add(addPrivilegedActionButton);
        horizontalButtonsContainer.add(Box.createRigidArea(new Dimension(20,0)));
        horizontalButtonsContainer.add(cancelButton);
        
        Container verticalMainContanier = Box.createVerticalBox();
        verticalMainContanier.add(horizontalComboBoxContainer);
        verticalMainContanier.add(Box.createRigidArea(new Dimension(0,5)));
        verticalMainContanier.add(horizontalButtonsContainer);
        
        mainPanel.add(verticalMainContanier);
        this.add(mainPanel);
        
        this.pack();
    }
    
    /**
     * Show centered.
     */
    public void showCentered() {
        Dialog parent = (Dialog)getParent();
        Dimension dim = parent.getSize();
        Point     loc = parent.getLocationOnScreen();

        Dimension size = getSize();

        loc.x += (dim.width  - size.width)/2;
        loc.y += (dim.height - size.height)/2;

        if (loc.x < 0) loc.x = 0;
        if (loc.y < 0) loc.y = 0;

        Dimension screen = getToolkit().getScreenSize();

        if (size.width  > screen.width)
          size.width  = screen.width;
        if (size.height > screen.height)
          size.height = screen.height;

        if (loc.x + size.width > screen.width)
          loc.x = screen.width - size.width;

        if (loc.y + size.height > screen.height)
          loc.y = screen.height - size.height;

        setBounds(loc.x, loc.y, size.width, size.height);
        setVisible(true);
      }
}

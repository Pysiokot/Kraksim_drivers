package pl.edu.agh.cs.kraksimcitydesigner.propertiesdialogs;

import javax.swing.JDialog;
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
import java.util.Set;
import java.util.TreeMap;

import javax.swing.*;

import pl.edu.agh.cs.kraksimcitydesigner.element.Intersection;
import pl.edu.agh.cs.kraksimcitydesigner.element.Link;
import pl.edu.agh.cs.kraksimcitydesigner.element.Node;
import pl.edu.agh.cs.kraksimcitydesigner.element.Intersection.IncomingLane;

// TODO: Auto-generated Javadoc
public class AddingActionDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    final private IntersectionPropertiesDialog owner;
    final private Link incomingLink;
    final private Set<Link> outcomingLinks;
    final private Intersection.ArmActions armActions;
    
    /**
     * Instantiates a new adding action dialog.
     * 
     * @param owner the owner
     * @param armActions the arm actions
     * @param incomingLink the incoming link
     * @param outcomingLinks the outcoming links
     */
    public AddingActionDialog(IntersectionPropertiesDialog owner, Intersection.ArmActions armActions, Link incomingLink, Set<Link> outcomingLinks) {
        
        super(owner,true);
        this.owner = owner; 
        this.incomingLink = incomingLink;
        this.outcomingLinks = outcomingLinks;
        this.armActions = armActions;
    }
    
    /**
     * Inits the.
     */
    public void init() {
        this.getContentPane().removeAll();
        Container verticalMainContanier = Box.createVerticalBox();
        
        final Map<Integer, List<Node>> availableNodesMap = new HashMap<Integer, List<Node>>();
        for (int laneNum = -1; laneNum <= 1; laneNum++) {
            if (  (laneNum == -1 && this.incomingLink.getLeftLines().size() > 0)
               || (laneNum ==  0 && this.incomingLink.getNumberOfLines() > 0)
               || (laneNum ==  1 && this.incomingLink.getRightLines().size() > 0) )
            {
                List<Node> availableNodesFromThisLane = new LinkedList<Node>();
                
                for (Link outcomingLink : this.outcomingLinks) {
                    if (outcomingLink.getEndNode() != incomingLink.getStartNode()) {                  
                        
                        Node reachableNode = outcomingLink.getEndNode();
                        System.out.println("reachableNode "+reachableNode.getId());
                        
                        assert armActions != null;
                        if (! armActions.contain(laneNum, reachableNode)) {
                            availableNodesFromThisLane.add(reachableNode);
                        }
                    }
                }
                if (availableNodesFromThisLane.size() > 0) {
                    availableNodesMap.put(laneNum, availableNodesFromThisLane);
                }
            }
        }
       
        final JComboBox laneNumComboBox = new JComboBox(availableNodesMap.keySet().toArray());
        final JComboBox availableNodesComboBox = new JComboBox();
        
        laneNumComboBox.setPreferredSize(new Dimension(120,20));
        availableNodesComboBox.setPreferredSize(new Dimension(150,20));
        
        laneNumComboBox.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                availableNodesComboBox.removeAllItems();
                
                int laneNum = Integer.parseInt(laneNumComboBox.getSelectedItem().toString());
                for (Node availableNode : availableNodesMap.get(laneNum)) {
                    availableNodesComboBox.addItem(availableNode.getId());
                }
            }
        });
        
        if (laneNumComboBox.getItemCount() > 0) {
            laneNumComboBox.getActionListeners()[0].actionPerformed(null);
        }
        
        Container comboBoxesHorizontalContainer = Box.createHorizontalBox();
        comboBoxesHorizontalContainer.add(new JLabel("line  "));
        comboBoxesHorizontalContainer.add(laneNumComboBox);
        comboBoxesHorizontalContainer.add(new JLabel("  to node  "));
        comboBoxesHorizontalContainer.add(availableNodesComboBox);
        
        JButton addButton = new JButton("Add action");
        addButton.addActionListener(new ActionListener() {
           @Override
            public void actionPerformed(ActionEvent e) {
               if (laneNumComboBox.getItemCount() > 0) {
                   Integer line = Integer.parseInt(laneNumComboBox.getSelectedItem().toString());
                   String exitNodeId = availableNodesComboBox.getSelectedItem().toString();
                   Node exitNode = null;
                   for (Node node : availableNodesMap.get(line)) {
                       if (node.getId().equals(exitNodeId)) {
                           exitNode = node;
                           break;
                       }
                   }
                   assert exitNode != null;
                   armActions.addAction(exitNode, line);
                   owner.init();
                   setVisible(false);
               }
            } 
        });
        
        JButton cancelButton = new JButton("Cancel button");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        
        Container horizontalButtonsContainer = Box.createHorizontalBox();
        horizontalButtonsContainer.add(addButton);
        horizontalButtonsContainer.add(cancelButton);
        
        verticalMainContanier.add(comboBoxesHorizontalContainer);
        verticalMainContanier.add(horizontalButtonsContainer);
        this.getContentPane().add(verticalMainContanier);
        
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


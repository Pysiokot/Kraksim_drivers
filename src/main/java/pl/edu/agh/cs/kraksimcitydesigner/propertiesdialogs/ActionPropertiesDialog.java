package pl.edu.agh.cs.kraksimcitydesigner.propertiesdialogs;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import pl.edu.agh.cs.kraksimcitydesigner.element.Intersection;

// TODO: Auto-generated Javadoc
public class ActionPropertiesDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    
    private Intersection.Action action;
    private IntersectionPropertiesDialog intersectionPropertiesDialog;
    private NewPrivilegedDialog newPrivilegedDialog;
    
    /**
     * Instantiates a new action properties dialog.
     * 
     * @param intersectionPropertiesDialog the intersection properties dialog
     */
    public ActionPropertiesDialog(IntersectionPropertiesDialog intersectionPropertiesDialog) {
        super(intersectionPropertiesDialog,"Action properties",true);
        this.intersectionPropertiesDialog = intersectionPropertiesDialog;
        this.newPrivilegedDialog = new NewPrivilegedDialog(this);
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

    
    /**
     * Inits the.
     */
    public void init() {
        getContentPane().removeAll();
        Container mainVerticalContainer = Box.createVerticalBox();
        mainVerticalContainer.add(new JLabel("Action: exit = "+action.getExitNode().getId()+", line = "+action.getLineNum()));
        
        JPanel priviledged = createPrivilegedPanel();
        JButton addNewButton = new JButton("Add new privileged action");
        addNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                newPrivilegedDialog.init();
                newPrivilegedDialog.showCentered();
            }
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        
        Box buttonsHorizontalContainer = Box.createHorizontalBox();
        buttonsHorizontalContainer.add(addNewButton);
        buttonsHorizontalContainer.add(cancelButton);
        
        mainVerticalContainer.add(priviledged);
        mainVerticalContainer.add(buttonsHorizontalContainer);
        this.add(mainVerticalContainer);
        this.pack();
    }

    /**
     * Creates the privileged panel.
     * 
     * @return the j panel
     */
    private JPanel createPrivilegedPanel() {
        JPanel resultPanel = new JPanel();
        Container verticalContanier = Box.createVerticalBox();
        
        for (final Intersection.IncomingLane privilegedIncomingLane : action.getPrivileged()) {
            Container horizontalActionContainer = Box.createHorizontalBox();
            JButton deletePrivilegedActionButton = new JButton("delete");
            deletePrivilegedActionButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    action.removePrivileged(privilegedIncomingLane);
                    init();
                    intersectionPropertiesDialog.refresh();
                }
            });
            JLabel priviligedActionLabel = new JLabel("arm = "+privilegedIncomingLane.getFromNode().getId()+", line = "+privilegedIncomingLane.getLaneNum());
            priviligedActionLabel.setPreferredSize(new Dimension(170,10));
            
            horizontalActionContainer.add(priviligedActionLabel);
            horizontalActionContainer.add(Box.createHorizontalGlue());
            horizontalActionContainer.add(deletePrivilegedActionButton);
            verticalContanier.add(horizontalActionContainer);
        }
        resultPanel.add(verticalContanier);
        return resultPanel;
    }

    /**
     * Sets the action.
     * 
     * @param action the new action
     */
    public void setAction(Intersection.Action action) {
        this.action = action;
    }

    /**
     * Gets the action.
     * 
     * @return the action
     */
    public Intersection.Action getAction() {
        return action;
    }

    /**
     * Recreate.
     */
    public void recreate() {
        setVisible(false);
        init();
        setVisible(true);
    }

    /**
     * Gets the intersection properties dialog.
     * 
     * @return the intersection properties dialog
     */
    public IntersectionPropertiesDialog getIntersectionPropertiesDialog() {
        return intersectionPropertiesDialog;
    }

}

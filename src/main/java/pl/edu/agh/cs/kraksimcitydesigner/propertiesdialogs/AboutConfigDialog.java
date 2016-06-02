package pl.edu.agh.cs.kraksimcitydesigner.propertiesdialogs;

import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JLabel;

import pl.edu.agh.cs.kraksimcitydesigner.MainFrame;
import pl.edu.agh.cs.kraksimcitydesigner.element.ElementManager;

public class AboutConfigDialog extends JDialog {
    
    private MainFrame mainFrame;
    private ElementManager elementManager;
    
    private JLabel numOfGatesLabel = new JLabel("");
    private JLabel numOfIntersectionsLabel = new JLabel("");
    private JLabel numOfRoadsLabel = new JLabel("");
    
    public AboutConfigDialog(MainFrame mainFrame, ElementManager elementManager) {
        
        this.mainFrame = mainFrame;
        this.elementManager = elementManager;
        
        Box verticalContainer = Box.createVerticalBox();
        
        Box horizontalContainer;
        horizontalContainer = Box.createHorizontalBox();
        horizontalContainer.add(new JLabel("numOfGates: "));
        horizontalContainer.add(numOfGatesLabel);
        verticalContainer.add(horizontalContainer);
        
        horizontalContainer = Box.createHorizontalBox();
        horizontalContainer.add(new JLabel("numOfIntersectionsLabel: "));
        horizontalContainer.add(numOfIntersectionsLabel);
        verticalContainer.add(horizontalContainer);
        
        horizontalContainer = Box.createHorizontalBox();
        horizontalContainer.add(new JLabel("numOfRoads: "));
        horizontalContainer.add(numOfRoadsLabel);
        verticalContainer.add(horizontalContainer);
        
        this.numOfGatesLabel.setText(""+elementManager.getGateways().size());
        this.numOfIntersectionsLabel.setText(""+elementManager.getIntersections().size());
        this.numOfRoadsLabel.setText(""+elementManager.getRoads().size());
        
        this.getContentPane().add(verticalContainer);
        this.pack();
        this.setVisible(true);
        
    }

}

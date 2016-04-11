package pl.edu.agh.cs.kraksimcitydesigner.propertiesdialogs;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

// TODO: Auto-generated Javadoc
public class AddLaneDialog extends JDialog {
    

    private static final long serialVersionUID = 6102236567160587354L;
    
    RoadPropertiesDialog callbackAddressee;
    JTextField lengthTextField;
    JFrame mainFrame;
    JPanel mainPanel;
    JButton addButton;
    JButton cancelButton; 
    List<Integer> lanes;
    String title;
    
    /**
     * Instantiates a new adds the lane dialog.
     * 
     * @param callbackAddressee the callback addressee
     * @param lanes the lanes
     * @param mainFrame the main frame
     * @param title the title
     */
    public AddLaneDialog(RoadPropertiesDialog callbackAddressee, List<Integer> lanes, JFrame mainFrame, String title) {
        super(mainFrame);
        this.callbackAddressee = callbackAddressee;
        this.mainFrame = mainFrame;
        this.setResizable(false);
        
        this.setPreferredSize(new Dimension(200, 80));
        this.lanes = lanes;
        
        lengthTextField = new JTextField();
        lengthTextField.selectAll();
        addButton = new JButton("Add");
        cancelButton = new JButton("Cancel");
        mainPanel = new JPanel();
        
        this.title = title;
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    }
    
    /**
     * Inits the.
     */
    public void init() {
        this.setTitle(title);
        
        lengthTextField.setText("0");
        lengthTextField.setMinimumSize(new Dimension(25,20));
        lengthTextField.setPreferredSize(new Dimension(25,20));
        lengthTextField.selectAll();
        cancelButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelAction();
            }
        });
        
        addButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                addAction();
            }
        });
        
        Box box = Box.createHorizontalBox();
        this.add(mainPanel);
        box.add(lengthTextField);
        box.add(Box.createHorizontalStrut(10));
        box.add(addButton);
        box.add(Box.createHorizontalStrut(10));
        box.add(cancelButton);
        mainPanel.add(box);
        pack();
    }
    
    /**
     * Cancel action.
     */
    public void cancelAction(){
        this.dispose();
    }
    
    /**
     * Adds the action.
     */
    public void addAction(){
        this.lanes.add(new Integer(lengthTextField.getText()));
        this.dispose();
        this.callbackAddressee.refreshDynamicContent();
    }
}

package pl.edu.agh.cs.kraksimcitydesigner.propertiesdialogs;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;

import pl.edu.agh.cs.kraksimcitydesigner.element.Intersection;
import pl.edu.agh.cs.kraksimcitydesigner.element.Link;
import pl.edu.agh.cs.kraksimcitydesigner.element.Node;
import pl.edu.agh.cs.kraksimcitydesigner.element.Road;

// TODO: Auto-generated Javadoc
public class RoadPropertiesDialog extends JDialog{
    public class DeleteLaneActionListener implements ActionListener {
        List<Integer> lanes;
        Integer laneToDelete;
        
        /**
         * Instantiates a new delete lane action listener.
         * 
         * @param lanes the lanes
         * @param laneToDelete the lane to delete
         */
        DeleteLaneActionListener(List<Integer> lanes, Integer laneToDelete) {
            this.lanes = lanes;
            this.laneToDelete = laneToDelete;
        }
        
        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            this.lanes.remove(laneToDelete);
            refreshDynamicContent();
        };
    }
    
    public class AddLaneActionListener implements ActionListener {
        List<Integer> lanes;
        RoadPropertiesDialog callbackAddressee;
        String title;
        Point dialogPosition;
        
        /**
         * Instantiates a new adds the lane action listener.
         * 
         * @param callbackAddressee the callback addressee
         * @param lanes the lanes
         * @param title the title
         */
        AddLaneActionListener(RoadPropertiesDialog callbackAddressee, List<Integer> lanes, String title) {
            this.lanes = lanes;
            this.title = title;
            this.callbackAddressee = callbackAddressee;
        }
        
        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            AddLaneDialog addLaneDialog = new AddLaneDialog(callbackAddressee, lanes, mainFrame, title);
            addLaneDialog.init();
            addLaneDialog.setVisible(true);
        }

    }
    
    private static final long serialVersionUID = 701357110147121100L;
    private static Logger log = Logger.getLogger(RoadPropertiesDialog.class);
    private JFrame mainFrame;
    private Road road;
    private RoadEditableSpecification roadSpecification;
    private JPanel uplinkRightLanesPanel = new JPanel();
    private JPanel uplinkLeftLanesPanel = new JPanel();
    private JPanel downlinkRightLanesPanel = new JPanel();
    private JPanel downlinkLeftLanesPanel = new JPanel();
    // Variables declaration
    private javax.swing.JLabel downlinkLabel;
    private javax.swing.JLabel downlinkDirectionLabel;
    private javax.swing.JScrollPane downlinkLeftLanesScrollPane;
    private javax.swing.JButton downlinkLeftTurnLanesAddButton;
    private javax.swing.JLabel downlinkLeftTurnLanesLabel;
    private javax.swing.JLabel downlinkLengthLabel;
    private javax.swing.JLabel downlinkNumOfLanesLabel;
    private javax.swing.JTextField downlinkNumOfLanesTextField;
    private javax.swing.JScrollPane downlinkRightLanesScrollPane;
    private javax.swing.JButton downlinkRightTurnLanesAddButton;
    private javax.swing.JLabel downlinkRightTurnLanesLabel;
    private javax.swing.JSeparator footerSeparator;
    private javax.swing.JSeparator headerSeparator;
    private javax.swing.JButton saveButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField downlinkLengthTextField;
    private javax.swing.JLabel roadIdLabel;
    private javax.swing.JLabel speedLimitLabel;
    private javax.swing.JTextField roadIdTextField;
    private javax.swing.JTextField speedLimitTextField;
    private javax.swing.JSeparator streetFromLinksSeparator;
    private javax.swing.JLabel streetNameLabel;
    private javax.swing.JTextField streetNameTextField;
    private javax.swing.JLabel uplinkDirectionLabel;
    private javax.swing.JLabel uplinkLabel;
    private javax.swing.JScrollPane uplinkLeftLanesScrollPane;
    private javax.swing.JButton uplinkLeftTurnLanesAddButton;
    private javax.swing.JLabel uplinkLeftTurnLanesLabel;
    private javax.swing.JLabel uplinkLengthLabel;
    private javax.swing.JTextField uplinkLengthTextField;
    private javax.swing.JLabel uplinkNumOfLanesLabel;
    private javax.swing.JTextField uplinkNumOfLanesTextField;
    private javax.swing.JScrollPane uplinkRightLanesScrollPane;
    private javax.swing.JButton uplinkRightTurnLanesAddButton;
    private javax.swing.JLabel uplinkRightTurnLanesLabel;
    private javax.swing.JSeparator verticalSeparator;
    // End of variables declaration
    
    /**
     * Instantiates a new road properties dialog.
     * 
     * @param mainFrame the main frame
     */
    public RoadPropertiesDialog(JFrame mainFrame){
        super(mainFrame);
        this.mainFrame = mainFrame;
        this.setResizable(false);
        initComponents();
    }
    

    /**
     * Inits the components.
     */
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        headerSeparator = new javax.swing.JSeparator();
        streetFromLinksSeparator = new javax.swing.JSeparator();
        roadIdLabel = new javax.swing.JLabel();
        speedLimitLabel = new javax.swing.JLabel();
        roadIdTextField = new javax.swing.JTextField();
        speedLimitTextField = new javax.swing.JTextField();
        streetNameLabel = new javax.swing.JLabel();
        streetNameTextField = new javax.swing.JTextField();
        verticalSeparator = new javax.swing.JSeparator();
        uplinkLabel = new javax.swing.JLabel();
        downlinkLabel = new javax.swing.JLabel();
        uplinkDirectionLabel = new javax.swing.JLabel();
        downlinkDirectionLabel = new javax.swing.JLabel();
        uplinkNumOfLanesLabel = new javax.swing.JLabel();
        uplinkNumOfLanesTextField = new javax.swing.JTextField();
        downlinkNumOfLanesLabel = new javax.swing.JLabel();
        downlinkNumOfLanesTextField = new javax.swing.JTextField();
        uplinkRightTurnLanesLabel = new javax.swing.JLabel();
        uplinkLeftTurnLanesLabel = new javax.swing.JLabel();
        uplinkLengthLabel = new javax.swing.JLabel();
        downlinkLengthLabel = new javax.swing.JLabel();
        uplinkLengthTextField = new javax.swing.JTextField();
        downlinkLengthTextField = new javax.swing.JTextField();
        downlinkRightTurnLanesLabel = new javax.swing.JLabel();
        downlinkLeftTurnLanesLabel = new javax.swing.JLabel();
        uplinkLeftTurnLanesAddButton = new javax.swing.JButton();
        downlinkLeftTurnLanesAddButton = new javax.swing.JButton();
        uplinkRightTurnLanesAddButton = new javax.swing.JButton();
        downlinkRightTurnLanesAddButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        footerSeparator = new javax.swing.JSeparator();
        downlinkRightLanesScrollPane = new javax.swing.JScrollPane();
        uplinkRightLanesScrollPane = new javax.swing.JScrollPane();
        downlinkLeftLanesScrollPane = new javax.swing.JScrollPane();
        uplinkLeftLanesScrollPane = new javax.swing.JScrollPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 228, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 121, Short.MAX_VALUE)
        );

        roadIdLabel.setText("Road id:");
        speedLimitLabel.setText("Speed limit:");
        streetNameLabel.setText("Street name:");
        verticalSeparator.setOrientation(javax.swing.SwingConstants.VERTICAL);
        uplinkLabel.setText("Uplink");
        downlinkLabel.setText("Downlink");
        downlinkDirectionLabel.setText("from to");
        uplinkNumOfLanesLabel.setText("Number of lanes:");
        downlinkNumOfLanesLabel.setText("Number of lanes:");
        uplinkRightTurnLanesLabel.setText("Right turn lanes");
        uplinkLeftTurnLanesLabel.setText("Left turn lanes");
        uplinkLengthLabel.setText("Length:");
        downlinkLengthLabel.setText("Length");
        downlinkRightTurnLanesLabel.setText("Right turn lanes");
        downlinkLeftTurnLanesLabel.setText("Left turn lanes");
        uplinkRightTurnLanesAddButton.setText("Add line");
        uplinkLeftTurnLanesAddButton.setText("Add line");
        downlinkLeftTurnLanesAddButton.setText("Add line");
        downlinkRightTurnLanesAddButton.setText("Add line");

        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(footerSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 431, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(uplinkRightTurnLanesLabel)
                                .addGap(12, 12, 12)
                                .addComponent(uplinkRightTurnLanesAddButton, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(uplinkLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(uplinkDirectionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(uplinkNumOfLanesLabel)
                            .addComponent(uplinkLengthLabel)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(uplinkLengthTextField, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(uplinkNumOfLanesTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(uplinkLeftTurnLanesLabel)
                                .addGap(18, 18, 18)
                                .addComponent(uplinkLeftTurnLanesAddButton, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(saveButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(uplinkRightLanesScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(uplinkLeftLanesScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(verticalSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(downlinkRightTurnLanesLabel)
                                .addGap(12, 12, 12)
                                .addComponent(downlinkRightTurnLanesAddButton, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(downlinkLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(downlinkDirectionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE))
                            .addComponent(downlinkNumOfLanesLabel)
                            .addComponent(downlinkLengthLabel)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(downlinkLengthTextField, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(downlinkNumOfLanesTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(downlinkLeftTurnLanesLabel)
                                .addGap(18, 18, 18)
                                .addComponent(downlinkLeftTurnLanesAddButton, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE))
                            .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(downlinkRightLanesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                            .addComponent(downlinkLeftLanesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)))
                    .addComponent(headerSeparator, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 431, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(streetNameLabel)
                                .addComponent(streetNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)
                                .addComponent(roadIdTextField)
                                .addComponent(speedLimitLabel)
                                .addComponent(speedLimitTextField))
                            .addComponent(roadIdLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(streetFromLinksSeparator, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 431, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(headerSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(roadIdLabel)
                        .addGap(10, 10, 10)
                        .addComponent(roadIdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(streetNameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(streetNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(10, 10, 10)
                            .addComponent(speedLimitLabel)
                        .addComponent(speedLimitTextField))
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(streetFromLinksSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(downlinkLabel)
                            .addComponent(downlinkDirectionLabel))
                        .addGap(18, 18, 18)
                        .addComponent(downlinkNumOfLanesLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(downlinkNumOfLanesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(downlinkLengthLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(downlinkLengthTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(34, 34, 34)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(downlinkRightTurnLanesLabel)
                            .addComponent(downlinkRightTurnLanesAddButton))
                        .addGap(5, 5, 5)
                        .addComponent(downlinkRightLanesScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(downlinkLeftTurnLanesLabel)
                            .addComponent(downlinkLeftTurnLanesAddButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(downlinkLeftLanesScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(uplinkDirectionLabel)
                            .addComponent(uplinkLabel))
                        .addGap(18, 18, 18)
                        .addComponent(uplinkNumOfLanesLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(uplinkNumOfLanesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(uplinkLengthLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(uplinkLengthTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(33, 33, 33)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(uplinkRightTurnLanesLabel)
                            .addComponent(uplinkRightTurnLanesAddButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(uplinkRightLanesScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(uplinkLeftTurnLanesLabel)
                            .addComponent(uplinkLeftTurnLanesAddButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(uplinkLeftLanesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addComponent(verticalSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 423, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addComponent(footerSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(saveButton))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }
    
    /**
     * Inits the by road.
     * 
     * @param road the road
     */
    public void initByRoad(Road road) {
        this.road = road;
        this.roadSpecification = new RoadEditableSpecification(road);
        //TODO combine view with road clone
        //add action listeners for butons as well
        //as for those connected to lanes either those connected to 
        //general actions (save/cancel
        this.setTitle("Road properties dialog");
        initStaticContent(road);
        initLinkContent(uplinkLeftLanesPanel, uplinkLeftLanesScrollPane, uplinkLeftTurnLanesAddButton, roadSpecification.getUplinkLeftLanes());
        initLinkContent(uplinkRightLanesPanel, uplinkRightLanesScrollPane, uplinkRightTurnLanesAddButton, roadSpecification.getUplinkRightLanes());
        initLinkContent(downlinkLeftLanesPanel, downlinkLeftLanesScrollPane, downlinkLeftTurnLanesAddButton, roadSpecification.getDownlinkLeftLanes());
        initLinkContent(downlinkRightLanesPanel, downlinkRightLanesScrollPane, downlinkRightTurnLanesAddButton, roadSpecification.getDownlinkRightLanes());

        pack();
    }
    
    /**
     * Inits the static content.
     * 
     * @param road the road
     */
    private void initStaticContent(Road road) {
        roadIdTextField.setText(road.getId());
        streetNameTextField.setText(road.getStreet());
        speedLimitTextField.setText(road.getSpeedLimit());
        
        uplinkDirectionLabel.setText("from " + road.getUplink().getStartNode().getId()
                + " to " + road.getUplink().getEndNode().getId());
        downlinkDirectionLabel.setText("from " + road.getDownlink().getStartNode().getId()
                + " to " + road.getDownlink().getEndNode().getId());
        uplinkNumOfLanesTextField.setText(new Integer(road.getUplink().getNumberOfLanes()).toString());
        downlinkNumOfLanesTextField.setText(new Integer(road.getDownlink().getNumberOfLanes()).toString());
        uplinkLengthTextField.setText(new Integer(road.getUplink().getLength()).toString());
        downlinkLengthTextField.setText(new Integer(road.getUplink().getLength()).toString());
        
        uplinkLeftTurnLanesAddButton.addActionListener(new AddLaneActionListener(this,
                roadSpecification.getUplinkLeftLanes(),
                "Add uplink left lane"));
        uplinkRightTurnLanesAddButton.addActionListener(new AddLaneActionListener(this,
                roadSpecification.getUplinkRightLanes(),
                "Add uplink right lane"));
        downlinkLeftTurnLanesAddButton.addActionListener(new AddLaneActionListener(this,
                roadSpecification.getDownlinkLeftLanes(),
                "Add downlink left lane"));
        downlinkRightTurnLanesAddButton.addActionListener(new AddLaneActionListener(this,
                roadSpecification.getDownlinkRightLanes(),
                "Add downlink right lane"));
    }    
    
    /**
     * Sets the position.
     * 
     * @param x the x
     * @param y the y
     */
    public void setPosition(int x, int y) {
        Point topLeft = getOwner().getLocationOnScreen();
        Point position = new Point((int)topLeft.getX()+x+110,(int)topLeft.getY()+y+60);
        setLocation(position);
    }

     /**
      * Save button action performed.
      * 
      * @param evt the evt
      */
     private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {
         road.setId(this.roadIdTextField.getText());
         road.setStreet(this.streetNameTextField.getText());
         road.setSpeedLimit(this.speedLimitTextField.getText());
         Link uplink = road.getUplink();
         Link downlink = road.getDownlink();
         uplink.setLength(Integer.parseInt(this.uplinkLengthTextField.getText()));
         uplink.setNumberOfLanes(Integer.parseInt(this.uplinkNumOfLanesTextField.getText()));
         uplink.setLeftLanes(this.roadSpecification.getUplinkLeftLanes());
         uplink.setRightLanes(this.roadSpecification.getUplinkRightLanes());
         
         downlink.setLength(Integer.parseInt(this.downlinkLengthTextField.getText()));
         downlink.setNumberOfLanes(Integer.parseInt(this.downlinkNumOfLanesTextField.getText()));
         downlink.setLeftLanes(this.roadSpecification.getDownlinkLeftLanes());
         downlink.setRightLanes(this.roadSpecification.getDownlinkRightLanes());
         
         if(!mainFrame.getTitle().contains("*")) {
             mainFrame.setTitle(mainFrame.getTitle() + "*");
         }
         
         
         Node startNode = uplink.getStartNode();
         if (startNode instanceof Intersection) {
             ((Intersection)startNode).updateTrafficLights();
         }
         Node endNode = uplink.getEndNode();
         if (endNode instanceof Intersection) {
             ((Intersection)endNode).updateTrafficLights();
         }
         dispose();
         
     }

     /**
      * Cancel button action performed.
      * 
      * @param evt the evt
      */
     private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
         this.dispose();
     }

    /**
     * Gets the road.
     * 
     * @return the road
     */
    public Road getRoad() {
        return road;
    }
    
    /**
     * Inits the link content.
     * 
     * @param panel the panel
     * @param scrollPane the scroll pane
     * @param addButton the add button
     * @param sideLanes the side lanes
     */
    private void initLinkContent(JPanel panel, JScrollPane scrollPane, JButton addButton, List<Integer>sideLanes ) {
        Box box = Box.createHorizontalBox();
        JLabel lengthLabel = new JLabel("Length");
        JLabel deleteLabel = new JLabel("Delete");
        
        panel.removeAll();
        scrollPane.getViewport().removeAll();
        
        //Uplink Right Lanes Panel
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        lengthLabel.setPreferredSize(new Dimension(80, 25));
        lengthLabel.setMaximumSize(new Dimension(80, 25));
        lengthLabel.setMinimumSize(new Dimension(80, 25));
        
        deleteLabel.setPreferredSize(new Dimension(100, 25));
        deleteLabel.setMaximumSize(new Dimension(100, 25));
        deleteLabel.setMinimumSize(new Dimension(100, 25));
        
        box.add(lengthLabel);
        box.add(Box.createHorizontalStrut(25));
        box.add(deleteLabel);
        
        box.setPreferredSize(new Dimension(scrollPane.getViewport().getWidth(), 25));
        box.setMaximumSize(new Dimension(scrollPane.getViewport().getWidth(), 25));
        
        panel.add(box);

        for(Integer length : sideLanes) {
            box = Box.createHorizontalBox();
            box.setAlignmentX(Box.CENTER_ALIGNMENT);
            
            JLabel lineLengthLabel = new JLabel();
            lineLengthLabel.setPreferredSize(new Dimension(80, 25));
            lineLengthLabel.setMaximumSize(new Dimension(80, 25));
            lineLengthLabel.setMinimumSize(new Dimension(80, 25));
            lineLengthLabel.setText(new Integer(length).toString());
            
            JButton jButton = new JButton("Usuń");
            jButton.setPreferredSize(new Dimension(100, 25));
            jButton.setMaximumSize(new Dimension(100, 25));
            jButton.setMinimumSize(new Dimension(100, 25));
            jButton.addActionListener(new DeleteLaneActionListener(sideLanes,length));

            box.add(lineLengthLabel);
            box.add(Box.createHorizontalStrut(25));
            box.add(jButton);
            
            box.setPreferredSize(new Dimension(scrollPane.getViewport().getWidth(), 20));
            box.setMaximumSize(new Dimension(scrollPane.getViewport().getWidth(), 20));
            panel.add(box);
        }
        
        scrollPane.getViewport().add(panel);
    }
    
    /**
     * Refresh dynamic content.
     */
    public void refreshDynamicContent(){
        initLinkContent(uplinkLeftLanesPanel, uplinkLeftLanesScrollPane, uplinkLeftTurnLanesAddButton, roadSpecification.getUplinkLeftLanes());
        initLinkContent(uplinkRightLanesPanel, uplinkRightLanesScrollPane, uplinkRightTurnLanesAddButton, roadSpecification.getUplinkRightLanes());
        initLinkContent(downlinkLeftLanesPanel, downlinkLeftLanesScrollPane, downlinkLeftTurnLanesAddButton, roadSpecification.getDownlinkLeftLanes());
        initLinkContent(downlinkRightLanesPanel, downlinkRightLanesScrollPane, downlinkRightTurnLanesAddButton, roadSpecification.getDownlinkRightLanes());
    }
    
}

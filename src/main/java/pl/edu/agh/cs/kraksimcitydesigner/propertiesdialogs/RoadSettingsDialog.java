package pl.edu.agh.cs.kraksimcitydesigner.propertiesdialogs;

import pl.edu.agh.cs.kraksimcitydesigner.MainFrame;
import pl.edu.agh.cs.kraksimcitydesigner.element.RoadsSettings;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RoadSettingsDialog extends JDialog {

    private final MainFrame mainFrame;
    private int defaultSpeedLimit;
    private RoadsSettings roadsSettings;

    public RoadSettingsDialog(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.roadsSettings = mainFrame.getRoadsSettings();
        initComponents();

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                defaultSpeedLimit = Integer.parseInt(defaultSpeedLimitField.getText());
                setDefaultSpeedLimit(defaultSpeedLimit);
                setVisible(false);
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
    }

    public int getDefaultSpeedLimit() {
        return defaultSpeedLimit;
    }

    public void setDefaultSpeedLimit(int defaultSpeedLimit) {
        this.defaultSpeedLimitField.setText("" + defaultSpeedLimit);
        this.defaultSpeedLimit = defaultSpeedLimit;
        roadsSettings.setDefaultSpeedLimit(defaultSpeedLimit);
    }

    public void refresh() {
        RoadsSettings roadsSettings = mainFrame.getRoadsSettings();
        setDefaultSpeedLimit(roadsSettings.getDefaultSpeedLimit());
    }

    private void initComponents() {
        defaultSpeedLimitLabel = new javax.swing.JLabel();
        defaultSpeedLimitField = new javax.swing.JTextField();

        saveButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        defaultSpeedLimitLabel.setText("Default speed limit");
        defaultSpeedLimitLabel.setName("defaultSpeedLimitLabel");

        saveButton.setText("Save"); // NOI18N
        saveButton.setName("saveButton"); // NOI18N

        cancelButton.setText("Cancel"); // NOI18N
        cancelButton.setName("cancelButton"); // NOI18N

        this.setTitle("Road settings");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(defaultSpeedLimitLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(defaultSpeedLimitField, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                ))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                                )
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[]{saveButton, cancelButton});

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(defaultSpeedLimitLabel)
                                        .addComponent(defaultSpeedLimitField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGap(7, 7, 7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(saveButton)
                                        .addComponent(cancelButton))
                                .addContainerGap())
        );

        pack();

    }

    private javax.swing.JLabel defaultSpeedLimitLabel;
    private javax.swing.JTextField defaultSpeedLimitField;
    private javax.swing.JButton saveButton;
    private javax.swing.JButton cancelButton;


}

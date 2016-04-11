package pl.edu.agh.cs.kraksimcitydesigner;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.apache.log4j.Logger;

import pl.edu.agh.cs.kraksimcitydesigner.tools.MapTool;
import pl.edu.agh.cs.kraksimcitydesigner.tools.MoveTool;

// TODO: Auto-generated Javadoc
/**
 * Panel placed on the left side containing buttons.
 * @author kkot
 */
public class ControlPanel extends JPanel {
    private static Logger log = Logger.getLogger(ControlPanel.class);

	private static final long serialVersionUID = -5240325579792915030L;
	private final MainFrame mainFrame;
	private EditorPanel editorPanel;
	
	/**
	 * Instantiates a new control panel.
	 * 
	 * @param mf the mf
	 */
	public ControlPanel (MainFrame mf) {
		this.mainFrame = mf;
		
//		Container verticalBox = Box.createVerticalBox();
		
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);

		final JToggleButton selectBtn = new JToggleButton("Selecting");
		selectBtn.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(selectBtn.isSelected()){
					mainFrame.getEditorPanel().setMode(EditorPanel.EditorMode.SELECTING);
					log.trace("setMode(SElECTING)");
				}
			}
		});
		
		final JToggleButton deleteBtn = new JToggleButton("Deleting");
		deleteBtn.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(deleteBtn.isSelected()){
					mainFrame.getEditorPanel().setMode(EditorPanel.EditorMode.DELETING);
					log.trace("setMode(DELETING)");
				}
			}
		});
		
		final JToggleButton intersectionBtn = new JToggleButton("Intersection");
		intersectionBtn.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(intersectionBtn.isSelected()){
					mainFrame.getEditorPanel().setMode(EditorPanel.EditorMode.INSERTING_INTERSECTION);
					log.trace("setMode(INSERTING_INTERSECTION)");
				}
			}
		});
		
		final JToggleButton gatewayBtn = new JToggleButton("Gateway");
		gatewayBtn.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(gatewayBtn.isSelected()){
					mainFrame.getEditorPanel().setMode(EditorPanel.EditorMode.INSERTING_GATEWAY);
					log.trace("setMode(INSERTING_GATEWAY)");
				}
			}
		});
		
		final JToggleButton roadBtn = new JToggleButton("Road");
		roadBtn.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(roadBtn.isSelected()){
					mainFrame.getEditorPanel().setMode(EditorPanel.EditorMode.INSERTING_ROAD_STARTPOINT);
					log.trace("setMode(INSERTING_ROAD_STARTPOINT)");
				}
			}
		});
		
		final JToggleButton movingBtn = new JToggleButton("Moving");
        movingBtn.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(movingBtn.isSelected()){
					mainFrame.getEditorPanel().setMode(EditorPanel.EditorMode.MOVING);
					log.trace("setMode(MOVING)");
				}
			}
		});
        
        ButtonGroup controlPanelButtonGroup = new ButtonGroup();
        controlPanelButtonGroup.add(selectBtn);
        controlPanelButtonGroup.add(deleteBtn);
        controlPanelButtonGroup.add(intersectionBtn);
        controlPanelButtonGroup.add(gatewayBtn);
        controlPanelButtonGroup.add(roadBtn);
        controlPanelButtonGroup.add(movingBtn);
        
		JToggleButton scrollingBtn = new JToggleButton("Scroll");

		layout.setHorizontalGroup(
		      layout.createParallelGroup()
              .addComponent(selectBtn, 120, 120, 120)
              .addComponent(deleteBtn, 120, 120, 120)
              .addComponent(intersectionBtn, 120, 120, 120)
              .addComponent(gatewayBtn, 120, 120, 120)
              .addComponent(roadBtn, 120, 120, 120)
              .addComponent(movingBtn, 120, 120, 120)
        );
		layout.setVerticalGroup(
		      layout.createSequentialGroup()
              .addComponent(selectBtn, 30, 30, 30)
              .addComponent(deleteBtn, 30, 30, 30)
              .addComponent(intersectionBtn, 30, 30, 30)
              .addComponent(gatewayBtn, 30, 30, 30)
              .addComponent(roadBtn, 30, 30, 30)
              .addComponent(movingBtn, 30, 30, 30)
        );
//		verticalBox.add(selectBtn);
//		//verticalBox.add(deleteBtn);
//		verticalBox.add(intersectionBtn);
//		verticalBox.add(gatewayBtn);
//		verticalBox.add(roadBtn);
//		verticalBox.add(movingBtn);
//		verticalBox.add(scrollingBtn);
//		this.add(verticalBox);
	}
	
	/**
	 * Sets the editor panel.
	 * 
	 * @param ep the new editor panel
	 */
	public void setEditorPanel(EditorPanel ep) {
		this.editorPanel = ep;
		MapTool ml = new MoveTool(editorPanel);
		editorPanel.addMouseListener(ml);
		editorPanel.addMouseMotionListener(ml);
	}
}

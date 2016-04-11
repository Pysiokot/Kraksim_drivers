package pl.edu.agh.cs.kraksimcitydesigner;

import javax.swing.*; 

import org.apache.log4j.Logger;

// TODO: Auto-generated Javadoc
public class InfoPanel extends JPanel {
	
	private static final long serialVersionUID = 6383003410026329015L;
	private static Logger log = Logger.getLogger(InfoPanel.class);
	
	private JLabel modeLabel = new JLabel("MODE");
	private JLabel nameLabel = new JLabel("Object name");	
	
	/**
	 * Instantiates a new info panel.
	 */
	public InfoPanel() {
		this.add(modeLabel);
		this.add(nameLabel);
	}

	/**
	 * Sets the mode info.
	 * 
	 * @param text the new mode info
	 */
	public void setModeInfo(String text) {
		modeLabel.setText(text);
	}

	/**
	 * Sets the name info.
	 * 
	 * @param name the new name info
	 */
	public void setNameInfo(String name) {
		nameLabel.setText(name);
	}
	
}

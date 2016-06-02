package pl.edu.agh.cs.kraksimcitydesigner;

import pl.edu.agh.cs.kraksim.main.gui.SetUpPanel;

// TODO: Auto-generated Javadoc
public class AppRunner {
	
	/**
	 * Creates the and show gui.
	 */
	private static MainFrame mf = null;

	public synchronized static void createAndShowGUI(String mapFile) {
		createAndShowGUI(mapFile, null);
	}

	public synchronized static void createAndShowGUI(String mapFile, SetUpPanel setUpPanel) {
		if (mf == null) {
			mf = new MainFrame(setUpPanel);
		}
		mf.changeFile(mapFile);
		mf.setVisible(true);

	}

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
	        
	        createAndShowGUI("./trafficConfigs/krakow_duzy.xml");
        }});
    }

}

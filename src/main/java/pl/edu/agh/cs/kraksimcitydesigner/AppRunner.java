package pl.edu.agh.cs.kraksimcitydesigner;

// TODO: Auto-generated Javadoc
public class AppRunner {
	
	/**
	 * Creates the and show gui.
	 */
	private static MainFrame mf = null;

	public synchronized static void createAndShowGUI() {
		if (mf == null) {
			mf = new MainFrame();
		}
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
	        
	        createAndShowGUI();
        }});
    }

}

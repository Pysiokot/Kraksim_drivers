package pl.edu.agh.cs.kraksimcitydesigner;

// TODO: Auto-generated Javadoc
public class AppRunner {
	
	/**
	 * Creates the and show gui.
	 */
	public static void createAndShowGUI() {
		MainFrame mf = new MainFrame();
        
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

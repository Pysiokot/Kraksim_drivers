package pl.edu.agh.cs.kraksimcitydesigner;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.event.MouseInputAdapter;
import pl.edu.agh.cs.kraksimcitydesigner.BgImageData;

import org.apache.log4j.Logger;

// TODO: Auto-generated Javadoc
public class Map extends JPanel {
	// TODO serVer
	static final long serialVersionUID = 1;

	private final Logger log = Logger.getLogger(this.getClass().getName());

	protected static Insets zeroInsets = new Insets(1, 1, 1, 1);

	protected JFrame frame = null;

	protected EditorPanel editorPanel = null;

	protected BgImageData bgImageData = BgImageData.getDefault();

	protected File graphDataFile;

	protected JToolBar toolbar;

	protected JPanel infoPanel;

	protected java.text.DecimalFormat decF = new java.text.DecimalFormat();

	private JLabel coordX, coordY, dist;

	protected Dimension toolSep;

	protected ButtonGroup group;

	/**
	 * Instantiates a new map.
	 * 
	 * @param frame the frame
	 */
	public Map(JFrame frame) {
		log.debug("Konstruktor");
		this.frame = frame;
		this.initialize();
		// this.makeGUI();
	}

	/**
	 * Initialize.
	 */
	protected void initialize() {
		group = new ButtonGroup();
		toolbar = new JToolBar();
		toolbar.setOrientation(JToolBar.VERTICAL);
		toolSep = new Dimension(0, 3);
	}

	/**
	 * Gets the editor panel.
	 * 
	 * @return the editor panel
	 */
	public EditorPanel getEditorPanel() {
		return editorPanel;
	}

	/**
	 * Sets the editor panel.
	 * 
	 * @param editorPanel the new editor panel
	 */
	public void setEditorPanel(EditorPanel editorPanel) {
		this.editorPanel = editorPanel;
	}

}

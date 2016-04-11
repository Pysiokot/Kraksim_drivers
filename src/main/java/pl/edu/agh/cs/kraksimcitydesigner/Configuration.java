package pl.edu.agh.cs.kraksimcitydesigner;

// TODO: Auto-generated Javadoc
public class Configuration {
	
	private final int DEFAULT_NODEWIDTH  = 20;
	private final int DEFAULT_NODEHEIGHT = 20;
	
	private int nodeWidth = DEFAULT_NODEHEIGHT;
	private int nodeHeight = DEFAULT_NODEWIDTH;
	
	/**
	 * Instantiates a new configuration.
	 */
	public Configuration() {
		
	}

	/**
	 * Gets the node width.
	 * 
	 * @return the node width
	 */
	public int getNodeWidth() {
		return nodeWidth;
	}

	/**
	 * Sets the node width.
	 * 
	 * @param nodeWidth the new node width
	 */
	public void setNodeWidth(int nodeWidth) {
		this.nodeWidth = nodeWidth;
	}

	/**
	 * Gets the node height.
	 * 
	 * @return the node height
	 */
	public int getNodeHeight() {
		return nodeHeight;
	}

	/**
	 * Sets the node height.
	 * 
	 * @param nodeHeight the new node height
	 */
	public void setNodeHeight(int nodeHeight) {
		this.nodeHeight = nodeHeight;
	}
}

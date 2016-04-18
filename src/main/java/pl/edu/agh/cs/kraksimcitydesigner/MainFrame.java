package pl.edu.agh.cs.kraksimcitydesigner;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import pl.edu.agh.cs.kraksim.main.gui.SetUpPanel;
import pl.edu.agh.cs.kraksimcitydesigner.element.DisplaySettings;
import pl.edu.agh.cs.kraksimcitydesigner.element.ElementManager;
import pl.edu.agh.cs.kraksimcitydesigner.parser.ModelParser;
import pl.edu.agh.cs.kraksimcitydesigner.parser.ParsingException;
import pl.edu.agh.cs.kraksimcitydesigner.propertiesdialogs.AboutConfigDialog;
import pl.edu.agh.cs.kraksimcitydesigner.propertiesdialogs.SettingsDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainFrame extends JFrame {
	private static final long serialVersionUID = 1957030091157298387L;
	private static Logger log = Logger.getLogger(MainFrame.class);
	
	private final String FRAME_TITLE = "KraksimCityDesigner";
	
	private DisplaySettings displaySettings;
	private ElementManager elementManager;
	private ControlPanel controlPanel;
	private EditorPanel editorPanel;
	private InfoPanel infoPanel;
	private Configuration configuration;
    private boolean projectChanged;
    private final JFileChooser fc = new JFileChooser();
    private final SettingsDialog settingsDialog;
	private SetUpPanel setUpPanel;
    
    private File loadedFile = null;
	
	/**
	 * Instantiates a new main frame.
	 */
	public MainFrame(SetUpPanel setUpPanel) {
	    this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	    this.setTitle(FRAME_TITLE+"- Nowy projekt");
		this.setSetUpPanel(setUpPanel);
	    
	    displaySettings = new DisplaySettings();
	    
	    configuration = new Configuration();
	    elementManager = new ElementManager(displaySettings);
	    controlPanel = new ControlPanel(this);
	    JScrollPane scrollPane = new JScrollPane();
	    editorPanel = new EditorPanel(this, scrollPane,displaySettings);
	    infoPanel = new InfoPanel();
	    settingsDialog = new SettingsDialog(this,fc,true);
	    
	    editorPanel.setDoubleBuffered(true);
	    editorPanel.setPreferredSize(new Dimension(BgImageData.getDefault().getWidth(), BgImageData.getDefault().getHeight()));
	    editorPanel.setPreferredSize(new Dimension(600,400));
	    scrollPane.getViewport().add(this.editorPanel);
	   
	    controlPanel.setEditorPanel(editorPanel);
	        
	    this.setLayout(new BorderLayout());
		this.add(scrollPane, BorderLayout.CENTER);
		this.add(infoPanel, BorderLayout.SOUTH);
		this.add(controlPanel, BorderLayout.WEST);
		
		// FileChooser
		fc.setCurrentDirectory(new File("."));

		// Adding menu bar
		JMenuBar menuBar = new JMenuBar();
		JMenu menuFile = createMenuFile();
		JMenu menuProject = createMenuProject();
		JMenu menuRoads = createMenuRoads();
		JMenu menuIntersections = createMenuIntersections();
		
		menuBar.add(menuFile);
		menuBar.add(menuProject);
		menuBar.add(menuRoads);
		menuBar.add(menuIntersections);
			
		this.setJMenuBar(menuBar);
	    // Display the window.
	    this.pack();
	    
//	    loadProjectFromFile(new File("./trafficConfigs/krakow_duzy.xml"));
	       
	    //this.setExtendedState(getExtendedState() | MAXIMIZED_BOTH);
	    this.setVisible(true);
	}

	public MainFrame() {
		this(null);
	}

	public void changeFile(String fileName) {
		loadProjectFromFile(new File(fileName));
		editorPanel.repaint();
	}

	private JMenu createMenuRoads() {
        
	    JMenu menuRoads = new JMenu("Roads");
        JMenuItem menuItem;

        // NEW Menu Item
        menuItem = new JMenuItem("Recalculate distances");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int returnVal = JOptionPane.showConfirmDialog(MainFrame.this, "All lengths of links will be set to Euclidean distance of Nodes. Do this ?" , "Confirmation", JOptionPane.YES_NO_OPTION);
                if (returnVal == JOptionPane.YES_OPTION) {
                    elementManager.recalculateDistancesOfLinks();
                    setProjectChanged(true);
                }
            }
        });
        
        menuRoads.add(menuItem);
        return menuRoads;
    }
	
	   private JMenu createMenuProject() {
	        
	        JMenu menu = new JMenu("Project");
	        JMenuItem menuItem;

	        // NEW Menu Item
	        menuItem = new JMenuItem("Settings");
	        menuItem.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	                settingsDialog.refresh();
	                settingsDialog.setVisible(true);
	            }
	        });
	        menu.add(menuItem);
	        
	        // NEW Menu Item
	        menuItem = new JMenuItem("About");
	        menuItem.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	                new AboutConfigDialog(MainFrame.this,elementManager);
	            }
	        });
	        
	        menu.add(menuItem);
	        return menu;
	    }
	
	private JMenu createMenuIntersections() {
	    
	    JMenu menuRoads = new JMenu("Intersections");
	    JMenuItem menuItem;
	    
	    // NEW Menu Item
	    menuItem = new JMenuItem("Create default actions for 2Way simple intersections (1 phase traffic light)");
	    menuItem.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	            int returnVal = JOptionPane.showConfirmDialog(MainFrame.this, "For intersections that has only two incoming links and have not actions yet defaults actions and traffic lights schedule will be created. Continue?" , "Confirmation", JOptionPane.YES_NO_OPTION);
	            if (returnVal == JOptionPane.YES_OPTION) {
	                elementManager.createDefaultActionsAndTrafficSchedules();
	                setProjectChanged(true);
	            }
	        }
	    });
	    menuRoads.add(menuItem);
	    
	    // NEW Menu Item
	    menuItem = new JMenuItem("Create default actions for 3Way simple intersections (1 phase traffic light)");
	    menuItem.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	            int returnVal = JOptionPane.showConfirmDialog(MainFrame.this, "For intersections that has only 3 roads and have not actions defined yet, defaults actions and traffic lights schedule will be created. Continue?" , "Confirmation", JOptionPane.YES_NO_OPTION);
	            if (returnVal == JOptionPane.YES_OPTION) {
	                int numOfChanged = elementManager.createDefaultActions3WaySimpleForIntersections();
	                JOptionPane.showMessageDialog(MainFrame.this, "Was changed "+numOfChanged+" intersections");
	                if (numOfChanged > 0) {
	                    setProjectChanged(true);
	                }
	            }
	        }
	    });
	    menuRoads.add(menuItem);
	    
	    // NEW Menu Item
	    menuItem = new JMenuItem("Create default actions for 4Way simple intersections (1 phase traffic light)");
	    menuItem.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	            int returnVal = JOptionPane.showConfirmDialog(MainFrame.this, "For intersections that has only 4 roads and have not actions defined yet, defaults actions and traffic lights schedule will be created. Continue?" , "Confirmation", JOptionPane.YES_NO_OPTION);
	            if (returnVal == JOptionPane.YES_OPTION) {
                    int numOfChanged = elementManager.createDefaultActions4WaySimpleForIntersections();
                    JOptionPane.showMessageDialog(MainFrame.this, "Was changed "+numOfChanged+" intersections");
                    if (numOfChanged > 0) {
	                    setProjectChanged(true);
	                }
	            }
	        }
	    });
	    menuRoads.add(menuItem);
	    
	    return menuRoads;
	}

    public JMenu createMenuFile() {

	    JMenu menuFile = new JMenu("File");
	    JMenuItem menuItem;

	    // NEW Menu Item
	    menuItem = new JMenuItem("New");
	    menuItem.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	            newProject();
	        }
	    });
	    menuFile.add(menuItem);

	    // OPEN Menu Item
	    menuItem = new JMenuItem("Open ...");
	    menuItem.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	            int returnVal = fc.showOpenDialog(MainFrame.this);
	            if (returnVal == JFileChooser.APPROVE_OPTION) {
	                File file = fc.getSelectedFile();
	                loadProjectFromFile(file);
	                editorPanel.repaint();
	                System.out.println("Opening: " + file.getName());
	            } else {
	                System.out.println("Open command cancelled by user.");
	            }
	        }
	    });
	    menuFile.add(menuItem);

	    // SAVE Menu Item
	    menuItem = new JMenuItem("Save");
	    menuItem.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	            saveProject();
	        }
	    });
	    menuFile.add(menuItem);

	    // SAVE AS Menu Item
	    menuItem = new JMenuItem("Save as ...");
	    menuItem.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	            saveProjectAs();
	        }
	    });
	    menuFile.add(menuItem);

	    return menuFile;
	}
	
	/**
	 * Save project.
	 */
	private void saveProject() {
        if (loadedFile != null) {
            saveProjectToFile(loadedFile);
            setProjectChanged(false);
        } else {
            saveProjectAs();
        }
    }

    /**
     * New project.
     */
    private void newProject() {
	    if (getProjectChanged()) {
	        int returnVal = JOptionPane.showConfirmDialog(this, "Project hasn't been saved. Do you want to save it now ? If you choose no, data may be lost.", "Project not saved" , JOptionPane.YES_NO_CANCEL_OPTION);
	        if (returnVal == JOptionPane.YES_OPTION) {
	            saveProject();
	            loadedFile = null;
	            elementManager.clear();
	        }
	        else if (returnVal == JOptionPane.NO_OPTION) {
	            clearProject();
	        }
	    }
	    else {
	        clearProject();
	    }
    }
    
    /**
     * Clear project.
     */
    private void clearProject() {
        loadedFile = null;
        elementManager.clear();
        editorPanel.repaint();
        setTitle(FRAME_TITLE+"- Nowy projekt");
    }

    /**
     * Save project as.
     */
    private void saveProjectAs() {
        int returnVal = fc.showSaveDialog(MainFrame.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            loadedFile = file;
            saveProjectToFile(file);
            setProjectChanged(false);
            System.out.println("Saving: " + file.getName());
            setTitle(FRAME_TITLE+"- "+file.getName());
        } else {
            System.out.println("Saving command cancelled by user.");
        }
    }

    /**
     * Save project to file.
     * 
     * @param file the file
     */
    private void saveProjectToFile(File file) {
	    
	    Document doc = elementManager.modelToDocument();
	    
	    // properties
	    Document propDoc = new Document();
	    fillPropertiesDOM(propDoc,displaySettings);
	    
	    XMLOutputter outp = new XMLOutputter();
	    outp.setFormat(Format.getPrettyFormat());

	    try {
	        FileWriter fw = new FileWriter(file);
	        //outp.output(doc, System.out);
	        outp.output(doc,fw);
	    }
	    catch (IOException e) {
	        JOptionPane.showMessageDialog(this, "Can't save project to file", "ERROR", JOptionPane.ERROR_MESSAGE);
	    }
	    try {
	        File propertiesFile = new File(file.getPath()+".properties");
	        FileWriter fw = new FileWriter(propertiesFile);
	        outp.output(propDoc, fw);
	    }
	    catch (IOException e) {
	        JOptionPane.showMessageDialog(this, "Can't save properties to file", "ERROR", JOptionPane.ERROR_MESSAGE);
	    }
    }
      
    private static void fillPropertiesDOM(Document document, DisplaySettings ds) {
        
        Element root = new Element("Project");
        document.addContent(root);
        
        Element cellsPerPixelElement = new Element("cellsPerPixel");
        cellsPerPixelElement.setAttribute("value", ""+ds.getCellsPerPixel());
        root.addContent(cellsPerPixelElement);
    }

    /**
     * Load project from file.
     * 
     * @param file the file
     */
    private void loadProjectFromFile(File file) {
        try {

            File propertiesFile = new File(file.getPath()+".properties");
            if (propertiesFile.canRead()) {
                
                DisplaySettings ds = readDisplaySettingFromFile(propertiesFile);
                if (ds != null) {
                    this.displaySettings = ds;
                } else {
                    this.displaySettings = new DisplaySettings();
                }
            }
            elementManager.clear();
            elementManager.setDisplaySettings(this.displaySettings);
            
            ModelParser.parse(elementManager, file);
            loadedFile = file;

            setProjectChanged(false);
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error while reading map file", "Error", JOptionPane.ERROR_MESSAGE);
            //e.printStackTrace();
        }
        catch (ParsingException e) {
            JOptionPane.showMessageDialog(this, "Error while parsing map file", "Error", JOptionPane.ERROR_MESSAGE);
            //e.printStackTrace();         
        }
	}

    private DisplaySettings readDisplaySettingFromFile(File propertiesFile) {

        DisplaySettings result = new DisplaySettings();
        
        SAXBuilder builder = new SAXBuilder();
        builder.setIgnoringElementContentWhitespace(true);
        try {
            Document doc = builder.build(propertiesFile);
            Element root = doc.getRootElement();

            Element cellsPerPixelElement = root.getChild("cellsPerPixel");
            
            try {
                double cellsPerPixel = Double.parseDouble(cellsPerPixelElement.getAttributeValue("value"));
                result.setCellsPerPixel(cellsPerPixel);
            }
            catch (Exception e) {
                // nothing
            }
        }
        catch (IOException e) {
            System.err.println("Error while reading file");
            return null;	          
        }
        catch (JDOMException e) {
            System.err.println("Error while parsing config file");
            return null;
        }
        System.out.println("Reading project settings from "+propertiesFile.getPath());
        return result;
    }

    /**
	 * Gets the element manager.
	 * 
	 * @return the element manager
	 */
	public ElementManager getElementManager() {
		return elementManager;
	}

	/**
	 * Gets the control panel.
	 * 
	 * @return the control panel
	 */
	public ControlPanel getControlPanel() {
		return controlPanel;
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
	 * Gets the configuration.
	 * 
	 * @return the configuration
	 */
	public Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * Gets the info panel.
	 * 
	 * @return the info panel
	 */
	public InfoPanel getInfoPanel() {
		return infoPanel;
	}

    /**
     * Sets the project changed.
     * 
     * @param projectChanged the new project changed
     */
    public void setProjectChanged(boolean projectChanged) {
        String asterisk = projectChanged ? "*" : "";
        String projectName = (loadedFile == null) ? "Nowy projekt" : loadedFile.getName();
        this.setTitle(FRAME_TITLE + " - " + projectName +  asterisk);
        this.projectChanged = projectChanged;
    }
    
    /**
     * Gets the project changed.
     * 
     * @return the project changed
     */
    public boolean getProjectChanged() {
        return this.projectChanged;
    }
    
    public DisplaySettings getDisplaySettings() {
        return displaySettings;
    }

	public SetUpPanel getSetUpPanel() {
		return setUpPanel;
	}

	public void setSetUpPanel(SetUpPanel setUpPanel) {
		this.setUpPanel = setUpPanel;
	}

	public void refresh() {
        repaint();
    }

	public void updateFilePath() {
		if (loadedFile != null) {
			setUpPanel.setMapLocationPath(loadedFile.getPath());
		}
	}
}

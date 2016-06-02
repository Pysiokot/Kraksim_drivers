package pl.edu.agh.cs.kraksimcitydesigner;

import java.io.File;
import java.io.Serializable;

import org.apache.log4j.Logger;

// TODO: Auto-generated Javadoc
//TODO writer reader...
public class BgImageData implements Comparable, Cloneable, Serializable {
    static final long serialVersionUID = 872672234666L;
    private static transient final Logger log = Logger.getLogger(BgImageData.class);
    private static  final File dir = new File("maps");
    private static  final String defName = "krakowbig.gif";
    private static Integer defW = 1685;
    private static Integer defH = 2381;
    private static Integer defX = -2280;
    private static Integer defY = 442;
    private String name = "";
    private String fileName;
    private Integer width;
    private Integer height;
    private Integer xCoord;
    private Integer yCoord;
    
    /**
     * Instantiates a new bg image data.
     */
    public BgImageData() {
    }
    
    /**
     * Instantiates a new bg image data.
     * 
     * @param name the name
     */
    public BgImageData(String name) {
        this.name = name;
        log.debug("Bacgroud Image data created...");
    }
 
 /**
  * Gets the default.
  * 
  * @return the default
  */
 public static BgImageData getDefault(){
        BgImageData bg = new BgImageData("default");
        String name = dir.toString()+File.separator+defName;
        bg.setFileName(name);
        log.debug("Filename: " + name);
        bg.setWidth(defW);
        bg.setHeight(defH);
        bg.setXCoord(defX);
        bg.setYCoord(defY);
        return bg;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return name;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        Object o = null;
        try {
            o = super.clone();
        } catch (CloneNotSupportedException cnse) {
            log.debug(cnse.getMessage());
        }
        return o;
    }
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(T)
     */
    public int compareTo(Object o) {
        return name.compareTo(((BgImageData) o).name);
    }
    
    /**
     * Gets the file name.
     * 
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }
    
    /**
     * Sets the file name.
     * 
     * @param fileName the new file name
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    /**
     * Gets the height.
     * 
     * @return the height
     */
    public Integer getHeight() {
        return height;
    }
    
    /**
     * Sets the height.
     * 
     * @param height the new height
     */
    public void setHeight(Integer height) {
        this.height = height;
    }
    
    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the name.
     * 
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the width.
     * 
     * @return the width
     */
    public Integer getWidth() {
        return width;
    }
    
    /**
     * Sets the width.
     * 
     * @param width the new width
     */
    public void setWidth(Integer width) {
        this.width = width;
    }
    
    /**
     * Gets the x coord.
     * 
     * @return the x coord
     */
    public Integer getXCoord() {
        return xCoord;
    }
    
    /**
     * Sets the x coord.
     * 
     * @param coord the new x coord
     */
    public void setXCoord(Integer coord) {
        xCoord = coord;
    }
    
    /**
     * Gets the y coord.
     * 
     * @return the y coord
     */
    public Integer getYCoord() {
        return yCoord;
    }
    
    /**
     * Sets the y coord.
     * 
     * @param coord the new y coord
     */
    public void setYCoord(Integer coord) {
        yCoord = coord;
    }

}

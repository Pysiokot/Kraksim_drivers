package pl.edu.agh.cs.kraksimcitydesigner;


import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

// TODO: Auto-generated Javadoc
/**
 * 
 * @author anna.bizon
 */
public class SwingUtils {
    private static int defSize=24;
    private static String defExt="gif";
    private static char theDot='.';
    
    /**
     * Gets the maximum window bounds.
     * 
     * @return the maximum window bounds
     */
    public static Dimension getMaximumWindowBounds() {
        String vers = System.getProperty("java.version");
        if (vers.compareTo("1.4")>= 0)
            return new Dimension(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getSize());
        else 
            return new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
    }
    
    /**
     * Make image icon.
     * 
     * @param key the key
     * @param size the size
     * @param imExt the im ext
     * 
     * @return the image icon
     */
    
    public static ImageIcon makeImageIcon(String key,int size,String imExt){
        StringBuffer sb=new StringBuffer();
        sb.append(key);
        sb.append(Integer.toString(size));
        sb.append(theDot);
        sb.append(imExt);
        return new ImageIcon(sb.toString());
    }
    
    /**
     * Make image icon.
     * 
     * @param name the name
     * 
     * @return the image icon
     */
    public static ImageIcon makeImageIcon(String name){
    	return new ImageIcon(name);
    }
    
    
    /**
     * Gets the image.
     * 
     * @param file the file
     * 
     * @return the image
     */
    
    /*public static ImageIcon makeImageIcon(String key){
        return makeImageIcon(key,defSize,defExt);
    }*/
    
    public static Image getImage(String file){
        return Toolkit.getDefaultToolkit().getImage(file);
    }
    
    /**
     * Show error message.
     * 
     * @param c the c
     * @param m the m
     * @param title the title
     */
    public static void showErrorMessage(Component c, String m, String title){
        JOptionPane.showMessageDialog(c, m, title, JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Checks if is empty.
     * 
     * @param s the s
     * 
     * @return true, if is empty
     */
    public static boolean isEmpty(String s){
        return (s==null || s=="");
    }
    
   
    
   
    
   
}

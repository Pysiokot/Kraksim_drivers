package pl.edu.agh.cs.kraksimcitydesigner.element;

import java.awt.Color;
import java.io.File;

public class DisplaySettings {
    
    private double cellsPerPixel = 1.0;
    private int nodeWidth = 20;
    private int nodeHeight = 20;
    private Color intersectionColor = Color.DARK_GRAY;
    private Color notConfiguredintersectionColor = Color.RED;
    private Color linkColor = Color.CYAN;
    private Color gatewayColor = Color.BLACK;
    private File backgroundImage = new File("maps/krakowbig.gif");
    
    public double getCellsPerPixel() {
        return cellsPerPixel;
    }
    public void setCellsPerPixel(double cellsPerPixel) {
        this.cellsPerPixel = cellsPerPixel;
    }
    public int getNodeWidth() {
        return nodeWidth;
    }
    public void setNodeWidth(int nodeWidth) {
        this.nodeWidth = nodeWidth;
    }
    public int getNodeHeight() {
        return nodeHeight;
    }
    public void setNodeHeight(int nodeHeight) {
        this.nodeHeight = nodeHeight;
    }
    public Color getIntersectionColor() {
        return intersectionColor;
    }
    public void setIntersectionColor(Color intersectionColor) {
        this.intersectionColor = intersectionColor;
    }
    public Color getLinkColor() {
        return linkColor;
    }
    public void setLinkColor(Color linkColor) {
        this.linkColor = linkColor;
    }
    public void setGatewayColor(Color gatewayColor) {
        this.gatewayColor = gatewayColor;
    }
    public Color getGatewayColor() {
        return gatewayColor;
    }
    public void setBackgroundImage(File backgroundImage) {
        this.backgroundImage = backgroundImage;
    }
    public File getBackgroundImage() {
        return backgroundImage;
    }
    public void setNotConfiguredintersectionColor(
            Color notConfiguredintersectionColor) {
        this.notConfiguredintersectionColor = notConfiguredintersectionColor;
    }
    public Color getNotConfiguredintersectionColor() {
        return notConfiguredintersectionColor;
    }

}

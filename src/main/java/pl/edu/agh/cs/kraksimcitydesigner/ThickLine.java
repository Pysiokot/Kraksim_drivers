package pl.edu.agh.cs.kraksimcitydesigner;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

// TODO: Auto-generated Javadoc
public class ThickLine {
	
	private Polygon polygon;

	 /**
 	 * Instantiates a new thick line.
 	 * 
 	 * @param x1 the x1
 	 * @param y1 the y1
 	 * @param x2 the x2
 	 * @param y2 the y2
 	 * @param thickness the thickness
 	 */
 	public ThickLine(int x1, int y1, int x2, int y2, int thickness) {	 
		int dX = x2 - x1;
		int dY = y2 - y1;
		// line length
		double lineLength = Math.sqrt(dX * dX + dY * dY);
		
		double scale = (double)(thickness) / (2 * lineLength);
		
		// The x,y increments from an endpoint needed to create a rectangle...
		double ddx = -scale * (double)dY;
		double ddy = scale * (double)dX;
		ddx += (ddx > 0) ? 0.5 : -0.5;
		ddy += (ddy > 0) ? 0.5 : -0.5;
		int dx = (int)ddx;
		int dy = (int)ddy;
		
		// Now we can compute the corner points...
		int xPoints[] = new int[4];
		int yPoints[] = new int[4];
		
		xPoints[0] = x1 + dx; yPoints[0] = y1 + dy;
		xPoints[1] = x1 - dx; yPoints[1] = y1 - dy;
		xPoints[2] = x2 - dx; yPoints[2] = y2 - dy;
		xPoints[3] = x2 + dx; yPoints[3] = y2 + dy;
		
		polygon = new Polygon(xPoints, yPoints, 4);
	 }

	/**
	 * Gets the shape.
	 * 
	 * @return the shape
	 */
	public Shape getShape() {
		return (Shape)polygon;
	}

}

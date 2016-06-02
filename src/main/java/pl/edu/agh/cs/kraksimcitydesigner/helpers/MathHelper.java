package pl.edu.agh.cs.kraksimcitydesigner.helpers;

public class MathHelper {
    
    /**
     * Checks if two vectors [x1,y1] and [x2,y2] has left orientation,<br>
     * i.e. <i>z</i> dimension of cross product is positive
     * @param vector1_x
     * @param vector1_y
     * @param vector2_x
     * @param vector2_y
     * @return
     */
    public static boolean leftOrientation(double vector1_x, double vector1_y, 
            double vector2_x, double vector2_y) {
        
        double z = vector1_x * vector2_y - vector2_x * vector1_y;
        if (z > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Calculate angle of the point in Polar coordinate system
     * @param x
     * @param y
     * @return
     */
    public static double calculateAngleForPoint(double x,
            double y) {
        
        if (x > 0 && y >= 0) {
            return Math.atan(y / x);
        }
        if (x > 0 && y < 0) {
            return Math.atan(y / x) + 2 * Math.PI;
        }
        if (x < 0) {
            return Math.atan(y / x) + Math.PI;
        }
        if (x == 0 && y > 0) {
            return Math.PI / 2;
        }
        if (x == 0 && y < 0) {
            return 3 * Math.PI / 2;
        }
        throw new RuntimeException();
    }
    
    

}

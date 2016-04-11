package pl.edu.agh.cs.kraksimcitydesigner.helpers;

import org.testng.annotations.Test;

import pl.edu.agh.cs.kraksimcitydesigner.helpers.MathHelper;

public class MathHelperTest {
    
    @Test
    public void leftOrientationTest() {
        
        double x1 = 3;
        double y1 = 1;
        
        double x2 = 0;
        double y2 = 1;
        
        assert MathHelper.leftOrientation(x1, y1, x2, y2) == true;
        assert MathHelper.leftOrientation(x2, y2, x1, y1) == false;
        
        x1 = 1;
        y1 = 3;
        
        x2 = -2;
        y2 = -3;
        
        assert MathHelper.leftOrientation(x1, y1, x2, y2) == true;
        assert MathHelper.leftOrientation(x2, y2, x1, y1) == false;
        
        x1 = 1;
        y1 = 3;
        
        x2 = -0.5;
        y2 = -3;
        
        assert MathHelper.leftOrientation(x1, y1, x2, y2) == false;
        assert MathHelper.leftOrientation(x2, y2, x1, y1) == true;
    }

}

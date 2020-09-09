package pl.edu.agh.cs.kraksim.real_extended;

import pl.edu.agh.cs.kraksim.KraksimConfigurator;
import pl.edu.agh.cs.kraksim.main.drivers.Driver;

import java.awt.*;
import java.util.Random;

public class DriverType {
    private static final Random randomGenerator= new Random(1234);
    private double speedLimitInterpretation;
    private double chanceForDoubleAcceleration;
    private double timeOverDistancePriority;
    private int type = -1;
    private Color color;

    DriverType(double speedLimitInterpretation ,double chanceForDoubleAcceleration,int turnsToIgnoreCrashRules,int frontSpaceOnRightNeeded,int laneSwitchTurnLimit,double powerValue, double routeChangeProbability, double timeOverDistancePriority,Color color) {

        speedLimitInterpretation = speedLimitInterpretation;
        chanceForDoubleAcceleration = chanceForDoubleAcceleration;
        timeOverDistancePriority = timeOverDistancePriority;
        color = color;

    }

    public DriverType(int type){
        if (type==1){
            // spokojni
            speedLimitInterpretation = 0.7;
            chanceForDoubleAcceleration = 0.0;
            timeOverDistancePriority  = 0.2;
            color = Color.BLACK;
        }
        else if (type==3){
            // agresywni
            speedLimitInterpretation = 1.3;
            chanceForDoubleAcceleration = 0.4;
            timeOverDistancePriority  = 0.2;
            color = Color.RED;
        }
        else{
            // przeciętni
            speedLimitInterpretation = 1.0;
            chanceForDoubleAcceleration = 0.2;
            timeOverDistancePriority  = 0.2;
            color = Color.ORANGE;
        }
        this.type = type;
    }

    public static DriverType generateDriverType(){
        int calm = Integer.parseInt(KraksimConfigurator.getProperty("calm"));
        int normal = Integer.parseInt(KraksimConfigurator.getProperty("normal"));
        int aggressive = Integer.parseInt(KraksimConfigurator.getProperty("agressive"));

        int decision = randomGenerator.nextInt(calm+normal+aggressive);

        if( calm+normal+aggressive == 100) {
            if (decision < calm) {
                return new DriverType(1);
            } else if (decision < calm + normal) {
                return new DriverType(2);
            } else {
                return new DriverType(3);
            }
        }
        else{
            return new DriverType(2);
        }
    }

    public Color getColor() {
        return color;
    }

    public double getChanceForDoubleAcceleration() {
        return chanceForDoubleAcceleration;
    }

    public double getSpeedLimitInterpretation() {
        return speedLimitInterpretation;
    }

    public double getTimeOverDistancePriority() {
        return timeOverDistancePriority;
    }

    public int getType() {
        return type-1;
    }
}

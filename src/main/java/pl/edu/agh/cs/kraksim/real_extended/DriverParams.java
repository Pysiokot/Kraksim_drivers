package pl.edu.agh.cs.kraksim.real_extended;

import pl.edu.agh.cs.kraksim.KraksimConfigurator;
import pl.edu.agh.cs.kraksim.core.Link;

import java.util.HashMap;
import java.util.Map;

public class DriverParams {

    private static final Map<Link, DriverParams> linkToParams = new HashMap<>();

    private int[] turnsToIgnoreCrashRules=new int[3];
    private int[] frontSpaceOnRightNeeded=new int[3];
    private int[] laneSwitchTurnLimit=new int[3];
    private double[] powerValue=new double[3];

    private int[] agressionLvl=new int[3];

//    private static double[] defaults = new double[]{
//            300, 75, 15,
//            15, 10, 10,
//            13, 10, 7,
//            2, 4, 7};
//
//    private static double[] defaults0 = new double[]{
//            500, 100, 25,
//            20, 15, 15,
//            16, 13, 10,
//            1, 3, 6};
//
//    private static double[] defaults2 = new double[]{
//            100, 50, 5,
//            15, 10, 10,
//            10, 7, 4,
//            3, 5, 8};

    //lowes level of params
    private static double[] defaults0 = new double[]{
            100,50, 25,
            20, 20, 20,
            13, 10, 7,
            1, 3, 6};
    //medium level of params
    private static double[] defaults = new double[]{
            50, 25, 10,
            15, 15, 15,
            11, 8, 5,
            2, 4, 7};
    //highest level of params
    private static double[] defaults2 = new double[]{
            25, 10, 5,
            10, 10, 10,
            9, 6, 3,
            3, 5, 8};


    private DriverParams(double [] defaults) {

        for(int i=0; i<defaults.length/4; i++){
            turnsToIgnoreCrashRules[i]=(int) defaults[i];
            frontSpaceOnRightNeeded[i]=(int) defaults[i+3];
            laneSwitchTurnLimit[i]=(int) defaults[i+6];
            powerValue[i]=defaults[i+9];
            agressionLvl[i]=1;

        }
        setLevelForType(2, Integer.parseInt(KraksimConfigurator.getProperty("alvl")));
        setLevelForType(1, Integer.parseInt(KraksimConfigurator.getProperty("clvl")));
        setLevelForType(0, Integer.parseInt(KraksimConfigurator.getProperty("nlvl")));

    }

    public static DriverParams getInstance(Link link) {
        if (linkToParams.get(link) == null) {
            linkToParams.put(link, new DriverParams(defaults));
        }
        return linkToParams.get(link);
    }


    public double getPowerValue(int type) { return powerValue[type]; }

    public int getFrontSpaceOnRightNeeded(int type) {
        return frontSpaceOnRightNeeded[type];
    }

    public int getLaneSwitchTurnLimit(int type) {
        return laneSwitchTurnLimit[type];
    }

    public int getTurnsToIgnoreCrashRules(int type) {
        return turnsToIgnoreCrashRules[type];
    }

    public DriverParams setPowerValue(int type, double val) {
        this.powerValue[type] = val;
        return this;
    }
    public DriverParams setFrontSpaceOnRightNeeded(int type, int val) {
        this.frontSpaceOnRightNeeded[type] = val;
        return this;
    }
    public DriverParams setLaneSwitchTurnLimit(int type, int val) {
        this.laneSwitchTurnLimit[type] = val;
        return this;
    }
    public DriverParams setTurnsToIgnoreCrashRules(int type, int val) {
        this.turnsToIgnoreCrashRules[type] = val;
        return this;
    }

    public DriverParams setLevelForType (int type, int lvl){

        double[] lvlValues;
        switch (lvl) {
            case 0:
                lvlValues = defaults0;
                break;
            case 1:
                lvlValues = defaults;
                break;
            case 2:
                lvlValues = defaults2;
                break;
            default:
                lvlValues = defaults;
                break;
        }
        setTurnsToIgnoreCrashRules(type, (int)lvlValues[type]);
        setFrontSpaceOnRightNeeded(type, (int)lvlValues[type+3]);
        setLaneSwitchTurnLimit(type, (int)lvlValues[type+6]);
        setPowerValue(type, (int)lvlValues[type+9]);
        this.agressionLvl[type]=lvl;
        return this;

    }

    public int getAgressionLvlforType(int type) {
        return agressionLvl[type];
    }
}

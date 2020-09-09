package pl.edu.agh.cs.kraksim.real_extended;


import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import pl.edu.agh.cs.kraksim.core.City;
import pl.edu.agh.cs.kraksim.core.Lane;
import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.iface.Clock;
import pl.edu.agh.cs.kraksim.iface.carinfo.CarInfoIView;
import pl.edu.agh.cs.kraksim.ministat.MiniStatEView;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

public class DriverEnv implements LearningEnv{
    private static final Logger LOG = Logger.getLogger(DriverEnv.class);

    private static final double INIT_VALS = 0;
    private static final int NUM_OF_STATE_PARAMS = 1;   // e.g. number of emergency vehicles, traffic density
    private static final int NUM_ACTIONS = 27;   // combinations of two params (d_c, d_e) with three possible values, e.g. d_c = {10, 20, 30}, d_e = {5, 10, 15}

//    private static final ArrayList<Integer> turnsAgressiveValues= Lists.newArrayList(100,300,500);
//    private static final ArrayList<Integer> turnsNormalValues= Lists.newArrayList(50,75,100);
//    private static final ArrayList<Integer> turnsCalmValues= Lists.newArrayList(5,15,25);
//
//    private static final ArrayList<Integer> frontAgressiveValues= Lists.newArrayList(5,10,15);
//    private static final ArrayList<Integer> frontNormalValues= Lists.newArrayList(5,10,15);
//    private static final ArrayList<Integer> frontCalmValues= Lists.newArrayList(10,15,20);
//
//    private static final ArrayList<Integer> laneAgressiveValues= Lists.newArrayList(10,7,4);
//    private static final ArrayList<Integer> laneNormalValues= Lists.newArrayList(13,10,7);
//    private static final ArrayList<Integer> laneCalmValues= Lists.newArrayList(16,13,10);
//
//    private static final ArrayList<Double> powerAgressiveValues= Lists.newArrayList(3,3.5,4);
//    private static final ArrayList<Double> powerNormalValues= Lists.newArrayList(2,2.5,3);
//    private static final ArrayList<Double> powerCalmValues= Lists.newArrayList(1,1.5,2);

    private static final ArrayList<Integer> agressivenessLevels = Lists.newArrayList(0,1,2);

    private final Link link;
    private final City city;
    private final MiniStatEView statView;
    private final CarInfoIView carInfoView;
    private final Clock clock;

    private double previousDensity;
    private int reward = 0;
    private int timeStamp = 0;
    private int successScore = 0;
    private int failScore = 0;
    PrintWriter linkWriter;

    private int[][] combs = generateCombs();

    public DriverEnv(Link link, City city, MiniStatEView statView, CarInfoIView carInfoView, Clock clock, String statFile) {
        this.link = link;
        this.city = city;
        this.statView = statView;
        this.carInfoView = carInfoView;
        this.clock = clock;

        try {
            this.linkWriter = new PrintWriter(new BufferedOutputStream(
                    new FileOutputStream(statFile.split(".csv")[0] + "\\" + link.getId() + ".csv")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        linkWriter.write("link, turn, state, action, calm, normal, aggresive\n");
    }

    @Override
    public int[] getDimension() {
        int[] retDim = new int[NUM_OF_STATE_PARAMS + 1];
        //retDim[0] = 3;  // nr of emergency cars: 0, 1, >1
        retDim[0] = 7;  // density of normal cars: <0.1, 0.1 - 0.2, ...  0.6 - 0.7, >0.7
        retDim[1] = NUM_ACTIONS;  // d_c and d_e combinations
        return retDim;
    }

    @Override
    public int[] getNextState(int action) {
        return getState(action);
    }

    @Override
    public void cleanUp(){
        linkWriter.close();
    }

    private int[] getState(int action) {

        int emVehState;
        int emVehCount = statView.ext(link).getEmergencyVehiclesCount();
        if (emVehCount == 0) {
            emVehState = 0;
        } else if (emVehCount == 1) {
            emVehState = 1;
        } else {
            emVehState = 2;
        }

        int allLanesLength = StreamSupport.stream(Spliterators.spliteratorUnknownSize(link.laneIterator(), Spliterator.ORDERED), false)
                .mapToInt(Lane::getLength)
                .sum();

        int normalCarsCount = statView.ext(link).getNormalCarsCount();

        double density = 5*((double) (normalCarsCount + emVehCount)) / allLanesLength;

        int densityState;
        if (density < 0.05) {
            densityState = 0;
        } else if (density >= 0.05 && density < 0.1) {
            densityState = 1;
        } else if (density >= 0.1 && density < 0.15) {
            densityState = 2;
        } else if (density >= 0.15 && density < 0.2) {
            densityState = 3;
        } else if (density >= 0.2 && density < 0.25) {
            densityState = 4;
        } else if (density >= 0.25 && density < 0.3) {
            densityState = 5;
        } else {
            densityState = 6;
        }

        if (density != 0) {
            LOG.info(String.format("Next state: %d %s %d %d %.2f", clock.getTurn(), link.getId(), action, emVehCount, density));
        }

        //int[] state = {emVehState, densityState};
        int[] state = {densityState};

        calculateReward(density);
        previousDensity = density;

        return state;
    }

    private void calculateReward(double density) {
        // compare density
        if (previousDensity == 0 || density == 0) {
            reward = 0;
        } else if (previousDensity == density) {
            reward = 0;
        } else if (previousDensity < density) {
            failScore++;
            reward = -50;
        } else {
            successScore++;
            reward = 100;
        }
    }

    @Override
    public double getReward() {
        return reward;
    }

    @Override
    public boolean validAction(int action, int[] state) {
        if (NUM_ACTIONS==27){
            updateDParams2(action, state);
        }
        if (NUM_ACTIONS==7){
            updateDParams(action);
        }
        return action >= 0 && action < NUM_ACTIONS;
    }

    private void updateDParams(int action) {
        int old, tmp, type;

        switch (action) {
            case 0:
                type=0;
                old = DriverParams.getInstance(link).getAgressionLvlforType(0);
                tmp = getPrevious(agressivenessLevels, old);
                DriverParams.getInstance(link).setLevelForType(0,tmp);
                break;
            case 1:
                type=0;
                old = DriverParams.getInstance(link).getAgressionLvlforType(0);
                tmp = getNext(agressivenessLevels, old);
                DriverParams.getInstance(link).setLevelForType(0,tmp);
                break;
            case 2:
                type=1;
                old = DriverParams.getInstance(link).getAgressionLvlforType(1);
                tmp = getPrevious(agressivenessLevels, old);
                DriverParams.getInstance(link).setLevelForType(1,tmp);
                break;
            case 3:
                type=1;
                old = DriverParams.getInstance(link).getAgressionLvlforType(1);
                tmp = getNext(agressivenessLevels, old);
                DriverParams.getInstance(link).setLevelForType(1,tmp);
                break;
            case 4:
                type=2;
                old = DriverParams.getInstance(link).getAgressionLvlforType(2);
                tmp = getPrevious(agressivenessLevels, old);
                DriverParams.getInstance(link).setLevelForType(2,tmp);
                break;
            case 5:
                type=2;
                old = DriverParams.getInstance(link).getAgressionLvlforType(2);
                tmp = getNext(agressivenessLevels, old);
                DriverParams.getInstance(link).setLevelForType(2,tmp);
                break;
            case 6:
                tmp = -1;
                type = -1;
                break;
            default:
                throw new RuntimeException("Unknown action");
        }
        LOG.info(String.format("Next DriverParams: turn:%d, link:%s, action:%d, type:%d, lvl:%d", clock.getTurn(), link.getId(), action, type, tmp));
        //LOG.info(String.format("Next DriverParams: turn:%d, link:%s, calm:%d, normal:%d, agressive:%d", clock.getTurn(), link.getId(), DriverParams.getInstance(link).getAgressionLvlforType(0), DriverParams.getInstance(link).getAgressionLvlforType(1), DriverParams.getInstance(link).getAgressionLvlforType(2)));

    }


    private void updateDParams2(int action, int[] state) {
        if(action>NUM_ACTIONS){
            throw new RuntimeException("Unknown action");
        }
        int[] cur = updateLvls(combs[action][0],combs[action][1],combs[action][2]);

        //LOG.info(String.format("DriverParams: turn:%d, link:%s, action:%d, calm:%d, normal:%d, aggresive:%d", clock.getTurn(), link.getId(), action, cur[0], cur[1], cur[2]));
        //LOG.info(String.format("Next DriverParams: turn:%d, link:%s, calm:%d, normal:%d, agressive:%d", clock.getTurn(), link.getId(), DriverParams.getInstance(link).getAgressionLvlforType(0), DriverParams.getInstance(link).getAgressionLvlforType(1), DriverParams.getInstance(link).getAgressionLvlforType(2)));
        linkWriter.write(String.format("%s, %d, %d, %d, %d, %d, %d\n", link.getId(), clock.getTurn(), state[0], action, cur[0], cur[1], cur[2]));
    }

    private int[] updateLvls(int c, int n, int a){
        int calm=updateLvlForType(c,0);
        int norm=updateLvlForType(n,1);
        int aggr=updateLvlForType(a,2);

        return new int[]{calm,norm,aggr};
    }

    private int updateLvlForType(int actionType, int driverType){
        int old,tmp;
        old = DriverParams.getInstance(link).getAgressionLvlforType(driverType);
        if(actionType==0){
            tmp = getPrevious(agressivenessLevels, old);
            DriverParams.getInstance(link).setLevelForType(driverType,tmp);
        }
        else if(actionType==2) {
            tmp = getNext(agressivenessLevels, old);
            DriverParams.getInstance(link).setLevelForType(driverType,tmp);
        }
        else{
            tmp = old;
        }
        return tmp;
    }

    public int[][] generateCombs(){
        int[][] combs = new int[27][3];

        for(int i=0;i<3;i++){
            for(int j=0;j<3;j++){
                for(int k=0;k<3;k++){
                    combs[i*3+j*3+k]=new int[]{i,j,k};
                }
            }
        }
        return combs;
    }

    private Integer getNext(List<Integer> allValues, int oldParam) {
        int idx = allValues.indexOf(oldParam);
        if (idx == allValues.size() - 1) {
            return oldParam;
        }
        return allValues.get(idx + 1);
    }

    private Integer getPrevious(List<Integer> allValues, int oldParam) {
        int idx = allValues.indexOf(oldParam);
        if (idx == 0) {
            return oldParam;
        }
        return allValues.get(idx - 1);
    }

    @Override
    public boolean endState() {
        if (clock.getTurn() - timeStamp > 50) {
            timeStamp = clock.getTurn();
            return true;
        }
        return false;
    }

    @Override
    public int[] resetState() {
        if (successScore != 0 && failScore != 0) {
            LOG.info(String.format("Reset: %d %s %d %d", clock.getTurn(), link.getId(), successScore, failScore));
            successScore = 0;
            failScore = 0;
        }
        return getState(-1);
    }

    @Override
    public double getInitValues() {
        return INIT_VALS;
    }

    @Override
    public String getLinkToString() {
        return link.toString();
    }

}

/*
    private void updateDParams(int action) {
        int oldDe = DParams.getInstance(link).getDe();
        int oldDc = DParams.getInstance(link).getDc();

        int tmpDc, tmpDe;

        switch (action) {
            case 0:
                tmpDe = DParams.getInstance(link).getDe();
                tmpDc = getNext(DC_VALUES, oldDc);
                DParams.getInstance(link).setDc(tmpDc);
                break;
            case 1:
                tmpDe = getNext(DE_VALUES, oldDe);
                tmpDc = DParams.getInstance(link).getDc();
                DParams.getInstance(link).setDe(tmpDe);
                break;
            case 2:
                tmpDe = getNext(DE_VALUES, oldDe);
                tmpDc = getNext(DC_VALUES, oldDc);
                DParams.getInstance(link).setDe(tmpDe).setDc(tmpDc);
                break;
            case 3:
                tmpDe = DParams.getInstance(link).getDe();
                tmpDc = getPrevious(DC_VALUES, oldDc);
                DParams.getInstance(link).setDc(tmpDc);
                break;
            case 4:
                tmpDe = getPrevious(DE_VALUES, oldDe);
                tmpDc = DParams.getInstance(link).getDc();
                DParams.getInstance(link).setDe(tmpDe);
                break;
            case 5:
                tmpDe = getPrevious(DE_VALUES, oldDe);
                tmpDc = getPrevious(DC_VALUES, oldDc);
                DParams.getInstance(link).setDe(tmpDe).setDc(tmpDc);
                break;
            default:
                throw new RuntimeException("Unknown action");
        }

        LOG.info(String.format("Next DParams: %d, %s %d %d %d", clock.getTurn(), link.getId(), action, tmpDe, tmpDc));
    }
*/
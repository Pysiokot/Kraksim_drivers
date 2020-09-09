package pl.edu.agh.cs.kraksim.real_extended;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.Arrays;

public class QLearner {

    LearningEnv thisWorld;
    Policy policy;

    double epsilon;
    double alpha;
    double gamma;
    double lambda;

    int[] dimSize;
    int[] state;
    int action;

    public boolean running;
    boolean shouldResetState = true;

    public QLearner() {}

    public QLearner(LearningEnv world) {
        // Getting the world from the invoking method.
        thisWorld = world;

        // Get dimensions of the world.
        dimSize = thisWorld.getDimension();

        // Creating new policy with dimensions to suit the world.
        policy = new Policy(dimSize);

        // Initializing the policy with the initial values defined by the world.
        policy.initValues(thisWorld.getInitValues());

        // set default values
        epsilon = 0.1;
        alpha = 0.2;
        gamma = 0.8;
        lambda = 0.1;

        //System.out.println("QLearner initialised");

    }

    public void cleanUp(){
        thisWorld.cleanUp();
    }

    // FIXME cleanup
    // execute one epoch of QLearning
    public void runEpoch() {
//        runPreEpoch();
////             Kraksim simulation
//        runPostEpoch();
    }

    public void runPreEpoch() {
        if (shouldResetState) {
            state = thisWorld.resetState();
            shouldResetState = false;
        }

        if (thisWorld.endState()) {
            shouldResetState = true;
        }
        // Calculate d_e and d_c based on metrics e.g. avg velocity
        action = selectAction(state);
    }

    public void runPostEpoch() {
        int[] newState = thisWorld.getNextState(action);
        double reward = thisWorld.getReward();

        double this_Q = policy.getQValue(state, action);
        double max_Q = policy.getMaxQValue(newState);

        // Calculate new Value for Q
        double new_Q = this_Q + alpha * (reward + gamma * max_Q - this_Q);
        policy.setQValue(state, action, new_Q);

        // Set state to the new state.
        state = newState;
    }

    /**
     * Greedy action selector
     *
     * @param state
     * @return
     */
    private int selectAction(int[] state) {

        double[] qValues = policy.getQValuesAt(state);
        int selectedAction = -1;

        double maxQ = -Double.MAX_VALUE;
        int[] doubleValues = new int[qValues.length];
        int maxDV = 0;

        //Explore
        if (Math.random() < epsilon) {
            selectedAction = -1;
        } else {

            for (int action = 0; action < qValues.length; action++) {
                if (qValues[action] > maxQ) {
                    selectedAction = action;
                    maxQ = qValues[action];
                    maxDV = 0;
                    doubleValues[maxDV] = selectedAction;
                } else if (qValues[action] == maxQ) {
                    maxDV++;
                    doubleValues[maxDV] = action;
                }
            }

            if (maxDV > 0) {
                int randomIndex = (int) (Math.random() * (maxDV + 1));
                selectedAction = doubleValues[randomIndex];
            }
        }

        // Select random action if all qValues == 0 or exploring.
        if (selectedAction == -1) {

            // System.out.println( "Exploring ..." );
            selectedAction = (int) (Math.random() * qValues.length);
        }

        // Choose new action if not valid.
        while (!thisWorld.validAction(selectedAction, state)) {

            selectedAction = (int) (Math.random() * qValues.length);
            // System.out.println( "Invalid action, new one:" + selectedAction);
        }

        return selectedAction;
    }

    public String QTableToString(){
        String lane = thisWorld.getLinkToString();
        Object table = policy.getQTable();
        int[][] visits = policy.getVisits();
        String res=lane+"\n";
        for (int i=0;i<7;i++){
            String line ="State "+i+"("+ Arrays.stream(visits[i]).sum()+")\n";
            for (int j=0;j<27;j++){
                line +=String.format("%.2f (%d)\t",(Double) Array.get(Array.get(table, i),j),visits[i][j]) ;
            }
            res+=line+"\n";
        }
        //System.out.println(res);
        return res;
    }


    public Policy getPolicy() {
        return policy;
    }

    public void setAlpha(double a) {
        if (a >= 0 && a < 1) {
            alpha = a;
        }
    }

    public double getAlpha() {
        return alpha;
    }

    public void setGamma(double g) {
        if (g > 0 && g < 1) {
            gamma = g;
        }
    }

    public double getGamma() {
        return gamma;
    }

    public void setEpsilon(double e) {
        if (e > 0 && e < 1) {
            epsilon = e;
        }
    }

    public double getEpsilon() {
        return epsilon;
    }

    //AK: let us clear the policy
    public Policy newPolicy() {
        policy = new Policy(dimSize);
        // Initializing the policy with the initial values defined by the world.
        policy.initValues(thisWorld.getInitValues());
        return policy;
    }
}
	

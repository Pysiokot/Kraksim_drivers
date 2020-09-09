package pl.edu.agh.cs.kraksim.real_extended;

import pl.edu.agh.cs.kraksim.main.CarMoveModel;

import java.util.Random;

public final class RealSimulationParams {
	public static final float CELL_LENGTH_IN_METERS = 7.5f;
	public static final float TURN_DURATION_IN_SECONDS = 5.0f;
	public static final int DEFAULT_MAX_VELOCITY = 10;
	public static final float DEFAULT_DECEL_PROB = 0.2f;
	public static final int DEFAULT_PRIOR_LANE_TIME_HEADWAY = 4;
	/**
	 * For deadlock
	 */
	public static final float DEFAULT_VICTIM_PROB = 0.8f;
	/**
	 * allows to change graph coloring level
	 */
	private static float redLevel = 0.6f;
	private static float orangeLevel = 0.3f;
	private final Random randomGenerator;

	private int switchTime = 0;
    private int minSafeDistance = 0;

	/**
	 * maximum car velocity
	 */
	final int maxVelocity;
	/**
	 * probability of deceleration in a turn
	 */
	final float decelProb;
	/**
	 * probability of becoming victim in deadlock situation
	 */
	final float victimProb;
	/**
	 * minimum time distance to an intersection for a car on a lane prior to
	 * some action, that an action can be performed
	 */
	final int priorLaneTimeHeadway;
	final CarMoveModel carMoveModel;

	public RealSimulationParams(Random rg, int maxVelocity, float decelProb, int priorLaneTimeHeadway) {
		this.maxVelocity = maxVelocity;
		this.decelProb = decelProb;
		this.priorLaneTimeHeadway = priorLaneTimeHeadway;
		randomGenerator = rg;
		carMoveModel = new CarMoveModel("nagel");
		victimProb = DEFAULT_VICTIM_PROB;
	}

	/* use default values */
	public RealSimulationParams(Random rg, CarMoveModel carMoveModel) {
		randomGenerator = rg;
		this.carMoveModel = carMoveModel;
		maxVelocity = DEFAULT_MAX_VELOCITY;
		decelProb = DEFAULT_DECEL_PROB;
		priorLaneTimeHeadway = DEFAULT_PRIOR_LANE_TIME_HEADWAY;
		victimProb = DEFAULT_VICTIM_PROB;
	}

	public static float convertToSeconds(int turns) {
		return turns * TURN_DURATION_IN_SECONDS;
	}

	public static float convertToMeters(int cells) {
		return cells * CELL_LENGTH_IN_METERS;
	}

	public static float convertFromKMToSpeed(float kmSpeed) {
		return kmSpeed * TURN_DURATION_IN_SECONDS / (CELL_LENGTH_IN_METERS * 3.6f);
	}

	public static float convertToKPH(float speed) {
		return 3.6f * convertToMeterPS(speed);
	}

	public static float convertToMeterPS(float speed) {
		return speed * CELL_LENGTH_IN_METERS / TURN_DURATION_IN_SECONDS;
	}

	public static float getRedLevel() {
		return redLevel;
	}

	public static void setRedLevel(float redLevel) {
		RealSimulationParams.redLevel = redLevel;
	}

	public static float getOrangeLevel() {
		return orangeLevel;
	}

	public static void setOrangeLevel(float orangeLevel) {
		RealSimulationParams.orangeLevel = orangeLevel;
	}

	public Random getRandomGenerator() {
		return randomGenerator;
	}

    public void setSwitchTime(int switchTime){
        this.switchTime = switchTime;
    }

    public int getSwitchTime() {
        return this.switchTime;
    }

    public void setMinSafeDistance(int minSafeDistance){
        this.minSafeDistance = minSafeDistance;
    }

    public int getMinSafeDistance(){
        return this.minSafeDistance;
    }
}

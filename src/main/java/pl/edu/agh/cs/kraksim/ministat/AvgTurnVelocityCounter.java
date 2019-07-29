package pl.edu.agh.cs.kraksim.ministat;

public class AvgTurnVelocityCounter {
	private int normalCarCount;
	private double normalCarVelocity;
	private int emergencyCarCount;
	private double emergencyCarVelocity;
	
	public AvgTurnVelocityCounter() {
		this.reset();
	}
	
	public void reset() {
		normalCarCount = 0;
		emergencyCarCount = 0;
		normalCarVelocity = 0;
		emergencyCarVelocity = 0;
	}
	
	public void insertNormalCarVelocity(int normalVel) {
		normalCarVelocity = ((normalCarVelocity * normalCarCount) + normalVel) / (normalCarCount+1);
		normalCarCount++;
	}
	
	public void insertEmergencyCarVelocity(int emergencyVel) {
		emergencyCarVelocity = ((emergencyCarVelocity * emergencyCarCount) + emergencyVel) / (emergencyCarCount+1);
		emergencyCarCount++;
	}
	
	public double getAvgNormalCarVelocity() {
		return this.normalCarVelocity;
	}
	
	public double getAvgEmergencyCarVelocity() {
		return this.emergencyCarVelocity;
	}
	
	public double getAvgAllVelocity() {
		return (normalCarVelocity*normalCarCount + emergencyCarVelocity*emergencyCarCount) / (normalCarCount+emergencyCarCount);
	}
	
	public int getEmergencyCarCount() {
		return emergencyCarCount;
	}
	public int getNormalCarCount() {
		return normalCarCount;
	}
	public int getAllCarCount() {
		return normalCarCount+emergencyCarCount;
	}
	
}

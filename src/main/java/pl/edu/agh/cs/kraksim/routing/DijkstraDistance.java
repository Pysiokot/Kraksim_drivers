package pl.edu.agh.cs.kraksim.routing;

public class DijkstraDistance {
	double distance;

	public DijkstraDistance() {
		distance = Double.MAX_VALUE;
	}

	public DijkstraDistance(double newDistance) {
		distance = newDistance;
	}

	public String toString() {
		return Double.toString(distance);
	}
}
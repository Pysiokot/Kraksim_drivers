package pl.edu.agh.cs.kraksim.routing.prediction;

public class WorldState {
	private final double[] state;    // TODO: change to TrafficLevel

	public WorldState(double[] state) {
		int length = state.length;
		this.state = new double[length];
		System.arraycopy(state, 0, this.state, 0, length);
	}

	public double getStateAt(int position) {
		if ((position < 0) || (position >= state.length)) {
			throw new IndexOutOfBoundsException("Unable to set state ar row no. " + position);
		}
		return state[position];
	}
}

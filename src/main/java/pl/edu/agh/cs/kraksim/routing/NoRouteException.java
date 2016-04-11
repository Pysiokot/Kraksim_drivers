package pl.edu.agh.cs.kraksim.routing;

@SuppressWarnings("serial")
public class NoRouteException extends Exception {
	public NoRouteException(String message) {
		super(message);
	}
}

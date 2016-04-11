package pl.edu.agh.cs.kraksim.iface.sim;

public interface GatewaySimIface {
	void setTravelEndHandler(TravelEndHandler handler);

	TravelEndHandler getTravelEndHandler();
}

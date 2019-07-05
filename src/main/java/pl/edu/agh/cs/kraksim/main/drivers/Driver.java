package pl.edu.agh.cs.kraksim.main.drivers;

import pl.edu.agh.cs.kraksim.core.Gateway;
import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.routing.Router;
import pl.edu.agh.cs.kraksim.traffic.TravellingScheme;

import java.awt.*;
import java.util.ListIterator;
import java.util.Random;

public abstract class Driver implements Comparable<Driver> {
	protected final int id;
	protected final TravellingScheme.Cursor cursor;
	protected final Router router;
	protected int departureTurn;
	protected Color carColor;
	protected boolean emergency;

	protected Driver(int id, TravellingScheme scheme, Router router, boolean emergency) {
		this.id = id;
		this.router = router;
		cursor = scheme.cursor();
		if (emergency) {
			carColor = scheme.getEmergencyVehicleColor();
		} else {
			carColor = scheme.getDriverColor();
		}
		this.emergency = emergency;
	}

	public abstract ListIterator<Link> updateRouteFrom(Link sourceLink);

	@Override
	public int compareTo(Driver driver) {
		return departureTurn - driver.departureTurn;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Driver && id == ((Driver) obj).id;
	}

	public boolean nextTravel() {
		cursor.next();
		return cursor.isValid();
	}

	public Gateway srcGateway() {
		return cursor.srcGateway();
	}

	public Gateway destGateway() {
		return cursor.destGateway();
	}

	public int getDepartureTurn() {
		return departureTurn;
	}

	public void setDepartureTurn(Random rg) {
		departureTurn = cursor.drawDepartureTurn(rg);
	}

	public Color getCarColor() {
		return carColor;
	}

	public void setCarColor(Color carColor) {
		this.carColor = carColor;
	}

	public boolean isEmergency() {
		return emergency;
	}

	public void setEmergency(boolean emergency) {
		this.emergency = emergency;
	}

	public DriverZones getAllowedZones() {
		return null;
	}
}

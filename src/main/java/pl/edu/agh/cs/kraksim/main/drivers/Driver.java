package pl.edu.agh.cs.kraksim.main.drivers;

import pl.edu.agh.cs.kraksim.core.Gateway;
import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.real_extended.DriverType;
import pl.edu.agh.cs.kraksim.routing.Router;
import pl.edu.agh.cs.kraksim.traffic.TravellingScheme;

import java.awt.*;
import java.util.ListIterator;
import java.util.Random;

public abstract class Driver implements Comparable<Driver> {
	protected final int id;
	protected final TravellingScheme.Cursor cursor;
	protected final Router router;
	protected final DriverType driverType;
	protected int departureTurn;
	protected Color carColor;
	protected boolean emergency;
	private final static Color[] carColors = {new Color(0xff60a917), new Color(0xff008a00), new Color(0xff1ba1e2),
			new Color(0xff0050ef), new Color(0xffaa00ff), new Color(0xfff472d0), new Color(0xffd80073),
			new Color(0xffa20025), new Color(0xffe51400), new Color(0xfffa6800), new Color(0xfff0a30a),
			new Color(0xffe3c800),	new Color(0xff825a2c)};

	protected Driver(int id, TravellingScheme scheme, Router router, boolean emergency, DriverType driverType) {
		this.id = id;
		this.router = router;
		this.driverType = driverType;
		cursor = scheme.cursor();
		if (emergency) {
			carColor = Color.WHITE;
		} else {
			Random generator = new Random();
			//this.carColor = carColors[generator.nextInt(carColors.length)];
			this.carColor = driverType.getColor();
		}
		this.emergency = emergency;
	}

	public double getLinkTime(Link link){
		if (router != null) {
			return router.getLinkDistance(link);
		}
		return -1;
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

	public DriverType getDriverType() {
		return driverType;
	}
}

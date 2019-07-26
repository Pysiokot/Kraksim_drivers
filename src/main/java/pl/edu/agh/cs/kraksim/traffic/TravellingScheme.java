package pl.edu.agh.cs.kraksim.traffic;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.tuple.Pair;
import pl.edu.agh.cs.kraksim.KraksimConfigurator;
import pl.edu.agh.cs.kraksim.core.Gateway;
import pl.edu.agh.cs.kraksim.main.StartupParameters;
import pl.edu.agh.cs.kraksim.main.drivers.DecisionHelper;
import pl.edu.agh.cs.kraksim.main.drivers.Driver;
import pl.edu.agh.cs.kraksim.main.drivers.DriverZones;
import pl.edu.agh.cs.kraksim.main.drivers.ZoneAwareDriver;
import pl.edu.agh.cs.kraksim.routing.TimeBasedRouter;

import java.awt.*;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class TravellingScheme {
	private final String emergencyVehiclesConfiguration;
	private final int count;
	private final int emergencyVehicles;
	private final Gateway[] gateways;
	private final Distribution[] departureDists;
	private final Color driverColor;
    private final Color emergencyVehicleColor;
	private final Iterator<DriverZones> driver_zones;
	private final StartupParameters parameters;

	/**
	 * @param count          liczba samochodow, ktorych dotyczy schemat
	 * @param gateways       tablica wezlow schematu
	 * @param departureDists tablica opisujaca rozklady p-stwa czasu odjazdu z węzłów;
	 *                       gateways.length == departureDists.length + 1 (z ostatniego
	 *                       węzła nie ma odjazdu)
	 */
	public TravellingScheme(StartupParameters parameters, int count, Gateway[] gateways, Distribution[] departureDists, Collection<Pair<String, Double>> zones, Color driverColor, Color emergencyVehicleColor) throws IllegalArgumentException {
		Preconditions.checkArgument(gateways.length >= 2, "There should be at least two gateways in travelling scheme");
		Preconditions.checkArgument(gateways.length == departureDists.length + 1, "There should be one gateway more than departure distributions");
		this.parameters = parameters;
		emergencyVehiclesConfiguration = KraksimConfigurator.getProperty("emergencyVehiclesConfiguration");
		Properties properties = new Properties();
		try {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(emergencyVehiclesConfiguration));
			properties.load(bis);
			bis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		double emergencySpawnPercentage = Double.parseDouble(KraksimConfigurator.getProperty("emergency_spawnPercentage"));
		this.count = count;
		this.emergencyVehicles = (int) Math.round(this.count * emergencySpawnPercentage);
		this.gateways = gateways;
		this.departureDists = departureDists;
        driver_zones = prepareDriverZones(zones);
		this.driverColor = driverColor;
        this.emergencyVehicleColor = emergencyVehicleColor;
	}

	public int getCount() {
		return count;
	}

    public int getEmergencyVehicles() {
        return emergencyVehicles;
    }

	public Cursor cursor() {
		return new Cursor();
	}

	public Driver generateDriver(int id, boolean emergency, TravellingScheme travelScheme, TimeBasedRouter dynamicRouter, DecisionHelper decisionHelper) {
		DriverZones allowed_zones = getDriverZones();

		return new ZoneAwareDriver(id, travelScheme, dynamicRouter, emergency, decisionHelper, allowed_zones);
	}

	public class Cursor {
		int i;

		public boolean isValid() {
			return i < gateways.length - 1;
		}

		public Gateway srcGateway() throws NoSuchElementException {
			if (i >= gateways.length - 1) {
				throw new NoSuchElementException();
			}

			return gateways[i];
		}

		public Gateway destGateway() throws NoSuchElementException {
			if (i >= gateways.length - 1) {
				throw new NoSuchElementException();
			}

			return gateways[i + 1];
		}

		public int drawDepartureTurn(Random rg) throws NoSuchElementException {
			if (i >= gateways.length - 1) {
				throw new NoSuchElementException();
			}
			return (int) departureDists[i].draw(rg);
		}

		public void next() {
			i++;
		}

		public void rewind() {
			i = 0;
		}
	}

	public Color getDriverColor() {
		return driverColor;
	}

    public Color getEmergencyVehicleColor() {
        return emergencyVehicleColor;
    }

	private DriverZones getDriverZones() {
		if (parameters.isZoneInfoIncluded()) {
			if (driver_zones == null) {
				return DriverZones.DEFAULT_ZONE;
			} else {
				return driver_zones.next();
			}
		} else {
			return DriverZones.NO_ZONE;
		}
	}

	private Iterator<DriverZones> prepareDriverZones(Collection<Pair<String, Double>> zones) {
		if (zones.isEmpty()) {
			return null;
		} else {
			return new ZonePreparator(count, emergencyVehicles, zones).getIterator();
		}
	}
}

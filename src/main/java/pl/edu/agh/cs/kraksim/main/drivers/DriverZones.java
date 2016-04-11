package pl.edu.agh.cs.kraksim.main.drivers;

import com.google.common.collect.ImmutableList;

import java.util.Collection;

public class DriverZones {
	public static final DriverZones NO_ZONE = new DriverZones("NO_ZONE");
	public static final DriverZones DEFAULT_ZONE = new DriverZones("");

	private final Collection<String> zones;

	public DriverZones(String zones) {
		this.zones = ImmutableList.<String>builder().add("").add(zones.split(",")).build();
	}

	public boolean contains(String zoneName) {
		return this == NO_ZONE || zones.contains(zoneName);
	}
}

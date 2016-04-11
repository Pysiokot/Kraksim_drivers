package pl.edu.agh.cs.kraksim.core;

import java.awt.*;

public class ZoneInfo {
	private final String zoneName;
	private final Color zoneColor;

	public ZoneInfo(String zoneName, Color zoneColor) {
		this.zoneName = zoneName;
		this.zoneColor = zoneColor;
	}

	public String getZoneName() {
		return zoneName;
	}

	public Color getZoneColor() {
		return zoneColor;
	}
}

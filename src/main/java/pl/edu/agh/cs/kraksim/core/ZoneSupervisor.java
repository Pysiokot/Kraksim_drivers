package pl.edu.agh.cs.kraksim.core;

import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

import java.awt.*;
import java.util.Iterator;
import java.util.Map;

public class ZoneSupervisor {
	private static final Iterator<Color> COLORS = Iterators.cycle(
			new Color(173, 255, 47),
			new Color(135, 206, 235),
			new Color(255, 165, 0),
			new Color(255, 35, 0),
			new Color(222, 184, 135)
	);
	private final Map<String, ZoneInfo> zones;

	public ZoneSupervisor(){
		zones = Maps.newHashMap();
	}

	public ZoneInfo getZoneInfo(String zoneName){
		Color color = zoneName.isEmpty() ? Color.LIGHT_GRAY : COLORS.next();
		if(!zones.containsKey(zoneName)){
			zones.put(zoneName, new ZoneInfo(zoneName, color));
		}

		return zones.get(zoneName);
	}
}

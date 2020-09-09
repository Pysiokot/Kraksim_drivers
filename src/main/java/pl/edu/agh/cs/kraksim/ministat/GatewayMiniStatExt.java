package pl.edu.agh.cs.kraksim.ministat;

import org.apache.log4j.*;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import pl.edu.agh.cs.kraksim.KraksimConfigurator;
import pl.edu.agh.cs.kraksim.core.Gateway;
import pl.edu.agh.cs.kraksim.iface.Clock;
import pl.edu.agh.cs.kraksim.iface.mon.CarEntranceHandler;
import pl.edu.agh.cs.kraksim.iface.mon.CarExitHandler;
import pl.edu.agh.cs.kraksim.iface.mon.GatewayMonIface;
import pl.edu.agh.cs.kraksim.iface.mon.MonIView;
import pl.edu.agh.cs.kraksim.real_extended.Car;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class GatewayMiniStatExt {
	private static final Logger LOGGER = Logger.getLogger(GatewayMiniStatExt.class);
	private final Map<Gateway, RouteStat> routeStatMap;
	GatewayMonIface gateMon;

	GatewayMiniStatExt(final Gateway gateway, MonIView monView, final Clock clock, final StatHelper helper) {

		String dest_path=KraksimConfigurator.getProperty("out_dic_name")+"\\"+KraksimConfigurator.getProperty("map_name")+"\\"+KraksimConfigurator.getProperty("density")+"\\"+KraksimConfigurator.getProperty("gen_name")+"\\"+"logs";
		LOGGER.trace(dest_path);
		LOGGER.trace("for: " + gateway);
		routeStatMap = new HashMap<>();
		gateMon = monView.ext(gateway);
		gateMon.installEntranceSensor(new CarEntranceHandler() {
			public void handleCarEntrance(Object driver) {
				helper.beginTravel(driver, GatewayMiniStatExt.this, clock.getTurn());
			}
		});

		gateMon.installExitSensor(new CarExitHandler() {
			public void handleCarExit(Object driver) {
				helper.endTravel(driver, gateway, clock.getTurn());
			}
		});
	}
	
	public LinkedList<Car> getWaitingCars() {
		return gateMon.getEnteringCars();
	}

	void clear() {
		routeStatMap.clear();
	}

	void noteTravel(Gateway dest, int length, int duration) {

		LOGGER.trace("Trip: to=" + dest + ", len=" + length + ", dur=" + duration);

		RouteStat rs = getRouteStatForGateway(dest);
		rs.noteTravel(length, duration);
		updateRouteStat(dest, rs);
	}

	private RouteStat getRouteStatForGateway(Gateway dest) {
		RouteStat rs = routeStatMap.get(dest);
		if (rs == null) {
			rs = new RouteStat();
		}
		return rs;
	}

	private void updateRouteStat(Gateway dest, RouteStat rs) {
		routeStatMap.put(dest, rs);
	}

	public RouteStat getRouteStat(Gateway dest) {
		LOGGER.trace("to=" + dest);
		return routeStatMap.get(dest);
	}
}

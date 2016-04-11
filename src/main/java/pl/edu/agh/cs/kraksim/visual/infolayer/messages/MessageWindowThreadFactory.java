package pl.edu.agh.cs.kraksim.visual.infolayer.messages;

import pl.edu.agh.cs.kraksim.core.Gateway;
import pl.edu.agh.cs.kraksim.core.Intersection;
import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.ministat.GatewayMiniStatExt;
import pl.edu.agh.cs.kraksim.ministat.LinkMiniStatExt;

import java.awt.*;

public class MessageWindowThreadFactory {

	public static Thread create(Link link, LinkMiniStatExt miniStat, Point p) {
		return new LinkMessageWindowThread(link, miniStat, p);
	}

	public static Thread create(Intersection intersection, Point p) {
		return new NodeMessageWindowThread(intersection, p);
	}

	public static Thread create(Gateway gateway, GatewayMiniStatExt gatewayMiniStatExt, Point p) {
		return new GatewayMessageWindowThread(gateway, gatewayMiniStatExt, p);
	}
}

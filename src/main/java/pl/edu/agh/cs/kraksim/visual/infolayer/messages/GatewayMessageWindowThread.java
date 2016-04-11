package pl.edu.agh.cs.kraksim.visual.infolayer.messages;

import pl.edu.agh.cs.kraksim.core.Gateway;
import pl.edu.agh.cs.kraksim.ministat.GatewayMiniStatExt;

import java.awt.*;

public class GatewayMessageWindowThread extends Thread {
	private final Point point;
	private final Gateway gateway;
	private final GatewayMiniStatExt gatewayMiniStatExt;

	public GatewayMessageWindowThread(Gateway gateway, GatewayMiniStatExt gatewayMiniStatExt, Point p) {
		this.gateway = gateway;
		this.gatewayMiniStatExt = gatewayMiniStatExt;
		point = p;
	}

	@Override
	public void run() {
		MessageWindow messageWindow = new MessageWindow(gateway, gatewayMiniStatExt, point);
		messageWindow.setVisible(true);

		super.run();
	}
}

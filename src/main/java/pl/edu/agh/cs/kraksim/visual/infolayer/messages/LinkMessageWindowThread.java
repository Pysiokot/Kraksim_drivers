package pl.edu.agh.cs.kraksim.visual.infolayer.messages;

import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.ministat.LinkMiniStatExt;

import java.awt.*;

/**
 * wątek do wyświetlania okna z informacjami
 *
 * @author borowski
 */
public class LinkMessageWindowThread extends Thread {
	private final Link link;
	private final Point point;
	private final LinkMiniStatExt miniStat;

	public LinkMessageWindowThread(Link link, LinkMiniStatExt miniStat, Point p) {
		this.link = link;
		this.miniStat = miniStat;
		point = p;
	}

	@Override
	public void run() {
		MessageWindow messageWindow = new MessageWindow(link, miniStat, point);
		messageWindow.setVisible(true);

		super.run();
	}
}

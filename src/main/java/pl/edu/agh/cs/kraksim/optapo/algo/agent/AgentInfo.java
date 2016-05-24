package pl.edu.agh.cs.kraksim.optapo.algo.agent;

import org.apache.log4j.Logger;

public class AgentInfo {
	private static final Logger LOGGER = Logger.getLogger(AgentInfo.class);
	private final String name;
	private Direction dir;
	private int incoming;

	public AgentInfo(String name, Direction d, int i) {
		LOGGER.trace("AgentINFO " + name + ' ' + d + ' ' + incoming);
		this.name = name;
		dir = d;
		incoming = i;
	}

	public Direction getDir() {
		return dir;
	}

	public void setDir(Direction dir) {
		this.dir = dir;
	}

	public int getIncoming() {
		return incoming;
	}

	public void setIncoming(int incoming) {
		this.incoming = incoming;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "AgentInfo=(" + name + ", " + dir + ", " + incoming + ')';
	}
}

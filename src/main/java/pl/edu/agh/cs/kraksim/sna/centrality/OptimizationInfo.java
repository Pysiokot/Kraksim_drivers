package pl.edu.agh.cs.kraksim.sna.centrality;

import pl.edu.agh.cs.kraksim.core.Intersection;
import pl.edu.agh.cs.kraksim.core.Link;

import java.util.Map;

/**
 * Klasa przechowujaca informacje dotyczace  preferencji zmian swiatel dla skrzyzowania
 */
public class OptimizationInfo {
	private Map<Link, Integer> greenLightChanges;
	private Link link;
	private int change;
	private Intersection intersection;

	public Link getLink() {
		return link;
	}

	public void setLink(Link link) {
		this.link = link;
	}

	public int getChange() {
		return change;
	}

	public void setChange(int change) {
		this.change = change;
	}

	public Map<Link, Integer> getGreenLightChanges() {
		return greenLightChanges;
	}

	public void setGreenLightChanges(Map<Link, Integer> greenLightChanges) {
		this.greenLightChanges = greenLightChanges;
	}

	public Intersection getIntersection() {
		return intersection;
	}

	public void setIntersection(Intersection intersection) {
		this.intersection = intersection;
	}
}

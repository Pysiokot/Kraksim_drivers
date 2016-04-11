package pl.edu.agh.cs.kraksim.real;

import pl.edu.agh.cs.kraksim.AssumptionNotSatisfiedException;
import pl.edu.agh.cs.kraksim.KraksimException;
import pl.edu.agh.cs.kraksim.core.*;
import pl.edu.agh.cs.kraksim.core.exceptions.ExtensionCreationException;

public class RealModuleCreator extends ModuleCreator<CityRealExt, NodeRealExt, GatewayRealExt, IntersectionRealExt, LinkRealExt, LaneRealExt> {
	private final RealSimulationParams params;
	private RealEView ev;

	public RealModuleCreator(RealSimulationParams params) {
		this.params = params;
	}

	@Override
	public void setModule(Module module) {
		try {
			ev = new RealEView(module);
		} catch (KraksimException e) {
			throw new AssumptionNotSatisfiedException(e);
		}
	}

	@Override
	public CityRealExt createCityExtension(City city) throws ExtensionCreationException {
		return new CityRealExt(city, ev);
	}

	@Override
	public GatewayRealExt createGatewayExtension(Gateway gateway) throws ExtensionCreationException {
		return new GatewayRealExt(gateway, ev);
	}

	@Override
	public IntersectionRealExt createIntersectionExtension(Intersection intersection) throws ExtensionCreationException {
		return new IntersectionRealExt(intersection, ev);
	}

	@Override
	public LinkRealExt createLinkExtension(Link link) throws ExtensionCreationException {
		return new LinkRealExt(link, ev, params);
	}

	@Override
	public LaneRealExt createLaneExtension(Lane lane) throws ExtensionCreationException {
		return new LaneRealExt(lane, ev, params);
	}
}

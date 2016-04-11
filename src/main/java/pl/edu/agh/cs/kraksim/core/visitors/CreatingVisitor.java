package pl.edu.agh.cs.kraksim.core.visitors;

import pl.edu.agh.cs.kraksim.KraksimException;
import pl.edu.agh.cs.kraksim.core.*;

public class CreatingVisitor implements ElementVisitor {
	private final ModuleCreator creator;
	private final Module module;

	public CreatingVisitor(Module module, ModuleCreator creator) {
		this.module = module;
		this.creator = creator;
	}

	public void visit(City city) throws VisitingException {
		try {
			city.setExtension(module, creator.createCityExtension(city));
		} catch (KraksimException e) {
			throw new VisitingException(e);
		}
	}

	public void visit(Gateway gateway) throws VisitingException {
		try {
			gateway.setExtension(module, creator.createGatewayExtension(gateway));
		} catch (KraksimException e) {
			throw new VisitingException(e);
		}
	}

	public void visit(Intersection intersection) throws VisitingException {
		try {
			intersection.setExtension(module, creator.createIntersectionExtension(intersection));
		} catch (KraksimException e) {
			throw new VisitingException(e);
		}
	}

	public void visit(Link link) throws VisitingException {
		try {
			link.setExtension(module, creator.createLinkExtension(link));
		} catch (KraksimException e) {
			throw new VisitingException(e);
		}
	}

	public void visit(Lane lane) throws VisitingException {
		try {
			lane.setExtension(module, creator.createLaneExtension(lane));
		} catch (KraksimException e) {
			throw new VisitingException(e);
		}
	}
}

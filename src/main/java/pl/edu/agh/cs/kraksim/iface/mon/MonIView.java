package pl.edu.agh.cs.kraksim.iface.mon;

import pl.edu.agh.cs.kraksim.core.Module;
import pl.edu.agh.cs.kraksim.core.ModuleView;
import pl.edu.agh.cs.kraksim.core.NULL;
import pl.edu.agh.cs.kraksim.core.exceptions.InvalidClassSetDefException;
import pl.edu.agh.cs.kraksim.core.exceptions.UnsatisfiedContractException;

public class MonIView extends ModuleView<NULL, NULL, GatewayMonIface, NULL, LinkMonIface, LaneMonIface> {

	public MonIView(Module module) throws InvalidClassSetDefException, UnsatisfiedContractException {
		super(module);
	}
}

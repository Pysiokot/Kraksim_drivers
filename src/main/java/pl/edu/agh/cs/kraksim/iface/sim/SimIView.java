package pl.edu.agh.cs.kraksim.iface.sim;

import pl.edu.agh.cs.kraksim.core.Module;
import pl.edu.agh.cs.kraksim.core.ModuleView;
import pl.edu.agh.cs.kraksim.core.NULL;
import pl.edu.agh.cs.kraksim.core.exceptions.InvalidClassSetDefException;
import pl.edu.agh.cs.kraksim.core.exceptions.UnsatisfiedContractException;

public class SimIView extends ModuleView<CitySimIface, NULL, GatewaySimIface, NULL, NULL, NULL> {

	public SimIView(Module module) throws UnsatisfiedContractException, InvalidClassSetDefException {
		super(module);
	}
}

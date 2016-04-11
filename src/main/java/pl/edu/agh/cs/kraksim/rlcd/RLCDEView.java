package pl.edu.agh.cs.kraksim.rlcd;

import pl.edu.agh.cs.kraksim.core.Module;
import pl.edu.agh.cs.kraksim.core.ModuleView;
import pl.edu.agh.cs.kraksim.core.NULL;
import pl.edu.agh.cs.kraksim.core.exceptions.InvalidClassSetDefException;
import pl.edu.agh.cs.kraksim.core.exceptions.UnsatisfiedContractException;

public class RLCDEView extends ModuleView<CityRLCDExt, NULL, NULL, NULL, NULL, LaneRLCDExt> {
	protected RLCDEView(Module module) throws InvalidClassSetDefException, UnsatisfiedContractException {
		super(module);
	}
}

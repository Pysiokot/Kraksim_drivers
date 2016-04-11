package pl.edu.agh.cs.kraksim.dsyncdecision;

import pl.edu.agh.cs.kraksim.core.Module;
import pl.edu.agh.cs.kraksim.core.ModuleView;
import pl.edu.agh.cs.kraksim.core.NULL;
import pl.edu.agh.cs.kraksim.core.exceptions.InvalidClassSetDefException;
import pl.edu.agh.cs.kraksim.core.exceptions.UnsatisfiedContractException;

class DSyncDecisionEView extends ModuleView<CityDsyncDecisionExt, NULL, NULL, IntersectionDsyncDecisionExt, NULL, NULL> {

	DSyncDecisionEView(Module module) throws InvalidClassSetDefException, UnsatisfiedContractException {
		super(module);
	}
}

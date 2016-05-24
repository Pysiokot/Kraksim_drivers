package pl.edu.agh.cs.kraksim.main;

import java.util.Iterator;

import pl.edu.agh.cs.kraksim.core.Core;
import pl.edu.agh.cs.kraksim.core.Module;
import pl.edu.agh.cs.kraksim.core.exceptions.InvalidClassSetDefException;
import pl.edu.agh.cs.kraksim.core.exceptions.ModuleCreationException;
import pl.edu.agh.cs.kraksim.iface.block.BlockIView;
import pl.edu.agh.cs.kraksim.iface.carinfo.CarInfoIView;
import pl.edu.agh.cs.kraksim.iface.mon.MonIView;
import pl.edu.agh.cs.kraksim.rlcd.RLCDModuleCreator;
import pl.edu.agh.cs.kraksim.rlcd.RLCDParams;
import pl.edu.agh.cs.kraksim.util.ArrayIterator;

public class RLCDModuleProvider implements EvalModuleProvider {
	static final float DEFAULT_DISCOUNT = 0.9f;
	static final int DEFAULT_HALVE_PERIOD = 60;

	private float discount = DEFAULT_DISCOUNT;
	private int halvePeriod = DEFAULT_HALVE_PERIOD;

	private static final KeyValPair[] PARAM_DESC = new KeyValPair[] {
		    new KeyValPair("discount", "discount factor - gamma in RL equations (default: "
		    		+ DEFAULT_DISCOUNT + ')'),
		    new KeyValPair("halve", "how often halving RL counters takes place; nonpositive value - never halve (default: " 
		    		+ DEFAULT_HALVE_PERIOD + ')') };

	public String getAlgorithmCode() {
		return "rlcd";
	}

	public String getAlgorithmName() {
		return "Reinforcement Learning with Context Detection";
	}

	public Iterator<KeyValPair> getParamsDescription() {
		return new ArrayIterator<>(PARAM_DESC);
	}

	public void setParam(String key, String val) throws AlgorithmConfigurationException {
		switch (key) {
			case "discount":
				try {
					float d = Float.parseFloat(val);
					if (d <= 0.0f || d >= 1.0f) {
						throw new NumberFormatException();
					}
					discount = d;
				} catch (NumberFormatException e) {
					throw new AlgorithmConfigurationException("discount must be a float in range (0,1)", e);
				}
				break;
			case "halve":
				try {
					halvePeriod = Integer.parseInt(val);
				} catch (NumberFormatException e) {
					throw new AlgorithmConfigurationException("halve period must be an integer", e);
				}
				break;
			default:
				throw new AlgorithmConfigurationException("invalid algorithm parameter -- " + key);
		}
	}

	public Module provideNew(String name, Core core, CarInfoIView carInfoView, MonIView monView, BlockIView blockView, int carStartDelay, int carMaxVelocity) throws InvalidClassSetDefException, ModuleCreationException {
		return core.newModule(name, new RLCDModuleCreator(carInfoView, blockView, new RLCDParams(discount, halvePeriod, carStartDelay, carMaxVelocity)));
	}
}

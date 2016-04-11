package pl.edu.agh.cs.kraksim.main;

import java.util.Iterator;

import pl.edu.agh.cs.kraksim.core.Core;
import pl.edu.agh.cs.kraksim.core.Module;
import pl.edu.agh.cs.kraksim.core.exceptions.InvalidClassSetDefException;
import pl.edu.agh.cs.kraksim.core.exceptions.ModuleCreationException;
import pl.edu.agh.cs.kraksim.iface.block.BlockIView;
import pl.edu.agh.cs.kraksim.iface.carinfo.CarInfoIView;
import pl.edu.agh.cs.kraksim.iface.mon.MonIView;

public interface EvalModuleProvider
{

  String getAlgorithmCode();

  String getAlgorithmName();

  public String toString();

  Iterator<KeyValPair> getParamsDescription();

  void setParam(String key, String val) throws AlgorithmConfigurationException;

  Module provideNew(String name,
      Core core,
      CarInfoIView carInfoView,
      MonIView monView,
      BlockIView blockView,
      int carStartDelay,
      int carMaxVelocity) throws InvalidClassSetDefException, ModuleCreationException;


}

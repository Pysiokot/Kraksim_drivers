package pl.edu.agh.cs.kraksim.main;

import java.util.Iterator;

import pl.edu.agh.cs.kraksim.core.Core;
import pl.edu.agh.cs.kraksim.core.Module;
import pl.edu.agh.cs.kraksim.core.exceptions.InvalidClassSetDefException;
import pl.edu.agh.cs.kraksim.core.exceptions.ModuleCreationException;
import pl.edu.agh.cs.kraksim.iface.block.BlockIView;
import pl.edu.agh.cs.kraksim.iface.carinfo.CarInfoIView;
import pl.edu.agh.cs.kraksim.iface.mon.MonIView;
import pl.edu.agh.cs.kraksim.sotl.SOTLModuleCreator;
import pl.edu.agh.cs.kraksim.sotl.SOTLParams;
import pl.edu.agh.cs.kraksim.util.ArrayIterator;

class SOTLModuleProvider implements EvalModuleProvider
{

  final static int            DEFAULT_ZONE_LENGTH = 18;
  private int                 zoneLength          = DEFAULT_ZONE_LENGTH;

  private static KeyValPair[] paramDesc           = new KeyValPair[] {
                                                    new KeyValPair(
                                                        "zone",
                                                        "length of metering zone (default: "
                                                            + DEFAULT_ZONE_LENGTH + ")" ) };
    private SOTLParams sotlParams;

    public String getAlgorithmCode() {
    return "sotl";
  }

  public String getAlgorithmName() {
    return "Self Organizing Traffic Lights";
  }

  public Iterator<KeyValPair> getParamsDescription() {
    return new ArrayIterator<>(paramDesc);
  }

  public void setParam(String key, String val) throws AlgorithmConfigurationException {
    if ( key.equals( "zone" ) ) {
      try {
        int z = Integer.parseInt( val );
        if ( z < 1 ) throw new NumberFormatException();
        zoneLength = z;
      }
      catch (NumberFormatException e) {
        throw new AlgorithmConfigurationException( "zone length must be positive" );
      }
    }
    else throw new AlgorithmConfigurationException( "invalid algorithm parameter -- " + key );
  }

  public Module provideNew(String name,
      Core core,
      CarInfoIView carInfoView,
      MonIView monView,
      BlockIView blockView,
      int carStartDelay,
      int carMaxVelocity) throws InvalidClassSetDefException, ModuleCreationException
  {
      sotlParams = new SOTLParams(
              zoneLength, carStartDelay, carMaxVelocity);
      return core.newModule( name, new SOTLModuleCreator( monView, blockView, sotlParams) );
  }

    @Override
    public String toString() {
        return sotlParams.toString();
    }
}

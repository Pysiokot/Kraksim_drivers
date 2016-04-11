package pl.edu.agh.cs.kraksim.sotl;

public class SOTLParams
{

  /* length of the lane segment where counting cars takes place */
  final int  zoneLength;
  /* number of turns between starts of two cars standing in the queue */
  final int  carStartDelay;
  /* maximum velocity of a car (in cells per turn) */
  final int  carMaxVelocity;
  final static int  minimumGreen = 5;
  public int threshold;

  public SOTLParams(int zoneLength, int carStartDelay, int carMaxVelocity) {
    this.zoneLength = zoneLength;
    this.carStartDelay = carStartDelay;
    this.carMaxVelocity = carMaxVelocity;
    this.threshold = zoneLength - 5;
  }

  public String toString(){
      StringBuffer params_desc = new StringBuffer("SOTL : ");
      params_desc.append("Zone Length : " + zoneLength + "  ");
      params_desc.append("Car start delay : " + carStartDelay + "  ");
      params_desc.append("Car max velocity : " + carMaxVelocity + "  ");
      params_desc.append("Minimum green : " + minimumGreen + "  ");
      params_desc.append("Threshold : " + threshold + "  ");
      return params_desc.toString();
  }
}

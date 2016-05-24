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
      return "SOTL : " + "Zone Length : " + zoneLength + "  " +
              "Car start delay : " + carStartDelay + "  " +
              "Car max velocity : " + carMaxVelocity + "  " +
              "Minimum green : " + minimumGreen + "  " +
              "Threshold : " + threshold + "  ";
  }
}

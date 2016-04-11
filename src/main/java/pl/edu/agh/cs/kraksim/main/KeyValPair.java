package pl.edu.agh.cs.kraksim.main;

public class KeyValPair
{
  private String key;

  private String val;

  KeyValPair(String key, String val) {
    this.key = key;
    this.val = val;
  }

  public String getKey() {
    return key;
  }

  public String getVal() {
    return val;
  }
}

package pac1.Bean;

import java.io.Serializable;

public class NetCloudDTO implements Serializable
{
  private static String className = "NetCloudDTO";
  private String generatorName = null;
  private String generatorID = null;
  private String generatorTRNumber = null;
  
  public NetCloudDTO()
  {
  }
  
  public String getGeneratorName() {
    return generatorName;
  }

  public void setGeneratorName(String generatorName) {
    this.generatorName = generatorName;
  }

  public String getGeneratorID() {
    return generatorID;
  }

  public void setGeneratorID(String generatorID) {
    this.generatorID = generatorID;
  }

  public String getGeneratorTRNumber() {
    return generatorTRNumber;
  }

  public void setGeneratorTRNumber(String generatorTRNumber) {
    this.generatorTRNumber = generatorTRNumber;
  }

}

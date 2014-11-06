package pac1.Bean;

import java.io.Serializable;

public class UpdateTestRunDataForUserProp implements Serializable
{
  private String reqDataLine = null;
  
  public void setRequestDataLine(String dataLine)
  {
    this.reqDataLine = dataLine;
  }
  
  public String getRequestDataLine()
  {
    return reqDataLine;
  }
  
  public static void main(String[] args)
  {
  }

}

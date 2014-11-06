package pac1.Bean;

import pac1.Bean.Log;

/**
 * Name : PanelZoomDTO.java
 * 
 * Author : Ravi Kant Sharma
 * 
 * Purpose : To Remove Code Complexity and Code Simplification. This class is used for Zoom in dashboard GUI.
 * 
 * Modification History:
 * 
 * 28/03/2014 - Initial Version
 * 
 */

public class PanelZoomDTO
{
  private static String className = "PanelZoomDTO";
  private String[] tbtrdKeyArray = null;
  private int[][] graphNumbersArray = null;
  private String startTime = "NA";
  private String endTime = "NA";
  private long progressInterval = 10000;
  private int testRunNumber = -1;

  public int getTestRunNumber()
  {
    return testRunNumber;
  }

  public void setTestRunNumber(int testRunNumber)
  {
    this.testRunNumber = testRunNumber;
  }

  public long getProgressInterval()
  {
    return progressInterval;
  }

  public void setProgressInterval(long progressInterval)
  {
    this.progressInterval = progressInterval;
  }

  public String getStartTime()
  {
    return startTime;
  }

  public void setStartTime(String startTime)
  {
    this.startTime = startTime;
  }

  public String getEndTime()
  {
    return endTime;
  }

  public void setEndTime(String endTime)
  {
    this.endTime = endTime;
  }

  public PanelZoomDTO()
  {
    Log.debugLogAlways(className, "PanelZoomDTO", "", "", "Default Constructor Called.");
  }

  public String[] getTbtrdKeyArray()
  {
    return tbtrdKeyArray;
  }

  public void setTbtrdKeyArray(String[] tbtrdKeyArray)
  {
    this.tbtrdKeyArray = tbtrdKeyArray;
  }

  public int[][] getGraphNumbersArray()
  {
    return graphNumbersArray;
  }

  public void setGraphNumbersArray(int[][] graphNumbersArray)
  {
    this.graphNumbersArray = graphNumbersArray;
  }
}

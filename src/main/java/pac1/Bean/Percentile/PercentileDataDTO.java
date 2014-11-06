/**
 * This class is for Storing Percentile Data 
 * @author Ravi Kant Sharma
 * @since Netsorm Version 4.0.0
 * @Modification_History Ravi Kant Sharma - Initial Version 4.0.0
 * @version 4.0.0
 * 
 */
package pac1.Bean.Percentile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import pac1.Bean.Log;

public class PercentileDataDTO implements Serializable
{
  private static final long serialVersionUID = 6572345367906751766L;
  private String className = "PercentileDataDTO";

  /**
   * This is for each percentile panel, It store percentile panel information
   * 
   * e.g.
   * 
   * 1. String DataKey (Whole Scenario, Last N Hours etc)
   * 
   * 2. GraphUniqueKeyDTO[] arrGraphUniqueKeyDTO
   * 
   * and many more panel info. For more details see {@link PanelDataInfo}
   * 
   */
  private ArrayList<PanelDataInfo> panelDataInfoList = new ArrayList<PanelDataInfo>();

  /**
   * Variable for Test Run Number
   * 
   */
  private int testRunNumber = -1;

  /**
   * flag for test is partitioning enable or not.
   * 
   */
  private boolean isPartitionModeEnabled = false;

  /**
   * flag to show status is percentile data whether generated or not
   * 
   */
  private boolean successFlag = false;

  /**
   * if successFlag is true then keep error message to display at client.
   * 
   */
  private StringBuffer errMsg = null;

  /**
   * This map is used to store the generated percentile data
   */
  private HashMap<PercentileDataKey, PercentileInfo> percentileDataMap = new HashMap<PercentileDataKey, PercentileInfo>();

  /**
   * GraphType 0 - PERCENTILE, 1 - SLAB COUNT, 2 - Frequency Distribution
   * 
   */
  private byte graphType = -1;

  /**
   * On the basis of Debug Level, Need to write debug logs
   */
  private int debugLevel = 0;

  /**
   * This variable is used for monitoring the time, Means how much time taken by the method
   */
  private int profileMode = 0;

  public int getProfileMode()
  {
    return profileMode;
  }

  public void setProfileMode(int profileMode)
  {
    this.profileMode = profileMode;
  }

  public int getDebugLevel()
  {
    return debugLevel;
  }

  public void setDebugLevel(int debugLevel)
  {
    this.debugLevel = debugLevel;
  }

  public byte getGraphType()
  {
    return graphType;
  }

  public void setGraphType(byte graphType)
  {
    this.graphType = graphType;
  }

  public PercentileDataDTO(int testRunNumber, ArrayList<PanelDataInfo> panelDataInfoList, int debugLevel)
  {
    try
    {
      this.debugLevel = debugLevel;

      if (debugLevel > 0)
        Log.debugLogAlways(className, "PercentileDataDTO", "", "", "Method Called. ");

      this.panelDataInfoList = panelDataInfoList;
      this.testRunNumber = testRunNumber;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "PercentileDataDTO", "", "", "Exception - ", e);
    }
  }

  public ArrayList<PanelDataInfo> getPanelDataInfoList()
  {
    return panelDataInfoList;
  }

  public void setPanelDataInfoList(ArrayList<PanelDataInfo> panelDataInfoList)
  {
    this.panelDataInfoList = panelDataInfoList;
  }

  public void addPanelDataInfo(PanelDataInfo panelDataInfo)
  {
    if (panelDataInfoList == null)
      panelDataInfoList = new ArrayList<PanelDataInfo>();

    panelDataInfoList.add(panelDataInfo);
  }

  public StringBuffer getErrMsg()
  {
    return this.errMsg;
  }

  public void setErrMsg(StringBuffer errMsg)
  {
    this.errMsg = errMsg;
  }

  public int getTestRunNumber()
  {
    return testRunNumber;
  }

  public void setTestRunNumber(int testRunNumber)
  {
    this.testRunNumber = testRunNumber;
  }

  public boolean isPartitionModeEnabled()
  {
    return isPartitionModeEnabled;
  }

  public void setPartitionModeEnabled(boolean isPartitionModeEnabled)
  {
    this.isPartitionModeEnabled = isPartitionModeEnabled;
  }

  public boolean isSuccessFlag()
  {
    return successFlag;
  }

  public void setSuccessFlag(boolean successFlag)
  {
    this.successFlag = successFlag;
  }

  public HashMap<PercentileDataKey, PercentileInfo> getPercentileDataMap()
  {
    return percentileDataMap;
  }

  public void setPercentileDataMap(HashMap<PercentileDataKey, PercentileInfo> percentileDataMap)
  {
    this.percentileDataMap = percentileDataMap;
  }

  /**
   * This method add percentile data for percentile data key
   * 
   * @param percentileDataKey
   * @param percentileInfo
   */
  public void addPercentileDataInMap(PercentileDataKey percentileDataKey, PercentileInfo percentileInfo)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "addPercentileDataInMap", "", "", "Adding percentile data for percentileDataKey = " + percentileDataKey);

      if (!percentileDataMap.containsKey(percentileDataKey))
        percentileDataMap.put(percentileDataKey, percentileInfo);
      else
        Log.errorLog(className, "addPercentileDataInMap", "", "", "percentileDataKey = " + percentileDataKey + " is already available.");
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "addPercentileDataInMap", "", "", "Exception - ", e);
    }
  }

  @Override
  public String toString()
  {
    return "TR = " + testRunNumber + ", Partition = " + isPartitionModeEnabled + ", successFlag = " + successFlag + ", graph Type = " + graphType + ", panelDataInfoList = " + panelDataInfoList;
  }
}

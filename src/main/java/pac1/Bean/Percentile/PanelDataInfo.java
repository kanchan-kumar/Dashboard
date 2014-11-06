/**
 * This class is for Storing Panel Information
 * @author Ravi Kant Sharma
 * @since Netsorm Version 4.0.0
 * @Modification_History Ravi Kant Sharma - Initial Version 4.0.0
 * @version 4.0.0
 * 
 */
package pac1.Bean.Percentile;

import java.io.Serializable;
import java.util.ArrayList;

import pac1.Bean.GraphUniqueKeyDTO;
import pac1.Bean.Log;
import pac1.Bean.TestRunDataType;

public class PanelDataInfo implements Serializable
{
  private static final long serialVersionUID = 1358656713641553342L;

  // This flag is used, as we dont have test run start time stamp. So need to get at bean.
  private boolean isBaseline = false;

  /**
   * dataKey is for storing panel data key
   * 
   * e.g. Whole Scenario, Last N Hours, Specified Time
   */
  private String dataKey = null;

  private String uniqueKey = null;
  /**
   * testRunDataType object is for storing time range
   */
  private TestRunDataType testRunDataType = null;

  /**
   * List of Non Derived Graphs for generating percentile data
   */
  private ArrayList<GraphUniqueKeyDTO> graphUniqueKeyList = new ArrayList<GraphUniqueKeyDTO>();

  /**
   * List of derived expression
   */
  private ArrayList<String> derivedExpList = new ArrayList<String>();

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
   * Variable for reading data from (Data Reading Start point) in Elapsed Format for NS/NDE/CM Mode
   */
  private String startTime = "NA";

  private long testRunStartTimeStamp = 0;

  /**
   * Variable for reading data to (Data Reading End point) in Elapsed Format for NS/NDE/CM Mode
   */
  private String endTime = "NA";

  /**
   * phaseName is the variable is used for phase name.
   * 
   * If phase name is available then no need to read start and end time as
   * 
   * ReportData.java automatically calculate start and end time
   */
  private String phaseName = null;

  private int debugLevel = 0;

  private String className = "PanelDataInfo";

  public PanelDataInfo(int debugLevel)
  {
    this.debugLevel = debugLevel;
  }

  public int getDebugLevel()
  {
    return debugLevel;
  }

  public void setDebugLevel(int debugLevel)
  {
    this.debugLevel = debugLevel;
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

  public boolean isSuccessFlag()
  {
    return successFlag;
  }

  public void setSuccessFlag(boolean successFlag)
  {
    this.successFlag = successFlag;
  }

  public StringBuffer getErrMsg()
  {
    return errMsg;
  }

  public void setErrMsg(StringBuffer errMsg)
  {
    this.errMsg = errMsg;
  }

  public String getPhaseName()
  {
    return phaseName;
  }

  public void setPhaseName(String phaseName)
  {
    this.phaseName = phaseName;
  }

  public ArrayList<String> getDerivedExpList()
  {
    return derivedExpList;
  }

  public void setDerivedExpList(ArrayList<String> derivedExpList)
  {
    this.derivedExpList = derivedExpList;
  }

  public String getDataKey()
  {
    return dataKey;
  }

  public void setDataKey(String dataKey)
  {
    this.dataKey = dataKey;
  }

  public TestRunDataType getTestRunDataType()
  {
    return testRunDataType;
  }

  public void setTestRunDataType(TestRunDataType testRunDataType)
  {
    this.testRunDataType = testRunDataType;
  }

  public void setAllParams(ArrayList<GraphUniqueKeyDTO> graphUniqueKeyList, ArrayList<String> derivedExpList, String startTime, String endTime, String phaseName, TestRunDataType testRunDataTypeObj, String dataKey, String uniqueKey, long testRunStartTimeStamp)
  {
    if (debugLevel > 0)
      Log.debugLogAlways(className, "setAllParams", "", "", "graphUniqueKeyList = " + graphUniqueKeyList + ", derivedExpList = " + derivedExpList + ", startTime = " + startTime + ", endTime = " + endTime + ", phaseName = " + phaseName + ", dataKey = " + dataKey + ", testRunDataTypeObj = " + testRunDataTypeObj + ", uniqueKey = " + uniqueKey);

    this.testRunStartTimeStamp = testRunStartTimeStamp;
    this.graphUniqueKeyList = graphUniqueKeyList;
    this.derivedExpList = derivedExpList;
    this.startTime = startTime;
    this.endTime = endTime;
    this.phaseName = phaseName;
    this.testRunDataType = testRunDataTypeObj;
    this.dataKey = dataKey;
    this.uniqueKey = uniqueKey;
  }

  public String getUniqueKey()
  {
    return uniqueKey;
  }

  public void setUniqueKey(String uniqueKey)
  {
    this.uniqueKey = uniqueKey;
  }

  public ArrayList<GraphUniqueKeyDTO> getGraphUniqueKeyList()
  {
    return graphUniqueKeyList;
  }

  public void setGraphUniqueKeyList(ArrayList<GraphUniqueKeyDTO> graphUniqueKeyList)
  {
    this.graphUniqueKeyList = graphUniqueKeyList;
  }

  public long getTestRunStartTimeStamp()
  {
    return testRunStartTimeStamp;
  }

  public void setTestRunStartTimeStamp(long testRunStartTimeStamp)
  {
    this.testRunStartTimeStamp = testRunStartTimeStamp;
  }

  public boolean isBaseline()
  {
    return isBaseline;
  }

  public void setBaseline(boolean isBaseline)
  {
    this.isBaseline = isBaseline;
  }

  public String toString()
  {
    return "DataKey = " + dataKey + ", NonDerived = " + graphUniqueKeyList + ", Derived = " + derivedExpList + ", Start Time = " + startTime + ", End Time = " + endTime + ", Phase Name = " + phaseName + ", testRunDataType = " + testRunDataType + ", testRunStartTimeStamp = " + testRunStartTimeStamp + ", successFlag = " + successFlag;
  }
}

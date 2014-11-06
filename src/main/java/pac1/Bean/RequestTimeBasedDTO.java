package pac1.Bean;

import java.io.Serializable;

import pac1.Bean.GraphName.GraphNames;

public class RequestTimeBasedDTO implements Serializable
{
  private static final long serialVersionUID = 3100844720472549919L;
  private String className = "RequestTimeBasedDTO";
  private boolean status = true;
  private StringBuffer errMsg = new StringBuffer();
  private TestRunData testRunData = null;
  private RequestGraphsDTO[] reqGraphDto = null;
  private int granularity;
  private int testRunDataType;
  private boolean isAutoActivate;
  private int numberOfPanels = 9;
  private String userName;
  private transient GraphNames graphNamesObj;
  private String startTime = "NA";
  private String endTime = "NA";
  private String phaseName = "NA";
  private String interval = "NA";
  private boolean testViewMode;
  private int testRun = -1;
  private TestRunDataType testRunDataTypeObj = null;

  public String toString()
  {
    return "Test Run = " + testRun + ", Status = " + status + ", errMsg = " + errMsg.toString() + ", testRunData = " + testRunData + ", reqGraphDto = " + reqGraphDto + ", granularity = " + granularity + ", testRunDataType = " + testRunDataType + ", isAutoActivate = " + isAutoActivate + ", numberOfPanels = " + numberOfPanels + ", User Name = " + userName + ", startTime = " + startTime + ", endTime = " + endTime + ", phaseName = " + phaseName + ", interval = " + interval + ", View Mode = " + testViewMode;
  }

  public void setTestRun(int testRun)
  {
    this.testRun = testRun;
  }

  public int getTestRun()
  {
    return testRun;
  }

  public boolean isStatus()
  {
    return status;
  }

  public void setStatus(boolean status)
  {
    this.status = status;
  }

  public StringBuffer getErrMsg()
  {
    return errMsg;
  }

  public void setErrMsg(StringBuffer errMsg)
  {
    this.errMsg = errMsg;
  }

  public TestRunData getTestRunData()
  {
    return testRunData;
  }

  public void setTestRunData(TestRunData testRunData)
  {
    this.testRunData = testRunData;
  }

  public RequestGraphsDTO[] getReqGraphDto()
  {
    return reqGraphDto;
  }

  public void setReqGraphDto(RequestGraphsDTO[] reqGraphDto)
  {
    this.reqGraphDto = reqGraphDto;
  }

  public int getGranularity()
  {
    return granularity;
  }

  public void setGranularity(int granularity)
  {
    this.granularity = granularity;
  }

  public int getTestRunDataType()
  {
    return testRunDataType;
  }

  public void setTestRunDataType(int testRunDataType)
  {
    this.testRunDataType = testRunDataType;
  }

  public boolean isAutoActivate()
  {
    return isAutoActivate;
  }

  public void setAutoActivate(boolean isAutoActivate)
  {
    this.isAutoActivate = isAutoActivate;
  }

  public int getNumberOfPanels()
  {
    return numberOfPanels;
  }

  public void setNumberOfPanels(int numberOfPanels)
  {
    this.numberOfPanels = numberOfPanels;
  }

  public String getUserName()
  {
    return userName;
  }

  public void setUserName(String userName)
  {
    this.userName = userName;
  }

  public GraphNames getGraphNamesObj()
  {
    return graphNamesObj;
  }

  public void setGraphNamesObj(GraphNames graphNamesObj)
  {
    this.graphNamesObj = graphNamesObj;
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

  public String getPhaseName()
  {
    return phaseName;
  }

  public void setPhaseName(String phaseName)
  {
    this.phaseName = phaseName;
  }

  public String getInterval()
  {
    return interval;
  }

  public void setInterval(String interval)
  {
    this.interval = interval;
  }

  public boolean isTestViewMode()
  {
    return testViewMode;
  }

  public void setTestViewMode(boolean testViewMode)
  {
    this.testViewMode = testViewMode;
  }

  public TestRunDataType getTestRunDataTypeObj()
  {
    return testRunDataTypeObj;
  }

  public void setTestRunDataTypeObj(TestRunDataType testRunDataTypeObj)
  {
    this.testRunDataTypeObj = testRunDataTypeObj;
  }
}

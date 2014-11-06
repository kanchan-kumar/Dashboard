/*--------------------------------------------------------------------
  @Name    : AdvisoryRule.java
  @Author  : Ravi Kant Sharma
  @Purpose : Provides rules information. This information writes into a file alert_status.dat
  @Modification History:
    10/06/13: Ravi Kant Sharma - Initial Version

----------------------------------------------------------------------*/
package pac1.Bean;

public class AdvisoryAlertStatus
{
  //rule id
  private String ruleId;
  //testrun
  private String testRun;
  //rule name
  private String ruleName;
  //severity
  private String severity;
  //alert time
  private String alertTime;
  //alert message
  private String alertMsg;
  //alert value
  private String value;
  //alert start time relative
  private String startTime;
  //alert end time relative
  private String endTime;
  //graphName
  private String graphNames;
  //rule description
  private String ruleDesc;
  //rule type
  private String ruleType;
  //graph data index
  private String graphDataIndex;
  //base line test run
  private String baseLineTR;
  //base line test run graph index
  private String graphDataIndexBaseline;
  
  /**
   * getter of start time
   * @return
   */
  public String getStartTime()
  {
    return startTime;
  }

  /**
   * setter of start time
   * @param startTime
   */
  public void setStartTime(String startTime)
  {
    this.startTime = startTime;
  }

  /**
   * getter of end time
   * @return
   */
  public String getEndTime()
  {
    return endTime;
  }

  /**
   * setter of end time
   * @param endTime
   */
  public void setEndTime(String endTime)
  {
    this.endTime = endTime;
  }

  /**
   * get graph name
   * @return
   */
  public String getGraphNames()
  {
    return graphNames;
  }

  /**
   * 
   * @param graphNames
   */
  public void setGraphNames(String graphNames)
  {
    this.graphNames = graphNames;
  }

  /**
   * Getter rule description
   * @return
   */
  public String getRuleDesc()
  {
    return ruleDesc;
  }

  
  /**
   * Setter for rule description
   * @param ruleDesc
   */
  public void setRuleDesc(String ruleDesc)
  {
    this.ruleDesc = ruleDesc;
  }

  /**
   * Getter for rule type
   * @return
   */
  public String getRuleType()
  {
    return ruleType;
  }

  /**
   * setter for rule type
   * @param ruleType
   */
  public void setRuleType(String ruleType)
  {
    this.ruleType = ruleType;
  }

  /**
   * Getter for Graph Data Index 
   * @return
   */
  public String getGraphDataIndex()
  {
    return graphDataIndex;
  }

  /**
   * Setter for Graph Data index
   * @param graphDataIndex
   */
  public void setGraphDataIndex(String graphDataIndex)
  {
    this.graphDataIndex = graphDataIndex;
  }

  /**
   * Getter for Base line Test run number
   * @return
   */
  public String getBaseLineTR()
  {
    return baseLineTR;
  }

  /**
   * Setter for Base line test run
   * @param baseLineTR
   */
  public void setBaseLineTR(String baseLineTR)
  {
    this.baseLineTR = baseLineTR;
  }

  /**
   * getter for graph Data index
   * @return
   */
  public String getGraphDataIndexBaseline()
  {
    return graphDataIndexBaseline;
  }

  /**
   * Setter for graph data index for base line test run
   * @param graphDataIndexBaseline
   */
  public void setGraphDataIndexBaseline(String graphDataIndexBaseline)
  {
    this.graphDataIndexBaseline = graphDataIndexBaseline;
  }

  /**
   * Getter for Rule Id 
   * @return
   */
  public String getRuleId()
  {
    return ruleId;
  }

  /**
   * setter for rule id
   * @param ruleId
   */
  public void setRuleId(String ruleId)
  {
    this.ruleId = ruleId;
  }

  /**
   * setter for test run
   * @return
   */
  public String getTestRun()
  {
    return testRun;
  }

  /**
   * getter for test run
   * @param testRun
   */
  public void setTestRun(String testRun)
  {
    this.testRun = testRun;
  }

  /**
   * Getter for rule Name
   * @return
   */
  public String getRuleName()
  {
    return ruleName;
  }

  /**
   * Setter rule name
   * @param ruleName
   */
  public void setRuleName(String ruleName)
  {
    this.ruleName = ruleName;
  }

  /**
   * Getter for Severity
   * @return
   */
  public String getSeverity()
  {
    return severity;
  }

  /**
   * Setter severity 
   * @param severity
   */
  public void setSeverity(String severity)
  {
    this.severity = severity;
  }

  /**
   * Getter for alert time
   * @return
   */
  public String getAlertTime()
  {
    return alertTime;
  }

  /**
   * Setter for alert time
   * @param alertTime
   */
  public void setAlertTime(String alertTime)
  {
    this.alertTime = alertTime;
  }

  /**
   * Getter for alert Message
   * @return
   */
  public String getAlertMsg()
  {
    return alertMsg;
  }

  /**
   * Setter for alert message
   * @param alertMsg
   */
  public void setAlertMsg(String alertMsg)
  {
    this.alertMsg = alertMsg;
  }
  
  /**
   * Getter value
   * @return
   */
  public String getValue()
  {
    return value;
  }

  /**
   * Setter value
   * @param value
   */
  public void setValue(String value)
  {
    this.value = value;
  }
}

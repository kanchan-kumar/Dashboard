//-------------------------------------------------------//
//  Name   : CheckRuleResultDTO.java
//  Author : Ravi Kant Sharma
//  Purpose: This is for save the results of Check Rules
//  Notes  : 
//  Modification History:
//   25 April 2013: Ravi Kant Sharma: - Initial Version
//-----------------------------------------------------//
package pac1.Bean;

import java.io.Serializable;

public class CheckRuleResultDTO implements Serializable
{
  /*
   * Note:
   * 
   * ---------------------Result File Format----------------------
   * 
   * #RuleName|Message|Value/%Change|startTime|EndTime|RuleType|Graph
   * Details|Baseline Graph Id |Correlated Graph Ids|Vector name|Baseline
   * TR|Baseline graph Id|Condition| Fail/Pass status| BaseLine TestRun
   * values|Previous TestRun Values| Future1| Future2
   * 
   * highAverageResponseTime|Rule added for
   * testing|MIN(1.3.NA)=0|NA|NA|Check|NA|NA|NA|NA|-1|NA|MIN(1.3.NA) >
   * 20|Fail|NA|NA|NA|NA highAverage1|Rule added for
   * testing|MIN(8.2.1xx)=0,MIN(8.2
   * .2xx)=0,MIN(8.2.3xx)=0,MAX(1.1.NA)=1,MAX(1.1.NA
   * )=1,MAX(1.1.NA)=1|NA|NA|Check
   * |NA|NA|NA|NA|-1|NA|AVG(MIN(8.2.1xx,8.2.2xx,8.2.3xx),MAX(1.1.NA)) ==
   * 1000|Fail|NA|NA|NA|NA
   * 
   * highAverage2|Rule added for
   * testing|MAX(2.4.NA)=25952.2|NA|NA|Check|NA|NA|NA|NA|-1|NA|MAX(2.4.NA) >=
   * 0|Pass|NA|NA|NA|NA
   * 
   * highAverage3|Rule added for
   * testing|MAX(2.1.NA)=1.3|NA|NA|Check|NA|NA|NA|NA|-1|NA|MAX(2.1.NA) >
   * 10|Fail|NA|NA|NA|NA highAverage4|Rule added for
   * testing|MAX(2.3.NA)=174.18|NA|NA|Check|NA|NA|NA|NA|-1|NA|MAX(2.3.NA) <
   * 10|Fail|NA|NA|NA|NA
   * 
   * --------------------------------------------------------------
   */

  // variables for holding the index
  public static int RuleName_INDEX = 0;
  public static int Message_INDEX = 1;
  public static int Percentage_Change_INDEX = 2;
  public static int startTime_INDEX = 3;
  public static int EndTime_INDEX = 4;
  public static int RuleType_INDEX = 5;
  public static int Graph_Details_INDEX = 6;
  public static int Base_Graph_Id_INDEX = 7;
  public static int Correlated_Graph_Ids_INDEX = 8;
  public static int Vector_NAME_Index = 9;
  public static int Baseline_TR_INDEX = 10;
  public static int Baseline_Graph_Id_Index = 11;
  public static int Condition_Index = 12;
  public static int Status_INDEX = 13;
  public static int BaseLine_TestRun_values_Index = 14;
  public static int Previous_TestRun_Values_Index = 15;

  // variable for getter and setters
  private int checkRuleID = 0;
  private int totalRules = -1;
  private String strPercentageChange = null;
  private String strRuleName = null;
  private String strRuleMessage = null;
  private String strStartTime = null;
  private String strEndTime = null;
  private String strRuleType = null;
  private String strGraphDetails = null;
  private String strCorrelatedGraphIds = null;
  private String strVectorName = null;
  private String strBaseLineTestRun = null;
  private String strBaseLineGraphId = null;
  private String strBaseGraphIds = null;
  private String strCondition = null;
  private String strStatus = null;
  private String strBaseLineTestRunValues = null;
  private String strPreviousTestRunValues = null;

  public String getStrBaseGraphIds()
  {
    return strBaseGraphIds;
  }

  public void setStrBaseGraphIds(String strBaseGraphIds)
  {
    this.strBaseGraphIds = strBaseGraphIds;
  }

  public int getCheckRuleID()
  {
    return checkRuleID;
  }

  public void setCheckRuleID(int checkRuleID)
  {
    this.checkRuleID = checkRuleID;
  }

  public int getTotalRules()
  {
    return totalRules;
  }

  public void setTotalRules(int totalRules)
  {
    this.totalRules = totalRules;
  }

  public String getStrPercentageChange()
  {
    return strPercentageChange;
  }

  public void setStrPercentageChange(String strPercentageChange)
  {
    this.strPercentageChange = strPercentageChange;
  }

  public String getStrRuleName()
  {
    return strRuleName;
  }

  public void setStrRuleName(String strRuleName)
  {
    this.strRuleName = strRuleName;
  }

  public String getStrRuleMessage()
  {
    return strRuleMessage;
  }

  public void setStrRuleMessage(String strRuleMessage)
  {
    this.strRuleMessage = strRuleMessage;
  }

  public String getStrStartTime()
  {
    return strStartTime;
  }

  public void setStrStartTime(String strStartTime)
  {
    this.strStartTime = strStartTime;
  }

  public String getStrEndTime()
  {
    return strEndTime;
  }

  public void setStrEndTime(String strEndTime)
  {
    this.strEndTime = strEndTime;
  }

  public String getStrRuleType()
  {
    return strRuleType;
  }

  public void setStrRuleType(String strRuleType)
  {
    this.strRuleType = strRuleType;
  }

  public String getStrGraphDetails()
  {
    return strGraphDetails;
  }

  public void setStrGraphDetails(String strGraphDetails)
  {
    this.strGraphDetails = strGraphDetails;
  }

  public String getStrCorrelatedGraphIds()
  {
    return strCorrelatedGraphIds;
  }

  public void setStrCorrelatedGraphIds(String strCorrelatedGraphIds)
  {
    this.strCorrelatedGraphIds = strCorrelatedGraphIds;
  }

  public String getStrVectorName()
  {
    return strVectorName;
  }

  public void setStrVectorName(String strVectorName)
  {
    this.strVectorName = strVectorName;
  }

  public String getStrBaseLineTestRun()
  {
    return strBaseLineTestRun;
  }

  public void setStrBaseLineTestRun(String strBaseLineTestRun)
  {
    this.strBaseLineTestRun = strBaseLineTestRun;
  }

  public String getStrBaseLineGraphId()
  {
    return strBaseLineGraphId;
  }

  public void setStrBaseLineGraphId(String strBaseLineGraphId)
  {
    this.strBaseLineGraphId = strBaseLineGraphId;
  }

  public String getStrCondition()
  {
    return strCondition;
  }

  public void setStrCondition(String strCondition)
  {
    this.strCondition = strCondition;
  }

  public String getStrStatus()
  {
    return strStatus;
  }

  public void setStrStatus(String strStatus)
  {
    this.strStatus = strStatus;
  }

  public String getStrBaseLineTestRunValues()
  {
    return strBaseLineTestRunValues;
  }

  public void setStrBaseLineTestRunValues(String strBaseLineTestRunValues)
  {
    this.strBaseLineTestRunValues = strBaseLineTestRunValues;
  }

  public String getStrPreviousTestRunValues()
  {
    return strPreviousTestRunValues;
  }

  public void setStrPreviousTestRunValues(String strPreviousTestRunValues)
  {
    this.strPreviousTestRunValues = strPreviousTestRunValues;
  }

  public static void main(String[] args)
  {
    // TODO Auto-generated method stub
    String record = "Profile|Project|SubProject1|SubProject2|UserName";

  }
}

/*--------------------------------------------------------------------
  @Name    : AdvisoryRule.java
  @Author  : Jyoti Jain
  @Purpose : Provides getter setter method utility
  @Read only :- Format of file
     Rule Type|Rule Id|Threshold Type|Graph Definition|Operation|Value|Time Option|Start Time|End Time|Time Window|Future1|Future2|Future3|Description
  @Modification History:
    08/24/11:Jyoti Jain - Initial Version
    10/06/2013 Ravi Kant - Add new fields for alert_status.dat

----------------------------------------------------------------------*/

package pac1.Bean;

import pac1.Bean.GraphName.GraphNames;

public class AdvisoryRuleInfo
{
  private String className = "AdvisoryRuleInfo";

  // used for rule type
  private String strRuleType = "";
  // used for rule id
  private String strRuleId = "";
  // used for threashold type
  private String strThresholdType = "";
  // used for graph def
  private String strGraphDef = "";
  // used for group id
  private String strGroupId = "";
  // used for graph id
  private String strGraphId = "";
  // used for vector name
  private String strVecName = "";
  // used for operation
  private String strOperation = "";
  // used for value
  private String strValue = "";
  // used for pct message change
  private String strPctChange = "";
  // used for time window
  private String strTimeWindow = "";
  // used for rule description
  private String strDescription = "";
  // used for graph data index
  private int graphDataIndex = -1;
  // used for progress interval
  private int interval = 10000;
  // used for graph name
  private String strGraphName = "";
  // used for rule details
  private String strRuleDetail = "";
  private String ruleName = "";
  private String alertMessage = "";

  private GraphUniqueKeyDTO graphUniqueKeyDTO;
  
  public String getAlertMessage()
  {
    return alertMessage;
  }

  public void setAlertMessage(String alertMessage)
  {
    this.alertMessage = alertMessage;
  }

  public String getRuleName()
  {
    return ruleName;
  }

  public void setRuleName(String ruleName)
  {
    this.ruleName = ruleName;
  }

  // Rule Type|Rule Id|Threshold Type|Graph Definition|Operation|Value|Last
  // State Change Time|Current State|% Change|Time
  // Window|Active|Severity|LogInAlertWindow|Description|Rule Name|Alert
  // Description|future1|future2|future3|future4|future5
  private GraphNames graphNamesObj = null;

  /**
   * Constructor
   * 
   */
  public AdvisoryRuleInfo(GraphNames graphNamesObj)
  {
    this.graphNamesObj = graphNamesObj;
  }

  // /////////////////////////////Getter methods///////////////////////////////

  public String getRuleType()
  {
    return strRuleType;
  }

  public String getRuleId()
  {
    return strRuleId;
  }

  public String getThresholdType()
  {
    return strThresholdType;
  }

  public String getGraphDef()
  {
    return strGraphDef;
  }

  public String getGroupId()
  {
    return strGroupId;
  }

  public String getGraphId()
  {
    return strGraphId;
  }

  public String getVecName()
  {
    return strVecName;
  }

  public String getOperation()
  {
    return strOperation;
  }

  public String getValue()
  {
    try
    {
      return strValue;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "getValue", "", "", "Exception - " + e);
      return "-1.0";
    }
  }

  public String getPctChange()
  {
    return strPctChange;
  }

  public String getTimeWindow()
  {
    return strTimeWindow;
  }

  public String getDescription()
  {
    return strDescription;
  }

  public int getGraphDataIndex()
  {
    return graphDataIndex;
  }

  public String getStrGraphName()
  {
    return strGraphName;
  }

  public int getInterval()
  {
    return interval;
  }

  public String getRuleDetails()
  {
    return strRuleDetail;
  }

  // ////////////////////////////Setter method///////////////////////////

  public void setGraphIndexAndName()
  {
    Log.debugLog(className, "setGraphIndexAndName", "", "", "Method Called. strGroupId = " + strGroupId + ", strGraphId = " + strGraphId + " , strVecName = " + strVecName);

    try
    {
      if (strGroupId.equals("Group Id"))
      {
        this.graphDataIndex = -1;
        this.strGraphName = "";
        Log.debugLogAlways(className, "setGraphIndexAndName", "", "", "graphDataIndex for Group Id = " + graphDataIndex);
      }
      else
      {
        this.graphUniqueKeyDTO = new GraphUniqueKeyDTO(Integer.parseInt(strGroupId), Integer.parseInt(strGraphId), strVecName);
        
        if(graphNamesObj == null)
          Log.debugLog(className, "setGraphIndexAndName", "", "", "graphNamesObj = NULLL");
        
        if(this.graphUniqueKeyDTO == null)
          Log.debugLog(className, "setGraphIndexAndName", "", "", "graphUniqueKeyDTO = NUllll");
        
        Log.debugLog(className, "setGraphIndexAndName", "", "", "graphUniqueKeyDTO = " + graphUniqueKeyDTO);

        this.graphDataIndex = graphNamesObj.getGraphDataIndexByGraphUniqueKeyDTO(this.graphUniqueKeyDTO);
        Log.debugLog(className, "setGraphIndexAndName", "", "", "graphDataIndex = " + graphDataIndex );
        
        this.interval = graphNamesObj.getInterval();
        
        this.strGraphName = graphNamesObj.getGraphNameByGraphUniqueKeyDTO(graphUniqueKeyDTO, true);
        Log.debugLog(className, "setGraphIndexAndName", "", "", "graphDataIndex = " + graphDataIndex + "interval = " + interval + "strGraphName" + strGraphName);
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getArrayListOfRuleDataInfoObj", "", "", "Exception - ", e);
    }
  }

  public GraphUniqueKeyDTO getGraphUniqueKeyDTO()
  {
    return graphUniqueKeyDTO;
  }

  public void setRuleType(String strRuleType)
  {
    this.strRuleType = strRuleType;
  }

  public void setRuleId(String strRuleId)
  {
    this.strRuleId = strRuleId;
  }

  public void setThresholdType(String strThresholdType)
  {
    this.strThresholdType = strThresholdType;
  }

  public void setGraphDef(String strGraphDef)
  {
    this.strGraphDef = strGraphDef;
  }

  public void setGroupId(String strGroupId)
  {
    this.strGroupId = strGroupId;
  }

  public void setGraphId(String strGraphId)
  {
    this.strGraphId = strGraphId;
  }

  public void setVecName(String strVecName)
  {
    this.strVecName = strVecName;
  }

  public void setOperation(String strOperation)
  {
    this.strOperation = strOperation;
  }

  public void setValue(String strValue)
  {
    this.strValue = strValue;
  }

  public void setPctChange(String strPctChange)
  {
    this.strPctChange = strPctChange;
  }

  public void setTimeWindow(String strTimeWindow)
  {
    this.strTimeWindow = strTimeWindow;
  }

  public void setDescription(String strDescription)
  {
    this.strDescription = strDescription;
  }

  public void setRuleDetails()
  {
    this.strRuleDetail = "Rule Type = " + getRuleType() + ", Rule Id = " + getRuleId() + ", Threahold Type = " + getThresholdType() + ", Graph Definition = " + getGraphDef() + ", Graph Data Index = " + getGraphDataIndex() + ", Operation = " + getOperation() + ", Value = " + getValue() + ", % Change = " + getPctChange() + ", Time window = " + getTimeWindow() + ", Description = " + getDescription();
  }
}

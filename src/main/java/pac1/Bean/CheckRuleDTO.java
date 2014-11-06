//-------------------------------------------------------//
//  Name   : CheckRuleDTO.java
//  Author : Ravi Kant Sharma
//  Purpose: DTO for Check Rule
//  Notes  : 
//  Modification History:
//   17 April 2013: Ravi Kant Sharma: - Initial Version
//-----------------------------------------------------//
package pac1.Bean;

import java.io.Serializable;

public class CheckRuleDTO implements Serializable
{
  private static final long serialVersionUID = 1L;
  private int ruleId = -1;
  private String ruleName = null;
  private String ruleType = null;
  private String graphDefinition = null;
  private int perecentChange = -1;
  private String expectedResult = null;
  private String state = null; // Active/Inactive
  private String lastUpdatedBy = null;
  private String lastUpdatedOn = null;
  private String ruleDescription = null;

  public int getRuleId()
  {
    return ruleId;
  }

  public void setRuleId(int ruleId)
  {
    this.ruleId = ruleId;
  }

  public String getRuleName()
  {
    return ruleName;
  }

  public void setRuleName(String ruleName)
  {
    this.ruleName = ruleName;
  }

  public String getRuleType()
  {
    return ruleType;
  }

  public void setRuleType(String ruleType)
  {
    this.ruleType = ruleType;
  }

  public String getGraphDefinition()
  {
    return graphDefinition;
  }

  public void setGraphDefinition(String graphDefinition)
  {
    this.graphDefinition = graphDefinition;
  }

  public int getPerecentChange()
  {
    return perecentChange;
  }

  public void setPerecentChange(int perecentChange)
  {
    this.perecentChange = perecentChange;
  }

  public String getExpectedResult()
  {
    return expectedResult;
  }

  public void setExpectedResult(String expectedResult)
  {
    this.expectedResult = expectedResult;
  }

  public String getState()
  {
    return state;
  }

  public void setState(String state)
  {
    this.state = state;
  }

  public String getLastUpdatedBy()
  {
    return lastUpdatedBy;
  }

  public void setLastUpdatedBy(String lastUpdatedBy)
  {
    this.lastUpdatedBy = lastUpdatedBy;
  }

  public String getLastUpdatedOn()
  {
    return lastUpdatedOn;
  }

  public void setLastUpdatedOn(String lastUpdatedOn)
  {
    this.lastUpdatedOn = lastUpdatedOn;
  }

  public String getRuleDescription()
  {
    return ruleDescription;
  }

  public void setRuleDescription(String ruleDescription)
  {
    this.ruleDescription = ruleDescription;
  }

  // Rule|Rule Id|Rule Name|Rule Type|Graph Definition|% Change|Expected
  // Result|State|Last Updated By|Last Updated
  // On|Future1|Future2|Future3|Future4|Future5|Rule Description
  public static void main(String[] args)
  {
    // TODO Auto-generated method stub

  }

}

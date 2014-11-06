//-------------------------------------------------------//
//  Name   : CheckRuleInstance.java
//  Author : Varun Singh
//  Purpose: Instance for Check Rule
//  Notes  : 
//  Modification History:
//   27 November 2013: Varun Singh: - Initial Version
//-----------------------------------------------------//
package pac1.Bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class CheckRuleInstance
{
	 //rule ID
	  private int ruleId = -1;
	  //rule Name
	  private String ruleName = null;
	  //rule type it can be two types check rules or compare rule 0 - check rule 1 - compare rules
	  private byte ruleType = 0;
	  //rule expression it can be single graph or combination of graph 
	  //graph can be scalar type or vector type
	  private String graphDefinition = null;
	  //percent change for compare rule
	  private int percentChange = -1;
	  //expected result pass or fail false - fail , true - pass
	  private boolean expectedResult = true;
	  // Active/Inactive state of rule false - inactive true - active
	  private boolean state = true; 
	  //last update by user 
	  private String lastUpdatedBy = null;
	  //last modified time of check rule
	  private String lastUpdatedOn = null;
	  //vector name
	  private String vectorName = "NA"; 
	  //rule description
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
	
	  public byte getRuleType()
	  {
	    return ruleType;
	  }
	
	  public void setRuleType(byte ruleType)
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
	
	  public int getPercentChange()
	  {
	    return percentChange;
	  }
	
	  public void setPercentChange(int percentChange)
	  {
	    this.percentChange = percentChange;
	  }

	  public boolean getExpectedResult()
	  {
	    return expectedResult;
	  }
	
	  public void setExpectedResult(boolean expectedResult)
	  {
	    this.expectedResult = expectedResult;
	  }
	
	  public boolean getState()
	  {
	    return state;
	  }
	
	  public void setState(boolean state)
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
	  
	  public String getVectorName()
	  {
		return vectorName;
	  }
	
	  public void setVectorName(String vectorName)
	  {
		this.vectorName = vectorName;
	  }

	  public String toString()
	  {
		StringBuilder attrBuilder = new StringBuilder();
		attrBuilder.append("Rule");
		attrBuilder.append("|");
		attrBuilder.append(ruleId);
		attrBuilder.append("|");
		attrBuilder.append(ruleName);
		attrBuilder.append("|");
		attrBuilder.append(ruleType);
		attrBuilder.append("|");
		attrBuilder.append(graphDefinition);
		attrBuilder.append("|");
		attrBuilder.append(percentChange);
		attrBuilder.append("|");
		attrBuilder.append(expectedResult);
		attrBuilder.append("|");
		attrBuilder.append(state);
		attrBuilder.append("|");
		attrBuilder.append(lastUpdatedBy);
		attrBuilder.append("|");
		attrBuilder.append(lastUpdatedOn);
		attrBuilder.append("|NA|NA|NA|NA|NA|");
		attrBuilder.append(ruleDescription);
	    return attrBuilder.toString();
	  }
	  
	  public void readFile()
	  {
		
		try
		{
			File fileObj = new File("path");
			BufferedReader bufferedReader = new BufferedReader(new FileReader(fileObj));
			while(bufferedReader.readLine() != null)
			{
			
			}
		}
		catch(Exception e){
			
		}
		
	  }

  	  //Rule|Rule Id|Rule Name|Rule Type|Graph Definition|% Change|ExpectedResult|State|Last Updated By|Last UpdatedOn|Future1|Future2|Future3|Future4|Future5|Rule Description
	  public static void main(String[] args)
	  {
		  CheckRuleInstance checkRuleInstance = new CheckRuleInstance();
		  checkRuleInstance.setRuleId(1);
		  checkRuleInstance.setRuleName("Transactions_Failures_Rule");
		  checkRuleInstance.setRuleType((byte)1);
		  checkRuleInstance.setGraphDefinition("Avg ( 10.2.All ) < 20.0");
		  checkRuleInstance.setPercentChange(50);
		  checkRuleInstance.setExpectedResult(true);
		  checkRuleInstance.setState(true);
		  checkRuleInstance.setLastUpdatedBy("netstorm");
		  checkRuleInstance.setLastUpdatedOn("07/09/2013 11:11:22");
		  checkRuleInstance.setRuleDescription("Avg Transactions Failures/Minute - All");
		  System.out.println(checkRuleInstance.toString());
	
	  }

	}

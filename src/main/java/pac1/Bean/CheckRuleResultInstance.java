//-------------------------------------------------------//
//  Name   : CheckRuleResultInstance.java
//  Author : Varun Singh
//  Purpose: This is for saving the results of Check Rules
//  Notes  : 
//  Modification History:
//   27 November 2013: Varun Singh: - Initial Version
//-----------------------------------------------------//
package pac1.Bean;

import java.io.Serializable;
import java.util.ArrayList;

public class CheckRuleResultInstance implements Serializable
{
  /*
   * ---------------------Result File Format----------------------
   * 
   * ruleName|message|graph values(comma separated data)|StartTime|endTime|ruleType|GraphDetails|graphID|Correlated Graph Ids|vectorName|
   * BaseLine TR|baseLineGraphID|condition|status|graph values of base line test run(comma separated data)|graph Values of previous test run 
   * (comma separated data)| expression Value|expression value for base line TR|expression value for previous TR| expression value of trend test run 
   * (comma separated value)|graph values of trend data by comma separated for specific test run And use & for other trend test runs 
   * 
   * --------------------------------------------------------------
   */

    //rule name
	private String ruleName = null;
	//message
	private String message = null;
	//value or value in % change in graphs used in check rule 
	private ArrayList<Double> value = null;
	//start time 
	private String startTime = "NA";
	//end time
	private String endTime = "NA";
	//rule type 0 for check rule, 1 for compare rule
	private byte ruleType = 0;
	//graph details
	private ArrayList<String> graphDetails = new ArrayList<String>(1); 
	//graphId
	private int graphId = -1;
	//Correlated Graph Ids
	private int correlatedGraphID = -1;
	//vector name
	private String vectorName = "NA";
	//BaseLine TR
	private int baseLineTR = -1;
	//base line graph Id
	private int baseLineGraphID = -1;
	//expression condition 
	private String condition = "";
	//fail or pass status 
	private boolean status = false;
	//graph values for base line 
	private ArrayList<Double> baseLineArr = null;
	//graph values for previous test run
	private ArrayList<Double> previousTestRunArr = null;
	//expression value
	private double expressionValue = -1;
	//expression value for base line test run
	private double expValForbaseLine = -1;
	//expression value for previous test run
	private double expValForPreviousTest =  -1;
	//expression value for Trend test runs
	private ArrayList<Double> expValForTrendTest =  new ArrayList<Double>();
	//graph value for Trend test runs
	private ArrayList<ArrayList<Double>> trendTestRunArr = new ArrayList<ArrayList<Double>>(); 

    public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ArrayList<Double> getValue() {
		return value;
	}

	public void setValue(ArrayList<Double> value) {
		this.value = value;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public byte getRuleType() {
		return ruleType;
	}

	public void setRuleType(byte ruleType) {
		this.ruleType = ruleType;
	}

	public ArrayList<String> getGraphDetails() {
		return graphDetails;
	}

	public void setGraphDetails(ArrayList<String> graphDetails) {
		this.graphDetails = graphDetails;
	}

	public int getGraphId() {
		return graphId;
	}

	public void setGraphId(int graphId) {
		this.graphId = graphId;
	}

	public int getCorrelatedGraphID() {
		return correlatedGraphID;
	}

	public void setCorrelatedGraphID(int correlatedGraphID) {
		this.correlatedGraphID = correlatedGraphID;
	}

	public String getVectorName() {
		return vectorName;
	}

	public void setVectorName(String vectorName) {
		this.vectorName = vectorName;
	}

	public int getBaseLineTR() {
		return baseLineTR;
	}

	public void setBaseLineTR(int baseLineTR) {
		this.baseLineTR = baseLineTR;
	}

	public int getBaseLineGraphID() {
		return baseLineGraphID;
	}

	public void setBaseLineGraphID(int baseLineGraphID) {
		this.baseLineGraphID = baseLineGraphID;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public ArrayList<Double> getBaseLineArr() {
		return baseLineArr;
	}

	public void setBaseLineArr(ArrayList<Double> baseLineArr) {
		this.baseLineArr = baseLineArr;
	}

	public ArrayList<Double> getPreviousTestRunArr() {
		return previousTestRunArr;
	}

	public void setPreviousTestRunArr(ArrayList<Double> previousTestRunArr) {
		this.previousTestRunArr = previousTestRunArr;
	}

	public double getExpressionValue() {
		return expressionValue;
	}

	public void setExpressionValue(double expressionValue) {
		this.expressionValue = expressionValue;
	}

	public double getExpValForbaseLine() {
		return expValForbaseLine;
	}

	public void setExpValForbaseLine(double expValForbaseLine) {
		this.expValForbaseLine = expValForbaseLine;
	}

	public double getExpValForPreviousTest() {
		return expValForPreviousTest;
	}

	public void setExpValForPreviousTest(double expValForPreviousTest) {
		this.expValForPreviousTest = expValForPreviousTest;
	}

	public ArrayList<Double> getExpValForTrendTest() {
		return expValForTrendTest;
	}

	public void setExpValForTrendTest(ArrayList<Double> expValForTrendTest) {
		this.expValForTrendTest = expValForTrendTest;
	}

	public ArrayList<ArrayList<Double>> getTrendTestRunArr() {
		return trendTestRunArr;
	}

	public void setTrendTestRunArr(ArrayList<ArrayList<Double>> trendTestRunArr) {
		this.trendTestRunArr = trendTestRunArr;
	}
	
	/**
	 * String representation of CheckRuleResultInstance object. 
	 */
	public String toString()
	{
		StringBuilder attrBuilder = new StringBuilder();
		attrBuilder.append(ruleName);
		attrBuilder.append("|");
		attrBuilder.append(message);
		attrBuilder.append("|");
		StringBuilder concatValue = new StringBuilder();
		if(value.size() == 0)
		{
			attrBuilder.append("11");	
		}
		else
		{
			for(double s: value){
				concatValue.append(s);
				concatValue.append(",");
			}
			attrBuilder.append(concatValue.substring(0, (concatValue.length() -1)));
		}
		attrBuilder.append("|");
		attrBuilder.append(startTime);
		attrBuilder.append("|");
		attrBuilder.append(endTime);
		attrBuilder.append("|");
		attrBuilder.append(ruleType);
		attrBuilder.append("|");
		attrBuilder.append(graphDetails);
		attrBuilder.append("|");
		attrBuilder.append(graphId);
		attrBuilder.append("|");
		attrBuilder.append(correlatedGraphID);
		attrBuilder.append("|");
		attrBuilder.append(vectorName);
		attrBuilder.append("|");
		attrBuilder.append(baseLineTR);
		attrBuilder.append("|");
		attrBuilder.append(baseLineGraphID);
		attrBuilder.append("|");
		attrBuilder.append(condition);
		attrBuilder.append("|");
		attrBuilder.append(status);
		attrBuilder.append("|");
		StringBuilder concatbaseLineArr = new StringBuilder();
		if(baseLineArr.size() == 0)
		{
			attrBuilder.append("11");	
		}
		else
		{
			for(double s: baseLineArr){
				concatbaseLineArr.append(s);
				concatbaseLineArr.append(",");
			}
			attrBuilder.append(concatbaseLineArr.substring(0, (concatbaseLineArr.length() -1)));
		}
		attrBuilder.append("|");
		StringBuilder concatpreviousTestRunArr = new StringBuilder();
		if(previousTestRunArr.size() == 0)
		{
			attrBuilder.append("11");	
		}
		else
		{
			for(double s: previousTestRunArr){
				concatpreviousTestRunArr.append(s);
				concatpreviousTestRunArr.append(",");
			}
			attrBuilder.append(concatpreviousTestRunArr.substring(0, (concatpreviousTestRunArr.length() -1)));
		}
		attrBuilder.append("|");
		attrBuilder.append(expressionValue);
		attrBuilder.append("|");
		attrBuilder.append(expValForbaseLine);
		attrBuilder.append("|");
		attrBuilder.append(expValForPreviousTest);
		attrBuilder.append("|");
		StringBuilder concatTrendTestRunDataArr1 = new StringBuilder();
		if(expValForTrendTest.size() == 0)
		{
			attrBuilder.append("11");	
		}
		else
		{
			for(int i=0; i<expValForTrendTest.size(); i++){
			for(double s: expValForTrendTest){
					concatTrendTestRunDataArr1.append(String.valueOf(s));
					concatTrendTestRunDataArr1.append(",");
				}
				concatTrendTestRunDataArr1 = concatTrendTestRunDataArr1.deleteCharAt(concatTrendTestRunDataArr1.length()-1);
				concatTrendTestRunDataArr1.append("&");
			}
			attrBuilder.append(concatTrendTestRunDataArr1.substring(0, (concatTrendTestRunDataArr1.length() -1)));
		}
		attrBuilder.append("|");
		StringBuilder concatTrendTestRunDataArr = new StringBuilder();
		if(trendTestRunArr.size() == 0)
		{
			attrBuilder.append("11");	
		}
		else
		{
			for(int i=0; i<trendTestRunArr.size(); i++){
				ArrayList<Double> graphValues = trendTestRunArr.get(i);
				for(Double s: graphValues){
					concatTrendTestRunDataArr.append(String.valueOf(s));
					concatTrendTestRunDataArr.append(",");
				}
				concatTrendTestRunDataArr = concatTrendTestRunDataArr.deleteCharAt(concatTrendTestRunDataArr.length()-1);
				concatTrendTestRunDataArr.append("&");
			}
			attrBuilder.append(concatTrendTestRunDataArr.substring(0, (concatTrendTestRunDataArr.length() -1)));
		}
		return attrBuilder.toString();
	  }

   //ruleName|message|graph values(comma separated data)|StartTime|endTime|ruleType|GraphDetails|graphID|Correlated Graph Ids|vectorName|BaseLine TR|
   //baseLineGraphID|condition|status| graph values of base line test run(comma separated data)|graph Values of previous test run (comma separated data)|
   //expression Value|expression value for base line TR|  expression value for previous TR| expression value of trend test run (comma separated value)|
   //graph values of trend data by comma separated for specific test run And use & for other trend test runs
   public static void main(String[] args)
   {
	 CheckRuleResultInstance checkRuleResultInstance = new CheckRuleResultInstance();
	 checkRuleResultInstance.setRuleName("Transactions_Failures_Rule");
	 checkRuleResultInstance.setMessage("Avg Transactions Failures/Minute - SSLWriteFail,ClickAway");
	 ArrayList<Double> valueDouble = new ArrayList<Double>();
	 valueDouble.add(1d);
	 valueDouble.add(2d);
	 checkRuleResultInstance.setValue(valueDouble);
	 checkRuleResultInstance.setStartTime("NA");
	 checkRuleResultInstance.setEndTime("NA");
	 checkRuleResultInstance.setRuleType((byte)1);
	 ArrayList<String> gr = new ArrayList<String>();
	 gr.add("NA");
	 checkRuleResultInstance.setGraphDetails(gr);
	 checkRuleResultInstance.setGraphId(-1);
	 checkRuleResultInstance.setCorrelatedGraphID(-1);
	 checkRuleResultInstance.setVectorName("NA");
	 checkRuleResultInstance.setBaseLineTR(-1);
	 checkRuleResultInstance.setBaseLineGraphID(-1);
	 checkRuleResultInstance.setCondition("Avg (10.2.SSLWriteFail,10.2.ClickAway) < 5.0");
	 checkRuleResultInstance.setStatus(true);
	 checkRuleResultInstance.setBaseLineArr(valueDouble);
	 checkRuleResultInstance.setPreviousTestRunArr(valueDouble);
	 ArrayList<ArrayList<Double>> valueDouble1 = new ArrayList<ArrayList<Double>>();
	 valueDouble1.add(valueDouble);
	 double val = 20.0;
	 checkRuleResultInstance.setExpValForTrendTest(valueDouble);
	 checkRuleResultInstance.setExpressionValue(val);
	 checkRuleResultInstance.setExpValForbaseLine(val);
	 checkRuleResultInstance.setExpValForPreviousTest(val);
	 checkRuleResultInstance.setTrendTestRunArr(valueDouble1);
	 
	 System.out.println(checkRuleResultInstance.toString());

  }
}

/*--------------------------------------------------------------------
  @Name    : Threshold.java
  @Author  : Prabhat Vashist
  @Purpose : This is to calculate Threshold events of specific graph by graphData
  @Modification History:
    08/25/11:Prabhat Vashist - Initial Version

----------------------------------------------------------------------*/
package pac1.Bean;

public class Threshold
{
  private static String className = "Threshold";

  private double[] arrRptData = null;
  private double[] arrRptAvgData = null;
  
  private double[] arrRptDataBaseline = null;
  private double[] arrRptAvgDataBaseline = null;  

  private String ruleType = "";
  private String thresholdType = "";
  private String strOperation = "";
  private double value = -1;
  private String strPctChange = "";
  private String strStartTime = "";
  private String strEndTime = "";
  private String strTimeWindow = "";
  private String strGraphNames = "";
  private String strRuleDesc = "";
  private long interval = 10000;
  private String severityOptions = "";
  private String ruleId = "";
  private String ruleName = "";
  private String alertMessage = "";
  
  private String strGraphDataIndex = "NA";
  private String strGraphDataIndexBaseline = "-1";

  private long maxSampleCount = 0;

  private EventGeneratorAdvisory eventGeneratorAdvisoryObj = null;
  private boolean debugLevel = false;

  public Threshold(double[] arrRptData, double[] arrRptAvgData, String ruleType, String thresholdType, String strOperation, double value, String strPctChange, String strStartTime, String strEndTime, String strTimeWindow, String strGraphNames, String strRuleDesc, int interval, EventGeneratorAdvisory eventGeneratorAdvisoryObj, String strGraphDataIndex , String severity , String ruleId , String ruleName , String alertMesssage)
  {
    this.arrRptData = arrRptData;
    this.arrRptAvgData = arrRptAvgData;
    this.strGraphDataIndex = strGraphDataIndex;
    this.severityOptions = severity;
    this.ruleId = ruleId;
    this.ruleName = ruleName;
    this.alertMessage = alertMesssage;
    setCommanValue(ruleType, thresholdType, strOperation, value, strPctChange, strStartTime, strEndTime, strTimeWindow, strGraphNames, strRuleDesc, interval, eventGeneratorAdvisoryObj);
  }
  
  //constructor for baseline comparison
  public Threshold(double[] arrRptData, double[] arrRptAvgData, double[] arrRptDataBaseline, double[] arrRptAvgDataBaseline, String ruleType, String thresholdType, String strOperation, double value, String strPctChange, String strStartTime, String strEndTime, String strTimeWindow, String strGraphNames, String strRuleDesc, int interval, EventGeneratorAdvisory eventGeneratorAdvisoryObj, String strGraphDataIndex, String strGraphDataIndexBaseline , String severity , String ruleID , String ruleName, String alertMessage)
  {
    this.arrRptData = arrRptData;
    this.arrRptAvgData = arrRptAvgData;
    this.strGraphDataIndex = strGraphDataIndex;
    this.severityOptions = severity;
    this.arrRptDataBaseline = arrRptDataBaseline;
    this.arrRptAvgDataBaseline = arrRptAvgDataBaseline;
    this.strGraphDataIndexBaseline = strGraphDataIndexBaseline;    
    this.ruleId = ruleID;
    this.ruleName = ruleName;
    this.alertMessage = alertMessage;
    
    setCommanValue(ruleType, thresholdType, strOperation, value, strPctChange, strStartTime, strEndTime, strTimeWindow, strGraphNames, strRuleDesc, interval, eventGeneratorAdvisoryObj);
  }  
  public void setCommanValue(String ruleType, String thresholdType, String strOperation, double value, String strPctChange, String strStartTime, String strEndTime, String strTimeWindow, String strGraphNames, String strRuleDesc, int interval, EventGeneratorAdvisory eventGeneratorAdvisoryObj)
  {
    this.ruleType = ruleType;
    this.thresholdType = thresholdType;
    this.strOperation = strOperation;
    this.value = value;
    this.strPctChange = strPctChange;
    this.strStartTime = strStartTime;
    this.strEndTime = strEndTime;
    this.strTimeWindow = strTimeWindow;
    this.strGraphNames = strGraphNames;
    this.strRuleDesc = strRuleDesc;
    this.interval = interval;
    this.eventGeneratorAdvisoryObj = eventGeneratorAdvisoryObj;
    //set debug level 
    this.debugLevel = eventGeneratorAdvisoryObj.getDebugLevel();
  }

//calculate threshold and generate events
  public void generateThresholdEventsForCompare()
  {
    if(debugLevel)
      Log.debugLog(className, "generateThresholdEventsForCompare", "", "", "Method called.");

    try
    {
      if(thresholdType.equals("Average"))
      {
        double[] tempArr = null;
        if(arrRptDataBaseline.length > arrRptData.length)
        {
          tempArr = arrRptData;
        }
        else
          tempArr = arrRptDataBaseline;
        
        // Now calculate average
        double average = arrRptAvgData[tempArr.length - 1];
        double averageBaseline = arrRptAvgDataBaseline[tempArr.length - 1];
        long sampleTime = (tempArr.length) * interval;

        String strTimeStamp = eventGeneratorAdvisoryObj.getSampleTime(strStartTime, sampleTime);

        if(debugLevel)
          Log.debugLog(className, "generateThresholdEvents", "", "", "For graph " + strGraphNames + "Current Average = " + average + " and Baseline Average = " + averageBaseline + " at " + strTimeStamp + " time.");

        String eventData = strGraphNames + "|" + strRuleDesc + "|" + strPctChange + "|" + strStartTime + "|" + strTimeStamp + "|" + ruleType + "|NA|" + strGraphDataIndex + "|NA|NA|" + eventGeneratorAdvisoryObj.baselineTR + "|" + strGraphDataIndexBaseline + "|" + getSeverity(average, strOperation)+ "|"+ruleName + "|" + ruleId + "|"+ alertMessage + "|NA|NA";
        
        double compareValue = ((average - averageBaseline)/averageBaseline)*100;
        
        /*if(Double.parseDouble(strPctChange) < 0)
        {
        	if(compareValue < Double.parseDouble(strPctChange))
          {
            eventGeneratorAdvisoryObj.addEventDataToFile(eventData);
          }
        }
        else*/
        {
          if(compareValue > Double.parseDouble(strPctChange))
          {
            eventGeneratorAdvisoryObj.addEventDataToFile(eventData);
          }
        }
      }
    }
    catch (Exception e) 
    {
      Log.stackTraceLog(className, "generateThresholdEventsForCompare", "", "", "Exception - ", e);
    }
  }
  
  // calculate threshold and generate events
  public void generateThresholdEvents()
  {
    if(debugLevel)
      Log.debugLog(className, "generateThresholdEvents", "", "", "Method called.");

    try
    {
      if(thresholdType.equals("Average"))
      {
        if(debugLevel)
          Log.debugLog(className, "generateThresholdEvents", "", "", "Method called.Average");
        // Now calculate average
        double average  = 0d;
        if(debugLevel)
          Log.debugLog(className, "generateThresholdEvents", "", "", " strEndTime = " + strEndTime + " and strStartTime = " + strStartTime);
        
        long sampleCount =  ((rptUtilsBean.convStrToMilliSec(strEndTime) - rptUtilsBean.convStrToMilliSec(strStartTime))/interval) + 1;
        
        if(debugLevel)
          Log.debugLog(className, "generateThresholdEvents", "", "", "For graph " + " strEndTime = " + strEndTime + " and strStartTime = " + strStartTime + ", interval = " + interval);
        
        if(arrRptData.length >= sampleCount)
        {
          average = calculateAverageOfRptData(sampleCount);
          if(debugLevel)
            Log.debugLog(className, "generateThresholdEvents", "", "", "For graph " + strGraphNames + " Average = " + average + " for sampleCount = " + sampleCount);
        }
        else
        {
          average = arrRptData[(arrRptData.length - 1)];
          if(debugLevel)
            Log.debugLog(className, "generateThresholdEvents", "", "", "For graph " + strGraphNames + " Average = " + average + " for sampleCount is less than arrRptData length");
        }
        
        long sampleTime = (arrRptAvgData.length) * interval;

        String strTimeStamp = eventGeneratorAdvisoryObj.getSampleTime(strStartTime, sampleTime);

        if(debugLevel)
          Log.debugLog(className, "generateThresholdEvents", "", "", "For graph " + strGraphNames + " Average = " + average + " at " + strTimeStamp + " time.");

        String eventData = strGraphNames + "|" + strRuleDesc + "|" + average + "|" + strStartTime + "|" + strTimeStamp + "|" + ruleType + "|NA|" + strGraphDataIndex + "|NA|NA|" + eventGeneratorAdvisoryObj.baselineTR + "|" + strGraphDataIndexBaseline + "|" + getSeverity(average, strOperation)+ "|"+ruleName + "|" + ruleId + "|"+ alertMessage + "|NA|NA";
        if(debugLevel)
          Log.debugLog(className, "generateThresholdEvents", "", "", "Event log = " + eventData);
                
        eventGeneratorAdvisoryObj.addEventDataToFile(eventData);
        
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "generateThresholdEvents", "", "", "Exception - ", e);
    }
  }
  
  
  /**
   * this method calculate the average of specific sample counts from last in array
   * @param sampleCount
   * @return
   */
  private double calculateAverageOfRptData(long sampleCount)
  {
    double average = 0d;
    try
    {
      for(int i = 0; i < sampleCount; i++)
      {
        if(debugLevel)
          Log.debugLog(className, "calculateAverageOfRptData", "", "", " value of arrRptData = " + arrRptData[i]);
        average += arrRptData[i];
      }
      
      if(debugLevel)
        Log.debugLog(className, "calculateAverageOfRptData", "", "", " value of sum of average = " + average);
      average = average / sampleCount;
      if(debugLevel)
        Log.debugLog(className, "calculateAverageOfRptData", "", "", " average = " + average);
      return average;
    }
    catch(ArithmeticException aE)
    {
      Log.stackTraceLog(className, "calculateAverageOfRptData", "", "", "Excerption in calculate Average Of RptData", aE);
      return average;
    }
  }

  /*
   * Calculate the severity on the basis of value and thresholds
   * Critical 
   * Major
   * Minor
   */
  private String getSeverity(double value , String operation)
  {
    String saverity = "NA";
    
    try
    {
      if(debugLevel)
        Log.debugLog(className, "getSeverity", "", "", "severity = " + severityOptions + " operation = " + operation + " compare value= " + value);
      String [] arrSav = severityOptions.split(",");
      double Options[] = new double[3];
      
      if(debugLevel)
        Log.debugLog(className, "getSeverity", "", "", "severity option length =  " + Options.length);
      if(arrSav.length == 3)
      {
        for(int i = 0 ; i < arrSav.length ; i++)
        {
          if(!arrSav[i].equals("NA"))
            Options[i] = Double.parseDouble(arrSav[i]);
          else
           Options[i] = -1d;
        }
    
        if(debugLevel)
        {
          Log.debugLog(className, "getSeverity", "", "", "critical value  = " + arrSav[0] + " Major value = " + arrSav[1] + " Minor value= " + arrSav[2]);
          Log.debugLog(className, "getSeverity", "", "", "options value critical value  = " + Options[0] + " options Major value = " + Options[1] + " options minor value= " + Options[2]);
        }
        //less than operation
        if(operation.equals("lessThan"))
        {
          //if critical value is provided so it will compare otherwise false
          if(Options[0] >= 0d ? value < Options[0] : false)
            saverity = "Critical";
          //if critical is provided so compare otherwise true because it is severity for major not for critical
          //and if major is provided so it will compare otherwise it return false
          else if((Options[0] >= 0d ? value >= Options[0] : true) && (Options[1] >= 0d ? value < Options[1] : false)) 
            saverity = "Major";
          //if Major is provided so compare otherwise true because it is severity for minor not for Major
          //and if minor is provided so it will compare otherwise it return false
          else if((Options[1] >= 0d ? value >= Options[1] : true) && (Options[2] >= 0d ? value < Options[2] : false))
           saverity = "Minor";
          else
            saverity = "Normal";
        }
        //less than equal operations
        else if(operation.equals("lessThanEqual"))
        {
          //if critical value is provided so it will compare otherwise false
          if(Options[0] >= 0d ? value <= Options[0] : false)
            saverity = "Critical";
          //if critical is provided so compare otherwise true because it is severity for major not for critical
          //and if major is provided so it will compare otherwise it return false
          else if((Options[0] >= 0d ? value > Options[0] : true) && (Options[1] >= 0d ? value <= Options[1] : false))
            saverity = "Major";
          //if Major is provided so compare otherwise true because it is severity for minor not for Major
          //and if minor is provided so it will compare otherwise it return false
          else if((Options[1] >= 0d ? value > Options[1] : true) && (Options[2] >= 0d ? value <= Options[2] : false))
            saverity = "Minor";
          else
            saverity = "Normal";
        }
        //greaten than operations
        else if(operation.equals("greaterThan"))
        {
          //if critical value is present so compare otherwise move to next
          if(Options[0] >= 0d ? value > Options[0] : false)
            saverity = "Critical";
          //if critical is provided so compare otherwise true because it is severity for major not for critical
          //and if major is provided so it will compare otherwise it return false
          else if((Options[0] >= 0d ? value <= Options[0] : true) && (Options[1] >= 0d ? value > Options[1] : false)) 
            saverity = "Major";
          //if Major is provided so compare otherwise true because it is severity for minor not for Major
          //and if minor is provided so it will compare otherwise it return false
          else if((Options[1] >= 0d ? value <= Options[1] : true) && (Options[2] >= 0d ? value > Options[2] : false))
            saverity = "Minor";
          else
            saverity = "Normal";
        }
        //greater than equal operations
        else if(operation.equals("greaterThanEqual"))
        {
          //if critical value is present so compare otherwise move to next
          if(Options[0] >= 0d ? value >= Options[0] : false)
            saverity = "Critical";
          //if critical is provided so compare otherwise true because it is severity for major not for critical
          //and if major is provided so it will compare otherwise it return false
          else if((Options[0] >= 0d ? value < Options[0] : true) && (Options[1] >= 0d ? value >= Options[1] : false)) 
            saverity = "Major";
          //if Major is provided so compare otherwise true because it is severity for minor not for Major
          //and if minor is provided so it will compare otherwise it return false
          else if((Options[1] >= 0d ? value < Options[1] : true) && (Options[2] >= 0d ? value >= Options[2] : false))
            saverity = "Minor";
          else
            saverity = "Normal";
        }
      }
      if(debugLevel)
        Log.debugLog(className, "getSeverity", "", "", "return severity = " + saverity + ", value = " + value);
      return saverity;
     
    }
    catch(Exception ex)
    {
      Log.errorLog(className, "getSeverity", "", "", "Wrong field for saverity, returning NA for saverity."); 
      return saverity;
    }
  }
}

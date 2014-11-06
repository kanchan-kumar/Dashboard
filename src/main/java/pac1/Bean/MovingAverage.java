/*--------------------------------------------------------------------
  @Name    : MovingAverage.java
  @Author  : Prabhat Vashist
  @Purpose : This is to calculate moving average of specific graph by graphData
  @Modification History:
    08/24/11:Prabhat Vashist - Initial Version

----------------------------------------------------------------------*/
package pac1.Bean;

public class MovingAverage
{
  private static String className = "MovingAverage";

  private double[] arrRptData = null;
  private double[] arrRptDataBaseline = null;

  private String ruleType = "";
  private String strOperation = "";
  private double value = -1;
  private String strPctChange = "";
  private String strStartTime = "";
  private String strEndTime = "";
  private String strTimeWindow = "";
  private String strGraphNames = "";
  private String strRuleDesc = "";
  private long interval = 10000;
  private String strGraphDataIndex = "NA";
  private String strGraphDataIndexBaseline = "-1";
  private String severityOptions = "";

  private String strStartTimeStamp = "NA";
  private String strEndTimeStamp = "NA";

  private long maxSampleCount = 0;
  private String ruleId = "";
  private String ruleName = "";
  private String alertMessage = "";

  private EventGeneratorAdvisory eventGeneratorAdvisoryObj = null;

  public MovingAverage(double[] arrRptData, double[] arrRptAvgData, String ruleType, String thresholdType, String strOperation, double value, String strPctChange, String strStartTime, String strEndTime, String strTimeWindow, String strGraphNames, String strRuleDesc, int interval, EventGeneratorAdvisory eventGeneratorAdvisoryObj, String strGraphDataIndex , String severity, String ruleId , String ruleName , String alertMesssage)
  {
    this.arrRptData = arrRptData;
    this.strGraphDataIndex = strGraphDataIndex;
    this.severityOptions = severity;
    this.ruleId = ruleId;
    this.ruleName = ruleName;
    this.alertMessage = alertMesssage;
    setCommanValue(ruleType, thresholdType, strOperation, value, strPctChange, strStartTime, strEndTime, strTimeWindow, strGraphNames, strRuleDesc, interval, eventGeneratorAdvisoryObj);
  }

  //Constructor for compare
  public MovingAverage(double[] arrRptData, double[] arrRptAvgData, double[] arrRptDataBaseline, double[] arrRptAvgDataBaseline, String ruleType, String thresholdType, String strOperation, double value, String strPctChange, String strStartTime, String strEndTime, String strTimeWindow, String strGraphNames, String strRuleDesc, int interval, EventGeneratorAdvisory eventGeneratorAdvisoryObj, String strGraphDataIndex, String strGraphDataIndexBaseline, String severity,String ruleId , String ruleName , String alertMesssage)
  {
     this.arrRptData = arrRptData;
     this.arrRptDataBaseline = arrRptDataBaseline;
     this.strGraphDataIndex = strGraphDataIndex;  
     this.strGraphDataIndexBaseline = strGraphDataIndexBaseline;
     this.severityOptions = severity;   
     this.ruleId = ruleId;
     this.ruleName = ruleName;
     this.alertMessage = alertMesssage;
     setCommanValue(ruleType, thresholdType, strOperation, value, strPctChange, strStartTime, strEndTime, strTimeWindow, strGraphNames, strRuleDesc, interval, eventGeneratorAdvisoryObj);
  }
  
  public void setCommanValue(String ruleType, String thresholdType, String strOperation, double value, String strPctChange, String strStartTime, String strEndTime, String strTimeWindow, String strGraphNames, String strRuleDesc, int interval, EventGeneratorAdvisory eventGeneratorAdvisoryObj)
  {
    this.ruleType = ruleType;
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
  }
  
  public void generateMovingAverageEventsForCompare()
  {
    Log.debugLog(className, "generateMovingAverageEventsForCompare", "", "", "Method called.");

    try
    {
      // first calculate max sample count by Time Window
      calcMaxSampleCountByTimeWindow();

      double pctChange = Double.parseDouble(strPctChange);
      
      String strTimeStamp = "";

      double sum = 0;
      double sumBaseline = 0;
      double movingAverage = -1;
      double movingAverageBaseline = -1;
      int removeIndex = 0;
      int removeIndexBaseline = 0;
      long sampleTime = 0;   
      double[] tempArr = null;
      
      if(arrRptDataBaseline.length > arrRptData.length)
      {
        tempArr = arrRptData;
      }
      else
        tempArr = arrRptDataBaseline;
        
      for(int i = 0; i < tempArr.length; i++)
      {
        //System.out.println(arrRptData[i] + "  ==   " + arrRptDataBaseline[i]);
        if((i + 1) <= maxSampleCount) // If sample count is less than maxSampleCount then add all sample values
        {
          sum = sum + arrRptData[i];
          sumBaseline = sumBaseline + arrRptDataBaseline[i];

          if((i + 1) == maxSampleCount) // for first moving average
          {
            // Now calculate moving average
            movingAverage = sum/maxSampleCount;
            movingAverageBaseline = sumBaseline/maxSampleCount;
            sampleTime = (i + 1) * interval;

            strTimeStamp = eventGeneratorAdvisoryObj.getSampleTime(strStartTime, sampleTime);

            // Debug level value is greater than 0 than print it in guiDebuglog
            if((Integer.parseInt(Config.getValue("debuglevel"))) > 1)
              Log.debugLog(className, "generateMovingAverageEvents", "", "", "For graph " + strGraphNames + " Moving Average = " + movingAverage + " at " + strTimeStamp + " time.");
          }
        }
        else // If sample count is greater than maxSampleCount then first remove first sample value then add new sample value
        {
          sum = sum - arrRptData[removeIndex];
          sum = sum + arrRptData[i];
          removeIndex++;

          sumBaseline = sumBaseline - arrRptDataBaseline[removeIndexBaseline];
          sumBaseline = sumBaseline + arrRptDataBaseline[i];
          removeIndexBaseline++;
          
          // Now calculate moving average
          movingAverage = sum/maxSampleCount;
          movingAverageBaseline = sumBaseline/maxSampleCount;
          
          sampleTime = (i + 1) * interval;

          strTimeStamp = eventGeneratorAdvisoryObj.getSampleTime(strStartTime, sampleTime);

          // Debug level value is greater than 0 than print it in guiDebuglog
          if((Integer.parseInt(Config.getValue("debuglevel"))) > 1)
            Log.debugLog(className, "generateMovingAverageEventsForCompare", "", "", "For graph " + strGraphNames + " Moving Average of current TR = " + movingAverage + " and Moving Average of base line TR  = " + movingAverageBaseline + " at " + strTimeStamp + " time.");
        }   
        //System.out.println(movingAverage + " == " + movingAverageBaseline);
        if(movingAverage != -1)
        {
          double compareValue = ((movingAverage - movingAverageBaseline)/movingAverageBaseline)*100;
          
          //System.out.println(compareValue + "===" + Double.parseDouble(strPctChange));
          if((Integer.parseInt(Config.getValue("debuglevel"))) > 1)
            Log.debugLog(className, "generateMovingAverageEventsForCompare", "", "", "For graph " + strGraphNames + " Compare value = " + compareValue + " and % change = " + pctChange);         
          
         /* if(pctChange < 0)
          {
          	if(compareValue < pctChange)
            {
              if(strStartTimeStamp.equals("NA"))
                strStartTimeStamp = strTimeStamp;
            }
            else
            {
              if(!strStartTimeStamp.equals("NA"))
              {
                sampleTime = i * interval;
                strTimeStamp = eventGeneratorAdvisoryObj.getSampleTime(strStartTime, sampleTime);

                strEndTimeStamp = strTimeStamp;
                String eventData = strGraphNames + "|" + strRuleDesc + "|" + strPctChange + "|" + strStartTime + "|" + strTimeStamp + "|" + ruleType + "|NA|" + strGraphDataIndex + "|NA|NA|" + eventGeneratorAdvisoryObj.baselineTR + "|" + strGraphDataIndexBaseline + "|NA|NA|NA|NA|NA|NA";
                // Now add event
                eventGeneratorAdvisoryObj.addEventDataToFile(eventData);

                strStartTimeStamp = "NA";
                strEndTimeStamp = "NA";
              }
            }        	
          }
          else*/
          {
            if(compareValue > pctChange)
            {
              if(strStartTimeStamp.equals("NA"))
                strStartTimeStamp = strTimeStamp;
            }
            else
            {
              if(!strStartTimeStamp.equals("NA"))
              {
                sampleTime = i * interval;
                strTimeStamp = eventGeneratorAdvisoryObj.getSampleTime(strStartTime, sampleTime);

                strEndTimeStamp = strTimeStamp;
                String eventData = strGraphNames + "|" + strRuleDesc + "|" + strPctChange + "|" + strStartTimeStamp + "|" + strTimeStamp + "|" + ruleType + "|NA|" + strGraphDataIndex + "|NA|NA|" + eventGeneratorAdvisoryObj.baselineTR + "|" + strGraphDataIndexBaseline + "|" + getSeverity(compareValue, strOperation) + "|"+ruleName + "|" + ruleId + "|"+ alertMessage + "|NA|NA";
                // Now add event
                eventGeneratorAdvisoryObj.addEventDataToFile(eventData);

                strStartTimeStamp = "NA";
                strEndTimeStamp = "NA";
              }
            }
          }
        }
      }
      
     //this is case to catch the last sample time
      if(!strStartTimeStamp.equals("NA"))
      {
        sampleTime = (arrRptData.length - 1) * interval;
        strTimeStamp = eventGeneratorAdvisoryObj.getSampleTime(strStartTime, sampleTime);

        strEndTimeStamp = strTimeStamp;
        
        //String eventData = strGraphNames + "|" + strRuleDesc + " ("+ detail + ")|" + strPctChange + "|" + strStartTimeStamp + "|" + strEndTimeStamp + "|" + ruleInfo + "|NA|" + strGraphDataIndex + "|NA";
        String eventData = strGraphNames + "|" + strRuleDesc + "|" + strPctChange + "|" + strStartTimeStamp + "|" + strTimeStamp + "|" + ruleType + "|NA|" + strGraphDataIndex + "|NA|NA|" + eventGeneratorAdvisoryObj.baselineTR + "|" + strGraphDataIndexBaseline + "|"+ getSeverity(movingAverage, strOperation)+"|"+ruleName + "|" + ruleId + "|"+ alertMessage + "|NA|NA";

        // Now add event
        eventGeneratorAdvisoryObj.addEventDataToFile(eventData);

        strStartTimeStamp = "NA";
        strEndTimeStamp = "NA";
      }      
    }
    catch (Exception e) 
    {
      Log.stackTraceLog(className, "generateMovingAverageEventsForCompare", "", "", "Exception - ", e);
    }
  }
  
  // calculate moving average and generate events
  public void generateMovingAverageEvents()
  {
    Log.debugLog(className, "generateMovingAverageEvents", "", "", "Method called.");

    try
    {
      // first calculate max sample count by Time Window
      calcMaxSampleCountByTimeWindow();

      String strTimeStamp = "";

      double sum = 0;
      double movingAverage = -1;
      int removeIndex = 0;
      long sampleTime = 0;
      double Options[] = new double[3];
      
      String arrSav [] = severityOptions.split(",");
      
      for(int i = 0 ; i < arrSav.length ; i++)
      {
        Options[i] = Double.parseDouble(arrSav[i]);
      }
      
      for(int i = 0; i < arrRptData.length; i++)
      {
        if((i + 1) <= maxSampleCount) // If sample count is less than maxSampleCount then add all sample values
        {
          sum = sum + arrRptData[i];

          if((i + 1) == maxSampleCount) // for first moving average
          {
            // Now calculate moving average
            movingAverage = sum/maxSampleCount;
            sampleTime = (i + 1) * interval;

            strTimeStamp = eventGeneratorAdvisoryObj.getSampleTime(strStartTime, sampleTime);

            // Debug level value is greater than 0 than print it in guiDebuglog
            if((Integer.parseInt(Config.getValue("debuglevel"))) > 1)
              Log.debugLog(className, "generateMovingAverageEvents", "", "", "For graph " + strGraphNames + " Moving Average = " + movingAverage + " at " + strTimeStamp + " time.");
          }
        }
        else // If sample count is greater than maxSampleCount then first remove first sample value then add new sample value
        {
          sum = sum - arrRptData[removeIndex];
          sum = sum + arrRptData[i];
          removeIndex++;

          // Now calculate moving average
          movingAverage = sum/maxSampleCount;
          sampleTime = (i + 1) * interval;

          strTimeStamp = eventGeneratorAdvisoryObj.getSampleTime(strStartTime, sampleTime);

          // Debug level value is greater than 0 than print it in guiDebuglog
          if((Integer.parseInt(Config.getValue("debuglevel"))) > 1)
            Log.debugLog(className, "generateMovingAverageEvents", "", "", "For graph " + strGraphNames + " Moving Average = " + movingAverage + " at " + strTimeStamp + " time.");
        }

        // Now compare moving average with rule value and generate events
        if(movingAverage != -1)
        {
          if(strOperation.equals("lessThan"))
          {
            if(movingAverage < Options[0] || movingAverage < Options[1] || movingAverage < Options[2])
            {
              if(strStartTimeStamp.equals("NA"))
                strStartTimeStamp = strTimeStamp;
            }
            else
            {
              if(!strStartTimeStamp.equals("NA"))
              {
                sampleTime = i * interval;
                strTimeStamp = eventGeneratorAdvisoryObj.getSampleTime(strStartTime, sampleTime);

                strEndTimeStamp = strTimeStamp;
                //String eventData = strGraphNames + "|" + strRuleDesc + "|" + EventGeneratorAdvisory.convTo3DigitDecimal(value) + "|" + strStartTimeStamp + "|" + strEndTimeStamp + "|NA|NA|" + strGraphDataIndex + "|NA";
                String eventData = strGraphNames + "|" + strRuleDesc + "|" + EventGeneratorAdvisory.convTo3DigitDecimal(value) + "|" + strStartTimeStamp + "|" + strTimeStamp + "|" + ruleType + "|NA|" + strGraphDataIndex + "|NA|NA|" + eventGeneratorAdvisoryObj.baselineTR + "|" + strGraphDataIndexBaseline + "|" + getSeverity(movingAverage, strOperation)+ "|"+ruleName + "|" + ruleId + "|"+ alertMessage + "|NA|NA";
                // Now add event
                eventGeneratorAdvisoryObj.addEventDataToFile(eventData);

                strStartTimeStamp = "NA";
                strEndTimeStamp = "NA";
              }
            }
          }
          else if(strOperation.equals("lessThanEqual"))
          {
            if(movingAverage <= Options[0] || movingAverage <= Options[1] || movingAverage <= Options[2])
            {
              if(strStartTimeStamp.equals("NA"))
                strStartTimeStamp = strTimeStamp;
            }
            else
            {
              if(!strStartTimeStamp.equals("NA"))
              {
                sampleTime = i * interval;
                strTimeStamp = eventGeneratorAdvisoryObj.getSampleTime(strStartTime, sampleTime);

                strEndTimeStamp = strTimeStamp;
                //String eventData = strGraphNames + "|" + strRuleDesc + "|" + EventGeneratorAdvisory.convTo3DigitDecimal(value) + "|" + strStartTimeStamp + "|" + strEndTimeStamp + "|NA|NA|" + strGraphDataIndex + "|NA";
                String eventData = strGraphNames + "|" + strRuleDesc + "|" + EventGeneratorAdvisory.convTo3DigitDecimal(value) + "|" + strStartTimeStamp + "|" + strTimeStamp + "|" + ruleType + "|NA|" + strGraphDataIndex + "|NA|NA|" + eventGeneratorAdvisoryObj.baselineTR + "|" + strGraphDataIndexBaseline + "|" + getSeverity(movingAverage, strOperation) + "|"+ruleName + "|" + ruleId + "|"+ alertMessage + "|NA|NA";
                // Now add event
                eventGeneratorAdvisoryObj.addEventDataToFile(eventData);

                strStartTimeStamp = "NA";
                strEndTimeStamp = "NA";
              }
            }
          }
          else if(strOperation.equals("greaterThan"))
          {
            if(movingAverage > Options[0] || movingAverage > Options[1] || movingAverage > Options[2])
            {
              if(strStartTimeStamp.equals("NA"))
                strStartTimeStamp = strTimeStamp;
            }
            else
            {
              if(!strStartTimeStamp.equals("NA"))
              {
                sampleTime = i * interval;
                strTimeStamp = eventGeneratorAdvisoryObj.getSampleTime(strStartTime, sampleTime);

                strEndTimeStamp = strTimeStamp;
                //String eventData = strGraphNames + "|" + strRuleDesc + "|" + EventGeneratorAdvisory.convTo3DigitDecimal(value) + "|" + strStartTimeStamp + "|" + strEndTimeStamp + "|NA|NA|" + strGraphDataIndex + "|NA";
                String eventData = strGraphNames + "|" + strRuleDesc + "|" + EventGeneratorAdvisory.convTo3DigitDecimal(value) + "|" + strStartTimeStamp + "|" + strTimeStamp + "|" + ruleType + "|NA|" + strGraphDataIndex + "|NA|NA|" + eventGeneratorAdvisoryObj.baselineTR + "|" + strGraphDataIndexBaseline + "|" + getSeverity(movingAverage, strOperation) + "|"+ruleName + "|" + ruleId + "|"+ alertMessage + "|NA|NA";
                
                // Now add event
                eventGeneratorAdvisoryObj.addEventDataToFile(eventData);

                strStartTimeStamp = "NA";
                strEndTimeStamp = "NA";
              }
            }
          }
          else if(strOperation.equals("greaterThanEqual"))
          {
            if(movingAverage >= Options[0] || movingAverage >= Options[1] || movingAverage >= Options[2])
            {
              if(strStartTimeStamp.equals("NA"))
                strStartTimeStamp = strTimeStamp;
            }
            else
            {
              if(!strStartTimeStamp.equals("NA"))
              {
                sampleTime = i * interval;
                strTimeStamp = eventGeneratorAdvisoryObj.getSampleTime(strStartTime, sampleTime);

                strEndTimeStamp = strTimeStamp;
                //String eventData = strGraphNames + "|" + strRuleDesc + "|" + EventGeneratorAdvisory.convTo3DigitDecimal(value) + "|" + strStartTimeStamp + "|" + strEndTimeStamp + "|NA|NA|" + strGraphDataIndex + "|NA";
                String eventData = strGraphNames + "|" + strRuleDesc + "|" + EventGeneratorAdvisory.convTo3DigitDecimal(value) + "|" + strStartTimeStamp + "|" + strTimeStamp + "|" + ruleType + "|NA|" + strGraphDataIndex + "|NA|NA|" + eventGeneratorAdvisoryObj.baselineTR + "|" + strGraphDataIndexBaseline + "|" + getSeverity(movingAverage, strOperation) + "|"+ruleName + "|" + ruleId + "|"+ alertMessage + "|NA|NA";
                // Now add event
                eventGeneratorAdvisoryObj.addEventDataToFile(eventData);

                strStartTimeStamp = "NA";
                strEndTimeStamp = "NA";
              }
            }
          }
        }
      }

      // this is case to catch the last sample time
      if(!strStartTimeStamp.equals("NA"))
      {
        sampleTime = (arrRptData.length - 1) * interval;
        strTimeStamp = eventGeneratorAdvisoryObj.getSampleTime(strStartTime, sampleTime);

        strEndTimeStamp = strTimeStamp;
        //String eventData = strGraphNames + "|" + strRuleDesc + "|" + EventGeneratorAdvisory.convTo3DigitDecimal(value) + "|" + strStartTimeStamp + "|" + strEndTimeStamp + "|NA|NA|" + strGraphDataIndex + "|NA";
        String eventData = strGraphNames + "|" + strRuleDesc + "|" + EventGeneratorAdvisory.convTo3DigitDecimal(value) + "|" + strStartTimeStamp + "|" + strTimeStamp + "|" + ruleType + "|NA|" + strGraphDataIndex + "|NA|NA|" + eventGeneratorAdvisoryObj.baselineTR + "|" + strGraphDataIndexBaseline + "|" + getSeverity(movingAverage, strOperation) + "|"+ruleName + "|" + ruleId + "|"+ alertMessage + "|NA|NA";
        Log.debugLog(className, "generateThresholdEvents", "", "", "Event log = " + eventData);
        // Now add event
        eventGeneratorAdvisoryObj.addEventDataToFile(eventData);

        strStartTimeStamp = "NA";
        strEndTimeStamp = "NA";
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "generateMovingAverageEvents", "", "", "Exception - ", e);
    }
  }

  // this function is to calculate max sample count for moving average
  private void calcMaxSampleCountByTimeWindow()
  {
    Log.debugLog(className, "calcMaxSampleCountByTimeWindow", "", "", "Method called.");

    try
    {
      long timeWindow = Long.parseLong(strTimeWindow);
      maxSampleCount = (timeWindow * 1000)/interval;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "calcMaxSampleCountByTimeWindow", "", "", "Exception - ", e);
    }
  }
  
  /*
   * Critical 
   * Major
   * Minor
   */
  private String getSeverity(double value , String operation)
  {
    String saverity = "NA";
    String [] arrSav = severityOptions.split(",");
    double Options[] = new double[3];
    
    if(arrSav.length == 3)
    {
      for(int i = 0 ; i < arrSav.length ; i++)
      {
        Options[i] = Double.parseDouble(arrSav[i]);
      }
  
      if(operation.equals("lessThan") || operation.equals("lessThanEqual"))
      {
        if(value < Options[2])
          saverity = "Critical";
        else if(value >= Options[2] && value <= Options[1])
          saverity = "Major";
        else if(value >= Options[1] && value <= Options[0])
         saverity = "Minor";
      }
      else if(operation.equals("greaterThan") || operation.equals("greaterThanEqual"))
      {
        if(value > Options[0])
          saverity = "Critical";
        else if(value <= Options[0] && value >= Options[1])
          saverity = "Major";
        else if(value <= Options[1] && value >= Options[2])
          saverity = "Minor";
      }
    }
    Log.debugLogAlways(className, "getSeverity", "", "", "return severity = " + saverity);
    return saverity;
  }
}

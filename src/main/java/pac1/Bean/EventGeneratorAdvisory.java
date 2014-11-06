/*--------------------------------------------------------------------
  @Name    : EventGeneratorAdvisory.java
  @Author  : Prabhat Vashist
  @Purpose : This is main class to generate analysis alerts on the basis of rules
  @Modification History:
    08/24/11:Prabhat Vashist - Initial Version
    06/06/13: Ravi Kant Sharma - Add feature Check Rule and change design for event generation
----------------------------------------------------------------------*/

package pac1.Bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class EventGeneratorAdvisory
{
  //logging
  private final String className = "EventGeneratorAdvisory";
  //Advisory Rule file modification time  
  private long narFileModificationDate = 0L;
  //test run number
  private int testRunNum = -1;
  //advisory rule file name it is .nar file
  private String ruleFileName = "";
  //rule mode 0 - only threshold, 1 - only compare, 2 - both
  private int ruleMode = 2;
  //start time of Alert generation duration 
  private String startTimeForRule = "";
  //End time of Alert generation duration 
  private String endTimeForRule = "";
  //Base Line Test run Number
  protected int baselineTR = -1;
  
  //Check for OnDemand mode
  private boolean OnDemandMode = false;
  //rule names for On demand Alert 
  private String[] arrRuleNames = null;
  //for product recognition 
  private boolean isNDE = false;
  
  //Map for Thresholds
  public Map hashMapThreshold = null;
  //List for advisory alert status for current interval
  private List<AdvisoryAlertStatus> alertStatus = new ArrayList<AdvisoryAlertStatus>();
  //List of all alert present in alert status file
  public List<AdvisoryAlertStatus> allAlertLine = new ArrayList<AdvisoryAlertStatus>();
  
  //double array for GraphData
  private double arrGraphData[][] = null;
  //double array for Average data values
  private double arrAvgDataValuesAll[][] = null;
  private double arrGraphDataCmp[][] = null; // It store baseline test run data
  private double arrAvgDataValuesAllCmp[][] = null; // It store baseline test run
  
  //alert history scheduler file name 
  private final String ALERT_HISTORY_SCHEDULAR_FILE_NAME = "alert_history_scheduler.dat";
  //alert history on demand file name 
  private final String ALERT_HISTORY_ONDEMAND_FILE_NAME = "alert_history_ondemand.dat";
  //alert status file name 
  private final String ALERT_STATUS_FILE_NAME = "alert_status.dat";
  
  //FileOutPutStream and PrintStream for on demand alert generation  
  private FileOutputStream foutOnDemand = null;
  private PrintStream printSteamOnDemand = null;
  //FileOutPutStream and PrintStream for all alert generation that goes in history 
  private FileOutputStream fout = null;
  private PrintStream printStream = null;
  //FileOutPutStream and PrintStream for alert status
  private FileOutputStream foutAlertStatus = null;
  private PrintStream printSteamAlertStatus = null;
 
  //Report Data for compare rule feature
  public ReportData rptData = null;
  public ReportData rptDataCompare = null; // for base line test run

  //message for Success, Failure, Error 
  private String SUCCESS = "SUCCESS|Analysis alert generated successfully.";
  private String FAILURE = "FAILURE|No event is generated.";
  private String ERROR = "ERROR|Error occur while generating analysis alert, Please see error logs for more details.";
  
  //debug Level for logging 
  private boolean debugLevel = false;

  /**
   * Ravi - default constructor, it is called from Server.java
   */
  public EventGeneratorAdvisory()
  {
    //set debug level
    setDebugLevel(Config.getValue("debugFlag"));
    if(debugLevel) 
      Log.debugLog(className, "", "", "", "default constructor Called.");
    //set product mode
    setProductMode();
  }

  /**
   * Constructor for On Demand Alert generation 
   * @param testRunNum
   * @param ruleFileName
   * @param ruleMode
   * @param startTimeForRule
   * @param endTimeForRule
   * @param baselineTR
   */
  public EventGeneratorAdvisory(int testRunNum, String ruleFileName, int ruleMode, String startTimeForRule, String endTimeForRule, int baselineTR)
  {
    if(debugLevel) 
      Log.debugLog(className, "", "", "", "constructor Called.");
    this.testRunNum = testRunNum;
    this.ruleFileName = ruleFileName;
    this.ruleMode = ruleMode; // mode 0 - only threshold, 1 - only compare, 2 - both
    this.startTimeForRule = startTimeForRule; // start and end time
    this.endTimeForRule = endTimeForRule;
    this.baselineTR = baselineTR; // base line tr

    // If baseline tr is not present it will show only threshold rules
    if (baselineTR == -1)
      this.ruleMode = 0;
    
    setProductMode();
  }

  /************************************Start file stream close and open operation methods ********************************************/
  
  /**
   * Get The full path of alert file according to product and operation whether is is onDenamd or history
   * @return
   */
  private String getAnalysisAlertFileNameWithPath()
  {
    Log.debugLogAlways(className, "getAnalysisAlertFileNameWithPath", "", "", "isNDE = " + isNDE + ", OnDemandMode = " + OnDemandMode);
    if (isNDE)
    {
      if (OnDemandMode)
        return (Config.getWorkPath() + "/webapps/logs/advisoryRules/" + ALERT_HISTORY_ONDEMAND_FILE_NAME);
      else
        return (Config.getWorkPath() + "/webapps/logs/advisoryRules/" + ALERT_HISTORY_SCHEDULAR_FILE_NAME);
    }
    else
    {
      if (OnDemandMode)
        return (Config.getWorkPath() + "/webapps/logs/TR" + testRunNum + "/" + ALERT_HISTORY_ONDEMAND_FILE_NAME);
      else
        return (Config.getWorkPath() + "/webapps/logs/TR" + testRunNum + "/" + ALERT_HISTORY_SCHEDULAR_FILE_NAME);
    }
  }
  
  /**
   * Ravi - return the alert_status.dat file path
   */
  private String getAlertStatusFileNameWithPath()
  {
    if (isNDE)
      return Config.getWorkPath() + "/webapps/logs/advisoryRules/" + ALERT_STATUS_FILE_NAME;
    else
      return (Config.getWorkPath() + "/webapps/logs/TR" + testRunNum + "/" + ALERT_STATUS_FILE_NAME);
  }

  /**
   * Close PrintStream and FileOutPutStream for alert history file 
   * @return
   */
  private boolean closeAlertStausAndHistoryFileStreams()
  {
    if(debugLevel) 
      Log.debugLog(className, "closeAlertStausAndHistoryFileStreams", "", "", "Method Called");
    
    try
    {
      //printStream and fout for alert history
      if (printStream != null)
      {
        printStream.close();
        printStream = null;
      }
      if (fout != null)
      {
        fout.close();
        fout = null;
      }
      
      //FileOutPutStream and PrintStream for alert status
      if(foutAlertStatus != null)
      {
        foutAlertStatus.close();
        foutAlertStatus = null;
      } 
      if(printSteamAlertStatus != null)
      {
        printSteamAlertStatus.close();
        printSteamAlertStatus = null;
      }

      return true;
    }
    catch (java.io.IOException e)
    {
      Log.stackTraceLog(className, "closeAlertStausAndHistoryFileStreams", "", "", "Exception - ", e);
      return false;
    }
  }

  /**
   * Open the printStream and FileOutPutStream for alert status file
   * if file is not present so it create the alert status file
   * @param fileNameWithPath
   * @return
   */
  private boolean openAlertStatusFile(String fileNameWithPath)
  {
    if(debugLevel) 
      Log.debugLog(className, "openAlertStatusFile", "", "", "Method Called, file = " + fileNameWithPath);
    try
    {
      String dirName = getAlertStatusDir();
      File fileDir = new File(dirName);
      if(!fileDir.exists())
        fileDir.mkdirs();

      if (isNDE && !rptUtilsBean.changeFilePerm(dirName, "netstorm", "netstorm", "775"))
        Log.errorLog(className, "openAlertStatusFile", "", "", "Error in change permission for dir - " + dirName);

      File analysisAlertFileObj = new File(fileNameWithPath);
      if (analysisAlertFileObj.exists())
      {
        if(debugLevel) 
          Log.debugLog(className, "openAlertStatusFile", "", "", "file = " + fileNameWithPath + " already exist.");
      }
      else
        analysisAlertFileObj.createNewFile();

      if (!rptUtilsBean.changeFilePerm(fileNameWithPath, "netstorm", "netstorm", "775"))
        Log.errorLog(className, "openAlertStatusFile", "", "", "Error in change permission for file - " + fileNameWithPath);

      foutAlertStatus = new FileOutputStream(analysisAlertFileObj);
      printSteamAlertStatus = new PrintStream(foutAlertStatus);

      if(debugLevel) 
        Log.debugLog(className, "openAlertStatusFile", "", "", "alert_status.dat file successfully created");
      
      return true;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "openAlertStatusFile", "", "", "fileNameWithPath = " + fileNameWithPath);
      Log.stackTraceLog(className, "openAlertStatusFile", "", "", "Exception - ", e);
      return false;
    }
  }

  /**
   * Ravi - This function returns the alert_status.dat dir path
   * @return
   */
  private String getAlertStatusDir()
  {
    if (isNDE)
      return Config.getWorkPath() + "/webapps/logs/advisoryRules";
    else
      return (Config.getWorkPath() + "/webapps/logs/TR" + testRunNum);
  }

  /**
   * Open the printStream and FileOutPutStream for alert on demand  file
   * if file is not present so it create the alert on demand file and put header
   * @param fileNameWithPath
   * @return
   */
  private boolean openOnDemandFile(String fileNameWithPath)
  {
    if(debugLevel) 
      Log.debugLog(className, "openOnDemandFile", "", "", "Method Called, file = " + fileNameWithPath);
    try
    {
      File analysisAlertFileObj = new File(fileNameWithPath);
      if (analysisAlertFileObj.exists())
      {
        foutOnDemand = new FileOutputStream(analysisAlertFileObj, true);
        printSteamOnDemand = new PrintStream(foutOnDemand);
        if(debugLevel) 
          Log.debugLog(className, "openOnDemandFile", "", "", "File is already exist at path = " + fileNameWithPath);
      }
      else
      {
        analysisAlertFileObj.createNewFile();
        if(debugLevel) 
          Log.debugLog(className, "openOnDemandFile", "", "", "File successfully created at path =  " + fileNameWithPath);
        foutOnDemand = new FileOutputStream(analysisAlertFileObj, true);
        printSteamOnDemand = new PrintStream(foutOnDemand);
        String headerLine = "#Graph Name|desc|value|Start Time|End Time|Rule Type|NA|Graph Index|NA|NA|-1|-1|Saverity|desc|Rule Id|desc|alert time|NA";
        printSteamOnDemand.println(headerLine);
        printSteamOnDemand.flush();
      }

      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "openOnDemandFile", "", "", "Exception - ", e);
      return false;
    }
  }

  /**
   * Open file and return as File object for writing alert history file
   * @param fileNameWithPath
   * @return
   */
  private boolean openHistoryFileStreams(File analysisAlertFileObj)
  {
    if(debugLevel) 
      Log.debugLog(className, "openHistoryFileStreams", "", "", "Method Called");
    try
    {
      if (analysisAlertFileObj.exists())
      {
        if(debugLevel) 
          Log.debugLog(className, "openHistoryFileStreams", "", "", "File already exist at path = " + ALERT_HISTORY_SCHEDULAR_FILE_NAME);
      }
      else
      {
        analysisAlertFileObj.createNewFile();
        if(debugLevel) 
          Log.debugLog(className, "openHistoryFileStreams", "", "", "File successfully created = " + ALERT_HISTORY_SCHEDULAR_FILE_NAME);
      }

      fout = new FileOutputStream(analysisAlertFileObj, true);
      printStream = new PrintStream(fout);

      return true;
    }
    catch (java.io.IOException iOE)
    {
      Log.stackTraceLog(className, "openFileStreams", "", "", "Exception - ", iOE);
      return false;
    }
  }

  /*
   * Ravi - This function check if nar file is modified by user, if yes then do
   * process otherwise process and generate file
   * @param filePath
   */
  private boolean isNarFileModified(String filePath)
  {
    if(debugLevel) 
      Log.debugLog(className, "isNarFileModified", "", "", "Method Called. filePath = " + filePath);
    File file = new File(filePath);
    long lastModifiedDate = file.lastModified();
    if(debugLevel) 
      Log.debugLog(className, "isNarFileModified", "", "", "narFileModificationDate = " + narFileModificationDate + ", lastModifiedDate = " + lastModifiedDate);
    if(narFileModificationDate == lastModifiedDate)
    {
      return false;
    }
    else
    {
      //assign the NAR file last modification time  to instance variable narFileModificationDate 
      narFileModificationDate = lastModifiedDate;
      return true;
    }
  }
  
  /************************************End file stream close and open operation methods ****************************************/
  
  /*********************************** Start of util methods********************************************************************/
  /**
   * Ravi - This function returns the alert time
   * @return alert time
   * Ravi -  It will not work on windows machine.
   */
  private String getAlertTime()
  {
    try
    {
      if(debugLevel) 
        Log.debugLog(className, "getAlertTime", "", "", "Method Called.");
      CmdExec cmdExec = new CmdExec();
      String command = "date";
      String args = " '+%m/%d/%y %T'";
      Vector result = cmdExec.getResultByCommand(command, args, CmdExec.SYSTEM_CMD, "netstorm", "root");
      String nsServerDateTime = result.get(0).toString();
      return nsServerDateTime;
    }
    catch (Exception ex)
    {
      Log.errorLog(className, "getAlertTime", "", "", "Exception - " + ex);
      return "00/00/00 00:00:00";
    }
  }
  
  /**
   * Convert the value in to three place of decimal
   * @param val
   * @return
   */
  public static double convTo3DigitDecimal(double val)
  {
    double dblVal;
    dblVal = (double) Math.round(val * 1000);
    dblVal = dblVal / 1000;
    return (dblVal);
  }
  
  /**
   * this function is to get the sample time relative to rule start time
   * @param strStartTime
   * @param sampleTime
   * @return
   */
  public String getSampleTime(String strStartTime, long sampleTime)
  {
    if(debugLevel) 
      Log.debugLog(className, "getSampleTime", "", "", "Method called. Start Time = " + strStartTime + ", Sample Time = " + sampleTime);
    try
    {
      if (!strStartTime.equals("NA"))
      {
        long startTime = rptUtilsBean.convStrToMilliSec(strStartTime);
        long sampleElapsedTime = startTime + sampleTime;

        return (rptUtilsBean.convMilliSecToStr(sampleElapsedTime));
      }
      else
        return (rptUtilsBean.convMilliSecToStr(sampleTime));
    }
    catch (NullPointerException e)
    {
      Log.stackTraceLog(className, "getSampleTime", "", "", "Exception - ", e);
      return "NA";
    }
  }

  /**
   * To prepare the pipe separated String from String Array
   * @param arrString
   * @return
   */
  public String arrToString(String arrString[])
  {
    StringBuffer strTmp = new StringBuffer(arrString[0]);
    for (int recNum = 1; recNum < arrString.length; recNum++)
      strTmp.append("|" + arrString[recNum]);
    return (strTmp.toString());
  }

  /**
   * To replace the index keyword in alert message by vector name 
   * @param dataLine
   * @return
   */
  private String changeAlertMessage(String dataLine)
  {
    try
    {
      if(debugLevel) 
        Log.debugLog(className, "changeAlertMessage", "", "", "Data line = " + dataLine);
      String[] arrDataLine = rptUtilsBean.strToArrayData(dataLine, "|");
      if(arrDataLine[12].equals("NA"))
        arrDataLine[12] = "Normal";
      
      // at 16 th index, assign alert time
      arrDataLine[16] = getAlertTime();
      dataLine = arrToString(arrDataLine);
      String graphNameWithVector = arrDataLine[0].trim();
      int lastIndex = graphNameWithVector.indexOf("-");
      String vectorName = graphNameWithVector.substring((lastIndex + 1), graphNameWithVector.length()).trim();

      String alertMessage = arrDataLine[15].trim();
      if(debugLevel) 
        Log.debugLog(className, "changeAlertMessage", "", "", "vectorName = " + vectorName + ", alertMessage = " + alertMessage);
      String searchKeyword = "{index}";
      if (alertMessage.indexOf(searchKeyword) != -1)
      {
        dataLine = dataLine.replace(searchKeyword, vectorName);
        if(debugLevel) 
          Log.debugLog(className, "changeAlertMessage", "", "", "after replacing dataLine = " + dataLine);
        return dataLine;
      }
      else
      {
        if(debugLevel) 
          Log.debugLog(className, "changeAlertMessage", "", "", "No change in data line.");
        return dataLine;
      }
    }
    catch(ArrayIndexOutOfBoundsException aIE)
    {
      Log.stackTraceLog(className, "changeAlertMessage", "", "", "Array Index out of bounds Exception - ", aIE);
      return dataLine;
    }
    catch(NullPointerException nPE)
    {
      Log.stackTraceLog(className, "changeAlertMessage", "", "", "Null pointer Exception - ", nPE);
      return dataLine;
    }
  }
  
  /**
   * Set debug level
   * @param debugValue
   */
  public void setDebugLevel(String debugValue)
  {
    if(debugValue != null && !debugValue.equals("NA"))
    {
      if(debugValue.equals("on"))
        debugLevel= true;
      else 
        debugLevel= true;
    }
  }
  
  /**
   * Get debug Level 
   * @param
   * @return
   */
  public boolean getDebugLevel()
  {
    return debugLevel;
  }
  
  /**
   * 
   */
  
  /********************************** End of util Methods **********************************************************/

  /**
   * This method load the all alert status in memory
   */
  public void loadAlertStatus()
  {
    if(debugLevel)
      Log.debugLog(className, "loadAlertStatus", "", "", "method called");
    //alert status file  
    String alertStatusFile = getAlertStatusFileNameWithPath();

    File fileName = new File(alertStatusFile);

    if(!fileName.exists())
    {
      Log.debugLog(className, "loadAlertStatus", "", "", "File " + alertStatusFile + " does not exist.");
    }
    else
    {
      FileInputStream fis = null;
      ;
      try
      {
        fis = new FileInputStream(fileName);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));

        String strLine = "";
        while((strLine = br.readLine()) != null)
        {
          if(strLine.length() == 0 || strLine.startsWith("#"))
            continue;
          else
            //add alert to list
            addAlertStatusToList(strLine);
        }
        br.close();
        fis.close();
      }
      catch(FileNotFoundException fNFE)
      {
        Log.stackTraceLog(alertStatusFile, "loadAlertStatus", "", "", "Alert status file does not exist", fNFE);
      }
      catch(IOException iOE)
      {
        Log.stackTraceLog(alertStatusFile, "loadAlertStatus", "", "", "Exception in reading Alert status file", iOE);
      }//end of loop
    }
  }
  
  /**
   * Add str line to list in AdvisoryAlertStatus object
   * Example of strLine - TestRun|Rule ID|Rule Name|Severity|Alert Time|Alert Msg|value|
   * StartTime|EndTime|ruleType|GraphNames|GraphDataIndex|BaseLineTR|GraphDataIndexBaseline|
   * RuleDesc|future1|future2|future3|future4|future5|future6
   * @param strLine
   */
  private void addAlertStatusToList(String strLine)
  {
    if(debugLevel)
      Log.debugLog(className, "addAlertStatusToList", "", "", "method called strLine = " + strLine);
    
    //Spiting data line by pipe
    String[] arrDataLine = rptUtilsBean.strToArrayData(strLine, "|");
    
    //Initialling the advisory alert status object 
    AdvisoryAlertStatus advisoryAlertStatus = new AdvisoryAlertStatus();
    
    //setting test run number
    advisoryAlertStatus.setTestRun(arrDataLine[0]);
    //rule ID
    advisoryAlertStatus.setRuleId(arrDataLine[1]);
    //rule name
    advisoryAlertStatus.setRuleName(arrDataLine[2]);
    //severity
    advisoryAlertStatus.setSeverity(arrDataLine[3]);
    //alert time
    advisoryAlertStatus.setAlertTime(arrDataLine[4]);
    //alert message
    advisoryAlertStatus.setAlertMsg(arrDataLine[5]);
    //value
    advisoryAlertStatus.setValue(arrDataLine[6]);
    //start time
    advisoryAlertStatus.setStartTime(arrDataLine[7]);
    //end time
    advisoryAlertStatus.setEndTime(arrDataLine[8]);
    //rule type
    advisoryAlertStatus.setRuleType(arrDataLine[9]);
    //Graph name
    advisoryAlertStatus.setGraphNames(arrDataLine[10]);
    //Graph data index
    advisoryAlertStatus.setGraphDataIndex(arrDataLine[11]);
    //base line test run
    advisoryAlertStatus.setBaseLineTR(arrDataLine[12]);
    //GraphData index for base line test run
    advisoryAlertStatus.setGraphDataIndexBaseline(arrDataLine[13]);
    //rule description
    advisoryAlertStatus.setRuleDesc(arrDataLine[14]);
    
    if(debugLevel) 
      Log.debugLog(className, "addAlertStatusLine", "", "", "ading data in allAlertLine = " + strLine);
    //add alert to list
    allAlertLine.add(advisoryAlertStatus);
    
  }

  /**
   *  This function add alert status line into the List of alert status 
   *  if its severity changes or new alert so it will write to alert status file and history file
   *  Example - dataline
   *  strGraphNames + "|" + strRuleDesc + "|" + average + "|" + strStartTime + "|" + strTimeStamp + "|" + ruleType + "|NA|" + 
   *  strGraphDataIndex + "|NA|NA|" + eventGeneratorAdvisoryObj.baselineTR + "|" + strGraphDataIndexBaseline + "|" + 
   *  getSeverity(average, strOperation)+ "|"+ruleName + "|" + ruleId + "|"+ alertMessage + "|NA|NA";
   * @param vectorData
   * @param dataLine
   * @return
   */
  private boolean addAlertStatusLine(String dataLine)
  {
    try
    {
      if(debugLevel) 
        Log.debugLog(className, "addAlertStatusLine", "", "", "Method called. dataLine = " + dataLine);

      //Spiting data line by pipe
      String[] arrDataLine = rptUtilsBean.strToArrayData(dataLine, "|");
      //Initialling the advisory alert status object 
      AdvisoryAlertStatus advisoryAlertStatus = new AdvisoryAlertStatus();
      
      //setting test run number
      advisoryAlertStatus.setTestRun("" + testRunNum);
      //Graph name
      advisoryAlertStatus.setGraphNames(arrDataLine[0]);
      //Rule description
      advisoryAlertStatus.setRuleDesc(arrDataLine[1]);
      //value
      advisoryAlertStatus.setValue(arrDataLine[2]);
      //start time
      advisoryAlertStatus.setStartTime(arrDataLine[3]);
      //end time
      advisoryAlertStatus.setEndTime(arrDataLine[4]);
      //rule type
      advisoryAlertStatus.setRuleType(arrDataLine[5]);
      //Graph data index
      advisoryAlertStatus.setGraphDataIndex(arrDataLine[7]);
      //base line test run
      advisoryAlertStatus.setBaseLineTR(arrDataLine[10]);
      //GraphData index for base line test run
      advisoryAlertStatus.setGraphDataIndexBaseline(arrDataLine[11]);
      //alert time
      advisoryAlertStatus.setAlertTime(getAlertTime());
      //severity
      advisoryAlertStatus.setSeverity(arrDataLine[12]);
      //rule name
      advisoryAlertStatus.setRuleName(arrDataLine[13]);
      //rule ID
      advisoryAlertStatus.setRuleId(arrDataLine[14]);
      //alert message
      advisoryAlertStatus.setAlertMsg(arrDataLine[15]);
      
      if(debugLevel) 
        Log.debugLog(className, "addAlertStatusLine", "", "", "ading data in alert status dataLine = " + dataLine);
      //add alert to list
      alertStatus.add(advisoryAlertStatus);
      
      return true;
    }
    catch(ArrayIndexOutOfBoundsException aIE)
    {
      Log.stackTraceLog(className, "addAlertStatusLine", "", "", "Array Index out of bounds Exception - ", aIE);
      return false;
    }
    catch(NullPointerException nPE)
    {
      Log.stackTraceLog(className, "addAlertStatusLine", "", "", "Null pointer Exception - ", nPE);
      return false;
    }
  }

  /**
   * this is to generate events by rules type
   * @param arrRptData
   * @param arrRptAvgData
   * @param advisoryRuleInfoObj
   */
  private void generatesEventByRules(double[] arrRptData, double[] arrRptAvgData, AdvisoryRuleInfo advisoryRuleInfoObj)
  {
    if(debugLevel) 
      Log.debugLog(className, "generatesEventByRules", "", "", "Method called.");
    try
    {
      String ruleType = advisoryRuleInfoObj.getRuleType();
      String thresholdType = advisoryRuleInfoObj.getThresholdType();
      String strOperation = advisoryRuleInfoObj.getOperation();
      String ruleId = advisoryRuleInfoObj.getRuleId();
      String alertMessage = advisoryRuleInfoObj.getAlertMessage();
      String ruleName = advisoryRuleInfoObj.getRuleName();

      // value contains 3 digits comma separator like 3,5,6
      String severityOpt = advisoryRuleInfoObj.getValue();
      String[] arrDataValues = severityOpt.split(",");
      String SevereVal = arrDataValues[0];
      
      if(SevereVal.equalsIgnoreCase("NA"))
        SevereVal = arrDataValues[1];
      if(SevereVal.equalsIgnoreCase("NA"))
        SevereVal = arrDataValues[2];
      
      double value = Double.parseDouble(SevereVal);
      String strPctChange = advisoryRuleInfoObj.getPctChange();
      String strTimeWindow = advisoryRuleInfoObj.getTimeWindow();
      String strGraphNames = advisoryRuleInfoObj.getStrGraphName();
      String strRuleDesc = advisoryRuleInfoObj.getDescription();
      int interval = advisoryRuleInfoObj.getInterval();
      int graphDataIndex = advisoryRuleInfoObj.getGraphDataIndex();
      if (thresholdType.equals("MovingAverage"))
      {
        MovingAverage movingAverageObj = new MovingAverage(arrRptData, arrRptAvgData, ruleType, thresholdType, strOperation, value, strPctChange, startTimeForRule, endTimeForRule, strTimeWindow, strGraphNames, strRuleDesc, interval, this, graphDataIndex + "", severityOpt, ruleId, ruleName, alertMessage);
        movingAverageObj.generateMovingAverageEvents();
      }
      else if (thresholdType.equals("Average"))
      {
        Threshold thresholdObj = new Threshold(arrRptData, arrRptAvgData, ruleType, thresholdType, strOperation, value, strPctChange, startTimeForRule, endTimeForRule, strTimeWindow, strGraphNames, strRuleDesc, interval, this, graphDataIndex + "", severityOpt, ruleId, ruleName, alertMessage);
        thresholdObj.generateThresholdEvents();
      }
      else
      {
        if(debugLevel) 
          Log.debugLog(className, "generatesEventByRules", "", "", "Rule type not supported, Rule Type = " + ruleType + ", for graph name = " + strGraphNames);
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "generatesEventByRules", "", "", "Exception - ", e);
    }
  }

  /**
   * Ravi - This function add dataLine to on demand file
   * @param dataLine
   */
  private void addDataToOnDemandFile(String dataLine)
  {
    if(debugLevel) 
      Log.debugLog(className, "addDataToOnDemandFile", "", "", "Method Called. Going to add data line - " + dataLine);
    if(foutOnDemand == null || printSteamOnDemand == null)
      openOnDemandFile(getAnalysisAlertFileNameWithPath()); 

    printSteamOnDemand.println(dataLine);
    printSteamOnDemand.flush();

  }

  /**
   * This method called from Threshold and Moving Average 
   * To add Event in file for alert status and history or on demand file
   * @param eventData
   */
  public synchronized void addEventDataToFile(String eventData)
  {
    if(debugLevel) 
      Log.debugLog(className, "addEventDataToFile", "", "", "Method called, Event Data = " + eventData + ", OnDemandMode = " + OnDemandMode);
    try
    {
      //change alert message for vector graph or group it replace the index by it's name 
      eventData = changeAlertMessage(eventData);
      
      if (OnDemandMode)
      {
        addDataToOnDemandFile(eventData);
      }
      else
      {
        //add  alert status line in alert status
        boolean newAlertAddInList = addAlertStatusLine(eventData); 
        if(debugLevel) 
          Log.debugLog(className, "addEventDataToFile", "", "", "Alert generate. newAlertAddInList = " + newAlertAddInList);
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "addEventDataToFile", "", "", "Exception - ", e);
    }
  }
  
  /**
   * Add alert in history file 
   * @param
   * @return
   */
  private void addAlertInHistoryFile(String eventData)
  {
    if(debugLevel)
      Log.debugLog(className, "addAlertInHistoryFile", "", "", "method called");
    
    //if history file stream is null so creating new stream
    if ((printStream == null) || (fout == null))
    {
      String historyFileWithPath = getAnalysisAlertFileNameWithPath();
      File historyFileObj = new File(historyFileWithPath); 
      if (!openHistoryFileStreams(historyFileObj))
      {
        Log.errorLog(className, "addAlertInHistoryFile", "", "", "Error in opening history file stream = " + historyFileWithPath);
        return;
      }
      else
      {
        //if history file length is zero so need to add header
        if(historyFileObj.length() == 0L)
        {
          // Adding header line
          String headerLine = "GraphNames|RuleDesc|Average|StartTime|TimeStamp|ruleType|NA|GraphDataIndex|NA|NA|BaselineTR|GraphDataIndexBaseline|Severity|RuleName|RuleId|AlertMessage|Alert Time|TestRun";
   
          printStream.println(headerLine);
        }
      }
    }
        
    //add event data to history file
    printStream.println(eventData);
    printStream.flush();
    if(debugLevel) 
      Log.debugLog(className, "addAlertInHistoryFile", "", "", "data = " + eventData + " is written to history sucessfully.");
   
  }
  
  /**
   * This function generate alert_status.dat file
   * and add alert in history file
   * 
   */
  private void genAlertStatusFile()
  {
    try
    {
      if(debugLevel) 
        Log.debugLog(className, "genAlertStatusFile", "", "", "Method Called.");
      
      //checking alert status size if no alert is generated so it will not update alert files
      if (alertStatus.size() == 0)
      {
        if(debugLevel) 
          Log.debugLog(className, "genAlertStatusFile", "", "", "No alert is generated.");
        return;
      }
      
      //check foe new alert generation
      boolean newAlerts = checkNewAlertGenertedOrSeverityChanged();
      if(newAlerts)
        writeAlertStatusLine();    

    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "genAlertStatusFile", "", "", "Exception - ", e);
    }
  }
  
  /**
   * To check for new alerts or alert severity changes
   * @return
   */
  private boolean checkNewAlertGenertedOrSeverityChanged()
  {
    if(debugLevel)
      Log.debugLog(className, "checkNewAlertGenertedOrSeverityChanged", "", "", "methosd called");
    
    //flag for new alert or severity change
    boolean newAlertOrServerityChange = false;
    //iterating the alert status size
    for (int i = 0; i < alertStatus.size(); i++)
    {
      AdvisoryAlertStatus advisoryAlertStatus = (AdvisoryAlertStatus) alertStatus.get(i);
      
      //alert line index in list
      int alertLineIndex = -1;
      //checking the index of alert in list 
      alertLineIndex = isAlertStatusLineExistInList(advisoryAlertStatus);

      //same alert
      if(alertLineIndex == 0)
      {
        if(debugLevel)
          Log.debugLog(className, "checkNewAlertGenertedOrSeverityChanged", "", "", "");
          
      }
      //new alert
      else if(alertLineIndex == -1)
      {
        if(debugLevel) 
          Log.debugLog(className, "checkNewAlertGenertedOrSeverityChanged", "", "", "new alert is generated.");
        
        //adding alert in list and history
        
        boolean normalFlag = addNewAlertInListAndHistory(advisoryAlertStatus);
        
        //make alert or severity change flag true
        newAlertOrServerityChange = normalFlag;
      }
      //Alert severity change
      else
      {
        if(debugLevel) 
          Log.debugLog(className, "checkNewAlertGenertedOrSeverityChanged", "", "", "severity change = " + advisoryAlertStatus.getRuleName());
        //add severity change alert in list
        addSeverityChangeAlert(alertLineIndex, advisoryAlertStatus);
        //make alert or severity change flag true
        newAlertOrServerityChange = true;
      }
    }//end of loop
    
    return newAlertOrServerityChange;
  }
  
  /**
   * Add alert in list and history
   * @param advisoryAlertStatus
   * @return
   */
  private boolean addNewAlertInListAndHistory(AdvisoryAlertStatus advisoryAlertStatus)
  {
    if(debugLevel)
      Log.debugLog(className, "addNewAlertInListAndHistory", "", "", "method called");
      
    //adding in to all alert line
    allAlertLine.add(advisoryAlertStatus);
    //sending mail 
    if(!advisoryAlertStatus.getSeverity().equals("Normal"))
    {
      if(debugLevel) 
        Log.debugLog(className, "checkNewAlertGenertedOrSeverityChanged", "", "", "Send mail for rule, advisoryAlertStatus = " + advisoryAlertStatus.getRuleName());
      sendAlertMail(advisoryAlertStatus);
    
      //writing in to history file
      String alertHistoryLine = createAlertHistoryLine(advisoryAlertStatus);
      addAlertInHistoryFile(alertHistoryLine);
      return true;
    }
    return false;
  }
  
  /**
   * Add alert severity change alert in history
   * and update the list
   * @param advisoryAlertStatus
   * @param alertLineIndex
   */
  private void addSeverityChangeAlert(int alertLineIndex, AdvisoryAlertStatus advisoryAlertStatus)
  {
    if(debugLevel)
      Log.debugLog(className, "addSeverityChangeAlert", "", "", "method called");
    //adding in to all alert line
    allAlertLine.remove(alertLineIndex);
    //adding in to all alert line
    allAlertLine.add(alertLineIndex, advisoryAlertStatus);
    
    if(debugLevel) 
      Log.debugLog(className, "checkNewAlertGenertedOrSeverityChanged", "", "", "severity change Send mail for rule, advisoryAlertStatus = " + advisoryAlertStatus.getRuleName());
    sendAlertMail(advisoryAlertStatus);
   
    //writing in to history file
    String alertHistoryLine = createAlertHistoryLine(advisoryAlertStatus);
    addAlertInHistoryFile(alertHistoryLine);
    
  }
  
  /**
   * Create alert entry for history file
   * Example - GraphNames|RuleDesc|Average|StartTime|TimeStamp|ruleType|NA|
   *           GraphDataIndex|NA|NA|BaselineTR|GraphDataIndexBaseline|Severity|RuleName|RuleId|AlertMessage|Alert Time|TestRun
   * @param advisoryAlertStatus
   * @return
   */
  private String createAlertHistoryLine(AdvisoryAlertStatus advisoryAlertStatus)
  {
    if(debugLevel)
      Log.debugLog(className, "createAlertHistoryLine", "", "", "method called");
    
    StringBuilder strBuilder = new StringBuilder();
    //creating alert line for history
    
    //graph Name
    strBuilder.append(advisoryAlertStatus.getGraphNames() + "|");
    //rule description
    strBuilder.append(advisoryAlertStatus.getRuleDesc() + "|");
    //value
    strBuilder.append(advisoryAlertStatus.getValue() + "|");
    //start time
    strBuilder.append(advisoryAlertStatus.getStartTime() + "|");
    //end time
    strBuilder.append(advisoryAlertStatus.getEndTime() + "|");
    //rule type
    strBuilder.append(advisoryAlertStatus.getRuleType() + "|");
    
    //future field 
    strBuilder.append("NA|");
    
    //graph Data index
    strBuilder.append(advisoryAlertStatus.getGraphDataIndex() + "|");
    
    //future field 
    strBuilder.append("NA|NA|");
    
    //base line TR
    strBuilder.append(advisoryAlertStatus.getBaseLineTR() + "|");
    //base line test run graph data index
    strBuilder.append(advisoryAlertStatus.getGraphDataIndexBaseline() + "|");
    //Severity
    strBuilder.append(advisoryAlertStatus.getSeverity() + "|");
    //rule name
    strBuilder.append(advisoryAlertStatus.getRuleName() + "|");
    //rule Id
    strBuilder.append(advisoryAlertStatus.getRuleId() + "|");
    //alert msg
    strBuilder.append(advisoryAlertStatus.getAlertMsg() + "|");
    //alert time
    strBuilder.append(advisoryAlertStatus.getAlertTime() + "|");
    //test run
    strBuilder.append(advisoryAlertStatus.getTestRun());   
   
    return strBuilder.toString();
  }

  
  /**
   * To check alert exist in list or not
   * for new alert it return -1
   * for same alert and same severity it return 0
   * for same alert and severity change it return index
   * @param advisoryAlertStatus
   * @return
   */
  private int isAlertStatusLineExistInList(AdvisoryAlertStatus advisoryAlertStatus)
  {
    if(debugLevel) 
      Log.debugLog(className, "isAlertStatusLineExistInList", "", "", "Method Called. advisoryAlertStatus = " + advisoryAlertStatus.getRuleName());
    
    if (allAlertLine.size() == 0)
    {
      if(debugLevel) 
        Log.debugLog(className, "isAlertStatusLineExistInList", "", "", "No entry in list");
      return -1;
    }

    //checking in all alert list
    for (int i = 0; i < allAlertLine.size(); i++)
    {
      //get alert object from list
      AdvisoryAlertStatus alertData = allAlertLine.get(i);
      
      //check rule name and alert msg
      if(alertData.getRuleName().equals(advisoryAlertStatus.getRuleName()) && alertData.getAlertMsg().equals(advisoryAlertStatus.getAlertMsg()))
      {
        //check severity
        if(alertData.getSeverity().equals(advisoryAlertStatus.getSeverity()))
        {
          if(debugLevel)
            Log.debugLog(className, "isAlertRuleExistInHistory", "", "", "alert already exist,Do not Need to add this data line to history file.");
          return 0;
        }
        else
        {
          if(debugLevel)
            Log.debugLog(className, "isAlertStatusLineExistInList", "", "", "Saverit is change. So need to add this line in status and history file.");
          return i;
        }
      }
    }  //end of loop

      //new alert
    if(debugLevel) 
      Log.debugLog(className, "isAlertStatusLineExistInList", "", "", "new alert generted No entry in list");
    return -1;
  }

  /**
   * This method write the alert status file by iterating the list of alert
   */
  private void writeAlertStatusLine()
  {
    if(debugLevel)
      Log.debugLog(className, "writeAlertStatusLine", "", "", "method called");
    
    //alert status file 
    String alertStatusFile = getAlertStatusFileNameWithPath();
    //open alert status file
    openAlertStatusFile(alertStatusFile);

    String headerLine = "#TestRun|Rule ID|Rule Name|Severity|Alert Time|Alert Msg|value|StartTime|EndTime|ruleType|GraphNames|GraphDataIndex|BaseLineTR|GraphDataIndexBaseline|RuleDesc|future1|future2|future3|future4|future5|future6";
    printSteamAlertStatus.println(headerLine);
    
    //writing all alert of list in to alert status file
    for (int i = 0; i < allAlertLine.size(); i++)
    {
      AdvisoryAlertStatus alertStatusLine = allAlertLine.get(i);
   
      StringBuilder strBuilder = new StringBuilder();
      //creating alert line
      //test run
      strBuilder.append(alertStatusLine.getTestRun() + "|");
      //rule Id
      strBuilder.append(alertStatusLine.getRuleId() + "|");
      //rule name
      strBuilder.append(alertStatusLine.getRuleName() + "|");
      //Severity
      strBuilder.append(alertStatusLine.getSeverity() + "|");
      //alert time
      strBuilder.append(alertStatusLine.getAlertTime() + "|");
      //alert msg
      strBuilder.append(alertStatusLine.getAlertMsg() + "|");
      //value
      strBuilder.append(alertStatusLine.getValue() + "|");
      //start time
      strBuilder.append(alertStatusLine.getStartTime() + "|");
      //end time
      strBuilder.append(alertStatusLine.getEndTime() + "|");
      //rule type
      strBuilder.append(alertStatusLine.getRuleType() + "|");
      //graph Name
      strBuilder.append(alertStatusLine.getGraphNames() + "|");
      //graph Data index
      strBuilder.append(alertStatusLine.getGraphDataIndex() + "|");
      //base line TR
      strBuilder.append(alertStatusLine.getBaseLineTR() + "|");
      //base line test run graph data index
      strBuilder.append(alertStatusLine.getGraphDataIndexBaseline() + "|");
      //rule description
      strBuilder.append(alertStatusLine.getRuleDesc() + "|");
      //future fields
      strBuilder.append("future1|future2|future3|future4|future5|future6");
      
      //writing
      printSteamAlertStatus.println(strBuilder.toString());
    }//end of loop
  }

  /**
   * this is main function is to generate events
   * @return
   */
  public String generateEvents()
  {
    if(debugLevel) 
      Log.debugLog(className, "generateEvents", "", "", "Method called");

    try
    {
      AdvisoryRule advisoryRuleObj = new AdvisoryRule(testRunNum + "", ruleFileName, ruleMode);
      String narFileName = Config.getWorkPath() + "/webapps/advisoryRules" + "/" + ruleFileName + ".nar";
      boolean flag = isNarFileModified(narFileName);
      if (flag || hashMapThreshold == null)
      {
        if(debugLevel) 
          Log.debugLog(className, "generateEvents", "", "", "Nar file modified so going to load nar file into memory.");
        // this is to remove previous file
        hashMapThreshold = advisoryRuleObj.getHashMapOfRuleDataInfoObj(arrRuleNames);
        alertStatus.clear();
      }
      else
      {
        if(debugLevel) 
          Log.debugLog(className, "generateEvents", "", "", "Nar file is not modified, Going to process data from loaded memory.");
      }

      if ((hashMapThreshold == null) || (hashMapThreshold.size() < 1))
      {
        Log.errorLog(className, "generateEvents", "", "", "Rules are not compatible in rules file = " + ruleFileName);
        return "FAILURE|Rules are not compatible in rules file = " + ruleFileName + ".";
      }

      // Create array list for store valid graph data index
      LinkedHashMap<Integer, GraphUniqueKeyDTO> uniqueDTOByIdxMap = new LinkedHashMap<Integer, GraphUniqueKeyDTO>();
      for (int i = 0; i < hashMapThreshold.size(); i++)
      {
        Set st = hashMapThreshold.keySet();
        Iterator itr = st.iterator();
        while (itr.hasNext())
        {
          AdvisoryRuleInfo advisoryRuleInfoObj = (AdvisoryRuleInfo) hashMapThreshold.get(itr.next());
          // If graph data index is not equla to -1 means graph exists
          if (advisoryRuleInfoObj.getGraphDataIndex() != -1)
          {
            uniqueDTOByIdxMap.put(advisoryRuleInfoObj.getGraphDataIndex(), advisoryRuleInfoObj.getGraphUniqueKeyDTO());
          }
        }
      }
      
      TimeBasedTestRunData basedTestRunDataObj = getAndSetDataFromRawDataFile(uniqueDTOByIdxMap, "Specified Time", startTimeForRule, endTimeForRule, false, true, true, false);
      if(basedTestRunDataObj == null)
      {
        if(debugLevel) 
          Log.debugLog(className, "generateEvents", "", "", "Error occur while processing raw data file.");
        return "FAILURE|Error occur while processing raw data file.";
      }
      Log.debugLog(className, "generateEvents", "", "", "hi ");
      int validDataIndex = 0;
      Iterator iterator = hashMapThreshold.keySet().iterator();
      while (iterator.hasNext())
      {
	Log.debugLog(className, "generateEvents", "", "", "hello ");
	Object keyCurr = iterator.next();

        // creating object of AdvisoryRuleInfo for baseline and current TR
        AdvisoryRuleInfo advisoryRuleInfoObj = (AdvisoryRuleInfo) hashMapThreshold.get(keyCurr);

        if(debugLevel) 
          Log.debugLog(className, "generateEvents", "", "", "Extracting graph data for rule " + advisoryRuleInfoObj.getRuleName() + ". Graph Data Index = " + advisoryRuleInfoObj.getGraphDataIndex());
        double[] arrRptData = basedTestRunDataObj.getTimeBasedDTO(uniqueDTOByIdxMap.get(advisoryRuleInfoObj.getGraphDataIndex())).getArrGraphSamplesData();
        double[] arrRptAvgData = arrRptData;
        Log.debugLog(className, "generateEvents", "", "", "getTimeBasedDTO =  " + basedTestRunDataObj.getTimeBasedDTO(uniqueDTOByIdxMap.get(advisoryRuleInfoObj.getGraphDataIndex())));
        if(arrRptData != null)
          generatesEventByRules(arrRptData, arrRptAvgData, advisoryRuleInfoObj);
        else
        {
          if(debugLevel) 
            Log.debugLog(className, "generateEvents", "", "", "Rule not compatible with this test run = " + advisoryRuleInfoObj.getRuleName());
          Log.errorLog(className, "generateEvents", "", "", "Rule not compatible with this test run = " + advisoryRuleInfoObj.getRuleDetails());
        }
      }

      return SUCCESS;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "generateEvents", "", "", "Exception - ", e);
      return ERROR;
    }
  }

  /**
   * Wrapper Method of generateEvents in which rule names is null 
   * It generates the alert by running Alert scheduler
   * @param testRunNum
   * @param ruleFileName
   * @param ruleMode
   * @param startTimeForRule
   * @param endTimeForRule
   * @param baselineTR
   * @return
   */
  public String generateEvents(int testRunNum, String ruleFileName, int ruleMode, String startTimeForRule, String endTimeForRule, int baselineTR)
  {
    return generateEvents(testRunNum, ruleFileName, ruleMode, startTimeForRule, endTimeForRule, baselineTR, null);
  }

  /**
   * Ravi - This function generate events for both Alert Scheduler and On demand  
   */
  public synchronized String generateEvents(int testRunNum, String ruleFileName, int ruleMode, String startTimeForRule, String endTimeForRule, int baselineTR, String[] arrRuleNames)
  {
    try
    {
      if(debugLevel) 
        Log.debugLog(className, "generateEvents", "", "", "Method Called. testRunNum = " + testRunNum + ", ruleFileName = " + ruleFileName + ", ruleMode = " + ruleMode + ", startTimeForRule = " + startTimeForRule + ", endTimeForRule = " + endTimeForRule + ", baselineTR = " + baselineTR);
      this.testRunNum = testRunNum;
      this.ruleFileName = ruleFileName;
      this.ruleMode = ruleMode;
      this.startTimeForRule = startTimeForRule; // start and end time
      this.endTimeForRule = endTimeForRule;
      this.baselineTR = baselineTR;
      this.arrRuleNames = arrRuleNames;

      if (arrRuleNames != null)
        OnDemandMode = true;

      if (baselineTR == -1)
        this.ruleMode = 0;

      //clear list for previous alert
      alertStatus.clear();
      //Generates alert for scheduler or on demand alert
      String msg = generateEvents();
      
      //Generate the alert status file and history file
      if(arrRuleNames == null)
        genAlertStatusFile();
      
      //closing alert files
      closeAlertStausAndHistoryFileStreams();
             
      return msg;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "generateEvents", "", "", "Exception - ", ex);
      return "";
    }
  }
  
  /**
   * Set product mode
   */
  private void setProductMode()
  {
    CmdExec cmdExec = new CmdExec();
    String command = "nsi_show_config";
    String args = " -t";
    try
    {
      Vector result = cmdExec.getResultByCommand(command, args, CmdExec.SYSTEM_CMD, "netstorm", "root");
      String ndeMode = result.get(0).toString().trim();
      if(debugLevel) Log.debugLog(className, "setProductMode", "", "", "ndeMode = " + ndeMode);
      if (ndeMode.equals("NDE"))
        isNDE = true;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "setProductMode", "", "", "Exception - " + e);
    }
  }
  
  /**
   * Send Mail
   * @param advisoryAlertStatus
   * @return
   */
  public boolean sendAlertMail(AdvisoryAlertStatus advisoryAlertStatus)
  {
    if(debugLevel) 
      Log.debugLog(className, "sendAlertMail", "", "", "Before Send Email ..."+" alertstatus = "+alertStatus + " size = "+alertStatus.size());
   
    try
    {
      //Sending Mail
      //Reading config values.
      //Getting Alerts Specific values.
      String[] keywords = new String[]{"netstorm.execution.alert_email", "netstorm.execution.alert.email_host", "netstorm.execution.alert.email_port", "netstorm.execution.alert.email_TO", "netstorm.execution.alert.email_from", "netstorm.execution.alert.email_password", "netstorm.execution.alert.email_replyto"};
      String[] strKeywords = rptUtilsBean.getKeyWordValue(keywords);
      String controllerInfo = rptUtilsBean.getControllerSpecificInfo();
      String isAlertActive = strKeywords[0];
    
      String alertMsgMailHost = strKeywords[1]; 
    
      String alertMsgMailPort = strKeywords[2];
    
      String alertMsgMailReceiver = strKeywords[3];
    
      String alertMsgMailSender = strKeywords[4];
    
      String alertMsgReciverPassword = strKeywords[5];
    
      String alertMsgReplyTo = strKeywords[6];
      String alertSeverity = (advisoryAlertStatus.getSeverity().trim().equalsIgnoreCase("Normal") ? "Clear" : advisoryAlertStatus.getSeverity()) + " ";
      String subject = "Netstorm " + (controllerInfo.equals("") ? "" : "(" + controllerInfo + ") " ) + alertSeverity + "Alert";
    
      String Message = "";
    
      if(debugLevel) 
        Log.debugLog(className, "generateEvents", "", "", "values = "+isAlertActive + " alertMsgMailHost = "+alertMsgMailHost + " alertMsgMailPort = "+alertMsgMailPort + " alertMsgMailReceiver = "+alertMsgMailReceiver + " alertMsgMailSender = "+alertMsgMailSender + " alertMsgReciverPassword = "+alertMsgReciverPassword + " , controllerInfo = " + controllerInfo);

      if(isAlertActive.trim().equalsIgnoreCase("on") && alertStatus != null)
      {
         //SendMail sm = new SendMail();
         if(isNDE)
           subject = "NetDiagnostics " + (controllerInfo.equals("") ? "" : "(" + controllerInfo + ") " ) + alertSeverity + "Alert";
       
        String mailSubject = subject + " - " + advisoryAlertStatus.getAlertMsg();
             
        if(isNDE)
          Message = "NetDiagnostics Alert Details\n---------------------------------------\n\nIDC - " + controllerInfo + "\nSession Number - " + testRunNum + "\n" ;
        else
          Message = "NetStorm Alert Details\n---------------------------------------\n\nIDC - " + controllerInfo + "\nTestRun Number - " + testRunNum + "\n" ;
       
        Message = Message + "Rule Name - "+advisoryAlertStatus.getRuleName() + "\n" + "Severity - "+ alertSeverity + "\n" + "Value - "+ advisoryAlertStatus.getValue() + "\n" + "Alert Time - "+ advisoryAlertStatus.getAlertTime() + "\n"+ "Message - "+advisoryAlertStatus.getAlertMsg();
        Message = Message + "\nIncluded Data Points = 5 \nAlarm Interval: Last 5 Minutes\nTime Range: From " + advisoryAlertStatus.getStartTime() + " to " + advisoryAlertStatus.getEndTime();

        if(debugLevel) 
          Log.debugLog(className, "generateEvents", "", "","Message = "+Message);
        //sm.SendAlertMail(alertMsgMailHost, alertMsgMailPort, alertMsgMailSender, alertMsgReciverPassword, alertMsgMailReceiver, mailSubject, Message, alertMsgReplyTo);
        return true;
      }
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "sendAlertMail", "", "", "Exception - ", ex);
      return false;
    }
    return false;  
  }
  
  // this function is to initilize Report data
  public boolean initReportData()
  {
    if(debugLevel) 
      Log.debugLog(className, "initReportData", "", "", "Method called");

    try
    {
      rptData = new ReportData(testRunNum);

      rptData.startSeq = 1; // First data packet seq is 1
      rptData.endSeq = rptData.maxSeq;

      rptData.getPhaseTimes();

      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getAndSetDataFromRawDataFile", "", "", "Exception - ", e);
      return false;
    }
  }

  //
  private int getAlertInterval()
  {
    int alertInterval = 5;
    try
    {

      String alertConfigInterval = new DashboardConfig().getAlertScheduleInterval();

      if(!alertConfigInterval.trim().equals("NA"))
      {
	alertInterval = Integer.parseInt(alertConfigInterval);

	if(alertInterval > 0)
	{
	  alertInterval = alertInterval/(1000*60);
	}

	if(alertInterval < 1)
	  alertInterval = 1;
      }
      return alertInterval;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      Log.errorLog(className, "getAlertInterval", "", "", "Error in getting Alert Interval. Setting Alert Interval to Default 5 Minutes.");
      return 5;
    }
  }
  // get and set data from rtgMessage.dat file By ReportData object
  private TimeBasedTestRunData getAndSetDataFromRawDataFile(LinkedHashMap<Integer, GraphUniqueKeyDTO> uniqueDTOByIdxMap, String time, String startTime, String endTime, boolean calAllIndex, boolean minMaxFlag, boolean avgFlag, boolean stdDevFlag)
  {
    if(debugLevel) 
      Log.debugLog(className, "getAndSetDataFromRawDataFile", "", "", "Method called. Test Run is  " + testRunNum + ", arrGraphIndex.length = " + uniqueDTOByIdxMap.size() + ", MinMax Flag = " + minMaxFlag + ", Avg Flag = " + avgFlag + ", Std-dev flag = " + stdDevFlag);

    try
    {
      // must initialeze Report data object here
      initReportData();

      //Creating TestRunDataType Object for restricting test run for LAST_N_MINUTES_DATA
      TestRunDataType testRunDataTypeObj = new TestRunDataType(TestRunDataType.LAST_N_MINUTES_DATA, -1, getAlertInterval(), "NA", "NA", false, false);
      // this is to selected TR(home TR)
      TimeBasedTestRunData timeBasedTestRunDataObj = rptData.getTimeBasedDataFromRTGFile(uniqueDTOByIdxMap, time, startTime, endTime, false, avgFlag, testRunDataTypeObj);
      
      if (timeBasedTestRunDataObj == null)
      {
        if(debugLevel) 
          Log.debugLog(className, "getAndSetDataFromRawDataFile", "", "", "arrGraphData = " + arrGraphData);
        return null;
      }

      return timeBasedTestRunDataObj;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getAndSetDataFromRawDataFile", "", "", "Exception - ", e);
      return null;
    }
  }

  /**
   * Generates Events for Compare
   * @return
   */
  public String generateEventForCompare()
  {
    if(debugLevel) 
      Log.debugLog(className, "generateEventForCompare", "", "", "Method called");

    try
    {
      Log.debugLogAlways(className, "generateEventForCompare", "", "", "Method called" + "testRunNum = " + testRunNum + " baselineTR = " + baselineTR); 
      // creating obj for current Test and baseline Test
      AdvisoryRule advisoryRuleCurrentTRObj = new AdvisoryRule(testRunNum + "", ruleFileName, 1);
      AdvisoryRule advisoryRuleBaselineTRObj = new AdvisoryRule(baselineTR + "", ruleFileName, 1);

      // Hash map for baseline and current tr
      HashMap hashMapCmpCurrTr = advisoryRuleCurrentTRObj.getHashMapOfRuleDataInfoObj(arrRuleNames);
      HashMap hashMapCmpBaselineTr = advisoryRuleBaselineTRObj.getHashMapOfRuleDataInfoObj(arrRuleNames);
      Log.debugLogAlways(className, "generateEventForCompare", "", "", "Method called" + "cur tr size = " + hashMapCmpCurrTr.size() + " baseline tr size = " + hashMapCmpBaselineTr.size());
      // If any one is null or size zero it will not compare
      if (((hashMapCmpCurrTr == null) || (hashMapCmpCurrTr.size() < 1)) && ((hashMapCmpBaselineTr == null) || (hashMapCmpBaselineTr.size() < 1)))
      {
        Log.errorLog(className, "generateEventForCompare", "", "", "Rules are not compatible in rules file = " + ruleFileName);
        return "FAILURE|Rules are not compatible for compare in rules file " + ruleFileName + ".";
      }

      // Create array list for store valid graph data index of current test,
      // baseline test and key
      LinkedHashMap<Integer, GraphUniqueKeyDTO> uniqueGraphDTOCmpCurr = new LinkedHashMap<Integer, GraphUniqueKeyDTO>();
      LinkedHashMap<Integer, GraphUniqueKeyDTO> uniqueGraphDTOCmpBaseLine = new LinkedHashMap<Integer, GraphUniqueKeyDTO>();
      
      ArrayList<String> arrayListForMatchKeyCmp = new ArrayList();

      // Iterator object for both baseline and current TR
      Iterator iteratorCurrTr = hashMapCmpCurrTr.keySet().iterator();
      Iterator iteratorBaselineTr = hashMapCmpBaselineTr.keySet().iterator();

      while (iteratorCurrTr.hasNext())
      {
        Object keyCurr = iteratorCurrTr.next();

        // creating object of AdvisoryRuleInfo for baseline and current TR
        AdvisoryRuleInfo advisoryRuleInfoForCurrObj = (AdvisoryRuleInfo) hashMapCmpCurrTr.get(keyCurr);
        AdvisoryRuleInfo advisoryRuleInfoForBaselineObj = (AdvisoryRuleInfo) hashMapCmpBaselineTr.get(keyCurr);

        if ((advisoryRuleInfoForCurrObj.getGraphDataIndex() != -1) && (advisoryRuleInfoForBaselineObj.getGraphDataIndex() != -1))
        {
          // store graph index of current and baeline graph
          // b'coz of graph may have differnt graph id for base or current
          uniqueGraphDTOCmpCurr.put(advisoryRuleInfoForCurrObj.getGraphDataIndex(), advisoryRuleInfoForCurrObj.getGraphUniqueKeyDTO());
          uniqueGraphDTOCmpBaseLine.put(advisoryRuleInfoForBaselineObj.getGraphDataIndex(), advisoryRuleInfoForBaselineObj.getGraphUniqueKeyDTO());
          arrayListForMatchKeyCmp.add(keyCurr.toString());
        }
        else
          Log.errorLog(className, "generateEventForCompare", "", "", "Graph data index may be -1 of " + keyCurr);
      }

      // this condition verify start and end time for current and baseline TR
      // IF curr > Base - take duration of base line
      // If Base > curr - take duration of curr tr
      // need to handle online mode elapse time of curr test ???
      long currTime = rptUtilsBean.convStrToMilliSec(Scenario.getTestDuration(testRunNum));
      long baselineTime = rptUtilsBean.convStrToMilliSec(Scenario.getTestDuration(baselineTR));

      long currEndTime = rptUtilsBean.convStrToMilliSec(endTimeForRule);

      if(debugLevel) 
        Log.debugLog(className, "generateEventForCompare", "", "", Scenario.getTestDuration(testRunNum) + " currTime = " + currTime + "-----  " + Scenario.getTestDuration(baselineTR) + " , baselineTime - " + baselineTime + "---   " + endTimeForRule + " , currEndTime = " + currEndTime);

      // if(currTime > baselineTime)
      if ((currEndTime > baselineTime) && baselineTime > 0)
      {
        // startTimeForRule = "00:00:00";
        endTimeForRule = Scenario.getTestDuration(baselineTR);
      }
      else if ((currEndTime > currTime) && currTime > 0)
      {
        // startTimeForRule = "00:00:00";
        endTimeForRule = Scenario.getTestDuration(testRunNum);
      }

      if(debugLevel) 
        Log.debugLog(className, "generateEventForCompare", "", "", "startTimeForRule = " + startTimeForRule + ", endTimeForRule - " + endTimeForRule);
      
      TimeBasedTestRunData basedTestRunDataCurrObj = getAndSetDataFromRawDataFile(uniqueGraphDTOCmpCurr, "Specified Time", startTimeForRule, endTimeForRule, false, true, true, false);
      
      if(basedTestRunDataCurrObj == null)
      {
        Log.errorLog(className, "generateEventForCompare", "", "", "Error occur while processing raw data file for current graph.");
        return "FAILURE|Error occur while processing raw data file for current Test Run(" + testRunNum + ").";
      }

      TimeBasedTestRunData basedTestRunDataBaseLineObj = getAndSetDataFromRawDataFileForCmp(uniqueGraphDTOCmpBaseLine, "Specified Time", startTimeForRule, endTimeForRule, false, true, true, false);
      
      if(basedTestRunDataBaseLineObj == null)
      {
        Log.errorLog(className, "generateEventForCompare", "", "", "Error occur while processing raw data file for baseline graph.");
        return "FAILURE|Error occur while processing raw data file for baseline Test Run(" + baselineTR + ").";
      }

      for (int i = 0; i < arrayListForMatchKeyCmp.size(); i++)
      {
        String keyIndex = arrayListForMatchKeyCmp.get(i);
        AdvisoryRuleInfo advisoryRuleInfoForCurrObj = (AdvisoryRuleInfo) hashMapCmpCurrTr.get(keyIndex);
        AdvisoryRuleInfo advisoryRuleInfoForBaselineObj = (AdvisoryRuleInfo) hashMapCmpBaselineTr.get(keyIndex);

        if(debugLevel) 
          Log.debugLog(className, "generateEventForCompare", "", "", "Extracting graph data for compare. Graph Data Index of current = " + advisoryRuleInfoForCurrObj.getGraphDataIndex() + " and baseline = " + advisoryRuleInfoForBaselineObj.getGraphDataIndex());

        double[] arrRptDataCurr = basedTestRunDataCurrObj.getTimeBasedDTO(advisoryRuleInfoForCurrObj.getGraphUniqueKeyDTO()).getArrGraphSamplesData();
        double[] arrRptDataBaseline = basedTestRunDataBaseLineObj.getTimeBasedDTO(advisoryRuleInfoForBaselineObj.getGraphUniqueKeyDTO()).getArrGraphSamplesData();

        generatesEventByRulesForCompare(arrRptDataCurr, arrRptDataCurr, advisoryRuleInfoForCurrObj, arrRptDataBaseline, arrRptDataCurr, advisoryRuleInfoForBaselineObj);
      }
      closeAlertStausAndHistoryFileStreams();

      // If event is not generated means failure
      String fileNameWithPath = getAnalysisAlertFileNameWithPath();
      File analysisAlertFileObj = new File(fileNameWithPath);

      if (!analysisAlertFileObj.exists())
      {
        return FAILURE;
      }
      return SUCCESS;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "generateEventForCompare", "", "", "Exception - ", e);
      return ERROR;
    }
  }

  // this function is to initilize Report data
  public boolean initReportDataForCompare()
  {
    if(debugLevel) 
      Log.debugLog(className, "initReportDataForCompare", "", "", "Method called");

    try
    {
      rptDataCompare = new ReportData(baselineTR);

      rptDataCompare.startSeq = 1; // First data packet seq is 1

      // case - if baseline tr have more seq so so initialize curr endseq
      if (rptDataCompare.maxSeq < rptData.maxSeq)
      {
        rptDataCompare.endSeq = rptDataCompare.maxSeq;
        rptData.maxSeq = rptDataCompare.maxSeq;
      }
      else
      {
        rptDataCompare.endSeq = rptData.maxSeq;
      }

      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "initReportDataForCompare", "", "", "Exception - ", e);
      return false;
    }
  }

  // get and set data from rtgMessage.dat file By ReportData object of baseline
  // TR
  private TimeBasedTestRunData getAndSetDataFromRawDataFileForCmp(LinkedHashMap<Integer, GraphUniqueKeyDTO> uniqueDTObyIndexMap, String time, String startTime, String endTime, boolean calAllIndex, boolean minMaxFlag, boolean avgFlag, boolean stdDevFlag)
  {
    if(debugLevel) 
      Log.debugLog(className, "getAndSetDataFromRawDataFileForCmp", "", "", "Method called. Test Run is  " + testRunNum + "arrGraphIndex.length = " + uniqueDTObyIndexMap.size() + ", MinMax Flag = " + minMaxFlag + ", Avg Flag = " + avgFlag + ", Std-dev flag = " + stdDevFlag);

    try
    {
      // must initialeze Report data object here
      initReportDataForCompare();
      
      //Creating TestRunDataType object for calculation of LAST_N_MINUTES_DATA data
      TestRunDataType testRunDataTypeObj = new TestRunDataType(TestRunDataType.LAST_N_MINUTES_DATA, -1, getAlertInterval(), "NA", "NA", false, false);
      // this is to selected TR(home TR)
      TimeBasedTestRunData basedTestRunDataCmpObj = rptDataCompare.getTimeBasedDataFromRTGFile(uniqueDTObyIndexMap, time, startTime, endTime, calAllIndex, stdDevFlag , testRunDataTypeObj);

      if (basedTestRunDataCmpObj == null)
      {
        if(debugLevel) 
          Log.debugLog(className, "getAndSetDataFromRawDataFileForCmp", "", "", "Getting null data");
        return null;
      }

      return basedTestRunDataCmpObj;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getAndSetDataFromRawDataFileForCmp", "", "", "Exception - ", e);
      return null;
    }
  }

  // this is to generate events by rules type
  private void generatesEventByRulesForCompare(double[] arrRptDataCurr, double[] arrRptAvgDataCurr, AdvisoryRuleInfo advisoryRuleInfoCurrObj, double[] arrRptDataBaseline, double[] arrRptAvgDataBaseline, AdvisoryRuleInfo advisoryRuleInfoBaselineObj)
  {
    if(debugLevel) 
      Log.debugLog(className, "generatesEventByRulesForCompare", "", "", "Method called.");
    try
    {
      String ruleType = advisoryRuleInfoCurrObj.getRuleType();
      String thresholdType = advisoryRuleInfoCurrObj.getThresholdType();
      String strOperation = advisoryRuleInfoCurrObj.getOperation();
      // value contains 3 digits comma separator like 3,5,6
      String severityOpt = advisoryRuleInfoCurrObj.getValue();
      double value = Double.parseDouble(severityOpt.split(",")[0]);
      String strPctChange = advisoryRuleInfoCurrObj.getPctChange();
      String strTimeWindow = advisoryRuleInfoCurrObj.getTimeWindow();
      String strGraphNames = advisoryRuleInfoCurrObj.getStrGraphName();
      String strRuleDesc = advisoryRuleInfoCurrObj.getDescription();

      int interval = advisoryRuleInfoCurrObj.getInterval();
      int intervalBaseline = advisoryRuleInfoBaselineObj.getInterval();

      int graphDataIndexCurr = advisoryRuleInfoCurrObj.getGraphDataIndex();
      int graphDataIndexBaseline = advisoryRuleInfoBaselineObj.getGraphDataIndex();

      String ruleId = advisoryRuleInfoCurrObj.getRuleId();
      String alertMessage = advisoryRuleInfoCurrObj.getAlertMessage();
      String ruleName = advisoryRuleInfoCurrObj.getRuleName();

      if (thresholdType.equals("MovingAverage"))
      {
        MovingAverage movingAverageObj = new MovingAverage(arrRptDataCurr, arrRptAvgDataCurr, arrRptDataBaseline, arrRptAvgDataBaseline, ruleType, thresholdType, strOperation, value, strPctChange, startTimeForRule, endTimeForRule, strTimeWindow, strGraphNames, strRuleDesc, interval, this, graphDataIndexCurr + "", graphDataIndexBaseline + "", severityOpt, ruleId, ruleName, alertMessage);
        movingAverageObj.generateMovingAverageEventsForCompare();
      }
      else if (thresholdType.equals("Average"))
      {
        Threshold thresholdObj = new Threshold(arrRptDataCurr, arrRptAvgDataCurr, arrRptDataBaseline, arrRptAvgDataBaseline, ruleType, thresholdType, strOperation, value, strPctChange, startTimeForRule, endTimeForRule, strTimeWindow, strGraphNames, strRuleDesc, interval, this, graphDataIndexCurr + "", graphDataIndexBaseline + "", severityOpt, ruleId, ruleName, alertMessage);
        thresholdObj.generateThresholdEventsForCompare();
      }
      else
      {
        if(debugLevel) 
          Log.debugLog(className, "generatesEventByRulesForCompare", "", "", "Rule type not supported, Rule Type = " + ruleType + ", for graph name = " + strGraphNames);
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "generatesEventByRulesForCompare", "", "", "Exception - ", e);
    }
  }
  
//this is to add correlated data to file
  public boolean addCorrelatedEventDataToFile(String correlatedEventData)
  {
    if(debugLevel) 
      Log.debugLog(className, "addCorrelatedEventDataToFile", "", "", "Method called, Event Data = " + correlatedEventData);

    try
    {
      String fileNameWithPath = getAnalysisAlertFileNameWithPath();
      File analysisAlertFileObj = new File(fileNameWithPath);

      // if file not there then it is the error condition
      if (!analysisAlertFileObj.exists())
      {
        try
        {
          analysisAlertFileObj.createNewFile();
          if (analysisAlertFileObj.exists())
          {
            if(debugLevel) 
              Log.debugLog(className, "addCorrelatedEventDataToFile", "", "", "Analysis Alert file, " + fileNameWithPath + " created successfully.");
          }
        }
        catch (Exception e)
        {
          Log.stackTraceLog(className, "addCorrelatedEventDataToFile", "", "", "Exception - ", e);
        }
        // Log.errorLog(className, "addCorrelatedEventDataToFile", "", "",
        // "Error analysis alert file " + fileNameWithPath +
        // " is not available in the TR");
        // return false;
      }

      fout = new FileOutputStream(analysisAlertFileObj, true);
      printStream = new PrintStream(fout);

      if(debugLevel) 
        Log.debugLog(className, "addCorrelatedEventDataToFile", "", "", "Adding correlated data to file " + fileNameWithPath + ", data = " + correlatedEventData);
      printStream.println(correlatedEventData);
      printStream.flush();
      closeAlertStausAndHistoryFileStreams();

      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "addCorrelatedEventDataToFile", "", "", "Exception - ", e);
      closeAlertStausAndHistoryFileStreams();
      return false;
    }
  }
  
  /**
   * generates All Event By Rules
   * @return
   */
  public String generatesAllEventByRules()
  {
    if(debugLevel) 
      Log.debugLog(className, "generatesAllEventByRules", "", "", "Method called");
    try
    {
      String strResult = generateEvents();
      if (baselineTR != -1)
      {
        strResult = generateEventForCompare();
      }
      else
        Log.errorLog(className, "generatesAllEventByRules", "", "", "Base line Tr is not present. So ignoring compare rules");

      return strResult;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "generatesAllEventByRules", "", "", "Exception - ", e);
      return ERROR;
    }
  }

  public static void main(String[] args)
  {
    if (args.length >= 2)
    {
      // int testRunNum = Integer.parseInt(args[0]);
      int testRunNum = 20142;
      String ruleFileName = args[1];
      int ruleMode = 0;
      // String startTime = "01:48:00";
      // String endTime = "01:50:00";
      String startTime = "00:00:00";
      String endTime = "02:22:10";
      // int baselineTR = 46993;
      int baselineTR = 41936;
      // String baselineTR = "21540";

      EventGeneratorAdvisory eventGeneratorAdvisoryObj = new EventGeneratorAdvisory(testRunNum, ruleFileName, ruleMode, startTime, endTime, baselineTR);
      //String strResult = eventGeneratorAdvisoryObj.generateEvents();
      // String strResult = eventGeneratorAdvisoryObj.generateEventForCompare();
      // boolean result = eventGeneratorAdvisoryObj.generatesAllEventByRules();
      String strResult = eventGeneratorAdvisoryObj.generatesAllEventByRules();
      if (strResult.startsWith("SUCCESS"))
        System.out.println("Analysis alert generated successfully");
      else if (strResult.startsWith("FAILURE"))
        System.out.println(strResult.substring((strResult.indexOf("|") + 1), strResult.length()));
      else if (strResult.startsWith("ERROR"))
        System.out.println("Error occur while generating analysis alert, Please see error logs for more details.");
    }
    else
      System.out.println("Please enter test run number and rule file name.");
  }
}

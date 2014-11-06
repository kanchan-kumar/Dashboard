/**----------------------------------------------------------------------------
 * Name     :  ImportAccessLogData.java
 * Purpose  :  To compute data for test run data using Apache Access log.
 * @author  :  Ritesh Sharma
 * Modification History
 *    Initial Version -> Ritesh -> 01/13/2013
 *---------------------------------------------------------------------------**/
package pac1.Bean;

import pac1.Bean.CustomizeLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pac1.Bean.*;
import java.net.*;
import javax.swing.JFrame;

public class ImportAccessLogData
{
  String className = "ImportAccessLogData";

  ArrayList listOffiles = new ArrayList(); // this contains all list of file

  // which provide by user

  ArrayList listOfFilestoProcessInfo = new ArrayList();// this contains

  // filtered file after
  // validate

  ArrayList listOfFilestoProcess = new ArrayList();// this contains the info of

  // each filtered file those
  // will be processed

  ArrayList detailsOfFilelist = new ArrayList();

  String progressMessage = "";

  /*
   * this define how much vector of graphs will be generate isOverAllUsed : for
   * all URL isForSpecificURL : for specific URL if that URL is found URLList:
   * contains list of URLs
   */
  boolean isOverAllUsed = true;

  boolean isForSpecificURL = true;
  boolean isRequestLine = true;
  ArrayList URLList = new ArrayList();
  ArrayList  <PatternMatcher> patternMatcherList= new ArrayList<PatternMatcher>();
  PatternMatcher patternMatcherObj = null;

  ArrayList VectorList = new ArrayList();

  /*
   * define which graph type will use Standard: 2 graph will make and 5 double
   * is required Extended: 21 graph will make and 42 double required
   */
  boolean isStandard = false;

  private final double MAXIMUM_VALUE = 9999999999999D;

  private final int COLUMN_COUNT_FOR_STANDARD = 5;

  private final int COLUMN_COUNT_FOR_EXTENDED = 42;

  public long AdjustedEndTimeInMiles = 0;

  int number_Of_Samples = 0;

  int number_of_columns = 0;

  double[][] arrDataValues = null; // Global 2-D array for test run data

  int arrColIndex[] = null;// this used to maintain column index to insert data

  // in global 2-D array

  String separator = "space"; // use as separator to parse the data line

  int url_field_index = 6; // URL field in data line

  int svctime_field_index = 8; // service time field in data line

  int dateTime_field_index = 4;// log date time field in data line

  int status_field_index = 7; // status code field in data line

  int dataSize_field_index = 5; // data packeted field in data line

  String dataTimeFormat = "dd/MMM/yyyy:hh:mm:ss";// data-time format for log

  // file

  int interval = 10000; // default interval
  int throughPutUnit = 1024;
  /*
   * test run info durationInMilies: test run duration in milliseconds
   * StartTestTimeInMiles: start test run time in milliseconds
   * EndTestTimeInMilies: end test run time in milliseconds TestRunDateFormat:
   * test run date-time format TestRunTimeZone: test run time zone
   */
  double durationInMilies = 0;

  long StartTestTimeInMiles = 0;

  long EndTestTimeInMilies = 0;

  String TestRunDateFormat = "dd/MM/yyyy:hh:mm:ss";

  String TestRunTimeZone = "Asia/Calcutta";

  /*
   * This set the time zone options: 0 - synchronised file data with testrun
   * time 1 - user give +00:00:00 or -00:00:00 2- Same time zone of test run and
   * data file
   */
  public int TimeZoneOption = 2;

  public String addTimeToSyncronizedData = "+00:00:10";

  String externalDataTimeZone = "Asia/Calcutta";
  public long addTimeInMilies = 0;
  public boolean isExtraTimeCalculated = false;
  public boolean isAdd = true;

  /*
   * This set the import data options: 0- parse test duration time data 1-
   * import data for xxx% of test run 2- import data of specific times i.e
   * defined by user
   */
  public int importDataOption = 2;//

  public int pecentage = 10;

  public String UserDefinedTimeToImport = "0:59:48";

  public long endTimeInMilesForUserDefine = 0;

  // for escape characters
  private String[] charsToEscape = new String[]{"[", "{", "(", "]", "}", ")", "^", "$", "&", "\\", ",", "+", "?", "|"};

  // this get current file pointer
  long offSet = 0;

  private final double CONVERTER_UNIT_SEC_TO_MICRO = 1000000; // to convert

  // microseconds to
  // seconds

  private final double CONVERTER_UNIT_SEC_TO_MILI = 1000; // to convert

  // milliseconds to
  // seconds

  String unitOption = "mis";

  RandomAccessFile fileObj = null;

  ProgressBar progressBar = null;

  ImportApacheLogSettingDTO dtoObj = null;
  CustomizeLog log = null;
  
  HashMap<String, FileInfo> filesInfoMap = new HashMap<String, FileInfo>();
  
  String logMinTime = null;
  String logMaxTime = null;

  /*
   * Parameterise constructor to initialise class variable
   */
  public ImportAccessLogData(ImportApacheLogSettingDTO dtoObj, ProgressBar progressBar , String testRunNumber) throws Exception
  {
    
    this.progressBar = progressBar;
    this.dtoObj = dtoObj;
    
    log = new CustomizeLog("import_access_log_debug.log" , "import_access_log_error.log");
    log.setTestRun(testRunNumber);
    
    progressMessage = "processing Import data ...";

    // set data form DTO object to class variable
    listOffiles = dtoObj.getSelectedFileList();
    interval = dtoObj.getInterval();;
   
    log.debugLog(className, "ImportAccessLogData", "", "", "interval = " + interval);

    separator = dtoObj.getSepratorField();
    log.debugLog(className, "ImportAccessLogData", "", "", "separator = " + separator);
    dateTime_field_index = dtoObj.getDateTimeField();
    log.debugLog(className, "ImportAccessLogData", "", "", "dateTime_field_index = " + dateTime_field_index);
    dataSize_field_index = dtoObj.getDataSizeField();
    log.debugLog(className, "ImportAccessLogData", "", "", "dataSize_field_index = " + dataSize_field_index);
    status_field_index = dtoObj.getStatusField();
    log.debugLog(className, "ImportAccessLogData", "", "", "status_field_index = " + status_field_index);
    svctime_field_index = dtoObj.getResponseTimeField();
    log.debugLog(className, "ImportAccessLogData", "", "", "svctime_field_index = " + svctime_field_index);
    url_field_index = dtoObj.getUrlField();
    log.debugLog(className, "ImportAccessLogData", "", "", "url_field_index = " + url_field_index);
    isOverAllUsed = dtoObj.isForAllUrl();
    log.debugLog(className, "ImportAccessLogData", "", "", "isOverAllUsed = " + isOverAllUsed);
    isStandard = dtoObj.getStanderd();
    log.debugLog(className, "ImportAccessLogData", "", "", "isStandard = " + isStandard);
    isForSpecificURL = dtoObj.isForSpecificUrl();
    log.debugLog(className, "ImportAccessLogData", "", "", "isForSpecificURL = " + isForSpecificURL);
    dataTimeFormat = dtoObj.getDateTimeFormat();
    log.debugLog(className, "ImportAccessLogData", "", "", "dataTimeFormat = " + dataTimeFormat);
    isRequestLine = dtoObj.isRequestLineUsed();
    log.debugLog(className, "ImportAccessLogData", "", "", "Date time format for access log = " + dataTimeFormat);
    
    URLList = dtoObj.getUrlName();
    log.debugLog(className, "ImportAccessLogData", "", "", "URL Pattern(s) = " + URLList.toString());
    TestRunTimeZone = dtoObj.getTimeZoneForTestRun();
    log.debugLog(className, "ImportAccessLogData", "", "", "TestRunTimeZone = " + TestRunTimeZone);
    TestRunDateFormat = dtoObj.getDateTimeFormatForTestRun();
    log.debugLog(className, "ImportAccessLogData", "", "", "TestRunDateFormat = " + TestRunDateFormat);
    String startTestTime = dtoObj.getStartTestRunTime();
    log.debugLog(className, "ImportAccessLogData", "", "", "startTestTime = " + startTestTime);
    StartTestTimeInMiles = convertIntoMiliSec(startTestTime, true);
    String endTestTime = dtoObj.getEndTestRunTime();
    log.debugLog(className, "ImportAccessLogData", "", "", "endTestTime = " + endTestTime);
    EndTestTimeInMilies = convertIntoMiliSec(endTestTime, true);

    TimeZoneOption = dtoObj.getDateTimeSelectionZoneFlag();
    log.debugLog(className, "ImportAccessLogData", "", "", "TimeZoneOption = " + TimeZoneOption);
    
    if(TimeZoneOption == 0)
    {
      externalDataTimeZone = dtoObj.getDateTimeSelectionZone();
    }
    else if(TimeZoneOption == 1)
    {
      addTimeToSyncronizedData = dtoObj.getDateTimeSelectionZone();
    }

    importDataOption = dtoObj.getImportDataTimeSelectionFlag();
    
    log.debugLog(className, "ImportAccessLogData", "", "", "importDataOption = " + importDataOption);
    
    if(importDataOption == 1)
      pecentage = Integer.parseInt(dtoObj.getImportDataTimeSelection());
    else if(importDataOption == 2)
      UserDefinedTimeToImport = dtoObj.getImportDataTimeSelection();

    unitOption = dtoObj.getResponseTimeUnit();
    
    log.debugLog(className, "ImportAccessLogData", "", "", "unitOption = " + unitOption);
    
    if(unitOption.equalsIgnoreCase("Seconds"))
      unitOption = "sec";
    else if(unitOption.equalsIgnoreCase("Milliseconds"))
      unitOption = "mis";
    else
      unitOption = "mic";

    if(isOverAllUsed)
      VectorList.add(dtoObj.getVectorName());

    ArrayList vecList = dtoObj.getUrlVectorName();
    if(isForSpecificURL)
    {
      for(int i = 0; i < vecList.size(); i++)
      {
        VectorList.add(vecList.get(i).toString());
      }
    }
  }

  public void changeProgressBarMsg(String msg)
  {
    if(progressBar != null)
    {
      progressBar.setVisible(false);
      progressBar.setLabelMsg(msg);
      progressBar.setVisible(true);
    }
  }

  public boolean calculateMinAndMaxLogsDuration(ArrayList listFiles)
  {
    try
    {
      Log.debugLog(className, "calculateMinAndMaxLogsDuration", "", "", "method called");
      
      long MinTime = Long.MAX_VALUE;
      long MaxTime = 0;
      
      for(int i = 0; i < listFiles.size(); i++)
      {
        FileInfo fileInfo =  new FileInfo();

        String filePath = listFiles.get(i).toString();
        
        fileInfo.setFilePath(filePath);

        progressMessage = "Validating file " + filePath + " ...";

        File file = new File(filePath);
        
        if(!file.exists())
        {
          String msg = "File is not exist.";
          log.debugLog(className, "calculateMinAndMaxLogsDuration", "", "", "file is not exist in given filepath= " + filePath);
          fileInfo.setState(false, "ERROR " + msg);
          filesInfoMap.put(filePath, fileInfo);
          continue;
        }
        
        String dataLine = getfirstLine(file);

        fileInfo.setFirstDataLine(dataLine);
        
        if(dataLine == null)
        {
          String msg = "File is not in correct format.";
          fileInfo.setState(false, "ERROR " + msg);
          filesInfoMap.put(filePath, fileInfo);
          log.errorLog(className, "calculateMinAndMaxLogsDuration", "", "", "error in getting reading first= " + filePath);
          continue;
        }

        String StarTime = getSpecificColumnData(dataLine, dateTime_field_index, true);
        
        fileInfo.setStartTime(StarTime);
      
        dataLine = getlastLine(file);
      
        fileInfo.setEndDataLine(dataLine);
        
        if(dataLine == null)
        {
          String msg = "File is not in correct format.";
          fileInfo.setState(false, "ERROR " + msg);
          filesInfoMap.put(filePath, fileInfo);
          log.errorLog(className, "calculateMinAndMaxLogsDuration", "", "", "error in getting reading last line = " + filePath);
          continue;
        }
      
        String endTime = getSpecificColumnData(dataLine, dateTime_field_index, true);
        
        fileInfo.setEndTime(endTime);

        if(StarTime == null || endTime == null)
        {
          String msg = "File is not in correct format.";
          fileInfo.setState(false, "ERROR " + msg);
          filesInfoMap.put(filePath, fileInfo);
          log.errorLog(className, "calculateMinAndMaxLogsDuration", "", "", "error in getting start or end time = " + filePath);
          continue;
        }

        long[] strData = new long[3];
      
        if(TimeZoneOption == 3)
        {
          isExtraTimeCalculated = false;
        }
      
        if(TimeZoneOption == 4)
        {
          strData[0] = getTimeInMilies(StarTime);
          strData[1] = getTimeInMilies(endTime);
        }
        else
        {
          strData[0] = convertIntoMiliSec(StarTime, false);
          strData[1] = convertIntoMiliSec(endTime, false);
        }
        
        fileInfo.setStartTimeInMilies(strData[0]);
        fileInfo.setEndTimeInMilies(strData[1]);
    
        if(strData[0] == 0 || strData[1] == 0)
        {
          String msg = "Response date-time is not found.";
          fileInfo.setState(false, "ERROR " + msg);
          filesInfoMap.put(filePath, fileInfo);
          log.debugLog(className, "calculateMinAndMaxLogsDuration", "", "", "Getting error to get Start time or End time of file= " + filePath);
          continue;
        }
        
        filesInfoMap.put(filePath, fileInfo);
       
        if(MinTime > strData[0])
        {
          MinTime = strData[0];
          logMinTime = StarTime;
        }
        if(MaxTime < strData[1])
        {
          MaxTime = strData[1];
          logMaxTime = endTime;
        }        
      }
      
      Log.debugLogAlways(className, "calculateMinAndMaxLogsDuration", "", "", "Minimum Log Time = " + logMinTime + ", Maximum Log Time = " + logMaxTime);
      
      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "calculateMinAndMaxLogsDuration", "", "", "Exception in calculating Min time from logs ", e);
      return false;
    }
  }
   
  private boolean calculateSamples()
  {
    try
    {
      Log.debugLog(className, "calculateSamples", "", "", "method called");
      
      // calculating nuber of rows
      durationInMilies = ((double)EndTestTimeInMilies - (double)StartTestTimeInMiles);

      if(importDataOption == 0)
      {
        double tempSamples = (durationInMilies / (interval));

        if(tempSamples > 1)
        {
          tempSamples = Math.ceil(tempSamples);
        }
        else if(durationInMilies != 0)
        {
          tempSamples = 1;
        }
        number_Of_Samples = (int)tempSamples;
        AdjustedEndTimeInMiles = EndTestTimeInMilies;
      }
      else if(importDataOption == 1)
      {
        double calculateDuration = (durationInMilies * pecentage) / 100;

        AdjustedEndTimeInMiles = StartTestTimeInMiles + (long)calculateDuration;

        double tempSamples = (calculateDuration / (interval));
        
        if(tempSamples > 1)
        {
          tempSamples = Math.ceil(tempSamples);
        }
        else if(durationInMilies != 0)
        {
          tempSamples = 1;
        }
        number_Of_Samples = (int)tempSamples;
      }
      else if(importDataOption == 2)
      {
        double calculateDuration = rptUtilsBean.convStrToMilliSec(UserDefinedTimeToImport);
        double tempSamples = (calculateDuration / (interval));

        if(tempSamples > 1)
        {
          tempSamples = Math.ceil(tempSamples);
        }
        else if(durationInMilies != 0)
        {
          tempSamples = 1;
        }

        AdjustedEndTimeInMiles = StartTestTimeInMiles + (long)calculateDuration;
        number_Of_Samples = (int)tempSamples;
      }

      log.debugLog(className, "validateFileWithTestRunDuration", "", "", "number of samples defind in test = " + number_Of_Samples);
      
      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "calculateSamples", "", "", "Exception is calculating samples ", e);
      return false;
    }
  }

  /*
   * This function validate the list of files which being imported First fetch
   * first data line and last line of file and get Date-Time, this will be start
   * time and end time of the file Then start time and end time should be match
   * with start and end time of test run
   */

  public boolean validateFileWithTestRunDuration(ArrayList listFiles)
  {
    try
    {
    
      Log.debugLog(className, "validateFileWithTestRunDuration", "", "", "method called");
      
      //Read all file and store start and end time of all logs and also calculate minimum start time and maximum time
      calculateMinAndMaxLogsDuration(listFiles);
     
      //This option to read from  minimum start time from all logs files
      if(TimeZoneOption == 4)
      {
        long startTimefromLog = convertIntoMiliSec(logMinTime, false);
        Log.debugLogAlways(className, "validateFileWithTestRunDuration", "", "", "Calculated Start Time to read from Log File is = " + startTimefromLog );
      }
      
      //Calculate samples
      calculateSamples();
      
      if(number_Of_Samples == 0)
      {
    	  log.errorLog(className, "validateFileWithTestRunDuration", "", "", "No samples is found");
        return false;
      }

      boolean bool = true;
     
      for(int i = 0; i < listFiles.size(); i++)
      {
        String filePath = listFiles.get(i).toString();
        
        FileInfo fileInfo = filesInfoMap.get(filePath);
        
        String fileSuccessDetail[] = new String[2];
        
        fileSuccessDetail[0] = filePath;
        
        //If file state is false then file will not continue to parse data
        if(!fileInfo.isState())
        {
          fileSuccessDetail[1] = fileInfo.getDescription();
          detailsOfFilelist.add(fileSuccessDetail);
          continue;
        }
        
        long[] strData = new long[3];
      
        //This option is used to sync file start time with Test Run start time
        if(TimeZoneOption == 3)
        {
          isExtraTimeCalculated = false;
        }
        
        strData[0] = convertIntoMiliSec(fileInfo.getStartTime(), false);
        strData[1] = convertIntoMiliSec(fileInfo.getEndTime(), false);
        
        if(strData[0] == 0 || strData[1] == 0)
        {
          String msg = "Response date-time is not found.";
          fileSuccessDetail[1] = "ERROR = " + msg;
          detailsOfFilelist.add(fileSuccessDetail);
    
          log.debugLog(className, "validateFileWithTestRunDuration", "", "", "Getting error to get Start time or End time of file= " + filePath);
          continue;
        }
        
        if(!(strData[0] <= EndTestTimeInMilies && strData[1] >= StartTestTimeInMiles))
        { 
          String msg = "No data found for the test time duration.";
         
          fileSuccessDetail[1] = "ERROR " + msg;
          detailsOfFilelist.add(fileSuccessDetail);
    
          log.debugLog(className, "validateFileWithTestRunDuration", "", "", "this file = " + filePath + " did not match with current test run duration");
          continue;
        }
        
        double numberOfSam = (strData[1] - strData[0]) / (interval);

        if(numberOfSam > 1)
        {
          strData[2] = Math.round(numberOfSam);
        }
        else if(numberOfSam != 0)
        {
          strData[2] = 1;
        }

        fileSuccessDetail[1] = "File processed successfully.";
    
        detailsOfFilelist.add(fileSuccessDetail);
        // add file info start time and end time and number of samples of file
        // which will be processed
        listOfFilestoProcessInfo.add(strData);

        // add file to process
        listOfFilestoProcess.add(listFiles.get(i).toString());
      }

      return true;
    }
    catch(Exception e)
    {
      e.printStackTrace();

      log.stackTraceLog(className, "validateFileWithTestRunDuration", "", "", "Exception - ", e);
      return false;
    }
  }

  private long getTimeInMilies(String strtime)
  {
    String calDateTimeFormat = dataTimeFormat.trim();
    Log.debugLog(className, "getTimeInMilies", "", "", "Method called");
    
    try
    {
      if(strtime == null || strtime.trim().equals(""))
        return 0;

      //28/Dec/2012:22:01:23 -0600
      long totalMiliSec = 0;
      
      strtime = strtime.trim();

      if(strtime.length() > calDateTimeFormat.length())
      {
       if(strtime.length() >= calDateTimeFormat.length())
         strtime = strtime.substring(0 , calDateTimeFormat.length());
      }
      Calendar cal = Calendar.getInstance();
      SimpleDateFormat dateFormat = new SimpleDateFormat(calDateTimeFormat);
      cal.setTime(dateFormat.parse(strtime));
      totalMiliSec = cal.getTimeInMillis();
      return totalMiliSec;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getTimeInMilies", "", "", "Exception in converting file date time in mili", e);
      return 0;
    }
  }
  /*
     * convert time into milliseconds
     * @param: Date-time (string)
     * @param: isFormTestRun (boolean)
     * return: time in milliseconds (long)
     */
  public long convertIntoMiliSec(String strtime, boolean isFormTestRun) throws Exception
  {
    String calDateTimeFormat = dataTimeFormat.trim();
    try
    {
      //if the time argument is null then return 0
      if(strtime == null || strtime.trim().equals(""))
        return 0;

      //28/Dec/2012:22:01:23 -0600
      long totalMiliSec = 0;
      
      strtime = strtime.trim();
  

      if(strtime.length() > calDateTimeFormat.length() && !isFormTestRun)
      {
       if(strtime.length() >= calDateTimeFormat.length())
         strtime = strtime.substring(0 , calDateTimeFormat.length());
      }
     
      Calendar cal = Calendar.getInstance();

      SimpleDateFormat dateFormat = null;
      if(isFormTestRun)
      {
        calDateTimeFormat = TestRunDateFormat;
      }

      if(TimeZoneOption == 0 && !isFormTestRun)
      {
        dateFormat = new SimpleDateFormat(calDateTimeFormat);
        TimeZone externalDataTime = TimeZone.getTimeZone(externalDataTimeZone);
        dateFormat.setTimeZone(externalDataTime);
        TimeZone testRunTimeZone = TimeZone.getTimeZone(TestRunTimeZone);

        TimeZone.setDefault(testRunTimeZone);
        cal.setTime(dateFormat.parse(strtime));
        totalMiliSec = cal.getTimeInMillis();
        Date d = new Date(totalMiliSec);
      }
      else
      {
        dateFormat = new SimpleDateFormat(calDateTimeFormat);
        cal.setTime(dateFormat.parse(strtime));
        totalMiliSec = cal.getTimeInMillis();

        if(TimeZoneOption == 1 && !isFormTestRun)
        {
          String operation = addTimeToSyncronizedData.substring(0, 1);
          long addTimeInMilies = rptUtilsBean.convStrToMilliSec(addTimeToSyncronizedData.substring(1));

          if(operation.equals("-"))
            totalMiliSec = totalMiliSec - addTimeInMilies;
          else
            totalMiliSec = totalMiliSec + addTimeInMilies;
        }
        if(TimeZoneOption == 3 && !isFormTestRun)
        {
          if(!isExtraTimeCalculated)
          {
            if(StartTestTimeInMiles > totalMiliSec)
            {
              isAdd = true;
              addTimeInMilies = StartTestTimeInMiles - totalMiliSec;
              totalMiliSec = totalMiliSec + addTimeInMilies;

            }
            else if(StartTestTimeInMiles < totalMiliSec)
            {
              isAdd = false;
              addTimeInMilies = totalMiliSec - StartTestTimeInMiles;
              totalMiliSec = totalMiliSec - addTimeInMilies;
            }
            isExtraTimeCalculated = true;
          }
          else
          {
            if(isAdd)
            {
              totalMiliSec = totalMiliSec + addTimeInMilies;
            }
            else
              totalMiliSec = totalMiliSec - addTimeInMilies;
          }
        }
        
        if(TimeZoneOption == 4)
        {
          if(!isExtraTimeCalculated)
          {
            if(StartTestTimeInMiles > totalMiliSec)
            {
              isAdd = true;
              addTimeInMilies = StartTestTimeInMiles - totalMiliSec;
              totalMiliSec = totalMiliSec + addTimeInMilies;
            }
            else if(StartTestTimeInMiles < totalMiliSec)
            {
             isAdd = false;
             addTimeInMilies = totalMiliSec - StartTestTimeInMiles;
             totalMiliSec = totalMiliSec - addTimeInMilies;
            }
            
            isExtraTimeCalculated = true;
          }
          else
          {
            if(isAdd)
            {
              totalMiliSec = totalMiliSec + addTimeInMilies;
            }
            else
              totalMiliSec = totalMiliSec - addTimeInMilies;
            
          }
        }
      }
      return totalMiliSec;
    }
    catch(Exception e)
    {
      log.debugLog(className, "convertIntoMiliSec", "", "", "Exception getting in convert time: " + strtime + " to milsec,dateformat=" + calDateTimeFormat);
      return 0;
    }
  }

  /*
   * this function calculates columns and initialise data with 0 To get minimum
   * set the some maximum value on specific column
   */
  public void calculateColumns()
  {
    try
    {
      int numberOfURL = 0;

      if(isOverAllUsed)
        numberOfURL++;

      if(isForSpecificURL)
      {
        numberOfURL = numberOfURL + URLList.size();
      }

      if(isStandard)
      {
        arrColIndex = new int[5];
        number_of_columns = numberOfURL * COLUMN_COUNT_FOR_STANDARD;
      }
      else
      {
        arrColIndex = new int[12];
        number_of_columns = numberOfURL * COLUMN_COUNT_FOR_EXTENDED;
      }

      arrDataValues = new double[number_Of_Samples][number_of_columns];
      log.debugLogAlways(className, "calculateColumns", "", "", "number_Of_Samples = " + number_Of_Samples + " ,number_of_columns = " + number_of_columns);
      // this increment is used to set the maximum value to get Minimum value of
      // service time
      int increment = 5;

      if(!isStandard)
        increment = 6;

      for(int i = 0; i < arrDataValues.length; i++)
      {
        for(int j = 0; j < arrDataValues[0].length; j = j + increment)
        {
          for(int k = 0; k < increment; k++)
          {
            if(k == 2)
            {
              arrDataValues[i][j + k] = MAXIMUM_VALUE;
            }
            else
              arrDataValues[i][j + k] = 0;
          }
        }
      }
    }
    catch(Exception e)
    {
      // e.printStackTrace();
      log.stackTraceLog(className, "calculateColumns", "", "", "Exception ", e);
    }
  }

  public ArrayList processFilesToParse(StringBuffer errMsg)
  {
    try
    {
      long processStartTime = System.currentTimeMillis();
      log.debugLogAlways(className, "processFilesToParse", "", "", "Starting import of access logs - reading log files");
   
      if(progressBar != null)
      { 
        progressBar.setVisible(false);
        progressBar.setLabelMsg("Validating file(s) ...");  
        progressBar.setVisible(true);
      }
    
      Timer timer = new Timer("Progress");
      ProgressBarDiff tim = new ProgressBarDiff();
    
      if(!validateFileWithTestRunDuration(listOffiles))
      {
        log.errorLog(className, "processFilesToParse", "", "", "Due to errors files will not processed.");
        errMsg.append("Due to errors files will not processed.");
        return detailsOfFilelist;
      }
       
      calculateColumns();
      
      if(listOfFilestoProcess.size() < 1)
      {
        if(progressBar != null)
          progressBar.setVisible(false);
        errMsg.append("Due to errors files will not processed.");
        return detailsOfFilelist;
      }
    
      //schedule timer for prpgress bar
      if(progressBar !=null)
        timer.schedule(tim, 0, 5000);
      
      for(int i = 0 ; i < URLList.size() ; i++)
      {
        patternMatcherObj =  new PatternMatcher(URLList.get(i).toString());
        patternMatcherList.add(patternMatcherObj);
      }
      
      for(int i = 0; i < listOfFilestoProcess.size(); i++)
      {
        long[] listData = (long[])listOfFilestoProcessInfo.get(i);
        String filePathToParse = listOfFilestoProcess.get(i).toString();
        long startTime = listData[0];
        long endTime = listData[1];
        int tempNumberOfSamples = 0;
 
        progressMessage = "Processing file " + filePathToParse + " ...";
        log.debugLogAlways(className, "processFilesToParse", "", "", progressMessage);
        
        File tempFileObj = new File(filePathToParse);

        // if file is not present then return
        if(!tempFileObj.exists())
        {
          continue;
        }
      
        fileObj = new RandomAccessFile(filePathToParse, "r");
      
        offSet = 0;// set offset 0 for read each file
        
        boolean firstTime = true;

        long endTimeData = EndTestTimeInMilies; 

        long readStartTime = StartTestTimeInMiles; 
        
        if(TimeZoneOption == 3)
        {
          isExtraTimeCalculated = false;
        }
        
        while(StartTestTimeInMiles < AdjustedEndTimeInMiles && (tempNumberOfSamples + 1) <= number_Of_Samples)
        {
          progressMessage = "Processing file " + filePathToParse + " sample " + (tempNumberOfSamples + 1) + " ...";
          
          if(progressBar == null && (tempNumberOfSamples % 60 == 0))
             log.debugLogAlways(className, "ProgressStatus", "", "", progressMessage);
          
          if(startTime < StartTestTimeInMiles && tempNumberOfSamples == 0)
          {
            long startToReadData = StartTestTimeInMiles - startTime;
            offSet = ignoreDataToParse(filePathToParse, startToReadData + startTime);
            
            if(offSet == -1)
            {
              Log.errorLog(className, "processFilesToParse", "", "", "error in reading file");
              if((i + 1) == (listOfFilestoProcess.size() - 1))
              {
                return detailsOfFilelist;
              }
              else
                continue;
            }
            firstTime = false;
          }
       
          readStartTime = readStartTime + interval;
          getAndSetCurData(filePathToParse, readStartTime, tempNumberOfSamples , StartTestTimeInMiles);
          tempNumberOfSamples++;
        }
        
        if(fileObj != null)
        {
          fileObj.close();
          fileObj = null;
        }
      }

      progressMessage = "Generating data from access log file. Please wait...";
      
      log.debugLogAlways(className, "processFilesToParse", "", "", progressMessage);

      int increment = 5;

      if(!isStandard)
        increment = 6;
    
      StringBuffer strOutput = new StringBuffer();

      for(int k = 0; k < arrDataValues.length; k++)
      {
        for(int j = 0; j < arrDataValues[0].length; j = j + increment)
        {
          double requestCount = 0;

          for(int m = 0; m < increment; m++)
          {
            if(m == 0)
            {
              requestCount = arrDataValues[k][j + m];

              if(requestCount > 0)
              {
                arrDataValues[k][j + m] = (arrDataValues[k][j + m] * 1000 )/ interval;
              }
            }

            if(m == 1)
            { 
              if(requestCount > 0)
              {
                arrDataValues[k][j + m] = arrDataValues[k][j + m] / requestCount;
              }
            }

            if(m == 2)
            {
              if(requestCount <= 0)
              {
                arrDataValues[k][j + m] = 0;
              }
            }
            if(!isStandard)
            {
              if(m == 5)
              {
                if(requestCount > 0)
                {
                  //kilo bits per second
                  arrDataValues[k][j + m] = (arrDataValues[k][j + m] * 1000*8) / (throughPutUnit * interval);
                }
              }
            }
            strOutput.append(arrDataValues[k][j + m] + "  ");
          }
        }
        strOutput.append("\n");
      }
    
      String debugFlag = Config.getValue("debugFlag");
      
      if(debugFlag.equals("on"))
        log.debugLog(className, "processFilesToParse", "", "", "Access Log data :\n" + strOutput);
      
     
     // ServletReport report = new ServletReport();
     // StringBuffer errMsg = new StringBuffer();
     // int trNumber = Integer.parseInt(Start.testNum);
     // ImportExternalCSVFileData objImportExternalCSVFileData = new ImportExternalCSVFileData(trNumber);
     // boolean importStatus = report.ImportApacheAccessLogData(Start.urlCodeBase, "../../NetstormServlet", arrDataValues, errMsg, VectorList, trNumber, isStandard);
      detailsOfFilelist.add(VectorList);
      detailsOfFilelist.add(isStandard);
      detailsOfFilelist.add(arrDataValues);
      progressMessage = "Generation of access log data is complete,";
      long timeTaken = System.currentTimeMillis() - processStartTime;
      log.debugLogAlways(className, "processFilesToParse", "", "", progressMessage + " Time Taken to Process : " + timeTaken + " ms." );

      if(progressBar != null)
      {
        progressBar.setVisible(false);
        timer.cancel();
        progressBar.setVisible(false);
      }
    
      return detailsOfFilelist;
    }
    catch(Exception e)
    {
      errMsg.append("Exception in Process Files.");
      log.stackTraceLog(className, "processFilesToParse", "", "", "Exception in Process Files.", e);
      return detailsOfFilelist;
    }
  }

  /*
   * This function is used to ignore starting data of which is not match with
   * Test run
   */
  public long ignoreDataToParse(String filePathToParse, long stopTimeToRead)
  {
    try
    {
      long backOffset = 0;
      File tempFileObj = new File(filePathToParse);

      // if file is not present then return
      if(!tempFileObj.exists())
      {
      	log.debugLog(className, "getAndSetCurData", "", "", "Log file not present. logFileName = " + filePathToParse);
        return 0;
      }
      
      RandomAccessFile  fileObjTemp = new RandomAccessFile(filePathToParse, "r");

      int i = 0;
      String ingoreLines = "";

      while(((ingoreLines = fileObjTemp.readLine()) != null))
      {
        long RequestTime = convertIntoMiliSec(getSpecificColumnData(ingoreLines, dateTime_field_index, true), false);
        if(RequestTime >= stopTimeToRead)
        {
          break;
        }
        backOffset = fileObjTemp.getFilePointer();
        i++;
      }
      if(ingoreLines == null)
      {
        return -1;
      }
      
      fileObjTemp.close();
      return backOffset;
    }
    catch(Exception e)
    {
      // e.printStackTrace();
      log.stackTraceLog(filePathToParse, "ignoreDataToParse", "", "", "error in reading file", e);
      return -1;
    }

  }

  public boolean isNumeric(String str)
  {
    try
    {
      Integer i = Integer.parseInt(str);
    }
    catch(NumberFormatException nfe)
    {
      return false;
    }
    return true;
  }

  /*
   * This method will read the data form file on specific interval parse
   * appropriate data form each data-line set into global 2-D array for Test run
   * data
   */
  public boolean getAndSetCurData(String filePathToParse, long stopTimeToParse, int SampleIndex , long firstStartTime)
  {
    //Log.debugLog(className, "getAndSetCurData", "", "", "Method called. log file name = " + filePathToParse);
    try
    {
      long backOffset = 0;

      backOffset = offSet;

      fileObj.seek(offSet);

      String URL = "";

      String dataLine = null;

      while((dataLine = fileObj.readLine()) != null)
      {
        if(dataLine.equals(""))
        {
          //Log.debugLog(className, "getAndSetCurData", "", "", "empty data line found , so moving for next line.");
          continue;
        }

        long RequestTime = convertIntoMiliSec(getSpecificColumnData(dataLine, dateTime_field_index, true), false);
       
        
        if(RequestTime == 0)
          continue;
        
        if(RequestTime >= stopTimeToParse)
        {
          break;
        }
        
        String tmpURL = getSpecificColumnData(dataLine, url_field_index, true);
     

        String[] tempArr = rptUtilsBean.split(tmpURL, " ");
       
        // it spit into 3 - "GET /enrollment-rest/keepalive.jsp HTTP/1.1"
        if(tempArr.length == 3 && (!isRequestLine)) 
        {
          URL = tempArr[1];
        }
        else
        {
          URL = tmpURL;
          //Log.debugLog(className, "getAndSetCurData", "", "", "Error occur while parsing URL from: " + dataLine);
        }

        Double SvcTime = calcSVCTimeData(getSpecificColumnData(dataLine, svctime_field_index, false));

        String strStatus = getSpecificColumnData(dataLine, status_field_index, false);
        
        // check status should be numeric value
        if(!isNumeric(strStatus))
          continue;
        
        Double status = Double.parseDouble(strStatus);

        String strDataSize = getSpecificColumnData(dataLine, dataSize_field_index, false);

        if(!isNumeric(strDataSize))
        {
          strDataSize = "0";
        }

        long tempDataSize = Long.parseLong(strDataSize);
        
        //Calculating Sample index by Data line
        int absSampleIndex = (int)Math.floor((((double)(RequestTime - firstStartTime))/interval));
        
        int sampleIndx = SampleIndex;
        
        if(sampleIndx != absSampleIndex)
          sampleIndx = absSampleIndex;
         
        if(isOverAllUsed)
        {
          int arrColIndex[] = getColumnIndexArray(status, 0);
          arrDataValues[sampleIndx][arrColIndex[0]]++;
          
          arrDataValues[sampleIndx][arrColIndex[1]] = arrDataValues[sampleIndx][arrColIndex[1]] + SvcTime;

          if(arrDataValues[sampleIndx][arrColIndex[3]] < SvcTime)
            arrDataValues[sampleIndx][arrColIndex[3]] = SvcTime;
          if(arrDataValues[sampleIndx][arrColIndex[2]] > SvcTime)
            arrDataValues[sampleIndx][arrColIndex[2]] = SvcTime;

          arrDataValues[sampleIndx][arrColIndex[4]]++;

          if(!isStandard)
          {
            arrDataValues[sampleIndx][arrColIndex[5]] = arrDataValues[sampleIndx][arrColIndex[5]] + tempDataSize;
            arrDataValues[sampleIndx][arrColIndex[6]]++;
            arrDataValues[sampleIndx][arrColIndex[7]] = arrDataValues[sampleIndx][arrColIndex[7]] + SvcTime;

            if(arrDataValues[sampleIndx][arrColIndex[9]] < SvcTime)
              arrDataValues[sampleIndx][arrColIndex[9]] = SvcTime;

            if(arrDataValues[sampleIndx][arrColIndex[8]] > SvcTime)
              arrDataValues[sampleIndx][arrColIndex[8]] = SvcTime;

            arrDataValues[sampleIndx][arrColIndex[10]]++;
            arrDataValues[sampleIndx][arrColIndex[11]] = arrDataValues[sampleIndx][arrColIndex[11]] + tempDataSize;
          }
        }

        if(isForSpecificURL)
        {
          for(int i = 0; i < URLList.size(); i++)
          {
            int increment = 5;
            if(isStandard)
            { 
              if(!isOverAllUsed)  
                increment = COLUMN_COUNT_FOR_STANDARD * (i);
              else
                increment = COLUMN_COUNT_FOR_STANDARD * (i + 1);
            }
            else
            {
              if(!isOverAllUsed)
                increment = COLUMN_COUNT_FOR_EXTENDED * (i);
              else
                increment = COLUMN_COUNT_FOR_EXTENDED * (i + 1);
            }

            int arrColIndex[] = getColumnIndexArray(status, increment);

            patternMatcherObj = patternMatcherList.get(i);

            if(matchPattern(URL, patternMatcherObj))
            {
              arrDataValues[sampleIndx][arrColIndex[0]]++;

              arrDataValues[sampleIndx][arrColIndex[1]] = arrDataValues[sampleIndx][arrColIndex[1]] + SvcTime;

              if(arrDataValues[sampleIndx][arrColIndex[3]] < SvcTime)
                arrDataValues[sampleIndx][arrColIndex[3]] = SvcTime;

              if(arrDataValues[sampleIndx][arrColIndex[2]] > SvcTime)
                arrDataValues[sampleIndx][arrColIndex[2]] = SvcTime;

              arrDataValues[sampleIndx][arrColIndex[4]]++;
              if(!isStandard)
              {
                arrDataValues[sampleIndx][arrColIndex[5]] = arrDataValues[sampleIndx][arrColIndex[5]] + tempDataSize;
                arrDataValues[sampleIndx][arrColIndex[6]]++;
                arrDataValues[sampleIndx][arrColIndex[7]] = arrDataValues[sampleIndx][arrColIndex[7]] + SvcTime;

                if(arrDataValues[sampleIndx][arrColIndex[9]] < SvcTime)
                  arrDataValues[sampleIndx][arrColIndex[9]] = SvcTime;

                if(arrDataValues[sampleIndx][arrColIndex[8]] > SvcTime)
                  arrDataValues[sampleIndx][arrColIndex[8]] = SvcTime;

                arrDataValues[sampleIndx][arrColIndex[10]]++;

                arrDataValues[sampleIndx][arrColIndex[11]] = arrDataValues[sampleIndx][arrColIndex[11]] + tempDataSize;
              }
            }
          }
        }
        
        backOffset = fileObj.getFilePointer();
      }

      offSet = backOffset;// Set the offSet to previous line
      return true;
    }
    catch(Exception e)
    {
      log.stackTraceLog(className, "getAndSetCurData", "", "", "Exception - ", e);
      return false;
    }
  }

  private String getEscapedChars(String str)
  {
    for(String charToEncode : charsToEscape)
      str = str.replace(charToEncode, "\\" + charToEncode);

    return "*" + str + "*";
  }

  /*
   * This method provide the index array to fill data in appropriate column
   * index of global 2-D array
   */
  public int[] getColumnIndexArray(double status, int increment)
  {
    if(isStandard)
    {
      for(int i = 0; i < arrColIndex.length; i++)
      {
        arrColIndex[i] = i + increment;
      }
    }
    else
    {
      for(int i = 0; i <= 5; i++)
      {
        arrColIndex[i] = i + increment;
      }

      int indexIncr = 6;
      if(String.valueOf(status).startsWith("1"))
        indexIncr = 6 + increment;
      else if(String.valueOf(status).startsWith("2"))
        indexIncr = 12 + increment;
      else if(String.valueOf(status).startsWith("3"))
        indexIncr = 18 + increment;
      else if(String.valueOf(status).startsWith("4"))
        indexIncr = 24 + increment;
      else if(String.valueOf(status).startsWith("5"))
        indexIncr = 30 + increment;
      else
        indexIncr = 36 + increment;

      for(int i = 6; i <= 11; i++)
      {
        arrColIndex[i] = indexIncr;
        indexIncr++;
      }
    }

    return arrColIndex;
  }

  /*
   * calculate svc time in seconds
   */
  private double calcSVCTimeData(String strSVCTimeData)
  {
    try
    {
      double svcTimeDataInSec = 0;

      if(strSVCTimeData.trim().equals("-"))
        return 0;
      // if svc Time data is in micro seconds only :1031302
      if(!strSVCTimeData.contains("/"))
      {

        if(unitOption.equalsIgnoreCase("mis"))
          svcTimeDataInSec = (Double.parseDouble(strSVCTimeData) / CONVERTER_UNIT_SEC_TO_MILI);
        else if(unitOption.equalsIgnoreCase("mic"))
          svcTimeDataInSec = (Double.parseDouble(strSVCTimeData) / CONVERTER_UNIT_SEC_TO_MICRO);
        else
          svcTimeDataInSec = Double.parseDouble(strSVCTimeData);
      }
      else
      {
        // SVC time data like --> [1/1031302] ---> converted from
        // getSpecificColumnData() method--> 1/1031302
        String[] arrData = rptUtilsBean.strToArrayData(strSVCTimeData, "/");

        double svcDataInSec = Double.parseDouble(arrData[0]);
        double svcDataInMicroSec = Double.parseDouble(arrData[1]);

        if(unitOption.equalsIgnoreCase("mis"))
          svcTimeDataInSec = (svcDataInMicroSec / CONVERTER_UNIT_SEC_TO_MILI);
        else if(unitOption.equalsIgnoreCase("mic"))
          svcTimeDataInSec = (svcDataInMicroSec / CONVERTER_UNIT_SEC_TO_MICRO);
        else
          svcTimeDataInSec = svcDataInMicroSec;

        // this is security check, If svc time data (in seconds) is greater than
        // svc time data (in micro seconds) then use svc time (in seconds).
        // This condition may arise when micro second svc time data exceed the
        // data limit
        if(svcTimeDataInSec < svcDataInSec)
          svcTimeDataInSec = svcDataInSec;

        log.debugLog(className, "calcSVCTimeData", "", "", "SVC time data (in seconds) = " + svcDataInSec + ", SVC time data (in micro seconds) = " + svcDataInMicroSec + ", Calculated SVC Time data in seconds = " + svcTimeDataInSec);
      }
      return svcTimeDataInSec;
    }
    catch(Exception e)
    {
      log.stackTraceLog(className, "calcSVCTimeData", "", "", "Exception - ", e);
      return 0;
    }
  }

  /*
   * This function used to read the data line and give the specific column value
   */
  private String getSpecificColumnData(String dataLine, int colIndex, boolean isToRemoveSplChar)
  {
    try
    {
      // Data Line --> 11.120.230.25 - - [28/Jan/2010:10:08:53 -0500] 1/1031302
      // "POST /register HTTP/1.1" 200 916 "-" "Mozilla_CA/4.79 [en] (X11; U;
      // Linux 2.4.18-3d_epoll i686)" "SetCookie-"
      // "SESSde50f29931e6a8172e3a27390fd1992f=5j26cb231b4799i1g0fjh3k760;
      // mt_redirect=true" "-" "-"
      boolean isStartWithSpecialCharecter = false;
      ArrayList arrData = new ArrayList();
      String TabString = "tab";
      String SpaceString = "space";

      // Data line splitted by white spaces

      String arrDataTemp[] = null;

      if(separator.equalsIgnoreCase(TabString))
      {
        arrDataTemp = dataLine.split("\t");
      }
      else if(separator.equalsIgnoreCase(SpaceString))
      {
        arrDataTemp = dataLine.split(" ");
      }
      else
      {
        arrDataTemp = rptUtilsBean.split(dataLine,separator); // this condition for others
        // delimiter
      }

      // Taken two string arrays occuring at start and end position of the time
      // field
      String[] Specialcharacters = new String[]{"[", "{", "\"", "<", "(", "-", "'"};
      String[] Specialcharacters2 = new String[]{"]", "}", "\"", ">", ")", "-", "'"};

      // Getting index for parse. As it is counted from '1' from GUI that's why
      // one is subtracted here.
      int parseIndex = colIndex - 1;
      String time = "";
      boolean doubleQuoteFlag = false;
      boolean bracketFlag = false;

      for(int i = 0; i < arrDataTemp.length; i++)
      {
        if((arrDataTemp[i].trim().startsWith("[") && (arrDataTemp[i].trim().endsWith("]"))))
        {
          if(bracketFlag || doubleQuoteFlag)
          {
            time = time + " " + arrDataTemp[i];
          }
          else
          {
            arrData.add(time + " " + arrDataTemp[i]);
          }
        }
        else if((arrDataTemp[i].trim().startsWith("\"")) && ((arrDataTemp[i].trim().endsWith("\"")) && (!(arrDataTemp[i].trim().endsWith("\\\"")))))
        {
          if(bracketFlag || doubleQuoteFlag)
          {
            time = time + " " + arrDataTemp[i];
          }
          else
          {
            arrData.add(time + " " + arrDataTemp[i]);
          }
        }
        else if(arrDataTemp[i].trim().startsWith("["))
        {
          bracketFlag = true;
          time = time + " " + arrDataTemp[i];
        }
        else if(arrDataTemp[i].trim().startsWith("\""))
        {
          doubleQuoteFlag = true;
          time = time + " " + arrDataTemp[i];
        }
        else if(doubleQuoteFlag && (arrDataTemp[i].trim().endsWith("\"") && !arrDataTemp[i].trim().endsWith("\\\"")))
        {
          doubleQuoteFlag = false;
          time = time + " " + arrDataTemp[i];
          arrData.add(time);
          time = "";
        }
        else if(bracketFlag && (arrDataTemp[i].trim().endsWith("]")))
        {
          bracketFlag = false;
          time = time + " " + arrDataTemp[i];
          arrData.add(time);
          time = "";
        }
        else if(bracketFlag)
        {
          time = time + " " + arrDataTemp[i];
        }
        else if(doubleQuoteFlag)
        {
          time = time + " " + arrDataTemp[i];
        }
        else
        {
          arrData.add(arrDataTemp[i]);
        }
      } // end of for loop

      String strSVCTimeData = arrData.get(parseIndex).toString().trim();

      // this is to remove spl char if String beg & end with it.
      if(isToRemoveSplChar)
      {
        for(int p = 0; p < Specialcharacters.length; p++)
        {
          if((strSVCTimeData.startsWith(Specialcharacters[p])) && (strSVCTimeData.endsWith(Specialcharacters2[p])))
          { 
            isStartWithSpecialCharecter = true;
            strSVCTimeData = strSVCTimeData.substring(1, (strSVCTimeData.length() - 1));
          }
        }// end of for loop

      }
    
      if(dateTime_field_index == colIndex  && isStartWithSpecialCharecter == false  && arrData.size() >= (parseIndex + 1))
      {
        strSVCTimeData = strSVCTimeData + " " + arrData.get(parseIndex + 1).toString().trim();
      }
        
      return strSVCTimeData;
    }
    catch(Exception e)
    {
      return "";
    }
  }

  /*
   * This function read first data line
   */
  public String getfirstLine(File file)
  {
    try
    {
      FileInputStream fs = new FileInputStream(file);
      BufferedReader br = new BufferedReader(new InputStreamReader(fs));
      String firstline = "";

      long startTime = 0;

      while((firstline = br.readLine()) != null)
      {
        startTime = convertIntoMiliSec(getSpecificColumnData(firstline, dateTime_field_index, true), false);
      
        if(startTime != 0)
        {
          break;
        }
      }
 
      br.close();
      fs.close();
      return firstline;
    }
    catch(java.io.FileNotFoundException e)
    {
      log.debugLog(className, "getFirstLine", "", "", "error in reading first line of file");  
      return null;
    }
    catch(java.io.IOException e)
    {
      log.debugLog(className, "getFirstLine", "", "", "error in reading first line of file");
      return null;
    }
    catch(Exception e)
    {
     log.debugLog(className, "getFirstLine", "", "", "error in reading first line of file");
     return null;
    }
  }

  /*
   * This function read only last line of the file
   */
  public String getlastLine(File file)
  {
    try
    {
      RandomAccessFile fileHandler = new RandomAccessFile(file, "r");
      long fileLength = file.length() - 1;
      StringBuilder sb = new StringBuilder();

      long endTime = 0;
      for(long filePointer = fileLength; filePointer != -1; filePointer--)
      {
        fileHandler.seek(filePointer);
        int readByte = fileHandler.readByte();

        if(readByte == 0xA)
        {
          if(filePointer == fileLength)
          {
            continue;
          }
          else
          {
            if(!sb.toString().trim().equals(""))
            {
              String strField =  getSpecificColumnData(sb.reverse().toString().trim(), dateTime_field_index, true) ;
              endTime = convertIntoMiliSec(strField, false);
         
              if(endTime != 0)
              {
                break;
                
              }
            }
            sb = new StringBuilder();
          }
        }
        else if(readByte == 0xD)
        {
          if(filePointer == fileLength - 1)
          {
            continue;
          }
          else
          {
            if(!sb.toString().trim().equals(""))
            {
              String strField =  getSpecificColumnData(sb.reverse().toString().trim(), dateTime_field_index, true) ;
              endTime = convertIntoMiliSec(strField, false);
              if(endTime != 0)
              {
                break;
              }
            }
            sb = new StringBuilder();
          }
        }
        sb.append((char)readByte);
      }
      
      String lastLine = sb.toString();
      
      fileHandler.close();

      return lastLine;
    }
    catch(java.io.FileNotFoundException e)
    {
      log.debugLog(className, "getLastLine", "", "", "error in reading first line of file");
      return null;
    }
    catch(Exception e)
    {
      log.debugLog(className, "getLastLine", "", "", "error in reading first line of file");
      return null;
    }
  }

  private boolean matchPattern(String line, PatternMatcher patternObj)
  {
    boolean isAllPatternFound = true;
    Pattern tempPattern = Pattern.compile(patternObj.getPatternToMatch());
    Matcher m = tempPattern.matcher(line);
    boolean matchFound = m.matches();

    if(!matchFound)
    {
      isAllPatternFound = false;
    }

    return isAllPatternFound;
  }

  class PatternMatcher
  {
    // This is to match in log to get specified counts
    private String patternToMatch;

    public PatternMatcher(String pattern)
    {
      this.patternToMatch = pattern.replace("*", "[a-zA-z0-9\\s\\W.]*");
    }

    public String getPatternToMatch()
    {
      return patternToMatch;
    }
  }

  class ProgressBarDiff extends TimerTask
  {
    public void run()
    {
      try
      {
        if(progressBar != null)
        {
          progressBar.setVisible(false);
          progressBar.setLabelMsg(progressMessage);
          progressBar.setVisible(true);
        }
        else
        {
          log.debugLogAlways(className, "ProgressStatus", "", "", progressMessage);
        }
      }
      catch(Exception e)
      {
        progressBar.setVisible(false);
      }

    }
  }
  
  public static void main(String args[]) throws Exception
  {
    
    ArrayList lis = new ArrayList();

    lis.add("C:\\Users\\compass-56\\Desktop\\Doc\\6mint.log");
   // lis.add("C:\\Users\\compass-56\\Desktop\\Doc\\6mint1.log");
    //lis.add("C:/Users/compass/Desktop/perf_access.2012-12-28 (2).log");

    ArrayList urlVectorNameMap = new ArrayList();

    urlVectorNameMap.add("/sharma*");
    urlVectorNameMap.add("/ritesh*");
    ImportApacheLogSettingDTO dtoObj = new ImportApacheLogSettingDTO();
    dtoObj.setUrlName(urlVectorNameMap);
    dtoObj.setUrlVectorName(urlVectorNameMap);
    dtoObj.setSelectedFileList(lis);
    dtoObj.setUrlField(6);
    dtoObj.setResponseTimeField(5);
    dtoObj.isRequestLineUsed(false);
    dtoObj.setDataSizeField(8);
    dtoObj.setStatusField(7);
    dtoObj.setDateTimeField(4);
    dtoObj.setStanderd(true);
    dtoObj.setVectorName("AllURL");
    dtoObj.setForAllUrl(true);
    
    dtoObj.setForSpecificUrl(false);
    dtoObj.setResponseTimeUnit("mis");
  
    dtoObj.setDateTimeFormat("dd/MMM/yyyy:HH:mm:ss");

    dtoObj.setSepratorField("space");
    //20/Jun/2012:04:18:56 28/Dec/2012:22:01:22
    dtoObj.setStartTestRunTime("6/20/2012 4:18:00");
    dtoObj.setEndTestRunTime("6/20/2012 4:27:30");

    dtoObj.setTimeZoneForTestRun("Asia/Calcutta");
    dtoObj.setDateTimeFormatForTestRun("MM/dd/yyyy HH:mm:ss");

    dtoObj.setImportDataTimeSelectionFlag(0);

    dtoObj.setDateTimeSelectionZoneFlag(3);
    JFrame f = new JFrame("TestData");
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    ProgressBar p = new ProgressBar(f, 333666, "Start process");
    p.setLabelMsg("Start process");
    p.setVisible(false);
    ImportAccessLogData ap = new ImportAccessLogData(dtoObj, null ,"21243");
    ap.progressMessage = "Start process";
    ap.processFilesToParse(new StringBuffer());
    
    System.exit(0);
  }
}

class FileInfo implements Serializable
{
   private String filePath ;
   private String firstDataLine = "";
   private String endDataLine = "";
   private String startTime = "";
   private String endTime = "";
   private long startTimeInMilies = 0;
   private long endTimeInMilies = 0;
   private boolean state  = true;
   private String description = "";
  
   public String getFilePath() {
    return filePath;
   }
  
   public void setFilePath(String filePath) {
    this.filePath = filePath;
   }
  
   public String getFirstDataLine() {
    return firstDataLine;
   }
  
   public void setFirstDataLine(String firstDataLine) {
    this.firstDataLine = firstDataLine;
   }
  
   public String getEndDataLine() {
    return endDataLine;
   }
  
   public void setEndDataLine(String endDataLine) {
    this.endDataLine = endDataLine;
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
   
   public long getStartTimeInMilies() {
    return startTimeInMilies;
   }
   
   public void setStartTimeInMilies(long startTimeInMilies) {
    this.startTimeInMilies = startTimeInMilies;
   }
   
   public long getEndTimeInMilies() {
    return endTimeInMilies;
   }
   
   public void setEndTimeInMilies(long endTimeInMilies) {
    this.endTimeInMilies = endTimeInMilies;
   }

  public boolean isState() {
    return state;
  }

  public void setState(boolean state , String description) 
  {
    this.description = description;
    this.state = state;
  }

  public String getDescription() {
    return description;
  }
}

/**----------------------------------------------------------------------------
 * Name       Scenario.java
 * Purpose    The purpose of this utility is to read scenario file and get Target complition time of running test run
 *
 * @author    Abhishek
 * @version   2.0.0
 *
 * Modification History
 *   6/13/07:Abhishek:2.0.0 - Initial Version.
 *---------------------------------------------------------------------------**/

package pac1.Bean;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;

public class Scenario
{
  static String className = "Scenario";
  static String workPath = Config.getWorkPath();
  private String totalProcessingTime;

  public Scenario()
  {
  }

  public String getTotalProcessingTime()
  {
    return totalProcessingTime;
  }

  public void setTotalProcessingTime(long startProcessingTime, long endProcessingTime)
  {
    this.totalProcessingTime = rptUtilsBean.convertMilliSecToSecs(endProcessingTime - startProcessingTime);
  }

  public static String getTestRunDirPath(int testRun)
  {
    return(workPath + "/webapps/logs/TR" + testRun);
  }

  
  // this method is used to get sorted scenario file name
  //Arguments :-
  //testRun = TR number Eg: - 2345
  //patternForScenFileName = 
  public static String getSortedTRScenFileWithAbsolutePath(int testRun)
  {
    Log.debugLog(className, "getSortedTRScenarioFileWithAbsolutePath", "", "", "Method called. Test run number =  " + testRun);
    //This method get the path up to TR dir
    //Eg: /home/netstorm/work/webapps/logs/TRXXXX
    String scenarioFile = getTestRunDirPath(testRun);
    
    try
    {
      String sortedFile = "sorted_scenario.conf";
    
      File fileScen = new File(scenarioFile + "/" + sortedFile);
      //checking sorted_scenario.conf file exist it will return sorted_scenario.conf file with absolute path
      if(fileScen.exists())
        return fileScen.getAbsolutePath();
      else
      {
        //getting list of sorted file in TR 
        //we are assuming it is at zeroth position in File array
        File fileArr[] = rptUtilsBean.getListOfFilesWithMatchingPatteren(Scenario.getTestRunDirPath(testRun), "sorted_*" + ".conf");
      
        if(fileArr != null)
        {
          return fileArr[0].getAbsolutePath();
        }
        else //sorted file not present it will return scenario file if the TR
          return scenarioFile + "/" + "scenario";
      }
    }
    catch (Exception e) 
    {
      Log.stackTraceLog(className, "getSortedTRScenarioFileWithAbsolutePath", "", "", "Exception", e);
      return scenarioFile + "/" + "scenario";
    }
  }
  
  public static String[] getKeywordValues(String keyword, int testRun)
  {
    Log.debugLog(className, "getKeywordValues", "", "", "Method called. Keyword =  " + keyword);
    String[] keyValues = null;

    try
    {
      String scenKwrd = "";
      // Creating path for scenario file
      String scenarioFile = getSortedTRScenFileWithAbsolutePath(testRun);
      
      // open file object in buffer stram
      FileInputStream fis = new FileInputStream(scenarioFile);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));

      while((scenKwrd = br.readLine()) != null)
      {
        if(scenKwrd.startsWith(keyword))
        {
          StringTokenizer st = new StringTokenizer(scenKwrd);
          keyValues = new String[st.countTokens() - 1];
          int j = 0;
          st.nextToken();  // Skip keyward
          while(st.hasMoreTokens())
          {
            keyValues[j] = (st.nextToken()).trim();
            j++;
          }
          break; // This is for scalar keyword only
        }
      }
      br.close(); // closing buffer stream
      fis.close();

      return keyValues;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getKeywordValues", "", "", "Exception", e);
      return null;
    }
  }

//this to retrieve all values for the same keyword.
  public static ArrayList getKeywordValuesMoreThanOneExist(String keyword, int testRun)
  {
    Log.debugLog(className, "getKeywordValuesMoreThanOnceExist", "", "", "Method called. Keyword =  " + keyword);
    ArrayList<String []> arrReadLine = new ArrayList<String []>();
    String[] keyValues = null;
    
    try
    {
      String scenKwrd = "";
      // Creating path for scenario file
      String scenarioFile = getTestRunDirPath(testRun) + "/scenario";
      // open file object in buffer stram
      FileInputStream fis = new FileInputStream(scenarioFile);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));
      
      while((scenKwrd = br.readLine()) != null)
      {
        if(scenKwrd.startsWith(keyword))
        {
          StringTokenizer st = new StringTokenizer(scenKwrd);
          keyValues = new String[st.countTokens() - 1];
          int j = 0;
          st.nextToken();  // Skip keyward
          while(st.hasMoreTokens())
          {
            keyValues[j] = (st.nextToken()).trim();
            j++;
          }
          arrReadLine.add(keyValues);
        }
      }
      br.close(); // closing buffer stream
      fis.close();

      return arrReadLine;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getKeywordValuesMoreThanOnceExist", "", "", "Exception", e);
      return null;
    }
  }
  
  // this function is to get the keyword values from global.dat file
  public static String[] getKeywordValuesFromGlobalFile(String keyword, int testRun)
  {
    Log.debugLog(className, "getKeywordValuesFromGlobalFile", "", "", "Method called. Keyword =  " + keyword);
    String[] keyValues = null;

    try
    {
      String scenKwrd = "";
      // Creating path for scenario file
      String scenarioFile = getTestRunDirPath(testRun) + "/global.dat";
      // open file object in buffer stram
      FileInputStream fis = new FileInputStream(scenarioFile);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));

      while((scenKwrd = br.readLine()) != null)
      {
        if(scenKwrd.startsWith(keyword))
        {
          StringTokenizer st = new StringTokenizer(scenKwrd);
          keyValues = new String[st.countTokens() - 1];
          int j = 0;
          st.nextToken();  // Skip keyward
          while(st.hasMoreTokens())
          {
            keyValues[j] = (st.nextToken()).trim();
            j++;
          }
          break; // This is for scalar keyword only
        }
      }
      br.close(); // closing buffer stream
      fis.close();

      return keyValues;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getKeywordValuesFromGlobalFile", "", "", "Exception", e);
      return null;
    }
  }


  /*public static String getTargetCompTime(int testRun)
  {
    Log.debugLog(className, "getTargetCompTime", "", "", "Method called. Test Run = " + testRun);
    try
    {
      int intHH = 0;
      int intMM = 0;
      int intSS = 0;

      String values[] = getKeywordValues("RUN_TIME", testRun);
      if(values == null)
        return "Till stopped by user";  // Default value if keyword is not found in the sceanrio file

      String strRunTimeUnit = values[1];
      String strTime = values[0];

      if(strRunTimeUnit.equals("I"))
        strTime = "Till stopped by user";
      else if(strRunTimeUnit.equals("C"))
        strTime = strTime + " Sessions Completion";
      else
      {
        int intTime_1 = Integer.parseInt(strTime);
        if(strRunTimeUnit.equals("H"))
          intHH = intTime_1;
        else if(strRunTimeUnit.equals("M"))
          intMM = intTime_1;
        else if(strRunTimeUnit.equals("S"))
        {
          intHH = intTime_1 / 3600;
          intTime_1 = intTime_1 % 3600;
          intMM = intTime_1 / 60;
          intTime_1 = intTime_1 % 60;
          intSS = intTime_1;
        }
        if (intHH < 10)
          strTime = "0" + String.valueOf(intHH) + ":";
        else
          strTime = String.valueOf(intHH) + ":";
        if (intMM < 10)
          strTime = strTime + "0" + String.valueOf(intMM) + ":";
        else
          strTime = strTime + String.valueOf(intMM) + ":";
        if (intSS <10 )
          strTime = strTime + "0" + String.valueOf(intSS);
        else
          strTime = strTime + String.valueOf(intSS);

        //strTime = strTime + " (HH:MM:SS)";
      }
      Log.debugLog(className, "getTargetCompTime", "", "", "Target Completion = " + strTime);
      return strTime;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getTargetCompTime", "", "", "Exception", e);
      return "";
    }
  }*/

  // this function is to get the target completion from global.dat file
  public static String getTargetCompTimeFromGlobalFile(int testRun)
  {
    Log.debugLog(className, "getTargetCompTimeFromGlobalFile", "", "", "Method called. Test Run = " + testRun);
    try
    {
      String values[] = getKeywordValuesFromGlobalFile("TARGET_COMPLETION", testRun);
      if(values == null)
        return "NA";  // Default value if keyword is not found in the global.dat file

      String strRunTimeUnit = values[0];
      String strTime = "";

      if(strRunTimeUnit.equals("INDEFINITE"))
        strTime = "Till stopped by user";
      else if(strRunTimeUnit.equals("SESSIONS"))
        strTime = values[1] + " Sessions Completion";
      else if(strRunTimeUnit.equals("COMPLETION"))
        strTime = "Till Completion";
      else if(strRunTimeUnit.equals("TIME"))// for TIME unit
        strTime = values[1];
      else
        strTime = "NA";

      Log.debugLog(className, "getTargetCompTimeFromGlobalFile", "", "", "Target Completion = " + strTime);

      return strTime;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getTargetCompTimeFromGlobalFile", "", "", "Exception", e);
      return "";
    }
  }


  // if DEBUG_TRACE keyword avail in scenario then return true, else false
  public static boolean getDebugTraceLogFlagVal(int testRun)
  {
    Log.debugLog(className, "getDebugTraceLogFlagVal", "", "", "Method called. Test Run = " + testRun);

    try
    {
      String values[] = getKeywordValues("DEBUG_TRACE", testRun);
      if(values == null)
        return false;
      else
      {
        if(Integer.parseInt(values[0]) != 0)
          return true;
        return false;
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getDebugTraceLogFlagVal", "", "", "Exception", e);
      return false;
    }
  }

  //if ALERT_PROFILE keyword avail in scenario then return value, else null
  public static String getAlertProfileVal(int testRun)
  {
    Log.debugLog(className, "getAlertProfileVal", "", "", "Method called. Test Run = " + testRun);

    try
    {
      String values[] = getKeywordValues("ALERT_PROFILE", testRun);
      if(values == null)
        return null;
      else
        return values[0].trim();
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getAlertProfileVal", "", "", "Exception", e);
      return null;
    }
  }
  
  // get the netstorm port from NSPort file in testRun
  public static int getNetstormPort(int testRun)
  {
    Log.debugLog(className, "getNetstormPort", "", "", "Method Starts.");

    try
    {
      String filePath = getTestRunDirPath(testRun) + "/" +"NSPort" ;
      String strLine = "";
      int nsPort = -1;
      File nsPortFile = new File(filePath);

      if(!nsPortFile.exists())
      {
        Log.errorLog(className, "getNetstormPort", "", "", filePath + " not found in the Test Run - " + testRun);
        return -1;
      }

      BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(nsPortFile)));

      if((strLine = br.readLine()) !=  null)
        strLine = strLine.trim();

      br.close();

      if(strLine.equals(""))
      {
        Log.errorLog(className, "getNetstormPort", "", "", filePath + " is empty in the Test Run - " + testRun);
        return -1;
      }

      try
      {
        nsPort = Integer.parseInt(strLine);
      }
      catch(Exception e)
      {
        Log.errorLog(className, "getNetstormPort", "", "", filePath + " has wrong data to process, in the Test Run - " + testRun);
        return -1;
      }

      return nsPort;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getNetstormPort", "", "", "Exception : ", e);
      return -1;
    }
  }


  // get the value from summary.top file by index
  private static String getValueFromSummaryTopByIdx(int testRun, int index)
  {
    Log.debugLog(className, "getValueFromSummaryTopByIdx", "", "", "Method called. Index =  " + index);
    String keyValue = "";

    try
    {
      // Creating path for summary.top file
      String summaryTopFile = getTestRunDirPath(testRun) + "/summary.top";

      File fileObj = new File(summaryTopFile);
      if(!fileObj.exists())
      {
        Log.errorLog(className, "getValueFromSummaryTopByIdx", "", "", summaryTopFile + " not present in the Test Run - " + testRun);
        return "";
      }

      // open file object in buffer stram
      FileInputStream fis = new FileInputStream(summaryTopFile);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));

      String dataLine = "";
      int i = 0;
      while(i == 0)
      {
        dataLine = br.readLine();
        i++;
      }

      br.close(); // closing buffer stream
      fis.close();

      String[] arrData = rptUtilsBean.strToArrayData(dataLine, "|");
      if(arrData == null)
      {
        Log.errorLog(className, "getValueFromSummaryTopByIdx", "", "", summaryTopFile + " has wrong data to process, in the Test Run - " + testRun);
        return "";
      }
      else if(arrData.length < index)
      {
        Log.errorLog(className, "getValueFromSummaryTopByIdx", "", "", summaryTopFile + " has wrong data to process, in the Test Run - " + testRun);
        return "";
      }
      else
        keyValue = arrData[index];

      return keyValue;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getValueFromSummaryTopByIdx", "", "", "Exception", e);
      return "";
    }
  }


  // get the test run start date/time from summary.top file
  public static String getTestRunStartTime(int testRun)
  {
    Log.debugLog(className, "getTestRunStartTime", "", "", "Method called. Test Run = " + testRun);

    try
    {
      // testRun start date/time available on second index in summary.top file
      String dataVal = getValueFromSummaryTopByIdx(testRun, 2);
      if(dataVal.equals(""))
        dataVal = "00/00/00 00:00:00";

      return dataVal;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getTestRunStartTime", "", "", "Exception", e);
      return "";
    }
  }


  // get the scenario name of test run from summary.top file
  public static String getScenarioName(int testRun)
  {
    Log.debugLog(className, "getScenarioName", "", "", "Method called. Test Run = " + testRun);

    try
    {
      // scenario name available on first index in summary.top file
      String dataVal = getValueFromSummaryTopByIdx(testRun, 1);
      if(dataVal.equals(""))
        dataVal = "Unavailable";

      return dataVal;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getScenarioName", "", "", "Exception", e);
      return "";
    }
  }

  // get the test name from summary.top file
  public static String getTestName(int testRun)
  {
    Log.debugLog(className, "getTestName", "", "", "Method called. Test Run = " + testRun);

    try
    {
      // testName available on 12th index in summary.top file
      String dataVal = getValueFromSummaryTopByIdx(testRun, 12);
      if(dataVal.equals(""))
        dataVal = "NA";

      return dataVal;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getTestName", "", "", "Exception", e);
      return "";
    }
  }
  // get the total duration of test run from summary.top file
  public static String getTestDuration(int testRun)
  {
    Log.debugLog(className, "getTestDuration", "", "", "Method called. Test Run = " + testRun);

    try
    {
      // testDuration available on 14th index in summary.top file
      String dataVal = getValueFromSummaryTopByIdx(testRun, 14);
      if(dataVal.equals(""))
        dataVal = "NA";

      return dataVal;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getTestDuration", "", "", "Exception", e);
      return "";
    }
  }


  // get baseline test run from baseline_tracking.dat file in testRun dir
   public static ArrayList<String> readBaseLineTrackingFile(int testRun)
   {
     Log.debugLog(className, "getBaselineTR", "", "", "Method Starts.");

     try
     {
       String filePath = getTestRunDirPath(testRun) + "/" +"baseline_tracking.dat" ;
       String strLine = "";
       File baseLineTRFile = new File(filePath);

       if(!baseLineTRFile.exists())
       {
         Log.debugLog(className, "getBaselineTR", "", "", filePath + " not found in the Test Run - " + testRun);
         return null;
       }

       BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(baseLineTRFile)));

       ArrayList<String> baseLineData = new ArrayList<String>();
       
       while((strLine = br.readLine()) !=  null)
         baseLineData.add(strLine.trim());

       br.close();

       if(baseLineData.size() == 0)
       {
         Log.errorLog(className, "getBaselineTR", "", "", filePath + " is empty in the Test Run - " + testRun);
         return null;
    }

    return baseLineData;
   }
   catch(Exception e)
   {
       Log.stackTraceLog(className, "getBaselineTR", "", "", "Exception : ", e);
       return null;
   }
 }


// get baseline Scenario name from baseline_tracking.dat file in testRun dir
public static String getBaseLineScenarioName(int testRun)
{
  Log.debugLog(className, "getBaselineTR", "", "", "Method Starts.");

  try
  {
    String filePath = getTestRunDirPath(testRun) + "/" +"baseline_tracking.dat" ;
    String strLine = "";
    String baseLineScenarioName = "";
    File baseLineTRFile = new File(filePath);

    if(!baseLineTRFile.exists())
    {
      Log.debugLog(className, "getBaselineTR", "", "", filePath + " not found in the Test Run - " + testRun);
      return "";
    }

    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(baseLineTRFile)));

    Vector vecFileData = new Vector();
    while((strLine = br.readLine()) !=  null)
    {
      vecFileData.add(strLine.trim());
    }

    br.close();

    if(vecFileData.size() == 0)
    {
      Log.errorLog(className, "getBaselineTR", "", "", filePath + " is empty in the Test Run - " + testRun);
      return "";
    }

    try
    {
      String dataLine = "";
      for(int i = 0; i < vecFileData.size(); i++)
      {
        dataLine = vecFileData.get(i).toString();
        String arrLine[] = rptUtilsBean.strToArrayData(dataLine, "|");
        if(arrLine.length > 1)
        {
          if(arrLine[1].startsWith("scenario"))
          {
            String[] arrTemp = rptUtilsBean.strToArrayData(arrLine[1], "=");
            baseLineScenarioName = arrTemp[arrTemp.length - 1];
          }
        }
        else
          baseLineScenarioName = "";
      }
    }
    catch(Exception e)
    {
      Log.errorLog(className, "getBaselineTR", "", "", filePath + " has wrong data to process, in the Test Run - " + testRun);
      return "";
    }

    return baseLineScenarioName;
  }
  catch(Exception e)
  {
    Log.stackTraceLog(className, "getBaselineTR", "", "", "Exception : ", e);
    return "";
  }
}
}
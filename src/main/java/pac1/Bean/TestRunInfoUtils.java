/*--------------------------------------------------------------------
@Name    : TestRunInfoUtils.java
@Author  : Jyoti
@Purpose : Provided the utility function(s) to Analysis, reporting (compare report) and execution GUI
         : It will take minimum time to access from network.
         : Decrease size of object.
@Modification History:
    11/08/2010 -> Jyoti (Initial Version)

----------------------------------------------------------------------*/

package pac1.Bean;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;

import pac1.Bean.GraphName.*;

public class  TestRunInfoUtils
{
  private String className = "TestRunInfoUtils";
  private final static String RTG_MESSAGE_DAT = "rtgMessage.dat";
  //private ArrayList listOfTestRunInfo = new ArrayList();
  private LinkedHashMap hashTestRunInfo = new LinkedHashMap();

  //Only first will get the value of time zone of server
  //to minimize size of object and less time to access.
  private TimeZone trTimeZone;

  /**
   * Constructor
   */
  public TestRunInfoUtils()
  {
    init();
  }

  /**
   * @Purpose: Get all test runs by shell
   * Skiping those test runs which are Archive, duration less than 5 and Sample size is zero.
   *
   */
  public void init()
  {
    Log.debugLog(className, "init", "", "", "Start method");

    CmdExec cmdExec = new CmdExec();
    String cmd = "nsu_show_test_logs";
    String args = "-A -r -l ";
    Vector vecCmdOutput = cmdExec.getResultByCommand(cmd, args, 0, null, "root");

    SimpleDateFormat strDateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
    DateFormat format = strDateFormat;

    setTRTimeZone(format);
    trTimeZone = getTRTimeZone();

    String[][] arrDataValues = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "-");

    if(arrDataValues == null)
    {
      Log.debugLog(className, "init", "", "", "No Test Run is available");
    }

    TestRunInfo testRunInfo_obj = null;
    for(int i = 1 ; i < arrDataValues.length; i++)
    {
      if((arrDataValues[i][16].equals("AR")) || (arrDataValues[i][16].equals("AW")))
      {
        Log.debugLog(className, "init", "", "", "Test Run = " + arrDataValues[i][2] + " is Archive");
        continue;
      }
      else if((rptUtilsBean.convStrToMilliSec(arrDataValues[i][6]) < 5) && (getSampleSize(arrDataValues[i][2]) <= 0))
      {
        Log.debugLog(className, "init", "", "", "Duration or Sample Size of Test Run " + arrDataValues[i][2] + " is less than 5 or equal to zero.");
        continue;
      }
      else
      {
        testRunInfo_obj = new TestRunInfo();
        
        //Project Name
        testRunInfo_obj.setTrProjName(arrDataValues[i][0]);
        
        //Sub Project Name
        testRunInfo_obj.setTrSubProjName(arrDataValues[i][1]);
        
        //TestRun number : set String
        testRunInfo_obj.setTestRun(arrDataValues[i][2]);

        //Start Time :
        //testRunInfo_obj.setTRStartTime(Scenario.getTestRunStartTime(Integer.parseInt(arrDataValues[i][2])));
        testRunInfo_obj.setTRStartTime(arrDataValues[i][5]);

        //Start Timestamp
        testRunInfo_obj.setTRStartTimeStamp(format);

        //Start Date
        testRunInfo_obj.setTRStartDate(format);

        //Test Name
        testRunInfo_obj.setTRTestName(arrDataValues[i][3]);

        //Scenario Name
        testRunInfo_obj.setTRScenarioName(arrDataValues[i][4]);

        if(arrDataValues[i][6].equals("00:00:00"))
        {
          GraphNames graphNames = new GraphNames(Integer.parseInt(arrDataValues[i][2]));
          long durationInMili = getSampleSize(arrDataValues[i][2]) * graphNames.getInterval();
          arrDataValues[i][6] = rptUtilsBean.convMilliSecToStr(durationInMili);
        }

        //Duration
        testRunInfo_obj.setTRDuration(arrDataValues[i][6]);

        testRunInfo_obj.setTRTimeZone(trTimeZone);

        double[] phaseTimes = rptUtilsBean.getPhaseTimes(arrDataValues[i][2]);
        testRunInfo_obj.setPhaseTimes(phaseTimes);
        
        long intervalRunPhaseInfo = -1;
        try
        {
          TestRunPhaseInfo testRunPhaseInfo = new TestRunPhaseInfo(Integer.parseInt(testRunInfo_obj.getTestRun()), testRunInfo_obj.getTRScenarioName(), intervalRunPhaseInfo);
       
        ArrayList phaseNameTimingInfo = (ArrayList) testRunPhaseInfo.getPhaseInfo();
        testRunInfo_obj.setPhaseNameTimingInfo(phaseNameTimingInfo);
        
        //listOfTestRunInfo.add(testRunInfo_obj);
        hashTestRunInfo.put(arrDataValues[i][2], testRunInfo_obj);
       }
       catch(Exception e)
       {
        //Ignoring Continuous testrun.
       }
      }
    }
    //System.out.println("listOfTestRunInfo  length =  " + listOfTestRunInfo.size());
  }

  /**
   * @purpose To check the size of sample of particular test run.
   * @param testRun
   * @return
   */
  public long getSampleSize(String testRun)
  {
    Log.debugLog(className, "getSampleSize", "", "", "Start method. Test Run no. = " + testRun);

    try
    {
      long fileSize = 0;
      File objFile = new File(Config.getWorkPath() + "/webapps/logs/TR" + testRun + "/" + RTG_MESSAGE_DAT);
      if (objFile.exists())
      {
        fileSize = objFile.length();
      }

      long numberOfSamples = fileSize/GraphNameUtils.getMsgDataSizeByTestNum(Integer.parseInt(testRun));
      return numberOfSamples;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getSampleSize", "", "", "Exception - ", ex);
      return Long.parseLong("0");
    }
  }

  //Getter setter for time zone
  public TimeZone getTRTimeZone()
  {
    return trTimeZone;
  }

  public void setTRTimeZone(DateFormat format)
  {
    this.trTimeZone = format.getTimeZone();;
  }

  public LinkedHashMap getTestRunInfo()
  {
    return this.hashTestRunInfo;
  }

  public static void main(String arg[])
  {
    LinkedHashMap listTR = new LinkedHashMap();
    TestRunInfoUtils testRunInfoUtils_obj = new TestRunInfoUtils();
    listTR = testRunInfoUtils_obj.getTestRunInfo();

    TestRunInfo testRunInfo = null;
    /**for(int i = 0; i < listTR.size(); i++)
    {
      testRunInfo = (TestRunInfo)listTR.get(i);
      System.out.print(" Test Run = " + testRunInfo.getTestRun());
      System.out.print(", Start Time = " + testRunInfo.getTRStartTime());
      System.out.print(", Time Stamp = " + testRunInfo.getTRStartTimeStamp());
      System.out.print(", Start Date = " + testRunInfo.getTRStartDate());
      System.out.print(", Duration " + testRunInfo.getTRDuration());
      System.out.print(", Test Name " + testRunInfo.getTRTestName());
      System.out.print(", Scenario Name " + testRunInfo.getTRScenarioName());
      System.out.println(" ");
    }**/


    Set st = listTR.keySet();
    System.out.println("Set created from LinkedHashMap Keys contains :");
    //iterate through the Set of keys
    Iterator itr = st.iterator();
    while(itr.hasNext())
    {
      testRunInfo = (TestRunInfo)listTR.get(itr.next());
      System.out.print(" Test Run = " + testRunInfo.getTestRun());
      System.out.print(", Start Time = " + testRunInfo.getTRStartTime());
      System.out.print(", Time Stamp = " + testRunInfo.getTRStartTimeStamp());
      System.out.print(", Start Date = " + testRunInfo.getTRStartDate());
      System.out.print(", Duration " + testRunInfo.getTRDuration());
      System.out.print(", Test Name " + testRunInfo.getTRTestName());
      System.out.print(", Scenario Name " + testRunInfo.getTRScenarioName());
      System.out.println(" ");
    }
  }
}


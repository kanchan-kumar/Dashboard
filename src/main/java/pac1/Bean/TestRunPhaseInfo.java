/*--------------------------------------------------------------------
@Name    : TestRunPhaseInfo.java
@Author  : Jaspreet Kaur
@Purpose : Provides Phase Name, Phase Start Time, Phase End Time Information for each Test Run.
         : It parses global.dat file to get phase information
         : Returns Arraylist of Phases Object (each object in arraylist contains info about each phase for particular test run)
@Modification History:
    11/11/2010 -> Jaspreet Kaur (Initial Version)

----------------------------------------------------------------------*/

package pac1.Bean;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class TestRunPhaseInfo implements java.io.Serializable
{
  private static String className = "TestRunPhaseInfo";
  private static int testRunNumber;
  private static String scenarioName;
  private static long interval ;
  private Phases phasesObject;

  public TestRunPhaseInfo(int testRunNumber, String scenarioName, long interval) //constructor to get testRunNumber
  {
    this.testRunNumber = testRunNumber;
    this.scenarioName = scenarioName;
    this.interval = interval;
  }

  public ArrayList getPhaseInfo()
  {
    try
    {
      Log.debugLog(className, "getPhaseInfo", "", "", "Method called, testRunNumber = " + testRunNumber + ", scenarioName = " +scenarioName + ", interval = " + interval);
      boolean sortByPhaseName = getScenarioType();
      String globalDataFilePath = Config.getWorkPath() + "/webapps/logs/TR" + testRunNumber + "/" + "global.dat";
      ArrayList<Phases> listOfPhases = new ArrayList<Phases>()
      {
        public boolean contains(Object obj)
        {
          String phaseName ;
          if (!(obj instanceof Phases))
          {
            return false;
          }
          phaseName = ((Phases) obj).getphaseName();

          for (Phases lst : this)
          {
            String curPhaseName = lst.getphaseName();
            if(curPhaseName.equals(phaseName))
            {
              return true;
            }
          }
          return false;
        }

        public int indexOf(Object obj)
        {
          int index = 0;
          String phaseName ;
          if (!(obj instanceof Phases))
          {
            return -1;
          }
          phaseName = ((Phases) obj).getphaseName();
          for (Phases lst : this)
          {
            String curPhaseName = lst.getphaseName();
            if(curPhaseName.equals(phaseName))
            {
              break;
            }
            index ++;
          }
          return (index);
        }

      };
      BufferedReader br  = new BufferedReader(new FileReader(globalDataFilePath));
      String str = null;
      int gotPhaseTimesFlag = 0;
      boolean gotPhaseStartEndTimeKeyword = false;
      String[] strTempPhase_Times = null;
      while(((str = br.readLine()) != null))
      {
        if(str.startsWith("PHASE_START_TIME") ||str.startsWith("PHASE_END_TIME"))
        {
          Log.debugLog(className, "getPhaseInfo", "", "", "Method called,  sortByPhaseName = "+ sortByPhaseName +", Line Parsed = " + str);
          String[] strTemp = rptUtilsBean.strToArrayData(str, " ");
          if(strTemp.length < 4)
          {
            Log.errorLog(className, "getPhaseInfo", "", "", "Error in getting phase Information ,global.dat might be corrupt for TR " + testRunNumber + " or PHASE_START_TIME/PHASE_END_TIME keywords are in corrupted format");
            return null;
          }
          String scenarioPhaseName;
          if(!sortByPhaseName)
          {
            scenarioPhaseName = strTemp[1];//for simple scenario (excluding group based)phaseName is taken from PhaseType
            String[] strToFormat = rptUtilsBean.strToArrayData(scenarioPhaseName, "_");
            String phName = "";
            for(int s = 0; s < strToFormat.length; s++)
            {
              Character c = strToFormat[s].charAt(0);
              if(s==0)
              {
                phName = Character.toUpperCase(c) + ((strToFormat[s]).substring(1)).toLowerCase();
              }
              else
              {
                phName = phName + " " + Character.toUpperCase(c) + ((strToFormat[s]).substring(1)).toLowerCase();
              }
            }
            scenarioPhaseName = phName; //this phase name is in required format (taken from phase_type)
          }
          else
            scenarioPhaseName = strTemp[2];//for 1.advance scenario ,2. advance group based scenario ,3.simple group based phaseName is taken from PhaseName itself
          //Log.debugLog(className, "getPhaseInfo", "", "", "scenarioPhaseName= " + scenarioPhaseName + "strTemp[3] =" +strTemp[3]);
          Phases tempPhase = new Phases(scenarioPhaseName,strTemp[3], "" );
          if(listOfPhases.contains(tempPhase))//if phase name exists already
          {
            tempPhase = listOfPhases.get(listOfPhases.indexOf(tempPhase));
            if(str.startsWith("PHASE_START_TIME") && (tempPhase.getstartTime()).equals("NA"))//means we already have endTime for this phase
            {
              if(isValidPhase(strTemp[3], tempPhase.getendTime()))
              {
                tempPhase.setstartTime(strTemp[3]);
              }
              else
              {
                listOfPhases.remove(tempPhase);
              }

            }
            else if(str.startsWith("PHASE_END_TIME") && (tempPhase.getendTime()).equals("NA"))//means we already have startTime for this phase
            {
              if(isValidPhase(tempPhase.getstartTime(), strTemp[3]))
              {
                tempPhase.setendTime(strTemp[3]);
              }
              else
              {
                listOfPhases.remove(tempPhase);
              }

            }

            //Log.debugLog(className, "getPhaseInfo", "", "", "Method called, strTemp[3] = " + strTemp[3] + " scenarioPhaseName = " +scenarioPhaseName);
          }
          else //adds phase names first time
          {
            if(str.startsWith("PHASE_START_TIME"))
              listOfPhases.add(new Phases(scenarioPhaseName,strTemp[3], "NA" ));
            else if(str.startsWith("PHASE_END_TIME"))
              listOfPhases.add(new Phases(scenarioPhaseName,"NA", strTemp[3]));
            //Log.debugLog(className, "getPhaseInfo", "", "", "First Time strTemp[3] = " + strTemp[3] + "scenarioPhaseName =" + scenarioPhaseName);
          }
          gotPhaseStartEndTimeKeyword = true;
        }
        else if(str.startsWith("PHASE_TIMES") && gotPhaseTimesFlag == 0)
        {
          strTempPhase_Times  = rptUtilsBean.strToArrayData(str, " ") ;
          if(strTempPhase_Times.length < 5)
          {
            Log.errorLog(className, "getPhaseInfo", "", "", "Error in getting phase Information ,global.dat might be corrupt for TR " + testRunNumber + " or PHASE_TIMES keyword is in corrupted format");
            return null;
          }
          gotPhaseTimesFlag = 1;
        }
      }
      
      //if global.dat has only phaseTimes keyword
      if((!gotPhaseStartEndTimeKeyword) && strTempPhase_Times!=null)
      {
        Log.debugLog(className, "getPhaseInfo", "", "", "Getting phaseInfo from Phase_Times keyword");
        for(int t = 1; t < strTempPhase_Times.length; t++)//t=1 coz at first index phase_times keyword is there
        {
          String[] strStartEndTime = null;
          switch(t)
          {
            case 1:
              strStartEndTime = timeHH_MM_SS("0",strTempPhase_Times[t]);
              if(strStartEndTime[0].equals("valid_Duration"))
              {
                listOfPhases.add(new Phases("Ramp Up",strStartEndTime[1], strStartEndTime[2]));
              }
              break;

            case 2:
              strStartEndTime = timeHH_MM_SS(strTempPhase_Times[t-1],strTempPhase_Times[t]);
              if(strStartEndTime[0].equals("valid_Duration"))
              {
                listOfPhases.add(new Phases("Stabilization",strStartEndTime[1], strStartEndTime[2]));
              }
              break;

            case 3:
              strStartEndTime = timeHH_MM_SS(strTempPhase_Times[t-1],strTempPhase_Times[t]);
              if(strStartEndTime[0].equals("valid_Duration"))
              {
                listOfPhases.add(new Phases("Duration",strStartEndTime[1], strStartEndTime[2]));
              }
              break;

            case 4:
              strStartEndTime = timeHH_MM_SS(strTempPhase_Times[t-1],strTempPhase_Times[t]);
              if(strStartEndTime[0].equals("valid_Duration"))
              {
                listOfPhases.add(new Phases("Ramp Down",strStartEndTime[1], strStartEndTime[2]));
              }
              break;

              default:
                Log.errorLog(className, "getPhaseInfo", "", "", "No match");
          }
        }
     }
      return listOfPhases;
    }
    catch(Exception ex)
    {
      Log.errorLog(className, "getPhaseInfo", "", "", "Exception : " + ex);
      Log.errorLog(className, "getPhaseInfo", "", "", "Error in getting phase Information ,global.dat might be corrupt for this TR " + testRunNumber + " or Phase_Times, PHASE_START_TIME/PHASE_END_TIME keywords not exist");
      return null;
    }
  }

  private boolean isValidPhase(String startTime, String endTime)
  {
    try
    {
      long phaseDuration =  rptUtilsBean.convStrToMilliSec(endTime) - rptUtilsBean.convStrToMilliSec(startTime);
      Log.debugLog(className, "isValidPhase", "", "", "Method starts phaseDuration = "+ phaseDuration + "interval = "+interval );
      if(phaseDuration < interval)
        return false;
      else
        return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "isValidPhase", "", "", "Exception : ", ex);
      Log.errorLog(className, "isValidPhase", "", "", "Error in validating phaseInfo");
      return false;
    }

  }

  private String[] timeHH_MM_SS(String startimeInSec , String EndTimeInSec)//converts given time in sec(string) into HH:MM:SS string format
  {
    Log.debugLog(className, "timeHH_MM_SS", "", "", "Method called, startimeInSec = " + startimeInSec + ", EndTimeInSec = " + EndTimeInSec );
    String[] arrStartEndTime = null;//it will have starTime(1 index), EndTime(2 index), (0 index)phaseDuration(false-> for duration less than interval , true->for duration more than interval)

    double phaseDuration = ((Double.parseDouble(EndTimeInSec.trim())) -(Double.parseDouble(startimeInSec.trim())))*1000;
    if(phaseDuration < interval)
    {
      arrStartEndTime = new String[1];
      arrStartEndTime[0] = "Invalid_Duration";//not valid phase duration
      Log.debugLog(className, "timeHH_MM_SS", "", "", "Invalid phaseDuration = "+ phaseDuration + ", interval = "+interval +", Duration is = " + arrStartEndTime[0] );
    }
    else
    {
      arrStartEndTime = new String[3];
      arrStartEndTime[0] = "valid_Duration";//valid phase duration
      String[] strStartTime = rptUtilsBean.strToArrayData((rptUtilsBean.timeInMilliSecToString(String.valueOf((Double.parseDouble(startimeInSec.trim()))*1000), 0)), ".");
      String[] strEndTime = rptUtilsBean.strToArrayData((rptUtilsBean.timeInMilliSecToString(String.valueOf((Double.parseDouble(EndTimeInSec.trim()))*1000), 0)), ".");

      arrStartEndTime[1] = strStartTime[0];//hh:mm:ss is truncated 'coz ss can contain float values
      arrStartEndTime[2] = strEndTime[0];
      Log.debugLog(className, "timeHH_MM_SS", "", "", "Valid phaseDuration = "+ phaseDuration + ", interval = "+interval +", Duration is = " + arrStartEndTime[0] + ", startTime = " + arrStartEndTime[1] +", endTime = " + arrStartEndTime[2]);
    }

    return arrStartEndTime;
  }

  public boolean getScenarioType()
  {
    try
    {
      Log.debugLog(className, "getScenarioType", "", "", "Method called, TestRunNum = " + testRunNumber + "scenarioName = " + scenarioName);
      boolean sortByPhaseName = true; //false mean it's not a simple scenario
      String[] strName = rptUtilsBean.strToArrayData(scenarioName, "/");
      String scenarioFilePath = "";
      if(strName.length == 1)//if its means scenario name only (instead of project/subProject/scenarioName format)
        scenarioFilePath = Config.getWorkPath() + "/webapps/logs/TR" + testRunNumber + "/" + strName[0] +".conf";
      else
        scenarioFilePath = Config.getWorkPath() + "/webapps/logs/TR" + testRunNumber + "/" + strName[2] +".conf";

      BufferedReader brScenario  = new BufferedReader(new FileReader(scenarioFilePath));
      String strTemp = null;
      String scheduleType = "SIMPLE"; //tells if schedule is : Advanced or Simple, "NA" is for no entry in scenario.conf
      String scheduleBy = "SCENARIO";//tells if scenario is by :group or simple
      while(((strTemp = brScenario.readLine()) != null))
      {
        if(strTemp.startsWith("SCHEDULE_TYPE"))
        {
          String[] strArr = rptUtilsBean.strToArrayData(strTemp, " ");
          if(strArr[1].equalsIgnoreCase("ADVANCED"))
            scheduleType = "ADVANCED";
          else
            scheduleType = "SIMPLE";
        }// in case of default when no entry for Schedule_Type keyword in scenario.conf it will take it as simple

        if(strTemp.startsWith("SCHEDULE_BY"))
        {
          String[] strArr = rptUtilsBean.strToArrayData(strTemp, " ");
          if(strArr[1].equalsIgnoreCase("GROUP"))
            scheduleBy = "GROUP";
          else
            scheduleBy = "SCENARIO";
        }//in case of default when no entry for Schedule_BY in scenario file it will take it as scenario
      }
      if(scheduleType.equalsIgnoreCase("SIMPLE") && scheduleBy.equalsIgnoreCase("SCENARIO"))
      {
        sortByPhaseName = false; // means simple scenario , and take phaseType as phaseName to show
      }
      return sortByPhaseName;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getScenarioType", "", "", "Exception : ", ex);
      Log.errorLog(className, "getScenarioType", "", "", "Error in parsing scenario");
      return true;
    }
  }
  
  public class Phases implements java.io.Serializable
  {
    String phaseName = "NA";
    String startTime = "NA";
    String endTime = "NA";
    private volatile int hashCode = 0;

    public Phases(String phaseName, String startTime, String endTime)
    {
      this.phaseName = phaseName;
      this.startTime = startTime;
      this.endTime = endTime;
    }
    void setstartTime(String str)
    {
      this.startTime =  str;
    }
    void setendTime(String str1)
    {
      this.endTime = str1;
    }
    void setphaseName(String str2)
    {
      this.phaseName= str2;
    }
    public String getphaseName()
    {
      return phaseName;
    }
    public String getstartTime()
    {
      return startTime;
    }
    public String getendTime()
    {
      return endTime;
    }

  }
 /* public static void main()
  {
    TestRunPhaseInfo testRunPhaseInfo = new TestRunPhaseInfo(4247, scenarioName, -1);
    testRunPhaseInfo.getPhaseInfo();
  }*/
}

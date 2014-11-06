package pac1.Bean;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class TestRunInfo implements java.io.Serializable
{
  private String className = "TestRunInfo";
  private String TestRun;
  private String trStartTime;
  private long trStartTimeStamp;
  private Date trStartDate;
  private String trProjName;
  private String trSubProjName;
  private String trScenarioName;
  private String trTestName;
  private String trDuration;
  private TimeZone trTimeZone;
  private double[] phaseTimes;
  private ArrayList phaseNameTimingInfo;

/**
 * Constructor
 *
 */
  public TestRunInfo() {  }



  /************************Getter Methods***************************/

  public String getTestRun()
  {
    return TestRun;
  }

  public String getTRStartTime()
  {
    return trStartTime;
  }

  public long getTRStartTimeStamp()
  {
    return trStartTimeStamp;
  }

  public Date getTRStartDate()
  {
    return trStartDate;
  }

  public String getTRScenarioName()
  {
    return trScenarioName;
  }

  public String getTRTestName()
  {
    return trTestName;
  }

  public String getTRDuration()
  {
    return trDuration;
  }

  public TimeZone getTRTimeZone()
  {
    return trTimeZone;
  }

  public double[] getPhaseTimes()
  {
    return phaseTimes;
  }
  public ArrayList getPhaseNameTimingInfo()
  {
    return phaseNameTimingInfo;
  }
  public String getTrSubProjName()
  {
    return trSubProjName;
  }
  public String getTrProjName()
  {
    return trProjName;
  }
  /************************Setter Methods***************************/

  public void setTestRun(String TestRun)
  {
    this.TestRun = TestRun;
  }

  public void setTRStartTime(String trStartTime)
  {
     this.trStartTime = trStartTime;
  }

  public void setTRStartTimeStamp(DateFormat format)
  {
    try
    {
      this.trStartTimeStamp = format.parse(getTRStartTime()).getTime();
    }
    catch(ParseException ex)
    {
      Log.stackTraceLog(className, "setTRStartTimeStamp", "", "", "ParseException - ", ex);
    }
  }

  public void setTRStartDate(DateFormat format)
  {
    try
    {
      this.trStartDate = format.parse(getTRStartTime());
    }
    catch(ParseException ex)
    {
      Log.stackTraceLog(className, "setTRStartDate", "", "", "ParseException - ", ex);
    }
  }

  public void setTRScenarioName(String trScenarioName)
  {
     this.trScenarioName = trScenarioName;
  }

  public void setTRDuration(String trDuration)
  {
    this.trDuration = trDuration;
  }

  public void setTRTestName(String trTestName)
  {
    this.trTestName = trTestName;
  }

  public void setTRTimeZone(TimeZone trTimeZone)
  {
    this.trTimeZone = trTimeZone;
  }

  public void setPhaseTimes(double[] phaseTimes)
  {
    this.phaseTimes = phaseTimes;
  }
  public void setPhaseNameTimingInfo(ArrayList phaseNameTimingInfo)
  {
    this.phaseNameTimingInfo = phaseNameTimingInfo;
  }
  public void setTrProjName(String trProjName)
  {
    this.trProjName = trProjName;
  }
  public void setTrSubProjName(String trSubProjName)
  {
    this.trSubProjName = trSubProjName;
  }
}
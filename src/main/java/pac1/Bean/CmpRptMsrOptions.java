/*--------------------------------------------------------------------
  @Name    : CmpRptMsrOptions.java
  @Author  : Prabhat
  @Purpose : To Set The Compare Report Measurement Options

  This class is used by CmpRptUsingRtpl class. It is used to keep information
  about one messurement for compare. Array of object of this class is maintained in the
  CmpRptUsingRtpl.

  @Modification History:
    07/28/07 : Prabhat --> Initial Version

----------------------------------------------------------------------*/

package pac1.Bean;


// Class for Compare Report Measurement Options
public class CmpRptMsrOptions
{
  private String className = "CmpRptMsrOptions";

  // Following 4 fields are for logging only
  String rptDesc = "";  // Report Description
  String graphViewType = "Simple Graph";  // Graph View Type - Simple, Tile, Multi, Correlated
  String graphType = "Normal"; // Graph Type - Normal or Long
  String granularity = "0";
  // String updatedOptions = ""; // Store updated options input from user

  String msrName = "";
  String testRun = "";
  int testRunIndex = -1; // Index in CmpRptTestRunData array for this measurement.
  String xAxisTimeFormat = "NA";
  String timeOption = "NA";  // Total, Run Phase or Specified
  String timeSelectionFormat = "NA";
  String startDate = "NA";
  String startTime = "NA";
  String endDate = "NA";
  String endTime = "NA";
  // boolean overrideRptOptions = false;
  boolean overrideRptOptions = true;

  String testRunStartDateTime = "";  // Test Run start Date/Time
  long testRunTotalTime = 0;          // Total test run time in milli-secs
  String testDuration = ""; // Test Run duration

  String elapsedStartTime = "NA";  // Elasped Start Time. Calculated if absolute date/time used
  String elapsedEndTime = "NA"; // Elasped End Time. Calculated if absolute date/time used

  private CmpRptUsingRtpl cmpRptUsingRtpl = null;

  // The purpose of using this constructor to get object for showing log detail in report screen
  public CmpRptMsrOptions(CmpRptUsingRtpl cmpRptUsingRtpl)
  {
    this.cmpRptUsingRtpl = cmpRptUsingRtpl;
  }

  public int getTestRun(String msrData)
  {
    String arrTmp[] = rptUtilsBean.strToArrayData(msrData, "|");
    return(Integer.parseInt(arrTmp[1]));
  }

  // This is to set Compare Report Measurement Options from msrData
  public boolean setCmpRptMsrOptions(String msrData, int testRunIndex, long testRunTotalTime , int controllerTR)
  {
    Log.reportDebugLog(className, "setCmpRptMsrOptions", "", "", "Method called. msrData = " + msrData + ", testRunIndex = " + testRunIndex + ", testRunTotalTime = " + testRunTotalTime);

    this.testRunIndex = testRunIndex;
    this.testRunTotalTime = testRunTotalTime;

    String arrTmp[] = rptUtilsBean.strToArrayData(msrData, "|");
    this.msrName = arrTmp[0];
    this.testRun = arrTmp[1];
    this.testRunStartDateTime = arrTmp[2];
    // if(arrTmp[3].equals("0"))  // It not override, we will options in the template
    //   return true;
    this.overrideRptOptions = true;

    // This is used for updating records for template
    // ?? this.updatedOptions = options;// store updated options
    setOptions(4, arrTmp);
    if(absoluteToElapsed() == false) return false;

    // get the test duration from summary.top file
    if(controllerTR == -1)
      testDuration = Scenario.getTestDuration(Integer.parseInt(testRun));
    else
      testDuration = Scenario.getTestDuration(controllerTR);

    Log.reportDebugLog(className, "setCmpRptMsrOptions", "", "", optionsString());
    return true;
  }

  // This is to convert Compare Report Measurement Options from a template record
  // Neeraj = Check how to do it.
  // Issus - Since each report in a template can have different options, how do we handle it?

  public boolean setCmpRptMsrOptionsUsignRtpl(String arrRptFields[])
  {
    // Following four fields are for logging only. Not used for any logic.
    rptDesc = arrRptFields[0];
    graphViewType = arrRptFields[1];
    graphType = arrRptFields[2];
    granularity = arrRptFields[3];

    if(overrideRptOptions == true)  // Since override, ignore report options of report in template
      return true;

    setOptions(4, arrRptFields);
    return absoluteToElapsed();
  }

  private void setOptions(int i, String arrTmp[])
  {
    xAxisTimeFormat = arrTmp[i++];
    timeOption = arrTmp[i++];
    timeSelectionFormat = arrTmp[i++];
    startDate = arrTmp[i++];
    startTime = arrTmp[i++];
    endDate = arrTmp[i++];
    endTime = arrTmp[i++];
  }

  public String optionsString()
  {
    return("Override Rpt Options = " + overrideRptOptions + ", XAxis Time Format = " +  xAxisTimeFormat +
  ", Time Option = " + timeOption + ", Time Selection Format = " + timeSelectionFormat + ", Start Date = " + startDate + ", Start Time = " + startTime + ", End Date = " +  endDate + ", End Time = " + endTime);
  }

  public String rptString()
  {
    return("Report Description = " +  rptDesc + ", Graph View Type = " + graphViewType + ", Graph Type = " + graphType + ", Granularity = " +  granularity + ", " + optionsString());
  }

  // Method to convert Absolute Start/End Date/Time to Elapsed Start/End Time
  private boolean absoluteToElapsed()
  {
    Log.reportDebugLog(className, "absoluteToElapsed", "", "", "Method called.");
    long inputstartTime = 0;
    long inputendTime = 0;

    try
    {
      elapsedStartTime = "NA";
      elapsedEndTime = "NA";
      long testRunStartTime = rptUtilsBean.convertDateToMilliSec(testRunStartDateTime);

      if(timeOption.equals("Specified Time"))
      {
        if(timeSelectionFormat.equals("Elapsed"))
        {
          Log.reportDebugLog(className, "absoluteToElapsed", "", "", "Checking if elapsed time specified is within the test run time range");

          inputstartTime = rptUtilsBean.convStrToMilliSec(startTime);
          inputendTime = rptUtilsBean.convStrToMilliSec(endTime);

          if(testRunTotalTime < inputstartTime)
          {
            Log.reportDebugLog(className, "absoluteToElapsed", "", "", "Error: Elapsed Start Time (" + startTime + ") is less than test run total run time (" + rptUtilsBean.convMilliSecToStr(testRunTotalTime) + ").");

            cmpRptUsingRtpl.reportStatusLog(cmpRptUsingRtpl.reportSetName, cmpRptUsingRtpl.numTestRun, "absoluteToElapsed", "", "", "Error: Elapsed Start Time (" + startTime + ") is less than test run total run time (" + rptUtilsBean.convMilliSecToStr(testRunTotalTime) + ").");

            return false;
          }

          elapsedStartTime = startTime;
          elapsedEndTime = endTime;
        }
        else
        {
          Log.reportDebugLog(className, "absoluteToElapsed", "", "", "Checking if absolute time specified is withing the test run time range");
          inputstartTime = rptUtilsBean.convertDateToMilliSec(startDate + " " + startTime);
          inputendTime = rptUtilsBean.convertDateToMilliSec(endDate +" " + endTime);

          if(inputstartTime < testRunStartTime)
          {
            Log.reportDebugLog(className, "absoluteToElapsed", "", "", "Warning: Absolute Start Date/Time (" + startDate + " " + startTime + ") is < test run start date/time (" + testRunStartDateTime + "). Using test run start date/time as start date/time for report generation.");

            cmpRptUsingRtpl.reportStatusLog(cmpRptUsingRtpl.reportSetName, cmpRptUsingRtpl.numTestRun, "absoluteToElapsed", "", "", "Warning: Absolute Start Date/Time (" + startDate + " " + startTime + ") is < test run start date/time (" + testRunStartDateTime + "). Using test run start date/time as start date/time for report generation.");
            inputstartTime = 0;
          }
          else
            inputstartTime = inputstartTime - testRunStartTime;
          // This will fail when no total run time given.
          if(inputstartTime >= testRunTotalTime)
          {
            Log.reportDebugLog(className, "absoluteToElapsed", "", "", "Error: Absolute Start Date/Time (" + startDate + " " + startTime + ") is >= test run end date/time.");

            cmpRptUsingRtpl.reportStatusLog(cmpRptUsingRtpl.reportSetName, cmpRptUsingRtpl.numTestRun, "absoluteToElapsed", "", "", "Error: Absolute Start Date/Time (" + startDate + " " + startTime + ") is >= test run end date/time.");
            return false;
          }
          // This will check end date/time with test run's end date/time
          if(inputendTime > testRunTotalTime)
          {
            Log.reportDebugLog(className, "absoluteToElapsed", "", "", "Error: Absolute End Date/Time (" + endDate + " " + endTime + ") is >= test run end date/time.");

            cmpRptUsingRtpl.reportStatusLog(cmpRptUsingRtpl.reportSetName, cmpRptUsingRtpl.numTestRun, "absoluteToElapsed", "", "", "Warning: Absolute End Date/Time (" + endDate + " " + endTime + ") is > test run end date/time. Using test run end date/time as end date/time for report generation.");
          }

          elapsedStartTime = rptUtilsBean.convMilliSecToStr(inputstartTime);
          elapsedEndTime = rptUtilsBean.convMilliSecToStr(inputendTime - testRunStartTime);
        }
      }
      Log.reportDebugLog(className, "absoluteToElapsed", "", "", "Elapsed Start/End Time = " + elapsedStartTime + "/" + elapsedEndTime);

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "absoluteToElapsed", "", "", "Exception - ", e);
      return false;
    }
  }

  public String getMSRName(String msrData)
  {
    String arrTmp[] = rptUtilsBean.strToArrayData(msrData, "|");
    return arrTmp[0];
  }
}

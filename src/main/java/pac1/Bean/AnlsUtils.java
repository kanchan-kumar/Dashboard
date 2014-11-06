/**----------------------------------------------------------------------------
 * Name       AnlsUtils.java
 * Purpose    This file is to verify Template , Test Run and pdf for Cross Compare
 *            report generation
 * @author    Jaspreet
 * Modification History
 *---------------------------------------------------------------------------**/

package pac1.Bean;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

import pac1.Bean.GraphName.GraphNames;

public class AnlsUtils implements Serializable
{
  private static String className = "AnlsUtils";

  //method to verify pdf
  private static Object verifyPdfFiles(ArrayList testRunsInfo, Vector templateLines)
  {
    Log.debugLog(className, "verifyPdfFiles", "", "", "Method Starts number of test runs=" + testRunsInfo.size());
    String errMsg = "";
    try
    {
      GraphNames tempGraphNames = null;

      // these list is to keep the test run info
      ArrayList arrListGraphNames = new ArrayList();
      ArrayList arrListTestRun = new ArrayList();
      ArrayList arrListTimeOption = new ArrayList();
      ArrayList arrListStartTime = new ArrayList();
      ArrayList arrListEndTime = new ArrayList();

      for(int ii = 0; ii < testRunsInfo.size(); ii++)
      {
        Object[] getObjects = (Object[])testRunsInfo.get(ii);

        int tempTestRun = Integer.parseInt(getObjects[2].toString());

        tempGraphNames = new GraphNames(tempTestRun);

        arrListGraphNames.add(tempGraphNames);

        arrListTestRun.add(tempTestRun + "");
        arrListTimeOption.add(getObjects[3].toString());
        arrListStartTime.add(getObjects[6].toString());
        arrListEndTime.add(getObjects[8].toString());
      }

      // initialize variables & object here
      String strRptInfoArr[][] = null;
      String timeOption = "";
      String startTime = "";
      String endTime = "";
      Object objError = null;
      String errPercentile = null;

      for(int tt = 3; tt < templateLines.size(); tt++)
      {
        String[] graphLine = rptUtilsBean.strToArrayData(templateLines.get(tt).toString(), "|");

        if(graphLine[1].equals("Derived Graph"))
          strRptInfoArr = ReportTemplate.rptInfoToArrForDerived(graphLine[11], false);
        else
          strRptInfoArr = ReportTemplate.rptInfoToArr(graphLine[11], false);

        for(int cc = 0; cc < testRunsInfo.size(); cc++)
        {
          int testRun = Integer.parseInt(arrListTestRun.get(cc).toString());

          timeOption = arrListTimeOption.get(cc).toString();
          startTime = arrListStartTime.get(cc).toString();
          endTime = arrListEndTime.get(cc).toString();

          tempGraphNames = (GraphNames)arrListGraphNames.get(cc);

          objError = tempGraphNames.validatePDFByGrpIdAndGraphId(strRptInfoArr[0], strRptInfoArr[1]); // objError is pdfMessage in operate Servlet
          errPercentile = objError.toString();

          if(!errPercentile.equals(""))
          {
            if(!errPercentile.startsWith("Percentile"))
            {
              Log.debugLog(className, "verifyPdf", "", "", "Error in verifying pdf for Test Run number -'" + testRun + "'");
              errMsg = errPercentile + "\nin Test Run number -'" + testRun + "'";
              return errMsg;
            }
            else if(graphLine[1].equals("Percentile Graph") || graphLine[1].equals("Slab Count Graph") || graphLine[1].equals("Frequency Distribution Graph"))
            {
              errMsg = errPercentile + "\nTest Run number - " + testRun;
              return errMsg;
            }
          }
          if(graphLine[1].equals("Percentile Graph") || graphLine[1].equals("Slab Count Graph") || graphLine[1].equals("Frequency Distribution Graph"))
          {
            Vector vecPCTL = new Vector();
            StringBuffer errMsgMsr = new StringBuffer();

            boolean validPercentile = CmpRptUtils.validataMsrForPercentileReportsByModeType(testRun + "", "", timeOption, startTime, endTime, errMsgMsr, true, strRptInfoArr[0], strRptInfoArr[1]);

            if(validPercentile)
            {
              vecPCTL.add("Success");
            }
            else
            {
              vecPCTL.add("Error");
              vecPCTL.add(errMsgMsr.toString());
            }
            Object objValidPCTL = ((Object)vecPCTL);

            if(objValidPCTL instanceof Vector)
            {
              Vector vecValidPCTL = (Vector) objValidPCTL;
              if(vecValidPCTL != null && vecValidPCTL.size() > 0)
              {
                if(vecValidPCTL.get(0).toString().equals("Error"))
                {
                  Log.debugLog(className, "verifyPdf", "", "", "Error in verifying pdf for Test Run number -'" + testRun + "'");
                  errMsg = "Error in verifying pdf for Test Run number -'" + testRun + "'";
                  return errMsg;
                }
              }
            }
          }
        }
      }
      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "verifyPdfFiles", "", "", "Exception in Verifying pdf", e);
      errMsg = "Not a valid pdf. May be the pdf is corrupt";
      return errMsg;
    }
  }

  //method to verify Test Run
  private static Object verifyTestRun(ArrayList testRunsInfo)
  {
    Log.debugLog(className, "verifyTestRun", "", "", "Method Starts test runs=" + testRunsInfo.size());
    String errMsg = "";
    try
    {
      for(int dd = 0; dd < testRunsInfo.size(); dd++)
      {
        Object[] strValues = (Object[])testRunsInfo.get(dd);
        int tempTestRun = Integer.parseInt(strValues[2].toString());

        String vrDuration = Scenario.getTestDuration(tempTestRun);
        String vrStartDateTime = Scenario.getTestRunStartTime(tempTestRun);
        String vrEndDatetime = CalEndTime(vrStartDateTime, vrDuration);
        if(strValues[3].toString().equals("Specified Time") && strValues[4].toString().equals("Absolute"))
        {
          if(!verifyStartEndDateTime(strValues[5].toString(), strValues[6].toString(), vrStartDateTime, vrEndDatetime))
          {
            Log.debugLog(className, "verifyTestRun", "", "", "Start Date Time is out side the test run Period for test run '" + tempTestRun + "'");
            errMsg = "Start Date Time is out side the test run Period for test run '" + tempTestRun + "'";
            return errMsg;
          }
          if(!verifyStartEndDateTime(strValues[7].toString(), strValues[8].toString(), vrStartDateTime, vrEndDatetime))
          {
            Log.debugLog(className, "verifyTestRun", "", "", "End Date Time is out side the test run Period for test run '" + tempTestRun + "'");
            errMsg = "End Date Time is out side the test run Period for test run '" + tempTestRun + "'";
            return errMsg;
          }
        }
        else if(strValues[3].toString().equals("Specified Time") && strValues[4].toString().equals("Elapsed"))
        {
          if(!verifyStartEndTime(strValues[6].toString(), vrDuration))
          {
            Log.debugLog(className, "verifyTestRun", "", "", "Start Time is out side the test run period for test run '" + tempTestRun + "'");
            errMsg = "Start Time is out side the test run period for test run '" + tempTestRun + "'";
            return errMsg;
          }
          if(!verifyStartEndTime(strValues[8].toString(), vrDuration))
          {
            Log.debugLog(className, "verifyTestRun", "", "", "End Time is out side the test run period for test run '" + tempTestRun + "'");
            errMsg = "End Time is out side the test run period for test run '" + tempTestRun + "'";
            return errMsg;
          }
        }
      }
      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "verifyTestRun", "", "", "Exception in Verifying Test Run", e);
      errMsg = "Due to some problem can't verify the test run details please check the error log";
      return errMsg;
    }
  }


  public static Object verifyTemplatePdfTestRunForCrossCompare(ArrayList testrun, String strTemplateName)
  {
    Log.debugLog(className, "verifyTemplatePdfTestRunForCrossCompare", "", "", "Method Starts Template name = " + strTemplateName);

    long startTimeInMillis = System.currentTimeMillis();
    Vector templateLines = new Vector();
    String errMsg = "";
    try
    {
      Log.debugLog(className, "verifyTemplatePdfTestRunForCrossCompare", "", "", "Start process to verify template");
      templateLines = ReportTemplate.readTemplateFile(ReportTemplate.getTemplateNameWithPath(strTemplateName + ".rtpl"));
      if(templateLines.size()<4)
      {
        Log.debugLog(className, "verifyTemplatePdfTestRunForCrossCompare", "", "", "No graph available in selected Template");
        errMsg = "No graph available in selected Template";
        return errMsg;
      }

      long verifyTestRunStartTimeInMillis = System.currentTimeMillis();

      Object resultVerifyTestRun = verifyTestRun(testrun);

      long verifyTestRunEndTimeInMillis = System.currentTimeMillis();

      if(resultVerifyTestRun instanceof String)
      {
        errMsg = resultVerifyTestRun.toString();
        return errMsg;
      }

      long verifyPdfFilesStartTimeInMillis = System.currentTimeMillis();

      Object resultVerifyPdfFiles = verifyPdfFiles(testrun, templateLines);

      long verifyPdfFilesEndTimeInMillis = System.currentTimeMillis();

      if(resultVerifyPdfFiles instanceof String)
      {
        errMsg = resultVerifyPdfFiles.toString();
        return errMsg;
      }

      long endTimeInMillis = System.currentTimeMillis();

      long totalTimeInMillis = endTimeInMillis - startTimeInMillis;
      long verifyTestRunTotalTimeInMillis = verifyTestRunEndTimeInMillis - verifyTestRunStartTimeInMillis;
      long verifyPdfFilesTotalTimeInMillis = verifyPdfFilesEndTimeInMillis - verifyPdfFilesStartTimeInMillis;

      //System.out.println("Time Clculation, Total time = " + rptUtilsBean.timeInMilliSecToString(totalTimeInMillis + "", 0) + ", VerifyTestRun Time = " + rptUtilsBean.timeInMilliSecToString(verifyTestRunTotalTimeInMillis + "", 0) + ", VerifyPdfFiles Time = " + rptUtilsBean.timeInMilliSecToString(verifyPdfFilesTotalTimeInMillis + "", 0));

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "verifyTemplatePdfTestRunForCrossCompare", "", "", "Exception in Verifying Template,Pdf,TestRun for CrossCompare", e);
      errMsg = "Due to some problem unable to validate. Check the Error log";
      return errMsg;
    }
  }

  public static String CalEndTime(String startDateTime, String trDuration)
  {
    try
    {
      String endDateTime = "";
      String arrStartDateTime[] = rptUtilsBean.strToArrayData(startDateTime, " ");
      String arrStDate[] = rptUtilsBean.strToArrayData(arrStartDateTime[0], "/");
      String arrStTime[] = rptUtilsBean.strToArrayData(arrStartDateTime[1], ":");
      String arrDuration[] = rptUtilsBean.strToArrayData(trDuration, ":");

      int tempSec = Integer.parseInt(arrStTime[2]) + Integer.parseInt(arrDuration[2]);
      int borrowMins = tempSec/60;
      int newSec = tempSec%60;
      int tempMins = Integer.parseInt(arrStTime[1]) + Integer.parseInt(arrDuration[1]) + borrowMins;
      int borrowHours = tempMins/60;
      int newMins = tempMins%60;
      int tempHours = Integer.parseInt(arrStTime[0]) + Integer.parseInt(arrDuration[0]) + borrowHours;
      int borrowDays = tempHours/24;
      int newHours = tempHours%24;
      int tempDays = Integer.parseInt(arrStDate[1]) + borrowDays;
      int month = Integer.parseInt(arrStDate[0]);
      int numDaysInMonth = 30;
      if(month==1 || month==3 || month==5 || month==7 || month==8 || month==10 || month==12)
      {
        numDaysInMonth = 31;
      }
      else if(month==2)
      {
        if (Integer.parseInt(arrStDate[2])%4==0)
          numDaysInMonth = 29;
        else
          numDaysInMonth = 28;
      }
      int borrowMonth = (tempDays-1)/numDaysInMonth;
      int newDays = ((tempDays-1)%numDaysInMonth) + 1;
      int tempMonth = month + borrowMonth;
      int borrowYear = (tempMonth-1)/12;
      int newMonth = ((tempMonth-1)%12)+1;
      int newYear = Integer.parseInt(arrStDate[2]) + borrowYear;
      String strMonth = newMonth + "";
      if(strMonth.length() <2)
        strMonth = "0" + strMonth;
      String strDays = newDays + "";
      if(strDays.length() <2)
        strDays = "0" + strDays;
      String strYear = newYear + "";
      if(strYear.length() <2)
        strYear = "0" + strYear;
      String strHour = newHours + "";
      if(strHour.length() <2)
        strHour = "0" + strHour;
      String strMins = newMins + "";
      if(strMins.length() <2)
        strMins = "0" + strMins;
      String strSecs = newSec + "";
      if(strSecs.length() <2)
        strSecs = "0" + strSecs;
      endDateTime = strMonth + "/" + strDays + "/" + strYear + " " + strHour + ":" + strMins + ":" + strSecs;
      return endDateTime;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "CalculateEndTime", "", "", "Exception-", ex);
      return "";
    }
  }

  public static boolean verifyStartEndDateTime(String dateInput, String timeInput, String testRunStartDateTime, String testRunEndDateTime)
  {
    try
    {
      Log.debugLog(className, "verifyStartEndDateTime", "", "", "Start Method");
      String arrStartDateTime[] = rptUtilsBean.strToArrayData(testRunStartDateTime, " ");
      String arrStDate[] = rptUtilsBean.strToArrayData(arrStartDateTime[0], "/");
      String arrStTime[] = rptUtilsBean.strToArrayData(arrStartDateTime[1], ":");
      String arrEndDateTime[] = rptUtilsBean.strToArrayData(testRunEndDateTime, " ");
      String arrEdDate[] = rptUtilsBean.strToArrayData(arrEndDateTime[0], "/");
      String arrEdTime[] = rptUtilsBean.strToArrayData(arrEndDateTime[1], ":");
      String arrInputDate[] = rptUtilsBean.strToArrayData(dateInput, "/");
      String arrInputTime[] = rptUtilsBean.strToArrayData(timeInput, ":");

      String inputFormatedDate = arrInputDate[2] + arrInputDate[0] + arrInputDate[1] + arrInputTime[0] + arrInputTime[1] + arrInputTime[2];
      String stFormatedDate = arrStDate[2] + arrStDate[0] + arrStDate[1] + arrStTime[0] + arrStTime[1] + arrStTime[2];
      String endFormatedDate = arrEdDate[2] + arrEdDate[0] + arrEdDate[1] + arrEdTime[0] + arrEdTime[1] + arrEdTime[2];
      long intInputDate = Long.parseLong(inputFormatedDate);
      long intStDate = Long.parseLong(stFormatedDate);
      long intEndDate = Long.parseLong(endFormatedDate);

      if(intInputDate < intStDate || intInputDate > intEndDate)
        return false;
      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "verifyStartEndDateTime", "", "", "Exception-", ex);
      return false;
    }
  }

  public static boolean verifyStartEndTime(String timeInput, String testRunDuration)
  {
    try
    {
      Log.debugLog(className, "verifyStartEndTime", "", "", "Start Method");
      String arrEdTime[] = rptUtilsBean.strToArrayData(testRunDuration, ":");
      String arrInputTime[] = rptUtilsBean.strToArrayData(timeInput, ":");

      String inputFormatedTime = arrInputTime[0] + arrInputTime[1] + arrInputTime[2];
      String endFormatedTime = arrEdTime[0] + arrEdTime[1] + arrEdTime[2];
      long intInputTime = Long.parseLong(inputFormatedTime);
      long intStTime = 0;
      long intEndTime = Long.parseLong(endFormatedTime);
      if(intInputTime < intStTime || intInputTime > intEndTime)
        return false;

      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "verifyStartEndTime", "", "", "Exception-", ex);
      return false;
    }
  }

}

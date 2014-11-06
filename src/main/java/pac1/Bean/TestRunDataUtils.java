/**************************************************************************************
 * @author Ravi
 * @Purpose Used for creating common methods
 * @Modification History 
 * 
 * 25/09/2013 Ravi Kant Sharma -> Initial Version
 *************************************************************************************/
package pac1.Bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Vector;

public class TestRunDataUtils
{
  public static String className = "TestRunDataUtils";

  // This method read test run pdf
  public static Vector<String> getTestRunPDF(int testRun)
  {
    return getTestRunPDF(testRun, -1);
  }

  public static Vector<String> getTestRunPDF(int testRun, int baseTR)
  {
    Log.debugLog(className, "setTestRunPDF", "", "", "Method Called. Test Run = " + testRun);
    
    if (testRun == -1)
    {
      Log.errorLog(className, "setTestRunPDF", "", "", "Wrong given Test Run = " + testRun);
      return null;
    }

    String gdfRptFileWithPath = "";
    if (baseTR == -1)
      gdfRptFileWithPath = Config.getWorkPath() + "/webapps/logs/TR" + testRun + "/testrun.pdf";
    else
      gdfRptFileWithPath = Config.getWorkPath() + "/webapps/logs/TR" + baseTR + "/NetCloud/TR" + testRun + "/testrun.pdf";

    return rptUtilsBean.readReport(gdfRptFileWithPath);
  }

  // This method read the testrun.gdf
  public static Vector<String> getTestRunGDF(int testRun)
  {
    return getTestRunGDF(testRun, -1);
  }

  public static Vector<String> getTestRunGDF(int testRun, int baseTR)
  {
    Log.debugLog(className, "setTestRunGDF", "", "", "Method Called. Test Run = " + testRun);
    String gdfRptFileWithPath = "";
    if (baseTR == -1)
      gdfRptFileWithPath = Config.getWorkPath() + "/webapps/logs/TR" + testRun + "/testrun.gdf";
    else
      gdfRptFileWithPath = Config.getWorkPath() + "/webapps/logs/TR" + baseTR + "/NetCloud/TR" + testRun + "/testrun.gdf";

    return rptUtilsBean.readReport(gdfRptFileWithPath);
  }

  // Reading summary.top file
  public static String getTestRunSummary_Top(int testRun)
  {
    Log.debugLog(className, "getTestRunSummary_Top", "", "", "Method Called. Test Run = " + testRun);
    if (testRun == -1)
      return "";

    String summary_top_line = "";
    String gdfRptFileWithPath = Config.getWorkPath() + "/webapps/logs/TR" + testRun + "/summary.top";
    Vector<String> tmpLine = rptUtilsBean.readReport(gdfRptFileWithPath);
    if (tmpLine != null && tmpLine.size() > 0)
      summary_top_line = tmpLine.get(0).toString();

    return summary_top_line;
  }

  // Reading reporting level
  public static int getReportingLevel(String summaryTopFile)
  {
    int reportingLevel = 1;
    try
    {
      if (summaryTopFile != null && !summaryTopFile.equals(""))
      {
        String[] arrSummary_TopFileData = rptUtilsBean.strToArrayData(summaryTopFile, "|");
        if (arrSummary_TopFileData != null)
        {
          if (!arrSummary_TopFileData[11].trim().equals(""))
            reportingLevel = Integer.parseInt(arrSummary_TopFileData[11].trim());
        }
      }
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getReportingLevel", "", "", "Exception - ", ex);
    }

    return reportingLevel;
  }

  // Reading wan env keyword
  public static int getWanEnv(String summaryTopFile)
  {
    int wanEnv = 0;
    try
    {
      if (summaryTopFile != null && !summaryTopFile.equals(""))
      {
        String[] arrSummary_TopFileData = rptUtilsBean.strToArrayData(summaryTopFile, "|");
        if (arrSummary_TopFileData != null)
        {
          if (!arrSummary_TopFileData[10].trim().equals(""))
            wanEnv = Integer.parseInt(arrSummary_TopFileData[10].trim());
        }
      }
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getWanEnv", "", "", "Exception - ", ex);
    }

    return wanEnv;
  }

  public static long getRtgMsgSizeByTestRun(int testNum)
  {
    try
    {
      Log.debugLog(className, "getRtgMsgSizeByTestRun", "", "", "Method Called.");
      String osname = System.getProperty("os.name").trim().toLowerCase();
      if (!osname.startsWith("win"))
      {
        CmdExec cmdExec = new CmdExec();
        String command = "ls";
        String args = "-l " + getRtgMessageFilePath(testNum);
        Vector result = cmdExec.getResultByCommand(command, args, CmdExec.SYSTEM_CMD, "netstorm", "root");
        if (result != null)
        {
          String tempRecord = result.get(0).toString();
          String[] fileSizeTemp = tempRecord.split(" ");
          long fileSize = Long.parseLong(fileSizeTemp[4]);
          return fileSize;
        }
        else
        {
          Log.errorLog(className, "getRtgMsgSizeByTestRun", "", "", "result = " + result);
          return 0;
        }
      }
      else
      {
        // for windows machine
        FileInputStream fis = new FileInputStream(getRtgMessageFilePath(testNum));
        long fileSize = fis.available();
        return fileSize;
      }
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getRtgMsgSizeByTestRun", "", "", "Exception - ", ex);
      return 0;
    }
  }

  private static String getRtgMessageFilePath(int testNum)
  {
    return (Config.getWorkPath() + "/webapps/logs/TR" + testNum + "/rtgMessage.dat");
  }

}

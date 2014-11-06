/*--------------------------------------------------------------------
  @Name    : CompareRptUsingRtpl.java
  @Author   : Prabhat
  @Purpose : To Compare Reports using template.
  @Modification History:
    07/17/07 : Prabhat --> Initial Version
    02/03/09 : Prabhat --> Add code to handle Percentile Reports
    03/19/09 : Prabhat --> Add code for server signature
    06/23/09 : Prabhat --> Handle code for Analysis GUI

  Pending Tasks:
----------------------------------------------------------------------*/

package pac1.Bean;

import java.awt.Paint;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.Vector;
import java.text.DecimalFormat;

import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import pac1.Bean.RptInfo;
import pac1.Bean.GraphName.GraphNameUtils;
import pac1.Bean.Percentile.PercentileDataUtils;
import pac1.Bean.Percentile.PercentileReportGenerator;

public class CmpRptUsingRtpl
{
  private String className = "CmpRptUsingRtpl";
  public final static String END_OF_RPT_SET_GEN_LINE = "End of report set generation.";

  private ReportSetControl reportSetControl = null;
  private ReportGenerate reportGenerate = null;
  private CmpRptMsrOptions[] cmpRptMsrOptions = null;
  private CmpRptTestRunData[] cmpRptTestRunData = null;
  private boolean isNetCloudTest;
  private String[] msrData;
  private int debugLevel = 0;
  ArrayList<String> tempVecNames = new ArrayList<String>();
  ArrayList<VectorMappingDTO> vectorMappingList = new ArrayList<VectorMappingDTO>();
  /**
   * object of AnlsCmpRptData for Analysis
   */
  private AnlsCmpRptData anlsCmpRptData = null;

  /**
   * List of all uniqure reports names
   */
  LinkedHashSet lhsRptNames = new LinkedHashSet();

  /**
   * List of all uniqure server signature names
   */
  TreeSet tsServerSignatureNames = new TreeSet();

  /**
   * map to keep cmpGraphData obj for unique reports
   */
  HashMap mapCmpGraphDataObj = new HashMap();

  /**
   * list of all report details "Graph View Type|Graph Data Idx|Template Details|Report GrpName|Report Name"
   */
  Vector vecReportDetails = new Vector();

  public int numTestRun = -1;
  private String testRun = "";
  private boolean changePerm = true;

  /**
   * Reporting Template Name
   */
  private String rtplName = "";
  String reportSetName = "";
  private String rptFilePath = "";

  /**
   * Report generation start date/time
   */
  Date rptGenStartDate = null;

  /**
   * Report Template file loaded in 2D arrays
   */
  private String[][] arrRtplRecs = null;

  /**
   * Number of reports generated succesfuly
   */
  private int numRptGenSucess = 0;

  /**
   * Number of reports generation failure
   */
  private int numRptGenFail = 0;

  /**
   * Number of unique test runs
   */
  private int numCmpRptTestRunData = 0;

  /**
   * if this is called from Analysis then it is true
   */
  private boolean anlsFlag = false;

  
  /*This variable is using to avoid multiple creations of objects in case of percentile*/
  private boolean isPercentileFirstTime = true;
  private boolean isSlabCountFirstTime = true;
  
  public CmpRptUsingRtpl(String testRun, boolean changePerm, boolean anlsFlag)
  {
    try
    {
      Log.reportDebugLog(className, "CmpRptUsingRtpl", "", "", "Constructor Called. changePerm = " + changePerm + ", testRun = " + testRun + ", anlsFlag = " + anlsFlag);
      this.testRun = testRun;
      this.changePerm = changePerm;
      this.anlsFlag = anlsFlag;
      numTestRun = Integer.parseInt(testRun.trim());
      reportSetControl = new ReportSetControl(changePerm);
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "CmpRptUsingRtpl", "", "", "Exception - ", ex);
    }
  }

  public CmpRptUsingRtpl()
  {

  }

  /**
   * Ravi -> This Function called by jsp
   * 
   * Arguments: rtplName - Report Template Name reportSetName - Report Set Name msrData - Array of measurements to be compared. Each measurement is in the following format
   * 
   * "MsrName|TestRun|TestRunStartDateTime|Override|XAxisTimeFormat|TimeOption|TimeFormat|StartDate|StartTime|EndDate|EndTime"
   * 
   * @param rtplName
   * @param reportSetName
   * @param msrData
   * @return
   */

  public boolean genCmpRptSet(String rtplName, String reportSetName, String[] msrData)
  {
    try
    {
      Log.reportDebugLog(className, "genCmpRptSet", "", "", "Method called. " + "Test Run = " + testRun + ", ReportSetName = " + reportSetName + ", msrData = " + rptUtilsBean.strArrayToStr(msrData, ","));
      this.rtplName = rtplName;
      this.reportSetName = reportSetName;
      rptGenStartDate = new Date();
      boolean chkAndCreRptSetDirFlag = chkAndCreRptSetDir();
      if (chkAndCreRptSetDirFlag == false)
      {
        Log.errorLog(className, "genCmpRptSet", "", "", "chkAndCreRptSetDirFlag = false");
        logEndRptSetGen("Error");
        return false;
      }

      /**
       * This need be called after chkAndCreRptSetDir() as report set dir is created in that method
       */
      logStartRptSetGen();

      rptFilePath = reportSetControl.getReportSetPath(reportSetName, numTestRun);
      
      Log.reportDebugLog(className, "genCmpRptSet", "", "", "rptFilePath = " + rptFilePath);
      boolean initFlag = init(msrData);
      
      Log.reportDebugLog(className, "genCmpRptSet", "", "", "initFlag = " + initFlag);
      if (initFlag == false)
      {
        logEndRptSetGen("Error");
        return false;
      }

      boolean readTemplateFileFlag = readTemplateFile();
      Log.reportDebugLog(className, "genCmpRptSet", "", "", "readTemplateFileFlag = " + readTemplateFileFlag);
      if (readTemplateFileFlag == false)
      {
        logEndRptSetGen("Error");
        return false;
      }

      boolean getAllUniqueRptNamesAndSSFlag = getAllUniqueRptNamesAndSS();
      
      Log.reportDebugLog(className, "genCmpRptSet", "", "", "getAllUniqueRptNamesAndSSFlag = " + getAllUniqueRptNamesAndSSFlag + ", anlsFlag = " + anlsFlag);
      if (getAllUniqueRptNamesAndSSFlag == false)
      {
        logEndRptSetGen("Error");
        return false;
      }

      if (!anlsFlag)
        copyFilesToRptSetDir();

      boolean genCmpRptDataFlag = genCmpRptData();
      
      Log.reportDebugLog(className, "genCmpRptSet", "", "", "genCmpRptDataFlag = " + genCmpRptDataFlag);
      if (genCmpRptDataFlag == false)
      {
        logEndRptSetGen("Error");
        return false;
      }

      if (!anlsFlag)
      {
        /**
         * This flag will true when user is not root else false
         */
        if (changePerm)
          if (rptUtilsBean.changeFilePerm(reportSetControl.getReportSetPath(reportSetName, numTestRun), "netstorm", "netstorm", "775") == false)
            reportStatusLog(reportSetName, numTestRun, "genCmpRptSet", "", "", "Warning: Error in changing permission of report set directory for report set '" + reportSetName + "'. Ignored.");
      }

      logEndRptSetGen("Successful");
      return true;
    }
    catch (Exception e)
    {
      reportStatusLog(reportSetName, numTestRun, "genCmpRptSet", "", "", "Error: Exception in report set generation - " + e);
      logEndRptSetGen("Error");
      return false;
    }
  }

  /**
   * This will allocate and initialize CmpRptMsrOptions and CmpRptTestRunData. This will make sure CmpRptTestRunData is allocated for unique test runs only.
   * 
   * @param msrData
   * @return
   */
  private boolean initCmpRptMsrOptionsAndData(String[] msrData)
  {
    try
    {
      Log.reportDebugLog(className, "initCmpRptMsrOptionsAndData", "", "", "Method called.");
      cmpRptMsrOptions = new CmpRptMsrOptions[msrData.length];
      cmpRptTestRunData = new CmpRptTestRunData[msrData.length];
      int testRunIndex = 0;

      for (int i = 0; i < msrData.length; i++)
      {
        Log.reportDebugLog(className, "initCmpRptMsrOptionsAndData", "", "", "Processing msrData = " + msrData[i]);
        cmpRptMsrOptions[i] = new CmpRptMsrOptions(this);
        int testRunNum = cmpRptMsrOptions[i].getTestRun(msrData[i]);
        Log.reportDebugLog(className, "initCmpRptMsrOptionsAndData", "", "", "Creating CmpRptTestRunData object for test run = " + testRunNum + ", testRunIndex = " + testRunIndex);
        if (isNetCloudTest)
        {
          String genName = cmpRptMsrOptions[i].getMSRName(msrData[i]);
          if (genName.equals("Controller"))
            cmpRptTestRunData[testRunIndex] = new CmpRptTestRunData(testRunNum, rtplName, this, cmpRptMsrOptions[i], genName, "NA");
          else
            cmpRptTestRunData[testRunIndex] = new CmpRptTestRunData(testRunNum, rtplName, this, cmpRptMsrOptions[i], genName, testRun);
        }
        else
        {
          cmpRptTestRunData[testRunIndex] = new CmpRptTestRunData(testRunNum, rtplName, this, cmpRptMsrOptions[i]);
        }

        if (cmpRptTestRunData[testRunIndex].initTestRunData() == false)
        {
          Log.reportDebugLog(className, "initCmpRptMsrOptionsAndData", "", "", "cannot initTestRunData.");
          return false;
        }

        Log.reportDebugLog(className, "initCmpRptMsrOptionsAndData", "", "", "Creating setCmpRptMsrOptions object for test run = " + testRunNum + ", testRunIndex = " + testRunIndex);

        if(isNetCloudTest)
        {
          if (cmpRptMsrOptions[i].setCmpRptMsrOptions(msrData[i], testRunIndex, cmpRptTestRunData[testRunIndex].getTestRunTotalTime(), numTestRun) == false)
            return false;
        }
        else
        {
          if (cmpRptMsrOptions[i].setCmpRptMsrOptions(msrData[i], testRunIndex, cmpRptTestRunData[testRunIndex].getTestRunTotalTime(), -1) == false)
           return false;
        }
        
        if (cmpRptTestRunData[testRunIndex].readData() == false)
          return false;

        testRunIndex++;
      }

      numCmpRptTestRunData = cmpRptTestRunData.length;
      return (true);
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "initCmpRptMsrOptionsAndData", "", "", "Exception - ", ex);
      return false;
    }
  }

  /**
   * Used for init
   * 
   * @param msrData
   * @return
   */
  private boolean init(String[] msrData)
  {
    try
    {
      Log.reportDebugLog(className, "init", "", "", "Method called. " + "Test Run = " + testRun + ", ReportSetName = " + reportSetName + ", msrData = " + rptUtilsBean.strArrayToStr(msrData, ","));
      rptFilePath = reportSetControl.getReportSetPath(reportSetName, numTestRun);
      reportGenerate = new ReportGenerate(numTestRun, true);

      if (initCmpRptMsrOptionsAndData(msrData) == false)
      {
        Log.reportDebugLog(className, "init", "", "", "returning false. Initialization failed due due to initCmpRptMsrOptionsAndData = false");
        return false;
      }

      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "init", "", "", "Error: Initialization failed due to exception", e);
      reportStatusLog(reportSetName, numTestRun, "init", "", "", "Error: Initialization failed due to exception - " + e);
      return false;
    }
  }

  /**
   * Start - Function which are same same in GenRptUsingRtpl. Need to move to // common file. This will copy template file and create input file in report set directory
   * 
   */
  private void copyFilesToRptSetDir()
  {
    try
    {
      Log.reportDebugLog(className, "copyFilesToRptSetDir", "", "", "Creating report set input file - " + reportSetName + ".inp");

      if (reportSetControl.createReportSetInputFile(testRun, rtplName, reportSetName, false, "", "") == false)
        reportStatusLog(reportSetName, numTestRun, "copyFilesToRptSetDir", "", "", "Warning: Error in creating input file for the report set '" + reportSetName + "'. Ignored.");

      Log.reportDebugLog(className, "copyFilesToRptSetDir", "", "", "Copying template file used for generating report set " + reportSetName);

      if (ReportTemplate.copyTemplateToReportSet(testRun, rtplName, rtplName, "", reportSetName, changePerm) == false)
        reportStatusLog(reportSetName, numTestRun, "copyFilesToRptSetDir", "", "", "Warning: Error in copying template file used to generate report set '" + reportSetName + "'. Ignored.");
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "copyFilesToRptSetDir", "", "", "Exception - ", ex);
    }
  }

  /**
   * Generate Report Status log
   * 
   * @param reportSetName
   * @param numTestRun
   * @param method
   * @param future1
   * @param future2
   * @param text
   */
  public void reportStatusLog(String reportSetName, int numTestRun, String method, String future1, String future2, String text)
  {
    if (!anlsFlag)
    {
      String logFileName = reportSetControl.getReportSetStatusLogFileName(reportSetName, numTestRun);
      Log.writeToFile(logFileName, "Status", Log.getDateTime() + " " + text);
    }

    Log.reportDebugLog(className, method, "", "", text);
  }

  // Check and Create Report Set Directory
  private boolean chkAndCreRptSetDir()
  {
    try
    {
      Log.reportDebugLog(className, "chkAndCreRptSetDir", "", "", "Method Called.");

      if (!anlsFlag)
      {
        Log.reportDebugLog(className, "chkAndCreRptSetDir", "", "", "Creating report set directory. Test run = " + testRun + ", report set name = " + reportSetName);
        if (reportSetControl.isReportSetExisting(numTestRun, reportSetName) == true)
        {
          /**
           * Issue - This will log in the existing report set log file. Need to fix later
           * 
           */
          reportStatusLog(reportSetName, numTestRun, "chkAndCreRptSetDir", "", "", "Error: Report set with the same name already exists. Please use different report set name and try again.");
          return false;
        }
        else
        {
          File gpFile = reportSetControl.createReportSetDir(numTestRun, reportSetName);
          if (gpFile == null)
          {
            // Issue - If creation of report set dir fails, then log will also
            // file. Need to fix later
            reportStatusLog(reportSetName, numTestRun, "chkAndCreRptSetDir", "", "", "Error: Error in creating report set directory.");
            return false;
          }
        }
      }
      return true;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "chkAndCreRptSetDir", "", "", "Exception - ", ex);
      return false;
    }
  }

  public void logStartRptSetGen()
  {
    reportStatusLog(reportSetName, numTestRun, "logStartRptSetGen", "", "", "Starting report set generation using template. Test Run = " + testRun + ", Template Name = " + rtplName + ", Report Set Name = " + reportSetName + "\n");
  }

  /**
   * Message for status of report
   * 
   * @param status
   */
  public void logEndRptSetGen(String status)
  {
    try
    {
      Log.debugLog(className, "logEndRptSetGen", "", "", "Method Called. status = " + status);
      String finalStatus = "Report set generation is successful";
      if (numRptGenFail != 0)
      {
        if (numRptGenSucess == 0)
          finalStatus = "Error: Report set generation failed";
        else
          finalStatus = "Warning: Report set generation is partially successful";
      }

      reportStatusLog(reportSetName, numTestRun, "logEndRptSetGen", "", "", finalStatus + ". Total time taken is " + rptUtilsBean.convMilliSecToStr((new Date()).getTime() - rptGenStartDate.getTime()));
      reportStatusLog(reportSetName, numTestRun, "logEndRptSetGen", "", "", "Total number of reports generated sucessfully = " + numRptGenSucess + ". Total number of report generation failure = " + numRptGenFail + "\n");
      reportStatusLog(reportSetName, numTestRun, "logEndRptSetGen", "", "", END_OF_RPT_SET_GEN_LINE); // This
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "logEndRptSetGen", "", "", "Exception - ", ex);
    }
  }

  /**
   * Read template file
   * 
   * @return
   */
  private boolean readTemplateFile()
  {
    try
    {
      Log.reportDebugLog(className, "readTemplateFile", "", "", "Reading template file. Test run = " + testRun + ", template name = " + rtplName);

      arrRtplRecs = ReportTemplate.getTemplateDetails(rtplName, false);

      if (arrRtplRecs.length < 4)
        reportStatusLog(reportSetName, numTestRun, "readTemplateFile", "", "", "INFO: Reporting Template does not have any reports to be generated. Template name is '" + rtplName + "'.");
      return true;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "readTemplateFile", "", "", "", ex);
      return false;
    }
  }

  private void logStartRptGen(String rptName)
  {
    reportStatusLog(reportSetName, numTestRun, "logStartRptGen", "", "", "Starting Report generation for report - " + rptName);
  }

  private void logEndRptGen(boolean result)
  {
    if (result)
    {
      numRptGenSucess++;
      reportStatusLog(reportSetName, numTestRun, "logEndRptGen", "", "", "End of report generation.\n");
    }
    else
    {
      numRptGenFail++;
      reportStatusLog(reportSetName, numTestRun, "logEndRptGen", "", "", "Error: Error in report generation.\n");
    }
  }

  // End - Function which are same same in GenRptUsingRtpl. Need to move to
  // common file.

  // Get all the Unique Rpt Names & server signature name in TreeSet from all
  // Cmp Rpt Test Run
  private boolean getAllUniqueRptNamesAndSS()
  {
    Log.reportDebugLog(className, "getAllUniqueRptNamesAndSS", "", "", "Method called. anlsFlag = " + anlsFlag);
    try
    {
      for (int i = 0; i < numCmpRptTestRunData; i++)
      {
        int[] rptGroupID = cmpRptTestRunData[i].getGroupIDArray();
        int[] rptGraphID = cmpRptTestRunData[i].getGraphIDArray();
        int[] pdfID = cmpRptTestRunData[i].getPDFIDArray();
        int[] pctDataIdx = cmpRptTestRunData[i].getPCTDataIdxArray();
        int[] graphDataIdx = cmpRptTestRunData[i].getRptTemplateGraphDataIdxArray();

        String[] rptGrpNames = cmpRptTestRunData[i].getRtpGrpNames();
        String[] rptNames = cmpRptTestRunData[i].getRtpNames();
        String[] rptGraphViewType = cmpRptTestRunData[i].getGraphViewTypeArray();
        String[] rptTemplateDetail = cmpRptTestRunData[i].getTemplateDetailsArray();

        for (int j = 0; j < rptNames.length; j++)
        {
          Log.reportDebugLog(className, "getAllUniqueRptNamesAndSS", "", "", "Adding report name in the HasMap. Report Name = " + rptNames[j] + " , Report Group name = " + rptGrpNames[j] + " , Graph View Type = " + rptGraphViewType[j] + " , Report Id = " + rptGraphID[j] + ", Report Group ID = " + rptGroupID[j]);

          String tempStr = rptNames[j];
          /*
           * if (rptGraphViewType[j].equals("Percentile Graph") || rptGraphViewType[j].equals("Slab Count Graph") || rptGraphViewType[j].equals("Frequency Distribution Graph") ||
           * rptGraphViewType[j].equals("Derived Graph"))
           * 
           * else tempStr = createMapKey(rptGroupID[j], rptGraphID[j], rptNames[j]);
           */
          Log.reportDebugLog(className, "getAllUniqueRptNamesAndSS", "", "", "tempStr = " + tempStr);

          if (lhsRptNames.add(tempStr) == false)
          {
            if (!rptGraphViewType[j].equals("Derived Graph"))
            {
              CmpGraphData cmpGraphData = (CmpGraphData) mapCmpGraphDataObj.get(tempStr);
              cmpGraphData.setGraphDataIdxPCTDataIdxAndPDFId(i, graphDataIdx[j], pctDataIdx[j], pdfID[j]);
              mapCmpGraphDataObj.put(tempStr, cmpGraphData);
              Log.reportDebugLog(className, "getAllUniqueRptNamesAndSS", "", "", "Key = " + tempStr + " already exists in the Map, updating cmpGraphaData object in the HashMap. Report Group Name|Report Name = " + rptGrpNames[j] + "|" + rptNames[j]);
            }
            else
            {
              Log.reportDebugLog(className, "getAllUniqueRptNamesAndSS", "", "", "Key = " + tempStr + " already exists in the HashMap. Report Group Name|Report Name = " + rptGrpNames[j] + "|" + rptNames[j]);
            }
          }
          else
          {
            CmpGraphData cmpGraphData = new CmpGraphData(rptGroupID[j], rptGraphID[j], rptGrpNames[j], rptNames[j], rptGraphViewType[j], rptTemplateDetail[j], numCmpRptTestRunData);
            if (!rptGraphViewType[j].equals("Derived Graph"))
              cmpGraphData.setGraphDataIdxPCTDataIdxAndPDFId(i, graphDataIdx[j], pctDataIdx[j], pdfID[j]);

            mapCmpGraphDataObj.put(tempStr, cmpGraphData);

            Log.reportDebugLog(className, "getAllUniqueRptNamesAndSS", "", "", "Adding Report details in map for key = " + tempStr + " Report Group Name|Report Name = " + rptGrpNames[j] + "|" + rptNames[j]);
          }
        }

        /**
         * currently we are not support server signature in Analysis
         */
        if (!anlsFlag)
        {
          ArrayList arrListTempSS = cmpRptTestRunData[i].serverSignatureInfo.getListServerSignature();

          if ((arrListTempSS != null) && (arrListTempSS.size() > 0))
          {
            for (int ss = 0; ss < arrListTempSS.size(); ss++)
            {
              String strServerSignatureName = arrListTempSS.get(ss).toString();

              Log.reportDebugLog(className, "getAllUniqueRptNamesAndSS", "", "", "Adding Server Signature name in the TreeSet. Server Signature Name = " + strServerSignatureName);

              if (tsServerSignatureNames.add(strServerSignatureName) == false)
                Log.reportDebugLog(className, "getAllUniqueRptNamesAndSS", "", "", "Server Signature Name already exists is the TreeSet. Server Signature Name = " + strServerSignatureName);
              else
                Log.reportDebugLog(className, "getAllUniqueRptNamesAndSS", "", "", "Server Signature Name added in the TreeSet. Server Signature Name = " + strServerSignatureName);
            }
          }
        }
      }

      Log.reportDebugLog(className, "getAllUniqueRptNamesAndSS", "", "", "Unique list of key in linkedHashSet : " + lhsRptNames);

      if (!anlsFlag)
        Log.reportDebugLog(className, "getAllUniqueRptNamesAndSS", "", "", "Unique list of Server Signature names in TreeSet: " + tsServerSignatureNames);

      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getAllUniqueRptNamesAndSS", "", "", "Exception", e);
      reportStatusLog(reportSetName, numTestRun, "getAllUniqueRptNamesAndSS", "", "", "Error: Error in creating unique list of report names - " + e);
      return false;
    }
  }

  // this function return the map key like (rptGroupId.rptId.vecName)
  private String createMapKey(int rptGroupID, int rptGraphID, String rptName)
  {
    Log.reportDebugLog(className, "createMapKey", "", "", "Method called. Report group id = " + rptGroupID + ", Report id = " + rptGraphID + ", Report name = " + rptName);

    String mapKey = "";
    String vecName = "NA";

    // Ravi -> Bug if Graph Name contains dash sign then it will return wrong vector name
    int strIndex = rptName.indexOf("-");
    if (strIndex != -1)
      vecName = rptName.substring((strIndex + 1), rptName.length());

    Log.reportDebugLog(className, "createMapKey", "", "", "vecName = " + vecName + " ,tempVecNames.size() = " + tempVecNames.size());

    int i = 0;
    if (tempVecNames.size() == 0)
    {
      tempVecNames.add(vecName);
    }
    else
    {
      for (i = 0; i < tempVecNames.size(); i++)
      {
        String tmpVectorName = tempVecNames.get(i);
        if (tmpVectorName.equals(vecName))
        {
          mapKey = rptGroupID + "." + rptGraphID + "." + tmpVectorName;
          return mapKey;
        }
      }
    }

    if (vectorMappingList == null || vectorMappingList.size() == 0)
      vectorMappingList = rptUtilsBean.getVectorNameFromMappedFile("VectorMapping.dat");

    if (i == tempVecNames.size())
    {
      // Getting Vector Name From Mapping File.
      String mappedVectorName = rptUtilsBean.getVectorNameFromVectorMapping(vectorMappingList, rptGroupID, vecName);
      for (int j = 0; j < tempVecNames.size(); j++)
      {
        if (tempVecNames.get(j).equals(mappedVectorName))
        {
          mapKey = rptGroupID + "." + rptGraphID + "." + tempVecNames.get(j);
          return mapKey;
        }
      }
    }

    mapKey = rptGroupID + "." + rptGraphID + "." + vecName;
    Log.reportDebugLog(className, "createMapKey", "", "", "mapKey = " + mapKey);
    tempVecNames.add(vecName);
    return mapKey;
  }

  private boolean addCmpDataFixedPart(CmpRptUtils cmpRptUtils)
  {
    String cmpRptDataMsrNameLine = "Report Group Name|Report Name|Sort Order|Hide|Highlight|Status|Compare Report Type|Future2|Future3|Future4";
    String cmpRptDataTestRunLine = "NA|Test Run Number|NA|NA|NA|NA|NA|NA|NA|NA";
    String cmpRptDataTestRunStartDateTimeLine = "NA|Test Run Start Date/Time|NA|NA|NA|NA|NA|NA|NA|NA";
    String cmpRptDataTestDurationLine = "NA|Test Duration|NA|NA|NA|NA|NA|NA|NA|NA";
    String cmpRptDataTimeOptionLine = "NA|Time Option|NA|NA|NA|NA|NA|NA|NA|NA";
    String cmpRptDataTimeSelFormatLine = "NA|Time Selection Format|NA|NA|NA|NA|NA|NA|NA|NA";
    String cmpRptDataStartDateTimeLine = "NA|Start Date/Time|NA|NA|NA|NA|NA|NA|NA|NA";
    String cmpRptDataEndDateTimeLine = "NA|End Date/Time|NA|NA|NA|NA|NA|NA|NA|NA";

    for (int msrNum = 0; msrNum < cmpRptMsrOptions.length; msrNum++)
    {
      if (msrNum == 0)
      {
        cmpRptDataMsrNameLine = cmpRptDataMsrNameLine + "|" + cmpRptMsrOptions[msrNum].msrName + "|" + "Maximum ("+ cmpRptMsrOptions[msrNum].msrName + ")";
        cmpRptDataTestRunLine = cmpRptDataTestRunLine + "|" + cmpRptMsrOptions[msrNum].testRun + "|NA";
        cmpRptDataTestRunStartDateTimeLine = cmpRptDataTestRunStartDateTimeLine + "|" + cmpRptMsrOptions[msrNum].testRunStartDateTime + "|NA";
        cmpRptDataTestDurationLine = cmpRptDataTestDurationLine + "|" + cmpRptMsrOptions[msrNum].testDuration + "|NA";
        cmpRptDataTimeOptionLine = cmpRptDataTimeOptionLine + "|" + cmpRptMsrOptions[msrNum].timeOption + "|NA";
        cmpRptDataTimeSelFormatLine = cmpRptDataTimeSelFormatLine + "|" + cmpRptMsrOptions[msrNum].timeSelectionFormat + "|NA";
        cmpRptDataStartDateTimeLine = cmpRptDataStartDateTimeLine + "|" + cmpRptMsrOptions[msrNum].startDate + "-" + cmpRptMsrOptions[msrNum].startTime + "|NA";
        cmpRptDataEndDateTimeLine = cmpRptDataEndDateTimeLine + "|" + cmpRptMsrOptions[msrNum].endDate + "-" + cmpRptMsrOptions[msrNum].endTime + "|NA";
      }
      else
      {
        cmpRptDataMsrNameLine = cmpRptDataMsrNameLine + "|" + cmpRptMsrOptions[msrNum].msrName  + "|" + "Maximum ("+ cmpRptMsrOptions[msrNum].msrName + ")" + "|" + cmpRptMsrOptions[msrNum].msrName + " Change" + "|" + cmpRptMsrOptions[msrNum].msrName + " Change(%)";
        cmpRptDataTestRunLine = cmpRptDataTestRunLine + "|" + cmpRptMsrOptions[msrNum].testRun + "|NA|NA|NA";
        cmpRptDataTestRunStartDateTimeLine = cmpRptDataTestRunStartDateTimeLine + "|" + cmpRptMsrOptions[msrNum].testRunStartDateTime + "|NA|NA|NA";
        cmpRptDataTestDurationLine = cmpRptDataTestDurationLine + "|" + cmpRptMsrOptions[msrNum].testDuration + "|NA|NA|NA";
        cmpRptDataTimeOptionLine = cmpRptDataTimeOptionLine + "|" + cmpRptMsrOptions[msrNum].timeOption + "|NA|NA|NA";
        cmpRptDataTimeSelFormatLine = cmpRptDataTimeSelFormatLine + "|" + cmpRptMsrOptions[msrNum].timeSelectionFormat + "|NA|NA|NA";
        cmpRptDataStartDateTimeLine = cmpRptDataStartDateTimeLine + "|" + cmpRptMsrOptions[msrNum].startDate + "-" + cmpRptMsrOptions[msrNum].startTime + "|NA|NA|NA";
        cmpRptDataEndDateTimeLine = cmpRptDataEndDateTimeLine + "|" + cmpRptMsrOptions[msrNum].endDate + "-" + cmpRptMsrOptions[msrNum].endTime + "|NA|NA|NA";
      }
    }
    if (addCmpData(cmpRptUtils, cmpRptDataMsrNameLine) == false)
      return false;
    if (addCmpData(cmpRptUtils, cmpRptDataTestRunLine) == false)
      return false;
    if (addCmpData(cmpRptUtils, cmpRptDataTestRunStartDateTimeLine) == false)
      return false;
    if (addCmpData(cmpRptUtils, cmpRptDataTestDurationLine) == false)
      return false;
    if (addCmpData(cmpRptUtils, cmpRptDataTimeOptionLine) == false)
      return false;
    if (addCmpData(cmpRptUtils, cmpRptDataTimeSelFormatLine) == false)
      return false;
    if (addCmpData(cmpRptUtils, cmpRptDataStartDateTimeLine) == false)
      return false;
    if (addCmpData(cmpRptUtils, cmpRptDataEndDateTimeLine) == false)
      return false;

    return true;
  }
  
  private boolean addCmpDataFixedPartForServerSignatuer(CmpRptUtils cmpRptUtils)
  {
    String cmpRptDataMsrNameLine = "Report Group Name|Report Name|Sort Order|Hide|Highlight|Status|Compare Report Type|Future2|Future3|Future4";
    String cmpRptDataTestRunLine = "NA|Test Run Number|NA|NA|NA|NA|NA|NA|NA|NA";
    String cmpRptDataTestRunStartDateTimeLine = "NA|Test Run Start Date/Time|NA|NA|NA|NA|NA|NA|NA|NA";
    String cmpRptDataTestDurationLine = "NA|Test Duration|NA|NA|NA|NA|NA|NA|NA|NA";
    String cmpRptDataTimeOptionLine = "NA|Time Option|NA|NA|NA|NA|NA|NA|NA|NA";
    String cmpRptDataTimeSelFormatLine = "NA|Time Selection Format|NA|NA|NA|NA|NA|NA|NA|NA";
    String cmpRptDataStartDateTimeLine = "NA|Start Date/Time|NA|NA|NA|NA|NA|NA|NA|NA";
    String cmpRptDataEndDateTimeLine = "NA|End Date/Time|NA|NA|NA|NA|NA|NA|NA|NA";

    for (int msrNum = 0; msrNum < cmpRptMsrOptions.length; msrNum++)
    {
      if (msrNum == 0)
      {
        cmpRptDataMsrNameLine = cmpRptDataMsrNameLine + "|" + cmpRptMsrOptions[msrNum].msrName ;
        cmpRptDataTestRunLine = cmpRptDataTestRunLine + "|" + cmpRptMsrOptions[msrNum].testRun ;
        cmpRptDataTestRunStartDateTimeLine = cmpRptDataTestRunStartDateTimeLine + "|" + cmpRptMsrOptions[msrNum].testRunStartDateTime ;
        cmpRptDataTestDurationLine = cmpRptDataTestDurationLine + "|" + cmpRptMsrOptions[msrNum].testDuration ;
        cmpRptDataTimeOptionLine = cmpRptDataTimeOptionLine + "|" + cmpRptMsrOptions[msrNum].timeOption ;
        cmpRptDataTimeSelFormatLine = cmpRptDataTimeSelFormatLine + "|" + cmpRptMsrOptions[msrNum].timeSelectionFormat ;
        cmpRptDataStartDateTimeLine = cmpRptDataStartDateTimeLine + "|" + cmpRptMsrOptions[msrNum].startDate + "-" + cmpRptMsrOptions[msrNum].startTime ;
        cmpRptDataEndDateTimeLine = cmpRptDataEndDateTimeLine + "|" + cmpRptMsrOptions[msrNum].endDate + "-" + cmpRptMsrOptions[msrNum].endTime ;
      }
      else
      {
        cmpRptDataMsrNameLine = cmpRptDataMsrNameLine + "|" + cmpRptMsrOptions[msrNum].msrName  + "|" + cmpRptMsrOptions[msrNum].msrName + " Change" + "|" + cmpRptMsrOptions[msrNum].msrName + " Change(%)";
        cmpRptDataTestRunLine = cmpRptDataTestRunLine + "|" + cmpRptMsrOptions[msrNum].testRun + "|NA|NA";
        cmpRptDataTestRunStartDateTimeLine = cmpRptDataTestRunStartDateTimeLine + "|" + cmpRptMsrOptions[msrNum].testRunStartDateTime + "|NA|NA";
        cmpRptDataTestDurationLine = cmpRptDataTestDurationLine + "|" + cmpRptMsrOptions[msrNum].testDuration + "|NA|NA";
        cmpRptDataTimeOptionLine = cmpRptDataTimeOptionLine + "|" + cmpRptMsrOptions[msrNum].timeOption + "|NA|NA";
        cmpRptDataTimeSelFormatLine = cmpRptDataTimeSelFormatLine + "|" + cmpRptMsrOptions[msrNum].timeSelectionFormat + "|NA|NA";
        cmpRptDataStartDateTimeLine = cmpRptDataStartDateTimeLine + "|" + cmpRptMsrOptions[msrNum].startDate + "-" + cmpRptMsrOptions[msrNum].startTime + "|NA|NA";
        cmpRptDataEndDateTimeLine = cmpRptDataEndDateTimeLine + "|" + cmpRptMsrOptions[msrNum].endDate + "-" + cmpRptMsrOptions[msrNum].endTime + "|NA|NA";
      }
    }
    if (addCmpData(cmpRptUtils, cmpRptDataMsrNameLine) == false)
      return false;
    if (addCmpData(cmpRptUtils, cmpRptDataTestRunLine) == false)
      return false;
    if (addCmpData(cmpRptUtils, cmpRptDataTestRunStartDateTimeLine) == false)
      return false;
    if (addCmpData(cmpRptUtils, cmpRptDataTestDurationLine) == false)
      return false;
    if (addCmpData(cmpRptUtils, cmpRptDataTimeOptionLine) == false)
      return false;
    if (addCmpData(cmpRptUtils, cmpRptDataTimeSelFormatLine) == false)
      return false;
    if (addCmpData(cmpRptUtils, cmpRptDataStartDateTimeLine) == false)
      return false;
    if (addCmpData(cmpRptUtils, cmpRptDataEndDateTimeLine) == false)
      return false;

    return true;
  }

  // add compare report data for server signature
  private boolean addCmpDataServerSignature(CmpRptUtils cmpRptUtils)
  {
    Log.reportDebugLog(className, "addCmpDataServerSignature", "", "", "Method called. test Run = " + testRun + ", Report Set name = " + reportSetName);

    try
    {
      boolean result = true;
      int msrNum;
      int testRunIndex;

      /**
       * Names of all messurement names used for y-axis lable
       */
      ArrayList arrSSNames = new ArrayList();

      Iterator irServerSignatureNames = tsServerSignatureNames.iterator();
      boolean arrServerSignFlag[] = new boolean[cmpRptMsrOptions.length];
      String diffFileWithPath = rptFilePath + "/server_signature.diff";

      File tmpFile = new File(diffFileWithPath);
      if (tmpFile.exists())
        tmpFile.delete();

      tmpFile.createNewFile();

      FileOutputStream fout = new FileOutputStream(tmpFile, true);
      PrintStream pw = new PrintStream(fout);
      FileDiff fileDiffObj = new FileDiff(pw);

      while (irServerSignatureNames.hasNext())
      {
        String strServerSignatureName = irServerSignatureNames.next().toString();

        String cmpDataLine;

        logStartRptGen(strServerSignatureName);

        reportStatusLog(reportSetName, numTestRun, "addCmpDataServerSignature", "", "", "Generating Compare Report for server signature - " + strServerSignatureName);
        cmpDataLine = "|SS|NA|NA|NA";
        Log.reportDebugLog(className, "addCmpDataServerSignature", "", "", "Generating compare data for server signature name = " + strServerSignatureName);
        arrSSNames.clear();
        Arrays.fill(arrServerSignFlag, false);

        for (msrNum = 0; msrNum < cmpRptMsrOptions.length; msrNum++)
        {
          testRunIndex = cmpRptMsrOptions[msrNum].testRunIndex;
          boolean ssAvail = cmpRptTestRunData[testRunIndex].serverSignatureInfo.isServerSignatuerAvail(strServerSignatureName);
          if (ssAvail)
            arrSSNames.add(cmpRptMsrOptions[msrNum].msrName);

          arrServerSignFlag[msrNum] = ssAvail;
        }

        Log.reportDebugLog(className, "addCmpDataServerSignature", "", "", strServerSignatureName + " is available in measurements: " + arrSSNames.toString());

        boolean flagMasterSignature = false;
        String strMasterServerSignatureName = "";

        for (msrNum = 0; msrNum < cmpRptMsrOptions.length; msrNum++)
        {
          String strServerSignatureStatusData = "NA";

          if (msrNum > 0)
            strServerSignatureStatusData = "NA|NA|NA";

          if (arrServerSignFlag[msrNum])
          {
            testRunIndex = cmpRptMsrOptions[msrNum].testRunIndex;

            if (!flagMasterSignature)
            {
              strMasterServerSignatureName = cmpRptTestRunData[testRunIndex].serverSignatureInfo.getServerSignaturesPathRelative() + strServerSignatureName + cmpRptTestRunData[testRunIndex].serverSignatureInfo.SS_FILE_EXTN;
              flagMasterSignature = true;
            }

            String strServerSignaturePath = cmpRptTestRunData[testRunIndex].serverSignatureInfo.getServerSignaturesPathRelative() + strServerSignatureName + cmpRptTestRunData[testRunIndex].serverSignatureInfo.SS_FILE_EXTN;
            strServerSignatureStatusData = strServerSignaturePath;

            
            if (msrNum > 0)
            {
              if ((Config.getWorkPath() + strMasterServerSignatureName).equals((Config.getWorkPath() + strServerSignaturePath)))
                strServerSignatureStatusData = strServerSignatureStatusData + "|NA|NA";
              else if (fileDiffObj.doDiff(Config.getWorkPath() + strMasterServerSignatureName, Config.getWorkPath() + strServerSignaturePath))
                strServerSignatureStatusData = strServerSignatureStatusData + "|Same|Same";
              else
                strServerSignatureStatusData = strServerSignatureStatusData + "|Different|Different";
            }

            Log.reportDebugLog(className, "addCmpDataServerSignature", "", "", "Adding data to compaer report for server signature. MsrName = " + cmpRptMsrOptions[msrNum].msrName + "Server Signature Data = " + strServerSignatureStatusData + ", Test Run = " + cmpRptTestRunData[testRunIndex].getTestRun());
          }
          cmpDataLine = cmpDataLine + "|" + strServerSignatureStatusData;
        }

        
        String htmlFileName = rptUtilsBean.doReplaceName(strServerSignatureName);
        String[] arrStrSSNames = (String[]) arrSSNames.toArray(new String[arrSSNames.size()]);

        Log.reportDebugLog(className, "addCmpDataServerSignature", "", "", "htmlFileName = " + htmlFileName + ", arrStrSSNames = " + arrStrSSNames);

        String fileName = "";
        fileName = "NA";
        if (!fileName.equals(""))
        {
          String arrRptFields[] = arrRtplRecs[2];
          arrRptFields[0] = "Server Signature - " + strServerSignatureName;
          cmpDataLine = "Server Signature|Server Signature - " + strServerSignatureName + "|NA|0|0|Success" + cmpDataLine;
          if (reportSetControl.addReport(reportSetName, testRun, arrRptFields) == false)
            reportStatusLog(reportSetName, numTestRun, "addCmpDataServerSignature", "", "", "Error: Error in adding report in report set control file. Continuing to next report.");
        }
        else
        {
          result = false;
          cmpDataLine = "Server Signature|Server Signature - " + strServerSignatureName + "|NA|0|0|NA" + cmpDataLine; // cmpDataLine
        }

        if (addCmpData(cmpRptUtils, cmpDataLine) == false)
          return false;

        logEndRptGen(result);
      }

      pw.close();
      fout.close();

      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "addCmpDataServerSignature", "", "", "Error in compare report generation for server signature", e);
      reportStatusLog(reportSetName, numTestRun, "addCmpDataServerSignature", "", "", "Error: Error in compare report generation for server signature due to exception - " + e);
      logEndRptGen(false);
      return false;
    }
  }

  int width = 0;
  int heigth = 0;
  // long startTimeInmilli;
  long endTimeInmilli = 0;

  private boolean genCmpRptData()
  {
    Log.reportDebugLog(className, "genCmpRptData", "", "", "Method called. test Run = " + testRun + ", Report Set name = " + reportSetName + ", anlsFlag = " + anlsFlag);

    boolean result = true;

    try
    {
      CmpRptUtils cmpRptUtils = new CmpRptUtils(testRun, "1");

      if (!anlsFlag)
      {
        if (cmpRptUtils.openCmpDataFile(reportSetName) == false)
          return false;
      }
      else
      {
        anlsCmpRptData = new AnlsCmpRptData();
        Log.reportDebugLog(className, "genCmpRptData", "", "", "AnlsCmpRptData object created");
      }

      int msrNum;
      /*if report generation used from dashboard(server signature) then rptlName was always _Blank_Template in that case we dont have max col*/
      boolean isForServerSignatuer = false;
      for (msrNum = 0; msrNum < cmpRptMsrOptions.length; msrNum++)
      {
        int testRunIndex = cmpRptMsrOptions[msrNum].testRunIndex;
        if(cmpRptTestRunData[testRunIndex].getRtplName().equals("_Blank_Template"))
        {
          isForServerSignatuer = true;
          break;
        }
      }
      
      Log.reportDebugLog(className, "genCmpRptData", "", "", "isForServerSignatuer = " + isForServerSignatuer);
      if(isForServerSignatuer)
      {
	if (addCmpDataFixedPartForServerSignatuer(cmpRptUtils) == false)
	  return false;
      }
      else
      {
        if (addCmpDataFixedPart(cmpRptUtils) == false)
          return false;
      }

      
      if (!anlsFlag)
      {
        if (tsServerSignatureNames.size() > 0)
          if (addCmpDataServerSignature(cmpRptUtils) == false)
            return false;
      }

      Iterator irRptName = lhsRptNames.iterator();
      ArrayList arrMsrNames = new ArrayList();

      int testRunIndex;
      TimeSeriesCollection[] timeSeriesCol = null;
      TimeSeries[] timeSeries = null;
      String[] legendMsg = null;
      XYSeriesCollection[] xySeriesCollectiondataset = null;
      XYSeries[] xySeries = null;
      DefaultCategoryDataset defaultCategoryDataset = null;
      int timeSeriesIndex;
      ReportData rptData = null;

      int rptPrevGraphID = -1;
      int rptPrevGroupID = -1;
      int[] arrPrevPCTGraphDataIndex = null;
      String rptPrevGraphViewType = "";
      boolean genPCTDataFlag = true;
      CmpRptPCTData[] cmpRptPCTData = null;
      PercentileReportGenerator[] percentileReportGenerator = null;
      PercentileReportGenerator[] slabReportGenerator = null;
      
      while (irRptName.hasNext())
      {
        String keyValue = irRptName.next().toString();
        CmpGraphData cmpGraphData = (CmpGraphData) mapCmpGraphDataObj.get(keyValue);

        String rptName = cmpGraphData.getReportName();
        String rptGrpName = cmpGraphData.getReportGroupName();
        String rptGraphViewType = cmpGraphData.getReportGraphViewType();
        String rptTemplateDetail = cmpGraphData.getReportTemplateDetails();

        int rptGraphID = cmpGraphData.getReportGraphId();
        int rptGroupID = cmpGraphData.getReportGroupId();

        int[] arrPDFId = cmpGraphData.getReportPDFId();
        int[] arrGraphDataIndex = cmpGraphData.getArrGraphDataIdx();
        int[] arrPCTGraphDataIndex = cmpGraphData.getArrPCTDataIdx();

        String cmpDataLine;

        double[] meanDataValue = null;

        /**********************************************************************************************
         * Note : Prabhat (02/06/2009) We are generating percentile data for each report, we don't keep percentile data in memory because percentile data in MB or GB, and keeping percentile data in
         * memory is very costly by performance, so we calculate it for each report.
         **********************************************************************************************/
        // if report are same(Group Id & Graph ID) then no need to generate PCT
        // data
        if ((rptPrevGroupID == rptGroupID) && (rptPrevGraphID == rptGraphID))
        {
          // if prev pct data idx not equal to null means not on first time
          if (arrPrevPCTGraphDataIndex != null)
          {
            // if prev pct data idx length and curr pct data idx length equal,
            // then chk its values
            if (arrPrevPCTGraphDataIndex.length == arrPCTGraphDataIndex.length)
            {
              for (int jj = 0; jj < arrPrevPCTGraphDataIndex.length; jj++)
              {
                if (arrPrevPCTGraphDataIndex[jj] != arrPCTGraphDataIndex[jj])
                {
                  genPCTDataFlag = true;
                  break;
                }
                else
                  genPCTDataFlag = false;
              }
            }
            else
              // if length are diff then calculate PCT data again
              genPCTDataFlag = true;
          }
          else
          {
            genPCTDataFlag = true;
          }
        }
        else
        {
          genPCTDataFlag = true;
        }

        logStartRptGen(rptName);
        reportStatusLog(reportSetName, numTestRun, "genCmpRptData", "", "", "Generating Compare Report - " + rptName);

        width = heigth = 0;
        endTimeInmilli = 0;
        cmpDataLine = "|NA|NA|NA|NA";
        Log.reportDebugLog(className, "genCmpRptData", "", "", "Generating compare data for report name = " + rptName);

        arrMsrNames.clear();
        String strGraphDataType = "";

        Log.reportDebugLog(className, "genCmpRptData", "", "", "rptGraphViewType = " + rptGraphViewType);

        /**
         * For simple data generation through rtgMessage.dat file. generate data for Simple, Multi, Tile Graph view type in template
         * 
         */
        if (!(rptGraphViewType.equals("Percentile Graph") || rptGraphViewType.equals("Slab Count Graph") || rptGraphViewType.equals("Frequency Distribution Graph") || rptGraphViewType.equals("Derived Graph")))
        {
          for (msrNum = 0; msrNum < cmpRptMsrOptions.length; msrNum++)
          {
            testRunIndex = cmpRptMsrOptions[msrNum].testRunIndex;
            int tmpIdx = arrGraphDataIndex[msrNum];
            if (tmpIdx != -1)
            {
              arrMsrNames.add(cmpRptMsrOptions[msrNum].msrName);
            }
          }
          // Invalidate graph data index for next report
          for (msrNum = 0; msrNum < cmpRptMsrOptions.length; msrNum++)
          {
            testRunIndex = cmpRptMsrOptions[msrNum].testRunIndex;
            cmpRptTestRunData[testRunIndex].invalidateGraphDataIndex();
          }
          
          Log.reportDebugLog(className, "genCmpRptData", "", "", rptName + " is available in measurements: " + arrMsrNames.toString());

          timeSeriesCol = new TimeSeriesCollection[arrMsrNames.size()];
          timeSeries = new TimeSeries[arrMsrNames.size()];
          legendMsg = new String[arrMsrNames.size()];
          timeSeriesIndex = 0;

          // this is to select master avg data
          boolean flagSelectMasterData = false;
          String strMasterOverAllAvgData = "";
          strGraphDataType = "";

          // Generate data for (Simple, Multi & Tile) Reports, get all data from
          // (rtgMessage.dat file)
          for (msrNum = 0; msrNum < cmpRptMsrOptions.length; msrNum++)
          {
            String strOverAllAvgData = "NA";
            String strMaxData = "NA";

            // this is for diff columns
            if (msrNum > 0)
              strOverAllAvgData = "NA|NA|NA|NA";
            else
              strOverAllAvgData = "NA|NA";

            if (arrGraphDataIndex[msrNum] != -1)
            {
              testRunIndex = cmpRptMsrOptions[msrNum].testRunIndex;
              cmpRptTestRunData[testRunIndex].genCmpData(rptName, arrGraphDataIndex[msrNum], cmpRptMsrOptions[msrNum]);
              strOverAllAvgData = cmpRptTestRunData[testRunIndex].getOverAllAvgCmpData();
              strMaxData = cmpRptTestRunData[testRunIndex].getMaxDataValue();
              rptData = cmpRptTestRunData[testRunIndex].getRptData();

              // this is to set master avg data
              if (!flagSelectMasterData)
              {
                strMasterOverAllAvgData = strOverAllAvgData;
                flagSelectMasterData = true;
              }

              Log.reportDebugLog(className, "genCmpRptData", "", "", "Adding data and timeseries for report generation. MsrName = " + arrMsrNames.get(timeSeriesIndex) + ", Time interval = " + rptData.interval + "Avg Data = " + strOverAllAvgData + ", Test Run = " + cmpRptTestRunData[testRunIndex].getTestRun() + ", Graph data index = " + arrGraphDataIndex[msrNum]);
              timeSeriesCol[timeSeriesIndex] = new TimeSeriesCollection();

              int graphDataType = cmpRptTestRunData[testRunIndex].graphNames.getArrDataTypeIndx()[arrGraphDataIndex[msrNum]];
              String legendText = "Avg";

              if (graphDataType == GraphNameUtils.DATA_TYPE_CUMULATIVE)
                legendText = "Total";

              if (strGraphDataType.equals(""))
                strGraphDataType = "" + graphDataType;
              else
                strGraphDataType = strGraphDataType + "," + graphDataType;

              String timeSeriesName = arrMsrNames.get(timeSeriesIndex) + "";
              if (anlsFlag)
                timeSeriesName = timeSeriesName + "(TR" + cmpRptTestRunData[testRunIndex].getTestRun() + ")";

              timeSeries[timeSeriesIndex] = new TimeSeries(timeSeriesName + " (" + legendText + " = " + strOverAllAvgData + ")", Millisecond.class);
              legendMsg[timeSeriesIndex] = arrMsrNames.get(timeSeriesIndex) + " (" + legendText + " = " + strOverAllAvgData + ")";

              // reportGenerate.addDataToTimeSeriesAll(arrSampleData, timeSeries[timeSeriesIndex], timeSeriesCol[timeSeriesIndex], timeSeriesIndex, rptData.avgArrSeqNum, rptData.interval,
              // rptData.timeInMilli);
              reportGenerate.createDataSetForGraph(timeSeries[timeSeriesIndex], timeSeriesCol[timeSeriesIndex], cmpRptTestRunData[testRunIndex].reportDataUtilsObj);

              // to show diff data value
              if (msrNum > 0)
              {
                String tempDataValue = strOverAllAvgData;
                strOverAllAvgData = strOverAllAvgData + "|" + strMaxData;
                // add changed diff data value
                strOverAllAvgData = strOverAllAvgData + "|" + getDiffDataVal(strMasterOverAllAvgData, tempDataValue);

                // add changed diff data value in pct
                strOverAllAvgData = strOverAllAvgData + "|" + getDiffDataValInPct(strMasterOverAllAvgData, tempDataValue);
              }
              else
              {
        	strOverAllAvgData = strOverAllAvgData + "|" + strMaxData;
              }

              // This to keep the maximum of all time series;
              if (cmpRptTestRunData[testRunIndex].reportDataUtilsObj.getReportWidth() > width)
                width = cmpRptTestRunData[testRunIndex].reportDataUtilsObj.getReportWidth();
              if (rptData.heigth > heigth)
                heigth = rptData.heigth;
              if (rptData.endTimeInmilli > endTimeInmilli)
                endTimeInmilli = rptData.endTimeInmilli;

              timeSeriesIndex++;
            }
            cmpDataLine = cmpDataLine + "|" + strOverAllAvgData;
          }
        }
        
        /**
         * Ravi - Add code for percentile reports Generate data for Percentile Reports. get all data from (pctMessage.dat file)
         * 
         * if data is not available in pctMessage.dat, we need to create percentile graphs from rtgMessage.dat
         * 
         */
        else if (rptGraphViewType.equals("Percentile Graph") || rptGraphViewType.equals("Slab Count Graph") || rptGraphViewType.equals("Frequency Distribution Graph"))
        {
          try
          {
            Log.debugLog(className, "genCmpRptData", "", "", "Generating reprt for graphs type = " + rptGraphViewType);

            for (msrNum = 0; msrNum < cmpRptMsrOptions.length; msrNum++)
            {
              testRunIndex = cmpRptMsrOptions[msrNum].testRunIndex;
              int tmpPDFId = arrPDFId[msrNum];
              Log.reportDebugLog(className, "genCmpRptData", "", "", "testRunIndex = " + testRunIndex + ", tmpPDFId = " + tmpPDFId);
              arrMsrNames.add(cmpRptMsrOptions[msrNum].msrName);
            }

            if (cmpRptPCTData == null || percentileReportGenerator == null)
              genPCTDataFlag = true;
            
            for (msrNum = 0; msrNum < cmpRptMsrOptions.length; msrNum++)
            {
              testRunIndex = cmpRptMsrOptions[msrNum].testRunIndex;
              cmpRptTestRunData[testRunIndex].invalidateGraphDataIndex();
              Log.reportDebugLog(className, "genCmpRptData", "", "", "testRunIndex = " + testRunIndex);
            }
            
            Log.reportDebugLog(className, "genCmpRptData", "", "", rptName + " is available in measurements: " + arrMsrNames.toString());
            timeSeriesIndex = 0;
            
            /*This condition is for generating percentile data once*/
            if(isPercentileFirstTime && rptGraphViewType.equals("Percentile Graph"))
            {
              percentileReportGenerator = new PercentileReportGenerator[cmpRptMsrOptions.length];
              for (int i = 0; i < cmpRptMsrOptions.length; i++)
              {
        	testRunIndex = cmpRptMsrOptions[i].testRunIndex;
        	PercentileReportGenerator pctDataObj = new PercentileReportGenerator(cmpRptTestRunData[testRunIndex].getTestRun(), PercentileDataUtils.GRAPH_TYPE_PERCENTILE, 2, cmpRptTestRunData[testRunIndex].getRptGraphDataIdxArray(), cmpRptTestRunData[testRunIndex].getControllerTRNumber(), cmpRptTestRunData[testRunIndex].getGenName(), cmpRptMsrOptions[i].testRunStartDateTime, cmpRptMsrOptions[i].timeSelectionFormat, cmpRptMsrOptions[i].timeOption, cmpRptMsrOptions[i].startTime, cmpRptMsrOptions[i].endTime, cmpRptMsrOptions[i].startDate, cmpRptMsrOptions[i].endDate, cmpRptTestRunData[testRunIndex].graphNames, cmpRptTestRunData[testRunIndex].getRptData());
                pctDataObj.generatePercentileData();
                percentileReportGenerator[i] = pctDataObj;
              }
              isPercentileFirstTime = false;
            }
            
            /*This condition for slab count graph*/
            if(isSlabCountFirstTime && rptGraphViewType.equals("Slab Count Graph"))
            {
              slabReportGenerator = new PercentileReportGenerator[cmpRptMsrOptions.length];
              for (int i = 0; i < cmpRptMsrOptions.length; i++)
              {
        	testRunIndex = cmpRptMsrOptions[i].testRunIndex;
        	PercentileReportGenerator slabDataObj = new PercentileReportGenerator(cmpRptTestRunData[testRunIndex].getTestRun(), PercentileDataUtils.GRAPH_TYPE_SLAB_COUNT, 4, cmpRptTestRunData[testRunIndex].getRptGraphDataIdxArray(), cmpRptTestRunData[testRunIndex].getControllerTRNumber(), cmpRptTestRunData[testRunIndex].getGenName(), cmpRptMsrOptions[i].testRunStartDateTime, cmpRptMsrOptions[i].timeSelectionFormat, cmpRptMsrOptions[i].timeOption, cmpRptMsrOptions[i].startTime, cmpRptMsrOptions[i].endTime, cmpRptMsrOptions[i].startDate, cmpRptMsrOptions[i].endDate, cmpRptTestRunData[testRunIndex].graphNames, cmpRptTestRunData[testRunIndex].getRptData());
        	slabDataObj.generatePercentileData();
                slabReportGenerator[i] = slabDataObj;
              }
              isSlabCountFirstTime = false;
            }
            
            Log.reportDebugLog(className, "genCmpRptData", "", "", "genPCTDataFlag = " + genPCTDataFlag + ", rptGraphViewType = " + rptGraphViewType);

            if (rptGraphViewType.equals("Percentile Graph") || rptGraphViewType.equals("Frequency Distribution Graph"))
            {
              xySeriesCollectiondataset = new XYSeriesCollection[arrMsrNames.size()];
              xySeries = new XYSeries[arrMsrNames.size()];
              legendMsg = new String[arrMsrNames.size()];
              meanDataValue = new double[arrMsrNames.size()];
            }

            if (rptGraphViewType.equals("Slab Count Graph"))
            {
              defaultCategoryDataset = new DefaultCategoryDataset();
            }

            // this is to select master avg data
            boolean flagSelectMasterData = false;
            String strMasterOverAllAvgData = "";
            String tmpRptName = rptName;
            
            for (msrNum = 0; msrNum < cmpRptMsrOptions.length; msrNum++)
            {
              String strOverAllAvgDataPCT = "NA";

              if (msrNum > 0)
                strOverAllAvgDataPCT = "NA|NA|NA|NA";
              else
        	strOverAllAvgDataPCT = "NA|NA";

              testRunIndex = cmpRptMsrOptions[msrNum].testRunIndex;
              
              if (!(rptGraphViewType.equals("Percentile Graph")) && !rptGraphViewType.equals("Slab Count Graph"))//if (arrPDFId[msrNum] != -1)
              {
                if (genPCTDataFlag)
                {
                  int msrTestRun = Integer.parseInt((cmpRptMsrOptions[msrNum].testRun).trim());

                  cmpRptPCTData[msrNum] = new CmpRptPCTData(msrTestRun, cmpRptTestRunData[testRunIndex].graphNames.getInterval(), cmpRptMsrOptions[msrNum], cmpRptTestRunData[testRunIndex].getRptData(), cmpRptTestRunData[testRunIndex].graphNames.getPdfNames());

                  int[] tempArrayPDFId = new int[1];
                  int[] tempArrayPCTGraphDataIdx = new int[1];

                  tempArrayPDFId[0] = arrPDFId[msrNum];
                  tempArrayPCTGraphDataIdx[0] = arrPCTGraphDataIndex[msrNum];

                  cmpRptPCTData[msrNum].setArrayPDFId(tempArrayPDFId);
                  cmpRptPCTData[msrNum].setArrayPCTGraphDataIdx(tempArrayPCTGraphDataIdx);

                  cmpRptPCTData[msrNum].initPercentileData();
                }

                // get GraphPCTData
                GraphPCTData[] graphPCTData = cmpRptPCTData[msrNum].getGraphPCTData();

                rptData = cmpRptTestRunData[testRunIndex].getRptData();

                if (genPCTDataFlag)
                {
                  if (arrPCTGraphDataIndex[msrNum] < 0)
                  {
                    double[] arrRptData = rptUtilsBean.getDataFromArrayByIdx(cmpRptTestRunData[testRunIndex].getRptGraphDataIdxArray(), cmpRptTestRunData[testRunIndex].getArrayGraphData(), arrGraphDataIndex[msrNum]);
                    Log.debugLog(className, "genCmpRptData", "", "", "Array Report Data = " + rptUtilsBean.doubleArrayToList(arrRptData) + ", length = " + arrRptData.length);
                    ReportData tempRptData = new ReportData(cmpRptTestRunData[testRunIndex].getTestRun());
                    tempRptData.openRTGMsgFile();
                    tempRptData.calPktRange(cmpRptMsrOptions[msrNum].timeOption, cmpRptMsrOptions[msrNum].elapsedStartTime, cmpRptMsrOptions[msrNum].elapsedEndTime);
                    graphPCTData[0].calcAndSetCustomGrpData(arrRptData, (int) tempRptData.startSeq, (int) tempRptData.endSeq);
                    tempRptData.closeRTGMsgFile();
                  }
                }
                
                if (rptGraphViewType.equals("Frequency Distribution Graph"))
                {
                  double[] fdData = graphPCTData[0].getArrPCTCumDataBuckets();
                  double[] arrDataXAxis = new double[graphPCTData[0].numGranule];
                  double formulaUnitData = graphPCTData[0].getFormulaUnitData();
                  double convertXAxisData = 1;

                  if (formulaUnitData != 1)
                    convertXAxisData = graphPCTData[0].minGranule * formulaUnitData;

                  for (int ii = 0; ii < arrDataXAxis.length; ii++)
                  {
                    arrDataXAxis[ii] = (ii + 1) * convertXAxisData;
                  }

                  double[] arrAvgData = rptUtilsBean.getDataFromArrayByIdx(cmpRptTestRunData[testRunIndex].getRptGraphDataIdxArray(), cmpRptTestRunData[testRunIndex].getArrayGraphAvgData(), arrGraphDataIndex[msrNum]);
                  double[] arrStdDevData = rptUtilsBean.getDataFromArrayByIdx(cmpRptTestRunData[testRunIndex].getRptGraphDataIdxArray(), cmpRptTestRunData[testRunIndex].getArrayGraphStdDevData(), arrGraphDataIndex[msrNum]);
                  int graphDataType = cmpRptTestRunData[testRunIndex].graphNames.getArrDataTypeIndx()[arrGraphDataIndex[msrNum]];
                  graphPCTData[0].setMeanPctDataVal(rptData.getAvgDataBySequenceOnly(arrAvgData, graphDataType));
                  graphPCTData[0].setStdDevDataVal(rptData.getStdDevBySequenceOnly(arrStdDevData));
                  String[] tempDetail = rptUtilsBean.strToArrayData(rptName, "-");
                  String strLegend = tempDetail[tempDetail.length - 1].trim();

                  if (strLegend.equals("Mean"))
                  {
                    strOverAllAvgDataPCT = "" + graphPCTData[0].getMeanPctDataVal();

                    // to show mean line on graph
                    meanDataValue[timeSeriesIndex] = graphPCTData[0].getMeanPctDataValForMeanLine();
                  }

                  if (strLegend.equals("StdDev"))
                  {
                    meanDataValue = null;
                    strOverAllAvgDataPCT = "" + graphPCTData[0].getStdDevDataVal();
                  }

                  xySeriesCollectiondataset[timeSeriesIndex] = new XYSeriesCollection();

                  String timeSeriesName = arrMsrNames.get(timeSeriesIndex) + "";
                  if (anlsFlag) // for analysis series name
                    timeSeriesName = timeSeriesName + "(TR" + cmpRptTestRunData[testRunIndex].getTestRun() + ")";

                  xySeries[timeSeriesIndex] = new XYSeries(timeSeriesName + " (" + strLegend + " data value = " + strOverAllAvgDataPCT + " " + graphPCTData[0].getPDFUnit() + ")");

                  legendMsg[timeSeriesIndex] = arrMsrNames.get(timeSeriesIndex) + " (" + strLegend + " data value = " + strOverAllAvgDataPCT + " " + graphPCTData[0].getPDFUnit() + ")";

                  reportGenerate.addDataToXYSeriesAll(arrDataXAxis, fdData, xySeries[timeSeriesIndex], xySeriesCollectiondataset[timeSeriesIndex]);
                }

                // this is to set master avg data
                if (!flagSelectMasterData)
                {
                  strMasterOverAllAvgData = strOverAllAvgDataPCT;
                  flagSelectMasterData = true;
                }

                // to show diff data value
                if (msrNum > 0)
                {
                  String tempDataValue = strOverAllAvgDataPCT;
                  strOverAllAvgDataPCT =  strOverAllAvgDataPCT + "|" + "NA";
                  strOverAllAvgDataPCT = strOverAllAvgDataPCT + "|" + getDiffDataVal(strMasterOverAllAvgData, tempDataValue);
                  // add changed diff data value in pct
                  strOverAllAvgDataPCT = strOverAllAvgDataPCT + "|" + getDiffDataValInPct(strMasterOverAllAvgData, tempDataValue);
                }
                else
                {
                  strOverAllAvgDataPCT = strOverAllAvgDataPCT + "|" + "NA";
                }

                // This to keep the maximum of all time series;
                if (cmpRptTestRunData[testRunIndex].reportDataUtilsObj.getReportWidth() > width)
                  width = cmpRptTestRunData[testRunIndex].reportDataUtilsObj.getReportWidth();
                if (rptData.heigth > heigth)
                  heigth = rptData.heigth;

                timeSeriesIndex++;
              }
              else
              {
                try
                {
                  Log.reportDebugLog(className, "genCmpRptData", "", "", "Generating percentile graph from DerivedDataProcessor,  for TestRun = " + cmpRptMsrOptions[msrNum].testRun);
                  
                  rptData = cmpRptTestRunData[testRunIndex].getRptData();
                  Log.reportDebugLog(className, "genCmpRptData", "", "", "graphDataIndexArray = " + rptUtilsBean.intArrayToStr(arrGraphDataIndex, ",") + ",testrunDataIndex = " + rptUtilsBean.intArrayToStr(cmpRptTestRunData[testRunIndex].getAllUniqueGraphDataIndex(), ","));
                  GraphUniqueKeyDTO graphUniqueKeyDTO = cmpRptTestRunData[testRunIndex].graphNames.getGraphUniqueKeyDTOByGraphDataIndex(arrGraphDataIndex[0]);
                  
                  if (rptGraphViewType.equals("Percentile Graph"))
                  {
                    PercentileReportGenerator pctDataObj = percentileReportGenerator[msrNum];
                    double[] percentileGraphData = pctDataObj.getPercentileData(graphUniqueKeyDTO);
                    Log.reportDebugLog(className, "genCmpRptData", "", "", "percentileGraphData = " + rptUtilsBean.doubleArrayToList(percentileGraphData));
                    double[] arrDataXAxis = new double[100];
                    for (int ii = 0; ii < 100; ii++)
                    {
                      arrDataXAxis[ii] = (ii + 1);
                    }

                    String[] tempDetail = rptUtilsBean.strToArrayData(rptName, "-");
                    tempDetail = rptUtilsBean.strToArrayData(tempDetail[tempDetail.length - 1], "th");
                    int pctIndex = Integer.parseInt(tempDetail[0].trim());
                    double nthPercentile = percentileGraphData[pctIndex];

                    strOverAllAvgDataPCT = "" + nthPercentile;

                    Log.reportDebugLog(className, "genCmpRptData", "", "", "pctIndex = " + pctIndex + ", nthPercentile = " + nthPercentile);
                    xySeriesCollectiondataset[timeSeriesIndex] = new XYSeriesCollection();
                    String timeSeriesName = arrMsrNames.get(timeSeriesIndex) + "";

                    if (anlsFlag)
                      timeSeriesName = timeSeriesName + "(TR" + cmpRptTestRunData[testRunIndex].getTestRun() + ")";

                    xySeries[timeSeriesIndex] = new XYSeries(timeSeriesName + " (" + pctIndex + "th Percentile data = " + strOverAllAvgDataPCT + " " + pctDataObj.formulaUnit + ")");
                    legendMsg[timeSeriesIndex] = arrMsrNames.get(timeSeriesIndex) + " (" + pctIndex + "th Percentile data = " + strOverAllAvgDataPCT + " " + pctDataObj.formulaUnit + ")";
                    reportGenerate.addDataToXYSeriesAll(arrDataXAxis, percentileGraphData, xySeries[timeSeriesIndex], xySeriesCollectiondataset[timeSeriesIndex]);
                  }
                  else if (rptGraphViewType.equals("Slab Count Graph"))
                  {
                    PercentileReportGenerator slabDataObj = slabReportGenerator[msrNum];
                    double[] slabData = slabDataObj.getPercentileData(graphUniqueKeyDTO);
                    if(slabData == null)
                      continue;
                    
                    /*we are overriding it to its original value*/
                    rptName = tmpRptName;
                    String[] slabInfo = slabDataObj.getSlabInfo(graphUniqueKeyDTO);
                    int slabIndex = slabDataObj.getSlabIndex(arrPDFId[msrNum], rptName.trim(), slabInfo);
                    rptName = slabDataObj.getSlabRptName(arrPDFId[msrNum], rptName.trim(), slabInfo[slabIndex]);
                    if (slabIndex != -1)
                    {
                      strOverAllAvgDataPCT = "" + slabData[slabIndex];
                    }
                    
                    String strSeries = arrMsrNames.get(timeSeriesIndex) + "";
                    for (int ii = 0; ii < slabInfo.length; ii++)
                    {
                      String strCategory = slabInfo[ii];
                      defaultCategoryDataset.addValue(slabData[ii], strSeries, strCategory);
                    }
                  }
                  
                  // this is to set master avg data
                  if (!flagSelectMasterData)
                  {
                    strMasterOverAllAvgData = strOverAllAvgDataPCT ;
                    flagSelectMasterData = true;
                  }

                  // to show diff data value
                  if (msrNum > 0)
                  {
                    String tempDataValue = strOverAllAvgDataPCT;
                    strOverAllAvgDataPCT = strOverAllAvgDataPCT + "|" + "NA";
                    strOverAllAvgDataPCT = strOverAllAvgDataPCT + "|" + getDiffDataVal(strMasterOverAllAvgData, tempDataValue);
                    // add changed diff data value in pct
                    strOverAllAvgDataPCT = strOverAllAvgDataPCT + "|" + getDiffDataValInPct(strMasterOverAllAvgData, tempDataValue);
                  }
                  else
                  {
                    strOverAllAvgDataPCT = strOverAllAvgDataPCT + "|" + "NA";
                  }

                  // This to keep the maximum of all time series;
                  if (rptData.width > width)
                    width = rptData.width;
                  if (rptData.heigth > heigth)
                    heigth = rptData.heigth;
                  if (rptData.endTimeInmilli > endTimeInmilli)
                    endTimeInmilli = rptData.endTimeInmilli;

                  timeSeriesIndex++;
                }
                catch (Exception ex)
                {
                  ex.printStackTrace();
                  Log.stackTraceLog(className, "genCmpRptData", "", "", "Exception - ", ex);
                }
              }

              cmpDataLine = cmpDataLine + "|" + strOverAllAvgDataPCT;
            }
          }
          catch (Exception ex)
          {
            Log.stackTraceLog(className, "genCmpRptData", "", "", "Exception - ", ex);
          }
        }
        else if (rptGraphViewType.equals("Derived Graph"))
        {
          try
          {
            Log.debugLog(className, "genCmpRptData", "", "", "Generating reprt for derived graphs....");

            String derivedExpression = rptTemplateDetail;
            boolean[] arrCompatibleFlag = new boolean[cmpRptMsrOptions.length];
            Arrays.fill(arrCompatibleFlag, false);
            for (msrNum = 0; msrNum < cmpRptMsrOptions.length; msrNum++)
            {
              testRunIndex = cmpRptMsrOptions[msrNum].testRunIndex;
              cmpRptTestRunData[testRunIndex].setDerivedDataObj(derivedExpression);
              if (cmpRptTestRunData[testRunIndex].derivedData.chkExpIsCompatible(cmpRptTestRunData[testRunIndex].graphNames))
              {
                arrCompatibleFlag[msrNum] = true;
                arrMsrNames.add(cmpRptMsrOptions[msrNum].msrName);
              }
              else
              {
        	Log.errorLog(className, "genCmpRptData", "", "", "Derived Graph is not available for Formula = " + derivedExpression  + ".");
              }
            }
            
            Log.reportDebugLog(className, "genCmpRptData", "", "", rptName + " is available in measurements: " + arrMsrNames.toString());

            timeSeriesCol = new TimeSeriesCollection[arrMsrNames.size()];
            timeSeries = new TimeSeries[arrMsrNames.size()];
            legendMsg = new String[arrMsrNames.size()];
            timeSeriesIndex = 0;

            boolean flagSelectMasterData = false;
            String strMasterOverAllAvgData = "";
            strGraphDataType = "";

            for (msrNum = 0; msrNum < cmpRptMsrOptions.length; msrNum++)
            {
              String strOverAllAvgData = "NA";
              String strMaxData = "NA";

              if (msrNum > 0)
                strOverAllAvgData = "NA|NA|NA|NA";
              else
                strOverAllAvgData = "NA|NA";
              
              testRunIndex = cmpRptMsrOptions[msrNum].testRunIndex;

              if (arrCompatibleFlag[msrNum])
              {
                // double[] arrDerivedData = cmpRptTestRunData[testRunIndex].derivedData.calcAndGetDerivedDataArray(cmpRptTestRunData[testRunIndex].getArrayGraphData(),
                // cmpRptTestRunData[testRunIndex].getRptGraphDataIdxArray(), cmpRptTestRunData[testRunIndex].graphNames);

                cmpRptTestRunData[testRunIndex].genCmpDataForDerived(null, rptName, derivedExpression, cmpRptMsrOptions[msrNum]);
                strOverAllAvgData = cmpRptTestRunData[testRunIndex].getOverAllAvgCmpData();
                
                strMaxData = cmpRptTestRunData[testRunIndex].getMaxDataValue();
                rptData = cmpRptTestRunData[testRunIndex].getRptData();
                if (!flagSelectMasterData)
                {
                  strMasterOverAllAvgData = strOverAllAvgData;
                  flagSelectMasterData = true;
                }

                timeSeriesCol[timeSeriesIndex] = new TimeSeriesCollection();

                String legendText = "Avg";
                int derivedExpDataType = cmpRptTestRunData[testRunIndex].getGraphDataType();
                if (derivedExpDataType == GraphNameUtils.DATA_TYPE_CUMULATIVE)
                  legendText = " (Total = ";

                if (strGraphDataType.equals(""))
                  strGraphDataType = "" + derivedExpDataType;
                else
                  strGraphDataType = strGraphDataType + "," + derivedExpDataType;

                String timeSeriesName = arrMsrNames.get(timeSeriesIndex) + "";
                if (anlsFlag)
                  timeSeriesName = timeSeriesName + "(TR" + cmpRptTestRunData[testRunIndex].getTestRun() + ")";

                timeSeries[timeSeriesIndex] = new TimeSeries(timeSeriesName + " (" + legendText + " = " + strOverAllAvgData + ")", Millisecond.class);
                legendMsg[timeSeriesIndex] = arrMsrNames.get(timeSeriesIndex) + " (" + legendText + " = " + strOverAllAvgData + ")";
                // reportGenerate.addDataToTimeSeriesAll(rptData.arrDataVal, timeSeries[timeSeriesIndex], timeSeriesCol[timeSeriesIndex], timeSeriesIndex, rptData.avgArrSeqNum, rptData.interval,
                // rptData.timeInMilli);
                reportGenerate.createDataSetForGraph(timeSeries[timeSeriesIndex], timeSeriesCol[timeSeriesIndex], cmpRptTestRunData[testRunIndex].reportDataUtilsObj);

                if (msrNum > 0)
                {
                  String tempDataValue = strOverAllAvgData;
                  strOverAllAvgData = strOverAllAvgData + "|" + strMaxData;
                  strOverAllAvgData = strOverAllAvgData + "|" + getDiffDataVal(strMasterOverAllAvgData, tempDataValue);
                  strOverAllAvgData = strOverAllAvgData + "|" + getDiffDataValInPct(strMasterOverAllAvgData, tempDataValue);
                }
                else
                {
                  strOverAllAvgData = strOverAllAvgData + "|" + strMaxData;
                }

                if (cmpRptTestRunData[testRunIndex].reportDataUtilsObj.getReportWidth() > width)
                  width = cmpRptTestRunData[testRunIndex].reportDataUtilsObj.getReportWidth();
                if (rptData.heigth > heigth)
                  heigth = rptData.heigth;
                if (rptData.endTimeInmilli > endTimeInmilli)
                  endTimeInmilli = rptData.endTimeInmilli;

                timeSeriesIndex++;
              }
              
              cmpDataLine = cmpDataLine + "|" + strOverAllAvgData;
            }
          }
          catch (Exception ex)
          {
            ex.printStackTrace();
            Log.stackTraceLog(className, "genCmpRptData", "", "", "Exception - ", ex);
          }
        }

        // Now generate chart
        String graphFileName = rptUtilsBean.doReplaceName(rptName);
        String[] arrStrMsrNames = (String[]) arrMsrNames.toArray(new String[arrMsrNames.size()]);
        Object retObject = null;
        String fileName = "";
        JFreeChart chartObj = null;
        if (arrStrMsrNames.length > 0)
        {
          if (rptGraphViewType.equals("Percentile Graph"))
          {
            retObject = reportGenerate.generatePercentileChart("", graphFileName, xySeriesCollectiondataset, arrStrMsrNames, width, heigth, reportSetName, rptFilePath, legendMsg, anlsFlag);
          }
          else if (rptGraphViewType.equals("Slab Count Graph"))
          {
            Paint[] barColors = new Paint[] { NSColor.lineColor(0), NSColor.lineColor(1), NSColor.lineColor(2), NSColor.lineColor(3), NSColor.lineColor(4), NSColor.lineColor(5), NSColor.lineColor(6), NSColor.lineColor(7), NSColor.lineColor(8), NSColor.lineColor(9), NSColor.lineColor(10), NSColor.lineColor(11), NSColor.lineColor(12), NSColor.lineColor(13), NSColor.lineColor(14), NSColor.lineColor(15), NSColor.lineColor(16), NSColor.lineColor(17), NSColor.lineColor(18), NSColor.lineColor(19), NSColor.lineColor(20), NSColor.lineColor(21), NSColor.lineColor(22), NSColor.lineColor(23), NSColor.lineColor(24), NSColor.lineColor(25), NSColor.lineColor(26), NSColor.lineColor(27), NSColor.lineColor(28), NSColor.lineColor(29), NSColor.lineColor(30), NSColor.lineColor(31), NSColor.lineColor(32) };

            retObject = reportGenerate.generateSlabChart("", graphFileName, defaultCategoryDataset, arrStrMsrNames, barColors, width, heigth, reportSetName, rptFilePath, anlsFlag);
          }
          else if (rptGraphViewType.equals("Frequency Distribution Graph"))
          {
            retObject = reportGenerate.generateFrequencyDist("", graphFileName, xySeriesCollectiondataset, arrStrMsrNames, meanDataValue, width, heigth, reportSetName, rptFilePath, legendMsg, anlsFlag);
          }
          else
          // for simple, multi & tile graph view type, this is also work for
          // derived reports
          {
            retObject = reportGenerate.generateChart("", graphFileName, timeSeriesCol, arrStrMsrNames, cmpRptMsrOptions[0].graphType, reportSetName, cmpRptMsrOptions[0].xAxisTimeFormat, width, heigth, rptData.startTimeInmilli, endTimeInmilli, rptFilePath, legendMsg, anlsFlag);
          }
        }

        if (retObject instanceof String)
          fileName = (String) retObject;
        else
          chartObj = (JFreeChart) retObject;

        if ((!fileName.equals("")) || (chartObj != null)) // Successful
        {
          // Currently we putting template data of one template as template data
          // in control file is not used.
          // Need to change this later.
          String arrRptFields[] = arrRtplRecs[2]; // Hdr Data of the tempate
          arrRptFields[0] = rptName;

          result = true;
          cmpDataLine = rptGrpName + "|" + rptName + "|NA|0|0|Success" + cmpDataLine; // cmpDataLine
          // starts
          // with
          // pipe
          // so
          // do
          // not
          // add
          // pipe
          // after
          // +

          Log.reportDebugLog(className, "genCmpRptData", "", "", "Adding report in report set control file.");

          if (!anlsFlag)
          {
            // pass updated record array to add in control file
            if (reportSetControl.addReport(reportSetName, testRun, arrRptFields) == false)
              reportStatusLog(reportSetName, numTestRun, "genCmpRptData", "", "", "Error: Error in adding report in report set control file. Continuing to next report.");
          }
        }
        else
        {
          result = false;
          
          if(rptName == null || rptName.equals("NA") || rptName.length() == 0)
            continue;
          
          cmpDataLine = rptGrpName + "|" + rptName + "|NA|0|0|NA" + cmpDataLine; // cmpDataLine
          // starts
          // with
          // pipe
          // so
          // do
          // not
          // add
          // pipe
          // after
          // +
        }

        if (addCmpData(cmpRptUtils, cmpDataLine) == false)
          return false;

        if (anlsFlag) // for Analysis store all data in anlsCmpRptData object
        {
          Log.reportDebugLog(className, "genCmpRptData", "", "", "Anls flag found.");

          boolean addCmpDataForSlabFlag = true;
          if (rptGraphViewType.equals("Slab Count Graph"))
          {
            // if prev graph type is same thn need to chk pct index
            if (rptPrevGraphViewType.equals(rptGraphViewType))
            {
              // if prev pdf id not equal to null means not on first time
              if (arrPrevPCTGraphDataIndex != null)
              {
                // if prev pdf id length and curr pdf id length equal, then chk
                // its values
                if (arrPrevPCTGraphDataIndex.length == arrPCTGraphDataIndex.length)
                {
                  for (int jj = 0; jj < arrPrevPCTGraphDataIndex.length; jj++)
                  {
                    if (arrPrevPCTGraphDataIndex[jj] != arrPCTGraphDataIndex[jj])
                    {
                      addCmpDataForSlabFlag = true;
                      break;
                    }
                    else
                      // if both array are same then no need to add data
                      addCmpDataForSlabFlag = false;
                  }
                }
                else
                  // if length are diff then add data
                  addCmpDataForSlabFlag = true;
              }
              else
                // if prev array is null then add data
                addCmpDataForSlabFlag = true;
            }
          }

          Log.reportDebugLog(className, "genCmpRptData", "", "", "Value of addCmpDataForSlabFlag = " + addCmpDataForSlabFlag + ", for report name = " + rptName);

          if (addCmpDataForSlabFlag) // this is for analysis to add cmp data for
          // slab graph only one time
          {
            anlsCmpRptData.addChartToList((Object) chartObj);
            anlsCmpRptData.addRptGrpIdsToList("" + rptGroupID);
            anlsCmpRptData.addRptIdsToList("" + rptGraphID);
            anlsCmpRptData.addPDFIdsToList(rptUtilsBean.intArrayToStr(arrPDFId, ","));
            anlsCmpRptData.addRptDataIdxToList(rptUtilsBean.intArrayToStr(arrGraphDataIndex, ","));
            anlsCmpRptData.addPCTGraphDataIdxToList(rptUtilsBean.intArrayToStr(arrPCTGraphDataIndex, ","));

            anlsCmpRptData.addGraphDataTypeToList(strGraphDataType);

            anlsCmpRptData.addRptNameToList(rptName);
            anlsCmpRptData.addRptGroupNameToList(rptGrpName);
            anlsCmpRptData.addRptGraphViewTypeToList(rptGraphViewType);
            anlsCmpRptData.addRptTemplateDetailToList(rptTemplateDetail);
          }
        }

        if (rptGraphViewType.equals("Percentile Graph") || rptGraphViewType.equals("Slab Count Graph") || rptGraphViewType.equals("Frequency Distribution Graph"))
        {
          rptPrevGraphID = rptGraphID;
          rptPrevGroupID = rptGroupID;
          rptPrevGraphViewType = rptGraphViewType;
          arrPrevPCTGraphDataIndex = arrPCTGraphDataIndex;
        }

        timeSeriesCol = null;
        timeSeries = null;
        xySeriesCollectiondataset = null;
        xySeries = null;
        defaultCategoryDataset = null;
        if(rptData != null)
        {
          rptData.startTimeInmilli = 0; // Check later if we need to do this
          rptData.endTimeInmilli = 0;
        }
        logEndRptGen(result);
      }
      cmpRptUtils.closeCmpDataFile(); // file stream must be closed
      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "genCmpRptData", "", "", "Error in compare report generation", e);
      reportStatusLog(reportSetName, numTestRun, "genCmpRptData", "", "", "Error: Error in compare report generation due to exception - " + e);
      logEndRptGen(false);
      return false;
    }
  }
  
  // this function return the reportdetail index in vector by report name
  private int getReportDetailIndexByReportName(String strGrpNameRptName)
  {
    Log.reportDebugLog(className, "getReportDetailIndexByReportName", "", "", "Method called. Report Name = " + strGrpNameRptName);

    try
    {
      int indexRptDetail = -1;
      for (int i = 0; i < vecReportDetails.size(); i++)
      {
        String[] strTemp = rptUtilsBean.split(vecReportDetails.get(i).toString(), "|");

        String tempStr = strTemp[4] + "|" + strTemp[5];
        if (strGrpNameRptName.equals(tempStr))
        {
          indexRptDetail = i;
          break;
        }
      }

      Log.reportDebugLog(className, "getReportDetailIndexByReportName", "", "", "Report Data = " + vecReportDetails.get(indexRptDetail).toString());

      return indexRptDetail;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getReportDetailIndexByReportName", "", "", "Error in getting index", e);
      return -1;
    }
  }

  // this function return the diff of data value with respect to master data(up
  // to 3 decimal)
  private String getDiffDataVal(String strMasterOverAllAvgData, String strOverAllAvgData)
  {
    Log.reportDebugLog(className, "getDiffDataVal", "", "", "Method called. Master data value = " + strMasterOverAllAvgData + ", Average data value = " + strOverAllAvgData);

    try
    {
      double masterDataValue = Double.parseDouble(strMasterOverAllAvgData);
      double avgDataValue = Double.parseDouble(strOverAllAvgData);

      String diffDataValue = fmtDouble(avgDataValue - masterDataValue);

      Log.reportDebugLog(className, "getDiffDataVal", "", "", "Diff data value = " + diffDataValue);

      return diffDataValue;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getDiffDataVal", "", "", "Error in calculatin diff data value", e);
      return "NA";
    }
  }

  // this function return the diff of data value with respect to master
  // data(round off %)
  private String getDiffDataValInPct(String strMasterOverAllAvgData, String strOverAllAvgData)
  {
    Log.reportDebugLog(className, "getDiffDataValInPct", "", "", "Method called. Master data value = " + strMasterOverAllAvgData + ", Average data value = " + strOverAllAvgData);

    try
    {
      double masterDataValue = Double.parseDouble(strMasterOverAllAvgData);
      double avgDataValue = Double.parseDouble(strOverAllAvgData);

      double diffDataValue = (avgDataValue - masterDataValue);

      String strDiffDataValueInPct = "NA";
      if (masterDataValue > 0)
      {
        int diffDataValueInPct = (int) ((diffDataValue / masterDataValue) * 100);
        strDiffDataValueInPct = "" + diffDataValueInPct;
      }
      else
      {
        if (diffDataValue == 0)
          strDiffDataValueInPct = "0";
        else
          strDiffDataValueInPct = "100";
      }

      Log.reportDebugLog(className, "getDiffDataValInPct", "", "", "Diff data value in pecentage = " + strDiffDataValueInPct);

      return strDiffDataValueInPct;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getDiffDataValInPct", "", "", "Error in calculatin diff data value", e);
      return "NA";
    }
  }

  public void setNetCloudValue(String netCloudValue)
  {
    Log.reportDebugLog(className, "setNetCloudValue", "", "", "Method called setting netCloudValue : " + netCloudValue);
    try
    {
      if (netCloudValue.trim().equals("1"))
        isNetCloudTest = true;
      else
        isNetCloudTest = false;
    }
    catch (Exception e)
    {
      isNetCloudTest = false;
    }

  }

  // Convert the double value according to the data value
  // >100 no need to show in decimal
  // >10 then format like "###.#" format
  // >1 then format like "###.##" format
  // <1 then format like "###.###" format
  public String fmtDouble(double val)
  {
    String fmt = "###.###";
    double dataValue = val;

    // to put format only on dataValue
    if (val < 0)
      dataValue = (-1) * val;

    if (dataValue > 100)
      fmt = "###";
    else
    {
      if (dataValue > 10)
        fmt = "###.#";
      else
      {
        if (dataValue > 1)
          fmt = "###.##";
        else
          fmt = "###.###";
      }
    }

    Log.debugLog(className, "fmtDouble", "", "", "Format of data = " + fmt);

    DecimalFormat frmt = new DecimalFormat(fmt);
    return frmt.format(val);
  }

  // add compare data in file or memory based on flag
  private boolean addCmpData(CmpRptUtils cmpRptUtils, String cmpRptDataLine)
  {
    Log.reportDebugLog(className, "addCmpData", "", "", "Method called, data line = " + cmpRptDataLine);

    if (!anlsFlag)
    {
      return (cmpRptUtils.addCmpData(cmpRptDataLine));
    }
    else
    {
      return (anlsCmpRptData.addCmpDataToList(cmpRptDataLine));
    }
  }

  // to get the AnlsCmpRptData object
  public AnlsCmpRptData getAnlsCmpRptData()
  {
    return anlsCmpRptData;
  }

  private static void usage(String error)
  {
    System.out.println(error);
    System.out.println("Usage: javac CmpRptUsingRtpl with following arguments");
    System.out.println("\t --testRun (or) -t  <Test Run Number where compare report set is created>");
    System.out.println("\t --template (or) -T <Report Template Name>");
    System.out.println("\t --reportSet (or) -r <Report Set Name>");
    System.out.println("\t --msrFile or -f <msrDataFileName> - File containing all Measuremet data");
    System.out.println("\t  OR");
    System.out.println("\t --msrData or -D <One or more Measuremet data>");
    System.out.println("\t  Where measurement data in the argument or file should be in the following format:");

    System.out.println("\t  MsrName|TestRun|TestRunStartDateTime|Override|XAxisTimeFormat|TimeOption|TimeFormat|StartDate|StartTime|EndDate|EndTime");
    System.out.println("\t    MsrName is measurement name (no space allowed)");
    System.out.println("\t    TestRunStartDateTime is Test Run start date/time in MM/DD/YYYY HH:MM:SS format");
    System.out.println("\t    Override is always 1");
    System.out.println("\t    XAxisTimeFormat is always Elapsed");
    System.out.println("\t    TimeOption is 'Total Run', 'Run Phase Only' or 'Specified Time'. Next fields are required for Specified Time. For other, use NA");
    System.out.println("\t    TimeFormat is 'Elasped' or 'Absolute'");
    System.out.println("\t    StartDate is start date in MM/DD/YYYY for the measurement for 'Absolute' else 'NA'");
    System.out.println("\t    StartTime is start time in HH:MM:SS for the measurement");
    System.out.println("\t    EndDate is end date in MM/DD/YYYY for the measurement for 'Absolute' else 'NA'");
    System.out.println("\t    --netCloud or -n <0/1> - 1 if netcloud test else 0");
    System.out.println("\t    --debugLevel or -d <0/1/2/3/4>");

    System.exit(1);

  }

  /**
   * 
   * @param errMsg
   * @param args
   * @return
   */
  private boolean parseOption(StringBuffer errMsg, String[] args)
  {
    try
    {
      return true;
    }
    catch (Exception ex)
    {
      errMsg.append("Exception occured in parsing options " + ex);
      return false;
    }
  }

  

  // ---------------------------------------------------------------------------------------
  // Purpose: Generate report set. Called from Shell programs
  // Arguments: See usage
  // DO NOT use main method for unit testing of this bean.

  // ---------------------------------------------------------------------------------------

  /*
   * Ravi - Please use argument as given below example MsrName|TestRun|Test Run StartDate/Time|OverrideTemplateOptions(0/1)|xAxisTimeFormat|timeOption|
   * timeSelectionFormat|startDate|startTime|endDate|endTime
   * 
   * Example:
   * 
   * M2|1234|7/13/13 10:10:17|1|Elapsed|Total Run|Elapsed|NA|NA|NA|NA M2|1234|7/13/13 10:10:17|1|Elapsed|Specified Time|Elapsed|NA|00:10:00|NA|00:50:00
   */

  public static void main(String[] args)
  {
    if (args.length < 5)
      usage("Invalid number of arguments");

    try
    {
      StringBuffer errMsg = new StringBuffer();
      CmpRptUsingRtpl cmpRptUsingRtpl = new CmpRptUsingRtpl();

      if (!cmpRptUsingRtpl.parseOption(errMsg, args))
      {
        usage(errMsg.toString());
      }
    }
    catch (Exception e)
    {
      System.out.println("Error: Error in generating compare report set due to exception.");
      System.out.println("See $NSWDIR/webapps/netstorm/logs/guiError.log and $NSWDIR/webapps/netstorm/logs/reportDebug.log for more details");
      System.exit(-1);
    }
  }

  /*
   * public static void main(String[] args) { CmpRptUsingRtpl cmpRptUsingRtpl = new CmpRptUsingRtpl("1023", false, true);
   * 
   * String rtplName = "_VuserReports"; String reportSetName = "AnalysisCompare"; String[] msrData = newString[]{ "Msr_name2|1023|2/13/08 12:34:25|0|Elapsed|Total Run|Elapsed|NA|NA|NA|NA",
   * "Msr_name2|1023|2/13/08 12:34:25|0|Elapsed|Specified Time|Elapsed|NA|00:01:00|NA|00:02:00" };
   * 
   * if(cmpRptUsingRtpl.genCmpRptSet(rtplName, reportSetName, msrData) == false) System.out.println("Error in Compare report set generation."); else {
   * System.out.println("Compare report set generated successfully."); AnlsCmpRptData anlsCmpRptData = cmpRptUsingRtpl.getAnlsCmpRptData();
   * 
   * ArrayList cmpData = anlsCmpRptData.getCmpDataList();
   * 
   * System.out.println("data length = " + cmpData.size()); for(int i = 0; i < cmpData.size(); i++) System.out.println("data line = " + cmpData.get(i).toString()); } }
   */
}

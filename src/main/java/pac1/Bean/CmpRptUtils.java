/*--------------------------------------------------------------------
@Name    : CmpRptUtils.java
@Author  : Prabhat
@Purpose : Bean for Create "compare.report" file in TRxxx and file manipulation
@Modification History:
    07/17/07 : Prabhat --> Initial Version


----------------------------------------------------------------------*/
package pac1.Bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pac1.Bean.GraphName.GraphNames;

public class CmpRptUtils
{
  private static String className = "CmpRptUtils";
  private String workPath = Config.getWorkPath();
  private final String COMP_RPT_FILE = "compare";
  private final String COMP_RPT_FILE_EXTN = ".report";
  private final String MSR_RPT_FILE = "msr_info";
  private final String MSR_RPT_FILE_EXTN = ".dat";
  private String testRun = "";
  private int numTestRun = -1;
  private int totalAvailReports = 0; // it stores actual length of number of report

  private FileOutputStream fosCmpDataFile = null;
  private PrintStream psCmpDataFile = null;

  // Header line -> "Report Group Name|Report Name|Sort Order|Hide|Highlight|Status|Future1|Future2|Future3|Future4"
  private final int HIDE_FIELD_INDEX = 3; // hide field index in cmp_data
  private final int HIGHLIGHT_FIELD_INDEX = 4; // highlight field index in cmp_data
  private final int STATUS_FIELD_INDEX = 5; // Status field index in cmp_data
  private final int CMP_RPT_TYPE = 6; // Compare Report Type field index in cmp_data

  private Vector vecRptRowId = null;

  // Constructor - Used from JSP
  // strSessionId is not used now.
  public CmpRptUtils(String testRun, String strSessionId)
  {
    this.testRun = testRun;
    numTestRun = Integer.parseInt(testRun);
  }

  // This will return the reportset Base path
  private String getReportSetBasePath(int numTestRun)
  {
    return (workPath + "/webapps/logs/TR" + numTestRun + "/reports/reportSet");
  }

  // This will return the reportset path
  private String getReportSetPath(String reportSetName, int numTestRun)
  {
    return (getReportSetBasePath(numTestRun) + "/" + reportSetName);
  }

  // Open msr_report file and return as File object
  private File openMsrFile(String cmpRptFileName)
  {
    Log.debugLog(className, "openMsrFile", "", "", "Open Compare Report file.");
    try
    {
      File msrDataFile = new File((workPath + "/webapps/logs/TR" + testRun + "/"), cmpRptFileName);
      return (msrDataFile);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "openMsrFile", "", "", "Exception - ", e);
      return null;
    }
  }

  // Add Data to "msr_report.txt" File
  public boolean addMsrData(String msrRecords)
  {
    Log.debugLog(className, "addMsrData", "", "", "Adding record to Compare Report file.");
    String cmpRptFileWithPath = "";
    cmpRptFileWithPath = workPath + "/webapps/logs/TR" + testRun + "/" + MSR_RPT_FILE + MSR_RPT_FILE_EXTN;
    try
    {
      // Read file's content
      File msrDataFile = openMsrFile(MSR_RPT_FILE + MSR_RPT_FILE_EXTN);
      if (!msrDataFile.exists())
        msrDataFile.createNewFile();

      FileOutputStream fout = new FileOutputStream(msrDataFile, true); // append mode
      PrintStream pw = new PrintStream(fout);

      pw.println(msrRecords); // Append the new record

      pw.close();
      fout.close();

      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "addCmpData", "", "", "Exception in adding record in Compare Report file (" + cmpRptFileWithPath + ") - ", e);
      return false;
    }
  }

  // Delete the Msr Record from File "msr_report.txt"
  public boolean deleteMsrData(String msrRecord)
  {
    Log.debugLog(className, "deleteMsrData", "", "", "The record to be deleted =" + msrRecord);
    String cmpRptFileWithPath = "";

    cmpRptFileWithPath = workPath + "/webapps/logs/TR" + testRun + "/" + MSR_RPT_FILE + MSR_RPT_FILE_EXTN;
    try
    {
      // Read file's content
      File msrDataFile = openMsrFile(MSR_RPT_FILE + MSR_RPT_FILE_EXTN);
      if (msrDataFile == null)
        return false;

      Vector vecCmpReport = readReport(cmpRptFileWithPath);
      if (vecCmpReport == null)
      {
        Log.errorLog(className, "deleteMsrData", "", "", "No Record exist in Compare Report file.");
        return false;
      }
      boolean success = (new File(cmpRptFileWithPath)).delete();
      if (!success)
      {
        Log.errorLog(className, "deleteMsrData", "", "", "Error in deleting Compare Report file (" + cmpRptFileWithPath + ")");
        return false;
      }

      msrDataFile.createNewFile();
      FileOutputStream fout = new FileOutputStream(msrDataFile, true); // append mode
      PrintStream pw = new PrintStream(fout);

      for (int i = 0; i < vecCmpReport.size(); i++)
      {
        String[] strCmpReportRcrd = rptUtilsBean.strToArrayData(vecCmpReport.elementAt(i).toString(), "|");
        if (strCmpReportRcrd[0].equals(msrRecord))
          continue;
        pw.println(vecCmpReport.elementAt(i).toString());
      }
      pw.close();
      fout.close();

      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "deleteMsrData", "", "", "Exception in deleting record from Compare Report file (" + cmpRptFileWithPath + ") - ", e);
      return false;
    }
  }

  // Methods for reading the File
  private Vector readReport(String cmpRptFileWithPath)
  {
    Log.debugLog(className, "readReport", "", "", "Method called. Compare Report FIle Name = " + cmpRptFileWithPath);

    try
    {
      Vector vecCmpReport = new Vector();
      String strLine;

      File cmpRptFile = new File(cmpRptFileWithPath);
      if (!cmpRptFile.exists())
      {
        Log.debugLog(className, "readReport", "", "", "File " + cmpRptFileWithPath + " not exist.");
        return null;
      }

      FileInputStream fis = new FileInputStream(cmpRptFileWithPath);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));

      while ((strLine = br.readLine()) != null)
      {
        strLine = strLine.trim();
        if (strLine.length() == 0)
          continue;
        Log.debugLog(className, "readReport", "", "", "Adding line in vector. Line = " + strLine);
        vecCmpReport.add(strLine);
      }
      br.close();
      fis.close();
      return vecCmpReport;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "readReport", "", "", "Exception - ", e);
      return null;
    }
  }

  // Get all Msr Report information in 1D array.
  public String[] getMsrDataLine()
  {
    Log.debugLog(className, "getMsrDataLine", "", "", "Method called");

    String cmpRptFileWithPath = workPath + "/webapps/logs/TR" + testRun + "/" + MSR_RPT_FILE + MSR_RPT_FILE_EXTN;
    return (getMsrDataLine(cmpRptFileWithPath));
  }

  // Get all Msr Report information in 1D array (filename is passed)
  public String[] getMsrDataLine(String cmpRptFileWithPath)
  {
    Log.debugLog(className, "getMsrDataLine", "", "", "Method called. cmpRptFileWithPath = " + cmpRptFileWithPath);

    try
    {
      String[] tempCmpRecords = null;
      Vector vecCmpReport = readReport(cmpRptFileWithPath);
      if (vecCmpReport == null || vecCmpReport.size() == 0)
      {
        Log.errorLog(className, "getMsrDataLine", "", "", "Error in reading file vecCmpReport = null");
        return null;
      }

      tempCmpRecords = new String[vecCmpReport.size()];

      for (int i = 0; i < tempCmpRecords.length; i++)
      {
        tempCmpRecords[i] = vecCmpReport.elementAt(i).toString();
      }

      return tempCmpRecords;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getMsrDataLine", "", "", "Exception - ", e);
      return null;
    }
  }

  // Delete the Msr Report File
  public boolean deleteMsrDataFile()
  {
    Log.debugLog(className, "deleteMsrDataFile", "", "", "Method called");

    File msrDataFile = openMsrFile(MSR_RPT_FILE + MSR_RPT_FILE_EXTN);
    if (msrDataFile == null)
      return false;
    else
    {
      msrDataFile.delete();
      return true;
    }
  }

  // Get all Msr Reports summary information in 2D array.
  public String[][] getMsrData()
  {
    Log.debugLog(className, "getMsrData", "", "", "Method called");

    try
    {
      String strLine = "";
      String[][] tempCmpRecords = null;
      Vector tempCmpRpt = null;

      String cmpRptFileWithPath = workPath + "/webapps/logs/TR" + testRun + "/" + MSR_RPT_FILE + MSR_RPT_FILE_EXTN;

      Vector vecCmpReport = readReport(cmpRptFileWithPath);

      if (vecCmpReport == null)
        return null;

      tempCmpRpt = vecCmpReport;

      if (tempCmpRpt.size() > 0)
        tempCmpRecords = new String[tempCmpRpt.size()][];

      for (int i = 0; i < tempCmpRecords.length; i++)
      {
        tempCmpRecords[i] = rptUtilsBean.strToArrayData(tempCmpRpt.elementAt(i).toString(), "|");
      }

      return tempCmpRecords;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getMsrData", "", "", "Exception - ", e);
      return null;
    }
  }

  // ----------- Methods related to reading/writing of compare report data --------------//

  // This will check if "compare.report" exist or not?
  public boolean isCmpRptSet(int numTestRun, String reportSetName)
  {
    String rptSetPath = getReportSetPath(reportSetName, numTestRun);
    File cmpDataFile = new File((rptSetPath + "/"), COMP_RPT_FILE + COMP_RPT_FILE_EXTN);

    if (!cmpDataFile.exists())
      return false;
    else
      return true;
  }

  // Get all Compare Report summary information in 2D array.
  public String[][] getCmpData(int numTestRun, String reportSetName)
  {
    Log.debugLog(className, "getCmpData", "", "", "Method called");

    try
    {
      String strLine = "";
      String[][] tempCmpRecords = null;
      Vector tempCmpRpt = new Vector();
      vecRptRowId = new Vector(); // get all report row id

      String cmpRptFileWithPath = getReportSetPath(reportSetName, numTestRun) + "/" + COMP_RPT_FILE + COMP_RPT_FILE_EXTN;

      Vector vecCmpReport = readReport(cmpRptFileWithPath);

      // it store actual length of number of reports
      totalAvailReports = vecCmpReport.size();

      if (vecCmpReport == null)
        return null;

      String dataLine = "";
      for (int i = 0; i < vecCmpReport.size(); i++)
      {
        dataLine = vecCmpReport.get(i).toString();
        String[] arrTempData = rptUtilsBean.strToArrayData(dataLine, "|");

        // for first header line & fixed part in compare.report file
        if ((arrTempData[STATUS_FIELD_INDEX].equals("Status")) || (arrTempData[STATUS_FIELD_INDEX].equals("NA") && dataLine.startsWith("NA")))
        {
          vecRptRowId.add("" + i);
          Log.debugLog(className, "getCmpData", "", "", "Add Report row id in vector = " + i);

          tempCmpRpt.add(dataLine);
          Log.debugLog(className, "getCmpData", "", "", "Add data line in vector = " + dataLine);
          continue; // if it is header line then no need to process
        }

        if (arrTempData[HIDE_FIELD_INDEX].equals("0")) // for slected row(s) except header row(s)
        {
          vecRptRowId.add("" + i);
          Log.debugLog(className, "getCmpData", "", "", "Add Report row id in vector = " + i);

          // if server signature then read data from server signature file
          if (arrTempData[CMP_RPT_TYPE].equals("SS"))
          {
            dataLine = "";
            for (int ii = 0; ii < arrTempData.length; ii++)
            {
              if (ii != 0)
                dataLine = dataLine + "|";

              if (arrTempData[ii].startsWith("/"))
                dataLine = dataLine + ServerSignatureInfo.getDataToShowInRpt(Config.getWorkPath() + arrTempData[ii]);
              else
                dataLine = dataLine + arrTempData[ii];
            }
            tempCmpRpt.add(dataLine);
            Log.debugLog(className, "getCmpData", "", "", "Add data line in vector = " + dataLine);
          }
          else
          {
            tempCmpRpt.add(dataLine);
            Log.debugLog(className, "getCmpData", "", "", "Add data line in vector = " + dataLine);
          }
        }
      }

      if (tempCmpRpt.size() > 0)
        tempCmpRecords = new String[tempCmpRpt.size()][];

      for (int i = 0; i < tempCmpRecords.length; i++)
      {
        tempCmpRecords[i] = rptUtilsBean.strToArrayData(tempCmpRpt.elementAt(i).toString(), "|");
      }

      return tempCmpRecords;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getCmpData", "", "", "Exception - ", e);
      return null;
    }
  }

  // Get all Compare Report summary information in 2D array.
  public String[][] getFilterCmpData(int numTestRun, String reportSetName, boolean bolNonZeroData, boolean bolShowServerSig, String strPctDiffGrtEql, String strPctDiffLessEql, String strDiffGrtEql, String strDiffLessEql)
  {
    Log.debugLog(className, "getFilterCmpData", "", "", "Method called");

    try
    {
      String strLine = "";
      String[][] tempCmpRecords = null;
      Vector tempCmpRpt = new Vector();
      vecRptRowId = new Vector(); // get all report row id

      String cmpRptFileWithPath = getReportSetPath(reportSetName, numTestRun) + "/" + COMP_RPT_FILE + COMP_RPT_FILE_EXTN;

      Vector vecCmpReport = readReport(cmpRptFileWithPath);

      // it store actual length of number of reports
      totalAvailReports = vecCmpReport.size();

      if (vecCmpReport == null)
        return null;

      String dataLine = "";
      ArrayList arrListColIndex = new ArrayList();
      ArrayList arrListColIndexDiff = new ArrayList();
      ArrayList arrListColIndexPctDiff = new ArrayList();

      if (vecCmpReport.size() > 0)
      {
        dataLine = vecCmpReport.get(0).toString();
        String[] arrTempData = rptUtilsBean.strToArrayData(dataLine, "|");
        for (int ii = 10; ii < arrTempData.length; ii++)
        {
          if ((arrTempData[ii].trim().indexOf(" Change(%)") > -1))
          {
            arrListColIndexPctDiff.add(ii);
          }
          else if ((arrTempData[ii].trim().indexOf(" Change") > -1))
          {
            arrListColIndexDiff.add(ii);
          }
          else if ((arrTempData[ii].trim().contains("Maximum")))
          {
            //TODO - we need to add one more filter for maximum
          }
          else
            arrListColIndex.add(ii);

        }
      }

      for (int i = 0; i < vecCmpReport.size(); i++)
      {
        dataLine = vecCmpReport.get(i).toString();
        String[] arrTempData = rptUtilsBean.strToArrayData(dataLine, "|");

        // for first header line & fixed part in compare.report file
        if ((arrTempData[STATUS_FIELD_INDEX].equals("Status")) || (arrTempData[STATUS_FIELD_INDEX].equals("NA") && dataLine.startsWith("NA")))
        {
          vecRptRowId.add("" + i);
          Log.debugLog(className, "getFilterCmpData", "", "", "Add Report row id in vector = " + i);

          tempCmpRpt.add(dataLine);
          Log.debugLog(className, "getFilterCmpData", "", "", "Add data line in vector = " + dataLine);
          continue; // if it is header line then no need to process
        }
        // else
        if (arrTempData[HIDE_FIELD_INDEX].equals("0")) // for slected row(s) except header row(s)
        {
          Log.debugLog(className, "getFilterCmpData", "", "", "Add Report row id in vector = " + i);

          dataLine = "";
          boolean isFilterNonZeroData = false;
          boolean isFilterPctDiffGrtEql = false;
          boolean isFilterPctDiffLessEql = false;
          boolean isFilterDiffGrtEql = false;
          boolean isFilterDiffLessEql = false;

          for (int ii = 0; ii < arrTempData.length; ii++)
          {
            if (!arrTempData[CMP_RPT_TYPE].equals("SS"))
            {
              if (bolNonZeroData && arrListColIndex.contains(ii))
              {
                if (!isFilterNonZeroData && ii > 9)
                {
                  Log.debugLog(className, "getFilterCmpData", "", "", "Check Non zero arrTempData[" + ii + "] = " + arrTempData[ii]);
                  if (arrTempData[ii] != null && !arrTempData[ii].equals("NA") && Double.parseDouble(arrTempData[ii]) > 0)
                    isFilterNonZeroData = true;
                }
              }

              if (!strPctDiffGrtEql.equals("") && arrListColIndexPctDiff.contains(ii))
              {
                if (!isFilterPctDiffGrtEql)
                {
                  Log.debugLog(className, "getFilterCmpData", "", "", "Percent Diff greater equal arrTempData[" + ii + "] = " + arrTempData[ii]);
                  if (arrTempData[ii] != null && !arrTempData[ii].equals("NA") && Math.abs(Double.parseDouble(arrTempData[ii])) >= Double.parseDouble(strPctDiffGrtEql))
                    isFilterPctDiffGrtEql = true;
                }
              }

              if (!strPctDiffLessEql.equals("") && arrListColIndexPctDiff.contains(ii))
              {
                if (!isFilterPctDiffLessEql)
                {
                  Log.debugLog(className, "getFilterCmpData", "", "", "Percent Diff Less equal arrTempData[" + ii + "] = " + arrTempData[ii]);
                  if (arrTempData[ii] != null && !arrTempData[ii].equals("NA") && Math.abs(Double.parseDouble(arrTempData[ii])) <= Double.parseDouble(strPctDiffLessEql))
                  {
                    if (bolNonZeroData && Double.parseDouble(arrTempData[ii]) == 0)
                      isFilterPctDiffLessEql = false;
                    else
                      isFilterPctDiffLessEql = true;
                  }
                }
              }

              if (!strDiffGrtEql.equals("") && arrListColIndexDiff.contains(ii))
              {
                if (!isFilterDiffGrtEql)
                {
                  Log.debugLog(className, "getFilterCmpData", "", "", "Diff Greater equal arrTempData[" + ii + "] = " + arrTempData[ii]);
                  if (arrTempData[ii] != null && !arrTempData[ii].equals("NA") && Math.abs(Double.parseDouble(arrTempData[ii])) >= Double.parseDouble(strDiffGrtEql))
                    isFilterDiffGrtEql = true;
                }
              }

              if (!strDiffLessEql.equals("") && arrListColIndexDiff.contains(ii))
              {
                if (!isFilterDiffLessEql)
                {
                  Log.debugLog(className, "getFilterCmpData", "", "", "Diff Less equal arrTempData[" + ii + "] = " + arrTempData[ii]);
                  if (arrTempData[ii] != null && !arrTempData[ii].equals("NA") && Math.abs(Double.parseDouble(arrTempData[ii])) <= Double.parseDouble(strDiffLessEql))
                  {
                    if (bolNonZeroData && Double.parseDouble(arrTempData[ii]) == 0)
                      isFilterDiffLessEql = false;
                    else
                      isFilterDiffLessEql = true;
                  }
                }
              }

              if (!arrTempData[ii].equals("NA") && arrListColIndex.contains(ii))
              {
                arrTempData[ii] = rptUtilsBean.convertTodecimal(Double.parseDouble(arrTempData[ii]), 3) + "";
                arrTempData[ii] = rptUtilsBean.convertNumberToCommaSeparate(arrTempData[ii]);
              }

              if (!arrTempData[ii].equals("NA") && arrListColIndexPctDiff.contains(ii))
              {
                arrTempData[ii] = "" + (int) Math.round(Float.parseFloat(arrTempData[ii]));
                arrTempData[ii] = rptUtilsBean.convertNumberToCommaSeparate(arrTempData[ii]);
              }

              if (!arrTempData[ii].equals("NA") && arrListColIndexDiff.contains(ii))
              {
                arrTempData[ii] = rptUtilsBean.convertTodecimal(Double.parseDouble(arrTempData[ii]), 3) + "";
                arrTempData[ii] = rptUtilsBean.convertNumberToCommaSeparate(arrTempData[ii]);
              }
            }

            if (arrTempData[ii].equals("NA"))
              arrTempData[ii] = "-";

            if (ii != 0)
              dataLine = dataLine + "|";
            // if server signature then read data from server signature file
            // if(bolShowServerSig && arrTempData[CMP_RPT_TYPE].equals("SS"))
            if (bolShowServerSig && arrTempData[CMP_RPT_TYPE].equals("SS"))
            {
              if (arrTempData[ii].startsWith("/"))
                arrTempData[ii] = ServerSignatureInfo.getDataToShowInRpt(Config.getWorkPath() + arrTempData[ii]);
            }

            dataLine = dataLine + arrTempData[ii];

            boolean bolfilterAllow = false;

            if (ii == (arrTempData.length - 1))
            {
              Log.debugLog(className, "getFilterCmpData", "", "", "arrTempData[CMP_RPT_TYPE] " + arrTempData[CMP_RPT_TYPE] + ", bolShowServerSig = " + bolShowServerSig + ",isFilterNonZeroData = " + isFilterNonZeroData + ", isFilterPctDiffLessEql = " + isFilterPctDiffLessEql + ", isFilterPctDiffGrtEql = " + isFilterPctDiffGrtEql + ", isFilterDiffGrtEql = " + isFilterDiffGrtEql + ", isFilterDiffLessEql = " + isFilterDiffLessEql);
              if (arrTempData[CMP_RPT_TYPE].equals("SS") && bolShowServerSig)
              {
                Log.debugLog(className, "getFilterCmpData", "", "", "Adding server Signature");
                bolfilterAllow = true;
              }
              else if ((bolNonZeroData) && strPctDiffGrtEql.equals("") && strPctDiffLessEql.equals("") && strDiffGrtEql.equals("") && strDiffLessEql.equals(""))
              {
                Log.debugLog(className, "getFilterCmpData", "", "", "Adding non zero value other param blank");
                if (isFilterNonZeroData)
                  bolfilterAllow = true;
              }
              // else if((!bolNonZeroData && !isFilterNonZeroData) && (isFilterPctDiffGrtEql || isFilterPctDiffLessEql || isFilterDiffGrtEql || isFilterDiffLessEql))
              else if (strPctDiffGrtEql.equals("") && strPctDiffLessEql.equals("") && strDiffGrtEql.equals("") && strDiffLessEql.equals(""))
              {
                Log.debugLog(className, "getFilterCmpData", "", "", "All param are blank");

                if (arrTempData[CMP_RPT_TYPE].equals("SS") && !bolShowServerSig)
                  bolfilterAllow = false;
                else
                  bolfilterAllow = true;
              }
              else if ((isFilterPctDiffGrtEql || isFilterPctDiffLessEql || isFilterDiffGrtEql || isFilterDiffLessEql))
              {
                Log.debugLog(className, "getFilterCmpData", "", "", "Any one filter is true");
                bolfilterAllow = true;
              }

              if (bolfilterAllow)
              {
                Log.debugLog(className, "getFilterCmpData", "", "", "Add data line in vector = " + dataLine);
                vecRptRowId.add("" + i);
                tempCmpRpt.add(dataLine);
              }
              isFilterNonZeroData = false;
              isFilterPctDiffGrtEql = false;
              isFilterPctDiffLessEql = false;
              isFilterDiffGrtEql = false;
              isFilterDiffLessEql = false;
            }
          }
        }
      }

      if (tempCmpRpt.size() > 0)
        tempCmpRecords = new String[tempCmpRpt.size()][];

      for (int i = 0; i < tempCmpRecords.length; i++)
      {
        tempCmpRecords[i] = rptUtilsBean.strToArrayData(tempCmpRpt.elementAt(i).toString(), "|");
      }

      return tempCmpRecords;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getFilterCmpData", "", "", "Exception - ", e);
      return null;
    }
  }

  public int[] getReportRowIdArray()
  {
    Log.debugLog(className, "getReportRowIdArray", "", "", "Method called");

    try
    {
      int[] arrRptRowId = new int[vecRptRowId.size()];
      for (int i = 0; i < vecRptRowId.size(); i++)
        arrRptRowId[i] = Integer.parseInt(vecRptRowId.get(i).toString());

      return arrRptRowId;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getReportRowIdArray", "", "", "Exception - ", e);
      return null;
    }
  }

  // Open compare.report file and return as File object
  public boolean openCmpDataFile(String reportSetName)
  {
    Log.debugLog(className, "openCmpDataFile", "", "", "Method called. Test Run = " + numTestRun + ", reportSetName = " + reportSetName);
    try
    {
      File cmpDataFile = new File((getReportSetPath(reportSetName, numTestRun) + "/"), COMP_RPT_FILE + COMP_RPT_FILE_EXTN);
      if (!cmpDataFile.exists())
        cmpDataFile.createNewFile();
      else
        cmpDataFile.delete(); // Must delete to make sure old file is not appended

      fosCmpDataFile = new FileOutputStream(cmpDataFile, true); // append mode
      psCmpDataFile = new PrintStream(fosCmpDataFile);
      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "openCmpDataFile", "", "", "Error in opening file", e);
      return false;
    }
  }

  public boolean closeCmpDataFile()
  {
    Log.debugLog(className, "closeCmpDataFile", "", "", "Method called");
    try
    {
      if (fosCmpDataFile != null)
        fosCmpDataFile.close();
      if (psCmpDataFile != null)
        psCmpDataFile.close();

      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "closeCmpDataFile", "", "", "Error in closing file", e);
      return false;
    }
  }

  // Add Data to "compare.report" File
  public boolean addCmpData(String cmpData)
  {
    Log.debugLog(className, "addCmpData", "", "", "Adding record to compare data file. cmpData = " + cmpData);
    try
    {
      if (cmpData.endsWith("|"))
        cmpData = cmpData + "NA";

      psCmpDataFile.println(cmpData); // Append the new record
      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "addCmpData", "", "", "Error in adding record in Compare Report file", e);
      return false;
    }
  }

  // This function update the compare.report file in the test run
  // args[1] -> # of test run, args[2] -> # of test run
  // args[3] -> Operation name (1 -> HIDE_SELECTED, 2 -> SHOW_SELECTED, 3 -> SHOW_ALL)
  // args[4] -> "either null means for all" or array of selected row index
  public boolean updateCmpData(int numTestRun, String reportSetName, String operationName, String[] arrCheckIdx)
  {
    Log.debugLog(className, "updateCmpData", "", "", "Method Start. TestRun = " + numTestRun + ", Report Set Name = " + reportSetName + ", Operation Name = " + operationName);

    try
    {
      String cmpRptFileWithPath = getReportSetPath(reportSetName, numTestRun) + "/" + COMP_RPT_FILE + COMP_RPT_FILE_EXTN;

      String setFlagHideIdx = "0"; // by default show all set hide flag to 0 (at the time of cmp report generation)
      String unsetFlagHideIdx = "NA";
      String status = "";

      Vector vecCmpReport = readReport(cmpRptFileWithPath);
      Vector vecUpdatedCmpReport = new Vector();

      if (vecCmpReport == null)
      {
        Log.errorLog(className, "updateCmpData", "", "", "Compare Report file not found, it may be corrupted.");
        return false;
      }

      if (operationName.equals("hideSelected"))
      {
        setFlagHideIdx = "1";
        unsetFlagHideIdx = "0";
      }

      if (operationName.equals("showSelected"))
      {
        setFlagHideIdx = "0";
        unsetFlagHideIdx = "1";
      }

      if (operationName.equals("showAll"))
      {
        setFlagHideIdx = "0";
        unsetFlagHideIdx = "0";
      }

      Log.debugLog(className, "updateCmpData", "", "", "Operation name = " + operationName + ", set hide field value = " + setFlagHideIdx + ", unset hide field value = " + unsetFlagHideIdx);

      if (arrCheckIdx != null)
      {
        Log.debugLog(className, "updateCmpData", "", "", "Selected report index = " + rptUtilsBean.arrToString(arrCheckIdx));

        String dataLine = "";
        for (int i = 0; i < vecCmpReport.size(); i++)
        {
          dataLine = vecCmpReport.get(i).toString();
          String[] arrTempData = rptUtilsBean.strToArrayData(dataLine, "|");

          // for first header line & fixed part in compare.report file
          if ((arrTempData[STATUS_FIELD_INDEX].equals("Status")) || (arrTempData[STATUS_FIELD_INDEX].equals("NA") && dataLine.startsWith("NA")))
          {
            vecUpdatedCmpReport.add(dataLine);
            Log.debugLog(className, "updateCmpData", "", "", "No need to update header line, dataLine = " + dataLine);
            continue; // if it is header line then no need to process
          }

          status = "";
          for (int j = 0; j < arrCheckIdx.length; j++)
          {
            if (i == (int) Integer.parseInt(arrCheckIdx[j]))
            {
              status = "true";
              break;
            }
          }

          if (operationName.equals("showSelected"))
          {
            if (status.equals("true")) // for slected row(s) except header row(s)
              arrTempData[HIDE_FIELD_INDEX] = setFlagHideIdx;
            else
              // for not slected row(s) except header row(s)
              arrTempData[HIDE_FIELD_INDEX] = unsetFlagHideIdx;

            dataLine = rptUtilsBean.strArrayToStr(arrTempData, "|");
          }
          else
          // for hide selected
          {
            if (status.equals("true"))
            {
              arrTempData[HIDE_FIELD_INDEX] = setFlagHideIdx;
              dataLine = rptUtilsBean.strArrayToStr(arrTempData, "|");
            }
          }

          vecUpdatedCmpReport.add(dataLine);
          Log.debugLog(className, "updateCmpData", "", "", "Updated cmp data line add to vector, dataLine = " + dataLine);
        }
      }
      else
      // for show all
      {
        String dataLine = "";
        for (int i = 0; i < vecCmpReport.size(); i++)
        {
          status = "";
          dataLine = vecCmpReport.get(i).toString();
          String[] arrTempData = rptUtilsBean.strToArrayData(dataLine, "|");

          // for first header line & fixed part in compare.report file
          if (arrTempData[STATUS_FIELD_INDEX].equals("Status") || (arrTempData[STATUS_FIELD_INDEX].equals("NA") && dataLine.startsWith("NA")))
          {
            vecUpdatedCmpReport.add(dataLine);
            Log.debugLog(className, "updateCmpData", "", "", "No need to update header line, dataLine = " + dataLine);
            continue;
          }
          else
          {
            arrTempData[HIDE_FIELD_INDEX] = setFlagHideIdx;

            dataLine = rptUtilsBean.strArrayToStr(arrTempData, "|");

            vecUpdatedCmpReport.add(dataLine);
            Log.debugLog(className, "updateCmpData", "", "", "Updated cmp data line add to vector, dataLine = " + dataLine);
          }
        }
      }

      closeCmpDataFile();

      File cmpFileObj = new File(cmpRptFileWithPath);

      if (cmpFileObj.exists())
      {
        boolean boolResult = cmpFileObj.delete();
        if (!boolResult)
        {
          Log.errorLog(className, "updateCmpData", "", "", "Error in deleting file.");
          return false;
        }
      }

      FileOutputStream fos = new FileOutputStream(cmpFileObj, true); // append mode
      PrintStream ps = new PrintStream(fos);

      for (int i = 0; i < vecUpdatedCmpReport.size(); i++)
        ps.println(vecUpdatedCmpReport.get(i).toString());

      fos.close();
      ps.close();

      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "updateCmpData", "", "", "Error in updating record in Compare Report file", e);
      return false;
    }
  }

  // This function common function that is called from JSP to validate each msr before adding it in Compare reports, and from Analysis GUI
  // args1 --> TestRunNum (String type)
  // args2 --> Template Name (String type)
  // args3 --> Time Option (Total Run, Run Phase Only, Specified Time)
  // args4 --> In case of Specified Time format it is start time else it is NA
  // args5 --> In case of Specified Time format it is end time else it is NA
  // args6 --> Display msg (StringBuffer Type)
  // args7 --> If anls flag is true then it is called from Analysis GUI, use grpIdArray & graphIdArray
  public static boolean validataMsrForPercentileReportsByModeType(String strTestRun, String rtplName, String time, String startTime, String endTime, StringBuffer displayMsg, boolean anlsFlag, String[] grpId, String[] graphIdArray)
  {
    Log.debugLog(className, "validataMsrForPercentileReportsByModeType", "", "", "Method Start. TestRun = " + strTestRun);

    try
    {
      // check if PDF available in TR or not
      if (!PDFNames.isPDFAvail(strTestRun))
      {
        Log.debugLog(className, "validataMsrForPercentileReportsByModeType", "", "", "Percentile reports are disable in this test run, testrun.pdf file not present in this TestRun = " + strTestRun);
        return true;
      }

      boolean percentileReportFlag = false;
      int numTestRun = Integer.parseInt(strTestRun);
      GraphNames objGraphNames = new GraphNames(numTestRun);

      Log.debugLog(className, "validataMsrForPercentileReportsByModeType", "", "", "anlsFlag = " + anlsFlag + ", numTestRun = " + numTestRun);

      if (anlsFlag)
      {
        for (int i = 0; i < grpId.length; i++)
        {
          int groupId = Integer.parseInt(grpId[i]);
          int graphId = Integer.parseInt(graphIdArray[i]);
          GraphUniqueKeyDTO graphUniqueKeyDTO = new GraphUniqueKeyDTO(groupId, graphId, "NA");
          long rptPCTGraphDataIndex = objGraphNames.getPDFDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTO);

          if (rptPCTGraphDataIndex < 0)
          {
            Log.debugLog(className, "validataMsrForPercentileReportsByModeType", "", "", "Percentile reports found in this TestRun = " + strTestRun + ", that is from custom group no need to validate Test Mode condition.");
          }
          else
          {
            percentileReportFlag = true;
            Log.debugLog(className, "validataMsrForPercentileReportsByModeType", "", "", "Percentile reports found in this TestRun = " + strTestRun + ", that is not from custom group need to validate Test Mode condition.");
            break;
          }
        }
      }
      else
      // for compare reports
      {
        Log.debugLog(className, "validataMsrForPercentileReportsByModeType", "", "", "Template Name = " + rtplName);

        String arrRtplRecs[][] = ReportTemplate.getTemplateDetails(rtplName, false);

        for (int i = 3; i < arrRtplRecs.length; i++) // First three lines are not for reports
        {
          String tempGraphViewType = arrRtplRecs[i][1];

          if (tempGraphViewType.equals("Percentile Graph") || tempGraphViewType.equals("Frequency Distribution Graph") || tempGraphViewType.equals("Slab Count Graph"))
          {
            try
            {
              String[][] arrRptInfo = ReportTemplate.rptInfoToArr(arrRtplRecs[i][arrRtplRecs[i].length - 1], false);

              for (int j = 0; j < arrRptInfo[0].length; j++)
              {
                try
                {
                  int tmpGroupId = Integer.parseInt(arrRptInfo[0][j]);
                  int tmpGraphId = Integer.parseInt(arrRptInfo[1][j]);
                  Log.debugLog(className, "validataMsrForPercentileReportsByModeType", "", "", "tmpGroupId = " + tmpGroupId + ", tmpGraphId = " + tmpGraphId);

                  GraphUniqueKeyDTO graphUniqueKeyDTO = new GraphUniqueKeyDTO(tmpGroupId, tmpGraphId, "NA");
                  long rptPCTGraphDataIndex = objGraphNames.getPDFDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTO);

                  // for custom Group
                  if (rptPCTGraphDataIndex < 0)
                  {
                    Log.debugLog(className, "validataMsrForPercentileReportsByModeType", "", "", "Percentile reports found in this TestRun = " + strTestRun + ", that is from custom group no need to validate Test Mode condition.");
                  }
                  else
                  {
                    // Percentile report found in template that is not from custom group need to validate testMode condition
                    percentileReportFlag = true;
                    Log.debugLog(className, "validataMsrForPercentileReportsByModeType", "", "", "Percentile reports found in this TestRun = " + strTestRun + ", that is not from custom group need to validate Test Mode condition.");

                    break;
                  }
                }
                catch (Exception ex)
                {
                  Log.stackTraceLog(className, "validataMsrForPercentileReportsByModeType", "", "", "Exception - ", ex);
                }
              }

              percentileReportFlag = true;
              if (percentileReportFlag)
                break;
            }
            catch (Exception ex)
            {
              Log.stackTraceLog(className, "validataMsrForPercentileReportsByModeType", "", "", "Exception - ", ex);
            }
          }
        }
      }

      // Percentile reports found in this TestRun, that is not from custom group need to validate Test Mode condition
      if (percentileReportFlag)
      {
        int modeType = objGraphNames.getPdfNames().getModeType();
        int interval = objGraphNames.getPdfNames().getInterval();

        Log.debugLog(className, "validataMsrForPercentileReportsByModeType", "", "", "Mode Type = " + modeType + ", interval = " + interval);

        double[] phaseTimes = rptUtilsBean.getPhaseTimes(strTestRun);
        long numStartTime = 0;
        long numEndTime = 0;

        // for total run
        if (modeType == 0)
        {
          // Only Total Run is allowed
          if (time.equals("Total Run"))
          {
            Log.debugLog(className, "validataMsrForPercentileReportsByModeType", "", "", "Mode Type = " + modeType + ", and time = " + time);

            return true;
          }
          else
          {
            Log.errorLog(className, "validataMsrForPercentileReportsByModeType", "", "", "Mode type is 0(Total Run), this " + time + " is not a valid condition.");

            displayMsg.append("Percentile data for this test run is available only for the total time. \nYou cannot use " + time + ".\nPlease change the time selection and try again.");

            return false;
          }
        }
        else if (modeType == 1) // for run phase only
        {
          if (time.equals("Total Run") || (time.equals("Run Phase Only") && phaseTimes == null))
          {
            Log.debugLog(className, "validataMsrForPercentileReportsByModeType", "", "", "Mode Type = " + modeType + ", and time = " + time);

            return true;
          }
          else
          {
            if (time.equals("Run Phase Only")) // Exclude RampUp, WarmUp and RampDown packets
            {
              Log.debugLog(className, "validataMsrForPercentileReportsByModeType", "", "", "Mode Type = " + modeType + ", and time = " + time);

              return true;
            }
            else
            {
              numStartTime = rptUtilsBean.convStrToMilliSec(startTime.trim());
              numEndTime = rptUtilsBean.convStrToMilliSec(endTime.trim());

              boolean startTimeFlag = false;
              boolean endTimeFlag = false;
              for (int i = 1; i < phaseTimes.length; i++)
              {
                if (numStartTime == phaseTimes[i])
                  startTimeFlag = true;
                if (numEndTime == phaseTimes[i])
                  endTimeFlag = true;
              }

              if (!(startTimeFlag && endTimeFlag))
              {
                Log.errorLog(className, "validataMsrForPercentileReportsByModeType", "", "", "Error: Invalid condition, Mode type is 1(RunPhase Only), and startTime " + startTime + " and endTime = " + endTime + " & Phase Times are " + phaseTimes[0] + ", " + phaseTimes[1] + ", " + phaseTimes[2] + ", " + phaseTimes[3]);

                displayMsg.append("Percentile data for this test run is available only for the Run Phase.\nYou cannot use " + time + ".\nPlease change the time selection and try again.");

                return false;
              }
            }

            Log.debugLog(className, "validataMsrForPercentileReportsByModeType", "", "", "Mode Type = " + modeType + ", and time = " + time);

            return true;
          }
        }
        else if (modeType == 2) // for specified interval
        {
          if (time.equals("Total Run") || (time.equals("Run Phase Only") && phaseTimes == null))
          {
            Log.debugLog(className, "validataMsrForPercentileReportsByModeType", "", "", "Mode Type = " + modeType + ", and time = " + time);

            return true;
          }
          else
          {
            if (time.equals("Run Phase Only")) // Exclude RampUp, WarmUp and RampDown packets
            {
              // phaseTimes[1] includes RampUp and WarmUp time
              numStartTime = (long) phaseTimes[1];
              // phaseTimes[2] includes RampUp, WarmUp and Run time
              numEndTime = (long) phaseTimes[2];
            }
            else
            {
              numStartTime = rptUtilsBean.convStrToMilliSec(startTime.trim());
              numEndTime = rptUtilsBean.convStrToMilliSec(endTime.trim());
            }
            // Make sure if sample time is divisible by interval then only we allow to generate graph data

            long startTimeDiff = numStartTime % interval;
            long endTimeDiff = numEndTime % interval;

            if ((startTimeDiff != 0) || (endTimeDiff != 0))
            {
              if (!time.equals("Run Phase Only")) // for Run Phase round up seq number
              {
                Log.errorLog(className, "validataMsrForPercentileReportsByModeType", "", "", "Mode type is 2(Specified), and startTime " + numStartTime + " and endTime = " + numEndTime + " is not divisible by interval = " + interval);

                displayMsg.append("Percentile data for this test run is available at an interval of " + interval / 1000 + " seconds.\nYou cannot use " + time + " which is not multiple of " + interval / 1000 + " seconds.\nPlease change the time selection and try again.");

                return false;
              }
            }
          }

          Log.debugLog(className, "validataMsrForPercentileReportsByModeType", "", "", "Mode Type = " + modeType + ", and time = " + time + ", and startTime " + numStartTime + " and endTime = " + numEndTime);

          return true;
        }
      }

      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "validataMsrForPercentileReportsByModeType", "", "", "Error in validating mesurement for percentile reports.", e);
      return false;
    }
  }

  // This function return the array of rptDataInfo, by strRptRowId(it is the comma seperated value of row id)
  public RptDataInfo[] getRptDataInfoByRptId(int numTestRun, String reportSetName, String strRptRowId, boolean bolNonZeroData, boolean bolShowServerSig, String strPctDiffGrtEql, String strPctDiffLessEql, String strDiffGrtEql, String strDiffLessEql)
  {
    Log.debugLog(className, "getRptDataInfoByRptId", "", "", "Method Start. Test Run = " + numTestRun + ", ReportSet Name = " + reportSetName + ", Report Row Id = " + strRptRowId);

    try
    {
      // get the compare report data in 2D array
      String[][] arrCmpReportData = getFilterCmpData(numTestRun, reportSetName, bolNonZeroData, bolShowServerSig, strPctDiffGrtEql, strPctDiffLessEql, strDiffGrtEql, strDiffLessEql);

      // create report row id array
      String[] arrRptRowId = rptUtilsBean.strToArrayData(strRptRowId, ",");

      // create rptDataInfo array by the length of arrRptRowId
      RptDataInfo[] rptDataInfo = new RptDataInfo[arrRptRowId.length];

      for (int i = 0; i < arrRptRowId.length; i++)
      {
        rptDataInfo[i] = new RptDataInfo();

        int rptRowId = Integer.parseInt(arrRptRowId[i]);

        // get the compare Hdr row array
        String[] cmpRptHdrData = arrCmpReportData[0];

        // get each compare report row data
        String[] cmpRptData = arrCmpReportData[rptRowId];

        // get reportName
        String rptName = cmpRptData[1];

        // Setting Para Information
        String arrParaInfo[] = new String[1];
        arrParaInfo[0] = rptName;
        rptDataInfo[i].setArrParaInfo(arrParaInfo);

        Log.debugLog(className, "getRptDataInfoByRptId", "", "", "Row Id = " + rptRowId + ", Report Name = " + rptName);

        // create the table data for server signature, to draw html table in jsp
        // 0 -> Number of table's data
        // 1 -> Msr Name
        // 2 -> Report data
        String[][][] arrTableData = null;

        if (cmpRptData[CMP_RPT_TYPE].equals("SS")) // to chk that it is server signature or not
        {
          // for each report size is always 1
          arrTableData = new String[1][][];

          Vector vecHdrInfo = new Vector();
          Vector vecSSMsrData = new Vector();

          for (int ii = 10; ii < cmpRptData.length; ii++)
          {
            // Skipping Change and Change(%) Columns
            if (((cmpRptHdrData[ii].lastIndexOf("Change")) == -1) && ((cmpRptHdrData[ii].lastIndexOf("Change(%)")) == -1))
            {
              vecHdrInfo.add(cmpRptHdrData[ii]);
              vecSSMsrData.add(cmpRptData[ii]);
              Log.debugLog(className, "getRptDataInfoByRptId", "", "", "Header Data[" + ii + "] = " + cmpRptHdrData[ii] + ", compare Data[" + ii + "] = " + cmpRptData[ii]);
            }
          }

          String[][] tempTableData = new String[2][vecSSMsrData.size()];

          // filling the header
          for (int jj = 0; jj < tempTableData[0].length; jj++)
          {
            tempTableData[0][jj] = vecHdrInfo.get(jj).toString();
          }

          // filling server signatures
          for (int kk = 0; kk < tempTableData[1].length; kk++)
          {
            int indx = 0;
            tempTableData[1][kk] = vecSSMsrData.get(kk).toString();

            if (tempTableData[1][kk].equals("NA") || tempTableData[1][kk].equals("Empty SS"))
              tempTableData[1][kk] = "No Server Signature data available";
          }

          arrTableData[0] = tempTableData;

          rptDataInfo[i].setArrTableData(arrTableData);
        }
        else
        // for img file
        {
          // create report file name
          String rptFileName = rptUtilsBean.doReplaceName(cmpRptData[1]);
          rptFileName = getReportLogPath(numTestRun, reportSetName) + rptFileName + ".jpeg";

          Log.debugLog(className, "getRptDataInfoByRptId", "", "", "Image File Name = " + rptFileName);

          String arrImgInfo[] = new String[1];
          arrImgInfo[0] = rptFileName;
          rptDataInfo[i].setArrImgPath(arrImgInfo);
        }
      }
      return rptDataInfo;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getRptDataInfoByRptId", "", "", "Error in getting rptDataInfo.", e);
      return null;
    }
  }

  // this function is to return the
  public String getNumReports()
  {
    Log.debugLog(className, "getNumReports", "", "", "Method called");

    try
    {
      return totalAvailReports + "";
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getNumReports", "", "", "Error in getting numReports.", e);
      return null;
    }
  }

  private String getReportLogPath(int numTestRun, String reportSetName)
  {
    return ("/logs/TR" + numTestRun + "/reports/reportSet/" + reportSetName + "/");
  }

  public static void main(String[] args)
  {
    CmpRptUtils cmpRptUtils = new CmpRptUtils("1023", "1");
    /*
     * cmpRptUtils.addMsrData("Msr_name1|5901|07/21/2007 10:10:10|0|Elapsed|Total Run|Elapsed|NA|NA|NA|NA");
     * cmpRptUtils.addMsrData("Msr_name2|5902|07/21/2007 10:10:10|0|Elapsed|Total Run|Elapsed|NA|NA|NA|NA"); String[][] temp = cmpRptUtils.getMsrData(); for (int i = 0; i < temp.length; i++) { for
     * (int j = 0; j < temp[i].length; j++) { System.out.print("temp==" + temp[i][j]); } System.out.println(); }
     */

    /*
     * String[] arrSelected = new String[]{"10", "15", "25", "30", "20"}; //boolean resultFlag = cmpRptUtils.updateCmpData(1023, "CmpRpt", "showAll", null); boolean resultFlag =
     * cmpRptUtils.updateCmpData(1023, "CmpRpt", "showSelected", arrSelected); //boolean resultFlag = cmpRptUtils.updateCmpData(1023, "CmpRpt", "hideSelected", arrSelected);
     * 
     * System.out.println("Result = " + resultFlag);
     */

    RptDataInfo rptDataInfo[] = null;
    // rptDataInfo = cmpRptUtils.getRptDataInfoByRptId(1023, "abc", "8,9,10,11,12");

    if (rptDataInfo == null)
    {
      System.out.println("Null Data");
      return;
    }

    for (int i = 0; i < rptDataInfo.length; i++)
    {
      String arrParaInfo[] = rptDataInfo[i].getArrParaInfo();

      for (int j = 0; j < arrParaInfo.length; j++)
      {
        System.out.println("<br>" + arrParaInfo[j]);
      }

      String arrImgPath[] = rptDataInfo[i].getArrImgPath();

      for (int k = 0; k < arrImgPath.length; k++)
      {
        System.out.println("<br>" + arrImgPath[k]);
      }

      String arrTableData[][][] = rptDataInfo[i].getArrTableData();

      for (int ctr1 = 0; ctr1 < arrTableData.length; ctr1++)
      {
        for (int ctr2 = 0; ctr2 < arrTableData[ctr1].length; ctr2++)
        {
          for (int ctr3 = 0; ctr3 < arrTableData[ctr1][ctr2].length; ctr3++)
          {
            System.out.println(arrTableData[ctr1][ctr2][ctr3] + " ");
          }
          System.out.println("<br>");
        }
        System.out.println("<br><br>");
      }
    }

    /*
     * String[][] arrData = cmpRptUtils.getCmpData(1023, "CmpRpt");
     * 
     * for (int i = 0; i < arrData.length; i++) { for (int j = 0; j < arrData[i].length; j++) { System.out.print(" " + arrData[i][j]); } System.out.println(); }
     */
  }
}

/*--------------------------------------------------------------------
  @Name    : ReportSetControl.java
  @Author  : Abhishek
  @Purpose : Bean for managing Report Set diretory generated during report set generation
  @Modification History:
    02/16/07:Abhishek:1.4.2 - Initial Version
----------------------------------------------------------------------*/

package pac1.Bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;

public class ReportSetControl implements java.io.Serializable
{
  static String className = "ReportSetControl";
  //static String workPath = Config.getWorkPath();

  private FileInputStream fis = null;
  private BufferedReader br = null;

  private static final String INP_FILE_HDR_LINE = "Report Set Name|Template Name|Report Generation Date/Time|Template Override Option|X-Axis Time Format|Time|Selection Time Format|Start Date|Start Time|End Date|End Time";
  public static String NON_RTPL_REPORT_SET_NAME = "Reports_Without_Template";
  private final String INP_EXTN = ".inp";
  private final String CNTL_EXTN = ".cntl";
  private final String JPEG_EXTN = ".jpeg";
  private final String HTML_EXTN = ".html";
  private boolean changePerm = true;

  public ReportSetControl()
  {
  }

  public ReportSetControl(boolean changePerm)
  {
    this.changePerm = changePerm;
  }

  private static String getReportSetBasePath(int numTestRun)
  {
    return (Config.getWorkPath() + "/webapps/logs/TR" + numTestRun + "/reports/reportSet");
  }

  private static String getAnalysisBasePath(int numTestRun)
  {
    return (Config.getWorkPath() + "/webapps/logs/TR" + numTestRun + "/reports/SavedAnalysis");
  }

  private static String getHTMLReportsBasePath(int numTestRun)
  {
    return (Config.getWorkPath() + "/webapps/logs/TR" + numTestRun + "/reports/htmlReports");
  }

  private static String getWordReportsBasePath(int numTestRun)
  {
    return (Config.getWorkPath() + "/webapps/logs/TR" + numTestRun + "/reports/wordReports");
  }

  private String getReportsPath(int numTestRun)
  {
    return (Config.getWorkPath() + "/webapps/logs/TR" + numTestRun + "/reports");
  }

  private String getReportLogPath(int numTestRun, String reportSetName)
  {
    return ("/logs/TR" + numTestRun + "/reports/reportSet/" + reportSetName + "/");
  }

  public String getReportSetPath(String reportSetName, int numTestRun)
  {
    return (getReportSetBasePath(numTestRun) + "/" + reportSetName);
  }

  public String getAnalysisPath(String reportSetName, int numTestRun)
  {
    return (getAnalysisBasePath(numTestRun) + "/" + reportSetName);
  }

  public String getHTMLReportsPath(String reportSetName, int numTestRun)
  {
    return (getHTMLReportsBasePath(numTestRun) + "/" + reportSetName);
  }

  public String getWordReportsPath(String reportSetName, int numTestRun)
  {
    return (getWordReportsBasePath(numTestRun) + "/" + reportSetName);
  }

  public static String getReportSetPathForNonRtpl(String testRun)
  {
    return (getReportSetBasePath(Integer.parseInt(testRun)) + "/" + NON_RTPL_REPORT_SET_NAME);
  }

  // Log file for report set generation log. This is shown to user in a GUI screen
  public String getReportSetStatusLogFileName(String reportSetName, int numTestRun)
  {
    return(getReportSetPath(reportSetName, numTestRun) + "/reportSetGeneration.log");
  }

  // Open path and return as File object
  private boolean openReportsPath(int numTestRun, boolean creFlag)
  {
    try
    {
      File rprtFile = new File(getReportsPath(numTestRun));
      // Check Path exists or not? If not create it and set permission and owenership
      if(!rprtFile.exists())
      {
        if(creFlag == false)
          return false;
        if(!rprtFile.mkdir())
        {
          Log.errorLog(className, "openReportsPath", "", "", "Error in creating directory");
          return false;
        }
        // Change ownership and permission of directory
        // this will change the permission when user is not root
        //<reports> directory permission has changed from 755 to 775 to allow same group users 5-02-2009
        if(changePerm)
          if(rptUtilsBean.changeFilePerm(rprtFile.getAbsolutePath(), "netstorm", "netstorm", "775") == false)
            return false;
      }

      return true;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "openReportsPath", "", "", "Exception - " + e);
      return false;
    }
  }

  // Open path and return as File object
  private boolean openAnalysisPath(int numTestRun)
  {
    try
    {
      File rprtFile = new File(getReportsPath(numTestRun));
      // Check Path exists or not? If not create it and set permission and owenership
      if(!rprtFile.exists())
        return false;
      return true;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "openAnalysisPath", "", "", "Exception - " + e);
      return false;
    }
  }

  // Open path and return as File object
  private boolean openHTMLReportsPath(int numTestRun)
  {
    try
    {
      File rprtFile = new File(getReportsPath(numTestRun));
      // Check Path exists or not? If not create it and set permission and owenership
      if(!rprtFile.exists())
        return false;

      return true;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "openHTMLReportsPath", "", "", "Exception - " + e);
      return false;
    }
  }

  // Open path and return as File object
  private boolean openWordReportsPath(int numTestRun)
  {
    try
    {
      File rprtFile = new File(getReportsPath(numTestRun));
      // Check Path exists or not? If not create it and set permission and owenership
      if(!rprtFile.exists())
        return false;

      return true;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "openWordReportsPath", "", "", "Exception - " + e);
      return false;
    }
  }

  // This will return File object of created report set directory
  private boolean openReportSetBasePath(int numTestRun, boolean creFlag)
  {
    try
    {
      if(openReportsPath(numTestRun, creFlag) == false)
        return false;

      File rprtFile = new File(getReportSetBasePath(numTestRun));
      // Check Path exists or not? If not create it and set permission and owenership
      if(!rprtFile.exists())
      {
        if(creFlag == false)
          return false;
        if(!rprtFile.mkdir())
        {
          Log.errorLog(className, "openReportSetBasePath", "", "", "Error in creating report set directory");
          return false;
        }
      // Change ownership and permission of directory
        // this will change the permission when user is not root
      if(changePerm)
        if(rptUtilsBean.changeFilePerm(rprtFile.getAbsolutePath(), "netstorm", "netstorm", "775") == false)
          return false;
      }

      return true;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "openReportSetBasePath", "", "", "Exception - " + e);
      return false;
    }
  }


  // This will return File object of created report set directory
  private boolean openAnalysisBasePath(int numTestRun)
  {
    try
    {
      if(openAnalysisPath(numTestRun) == false)
        return false;

      File rprtFile = new File(getAnalysisBasePath(numTestRun));
      // Check Path exists or not? If not create it and set permission and owenership
      if(!rprtFile.exists())
        return false;
      return true;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "openAnalysisBasePath", "", "", "Exception - " + e);
      return false;
    }
  }

  // This will return File object of created report set directory
  private boolean openHTMLReportsBasePath(int numTestRun)
  {
    try
    {
      if(openHTMLReportsPath(numTestRun) == false)
        return false;

      File rprtFile = new File(getHTMLReportsBasePath(numTestRun));
      // Check Path exists or not? If not create it and set permission and owenership
      if(!rprtFile.exists())
        return false;
      return true;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "openHTMLReportsBasePath", "", "", "Exception - " + e);
      return false;
    }
  }

  // This will return File object of created report set directory
  private boolean openWordReportsBasePath(int numTestRun)
  {
    try
    {
      if(openWordReportsPath(numTestRun) == false)
        return false;

      File rprtFile = new File(getWordReportsBasePath(numTestRun));
      // Check Path exists or not? If not create it and set permission and owenership
      if(!rprtFile.exists())
        return false;
      return true;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "openWordReportsBasePath", "", "", "Exception - " + e);
      return false;
    }
  }

  // This will read all inp file of every report set including NonRtpl reports
  // This will return report set detail in 2D array array.
  public String[][] getReportSets(String testRun)
  {
    File rprtFile = null;
    Vector tempRptVc = new Vector();
    String[][] tempArr = null;
    int numTestRun = Integer.parseInt(testRun.trim());
    File gpFile = null;

    try
    {
      boolean checkPath = openReportSetBasePath(numTestRun, false);

      if(checkPath)
      {
        gpFile = new File(getReportSetBasePath(numTestRun));

        String arrayFiles[] = gpFile.list();
        // If graphs directory not exists then arrayFiles will be null
        for(int i = 0; i < arrayFiles.length; i++)
        {
          // this chk is to avoid System files
          if(!arrayFiles[i].startsWith("__"))
          {
            String inputFileName = getReportSetPath(arrayFiles[i], numTestRun) + "/" + arrayFiles[i] + INP_EXTN;
            File file = new File(getReportSetPath(arrayFiles[i], numTestRun));
            if(file.isDirectory())
            {
              Vector vecRtpl = readFile(inputFileName, 1);
              if((vecRtpl != null) && (vecRtpl.size() != 0)) // This will check for null if file does not exist or is empty or have only one line
              {
                if(rptUtilsBean.strToArrayData(vecRtpl.elementAt(0).toString(), "|").length == 11)
                  tempRptVc.add(vecRtpl.elementAt(0).toString());
              }
            }
          }
        }
      }

      //code added to get all analysis saved on server
      boolean analysisPath = openAnalysisBasePath(numTestRun);

      if(analysisPath)
      {
        File anlsFile = new File(getAnalysisBasePath(numTestRun));

        String anlsFiles[] = anlsFile.list();
        // If graphs directory not exists then arrayFiles will be null
        for(int i = 0; i < anlsFiles.length; i++)
        {
          File anlsFile1 = new File(getAnalysisPath(anlsFiles[i], numTestRun));
          if(anlsFile1.isDirectory())
          {
            String addAnlsRow = anlsFiles[i] + "|" + "Analysis Report" + "|" + "NA" + "|" + "NA" + "|" + "NA" + "|" + "NA" + "|" + "NA" + "|" + "NA" + "|" + "NA" + "|" + "NA" + "|" + "NA";
            tempRptVc.add(addAnlsRow);
          }
        }
      }

      //code added to get all HTML reports saved on server
      boolean htmlPath = openHTMLReportsBasePath(numTestRun);

      if(htmlPath)
      {
        File htmlFile = new File(getHTMLReportsBasePath(numTestRun));

        String htmlFiles[] = htmlFile.list();
        // If graphs directory not exists then arrayFiles will be null
        for(int i = 0; i < htmlFiles.length; i++)
        {
          File htmlFile1 = new File(getHTMLReportsPath(htmlFiles[i], numTestRun));
          if(htmlFile1.isDirectory())
          {
            String addAnlsRow = htmlFiles[i] + "|" + "HTML Report" + "|" + "NA" + "|" + "NA" + "|" + "NA" + "|" + "NA" + "|" + "NA" + "|" + "NA" + "|" + "NA" + "|" + "NA" + "|" + "NA";
            tempRptVc.add(addAnlsRow);
          }
        }
      }

      //code added to get all Word reports saved on server
      boolean wordPath = openWordReportsBasePath(numTestRun);

      if(wordPath)
      {
        File wordFile = new File(getWordReportsBasePath(numTestRun));

        String wordFiles[] = wordFile.list();
        // If graphs directory not exists then arrayFiles will be null
        for(int i = 0; i < wordFiles.length; i++)
        {
          File wordFile1 = new File(getWordReportsPath(wordFiles[i], numTestRun));
          if(wordFile1.isDirectory())
          {
            String addAnlsRow = wordFiles[i] + "|" + "Word Report" + "|" + "NA" + "|" + "NA" + "|" + "NA" + "|" + "NA" + "|" + "NA" + "|" + "NA" + "|" + "NA" + "|" + "NA" + "|" + "NA";
            tempRptVc.add(addAnlsRow);
          }
        }
      }

      if(tempRptVc.size() > 0)
       tempArr = new String[tempRptVc.size() + 1][11];
      else
        tempArr = new String[1][11];

      tempArr[0][0] = "Report Set Name";
      tempArr[0][1] = "Template Name";
      tempArr[0][2] = "Report Generation Date/Time";
      tempArr[0][3] = "Template Time Override Option";
      tempArr[0][4] = "X Axis Time Format";
      tempArr[0][5] = "Time";
      tempArr[0][6] = "Selection Time Format";
      tempArr[0][7] = "Start Date";
      tempArr[0][8] = "Start Time";
      tempArr[0][9] = "End Date";
      tempArr[0][10] = "End Time";

      //if(tempRptVc.size() == 0) // Not report set existing
      //  return tempArr;

      for(int i = 0; i < tempRptVc.size(); i++)
      {
        tempArr[i+1] = rptUtilsBean.strToArrayData(tempRptVc.elementAt(i).toString(), "|");

        if(tempArr[i+1][0].equals(NON_RTPL_REPORT_SET_NAME))
          tempArr[i+1][1] = "GeneratedWithoutTemplate";
      }



      return tempArr;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "getReportSets", "", "", "Exception - " + e);
      e.printStackTrace();
      return null;
    }
  }

  // This will return array for available graph records
  public String[][] getReportDetail(String testRun, String reportSetName, String rptDesc)
  {
    Log.debugLog(className, "getReportDetail", "", "", "Report Control file = " +  reportSetName + CNTL_EXTN);

    int numTestRun = Integer.parseInt(testRun);
    String cntlFile = getReportSetPath(reportSetName, numTestRun) + "/" + reportSetName + CNTL_EXTN;
    String[][] tempArrTemplate = null;

    try
    {
      Vector vecCntl = null;
      File reportControlFile = new File(cntlFile);

      if(reportControlFile.exists())
        vecCntl = readFile(cntlFile, -1);

      if(vecCntl != null)
        tempArrTemplate = new String[vecCntl.size()+1][14];
      else
        tempArrTemplate = new String[1][14];

      tempArrTemplate[0][0] = "Report Description";
      tempArrTemplate[0][1] = "Graph View Type";
      tempArrTemplate[0][2] = "Type";
      tempArrTemplate[0][3] = "Granularity";
      tempArrTemplate[0][4] = "X-Axis Time Format";
      tempArrTemplate[0][5] = "Time";
      tempArrTemplate[0][6] = "Selection Time Format";
      tempArrTemplate[0][7] = "Start Date";
      tempArrTemplate[0][8] = "Start Time";
      tempArrTemplate[0][9] = "End Date";
      tempArrTemplate[0][10] = "End Time";
      tempArrTemplate[0][11] = "Report Info";
      tempArrTemplate[0][12] = "Report File Name";
      tempArrTemplate[0][13] = "Report Flag";

      if(vecCntl == null)
        return tempArrTemplate;

      for(int i = 0; i < vecCntl.size(); i++)
      {
        StringTokenizer stTokenTemplate = new StringTokenizer(vecCntl.elementAt(i).toString(), "|");

        tempArrTemplate[i+1] = new String[stTokenTemplate.countTokens() + 2];

        int j = 0;
        while(stTokenTemplate.hasMoreTokens())
        {
          tempArrTemplate[i+1][j] = stTokenTemplate.nextToken();
          j++;
        }

        boolean rptFlag = rptUtilsBean.isDirPathExist(Config.getWorkPath() + "/webapps" + getReportLogPath(numTestRun, reportSetName) + rptUtilsBean.doReplaceName(tempArrTemplate[i+1][0]));

        if(rptFlag)
        {
          tempArrTemplate[i+1][12] = getReportLogPath(numTestRun, reportSetName) + rptUtilsBean.doReplaceName(tempArrTemplate[i+1][0]);

          tempArrTemplate[i+1][13] = "dir";
        }
        else
        {
          String filePath = getReportLogPath(numTestRun, reportSetName) + rptUtilsBean.doReplaceName(tempArrTemplate[i+1][0]) + JPEG_EXTN;

          rptFlag = rptUtilsBean.isFileExist(Config.getWorkPath() + "/webapps" + filePath);

          if(rptFlag) // if file is .jpeg
          {
            tempArrTemplate[i+1][12] = filePath;

            tempArrTemplate[i+1][13] = "img";
          }
          else // if file is .html
          {
            filePath = getReportLogPath(numTestRun, reportSetName) + rptUtilsBean.doReplaceName(tempArrTemplate[i+1][0]) + HTML_EXTN;

            rptFlag = rptUtilsBean.isFileExist(Config.getWorkPath() + "/webapps" + filePath);

            if(rptFlag)
            {
              tempArrTemplate[i+1][12] = filePath;

              tempArrTemplate[i+1][13] = "html";
            }
          }
        }
      }
      br.close();
      fis.close();

      return tempArrTemplate;
    }
    catch(Exception e)
    {
      System.out.println(e);
      Log.stackTraceLog(className, "getReportDetail", "", "", "Exception while getting detail -" , e);
      return null;
    }
  }

  // This will check if report set already exists or not?
  public boolean isReportSetExisting(int numTestRun, String reportSetName)
  {
    try
    {
      if(openReportSetBasePath(numTestRun, false) == false)
        return false;
      String reportSetDirName = getReportSetPath(reportSetName, numTestRun);
      File  rprtDir = new File(reportSetDirName);
      if(rprtDir.exists())
        return true;
      else
        return false;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "isReportSetExisting", "", "", "Exception - " + e);
      return false;
    }
  }

  // This will return File object of created report set directory
  public File createReportSetDir(int numTestRun, String reportSetName)
  {

    try
    {
      if(openReportSetBasePath(numTestRun, true) == false)
        return null;
      String reportSetDirName = getReportSetPath(reportSetName, numTestRun);

      File  rprtDir = new File(reportSetDirName);
      // Check Path exists or not? If not create it and set permission and owenership
      if(!rprtDir.exists())
      {
        if(!rprtDir.mkdir())
        {
          Log.errorLog(className, "createReportSetDir", "", "", "Error in creating report set directory");
          return null;
        }
        // Change ownership and permission of directory
        // this will change the permission when user is not root
        if(changePerm)
          if(rptUtilsBean.changeFilePerm(rprtDir.getAbsolutePath(), "netstorm", "netstorm", "775") == false)
            return null;
      }

      return(rprtDir);
    }
    catch(Exception e)
    {
      Log.errorLog(className, "createReportSetDir", "", "", "Exception - " + e);
      return null;
    }
  }

  // Methods for reading any file in a vector.
  // If rowNum is -1, then complete file is loaded in vector.
  //    otherwise, rowNum line is loaded in vector
  private Vector readFile(String fileName, int rowNum)
  {
    Log.debugLog(className, "readFile", "", "", "Method called. Filename = " + fileName);

    try
    {
      Vector vecRtpl = new Vector();
      String strLine;

      fis = new FileInputStream(fileName);
      br = new BufferedReader(new InputStreamReader(fis));

      int j = 0;
      while((strLine = br.readLine()) !=  null)
      {
        if(rowNum != -1)
        {
          if(j == rowNum)
          {
            Log.debugLog(className, "readFile", "", "", "Adding line in vector. Line = " + strLine);
            vecRtpl.add(strLine);
            break;
          }
        }
        else
        {
          if(!strLine.startsWith("#") || (strLine.length() != 0))  // Ignore commented and empty lines
          {
            Log.debugLog(className, "readFile", "", "", "Adding line in vector. Line = " + strLine);
            vecRtpl.add(strLine);
          }
        }
        j++;
      }
      br.close();
      fis.close();
      if(vecRtpl.size() < 1)
      {
        Log.errorLog(className, "readFile", "", "", "Invalid Report file. File name = " + fileName);
        return null;
      }
      return vecRtpl;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "readFile", "", "", "Exception in reading file. File name = " + fileName + ". Exception - " + e);
      // e.printStackTrace();
      return null;
    }
  }

  // This will delete Report set directory and all files in this report set
  public boolean deleteRptSetDir(String reportSetName, String testRun)
  {
    Log.reportDebugLog(className, "deleteRptSetDir", "", "", "Deleting report set. Test Run is " + testRun + ", Report Set = " + reportSetName);
    Date start = new Date();
    int numTestRun = Integer.parseInt(testRun.trim());

    String reportSetWithPath = getReportSetPath(reportSetName, numTestRun);

    boolean result = removeDir(reportSetWithPath);

    Log.reportDebugLog(className, "deleteRptSetDir", "", "", "Report set deletion is complete. Time taken is "+ rptUtilsBean.convMilliSecToStr((new Date()).getTime() - start.getTime()));

    return result;
  }


  // This will delete Analysis directory and all files in this Analysis
  public boolean deleteAnalysisReportDir(String analysisReportName, String testRun)
  {
    Log.reportDebugLog(className, "deleteAnalysisReportDir", "", "", "Deleting analysis report. Test Run is " + testRun + ", Analysis Report = " + analysisReportName);
    Date start = new Date();
    int numTestRun = Integer.parseInt(testRun.trim());

    String analysisReportWithPath = getAnalysisPath(analysisReportName, numTestRun);

    boolean result = removeDir(analysisReportWithPath);

    Log.reportDebugLog(className, "deleteAnalysisReportDir", "", "", "Analysis Report deletion is complete. Time taken is "+ rptUtilsBean.convMilliSecToStr((new Date()).getTime() - start.getTime()));

    return result;
  }

  // This will delete HTML directory and all files in this HTML
  public boolean deleteHTMLReportDir(String htmlReportName, String testRun)
  {
    Log.reportDebugLog(className, "deleteHTMLReportDir", "", "", "Deleting html report. Test Run is " + testRun + ", HTML Report = " + htmlReportName);
    Date start = new Date();
    int numTestRun = Integer.parseInt(testRun.trim());

    String htmlReportWithPath = getHTMLReportsPath(htmlReportName, numTestRun);

    boolean result = removeDir(htmlReportWithPath);

    Log.reportDebugLog(className, "deleteHTMLReportDir", "", "", "HTML Report deletion is complete. Time taken is "+ rptUtilsBean.convMilliSecToStr((new Date()).getTime() - start.getTime()));

    return result;
  }

  // This will delete Word Report file
  public boolean deleteWordReportDir(String wordReportName, String testRun)
  {
    Log.reportDebugLog(className, "deleteWordReportDir", "", "", "Deleting word report. Test Run is " + testRun + ", Word Report = " + wordReportName);
    Date start = new Date();
    int numTestRun = Integer.parseInt(testRun.trim());

    String wordReportWithPath = getWordReportsPath(wordReportName, numTestRun);

    boolean result = removeDir(wordReportWithPath);

    Log.reportDebugLog(className, "deleteWordReportDir", "", "", "Word Report deletion is complete. Time taken is "+ rptUtilsBean.convMilliSecToStr((new Date()).getTime() - start.getTime()));

    return result;
  }

  // Delete directory recuscively
  public boolean removeDir(String reportSetWithPath)
  {
    Log.reportDebugLog(className, "removeDir", "", "", "Deleting report set. Report Set = " + reportSetWithPath);

    try
    {
      File newFile = new File(reportSetWithPath);

      String arrayFiles[] = newFile.list();

      if(arrayFiles != null)
      {
        for(int i = 0; i < arrayFiles.length; i++)
        {
          String strTempFile = reportSetWithPath + "/" + arrayFiles[i];

          File newFile1 = new File(strTempFile);

          if(newFile1.isFile())
          {
            Log.reportDebugLog(className, "removeDir", "", "", "Deleting report/file - " + reportSetWithPath + "/" + arrayFiles[i]);

            if(newFile1.delete() == false)
            {
              Log.errorLog(className, "removeDir", "", "", "Error in deleting file - " + reportSetWithPath + "/" + arrayFiles[i]);
              return false;
            }
          }
          if(newFile1.isDirectory())
          {
            removeDir(strTempFile);
          }
          if(newFile1.isDirectory())
          {
            if(newFile1.delete() == false)
            {
              Log.errorLog(className, "removeDir", "", "", "Error in deleting (" + reportSetWithPath + ")");
              return false;
            }
          }
        }
      }

      if((new File(reportSetWithPath)).delete() == false)
      {
        Log.errorLog(className, "removeDir", "", "", "Error in deleting (" + reportSetWithPath + ")");
        return false;
      }
    }
    catch(Exception e)
    {
      Log.errorLog(className, "deleteRptSetDir", "", "", "Exception in deletion of Report Set Dir or report/file (" + reportSetWithPath + ") - " + e);
      return false;
    }
    return true;
  }


/*  // This will delete Report set directory and all files in this report set
  public boolean deleteRptSetDir(String reportSetName, String testRun)
  {
    Log.reportDebugLog(className, "deleteRptSetDir", "", "", "Deleting report set. Test Run is " + testRun + ", Report Set = " + reportSetName);
    Date start = new Date();
    int numTestRun = Integer.parseInt(testRun.trim());

    String reportSetWithPath = getReportSetPath(reportSetName, numTestRun);

    try
    {
      File newFile = new File(reportSetWithPath);
      String arrayFiles[] = newFile.list();
      if(arrayFiles != null)
      {
        for(int i = 0; i < arrayFiles.length; i++)
        {
          Log.reportDebugLog(className, "deleteRptSetDir", "", "", "Deleting report/file - " + reportSetWithPath + "/" + arrayFiles[i]);
          File newFile1 = new File(reportSetWithPath, arrayFiles[i]);
          if(newFile1.delete() == false)
          {
            Log.errorLog(className, "deleteRptSetDir", "", "", "Error in deleting file - " + reportSetWithPath + "/" + arrayFiles[i]);
            return false;
          }
        }
      }

      if((new File(reportSetWithPath)).delete() == false)
      {
        Log.errorLog(className, "deleteRptSetDir", "", "", "Error in deleting (" + reportSetWithPath + ")");
        return false;
      }

      Log.reportDebugLog(className, "deleteRptSetDir", "", "", "Report set deletion is complete. Time taken is "+ rptUtilsBean.convMilliSecToStr((new Date()).getTime() - start.getTime()));
    }
    catch(Exception e)
    {
      Log.errorLog(className, "deleteRptSetDir", "", "", "Exception in deletion of Report Set Dir or report/file (" + reportSetWithPath + ") - " + e);
      return false;
    }
    return true;
  }
*/

  private static String getInfoForReportSetInputFile(String testRun, String templateName, String reportSetName, boolean overrideRptOptions, String updateTemplateValue, String startDate)
  {
    Log.debugLog(className, "getInfoForReportSetInputFile", "", "", "Start method");
    Date timeDate = null;
    String strDateTime = "";
    String endDateTime = "";
    String reportSetGenTimeRec = "";
    String[] updateGraphValues = null;
    long timeInmilli = 0;

    try
    {
      if(overrideRptOptions == false)
      {
        strDateTime = "NA|NA";
        endDateTime = "NA|NA";
      }
      else
      {
        updateGraphValues = rptUtilsBean.strToArrayData(updateTemplateValue, "|");
        if(updateGraphValues[2].equals("Elapsed"))
        {
          if(updateGraphValues[1].equals("Specified Time"))
          {
            strDateTime = "NA|" + updateGraphValues[4];
            endDateTime = "NA|" + updateGraphValues[6];
          }
          else
          {
            strDateTime = "NA|NA";
            endDateTime = "NA|NA";
          }
        }
        else
        {
          if(updateGraphValues[1].equals("Specified Time"))
          {
            strDateTime = updateGraphValues[3] + " - " + updateGraphValues[4];
            endDateTime = updateGraphValues[5] + " - " + updateGraphValues[6];
          }
          else
          {
            strDateTime = "NA|NA";
            endDateTime = "NA|NA";
          }
        }
      }

      String strGenTimeStamp = rptUtilsBean.setDateFormat("MM/dd/yy HH:mm", (new Date()).getTime());

      if(overrideRptOptions == false)
        reportSetGenTimeRec = reportSetName + "|" + templateName + "|" + strGenTimeStamp + "|" + "NA" + "|NA|NA|NA|" + strDateTime + "|" + endDateTime;
      else
        reportSetGenTimeRec = reportSetName + "|" + templateName + "|" + strGenTimeStamp + "|" + "1" + "|" + updateTemplateValue;

      return reportSetGenTimeRec;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "getInfoForReportSetInputFile", "", "", "Exception in  -" + e);
      return "";
    }
  }


  // This will create file in report set for detail for report set directory
  public boolean createReportSetInputFile(String testRun, String templateName, String reportSetName, boolean overrideRptOptions, String updateTemplateValue, String startDate)
  {
    Log.debugLog(className, "createReportSetInputFile", "", "", "Test Run =" + testRun + ", Input File =" + reportSetName + INP_EXTN);
    int numTestRun = Integer.parseInt(testRun);

    String inputFileName = getReportSetPath(reportSetName, numTestRun) + "/" + reportSetName + INP_EXTN;
    String str = "";

    try
    {
      String infoForInputFile = getInfoForReportSetInputFile(testRun, templateName, reportSetName, overrideRptOptions, updateTemplateValue, startDate);
      File inputFile = new File(inputFileName);

      if(inputFile.exists())
        inputFile.delete();
      inputFile.createNewFile(); // Create new report set input file

      FileOutputStream fout = new FileOutputStream(inputFile, true);  // append mode
      PrintStream pw = new PrintStream(fout);
      pw.println(INP_FILE_HDR_LINE);
      pw.println(infoForInputFile); // Append the new report set detail to the end of file
      pw.close();
      fout.close();

      return true;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "createReportSetInputFile", "", "", "Exception in adding record to input file (" + inputFileName + ") - " + e);
      return false;
    }
  }

  // This will delete report entry from the control file using report description as key
  private boolean delRptFromCntlFile(String testRun, String rptSetName, String rptDesc)
  {
    Log.debugLog(className, "delRptFromCntlFile", "", "", "Control File = " + rptSetName + CNTL_EXTN);
    int numTestRun = Integer.parseInt(testRun);

    String cntlFileName = getReportSetPath(rptSetName, numTestRun) + "/" + rptSetName + CNTL_EXTN;
    Vector vecCntl = new Vector();
    String str = "";

    try
    {
      File reportControlFile = new File(cntlFileName);

      if(reportControlFile.exists())
      {
        vecCntl = readFile(cntlFileName, -1); // read complete file in vector
        reportControlFile.delete(); // Delete control file
        reportControlFile.createNewFile(); // Create new control file
      }
      else
      {
        Log.errorLog(className, "delRptFromCntlFile", "", "", "Report control file does not exist (" + cntlFileName + ")");
        return false;
     }
      FileOutputStream fout = new FileOutputStream(reportControlFile, true);  // append mode
      PrintStream pw = new PrintStream(fout);
      String status = "";

      for(int i = 0; i < vecCntl.size(); i++)
      {
        str = (vecCntl.elementAt(i).toString());
        if(!str.equals(""))
        {
          if(!(str.indexOf(rptDesc) != -1))
          {
            pw.println(vecCntl.elementAt(i).toString());
          }
        }
      }

      pw.close();
      fout.close();

      return true;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "delRptFromCntlFile", "", "", "Exception in deleting report to control file (" + cntlFileName + ") - " + e);
      return false;
    }
  }

  // This will delete report in Report set
  public boolean deleteReport(String rptDesc, String reportSetName, String testRun)
  {
    Log.reportDebugLog(className, "deleteReport", "", "", "Deleting report. Test Run is " + testRun + ", Description = " + rptDesc);
    Date start = new Date();

    int numTestRun = Integer.parseInt(testRun.trim());
    String reportFileWithPath = getReportSetPath(reportSetName, numTestRun) + "/" + rptUtilsBean.doReplaceName(rptDesc) + JPEG_EXTN;

    try
    {
      boolean fileFlag = false;
      File rptFile = new File(reportFileWithPath);

      if(rptFile.exists()) // for .jpeg file
        fileFlag = true;
      else
      {
        reportFileWithPath = getReportSetPath(reportSetName, numTestRun) + "/" + rptUtilsBean.doReplaceName(rptDesc) + HTML_EXTN;

        rptFile = new File(reportFileWithPath);

        if(rptFile.exists()) // for .html file
          fileFlag = true;
      }

      boolean success = false;
      if(!fileFlag)
      {
        reportFileWithPath = getReportSetPath(reportSetName, numTestRun) + "/" + rptUtilsBean.doReplaceName(rptDesc);

        rptFile = new File(reportFileWithPath);

        if(rptFile.isDirectory())
        {
          boolean success1 = deleteRptSetDir(reportSetName + "/" + rptUtilsBean.doReplaceName(rptDesc), testRun);

          if (!success1)
          {
            Log.errorLog(className, "deleteReport", "", "", "Error in deleting directory (" + reportFileWithPath + ")");
            return false;
          }
          else
            success1 = delRptFromCntlFile(testRun, reportSetName, rptDesc);
        }
      }
      else
      {
        success = rptFile.delete();
        if (!success)
        {
          Log.errorLog(className, "deleteReport", "", "", "Error in deleting report file (" + reportFileWithPath + ")");
          return false;
        }
        else
          success = delRptFromCntlFile(testRun, reportSetName, rptDesc);
      }

      Log.reportDebugLog(className, "deleteReport", "", "", "Report deletion is complete. Time taken is "+ rptUtilsBean.convMilliSecToStr((new Date()).getTime() - start.getTime()));
    }
    catch(Exception e)
    {
      Log.errorLog(className, "deleteReport", "", "", "Exception in deletion of report file (" + reportFileWithPath + ") - " + e);
      return false;
    }
    return true;
  }

  // This will add report in Report set control file
  public boolean addReport(String reportSetName, String testRun, String rptRec[])
  {
    Log.reportDebugLog(className, "addReport", "", "", "Adding report. Test Run is " + testRun + ", Control file = " + reportSetName + CNTL_EXTN);

    int numTestRun = Integer.parseInt(testRun.trim());
    String cntlFile = getReportSetPath(reportSetName, numTestRun) + "/" + reportSetName + CNTL_EXTN;

    try
    {
      File reportControlFile = new File(cntlFile);

      if(!reportControlFile.exists())
        reportControlFile.createNewFile(); // Create new file

      FileOutputStream fout = new FileOutputStream(reportControlFile, true);  // append mode
      PrintStream pw = new PrintStream(fout);
      String str = "";

      pw.println(rptUtilsBean.strArrayToStr(rptRec, "|"));

      pw.close();
      fout.close();

      Log.reportDebugLog(className, "addReport", "", "", "Report adding is complete.");
    }
    catch(Exception e)
    {
      Log.errorLog(className, "addReport", "", "", "Exception in adding report file (" + cntlFile + ") - " + e);
      return false;
    }
    return true;
  }

  public boolean checkWenEnv(String testRun)
  {
    Log.reportDebugLog(className, "checkWenEnv", "", "", "Checking WEN_ENV. Test Run is " + testRun);

    int numTestRun = Integer.parseInt(testRun.trim());

    try
    {
      String str1 = "";
      String tempWenEnvVal = "0";
      String confFileName = "scenario";

      StringTokenizer st;

      FileInputStream fis = new FileInputStream( Config.getWorkPath() + "/webapps/logs/TR" + numTestRun + "/" + confFileName);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));

      while((str1 = br.readLine()) != null)
      {
        if(str1.indexOf("WAN_ENV") != -1)
        {
          st = new StringTokenizer(str1);
          st.nextToken();
          tempWenEnvVal = st.nextToken().trim();
          // Do not break as last entry in the file is  used by netstorm.
        }
      }

      if(tempWenEnvVal.equals("1"))
        return true;

      return false;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "checkWenEnv", "", "", "Exception while checking keyword - " + e);
      return false;
    }
  }

  public static void main(String[] args)
  {
    ReportSetControl rptCntObj = new ReportSetControl();
    String[] tempA = {"A|B|C|D","AA|BB|CC|DD","AAA|BBB|CCC|DDD"};
    boolean check = rptCntObj.checkWenEnv("589");
    System.out.println(check);
//    String str = ReportSetControl.getReportSetBasePath(695);
//    File str = rptCntObj.openReportSetBasePath(695, false);
//    System.out.println(str);
/*
    int[] tempGrp = {1,2,3,4};
    int[] tempRpt = {1,2,3,4};

    String[][] intTemp = rptCntObj.getRptNameAndIndxs(tempGrp, tempRpt, "695");
    int[] tempIdxFrmFun = rptCntObj.strArrayToIntArray(intTemp[1]);
    for(int j = 0; j < tempIdxFrmFun.length;j++)
    {
      System.out.println("Index - " + tempIdxFrmFun[j]);
      System.out.println("intTemp - " + intTemp[0][j]);
    }
    */
    //boolean check = rptCntObj.delGenInfoToCntFile(695, "ReportControlFile", "abhishek");
//    boolean check1 = rptCntObj.deleteRptSetDir("abhishek", "695");
      //boolean check1 = rptCntObj.deleteReport("abhiabhi", "abhishek", "695");
      //System.out.println(check1);
      String[][] temp = rptCntObj.getReportSets("695");

      for(int j = 0; j < temp.length;j++)
      {
        for(int i =0; i < temp[0].length; i++)
          System.out.println("temp - " + temp[j][i]);
      }

  }
}
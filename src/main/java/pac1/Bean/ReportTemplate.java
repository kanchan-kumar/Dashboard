/*--------------------------------------------------------------------
  @Name    : ReportTemplate.java
  @Author  : Abhishek/Neeraj
  @Purpose : Bean for Report Template Management. Used by Analyze GUI.
  @Modification History:
    01/29/07:Abhishek:1.4.2 - Initial Version

  @Template File Structure:
  Line1 - Template Summary Header Line
  Line2 - Tempalte Summary Information Line
  Line3 - Report Header Line
  Line4 and above = Report Information Line (One per report element)
  Example of Template file is:
    Name|Description|Creation Date|Modified Date
    Template1|Test Template|12/31/2007 10:10:10 AM|01/15/2007 10:10:10 AM
    Report Description|Graph View Type|Type|Granularity|X-Axis Time Format|Time|Selection Time Format|Start Date|Start Time|End Date|End Time|Report Info
    URL Hits|Tile|Normal|Auto|Absolute|Specified Time|Elapsed|NA|00:01:00|NA|00:02:00|RptGrpId=1,51,1001:RptId=1,3,1:SSO=NA,1,NA:SN=NA,NA,NA

  Note - In the template, Selection Time Format is always Elapsed and Start Date/End Date are always NA

----------------------------------------------------------------------*/

package pac1.Bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.ArrayList;

import pac1.Bean.Log;
import pac1.Bean.rptUtilsBean;
import pac1.Bean.GraphName.GraphNames;

public class ReportTemplate
{
  static String className = "ReportTemplate";
  //static String workPath = Config.getWorkPath();
  static FileInputStream fis = null;
  static BufferedReader br = null;

  static int nonRtplCounter = 1;  // This is used to generate unique filename for non rtpl template

  static int  arrFldList[] = {0, 1, 2, 3};  // Array of field list required by rec2FldArr. Used to get summary line in array

  // Move this to RptInfo later
  static String[] serverStat = { "NA", "All", "Specified"};

  private final static String RTPL_EXTN = ".rtpl";
  private final static String RTPL_BAK_EXTN = ".hot"; // This is used to save the template for edit
  private final static String JPEG_EXTN = ".jpeg";
  private final static String RTPL_SUMMARY_HDR_LINE = "Name|Description|Creation Date|Modified Date";
  private final static String RTPL_DETAIL_HDR_LINE = "Report Description|Graph View Type|Graph Type|Granularity|X-Axis Time Format|Time|Time Selection Format|Start Date|Start Time|End Date|End Time|Report Info";
  // Time Selection Format - This will be always Elapsed in Template

  public Vector vecRptData = new Vector();

  public ReportTemplate(){}

  // Common methods
  private static String getTemplatesPath()
  {
    return (Config.getWorkPath() + "/templates/" );
  }
  // This will used for path for Report Set directory
  private static String getTemplatePathForReportSet(int numTestRun, String reportSetName)
  {
    return (Config.getWorkPath() + "/webapps/logs/TR" + numTestRun + "/reports/reportSet/" + reportSetName + "/");
  }

  public static String getTemplateNameWithPath(String templateFileName)
  {
    return (getTemplatesPath() + templateFileName);
  }
  // Open template file and return as File object
  private static File openTemplate(String rtplFileName)
  {
    try
    {
      File rtplFile = new File(getTemplateNameWithPath(rtplFileName));
      return(rtplFile);
    }
    catch(Exception e)
    {
      Log.errorLog(className, "openTemplate", "", "", "Exception - " + e);
      return null;
    }
  }
  // Open template path and return as File object
  private static File openTemplatesPath()
  {
    try
    {
      File rtplFile = new File(getTemplatesPath());
      // Check templates Path exists or not? If not create it and set permission and owenership
      if(!rtplFile.exists())
      {
        if(!rtplFile.mkdir())
        {
          Log.errorLog(className, "openTemplatesPath", "", "", "Error in creating templates directory");
          return null;
        }
        else
        {
          // rtplFile.close();
          // Change ownership and permission of templates directory
          if(rptUtilsBean.changeFilePerm(rtplFile.getAbsolutePath(), "netstorm", "netstorm", "775") == false)
            return null;
        }

      }

      return(rtplFile);
    }
    catch(Exception e)
    {
      Log.errorLog(className, "openTemplatesPath", "", "", "Exception - " + e);
      return null;
    }
  }
  // Methods for reading templates
  public static Vector readTemplateFile(String rtplNameWithPath)
  {
    Log.debugLog(className, "readTemplateFile", "", "", "Method called. Template Name = " + rtplNameWithPath);

    try
    {
      Vector vecRtpl = new Vector();
      String strLine;

      fis = new FileInputStream(rtplNameWithPath);
      br = new BufferedReader(new InputStreamReader(fis));

      while((strLine = br.readLine()) !=  null)
      {
        strLine = strLine.trim();
        if(!strLine.startsWith("#") || (strLine.length() != 0))  // Ignore commented and empty lines
        {
          Log.debugLog(className, "readTemplateFile", "", "", "Adding line in vector. Line = " + strLine);
          vecRtpl.add(strLine);
        }
      }
      br.close();
      fis.close();
      if(vecRtpl.size() < 3)
      {
        Log.errorLog(className, "readTemplateFile", "", "", "Invalid template file. Number of lines in the template file must be >= 3");
        return null;
      }
      return vecRtpl;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "readTemplateFile", "", "", "Exception - " + e);
      e.printStackTrace();
      return null;
    }
  }
  // Get all templates summary information in 2D array.
  // Template summary information is taken from line2 of the the template file.
  public static String[][] getTemplates()
  {
    Log.debugLog(className, "getTemplates", "", "", "Method called");

    try
    {
      String strLine = "";
      String[][] tempArrTemplate = null;
      Vector tempRtpl = new Vector();
      Vector tempRtplSize = new Vector();

      File rtplFile = openTemplatesPath();
      if(rtplFile == null)
        return null;

      // Enhance this to use list(FilenameFilter filter) to get only *.rtpl file ??
      String arrayFiles[] = rtplFile.list();
      // Abhishek (03/13/07): This will sort the template names
      Arrays.sort(arrayFiles);
      // Calculate number of template files
      for(int ii = 0; ii < arrayFiles.length; ii++)
      {
        // Abhishek : use lastIndexOf() function instead indexOf() beacuse reading extn.
        if(arrayFiles[ii].lastIndexOf(RTPL_EXTN) == -1)  // Skip non template files
          continue;
        // Skip template used for generating reports without templates.
        // These are interal temporary templates starting with __
        if(arrayFiles[ii].startsWith("__"))  // Skip internal temporary template files
          continue;
        // Need to handle the case when one file is incorrect
        Vector vecRtpl = readTemplateFile(getTemplateNameWithPath(arrayFiles[ii]));
        // Get template summary information from Line2 (vector index 1)
        if(vecRtpl == null)
          continue;

        tempRtpl.add(vecRtpl.elementAt(1).toString());
        tempRtplSize.add("" + vecRtpl.size());
      }

      if(tempRtpl.size() > 0)
        tempArrTemplate = new String[tempRtpl.size() + 1][5];
      else
        tempArrTemplate = new String[1][5];

      tempArrTemplate[0][0] = "Name";
      tempArrTemplate[0][1] = "Description";
      tempArrTemplate[0][2] = "Creation Date";
      tempArrTemplate[0][3] = "Modified Date";
      tempArrTemplate[0][4] = "Number Of Report Elements";

      int index = 1;  //Index in 2D array for storing template summary info
      for(int ii = 0; ii < tempRtpl.size(); ii++)  // Here loop must be for all files
      {
        rptUtilsBean.rec2FldArr(tempRtpl.elementAt(ii).toString(), tempArrTemplate[index], 4, 0, arrFldList, "NA");
        tempArrTemplate[index][4] = "" + (Integer.parseInt(tempRtplSize.elementAt(ii).toString()) - 3);  // Number of report elements
        index++;
      }

      return tempArrTemplate;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "getTemplates", "", "", "Exception - " + e);
      return null;
    }
  }

  public static String[][] getTemplateDetails(String rtplName)
  {
    return(getTemplateDetails(rtplName, true));
  }
  // Get template sumamry and details (reports) of the template as 2D Array
  // This is always taken from Bak file
  public static String[][] getTemplateDetails(String rtplName, boolean bakFile)
  {
    Log.debugLog(className, "getTemplateDetails", "", "", "TemplateName =" +  rtplName);

    try
    {
      String rtplFileName;
      if(bakFile)
        rtplFileName = getTemplateNameWithPath(rtplName + RTPL_BAK_EXTN);
      else
        rtplFileName = getTemplateNameWithPath(rtplName + RTPL_EXTN);

      Vector vecRtpl = readTemplateFile(rtplFileName);

      String[][] tempArrTemplate = new String[vecRtpl.size()][];

      for(int i = 0; i < vecRtpl.size(); i++)
      {
        String[] stTokenTemplate = vecRtpl.elementAt(i).toString().split("\\|");
        tempArrTemplate[i] = new String[stTokenTemplate.length];

        int j = 0;
        for(int index = 0; index < stTokenTemplate.length; index++)
        {
          tempArrTemplate[i][j] = stTokenTemplate[index];
          j++;
        }
      }
      br.close();
      fis.close();
      return tempArrTemplate;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "getTemplateDetails", "", "", "Exception - " + e);
      return null;
    }
  }
  // Delete methods
  // This delete the existing template
  public static boolean deleteTemplate(String templateName)
  {
    Log.debugLog(className, "deleteTemplate", "", "", "templateName =" +  templateName);
    deleteTemplate(templateName, RTPL_BAK_EXTN);
    return(deleteTemplate(templateName, RTPL_EXTN));
  }
  // This delete the  template backup if present
  public static boolean deleteBakTemplate(String rtplName)
  {
    Log.debugLog(className, "deleteBakTemplate", "", "", "templateName =" +  rtplName);
    return(deleteTemplate(rtplName, RTPL_BAK_EXTN));
  }
  // This delete the existing templates
  private static boolean deleteTemplate(String templateName, String fileExtn)
  {
    Log.debugLog(className, "deleteTemplate", "", "", "templateFile =" +  templateName + "." + fileExtn);
    String tmptFile = getTemplateNameWithPath(templateName + fileExtn);

    try
    {
      boolean success = (new File(tmptFile)).delete();
      if (!success)
      {
        Log.errorLog(className, "deleteTemplate", "", "", "Error in deleting template file (" + tmptFile + ")");
        return false;
      }
    }
    catch(Exception e)
    {
      Log.errorLog(className, "deleteTemplate", "", "", "Exception in deletion of template (" + tmptFile + ") - " + e);
      return false;
    }
    return true;
  }
  // Copy template methods
  // Make Bak file of  template
  // All edit/changes are done in Backup file
  public static boolean createTemplateBackup(String rtplName)
  {
    Log.debugLog(className, "createTemplateBackup", "", "", "TemplateName = " +  rtplName);
    try
    {
      String rtplFileName = getTemplateNameWithPath(rtplName + RTPL_EXTN);
      File rtplFile = new File(rtplFileName);
      if(!rtplFile.exists())
      {
        Log.errorLog(className, "createTemplateBackup", "", "", "Source template does not exist. Template name = " + rtplName);
        return false;
      }
      String rtplBakFileName = getTemplateNameWithPath(rtplName + RTPL_BAK_EXTN);

      if(copyTemplateFile(rtplFileName, rtplBakFileName, "", "", false, false) == false)
      {
        Log.errorLog(className, "createTemplateBackup", "", "", "Error in creating Bak of Template. Template file is " + rtplFileName + ". Backup file is " + rtplBakFileName);
        return false;
      }
      return(true);
    }
    catch(Exception e)
    {
      Log.errorLog(className, "createTemplateBackup", "", "", "Exception in creating template backup (" + rtplName + ") - " + e);
      return false;
    }
  }
  // Make copy of a template with new name and description
  // Also creation date and modification date is new template is set to current date/time
  public static boolean copyTemplate(String rtplSrcName, String rtplDestName, String rtplDestDesc)
  {
    Log.debugLog(className, "copyTemplate", "", "", "SrcName =" +  rtplSrcName + ", DestName = " + rtplDestName + " Description = " + rtplDestDesc);

    try
    {
      String rtplSrcFileName = getTemplateNameWithPath(rtplSrcName + RTPL_EXTN);
      String rtplDestFileName = getTemplateNameWithPath(rtplDestName + RTPL_EXTN);

      File rtplSrcFile = new File(rtplSrcFileName);
      if(!rtplSrcFile.exists())
      {
        Log.errorLog(className, "copyTemplate", "", "", "Source template does not exist. Template name = " + rtplSrcName);
        return false;
      }
      File rtplDestFile = new File(rtplDestFileName);
      if(rtplDestFile.exists())
      {
        Log.errorLog(className, "copyTemplate", "", "", "Destination template already exists. Template name = " + rtplDestName);
        return false;
      }

      if(copyTemplateFile(rtplSrcFileName, rtplDestFileName, rtplDestName, rtplDestDesc, true, true) == false)
      {
        Log.errorLog(className, "copyTemplate", "", "", "Errro in copying Template. Source file is " + rtplSrcFileName + ". Destination file is " + rtplDestFileName);
        return false;
      }
      return true;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "copyTemplate", "", "", "Exception in copying template (" + rtplSrcName + ") - " + e);
      return false;
    }
  }
  public static boolean copyTemplateToReportSet(String testRun, String rtplSrcName, String rtplDestName, String rtplDestDesc, String reportSetName)
  {
    Log.debugLog(className, "copyTemplateToReportSet", "", "", "SrcName =" +  rtplSrcName + ", DestName = " + rtplDestName + " Description = " + rtplDestDesc);
    return(copyTemplateToReportSet(testRun, rtplSrcName, rtplDestName, rtplDestDesc, reportSetName, false));
  }
  // This will copy template to report set directory
  // by which report set is generated for test run
  public static boolean copyTemplateToReportSet(String testRun, String rtplSrcName, String rtplDestName, String rtplDestDesc, String reportSetName, boolean changePerm)
  {
    Log.debugLog(className, "copyTemplateToReportSet", "", "", "SrcName =" +  rtplSrcName + ", DestName = " + rtplDestName + " Description = " + rtplDestDesc);
    int numTestRun = Integer.parseInt(testRun);

    try
    {
      String rtplSrcFileName = getTemplateNameWithPath(rtplSrcName + RTPL_EXTN);
      String rtplDestFileName = getTemplatePathForReportSet(numTestRun, reportSetName) + (rtplDestName + RTPL_EXTN);

      File rtplSrcFile = new File(rtplSrcFileName);
      if(!rtplSrcFile.exists())
      {
        Log.errorLog(className, "copyTemplateToReportSet", "", "", "Source template does not exist. Template name = " + rtplSrcName);
        return false;
      }
      File rtplDestFile = new File(rtplDestFileName);

      if(changePerm == false)
        if(rtplDestFile.exists())
        {
          Log.errorLog(className, "copyTemplateToReportSet", "", "", "Destination template already exists. Template name = " + rtplDestName);
          return false;
        }
      else
      {
        if(rtplDestFile.exists())
        {
          rtplDestFile.delete();
          rtplDestFile.createNewFile();
        }
        else
          rtplDestFile.createNewFile();
      }
      // Abhishek (03/14/07) - This will copy file from source to destination with checking permisson flag
      if(copyFileToRptSet(rtplSrcFileName, rtplDestFileName, changePerm) == false)
      {
        Log.errorLog(className, "copyFileToRptSet", "", "", "Errro in copying Template. Source file is " + rtplSrcFileName + ". Destination file is " + rtplDestFileName);
        return false;
      }
      return true;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "copyTemplateToReportSet", "", "", "Exception in copying template (" + rtplSrcName + ") - " + e);
      return false;
    }
  }
  // Abhishek (03/14/07) - To copy file from source to destination with permisson flag
  private static boolean copyFileToRptSet(String srcFileName, String destFileName, boolean changePerm)
  {
    Log.debugLog(className, "copyFileToRptSet", "", "", "Method called");
    try
    {
      File fileSrc = new File(srcFileName);
      File fileDest = new File(destFileName);

      if(!fileSrc.exists())
      {
        Log.errorLog(className, "copyTemplateFile", "", "", "Source file does not exists. Filename = " + srcFileName);
        return false;
      }

      if(fileDest.exists())
        fileDest.delete();
      fileDest.createNewFile();
      // Checking whether user is root or not, if it is not root then change permisson
      if(changePerm)
        if(rptUtilsBean.changeFilePerm(fileDest.getAbsolutePath(), "netstorm", "netstorm", "775") == false)
          return false;

      FileInputStream fin = new FileInputStream(fileSrc);
      BufferedReader br = new BufferedReader(new InputStreamReader(fin));
      FileOutputStream fout = new FileOutputStream(fileDest, true);  // append mode
      PrintStream pw = new PrintStream(fout);
      String str;

      int lineNum = 0;
      while((str = br.readLine()) != null)
      {
        pw.println(str);
        lineNum++;
      }

      pw.close();
      br.close();
      fin.close();
      fout.close();
      return true;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "copyFileToRptSet", "", "", "Exception - " + e);
      return false;
    }
  }

  // To check if template is modified or note after last save was done
  public static boolean isTemplateModified(String rtplName)
  {
    Log.debugLog(className, "isTemplateModified", "", "", "Method called. Template Name = " + rtplName);
    try
    {

      String rtplFileName = getTemplateNameWithPath(rtplName + RTPL_EXTN);
      String rtplBakFileName = getTemplateNameWithPath(rtplName + RTPL_BAK_EXTN);

      Vector vecRtpl = readTemplateFile(rtplFileName);
      Vector vecBakRtpl = readTemplateFile(rtplBakFileName);

      String arrRtplSummary[] = new String[4];
      String arrBakRtplSummary[] = new String[4];

      rptUtilsBean.rec2FldArr(vecRtpl.elementAt(1).toString(), arrRtplSummary, 4, 0, arrFldList, "NA");
      rptUtilsBean.rec2FldArr(vecBakRtpl.elementAt(1).toString(), arrBakRtplSummary, 4, 0, arrFldList, "NA");
      Log.debugLog(className, "isTemplateModified", "", "", "Modification Date/Time for Template and Bak file are - " + arrRtplSummary[3] + " and " + arrBakRtplSummary[3]);
      // Open file for compare modification time
//      File f1 = new File(rtplFileName);
//      File f2 = new File(rtplBakFileName);
//      if((f1.lastModified()/1000) == (f2.lastModified()/1000))
//        return false;

      // Compare modified file
      if((rptUtilsBean.convertDateToMilliSec(arrRtplSummary[3])) == (rptUtilsBean.convertDateToMilliSec(arrBakRtplSummary[3])))
        return false;

      return true;

    }
    catch(Exception e)
    {
      Log.errorLog(className, "isTemplateModified", "", "", "Exception - " + e);
      return false;
    }
  }

  // Copy source template file to destination file.
  // If destName is not not empty, dest file template name will have this name
  // If destDesc is not not empty, dest file template desc will have this desc
  // If updCreDate and updModDate are true, then current date/time is used for cre/mod date/time
  private static boolean copyTemplateFile(String srcFileName, String destFileName, String destName, String destDesc, boolean updCreDate, boolean updModDate)
  {
    Log.debugLog(className, "copyTemplateFile", "", "", "Method called");
    try
    {
      File fileSrc = new File(srcFileName);
      File fileDest = new File(destFileName);

      if(!fileSrc.exists())
      {
        Log.errorLog(className, "copyTemplateFile", "", "", "Source file does not exists. Filename = " + srcFileName);
        return false;
      }

      if(fileDest.exists())
        fileDest.delete();
      fileDest.createNewFile();

      if(rptUtilsBean.changeFilePerm(fileDest.getAbsolutePath(), "netstorm", "netstorm", "775") == false)
        return false;

      FileInputStream fin = new FileInputStream(fileSrc);
      BufferedReader br = new BufferedReader(new InputStreamReader(fin));
      FileOutputStream fout = new FileOutputStream(fileDest, true);  // append mode
      PrintStream pw = new PrintStream(fout);
      String str;

      int lineNum = 0;
      while((str = br.readLine()) != null)
      {
        if(lineNum == 1)  // Template summary info line
          str = getTemplateSummaryLine(str, destName, destDesc, updCreDate, updModDate);
        pw.println(str);
        lineNum++;
      }

      pw.close();
      br.close();
      fin.close();
      fout.close();
      return true;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "copyTemplateFile", "", "", "Exception - " + e);
      return false;
    }
  }
  // This will delete selected reports from a template
  // selected reports are passed as array of index. Index for first report is 3
  public static boolean deleteRptFromTemplate(String rtplName, String[] arrRptIdx)
  {
    Log.debugLog(className, "deleteRptFromTemplate", "", "", "templateName = " + rtplName);

    String rtplNameWithPath = getTemplateNameWithPath(rtplName + RTPL_BAK_EXTN);
    try
    {
      // Need to handle the case when one file is incorrect
      Vector vecRtpl = readTemplateFile(rtplNameWithPath);

      File rtplFile = new File(rtplNameWithPath);

      rtplFile.delete(); // Delete template file
      rtplFile.createNewFile(); // Create new template file
      if(rptUtilsBean.changeFilePerm(rtplFile.getAbsolutePath(), "netstorm", "netstorm", "775") == false)
        return false;

      FileOutputStream fout = new FileOutputStream(rtplFile, true);  // append mode
      PrintStream pw = new PrintStream(fout);
      String status = "";

      for(int i = 0; i < vecRtpl.size(); i++)
      {
        status = "";
        // This will update modification date/time of file
        if(i == 1)
        {
          String str = getTemplateSummaryLine(vecRtpl.elementAt(i).toString(), "", "", false, true);
          pw.println(str);
        }
        else if(i <= 2 && i != 1)
          pw.println(vecRtpl.elementAt(i).toString());
        else
        {
          for(int k = 0; k < arrRptIdx.length; k++)
          {
            if(i == (int)Integer.parseInt(arrRptIdx[k]))
            {
              status = "true";
              break;
            }
          }
          if(!status.equals("true"))
            pw.println(vecRtpl.elementAt(i).toString());
        }
      }

      pw.close();
      fout.close();
      return true;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "deleteRptFromTemplate", "", "", "Exception in deleting report from template of (" + rtplNameWithPath + ") - " + e);
      return false;
    }
  }
  public static boolean addRptToTemplate(String rtplName, String rptDetail)
  {
    Log.debugLog(className, "addRptToTemplate", "", "", "templateName =" + rtplName);
    return(addRptToTemplate(rtplName, rptDetail, false));
  }
  // Add Report to Template file. Add is done to backup file only
  public static boolean addRptToTemplate(String rtplName, String rptDetail, boolean fileType)
  {
    Log.debugLog(className, "addRptToTemplate", "", "", "templateName =" + rtplName);
    String rtplNameWithPath = "";
    if(fileType)
      rtplNameWithPath = getTemplateNameWithPath(rtplName + RTPL_EXTN);
    else
      rtplNameWithPath = getTemplateNameWithPath(rtplName + RTPL_BAK_EXTN);

    try
    {
      // Read file's content
      Vector vecRtpl = readTemplateFile(rtplNameWithPath);

      File rtplFile = new File(rtplNameWithPath);
      rtplFile.delete(); // Delete template file
      rtplFile.createNewFile(); // Create new template file
      if(rptUtilsBean.changeFilePerm(rtplFile.getAbsolutePath(), "netstorm", "netstorm", "775") == false)
        return false;

      if(!rtplFile.exists())
      {
        Log.errorLog(className, "addRptToTemplate", "", "", "Template file does not exist. Template filename is - " + rtplNameWithPath);
        return(false);
      }

      FileOutputStream fout = new FileOutputStream(rtplFile, true);  // append mode
      PrintStream pw = new PrintStream(fout);
      for(int i = 0; i < vecRtpl.size(); i++)
      {
        if(i == 1)
        {
          String str = getTemplateSummaryLine(vecRtpl.elementAt(i).toString(), "", "", false, true);
          pw.println(str);
        }
        else
          pw.println(vecRtpl.elementAt(i).toString());
      }
      pw.println(rptDetail);  // Append the new report to the end of file
      pw.close();
      fout.close();
      return true;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "addRptToTemplate", "", "", "Exception in adding report to template (" + rtplNameWithPath + ") - " + e);
      return false;
    }
  }
  // Edit Record to Template file
  public static boolean editRptInTemplate(String rtplName, int rptRowId, String rptDetail)
  {
    Log.debugLog(className, "editRptInTemplate", "", "", "templateFile =" + rtplName);
    String tmptFile = getTemplateNameWithPath(rtplName + RTPL_BAK_EXTN);
    Vector vc = new Vector();

    try
    {
      String[] arrRptIdx = new String[1];
      arrRptIdx[0] = "" + rptRowId;

      boolean checkDel = deleteRptFromTemplate(rtplName, arrRptIdx);
      if(checkDel)
        addRptToTemplate(rtplName, rptDetail);
      else
        return false;

      return true;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "editRptInTemplate", "", "", "Exception in editing record to template (" + tmptFile + ") - " + e);
      return false;
    }
  }


  public static String  getNonRtplTemplateName(String testRun, String sessionId)
  {
    String nonRtplFile = "__" + testRun + "_" + sessionId + "_" + nonRtplCounter; // This name will be used for temporary template which used for report generation
    nonRtplCounter++;
    return(nonRtplFile);
  }

  // Prabhat - This Function has following limitation
  // Issue - After Generate the Reports if user delete it (In Multiple Part) than the entry will be appear in the Template.
  public static boolean createNewTemplateUsingNonRtplRptSet(String testRun, String rtplName, String rtplDesc)
  {
    Log.debugLog(className, "createNewTemplateUsingNonRtplRptSet", "", "", "Method called. Test Run =" +  testRun + ", Template Name = " + rtplName + ", Desc = " + rtplDesc);
    Vector tempJpegFiles = new Vector();

    try
    {
      if(createNewTemplate(rtplName, rtplDesc, true) == false)
        return(false);

      String rptSetPath = ReportSetControl.getReportSetPathForNonRtpl(testRun);
      File rptSetFile = new File(rptSetPath);
      // Check report Set Path exists or not?
      if(!rptSetFile.exists())
      {
        Log.errorLog(className, "createNewTemplateUsingNonRtplRptSet", "", "", "Warning: Report set does not exits. Ignored");
        return true;
      }

      // Enhance this to use list(FilenameFilter filter) to get only *.rtpl file ??
      String arrayFiles[] = rptSetFile.list();

      for(int ii = 0; ii < arrayFiles.length; ii++)
      {
        // Use lastIndexOf() function instead indexOf() beacuse reading extn.
        if(arrayFiles[ii].lastIndexOf(JPEG_EXTN) == -1)  // Skip non template files
          continue;
        tempJpegFiles.add(arrayFiles[ii].substring(0, arrayFiles[ii].indexOf(JPEG_EXTN)));
      }
      String[] tempJpegFilesArr = rptUtilsBean.getUniqueValArray(tempJpegFiles);
      // Calculate number of template files
      for(int ii = 0; ii < arrayFiles.length; ii++)
      {
        // Use lastIndexOf() function instead indexOf() beacuse reading extn.
        if(arrayFiles[ii].lastIndexOf(RTPL_EXTN) == -1)  // Skip non template files
          continue;
        // Need to handle the case when one file is incorrect
        Vector vecRtpl = readTemplateFile(rptSetPath + "/" + arrayFiles[ii]);
        if((vecRtpl == null) || (vecRtpl.size() <= 3))
          continue;
        for(int i = 3; i < vecRtpl.size(); i++)
        {
          String tempElement = vecRtpl.elementAt(i).toString();
          for(int jj = 0; jj < tempJpegFilesArr.length; jj++)
          {
            String[] temp = rptUtilsBean.strToArrayData(tempElement, "|");
            if((rptUtilsBean.undoReplaceName(tempJpegFilesArr[jj])).startsWith(temp[0]))
            {
              if(addRptToTemplate(rtplName, vecRtpl.elementAt(i).toString(), true) == false)
                return false;
              break;
            }
          }
        }
      }
      return true;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "createNewTemplateUsingNonRtplRptSet", "", "", "Exception - " + e);
      return false;
    }
  }

  public static boolean createNewTemplate(String rtplName, String rtplDesc)
  {
    Log.debugLog(className, "createNewTemplate", "", "", "TemplateName =" + rtplName + ", Description = " + rtplDesc);
    return(createNewTemplate(rtplName, rtplDesc, false));
  }
  // Create new template. New template is always create as bak file.
  public static boolean createNewTemplate(String rtplName, String rtplDesc, boolean checkFileType)
  {
    Log.debugLog(className, "createNewTemplate", "", "", "TemplateName =" + rtplName + ", Description = " + rtplDesc);
    String rtplFileName = "";

    try
    {
      // This will allow user to create template file with rtpl extn.
      if(checkFileType == true)
        rtplFileName = getTemplateNameWithPath(rtplName + RTPL_EXTN);
      else
        rtplFileName = getTemplateNameWithPath(rtplName + RTPL_BAK_EXTN);

      File rtplFile = new File(rtplFileName);
      if(rtplFile.exists())
      {
        Log.errorLog(className, "createNewTemplate", "", "", "Report template already exists. Tempalte name = " + rtplName);
        return false;
      }
      rtplFile.createNewFile();
      if(rptUtilsBean.changeFilePerm(rtplFile.getAbsolutePath(), "netstorm", "netstorm", "775") == false)
        return false;
      FileOutputStream fout = new FileOutputStream(rtplFile, true);  // append mode
      PrintStream pw = new PrintStream(fout);

      pw.println(RTPL_SUMMARY_HDR_LINE);
      pw.println(rtplName + "|" + rtplDesc + "|" + rptUtilsBean.getCurDateTime() + "|" + rptUtilsBean.getCurDateTime());
      pw.println(RTPL_DETAIL_HDR_LINE);

      pw.close();
      fout.close();
      return true;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "createNewTemplate", "", "", "Exception in creating new template (" + rtplName + ") - " + e);
      return false;
    }
  }
  // Save template will delete tempalte file and copy bak file to template file
  // Bak is not deleted as GUI needs this file. Mod date/time is also updated in template file.
  // Issue - When do we delete Bak file???
  public static boolean saveTemplate(String rtplName, String rtplDesc)
  {
    Log.debugLog(className, "saveTemplate", "", "", "templateFile =" + rtplName + ", Description = " + rtplDesc);

    try
    {
      // First check if Bak file is existing or not. If not create new template Bak file
      String rtplBakFileName = getTemplateNameWithPath(rtplName + RTPL_BAK_EXTN);
      File rtplBakFile = new File(rtplBakFileName);
      if(!rtplBakFile.exists())
        createNewTemplate(rtplName, rtplDesc);
      // Now copy Bak file to template file to save the changes
      String rtplFileName = getTemplateNameWithPath(rtplName + RTPL_EXTN);
      if(copyTemplateFile(rtplBakFileName, rtplFileName, "", rtplDesc, false, true) == false)
      {
        Log.errorLog(className, "saveTemplate", "", "", "Errro in copying Bak to Template (" + rtplFileName + ")");
        return false;
      }
      // Now copy tempalt to Bak file so that Bak is same as template file
      if(copyTemplateFile(rtplFileName, rtplBakFileName, "", "", false, false) == false)
      {
        Log.errorLog(className, "saveTemplate", "", "", "Errro in copying templat to Bak (" + rtplFileName + ")");
        return false;
      }

      return true;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "saveTemplate", "", "", "Exception in saving template (" + rtplName + ") - " + e);
      return false;
    }
  }

  private static String getTemplateSummaryLine(String rtplSumInfo, String rtplNewName, String rtplNewDesc, boolean updCreDate, boolean updModDate)
  {
    Log.debugLog(className, "getTemplateSummaryLine", "", "", "Template summary line =" + rtplSumInfo);

    try
    {
      StringTokenizer st = new StringTokenizer(rtplSumInfo, "|");
      String rtplName = st.nextToken();
      String rtplDesc = st.nextToken();
      String rtplCreDate = st.nextToken();
      String rtplModDate = st.nextToken();

      if(!rtplNewName.equals(""))
        rtplName = rtplNewName;
      if(!rtplNewDesc.equals(""))
        rtplDesc = rtplNewDesc;
      if(updCreDate)
        rtplCreDate = rptUtilsBean.getCurDateTime();
      if(updModDate)
        rtplModDate = rptUtilsBean.getCurDateTime();
      return(rtplName + "|" + rtplDesc + "|" + rtplCreDate + "|" + rtplModDate);
    }
    catch(Exception e)
    {
      Log.errorLog(className, "getTemplateSummaryLine", "", "", "Exception -" + e);
      return("AA|BB|CC|DD");
    }
  }
  // Returns detail of a report from template (Report information line)
  public static String[] getRptDetailFromTemplate(String rtplName, int rptIdx)
  {
    Log.debugLog(className, "getRptDetailFromTemplate", "", "", "templateName =" + rtplName + ", RptIndex = " + rptIdx);

    String rtplNameWithPath = getTemplateNameWithPath(rtplName + RTPL_BAK_EXTN);
    try
    {
      Vector vecRtpl = readTemplateFile(rtplNameWithPath);
      return(rptUtilsBean.strToArrayData(vecRtpl.elementAt(rptIdx).toString(), "|"));
    }
    catch(Exception e)
    {
      Log.errorLog(className, "getRptDetailFromTemplate", "", "", "Exception - " + e);
      return null;
    }
  }

  // This will convert Report Info of a report in 2D Array
  public static String[][] rptInfoToArr(String rptInfo, boolean convSSOToString)
  {
    Log.debugLog(className, "rptInfoToArr", "", "", "Getting graph details from template.");
    String[][] arrTemp = null;

    try
    {
      String[] strGrpGrhDetl = rptUtilsBean.strToArrayData(rptInfo, ":");
      arrTemp = new String[strGrpGrhDetl.length][];

      for(int i = 0; i < strGrpGrhDetl.length; i++)
      {
        String[] strGrpGrhToken = rptUtilsBean.strToArrayData(strGrpGrhDetl[i], "=");
        arrTemp[i] = rptUtilsBean.strToArrayData(strGrpGrhToken[1], ",");
      }
      if(convSSOToString)
        return(getRptInfoSerTypeName(arrTemp));
      else
        return arrTemp;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "rptInfoToArr", "", "", "Exception in getting detail -" + e);
      return null;
    }
  }

  /* This will convert Report Info of a report in 2D Array for derived reports
   * input to this method will be in the form i.e 10020.1.[Apache_SVC_time] or 7.2.MiscErr 
   */
   
  public static String[][] rptInfoToArrForDerived(String derivedExp, boolean convSSOToString)
  {
    Log.debugLog(className, "rptInfoToArrForDerived", "", "", "Derived expression = " + derivedExp);
    //  encoding the expression in hash code i.e 1.2.#1
    //derivedExp = ParseDerivedExp.parseSavedDerivedExpression(derivedExp);
    //return rptInfoToArrForDerived(derivedExp, convSSOToString, ParseDerivedExp.hmCounter);
    return rptInfoToArrForDerived(derivedExp, convSSOToString, null);//Passing hmCounter as null
  }
  
  public static String[][] rptInfoToArrForDerived(String derivedExp, boolean convSSOToString, HashMap hmap)
  {
    Log.debugLog(className, "rptInfoToArrForDerived", "", "", "Getting graph details from derived expression = " + derivedExp);
    String[][] arrTemp = null;

    try
    {
      ArrayList rptGrpId = new ArrayList();
      ArrayList rptId = new ArrayList();
      ArrayList vecName = new ArrayList();

      //StringTokenizer stTemp = new StringTokenizer(derivedExp, "(+-*/)");
      String[] stTemp = DerivedData.getTokens(derivedExp);
      int i = 1;
      for(int j = 0; j < stTemp.length; j++)
      {
        String strToken = stTemp[j].trim();
        // need to replca special charactes with the blank
        strToken = strToken.replace("{", "");
        strToken = strToken.replace("}", "");
        strToken = strToken.replace("(", "");
        strToken = strToken.replace(")", "");
        strToken = strToken.replace("[", "");
        strToken = strToken.replace("]", "");
        String[] arrTempOp = rptUtilsBean.strToArrayData(strToken, ".");

        // check for greater than 2 because server name can also have ".", like 192.168.18.106
        if(arrTempOp.length > 2)
        {
          String strVecName = "";
          for(int ii = 0; ii < arrTempOp.length; ii++)
          {
            if(ii == 0 || ii == 1)
            {
              arrTempOp[ii] = ParseDerivedExp.replaceDerivedFunName(arrTempOp[ii]);
            }
            
            if(ii == 0)
              rptGrpId.add(arrTempOp[ii]);
            else if(ii == 1)
              rptId.add(arrTempOp[ii]);
            else
            {
              // here in the arrTempOp, vector name will be in Hash format, we need to decode it with its actual value
              String strVectorName = arrTempOp[ii];
              /*if(strVectorName.startsWith("#") && hmap != null && hmap.containsKey(strVectorName))
                strVectorName = hmap.get(strVectorName).toString();*/

              Log.debugLog(className, "getGraphNumbersFromDerivedFormula", "", "", "vectName = " + strVectorName);
              strVectorName = rptUtilsBean.replace(strVectorName, "[", ""); 
              strVectorName = rptUtilsBean.replace(strVectorName, "]", "");
                  
              if(strVecName.equals(""))
                strVecName = strVectorName;
              else
                strVecName = strVecName + "." + strVectorName;
            }
          }
          vecName.add(strVecName);
        }
      }

      arrTemp = new String[4][rptGrpId.size()];
      for(int j = 0; j < rptGrpId.size(); j++)
      {
        arrTemp[0][j] = rptGrpId.get(j).toString();
        arrTemp[1][j] = rptId.get(j).toString();
        arrTemp[3][j] = vecName.get(j).toString();
        if(arrTemp[3][j].equals("NA"))
          arrTemp[2][j] = "NA";
        else if(arrTemp[3][j].equals("All"))
          arrTemp[2][j] = "1";
        else
          arrTemp[2][j] = "2";
      }

      if(convSSOToString)
        return(getRptInfoSerTypeName(arrTemp));
      else
        return arrTemp;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "rptInfoToArrForDerived", "", "", "Exception in getting detail -" + e);
      return null;
    }
  }

  // This will return graph detail with server in 2D array form of each row of template.
  private static String[][] getRptInfoSerTypeName(String[][] graphRptInfo)
  {
    Log.debugLog(className, "getRptInfoSerTypeName", "", "", "Getting graph details from template.");
    String[][] arrTemp = null;
    GraphNames graphNames = new GraphNames(-1);

    try
    {
      arrTemp = new String[graphRptInfo.length + 2][graphRptInfo[0].length];

      for(int i = 0; i < graphRptInfo[0].length; i++)
      {
        arrTemp[0][i] = graphRptInfo[0][i];
        arrTemp[1][i] = graphRptInfo[1][i];
        arrTemp[2][i] = graphRptInfo[2][i];
        arrTemp[3][i] = graphRptInfo[3][i];
        if(arrTemp[2][i].equals("1") || arrTemp[2][i].equals("2"))
        {
          arrTemp[4][i] = serverStat[Integer.parseInt(arrTemp[2][i].trim())];
        }
        else
        {
          arrTemp[4][i] = serverStat[0];
        }
        arrTemp[5][i] = RptInfo.getRptName(Integer.parseInt(arrTemp[0][i]), Integer.parseInt(arrTemp[1][i]), graphNames);
      }
      return arrTemp;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "getRptInfoSerTypeName", "", "", "Exception in getting detail -" + e);
      return null;
    }
  }

  public static String rptInfoToString(String[] graphInfo)
  {
    Log.debugLog(className, "rptInfoToString", "", "", "Method called");
    String[][] arrTemp = null;
    String strTemp = null;
    String[] strTempArr = new String[4];

    try
    {
      arrTemp = new String[graphInfo.length][4];

      for(int i = 0; i < graphInfo.length; i++)
      {
        arrTemp[i] = rptUtilsBean.strToArrayData(graphInfo[i], "|");
      }

      arrTemp = rptUtilsBean.swapRowCol(arrTemp);

      strTemp = "RptGrpId=" + rptUtilsBean.strArrayToStr(arrTemp[0], ",") + ":RptId=" + rptUtilsBean.strArrayToStr(arrTemp[1], ",") + ":SSO=" + rptUtilsBean.strArrayToStr(arrTemp[2], ",") + ":SN=" + rptUtilsBean.strArrayToStr(arrTemp[3], ",");

      return strTemp;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "rptInfoToString", "", "", "Exception in getting detail -" + e);
      return "";
    }
  }

  public void addReprtDataToVector(String rptData)
  {
    vecRptData.add(vecRptData);
  }


  //This will genrate Template for analyze window:Atul
  public static boolean genRptTemplateForAnalyze(String templateName, String templateDesc, String graphDetail)
  {
    Log.debugLog(className, "genRptTemplateForAnalyze", "", "", "Method called");
    try
    {
      String tempRtplFileName = getTemplateNameWithPath(templateName + RTPL_EXTN);

      File tempRtplFile = new File(tempRtplFileName);
      if(tempRtplFile.exists())
        tempRtplFile.delete();

      if(!createNewTemplate(templateName,templateDesc,true))
      {
        Log.errorLog(className, "genRptTemplateForAnalyze", "", "", "Can not create new template");
        return false;
      }
      else
        return addRptToTemplate(templateName, graphDetail, true);
    }
    catch(Exception e)
    {
      Log.errorLog(className, "genRptTemplateForAnalyze", "", "", "Exception in genRptTemplateForAnalyze() -" + e);
      return false;
    }
  }

  public static void main(String[] args)
  {
 //   String str[][] = RptInfo.getRptNames("");
    //  String str[][] = ReportTemplate.getGraphRptInfo("RptGrpId=1,:RptId=1:SSO=NA:SN=NA");
    //  String str[][] = ReportTemplate.getRptGrpNames("695");
    //  String strstr[] = {"A","B","C","D"};
    //  String graph = rptUtilsBean.strArrayToStr(strstr, ",");
    //  System.out.println(" graph- " + graph);

    //  String strSert[][] = ReportTemplate.getRptInfoSerTypeName(str);
    //  String strSert[][] = ReportTemplate.getRptInfoWithNameAndIndx(str);

    //  for(int i = 0; i < strSert.length; i++)
    //    for(int j = 0; j < strSert[0].length;j++)
    //      System.out.println("Server type name - " + strSert[i][j]);
 //   String[] tempA = {"A|B|C|D","AA|BB|CC|DD","AAA|BBB|CCC|DDD"};
 //   ReportTemplate.copyTemplateToReportSet("695", "Template1", "Template1","", "abhi");
    ReportTemplate.createNewTemplateUsingNonRtplRptSet("29232", "VashistTest", "TestingVashist");
 //   boolean check = ReportTemplate.isTemplateModified("Template1");
 //   System.out.println("is modified-"+check);
  }
}

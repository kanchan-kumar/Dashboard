/*--------------------------------------------------------------------
  @Name      : BatchJobs.java
  @Author    : Ankit Khanijau
  @Purpose   : Provided the utility function(s) to batch jobs GUI(jsp)
  @Modification History:
      05/14/2012 -> Ankit Khanijau (Initial Version)

----------------------------------------------------------------------*/

package pac1.Bean;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

public class BatchJobs
{
  private static String className = "BatchJobs";
  private static String workPath = Config.getWorkPath();
  private final static String BJOBS_FILE_EXTN = ".bjobs";
  private final static String BJOBS_BAK_EXTN = ".hot"; // This is used to save the batch jobs for backup.
  private final static String HIDDEN_FILE_PARAM = ".";

  private final static String BJOBS_DIR_NAME = "batch_jobs";
  private final static String WIN_FILE_SEPERATOR = "/";

  private transient final String BJOB_DESC = "BJOB_DESC = ";
  private transient final String LAST_MODIFIED_DATE = "LAST_MODIFIED_DATE = ";

  private transient final int CUSTOM_VECTOR_NAME_INDEX = 3;
  private transient final int STANDARD_VECTOR_NAME_INDEX = 2;
  private static String topologyName = "";

  public String getTopologyName() {
    return topologyName;
  }

  public void setTopologyName(String topologyName) {
    this.topologyName = topologyName;
  }

  // This will return the Batch Jobs dir path
  private static String getBatchJobGroupsPath()
  {
    Log.debugLog(className, "getMonitorPath", "", "", "Method called.:" + workPath + WIN_FILE_SEPERATOR
        + BJOBS_DIR_NAME + WIN_FILE_SEPERATOR + topologyName + WIN_FILE_SEPERATOR );
    
    if(topologyName != null && !topologyName.trim().equals(""))
      return (workPath  +  WIN_FILE_SEPERATOR + BJOBS_DIR_NAME + WIN_FILE_SEPERATOR + topologyName 
          + WIN_FILE_SEPERATOR );
    else
      return (workPath + WIN_FILE_SEPERATOR + BJOBS_DIR_NAME + WIN_FILE_SEPERATOR);
  }

  //This returns full path of batch jobs files
  private static String getBatchJobNameWithPath(String fileName)
  {
    return (getBatchJobGroupsPath() + fileName);
  }

  // This will return the bak bjobs name
  private String getBakBJOBName(String fileName)
  {
    return (getBatchJobGroupsPath() + HIDDEN_FILE_PARAM + fileName + BJOBS_FILE_EXTN + BJOBS_BAK_EXTN);
  }

  // Open file and returns File object
  private static File openFile(String fileName)
  {
    try
    {
       File tempFile = new File(fileName);
       return(tempFile);
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "openFile", "", "", "Exception - ", e);
      return null;
    }
  }

  private static Vector readFile(String batchJobWithPath)
  {
    return readFile(batchJobWithPath, false);
  }

  // Method for reading the File
  private static Vector readFile(String batchJobWithPath, boolean isReadingDataFromBJOB)
  {
    Log.debugLog(className, "readFile", "", "", "Method called. Custom Monitor Profile File Name = " + batchJobWithPath + ", isReadingDataFromBJOB = " + isReadingDataFromBJOB);
    try
    {
      Vector vecFileData = new Vector();
      String strLine;

      File batchJobObj = openFile(batchJobWithPath);

      if(!batchJobObj.exists())
      {
        Log.errorLog(className, "readFile", "", "", "Batch job not found, filename - " + batchJobWithPath);
        return null;
      }

      FileInputStream fis = new FileInputStream(batchJobWithPath);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));

      while((strLine = br.readLine()) !=  null)
      {
        strLine = strLine.trim();
        //if(strLine.startsWith("#"))
         // continue;
        if(strLine.length() == 0)
          continue;

        Log.debugLog(className, "readFile", "", "", "Reading encoded line. Line = " + strLine);

        /**decode pattern.
         * The alphanumeric characters "a" through "z", "A" through "Z" and "0" through "9" remain the same.
         * The special characters ".", "-", "*", and "_" remain the same.
         * The plus sign "+" is converted into a space character " " .
         * A sequence of the form "%xy" will be treated as representing a byte where xy is the two-digit hexadecimal representation of the 8 bits. Then, all substrings that contain one or more of these byte sequences consecutively will be replaced by the character(s) whose encoding would result in those consecutive bytes. The encoding scheme used to decode these characters may be specified, or if unspecified, the default encoding of the platform will be used.
        **/

        String decodedPattern = strLine;
        if(isReadingDataFromBJOB)
        {
          try
          {
            decodedPattern = URLDecoder.decode(decodedPattern);
          }
          catch(Exception e)
          {
            decodedPattern = strLine;
            Log.debugLog(className, "readFile", "", "", batchJobWithPath + "is not decoded. Its contain special character as " + strLine);
          }
          Log.debugLog(className, "readFile", "", "", "Adding decoded line in vector. Line = " + decodedPattern);
        }

        vecFileData.add(decodedPattern);
      }

      br.close();
      fis.close();

      return vecFileData;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "readFile", "", "", "Exception - ", e);
      return null;
    }
  }

  /* load all mprof file in Array List
   *
   * @param : NA
   * @return : return Arraylist of all BJOBs under $NS_WDIR/batch_jobs/ dir in sorted order.
   */
  private ArrayList loadAllBJOBGroups()
  {
    Log.debugLog(className, "loadAllBJOBGroups", "", "", "Method called");

    ArrayList arrListAllBJOB = new ArrayList();  // Names of all BJOB Groups files
    ArrayList arrListOfAllBJOBSortedOrder = new ArrayList();  // Names of all BJOB Groups in sorted order

    File bjobFile = new File(getBatchJobGroupsPath());
    String temp;
    String tempPath;
    
    if(topologyName.trim().equals(""))
    {
      if(bjobFile == null)
        return null;
    }
    else
    {
      if(!bjobFile.exists())
      {
        bjobFile.mkdirs();  
        
        if(rptUtilsBean.changeFilePerm(bjobFile.getAbsolutePath(), "netstorm", "netstorm", "775") == false)
          return null;     
      } 
    }
    try
    {
      String arrAvailFiles[] = bjobFile.list();
      tempPath = getBatchJobGroupsPath();

      for(int j = 0; j < arrAvailFiles.length; j++)
      {
        temp = "";
        if(arrAvailFiles[j].lastIndexOf(BJOBS_FILE_EXTN) == -1)  // Skip non bjob files
          continue;

        temp = tempPath + arrAvailFiles[j];
        Log.debugLog(className, "loadAllBJOBGroups", "", "", "Adding '.bjobs' file name in ArrayList = " + temp);

        String[] tempArr = rptUtilsBean.strToArrayData(arrAvailFiles[j], "."); // for remove batch job extension
        if(tempArr.length == 2)
        {
          arrListAllBJOB.add(tempArr[0]);
          Log.debugLog(className, "loadAllBJOBGroups", "", "", "Adding '.bjobs' file name in ArrayList = " + temp);
        }
        else
          Log.debugLog(className, "loadAllBJOBGroups", "", "", "Skiping file name  = " + temp);
      }

      Log.debugLog(className, "loadAllBJOBGroups", "", "", "Number of '.bjobs' files = " + arrListAllBJOB.size());

      Object[] arrTemp = arrListAllBJOB.toArray();
      Arrays.sort(arrTemp);

      for(int i = 0; i < arrTemp.length; i++)
        arrListOfAllBJOBSortedOrder.add(arrTemp[i].toString());

      Log.debugLog(className, "loadAllBJOBGroups", "", "", "Method Ends");
      return arrListOfAllBJOBSortedOrder;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "loadAllBJOBGroups", "", "", "Exception - ", e);
      return null;
    }
  }

  /**public boolean addBatchJobDetail(String strBatchJobName, String strBatchJobDesc, String batchKeywordLine)
  {
    Log.debugLog(className, "addBatchJobDetail", "", "", "Start method. Batch Job Name = " + strBatchJobName + ", Batch Job Desc = " + strBatchJobDesc + ", batchKeywordLine = " + batchKeywordLine);

    try
    {
	  String bjobNameWithPath = getBatchJobNameWithPath(strBatchJobName) + BJOBS_FILE_EXTN;
      File fileObj = openFile(bjobNameWithPath);

      if(!fileObj.exists())
      {
        Log.errorLog(className, "addBatchJobDetail", "", "", "Source bjob does not exist. BJOB name = " + strBatchJobName);
        return false;
      }

      FileInputStream fin = new FileInputStream(mprofSrcFileName);
      BufferedReader br = new BufferedReader(new InputStreamReader(fin));

      FileOutputStream fout = new FileOutputStream(bjobNameWithPath, true);  // append mode
      PrintStream pw = new PrintStream(fout);

      String dataLine = "";
      while((dataLine = br.readLine()) != null)
      {
        if(dataLine.startsWith(BJOB_DESC))
        {
          dataLine = BJOB_DESC + strBatchJobDesc;
        }
        if(dataLine.startsWith(LAST_MODIFIED_DATE))
        {
          dataLine = LAST_MODIFIED_DATE + rptUtilsBean.getCurDateTime();
        }
        pw.println(dataLine);
      }

      pw.println(batchKeywordLine);

      pw.close();
      br.close();
      fin.close();
      fout.close();

    }
    catch(Exception ex)
    {
	  Log.stackTraceLog(className, "addBatchJobDetail", "", "", "Exception - ", ex);
	  return false;
    }
  }**/

/*****************Get List of Standard Monitor *************************/
// This function returns the list of standard monitor

  /**public String[][] getStdMonitorList()
  {
    Log.debugLog(className, "getStdMonitorList", "", "", "Method called");
    String fileWithPath = getStdMonitorProfileNameWithPath("standard_monitors.dat");

    try
    {
      Vector vecStdMonitorData = readFile(fileWithPath);

      String[] arrRcrd = new String[9];


      if(vecStdMonitorData == null)
      {
        Log.debugLog(className, "getStdMonitorList", "", "", "Standard Monitor Profile File not found, It may be corrupted. File name = " + fileWithPath);
        return null;
      }

      int count = 0;
      for (int i = 0; i < vecStdMonitorData.size(); i++)
      {
        if(!vecStdMonitorData.elementAt(i).toString().startsWith("#"))
        	count++;
      }
      String[][] arrStdMonitorProfiles = new String[count][9];

      count = 0;
      for (int i = 0; i < vecStdMonitorData.size(); i++)
      {
        if(vecStdMonitorData.elementAt(i).toString().startsWith("#"))
         continue;

        //System.out.println(i + "    " + vecStdMonitorData.elementAt(i).toString());
        arrRcrd = rptUtilsBean.strToArrayData(vecStdMonitorData.elementAt(i).toString(), "|");
        if(arrRcrd.length != 9)
        {
          Log.errorLog(className, "getStdMonitorList", "", "", "Number of fields are not 9. Count is " + arrRcrd.length);
          continue; // Ignore this line
        }
        for(int j = 0; j < 9; j++)
          arrStdMonitorProfiles[count][j] = arrRcrd[j];
        count++;
      }

      return arrStdMonitorProfiles;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getStdMonitorList", "", "", "Exception - ", e);
      return null;
    }
  }**/

/***********************************************************************/

  /* This function returns the Batch Job Description of passed Batch Job
   *
   * @param : String bjob file name.
   * @return : String bjobDesc.
   */
  private String getBatchGroupDesc(String bjobName)
  {
    Log.debugLog(className, "getBatchGroupDesc", "", "", "Method called. Batch Group Name = " + bjobName);
    Properties tempBatchProp = new Properties();

    try
    {
      String tempBatchGroupNameWithPath = getBatchJobNameWithPath(bjobName + BJOBS_FILE_EXTN);
      File tempJobFile = new File(tempBatchGroupNameWithPath);
      FileInputStream fis = new FileInputStream(tempJobFile);
      tempBatchProp.load(fis);

      String value = tempBatchProp.getProperty("BJOB_DESC");
      if(value == null)
      {
        value = bjobName;
      }
      Log.debugLog(className, "getBatchGroupDesc", "", "", "Batch Job Name: " + bjobName + "'s description = " + value.trim());
      return (value.trim());
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getBatchGroupDesc", "", "", "Exception - ", e);
      return "";
    }
  }

  /* This method returns 2D String array, which contains data like [0][0]=name, [0][1]=Description, [0][2]=Modified date, [0][3]=Number of Bjobs.
   *
   * @param : NA
   * @return : String [][]
   */
  public String[][] getBJOBsFromBatchGroup()
  {
    Log.debugLog(className, "getBJOBsFromBatchGroup", "", "", "Method called");

    try
    {
      ArrayList arrListAllBJOB = loadAllBJOBGroups();  // Names of all BJOB files

      if(arrListAllBJOB == null)
        return null;

      String[][] strBatchJobs = new String[arrListAllBJOB.size() + 1][4]; // Add 1 for header.

      strBatchJobs[0][0] = "Batch Group Name";
      strBatchJobs[0][1] = "Batch Group Description";
      strBatchJobs[0][2] = "Modified Date";
      strBatchJobs[0][3] = "Number Of Batch Jobs";

      for(int i = 0; i < arrListAllBJOB.size(); i++)
      {
        String lastModDateTime = "";
        String fileWithPath = getBatchJobNameWithPath(arrListAllBJOB.get(i).toString() + BJOBS_FILE_EXTN);
        int headerCount = 0; // This is to count number of monitor in the profile.

        Vector vecJobData = readFile(fileWithPath);

        if(vecJobData == null)
        {
          Log.debugLog(className, "getBJOBsFromBatchGroup", "", "", "Batch Job File not found, It may be correpted, File name " + arrListAllBJOB.get(i).toString());
          continue;
        }

        boolean profileDescFlag = false;
        boolean lastModifiedDateFlag = false;

        String dataLine = "";
        for(int ii = 0; ii < vecJobData.size(); ii++)
        {
          dataLine = vecJobData.get(ii).toString();

          if(dataLine.startsWith(BJOB_DESC))
          {
           profileDescFlag = true;
           headerCount++;
          }
          if(dataLine.startsWith(LAST_MODIFIED_DATE))
          {
            lastModifiedDateFlag = true;
            headerCount++;
          }
        }

        strBatchJobs[i + 1][0] = arrListAllBJOB.get(i).toString();//Adding job name in to the String Array.
        Log.debugLog(className, "getBJOBsFromBatchGroup", "", "", "Adding Job name in array = " + strBatchJobs[i + 1][0]);

        strBatchJobs[i + 1][1] = getBatchGroupDesc(arrListAllBJOB.get(i).toString());   // Adding job description in the String Array.
        Log.debugLog(className, "getBJOBsFromBatchGroup", "", "", "Adding Job Description in array = " + strBatchJobs[i + 1][1]);

        for(int ii = 0; ii < vecJobData.size(); ii++)
        {
          if((vecJobData.elementAt(ii).toString()).startsWith(LAST_MODIFIED_DATE))
          {
             String[] arrTemp = rptUtilsBean.strToArrayData(vecJobData.elementAt(ii).toString(), "=");
             lastModDateTime = arrTemp[1].trim();
             break;
          }
        }

        if(lastModDateTime.equals(""))
        {
          File fileObj = openFile(fileWithPath);
          long lastModifiedTime = fileObj.lastModified();
          Date dateLastModified = new Date(lastModifiedTime);
          SimpleDateFormat smt = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
          lastModDateTime = smt.format(dateLastModified);
        }

        strBatchJobs[i + 1][2] = lastModDateTime;  // Adding File's last modified date in to the String Array.
        Log.debugLog(className, "getBJOBsFromBatchGroup", "", "", "Adding Job's last modified date in array = " + strBatchJobs[i + 1][2]);

        if(profileDescFlag || lastModifiedDateFlag)  //In the job, BjobDesc or last modified field is present.
          strBatchJobs[i + 1][3] = "" + (vecJobData.size() - headerCount); // - headerCount is to skip profile description and Last mofidied date.
        else  //In the job, BjobDesc or last modified field is not present, so there would be only bjob in that group.
          strBatchJobs[i + 1][3] = "" + (vecJobData.size());

        Log.debugLog(className, "getBJOBsFromBatchGroup", "", "", "Adding Number of Batch Jobs in array = " + strBatchJobs[i + 1][3]);
      }
      Log.debugLog(className, "getBJOBsFromBatchGroup", "", "", "Method Ends");
      return strBatchJobs;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getBJOBsFromBatchGroup", "", "", "Exception - ", e);
      return null;
    }
  }

  // this is to count monitors for all monitor type individually.
  /**public int[] getMPROFCount(String monProfileName)
  {
    Log.debugLog(className, "getMPROFCount", "", "", "Method called, Monitor Profile Name = " + monProfileName);

    String[][] mprofDetail = getBJOBDetails(monProfileName);

    int countStndMon = 0, countSplMon = 0, countCustMon = 0, countDynamicMon = 0, countServerStatMon = 0, countServerSigMon = 0, countCheckMon = 0, countUserMon = 0;

    int[] getAllMonitorCount = new int[8];
    for(int i = 1; i < mprofDetail.length; i++)
    {
      if(mprofDetail[i] == null)
        continue;

      String monType = mprofDetail[i][0];

      if(monType.equals("STANDARD_MONITOR"))
      {
        countStndMon++;
        getAllMonitorCount[0] = countStndMon;
      }
      // Earlier LOG_MONITOR is used as SPECIAL_MONITOR
	  // but to handle old mprof following condition is there
	  //  As it is to be remove after all old mprof is replaced to LOG_MONITOR.
      //
      else if(monType.equals("SPECIAL_MONITOR") || monType.equals("LOG_MONITOR"))
      {
        countSplMon++;
        getAllMonitorCount[1] = countSplMon;
      }
      else if(monType.equals("CUSTOM_MONITOR"))
      {
        countCustMon++;
        getAllMonitorCount[2] = countCustMon;
      }
      else if(monType.equals("DYNAMIC_VECTOR_MONITOR"))
      {
        countDynamicMon++;
        getAllMonitorCount[3] = countDynamicMon;
      }
      else if(monType.equals("SERVER_PERF_STATS"))
      {
        countServerStatMon++;
        getAllMonitorCount[4] = countServerStatMon;
      }
      else if(monType.equals("SERVER_SIGNATURE"))
      {
        countServerSigMon++;
        getAllMonitorCount[5] = countServerSigMon;
      }
      else if(monType.equals("CHECK_MONITOR"))
      {
        countCheckMon++;
        getAllMonitorCount[6] = countCheckMon;
      }
      else if(monType.equals("USER_MONITOR"))
      {
        countUserMon++;
        getAllMonitorCount[7] = countUserMon;
      }
      else
        Log.debugLog(className, "getMPROFCount", "", "", "There is no monitor like '" + monType + "'.");
    }

    return getAllMonitorCount;
  }**/

  /* This gives details of all batch jobs of batch group.
   *
   * @param : String bjob name
   * @return : String[][]
   */
  public String[][] getBJOBDetails(String bjobName)
  {
    Log.debugLog(className, "getBJOBDetails", "", "", "Method called, file name = " + bjobName);

    String fileWithPath = getBakBJOBName(bjobName);
    try
    {
      Vector vecProfileData = readFile(fileWithPath);

      if(vecProfileData == null)
      {
        Log.debugLog(className, "getBJOBDetails", "", "", "Batch Job File not found, It may be correpted., file name = " + bjobName);
        return null;
      }

Log.debugLog(className, "getBJOBDetails", "", "", "vecProfileData.size() = " + vecProfileData.size());

      String[][] arrBatchJobDetails = new String[vecProfileData.size() - 1][];//Skipping Batch job description and last modified date and add bjob detail(job details and last modified date).

      String[] arrBJOBDetail = new String[2];
      String[] arrTemp = null;

      String dataLine = "";
      int count = 1;

      for(int i = 0; i < vecProfileData.size(); i++)//Batch Job description wiil be on the zeroth index and last modified date on oneth index.
      {
        dataLine = vecProfileData.elementAt(i).toString();

        if(dataLine.startsWith(BJOB_DESC))
        {
          arrTemp = null;
          arrTemp = rptUtilsBean.strToArrayData(dataLine, "=");
          arrBJOBDetail[0] = arrTemp[1].trim();
        }
        else if(dataLine.startsWith(LAST_MODIFIED_DATE))
        {
          arrTemp = null;
          arrTemp = rptUtilsBean.strToArrayData(dataLine, "=");
          arrBJOBDetail[1] = arrTemp[1].trim();
        }
        else
        {
          String[] arr = rptUtilsBean.strToArrayData(dataLine, "|");
          Log.debugLog(className, "getBJOBDetails", "", "", "arr Size" + arr.length);
          arrBatchJobDetails[count] = rptUtilsBean.strToArrayData(dataLine, "|");
          count++;
        }
      }
      arrBatchJobDetails[0] = arrBJOBDetail; // append job desc and last modified date at first index
      Log.debugLog(className, "getBJOBDetails", "", "", "BJOBDetail =  " + arrBatchJobDetails[0][0]);
      Log.debugLog(className, "getBJOBDetails", "", "", "Method Ends.");
      return arrBatchJobDetails;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getBJOBDetails", "", "", "Exception - ", e);
      return null;
    }
  }
  /***********************************************************************************************/
  //                              Utility function(s) called from JSP                            //
  /***********************************************************************************************/

  /* This delete the existing bjob file
   *
   * @param : String bjob name
   * @return : boolean true, if file is successfully deleted else false.
   */
  public boolean deleteBJOBGroup(String bjobName)
  {
    Log.debugLog(className, "deleteBJOBGroup", "", "", "Batch Job Name = " +  bjobName);
    deleteBJOBGroup(bjobName, BJOBS_BAK_EXTN);
    return(deleteBJOBGroup(bjobName, BJOBS_FILE_EXTN));
  }

  /* This delete the bjob backup if present
   *
   * @param : String bjob name
   * @return : boolean true, if file is successfully deleted else false.
   */
  public boolean deleteBakBJOBGroup(String bjobName)
  {
    Log.debugLog(className, "deleteBakBJOBGroup", "", "", "Batch Job Name = " +  bjobName);
    return(deleteBJOBGroup(bjobName, BJOBS_BAK_EXTN));
  }

  /* This method delete existing BJOB, depends upon extension pass to it.
   *
   * @param : String bjob name, String file extension either it may be .bjobs or .hot
   * @return : boolean true, if file is successfully deleted else false.
   */
  private boolean deleteBJOBGroup(String bjobName, String fileExtn)
  {
    Log.debugLog(className, "deleteBJOBGroup", "", "", "Method Starts. Batch Job Filename = " + bjobName + fileExtn);
    String tmpFile = null;

    if(fileExtn.equals(BJOBS_FILE_EXTN))
      tmpFile = getBatchJobNameWithPath(bjobName + fileExtn);
    else
      tmpFile = getBakBJOBName(bjobName);

    try
    {
      File fileObj = new File(tmpFile);
      if(fileObj.exists())
      {
        boolean success = fileObj.delete();
        if (!success)
        {
          Log.errorLog(className, "deleteBJOBGroup", "", "", "Error in deleting bjob file (" + tmpFile + ")");
          return false;
        }
      }
      else
      {
        Log.errorLog(className, "deleteBJOBGroup", "", "", "BJOB file (" + tmpFile + ") does not exist.");
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "deleteBJOBGroup", "", "", "Exception - ", e);
      return false;
    }

    Log.debugLog(className, "deleteBJOBGroup", "", "", "Method Ends.");
    return true;
  }

  /* Make Bak file of MPROF
   * All edit/changes are done in Backup file
   *
   * @param : String bjob filename.
   * @return : boolean true, if file is successfully backup created else false.
   */
  public boolean createBJOBBackup(String bjobName)
  {
    Log.debugLog(className, "createBJOBBackup", "", "", "Method Starts. BJOB Name = " +  bjobName);
    String bjobFileName = "";

    try
    {
      bjobFileName = getBatchJobNameWithPath(bjobName + BJOBS_FILE_EXTN);
      File bjobFileObj = new File(bjobFileName);
      if(!bjobFileObj.exists())
      {
        Log.errorLog(className, "createBJOBBackup", "", "", "Source bjob does not exist. BJOB name = " + bjobName);
        return false;
      }

      String bjobBakFileName = getBakBJOBName(bjobName);

      if(copyBJOBFile(bjobFileName, bjobBakFileName, "", false) == false)
      {
        Log.errorLog(className, "createBJOBBackup", "", "", "Error in creating Bakup of BJOB. BJOB file is " + bjobFileName + ". Backup file is " + bjobBakFileName);
        return false;
      }

      Log.debugLog(className, "createBJOBBackup", "", "", "Method Ends.");
      return(true);
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "createBJOBBackup", "", "", "Exception - ", e);
      return false;
    }
  }

  /* Copy source mprof file to destination mprof file.
   *
   * @param : String source file name(with path), String destination file name(with path), String bjob description for destination file, boolean update Modification date flag.
   * @return : boolean true, if file is successfully copied else false.
   */
  private boolean copyBJOBFile(String srcFileNameWithPath, String destFileNameWithPath, String destDesc, boolean updModDate)
  {
    Log.debugLog(className, "copyBJOBFile", "", "", "Method called");

    try
    {
      File fileSrc = new File(srcFileNameWithPath);
      File fileDest = new File(destFileNameWithPath);

      Vector vecSourceFileData = readFile(srcFileNameWithPath);
      boolean lastModDateFlag = false;
      boolean profileDescFlag = false;

      if(!fileSrc.exists())
      {
        Log.errorLog(className, "copyBJOBFile", "", "", "Source file does not exists. Filename = " + srcFileNameWithPath);
        return false;
      }

      if(fileDest.exists())
        fileDest.delete();
      fileDest.createNewFile();

      if(rptUtilsBean.changeFilePerm(fileDest.getAbsolutePath(), "netstorm", "netstorm", "664") == false)
        return false;

      String dataLine = "";
      for(int i = 0; i < vecSourceFileData.size(); i++)
      {
        dataLine = vecSourceFileData.elementAt(i).toString();
        if(dataLine.startsWith(LAST_MODIFIED_DATE))
          lastModDateFlag = true;
        if(dataLine.startsWith(BJOB_DESC))
          profileDescFlag = true;
      }

      FileInputStream fin = new FileInputStream(fileSrc);
      BufferedReader br = new BufferedReader(new InputStreamReader(fin));

      FileOutputStream fout = new FileOutputStream(fileDest, true);  // append mode
      PrintStream pw = new PrintStream(fout);

      if(!lastModDateFlag && !profileDescFlag) //Last modified date & profile desc is not in the profile, then write profile desc and current date in the file in the file.
      {
        pw.println(BJOB_DESC + "NA");
        pw.println(LAST_MODIFIED_DATE + rptUtilsBean.getCurDateTime());
      }

      String str;

      while((str = br.readLine()) != null)
      {
        if(str.startsWith(LAST_MODIFIED_DATE))
        {
          if(updModDate)
            str = LAST_MODIFIED_DATE + rptUtilsBean.getCurDateTime();
        }
        if(str.startsWith(BJOB_DESC))
        {
          if(!destDesc.equals(""))
            str = BJOB_DESC + destDesc;
        }

        pw.println(str);
      }

      pw.close();
      br.close();
      fin.close();
      fout.close();

      Log.debugLog(className, "copyBJOBFile", "", "", "Method Ends.");
      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "copyBJOBFile", "", "", "Exception - ", e);
      return false;
    }
  }

  /* This will delete selected batch job from its group. Selected batch jobs to be delete are   passed as array of index.
   *
   * @param : String bjobName, Srting[] rows no of bjob to be deleted.
   * @return : true, if selected bjobs are deleted successfully else false.
   */
  public boolean deleteBatchJobFromBJOBGroup(String strBJOBGrpName, String strBJOBDesc, String[] arrBjobIdx)
  {
    Log.debugLog(className, "deleteBatchJobFromBJOBGroup", "", "", "Method Start. BJOB Group Name = " + strBJOBGrpName + ", BJOB Description = " + strBJOBDesc);

    String bjobNameWithPath = getBakBJOBName(strBJOBGrpName);
    try
    {
      Vector vecData = readFile(bjobNameWithPath);

      File bjobFileObj = new File(bjobNameWithPath);

      if(bjobFileObj.exists())
        bjobFileObj.delete(); // Delete bjob bak file

      bjobFileObj.createNewFile(); // Create new bjob bak file

      if(rptUtilsBean.changeFilePerm(bjobFileObj.getAbsolutePath(), "netstorm", "netstorm", "664") == false)
        return false;

      FileOutputStream fout = new FileOutputStream(bjobFileObj, true);  // append mode
      PrintStream pw = new PrintStream(fout);
      String status = "";

      String dataLine = "";

      pw.println(vecData.elementAt(0).toString()); // for profile description

      for(int i = 1; i < vecData.size(); i++)
      {
        dataLine = vecData.elementAt(i).toString();

        /**if(dataLine.startsWith(BJOB_DESC))
        {
          dataLine = BJOB_DESC + strBJOBDesc;
          pw.println(dataLine);
        }
        else**/ if(dataLine.startsWith(LAST_MODIFIED_DATE))
        {
          dataLine = LAST_MODIFIED_DATE + rptUtilsBean.getCurDateTime();
          pw.println(dataLine);
          Log.debugLog(className, "deleteBatchJobFromBJOBGroup", "", "", "Line written: " + dataLine + "i: " + i);
        }
        else
        {
          status = "";
          for(int k = 0; k < arrBjobIdx.length; k++)
          {
            if((i - 2) == (int)Integer.parseInt(arrBjobIdx[k])) // substratct by 2 because first line is description and second is last modified date of profile
            {
              status = "true";
              Log.debugLog(className, "deleteBatchJobFromBJOBGroup", "", "", "Status is true for, i: " + i + ", and arrBjobIdx[" + k + "]: " + arrBjobIdx[k]);
              break;
            }
            else
              Log.debugLog(className, "deleteBatchJobFromBJOBGroup", "", "", "Status is false for, i: " + i + ", and arrBjobIdx[" + k + "]: " + arrBjobIdx[k]);
          }

          if(!status.equals("true"))
          {
            pw.println(vecData.elementAt(i).toString());
            Log.debugLog(className, "deleteBatchJobFromBJOBGroup", "", "", "Line written: " + vecData.elementAt(i).toString() + "i: " + i);
          }
          else
            Log.debugLog(className, "deleteBatchJobFromBJOBGroup", "", "", "Line is not written: " + vecData.elementAt(i).toString() + "i: " + i);
        }
      }

      pw.close();
      fout.close();

      Log.debugLog(className, "deleteBatchJobFromBJOBGroup", "", "", "Method Ends.");
      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "deleteBatchJobFromBJOBGroup", "", "", "Exception - ", e);
      return false;
    }
  }

  /* This is to remove monitor which are having same GDF Name.
   * Param - mprofName gdfName vectorName serverName as arguments
   * return boolean
   */
  /**public boolean deleteMonitorFromMPROFHavingSameGDFName(String mprofName, String gdfName, String vectorName)
  {
    Log.debugLog(className, "deleteMonitorFromMPROFHavingSameGDFName", "", "", "MPROF Name = " + mprofName + ", GDF Name = " + gdfName + ", Vector Name = " + vectorName + "");

    String mprofNameWithPath = getBAKMPROFName(mprofName);
    try
    {
      Vector vecData = readFile(mprofNameWithPath);

      File mprofFileObj = new File(mprofNameWithPath);

      if(mprofFileObj.exists())
        mprofFileObj.delete(); // Delete mprof bak file

      mprofFileObj.createNewFile(); // Create new mprof bak file

      if(rptUtilsBean.changeFilePerm(mprofFileObj.getAbsolutePath(), "netstorm", "netstorm", "664") == false)
        return false;

      FileOutputStream fout = new FileOutputStream(mprofFileObj, true);  // append mode
      PrintStream pw = new PrintStream(fout);
      String status = "";

      String dataLine = "";

      pw.println(vecData.elementAt(0).toString()); // for profile description

      for(int i = 1; i < vecData.size(); i++)
      {
        dataLine = vecData.elementAt(i).toString();

        if(dataLine.startsWith(LAST_MODIFIED_DATE))
        {
          dataLine = LAST_MODIFIED_DATE + rptUtilsBean.getCurDateTime();
          pw.println(dataLine);
        }
        else
        {
          status = "";
          // Earlier LOG_MONITOR is used as SPECIAL_MONITOR
          //  but to handle old mprof following condition is there
          //  As it is to be remove after all old mprof is replaced to LOG_MONITOR.
          //
          if(vecData.elementAt(i).toString().startsWith("SPECIAL_MONITOR") || vecData.elementAt(i).toString().startsWith("LOG_MONITOR"))
          {
            String[] monitorData = rptUtilsBean.split(vecData.elementAt(i).toString(), " ");

            //if(monitorData[1].equals(serverName) && monitorData[2].equals(gdfName) && monitorData[3].equals(vectorName) && monitorData[8].equals(logFileCommandName))

            // As GDF Name and vectorName both together is unique for every Log Monitor.
            if(monitorData[2].equals(gdfName) && monitorData[3].equals(vectorName))
              status = "true";
          }

          if(!status.equals("true"))
            pw.println(vecData.elementAt(i).toString());
        }
      }

      pw.close();
      fout.close();

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "deleteMonitorFromMPROFHavingSameGDFName", "", "", "Exception - ", e);
      return false;
    }
  }**/

  /* Add Batch Jobs to Batch Job Group. Add is done to backup file only.
   *
   * @param : String BJobName, String BJobDesc
   * @return : boolean - true if bjob is successfully added in bjobGroup else false.
   */
  public boolean addBatchJobToBJOBGroup(String strBatchJobName, String strBatchJobDesc)
  {
    Log.debugLog(className, "addBatchJobToBJOBGroup", "", "", "BJOB Name =" + strBatchJobName);
    return(addBatchJobToBJOBGroup(strBatchJobName, strBatchJobDesc, ""));
  }

  /* Add Batch Jobs to Batch Job Group. Add is done to backup file only.
   *
   * @param : String BJobName, String BJobDesc, String BJob Detail
   * @return : boolean - true if bjob is successfully added in bjobGroup else false.
   */
  public boolean addBatchJobToBJOBGroup(String strBatchJobName, String strBatchJobDesc, String batchjobDetail)
  {
    Log.debugLog(className, "addBatchJobToBJOBGroup", "", "", "Method Starts. BJOB Name =" + strBatchJobName + ", BJOB Desc = " + strBatchJobDesc + ", batchjobDetail = " + batchjobDetail);

    String bakBjobNameWithPath = getBakBJOBName(strBatchJobName);

    try
    {
      // Read file's content
      Vector vecData = readFile(bakBjobNameWithPath);

      File bjobfileObj = openFile(bakBjobNameWithPath);

      bjobfileObj.delete(); // Delete bjbob file
      bjobfileObj.createNewFile(); // Create new bjob file

      if(rptUtilsBean.changeFilePerm(bjobfileObj.getAbsolutePath(), "netstorm", "netstorm", "664") == false)
        return false;

      if(!bjobfileObj.exists())
      {
        Log.errorLog(className, "addBatchJobToBJOBGroup", "", "", "Source bjob does not exist. BJOB name = " + strBatchJobName);
        return false;
      }

      FileOutputStream fout = new FileOutputStream(bjobfileObj, true);  // append mode
      PrintStream pw = new PrintStream(fout);

      String dataLine = "";
      for(int i = 0; i < vecData.size(); i++)
      {
        dataLine = vecData.get(i).toString();

        if(dataLine.startsWith(BJOB_DESC))
          dataLine = BJOB_DESC + strBatchJobDesc;
        else if(dataLine.startsWith(LAST_MODIFIED_DATE))
          dataLine = LAST_MODIFIED_DATE + rptUtilsBean.getCurDateTime();

        pw.println(dataLine);
      }

      pw.println(batchjobDetail);  // Append the new bjob to the end of file
      pw.close();
      fout.close();

      Log.debugLog(className, "addBatchJobToBJOBGroup", "", "", "Method Ends.");
      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "addBatchJobToBJOBGroup", "", "", "Exception - ", e);
      return false;
    }
  }

  /* Create new bjob. New bjob is always create as bak file.
   *
   * @param : String BJobName, String BJobDesc
   * @return : boolean - true if bjob is successfully created else false.
   */
  public boolean createNewBJOB(String bjobName, String bjobDesc)
  {
    Log.debugLog(className, "createNewBJOB", "", "", "BJOB Name =" + bjobName + ", job description = " + bjobDesc);

    return(createNewBJOB(bjobName, bjobDesc, false));
  }

  /* Add Batch Jobs to Batch Job Group. If false is passed, then hot file is created else main file.
   *
   * @param : String BJobName, String BJobDesc, boolean flag
   * @return : boolean - true if bjob is successfully created else false.
   */
  public boolean createNewBJOB(String bjobName, String bjobDesc, boolean checkFileType)
  {
    Log.debugLog(className, "createNewBJOB", "", "", "Method Starts. BJOB Name =" + bjobName + ", batch job desc = " + bjobDesc);
    String bjobNameWithPath = "";

    try
    {
      // This will allow user to create mprof file with .mprof extn.
      if(checkFileType)
        bjobNameWithPath = getBatchJobNameWithPath(bjobName + BJOBS_FILE_EXTN);
      else
        bjobNameWithPath = getBakBJOBName(bjobName);

      File bjobFileObj = new File(bjobNameWithPath);

      if(bjobFileObj.exists())
      {
        Log.errorLog(className, "createNewBJOB", "", "", "BJOB already exists. BJOB name = " + bjobName);
        return false;
      }

      bjobFileObj.createNewFile();

      if(rptUtilsBean.changeFilePerm(bjobFileObj.getAbsolutePath(), "netstorm", "netstorm", "664") == false)
        return false;

      FileOutputStream fout = new FileOutputStream(bjobFileObj, true);  // append mode
      PrintStream pw = new PrintStream(fout);

      String jobDescLine = BJOB_DESC + bjobDesc;
      pw.println(jobDescLine);
      pw.println(LAST_MODIFIED_DATE + rptUtilsBean.getCurDateTime());
      pw.close();
      fout.close();

      Log.debugLog(className, "createNewBJOB", "", "", "Method Ends.");
      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "createNewBJOB", "", "", "Exception - ", e);
      return false;
    }
  }

  /* Save bjob will delete bjob file and copy bak file to bjob file
   * Bak is not deleted as GUI needs this file. Mod date/time is also updated in bjob file.
   *
   * @param : String bjob name, String bjobDesc
   * @return : boolean true, if file is successfully saved else false.
   */
  public boolean saveBJOB(String bjobName, String bjobDesc)
  {
    Log.debugLog(className, "saveBJOB", "", "", "Method Starts. BJOB Filename =" + bjobName + ", bjob description = " + bjobDesc);

    try
    {
      // First check if Bak file is existing or not. If not create new bjob Bak file
      String bjobBakFileName = getBakBJOBName(bjobName);

      File bjobBakFileObj = new File(bjobBakFileName);

      if(!bjobBakFileObj.exists())
        createNewBJOB(bjobName, bjobDesc);

      // Now copy Bak file to bjob file to save the changes
      String bjobFileName = getBatchJobNameWithPath(bjobName + BJOBS_FILE_EXTN);

      if(copyBJOBFile(bjobBakFileName, bjobFileName, bjobDesc, true) == false)
      {
        Log.errorLog(className, "saveBJOB", "", "", "Error in copying Bak to bjob (" + bjobFileName + ")");
        return false;
      }

      // Now copy bjob to Bak file so that Bak is same as bjob file
      if(copyBJOBFile(bjobFileName, bjobBakFileName, "", false) == false)
      {
        Log.errorLog(className, "saveBJOB", "", "", "Error in copying bjob to Bak (" + bjobFileName + ")");
        return false;
      }

      Log.debugLog(className, "saveBJOB", "", "", "Method Ends.");
      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "saveBJOB", "", "", "Exception - ", e);
      return false;
    }
  }

  /* To check if bjob is modified or not after last save was done
   *
   * @param : String bjob file name
   * @return : boolean true, if file is modified else false.
   */
  public boolean isBJOBModified(String bjobName)
  {
    Log.debugLog(className, "isBJOBModified", "", "", "Method called. Batch Job Name = " + bjobName);

    try
    {

      String bjobFileName = getBatchJobNameWithPath(bjobName + BJOBS_FILE_EXTN);
      String bjobBakFileName = getBakBJOBName(bjobName);

      Vector vecData = readFile(bjobFileName);
      Vector vecBakData = readFile(bjobBakFileName);

      String strDate = "";
      String strBakDate = "";

      String dataLine = "";
      for(int i = 0; i < vecData.size(); i++)
      {
        dataLine = vecData.get(i).toString();

        if(dataLine.startsWith(LAST_MODIFIED_DATE))
        {
          String[] arrTemp = rptUtilsBean.strToArrayData(dataLine, "=");
          strDate = arrTemp[1].trim();
          break;
        }
      }

      for(int i = 0; i < vecBakData.size(); i++)
      {
        dataLine = vecBakData.get(i).toString();

        if(dataLine.startsWith(LAST_MODIFIED_DATE))
        {
          String[] arrTemp = rptUtilsBean.strToArrayData(dataLine, "=");
          strBakDate = arrTemp[1].trim();
          break;
        }
      }

      Log.debugLog(className, "isBJOBModified", "", "", "Modification Date/Time for BJOB and Bak file are - " + strDate + " and " + strBakDate);

      // Compare modified file
      if((rptUtilsBean.convertDateToMilliSec(strDate)) == (rptUtilsBean.convertDateToMilliSec(strBakDate)))
        return false;

      Log.debugLog(className, "isBJOBModified", "", "", "Method Ends");
      return true;

    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "isBJOBModified", "", "", "Exception - ", e);
      return false;
    }
  }

/***************************************Start of function ***********************************/

  /* This is to retrieve URL encode for the special characters which are placed as 'Others' in Field Seperators.
   *
   * Param args @ String as URL encoded format
   * Return String as Special Characters in URL Decoded format other than Tab(%09), Space(+), Double Quotes(%22), Single Quotes(%27), Comma(%2C), BackSlash(%5C), Dash(-), Semi colon(%3B), Equal(%3D).
   */
  /**public String getDecodeValueForFileSeperators(String strLine)
  {
    Log.debugLog(className, "getDecodeValueForFileSeperators", "", "", "strLine = " + strLine  + ", " + strLine.length());

    String otherFileSeperators = "", decodeFileSeperators = "";

    String strTemp = "";
    int y = 0;

    for(int i = 0; i < strLine.length(); i++, y++)
    {
      strTemp = strTemp.concat(String.valueOf(strLine.charAt(i)));

      if(!strTemp.equals("+") && !strTemp.equals("-"))
      {
        if((y+1) % 3 == 0)
        {
          if(!strTemp.equals("%09") && !strTemp.equals("%27") && !strTemp.equals("%2C") && !strTemp.equals("%22") && !strTemp.equals("%5C") && !strTemp.equals("%3B") && !strTemp.equals("%3D"))
          {
            otherFileSeperators = otherFileSeperators.concat(strTemp);
            strTemp = "";

            Log.debugLog(className, "getDecodeValueForFileSeperators", "", "", "otherFileSeperators = " + otherFileSeperators);
          }
          else
            strTemp = "";
        }
      }
      else if(strTemp.equals("*") || strTemp.equals("_") || strTemp.equals("."))
      {
        otherFileSeperators = otherFileSeperators.concat(strTemp);
        y--;

        Log.debugLog(className, "getDecodeValueForFileSeperators", "", "", "Else if otherFileSeperators = " + otherFileSeperators);
      }
      else if(strTemp.equals("+") || strTemp.equals("-"))
      {
        strTemp = "";
        y--;
	  }
      else
      {
        strTemp = "";
        y--;
      }
    }

    //decode pattern.
    // The alphanumeric characters "a" through "z", "A" through "Z" and "0" through "9" remain the same.
    // The special characters ".", "-", "*", and "_" remain the same.
    // The plus sign "+" is converted into a space character " " .
    // A sequence of the form "%xy" will be treated as representing a byte where xy is the two-digit hexadecimal representation of the 8 bits. Then, all substrings that contain one or more of these byte sequences consecutively will be replaced by the character(s) whose encoding would result in those consecutive bytes. The encoding scheme used to decode these characters may be specified, or if unspecified, the default encoding of the platform will be used.
    //

    if(!otherFileSeperators.equals(""))
    {
      try
      {
        decodeFileSeperators = URLDecoder.decode(otherFileSeperators);
      }
      catch(Exception e)
      {
        decodeFileSeperators = otherFileSeperators;
        Log.debugLog(className, "getDecodeValueForFileSeperators", "", "", "Its contain special character as " + decodeFileSeperators + ", which is not handle by URLDecoder.");
      }
    }

    return decodeFileSeperators;
  }**/

 /********************************************** End Of function ********************************/

  /* Make copy of a bjob group with new name and job desc
   * Also creation date and modification date is new bjob is set to current date/time
   *
   * @param : String source Batch group, String destination batch group, String bjobDesc
   * @return : boolean true, if file is successfully saved else false.
   */
  public boolean copyBJOBGroup(String srcBatchGroup, String destBatchGroup, String bjobDesc)
  {
    Log.debugLog(className, "copyBJOBGroup", "", "", "Method Starts. SrcName =" +  srcBatchGroup + ", DestName = " + destBatchGroup + ", Batch Job Description = " + destBatchGroup);

    try
    {
      String bjobSrcFileName = getBatchJobNameWithPath(srcBatchGroup + BJOBS_FILE_EXTN);
      String bjobDestFileName = getBatchJobNameWithPath(destBatchGroup + BJOBS_FILE_EXTN);

      File bjobSrcFileObj = new File(bjobSrcFileName);
      if(!bjobSrcFileObj.exists())
      {
        Log.errorLog(className, "copyBJOBGroup", "", "", "Source bjob does not exist. BJOB name = " + srcBatchGroup);
        return false;
      }

      File bjobDestFileObj = new File(bjobDestFileName);
      if(bjobDestFileObj.exists())
      {
        Log.errorLog(className, "copyBJOBGroup", "", "", "Destination bjob already exists. BJOB name = " + destBatchGroup);
        return false;
      }

      FileInputStream fin = new FileInputStream(bjobSrcFileName);
      BufferedReader br = new BufferedReader(new InputStreamReader(fin));

      FileOutputStream fout = new FileOutputStream(bjobDestFileName, true);  // append mode
      PrintStream pw = new PrintStream(fout);

      String dataLine = "";
      while((dataLine = br.readLine()) != null)
      {
        if(dataLine.startsWith(BJOB_DESC))
        {
          dataLine = BJOB_DESC + bjobDesc;
        }
        if(dataLine.startsWith(LAST_MODIFIED_DATE))
        {
          dataLine = LAST_MODIFIED_DATE + rptUtilsBean.getCurDateTime();
        }
        pw.println(dataLine);
      }

      pw.close();
      br.close();
      fin.close();
      fout.close();

      Log.debugLog(className, "copyBJOBGroup", "", "", "Method Ends.");
      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "copyBJOBGroup", "", "", "Exception - ", e);
      return false;
    }
  }

  ///////////////////////////////Start of function //////////////////////////

  // this function return the unique list of Vector Name available in mprof, In Sorted order
  /**public TreeSet getCustomMonitorList()
  {
    Log.debugLog(className, "getCustomMonitorList", "", "", "Method called.");

    try
    {
      ArrayList arrListAllMPROF = loadAllBJOBGroups();  // Names of all MPROF files

      if(arrListAllMPROF == null)
        return null;

      TreeSet setAllUniqueVectorName = new TreeSet(); // this is also sorted set

      if(arrListAllMPROF.size() > 0)
      {
        for( int i = 0; i < arrListAllMPROF.size(); i++)
        {
          String fileWithPath = getMonitorProfileNameWithPath(arrListAllMPROF.get(i).toString() + MPROF_FILE_EXTN);
          int headerCount = 0; // This is to count number of monitor in the profile.

          Vector vecProfileData = readFile(fileWithPath);

          if(vecProfileData == null)
          {
            Log.debugLog(className, "getCustomMonitorList", "", "", "Monitor Profile File not found, It may be correpted, File name " + arrListAllMPROF.get(i).toString());
            continue;
          }

          String dataLine = "";
          for(int ii = 0; ii < vecProfileData.size(); ii++)
          {
            dataLine = vecProfileData.get(ii).toString();

            String[] arrTemp = rptUtilsBean.strToArrayData(dataLine, " ");

            if(dataLine.startsWith("CUSTOM_MONITOR") || dataLine.startsWith("STANDARD_MONITOR"))
            {
              if(dataLine.startsWith("CUSTOM_MONITOR"))
              {
                if(setAllUniqueVectorName.add(arrTemp[CUSTOM_VECTOR_NAME_INDEX]) == false)
                {
                  Log.debugLog(className, "getCustomMonitorList", "", "", "Vector name added to the unique list = " + arrTemp[CUSTOM_VECTOR_NAME_INDEX]);
                }
                else
                {
                  Log.debugLog(className, "getCustomMonitorList", "", "", "Vector name.already exist in the list = " + arrTemp[CUSTOM_VECTOR_NAME_INDEX]);
                } // end of inner most if
              }

              if(dataLine.startsWith("STANDARD_MONITOR"))
              {
                if(setAllUniqueVectorName.add(arrTemp[STANDARD_VECTOR_NAME_INDEX]) == false)
                {
                  Log.debugLog(className, "getCustomMonitorList", "", "", "Vector name added to the unique list = " + arrTemp[STANDARD_VECTOR_NAME_INDEX]);
                }
                else
                {
                  Log.debugLog(className, "getCustomMonitorList", "", "", "Vector name.already exist in the list = " + arrTemp[STANDARD_VECTOR_NAME_INDEX]);
                } // end of inner most if
              }
            } // end of inner if
          } // end of profile data loop
        } // end of all mprof loop
      } // end of outer if
      return setAllUniqueVectorName;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getCustomMonitorList", "", "", "Exception - ", e);
      return null;
    }
  }**/

  /**
   * This method the server and pool name from the welogic server
   * @param serverName
   * @param userName
   * @param pwd
   * @param bolServer
   * @param bolPoolName
   * @return
   *  nsu_server_admin -s 192.168.1.41 -c 'java -cp .:/root/beahome/wlserver_10.3/server/lib/wljmxclient.jar cm_weblogic_monitor_args 192.168.1.41 7001 weblogic weblogic' -g
cmd_args = java:-cp .%3A/root/beahome/wlserver_10.3/server/lib/wljmxclient.jar cm_weblogic_monitor_args 192.168.1.41 7001 weblogic weblogic
SERVER_NAME = examplesServer
examples-demoXA-2
examples-demoXA
examples-demo
   LinkedHashMap*/
  /**public String[][] getServerPoolName(String monServerName, String webHostName, String webPort, String webUserName, String webPwd)
  {
    Log.debugLog(className, "getServerPoolName", "", "", "Method called.");

    //Maintain server name as a key and value is object of vector for pool name
    LinkedHashMap linkedHashMap = new LinkedHashMap();

    String[][] arrServerPoolDetail = null;
    try
    {
      CmdExec exec = new CmdExec();
      String strCmdName = "nsu_server_admin";
      String strCmdArgs = " -s " + monServerName + " -c " + "\'" + "java cm_weblogic_monitor_args "  + webHostName + " " + webPort + " " + webUserName + " " + webPwd + "\'" + " -g";
      Log.debugLog(className, "getServerPoolName", "", "", "strCmdName = " + strCmdName + "\n" + "strCmdArgs = " + strCmdArgs);
      //System.out.println("strCmdName = " + strCmdName + "\n" + "strCmdArgs = " + strCmdArgs);
      Vector vecCmdOut = exec.getResultByCommand(strCmdName, strCmdArgs, 0, null, "root");

      if((vecCmdOut.size() > 0) && ((String)vecCmdOut.lastElement()).startsWith("ERROR"))
        return null;

      if(vecCmdOut == null)
        return null;

      String strServerName = "";
      Vector vecPool = null;

      for(int i = 0; i < vecCmdOut.size(); i++)
      {
        String strLine = vecCmdOut.get(i).toString().trim();
        //System.out.println("strLine = " + strLine);
        if(strLine.startsWith("SERVER_NAME"))
        {
          vecPool = new Vector();
          String[] serverName = rptUtilsBean.split(strLine, "=");
          strServerName =serverName[1].trim();
        }
        else
          vecPool.add(vecCmdOut.get(i).toString().trim());

        if(!strServerName.equals(""))
          linkedHashMap.put(strServerName, vecPool);
      }

      ArrayList serverPoolList = new ArrayList();

      Set st = linkedHashMap.keySet();
      //iterate through the Set of keys
      Iterator itr = st.iterator();
      while(itr.hasNext())
      {
        String key = itr.next().toString();

        Vector pool = (Vector)linkedHashMap.get(key);

        if(pool.size() == 0)
          serverPoolList.add(key + "|" + null);

        for(int k = 0; k < pool.size(); k++)
          serverPoolList.add(key + "|" + pool.get(k).toString());
      }

      arrServerPoolDetail = new String[serverPoolList.size()][2];
      for(int k = 0; k < serverPoolList.size(); k++)
      {
        String arrTemp[] = rptUtilsBean.split(serverPoolList.get(k).toString(), "|");
        arrServerPoolDetail[k][0] = arrTemp[0];
        arrServerPoolDetail[k][1] = arrTemp[1];
      }
      return arrServerPoolDetail;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getServerPoolName", "", "", "Exception - ", e);
      return null;
    }
  }**/

  //////////////////////////////End of function/////////////////////////////

  public static void main(String[] args)
  {
    BatchJobs batchJobObj = new BatchJobs();
    boolean resultFlag;
    int choice = 0;

    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    System.out.println("********Please enter the option for desired operation*********");
    System.out.println("For BJOB Group details Enter : 1");
    System.out.println("For BJOB Name, Description, Modified Date, and # of monitor Enter : 2");
    System.out.println("To create new BJOB Enter : 3");
    System.out.println("To Add batch job in BJOB Group Enter : 4");
    System.out.println("To Delete BJOB Enter : 5");
    System.out.println("To Copy BJOB Group Enter : 6");
    System.out.println("To Save BJOB Enter : 7");
    System.out.println("To Create BJOB BakUp Enter : 8");
    System.out.println("To Delete Custom Monitor from BJOB Enter : 9");
    System.out.println("To check that BJOB is modified from the previous change or not Enter : 10");
    //System.out.println("Getting all vector name: 11");
    //System.out.println("Getting all Standard Monitor Details: 12");
    //System.out.println("Getting number of monitor: 13");
    //System.out.println("Getting Server and Pool Name: 14");
    System.out.println("*************************************************************");
    try
    {
      choice = Integer.parseInt(br.readLine());
    }
    catch(IOException e)
    {
      System.out.println("Error in entered choice: " + e);
    }

    switch(choice)
    {
      case 1:
        String profileName = null;
        System.out.println("Enter the batch job group Name : ");
        try
        {
          profileName = br.readLine();
        }
        catch(IOException e)
        {
          System.out.println("Error in entered profile : " + e);
        }
        //Fetch data of entered profile.
        String[][] arrData = batchJobObj.getBJOBDetails(profileName);
        for(int i = 0; i < arrData.length; i++)
          for(int j = 0; j < arrData[i].length; j++)
            System.out.println("data value[" + i + "][" + j + "] = " + arrData[i][j]);
        break;

      case 2:
        //To fetch details i.e. profile name, desc, last modified date, and number of custom monitor.
        String[][] arrMPROFData = batchJobObj.getBJOBsFromBatchGroup();
        for(int i = 0; i < arrMPROFData.length; i++)
          for(int j = 0; j < arrMPROFData[i].length; j++)
            System.out.println("data value[" + i + "][" + j + "] = " + arrMPROFData[i][j]);

        break;

      case 3:
        String newProfileName = null;
        String newProfileDesc = null;
        try
        {
          System.out.println("Enter Profile Name : ");
          newProfileName = br.readLine();
          System.out.println("Enter Profile Description : ");
          newProfileDesc = br.readLine();
        }
        catch(IOException e)
        {
          System.out.println("Error in entered arguments : " + e);
        }
        //Create New bak file for a non existing profile and existing profile.
        resultFlag = batchJobObj.createNewBJOB(newProfileName, newProfileDesc);
        System.out.println("Result = " + resultFlag);
        break;

      case 4:
        String cmon = null;
        String newProfile = null;
        try
        {
          System.out.println("Enter Batch Job Group Name : ");
          newProfile = br.readLine();
          System.out.println("Enter Batch Job Detail: ");
          cmon = br.readLine();
        }
        catch(IOException e)
        {
          System.out.println("Error in entered arguments : " + e);
        }

        String batchDetail = "BATCH_JOBS|192.168.1.64|%2Fhome%2Fnetstorm%2Fabc.txt|3|rampUp|00:00:10|1|Test|2";
        // add Custom Monitor to Profile
        resultFlag = batchJobObj.addBatchJobToBJOBGroup(newProfile, cmon, batchDetail);
        System.out.println("Result = " + resultFlag);
        break;

      case 5:
        String profileNameToBeDeleted = null;
        try
        {
          System.out.println("Enter batch job Group Name : ");
          profileNameToBeDeleted = br.readLine();
        }
        catch(IOException e)
        {
          System.out.println("Error in entered profile : " + e);
        }
        // delete profile .mprof and .hot file.
        resultFlag = batchJobObj.deleteBJOBGroup(profileNameToBeDeleted);
        System.out.println("Result = " + resultFlag);
        break;

      case 6:
        String srcProfileName = null;
        String destProfileName = null;
        String destProfileDesc = null;
        try
        {
          System.out.println("Enter Source Batch Group Name : ");
          srcProfileName = br.readLine();

          System.out.println("Enter Destination Batch Group Name : ");
          destProfileName = br.readLine();

          System.out.println("Enter Destination Batch Group Description : ");
          destProfileDesc = br.readLine();
        }
        catch(IOException e)
        {
          System.out.println("Error in entered string : " + e);
        }
        // copy src MPROF to destination MPROF
        resultFlag = batchJobObj.copyBJOBGroup(srcProfileName, destProfileName, destProfileDesc);
        System.out.println("Result = " + resultFlag);

        break;

      case 7:
        String profileNameToBeSaved = null;
        String profileDescToBeSaved = null;

        try
        {
          System.out.print("Enter Batch Group Name : ");
          profileNameToBeSaved = br.readLine();

          System.out.println("Enter Batch Group Description : ");
          profileDescToBeSaved = br.readLine();
        }
        catch(IOException e)
        {
          System.out.println("Error in entered string : " + e);
        }
        //save a new BJOB as .bjobs and .hot. and .hot for an existing MPROF.
        resultFlag = batchJobObj.saveBJOB(profileNameToBeSaved, profileDescToBeSaved);
        System.out.println("Result = " + resultFlag);
        break;

      case 8:
        String profileNameForBakUp = null;

        try
        {
          System.out.print("Enter Batch Group Name : ");
          profileNameForBakUp = br.readLine();
        }
        catch(IOException e)
        {
          System.out.println("Error in entered string : " + e);
        }

        // creates a bak file of passed arguments.
        resultFlag = batchJobObj.createBJOBBackup(profileNameForBakUp);
        System.out.println("Result = " + resultFlag);
        break;

      case 9:
        String profileNameToBeModified = null, profileDesc = null;
        int numberOfCMON = 0;
        String[] strCMON = null;
        try
        {
          System.out.print("Enter Profile Name : ");
          profileNameToBeModified = br.readLine();

          System.out.print("Enter Profile Desc : ");
          profileDesc = br.readLine();

          System.out.println("Enter how many custom monitor you want to delete : ");
          numberOfCMON = Integer.parseInt(br.readLine());

          strCMON = new String[numberOfCMON];
          for(int i = 0; i < numberOfCMON; i++)
          {
            System.out.println("Enter " + i + "th id : ");
            strCMON[i] = br.readLine();
          }
        }
        catch(IOException e)
        {
          System.out.println("Error in entered string : " + e);
        }
        //delete custom monitor from mprof
        resultFlag = batchJobObj.deleteBatchJobFromBJOBGroup(profileNameToBeModified, profileDesc, strCMON);
        System.out.println("Result = " + resultFlag);
        break;

      case 10:
        String profilename = null;
        try
        {
          System.out.println("Enter Profile Name : ");
          profilename = br.readLine();
        }
        catch(IOException e)
        {
          System.out.println("Error in entered profile : " + e);
        }
        // check if mprof file is modified from last change or not.
        resultFlag = batchJobObj.isBJOBModified(profilename);
        System.out.println("Result = " + resultFlag);
       break;

      /**case 11:
        //To fetch unique vector name
        TreeSet setAllUniqueVectorName = new TreeSet();
        setAllUniqueVectorName = batchJobObj.getCustomMonitorList();

        Iterator irVectorName = setAllUniqueVectorName.iterator();
        while(irVectorName.hasNext())
        {
          String strVectorName = irVectorName.next().toString();

          System.out.println("Vector Name = " + strVectorName);
        }
        break;
       **/
      /**case 12:
        //To fetch details i.e. profile name, desc, last modified date, and number of custom monitor.
        String[][] arrStdDataData = batchJobObj.getStdMonitorList();
        //for(int i = 0; i < arrStdDataData.length; i++)
          for(int j = 0; j < arrStdDataData.length; j++)
            System.out.println("data value[" + j + "] = " + arrStdDataData[j][0]);

        break;
      **/
      /**case 13:
        //To fetch details i.e. profile name, desc, last modified date, and number of custom monitor.
        int[] arrCountMon = batchJobObj.getMPROFCount("vector");
          for(int j = 0; j < arrCountMon.length; j++)
            System.out.println("data value[" + j + "] = " + arrCountMon[j]);

        break;
      **/
      /**case 14:
        //To fetch details i.e. profile name, desc, last modified date, and number of custom monitor.
        //LinkedHashMap  linkedHashMap = batchJobObj.getServerPoolName("","","","","");
        String arrTemp[][] = batchJobObj.getServerPoolName("","","","","");
        //Set st = linkedHashMap.keySet();
        //System.out.println("Set created from LinkedHashMap Keys contains :");
        //iterate through the Set of keys
        //Iterator itr = st.iterator();
        //while(itr.hasNext())
        //{
        //  String key = itr.next().toString();

        //  System.out.println("key = " + key);

        //  Vector pool = (Vector)linkedHashMap.get(key);
        //  for(int k = 0; k < pool.size(); k++)
        //    System.out.println("Pool[" + k + "] = " + pool.get(k).toString());
        //}

        for(int k = 0; k < arrTemp.length; k++)
        {
          for(int kk = 0; kk < arrTemp[k].length; kk++)
          {
            System.out.println("arrTemp[" + k + "][" + kk + "] = " + arrTemp[k][kk]);
          }
        }
        break;
        **/
       default:
         System.out.println("Please select the correct option.");
    }
  }
}
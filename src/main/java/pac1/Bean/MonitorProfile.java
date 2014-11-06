/*--------------------------------------------------------------------
  @Name      : MonitorProfile.java
  @Author    : Chitra
  @Purpose   : Provided the utility function(s) to monitor profile GUI(jsp)
  @Modification History:
      09/08/2008 -> Chitra (Initial Version)
      10/07/2012 -> Saloni (Changes for method getServerMBeanList for MBean based monitors)
      10/09/2013 -> Bala Sudheer(Added deleteAllMonitorsFromMPROFHavingSameGDFName method to delete all monitors having same GDF & changed getDecodeValueForFileSeperators method for bug fix )
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

public class MonitorProfile
{
  private static String className = "MonitorProfile";
  private static String workPath = Config.getWorkPath();
  private static String topologyName = null;
  private final static String MPROF_FILE_EXTN = ".mprof";
  private final static String MPROF_BAK_EXTN = ".hot"; // This is used to save the custom monitor profile for backup.
  private transient final String LAST_MODIFIED_DATE = "LAST_MODIFIED_DATE = ";

  private transient final int CUSTOM_VECTOR_NAME_INDEX = 3;
  private transient final int STANDARD_VECTOR_NAME_INDEX = 2;

  //This array is required for monitor gui, For each new windows monitor we need to update this array.
  //And we need to check page buffer in editStandardMonitor.jsp file in case of sendRedirectio Issue.
  public String windowsMonitor[][] = {
      {"AppPoolWASStats", ""},
      {"ASPActiveServerPagesStats", ""},
      {"ASPNET64BITStats", ""},
      {"ASPNETStats", ""},
      {"ASPNETStats64V2050727", "__Total__"},
      {"ASPNETStats64V4030319", "__Total__"},
      {"CacheStats", ""},
      {"HTTPServiceUrlGroupsStats", "_Total"},
      {"IPHTTPSGlobalStats", ""},
      {"LogicalDiskStats", "_Total"},
      {"MemoryStats", ""},
      {".NETCLRExeptionsStats", "_Global"},
      {".NETCLRInterropStats", "_Global"},
      {".NETCLRJitStats", "_Global"},
      {".NETCLRLoadingStats", "_Global"},
      {".NETCLRLocksAndThreadsStats", "_Global"},
      {".NETCLRMemoryStats", "_Global"},
      {".NETCLRRemotingStats", "_Global"},
      {".NETCLRSecurityStats", "_Global"},
      {".NETDataProviderForSqlServerStats", ""},
      {"PagingStats", ""},
      {"PhysicalDiskStats", ""},
      {"ProcessorInfo", ""},
      {"ProcessorStats", "_Total"},
      {"ProcessStats", "_Total"},
      {"ServerStats", ""},
      {"SQLServerAccessMethods", ""},
      {"SQLServerBufferManager", ""},
      {"SQLServerDatabase", "_Total"},
      {"SQLServerGeneralStatistics", ""},
      {"SQLServerLatches", "_Total"},
      {"SQLServerLocks", "_Total"},
      {"SQLServerMemoryManager", ""},
      {"SQLServerPlanCache", "_Total"},
      {"SQLServerSQLStatistics", ""},
      {"SQLServerSQLWaitStatistics", "_Total"},
      {"TCPIPNetworkInterfaceStats", ""},
      {"TCPIPTCP4Stats", ""},
      {"W3SVCWebServiceStats", "_Total"},
      {"W3SVC_W3WPStats", ""},
      {"WebServicesStats", ""},
      {"WinSystemStats", ""}
  };

  private static String toEncodeDecode[][] = {
	  {"\t", "%09"}
    ,{" ", "%20"}
    ,{"\\", "%5C"}
    ,{"'", "%27"}
    ,{";", "%3B"}
    ,{"\"", "%22"}
    ,{"-", "%2D"}
    ,{"=", "%3D"}
    //,{"|", "%7C"}
    //,{"`", "%60"}
  };

  private static String toDecodeEncode[][] = {
	  //{"\\\\t", "%5Ct"}
	  {"\\\\t", "%09"}
    ,{" ", "%20"}
    ,{"\\\\", "%5C"}
    ,{"'", "%27"}
    ,{";", "%3B"}
    ,{"\\\"", "%22"}
    ,{"-", "%2D"}
    ,{"=", "%3D"}
    //,{"|", "%7C"}
    //,{"`", "%60"}
  };

  public static String decode(String strUrl)
  {
    for(int i = 0 ; i < toEncodeDecode.length ; i++)
      strUrl = rptUtilsBean.replace(strUrl, toEncodeDecode[i][1], toEncodeDecode[i][0]);

    return strUrl;
  }

  //This is moved from ScriptUtilis.java file
  public static String encode(String strUrl)
  {
    for(int i = 0 ; i < toDecodeEncode.length ; i++)
    {
      strUrl = rptUtilsBean.replace(strUrl, toDecodeEncode[i][0], toDecodeEncode[i][1]);
    }
    return strUrl;
  }
  
  public void setTopologyName(String topologyName1)
  { 
    Log.debugLog(className, "setTopologyName", "", "", "Method called. " + topologyName1 );
    topologyName = topologyName1;
  }

  //This method will return title string for Standard Monitor GUI
  public String getTitle(String strOperAction)
  {
    String strTitle = "";

    if(strOperAction.equals("editStandardMonitor"))
      strTitle = "Edit Standard Monitor";
    else if(strOperAction.equals("copyStandardMonitor"))
      strTitle = "Copy Standard Monitor";
    else
      strTitle = "Add Standard Monitor";

    return strTitle;
  }

  // This will return the Custom Monitor profile's path
  private static String getMonitorPath()
  {
    Log.debugLog(className, "getMonitorPath", "", "", "Method called.:" + workPath +"/"+ topologyName + "/mprof/");
    if(topologyName != null && !topologyName.trim().equals(""))
      return (workPath  + "/mprof/" + topologyName + "/");
    else
      return (workPath + "/mprof/");
  }

  //This returns full path of passed profile
  private static String getMonitorProfileNameWithPath(String fileName)
  {
    return (getMonitorPath() + fileName);
  }

  // This will return the bak mprof name
  private String getBAKMPROFName(String mprofName)
  {
    return (getMonitorPath() + "." + mprofName + MPROF_FILE_EXTN + MPROF_BAK_EXTN);
  }

  //This will return the standard Monitor path
  private static String getStdMonitorPath()
  {
    return (workPath + "/etc/");
  }

  //This returns full path of passed profile
  private static String getStdMonitorProfileNameWithPath(String fileName)
  {
    return (getStdMonitorPath() + fileName);
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

  private static Vector readFile(String customMonitorProfileFileWithPath)
  {
    return readFile(customMonitorProfileFileWithPath, false);
  }

  // Method for reading the File
  private static Vector readFile(String customMonitorProfileFileWithPath, boolean isReadingDataFromMPROF)
  {
    Log.debugLog(className, "readFile", "", "", "Method called. Custom Monitor Profile File Name = " + customMonitorProfileFileWithPath + ", isReadingDataFromMPROF = " + isReadingDataFromMPROF);
    try
    {
      Vector vecFileData = new Vector();
      String strLine;

      File customMonProfile = openFile(customMonitorProfileFileWithPath);

      if(!customMonProfile.exists())
      {
        Log.errorLog(className, "readFile", "", "", "Custom Monitor Profile not found, filename - " + customMonitorProfileFileWithPath);
        return null;
      }

      FileInputStream fis = new FileInputStream(customMonitorProfileFileWithPath);
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
        if(isReadingDataFromMPROF)
        {
          try
          {
            decodedPattern = URLDecoder.decode(decodedPattern);
          }
          catch(Exception e)
          {
            decodedPattern = strLine;
            Log.debugLog(className, "readFile", "", "", customMonitorProfileFileWithPath + "is not decoded. Its contain special character as " + strLine);
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

  // load all mprof file in Array List
  private ArrayList loadAllMPROF()
  {
    Log.debugLog(className, "loadAllMPROF", "", "", "Method called");

    ArrayList arrListAllMPROF = new ArrayList();  // Names of all MPROF files
    ArrayList arrListOfAllMPROFSortedOrder = new ArrayList();  // Names of all MPROF files in sorted order

    File mprofFile = new File(getMonitorPath());
    String temp;
    String tempPath;

    if(!mprofFile.exists())
    {
      mprofFile.mkdirs();  
      
      if(rptUtilsBean.changeFilePerm(mprofFile.getAbsolutePath(), "netstorm", "netstorm", "775") == false)
        return null;
           
    }
    try
    {
      String arrAvailFiles[] = mprofFile.list();
      tempPath = getMonitorPath();

      for(int j = 0; j < arrAvailFiles.length; j++)
      {
        temp = "";
        if(arrAvailFiles[j].lastIndexOf(MPROF_FILE_EXTN) == -1)  // Skip non mprof files
          continue;

        temp = tempPath + arrAvailFiles[j];
        Log.debugLog(className, "loadAllMPROF", "", "", "Adding '.mprof' file name in ArrayList = " + temp);

        String[] tempArr = rptUtilsBean.strToArrayData(arrAvailFiles[j], "."); // for remove monitor profile extension
        if(tempArr.length == 2)
        {
          arrListAllMPROF.add(tempArr[0]);
          Log.debugLog(className, "loadAllMPROF", "", "", "Adding '.mprof' file name in ArrayList = " + temp);
        }
        else
          Log.debugLog(className, "loadAllMPROF", "", "", "Skiping file name  = " + temp);
      }

      Log.debugLog(className, "loadAllMPROF", "", "", "Number of '.mprof' files = " + arrListAllMPROF.size());

      Object[] arrTemp = arrListAllMPROF.toArray();
      Arrays.sort(arrTemp);

      for(int i = 0; i < arrTemp.length; i++)
        arrListOfAllMPROFSortedOrder.add(arrTemp[i].toString());

      return arrListOfAllMPROFSortedOrder;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "loadAllMPROF", "", "", "Exception - ", e);
      return null;
    }
  }

/*****************Get List of Standard Monitor *************************/
// This function returns the list of standard monitor

  public String[][] getStdMonitorList()
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
  }

/***********************************************************************/


// This function returns the Profile Description of passed Profile Name
  private String getMonitorProfileDesc(String tempProfileName)
  {
    Log.debugLog(className, "getMonitorProfileDesc", "", "", "Method called. File Name = " + tempProfileName);
    Properties tempProfileProp = new Properties();

    try
    {
      String tempProfileNameWithPath = getMonitorProfileNameWithPath(tempProfileName + MPROF_FILE_EXTN);
      File tempProfileFile = new File(tempProfileNameWithPath);
      FileInputStream fis = new FileInputStream(tempProfileFile);
      tempProfileProp.load(fis);

      String value = tempProfileProp.getProperty("PROFILE_DESC");
      if(value == null)
      {
        value = tempProfileName;
      }
      Log.debugLog(className, "getMonitorProfileDesc", "", "", "Custom Monitor Profile " + tempProfileName + "'s description = " + value.trim());
      return (value.trim());
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getMonitorProfileDesc", "", "", "Exception - ", e);
      return "";
    }
  }

  //This method returns 2D String array, which contains data like [0][0]=name, [0][1]=Description, [0][2]=Modified date, [0][3]=Number of Custom Monitor.
  public String[][] getMPROFs()
  {
    Log.debugLog(className, "getMPROFs", "", "", "Method called");

    try
    {
      ArrayList arrListAllMPROF = loadAllMPROF();  // Names of all MPROF files

      if(arrListAllMPROF == null)
        return null;

      String[][] strMonitorProfiles = new String[arrListAllMPROF.size() + 1][4]; // Add 1 for header.

      strMonitorProfiles[0][0] = "Monitor Group Name";
      strMonitorProfiles[0][1] = "Monitor Group Description";
      strMonitorProfiles[0][2] = "Modified Date";
      strMonitorProfiles[0][3] = "Number Of Monitors";

      for(int i = 0; i < arrListAllMPROF.size(); i++)
      {
        String lastModDateTime = "";
        String fileWithPath = getMonitorProfileNameWithPath(arrListAllMPROF.get(i).toString() + MPROF_FILE_EXTN);
        int headerCount = 0; // This is to count number of monitor in the profile.

        Vector vecProfileData = readFile(fileWithPath);

        if(vecProfileData == null)
        {
          Log.debugLog(className, "getMPROFs", "", "", "Custom Monitor Profile File not found, It may be correpted, File name " + arrListAllMPROF.get(i).toString());
          continue;
        }

        boolean profileDescFlag = false;
        boolean lastModifiedDateFlag = false;

        String dataLine = "";
        for(int ii = 0; ii < vecProfileData.size(); ii++)
        {
          dataLine = vecProfileData.get(ii).toString();

          if(dataLine.startsWith("PROFILE_DESC"))
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

        strMonitorProfiles[i + 1][0] = arrListAllMPROF.get(i).toString();    //Adding Profile name in to the String Array.
        Log.debugLog(className, "getMPROFs", "", "", "Adding Profile name in array = " + strMonitorProfiles[i + 1][0]);

        strMonitorProfiles[i + 1][1] = getMonitorProfileDesc(arrListAllMPROF.get(i).toString());   // Adding profile description in the String Array.
        Log.debugLog(className, "getMPROFs", "", "", "Adding Profile Description in array = " + strMonitorProfiles[i + 1][1]);

        for(int ii = 0; ii < vecProfileData.size(); ii++)
        {
          if((vecProfileData.elementAt(ii).toString()).startsWith(LAST_MODIFIED_DATE))
          {
             String[] arrTemp = rptUtilsBean.strToArrayData(vecProfileData.elementAt(ii).toString(), "=");
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

        strMonitorProfiles[i + 1][2] = lastModDateTime;  // Adding File's last modified date in to the String Array.
        Log.debugLog(className, "getMPROFs", "", "", "Adding Profile's last modified date in array = " + strMonitorProfiles[i + 1][2]);

        if(profileDescFlag || lastModifiedDateFlag)  //In the profile Profile Desc or last modified field is present.
          strMonitorProfiles[i + 1][3] = "" + (vecProfileData.size() - headerCount);   // - headerCount is to skip profile description and Last mofidied date.
        else  //In the profile Profile Desc or last modified field is not present, so there would be only monitors in that profile.
          strMonitorProfiles[i + 1][3] = "" + (vecProfileData.size());

        Log.debugLog(className, "getMPROFs", "", "", "Adding Number of Custom Monitor in array = " + strMonitorProfiles[i + 1][3]);
      }
      return strMonitorProfiles;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getMPROFs", "", "", "Exception - ", e);
      return null;
    }
  
  }

  // this is to count monitors for all monitor type individually.
  public int[] getMPROFCount(String monProfileName)
  {
    Log.debugLog(className, "getMPROFCount", "", "", "Method called, Monitor Profile Name = " + monProfileName);

    int[] getAllMonitorCount = null;
    if(!monProfileName.equals("") && monProfileName != null)
    {
      String[][] mprofDetail = getMPROFDetails(monProfileName);

      int countStndMon = 0, countSplMon = 0, countCustMon = 0, countDynamicMon = 0, countServerStatMon = 0, countServerSigMon = 0, countCheckMon = 0, countUserMon = 0;

      getAllMonitorCount = new int[8];

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
        
        /** Earlier LOG_MONITOR is used as SPECIAL_MONITOR
          *  but to handle old mprof following condition is there
	  *  As it is to be remove after all old mprof is replaced to LOG_MONITOR.
	  */
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
    }
  
    return getAllMonitorCount;
  }

  public int getMonitorCountOfSameGDF(String monProfileName, String gdfName)
  {
    Log.debugLog(className, "getMonitorCountOfSameGDF", "", "", "Method Starts. Monitor Profile Name: " + monProfileName + ", GDF Name: " + gdfName);
    
    int monCount = 0;
    if(!monProfileName.equals("") && monProfileName != null)
    {
      String[][] mprofDetail = getMPROFDetails(monProfileName);
      
      for(int i = 1; i < mprofDetail.length; i++)
      {
        if(mprofDetail[i] == null)
          continue;
        
        if(mprofDetail[i][0].startsWith("SPECIAL_MONITOR") || mprofDetail[i][0].startsWith("LOG_MONITOR"))
        {
          if(!gdfName.equals("") && gdfName != null)
          {
            if(mprofDetail[i][2].equals(gdfName))
              monCount++;
          }
          else
            Log.debugLog(className, "getMonitorCountOfSameGDF", "", "", "GDF Name is empty or null");
            
        } //end of if 
        
      } //end of for loop
    }
    else
      Log.debugLog(className, "getMonitorCountOfSameGDF", "", "", "Monitor Profile Name is empty or null");
    
    Log.debugLog(className, "getMonitorCountOfSameGDF", "", "", "Monitor count is: " + monCount);
    return monCount;
  }
  
  public String[][] getMPROFDetails(String monProfileName)
  {
    Log.debugLog(className, "getMPROFDetails", "", "", "Method called, file name = " + monProfileName);
    String fileWithPath = getBAKMPROFName(monProfileName);
    try
    {
      Vector vecProfileData = readFile(fileWithPath);

      if(vecProfileData == null)
      {
        Log.debugLog(className, "getMPROFDetails", "", "", "Custom Monitor Profile File not found, It may be correpted., file name = " + monProfileName);
        return null;
      }

      String[][] arrCustomMonitorDetails = new String[vecProfileData.size() - 1][];  //Skipping Profile description and last modified date and add mprof detail(profile details and last modified date).

      String[] arrMPROFDetail = new String[2];
      String[] arrTemp = null;

      String dataLine = "";
      int count = 1;

      for(int i = 0; i < vecProfileData.size(); i++)    //Profile description wiil be on the zeroth index and last modified date on oneth index.
      {
        dataLine = vecProfileData.elementAt(i).toString();

        if(dataLine.startsWith("PROFILE_DESC"))
        {
          arrTemp = null;
          arrTemp = rptUtilsBean.strToArrayData(dataLine, "=");
          arrMPROFDetail[0] = arrTemp[1].trim();
        }
        else if(dataLine.startsWith(LAST_MODIFIED_DATE))
        {
          arrTemp = null;
          arrTemp = rptUtilsBean.strToArrayData(dataLine, "=");
          arrMPROFDetail[1] = arrTemp[1].trim();
        }
        else
        {
          arrCustomMonitorDetails[count] = rptUtilsBean.strToArrayData(dataLine, " ");
          count++;
        }
      }
      arrCustomMonitorDetails[0] = arrMPROFDetail; // append profile desc and last modified date at first index

      return arrCustomMonitorDetails;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getMPROFDetails", "", "", "Exception - ", e);
      return null;
    }
  }

  /***********************************************************************************************/
  //                              Utility function(s) called from JSP                            //
  /***********************************************************************************************/

  // Delete methods
  // This delete the existing mprof file
  public boolean deleteMPROF(String mprofName)
  {
    Log.debugLog(className, "deleteMPROF", "", "", "MProf Name = " +  mprofName);
    deleteMPROF(mprofName, MPROF_BAK_EXTN);
    return(deleteMPROF(mprofName, MPROF_FILE_EXTN));
  }

  // This delete the  mprof backup if present
  public boolean deleteBakMonitorProfile(String mprofName)
  {
    Log.debugLog(className, "deleteBakMonitorProfile", "", "", "MPROF Name = " +  mprofName);
    return(deleteMPROF(mprofName, MPROF_BAK_EXTN));
  }

  // This method delete existing MPROF
  private boolean deleteMPROF(String mprofName, String fileExtn)
  {
    Log.debugLog(className, "deleteMPROF", "", "", "MPROF File = " + mprofName + "." + fileExtn);
    String tmpFile = null;

    if(fileExtn.equals(MPROF_FILE_EXTN))
      tmpFile = getMonitorProfileNameWithPath(mprofName + fileExtn);
    else
      tmpFile = getBAKMPROFName(mprofName);

    try
    {
      File fileObj = new File(tmpFile);
      if(fileObj.exists())
      {
        boolean success = fileObj.delete();
        if (!success)
        {
          Log.errorLog(className, "deleteMPROF", "", "", "Error in deleting mprof file (" + tmpFile + ")");
          return false;
        }
      }
      else
      {
        Log.errorLog(className, "deleteMPROF", "", "", "MPROF file (" + tmpFile + ") does not exist.");
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "deleteMPROF", "", "", "Exception - ", e);
      return false;
    }
    return true;
  }



  
  //Make Bak file of MPROF
  // All edit/changes are done in Backup file
  public boolean createMPROFBackup(String mprofName)
  {
    Log.debugLog(className, "createMPROFBackup", "", "", "MPROF Name = " +  mprofName );

    try
    {
      String mprofFileName = getMonitorProfileNameWithPath(mprofName + MPROF_FILE_EXTN);

      File mprofFileObj = new File(mprofFileName);
      if(!mprofFileObj.exists())
      {
        Log.errorLog(className, "createMPROFBackup", "", "", "Source mprof does not exist. MPROF name = " + mprofName);
        return false;
      }

      String mprofBakFileName =  getBAKMPROFName(mprofName);

      if(copyMPROFFile(mprofFileName, mprofBakFileName, "", "", false, false) == false)
      {
        Log.errorLog(className, "createMPROFBackup", "", "", "Error in creating Bakup of MPROF. MPROF file is " + mprofFileName + ". Backup file is " + mprofBakFileName);
        return false;
      }
      return(true);
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "createMPROFBackup", "", "", "Exception - ", e);
      return false;
    }
  }
  
  
  

  // Copy source mprof file to destination mprof file.
  private boolean copyMPROFFile(String srcFileName, String destFileName, String destName, String destDesc, boolean updCreDate, boolean updModDate)
  {
    Log.debugLog(className, "copyMPROFFile", "", "", "Method called");

    try
    {
      File fileSrc = new File(srcFileName);
      File fileDest = new File(destFileName);

      Vector vecSourceFileData = readFile(srcFileName);
      boolean lastModDateFlag = false;
      boolean profileDescFlag = false;

      if(!fileSrc.exists())
      {
        Log.errorLog(className, "copyMPROFFile", "", "", "Source file does not exists. Filename = " + srcFileName);
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
        if(dataLine.startsWith("PROFILE_DESC"))
          profileDescFlag = true;
      }


      FileInputStream fin = new FileInputStream(fileSrc);
      BufferedReader br = new BufferedReader(new InputStreamReader(fin));

      FileOutputStream fout = new FileOutputStream(fileDest, true);  // append mode
      PrintStream pw = new PrintStream(fout);

      if(!lastModDateFlag && !profileDescFlag) //Last modified date & profile desc is not in the profile, then write profile desc and current date in the file in the file.
      {
        pw.println("PROFILE_DESC = " + "NA");
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
        if(str.startsWith("PROFILE_DESC"))
        {
          if(!destDesc.equals(""))
            str = "PROFILE_DESC = " + destDesc;
        }

        pw.println(str);
      }

      pw.close();
      br.close();
      fin.close();
      fout.close();
      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "copyMPROFFile", "", "", "Exception - ", e);
      return false;
    }
  }

  // This will delete selected custom monitor from mprof
  // selected custom monitor are passed as array of index.
  public boolean deleteMonitorFromMPROF(String mprofName, String[] arrProfIdx)
  {
    Log.debugLog(className, "deleteMonitorFromMPROF", "", "", "MPROF Name = " + mprofName);

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
          for(int k = 0; k < arrProfIdx.length; k++)
          {
            if((i - 2) == (int)Integer.parseInt(arrProfIdx[k])) // substratct by 2 because first line is description and second is last modified date of profile
            {
              status = "true";
              break;
            }
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
      Log.stackTraceLog(className, "deleteMonitorFromMPROF", "", "", "Exception - ", e);
      return false;
    }
  }

  /* This is to remove monitor which are having same GDF Name.
   * Param - mprofName gdfName vectorName serverName as arguments
   * return boolean
   */
  public boolean deleteMonitorFromMPROFHavingSameGDFName(String mprofName, String gdfName, String currVectorName)
  {
    Log.debugLog(className, "deleteMonitorFromMPROFHavingSameGDFName", "", "", "MPROF Name = " + mprofName + ", GDF Name = " + gdfName + ", Curr Vector Name: " + currVectorName);

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

      boolean isVectorMatched = false;
 
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
          
          Log.debugLog(className, "deleteMonitorFromMPROFHavingSameGDFName", "", "", "Line: " + dataLine);
          /** Earlier LOG_MONITOR is used as SPECIAL_MONITOR
           *  but to handle old mprof following condition is there
           *  As it is to be remove after all old mprof is replaced to LOG_MONITOR.
           */
          if(vecData.elementAt(i).toString().startsWith("SPECIAL_MONITOR") || vecData.elementAt(i).toString().startsWith("LOG_MONITOR"))
          {
            String[] monitorData = rptUtilsBean.split(vecData.elementAt(i).toString(), " ");

            // As GDF Name and vectorName both together is unique for every Log Monitor.
            //if(monitorData[2].equals(gdfName) && monitorData[3].equals(vectorName))
            if(monitorData[2].equals(gdfName))
            {
              if(monitorData[3].equals(currVectorName))
                status = "false";
              else if(!isVectorMatched) 
              {
                status = "true";
                isVectorMatched = true;
              } 
              else
                status = "true";
            }
          }

          if(!status.equals("true"))
            pw.println(vecData.elementAt(i).toString());

          if(isVectorMatched)
          {
            for(int k = (i + 1); k < vecData.size(); k++)
              pw.println(vecData.elementAt(k).toString());
 
            break;
          }
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
  }
  
  /* This is to remove monitor which are having same GDF Name.
   * Param - mprofName gdfName
   * return boolean
   */
  public boolean deleteAllMonitorsFromMPROFHavingSameGDFName(String mprofName, String gdfName)
  {
    Log.debugLog(className, "deleteAllMonitorsFromMPROFHavingSameGDFName", "", "", "MPROF Name = " + mprofName + ", GDF Name = " + gdfName );

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
          status = "true";
          
          Log.debugLog(className, "deleteAllMonitorsFromMPROFHavingSameGDFName", "", "", "Line: " + dataLine);
          /** Earlier LOG_MONITOR is used as SPECIAL_MONITOR
           *  but to handle old mprof following condition is there
           *  As it is to be remove after all old mprof is replaced to LOG_MONITOR.
           */
          if(vecData.elementAt(i).toString().startsWith("SPECIAL_MONITOR") || vecData.elementAt(i).toString().startsWith("LOG_MONITOR"))
          {
            String[] monitorData = rptUtilsBean.split(vecData.elementAt(i).toString(), " ");

            if(monitorData[2].equals(gdfName))
              status = "false";
          
            if(status.equals("true"))
              pw.println(vecData.elementAt(i).toString());
 
          
          }
        }
      }
      pw.close();
      fout.close();

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "deleteAllMonitorsFromMPROFHavingSameGDFName", "", "", "Exception - ", e);
      return false;
    }
  }

  //Add Custom Monitor into the mprof profile.
  public boolean addMonitorToMPROF(String mprofName, String monitorDetails)
  {
    Log.debugLog(className, "addMonitorToMPROF", "", "", "MPROF Name =" + mprofName);
    return(addMonitorToMPROF(mprofName, monitorDetails, false));
  }

  // Add Custom Monitor to mprof file. Add is done to backup file only.
  public boolean addMonitorToMPROF(String mprofName, String monitorDetails, boolean fileType)
  {
    Log.debugLog(className, "addMonitorToMPROF", "", "", "MPROF Name =" + mprofName + ", monitorDetails = " + monitorDetails);

    String mprofNameWithPath = "";

    if(fileType)
      mprofNameWithPath = getMonitorProfileNameWithPath(mprofName + MPROF_FILE_EXTN);
    else
      mprofNameWithPath = getBAKMPROFName(mprofName);

    try
    {
      // Read file's content
      Vector vecData = readFile(mprofNameWithPath);

      File mprofFileObj = new File(mprofNameWithPath);

      mprofFileObj.delete(); // Delete mprof file
      mprofFileObj.createNewFile(); // Create new mprof file

      if(rptUtilsBean.changeFilePerm(mprofFileObj.getAbsolutePath(), "netstorm", "netstorm", "664") == false)
        return false;

      if(!mprofFileObj.exists())
      {
        Log.errorLog(className, "addMonitorToMPROF", "", "", "MPROF file does not exist. MPROF filename is - " + mprofNameWithPath);
        return(false);
      }

      FileOutputStream fout = new FileOutputStream(mprofFileObj, true);  // append mode
      PrintStream pw = new PrintStream(fout);

      String dataLine = "";
      for(int i = 0; i < vecData.size(); i++)
      {
        dataLine = vecData.get(i).toString();
        if(dataLine.startsWith(LAST_MODIFIED_DATE))
          dataLine = LAST_MODIFIED_DATE + rptUtilsBean.getCurDateTime();

        pw.println(dataLine);
      }
      pw.println(monitorDetails);  // Append the new monitor to the end of file
      pw.close();
      fout.close();
      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "addMonitorToMPROF", "", "", "Exception - ", e);
      return false;
    }
  }


  public boolean createNewMPROF(String mprofName, String profDesc)
  {
    Log.debugLog(className, "createNewMPROF", "", "", "MPROF Name =" + mprofName + ", profile description = " + profDesc);
    return(createNewMPROF(mprofName, profDesc, false));
  }


  // Create new mprof. New mprof is always create as bak file.
  public boolean createNewMPROF(String mprofName, String profDesc, boolean checkFileType)
  {
    Log.debugLog(className, "createNewMPROF", "", "", "MPROF Name =" + mprofName + ", profile desc = " + profDesc);
    String mprofNameWithPath = "";

    try
    {
      // This will allow user to create mprof file with .mprof extn.
      if(checkFileType)
        mprofNameWithPath = getMonitorProfileNameWithPath(mprofName + MPROF_FILE_EXTN);
      else
        mprofNameWithPath = getBAKMPROFName(mprofName);

      File mprofFileObj = new File(mprofNameWithPath);

      if(mprofFileObj.exists())
      {
        Log.errorLog(className, "createNewMPROF", "", "", "MPROF already exists. MPROF name = " + mprofName);
        return false;
      }

      mprofFileObj.createNewFile();

      if(rptUtilsBean.changeFilePerm(mprofFileObj.getAbsolutePath(), "netstorm", "netstorm", "664") == false)
        return false;

      FileOutputStream fout = new FileOutputStream(mprofFileObj, true);  // append mode
      PrintStream pw = new PrintStream(fout);

      String profileDescLine = "PROFILE_DESC = " + profDesc;
      pw.println(profileDescLine);
      pw.println(LAST_MODIFIED_DATE + rptUtilsBean.getCurDateTime());
      pw.close();
      fout.close();
      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "createNewMPROF", "", "", "Exception - ", e);
      return false;
    }
  }


  // Save mprof will delete mprof file and copy bak file to mprof file
  // Bak is not deleted as GUI needs this file. Mod date/time is also updated in mprof file.
  public boolean saveMPROF(String mprofName, String profileDesc)
  {
    Log.debugLog(className, "saveMPROF", "", "", "MPROF File =" + mprofName + ", profile description = " + profileDesc);

    try
    {
      // First check if Bak file is existing or not. If not create new mprof Bak file
      String mprofBakFileName = getBAKMPROFName(mprofName);

      File mprofBakFileObj = new File(mprofBakFileName);

      if(!mprofBakFileObj.exists())
        createNewMPROF(mprofName, profileDesc);

      // Now copy Bak file to mprof file to save the changes
      String mprofFileName = getMonitorProfileNameWithPath(mprofName + MPROF_FILE_EXTN);

      if(copyMPROFFile(mprofBakFileName, mprofFileName, "", profileDesc, false, true) == false)
      {
        Log.errorLog(className, "saveMPROF", "", "", "Error in copying Bak to mprof (" + mprofFileName + ")");
        return false;
      }

      // Now copy mprof to Bak file so that Bak is same as mprof file
      if(copyMPROFFile(mprofFileName, mprofBakFileName, "", "", false, false) == false)
      {
        Log.errorLog(className, "saveMPROF", "", "", "Error in copying mprof to Bak (" + mprofFileName + ")");
        return false;
      }

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "saveMPROF", "", "", "Exception - ", e);
      return false;
    }
  }


  // To check if mprof is modified or not after last save was done
  public boolean isMPROFModified(String mprofName)
  {
    Log.debugLog(className, "isMPROFModified", "", "", "Method called. Profile Name = " + mprofName );
    try
    {

      String mprofFileName =getMonitorProfileNameWithPath(mprofName + MPROF_FILE_EXTN);
      String  mprofBakFileName = getBAKMPROFName(mprofName);
   
      Vector vecData = readFile(mprofFileName);
      Vector vecBakData = readFile(mprofBakFileName);

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

      Log.debugLog(className, "isMPROFModified", "", "", "Modification Date/Time for MPROF and Bak file are - " + strDate + " and " + strBakDate);

      // Compare modified file
      if((rptUtilsBean.convertDateToMilliSec(strDate)) == (rptUtilsBean.convertDateToMilliSec(strBakDate)))
        return false;

      return true;

    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "isMPROFModified", "", "", "Exception - ", e);
      return false;
    }
  }


/***************************************Start of function ***********************************/


 // This function return list of command tokens, by keeping ("") data as one token
  public ArrayList getCommandTokensList(String cmdDataLine)
  {

    Log.debugLog(className, "getCommandTokensList", "", "", "Method called, Command data line = " + cmdDataLine);

    try
    {
      ArrayList arryListCmdTokenData = new ArrayList();

      String[] strData = rptUtilsBean.strToArrayData(cmdDataLine, " "); // split cmd line by space

      int tokenCounter = 0;
      String strToken = "";

      for(int i = 0; i < strData.length; i++)
      {
        int index = strData[i].indexOf("\"");
        int endIndex;

        //start and last character is ".  startsWith
        if((strData[i].startsWith("\"")) && (((endIndex = strData[i].lastIndexOf("\"")) != -1) && ((endIndex = strData[i].lastIndexOf("\"")) != 0)))
        {
          strToken = strData[i].substring(index+1, (strData[i].length()-1));
          //System.out.println("  strToken  start and end " + strToken);
        }
        else if(index != -1) // if " sub string found
        {
          if(tokenCounter == 0) // " substring found, increment counter & assign value to strToken
          {
            tokenCounter++;
            strToken = strData[i].substring(index+1, strData[i].length());
          }
          else // " substring found & counter have non zero value, then decrement counter & append value of last space seperated substring
          {
            tokenCounter--;
            strToken = strToken + " " + strData[i].substring(0, index);
          }
          //System.out.println("  strToken  start  " + strToken);
        }
        else // if " substring  not found
        {
          if(tokenCounter == 0) // if counter is 0 & " substring not found then token is same as strData
            strToken = strData[i];
          else // if counter is not 0 & " string not found then strtoken has appended by strData(space seperated) this is for multiple tokens like (-c "/tmp/command_with_argument.sh 1 4 6")
            strToken = strToken + " " + strData[i];
          //System.out.println("  strToken  start  " + strToken);

        }

        if(tokenCounter == 0) // when counter is zero then add token value to list
          arryListCmdTokenData.add(strToken);
      }

      Log.debugLog(className, "getCommandTokensList", "", "", "Number of tokens = " + arryListCmdTokenData.size() + " Command tokens = " + arryListCmdTokenData);

      return arryListCmdTokenData;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "getCommandTokensList", "", "", "Exception in getCommandTokensList() -" + e);
      return null;
    }
  }

  /* This is to retrieve URL encode for the special characters which are placed as 'Others' in Field Seperators.
   *
   * Param args @ String as URL encoded format
   * Return String as Special Characters in URL Decoded format other than Tab(%09), Space(+), Double Quotes(%22), Single Quotes(%27), Comma(%2C), BackSlash(%5C), Dash(-), Semi colon(%3B), Equal(%3D).
   */
  public String getDecodeValueForFileSeperators(String strLine)
  {
    Log.debugLog(className, "getDecodeValueForFileSeperators", "", "", "strLine = " + strLine  + ", " + strLine.length());

    String otherFileSeperators = "", decodeFileSeperators = "";

    String strTemp = "";
    int y = 0;

    for(int i = 0; i < strLine.length(); i++, y++)
    {
      strTemp = strTemp.concat(String.valueOf(strLine.charAt(i)));

      if(!strTemp.equals("+") && !strTemp.equals("-")&& !strTemp.equals("*") && !strTemp.equals("_") && !strTemp.equals("."))
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

    /**decode pattern.
     * The alphanumeric characters "a" through "z", "A" through "Z" and "0" through "9" remain the same.
     * The special characters ".", "-", "*", and "_" remain the same.
     * The plus sign "+" is converted into a space character " " .
     * A sequence of the form "%xy" will be treated as representing a byte where xy is the two-digit hexadecimal representation of the 8 bits. Then, all substrings that contain one or more of these byte sequences consecutively will be replaced by the character(s) whose encoding would result in those consecutive bytes. The encoding scheme used to decode these characters may be specified, or if unspecified, the default encoding of the platform will be used.
     **/

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
  }

  /********************************************** End Of function ********************************/


  

 /********************************************** End Of function ********************************/


  // Make copy of a mprof with new name and profile desc
  // Also creation date and modification date is new mprof is set to current date/time
  public boolean copyMPROF(String strSrcMPROFName, String strDestMPROFName, String strProfileDesc)
  {

    Log.debugLog(className, "copyMPROF", "", "", "SrcName =" +  strSrcMPROFName + ", DestName = " + strDestMPROFName + ", Profile Description = " + strProfileDesc);

    try
    {
      String mprofSrcFileName = getMonitorProfileNameWithPath(strSrcMPROFName + MPROF_FILE_EXTN);
      String mprofDestFileName =getMonitorProfileNameWithPath(strDestMPROFName + MPROF_FILE_EXTN);

      File mprofSrcFileObj = new File(mprofSrcFileName);
      if(!mprofSrcFileObj.exists())
      {
        Log.errorLog(className, "copyMPROF", "", "", "Source mprof does not exist. MPROF name = " + strSrcMPROFName);
        return false;
      }

      File mprofDestFileObj = new File(mprofDestFileName);
      if(mprofDestFileObj.exists())
      {
        Log.errorLog(className, "copyMPROF", "", "", "Destination mprof already exists. MPORF name = " + strDestMPROFName);
        return false;
      }
      
      FileInputStream fin = new FileInputStream(mprofSrcFileName);
      BufferedReader br = new BufferedReader(new InputStreamReader(fin));

      FileOutputStream fout = new FileOutputStream(mprofDestFileName, true);  // append mode
      PrintStream pw = new PrintStream(fout);

      String dataLine = "";
      while((dataLine = br.readLine()) != null)
      {
        if(dataLine.startsWith("PROFILE_DESC"))
        {
          dataLine = "PROFILE_DESC = " + strProfileDesc;
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


      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "copyMPROF", "", "", "Exception - ", e);
      return false;
    }
  
  }

  ///////////////////////////////Start of function //////////////////////////

  // this function return the unique list of Vector Name available in mprof, In Sorted order
  public TreeSet getCustomMonitorList()
  {
    Log.debugLog(className, "getCustomMonitorList", "", "", "Method called.");

    try
    {
      ArrayList arrListAllMPROF = loadAllMPROF();  // Names of all MPROF files

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
  }
  
  
  /**
   * This method the server and pool name from the welogic server
   * @param serverName
   * @param userName
   * @param pwd
   * @param bolServer
   * @param bolPoolName
   * @return server pool list
   * nsu_server_admin -s 192.168.1.41 -c 'java -cp .:/root/beahome/wlserver_10.3/server/lib/wljmxclient.jar cm_weblogic_monitor_args 192.168.1.41 7001 weblogic weblogic' -g
     cmd_args = java:-cp .%3A/root/beahome/wlserver_10.3/server/lib/wljmxclient.jar cm_weblogic_monitor_args 192.168.1.41 7001 weblogic weblogic
     SERVER_NAME = examplesServer
     examples-demoXA-2
     examples-demoXA
     examples-demo
     LinkedHashMap
   **/
  
  public String[][] getServerPoolName(String monServerName, String webHostName, String webPort, String webUserName, String webPwd)
  {
    return  getServerPoolName(monServerName, webHostName, webPort, webUserName, webPwd, "");
  }

  /**
   * This method the server and pool name from the welogic server
   * @param serverName
   * @param userName
   * @param pwd
   * @param bolServer
   * @param bolPoolName
   * @param topologyName
   * @return server pool list
   *  nsu_server_admin -s 192.168.1.41 -c 'java -cp .:/root/beahome/wlserver_10.3/server/lib/wljmxclient.jar cm_weblogic_monitor_args 192.168.1.41 7001
      weblogic weblogic' -t topologyName -g
      cmd_args = java:-cp .%3A/root/beahome/wlserver_10.3/server/lib/wljmxclient.jar cm_weblogic_monitor_args 192.168.1.41 7001 weblogic weblogic
      SERVER_NAME = examplesServer
      examples-demoXA-2
      examples-demoXA
      examples-demo
   **/
  public String[][] getServerPoolName(String monServerName, String webHostName, String webPort, String webUserName, String webPwd, String topologyName)
  {
    Log.debugLog(className, "getServerPoolName", "", "", "Method called for Topology: " + topologyName + " Server : " + monServerName);

    //Maintain server name as a key and value is object of vector for pool name
    LinkedHashMap linkedHashMap = new LinkedHashMap();

    String[][] arrServerPoolDetail = null;
    try
    {
      CmdExec exec = new CmdExec();
      String strCmdName = "nsu_server_admin";
      String strCmdArgs = " -s " + monServerName + " -c " + "\'" + "java cm_weblogic_monitor_args "  + webHostName + " " + webPort + " " + webUserName + " " + webPwd + "\'" + " -g";
      
      if(topologyName != null && !topologyName.trim().equals(""))
        strCmdArgs = strCmdArgs + " -t " + topologyName.trim();
      
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
  }

  /**
   * This method returns output of the following command
   * nsu_server_admin -s <server> 'java -cp .:/opt/cavisson/monitors/lib/CmonLib.jar:/opt/cavisson/monitors/lib/java-getopt-1.0.9.jar:/opt/cavisson/monitors/bin: cm_spring_server_stats -D -h <host name> -p <port> -t <mbean type>'
   * e.g., nsu_server_admin -s 192.168.1.91 -c 'java -cp .:/opt/cavisson/monitors/lib/CmonLib.jar:/opt/cavisson/monitors/lib/java-getopt-1.0.9.jar:/opt/cavisson/monitors/bin: cm_spring_server_stats -D -h 192.168.1.91 -p 6969 -t Servlet'
   * @param monServerName - server name to be given with -s option
   * @param args - arguments to be given with program name
   * @return
   */
  public Vector getServerMBeanList(String monServerName, String args)
  {
    Log.debugLog(className, "getServerMBeanList", "", "", "Method called.");

    try
    {
      CmdExec exec = new CmdExec();
      String strCmdName = "nsu_server_admin";
      //-h 192.168.1.91 -p 6969 -M true -t Servlet
      String strCmdArgs = " -s " + monServerName + " -c " + "\'" + "java cm_mbean_server_stats"  + args + "\'" + " -g";
      Log.debugLog(className, "getServerMBeanList", "", "", "strCmdName = " + strCmdName + "\n" + "strCmdArgs = " + strCmdArgs);
      //System.out.println("strCmdName = " + strCmdName + "\n" + "strCmdArgs = " + strCmdArgs);
      Vector vecCmdOut = exec.getResultByCommand(strCmdName, strCmdArgs, 0, null, "root");

      if((vecCmdOut.size() > 0) && ((String)vecCmdOut.lastElement()).startsWith("ERROR"))
        return null;

      if(vecCmdOut == null)
        return null;


      return vecCmdOut;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getServerMBeanList", "", "", "Exception - ", e);
      return null;
    }
  }

  /**
   * This method is used in Standard Monitor GUI
   * @return - true if monitor is a windows monitor
   */
  public boolean isMonitorWindowsVBSType(String monName)
  {
    Log.debugLog(className, "isMonitorWindowsVBSType", "", "", "Method called.");
    for(int i = 0; i < windowsMonitor.length; i++)
    {
      Log.debugLog(className, "isMonitorWindowsVBSType", "", "", "Method called.");
      if(windowsMonitor[i][0].equals(monName))
        return true;
    }
    return false;
  }

  /**
   * This monitor returns the argument for particular windows monitor
   * @return - _Total, _Global etc or NULL
   */
  public String getMonitorWindowsVBSTypeArgument(String monName)
  {
    //Return _Total, _Gloabl etc or NULL
    Log.debugLog(className, "getMonitorWindowsVBSTypeArgument", "", "", "Method called.");
    for(int i = 0; i < windowsMonitor.length; i++)
    {
      Log.debugLog(className, "getMonitorWindowsVBSTypeArgument", "", "", "Method called.");
      if(windowsMonitor[i][0].equals(monName))
        return windowsMonitor[i][1];
    }
    return null;
  }



  //////////////////////////////End of function/////////////////////////////

  public static void main(String[] args)
  {
    MonitorProfile monitorProfile = new MonitorProfile();
    boolean resultFlag;
    int choice = 0;

    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    System.out.println("********Please enter the option for desired operation*********");
    System.out.println("For MPROF details Enter : 1");
    System.out.println("For MPROF Name, Description, Modified Date, and # of monitor Enter : 2");
    System.out.println("To create new MPROF Enter : 3");
    System.out.println("To Add custom monitor in MPROF Enter : 4");
    System.out.println("To Delete MPROF Enter : 5");
    System.out.println("To Copy MPROF Enter : 6");
    System.out.println("To Save MPROF Enter : 7");
    System.out.println("To Create MPROF BakUp Enter : 8");
    System.out.println("To Delete Custom Monitor from MPROF Enter : 9");
    System.out.println("To check that MPROF is modified from the previous change or not Enter : 10");
    System.out.println("Getting all vector name: 11");
    System.out.println("Getting all Standard Monitor Details: 12");
    System.out.println("Getting number of monitor: 13");
    System.out.println("Getting Server and Pool Name: 14");
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
        System.out.println("Enter the Profile Name : ");
        try
        {
          profileName = br.readLine();
        }
        catch(IOException e)
        {
          System.out.println("Error in entered profile : " + e);
        }
        //Fetch data of entered profile.
        String[][] arrData = monitorProfile.getMPROFDetails(profileName);
        for(int i = 0; i < arrData.length; i++)
          for(int j = 0; j < arrData[i].length; j++)
            System.out.println("data value[" + i + "][" + j + "] = " + arrData[i][j]);
        break;

      case 2:
        //To fetch details i.e. profile name, desc, last modified date, and number of custom monitor.
        String[][] arrMPROFData = monitorProfile.getMPROFs();
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
        resultFlag = monitorProfile.createNewMPROF(newProfileName, newProfileDesc);
        System.out.println("Result = " + resultFlag);
        break;

      case 4:
        String cmon = null;
        String newProfile = null;
        try
        {
          System.out.println("Enter Profile Name : ");
          newProfile = br.readLine();
          System.out.println("Enter Custom Monitor : ");
          cmon = br.readLine();

          if(cmon.equals("CUSTOM_MONITOR"))
          {
            System.out.print("Enter Custom Monitor Name : ");
            cmon = cmon + " " + br.readLine();

            System.out.print("Enter GDF Name : ");
            cmon = cmon + " " + br.readLine();

            System.out.print("Enter Run Option : ");
            cmon = cmon + " " + br.readLine();

            System.out.print("Enter Print Mode : ");
            cmon = cmon + " " + br.readLine();

            System.out.print("Enter Create Server IP : ");
            cmon = cmon + " " + br.readLine();

            System.out.print("Enter Access : ");
            cmon = cmon + " " + br.readLine();

            System.out.print("Enter Machine IP : ");
            cmon = cmon + " " + br.readLine();

            System.out.print("Enter User Name : ");
            cmon = cmon + " " + br.readLine();

            System.out.print("Enter Passward : ");
            cmon = cmon + " " + br.readLine();

            System.out.print("Enter Program Name with option: ");
            cmon = cmon + " " + br.readLine();
          }
          else if(cmon.equals("SERVER_STATS"))
          {
            int numberOfIP = 0;
            System.out.print("How many IP you want to give: ");
            numberOfIP = Integer.parseInt(br.readLine());

            for(int i = 0; i < numberOfIP; i++)
            {
              System.out.println("Enter " + i + "th IP : ");
              cmon = cmon + " " + br.readLine();
            }
           }
           else if(cmon.equals("SERVER_PERF_STATS"))
           {
             System.out.print("Enter Option : ");
             cmon = cmon + " " + br.readLine();

             System.out.print("Enter Server Address : ");
             cmon = cmon + " " + br.readLine();
           }
        }
        catch(IOException e)
        {
          System.out.println("Error in entered arguments : " + e);
        }
        // add Custom Monitor to Profile
        resultFlag = monitorProfile.addMonitorToMPROF(newProfile, cmon);
        System.out.println("Result = " + resultFlag);
        break;

      case 5:
        String profileNameToBeDeleted = null;
        try
        {
          System.out.println("Enter Profile Name : ");
          profileNameToBeDeleted = br.readLine();
        }
        catch(IOException e)
        {
          System.out.println("Error in entered profile : " + e);
        }
        // delete profile .mprof and .hot file.
        resultFlag = monitorProfile.deleteMPROF(profileNameToBeDeleted);
        System.out.println("Result = " + resultFlag);
        break;

      case 6:
        String srcProfileName = null;
        String destProfileName = null;
        String destProfileDesc = null;
        try
        {
          System.out.println("Enter Source Profile Name : ");
          srcProfileName = br.readLine();

          System.out.println("Enter Destination Profile Name : ");
          destProfileName = br.readLine();

          System.out.println("Enter Destination Profile Description : ");
          destProfileDesc = br.readLine();
        }
        catch(IOException e)
        {
          System.out.println("Error in entered string : " + e);
        }
        // copy src MPROF to destination MPROF
        resultFlag = monitorProfile.copyMPROF(srcProfileName, destProfileName, destProfileDesc);
        System.out.println("Result = " + resultFlag);

        break;

      case 7:
        String profileNameToBeSaved = null;
        String profileDescToBeSaved = null;

        try
        {
          System.out.print("Enter Profile Name : ");
          profileNameToBeSaved = br.readLine();

          System.out.println("Enter Profile Description : ");
          profileDescToBeSaved = br.readLine();
        }
        catch(IOException e)
        {
          System.out.println("Error in entered string : " + e);
        }
        //save a new MPROF as .mprof and .hot. and .hot for an existing MPROF.
        resultFlag = monitorProfile.saveMPROF(profileNameToBeSaved, profileDescToBeSaved);
        System.out.println("Result = " + resultFlag);
        break;

      case 8:
        String profileNameForBakUp = null;

        try
        {
          System.out.print("Enter Profile Name : ");
          profileNameForBakUp = br.readLine();
        }
        catch(IOException e)
        {
          System.out.println("Error in entered string : " + e);
        }

        // creates a bak file of passed arguments.
        resultFlag = monitorProfile.createMPROFBackup(profileNameForBakUp);
        System.out.println("Result = " + resultFlag);
        break;

      case 9:
        String profileNameToBeModified = null;
        int numberOfCMON = 0;
        String[] strCMON = null;
        try
        {
          System.out.print("Enter Profile Name : ");
          profileNameToBeModified = br.readLine();

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
        resultFlag = monitorProfile.deleteMonitorFromMPROF(profileNameToBeModified, strCMON);
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
        resultFlag = monitorProfile.isMPROFModified(profilename);
        System.out.println("Result = " + resultFlag);
       break;

      case 11:
        //To fetch unique vector name
        TreeSet setAllUniqueVectorName = new TreeSet();
        setAllUniqueVectorName = monitorProfile.getCustomMonitorList();

        Iterator irVectorName = setAllUniqueVectorName.iterator();
        while(irVectorName.hasNext())
        {
          String strVectorName = irVectorName.next().toString();

          System.out.println("Vector Name = " + strVectorName);
        }
        break;

      case 12:
        //To fetch details i.e. profile name, desc, last modified date, and number of custom monitor.
        String[][] arrStdDataData = monitorProfile.getStdMonitorList();
        //for(int i = 0; i < arrStdDataData.length; i++)
          for(int j = 0; j < arrStdDataData.length; j++)
            System.out.println("data value[" + j + "] = " + arrStdDataData[j][0]);

        break;

      case 13:
        //To fetch details i.e. profile name, desc, last modified date, and number of custom monitor.
        int[] arrCountMon = monitorProfile.getMPROFCount("vector");
          for(int j = 0; j < arrCountMon.length; j++)
            System.out.println("data value[" + j + "] = " + arrCountMon[j]);

        break;

      case 14:
        //To fetch details i.e. profile name, desc, last modified date, and number of custom monitor.
        //LinkedHashMap  linkedHashMap = monitorProfile.getServerPoolName("","","","","");
        String arrTemp[][] = monitorProfile.getServerPoolName("","","","","");
        /*Set st = linkedHashMap.keySet();
        System.out.println("Set created from LinkedHashMap Keys contains :");
        //iterate through the Set of keys
        Iterator itr = st.iterator();
        while(itr.hasNext())
        {
          String key = itr.next().toString();

          System.out.println("key = " + key);

          Vector pool = (Vector)linkedHashMap.get(key);
          for(int k = 0; k < pool.size(); k++)
            System.out.println("Pool[" + k + "] = " + pool.get(k).toString());
        }*/

        for(int k = 0; k < arrTemp.length; k++)
        {
          for(int kk = 0; kk < arrTemp[k].length; kk++)
          {
            System.out.println("arrTemp[" + k + "][" + kk + "] = " + arrTemp[k][kk]);
          }
        }
        break;

       default:
         System.out.println("Please select the correct option.");
    }
  }
  
  public String getGDFNameByMonitorName(String monitorName)
  {
    Log.debugLog(className, "getGDFNameByMonitorName", "", "", "Method called");
    String fileWithPath = getStdMonitorProfileNameWithPath("standard_monitors.dat");

    try
    {
      Vector vecStdMonitorData = readFile(fileWithPath);

      String[] arrRcrd = new String[9];


      if(vecStdMonitorData == null)
      {
        Log.debugLog(className, "getGDFNameByMonitorName", "", "", "Standard Monitor Profile File not found, It may be corrupted. File name = " + fileWithPath);
        return null;
      }

 
      for (int i = 0; i < vecStdMonitorData.size(); i++)
      {
        if(!vecStdMonitorData.elementAt(i).toString().startsWith("#"))
        {
          arrRcrd = rptUtilsBean.strToArrayData(vecStdMonitorData.elementAt(i).toString(), "|");
          
          if(arrRcrd.length != 9)
          {
            Log.errorLog(className, "getGDFNameByMonitorName", "", "", "Number of fields are not 9. Count is " + arrRcrd.length);
            continue; // Ignore this line
          }
          
          if(arrRcrd[0].equals(monitorName))
            return arrRcrd[1];
        }
      }
      return null;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getGDFNameByMonitorName", "", "", "Exception - ", e);
      return null;
    }
  }

}

//
// Name    : FileBean.java
// Author  : Neeraj Jain
// Modification History:
//  02/13/05:1.2.1:Neeraj
//    Changed su - netstorm to su netstorm.
//    Added reqId in the wrapper name to make it unique
//    Added new methods for Other Keywords and keywords updates
//  05/15/05:1.2.2:Neeraj
//    Added several new methods to simplify JSP Code.
//  07/16/05:1.4:Neeraj
//    Added refresh of property in updateKeyValues()
//  08/13/05:1.4:Neeraj
//    Fixed bug in getKeywordFields(). It was returning always 0
//    Added escapeQuotes().
//    Default keyword values are not saved in scenario file.
//    Change exit value position in the ERROR line added by logCmdExecError()
//  01/05/06:1.4 Vikas
//    Added new data structure to store command name to be executed under root.
//  01/05/06:1.4 Vikas
//    Added new find() method to find command stored in arrCommandName[].It will return
//    true/false depending whether it find or not.
//  01/14/07:1.4.2-Neeraj
//    Added openScenFile() and migrateKeywords() and deleted createFile().
//  07/07/09:3.5.0-Atul
//    Remove migrateKeywords() methods and add new method to save scenario saveScenario()
//
//  Note:- On okHandler it copies hot to cold
//         On cancelHandler it copies cold to hot
//         On saveScenHandler it copies hot to conf file

package pac1.Bean;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import pac1.Bean.rptUtilsBean;

public class FileBean
{
  static String className = "FileBean";
  
  //nsConfig is copy of config
  //in nsconfig contain non static variable
  static nsConfig Config = new nsConfig();
  
  String Tr069Dir = Config.getWorkPath() + "/data/tr069/";
  CmdExec cmdExec;
  KeywordDefinition keywordDefinition;
  private String scenFileName = "";
  private String TRNUM = "";
  private String TRNUMWithFullPath = "";
  private String projectSubProj = "default/default";
  
  final String RUN_TIME_CHANGABLE_DIR = "runtime_changes";
  final String RUN_TIME_CHANGABLE_FILE = "runtime_changes.conf";
  final String RUN_TIME_CHANGABLE_FILE_ALL = "runtime_changes_all.conf";
  //String RUN_TIME_CHANGABLE_FILE_ALL = "runtime_changes/runtime_changes.conf";
  final String RUN_TIME_CHANGABLE_FILE_HIDDEN = ".runtime_changes.conf";

  private boolean VIEW_MODE_FLAG = false;
  private boolean EDIT_MODE_FLAG = false;
  private boolean RUNTIMECHANGE_MODE_FLAG = false;
  private boolean EDIT_VIEW_MODE_FLAG = false; //scenario open from test design window not Tr view mode
  private boolean DEFAULT_SETTING_MODE_FLAG = false; //scenario open from test design window not Tr view mode

  private static final String VIEW_MODE = "VIEW_MODE";
  private static final String EDIT_MODE = "EDIT_MODE";
  private static final String RUNTIME_MODE = "RUNTIME_MODE";
  private static final String DEFAULT_SETTING_MODE = "DEFAULT_SETTING_MODE";

  private static final String SCALAR = "Scalar";
  private static final String VECTOR = "Vector";

  private static final String ADD_OPERATION = "ADD";
  private static final String DELETE_OPERATION = "DELETE";
  private static final String UPDATE_OPERATION = "UPDATE";

  private String screenKeywords = "";
  private String userName = "netstorm";
  private String scenarioMode = null;
  private String fromServer = "";
  //this variable will hold the Scenario Profile Path
  public  static String scenProfileFileNameWithPathValue = Config.getValue("scenarioProfilePath");
  // this variable will hold the file name to be load in the property either site_keyword.default (default) or user specified scenario setting profile
  public static String siteDefaultKeywordFile  = Config.getValueWithPath("siteDefaultKeywordFile");
  
  public FileBean()
  {
    setScenarioProfilePath();    
    Config.loadConfigFile();
    cmdExec = new CmdExec();
  }

  //0 for Edit mode
  //1 for View mode
  //2 for run time changes mode
  //3 for default setting mode - means update site default file 
  public FileBean(String modeType, String userName)
  {
    setScenarioProfilePath();
    Config.loadConfigFile();
    cmdExec = new CmdExec();

    //setting flag on the basis of MODE
    if(modeType.equals("0"))
      EDIT_MODE_FLAG = true;
    else if(modeType.equals("1"))
      VIEW_MODE_FLAG = true;
    else if(modeType.equals("3"))
      DEFAULT_SETTING_MODE_FLAG = true;    
    else
    {
      RUNTIMECHANGE_MODE_FLAG = true;
    }

    this.userName = userName;
  }

  public FileBean(String dummy)
  {
    setScenarioProfilePath();
    cmdExec = new CmdExec();
  }

  /**
   * Constructor is called by MergeSortScen.java
   * MergeSortScen.java called by netstorm to sort the file
   * Scenario always execute in edit mode
   * @param keywordDefinition
   */
  public FileBean(KeywordDefinition keywordDefinition)
  {
    setScenarioProfilePath();
    this.keywordDefinition = keywordDefinition;
    cmdExec = new CmdExec();
    EDIT_MODE_FLAG = true;
  }

  public void setScenarioProfilePath()
  {
    if(scenProfileFileNameWithPathValue.equals(""))
      scenProfileFileNameWithPathValue = Config.getWorkPath() + "/" + "scenario_profiles";
    else
    {
      String workPath = Config.getWorkPath();
      if(!scenProfileFileNameWithPathValue.startsWith(workPath))
        scenProfileFileNameWithPathValue = workPath + "/" + scenProfileFileNameWithPathValue;
    }
    
  }
  
  //Getting mode on the basis of flag
  public String getModeType()
  {
    if(VIEW_MODE_FLAG)
      return VIEW_MODE;
    else if(RUNTIMECHANGE_MODE_FLAG)
      return RUNTIME_MODE;
    else if(DEFAULT_SETTING_MODE_FLAG)  
      return DEFAULT_SETTING_MODE;

    return EDIT_MODE;
  }

  //For Each screen it will set available keywords in pipe separated manner
  //Enable Apply button in case of run time changing
  public void setScreenKeywords(String screenKeywords)
  {
    this.screenKeywords = screenKeywords;
  }

  //getting keyword
  public String getScreenKeywords()
  {
    return screenKeywords;
  }

  public String getUserName()
  {
    return userName;
  }

  //This method return full path of the TR file
  //file name can be .runtime_changes.conf , runtime_changes_all.conf
  public String getTRDirPath(String filename)
  {
    Log.debugLog(className, "getTRDirPath", "", "", "Method called. scenFileName = " + scenFileName + "  " + TRNUMWithFullPath + " File name = " + filename);

    try
    {
      //storing test run number
      TRNUM = scenFileName.substring(scenFileName.indexOf("/") + 1, scenFileName.lastIndexOf("/"));
      //storing path upto /home/netstorm/work/webapps/logs/TR4200/
      TRNUMWithFullPath = Config.getWorkPath() + "/webapps/logs/TR" + TRNUM + "/";

      //home/netstorm/work/webapps/logs/TR4200/<filename>
      String scenFileNameWithPath = TRNUMWithFullPath + filename;

      Log.debugLog(className, "getTRDirPath", "", "", "Test run file path = " + scenFileNameWithPath);
      return scenFileNameWithPath;
    }
    catch (Exception e) 
    {
      //may be error index
      Log.errorLog(className, "getTRDirPath", "", "", "Exception - " + e);
      return "";
    }

  }

  //This method check file exist or not
  //EX. .runtime_changes.conf , runtime_changes_all.conf exists or not for runtime change.
  public boolean checkFileExistOrnot(String fileNameWithPath, boolean isCreate)
  {
    Log.debugLog(className, "checkFileExistOrnot", "", "", "File path = " + fileNameWithPath + ", Create flag = " + isCreate);

    try
    {
      File fileScen = new File(fileNameWithPath);
      if(fileScen.exists())
      {
        Log.debugLog(className, "checkFileExistOrnot", "", "", "File path = " + fileNameWithPath + ", Create flag = " + isCreate);
        return true;
      }
      else
      {
        Log.debugLog(className, "checkFileExistOrnot", "", "", "File does not exist = " + fileNameWithPath);
        if(isCreate)
        {
          Log.debugLog(className, "checkFileExistOrnot", "", "", "creating file = " + fileNameWithPath);
          fileScen.createNewFile();
          if(rptUtilsBean.changeFilePerm(fileScen.getAbsolutePath(), "netstorm", "netstorm", "664") == false)
            Log.errorLog(className, "checkFileExistOrnot", "", "", "Error in changing permission. File name = " + fileNameWithPath);
          return true;
        }
        return false;
      }
    }
    catch(Exception e)
    {
      Log.errorLog(className, "checkFileExistOrnot", "", "", "Exception - " + e);
      return false;
    }

  }

  /** This Method is used to create scenario profile at NS_WDIR/scenario_profile/project/subproject level
   * 
   * @param profileName
   * @param errMsg
   * @return
   */
  public boolean createScenarioSettingProfile(String profileName, StringBuffer errMsg, String owner)
  {
    try
    {
      String[] prjectSubProjProfile = profileName.split("/");
      String projectName = prjectSubProjProfile[0];
      String subProjectName = prjectSubProjProfile[1];
      
      File scenarioProfileDirPath = new File(scenProfileFileNameWithPathValue + "/");
      if(!scenarioProfileDirPath.exists())
      {
        scenarioProfileDirPath.mkdir();
        if(rptUtilsBean.changeFilePerm(scenarioProfileDirPath.getAbsolutePath(), owner, "netstorm", "777") == false)
          Log.errorLog(className, "createScenarioSettingProfile", "", "", "Error in creating scenario profile directory. File name = " + scenarioProfileDirPath);
      }
      
      String projectFilePath = scenProfileFileNameWithPathValue + "/" + projectName;
      String subProjectFilePath = scenProfileFileNameWithPathValue + "/" + projectName + "/" + subProjectName;
      File projectFile = new File(projectFilePath);
      if(!projectFile.exists())
      {
        projectFile.mkdir();
        if(rptUtilsBean.changeFilePerm(projectFile.getAbsolutePath(), owner, "netstorm", "777") == false)
          Log.errorLog(className, "createScenarioSettingProfile", "", "", "Error in changing project. File name = " + projectFile);  	  
      }
      File subProjectFile = new File(subProjectFilePath);
      if(!subProjectFile.exists())
      {
        subProjectFile.mkdir(); 
        if(rptUtilsBean.changeFilePerm(subProjectFile.getAbsolutePath(), owner, "netstorm", "777") == false)
          Log.errorLog(className, "createScenarioSettingProfile", "", "", "Error in changing subproject. File name = " + subProjectFile);  	  
      }
      
      String scenProfileFileNameWithPath = scenProfileFileNameWithPathValue + "/" + profileName.trim() + ".ssp";
      File fileScen = new File(scenProfileFileNameWithPath);
      if(fileScen.exists())
      {
        appendError(errMsg, "Scenario profile " + scenProfileFileNameWithPath + " already exist.");
        Log.errorLog(className, "createScenarioSettingProfile", "", "", "Scenario profile already exist. Scenario profilename = " + scenProfileFileNameWithPath);
        return false;
      }
      fileScen.createNewFile();
      if(rptUtilsBean.changeFilePerm(fileScen.getAbsolutePath(), owner, "netstorm", "664") == false)
        Log.errorLog(className, "createScenarioSettingProfile", "", "", "Error in changing permission. File name = " + scenProfileFileNameWithPath);
      return true;
    }
    catch(Exception ex)
    {
      Log.errorLog(className, "createScenarioSettingProfile", "", "", "Exception - " + ex);
      return false;      
    }
  }
  
  public boolean openScenFile(String scenFileName, boolean create, StringBuffer errMsg)
  {
    return openScenFile(scenFileName, create, errMsg, false, "");
  }
  
  // Method for creating a new Scenario File or opening an existing Scenario File
  //scenFileName will have the proj\subproj name
  public boolean openScenFile(String scenFileName, boolean create, StringBuffer errMsg, boolean isCalledFromScenarioProfile, String siteKeywordFilePath)
  {
    try
    {
      Log.debugLog(className, "openScenFile", "", "", "scenFileName = " + scenFileName + " , create = " + create + " , isCalledFromScenarioProfile = " + isCalledFromScenarioProfile + " , siteKeywordFilePath = " + siteKeywordFilePath);
      String[] arrFiles = null;
      if(create)
      {
        arrFiles = scenFileName.split("##");
        scenFileName = arrFiles[0].trim();
      }
      String scenFileNameWithPath = Config.getValueWithPath("scenarioPath") + "/" + scenFileName.trim() + ".conf";
      
      if(isCalledFromScenarioProfile)
      {
        DEFAULT_SETTING_MODE_FLAG = true;
        siteDefaultKeywordFile = scenProfileFileNameWithPathValue + "/" + siteKeywordFilePath.trim() + ".ssp";
        Log.debugLog(className, "openScenFile", "", "", "siteDefaultKeywordFile = " + siteDefaultKeywordFile);
      }
      else
      {
        siteDefaultKeywordFile = Config.getValueWithPath("siteDefaultKeywordFile");
        Log.debugLog(className, "openScenFile", "", "", "siteDefaultKeywordFile = " + siteDefaultKeywordFile);
      }
      //Scenario open in view mode and open from test design 
      if(!scenFileName.startsWith("TestRun/"))
        EDIT_VIEW_MODE_FLAG = true;
      
      //String scenFileNameWithPath = Config.getValueWithPath("scenarioPath") + "/" + scenFileName.trim();
      File fileScen = new File(scenFileNameWithPath);
      // If create flag is true, it will check if scenario file exists or not.
      // If exists, give error else create file.
      if(create) // Called for creating a new scenario file
      {
        if(fileScen.exists())
        {
          appendError(errMsg, "Scenario file " + scenFileNameWithPath + " already exist.");
          Log.errorLog(className, "openScenFile", "", "", "Scenario file already exist. Scenario Filename = " + scenFileNameWithPath);
          return false;
        }
        // Create empty scenario file. Do not copy Vendor and Default keywords in the file
        fileScen.createNewFile();
        // means using profile while creating scenario
        if(arrFiles.length == 2)
        {
          String scenarioProfile = arrFiles[1].trim();
          String scenarioProfKeyword = "SCENARIO_SETTINGS_PROFILE " + scenarioProfile + ".ssp";
          final java.io.OutputStream outStrem = new java.io.BufferedOutputStream(new java.io.FileOutputStream(fileScen));
          final java.io.PrintWriter writer = new java.io.PrintWriter(outStrem);
          
          writer.println(scenarioProfKeyword);
          writer.close();
          outStrem.close();
        }
        /**
         This is called here because if hot.<scenarioName>.conf and cold.<scenarioName>.conf
         already exist for this new scenario file name then it these should be empty
         */
        createTempFiles(scenFileNameWithPath, scenFileName);
      }
      // If create flag is false, it will check if scenario name exists or not. If NOT exist, give error.
      else // Called for opening scenario file
      {
        if(!fileScen.exists())
        {
          appendError(errMsg, "Scenario file " + scenFileNameWithPath + " does not exist.");
          Log.errorLog(className, "openScenFile", "", "", "Scenario file does not exist. Scenario Filename = " + scenFileNameWithPath);
          return false;
        }
        createTempFiles(scenFileNameWithPath, scenFileName);
        Properties p;
        if((p = getKeyValues(scenFileName, errMsg)) == null)
          return false;
        //Remove migration code 07/07/2009 :Atul
        //return(migrateKeywords(p, scenFileName));
      }
      return true;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "openScenFile", "", "", "Exception - " + e);
      return false;
    }
  }

  // Method for opening scenario file of a test run
  //  scenFileName is in "TestRun/<testRunNum>/<ScenName>" format
  //  e.g. TestRun/4200/myscen
  public boolean openTestRunScenFile(String scenFileName, StringBuffer errMsg)
  {
    Log.debugLog(className, "openTestRunScenFile", "", "", "Method called. scenFileName = " + scenFileName);

    Properties p = openScenFileFromTestRun(scenFileName, errMsg);

    if(p == null)
      return false;
    return true;
  }

  public Properties openScenFileForObserver(String scenFileName, StringBuffer errMsg)
  {
    try
    {
      Log.debugLog(className, "openScenFileForObserver", "", "", "Method called. scenFileName = " + scenFileName);
      String workPath = Config.getWorkPath();

      String scenFileNameWithPath = workPath + "/scenarios/" + scenFileName.trim() + ".conf";
      File fileScen = new File(scenFileNameWithPath);

      if(!fileScen.exists())
      {
        appendError(errMsg, "Scenario file " + scenFileNameWithPath + " does not exist");
        Log.errorLog(className, "openScenFileForObserver", "", "", "Scenario file does not exits. Scenario Filename = " + scenFileNameWithPath);
        return null;
      }

      createTempFiles(scenFileNameWithPath, scenFileName);
      Properties p;
      if((p = getKeyValues(scenFileName, errMsg)) == null)
        return null;

      return p;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "openScenFileForObserver", "", "", "Exception - " + e);
      return null;
    }
  }
  
  
  /**
   * This wrapper is created for calling from schedule GUI if opened from
   * RTG
   *
   * @param scenFileName
   * @param errMsg
   * @return
   */
  public Properties openScenFileFromTestRun(String scenFileName, StringBuffer errMsg)
  {
    try
    {
      Log.debugLog(className, "openScenFileFromTestRun", "", "", "Method called. scenFileName = " + scenFileName);
      String workPath = Config.getWorkPath();
      String scenFileNameWithTR = scenFileName.substring(scenFileName.indexOf("/") + 1);

      String TR = scenFileName.substring(scenFileName.indexOf("/") + 1, scenFileName.lastIndexOf("/") + 1);

      File TRFile = new File(workPath + "/webapps/logs/TR" + TR + RUN_TIME_CHANGABLE_DIR);
      if(!TRFile.exists())
      {
         TRFile.mkdir();
         //String strCmdName = "chown";
         //String strCmdArgs = " netstorm.netstorm " + workPath + "/webapps/logs/TR" + TR + RUN_TIME_CHANGABLE_DIR;
         //boolean bol = CmdExec.getResultByCommand(Vector vecCmdOut, strCmdName, strCmdArgs, 1, "netstorm", null);
      }

      String scenFileNameWithPath = workPath + "/webapps/logs/TR" + scenFileNameWithTR.trim() + ".conf";
      File fileScen = new File(scenFileNameWithPath);

      if(!fileScen.exists())
      {
        appendError(errMsg, "Scenario file " + scenFileNameWithPath + " does not exist");
        Log.errorLog(className, "openScenFileFromTestRun", "", "", "Scenario file does not exits. Scenario Filename = " + scenFileNameWithPath);
        return null;
      }

      String cmd = "nsi_migrate_scen";
      String args = "-s " + scenFileNameWithPath;
      Vector vecCmdOutput = cmdExec.getResultByCommand(cmd, args, CmdExec.NETSTORM_CMD, null, "root");

      if(vecCmdOutput == null)
      {
        appendError(errMsg, "Can not migrate scenario " + scenFileNameWithPath);
        return null;
      }

      if((vecCmdOutput.size() > 0) && ((String)vecCmdOutput.lastElement()).startsWith("ERROR"))
      {
        for(int i = 0; i < (vecCmdOutput.size() - 1); i++)
          appendError(errMsg, vecCmdOutput.elementAt(i).toString());
        return null;
      }

      createTempFiles(scenFileNameWithPath, scenFileName);
      Properties p;
      if((p = getKeyValues(scenFileName, errMsg)) == null)
        return null;
      //Remove migration code 07/07/2009:Atul
      //return(migrateKeywords(p, scenFileName));
      return p;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "openScenFileFromTestRun", "", "", "Exception - " + e);
      return null;
    }
  }

  private void appendError(StringBuffer errMsg, String msg)
  {
    errMsg.append("  " + msg + "\n");
  }

  // Delete Scenario File (Change Name later)
  // TBD - add code for deleting hot and cold file if present
  //This will have the scenario file name only need to append proj/subProj name after
  public boolean del(String fname)
  {
    try
    {
      String del = Config.getValueWithPath("scenarioPath") + "/" + fname.trim() + ".conf";
      //String del = Config.getValueWithPath("scenarioPath") + "/" + fname.trim();

      del = del.trim();
      File f = new File(del);
      String profilePath = Config.getWorkPath() + "/replay_profiles/" + fname.trim();
      String scriptPath = Config.getValueWithPath("sessionPath") + "/" + fname.trim();
      
      File profile = new File(profilePath);
      File script = new File(scriptPath);
      if(f.exists())
      {
        f.delete();
        
        //delete the replay profile if exist. 
        if(profile.exists())
        {
          TestSuiteBean testSuiteObj = new TestSuiteBean();
          testSuiteObj.removeDirectory(profile);
          testSuiteObj.removeDirectory(script);
        }
        
        return true;
      }
      else
      {
        Log.errorLog(className, "del", "", "", "Scenario File Not Found. Filename = " + del);
        return false;
      }
    }
    catch(Exception e)
    {
      Log.errorLog(className, "del", "", "", "Exception in del()" + e);
    }
    return false;
  }//end of del()

  // Rename Scenario File
  // TBD - add code for deleting hot and cold file if present
  // and check if f1 file already exists
  public boolean rename(String f, String f1)
  {
    try
    {
      String reFrom = Config.getValueWithPath("scenarioPath") + "/" + f.trim() + ".conf";
      String reTo = Config.getValueWithPath("scenarioPath") + "/" + f1.trim() + ".conf";

      //String reFrom = Config.getValueWithPath("scenarioPath") + "/" + f.trim();
      //String reTo = Config.getValueWithPath("scenarioPath") + "/" + f1.trim();

      reFrom = reFrom.trim();
      reTo = reTo.trim();
      File fname = new File(reFrom);
      File fname1 = new File(reTo);
      if(fname.exists())
      {
        fname.renameTo(fname1);
        return true;
      }
      else
      {
        Log.errorLog(className, "rename", "", "", "File Not Found. Filename = " + reFrom);
        return false;
      }
    }
    catch(Exception e)
    {
      Log.errorLog(className, "rename", "", "", "Exception in rename()" + e);
    }
    return false;
  }//end of rename


  // copy fileSrc to fileDest
  public boolean copyToFile(String fileSrcName, String fileDestName)
  {
    Log.debugLog(className, "copyToFile", "", "", "Method Called fileSrcName = " + fileSrcName + ", fileDestName = " + fileDestName);

    try
    {
      File fileSrc = new File(fileSrcName);
      File fileDest = new File(fileDestName);
      if(!fileSrc.exists())
      {
        Log.errorLog(className, "copyToFile", "", "", "Source file does not exits. Filename = " + fileSrcName);
        return false;
      }

      /*
        Remove this check because FileOutputStream will create file if
        it was not there.: Atul 04/02/09

        if(!fileDest.exists())
        {
          Log.errorLog(className, "appendToFile", "", "", "Destination file does not exits. Filename = " + fileDestName);
          return false;
        }
      */

      FileInputStream fin = new FileInputStream(fileSrc);
      BufferedReader br = new BufferedReader(new InputStreamReader(fin));
      FileOutputStream fout = new FileOutputStream(fileDest, false);  //not append mode
      PrintStream pw = new PrintStream(fout);
      String str;
      while((str = br.readLine()) != null)
        pw.println(str);

      pw.close();
      br.close();
      fin.close();
      fout.close();
      return true;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "copyToFile", "", "", "Exception in appendToFile()" + e);
      return false;
    }
  }

  public boolean updateKeyValue(Properties p, String filename)
  {
    StringBuffer errMsg = new StringBuffer();
    return updateKeyValue(p, filename, "hot", errMsg);
  }

  private boolean updateKeyValue(Properties p, String filename, String strWhich, StringBuffer errMsg)
  {
    try
    {
      //In runtime mode will we do nothing
      //TODO: handle to maintain properties hidden file to remove duplicate keyword
      String path = genScenTempFileName(filename, strWhich);
      if(RUNTIMECHANGE_MODE_FLAG)
      {
        p.clear();
        return getKeyValues(p, filename, strWhich, true, errMsg);
        // path = genScenTempFileName(filename, strWhich);
      }

      FileOutputStream fout = new FileOutputStream(path);
      PrintStream pw = new PrintStream(fout); //new
      Enumeration e = p.propertyNames();

      String vendorKeywordFile  = Config.getValueWithPath("vendorKeywordFile");
      Object arrVendorKeywords[] = getFileData(vendorKeywordFile);
      if(Integer.parseInt(Config.getIntValue("debuglevel")) > 1)
      {
        for(int i = 0; i < arrVendorKeywords.length; i++)
          Log.debugLog(className, "updateKeyValue", "", "", "Vendor Keywords = " + arrVendorKeywords[i]);
      }

      Object arrSiteDefaultKeywords[] = getFileData(siteDefaultKeywordFile);
      Vector vecSiteDefaultKeyWord = new Vector();

      if(!DEFAULT_SETTING_MODE_FLAG)
      if(arrSiteDefaultKeywords != null)
      {
        for(int i = 0; i < arrSiteDefaultKeywords.length; i++)
        {
         String arr[] = arrSiteDefaultKeywords[i].toString().split(" ");
         vecSiteDefaultKeyWord.add(arr[0]);
         if(Integer.parseInt(Config.getIntValue("debuglevel")) > 1)
           Log.debugLog(className, "updateKeyValue", "", "", "Default Site Specific Keywords = " + arr[0]);
        }
      }
      else
        Log.errorLog(className, "updateKeyValue", "", "", "siteDefaultKeywordFile = " + siteDefaultKeywordFile + " file does not exist.");

      // ArrayList alLines1 = new ArrayList();
      // String defaultKeywordFile =
      // Config.getValueWithPath("defaultKeywordFile");
      // loadFileInArray(defaultKeywordFile, alLines1);
      if(keywordDefinition == null)
        keywordDefinition = new KeywordDefinition();
      Object arrDefaultKeywords[] = keywordDefinition.getKeywordsWithDefaultValue();
      Arrays.sort(arrDefaultKeywords);
      if(Integer.parseInt(Config.getIntValue("debuglevel")) > 1)
      {
        for(int i = 0; i < arrDefaultKeywords.length; i++)
          Log.debugLog(className, "updateKeyValue", "", "", "Default Keywords = " + arrDefaultKeywords[i]);
      }

      //Iterate all the keyword define in the scenario file
      while(e.hasMoreElements())
      {
        String key_str = (String)e.nextElement();
        String temp_str = p.getProperty(key_str).trim();
        if(temp_str.equals(""))  //handle case of keyword without value
          pw.println(key_str);
        else
        {
          /**
           *The format of keyword value are put in property in below style
           *if it is more than once in scenario file:
           *
           *G_KA_PCT ALL 100
           *G_KA_PCT G1 60
           *G_KA_PCT G2 50
           *
           *Then it will be saved in property (for 'G_KA_PCT' as example )
           *as 'ALL 100|G1 60|G2 50'
           */
          StringTokenizer key_value = new StringTokenizer(temp_str, "|");
          while(key_value.hasMoreTokens())
          {
            String kwVal = key_value.nextToken();
            String line = key_str + " " + kwVal;
            Log.debugLog(className, "updateKeyValue", "", "", "Keywords to be saved = " + line);
            KeywordDefinition.Keywords keywordObj = (KeywordDefinition.Keywords)keywordDefinition.getHashForKeywords().get(key_str);
            boolean isGroupBased;
            boolean isVector;
            if(keywordObj == null)
            {
              //Temporary solution for keyword that is not found in the keywordDefinetion file
              Log.errorLog(className, "updateKeyValue", "", "", "keywordObj is coming null for the key = " + key_str  + ", key with value = " + line);
              isGroupBased = false;
              isVector = false;
            }
            else
            {
              isGroupBased = keywordObj.getIsGroupBased();
              isVector = keywordObj.getType() == KeywordDefinition.Keywords.VECTOR;
            }

            if(vecSiteDefaultKeyWord.contains(key_str))//site default key contain the keyword (may be not with same value)
            {
              if(checkToWriteKeyword(isGroupBased, isVector, key_str, kwVal, temp_str, arrVendorKeywords,  arrSiteDefaultKeywords))
                pw.println(key_str + " " + kwVal);
            }
            else//Not found in SiteDefaultKeyword file
            {
              if(checkToWriteKeyword(isGroupBased, isVector, key_str, kwVal, temp_str, arrVendorKeywords,  arrDefaultKeywords))
                pw.println(key_str + " " + kwVal);
            }
          }
        }
      }

      pw.close();//new
      fout.close();
      // Now refresh p with the latest values in the file
      // This is done so that JSP have the latest info after add/delete etc
      p.clear();
      return getKeyValues(p, filename, strWhich, true, errMsg);
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "updateKeyValue", "", "", "Exception in updateKeyValue()", e);
      return false;
    }
  }

  /**
   This method is to check the key value for given keyword
   should write in the hot file OR not.
   for example:
   if keyword -->
     G_KA_PCT ALL 70 exist in the property.
   then
     G_KA_PCT <group name> 70 should not write in the file
     because value (70) is same
   */
  //keyword = G_HTTP_CACHING, keyValue = G2 0 0 0, allValueOfKey = ALL 0 0 0|G2 0 0 0
//  private boolean shouldWrite(String keyword, String keyValue, String allValueOfKey, Object defaultValueArr)
  private boolean checkToWriteGroupBasedKeyword(String keyword, String keyValue, String allValueOfKey)
  {
    if(Integer.parseInt(Config.getIntValue("debuglevel")) > 1)
      Log.debugLog(className, "checkToWriteGroupBasedKeyword", "", "", "Method called. keyword = " + keyword + ", keyValue = " + keyValue + ", allValueOfKey = " + allValueOfKey);
    try
    {
      StringTokenizer key_valuesTkns = new StringTokenizer(allValueOfKey, "|");
      
      //To get the value with  'ALL' for the keyword
      while(key_valuesTkns.hasMoreTokens())
      {
        String key_valueTkn = key_valuesTkns.nextToken();
        String strSplit[] = key_valueTkn.split(" ");
        //ALL found then need to get the value next to it
        //It is assumed that value with ALL for keyword must be found
        if(key_valueTkn.indexOf("ALL") != -1 && strSplit[0].equals("ALL"))
        {
          String strArrDefWithAll[] = key_valueTkn.split(" ");
          String strArrGivenKeyVal[] = keyValue.split(" ");

          String strDefaultWithAll = "";
          String strGivenKeyValue = "";

          //To create the value for default keyvalue without 'ALL' prefix
          for(int i = 1 ; i < strArrDefWithAll.length ; i++)
          {
            if(strDefaultWithAll.equals(""))
              strDefaultWithAll = strArrDefWithAll[i];
            else
              strDefaultWithAll = strDefaultWithAll + " " + strArrDefWithAll[i];
          }

          //To create the value for given keyValue without <group name> prefix
          for(int i = 1 ; i < strArrGivenKeyVal.length ; i++)
          {
            if(strGivenKeyValue.equals(""))
              strGivenKeyValue = strArrGivenKeyVal[i];
            else
              strGivenKeyValue = strGivenKeyValue + " " + strArrGivenKeyVal[i];
              //strGivenKeyValue = strDefaultWithAll + " " + strArrGivenKeyVal[i];
          }

          if(strDefaultWithAll.equals(strGivenKeyValue))
            return false;

          //specific group value is different with ALL default value for that keyword
          return true;
        }
      }

      Log.errorLog(className, "checkToWriteGroupBasedKeyword", "", "", "Keyword value with ALL not found in the allValueOfKey = " + allValueOfKey + ", keyword = " + keyword);
      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "checkToWriteGroupBasedKeyword", "", "", "Exception in shouldWrite", e);
      return true;
    }
  }

  private boolean checkToWriteKeyword(boolean isGroupBased, boolean isVector, String keywordName, String keywordVal, String allKeywordValue, Object[] vendorKeywordsArr, Object[] siteORdefautArr)
  {
    Log.debugLog(className, "checkToWriteKeyword", "", "", "Method called. isGroupBased = " + isGroupBased + ", isVector = " + isVector + ", keywordName = " + keywordName + ", keywordVal = " + keywordVal + ", allKeywordValue = " + allKeywordValue);
    try
    {
      String keywordNameWithValue = keywordName + " " + keywordVal;

      //check keyword is group based OR not
      if(isGroupBased)
      {
        Log.debugLog(className, "checkToWriteKeyword", "", "", "Group based Keyword = " + keywordName);


        //Match with default keyword value so ignored
        if(Arrays.binarySearch(siteORdefautArr, keywordNameWithValue) >= 0)
          return false;

        //Keyword for all groups
        if(keywordVal.startsWith("ALL"))
          return true;
        else
        {
          if(isVector)//If vector then no need to check just write in the file
          {
            Log.debugLog(className, "checkToWriteKeyword", "", "", "Group based vector Keyword = " + keywordName);
            return true;
          }

          Log.debugLog(className, "checkToWriteKeyword", "", "", "Group based scalar Keyword = " + keywordName);

          if(checkToWriteGroupBasedKeyword(keywordName, keywordVal, allKeywordValue))
            return true;
          else
            return false;
        }
      }
      else//Not group based
      {
        Log.debugLog(className, "checkToWriteKeyword", "", "", "Non-group based Keyword = " + keywordName);

        if((Arrays.binarySearch(vendorKeywordsArr, keywordNameWithValue) < 0) && (Arrays.binarySearch(siteORdefautArr, keywordNameWithValue) < 0))
          return true;
        else
          return false;
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "checkToWriteKeyword", "", "", "Exception in shouldWrite", e);
      return true;
    }
  }

  public boolean loadFileInArray(String nameOfFile, ArrayList alLines)
  {
    try
    {
      File file = new File(nameOfFile.trim());

      if(!file.exists())
      {
        Log.debugLog(className, "loadFileInArray", "", "", "File Does not exits. Filename = " + nameOfFile);
        return false;
      }

      FileInputStream fin = new FileInputStream(file);
      BufferedReader in = new BufferedReader(new InputStreamReader(fin));
      String s_line;

      while((s_line = in.readLine()) != null)
      {
        if((s_line.trim().equals("")) || (s_line.trim().startsWith("#")))
          continue;
        // This is done to make sure words are separated by only one space
        // as we will do binary search for the line later.
        String arrTmp[] = split(s_line, " ");
        s_line = arrTmp[0];
        for(int i = 1; i < arrTmp.length; i++)
          s_line = s_line + " " + arrTmp[i];
        alLines.add(s_line);
      }
      in.close();
      fin.close();
      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "loadFileInArray", "", "", "Exception in loadFileInArray()", e);
    }
    return false;
  }

  private Object[] getFileData(String filePath)
  {
    return getFileData(filePath, true);
  }

  private Object[] getFileData(String filePath, boolean isSort)
  {
    Log.debugLog(className, "getFileData", "", "", "Method Called. filePath = " + filePath);
    Object[] data = null;
    try
    {
      ArrayList list = new ArrayList();
      if(!loadFileInArray(filePath, list))
        return null;

      data = list.toArray();

      if(isSort)
        Arrays.sort(data);

      return data;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getFileData", "", "", "Exception in getFileData()", e);
      return null;
    }
  }

  private Object[] reverseObjectArray(Object[] arrReverse)
  {
    java.util.List<Object> list = Arrays.asList(arrReverse);
    Collections.reverse(list);

    //return (List) list;
    return list.toArray();
  }

  /*
   * This method check keyword existence
   * load in the properties
   */
  private void loadFileInProperties(Properties p, Properties tempP, String fileName, StringBuffer errMsg, boolean isduplicateAllow)
  {
    Object arrKeywords[] = getFileData(fileName, false);

    if(isduplicateAllow)
      arrKeywords = reverseObjectArray(arrKeywords);

    if(arrKeywords == null)
    {
      appendError(errMsg, "File not found = " + fileName);
    }
    else
    {
      tempP.clear();
      for(int i = 0; i < arrKeywords.length; i++)
      {
        String keywordNameWithValue = arrKeywords[i].toString();
        String keywordName = getKeywordName(p, keywordNameWithValue);

        if(isValidKeyword(keywordName))
          appendKeyValue(p, tempP, keywordName, keywordNameWithValue, fileName, isduplicateAllow, errMsg);
        else
          appendError(errMsg, "Keyword " + keywordName  + " defined in file " + fileName + " is not a valid keyword");
      }
    }
  }

  // Read scenario file (hot) and load keywords in the Java Property class.
  public Properties getKeyValues(String fname, StringBuffer errMsg)
  {
    return(getKeyValues(fname, "hot", errMsg));
  }

  // Read scenario file (hot or cold) and load keywords in the Java Property class.
  public Properties getKeyValues(String fname, String strWhich, StringBuffer errMsg)
  {
    try
    {
      Properties p =  new Properties();
      if(!getKeyValues(p, fname, strWhich, true, errMsg))
        return null;
      return p;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getKeyValues", "", "", "Exception in getKeyValues()", e);
    }
    return null;
  } // end of getKeyValues

  // Read scenario file (hot or cold) and load keywords in the Java Property class.
  public boolean getKeyValues(Properties p, String fname, String strWhich, boolean shouldMerge, StringBuffer errMsg)
  {
    try
    {
      int i;
      Properties tempP = new Properties();

      if(keywordDefinition == null)
        keywordDefinition = new KeywordDefinition();

      this.scenFileName = fname;

      String scenFile = null;
      /**
       * This will come null when this method is called from the MergeSortScen.java file(for netstorm)
       * because we do not need to read vendor file when call this method for
       * MergeAndSort by netstorm
       */
      if(strWhich != null)
      {
        Log.debugLog(className, "getKeyValues", "", "", "strWhich is not comming null. Called from GUI");

        // First get keywords from Vendor file in the sorted order.
        // This is done so that we show vendor keywords first the list/table
        // TBD - This may cause issue if we want to show in sorted order for all KW
        String vendorKeywordFile = Config.getValueWithPath("vendorKeywordFile");

        /**if(checkFileExistOrnot(vendorKeywordFile, false))
        {
          Log.debugLog(className, "getKeyValues", "", "", "Loading vendor default file = " + vendorKeywordFile);
          loadFileInProperties(p, tempP, vendorKeywordFile, errMsg, false);
        }**/
        
        Object arrVendorKeywords[] = getFileData(vendorKeywordFile);

        for(i = 0; i < arrVendorKeywords.length; i++)
        {
          String keywordNameWithValue = arrVendorKeywords[i].toString();
          String keywordName = getKeywordName(p, keywordNameWithValue);

          if(isValidKeyword(keywordName))
          {
            int count = getKeywordFieldsMatchCount(p, keywordName, keywordNameWithValue);

            if(count == -1)//Error in checking duplicate keyword
              appendError(errMsg,"Error in checking duplicate Keyword " + keywordName  + " defined in file " + vendorKeywordFile);
            else
            {
              if(count >= 1)
              {
                appendError(errMsg, "Keyword " + keywordName  + " defined in file " + vendorKeywordFile + " is a duplicate keyword");
                Log.errorLog(className, "getKeyValues", "", "", "Keyword " + keywordName  + " defined in file " + vendorKeywordFile + " is a duplicate keyword");
              }
              else
                setKeyValue(p, keywordNameWithValue);
            }
          }
          else
            appendError(errMsg, "Keyword " + keywordName  + " defined in file " + vendorKeywordFile + " is not a valid keyword");
        }
        scenFile = genScenTempFileName(fname, strWhich);
      }
      else//Called for netstorm
      {
        Log.debugLog(className, "getKeyValues", "", "", "strWhich is comming null, may be called from netstorm");
        scenFile = fname;
        // this is done to get the project subproject from scenario file name which will be used for scenario setting file path
        String[] arrProjectSubProject = fname.split("/");
        if(arrProjectSubProject.length >= 2)
          projectSubProj = arrProjectSubProject[1] + "/" + arrProjectSubProject[2];
      }

      if((RUNTIMECHANGE_MODE_FLAG) || (VIEW_MODE_FLAG))
      {
        if(RUNTIMECHANGE_MODE_FLAG)
        {
          //Loading TestRun hidden file .runtime_changes.conf
          String runTimeFilePathHidden = getTRDirPath(RUN_TIME_CHANGABLE_DIR + "/" + RUN_TIME_CHANGABLE_FILE_HIDDEN);
          if(checkFileExistOrnot(runTimeFilePathHidden, false))
          {
            Log.debugLog(className, "getKeyValues", "", "", "Loading hidden file = " + runTimeFilePathHidden);
            loadFileInProperties(p, tempP, runTimeFilePathHidden, errMsg, false);
          }
        }

        //Loading Testrun runtime_changes_all.conf
        String runTimeFilePath = getTRDirPath(RUN_TIME_CHANGABLE_DIR + "/" + RUN_TIME_CHANGABLE_FILE_ALL);
        if(checkFileExistOrnot(runTimeFilePath, false))
        {
          Log.debugLog(className, "getKeyValues", "", "", "Loading runtime all file = " + runTimeFilePath);
          //last arg means - file will read in reverse order and allow duplicate keyword 
          loadFileInProperties(p, tempP, runTimeFilePath, errMsg, true);
        }
        
        //Loading Testrun sort file 
        //Sort file name is combination of sorted + login name + scenario file name + .conf
        // Ex: sorted_netstorm_delete_users_simple_scenario_RU.conf
        //runtime and view of test run we will not read scenariofile
        if(!EDIT_VIEW_MODE_FLAG && !VIEW_MODE_FLAG) 
        {
          //System.out.println(" fname " + fname + "  " +  TRNUMWithFullPath + " " + fname.substring(fname.lastIndexOf("/") + 1) + ".conf");
          int testRunScen = -1;
          if(!TRNUM.equals(""))
            testRunScen = Integer.parseInt(TRNUM);
          
          String sortedFileName = Scenario.getSortedTRScenFileWithAbsolutePath(testRunScen);
          Log.debugLog(className, "getKeyValues", "", "", "sortedFileName = " + sortedFileName);
  
          if(checkFileExistOrnot(sortedFileName, false))
          {
            Log.debugLog(className, "getKeyValues", "", "", "Loading sorted file = " + sortedFileName);
            loadFileInProperties(p, tempP, sortedFileName, errMsg, false);
          }
          else
            Log.debugLog(className, "getKeyValues", "", "", "sorted file is not present = " + fname);
        }
      }

      if(VIEW_MODE_FLAG)
      {
        if(checkFileExistOrnot(scenFile, false))
        {
          Log.debugLog(className, "getKeyValues", "", "", "Loading scenario file = " + fname);
          loadFileInProperties(p, tempP, scenFile, errMsg, false);
        }        
      }
      
      //read scenario in edit and non test run view mode
      if((EDIT_MODE_FLAG) || (EDIT_VIEW_MODE_FLAG) || (DEFAULT_SETTING_MODE_FLAG))
      {
        if(checkFileExistOrnot(scenFile, false))
        {
          Log.debugLog(className, "getKeyValues", "", "", "Loading scenario file = " + fname);
          loadFileInProperties(p, tempP, scenFile, errMsg, false);
        }
      }
        
      // Now get keywords from Scenario file in the sorted order.
     /** Log.debugLog(className, "getKeyValues", "", "", "Loading scenario file = " + fname);

      Object arrScenKeywords[] = getFileData(scenFile, false);

      if(arrScenKeywords == null)
      {
        appendError(errMsg, "Scenario file not found = " + fname);
      }
      else
      {
        tempP.clear();
        for(i = 0; i < arrScenKeywords.length; i++)
        {
          String keywordNameWithValue = arrScenKeywords[i].toString();
          String keywordName = getKeywordName(p, keywordNameWithValue);

          if(isValidKeyword(keywordName))
            appendKeyValue(p, tempP, keywordName, keywordNameWithValue, fname, errMsg);
          else
            appendError(errMsg, "Keyword " + keywordName  + " defined in file " + fname + " is not a valid keyword");
        }
      }**/

      if(shouldMerge)
      {
        if((EDIT_MODE_FLAG) || (EDIT_VIEW_MODE_FLAG) )
        {
          if(checkFileExistOrnot(siteDefaultKeywordFile, false))
          {
            Log.debugLog(className, "getKeyValues", "", "", "Loading site default file = " + siteDefaultKeywordFile);
            loadFileInProperties(p, tempP, siteDefaultKeywordFile, errMsg, false);
          }
          else
            Log.errorLog(className, "getKeyValues", "", "", "siteDefaultKeywordFile = " + siteDefaultKeywordFile + " file does not exist.");
        }
        
        /**Object arrSiteDefaultKeywords[] = getFileData(siteDefaultKeywordFile);;
        if(arrSiteDefaultKeywords != null)
        {
          tempP.clear();
          for(i = 0; i < arrSiteDefaultKeywords.length; i++)
          {
            String defaultSiteKeywordName = getKeywordName(p, arrSiteDefaultKeywords[i].toString());
            if(isValidKeyword(defaultSiteKeywordName))
              appendKeyValue(p, tempP, defaultSiteKeywordName, arrSiteDefaultKeywords[i].toString(), siteDefaultKeywordFile, errMsg);
            else
              appendError(errMsg, "Keyword " + defaultSiteKeywordName  + " defined in file " + siteDefaultKeywordFile + " is not a valid keyword");
          }//End of loop for site default array
        }//End of if condition for loadFileInArray
        **/

        Object arrDefaultKeywords[] = keywordDefinition.getKeywordsWithDefaultValue();
        String keywordDefinitionFile = Config.getValueWithPath("keywordDefinitionFile");

        Arrays.sort(arrDefaultKeywords);
        //Clear the temporary property object
        tempP.clear();
        for(i = 0; i < arrDefaultKeywords.length; i++)
        {
          String defaultKeywordName = getKeywordName(p, arrDefaultKeywords[i].toString());
          appendKeyValue(p, tempP, defaultKeywordName, arrDefaultKeywords[i].toString(), keywordDefinitionFile, errMsg);
        }//End of loop for keyword definition array
      }//End of should merge condition

      if(Integer.parseInt(Config.getIntValue("debuglevel")) > 1)
        logAllKeywords(p);
     //commenting  this 
      //because "Error in parsing scenario file /home/netstorm/work2/logs/TR1570/runtime_changes/runtime_changes.conf due to following errors:
      //05:00:31: Error in opening file /home/netstorm/work2/logs/TR1570/runtime_changes/sorted_runtime_changes.conf. Errno=No such file or directory
      // Runtime Updation Failed" problem was comming
      //To add Ramp down phase if Ramp down phase is not added after duration phase 
      //only in simple Scenario and simple group.
      //makeScenarioBaseSchedule(p);
      
      //errMsg contain invalid keyword and duplicate keyword
      // if exist it return false property become null - so that gui gives alert msg and not open
      //Here we are comment these two line so user can open scenario from the gui. At the time opening invalid and duplicate keyword is not added in the property
      //when user save from gui it delete the invalid and duplicate keyword
      //if(errMsg.length() > 0)
        //return false;

      migrateSGRPKywordWithGenerator(p);
      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getKeyValues", "", "", "Exception in getKeyValues()" , e);
      return false;
    }
  }// end of getKeyValues

  public void migrateSGRPKywordWithGenerator(Properties p)
  {
    String[][] arrSGRP = getKeywordRecordFields(p, "SGRP", "", "");
    
    String[] arrTemp = null;
    if(arrSGRP != null && arrSGRP.length > 0)
    {
      arrTemp = new String[arrSGRP.length];
      
      for(int ii = 0; ii < arrSGRP.length; ii++)
      {
        String strTemp = "";
        for(int jj = 0; jj < arrSGRP[ii].length; jj++)
        {
          if(!(arrSGRP[ii][2].equals("NA") || arrSGRP[ii][2].equals("FIX_CONCURRENT_USERS") || arrSGRP[ii][2].equals("FIX_SESSION_RATE")))
          {
            if(jj == 1)
            {
              strTemp = strTemp + " NA " + arrSGRP[ii][jj];
            }
            else
              strTemp = strTemp + " " + arrSGRP[ii][jj];
          }
          else
            strTemp = strTemp + " " + arrSGRP[ii][jj];
        }
        arrTemp[ii] = strTemp.trim();
      }
      String strResult = "";
      for(int k = 0; k < arrTemp.length; k++)
      {
        if(k == 0)
          strResult = arrTemp[k];
        else
          strResult = strResult + "|" + arrTemp[k];
      }
      
      clearKeywordValue(p, "SGRP");
      p.setProperty("SGRP", strResult);
    }    
  }
  
  /*-------------------------------------------------------------------------------------------------
  Name: mergeRTCChangesInScheduleKeyword
  Purpose: To get the updated schedule keyword in the correct order after changes done in schedule keyword in runtime.
           This need to be used only when we are showing schedule of a test run scenario ONLY (View of test run or run time change of test run)
  Input:
    P - Properties containing all keywords including runtime changed keywords or changes not yet applied
  Ouput:
    String containing keyword value of SCHEDULE keyword. For example:
    ALL P1 START IMMEDIATELY|ALL P2 RAMP_UP ALL RATE 120 M LINEARLY|ALL P3 STABILIZATION TIME 0|ALL P4 DURATION SESSIONS 1|ALL P5 RAMP_DOWN ALL IMMEDIATELY

  Algorithm:
    Algorithm is based on following facts:
    1. All phases have unique phase names
    2. Runtime changed schedule phases are coming first in the order of change done (latest changes first) followed by
       phases as per scenario file in the order of execution order


    Let us take an example:
      Scenario has three phases: P1, P2 and P3
      At run time, we changed phases in following order:
        P3, P1, P2, P2

      So Schedule Keyword will have values
        P2|P2|P1|P3|P1|P2|P3

    This function need to return P1|P2|P3 with the latest updated value of all phases which were in original scenario.

    Steps are:
    1. Back traversing the array of all phases
    2. Find the phase with same phase name and if found replace the current value of phase and make found phase value empty

    In above example, as we process

      P2|P2|P1|P3|P1|P2|P3 (Original value)
      P2|P2|P1|X|P1|P2|P3  (Replaced P3 at index 6 with P3 at index 3 and made value at index 3 empty (marked by X))
      P2|X|P1|X|P1|P2|P3   (Replaced P2 at index 5 with P2 at index 1 and made value at index 1 empty (marked by X))
      X|X|P1|X|P1|P2|P3    (Replaced P2 at index 5 with P2 at index 0 and made value at index 0 empty (marked by X))
      X|X|X|X|P1|P2|P3     (Replaced P1 at index 4 with P1 at index 2 and made value at index 2 empty (marked by X))

      So after discarding empty values, we will have updated schedules for the phases
      P1|P2|P3

  -------------------------------------------------------------------------------------------------*/
  
  public static String mergeRTCChangesInScheduleKeyword(Properties p)
  {
    Log.debugLog(className, "mergeRTCChangesInScheduleKeyword", "", "", "Method starts");

    String scheduleValue = p.getProperty("SCHEDULE");
    if(scheduleValue == null)  // This should not happen but check to avoid exceptions
      return null;

    String phaseData[] = rptUtilsBean.strToArrayData(scheduleValue, "|");

    int numPhases = phaseData.length;

    /*
    // If there is only one phase, then there is no need to merge
    if(numPhases == 1)
    {
      Log.debugLog(className, "mergeRTCChangesInScheduleKeyword", "", "", "There is only one phase with value " + scheduleValue);
      return scheduleValue;
    }
    */

    for(int idx1 = (numPhases - 1); idx1 >= 0; idx1--)
    {
      if(phaseData[idx1].equals(""))
      {
        Log.debugLog(className, "mergeRTCChangesInScheduleKeyword", "", "", "Processing phase at index " + idx1 + " with empty value. Skipping this phase");
        continue;
      }

      Log.debugLog(className, "mergeRTCChangesInScheduleKeyword", "", "", "Processing phase at index " + idx1 + " with value " + phaseData[idx1]);

      String phaseValues[] = rptUtilsBean.strToArrayData(phaseData[idx1], " "); 
      String phaseNameAtIdx1 = phaseValues[1];

      for(int idx2 = (idx1 - 1); idx2 >= 0; idx2--)
      {
        if(phaseData[idx2].equals(""))
        {
          Log.debugLog(className, "mergeRTCChangesInScheduleKeyword", "", "", "Checking phase at index " + idx2 + " with empty value. Skipping this phase");
          continue;
        }

        Log.debugLog(className, "mergeRTCChangesInScheduleKeyword", "", "", "Checking phase at index " + idx2 + " with value " + phaseData[idx2]);

        String phaseValues2[] = rptUtilsBean.strToArrayData(phaseData[idx2], " ");
        String phaseNameAtIdx2 = phaseValues2[1];

        if(phaseNameAtIdx1.equals(phaseNameAtIdx2))
        {
          Log.debugLog(className, "mergeRTCChangesInScheduleKeyword", "", "", "Replacing phase data at index " + idx1 + " with phase data at index " + idx2 + " with value " + phaseData[idx2]);
          phaseData[idx1] = phaseData[idx2];
          phaseData[idx2] = ""; // Must make empty as this is used
        }
      }
    }

    // Now get the updated phase list
    int idx;

    // First find the index of the first non empty phase
    for(idx = 0; idx < numPhases; idx++)
    {
      if(!phaseData[idx].equals(""))
        break;
    }

    Log.debugLog(className, "mergeRTCChangesInScheduleKeyword", "", "", "First valid phase at " + idx + " with value "+ phaseData[idx]);
    String updatedList = "";

    for(; idx < numPhases; idx++)
    {

      if(updatedList.equals(""))
      {
        updatedList = phaseData[idx];
      }
      else
        updatedList += "|" + phaseData[idx];

      Log.debugLog(className, "mergeRTCChangesInScheduleKeyword", "", "", "value =  " + phaseData[idx] + " added to list");
    }

    return updatedList;
  }
  
  /************************************************************************************************************
   * 
   * Purpose:- This function is used to add default phases if user has created scenario file from CLI with no phases
   *           or some phases and run it with -S gui option and make changes from GUI in RunTime. Used only for simple Scenario and simple group.
   * Input:- 
   *          p- Properties containg all keywords.
   * Output:- 
   *         Set property for schedule keyword containg All phases For example:
   *         ALL START IMMEDIATELY|ALL RAMP_UP ALL RATE 120 M LINEARLY|ALL STABILIZATION TIME 0|ALL DURATION SESSIONS 1|ALL RAMP_DOWN ALL IMMEDIATELY
   * Algorithm:-
   *    if scenario is of type  Simple Scenario or Simple Group then we will check for each phase from segeuence.
   *    For example:-
   *      Start -> RampUp -> Duration -> Stabilize -> RampDown.
   *    if it is present then we will add that phase
   *    if it is not present then we will add default value for that phase 
   *    
   * Let's take an example-
   *    A scenario has two groups g1 and g2 and schedule keyword give the value-
   *    g1 g1Start0 START IMMEDIATELY|g1 g1RampUp0 RAMP_UP ALL RATE 120 M LINEARLY|g1 g1Stabilize0 STABILIZATION TIME 00:07:00|g1 g1Duration0 DURATION TIME 00:10:00|
   *    g2 g2Start0 START IMMEDIATELY|g2 g2RampUp0 RAMP_UP ALL RATE 120 M LINEARLY|g2 g2Stabilize0 STABILIZATION TIME 00:18:00|g2 g2Duration0 DURATION INDEFINITE
   *  
   * This function need to set property for schedule keyword-
   *    g1 g1Start0 START IMMEDIATELY|g1 g1RampUp0 RAMP_UP ALL RATE 120 M LINEARLY|g1 g1Stabilize0 STABILIZATION TIME 00:07:00|g1 g1Duration0 DURATION TIME 00:10:00|g1 g1RampDown0 RAMP_DOWN ALL IMMEDIATELY|
   *    g2 g2Start0 START IMMEDIATELY|g2 g2RampUp0 RAMP_UP ALL RATE 120 M LINEARLY|g2 g2Stabilize0 STABILIZATION TIME 00:18:00|g2 g2Duration0 DURATION INDEFINITE|g2 g2RampDown0 RAMP_DOWN ALL IMMEDIATELY
   * 
   * Steps:-
   *   Traverse the array of phaseList from first index and check for each phase for each group.
   *   if any phase is not present then add that phase with default value for that group.
   * 
   ************************************************************************************************************/
  
  public void makeScenarioBaseSchedule(Properties p)
  {
    Log.debugLog(className, "makeScenarioBaseSchedule", "", "", "Method starts");
    
    String scheduleType = p.getProperty("SCHEDULE_TYPE");
    String scheduleBy = p.getProperty("SCHEDULE_BY");
    Log.debugLog(className, "makeScenarioBaseSchedule", "", "", "scheduleType = "+scheduleType+" scheduleBy = "+scheduleBy);
    
    if(scheduleType.equals("SIMPLE") && scheduleBy.equals("SCENARIO"))
    {
      String phaseList = p.getProperty("SCHEDULE");
      Log.debugLog(className, "makeScenarioBaseSchedule", "", "", "PhaseList for keyword schedule = "+phaseList);
      if(phaseList == null)
        return;
      
      String arrPhaseList[] = rptUtilsBean.strToArrayData(phaseList, "|");
      String listOfOrderedPhases = "";
      
      for(int i = 0; i < 5; i++)
      {
       String phaseName = "";
       String defaultValueForPhase = "";
       
       switch(i)
       {
         case 0:  phaseName = "START";
                  defaultValueForPhase = "ALL Start0 START IMMEDIATELY";
                  break;
                  
         case 1: phaseName = "RAMP_UP";
                 defaultValueForPhase = "ALL RampUp0 RAMP_UP ALL RATE 120 M LINEARLY";
                 break;
                 
         case 2: phaseName = "STABILIZATION";
                  defaultValueForPhase = "ALL Stabilize0 STABILIZATION TIME 0";
                  break;
                  
         case 3: phaseName = "DURATION";
                  defaultValueForPhase = "ALL Duration0 DURATION INDEFINITE";
                  break;
                  
         case 4: phaseName = "RAMP_DOWN";
                  defaultValueForPhase = "ALL RampDown0 RAMP_DOWN ALL IMMEDIATELY";
                  break;         
       }
       Log.debugLog(className, "makeScenarioBaseSchedule", "", "", "Checking for "+phaseName);
       
       for(int j = 0; j < arrPhaseList.length; j++)
       {
        if(arrPhaseList[j].indexOf(phaseName) == -1)
         continue;
        else
        {
         Log.debugLog(className, "makeScenarioBaseSchedule", "", "", ""+phaseName+" found");
         defaultValueForPhase = arrPhaseList[j];
         break;
        }
       }
       
       Log.debugLog(className, "makeScenarioBaseSchedule", "", "", "adding " + defaultValueForPhase + " for phase "+phaseName);
       if(listOfOrderedPhases.equals(""))
        listOfOrderedPhases = defaultValueForPhase;
       else
        listOfOrderedPhases += "|" + defaultValueForPhase;
      }
      
      p.setProperty("SCHEDULE", listOfOrderedPhases);
      
    }
    else if(scheduleType.equals("SIMPLE") && scheduleBy.equals("GROUP"))
    {
      String phaseList = p.getProperty("SCHEDULE");
      Log.debugLog(className, "makeScenarioBaseSchedule", "", "", "PhaseList for keyword schedule = "+phaseList);
      
      if(phaseList == null)
        return;
      
      String arrScenGroupNames[] = getKeywordRecordOneField(p, "SGRP", "", "", "0");
      String arrPhaseList[] = rptUtilsBean.strToArrayData(phaseList, "|");
      
      String listOfOrderedPhases = "";
      int indexOfGroup = 0;
      String currGroup = "";
      for(int i = 0; i < (5*arrScenGroupNames.length); i++)
      {
       currGroup = arrScenGroupNames[indexOfGroup];
       String phaseName = "";
       String defaultValueForPhase = "";
       
        //g1 g1RampDown0 RAMP_DOWN ALL IMMEDIATELY
       switch(i%5)
       {
         case 0:  phaseName = "START";
                  defaultValueForPhase = currGroup + " " + currGroup + "Start0 START IMMEDIATELY";
                  break;
                  
         case 1: phaseName = "RAMP_UP";
                 defaultValueForPhase = currGroup + " " + currGroup + "RampUp0 RAMP_UP ALL RATE 120 M LINEARLY";
                 break;
                 
         case 2: phaseName = "STABILIZATION";
                  defaultValueForPhase = currGroup + " " + currGroup + "Stabilize0 STABILIZATION TIME 0";
                  break;
                  
         case 3: phaseName = "DURATION";
                  defaultValueForPhase = currGroup + " " + currGroup + "Duration0 DURATION INDEFINITE";
                  break;
                  
         case 4: phaseName = "RAMP_DOWN";
                  defaultValueForPhase = currGroup + " " + currGroup + "RampDown0 RAMP_DOWN ALL IMMEDIATELY";
                  indexOfGroup++;
                  break;         
       }
       
       for(int j = 0; j < arrPhaseList.length; j++)
       {
        if(arrPhaseList[j].startsWith(currGroup+" ") && (arrPhaseList[j].indexOf(phaseName) != -1))
        {
         Log.debugLog(className, "makeScenarioBaseSchedule", "", "", ""+phaseName+" found for group "+currGroup);
         defaultValueForPhase = arrPhaseList[j];
         break;
        }
       }
       
       Log.debugLog(className, "makeScenarioBaseSchedule", "", "", "adding " + defaultValueForPhase + " for phase "+phaseName+" and group "+currGroup);
       if(listOfOrderedPhases.equals(""))
        listOfOrderedPhases = defaultValueForPhase;
       else
        listOfOrderedPhases += "|" + defaultValueForPhase;
      }
      
      p.setProperty("SCHEDULE", listOfOrderedPhases);
    }
  }
  
  //this function for check keyword is run time changable or not
  public boolean isRunTimeChangableKeyword(Properties p, String keywordName)
  {
    try
    {
      if(keywordDefinition == null)
        keywordDefinition = new KeywordDefinition();

      //checking keyword is valid or not
      //if valid is run time changable or not
      KeywordDefinition.Keywords keywordObj = (KeywordDefinition.Keywords)keywordDefinition.getHashForKeywords().get(keywordName);
      if(keywordObj == null)
        return false;
      else
      {
        if(DEFAULT_SETTING_MODE_FLAG) 
          return true;
        else if(keywordObj.getIsRunTimeChangeable())
          return true;
    	  else
    	    return false;
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "isValidKeyword", "", "", "Exception in isRunTimeChangableKeyword()" , e);
      return false;
    }
  }

  private boolean isValidKeyword(String keywordName)
  {
    try
    {
      if(keywordDefinition == null)
        keywordDefinition = new KeywordDefinition();
      KeywordDefinition.Keywords keywordObj = (KeywordDefinition.Keywords)keywordDefinition.getHashForKeywords().get(keywordName);
      if(keywordObj == null)
        return false;

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "isValidKeyword", "", "", "Exception in isValidKeyword()" , e);
      return false;
    }
  }

  //This function return true is case of edit or runtime changeable
  //return false in case of view and non run time changeable
  public boolean enableKeyword(Properties p, String strKeyword)
  {
    return enableKeyword(p, strKeyword, "");
  }

  //Group name is ALL, keyword is not enable
  public boolean enableKeyword(Properties p, String strKeyword, String groupName)
  {
    if(EDIT_MODE_FLAG)
      return true;
    else if(VIEW_MODE_FLAG)
      return false;
    else if(RUNTIMECHANGE_MODE_FLAG)
    {
      if(groupName.equals("ALL"))
        return false;
      else
        return isRunTimeChangableKeyword(p, strKeyword);
    }
    else if(DEFAULT_SETTING_MODE_FLAG) //if mode in default setting mode
    {
      return isRunTimeChangableKeyword(p, strKeyword);
    }
    return false;
  }

  //This function used for button enable disable
  public boolean enableButtonBaseOnMode(Properties p)
  {
    return enableButtonBaseOnMode(p , "");
  }
  
//Group name is ALL, Apply is not enable
  public boolean enableButtonBaseOnMode(Properties p, String groupName)
  {
    
    if(RUNTIMECHANGE_MODE_FLAG)
    {
      if(groupName.equals("ALL"))
        return false;
      String[] arrTemp = rptUtilsBean.split(getScreenKeywords(), "|");
      for(int i = 0; i < arrTemp.length; i++)
        if(isRunTimeChangableKeyword(p, arrTemp[i].trim()))
          return true;
    }
    else if(DEFAULT_SETTING_MODE_FLAG)
    {
      String[] arrTemp = rptUtilsBean.split(getScreenKeywords(), "|");
      for(int i = 0; i < arrTemp.length; i++)
        if(isRunTimeChangableKeyword(p, arrTemp[i].trim()))
          return true;
    }

    return false;
  }

  /*
   * Wrapper for remove pass flag to skip duplicate keyword or not
   * by default false
   */
  private void appendKeyValue(Properties p, Properties tempP, String keywordName, String keywordNameWithValue, String fname, StringBuffer errMsg)
  {
    appendKeyValue(p, tempP, keywordName, keywordNameWithValue, fname, false, errMsg);
  }

  /**
    This method is to append key value(based on condition) to the existing key in the property 'p'
    It will append to the property group based key word with different value with ALL.

    And vector keyword with different key value based on the key fields.
   */

  private void appendKeyValue(Properties p, Properties tempP, String keywordName, String keywordNameWithValue, String fname, boolean isduplicateAllow, StringBuffer errMsg)
  {
    Log.debugLog(className, "appendKeyValue", "", "", "Method called. keywordName = " + keywordName + ", keywordNameWithValue = " + keywordNameWithValue + ", fname = " + fname);
    KeywordDefinition.Keywords keywordObj = (KeywordDefinition.Keywords)keywordDefinition.getHashForKeywords().get(keywordName);

    if(keywordObj == null)
      Log.errorLog(className, "appendKeyValue", "", "", "keywordObj is coming null, no keyword found in the KeywordDefinition file");
    else
    {
      int count = getKeywordFieldsMatchCount(tempP, keywordName, keywordNameWithValue);

      if(count == -1)//Error in checking duplicate keyword
      {
        Log.errorLog(className, "getKeyValues", "", "", "Keyword " + keywordName  + " defined in file " + fname + " is a duplicate keyword");
        //appendError(errMsg, "Error in checking duplicate Keyword " + keywordName  + " defined in file " + fname);
        appendError(errMsg, "Error in : " + keywordName + " keyword has no argument in " + fname + " file");
      }
      else
      {
        if(count >= 1)
        {
          appendError(errMsg, "Keyword " + keywordName  + " defined in file " + fname + " is a duplicate keyword");
          Log.errorLog(className, "getKeyValues", "", "", "Keyword " + keywordName  + " defined in file " + fname + " is a duplicate keyword");
        }
        else
        {
          if(keywordName.equals("SCENARIO_SETTINGS_PROFILE"))
            setScenarioSettingsProfile(keywordNameWithValue);
          if(p.getProperty(keywordName) == null)
            setKeyValue(p, keywordNameWithValue);
          else
          {
            if(getKeywordFieldsMatchCount(p, keywordName, keywordNameWithValue) == 0)
              setKeyValue(p, keywordNameWithValue);
            else
              Log.debugLog(className, "appendKeyValue", "", "", "keyword = " + keywordName + " with value already found in the scenario file");
          }

          //In case of all file of runtimechangable
          //its contain duplicate value
          //so skipping duplicate keyword
          if(!isduplicateAllow)
            setKeyValue(tempP, keywordNameWithValue);
        }
      }
    }//End of else condition for keywords obj NOT null
  }

  /** This method is used to return a comman separated string of profiles for a given project subproject
   * 
   * @param project
   * @param subProject
   * @return
   */
  public String getProfilesOfProjSubProj(String project, String subProject)
  {
    String listOfProfiles = "";
    try
    {
      String profileDir = scenProfileFileNameWithPathValue + "/" + project + "/" + subProject + "/";
      File file = new File(profileDir);
      if(file.exists())
      {
        
        String[] arrFiles = file.list();
        for(int ii = 0; ii < arrFiles.length; ii++)
        {
          if(arrFiles[ii].trim().endsWith(".ssp"))
            listOfProfiles = listOfProfiles + arrFiles[ii].substring(0, arrFiles[ii].lastIndexOf(".")) + ",";   
        }
      }
      if(!listOfProfiles.equals(""))
      {
        listOfProfiles = listOfProfiles.substring(0, listOfProfiles.lastIndexOf(","));
        
      } 
      return listOfProfiles;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getProfilesOfProjSubProj", "", "", "Exception caught - ", ex);
      return listOfProfiles;
    }
  }
  
  /** This method is used to update scenario file for SCENARIO_SETTING_PROFILE keyword
   * 
   */
  public boolean updateScenarioFileForSSP(String scenarioFilePath, String sspProfile)
  {
    boolean  isUpdate = false;
    String keywordToAdd = "SCENARIO_SETTINGS_PROFILE " + sspProfile + ".ssp"; 
    boolean isKeywordExist = false;
    int indexOfKeyword = -1;
    
    BufferedReader br;
    Vector vecFileData = new Vector();
    try
    {
      String scenarioFile = Config.getValueWithPath("scenarioPath") + "/" + scenarioFilePath.trim() + ".conf";
      File file = new File(scenarioFile);
      if(file.exists())
      {
        br = new BufferedReader(new java.io.FileReader(file));
        String str = null;
        while((str = br.readLine()) != null)
          vecFileData.add(str); 
    	
        br.close();
        if(vecFileData.size() != 0)
        {
          for(int ii = 0; ii < vecFileData.size(); ii++)
          {
            if(vecFileData.get(ii).toString().startsWith("SCENARIO_SETTINGS_PROFILE"))
            {
              isKeywordExist = true;
              indexOfKeyword = ii;
              break;
            }
          }
        }
        if(isKeywordExist)
        {
          vecFileData.remove(indexOfKeyword);
          vecFileData.add(indexOfKeyword, keywordToAdd);
        }
        else
          vecFileData.add(keywordToAdd);
    	  
        final java.io.OutputStream outStrem = new java.io.BufferedOutputStream(new java.io.FileOutputStream(file));
        final java.io.PrintWriter writer = new java.io.PrintWriter(outStrem);
        for(int jj = 0; jj < vecFileData.size(); jj++)
          writer.println(vecFileData.get(jj));
        
        writer.close();
        outStrem.close();
        isUpdate = true;
        
        return isUpdate; 
      }
      else
      {
        Log.errorLog(className, "updateScenarioFileForSSP", "", "", "Scenario file not present.");
        return isUpdate;
      }
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "updateScenarioFileForSSP", "", "", "Exception caught - ", ex);
      return isUpdate;
    }
  }
  
  /** This Method is called when SCENARIO_SETTINGS_PROFILE keyword is present in the scenario file.
   * in that case inspite of loading site_keyword.default profile, we load the default profile as specified in the scenario by the keyword SCENARIO_SETTINGS_PROFILE
   * 
   * @param keywordNameWithValue
   */
  public void setScenarioSettingsProfile(String keywordNameWithValue)
  {
    Log.debugLog(className, "setScenarioSettingsProfile", "", "", "keywordNameWithValue = " + keywordNameWithValue);
    String scenarioSettingFile = "";
    String[] arrKeyword = keywordNameWithValue.split(" ");
    if(arrKeyword.length > 1)
      scenarioSettingFile = arrKeyword[1];
    if(!scenarioSettingFile.contains("/"))
      scenarioSettingFile = getProjectSubProject() + "/" + scenarioSettingFile;
    
    if(!scenarioSettingFile.equals(""))
    {
      scenarioSettingFile = Config.getWorkPath() + "/scenario_profiles/" + scenarioSettingFile;
      Log.debugLog(className, "setScenarioSettingsProfile", "", "", "changing sitekeyword file path to = " + scenarioSettingFile);
      siteDefaultKeywordFile = scenarioSettingFile;
    }
  }
  
  public void logAllKeywords(Properties p)
  {
    Enumeration keywords = p.keys();
    while(keywords.hasMoreElements())
    {
      String name = keywords.nextElement().toString();
      Log.debugLog(className, "logAllKeywords", "", "", name + " = " + p.get(name));
    }
  }

  // Generate scenario temp file name (hot and cold)
  public String genScenTempFileName(String scenFileName, String strWhich)
  {
    Log.debugLog(className, "genScenTempFileName", "", "", "scenFileName = " + scenFileName + ", strWhich = " + strWhich);
    scenFileName = replace(scenFileName, "/", ".");
    return(Config.getValueWithPath("tempFilePath") + "/" + strWhich + "." + scenFileName.trim() + ".conf");
    //return(Config.getValueWithPath("tempFilePath") + "/" + strWhich + "." + scenFileName.trim());
  }

  public String replace(String src, String toReplace, String replaceWith)
  {
    if (src != null)
    {
      final int len = toReplace.length();
      StringBuffer sb = new StringBuffer();
      int found = -1;
      int start = 0;
      while((found = src.indexOf(toReplace, start) ) != -1)
      {
        sb.append(src.substring(start, found));
        sb.append(replaceWith);
        start = found + len;
      }
      sb.append(src.substring(start));
      return sb.toString();
    }
    else return "";

  }

  public static String escapeHTML(String s)
  {
    Log.debugLog(className, "escapeHTML", "", "",  "Method Called. String = " + s);
    if((s == null) || (s.length() == 0))
      return s;
    StringBuffer sb = new StringBuffer();
    int n = s.length();
    for (int i = 0; i < n; i++)
    {
       char c = s.charAt(i);
       switch (c)
       {
          case '<': sb.append("&lt;"); break;
          case '>': sb.append("&gt;"); break;
          case '&': sb.append("&amp;"); break;
          case '"': sb.append("&quot;"); break;
          case '\'': sb.append("&#39;"); break;
          case '\\': sb.append("&#92;"); break;
          // be carefull with this one (non-breaking whitee space)
//          case ' ': sb.append("&nbsp;");break;

          default:  sb.append(c); break;
       }
    }
    Log.debugLog(className, "escapeHTML", "", "",  "returning String = " + sb.toString());
    return sb.toString();
  }

  public static String unEscapeHTML(String s)
  {
    Log.debugLog(className, "unEscapeHTML", "", "",  "Method Called. String = " + s);
    if((s == null) || (s.length() == 0))
      return s;
    s = s.replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&amp;", "&");
    s = s.replaceAll("&quot;", "\\\\\\\"").replaceAll("&#39;", "\\\\\\\'").replaceAll("&#92;", "\\\\\\\\");

    Log.debugLog(className, "unEscapeHTML", "", "",  "returning String = " + s);
    return s;
  }

  // Create temp file (hot and cold) of Opened Scenario
  public boolean createTempFiles(String scenFileNameWithPath, String filename)
  {
    try
    {
      // String conf_file = Config.getValueWithPath("scenarioPath") + "/" + filename.trim() + ".conf";
      //String conf_file = Config.getValueWithPath("scenarioPath") + "/" + filename.trim();
      String conf_file = scenFileNameWithPath;
      File file_conf = new File(conf_file);
      if(file_conf.exists())
      {
        String hot_file = genScenTempFileName(filename, "hot");
        File file_hot = new File(hot_file);
        /*
         if(file_hot.exists())
         file_hot.delete();
         file_hot.createNewFile();

         copyToFile will create file if not exist
        */

        copyToFile(conf_file, hot_file);

        String cold_file = genScenTempFileName(filename, "cold");
        File file_cold = new File(cold_file);

        /*
        if(file_cold.exists())
          file_cold.delete();
        file_cold.createNewFile();

        copyToFile will create file if not exist
        */

        copyToFile(conf_file, cold_file);
        return true;
      }
      else
        return false;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "createTempFiles", "", "", "Exception in createTempFiles()" + e);
    }
    return false;
  }

  // copy hot file content into cold.
  public boolean copyHottoCold(String filename)
  {
    try
    {
      String hot_file = genScenTempFileName(filename, "hot");
      String cold_file = genScenTempFileName(filename, "cold");

      File file_hot = new File(hot_file);
      File file_cold = new File(cold_file);

      /*
      if(file_hot.exists() && file_cold.exists())
      {
        file_cold.delete();
        file_cold.createNewFile();
        copyToFile(hot_file, cold_file);
        return true;
      }
      else
        return false;

      */
      copyToFile(hot_file, cold_file);
      return true;

    }
    catch(Exception e)
    {
      Log.errorLog(className, "copyHottoCold", "", "", "Exception in copyHottoCold()" + e);
    }
    return true;
  }

  //file Name now will be like <projectName>/<subProjectName>/<fileName> with out extension
  public boolean copyColdtoHot(String filename)
  {
    try
    {
      String hot_file = genScenTempFileName(filename, "hot");
      String cold_file = genScenTempFileName(filename, "cold");

      File file_hot = new File(hot_file);
      File file_cold = new File(cold_file);

     /*
      if(file_hot.exists() && file_cold.exists())
      {
        file_hot.delete();
        file_hot.createNewFile();
        appendToFile(cold_file, hot_file);
        return true;
      }
      else
        return false;

      copyToFile will create file if not exist
        */
      copyToFile(cold_file, hot_file);
      return true;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "copyColdtoHot", "", "", "Exception in copyColdtoHot()" + e);
    }
    return true;
  }

  public boolean copyHottoConf(String filename)
  {
    try
    {
      String conf_file = Config.getValueWithPath("scenarioPath") + "/" + filename.trim() + ".conf";

      //String conf_file = Config.getValueWithPath("scenarioPath") + "/" + filename.trim();
      String hot_file = genScenTempFileName(filename, "hot");

      File file_hot = new File(hot_file);
      File file_conf = new File(conf_file);

      /*
      if(file_hot.exists() && file_conf.exists())
      {
        file_conf.delete();
        file_conf.createNewFile();
        appendToFile(hot_file, conf_file);
        return true;
      }
      else
        return false;

        copyToFile will create file if not exist
      */

      copyToFile(hot_file, conf_file);
      return true;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "copyHottoConf", "", "", "Exception in copyHottoConf()" + e);
    }
    return true;
  }

  /**
   This method is NOT USED presently, now the scripts are fetched from
   the "nsu_show_scripts"



  //need to append the proj and subproj name for getting the script name
  public String[] getSessionNames()
  {
    // Note - Script and Session are the same thing
    String scriptPath = Config.getValueWithPath("sessionPath");
    File fileScriptPath = new File(scriptPath);
    String arrScripts[] = fileScriptPath.list();
    ArrayList alTmp = new ArrayList();
    // Take only directories in sessionPath and also filter WEB-INF
    for(int i = 0; i < arrScripts.length; i++)
    {
      if(!arrScripts[i].equals("WEB-INF"))
      {
        File fileScript = new File(scriptPath + "/" + arrScripts[i]);
        if(fileScript.isDirectory())
          alTmp.add(arrScripts[i]);
      }
    }
    // This code for sorting the scripts list
    Object arrStrTmp[] = alTmp.toArray();
    Arrays.sort(arrStrTmp);
    return(objArrToStrArr(arrStrTmp));
  }*/

  public String arrToString(String arrString[])
  {
    String strTmp = arrString[0];
    for(int i = 1; i < arrString.length; i++)
      strTmp = strTmp + "," + arrString[i];
    return(strTmp);
  }

  /**
   This method has been moved to CmdExec.java file,
   it is here for backward compatibility,
   to be remove after changes in ALL jsp and java source code.

   It should be called directly from CmdExec.java file
   */
  /*public Vector getResultByCommand(String cmd, String args)
  {
    return cmdExec.getResultByCommand(cmd, args);
  }*/

  // Added by Neeraj in release 1.2.1 - 2/12/05

  public BufferedReader openFile(String path, String fname)
  {
    try
    {
      String f = path + "/" + fname.trim();
      File file = new File(f);

      if(file.exists())
      {
        FileInputStream fin = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fin));
        return(br);
      }
      Log.errorLog(className, "openFile", "", "", "File not found - " + f);
      return null;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "openFile", "", "", "Exception - " + e);
      return null;
    }
  }

  public String[] objArrToStrArr(Object arrObj[])
  {
/*    String arrTmp[] = new String[arrObj.length];
    for(int i = 0; i < arrObj.length; i++)
      arrTmp[i] = (String )arrObj[i];

    return(arrTmp);
*/
    int length = arrObj.length;
    ArrayList list = new ArrayList(Arrays.asList(arrObj));

    String strArr[] = new String[length];
    strArr = (String[])list.toArray(strArr);

    return strArr;

  }

  // Return all GUI keyword names in a array
  // These are taken from $NS_WDIR/etc/ScenControl.dat file. Format is
  //  KEYWORD <keyword> <Comments> e.g
  //  KEYWORD ULOCATION ulocations
  //  KEYWORD UPLOCATION uplocation
  //  KEYWORD UACCESS uaccess

  public String[] getGUIKeywords()
  {
/*    ArrayList alTmp = new ArrayList();
    String strLine;

    BufferedReader br = openFile(Config.getValueWithPath("workPath"), "/etc/ScenControl.dat");
    if(br == null)
      return null;
    try
    {
      while((strLine = br.readLine()) != null)
      {
        if((strLine.trim().equals("")) || (strLine.trim().startsWith("#")))
          continue;

        StringTokenizer st = new StringTokenizer(strLine);
        String field1 = st.nextToken();
        if(!field1.equals("KEYWORD"))
          continue;

        if(st.hasMoreTokens())
          alTmp.add(st.nextToken().trim());  // keyword name
      }
      Object arrStrTmp[] = alTmp.toArray();
      Arrays.sort(arrStrTmp);
      return(objArrToStrArr(arrStrTmp));
    }
    catch(Exception e)
    {
      Log.errorLog(className, "getGUIKeywords", "", "", "Exception - " + e);
      return null;
    }*/

    if(keywordDefinition == null)
      keywordDefinition = new KeywordDefinition();

    return objArrToStrArr(keywordDefinition.getGUIKeywords());
  }

  public String[] getKeywordsList()
  {
    if(keywordDefinition == null)
      keywordDefinition = new KeywordDefinition();

    Set set = keywordDefinition.getHashForKeywords().keySet();

    ArrayList list = new ArrayList(set);

    return objArrToStrArr(list.toArray());
  }

  // Return all Keyword and it's value which are not in arrKeywords
  public String[] getOtherKeyValue(Properties p, String arrKeywords[])
  {
    ArrayList alTmp = new ArrayList();

    if(keywordDefinition == null)
      keywordDefinition = new KeywordDefinition();

    String scenFile = genScenTempFileName(this.scenFileName, "hot");
    Object arrScenKeywords[] = getFileData(scenFile);

    for(int k = 0 ; k < arrScenKeywords.length ; k++)
    {
      String keyword = getKeywordName(p, (String)arrScenKeywords[k]);

      KeywordDefinition.Keywords keywordObj = (KeywordDefinition.Keywords)keywordDefinition.getHashForKeywords().get(keyword);

      if(keywordObj == null)
      {
        Log.debugLog(className, "getOtherKeyValue", "", "", "keywordObj obj is coming null, keyword = " + keyword + " not found in the keyword defition file" );
        return null;
      }

      //If keyword is not in GUI
      if(!keywordObj.getIsInGUI())
      {
        Log.debugLog(className, "getOtherKeyValue", "", "", "Keyword = " + keyword + " is not in GUI adding to array");
        alTmp.add(arrScenKeywords[k].toString());
      }
      else
        Log.debugLog(className, "getOtherKeyValue", "", "", "Keyword = " + keyword + " is in GUI ignored");
    }
    Object arrStrTmp[] = alTmp.toArray();
//    Arrays.sort(arrStrTmp);
    return(objArrToStrArr(arrStrTmp));
  }

  public void  clearKeywordValue(Properties p, String keywordVal)
  {
    if(keywordVal.trim().equals("") || keywordVal.trim().startsWith("#"))
      return;
    StringTokenizer st = new StringTokenizer(keywordVal);
    String key = st.nextToken();      // key
    // p.setProperty(key, "");
    p.remove(key);
  }

  // Get keyword name (e.g. if keywordVal is "RUN_TIME 0 I, keyword Name is RUN_TIME
  private String getKeywordName(Properties p, String keywordVal)
  {
    StringTokenizer st = new StringTokenizer(keywordVal);
    return(st.nextToken());
  }


  // Sets keyword in the proporties if not existing otherwise append it
  // For every keyword line, keyword is the key and remaning part is the value.
  // For multiple lines for same keywords, value is stored separated by |
  // e.g  "Value1 Value2|Value3 Value4"

  private void setKeyValue(Properties p, String keywordVal)
  {
    setKeyValue(p, keywordVal, true);
  }

  // bottom = true if at the end.
  private void setKeyValue(Properties p, String keywordVal, boolean bottom)
  {
//    Log.debugLog(className, "setKeyValue", "", "", "keywordVal = " + keywordVal);
    
    if(keywordVal.trim().equals("") || keywordVal.trim().startsWith("#"))
      return;

    StringTokenizer st = new StringTokenizer(keywordVal);
    String key = st.nextToken();      // key
    String values = "";
    while(st.hasMoreTokens())
      values = values + " " + st.nextToken();  // values
    values = values.trim();
    // If old was empty, we need to replace
    if((p.getProperty(key) == null) || p.getProperty(key).equals(""))
      p.setProperty(key, values);
    else
    {
      String temp_value = p.getProperty(key);
      if(bottom == true)
        temp_value = temp_value + "|" + values;
      else
        temp_value = values + "|" + temp_value;
      
      p.setProperty(key, temp_value);
      
    }
//    Log.debugLog(className, "setKeyValue", "", "", "Property after set = " + key + " " + p.getProperty(key));
  }

  // Replace/Add Keyword and it's value in Properties p which are in otherKeywords
  // otherKeywords have one or more keywords/values with new line as separtor
  //  e.g  "MY_KW1 test\r\nMY_KW2"

  public void setOtherKeyValue(Properties p, String otherKeywords)
  {
    int i;
    Log.debugLog(className, "setOtherKeyValue", "", "", "otherKeywords = \n" + otherKeywords);
    String arrOtherKeywords[] = otherKeywords.split("\r\n");
    Log.debugLog(className, "setOtherKeyValue", "", "", "After splitting, array length =  " + arrOtherKeywords.length);

    // First clear old values. This must be done first in a separate loop
    // Get Existing Other Keywords from file
    String arrGuiKeywords[] = getGUIKeywords();
    String arrOldOtherKeywords[] = getOtherKeyValue(p, arrGuiKeywords);

    for(i = 0; i < arrOldOtherKeywords.length; i++)
      clearKeywordValue(p, arrOldOtherKeywords[i]);

    for(i = 0; i < arrOtherKeywords.length; i++)
      setKeyValue(p, arrOtherKeywords[i]);
  }

  // Cancels keywords updates in hot file by taking value from cold file
  // strKeywords contains keywords (, separated)
  // These functions are used in child screens where we need to update
  // only keywords owned by the screen
  public void undoUpdate(Properties hot, String strScenFileName, String strKeywords)
  {
    Properties cold;

    Log.debugLog(className, "undoUpdate", "", "", "Keywords = " + strKeywords);
    StringBuffer errMsg = new StringBuffer();
    cold = getKeyValues(strScenFileName, "cold", errMsg);

    StringTokenizer st = new StringTokenizer(strKeywords, ",");
    while(st.hasMoreTokens())
    {
      String keyword = st.nextToken();
      hot.setProperty(keyword, cold.getProperty(keyword));
    }
    updateKeyValue(hot, strScenFileName, "hot", errMsg);
  }

  // Do keywords updates in cold file by taking value from hot file
  // These functions are used in child screens where we need to update
  // only keywords owned by the screen
  // strKeywords contains keywords (, separated)
  public void doUpdate(Properties hot, String strScenFileName, String strKeywords)
  {
    Properties cold;

    Log.debugLog(className, "doUpdate", "", "", "Keywords = " + strKeywords);
    StringBuffer errMsg = new StringBuffer();
    cold = getKeyValues(strScenFileName, "cold", errMsg);

    StringTokenizer st = new StringTokenizer(strKeywords, ",");
    while(st.hasMoreTokens())
    {
      String keyword = st.nextToken();
      cold.setProperty(keyword, hot.getProperty(keyword));
    }

   updateKeyValue(cold, strScenFileName, "cold", errMsg);

  }

  //In following cases it will return true:
  //In edit mode
  //shell executed successfully in run time mode
  public boolean okHandler(Properties p, String strScenFileName, String strJSPFileName, StringBuffer errorMsg)
  {
    if(RUNTIMECHANGE_MODE_FLAG)
      return executeRunTimeChangableFile(errorMsg);
    else
    {
      copyHottoCold(strScenFileName);
      return true;
    }
  }

/* Added in 1.2.2 */
  public boolean okHandler(Properties p, String strScenFileName, String strJSPFileName)
  {
    copyHottoCold(strScenFileName);
    return true;
  }

  public boolean saveScenHandler(Properties p, String strScenFileName, String strJSPFileName)
  {
    copyHottoConf(strScenFileName);

    if(DEFAULT_SETTING_MODE_FLAG)
    {
      String strSrcFileName = Config.getValueWithPath("scenarioPath") + strScenFileName + ".conf";
      String strDestFileName = siteDefaultKeywordFile;
      Log.debugLog(className, "saveScenHandler", "", "", "destination file to be copied strDestFileName = " + strDestFileName);
      rptUtilsBean.copyToFile(strSrcFileName, strDestFileName);
    }
    
    return true;
  }

  /**
   * This method is called from scheduler GUI through servlet.
   *
   * @param p - property object which contains all the keywords to write
   * @param strScenFileName - name of scenario
   * @return
   */

  public boolean saveScenanrio(Properties p, String strScenFileName, String userName, boolean isForHotOnly, StringBuffer errMsg)
  {
    Log.debugLog(className, "saveScenanrio", "", "", "Method Called. strScenFileName = " + strScenFileName);
    try
    {
      if(!updateKeyValue(p, strScenFileName, "hot", errMsg))
        return false;

      if(isForHotOnly)
        return true;

      String hot_file = genScenTempFileName(strScenFileName, "hot");
      String cold_file = genScenTempFileName(strScenFileName, "cold");

      copyToFile(hot_file, cold_file);
      String scenFileName = Config.getValueWithPath("scenarioPath") + "/" + strScenFileName + ".conf";
      copyToFile(cold_file, scenFileName);

      String strCmdName = "nsi_post_proc_scen";
      String strCmdArgs = strScenFileName + " " + userName;
      CmdExec exec = new CmdExec();
      Vector vecCmdOutput = exec.getResultByCommand(strCmdName, strCmdArgs, 0, userName, "root");

      if(vecCmdOutput == null)
      {
        errMsg.append("Error in executing nsi_post_proc_scen command");
        return false;
      }

      if((vecCmdOutput.size() > 0) && ((String)vecCmdOutput.lastElement()).startsWith("ERROR"))
      {
        for(int i = 0; i < (vecCmdOutput.size() - 1); i++)
          errMsg.append(vecCmdOutput.elementAt(i).toString() + "\n");

        return false;
      }

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "saveScenanrio", "", "", "Exception in saveScnanio()", e);
      errMsg.append("Error in saving scenario");
      return false;
    }
  }

  //This method work on cancel
  //copy cold file contents into hot file (work as undo)
  //If it is in runtime mode it will delete hidden file
  public boolean cancelHandler(Properties p, String strScenFileName, String strJSPFileName)
  {
    if(RUNTIMECHANGE_MODE_FLAG)
      deleterunTimeHiddenFile();
    else
      copyColdtoHot(strScenFileName);
    return true;
  }

  // Java String.split not working for | separator. So coded own function.
  public String[] split(String strList, String strSeparator)
  {
    StringTokenizer st = new StringTokenizer(strList, strSeparator);
    String arrTmp[] = new String[st.countTokens()];
    int i = 0;
    while(st.hasMoreTokens())
      arrTmp[i++] = st.nextToken().trim();
    return(arrTmp);
  }

  // Name: copyKeywordRecords
  // Purpose: To make copy of keywords of key1 (source) to  key2 (Destination).
  // Arguments:
  //  Arg1: Properties with all keywords.
  //  Arg2: Pipe separated keyword list
  //  Arg3: List of Field Id which are to compared with source fields..
  //        It must be in the sorted order e.g. "0|3|5"
  //  Arg4: Source key value list for keyword records which are to be copied.
  //  Arg5: Destination key value list for keyword records which are to be added.
  //
  // Return: -1 if erorr else number of keywords records copied.

  // Example: To copy all UPLOCATION,UPACCESS,UPBROWSER,UPAL keywords whose first
  //          field is Internet for Neeraj, pass following arguments:
  //   copyKeywordRecords(p, "UPLOCATION|UPACCESS|UPBROWSER|UPAL", "0", "Internet", "Neeraj");

  public int copyKeywordRecords(Properties p, String strKeywordList, String strFldIdList, String strSrcFldValList, String strDestFldValList)
  {
    Log.debugLog(className, "copyKeywordRecords", "", "", "KeywordList = " + strKeywordList + ", FieldIdList = " + strFldIdList + ", SrcFldValList = " + strSrcFldValList + ", DestFldValList = " + strDestFldValList);

    String arrFldId[] = split(strFldIdList, "|");
    String arrSrcFldVal[] = split(strSrcFldValList, "|");
    String arrDestFldVal[] = split(strDestFldValList, "|");
    int numCopied = 0, i = 0;

    if((arrFldId.length != arrSrcFldVal.length) || (arrDestFldVal.length != arrSrcFldVal.length))

    {
      Log.errorLog(className, "copyKeywordRecords", "", "", "Number of field Ids are not correct. Length = " + arrFldId.length);
      return -1;
    }

    StringTokenizer stKW = new StringTokenizer(strKeywordList, "|");
    while(stKW.hasMoreTokens())
    {
      String strKeyword = stKW.nextToken().trim();
      String strKeywordVal = p.getProperty(strKeyword);
      Log.debugLog(className, "copyKeywordRecords", "", "", "Keyword Value before copy for  " + strKeyword + " = " + strKeywordVal);
      if((strKeywordVal == null) || strKeywordVal.equals(""))
      {
        Log.errorLog(className, "copyKeywordRecords", "", "", "Source keyword does not exist. Keyword = " + strKeyword);
        continue;
      }
      StringTokenizer stRecords = new StringTokenizer(strKeywordVal, "|");

      while(stRecords.hasMoreTokens())
      {
        String strLine = stRecords.nextToken().trim();
        Log.debugLog(className, "copyKeywordRecords", "", "", "Checking for keyword = " + strKeyword + " " + strLine);
        StringTokenizer stFlds = new StringTokenizer(strLine);
        int fldIdx = 0;
        String arrFlds[] = new String[stFlds.countTokens()];
        while(stFlds.hasMoreTokens())
        {
          String token = stFlds.nextToken().trim();
          arrFlds[fldIdx++] = token;
        }

        String strRecordsKeys = "";
        for(i = 0; i < arrFldId.length; i++)
        {
          int fldId = Integer.parseInt(arrFldId[i]);
          if(strRecordsKeys.equals(""))
            strRecordsKeys = arrFlds[fldId];
          else
            strRecordsKeys = strRecordsKeys + "|" + arrFlds[fldId];
          arrFlds[fldId] = arrDestFldVal[i];  // Replace with Dest Field Value
        }

        if(strRecordsKeys.equals(strSrcFldValList))
        {
          String strCopyKeywords = strKeyword;
          for(i = 0; i < arrFlds.length; i++)
            strCopyKeywords = strCopyKeywords + " " + arrFlds[i];
          
          setKeyValue(p, strCopyKeywords);
          
          numCopied++;
          strKeywordVal = p.getProperty(strKeyword);
          Log.debugLog(className, "copyKeywordRecords", "", "", "Keyword Value after copy for  " + strKeyword + " = " + strKeywordVal);
        }
        else
          Log.debugLog(className, "copyKeywordRecords", "", "", "Key did not match. Copy not done");
      }
    }
    // updateKeyValue(p, strScenFileName); // strScenFileName not passed
    return(numCopied);
  }

  // Name: deleteKeywordRecords
  // Purpose: Delete keywords of key1 (source).
  // Arguments:
  //  Arg1: Properties with all keywords.
  //  Arg2: Pipe separated keyword list
  //  Arg3: List of Field Id which are to compared with source fields..
  //        It must be in the sorted order e.g. "0|3|5"
  //  Arg4: Source key value list for keyword records which are to be deleted.
  //
  // Return: -1 if erorr else number of keywords records deleted.

  // Example: To delete all UPLOCATION,UPACCESS,UPBROWSER,UPAL keywords whose first
  //          field is Neeraj, pass following arguments:
  //   deleteKeywordRecords(p, "UPLOCATION|UPACCESS|UPBROWSER|UPAL", "0", "Neeraj");

  public int deleteKeywordRecords(Properties p, String strKeywordList, String strFldIdList, String strSrcFldValList)
  {
    Log.debugLog(className, "deleteKeywordRecords", "", "", "KeywordList = " + strKeywordList + ", FieldIdList = " + strFldIdList + ", SrcFldValList = " + strSrcFldValList);

    String arrFldId[] = split(strFldIdList, "|");
    String arrSrcFldVal[] = split(strSrcFldValList, "|");
    int numDeleted = 0, i = 0;

    if(arrFldId.length != arrSrcFldVal.length)

    {
      Log.errorLog(className, "deleteKeywordRecords", "", "", "Number of field Ids are not correct. Length = " + arrFldId.length);
      return -1;
    }

    StringTokenizer stKW = new StringTokenizer(strKeywordList, "|");
    while(stKW.hasMoreTokens())
    {
      String strKeyword = stKW.nextToken().trim();
      String strKeywordVal = p.getProperty(strKeyword);
      Log.debugLog(className, "deleteKeywordRecords", "", "", "Keyword Value before delete for  " + strKeyword + " = " + strKeywordVal);
      if((strKeywordVal == null) || strKeywordVal.equals(""))
      {
        Log.errorLog(className, "deleteKeywordRecords", "", "", "Source keyword does not exist. Keyword = " + strKeyword);
        continue;
      }
      StringTokenizer stRecords = new StringTokenizer(strKeywordVal, "|");

      clearKeywordValue(p, strKeyword);
      while(stRecords.hasMoreTokens())
      {
        String strLine = stRecords.nextToken().trim();
        String strSaveLine = new String(strLine);
        Log.debugLog(className, "deleteKeywordRecords", "", "", "Checking for keyword = " + strKeyword + " " + strLine);
        StringTokenizer stFlds = new StringTokenizer(strLine);
        int fldIdx = 0;
        String arrFlds[] = new String[stFlds.countTokens()];
        while(stFlds.hasMoreTokens())
          arrFlds[fldIdx++] = stFlds.nextToken().trim();

        String strRecordsKeys = "";
        for(i = 0; i < arrFldId.length; i++)
        {
          int fldId = Integer.parseInt(arrFldId[i]);
          if(strRecordsKeys.equals(""))
            strRecordsKeys = arrFlds[fldId];
          else
            strRecordsKeys = strRecordsKeys + "|" + arrFlds[fldId];
        }

        if(strRecordsKeys.equals(strSrcFldValList))
        {
          numDeleted++;
          Log.debugLog(className, "deleteKeywordRecords", "", "", "Keyword deleted for  " + strKeyword + " key = " + strRecordsKeys);
        }
        else
        {
          Log.debugLog(className, "deleteKeywordRecords", "", "", "Key did not match. Keyword not deleted for  " + strKeyword + " key = " + strRecordsKeys);
          setKeyValue(p, strKeyword + " " + strSaveLine);
        }
      }
      Log.debugLog(className, "deleteKeywordRecords", "", "", "Keyword after delete for  " + strKeyword + " = " + p.getProperty(strKeyword));

    }
    return(numDeleted);
  }

  /**
   * This will delete the keyword record from property for group based keywords
   *
   * This is needed to remove all keywords for any specific GROUP, when that
   * GROUP is removed from scenario.
   *
   * @param p
   * @param keywordNameList
   * @param groupName
   * @return
   */
  public boolean deleteGroupBasedKeywordsOfGroup(Properties p, String keywordNameList, String groupName)
  {
    Log.debugLog(className, "deleteGroupBasedKeywordsOfGroup", "", "", "Method Called. keywordNameList = " + keywordNameList + ", groupName = " + groupName);
    try
    {
      if(groupName.toUpperCase().equals("ALL"))
      {
        Log.errorLog(className, "deleteGroupBasedKeywordsOfGroup", "", "", "Group name can not be ALL, groupName = " + groupName);
        return false;
      }

      int delCount = -1;

      if(keywordNameList.equals(""))//Keywords list is NOT already given
      {
        Log.debugLog(className, "deleteGroupBasedKeywordsOfGroup", "", "", "Keyword list is empty so getting all group based keyword list from keyword definition file.");

        if(keywordDefinition == null)
          keywordDefinition = new KeywordDefinition();
        keywordNameList = getGroupBasedKeywordList(keywordDefinition, "|");

        if(keywordNameList == null)
        {
          Log.errorLog(className, "deleteGroupBasedKeywordsOfGroup", "", "", "Error in getting keyword name list");
          return false;
        }
      }

      delCount = deleteKeywordRecords(p, keywordNameList, "0", groupName);

      if(delCount == -1)//When error in calling deleteKeywordRecords methods
      {
        Log.errorLog(className, "deleteGroupBasedKeywordsOfGroup", "", "", "Error in calling deleteKeywordRecords for the args. keywordNameList = " + keywordNameList + ", strFldIdList = 0" + ", groupName = " + groupName);
        return false;
      }
      if(delCount == 0)//When error in calling deleteKeywordRecords methods
        Log.debugLog(className, "deleteGroupBasedKeywordsOfGroup", "", "", "No record can be deleted for the group = " + groupName);

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "deleteGroupBasedKeywordsOfGroup", "", "", "Exception in deleteGroupBasedKeywordsOfGroup()" , e);
      return false;
    }
  }

  public boolean updateGroupName(Properties p, String oldGroupName, String newGroupName)
  {
    Log.debugLog(className, "updateGroupName", "", "", "Method Called. oldGroupName = " + oldGroupName + ", newGroupName = " + newGroupName);
    try
    {
      String groupBasedKeywords = getGroupBasedKeywordList(keywordDefinition, " ");

      if(groupBasedKeywords == null)
      {
        Log.errorLog(className, "updateGroupName", "", "", "groupBasedKeywords list is coming null");
        return false;
      }

      String strArrKeywords[] = groupBasedKeywords.split(" ");

      for(int i  = 0 ; i < strArrKeywords.length ; i++)
      {
        String keywordName = strArrKeywords[i];
        String keywordVal = p.getProperty(keywordName);

        if((keywordVal == null) || keywordVal.equals(""))
        {
          Log.errorLog(className, "updateGroupName", "", "", "Source keyword does not exist in the property. keywordName = " + keywordName);
          continue;
        }

        Log.debugLog(className, "updateGroupName", "", "", "Checking for the keyword = " + keywordName);

        StringTokenizer stRecords = new StringTokenizer(keywordVal, "|");
        int id = 0;

        while(stRecords.hasMoreElements())
        {
          String strLine = stRecords.nextToken().trim();

          Log.debugLog(className, "updateGroupName", "", "", "Checking for the keyword = " + keywordName + ", strLine = " + strLine);
          String arrFields[] = strLine.split(" ");

          //If first index is group name then it will be matched then need to update
          if(arrFields[0].equals(oldGroupName))
          {
            Log.debugLog(className, "updateGroupName", "", "", "Keyword found for the group = " + oldGroupName + ", keywordName = " + keywordName + ", strLine = " + strLine);

            String strArr[] = new String[]{id+""};
            int count = deleteKeywordRecordsByRowId(p, strArrKeywords[i], "", "", strArr);

            if(count <= 0)//it must be one
            {
              Log.errorLog(className, "updateGroupName", "", "", "Can not delete the keyword value for the group = " + oldGroupName + ", keywordName = " + keywordName + ", strLine = " + strLine + ", count = " + count);
              return false;
            }

            addKeywordRecord(p, strArrKeywords[i], replace(strLine, oldGroupName, newGroupName));
          }

          id++;
        }
      }
      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "updateGroupName", "", "", "Exception in updateGroupName()" , e);
      return false;
    }
  }

  /** This Method is used to save scenario as profile
   * 
   * @param projectSubProjectScenName
   * @param errMsg
   * @return
   */
  public boolean saveScenarioAsProfile(String projectSubProjectScenName, StringBuffer errMsg, String owner)
  {
    try
    {
      Log.debugLog(className, "saveScenarioAsProfile", "", "", "Method Called.");
      String[] arrData = projectSubProjectScenName.split("##");
      
      if(keywordDefinition == null)
        keywordDefinition = new KeywordDefinition();
      
      LinkedHashMap mapHash = keywordDefinition.getHashForKeywords();
      String scenarioFileWithPath = Config.getValueWithPath("scenarioPath") + "/" + arrData[0] + ".conf";      
      File scenarioFile = new File(scenarioFileWithPath);
      
      if(!scenarioFile.exists())
      {
        appendError(errMsg, "Scenario profile " + arrData[0] + " not exist.");
        return false;
      }
      else
      {
        if(!createScenarioSettingProfile(arrData[1] , errMsg, owner))
          return false;
        
        String scenProfileFileNameWithPath = scenProfileFileNameWithPathValue + "/" + arrData[1].trim() + ".ssp";
        FileOutputStream fout = new FileOutputStream(scenProfileFileNameWithPath);
        PrintStream pw = new PrintStream(fout);

        ArrayList arrListOfScenarioProfileKeywords = getScenarioSettingProfileKeywordList(keywordDefinition); 
        Vector vecScenarioData = rptUtilsBean.readFileInVector(scenarioFileWithPath);
        for(int ii = 0; ii < vecScenarioData.size(); ii++)
        {
          String keywordWithValue = vecScenarioData.get(ii).toString();
          String[] arrKeywordValue = keywordWithValue.split(" ");
          KeywordDefinition.Keywords keywordObj = (KeywordDefinition.Keywords)mapHash.get(arrKeywordValue[0].trim());
          if(!keywordObj.getDefaultValue().equals(arrKeywordValue[1].trim()) && arrListOfScenarioProfileKeywords.contains(arrKeywordValue[0].trim()))
          {
            if(!keywordObj.getIsGroupBased())
              pw.println(keywordWithValue);
            else
            {
              String[] splitValueOfKeyword = arrKeywordValue[1].trim().split(" ");
              if(splitValueOfKeyword[0].trim().toUpperCase().equals("ALL"))
                pw.println(keywordWithValue);
            }
          }
        }
        pw.close();
        fout.close();
        return true;
      }
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "saveScenarioAsProfile", "", "", "Exception caught - ", ex);
      appendError(errMsg, "An error occurred while saving scenario as profile. Please see error logs");
      return false;
    }
  }
  
  /** This method is used to get those keywords which can be written in scenario Profile
   * 
   * @param keywordDefinition
   * @return
   */
  private ArrayList getScenarioSettingProfileKeywordList(KeywordDefinition keywordDefinition)
  {
    Log.debugLog(className, "getScenarioSettingProfileKeywordList", "", "", "Method Called.");

    try
    {
      ArrayList keywordNameList = new ArrayList();
      LinkedHashMap mapHash = keywordDefinition.getHashForKeywords();
      Iterator iterator = mapHash.keySet().iterator();

      while(iterator.hasNext())
      {
        String keywordName = (String)iterator.next();
        KeywordDefinition.Keywords keywordObj = (KeywordDefinition.Keywords)mapHash.get(keywordName);

        if(keywordObj.getFuture1().toString().toUpperCase().equals("YES"))
          keywordNameList.add(keywordName);
      }

     return keywordNameList;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getScenarioSettingProfileKeywordList", "", "", "Exception in getGroupBasedKeywordList()" , e);
      return null;
    }
  }

  
  private String getGroupBasedKeywordList(KeywordDefinition keywordDefinition, String separator)
  {
    Log.debugLog(className, "getGroupBasedKeywordList", "", "", "Method Called.");

    try
    {
      String keywordNameList = "";
      LinkedHashMap mapHash = keywordDefinition.getHashForKeywords();
      Iterator iterator = mapHash.keySet().iterator();

      while(iterator.hasNext())
      {
        String keywordName = (String)iterator.next();
        KeywordDefinition.Keywords keywordObj = (KeywordDefinition.Keywords)mapHash.get(keywordName);

        if(keywordObj.getIsGroupBased())
        {
          if(keywordNameList.equals(""))
            keywordNameList = keywordName;
          else
            keywordNameList = keywordNameList + separator +keywordName;
        }
      }

     return keywordNameList;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getGroupBasedKeywordList", "", "", "Exception in getGroupBasedKeywordList()" , e);
      return null;
    }
  }

  // Name: deleteKeywordRecordsByRowId
  // Purpose: Delete keyword record of passed key by Row Id
  // Arguments:
  //  Arg1: Properties with all keywords.
  //  Arg2: Pipe separated keyword list
  //  Arg3: List of Field Id which are to compared with source fields..
  //        It must be in the SORTED order e.g. "0|3|5"
  //  Arg4: Source key value list for keyword records which are to be deleted.
  //  Arg5: Array of Row Ids
  //
  // Return: -1 if erorr else number of keywords records deleted.

  // Example: To delete 1,2 and 5th record of UPLOCATION,UPACCESS keywords
  //          whose first field is Neeraj, pass following arguments:
  //   deleteKeywordRecordsByRowId(p, "UPLOCATION|UPACCESS", "0", "Neeraj", array of rowIds);
  //

 //KeywordList = G_PAGE_THINK_TIME, FieldIdList = 0, SrcFldValList = g1, RowIds = 0

  public int deleteKeywordRecordsByRowId(Properties p, String strKeywordList, String strFldIdList, String strSrcFldValList, String arrRowId[])
  {
    Log.debugLog(className, "deleteKeywordRecordsByRowId", "", "", "KeywordList = " + strKeywordList + ", FieldIdList = " + strFldIdList + ", SrcFldValList = " + strSrcFldValList + ", RowIds = " + arrToString(arrRowId));

    String arrFldId[] = split(strFldIdList, "|");
    String arrSrcFldVal[] = split(strSrcFldValList, "|");
    Arrays.sort(arrRowId);  // Must be sorted for binary search
    int numDeleted = 0, i = 0;

    if(arrFldId.length != arrSrcFldVal.length)
    {
      Log.errorLog(className, "deleteKeywordRecordsByRowId", "", "", "Number of field Ids are not correct. Length = " + arrFldId.length);
      return -1;
    }

    StringTokenizer stKW = new StringTokenizer(strKeywordList, "|");
    while(stKW.hasMoreTokens())
    {
      String strKeyword = stKW.nextToken().trim();
      String strKeywordVal = p.getProperty(strKeyword);
      Log.debugLog(className, "deleteKeywordRecordsByRowId", "", "", "Keyword Value before delete for  " + strKeyword + " = " + strKeywordVal);
      if((strKeywordVal == null) || strKeywordVal.equals(""))
      {
        Log.errorLog(className, "deleteKeywordRecordsByRowId", "", "", "Source keyword does not exist. Keyword = " + strKeyword);
        continue;
      }
      StringTokenizer stRecords = new StringTokenizer(strKeywordVal, "|");

      clearKeywordValue(p, strKeyword);
      int rowId = 0;
      while(stRecords.hasMoreTokens())
      {
        String strLine = stRecords.nextToken().trim();
        String strSaveLine = new String(strLine);
        Log.debugLog(className, "deleteKeywordRecordsByRowId", "", "", "Checking for keyword = " + strKeyword + " " + strLine);
        StringTokenizer stFlds = new StringTokenizer(strLine);
        int fldIdx = 0;
        String arrFlds[] = new String[stFlds.countTokens()];
        while(stFlds.hasMoreTokens())
          arrFlds[fldIdx++] = stFlds.nextToken().trim();

        String strRecordsKeys = "";
        for(i = 0; i < arrFldId.length; i++)
        {
          int fldId = Integer.parseInt(arrFldId[i]);
          if(strRecordsKeys.equals(""))
            strRecordsKeys = arrFlds[fldId];
          else
            strRecordsKeys = strRecordsKeys + "|" + arrFlds[fldId];
        }

        boolean isKeywordPresentrunTimeFile = true;
        String strRowId = Integer.toString(rowId);
        if(strRecordsKeys.equals("") || strRecordsKeys.equals(strSrcFldValList))
        {
          if(Arrays.binarySearch(arrRowId, strRowId) >= 0)
          {
            if(RUNTIMECHANGE_MODE_FLAG)
            {
              Log.debugLog(className, "deleteKeywordRecordsByRowId", "", "", "RUN time changeable. Keyword deleted for  " + strKeyword + " key = " + strRecordsKeys + ",  KeywordWithValue = " + strKeyword + " " + strSaveLine);
              isKeywordPresentrunTimeFile = updateRunTimeChangableFile(strSrcFldValList, strKeyword + " " + strSaveLine, DELETE_OPERATION);

              if(isKeywordPresentrunTimeFile)
              {
                //setKeyValue(p, strKeyword + " " + strSaveLine);
              }
            }
            numDeleted++;
            Log.debugLog(className, "deleteKeywordRecordsByRowId", "", "", "Keyword deleted for  " + strKeyword + " key = " + strRecordsKeys);
          }
          else
          {
            Log.debugLog(className, "deleteKeywordRecordsByRowId", "", "", "Row Id did not match. Keyword not deleted for  " + strKeyword + " key = " + strRecordsKeys);
            setKeyValue(p, strKeyword + " " + strSaveLine);

          }
          rowId++;
        }
        else
        {
          Log.debugLog(className, "deleteKeywordRecordsByRowId", "", "", "Key did not match. Keyword not deleted for  " + strKeyword + " key = " + strRecordsKeys);
          setKeyValue(p, strKeyword + " " + strSaveLine);
        }
      }
      Log.debugLog(className, "deleteKeywordRecordsByRowId", "", "", "Keyword after delete for  " + strKeyword + " = " + p.getProperty(strKeyword));

    }
    return(numDeleted);
  }


  // Name: deleteKeywordSubRecordsByRowId
  // Purpose: Delete keyword record of passed key by Row Id
  // Arguments:
  //  Arg1: Properties with all keywords.
  //  Arg2: Pipe separated keyword list
  //  Arg3: List of Field Id which are to compared with source fields..
  //        It must be in the SORTED order e.g. "0|3|5"
  //  Arg4: Source key value list for keyword records which are to be deleted.
  //  Arg5: Array of Row Ids
  //
  // Return: -1 if erorr else number of keywords records deleted.

  // Example: To delete 1,2 and 5th sub records of SERVER_HOST keyword
  //          without matching key, pass following arguments:
  //   deleteKeywordSubRecordsByRowId(p, "SERVER_HOST", "", "", 1, 2, array of SubRowIds);
  //

  public int deleteKeywordSubRecordsByRowId(Properties p, String strKeywordList, String strFldIdList, String strSrcFldValList, int numFixedFlds, int numSubRecFlds, String arrSubRowId[])
  {
    Log.debugLog(className, "deleteKeywordSubRecordsByRowId", "", "", "KeywordList = " + strKeywordList + ", FieldIdList = " + strFldIdList + ", SrcFldValList = " + strSrcFldValList + ", numFixedFlds = " + numFixedFlds + ", numSubRecFlds = " + numSubRecFlds + ", SubRowIds = " + arrToString(arrSubRowId));

    String arrFldId[] = split(strFldIdList, "|");
    String arrSrcFldVal[] = split(strSrcFldValList, "|");
    Arrays.sort(arrSubRowId);  // Must be sorted for binary search
    int numDeleted = 0, i = 0;

    if(arrFldId.length != arrSrcFldVal.length)
    {
      Log.errorLog(className, "deleteKeywordSubRecordsByRowId", "", "", "Number of field Ids are not correct. Length = " + arrFldId.length);
      return -1;
    }

    StringTokenizer stKW = new StringTokenizer(strKeywordList, "|");
    while(stKW.hasMoreTokens())
    {
      String strKeyword = stKW.nextToken().trim();
      String strKeywordVal = p.getProperty(strKeyword);
      Log.debugLog(className, "deleteKeywordSubRecordsByRowId", "", "", "Keyword Value before delete for  " + strKeyword + " = " + strKeywordVal);
      if((strKeywordVal == null) || strKeywordVal.equals(""))
      {
        Log.errorLog(className, "deleteKeywordSubRecordsByRowId", "", "", "Source keyword does not exist. Keyword = " + strKeyword);
        continue;
      }
      StringTokenizer stRecords = new StringTokenizer(strKeywordVal, "|");

      clearKeywordValue(p, strKeyword);
      int subRowId = 0;
      while(stRecords.hasMoreTokens())
      {
        String strLine = stRecords.nextToken().trim();
        String strSaveLine = new String(strLine);
        Log.debugLog(className, "deleteKeywordSubRecordsByRowId", "", "", "Checking for keyword = " + strKeyword + " " + strLine);
        StringTokenizer stFlds = new StringTokenizer(strLine);
        int fldIdx = 0;
        String arrFlds[] = new String[stFlds.countTokens()];
        while(stFlds.hasMoreTokens())
          arrFlds[fldIdx++] = stFlds.nextToken().trim();

        String strRecordsKeys = "";
        for(i = 0; i < arrFldId.length; i++)
        {
          int fldId = Integer.parseInt(arrFldId[i]);
          if(strRecordsKeys.equals(""))
            strRecordsKeys = arrFlds[fldId];
          else
            strRecordsKeys = strRecordsKeys + "|" + arrFlds[fldId];
        }

        if(strRecordsKeys.equals("") || strRecordsKeys.equals(strSrcFldValList))
        {
          String strTmpRecord = arrFlds[0]; // Assuming 1st field is fixed
          if(!strRecordsKeys.equals(""))
            strTmpRecord = rptUtilsBean.replace(strRecordsKeys, "|", " ");
          
          boolean atLeastOneSubRec = false;
          for(i = numFixedFlds; i < arrFlds.length; i = i + numSubRecFlds)
          {
            String strSubRowId = Integer.toString(subRowId);
            if(Arrays.binarySearch(arrSubRowId, strSubRowId) >= 0)
            {
              numDeleted++;
              Log.debugLog(className, "deleteKeywordSubRecordsByRowId", "", "", "Keyword deleted for  " + strKeyword + " key = " + strRecordsKeys);
            }
            else
            {
              Log.debugLog(className, "deleteKeywordSubRecordsByRowId", "", "", "Row Id did not match. Keyword not deleted for  " + strKeyword + " key = " + strRecordsKeys);
              for(fldIdx = i; fldIdx < (i + numSubRecFlds); fldIdx++)
                strTmpRecord = strTmpRecord + " " + arrFlds[fldIdx];
              atLeastOneSubRec = true;
            }
            subRowId++;
          }
          if(atLeastOneSubRec == true)
            setKeyValue(p, strKeyword + " " + strTmpRecord);
        }
        else
        {
          Log.debugLog(className, "deleteKeywordSubRecordsByRowId", "", "", "Key did not match. Keyword not deleted for  " + strKeyword + " key = " + strRecordsKeys);
          setKeyValue(p, strKeyword + " " + strSaveLine);
        }
      }
      Log.debugLog(className, "deleteKeywordSubRecordsByRowId", "", "", "Keyword after delete for  " + strKeyword + " = " + p.getProperty(strKeyword));

    }
    return(numDeleted);
  }

  //If Keyword is vector
  public int addKeywordRecord(Properties p, String strKeyword, String groupName, String strAddRecord)
  {
    Log.debugLog(className, "addKeywordRecord", "", "", "Keyword = " + strKeyword + ", AddRecord = " + strAddRecord + ", mode = " + RUNTIMECHANGE_MODE_FLAG);
    if(RUNTIMECHANGE_MODE_FLAG)
    {
      String keywordVal = strAddRecord;

      if(!groupName.equals(""))
        keywordVal = strAddRecord.substring(groupName.length()).trim();

      if(!updateKeywordAtRunTime(p, strKeyword, groupName, keywordVal, VECTOR))
        return -1;
    }
    else if(EDIT_MODE_FLAG || DEFAULT_SETTING_MODE_FLAG)
      setKeyValue(p, strKeyword + " " + strAddRecord, false);

    return 1;
  }

  // Name: addKeywordRecord
  // Purpose: Add keyword record.
  // Arguments:
  //  Arg1: Properties with all keywords.
  //  Arg2: keyword Name
  //  Arg3: Key value (record)
  //
  // Return: Number of  records added (which is always 1).

  // Example: To add UPAL record
  //   addKeywordRecord(p, "UPAL", " Neeraj DSL 50");

  public int addKeywordRecord(Properties p, String strKeyword, String strAddRecord)
  {
    Log.debugLog(className, "addKeywordRecord", "", "", "Keyword = " + strKeyword + ", AddRecord = " + strAddRecord);

    setKeyValue(p, strKeyword + " " + strAddRecord, false);  // Add new record at the top so it is displayed as 1st row in the table
    return (1);
  }

  /**
   * 
   * @param p
   * @param strKeyword
   * @param groupName
   * @param strAddRecord
   * This method add keyword at the bottom EX: RAMPDOWN
   * In run time mode it will add in runtime file
   * In edit mode it will add in the scenario file
   */
  public void addKeywordRecordToBottom(Properties p, String strKeyword, String groupName, String strAddRecord)
  {
    Log.debugLog(className, "addKeywordRecordToBottom", "", "", "Keyword = " + strKeyword + ", Group name = " + groupName + ", AddRecord = " + strAddRecord);
    if(RUNTIMECHANGE_MODE_FLAG)
    {
      String keywordVal = strAddRecord;

      if(!groupName.equals(""))
        keywordVal = strAddRecord.substring(groupName.length()).trim();

      updateKeywordAtRunTime(p, strKeyword, groupName, keywordVal, VECTOR);
    }
    else if(EDIT_MODE_FLAG || DEFAULT_SETTING_MODE_FLAG)
      setKeyValue(p, strKeyword + " " + strAddRecord, true);  // Add new record at the top so it is displayed as 1st row in the table
  }
  
  public void addKeywordRecordToBottom(Properties p, String strKeyword, String strAddRecord)
  {
    Log.debugLog(className, "addKeywordRecordToBottom", "", "", "Keyword = " + strKeyword + ", AddRecord = " + strAddRecord);

    setKeyValue(p, strKeyword + " " + strAddRecord, true);  // Add new record at the top so it is displayed as 1st row in the table
  }

  // Name: addKeywordSubRecord
  // Purpose: Add sub record for a key passed
  // Arguments:
  //  Arg1: Properties with all keywords.
  //  Arg2: keyword
  //  Arg3: "" or List of Field Id which are to compared with source fields..
  //        It must be in the sorted order e.g. "0|3|5"
  //  Arg4: "" or Source key value list for keyword records which are to be returned.
  //  Arg5: Number of fixed fields in the record
  //  Arg6: Number of fields in the sub record which is part of the record.
  //  Arg7: Sub record to be added
  // Return: Number of sub records added (always 1) or -1 if error

  // Example: To add sub record in SERVER_HOST for RecServer1, use
  //   addKeywordSubRecord(p, "SERVER_HOST", "0", "RecServer1", 1, 2, "Server1 Location1")
  //

  public int addKeywordSubRecord(Properties p, String strKeyword, String strFldIdList, String strSrcFldValList, int numFixedFlds, int numSubRecFlds, String strSubRec)
  {
    Log.debugLog(className, "addKeywordSubRecord", "", "", "Keyword = " + strKeyword + ", FieldIdList = " + strFldIdList + ", SrcFldValList = " + strSrcFldValList + ", numFixedFlds = " + numFixedFlds + ", numSubRecFlds = " + numSubRecFlds + ", SubRecord = " + strSubRec);

    String arrFldId[] = split(strFldIdList, "|");
    String arrSrcFldVal[] = split(strSrcFldValList, "|");
    int i, numSubRecAdded = 0;

    if(arrFldId.length != arrSrcFldVal.length)

    {
      Log.errorLog(className, "addKeywordSubRecord", "", "", "Number of field Ids are not correct. Length = " + arrFldId.length);
      return -1;
    }

    String strKeywordVal = p.getProperty(strKeyword);
    Log.debugLog(className, "addKeywordSubRecord", "", "", "Keyword Value for  " + strKeyword + " = " + strKeywordVal);
    if((strKeywordVal == null) || strKeywordVal.equals(""))
    {
      // Currently it is assuming one felds as key - To be enhanced later
      if(arrSrcFldVal.length > 1)
        setKeyValue(p, strKeyword + " " + arrSrcFldVal[0] + " " + arrSrcFldVal[1] + " " + strSubRec);
      else
        setKeyValue(p, strKeyword + " " + arrSrcFldVal[0] + " " + strSubRec);
      return (++numSubRecAdded);
    }

    clearKeywordValue(p, strKeyword);

    StringTokenizer stRecords = new StringTokenizer(strKeywordVal, "|");
    String strNewKeywordVal = "";
    while(stRecords.hasMoreTokens())
    {
      String strLine = stRecords.nextToken().trim();
      String strSaveLine = new String(strLine);
      Log.debugLog(className, "addKeywordSubRecord", "", "", "Checking for keyword = " + strKeyword + " " + strLine);
      StringTokenizer stFlds = new StringTokenizer(strLine);

      int fldIdx = 0;
      String arrFlds[] = new String[stFlds.countTokens()];
      while(stFlds.hasMoreTokens())
        arrFlds[fldIdx++] = stFlds.nextToken().trim();

      String strRecordsKeys = "";
      for(i = 0; i < arrFldId.length; i++)
      {
        int fldId = Integer.parseInt(arrFldId[i]);
        if(strRecordsKeys.equals(""))
          strRecordsKeys = arrFlds[fldId];
        else
          strRecordsKeys = strRecordsKeys + "|" + arrFlds[fldId];
      }

      if(strRecordsKeys.equals("") || strRecordsKeys.equals(strSrcFldValList))
      {
        Log.debugLog(className, "addKeywordSubRecord", "", "", "Keyword record found. Adding sub record for  " + strKeyword + " key = " + strRecordsKeys);
        setKeyValue(p, strKeyword + " " + strSaveLine + " " + strSubRec);
        numSubRecAdded++;
      }
      else
      {
        Log.debugLog(className, "addKeywordSubRecord", "", "", "Key did not match. Keeping the record as is for  " + strKeyword + " key = " + strRecordsKeys);
        setKeyValue(p, strKeyword + " " + strSaveLine);
      }
    }
    if(numSubRecAdded == 0)
    {
      // Currently it is assuming one felds as key - To be enhanced later
      if(arrSrcFldVal.length > 1)
        setKeyValue(p, strKeyword + " " + arrSrcFldVal[0] + " " + arrSrcFldVal[1] + " " + strSubRec);
      else
        setKeyValue(p, strKeyword + " " + arrSrcFldVal[0] + " " + strSubRec);
    }
    return (numSubRecAdded);
  }

  /** 
   * This method is to get default scenario mode in case of scenario mode is not set in scenario file.
   * Currently this method is used in Runtime Changes GUI.
   * 
   * @param scenarioType - This is set in RTC GUI, at time of loading RTC GUI
   * @param scenarioMode - In this value from sorted properties is passed, 
   *        If it is null then value is assign as per scenario type, in this method.
   *        Else if it is not null and scenario type is 'NUM_AUTO' then it is assign as 'NUM' i.e. by number mode. 
   * 
   */
  public void setDistributionMode(String scenarioType, String scenarioMode)
  {
    Log.debugLog(className, "setDistributionMode", "", "", "Method Starts. Scenario Type: " + scenarioType + ", Scenario Mode: " + scenarioMode);
    
    //final String STR_FCU = "FIX_CONCURRENT_USERS"; // this is default value for STYPE
    final String STR_FSR = "FIX_SESSION_RATE";
    final String STR_PROF_PCT_NUM_MODE = "NUM"; // this is default value for PROF_PCT_MODE, in case of FSR and Mixed Mode.
    final String STR_PROF_PCT_PERC_MODE = "PCT"; // this is percentage mode for PROF_PCT_MODE
    final String STR_PROF_PCT_NUM_AUTO_MODE = "NUM_AUTO";
    
    if(scenarioMode == null)
    {
      if(scenarioType.equals(STR_FSR))
        this.scenarioMode = STR_PROF_PCT_PERC_MODE;
      else
        this.scenarioMode = STR_PROF_PCT_NUM_MODE;
    }
    else
    {
      //this is to set in case of Runtime Changes Users/Sessions in Execution GUI, as in that NUM_AUTO and NUM is consider as same scenario mode.
      if(scenarioMode.equals(STR_PROF_PCT_NUM_AUTO_MODE))
        this.scenarioMode = STR_PROF_PCT_NUM_MODE;
      else
        this.scenarioMode = scenarioMode;
    }
  
    Log.debugLog(className, "setDistributionMode", "", "", "Method Ends. Scenario Mode: " + scenarioMode);
  }

  public String getDistributionMode()
  {
    return scenarioMode;
  }
  
  // Name: getKeywordRecordFields
  // Purpose: Get all fields of keywords of key1 (source).
  // Arguments:
  //  Arg1: Properties with all keywords.
  //  Arg2: keyword
  //  Arg3: "" or List of Field Id which are to compared with source fields..
  //        It must be in the sorted order e.g. "0|3|5"
  //  Arg4: "" or Source key value list for keyword records which are to be returned.
  // Return: null if erorr else 2D array

  // Example: To get all UPLOCATION keyword record fields whose first
  //          field is Neeraj, pass following arguments:
  //   getKeywordRecordFields(p, "UPLOCATION", "0", "Neeraj")
  //
  public String [][] getKeywordRecordFields(Properties p, String strKeyword, String strFldIdList, String strSrcFldValList)
  {
    return(getKeywordRecordFields(p, strKeyword, strFldIdList, strSrcFldValList, null));
  }

  public String [][] getKeywordRecordFields(Properties p, String strKeyword, String strFldIdList, String strSrcFldValList, Vector vecStatus)
  {
    Log.debugLog(className, "getKeywordRecordFields", "", "", "Keyword = " + strKeyword + ", FieldIdList = " + strFldIdList + ", SrcFldValList = " + strSrcFldValList);

    String arrFldId[] = split(strFldIdList, "|");
    String arrSrcFldVal[] = split(strSrcFldValList, "|");
    int recIdx = 0, i = 0, j = 0;

    if(arrFldId.length != arrSrcFldVal.length)

    {
      Log.errorLog(className, "getKeywordRecordFields", "", "", "Number of field Ids are not correct. Length = " + arrFldId.length);
      return null;
    }

    String strKeywordVal = p.getProperty(strKeyword);
    Log.debugLog(className, "getKeywordRecordFields", "", "", "Keyword Value for  " + strKeyword + " = " + strKeywordVal);
    if((strKeywordVal == null) || strKeywordVal.equals(""))
    {
      Log.errorLog(className, "getKeywordRecordFields", "", "", "Source keyword does not exist. Keyword = " + strKeyword);
      return null;
    }

    Object arrVendorKeywords[] = null;
    if(vecStatus != null)
    {
      String vendorKeywordFile  = Config.getValueWithPath("vendorKeywordFile");
      arrVendorKeywords = getFileData(vendorKeywordFile);
    }

    StringTokenizer stRecords = new StringTokenizer(strKeywordVal, "|");
    String arrRecordFlds[][] = new String[stRecords.countTokens()][];

    while(stRecords.hasMoreTokens())
    {
      String strLine = stRecords.nextToken().trim();
      String strSaveLine = new String(strLine);
      Log.debugLog(className, "getKeywordRecordFields", "", "", "Checking for keyword = " + strKeyword + " " + strLine);
      StringTokenizer stFlds = new StringTokenizer(strLine);

      int fldIdx = 0;
      String arrFlds[] = new String[stFlds.countTokens()];
      while(stFlds.hasMoreTokens())
        arrFlds[fldIdx++] = stFlds.nextToken().trim();

      String strRecordsKeys = "";
      for(i = 0; i < arrFldId.length; i++)
      {
        int fldId = Integer.parseInt(arrFldId[i]);
        if(strRecordsKeys.equals(""))
          strRecordsKeys = arrFlds[fldId];
        else
          strRecordsKeys = strRecordsKeys + "|" + arrFlds[fldId];
      }

      if(strRecordsKeys.equals("") || strRecordsKeys.equals(strSrcFldValList))
      {
        Log.debugLog(className, "getKeywordRecordFields", "", "", "Keyword record found for  " + strKeyword + " key = " + strRecordsKeys);
        arrRecordFlds[recIdx] = new String[arrFlds.length];
        for(i = 0; i < arrFlds.length; i++)
          arrRecordFlds[recIdx][i] = arrFlds[i];
        recIdx++;

        if(vecStatus != null)
        {
          if(Arrays.binarySearch(arrVendorKeywords, strKeyword + " " + strSaveLine) < 0)
            vecStatus.add(new String("readWrite"));
          else
            vecStatus.add(new String("readOnly"));
        }
      }
      else
      {
        Log.debugLog(className, "getKeywordRecordFields", "", "", "Key did not match. Keyword ignored for  " + strKeyword + " key = " + strRecordsKeys);
      }
    }
    // Since we are only storing matched reocrds,
    // all array may not have data. So create new array.
    String arrRecordFldsFinal[][] = new String[recIdx][];
    for(i = 0; i < recIdx; i++)
    {
      arrRecordFldsFinal[i] = new String[arrRecordFlds[i].length];
      for(j = 0; j < arrRecordFlds[i].length; j++)
      {
        arrRecordFldsFinal[i][j] = arrRecordFlds[i][j];
        Log.debugLog(className, "getKeywordRecordFields", "", "", "Field returned:  " + arrRecordFldsFinal[i][j]);
      }
    }
    return(arrRecordFldsFinal);
  }


  // Name: getKeywordRecordOneField
  // Purpose: Get one field (unique) of keywords of key1 (source).
  // Arguments:
  //  Arg1: Properties with all keywords.
  //  Arg2: keyword
  //  Arg3: "" or List of Field Id which are to compared with source fields..
  //        It must be in the sorted order e.g. "0|3|5"
  //  Arg4: "" or Source key value list for keyword records which are to be returned.
  //  Arg5: Vector for getting status of keyword
  //          - readOnly - Keyword came from Vendor file and cannot be changed
  //          - readWrite - Keyword can be changed

  // Return: null if erorr else 2D array

  // Example: To get all UPLOCATION keyword record fields whose first
  //          field is Neeraj, pass following arguments:
  //   getKeywordRecordOneField(p, "UPLOCATION", "0", "Neeraj")
  //

  public String [] getKeywordRecordOneField(Properties p, String strKeywordList, String strFldIdList, String strSrcFldValList, String strFldId)
  {
    return(getKeywordRecordOneField(p, strKeywordList, strFldIdList, strSrcFldValList, strFldId, null));
  }

  public String [] getKeywordRecordOneField(Properties p, String strKeywordList, String strFldIdList, String strSrcFldValList, String strFldId, Vector vecStatus)
  {
    Log.debugLog(className, "getKeywordRecordOneField", "", "", "KeywordList = " + strKeywordList + ", FieldIdList = " + strFldIdList + ", SrcFldValList = " + strSrcFldValList + ", FeldId = " + strFldId);

    String arrFldId[] = split(strFldIdList, "|");
    String arrSrcFldVal[] = split(strSrcFldValList, "|");
    int i = 0;
    int numFldId = Integer.parseInt(strFldId);

    Vector vecFld = new Vector();  // Vector is used to store only unique values.

    if(arrFldId.length != arrSrcFldVal.length)
    {
      Log.errorLog(className, "getKeywordRecordOneField", "", "", "Number of field Ids are not correct. Length = " + arrFldId.length);
      return null;
    }

    Object arrVendorKeywords[] = null;
    if(vecStatus != null)
    {
      String vendorKeywordFile  = Config.getValueWithPath("vendorKeywordFile");
      arrVendorKeywords = getFileData(vendorKeywordFile);
    }

    StringTokenizer stKW = new StringTokenizer(strKeywordList, "|");
    //it will iterate to the number of keyword pass as args as '|' separated
    while(stKW.hasMoreTokens())
    {
      String strKeyword = stKW.nextToken().trim();

      String strKeywordVal = p.getProperty(strKeyword);
//      Log.debugLog(className, "getKeywordRecordOneField", "", "", "Keyword Value for  " + strKeyword + " = " + strKeywordVal);
      if((strKeywordVal == null) || strKeywordVal.equals(""))
      {
//        Log.debugLog(className, "getKeywordRecordOneField", "", "", "Source keyword does not exist. Keyword = " + strKeyword);
//        return null;
        continue;
      }
      StringTokenizer stRecords = new StringTokenizer(strKeywordVal, "|");


      //it will iterate to the number of keyword value as '|' separated
      while(stRecords.hasMoreTokens())
      {
        String strLine = stRecords.nextToken().trim();
        String strSaveLine = new String(strLine);
//        Log.debugLog(className, "getKeywordRecordOneField", "", "", "Checking for keyword = " + strKeyword + " " + strLine);
        StringTokenizer stFlds = new StringTokenizer(strLine);

        int fldIdx = 0;
        //This will contain all the fields of each keywords value
        String arrFlds[] = new String[stFlds.countTokens()];
        while(stFlds.hasMoreTokens())
          arrFlds[fldIdx++] = stFlds.nextToken().trim();

        //This will have the same format of string to match with 'strSrcFldValList' as '|' separated
        String strRecordsKeys = "";
        for(i = 0; i < arrFldId.length; i++)
        {
          int fldId = Integer.parseInt(arrFldId[i]);
          if(strRecordsKeys.equals(""))
            strRecordsKeys = arrFlds[fldId];
          else
            strRecordsKeys = strRecordsKeys + "|" + arrFlds[fldId];
        }

        if(strRecordsKeys.equals("") || strRecordsKeys.equals(strSrcFldValList))
        {
//          Log.debugLog(className, "getKeywordRecordOneField", "", "", "Keyword record found for  " + strKeyword + " key = " + strRecordsKeys);
          if(!vecFld.contains(arrFlds[numFldId]))  // Add if not already there
          {
            vecFld.addElement(arrFlds[numFldId]);
            if(vecStatus != null)
            {
              if(Arrays.binarySearch(arrVendorKeywords, strKeyword + " " + strSaveLine) < 0)
                vecStatus.add(new String("readWrite"));
              else
                vecStatus.add(new String("readOnly"));
            }
          }
        }
        else
        {
//          Log.debugLog(className, "getKeywordRecordOneField", "", "", "Key did not match. Keyword ignored for  " + strKeyword + " key = " + strRecordsKeys);
        }
      }
    }
    Object arrStrTmp[] = new Object[vecFld.size()];
    vecFld.copyInto(arrStrTmp);
    // DO not sort as vecStatus gets out of sync.
//    Arrays.sort(arrStrTmp);
    return(objArrToStrArr(arrStrTmp));
  }

  /**
   * This method will return the match count in the property 'p' for that keyword's
   * key fields.
   *
   * This match will be check on the basis on of key fields, from that key fields
   * a '|' separated string will be created from the @param 'keywordNameWithValue'.
   *
   * Then this string will be match with every value of that keyword define in
   * property 'p', if that keyword not found in the 'p' then it will return
   * '0' (zero).
   *
   * @param p
   * @param keywordName
   * @param keywordNameWithValue
   * @return match count
   */
  private int getKeywordFieldsMatchCount(Properties p, String keywordName, String keywordNameWithValue)
  {
    //Log.debugLog(className, "getKeywordFieldsMatchCount", "", "", "Method Called. keywordName = " + keywordName + ", keywordNameWithValue = " + keywordNameWithValue);
    try
    {
      KeywordDefinition.Keywords keywordObj = (KeywordDefinition.Keywords)keywordDefinition.getHashForKeywords().get(keywordName);

      String strFldIdList = "";
      String strSrcFldValList = "";

      boolean isGroupBased = keywordObj.getIsGroupBased();
      boolean isScalar = keywordObj.getType() == KeywordDefinition.Keywords.SCALAR;
      String keyFields = keywordObj.getKeyFields();

      if(isScalar)//scalar
      {
        //Log.debugLog(className, "getKeywordFieldsMatchCount", "", "", "Scalar keyword = " + keywordName);
        if(isGroupBased)//Group based scalar
        {
          //Log.debugLog(className, "getKeywordFieldsMatchCount", "", "", "Group based scalar keyword = " + keywordName);

          strFldIdList = "0";

          //It may be ALL OR any group name
          strSrcFldValList = getStrFmtToMatch(keywordNameWithValue, "0");
        }
      }
      else//Group Based OR Non-Group based Vector
      {
        //Log.debugLog(className, "getKeywordFieldsMatchCount", "", "", "Vector keyword = " + keywordName + ", isGroupBased = " + isGroupBased);

        //If keywords is vector and key fields are not given, then do not check for duplicate
        if((keyFields.equals("")) || (keyFields.equals("-")))//Key Fields not found for that keyword
        {
          //Log.debugLog(className, "getKeywordFieldsMatchCount", "", "", "KeyFields not found for the Vector keyword = " + keywordName + ", isGroupBased = " + isGroupBased + ", returning 0(zero) to add in the property");
          return 0;
        }
        else//Key Fields found for that keyword
        {
          strSrcFldValList = getStrFmtToMatch(keywordNameWithValue, keyFields);
          strFldIdList = replace(keyFields, ",", "|");
        }//End of else condition for key field found
      }//End of else condition for vector

      //Log.debugLog(className, "getKeywordFieldsMatchCount", "", "", "strFldIdList = " + strFldIdList + ", strSrcFldValList = " + strSrcFldValList);

      String arrFldId[] = split(strFldIdList, "|");
      String arrSrcFldVal[] = split(strSrcFldValList, "|");

      if(arrFldId.length != arrSrcFldVal.length)
      {
        Log.errorLog(className, "getKeywordFieldsMatchCount", "", "", "Number of field Ids are not correct. Length = " + arrFldId.length);
        return -1;
      }

      String strKeywordVal = p.getProperty(keywordName);

      //Log.debugLog(className, "getKeywordFieldsMatchCount", "", "", "Keyword Value for  " + keywordName + " from property p = " + strKeywordVal);

      if((strKeywordVal == null) || strKeywordVal.equals(""))
      {
      //  Log.debugLog(className, "getKeywordFieldsMatchCount", "", "", "Source keyword does not exist in the property. Keyword = " + keywordName);
        return 0;
      }

      StringTokenizer stRecords = new StringTokenizer(strKeywordVal, "|");

      int countMatch = 0;

      //It will iterate to the number of keyword value as '|' separated
      while(stRecords.hasMoreTokens())
      {
        String strLine = stRecords.nextToken().trim();
      //  Log.debugLog(className, "getKeywordFieldsMatchCount", "", "", "Checking for keyword fetched from property = " + keywordName + " " + strLine);
        StringTokenizer stFlds = new StringTokenizer(strLine);

        int fldIdx = 0;

        //This will contain all the fields of each keywords value
        String arrFlds[] = new String[stFlds.countTokens()];
        while(stFlds.hasMoreTokens())
          arrFlds[fldIdx++] = stFlds.nextToken().trim();

        //This will have the same format of string to match with 'strSrcFldValList' as '|' separated
        String strRecordsKeys = "";
        for(int i = 0; i < arrFldId.length; i++)
        {
          int fldId = Integer.parseInt(arrFldId[i]);
          if(strRecordsKeys.equals(""))
            strRecordsKeys = arrFlds[fldId];
          else
            strRecordsKeys = strRecordsKeys + "|" + arrFlds[fldId];
        }

      //  Log.debugLog(className, "getKeywordFieldsMatchCount", "", "", "Matching of strRecordsKeys from property = " + strRecordsKeys + ", with strSrcFldValList from keywordNameWithValue = " + strSrcFldValList);

        if(strRecordsKeys.equals(strSrcFldValList))
        {
          if(countMatch >= 1)
            Log.errorLog(className, "getKeywordFieldsMatchCount", "", "", "Duplicate value for keyword = " + keywordName + ", for " + strLine);
          countMatch++;
        }
        else
          Log.debugLog(className, "getKeywordFieldsMatchCount", "", "", "Key did not match. Keyword ignored for  " + keywordName + " key = " + strRecordsKeys);

      }
      return countMatch;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getKeywordFieldsMatchCount", "", "", "Exception in getKeywordFieldsMatchCount() method", e);
      return -1;
    }
  }

  /**
   This method will return string of the keyword value of specific key
   field with pipe separated.

   As example:

   "G_KA_PCT ALL 70" for the key field 0,1
   then it will return "ALL|70"

   and if "G_PAGE_THINK_TIME ALL ALL 0" for key field 0,1
   then it will return "ALL|ALL"

   */
  private String getStrFmtToMatch(String keywordWithValue, String keyfields)
  {
    Log.debugLog(className, "getStrFmtToMatch", "", "", "Method called. keywordWithValue = " + keywordWithValue + ", keyFields = " + keyfields);
    try
    {
      String strArrKeyword[] = rptUtilsBean.split(keywordWithValue, " ");
      String strArrKeyFields[] = rptUtilsBean.split(keyfields, ",");
      String str = "";

      //At index '0' will be keyword name itself so adding '1'
      for(int i = 0 ; i < strArrKeyFields.length ; i++)
      {
        if(str.equals(""))
          str = strArrKeyword[Integer.parseInt(strArrKeyFields[i]) + 1];
        else
          str = str + "|" + strArrKeyword[Integer.parseInt(strArrKeyFields[i]) + 1];
      }
      return str;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getStrFmtToMatch", "", "", "Exception in getStrFmtToMatch() method", e);
      return "";

    }
  }

  // Name: getKeywordFields
  // Purpose: Get all fields of keywords which is scalar (only one record)
  // Arguments:
  //  Arg1: Properties with all keywords.
  //  Arg2: keyword
  //  Arg3 to 12 :
  // Return: -1 if error else number of feilds pouplated

  // Example: To get all fields of RUN_TIME keyword, pass following arguments:
  //   getKeywordFields(p, "RUN_TIME", strRunTimeValue, strRunTimeUnit, null, null, null, null, null, null, null, null);
  //

  public int getKeywordFields(Properties p, String strKeyword, StringBuffer strFld1, StringBuffer strFld2, StringBuffer strFld3, StringBuffer strFld4, StringBuffer strFld5)
  {
    return(getKeywordFields(p, strKeyword, strFld1, strFld2, strFld3, strFld4, strFld5, null, null, null, null, null));
  }

  public int getKeywordFields(Properties p, String strKeyword, StringBuffer strFld1, StringBuffer strFld2, StringBuffer strFld3, StringBuffer strFld4, StringBuffer strFld5, StringBuffer strFld6, StringBuffer strFld7, StringBuffer strFld8, StringBuffer strFld9, StringBuffer strFld10)
  {
    Log.debugLog(className, "getKeywordFields", "", "", "Keyword = " + strKeyword);

    int i = 0, j = 0;

    String strKeywordVal = p.getProperty(strKeyword);
    Log.debugLog(className, "getKeywordFields", "", "", "Keyword Value for  " + strKeyword + " = " + strKeywordVal);
    if((strKeywordVal == null) || strKeywordVal.equals(""))
    {
      Log.debugLog(className, "getKeywordFields", "", "", "Source keyword does not exist. Keyword = " + strKeyword);
      return 0;
    }
    StringTokenizer stRecords = new StringTokenizer(strKeywordVal, "|");
    if(stRecords.countTokens() > 1)
    {
      Log.errorLog(className, "getKeywordFields", "", "", "Keyword is not scalar. Keyword = " + strKeyword);
      return -1;
    }
    String strLine = stRecords.nextToken().trim();
    StringTokenizer stFlds = new StringTokenizer(strLine);
    int numFlds = stFlds.countTokens();

    copyKeywordFld(stFlds, strFld1);
    copyKeywordFld(stFlds, strFld2);
    copyKeywordFld(stFlds, strFld3);
    copyKeywordFld(stFlds, strFld4);
    copyKeywordFld(stFlds, strFld5);
    copyKeywordFld(stFlds, strFld6);
    copyKeywordFld(stFlds, strFld7);
    copyKeywordFld(stFlds, strFld8);
    copyKeywordFld(stFlds, strFld9);
    copyKeywordFld(stFlds, strFld10);

    return(numFlds);
  }

  private void copyKeywordFld(StringTokenizer st, StringBuffer strFld)
  {
    if(st.hasMoreTokens())
    {
      if(strFld != null)
      {
        strFld.delete(0, strFld.length());
        strFld.append(st.nextToken().trim());
      }
    }
  }


  // Name: updateKeywordFields
  // Purpose: Update all fields of keywords which is scalar (only one record)
  // Arguments:
  //  Arg1: Properties with all keywords.
  //  Arg2: keyword
  //  Arg3 to 12 :
  // Return: -1 if error else 1

  // Example: To get all fields of RUN_TIME keyword, pass following arguments:
  //   updateKeywordFields(p, "RUN_TIME", strRunTimeValue, strRunTimeUnit, null, null, null, null, null, null, null, null);
  //

  public int updateKeywordFields(Properties p, String strKeyword, String strFld1, String strFld2, String strFld3, String strFld4, String strFld5)
  {
    return(updateKeywordFields(p, strKeyword, strFld1, strFld2, strFld3, strFld4, strFld5, null, null, null, null, null));
  }

  public int updateKeywordFields(Properties p, String strKeyword, String strFld1, String strFld2, String strFld3, String strFld4, String strFld5, String strFld6, String strFld7, String strFld8, String strFld9, String strFld10)
  {
    Log.debugLog(className, "updateKeywordFields", "", "", "Keyword = " + strKeyword);

    String strKeywordVal = p.getProperty(strKeyword);
    Log.debugLog(className, "updateKeywordFields", "", "", "Keyword Value before update = " + strKeyword + " = " + strKeywordVal);

    if((strKeywordVal != null) && !strKeywordVal.equals(""))
    {
      StringTokenizer stRecords = new StringTokenizer(strKeywordVal, "|");
      if(stRecords.countTokens() > 1)
      {
        Log.errorLog(className, "updateKeywordFields", "", "", "Keyword is not scalar. Keyword = " + strKeyword);
        return -1;
      }
    }

    if(strFld1 != null) strKeywordVal = strFld1;
    if(strFld2 != null) strKeywordVal = strKeywordVal + " " + strFld2;
    if(strFld3 != null) strKeywordVal = strKeywordVal + " " + strFld3;
    if(strFld4 != null) strKeywordVal = strKeywordVal + " " + strFld4;
    if(strFld5 != null) strKeywordVal = strKeywordVal + " " + strFld5;
    if(strFld6 != null) strKeywordVal = strKeywordVal + " " + strFld6;
    if(strFld7 != null) strKeywordVal = strKeywordVal + " " + strFld7;
    if(strFld8 != null) strKeywordVal = strKeywordVal + " " + strFld8;
    if(strFld9 != null) strKeywordVal = strKeywordVal + " " + strFld9;
    if(strFld10 != null) strKeywordVal = strKeywordVal + " " + strFld10;

    Log.debugLog(className, "updateKeywordFields", "", "", "Keyword Value after update = " + strKeyword + " = " + strKeywordVal);

    if(EDIT_MODE_FLAG || DEFAULT_SETTING_MODE_FLAG)
    {
      clearKeywordValue(p, strKeyword);
      setKeyValue(p, strKeyword + " " + strKeywordVal);
    }
    else if(RUNTIMECHANGE_MODE_FLAG)
    {
      if(!updateKeywordAtRunTime(p, strKeyword, "", strKeywordVal, SCALAR))
        return -1;
    }
    return(1);
  }


  /**
   This method is NOT USED presently because now scenario are fetched from "nsu_show_scenario"
   command.


  // this function return the array of scenario Name available in particular $NS_WDIR
  public String[] getScenarioName()
  {
    Log.debugLog(className, "getScenarioName", "", "", "Method Started");

    try
    {
      // Get list of existing Scenario File Names
      String[] arrScenFileNames = null;
      File file = new File(Config.getValueWithPath("scenarioPath"));
      String files[] = file.list();  // This give all files in the scenario directory
      // code for sorting and to filter non .conf files
      TreeSet ts = new TreeSet();
      for(int i = 0; i < files.length; i++)
      {
        String fileExt; // File extension
        int len, len1;
        len = files[i].lastIndexOf(".");
        if(len != -1)
        {
          len1 = files[i].length();
          fileExt = files[i].substring(len + 1, len1);
          if(fileExt.equals("conf"))
            ts.add(files[i]);
        }
      }

      arrScenFileNames = new String[ts.size()];
      Iterator it = ts.iterator();
      int i = 0;
      while(it.hasNext())
      {
        arrScenFileNames[i] = (String)it.next();
        Log.debugLog(className, "getScenarioName", "", "", "Scenario Name added to list = " + arrScenFileNames[i]);
        i++;
      }
      return arrScenFileNames;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getAllCustomGDF", "", "", "Exception - ", e);
      return null;
    }
  }*/


  private void copyKeywordFldFromArray(String strSrc, StringBuffer strFld)
  {
    if(strFld != null)
    {
      strFld.delete(0, strFld.length());
      strFld.append(strSrc.trim());
    }
  }

  public int getGroupBasedKeywordFields(Properties p, String strKeyword, String groupName, StringBuffer strFld1, StringBuffer strFld2, StringBuffer strFld3, StringBuffer strFld4, StringBuffer strFld5, StringBuffer strFld6, StringBuffer strFld7, StringBuffer strFld8, StringBuffer strFld9, StringBuffer strFld10)
  {
    Log.debugLog(className, "getGroupBasedKeywordFields", "", "", "Method called. Keyword = " + strKeyword + ", grpupName = " + groupName);

    String arrTmpKwFields[][] = getKeywordRecordFields(p, strKeyword, "0", groupName);

    if((arrTmpKwFields == null) || (arrTmpKwFields.length == 0))
    {
      Log.debugLog(className, "getGroupBasedKeywordFields", "", "", "Not records found for Keyword = " + strKeyword + " for group " + groupName);

      if(groupName.equals("ALL"))
      {
        Log.errorLog(className, "getGroupBasedKeywordFields", "", "", "Records not found for even for ALL Keyword = " + strKeyword + " for group " + groupName);
        return 0;
      }
      else
      {
        Log.debugLog(className, "getGroupBasedKeywordFields", "", "", "Not records found for specific group Keyword = " + strKeyword + " for group " + groupName + ", getting value for ALL groups");
        return getGroupBasedKeywordFields(p, strKeyword, "ALL", strFld1, strFld2, strFld3, strFld4, strFld5, strFld6, strFld7, strFld8, strFld9, strFld10);
      }
    }

    if(arrTmpKwFields.length > 1)
    {
      Log.debugLog(className, "getGroupBasedKeywordFields", "", "", "More than one records found for Keyword = " + strKeyword + " for group " + groupName);
      return -1;
    }

    int i = 1, numFlds = arrTmpKwFields[0].length;  // numFlds also includes group name so start i from 1

    if(i < numFlds) copyKeywordFldFromArray(arrTmpKwFields[0][i++], strFld1);
    if(i < numFlds) copyKeywordFldFromArray(arrTmpKwFields[0][i++], strFld2);
    if(i < numFlds) copyKeywordFldFromArray(arrTmpKwFields[0][i++], strFld3);
    if(i < numFlds) copyKeywordFldFromArray(arrTmpKwFields[0][i++], strFld4);
    if(i < numFlds) copyKeywordFldFromArray(arrTmpKwFields[0][i++], strFld5);
    if(i < numFlds) copyKeywordFldFromArray(arrTmpKwFields[0][i++], strFld6);
    if(i < numFlds) copyKeywordFldFromArray(arrTmpKwFields[0][i++], strFld7);
    if(i < numFlds) copyKeywordFldFromArray(arrTmpKwFields[0][i++], strFld8);
    if(i < numFlds) copyKeywordFldFromArray(arrTmpKwFields[0][i++], strFld9);
    if(i < numFlds) copyKeywordFldFromArray(arrTmpKwFields[0][i++], strFld10);

    return(i - 1);
  }

  private boolean checkWholeKeywordMatch(Properties p, String keywordName, String groupName, String keywordValue)
  {
    String val = p.getProperty(keywordName);
    //replace space with pipe
    String pipeVal = rptUtilsBean.replace(val, " ", "|");
    //String may be g2|32|ALL|0
    if(!groupName.equals(""))
      pipeVal = pipeVal.substring(pipeVal.indexOf(groupName));

    String pipeCurrentVal = rptUtilsBean.replace(keywordValue, " ", "|");

    String arrVal[] = rptUtilsBean.split(pipeVal, "|");
    String arrCurrentVal[] = rptUtilsBean.split(pipeCurrentVal, "|");

    int length = arrVal.length;
    if(arrVal.length > arrCurrentVal.length)
      length = arrCurrentVal.length;

    int count = 0;
    for(int i = 0; i < length; i++)
    {
      if(arrCurrentVal[i].equals(arrVal[i]))
        count++;
    }
    if(count == length)
      return true;

    return false;
  }

  public boolean updateGroupBasedKeywordFields(Properties p, String strKeyword, String strKeywordVal, String groupName)
  {
    Log.debugLog(className, "updateGroupBasedKeywordFields", "", "", "Method called. Keyword = " + strKeyword + ", grpupName = " + groupName + ", keyword value = " + strKeywordVal);
    if(EDIT_MODE_FLAG || DEFAULT_SETTING_MODE_FLAG)
    {
      // Modify by delete and then add
      deleteKeywordRecords(p, strKeyword, "0", groupName);
      addKeywordRecord(p, strKeyword, groupName + " " + strKeywordVal);
      Log.debugLog(className, "updateGroupBasedKeywordFields", "", "", "After updating the Keyword = " + strKeyword + ", Value of keyword = " + p.getProperty(strKeyword));
    }
    else if(RUNTIMECHANGE_MODE_FLAG)
    {
      //In run time mode All will not update
      if(groupName.equals("ALL"))
        return true;

      updateKeywordAtRunTime(p, strKeyword, groupName, strKeywordVal, SCALAR);
    }

    return true;
  }

  private boolean updateKeywordAtRunTime(Properties p, String strKeyword, String groupName, String strKeywordVal, String type)
  {

  //checking keyword is runtime changeable or not
    if(isRunTimeChangableKeyword(p, strKeyword))
    {
      Log.debugLog(className, "updateKeywordAtRunTime", "", "", "Run time changeable. Keyword = " + strKeyword + ", grpupName = " + groupName + ", keyword value = " + strKeywordVal);
      String keywordNameWithValue = strKeyword + " " + groupName + " " + strKeywordVal;
      //checking count to keyord is present in the property or not
      //this method only checking keyfields
      int count = getKeywordFieldsMatchCount(p, strKeyword, keywordNameWithValue);

      if((count == 0) || (count == 1))
      {
        
        if(count == 1)
        {
          //checking each field
          if(checkWholeKeywordMatch(p, strKeyword, groupName, groupName + " " + strKeywordVal))
            return true;
          else// delete from the property
          {
            if(type.equals(SCALAR))
            {
              if(groupName.equals(""))
                clearKeywordValue(p, strKeywordVal);
              else
                deleteKeywordRecords(p, strKeyword, "0", groupName);
            }
          }
        }
        else
        {
          //case1 :- If keyword is not exists for group it will not add default value in the file
          //case2 :- If user update default value it will add in the file
          //case3 :- If user update updated value of keyword it will add default value in run time
          //         Possibility apply changes  with updated value for first 2 session and rest of the session user apply default value
          if(!checkToWriteGroupBasedKeyword(strKeyword, groupName + " " + strKeywordVal, p.getProperty(strKeyword)))
           return true;
        }
        if(type.equals(VECTOR))
          setKeyValue(p, keywordNameWithValue, false);
        else
        {
          //updating keyword
          setKeyValue(p, keywordNameWithValue);
        }

        //updating run time file
        if(!updateRunTimeChangableFile(groupName, keywordNameWithValue, ADD_OPERATION))
          return false;
      }
      else
        Log.debugLog(className, "updateKeywordAtRunTime", "", "", "keyword = " + strKeyword + " with value already found in the scenario file");
    }
    return true;
  }

  public String [][] getScenGroupInfo(Properties p, String strKeywordList)
  {
    return getScenGroupInfo(p, strKeywordList, "", false);
  }
  
  // Purpose: To return 2D array for all groups names and flag if keyword requested are present
  //          2D Array 0th element will be group name and 1st element is "Yes" or "No"
  // Arguments:
  //  strKeywordList: Pipe separated list of keywords to be checked.
  //                  Example: "G_KA_PCT|G_NUM_KA"
  //
  //group name to create
  //operation first time create tab
  //
  public String [][] getScenGroupInfo(Properties p, String strKeywordList, String groupName, boolean isAddGroup)
  {

    Log.debugLog(className, "getScenGroupInfo", "", "", "Method called. KestrKeywordList = " + strKeywordList);

    // Get Scenario Group Names in arrScenGroupNames
    String arrScenGroupNames[] = getKeywordRecordOneField(p, "SGRP", "", "", "0");
    if(arrScenGroupNames == null)
    {
      Log.debugLog(className, "getScenGroupInfo", "", "", "No scenario groups defined in the scenario");
      return null;
    }

    String arrScenGroupInfo[][] = new String[arrScenGroupNames.length][2];

    // Now check for which groups, requested keywords are present

    for(int groupNum = 0; groupNum < arrScenGroupNames.length; groupNum++)
    {
      arrScenGroupInfo[groupNum][0] = arrScenGroupNames[groupNum];
      
      //when user add group to 
      /**if(operation.equals("addGroupTab"))
      {
        if(groupName.equals(arrScenGroupInfo[groupNum][0]))
          arrScenGroupInfo[groupNum][1] = "Yes";
        else
          arrScenGroupInfo[groupNum][1] = "No";
        continue;
      }**/
        
      // Check if keyword exists for the group
      String arrTmp[] = getKeywordRecordOneField(p, strKeywordList, "0", arrScenGroupNames[groupNum], "0");

      if(((arrTmp != null) && (arrTmp.length > 0)) || (isAddGroup && groupName.equals(arrScenGroupInfo[groupNum][0])))
      {
        Log.debugLog(className, "getScenGroupInfo", "", "", strKeywordList + " are present for group - " + arrScenGroupInfo[groupNum][0]);
        arrScenGroupInfo[groupNum][1] = "Yes";
      }
      else
      {
        Log.debugLog(className, "getScenGroupInfo", "", "", strKeywordList + " are not present for group - " + arrScenGroupInfo[groupNum][0]);
        arrScenGroupInfo[groupNum][1] = "No";
      }
    }
    return(arrScenGroupInfo);
  }

  /*
   * P : property
   * Keyword list
   * This method give the information of the SGRP of selected field
   * Output :- GroupName|Script Type|Script Name
   */
  public Object[] getScenGroupScriptInfo(Properties p, String strKeywordList, LinkedHashMap linkedHashMap)
  {
    Log.debugLog(className, "getScenGroupScriptInfo", "", "", "Method called. strKeywordList = " + strKeywordList);

    try
    {
      Object[] objArr = new Object[2];
      long startProcessingTime = System.currentTimeMillis();
      // Get Scenario Group Names in arrScenGroupNames
      //Group name - 0, script type - 4 and script/URL Name - 5
      String[][] arrSGRPRecordFlds = getKeywordRecordFields(p, strKeywordList, "4", "0");
      if(arrSGRPRecordFlds == null)
      {
        Log.debugLog(className, "getScenGroupScriptInfo", "", "", "No scenario groups defined in the scenario");
        return null;
      }
      
      //Skipping script URL based
      for(int i = 0; i < arrSGRPRecordFlds.length; i++)
      {
        //Script name is key and it is already present
        if(linkedHashMap.containsKey(arrSGRPRecordFlds[i][5]))
        {
          ScenarioTPSSyncData scenarioTPSSyncData = (ScenarioTPSSyncData) linkedHashMap.get(arrSGRPRecordFlds[i][5]);
          if(scenarioTPSSyncData.getIsScriptDeleteOrNot() != 0)
            scenarioTPSSyncData.setIsScriptDeleteOrNot(1);//means in use
          //getting group list
          LinkedHashMap grpLinkedHashMap = scenarioTPSSyncData.getGrpLinkedHashMap();
          //group name is not present means group is deleted
          //no need to check is exist or not means new group added or old grp is present
          //mark as true means group in use
          //if(!grpLinkedHashMap.containsKey(arrSGRPRecordFlds[i][0]))
          {
            //means new group is added or old group 
            //mark true means in use 
            grpLinkedHashMap.put(arrSGRPRecordFlds[i][0], true);
          }
          
          scenarioTPSSyncData.setGrpLinkedHashMap(grpLinkedHashMap);
          
          linkedHashMap.put(arrSGRPRecordFlds[i][5], scenarioTPSSyncData);
        }
        //means new script is entered
        else
        {
          ScenarioTPSSyncData scenarioTPSSyncData = new ScenarioTPSSyncData();
          LinkedHashMap grpLinkedHashMap = scenarioTPSSyncData.getGrpLinkedHashMap();
          grpLinkedHashMap.put(arrSGRPRecordFlds[i][0], true); //add group of this script
          scenarioTPSSyncData.setGrpLinkedHashMap(grpLinkedHashMap);
          scenarioTPSSyncData.setIsScriptDeleteOrNot(0); //means in use
          linkedHashMap.put(arrSGRPRecordFlds[i][5], scenarioTPSSyncData);
        }
      }
      
      //For Testing
      /**Iterator it = linkedHashMap.entrySet().iterator();
      while (it.hasNext()) 
      {
        Map.Entry pairs = (Map.Entry)it.next();
          
        ScenarioTPSSyncData syncData = (ScenarioTPSSyncData)pairs.getValue();
        System.out.println("Script = " + pairs.getKey() + " status = " + syncData.getIsScriptDeleteOrNot());
        LinkedHashMap hashMap = syncData.getGrpLinkedHashMap();
          
        Iterator itg = hashMap.entrySet().iterator();
          
        while (itg.hasNext()) 
        {
          Map.Entry pairsg = (Map.Entry)itg.next();
          System.out.println(pairsg.getKey() + " == " + pairsg.getValue());
        }
      }**/
        
      long endProcessingTime = System.currentTimeMillis();
      Log.debugLog(className, "getScenGroupScriptInfo", "", "", "Total time taken to get Group name and script = " + (endProcessingTime - startProcessingTime) + " in milliSecs");
      
      objArr[0] = arrSGRPRecordFlds;
      objArr[1] = linkedHashMap;
      
      return objArr;
    }
    catch (Exception e) 
    {
      e.printStackTrace();
      Log.errorLog(className, "getScenGroupScriptInfo", "", "", "Exception - " + e);
      return null;
    }
  }
 
// boolean bolRunningTest = cmdExec.getResultByCommand(vecRunningTest, strCmd, strArgs, CmdExec.NETSTORM_CMD, "RunAsProcessUser", null, "Server"); 
  public LinkedHashMap updateTransPageSyncList(LinkedHashMap sessLinkedHashMap, String fromServer)
  {
    this.fromServer = fromServer;
    return updateTransPageSyncList(sessLinkedHashMap);
  }
  public LinkedHashMap updateTransPageSyncList(LinkedHashMap sessLinkedHashMap)
  {
    try
    {
      StringBuilder strNewScript = new StringBuilder();
      
      Set st = sessLinkedHashMap.keySet();
      
      Iterator it = st.iterator();
      while (it.hasNext()) 
      {
        Object sessKey = it.next();
        //System.out.println("sessKey = " + sessKey);
        ScenarioTPSSyncData syncData = (ScenarioTPSSyncData)sessLinkedHashMap.get(sessKey);
        
        //System.out.println("Script = " + sessKey + " status = " + syncData.getIsScriptDeleteOrNot());
        
        //removing script which are deleted
        if(syncData.getIsScriptDeleteOrNot() == 2) 
        {
          it.remove();
          continue;
        }
        else
        {
          LinkedHashMap hashMap = syncData.getGrpLinkedHashMap();
        
          Iterator itg = hashMap.entrySet().iterator();
        
          while (itg.hasNext()) 
          {
            Map.Entry pairsg = (Map.Entry)itg.next();
            //System.out.println(pairsg.getKey() + " == " + pairsg.getValue());
            //remove group those are deleted from SGRP
            if(!(Boolean)pairsg.getValue())
              itg.remove();
            else //reset default value
              hashMap.put(pairsg.getKey(), false);
          }
          
          if(syncData.getIsScriptDeleteOrNot() == 0)
          {
            strNewScript.append(" -s ");
            strNewScript.append(getProjectSubProject());
            strNewScript.append("/");
            strNewScript.append(sessKey.toString());
          }
          //reset default value
          syncData.setIsScriptDeleteOrNot(2);
            
        }
        sessLinkedHashMap.put(sessKey, syncData);
      }
      
      //System.out.println(strNewScript.length());
        
      
      LinkedHashMap sessLinkedHashMapnew = sessLinkedHashMap;
        
      //For Testing
     /** Iterator itT = sessLinkedHashMapnew.entrySet().iterator();
      while (itT.hasNext()) 
      {
        Map.Entry pairs = (Map.Entry)itT.next();
            
        ScenarioTPSSyncData syncDataT = (ScenarioTPSSyncData)pairs.getValue();
        System.out.println("Script = " + pairs.getKey() + " status = " + syncDataT.getIsScriptDeleteOrNot());
        System.out.println("syncDataT = " +syncDataT.getTransList().toString());
        LinkedHashMap hashMap = syncDataT.getGrpLinkedHashMap();
            
        Iterator itg = hashMap.entrySet().iterator();
            
        while (itg.hasNext()) 
        {
          Map.Entry pairsg = (Map.Entry)itg.next();
          System.out.println(pairsg.getKey() + " == " + pairsg.getValue());
        }
      }**/
      if(strNewScript.length() != 0)
        sessLinkedHashMapnew = getTransPageSyncList(strNewScript.toString(), "0", sessLinkedHashMap);
        
        
      return sessLinkedHashMapnew;
        
    }
    catch (Exception e) 
    {
      e.printStackTrace();
      Log.errorLog(className, "getScenGroupScriptInfo", "", "", "Exception - " + e);
      return null;
    }
  }
  
  public ArrayList getScriptTransList(String ScriptName, LinkedHashMap sessLinkedHashMap)
  {
    Log.debugLog(className, "getScriptTransList", "", "", "Method called.");
    ArrayList arrTransList = new ArrayList();
    try
    {
      if(ScriptName.equals(""))
      {
        Iterator itT = sessLinkedHashMap.entrySet().iterator();
        while (itT.hasNext()) 
        {
          Map.Entry pairs = (Map.Entry)itT.next();
              
          ScenarioTPSSyncData syncDataT = (ScenarioTPSSyncData)pairs.getValue();
          ArrayList arrScriptT = syncDataT.getTransList();
          arrTransList.addAll(arrScriptT);
        }
        return arrTransList;
      }
      else
      {
        ScenarioTPSSyncData syncDataT = (ScenarioTPSSyncData)sessLinkedHashMap.get(ScriptName);
        return syncDataT.getTransList();
      }
    }
    catch (Exception e) 
    {
      Log.errorLog(className, "getScriptTransList", "", "", "Exception - " + e);
      return arrTransList;
    }
  }
  
  public ArrayList getScriptPageList(String ScriptName, LinkedHashMap sessLinkedHashMap)
  {
    Log.debugLog(className, "getScriptPageList", "", "", "Method called.");
    ArrayList arrPageList = new ArrayList();
    try
    {
      if(ScriptName.equals(""))
      {
        Iterator itT = sessLinkedHashMap.entrySet().iterator();
        while (itT.hasNext()) 
        {
          Map.Entry pairs = (Map.Entry)itT.next();
          //System.out.println(pairs.getKey() + " Key ");
          ScenarioTPSSyncData syncDataT = (ScenarioTPSSyncData)pairs.getValue();
          ArrayList arrScriptT = syncDataT.getPageList();
          //System.out.println("jjjjjjjjjjjjjjj" + arrScriptT.toString());
          arrPageList.addAll(arrScriptT);
        }
        return arrPageList;
      }
      else
      {
        ScenarioTPSSyncData syncDataT = (ScenarioTPSSyncData)sessLinkedHashMap.get(ScriptName);
        return syncDataT.getPageList();
      }
    }
    catch (Exception e) 
    {
      Log.errorLog(className, "getScriptPageList", "", "", "Exception - " + e);
      return arrPageList;
    }
  }
  
  public ArrayList getScriptFromPage(String selectedPage, LinkedHashMap sessLinkedHashMap)
  {
    Log.debugLog(className, "getScriptFromPage", "", "", "Method called.");
    ArrayList arrPageList = new ArrayList();
    try
    {
      //System.out.println("\n\n\n");
      Iterator itT = sessLinkedHashMap.entrySet().iterator();
      while (itT.hasNext()) 
      {
        Map.Entry pairs = (Map.Entry)itT.next();
        String scriptKey = pairs.getKey().toString();
        //System.out.println(scriptKey + " scriptKey ");
        ScenarioTPSSyncData syncDataT = (ScenarioTPSSyncData)pairs.getValue();
        ArrayList arrScriptT = syncDataT.getPageList();
        //System.out.println("jjjjjjjjjjjjjjj"  + arrScriptT.toString());
        //System.out.println("gdfgfdfg" + selectedPage);
        if(arrScriptT.contains(selectedPage.trim()))
        {
          //System.out.println("Testt\n " + arrPageList.toString());
          if(!arrPageList.contains(scriptKey))
            arrPageList.add(scriptKey);
        }
      }
      return arrPageList;
    }
    catch (Exception e) 
    {
      Log.errorLog(className, "getScriptFromPage", "", "", "Exception - " + e);
      return arrPageList;
    }
  }
  
  public ArrayList getScriptFromTransaction(String selectedTrans, LinkedHashMap sessLinkedHashMap)
  {
    Log.debugLog(className, "getScriptFromTransaction", "", "", "Method called.");
    ArrayList arrScriptList = new ArrayList();
    try
    {
      Iterator itT = sessLinkedHashMap.entrySet().iterator();
      while (itT.hasNext()) 
      {
        Map.Entry pairs = (Map.Entry)itT.next();
        String scriptKey = pairs.getKey().toString();
        //System.out.println(scriptKey + " scriptKey ");
        ScenarioTPSSyncData syncDataT = (ScenarioTPSSyncData)pairs.getValue();
        ArrayList arrScriptT = syncDataT.getTransList();
        //System.out.println("jjjjjjjjjjjjjjj"  + arrScriptT.toString());
        //System.out.println("gdfgfdfg" + selectedTrans);
        if(arrScriptT.contains(selectedTrans))
        {
          if(!arrScriptList.contains(scriptKey))
            arrScriptList.add(scriptKey);
        }
      }
      return arrScriptList;
    }
    catch (Exception e) 
    {
      Log.errorLog(className, "getScriptFromTransaction", "", "", "Exception - " + e);
      return arrScriptList;
    }
  }  
  
  public ArrayList getScriptFromSyncPoint(String selectedSyncPoint, LinkedHashMap sessLinkedHashMap)
  {
    Log.debugLog(className, "getScriptFromSyncPoint", "", "", "Method called.");
    ArrayList arrSyncPointList = new ArrayList();
    try
    {
      Iterator itT = sessLinkedHashMap.entrySet().iterator();
      while (itT.hasNext()) 
      {
        Map.Entry pairs = (Map.Entry)itT.next();
        String scriptKey = pairs.getKey().toString();
        //System.out.println(scriptKey + " scriptKey ");
        ScenarioTPSSyncData syncDataT = (ScenarioTPSSyncData)pairs.getValue();
        ArrayList arrScriptT = syncDataT.getSyncList();
       // System.out.println("jjjjjjjjjjjjjjj"  + arrScriptT.toString());
        //System.out.println("gdfgfdfg" + selectedSyncPoint);
        if(arrScriptT.contains(selectedSyncPoint.trim()))
        {
          if(!arrSyncPointList.contains(scriptKey))
            arrSyncPointList.add(scriptKey);
        }
      }
      return arrSyncPointList;
    }
    catch (Exception e) 
    {
      Log.errorLog(className, "getScriptFromPage", "", "", "Exception - " + e);
      return arrSyncPointList;
    }
  } 
  
  public ArrayList getScriptSyncList(String ScriptName, LinkedHashMap sessLinkedHashMap)
  {
    Log.debugLog(className, "getScriptFromSyncPoint", "", "", "Method called.");
    ArrayList arrSyncList = new ArrayList();
    try
    {
      if(ScriptName.equals(""))
      {
        Iterator itT = sessLinkedHashMap.entrySet().iterator();
        while (itT.hasNext()) 
        {
          Map.Entry pairs = (Map.Entry)itT.next();
              
          ScenarioTPSSyncData syncDataT = (ScenarioTPSSyncData)pairs.getValue();
          ArrayList arrScriptT = syncDataT.getSyncList();
          arrSyncList.addAll(arrScriptT);
        }
        return arrSyncList;
      }
      else
      {
        ScenarioTPSSyncData syncDataT = (ScenarioTPSSyncData)sessLinkedHashMap.get(ScriptName);
        return syncDataT.getSyncList();
      }
    }
    catch (Exception e) 
    {
      Log.errorLog(className, "getScriptSyncList", "", "", "Exception - " + e);
      return arrSyncList;
    }
  }
  
  /*
   * P : property
   * Keyword list
   * This method give the information of the SGRP of selected field
   * arrGroupList array contain GroupName|Script Type|Script Name
   * Output :- GroupName|Script Name| Transaction name
   */
  public LinkedHashMap getTransPageSyncList(String strArgs, String operationName, LinkedHashMap sessLinkedHashMap)
  {
    Log.debugLog(className, "getTransPageSyncList", "", "", "Method called. operationName = " + operationName);

    try
    {
      long startProcessingTime = System.currentTimeMillis();
      ArrayList arrList = new ArrayList();
      int rowCount = 0;
      //get list of trans/Page/Sync of available group
      String strCmdName = "nsu_script_tool";
      
      //project/subproject/script name
      //operation - AllPageList/ShowTxListAllFlow/syncPoint
      String strCmdArgs = strArgs + " -o ShowScriptComponentsFromUsedFlow";
      
      //Argument for getting data from test run
      if(RUNTIMECHANGE_MODE_FLAG || VIEW_MODE_FLAG)
        strCmdArgs = "-t " + TRNUM + " -o ShowScriptComponentsFromUsedFlow";

      ScenarioTPSSyncData syncData = null;
      String scriptName = "";
        
      CmdExec exec = new CmdExec();
      Vector vecCmdOutput = new Vector();
      
      boolean bolResult = true;
      if(fromServer.equals("Server"))
        bolResult = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, CmdExec.NETSTORM_CMD, "RunAsProcessUser", null, "Server");
      else
        bolResult = exec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, getUserName(), null);
      
      if(vecCmdOutput != null)
      {
        for(int ii = 0; ii < vecCmdOutput.size(); ii++)
        {
          String[] arrTemp = rptUtilsBean.split(vecCmdOutput.get(ii).toString(), "|");
          /**if(ii == 1)
          {
            scriptName = arrTemp[0];
            syncData = (ScenarioTPSSyncData)sessLinkedHashMap.get(arrTemp[0]);
          }**/
         /** if(sessLinkedHashMap.containsKey(arrTemp[0]))
            System.out.println("contain" + arrTemp[0]);
          System.out.println("arrTemp[0] = " + arrTemp[0]);**/
          if(arrTemp.length > 3)
          {
            syncData = (ScenarioTPSSyncData)sessLinkedHashMap.get(arrTemp[0]);
            scriptName = arrTemp[0];
          }
          else //Error: null
            syncData = new ScenarioTPSSyncData();
          //if(syncData == null)
            //syncData = new ScenarioTPSSyncData();
          //System.out.println("fdsfff " + syncData.getIsScriptDeleteOrNot());
          
          if(arrTemp.length > 3 && arrTemp[3].trim().equals("Transaction"))
          {
            arrList = syncData.getTransList();
            arrList.add(arrTemp[4].trim());
            syncData.setTransList(arrList);
          }
          else if(arrTemp.length > 3 && arrTemp[3].trim().equals("Page"))
          {
            arrList = syncData.getPageList();
            arrList.add(arrTemp[4].trim());
            syncData.setPageName(arrList);
          }
          else if(arrTemp.length > 3 && arrTemp[3].trim().equals("SyncPoint"))
          {
            arrList = syncData.getSyncList();
            arrList.add(arrTemp[4].trim());
            syncData.setSyncList(arrList);
          } 
          sessLinkedHashMap.put(arrTemp[0], syncData);
        }
      }
      
      long endProcessingTime = System.currentTimeMillis();
      Log.debugLog(className, "getTransPageSyncList", "", "", "Total time taken to " + operationName + " = " + (endProcessingTime - startProcessingTime) + " in milliSecs");
      
      return sessLinkedHashMap;
    }
    catch (Exception e) 
    {
      e.printStackTrace();
      Log.errorLog(className, "getTransPageSyncList", "", "", "Exception - " + e);
      return null;
    }
  }
  
/**
 * This function directory name with full path
 * 'tr069_cpe_data.dat' and 'tr069_cpe_data.index' if both will file exist
 * It will return dir name with full path
 * @return
 */
  public ArrayList getCPEDataDir()
  {
    Log.debugLog(className, "getCPEDataDir", "", "", "Method Called");

    ArrayList arrListCPEDataDir = new ArrayList();
    try
    {
      String filePath = Tr069Dir;

      File file = new File(filePath);

      //checking directort exists or not
      if(!file.exists())
      {
        Log.debugLog(className, "getCPEDataDir", "", "", filePath + " directory does not exist.");
        return null;
      }

      //getting list of directory
      String[] arrgetAllFiles = file.list();

      for(int i = 0; i < arrgetAllFiles.length; i++)
      {
        String strDataDir = filePath + arrgetAllFiles[i];

        //checking inside tr069_cpe_data.dat and tr069_cpe_data.index files exist or not
        File fileTr = new File(strDataDir);

        if(fileTr.isDirectory())
        {
          File fileDat = new File(strDataDir + "/" + "tr069_cpe_data.dat");
          File fileIndex = new File(strDataDir + "/" + "tr069_cpe_data.dat.count");

          if(fileDat.exists() && fileIndex.exists())
            arrListCPEDataDir.add(strDataDir);
          else
            Log.debugLog(className, "getCPEDataDir", "", "", "Skipping directory " + strDataDir + " . tr069_cpe_data.dat and tr069_cpe_data.dat.index does not exists.");
        }
      }

      //sorting list of dir
      Collections.sort(arrListCPEDataDir);

      return arrListCPEDataDir;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "getCPEDataDir", "", "", "Exception - " + e);
      return arrListCPEDataDir;
    }
  }

  /**
   * This function call on apply button from jsp.
   * @param p
   * @param sesLoginName
   * @param errMsg
   * @return
   */
  public boolean executeRunTimeChangableFile(StringBuffer errMsg)
  {
    Log.debugLog(className, "executeRunTimeChangableFile", "", "", "Method Called.");
    //checking hidden file exist or not
    String runTimeFileHiddenPath = getTRDirPath(RUN_TIME_CHANGABLE_DIR + "/" + RUN_TIME_CHANGABLE_FILE_HIDDEN);
    if(!checkFileExistOrnot(runTimeFileHiddenPath, false))
      return false;

    //checking runtime file exist or not
    String runTimeFilePath = getTRDirPath(RUN_TIME_CHANGABLE_DIR + "/" + RUN_TIME_CHANGABLE_FILE);
    if(!checkFileExistOrnot(runTimeFilePath, true))
      return false;

    //copying hidden file contents into runtimefile
    copyToFile(runTimeFileHiddenPath, runTimeFilePath);

    //running nsi_runtime_update shell on apply
    //usage: nsi_runtime_update test-run-number
    String strCmdName = "nsi_runtime_update";
    String strCmdArgs = TRNUM ;
    int vecLen = 0;
    CmdExec exec = new CmdExec();
    Vector vecCmdOutput = exec.getResultByCommand(strCmdName, strCmdArgs, 0, getUserName(), null);

    if(vecCmdOutput == null)
    {
      errMsg.append("Error in executing " + strCmdName + " command");
      return false;
    }

    if((vecCmdOutput.size() > 0) && ((String)vecCmdOutput.lastElement()).startsWith("ERROR"))
      vecLen= vecCmdOutput.size() - 1;
    else
      vecLen = vecCmdOutput.size();

    //Error in applying changes in the scenario of the running test:
    for(int i = 0; i < vecLen; i++)
    {
      String line = vecCmdOutput.elementAt(i).toString();
      line = rptUtilsBean.replaceSpecialCharacter(line);
      errMsg.append(line + "\\n");
    }

    if((vecCmdOutput.size() > 0) && ((String)vecCmdOutput.lastElement()).startsWith("ERROR"))
    {
      //Error is coming append line on 0 index with TR number
      vecCmdOutput.remove((vecCmdOutput.size() - 1));
      errMsg.insert(0, "Error in applying changes in the scenario of the running test: " + TRNUM + "\\n");
      Log.errorLog(className, "executeRunTimeChangableFile", "", "", "Error in executing command - " + rptUtilsBean.replace(errMsg.toString(), "'", "\'"));
      return false;
    }

    //deleting hidden file after executing nsi_runtime_update shell successfully
    deleterunTimeHiddenFile();
    return true;
  }

  /*
   * load hidden file in properties to maintain uniqueness
   */
  public boolean makeRTCfile(String runTimeFilePath, String groupName, String keywordNameWithValue, String operation)
  {
    Log.debugLog(className, "makeRTCfile", "", "", "Method Called. keyword name = " + keywordNameWithValue + ", Operation = " + operation);
    try
    {
      Properties dummy_p = new Properties();
      Properties rtc_p = new Properties();
      StringBuffer errMsg = new StringBuffer();
      int deleteCount = 0;

      //load file in properties
      loadFileInProperties(dummy_p, rtc_p, runTimeFilePath, errMsg, false);
      //get keyword name (G_SESSION_PACING ALL 1 0 5555)
      String keywordName = getKeywordName(dummy_p, keywordNameWithValue);

      if(!operation.equals(DELETE_OPERATION))
      {
        //If keyword is not present in the propery add keyword in property
        if(dummy_p.getProperty(keywordName) == null)
        {
          setKeyValue(dummy_p, keywordNameWithValue);
        }
        else
        {
          //If keyword is not present add keyword in property
          //This condition will not happen we are reading only hidden file
          if(getKeywordFieldsMatchCount(dummy_p, keywordName, keywordNameWithValue) == 0)
            setKeyValue(dummy_p, keywordNameWithValue);
          else
          {
            if(groupName.equals(""))//Global keyword
              clearKeywordValue(dummy_p, keywordNameWithValue);
            else
            {
              //For group base
              deleteKeywordRecords(dummy_p, keywordName, "0", groupName);
            }
            setKeyValue(dummy_p, keywordNameWithValue);
          }
        }
      }

      if(dummy_p != null)
      {
        //store all key in the Enumeration
        File fileScen = new File(runTimeFilePath);
        fileScen.delete();
        fileScen.createNewFile();
        FileOutputStream fos = new FileOutputStream(fileScen, true);
        PrintStream runTimeFile = new PrintStream(fos);
        Enumeration keywords = dummy_p.keys();

        while(keywords.hasMoreElements())
        {
          String key_str = keywords.nextElement().toString();
          String pipeSeparatedValue = dummy_p.get(key_str).toString();

          /*Then it will be saved in property (for 'G_KA_PCT' as example )
          *as 'ALL 100|G1 60|G2 50'
          */
          StringTokenizer key_value = new StringTokenizer(pipeSeparatedValue, "|");
          while(key_value.hasMoreTokens())
          {
            String kwVal = key_value.nextToken();
            String line = key_str + " " + kwVal;
            if((operation.equals(DELETE_OPERATION)) && line.equals(keywordNameWithValue))
            {
              Log.debugLog(className, "makeRTCfile", "", "", "Deleting keyword from file = " + line);
              deleteCount++;
              continue;
            }
            else
            {
              Log.debugLog(className, "makeRTCfile", "", "", "Adding keyword in file = " + line);
              runTimeFile.println(line);
            }
          }
        }
        runTimeFile.close();
        fos.close();
      }

      if((deleteCount == 0) && operation.equals(DELETE_OPERATION))
        return false;
      else
        return true;
    }
    catch (Exception e)
    {
      // TODO: handle exception
      Log.errorLog(className, "makeRTCfile", "", "", "Exception - " + e);
      return false;
    }
  }

  /*
   * updating hidden file. on click apply or tab switching.
   */
  public boolean updateRunTimeChangableFile(String groupName, String keyword, String operation)
  {
    Log.debugLog(className, "updateRunTimeChangableFile", "", "", "Method Called. Scenario file name = " + scenFileName + ", keyword name = " + keyword);

    try
    {
      //checking hidden file exist or not
      //if present it will open in append mode
      //otherwise it will create new file
      String runTimeFilePath = getTRDirPath(RUN_TIME_CHANGABLE_DIR + "/" + RUN_TIME_CHANGABLE_FILE_HIDDEN);
      if(!checkFileExistOrnot(runTimeFilePath, true))
        return false;

      return makeRTCfile(runTimeFilePath, groupName, keyword, operation);
      /*File fileScen = new File(runTimeFilePath);
      FileOutputStream fos = new FileOutputStream(fileScen, true);
      PrintStream runTimeFile = new PrintStream(fos);
      runTimeFile.println(keyword);
      runTimeFile.close();
      fos.close();*/
    }
    catch (Exception e)
    {
      // TODO: handle exception
      Log.errorLog(className, "updateRunTimeChangableFile", "", "", "Exception - " + e);
      return false;
    }
  }

  public boolean deleterunTimeHiddenFile()
  {
    String runTimeFileHiddenPath = getTRDirPath(RUN_TIME_CHANGABLE_DIR + "/" + RUN_TIME_CHANGABLE_FILE_HIDDEN);
    if(checkFileExistOrnot(runTimeFileHiddenPath, false))
    {
      File deleteRunTimeFile = new File(runTimeFileHiddenPath);
      if(deleteRunTimeFile.delete())
        return true;
      else
        return false;
    }
    return true;
  }

  //this to retrieve all values for the same keyword
  public ArrayList readUpdateRuntime(String keyword, String fileName)
  {
    Log.debugLog(className, "readUpdateRunTime", "", "", "Method Start. Keyword = " + keyword + ", File Name = " + fileName);

    ArrayList<String []> arrReadLine = new ArrayList<String []>();
    String[] keyValues = null;

    try
    {
      String scenKwrd = "";

      // Creating path for /runtime_changes/runtime_changes_all.conf file
      String runTimeLogFile = fileName + "/" + RUN_TIME_CHANGABLE_DIR + "/" + RUN_TIME_CHANGABLE_FILE_ALL;
      Log.debugLog(className, "readUpdateRunTime", "", "", "Path to read logs = " + runTimeLogFile);

      // open file object in buffer stram
      FileInputStream fis = new FileInputStream(runTimeLogFile);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));

      while((scenKwrd = br.readLine()) != null)
      {
        if(scenKwrd.startsWith(keyword))
        {
          StringTokenizer st = new StringTokenizer(scenKwrd);
          keyValues = new String[st.countTokens() - 1];
          int j = 0;
          st.nextToken();  // Skip keyward
          while(st.hasMoreTokens())
          {
            keyValues[j] = (st.nextToken()).trim();
            j++;
          }
          arrReadLine.add(keyValues);
        }
      }
      br.close(); // closing buffer stream
      fis.close();

      Log.debugLog(className, "readUpdateRunTime", "", "", "Method End.");
      return arrReadLine;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "readUpdateRunTime", "", "", "Exception", e);
      return null;
    }
  }

  /*
   * This method
   */
  public Object[] executeRuntimeCmd(Object servletRunTimeChanges[], boolean isProperty, int testRun, StringBuffer errMsg)
  {
    Log.debugLog(className, "executeRuntimeCmd", "", "", "Method Start. Test Run = " + testRun + "");

    Vector vecResultObj = new Vector();
    String message = "";
    String strCmdName = "nsi_runtime_update";
    String strCmdArgs = "" + testRun;
    String userName = getUserName();

    CmdExec cmdExecObj = new CmdExec();
    //boolean bolResult = cmdExecObj.getResultByCommand(vecResultObj, strCmdName, strCmdArgs, 0, userName, null);
    boolean bolResult = true;
    vecResultObj = cmdExecObj.getResultByCommand(strCmdName, strCmdArgs, 0, userName, null);
    
    if((vecResultObj.size() > 0) && ((String)vecResultObj.lastElement()).startsWith("ERROR"))
    {
      bolResult = false;
      vecResultObj.remove((vecResultObj.size() - 1));
    }

    if(vecResultObj == null)
    {
      message = "Error in applying runtime changes for test run = " + testRun + ". command Name = " + strCmdName;
      Log.errorLog(className, "executeRuntimeCmd", "", "", message);
      errMsg.append(message);
      
      servletRunTimeChanges[0] = bolResult;
      servletRunTimeChanges[1] = errMsg;
      
      if(isProperty)
        servletRunTimeChanges[2] = getKeyValues(scenFileName, new StringBuffer());

      return servletRunTimeChanges;
    }

    for(int i = 0; i < vecResultObj.size(); i++)
    {
      Log.debugLog(className, "executeRuntimeCmd", "", "", "Output = " + vecResultObj.get(i).toString());
      errMsg.append(vecResultObj.get(i).toString() + "\n");
    }

    servletRunTimeChanges[0] = bolResult;
    servletRunTimeChanges[1] = errMsg;
    
    if(isProperty)
      servletRunTimeChanges[2] = getKeyValues(scenFileName, new StringBuffer());
    
    return servletRunTimeChanges;
  }

  /*
   * This function using for servlet communication
   * args
   * 1 - path upto TR EX. /home/netstorm/work/webapps/TRXXX
   * 2 - datalines. Ex- data[0] START_MONITOR hh checALL vecName
   * 3 - Test run number
   * 4 - StringBUffer which store message output given by shell ao file not found
   * 
   * return Object array on
   *  0 - index return status (pass/fail)
   *  1 - message (EX. file not found)
   * 
   */
  public Object[] applyRunTimeChanges(String pathName, String[] data, int testRun, String scenName, boolean isProperty, String fromGui, String future1, String future2, StringBuffer errMsg)
  {
    Log.debugLog(className, "applyRunTimeChanges", "", "", "Method Start. Path = " + pathName + ", Data Rows = " + data.length + ", Test Run = " + testRun + "");
    
    //getting scenario name 
    //TestRun/4200/myscen
    if(scenName != "")
      scenFileName = scenName;
    
    //0 - true/false and 1 - message
    Object[] servletRunTimeChanges = new Object[2];
    
    if(isProperty)
      servletRunTimeChanges = new Object[3];
    
    String message = ""; 
    
    boolean result = true;
    
    ///home/netstorm/work/webapps/TRXXX/runtime_changes
    String runTimeDirPath = pathName + "/" + RUN_TIME_CHANGABLE_DIR;
    
    Log.debugLog(className, "applyRunTimeChanges", "", "", "Run time directory path = " + runTimeDirPath);

    try
    {
      File dirObj = new File(runTimeDirPath);
      if(!dirObj.exists())
      {
        if(!dirObj.mkdir())
        {
          message = "Runtime changes cannot be applied. Error in creating directory '" + runTimeDirPath + "'";
          Log.errorLog(className, "applyRunTimeChanges", "", "", message);
          errMsg.append(message);
          
          servletRunTimeChanges[0] = false;
          servletRunTimeChanges[1] = errMsg;
          
          if(isProperty)
            servletRunTimeChanges[2] = getKeyValues(scenFileName, new StringBuffer());
          
          return servletRunTimeChanges;
        }
      }
      
      String runTimeFilePath = runTimeDirPath + "/" + RUN_TIME_CHANGABLE_FILE;
      File fileObj = new File(runTimeFilePath);
      
      fileObj.delete();
      if(!fileObj.createNewFile())
      {
        message = "Runtime changes cannot be applied. Error in creating file '" + runTimeFilePath + "'";
        Log.errorLog(className, "applyRunTimeChanges", "", "", message);
        errMsg.append(message);
        
        servletRunTimeChanges[0] = false;
        servletRunTimeChanges[1] = errMsg;
        
        if(isProperty)
          servletRunTimeChanges[2] = getKeyValues(scenFileName, new StringBuffer());
        
        return servletRunTimeChanges;
      }
      
      FileOutputStream fstream = new FileOutputStream(runTimeFilePath, true);
      PrintStream pw = new PrintStream(fstream);

      for(int i = 0; i < data.length; i++)
      {
        Log.debugLog(className, "applyRunTimeChanges", "", "", "Adding data in file = " + data[i]);
        pw.println(data[i]);
      }

      pw.close();
      fstream.close();
      
      return executeRuntimeCmd(servletRunTimeChanges, isProperty, testRun, errMsg);
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "applyRunTimeChanges", "", "", "Exception", e);
      return null;
    }
  }

  //Setting project subproject
  public boolean setProjectSubProject()
  {
    if(EDIT_MODE_FLAG || DEFAULT_SETTING_MODE_FLAG)
    {
      String arrTemp[] = rptUtilsBean.split(scenFileName, "/");
      projectSubProj = arrTemp[0] + "/" +  arrTemp[1];
    }
    if((RUNTIMECHANGE_MODE_FLAG) || (VIEW_MODE_FLAG))
    {
      // nsu_show_test_logs -t 1123 -r -l
      String strCmdName = "nsu_show_test_logs";
      String strCmdArgs =  "-t " +  TRNUM  + " -rl";
      CmdExec exec = new CmdExec();
      Vector vecCmdOutput = exec.getResultByCommand(strCmdName, strCmdArgs, 0, getUserName(), null);

      StringBuffer errMsg = new StringBuffer();
    
      if(vecCmdOutput == null)
      {
        errMsg.append("Error in executing " + strCmdName + " command");
        return false;
      }

      //Error in applying changes in the scenario of the running test:
      for(int i = 0; i < (vecCmdOutput.size() - 1); i++)
      {
        errMsg.append(vecCmdOutput.elementAt(i).toString() + "\\n");
      }

      if((vecCmdOutput.size() > 0) && ((String)vecCmdOutput.lastElement()).startsWith("ERROR"))
      {
        //Error is coming append line on 0 index with TR number
        Log.errorLog(className, "setProjectSubProject", "", "", "Error in executing command - " + errMsg.toString());
        return false;
      }
    
      String arrTemp[] = rptUtilsBean.split(vecCmdOutput.get(1).toString(), "|");
      projectSubProj = arrTemp[0] + "/" +  arrTemp[1];
    }
      
      return true;
  }
 
  public String getProjectSubProject()
  {
    return projectSubProj;
  }
  
  /*
   * In this function it check latest value and user define value update are same or not 
   * if same it will nor add in the file in run time.
   * In edit mode it will not check value. It  first clear the value than add in the properties.
   * Ex: RAMPDOWN in goal base.
   * Any one switch FCU to goal base. FCU contain all 5 phases but in goal base it 3 phase stablize, duration and ramp down
   * need to check updated value
   */
  public boolean checkScheduleKeywordValueExist(Properties p, String keywordName, String keywordNameWithValue)
  {
    Log.debugLog(className, "applyRunTimeChanges", "", "", "Method Start. keywordName = " + keywordName + ", keywordNameWithValue = " + keywordNameWithValue);
    
    //In run time mode return. No need to check
    if(EDIT_MODE_FLAG)
      return false;
    
    //getting properties of keyword
    String schValue = p.getProperty(keywordName);
    
    //Splitting existing value which are pipe separated
    String arrTemp[] = rptUtilsBean.split(schValue, "|");
    
    //Current value which are space separated 
    String currArr[] = rptUtilsBean.split(keywordNameWithValue, " ");
    
    //ALL Start0 START IMMEDIATELY

    String currTemp = "";
    for(int i = 3 ; i < currArr.length; i++)
    {
      if(currTemp.equals(""))
        currTemp = currArr[i];
      else
        currTemp = currTemp + " " + currArr[i]; 
    }
    
    String fldTemp  = "";
    for(int i = 0; i < arrTemp.length; i++)
    {
      String fldArr[] = rptUtilsBean.split(arrTemp[i].trim(), " ");
      
      //checking phase name EX: START
      if(fldArr[2].trim().equals(currArr[2].trim()))
      {
        for(int k = 3; k < fldArr.length; k++)
        {
          if(fldTemp.equals(""))
            fldTemp = fldArr[k];
          else
            fldTemp = fldTemp + " " + fldArr[k]; 
        }
        break;
      }
    }
    
    //matching current and previous
    if(currTemp.equals(fldTemp))
      return true;
    
    return false;
  }
  
  /*  This Method is used to return a vector consisting of 9 pipe separated values for each item in the vector.
   * default value for keyword HEALTH_MONITOR_INODE_SPACE and HEALTH_MONITOR_DISK_SPACE is '1 ALL 50 25 50 No'. Input vector will contain 4 pipe separated values for each element.
   * like FS1|50|25|50 and output vector will contains 9 pipe separated values for each element like (for example) '1|FS1|50|25|50|50|25|50|NO' for each element in the vector.
   * 
   */

  public Vector getVectorListOfExistingFileSystemForDiskAndINode(Properties p , Vector FileSystemData, String StrKeyword)
  {
    Log.debugLog(className, "getVectorListOfExistingFileSystemForDiskAndINode", "", "", "Method called." );
    try
    {
      Vector vecFileLines = new Vector();
      if(FileSystemData != null)
      {
        String strKeywordValue = p.getProperty(StrKeyword);
        for(int i =0; i < FileSystemData.size(); i ++)
        {
          Boolean FSExistOrNot = false;
          String systemDataItem = "";
          if(FileSystemData.get(i).toString().contains("\""))
            systemDataItem = FileSystemData.get(i).toString().replace("\"", "");
          else
            systemDataItem = FileSystemData.get(i).toString(); 
          
          String FileSystemName = getFileSystemName(systemDataItem);
          if(strKeywordValue.contains("|"))
          {
        	String strSplit[] = strKeywordValue.split("\\|");
        	for(int j =0; j < strSplit.length; j++)
        	{
        	  String strSplitFSData[] = strSplit[j].split(" ");
        	  if(strSplitFSData[1].toUpperCase().equals("ALL"))
        	    continue;
    	      if(strSplitFSData[1].trim().equals(FileSystemName))
    	      {
    	        FSExistOrNot = true;
    	    	String str = strSplitFSData[0] + "|" + systemDataItem + "|" + strSplitFSData[2] + "|" + strSplitFSData[3] + "|" + strSplitFSData[4] + "|" + strSplitFSData[5];
    	    	vecFileLines.add(str.trim().toString());
    	        break;
    	      }
        	}
        	/*for(int jj=0; jj <strSplit.length; jj++)
        	{
              String strSplitFSData[] = strSplit[jj].split(" ");
          	  if(strSplitFSData[1].toUpperCase().equals("ALL"))
          	    continue;
      	      if(strSplitFSData[1].trim().equals(FileSystemName))
      	    	FSExistOrNot = true;
        	}*/
        	if(!FSExistOrNot)
        	{
              for(int kk=0; kk <strSplit.length; kk++)
              {
                String strSplitFSData_1[] = strSplit[kk].split(" ");
              	if(strSplitFSData_1[1].toUpperCase().equals("ALL"))
              	{
              	  String strng = strSplitFSData_1[0] + "|" + systemDataItem + "|" + strSplitFSData_1[2] + "|" + strSplitFSData_1[3] + "|" + strSplitFSData_1[4] + "|" + strSplitFSData_1[5];
              	  vecFileLines.add(strng.trim().toString());
        	      break;	  
              	}
              }
        	}
          }
          else
          {
        	String strSplitFSData[] = strKeywordValue.split(" ");
            String str = strSplitFSData[0] + "|" + systemDataItem + "|" + strSplitFSData[2] + "|" + strSplitFSData[3] + "|" + strSplitFSData[4] + "|" + strSplitFSData[5];
            vecFileLines.add(str.trim().toString());
          }
        }
      }
      return vecFileLines;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getVectorListOfExistingFileSystemForDiskAndINode", "", "", "Exception - ", e);
      return null;
    }
  }
  public String getFileSystemName(String FSInfo)
  {
    Log.debugLog(className, "getFileSystemName", "", "", "Method called." + " FSInfo = " + FSInfo);
    String StrFSName = "";
	if(!FSInfo.equals(""))
    {
     String str[] = FSInfo.split("\\|");
     if(str[0].contains("\""))
       str[0] = str[0].replace("\"", "");
     StrFSName = str[0];
    }
	return StrFSName.trim();
  }
  
  /* main is for testing only */
  public static void main(String[] args)
  {

    FileBean bean = new FileBean("0", "netstorm");
    StringBuffer errMsg = new StringBuffer();
    Properties p = bean.getKeyValues("Project2/sub1/ggggggg", errMsg);
    LinkedHashMap linkedHashMap = new LinkedHashMap();
    ScenarioTPSSyncData data = new ScenarioTPSSyncData();
    LinkedHashMap grHashMap = new LinkedHashMap();
    grHashMap.put("G1", false);
    grHashMap.put("G21", false);
    data.setGrpLinkedHashMap(grHashMap);
    data.setIsScriptDeleteOrNot(2);
    
    //linkedHashMap.put("S1", data);
    ScenarioTPSSyncData data1 = new ScenarioTPSSyncData();
    grHashMap.clear();
    grHashMap.put("G1", false);
    grHashMap.put("G31", false);
    data1.setGrpLinkedHashMap(grHashMap);
    ArrayList arrayList = new ArrayList();
    arrayList.add("Grp1p");
    arrayList.add("Grp2p");
    arrayList.add("Grp3p");
    data1.setPageName(arrayList);
   // linkedHashMap.put("script1", data1);
    
    Object[] objTemp = bean.getScenGroupScriptInfo(p, "SGRP", linkedHashMap);
    LinkedHashMap linkedHashMap2 = new LinkedHashMap();
    if(objTemp != null)
    {
      String arrTemp[][] = (String[][])objTemp[0];
      LinkedHashMap sesHashMap = (LinkedHashMap)objTemp[1];
      for(int i = 0; i < arrTemp.length; i++)
      {
        for(int ii = 0; ii < arrTemp[i].length; ii++)
        {
          System.out.print(" " + arrTemp[i][ii]);
        }
        System.out.println("\n");
      }
      
     linkedHashMap2 =  bean.updateTransPageSyncList(sesHashMap);
     
     Iterator itT = linkedHashMap2.entrySet().iterator();
     while (itT.hasNext()) 
     {
       Map.Entry pairs = (Map.Entry)itT.next();
           
       ScenarioTPSSyncData syncDataT = (ScenarioTPSSyncData)pairs.getValue();
       System.out.println("Script = " + pairs.getKey());
       System.out.println("Transaction List = " +syncDataT.getTransList().toString());
       System.out.println("Page List = " +syncDataT.getPageList().toString());
       System.out.println("Sync List = " +syncDataT.getSyncList().toString());
       
       LinkedHashMap hashMap = syncDataT.getGrpLinkedHashMap();
       
       Iterator itg = hashMap.entrySet().iterator();
           
       while (itg.hasNext()) 
       {
         Map.Entry pairsg = (Map.Entry)itg.next();
         System.out.println(pairsg.getKey() + " == " + pairsg.getValue());
       }
     }
     
    }
    
    //bean.getScriptTransList(ScriptName, sessLinkedHashMap)
    System.out.println("\n\nTrans List " + bean.getScriptFromSyncPoint("syncx\"", linkedHashMap2));
   /* ArrayList arrayListObj = bean.getCPEDataDir();
    for(int i = 0; i < arrayListObj.size(); i++)
      System.out.println("arrRecordFlds[" + i + "] = " + arrayListObj.get(i).toString());*/

    ///String runTimeFilePath = "C:\\home\\netstorm\\work\\webapps\\logs\\TR20142\\runtime_changes\\.runtime_changes.conf";
    //bean.makeRTCfile(runTimeFilePath, "ALL", "G_PAGE_THINK_TIME ALL ALL 2 5000", "DELETE");
    //bean.makeRTCfile(runTimeFilePath, "G_SESSION_PACING ALL 1 0 5555", "add);
    //Properties p = bean.getKeyValues("FixConUsers.conf", new StringBuffer());

//    int num = bean.deleteKeywordRecords(p, args[0], args[1], args[2]);

    /**
    String arrRecordFlds[][] = bean.getKeywordRecordFields(p, args[0], args[1], args[2]);
    if(arrRecordFlds != null)
    {
     for(int i = 0; i < arrRecordFlds.length; i++)
       for(int j = 0; j < arrRecordFlds[i].length; j++)
         System.out.println("arrRecordFlds " + i + " " + j + " " + arrRecordFlds[i][j]);
    }
    **/

//    String arrFldValues[] = bean.getKeywordRecordOneField(p, args[0], args[1], args[2], "0");
//    if(arrFldValues != null)
//    {
//      for(int i = 0; i < arrFldValues.length; i++)
//       System.out.println("arrFldValues " + i + " " + arrFldValues[i]);
//    }
//
//    bean.addKeywordSubRecord(p, args[0], args[1], args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]), args[5]);
//
//    String arrSubRowId[] = bean.split(args[5], ",");
//
//    bean.deleteKeywordSubRecordsByRowId(p, args[0], args[1], args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]), arrSubRowId);
//
//    bean.updateKeyValue(p, "FixConUsers.conf");


      /*String arrScripts[] = bean.getSessionNames();
      for(int i = 0; i < arrScripts.length; i++)
        System.out.println(arrScripts[i]);*/

  }

}

// -- end of class -------------------------------------------------------------

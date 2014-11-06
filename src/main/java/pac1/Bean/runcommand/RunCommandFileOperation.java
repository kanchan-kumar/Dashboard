package pac1.Bean.runcommand;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

import pac1.Bean.Config;
import pac1.Bean.Log;
import pac1.Bean.rptUtilsBean;


/**
 * This class is for providing data to client
 * @author Bala Sudheer
 * @since Netsorm Version 4.0.1
 * @Modification_History  - Initial Version 4.0.1
 * @version 4.0.1
 * */

public class RunCommandFileOperation {
  
  private  final static String className = "RunCommandFileOperation";
  private static String  runCommandFilePath;
  private static String  siteRunCommandFilePath;
  
  /**
   * This method saves data in file
   * @param siteRunCmdDataList
   * @param errMsg
   */
  public void saveDataInFiles(ArrayList<String> siteRunCmdDataList, StringBuffer errMsg)
  {
    Log.debugLog(className, "saveDataInFiles", "", "", "Method started for path");
    
    try
    {  
       String path = getSiteRunCommandFilePath() + "site_run_command.dat";
       saveDataInFile(siteRunCmdDataList, path, errMsg);
    }
    catch(Exception ex)
    {
      errMsg.append("Unable to save due to " + ex);
      Log.stackTraceLog(className, "saveDataInFile", "", "","Exception in saveDataInFile", ex);
    } 
  }
  
  
  /**
   * This method save date in given file path.
   * @param runCmdDataList
   * @param path
   */
  private void saveDataInFile(ArrayList<String> runCmdDataList, String path, StringBuffer errMsg) 
  {
    Log.debugLog(className, "getAllFilesCommandsList", "", "", "Method started for path " + path);
    File file = null;
    FileWriter fw = null;

    try
    {
      file = new File(path);
      if (file.exists())
        file.delete();

      fw = new FileWriter(file);

      fw.write("#--------------------------------------------------------------------------------"
          + "----------------\n#Group Name|CommandName|Actual Command|Role|Server Type|Filter Keyword|View Type|Is Header Contains|Separator|CommandUIArgs|Max Inline Arguments|Future1|Future2|Future3|Future4|Future5|Description"
          + "\n#------------------------------------------------------------------------------------------------\n");
      fw.flush();
      
      Collections.sort(runCmdDataList);


      for (int i = 0; i < runCmdDataList.size(); i++)
      {
        String line = runCmdDataList.get(i);
        fw.write(line);
        fw.write("\n");
      }

      fw.flush();
      fw.close();
      
      try
      {
        Log.debugLogAlways(className, "saveDataInFile", "", "", "changing file permission");

        rptUtilsBean.changeFilePerm(file.getAbsolutePath(), "netstorm", "netstorm", "664");
      }
      catch (Exception e) {
        Log.stackTraceLog(className, "saveDataInFile", "", "", "Unable to change file permission", e);

      }
    }
    catch (Exception e)
    {
      errMsg.append("Unable to save due to " + e);
      Log.stackTraceLog(className, "saveDataInFile", "", "", "Exception in saveDataInFile", e);
    }
    finally
    {
      try
      {
        fw.close();
      }
      catch (Exception e)
      {
        Log.stackTraceLog(className, "getCommandList", "", "", "Unable to close Resources due to ", e);
      }
    }

  }

  /**
   * This method reads run_command.dat and site_run_command.dat files and
   * creates ArrayList<String> for each file
   * 
   * @return
   */
  public Object[] getAllFilesCommandsList()
  {
    Log.debugLog(className, "getAllFilesCommandsList", "", "", "Method started runCommandFilePath");

    try
    {
      String path = getRunCommandFilePath() + "run_command.dat";
      Object obj[] = new Object[2];
      obj[0] = getCommandList(path);

      // if site_run_command.dat is exist than neet to load site command also in
      // command info
      path = getSiteRunCommandFilePath() + "site_run_command.dat";
      obj[1] = getCommandList(path);
      return obj;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getAllFilesCommandsList", "", "", "Exception in getAllFilesCommandsList() -", e);
      return null;
    }
  }

  /**
   * This method returns run command file path
   * 
   * @return
   */
  private String getRunCommandFilePath()
  {
    Log.debugLog(className, "getRunCommandFilePath", "", "", "Method started runCommandFilePath = " + runCommandFilePath);

    if (runCommandFilePath != null)
      return runCommandFilePath;

    String osname = System.getProperty("os.name").trim().toLowerCase();

    if (runCommandFilePath == null)
    {
      if (osname.startsWith("win"))
        runCommandFilePath = Config.getWorkPath() + "/etc";
      else
        runCommandFilePath = Config.getWorkPath() + "/etc/";
    }
    Log.debugLog(className, "getRunCommandFilePath", "", "", "returning runCommandFilePath = " + runCommandFilePath);

    return runCommandFilePath;
  }

  /**
   * This method returns site command file path
   * 
   * @return
   */
  private String getSiteRunCommandFilePath()
  {
    Log.debugLog(className, "getSiteRunCommandFilePath", "", "", "Method started runCommandFilePath = " + siteRunCommandFilePath);

    if (siteRunCommandFilePath != null)
      return siteRunCommandFilePath;

    String osname = System.getProperty("os.name").trim().toLowerCase();

    if (siteRunCommandFilePath == null)
    {
      if (osname.startsWith("win"))
        siteRunCommandFilePath = Config.getWorkPath() + "/sys/";
      else
        siteRunCommandFilePath = Config.getWorkPath() + "/sys/";
    }

    Log.debugLog(className, "getSiteRunCommandFilePath", "", "", "returning siteRunCommandFilePath = " + siteRunCommandFilePath);

    return siteRunCommandFilePath;
  }

  /**
   * This method gets commands list
   * 
   * @param uniqueCmdLineList
   * @param path
   * @return
   */
  private ArrayList<String> getCommandList(String path)
  {
    Log.debugLog(className, "getCommandsList", "", "", "Method Started.");
    ArrayList<String> tmpList = new ArrayList<String>();
    FileInputStream fis = null;
    BufferedReader br = null;
    try
    {
      File inputFile = new File(path);

      if (inputFile.exists())
      {
        fis = new FileInputStream(inputFile);

        br = new BufferedReader(new InputStreamReader(fis));

        String strLine = "";

        while ((strLine = br.readLine()) != null)
        {
          if (!strLine.trim().equals(""))
          {
            if (strLine.startsWith("#"))
              continue;
            tmpList.add(strLine);
          }
        }
        br.close();
        fis.close();
      }
      else
        Log.debugLog(className, "getCommandList", "", "", "File doesn't exits at path" + path);

    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getCommandList", "", "", "Exception in getCommnList() -", e);
    }
    finally
    {
      try
      {
        br.close();
        fis.close();
      }
      catch (Exception e)
      {
        Log.stackTraceLog(className, "getCommandList", "", "", "Unable to close Resources due to ", e);
      }
    }
    return tmpList;
  }
}

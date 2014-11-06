/**
 * Name : DashboardSettingBean.java
 * 
 * Author : Rohit Jha
 * 
 * Purpose : To update config.ini file 
 * 
 * Modification History:
 * 
 * 24/07/2014 - Initial Version
 * 
 */
package pac1.Bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Vector;

public class DashboardSettingBean implements Serializable
{
  private static final long serialVersionUID = -1137540018628363771L;
  private static String className = "DashboardSettingBean";

  // This is for storing percentile numbers for Dashboard GUI
  private String percentileNumberForDashboard = null;

  // this is for storing percentile numbers for transaction GUI
  private String percentileNumberForTransaction = null;

  // This is for storing the interval for which percentile graph will refresh in Dashboard GUI
  private String refreshDuration = "5";

  // This is for Storing Alert main schedule interval
  private String alertScheduleInterval = null;

  // This is for storing Alert mail existance
  private String alertMail = null;

  // This is for storing the Alert mail host
  private String alertMainHost = null;

  // This is for storing the Alert mail port
  private String alertMailPort = null;

  // This is for storing the Alert mail destination
  private String alertMailDestination = null;

  // This is for storing the Alert mail sender
  private String alertMailSeder = null;

  // This is for storing the Alert mail password
  private String alertMailPass = null;

  // This is for Storing the Alert mail wait time
  private String alertMailWaitTime = null;

  // This is for Storing the destination to reply
  private String alertMailReplyDestination = null;

  // This is for storing the script Runlogic
  private String scriptRunlogic = null;

  public String getAlertScheduleInterval()
  {
    return alertScheduleInterval;
  }

  public void setAlertScheduleInterval(String alertScheduleInterval)
  {
    this.alertScheduleInterval = alertScheduleInterval;
  }

  public String getAlertMail()
  {
    return alertMail;
  }

  public void setAlertMail(String alertMail)
  {
    this.alertMail = alertMail;
  }

  public String getAlertMainHost()
  {
    return alertMainHost;
  }

  public void setAlertMainHost(String alertMainHost)
  {
    this.alertMainHost = alertMainHost;
  }

  public String getAlertMailPort()
  {
    return alertMailPort;
  }

  public void setAlertMailPort(String alertMailPort)
  {
    this.alertMailPort = alertMailPort;
  }

  public String getAlertMailDestination()
  {
    return alertMailDestination;
  }

  public void setAlertMailDestination(String alertMailDestination)
  {
    this.alertMailDestination = alertMailDestination;
  }

  public String getAlertMailSeder()
  {
    return alertMailSeder;
  }

  public void setAlertMailSeder(String alertMailSeder)
  {
    this.alertMailSeder = alertMailSeder;
  }

  public String getAlertMailPass()
  {
    return alertMailPass;
  }

  public void setAlertMailPass(String alertMailPass)
  {
    this.alertMailPass = alertMailPass;
  }

  public String getAlertMailWaitTime()
  {
    return alertMailWaitTime;
  }

  public void setAlertMailWaitTime(String alertMailWaitTime)
  {
    this.alertMailWaitTime = alertMailWaitTime;
  }

  public String getAlertMailReplyDestination()
  {
    return alertMailReplyDestination;
  }

  public void setAlertMailReplyDestination(String alertMailReplyDestination)
  {
    this.alertMailReplyDestination = alertMailReplyDestination;
  }

  public String getScriptRunlogic()
  {
    return scriptRunlogic;
  }

  public void setScriptRunlogic(String scriptRunlogic)
  {
    this.scriptRunlogic = scriptRunlogic;
  }

  // This is for debug level
  private int debugLevel = 0;

  // This is for new keyword to add in config.ini file
  private transient Vector<String> newKeywordContent = new Vector<String>();

  // This is for reading the file content of config.ini
  private transient Vector<String> configFileContent = new Vector<String>();

  // File owner name
  private String userName = "netstorm";

  public DashboardSettingBean(int debugLevel)
  {
    this.debugLevel = debugLevel;
  }

  public String getUserName()
  {
    return userName;
  }

  public void setUserName(String userName)
  {
    this.userName = userName;
  }

  public String getPercentileNumberForDashboard()
  {
    return percentileNumberForDashboard;
  }

  public void setPercentileNumberForDashboard(String percentileNumberForDashboard)
  {
    this.percentileNumberForDashboard = percentileNumberForDashboard;
  }

  public String getPercentileNumberForTransaction()
  {
    return percentileNumberForTransaction;
  }

  public void setPercentileNumberForTransaction(String percentileNumberForTransaction)
  {
    this.percentileNumberForTransaction = percentileNumberForTransaction;
  }

  public String getRefreshDuration()
  {
    return refreshDuration;
  }

  public void setRefreshDuration(String refreshDuration)
  {
    this.refreshDuration = refreshDuration;
  }

  public int getDebugLevel()
  {
    return debugLevel;
  }

  public void setDebugLevel(int debugLevel)
  {
    this.debugLevel = debugLevel;
  }

  /**
   * This method checks whether keyword is available or not
   * 
   * @param line
   * @param key
   * @param debugLevel
   * @return
   */
  public static boolean checkLineCompatable(String line, String key, int debugLevel)
  {
    if (debugLevel > 0)
      Log.debugLogAlways(className, "checkLineCompatable", "", "", "Method Called. line = " + line + ", key = " + key);

    if (line.indexOf(key) == -1)
      return false;

    String[] str = line.split("=");
    if (!str[0].trim().equals(key))
      return false;

    return true;
  }

  private String getConfigFilePath()
  {
    return Config.getWorkPath() + "/webapps/sys/config.ini";
  }

  /**
   * This is method for update each keyword value in config.ini
   */
  public void updateConfigFile()
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "updateConfigFile", "", "", "Method Called.");

      String fileNameWithPath = getConfigFilePath();

      configFileContent = readFileInVector(fileNameWithPath, debugLevel);

      // update percentile numbers for dashboard GUI
      String keyword = null;
      String keywordValue = null;

      /**************** Updating Dashboard Percentile Keyword Value *****************/
      keyword = ConfigKeywordDefinition.PERCENTILE_KEYWORD_DASHBOARD;
      keywordValue = percentileNumberForDashboard;

      if (keyword != null && keywordValue != null)
        updateConfigKeyword(keyword, keywordValue, configFileContent);

      /**************** Updating Transaction Percentile Keyword Value *****************/
      keyword = ConfigKeywordDefinition.PERCENTILE_KEYWORD_TRANSACTION;
      keywordValue = percentileNumberForTransaction;

      if (keyword != null)
        updateConfigKeyword(keyword, keywordValue, configFileContent);

      /**************** Updating Dashboard Percentile Graph Refresh Interval **********/
      keyword = ConfigKeywordDefinition.PERCENTILE_GRAPH_UPDATE_DASHBOARD;
      keywordValue = refreshDuration;

      if (keyword != null && keywordValue != null)
        updateConfigKeyword(keyword, keywordValue, configFileContent);

      /**************** Updating **********/
      keyword = ConfigKeywordDefinition.ALERT_SCHEDULE_INTERVAL;
      keywordValue = alertScheduleInterval;

      if (keyword != null && keywordValue != null)
        updateConfigKeyword(keyword, keywordValue, configFileContent);

      /**************** Updating Dashboard Percentile Graph Refresh Interval **********/
      keyword = ConfigKeywordDefinition.ALERT_EMAIL;
      keywordValue = alertMail;

      if (keyword != null && keywordValue != null)
        updateConfigKeyword(keyword, keywordValue, configFileContent);

      /**************** Updating Dashboard Percentile Graph Refresh Interval **********/
      keyword = ConfigKeywordDefinition.ALERT_EMAIL_HOST;
      keywordValue = alertMainHost;

      if (keyword != null && keywordValue != null)
        updateConfigKeyword(keyword, keywordValue, configFileContent);

      /**************** Updating Dashboard Percentile Graph Refresh Interval **********/
      keyword = ConfigKeywordDefinition.ALERT_EMAIL_PORT;
      keywordValue = alertMailPort;

      if (keyword != null && keywordValue != null)
        updateConfigKeyword(keyword, keywordValue, configFileContent);

      /**************** Updating Dashboard Percentile Graph Refresh Interval **********/
      keyword = ConfigKeywordDefinition.ALERT_EMAIL_TO;
      keywordValue = alertMailDestination;

      if (keyword != null && keywordValue != null)
        updateConfigKeyword(keyword, keywordValue, configFileContent);

      /**************** Updating Dashboard Percentile Graph Refresh Interval **********/
      keyword = ConfigKeywordDefinition.ALERT_EMAIL_FROM;
      keywordValue = alertMailSeder;

      if (keyword != null && keywordValue != null)
        updateConfigKeyword(keyword, keywordValue, configFileContent);

      /**************** Updating Dashboard Percentile Graph Refresh Interval **********/
      keyword = ConfigKeywordDefinition.ALERT_EMAIL_PASSWORD;
      keywordValue = alertMailPass;

      if (keyword != null && keywordValue != null)
        updateConfigKeyword(keyword, keywordValue, configFileContent);

      /**************** Updating Dashboard Percentile Graph Refresh Interval **********/
      keyword = ConfigKeywordDefinition.WAIT_TIME;
      keywordValue = alertMailWaitTime;

      if (keyword != null && keywordValue != null)
        updateConfigKeyword(keyword, keywordValue, configFileContent);

      /**************** Updating Dashboard Percentile Graph Refresh Interval **********/
      keyword = ConfigKeywordDefinition.ALERT_EMAIL_REPLY_TO;
      keywordValue = alertMailReplyDestination;

      if (keyword != null && keywordValue != null)
        updateConfigKeyword(keyword, keywordValue, configFileContent);

      /**************** Updating Dashboard Percentile Graph Refresh Interval **********/
      keyword = ConfigKeywordDefinition.SCRIPT_RUNLOGIC;
      keywordValue = scriptRunlogic;

      if (keyword != null && keywordValue != null)
        updateConfigKeyword(keyword, keywordValue, configFileContent);

      /******************* Update Config.ini file *************************/
      writeConfigFile();
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "updateConfigFile", "", "", "Exception - ", e);
    }
  }

  /**
   * This method update keyword value in config.ini vector
   * 
   * @param keyword
   * @param keywordValue
   * @param fileContent
   */
  private void updateConfigKeyword(String keyword, String keywordValue, Vector<String> fileContent)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLog(className, "updateConfigKeyword", "", "", "Method Called. keyword = " + keyword + ", keywordValue = " + keywordValue);

      boolean isNewKeyWord = true;
      String keywordLine = null;

      int totalLinesInFile = 0;
      if (fileContent != null)
        totalLinesInFile = fileContent.size();

      for (int i = 0; i < totalLinesInFile; i++)
      {
        try
        {
          String line = fileContent.get(i);

          if (line == null || line.length() == 0)
            continue;
          else if (keyword == null || keyword.trim().length() == 0)
            continue;
          else if (line.startsWith("#"))
            continue;

          boolean isKeywordExist = checkLineCompatable(line, keyword, debugLevel);

          keywordLine = keyword + " = " + keywordValue;
          Log.debugLog(className, "updateConfigKeyword", "", "", "isKeywordExist = " + isKeywordExist);
          if (isKeywordExist)
          {
            if (keywordValue == null)
              configFileContent.remove(i);
            else
              configFileContent.set(i, keywordLine);

            isNewKeyWord = false;
          }
        }
        catch (Exception e)
        {
          Log.stackTraceLog(className, "updateConfigKeyword", "", "", "Exception - ", e);
        }
      }

      if (isNewKeyWord && keywordValue != null)
      {
        if (newKeywordContent == null)
          newKeywordContent = new Vector<String>();

        newKeywordContent.add(keywordLine);
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "modifiedPercentileValues", "", "", "Exception - ", e);
    }
  }

  /**
   * This method is for writing config.ini file
   */
  private void writeConfigFile()
  {
    try
    {
      Log.debugLogAlways(className, "writeConfigFile", "", "", "*******newKeywordContent" + newKeywordContent);
      if (debugLevel > 0)
        Log.debugLogAlways(className, "writeConfigFile", "", "", "Method Called.");

      File writeFile = new File(getConfigFilePath());
      writeFile.createNewFile();

      // Change file permission
      rptUtilsBean.changeFilePerm(writeFile.getAbsolutePath(), userName, userName, "775");

      FileWriter fileWriter = new FileWriter(writeFile);

      int totalLinesInOldFile = 0;
      if (configFileContent != null)
        totalLinesInOldFile = configFileContent.size();

      for (int i = 0; i < totalLinesInOldFile; i++)
      {
        String lineToWrite = configFileContent.get(i) + "\n";
        fileWriter.write(lineToWrite);
      }

      int totalLinesInNewFile = 0;
      if (newKeywordContent != null)
        totalLinesInNewFile = newKeywordContent.size();

      for (int i = 0; i < totalLinesInNewFile; i++)
      {
        String lineToWrite = newKeywordContent.get(i) + "\n";
        fileWriter.write(lineToWrite);
      }

      fileWriter.close();
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "writeConfigFile", "", "", "Exception - ", e);
    }
  }

  /**
   * Don't delete this method as We are not doing anything here, just we are reading file line by line
   * 
   * @param fileNameWithPath
   * @param debugLevel
   * @return
   */
  public static Vector<String> readFileInVector(String fileNameWithPath, int debugLevel)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLog(className, "readFileInVector", "", "", "Method called. File Name = " + fileNameWithPath);

      Vector<String> vecFileLines = new Vector<String>();

      String strLine;

      File fileName = new File(fileNameWithPath);

      if (!fileName.exists())
      {
        Log.debugLog(className, "readFileInVector", "", "", "File " + fileNameWithPath + " does not exist.");
        String path = fileNameWithPath;
        int i = path.lastIndexOf("/");
        String chekDir = path.substring(0, i);

        File file = new File(chekDir);
        if (!file.exists())
        {
          boolean success = new File(chekDir).mkdir();
          if (success)
            Log.debugLog(className, "readFileInVector", "", "", "dir " + chekDir + " created.");
        }
        return null;
      }

      FileInputStream fis = new FileInputStream(fileNameWithPath);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));

      while ((strLine = br.readLine()) != null)
      {
        strLine = strLine.trim();
        vecFileLines.add(strLine);
      }

      br.close();
      fis.close();

      return vecFileLines;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "readFileInVector", "", "", "Exception - ", e);
      return null;
    }
  }

  public String toString()
  {
    return "percentileNumberForDashboard = " + percentileNumberForDashboard + ", percentileNumberForTransaction = " + percentileNumberForTransaction + ", refreshDuration = " + refreshDuration + ", alertScheduleInterval = " + alertScheduleInterval + ", alertMail = " + alertMail + ", alertMainHost = " + alertMainHost + ", alertMailPort = " + alertMailPort + ", alertMailDestination = " + alertMailDestination + ", alertMailSeder = " + alertMailSeder + ", alertMailPass = " + alertMailPass + ", alertMailReplyDestination = " + alertMailReplyDestination + ", alertMailWaitTime = " + alertMailWaitTime + ", scriptRunlogic = " + scriptRunlogic;
  }
}

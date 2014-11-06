package pac1.Bean;


import java.util.*;
import java.io.*;

public class CustomizeLog
{
  private String debugLogFileName = "NA";
  private String errorLogFileName = "NA";
  private long logFileSize = 0;
  private String testRun = "NA";


  public CustomizeLog(String debugLogFileName, String errorLogFileName)
  {
    this.debugLogFileName = debugLogFileName;
    this.errorLogFileName = errorLogFileName;
  }

  private String getDateTime()
  {
    Calendar cal = Calendar.getInstance();
    String dateTime = (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DAY_OF_MONTH) + " " +
        cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND);

    return dateTime;
  }

  private String getLogFileName(String fileName)
  {
    // return (cmonConfig.logFilePrefix + fileName);
    return (fileName);
  }

  public String getLogFilePath()
  {
    String logFilePath = Config.workPath + "/webapps";
   
    if(logFilePath.length() == 0)
      logFilePath = System.getProperty("user.dir");
    
    logFilePath =  logFilePath + "/logs/TR" + testRun +"/ns_logs/" ;
    
    File logFilePathDir = new File(logFilePath);
    
    if(!logFilePathDir.exists())
      logFilePathDir.mkdirs();
    
    ChangeOwnerOfFile(logFilePathDir.getAbsolutePath(), "netstorm");
    
    return(logFilePath);
  }

  private boolean ChangeOwnerOfFile(String filePath, String owner)
  {
    try
    {
      Runtime r = Runtime.getRuntime();
      String strCmd = "chown" + " " + owner + "." + "netstorm" + " " + filePath;
      Process changePermissions = r.exec(strCmd);
      int exitValue = changePermissions.waitFor();

      if (exitValue == 0)
        return true;
      else
        return false;
    }
    catch (Exception e)
    {
      return false;
    }
  }
  public void debugLog(String className, String method, String future1, String future2, String text)
  {
    String debugFlag = Config.getValue("debugFlag");
    if(!debugFlag.equals("on"))
      return;

    debugLogAlways(className, method, future1, future2, text);
  }

  public void debugLogStackTrace(String className, String method, String future1, String future2, String text, Exception e)
  {
    String debugFlag = Config.getValue("debugFlag");
    if(!debugFlag.equals("on"))
      return;

    StringWriter str = new StringWriter();
    PrintWriter writer = new PrintWriter(str);
    e.printStackTrace(writer);
    text = text + str.getBuffer().toString();
    
    debugLogAlways(className, method, future1, future2, text);
  }

  public void debugLogAlways(String className, String method, String future1, String future2, String text)
  {
    String logFileName = getLogFilePath() + "/" + getLogFileName(debugLogFileName);
    writeToFile(logFileName, "Debug", getDateTime() + "|" + testRun + "|" + className + "|" + method + "|" + text);
  }


  public void errorLog(String className, String method, String future1, String future2, String text)
  {
    String logFileName = getLogFilePath() + "/" + getLogFileName(errorLogFileName);
    writeToFile(logFileName, "Error", getDateTime() + "|" + testRun + "|" + className + "|" + method + "|" + text);
  }


  public void stackTraceLog(String className, String method, String future1, String future2, String text, Exception e)
  {
    try
    {
      String logFileName = getLogFilePath() + "/" + getLogFileName(errorLogFileName);
      writeToFile(logFileName, "Error", getDateTime() + "|" + testRun + "|" + className + "|" + method + "|" + text + "|" + "Following is the stack trace:");

      File curLogFileObj = new File(logFileName);
      FileOutputStream fout = new FileOutputStream(curLogFileObj, true);  // Append mode
      PrintStream printStream = new PrintStream(fout);
      e.printStackTrace(printStream);
      printStream.close();
      fout.close();
    }
    catch(Exception ee)
    {
      System.out.println(text);
      ee.printStackTrace();
    }
  }

  private long getMaxLogSize()
  {
    //long logFileSize = Long.parseLong(cmonConfig.getValue("maxLogSize"));
    if(logFileSize == 0)
      logFileSize = 10 * 1024 *1024; // 10 MB

     return(logFileSize);
  }

  private void writeToFile(String logFileName, String errOrDebug, String text)
  {
    try
    {
      File curLogFileObj = new File(logFileName);
      
      if(!curLogFileObj.exists())
    	  curLogFileObj.createNewFile();
      
      FileOutputStream fout = new FileOutputStream(curLogFileObj, true);  // Append mode
      PrintStream printStream = new PrintStream(fout);
      printStream.println(text);
      printStream.close();
      fout.close();
      // if file size is more than max, rename this file to .prev file
      if(curLogFileObj.length() > getMaxLogSize())
      {
        File prevLogFileObj =  new File(logFileName + ".prev");
        if(prevLogFileObj.exists())
          prevLogFileObj.delete();

        if(curLogFileObj.renameTo(prevLogFileObj) == false)
          System.out.println("ERROR in renaming log file to prev file");
        fout = new FileOutputStream(curLogFileObj);  // Open in non Append mode
        fout.close();
      }

    }
    catch(Exception e)
    {
      if(errOrDebug.equals("Debug"))
         errorLog("Log", "writeToFile", "", "", "Exception - " + e);
      else
        System.out.println(text);
    }
  }


  // this is for set the test run
  public void setTestRun(String testRun)
  {
    this.testRun = testRun;
  }

  // this is for get the test run
  public String getTestRun()
  {
    return testRun;
  }
  
  /* main is for testing only */
  public static void main(String[] args)
  {
    CustomizeLog cmonLog = new CustomizeLog("testDebug.log", "testError.log");

    cmonLog.debugLog("Log", "main", "", "", "Test Debug log");
    cmonLog.errorLog("Log", "main", "", "", "Test Error log");

  }
}


/***************************************************************************
  1. Log file names are changed to different names due to permission issue
     All bean methods run under root and create log file with owner root
     Since RTG Server run as netstorm, it is not able to write into log files

  On Jun 24, 2007, this file is modified so that debugLog and errorLog can be used
     from client code also So we should use these methods in client.
     (Not debugLogClient and errorLogClient)
***************************************************************************/
package pac1.Bean;
import java.util.*;
import java.io.*;
import pac1.Bean.*;
import java.text.SimpleDateFormat;

public class Log
{

  public static String getDateTime()
  {
	//Chnages done to get the date time in specific format
    Calendar currentDate = Calendar.getInstance();
    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    String dateTime = formatter.format(currentDate.getTime());

    //It was the earleier method to get the current date time
    //Calendar cal = Calendar.getInstance();
    //String dateTime = (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DAY_OF_MONTH) + " " + cal.get(Calendar.HOUR) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND);
    return dateTime;
  }

  private static String getLogFileName(String fileName)
  {
    return (Config.logFilePrefix + fileName);
  }

  public static String getLogFilePath()
  {
    String logFilePath = Config.getValueWithPath("logFilePath");
    if(logFilePath.length() == 0)
      logFilePath = System.getProperty("user.dir");
    return(logFilePath);
  }

  public static void debugLog(String className, String method, String future1, String future2, String text)
  {
    String debugFlag = Config.getValue("debugFlag");
    if(!debugFlag.equals("on"))
      return;
    if(!(Config.logFilePrefix.equals("")))
      debugLogAlways(className, method, future1, future2, text);
    else
      debugLogClientAlways(className, method, future1, future2, text);
  }

  public static void debugLogAlways(String className, String method, String future1, String future2, String text)
  {
    if(!(Config.logFilePrefix.equals("")))
    {
      String logFileName = getLogFilePath() + "/" + getLogFileName("Debug.log");
      writeToFile(logFileName, "Debug", getDateTime() + "|" + className + "|" + method + "|" + text);
    }
    else
      debugLogClientAlways(className, method, future1, future2, text);

  }

  public static void debugLogProfile(String className, String method, String future1, String future2, String text)
  {
    String debugProfileFlag = Config.getValue("netstorm.execution.profileMode");
    if(!debugProfileFlag.equals("0"))
    {
      if(!(Config.logFilePrefix.equals("")))
      {  
        String logFileName = getLogFilePath() + "/" + getLogFileName("Debug.log");
        writeToFile(logFileName, "Debug", getDateTime() + "|" + className + "|" + method + "|" + text);
      }
      else
        debugLogClientAlways(className, method, future1, future2, text);
    }
  }

  /* Used by client - To be removed once client code is changed to use debugLog() */
  public static void debugLogClient(String className, String method, String future1, String future2, String text)
  {
    String debugFlag;

    debugFlag = Config.getValue("debugFlag");
    if(!debugFlag.equals("on"))
      return;

    debugLogClientAlways(className, method, future1, future2, text);
  }

  public static void debugLogClientAlways(String className, String method, String future1, String future2, String text)
  {
    System.out.println(getDateTime() + "|" + className + "|" + method + "|" + text);
  }


  public static void errorLog(String className, String method, String future1, String future2, String text)
  {
    if(!(Config.logFilePrefix.equals("")))
    {
      String logFileName = getLogFilePath() + "/" + getLogFileName("Error.log");
      writeToFile(logFileName, "Error", getDateTime() + "|" + className + "|" + method + "|" + text);
    }
    else
      errorLogClient(className, method, future1, future2, text);
  }

  /* Used by client - To be removed once client code is changed to use errorLog() */
  public static void errorLogClient(String className, String method, String future1, String future2, String text)
  {
    System.out.println(getDateTime() + "|" + className + "|" + method + "|" + text);
  }

  public static void stackTraceLog(String className, String method, String future1, String future2, String text, Exception e)
  {
    try
    {
      if(!(Config.logFilePrefix.equals("")))
      {
        String logFileName = getLogFilePath() + "/" + getLogFileName("Error.log");
        writeToFile(logFileName, "Error", getDateTime() + "|" + className + "|" + method + "|" + text + "|" + "Following is the stack trace:");

        File curLogFileObj = new File(logFileName);
        FileOutputStream fout = new FileOutputStream(curLogFileObj, true);  // Append mode
        PrintStream printStream = new PrintStream(fout);
        e.printStackTrace(printStream);
        printStream.close();
        fout.close();
      }
      else
      {
        System.out.println(getDateTime() + "|" + className + "|" + method + "|" + text + "|" + "Following is the stack trace:");
        e.printStackTrace();
      }
    }
    catch(Exception ee)
    {
      System.out.println(text);
      ee.printStackTrace();
    }
  }

  public static long getMaxLogSize()
  {
    long logFileSize = Long.parseLong(Config.getValue("maxLogSize"));
    if(logFileSize == 0)
      logFileSize = 1024 *1024;

     return(logFileSize);
  }

  public static void msgLog(String className, String method, String future1, String future2, String text)
  {

    String msgDebugFlag = Config.getValue("msgDebugFlag");
    if(!msgDebugFlag.equals("on"))
      return;

    String logFileName = getLogFilePath() + "/" + getLogFileName("msg.log");
    writeToFile(logFileName, "Debug", getDateTime() + "|" + className + "|" + method + "|" + text);
  }

  public static void cmdLog(String className, String method, String cmdFlag, String future2, String text)
  {
    String debugFlag = Config.getValue("cmdDebugFlag");
    if(!debugFlag.equals("on"))
      return;

    String logFileName = getLogFilePath() + "/" + getLogFileName("CmdDebug.log");
    if(cmdFlag.equals("Start"))
      writeToFile(logFileName, "Debug", "Start|" + getDateTime() + "|" + text);
    else if(cmdFlag.equals("End"))
      writeToFile(logFileName, "Debug", "End|" + getDateTime() + "|" + text);
    else
      writeToFile(logFileName, "Debug", text);
  }

  public static void writeToFile(String logFileName, String errOrDebug, String text)
  {
    try
    {
      File curLogFileObj = new File(logFileName);
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
         Log.errorLog("Log", "writeToFile", "", "", "Exception - " + e);
      // these line are commented because when the file dose have write permisson it prints on console and comes in page list.
      //else
        //System.out.println(text);
    }
  }


  public static void reportDebugLog(String className, String method, String future1, String future2, String text)
  {
//    String reportDebug = Config.getValue("reportDebugFlag");
//    if(!reportDebug.equals("on"))
//      return;

    String logFileName = getLogFilePath() + "/reportDebug.log";
    writeToFile(logFileName, "Debug", getDateTime() + "|" + className + "|" + method + "|" + text);
  }


  /* main is for testing only */
  public static void main(String[] args)
  {
    //Debug rp=new Debug();
    Log.debugLog("Log", "main", "", "", "Test Debug log");
    Log.errorLog("Log", "main", "", "", "Test Error log");

    Log.debugLogClient("Log", "main", "", "", "Test Debug log from Client");
    Log.errorLogClient("Log", "main", "", "", "Test Error log from Client");
  }
}

/*--------------------------------------------------------------------
  @Name    : CmdExec.java
  @Author  : Atul
  @Purpose : To execute the command

  @Modification History: 17/01/09 - Initial version

----------------------------------------------------------------------*/

package pac1.Bean;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Vector;


public class CmdExec
{
  private static String className = "CmdExec";
  static String arrCommandName[] = new String[]
                                  {
                                    "nsi_change_perm"
                                  };// This data structure is used to store's command which must run under root.
  static int reqId = 1; // Neeraj:02/13/05 - This is used to make wrapper name unique

  public static final int SYSTEM_CMD = 1;
  public static final int NETSTORM_CMD = 0;
  public static final int HPD_CMD = 2;
  public int exitCommandValue = 0;
  private String osname;
  private String commandPath;// path to bin no need to append for SYSTEM type command
  private String hpdCommandPath;// path to $HPD_ROOT/bin no need to append for SYSTEM type command
  
  
  public CmdExec()
  {
     osname = System.getProperty("os.name").trim().toLowerCase();
     commandPath = Config.getValueWithPath("commandPath");
     hpdCommandPath = Config.getValueWithHPDPath("commandPath");
  }

  private void logCmdExecError(Vector vecCmdOutput, String cmd, String args, String errMsg, int exitValue)
  {
    Log.errorLog(className, "logCmdExecError", "", "",  errMsg + " (Command = " + cmd + " " + args + "), exit value = " + exitValue);
    for(int i = 0; i < vecCmdOutput.size(); i++)
      Log.errorLog(className, "logCmdExecError", "", "",  vecCmdOutput.get(i).toString());

    vecCmdOutput.add("ERROR|" + exitValue + "|" + errMsg + "|" + cmd + " " + args);
    // This is required to fix issue of writing cmd error output in JS arrays in JSP code
    escapeQuotes(vecCmdOutput);
  }

  // Replace single (') and double quotes (") by \' or \"
  // This is required for writting this in Java Script variable.
  public static void escapeQuotes(Vector vecInput)
  {
    String strLine;
    for(int i = 0; i < vecInput.size(); i++)
    {
      strLine = escapeQuotes(vecInput.get(i).toString());
      vecInput.set(i, strLine);
    }
  }

  // Replace single (') and double quotes (") by \' or \"
  // This is required for writting this in Java Script variable.
  public static String escapeQuotes(String str)
  {
    // first split for single quote
    String arrSplit1[] = str.split("'");
    String strSplit1 = "", strSplit2 = "";
    for(int i = 0; i < arrSplit1.length; i++)
      strSplit1 = strSplit1 + arrSplit1[i] + "\\" + "\'";
    //Remove extra \' at the end
    //If String ends with single qoute then not require to remove \' at the end
    if(!str.trim().endsWith("\'"))
      strSplit1 = strSplit1.substring(0, strSplit1.length()-2);
    // second split for double quote
    String arrSplit2[] = strSplit1.split("\"");
    for(int i = 0; i < arrSplit2.length; i++)
      strSplit2 = strSplit2 + arrSplit2[i] + "\\" + "\"";
    // Remove extra \" at the end
    // If String ends with double qoute then not require to remove \' at the end
    if(!str.trim().endsWith("\""))
      strSplit2 = strSplit2.substring(0, strSplit2.length() - 2);
    return strSplit2;
  }

  public String createCmdWrapper(String cmd, String args, int cmdType, String userName, String procName)
  {
    try
    {
      /**
       * If any command contain the '/' character in its name,
       * then '/' will be replaced with '_' for creating the wrapper file
       * name.
       *
       * This will be just for wrapper file name, not for
       * command name.
       *
       * example /bin/cp will be like - _bin_cp
       */

      String wrapperName = Config.getWorkPath() + "/webapps/netstorm/temp/" + replace(cmd, "/", "_") + "." + procName + "." + reqId;
      if(++reqId > 100)
        reqId = 1;

      /**
       * For 
       * 1- netstorm-type command NS_WDIR is needed to append,
       * but system-type command like cp, unzip, ls etc no need to
       * append the NS_WDIR
       * 2- hpd-type command HPD_ROOT is needed to append,
       * but system-type command like cp, unzip, ls etc no need to
       * append the HPD_ROOT
       *
       */

      String path = "";
      if(cmdType == NETSTORM_CMD)
        path = commandPath;
      else if(cmdType == HPD_CMD)
        path = hpdCommandPath;

      /**
       * This is to check that if userName is not there,
       * set its value depends on condition as root OR netstorm
       */
      if((userName == null) || (userName.equals("")))
      {
        if(find(cmd))
          userName = "root";
        else
          userName = "netstorm";
      }

      if(userName.equals("admin"))
        userName = "root";

      String wrapperCmd;
      File file = new File(wrapperName);
      if(file.exists())
      {
        if(file.delete() == false)
        {
          Log.errorLog(className, "createCmdWrapper", "", "", "Error in deleting existing file - " + wrapperName);
          return "";
        }
      }

      file.createNewFile();
      FileOutputStream fout = new FileOutputStream(file);
      PrintStream pw = new PrintStream(fout);
      pw.println("#!/bin/sh");

      /**
       * These are commands which must be run as root or without su username
       * RunAsProcessUser user execute the command from netstorm id.
       * Java server process command from netstorm id (in execution gui)
       * Date 28 april 2011 - Jyoti
       */
      if((userName.equals("root")) || (userName.equals("RunAsProcessUser")))
        wrapperCmd = path + cmd + " " + args + " 2>&1";
      else
      {
        //Neeraj:02/13/05 - su - netstorm causes all env variables to be lost
        wrapperCmd = "su " + userName + " -c '" + path + cmd + " " + args + " 2>&1'";
      }

      Log.debugLog(className, "createCmdWrapper", "", "", "wrapper command : " + wrapperCmd);
      pw.println("# We are changing permission to 666 so that any user can later delete this file.");
      pw.println("chmod 666 $0");
      String debugFlag = Config.getValue("debugFlag");
      if(!debugFlag.equals("on"))
      {
        // rm command will remove the wrapper after command is over
        // We are doing it in non debug mode that these files are automatically removed
        pw.println("# We are removing this wrapper so that after command is over, it will be removed.");
        pw.println("rm -f $0");
      }
      pw.println(wrapperCmd);
      pw.println("#exit the exit value of the command");
      pw.println("exit $?");
      fout.close();
      return wrapperName;
    }
    catch(Exception e)
    {
     // Log.errorLog(className, "createCmdWrapper", "", "", "Exception - " + e);
      return "";
    }
  }

//This method will used to find string in arrCommandName array
  public static boolean find(String command)
  {
    for(int i=0; i<arrCommandName.length; i++)
    {
      if(arrCommandName[i].startsWith(command))
        return true;
    }
    return false;
  }


  /****
   cmd       - name of command

   args      - argument for that command

   cmdType   - To decide that SYSTEM command OR NETSTORM command
               [0] for NETSTORM type command and [1] for SYSTEM type command

   userName  - name of user which is going to run that command
               if it is null OR empty then it will be "netstorm" OR "root"
               based on the match of cmd with commands define in arrCommandName array.

   runAsUser - if any user want to execute any command not from its id and from root OR netstorm id
               then this contain the string as root OR netstorm , If it is null then command will run by
               userName
   */

//procName is tomcat, Server


 public Vector getResultByCommand(String cmd, String args, int cmdType, String userName, String runAsUser)
 {
    return(getResultByCommand(cmd, args, cmdType, userName, runAsUser, "tomcat"));
 }

 /** This method is used to find nth occurence of a char in a string
  * 
  * @param str
  * @param c
  * @param n
  * @return
  */
 public static int nthOccurrence(String str, char c, int n)
 {
   int pos = str.indexOf(c, 0);
   while(n-- > 0 && pos != -1)
     pos = str.indexOf(c, pos + 1);
   return pos;
 }
 
 /*
  * This function is used for Auto Sensor Data
  */
 public Vector getAutoSensorData(String cmd, String args, int cmdType, String userName, String runAsUser)
 {
   Log.debugLog(className, "getAutoSensorData", "", "", "Method called.");
   
   //Vector result = getResultByCommand(cmd, args, cmdType, userName, runAsUser);
   Vector result = new Vector();

   boolean cmdStatus = getResultByCommand(result,cmd, args, cmdType, userName, runAsUser);

   if(!cmdStatus)
     return null;
   
   try
   {
     SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
     Vector finalResult = new Vector(100);
     finalResult.add(result.get(0).toString());
     for(int i = 1; i < result.size(); i++)
     {
       String resultLine = result.get(i).toString();
       if (resultLine.contains(","))
       {       
         int fourthIndex = nthOccurrence(resultLine, ',', 4);
         int FivethIndex = nthOccurrence(resultLine, ',', 5);
         
         String strSub1 = resultLine.substring(0, fourthIndex);
         String strSub2 = resultLine.substring(fourthIndex + 1, FivethIndex);
         String strSub3 = resultLine.substring(FivethIndex + 1, resultLine.length());
         String dateTime = "00/00/00 00:00:00";
         
         try
         {
           long miliseconds = Long.parseLong(strSub2.trim());
           try
           {
            // dateTime = formatter.format(miliseconds).trim();
             
            // We are taking out timezone to resolve the timezone CDT issue for hotspot gui.

             TimeZone timeZone = ExecutionDateTime.getSystemTimeZoneGMTOffset();
             dateTime = ExecutionDateTime.convertDateTimeStampToFormattedString(miliseconds, "MM/dd/yy HH:mm:ss", timeZone);
             
           }
           catch(Exception e)
           {
             Log.errorLog(className, "getAutoSensorData", "", "", "Exception in converting Millisecond to date format = " + e);
             dateTime = "00/00/00 00:00:00";
           }
           
           String newdataLine = strSub1 + "," + dateTime + "," + strSub3 + "," + strSub2;
           
           finalResult.add(newdataLine);
         }
         catch(Exception ex)
         {
           Log.errorLog(className, "getAutoSensorData", "", "", "Exception - " + ex);
         }
       }
       else
       {
    	   //Autosensor bottompanel data
    	   if(resultLine.contains("|") && resultLine.contains(":") ){
    		   finalResult.add(resultLine);
    	   }
    	   else{
             Log.errorLog(className, "getAutoSensorData", "", "", "resultLine = " + resultLine + " is not correct");
             continue;
    	   }
       }
     }
     return finalResult;
   }
   catch(Exception ex)
   {
     Log.errorLog(className, "getAutoSensorData", "", "", "Exception - " + ex);
     return result;
   }
 }
 
 private String getDateTimeFromMiliseconds(long miliseconds)
 {
   try
   {
     Log.debugLog(className, "getDateTimeFromMiliseconds", "", "", "miliseconds = " + miliseconds);
     /*long seconds = miliseconds/1000;
     long remainMs = miliseconds%1000;
     String strCmd = "date";
     String args = " -d@" + seconds + " +\"%m/%d/%y %H:%M:%S\"." + remainMs;
     Log.debugLog(className, "getDateTimeFromMiliseconds", "", "", "args = " + args);
     CmdExec cmdExec = new CmdExec();
     Vector result = cmdExec.getResultByCommand(strCmd, args, CmdExec.SYSTEM_CMD, "netstorm", "root");
     String dateTime = result.get(0).toString();
     Log.debugLog(className, "getDateTimeFromMiliseconds", "", "", "converted dateTime = " + dateTime);
     return dateTime;*/
     
     SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
     return formatter.format(miliseconds);
   }
   catch(Exception ex)
   {
     return "00/00/00 00:00:00";
   }
 }
 
  public Vector getResultByCommand(String cmd, String args, int cmdType, String userName, String runAsUser, String procName)
  {
    DataInputStream dt;
    Vector vecCmdOutput = new Vector();
    String command;
    int exitValue = 0;
    exitCommandValue = 0;
    if(runAsUser != null)
      userName = runAsUser;

    
    if(osname.startsWith("win"))
      command  = "cmd /C " +  commandPath + cmd  + " " + args;
    else
    {
      String wrapperName =  createCmdWrapper(cmd, args, cmdType, userName, procName);
      if(wrapperName.equals(""))
      {
        logCmdExecError(vecCmdOutput, cmd, args, "Error in creating command wrapper",  -1);
        return vecCmdOutput;
      }
      command  =  "sh " + wrapperName;
    }
    Log.cmdLog(className, "getResultByCommand", "Start", "", "Command :" + cmd + " " + args);
    String str;
    try
    {
      Process child  =  Runtime.getRuntime().exec(command);
      dt = new DataInputStream(child.getInputStream());
      while((str = dt.readLine()) !=  null)
      {
        Log.cmdLog(className, "getResultByCommand", "Output", "", "str: " + str);
        
        vecCmdOutput.add(str);
        // For nsu_start_test in Windows, do not wait till readLine return null as
        // it waits for sub processes started with start command to be over
        // This need to be fixed later.
        if((cmd.startsWith("nsu_start_test") && osname.startsWith("win")))
        {
          if(vecCmdOutput.size() >= 2)
            return (vecCmdOutput);
        }
      }
      try
      {
        // this will wait if cmd not over
        if((exitValue = child.waitFor()) == 0)
        {
          // System.out.println("waiting for child.exitValue() for " + cmd + ", OS is " + osname);
          if((exitValue = child.exitValue()) != 0)
            logCmdExecError(vecCmdOutput, cmd, args, "Command execution error",  exitValue);
        }
        else
        {
          logCmdExecError(vecCmdOutput, cmd, args, "Command execution error",  exitValue);
        }
        
        exitCommandValue = exitValue;
      }
      catch (InterruptedException e1)
      {
        logCmdExecError(vecCmdOutput, cmd, args, "Exception in child.waitFor() - " + e1,  998);
      }
    }
    catch(Exception e)
    {
      logCmdExecError(vecCmdOutput, cmd, args, "Exception in running command - " + e,  999);
      Log.errorLog(className, "getResultByCommand", "", "", "Exception Message  - " + e.getMessage() + " Cause = " + e.getCause());
    }
    if(!cmd.equals("nsu_upgrade_build"))
      Log.cmdLog(className, "getResultByCommand", "End", "", "Exit Value : " + exitValue);
    return vecCmdOutput;
  }

  //This is for backward compatibility, to be remove after changes in ALL jsp and java source code.
  public Vector getResultByCommand(String cmd, String args)
  {
    int cmdType;
    if(cmd.equals("cp") || cmd.equals("unzip"))
      cmdType = 1;
    else
      cmdType = 0;

    return getResultByCommand(cmd, args, cmdType, null, null);
  }
  
  /**
  * @param vecCmdOut
  * @param cmd
  * @param args
  * @param cmdType
  * @param userName
  * @param runAsUser
  * @param procName execute from server or tomcat
  * @return
  */
  public boolean getResultByCommand(Vector vecCmdOut, String cmd, String args, int cmdType, String userName, String runAsUser, String procName)
  {
    Log.debugLog(className, "getResultByCommand", "", "", "Method Starts. Command: " + cmd + ", Arguments: " + args + ", Command Type: " + cmdType + ", UserName: " + userName + ", Run as user: " + runAsUser + ", procName: " + procName);

    int result = getIntResultByCommand(vecCmdOut, cmd, args, cmdType, userName, runAsUser, procName);

    if(result == 0) 
      return true;

    return false;
  }

  /**
   *This method is copy of getResultByCommand with one more argument of Vector type
   *this will return boolean as per success OR failure
   *
   *If error came in executing that command then given vector contain the error line
   *which is thrown by the command
   */
  public boolean getResultByCommand(Vector vecCmdOut, String cmd, String args, int cmdType, String userName, String runAsUser)
  {
    Log.debugLog(className, "getResultByCommand", "", "", "Method Starts. Command: " + cmd + ", Arguments: " + args + ", Command Type: " + cmdType + ", UserName: " + userName + ", Run as user: " + runAsUser);

    int result = getIntResultByCommand(vecCmdOut, cmd, args, cmdType, userName, runAsUser, "tomcat");

    if(result == 0)
      return true;

    return false;
  }

  //return exit status of the command
  public int getIntResultByCommand(Vector vecCmdOut, String cmd, String args, int cmdType, String userName, String runAsUser, String procName)
  {
    Log.debugLog(className, "getIntResultByCommand", "", "", "Method Starts. Command: " + cmd + ", Arguments: " + args + ", Command Type: " + cmdType + ", UserName: " + userName + ", Run as user: " + runAsUser + ", procName: " + procName);
    
    int retValue;
    
    //this is to check if vector is coming null then to initialise it. As in jsp vecCmdOut is declared only not initialise.
    if(vecCmdOut == null)
      vecCmdOut = new Vector();
    
    vecCmdOut.addAll(getResultByCommand(cmd, args, cmdType, userName, runAsUser, procName));
      
    if((vecCmdOut.size() > 0) && ((String)vecCmdOut.lastElement()).startsWith("ERROR"))
    {
      Log.errorLog(className, "getResultByCommand", "", "", "Error in executing command = " + cmd);

      String errorMsg = (String)vecCmdOut.lastElement();
      Log.debugLog(className, "getIntResultByCommand", "", "", "Error Msg is: " + errorMsg);
      
      //"ERROR|" + exitValue + "|" + errMsg + "|" + cmd + " " + args
      String[] argsValue = rptUtilsBean.split(errorMsg, "|");
      retValue =  Integer.parseInt(argsValue[1]);

      vecCmdOut.remove((vecCmdOut.size() - 1));
    }
    else
      retValue = 0;
    
    Log.debugLog(className, "getIntResultByCommand", "", "", "Method Ends. Return value: " + retValue);
    return retValue; 	
  }
  
  public int getExitValue(Vector vecCmdOut)
  {
    if(vecCmdOut == null)
      return 0;
    else if(((String)vecCmdOut.lastElement()).startsWith("ERROR"))
    {
      String arr[] = rptUtilsBean.split((String)vecCmdOut.lastElement(), "|");
      return Integer.parseInt(arr[1]);
    }
    return 0;
  }

  public String getErrMsg(Vector vecCmdOut)
  {
    String str = "";
    for(int i = 0 ; i < vecCmdOut.size() - 1 ; i++)
    {
      if(i == 0)
        str = vecCmdOut.get(i).toString();
      else
        str = str + "\n" + vecCmdOut.get(i).toString();
    }

    return str;
  }

  //Create own replace method because jdk 1.4 does not support replace method for string
  public String replace(String source, String toReplace, String replaceWith)
  {
    Log.debugLog(className, "replace", "", "", "Method called. source = " + source + ", toReplace = " + toReplace + ", replaceWith = " + replaceWith);
    if (source!= null)
    {
      final int len = toReplace.length();
      StringBuffer sb = new StringBuffer();
      int found = -1;
      int start = 0;
      while((found = source.indexOf(toReplace, start) ) != -1)
      {
        sb.append(source.substring(start, found));
        sb.append(replaceWith);
        start = found + len;
      }
      sb.append(source.substring(start));
      return sb.toString();
    }
    else return "";
  }

  public int getExitValue() 
  {
    return exitCommandValue;
  }

  public void setExitValue(int exitValue)
  {
    this.exitCommandValue = exitValue;
  }

  private void printResult(Vector vec, String cmd, int type, String userName)
  {
    System.out.println("Output for cmd = " + cmd + ", user = " + userName + ", type = " + type);
    for(int i = 0 ; i < vec.size() ; i++)
      System.out.println(vec.get(i).toString());
  }

  public static void main(String args[])
  {
    CmdExec cmdExec = new CmdExec();
    String cmd = "/bin/ls";
    String user = "netstorm";
    int type = 1;

    //Case 1 : System cmd by netstorm with full path
    Vector vecOutput = cmdExec.getResultByCommand(cmd, "", type, user, "root");
    cmdExec.printResult(vecOutput, cmd, type, user);


    //Case 2 : netstorm command by netstorm
    cmd = "nsu_get_errors";
    vecOutput = cmdExec.getResultByCommand(cmd, "2", type, user, "netstorm");
    cmdExec.printResult(vecOutput, cmd, type, user);

    //Case 3 : system command by root
    cmd = "cp";
    type = 1;
    user = "root";
    vecOutput = cmdExec.getResultByCommand(cmd, "CmdExec.java /temp/", type, user, null);
    cmdExec.printResult(vecOutput, cmd, type, user);

    //Case 4 : netstorm command by netstorm wtih exit value
    cmd = "nsu_copy_script";
    type = 1;
    user = "root";
    vecOutput = cmdExec.getResultByCommand(cmd, "hpd_tours hpd_tours_delete", type, user, null);
    cmdExec.printResult(vecOutput, cmd, type, user);
  }
}

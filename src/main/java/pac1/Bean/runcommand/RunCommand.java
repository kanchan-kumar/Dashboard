 /*
  *Group Name|CommandName|Actual Command|Role|Server Type|Filter Keyword|View Type|Is Header Contains|Separator|CommandUIArgs|Max Inline Arguments|Future1|Future2|Future3|Future4|Future5|Description
  */
package pac1.Bean.runcommand;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;

import pac1.Bean.CmdExec;
import pac1.Bean.Config;
import pac1.Bean.Log;
import pac1.Bean.rptUtilsBean;


public class RunCommand 
{
  private static String className = "RunCommand";
  private static String traceLogFilePath = "";
  private static String  runCommandFilePath;
  private static String  siteRunCommandFilePath;
  private static int MaxlogFileSize = 10 * 1024 * 1024;// 10 MB for maximum log file size
  ArrayList<RunCommandDTO> outputList = new ArrayList<RunCommandDTO>();
  ArrayList<RunCommandDTO> runCmdList = null;
  boolean isAllCommandRun = false;
  
  public RunCommand()
  {
    traceLogFilePath =  Config.getWorkPath() + "/logs/.run_command/run_command.logs";
  } 
  
  private String getRunCommandFilePath()
  {
     Log.debugLog(className, "getRunCommandFilePath", "", "", "Method started runCommandFilePath = " + runCommandFilePath);
    
     if(runCommandFilePath != null)
        return runCommandFilePath;

     String osname = System.getProperty("os.name").trim().toLowerCase();
        
     if(runCommandFilePath == null)
     {
       if(osname.startsWith("win"))
         runCommandFilePath = Config.getWorkPath() + "/etc";
       else
         runCommandFilePath = Config.getWorkPath() + "/etc/";
     }
     Log.debugLog(className, "getRunCommandFilePath", "", "", "returning runCommandFilePath = " + runCommandFilePath);
    
     return runCommandFilePath;
   }
  
  private String getSiteRunCommandFilePath()
  {
     Log.debugLog(className, "getSiteRunCommandFilePath", "", "", "Method started runCommandFilePath = " + siteRunCommandFilePath);
    
     if(siteRunCommandFilePath != null)
        return siteRunCommandFilePath;

     String osname = System.getProperty("os.name").trim().toLowerCase();
        
     if(siteRunCommandFilePath == null)
     {
       if(osname.startsWith("win"))
         siteRunCommandFilePath = Config.getWorkPath() + "/sys/";
       else
         siteRunCommandFilePath = Config.getWorkPath() + "/sys/";
     }
     
     Log.debugLog(className, "getSiteRunCommandFilePath", "", "", "returning siteRunCommandFilePath = " + siteRunCommandFilePath);
    
     return siteRunCommandFilePath;
  }
  
  public void runCmd(int cmdIndex)
  {
    Log.debugLog(className, "runCmd", "", "", "Method Started.");
    RunCommandDetailInfoDTO cmdInfoDto = null;
    RunCommandDTO runCmdDTOObj = null;
    try
    {
      Vector vecCmdOut = new Vector();
       
      runCmdDTOObj = runCmdList.get(cmdIndex);
      Log.debugLog(className, "runCmd", "", "", "Command Details -> " + runCmdDTOObj.toString());
      cmdInfoDto = runCmdDTOObj.getRunCmdInfoDto();
      long startProcessingTime = System.currentTimeMillis();
      
      String strCmdArgs = "";
      strCmdArgs = strCmdArgs + " -g -s " + runCmdDTOObj.getServerName() + " -u " + runCmdDTOObj.getUserName() + " -i ";
      strCmdArgs = strCmdArgs +  " -c " + " \"" + cmdInfoDto.getActualCommand() + " " + cmdInfoDto.getActualCmdArgs()+ "\" ";
      
      if(!runCmdDTOObj.isUserDefindCommad())
      {
        if(!cmdInfoDto.isColumnContains())
        {
          if(!runCmdDTOObj.getSubOutputOption().equals(""))
          {
           strCmdArgs = strCmdArgs + " | " + runCmdDTOObj.getSubOutputOption() + " -" + runCmdDTOObj.getSubOutputValue();
          }
          if(!cmdInfoDto.getSearchKeyword().trim().equals("NA"))
          {
            strCmdArgs = strCmdArgs + " | grep " + cmdInfoDto.getSearchKeyword().trim();
          }
          if(!runCmdDTOObj.getFilerKeyword().trim().equals("NA"))
          {
            strCmdArgs = strCmdArgs + " | grep " + runCmdDTOObj.getFilerKeyword();
          }
        }
        else
        {
          if(!runCmdDTOObj.getSubOutputOption().equals(""))
          {
           strCmdArgs = strCmdArgs + " | ( read -r head; printf '%s\n' \"$head\"; " + runCmdDTOObj.getSubOutputOption() + " -" 
           + runCmdDTOObj.getSubOutputValue()+"; ) ";
          }
          if(!cmdInfoDto.getSearchKeyword().trim().equals("NA"))
          {
            strCmdArgs = strCmdArgs + " | ( read -r head; printf '%s\n' \"$head\"; grep " + cmdInfoDto.getSearchKeyword().trim() +"; ) ";
          }
          if(!runCmdDTOObj.getFilerKeyword().trim().equals("NA"))
          {
            strCmdArgs = strCmdArgs + " | ( read -r head; printf '%s\n' \"$head\"; grep  " + runCmdDTOObj.getFilerKeyword() + "; ) ";
          } 
        }
      }
      String strCmdName = "nsu_server_admin";
      CmdExec cmdExec = new CmdExec();
      vecCmdOut = cmdExec.getResultByCommand(strCmdName, strCmdArgs, 0, runCmdDTOObj.getUserName(), "root");
      
      runCmdDTOObj.setExitValue(cmdExec.getExitValue());
      
      if(((vecCmdOut.size() > 0) && ((String)vecCmdOut.lastElement()).startsWith("ERROR")) || cmdExec.getExitValue()!= 0)
      {
        //traceLog( runCmdDTOObj.getUserName(), runCmdDTOObj.getServerName(), cmdInfoDto.getActualCommand(), "Fail");
        runCmdDTOObj.setStatus(false);
        if(vecCmdOut != null && vecCmdOut.size() > 0)
          vecCmdOut.remove(vecCmdOut.size() - 1);
        
        runCmdDTOObj.setErrMsg(vecCmdOut);
        runCmdDTOObj.setServerTime(rptUtilsBean.getCurDateTime());
        Log.debugLog(className, "runCmd", "", "","Got error and Run Command Output Details -> " + runCmdDTOObj.toString());
        outputList.add(runCmdDTOObj);
        
        if(runCmdList.size() == outputList.size())
          isAllCommandRun = true;
        return ;
      }
      
      //traceLog(runCmdDTOObj.getUserName(), runCmdDTOObj.getServerName(), cmdInfoDto.getActualCommand(), "Pass");
      
      String totalTime = rptUtilsBean.convertMilliSecToSecs(System.currentTimeMillis() - startProcessingTime);
      Log.debugLog(className, "runCmd", "", "", "Time taken to execute command " + cmdInfoDto.getActualCommand() + " = " + totalTime + " seconds.");
      
      if(runCmdDTOObj.isOutPutSaveOnServer())
      {
        copyOutputOnServer(runCmdDTOObj, vecCmdOut);
      }
      
      runCmdDTOObj.setServerTime(rptUtilsBean.getCurDateTime());
      runCmdDTOObj.setOutput(vecCmdOut);
      runCmdDTOObj.setStatus(true);
      Log.debugLog(className, "runCmd", "", "","Run Command Output Details -> " + runCmdDTOObj.toString());
      outputList.add(runCmdDTOObj);
      if(runCmdList.size() == outputList.size())
        isAllCommandRun = true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "runCmd", "", "", "Exception in runCmd() -", e);
      //traceLog( runCmdDTOObj.getUserName(), runCmdDTOObj.getServerName(), cmdInfoDto.getActualCommand(), "Fail");
      runCmdDTOObj.setStatus(false);
      Vector vec = new Vector();
      vec.add("Exception in runCmd() -" + e.getMessage());
      runCmdDTOObj.setErrMsg(vec);
      runCmdDTOObj.setServerTime(rptUtilsBean.getCurDateTime());
      outputList.add(runCmdDTOObj);
      if(runCmdList.size() == outputList.size())
        isAllCommandRun = true;
    }
  }
   
  public  void startProcess(int i)
  {
    Log.debugLog(className, "startProcess", "", "", "method called");
    final int startIndex =  i;
    Runnable downloadRun = new Runnable() 
    {
      @Override
      public synchronized void run()
      {
        runCmd(startIndex);
      }
    };
    
    Thread t = new Thread(downloadRun);
    t.start();
  }
  
  
  public ArrayList<RunCommandDTO> runCmdOnMulipleServer( ArrayList<RunCommandDTO> cmdList)
  {
    Log.debugLog(className, "runCmdOnMulipleServer", "", "", "method called");
    try
    {
      runCmdList = cmdList;
      
      for(int i = 0 ; i < cmdList.size() ; i++)
      {
        startProcess(i);
      }
      
      while(true)
      {
        if(isAllCommandRun)
          break;
        Thread.sleep(100);
      }
      
      Log.debugLog(className, "runCmdOnMulipleServer", "", "", "All command exexcuted.");
      return outputList;
    }
    catch (Exception e) 
    {
      e.printStackTrace();
      return outputList;
    }
  }
  
  private void copyOutputOnServer(RunCommandDTO runCmdDto , Vector vecOuput)
  {
    Log.debugLog(className, "copyOutputOnServer", "", "", "method called");
    try
    {
      String outputDirPath = Config.getWorkPath() + "/logs/TR" + runCmdDto.getTestRun() + "/server_logs/" + runCmdDto.getServerName();
      File ouputFileDir =  new File(outputDirPath);
      Log.debugLog(className, "copyOutputOnServer", "", "", "Ouput file directory - " + outputDirPath );
      
      if(!ouputFileDir.exists())
        ouputFileDir.mkdirs();
      
      String outPutFile =  runCmdDto.getServerName() + "_" + runCmdDto.getRunCmdInfoDto().getActualCommand() + "_" + rptUtilsBean.getCurDateTime() + ".out";
      outPutFile = outPutFile.replace(" ", "_").replace("/", "_").replace(":", "_").replace("-", "_");
      String outPutFilePath = outputDirPath + "/" + outPutFile;
      
      FileOutputStream fout = new FileOutputStream(outPutFilePath);  // Append mode
      PrintStream printStream = new PrintStream(fout);
      for(int i = 0 ; i < vecOuput.size() ; i++)
      {
       printStream.println(vecOuput.get(i).toString());
      }
      
      printStream.close();
      fout.close();
      
      ChangeOwnerOfFile(Config.getWorkPath() + "/logs/TR" + runCmdDto.getTestRun() + "/server_logs/", runCmdDto.getUserName());
      ChangeOwnerOfFile(outputDirPath, runCmdDto.getUserName());
      ChangeOwnerOfFile(outPutFilePath, runCmdDto.getUserName());
    }
    catch (Exception e) 
    {
      Log.stackTraceLog(className, "copyOutputOnServer", "", "", "Exception caught in writing output file " , e);
    }
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
  
  public ArrayList<RunCommandDetailInfoDTO> saveInfoInList(ArrayList<RunCommandDetailInfoDTO> dtoInfoList, String[] data)
  {
    Log.debugLog(className, "saveInfoInList", "", "", "Method called");
    
    try
    {
      RunCommandDetailInfoDTO cmdInfoDto = new RunCommandDetailInfoDTO();
     
      cmdInfoDto.setGroupName(data[0]);
      cmdInfoDto.setCommandName(data[1]);
      cmdInfoDto.setActualCommand(data[2]);
      cmdInfoDto.setRole(data[3]);
      cmdInfoDto.setServerType(data[4]);
      
      if(!data[5].trim().equals("NA"))
        cmdInfoDto.setSearchKeyword(data[5]);
    
      if(!data[6].trim().equalsIgnoreCase("Text"))
        cmdInfoDto.setViewType(data[6]);
      
      if(data[7].trim().equalsIgnoreCase("Yes"))
        cmdInfoDto.setColumnContains(true);
      if(!data[8].trim().equalsIgnoreCase("NA"))
      {
        if(data[8].trim().equalsIgnoreCase("Space") || data[8].trim().equalsIgnoreCase("Tab"))
          cmdInfoDto.setSeparator(data[8].trim());
        else if(data[8].trim().length() > 1)
        {
          Log.errorLog(className, "saveInfoInList", "", "", "Warning: Separator should be single charactor, Given separator is \"" + data[8] + "\", setting to default separator \"Space\".");
          cmdInfoDto.setSeparator("Space");
        }
        else
          cmdInfoDto.setSeparator(data[8].trim());
      }
    
      cmdInfoDto.setCmdUIArgs(data[9]);
    
      cmdInfoDto.setMaxInLineArguments(data[10]);
    
      cmdInfoDto.setDescription(data[16]);
    
      dtoInfoList.add(cmdInfoDto);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "saveInfoInList", "", "", "Exception caught to get cmd info from run_command.dat", e);
    }
    
    return dtoInfoList;
  }
  
  public ArrayList<RunCommandDetailInfoDTO> getRunCommandInfo()
  {
    Log.debugLog(className, "getRunCommandInfo", "", "", "Method Started.");
    
    try
    {
      ArrayList<RunCommandDetailInfoDTO> commandsList = new ArrayList<RunCommandDetailInfoDTO>();
      
      ArrayList <String> uniqueCmdLineList = new ArrayList<String>();
      
      String path = getRunCommandFilePath() + "run_command.dat";
      
      File inputFile = new File(path);
 
      FileInputStream fis = new FileInputStream(inputFile);
   
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));
      
      String strLine = "";
      
      while((strLine = br.readLine()) != null)
      {
        if(!strLine.trim().equals("") )
        {
          if(strLine.startsWith("#"))
            continue;
          
          if(uniqueCmdLineList.indexOf(strLine) < 0)
            uniqueCmdLineList.add(strLine);
         
        }
      }
      
      br.close();
      fis.close();
      
      //if site_run_command.dat is exist than neet to load site command also in command info
      path = getSiteRunCommandFilePath() + "site_run_command.dat";
      inputFile = new File(path);
      
      if(inputFile.exists())
      {
        fis = new FileInputStream(inputFile);
        br = new BufferedReader(new InputStreamReader(fis));
        
        while((strLine = br.readLine()) != null)
        {
          if(!strLine.trim().equals(""))
          {
            if(strLine.startsWith("#"))
              continue;
            
            if(uniqueCmdLineList.indexOf(strLine) < 0)
              uniqueCmdLineList.add(strLine);
          }
        }
      
        br.close();
        fis.close();
      }
      
      for(int  i = 0 ; i < uniqueCmdLineList.size(); i++)
      {
        String tmp[] = uniqueCmdLineList.get(i).split("\\|");
        if(tmp.length == 17)
        {
          commandsList = saveInfoInList(commandsList, tmp);
        }
      }
      
      Log.debugLog(className, "getRunCommandInfo", "", "", "Returning ArrayList -"+ commandsList.toString());
      return (commandsList);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getRunCommandInfo", "", "", "Exception in getRunCommandInfo() -", e);

      return null;
    }    
  }
  
  private void traceLog(String User , String Server, String Command , String Status)
  {
    String Messgae =   getDateTime() + "|" + User + "|" + Server + "|" + Command + "|" + Status; 
    writeToFile(Messgae);
  }
  
  //to get the date time in specific format
  public static String getDateTime()
  {
    Calendar currentDate = Calendar.getInstance();
    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    String dateTime = formatter.format(currentDate.getTime());
    return dateTime;
  }
  
  public static void writeToFile(String text)
  {
    try
    {
      File logDirPath = new File(Config.getWorkPath() + "/logs/.run_command/");
    
      if(!logDirPath.exists())
        logDirPath.mkdirs();  
      
      File curLogFileObj = new File(traceLogFilePath);
      
      FileOutputStream fout = new FileOutputStream(traceLogFilePath, true);  // Append mode
      PrintStream printStream = new PrintStream(fout);
      printStream.println(text);
      printStream.close();
      fout.close();
      
      // if file size is more than max, rename this file to .prev file
      if(curLogFileObj.length() > MaxlogFileSize)
      {
        File prevLogFileObj =  new File(traceLogFilePath + ".prev");
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
      Log.errorLog("Log", "writeToFile", "", "", "Exception - " + e);
    }
  }
}

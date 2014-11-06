package pac1.Bean.runcommand;

import java.io.Serializable;
import java.util.Vector;

public class RunCommandDTO implements Serializable
{
  private static String className = "RunCommandDTO";
  private String userName;
  private RunCommandDetailInfoDTO runCmdInfoDto;
  private boolean userDefindCommad = false;
  private String serverName;
  private boolean Status = false;
  private boolean isOutPutSaveOnServer = false;
  private String serverTime ; //Format will be MM/dd/yyyy hh:mm:ss
  private Vector output;
  private Vector errMsg;
  private String testRun ; 
  private int exitValue = 0;
  private String subOutputOption = "";
  private int subOutputValue = 0;
  private String filerKeyword = "NA";
  private String serverDisplayName = "";
  
  public String getSubOutputOption()
  {
    return subOutputOption;
  }

  public void setSubOutputOption(String subOutputOption)
  {
    this.subOutputOption = subOutputOption;
  }

  public int getSubOutputValue()
  {
    return subOutputValue;
  }

  public void setSubOutputValue(int subOutputValue) 
  {
    this.subOutputValue = subOutputValue;
  }
  
  public String toString()
  {
    return "User Name = " + userName + "Run Command details: [" + runCmdInfoDto.toString() + " ], Server Name = " + serverName + ", Test Run = " 
           + testRun + ", User defined Command = " + userDefindCommad + ", Status = " + Status + ", SubOutputPartOption = " + subOutputOption +
           ", SubOutputPartValue = " + subOutputValue;
  }
  
  public boolean isUserDefindCommad()
  {
    return userDefindCommad;
  }

  public int getExitValue()
  {
    return exitValue;
  }
  public void setExitValue(int exitValue)
  {
    this.exitValue = exitValue;
  }
  
  public void setUserDefindCommad(boolean userDefindCommad)
  {
    this.userDefindCommad = userDefindCommad;
  }
  
  public String getFilerKeyword() 
  {
    return filerKeyword;
  }

  public void setFilerKeyword(String filerKeyword) 
  {
    this.filerKeyword = filerKeyword;
  }

  public RunCommandDetailInfoDTO getRunCmdInfoDto()
  {
    return runCmdInfoDto;
  }

  public void setRunCmdInfoDto(RunCommandDetailInfoDTO runCmdInfoDto) 
  {
    this.runCmdInfoDto = runCmdInfoDto;
  }
  public String getTestRun()
  {
    return testRun;
  }

  public void setTestRun(String testRun)
  {
    this.testRun = testRun;
  }

  public Vector getOutput() 
  {
    return output;
  }

  public void setOutput(Vector output) {
    this.output = output;
  }

  public boolean isOutPutSaveOnServer()
  {
    return isOutPutSaveOnServer;
  }

  public void setOutPutSaveOnServer(boolean isOutPutSaveOnServer)
  {
    this.isOutPutSaveOnServer = isOutPutSaveOnServer;
  }

  public String getServerTime()
  {
    return serverTime;
  }

  public void setServerTime(String serverTime) {
    this.serverTime = serverTime;
  }
  
  public RunCommandDTO(){}
  
  public String getUserName()
  {
    return userName;
  }
  
  public void setUserName(String userName)
  {
    this.userName = userName;
  }
  
  public String getServerDisplayName()
  {
    return serverDisplayName;
  }
  public void setServerDisplayName(String serverDisplayName) {
    this.serverDisplayName = serverDisplayName;
  }
  
  public String getServerName()
  {
    return serverName;
  }
  public void setServerName(String serverName) {
    this.serverName = serverName;
  }
 
  public boolean isStatus() {
    return Status;
  }
  public void setStatus(boolean status) {
    Status = status;
  }
  public Vector getErrMsg() {
    return errMsg;
  }
  public void setErrMsg(Vector errMsg) {
    this.errMsg = errMsg;
  }
  
}

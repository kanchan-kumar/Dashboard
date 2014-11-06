package pac1.Bean;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class ThreadDumpReqDTO implements Serializable
{
  private String testRun = "-1";
  private String userName = "";
  private String owner = "";
  private String schProfile = "";
  
  //Getting message from server
  private String strMessageInfo = "";
  private String strStatus = "NONE"; //Default profile is not running otherwise RUNNING/OVER 
  private String strSchProfileOperation = "NONE"; //it can be DELETE/CREATE/REFRESH/START/STOP
  
  //key schedule name as a key and object of schedule info
  private LinkedHashMap<String, ThreadDumpSchedule> hashMapScheduleInfo = new LinkedHashMap<String, ThreadDumpSchedule>();
  
  private ExecuteScheduleThreadDump executeScheduleThreadDump = null;
  
  public ThreadDumpReqDTO(String testRun, String userName, String owner, String schProfile)
  {
    this.testRun = testRun;
    this.userName = userName;
    this.owner = owner;
    this.schProfile = schProfile;
  }
  
  public String getTestRun()
  {
    return testRun;
  }
 
  public String getUserName()
  {
    return userName;
  }
  
  public String getOwner()
  {
    return owner;
  }
  
  public String getSchProfile()
  {
    return schProfile;
  }
  
  //key schedule name as a key and object of schedule info
  public LinkedHashMap<String, ThreadDumpSchedule> getThreadDumpScheduleInfo()
  {
    return hashMapScheduleInfo;
  }
  
  public void setThreadDumpScheduleInfo(LinkedHashMap<String, ThreadDumpSchedule> hashMapScheduleInfo)
  {
    this.hashMapScheduleInfo = hashMapScheduleInfo;
  }
  
  //Message from server about profile
  public String getMessageInfo()
  {
    return strMessageInfo;
  }
  
  //Default profile is not running otherwise RUNNING/OVER 
  public void setMessageInfo(String strMessageInfo)
  {
    this.strMessageInfo = strMessageInfo;
  }
  
  public String getSchProfileStatus()
  {
    return strStatus;
  }
  
  public void setSchProfileStatus(String strStatus)
  {
    this.strStatus = strStatus;
  }
  
  //user have start for take schedule thread dump
  //then user refresh it update the object
  public ExecuteScheduleThreadDump getExecuteScheduleThreadDump()
  {
    return executeScheduleThreadDump;
  }
  
  public void setExecuteScheduleThreadDump(ExecuteScheduleThreadDump executeScheduleThreadDump)
  {
    this.executeScheduleThreadDump = executeScheduleThreadDump;
  }  
  
  //performing operation on profile
  public String getSchProfileOperation()
  {
    return strSchProfileOperation;
  }
  
  public void setSchProfileOperation(String strSchProfileOperation)
  {
    this.strSchProfileOperation = strSchProfileOperation;
  }
}

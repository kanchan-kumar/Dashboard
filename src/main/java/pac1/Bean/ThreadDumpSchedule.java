package pac1.Bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ThreadDumpSchedule implements Serializable
{
  private String scheduleName = "";
  private String startTime = "Immediate";
  private boolean isRepeat = false;
  private boolean isCondition = false;
  private int repeatInterval = 30; //seconds
  private int repeatCount = 0;
  
  private String strCondition = "NA";
  private String conditionStartTime = "Immediate";
  private boolean isRepeatCondition = false;
  private boolean isAnyRulePass = false;  //flag to satisfy any rule in check profile(s)
  private int conditionRepeatInterval = 30;
  private int conditionRepeatCount = 0;
  
  
  private LinkedHashMap<String, ArrayList<TakeThreadDump>> hashMapSchServerInstances = new  LinkedHashMap<String, ArrayList<TakeThreadDump>>();
  
  private int threadDumpCount = 0;
  private String lastThreadDumpTime = "-";
  
  private int conditionCount = 0;
  private String lastThreadDumpConditionTime = "-";
  
  private String fileOperation = "NONE"; //operation can be ADD/DELETE/UPDATE
  private String operation = "NONE"; //operation can be START/STOP/REFRESH
  private String scheduleStatus = "NOT STARTED"; //Status of schedule STARTED/RUNNING/OVER/NOT STARTED
  
  public ThreadDumpSchedule(String scheduleName)
  {
    this.scheduleName = scheduleName;
  }
  
  public String getScheduleName()
  {
    return scheduleName;
  }
  
  //when schedule will start time should be relative to test run
  public String getStartTime()
  {
    return startTime;
  }
  
  public void setStartTime(String startTime)
  {
    this.startTime = startTime;
  }
  
  //schedule repeat or not
  public boolean getIsRepeat()
  {
    return isRepeat;
  }
  
  public void setIsRepeat(boolean isRepeat)
  {
    this.isRepeat = isRepeat;
  }
  
  //If schedule repeat is yes then interval and count will also be enable
  public int getRepeatInterval()
  {
    return repeatInterval;
  }
  
  public void setRepeatInterval(int repeatInterval)
  {
    this.repeatInterval = repeatInterval;
  }
  
  //If schedule repeat is yes then interval and count will also be enable
  public int getRepeatCount()
  {
    return repeatCount;
  }
  
  public void setRepeatCount(int repeatCount)
  {
    this.repeatCount = repeatCount;
  }
  
  //Condition related to graphs
  //suppose Average response time > 10 secs then thread dump will take
  public String getCondition()
  {
    return strCondition;
  }
  
  public void setCondition(String strCondition)
  {
    this.strCondition = strCondition;
  }  
  
  //Repeat condition Immediate/after some time
  public String getConditionStartTime()
  {
    return conditionStartTime;
  }
  
  public void setConditionStartTime(String conditionStartTime)
  {
    this.conditionStartTime = conditionStartTime;
  }
  
  //Is Repeat condition
  public boolean getIsConditionRepeat()
  {
    return isRepeatCondition;
  }
  
  public void setIsConditionRepeat(boolean isRepeatCondition)
  {
    this.isRepeatCondition = isRepeatCondition;
  } 
  
  //Repeat condition
  public int getConditionRepeatCount()
  {
    return conditionRepeatCount;
  }
  
  public void setConditionRepeatCount(int conditionRepeatCount)
  {
    this.conditionRepeatCount = conditionRepeatCount;
  } 
  
  //Is Repeat condition
  public int getConditionRepeatInterval()
  {
    return conditionRepeatInterval;
  }
  
  public void setConditionRepeatInterval(int conditionRepeatInterval)
  {
    this.conditionRepeatInterval = conditionRepeatInterval;
  } 
  
  //Linked hash map contain server name as a key and list of object of TakeThreadDump as value
  public LinkedHashMap<String, ArrayList<TakeThreadDump>> getSchServerInstances()
  {
    return hashMapSchServerInstances;
  }
  
  public void setSchServerInstances(LinkedHashMap<String, ArrayList<TakeThreadDump>> hashMapSchServerInstances)
  {
    this.hashMapSchServerInstances = hashMapSchServerInstances;
  }  
  
  //number of time is execute schedule
  public int getThreadDumpCount()
  {
    return threadDumpCount;
  }
  
  public void setThreadDumpCount(int threadDumpCount)
  {
    this.threadDumpCount = threadDumpCount;
  }   
  
  //time of thread of schedule execute 
  public String getLastThreadDumpTime()
  {
    return lastThreadDumpTime;
  }
  
  public void setLastThreadDumpTime(String lastThreadDumpTime)
  {
    this.lastThreadDumpTime = lastThreadDumpTime;
  }  

  //number of time is execute condition after satisfied
  public int getThreadDumpConditionCount()
  {
    return conditionCount;
  }
  
  public void setThreadDumpConditionCount(int conditionCount)
  {
    this.conditionCount = conditionCount;
  }   
  
  //time of thread of schedule execute 
  public String getLastThreadDumpConditionTime()
  {
    return lastThreadDumpConditionTime;
  }
  
  public void setLastThreadDumpConditionTime(String lastThreadDumpConditionTime)
  {
    this.lastThreadDumpConditionTime = lastThreadDumpConditionTime;
  }
  
  //fileOperation on file it will use when operation is SAVE
  public String getFileOperation()
  {
    return fileOperation;
  }
  
  public void setFileOperation(String fileOperation)
  {
    this.fileOperation = fileOperation;
  } 
  
  //operation on schedule 
  public String getOperation()
  {
    return operation;
  }
  
  public void setOperation(String operation)
  {
    this.operation = operation;
  }  
    
  //schedule status 
  public String getScheduleStatus()
  {
    return scheduleStatus;
  }
  
  public void setScheduleStatus(String scheduleStatus)
  {
    this.scheduleStatus = scheduleStatus; 
  }  
  
  //schedule condition   
  public boolean getIsCondition()
  {
    return isCondition;
  }
  
  public void setIsCondition(boolean isCondition)
  {
    this.isCondition = isCondition;
  }  
  
  //getter for rule condition on check profile   
  public boolean getIsAnyRulePassInCheckProfile()
  {
    return isAnyRulePass;
  }
  
  //setter for rule condition on check profile 
  public void setIsAnyRulePassInCheckProfile(boolean isAnyRulePass)
  {
    this.isAnyRulePass = isAnyRulePass;
  }  
  
  
}

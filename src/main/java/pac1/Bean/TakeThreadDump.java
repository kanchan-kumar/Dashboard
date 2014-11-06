/*
 *Pid|Owner|StartTime|CPUTime|TDUsingPID|TDUsingJMX|Instance|Arguments|LogFileName
 *2383|netstorm|Feb06|00:00:36|No|No|-|java -DNS_WDIR=/home/netstorm/work Server|-
 */
package pac1.Bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Vector;

public class TakeThreadDump implements Serializable
{
  private String strPID = "";
  private String strOwner = "";
  private String strStartTime = "";
  private String strCPUTime = "";
  private String strTDUsingPID = "";
  private String strTDUsingJMX = "";
  private String strTDUsingCMD = "";
  private String strInstance = "";
  private String strProcessArgs = "";
  private String strLogFileName = "";
  private String strSearchPattern = "";
  
  private String strWaitTimeInSecs = "10";

  //0 - Now
  //1- At Elapsed
  //2 - At Absolute
  private String strThreadDumpTime = "Now";
  private String strThreadDumpStartTime = "";
  private String strThreadDumpEndTime = "";
  private String strServerName = "";
  private String strTestRunNum = "";
  private String strUserName = "";
  private Vector vecCmdResult = null;
  
  private String strScheduleName = "";
  private String strDumpType = "Thread";
  
  public TakeThreadDump(String strPID, String strOwner, String strStartTime, String strCPUTime, String strTDUsingPID, String strTDUsingJMX, String strTDUsingCMD, String strInstance, String strProcessArgs, String strLogFileName)
  {
    this.strPID = strPID;
    this.strOwner = strOwner;
    this.strStartTime = strStartTime;
    this.strCPUTime = strCPUTime;
    this.strTDUsingPID = strTDUsingPID;
    this.strTDUsingJMX = strTDUsingJMX;
    this.strTDUsingCMD = strTDUsingCMD;
    this.strInstance = strInstance;
    this.strProcessArgs = strProcessArgs;
    this.strLogFileName = strLogFileName;
  }
  //Set Java Instance Information
  public void setPID(String strPID)
  {
    this.strPID = strPID;
  }

  public void setOwner(String strOwner)
  {
    this.strOwner = strOwner;
  }
  
  public void setStartTime(String strStartTime)
  {
    this.strStartTime = strStartTime;
  }  
  
  public void setCPUTime(String strCPUTime)
  {
    this.strCPUTime = strCPUTime;
  }  
  
  public void setTDUUsingPID(String strTDUsingPID)
  {
    this.strTDUsingPID = strTDUsingPID;
  }  
  
  public void setTDUUsingJMX(String strTDUsingJMX)
  {
    this.strTDUsingJMX = strTDUsingJMX;
  }   
  
  public void setTDUUsingCMD(String strTDUsingCMD)
  {
    this.strTDUsingCMD = strTDUsingCMD;
  }
  
  public void setInstance(String strInstance)
  {
    this.strInstance = strInstance;
  } 
  
  public void setProcessArgs(String strProcessArgs)
  {
    this.strProcessArgs = strProcessArgs;
  }  
  
  public void setLogFileName(String strLogFileName)
  {
    this.strLogFileName = strLogFileName;
  }
  
  public void setSearchPattern(String strSearchPattern)
  {
    this.strSearchPattern = strSearchPattern;
  }
  
  //Setting other information
  public void setWaitTimeInSecs(String strWaitTimeInSecs)
  {
    this.strWaitTimeInSecs = strWaitTimeInSecs;
  }  
 
  public void setThreadDumpTime(String strThreadDumpTime)
  {
    this.strThreadDumpTime = strThreadDumpTime;
  } 
  
  public void setThreadDumpStartTime(String strThreadDumpStartTime)
  {
    this.strThreadDumpStartTime = strThreadDumpStartTime;
  } 
  
  public void setThreadDumpEndTime(String strThreadDumpEndTime)
  {
    this.strThreadDumpEndTime = strThreadDumpEndTime;
  } 
  
  public void setServerName(String strServerName)
  {
    this.strServerName = strServerName;
  }
  
  public void setScheduleName(String strScheduleName)
  {
    this.strScheduleName = strScheduleName;
  }
  
  public void setTestRunNum(String strTestRunNum)
  {
    this.strTestRunNum = strTestRunNum;
  }
  
  public void setUserName(String strUserName)
  {
    this.strUserName = strUserName;
  }
  
  public void setDumpType(String strDumpType)
  {
    this.strDumpType = strDumpType;
  }

  public void setvecCmdResult(Vector vecCmdResult)
  {
    this.vecCmdResult = vecCmdResult;   
  }
  //getting Java Information
  public String getPID()
  {
    return strPID;
  }

  public String getOwner()
  {
    return strOwner;
  }
  
  public String getStartTime()
  {
    return strStartTime;
  }  
  
  public String getCPUTime()
  {
    return strCPUTime;
  }  
  
  public String getTDUUsingPID()
  {
    return strTDUsingPID;
  }  
  
  public String getTDUUsingJMX()
  {
    return strTDUsingJMX;
  }   
  
  public String getTDUUsingCMD()
  {
    return strTDUsingCMD;
  }
  
  public String getInstance()
  {
    return strInstance;
  } 
  
  public String getProcessArgs()
  {
    return strProcessArgs;
  }  
  
  public String getLogFileName()
  {
    return strLogFileName;
  }  
  
  public String getSearchPattern()
  {
    return strSearchPattern;
  }
  
  //getting other information
  public String getWaitTimeInSecs()
  {
    return strWaitTimeInSecs;
  }  
 
  public String getThreadDumpTime()
  {
    return strThreadDumpTime;
  } 
  
  public String getThreadDumpStartTime()
  {
    return strThreadDumpStartTime;
  } 
  
  public String getThreadDumpEndTime()
  {
    return strThreadDumpEndTime;
  } 
  
  public String getServerName()
  {
    return strServerName;
  }
  
  public String getScheduleName()
  {
    return strScheduleName;
  }
  
  public String getTestRunNum()
  {
    return strTestRunNum;
  }  
  
  public String getUserName()
  {
    return strUserName;
  }  
  
  public String getDumpType()
  {
    return strDumpType;
  }  
  
  public Vector getVecCmdResult()
  {
    return vecCmdResult;   
  }
}

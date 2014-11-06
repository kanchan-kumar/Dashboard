/**
 * Class ThreadDumpSummaryInfo contains following fields
 * 1- Date
 * 2- Server Name
 * 3- Insatnce
 * 4- Log File Name 
 * and the setter and getter of these fields.
 * thease fields are extracted from the path 
 * '/home/netstorm/work/webapps/TRXXXX/server_logs/thread_dumps/serverName/InstanceName/yyyy_mm_dd_hh_mm_ss_td.txt'
 */
package pac1.Bean;

import java.util.Date;

public class ThreadDumpSummaryInfo implements java.io.Serializable
{
  private String date = null;
  private String serverName = null;
  private String instance = null;
  private String logFileName = null;
 
  private boolean isUserNoteFilePresent = false;
  private StringBuffer userNoteFileContents = new StringBuffer();
  private String userNoteFileName = "NA";
  
  public void setDate(String arg)
  {
    date = arg;
  }
  
  public String getDate()
  {
    return date;
  }
  
  public void setServerName(String arg)
  {
    serverName = arg;
  }
  
  public String getServerName()
  {
    return serverName;
  }
  
  public void setInstance(String arg)
  {
    instance = arg; 
  }
  
  public String getInstance()
  {
    return instance;
  }
  
  public void setLogFileName(String arg)
  {
    logFileName = arg; 
  }
  
  public String getLogFileName()
  {
    return logFileName;
  }
  
  public void setIsUserNoteFilePresent(boolean isUserNoteFilePresent)
  {
    this.isUserNoteFilePresent = isUserNoteFilePresent; 
  }
  
  public boolean getIsUserNoteFilePresent()
  {
    return isUserNoteFilePresent;
  }
  
  public void setUserNoteFileContents(StringBuffer userNoteFileContents)
  {
    this.userNoteFileContents = userNoteFileContents; 
  }
  
  public StringBuffer getUserNoteFileContents()
  {
    return userNoteFileContents;
  }
  
  public void setUserNoteFileName(String userNoteFileName)
  {
    this.userNoteFileName = userNoteFileName; 
  }
  
  public String getUserNoteFileName()
  {
    return userNoteFileName;
  }
}

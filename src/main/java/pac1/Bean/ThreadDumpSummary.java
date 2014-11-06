/**
 * ThreadDumpSummary has four fields
 * 1- TRNum --> Test Run number
 * 2- status --> true for success and false for fail.
 * 3- errMsg --> Message regarding failures.
 * 4- ThreadDumpSummaryInfo[] --> it has 4 fields
 *   1-Date
 *   2-ServerName
 *   3-InstanceName
 *   4-LogFileName
 * these fields are extracted from the path 
 * 'home/netstorm/work/webapps/logs/TRxxxx/server_logs/thread_dumps/serverName/InstanceName/yyyy_MM_dd_hh_mm_ss_td.txt'
*/

package pac1.Bean;

public class ThreadDumpSummary implements java.io.Serializable
{
  private int TRNum = -1;
  private boolean status = true;
  private String errMsg = null;
  private ThreadDumpSummaryInfo[] threadDumpSummaryInfo = null;
  
  public void setTRNum(int arg)
  {
    TRNum = arg;
  }
  
  public int getTRNum()
  {
    return TRNum;
  }
  
  public void setStatus(boolean arg)
  {
    status = arg;
  }
  
  public boolean getStatus()
  {
    return status;
  }
  
  public void setErrorMsg(String arg)
  {
    errMsg = arg;
  }
  
  public String getErrorMsg()
  {
    return errMsg; 
  }
  
  public void setThreadDumpSummaryInfo(ThreadDumpSummaryInfo[] arg)
  {
    threadDumpSummaryInfo = arg;
  }
  
  public ThreadDumpSummaryInfo[] getThreadDumpSummaryInfo()
  {    
    return threadDumpSummaryInfo;
  }
}

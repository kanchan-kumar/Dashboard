package pac1.Bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class RunThreadDumpInstances 
{
  private static String className = "runThreadDumpInstances";
  LinkedHashMap<String, ArrayList<TakeThreadDump>> hashMapServerInfo = null;
  String waitTime = "300";
  
  public RunThreadDumpInstances(LinkedHashMap<String, ArrayList<TakeThreadDump>> hashMapServerInfo)
  {
    this.hashMapServerInfo = hashMapServerInfo;
    waitTime = Config.getIntValue("netstorm.execution.waitTime", "300");
  }

  public LinkedHashMap<String, ArrayList<TakeThreadDump>> executeInstancesToGetTD()
  {
    Log.debugLog(className, "executeInstancesToGetTD", "", "", "Method called");
    
    try
    {
      
      //Testing Purpose
      //printLinkHashMap();
      
      Iterator<Map.Entry<String, ArrayList<TakeThreadDump>>> entries = hashMapServerInfo.entrySet().iterator();
      ThreadGroup[] threadGroupInstance = new ThreadGroup[hashMapServerInfo.size()];
      int threadCounter = 0;
      
      while (entries.hasNext()) 
      {
        Map.Entry<String, ArrayList<TakeThreadDump>> pairs = (Map.Entry)entries.next();
        
        ArrayList<TakeThreadDump> arrListInstance = pairs.getValue();
        threadGroupInstance[threadCounter] = new ThreadGroup(pairs.getKey());
        Log.debugLog(className, "executeInstancesToGetTD", "", "", "Thread Group Name = " + pairs.getKey());
        
        executeThreadInTG executeThread[] = new executeThreadInTG[arrListInstance.size()];
        threadGroupInstance[threadCounter].enumerate(executeThread);
        for(int i = 0; i < arrListInstance.size(); i++)
        {
          TakeThreadDump takeThreadDump = arrListInstance.get(i);
          Log.debugLog(className, "executeInstancesToGetTD", "", "", "Server Name = " + takeThreadDump.getServerName() + ", PID = " + takeThreadDump.getPID() + ", Log Path Name = " + takeThreadDump.getLogFileName());
          executeThread[i] = new executeThreadInTG(threadGroupInstance[threadCounter], takeThreadDump, arrListInstance, i, this);
          executeThread[i].start();
        }
        
        threadCounter++;
      }

      Log.debugLog(className, "executeInstancesToGetTD", "", "", "All Thread Started");
      waitForInstanceRunThreads(threadGroupInstance);
      
      //printLinkHashMap();
      return hashMapServerInfo;
    }
    catch (Exception e) 
    {
      Log.errorLog(className, "executeInstancesToGetTD", "", "", "Exception - " + e);
      return null;
    }
  }
  
  public void printLinkHashMap()
  {
    System.out.println("hashMapServerInfo = " + hashMapServerInfo.size());
    Iterator<Map.Entry<String, ArrayList<TakeThreadDump>>> entries = hashMapServerInfo.entrySet().iterator();
    while (entries.hasNext()) 
    {
      Map.Entry<String, ArrayList<TakeThreadDump>> pairs = (Map.Entry)entries.next();
      
      ArrayList<TakeThreadDump> arrListInstance = pairs.getValue();
      ThreadGroup threadGroupInstance = new ThreadGroup(pairs.getKey());
      
      System.out.println("arrListInstance.size() = " + arrListInstance.size());
      for(int i = 0; i < arrListInstance.size(); i++)
      {
        TakeThreadDump threadDump = arrListInstance.get(i);
        System.out.println("\nResult = " + threadDump.getVecCmdResult() + "\n" + threadDump.getServerName() + "\n" + threadDump.getPID());
      }  
    }
  }
  
  public synchronized void waitForInstanceRunThreads(ThreadGroup[] arrthreadGroup)
  {
    Log.debugLog(className, "waitForInstanceRunThreads", "", "", "Method called." + arrthreadGroup.length);
    try
    {
      int pendingThreadCount = arrthreadGroup.length;
      do //infinite loop
      {
        Log.debugLog(className, "waitForInstanceRunThreads", "", "", "Waiting for notify from threads ...");
        // wait();
        wait(1000);
        Log.debugLog(className, "waitForInstanceRunThreads", "", "", "Got notify ...");
        pendingThreadCount = arrthreadGroup.length;
        //checking each thread is alive or not
        //If not, stopping thread and decrease counter
        //then break from the loop
        for(int i = 0; i < arrthreadGroup.length; i++)
        {
          if(arrthreadGroup[i].activeCount() == 0)
          {
            Log.debugLog(className, "waitForInstanceRunThreads", "", "", "Thread[" + i + "]" + arrthreadGroup[i].getName() + " is not alive");
            pendingThreadCount--;
          }
          else
            Log.debugLog(className, "waitForInstanceRunThreads", "", "", "Thread[" + i + "]" + arrthreadGroup[i].getName() + " is alive");
        }
      } while (pendingThreadCount > 0);
    }
    catch (Exception e) {
      System.out.println("Exception in thread = " + e);
      // TODO: handle exception
    }
  }  
  
  class executeThreadInTG extends Thread 
  {
    RunThreadDumpInstances threadDumpInstances = null;
    TakeThreadDump takeThreadDump = null;
    ArrayList<TakeThreadDump> arrListInstance = null;
    int tdIndex = -1;
    
    public executeThreadInTG(ThreadGroup g, TakeThreadDump takeThreadDump, ArrayList<TakeThreadDump> arrListInstance, int tdIndex, RunThreadDumpInstances threadDumpInstances) 
    {
      super(g, takeThreadDump.getPID());
      Log.debugLog(className, "Take Thread Dump", "", "", "Thread Group name = " + g.getName() + ", Thread Name = " + takeThreadDump.getPID());
      
      this.takeThreadDump = takeThreadDump;
      this.arrListInstance = arrListInstance;
      this.tdIndex = tdIndex;
      this.threadDumpInstances = threadDumpInstances;
    }

    @Override
    public void run() 
    {
      try
      {
        Log.debugLog(className, "Take Thread Dump", "", "", "Thread group, prority, thread name " + Thread.currentThread());
        //-t <test run> -s <server name> -p <pid> -l <log file name> [ -w <wait time in secs> -D ]
      
        String strCmd = "nsi_take_java_thread_dump";
        String strArgs = "-t " + takeThreadDump.getTestRunNum() + " -s " + takeThreadDump.getServerName() + " -p " + takeThreadDump.getPID();

        if(takeThreadDump.getDumpType().trim().toLowerCase().equals("Thread".toLowerCase()))
        {        
          if(takeThreadDump.getTDUUsingCMD().trim().toLowerCase().equals("yes".toLowerCase()) || takeThreadDump.getTDUUsingCMD().trim().equalsIgnoreCase("yesUsingSudo") || takeThreadDump.getTDUUsingJMX().trim().toLowerCase().equals("yes".toLowerCase()))
          {
          
          }
          else
            strArgs = strArgs + " -l " + takeThreadDump.getLogFileName();
        }
        else
        {
          strCmd = "nsi_take_java_heap_dump";
          strArgs = strArgs + " -l " + takeThreadDump.getLogFileName();;
        }
        
        //If command needs to be execute with sudo permission, need to add -r option - preeti
        if(takeThreadDump.getTDUUsingCMD().trim().equalsIgnoreCase("yesUsingSudo"))
        {
          strArgs = strArgs  + " -r";
        }
        
        if(takeThreadDump.getInstance().trim().equals(""))
          strArgs = strArgs  + " -i " + takeThreadDump.getPID();
        else
          strArgs = strArgs  + " -i " + takeThreadDump.getInstance();

        //user name and wait time for response
        strArgs = strArgs + " -u " + takeThreadDump.getUserName() + " -w " + waitTime;
        
        Log.debugLog(className, "Take Thread Dump", "", "", "strCmd = "+ strCmd + ", strArgs = " + strArgs);
      
        CmdExec exec = new CmdExec(); 
        Vector vecCmdOutput = exec.getResultByCommand(strCmd, strArgs, 0, takeThreadDump.getUserName(), "root");
        
        Log.debugLog(className, "Take Thread Dump", "", "", "tdIndex = " + tdIndex + ", strCmd = "+ strCmd + ", strArgs = " + strArgs + "\noutput = " + vecCmdOutput.toString());
        takeThreadDump.setvecCmdResult(vecCmdOutput);
        arrListInstance.set(tdIndex, takeThreadDump);
        
        hashMapServerInfo.put(takeThreadDump.getServerName(), arrListInstance);
        threadDumpInstances.notifyAll();
      }
      catch (Exception e) {
        // TODO: handle exception
      }
    }
  }

  public static void main(String[] args) 
  {
    LinkedHashMap hashMap = new LinkedHashMap();
    ArrayList jj = new ArrayList();
    
    String serverName = "192.168.1.83";
    
    TakeThreadDump dump = new TakeThreadDump("11", "netstorm", "", "","","","","", "", "/tmp");
    dump.setServerName(serverName);
    jj.add(dump);
    
    /**TakeThreadDump dump1 = new TakeThreadDump("12", "netstorm1", "", "","","","","","", "/tmp1");
    dump1.setServerName(serverName); 
    jj.add(dump1);**/
    
    hashMap.put(serverName, jj);
    
    serverName = "192.168.1.84";
    jj = new ArrayList();
    dump = new TakeThreadDump("21", "netstorm", "", "","","","","","", "/tmp");
    dump.setServerName(serverName);
    jj.add(dump);
    
   /** dump1 = new TakeThreadDump("22", "netstorm1", "", "","","","","", "", "/tmp1");
    dump1.setServerName(serverName); 
    jj.add(dump1);
    **/
    hashMap.put(serverName, jj);
    
    RunThreadDumpInstances dumpInstances = new RunThreadDumpInstances(hashMap);
    dumpInstances.executeInstancesToGetTD();
  }
}
  

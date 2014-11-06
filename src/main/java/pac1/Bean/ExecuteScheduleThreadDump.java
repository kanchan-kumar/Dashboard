package pac1.Bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class ExecuteScheduleThreadDump implements Serializable
{
  private static final String className = "ExecuteScheduleThreadDump";
  public ThreadDumpReqDTO threadDumpReqDTO;
  TestRunData testRunDataObj = null;
  LinkedHashMap<String, ThreadDumpSchedule> hashMapScheduleInfo = null;
  //CheckRuleEvaluator checkRuleEvaluator = null;
  String waitTime = "300";
  //ThreadGroup[] threadGroupInstance = null;
  
  public ExecuteScheduleThreadDump()
  {
    waitTime = Config.getIntValue("netstorm.execution.waitTime", "300");
  }
  
  public void setTestRunData(TestRunData testRunDataObj)
  {
    try
    {
      this.testRunDataObj = testRunDataObj;
      //creating object of check profile only once
      Log.debugLog(className, "getStatOfScheduleRunThreads", "", "", "Called Before-------------");
      //checkRuleEvaluator = new CheckRuleEvaluator(testRunDataObj);
      Log.debugLog(className, "getStatOfScheduleRunThreads", "", "", "----------------Called After");
    }
    catch (Exception e) 
    {
      e.printStackTrace();
    }
  }
  
  public ThreadDumpReqDTO getThreadDumpReqDTO()
  {
    return threadDumpReqDTO;
  }

  public void executeSchTD(ThreadDumpReqDTO threadDumpReqDTO)
  {
    this.threadDumpReqDTO = threadDumpReqDTO;
    Log.debugLog(className, "executeSchTD", "", "", "Method called");
    
    try
    {
      hashMapScheduleInfo = threadDumpReqDTO.getThreadDumpScheduleInfo();
      executeInstancesToGetTD();
    }
    catch (Exception e) 
    {
      e.printStackTrace();
    }
  }
  
  public void getStatOfScheduleRunThreads()
  {
  /**  Log.debugLog(className, "getStatOfScheduleRunThreads", "", "", "Method called." + threadGroupInstance.length);
    try
    {
      //checking each thread is alive or not
      //If not, stopping thread and decrease counter
      //then break from the loop
      for(int i = 0; i < threadGroupInstance.length; i++)
      {
        if(threadGroupInstance[i].activeCount() != 0)
        {
          threadDumpReqDTO.setSchProfileStatus("RUNNING");
          Log.debugLog(className, "waitForInstanceRunThreads", "", "", "Thread[" + i + "]" + threadGroupInstance[i].getName() + " is alive");
          break;
        }
        else
        {
          threadDumpReqDTO.setSchProfileStatus("OVER");
          Log.debugLog(className, "waitForInstanceRunThreads", "", "", "Thread[" + i + "]" + threadGroupInstance[i].getName() + " is not alive");
        }
      }
    }
    catch (Exception e) 
    {
      threadDumpReqDTO.setSchProfileStatus("OVER");
      e.printStackTrace();
      System.out.println("Exception in thread = " + e);
      // TODO: handle exception
    }**/
  }  
  
  
  private void printStatement(String strMessage)
  {
    //System.out.println(strMessage);
  }
  
  public void executeInstancesToGetTD()
  {
    Log.debugLog(className, "executeInstancesToGetTD", "", "", "Method called");
    
    try
    {
      
      //Testing Purpose
      //printLinkHashMap();
      
      Iterator<Map.Entry<String, ThreadDumpSchedule>> entries = hashMapScheduleInfo.entrySet().iterator();
      ThreadGroup[] threadGroupInstance = new ThreadGroup[hashMapScheduleInfo.size()];
      int threadCounter = 0;
      
      while (entries.hasNext()) 
      {
        Map.Entry<String, ThreadDumpSchedule> pairs = (Map.Entry)entries.next();
        
        ThreadDumpSchedule threadDumpSchedule = pairs.getValue();
        threadGroupInstance[threadCounter] = new ThreadGroup(pairs.getKey());
        Log.debugLog(className, "executeInstancesToGetTD", "", "", "Thread Group Name = " + pairs.getKey());
        
        printStatement(threadDumpSchedule.getOperation() + ", Thread Group Name = " + pairs.getKey());
        
        if(!threadDumpSchedule.getOperation().equals("START"))
          continue;
        
        printStatement(threadDumpSchedule.getOperation()+ ", Execute Thread Group Name = " + pairs.getKey());
        
        executeThreadInTG executeThread[] = new executeThreadInTG[threadDumpSchedule.getSchServerInstances().size()];
        threadGroupInstance[threadCounter].enumerate(executeThread);
        
        LinkedHashMap<String, ArrayList<TakeThreadDump>> hasMapServerInfo =  threadDumpSchedule.getSchServerInstances();
        Iterator<Map.Entry<String, ArrayList<TakeThreadDump>>> entriesServer = hasMapServerInfo.entrySet().iterator();
        int countServer = 0;
        while (entriesServer.hasNext()) 
        {
          Map.Entry<String, ArrayList<TakeThreadDump>> pairsServer = (Map.Entry)entriesServer.next();
          
          printStatement("Key = " + pairsServer.getKey() + ", Value = " + pairsServer.getValue());
          
          ArrayList<TakeThreadDump> arrListInstance = pairsServer.getValue();
        
          Log.debugLog(className, "executeInstancesToGetTD", "", "", "Server Name = " + pairsServer.getKey());
          executeThread[countServer] = new executeThreadInTG(threadGroupInstance[threadCounter], pairs.getKey(), pairsServer.getKey(), hasMapServerInfo, threadDumpSchedule, arrListInstance, countServer, this);
          executeThread[countServer].start();
        }
        
        threadCounter++;
      }

      Log.debugLog(className, "executeInstancesToGetTD", "", "", "All Thread Started");
      getStatOfScheduleRunThreads();      
      //waitForInstanceRunThreads(threadGroupInstance);
      
     /* for(int i = 0; i < 2; i++)
      {
        printStatement("Result------------------------");
      }*/
      //printLinkHashMap();
      //printStatement("End Result ---------------------");
      //Thread.sleep(5000);
      //printLinkHashMap();
      //return hashMapServerInfo;
    }
    catch (Exception e) 
    {
      Log.errorLog(className, "executeInstancesToGetTD", "", "", "Exception - " + e);
      //return null;
    }
  }
  
  public void printLinkHashMap()
  {
    System.out.println("Profile Name = " + threadDumpReqDTO.getSchProfile());
    LinkedHashMap<String, ThreadDumpSchedule> hashMapScheduleInfo = threadDumpReqDTO.getThreadDumpScheduleInfo();
    Iterator<Map.Entry<String, ThreadDumpSchedule>> entriesSchedule = hashMapScheduleInfo.entrySet().iterator();
    while (entriesSchedule.hasNext()) 
    {
      Map.Entry<String, ThreadDumpSchedule> pairsSchedule = (Map.Entry)entriesSchedule.next();
      
      ThreadDumpSchedule threadDumpSchedule = pairsSchedule.getValue();
      System.out.println("Schedule Name = " + threadDumpSchedule.getScheduleName());
      printStatement("Count = " + threadDumpSchedule.getThreadDumpCount() + ", Last Modified Time = " + threadDumpSchedule.getLastThreadDumpTime());
      
      LinkedHashMap<String, ArrayList<TakeThreadDump>> hasMapServerInfo =  threadDumpSchedule.getSchServerInstances();
      Iterator<Map.Entry<String, ArrayList<TakeThreadDump>>> entriesServer = hasMapServerInfo.entrySet().iterator();
      while (entriesServer.hasNext()) 
      {
        Map.Entry<String, ArrayList<TakeThreadDump>> pairs = (Map.Entry)entriesServer.next();
      
        ArrayList<TakeThreadDump> arrListInstance = pairs.getValue();
      
        System.out.println("arrListInstance.size() = " + arrListInstance.size());
        for(int i = 0; i < arrListInstance.size(); i++)
        {
          TakeThreadDump threadDump = arrListInstance.get(i);
          System.out.println("\nResult = " + threadDump.getVecCmdResult() + "\n" + threadDump.getServerName() + "\n" + threadDump.getPID());
        }  
      }
    }
  }  
  
  
  class executeThreadInTG extends Thread 
  {
    ExecuteScheduleThreadDump executeScheduleThreadDump = null;
    LinkedHashMap<String, ArrayList<TakeThreadDump>> hashMapServerInfo = null;
    TakeThreadDump takeThreadDump = null;
    ThreadDumpSchedule threadDumpSchedule = null;
    ArrayList<TakeThreadDump> arrListInstance = null;
    String scheduleName = "";
    String serverName = "";
    int tdIndex = -1;
    
    String strCmd = "nsi_take_java_thread_dump";
    
    public executeThreadInTG(ThreadGroup g, String scheduleName, String serverName, LinkedHashMap<String, ArrayList<TakeThreadDump>> hashMapServerInfo, ThreadDumpSchedule threadDumpSchedule, ArrayList<TakeThreadDump> arrListInstance, int tdIndex, ExecuteScheduleThreadDump executeScheduleThreadDump) 
    {
      super(g, serverName);
      Log.debugLog(className, "Take Thread Dump", "", "", "Thread Group name = " + g.getName() + ", Thread Name = " + serverName);
      printStatement("Thread Group name = " + g.getName() + ", Thread Name = " + serverName);
      this.scheduleName = scheduleName;
      this.serverName = serverName;
      this.arrListInstance = arrListInstance;
      this.hashMapServerInfo = hashMapServerInfo;
      this.tdIndex = tdIndex;
      this.executeScheduleThreadDump = executeScheduleThreadDump;
      this.threadDumpSchedule = threadDumpSchedule;
    }

    @Override
    public void run() 
    {
      try
      {
        Log.debugLog(className, "Take Thread Dump", "", "", "Thread group, prority, thread name " + Thread.currentThread());
        //-t <test run> -s <server name> -p <pid> -l <log file name> [ -w <wait time in secs> -D ]
      
        //tracking schedule execute
        int threadDumpCount = 0;
        //tracking condition execute it include both pass and fail 
        int conditionCount = 0;
        //tracking satisfied condition
        int satisfiedConditionCount = 0;
        
        printStatement("Is Repeat = "+ threadDumpSchedule.getIsRepeat());
        
        //schedule repeat start with 1
        if(threadDumpSchedule.getIsRepeat())
          threadDumpCount++;
        else //start with 0
          threadDumpCount = threadDumpSchedule.getRepeatCount();
        
        Log.debugLog(className, "Take Thread Dump", "", "", "threadDumpSchedule.getStartTime() = " + threadDumpSchedule.getStartTime());
        
        {
          printStatement(" Repeat Count = "+ threadDumpSchedule.getRepeatCount());
          //Infinite loop
          //If user want take thread dump till test is over if flag of test is over is true from testRunData data object it will break from the while loop 
          // or 
          //specified number of count it will break from while loop
          while(true)
          {
            Log.debugLog(className, "Take Thread Dump", "", "", "Current thread count = " + threadDumpCount + ", threadDumpSchedule.getRepeatCount() = " + threadDumpSchedule.getRepeatCount());
            
            //Repeat immediately or after some time
            //If it is not Now it will start take thread dump after sleep 
            if(!threadDumpSchedule.getStartTime().toUpperCase().equals("NOW"))
              Thread.sleep(rptUtilsBean.convStrToMilliSec(threadDumpSchedule.getStartTime()));
            
            Log.debugLog(className, "Take Thread Dump", "", "", "Repeat Count = "+ threadDumpCount + ", Number of execution = " + threadDumpCount);
            
            //-1 means till test is over
            if(threadDumpSchedule.getRepeatCount() == -1)
            {
              Log.debugLog(className, "Take Thread Dump", "", "", "testRunDataObj.isTestRunIsOver() = " + testRunDataObj.isTestRunIsOver());
            }
            else if(!(threadDumpCount <= threadDumpSchedule.getRepeatCount())) //specified number of execution
              break;
            
            printStatement("dddddddddReapeat Count = "+ threadDumpCount);
            
            Log.debugLog(className, "Take Thread Dump", "", "", "Condition = " + threadDumpSchedule.getCondition());
            //Adding condition
            //if condition is not NA
            if(!threadDumpSchedule.getCondition().equals("NA"))
            {
              //condition repeat start with 1
              if(threadDumpSchedule.getIsConditionRepeat())
              {
                conditionCount++;
              }
              else //start with 0
                conditionCount = threadDumpSchedule.getConditionRepeatCount();
              
              Log.debugLog(className, "Take Thread Dump", "", "", " Is Repeat Condition = " +threadDumpSchedule.getIsConditionRepeat() + ", Count = " + conditionCount);
              //Infinite loop
              //It will check condition till test is over if flag of test is over is true from testRunData data object it will break from the while loop 
              // or 
              //specified number of count it will break from while loop
              while(true)
              {
                Log.debugLog(className, "Take Thread Dump", "", "", "Current condition count = " + conditionCount + ", threadDumpSchedule.getConditionRepeatCount() = " + threadDumpSchedule.getConditionRepeatCount());
                //-1 means till test is over
                if(threadDumpSchedule.getConditionRepeatCount() == -1)
                {
                  Log.debugLog(className, "Take Thread Dump", "", "", "testRunDataObj.isTestRunIsOver() = " + testRunDataObj.isTestRunIsOver());
                  //if test run is over and condition for till test is over it will break from the inner loop
                  if(threadDumpSchedule.getConditionRepeatCount() == -1 && testRunDataObj.isTestRunIsOver())
                    break;
                }
                else if(!(conditionCount <= threadDumpSchedule.getConditionRepeatCount())) //specified number of execution
                  break;
                
                Log.debugLog(className, "Take Thread Dump", "", "", "Start Time for condition = " + threadDumpSchedule.getConditionStartTime());
                
                //Repeat immediately or after some time
                //If it is not Now it will start take thread dump after sleep 
                if(!threadDumpSchedule.getConditionStartTime().equals("Immediate"))
                {
                  Log.debugLog(className, "Take Thread Dump", "", "", "Start Time for condition - Going for sleep for " + threadDumpSchedule.getConditionStartTime());
                  Thread.sleep(rptUtilsBean.convStrToMilliSec(threadDumpSchedule.getConditionStartTime()));
                }
                
                //checking check profile is satisfied or not
                if(isCheckProfileSatisfied(threadDumpSchedule.getCondition(), threadDumpSchedule.getIsAnyRulePassInCheckProfile()))
                {
                  Log.debugLog(className, "Take Thread Dump", "", "", "Condition satisfied");
                  //execute all profile
                }
                else //not satisfied and will not take thread dump and continue
                {
                  Log.debugLog(className, "Take Thread Dump", "", "", "Condition not satisfied");
                  conditionCount++;
                  if(threadDumpSchedule.getIsConditionRepeat())
                  {
                    Log.debugLog(className, "Take Thread Dump", "", "", "Repeat after condition interval " + threadDumpSchedule.getConditionRepeatInterval());
                    Thread.sleep(threadDumpSchedule.getConditionRepeatInterval());
                  }
                  continue;
                }
                
                satisfiedConditionCount++;
                threadDumpSchedule.setThreadDumpConditionCount(satisfiedConditionCount);
                threadDumpSchedule.setLastThreadDumpConditionTime(rptUtilsBean.getCurDateTime());
                
                Log.debugLog(className, "Take Thread Dump", "", "", "Satisfied count condition = " + satisfiedConditionCount);
                executeInstance();
                if(threadDumpSchedule.getIsConditionRepeat())
                {
                  Log.debugLog(className, "run", "", "", "Repeat after condition interval = " + threadDumpSchedule.getConditionRepeatInterval());
                  Thread.sleep(threadDumpSchedule.getConditionRepeatInterval());
                }
                conditionCount++;
              }
            }
            else
              executeInstance();
              
            
            Log.debugLog(className, "Take Thread Dump", "", "", "threadDumpCount = " + threadDumpCount + ", threadDumpSchedule.getRepeatCount() = " + threadDumpSchedule.getRepeatCount());

            //schedule execution match with user define count mean schedule is over
            if(threadDumpCount == threadDumpSchedule.getRepeatCount())
            {
              Log.debugLog(className, "Take Thread Dump", "", "", "OVERRRRRRRRRRRRRRRRRRRRR");
              threadDumpSchedule.setScheduleStatus("OVER");
              threadDumpReqDTO.setSchProfileStatus("OVER");
            }
            else //Running
            {
              Log.debugLog(className, "Take Thread Dump", "", "", "RUNNINGGGGGGGGGGGGGGGGG");
              threadDumpSchedule.setScheduleStatus("RUNNING");
              threadDumpReqDTO.setSchProfileStatus("RUNNING");
            }

            //Setting value in threadDumpSchedule object
            hashMapScheduleInfo = threadDumpReqDTO.getThreadDumpScheduleInfo();
            
            threadDumpSchedule.setThreadDumpCount(threadDumpCount);
            threadDumpSchedule.setLastThreadDumpTime(rptUtilsBean.getCurDateTime());
            threadDumpSchedule.setSchServerInstances(hashMapServerInfo);
            hashMapScheduleInfo.put(scheduleName, threadDumpSchedule);
            threadDumpReqDTO.setThreadDumpScheduleInfo(hashMapScheduleInfo);
            
            if(threadDumpCount != threadDumpSchedule.getRepeatCount() || threadDumpSchedule.getRepeatCount() == -1)
              Thread.sleep(threadDumpSchedule.getRepeatInterval());
            
            //Test is over and user define till test is over set status over and break from outer loop
            if(threadDumpSchedule.getRepeatCount() == -1 && testRunDataObj.isTestRunIsOver())
            {
              Log.debugLog(className, "Take Thread Dump", "", "", "Till test isOVERRRRRRRRRRRRRRRRRRRRR");
              threadDumpSchedule.setScheduleStatus("OVER");
              threadDumpSchedule.setSchServerInstances(hashMapServerInfo);
              hashMapScheduleInfo.put(scheduleName, threadDumpSchedule);
              threadDumpReqDTO.setThreadDumpScheduleInfo(hashMapScheduleInfo);
              threadDumpReqDTO.setSchProfileStatus("OVER");
              break;
            }
            threadDumpCount++;
          }
        }
      }
      catch (Exception e) 
      {
        e.printStackTrace();
        // TODO: handle exception
      }
    }
    
    //Execute instance in sequential manner to take thread dump
    public void executeInstance()
    {
      Log.debugLog(className, "executeInstance", "", "", "Method called. Instance Size = " + arrListInstance.size() + " and value = " + arrListInstance.toString());
      
      for(int i = 0; i < arrListInstance.size(); i++)
      {
        takeThreadDump = arrListInstance.get(i);
        String strArgs = "-t " + takeThreadDump.getTestRunNum() + " -s " + takeThreadDump.getServerName();
        
        if(!takeThreadDump.getSearchPattern().trim().equals("") && !takeThreadDump.getSearchPattern().trim().equals("-"))
          strArgs = strArgs + " -S \"" + takeThreadDump.getSearchPattern() + "\"";
        else
          strArgs = strArgs + " -p " + takeThreadDump.getPID();
        
        if(takeThreadDump.getTDUUsingCMD().trim().toLowerCase().equals("yes".toLowerCase()) || takeThreadDump.getTDUUsingCMD().trim().equalsIgnoreCase("yesUsingSudo") || takeThreadDump.getTDUUsingJMX().trim().toLowerCase().equals("yes".toLowerCase()))
        {
          
        }
        else
          strArgs = strArgs + " -l " + takeThreadDump.getLogFileName();
        
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
        
        printStatement("strCmd = "+ strCmd + ", strArgs = " + strArgs);
        
        CmdExec exec = new CmdExec(); 
        Vector vecCmdOutput = exec.getResultByCommand(strCmd, strArgs, 0, "RunAsProcessUser", null, "Server");
        
        Log.debugLog(className, "Take Thread Dump", "", "", "####tdIndex = " + tdIndex + ", strCmd = "+ strCmd + ", strArgs = " + strArgs + "\noutput = " + vecCmdOutput.toString());
        printStatement("tdIndex = " + tdIndex + ", strCmd = "+ strCmd + ", strArgs = " + strArgs + "\noutput = " + vecCmdOutput.toString());
        takeThreadDump.setvecCmdResult(vecCmdOutput);
        Log.debugLog(className, "executeInstance", "", "", "Set on Index i = " + i);
        arrListInstance.set(i, takeThreadDump);
        
        hashMapServerInfo.put(takeThreadDump.getServerName(), arrListInstance);
      }
    }
    
    //This method return condition satisfied or not
    public boolean isCheckProfileSatisfied(String strCondition, boolean anyRulePass)
    {
      Log.debugLog(className, "isCheckProfileSatisfied", "", "", "Method called");
      /*
      CmdExec exec = new CmdExec(); 
      Vector vecData = new Vector();
      //nsi_check_tr_using_profile --t 6666 --f default/default/NewProfName --i 192.168.1.64 --d
      
      return exec.getResultByCommand(vecData, "nsu_check_Profile", "", 0, "RunAsProcessUser", null, "Server");*/
      
      
      try
      {
        String arrTempCond[] = rptUtilsBean.split(strCondition, ",");
        ArrayList<String> condList = new ArrayList<String>();
        for(int i = 0; i < arrTempCond.length; i++)
        {
          condList.add(arrTempCond[i] + ".cprof");
        }
        
        Log.debugLog(className, "isCheckProfileSatisfied", "", "", "condList = " + condList.toString());
        boolean flag = false;//checkRuleEvaluator.evaluateCheckRules(testRunDataObj, condList, anyRulePass);
        Log.debugLog(className, "isCheckProfileSatisfied", "", "", "flag = " + flag);
        return flag;
      }
      catch (Exception e) 
      {
        e.printStackTrace();
        return false;
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
    
    ThreadDumpReqDTO threadDumpReqDTO = new ThreadDumpReqDTO("11", "netstorm", "netstorm", "testSchProfile");
    
    LinkedHashMap<String, ThreadDumpSchedule> hashMapScheduleInfo = new LinkedHashMap<String, ThreadDumpSchedule>();
    
    ThreadDumpSchedule threadDumpSchedule = new ThreadDumpSchedule("Schedule1"); 
    threadDumpSchedule.setOperation("START");
    threadDumpSchedule.setIsRepeat(true);
    threadDumpSchedule.setRepeatInterval(10);
    threadDumpSchedule.setRepeatCount(2);
    LinkedHashMap<String, ArrayList<TakeThreadDump>> hashMapForThreadDump = hashMap;
    threadDumpSchedule.setSchServerInstances(hashMapForThreadDump);
    hashMapScheduleInfo.put("Schedule1", threadDumpSchedule);
    
    ThreadDumpSchedule threadDumpScheduleRef = new ThreadDumpSchedule("Schedule2"); 
    threadDumpScheduleRef.setOperation("REFRESH");
    threadDumpScheduleRef.setIsRepeat(true);
    threadDumpScheduleRef.setRepeatInterval(10);
    threadDumpScheduleRef.setRepeatCount(1);
    
    hashMap = new LinkedHashMap();
    serverName = "192.168.1.85";
    jj = new ArrayList();
    dump = new TakeThreadDump("41", "netstorm", "", "","","","","","", "/tmp");
    dump.setServerName(serverName);
    jj.add(dump);
    
    hashMap.put(serverName, jj);
    
    LinkedHashMap<String, ArrayList<TakeThreadDump>> hashMapForThreadDumpRef = hashMap;
    threadDumpScheduleRef.setSchServerInstances(hashMapForThreadDumpRef);
    hashMapScheduleInfo.put("Schedule2", threadDumpScheduleRef);
    
    threadDumpReqDTO.setThreadDumpScheduleInfo(hashMapScheduleInfo);   
    
    ExecuteScheduleThreadDump executeScheduleThreadDump = new ExecuteScheduleThreadDump();
    executeScheduleThreadDump.executeSchTD(threadDumpReqDTO);
  }
    
}

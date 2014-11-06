package pac1.Bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class PartitionUIUtils 
{
  public final String className = "PartitionInfoUtils";
  public final String PARTITION_INFO_CMD = "nsu_show_partition_detail";
  private final String ARCHIVE_CMD = "nsu_archive_trun";
  private final String UNARCHIVE_CMD = "nsu_unarchive_trun";
  private final String SET_TEST_MODE_CMD = "nsi_set_test_mode";
  private String testNumber = null;

  public PartitionUIUtils(String testNumber)
  {
    this.testNumber = testNumber;
    Log.debugLog(className, "PartitionInfoUtils", "", "", "Object initialized, Test Run = " + this.testNumber);
  }
  
  public boolean deletePartitions(ArrayList<String> partitionList)
  {
    try
    {
      Log.debugLog(className, "deleteSessionPartition", "", "", "Method called, Partitions = " + partitionList.toString());
      return true;
      
      
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "deleteSessionPartition", "", "", "Exception in deleting partitions", e);
      return false;
    }
  }
  
  /**
   * This method locks or unlocks test run partitions
   * @param arrUnlockSessions array of partitions that need to be locked.
   * @param arrLockSessions array of partitions that need to be unlocked.
   * @param errMsg appends error message.
   * @return true if no exception occurs
   *         false if exception occurs
   */
  public boolean changePartitionMode(String [] arrUnlockSessions , String [] arrLockSessions, StringBuffer errMsg)
  {
    try
    {
      
      List<String> unlockSessionList = new ArrayList<String>();
      List<String> lockSessiontList = new ArrayList<String>();

      if(arrUnlockSessions != null)
        unlockSessionList = Arrays.asList(arrUnlockSessions);
      
      if(arrLockSessions != null)
        lockSessiontList = Arrays.asList(arrLockSessions);
      
      
      Log.debugLog(className, "changePartitionMode", "", "", "Method called, Unlock Partitions = " + unlockSessionList.toString() 
          + ", Lock Partition = " + lockSessiontList.toString());
      
      String cmdArgs = testNumber ;
      
      if(unlockSessionList.size() > 0 )
        lockOrUnLockTestPartition(SET_TEST_MODE_CMD, cmdArgs, unlockSessionList, errMsg , "R");
      
      if(lockSessiontList.size() > 0)
        lockOrUnLockTestPartition(SET_TEST_MODE_CMD, cmdArgs, lockSessiontList, errMsg, "W");
      
      return true;
      
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "changePartitionMode", "", "", "Exception in change partitions mode", e);
      return false;
    }
  }
  
  /** This method runs lock or unlock partition command
   *  <b> LOCK COMMAND </b>
   *    nsi_set_test_mode <TestRunNumber> R <Partition>
   *    nsi_set_test_mode 1639 R 20140823182000
   *  <b> UNLOCK COMMAND </b>
   *    nsi_set_test_mode <TestRunNumber> W <Partition>
   *    nsi_set_test_mode 1639 W 20140823182000  
   * @param testModeCommand nsi_set_test_mode 
   * @param cmdArgs
   * @param sessionList list partitions to be locked or unlocked.
   * @param errMsg appends error message if any
   * @param lockOrUnlockString either W or R
   */
  private void lockOrUnLockTestPartition(String testModeCMD, String cmdArgs,
      List<String> partitionList, StringBuffer errMsg, String lockOrUnlockString) {
    
    Log.debugLog(className, "lockOrUnLockTestPartition", "", "", "Method Started");   
     
    CmdExec objCmdExec = new CmdExec();

    for(int i = 0 ; i < partitionList.size();  i++)
    {      
      if(!"".equals(partitionList.get(i)))
      {
        Vector vecCmdOutPut = objCmdExec.getResultByCommand(testModeCMD, cmdArgs + " " + lockOrUnlockString + " " + partitionList.get(i),
            0, "admin", null);
      
        if(vecCmdOutPut != null && vecCmdOutPut.size() > 0 && ((String)vecCmdOutPut.lastElement()).startsWith("ERROR"))
        {
          Log.debugLog(className, "getSesionPartitionData", "", "", "No Logs found, Error = " + (String)vecCmdOutPut.lastElement());
          
          errMsg.append("Error in lock or unlock partition: " + partitionList.get(i)+ " due to " + vecCmdOutPut.get(0) + "\\n");
        }
      }
    }
  }

  /**
   * This method archive or unarchives test run partitions
   * @param arrArchiveRows array of partitions that need to be archived
   * @param arrUnArchiveRows array of partitions that need to be unarchived.
   * @param errMsg appends error message.
   * @return
   */
  public boolean archiveAndUnArchivePartition(String[] arrArchiveRows, String[] arrUnArchiveRows, StringBuffer errMsg)
  {
    try
    {
      List<String> archiveList = new ArrayList<String>();
      List<String> unArchiveList = new ArrayList<String>();

      if(arrArchiveRows != null)
        archiveList = Arrays.asList(arrArchiveRows);
      
      if(arrUnArchiveRows != null)
        unArchiveList = Arrays.asList(arrUnArchiveRows);
      
      Log.debugLog(className, "ArchiveAndUnArchivePartition", "", "", "Method called, Partitions = " + archiveList.toString() 
          + ", Respective Mode = " + unArchiveList.toString());
      
      
      String cmdArgs = "-n  " + testNumber + " -p";
      
      if(archiveList.size() > 0 )
        archiveOrUnArchivePartitions(ARCHIVE_CMD, cmdArgs, archiveList, errMsg);
      
      if(unArchiveList.size() > 0)
        archiveOrUnArchivePartitions(UNARCHIVE_CMD, cmdArgs, unArchiveList, errMsg);
      
      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "ArchiveAndUnArchivePartition", "", "", "Exception in deleting partitions", e);
      return false;
    }
  }
  
  /** This method runs archive or unarchive commands on test run partitions
   *  <b>ARCHIVE COMMAND </b>
   *    nsu_archive_trun -n  TestRunNumber -p PartitionNumber
   *    Eg:- nsu_archive_trun -n  1639 -p 20140823181300
   *  <b>UNARCHIVE COMMAND</b>
   *    nsu_unarchive_trun -n  TestRunNumber -p PartitionNumber
   *    Eg:- nsu_unarchive_trun -n  1639 -p 20140823181300     
   * @param archiveCmd either nsu_archive_trun 
   * @param cmdArgs
   * @param partitionList list of partitions
   * @param errMsg for storing errorMessage
   */
  private void archiveOrUnArchivePartitions(String archiveCmd, String cmdArgs,
      List<String> partitionList, StringBuffer errMsg) {
    Log.debugLog(className, "archiveOrUnArchivePartitions", "", "", "Method Started");    
    
    CmdExec objCmdExec = new CmdExec();
    
    for(int i = 0 ; i < partitionList.size();  i++)
    {
      if(!"".equals(partitionList.get(i)))
      {
        Vector vecCmdOutPut = objCmdExec.getResultByCommand(archiveCmd, cmdArgs + " " + partitionList.get(i), 0, "admin", null);
      
        if((vecCmdOutPut == null) || (vecCmdOutPut.size() <= 0))
        {
          Log.debugLog(className, "getSesionPartitionData", "", "", "No Logs found");
          
          errMsg.append(", Error in archive or unarchive partition: " + partitionList.get(i) + "\\n");      
        }
        else if(((String)vecCmdOutPut.lastElement()).startsWith("ERROR"))
        {
          Log.debugLog(className, "getSesionPartitionData", "", "", "No Logs found, Error = " + (String)vecCmdOutPut.lastElement());
          
          errMsg.append("Error in archive or unarchive partition: " + partitionList.get(i)+ " due to " + vecCmdOutPut.get(0) + "\\n");     
        }
      }
    }  
  }

  /**
   * This method gets partition details of test run by executing following command <br>
   *  nsu_show_partition_detail -t TestRunNumber <br>
   * <pre>    Example nsu_show_partition_detail -t 1639 
   *  20140823181125|AR|00:00:35|08/23/14  18:11:24|82844|NA
   *  20140823181500|AR|00:00:59|08/23/14  18:15:00|78912|NA
   *  20140823183300|AR|00:00:59|08/23/14  18:33:00|78624|NA
   *  20140823182600|AR|00:00:59|08/23/14  18:26:00|87097|NA
   *  20140823183900|W|00:01:01|08/23/14  18:39:00|16342364|NA
   *  20140823181800|AR|00:01:00|08/23/14  18:18:00|78877|NA
   *  20140823182000|R|00:00:59|08/23/14  18:20:00|16255762|NA
   *  20140823182200|AR|00:01:00|08/23/14  18:22:00|78641|NA
   *  20140823182300|AR|00:01:00|08/23/14  18:23:00|78733|NA
   *  20140823183600|AR|00:01:00|08/23/14  18:36:00|494598|NA
   *  20140823182100|AR|00:01:00|08/23/14  18:21:00|79311|NA
   *  20140823183700|AR|00:00:59|08/23/14  18:37:00|80120|NA  </pre>
   * @param userName
   * @param cmdArgs
   * @param errMsg
   * @return two dimensional array containing each partition details
   * 
   *   <br>If any exception occurs it returns null
   */
  public String [][] getSesionPartitionData(String userName, String cmdArgs, StringBuffer errMsg)
  {
    try
    {
       Log.debugLog(className, "getSesionPartitionData", "", "", "method called, User Name = " + userName + ", Cmd Args = " + cmdArgs + ", Test Run = " + testNumber);
       
       String [][] arrDataValues = null;
       CmdExec objCmdExec = new CmdExec();
       
       if(!cmdArgs.equals(""))
         cmdArgs = "-t " + testNumber + " " + cmdArgs;
       else
         cmdArgs = "-t " + testNumber;
       
       Vector vecCmdOutPut = objCmdExec.getResultByCommand(PARTITION_INFO_CMD, cmdArgs, 0, "root", null);

       if((vecCmdOutPut == null) || (vecCmdOutPut.size() <= 0))
       {
         Log.debugLog(className, "getSesionPartitionData", "", "", "No Logs found");
         errMsg.append("No Partition Found for Test run - " + this.testNumber );
         return null;
       }
       else if(((String)vecCmdOutPut.lastElement()).startsWith("ERROR"))
       {
         Log.debugLog(className, "getSesionPartitionData", "", "", "No Logs found, Error = " + (String)vecCmdOutPut.lastElement());
         errMsg.append("No Partition Found for Test run - " + this.testNumber );
         return null;
       }

       ArrayList<String []> arrList = new ArrayList <String []>();
       
       for(int i = 0 ; i < vecCmdOutPut.size() ; i++)
       {
         String strLine = (String) vecCmdOutPut.get(i);
         
         if(strLine.startsWith("#") || strLine.contains("Error:"))
         {
           Log.errorLog(className, "getSesionPartitionData", "", "", "Error in following line = " + strLine);
           continue;
         }
         
         String arrTempValues[] = rptUtilsBean.split(strLine, "|");
         
         if(arrTempValues.length >= 6 )
         {
           arrList.add(arrTempValues);  
         }
       }
       
       arrDataValues = new String[arrList.size()][6];
       
       for(int i = 0 ; i < arrDataValues.length ; i++)
       {
         arrDataValues[i] = arrList.get(i);
       }
       
       return arrDataValues;
    }
    catch (Exception e) 
    {
      Log.stackTraceLog(className, "getSesionPartitionData", "", "", "", e);
      return null;
    }
  }
}

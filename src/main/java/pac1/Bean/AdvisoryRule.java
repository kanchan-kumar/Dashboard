/*--------------------------------------------------------------------
  @Name    : AdvisoryRule.java
  @Author  : Jyoti Jain
  @Purpose : Provides rules in formation and corresponding graph index and name
  @Read only :- Format of file
     Rule Type|Rule Id|Threshold Type|Graph Definition|Operation|Value|Time Option|Start Time|End Time|Time Window|Future1|Future2|Future3|Description
  @Modification History:
    08/24/11:Jyoti Jain - Initial Version

----------------------------------------------------------------------*/

package pac1.Bean;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;

import pac1.Bean.GraphName.GraphNames;

public class AdvisoryRule implements java.io.Serializable
{
  private static String className = "AdvisoryRule";
  private static String workPath = Config.getWorkPath();
  private final static String NAR_DIR_NAME = "advisoryRules"; //netstorm advisory rule
  private final static String NAR_FILE_EXTN = ".nar"; //netstorm advisory rule
  private final static String SYSTEM_NAR_FILE = "_netstorm_advisory_rule" + NAR_FILE_EXTN;
  private final static String HEADER_LINE = "#Rule Type|Rule Id|Threshold Type|Graph Definition|Operation|Value|Last State Change Time|Current State|% Change|Time Window|Active|Severity|LogInAlertWindow|Description|Rule Name|Alert Description|Future1|Future2|Future3|Future4|future5";
  private final static String ALER_STATUS_HEADER_LINE = "#TestRun|Rule ID|Rule Name|Severity|Alert Time|Alert Msg|future1|future2|future3|future4|future5|future6|future7|future8|future9|future10|future11|future12|future13|future14|future15";
  private final static String THRESHOLD_TYPE = "Threshold"; 
  private final static String COMPARE_TYPE = "Compare";
  private final static int BOTH_TYPE = 2;
  
  private String numTestRun = "-1";
  private String narFileName = "";
  private int ruleType = 2; //0 - only Threshold, 1 - only compare, 2 - both type

  private GraphNames graphNamesObj = null;

  public AdvisoryRule(String numTestRun, String narFileName, int ruleType)
  {
    this.numTestRun = numTestRun;
    this.narFileName = narFileName;
    this.ruleType = ruleType;
  }

  public AdvisoryRule(String numTestRun, String narFileName)
  {
    this.numTestRun = numTestRun;
    this.narFileName = narFileName;
  }

  public AdvisoryRule(String numTestRun)
  {
    this.numTestRun = numTestRun;
  }

  private String getNarPathByFileName(String fileName)
  {
    return workPath + "/webapps/" + NAR_DIR_NAME + "/" + fileName + NAR_FILE_EXTN;
  }

  private String getAlertPathByFileName(String fileName)
  {
    if(numTestRun.equals(""))
      return workPath + "/webapps/logs/" + NAR_DIR_NAME + "/" + fileName + ".dat";
    else
      return workPath + "/webapps/logs/TR" + numTestRun + "/" + fileName + ".dat";
  }

  
  // this function return the rule information in 2D array
  public String[][] getAllData()
  {
    Log.debugLog(className, "getAllData", "", "", "Method called. NAR file name with path = " + getNarPathByFileName(narFileName));

    try
    {
      Vector vecData = rptUtilsBean.readFileInVector(getNarPathByFileName(narFileName));

      if(vecData == null)
        return null;

      String arrNarData[][] = new String[vecData.size()][9];

      String dataLine = "";
      for(int i = 0; i < vecData.size(); i++)
      {
        dataLine = vecData.get(i).toString();

        //Log.debugLog(className, "getAllData", "", "", "Reading data line = " + dataLine);
        arrNarData[i] = rptUtilsBean.strToArrayData(dataLine, "|");
      }

      return arrNarData;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getAllData", "", "", "Exception - ", e);
      return null;
    }
  }

  // this function to delete the rule(s)
   public boolean deleteRules(String[] ruleIds)
   {
     Log.debugLog(className, "deleteRules", "", "", "Method called. number of ruleIds to delete = " + ruleIds.length);

     try
     {
       Vector vecData = rptUtilsBean.readFileInVector(getNarPathByFileName(narFileName));
       Vector newVecData = new Vector();

       if(vecData == null)
         return false;

       String dataLine = "";
       for(int i = 0; i < vecData.size(); i++)
       {
         dataLine = vecData.get(i).toString();
         Log.debugLog(className, "dataLine initially", "", "", "dataLine = " + dataLine);
         boolean matchedToDelete = false;
         String[] tmp = rptUtilsBean.strToArrayData(dataLine, "|");

         for(int xx = 0; xx < ruleIds.length; xx++)
         {
           Log.debugLog(className, "deleteRules", "", "", "ruleId = " + ruleIds[xx]);
           if(tmp[1].equals(ruleIds[xx]))
           {
             Log.debugLog(className, "dataLine to matched to delete", "", "", "dataLine = " + dataLine);
             matchedToDelete = true;
             break;
           }
         }

         if(!matchedToDelete)
         {
           Log.debugLog(className, "dataLine added in vector", "", "", "dataLine = " + dataLine);
           newVecData.add(dataLine);
         }
       }
       if(!writeToFile(newVecData))
           return false;
       return true;
     }
     catch (Exception e)
     {
       Log.stackTraceLog(className, "deleteRules", "", "", "Exception - ", e);
       return false;
     }
   }

   // this function to delete the Active Alerts
   public boolean deleteActiveAlerts(String alertFileName, String[] ruleNames)
   {
     Log.debugLog(className, "deleteActiveAlerts", "", "", "Method called. number of ruleNames to delete = " + ruleNames.length);

     try
     {
       String alertHistorySchedulerFilePath = getAlertPathByFileName("alert_history_scheduler");
       Vector vecData = rptUtilsBean.readFileInVector(getAlertPathByFileName(alertFileName));
       Vector newVecData = new Vector();

       if(vecData == null)
         return false;

       String dataLine = "";
       for(int i = 0; i < vecData.size(); i++)
       {
         dataLine = vecData.get(i).toString();
         Log.debugLog(className, "dataLine initially", "", "", "dataLine = " + dataLine);
         boolean matchedToDelete = false;
         String[] tmp = rptUtilsBean.strToArrayData(dataLine, "|");

         for(int xx = 0; xx < ruleNames.length; xx++)
         {
           Log.debugLog(className, "deleteActiveAlerts", "", "", "ruleNames = " + ruleNames[xx]);
           String[] alertDataToDelete = null; 

           if(ruleNames[xx].contains("|"))
             alertDataToDelete = ruleNames[xx].split("\\|");

           if(alertDataToDelete != null)
           {
             if((tmp[3].equals(alertDataToDelete[0])) && (tmp[2].equals(alertDataToDelete[1])) && (tmp[4].equals(alertDataToDelete[2])) && (tmp[5].equals(alertDataToDelete[3])))
             {
               Log.debugLog(className, "dataLine to matched to delete", "", "", "dataLine = " + dataLine);
               matchedToDelete = true;
               try
               {
                 PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(alertHistorySchedulerFilePath, true)));
                 String dataToAppend = "NA|Forced clear by operator|0.0|00:00:00|00:00:00|Threshold|NA|0|NA|NA|-1|-1|"+ alertDataToDelete[0] + "|" + alertDataToDelete[1] + "|" + tmp[1] + "|Forced clear by operator|"+ alertDataToDelete[2] +"|NA";
                 out.println(dataToAppend);
                 out.close();
               }
               catch(IOException e)
               {
                 Log.stackTraceLog(className, "deleteActiveAlerts", "", "", "Exception caught - ", e);
                 break;
               }
               break;
             }
           }
         }

         if(!matchedToDelete)
         {
           Log.debugLog(className, "dataLine added in vector", "", "", "dataLine = " + dataLine);
           newVecData.add(dataLine);
         }
       }
       if(!writeToAlertStatusFile(newVecData, alertFileName))
           return false;
       return true;
     }
     catch (Exception e)
     {
       Log.stackTraceLog(className, "deleteActiveAlerts", "", "", "Exception - ", e);
       return false;
     }
   }
   
   
  // this function to add new rule
  public boolean addRule(String[] ruleValues)
  {
    Log.debugLog(className, "addRule", "", "", "Method called");
    //String ruleType, String thresholdType, String graphDefinition, String operation, double value, String timeOption, String startTime, String endTime, long timeWindow, String Description
    try
    {
      Vector vecData = rptUtilsBean.readFileInVector(getNarPathByFileName(narFileName));
      int newRuleId = 1;
      if(vecData == null || vecData.size() < 1)
        vecData = new Vector();
      else
      {
        String dataLine = vecData.get(vecData.size()-1).toString();
        //System.out.println("dataLine555 = " + dataLine);
        Log.debugLog(className, "addRule", "", "", "Method called");
        String[] tmp = rptUtilsBean.strToArrayData(dataLine, "|");
        newRuleId = Integer.parseInt(tmp[1]) + 1;
        //vecData.add(0, HEADER_LINE);
      }

      String newDataLine = ruleValues[0] + "|" + newRuleId + "|" + ruleValues[1] + "|" + ruleValues[2] + "|" + ruleValues[3] + "|" + ruleValues[4] + "|" + ruleValues[5] + "|" + ruleValues[6] + "|" + ruleValues[7] + "|" + ruleValues[8] + "|"+ ruleValues[12] +"|"+ ruleValues[10] +"|"+ ruleValues[11] +"|" + ruleValues[9]+ "|" + ruleValues[13] + "|" + ruleValues[14] + "|" + ruleValues[15] + "|" + ruleValues[16] + "|" + ruleValues[17] + "|" + ruleValues[18] + "|" + ruleValues[19];
      vecData.add(newDataLine);

      //System.out.println("newDataLine = " + newDataLine);
      if(!writeToFile(vecData))
        return false;

      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "addRule", "", "", "Exception - ", e);
      return false;
    }
  }

  // this function to add new rule
  public boolean updateRule(String ruleId, String[] ruleValues)
  {
    Log.debugLog(className, "updateRule", "", "", "Method called ruleId = " + ruleId);

    try
    {
      Vector vecData = rptUtilsBean.readFileInVector(getNarPathByFileName(narFileName));
      Vector newVecData = new Vector();

      if(vecData == null)
      {
        return false;
      }

      String newDataLine = ruleValues[0] + "|" + ruleId + "|" + ruleValues[1] + "|" + ruleValues[2] + "|" + ruleValues[3] + "|" + ruleValues[4] + "|" + ruleValues[5] + "|" + ruleValues[6] + "|" + ruleValues[7] + "|" + ruleValues[8] + "|"+ ruleValues[12] +"|"+ ruleValues[10] +"|"+ ruleValues[11] +"|" + ruleValues[9] + "|" + ruleValues[13] + "|" + ruleValues[14] + "|" + ruleValues[15] + "|" + ruleValues[16] + "|" + ruleValues[17] + "|" + ruleValues[18] + "|" + ruleValues[19];;

      String dataLine = "";
      for(int i = 0; i < vecData.size(); i++)
      {
        dataLine = vecData.get(i).toString();

        String[] tmp = rptUtilsBean.strToArrayData(dataLine, "|");
        if(tmp[1].equals(ruleId))
        {
          newVecData.add(newDataLine);
        }
        else
        {
          newVecData.add(dataLine);
        }

      }

      if(!writeToFile(newVecData))
        return false;

      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "updateRule", "", "", "Exception - ", e);
      return false;
    }
  }

  private boolean writeToAlertStatusFile(Vector vecModified, String fileName)
  {
    Log.debugLog(className, "writeToAlertStatusFile", "", "", "Method called");

    try
    {
      FileOutputStream out2 = new FileOutputStream(new File(getAlertPathByFileName(fileName)));
      PrintStream alertFile = new PrintStream(out2);
      alertFile.println(ALER_STATUS_HEADER_LINE);
      for(int ad = 0; ad < vecModified.size(); ad++)
        alertFile.println(vecModified.get(ad).toString());
      alertFile.close();
      out2.close();

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "writeToAlertStatusFile", "", "", "Exception - ", e);
      return false;
    }
  }
  
  
  private boolean writeToFile(Vector vecModified)
  {
    Log.debugLog(className, "writeToFile", "", "", "Method called");

    try
    {
      FileOutputStream out2 = new FileOutputStream(new File(getNarPathByFileName(narFileName)));
      PrintStream narFile = new PrintStream(out2);
      narFile.println(HEADER_LINE);
      for(int ad = 0; ad < vecModified.size(); ad++)
        narFile.println(vecModified.get(ad).toString());
      narFile.close();
      out2.close();

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "writeToFile", "", "", "Exception - ", e);
      return false;
    }
  }

  private boolean checkRuleNameExist(String[] arrRuleNames, String ruleName)
  {
    try
    {
      Log.debugLog(className, "checkRuleNameExist", "", "", "Method Called.");
      if(arrRuleNames.length == 0)
      {
        Log.debugLog(className, "checkRuleNameExist", "", "", "Rule Name does not exist in array.");
        return false;
      }
      else
      {
        for(int i = 0 ; i < arrRuleNames.length; i++)
        {
          String tmpRuleName = arrRuleNames[i].trim();
          if(tmpRuleName.equals(ruleName))
            return true;
        }
        
        return false;
      }
      
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "checkRuleNameExist", "", "", "Exception - ", ex);
      return false;
    }
  }
  
  // this function return the rule information in arraylist contain object of
  // AdvisoryRuleInfo
  public LinkedHashMap getHashMapOfRuleDataInfoObj(String[] arrRuleNames)
  {
    try
    {
      Log.debugLog(className, "getHashMapOfRuleDataInfoObj", "", "", "Method called. arrRuleNames = " + arrRuleNames + ", numTestRun = " + numTestRun);
      String narFilePath = getNarPathByFileName(narFileName);
      Vector vecData = rptUtilsBean.readFileInVector(narFilePath);
      if (vecData == null)
      {
        Log.errorLog(className, "getHashMapOfRuleDataInfoObj", "", "", "Cannot get data from rule definition file. nar file may not exist.");
        return null;
      }

      //creating report data object to get graph name object
      ReportData data = new ReportData(Integer.parseInt(numTestRun));
      
      // creating graphName object to get the graph index and name
      graphNamesObj = data.createGraphNamesObj();
      
      LinkedHashMap hashMapAdvisoryRuleInfoObj = new LinkedHashMap();
      String dataLine = "";
      for (int i = 0; i < vecData.size(); i++)
      {
        try
        {
          dataLine = vecData.get(i).toString();
          
          if(dataLine.startsWith("#"))
            continue;
          
          Log.debugLog(className, "getHashMapOfRuleDataInfoObj", "", "", "Reading data line = " + dataLine);
          String[] arrNarData = rptUtilsBean.strToArrayData(dataLine, "|");
          // if rule line length is less than 21 then we need to check is this old file format
          if (arrNarData.length > 20)
          {
            // check if rule is not active then no need to save in memory
            if(arrNarData[10].trim().equalsIgnoreCase("no"))
            {
              Log.debugLog(className, "getHashMapOfRuleDataInfoObj", "", "", "Data line = " + dataLine + " is not active.");
              continue;
            }
            
            String ruleName = arrNarData[14];
            if(arrRuleNames != null)
            {
              boolean flag = checkRuleNameExist(arrRuleNames, ruleName);
              if(!flag)
              {
                Log.debugLog(className, "getHashMapOfRuleDataInfoObj", "", "", "Rule Name = " + ruleName + " is not present in array.");
                continue;
              }
            }
            
            String arrSplitWithDot[] = rptUtilsBean.strToArrayData(arrNarData[3], ".");
            Log.debugLog(className, "getHashMapOfRuleDataInfoObj", "", "", "arrNarData[3] = " + arrNarData[3]); 
            String strGroupId = "";
            String strGraphId = "";
            String strVecName = "";

            // if length is one means header line
            if (arrSplitWithDot.length == 1)
            {
              strGroupId = "Group Id";
              strGraphId = "Graph Id";
              strVecName = "Vector Name";
            }
            else
            {
              strGroupId = arrSplitWithDot[0];
              strGraphId = arrSplitWithDot[1];

              // If length is greater than one means vector name may be 192.168.1.70
              // contcatenate string
              if (arrSplitWithDot.length > 3)
              {
                for (int k = 2; k < arrSplitWithDot.length; k++)
                {
                  if (strVecName.equals(""))
                    strVecName = arrSplitWithDot[k];
                  else
                    strVecName = strVecName + "." + arrSplitWithDot[k];
                }
              }
              else
                // otherwise its a single vector name
                strVecName = arrSplitWithDot[2];
            }

            Log.debugLog(className, "getHashMapOfRuleDataInfoObj", "", "", "after checking strVecName = = " + strVecName); 
            
            String arrVecTemp[] = null;

            // Maintain the array for loop b'coz we will treat individual event
            // other value will remain same
            /**
             * If NA - no vector name If All - Collect all vector name through
             * graph name If 2xx;4xx;3xx;5xx - split with semicolon
             */
            
            Log.debugLog(className, "getHashMapOfRuleDataInfoObj", "", "", "before decide case strVecName = " + strVecName + ", strGroupId = " + strGroupId + ", strGraphId = " + strGraphId); 
            
            if (strVecName.trim().equals("NA"))
            {
              arrVecTemp = new String[] { strVecName.trim() };
              Log.debugLog(className, "getHashMapOfRuleDataInfoObj", "", "", "vector name is NA ");
            }
            else if (strVecName.trim().toUpperCase().equals("ALL"))
            {
              Log.debugLog(className, "getHashMapOfRuleDataInfoObj", "", "", "vector name is ALL ");
              String grpGraphType = graphNamesObj.getGroupTypeGraphTypeAndNumOfIndices(Integer.parseInt(strGroupId), Integer.parseInt(strGraphId));
              String[] arrGroupGraphType = rptUtilsBean.strToArrayData(grpGraphType, "|");
              // check Group Type vector
              if(arrGroupGraphType[0].trim().equals("vector"))
                arrVecTemp = graphNamesObj.getNameOfGroupIndicesByGroupId(Integer.parseInt(strGroupId));
              else
                arrVecTemp = graphNamesObj.getNameOfIndicesByGroupIdAndGraphId(Integer.parseInt(strGroupId), Integer.parseInt(strGraphId));

              Log.debugLog(className, "getHashMapOfRuleDataInfoObj", "", "", "vector name is ALL " + arrVecTemp.length);
            }
            else
            {
              Log.debugLog(className, "getHashMapOfRuleDataInfoObj", "", "", "other case");
              arrVecTemp = rptUtilsBean.strToArrayData(strVecName, ";");
            }

            for(int ii = 0; ii < arrVecTemp.length; ii++)
              Log.debugLog(className, "getHashMapOfRuleDataInfoObj", "", "", "arrVecTemp = " + arrVecTemp[ii]);
            
            for (int k = 0; k < arrVecTemp.length; k++)
            {
              AdvisoryRuleInfo advisoryRuleInfoObj = new AdvisoryRuleInfo(graphNamesObj);
              String tmpRuleType = arrNarData[0].trim();
              Log.debugLog(className, "getHashMapOfRuleDataInfoObj", "", "", "tmpRuleType = " + tmpRuleType);
              advisoryRuleInfoObj.setRuleType(tmpRuleType);
              String ruleId = arrNarData[1].trim();
              Log.debugLog(className, "getHashMapOfRuleDataInfoObj", "", "", "ruleId = " + ruleId);
              advisoryRuleInfoObj.setRuleId(ruleId);

              String thresholdType = arrNarData[2];
              Log.debugLog(className, "getHashMapOfRuleDataInfoObj", "", "", "thresholdType = " + thresholdType);
              advisoryRuleInfoObj.setThresholdType(thresholdType);
              String graphDef = arrNarData[3].trim();
              Log.debugLog(className, "getHashMapOfRuleDataInfoObj", "", "", "graphDef = " + graphDef);
              advisoryRuleInfoObj.setGraphDef(graphDef);

              Log.debugLog(className, "getHashMapOfRuleDataInfoObj", "", "", "ruleName = " + ruleName);
              advisoryRuleInfoObj.setRuleName(ruleName);

              String alertMessage = arrNarData[15].trim();
              Log.debugLog(className, "getHashMapOfRuleDataInfoObj", "", "", "alertMessage = " + alertMessage);
              advisoryRuleInfoObj.setAlertMessage(alertMessage);

              advisoryRuleInfoObj.setGroupId(strGroupId);
              Log.debugLog(className, "getHashMapOfRuleDataInfoObj", "", "", "strGroupId = " + strGroupId);

              advisoryRuleInfoObj.setGraphId(strGraphId);
              Log.debugLog(className, "getHashMapOfRuleDataInfoObj", "", "", "strGraphId = " + strGraphId);

              String vectName = arrVecTemp[k].trim();
              Log.debugLog(className, "getHashMapOfRuleDataInfoObj", "", "", "vectName = " + vectName);
              advisoryRuleInfoObj.setVecName(vectName);

              String operation = arrNarData[4].trim();
              Log.debugLog(className, "getHashMapOfRuleDataInfoObj", "", "", "operation = " + operation);
              advisoryRuleInfoObj.setOperation(operation);

              String tmpValue = arrNarData[5].trim();
              advisoryRuleInfoObj.setValue(tmpValue);
              Log.debugLog(className, "getHashMapOfRuleDataInfoObj", "", "", "tmpValue = " + tmpValue);

              String pctChange = arrNarData[8].trim();
              Log.debugLog(className, "getHashMapOfRuleDataInfoObj", "", "", "pctChange = " + pctChange);
              advisoryRuleInfoObj.setPctChange(pctChange);

              String timeWindow = arrNarData[9].trim();
              advisoryRuleInfoObj.setTimeWindow(timeWindow);
              Log.debugLog(className, "getHashMapOfRuleDataInfoObj", "", "", "timeWindow = " + timeWindow);

              String desc = arrNarData[13].trim();
              advisoryRuleInfoObj.setDescription(desc);
              Log.debugLog(className, "getHashMapOfRuleDataInfoObj", "", "", "desc = " + desc);

              advisoryRuleInfoObj.setGraphIndexAndName();

              if (!advisoryRuleInfoObj.getRuleId().equals("Rule Id"))
              {
                String advisoryRuleKey = advisoryRuleInfoObj.getGroupId() + "." + advisoryRuleInfoObj.getGraphId() + "." + advisoryRuleInfoObj.getVecName() + "-" + advisoryRuleInfoObj.getRuleId();
                if ((ruleType == 0) && advisoryRuleInfoObj.getRuleType().equals(THRESHOLD_TYPE))
                  hashMapAdvisoryRuleInfoObj.put(advisoryRuleKey, advisoryRuleInfoObj);
                if ((ruleType == 1) && advisoryRuleInfoObj.getRuleType().equals(COMPARE_TYPE))
                  hashMapAdvisoryRuleInfoObj.put(advisoryRuleKey, advisoryRuleInfoObj);
                else if (ruleType == BOTH_TYPE)
                  hashMapAdvisoryRuleInfoObj.put(advisoryRuleKey, advisoryRuleInfoObj);
              }
              else
              {
                Log.errorLog(className, "getHashMapOfRuleDataInfoObj", "", "", "Data line is not correct. Ignoring rule - " + dataLine);
                continue;
              }
            }
          }
          else
          {
            Log.errorLog(dataLine, "getHashMapOfRuleDataInfoObj", "", "", "Ignoring rule - " + dataLine);
            continue;
          }
        }
        catch (Exception ex)
        {
          Log.errorLog(className, "getHashMapOfRuleDataInfoObj", "", "", "Exception - " + ex);
          continue;
        }
      }

      return hashMapAdvisoryRuleInfoObj;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getHashMapOfRuleDataInfoObj", "", "", "Exception - ", e);
      return null;
    }
  }

  public boolean updateActiveStatus(String status, String[] ruleIds)
  {
    Log.debugLog(className, "updateActiveStatus", "", "", "Method called status = " + status);
    Log.debugLog(className, "updateActiveStatus", "", "", "Method called rulesIds length = " + ruleIds.length);

    try
    {
      Vector vecData = rptUtilsBean.readFileInVector(getNarPathByFileName(narFileName));
      Vector newVecData = new Vector();

      if(vecData == null)
      {
        return false;
      }

      String dataLine = "";
      for(int i = 0; i < vecData.size(); i++)
      {
        String[] tmp = rptUtilsBean.strToArrayData(vecData.get(i).toString(), "|");
        boolean found = false;
        for(int j = 0;  j < ruleIds.length; j++)
        {
          if(tmp[1].equals(ruleIds[j]))
          {
         Log.debugLog(className, "updateActiveStatus", "", "", "Id match" + tmp[1]);
         tmp[10] = status;
            for(int k = 0; k < tmp.length; k++)
            {
              if(k == 0)
                dataLine = tmp[k];
              else
             dataLine += "|" + tmp[k];
            }
            found = true;
            newVecData.add(dataLine);
            break;
          }
        }
        
        if(!found)
        {
          Log.debugLog(className, "updateActiveStatus", "", "", "not match" + tmp[1]);
          newVecData.add(vecData.get(i).toString());
        }
      }

      if(!writeToFile(newVecData))
        return false;

      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "updateRule", "", "", "Exception - ", e);
      return false;
    }
  }
  
  public static void main(String args[])
  {
    if(args.length >= 2)
    {
      String strTRNum = args[0];
      String strFileName = args[1];
      int ruleType = 2;
      //AdvisoryRule advisoryRule_Obj = new AdvisoryRule("46993", "sampleFile");

      AdvisoryRule advisoryRule_Obj = new AdvisoryRule(strTRNum, strFileName, ruleType);

      /**String arrTemp[][] = advisoryRule_Obj.getAllData();
      for(int i = 0; i < arrTemp.length; i++)
      {
        for(int j = 0; j < arrTemp[i].length; j++)
        {
          System.out.print(arrTemp[i][j] + ", ");
        }
        System.out.println("\n");
      }**/

      LinkedHashMap hashMapObj = advisoryRule_Obj.getHashMapOfRuleDataInfoObj(null);

      if(hashMapObj != null)
      {
        //Set st = hashMapObj.keySet();
        Iterator itr = hashMapObj.keySet().iterator();
        while(itr.hasNext())
        {
          Object key = itr.next();
          //System.out.println("itr.next() = " + key);
          AdvisoryRuleInfo advisoryRuleInfoObj = (AdvisoryRuleInfo)hashMapObj.get(key);
          System.out.println("Rule Type = " + advisoryRuleInfoObj.getRuleType() + ", Rule Id = " + advisoryRuleInfoObj.getRuleId() + ", Threshold Type = " + advisoryRuleInfoObj.getThresholdType() + ", Operation = " + advisoryRuleInfoObj.getOperation() + ", Value = " + advisoryRuleInfoObj.getValue() +  ", Percentage change = " + advisoryRuleInfoObj.getPctChange() + ", Time window = " + advisoryRuleInfoObj.getTimeWindow() + ", Description = " + advisoryRuleInfoObj.getDescription() + ", Group Id = " + advisoryRuleInfoObj.getGroupId() + ", Graph Id = " + advisoryRuleInfoObj.getGraphId() + ", vector name = " + advisoryRuleInfoObj.getVecName() + ", Graph Index = " + advisoryRuleInfoObj.getGraphDataIndex() + ", Graph name = " + advisoryRuleInfoObj.getStrGraphName() + ", Interval = " + advisoryRuleInfoObj.getInterval());
        }
      }
    }
    else
      System.out.println("Please enter Test run number and nar file name.");
  }
}

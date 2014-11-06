/**----------------------------------------------------------------------------
 * Name       TreeStructure.java
 * Purpose
 * @author    Atul
 * Modification History
 *---------------------------------------------------------------------------**/

package pac1.Bean;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

public class ScriptTree implements java.io.Serializable
{
  private static String className = "ScriptTree";
  private ArrayList listProjName = new ArrayList();
  private ArrayList listSubProjName = new ArrayList();
  private int isFirstCall = 0;
  private Hashtable hashTable = new Hashtable();
  public Hashtable tableForManuallyCreatedProjSubProj = new Hashtable();
  private Hashtable hashTableToScript = new Hashtable();
  ScriptNode rootNode;
  Boolean isForDiff = false;
  String openFromDiff = "";

  public ScriptTree(){}

  /**
   * This methods creates Script tree based upon it is called from which GUI
   * @param userName
   * @param isForAll
   * @param openFrom
   * @return
   */
  public Object getScriptTreeRootNode(String userName, boolean isForAll, String openFrom, boolean showRegistration, ArrayList projSubScriptArr)
  {
    Log.debugLog(className, "getScriptTreeRootNode", "", "","Method called--- openFrom = " + openFrom + ", userName = " + userName);
    try
    {
      StringBuffer absolutePath = new StringBuffer();
      String strOpennValue = openFrom;
      //if Script diff openFrom Deshboard then we check it start with DeshBoard
      if(openFrom.startsWith("TR") && openFrom.endsWith("_DIFF"))
      {
        setOpenFromDiff("DashBoard"); 
        return getTrScriptForDiff(userName, isForAll, openFrom, showRegistration);
      }
      
      if(openFrom.equalsIgnoreCase("Version_DIFF") || openFrom.equalsIgnoreCase("User_DIFF") || (openFrom.indexOf("/") != -1) && openFrom.contains("_DIFF")) 
      {
        setOpenFromDiff(openFrom);
        openFrom = openFrom.substring(0, openFrom.lastIndexOf("_"));
        setIsForDiff(true);
      }
      if(!checkScriptExistence(openFrom, absolutePath))//Check for scripts directory existence
      {
        String errMsg = "Scripts directory not found.";
        return (Object) errMsg;
      }

      rootNode = new ScriptNode(getRootNodeName(openFrom));
      
      /***
       * get the project name for the user, scenario or testrun by running command
       */
      Vector vecProjNameTemp = getScriptList(userName, isForAll, openFrom, projSubScriptArr);
      /**       
       Here no need to get subprojects, just get all 
       the project name and set them into the vector to ignore 
       which is not present in that vector
       */
      
      if((vecProjNameTemp == null) || (vecProjNameTemp.size() <= 0))
      {
         Log.errorLog(className, "getScriptsNode", "", "", "project name can not be get for the user = " + userName);
         return null;
      }
      //this is done only for the scripts tree which is opened from scriptmanagement means openFrom = User
      //because we need the createHashTable( before making the tree
      if(openFrom.startsWith("User"))
      {
        if(openFrom.startsWith("TR") || openFrom.indexOf("/") != -1)
        {}
        else if(!createHashTable(userName, isForAll))
        {
          Log.errorLog(className, "getScriptTreeRootNode", "", "", "Can not get project subproject for hashTable");
          String errMsg = "No project available for - '" + userName + "'";
          return (Object)errMsg;
        }
      }

      //Project|Sub-Project|Script|Owner|Group|Permission|Modification Date
      for(int i = 1 ; i < vecProjNameTemp.size() ; i++)
      {
        String str[] = rptUtilsBean.split(vecProjNameTemp.get(i).toString(), "|");
        
        String projName = str[0];
        String subProjName = str[1];
        String scriptName = str[2];
        
        String permission = str[5];

        boolean canRead = getPermission(permission, "r");
        boolean canWrite = getPermission(permission, "w");
        boolean canDelete = getPermission(permission, "d");

        Log.debugLog(className, "getScriptTreeRootNode", "", "","the project and subproject name = "+projName + "   "+subProjName);
        if(!openFrom.startsWith("TR") && (openFrom.indexOf("/") == -1) && (!strOpennValue.equals("Version_DIFF")))
        {
          if(!hashTable.containsKey(projName))
          {
            userCreatedProjectSubProjList(projName, subProjName);   
            continue;
          }   
          if(!((ArrayList) hashTable.get(projName)).contains(subProjName))
          {
            userCreatedProjectSubProjList(projName, subProjName);
            continue;
          }
        }
         
        if(!listProjName.contains(projName))//If new Project Name found
        {
          Log.debugLog(className, "getScriptTreeRootNode", "", "","New project name found. name = " + projName);
          ScriptNode projNode = new ScriptNode(projName);
          rootNode.add(projNode);
          
          listProjName.add(projName);
          listSubProjName.clear();
        }
        
        if(!listSubProjName.contains(subProjName))//If new subproject name found in that project
        { 
          Log.debugLog(className, "getScriptTreeRootNode", "", "","New subproject name found. projName = " + projName + ", subProjectName = " + subProjName);
          ScriptNode subProjNode = new ScriptNode(subProjName);
          ((ScriptNode)rootNode.getLastChild()).add(subProjNode);
          
          listSubProjName.add(subProjName);
        }
        
        String keyForScript = projName + "/" + subProjName;
        if(!hashTableToScript.containsKey(keyForScript))
        {
          ArrayList scriptList = new ArrayList();
          scriptList.add(scriptName);
          
          hashTableToScript.put(keyForScript, scriptList);
        }
        else
        {
          ArrayList temp = (ArrayList)hashTableToScript.get(keyForScript);
          temp.add(scriptName);
          
//          hashTableToScript.put(keyForScript,temp);
        }
        
        Log.debugLog(className, "getScriptTreeRootNode", "", "","adding script. projName = " + projName + ", subProjectName = " + subProjName + ", scriptName = " + scriptName);
        //To add script at the specified position
        ScriptNode scriptNode = new ScriptNode(scriptName);
        ((ScriptNode)((ScriptNode)rootNode.getLastChild()).getLastChild()).add(scriptNode);
        
        String absolutePathOfDir = absolutePath.toString();
        
        Log.debugLog(className, "getScriptTreeRootNode", "", "","openFrom version = " + openFrom.startsWith("Version"));
        if(openFrom.startsWith("Version"))
        {
          if(scriptName.contains("("))
          {
            String version = scriptName.substring(scriptName.indexOf("(")+1, scriptName.length()-1);
            scriptName = scriptName.substring(0, scriptName.indexOf("("));
            absolutePathOfDir = absolutePathOfDir + "/" + projName + "/" + subProjName + "/" + scriptName + "/" + ".version" + "/" + version;
            Log.debugLog(className, "getScriptTreeRootNode", "", "","absolute path for version = " + absolutePathOfDir);
          }
          else
            absolutePathOfDir = absolutePathOfDir + "/" + projName + "/" + subProjName + "/" + scriptName;   
        }
        
        if((openFrom.equalsIgnoreCase("USER")) || (openFrom.indexOf("/") != -1))
        {
          absolutePathOfDir = absolutePathOfDir + "/" + projName + "/" + subProjName + "/" + scriptName;
        }
        else if(openFrom.startsWith("TR"))
        {
          absolutePathOfDir = absolutePathOfDir + "/" + scriptName;
        }
        scriptNode = getScriptsNode(absolutePathOfDir, scriptNode, showRegistration);
        if(scriptNode == null)
        {
          Log.errorLog(className, "getScriptTreeRootNode", "", "", "getScriptsNode() method returning null");
          String errMsg = "Error in getting scripts.";
          return (Object)errMsg;
        }
        if(!strOpennValue.equals("User_DIFF") && (!strOpennValue.equals("Version_DIFF")))
        {  
          ScriptNode runLogicNode = new ScriptNode("runlogic");
          runLogicNode.setType(ScriptNode.USER_RUNLOGICNODE);
          scriptNode.add(runLogicNode);
        }
        scriptNode.setIsPermSet(true);
        scriptNode.setCanDelete(canDelete);
        scriptNode.setCanWrite(canWrite);
        scriptNode.setCanRead(canRead);
        
        //populate proj/subproject hashtable if opening GUI from testrun
        if(openFrom.startsWith("TR") || openFrom.indexOf("/") != -1)
        {
          Log.debugLog(className, "getScriptTreeRootNode", "", "", "Populating list of project " + projName + ", subproject " + subProjName + " for hashTable");
          if (hashTable.containsKey(projName))
          {
            ArrayList listTemp = (ArrayList) hashTable.get(projName);
            if(!listTemp.contains(subProjName))//No need to add subproject if already exist
              listTemp.add(subProjName);
          }
          else
          {
            ArrayList listTemp = new ArrayList();
            listTemp.add(subProjName);
            hashTable.put(projName, listTemp);
          }
        }
        
      }

      //this is deone as usual before
      if(!openFrom.startsWith("User"))
      {
        if(openFrom.startsWith("TR") || openFrom.indexOf("/") != -1)
        {}
        else if(!createHashTable(userName, isForAll))
        {
          Log.errorLog(className, "getScriptTreeRootNode", "", "", "Can not get project subproject for hashTable");
          String errMsg = "No project available for - '" + userName + "'";
          return (Object)errMsg;
        }
      }
      return (Object)this;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getScriptTreeRootNode", "", "", "Exception in getScriptTreeRootNode() -", e);
      String errMsg = "Error in getting scripts..";
      return (Object)errMsg;
    }
  }

  private void userCreatedProjectSubProjList(String projName, String subProjName)
  {
    if (tableForManuallyCreatedProjSubProj.containsKey(projName))
    {
      ArrayList listTemp = (ArrayList) tableForManuallyCreatedProjSubProj.get(projName);
      if(!listTemp.contains(subProjName))//No need to add subproject if already exist
        listTemp.add(subProjName);
    }
    else
    {
      ArrayList listTemp = new ArrayList();
      listTemp.add(subProjName);
      tableForManuallyCreatedProjSubProj.put(projName, listTemp);
    }
  }
  
  /**
   * This method create script tree to show test runs scripts from TR(s)
   * Script tree
   * Root Node = Compare TestRuns Scripts
   * TRXXX node is child node of root node
   * script node is child node of TR 
   * @param userName
   * @param isForAll
   * @param openFrom
   * @param showRegistration
   * @return
   */
  public Object getTrScriptForDiff(String userName, boolean isForAll, String openFrom, boolean showRegistration)
  {
    Log.debugLog(className, "getTrScriptForDiff", "", "","Method called openFrom = " + openFrom + ", userName = " + userName);
    try
    {
      rootNode = new ScriptNode(getRootNodeName(openFrom));
      
      ArrayList TRArray = new ArrayList();
      String[] TRNumbers = openFrom.split("_");

      for(int j = 0 ; j < TRNumbers.length; j++)//this loop run for all TR for compare diff
      {
        String TrStr = TRNumbers[j];
        if(TrStr.indexOf("DIFF") > -1)
          continue;
        TRArray.add(TrStr);//put all TR into array
      }
      
      /***
       * get the project name for the user, scenario or testrun by running command
       */
      Log.debugLog(className, "getTrScriptForDiff", "", "","Method called open from = " + TRArray);
      HashMap hashMap = new HashMap();
      ArrayList tempScriptLineVec = new ArrayList();
      for(int i = 0 ; i < TRArray.size(); i++)
      {
        Vector projectSubProArr = new Vector();
        Vector tempVecProjName = getScriptList(userName, isForAll, TRArray.get(i).toString(), TRArray);
        Log.debugLog(className, "getTrScriptForDiff", "", "","tempVecProjName = " + tempVecProjName);
        for(int ii = 1 ; ii < tempVecProjName.size(); ii++)
        {
          String[] tempStrLine = rptUtilsBean.split(tempVecProjName.get(ii).toString(), "|");
          String trScriptLine = TRArray.get(i).toString() + "/" + tempStrLine[2].toString();
          projectSubProArr.add(tempVecProjName.get(ii).toString());
          tempScriptLineVec.add(trScriptLine);
        }
        hashMap.put(TRArray.get(i).toString(), projectSubProArr);//key=TR,value=array of proj|sub|scritp|....
        setListProjName(tempScriptLineVec);
      }
      Log.debugLog(className, "getTrScriptForDiff", "", "","hashMap = " + hashMap);
      /**       
       Here no need to get subprojects, just get all 
       the project name and set them into the vector to ignore 
       which is not present in that vector
       */
      
      if((hashMap == null) || (hashMap.size() <= 0))
      {
         Log.errorLog(className, "getTrScriptForDiff", "", "", "project name can not be get for the user = " + userName);
         return null;
      }
      Log.debugLog(className, "getTrScriptForDiff", "", "","TRArray size = " + TRArray.size());   
      for(int i = 0 ; i < TRArray.size(); i++)//this loop run for all TR scripts diff
      {
        Log.debugLog(className, "getTrScriptForDiff", "", "","TR Node Name = " + TRArray.get(i).toString());
        StringBuffer absolutePath = new StringBuffer();
        if(!checkScriptExistence(TRArray.get(i).toString(), absolutePath))//Check for scripts directory existence
        {
          String errMsg = "Scripts directory not found.";
          return (Object) errMsg;
        }
        Log.debugLog(className, "getTrScriptForDiff", "", "","TR Node created = " + TRArray.get(i).toString());
        ScriptNode trNode = new ScriptNode(TRArray.get(i).toString());
        rootNode.add(trNode);
        Vector vecProjNameTemp = (Vector)hashMap.get(TRArray.get(i));
        for(int k = 0 ; k < vecProjNameTemp.size() ; k++)
        {
          Log.debugLog(className, "getTrScriptForDiff", "", "","Script Line = " + vecProjNameTemp.get(k).toString());
          String str[] = rptUtilsBean.split(vecProjNameTemp.get(k).toString(), "|");
          String projName = str[0];
          String subProjName = str[1];
          String scriptName = str[2];
          String permission = str[5];

          boolean canRead = getPermission(permission, "r");
          boolean canWrite = getPermission(permission, "w");
          boolean canDelete = getPermission(permission, "d");
          Log.debugLog(className, "getTrScriptForDiff", "", "","Script Node name = " + scriptName);  
          
          ScriptNode scriptNode = new ScriptNode(scriptName);
          ((ScriptNode)((ScriptNode)rootNode.getLastChild())).add(scriptNode);

          String absolutePathOfDir = absolutePath.toString();
          absolutePathOfDir = absolutePathOfDir + "/" + scriptName;
          Log.debugLog(className, "getTrScriptForDiff", "", "","absolutePathOfDir = " + absolutePathOfDir);
          scriptNode = getScriptsNode(absolutePathOfDir, scriptNode, true);
          if(scriptNode == null)
          {
            Log.errorLog(className, "getTrScriptForDiff", "", "", "getScriptsNode() method returning null");
            String errMsg = "Error in getting scripts.";
            return (Object)errMsg;
          }
          scriptNode.setIsPermSet(true);
          scriptNode.setCanDelete(canDelete);
          scriptNode.setCanWrite(canWrite);
          scriptNode.setCanRead(canRead);
        }
      }
      return (Object)this;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getTrScriptForDiff", "", "", "Exception in getScriptTreeRootNode() -", e);
      String errMsg = "Error in getting scripts...";
      return (Object)errMsg;
    }
  }
  
  
  /**
   * This method checks whether the script directory exists and sets absolute path for all cases
   * for User and scenario- scripts/proj/subproj/scriptname
   * for TR - logs/TRXXXX/scripts
   * @param openFrom
   * @param absolutePath
   * @return
   */
  private boolean checkScriptExistence(String openFrom, StringBuffer absolutePath)
  {
    Log.debugLog(className, "checkScriptExistence", "", "","openFrom = " + openFrom + ",absolutePath = " + absolutePath);
    try
    {
      String dirRelativePath = "/webapps";
      String dirName = "/scripts";

      String absolutePathOfDir = Config.getWorkPath() + dirRelativePath + dirName;
      
      Log.debugLog(className, "checkScriptExistence", "", "","Method called. openFrom = " + openFrom);
      //when Script gui is called from menu tab or scenario gui
      //for version diff we have same path for script version diff
      if(openFrom.equalsIgnoreCase("USER") || openFrom.equalsIgnoreCase("Version") || (openFrom.indexOf("/") != -1))
      {
        Log.debugLog(className, "checkScriptExistence", "", "","Method called for User.");
        dirRelativePath = "/webapps";
        dirName = "/scripts";
      }
      //if it is called from test run gui
      if(openFrom.startsWith("TR"))
      {
        Log.debugLog(className, "checkScriptExistence", "", "","Method called for TestRun.");
        dirRelativePath = "/logs/" + openFrom;
        dirName = "/scripts";
      }
      
      absolutePathOfDir = Config.getWorkPath() + dirRelativePath + dirName;
      absolutePath.append(absolutePathOfDir);
      
      Log.debugLog(className, "checkScriptExistence", "", "","Checking for Existence of directory. absolutePathOfDir = " + absolutePathOfDir);
      File fileForScript = new File(absolutePathOfDir);
      if(!fileForScript.exists())//Check for scripts directory existence
      {
        //String errMsg = "Scripts directory not found.";
        return false;
      }

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "checkScriptExistence", "", "", "Exception in getting Script Directory -", e);
      return false;
    }
  }
  
  /**
   * This method runs command to get script list with following details
   * Project|Sub-Project|Script|Owner|Group|Permission|Modification Date
   * @param userName
   * @param isForAll
   * @param openFrom
   * @return
   */
  private Vector getScriptList(String userName, boolean isForAll, String openFrom, ArrayList projSubScriptArr)
  {
    try
    {
      CmdExec cmdExec = new CmdExec();
      String args = null;
      Log.debugLog(className, "getScriptList", "", "","Method called. openFrom = " + openFrom + ", userName = " + userName);
      
      //when Script gui is called from menu tab
      if(openFrom.equalsIgnoreCase("USER"))
      {
        Log.debugLog(className, "getScriptList", "", "","Method called for User.");
        Log.debugLog(className, "getScriptList.", "", "","Method called. openFrom = " + openFrom);   
          if(!isForAll)
          {
            args = "-u " + userName;
          }
          else
            args = "-A";
      }
      //if it is called from Scenario gui
      if(openFrom.indexOf("/") != -1)
      {
        Log.debugLog(className, "getScriptList", "", "","Method called for Scenario.");
        args = "-s " + openFrom;
      }
      //if it is called from test run gui
      if(openFrom.startsWith("TR"))
      {
        Log.debugLog(className, "getScriptList", "", "","Method called for TestRun.");
        args = "-t " + openFrom.substring(2);
      }
      
      Log.debugLog(className, "getScriptList", "", "","Checking for Existence of directory.");
      
      Vector vecProjNameTemp = cmdExec.getResultByCommand("nsu_show_scripts", args, CmdExec.NETSTORM_CMD, userName, null);
      Log.debugLog(className, "getScriptList", "", "","output = " + vecProjNameTemp);
      if(getIsForDiff())
      {
        vecProjNameTemp = getScriptDiffProjectSubProjectDetail(vecProjNameTemp, projSubScriptArr);
        Log.debugLog(className, "getScriptList", "", "","output for diff = " + vecProjNameTemp);
      }
      
      return vecProjNameTemp;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getScriptList", "", "", "Exception in getting Script Directory -", e);
      return null;
    }
  }
  
  /**
   * This method create a modify map which contain only diff scripts info
   * @param vecProjNameTemp
   * @param firstScript
   * @param secondScript
   * @return
   */
  private Vector getScriptDiffProjectSubProjectDetail(Vector vecProjNameTemp, ArrayList projSubScriptArr)
  {
    Vector modifyVecProjNameTemp = new Vector(); 
    try
    {
    Log.debugLog(className, "getScriptDiffProjectSubProjectDetail", "", "","Method call projSubScriptArr = " + projSubScriptArr);
    modifyVecProjNameTemp.add("Project|Subproject|Script|Owner|Group|Permission|Modification Date");
    /**
     * In version diff case array contain
     * case 1: array contain project/subproject/script
     * case 2: array contain script version no. 
     */
    Log.debugLog(className, "getScriptDiffProjectSubProjectDetail", "", "","getOpenFromDiff() = " + getOpenFromDiff());
    if(getOpenFromDiff().equalsIgnoreCase("Version_DIFF"))
    {
      String str[] = rptUtilsBean.split(projSubScriptArr.get(0).toString(), "/");
      String projName = str[0];
      String subProjName = str[1];
      String scriptName = str[2];
      String firstProjSubScriptLine = projName + "/" + subProjName + "/" + scriptName;  
      Log.debugLog(className, "getScriptDiffProjectSubProjectDetail", "", "","firstProjSubScriptLine = " + firstProjSubScriptLine);   
      if(projSubScriptArr.size() >= 3)
      {
        for(int ii = 1 ; ii < projSubScriptArr.size(); ii++)
        {
          for(int i = 1 ; i < vecProjNameTemp.size(); i++)
          {
            String strLine[] = rptUtilsBean.split(vecProjNameTemp.get(i).toString(), "|");
            String proj = strLine[0];
            String subProj = strLine[1];
            String script = strLine[2];
            String tmpLine = proj + "/" + subProj + "/" + script;
            
            if(tmpLine.equals(firstProjSubScriptLine))
            {
              Log.debugLog(className, "getScriptDiffProjectSubProjectDetail", "", "","Detail = " + vecProjNameTemp.get(i));
              String[] strScriptPathLine = rptUtilsBean.split(vecProjNameTemp.get(i).toString(), "|");
              String tmpScriptLine = strScriptPathLine[0] + "|" + strScriptPathLine[1] + "|" + strScriptPathLine[2] + "(" + projSubScriptArr.get(ii).toString() + ")" + "|" + strScriptPathLine[3] + "|" + strScriptPathLine[4] + "|" + strScriptPathLine[5]; 
              modifyVecProjNameTemp.add(tmpScriptLine);
            }
          }
        }
      }
      else
      {
        String tmpStr[] = rptUtilsBean.split(projSubScriptArr.get(0).toString(), "/");
        String proName = tmpStr[0];
        String subProName = tmpStr[1];
        String tmpScriptname = tmpStr[2];
        String tmpScriptpathLine = proName + "/" + subProName + "/" + tmpScriptname;     
        Log.debugLog(className, "getScriptDiffProjectSubProjectDetail", "", "","tmpScriptpathLine = " + tmpScriptpathLine);
        for(int ii = 0 ; ii < projSubScriptArr.size(); ii++)
        {
        for(int i = 1 ; i < vecProjNameTemp.size(); i++)
        {
          String strLine[] = rptUtilsBean.split(vecProjNameTemp.get(i).toString(), "|");
          
          String proj = strLine[0];
          String subProj = strLine[1];
          String script = strLine[2];
          String tmpLine = proj + "/" + subProj + "/" + script;
          Log.debugLog(className, "getScriptDiffProjectSubProjectDetail", "", "","tmpLine.equals(tmpScriptpathLine) = " + tmpLine.equals(tmpScriptpathLine));     
          if(tmpLine.equals(tmpScriptpathLine))
          {
            Log.debugLog(className, "getScriptDiffProjectSubProjectDetail", "", "","Size of map = " + modifyVecProjNameTemp.size());    
            if(modifyVecProjNameTemp.size() >= 2)
            {
              String[] strScriptPathLine = rptUtilsBean.split(vecProjNameTemp.get(i).toString(), "|");
              String tmpScriptLine = strScriptPathLine[0] + "|" + strScriptPathLine[1] + "|" + strScriptPathLine[2] + "(" + projSubScriptArr.get(1).toString() + ")" + "|" + strScriptPathLine[3] + "|" + strScriptPathLine[4] + "|" + strScriptPathLine[5]; 
              modifyVecProjNameTemp.add(tmpScriptLine);
            }
            else
              modifyVecProjNameTemp.add(vecProjNameTemp.get(i));
            Log.debugLog(className, "getScriptDiffProjectSubProjectDetail", "", "","line add in array = " + modifyVecProjNameTemp);       
          }
        }
        }
        Log.debugLog(className, "getScriptDiffProjectSubProjectDetail", "", "","modifyVecProjNameTemp = " + modifyVecProjNameTemp);
      }
    }
    else
    {
      for(int ii = 0 ; ii < projSubScriptArr.size(); ii++)
      {
        String tmpSelNode = projSubScriptArr.get(ii).toString();
        String selectedNodeLine = tmpSelNode.substring(tmpSelNode.indexOf("["), tmpSelNode.lastIndexOf("]"));
        String[] tmpLineArr = selectedNodeLine.split(",");
        String firstProjSubScriptLine = tmpLineArr[1].toString().trim() + "/" + tmpLineArr[2].toString().trim() + "/" + tmpLineArr[3].toString().trim();   
        for(int i = 1 ; i < vecProjNameTemp.size(); i++)
        {
          String str[] = rptUtilsBean.split(vecProjNameTemp.get(i).toString(), "|");
          
          String projName = str[0];
          String subProjName = str[1];
          String scriptName = str[2];
          String tmpLine = projName + "/" + subProjName + "/" + scriptName;
          if(tmpLine.equals(firstProjSubScriptLine))
          {
            Log.debugLog(className, "getScriptDiffProjectSubProjectDetail", "", "","Detail = " + vecProjNameTemp.get(i));
            modifyVecProjNameTemp.add(vecProjNameTemp.get(i));
          }
        }
      }  
    }
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    return modifyVecProjNameTemp;
  }
  
  /**
   * get root node name
   * @param openFrom
   * @return
   */
  private String getRootNodeName(String openFrom)
  {
    Log.debugLog(className, "getRootNodeName", "", "","open from = " + openFrom + ", getIsForDiff() = " + getIsForDiff() + ", getOpenFromDiff() = "  + getOpenFromDiff());
    if(getIsForDiff())
    {
      if(getOpenFromDiff().equalsIgnoreCase("User_DIFF"))
      {
        return "Compare Scripts";
      }
      else if(getOpenFromDiff().equalsIgnoreCase("Version_DIFF"))
      {
        return "Compare Script Versions";
      }
      else if((getOpenFromDiff().indexOf("/") != -1) && getOpenFromDiff().contains("_DIFF"))
      {
        return "Compare Scenarios Scripts"; 
      }
      return "Available Script";
    }
    else
    {
      if(getOpenFromDiff().equals("DashBoard"))
        return "Compare TestRuns Scripts";
      else if((openFrom.startsWith("TR")) || (openFrom.indexOf("/") != -1))
        return openFrom + " Scripts";
      else  
        return "Available Scripts";
    }
  }
  
  /**
   This method is called separately for creating hashTable 
   even it could be get from the <nsu_show_script> as in getScriptTreeRootNode().
   
   But there is possibility when users have not any script in its project and
   subproject, at that condition 
   nsu_show_script will not give any info for PROJECT AND SUBPROJECT TOO as well as 
   for script.
    
   */
  private boolean createHashTable(String userName, boolean isForAll)
  {
    /***
     * get the project name for the user
     */
    CmdExec cmdExec = new CmdExec();
    String args = null;
    
    Log.debugLog(className, "createHashTable", "", "","UserName = " + userName);
      if(!isForAll)
        args = "-e -u " + userName;
      else
        args = "-A";
    
    Vector vecProjNameTemp = cmdExec.getResultByCommand("nsu_show_projects", args, CmdExec.NETSTORM_CMD, userName, null);
    
    /**       
     Here no need to get subprojects, just get all 
     the project name and set them into the vector to ignore 
     which is not present in that vector
     */
    
    //one [1] to remove header line also
    if((vecProjNameTemp == null) || (vecProjNameTemp.size() <= 1))
    {
       Log.errorLog(className, "createHashTable", "", "", "project name can not be get for the user = " + userName);
       return false;
    }
    //User|Project|Subproject
    for(int i = 1 ; i < vecProjNameTemp.size() ; i++)
    {
      String str[] = rptUtilsBean.split(vecProjNameTemp.get(i).toString(), "|");
      
      String projName = str[1];
      String subProjName = str[2];

      if (hashTable.containsKey(projName))
      {
        ArrayList listTemp = (ArrayList) hashTable.get(projName);
        if(!listTemp.contains(subProjName))//No need to add subproject if already exist
          listTemp.add(subProjName);
      }
      else
      {
        ArrayList listTemp = new ArrayList();
        listTemp.add(subProjName);
        hashTable.put(projName, listTemp);
      }
    }
    return true;
  }
  
  public ScriptNode getScriptNode()
  {
    return rootNode;
  }
  
  public Hashtable getHashTable()
  {
    return hashTable;
  }
  
  public Hashtable getHashTableToScript()
  {
    return hashTableToScript;
  }
  
  private boolean getPermission(String src, String type)
  {
    if((src.indexOf(type)) != -1)
      return true;
    else if((src.indexOf(type.toUpperCase())) != -1)
      return true;
    return false;
  }

  // This will return the scripts node which will contain all the scripts
  private ScriptNode getScriptsNode(String absolutePathOfDir, ScriptNode tempNode, boolean showRegistration)
  {
    Log.debugLog(className, "getScriptNode", "", "","Method called. absolutePathOfDir = " + absolutePathOfDir);

    try
    {
      //String dirRelativePath = "/webapps";
      //String nodeName = absolutePathOfDir.substring((absolutePathOfDir.lastIndexOf("/") + 1), absolutePathOfDir.length());


      String[] strListOfFiles = getListOfDir(absolutePathOfDir);

      
      if(strListOfFiles != null)
      {
        Arrays.sort(strListOfFiles);
        
        for (int i = 0; i < strListOfFiles.length; i++)
        {
          if((strListOfFiles[i].equals("WEB-INF"))
              || (strListOfFiles[i].equals("dump"))
              || (strListOfFiles[i].equals("temp"))
              || (strListOfFiles[i].equals("Snapshots"))
              || (strListOfFiles[i].equals("runlogic.c")) //runlogic.c file does not viewable in Script - release 3.8.5 sys_build 3.8.13
              || (strListOfFiles[i].equals("runlogic.java")) 
              || (strListOfFiles[i].startsWith("."))
              || (strListOfFiles[i].endsWith("~"))
              || (strListOfFiles[i].startsWith("#"))
              || (strListOfFiles[i].equals("registrations.spec") && !showRegistration))
            
            continue;
            
          
          //presently NO need to check it, because we are getting script name from nsu_show_scripts 
          /*if((nodeName.equals("Available Scripts")) && (!listProjName.contains(strListOfFiles[i])))
            continue;*/

          String strTemp = absolutePathOfDir + "/" + strListOfFiles[i];

          ScriptNode node = null;

          StringBuffer errMsg2 = new StringBuffer();

          File fileTemp = new File(strTemp);
          boolean checkDir = fileTemp.isDirectory();
          node = new ScriptNode(strListOfFiles[i]);
          if(checkDir)
          {
            node = getScriptsNode(strTemp, node, showRegistration);
            if(node != null)
              node.setType(ScriptNode.DIR);
          }
          else
          {
            if(node != null)
            {
              if(isValidChild(strTemp))
              {
                int type = getType(strListOfFiles[i]);
                node.setType(type);
              }
              else//This is file in scripts directory like a tar OR any other file
              {
                Log.debugLog(className, "getScriptsNode", "", "", "Invalid node = " + node.getName());
                continue;
              }
            }
          }
          if (node != null)
            tempNode.add(node);
        }
      }
      return tempNode;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getScriptNode", "", "", "Exception in getScriptNode() -", e);
      return null;
    }
  }

  private int getType(String name)
  {
    Log.debugLog(className, "getType", "", "", "name = " + name);
    if (name.equalsIgnoreCase("script.c"))
      return ScriptNode.C;
    if (name.equalsIgnoreCase("script.capture"))
      return ScriptNode.CAPTURE;
    if (name.equalsIgnoreCase("script.detail"))
      return ScriptNode.DETAIL;
    if (name.equalsIgnoreCase("script.h"))
      return ScriptNode.H;
    if (name.equalsIgnoreCase("runlogic.c") || name.equalsIgnoreCase("runlogic.java"))
      return ScriptNode.RUN_LOGIC;
    if (name.equalsIgnoreCase("script.lib"))
      return ScriptNode.LIB;
    if(name.equalsIgnoreCase("init_script.c") || name.equalsIgnoreCase("init_script.java"))
      return ScriptNode.FLOW_INIT;
    //getting the file type for both java type and c type script
    if(name.equalsIgnoreCase("exit_script.c") || name.equalsIgnoreCase("exit_script.java"))
      return ScriptNode.FLOW_EXIT;
    if(name.equalsIgnoreCase("user_test_init.c"))
      return ScriptNode.USER_INIT;
    if(name.equalsIgnoreCase("user_test_exit.c"))
      return ScriptNode.USER_EXIT;
    if(name.endsWith(".c") || name.endsWith(".java")) //for getting the flow type node in java type file
      return ScriptNode.FLOW_FILE;
    if(name.equalsIgnoreCase("registrations.spec"))
      return ScriptNode.REG_SPEC;
    return ScriptNode.UNKNOWN;
  }

  private boolean isValidChild(String dirPath)
  {
    Log.debugLog(className, "isValidChild", "", "", "Method Started, DirPath = " + dirPath);

    try
    {
      // assuming that dir in
      // win - C:/home/netstorm/work/webapps/scripts/test_atu/
      // linux - /home/netstorm/work/webapps/scripts/test_atu/
      String[] strArr = rptUtilsBean.strToArrayData(dirPath, "/");

      String osname = System.getProperty("os.name").trim().toLowerCase();
      if(osname.startsWith("win"))//This to test for local testing on window machine
      {
        if(strArr.length > 7)
          return true;
        else if(strArr.length == 7)
        {
          if(strArr[5].equals("scripts"))
          {
            File tmpfile = new File(dirPath);
            if(tmpfile.isDirectory())
              return true;
            else
              return false;
          }
          else
            return false;
        }
        else
          return false;
      }
      else
      {
        if(strArr.length > 6)
          return true;
        else if(strArr.length == 6)
        {
          if(strArr[4].equals("scripts"))
          {
            File tmpfile = new File(dirPath);
            if(tmpfile.isDirectory())
              return true;
            else
              return false;
          }
          else
            return false;
        }
        else
          return false;
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "isValidChild", "", "", "Exception in isValidChild() -", e);
      return false;
    }
  }

  private String[] getListOfDir(String absolutePathOfDir)
  {
    Log.debugLog(className, "getListOfDir", "", "", "Method Started. absolutePathOfDir = " + absolutePathOfDir);

    try
    {
      File tempFile = new File(absolutePathOfDir);

      String[] strListOfFiles = null;
      if(tempFile.exists())
        strListOfFiles = tempFile.list();

      return strListOfFiles;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getListOfDir", "", "", "Exception in getListOfDir() -", e);
      return null;
    }
  }
  public ArrayList getListProjName()
  {
    return listProjName;
  }

  public void setListProjName(ArrayList listProjName)
  {
    this.listProjName = listProjName;
  }
  
  public String getOpenFromDiff()
  {
    return openFromDiff;
  }

  private void setOpenFromDiff(String openFromDiff)
  {
    this.openFromDiff = openFromDiff;
  }
  
  private Boolean getIsForDiff()
  {
    return isForDiff;
  }

  private void setIsForDiff(Boolean isForDiff)
  {
    this.isForDiff = isForDiff;
  }
 
  public static void main(String[] args)
  {
    ScriptTree scriptTree = new ScriptTree();

    JFrame frame = new JFrame("Testing");
    frame.setSize(500, 500);

    JTree tree = null;

    ScriptNode scriptsNode = (ScriptNode)scriptTree.getScriptTreeRootNode("neeraj", false, "User", false, null);
    DefaultTreeModel treeModel = new DefaultTreeModel(scriptsNode);

    tree = new JTree(treeModel);
    tree.setRootVisible(false);

    frame.add(tree);

    frame.setVisible(true);
  }
}

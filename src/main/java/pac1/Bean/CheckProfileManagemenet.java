//-------------------------------------------------------//
//  Name   : CheckProfileDTO.java
//  Author : Ravi Kant Sharma
//  Purpose: For setting CheckRuleDTO and CheckProfileDTO - Check Profile Management
//  Notes  : 
//  Modification History:
//   17 April 2013: Ravi Kant Sharma: - Initial Version
//-----------------------------------------------------//
package pac1.Bean;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class CheckProfileManagemenet
{
  private static String className = "CheckProfileManagemenet";
  private String workPath = Config.getWorkPath();
  private String checkProfileExtension = ".cprof";
  private Properties properties = null;
  private int RULE_ID_INDEX = 1;
  private int RULE_NAME_INDEX = 2;
  private int RULE_TYPE_INDEX = 3;
  private int GRAPH_DEFINITION_INDEX = 4;
  private int PERCENTAGE_CHANGE_INDEX = 5;
  private int EXPECTED_RESULT_INDEX = 6;
  private int STATE_INDEX = 7;
  private int LAST_UPDATED_BY_INDEX = 8;
  private int LAST_UPDATED_ON_INDEX = 9;
  private int RULE_DESC_INDEX = 15;

  /*
   * this function returns the path of check profile
   */
  private String getCheckProfilePath()
  {
    String path = workPath + "/checkprofile/";
    Log.debugLog(className, "getCheckProfilePath", "", "", "CheckProfilePath = " + path);
    return path;
  }

  // This will return the Test Run path
  private String getTestRunPath(int numTestRun)
  {
    return (workPath + "/webapps/logs/TR" + numTestRun);
  }

  /*
   * this function returns the path of check profile with project subproject
   */
  private String getCheckProfilePathWithProjectSubProject(String projectSubProject)
  {
    String path = workPath + "/checkprofile/" + projectSubProject;
    Log.debugLog(className, "getCheckProfilePathWithProjectSubProject", "", "", "CheckProfilePath = " + path);
    return path;
  }

  /*
   * this method return the list of all project and sub project with profile
   * names
   */
  public ArrayList<String> getAllCheckProfileProjectSubProjectWithProfiles()
  {
    Log.debugLog(className, "getAllCheckProfileProjectSubProjectWithProfiles", "", "", "Method Called.");
    ArrayList<String> checkProfileList = new ArrayList<String>();
    checkProfileList.add("#Project|SubProject|ProfileName|UserName");
    try
    {
      File checkProfileDir = new File(Config.getWorkPath() + "/checkprofile/");
      String arrProjectDir[] = checkProfileDir.list();
      if (arrProjectDir != null)
      {
        for (int i = 0; i < arrProjectDir.length; i++)
        {
          File projectDir = new File(Config.getWorkPath() + "/checkprofile/" + arrProjectDir[i]);
          String arrSubProjectDir[] = projectDir.list();
          if (arrSubProjectDir != null)
          {
            for (int j = 0; j < arrSubProjectDir.length; j++)
            {
              File subProjectDir = new File(Config.getWorkPath() + "/checkprofile/" + arrProjectDir[i] + "/" + arrSubProjectDir[j]);
              String arrCheckProfile[] = subProjectDir.list();
              if (arrCheckProfile != null)
              {
                for (int k = 0; k < arrCheckProfile.length; k++)
                {
                  if (arrCheckProfile[k].trim().endsWith(".cprof"))
                  {
                    int idx = arrCheckProfile[k].lastIndexOf(".");
                    String profileName = arrCheckProfile[k].substring(0, idx);
                    checkProfileList.add(arrProjectDir[i] + "|" + arrSubProjectDir[j] + "|" + profileName + "|" + "netstorm");
                  }
                }
              }
            }
          }
        }
      }
    }
    catch (Exception e)
    {
      Log.errorLog(className, "getAllCheckProfileProjectSubProjectWithProfiles", "", "", e.getMessage());
    }

    return checkProfileList;
  }

  /*
   * This function returns a HashMap of Key as Project Name and value will be an
   * array list that contains subproject name
   */
  public HashMap<String, ArrayList<String>> gerCheckProfileProjectSubProject()
  {
    try
    {
      Log.debugLog(className, "gerCheckProfileProjectSubProject", "", "", "Method Called.");
      String profilePath = getCheckProfilePath();
      File folder = new File(profilePath);
      File[] listOfProjects = folder.listFiles();
      HashMap<String, ArrayList<String>> arrProjectSubProject = new HashMap<String, ArrayList<String>>();
      for (int i = 0; i < listOfProjects.length; i++)
      {
        if (listOfProjects[i].isDirectory())
        {
          String projectName = listOfProjects[i].getName();
          File[] subProjectName = listOfProjects[i].listFiles();
          ArrayList<String> arrSubprojects = new ArrayList<String>();
          for (int j = 0; j < subProjectName.length; j++)
          {
            if (subProjectName[j].isDirectory())
              arrSubprojects.add(subProjectName[j].getName());
          }

          arrProjectSubProject.put(projectName, arrSubprojects);
        }
      }

      return arrProjectSubProject;
    }
    catch (Exception ex)
    {
      Log.errorLog(className, "gerCheckProfileProjectSubProject", "", "", "Exception while getting project/subproject - " + ex);
      return null;
    }
  }

  /*
   * this function returns the all profile name
   */
  public ArrayList<String> getAllProfiles(String projectSubProject)
  {
    try
    {
      ArrayList<String> arrayList = new ArrayList<String>();
      String profilePath = getCheckProfilePathWithProjectSubProject(projectSubProject);
      File folder = new File(profilePath);
      File[] listOfFiles = folder.listFiles();
      String files;
      if(listOfFiles != null)
      {
      for (int i = 0; i < listOfFiles.length; i++)
      {

        if (listOfFiles[i].isFile())
        {
          files = listOfFiles[i].getName();
          if (files.endsWith(checkProfileExtension))
            arrayList.add(files);
        }
      }
      }
      return arrayList;
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      Log.errorLog(className, "getAllProfiles", "", "", "Exception in getting profile names - " + ex);
      return null;
    }
  }

  /*
   * This function delete the check profile
   */
  public ArrayList<String> deleteProfile(String[] profileToDelete, String projectSubProject)
  {
    try
    {
      ArrayList<String> arrFileDeleteStatus = new ArrayList<String>();
      for (int i = 0; i < profileToDelete.length; i++)
      {
        String profilePath = getCheckProfilePathWithProjectSubProject(projectSubProject) + "/" + profileToDelete[i];
        File file = new File(profilePath);
        if (!file.exists())
        {
          Log.errorLog(profilePath, "deleteProfile", "", "", "Profile - " + profileToDelete[i] + " does not exist. Please check file name.");
          arrFileDeleteStatus.add(profileToDelete[i]);
        }
        else
        {
          boolean isDeleted = file.delete();
          if (!isDeleted)
            arrFileDeleteStatus.add(profileToDelete[i]);
        }
      }

      return arrFileDeleteStatus;
    }
    catch (Exception ex)
    {
      Log.errorLog(className, "deleteProfile", "", "", "Exception in deleting profile - " + profileToDelete + ", Exception - " + ex);
      return null;
    }
  }

  /*
   * This function copy the check profile from old profile to new profile
   */
  public boolean copyCheckProfile(String oldProfileName, String newProfileName, String projectSubProject)
  {
    try
    {
      Log.debugLog(className, "copyCheckProfile", "", "", "srFile = " + oldProfileName + " , dtFile = " + newProfileName);
      String oldProfilePath = getCheckProfilePathWithProjectSubProject(projectSubProject) + "/" + oldProfileName;
      String newProfilePath = getCheckProfilePathWithProjectSubProject(projectSubProject) + "/" + newProfileName;

      try
      {
        File f1 = new File(oldProfilePath);
        File f2 = new File(newProfilePath);
        InputStream in = new FileInputStream(f1);

        OutputStream out = new FileOutputStream(f2, false);

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0)
        {
          out.write(buf, 0, len);
        }
        in.close();
        out.close();
      }
      catch (FileNotFoundException ex)
      {
        Log.stackTraceLog(className, "copyCheckProfile", "", "", "Exception in copyfile - ", ex);
        return false;
      }
      catch (IOException e)
      {
        Log.stackTraceLog(className, "copyCheckProfile", "", "", "Exception in copyfile - ", e);
        return false;
      }
      return true;
    }
    catch (Exception ex)
    {
      Log.errorLog(newProfileName, "copyCheckProfile", "", "", "Exception while copy the profile - " + oldProfileName + " to profile " + newProfileName);
      return false;
    }
  }

  public void loadFile(String filePath)
  {
    properties = new Properties();
    try
    {
      properties.load(new FileInputStream(filePath));
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  /*
   * This function returns the check profile data
   */
  public HashMap<String, CheckProfileDTO> getCheckProfileData(String projectSubProject)
  {
    try
    {
      ArrayList<String> arrProfiles = getAllProfiles(projectSubProject);
      if (arrProfiles == null)
      {
        Log.errorLog(className, "getCheckProfileData", "", "", "Cannot get profile data may be there is no profile exist.");
        return null;
      }

      HashMap<String, CheckProfileDTO> hmProfileData = new HashMap<String, CheckProfileDTO>();
      for (int i = 0; i < arrProfiles.size(); i++)
      {
        String profileName = arrProfiles.get(i).toString();
        String profilePath = getCheckProfilePathWithProjectSubProject(projectSubProject) + "/" + profileName;
        loadFile(profilePath);

        String profileDesc = properties.getProperty("Profile_Description");
        String updatedBy = properties.getProperty("Profile_Last_Updated_By");
        String lastModifiedDate = properties.getProperty("Profile_Last_Updated_On");

        BufferedReader br = new BufferedReader(new FileReader((profilePath)));
        if (br == null)
        {
          Log.errorLog(className, "getCheckProfileData", "", "", "Error in creating intance for file - " + profilePath);
          return null;
        }

        HashMap<String, CheckRuleDTO> hmChkRuleDTO = new HashMap<String, CheckRuleDTO>();
        int checkRuleCount = 0;
        int compareRuleCount = 0;
        String line = "";
        while ((line = br.readLine()) != null)
        {
          if (line.trim().startsWith("#") || line.trim().equals(""))
          {
            continue;
          }
          else if (line.trim().startsWith("Rule"))
          {
            String[] arrLineData = rptUtilsBean.strToArrayData(line, "|");
            try
            {
              CheckRuleDTO chkRuleDTO = new CheckRuleDTO();

              int ruleId = Integer.parseInt(arrLineData[RULE_ID_INDEX]);
              chkRuleDTO.setRuleId(ruleId);

              String ruleName = arrLineData[RULE_NAME_INDEX];
              chkRuleDTO.setRuleName(ruleName);

              String ruleType = arrLineData[RULE_TYPE_INDEX];
              chkRuleDTO.setRuleType(ruleType);

              String graphDefinition = arrLineData[GRAPH_DEFINITION_INDEX];
              chkRuleDTO.setGraphDefinition(graphDefinition);

              int percentChange = Integer.parseInt(arrLineData[PERCENTAGE_CHANGE_INDEX]);
              chkRuleDTO.setPerecentChange(percentChange);

              String expectedResult = arrLineData[EXPECTED_RESULT_INDEX];
              chkRuleDTO.setExpectedResult(expectedResult);

              String state = arrLineData[STATE_INDEX];
              chkRuleDTO.setState(state);

              String ruleUpdatedBy = arrLineData[LAST_UPDATED_BY_INDEX];
              chkRuleDTO.setLastUpdatedBy(ruleUpdatedBy);

              String ruleUpdatedOn = arrLineData[LAST_UPDATED_ON_INDEX];
              chkRuleDTO.setLastUpdatedOn(ruleUpdatedOn);

              String ruleDescription = arrLineData[RULE_DESC_INDEX];
              chkRuleDTO.setRuleDescription(ruleDescription);

              if (ruleType.equals("Check Rules"))
                checkRuleCount++;
              else
                compareRuleCount++;

              hmChkRuleDTO.put(ruleName, chkRuleDTO);
            }
            catch (Exception ex)
            {
              Log.errorLog(className, "getProfileData", "", "", "Error in check profile parsing - " + ex);
            }
          }
        }

        CheckProfileDTO chkProfileDTO = new CheckProfileDTO();
        chkProfileDTO.setCheckRuleCount(checkRuleCount);
        chkProfileDTO.setCompareRuleCount(compareRuleCount);
        chkProfileDTO.setLastUpdatedDate(lastModifiedDate);
        chkProfileDTO.setChkRuleDTO(hmChkRuleDTO);
        chkProfileDTO.setProfileName(profileName);
        chkProfileDTO.setProfileDesc(profileDesc);
        chkProfileDTO.setUpdatedBy(updatedBy);
        hmProfileData.put(profileName, chkProfileDTO);
      }

      return hmProfileData;
    }
    catch (Exception ex)
    {
      Log.errorLog(className, "getCheckProfileData", "", "", "Exception in getting check proiles - " + ex);
      return null;
    }
  }

  /*
   * This function is used for saving check profile, It will called from Client
   * GUI using Servlet communication
   */
  public boolean saveCheckProfile(String profileName, CheckProfileDTO chkProfileDTO, String projectSubProject, String userName, String grpName)
  {
    try
    {
      Log.debugLog(className, "saveCheckProfile", "", "", "Method Called.");
      createAndChangePerMissionofDir(projectSubProject, userName, grpName);
      
      String filePath = getCheckProfilePathWithProjectSubProject(projectSubProject) + "/" + profileName;
      File file = new File(filePath);
      if (file.exists())
        file.delete();

      BufferedWriter writer = new BufferedWriter(new FileWriter(file));

      String profileUpdateByLine = "Profile_Last_Updated_By = " + chkProfileDTO.getUpdatedBy() + "\n";
      String profileUpdateOnLine = "Profile_Last_Updated_On = " + chkProfileDTO.getLastUpdatedDate() + "\n";
      String ProfileDescriptionLine = "Profile_Description = " + chkProfileDTO.getProfileDesc();
      String headerLine = "# Rule|Rule Id|Rule Name|Rule Type|Graph Definition|% Change|Expected Result|State|Last Updated By|Last Updated On|Future1|Future2|Future3|Future4|Future5|Rule Description";
      writer.write(profileUpdateByLine);
      writer.write(profileUpdateOnLine);
      writer.write(ProfileDescriptionLine);
      writer.write("\n");
      writer.write(headerLine);
      writer.write("\n");
      // this function returns all checkRuleDTO of a check profile
      
      HashMap<String, CheckRuleDTO> chkRuleDTOs = chkProfileDTO.getChkRuleDTO();
      
      ArrayList as = entriesSortedByValues(chkRuleDTOs);

      //Set set = chkRuleDTOs.keySet();
      Iterator itr = as.iterator();

      while (itr.hasNext())
      {
        String ruleName = (String)((Map.Entry)itr.next()).getKey();
        CheckRuleDTO chkRuleDTO = chkRuleDTOs.get(ruleName);
        int ruleId = chkRuleDTO.getRuleId();
        String graphDefinition = chkRuleDTO.getGraphDefinition();
        int percentChange = chkRuleDTO.getPerecentChange();
        String expectedResult = chkRuleDTO.getExpectedResult();
        String state = chkRuleDTO.getState();
        String ruleUpdatedBy = chkRuleDTO.getLastUpdatedBy();
        String ruleUpdatedOn = chkRuleDTO.getLastUpdatedOn();
        String ruleDescription = chkRuleDTO.getRuleDescription();
        String ruleType = chkRuleDTO.getRuleType();

        String dataLine = "Rule|" + ruleId + "|" + ruleName + "|" + ruleType + "|" + graphDefinition + "|" + percentChange;
        dataLine = dataLine + "|" + expectedResult + "|" + state + "|" + ruleUpdatedBy + "|" + ruleUpdatedOn + "|-|-|-|-|-|" + ruleDescription + "\n";
        writer.write(dataLine);
      }

      writer.flush();
      writer.close();
      rptUtilsBean.changeFilePerm(filePath, userName, grpName, "664");
      return true;
    }
    catch (Exception ex)
    {
      Log.errorLog(className, "saveCheckProfile", "", "", "Exception while saving check rule profile - " + ex);
      Log.stackTraceLog(className, "saveCheckProfile", "", "", "Exception while saving check rule profile - ", ex);
      return false;
    }
  }

  /**
   * To sort the map on the bais of rule id in it's value 
   * @param map
   * @return
   */
  public ArrayList entriesSortedByValues(Map<String, CheckRuleDTO> map)
  {
    ArrayList as = new ArrayList(map.entrySet());

    Collections.sort(as, new Comparator()
    {
      public int compare(Object o1, Object o2)
      {
        Map.Entry e1 = (Map.Entry)o1;
        Map.Entry e2 = (Map.Entry)o2;
        Integer first = (Integer)((CheckRuleDTO)e1.getValue()).getRuleId();
        Integer second = (Integer)((CheckRuleDTO)e2.getValue()).getRuleId();
        return first.compareTo(second);
      }
    });
    
    return as;
  }

  /** This method is used to create checkprofile/project/subproject directory at workpath level.. and change their permission
   *  
   * @param projectSubProject
   * @param userName
   * @param grpName
   * @return
   */
  private boolean createAndChangePerMissionofDir(String projectSubProject, String userName, String grpName)
  {
    try
    {
      Log.debugLog(className, "createAndChangePerMissionofDir", "", "", "Method Called.");
      File filePath = null;
      String[] projSubProj = projectSubProject.split("/");
      
      String chkProfDirPath = getCheckProfilePath();
      filePath = new File(chkProfDirPath);
      if(!filePath.exists())
      {
        filePath.mkdirs();
        rptUtilsBean.changeFilePerm(chkProfDirPath, userName, grpName, "775");
      }
      
      String chkProfProjectPath = chkProfDirPath + "/" + projSubProj[0];
      filePath = new File(chkProfProjectPath);
      if(!filePath.exists())
      {
        filePath.mkdirs();
        rptUtilsBean.changeFilePerm(chkProfProjectPath, userName, grpName, "775");
      }

      String chkSubProfProjectPath = chkProfProjectPath + "/" + projSubProj[1];
      filePath = new File(chkSubProfProjectPath);
      if(!filePath.exists())
      {
        filePath.mkdirs();
        rptUtilsBean.changeFilePerm(chkSubProfProjectPath, userName, grpName, "775");
      }

      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "createAndChangePerMissionofDir", "", "", "Exception in createAndChangePerMissionofDir - ", ex);
      return false;      
    }
  }
  /*
   * This function returns an array list of all check rule DTO
   */
  public ArrayList<CheckRuleDTO> getCheckRuleDTO(String profileName, String projectSubProject)
  {
    try
    {
      Log.debugLog(profileName, "getCheckRuleDTO", "", "", "Method Called. profileName - " + profileName);

      String profilePath = getCheckProfilePathWithProjectSubProject(projectSubProject) + "/" + profileName;

      BufferedReader br = new BufferedReader(new FileReader((profilePath)));
      if (br == null)
      {
        Log.errorLog(className, "getCheckProfileData", "", "", "Error in creating intance for file - " + profilePath);
        return null;
      }

      String line = "";
      ArrayList<CheckRuleDTO> arrChkRuleDTO = new ArrayList<CheckRuleDTO>();
      while ((line = br.readLine()) != null)
      {
        if (line.trim().startsWith("Rule"))
        {
          String[] arrLineData = rptUtilsBean.strToArrayData(line, "|");
          try
          {
            CheckRuleDTO chkRuleDTO = new CheckRuleDTO();

            int ruleId = Integer.parseInt(arrLineData[RULE_ID_INDEX]);
            chkRuleDTO.setRuleId(ruleId);

            String ruleName = arrLineData[RULE_NAME_INDEX];
            chkRuleDTO.setRuleName(ruleName);

            String ruleType = arrLineData[RULE_TYPE_INDEX];
            chkRuleDTO.setRuleType(ruleType);

            String graphDefinition = arrLineData[GRAPH_DEFINITION_INDEX];
            chkRuleDTO.setGraphDefinition(graphDefinition);

            int percentChange = Integer.parseInt(arrLineData[PERCENTAGE_CHANGE_INDEX]);
            chkRuleDTO.setPerecentChange(percentChange);

            String expectedResult = arrLineData[EXPECTED_RESULT_INDEX];
            chkRuleDTO.setExpectedResult(expectedResult);

            String state = arrLineData[STATE_INDEX];
            chkRuleDTO.setState(state);

            String ruleUpdatedBy = arrLineData[LAST_UPDATED_BY_INDEX];
            chkRuleDTO.setLastUpdatedBy(ruleUpdatedBy);

            String ruleUpdatedOn = arrLineData[LAST_UPDATED_ON_INDEX];
            chkRuleDTO.setLastUpdatedOn(ruleUpdatedOn);

            String ruleDescription = arrLineData[RULE_DESC_INDEX];
            chkRuleDTO.setRuleDescription(ruleDescription);

            arrChkRuleDTO.add(chkRuleDTO);
          }
          catch (Exception ex)
          {
            Log.errorLog(className, "getProfileData", "", "", "Error in check profile parsing - " + ex);
          }
        }
      }

      return arrChkRuleDTO;
    }
    catch (Exception ex)
    {
      Log.errorLog(profileName, "getCheckRuleDTO", "", "", "Exception in getting CheckRuleDTO - " + ex);
      return null;
    }
  }

  /*
   * This function reads all file and returns total check profile rule
   */
  private int getTotalCheckRules(String profileNameWithPath)
  {
    try
    {
      int totalRules = 0;
      BufferedReader br = new BufferedReader(new FileReader((profileNameWithPath)));
      if (br == null)
      {
        Log.errorLog(className, "getCheckRulesResultDTO", "", "", "Check profile -" + profileNameWithPath + " does not exist.");
        return 0;
      }
      String resultLine = null;
      while ((resultLine = br.readLine()) != null)
      {
        if (resultLine.startsWith("#"))
          continue;
        if (resultLine.trim().startsWith("Rule"))
          totalRules++;
      }

      return totalRules;
    }
    catch (Exception ex)
    {
      Log.errorLog(className, "getTotalCheckRules", "", "", "Exception - " + ex);
      return 0;
    }
  }

  /*
   * This function returns the Check Rule Status DTO to show the result in Swing
   * GUI.
   * 
   * ----------------------- Result File Format -----------------------
   * 
   * Message|Condition|Start Time|End Time|%Change|Graph Details|Baseline
   * TR|Baseline Graph Id|Correlated Graph Ids|Vector
   * Name|Values|Future1|Future2|Future3|Future4|Future5
   * 
   * ---------------------------------------------------------------------
   */
  public HashMap<String, CheckRuleResultDTO> getCheckRulesResultDTO(int numTestRun, String projectSubproject, String checkProfileName)
  {
    try
    {
      String testRunDirPath = getTestRunPath(numTestRun);
      String resultFilePath = testRunDirPath + "/checkprofile/" + projectSubproject + "/" + checkProfileName + ".result";
      String checkProfilePath = testRunDirPath + "/checkprofile/" + projectSubproject + "/" + checkProfileName;

      BufferedReader br = new BufferedReader(new FileReader((resultFilePath)));
      if (br == null)
      {
        Log.errorLog(className, "getCheckRulesResultDTO", "", "", "Result file -" + resultFilePath + " does not exist.");
        return null;
      }

      String resultLine = null;
      int checkRuleID = 1;
      int totalRules = getTotalCheckRules(checkProfilePath);

      // this hash map for storing the result of Check Rules Status
      HashMap<String, CheckRuleResultDTO> hmResults = new HashMap<String, CheckRuleResultDTO>();

      while ((resultLine = br.readLine()) != null)
      {
        if (resultLine.startsWith("#"))
          continue;
        try
        {
          String[] resultLineArr = resultLine.split("\\|");
          if (resultLineArr.length != 18)
          {
            Log.errorLog(className, "getCheckRulesResultDTO", "", "", "Unexpected line found in profile. Please see line - " + resultLineArr + " in profile - " + checkProfileName);
            continue;
          }
          else
          {
            checkRuleID++;
            String strRuleName = resultLineArr[CheckRuleResultDTO.RuleName_INDEX].trim();
            String strRuleMessage = resultLineArr[CheckRuleResultDTO.Message_INDEX].trim();
            String strPercentageChange = resultLineArr[CheckRuleResultDTO.Percentage_Change_INDEX].trim();
            String strStartTime = resultLineArr[CheckRuleResultDTO.startTime_INDEX].trim();
            String strEndTime = resultLineArr[CheckRuleResultDTO.EndTime_INDEX].trim();
            String strRuletype = resultLineArr[CheckRuleResultDTO.RuleType_INDEX].trim();
            String strGraphDetails = resultLineArr[CheckRuleResultDTO.Graph_Details_INDEX].trim();
            String strBasGraphIds = resultLineArr[CheckRuleResultDTO.Base_Graph_Id_INDEX].trim();
            String strCorrelatedGraphIds = resultLineArr[CheckRuleResultDTO.Correlated_Graph_Ids_INDEX].trim();
            String strVectorName = resultLineArr[CheckRuleResultDTO.Vector_NAME_Index].trim();
            String strBaseLineTestRun = resultLineArr[CheckRuleResultDTO.Baseline_TR_INDEX].trim();
            String strBaseLineGraphIds = resultLineArr[CheckRuleResultDTO.Baseline_Graph_Id_Index].trim();
            String strCondition = resultLineArr[CheckRuleResultDTO.Condition_Index].trim();
            String strStatus = resultLineArr[CheckRuleResultDTO.Status_INDEX].trim();
            String strBaseLineTestRunValues = resultLineArr[CheckRuleResultDTO.BaseLine_TestRun_values_Index].trim();
            String strPreviousTestRunValues = resultLineArr[CheckRuleResultDTO.Previous_TestRun_Values_Index].trim();

            CheckRuleResultDTO checkRuleResultDTO = new CheckRuleResultDTO();
            checkRuleResultDTO.setStrRuleName(strRuleName);
            checkRuleResultDTO.setStrRuleMessage(strRuleMessage);
            checkRuleResultDTO.setStrBaseLineGraphId(strBaseLineGraphIds);
            checkRuleResultDTO.setCheckRuleID(checkRuleID);
            checkRuleResultDTO.setStrCondition(strCondition);
            checkRuleResultDTO.setStrBaseLineTestRunValues(strBaseLineTestRunValues);
            checkRuleResultDTO.setStrStatus(strStatus);
            checkRuleResultDTO.setStrPreviousTestRunValues(strPreviousTestRunValues);
            checkRuleResultDTO.setStrBaseLineTestRun(strBaseLineTestRun);
            checkRuleResultDTO.setStrPercentageChange(strPercentageChange);
            checkRuleResultDTO.setStrStartTime(strStartTime);
            checkRuleResultDTO.setStrEndTime(strEndTime);
            checkRuleResultDTO.setStrRuleType(strRuletype);
            checkRuleResultDTO.setStrVectorName(strVectorName);
            checkRuleResultDTO.setStrGraphDetails(strGraphDetails);
            checkRuleResultDTO.setStrBaseGraphIds(strBasGraphIds);
            checkRuleResultDTO.setStrCorrelatedGraphIds(strCorrelatedGraphIds);
            hmResults.put(strRuleName, checkRuleResultDTO);
          }
        }
        catch (Exception ex)
        {
          Log.errorLog(className, "getCheckRulesResultDTO", "", "", "Exception while parsing profile - " + checkProfileName);
          continue;
        }
      }

      return hmResults;
    }
    catch (Exception ex)
    {
      Log.errorLog(className, "getCheckRulesResultDTO", "", "", "Exception while getting Check Rules Results DTO - " + ex);
      return null;
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args)
  {
    // TODO Auto-generated method stub

  }

}

/*--------------------------------------------------------------------
@Name    : RTGProfile.java
@Author  : Prabhat
@Purpose : This is the main file of RTG profile.
@Modification History:

Ravi - 24/05/2014 Change design from Graph Numbers to GraphUniqueKeyDTO
----------------------------------------------------------------------*/
package pac1.Bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import pac1.Bean.GraphName.GraphNameUtils;
import pac1.Bean.GraphName.GraphNames;

class PanelInfoBean
{
  private String className = "PanelInfoBean";
  private RTGProfileBean rtgProfile = null;
  private String panelCaption = "";
  private String[] grpID = null;
  private String[] graphID = null;
  private String[] vectorName = null;
  private GraphUniqueKeyDTO[] arrGraphUniqueKeyDTO = null;

  public GraphUniqueKeyDTO[] getArrGraphUniqueKeyDTO()
  {
    return arrGraphUniqueKeyDTO;
  }

  public void setArrGraphUniqueKeyDTO(GraphUniqueKeyDTO[] arrGraphUniqueKeyDTO)
  {
    this.arrGraphUniqueKeyDTO = arrGraphUniqueKeyDTO;
  }

  public PanelInfoBean(RTGProfileBean rtgProfile, String panelCaption, String[][] arrInfo)
  {
    this.rtgProfile = rtgProfile;
    init(panelCaption, arrInfo);
  }

  private void init(String panelCaption, String[][] arrInfo)
  {
    try
    {
      Log.debugLogAlways(className, "init", "", "", "Start method. panelCaption = " + panelCaption);
      this.panelCaption = panelCaption;
      this.grpID = arrInfo[0];
      this.graphID = arrInfo[1];
      this.vectorName = arrInfo[3];
      arrGraphUniqueKeyDTO = GraphNameUtils.createGraphUniqueKeyDTO(grpID, graphID, vectorName, rtgProfile.graphNames, rtgProfile.arrVectorMappingList);
      if (arrGraphUniqueKeyDTO != null)
      {
        if (arrGraphUniqueKeyDTO.length == 1 && arrGraphUniqueKeyDTO[0] == null)
        {
          arrGraphUniqueKeyDTO = null;
        }
        else
        {
          int counter = 0;
          for (int j = 0; j < arrGraphUniqueKeyDTO.length; j++)
          {
            if (arrGraphUniqueKeyDTO[j] == null)
              counter = counter + 1;
          }

          if (counter == arrGraphUniqueKeyDTO.length)
            arrGraphUniqueKeyDTO = null;
        }
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "init", "", "", "Exception - ", e);
    }
  }

  // This function return the panel Caption
  public String getPanelCaption()
  {
    return panelCaption;
  }

  // This function set the Panel Caption
  public void setPanelCaption(String panelCaption)
  {
    this.panelCaption = panelCaption;
  }

  // Return the number of graphs in the panel
  public int getNumPanelGraph()
  {
    return (grpID.length);
  }
}

class MergeGrpInfoBean
{
  private String className = "MergeGrpInfoBean";
  private RTGProfileBean rtgProfile = null;
  private String grpCaption = "";
  private String[] grpID = null;
  private String[] graphID = null;
  private String[] vectorName = null;
  private GraphUniqueKeyDTO[] arrGraphUniqueKeyDTO = null;

  public GraphUniqueKeyDTO[] getArrGraphUniqueKeyDTO()
  {
    return arrGraphUniqueKeyDTO;
  }

  public void setArrGraphUniqueKeyDTO(GraphUniqueKeyDTO[] arrGraphUniqueKeyDTO)
  {
    this.arrGraphUniqueKeyDTO = arrGraphUniqueKeyDTO;
  }

  public MergeGrpInfoBean(RTGProfileBean rtgProfile, String grpCaption, String[][] arrInfo)
  {
    this.rtgProfile = rtgProfile;
    init(grpCaption, arrInfo);
  }

  private void init(String grpCaption, String[][] arrInfo)
  {
    try
    {
      Log.debugLog(className, "init", "", "", "Start method. grpCaption = " + grpCaption);
      this.grpCaption = grpCaption;
      this.grpID = arrInfo[0];
      this.graphID = arrInfo[1];
      this.vectorName = arrInfo[3];
      arrGraphUniqueKeyDTO = GraphNameUtils.createGraphUniqueKeyDTO(grpID, graphID, vectorName, rtgProfile.graphNames, rtgProfile.arrVectorMappingList);
      if (arrGraphUniqueKeyDTO != null)
      {
        if (arrGraphUniqueKeyDTO.length == 1 && arrGraphUniqueKeyDTO[0] == null)
        {
          arrGraphUniqueKeyDTO = null;
        }
        else
        {
          int counter = 0;
          for (int j = 0; j < arrGraphUniqueKeyDTO.length; j++)
          {
            if (arrGraphUniqueKeyDTO[j] == null)
              counter = counter + 1;
          }

          if (counter == arrGraphUniqueKeyDTO.length)
            arrGraphUniqueKeyDTO = null;
        }
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "init", "", "", "Exception - ", e);
    }
  }

  // This function return the Merged Grp Caption
  public String getMergedGrpCaption()
  {
    return grpCaption;
  }

  // Return the number of graphs in Merged Group
  public int getNumMergedGraphs()
  {
    return (grpID.length);
  }
}

public class RTGProfileBean
{
  private static String className = "RTGProfileBean";
  private Properties profileProp = null;
  private static final String DATE_FORMAT_NOW = "MM/dd/yy HH:mm:ss";
  public ArrayList<VectorMappingDTO> arrVectorMappingList = rptUtilsBean.getVectorNameFromMappedFile("VectorMapping.dat");
  PanelInfoBean[] panelInfo = null;
  MergeGrpInfoBean[] mergeGrpInfo = null;

  final int PANEL_CAPTION_INDX = 3; // Index of Panel caption in Panel Info Line
  final int MAX_NUM_PANEL = 32; // Maximum number of Panels that is displayed in RTG GUI
  final int MERGED_GRP_CAPTION_INDX = 1; // Index of Merged Grp caption in Merged Grp Info Line
  final int DEFAULT_MERGE_GROUP_COUNTER = 100000; // Default Merge Group counter
  static final String DEFAULT_PROFILE_NAME = "_default";
  static final int DEFAULT_NUM_PANEL = 9; // default number of panel
  static final int DEFAULT_ROW_COL_COUNT = 3; // default number of rows and column

  int numPanel = 0; // Number of Panels to be displayed in RTG GUI
  int numRows = 0; // Number of Panels Row to be displayed in RTG GUI
  int numColumn = 0; // Number of Panels Column to be displayed in RTG GUI
  int numMergedGrp = 0; // Number of Merged Group to be displayed in RTG GUI
  int graphTimeOp = 0; // Graph Time Option displayed in RTG GUI (i.e. - 60 secs, 180 secs, 600 secs, 3600 secs, whole scenario)
  int xAxisValOp = 0; // X-Axis Value Option displayed in RTG GUI (i.e. - Elapsed, Absolute)
  int scaleOp = 0; // Set the scaling in RTG GUI (i.e. - On, Off)
  int detailPanelOp = 0; // Detail panel Option displayed in RTG GUI (i.e. - Selected Panel, Visible Panel, All Panels)
  int debugFlagOp = 0; // Debug Flag Option displayed in RTG GUI (i.e. - On, Off)
  String lastModifiedBy = "NA"; // this is to keep the last modified by value (username)

  Vector vecSelectedProfileData = null;
  Vector vecInfo = null;
  Vector vecPanelRec = null;
  Vector vecMergeGrpRec = null;
  Vector vecChangedColors = null;

  String profilePath = "";
  String selectedProfileName = "";
  String profileDesc = "NA"; // Profile Description for selected profile

  // public RightPane rightPane = null;
  public GraphNames graphNames = null;
  public int testNum = -1;
  public TreeMap treeMapProfileData = null;

  // It is use when "_default.egp" file is not found
  private static final String[] defaultPanelInfo = { "PANEL|0|0|Vusers Info|2|GrpId=1,1:GraphId=1,2:SSO=NA,NA:SN=NA,NA", "PANEL|1|0|Data Throughput|2|GrpId=2,2:GraphId=1,2:SSO=NA,NA:SN=NA,NA", "PANEL|2|0|Connections/Sec|2|GrpId=2,2:GraphId=7,8:SSO=NA,NA:SN=NA,NA", "PANEL|3|0|Ethernet Send Throughput (Kbps)|1|GrpId=2:GraphId=3:SSO=NA:SN=NA", "PANEL|4|0|SSL/Sec|5|GrpId=2,2,2,2,2:GraphId=9,10,11,12,13:SSO=NA,NA,NA,NA,NA:SN=NA,NA,NA,NA,NA", "PANEL|5|0|URL Hits|3|GrpId=3,3,3:GraphId=1,2,3:SSO=NA,NA,NA:SN=NA,NA,NA", "PANEL|6|0|Average URL Response Time (Seconds)|1|GrpId=3:GraphId=3:SSO=NA:SN=NA", "PANEL|7|0|Page Views|3|GrpId=4,4,4:GraphId=1,2,3:SSO=NA,NA,NA:SN=NA,NA,NA", "PANEL|8|0|Sessions/Minute|1|GrpId=5:GraphId=1:SSO=NA:SN=NA", "PANEL|9|0|Vuser Info Group|6|GrpId=1,1,1,1,1,1:GraphId=1,2,3,4,5,6:SSO=NA,NA,NA,NA,NA,NA:SN=NA,NA,NA,NA,NA,NA", "PANEL|10|0|Network Throughput Group|11|GrpId=2,2,2,2,2,2,2,2,2,2,2:GraphId=1,2,3,4,7,8,9,10,11,12,13:SSO=NA,NA,NA,NA,NA,NA,NA,NA,NA,NA,NA:SN=NA,NA,NA,NA,NA,NA,NA,NA,NA,NA,NA", "PANEL|11|0|URL Hits Group|3|GrpId=3,3,3:GraphId=1,2,3:SSO=NA,NA,NA:SN=NA,NA,NA", "PANEL|12|0|Page Download Group|3|GrpId=4,4,4:GraphId=1,2,3:SSO=NA,NA,NA:SN=NA,NA,NA", "PANEL|13|0|Session Group|3|GrpId=5,5,5:GraphId=1,2,3:SSO=NA,NA,NA:SN=NA,NA,NA", "PANEL|14|0|URL Failures Groups|1|GrpId=7:GraphId=1:SSO=NA:SN=NA", "PANEL|15|0|Page Failures Groups|1|GrpId=8:GraphId=1:SSO=NA:SN=NA", "PANEL|16|0|Session Failures Groups|1|GrpId=9:GraphId=1:SSO=NA:SN=NA", "PANEL|17|0|Failed URL Responses (All Errors)/Second|1|GrpId=7:GraphId=1:SSO=NA:SN=NA", "PANEL|18|0|Failed Page Responses (All Errors)/Minute|1|GrpId=8:GraphId=1:SSO=NA:SN=NA", "PANEL|19|0|Failed Sessions (All Errors)/Minute|1|GrpId=9:GraphId=1:SSO=NA:SN=NA", "PANEL|20|0|TCP Send Throughput (Kbps)|1|GrpId=2:GraphId=1:SSO=NA:SN=NA", "PANEL|21|0|TCP Receive Throughput (Kbps)|1|GrpId=2:GraphId=2:SSO=NA:SN=NA", "PANEL|22|0|Ethernet Send Throughput (Kbps)|1|GrpId=2:GraphId=3:SSO=NA:SN=NA", "PANEL|23|0|Running Vusers|1|GrpId=1:GraphId=1:SSO=NA:SN=NA", "PANEL|24|0|Active Vusers|1|GrpId=1:GraphId=2:SSO=NA:SN=NA", "PANEL|25|0|Thinking Vusers|1|GrpId=1:GraphId=3:SSO=NA:SN=NA", "PANEL|26|0|Waiting Vusers|1|GrpId=1:GraphId=4:SSO=NA:SN=NA", "PANEL|27|0|Idling Vusers|1|GrpId=1:GraphId=5:SSO=NA:SN=NA", "PANEL|28|0|Number Of Connections|1|GrpId=1:GraphId=6:SSO=NA:SN=NA", "PANEL|29|0|Ethernet Throughput|2|GrpId=2,2:GraphId=3,4:SSO=NA,NA:SN=NA,NA", "PANEL|30|0|URL Hits/Second|1|GrpId=3:GraphId=1:SSO=NA:SN=NA", "PANEL|31|0|Page Download/Minute|1|GrpId=4:GraphId=1:SSO=NA:SN=NA" };

  // This is call by putTestRunData in RightPane
  public RTGProfileBean(int testNum)
  {
    graphNames = new GraphNames(testNum);
    this.testNum = testNum;
    // to load all profile data at first time
    RTGProfileData rtgProfileDataObj = new RTGProfileData();
    treeMapProfileData = rtgProfileDataObj.getTreeMapProfileData();
  }

  public RTGProfileBean(int testNum, GraphNames graphNames)
  {
    this.testNum = testNum;
    this.graphNames = graphNames;
    RTGProfileData rtgProfileDataObj = new RTGProfileData();
    treeMapProfileData = rtgProfileDataObj.getTreeMapProfileData();
  }

  // initialize RTG Profile variable
  public void initRTGProfile(String profileName, Vector vecProfileData)
  {
    init(profileName, vecProfileData);
    loadPanelInfoInVec();
    loadPanelInfoInArr(profileName, vecProfileData);
    loadMergeGrpInfoInVec();
    loadMergeGrpInfoInArr();
  }

  // This function return the profile path
  private String pathToProfile()
  {
    return (Config.getWorkPath() + "/webapps/profiles/");
  }

  // This function load the profile file
  private void loadProfileFile(String profileName)
  {
    try
    {
      profileProp = getProfilePropByProfileName(profileName);

      if (profileProp == null)
      {
        Log.debugLogAlways(className, "loadProfileFile", "", "", "Profile not " + profileName + " found, therefore using default profile.");

        profileProp = getProfilePropByProfileName(DEFAULT_PROFILE_NAME);

        if (profileProp == null)
        {
          Log.errorLog(className, "loadProfileFile", "", "", DEFAULT_PROFILE_NAME + " file not found");
          return;
        }
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "loadProfileFile", "", "", "Exception - ", e);
    }
  }

  // this function return profile properties object
  public Properties getProfilePropByProfileName(String profileName)
  {
    ProfileData objProfileData = (ProfileData) treeMapProfileData.get(profileName);
    if (objProfileData == null)
    {
      Log.debugLogAlways(className, "getProfilePropByProfileName", "", "", "Favorite not " + profileName + " found, therefore using default favorite.");
      return null;
    }

    return (objProfileData.getPropProfile());
  }

  // this function is to chk that profile data is available on client or not
  public boolean chkProfileAvailability(String profileName)
  {
    return treeMapProfileData.containsKey(profileName);
  }

  // this function return profile content in vector
  public Vector getVecProfileDataByProfileName(String profileName)
  {
    ProfileData objProfileData = (ProfileData) treeMapProfileData.get(profileName);
    if (objProfileData == null)
    {
      Log.debugLogAlways(className, "getProfilePropByProfileName", "", "", "Favorite not " + profileName + " found, therefore using default favorite.");
      return null;
    }
    return (objProfileData.getVecProfileData());
  }

  // This function return the profile file data by data
  public Properties getPropertiesByProfileData(Vector vecProfileData)
  {
    loadPropertiesByProfileData(vecProfileData);
    return profileProp;
  }

  // This function load the profile file by data
  private void loadPropertiesByProfileData(Vector vecProfileData)
  {
    try
    {
      File tempFileObj = new File(System.getProperty("java.io.tmpdir") + "/TempProfileSettings.tmp");

      if (tempFileObj.exists())
        tempFileObj.delete();

      tempFileObj.createNewFile();

      FileOutputStream fout = new FileOutputStream(tempFileObj, true); // Append mode
      PrintStream printStream = new PrintStream(fout);

      String strData = "";
      for (int i = 0; i < vecProfileData.size(); i++)
      {
        strData = vecProfileData.get(i).toString();
        printStream.println(strData);
      }
      printStream.close();
      fout.close();

      profileProp = new Properties();
      profileProp.load(new FileInputStream(tempFileObj));
    }
    catch (Exception e)
    {
      Log.errorLog(className, "loadProfileFile", "", "", "Exception - " + e);
    }
  }

  // This function return the String value according to the keyword and if Keyword not found then it return default value
  private String getValue(String profileName, String key, String defaultVal)
  {
    try
    {
      String value;

      if (profileProp == null)
        loadProfileFile(profileName);

      value = profileProp.getProperty(key);
      if (value == null)
      {
        Log.debugLog(className, "getValue", "", "", "Keyword not found in file. Keyword = " + key + "Use default value = " + defaultVal);
        value = defaultVal;
      }
      return (value.trim());
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getValue", "", "", "Exception - ", e);
      return "";
    }
  }

  // Methods for reading the File
  private Vector readFile(String fileName)
  {
    Log.debugLog(className, "readFile", "", "", "Method called. FIle Name = " + fileName.trim());

    try
    {
      fileName = fileName.trim();
      boolean availFlag = chkProfileAvailability(fileName);

      if (availFlag)
      {
        selectedProfileName = fileName;
      }
      else
      {
        Log.errorLog(className, "readFile", "", "", "Profile " + fileName + " is not available. Using " + DEFAULT_PROFILE_NAME + " profile.");
        selectedProfileName = DEFAULT_PROFILE_NAME;

        availFlag = chkProfileAvailability(DEFAULT_PROFILE_NAME);

        if (!availFlag)
        {
          Log.errorLog(className, "readFile", "", "", "availFlag = " + availFlag);
          return null;
        }

        profilePath = (pathToProfile() + DEFAULT_PROFILE_NAME + ".egp");
      }

      return getVecProfileDataByProfileName(selectedProfileName);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "readFile", "", "", "Exception - ", e);
      return null;
    }
  }

  // This is the main function called from constructor that initialize all variables
  private void init(String profileName, Vector vecProfileData)
  {
    try
    {
      Log.debugLog(className, "init", "", "", "Start method.");
      loadSettings(profileName, vecProfileData);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "init", "", "", "Exception - ", e);
    }
  }

  // This function set the value of variables according to the Keyword in profile file
  private void loadSettings(String profileName, Vector vecProfileDataTemp)
  {
    try
    {
      Log.debugLog(className, "loadSettings", "", "", "Method called. File Name = " + profileName);
      if (vecProfileDataTemp != null)
      {
        vecInfo = vecProfileDataTemp;
        loadPropertiesByProfileData(vecInfo);
      }
      else
      {
        if (vecSelectedProfileData == null) // this is at initial time
          vecSelectedProfileData = readFile(profileName);
        else if (!profileName.equals(selectedProfileName)) // this is the case when user loading different profile
          vecSelectedProfileData = readFile(profileName);

        vecInfo = vecSelectedProfileData;
      }

      // update profile description only when vecProfileDataTemp is null, because when user activate/inactivate any graph(s) then this vector is not null
      if (vecProfileDataTemp == null)
        profileDesc = getValue(profileName, "PROFILE_DESC", "NA");

      numPanel = Integer.parseInt(getValue(profileName, "NUMBER_OF_PANELS", "9"));
      numRows = Integer.parseInt(getValue(profileName, "NUMBER_OF_ROWS", "3"));
      numColumn = Integer.parseInt(getValue(profileName, "NUMBER_OF_COLUMNS", "3"));
      numMergedGrp = Integer.parseInt(getValue(profileName, "NUMBER_OF_MERGED_GROUPS", "0"));
      graphTimeOp = Integer.parseInt(getValue(profileName, "GRAPH_TIME", "0"));
      xAxisValOp = Integer.parseInt(getValue(profileName, "X_AXIS_VALUE", "0"));
      scaleOp = Integer.parseInt(getValue(profileName, "SCALE_VALUE", "0"));
      detailPanelOp = Integer.parseInt(getValue(profileName, "DETAIL_PANEL", "0"));
      debugFlagOp = Integer.parseInt(getValue(profileName, "DEBUG_FLAG", "0"));
      lastModifiedBy = getValue(profileName, "UPDATED_BY", "netstorm");

      setDebugFlag(); // Set the Debug Flag
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "loadSettings", "", "", "Exception - ", e);
    }
  }

  // This function set the Debug flag value.
  private void setDebugFlag()
  {
    if (debugFlagOp == 1)
      Config.addConfigParam("debugFlag", "on");
    else
      Config.addConfigParam("debugFlag", "off");
  }

  // This function load the Panel settings in to vector
  private void loadPanelInfoInVec()
  {
    try
    {
      Log.debugLog(className, "loadPanelInfoInVec", "", "", "Start method.");
      vecPanelRec = new Vector();

      if (vecInfo == null)
      {
        for (int i = 0; i < defaultPanelInfo.length; i++)
        {
          Log.debugLog(className, "loadPanelInfoInVec", "", "", "Adding line in vector. Line = " + defaultPanelInfo[i]);
          vecPanelRec.add(defaultPanelInfo[i]);
        }
      }
      else
      {
        for (int i = 0; i < vecInfo.size(); i++)
        {
          if ((vecInfo.elementAt(i).toString()).startsWith("PANEL") || (vecInfo.elementAt(i).toString()).startsWith("TRACKED_PANEL"))
          {
            Log.debugLog(className, "loadPanelInfoInVec", "", "", "Adding line in vector. Line = " + vecInfo.elementAt(i).toString());
            vecPanelRec.add(vecInfo.elementAt(i).toString());
          }
        }
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "loadPanelInfoInVec", "", "", "Exception - ", e);
    }
  }

  // This function load the Merge Grp settings in to vector
  private void loadMergeGrpInfoInVec()
  {
    try
    {
      Log.debugLog(className, "loadMergeGrpInfoInVec", "", "", "Start method.");
      vecMergeGrpRec = new Vector();

      if (vecInfo != null)
      {
        for (int i = 0; i < vecInfo.size(); i++)
        {
          if ((vecInfo.elementAt(i).toString()).startsWith("MERGE_GROUP"))
          {
            Log.debugLog(className, "loadMergeGrpInfoInVec", "", "", "Adding line in vector. Line = " + vecInfo.elementAt(i).toString());
            vecMergeGrpRec.add(vecInfo.elementAt(i).toString());
          }
        }
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "loadMergeGrpInfoInVec", "", "", "Exception - ", e);
    }
  }

  // This function set the Panel Info (Caption, GrpId, GraphId, Vector Option)
  private void loadPanelInfoInArr(String profileName, Vector vecProfileData)
  {
    try
    {
      Log.debugLog(className, "loadPanelInfoInArr", "", "", "Start method.");
      String[][] arrInfo = null;
      String[] temp = null;
      panelInfo = new PanelInfoBean[MAX_NUM_PANEL];

      if (vecPanelRec.size() != MAX_NUM_PANEL)
      {
        for (int i = 0; i < vecPanelRec.size(); i++)
        {
          arrInfo = null;
          temp = null;

          temp = rptUtilsBean.strToArrayData(vecPanelRec.elementAt(i).toString(), "|");
          arrInfo = rptUtilsBean.rptInfoToArr(temp[temp.length - 1]);
          panelInfo[i] = new PanelInfoBean(this, temp[PANEL_CAPTION_INDX], arrInfo);

          if (panelInfo[i].getArrGraphUniqueKeyDTO() == null)
          {
            String defaultPanelInfoLine = defaultPanelInfo[i];
            String[] arrTemp = rptUtilsBean.strToArrayData(defaultPanelInfoLine, "|");
            String[][] arrInfoTemp = rptUtilsBean.rptInfoToArr(arrTemp[arrTemp.length - 1]);

            panelInfo[i] = new PanelInfoBean(this, arrTemp[PANEL_CAPTION_INDX], arrInfoTemp);
          }
          else
          {
            if (vecPanelRec.get(i).toString().startsWith("TRACKED_PANEL"))
              Log.debugLog(className, "loadPanelInfoInArr", "", "", "Set tracked panel info, Panel Index = " + i);
          }
        }
      }
      else
      {
        for (int i = 0; i < MAX_NUM_PANEL; i++)
        {
          arrInfo = null;
          temp = null;

          temp = rptUtilsBean.strToArrayData(vecPanelRec.elementAt(i).toString(), "|");
          arrInfo = rptUtilsBean.rptInfoToArr(temp[temp.length - 1]);
          panelInfo[i] = new PanelInfoBean(this, temp[PANEL_CAPTION_INDX], arrInfo);

          // this is for using default panel
          if (panelInfo[i].getArrGraphUniqueKeyDTO() == null)
          {
            String defaultPanelInfoLine = defaultPanelInfo[i];
            String[] arrTemp = rptUtilsBean.strToArrayData(defaultPanelInfoLine, "|");
            String[][] arrInfoTemp = rptUtilsBean.rptInfoToArr(arrTemp[arrTemp.length - 1]);
            panelInfo[i] = new PanelInfoBean(this, arrTemp[PANEL_CAPTION_INDX], arrInfoTemp);
          }
          else
          {
            if (vecPanelRec.get(i).toString().startsWith("TRACKED_PANEL"))
              Log.debugLog(className, "loadPanelInfoInArr", "", "", "Set tracked panel info, Panel Index = " + i);
          }
        }
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "loadPanelInfoInArr", "", "", "Exception - ", e);
    }
  }

  // This function set the Merge Grp Info (Caption, GrpId, GraphId, Vector Option)
  private void loadMergeGrpInfoInArr()
  {
    try
    {
      Log.debugLog(className, "loadMergeGrpInfoInArr", "", "", "Start method.");
      String[][] arrInfo = null;
      String[] temp = null;
      mergeGrpInfo = new MergeGrpInfoBean[numMergedGrp];

      for (int i = 0; i < vecMergeGrpRec.size(); i++)
      {
        arrInfo = null;
        temp = null;

        temp = rptUtilsBean.strToArrayData(vecMergeGrpRec.elementAt(i).toString(), "|");
        arrInfo = rptUtilsBean.rptInfoToArr(temp[temp.length - 1]);
        mergeGrpInfo[i] = new MergeGrpInfoBean(this, temp[MERGED_GRP_CAPTION_INDX], arrInfo);
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "loadMergeGrpInfoInArr", "", "", "Exception - ", e);
    }
  }

  // This function return the string on the basis of "detailPanelOp"
  public String getDetailPanelFlag()
  {
    try
    {
      Log.debugLog(className, "getDetailPanelFlag", "", "", "Start method.. File Name = " + profilePath);
      String temp = "";
      switch (detailPanelOp)
      {
      case 0:
        temp = "selectedPanel";
        break;
      case 1:
        temp = "displayGraphs";
        break;
      case 2:
        temp = "allGraphs";
        break;
      default:
        temp = "selectedPanel";
        break;
      }
      return temp;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getDetailPanelFlag", "", "", "Exception - ", e);
      return "";
    }
  }

  // This function set the selected panel Caption By Panel Index
  public void setSelectedPanelCaption(int panelIndex, String panelCaption)
  {
    panelInfo[panelIndex].setPanelCaption(panelCaption);
  }

  // This Function Apply selected profiles settings in GUI
  public boolean applyProfile(String tempProfileName, boolean baselineEnableTR)
  {
    try
    {
      RTGProfileBean tempRTGProfile = new RTGProfileBean(1023);
      tempRTGProfile.initRTGProfile(tempProfileName, null);
      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "applyProfile", "", "", "Exception in applyProfile() -", e);
      return false;
    }
  }

  // This function check the profile "Panel" compatibility
  public String[] chkProfilePanelCompatibility(String profileName, Vector vecProfileData)
  {
    try
    {
      Log.debugLogAlways(className, "chkProfilePanelCompatibility", "", "", "Method called. File Name = " + profileName);
      Vector vecLoadProfileInfo;
      Vector vecLoadProfilePanelInfo = new Vector();
      String[] arrFlagValue = new String[MAX_NUM_PANEL]; // true|<Panel_name> or false|"NA"

      if (vecProfileData != null)
      {
        vecLoadProfileInfo = vecProfileData;
      }
      else
      {
        if (vecSelectedProfileData == null) // this is at initial time
          vecSelectedProfileData = readFile(profileName);
        else if (!profileName.equals(selectedProfileName)) // this is the case when user loading different profile
          vecSelectedProfileData = readFile(profileName);

        vecLoadProfileInfo = vecSelectedProfileData;
      }

      for (int i = 0; i < vecLoadProfileInfo.size(); i++)
      {
        String lineInProfile = vecLoadProfileInfo.elementAt(i).toString();
        if (lineInProfile.startsWith("PANEL") || lineInProfile.startsWith("TRACKED_PANEL"))
          vecLoadProfilePanelInfo.add(lineInProfile);
      }

      int i = 0;
      for (i = 0; i < vecLoadProfilePanelInfo.size(); i++)
      {
        String panelLine = vecLoadProfilePanelInfo.elementAt(i).toString();
        String[] arrTemp = rptUtilsBean.strToArrayData(panelLine, "|");
        String panelType = arrTemp[1];
        if (panelType.startsWith("Dial"))// Dial_Min_Max_Condition_WariningThreshold_CriticalThreshold
        {
          arrFlagValue[i] = "true|" + arrTemp[PANEL_CAPTION_INDX];
          continue;
        }

        try
        {
          String tempInfo = arrTemp[arrTemp.length - 1];
          String[][] arrInfoTemp = rptUtilsBean.rptInfoToArr(tempInfo);
          GraphUniqueKeyDTO[] arrGraphUniqueKeyDTO = GraphNameUtils.createGraphUniqueKeyDTO(arrInfoTemp[0], arrInfoTemp[1], arrInfoTemp[3], graphNames, arrVectorMappingList);

          if (arrGraphUniqueKeyDTO == null)
          {
            arrFlagValue[i] = "true|" + arrTemp[PANEL_CAPTION_INDX];
          }
          else
          {
            if (arrGraphUniqueKeyDTO.length == 1 && arrGraphUniqueKeyDTO[0] == null)
            {
              arrFlagValue[i] = "true|" + arrTemp[PANEL_CAPTION_INDX];
            }
            else
            {
              int counter = 0;
              for (int j = 0; j < arrGraphUniqueKeyDTO.length; j++)
              {
                if (arrGraphUniqueKeyDTO[j] == null)
                  counter = counter + 1;
              }

              if (counter == arrGraphUniqueKeyDTO.length)
                arrFlagValue[i] = "true|" + arrTemp[PANEL_CAPTION_INDX];
              else
                arrFlagValue[i] = "false|NA";
            }
          }
        }
        catch (Exception e)
        {
          Log.errorLog(className, "chkProfilePanelCompatibility", "", "", "panelLine = " + panelLine + ", Exception - " + e);
          arrFlagValue[i] = "true|" + arrTemp[PANEL_CAPTION_INDX];
        }
      }

      for (; i < MAX_NUM_PANEL; i++)
      {
        String[] arrDefaultPanel = rptUtilsBean.strToArrayData(defaultPanelInfo[i], "|");
        arrFlagValue[i] = "true|" + arrDefaultPanel[PANEL_CAPTION_INDX];
      }

      return arrFlagValue;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "chkProfilePanelCompatibility", "", "", "Exception in chkProfilePanelCompatibility() -", e);
      return null;
    }
  }

  public String[] chkProfileMergedGrpCompatibility(String profileName, StringBuffer msg)
  {
    return (chkProfileMergedGrpCompatibility(profileName, msg, null));
  }

  // This function chk the profile "Merged Grp" compatibility
  public String[] chkProfileMergedGrpCompatibility(String profileName, StringBuffer msg, Vector vecProfileData)
  {
    try
    {
      Log.debugLog(className, "chkProfileMergedGrpCompatibility", "", "", "Method called. File Name = " + profileName);
      Vector vecLoadProfileInfo;
      Vector vecLoadProfileMergedGrpInfo = new Vector();

      if (vecProfileData != null)
        vecLoadProfileInfo = vecProfileData;
      else
      {
        if (vecSelectedProfileData == null) // this is at initial time
          vecSelectedProfileData = readFile(profileName);
        else if (!profileName.equals(selectedProfileName)) // this is the case when user loading different profile
          vecSelectedProfileData = readFile(profileName);

        vecLoadProfileInfo = vecSelectedProfileData;
      }

      for (int i = 0; i < vecLoadProfileInfo.size(); i++)
      {
        if ((vecLoadProfileInfo.elementAt(i).toString()).startsWith("MERGE_GROUP"))
        {
          vecLoadProfileMergedGrpInfo.add(vecLoadProfileInfo.elementAt(i).toString());
          Log.debugLog(className, "chkProfileMergedGrpCompatibility", "", "", "Data line added to vector = " + vecLoadProfileInfo.elementAt(i).toString());
        }
      }

      if (vecLoadProfileMergedGrpInfo == null || vecLoadProfileMergedGrpInfo.size() == 0)
      {
        String temp = "NA";
        msg.append(temp);
        return null;
      }

      String[] arrCompVal = new String[vecLoadProfileMergedGrpInfo.size()];
      int i = 0;
      for (i = 0; i < vecLoadProfileMergedGrpInfo.size(); i++)
      {
        String[] arrTemp = rptUtilsBean.strToArrayData(vecLoadProfileMergedGrpInfo.elementAt(i).toString(), "|");
        String[][] arrInfoTemp = rptUtilsBean.rptInfoToArr(arrTemp[arrTemp.length - 1]);

        GraphUniqueKeyDTO[] arrGraphUniqueKeyDTO = GraphNameUtils.createGraphUniqueKeyDTO(arrInfoTemp[0], arrInfoTemp[1], arrInfoTemp[3], graphNames, arrVectorMappingList);

        if (arrGraphUniqueKeyDTO == null)
        {
          arrCompVal[i] = "true|" + arrTemp[MERGED_GRP_CAPTION_INDX];
        }
        else
        {
          if (arrGraphUniqueKeyDTO.length == 1 && arrGraphUniqueKeyDTO[0] == null)
          {
            arrCompVal[i] = "true|" + arrTemp[PANEL_CAPTION_INDX];
          }
          else
          {
            int counter = 0;
            for (int j = 0; j < arrGraphUniqueKeyDTO.length; j++)
            {
              if (arrGraphUniqueKeyDTO[j] == null)
                counter = counter + 1;
            }

            if (counter == arrGraphUniqueKeyDTO.length)
              arrCompVal[i] = "true|" + arrTemp[MERGED_GRP_CAPTION_INDX];
            else
              arrCompVal[i] = "false|NA";
          }
        }

        Log.debugLog(className, "chkProfileMergedGrpCompatibility", "", "", "Add bool value = " + arrCompVal[i]);
      }

      return arrCompVal;
    }
    catch (Exception e)
    {
      String temp = "Exception";
      msg.append(temp);
      Log.stackTraceLog(className, "chkProfileMergedGrpCompatibility", "", "", "Exception in chkProfileMergedGrpCompatibility() -", e);
      return null;
    }
  }

  public boolean chkProfileEnableWithBaselineTracking(String profileName)
  {
    return (chkProfileEnableWithBaselineTracking(profileName, null));
  }

  // this function is to chk that profile has baseline tracking enabled or not
  public boolean chkProfileEnableWithBaselineTracking(String profileName, Vector vecProfileData)
  {
    try
    {
      Log.debugLog(className, "chkProfileEnableWithBaselineTracking", "", "", "Method called. File Name = " + profileName);
      Vector vecLoadProfileInfo = null;
      if (vecProfileData != null)
      {
        vecLoadProfileInfo = vecProfileData;
      }
      else
      {
        if (vecSelectedProfileData == null) // this is at initial time
          vecSelectedProfileData = readFile(profileName);
        else if (!profileName.equals(selectedProfileName)) // this is the case when user loading different profile
          vecSelectedProfileData = readFile(profileName);

        vecLoadProfileInfo = vecSelectedProfileData;
      }

      boolean result = false;

      for (int i = 0; i < vecLoadProfileInfo.size(); i++)
      {
        if ((vecLoadProfileInfo.elementAt(i).toString()).startsWith("TRACKED_PANEL"))
        {
          Log.debugLog(className, "chkProfileEnableWithBaselineTracking", "", "", "Profile " + profileName + " has baseline tracking.");
          result = true;
          break;
        }
      }

      return result;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "chkProfileEnableWithBaselineTracking", "", "", "Exception in chkProfileEnableWithBaselineTracking() -", e);
      return false;
    }
  }

  public static void main(String[] args)
  {
  }
}

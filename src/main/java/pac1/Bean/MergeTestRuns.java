/*--------------------------------------------------------------------
@Name    : MergeTestRuns
@Author  : Ravi Kant Sharma
@Date    : April 02, 2012
@Purpose : Merging of two test runs in one test run
@Modification History:

 ----------------------------------------------------------------------*/

package pac1.Bean;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import pac1.Bean.GraphName.*;

public class MergeTestRuns
{
  private static String className = "MergeTestRuns";

  // used for calculating interval period
  // if interval period is not same in both test run, we would not allow
  // merging
  private int interval = 0;
  // used to hold next msg data index of graph for merge testrun.gdf
  private int nextGraphDataIdx = 0;
  // used to check type of group
  private String groupType = "";
  // used for initialized the data index of graph
  private int previousDataIdx = 0;
  // if group type is vector, then next data index will dataTypeSize * no of graphs
  private int nextDataIdx = 0;
  // returns the working path eg. "C:/home/netstorm/work"
  private String workPath = Config.getWorkPath();
  // creating reference variables of GraphNames class
  private transient GraphNames graphNames1 = null;
  private transient GraphNames graphNames2 = null;
  // testRunNum1 and testRunNum2 will contains the test run number which are to be merged
  private transient int testRunNum1 = 0;
  private transient int testRunNum2 = 0;
  private transient String startElapsedTimeTR1 = "00:00:00";
  private transient String startElapsedTimeTR2 = "00:00:00";
  private transient String endElapsedTimeTR1 = "00:00:00";
  private transient String endElapsedTimeTR2 = "00:00:00";
  // used for merge test run number that is returns from Shell "nsi_get_next_test_run_id"
  private transient int mergeNumTestRun = 0;
  // contains testrun.gdf file version number
  private transient String VERSION_NUMBER = "";
  // contains size of message data
  private transient int sizeOfMsgData = 0;
  // use for major and minor version of testrun.gdf file
  // for example - in file version 3.0, major version is 3 and minor version
  // is 0
  private transient int minorVersionNum = 0;
  private transient int majorVersionNum = 0;
  // size of data element
  private transient int dataElementSize = 0;
  // array list of Groupid.GraphId.VectorName, graph data idx and data type of graph
  // 0th Index it will have GroupID.GraphID.VectorName
  // 1st Index it will have GraphDataIdx of given TestRun
  // 2nd Index it will have DataType of graph for given TestRun
  // 3rd index it will have group and graph lines of passed test run
  // 4th index it will have linked hash map with key group id and value will be number of graph for key (group id)
  private ArrayList arrAllInfoAbtTR1 = null;
  private ArrayList arrAllInfoAbtTR2 = null;
  // used to mode of merging
  // 0 for union mode and 1 for append mode
  private int mergeMode = 0;
  // array list of merged GroupId-GraphId-VectorName
  private ArrayList arrGroupIdGraphIdVectName = new ArrayList();
  // used for data type of graphs in Merged Test Run Number
  private ArrayList arrDataType = new ArrayList();
  // used for data index of MergeTestRun Number which is taken from First TestRun Number
  // (Base Line Test Run Number), if any graph is presnt in Second Test Run Number
  // but not in Base Line Test Run Number then data index will be -1
  private ArrayList arrDataIdxTR1 = new ArrayList();
  // used for data index of Merge Test Run Number which is taken from Second Test Run Number
  // if any graph is present in First Test Run Nuber but not in Second Test Run number
  // then data index will be -1
  private ArrayList arrDataIdxTR2 = new ArrayList();
  // used for holding group type
  String prevGroupType = "";
  // used to get the name of logged user
  String loggedUserName = "";
  // used for user group
  String userGroup = "";
  // testrun directory path for merge test run number
  String mergeTRNumberDirectoryPath = "";
  // used arraylist flag for append mode
  ArrayList arrlistOfGrpIdGraphIdVectNameOfTR1 = null;
  ArrayList arrGrpIdGraphIdVectNameTR2 = null;

  // constructor of MergeTestRuns class and sets logged user name in machine
  public MergeTestRuns(String loggedUserName)
  {
    this.loggedUserName = loggedUserName;

  }

  // method to getting testrun data
  // Note - Here we are getting gdf data
  public ArrayList getDataOfTestRuns(final int testRunNum1, final int testRunNum2)
  {
    try
    {
      Log.debugLog(className, "getDataOfTestRuns", "", "", "Method called.");
      // used for getting system time when getDataOfTestRuns method called
      long methodCallingTime = System.currentTimeMillis();
      // merged grpId-GraphId-VectName , data index and data type
      ArrayList arrGGVDataIdxDataType = getGraphDataIdxDataTypes();
      // getting array list that contains at 0th index (GrpId-GraphId-VectName)
      // at 1st index (data indexes of base line test run)
      // at 2nd index (data indexes of second test run)
      // at 3rd index (data type of graphs)
      // 3rd index it will have group and graph lines of passed test run
      // 4th index it will have linked hash map with key group id and value will be number of graph for key (group id)
      arrGroupIdGraphIdVectName = (ArrayList) arrGGVDataIdxDataType.get(0);
      arrDataIdxTR1 = (ArrayList) arrGGVDataIdxDataType.get(1);
      arrDataIdxTR2 = (ArrayList) arrGGVDataIdxDataType.get(2);
      arrDataType = (ArrayList) arrGGVDataIdxDataType.get(3);
      long methodEndTime = System.currentTimeMillis();
      Log.debugLog(className, "getDataOfTestRuns", "", "", "Total Time Taken in miliSeconds = " + (methodEndTime - methodCallingTime));
      ArrayList arrGDFDataStatus = new ArrayList();
      arrGDFDataStatus.add("Success");
      return arrGDFDataStatus;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getDataOfTestRuns", "", "", "Exception - ", ex);
      ArrayList arrGDFDataStatus = new ArrayList();
      arrGDFDataStatus.add("Error");
      arrGDFDataStatus.add(ex);
      return arrGDFDataStatus;
    }
  }

  // return major version of GDF
  public int getMajorVersion()
  {
    return majorVersionNum;
  }

  // returns data type size
  private int getDataTypeSize(final String dataTypeName)
  {
    int dataTypeSize = dataElementSize;

    if (dataTypeName.equals("sample"))
      return dataTypeSize;

    if (dataTypeName.equals("rate"))
      return dataTypeSize;

    if (dataTypeName.equals("times"))
      return (dataTypeSize * 4);

    if (majorVersionNum < 2)
    {
      if (dataTypeName.equals("cumulative"))
        return (dataTypeSize * 2);

      if (dataTypeName.equals("timesStd"))
        return (dataTypeSize * 6);
    }
    else
    {
      if (dataTypeName.equals("cumulative"))
        return dataTypeSize;

      if (dataTypeName.equals("timesStd"))
        return (dataTypeSize * 5);
    }
    return 0;
  }

  // return minor version of GDF
  public int getMinorVersion()
  {
    return minorVersionNum;
  }

  // Return the start index
  public int getStartIndex()
  {
    try
    {
      return graphNames1.getStartIndex();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      return 0;
    }
  }

  // returns start date time of merge test run
  public String getTestRunStartDateTime()
  {
    return graphNames1.getTestRunStartDateTime();
  }

  // returns interval for merge test run number
  public int getInterval()
  {
    try
    {
      return interval;
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      return 0;
    }
  }

  // compare two string time format "HH:MM:SS" and returns max time
  public String findMaxDuration(String testDuration1, String testDuration2)
  {
    try
    {
      Log.debugLog(className, "findMaxDuration", "", "", "method called.");
      String maxTimeValue = "";
      long testRunDuration1 = rptUtilsBean.convStrToMilliSec(testDuration1);
      long testRunDuration2 = rptUtilsBean.convStrToMilliSec(testDuration2);

      if (testRunDuration1 <= testRunDuration2)
      {
        maxTimeValue = testDuration2;
      }
      else
      {
        maxTimeValue = testDuration1;
      }

      return maxTimeValue;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "findMaxDuration", "", "", "Exception - ", ex);
      return "";
    }
  }

  // returns info line for testrun.gdf, fields of info line are
  // Info|1.0|16|52|29430|1000|10000|10/9/07 12:41:18
  // where VERSION = 1.0, NUM_GROUP = 16, Start Index = 52, TestRunNum =
  // 29430, Size of message data = 1000
  // interval = 10000 , TEST_RUN_START_DATETIME = 10/9/07 12:41:18
  // this method will call from writeMergeTestRunGDF()
  // returns info line of testrun.gdf
  public String getInfoLine()
  {
    try
    {
      Log.debugLog(className, "getInfoLine", "", "", "Method called.");
      String strInfoLine = "";
      strInfoLine = "Info|" + VERSION_NUMBER;
      strInfoLine = strInfoLine + "|" + getTotalGroupsInMergeTR();
      strInfoLine = strInfoLine + "|" + getStartIndex();
      strInfoLine = strInfoLine + "|" + mergeNumTestRun;
      strInfoLine = strInfoLine + "|" + sizeOfMsgData;
      strInfoLine = strInfoLine + "|" + getInterval();
      strInfoLine = strInfoLine + "|" + getTestRunStartDateTime();

      return strInfoLine;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getInfoLine", "", "", "Exception - ", ex);
      ex.printStackTrace();
      return "";
    }
  }

  /*------------------------- Algorithm -------------------------------
   *
   * Let Two test runs TR Numbers 1234 and 1235 and we will merge both test run into new Test Run like TRxxxx
   *
   * Suppose in TR1234,
   * GrpIdGraphIdVectName are
   * (1-1-NA, 1-2-NA, 1-3-NA, 1-4-NA , 1-5-NA, 1-6-NA, 2-1-NA, 2-2-NA, 3-1-NA, 7-1-NA, 7-2-MiscErr, 7-3-1xx)
   * GraphDataIdx (0,1,2,3,4,5,6,7,8,9,10,11)
   * GraphDataType(0,0,0,0,0,0,1,1,2,3,3,3)
   *
   * Suppose in TR1235
   * GrpIdGraphIdVectName are
   * (1-1-NA, 1-2-NA, 1-3-NA, 1-4-NA, 2-1-NA, 2-2-NA, 3-1-NA,3-2-NA, 4-1-NA, 7-1-NA, 7-2-MiscErr, 7-3-1xx)
   * GraphDataIdx (0,1,2,3,6,7,9,10,12,16,17,18)
   * GraphDataType(0,0,0,0,1,1,2,2,1,3,3,3)
   *
   * Then merge TRxxxx will contain
   *
   * GrpIdGraphIdVectName will be
   * (1-1-NA, 1-2-NA, 1-3-NA, 1-4-NA , 1-5-NA, 1-6-NA, 2-1-NA, 2-2-NA, 3-1-NA,3-2-NA,4-1-NA, 7-1-NA, 7-2-MiscErr, 7-3-1xx)
   * GraphDataIdx1234 (0, 1, 2, 3, 4, 5, 6, 7, 8, -1, -1, 9, 10,11)
   * GraphDataIdx1235 (0, 1, 2, 3, -1, -1, 6, 7, 9, 10, 12, 16, 17, 18)
   * GraphDataType (0 ,0 , 0, 0, 0, 0, 1, 1, 2, 2, 1, 3, 3 ,3)
   *
   * Note:- if GrpIdGraphIdVectName is not present in given testRunNum then
   * we will add -1 in graph Data index
   *
   * Note :-
   * 0 for DATA_TYPE_SAMPLE,
   * 1 for DATA_TYPE_RATE,
   * 2 for DATA_TYPE_CUMULATIVE,
   * 3 for DATA_TYPE_TIMES and
   * 4 for DATA_TYPE_TIMES_STD

  ----------------------------------------------------------------------*/

  // return array list of graph data index of given test runs
  // "testRunNum1" & "testRunNum2"
  // and data type for merged test run
  // if GroupId-GraphId-VectorName is not present in given test run
  // then data index will be -1.
  // returns groupId-GraphId-VectorName and data index
  int index = 0;

  public ArrayList getGraphDataIdxDataTypes()
  {
    try
    {
      Log.debugLog(className, " getGraphDataIdxDataTypes ", " ", " ", "Test Run Num1 =" + testRunNum1 + ", Test Run Num2 = " + testRunNum2);

      // arrGGVGraphDataIdxDataType will contain GrpIdGraphIdVectName,
      // graph data index of given testRunNum "testRunNum1" and "testRunNum2" and data type
      ArrayList arrGGVGraphDataIdxDataType = new ArrayList();

      // getting ArrayList of GroupId-GraphId-VectorName for given test run "testRunNum1"
      arrlistOfGrpIdGraphIdVectNameOfTR1 = (ArrayList) arrAllInfoAbtTR1.get(0);

      // getting ArrayList of GroupId-GraphId-VectorName for given test run "testRunNum2"
      arrGrpIdGraphIdVectNameTR2 = (ArrayList) arrAllInfoAbtTR2.get(0);
      // getting ArrayList of graph data index for given test run "testRunNum1"
      ArrayList arrDataIdxTR1 = (ArrayList) arrAllInfoAbtTR1.get(1);
      // getting ArrayList of graph data index for given test run "testRunNum2"
      ArrayList arrDataIdxTR2 = (ArrayList) arrAllInfoAbtTR2.get(1);
      // getting ArrayList of data types for given test run "testRunNum1"
      ArrayList arrDataTypeTR1 = (ArrayList) arrAllInfoAbtTR1.get(2);
      // getting ArrayList of data types for given test run "testRunNum2"
      ArrayList arrDataTypeTR2 = (ArrayList) arrAllInfoAbtTR2.get(2);
      // it will contain GroupId-GraphId-VectorName of given test runs "testRunNum1" and "testRunNum2"
      ArrayList arrUnionGrpIdGraphIdVectName = new ArrayList();
      arrUnionGrpIdGraphIdVectName.addAll(arrlistOfGrpIdGraphIdVectNameOfTR1);

      // adding GroupId-GraphId-VectorName of test run "testRunNum2" in
      // ArrayList arrUnionGrpIdGraphIdVectName
      // if GroupId-GraphId-VectorName of test run "testRunNum2" is not
      // present in ArrayList "arrUnionGrpIdGraphIdVectName"
      LinkedHashMap hmTotalGraphsByGrpId = (LinkedHashMap) arrAllInfoAbtTR1.get(4);
      for (int i = 0; i < arrGrpIdGraphIdVectNameTR2.size(); i++)
      {
        // grpId - graphId- VectName of TR2
        String grpIdGraphIdVectorNameFromTR2 = (String) arrGrpIdGraphIdVectNameTR2.get(i);
        // if grpId-GraphId-VectorName is not present in TR1
        if (!arrUnionGrpIdGraphIdVectName.contains(grpIdGraphIdVectorNameFromTR2))
        {
          String[] arrGrpIdGraphIdVectName = rptUtilsBean.strToArrayData(grpIdGraphIdVectorNameFromTR2, "-");
          // getting group id
          int groupId = Integer.parseInt(arrGrpIdGraphIdVectName[0]);
          // if this group is already present in TR1
          if (hmTotalGraphsByGrpId.containsKey(groupId))
          {
            Iterator iterator = hmTotalGraphsByGrpId.keySet().iterator();
            // used to arraylist index where we put new grpId-GraphId-VectorName
            int index = 0;
            while (iterator.hasNext())
            {
              // group id of TR1
              int groupIdTR1 = Integer.parseInt(iterator.next().toString());
              // getting number of graphs for perticular group id
              int value = Integer.parseInt(hmTotalGraphsByGrpId.get(groupIdTR1).toString());
              // calculating index
              index = index + value;
              if (groupId == groupIdTR1)
              {
                // adding grpId - graphId- VectName of TR2 that is not present in TR1
                arrUnionGrpIdGraphIdVectName.add(index, grpIdGraphIdVectorNameFromTR2);
                // int totalGraphs = Integer.parseInt(hmTotalGraphsByGrpId.get(groupId).toString());
                int totalGraphs = value + 1;
                // updating hashmap value when new graph is added in merged arraylist of GrpId-GraphId-VectName
                hmTotalGraphsByGrpId.put(groupId, totalGraphs);
                break;
              }
            }
          }
          else
          {
            // if group is new that is not present in TR1
            // adding graph entry in arraylist
            arrUnionGrpIdGraphIdVectName.add(grpIdGraphIdVectorNameFromTR2);
            // updating hashmap value when new graph is added in array list
            hmTotalGraphsByGrpId.put(groupId, 1);
          }
        }
      }

      // arrGraphDataIdxTR1 will contain data index for test run "testRunNum1" after merging
      ArrayList arrGraphDataIdxTR1 = new ArrayList();

      // arrGraphDataIdxTR2 will contain data index for test run "testRunNum2" after merging
      ArrayList arrGraphDataIdxTR2 = new ArrayList();

      // arrGraphDataType will contain data types for merged test run
      ArrayList arrGraphDataType = new ArrayList();

      for (int i = 0; i < arrUnionGrpIdGraphIdVectName.size(); i++)
      {
        // getting each item of union GroupId-GraphId-VectorName
        String GrpIdGraphIdVectName = arrUnionGrpIdGraphIdVectName.get(i).toString();
        // when GrpIdGraphIdVectorName is present in testRunNum1 and testRunNum2
        if (arrlistOfGrpIdGraphIdVectNameOfTR1.contains(GrpIdGraphIdVectName) && arrGrpIdGraphIdVectNameTR2.contains(GrpIdGraphIdVectName))
        {
          // getting data index of given test run testRunNum1 for GrpIdGraphIdVectName
          int dataIdxTR1 = Integer.parseInt(arrDataIdxTR1.get(arrlistOfGrpIdGraphIdVectNameOfTR1.indexOf(GrpIdGraphIdVectName)).toString());
          arrGraphDataIdxTR1.add(i, dataIdxTR1);
          // getting data type of given test run "testRunNum1"
          int dataType = Integer.parseInt(arrDataTypeTR1.get(arrlistOfGrpIdGraphIdVectNameOfTR1.indexOf(GrpIdGraphIdVectName)).toString());
          arrGraphDataType.add(i, dataType);
          // getting data index of given test run testRunNum2 for GrpIdGraphIdVectName
          int dataIdxTR2 = Integer.parseInt(arrDataIdxTR2.get(arrGrpIdGraphIdVectNameTR2.indexOf(GrpIdGraphIdVectName)).toString());
          arrGraphDataIdxTR2.add(i, dataIdxTR2);
        }
        else if (arrlistOfGrpIdGraphIdVectNameOfTR1.contains(GrpIdGraphIdVectName))
        {
          // when GrpIdGraphIdVectorName is present in testRunNum1
          int dataIdx = Integer.parseInt(arrDataIdxTR1.get(arrlistOfGrpIdGraphIdVectNameOfTR1.indexOf(GrpIdGraphIdVectName)).toString());
          // adding data index
          arrGraphDataIdxTR1.add(i, dataIdx);
          int dataType = Integer.parseInt(arrDataTypeTR1.get(arrlistOfGrpIdGraphIdVectNameOfTR1.indexOf(GrpIdGraphIdVectName)).toString());
          arrGraphDataType.add(i, dataType); // adding data type
          // GrpIdGraphIdVectorName is not present in testRunNum2 then we will add -1 for graphDataIndex
          arrGraphDataIdxTR2.add(i, "-1");
        }
        // when GrpId-GraphId-VectorName is present in testRunNum2
        else
        {
          arrGraphDataIdxTR1.add(i, "-1");
          // getting data index
          int dataIdx = Integer.parseInt(arrDataIdxTR2.get(arrGrpIdGraphIdVectNameTR2.indexOf(GrpIdGraphIdVectName)).toString());
          arrGraphDataIdxTR2.add(i, dataIdx); // adding data index
          // getting data type
          int dataType = Integer.parseInt(arrDataTypeTR2.get(arrGrpIdGraphIdVectNameTR2.indexOf(GrpIdGraphIdVectName)).toString());
          // adding data type
          arrGraphDataType.add(i, dataType);
        }
      }

      // adding Union of GrpIdGraphIdVectName
      arrGGVGraphDataIdxDataType.add(arrUnionGrpIdGraphIdVectName);
      Log.debugLog(className, "getInfoAboutGraph", "", "", "in testrun " + mergeNumTestRun + " Total Graphs = " + arrUnionGrpIdGraphIdVectName.size());
      // adding graph data index of test run "testRunNum1" in ArrayList
      // "arrGraphDataIdxDataType"
      arrGGVGraphDataIdxDataType.add(arrGraphDataIdxTR1);
      // adding graph data index of test run "testRunNum2" in ArrayList
      // "arrGraphDataIdxDataType"
      arrGGVGraphDataIdxDataType.add(arrGraphDataIdxTR2);
      // adding graph data type of new merge test run
      arrGGVGraphDataIdxDataType.add(arrGraphDataType);
      return arrGGVGraphDataIdxDataType;

    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getGraphDataIdxDataTypes", "", "", "Exception - ", ex);
      ex.printStackTrace();
      return null;
    }
  }

  // returns all group id for merge test run
  public int getTotalGroupsInMergeTR()
  {
    try
    {
      Log.debugLog(className, " getAllGroupId ", " ", " ", " method start ");
      // used for group ids
      ArrayList<Integer> arrGroupId = new ArrayList<Integer>();
      for (int i = 0; i < arrGroupIdGraphIdVectName.size(); i++)
      {
        String strGGV = (String) arrGroupIdGraphIdVectName.get(i);
        int idx = strGGV.indexOf("-");
        int grpId = Integer.parseInt(strGGV.substring(0, idx));
        if (!arrGroupId.contains(grpId))
        {
          arrGroupId.add((grpId));
        }
      }
      int totalGroups = arrGroupId.size();
      return totalGroups;
    }
    catch (Exception ex)
    {
      Log.errorLog(className, "getAllGroupId", "", "", "Exception in getting all group ids -" + ex);
      ex.printStackTrace();
      return 0;
    }
  }

  // creates testrun directory for merge test run number
  private String createTestRunDir(int testrun)
  {
    try
    {
      Log.debugLog(className, "createTestRunDir", "", "", "Method called");
      if (loggedUserName.equals("admin"))
      {
        userGroup = "root";
        loggedUserName = "root";
      }
      else
      {
        userGroup = "netstorm";
      }

      String mergeTRNumberDirectoryPath = workPath + "/webapps/logs/TR" + testrun;
      File file = new File(mergeTRNumberDirectoryPath);

      if (file.exists())
      {
        Log.debugLog(className, "createTestRunDir", "", "", "TestRun " + testrun + " was already exist so it is delete. ");
        String cmdName = "rm";
        String cmdargs = " -rf ";
        CmdExec cmdExec = new CmdExec();
        cmdExec.getResultByCommand(cmdName, cmdargs + mergeTRNumberDirectoryPath, 1, "root", "root");
      }

      if (file.mkdir())
      {
        Log.debugLog(className, "createTestRunDir", "", "", "Fresh directory for testrun " + testrun + " created.");
      }

      // change testrun permisson
      // 777 means 1 is execute , 2 is write and 4 is read
      if (rptUtilsBean.changeFilePerm(file.getAbsolutePath(), loggedUserName, userGroup, "777") == false)
      {
        Log.debugLog(className, "createTestRunDir", "", "", "Permisson for TestRun " + testrun + " is not changed.");
        return "";
      }
      else
      {
        Log.debugLog(className, "createTestRunDir", "", "", "Permisson for TestRun " + testrun + "is changed.");
      }
      this.mergeTRNumberDirectoryPath = mergeTRNumberDirectoryPath;
      return mergeTRNumberDirectoryPath;
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      return "";
    }
  }

  // create a method that returns total graphs for given group id
  public int getTotalGraphs(int groupId)
  {
    try
    {
      // used for total graphs
      int totalGraphs = 0;
      // used for total graphs
      ArrayList arrTotalGraphs = new ArrayList();
      for (int i = 0; i < arrGroupIdGraphIdVectName.size(); i++)
      {
        String ggv = (String) arrGroupIdGraphIdVectName.get(i);
        String[] arrGGV = rptUtilsBean.strToArrayData(ggv, "-");
        int grpId = Integer.parseInt(arrGGV[0]);
        String graphId = arrGGV[1];
        if (grpId == groupId)
        {
          if (!arrTotalGraphs.contains(graphId))
          {
            arrTotalGraphs.add(graphId);
          }
        }
      }
      // calculating total graphs
      totalGraphs = arrTotalGraphs.size();
      return totalGraphs;
    }
    catch (Exception ex)
    {
      return 0;
    }
  }

  // method to convert string array to string
  // eg. suppose We have string array like String[] arrStr = {"Net", "Omini"}
  // it will return Net|Omini
  public String arrToString(String arrString[])
  {
    try
    {
      Log.debugLog(className, "arrToString", "", "", "Method called");
      String strTmp = arrString[0];
      for (int recNum = 1; recNum < arrString.length; recNum++)
        strTmp = strTmp + "|" + arrString[recNum];
      return (strTmp);
    }
    catch (Exception ex)
    {
      Log.errorLog(className, "arrToString", "", "", "Exception in changing string array to string -" + ex);
      ex.printStackTrace();
      return "";
    }
  }

  // method that returns header version 2 (it will be 112 bytes means 14 doubles (14*8))
  // creating rtgMessage.dat file header
  private double[] getHeaderVer2(int OPCODE, int TEST_RUN, int INTERVAL, int SEQ_NUM)
  {
    try
    {
      double[] arrHeader = new double[14];
      arrHeader[0] = OPCODE;
      arrHeader[1] = TEST_RUN;
      arrHeader[2] = INTERVAL;
      arrHeader[3] = SEQ_NUM;

      // 10 fields for future use
      arrHeader[4] = 0;
      arrHeader[5] = 0;
      arrHeader[6] = 0;
      arrHeader[7] = 0;
      arrHeader[8] = 0;
      arrHeader[9] = 0;
      arrHeader[10] = 0;
      arrHeader[11] = 0;
      arrHeader[12] = 0;
      arrHeader[13] = 0;
      return arrHeader;
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      return null;
    }
  }

  // method to get RawData of test run from rtgMessage.dat file by passing test run number
  // returns all packets of raw data
  private ArrayList getRawDataByTestRunNum(int testRunNum, int packetSizeTR, String startElapsedTime, String endElapsedTime)
  {
    try
    {
      Log.debugLog(className, "getRawDataByTestRunNum", "", "", "method called. testRunNum = " + testRunNum + ", packetSize = " + packetSizeTR + ", startElapsedTime = " + startElapsedTime + ", endElapsedTime = " + endElapsedTime);
      // getting rtgMessage.dat file path
      String rtgFileWithPath = Config.getWorkPath() + "/webapps/logs/TR" + testRunNum + "/rtgMessage.dat";
      if (interval == 0 || interval == -1)
        interval = 10000;

      long startTime = rptUtilsBean.convStrToMilliSec(startElapsedTime);
      long endTime = rptUtilsBean.convStrToMilliSec(endElapsedTime);
      long startSeq = startTime / interval;
      long endSeq = endTime / interval;
      Log.debugLog(className, "getRawDataByTestRunNum", "", "", "startSeq = " + startSeq + ", endSeq = " + endSeq);
      // vector that contains all packets of raw data
      ArrayList arrRawdata = new ArrayList();

      File rtgFile = new File(rtgFileWithPath);
      if (!rtgFile.exists())
      {
        ArrayList err = new ArrayList();
        err.add("Error");
        err.add("Testrun " + testRunNum + " does not have required file(s). So you cannot merge these test runs.");
        return err;
      }
      // creating instance of input streams
      FileInputStream fis = new FileInputStream(rtgFile);
      DataInputStream dis = new DataInputStream(fis);

      // creating byte array
      byte byteBuf[] = new byte[(int) packetSizeTR];
      while (dis.read(byteBuf) != -1)
      {
        // wrap the byte array to byte buffer
        ByteBuffer bb = ByteBuffer.wrap(byteBuf);
        // setting the order of bytes
        bb = bb.order(ByteOrder.LITTLE_ENDIAN);
        // creating packet of data
        double[] pkt = new double[(int) packetSizeTR / 8];
        // index for reading data
        int index = 0;
        for (int i = 0; i < packetSizeTR / 8; i++)
        {
          pkt[i] = bb.getDouble(index);
          index = index + 8;
        }

        // data packet length would be (packet size - header size )/8
        int pktLength = (int) ((packetSizeTR - 112) / 8);
        // graph data array
        double[] arrGraphRawData = new double[pktLength];
        // used to leave header part of rtgMessage.dat
        int count = 0;
        boolean needToAdd = false;
        for (int j = 14; j < pkt.length; j++)
        {
          if (pkt[0] == 0 || pkt[0] == 2)
            break;
          if (pkt[3] >= startSeq && pkt[3] <= endSeq)
          {
            arrGraphRawData[count] = pkt[j];
            count++;
            needToAdd = true;
          }
          else
            break;
        }

        // adding graph data to array list
        if (needToAdd)
          arrRawdata.add(arrGraphRawData);
      }

      fis.close();
      dis.close();
      return arrRawdata;
    }
    catch (Exception ex)
    {
      Log.errorLog(className, "getRawDataByTestRunNum", "", "", "error while reading graph raw data from rtgMessage.dat file. ");
      ex.printStackTrace();
      ArrayList err = new ArrayList();
      err.add("Error");
      err.add(ex);
      return err;
    }
  }

  // method to calculate graph data when both test run have packets
  private double[] getCalcGraphData(double[] arrGraphData1, double[] arrGraphData2)
  {
    try
    {
      Log.debugLog(className, "getCalcGraphData", "", "", "Method called.");
      // data index for merged test run's graph data
      int dataIdx = 0;
      // merged packet data size
      int mergePKTDataSize = (sizeOfMsgData - 112) / 8;

      // if merge mode is append
      if (mergeMode == 1)
      {
        // array that holds merged graph data
        double[] arrMergedGraphData = new double[mergePKTDataSize];

        int dataType = -1;
        // used this idiom because if list is random access then it
        // gives best performance otherwise
        // it displays quadratic performance.
        for (int id = 0, n = arrGroupIdGraphIdVectName.size(); id < n; id++)
        {
          String strGrpIdGraphIdVectName = (String) arrGroupIdGraphIdVectName.get(id);
          String[] arrGGV = rptUtilsBean.strToArrayData(strGrpIdGraphIdVectName, "-");
          String graphId = arrGGV[1];

          // getting data index of graph in base line test run
          int dataIdxTR1 = Integer.parseInt(arrDataIdxTR1.get(id).toString());

          // getting data index of graph in second test run
          int dataIdxTR2 = Integer.parseInt(arrDataIdxTR2.get(id).toString());

          // getting data type of graph
          dataType = (Integer) arrDataType.get(id);
          if (dataType == 0 || dataType == 1 || dataType == 2)
          {
            if (dataIdxTR1 != -1)
            {
              double graphDataValueTR1 = arrGraphData1[dataIdxTR1];
              // assigning new graph data to array
              arrMergedGraphData[dataIdx] = graphDataValueTR1;
              dataIdx = dataIdx + 1;
            }
            else
            {
              double graphDataValueTR2 = arrGraphData2[dataIdxTR2];
              arrMergedGraphData[dataIdx] = graphDataValueTR2;
              dataIdx = dataIdx + 1;
            }
          }
          else if (dataType == 3)
          {
            if (dataIdxTR1 != -1)
            {
              // assigning new graph data to array
              double graphDataValueTR1 = arrGraphData1[dataIdxTR1];
              arrMergedGraphData[dataIdx] = graphDataValueTR1;
              dataIdx = dataIdx + 1;

              // assigning min value to merged graph data array
              double minValueTR1 = arrGraphData1[dataIdxTR1 + 1];
              arrMergedGraphData[dataIdx] = minValueTR1;
              dataIdx = dataIdx + 1;

              // assigning max value to merged graph data array
              double maxValueTR1 = arrGraphData1[dataIdxTR1 + 2];
              arrMergedGraphData[dataIdx] = maxValueTR1;
              dataIdx = dataIdx + 1;

              // assigning count value to merged graph data array
              double countValueTR1 = arrGraphData1[dataIdxTR1 + 3];
              arrMergedGraphData[dataIdx] = countValueTR1;
              dataIdx = dataIdx + 1;
            }
            else
            {
              // assigning new graph data to array
              double graphDataTR2 = arrGraphData2[dataIdxTR2];
              arrMergedGraphData[dataIdx] = graphDataTR2;
              dataIdx = dataIdx + 1;

              // assigning min value to merged graph data array
              double minValueTR2 = arrGraphData2[dataIdxTR2 + 1];
              arrMergedGraphData[dataIdx] = minValueTR2;
              dataIdx = dataIdx + 1;

              // assigning max value to merged graph data array
              double maxValueTR2 = arrGraphData2[dataIdxTR2 + 2];
              arrMergedGraphData[dataIdx] = maxValueTR2;
              dataIdx = dataIdx + 1;

              // assigning count value to merged graph data array
              double countValueTR2 = arrGraphData2[dataIdxTR2 + 3];
              arrMergedGraphData[dataIdx] = countValueTR2;
              dataIdx = dataIdx + 1;
            }
          }
          else if (dataType == 4)
          {
            if (dataIdxTR1 != -1)
            {
              // assigning new graph data to array
              double graphDataValueTR1 = arrGraphData1[dataIdxTR1];
              arrMergedGraphData[dataIdx] = graphDataValueTR1;
              dataIdx = dataIdx + 1;

              // assigning min value to merged graph data array
              double minValueTR1 = arrGraphData1[dataIdxTR1 + 1];
              arrMergedGraphData[dataIdx] = minValueTR1;
              dataIdx = dataIdx + 1;

              // assigning max value to merged graph data array
              double maxValueTR1 = arrGraphData1[dataIdxTR1 + 2];
              arrMergedGraphData[dataIdx] = maxValueTR1;
              dataIdx = dataIdx + 1;

              // assigning count value to merged graph data array
              double countValueTR1 = arrGraphData1[dataIdxTR1 + 3];
              arrMergedGraphData[dataIdx] = countValueTR1;
              dataIdx = dataIdx + 1;

              // assigning curCount value to merged graph data array
              double curCountValueTR1 = arrGraphData1[dataIdxTR1 + 4];
              arrMergedGraphData[dataIdx] = curCountValueTR1;
              dataIdx = dataIdx + 1;
            }
            else
            {
              // assigning new graph data to array
              double graphDataTR2 = arrGraphData2[dataIdxTR2];
              arrMergedGraphData[dataIdx] = graphDataTR2;
              dataIdx = dataIdx + 1;

              // assigning min value to merged graph data array
              double minValueTR2 = arrGraphData2[dataIdxTR2 + 1];
              arrMergedGraphData[dataIdx] = minValueTR2;
              dataIdx = dataIdx + 1;

              // assigning max value to merged graph data array
              double maxValueTR2 = arrGraphData2[dataIdxTR2 + 2];
              arrMergedGraphData[dataIdx] = maxValueTR2;
              dataIdx = dataIdx + 1;

              // assigning count value to merged graph data array
              double countValueTR2 = arrGraphData2[dataIdxTR2 + 3];
              arrMergedGraphData[dataIdx] = countValueTR2;
              dataIdx = dataIdx + 1;

              // assigning curCount value to merged graph data array
              double curCountValueTR2 = arrGraphData2[dataIdxTR2 + 4];
              arrMergedGraphData[dataIdx] = curCountValueTR2;
              dataIdx = dataIdx + 1;
            }
          }
        }
        return arrMergedGraphData;
      }
      else
      {
        // array that holds merged graph data
        double[] arrMergedGraphData = new double[mergePKTDataSize];
        String prevGraphId = "";
        int dataType = -1;

        // used this idiom because if list is random access then it
        // gives best performance otherwise
        // it displays quadratic performance.
        for (int id = 0, n = arrGroupIdGraphIdVectName.size(); id < n; id++)
        {
          String strGrpIdGraphIdVectName = (String) arrGroupIdGraphIdVectName.get(id);
          String[] arrGGV = rptUtilsBean.strToArrayData(strGrpIdGraphIdVectName, "-");
          String graphId = arrGGV[1];

          // getting data index of graph in base line test run
          int dataIdxTR1 = Integer.parseInt(arrDataIdxTR1.get(id).toString());

          // getting data index of graph in second test run
          int dataIdxTR2 = Integer.parseInt(arrDataIdxTR2.get(id).toString());
          // getting data type of graph
          dataType = (Integer) arrDataType.get(id);
          if (dataType == 0 || dataType == 1 || dataType == 2)
          {
            if (dataIdxTR1 != -1 && dataIdxTR2 != -1)
            {
              // temp variables that holds value of graph
              double graphDataValueTR1 = arrGraphData1[dataIdxTR1];
              double graphDataValueTR2 = arrGraphData2[dataIdxTR2];
              // adding graph data value
              double graphDataValueMergeTR = graphDataValueTR1 + graphDataValueTR2;
              // assigning new graph data to array
              arrMergedGraphData[dataIdx] = graphDataValueMergeTR;
              dataIdx = dataIdx + 1;
            }
            else if (dataIdxTR2 != -1)
            {
              double graphDataValueTR2 = arrGraphData2[dataIdxTR2];
              arrMergedGraphData[dataIdx] = graphDataValueTR2;
              dataIdx = dataIdx + 1;
            }
            else
            {
              double graphDataValueTR1 = arrGraphData1[dataIdxTR1];
              arrMergedGraphData[dataIdx] = graphDataValueTR1;
              dataIdx = dataIdx + 1;
            }
          }
          else if (dataType == 3)
          {
            if (dataIdxTR1 != -1 && dataIdxTR2 != -1)
            {
              // temp variables that contains graph data values
              double graphDataTR1 = arrGraphData1[dataIdxTR1];
              double graphDataTR2 = arrGraphData2[dataIdxTR2];

              // adding graph data
              double graphDataMergeTR = graphDataTR1 + graphDataTR2;
              // assigning new graph data to array
              arrMergedGraphData[dataIdx] = graphDataMergeTR;
              dataIdx = dataIdx + 1;

              // getting min value from base line test run
              double minValueTR1 = arrGraphData1[dataIdxTR1 + 1];

              // getting max value from base line test run
              double maxValueTR1 = arrGraphData1[dataIdxTR1 + 2];

              // getting count value from base line test run
              double countValueTR1 = arrGraphData1[dataIdxTR1 + 3];

              // getting min value from second test run
              double minValueTR2 = arrGraphData2[dataIdxTR2 + 1];

              // getting max value from second test run
              double maxValueTR2 = arrGraphData2[dataIdxTR2 + 2];

              // getting count value from second test run
              double countValueTR2 = arrGraphData2[dataIdxTR2 + 3];

              // calculating min, max and count for merged test run
              double minValueMergeTR;
              double maxValueMergeTR;
              double countMergeTR;
              if (minValueTR1 <= minValueTR2)
              {
                minValueMergeTR = minValueTR1;
              }
              else
              {
                minValueMergeTR = minValueTR2;
              }

              if (maxValueTR1 <= maxValueTR2)
              {
                maxValueMergeTR = maxValueTR2;
              }
              else
              {
                maxValueMergeTR = maxValueTR1;
              }

              countMergeTR = (countValueTR1 + countValueTR2);

              // assigning min value to merged graph data array
              arrMergedGraphData[dataIdx] = minValueMergeTR;
              dataIdx = dataIdx + 1;

              // assigning max value to merged graph data array
              arrMergedGraphData[dataIdx] = maxValueMergeTR;
              dataIdx = dataIdx + 1;

              // assigning count value to merged graph data array
              arrMergedGraphData[dataIdx] = countMergeTR;
              dataIdx = dataIdx + 1;

            }
            else if (dataIdxTR2 != -1)
            {
              // assigning graph data value to merged graph data array
              double graphDataValueFromTR2 = arrGraphData2[dataIdxTR2];
              arrMergedGraphData[dataIdx] = graphDataValueFromTR2;
              dataIdx = dataIdx + 1;

              // getting min value and assigning to merged graph data array
              double minValueFromTR2 = arrGraphData2[dataIdxTR2 + 1];
              arrMergedGraphData[dataIdx] = minValueFromTR2;
              dataIdx = dataIdx + 1;

              // getting max value and assigning to merged graph data array
              double maxValueFromTR2 = arrGraphData2[dataIdxTR2 + 2];
              arrMergedGraphData[dataIdx] = maxValueFromTR2;
              dataIdx = dataIdx + 1;

              // getting count value and assigning to merged graph data array
              double countValueFromTR2 = arrGraphData2[dataIdxTR2 + 3];
              arrMergedGraphData[dataIdx] = countValueFromTR2;
              dataIdx = dataIdx + 1;
            }
            else
            {
              // getting graph data value and assigning to merged graph data array
              double graphDataValueFromTR1 = arrGraphData1[dataIdxTR1];
              arrMergedGraphData[dataIdx] = graphDataValueFromTR1;
              dataIdx = dataIdx + 1;

              // getting min value and assigning to merged graph data array
              double minValueFromTR1 = arrGraphData1[dataIdxTR1 + 1];
              arrMergedGraphData[dataIdx] = minValueFromTR1;
              dataIdx = dataIdx + 1;

              // getting max value and assigning to merged graph data array
              double maxValueTR1 = arrGraphData1[dataIdxTR1 + 2];
              arrMergedGraphData[dataIdx] = maxValueTR1;
              dataIdx = dataIdx + 1;

              // getting count value and assigning to merged graph data array
              double countFromTR1 = arrGraphData1[dataIdxTR1 + 3];
              arrMergedGraphData[dataIdx] = countFromTR1;
              dataIdx = dataIdx + 1;
            }
          }
          else if (dataType == 4)
          {
            if (dataIdxTR1 != -1 && dataIdxTR2 != -1)
            {
              // temp variables that holds graph data value
              double graphDataTR1 = arrGraphData1[dataIdxTR1];
              double graphDataTR2 = arrGraphData2[dataIdxTR2];
              // adding graph data value
              double graphDataMergeTR = graphDataTR1 + graphDataTR2;
              // assigning merged graph value to array
              arrMergedGraphData[dataIdx] = graphDataMergeTR;
              dataIdx = dataIdx + 1;

              // getting min value of from base line test run
              double minValueFromTR1 = arrGraphData1[dataIdxTR1 + 1];

              // getting max value of from base line test run
              double maxValueFromTR1 = arrGraphData1[dataIdxTR1 + 2];

              // getting count value of from base line test run
              double countValueFromTR1 = arrGraphData1[dataIdxTR1 + 3];

              // getting curCount value of from base line test run
              double curCountValueFromTR1 = arrGraphData1[dataIdxTR1 + 4];

              // getting min value of from second test run
              double minValueFromTR2 = arrGraphData2[dataIdxTR2 + 1];

              // getting max value of from second test run
              double maxValueFromTR2 = arrGraphData2[dataIdxTR2 + 2];

              // getting count value of from second test run
              double countValueFromTR2 = arrGraphData2[dataIdxTR2 + 3];

              // getting curCount value of from second test run
              double curCountValueFromTR2 = arrGraphData2[dataIdxTR2 + 4];

              // calculating min, max, count and curCount for new
              // merge test run
              double minValueMergeTR;
              double maxValueMergeTR;
              double countValueMergeTR;
              double curCountValueMergeTR;
              if (minValueFromTR1 <= minValueFromTR2)
              {
                minValueMergeTR = minValueFromTR1;
              }
              else
              {
                minValueMergeTR = minValueFromTR2;
              }

              if (maxValueFromTR1 <= maxValueFromTR2)
              {
                maxValueMergeTR = maxValueFromTR2;
              }
              else
              {
                maxValueMergeTR = maxValueFromTR1;
              }
              // calculating count value for merge TR
              countValueMergeTR = (countValueFromTR1 + countValueFromTR2);
              // calculating curCount value for merge TR
              curCountValueMergeTR = curCountValueFromTR1 + curCountValueFromTR2;
              // assigning min value to merged graph data array
              arrMergedGraphData[dataIdx] = minValueMergeTR;
              dataIdx = dataIdx + 1;

              // assigning max value to merged graph data array
              arrMergedGraphData[dataIdx] = maxValueMergeTR;
              dataIdx = dataIdx + 1;

              // assigning count value to merged graph data array
              arrMergedGraphData[dataIdx] = countValueMergeTR;
              dataIdx = dataIdx + 1;

              // assigning curCount value to merged graph data
              // array
              arrMergedGraphData[dataIdx] = curCountValueMergeTR;
              dataIdx = dataIdx + 1;
            }
            else if (dataIdxTR2 != -1)
            {
              // assigning graph data value to merged graph data array
              double graphDataValueFromTR2 = arrGraphData2[dataIdxTR2];
              arrMergedGraphData[dataIdx] = graphDataValueFromTR2;
              dataIdx = dataIdx + 1;

              // getting minValue and assigning data value to merged graph data array
              double minValueFromTR2 = arrGraphData2[dataIdxTR2 + 1];
              arrMergedGraphData[dataIdx] = minValueFromTR2;
              dataIdx = dataIdx + 1;

              // getting maxValue and assigning data value to merged graph data array
              double maxValueFromTR2 = arrGraphData2[dataIdxTR2 + 2];
              arrMergedGraphData[dataIdx] = maxValueFromTR2;
              dataIdx = dataIdx + 1;

              // getting count value and assigning data value to merged graph data array
              double countValueFromTR2 = arrGraphData2[dataIdxTR2 + 3];
              arrMergedGraphData[dataIdx] = countValueFromTR2;
              dataIdx = dataIdx + 1;

              // getting curCount and assigning data value to merged graph data array
              double curCountFromTR2 = arrGraphData2[dataIdxTR2 + 4];
              arrMergedGraphData[dataIdx] = curCountFromTR2;
              dataIdx = dataIdx + 1;
            }
            else
            {
              // assigning graph data value to merged graph data array
              double graphDataValueTR1 = arrGraphData1[dataIdxTR1];
              arrMergedGraphData[dataIdx] = graphDataValueTR1;
              dataIdx = dataIdx + 1;

              // getting minValue and assigning data value to merged graph data array
              double minValueFromTR1 = arrGraphData1[dataIdxTR1 + 1];
              arrMergedGraphData[dataIdx] = minValueFromTR1;
              dataIdx = dataIdx + 1;

              // getting maxValue and assigning data value to merged graph data array
              double maxValueFromTR1 = arrGraphData1[dataIdxTR1 + 2];
              arrMergedGraphData[dataIdx] = maxValueFromTR1;
              dataIdx = dataIdx + 1;

              // getting count value and assigning data value to merged graph data array
              double countValueFromTR1 = arrGraphData1[dataIdxTR1 + 3];
              arrMergedGraphData[dataIdx] = countValueFromTR1;
              dataIdx = dataIdx + 1;

              // getting curCount and assigning data value to merged graph data array
              double curCountValueFromTR1 = arrGraphData1[dataIdxTR1 + 4];
              arrMergedGraphData[dataIdx] = curCountValueFromTR1;
              dataIdx = dataIdx + 1;
            }
          }
        }
        return arrMergedGraphData;
      }
    }
    catch (Exception ex)
    {
      Log.errorLog(className, "getCalcGraphData", "", "", "Error while calculating graph data packets - " + ex);
      ex.printStackTrace();
      return null;
    }
  }

  // method to calculate graph data when number of packets are not same in both test run
  private double[] calcGraphData(double[] arrGraphData1, double[] arrGraphData2)
  {
    try
    {
      Log.debugLog(className, "calcGraphData", "", "", "Method called.");
      // data index for new merged test run graph data
      int dataIdx = 0;
      int temp = (sizeOfMsgData - 112) / 8;
      // array that will hold merged graph data
      double[] arrMergedGraphData = new double[temp];
      // if merging request for append mode
      if (mergeMode == 1)
      {
        // used this idiom because if list is random access then it gives
        // best performance otherwise
        // it displays quadratic performance.
        for (int id = 0, n = arrGroupIdGraphIdVectName.size(); id < n; id++)
        {
          String strGrpIdGraphIdVectName = (String) arrGroupIdGraphIdVectName.get(id);
          int dataType = (Integer) arrDataType.get(id);

          if (arrGraphData2 != null)
          {
            // getting data index from second TR
            int dataIdxTR = Integer.parseInt(arrDataIdxTR2.get(id).toString());

            if (dataType == 0 || dataType == 1 || dataType == 2)
            {
              if (arrlistOfGrpIdGraphIdVectNameOfTR1.contains(strGrpIdGraphIdVectName) || dataIdxTR == -1)
              {
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;
              }
              else
              {
                arrMergedGraphData[dataIdx] = arrGraphData2[dataIdxTR];
                dataIdx = dataIdx + 1;
              }
            }
            else if (dataType == 3)
            {
              if (arrlistOfGrpIdGraphIdVectNameOfTR1.contains(strGrpIdGraphIdVectName) || dataIdxTR == -1)
              {
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;

                // getting minValue
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;

                // getting maxValue
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;

                // getting count
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;
              }
              else
              {
                arrMergedGraphData[dataIdx] = arrGraphData2[dataIdxTR];
                dataIdx = dataIdx + 1;

                // getting minValue
                arrMergedGraphData[dataIdx] = arrGraphData2[dataIdxTR + 1];
                dataIdx = dataIdx + 1;

                // getting maxValue
                arrMergedGraphData[dataIdx] = arrGraphData2[dataIdxTR + 2];
                dataIdx = dataIdx + 1;

                // getting count
                arrMergedGraphData[dataIdx] = arrGraphData2[dataIdxTR + 3];
                dataIdx = dataIdx + 1;
              }
            }
            if (dataType == 4)
            {
              if (arrlistOfGrpIdGraphIdVectNameOfTR1.contains(strGrpIdGraphIdVectName) || dataIdxTR == -1)
              {
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;

                // getting minValue
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;

                // getting maxValue
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;

                // getting count
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;

                // getting curCount
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;
              }
              else
              {
                arrMergedGraphData[dataIdx] = arrGraphData2[dataIdxTR];
                dataIdx = dataIdx + 1;

                // getting minValue
                arrMergedGraphData[dataIdx] = arrGraphData2[dataIdxTR + 1];
                dataIdx = dataIdx + 1;

                // getting maxValue
                arrMergedGraphData[dataIdx] = arrGraphData2[dataIdxTR + 2];
                dataIdx = dataIdx + 1;

                // getting count
                arrMergedGraphData[dataIdx] = arrGraphData2[dataIdxTR + 3];
                dataIdx = dataIdx + 1;

                // getting curCount
                arrMergedGraphData[dataIdx] = arrGraphData2[dataIdxTR + 4];
                dataIdx = dataIdx + 1;
              }
            }
          }
          else
          {
            int dataIdxTR = Integer.parseInt(arrDataIdxTR1.get(id).toString());
            if (dataType == 0 || dataType == 1 || dataType == 2)
            {
              if (dataIdxTR != -1)
              {
                arrMergedGraphData[dataIdx] = arrGraphData1[dataIdxTR];
                dataIdx = dataIdx + 1;
              }
              else
              {
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;
              }
            }
            else if (dataType == 3)
            {
              if (dataIdxTR != -1)
              {
                arrMergedGraphData[dataIdx] = arrGraphData1[dataIdxTR];
                dataIdx = dataIdx + 1;

                // getting minValue
                arrMergedGraphData[dataIdx] = arrGraphData1[dataIdxTR + 1];
                dataIdx = dataIdx + 1;

                // getting maxValue
                arrMergedGraphData[dataIdx] = arrGraphData1[dataIdxTR + 2];
                dataIdx = dataIdx + 1;

                // getting count
                arrMergedGraphData[dataIdx] = arrGraphData1[dataIdxTR + 3];
                dataIdx = dataIdx + 1;
              }
              else
              {
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;

                // getting minValue
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;

                // getting maxValue
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;

                // getting count
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;
              }
            }
            if (dataType == 4)
            {
              if (dataIdxTR != -1)
              {
                arrMergedGraphData[dataIdx] = arrGraphData1[dataIdxTR];
                dataIdx = dataIdx + 1;

                // getting minValue
                arrMergedGraphData[dataIdx] = arrGraphData1[dataIdxTR + 1];
                dataIdx = dataIdx + 1;

                // getting maxValue
                arrMergedGraphData[dataIdx] = arrGraphData1[dataIdxTR + 2];
                dataIdx = dataIdx + 1;

                // getting count
                arrMergedGraphData[dataIdx] = arrGraphData1[dataIdxTR + 3];
                dataIdx = dataIdx + 1;

                // getting curCount
                arrMergedGraphData[dataIdx] = arrGraphData1[dataIdxTR + 4];
                dataIdx = dataIdx + 1;
              }
              else
              {
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;

                // getting minValue
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;

                // getting maxValue
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;

                // getting count
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;

                // getting curCount
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;
              }
            }
          }
        }
        return arrMergedGraphData;
      }
      else
      {
        // used this idiom because if list is random access then it gives
        // best performance otherwise it displays quadratic performance.
        for (int id = 0, n = arrGroupIdGraphIdVectName.size(); id < n; id++)
        {
          String strGrpIdGraphIdVectName = (String) arrGroupIdGraphIdVectName.get(id);
          int dataType = (Integer) arrDataType.get(id);
          // if data packet taken from TR2
          if (arrGraphData2 != null)
          {
            int dataIdxTR = Integer.parseInt(arrDataIdxTR2.get(id).toString());
            if (dataType == 0 || dataType == 1 || dataType == 2)
            {
              if (dataIdxTR != -1)
              {
                arrMergedGraphData[dataIdx] = arrGraphData2[dataIdxTR];
                dataIdx = dataIdx + 1;
              }
              else
              {
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;
              }
            }
            else if (dataType == 3)
            {
              if (dataIdxTR != -1)
              {
                arrMergedGraphData[dataIdx] = arrGraphData2[dataIdxTR];
                dataIdx = dataIdx + 1;

                // getting minValue
                arrMergedGraphData[dataIdx] = arrGraphData2[dataIdxTR + 1];
                dataIdx = dataIdx + 1;

                // getting maxValue
                arrMergedGraphData[dataIdx] = arrGraphData2[dataIdxTR + 2];
                dataIdx = dataIdx + 1;

                // getting count
                arrMergedGraphData[dataIdx] = arrGraphData2[dataIdxTR + 3];
                dataIdx = dataIdx + 1;
              }
              else
              {
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;

                // getting minValue
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;

                // getting maxValue
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;

                // getting count
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;
              }
            }
            if (dataType == 4)
            {
              if (dataIdxTR != -1)
              {
                arrMergedGraphData[dataIdx] = arrGraphData2[dataIdxTR];
                dataIdx = dataIdx + 1;

                // getting minValue
                arrMergedGraphData[dataIdx] = arrGraphData2[dataIdxTR + 1];
                dataIdx = dataIdx + 1;

                // getting maxValue
                arrMergedGraphData[dataIdx] = arrGraphData2[dataIdxTR + 2];
                dataIdx = dataIdx + 1;

                // getting count
                arrMergedGraphData[dataIdx] = arrGraphData2[dataIdxTR + 3];
                dataIdx = dataIdx + 1;

                // getting curCount
                arrMergedGraphData[dataIdx] = arrGraphData2[dataIdxTR + 4];
                dataIdx = dataIdx + 1;
              }
              else
              {
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;

                // getting minValue
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;

                // getting maxValue
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;

                // getting count
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;

                // getting curCount
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;
              }
            }
          }
          // if data packet taken from TR1
          else
          {
            int dataIdxTR = Integer.parseInt(arrDataIdxTR1.get(id).toString());
            if (dataType == 0 || dataType == 1 || dataType == 2)
            {
              if (dataIdxTR != -1)
              {
                arrMergedGraphData[dataIdx] = arrGraphData1[dataIdxTR];
                dataIdx = dataIdx + 1;
              }
              else
              {
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;
              }
            }
            else if (dataType == 3)
            {
              if (dataIdxTR != -1)
              {
                arrMergedGraphData[dataIdx] = arrGraphData1[dataIdxTR];
                dataIdx = dataIdx + 1;

                // getting minValue
                arrMergedGraphData[dataIdx] = arrGraphData1[dataIdxTR + 1];
                dataIdx = dataIdx + 1;

                // getting maxValue
                arrMergedGraphData[dataIdx] = arrGraphData1[dataIdxTR + 2];
                dataIdx = dataIdx + 1;

                // getting count
                arrMergedGraphData[dataIdx] = arrGraphData1[dataIdxTR + 3];
                dataIdx = dataIdx + 1;
              }
              else
              {
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;

                // getting minValue
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;

                // getting maxValue
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;

                // getting count
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;
              }
            }
            if (dataType == 4)
            {
              if (dataIdxTR != -1)
              {
                arrMergedGraphData[dataIdx] = arrGraphData1[dataIdxTR];
                dataIdx = dataIdx + 1;

                // getting minValue
                arrMergedGraphData[dataIdx] = arrGraphData1[dataIdxTR + 1];
                dataIdx = dataIdx + 1;

                // getting maxValue
                arrMergedGraphData[dataIdx] = arrGraphData1[dataIdxTR + 2];
                dataIdx = dataIdx + 1;

                // getting count
                arrMergedGraphData[dataIdx] = arrGraphData1[dataIdxTR + 3];
                dataIdx = dataIdx + 1;

                // getting curCount
                arrMergedGraphData[dataIdx] = arrGraphData1[dataIdxTR + 4];
                dataIdx = dataIdx + 1;
              }
              else
              {
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;

                // getting minValue
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;

                // getting maxValue
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;

                // getting count
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;

                // getting curCount
                arrMergedGraphData[dataIdx] = 0.0;
                dataIdx = dataIdx + 1;
              }
            }
          }
        }
        return arrMergedGraphData;
      }
    }
    catch (Exception ex)
    {
      Log.errorLog(className, "calcGraphData", "", "", "Error while calculating graph data packets - " + ex);
      ex.printStackTrace();
      return null;
    }
  }

  // creating data packets for rtgMessage.dat file
  public ArrayList getMergeAllPKTGraphData()
  {

    try
    {
      // array list that hold all packet merged graph data that is to be
      // returned by this method
      ArrayList arrResultedGraphData = new ArrayList<double[]>();
      // vector of all packets of base line test run number
      int packetSizeTR1 = graphNames1.getSizeOfMsgData();
      ArrayList arrGraphData1 = getRawDataByTestRunNum(testRunNum1, packetSizeTR1, startElapsedTimeTR1, endElapsedTimeTR1);
      // if any exception comes while reading raw data from TestRun1 rtgMessage.dat file
      if (arrGraphData1.contains("Error"))
      {
        arrResultedGraphData.add("Error");
        arrResultedGraphData.add(arrGraphData1.get(1).toString());
        return arrResultedGraphData;
      }
      if (arrGraphData1 == null || arrGraphData1.size() <= 2)
      {
        arrResultedGraphData.add("Error");
        arrResultedGraphData.add("Can not merge because rtgMessage.dat file for testrun " + testRunNum1 + " may not be correct.");
        return arrResultedGraphData;
      }

      // vector of all packets of second test run number
      int packetSizeTR2 = graphNames2.getSizeOfMsgData();
      ArrayList arrGraphData2 = getRawDataByTestRunNum(testRunNum2, packetSizeTR2, startElapsedTimeTR2, endElapsedTimeTR2);
      // if any exception comes while reading raw data from TestRun2 rtgMessage.dat file
      if (arrGraphData2.contains("Error"))
      {
        arrResultedGraphData.add("Error");
        arrResultedGraphData.add(arrGraphData2.get(1).toString());
        return arrResultedGraphData;
      }

      if (arrGraphData2 == null || arrGraphData2.size() <= 2)
      {
        arrResultedGraphData.add("Error");
        arrResultedGraphData.add("Can not merge because rtgMessage.dat file for testrun " + testRunNum2 + " may not be correct.");
        return arrResultedGraphData;
      }

      // calculating size of packets including start and end packet
      int arrTR1Size = arrGraphData1.size();
      int arrTR2Size = arrGraphData2.size();
      // used for calculating total packets in rtgMessage.dat file for merge test run
      long totalPkts = 0;
      if (arrTR1Size <= arrTR2Size)
        totalPkts = arrTR2Size;
      else
        totalPkts = arrTR1Size;

      // calculating data packet size
      int dataPktSize = (sizeOfMsgData - 112) / 8;
      double[] arrResult = new double[dataPktSize];
      // total packets are calculated as maximum packets from both test
      // run
      for (int pktNum = 0; pktNum < totalPkts; pktNum++)
      {
        // if number of packets of base line test run is less than
        // number of packets of second test run
        if (pktNum < arrTR2Size && pktNum < arrTR1Size)
        {
          // reading packet by packet graph data from vector
          double[] arrGraphRawDataTR1 = (double[]) arrGraphData1.get(pktNum);
          double[] arrGraphRawDataTR2 = (double[]) arrGraphData2.get(pktNum);
          // returns merged graph data packet
          arrResult = getCalcGraphData(arrGraphRawDataTR1, arrGraphRawDataTR2);
        }
        else if (pktNum < arrTR1Size)
        {
          // reading packet by packet from vector of second test run
          double[] arrGraphData = (double[]) arrGraphData1.get(pktNum);

          // returns merged graph data packet
          arrResult = calcGraphData(arrGraphData, null);
        }
        else if (pktNum < arrTR2Size)
        {
          // reading packet by packet from vector of second test run
          double[] arrGraphData = (double[]) arrGraphData2.get(pktNum);
          // returns merged graph data packet
          arrResult = calcGraphData(null, arrGraphData);
        }

        // copy this array to another temp array
        double[] arrTemp = new double[arrResult.length];
        System.arraycopy(arrResult, 0, arrTemp, 0, arrResult.length);
        // adding array to arraylist
        arrResultedGraphData.add(arrTemp);
      }
      // free memory from global arraylists
      // used to prevent heap memory leaks
      arrAllInfoAbtTR1 = null;
      arrAllInfoAbtTR2 = null;
      arrGroupIdGraphIdVectName = null;
      arrDataType = null;
      arrDataIdxTR1 = null;
      arrDataIdxTR2 = null;
      return arrResultedGraphData;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getMergeAllPKTGraphData", "", "", "Exception - ", ex);
      ex.printStackTrace();
      ArrayList err = new ArrayList();
      err.add("Error");
      err.add(ex.toString());
      return err;
    }
  }

  // creating start or end packet for rtgMessage.dat by passing opcode
  // 0 for start packet and 2 for end packet
  public double[] getStartOrEndPacketByOpcode(int opcode)
  {
    try
    {
      interval = getInterval();
      int count = 0;
      double[] arrHeader = getHeaderVer2(opcode, mergeNumTestRun, interval, 0);
      double[] arrTemp = new double[(sizeOfMsgData - 112) / 8];
      double[] arrResulted = new double[sizeOfMsgData / 8];

      if (arrHeader != null)
      {
        for (int i = 0, n = arrHeader.length; i < n; i++)
        {
          arrResulted[i] = arrHeader[i];
          count = i;
        }
        for (int i = 0, n = arrTemp.length; i < n; i++)
        {
          arrResulted[count] = 0.0;
          count++;
        }
        return arrResulted;
      }
      else
      {
        Log.errorLog(className, "getEndPacket", "", "", "unable to getting header for merge rtgMessage.dat file. ");
        return null;
      }
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getEndPacket", "", "", "Exception - ", ex);
      ex.printStackTrace();
      return null;
    }
  }

  // writing rtgMessage.dat file
  public ArrayList writeRTGMessageFile()
  {
    ArrayList arrRTGStatus = new ArrayList();
    try
    {
      Log.debugLog(className, "writeRTGMessageFile", "", "", "Method called");
      // getting rtgMessage.dat file path
      final String rtgFilePath = mergeTRNumberDirectoryPath + "/rtgMessage.dat";

      // used to change permisson for rtgMessage.dat file
      String strCmd = "chown";
      String strArg = loggedUserName + ".netstorm -R " + rtgFilePath;
      CmdExec cmdExec = new CmdExec();

      if (!rtgFilePath.equals(""))
      {
        File file = new File(rtgFilePath);
        // creating instance of FileChannel class
        FileChannel out = new FileOutputStream(file).getChannel();
        // changing file permisson
        Vector vecCmdOutput = cmdExec.getResultByCommand(strCmd, strArg, CmdExec.SYSTEM_CMD, loggedUserName, "root");
        // getting start packet
        double[] arrStartPacket = getStartOrEndPacketByOpcode(0);
        // if any error comes while making start packet
        if (arrStartPacket == null)
        {
          arrRTGStatus.add("Error");
          arrRTGStatus.add("Can not merge because unable to create header part of rtgMessage.dat file.");
          return arrRTGStatus;
        }

        // getting data packets
        ArrayList arrDataPKTs = getMergeAllPKTGraphData();
        // if any error comes while making data packets for rtgMessage.dat file
        if (arrDataPKTs.contains("Error"))
        {
          arrRTGStatus.add("Error");
          arrRTGStatus.add(arrDataPKTs.get(1).toString());
          return arrRTGStatus;
        }
        if (arrDataPKTs == null)
        {
          arrRTGStatus.add("Error");
          arrRTGStatus.add("Can not merge because unable to get data packets.");
          return arrRTGStatus;
        }

        // getting end packet
        double[] arrEndPacket = getStartOrEndPacketByOpcode(2);
        if (arrEndPacket == null)
        {
          arrRTGStatus.add("Error");
          arrRTGStatus.add("Can not merge because unable to get end packet for rtgMessage.dat file.");
          return arrRTGStatus;
        }
        int sizeOfMergedDataPKT = sizeOfMsgData / 8;
        double[] arrMergedGraphData = new double[sizeOfMergedDataPKT];

        ByteBuffer bb = ByteBuffer.allocate(8); // allocates 8 bytes
        bb.order(ByteOrder.LITTLE_ENDIAN); // setting byte order
        // writting start packet
        // used this idiom because if list is random access then it
        // gives best performance otherwise
        // it displays quadratic performance.
        for (int a = 0, n = arrStartPacket.length; a < n; a++)
        {
          bb.putDouble(arrStartPacket[a]);
          bb.flip();
          out.write(bb);
          bb.clear();
        }
        // writting data packets
        for (int i = 0, n = arrDataPKTs.size(); i < n; i++)
        {
          double[] arrHeader = getHeaderVer2(1, mergeNumTestRun, interval, i + 1);
          for (int j = 0, m = arrHeader.length; j < m; j++)
          {
            bb.putDouble(arrHeader[j]);
            bb.flip();
            out.write(bb);
            bb.clear();
          }
          double[] arrDataPkt = (double[]) arrDataPKTs.get(i);
          for (int j = 0, m = arrDataPkt.length; j < m; j++)
          {
            bb.putDouble(arrDataPkt[j]);
            bb.flip();
            out.write(bb);
            bb.clear();
          }
        }
        // wriiting end packet
        for (int a = 0, n = arrEndPacket.length; a < n; a++)
        {
          bb.putDouble(arrEndPacket[a]);
          bb.flip();
          out.write(bb);
          bb.clear();
        }
        out.force(true);
        out.close();
        // if rtgMessage.dat file written successfully.
        arrRTGStatus.add("Success");
        return arrRTGStatus;
      }
      else
      {
        Log.errorLog(className, "writeRTGMessageFile", "", "", "unable to write rtgMessage.dat file, testrun directory not found. ");
        arrRTGStatus.add("Error");
        arrRTGStatus.add("rtgMessage.dat file is not written, testrun directory not found.");
        return arrRTGStatus;
      }
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "writeRTGMessageFile", "", "", "Exception - ", ex);
      ex.printStackTrace();
      // if any exception comes, returns an error message with description
      arrRTGStatus.add("Error");
      arrRTGStatus.add(ex);
      return arrRTGStatus;
    }
  }

  // returns summary.top file content
  public String getSummury_TopLine()
  {
    try
    {
      Log.debugLog(className, "getSummury_TopLine", "", "", "Method called.");

      String summary_TopLine = ""; // use for summary.top file line
      String[] arrSummaryLine1 = null;
      String[] arrSummaryLine2 = null;
      int wanEnv1 = -1; // used for WAN environment
      int wanEnv2 = -1; // used for WAN environment
      int rptLevel1 = -1; // used for Report Level
      int rptLevel2 = -1; // used for Report Level
      String testRunDuration1 = ""; // used for test duration time
      String testRunDuration2 = ""; // used for test duration time
      int numConnection1 = -1; // used for number of connections
      int numConnection2 = -1; // used for number of connections
      // Note :- We would take start date time from base line test run
      String strTestRunStartDateTime = ""; // used for start date time.
      // used for scenarion name with project and sub - project name (if
      // present)
      String projSubProScenarioName = "";
      // getting summary.top file of baseline test run
      String summary_TopFile1 = GraphNameUtils.getSummary_TopFile(graphNames1.getTestRun());
      if (summary_TopFile1.startsWith("Error"))
        return summary_TopFile1;

      if (summary_TopFile1 != null && !summary_TopFile1.equals(""))
      {
        // converting string to string array for parsing
        arrSummaryLine1 = rptUtilsBean.strToArrayData(summary_TopFile1, "|");
        // reading projSubProScenarioName from baseline testrun
        projSubProScenarioName = arrSummaryLine1[1];
        // reading wan_env value
        // we will check if wan_env is on in any test run then its value
        // would be on in resulted test run
        // on --> 1 and off --> 0
        wanEnv1 = Integer.parseInt(arrSummaryLine1[10]);
        // in merge testrun, report level would be max in first and
        // second testrun
        rptLevel1 = Integer.parseInt(arrSummaryLine1[11]);
        // test run duration would be max test run duration time
        testRunDuration1 = arrSummaryLine1[14];
        // reading testRunStartDateTime from base line testrun
        strTestRunStartDateTime = arrSummaryLine1[2];
        // getting number of connections in base line test run
        numConnection1 = Integer.parseInt(arrSummaryLine1[15]);
      }
      else
      {
        Log.errorLog(className, "getSummury_TopLine", "", "", "summary_TopFile1 = " + summary_TopFile1);
        return "";
      }
      // getting summary.top file of second testrun number
      String summary_TopFile2 = GraphNameUtils.getSummary_TopFile(graphNames2.getTestRun());
      if (summary_TopFile2.startsWith("Error"))
        return summary_TopFile2;

      if (summary_TopFile2 != null && !summary_TopFile2.equals(""))
      {
        // converting string to string array for parsing
        arrSummaryLine2 = rptUtilsBean.strToArrayData(summary_TopFile2, "|");
        // used for getting WAN environment flag value from second test
        // run number
        wanEnv2 = Integer.parseInt(arrSummaryLine2[10]);
        // used for getting report level from second test run number
        rptLevel2 = Integer.parseInt(arrSummaryLine2[11]);
        // used for getting test run duration time
        testRunDuration2 = arrSummaryLine2[14];
        // getting total number of connections from second test run
        // number
        numConnection2 = Integer.parseInt(arrSummaryLine2[15]);
      }
      else
      {
        Log.errorLog(className, "getSummury_TopLine", "", "", "summary_TopFile2 = " + summary_TopFile2);
        return "";
      }

      String wan_Env = "";
      if (wanEnv1 != 0 || wanEnv2 != 0)
        wan_Env = "1";
      else
        wan_Env = "0";

      String rptLevel = "";
      if (rptLevel1 <= rptLevel2)
        rptLevel = "" + rptLevel2;
      else
        rptLevel = "" + rptLevel1;

      long startElapsedTimeTR1InMS = rptUtilsBean.convStrToMilliSec(startElapsedTimeTR1);
      long endElapsedTimeTR1InMS = rptUtilsBean.convStrToMilliSec(endElapsedTimeTR1);
      long durationTR1 = endElapsedTimeTR1InMS - startElapsedTimeTR1InMS;

      long startElapsedTimeTR2InMS = rptUtilsBean.convStrToMilliSec(startElapsedTimeTR2);
      long endElapsedTimeTR2InMS = rptUtilsBean.convStrToMilliSec(endElapsedTimeTR2);
      long durationTR2 = endElapsedTimeTR2InMS - startElapsedTimeTR2InMS;

      long mergeTRDuration = 0;

      if (durationTR1 <= durationTR2)
        mergeTRDuration = durationTR2;
      else
        mergeTRDuration = durationTR1;

      String mergeTestRunDuration = "00:00:00";
      mergeTestRunDuration = rptUtilsBean.convMilliSecToStr(mergeTRDuration);

      Log.debugLog(className, "getSummury_TopLine", "", "", "Duration of merge test run = " + mergeTestRunDuration);

      // number of users/connections would be sum of users/connection of
      // both testrun
      String strConnectionNum = "";

      int numConnection = 0;
      // handle case for bug ID - 4231
      if (mergeMode == 1)
        numConnection = numConnection1;
      else
        numConnection = numConnection1 + numConnection2;

      String testName = "";
      // handle case for bug Id - 4232
      // Bug 4232 - Merge Test Run : Merge Test Run Name should show the Merging type applied in Test Run Window
      if (mergeMode == 1)
        testName = "Merged_TR" + testRunNum1 + "_A_TR" + testRunNum2;
      else
        testName = "Merged_TR" + testRunNum1 + "_U_TR" + testRunNum2;

      strConnectionNum = "" + numConnection;
      summary_TopLine = mergeNumTestRun + "|" + projSubProScenarioName + "|" + strTestRunStartDateTime + "|";
      summary_TopLine = summary_TopLine + "Y" + "|" + "N" + "|" + "Y" + "|" + "N" + "|" + "N" + "|" + "N" + "|" + "N" + "|";
      summary_TopLine = summary_TopLine + wan_Env + "|" + rptLevel + "|" + testName + "|" + "W" + "|" + mergeTestRunDuration + "|" + strConnectionNum;
      return summary_TopLine;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getSummury_TopLine", "", "", "Exception - ", ex);
      ex.printStackTrace();
      return "";
    }
  }

  // writting summary.top file
  public ArrayList writeSummary_TopFile()
  {
    ArrayList arrSummary_TopFileStatus = new ArrayList();
    try
    {
      Log.debugLog(className, "writeSummary_TopFile", "", "", "Method called.");
      // getting summary.top file path
      String summaryFilePath = mergeTRNumberDirectoryPath + "/summary.top";

      String strCmd = "chown";
      String strArg = loggedUserName + ".netstorm -R " + summaryFilePath;
      CmdExec cmdExec = new CmdExec();

      if (!summaryFilePath.equals(""))
      {
        File file = new File(summaryFilePath);
        // creating instance of file writer class
        FileWriter summaryFileStream = new FileWriter(file);
        Vector vecCmdOutput = cmdExec.getResultByCommand(strCmd, strArg, CmdExec.SYSTEM_CMD, loggedUserName, "root");
        // creating instance of buffered writer class
        BufferedWriter out = new BufferedWriter(summaryFileStream);
        // getting summary.top file line
        String strSummary_TopFile = getSummury_TopLine();
        if (strSummary_TopFile.startsWith("Error"))
        {
          String[] errDescription = rptUtilsBean.strToArrayData(strSummary_TopFile, "|");
          arrSummary_TopFileStatus.add("Error");
          arrSummary_TopFileStatus.add(errDescription[1]);
          return arrSummary_TopFileStatus;
        }
        if (strSummary_TopFile.equals(""))
        {
          arrSummary_TopFileStatus.add("Error");
          arrSummary_TopFileStatus.add("For Merge test run, summary.top file is not written.");
          return arrSummary_TopFileStatus;
        }
        // writting summary.top line into file
        out.write(strSummary_TopFile);
        // Close the stream
        out.flush();
        out.close();
        summaryFileStream.close();
        arrSummary_TopFileStatus.add("Success");
        return arrSummary_TopFileStatus;
      }
      else
      {
        Log.errorLog(className, "writeSummary_TopFile", "", "", "testrun directory not found, summary.top not written.");
        arrSummary_TopFileStatus.add("Error");
        arrSummary_TopFileStatus.add("summary.top can not be written, testrun directory not found.");
        return arrSummary_TopFileStatus;
      }
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "writeSummary_TopFile", "", "", "Exception - ", ex);
      ex.printStackTrace();
      arrSummary_TopFileStatus.add("Error");
      arrSummary_TopFileStatus.add(ex);
      return arrSummary_TopFileStatus;
    }
  }

  // Returns LinkedHashMap of group and graph lines for merge test run
  // returns a map of all group and graph lines of merge test run
  // returns all merged group and graph lines
  public LinkedHashMap getMergedGroupGraphLines()
  {
    try
    {
      // used for all group and graph line of merge test run number
      LinkedHashMap hmGroupGraphLines = new LinkedHashMap();

      /*****************************************************************************************
       * * * Algorithm Used * * * * Note: - We need take the data structure LinkedHashMap because of we do not need to int the key.
       * 
       * We would put key as if group line comes in testrun.gdf then we would put groupId as key and group line as their value
       * 
       * if graph line comes then we would put GroupId_GraphId as key and graph line as their value
       *******************************************************************************************/

      LinkedHashMap hmGrpGraphLinesTR1 = (LinkedHashMap) arrAllInfoAbtTR1.get(3);
      LinkedHashMap hmGrpGraphLinesTR2 = (LinkedHashMap) arrAllInfoAbtTR2.get(3);
      if (hmGrpGraphLinesTR1 == null)
      {
        Log.errorLog(className, "getGroupGraphLines", "", "", "error while getting group and graph lines from base line. -  ");
        LinkedHashMap hmResult = new LinkedHashMap();
        hmResult.put("Error", "For TestRun " + testRunNum1 + "testrun.gdf may be corrupted.");
        return hmResult;
      }

      if (hmGrpGraphLinesTR2 == null)
      {
        Log.errorLog(className, "getGroupGraphLines", "", "", "error while getting group and graph lines from base line or second test run. -  ");
        LinkedHashMap hmResult = new LinkedHashMap();
        hmResult.put("Error", "For TestRun " + testRunNum2 + "testrun.gdf may be corrupted.");
        return hmResult;
      }

      for (int i = 0; i < arrGroupIdGraphIdVectName.size(); i++)
      {
        String ggv = (String) arrGroupIdGraphIdVectName.get(i);
        String[] arrGGV = rptUtilsBean.strToArrayData(ggv, "-");
        String grpId = arrGGV[0];
        String graphId = arrGGV[1];

        if (hmGrpGraphLinesTR1.containsKey(grpId) && hmGrpGraphLinesTR2.containsKey(grpId))
        {
          /***********************************************************************************
           ******** Algorithm *********************************************** for same group id, if group name is diffrent, then we can not allow to merge and for same group id, if group type is diffrent
           * then we can not allow to merge
           ************************************************************************************/

          String groupLineTR1 = (String) hmGrpGraphLinesTR1.get(grpId);
          String groupLineTR2 = (String) hmGrpGraphLinesTR2.get(grpId);
          String[] arrGroupLine1 = rptUtilsBean.strToArrayData(groupLineTR1, "|");
          String[] arrGroupLine2 = rptUtilsBean.strToArrayData(groupLineTR2, "|");

          String groupNameInTR1 = arrGroupLine1[1];
          String groupTypeInTR1 = arrGroupLine1[3];

          String groupNameInTR2 = arrGroupLine2[1];
          String groupTypeInTR2 = arrGroupLine2[3];

          if (groupNameInTR1.equals(groupNameInTR2))
          {
            if (groupTypeInTR1.equals(groupTypeInTR2))
            {
              String groupLine = (String) hmGrpGraphLinesTR1.get(grpId);
              if (!hmGroupGraphLines.containsKey(grpId))
              {
                hmGroupGraphLines.put(grpId, groupLine);
              }
            }
            else
            {
              LinkedHashMap hmResult = new LinkedHashMap();
              hmResult.put("Error", "Group " + groupNameInTR1 + " is different type in testruns. \\n It is " + groupTypeInTR1 + " in " + testRunNum1 + "\\n It is " + groupTypeInTR2 + " in " + testRunNum2);
              return hmResult;
            }
          }
          else
          {
            LinkedHashMap hmResult = new LinkedHashMap();
            hmResult.put("Error", "Group names are different for group id " + grpId);
            return hmResult;
          }
        }
        else if (hmGrpGraphLinesTR2.containsKey(grpId))
        {
          String groupLine = (String) hmGrpGraphLinesTR2.get(grpId);
          if (!hmGroupGraphLines.containsKey(grpId))
          {
            hmGroupGraphLines.put(grpId, groupLine);
          }
        }
        else
        {
          String groupLine = (String) hmGrpGraphLinesTR1.get(grpId);
          if (!hmGroupGraphLines.containsKey(grpId))
          {
            hmGroupGraphLines.put(grpId, groupLine);
          }
        }

        if (hmGrpGraphLinesTR1.containsKey(grpId + "_" + graphId) && hmGrpGraphLinesTR2.containsKey(grpId + "_" + graphId))
        {
          String graphLine = (String) hmGrpGraphLinesTR1.get(grpId + "_" + graphId);
          if (!hmGroupGraphLines.containsKey(grpId + "_" + graphId))
          {
            hmGroupGraphLines.put(grpId + "_" + graphId, graphLine);
          }
        }
        else if (hmGrpGraphLinesTR2.containsKey(grpId + "_" + graphId))
        {
          String graphLine = (String) hmGrpGraphLinesTR2.get(grpId + "_" + graphId);
          if (!hmGroupGraphLines.containsKey(grpId + "_" + graphId))
          {
            hmGroupGraphLines.put(grpId + "_" + graphId, graphLine);
          }
        }
        else
        {
          String graphLine = (String) hmGrpGraphLinesTR1.get(grpId + "_" + graphId);
          if (!hmGroupGraphLines.containsKey(grpId + "_" + graphId))
          {
            hmGroupGraphLines.put(grpId + "_" + graphId, graphLine);
          }
        }
      }
      return hmGroupGraphLines;

    }
    catch (Exception ex)
    {
      Log.errorLog(className, "getGroupGraphLines", "", "", "exception while making merged group and graph line -  " + ex);
      ex.printStackTrace();
      LinkedHashMap hmResult = new LinkedHashMap();
      hmResult.put("Error", ex.toString());
      return hmResult;
    }
  }

  // returns merge testrun.gdf array list that contains group and graph line
  // with their vector names
  public ArrayList getMergedTestRunGDF()
  {
    try
    {
      Log.debugLog(className, "getMergedTestRunGDF", "", "", "Method called");
      // use for group id of given testrun number
      ArrayList arrGrpId1 = graphNames1.getUniqueGroupIds();
      ArrayList arrGrpId2 = graphNames2.getUniqueGroupIds();

      // array list that will hold merge testrun.gdf file group and graph
      // lines with their vectors (if present)
      ArrayList mergeGDF = new ArrayList();
      // used for groupName
      String groupName = "";
      // getting all group and graph lines of merge testrun.gdf file
      LinkedHashMap groupGraphLines = getMergedGroupGraphLines();
      int NumOfGraphVectors = 0;
      // used for number of vector of group if group is vector type
      int totalGroupVector = 0;
      if (groupGraphLines == null)
      {
        Log.errorLog(className, "getMergedTestRunGDF", "", "", "Error while getting group and graph line for merge test run number.");
        ArrayList arrResult = new ArrayList();
        arrResult.add("Error");
        arrResult.add("Error while getting group and graph lines.");
        return arrResult;
      }
      if (groupGraphLines.containsKey("Error"))
      {
        // returns error description
        String strErrorDescription = (String) groupGraphLines.get("Error");
        ArrayList arrResult = new ArrayList();
        arrResult.add("Error");
        arrResult.add(strErrorDescription);
        return arrResult;
      }

      Iterator iterator = groupGraphLines.keySet().iterator();
      while (iterator.hasNext())
      {
        // used for holding all vector names for perticular graph
        ArrayList arrGroupVectorNames = new ArrayList();
        arrGroupVectorNames.clear();

        String grpId_GraphId = iterator.next().toString();
        // used for group and graph id
        int groupId = -1;
        int graphId = -1;
        // convert string to array for getting group and graph id
        String[] arrGrpIdGraphId = rptUtilsBean.strToArrayData(grpId_GraphId, "_");
        if (arrGrpIdGraphId.length == 2)
        {
          groupId = Integer.parseInt(arrGrpIdGraphId[0]);
          graphId = Integer.parseInt(arrGrpIdGraphId[1]);
        }
        else
        {
          groupId = Integer.parseInt(arrGrpIdGraphId[0]);
        }
        // getting gdf line of merge test run
        // it may be group line or graph line
        String gdfLine = groupGraphLines.get(grpId_GraphId).toString();
        if (gdfLine.startsWith("Group"))
        {
          // getting group line
          String groupLine = gdfLine;
          // converting string to string array
          String[] arrGroupLine = rptUtilsBean.strToArrayData(groupLine, "|");
          // getting group type
          groupType = arrGroupLine[3];
          // getting groupName
          groupName = arrGroupLine[1];

          if (prevGroupType.equals("vector") && totalGroupVector != 0)
          {
            nextDataIdx = nextDataIdx + (nextGraphDataIdx * (totalGroupVector - 1));
            sizeOfMsgData = nextDataIdx;
            prevGroupType = "";
            nextGraphDataIdx = 0;
          }
          // assigning total graphs for perticular group that is
          // changed after merging
          int totalGraphs = getTotalGraphs(groupId);
          arrGroupLine[4] = "" + totalGraphs;

          // checking if group type is scalar then it will not have no
          // vector list
          if (groupType.equals("scalar"))
          {
            String scalarGroupLine = rptUtilsBean.strArrayToStr(arrGroupLine, "|");
            mergeGDF.add("\n" + scalarGroupLine + "\n");
            mergeGDF.add("\n");
          }
          // checking if group type type is vector then need to add
          // its all vector to array list
          else if (groupType.equals("vector"))
          {
            if (arrGrpId1.contains(groupId) && arrGrpId2.contains(groupId))
            {
              // getting all vector names
              String[] arrVectNamesTR1 = graphNames1.getNameOfGroupIndicesByGroupId(groupId);
              String[] arrVectNamesTR2 = graphNames2.getNameOfGroupIndicesByGroupId(groupId);
              // adding all vector of group
              for (int vect = 0; vect < arrVectNamesTR1.length; vect++)
              {
                arrGroupVectorNames.add(arrVectNamesTR1[vect]);
              }
              // adding vector of specified Group from second test
              // run which is not in base line test run
              for (int vect = 0; vect < arrVectNamesTR2.length; vect++)
              {
                if (!arrGroupVectorNames.contains(arrVectNamesTR2[vect]))
                {
                  arrGroupVectorNames.add(arrVectNamesTR2[vect]);
                }
              }
            }
            else if (arrGrpId1.contains(groupId))
            {
              String[] arrVectNamesTR1 = graphNames1.getNameOfGroupIndicesByGroupId(groupId);
              for (int vect = 0; vect < arrVectNamesTR1.length; vect++)
              {
                arrGroupVectorNames.add(arrVectNamesTR1[vect]);
              }
            }
            else if (arrGrpId2.contains(groupId))
            {
              String[] arrVectNamesTR2 = graphNames2.getNameOfGroupIndicesByGroupId(groupId);
              for (int vect = 0; vect < arrVectNamesTR2.length; vect++)
              {
                arrGroupVectorNames.add(arrVectNamesTR2[vect]);
              }
            }
            if (arrGroupVectorNames != null)
            {
              totalGroupVector = arrGroupVectorNames.size();
              arrGroupLine[5] = "" + totalGroupVector;
              String vectorGroupLine = rptUtilsBean.strArrayToStr(arrGroupLine, "|");
              mergeGDF.add("\n" + vectorGroupLine + "\n");
              mergeGDF.add("\n");

              for (int j = 0; j < arrGroupVectorNames.size(); j++)
              {
                mergeGDF.add(arrGroupVectorNames.get(j) + "\n");
              }
            }
          }
        }
        else if (gdfLine.startsWith("Graph"))
        {
          String graphLine = gdfLine;
          String[] arrGraphLine = rptUtilsBean.strToArrayData(graphLine, "|");
          String graphType = arrGraphLine[3];
          String graphDataType = arrGraphLine[4];
          // calculating data type size
          int dataTypeSize = getDataTypeSize(graphDataType);
          ArrayList arrVectNames = new ArrayList();

          if (graphType.equals("vector") && (graphId != -1))
          {
            if (arrGrpId1.contains(groupId) && arrGrpId2.contains(groupId))
            {
              String[] arrGraphVectNames1 = graphNames1.getNameOfIndicesByGroupIdAndGraphId(groupId, graphId);
              String[] arrGraphVectNames2 = graphNames2.getNameOfIndicesByGroupIdAndGraphId(groupId, graphId);
              // adding graph line if graph is vector type
              for (int k = 0; k < arrGraphVectNames1.length; k++)
              {
                // adding vector names in array list
                arrVectNames.add(arrGraphVectNames1[k]);
              }

              for (int k = 0; k < arrGraphVectNames2.length; k++)
              {
                // adding vector names in array list which is
                // not present in first testrun.gdf
                if (!arrVectNames.contains(arrGraphVectNames2[k]))
                {
                  arrVectNames.add(arrGraphVectNames2[k]);
                }
              }
            }
            else if (arrGrpId1.contains(groupId))
            {
              String[] arrGraphVectNames1 = graphNames1.getNameOfIndicesByGroupIdAndGraphId(groupId, graphId);
              // adding graph line if graph is vector type
              for (int k = 0; k < arrGraphVectNames1.length; k++)
              {
                // adding vector names in array list
                arrVectNames.add(arrGraphVectNames1[k]);
              }
            }
            else if (arrGrpId2.contains(groupId))
            {
              String[] arrGraphVectNames2 = graphNames2.getNameOfIndicesByGroupIdAndGraphId(groupId, graphId);
              // adding graph line if graph is vector type
              for (int k = 0; k < arrGraphVectNames2.length; k++)
              {
                // adding vector names in array list
                arrVectNames.add(arrGraphVectNames2[k]);
              }
            }

            if (previousDataIdx == 0)
            {
              previousDataIdx = Integer.parseInt(arrGraphLine[5]);
              arrGraphLine[5] = "" + previousDataIdx;
              nextDataIdx = previousDataIdx;
              sizeOfMsgData = nextDataIdx;
            }
            else if (previousDataIdx != 0)
            {
              arrGraphLine[5] = "" + nextDataIdx;
              nextDataIdx = nextDataIdx + dataTypeSize;
              sizeOfMsgData = nextDataIdx;
            }
            NumOfGraphVectors = arrVectNames.size();
            arrGraphLine[7] = "" + NumOfGraphVectors;
            if (groupType.equals("vector"))
            {
              nextGraphDataIdx = nextGraphDataIdx + dataTypeSize;
            }

            String mergedGraphLine = rptUtilsBean.strArrayToStr(arrGraphLine, "|");
            mergeGDF.add(mergedGraphLine + "\n");
            for (int j = 0; j < NumOfGraphVectors; j++)
            {
              String vectName = (String) arrVectNames.get(j);
              mergeGDF.add(vectName + "\n");
            }
            // because one time already added data type size
            nextDataIdx = nextDataIdx + ((NumOfGraphVectors - 1) * dataTypeSize);
          }
          else if (graphType.equals("scalar"))
          {
            arrGraphLine = rptUtilsBean.strToArrayData(graphLine, "|");
            if (previousDataIdx == 0)
            {
              previousDataIdx = Integer.parseInt(arrGraphLine[5]);
              arrGraphLine[5] = "" + previousDataIdx;
              nextDataIdx = previousDataIdx + dataTypeSize;
            }
            else if (previousDataIdx != 0)
            {
              arrGraphLine[5] = "" + nextDataIdx;
              nextDataIdx = nextDataIdx + dataTypeSize;
            }

            if (groupType.equals("vector"))
            {
              nextGraphDataIdx = nextGraphDataIdx + dataTypeSize;
            }
            String mergedGraphLine = rptUtilsBean.strArrayToStr(arrGraphLine, "|");
            mergeGDF.add(mergedGraphLine + "\n");
          }
          prevGroupType = groupType;
        }
      }

      if (groupType.equals("vector"))
      {
        nextDataIdx = nextDataIdx + (totalGroupVector - 1) * nextGraphDataIdx;
      }
      sizeOfMsgData = nextDataIdx;
      return mergeGDF;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getMergedTestRunGDF", "", "", "Exception - ", ex);
      ex.printStackTrace();
      ArrayList arrResult = new ArrayList();
      arrResult.add("Error");
      arrResult.add(ex);
      return arrResult;
    }
  }

  // writing testrun.gdf for merge test run number
  public ArrayList writeMergeTestRunGDF()
  {
    // used for gdf file status
    ArrayList gdfFileWrittingStatus = new ArrayList();
    try
    {
      Log.debugLog(className, "writeMergeTestRunGDF", "", "", "method called.");

      // used to initialized msg data index in graph line of merge
      // testrun's testrun.gdf
      nextDataIdx = 0;
      previousDataIdx = 0;
      // getting path of testrun.gdf file
      String testRunPath = mergeTRNumberDirectoryPath;
      String testRunGDFFilePath = testRunPath + "/testrun.gdf";
      File gdfFilePath = new File(testRunGDFFilePath);

      String strCmd = "chown";
      String strArg = loggedUserName + ".netstorm -R " + testRunGDFFilePath;
      CmdExec cmdExec = new CmdExec();

      if (!testRunGDFFilePath.equals(""))
      {
        // getting array list of merge testrun.gdf file
        ArrayList arrGDF = getMergedTestRunGDF();
        if (arrGDF != null && arrGDF.size() > 0)
        {
          if (arrGDF.contains("Error"))
          {
            gdfFileWrittingStatus.add("Error");
            gdfFileWrittingStatus.add(arrGDF.get(1).toString());
            return gdfFileWrittingStatus;
          }
        }
        // if (rptUtilsBean.changeFilePerm(file.getAbsolutePath(), loggedUserName, userGroup, "662"))

        FileWriter gdfFileStream = new FileWriter(gdfFilePath);
        // creting instance of buffered writer
        BufferedWriter out = new BufferedWriter(gdfFileStream);
        // getting info line of merged testrun number
        String infoline = getInfoLine();
        Vector vecCmdOutput = cmdExec.getResultByCommand(strCmd, strArg, CmdExec.SYSTEM_CMD, loggedUserName, "root");
        // writing testrun.gdf file info line
        out.write(infoline);
        // writting all group and graph line in merge testrun.gdf
        // file
        for (int i = 0; i < arrGDF.size(); i++)
        {
          String gdfLine = (String) arrGDF.get(i);
          out.write(gdfLine);
        }
        out.flush();
        out.close();
        gdfFileStream.close();
        Log.debugLog(className, "writeMergeTestRunGDF", "", "", "testrun.gdf file written successfully.");
        gdfFileWrittingStatus.add("Success");
        return gdfFileWrittingStatus;

      }
      else
      {
        Log.errorLog(className, "writeMergeTestRunGDF", "", "", "test run directory not found for testrun.gdf.");
        // returns an error message with description if merge testrun
        // directory not found.
        gdfFileWrittingStatus.add("Error");
        gdfFileWrittingStatus.add("for merge testrun, testrun directory not found.");
        return gdfFileWrittingStatus;
      }
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "writeMergeTestRunGDF", "", "", "Exception while writting testrun.gdf - ", ex);
      ex.printStackTrace();
      // return an error message with description if exception comes
      gdfFileWrittingStatus.add("Error");
      gdfFileWrittingStatus.add(ex);
      return gdfFileWrittingStatus;
    }
  }

  // checking version number of testrun.gdf is same
  // if version num same in both testrun.gdf then returns true
  public ArrayList isVersionNumSame()
  {
    ArrayList arrResult = new ArrayList();
    try
    {
      Log.debugLog(className, "isVersionNumSame", "", "", "Method called.");

      // reading info line from both testrun.gdf that is to be merged
      String infoLineTR1 = graphNames1.getGDFInfoLine();
      String infoLineTR2 = graphNames2.getGDFInfoLine();

      if (infoLineTR1.startsWith("Error"))
      {
        arrResult.add("Error");
        String[] errDescription = rptUtilsBean.strToArrayData(infoLineTR1, "|");
        arrResult.add(errDescription[1]);
        Log.errorLog(className, "isVersionNumSame", "", "", errDescription[1]);
        return arrResult;
      }

      if (infoLineTR2.startsWith("Error"))
      {
        arrResult.add("Error");
        String[] errDescription = rptUtilsBean.strToArrayData(infoLineTR2, "|");
        arrResult.add(errDescription[1]);
        Log.errorLog(className, "isVersionNumSame", "", "", errDescription[1]);
        return arrResult;
      }

      if (infoLineTR1.equals("") || infoLineTR1.length() == 0)
      {
        Log.errorLog(className, "isVersionNumSame", "", "", "unable to get version of testrun.gdf for TestRun " + testRunNum1);
        arrResult.add("Error");
        arrResult.add("Unable to get version of testrun.gdf for TestRun " + testRunNum1);
        return arrResult;
      }

      if (infoLineTR2.equals("") || infoLineTR2.length() == 0)
      {
        Log.errorLog(className, "isVersionNumSame", "", "", "Unable to get version of testrun.gdf for TestRun " + testRunNum2);
        arrResult.add("Error");
        arrResult.add("Unable to get version of testrun.gdf for TestRun " + testRunNum2);
        return arrResult;
      }

      Log.debugLogAlways(className, "isVersionNumSame", "", "", "infoLineTR1 = " + infoLineTR1 + ", infoLineTR2 = " + infoLineTR2);
      
      // parsing infoline of testruns to get version number
      String[] arrInfoLineTR1 = rptUtilsBean.strToArrayData(infoLineTR1, "|");
      String[] arrInfoLineTR2 = rptUtilsBean.strToArrayData(infoLineTR2, "|");

      // getting version number
      String versionTR1 = arrInfoLineTR1[1];
      String versionTR2 = arrInfoLineTR2[1];

      if (versionTR1.equals(versionTR2))
      {
        VERSION_NUMBER = versionTR1;
        String[] arrVersionInfo = rptUtilsBean.strToArrayData(VERSION_NUMBER, ".");
        majorVersionNum = Integer.parseInt(arrVersionInfo[0]);
        minorVersionNum = Integer.parseInt(arrVersionInfo[1]);

        Log.debugLog(className, "isVersionNumSame", "", "", "VERSION in both test run is  = " + VERSION_NUMBER);

        if (majorVersionNum < 2)
          dataElementSize = 4;
        else
          dataElementSize = 8;

        arrResult.add("Success");
        return arrResult;
      }
      else
      {
        Log.errorLog(className, "isVersionNumSame", "", "", "version in " + testRunNum1 + " = " + versionTR1 + " and in " + testRunNum2 + " = " + versionTR2);
        arrResult.add("Error");
        arrResult.add("Graph definition(GDF) file versions are not same for both test runs.<br>Version in " + testRunNum1 + " = " + versionTR1 + "<br>Version in " + testRunNum2 + " = " + versionTR2 + "<br> So you cannot merge these test runs.");

        return arrResult;
      }
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "isVersionNumSame", "", "", "Exception - ", ex);
      ex.printStackTrace();
      arrResult.add("Error");
      arrResult.add(ex);
      return arrResult;
    }
  }

  // checking interval is same
  public ArrayList isIntervalSame()
  {
    ArrayList arrIsIntervalSameResult = new ArrayList();
    try
    {
      Log.debugLog(className, "isIntervalSame", "", "", "Method called");
      int interval1 = graphNames1.getInterval();
      int interval2 = graphNames2.getInterval();

      if (interval1 == interval2)
      {
        interval = graphNames1.getInterval();
        Log.debugLog(className, "isIntervalSame", "", "", "interval in both testrun.gdf = " + interval);
        // if interval is same returns successfull message
        arrIsIntervalSameResult.add("Success");
        return arrIsIntervalSameResult;
      }
      else
      {
        Log.debugLog(className, "isIntervalSame", "", "", "interval in base line test run = " + interval1 + "interval in second test run = " + interval2);

        // if any exception occurs returns an error message with error
        // message description
        arrIsIntervalSameResult.add("Error");
        arrIsIntervalSameResult.add("Cannot merge because interval is not same for both test runs.\\nInteval in testrun " + testRunNum1 + " = " + interval1 + "\\nInteval in testrun " + testRunNum2 + " = " + interval2);
        return arrIsIntervalSameResult;
      }
    }
    catch (Exception ex)
    {
      Log.debugLog(className, "isIntervalSame", "", "", "exception while reading interval." + ex);
      ex.printStackTrace();

      // if any exception occurs returns an error message with exception
      // description
      arrIsIntervalSameResult.add("Error");
      arrIsIntervalSameResult.add(ex);
      return arrIsIntervalSameResult;
    }
  }

  // handle if testrun directory is not available
  public boolean isTestRunExist(int testRunNum)
  {
    try
    {
      String logsPath = workPath + "/webapps/logs";
      String testRunDir = logsPath + "/TR" + testRunNum;
      File file = new File(testRunDir);
      if (file.exists())
      {
        return true;
      }
      else
      {
        return false;
      }
    }
    catch (Exception ex)
    {
      return false;
    }
  }

  public static boolean deleteDir(File dir)
  {
    if (dir.isDirectory())
    {
      String[] children = dir.list();
      for (int i = 0; i < children.length; i++)
      {
        boolean success = deleteDir(new File(dir, children[i]));
        if (!success)
        {
          return false;
        }
      }
    }

    // The directory is now empty so delete it
    return dir.delete();
  }

  // method to merging two test runs into one test run and returns true if
  // merging successfully done.
  // mode 0 - union Mode
  // mode 1 - Append Mode
  public String[] mergeGraphs(int newTestRun, int[] arrTestRunsToMerge, int mode, String[] arrStartElapsedTime, String[] arrEndElapsedTime)
  {
    String[] mergeGraphsResult = new String[2];
    try
    {
      Log.debugLog(className, "mergeGraphs", "", "", "Method called.");
      long startMergingTime = System.currentTimeMillis();
      // used to merge test run number
      mergeNumTestRun = newTestRun;
      // base line test run number
      testRunNum1 = arrTestRunsToMerge[0];
      // second test run number that is to be merged with base line test
      // run number
      testRunNum2 = arrTestRunsToMerge[1];
      startElapsedTimeTR1 = arrStartElapsedTime[0].trim();
      startElapsedTimeTR2 = arrStartElapsedTime[1].trim();
      endElapsedTimeTR1 = arrEndElapsedTime[0].trim();
      endElapsedTimeTR2 = arrEndElapsedTime[1].trim();

      Log.debugLog(className, "mergeGraphs", "", "", "Test Run " + testRunNum1 + " Start Elapsed Time = " + startElapsedTimeTR1 + ", end Elapsed Time = " + endElapsedTimeTR1);
      Log.debugLog(className, "mergeGraphs", "", "", "Test Run " + testRunNum2 + " Start Elapsed Time = " + startElapsedTimeTR2 + ", end Elapsed Time = " + endElapsedTimeTR2);

      if (!isTestRunExist(testRunNum1))
      {
        mergeGraphsResult[0] = "Error";
        mergeGraphsResult[1] = "Can not merge because TestRun " + testRunNum1 + " may not exist.";
        return mergeGraphsResult;
      }

      if (!isTestRunExist(testRunNum2))
      {
        mergeGraphsResult[0] = "Error";
        mergeGraphsResult[1] = "Can not merge because TestRun " + testRunNum2 + " may not exist.";
        return mergeGraphsResult;
      }
      // mode for merging
      // 0 -> for union mode and 1 -> for append mode
      mergeMode = mode;
      Log.debugLog(className, "mergeGraphs", "", "", "Base line Test Run Number = " + testRunNum1 + ", Second Test Run Number = " + testRunNum2);
      Log.debugLog(className, "mergeGraphs", "", "", "Merge Test Run Number  = " + mergeNumTestRun + ", Merging Mode = " + mergeMode);

      graphNames1 = new GraphNames(testRunNum1, null, null, "NA", "", "", true);
      graphNames2 = new GraphNames(testRunNum2, null, null, "NA", "", "", true);
      
      // checking if version is not same in both test run returns error
      // with error message description
      ArrayList isVersionSame = isVersionNumSame();
      if (isVersionSame != null && isVersionSame.size() > 0)
      {
        if (isVersionSame.get(0).toString().equals("Error"))
        {
          // if version number is diffrent, merging can not be done
          // and
          // return an error message with description
          mergeGraphsResult[0] = "Error";
          mergeGraphsResult[1] = isVersionSame.get(1).toString();
          return mergeGraphsResult;
        }
        else if (majorVersionNum < 2)
        {
          // if version number is less than 3, merging can not be done
          // return an error message with description
          mergeGraphsResult[0] = "Error";
          mergeGraphsResult[1] = "Selected testruns are not compatible. So you cannot be merged these test runs.";
          return mergeGraphsResult;
        }
      }
      // checking if interval is not same in both test run returns error
      // with error message description
      ArrayList isIntervalSame = isIntervalSame();
      if (isIntervalSame != null && isIntervalSame.size() > 0)
      {
        if (isIntervalSame.get(0).toString().equals("Error"))
        {
          // interval is diffrent, merging can not be done,
          // returns an error log with description
          mergeGraphsResult[0] = "Error";
          mergeGraphsResult[1] = isIntervalSame.get(1).toString();
          return mergeGraphsResult;
        }
      }
      
      arrAllInfoAbtTR1 = graphNames1.getInfoAboutGraph();
      if (arrAllInfoAbtTR1.contains("Error"))
      {
        // return an error message with description if any error comes while getting data
        mergeGraphsResult[0] = "Error";
        mergeGraphsResult[1] = "Cannot merge because " + arrAllInfoAbtTR1.get(1).toString();
        return mergeGraphsResult;
      }
      arrAllInfoAbtTR2 = graphNames2.getInfoAboutGraph();
      if (arrAllInfoAbtTR2.contains("Error"))
      {
        // return an error message with description if any error comes
        // while getting data
        mergeGraphsResult[0] = "Error";
        mergeGraphsResult[1] = "Cannot merge because " + arrAllInfoAbtTR2.get(1).toString();
        return mergeGraphsResult;
      }

      // getting gdf data of both test run
      ArrayList arrGDFDataStatus = getDataOfTestRuns(testRunNum1, testRunNum2);
      if (arrGDFDataStatus.contains("Error"))
      {
        // return an error message with description if any error comes
        // while getting data
        mergeGraphsResult[0] = "Error";
        mergeGraphsResult[1] = arrGDFDataStatus.get(1).toString();
        return mergeGraphsResult;
      }

      createTestRunDir(mergeNumTestRun);
      File mergeTRDir = new File(mergeTRNumberDirectoryPath);
      // writting testrun.gdf file
      ArrayList gdfFileStatus = writeMergeTestRunGDF();
      if (gdfFileStatus != null && gdfFileStatus.size() > 0)
      {
        if (gdfFileStatus.get(0).toString().equals("Error"))
        {
          // testrun.gdf is not written, merging can not be done
          // returns error message if any error comes while writting
          // gdf for merge test run
          mergeGraphsResult[0] = "Error";
          mergeGraphsResult[1] = gdfFileStatus.get(1).toString();
          if (mergeTRDir.exists())
          {
            deleteDir(mergeTRDir);
          }
          return mergeGraphsResult;
        }
        else
        {
          Log.debugLog(className, "mergeGraphs", "", "", "for merge test run, testrun.gdf file is written successfully.");
        }
      }
      // writting summary.top file
      ArrayList summart_TopFileStatus = writeSummary_TopFile();
      if (summart_TopFileStatus != null && summart_TopFileStatus.size() > 0)
      {
        if (summart_TopFileStatus.get(0).toString().equals("Error"))
        {
          // summary.top file is not written, merging can not be done
          // returns error message if any error comes while writting
          // gdf for merge test run
          mergeGraphsResult[0] = "Error";
          mergeGraphsResult[1] = summart_TopFileStatus.get(1).toString();
          if (mergeTRDir.exists())
            deleteDir(mergeTRDir);

          return mergeGraphsResult;
        }
        else
        {
          Log.debugLog(className, "mergeGraphs", "", "", "for merge testrun, summary.top file is written successfully.");
        }
      }

      // writting rtgMessage.dat file
      ArrayList rtgFileStatus = writeRTGMessageFile();
      if (rtgFileStatus != null && rtgFileStatus.size() > 0)
      {
        if (rtgFileStatus.get(0).toString().equals("Error"))
        {
          // rtgMessage.dat file is not written, merging can not be done
          // returns error message if any error comes while writting
          // gdf for merge test run
          // any any error comes, then need to remove directory from backend
          mergeGraphsResult[0] = ("Error");
          mergeGraphsResult[1] = rtgFileStatus.get(1).toString();
          if (mergeTRDir.exists())
          {
            deleteDir(mergeTRDir);
          }
          return mergeGraphsResult;
        }
        else
        {
          Log.debugLog(className, "mergeGraphs", "", "", "for merge testrun, rtgMessage.dat file is written successfully.");
        }
      }
      mergeGraphsResult[0] = "Success";
      long mergingEndTime = System.currentTimeMillis();
      // time taken in merging
      long totalTimeTaken = mergingEndTime - startMergingTime;
      Log.debugLog(className, "mergeGraphs", "", "", "total time taken in miliseconds = " + totalTimeTaken + " in merging of testruns " + testRunNum1 + " and " + testRunNum2);
      return mergeGraphsResult;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "mergeGraphs", "", "", "Exception occurs while merging - ", ex);
      ex.printStackTrace();

      // returns an error message with description if any exception occurs
      mergeGraphsResult[0] = "Error";
      mergeGraphsResult[1] = ex.toString();
      return mergeGraphsResult;
    }
  }

  // main method
  public static void main(String[] args)
  {
    System.out.println("TR");
    MergeTestRuns obj = new MergeTestRuns("Ravi");
    long mergingStartTime = System.currentTimeMillis();
    System.out.println("Test Run Merging Start.... Please wait");
    int[] arrTestRunsToMerge = { 4957, 4953 };
    String[] arrStartElapsedTime = new String[2];
    arrStartElapsedTime[0] = "00:00:00";
    arrStartElapsedTime[1] = "00:00:00";

    String[] arrEndElapsedTime = new String[2];
    arrEndElapsedTime[0] = "00:01:00";
    arrEndElapsedTime[1] = "00:02:00";

    String[] arr = obj.mergeGraphs(55555, arrTestRunsToMerge, 1, arrStartElapsedTime, arrEndElapsedTime);
    System.out.println(arr[0]);
    System.out.println(arr[1]);
    long mergingEndTime = System.currentTimeMillis();
    System.out.println("total Time Taken = " + (mergingEndTime - mergingStartTime) + " miliseconds");
  }
}

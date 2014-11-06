/**
 * TestSuiteBean.java Purpose: For TestSuite and TestCase gui Method Implementation.
 **/

package pac1.Bean;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;

import sun.security.pkcs11.wrapper.CK_ATTRIBUTE;

public class TestSuiteBean
{

  private final String className = "TestSuiteBean"; // class Name
  private final String workPath = Config.getWorkPath(); // Work path
  private final String TEST_SUITE_FILE_EXTN = ".conf"; // test suite file extension
  private final String TEST_CASE_FILE = "/testcase.conf"; // test case file name.
  private final String RUN_TEST_SUITE = "ts_run"; // command name to run test suite.
  private final String RUN_TEST_SUITE_NEW = "ts_start_test";
  private final String TS_SHOW_TEST_SUITE = "ts_show_testsuites"; // command to show test suites.
  private final String TS_SHOW_TEST_CASE = "ts_show_testcases"; // command to show test cases.
  private final String TS_SHOW_TEST_SUITE_CONTENT = "ts_show_testsuit_content"; // command to show test cases of testsuite.
  private final String NSU_SHOW_SCENARIOS = "nsu_show_scenarios"; // command to show scenarios.
  private final String NSU_SHOW_PROJECTS = "nsu_show_projects"; // Showing projects/subprojects.
  private final String arrCheckStatusVar[][] = {{"TCP Connections Total", "SD_TotalConn"}, {"TCP Connections Success", "SD_TotalSuccConn"},
      {"Connection Pass Percentage", "SD_TotalPassPct"}, {"Total URL hits", "SD_TotalURLHits"}, {"Successful URL hits", "SD_TotalSuccURL"},
      {"URL hits per second", "SD_URLHitsPerSec"}, {"URL failures", "SD_URLFailures"}, {"Connections open per second", "SD_TotalConnOpenSec"},
      {"Connections close per second", "SD_TotalConnCloseSec"}, {"TCP Tx Troughput", "SD_TcpTx"}, {"TCP Rx Troughput", "SD_TcpRx"},
      {"Ethernet Tx Throughput", "SD_EthTx"}, {"Ethernet Rx Throughput", "SD_EthRx"}, {"Url Avg Time (Sceonds)", "SD_AvgURLTime"},
      {"Url Max Time (Sceonds)", "SD_MaxURLTime"}, {"Url Min Time (Sceonds)", "SD_MinURLTime"}, {"Trans. Avg Time (Sceonds)", "SD_AvgTrasTime"},
      {"Trans. Max Time (Sceonds)", "SD_MaxTrasTime"}, {"Trans. Min Time (Sceonds)", "SD_MinTrasTime"}, {"Trans. Total", "SD_TotalTrasHits"},
      {"Trans. Success", "SD_TotalSuccTras"}, {"Transactions/Sec", "SD_TrasHitsPerSec"}, {"Trans. Failures", "SD_TrasFailures"}};

  String defaultTestSuiteArgsValue1 = "1"; // default value of test suite file 3rd column data.
  String defaultTestSuiteArgsValue2 = "1"; // default value of test suite file 4th column data.

  // Common variables for excute cmd
  String strCmdName = ""; // command name.
  String strCmdArgs = ""; // command arguments.
  Vector vecCmdOutput = new Vector(); // vector to store command data.
  String[][] arrDataValues = null; // 2D array to send data to jsp.
  CmdExec cmdExec = new CmdExec();
  ArrayList projectSubprojectList = null; // list of project/subproject.

  // Writer objects to write in a file.
  File testCaseFile = null; // file object to read/write file.
  FileWriter testcaseWriter = null; // file writer object to write in a file.
  BufferedWriter testcaseDataWriter = null; // bufferWriter object.
  FileInputStream ifile = null; // file stream object to read data from file.
  BufferedReader iBufferFile = null; // buffer reader object to store data in buffer while reading.

  String projSubproject = "default/default"; // project/subproject of testsuite/testcase directory.
  String testSuiteName = ""; // Name of testsuite.

  String strData = ""; // Temporary storage of data in string while reading.
  String strValue = ""; // Store data after getting from stream.

  String ownerName = "netstorm"; // Owner name of files.
  String lineSeparator = System.getProperty("line.separator");
  String formattedData = "";
  boolean isGuiMode = true;
  
  public TestSuiteBean()
  {

  }

  // Path of the TestSuites
  public String testSuitePath()
  {
    return (workPath + "/testsuites/");
    // return "G://New//";
  }

  // Path of the TestCases
  public String TestCasePath()
  {
    return (workPath + "/testcases/");
    // return "G://new1//";
  }

  // Path of the Scenario directory
  public String ScenarioPath()
  {
    return (workPath + "/scenarios/");
    // return "G://new2//";
  }

  public String getDataWithLineSaperator(String strTextData)
  {
    try
    {
      if (strTextData.equals(""))
        return "";

      String textData[] = strTextData.split("\n");
      String formattedString = "";
      for (int i = 0; i < textData.length; i++)
      {
        formattedString = formattedString + textData[i].replaceAll("\r", "");
        formattedString = formattedString + lineSeparator;
      }
      formattedString = formattedString.replaceAll("\r", "");
      return formattedString;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return strTextData;
    }
  }

  public void setGuiMode(boolean isGuiMode)
  {
    this.isGuiMode = isGuiMode;
  }
  
  public boolean getGuiMode()
  {
    return this.isGuiMode;
  }
  
  // Show All test suite files.
  public String[][] getAllTestSuiteFile(String strCmdArguments)
  {
    try
    {
      Log.debugLog(className, "getAllTestSuiteFile", "", "", "method called.");

      strCmdName = TS_SHOW_TEST_SUITE; // command Name
      strCmdArgs = strCmdArguments;
      vecCmdOutput = new Vector();

      boolean isQueryStatus = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, "netstorm", null);

      if (!isQueryStatus)
      {
        arrDataValues = new String[2][1];
        arrDataValues[0][0] = "Error in Command Execution";
        arrDataValues[1][0] = getVectorDataInStringBuff(vecCmdOutput).toString();
        return arrDataValues;

      }
      arrDataValues = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "-");
      return arrDataValues;
    }
    catch (Exception e)
    {
      // e.printStackTrace();
      Log.stackTraceLog(className, "getAllTestSuiteFile", "", "", "Exception  = ", e);
      return null;
    }
  }

  // Show All test suite files.
  public String[][] getAllTestCaseOfTestSuite(String testSuiteName)
  {
    try
    {
      Log.debugLog(className, "getAllTestCaseOfTestSuite", "", "", "method called");

      String arrData[][] = null;
      if (testSuiteName.equals(""))
        return null;

      strCmdName = TS_SHOW_TEST_SUITE_CONTENT; // command Name
      strCmdArgs = "-t" + " " + getProjectSubproject() + "/" + testSuiteName + ".conf";

      // System.out.println("strCmdArgs ==" + strCmdArgs);
      vecCmdOutput = new Vector();

      boolean isQueryStatus = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, "netstorm", null);

      if (!isQueryStatus)
      {
        arrData = new String[2][1];
        arrData[0][0] = "Error in Command Execution";
        arrData[1][0] = getVectorDataInStringBuff(vecCmdOutput).toString();
        return arrData;
      }
      arrData = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "-");
      return arrData;
    }
    catch (Exception e)
    {
      // e.printStackTrace();
      Log.stackTraceLog(className, "getAllTestCaseOfTestSuite", "", "", "Exception = ", e);
      return null;
    }
  }

  // Show all test Case Files.
  public String[][] getAllTestCaseFile(String strCmdArguments)
  {
    try
    {
      Log.debugLog(className, "getAllTestCaseFile", "", "", "method called");

      strCmdName = TS_SHOW_TEST_CASE;
      strCmdArgs = strCmdArguments;
      vecCmdOutput = new Vector();

      boolean isQueryStatus = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, "netstorm", null);

      if (!isQueryStatus)
      {
        arrDataValues = new String[2][1];
        arrDataValues[0][0] = "Error in Command Execution";
        arrDataValues[1][0] = getVectorDataInStringBuff(vecCmdOutput).toString();
        return arrDataValues;

      }
      arrDataValues = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "-");
      return arrDataValues;
    }
    catch (Exception e)
    {
      // e.printStackTrace();
      Log.stackTraceLog(className, "getAllTestCaseFile", "", "", "Exception = ", e);
      return null;
    }
  }

  // Show all Scenario Files.
  public String[][] getAllScenarioFile(String strCmdArguments)
  {
    try
    {
      Log.debugLog(className, "getAllTestCaseFile", "", "", "method called");

      strCmdName = NSU_SHOW_SCENARIOS;
      strCmdArgs = strCmdArguments;

      vecCmdOutput = new Vector();
      boolean isQueryStatus = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, "netstorm", null);

      if (!isQueryStatus)
      {
        arrDataValues = new String[2][1];
        arrDataValues[0][0] = "Error in Command Execution";
        arrDataValues[1][0] = getVectorDataInStringBuff(vecCmdOutput).toString();
        return arrDataValues;

      }
      arrDataValues = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "-");
      return arrDataValues;
    }
    catch (Exception e)
    {
      // e.printStackTrace();
      Log.stackTraceLog(className, "getAllScenarioFile", "", "", "Exception = ", e);
      return null;
    }
  }

  // delete testSuite file.
  public void DeleteTestSuiteFile(String testSuiteName)
  {
    try
    {
      Log.debugLog(className, "getAllTestCaseFile", "", "", "method called");

      if (testSuiteName.equals(""))
        return;

      File testSuiteFile = openFile(testSuitePath() + "/" + testSuiteName + ".conf");
      if (testSuiteFile.exists())
      {
        testSuiteFile.delete();
      }
    }
    catch (Exception e)
    {
      // e.printStackTrace();
      Log.stackTraceLog(className, "DeleteTestSuiteFile", "", "", "Exception = ", e);
    }
  }

  // delete testSuite file.
  public void DeleteTestCaseFile(String testCaseName)
  {
    try
    {
      Log.debugLog(className, "getAllTestCaseFile", "", "", "method called");

      if (testCaseName.equals(""))
        return;

      File testCaseFile = openFile(TestCasePath() + "/" + testCaseName);
      if (testCaseFile.exists())
      {
        boolean isDelete = removeDirectory(testCaseFile);
      }
    }
    catch (Exception e)
    {
      // e.printStackTrace();
      Log.stackTraceLog(className, "DeleteTestCaseFile", "", "", "Exception = ", e);
    }
  }

  // delete multiple testsuites files.
  public void DeleteMultipleTestSuiteFiles(ArrayList testSuiteNames)
  {
    try
    {
      Log.debugLog(className, "DeleteMultipleTestSuiteFiles", "", "", "method called");

      if (testSuiteNames == null)
        return;

      for (int i = 0; i < testSuiteNames.size(); i++)
        DeleteTestSuiteFile(testSuiteNames.get(i).toString());

    }
    catch (Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "DeleteMultipleTestSuiteFiles", "", "", "Exception = ", e);

    }
  }

  // delete multiple testCases files.
  public void DeleteMultipleTestCaseFiles(ArrayList testCaseNames)
  {
    try
    {
      Log.debugLog(className, "DeleteMultipleTestCaseFiles", "", "", "method called");

      if (testCaseNames == null)
        return;

      for (int i = 0; i < testCaseNames.size(); i++)
        DeleteTestCaseFile(testCaseNames.get(i).toString());

    }
    catch (Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "DeleteMultipleTestCaseFiles", "", "", "Exception = ", e);

    }
  }

  public static boolean removeDirectory(File directory)
  {
    try
    {
      Log.debugLog("TestSuiteBean", "removeDirectory", "", "", "method called");

      if (directory == null)
        return false;
      if (!directory.exists())
        return true;
      if (!directory.isDirectory())
        return false;

      String[] list = directory.list();

      // Some JVMs return null for File.list() when the
      // directory is empty.
      if (list != null)
      {
        for (int i = 0; i < list.length; i++)
        {
          File entry = new File(directory, list[i]);

          if (entry.isDirectory())
          {
            if (!removeDirectory(entry))
              return false;
          }
          else
          {
            if (!entry.delete())
              return false;
          }
        }
      }

      return directory.delete();
    }
    catch (Exception e)
    {
      return false;
    }
  }

  // Method to get String data in buffer.
  public StringBuffer getVectorDataInStringBuff(Vector vecData)
  {
    StringBuffer strBuff = new StringBuffer();

    for (int i = 0; i < vecData.size(); i++)
    {
      strBuff.append(vecData.get(i).toString() + "<br>");
    }
    return strBuff;
  }

  // Open file and return as File object
  private File openFile(String fileName)
  {
    try
    {
      File tempFile = new File(fileName);
      return (tempFile);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "openFile", "", "", "Exception - ", e);
      return null;
    }
  }

  // Method Used To Save Test Suite Data in .conf file.
  public void SaveTestSuiteFileData(String testSuiteName, ArrayList testSuiteData, String newTestSuiteName, String ownerName)
  {

    try
    {
      Log.debugLog(className, "SaveTestSuiteFileData", "", "", "method called");

      if (testSuiteName.equals("") || testSuiteData == null)
        return;

      ValidateProjectSubProject(testSuitePath() + "/" + getProjectSubproject());
      File testSuiteFile = openFile(testSuitePath() + "/" + getProjectSubproject() + "/" + testSuiteName + ".conf");
      if (testSuiteFile.exists())
      {
        testSuiteFile.delete();
      }
      if (!newTestSuiteName.equals(""))
        testSuiteFile = openFile(testSuitePath() + "/" + getProjectSubproject() + "/" + newTestSuiteName + ".conf");

      testSuiteFile.createNewFile();

      if (!ownerName.equals(""))
        this.ownerName = ownerName;

      ChangeOwnerOfFile(testSuiteFile.getAbsolutePath(), this.ownerName);

      FileWriter fw = new FileWriter(testSuiteFile, true);
      BufferedWriter bw = new BufferedWriter(fw);
      int i = 0;
      // bw.write("\n");

      while (i < testSuiteData.size())
      {
        bw.write(testSuiteData.get(i).toString());
        bw.newLine();
        i++;
      }
      bw.close();
      fw.close();
    }
    catch (Exception e)
    {
      // e.printStackTrace();
      Log.stackTraceLog(className, "SaveTestSuiteFileData", "", "", "Exception = ", e);
    }
  }

  // Create iteration file of testcase.
  public boolean CreateIterationFile(String testcaseDirPath, TestCaseCacheData testCaseCacheData)
  {
    try
    {
      Log.debugLog(className, "CreateIterationFile", "", "", "method called");

      testCaseFile = new File(testcaseDirPath + "/" + "iteration.spec");
      if (testCaseFile.exists())
        testCaseFile.delete();

      if (testCaseCacheData.getIterationTabData().equals(""))
        return true;

      testCaseFile.createNewFile();
      ChangeOwnerOfFile(testCaseFile.getAbsolutePath(), ownerName);

      testcaseWriter = new FileWriter(testCaseFile.getAbsolutePath());
      testcaseDataWriter = new BufferedWriter(testcaseWriter);

      // testcaseDataWriter.close();
      // write data in file

      // System.out.println("testCaseCacheData.getIterationTabData()====" + testCaseCacheData.getIterationTabData());
      // System.out.println("writing data ===hi");
      formattedData = getDataWithLineSaperator(testCaseCacheData.getIterationTabData().replaceAll("&#010;", "\n"));
      testcaseDataWriter.write(formattedData);

      // testcaseDataWriter.write(testCaseCacheData.getIterationTabData().replaceAll("&#010;", lineSeparator));
      testcaseDataWriter.flush();
      testcaseWriter.close();
      testcaseDataWriter.close();
      return true;
    }
    catch (Exception e)
    {
      // e.printStackTrace();
      Log.stackTraceLog(className, "CreateIterationFile", "", "", "Exception = ", e);
      return false;
    }
    finally
    {
      try
      {
        // if (testcaseWriter != null)
        // testcaseWriter.close();
        // if (testcaseDataWriter != null)
        // testcaseDataWriter.close();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  // Create check status file of testcase.
  public boolean CreateCheckStatusFile(String testcaseDirPath, TestCaseCacheData testCaseCacheData)
  {
    try
    {
      Log.debugLog(className, "CreateCheckStatusFile", "", "", "method called");

      //testCaseFile = new File(testcaseDirPath + "/" + "check_status");
      testCaseFile = new File(testcaseDirPath + "/" + "check_status_using_profile");
      //System.out.println(" Path: "+testcaseDirPath + "/" + "check_status_using_profile");
      if (testCaseFile.exists())
        testCaseFile.delete();

      File file = new File(testcaseDirPath);
      if(!file.exists())
        file.mkdirs();
      
      //if (testCaseCacheData.getCheckStatusTabData().trim().equals(""))
        //testCaseCacheData.setCheckStatusTabData("#!/bin/bash \nexit 0");
      //return true;

      testCaseFile.createNewFile();
      ChangeOwnerOfFile(testCaseFile.getAbsolutePath(), ownerName);
      ChangePermissionOfFile(testCaseFile.getAbsolutePath(), "");

      testcaseWriter = new FileWriter(testCaseFile);
      testcaseDataWriter = new BufferedWriter(testcaseWriter);
      
      // GUI mode
      if(getGuiMode())
      {
      // write data in file
      formattedData = getDataWithLineSaperator(testCaseCacheData.getCheckStatusTabData().replaceAll("&#010;", "\n"));
      //System.out.println("formattedDatta= "+formattedData);

      //testcaseDataWriter.write("#keyword Project/SubProject/ProfileName RunPostCheckScriptOnFail\n");
      if(!formattedData.trim().equals(""))
      {
        String[] data = formattedData.split("\n");

        for(int i = 0; i < data.length; i++)
          testcaseDataWriter.write("CHECK_STATUS " + getProjectSubproject()+ "/" + data[i] + ".cprof " + "\n");
      }
      testcaseDataWriter.write("RUN_POST_SCRIPT_ON_FAILURE " + testCaseCacheData.getRunPostCheckScriptOnFail() + "\n");
      testcaseDataWriter.write("BASELINE_TESTRUN " + testCaseCacheData.getBaseLineTestRun() + "\n");
      if(!testCaseCacheData.getBaseLineTestRun().equals("-1"))
        testcaseDataWriter.write("BASELINE_SCENARIO " + testCaseCacheData.getScenName() + "\n");
      /*if(!testCaseCacheData.getPreviousTestRun().equals("-1"))
      {
    	String strCmdName = "nsu_show_test_logs";
    	String scenarioName = testCaseCacheData.getScenarioName();
    	System.out.println("scenario name: "+scenarioName);
    	int idx = scenarioName.lastIndexOf("/");
    	if(idx >= 0)
    		scenarioName = scenarioName.substring(idx+1);
        String strCmdArgs = "-c " + scenarioName + " -r";
        vecCmdOutput = new Vector();

        boolean isQueryStatus = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, "netstorm", null);

        if (!isQueryStatus)
        {
          testcaseDataWriter.write("PREVIOUS_TESTRUN -1" + "\n");
        }
        else
        {
          arrDataValues = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "-");
          if(arrDataValues.length > 1)
            testcaseDataWriter.write("PREVIOUS_TESTRUN " + arrDataValues[arrDataValues.length-1][2] + "\n");
          else
        	testcaseDataWriter.write("PREVIOUS_TESTRUN -1" + "\n");
        }
      }
      else*/
      testcaseDataWriter.write("PREVIOUS_TESTRUN " + testCaseCacheData.getPreviousTestRun() + "\n");
      testcaseDataWriter.write("NORMAL_THRESHOLD " + testCaseCacheData.getNormalThreshold() + "\n");
      testcaseDataWriter.write("WORNING_THRESHOLD " + testCaseCacheData.getWorningThreshold() + "\n");
      testcaseDataWriter.write("CRITICAL_THRESHOLD " + testCaseCacheData.getCriticalThreshold() + "\n");
      testcaseDataWriter.write("FAIL_CRITERIA " + testCaseCacheData.getFailCriteria() + "\n");
      String scenarioName = testCaseCacheData.getScenarioName();
  	  int idx = scenarioName.lastIndexOf("/");
  	  if(idx >= 0)
  		scenarioName = scenarioName.substring(idx+1);
      testcaseDataWriter.write("PREVIOUS_SCENARIO " + scenarioName + "\n");
      }
      else
      {
        formattedData = testCaseCacheData.getCheckProfileTabData();
        testCaseCacheData.setCheckProfileTabData(formattedData);
        formattedData = getDataWithLineSaperator(formattedData.replaceAll("&#010;", "\n"));
        // formattedData = getDataWithLineSaperator(testCaseCacheData.getPostTestTabData().replaceAll("&#010;", "\n"));
        testcaseDataWriter.write(formattedData);        
      }
      //testcaseDataWriter.write(formattedData);
      // testcaseDataWriter.write(testCaseCacheData.getCheckStatusTabData().replaceAll("&#010;", lineSeparator));

      testcaseDataWriter.flush();
      testcaseWriter.close();
      testcaseDataWriter.close();
      return true;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "CreateCheckStatusFile", "", "", "Exception = ", e);
      return false;
    }
    finally
    {
      try
      {
        if (testcaseWriter != null)
          testcaseWriter.close();
        if (testcaseDataWriter != null)
          testcaseDataWriter.close();

      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  // return string by merging string array data.
  public static String GetStringOfArray(String[] arr)
  {
    String str = "";
    if (arr == null)
      return str;

    // System.out.println("data length===" + arr.length);
    for (int i = 0; i < arr.length; i++)
    {
      // System.out.println("data===" + arr[i]);
      str = str + arr[i].replaceAll("\\s+$", "");
      str = str + "&#010;";
    }
    // System.out.println("after str===" + str);
    return str;
  }

  // Create pre test file of testcase.
  public boolean CreatePreTestFile(String testcaseDirPath, TestCaseCacheData testCaseCacheData)
  {
    try
    {
      Log.debugLog(className, "CreatePreTestFile", "", "", "method called");

      testCaseFile = new File(testcaseDirPath + "/" + "pre_test_setup");

      if (testCaseFile.exists())
        testCaseFile.delete();

      if (testCaseCacheData.getPreTestTabData().equals(""))
        testCaseCacheData.setPreTestTabData("#!/bin/bash \nexit 0");
      //return true;

      testCaseFile.createNewFile();
      ChangeOwnerOfFile(testCaseFile.getAbsolutePath(), ownerName);
      ChangePermissionOfFile(testCaseFile.getAbsolutePath(), "");

      testcaseWriter = new FileWriter(testCaseFile);
      testcaseDataWriter = new BufferedWriter(testcaseWriter);

      // write data in file
      formattedData = testCaseCacheData.getPreTestTabData();
      formattedData = formattedData.replace("#mode: GUI", "#mode: Advance");
      testCaseCacheData.setPreTestTabData(formattedData);
      formattedData = getDataWithLineSaperator(formattedData.replaceAll("&#010;", "\n"));
      // formattedData = getDataWithLineSaperator(testCaseCacheData.getPreTestTabData().replaceAll("&#010;", "\n"));
      testcaseDataWriter.write(formattedData);

      testcaseDataWriter.flush();
      testcaseWriter.close();
      testcaseDataWriter.close();

      return true;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "CreatePreTestFile", "", "", "Exception = ", e);
      return false;
    }
    finally
    {
      try
      {
        if (testcaseWriter != null)
          testcaseWriter.close();
        if (testcaseDataWriter != null)
          testcaseDataWriter.close();

      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  // Create Posttest file of testcase.
  public boolean CreatePostTestFile(String testcaseDirPath, TestCaseCacheData testCaseCacheData)
  {
    try
    {
      Log.debugLog(className, "CreatePostTestFile", "", "", "method called");

      testCaseFile = new File(testcaseDirPath + "/" + "post_test_setup");

      if (testCaseFile.exists())
        testCaseFile.delete();

      if (testCaseCacheData.getPostTestTabData().equals(""))
        testCaseCacheData.setPostTestTabData("#!/bin/bash \nexit 0");
      //return true;

      testCaseFile.createNewFile();
      ChangeOwnerOfFile(testCaseFile.getAbsolutePath(), ownerName);
      ChangePermissionOfFile(testCaseFile.getAbsolutePath(), "");

      testcaseWriter = new FileWriter(testCaseFile);
      testcaseDataWriter = new BufferedWriter(testcaseWriter);

      // write data in file
      formattedData = testCaseCacheData.getPostTestTabData();
      formattedData = formattedData.replace("#mode: GUI", "#mode: Advance");
      testCaseCacheData.setPostTestTabData(formattedData);
      formattedData = getDataWithLineSaperator(formattedData.replaceAll("&#010;", "\n"));
      // formattedData = getDataWithLineSaperator(testCaseCacheData.getPostTestTabData().replaceAll("&#010;", "\n"));
      testcaseDataWriter.write(formattedData);
      // testcaseDataWriter.write(testCaseCacheData.getPostTestTabData().replaceAll("&#010;", lineSeparator));

      testcaseDataWriter.flush();
      testcaseWriter.close();
      testcaseDataWriter.close();

      return true;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "CreatePostTestFile", "", "", "Exception = ", e);
      return false;
    }
    finally
    {
      try
      {
        if (testcaseWriter != null)
          testcaseWriter.close();
        if (testcaseDataWriter != null)
          testcaseDataWriter.close();

      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  // Create testcase.conf file of testcase.
  public boolean CreateTestCaseConfFile(String testcaseDirPath, TestCaseCacheData testCaseCacheData)
  {
    try
    {
      Log.debugLog(className, "CreateTestCaseConfFile", "", "", "method called");

      testCaseFile = new File(testcaseDirPath + "/" + "testcase.conf");

      if (testCaseFile.exists())
        testCaseFile.delete();

      if (testCaseCacheData.getScenarioTabData().equals(""))
        return true;

      testCaseFile.createNewFile();
      ChangeOwnerOfFile(testCaseFile.getAbsolutePath(), ownerName);

      testcaseWriter = new FileWriter(testCaseFile);
      testcaseDataWriter = new BufferedWriter(testcaseWriter);

      // write data in file
      formattedData = getDataWithLineSaperator(testCaseCacheData.getScenarioTabData().replaceAll("&#010;", "\n"));
      testcaseDataWriter.write(formattedData);
      // testcaseDataWriter.write(testCaseCacheData.getScenarioTabData().replaceAll("&#010;", lineSeparator));

      testcaseDataWriter.flush();
      testcaseWriter.close();
      testcaseDataWriter.close();

      return true;
    }
    catch (Exception e)
    {
      // e.printStackTrace();
      Log.stackTraceLog(className, "CreateTestCaseConfFile", "", "", "Exception = ", e);
      return false;
    }
    finally
    {
      try
      {
        if (testcaseWriter != null)
          testcaseWriter.close();
        if (testcaseDataWriter != null)
          testcaseDataWriter.close();

      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  // Create scenario configuration file.
  public boolean CreateScenarioConfFile(TestCaseCacheData testCaseCacheData)
  {
    try
    {
      Log.debugLog(className, "CreateScenarioConfFile", "", "", "method called");

      testCaseFile = new File(ScenarioPath() + "/" + testCaseCacheData.getScenarioProSubProject() + "/" + testCaseCacheData.getScenarioName() + ".conf");

      if (!testCaseFile.exists())
        testCaseFile.delete();

      if (testCaseCacheData.getScenarioConfFileData().equals(""))
        return true;

      testCaseFile.createNewFile();
      ChangeOwnerOfFile(testCaseFile.getAbsolutePath(), ownerName);

      // ChangeOwnerOfFile();
      testcaseWriter = new FileWriter(testCaseFile);
      testcaseDataWriter = new BufferedWriter(testcaseWriter);

      // write data in file
      formattedData = getDataWithLineSaperator(testCaseCacheData.getScenarioConfFileData().replaceAll("&#010;", "\n"));
      testcaseDataWriter.write(formattedData);
      // testcaseDataWriter.write(testCaseCacheData.getScenarioConfFileData().replaceAll("&#010;", lineSeparator));

      testcaseDataWriter.flush();
      testcaseWriter.close();
      testcaseDataWriter.close();

      return true;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "CreateScenarioConfFile", "", "", "Exception = ", e);
      return false;
    }
    finally
    {
      try
      {
        if (testcaseWriter != null)
          testcaseWriter.close();
        if (testcaseDataWriter != null)
          testcaseDataWriter.close();

      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  // Reading iteration.conf file data of testcase.
  public void GetIterationFileData(String testcaseDirPath, TestCaseCacheData testCaseCacheData)
  {

    testCaseFile = new File(testcaseDirPath + "/" + "iteration.spec");
    try
    {
      Log.debugLog(className, "GetIterationFileData", "", "", "method called");

      if (!testCaseFile.exists())
        return;

      strData = "";
      strValue = "";
      ifile = new FileInputStream(testCaseFile);
      iBufferFile = new BufferedReader(new InputStreamReader(ifile));

      while ((strValue = iBufferFile.readLine()) != null)
      {
        strData = strData + strValue;
        strData = strData + "&#010;";
      }
      testCaseCacheData.setIterationTabData(strData);
    }
    catch (Exception e)
    {
      // e.printStackTrace();
      Log.stackTraceLog(className, "GetIterationFileData", "", "", "Exception = ", e);
    }
    finally
    {
      try
      {
        if (iBufferFile != null)
          iBufferFile.close();

        if (ifile != null)
          ifile.close();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  // Reading pre test file data of testcase.
  public void GetPreTestFileData(String testcaseDirPath, TestCaseCacheData testCaseCacheData)
  {

    testCaseFile = new File(testcaseDirPath + "/" + "pre_test_setup");
    try
    {
      Log.debugLog(className, "GetPreTestFileData", "", "", "method called");

      if (!testCaseFile.exists())
        return;

      strData = "";
      strValue = "";
      ifile = new FileInputStream(testCaseFile);
      iBufferFile = new BufferedReader(new InputStreamReader(ifile));

      while ((strValue = iBufferFile.readLine()) != null)
      {
        strData = strData + strValue;
        strData = strData + "\n";
      }
      testCaseCacheData.setPreTestTabData(strData);
    }
    catch (Exception e)
    {
      // e.printStackTrace();
      Log.stackTraceLog(className, "GetPreTestFileData", "", "", "Exception = ", e);
    }
    finally
    {
      try
      {
        if (iBufferFile != null)
          iBufferFile.close();

        if (ifile != null)
          ifile.close();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  // Get post test file data of testcase.
  public void GetPostTestFileData(String testcaseDirPath, TestCaseCacheData testCaseCacheData)
  {

    testCaseFile = new File(testcaseDirPath + "/" + "post_test_setup");
    try
    {
      Log.debugLog(className, "GetPostTestFileData", "", "", "method called");

      if (!testCaseFile.exists())
        return;

      strData = "";
      strValue = "";
      ifile = new FileInputStream(testCaseFile);
      iBufferFile = new BufferedReader(new InputStreamReader(ifile));

      while ((strValue = iBufferFile.readLine()) != null)
      {
        strData = strData + strValue;
        strData = strData + "\n";
      }
      testCaseCacheData.setPostTestTabData(strData);
    }
    catch (Exception e)
    {
      // e.printStackTrace();
      Log.stackTraceLog(className, "GetPostTestFileData", "", "", "Exception = ", e);
    }
    finally
    {
      try
      {
        if (iBufferFile != null)
          iBufferFile.close();

        if (ifile != null)
          ifile.close();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  // Get Check Status file data.
  public void GetCheckStatusFileData(String testcaseDirPath, TestCaseCacheData testCaseCacheData)
  {

    //testCaseFile = new File(testcaseDirPath + "/" + "check_status");
	testCaseFile = new File(testcaseDirPath + "/" + "check_status_using_profile");

    try
    {
      Log.debugLog(className, "GetCheckStatusFileData", "", "", "method called");

      if (!testCaseFile.exists())
        return;

      strData = "";
      strValue = "";
      ifile = new FileInputStream(testCaseFile);
      iBufferFile = new BufferedReader(new InputStreamReader(ifile));

      while ((strValue = iBufferFile.readLine()) != null)
      {
        //strData = strData + strValue;
        //strData = strData + "\n";
    	strValue = strValue.trim();
    	if(strValue.startsWith("#"))
          continue;

    	if(strValue.startsWith("CHECK_STATUS "))
    	{
    	  String tmp[] = strValue.split(" ");
    	  String checkProfile = tmp[1];
    	  int idx = checkProfile.lastIndexOf("/");
    	  if(idx >= 0)
    		  strData += checkProfile.substring(idx+1, checkProfile.lastIndexOf(".")).trim() + "\n";
    	}
    	else if(strValue.startsWith("RUN_POST_SCRIPT_ON_FAILURE "))
    	{
    	  String tmp[] = strValue.split(" ");
    	  testCaseCacheData.setRunPostCheckScriptOnFail(tmp[1]);
    	}
    	else if(strValue.startsWith("BASELINE_TESTRUN "))
    	{
    	  String tmp[] = strValue.split(" ");
    	  testCaseCacheData.setBaseLineTR(tmp[1]);
    	}
      else if(strValue.startsWith("BASELINE_SCENARIO "))
      {
        String tmp[] = strValue.split(" ");
        testCaseCacheData.setScenName(tmp[1]);
      }
    	else if(strValue.startsWith("PREVIOUS_TESTRUN "))
    	{
    	  String tmp[] = strValue.split(" ");
    	  if(!tmp[1].equals("-1"))
    	    testCaseCacheData.setPreviousTestRun("1");
    	}
    	else if(strValue.startsWith("NORMAL_THRESHOLD "))
    	{
    	  String tmp[] = strValue.split(" ");
    	  testCaseCacheData.setNormalThreshold(tmp[1]);
    	}
      else if(strValue.startsWith("FAIL_CRITERIA "))
      {
        String tmp[] = strValue.split(" ");
        testCaseCacheData.setFailCriteria(tmp[1]);
      }
    	else if(strValue.startsWith("WORNING_THRESHOLD "))
    	{
    	  String tmp[] = strValue.split(" ");
    	  testCaseCacheData.setWorningThreshold(tmp[1]);
    	}
    	else if(strValue.startsWith("CRITICAL_THRESHOLD "))
    	{
    	  String tmp[] = strValue.split(" ");
    	  testCaseCacheData.setCriticalThreshold(tmp[1]);
    	}
      }
      testCaseCacheData.setCheckStatusTabData(strData);
      
      // here need to read file again and set data to checkprofileTab data
      
      strData = "";
      strValue = "";
      ifile = new FileInputStream(testCaseFile);
      iBufferFile = new BufferedReader(new InputStreamReader(ifile));

      while ((strValue = iBufferFile.readLine()) != null)
      {
        strData = strData + strValue;
        strData = strData + "\n";
      }
      testCaseCacheData.setCheckProfileTabData(strData);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "GetCheckStatusFileData", "", "", "Exception = ", e);
      // e.printStackTrace();
    }
    finally
    {
      try
      {
        if (iBufferFile != null)
          iBufferFile.close();

        if (ifile != null)
          ifile.close();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  // Get the configuration file data of testcase(testcase.conf).
  public void GetTestCaseConfFileData(String testcaseDirPath, TestCaseCacheData testCaseCacheData)
  {

    testCaseFile = new File(testcaseDirPath + "/" + "testcase.conf");
    try
    {
      Log.debugLog(className, "GetTestCaseConfFileData", "", "", "method called");

      if (!testCaseFile.exists())
        return;

      strData = "";
      strValue = "";
      ifile = new FileInputStream(testCaseFile);
      iBufferFile = new BufferedReader(new InputStreamReader(ifile));

      while ((strValue = iBufferFile.readLine()) != null)
      {
        strData = strData + strValue;
        strData = strData + "\n";
      }
      testCaseCacheData.setScenarioTabData(strData);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "GetTestCaseConfFileData", "", "", "Exception = ", e);
      // e.printStackTrace();
    }
    finally
    {
      try
      {
        if (iBufferFile != null)
          iBufferFile.close();

        if (ifile != null)
          ifile.close();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  public void GetScenarioConfFileData(TestCaseCacheData testCaseCacheData)
  {

    try
    {
      Log.debugLog(className, "GetScenarioConfFileData", "", "", "method called");

      testCaseFile = new File(ScenarioPath() + "/" + testCaseCacheData.getScenarioProSubProject() + "/" + testCaseCacheData.getScenarioName() + ".conf");

      if (!testCaseFile.exists())
        return;

      strData = "";
      strValue = "";
      ifile = new FileInputStream(testCaseFile);
      iBufferFile = new BufferedReader(new InputStreamReader(ifile));

      while ((strValue = iBufferFile.readLine()) != null)
      {
        strData = strData + strValue;
        strData = strData + "\n";
      }
      testCaseCacheData.setScenarioConfFileData(strData);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "GetScenarioConfFileData", "", "", "Exception = ", e);
      // e.printStackTrace();
    }
    finally
    {
      try
      {
        if (iBufferFile != null)
          iBufferFile.close();

        if (ifile != null)
          ifile.close();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  // Get the Scenario file data.
  public String GetScenarioFileData(String scenarioName, String projSubproject)
  {
    try
    {
      Log.debugLog(className, "GetScenarioFileData", "", "", "method called");

      if (projSubproject.equals(""))
        projSubproject = "default/default";

      testCaseFile = new File(ScenarioPath() + "/" + projSubproject + "/" + scenarioName + ".conf");

      if (!testCaseFile.exists())
        return "";

      strData = "";
      strValue = "";
      ifile = new FileInputStream(testCaseFile);
      iBufferFile = new BufferedReader(new InputStreamReader(ifile));

      while ((strValue = iBufferFile.readLine()) != null)
      {
        strData = strData + strValue;
        strData = strData + "\n";
      }
      return strData;
    }
    catch (Exception e)
    {
      // e.printStackTrace();
      Log.stackTraceLog(className, "GetScenarioFileData", "", "", "Exception = ", e);
      return "";
    }
    finally
    {
      try
      {
        if (iBufferFile != null)
          iBufferFile.close();

        if (ifile != null)
          ifile.close();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  // Read TestCase files data.
  public TestCaseCacheData GetTestCaseData(String testCaseName, String scenarioProSubproject, String scenarioName)
  {
    try
    {
      Log.debugLog(className, "GetTestCaseData", "", "", "method called");

      if (testCaseName.equals(""))
        return null;

      File testCaseFile = openFile(TestCasePath() + "/" + projSubproject + "/" + testCaseName);

      if (!testCaseFile.exists())
        return null;

      TestCaseCacheData testCaseCacheData = new TestCaseCacheData();

      if (!scenarioProSubproject.equals(""))
        testCaseCacheData.setScenarioProSubProject(scenarioProSubproject);

      if (!scenarioName.equals(""))
        testCaseCacheData.setScenarioName(scenarioName);

      String testcaseDirPath = testCaseFile.getAbsolutePath();

      GetIterationFileData(testcaseDirPath, testCaseCacheData);
      GetPreTestFileData(testcaseDirPath, testCaseCacheData);
      GetPostTestFileData(testcaseDirPath, testCaseCacheData);
      GetTestCaseConfFileData(testcaseDirPath, testCaseCacheData);
      GetCheckStatusFileData(testcaseDirPath, testCaseCacheData);
      GetScenarioConfFileData(testCaseCacheData);

      return testCaseCacheData;
    }
    catch (Exception e)
    {
      // e.printStackTrace();
      Log.stackTraceLog(className, "GetTestCaseData", "", "", "Exception = ", e);
      return null;
    }
  }

  public void PrepareConfFileData(TestCaseCacheData testCaseCacheData)
  {
    String prepareData = "";
    if (testCaseCacheData.getScenarioName().trim().equals(""))
    {
      testCaseCacheData.setScenarioTabData("");
      return;
    }

    prepareData = "SCENARIO_NAME " + testCaseCacheData.getScenarioProSubProject() + "/" + testCaseCacheData.getScenarioName();
    prepareData = prepareData + "\n";
    prepareData = prepareData + "DESCRIPTION " + testCaseCacheData.getScenarioDescription();

    // setting data.
    testCaseCacheData.setScenarioTabData(prepareData);
  }

  public String SaveTestCaseData(String testCaseName, TestCaseCacheData testCaseCacheData, String newTestCaseName, String ownerName)
  {
    try
    {
      Log.debugLog(className, "SaveTestCaseData", "", "", "method called");
      // FileWriter testcaseWriter = null;
      // BufferedWriter testcaseDataWriter = null;

      if (testCaseName.equals(""))
        return "Test Case Name is empty";

      if (testCaseCacheData == null)
        return "testCaseCacheData object is null";

      ValidateProjectSubProject(TestCasePath() + "/" + projSubproject);
      File testCaseFile = openFile(TestCasePath() + "/" + projSubproject + "/" + testCaseName);

      if (testCaseFile.exists())
      {
        DeleteTestCaseFile(testCaseName);
      }

      testCaseFile = openFile(TestCasePath() + "/" + projSubproject + "/" + newTestCaseName);
      testCaseFile.mkdir();

      String testcaseDirPath = testCaseFile.getAbsolutePath();

      if (!ownerName.equals(""))
        this.ownerName = ownerName;

      ChangeOwnerOfFile(testCaseFile.getAbsolutePath(), this.ownerName);
      // System.out.println("testcaseDirPath===" + testcaseDirPath);
      // testCaseFile = openFile(testcaseDirPath + "/" + );

      // Creating Iteration.spec file
      CreateIterationFile(testcaseDirPath, testCaseCacheData);

      // Creating CheckStatus file
      CreateCheckStatusFile(testcaseDirPath, testCaseCacheData);

      // Creating PreTest file
      CreatePreTestFile(testcaseDirPath, testCaseCacheData);

      // Creating PostTest file
      CreatePostTestFile(testcaseDirPath, testCaseCacheData);

      // Prepare conf file data.
      PrepareConfFileData(testCaseCacheData);

      // Creating testcase.conf file
      CreateTestCaseConfFile(testcaseDirPath, testCaseCacheData);

      // Creating Scenario.conf file
      CreateScenarioConfFile(testCaseCacheData);

      // FileWriter fw = new FileWriter(testcaseDirPath, true);
      // BufferedWriter bw = new BufferedWriter(fw);

      // int i = 0;
      // bw.write("\n");
      // while (i < testCaseData.length)
      // {
      // bw.write(testCaseData[i]);
      // bw.newLine();
      // i++;
      // }
      // bw.close();
      // fw.close();

      return "File";
    }
    catch (Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "SaveTestCaseData", "", "", "Exception = ", e);
      return "Exception in writing " + e.getMessage();
    }

  }

  // Create the project/subprojects directories if not exist.
  public boolean ValidateProjectSubProject(String dirPath)
  {
    try
    {
      testCaseFile = new File(dirPath);
      if (testCaseFile.exists())
        return true;

      testCaseFile.mkdirs();
      return true;

    }
    catch (Exception e)
    {
      e.printStackTrace();
      return false;
    }
  }

  // Inner Class Used for Storing TestCase Tabs Data in Session.
  public static class TestCaseCacheData
  {

    // Declare neccessary variables.
    String tabName = ""; // Used for tab Name.
    String tabValues = ""; // Used for tab values.
    String iterationBufferData = "";
    String preTestBufferData = "";
    String postTestBufferData = "";
    String checkStatusBufferData = "";
    String checkProfileTabData = "";
    String scenarioBufferData = "";
    String scenarioProSubProject = "default/default";
    String scenarioConfFileData = "";
    String scenarioDescription = "";
    String scenarioName = "";
    String runPostCheckScriptOnFail = "0";
    String[][] arrScenarioList = null;
    ArrayList arrProjList = new ArrayList();
    String baseLineTestRun = "";
    String scenNameIs = "";
    String previousTestRun = "-1";
    String normalThreshold = "20";
    String worningThreshold = "20-50";
    String criticalThreshold = "50";
    String failCriteria = "10";
    ArrayList<String> allCheckProfiles = new ArrayList<String>();

    // data.

    // setter Method
    // Set Tab Name
    public void setTabName(String tabName)
    {
      if (tabName == null)
        this.tabName = "";
      else
        this.tabName = tabName;
    }

    // Set Tab Values
    public void setTabValues(String tabValues)
    {
      if (tabValues == null)
        this.tabValues = "";
      else
        this.tabValues = tabValues;
    }

    // Set Scenario Name
    public void setScenarioName(String scenarioName)
    {
      if (scenarioName == null)
        this.scenarioName = "";
      else
        this.scenarioName = scenarioName;
    }

    // Set project/subproject.
    public void setProjectSubproject(ArrayList arrProjList)
    {
      if (arrProjList != null)
        this.arrProjList = arrProjList;
      else
        this.arrProjList.add("default/default");
    }

    public void setAllCheckProfiles(ArrayList<String> allCheckProfiles)
    {
      this.allCheckProfiles = allCheckProfiles;
    }

    // set Scenario project/subproject.
    public void setScenarioProSubProject(String scenarioProSubProject)
    {
      if (scenarioProSubProject == null)
        this.scenarioProSubProject = "default/default";
      else
        this.scenarioProSubProject = scenarioProSubProject;
    }

    // set Scenario Description.
    public void setScenarioDescription(String scenarioDescription)
    {
      if (scenarioDescription == null)
        this.scenarioDescription = "";
      else
        this.scenarioDescription = scenarioDescription;
    }

    // set Scenario project/subproject.
    public void setScenarioConfFileData(String scenarioConfFileData)
    {
      if (scenarioConfFileData == null)
        this.scenarioConfFileData = "";
      else
        this.scenarioConfFileData = scenarioConfFileData;
    }

    // Set Iteration tab Data.
    public void setIterationTabData(String iterationBufferData)
    {
      if (iterationBufferData == null)
        this.iterationBufferData = "";
      else
        this.iterationBufferData = iterationBufferData;
    }

    // Set PreTest tab Data.
    public void setPreTestTabData(String preTestBufferData)
    {
      if (preTestBufferData == null)
        this.preTestBufferData = "";
      else
        this.preTestBufferData = preTestBufferData;
    }

    // Set CheckStatus tab Data.
    public void setCheckStatusTabData(String checkStatusBufferData)
    {
      if (checkStatusBufferData == null)
        this.checkStatusBufferData = "";
      else
        this.checkStatusBufferData = checkStatusBufferData;
    }

    // set checkProfileTabData
    public void setCheckProfileTabData(String checkProfileBufferData)
    {
      if (checkProfileTabData == null)
        this.checkProfileTabData = "";
      else
        this.checkProfileTabData = checkProfileBufferData;
    }
    
    // Set PostTest tab Data.
    public void setPostTestTabData(String postTestBufferData)
    {
      if (postTestBufferData == null)
        this.postTestBufferData = "";
      else
        this.postTestBufferData = postTestBufferData;
    }

    // Set Scenario tab Data.
    public void setScenarioTabData(String scenarioBufferData)
    {
      if (scenarioBufferData == null)
        this.scenarioBufferData = "";
      else
        this.scenarioBufferData = scenarioBufferData;
    }

    public void setScenarioList(String[][] arrScenaioList)
    {
      arrScenarioList = new String[arrScenaioList.length][];
      for (int i = 0; i < arrScenaioList.length; i++)
      {
        arrScenarioList[i] = arrScenaioList[i].clone();
      }
    }

    public void setRunPostCheckScriptOnFail(String value)
    {
      runPostCheckScriptOnFail = value;
    }

    public void setBaseLineTR(String arg)
    {
      baseLineTestRun = arg;
    }

    public void setScenName(String arg)
    {
      scenNameIs = arg;
    }

    public void setPreviousTestRun(String arg)
    {
      previousTestRun = arg;
    }

    public void setNormalThreshold(String arg)
    {
      normalThreshold = arg;
    }

    public void setFailCriteria(String arg)
    {
      failCriteria = arg;
    }

    public void setWorningThreshold(String arg)
    {
      worningThreshold = arg;
    }

    public void setCriticalThreshold(String arg)
    {
      criticalThreshold = arg;
    }

    // Getter Method.
    // Get Tab Name.
    public String getRunPostCheckScriptOnFail()
    {
      return runPostCheckScriptOnFail;
    }

    public String getBaseLineTestRun()
    {
      return baseLineTestRun;
    }

    public String getScenName()
    {
      return scenNameIs;
    }

    public String getPreviousTestRun()
    {
      return previousTestRun;
    }

    public String getNormalThreshold()
    {
      return normalThreshold;
    }

    public String getFailCriteria()
    {
      return failCriteria;
    }

    public String getWorningThreshold()
    {
      return worningThreshold;
    }

    public String getCriticalThreshold()
    {
      return criticalThreshold;
    }

    public String getTabName()
    {
      return tabName;
    }

    // Get Tab Values.
    public String getTabValues()
    {
      return tabValues;
    }

    // Get Project/subProject
    public ArrayList getProjectSubproject()
    {
      return arrProjList;
    }

    public ArrayList<String> getAllCheckProfiles()
    {
      return allCheckProfiles;
    }

    // Get Tab Data
    public String getIterationTabData()
    {
      return iterationBufferData;
    }

    public String[][] getScenarioList()
    {
      return arrScenarioList;
    }

    // Get Tab Data
    public String getPreTestTabData()
    {
      return preTestBufferData;
    }

    // Get Tab Data
    public String getPostTestTabData()
    {
      return postTestBufferData;
    }

    // Get Tab Data
    public String getCheckStatusTabData()
    {
      return checkStatusBufferData;
    }
    
    // Get checkProfileTabData
    public String getCheckProfileTabData()
    {
      return checkProfileTabData;
    }
    
    // Get Tab Data
    public String getScenarioTabData()
    {
      return scenarioBufferData;
    }

    // Get Scenario projectSubproject.
    public String getScenarioProSubProject()
    {
      return scenarioProSubProject;
    }

    // get Scenario conf file data.
    public String getScenarioConfFileData()
    {
      return scenarioConfFileData;
    }

    // get Scenario Name
    public String getScenarioName()
    {
      return scenarioName;
    }

    // get Scenario Description.
    public String getScenarioDescription()
    {
      return scenarioDescription;
    }
  }

  // Setting project/subproject values.
  public void setProjectSubproject(String projectSubproject)
  {
    if (projectSubproject != null)
      this.projSubproject = projectSubproject;
  }

  // Getting project/subproject values.
  public String getProjectSubproject()
  {
    return projSubproject;
  }

  // Setting testsuite name.
  public void setTestSuiteName(String testSuiteName)
  {
    if (testSuiteName != null)
      this.testSuiteName = testSuiteName;
  }

  // Getting testsuite name value.
  public String getTestSuiteName()
  {
    return testSuiteName;
  }

  // For Running TestSuite.
  public Vector RunTestSuite(String testsuiteName, String project, String subproject, String ipAddress, boolean debugMode)
  {
    try
    {
      Log.debugLog(className, "RunTestSuite", "", "", "method called");
      Vector result = new Vector();
      if (testsuiteName.equals(""))
        return null;
      else if (project.equals("") || subproject.equals(""))
        return null;

      strCmdName = RUN_TEST_SUITE_NEW;
      strCmdArgs = "-n" + " " + project + "/" + subproject + "/" + testsuiteName + " -i " + ipAddress + " -S guiFg";

      // System.out.println("strCmdArgs ===" + strCmdArgs);
      vecCmdOutput = new Vector();

      if (debugMode)
        strCmdArgs = strCmdArgs + " " + "-D";

      boolean isqueryExecute = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, CmdExec.SYSTEM_CMD, "netstorm", "netstorm", "");
      // System.out.println("status of command == " + isqueryExecute);
      if (!isqueryExecute)
      {
        result.add("TestSuite execution is not started Successfully.");
        // result.add();
        return result;
      }
      if (vecCmdOutput != null)
      {
        for (int i = 0; i < vecCmdOutput.size(); i++)
        {
          // System.out.println("Inside run = " + vecCmdOutput.get(i).toString());
          if (vecCmdOutput.get(i).toString().trim().startsWith("TestCycleNumber"))
          {
            // System.out.println("main data====" + vecCmdOutput.get(i).toString());
            StringTokenizer ss = new StringTokenizer(vecCmdOutput.get(i).toString(), "=");
            // System.out.println("total tokens = " + ss.countTokens());
            if (ss.countTokens() > 1)
            {
              ss.nextToken();
              result.add("TestSuite Execution is Successfully started with test cycle number " + ss.nextToken());
            }
            else
              result.add("TestSuite execution is not started Successfully.");
            break;
          }
        }
      }
      if (result.size() == 0)
        result.add("TestSuite execution is not started Successfully.");
      // System.out.println("vector size ==" + vecCmdOutput.size() + "  array size ====" + arrDataValues.length);
      // System.out.println("arrray data ===" + vecCmdOutput.get(0).toString());
      // arrDataValues = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "-");
      return result;
    }
    catch (Exception e)
    {
      // e.printStackTrace();
      Log.stackTraceLog(className, "RunTestSuite", "", "", "Exception = ", e);
      return null;
    }
  }

  private String getTestCycleNumber()
  {
    try
    {
      File file = new File("/tmp/tmp.txt");
      if (!file.exists())
      {
        System.out.println("file not found.");
        return "TestSuite execution is not started Successfully.";
      }

      BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
      int i = 0;
      String str = "";
      // search till 4th line beacuse probably it will come in second line
      while ((str = br.readLine()) != null && i <= 3)
      {
        if (str.trim().startsWith("Test Cycle Number"))
          break;
      }
      String testCycleNum = str;
      System.out.println("testCycleNum = " + testCycleNum);
      br.close();
      file.delete();

      String data[] = testCycleNum.split("=");
      if (data.length > 1)
        testCycleNum = "TestSuite execution is started Successfully with test cycle number " + data[1].trim();
      else
        testCycleNum = "TestSuite execution is not started Successfully.";
      return testCycleNum;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return "TestSuite execution is not started Successfully.";
    }
  }

  // Change User type and group of file
  public boolean ChangeOwnerOfFile(String filePath, String owner)
  {
    try
    {
      Log.debugLog(className, "ChangeOwnerOfFile", "", "", "method called");

      Runtime r = Runtime.getRuntime();
      String strCmd = "chown" + " " + owner + "." + "netstorm" + " " + filePath;
      Process changePermissions = r.exec(strCmd);
      int exitValue = changePermissions.waitFor();

      if (exitValue == 0)
        return true;
      else
        return false;

    }
    catch (Exception e)
    {
      // e.printStackTrace();
      Log.stackTraceLog(className, "ChangeOwnerOfFile", "", "", "Exception = ", e);
      return false;
    }
  }

  // Change permissions of file
  public boolean ChangePermissionOfFile(String filePath, String cmdVal)
  {
    String filePermission = "775";

    if (filePath.equals(""))
      return false;

    if (!cmdVal.equals(""))
      filePermission = cmdVal;

    try
    {
      Log.debugLog(className, "ChangePermissionOfFile", "", "", "method called");
      Runtime r = Runtime.getRuntime();
      String strCmd = "chmod" + " " + filePermission + " " + filePath;
      Process changePermissions = r.exec(strCmd);
      int exitValue = changePermissions.waitFor();

      if (exitValue == 0)
        return true;
      else
        return false;
    }
    catch (Exception e)
    {
      // e.printStackTrace();
      Log.stackTraceLog(className, "ChangePermissionOfFile", "", "", "method called", e);
      return false;
    }
  }

  // function for getting project/subproject list of given path in arraylist.
  public ArrayList ListProjectSubproject(String filePath)
  {
    ArrayList arr = new ArrayList();
    ArrayList arrSubDirList = null;
    ArrayList arrTemp = null;
    File newFile = null;
    try
    {
      Log.debugLog(className, "ListProjectSubproject", "", "", "method called");

      if (filePath.equals(""))
        return null;

      newFile = new File(filePath);
      if (!newFile.exists())
        return null;

      arrTemp = getProjectSubProjectList(newFile);
      if (arrTemp == null)
        return null;

      for (int i = 0; i < arrTemp.size(); i++)
      {
        String str = arrTemp.get(i).toString();
        newFile = new File(filePath + "/" + str);
        arrSubDirList = getProjectSubProjectList(newFile);

        for (int j = 0; j < arrSubDirList.size(); j++)
        {
          str = arrTemp.get(i).toString() + "/" + arrSubDirList.get(j);
          arr.add(str);
        }
      }

      return arr;
    }
    catch (Exception e)
    {
      // e.printStackTrace();
      Log.stackTraceLog(className, "ListProjectSubproject", "", "", "Exception = ", e);
      return null;
    }
  }

  // This filter only returns directories
  FileFilter fileFilter = new FileFilter()
  {
    //@Override
    @Override
    public boolean accept(File file)
    {
      return file.isDirectory();
    }
  };

  // function for getting sub directories of directory.
  public ArrayList getProjectSubProjectList(File dirFile)
  {
    try
    {
      ArrayList arrDirList = new ArrayList();
      if (dirFile == null || !dirFile.isDirectory())
        return null;

      File tempDir[] = dirFile.listFiles(fileFilter);
      for (File dir : tempDir)
      {
        arrDirList.add(dir.getName());
        // System.out.println("files ==" + dir.getName());
      }

      return arrDirList;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  // Method returns project/subproject of Testsuite directory.
  public ArrayList getTestSuiteProjectSubproject(String userName)
  {
    projectSubprojectList = getProjectSubProjects(userName);
    if (projectSubprojectList == null)
    {
      projectSubprojectList = new ArrayList();
      // projectSubprojectList.add("default/default");
    }
    return projectSubprojectList;
  }

  // Method returns project/subproject of TestCase directory.
  public ArrayList getTestCaseProjectSubproject(String userName)
  {
    projectSubprojectList = getProjectSubProjects(userName);
    if (projectSubprojectList == null)
    {
      projectSubprojectList = new ArrayList();
      // projectSubprojectList.add("default/default");
    }
    return projectSubprojectList;
  }

  // shows all the project/subprojects list.
  public ArrayList getProjectSubProjects(String userName)
  {
    Log.debugLog(className, "getProjectSubProjects", "", "", "Method called.");
    String[][] arrData = null;
    ArrayList arrList = new ArrayList();
    try
    {
      strCmdName = NSU_SHOW_PROJECTS;
      strCmdArgs = " -u " + userName + " -e";

      Vector vecCmdOutput = new Vector();
      boolean isQueryStatus = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, "netstorm", null);
      if (!isQueryStatus)
      {
        return arrList;
      }
      arrData = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "");
      if (arrData != null)
      {
        for (int i = 0; i < arrData.length; i++)
        {
          arrList.add(arrData[i][1] + "/" + arrData[i][2]);
        }
      }

      return arrList;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return arrList;
    }
  }

  public ArrayList<String> getAllCheckProfiles(String projectSubProject)
  {
    Log.debugLog(className, "getAllCheckProfiles", "", "", "method called for projectSubProject: "+projectSubProject);
    ArrayList<String> arrayList = new ArrayList<String>();
    try
    {
      String profilePath = Config.getWorkPath() + "/checkprofile/"+projectSubProject + "/";
      File folder = new File(profilePath);
      File[] listOfFiles = folder.listFiles();
      String files;
      for (int i = 0; i < listOfFiles.length; i++)
      {
        if (listOfFiles[i].isFile())
        {
          files = listOfFiles[i].getName();
          if(files.endsWith(".cprof"))
          {
            arrayList.add(files.substring(0, files.lastIndexOf(".")));
          }
        }
      }
    }
    catch (Exception ex)
    {
      Log.errorLog(className, "getAllProfiles", "", "", "Exception in getting profile names - " + ex);
    }

    return arrayList;
  }

  public String getCheckProfileData(String profileName)
  {
    try
    {
      File file = new File(Config.getWorkPath() + "/webapps/CheckProfile/" +profileName);
      if(!file.exists())
      {
        Log.debugLog(className, "getCheckProfileData", "", "", "File not perest.");
        return "";
      }
      FileInputStream fis = new FileInputStream(file);
      byte b[] = new byte[(int)file.length()];
      fis.read(b);
      StringBuffer buff = new StringBuffer();
      for(int i = 0; i < b.length; i++)
        buff.append((char)b[i]);
      fis.close();

      return buff.toString();
    }
    catch(Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "getCheckProfileData", "", "", "", e);
    }

    return "";
  }

  public static void main(String[] args)
  {
    /*System.out.println("Inside main");
    try
    {
      TestSuiteBean ts = new TestSuiteBean();
      TestSuiteBean.TestCaseCacheData testCaseCacheData = new TestSuiteBean.TestCaseCacheData();
      testCaseCacheData.setIterationTabData("Hello how are you");
      testCaseCacheData.setCheckStatusTabData("I am fine");
      testCaseCacheData.setScenarioTabData("Scenario ABC 123");
      testCaseCacheData.setPreTestTabData("pre test file");
      testCaseCacheData.setPostTestTabData("");
      testCaseCacheData.setScenarioName("t1");
      testCaseCacheData.setScenarioConfFileData("hi how are yousdsdfdgf sfdsgfdsgf");
      testCaseCacheData.setScenarioDescription("sdfgdsafsfgds.......................   sdsfd");
      ts.PrepareConfFileData(testCaseCacheData);
      // ts.CreateScenarioConfFile(testCaseCacheData);
      // testCaseCacheData.setScenarioName("");
      ts.GetScenarioConfFileData(testCaseCacheData);
      System.out.println("data================" + testCaseCacheData.getScenarioConfFileData());

      String arr[][] = ts.getAllScenarioFile("-A");
      // System.out.println("length of scenarios===" + arrScen.length);
      // testCaseCacheData.printName();
      // Vector v = ts.readFile("G:\\GraphPanel.java");
      // System.out.println(v.size());
      // ArrayList arrlist = ts.getAllTestCaseFile();
      // System.out.println("total files====" + arrlist.size());
      // for (int i = 0; i < arrlist.size(); i++)
      // {
      // System.out.println("elements are :==" + arrlist.get(i));
      // }

      // String arr[][] = ts.getAllTestSuiteFile("");
      System.out.println(arr.length);
      for (int i = 0; i < arr.length; i++)
      {
        for (int j = 0; j < arr[0].length; j++)
        {
          System.out.print(arr[i][j] + " ");

        }
        System.out.println();
      }

      File f = new File("C:\\home\\netstorm\\work\\webapps\\netstorm\\WEB-INF\\classes\\abc.conf");

      System.out.println("file path ===" + f.getAbsolutePath());
      // String str[] = new String[2];
      ArrayList arrlist = new ArrayList();

      arrlist.add("TEST_CASE_NAME Sample_test_case3 Abort");
      arrlist.add("TEST_CASE_NAME Sample_test_case4 Continue");

      // ts.SaveTestSuiteFileData("awq", arrlist, "kkkk");
      // ts.getAllTestCaseOfTestSuite("adc");

      // testCaseCacheData = ts.GetTestCaseData("aa");
      System.out.println(" Getting Iteration file data = " + testCaseCacheData.getIterationTabData());
      System.out.println("Getting Pre Test File data = " + testCaseCacheData.getPreTestTabData());
      System.out.println("Getting Post Test File data = " + testCaseCacheData.getPostTestTabData());
      System.out.println("Getting CheckStatus Test File data = " + testCaseCacheData.getPreTestTabData());
      System.out.println("Getting TestCase.conf Test File data = " + testCaseCacheData.getPreTestTabData());

      // String stra = ts.SaveTestCaseData("sdds", testCaseCacheData, "kanchan");
      // System.out.println("stra===" + stra);

      System.out.println("preData========" + testCaseCacheData.getIterationTabData());
      System.out.println(GetStringOfArray(testCaseCacheData.getIterationTabData().split("\n")));

      // ts.DeleteTestSuiteFile("kkk");
      // ts.DeleteTestCaseFile("what");\

    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
     */
    TestSuiteBean ob = new TestSuiteBean();
    //String str = "if [ SD_TotalSuccConn > 20 ];then \r\n STATUS = PASS \r\n if [ SD_TotalSuccConn > 20 ];then \r\n STATUS = PASS";
    String str = "if ( ${TCP Connections Success} < 100 AND ${TCP Connections Total} != ${TCP Connections Total} OR ${Connection Pass Percentage} < 5000 ) \n    STATUS = PASS\nelse\n    STATUS = FAIL\n    exit 3";
    System.out.println(ob.convertCheckStatusToShellForm(str));
    System.out.println("------------------------------");
    String str2 = "#!/bin/bash\nif [ $(echo \"$SD_TotalSuccConn < 100\" | bc ) -eq 1 -a $(echo \"$SD_TotalConn != $SD_TotalConn\" | bc ) -eq 1 -o $(echo \"$SD_TotalPassPct < 5000\" | bc ) -eq 1 ];then\n    STATUS = PASS\nelse\n    STATUS = FAIL\n    exit 3\nfi\nexit 0";
    //String str2 = "#!/bin/bash\nif [ $(echo \"$SD_TotalConn < 10.9\" | bc ) -eq 1 -a $SD_TotalSuccURL == $SD_TcpRx -o $(echo \"$SD_URLHitsPerSec != 122.6\" | bc ) -eq 1 -a $(echo \"$SD_EthRx <= 2324.7\" | bc ) -eq 1 ];then\n    STATUS = PASS\nelse\n    STATUS = FAIL\nfi\nexit 0";
    System.out.println(ob.convertCheckStatusToGUIForm(str2));
  }

  /**
   * This function add the action string to preTestSuit or postTestSuite.
   * action String is added just before main function closing braces.
   * for example:
   * input:                                                  output:
   * testCaseData is-
   * #!/bin/bash                                             #!/bin/bash
   * #mode: GUI                                              #mode: GUI
   * source $NS_WDIR/bin/ts_function_lib                     source $NS_WDIR/bin/ts_function_lib
   * function main()                                         function main()
   * {                                                       {
   * }                                                         start_service NO 'asa'
   * main                                                    }
   * exit 0                                                  main
   *                                                         exit 0
   * and input : actionStr- start_service NO 'asa'
   * @param testCaseData
   * @param actionStr
   * @return
   */
  public static String addActionToTestSuite(String testCaseData, String actionStr, String preOrPost)
  {
    try
    {
      String result = "";
      String arrTestSuitData[] = testCaseData.split("\n");
      boolean mainFound = false;

      if (!testCaseData.trim().equals(""))
      {
        String strLine = "";
        for (int i =0; i < arrTestSuitData.length; i++)
        {
          strLine = arrTestSuitData[i];
          if (!mainFound && strLine.trim().endsWith("exit 0"))
            continue;
          if (strLine.trim().equals("function main()"))
            mainFound = true;
          if (mainFound && strLine.trim().equals("}"))
          {
            result += "  " + actionStr + "\n";
            result += strLine + "\n";
          }
          else
            result += strLine + "\n";
        }
      }
      else
      {
        result += "#!/bin/bash" + "\n";
        result += "#mode: GUI" + "\n";
        result += "source $NS_WDIR/bin/ts_function_lib" + "\n";
        result += "function main()" + "\n";
        result += "{" + "\n";
        result += "  echo \" Starting "+ preOrPost +" test suite.\" " + "\n";
        result += "  " + actionStr + "\n";
        result += "}" + "\n";
        result += "main" + "\n";
        result += "exit 0" + "\n";
      }

      if (!mainFound && !testCaseData.trim().equals(""))
      {
        result += "#mode: GUI" + "\n";
        result += "source $NS_WDIR/bin/ts_function_lib" + "\n";
        result += "function main()" + "\n";
        result += "{" + "\n";
        result += "  echo \" Starting "+ preOrPost +" test suite.\" " + "\n";
        result += "  " + actionStr + "\n";
        result += "}" + "\n";
        result += "main" + "\n";
        result += "exit 0" + "\n";
      }
      return result;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * This function returns the list of added action string between function main() {}
   * for example:
   * input testCaseData-
   * #!/bin/bash
   * #mode: GUI
   * source $NS_WDIR/bin/ts_function_lib
   * function main()
   * {
   *   start_service NO 'asa'
   *   stop_service NS 'dtp'
   * }
   * main
   * exit 0
   * output: [start_service NO 'asa', stop_service NS 'dtp']
   * @param testCaseData
   * @return
   */
  public static ArrayList<String> getAddedActionList(String testCaseData)
  {
    try
    {
      ArrayList<String> actionList = new ArrayList<String>();
      if (testCaseData.trim().equals(""))
      {
        return actionList;
      }
      String arrTestCaseData[] = testCaseData.split("\n");
      String strLine = "";
      boolean mainFound = false;
      for(int i = 0; i < arrTestCaseData.length; i++)
      {
        strLine = arrTestCaseData[i];
        if (mainFound && strLine.trim().equals("}"))
          mainFound = false;
        if (mainFound && !strLine.trim().equals("{") && !strLine.trim().equals("}"))
        {
          actionList.add(strLine.trim());
        }
        if (strLine.trim().equals("function main()"))
          mainFound = true;
      }
      return actionList;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * This function format the input list of action string to show in gui form
   * for example:
   * input actionList = [start_service NO 'asa', stop_service NS 'dtp']
   * output formatedActionList: [Action: start_service, Server: NO, Service string:'asa',
   *                             Action: stop_service, Server: NS, Service string: 'dtp']
   * @param actionList
   * @return
   */
  public static ArrayList<String> formatTheAddedActionList(ArrayList<String> actionList)
  {
    try
    {
      if (actionList != null && actionList.size() > 0)
      {
        for (int i = 0; i < actionList.size(); i++)
        {
          if (actionList.get(i).trim().startsWith("copy_file "))
          {
            String[] data = actionList.get(i).trim().split(" ");
            String formatStr = "Action: " + data[0] + ", Server: " + data[1] + ", Source: " + data[2] + ", Destination: " + data[3];
            actionList.remove(i);
            actionList.add(i, formatStr);
          }
          else if (actionList.get(i).trim().startsWith("start_service ") || actionList.get(i).trim().startsWith("stop_service ") || actionList.get(i).trim().startsWith("remove_file "))
          {
            String[] data = actionList.get(i).trim().split(" ");
            String formatStr = "";
            if (actionList.get(i).trim().startsWith("remove_file "))
            {
              formatStr = "Action: " + data[0] + ", Server: " + data[1] + ", File_path: ";
              for (int j = 2; j < data.length; j++)
                formatStr += data[j] + " ";
            }
            else
            {
              data[0] = data[0].trim().equals("start_service") ? "start_command" : "stop_command";

              formatStr = "Action: " + data[0] + ", Server: " + data[1] + ", Service_string: ";
              for (int j = 2; j < data.length; j++)
              {
                if (j == 2 || j == data.length - 1)
                  formatStr += data[j].replace("'", "") + " "; // remove the '' from srvice String.
                else
                  formatStr += data[j] + " ";
              }
            }
            actionList.remove(i);
            actionList.add(i, formatStr);
          }
          else if(actionList.get(i).trim().startsWith("remote_exec "))
          {
            String[] data = actionList.get(i).trim().split(" ");
            String formatStr = "";
            formatStr = "Action: " + "remote_exceution" + ", Server: " + data[1] + ", Remote Command: ";
            for (int j = 2; j < data.length; j++)
            {
              if (j == 2 || j == data.length - 1)
                formatStr += data[j].replace("'", "") + " "; // remove the '' from srvice String.
              else
                formatStr += data[j] + " ";
            }
            actionList.remove(i);
            actionList.add(i, formatStr);
          }
          else if (actionList.get(i).trim().startsWith("sudo_stop_service ") || actionList.get(i).trim().startsWith("sudo_start_service "))
          {
            String[] data = actionList.get(i).trim().split(" ");
            String formatStr = "Action: " + data[0] + ", Server: " + data[1] + ", Sudo Command: " + data[2] + ", Service string: ";
            for (int j = 3; j < data.length; j++)
            {
              if (j == 3 || j == data.length - 1)
                formatStr += data[j] + " ";
              else
                formatStr += data[j] + " ";
            }
            actionList.remove(i);
            actionList.add(i, formatStr);
          }
          else if (actionList.get(i).trim().startsWith("upload_file ") || actionList.get(i).trim().startsWith("download_file "))
          {
            String[] data = actionList.get(i).trim().split(" ");
            String formatStr = "Action: " + data[0] + ", Server: " + data[1] + ", Remote Path: " + data[2] + ", Local Path: " + data[3];
            actionList.remove(i);
            actionList.add(i, formatStr);
          }
        }
      }
      return actionList;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * This function accept the indexes of action String lines which have to remove from preTestSuite or postTestsuite.
   * For example:
   * input testCaseData-
   * #!/bin/bash
   * #mode: GUI
   * source $NS_WDIR/bin/ts_function_lib
   * function main()
   * {
   *   start_service NO 'asa'                       idx--> 0
   *   stop_service NS 'dtp'                        idx--> 1
   *   start_service NO 'asa'                       idx--> 2
   *   stop_service NS 'dtp'                        idx--> 3
   *   stop_service NS 'stop1'                      idx--> 4
   * }
   * main()
   * exit 0
   *
   * input actions = [2, 4]
   * output:
   * #!/bin/bash
   * #mode: GUI
   * source $NS_WDIR/bin/ts_function_lib
   * function main()
   * {
   *   start_service NO 'asa'
   *   stop_service NS 'dtp'
   *   stop_service NS 'dtp'
   * }
   * main()
   * exit 0
   * @param testCaseData
   * @param actions
   * @return
   */
  public static String removeActionList(String testCaseData, String actions[])
  {
    String result = "";
    try
    {
      String arrTestCaseData[] = testCaseData.split("\n");
      ArrayList<String> fileData = new ArrayList<String>();
      int index = 0;
      String strLine = "";
      boolean mainFound = false;
      for(int i = 0; i < arrTestCaseData.length; i++)
      {
        strLine = arrTestCaseData[i];
        fileData.add(strLine);
        if (strLine.trim().equals("function main()"))
          mainFound = true;
        if (mainFound && strLine.trim().equals("{"))
        {
          index = fileData.size();
        }
      }

      // remove the selected records
      for (int i = 0; i < actions.length; i++)
      {
        fileData.remove(index + Integer.parseInt(actions[i]) - i);
      }

      for(int i = 0; i < fileData.size(); i++)
      {
        result += fileData.get(i) + "\n";
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return result;
  }

  /**
   * This function accept the action and index of action String lines which have to be updated.
   * For example:
   * input testCaseData-
   * #!/bin/bash
   * #mode: GUI
   * source $NS_WDIR/bin/ts_function_lib
   * function main()
   * {
   *   start_service NO 'asa'                       idx--> 0
   *   stop_service NS 'dtp'                        idx--> 1
   *   start_service NO 'asa'                       idx--> 2
   *   stop_service NS 'dtp'                        idx--> 3
   *   stop_service NS 'stop1'                      idx--> 4
   * }
   * main()
   * exit 0
   *
   * input action = start_service NO 'asaUpdated' and idx= 2
   * output:
   * #!/bin/bash
   * #mode: GUI
   * source $NS_WDIR/bin/ts_function_lib
   * function main()
   * {
   *   start_service NO 'asa'
   *   stop_service NS 'dtp'
   *   start_service NO 'asaUpdated'
   *   stop_service NS 'dtp'
   *   stop_service NS 'stop1'
   * }
   * main()
   * exit 0
   *
   */
  public static String updateActioList(String testCaseData, String action, int idx)
  {
    String result = "";
    try
    {
      String arrTestCaseData[] = testCaseData.split("\n");
      ArrayList<String> fileData = new ArrayList<String>();
      int index = 0;
      String strLine = "";
      boolean mainFound = false;
      for(int i = 0; i < arrTestCaseData.length; i++)
      {
        strLine = arrTestCaseData[i];
        fileData.add(strLine);
        if (strLine.trim().equals("function main()"))
          mainFound = true;
        if (mainFound && strLine.trim().equals("{"))
        {
          index = fileData.size();
        }
      }

      fileData.remove(index + idx);
      fileData.add(index + idx, action);

      for(int i = 0; i < fileData.size(); i++)
      {
        result += fileData.get(i) + "\n";
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    return result;
  }

  public static String moveUpAndDownAction(String testCaseData, int moveFrom, int moveTo)
  {
    String result = "";
    try
    {
      String arrTestCaseData[] = testCaseData.split("\n");
      ArrayList<String> fileData = new ArrayList<String>();
      int index = 0;
      String strLine = "";
      boolean mainFound = false;
      for(int i = 0; i <  arrTestCaseData.length; i++)
      {
        strLine = arrTestCaseData[i];
        fileData.add(strLine);
        if (strLine.trim().equals("function main()"))
          mainFound = true;
        if (mainFound && strLine.trim().equals("{"))
        {
          index = fileData.size();
        }
      }

      String action = fileData.get(index + moveFrom);
      fileData.remove(index + moveFrom);
      fileData.add(index + moveTo, action);

      for(int i = 0; i < fileData.size(); i++)
      {
        result += fileData.get(i) + "\n";
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return result;
  }

  /**
   * This function convert the Gui condition exp to shell condtion exp
   * for example
   * input parameter: String
   * ${TCP Connections Total} < 20 AND ${Connection Pass Percentage} < 10
   * output parameter: String
   * $SD_TotalConn < 20 -a $SD_TotalPassPct < 10
   * @param conditionExp
   * @return
   */
  public String convertConditonExpToShellForm(String conditionExp)
  {
    String result = "";
    try
    {
      ArrayList<String> varList = new ArrayList<String>();
      ArrayList<String> operList = new ArrayList<String>();
      String condOper = "<>!=";
      int length = conditionExp.length();
      String tmpStr = "";
      for(int i = 0; i < length; i++)
      {
        if(condOper.contains(conditionExp.charAt(i)+""))
        {
          varList.add(tmpStr.trim());
          if(i+1 < length && condOper.contains(conditionExp.charAt(i+1)+""))
          {
            operList.add(""+conditionExp.charAt(i)+conditionExp.charAt(i+1));
            i++;
          }
          else
            operList.add(""+conditionExp.charAt(i));
          tmpStr = "";
        }
        else if(conditionExp.charAt(i) == 'A' && i-1 > 0 && conditionExp.charAt(i-1) == ' ' && i+3 < length && conditionExp.charAt(i+1) == 'N' && conditionExp.charAt(i+2) == 'D' && conditionExp.charAt(i+3) == ' ')
        {
          varList.add(tmpStr.trim());
          operList.add("AND");
          i = i+3;
          tmpStr= "";
        }
        else if(conditionExp.charAt(i) == 'O' && i-1 > 0 && conditionExp.charAt(i-1) == ' ' && i+2 < length && conditionExp.charAt(i+1) == 'R' && conditionExp.charAt(i+2) == ' ')
        {
          varList.add(tmpStr.trim());
          operList.add("OR");
          i = i+2;
          tmpStr= "";
        }
        else
          tmpStr += conditionExp.charAt(i);
      }
      varList.add(tmpStr.trim());

      for(int i = 0; i < operList.size(); i++)
      {
        if(operList.get(i).equals("AND") || operList.get(i).equals("OR"))
        {
          if(operList.get(i).equals("AND"))
            result += " -a ";
          else
            result += " -o ";
        }
        else
        {
          String varLeft1 = varList.get(i);
          String varRight2 = varList.get(i+1);
          if(varLeft1.startsWith("${"))
          {
            int idx1 = varLeft1.indexOf("{");
            int idx2 = varLeft1.lastIndexOf("}");
            varLeft1 = "$" + getVarNameForShell(varLeft1.substring(idx1+1, idx2));
          }
          if(varRight2.startsWith("${"))
          {
            int idx1 = varRight2.indexOf("{");
            int idx2 = varRight2.lastIndexOf("}");
            varRight2 = "$" + getVarNameForShell(varRight2.substring(idx1+1, idx2));
          }
          result += "$(echo \""+ varLeft1 + " " + operList.get(i) + " " + varRight2 + "\" | bc ) -eq 1";
        }
      }

      /*for(int i = 0; i < varList.size(); i++)
      {
        //var varName in shell Form
        if(varList.get(i).trim().startsWith("${"))
        {
          int idx1 = varList.get(i).indexOf("{");
          int idx2 = varList.get(i).lastIndexOf("}");
          result += "$" + getVarNameForShell(varList.get(i).substring(idx1+1, idx2));
        }
        else
        {
          //result += varList.get(i);
          String value = varList.get(i);
          if(isNumeric(value))
            result += varList.get(i);
          else
          {
            result = result.substring(0, result.lastIndexOf("$"));
            int idx1 = varList.get(i-1).indexOf("{");
            int idx2 = varList.get(i-1).lastIndexOf("}");
            String varName = "$" + getVarNameForShell(varList.get(i-1).substring(idx1+1, idx2));
            result += "$(echo \""+ varName + " " + operList.get(i-1) + " " + varList.get(i) + "\" | bc ) -eq 1";
          }
        }

        //add operator name in shell form
        if(i != varList.size()-1)
        {
          if(operList.get(i).equals(">"))
            result += " -gt ";
          else if(operList.get(i).equals("<"))
            result += " -lt ";
          else if(operList.get(i).equals(">="))
            result += " -ge ";
          else if(operList.get(i).equals("<="))
            result += " -le ";
          else if(operList.get(i).equals("AND"))
            result += " -a ";
          else if(operList.get(i).equals("OR"))
            result += " -o ";
          else
            result += " "+operList.get(i) +" ";
        }
      }*/
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    return result;
  }

  /**
   * This function convert the gui expression to shell form
   * input parameter: String of expression in Gui form
   * output parametr: String of expression in shell form
   * for example:
   * input parameter is                               output parametr
   * if(${TCP Connections Total} > 20)                if($SD_TotalConn > 20)
   *   STATUS=PASS                                      STATUS=PASS
   * else                                             else
   * STATUS=FAIL                                        STATUS=EAIL
   *                                                  fi
   * @param chkStatus
   * @return
   */
  public String convertCheckStatusToShellForm(String chkStatus)
  {
    Log.debugLog(className, "convertCheckStatusToShellForm", "", "", "method called for str: "+chkStatus);
    String result = "";
    try
    {
      String data[] = chkStatus.split("\n");
      boolean ifConditionFound = false;
      boolean elseFound = false;
      String condExp = "";

      result = "#!/bin/bash" + "\n"; //add at the beginning
      for(int i = 0; i < data.length; i++)
      {
        if(data[i].trim().startsWith("if(") || data[i].trim().startsWith("if "))
        {
          if(ifConditionFound)
            result += "fi" + "\n";

          int idx1 = data[i].indexOf("(");
          int idx2 = data[i].lastIndexOf(")");
          String subStr = data[i].substring(idx1+1, idx2);
          condExp = subStr.replace("$", "").replace("{", "").replace("}", "");
          result += "if [ " + convertConditonExpToShellForm(subStr) + " ];then" + "\n";
          ifConditionFound = true;
          elseFound = false;
        }
        else if(data[i].trim().equals("else"))
        {
          result += "else" + "\n";
          elseFound = true;
        }
        else if(data[i].trim().startsWith("else "))
        {
          int idx1 = data[i].indexOf("(");
          int idx2 = data[i].lastIndexOf(")");
          String subStr = data[i].substring(idx1+1, idx2);
          condExp = subStr.replace("$", "").replace("{", "").replace("}", "");
          result += "else if [ " + convertConditonExpToShellForm(subStr) + " ];then" + "\n";
          ifConditionFound = true;
          elseFound = false;
        }
        else if(data[i].trim().startsWith("STATUS"))
        {
          if(!elseFound)
          {
            if(data[i].trim().equals("STATUS=FAIL AND RUN POST TEST SCRIPT"))
            {
              result += "    STATUS=FAIL" + "\n";
              result += "    echo \" Test $STATUS due to condition '"+ condExp +"' satisfied. \" " + "\n";
              result += "    exit 3" +"\n";
            }
            else
            {
              result += data[i] + "\n";
              result += "    echo \" Test $STATUS due to condition '"+ condExp +"' satisfied. \" " + "\n";
            }
          }
          else
          {
            if(data[i].trim().equals("STATUS=FAIL AND RUN POST TEST SCRIPT"))
            {
              result += "    STATUS=FAIL" + "\n";
              result += "    echo \" Test $STATUS due to condition '"+ condExp +"' is not satisfied. \" " + "\n";
              result += "    exit 3" +"\n";
            }
            else if(data[i].trim().equals("STATUS=FAIL"))
            {
              result += "    STATUS=FAIL" + "\n";
              result += "    echo \" Test $STATUS due to condition '"+ condExp +"' is not satisfied. \" " + "\n";
              result += "    exit 1" +"\n";
            }
            else
            {
              result += data[i] + "\n";
              result += "    echo \" Test $STATUS due to condition '"+ condExp +"' is not satisfied. \" " + "\n";
            }
          }
        }
        else
          result += data[i] + "\n";
      }
      if(ifConditionFound)
        result += "fi" + "\n";
      result += "exit 0"; //add at the last

      //result = result.replace("AND RUN POST TEST SCRIPT", "\n    exit 3");
    }
    catch(Exception e)
    {
      Log.errorLog(className, "convertCheckStatusToShellForm", "", "", e.getMessage());
      e.printStackTrace();
    }
    return result.trim();
  }

  /**
   * This function convert the shell condition exp to gui condtion exp
   * for example
   * input parameter: String
   * SD_TotalConn < 20 -a SD_TotalPassPct < 10
   * output parameter: String
   * ${TCP Connections Total} < 20 AND ${Connection Pass Percentage} < 10
   * @param conditionExp
   * @return
   */
  public String convertConditonExpToGUIForm(String conditionExp)
  {
    String result = "";
    try
    {
      ArrayList<String> varList = new ArrayList<String>();
      ArrayList<String> operList = new ArrayList<String>();
      int length = conditionExp.length();
      String tmpStr = "";
      for(int i = 0; i < length; i++)
      {
        if(conditionExp.charAt(i) == '-')
        {
          if(i-1 > 0 && conditionExp.charAt(i-1) == ' ' && i+2 < length && conditionExp.charAt(i+1) == 'a' && conditionExp.charAt(i+2) == ' ')
          {
            varList.add(tmpStr.trim());
            operList.add("AND");
            i = i+2;
            tmpStr = "";
          }
          else if(i-1 > 0 && conditionExp.charAt(i-1) == ' ' && i+2 < length && conditionExp.charAt(i+1) == 'o' && conditionExp.charAt(i+2) == ' ')
          {
            varList.add(tmpStr.trim());
            operList.add("OR");
            i = i+2;
            tmpStr = "";
          }
          /*else if(i-1 > 0 && conditionExp.charAt(i-1) == ' ' && i+3 < length && conditionExp.charAt(i+1) == 'g' && conditionExp.charAt(i+2) == 't' && conditionExp.charAt(i+3) == ' ')
          {
            varList.add(tmpStr.trim());
            operList.add(">");
            i = i+3;
            tmpStr = "";
          }
          else if(i-1 > 0 && conditionExp.charAt(i-1) == ' ' && i+3 < length && conditionExp.charAt(i+1) == 'g' && conditionExp.charAt(i+2) == 'e' && conditionExp.charAt(i+3) == ' ')
          {
            varList.add(tmpStr.trim());
            operList.add(">=");
            i = i+3;
            tmpStr = "";
          }
          else if(i-1 > 0 && conditionExp.charAt(i-1) == ' ' && i+3 < length && conditionExp.charAt(i+1) == 'l' && conditionExp.charAt(i+2) == 't' && conditionExp.charAt(i+3) == ' ')
          {
            varList.add(tmpStr.trim());
            operList.add("<");
            i = i+3;
            tmpStr = "";
          }
          else if(i-1 > 0 && conditionExp.charAt(i-1) == ' ' && i+3 < length && conditionExp.charAt(i+1) == 'l' && conditionExp.charAt(i+2) == 'e' && conditionExp.charAt(i+3) == ' ')
          {
            varList.add(tmpStr.trim());
            operList.add("<=");
            i = i+3;
            tmpStr = "";
          }*/
        }
        /*else if("!=".contains(conditionExp.charAt(i)+"") && i+1 < length && "!=".contains(conditionExp.charAt(i+1)+"")) // this is handle case for != or ==
        {
          varList.add(tmpStr.trim());
          operList.add(""+conditionExp.charAt(i) + conditionExp.charAt(i+1));
          i = i+1;
          tmpStr = "";
        }*/
        else
          tmpStr += conditionExp.charAt(i);
      }
      varList.add(tmpStr.trim());

      for(int i = 0; i < varList.size(); i++)
      {
        int idx1 =  varList.get(i).indexOf("\"");
        int idx2 = varList.get(i).lastIndexOf("\"");
        String tmp[] = varList.get(i).substring(idx1+1, idx2).trim().split(" ");
        String varLeft = "${" + getVarNameForGui(tmp[0].substring(1, tmp[0].length())) +"}";
        String varRight = tmp[2];
        if(varRight.startsWith("$"))
          varRight = "${" + getVarNameForGui(tmp[2].substring(1, tmp[2].length())) +"}";
        result += varLeft + " " + tmp[1] + " " + varRight;

        if(i != varList.size()-1)
          result += " " + operList.get(i) + " ";
      }

      /*for(int i = 0; i < varList.size(); i++)
      {
        //add varName in GUI Form
        if("0123456789".contains(varList.get(i).trim().charAt(0)+""))
        {
          result += varList.get(i);
        }
        else
        {
          if(varList.get(i).startsWith("$(echo"))
          {
            if(i != varList.size()-1 && (operList.get(i).equals("==") || operList.get(i).equals("!=")))
            {
              int idx = varList.get(i).lastIndexOf("$");
              String varName = varList.get(i).substring(idx+1, varList.get(i).length());
              result += "${" +getVarNameForGui(varName) + "}" + " " + operList.get(i) + " ";
              i = i+1;
              result += varList.get(i).substring(0, varList.get(i).indexOf("\""));
            }
            else
            {
              String exp = varList.get(i).substring(varList.get(i).indexOf("\"")+1, varList.get(i).lastIndexOf("\"")).trim();
              StringTokenizer stTemp = new StringTokenizer(exp, "<>!=");
              String strToken = stTemp.nextToken().trim();
              String varName = "${" + getVarNameForGui(strToken.substring(1, strToken.length())) + "}";
              exp = exp.replace(strToken, varName);
              result += exp;
            }
          }
          else
            result += "${" + getVarNameForGui(varList.get(i).substring(1, varList.get(i).trim().length())) + "}";
        }

        //add operator name in GUI form
        if(i != varList.size()-1)
        {
          result += " "+operList.get(i) +" ";
        }
      }*/
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    return result;
  }

  /**
   * This function convert the expression shell for to gui form
   * input paramater: String of expression in shell form
   * output parameter: String of expression in Gui form
   * for example:
   * input parameter                                 output parametr
   * if(SD_TotalConn > 20)                           if(${TCP Connections Total} > 20)
   *   STATUS=PASS                                     STATUS=PASS
   * else                                            else
   *   STATUS=FAIL                                     STATUS=EAIL
   * fi
   * @param chkStatus
   * @return
   */
  public String convertCheckStatusToGUIForm(String chkStatus)
  {
    Log.debugLog(className, "convertCheckStatusToGUIForm", "", "", "method called for str: "+chkStatus);
    String result = "";
    try
    {
      String data[] = chkStatus.split("\n");
      //first remove the starting #!/bin/bash and ending exit 0 from input string
      for(int i = 0; i < data.length; i++)
      {
        if(data[i].trim().equals("#!/bin/bash"))
        {
          data[i] = "";
          break;
        }
      }

      for(int i = data.length-1; i >= 0; i--)
      {
        if(data[i].trim().equals("exit 0"))
        {
          data[i] = "";
          break;
        }
      }

      for(int i = 0; i < data.length; i++)
      {
        if(data[i].trim().startsWith("if[") || data[i].trim().startsWith("if "))
        {
          int idx1 = data[i].indexOf("[");
          int idx2 = data[i].lastIndexOf("]");
          String subStr = data[i].substring(idx1+1, idx2);
          result += "if ( " + convertConditonExpToGUIForm(subStr) + " )" + "\n";
        }
        else if(data[i].trim().startsWith("else if") || data[i].trim().startsWith("else "))
        {
          int idx1 = data[i].indexOf("[");
          int idx2 = data[i].lastIndexOf("]");
          String subStr = data[i].substring(idx1+1, idx2);
          result += "else if ( " + convertConditonExpToGUIForm(subStr) + " )" + "\n";
        }
        else if(data[i].trim().equals("fi") || data[i].trim().startsWith("echo") || data[i].trim().equals("exit 1"))
          continue;
        else if(data[i].trim().equals("exit 3"))
        {
          result = result.trim() + " AND RUN POST TEST SCRIPT" + "\n";
        }
        else
          result += data[i] + "\n";
      }
    }
    catch(Exception e)
    {
      Log.errorLog(className, "convertCheckStatusToGUIForm", "", "", e.getMessage());
      e.printStackTrace();
    }
    return result.trim();
  }

  public String[][] getCheckStatusVar()
  {
    return arrCheckStatusVar;
  }

  public String getVarNameForShell(String name)
  {
    for(int i = 0; i < arrCheckStatusVar.length; i++)
    {
      if(arrCheckStatusVar[i][0].equals(name))
        return arrCheckStatusVar[i][1];
    }
    return name;
  }

  public String getVarNameForGui(String name)
  {
    for(int i = 0; i < arrCheckStatusVar.length; i++)
    {
      if(arrCheckStatusVar[i][1].equals(name))
        return arrCheckStatusVar[i][0];
    }
    return name;
  }

  //it returns true for integer value otherwise false.
  public boolean isNumeric(String varName)
  {
    try
    {
      Integer.parseInt(varName);
      return true;
    }
    catch(Exception e)
    {
      return false;
    }
  }
}

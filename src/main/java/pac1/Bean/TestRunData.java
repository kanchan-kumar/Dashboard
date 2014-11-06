/**----------------------------------------------------------------------------
 * Name       TestRunData.java
 * Purpose    Common varibales, methods and keep time based test run data in memory
 *
 * @author    Ravi Sharma
 * @version   3.9.2.
 *
 * Modification History
 *
 *
 * Notes
 *  static variables are not serialized which is good as less data will be send.
 *---------------------------------------------------------------------------**/

package pac1.Bean;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TimeZone;
import java.util.Vector;

import pac1.Bean.GraphName.GraphNames;

public class TestRunData implements java.io.Serializable, Cloneable
{
  private static final long serialVersionUID = 1828378429004263921L;
  
  //used for logging purpose
  private static String className = "TestRunData";

  /**
   * Keeping Graph Names object. No need to serialized GraphNames Object so we are using as transient. As we are creating GraphNames object at client
   */
  public transient GraphNames graphNames = null;

  public transient GraphNames generatorGraphNames = null;
  
  
  public boolean isUpdateGeneratorTBRD = true;//Use to read data form rtgMessage.dat or just initialized object in NetCloud Partition mode

  public String strTimeBasedTestRunDataKey = "";

  // 0 - Not started, 1 - started, 2 - ended
  public int testRunState = 0;

  /**
   * Keeping testrun.gdf file content. Don't Make it transient/static. We need to serialized it
   */
  private Vector<String> vectGDF = new Vector<String>();
  private Vector<String> vectGenGDF = new Vector<String>();

  /**
   * Keeping content of testrun.pdf. Don't Make it transient/static. We need to serialized it
   */
  private Vector<String> vectPDF = new Vector<String>();
  private Vector<String> vectGenPDF = new Vector<String>();

  private ArrayList<NetCloudDTO> netcloudDtoList = null;

  // Keeping net cloud data into memory
  private Vector<String> vectNetCloudData = new Vector<String>();

  // Keeping content of summary.top file
  private String summary_top_line = null;

  public int config_rtg_max_data_samples_in_one_graph = 40;

  // Assigning default Value for low water mark.
  public int lowWaterMark = 3000;

  // Assigning default Value for high water mark.
  public int highWaterMark = 5000;

  // This used for target completion time
  public String targetCompTime = null;

  //This used for test run start time
  public String trStartTime = null;

  //Variable added to store scenario start Date
  public Date startDate = null;

  /**
   * TestRun start time stamp in milli - seconds (in the net storm machine time zone)
   */
  public long trStartTimeStamp;

  // TestRun time zone (in the net storm machine time zone)
  public TimeZone trTimeZone;

  // Assign default value for WAN_ENV keyword
  public int wanEnv = 0;

  // Assigning default value for REPORTING keyword
  public int reportingLevel = 1;

  // Assigning default value for READER_RUN_MODE keyword
  public int readerRunMode = 0;

  // Assigning default value for SHOW_INITIATED keyword
  public int showInitiated = 0;

  //Assigning default value for G_ENABLE_NETWORK_CACHE_STATS keyword
  public int netCacheApplied = 0;

  // It keep the scenario name
  public String scenarioName = "";

  // This used for test name
  public String testName = "";

  // This used for test duration
  public String testDuration = "";

  // This is used to check DEBUG_TRACE keyword avail in scenario or not
  public boolean debugTraceLogFlag = false;

  // This will hold the first base line TR number
  public int baseLineTR = -1;

  // This is used for baseline test run data taken form the baseline tracking file
  private ArrayList<String> baseLineTrackingFileData = null;

  // this is used for baseLine Scenario name.
  public String baseLineScenarioName = "";

  // This is used for storing NSport for testRun
  private int netstormPort = -1;

  // phase information contained in global.dat file
  public ArrayList arrListTestRunPhasesObj;

  // ADF file information used in dial chart
  public TestRunAdfInfo testRunAdfInfo = null;

  // Keeping test run number
  public int testNum = -1;

  // Keeping previous Test Run Number
  public long prevTestRunNum = -1;

  /**
   * This flag is to specify the testRunDataType (0 --> Normal Test Run Data, 1 --> Baseline Test Run Data, 2 --> Tracked Test Run Data), by default it is 0
   */
  public int testRunDataType = 0;
  private TestRunDataType testRunDataTypeObj;
  // Start & End Seq Number
  private long startSeq = -1;
  private long endSeq = -1;

  // Default granularity auto (-1)
  private int granularity = -1;

  // Number of visible panels
  public int numberOfDisplayPanels = 9;

  // Logged User Name
  public String userName = "";

  // true for off line and false for online mode
  private boolean testViewMode = false;

  // array for active graph numbers
  private GraphUniqueKeyDTO[] arrActiveGraphUniqueKeyDTO = null;

  // Assigning default value for ENABLE_SYNC_POINT keyword
  public int enableSynPoint = 0;

  // This is for dash board GUI to DDR Integration. Later we can do it at client.
  public ExecutionDrillDown executionDrillDown = null;

  // This is called from Transaction Summary Window to enable link.
  public String enableDrillDownReportLinkInTransWin = "";

  // it contains key as script name and value as object of syncTPSyncdata class
  public LinkedHashMap linkedHashMapForTransPageSync = new LinkedHashMap();

  // information of script and group
  public String[][] arrGroupScript = null;

  // Keeping properties of scenario globally
  public Properties globalScenarioProp = null;

  // Keeping time based test run data. The key is Test Run Data Type and value is TimeBasedTestRun.
  public HashMap<String, TimeBasedTestRunData> hMapTimeBasedTestRunData = new HashMap<String, TimeBasedTestRunData>();

  // True if netstorm.execution.enableAutoActivate is on
  public boolean isEnableAutoActivate = false;

  // Variable to indicate how many type data we can keep into the memory
  private int TIME_BASED_TEST_RUN_HASHMAP_MEMORY_LIMIT = 10;

  /**
   * This array list maintain the key order means which key first inserted into the hash map hMapTimeBasedTestRunData. It is used to remove time based test run from hash map hMapTimeBasedTestRunData.
   */
  public ArrayList<String> timeBasedTestRunDataHashMapKeys = new ArrayList<String>();

  // this is to read the user properties from *.prop files
  public UserProp objUserProp = null;

  public static String defaultTestRunDataView = "WholeScenario";

  // Keeping TestControlData object
  public TestControlData testCtrlData;

  private TimeBasedTestRunData timeBasedTestRunDataObj;

  // keeping error message flag
  public boolean bolError = false;

  // keeping warning message
  public boolean bolWarning = false;

  // Keeping warning message
  public String warningMsg = "";

  // keeping error message
  public String errorMsg = "";
  public long interval = -1;

  private int debugLogLevel = 0;

  // Used to identify the NDE Continuous mode.
  private boolean nDE_Continuous_Mode = false;

  // Keeping active Partition Directory Name
  private String activePartitionDirName = "";

  // Variable of DashboardConfig to keep all keyword values of config.ini of Dash board
  public DashboardConfig dashboardConfig = null;

  /* This variable is used for Getting net storm time EPOCH year from scenario file. */
  private String epochYear = "2014"; // Default.

  /*This variable contains the epoch time stamp. */
  private long epochTimeStamp = 0L;

  //keeping the value of hierarchical tree mode shown in GUI.
  private int hierarchicalMode = 0;

  private String topologyName = "";

  //Separator for vector type/Tier structure based graphs.
  private String vectorSeperator = "_";

  //Object of SessionReportData For Getting Data from different session files.
 // public transient SessionReportData sessionReportData = null;

  //contains the partition type of test run.
  private int testRun_Partition_Type = 0;

  //ArrayList for vector separator mapping between two TR's.
  private ArrayList<VectorMappingDTO> arrVectorMappingList = new ArrayList<VectorMappingDTO>();

  //This hash map keep the data for Icon and Color for node
  private HashMap<String, TreeIconColorDTO> hieararchicalTreeSettingData = new HashMap<String, TreeIconColorDTO>();

  //Derived DTO object containing Derived Graph request.
  private transient DerivedDataDTO derivedDataDTO = null;
    
  public TestRunData()
  {
    try
    {
      Log.debugLog(className, "TestRunData", "", "", "Default contructor Called.");
      initForConstructor();
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "TestRunData", "", "", "Exception - ", ex);
    }
  }

  //Initializing Test Run Data
  public TestRunData(TestRunDataType testRunDataTypeObj)
  {
    try
    {
      Log.debugLog(className, "TestRunData", "", "", "Method Called. testRunDataTypeObj = " + testRunDataTypeObj);
      this.testRunDataTypeObj = testRunDataTypeObj;
      initForConstructor();
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "TestRunData", "", "", "Exception - ", ex);
    }
  }

  //Creating Constructor for Test Run Data
  public TestRunData(int testNum, TestRunDataType testRunDataTypeObj)
  {
    try
    {
      Log.debugLog(className, "TestRunData", "", "", "testNum = " + testNum + ", testRunDataTypeObj = " + testRunDataTypeObj);

      this.testRunDataTypeObj = testRunDataTypeObj;
      this.testNum = testNum;
      if (graphNames == null)
        graphNames = new GraphNames(testNum);

      initForConstructor();
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "TestRunData", "", "", "Exception - ", ex);
    }
  }

  //Creating constructor for Test Run Data
  public TestRunData(int testRunDataType, int testNum, TestRunDataType testRunDataTypeObj)
  {
    try
    {
      Log.debugLog(className, "TestRunData", "", "", "Method Called. testRunDataType = " + testRunDataType + ", testNum = " + testNum + ", testRunDataTypeObj = " + testRunDataTypeObj);
      this.testRunDataTypeObj = testRunDataTypeObj;
      this.testNum = testNum;
      if (this.graphNames == null)
        this.graphNames = new GraphNames(testNum);

      this.testRunDataType = testRunDataType;

      initForConstructor();
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "TestRunData", "", "", "Exception - ", ex);
    }
  }

  public TestRunData(int testRunDataType, int testNum, long startSeq, long endSeq, int granularity, int numberOfDisplayPanels, String userName, GraphUniqueKeyDTO[] arrActiveGraphUniqueKeyDTO, boolean testViewMode, GraphNames graphNames, TestRunDataType testRunDataTypeObj)
  {
    try
    {
      Log.debugLog(className, "TestRunData", "", "", "Method Called. testRunDataType = " + testRunDataType + ", testNum = " + testNum + ", startSeq = " + startSeq + ", endSeq = " + endSeq + ", granularity = " + granularity + ", numberOfDisplayPanels = " + numberOfDisplayPanels + ", testViewMode = " + testViewMode + ", testRunDataTypeObj = " + testRunDataTypeObj + ", graphNames = " + graphNames);

      this.testRunDataTypeObj = testRunDataTypeObj;
      this.graphNames = graphNames;
      this.testNum = testNum;

      if (TestRunDataType.isNDE_Continuous_Mode())
        nDE_Continuous_Mode = true;

      if (graphNames == null && !nDE_Continuous_Mode)
        this.graphNames = new GraphNames(testNum);

      this.testRunDataType = testRunDataType;
      this.startSeq = startSeq;
      this.endSeq = endSeq;
      this.granularity = granularity;
      this.numberOfDisplayPanels = numberOfDisplayPanels;
      this.userName = userName;
      this.testViewMode = testViewMode;
      this.arrActiveGraphUniqueKeyDTO = arrActiveGraphUniqueKeyDTO;

      initForConstructor();

      if (this.arrActiveGraphUniqueKeyDTO == null)
        setAutoActivateValue();

      timeBasedTestRunDataObj.getMsgData().setTestRun(testNum);
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "TestRunData", "", "", "Exception - ", ex);
    }
  }

  /**
   * This Constructor is used for getting TestRunData object in NDE Continuous monitoring mode.
   * 
   * @param testNum
   * @param granularity
   * @param numberOfDisplayPanels
   * @param userName
   * @param arrActiveGraphNumbers
   * @param testViewMode
   * @param testRunDataTypeObj
   */
  public TestRunData(int testNum, int granularity, long startTime, long endTime, int numberOfDisplayPanels, String userName, GraphUniqueKeyDTO[] arrActiveGraphUniqueKeyDTO, boolean testViewMode, TestRunDataType testRunDataTypeObj, String activePartitionDirName, GraphNames graphNamesObj, DerivedDataDTO derivedDataDTO , boolean isUpdateGeneratorTBRD)
  {
    try
    {
      Log.debugLogAlways(className, "TestRunData:Constructor", "", "", "Method Called. testNum = " + testNum + ", granularity = " + granularity + ", numberOfDisplayPanels = " + numberOfDisplayPanels + ", testViewMode = " + testViewMode + ", activePartitionDirName = " + activePartitionDirName);
      this.testRunDataTypeObj = testRunDataTypeObj;
      this.testNum = testNum;
      this.isUpdateGeneratorTBRD = isUpdateGeneratorTBRD;
      Log.debugLogAlways(className, "TestRunData:Constructor", "", "", "Setting isUpdateGeneratorTBRd = " + isUpdateGeneratorTBRD);
      this.granularity = granularity;
      this.numberOfDisplayPanels = numberOfDisplayPanels;
      this.userName = userName;
      this.testViewMode = testViewMode;
      this.arrActiveGraphUniqueKeyDTO = arrActiveGraphUniqueKeyDTO;
      this.graphNames = graphNamesObj;
      this.activePartitionDirName = activePartitionDirName;
      this.derivedDataDTO = derivedDataDTO;
      
      // putting Absolute start time and end time
      startSeq = startTime;
      endSeq = endTime;

      initForConstructor();
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "TestRunData", "", "", "Exception - ", e);
    }
  }
  
  private void initForConstructor()
  {
    //Creating the object of DahboardConfig to get loaded all keyword values from config.ini of Dash board
    dashboardConfig = new DashboardConfig();

    Log.debugLogAlways(className, "initForConstructor", "", "", "Method Called. NDE TestRun = " + dashboardConfig.getNDETestRunNum() + ", testRunDataTypeObj = " + testRunDataTypeObj);
    if (TestRunDataType.isNDE_Continuous_Mode())
      nDE_Continuous_Mode = true;

    debugLogLevel = dashboardConfig.getDebugLevel();

    setTBTRDSizeInMap();

    initTestControlData();
    setAutoActivateValue();

    if (testRunDataTypeObj == null)
    {
      if (nDE_Continuous_Mode)
      {
        String dataView = dashboardConfig.getDataView();
        this.testRunDataTypeObj = TestRunDataType.getTestRunDataTypeObjByLastNKey(dataView);
      }
      else
        this.testRunDataTypeObj = new TestRunDataType(testViewMode);

      strTimeBasedTestRunDataKey = this.testRunDataTypeObj.getHMapKey();
    }
    else
    {
      strTimeBasedTestRunDataKey = testRunDataTypeObj.getHMapKey();
    }

    Log.debugLogAlways(className, "initForConstructor", "", "", "Active Requested Time Based Key = " + strTimeBasedTestRunDataKey);
    timeBasedTestRunDataObj = new TimeBasedTestRunData(this, strTimeBasedTestRunDataKey, this.testRunDataTypeObj);
    insertDataInHashMap(strTimeBasedTestRunDataKey, timeBasedTestRunDataObj);

    if (testNum > 0)
      initForNewTestRun(startSeq, endSeq, granularity, numberOfDisplayPanels, userName, arrActiveGraphUniqueKeyDTO, testViewMode);

    if (dashboardConfig.getEnableTreeColorIcon() == 1)
    {
      HieararchicalTreeSettingData hieararchicalTreeSettingDataObj = new HieararchicalTreeSettingData();
      hieararchicalTreeSettingData = hieararchicalTreeSettingDataObj.getTreeIconColorHM();
    }
  }

  /**
   * This Constructor is used for getting TestRunData object in NDE Continuous monitoring mode.
   * 
   * @param testNum
   * @param granularity
   * @param numberOfDisplayPanels
   * @param userName
   * @param arrActiveGraphNumbers
   * @param testViewMode
   * @param testRunDataTypeObj
   */
  public TestRunData(int testNum, int granularity, long startTime, long endTime, int numberOfDisplayPanels, String userName, GraphUniqueKeyDTO[] arrActiveGraphUniqueKeyDTO, boolean testViewMode, TestRunDataType testRunDataTypeObj, String activePartitionDirName, GraphNames graphNamesObj, DerivedDataDTO derivedDataDTO)
  {
    try
    {
      Log.debugLogAlways(className, "TestRunData:Constructor", "", "", "Method Called. testNum = " + testNum + ", granularity = " + granularity + ", numberOfDisplayPanels = " + numberOfDisplayPanels + ", testViewMode = " + testViewMode + ", activePartitionDirName = " + activePartitionDirName);
      this.testRunDataTypeObj = testRunDataTypeObj;
      this.testNum = testNum;
      this.granularity = granularity;
      this.numberOfDisplayPanels = numberOfDisplayPanels;
      this.userName = userName;
      this.testViewMode = testViewMode;
      this.arrActiveGraphUniqueKeyDTO = arrActiveGraphUniqueKeyDTO;
      this.graphNames = graphNamesObj;
      this.activePartitionDirName = activePartitionDirName;
      this.derivedDataDTO = derivedDataDTO;

      // putting Absolute start time and end time
      startSeq = startTime;
      endSeq = endTime;

      initForConstructor();

    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "TestRunData", "", "", "Exception - ", e);
    }
  }

  /**
   * Get Epoch Start TimeStamp.
   * 
   * @return
   */
  public long getEpochTimeStamp()
  {
    return epochTimeStamp;
  }

  /**
   * Method used to check NDE continuous monitoring mode.
   * 
   * @return
   */
  public boolean isnDE_Continuous_Mode()
  {
    return nDE_Continuous_Mode;
  }

  /**
   * Returns the active partition file name.
   * 
   * @return
   */
  public String getActivePartitionDirName()
  {
    return activePartitionDirName;
  }

  /**
   * Sets the active partition file name.
   * 
   * @param activePartitionDirName
   */
  public void setActivePartitionDirName(String activePartitionDirName)
  {
    this.activePartitionDirName = activePartitionDirName;
  }

  /**
   * This method is used to identify the directory structure of Test Run.
   * 
   * @return
   */
  public int getTestRun_Partition_Type()
  {
    return testRun_Partition_Type;
  }

  /**
   * Method Sets the value of partition type used by test run number.
   * 
   * @param testRun_Partition_Type
   */
  public void setTestRun_Partition_Type(int testRun_Partition_Type)
  {
    this.testRun_Partition_Type = testRun_Partition_Type;
  }

  /*
   * This method will get info about NetCloud Generator After that, will create TimeBasedTestRunData Object for each generator and put in hashMap If Generator GraphName object is null, then create
   * new GraphNames object for Generator
   */
  public void createNetCloudGeneratorTBRD()
  {
    try
    {
      Log.debugLogAlways(className, "createNetCloudGeneratorTBRD", "", "", "method called");

      netcloudDtoList = NetCloudData.getNectCloudDtoList(vectNetCloudData, testNum);
   
      if(testRun_Partition_Type <= 0)
      {
	
	/*Checking for NetCloud Test Runs Availability.*/
	if(netcloudDtoList != null)
	{
	  /*Increasing the default limit with number of netcloud test runs.*/
	  TIME_BASED_TEST_RUN_HASHMAP_MEMORY_LIMIT = TIME_BASED_TEST_RUN_HASHMAP_MEMORY_LIMIT + netcloudDtoList.size();
	}
	
        for (int i = 0; i < netcloudDtoList.size(); i++)
        {
          NetCloudDTO tempNCDto = netcloudDtoList.get(i);

          if (generatorGraphNames == null)
          {
            Log.debugLogAlways(className, "createNetCloudGeneratorTBRD", "", "", "Creating GraphNames object for generator - " + tempNCDto.getGeneratorName());
            int numTestRun = Integer.parseInt(tempNCDto.getGeneratorTRNumber());
            generatorGraphNames = new GraphNames(numTestRun, null, null, String.valueOf(testNum), tempNCDto.getGeneratorName(), "", false);
            vectGenGDF = generatorGraphNames.getGdfData();
            vectGenPDF = TestRunDataUtils.getTestRunPDF(Integer.parseInt(tempNCDto.getGeneratorTRNumber()), testNum);
          }

          // Generating TBRD object for generators
          String strGenTimeBasedTestRunDataKey = tempNCDto.getGeneratorName() + "_" + testRunDataTypeObj.getHMapKey();

          Log.debugLogAlways(className, "createNetCloudGeneratorTBRD", "", "", "Generate TimeBasedTestRunData object for generator = " + tempNCDto.getGeneratorName());

          TimeBasedTestRunData generatorTimeBasedTestRunData = new TimeBasedTestRunData(this, strGenTimeBasedTestRunDataKey, this.testRunDataTypeObj);
          generatorTimeBasedTestRunData.setGraphNamesObj(generatorGraphNames);
          generatorTimeBasedTestRunData.setGeneratorName(tempNCDto.getGeneratorName());

          insertDataInHashMap(strGenTimeBasedTestRunDataKey, generatorTimeBasedTestRunData);
        }
      }
      else
      {
        for (int i = 0; i < netcloudDtoList.size(); i++)
        {
          NetCloudDTO tempNCDto = netcloudDtoList.get(i);

          // Generating TBRD object for generators
          String strGenTimeBasedTestRunDataKey = tempNCDto.getGeneratorName() + "_" + testRunDataTypeObj.getHMapKey();

          Log.debugLogAlways(className, "createNetCloudGeneratorTBRD", "", "", "Generate TimeBasedTestRunData object for generator = " + tempNCDto.getGeneratorName());

          TimeBasedTestRunData generatorTimeBasedTestRunData = new TimeBasedTestRunData(this, strGenTimeBasedTestRunDataKey, this.testRunDataTypeObj);
          
          generatorTimeBasedTestRunData.setGeneratorName(tempNCDto.getGeneratorName());
          generatorTimeBasedTestRunData.setGeneratorTRNum(tempNCDto.getGeneratorTRNumber());
          insertDataInHashMap(strGenTimeBasedTestRunDataKey, generatorTimeBasedTestRunData);
          
          if(isUpdateGeneratorTBRD)
            createTimeBasedDataFromPartitions(granularity, null ,testViewMode , generatorTimeBasedTestRunData);
        }
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "createNetCloudGeneratorTBRD", "", "", "Caught exception in NetClout TBRD generation", e);
    }
  }

  // Initialization for Test Control Data.
  private void initTestControlData()
  {
    testCtrlData = new TestControlData();
    testCtrlData.setIsPaused(false);
    testCtrlData.setCurTime(System.currentTimeMillis());
  }

  // Initializing test run data
  public void initForNewTestRun()
  {
    try
    {
      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "initForNewTestRun", "", "", "initForNewTestRun for testNum = " + testNum);

      initForNewTestRun(-1, -1, -1);
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "initForNewTestRun", "", "", "Exception - ", ex);
    }
  }

  /**
   * initializing test run data arrays
   * 
   * @author Ravi
   * @param startSeq
   * @param endSeq
   * @param granularity
   */
  public void initForNewTestRun(long startSeq, long endSeq, int granularity)
  {
    if (debugLogLevel > 0)
      Log.debugLogAlways(className, "initForNewTestRun", "", "", "Method Called. startSeq = " + startSeq + ", endSeq = " + endSeq + ", granularity = " + granularity + ", testNum = " + testNum);

    initForNewTestRun(startSeq, endSeq, granularity, numberOfDisplayPanels, userName);
  }

  /**
   * initializing test run data arrays
   * 
   * @author Ravi
   * @param startSeq
   * @param endSeq
   * @param granularity
   * @param numberOfPanels
   * @param userName
   */
  public void initForNewTestRun(long startSeq, long endSeq, int granularity, int numberOfPanels, String userName)
  {
    if (debugLogLevel > 0)
      Log.debugLogAlways(className, "initForNewTestRun", "", "", "Method Called. startSeq = " + startSeq + ", endSeq = " + endSeq + ", granularity = " + granularity + ", numberOfPanels = " + numberOfPanels + ", userName = " + userName + ", testNum = " + testNum);
    initForNewTestRun(startSeq, endSeq, granularity, numberOfPanels, userName, arrActiveGraphUniqueKeyDTO, testViewMode);
  }

  /**
   * This method is used for initializing the Time based Test Run Data array and variables.
   * 
   * @author Ravi
   * @param startSeq
   * @param endSeq
   * @param granularity
   * @param numberOfDisplayPanels
   * @param userName
   * @param arrActiveGraphNumbers
   * @param testViewMode
   */
  public synchronized void initForNewTestRun(long startSeq, long endSeq, int granularity, int numberOfDisplayPanels, String userName, GraphUniqueKeyDTO[] arrActiveGraphUniqueKeyDTO, boolean testViewMode)
  {
    try
    {
      if(debugLogLevel > 0)
        Log.debugLogAlways(className, "initForNewTestRun", "", "", "Method Called. startSeq = " + startSeq + ", endSeq = " + endSeq + ", granularity = " + granularity + ", numberOfDisplayPanels = " + numberOfDisplayPanels + ", userName = " + userName + ", testViewMode = " + testViewMode + ", testNum = " + testNum + ", nDE_Continuous_Mode = " + nDE_Continuous_Mode);
     
      //Reading TestRun Partition Type.
      testRun_Partition_Type = TestRunDataType.getTestRunPartitionType(testNum);
      
      bolError = false;
      bolWarning = false;

      //this is to read the user properties from *.prop files
      objUserProp = new UserProp();
      objUserProp.readFile();

      this.granularity = granularity;

      long processingStartTime = System.currentTimeMillis();

      config_rtg_max_data_samples_in_one_graph = dashboardConfig.getRtgMaxDataSamplesInOneGraph();
      lowWaterMark = dashboardConfig.getLowWaterMarkForTRD();
      highWaterMark = dashboardConfig.getHighWaterMarkForTRD();

      if(debugLogLevel > 0)
        Log.debugLogAlways(className, "initForNewTestRun", "", "", "value for lowWaterMark: " + lowWaterMark + ", value for highWaterMark: " + highWaterMark + ", testRun_Partition_Type = " + testRun_Partition_Type);

      //Reading Test Run Completion Time
      targetCompTime = Scenario.getTargetCompTimeFromGlobalFile(testNum);
      
      //Reading Test Run Start Time
      trStartTime = Scenario.getTestRunStartTime(testNum);

      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "initForNewTestRun", "", "", "targetCompTime = " + targetCompTime + ", trStartTime = " + trStartTime);

      try
      {
	      /*TODO - Need to keep Time Zone for each TR.*/
	      /*Method actually returns the machine time zone in which the test run exist.Need keep the test run time zone information.*/
       	trTimeZone = ExecutionDateTime.getSystemTimeZoneGMTOffset();
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
        format.setTimeZone(trTimeZone);
        startDate = format.parse(trStartTime);
        trStartTimeStamp = format.parse(trStartTime).getTime();

        if(debugLogLevel > 0)
          Log.debugLogAlways(className, "initForNewTestRun", "", "", "TestRun start Date = " + startDate + ", testRun start time stamp = " + trStartTimeStamp + ", testrun TimeZone = " + trTimeZone.toString());
      }
      catch(Exception e)
      {
        Log.stackTraceLog(className, "initForNewTestRun", "", "", "Exception : ", e);
        startDate = new Date();
        Log.errorLog(className, "initForNewTestRun", "", "", "Error in parsing test run start date/time, use current time = " + startDate);
      }

      scenarioName = Scenario.getScenarioName(testNum);
      testName = Scenario.getTestName(testNum);
      testDuration = Scenario.getTestDuration(testNum);
      debugTraceLogFlag = Scenario.getDebugTraceLogFlagVal(testNum);
      
      if(debugLogLevel > 0)
        Log.debugLogAlways(className, "initForNewTestRun", "", "", "scenarioName = " + scenarioName + ", testName = " + testName + ", testDuration = " + testDuration + ", debugTraceLogFlag = " + debugTraceLogFlag);

      TestRunPhaseInfo testRunPhaseInfo = new TestRunPhaseInfo(testNum, scenarioName, -1);

      arrListTestRunPhasesObj = (ArrayList) testRunPhaseInfo.getPhaseInfo();
      
      //this is use to keep ADF information in memory
      testRunAdfInfo = (TestRunAdfInfo) ((new ADFBean()).getTestRunADFInfo());

      //Get net storm port
      netstormPort = Scenario.getNetstormPort(testNum);

      //Get baseline test run and scenario
      baseLineTrackingFileData = Scenario.readBaseLineTrackingFile(testNum);

      /*This will set baseline TR number by reading first record from the baseline tracking file(baseLineTrackingFileData) */
      baseLineTR = getBaseLineTRNum();

      /* This line is commented because now we are doing multi compare, in this we have more than one scenario */
      // baseLineScenarioName = Scenario.getBaseLineScenarioName(testNum);

      if(debugLogLevel > 0)
        Log.debugLogAlways(className, "initForNewTestRun", "", "", "netstormPort = " + netstormPort + ", baseLineTR = " + baseLineTrackingFileData + ", baseLineScenarioName = " + baseLineScenarioName + ", testNum = " + testNum);

      summary_top_line = TestRunDataUtils.getTestRunSummary_Top(testNum);
      vectNetCloudData = NetCloudData.getNetCloudData(testNum);

      if(testRun_Partition_Type > 0)
      {
        //Getting Epoch Year from scenario file.
        String[] arrKeyWordValues = Scenario.getKeywordValues("CAV_EPOCH_YEAR", testNum);

        if(arrKeyWordValues != null)
        {
          if(arrKeyWordValues.length != 0)
            epochYear = arrKeyWordValues[0].trim();
        }

        //Getting time stamp of epoch date.
        epochTimeStamp = ExecutionDateTime.getEpochTimeStamp(epochYear, trTimeZone);
      }
      else
      {
        vectGDF = TestRunDataUtils.getTestRunGDF(testNum);
        vectPDF = TestRunDataUtils.getTestRunPDF(testNum);

        if (graphNames == null)
          graphNames = new GraphNames(testNum, vectGDF, vectPDF, "NA", "", "", false);

        //Set test run interval
        interval = graphNames.getInterval();
      }

      //reading WAN_ENV_MODE value from summary.top
      wanEnv = TestRunDataUtils.getWanEnv(summary_top_line);

      //reading REPORTING_LEVEL value from summary.top
      reportingLevel = TestRunDataUtils.getReportingLevel(summary_top_line);

      if(debugLogLevel > 0)
        Log.debugLogAlways(className, "initForNewTestRun", "", "", "WAN_ENV_MODE = " + wanEnv + ", REPORTING_LEVEL = " + reportingLevel);

      //Creating instance for ExecutionDrillDown
      executionDrillDown = new ExecutionDrillDown();

      //Getting value for READER_RUN_MODE keyword
      String[] arrKeyWordValues = Scenario.getKeywordValues("READER_RUN_MODE", testNum);
      if(arrKeyWordValues != null)
      {
        if(arrKeyWordValues.length != 0)
          readerRunMode = Integer.parseInt(arrKeyWordValues[0].trim());
      }

      //Getting value for showInitiated
      arrKeyWordValues = Scenario.getKeywordValues("SHOW_INITIATED", testNum);
      if(arrKeyWordValues != null)
      {
        if(arrKeyWordValues.length != 0)
          showInitiated = Integer.parseInt(arrKeyWordValues[0].trim());
      }

      if(debugLogLevel > 0)
        Log.debugLogAlways(className, "initForNewTestRun", "", "", "readerRunMode = " + readerRunMode + ", showInitiated = " + showInitiated);
    //Getting value for netCacheApplied
      arrKeyWordValues = Scenario.getKeywordValues("G_ENABLE_NETWORK_CACHE_STATS", testNum);
      if(arrKeyWordValues != null)
      {
        if(arrKeyWordValues.length != 0)
          netCacheApplied = Integer.parseInt(arrKeyWordValues[1].trim());
      }

      if(debugLogLevel > 0)
        Log.debugLogAlways(className, "initForNewTestRun", "", "", "readerRunMode = " + readerRunMode + ", netCacheApplied = " + netCacheApplied);

      //getting value of ENABLE_SYNC_POINT keyword
      String[] arrKeyWordValue = Scenario.getKeywordValues("ENABLE_SYNC_POINT", testNum);
      if(arrKeyWordValue != null)
      {
        if(arrKeyWordValue.length != 0)
          enableSynPoint = Integer.parseInt(arrKeyWordValue[0].trim());
      }

      if(debugLogLevel > 0)
        Log.debugLogAlways(className, "initForNewTestRun", "", "", "enableSynPoint = " + enableSynPoint);

      try
      {
        arrKeyWordValues = Scenario.getKeywordValues("HIERARCHICAL_VIEW", testNum);
        if(arrKeyWordValues != null)
        {
          if(arrKeyWordValues.length != 0)
          {
            hierarchicalMode = Integer.parseInt(arrKeyWordValues[0]);

            //Getting value of vector separator if available and hierarchical mode is 1.
            if(hierarchicalMode == 1)
            {
              //Taking third parameter of keyword as separator if available.
              if(arrKeyWordValues.length > 1)
                topologyName = arrKeyWordValues[1];
              if(arrKeyWordValues.length > 2)
                vectorSeperator = arrKeyWordValues[2];
              else
                vectorSeperator = ">"; // Set as default if value is not available in keyword.
            }
            else
            {
              if (arrKeyWordValues.length > 1)
                topologyName = arrKeyWordValues[1];
            }
          }

          Log.debugLog(className, "initForNewTestRun", "", "", "hierarchicalMode = " + hierarchicalMode + " and vectorSeperator = " + vectorSeperator);
        }
        else
        {
          Log.errorLog(className, "initForNewTestRun", "", "", "Unable to get value of hierarchical tree mode value from Scenario file");
        }
      }
      catch (Exception e)
      {
        hierarchicalMode = 0;
        Log.stackTraceLog(className, "initForNewTestRun", "", "", "Exception : ", e);
      }

      //Here we get vector separator Mapping from Mapping file.
      //Passing File Name as Argument.
      arrVectorMappingList = rptUtilsBean.getVectorNameFromMappedFile("VectorMapping.dat");

      /**
       * if ENABLE_SYNC_POINT is enable than making object of file bean and updating global variables
       */
      if (enableSynPoint > 0)
      {
        // object of file bean
        FileBean fileBeanObj = new FileBean("2", userName);
        StringBuffer errMsg = new StringBuffer();

        if (debugLogLevel > 0)
          Log.debugLogAlways(className, "initForNewTestRun", "", "", "Scenario Name = " + "TestRun/" + testNum + "/" + scenarioName.substring(scenarioName.lastIndexOf("/") + 1, scenarioName.length()));

        // getting global properties
        globalScenarioProp = fileBeanObj.getKeyValues("TestRun/" + testNum + "/" + scenarioName.substring(scenarioName.lastIndexOf("/") + 1, scenarioName.length()), errMsg);

        if (globalScenarioProp != null)
        {
          Object[] obj = fileBeanObj.getScenGroupScriptInfo(globalScenarioProp, "SGRP", linkedHashMapForTransPageSync);
          if (obj != null)
          {
            arrGroupScript = (String[][]) obj[0];
            linkedHashMapForTransPageSync = (LinkedHashMap) obj[1];
            linkedHashMapForTransPageSync = fileBeanObj.updateTransPageSyncList(linkedHashMapForTransPageSync, "Server");
          }
        }
      }

      //Getting phase info
      arrListTestRunPhasesObj = (ArrayList) testRunPhaseInfo.getPhaseInfo();
      long processingEndTime = System.currentTimeMillis();

      Log.debugLog(className, "initForNewTestRun", "", "", "timeBasedTestRunDataObj = " + timeBasedTestRunDataObj + ", graphNames = " + graphNames + ", testRun_Partition_Type = " + testRun_Partition_Type);

      if (testRun_Partition_Type <= 0)
      {
        if(timeBasedTestRunDataObj != null)
        {
          timeBasedTestRunDataObj.setGraphNamesObj(graphNames);
          timeBasedTestRunDataObj.initForNewTimeBasedTestRunData(granularity, arrActiveGraphUniqueKeyDTO, testViewMode);
        }
      }
      else
      {
        if(timeBasedTestRunDataObj != null)
          createTimeBasedDataFromPartitions(granularity, arrActiveGraphUniqueKeyDTO, testViewMode , timeBasedTestRunDataObj);
        else
          Log.errorLog(className, "initForNewTestRun", "", "", "timeBasedTestRunDataObj is null.");
      }

      //Check and create NetCloud TBRD
      if(vectNetCloudData != null)
        createNetCloudGeneratorTBRD();

      if(debugLogLevel > 0)
        Log.debugLogAlways(className, "initForNewTestRun", "", "", "Total Time Taken In Initializing Time Based Test Run Data = " + (processingEndTime - processingStartTime) + " miliseconds.");
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "initForNewTestRun", "", "", "Exception - ", ex);
    }
  }

  private int getBaseLineTRNum()
  {
    try
    {
      Log.debugLogAlways(className, "getBaseLineTRNum", "", "", "Method called");

      if (baseLineTrackingFileData == null || baseLineTrackingFileData.size() < 2)
        return -1;

      String firstRecord = baseLineTrackingFileData.get(1);
      String[] str = rptUtilsBean.strToArrayData(firstRecord, "|");

      return Integer.parseInt(str[2].trim());
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getBaseLineTRNum", "", "", "Exception - ", e);
      return -1;
    }
  }
  
  /**
    * Method is used to initializing and getting partition related information.
    */
  public void createTimeBasedDataFromPartitions(int granularity, GraphUniqueKeyDTO[] arrActiveGraphUniqueKeyDTO, boolean testViewMode , TimeBasedTestRunData tbrdObj  )
  {
    try
    {
      Log.debugLogAlways(className, "createTimeBasedDataFromPartitions", "", "", "Method Called. testNum = " + testNum + ", granularity = " + granularity + ", testViewMode = " + testViewMode);
    
      SessionReportData sessionReportData = null;
      
      if(tbrdObj.getGeneratorName() == null )
        sessionReportData = new SessionReportData(testNum);
      else
        sessionReportData = new SessionReportData(testNum , tbrdObj.getGeneratorName() , tbrdObj.getGeneratorTRNum());
            
      Log.debugLogAlways(className, "createTimeBasedDataFromPartitions", "", "", "Time Zone = " + trTimeZone.getDisplayName() + ", and ID = " + trTimeZone.getID());

      sessionReportData.setTimeZone(trTimeZone);

      if (sessionReportData.getAvailableSessionList().size() == 0)
      {
        Log.errorLog(className, "createTimeBasedDataFromPartitions", "", "", "No Session available.");
        return;
      }
      
      /*Getting Last Valid Partition with GDF.*/
      String currActivePartitionDirName = getLastValidPartitionForGDF(sessionReportData);
      
      if(currActivePartitionDirName == null)
      {
	Log.errorLog(className, "createTimeBasedDataFromPartitions", "", "", "Error in Getting Valid Partition.");
	return;
      }
      
      // Getting GraphNames object from sessions and initialize the session.
      // Getting GraphNames object of Last active session.
      GraphNames currGraphNames = sessionReportData.createGraphNamesObj(testNum, currActivePartitionDirName, sessionReportData.getLatestGDFVersion(currActivePartitionDirName));
      
      if(currGraphNames == null)
      {
        Log.errorLog(userName, "createTimeBasedDataFromPartitions", "", "", "GraphNames object must not be null. Please check logs for detail.");
        return;
      }
      
      /*Getting the first Partition Time stamp.*/
      if(tbrdObj.getGeneratorName() == null)
      {
        trStartTimeStamp = ExecutionDateTime.convertFormattedDateToMilliscond(sessionReportData.getFirstParitionName(), DataPacketInfo.PARTITION_FILE_START_DATE_FORMAT, trTimeZone);
        testDuration = ExecutionDateTime.convertTimeToFormattedString(sessionReportData.getTotalDurationOfAllPartitions(), ":");
       
        activePartitionDirName = currActivePartitionDirName;
        interval = currGraphNames.getInterval();
        vectGDF = currGraphNames.getGdfData();
        vectPDF = currGraphNames.getPdfData();
        
        graphNames = currGraphNames;
      }
      else
      {
        vectGenGDF = currGraphNames.getGdfData();
        vectGenPDF = currGraphNames.getPdfData();
        generatorGraphNames = currGraphNames;
        if(arrActiveGraphUniqueKeyDTO == null)
        {
          arrActiveGraphUniqueKeyDTO = currGraphNames.getGraphUniqueKeyDTO();
        }
      }
      
      /*TODO - Why it is needed. it cause problem as it disabled auto activate mode.*/
      /*if(arrActiveGraphUniqueKeyDTO == null)
      {
        arrActiveGraphUniqueKeyDTO = currGraphNames.getGraphUniqueKeyDTO();
      }*/
      
      Log.debugLogAlways(className, "createTimeBasedDataFromPartitions", "", "", "currActivePartitionDirName = " + currActivePartitionDirName);

      sessionReportData.graphNamesObj = currGraphNames;
      tbrdObj.setGraphNamesObj(currGraphNames);

      if(nDE_Continuous_Mode)
      tbrdObj.setNDE_Continuous_Mode(true);

      tbrdObj.setTestRun_Partition_Type(testRun_Partition_Type);

      //Initializing the time based object.
      tbrdObj.initForNewTimeBasedTestRunData(granularity, arrActiveGraphUniqueKeyDTO, testViewMode);

      //Initialization of Derived Data Processor. Need to discuss for Generator
      if(derivedDataDTO != null && tbrdObj.getGeneratorName() == null)
        tbrdObj.getDerivedDataProcessor().init(derivedDataDTO, timeBasedTestRunDataObj);

      //Creating Instance of session data info.
      SessionDataInfo sessionDataInfo = null;

      //Checking TestRunData Request Type by key.
      if(testRunDataTypeObj.getType() == TestRunDataType.LAST_N_MINUTES_DATA)
      {
        sessionDataInfo = sessionReportData.getSessionInfoForLastNMinute(testRunDataTypeObj.getLastNMinutesValue());
        Log.debugLogAlways(className, "createTimeBasedDataFromPartitions", "", "", "Last N Moving TimeStamp = " + ExecutionDateTime.convertDateTimeStampToFormattedString(sessionDataInfo.getEndTimeStamp(), "MM/dd/yy HH:mm:ss", trTimeZone));

        tbrdObj.setMovingTimeStampInLastViewMode(sessionDataInfo.getEndTimeStamp());// + epochTimeStamp);
      }
      else if(testRunDataTypeObj.getType() == TestRunDataType.SPECIFIED_PHASE_OR_TIME)
      {
        sessionDataInfo = sessionReportData.getSessionInfoForSpecifiedDates(startSeq, endSeq);
      }
      else
      {
	if(isnDE_Continuous_Mode())
	{
	  Log.errorLog(className, "createTimeBasedDataFromPartitions", "", "", "Invalid Request key. Key = " + testRunDataTypeObj.getHMapKey());
	  return;
	}

	sessionDataInfo = sessionReportData.getSessionInfoForWholeScenario();
      }

      if(debugLogLevel > 3)
        printHashMap(sessionReportData.getAvailableSessionDuration());

      //Getting Time Based Data object for requested sessions.
      tbrdObj = sessionReportData.readFileAndCreateTimeBasedData(sessionDataInfo, tbrdObj);
    }
    catch(Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "createTimeBasedDataFromPartitions", "", "", "Exception - ", e);
    }
  }
  
  /**
   * Looking for valid Partition with GDF.
   * @param sessionReportData
   * @return
   */
  private String getLastValidPartitionForGDF(SessionReportData sessionReportData)
  {
    try
    {
      Log.debugLogAlways(className, "getLastValidPartitionForGDF", "", "", "Method Called.");
      
      /*Checking for session object.*/
      if(sessionReportData == null)
      {
	Log.errorLog(className, "getLastValidPartitionForGDF", "", "", "Getting Session object null");
	return null;
      }
      
      /*Looking for valid partition.*/
      for(int i = sessionReportData.getAvailableSessionList().size() - 1; i >= 0; i--)
      {
	if(sessionReportData.isValidPartitionWithGDF(sessionReportData.getAvailableSessionList().get(i)))
	  return sessionReportData.getAvailableSessionList().get(i);
      }
      
      return null;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Printing the available sessions/Partitions.
   * 
   * @param hashMap
   */
  private void printHashMap(HashMap<String, PartitionDataProperties> hashMap)
  {
    try
    {
      Iterator it = hashMap.entrySet().iterator();
      TimeZone tr = ExecutionDateTime.getSystemTimeZoneGMTOffset();
      long total = 0;
      
      while (it.hasNext())
      {
        Map.Entry<String, PartitionDataProperties> pairs = (Map.Entry) it.next();
        total = total + pairs.getValue().getPartitionDuration();
        Log.debugLog(className, "TestRunData:printHashMap", "", "", "Key = " + ExecutionDateTime.convertDateTimeStampToFormattedString(ExecutionDateTime.convertFormattedDateToMilliscond(pairs.getKey(), "yyyyMMddHHmmss", tr), "MM/dd/yy HH:mm:ss", tr) + " value = " + pairs.getValue());
      }
      
      Log.debugLog(className, "TestRunData:printHashMap", "", "", "Total available duration = " + ExecutionDateTime.convertTimeToFormattedString(total, ":"));
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public void makeGeneratorGraphNamesObj()
  {
    Log.debugLogAlways(className, "makeGeneratorGraphNamesObj", "", "", "method called");

    try
    {
      if (netcloudDtoList != null && netcloudDtoList.size() > 0)
      {
        for (int i = 0; i < netcloudDtoList.size(); i++)
        {
          NetCloudDTO tempNetCloudDto = netcloudDtoList.get(i);

          //Creating GraphNames object only first time, assuming all Generator are using same GDF.
          if(generatorGraphNames == null)
          {
            int numTestRun = Integer.parseInt(tempNetCloudDto.getGeneratorTRNumber());
            generatorGraphNames = new GraphNames(numTestRun, getTestRunGeneratorGDF(), getTestRunGeneratorPDF(), String.valueOf(getTestRun()), "", "", false);
          }

          String tbrdKey = tempNetCloudDto.getGeneratorName() + "_" + strTimeBasedTestRunDataKey;

          TimeBasedTestRunData tbrdObj = getTimeBasedTestRunDataByKey(tbrdKey);

          if (tbrdObj == null)
          {
            Log.errorLog(className, "makeGeneratorGraphNamesObj", "", "", "NetCloud TBRD Object is not exist for key - " + tbrdKey);
            continue;
          }
          
          tbrdObj.setGraphNamesObj(generatorGraphNames);
        }
      }
      else
      {
        Log.debugLog(className, "makeGeneratorGraphNamesObj", "", "", "NetCloud data is not exist.");
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "makeGeneratorGraphNamesObj", "", "", "Exception in makeGeneratorGraphNamesObj()", e);
    }
  }

  public void updateTBRDObject(TimeBasedTestRunData tbrdObj, String key)
  {
    try
    {
      Log.debugLog(className, "updateTBRDObject", "", "", "method called");
      //Setting graphNames object
      tbrdObj.setTestRunDataObj(this);

      tbrdObj.setGraphNamesObj(graphNames);
      //inserting into map
      insertDataInHashMap(key, tbrdObj);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "updateTBRDObject", "", "", "Error in updating tbrd object in TestRunData object", e);
    }
  }

  public GraphNames getGeneratorGraphNames()
  {
    return generatorGraphNames;
  }

  /**
   * This Method is used to get the vector separator for auto sensor as well as vector/Tier graphs.
   * 
   * @return
   */
  public String getVectorSeperator()
  {
    return vectorSeperator;
  }

  /**
   * This method is used to set the vector separator for auto sensor as well as vector/Tier graphs.
   * 
   * @param vectorSeperator
   */
  public void setVectorSeperator(String vectorSeperator)
  {
    this.vectorSeperator = vectorSeperator;
  }

  /**
   * Getting List containing Mapping of vector separator of different TestRuns.
   * 
   * @return
   */
  public ArrayList<VectorMappingDTO> getVectorMappingList()
  {
    return arrVectorMappingList;
  }

  public int getHierarchicalMode()
  {
    return hierarchicalMode;
  }

  public void setHierarchicalMode(int hierarchicalMode)
  {
    this.hierarchicalMode = hierarchicalMode;
  }

  public String getTopologyName()
  {
    return topologyName;
  }

  /**
   * This method return content of summary.top file
   * 
   * @author Ravi
   * @return summary.top file content
   */
  public String getTestRunSummary_Top_Line()
  {
    return summary_top_line;
  }

  /**
   * Basis of opcode, here we are checking is this test new. This method is called from NetstormListener
   * 
   * @author Ravi
   */
  public void chkForNewTestRun(data msgData)
  {
    try
    {
      testNum = msgData.getTestRun();
      interval = msgData.getInterval();
      activePartitionDirName = msgData.getPartitionFileName() + "";

      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "chkForNewTestRun", "", "", "prevTestRunNum = " + prevTestRunNum + ", testNum = " + testNum + ", SeqNum = " + msgData.getSeqNum() + ", Opcode = " + msgData.getOpcode());

      if ((msgData.getOpcode() == timeBasedTestRunDataObj.getPrevOpCode()) && (msgData.getSeqNum() == timeBasedTestRunDataObj.getPrevSeqNum()))
      {
        Log.errorLog(className, "chkForNewTestRun", "", "", "Duplicate data packet received. Opcode = " + msgData.getOpcode() + ", SeqNum = " + msgData.getSeqNum() + ", prevSeqNum = " + timeBasedTestRunDataObj.getPrevSeqNum());
        return;
      }

      if (prevTestRunNum != testNum)
      {
        if (debugLogLevel > 0)
          Log.debugLogAlways(className, "chkForNewTestRun", "", "", "New test run starts. Test Run = " + msgData.getTestRun());

        if (msgData.getOpcode() != data.START_PKT)
          Log.debugLogAlways(className, "chkForNewTestRun", "", "", "New test run started without start message.");

        // Ravi - By Mistake, I forget to set msgData. It is used in online mode
        // When user restart nsServer then it throws exception
        timeBasedTestRunDataObj.setMsgData(msgData);
        initForNewTestRun();
      }

      // Check if graph is restarted by Net storm or Server got restarted. This will occur if we
      // missed End Packet
      if ((msgData.getOpcode() == data.DATA_PKT) && (msgData.getSeqNum() < timeBasedTestRunDataObj.getPrevSeqNum()))
      {
        Log.debugLogAlways(className, "chkForNewTestRun", "", "", "Restart gap so that previous sequence is greater than current. Opcode = " + msgData.getOpcode() + ", SeqNum = " + msgData.getSeqNum());
        timeBasedTestRunDataObj.setMsgData(msgData);
        initForNewTestRun();
      }
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "chkForNewTestRun", "", "", "Exception - ", ex);
    }
  }

  /**
   * This method Gets the Elapsed Start Time According to Panel Data Type for DDR.
   */
  public long getElapsedStartTime(String timeBasedKey)
  {
    // Getting TimeBasedTestRunData Object.
    TimeBasedTestRunData tempTimeBasedTestRunData = getTimeBasedTestRunDataByKey(timeBasedKey);
    return tempTimeBasedTestRunData.getElapsedStartTimeInMillies(interval);
  }

  /**
   * This method Gets the Elapsed End Time According to Panel Data Type for DDR.
   */
  public long getElapsedEndTime(String timeBasedKey)
  {
    // Getting TimeBasedTestRunData Object.
    TimeBasedTestRunData tempTimeBasedTestRunData = getTimeBasedTestRunDataByKey(timeBasedKey);
    return tempTimeBasedTestRunData.getElapsedEndTimeInMillies(interval);
  }

  /**
   * This method Gets the Absolute Start Time According to Panel Data Type for DDR.
   */
  public long getAbsoluteStartTime(String timeBasedKey)
  {
    return (getElapsedStartTime(timeBasedKey) + trStartTimeStamp);
  }

  /**
   * This method Gets the Absolute End Time According to Panel Data Type for DDR.
   */
  public long getAbsoluteEndTime(String timeBasedKey)
  {
    return (getElapsedEndTime(timeBasedKey) + trStartTimeStamp);
  }

  /**
   * This method insert data into the hash map hMapTimeBasedTestRunData
   * 
   * @param key
   * @param value
   */
  public void insertDataInHashMap(String key, TimeBasedTestRunData value)
  {
    if (TIME_BASED_TEST_RUN_HASHMAP_MEMORY_LIMIT <= hMapTimeBasedTestRunData.size())
      manageWaterMark();

    hMapTimeBasedTestRunData.put(key, value);
    timeBasedTestRunDataHashMapKeys.add(key);
  }

  /**
   * Method is used to Remove Time Based Objects from memory, it is reaches Max Limit.
   */
  public void manageWaterMark()
  {
    try
    {
      Log.debugLog(className, "manageWaterMark", "", "", "Method Called. Total Available Object is = " + timeBasedTestRunDataHashMapKeys.size() + ", default key = " + testRunDataTypeObj.getHMapKey());
      
      int i = 0;
     
      /*Getting key for old Time Based objects.*/
      while(i < timeBasedTestRunDataHashMapKeys.size())
      {
	/*Getting the Time Based key of oldest one.*/
	String oldKey = timeBasedTestRunDataHashMapKeys.get(i);
	
	/*Checking for default key.*/
	if(testRunDataTypeObj.getHMapKey().equals(oldKey))
	{
	  Log.debugLog(className, "manageWaterMark", "", "", "Skipping Controller TestRun OR default Time Based Object.");
	}	
	/*Checking for availability of Time Based object.*/
	else if(hMapTimeBasedTestRunData.containsKey(oldKey))
	{
	  hMapTimeBasedTestRunData.remove(oldKey);
	  timeBasedTestRunDataHashMapKeys.remove(0);
	  Log.debugLogAlways(className, "manageWaterMark", "", "", "Watermark limit reached. Time Based Object for " + oldKey + " deleted successfully from memory.");
	 
	  /*Return after deleting one object.*/
	  return;
	}
	
	i++;
      }
    }
    catch(Exception e)
    {
      Log.errorLog(className, "manageWaterMark", "", "", "Error in cleaning Time Based object from memory.");
    }
  }

  /**
   * return TestRun number.
   * 
   * @author Ravi
   * @return
   */
  public int getTestRun()
  {
    return testNum;
  }

  /**
   * Setting test run number
   * 
   * @author Ravi
   * @param testNum
   */
  public void setTestNum(int testNum)
  {
    this.testNum = testNum;
  }

  /**
   * Returning net cloud data
   * 
   * @author Ravi
   * @return vector with net cloud data content
   */
  public Vector<String> getNetCloudData()
  {
    // NetCloudData.getGeneratorDetailsWithTR(vecData, controllerTestNum)
    return vectNetCloudData;
  }

  /**
   * This method return netstorm port, Called from Start.java
   * 
   * @author Ravi
   * @return netstormPort
   */
  public int getNetstormPort()
  {
    return netstormPort;
  }

  /**
   * This method returns the testrun.gdf
   * 
   * @author Ravi
   * @return content of testrun.gdf into a vector
   */
  public Vector<String> getTestRunGDF()
  {
    return vectGDF;
  }

  public ArrayList<NetCloudDTO> getNetCloudDto()
  {
    return this.netcloudDtoList;
  }

  public Vector<String> getTestRunGeneratorGDF()
  {
    return vectGenGDF;
  }

  public Vector<String> getTestRunGeneratorPDF()
  {
    return vectGenPDF;
  }

  /**
   * This methd returns the testrun.pdf
   * 
   * @author Ravi
   * @return content of testrun.pdf into a vector
   */
  public Vector<String> getTestRunPDF()
  {
    return vectPDF;
  }
  
  /**
   * Setting the PDF File Data in vector.
   * @param vectPDF
   */
  public void setTestRunPDF(Vector<String> vectPDF)
  {
    this.vectPDF = vectPDF;
  }
  
  /**
   * Setting the GDF File Data in vector.
   * @param vectPDF
   */
  public void setTestRunGDF(Vector<String> vectGDF)
  {
    this.vectGDF = vectGDF;
  }
  
  /**
   * Setting the Generator GDF file.
   * @param vectGenGDF
   */
  public void setVectGenGDF(Vector<String> vectGenGDF) 
  {
    this.vectGenGDF = vectGenGDF;
  }

  /**
   * Setting the Generator PDF file.
   * @param vectGenPDF
   */
  public void setVectGenPDF(Vector<String> vectGenPDF) 
  {
    this.vectGenPDF = vectGenPDF;
  }

  /**
   * 
   * @return all available time based test run data which is available into the memory
   */
  public HashMap<String, TimeBasedTestRunData> getAllAvailableTestRunData()
  {
    return hMapTimeBasedTestRunData;
  }

  /**
   * This method return current view in GUI Time Based Test Data
   * 
   * @return
   */
  public TimeBasedTestRunData getDefaultTimeBasedTestRunData()
  {
    try
    {
      if (hMapTimeBasedTestRunData.containsKey(strTimeBasedTestRunDataKey))
        return hMapTimeBasedTestRunData.get(strTimeBasedTestRunDataKey);
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getDefaultTimeBasedTestRunData", "", "", "Exception - ", ex);
    }

    if (debugLogLevel > 0)
      Log.debugLogAlways(className, "getDefaultTimeBasedTestRunData", "", "", "Required " + strTimeBasedTestRunDataKey + " data not available in memory.");
    return hMapTimeBasedTestRunData.get(defaultTestRunDataView);
  }

  public TimeBasedTestRunData getTimeBasedTestRunDataByKey(String key)
  {
    if (hMapTimeBasedTestRunData.containsKey(key))
    {
      return hMapTimeBasedTestRunData.get(key);
    }
    else
    {
      String defaultKey = defaultTestRunDataView;
      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "getTimeBasedTestRunDataByKey", "", "", "Invalid Key = " + key + ", Going for default key = " + defaultKey);
      if (hMapTimeBasedTestRunData.containsKey(defaultKey))
      {
        return hMapTimeBasedTestRunData.get(defaultKey);
      }
      else
      {
        Log.errorLog(className, "getTimeBasedTestRunDataByKey", "", "", "TimeBasedTestRunData not available for the key = " + key);
        return null;
      }
    }
  }

  // This method is for auto activate
  public void setAutoActivateValue()
  {
    if (dashboardConfig.getAutoActivate().equals("on"))
      isEnableAutoActivate = true;
  }

  public void setTBTRDSizeInMap()
  {
    TIME_BASED_TEST_RUN_HASHMAP_MEMORY_LIMIT = dashboardConfig.getTbtrdLimitInMemory();
  }

  // set error message
  public void setErrorMsg(String errorMsg)
  {
    bolError = true;
    this.errorMsg = errorMsg;
  }

  // This method set the waring in test run data
  public void setWarningMsg(String warningMsg)
  {
    bolWarning = true;
    this.warningMsg = warningMsg;
  }

  public void setTestRunIsNotRunning()
  {
    this.testRunState = 0;
  }

  // for Test Run is over
  public void testRunIsOver()
  {
    testRunState = 2;
  }

  // check is test run is over
  public boolean isTestRunIsOver()
  {
    if (testRunState == 2)
      return true;

    return false;
  }

  // method for is test run not running
  public boolean isTestRunIsNotRunning()
  {
    if (testRunState == 0)
      return true;

    return false;
  }

  /**
   * This method update data in all time based test run data
   * 
   * @param msgData
   */

  public synchronized void putTestRunData(data msgData)
  {
    try
    {
      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "putTestRunData", "", "", "Method Called. seqNum = " + msgData.getSeqNum());

      if (hMapTimeBasedTestRunData == null || hMapTimeBasedTestRunData.size() == 0)
      {
        Log.errorLog(className, "putTestRunData", "", "", "Bug: hMapTimeBasedTestRunData size = 0");
        return;
      }

      // Check if graph is over
      if (msgData.getOpcode() == data.END_PKT) // End packet is not kept in Vector
      {
        Log.debugLog(className, "putTestRunData", "", "", "Getting End Packet in TestRun Data. Test Run Number " + prevTestRunNum + " is over");
        testRunIsOver(); // This means current test run is over
        testCtrlData.setIsPaused(false);// set resume to if test run is over
      }

      // updating controller data packet
      updateTestRunDataPacket(msgData, null);

      // This Array will not be null, if Test Run Type is NetCloud
      data[] generatorDataArray = msgData.getGeneratorData();

      // updating generator data packet
      if (generatorDataArray != null)
      {
        for (int i = 0; i < netcloudDtoList.size(); i++)
        {
          data tempMsgData = generatorDataArray[i];

          if (tempMsgData == null)
          {
            Log.debugLogAlways(className, "putTestRunData", "", "", "data packet is not come for generator = " + netcloudDtoList.get(i));
          }

          updateTestRunDataPacket(tempMsgData, netcloudDtoList.get(i).getGeneratorName());
        }
      }

    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "putTestRunData", "", "", "", ex);
    }
  }

  public void updateTestRunDataPacket(data msgData, String generatorName)
  {
    try
    {
      if (debugLogLevel > 2)
        Log.debugLogAlways(className, "updateTestRunDataPacket", "", "", "method called");

      Iterator it = hMapTimeBasedTestRunData.entrySet().iterator();

      while (it.hasNext())
      {
        Map.Entry<String, TimeBasedTestRunData> pairs = (Map.Entry) it.next();

        TimeBasedTestRunData timeBasedTestRunData = pairs.getValue();

        if (debugLogLevel > 2)
          Log.debugLogAlways(className, "updateTestRunDataPacket", "", "", "Getting Time Based Object for key = " + pairs.getKey() + " timeBasedTestRunData.generatorName  = " + timeBasedTestRunData.getGeneratorName());

        if (generatorName == null)
        {
          if (timeBasedTestRunData.getGeneratorName() != null)
            continue;
          
          /*Need to check for GDF changes.*/
          if(msgData.getTestCtrlDataObj().isGDFChanged())
          {
            Log.debugLogAlways(className, "updateTestRunDataPacket", "", "", "Detecting GDF changes, applying it to Time Based Data.");
            timeBasedTestRunData.setGraphNamesObj(graphNames);
            timeBasedTestRunData.invalidateGraphDataIndex();
          }
        }
        else
        {
          if (timeBasedTestRunData.getGeneratorName() != null && !timeBasedTestRunData.getGeneratorName().equals(generatorName) || (timeBasedTestRunData.getGeneratorName() == null))
            continue;
          
          /*Need to check for GDF changes.*/
          if(msgData.getTestCtrlDataObj().isGDFChanged())
          {
            Log.debugLogAlways(className, "updateTestRunDataPacket", "", "", "Detecting GDF changes, applying it to Time Based Data for generator " + timeBasedTestRunData.getGeneratorName() + ".");
            timeBasedTestRunData.setGraphNamesObj(generatorGraphNames);
            timeBasedTestRunData.invalidateGraphDataIndex();
          }
        }

        // Check every time based data type.
        if (TestRunDataType.LAST_N_MINUTES_DATA != timeBasedTestRunData.getTestRunDataTypeObj().getType() && TestRunDataType.WHOLE_SCENARIO_DATA != timeBasedTestRunData.getTestRunDataTypeObj().getType())
        {
          if (debugLogLevel > 2)
            Log.debugLogAlways(className, "updateTestRunDataPacket", "", "", "Not Updating TimeBased for pairs.getKey =" + pairs.getKey());
          continue;
        }

        if (debugLogLevel > 2)
          Log.debugLogAlways(className, "updateTestRunDataPacket", "", "", "Updating time based data . generatorName = " + generatorName + " key = " + pairs.getKey());
        
        timeBasedTestRunData.putTimeBasedTestRunData(msgData);
        activePartitionDirName = new DecimalFormat("#").format(msgData.getPartitionFileName());
      }
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "updateTestRunDataPacket", "", "", "", ex);
    }
  }
  
  /**
   * Method is used to update New Added Monitor Graphs in active time based data object.
   * @param arrNewGraphDTOList
   */
  public void updateGraphsInTimeBasedDataObjects(ArrayList<GraphUniqueKeyDTO> arrNewGraphDTOList)
  {
    try
    {
      Log.debugLogAlways(className, "updateGraphsInTimeBasedDataObjects", "", "", "Method Called.");
      if (hMapTimeBasedTestRunData == null || hMapTimeBasedTestRunData.size() == 0)
      {
        Log.errorLog(className, "updateGraphsInTimeBasedDataObjects", "", "", "Bug: hMapTimeBasedTestRunData size = 0");
        return;
      }
      
      /*Getting Iterator From HashMap.*/
      Iterator<Entry<String, TimeBasedTestRunData>> it = hMapTimeBasedTestRunData.entrySet().iterator();

      /*Iterating through each time based object.*/
      while (it.hasNext())
      {
	/*Getting the Each Entry.*/
        Map.Entry<String, TimeBasedTestRunData> pairs = (Entry<String, TimeBasedTestRunData>) it.next();

        /*Getting Time Based Data object.*/
        TimeBasedTestRunData timeBasedTestRunData = pairs.getValue();
        
        Log.debugLogAlways(className, "updateGraphsInTimeBasedDataObjects", "", "", "Updating time based data for key = " + pairs.getKey());
        
        /*Activate Graphs in Time Based Data.*/
        timeBasedTestRunData.addAndActivateNewGraphDTO(arrNewGraphDTOList);
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Method for setting msgData of default timeBasedTestRunData to null.
   */
  public void setDefaultTimeBasedTestRunDataMsgData(data obj)
  {
    getDefaultTimeBasedTestRunData().setMsgData(obj);
  }

  /**
   * Method for getting sequence number based on timeBasedTestRunDataKey.
   */
  public long getLastSeqNumber(String key)
  {
    return getTimeBasedTestRunDataByKey(key).getSeqNumber();
  }

  // Setting debug log level
  public void setDebugLevel(int debugLogLevel)
  {
    this.debugLogLevel = debugLogLevel;
    Iterator it = hMapTimeBasedTestRunData.entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry<String, TimeBasedTestRunData> pairs = (Map.Entry) it.next();
      TimeBasedTestRunData timeBasedTestRunData = pairs.getValue();
      timeBasedTestRunData.setDebugLevel(debugLogLevel);
    }
  }

  // resetting data available flag of all TBTRD
  public void resetDataAvailableFlag()
  {
    Iterator it = hMapTimeBasedTestRunData.entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry<String, TimeBasedTestRunData> pairs = (Map.Entry) it.next();
      TimeBasedTestRunData timeBasedTestRunData = pairs.getValue();
      timeBasedTestRunData.resetDataAvailableFlag();
    }
  }

  public HashMap<String, TreeIconColorDTO> getHieararchicalTreeSettingData()
  {
    return hieararchicalTreeSettingData;
  }

  /* Getter method used to get baseline tracking data read from baselineTracking.dat file */
  public ArrayList<String> getBaseLineTrackingFileData()
  {
    return baseLineTrackingFileData;
  }

  /* Getter method used to set baseline tracking data to save in to baselineTracking.dat file */
  public void setBaseLineTrackingFileData(ArrayList<String> baseLineTR)
  {
    this.baseLineTrackingFileData = baseLineTR;
  }

  /**
   * Cloning Object through Clonable interface.
   */
  public TestRunData clone()
  {
    try
    {
      return (TestRunData) super.clone();
    }
    catch (CloneNotSupportedException e)
    {
      Log.errorLog(className, "clone", "", "", "Error in Cloning TestRunData object" + e);
      return null;
    }
  }
}

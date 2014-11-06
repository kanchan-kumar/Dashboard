/**
 * This class is for creating Time Based Test Run Data 
 * (eg. Last n hours/minutes data, Whole Scenario Data, specified date and time and for specified phase) 
 * 
 * @author Ravi Sharma
 * @since Netsorm Version 3.9.2
 * @Modification_History Ravi Kant Sharma - Initial Version 3.9.2
 * @version 3.9.2
 * 
 */
package pac1.Bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import pac1.Bean.GraphName.*;
import pac1.Bean.Percentile.PercentileDataKey;
import pac1.Bean.Percentile.PercentileInfo;

public class TimeBasedTestRunData implements Serializable, Cloneable
{
  private static final long serialVersionUID = 154276347229086122L; // Serial Version Id
  private static String className = "TimeBasedTestRunData";

  public double[] arrSeqNumber = null; // keeping Sequence Number array
  public long[] arrTimeStamp = null; // Keeping time stamp, Don't Make it private as It is used in ReportData.java
  private GraphUniqueKeyDTO[] arrGraphUniqueKeyDTO = null;
  private HashMap<GraphUniqueKeyDTO, TimeBasedDTO> hmTimeBasedDTO = new HashMap<GraphUniqueKeyDTO, TimeBasedDTO>(); // To store averaged graph data by GroupId.GraphId.VectorName

  private int granularity_Arrays_Incremented_size = 40; // Keeps size to increase if array size is full while applying granularity.
  private int maxSampleInGraph = 40;
  private int totalSampleForLast = 0; // Made class variable for logging purpose only
  private int movingStartFromSeq = Integer.MAX_VALUE;
  private int avgCounter = 0; // Counter of how many data packets received including lost pkts. Reset after averaging is done
  private int avgSampleCount = 0; // Actual Number of samples received for averaging (avgCounter-pktLost)
  private double avgSeqNum = 0.0; // Keeping avgSeqNum
  private boolean avgCountFlag = false; // Flag average is done, we need to update data in series at client side
  private int dataItemCount = 0; // keeping last sample index
  private boolean avgDoneFlag = false; // Flag for is averaging done, true - All samples averaging done, We need to reset graph time series at client
  private String timeBasedTestRunDataType; // It's keep the value for Test Run Data Type
  private int avgCount = 1; // keeping value for how many data values to be used for averaging
  private int granularity = -1;// Keeping Granularity/Resolution value
  public data msgData = null;// msg data, It contains info about data sample
  public long seqNum = 0;
  private int testRunDataType = 0; // This flag is to specify the testRunDataType (0 --> Normal Test Run Data, 1--> Baseline Test Run Data, 2 --> Tracked Test Run Data), by default it is 0
  private long sessionStartSeqNum = 0; // Session Sequence Number
  private String totalProcessingTime = "NotSet"; // total processing time
  private int prevOpcode = -1; // Keeping previous Opcode value, Used in Test Run Data
  private long prevSeqNum = 0; // Must set to 0, keeping previous Seq Num value, Used in TestRunData
  private int totalPktLost = 0;
  private int activeGraphCount = 0; // Keeping number of graphs in memory
  public TestRunData testRunData;
  public TestRunDataType testRunDataTypeObj;
  private int debugLogLevel = 0;
  private int currentPartitionGDFVersion = 0;

  GraphStats graphStatsObj = null; // Object of graph stats used for dual purpose, 1. Getting sample data from data.java 2. Sending averaged sample data for displaying in lower pane.
  TimeBasedDataUtils timeBasedDataUtils = null; // Used For Utilities function.
  private transient GraphNames graphNames = null;
  private String generatorName = null;
  private String generatorTRNum = null;

  private boolean isNDE_Continuous_Mode = false; /* variables tells about NDE Execution mode (Continuous Monitoring). */
  private double prevAbsTimeStamp = 0; /* variable for storing previous absolute time stamp */
  private double absTimeStamp = 0;/* variable for storing absolute time stamp */
  private double avgAbsTimeStamp = 0; /* variable used for storing average time stamp. */
  private double movingTimeStampInLastViewMode = Double.MAX_VALUE; /* Contains the timeStamp which decide moving data arrays of Last N view. */
  private int testRun_Partition_Type = 0; /* Contain the value of TestRun Partition Type. */
  private DerivedDataProcessor derivedDataProcessor = new DerivedDataProcessor(); /* Used For processing Derived Graph Data. */
  private String activeParitionName = "NA"; /* Variable Contain Active partition Name. */

  private HashMap<PercentileDataKey, PercentileInfo> percentileDataMap = new HashMap<PercentileDataKey, PercentileInfo>();

  public HashMap<PercentileDataKey, PercentileInfo> getPercentileDataMap()
  {
    return percentileDataMap;
  }

  public void setPercentileDataMap(HashMap<PercentileDataKey, PercentileInfo> percentileDataMap)
  {
    this.percentileDataMap = percentileDataMap;
  }

  /**
   * Constructor for TimeBased Test Run Data
   * 
   * @param testRunData
   * @param timeBasedData
   * @param testRunDataTypeObj
   */
  public TimeBasedTestRunData(TestRunData testRunData, String timeBasedData, TestRunDataType testRunDataTypeObj)
  {
    try
    {
      debugLogLevel = TimeBasedDataUtils.getDebugLevelFromConfig();

      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "TimeBasedTestRunData", "", "", "Constructor Called. testRunDataType = " + testRunDataType + ", timeBasedData = " + timeBasedData);

      setGraphNamesObj(testRunData.graphNames);

      maxSampleInGraph = testRunData.config_rtg_max_data_samples_in_one_graph;
      initForConstructor(testRunData, testRunDataTypeObj, timeBasedData);
    }
    catch (Exception ex)
    {
      Log.errorLog(className, "TimeBasedTestRunData", "", "", "Exception - " + ex);
    }
  }

  /**
   * This constructor set the test run number.
   * 
   * @param testNum
   * @param testRunDataType
   * @param testRunData
   * @param testRunDataTypeObj
   */
  public TimeBasedTestRunData(int testNum, int testRunDataType, TestRunData testRunData, TestRunDataType testRunDataTypeObj)
  {
    try
    {
      debugLogLevel = TimeBasedDataUtils.getDebugLevelFromConfig();

      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "TimeBasedTestRunData", "", "", "Constructor Called. testNum = " + testNum + ", testRunDataType = " + testRunDataType);

      maxSampleInGraph = testRunData.config_rtg_max_data_samples_in_one_graph;
      this.testRunDataType = testRunDataType;

      setGraphNamesObj(testRunData.graphNames);

      initForConstructor(testRunData, testRunDataTypeObj, testRunDataTypeObj.getHMapKey());
      msgData.setTestRun(testNum);
    }
    catch (Exception ex)
    {
      Log.errorLog(className, "TimeBasedTestRunData", "", "", "Exception - " + ex);
    }
  }

  /**
   * Initializing the Time Based Object.
   * 
   * @param testRunData
   * @param testRunDataTypeObj
   * @param timeBasedData
   */
  private void initForConstructor(TestRunData testRunData, TestRunDataType testRunDataTypeObj, String timeBasedData)
  {
    msgData = new data();
    avgSeqNum = 0;
    absTimeStamp = 0;
    dataItemCount = 0;
    seqNum = 0;

    this.timeBasedTestRunDataType = timeBasedData;
    this.testRunData = testRunData;
    this.testRunDataTypeObj = testRunDataTypeObj;
  }

  /**
   * Setting the Graph Names object.
   * 
   * @param graphNames
   */
  public void setGraphNamesObj(GraphNames graphNames)
  {
    if (timeBasedTestRunDataType != null)
      Log.debugLog(className, "setGraphNamesObj", "", "", "Setting graph Name object for " + this.timeBasedTestRunDataType);

    this.graphNames = graphNames;
  }

  /**
   * Return Graph Names object.
   * 
   * @return
   */
  public GraphNames getGraphNamesObj()
  {
    return graphNames;
  }

  /**
   * Injecting TestRunData required on client for processing data.
   * 
   * @param testRunDataObj
   * @return
   */
  public void setTestRunDataObj(TestRunData testRunData)
  {
    this.testRunData = testRunData;
  }

  /**
   * Method Returns object of TestRunData.
   * 
   * @return
   */
  public TestRunData getTestRunDataObj()
  {
    return testRunData;
  }

  public void setGeneratorName(String generatorName)
  {
    Log.debugLog(className, "setGraphNameObject", "", "", "Setting generator name(" + generatorName + ") for " + this.timeBasedTestRunDataType);
    this.generatorName = generatorName;
  }

  public void setGeneratorTRNum(String generatorTRNum)
  {
    this.generatorTRNum = generatorTRNum;
  }

  public String getGeneratorTRNum()
  {
    return this.generatorTRNum;
  }

  public int getCurrentPartitionGDFVersion()
  {
    return currentPartitionGDFVersion;
  }

  public void setCurrentPartitionGDFVersion(int currentPartitionGDFVersion) 
  {
    this.currentPartitionGDFVersion = currentPartitionGDFVersion;
  }

  /**
   * This method is used to initialize the time based test run data variables
   * 
   * @param granularity
   * @param arrGraphUniqueKeyDTO
   * @param testViewMode
   */
  public synchronized void initForNewTimeBasedTestRunData(int granularity, GraphUniqueKeyDTO[] arrGraphUniqueKeyDTO, boolean testViewMode)
  {
    try
    {
      if (generatorName == null)
        graphNames = testRunData.graphNames;
      else
        graphNames = testRunData.generatorGraphNames;

      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "initForNewTimeBasedTestRunData", "", "", "Method Called. granularity = " + granularity + ", testViewMode = " + testViewMode);

      long startTimeOfProcessing = System.currentTimeMillis();

      resetDataAvailableFlag();

      /* Creating Object of GraphStats.java for persisting values while calculation. */
      graphStatsObj = new GraphStats();

      this.arrGraphUniqueKeyDTO = arrGraphUniqueKeyDTO;

      /* Used for utilities function. */
      timeBasedDataUtils = new TimeBasedDataUtils();
      TimeBasedDataUtils.debugLogLevel = debugLogLevel;

      /* Setting NDE continuous keyword through test run data. */
      isNDE_Continuous_Mode = testRunData.isnDE_Continuous_Mode();

      /* Setting testRun_Partition_Type keyword value. */
      testRun_Partition_Type = testRunData.getTestRun_Partition_Type();

      this.prevSeqNum = 0; /* Must set as we are reusing this object in online */
      prevOpcode = data.END_PKT; /* Set it as we may get new TR without end of previous TR mode */

      this.granularity = granularity;

      avgCount = 1; /* Initializing avgCount as 1 */
      testRunData.testRunState = 1; /* New Test Run is Started. */

      avgSeqNum = 0;
      avgAbsTimeStamp = 0;
      dataItemCount = 0;
      seqNum = 0;

      if (generatorName == null)
        testRunData.prevTestRunNum = msgData.getTestRun();

      /* here we need to check if test is in online mode then no need to initialize all arrays which are not required. */
      msgData.initForNewTimeBasedData(graphNames);

      if (this.arrGraphUniqueKeyDTO == null)
        this.arrGraphUniqueKeyDTO = getActiveGraphUniqueKeyDTOArray();
      else
        this.arrGraphUniqueKeyDTO = arrGraphUniqueKeyDTO;

      /* Total Active Graphs. */
      activeGraphCount = this.arrGraphUniqueKeyDTO.length;

      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "initForNewTimeBasedTestRunData", "", "", "Total Active Graphs = " + activeGraphCount + ", Total Graphs = " + graphNames.getTotalNumOfGraphs());

      /* Initializing arrays for average data of samples. It is like Key as Graph */
      initDataArrays();

      /* Initializing Counter, flags and variables. */
      initAvgFields();

      /* Initializing and calculating parameters require for Last N Minute Data Request. */
      initForLastNMinutes();

      /* This Function calculate avgCount For granularity. */
      initForGranularity();

      if (maxSampleInGraph % 2 != 0)
      {
        if (debugLogLevel > 0)
          Log.debugLogAlways(className, "initForNewTimeBasedTestRunData", "", "", "maxSampleInGraph = " + maxSampleInGraph + " is odd. So making it even as " + (maxSampleInGraph + 1));

        maxSampleInGraph += 1;
      }

      long endTimeOfProcessing = System.currentTimeMillis();

      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "initForNewTimeBasedTestRunData", "", "", "Method End. Total Time Taken for initilization In Milliseconds = " + (endTimeOfProcessing - startTimeOfProcessing));
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "initForNewTimeBasedTestRunData", "", "", "Exception - ", ex);
    }
  }

  /**
   * Initializing Arrays of Sample Data, TimeStamp, Seq Number , Min , Max etc.
   */
  private void initDataArrays()
  {
    try
    {
      Log.debugLogAlways(className, "initDataArrays", "", "", "Method Called.");
      if (arrGraphUniqueKeyDTO != null)
      {
        for (int k = 0; k < this.arrGraphUniqueKeyDTO.length; k++)
        {
          GraphUniqueKeyDTO graphUniqueKeyDTO = this.arrGraphUniqueKeyDTO[k];
          if (graphUniqueKeyDTO == null)
            continue;

          TimeBasedDTO timeBasedDTO = new TimeBasedDTO();
          timeBasedDTO.setGraphUniqueKeyDTO(graphUniqueKeyDTO);
          timeBasedDTO.initTimeBasedDTO(maxSampleInGraph);
          hmTimeBasedDTO.put(graphUniqueKeyDTO, timeBasedDTO);
        }
      }
      else
      {
        Log.errorLog(className, "initDataArrays", "", "", "arrGraphUniqueKeyDTO is null.");
      }

      /* Array for keeping the time stamp */
      arrTimeStamp = new long[maxSampleInGraph];

      /* Array for keeping sequence number. */
      arrSeqNumber = new double[maxSampleInGraph];
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "initDataArrays", "", "", "Exception - ", e);
    }
  }

  /**
   * Method is used to add New Graphs in Graph Unique key DTO List.
   * 
   * @param arrGraphUniqueKeyDTOList
   */
  public void addAndActivateNewGraphDTO(ArrayList<GraphUniqueKeyDTO> arrGraphUniqueKeyDTOList)
  {
    try
    {
      Log.debugLogAlways(className, "addAndActivateNewGraphDTO", "", "", "Method Called.");

      if (arrGraphUniqueKeyDTOList == null || arrGraphUniqueKeyDTOList.size() == 0)
      {
        Log.debugLogAlways(className, "addAndActivateNewGraphDTO", "", "", "Graph DTO list must not be null or blank.");
        Log.errorLog(className, "addAndActivateNewGraphDTO", "", "", "Graph DTO list must not be null or blank.");
        return;
      }

      /* Adding New Graphs to Graph DTO List. */
      for (GraphUniqueKeyDTO graphUniqueKeyDTO : arrGraphUniqueKeyDTOList)
      {
        TimeBasedDTO timeBasedDTOObj = new TimeBasedDTO();
        timeBasedDTOObj.initTimeBasedDTO(maxSampleInGraph);
        hmTimeBasedDTO.put(graphUniqueKeyDTO, timeBasedDTOObj);
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "addAndActivateNewGraphDTO", "", "", "Exception - ", e);
    }
  }

  /**
   * This Method Calculates avgCount For granularity.
   */
  private void initForGranularity()
  {
    try
    {
      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "initForGranularity", "", "", "Interval = " + testRunData.interval + " granularity = " + granularity);

      if (granularity != -1)
      {
        avgCount = (int) ((granularity * 1000) / testRunData.interval);
        Log.debugLogAlways(className, "initForGranularity", "", "", "Calculating average count, User defined Granularity = " + granularity + " (in secs), Interval = " + testRunData.interval + " (in msecs) calculated average count = " + avgCount);
      }
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "initForGranularity", "", "", "Exception - ", ex);
    }
  }

  /**
   * Set required information like last 10 minutes, last 1 hours data
   */
  private void initForLastNMinutes()
  {
    try
    {
      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "initForLastNMinutes", "", "", "Method Called. timeBasedTestRunDataType = " + timeBasedTestRunDataType);

      if (testRunDataTypeObj.getType() != TestRunDataType.LAST_N_MINUTES_DATA)
      {
        Log.errorLog(className, "initForLastNMinutes", "", "", "testRunDataTypeObj is not correct as testRunDataTypeObj = " + testRunDataTypeObj);
        return;
      }

      /* This is the total time of last n type of test run data */
      long lastNMinutesDuration = testRunDataTypeObj.getLastNMinutesValue() * 60 * 1000;

      if (lastNMinutesDuration == 0)
      {
        Log.errorLog(className, "initForLastNMinutes", "", "", "Bug: lastNMinutesDuration = 0, Returning...");
        return;
      }

      totalSampleForLast = (int) (lastNMinutesDuration / testRunData.interval);
      maxSampleInGraph = Math.min(maxSampleInGraph, totalSampleForLast);
      movingStartFromSeq = totalSampleForLast;

      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "initForLastNMinutes", "", "", "Method End. totalSampleForLast = " + totalSampleForLast + ", maxSampleInGraph = " + maxSampleInGraph + ", movingStartFromSeq = " + movingStartFromSeq);
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "initForLastNMinutes", "", "", "Exception - ", ex);
    }
  }

  /**
   * This method calculates total processing time
   * 
   * @param startProcessingTime
   * @param endProcessingTime
   */
  public void setTotalProcessingTime(long startProcessingTime, long endProcessingTime)
  {
    try
    {
      this.totalProcessingTime = rptUtilsBean.convertMilliSecToSecs(endProcessingTime - startProcessingTime);
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "setTotalProcessingTime", "", "", "Exception - ", ex);
    }
  }

  /**
   * This method return the total processing time
   * 
   * @return
   */
  public String getTotalProcessingTime()
  {
    return totalProcessingTime;
  }

  /**
   * This method is to get graph data last by graph DTO & index
   * 
   * @author Ravi
   * @param graphNum
   * @param index
   *          --> DateItemCount
   * @return
   */
  public double getGraphDataLastByGraphAndIndex(GraphUniqueKeyDTO graphUniqueKeyDTO, int index)
  {
    try
    {
      if (graphUniqueKeyDTO == null)
      {
        Log.debugLogAlways(className, "getGraphDataLastByGraphAndIndex", "", "", "graphUniqueKeyDTO is null so assigning 0.0");
        return 0.0;
      }

      TimeBasedDTO timeBasedDTOObj = hmTimeBasedDTO.get(graphUniqueKeyDTO);
      double[] dataValue = timeBasedDTOObj.getArrGraphSamplesData();
      return dataValue[index];
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getGraphDataLastByGraphAndIndex", "", "", "graphUniqueKeyDTO = " + graphUniqueKeyDTO + ", Exception - ", ex);
      return 0;
    }
  }

  /**
   * This method returns all Min data sample (in double[] array) of specified graph DTO.
   * 
   * @param graphNum
   * @return
   */
  public double[] getMinGraphDataByGraph(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    try
    {
      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "getMinGraphDataByGraph", "", "", "Method Called. graphUniqueKeyDTO = " + graphUniqueKeyDTO);

      TimeBasedDTO timeBasedDTOObj = hmTimeBasedDTO.get(graphUniqueKeyDTO);
      return timeBasedDTOObj.getArrMinData();
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getMinGraphDataByGraph", "", "", "Exception - ", ex);
    }

    return null;
  }

  /**
   * This method returns all Max data sample (in double[] array) of specified graph DTO.
   * 
   * @param graph
   * @return
   */
  public double[] getMaxGraphDataByGraph(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    try
    {
      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "getMaxGraphDataByGraph", "", "", "graphUniqueKeyDTO = " + graphUniqueKeyDTO);

      TimeBasedDTO timeBasedDTOObj = hmTimeBasedDTO.get(graphUniqueKeyDTO);
      return timeBasedDTOObj.getArrMaxData();
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getMaxGraphDataByGraph", "", "", "Exception - ", ex);
    }

    return null;
  }

  /**
   * This method returns all Count data sample (in int[] array) of specified graph DTO.
   * 
   * @param graphNum
   * @return
   */
  public int[] getCountArrayByGraph(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    try
    {
      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "getCountArrayByGraph", "", "", "graphUniqueKeyDTO = " + graphUniqueKeyDTO);

      return hmTimeBasedDTO.get(graphUniqueKeyDTO).getArrCountData();
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getCountArrayByGraph", "", "", "Exception - ", ex);
    }

    return null;
  }

  /**
   * This method returns all Sum Sqr data sample (in double[] array) of specified graph DTO.
   * 
   * @param graphNum
   * @return
   */
  public double[] getSumSqrArrayByGraph(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    try
    {
      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "getSumSqrArrayByGraph", "", "", "graphUniqueKeyDTO = " + graphUniqueKeyDTO);

      return hmTimeBasedDTO.get(graphUniqueKeyDTO).getArrSumSqrData();
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getSumSqrArrayByGraph", "", "", "Exception - ", ex);
    }

    return null;
  }

  /**
   * Check data is present for Graph DTO in memory
   * 
   * @param graphUniqueKeyDTO
   * @return
   */
  public boolean isGraphExistInMemory(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    try
    {
      return hmTimeBasedDTO.containsKey(graphUniqueKeyDTO);
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "isGraphExistInMemory", "", "", "Exception - ", ex);
      return false;
    }
  }

  /**
   * this function is to get seq number by index
   * 
   * @param index
   * @return
   */
  public double getSeqNumByIndex(int index)
  {
    try
    {
      if (index != -1)
        return arrSeqNumber[index];
      else
        return 1;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getSeqNumByIndex", "", "", "Exception - ", e);
      return 1;
    }
  }

  /**
   * This method is to get time stamp number by index
   * 
   * @author Ravi
   * @param index
   * @return
   */
  public long getTimeStampByIndex(int index)
  {
    try
    {
      if (index != -1)
        return arrTimeStamp[index];
      else
        return System.currentTimeMillis();
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getTimeStampByIndex", "", "", "Exception - ", e);
      return System.currentTimeMillis();
    }
  }

  /**
   * This Method Return the Start Time Based On Start Sequence Number and Interval for DDR.
   */
  public long getElapsedStartTimeInMillies(long interval)
  {
    long elapsedStartTimeInMillies = 0;

    try
    {
      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "getElapsedStartTimeInMillies", "", "", "interval = " + interval);

      if (testRunDataTypeObj == null)
      {
        Log.debugLogAlways(className, "getElapsedStartTimeInMillies", "", "", "testRunDataTypeObj must not be null.");
        return elapsedStartTimeInMillies;
      }

      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "getElapsedStartTimeInMillies", "", "", "Key = " + testRunDataTypeObj.getHMapKey() + " avgCounter = " + avgCounter + " avgCount = " + avgCount);

      // Here we check data type.
      // Note - Previously we add avgCounter but diff is still coming. But by subtracting avgCount it is more accurate.
      if (testRunDataTypeObj.getHMapKey().trim().startsWith("Last_"))
        elapsedStartTimeInMillies = (long) ((arrSeqNumber[0] - avgCount) * interval);
      else if (testRunDataTypeObj.getHMapKey().trim().startsWith("SPECIFIED_TIME_"))
        elapsedStartTimeInMillies = rptUtilsBean.convStrToMilliSec(testRunDataTypeObj.getStartDateTime());
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getGraphDataLastByGraphNumberAndIndex", "", "", "Exception - ", e);
    }
    return elapsedStartTimeInMillies;
  }

  /**
   * This Method Return the End Time Based On End Sequence Number and Interval for DDR.
   */
  public long getElapsedEndTimeInMillies(long interval)
  {
    long elapsedEndTimeInMillies = 0L;

    try
    {
      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "getElapsedEndTimeInMillies", "", "", "interval = " + interval);

      if (testRunDataTypeObj == null)
      {
        Log.debugLogAlways(className, "getElapsedEndTimeInMillies", "", "", "testRunDataTypeObj must not be null.");
        return elapsedEndTimeInMillies;
      }

      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "getElapsedStartTimeInMillies", "", "", "Key = " + testRunDataTypeObj.getHMapKey());

      if (testRunDataTypeObj.getHMapKey().trim().startsWith("SPECIFIED_TIME_"))
        elapsedEndTimeInMillies = rptUtilsBean.convStrToMilliSec(testRunDataTypeObj.getEndDateTime());
      else
        elapsedEndTimeInMillies = (seqNum * interval);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getGraphDataLastByGraphNumberAndIndex", "", "", "Exception - ", e);
    }
    return elapsedEndTimeInMillies;
  }

  /**
   * Method is used to create Empty Packets Data for those Graphs which are discontinued.
   * 
   * @param graphStats
   */
  private void updateEmptyPacketInDiscontinuedGraphs(GraphStats graphStats, TimeBasedDTO timeBasedDTOObj)
  {
    try
    {
      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "updateEmptyPacketInDiscontinuedGraphs", "", "", "Method Called. graphStats = " + graphStats + ", timeBasedDTOObj = " + timeBasedDTOObj);

      /* Filling the No Data Values. */
      /*
       * graphStats.setCount(DataPacketInfo.INT_NO_DATA_IDENTITY); graphStats.setMinData(DataPacketInfo.DOUBLE_NO_DATA_IDENTITY); graphStats.setMaxData(DataPacketInfo.DOUBLE_NO_DATA_IDENTITY); graphStats.setLastSample(DataPacketInfo.DOUBLE_NO_DATA_IDENTITY); graphStats.setGraphValue(DataPacketInfo.DOUBLE_NO_DATA_IDENTITY); graphStats.setSumSquare(DataPacketInfo.DOUBLE_NO_DATA_IDENTITY);
       */

      /* Need to Filled Time Based Array with No Data Identity Values to identity Graphs with No Data Availability. */
      /* Update Last Sample Value with No Data Identity. */
      double lastAvgSampleData = timeBasedDTOObj.getLastAvgSampleData();

      if (lastAvgSampleData <= 0)
        lastAvgSampleData = DataPacketInfo.DOUBLE_NO_DATA_IDENTITY;

      timeBasedDTOObj.setLastAvgSampleData(lastAvgSampleData);

      /* Update Min Data with No Data Identity */
      double lastMinData = timeBasedDTOObj.getLastMinData();

      /* Need to check if it already contain an averaged Min Sample. */
      if (lastMinData == Double.MAX_VALUE)
        timeBasedDTOObj.setLastMinData(DataPacketInfo.DOUBLE_NO_DATA_IDENTITY);

      /* Update Last Max DAta with No Data Identity. */
      double lastMaxData = timeBasedDTOObj.getLastMaxData();

      /* Need to check if it already contain an averaged Max Sample. */
      if (lastMaxData <= 0)
        timeBasedDTOObj.setLastMaxData(DataPacketInfo.DOUBLE_NO_DATA_IDENTITY);

      /* Update Last Count Data with No Data Identity. */
      int lastCountData = timeBasedDTOObj.getLastCountData();

      /* Need to check if it already contain an averaged count Sample. */
      if (lastCountData <= 0)
        timeBasedDTOObj.setLastCountData(DataPacketInfo.INT_NO_DATA_IDENTITY);

      /* Update Last Standard deviation Data with No Data Identity. */
      double lastStdDevData = timeBasedDTOObj.getLastSumSqrData();

      /* Need to check if it already contain an averaged Standard deviation Sample. */
      if (lastStdDevData <= 0)
        timeBasedDTOObj.setLastSumSqrData(DataPacketInfo.DOUBLE_NO_DATA_IDENTITY);

      /* Update Last Sample Data. */
      timeBasedDTOObj.setLastSampleData(DataPacketInfo.DOUBLE_NO_DATA_IDENTITY);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "updateEmptyPacketInDiscontinuedGraphs", "", "", "Exception - ", e);
    }
  }

  /**
   * Getting the Sequence difference from time stamp.
   * 
   * @return
   */
  private int getSequenceDiffFromTimeStamp()
  {
    try
    {
      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "getSequenceDiffFromTimeStamp", "", "", "absTimeStamp = " + absTimeStamp + ", prevAbsTimeStamp = " + prevAbsTimeStamp + ", interval = " + testRunData.interval);

      if (prevAbsTimeStamp == 0)
        return 1;

      return (int) ((absTimeStamp - prevAbsTimeStamp) / testRunData.interval);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getSequenceDiffFromTimeStamp", "", "", "Exception - ", e);
      return 0;
    }
  }

  /**
   * Method is used to validate data packet, it checks opcode, sequence number and time stamp.
   * 
   * @param msgData
   */
  public synchronized void putTimeBasedTestRunData(data msgData)
  {
    try
    {
      if (debugLogLevel > 0 && generatorName != null)
        Log.debugLog(className, "putTimeBasedTestRunData", "", "", "Updating data packet come for generator - " + generatorName);

      /* Checking for correct data packet size. */
      if (msgData.getMsgDataLength() != graphNames.getSizeOfMsgData())
      {
        Log.debugLogAlways(className, "putTimeBasedTestRunData", "", "", "Incoming Packet size is mismatching with existing GDF. Ignoring Packet. Current Packet size = " + msgData.getMsgDataLength() + ", current GDF Packet Size = " + graphNames.getSizeOfMsgData());
        return;
      }

      /* Getting the Current Sequence Number. */
      this.msgData = msgData;
      long currentSeqNum = msgData.getSeqNum();

      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "putTimeBasedTestRunData", "", "", "Method Called. TestRun = " + msgData.getTestRun() + ", Opcode = " + msgData.getOpcode() + ", prevSeqNum = " + prevSeqNum + ", currentSeqNum = " + currentSeqNum);

      /* Checking for Opcode and Data Packets. */
      if (msgData.getOpcode() != data.DATA_PKT)
      {
        if ((msgData.getOpcode() != data.START_PKT) && (msgData.getOpcode() != data.END_PKT))
          Log.debugLog(className, "putTimeBasedTestRunData", "", "", "Error: Invalid opcode = " + msgData.getOpcode() + ". TestRun = " + msgData.getTestRun() + ", prevSeqNum = " + prevSeqNum + ", currentSeqNum = " + currentSeqNum);

        if (msgData.getOpcode() == data.END_PKT)
          Log.debugLogAlways(className, "putTimeBasedTestRunData", "", "", "Test Run Number " + testRunData.prevTestRunNum + " is over");

        /* Keeping Previous Sequence Number and Opcode. */
        prevOpcode = msgData.getOpcode();
        prevSeqNum = currentSeqNum - 1;
        return;
      }

      /* Code will come here only for data packet */
      /* TODO - Is this code is still Needed. */
      if (prevOpcode == data.END_PKT)
      {
        long prevTestRun = -1;
        if (testRunData != null)
          prevTestRun = -1;

        Log.errorLog(className, "putTimeBasedTestRunData", "", "", "Bug - Previous Test Run is over. Data packet recieved without start packet. currentTestRun = " + msgData.getTestRun() + ", prevTestRun = " + prevTestRun + ", currentSeqNum = " + currentSeqNum + ", prevSeqNum = " + prevSeqNum + ", curentOpcode = " + msgData.getOpcode() + ", prevOpcode = " + prevOpcode + ". Setting prevSeqNum to currrentSeqNum - 1");
        prevSeqNum = currentSeqNum - 1; // We need to set prevSeqNum as that seqDiff is correct
      }

      /* On start of GUI we get TRD followed by msgData. In this case, this packet is already processed in TRD. */
      /* We detect it by checking seqDiff. If <= 0, then it means this packet is already processed. So ignore it. */
      int seqDiff = 0;

      if (debugLogLevel > 1)
        Log.debugLogAlways(className, "putTimeBasedTestRunData", "", "", "absTimeStamp = " + absTimeStamp + " Format = " + ExecutionDateTime.convertDateTimeStampToFormattedString((long) absTimeStamp, "MM/dd/yy HH:mm:ss", testRunData.trTimeZone));

      /* Checking for Test Run Partition Type. In Old Format Test Run we use Sequence Number, But for New Test Runs after 3.9.4 the data is generated with Time Stamp. */
      if (testRun_Partition_Type <= 0)
      {
        seqDiff = (int) (currentSeqNum - prevSeqNum);

        if ((msgData.getOpcode() == data.DATA_PKT) && (currentSeqNum != prevSeqNum + 1))
        {
          totalPktLost = (int) (totalPktLost + (seqDiff - 1));
          Log.errorLog(className, "putTimeBasedTestRunData", "", "", "msgData with incorrect sequence received, data may be lost. Opcode = " + msgData.getOpcode() + ", SeqNum = " + currentSeqNum + ", prevSeqNum = " + prevSeqNum + ", Total Pkt Lost = " + totalPktLost);
        }
      }
      else
      {
        /* Getting the Absolute Time From Data Format. */
        absTimeStamp = msgData.getAbsTimeStamp();

        /* Checking For Same Packet/Invalid Packet. */
        if (prevAbsTimeStamp >= absTimeStamp)
        {
          Log.debugLogAlways(className, "putTimeBasedTestRunData", "", "", "Duplicate Packet. Ignoring.");
          return;
        }

        if (debugLogLevel > 0)
          Log.debugLogAlways(className, "putTimeBasedTestRunData", "", "", "absTimeStamp = " + absTimeStamp + " Format = " + ExecutionDateTime.convertDateTimeStampToFormattedString((long) absTimeStamp, "MM/dd/yy HH:mm:ss", testRunData.trTimeZone));

        /* Getting Different between Two consecutive packets. */
        seqDiff = (int) getSequenceDiffFromTimeStamp();
      }

      /* Checking for Consecutive packet difference. */
      if (seqDiff <= 0)
      {
        if (debugLogLevel > 0)
          Log.debugLogAlways(className, "putTimeBasedTestRunData", "", "", "seqDiff = " + seqDiff + ". So No need to put msgData.");

        return;
      }

      /* Getting the Opcode, Sequence Number and Time Stamp For validating Next Incoming Packet. */
      prevOpcode = msgData.getOpcode();
      prevSeqNum = currentSeqNum;
      prevAbsTimeStamp = absTimeStamp;

      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "putTimeBasedTestRunData", "", "", "prevOpcode = " + prevOpcode + ", prevSeqNum = " + prevSeqNum + ", prevAbsTimeStamp = " + prevAbsTimeStamp + ", seqDiff = " + seqDiff);

      /* Update the Time Based Graph Data by processing packet data. */
      updateArrayWithTestRunData(msgData, seqDiff);
      
      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "putTimeBasedTestRunData", "", "", "Method End. dataItemCount= " + dataItemCount + ", avgDoneFlag = " + avgDoneFlag + ", avgCountFlag = " + avgCountFlag);
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "putTimeBasedTestRunData", "", "", "Exception - ", ex);
    }
  }

  /**
   * Update Data Sample in Respected Graphs Time Based DTO and averaging sample based on provided inputs.
   * 
   * @param msgData
   * @param seqDiff
   */
  private void updateArrayWithTestRunData(data msgData, int seqDiff)
  {
    try
    {
      Log.debugLogAlways(className, "updateArrayWithTestRunData", "", "", "Packet Data Size = " + msgData.getMsgDataLength() + ", seqDiff = " + seqDiff + ", Absolute Packet TimeStamp = " + ExecutionDateTime.convertDateTimeStampToFormattedString((long) absTimeStamp, "MM/dd/yy HH:mm:ss", testRunData.trTimeZone) + ", currentPartitionGDFVersion = " + currentPartitionGDFVersion);

      if (debugLogLevel > 2)
        Log.debugLogAlways(className, "updateArrayWithTestRunData", "", "", "Method Called. TimeBasedTestRunDataType = " + timeBasedTestRunDataType + ", seqDiff = " + seqDiff);

      /* Get Sample Data By Processing Packets, average sample and Update Time Based DTO. */
      updateAndCreateTimeBasedData(msgData);

      /* Getting Current Sequence Number. Not Available in Future Test Run. */
      seqNum = msgData.getSeqNum();

      if (debugLogLevel > 2)
        Log.debugLogAlways(className, "updateArrayWithTestRunData", "", "", "seq number = " + seqNum + " movingTimeStampInLastViewMode = " + movingTimeStampInLastViewMode + ", currentTimeStamp = " + absTimeStamp + "dataItemCount = " + dataItemCount + " avgCounter = " + avgCounter + ", avgCount = " + avgCount + ", avgSampleCount = " + avgSampleCount);

      // TODO - Need to remove test run data object dependency
      if (testRunData != null)
      {
        testRunData.interval = msgData.getInterval(); // TODO - Why We need this

        /* will not set in case of generator. */
        if (generatorName == null)
          testRunData.testNum = msgData.getTestRun(); // TODO - Why We need this
      }

      /* Getting Average of Sequence Numbers. */
      avgSeqNum = avgSeqNum + (double) seqNum;

      /* Getting Average of Time Stamp */
      avgAbsTimeStamp = avgAbsTimeStamp + absTimeStamp;

      avgCounter += seqDiff;
      avgSampleCount++;
      avgCountFlag = false;

      /* Update Derived Graph statistics. */
      derivedDataProcessor.updateDerivedGraphStats();

      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "updateArrayWithTestRunData", "", "", "avgCounter = " + avgCounter + ", avgCount = " + avgCount + ", avgSampleCount = " + avgSampleCount + ", testRun_Partition_Type = " + testRun_Partition_Type);

      /* Checking Number of Packets received for averaging. */
      if (avgCounter >= avgCount)
      {
        double movingPoint = movingStartFromSeq;
        double currentTimeStamp = seqNum;

        if (testRun_Partition_Type > 0)
        {
          movingPoint = movingTimeStampInLastViewMode;
          currentTimeStamp = absTimeStamp;
        }

        if (currentTimeStamp > movingPoint)
        {
          mvDataValuesInArray();
          derivedDataProcessor.updateRecurringSamplesInLastNMinutes(avgCounter);
          genAvgData();
        }
        else
        {
          /* Auto averaging is off for baseline TR where granularity is specified by user. */
          if (granularity == -1)
          {
            if (debugLogLevel > 0)
              Log.debugLogAlways(className, "updateArrayWithTestRunData", "", "", "Auto Averaging with granularity = " + granularity + ", dataItemCount = " + dataItemCount + ", seqNum = " + seqNum);

            if (dataItemCount == maxSampleInGraph)
              averageAllSamples();
          }

          if (avgCounter >= avgCount)
            genAvgData();
        }
      }

      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "updateArrayWithTestRunData", "", "", "Method End. avgDoneFlag = " + avgDoneFlag + ", avgCountFlag = " + avgCountFlag + ", movingStartFromSeq = " + movingStartFromSeq + "\n");
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "updateArrayWithTestRunData", "", "", "Exception - ", ex);
    }
  }

  /**
   * Method is used to Update Time Based Data with Incoming Time Based Data.
   * 
   * @param msgData
   */
  private synchronized void updateAndCreateTimeBasedData(data msgData)
  {
    try
    {
      /* Iterate through each HashMap Graph Entry and Update associated Time Based DTO objects. */
      for (Map.Entry<GraphUniqueKeyDTO, TimeBasedDTO> entry : hmTimeBasedDTO.entrySet())
      {

        /* Getting the Graph DTO Entry From HashMap Iterator. */
        GraphUniqueKeyDTO graphUniqueKeyDTO = entry.getKey();

        /* Check the availability of Graph DTO. */
        if (graphUniqueKeyDTO == null)
        {
          Log.errorLog(className, "updateAndCreateTimeBasedData", "", "", "graphUniqueKeyDTO is null");
          continue;
        }

        /* Getting the Time Based DTO From HashMap Iterator. */
        TimeBasedDTO timeBasedDTO = entry.getValue();
        if (timeBasedDTO == null)
        {
          Log.errorLog(className, "updateAndCreateTimeBasedData", "", "", "timeBasedDTO is null");
          continue;
        }

        /* Getting the Graph Data Index From Graph DTO. */
        int graphDataIndex = -1;

        if (graphUniqueKeyDTO.getGraphDataIndex() == -1)
        {
          graphDataIndex = graphNames.getGraphDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTO);
          graphUniqueKeyDTO.setGraphDataIndex(graphDataIndex);
        }
        else
        {
          graphDataIndex = graphUniqueKeyDTO.getGraphDataIndex();
        }

        if (graphDataIndex == -1)
        {
          if (debugLogLevel > 2)
            Log.errorLog(className, "updateAndCreateTimeBasedData", "", "", "Graph with Group Id = " + graphUniqueKeyDTO.getGroupId() + ", Graph Id = " + graphUniqueKeyDTO.getGraphId() + " and Vector Name = " + graphUniqueKeyDTO.getVectorName() + " is not available is current Partition. Active Partition Name is = " + activeParitionName);

          /* Filling Empty Packet Data in discontinued Graph. */
          updateEmptyPacketInDiscontinuedGraphs(graphStatsObj, timeBasedDTO);
        }
        else
        {

          /* Getting the Graph Statistics from data object. */
          msgData.getGraphStats(graphDataIndex, graphStatsObj);

          if (TimeBasedDataUtils.isEmptySample(graphStatsObj.getGraphValue()))
          {

            /* Filling Empty Packet Data for No Data Available for Graph. */
            updateEmptyPacketInDiscontinuedGraphs(graphStatsObj, timeBasedDTO);
          }
          else
          {
            /* Now Update Time Based DTO of Respected Graph. */
            updateTimeBasedDTO(timeBasedDTO, graphStatsObj);
          }
        }

        if (debugLogLevel > 3)
          Log.debugLogAlways(className, "updateTimeBasedDTO", "", "", "graphUniqueKeyDTO = " + graphUniqueKeyDTO + ", timeBasedDTO = " + timeBasedDTO);
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "updateTimeBasedDTO", "", "", "Exception - ", e);
    }
  }

  /**
   * Method Updates the Available Graph Time Based DTO.
   * 
   * @param msgData
   */
  private void updateTimeBasedDTO(TimeBasedDTO timeBasedDTO, GraphStats graphStatsObj)
  {
    try
    {
      if (debugLogLevel > 3)
        Log.debugLogAlways(className, "updateTimeBasedDTO", "", "", "Method Called. timeBasedDTO = " + timeBasedDTO + ", graphStatsObj = " + graphStatsObj);

      /* Update the Last Averaged Sample Data in Time Based DTO for associated Graph. */
      double lastAvgSampleData = timeBasedDTO.getLastAvgSampleData();
      lastAvgSampleData += graphStatsObj.getGraphValue();
      timeBasedDTO.setLastAvgSampleData(lastAvgSampleData);

      /* Update the Last Min Sample Data into Time Based DTO for associated Graph. */
      double lastMinData = timeBasedDTO.getLastMinData();
      if (graphStatsObj.getMinData() < lastMinData)
      {
        lastMinData = graphStatsObj.getMinData();
        timeBasedDTO.setLastMinData(lastMinData);
      }

      /* Update the Last Max Sample Data into Time Based DTO for associated Graph. */
      double lastMaxData = timeBasedDTO.getLastMaxData();
      if (graphStatsObj.getMaxData() > lastMaxData)
      {
        lastMaxData = graphStatsObj.getMaxData();
        timeBasedDTO.setLastMaxData(lastMaxData);
      }

      /* Update the Last Count Sample Data into Time Based DTO for associated Graph. */
      int lastCountData = timeBasedDTO.getLastCountData();
      lastCountData += graphStatsObj.getCount();
      timeBasedDTO.setLastCountData(lastCountData);

      /* Update the Last Sum Square Sample Data into Time Based DTO for associated Graph. */
      double lastSumSqrData = timeBasedDTO.getLastSumSqrData();
      lastSumSqrData += graphStatsObj.getSumSquare();
      timeBasedDTO.setLastSumSqrData(lastSumSqrData);

      /* Update the Last Sample Data(not averaged) into Time Based DTO for associated Graph. */
      double lastSampleData = graphStatsObj.getLastSample();
      timeBasedDTO.setLastSampleData(lastSampleData);
    }
    catch (Exception ex)
    {
      Log.errorLog(className, "updateTimeBasedDTO", "", "", "hmTimeBasedDTO = " + hmTimeBasedDTO);
      Log.stackTraceLog(className, "updateTimeBasedDTO", "", "", "Exception - ", ex);
    }
  }

  /**
   * Averaging of all samples if arrays are filled with required/Mentioned Number of Averaged Sample.</br> 1 Average whole series. </br> 2 Two data items as averaged to one.
   */
  public synchronized void averageAllSamples()
  {
    try
    {
      Log.debugLogAlways(className, "averageAllSamples", "", "", "Averaging all samples for all graphs. Current SeqNum = " + seqNum + ". Current AvgCount = " + avgCount);

      /* Available Graph To be Processed. */
      int graphCount = 0;

      /* Iterating through available number of Graph DTO, and average samples in associated Time Based data. */
      for (Map.Entry<GraphUniqueKeyDTO, TimeBasedDTO> entry : hmTimeBasedDTO.entrySet())
      {

        /* Getting Graph DTO from hashMap. */
        GraphUniqueKeyDTO graphUniqueKeyDTO = entry.getKey();
        try
        {

          if (graphUniqueKeyDTO == null)
          {
            Log.errorLog(className, "averageAllSamples", "", "", "graphUniqueKeyDTO is null.");
            continue;
          }

          /* Getting Time Based DTO from HashMap */
          TimeBasedDTO timeBasedDTO = entry.getValue();

          /* Getting Data Arrays of Graphs. */
          int[] arrCountData = timeBasedDTO.getArrCountData();
          double[] arrGraphSamplesData = timeBasedDTO.getArrGraphSamplesData();
          double[] arrSumSqrData = timeBasedDTO.getArrSumSqrData();
          double[] arrMinData = timeBasedDTO.getArrMinData();
          double[] arrMaxData = timeBasedDTO.getArrMaxData();

          /* Getting Graph Data Index from respected Graph DTO. */
          int graphDataIndex = graphUniqueKeyDTO.getGraphDataIndex();
          int graphDataType = -1;

          /* Getting Graph Data Type. If Graph Available in Current Partition GDF. */
          if (graphDataIndex != -1)
            graphDataType = graphNames.getDataTypeNumByGraphUniqueKeyDTO(graphUniqueKeyDTO);

          /* Count Number of Averaged Sample. */
          int k = 0;

          for (int j = 0; (j + 1) <= dataItemCount; j = j + 2)
          {
            int startIndex = j;
            int endIndex = j + 1;
            double dataValueTemp = 0;
            int totalAvgCount = 0;
            double dataValueTemp1 = arrGraphSamplesData[startIndex];
            double dataValueTemp2 = arrGraphSamplesData[endIndex];

            //Pydi Warning:we not handled the case of dataValueTemp1 = x and dataValueTemp2 = -0.0 so no gap
            //Warning:we not handled the case of dataValueTemp1 = -0.0 and dataValueTemp2 = x so no gap
            /* Check for Graph Data Availability */
            /* If Both Sample Data Contains No Data Then No Need for Further Calculation. */
            if (TimeBasedDataUtils.isEmptySample(dataValueTemp1) && TimeBasedDataUtils.isEmptySample(dataValueTemp2))
            {
              dataValueTemp = DataPacketInfo.DOUBLE_NO_DATA_IDENTITY;
              totalAvgCount = DataPacketInfo.INT_NO_DATA_IDENTITY;
            }
            else
            {
              /* Calculating Total Count. */
              totalAvgCount = (arrCountData[startIndex] + arrCountData[endIndex]);

              /* Here we check if Graph is of Time/TimesSTD type then it must calculated by this way. */
              if (graphDataType == GraphNameUtils.DATA_TYPE_TIMES || graphDataType == GraphNameUtils.DATA_TYPE_TIMES_STD)
              {
                if (totalAvgCount > 0)
                  dataValueTemp = ((dataValueTemp1 * arrCountData[startIndex]) + (dataValueTemp2 * arrCountData[endIndex])) / totalAvgCount;
                else
                  dataValueTemp = 0;
              }
              else
              {
                dataValueTemp = (dataValueTemp1 + dataValueTemp2) / 2;
              }
            }

            /* Average based on data type */
            arrGraphSamplesData[k] = dataValueTemp;

            /* Averaging of Count Data. */
            arrCountData[k] = totalAvgCount;

            /* Averaging of Min/Max Arrays. */
            if (arrMinData[startIndex] < arrMinData[endIndex])
              arrMinData[k] = arrMinData[startIndex];
            else
              arrMinData[k] = arrMinData[endIndex];

            /* Calculating Max Data. */
            if (arrMaxData[startIndex] > arrMaxData[endIndex])
              arrMaxData[k] = arrMaxData[startIndex];
            else
              arrMaxData[k] = arrMaxData[endIndex];

            /* Averaging of Sum Square data. */
            arrSumSqrData[k] = (arrSumSqrData[startIndex] + arrSumSqrData[endIndex]);

            /* Time Stamp Array and Sequence Array are common for all Graphs. */
            if (graphCount == 0)
            {
              arrSeqNumber[k] = (arrSeqNumber[startIndex] + arrSeqNumber[endIndex]) / 2;
              arrTimeStamp[k] = (arrTimeStamp[startIndex] + arrTimeStamp[endIndex]) / 2;
            }

            k++;
          }
        }
        catch (Exception ex)
        {
          Log.stackTraceLog(className, "averageAllSamples", "", "", "Exception - ", ex);
        }
        graphCount++;
      }

      /* Need to update Derived Graph Data also. */
      derivedDataProcessor.averageAllDerivedDataSample();

      dataItemCount = dataItemCount / 2;
      avgCount = avgCount * 2;
      avgDoneFlag = true;

      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "averageAllSamples", "", "", "Averaging all samples for all graphs is completed. Current SeqNum = " + seqNum + ". Current AvgCount = " + avgCount + ", dataItemCount = " + dataItemCount);

      if (debugLogLevel > 3)
        Log.debugLogAlways(className, "averageAllSamples", "", "", logData(arrSeqNumber));
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "averageAllSamples", "", "", "Exception in doing averaging", e);
    }
  }

  /**
   * This will move the graph data values one position left.<br>
   * this function called in case of online mode.
   */
  public synchronized void mvDataValuesInArray()
  {
    try
    {
      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "mvDataValuesInArray", "", "", "Method Called. Current Seq Num = " + seqNum + ", dataItemCount = " + dataItemCount + ", Moving packet...");

      /* Iterating through Sequence and Time Stamp Arrays and Shifting of elements to one position Left. */
      for (int i = 0; i < dataItemCount - 1; i++)
      {
        arrSeqNumber[i] = arrSeqNumber[i + 1];
        arrTimeStamp[i] = arrTimeStamp[i + 1];
      }

      dataItemCount--;

      /* Iterating through each Graph DTO Arrays and Shifting of elements to one position Left. */
      for (Map.Entry<GraphUniqueKeyDTO, TimeBasedDTO> entry : hmTimeBasedDTO.entrySet())
      {
        GraphUniqueKeyDTO graphUniqueKeyDTO = entry.getKey();
        if (graphUniqueKeyDTO == null)
          continue;

        TimeBasedDTO timeBasedDTO = entry.getValue();
        timeBasedDTO.mvDataValuesInArray();
        hmTimeBasedDTO.put(graphUniqueKeyDTO, timeBasedDTO);
      }

      /* Sample Averaging Flag for showing sample data in gui. */
      avgDoneFlag = true;

      if (debugLogLevel > 3)
        Log.debugLogAlways(className, "mvDataValuesInArray", "", "", logData(arrSeqNumber));

      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "mvDataValuesInArray", "", "", "Method End. avgDoneFlag = " + avgDoneFlag + ", dataItemCount = " + dataItemCount);
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "mvDataValuesInArray", "", "", "Exception - ", ex);
    }
  }

  /**
   * Initializing average fields
   */
  private void initAvgFields()
  {
    avgSeqNum = 0.0;
    avgAbsTimeStamp = 0.0;
    avgCounter = 0;
    avgSampleCount = 0;
  }

  /**
   * Change for calculate time from server.<br>
   * Changed to use the testRun start date/time to calculate AbsoluteTime previously we are using System.currentTimeMillis() that cause problem when there are mismatch b/w server time and client time <br>
   * 
   * @param seqNum
   * @return
   */
  private long getCurTimeStamp(double avgSeqNum)
  {
    long time = 0l;
    try
    {
      time = testRunData.trStartTimeStamp + (int) (testRunData.interval * avgSeqNum);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getCurTimeStamp", "", "", "Exception : ", e);
      time = System.currentTimeMillis();
    }

    return time;
  }

  /**
   * Method is used to resize arrays.If granularity is applied then all averaging is turned off.<br>
   * So we need to increase the size of arrays, if arrays are full.
   */
  private void resizeGraphDataArrays()
  {
    try
    {
      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "resizeGraphDataArrays", "", "", "Resizing arrays. dataItemCount = " + dataItemCount);

      arrTimeStamp = timeBasedDataUtils.increaseSizeOfLongArray(arrTimeStamp, granularity_Arrays_Incremented_size);
      arrSeqNumber = timeBasedDataUtils.increaseSizeOfDoubleArray(arrSeqNumber, granularity_Arrays_Incremented_size);

      Iterator<Map.Entry<GraphUniqueKeyDTO, TimeBasedDTO>> entries = hmTimeBasedDTO.entrySet().iterator();
      while (entries.hasNext())
      {
        Map.Entry<GraphUniqueKeyDTO, TimeBasedDTO> entry = entries.next();
        GraphUniqueKeyDTO graphUniqueKeyDTO = entry.getKey();
        TimeBasedDTO timeBasedDTO = entry.getValue();
        timeBasedDTO.increase1DArraySize(granularity_Arrays_Incremented_size);
        hmTimeBasedDTO.put(graphUniqueKeyDTO, timeBasedDTO);
      }

      /* Increasing the size of Derived Data Arrays. */
      derivedDataProcessor.increaseDerivedArraySize(granularity_Arrays_Incremented_size);
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "resizeGraphDataArrays", "", "", "Exception - ", ex);
    }
  }

  /**
   * Method for putting Generating Averaged Sample into respective Graph DTO Sample Arrays.
   */
  private void genAvgData()
  {
    try
    {
      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "genAvgData", "", "", "Method Called. dataItemCount = " + dataItemCount + ", avgCount = " + avgCount + ", avgSampleCount = " + avgSampleCount + ", maxSampleInGraph = " + maxSampleInGraph);

      /* Iterating through each Graph DTO and update Averaged Sample Data. */
      for (Map.Entry<GraphUniqueKeyDTO, TimeBasedDTO> entry : hmTimeBasedDTO.entrySet())
      {
        /* Getting Graph DTO. */
        GraphUniqueKeyDTO graphUniqueKeyDTO = entry.getKey();

        try
        {
          if (graphUniqueKeyDTO == null)
          {
            Log.errorLog(className, "genAvgData", "", "", "graphUniqueKeyDTO is null.");
            continue;
          }

          /* Getting Graph Time Based DTO object. */
          TimeBasedDTO timeBasedDTO = entry.getValue();
          if (timeBasedDTO == null)
          {
            Log.errorLog(className, "genAvgData", "", "", "timeBasedDTO is null.");
            continue;
          }

          /* Update the averaged sample into Graph Time Based Data Array. */
          timeBasedDTO.updateAvgSampleData(dataItemCount, graphUniqueKeyDTO.getGraphDataIndex());
        }
        catch (Exception ex)
        {
          Log.errorLog(className, "genAvgData", "", "", "hmTimeBasedDTO = " + hmTimeBasedDTO);
          Log.stackTraceLog(className, "genAvgData", "", "", "graphUniqueKeyDTO = " + graphUniqueKeyDTO + ", Exception - ", ex);
        }
      }

      /* Generating Average Sequence Number and Time Stamp Value. */
      avgSeqNum = avgSeqNum / (double) avgSampleCount;
      avgAbsTimeStamp = avgAbsTimeStamp / (double) avgSampleCount;

      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "genAvgData", "", "", "avgSeqNum = " + avgSeqNum + ", avgCount = " + avgCount + ", dataItemCount = " + dataItemCount + " avgAbsTimeStamp = " + avgAbsTimeStamp);

      /* Update Averaged Values into respective arrays. */
      arrSeqNumber[dataItemCount] = avgSeqNum;

      if (testRun_Partition_Type > 0)
        arrTimeStamp[dataItemCount] = (long) avgAbsTimeStamp;
      else
        arrTimeStamp[dataItemCount] = getCurTimeStamp(avgSeqNum);

      /* Updating samples in Derived Graph. */
      derivedDataProcessor.updateDerivedGraphsData(dataItemCount);

      /* Must be incremented at this point as we have added data point */
      dataItemCount++;

      /* This Method worked when granularity is applied with Time Based Data. */
      if (granularity != -1 && dataItemCount > hmTimeBasedDTO.get(arrGraphUniqueKeyDTO[0]).getArrGraphSamplesData().length - 1)
        resizeGraphDataArrays();

      if (debugLogLevel > 3)
        Log.debugLogAlways(className, "genAvgData", "", "", logData(arrSeqNumber));

      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "genAvgData", "", "", "Method end. dataItemCount = " + dataItemCount);

      avgCountFlag = true;
      initAvgFields();
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "genAvgData", "", "", "Exception - ", ex);
    }
  }

  /**
   * Method is used to Invalidate Graph Data Index if Graph GDF is changed.
   */
  public synchronized void invalidateGraphDataIndex()
  {
    try
    {
      Log.debugLogAlways(className, "invalidateGraphDataIndex", "", "", "Method Called. Active Parition Name = " + activeParitionName);
      /* Iterating through each Graph DTO and update Averaged Sample Data. */
      for (Map.Entry<GraphUniqueKeyDTO, TimeBasedDTO> entry : hmTimeBasedDTO.entrySet())
      {
        /* Getting Graph DTO. */
        GraphUniqueKeyDTO graphUniqueKeyDTO = entry.getKey();

        if (graphUniqueKeyDTO != null)
        {
          /* Invalidate Graph Data Index if GDF is changed. */
          graphUniqueKeyDTO.setGraphDataIndex(-1);
        }
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "invalidateGraphDataIndex", "", "", "Exception - ", e);
    }
  }

  /**
   * Getting Active Graph DTO Array.
   * 
   * @return
   */
  public GraphUniqueKeyDTO[] getArrGraphUniqueKeyDTO()
  {
    return arrGraphUniqueKeyDTO;
  }

  /**
   * This method return active graph DTO array
   * 
   * @return
   */
  private GraphUniqueKeyDTO[] getActiveGraphUniqueKeyDTOArray()
  {
    if (testRunData != null && testRunData.isEnableAutoActivate)
    {
      /* auto activate is on, Getting all graph numbers from all profiles. */
      Log.debugLogAlways(className, "getActiveGraphUniqueKeyDTOArray", "", "", "enableAutoActivate keyword is on. So going to get all graph indexes from all profiles");
      this.arrGraphUniqueKeyDTO = TimeBasedDataUtils.getAllGraphUniqueKeyDTOFromProfile(graphNames);
    }
    else
    {
      Log.debugLogAlways(className, "getActiveGraphUniqueKeyDTOArray", "", "", "enableAutoActivate keyword is off. So going to get all graph indexes from test run.");

      /* auto activate is off, so Getting active graph numbers from GraphNames.java */
      this.arrGraphUniqueKeyDTO = graphNames.getGraphUniqueKeyDTO();
    }

    if (this.arrGraphUniqueKeyDTO == null)
    {
      Log.debugLogAlways(className, "getActiveGraphUniqueKeyDTOArray", "", "", "Bug: arrActiveGraphs = null. So getting all graphs...");
      this.arrGraphUniqueKeyDTO = graphNames.getGraphUniqueKeyDTO();
    }

    return this.arrGraphUniqueKeyDTO;
  }

  /**
   * This method is used to repeat last sample data , MinData , MaxData , CountData and SumSqrData in reqTBTRD.
   * 
   * @param repeatCount
   */
  public void repeatSamples(int repeatCount)
  {
    try
    {
      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "repeatSamples", "", "", "Method Called. repeatCount = " + repeatCount);

      for (int i = 0; i < arrGraphUniqueKeyDTO.length; i++)
      {
        GraphUniqueKeyDTO graphUniqueKeyDTO = arrGraphUniqueKeyDTO[i];
        if (graphUniqueKeyDTO == null)
          continue;
        TimeBasedDTO timeBasedDTO = hmTimeBasedDTO.get(graphUniqueKeyDTO);
        timeBasedDTO.repeatSamples(dataItemCount, repeatCount);
        hmTimeBasedDTO.put(graphUniqueKeyDTO, timeBasedDTO);
      }
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "repeatSamples", "", "", "Exception - ", ex);
    }
  }

  public boolean isAllSampleAveragingDone()
  {
    return avgDoneFlag;
  }

  public boolean isLastSampleAvailable()
  {
    return avgCountFlag;
  }

  public int getTotalActiveGraphNumber()
  {
    return activeGraphCount;
  }

  /**
   * This function return sequence number.
   * 
   * @param args
   */
  public long getSeqNumber()
  {
    return seqNum;
  }

  public long getEndTime()
  {
    long endTime = seqNum * testRunData.interval;
    return endTime;
  }

  /**
   * Get Start Time Based on current TimeBasedTestRunData type.
   * 
   * @return
   */
  public long getAveragedStartTime(long interval)
  {
    try
    {
      return (long) ((arrSeqNumber[0] * interval));
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getAveragedStartTime", "", "", "Exception - ", e);
      return 1;
    }
  }

  /**
   * Get End Time Based on current TimeBasedTestRunData type.
   * 
   * @return
   */
  public long getAveragedEndTime(long interval)
  {
    try
    {
      // when we open dash board then sometime we don't have data there then dataItemCount will be zero
      // In this case if user click or drag on graph panel then it calls this method
      // then it will try to get the value from arrSeqNumber for index -1 which is invalid
      // So we added a check that if dataItemCount is < 1 then return 0;
      if (dataItemCount < 1)
        return 0;
      else
        return (long) ((arrSeqNumber[dataItemCount - 1] * interval));
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getAveragedEndTime", "", "", "Exception - ", e);
      return 0;
    }
  }

  public int getDataItemCount()
  {
    return dataItemCount;
  }

  public void setDebugLevel(int debugLogLevel)
  {
    this.debugLogLevel = debugLogLevel;

    if (derivedDataProcessor != null)
      derivedDataProcessor.setDebugLevel(debugLogLevel);
  }

  public int getAvgCounter()
  {
    return avgCounter;
  }

  public int getAvgCount()
  {
    return avgCount;
  }

  public int getAvgSampleCount()
  {
    return avgSampleCount;
  }

  public void setAvgCount(int avgCount)
  {
    this.avgCount = avgCount;
  }

  public int getActiveGraphNum()
  {
    return activeGraphCount;
  }

  public void increamentActiveGraphNum()
  {
    activeGraphCount += 1;
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

  public double getLastSample(String groupIdGraphVectorName)
  {
    try
    {
      // when we open dash board then sometime we don't have data there then dataItemCount will be zero
      // In this case if user click or drag on graph panel then it calles this method
      // then it will try to get the value from arrGraphSamplesData for index -1 which is invalid
      // So we added a check that if dataItemCount is < 1 then return 0;

      if (dataItemCount < 1)
        return 0;
      else
        return hmTimeBasedDTO.get(groupIdGraphVectorName).getArrGraphSamplesData()[dataItemCount - 1];
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getLastSample", "", "", "Exception - ", e);
      return 0;
    }
  }

  /**
   * Returning Last Sample from MinData Array.
   * 
   * @param activeGraphNum
   * @return
   */
  public double getLastMinSampleData(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    try
    {
      if (dataItemCount < 1)
        return 0;
      else
        return hmTimeBasedDTO.get(graphUniqueKeyDTO).getArrMinData()[dataItemCount - 1];
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getLastMinSampleData", "", "", "Exception - ", e);
      return 0;
    }
  }

  /**
   * Returning Last Sample from MaxData Array.
   * 
   * @param activeGraphNum
   * @return
   */
  public double getLastMaxSampleData(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    try
    {
      if (dataItemCount < 1)
        return 0;
      else
        return hmTimeBasedDTO.get(graphUniqueKeyDTO).getArrMaxData()[dataItemCount - 1];
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getLastMaxSampleData", "", "", "Exception - ", e);
      return 0;
    }
  }

  /**
   * Returning Last Sample from CountData Array.
   * 
   * @param activeGraphNum
   * @return
   */
  public int getLastCountData(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    try
    {
      if (dataItemCount < 1)
        return 0;
      else
        return hmTimeBasedDTO.get(graphUniqueKeyDTO).getArrCountData()[dataItemCount - 1];
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getLastCountData", "", "", "Exception - ", e);
      return 0;
    }
  }

  /**
   * Returning Last Sample from SumSqrData Array.
   * 
   * @param activeGraphNum
   * @return
   */
  public double getLastSumSqrData(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    try
    {
      if (dataItemCount < 1)
        return 0;
      else
        return hmTimeBasedDTO.get(graphUniqueKeyDTO).getArrSumSqrData()[dataItemCount - 1];
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getLastSumSqrData", "", "", "Exception - ", e);
      return 0;
    }
  }

  /**
   * Getting variable, telling the starting Sequence number for moving data in arrays while generating/updating Last N Minute Data.
   * 
   * @return
   */
  public int getMovingStartFromSeq()
  {
    return movingStartFromSeq;
  }

  /**
   * Setting variable, telling the starting Sequence number for moving data in arrays while generating/updating Generating Last N Minute Data.
   * 
   * @param movingStartFromSeq
   */
  public void setMovingStartFromSeq(int movingStartFromSeq)
  {
    this.movingStartFromSeq = movingStartFromSeq;
  }

  public double getLastAvgSample(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    try
    {
      return hmTimeBasedDTO.get(graphUniqueKeyDTO).getLastSampleData();
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getLastAvgSample", "", "", "Exception - ", e);
      return 0;
    }
  }

  /**
   * Get Moving Point of Last N for NDE Continuous Mode.
   * 
   * @return
   */
  public double getMovingTimeStampInLastViewMode()
  {
    return movingTimeStampInLastViewMode;
  }

  /**
   * Set Moving Point of Last N for NDE Continuous Mode.
   * 
   * @param movingTimeStampInLastViewMode
   */
  public void setMovingTimeStampInLastViewMode(double movingTimeStampInLastViewMode)
  {
    this.movingTimeStampInLastViewMode = movingTimeStampInLastViewMode;
  }

  /**
   * Gets the flag for NDE continuous Monitoring.
   * 
   * @return
   */
  public boolean isNDE_Continuous_Mode()
  {
    return isNDE_Continuous_Mode;
  }

  /**
   * Sets the flag for NDE continuous Monitoring.
   * 
   * @param isNDE_Continuous_Mode
   */
  public void setNDE_Continuous_Mode(boolean isNDE_Continuous_Mode)
  {
    this.isNDE_Continuous_Mode = isNDE_Continuous_Mode;
  }

  /**
   * Getting Object of Derived Data Processor.
   * 
   * @return
   */
  public DerivedDataProcessor getDerivedDataProcessor()
  {
    return derivedDataProcessor;
  }

  /**
   * Setting object of Derived Data Processor.
   * 
   * @param derivedDataProcessor
   */
  public void setDerivedDataProcessor(DerivedDataProcessor derivedDataProcessor)
  {
    this.derivedDataProcessor = derivedDataProcessor;
  }

  /**
   * Gets the maximum samples used to draw the graph.
   * 
   * @return
   */
  public int getMaxSampleInGraph()
  {
    return maxSampleInGraph;
  }

  /**
   * This will add list of passed graph numbers in the active graph list and update related data structures: arrGraphNumberToSampleDataArray
   * 
   * @param reqActiveGraphs
   */
  public void activateGraphs(GraphUniqueKeyDTO[] reqActiveGraphs)
  {
    /* Merging Graph Numbers of requested TBTRD. */
    this.arrGraphUniqueKeyDTO = TimeBasedDataUtils.appendGraphUniqueKeyDTOArray(arrGraphUniqueKeyDTO, reqActiveGraphs);
    this.activeGraphCount = this.arrGraphUniqueKeyDTO.length;
  }

  /**
   * This method is used to get GraphStats For Derived Graph by Derived Graph Number.
   * 
   * @param derivedGraphNumber
   * @return
   */
  public synchronized GraphStats getGraphStatsForDerivedGraphNumber(int derivedGraphNumber)
  {
    try
    {
      Log.debugLogAlways(className, "getGraphStatsForDerivedGraphNumber", "", "", "derivedGraphNumber = " + derivedGraphNumber);

      if (derivedDataProcessor == null)
      {
        Log.debugLogAlways(className, "getGraphStatsForDerivedGraphNumber", "", "", "Derived Data in Not Available in current Time Based Data.");
        return null;
      }

      /* Getting Graph Statistics For Derived Graph. */
      derivedDataProcessor.getDerivedGraphStats(derivedGraphNumber, graphStatsObj);
      return graphStatsObj;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getGraphStatsForDerivedGraphNumber", "", "", "Exception - ", e);
      return null;
    }
  }

  /**
   * Getting Graph Statistics by Graph Number.
   * 
   * @param graphNumber
   * @return
   */
  public synchronized GraphStats getGraphStatByGraphUniqueKeyDTO(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    try
    {
      if (debugLogLevel > 1)
        Log.debugLogAlways(className, "getGraphStatByGraph", "", "", "Method Called. graphUniqueKeyDTO = " + graphUniqueKeyDTO.toString());

      boolean isGraphExistInMemory = isGraphExistInMemory(graphUniqueKeyDTO);
      if (isGraphExistInMemory)
      {
        TimeBasedDTO timeBasedDTO = hmTimeBasedDTO.get(graphUniqueKeyDTO);

        /* Calculating Min Data for Graph Statistics */
        double[] arrMinData = timeBasedDTO.getArrMinData();
        double lastMinData = timeBasedDTO.getLastMinData();
        double minData = timeBasedDataUtils.getMinValueFromArray(arrMinData, dataItemCount);

        if ((!TimeBasedDataUtils.isEmptySample(lastMinData)) && (minData > lastMinData))
          minData = lastMinData;

        /* Calculating Max Data for Graph Statistics */
        double[] arrMaxData = timeBasedDTO.getArrMaxData();
        double lastMaxData = timeBasedDTO.getLastMaxData();
        double maxData = timeBasedDataUtils.getMaxValueFromArray(arrMaxData, dataItemCount);
        if (maxData < lastMaxData)
          maxData = lastMaxData;

        int[] arrCountData = timeBasedDTO.getArrCountData();
        int totalCount = timeBasedDataUtils.getSumOfArrayElements(arrCountData, dataItemCount);
        int lastCountData = timeBasedDTO.getLastCountData();
        totalCount += lastCountData;

        double[] arrGraphSamplesData = timeBasedDTO.getArrGraphSamplesData();
        double sum = timeBasedDataUtils.getAvgSum(arrGraphSamplesData, arrCountData, dataItemCount);
        double lastAvgSampleData = timeBasedDTO.getLastAvgSampleData();
        sum += lastAvgSampleData;
        double average = sum / totalCount;

        double[] arrSumSqrData = timeBasedDTO.getArrSumSqrData();
        double lastSumSqrData = timeBasedDTO.getLastSumSqrData();
        double totalSumSqr = timeBasedDataUtils.getSumOfDoubleArrayElements(arrSumSqrData, dataItemCount);
        totalSumSqr += lastSumSqrData;

        int graphDataType = graphNames.getDataTypeNumByGraphUniqueKeyDTO(graphUniqueKeyDTO);
        if (graphDataType == GraphNameUtils.DATA_TYPE_TIMES_STD)
          totalSumSqr = totalSumSqr / 1000;

        double stdDev = 0.0;

        if (totalCount > 1) /* StdDev calculated only if total Sample is more than 1. */
          stdDev = Math.sqrt((totalSumSqr - (sum * average)) / totalCount - 1);

        double lastSampleData = timeBasedDTO.getLastSampleData();

        /* Checking for Empty Sample. */
        if (TimeBasedDataUtils.isEmptySample(lastSampleData))
          lastSampleData = 0.0;

        /* Checking for min data. */
        if (TimeBasedDataUtils.isEmptySample(minData))
          minData = 0.0;

        graphStatsObj.setMinData(minData);
        graphStatsObj.setMaxData(maxData);
        graphStatsObj.setCount(totalCount);
        graphStatsObj.setAvgData(average);
        graphStatsObj.setGraphDataType(graphDataType);
        graphStatsObj.setStdDev(stdDev);
        graphStatsObj.setLastSample(lastSampleData);
      }
      else
      {
        /* if graph Number is not available then reset object before returning it. */
        graphStatsObj.setMaxData(0.0);
        graphStatsObj.setMinData(0.0);
        graphStatsObj.setAvgData(0.0);
        graphStatsObj.setCount(0);
        graphStatsObj.setGraphDataType(-1);
        graphStatsObj.setLastSample(0.0);
        graphStatsObj.setStdDev(0.0);
      }

      return graphStatsObj;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getGraphStatByGraph", "", "", "Exception - ", e);
      return null;
    }
  }

  /**
   * Logging methods, Method for printing the data in time based test run data.
   */
  public void logInfo()
  {
    try
    {
      System.out.println("Test run data type = " + testRunDataType);
      System.out.println("Data View Type = " + timeBasedTestRunDataType);
      if (testRunData != null && graphNames != null)
        System.out.println("Total graphs = " + graphNames.getTotalNumOfGraphs());

      System.out.println("Session start sequence number = " + sessionStartSeqNum);
      System.out.println("Previous OPCODE = " + prevOpcode);
      System.out.println("Total packet lost = " + totalPktLost);
      System.out.println("total active graphs = " + this.arrGraphUniqueKeyDTO.length);
      if (testRunData != null)
        System.out.println("Auto Activate Keyword = " + testRunData.isEnableAutoActivate);
      else
        System.out.println("testRunData is null. So do not know about isEnableAutoActivate");

      System.out.println("Maximum samples in graphs =  " + maxSampleInGraph);
      System.out.println("Total samples for last = " + totalSampleForLast);
      if (movingStartFromSeq != Integer.MAX_VALUE)
        System.out.println("Moving start from sequence = " + movingStartFromSeq);
      else
        System.out.println("Moving start from sequence = NA");
      System.out.println("Average counter  = " + avgCounter);
      System.out.println("Average sequence number = " + avgSeqNum);
      System.out.println("Data Item Count = " + dataItemCount);
      System.out.println("Avg Count = " + avgCount);
      System.out.println("Last Sequence Number = " + seqNum);
      System.out.println("Flag For Series Update At Client Side = " + avgCountFlag);
      System.out.println("Flag For Series Refresh At Client Side = " + avgDoneFlag);
      System.out.print("Time Stamp array = ");
      if (arrTimeStamp != null)
      {
        for (int i = 0; i < arrTimeStamp.length; i++)
        {
          System.out.print(arrTimeStamp[i] + ",");
        }
      }
      System.out.println();
      System.out.print("Sequence Number Array = ");
      if (arrSeqNumber != null)
      {
        for (int i = 0; i < dataItemCount; i++)
        {
          System.out.print(arrSeqNumber[i] + ",");
        }
      }

      System.out.println();

      // Keeping Graph Numbers - Log at the end as output on console gets truncated after 10K
      System.out.print("Active Graphs = ");
      for (int i = 0; i < this.arrGraphUniqueKeyDTO.length; i++)
      {
        if (arrGraphUniqueKeyDTO[i] == null)
          continue;

        System.out.print(this.arrGraphUniqueKeyDTO[i].toString() + ",");
      }
      System.out.println();
      System.out.println("----------------------------------------------------------");
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "logInfo", "", "", "Exception - ", ex);
    }
  }

  /**
   * This method print Graph data of active graphs.
   */
  public void logGraphSamples()
  {
    for (int i = 0; i < this.arrGraphUniqueKeyDTO.length; i++)
    {
      GraphUniqueKeyDTO graphUniqueKeyDTO = arrGraphUniqueKeyDTO[i];
      double[] arrGraphSamplesData = hmTimeBasedDTO.get(graphUniqueKeyDTO).getArrGraphSamplesData();
      System.out.println("Graph = " + graphUniqueKeyDTO + ", Graph Data = " + TimeBasedDataUtils.convertDoubleArrayToString(arrGraphSamplesData, ","));
    }
  }

  // This method print last sample of active graphs.
  public void logLastAvgGraphSamples()
  {
    try
    {
      for (int i = 0; i < this.arrGraphUniqueKeyDTO.length; i++)
      {
        GraphUniqueKeyDTO graphUniqueKeyDTO = arrGraphUniqueKeyDTO[i];
        if (graphUniqueKeyDTO == null)
          continue;

        double lastAvgSampleData = hmTimeBasedDTO.get(graphUniqueKeyDTO).getLastAvgSampleData();
        System.out.println("Graph = " + graphUniqueKeyDTO + ", Last Avg Graph Data = " + lastAvgSampleData);
      }
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "logLastAvgGraphSamples", "", "", "Exception - ", ex);
    }
  }

  private String logData(double[] arrSeq)
  {
    String msg = "TimeBasedTestRunDataType = " + timeBasedTestRunDataType + "|Current SeqNum = " + seqNum + "| DataItemCount = " + dataItemCount + "| avgCount = " + avgCount + "| avgCounter = " + avgCounter + "| movingStartFromSeq = " + movingStartFromSeq + "| totalSampleForLast = " + totalSampleForLast + "| maxSampleInGraph = " + maxSampleInGraph + "| avgCountFlag = " + avgCountFlag + "| avgDoneFlag = " + avgDoneFlag;
    if (dataItemCount == 0)
    {
      msg += "|No Sample Data Available, So Return.";
      return msg;
    }

    String s = msg + "|Seq Number Array = ";
    for (int i = 0; i < dataItemCount; i++)
    {
      s += "," + arrSeq[i];
    }

    s += "|Graph Data Array for Graph " + arrGraphUniqueKeyDTO[0].toString() + " = ";
    double[] arrGraphSamplesData = hmTimeBasedDTO.get(arrGraphUniqueKeyDTO[0]).getArrGraphSamplesData();
    s += ", " + TimeBasedDataUtils.convertDoubleArrayToString(arrGraphSamplesData, ",");

    return s;
  }

  /**
   * Use this method to get start time stamp in absolute format.
   * 
   * @return
   */
  public long getAbsStartTimeStamp()
  {
    try
    {
      return arrTimeStamp[0];
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getAbsStartTimeStamp", "", "", "Exception - ", e);
      return 0;
    }
  }

  /**
   * Use this method to get end time stamp in absolute format.
   * 
   * @return
   */
  public long getAbsEndTimeStamp()
  {
    try
    {
      // when we open dash board then sometime we don't have data there then dataItemCount will be zero
      // In this case if user click or drag on graph panel then it calles this method
      // then it will try to get the value from arrSeqNumber for index -1 which is invalid
      // So we added a check that if dataItemCount is < 1 then return 0;
      if (dataItemCount < 1)
        return 0;

      return arrTimeStamp[dataItemCount - 1];
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getAbsEndTimeStamp", "", "", "Exception - ", e);
      return 0;
    }
  }

  public void resetDataAvailableFlag()
  {
    avgCountFlag = false;
    avgDoneFlag = false;
  }

  public TimeBasedDTO getTimeBasedDTO(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    return hmTimeBasedDTO.get(graphUniqueKeyDTO);
  }

  public void setTimeBasedDTO(GraphUniqueKeyDTO graphUniqueKeyDTO, TimeBasedDTO timeBasedDTO)
  {
    hmTimeBasedDTO.put(graphUniqueKeyDTO, timeBasedDTO);
  }

  public data getMsgData()
  {
    return this.msgData;
  }

  public void setMsgData(data msgData)
  {
    this.msgData = msgData;
  }

  public String getGeneratorName()
  {
    return this.generatorName;
  }

  public TestRunDataType getTestRunDataTypeObj()
  {
    return this.testRunDataTypeObj;
  }

  public long getPrevSeqNum()
  {
    return this.prevSeqNum;
  }

  public int getPrevOpCode()
  {
    return this.prevOpcode;
  }

  public long[] getTimeStampArray()
  {
    return arrTimeStamp;
  }

  public double[] getSeqNumberArray()
  {
    return arrSeqNumber;
  }

  /**
   * Sets the Active Partition Name.
   * 
   * @return
   */
  public String getActiveParitionName()
  {
    return activeParitionName;
  }

  /**
   * Returns the Active Partition Name.
   * 
   * @param activeParitionName
   */
  public void setActiveParitionName(String activeParitionName)
  {
    this.activeParitionName = activeParitionName;
  }

  /**
   * Return Absolute Time Stamp.
   * 
   * @return
   */
  public double getAbsTimeStamp()
  {
    return absTimeStamp;
  }

  /**
   * Method is used to get the HashMap of Time Based DTO.
   * 
   * @return
   */
  public HashMap<GraphUniqueKeyDTO, TimeBasedDTO> getHmTimeBasedDTO()
  {
    return hmTimeBasedDTO;
  }
  
  /**
   * Setting HashMap of Graph DTO. 
   * @param hmTimeBasedDTO
   */
  public void setHmTimeBasedDTO(HashMap<GraphUniqueKeyDTO, TimeBasedDTO> hmTimeBasedDTO)
  {
    this.hmTimeBasedDTO = hmTimeBasedDTO;
  }
  
  /**
   * Method is used to set active Graph DTO List.
   * @param arrGraphUniqueKeyDTO
   */
  public void setArrGraphUniqueKeyDTO(GraphUniqueKeyDTO[] arrGraphUniqueKeyDTO)
  {
    this.arrGraphUniqueKeyDTO = arrGraphUniqueKeyDTO;
  }

  public static void main(String[] args)
  {
  }
}

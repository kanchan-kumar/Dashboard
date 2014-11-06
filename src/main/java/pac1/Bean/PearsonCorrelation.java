/*--------------------------------------------------------------------
  @Name    : PearsonCorrelation.java
  @Author  : Prabhat Vashist
  @Purpose : This is class to calculate pearson-correlation of graphs, for refrence check http://algorithmsanalyzed.blogspot.com/2008/07/bellkor-algorithm-pearson-correlation.html

  @Modification History:
    08/26/11:Prabhat Vashist - Initial Version
----------------------------------------------------------------------*/

package pac1.Bean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Vector;

import pac1.Bean.GraphName.GraphNames;

public class PearsonCorrelation
{
  private final String className = "PearsonCorrelation";

  private final int ERROR_CODE = -10;
  private final double DEFAULT_PEARSON_FACTOR = 0.8;
  private final int RELATED_GRAPH_FIELD_INDEX = 6;
  private final int GRAPH_DETAIL_INDEX = 8;
  private final int VALUE_FIELD_INDEX = 2;

  private final int SKIP_CUM = 1;
  private final int SKIP_TIMESANDTIMESSTD = 2;
  private final int SKIP_CUMTIMESANDTIMESSTD = 3;

  private static final int DATA_TYPE_CUMULATIVE = 2;
  private static final int DATA_TYPE_TIMES = 3;

  private int testRunNum = -1;
  private int baselineGraphIndex = -1;
  private int[] otherGraphIndex = null;
  private String graphDetails = "";
  private String startTime = "NA";
  private String endTime = "NA";
  private boolean calcAllIndex = false;
  private boolean skipCumGraph = false;
  private boolean skipTimesAndTimesStd = false;
  private boolean isIncludeAntiFlag = false;
  double correlatedValuePct = 0;

  private double arrGraphData[][] = null;

  private double arrBaselineGraphData[] = null;
  public double sumOfBaselineGraphData = -1;
  public double sumOfSquareOfBaselineGraphData = -1;
  
  /*Keeping DTO Array.*/
  private GraphUniqueKeyDTO []arrGraphUniqueKeyDTO = null;
  
  /*Keeping DTO for baseline and Normal Graphs.*/
  private GraphUniqueKeyDTO graphUniqueKeyDTOList[] = null;
  
  /*Graph DTO of Baseline Graph.*/
  GraphUniqueKeyDTO baselineGraphUniqueKeyDTO = null;
  
  /*Time Based Data object.*/
  private TimeBasedTestRunData timeBasedTestRunDataObj = null;
  
  /*Graph Names object.*/
  private GraphNames graphNames = null;
  
  /*Object of CorrelationGraphDTO.*/
  private CorrelationRequestDTO correlationRequestDTO = null;
  
  /*Partition Info Utils Object.*/
  private PartitionInfoUtils partitionInfoUtilsObj = new PartitionInfoUtils();
  
  /*Report Data Object.*/
  ReportData rptData = null;
    
  public double arrPearsonValue[] = null;
  private boolean isAliveAll = false;
  private int correlationMaxThreads = 8;
  private int correlationNumGraphsPerThread = 40;

  // this is to generate csv file
  /*
   * public ArrayList arrListGraphNames = new ArrayList(); public ArrayList arrListSumOfGraphData = new ArrayList(); public ArrayList arrListSumOfSquareOfGraphData = new ArrayList(); public ArrayList
   * arrListSumOfCartesianProduct = new ArrayList(); public ArrayList arrListPearsonValue = new ArrayList();
   */

  public Vector arrListGraphNames = new Vector();
  public Vector arrListSumOfGraphData = new Vector();
  public Vector arrListSumOfSquareOfGraphData = new Vector();
  public Vector arrListSumOfCartesianProduct = new Vector();
  public Vector arrListPearsonValue = new Vector();

  private EventGeneratorAdvisory eventGeneratorAdvisoryObj = null;
  
  /**
   * Constructor Initialize the Correlation Graph DTO Object.
   * @param correlationRequestDTO
   */
  public PearsonCorrelation(CorrelationRequestDTO correlationRequestDTO, EventGeneratorAdvisory eventGeneratorAdvisoryObj)
  {
    try
    {
      Log.debugLogAlways(className, "PearsonCorrelation:Contructor", "", "", "Method Called.");
      
      this.testRunNum = correlationRequestDTO.getTestRun();
      this.graphDetails = correlationRequestDTO.getCorrRequestedGraphDetail();
      this.isIncludeAntiFlag = correlationRequestDTO.isIncludeAntiFlag();
      if (isIncludeAntiFlag)
        this.correlatedValuePct = Math.abs(correlationRequestDTO.getCorrThreshold());
      else
        this.correlatedValuePct = correlationRequestDTO.getCorrThreshold();
      
      this.startTime = correlationRequestDTO.getStartTime();
      this.endTime = correlationRequestDTO.getEndTime();
      this.calcAllIndex = correlationRequestDTO.isCalcAllIndex();
      this.skipCumGraph = correlationRequestDTO.isSkipCumGraph();
      this.skipTimesAndTimesStd = correlationRequestDTO.isSkipTimesAndTimesStd();
      this.arrGraphUniqueKeyDTO = correlationRequestDTO.getArrGraphUniqueKeyDTO();
      this.baselineGraphUniqueKeyDTO = correlationRequestDTO.getBaselineGraphUniqueKeyDTO();
      this.correlationRequestDTO = correlationRequestDTO;
      this.eventGeneratorAdvisoryObj = eventGeneratorAdvisoryObj;
      
      rptData = new ReportData(testRunNum);
      graphNames = rptData.createGraphNamesObj();
      
      Log.debugLogAlways(className, "PearsonCorrelation:Contructor", "", "", "Initialization with testRunNum = " + testRunNum + ", isIncludeAntiFlag = " + isIncludeAntiFlag + ", correlatedValuePct = " + correlatedValuePct + ", startTime = " + startTime + ", endTime = " + endTime + ", calcAllIndex = " + calcAllIndex + ", skipCumGraph = " + skipCumGraph + ", skipTimesAndTimesStd = " + skipTimesAndTimesStd + ", baselineGraphUniqueKeyDTO = " + baselineGraphUniqueKeyDTO + ", graphDetails = " + graphDetails);
      
      if(arrGraphUniqueKeyDTO != null)
      {
	for(int i = 0; i < arrGraphUniqueKeyDTO.length; i++)
	  Log.debugLogAlways(className, "PearsonCorrelation:Contructor", "", "", "Input Graph DTO = " + arrGraphUniqueKeyDTO[i]);
      }
      
      setThreadValueFromConfig();
      
    }
    catch(Exception e)
    {
      Log.errorLog(className, "PearsonCorrelation:Constructor", "", "", "Error in Initializing Contructor");
    }
    
    Log.debugLogAlways(className, "PearsonCorrelation:Contructor", "", "", "Initialization done.");
  }
  

  public PearsonCorrelation(int testRunNum, int baselineGraphIndex, int[] otherGraphIndex, String graphDetails, EventGeneratorAdvisory eventGeneratorAdvisoryObj, double correlatedValuePct, boolean isIncludeAntiFlag, String startTime, String endTime, boolean calcAllIndex, boolean skipCumGraph, boolean skipTimesAndTimesStd)
  {
    Log.debugLogAlways(className, "PearsonCorrelation", "", "", "Method called, testRunNum = " + testRunNum + ", graphDetails = " + graphDetails + ", correlatedValuePct = " + correlatedValuePct + ", startTime = " + startTime + ", endTime = " + endTime + ", calcAllIndex = " + calcAllIndex + ", baselineGraphIndex = " + baselineGraphIndex + ", skipCumGraph = " + skipCumGraph + ", skipTimesAndTimesStd = " + skipTimesAndTimesStd);

    this.testRunNum = testRunNum;
    this.baselineGraphIndex = baselineGraphIndex;
    this.otherGraphIndex = otherGraphIndex;
    this.graphDetails = graphDetails;
    this.eventGeneratorAdvisoryObj = eventGeneratorAdvisoryObj;
    this.isIncludeAntiFlag = isIncludeAntiFlag;
    if (isIncludeAntiFlag)
      this.correlatedValuePct = Math.abs(correlatedValuePct);
    else
      this.correlatedValuePct = correlatedValuePct;
    this.startTime = startTime;
    this.endTime = endTime;
    this.calcAllIndex = calcAllIndex;
    this.skipCumGraph = skipCumGraph;
    this.skipTimesAndTimesStd = skipTimesAndTimesStd;
    
    rptData = new ReportData(testRunNum);
    graphNames = rptData.createGraphNamesObj();

    setThreadValueFromConfig();
  }

  public void setThreadValueFromConfig()
  {
    Config config = new Config();
    String maxThreadsFromConfig = Config.getValue("CorrelationMaximumThreads");
    if (!maxThreadsFromConfig.trim().equals(""))
      correlationMaxThreads = Integer.parseInt(maxThreadsFromConfig);

    String numGraphsPerThreadConfig = Config.getValue("NumberOfGraphsPerThread");
    if (!numGraphsPerThreadConfig.trim().equals(""))
      correlationNumGraphsPerThread = Integer.parseInt(numGraphsPerThreadConfig);
    
    Log.debugLogAlways(className, "setThreadValueFromConfig", "", "", "Getting Configuration values. correlationMaxThreads = " + correlationMaxThreads + ", correlationNumGraphsPerThread = " + correlationNumGraphsPerThread);
  }

  // this function return pearson array value
  public double[] getArrPearsonValue()
  {
    return arrPearsonValue;
  }

  // this is to convert correlation Pct Value in correlation factor
  private double calcCorrelationFactorByPctValue()
  {
    Log.debugLogAlways(className, "calcCorrelationFactorByPctValue", "", "", "Method called. correlatedValuePct = " + correlatedValuePct);

    try
    {
      return (correlatedValuePct / 100);

    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "calcCorrelationFactorByPctValue", "", "", "Exception - ", e);
      return -1;
    }
  }

  public synchronized void stopThread(Thread[] arrthread)
  {
    try
    {
      Log.debugLog(className, "stopThread", "", "", "Method called." + arrthread.length);
      do // infinite loop
      {
        // notifyAll();
        int counter = arrthread.length;
        // checking each thread is alive or not
        // If not, stopping thread and decrease counter
        // then break from the loop
        for (int ii = 0; ii < arrthread.length; ii++)
        {
          if (!arrthread[ii].isAlive())
          {
            counter--;
            Log.debugLog(className, "stopThread", "", "", "Stopping thread. Thread name = " + arrthread[ii].getName());
            arrthread[ii].stop();
          }
        }
        if (counter == 0)
        {
          isAliveAll = false;
          break;
        }
      }
      while (true);
    }
    catch (Exception e)
    {
      Log.errorLog(className, "stopThread", "", "", "Exception - " + e);
    }
  }
  
  /**
   * Return the Array of Graph DTO.
   * @return
   */
  public GraphUniqueKeyDTO[] getArrGraphUniqueKeyDTO() 
  {
    return arrGraphUniqueKeyDTO;
  }

  /**
   * Setting the Array of Graph DTO.
   * @param arrGraphUniqueKeyDTO
   */
  public void setArrGraphUniqueKeyDTO(GraphUniqueKeyDTO[] arrGraphUniqueKeyDTO) 
  {
    this.arrGraphUniqueKeyDTO = arrGraphUniqueKeyDTO;
  }
  
  /**
   * Method used to get Baseline Graph DTO.
   * @return
   */
  public GraphUniqueKeyDTO getBaselineGraphUniqueKeyDTO() {
    return baselineGraphUniqueKeyDTO;
  }

  /**
   * Method used to set Baseline Graph DTO.
   * @param baselineGraphUniqueKeyDTO
   */
  public void setBaselineGraphUniqueKeyDTO(
      GraphUniqueKeyDTO baselineGraphUniqueKeyDTO) {
    this.baselineGraphUniqueKeyDTO = baselineGraphUniqueKeyDTO;
  }

  /**
   * This is to generate sum of graph data and sum square of graph data
   * @param baselineGraphDataIndex
   * @return
   */
  private boolean generateSumOfGraphDataAndSumSquareOfGraphData(int baselineGraphDataIndex)
  {
    try
    {
      Log.debugLog(className, "generateSumOfGraphDataAndSumSquareOfGraphData", "", "", "Method called. baselineGraphDataIndex = " + baselineGraphDataIndex + ", Baseline Graph DTO = " + baselineGraphUniqueKeyDTO);
      double sumOfGraphData = 0;
      double sumOfSquareOfGraphData = 0;

      GraphUniqueKeyDTO graphUniqueKeyDTO = graphUniqueKeyDTOList[0];

      double[] arrRptData = timeBasedTestRunDataObj.getTimeBasedDTO(graphUniqueKeyDTO).getArrGraphSamplesData();
      arrBaselineGraphData = arrRptData;

      String tempGraphNames = graphNames.getGraphNameByGraphUniqueKeyDTO(graphUniqueKeyDTO, true);

      // this is to generate csv file if Debug level value is greater than equals to 4 than create logs
      if ((Integer.parseInt(Config.getValue("debuglevel"))) >= 4)
      {
        Log.debugLog(className, "generateSumOfGraphDataAndSumSquareOfGraphData", "", "", "Baseline graph name tempGraphNames = " + tempGraphNames);
        arrListGraphNames.set(1, tempGraphNames);
      }

      for(int i = 0; i < timeBasedTestRunDataObj.getDataItemCount(); i++)
      {
        sumOfGraphData = sumOfGraphData + arrRptData[i];
        sumOfSquareOfGraphData = sumOfSquareOfGraphData + arrRptData[i] * arrRptData[i];
      }

      /*if saveAsBaseline is true then save this as baseline.*/
      sumOfBaselineGraphData = sumOfGraphData;
      sumOfSquareOfBaselineGraphData = sumOfSquareOfGraphData;

      Log.debugLogAlways(className, "generateSumOfGraphDataAndSumSquareOfGraphData", "", "", "Sum of graph data = " + sumOfGraphData + ", sum of square of graph data = " + sumOfSquareOfGraphData + " for graph = " + tempGraphNames);
      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "generateSumOfGraphDataAndSumSquareOfGraphData", "", "", "Exception - ", e);
      return false;
    }
  }

  /**
   * 
   * @param totalGraph
   * @return Cases : - Case1 - CorrelationMaximumThreads = 0 NumberOfGraphsPerThread = 0
   * 
   *         Total Graph 6 correlationNumGraphsPerThread = 0 correlationMaxThreads = 0 startIndex 0, endIndex = 6, remainder = 0 createThread without thread 0 case2 - CorrelationMaximumThreads = 2
   *         NumberOfGraphsPerThread = 40
   * 
   *         Total Graph 65 correlationNumGraphsPerThread = 40 correlationMaxThreads = 2 0 startIndex 0, endIndex = 32, remainder = 1 createThread With thread 2 1 startIndex 32, endIndex = 65,
   *         remainder = 1 createThread With thread 2
   */
  // THis method calculate no of thread to start
  private int calculateThreadForStart(int totalGraph)
  {
    try
    {
      Log.debugLogAlways(className, "calculateThreadForStart", "", "", "Method called. " + "totalGraph " + totalGraph + ", correlationNumGraphsPerThread = " + correlationNumGraphsPerThread + ", correlationMaxThreads = " + correlationMaxThreads);

      int count = totalGraph / correlationNumGraphsPerThread;
      int remainder = totalGraph % correlationNumGraphsPerThread;

      // If correlationMaxThreads = 0 in config.ini means no thread will start
      if (correlationMaxThreads == 0)
        return 0;

      if (remainder > 0)
        count++;

      if (correlationMaxThreads > 0 && correlationMaxThreads < count)
      {
        return correlationMaxThreads;
      }

      return count;
    }
    catch (Exception e)
    {
      return 0;
      // TODO: handle exception
    }
  }

  /**
   * This is main function to generate PearsonCorrelation.
   * @param arrGraphIndex
   * @param skipValue
   * @return
   */
  public void getFilteredGraphDTOList(int skipValue)
  {
    try
    {
      Log.debugLogAlways(className, "getFilteredGraphDTOList", "", "", "Filtering Graph. skipValue = " + skipValue);
       
      ArrayList<GraphUniqueKeyDTO> arrFilteredGraphDTOList = new ArrayList<GraphUniqueKeyDTO>();
            
      GraphUniqueKeyDTO arrGraphUniqueKeyDTOTestRun[] = graphNames.getGraphUniqueKeyDTO();
            
      if(skipValue == SKIP_CUMTIMESANDTIMESSTD)
      {
	for(int j = 0; j < arrGraphUniqueKeyDTOTestRun.length; j++)
	{
	  if(graphNames.getDataTypeNumByGraphUniqueKeyDTO(arrGraphUniqueKeyDTOTestRun[j]) < DATA_TYPE_CUMULATIVE)
	    arrFilteredGraphDTOList.add(arrGraphUniqueKeyDTOTestRun[j]);  
	}
      }
      
      if(skipValue == SKIP_TIMESANDTIMESSTD)
      {
	for (int j = 0; j < arrGraphUniqueKeyDTOTestRun.length; j++)
	{
	  if(graphNames.getDataTypeNumByGraphUniqueKeyDTO(arrGraphUniqueKeyDTOTestRun[j]) < DATA_TYPE_TIMES)
	    arrFilteredGraphDTOList.add(arrGraphUniqueKeyDTOTestRun[j]);  
	}
      }
      
      if (skipValue == SKIP_CUM)
      {
	for (int j = 0; j < arrGraphUniqueKeyDTOTestRun.length; j++)
	{
	  if(graphNames.getDataTypeNumByGraphUniqueKeyDTO(arrGraphUniqueKeyDTOTestRun[j]) != DATA_TYPE_CUMULATIVE)
	    arrFilteredGraphDTOList.add(arrGraphUniqueKeyDTOTestRun[j]);
	}
      }
      
      /*Checking for Filtered Graph Data Index.*/
      arrGraphUniqueKeyDTO = new GraphUniqueKeyDTO[arrFilteredGraphDTOList.size()];
      
      /*Getting Filtered List of Graph DTO and Graph Index Array.*/
      for(int k = 0; k < arrFilteredGraphDTOList.size(); k++)
      {
	GraphUniqueKeyDTO graphUniqueKeyDTO = arrFilteredGraphDTOList.get(k);
	arrGraphUniqueKeyDTO[k] = graphUniqueKeyDTO;
      }     
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Generating Correlated Graph Data.
   * @return
   */
  public boolean generatePearsonCorrelation()
  {
    Log.debugLogAlways(className, "generatePearsonCorrelation", "", "", "Method called");

    try
    {
      long startTimeInMilliSec = System.currentTimeMillis();

      /*Need to calculate for all indexes.*/
      if(calcAllIndex && skipCumGraph && skipTimesAndTimesStd)
      {	
	Log.debugLogAlways(className, "generatePearsonCorrelation", "", "", "Going To Correlate All Graph except Times, TimeSTD and Cumulative Graphs.");
        getFilteredGraphDTOList(SKIP_CUMTIMESANDTIMESSTD);
      }
      else if(calcAllIndex && skipTimesAndTimesStd)
      {
	Log.debugLogAlways(className, "generatePearsonCorrelation", "", "", "Going To Correlate All Graph except Times and TimeSTD Graphs.");
        getFilteredGraphDTOList(SKIP_TIMESANDTIMESSTD);
      }
      else if(calcAllIndex && skipCumGraph)
      {
	Log.debugLogAlways(className, "generatePearsonCorrelation", "", "", "Going To Correlate All Graph except Cumulative Graphs.");
        getFilteredGraphDTOList(SKIP_CUM);
      }
      else if(calcAllIndex)
      {
	Log.debugLogAlways(className, "generatePearsonCorrelation", "", "", "Going To Correlate All Graphs of Test Run.");
        arrGraphUniqueKeyDTO = graphNames.getGraphUniqueKeyDTO();
        
      }
      
      /*Create DTO array.*/
      graphUniqueKeyDTOList = new GraphUniqueKeyDTO[arrGraphUniqueKeyDTO.length + 1];
      graphUniqueKeyDTOList[0] = baselineGraphUniqueKeyDTO;
      
      //Array to store calculated pearson value
      arrPearsonValue = new double[arrGraphUniqueKeyDTO.length];
      Arrays.fill(arrPearsonValue, ERROR_CODE); // first fill all by ERROR_CODE

      //Here we are initializing size of vectors so we can set value on particular index
      //Adding 2, 0 index - Elapse time info, 1 index - baseline info, then rest of the index info
      arrListGraphNames.setSize(arrPearsonValue.length + 2);
      arrListSumOfGraphData.setSize(arrPearsonValue.length + 2);
      arrListSumOfSquareOfGraphData.setSize(arrPearsonValue.length + 2);
      arrListSumOfCartesianProduct.setSize(arrPearsonValue.length + 2);
      arrListPearsonValue.setSize(arrPearsonValue.length + 2);
      
      /*Copying DTO in main DTO List.*/
      for(int i = 1; i < arrGraphUniqueKeyDTO.length + 1; i++)
	graphUniqueKeyDTOList[i] = arrGraphUniqueKeyDTO[i - 1];

      //Converting correlation pct into correlation factor
      double correlationFactor = calcCorrelationFactorByPctValue();
      
      Log.debugLogAlways(className, "generatePearsonCorrelation", "", "", "correlationFactor = " + correlationFactor);

      //if missing correlation factor it will take default value
      if(correlationFactor == -1)
        correlationFactor = DEFAULT_PEARSON_FACTOR;

      //Getting data of selected index of graph of specified time
      if(!getAndSetDataFromRawDataFile("Specified Time", startTime, endTime, false, true, true, true))
      {
        Log.errorLog(className, "generatePearsonCorrelation", "", "", "Error occur while processing raw data file.");
        correlationRequestDTO.setSuccessful(false);
        correlationRequestDTO.setErrorMsg("Error occur while reading and processing raw data file.");
        return false;
      }

      //This is to generate csv file if Debug level value is greater than equals to 4 than create logs
      if((Integer.parseInt(Config.getValue("debuglevel"))) >= 4)
      {
        arrListGraphNames.set(0, "Elapsed Time");
        arrListSumOfGraphData.set(0, "Sum of graph data");
        arrListSumOfSquareOfGraphData.set(0, "Sum of square of graph data");
        arrListSumOfCartesianProduct.set(0, "Sum of cartesian product");
        arrListSumOfCartesianProduct.set(1, "NA");
        arrListPearsonValue.set(0, "Pearson Value");
        arrListPearsonValue.set(1, "NA");
      }

      //sum of graph data
      if(!generateSumOfGraphDataAndSumSquareOfGraphData(baselineGraphIndex))
      {
        Log.errorLog(className, "generatePearsonCorrelation", "", "", "Error occur while generating sum of graph data and sum of square of graph data.");
        correlationRequestDTO.setSuccessful(false);
        correlationRequestDTO.setErrorMsg("Error occur while generating sum and sum square of graph data.");
        return false;
      }

      // default only one thread will create
      int createThread = calculateThreadForStart(arrGraphUniqueKeyDTO.length);
      
      if(createThread > 0)
        createThread = arrGraphUniqueKeyDTO.length;
      // total number of graphs
      int length = arrGraphUniqueKeyDTO.length;
      // If total graph in odd number
      int remainder = 0;

      // if graph more than 40 it will create 4 thread
      try
      {
        remainder = length % createThread;
      }
      catch (ArithmeticException e)
      {
        Log.debugLog(className, "generatePearsonCorrelation", "", "", "No thread will start.");
        // TODO: handle exception
      }

      // creating array of calculatePearsonValue class
      // define length on basis of createThread
      // Each index of array will create different thread
      Thread[] threadArr = new Thread[createThread];

      // If in config file thread 0
      // loop execute 1 time without thread
      // otherwise as it is
      int executeLoop = 1;

      if (createThread != 0)
        executeLoop = createThread;

      // setting start and end index of arrGraphIndex
      for(int i = 0; i < arrGraphUniqueKeyDTO.length; i++)
      {
        if (i == (createThread - 1))
        {
          isAliveAll = true;
        }

        Log.debugLogAlways(className, "generatePearsonCorrelation", "", "", "index = " + i);

        // from eventGeneratorAdvisoryObj to get object of rptData
        GraphUniqueKeyDTO graphDTO = arrGraphUniqueKeyDTO[i];
        int index = i;
        // If createThread is 1 it will not create thread and start.
        // Its simply execute call the method.
        if (createThread == 0)
        {
          Log.debugLog(className, "generatePearsonCorrelation", "", "", "Without Thread");
          calculatePearsonValue calculatePearsonValueObj = null;
          calculatePearsonValueObj = new calculatePearsonValue(arrBaselineGraphData, timeBasedTestRunDataObj, graphDTO, index, this, graphNames);
          calculatePearsonValueObj.getPearsonValue();
        }
        else
        {          
          calculatePearsonValue[] calculatePearsonValueObj = new calculatePearsonValue[createThread];
          calculatePearsonValueObj[i] = new calculatePearsonValue(arrBaselineGraphData, timeBasedTestRunDataObj, graphDTO, index, this, graphNames);
          threadArr[i] = new Thread(calculatePearsonValueObj[i], "Pearson - " + i);
          // Starting thread
          Log.debugLogAlways(className, "generatePearsonCorrelation", "", "", "Thread Start. Thread name = " + threadArr[i].getName());
          threadArr[i].start();
        }
      }
      // wait for all thread start
      // if last thread is started this flag become false and will go stop thread method
      // And notify all thread.
      if (createThread >= 1)
      {
        if (!isAliveAll)
          wait();

        // notify all thread
        // checking thread is running or not
        // All thread are not alive it stops all thread
        stopThread(threadArr);
      }

      // storing info baseline line graph detail
      String[] arrGraphDetail = rptUtilsBean.strToArrayData(graphDetails, "|");

      String correlaedGraphNames = "NA";
      String correlatedGroupGraphVectorName = "NA"; // it is comma separated
      
      double pearsonFactorValue = correlationFactor;

      // sort this array
      // Arrays.sort(arrPearsonValue);

      // now need to add those details to file
      // for(int i = (arrPearsonValue.length - 1); i >= 0; i--)
      for (int i = 0; i < arrPearsonValue.length; i++)
      {
        // Case 1:
        // For anti GraphPearsonvalue should be greater than threahold
        // |GPV| >= |Threahold|
        // case2 :
        // If threshhold is negative
        double graphPearsonValue = arrPearsonValue[i];
        if (graphPearsonValue != ERROR_CODE)
        {
          boolean executeFlag = false;

          if (isIncludeAntiFlag)
          {
            Log.debugLog(className, "generatePearsonCorrelation", "", "", "Threshold include anti. Graph pearson value = " + Math.abs(graphPearsonValue) + ", Threshold value = " + pearsonFactorValue);
            if (Math.abs(graphPearsonValue) >= pearsonFactorValue)
              executeFlag = true;
          }
          else if (pearsonFactorValue < 0)
          {
            if (graphPearsonValue <= pearsonFactorValue)
            {
              Log.debugLog(className, "generatePearsonCorrelation", "", "", "Threshold with negative value. Graph pearson value = " + graphPearsonValue + ", Threshold value = " + pearsonFactorValue);
              executeFlag = true;
            }
          }
          else if (graphPearsonValue >= pearsonFactorValue)
          {
            Log.debugLog(className, "generatePearsonCorrelation", "", "", "Threshold without anti. Graph pearson value = " + graphPearsonValue + ", Threshold value = " + pearsonFactorValue);
            executeFlag = true;
          }

          if(executeFlag)
          {
            GraphUniqueKeyDTO graphUniqueKeyDTO = arrGraphUniqueKeyDTO[i];
            String tempGraphNames = graphNames.getGraphNameByGraphUniqueKeyDTO(graphUniqueKeyDTO, true);

            String pearsonValInPct = "(" + fmtDouble(arrPearsonValue[i] * 100) + ")";
            if(correlaedGraphNames.equals("NA"))
            {
              correlaedGraphNames = tempGraphNames + pearsonValInPct;
              //correlaedGraphIndexes = graphUniqueKeyDTOList[i].getGraphDataIndex() + "";
              correlatedGroupGraphVectorName = graphUniqueKeyDTO.getGroupId() + ";" + graphUniqueKeyDTO.getGraphId() + ";" + graphUniqueKeyDTO.getVectorName();
            }
            else
            {
              correlaedGraphNames = correlaedGraphNames + "," + tempGraphNames + pearsonValInPct;
              //correlaedGraphIndexes = correlaedGraphIndexes + "," + graphUniqueKeyDTO.getGraphDataIndex();
              correlatedGroupGraphVectorName = correlatedGroupGraphVectorName + "," + graphUniqueKeyDTO.getGroupId() + ";" + graphUniqueKeyDTO.getGraphId() + ";" + graphUniqueKeyDTO.getVectorName();

            }
          }
        }
      }

      /*This is to generate csv file if Debug level value is greater than equals to 4 than create logs.*/
      if ((Integer.parseInt(Config.getValue("debuglevel"))) >= 4)
      {
        //if (generateCSVFile())
        //  Log.debugLogAlways(className, "generatePearsonCorrelation", "", "", "CSV file generated successfully.");
       // else
        //  Log.errorLog(className, "generatePearsonCorrelation", "", "", "Error occur while generating CSV file.");
      }

      if (!correlaedGraphNames.equals("NA"))
      {
        arrGraphDetail[GRAPH_DETAIL_INDEX] = correlatedGroupGraphVectorName;
        //update Related Graph field of array
        arrGraphDetail[RELATED_GRAPH_FIELD_INDEX] = correlaedGraphNames;
        //update correlation threshold

        if (isIncludeAntiFlag)
          pearsonFactorValue = -pearsonFactorValue;

        // 0.0 == 0.0
        // -0.0 == 0.0
        // both are equal
        if (pearsonFactorValue == 0.0)
          pearsonFactorValue = Math.abs(pearsonFactorValue);

        arrGraphDetail[VALUE_FIELD_INDEX] = (pearsonFactorValue * 100) + "";

        String correlatedGraphDetails = rptUtilsBean.strArrayToStr(arrGraphDetail, "|");
        
        Log.debugLogAlways(className, "generatePearsonCorrelation", "", "", "correlatedGraphDetails = " + correlatedGraphDetails);
        if (partitionInfoUtilsObj.addCorrelatedEventDataToFile(correlatedGraphDetails, testRunNum +""))
        {
          long endTimeInMilliSec = System.currentTimeMillis();
          String timeTaken = EventGeneratorAdvisory.convTo3DigitDecimal(((double) (endTimeInMilliSec - startTimeInMilliSec)) / 1000) + "";

          Log.debugLogAlways(className, "generatePearsonCorrelation", "", "", "Correlated data added successfully to file." + correlatedGraphDetails + ", total time taken (in seconds) = " + timeTaken);
          correlationRequestDTO.setSuccessful(true);
          return true;
        }
        else
        {
          long endTimeInMilliSec = System.currentTimeMillis();
          String timeTaken = EventGeneratorAdvisory.convTo3DigitDecimal(((double) (endTimeInMilliSec - startTimeInMilliSec)) / 1000) + "";

          Log.errorLog(className, "generatePearsonCorrelation", "", "", "Error occur while adding data to file, total time taken (in seconds) = " + timeTaken);
          correlationRequestDTO.setSuccessful(false);
          correlationRequestDTO.setErrorMsg("Error occur while adding correlated data to file. Please check error logs.");
          return false;          
        }
      }
      else
      {
        long endTimeInMilliSec = System.currentTimeMillis();
        String timeTaken = EventGeneratorAdvisory.convTo3DigitDecimal(((double) (endTimeInMilliSec - startTimeInMilliSec)) / 1000) + "";

        Log.debugLogAlways(className, "generatePearsonCorrelation", "", "", "Correlated data not available, total time taken (in seconds) = " + timeTaken);
        correlationRequestDTO.setErrorMsg("Correlated data not available. Please check error logs.");
        correlationRequestDTO.setSuccessful(false);
        //if no graph is correlated & analysis_alert_offline.dat file is also not exist.
        return false;
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "generatePearsonCorrelation", "", "", "Exception - ", e);
      correlationRequestDTO.setErrorMsg("Error occur while correlating graphs. Please check error logs.");
      correlationRequestDTO.setSuccessful(false);
      return false;
    }
  }

  // Convert the double value to "###.###" format
  public String fmtDouble(double val)
  {
    String fmt = "###";

    DecimalFormat frmt = new DecimalFormat(fmt);
    return frmt.format(val);
  }

  /**
   * This is to generate CSV file
   * @return
   */
  private boolean generateCSVFile()
  {
    Log.debugLogAlways(className, "generateCSVFile", "", "", "Method called.");

    //csv file name
    String fileWithPath = Config.getValueWithPath("logFilePath") + "/analysis_alert_correlation.csv";

    try
    {
      long sampleTime = 0;
      long interval = graphNames.getInterval();

      String strStartTime = "00:00:10";
      if (!startTime.equals("NA"))
        strStartTime = startTime;

      File fileObj = new File(fileWithPath);
      if (fileObj.exists())
        fileObj.delete();

      fileObj.createNewFile();

      FileOutputStream fout = new FileOutputStream(fileObj, true); // append mode
      PrintStream printStream = new PrintStream(fout);

      String dataLine = "";
      for (int i = 0; i < arrListGraphNames.size(); i++)
      {
        if (i == 0)
          dataLine = arrListGraphNames.get(i).toString();
        else
          dataLine = dataLine + "," + arrListGraphNames.get(i).toString();
      }

      // first write header line
      printStream.println(dataLine);

      for (int i = 0; i < arrGraphData[0].length; i++)
      {
        sampleTime = i * interval;
        dataLine = eventGeneratorAdvisoryObj.getSampleTime(strStartTime, sampleTime);

        for (int j = 0; j < arrGraphData.length; j++)
          dataLine = dataLine + "," + arrGraphData[j][i];

        // write data line
        printStream.println(dataLine);
      }

      // this is for sum of graph data
      for (int i = 0; i < arrListSumOfGraphData.size(); i++)
      {
        if (i == 0)
          dataLine = arrListSumOfGraphData.get(i).toString();
        else
          dataLine = dataLine + "," + arrListSumOfGraphData.get(i).toString();
      }

      // write sum of graph data line
      printStream.println(dataLine);

      // this is for sum of square of graph data
      for (int i = 0; i < arrListSumOfSquareOfGraphData.size(); i++)
      {
        if (i == 0)
          dataLine = arrListSumOfSquareOfGraphData.get(i).toString();
        else
          dataLine = dataLine + "," + arrListSumOfSquareOfGraphData.get(i).toString();
      }

      // write sum of square of graph data line
      printStream.println(dataLine);

      // this is for sum of Cartesian Product
      for (int i = 0; i < arrListSumOfCartesianProduct.size(); i++)
      {
        if (i == 0)
          dataLine = arrListSumOfCartesianProduct.get(i).toString();
        else
          dataLine = dataLine + "," + arrListSumOfCartesianProduct.get(i).toString();
      }

      // write sum of Cartesian Product data line
      printStream.println(dataLine);

      for (int i = 0; i < arrListPearsonValue.size(); i++)
      {
        if (i == 0)
          dataLine = arrListPearsonValue.get(i).toString();
        else
          dataLine = dataLine + "," + arrListPearsonValue.get(i).toString();
      }

      // write pearson value data line
      printStream.println(dataLine);

      // now close this file
      printStream.close();
      fout.close();

      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "generateCSVFile", "", "", "Exception - ", e);
      return false;
    }
  }

  /**
   * Get and set data from rtgMessage.dat file By ReportData object
   * @param arrGraphIndex
   * @param time
   * @param startTime
   * @param endTime
   * @param calAllIndex
   * @param minMaxFlag
   * @param avgFlag
   * @param stdDevFlag
   * @return
   */
  private boolean getAndSetDataFromRawDataFile(String time, String startTime, String endTime, boolean calAllIndex, boolean minMaxFlag, boolean avgFlag, boolean stdDevFlag)
  {
    Log.debugLogAlways(className, "getAndSetDataFromRawDataFile", "", "", "Method called. Test Run is  " + testRunNum + ", MinMax Flag = " + minMaxFlag + ", Avg Flag = " + avgFlag + ", Std-dev flag = " + stdDevFlag);

    try
    {      
      if(graphUniqueKeyDTOList == null || graphUniqueKeyDTOList.length == 0)
      {
	Log.errorLog(className, "getAndSetDataFromRawDataFile", "", "", "Getting Empty Graph List for Correlation. Taking All Graphs of Test Run.");
	graphUniqueKeyDTOList = graphNames.getGraphUniqueKeyDTO();
      }
      
      LinkedHashMap<Integer, GraphUniqueKeyDTO> hmGraphDTOList =  new LinkedHashMap<Integer, GraphUniqueKeyDTO>();

      /*Creating HashMap for Making Graph Data Request.*/
      for(int i = 0; i < graphUniqueKeyDTOList.length; i++)
      {
	int graphDataIndex = graphNames.getGraphDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTOList[i]);
	hmGraphDTOList.put(graphDataIndex, graphUniqueKeyDTOList[i]);
	
	Log.debugLogAlways(className, "getAndSetDataFromRawDataFile", "", "", "Method Called. Requested Graph = " + graphUniqueKeyDTOList[i]);
      }

      /*Making Graph Data Request.*/
      timeBasedTestRunDataObj = rptData.getTimeBasedDataFromRTGFile(hmGraphDTOList, time, startTime, endTime, calAllIndex, false);
            
      /*Checking for data availability*/
      if(timeBasedTestRunDataObj == null)
      {
	Log.errorLog(className, "getAndSetDataFromRawDataFile", "", "", "Error in Getting Time Based Data.");
	return false;
      }

      /*Creating Array Size.*/
      arrGraphData = new double[graphUniqueKeyDTOList.length][];

      /*Creating Graph Data Array.*/
      for(int i = 0; i < graphUniqueKeyDTOList.length; i++)
      {
	TimeBasedDTO timeBasedDTO = timeBasedTestRunDataObj.getTimeBasedDTO(graphUniqueKeyDTOList[i]);
	arrGraphData[i] = timeBasedDTO.getArrGraphSamplesData(); 
      }

      /*Checking Graph Data Availability.*/
      if(arrGraphData == null)
      {
	Log.errorLog(className, "getAndSetDataFromRawDataFile", "", "", "Error in Reading/Creating Data From RTG File.");
	return false;
      }
      
      /*Setting in DTO object.*/
      correlationRequestDTO.setTimeBasedTestRunDataObj(timeBasedTestRunDataObj);
      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getAndSetDataFromRawDataFile", "", "", "Exception - ", e);
      return false;
    }
  }

  public static void main(String[] args)
  {
    int testRunNum = 20142;
    int baselineGraphIndex = 0;
    String ruleFileName = "_netstorm_advisory_rule.nar";
    int ruleMode = 2;

    String graphDetail = "Running Vusers|Test Moving Avg|50.0|00:01:20|00:38:10|NA|NA|0|NA|NA|41936|-1|NA|NA|NA|NA|NA|NA";
    // int[] otherGraphIndex = {1, 6, 46,7, 19, 49, 44,56, 89, 66,618,56,90, 619, 77, 99, 3, 67, 89, 84, 12, 1, 6, 46,7, 19, 49, 44,56, 89, 66,618,56,90, 619, 77, 99, 3, 67, 89, 84, 12};
    int[] otherGraphIndex = { 1, 6, 46, 7, 19, 49, 44, 56, 89, 66, 618, 56, 90, 619, 77, 99, 3, 67, 89, 84, 12, 1, 6, 46, 7, 19, 49, 44, 56, 89, 66, 618, 56, 90, 619, 77, 99, 3, 67, 89, 84, 12, 56, 89, 66, 618, 56, 90, 619, 77, 99, 3, 67, 56, 89, 66, 618, 56, 90, 619, 77, 99, 3, 67 };
    // int[] otherGraphIndex = {1, 6, 46,7, 19, 49, 44,56, 89, 66,618,56,90, 619, 77, 99, 3, 67, 89, 84, 12};
    // int[] otherGraphIndex = {1, 6,89, 84, 12};
    boolean isIncludeAntiFlag = false;
    String startTimeForRule = "00:00:10";
    String endTimeForRule = "00:22:40";
    int baselineTR = 20142;

    EventGeneratorAdvisory eventGeneratorAdvisoryObj = new EventGeneratorAdvisory(testRunNum, ruleFileName, ruleMode, startTimeForRule, endTimeForRule, baselineTR);
    eventGeneratorAdvisoryObj.initReportData();

    PearsonCorrelation pearsonCorrelationObj = new PearsonCorrelation(testRunNum, baselineGraphIndex, otherGraphIndex, graphDetail, eventGeneratorAdvisoryObj, 10, isIncludeAntiFlag, startTimeForRule, endTimeForRule, false, false, false);

    pearsonCorrelationObj.generatePearsonCorrelation();

    double[] arrPearsonValue = pearsonCorrelationObj.getArrPearsonValue();
    for (int i = 0; i < arrPearsonValue.length; i++)
      System.out.println("Pearson Value for [" + i + "] index = " + arrPearsonValue[i]);
  }
}

/**
 * Inner class calculate pearson value
 *
 */
class calculatePearsonValue implements Runnable
{
  private final String className = "calculatePearsonValue";
  private final int ERROR_CODE = -10;

  private double sumOfOtherGraphData = -1;
  private double sumOfSquareOfOtherGraphData = -1;
  private double[] arrBaselineGraphData = null;
  private GraphUniqueKeyDTO graphUniqueKeyDTO = null;
  private TimeBasedTestRunData timeBasedTestRunDataObj = null;
  int index = 0;
  GraphNames graphNamesObj = null;

  private PearsonCorrelation PearsonCorrelationObj = null;

  /**
   * Constructor for initializing values.
   * @param arrBaselineGraphData
   * @param arrGraphData
   * @param arrGraphIndex
   * @param startIndex
   * @param endIndex
   * @param PearsonCorrelationObj
   * @param graphNamesObj
   */
  public calculatePearsonValue(double[] arrBaselineGraphData, TimeBasedTestRunData timeBasedTestRunData, GraphUniqueKeyDTO graphUniqueKeyDTO, int index, PearsonCorrelation PearsonCorrelationObj, GraphNames graphNamesObj)
  {   
    Log.debugLogAlways(className, "calculatePearsonValue", "", "", "index = " + index);
    
    this.PearsonCorrelationObj = PearsonCorrelationObj;
    this.arrBaselineGraphData = arrBaselineGraphData;
    this.graphNamesObj = graphNamesObj;
    this.timeBasedTestRunDataObj = timeBasedTestRunData;
    this.graphUniqueKeyDTO = graphUniqueKeyDTO;
    this.index = index;
  }

  public void run()
  {
    getPearsonValue();
  }

  public void getPearsonValue()
  {
    double[] arrRptData = timeBasedTestRunDataObj.getTimeBasedDTO(graphUniqueKeyDTO).getArrGraphSamplesData();
    String tempGraphNames = graphNamesObj.getGraphNameByGraphUniqueKeyDTO(graphUniqueKeyDTO, true);

    /*This is to generate csv file if Debug level value is greater than equals to 4 than create logs.*/
    if(((Integer.parseInt(Config.getValue("debuglevel"))) >= 4) && index != 0)
    {
      PearsonCorrelationObj.arrListGraphNames.set((index), tempGraphNames);
    }

    /*This is to generate pearson Value.*/
    double pearsonValue = generatePearsonValue(arrBaselineGraphData, PearsonCorrelationObj.sumOfBaselineGraphData, PearsonCorrelationObj.sumOfSquareOfBaselineGraphData, arrRptData, tempGraphNames, index, graphUniqueKeyDTO);

    if(pearsonValue == ERROR_CODE)
    {
      Log.errorLog(className, "generatePearsonCorrelation", "", "", "Error occur while generating Pearson value.");
    }
    else if (index >= 0)
    {
      Log.debugLog(className, "generatePearsonCorrelation", "", "", "Pearson value = " + pearsonValue + " for graph " + tempGraphNames);
      PearsonCorrelationObj.arrPearsonValue[index] = pearsonValue; // because arrPearsonValue is maintain only for other graphs not for baseline graph
    }
  }

  /**
   * This is to generate sum of graph data and sum square of graph data.
   * @param arrRptData
   * @param strGraphNames
   * @param startIndex
   * @return
   */
  private boolean generateSumOfGraphDataAndSumSquareOfGraphData(double[] arrRptData, String strGraphNames, int startIndex)
  {
    Log.debugLogAlways(className, "generateSumOfGraphDataAndSumSquareOfGraphData", "", "", "Method called. data array length = " + arrRptData.length + ", str graph names = " + strGraphNames);

    try
    {
      double sumOfGraphData = 0;
      double sumOfSquareOfGraphData = 0;

      for(int i = 0; i < arrRptData.length; i++)
      {
        sumOfGraphData = sumOfGraphData + arrRptData[i];
        sumOfSquareOfGraphData = sumOfSquareOfGraphData + arrRptData[i] * arrRptData[i];
      }

      sumOfOtherGraphData = sumOfGraphData;
      sumOfSquareOfOtherGraphData = sumOfSquareOfGraphData;

      /*This is to generate csv file if Debug level value is greater than equals to 4 than create logs.*/
      if((Integer.parseInt(Config.getValue("debuglevel"))) >= 4)
      {
        PearsonCorrelationObj.arrListSumOfGraphData.set((startIndex), sumOfGraphData + "");
        PearsonCorrelationObj.arrListSumOfSquareOfGraphData.set((startIndex), sumOfSquareOfGraphData + "");
      }

      Log.debugLogAlways(className, "generateSumOfGraphDataAndSumSquareOfGraphData", "", "", "Sum of graph data = " + sumOfGraphData + ", sum of square of graph data = " + sumOfSquareOfGraphData + " for graph = " + strGraphNames);

      return true;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "generateSumOfGraphDataAndSumSquareOfGraphData", "", "", "Exception - ", e);
      return false;
    }
  }

  /**
   * This is to generate sum of Cartesian Product
   * @param arrBaselineGraphData
   * @param arrRptData
   * @param startIndex
   * @return
   */
  private double generateSumOfCartesianProduct(double[] arrBaselineGraphData, double[] arrRptData, int startIndex)
  {
    Log.debugLogAlways(className, "generateSumOfCartesianProduct", "", "", "Method called.");

    try
    {
      double sumOfCartesianProduct = 0;
      for (int i = 0; i < timeBasedTestRunDataObj.getDataItemCount(); i++)
      {
        sumOfCartesianProduct = sumOfCartesianProduct + arrBaselineGraphData[i] * arrRptData[i];
      }

      /*This is to generate csv file if Debug level value is greater than equals to 4 than create logs.*/
      if((Integer.parseInt(Config.getValue("debuglevel"))) >= 4)
        PearsonCorrelationObj.arrListSumOfCartesianProduct.set((startIndex), sumOfCartesianProduct + "");

      Log.debugLogAlways(className, "generateSumOfCartesianProduct", "", "", "Sum of cartesian product = " + sumOfCartesianProduct);

      return sumOfCartesianProduct;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "generateSumOfCartesianProduct", "", "", "Exception - ", e);
      return ERROR_CODE;
    }
  }

  /**
   * This is to generate pearson Value
   * @param arrBaselineGraphData
   * @param sumOfBaselineGraphData
   * @param sumOfSquareOfBaselineGraphData
   * @param arrRptData
   * @param strGraphNames
   * @param startIndex
   * @return
   */
  private double generatePearsonValue(double[] arrBaselineGraphData, double sumOfBaselineGraphData, double sumOfSquareOfBaselineGraphData, double[] arrRptData, String strGraphNames, int startIndex, GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    Log.debugLog(className, "generatePearsonValue", "", "", "Method called.");

    try
    {
      if (!generateSumOfGraphDataAndSumSquareOfGraphData(arrRptData, strGraphNames, startIndex))
      {
        Log.errorLog(className, "generatePearsonValue", "", "", "Error occur while generating sum of graph data and sum of square of graph data.");
        return ERROR_CODE;
      }

      double sumOfCartesianProduct = generateSumOfCartesianProduct(arrBaselineGraphData, arrRptData, startIndex);

      if (sumOfCartesianProduct == ERROR_CODE)
      {
        Log.errorLog(className, "generatePearsonValue", "", "", "Error occur while generating sum of Cartesian Product.");
        return ERROR_CODE;
      }

      double numSample = timeBasedTestRunDataObj.getDataItemCount();
      Log.debugLog(className, "generatePearsonValue", "", "", "Number of sample = " + numSample);

      double upperPart = sumOfCartesianProduct - ((sumOfBaselineGraphData * sumOfOtherGraphData) / numSample);

      Log.debugLog(className, "generatePearsonValue", "", "", "sumOfCartesianProduct = " + sumOfCartesianProduct + ", sumOfBaselineGraphData= " + sumOfBaselineGraphData + ", sumOfOtherGraphData = " + sumOfOtherGraphData + ", (sumOfBaselineGraphData * sumOfOtherGraphData) = " + (sumOfBaselineGraphData * sumOfOtherGraphData) + ", ((sumOfBaselineGraphData * sumOfOtherGraphData)/numSample) = " + ((sumOfBaselineGraphData * sumOfOtherGraphData) / numSample));

      double lowerFirstPart = (sumOfSquareOfBaselineGraphData - (sumOfBaselineGraphData * sumOfBaselineGraphData) / numSample);
      double lowerSecondPart = (sumOfSquareOfOtherGraphData - (sumOfOtherGraphData * sumOfOtherGraphData) / numSample);
      double lowerPart = Math.sqrt(lowerFirstPart * lowerSecondPart);

      Log.debugLog(className, "generatePearsonValue", "", "", "upperPart = " + upperPart + ", lowerPart = " + lowerPart + " for graph " + strGraphNames);

      double pearsonValue = 0;
      if (lowerPart != 0) // if lower part is 0 then value is NaN
        pearsonValue = upperPart / lowerPart;
      else if(PearsonCorrelationObj.correlatedValuePct == 100 && graphUniqueKeyDTO.equals(PearsonCorrelationObj.baselineGraphUniqueKeyDTO)) /*It must match to itself on 100 % threshold value.*/
	pearsonValue = 1;

      // this is to generate csv file if Debug level value is greater than equals to 4 than create logs
      if ((Integer.parseInt(Config.getValue("debuglevel"))) >= 4)
        PearsonCorrelationObj.arrListPearsonValue.set((startIndex), EventGeneratorAdvisory.convTo3DigitDecimal(pearsonValue) + "");

      Log.debugLog(className, "generatePearsonValue", "", "", "Calculated pearson value = " + EventGeneratorAdvisory.convTo3DigitDecimal(pearsonValue) + " for graph " + strGraphNames);

      return pearsonValue;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "generatePearsonValue", "", "", "Exception - ", e);
      return ERROR_CODE;
    }
  }
}

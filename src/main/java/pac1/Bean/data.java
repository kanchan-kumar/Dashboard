/**----------------------------------------------------------------------------
 * Name       data.java
 * Purpose    processing of message data reveived from netstorm. It generates
 *             - graphData
 *             - minData
 *             - maxData
 *             - avgData
 *
 * @author    Neeraj/Pratibha
 * @version   1.2
 * @since     1.0
 *
 *
 * Modification History
 *   12/20/04:Pratibha:1.2 - Redesign based on new structure of message
 *   01/11/05:Neeraj:1.2
 *     - Fixed strCurCountIndex (it had graphData Index).
 *     - Converted graphData and avgData to upto 3 decimal points.
 *   01/12/05:Neeraj:1.2
 *     - Added check for -1 (4294967295D) min.
 *     - Changed rawData to static as we do not need to pass it to client
 *   07/21/05:Neeraj:1.4
 *     - Added Mill-Sec to Sec and Byts/Sec to Kbps conversion.
 *   10/26/05:Neeraj/Vinit:1.4
 *     - Added Min/Max for all graphs.
 *   09/11/06:Abhishek
       - Added Function to get server name
 *   10/12/06:Neeraj
       - Added Functions for Graphs and minor changes
 *   01/01/07:Abhishek
       - Added function which process data for all graph indexes pass in array
 *   12/22/08:Prabhat
       - Change rawData long to double (#3.1.2)
 *   02/04/2009:Prabhat
       - Add graph stats analysis result in header
     25/09/2009:Atul:3.5.0
       - Create wrapper method processHdrVer2WithByteBuffer for processHdrVer2
         which will take ByteBuffer insteadof byte[].This new method will be
         called from NetsormListener-->put method if found data packet need
         to handle pause/Resume feature
 * Notes
 *  static variables are not serialized which is good as less data will be send.
 *---------------------------------------------------------------------------**/

/***********************************************************************************************************************
 *  Analysis Details : Prabhat --> 02/04/2009
 *  Build Version : 3.2.2
 *  GDF Version : 2.1
 *  PDF Version : 1.0
 *  Number of Samples : 5583 (excluding start & end pkt)
 *  Each Packet Size : 27920 bytes
 *  rtgMessage.dat file size : 155933200 bytes (149 MB)
 *  pctMessage.dat file size : 10283082048 bytes (9.6 GB)
 *  Test Run Duration : (15:30:23) hours
 *  Total Number of Graphs : 1637
 *
 *  Note : Debug Flag is ON (means writing all logs in log  file)
 *
 *  Getting data In offline mode, for all index
 *
 *  Timing Result -->
 *    1- GraphData with all graph stats(min, max, avg, std-dev) --> 40 secs
 *    2- GraphData with graph stats(min, max) --> 31 secs
 *    3- GraphData with graph stats(avg, std-dev) --> 37 secs
 *    4- GraphData with graph stats(avg) --> 34 secs
 *    5- GraphData with no graph stats --> 28 secs
 *    6- GraphData with no graph stats and debug flag off --> 27 secs
 *
 ***********************************************************************************************************************/
/*********************************************************************************************
 * Note : Prabhat --> (04/10/2009)(#build -> 3.2.6)
 *
 * Avg data shows in different location that are --
 *   RTG Lower Pane --> through msgData in running scenario, or in offline mode we read whole data file
 *   RTG ZOOM Panel --> read only selected data, and calc & create msgData again
 *   Analysis GUI Lower Pane --> read only selected data on the basis of time option
 *   Analysis GUI Zoom Panel --> read only selected data, and calc & create msgData again
 *   Percentile Reports(Frequency Distribution (legend)) --> created from Analysis GUI so generate only selected data
 *   Compare Reports (table & legend) --> we process test run data only for selected time duration
 *   Report Generation(Legend) --> generate data through overalldata on the basis of start & end seq
 ***********************************************************************************************/

package pac1.Bean;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.StringTokenizer;

import pac1.Bean.GraphName.GraphNameUtils;
import pac1.Bean.GraphName.GraphNames;

public class data implements java.io.Serializable, Cloneable
{
  // Static members
  final static private String className = "data";

  public final static int OPCODE_INDEX = 0;
  public final static int TEST_RUN_INDEX = 1;
  public final static int INTERVAL_INDEX = 2;
  final static int SEQ_NUM_INDEX = 3;
  static int PROCESS_HDR_LEN = 32; // process only 32 bytes(4 double), this may be increase in future
  final static int MAX_INITIAL_SEQ_COUNTER = 6; // It is to check first 6 sample's data value, if any non zero data value found then it will be set to Initial value other wise initial value will be
                                                // zero.
  final static int TIMESTAMP_IDX = 5;
  final static int PARTITION_FILE_IDX = 4;

  public final static int START_PKT = 0;
  public final static int DATA_PKT = 1;
  public final static int END_PKT = 2;

  // these are control packet number
  public final static int PAUSED_MSG = 11;
  public final static int RESUME_MSG = 12;

  public final static int DATA_PKT_SIZE_TX_BY_NS = 112; // size of data packet that is received from NetStorm (START_PKT, DATA_PKT, END_PKT & All control data packet(PAUSED_MSG, RESUME_MSG))

  // private (Non transient) members
  private int rawDataLen = 0; // Raw data size in multiple of long(ver < 2) OR in double (ver > 2)
  private int processedDataLen = 0; // Processes data size in multiple of long(ver < 2) OR in double (ver > 2) (This is without hdr)
  private int msgHdrLen = 0; // Message header size in multiple of long(ver < 2) OR in double (ver > 2)
  private long msgDataLen = 0L;

  private int opcode = -1;
  private long interval = -1;
  private long seqNum = -1; // sequence number of data packet
  private double absTimeStamp = 0; // Storing time stamp.
  private double partition_File_Name = 0; // Storing Name of Partition Folder.
  private int partitionFileVersion = 0; //Storing Partition File Version.

  // private Date time = null; // Remove this in future. Data/Time when packet was received by Server
  // private long timeStamp = 0; // Timestamp when packet was received by Server

  private int testRun = -1;
  public boolean newRunTest = false; // Not used now

  private double graphData[] = null;
  private double minData[] = null;
  private double maxData[] = null;
  private double avgData[] = null;
  private double stdDev[] = null;

  // use to store the initial value of data, for CUMULATIVE graph type
  // this is use to calculate avg data of CUMULATIVE graph
  private transient double cumInitialValue[] = null;

  // private (transient) members
  // Raw Data is used to generate graphData and is used to get curCount
  private double rawData[] = null; // Changed from static to transient

  private long cumCount[] = null; // Use for generating avgData
  private double curCount[] = null; // Use for generating avgData.
  private transient double cumSumSqr[] = null; // Use for generating Std-Dev

  private transient int minIndex[] = null; // Use for generating minData
  private transient int maxIndex[] = null; // Use for generating maxData

  transient int curCountIndex[] = null; // Use for generating avgData. This is index in Raw Data
  private int arrGraphFormula[] = null; // Use for graph formula (0-SEC, 1-PM, 2-PS, 3-KBPS, 4-DBH and -1 for not any formula applied on that Graph)

  // It is transient.
  private int arrDataTypeIndx[] = null; // Use for graph data index
  private int arrDataType[] = null;
  private int majorVersionNum = 0; // Use to store the major version of gdf
  private int minorVersionNum = 0; // Use to store the minor version of gdf
  private int dataElementSize = 0; // Use to store the data element size either long(4 bytes) OR double(8 bytes)

  private double PMMultiplier = 0; // Used For Per Minute
  private int PSDivisor = 0; // Used For Per Seconds.
  
  /*Used for sending TestCtrlData */
  private TestControlData testCtrlDataObj = null;

  private data generatorData[] = null;

  public data()
  {
    /*Initializing Test Control Data object.*/
    testCtrlDataObj = new TestControlData();
  }

  public void setSeqNum(long seqNum)
  {
    this.seqNum = seqNum;
  }

  public long getSeqNum()
  {
    return (seqNum);
  }

  public void setInterval(long interval)
  {
    this.interval = interval;
  }

  public long getInterval()
  {
    return (interval);
  }

  public void setOpcode(int opcode)
  {
    this.opcode = opcode;
  }

  public int getOpcode()
  {
    return (opcode);
  }

  public int getTestRun()
  {
    return testRun;
  }

  public void setTestRun(int testRun)
  {
    this.testRun = testRun;
  }

  public data[] getGeneratorData()
  {
    return this.generatorData;
  }

  public void setGeneratorData(data[] generatorData)
  {
    this.generatorData = generatorData;
  }

  public double[] getGraphData()
  {
    return graphData;
  }

  public double[] getMinData()
  {
    return minData;
  }

  public double[] getMaxData()
  {
    return maxData;
  }

  public double[] getAvgData()
  {
    return avgData;
  }

  public double[] getStdDev()
  {
    return stdDev;
  }

  public long[] getCumCount()
  {
    return cumCount;
  }

  public double[] getCurCount()
  {
    return curCount;
  }

  public double getPartitionFileName()
  {
    return partition_File_Name;
  }

  /**
   * This method is used to set absolute timestamp and partition name.
   * 
   * @param bb
   */
  public void setTimeStampAndPartitionName(ByteBuffer bb)
  {
    if (bb.limit() > 8 * TIMESTAMP_IDX)
      absTimeStamp = bb.getDouble(8 * TIMESTAMP_IDX);

    if (bb.limit() > 8 * PARTITION_FILE_IDX)
      partition_File_Name = bb.getDouble(8 * PARTITION_FILE_IDX);
    
    if(bb.limit() > 8 * DataPacketInfo.PARTITION_FILE_SEQUENCE_INDEX)
      partitionFileVersion = (int) bb.getDouble(8 * DataPacketInfo.PARTITION_FILE_SEQUENCE_INDEX);
      
  }

  public int getProcessedDataLen()
  {
    return processedDataLen;
  }

  // This is initializes for all data with transaction data
  public void initForNewTestRun(GraphNames graphNames)
  {
    dataElementSize = graphNames.getDataElementSize();
    rawDataLen = (graphNames.getSizeOfMsgData() / dataElementSize);
    msgHdrLen = (graphNames.getStartIndex() / dataElementSize);
    msgDataLen = graphNames.getSizeOfMsgData();
    processedDataLen = rawDataLen - msgHdrLen;

    majorVersionNum = graphNames.getMajorVersion();
    minorVersionNum = graphNames.getMinorVersion();

    if (majorVersionNum <= 2)
      Log.errorLog(className, "initForNewTimeBasedData", "", "", "Error: UnSupported Version. majorVersionNum = " + majorVersionNum + ", minorVersionNum = " + minorVersionNum);

    Log.debugLogAlways(className, "initForNewTestRun", "", "", "rawDataLen = " + rawDataLen + ", processedDataLen = " + processedDataLen + "Major Version = " + majorVersionNum + ", Minor Version = " + minorVersionNum + ", Data Element Size = " + dataElementSize);

    rawData = new double[rawDataLen];

    graphData = new double[processedDataLen];
    minData = new double[processedDataLen];
    maxData = new double[processedDataLen];
    avgData = new double[processedDataLen];
    stdDev = new double[processedDataLen];

    cumInitialValue = new double[processedDataLen];

    cumCount = new long[processedDataLen];
    curCount = new double[processedDataLen];
    cumSumSqr = new double[processedDataLen];

    initAllIndexes(graphNames);

    initOnStartPkt(interval);
  }

  /**
   * This method is used by execution gui for initializing msgData arrays.
   */
  public void initForNewTimeBasedData(GraphNames graphNames)
  {
    try
    {
      majorVersionNum = graphNames.getMajorVersion();
      minorVersionNum = graphNames.getMinorVersion();

      if (majorVersionNum <= 2)
        Log.errorLog(className, "initForNewTimeBasedData", "", "", "Error: UnSupported Version. majorVersionNum = " + majorVersionNum + ", minorVersionNum = " + minorVersionNum);

      dataElementSize = graphNames.getDataElementSize();
      rawDataLen = (graphNames.getSizeOfMsgData() / dataElementSize);
      msgHdrLen = (graphNames.getStartIndex() / dataElementSize);
      msgDataLen = graphNames.getSizeOfMsgData();
      processedDataLen = rawDataLen - msgHdrLen;

      // Initialize array with row data length.
      rawData = new double[rawDataLen];

      // Used for applying formula.
      arrDataType = graphNames.getDataTypeNumArray();
      arrDataTypeIndx = graphNames.getArrDataTypeIndx();
      arrGraphFormula = graphNames.getGraphFormula();
      setInterval(interval); // Interval comes in start packet
      setSeqNum(0); // For start packet, assume seqNum = 0      
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "initForNewTimeBasedData", "", "", "Exception - ", e);
    }
  }
  
  /**
   * Method is used to get Test Control Data object.
   * @return
   */
  public TestControlData getTestCtrlDataObj() 
  {
    return testCtrlDataObj;
  }

  /**
   * Method is used to set Test Control Data object.
   * @param testCtrlDataObj
   */
  public void setTestCtrlDataObj(TestControlData testCtrlDataObj) 
  {
    this.testCtrlDataObj = testCtrlDataObj;
  }

  /**
   * Used For Getting Graph Data Statistics by processing raw data.
   * 
   * @param graphIndex
   * @param graphStatsObj
   */
  public void getGraphStats(int graphDataIndex, GraphStats graphStatsObj)
  {
    try
    {
      // Maintaining Indexes.
      int rawDataIndex = (int) (msgHdrLen + graphDataIndex);
      double graphData = getGraphDataByFormula(graphDataIndex, rawData[rawDataIndex]);

      int dataType = arrDataType[arrDataTypeIndx[graphDataIndex]];
      // For Graph Type Times and TimesSTd.
      if (dataType == GraphNameUtils.DATA_TYPE_TIMES_STD || dataType == GraphNameUtils.DATA_TYPE_TIMES)
      {
        double minData = Double.MAX_VALUE;
        double maxData = 0.0;
        int count = (int) rawData[rawDataIndex + 3];
        if (count > 0) // If count is 0, then min and max value are not to be taken.
        {
          minData = getGraphDataByFormula(graphDataIndex, rawData[rawDataIndex + 1]);
          maxData = getGraphDataByFormula(graphDataIndex, rawData[rawDataIndex + 2]);
        }

        // If Graph is Times/TiemsSTD, then we need to multiply it by count.
        graphStatsObj.setGraphValue(graphData * count);
        graphStatsObj.setMinData(minData);
        graphStatsObj.setMaxData(maxData);
        graphStatsObj.setCount(count);

        if (dataType == GraphNameUtils.DATA_TYPE_TIMES_STD)
        {
          double sumSqr = getGraphDataByFormula(graphDataIndex, rawData[rawDataIndex + 4]); // TODO - check how formula will work on square.
          graphStatsObj.setSumSquare(sumSqr);
        }
        else
        {
          // Must multiplied with count if graph is Times type.
          graphStatsObj.setSumSquare(graphData * graphData * count);
        }
      }
      else
      {
        graphStatsObj.setGraphValue(graphData);
        graphStatsObj.setMinData(graphData);
        graphStatsObj.setMaxData(graphData);
        graphStatsObj.setCount(1);
        graphStatsObj.setSumSquare(graphData * graphData);
      }

      graphStatsObj.setLastSample(graphData);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getGraphStats", "", "", "Exception - ", e);
    }
  }

  /**
   * This Method apply formula on graph Data.
   * 
   * @param graphDataIndex
   * @param graphData
   * @return
   */
  private double getGraphDataByFormula(int graphDataIndex, double graphData)
  {
    try
    {
      // Used For applying formula.
      PMMultiplier = (60 / (interval / 1000.0));
      PSDivisor = (int) (interval / 1000);
      int formula = arrGraphFormula[graphDataIndex];

      // Check if formula is not available then no need to check conditions
      if (formula != GraphNameUtils.FORMULA_NONE && graphData != 4294967295D) // 4294967295D is -1, means data is not valid.
      {
        // Now apply strFormula to get processed data
        if (formula == GraphNameUtils.FORMULA_SEC) // SEC - Convert milli-sec to seconds
          graphData = graphData / 1000;

        else if (formula == GraphNameUtils.FORMULA_PM) // PM - Convert to Per Minute
          graphData = graphData * PMMultiplier;

        else if (formula == GraphNameUtils.FORMULA_PS) // PS - Convert to Per Seconds
          graphData = graphData / PSDivisor;

        else if (formula == GraphNameUtils.FORMULA_KBPS) // KBPS - Convert to kilo byte per second
          graphData = (graphData / 1024);

        else if (formula == GraphNameUtils.FORMULA_DBH) // DBH - Convert to divide by 100
          graphData = graphData / (100);
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getGraphDataByFormula", "", "", "Exception - ", e);
    }
    return graphData;
  }

  private void initAllIndexes(GraphNames graphNames)
  {
    arrDataType = graphNames.getDataTypeNumArray();
    arrDataTypeIndx = graphNames.getArrDataTypeIndx();
    arrGraphFormula = graphNames.getGraphFormula();
    minIndex = graphNames.getMinDataIndx();
    maxIndex = graphNames.getMaxDataIndx();
    curCountIndex = graphNames.getCurCountIndx();
  }

  // Initialize all arrays . This is done at the start of the graph (Opcode = 0)
  private void initOnStartPkt(long interval)
  {
    setInterval(interval); // Interval comes in start packet
    setSeqNum(0); // For start packet, assume seqNum = 0
    Arrays.fill(minData, Double.MAX_VALUE);
    Arrays.fill(maxData, 0); // Arrays.fill(maxData, Long.MIN_VALUE);
    Arrays.fill(cumCount, 0);
    Arrays.fill(cumSumSqr, 0);
    Arrays.fill(avgData, 0);
    Arrays.fill(stdDev, 0);
    Arrays.fill(graphData, 0);
    Arrays.fill(cumInitialValue, -1);

  }

  private void initOnDataPkt(long seqNum)
  {
    setSeqNum(seqNum); // seqNum comes in data packet
    Arrays.fill(graphData, 0); // initialize to 0 as we are doing sum
  }

  private void initOnEndPkt()
  {
    setSeqNum(0); // For end packet, assume seqNum = 0
    Arrays.fill(graphData, 0); // initialize to 0 as it not valid
  }

  // Generate interval if we missed start packet
  private void genInterval(data prevData)
  {
    // prevData.time = time;
    // prevData.timeStamp = timeStamp;
    prevData.opcode = opcode;
    prevData.interval = interval;
    prevData.seqNum = seqNum;

    prevData.absTimeStamp = absTimeStamp;
    prevData.partition_File_Name = partition_File_Name;
  }

  /**
   * This Method optimize MsgData for only selected Graphs in case of zooming graphs at client.
   * 
   * @param arrGraphNumbers
   * @param graphNames
   * @return
   */
  public data genActiveGraphsMsgData(int[] arrGraphNumbers, GraphNames graphNames)
  {
    Log.debugLog(className, "genActiveGraphsMsgData", "", "", "Method Called.");
    data activeGraphMsgData = new data();
    try
    {
      if (arrGraphNumbers == null)
      {
        Log.debugLog(className, "genActiveGraphsMsgData", "", "", "Getting Graph number array null.");
        return this;
      }

      activeGraphMsgData.processedDataLen = arrGraphNumbers.length;

      // Initialize arrays for required Graphs.
      activeGraphMsgData.graphData = new double[activeGraphMsgData.processedDataLen];
      activeGraphMsgData.minData = new double[activeGraphMsgData.processedDataLen];
      activeGraphMsgData.maxData = new double[activeGraphMsgData.processedDataLen];
      activeGraphMsgData.avgData = new double[activeGraphMsgData.processedDataLen];
      activeGraphMsgData.stdDev = new double[activeGraphMsgData.processedDataLen];
      // activeGraphMsgData.cumInitialValue = new double[activeGraphMsgData.processedDataLen];
      activeGraphMsgData.cumCount = new long[activeGraphMsgData.processedDataLen];
      activeGraphMsgData.setInterval(interval);

      // Filling Data for only selectedGraphs.
      for (int k = 0; k < arrGraphNumbers.length; k++)
      {

        int graphIndex = arrGraphNumbers[k];// graphNames.getGraphIndex(arrGraphNumbers[k]);
        activeGraphMsgData.graphData[k] = graphData[graphIndex];
        activeGraphMsgData.minData[k] = minData[graphIndex];
        activeGraphMsgData.maxData[k] = maxData[graphIndex];
        activeGraphMsgData.avgData[k] = avgData[graphIndex];
        activeGraphMsgData.stdDev[k] = stdDev[graphIndex];
        activeGraphMsgData.cumCount[k] = cumCount[graphIndex];
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "genActiveGraphsMsgData", "", "", "Exception in getting zoomed data ", e);
      // e.printStackTrace();
    }
    return activeGraphMsgData;
  }

  // process header for long type graph data
  public void processHdrVer1(byte recvData[])
  {
    ByteBuffer bb;

    PROCESS_HDR_LEN = 16; // for long type of raw data

    // Wrap only 16 bytes of data pkt, because hdr info available on 16 bytes
    bb = ByteBuffer.wrap(recvData, 0, PROCESS_HDR_LEN);

    // Neeraj - Network to host byte ordering code is not done. It is working
    // with netstorm without this.
    // process heading set interval and intialize Data Packet.
    testRun = bb.getInt(4 * TEST_RUN_INDEX);
    opcode = bb.getInt(4 * OPCODE_INDEX);
    interval = bb.getInt(4 * INTERVAL_INDEX);
    seqNum = bb.getInt(4 * SEQ_NUM_INDEX);

    if (opcode == START_PKT)
      Log.debugLog(className, "processHdrVer1", "", "", "New test run starts. Test Run = " + testRun + ",opcode = " + opcode + ",interval = " + interval + ",seqNum = " + seqNum);
    else if (opcode == DATA_PKT)
      initOnDataPkt(seqNum);
    else if (opcode == END_PKT)
      initOnEndPkt();
  }

  // process header for double type graph data
  public void processHdrVer2(byte recvData[])
  {
    ByteBuffer bb;

    PROCESS_HDR_LEN = 32; // for double type of raw data

    // Wrap only 32 bytes of data pkt, because hdr info available on 32 bytes
    bb = ByteBuffer.wrap(recvData, 0, PROCESS_HDR_LEN);

    bb = bb.order(ByteOrder.LITTLE_ENDIAN);

    // process heading set interval and intialize Data Packet.
    testRun = (int) bb.getDouble(8 * TEST_RUN_INDEX);
    opcode = (int) bb.getDouble(8 * OPCODE_INDEX);
    interval = (int) bb.getDouble(8 * INTERVAL_INDEX);
    seqNum = (int) bb.getDouble(8 * SEQ_NUM_INDEX);

    setTimeStampAndPartitionName(bb);

    if (opcode == START_PKT)
      Log.debugLog(className, "processHdrVer2", "", "", "New test run starts. Test Run = " + testRun + ",opcode = " + opcode + ",interval = " + interval + ",seqNum = " + seqNum);
    else if (opcode == DATA_PKT)
      initOnDataPkt(seqNum);
    else if (opcode == END_PKT)
      initOnEndPkt();
  }

  // Added by Neeraj on May 12th, 2011 as we need to set this before we can check for new test run
  public void setTestRunInfo(ByteBuffer bb)
  {
    testRun = (int) bb.getDouble(8 * TEST_RUN_INDEX);
    opcode = (int) bb.getDouble(8 * OPCODE_INDEX);
    interval = (int) bb.getDouble(8 * INTERVAL_INDEX);
    seqNum = (int) bb.getDouble(8 * SEQ_NUM_INDEX);

    setTimeStampAndPartitionName(bb);
  }

  public void setTestRunInfo(int opcode, long interval, long seqNum)
  {
    this.opcode = opcode;
    this.interval = interval;
    this.seqNum = seqNum;
  }

  public static long getSeqNumFromBB(ByteBuffer bb)
  {
    Log.debugLog(className, "getSeqNumFromBB", "", "", "Method called.");

    long localSeqNum = (long) bb.getDouble(8 * SEQ_NUM_INDEX);
    return localSeqNum;
  }

  public static long getOpcodeFromBB(ByteBuffer bb)
  {
    Log.debugLog(className, "getOpcodeFromBB", "", "", "Method called.");

    long localOpcode = (long) bb.getDouble(8 * OPCODE_INDEX);
    return localOpcode;
  }

  // process header for double type graph data called from NL-->put method
  public void processHdrVer2WithByteBuffer()
  {
    if (opcode == START_PKT)
      Log.debugLog(className, "processHdrVer2WithByteBuffer", "", "", "New test run starts. Test Run = " + testRun + ",opcode = " + opcode + ",interval = " + interval + ",seqNum = " + seqNum);
    else if (opcode == DATA_PKT)
      initOnDataPkt(seqNum);
    else if (opcode == END_PKT)
      initOnEndPkt();
  }

  public static double convTo3DigitDecimal(double val)
  {
    double dblVal;
    dblVal = (double) Math.round(val * 1000);
    dblVal = dblVal / 1000;
    return (dblVal);
  }

  public double conv2LToDouble(long lower, long upper)
  {
    Log.debugLog(className, "conv2LToDouble", "", "", "Method called : " + "lower = " + lower + "upper = " + upper);
    return ((double) ((upper << 32) + lower));
  }

  // Treats int as unsigned and converts to positive long
  public static long intToPosLong(int intNum)
  {
    long longNum = intNum;
    if (longNum < 0)
      longNum += Math.pow(2, 32);
    return (longNum);
  }

  /**
   * Return the current absolute Time stamp associated with packet.
   * 
   * @return
   */
  public double getAbsTimeStamp()
  {
    return absTimeStamp;
  }

  // Process message data received from netstorm and create all data arrays
  // which are required by client for diplay on GUI.
  public void processData(byte recvData[], data prevMsgData, GraphNames graphNames)
  {
    try
    {
      genRawData(recvData, prevMsgData);
      // loadErrCodes(); // Load Error codes if not already loaded
      if (opcode != END_PKT) // Only start and data packet is to be processed
      {
        genGraphData(0, processedDataLen - 1); // Need to do this for start to get test run, servers etc
        if (opcode == DATA_PKT) // Only data packet is to be processed
        {
          // genMinMaxData();
          // genAvgAndStdDevData(0, processedDataLen - 1); // process only graph index
          genGraphStats(0, processedDataLen - 1, true, true, true, graphNames); // process only graph index
        }
      }
      logMsgData();
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "processData", "", "", "Exception", e);
    }
  }

  // Process message data received from netstorm in byteBuffer and create all data arrays
  // which are required by client for diplay on GUI.
  public void processData(ByteBuffer bb, data prevMsgData, GraphNames graphNames)
  {
    try
    {
      genRawDataByByteBuffer(bb, prevMsgData);
      // loadErrCodes(); // Load Error codes if not already loaded
      if (opcode != END_PKT) // Only start and data packet is to be processed
      {
        genGraphData(0, processedDataLen - 1); // Need to do this for start to get test run, servers etc
        if (opcode == DATA_PKT) // Only data packet is to be processed
        {
          // genMinMaxData();
          // genAvgAndStdDevData(0, processedDataLen - 1); // process only graph index
          genGraphStats(0, processedDataLen - 1, true, true, true, graphNames); // process only graph index
        }
      }
      logMsgData();
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "processData", "", "", "Exception", e);
    }
  }

  // This will process data for all graph indexes
  // this is for show data in offline mode
  // to calculate graph stats(min, max, avg, and std-dev) for all index
  public void processDataForGraphs(byte recvData[], data prevMsgData, GraphNames graphNames)
  {
    try
    {
      // This must be called as we are not processing Hdr packet in ReportData.java.
      processHdrVer2(recvData);

      genRawData(recvData, prevMsgData);
      // loadErrCodes(); // Load Error codes if not already loaded
      if (opcode != END_PKT) // Only start and data packet is to be processed
      {
        genGraphData(0, processedDataLen - 1); // Need to do this for start to get test run, servers etc
        if (opcode == DATA_PKT) // Only data packet is to be processed
          genGraphStats(0, processedDataLen - 1, true, true, true, graphNames);
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "processDataForGraphs", "", "", "Exception", e);
    }
  }

  // This will process data for all graph indexes in array
  // Calculate all graph stats on the basis of stats flags
  // flagMinMax --> for calculate min & max value if flag is true
  // flagAvg --> for calculate avg value if flag is true
  // flagStdDev --> for calculate std-dev value if flag is true
  public void processDataForGraphs(byte recvData[], data prevMsgData, int[] arrIndex, boolean flagMinMax, boolean flagAvg, boolean flagStdDev, GraphNames graphNames)
  {
    try
    {
      // This must be called as we are not processing Hdr packet in ReportData.java.
      processHdrVer2(recvData);

      genRawData(recvData, prevMsgData);
      // loadErrCodes(); // Load Error codes if not already loaded
      if (opcode != END_PKT) // Only start and data packet is to be processed
      {
        for (int i = 0; i < arrIndex.length; i++)
        {
          // clculate start and end index on the basis of Graph data type to calculate min, max, count and sum of square
          int startIndex = arrIndex[i];
          int endIndex = arrIndex[i] + getEndIndexByGDFVersionAndDataType(majorVersionNum, arrDataType[arrDataTypeIndx[startIndex]]);

          genGraphData(startIndex, endIndex); // Need to do this for start to get test run, servers etc

          if (opcode == DATA_PKT) // Only data packet is to be processed
            genGraphStats(startIndex, startIndex, flagMinMax, flagAvg, flagStdDev, graphNames);
        }
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "processDataForGraphs", "", "", "Exception", e);
    }
  }

  // This function called from GraphsGenerateReport.java for misc graphs
  public void processDataForGraphs(byte recvData[], data prevMsgData, int startIndex, int endIndex)
  {
    try
    {
      // This must be called as we are not processing Hdr packet in ReportData.java.
      processHdrVer2(recvData);

      genRawData(recvData, prevMsgData);
      if (opcode != END_PKT) // Only start and data packet is to be processed
        genGraphData(startIndex, endIndex); // Need to do this for start to get test run, servers etc
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "processDataForGraphs", "", "", "Exception", e);
    }
  }

  /**
   * This Method is used to generate raw data for offline mode.
   * 
   * @param recvData
   * @param prevMsgData
   * @throws Exception
   */
  public void genRawData(byte recvData[], data prevMsgData) throws Exception
  {
    int i = 0;
    int index = 0;
    ByteBuffer bb;

    bb = ByteBuffer.wrap(recvData);

    bb = bb.order(ByteOrder.LITTLE_ENDIAN);

    // processHdr(recvData);

    for (i = 0; i < rawDataLen; i++)
    {
      rawData[i] = bb.getDouble(index);
      if (Double.isInfinite(rawData[i]) || Double.isNaN(rawData[i]))
        rawData[i] = 0.0;

      index = index + 8;
    }

    testRun = (int) rawData[TEST_RUN_INDEX];
    opcode = (int) rawData[OPCODE_INDEX];
    interval = (int) rawData[INTERVAL_INDEX];
    seqNum = (int) rawData[SEQ_NUM_INDEX];

    setTimeStampAndPartitionName(bb);

    // setTime(); // Do not move from here as interval may get extra few msecs
    genInterval(prevMsgData);
  }

  /**
   * this function called from NetstormListener.java
   * 
   * @param bb
   * @param prevMsgData
   * @throws Exception
   */
  public void genRawDataByByteBuffer(ByteBuffer bb, data prevMsgData) throws Exception
  {
    Log.debugLog(className, "genRawDataByByteBuffer", "", "", "method called");
    int i = 0;
    int index = 0;

    for (i = 0; i < rawDataLen; i++)
    {
      rawData[i] = bb.getDouble(index);
      if (Double.isInfinite(rawData[i]) || Double.isNaN(rawData[i]))
        rawData[i] = 0.0;

      index = index + 8;
    }

    // setTime(); // Do not move from here as interval may get extra few msecs
    genInterval(prevMsgData);
  }

  private void genGraphData(int startIndex, int endIndex) throws Exception
  {
    int graphDataIndex;
    int rawDataIndex = (int) (msgHdrLen + startIndex); // We only copy raw data after header
    long interval = this.interval; // Use local variable so that we do not update class variables
    long PSDivisor;

    if (interval == -1) // To take care of case where we lost start pkt
      interval = 10000;

    double PMMultiplier = (60 / (interval / 1000.0));
    PSDivisor = interval / 1000;

    for (graphDataIndex = (int) startIndex; graphDataIndex <= (int) endIndex;)
    {
      int formula = arrGraphFormula[graphDataIndex];
      graphData[graphDataIndex] = rawData[rawDataIndex++];

      if (graphData[graphDataIndex] != 4294967295D) // 4294967295D is -1, means data is not valid.
      {
        // Now apply strFormula to get processed data
        if (formula == GraphNameUtils.FORMULA_SEC) // SEC - Convert milli-sec to seconds
          graphData[graphDataIndex] = graphData[graphDataIndex] / 1000;

        else if (formula == GraphNameUtils.FORMULA_PM) // PM - Convert to Per Minute
          graphData[graphDataIndex] = graphData[graphDataIndex] * PMMultiplier;

        else if (formula == GraphNameUtils.FORMULA_PS) // PS - Convert to Per Seconds
          graphData[graphDataIndex] = graphData[graphDataIndex] / PSDivisor;

        else if (formula == GraphNameUtils.FORMULA_KBPS) // KBPS - Convert to kilo byte per second
          graphData[graphDataIndex] = (graphData[graphDataIndex]) / (1024);

        else if (formula == GraphNameUtils.FORMULA_DBH) // DBH - Convert to divide by 100
          graphData[graphDataIndex] = (graphData[graphDataIndex]) / (100);
      }
      // make it upto 3 digit decimal
      // graphData[graphDataIndex] = convTo3DigitDecimal(graphData[graphDataIndex]);
      graphDataIndex++;
    }
  }

  // this function calculate graph stats on the basis of flags
  // Std-Dev = square root of((sum of square - (sum * avg))/(N-1)) where N = number of sample
  private void genGraphStats(int startIndex, int endIndex, boolean flagMinMax, boolean flagAvg, boolean flagStdDev, GraphNames graphNames) throws Exception
  {
    int i = 0;
    double sum = 0;

    for (i = startIndex; i <= endIndex; i++)
    {
      sum = 0;
      if (minIndex[i] == -1) // These are for graph data element which are not for graph data e.g. Min/Max/Count fields in times graph. So no need to do averaging
        continue;

      // to calculate min & max, if flag is true
      if (flagMinMax)
      {
        if (graphData[minIndex[i]] >= 0) // 4294967295D is -1, means min is not valid. (Neeraj:1/12/05).
        {
          if ((graphData[minIndex[i]] < minData[i]))
            minData[i] = graphData[minIndex[i]];
        }
        if (graphData[maxIndex[i]] >= 0) // 4294967295D is -1, means min is not valid.
        {
          if ((graphData[maxIndex[i]] > maxData[i]))
            maxData[i] = graphData[maxIndex[i]];
        }
      }

      // if flagAvg flag is true then we calculate avg data
      // if std-dev flag is true then we also calculate avg data
      if (flagAvg || flagStdDev)
      {
        if (curCountIndex[i] != -1)
          curCount[i] = graphData[curCountIndex[i]];
        else
          curCount[i] = 1;

        // Should we check if value in graphData is 4294967295D which is -1, which means it is not valid. ??????
        // Neeraj - Issue: Check if this is overflow or not?
        if ((cumCount[i] + curCount[i]) != 0)
        {
          sum = ((avgData[i] * cumCount[i]) + (graphData[i] * curCount[i]));
          cumCount[i] += curCount[i];
          if (arrDataType[arrDataTypeIndx[i]] == GraphNameUtils.DATA_TYPE_CUMULATIVE)
          {
            if ((cumInitialValue[i] <= 0)) // for initial data value
            {
              if (cumInitialValue[i] == -1)
                cumInitialValue[i] = graphData[i];
              else if ((cumInitialValue[i] == 0) && (seqNum <= MAX_INITIAL_SEQ_COUNTER))
                cumInitialValue[i] = graphData[i];
            }

            int rptGroupID = graphNames.getGraphUniqueKeyDTOByGraphDataIndex(i).getGroupId();

            /*
             * Cumulative graphs can be of two types. Type1 - Start value is always 0 - All netstorm generated data is of this type - Customer monitor data may be of this type. We have no means of
             * finding it out
             * 
             * Type2 - Start value may have value before test is started - Customer monitor data may be of this type. We have no means of finding it out
             * 
             * In Type 1, we need to to show last value in the compare report if start time is 0. Else we show diff of last - first
             * 
             * In Type 2, we need to to always show diff of last value with first value in the compare report.
             * 
             * Since there is no way to find out whether data is type1 or type2, we can make following assumptions now
             * 
             * All netstorm generated graphs are type1 and all other are type2.
             */

            // check for Netstrom generated graphs
            if (rptGroupID < GraphNameUtils.CUSTOM_GROUP_START_ID)
              avgData[i] = graphData[i];
            else
              avgData[i] = graphData[i] - cumInitialValue[i];
          }
          else
            avgData[i] = sum / (cumCount[i]);

          // to calculate std-dev, if flag is true
          if (flagStdDev)
          {
            if (arrDataType[arrDataTypeIndx[i]] == GraphNameUtils.DATA_TYPE_TIMES_STD)
            {
              cumSumSqr[i] += graphData[i + 4]; // Add sum of square coming from NS to cummulative sum of squares
              // We are taking int cut by dividing by 1000000. Assumption is that for TimeStd, sum of sqr will be in mill-sec square
              if (cumCount[i] != 1)
                stdDev[i] = Math.sqrt(((cumSumSqr[i] / 1000000.0) - (sum * avgData[i])) / (cumCount[i] - 1));
            }
            else
            {
              // Square of current data is depend upon current count of current data
              // Here the cumSumSqr is in Sec because we calculate it with the help of graphData and graphData is also available in seconds
              cumSumSqr[i] += ((graphData[i] * graphData[i]) * curCount[i]); // calculate square of current data and add to cummulative sum of squares
              // Log.debugLog(className, "genGraphStats", "", "", "cumSumSqr[" + i + "]= " + cumSumSqr[i] + " ,graphData = " + graphData[i]);
              if (cumCount[i] != 1)
                stdDev[i] = Math.sqrt(((cumSumSqr[i]) - (sum * avgData[i])) / (cumCount[i] - 1));
            }
          }
        }
      }
    }
  }

  /*********************************************************************************************
   * Note : Prabhat --> (04/10/2009)(#build -> 3.2.6) This function called from Report data to calculate avg data to be show in legend In Report Generation(Legend) --> generate data through
   * overalldata on the basis of start & end seq Input --> arrRptData -> this is data of one selected report for whole TR arrCurCountReportData -> this is array of curCount of selected Report, for
   * whole TR (This is used only in case of TIMES & TIMES_STD) curCountIndexByGraphIndex -> curCountIndex of that graph startSequence -> based on selected time range, this is start index in arrRptData
   * endSequence -> based on selected time range, this is end index in arrRptData graphDataType -> This is data type of that selected graph
   * 
   * OutPut --> return avgData on the basis of time option
   ***********************************************************************************************/
  public double calcAndGetAvgDataBySeq(double[] arrRptData, double[] arrCurCountReportData, int curCountIndexByGraphIndex, int startSequence, int endSequence, int graphDataType, boolean overrideRptOptions)
  {
    try
    {
      double avgData = 0;
      double avgDataValueBySeq = 0;

      double cumCount = 0;
      double cumInitialValue = 0;

      int count = 0; // this is used for overwrite time option, because we read selected data in case of overwrite option

      // Sequence started from 1, so must -1 here
      for (int idx = (startSequence - 1); idx < endSequence; idx++)
      {
        double sum = 0;
        double graphData = 0;

        if (overrideRptOptions)
          graphData = arrRptData[count];
        else
          graphData = arrRptData[idx];

        double curCount = 0;

        if (curCountIndexByGraphIndex != -1)
        {
          if (overrideRptOptions)
            curCount = arrCurCountReportData[count];
          else
            curCount = arrCurCountReportData[idx];
        }
        else
        {
          curCount = 1;
        }

        if ((cumCount + curCount) != 0)
        {
          sum = ((avgData * cumCount) + (graphData * curCount));
          cumCount += curCount;

          if (graphDataType == GraphNameUtils.DATA_TYPE_CUMULATIVE)
          {
            if (cumInitialValue == 0) // for initial data value
              cumInitialValue = graphData;

            avgData = graphData - cumInitialValue;
          }
          else
          {
            avgData = sum / (cumCount);
          }
        }
        count++;
      }
      avgDataValueBySeq = convTo3DigitDecimal(avgData);

      return avgDataValueBySeq;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "calcAndGetAvgDataBySeq", "", "", "Exception - ", e);
      return -1;
    }
  }

  // check if index in the string in the format (0-6,9,10-20,50-51)
  static boolean isIndexInArr(String str, int index)
  {
    String str2;
    StringTokenizer tkn1, tkn2;
    int start, end;

    tkn1 = new StringTokenizer(str, ",");
    while (tkn1.hasMoreTokens())
    {
      str2 = tkn1.nextToken();
      tkn2 = new StringTokenizer(str2, "-");

      start = Short.parseShort(tkn2.nextToken());
      if (tkn2.hasMoreTokens())
        end = Short.parseShort(tkn2.nextToken());
      else
        end = start;
      if ((index >= start) && (index <= end))
        return true;
    }
    return false;
  }

  // convert string in the format (0-6,9,10-20,50-51) in the array
  static void strToArr(String str, int arr[])
  {
    String str2;
    int i = 0;
    int start, end, value;

    StringTokenizer tkn1, tkn2;

    tkn1 = new StringTokenizer(str, ",");
    while (tkn1.hasMoreTokens())
    {
      str2 = tkn1.nextToken();
      tkn2 = new StringTokenizer(str2, "-");

      start = Short.parseShort(tkn2.nextToken());
      if (tkn2.hasMoreTokens())
        end = Short.parseShort(tkn2.nextToken());
      else
        end = start;

      for (value = start; value <= end; value++)
      {
        arr[i] = value;
        i++;
      }
    }
  }

  public static String arrToString(String arrString[])
  {
    String strTmp = arrString[0];
    for (int recNum = 1; recNum < arrString.length; recNum++)
      strTmp = strTmp + "," + arrString[recNum];
    return (strTmp);
  }

  // Convert record from array to string through saperator
  public static String intArrayToStr(int[] tempRecord, String separator)
  {
    Log.debugLog(className, "intArrayToStr", "", "", "Method called");
    String tempStr = null;
    try
    {
      for (int i = 0; i < tempRecord.length; i++)
      {
        if (i == 0)
          tempStr = "" + tempRecord[i];
        else
          tempStr = tempStr + separator + tempRecord[i];
      }

      return tempStr;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "intArrayToStr", "", "", "Exception in change from int array to string -" + e);
      return null;
    }
  }

  public void logMsgData()
  {
    int i;
    String tmpBuf = "";
    String strLogIndex = Config.getValue("msgLogIndex");

    if (strLogIndex == "")
      return;

    Log.msgLog(className, "logMsgData", "", "", "hdrData - " + toStringHdr());
    Log.msgLog(className, "logMsgData", "", "", "index rawData graphData minData maxData curCount cumCount avgData");
    // Neeraj - Need to take care of rawData is 4 less
    for (i = 0; i < processedDataLen; i++)
    {
      String min, max;

      if (minData[i] == Double.MAX_VALUE)
      {
        min = "NA";
        max = "NA";
      }
      else
      {
        min = "" + minData[i];
        max = "" + maxData[i];
      }
      if (isIndexInArr(strLogIndex, i) == false)
        continue;
      if (i < rawDataLen)
        tmpBuf = "[" + i + "]" + " " + rawData[i] + " " + graphData[i] + " " + min + " " + max + " " + curCount[i] + " " + cumCount[i] + " " + avgData[i];
      else
        tmpBuf = "[" + i + "]" + " " + "--" + " " + graphData[i] + " " + min + " " + max + " " + curCount[i] + " " + cumCount[i] + " " + avgData[i];
      Log.msgLog(className, "logMsgData", "", "", tmpBuf);
    }
  }

  public void logData1()
  {
    // Log.debugLog(className, "logData1", "", "", "hdrData - " + toStringHdr());
    // Log.debugLog(className, "logData1", "", "", "rawData - " + toString(rawData));
    // Log.debugLog(className, "logData1", "", "", "graphData - " + toString(graphData));
    // Log.debugLog(className, "logData1", "", "", "minData - " + toString(minData));
    // Log.debugLog(className, "logData1", "", "", "maxData - " + toString(maxData));
    // Log.debugLog(className, "logData1", "", "", "avgData - " + toString(avgData));
  }

  public String toString(double arrData[])
  {
    int i;
    String tmpBuf = "";
    for (i = 0; i < arrData.length; i++)
    {
      long tmp = (long) arrData[i];
      if ((arrData[i] - tmp) != 0) // means arrData is in decimals
        tmpBuf = tmpBuf + "," + arrData[i];
      else
        tmpBuf = tmpBuf + "," + tmp;
    }
    return (tmpBuf);
  }

  /**
   * Showing Row Data Array.
   */
  public String toString()
  {
    return (toString(rawData));
  }

  public String toStringHdr()
  {
    String tmpBuf = "Opcode=" + opcode + ",Interval=" + interval + ",SeqNum=" + seqNum;
    return (tmpBuf);
  }

  private int getEndIndexByGDFVersionAndDataType(int majorVersionNum, int dataTypeNum)
  {
    if (dataTypeNum == GraphNameUtils.DATA_TYPE_TIMES)
      return 3;
    if (dataTypeNum == GraphNameUtils.DATA_TYPE_TIMES_STD)
      return 4;

    return 0;
  }
  
  /**
   * Method is used to return size fo msgData.
   * @return
   */
  public long getMsgDataLength()
  {
    return msgDataLen;  
  }
  
  /**
   * Method is used to get partition file version
   * @return
   */
  public int getPartitionFileVersion() 
  {
    return partitionFileVersion;
  }

  /**
   * 
   * @param partitionFileVersion
   */
  public void setPartitionFileVersion(int partitionFileVersion) 
  {
    this.partitionFileVersion = partitionFileVersion;
  }

  /**
   * Cloning data object.
   */
  public data clone()
  {
    try
    {
      return (data) super.clone();
    }
    catch (CloneNotSupportedException e)
    {
      Log.errorLog(className, "clone", "", "", "Error in Cloning msgData object" + e);
      return null;
    }
  }
}

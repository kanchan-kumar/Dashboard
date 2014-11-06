/*--------------------------------------------------------------------
  @Name    : ReportData.java
  @Author  : Abhishek/Neeraj
  @Purpose : Bean for getting data from Report Raw files.
             Currently following files are support by this bean
              1. rtgMessage.dat
              2. global.dat
  @Modification History:
    02/05/07:Abhishek/Neeraj:1.4.2 - Initial Version

----------------------------------------------------------------------*/

package pac1.Bean;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

import org.jfree.chart.util.RelativeDateFormat;

import pac1.Bean.GraphName.*;

public class ReportData implements java.io.Serializable
{
  String className = "ReportData";
  public long pktSize = 0;
  public long interval = -1; // default
  public long totalPkts = 0;
  public double arrSeqNum[] = null;
  public double arrSeqNumAll[] = null;
  public double avgArrDataVal[] = null;
  public double avgArrSeqNum[] = null;
  public double arrDataVal[] = null;
  public int processedDataLen = 0;

  public long startSeq = 0; // Seq number of first packet to be shown in the graph
  public long endSeq = 0; // Seq number of last packet to be shown in the graph
  public long maxSeq; // Max Seq number is the seq number of last data packet in the file

  public final int heigth = 400; // Height of the graph image (same for both types)
  final int normalGraphWidth = 900; // For normal graphs
  public int width = 0; // Actual width based on graph type and max allowed due to memory
  final int maxPktsForAutoGranularity = 40; // This is for Normal graph and Auto granularity
  // Max graph width for Long graph can be increase upto 35000 for better view
  final int maxLongGraphWidth = 20000; // For Long graphs
  final int pixelsPerDot = 10;

  public data prevMsgData = null;
  public data msgData = null;
  public GraphNames graphNames = null;

  FileInputStream fis = null;
  DataInputStream dis = null;

  double phaseTimes[] = null;
  // These are used to set start and end x axis time stamp in graph
  public long startTimeInmilli = 0;
  public long endTimeInmilli = 0;
  public long timeInMilli = 0;
  public int countAvg = 0;
  public int numTestRun = 0;

  public double[][] arrAvgDataValuesAll = null; // avg data values of all graphs(used in compare report & analysis GUI)
  public double[][] arrStdDevDataValuesAll = null; // std-dev data values of all graphs(used in compare report & analysis GUI)
  public double[][] arrCurCountAll = null; // avg data values of all graphs(used in report generation)

  // Static Constants for msgDataRequired Flag.
  public static int MSG_DATA_NOT_REQUIRED = 0;
  public static int MSG_DATA_REQUIRED = 1;
  public static int MSG_DATA_STORED_IN_SESSION = 2;
  private String controllerTRNumber = "NA";

  // This is temporary flag tells only to create raw data array in msgData object.
  private boolean genOnlyRawData = false;
  private String generatorName;

  // Tells that the reporting mode is NDE.
  private boolean nDE_Continuous_Mode = false;

  // contains the partition type of test run.
  private int testRun_Partition_Type = 0;

  // Contains information of derived data.
  private DerivedDataDTO derivedDataDTO = null;

  private int debugLevel = 0;

  public boolean reporting = false;

  public void setDebugLevel(int debugLevel)
  {
    this.debugLevel = debugLevel;
  }

  public void setDebugLevelFromConfig()
  {
    try
    {
      String debugLevel = Config.getValue("netstorm.execution.debugLevel");
      this.debugLevel = Integer.parseInt(debugLevel);
    }
    catch (Exception e)
    {
      this.debugLevel = 0;
    }
  }

  public ReportData(int numTestRun)
  {
    this.numTestRun = numTestRun;

    setDebugLevelFromConfig();

    // Sets the keyword for NDE continuous mode by reading config values.
    setNDEConitnuousModeByConfig();
  }

  public ReportData(int numTestRun, boolean genOnlyRawData)
  {
    this.numTestRun = numTestRun;
    this.genOnlyRawData = genOnlyRawData;

    setDebugLevelFromConfig();

    // Sets the keyword for NDE continuous mode by reading config values.
    setNDEConitnuousModeByConfig();
  }

  /**
   * Activate Reporting for NDE Continuous mode.
   * 
   * @param nDE_Continuous_Mode
   */
  public void setnDE_Continuous_Mode(boolean nDE_Continuous_Mode)
  {
    this.nDE_Continuous_Mode = nDE_Continuous_Mode;
  }

  /**
   * Method used to check weather or not NDE Continuous Mode is enabled.
   * 
   * @return
   */
  public boolean isnDE_Continuous_Mode()
  {
    return nDE_Continuous_Mode;
  }

  /**
   * Sets the Derived Data DTO object to process Derived Graphs.
   * 
   * @param derivedDataDTO
   */
  public void setDerivedDataDTO(DerivedDataDTO derivedDataDTO)
  {
    this.derivedDataDTO = derivedDataDTO;
  }

  /**
   * Checking the settings of NDE Continuous Monitoring.
   */
  public void setNDEConitnuousModeByConfig()
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "setNDEConitnuousModeByConfig", "", "", "Method Called.");

      /* Checking for NDE continuous Mode. */
      nDE_Continuous_Mode = TestRunDataType.isNDE_Continuous_Mode();

      if (controllerTRNumber.equals("NA"))
        testRun_Partition_Type = TestRunDataType.getTestRunPartitionType(numTestRun);
      else
        testRun_Partition_Type = TestRunDataType.getTestRunPartitionType(Integer.parseInt(controllerTRNumber));
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "setNDEConitnuousModeByConfig", "", "", "Exception - ", e);
    }
  }

  /**
   * Getting TestRunDataType by checking the config.ini netstorm.execution.defaultDataView keyword value. </br> If value invalid or keyword is not available then taking default as Last 4 hour in continuous mode. </br> In NS/NDE without Continuous Mode default key is whole scenario.
   * 
   * @return
   */
  public TestRunDataType getTestRunDataTypeByConfigSetting()
  {
    if (debugLevel > 0)
      Log.debugLogAlways(className, "getTestRunDataTypeByConfigSetting", "", "", "Method Called.");

    String dataView = TestRunDataType.getDataTypeValueFromConfig();

    if (!nDE_Continuous_Mode && (dataView == null || dataView.trim().equals("")))
      return (new TestRunDataType(TestRunDataType.WHOLE_SCENARIO_DATA, -1, 0, "NA", "NA", false, false));

    return TestRunDataType.getTestRunDataTypeObjByLastNKey(dataView);
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

  /**
   * ReportData constructor for Generator TR.
   * 
   * @param numTestRun
   * @param generatorName
   * @param controllerTRNumber
   */
  public ReportData(int numTestRun, String generatorName, String controllerTRNumber)
  {

    Log.debugLogAlways(className, "ReportData:Constructor", "", "", "Method Called. numTestRun = " + numTestRun + " generatorName = " + generatorName + " controllerTRNumber = " + controllerTRNumber);

    this.numTestRun = numTestRun;
    this.generatorName = generatorName;

    if (this.generatorName != null && this.generatorName.equals("Controller"))
      this.generatorName = null;

    this.controllerTRNumber = controllerTRNumber;

    setDebugLevelFromConfig();

    // Sets the keyword for NDE continuous mode by reading config values.
    setNDEConitnuousModeByConfig();
  }

  // Add function for returning absolute path starting with workpath
  public String getTestRunDirPath()
  {
    if (debugLevel > 0)
      Log.debugLogAlways(className, "ReportData:getTestRunDirPath", "", "", "controllerTRNumber = " + controllerTRNumber);

    if (controllerTRNumber.equals("NA"))
      return (Config.getWorkPath() + "/webapps/logs/TR" + numTestRun);
    else
      return (Config.getWorkPath() + "/webapps/logs/TR" + controllerTRNumber);
  }

  // Add function for returning absolute path starting with testRun dir's message file
  public String getRTGFileNameWithPath()
  {
    if (debugLevel > 0)
      Log.debugLogAlways(className, "ReportData:getRTGFileNameWithPath", "", "", "controllerTRNumber = " + controllerTRNumber + " generatorName = " + generatorName + " numTestRun = " + numTestRun);

    if (controllerTRNumber.equals("NA") || generatorName == null || generatorName.length() == 0)
      return (Config.getWorkPath() + "/webapps/logs/TR" + numTestRun + "/rtgMessage.dat");
    else
      return (Config.getWorkPath() + "/webapps/logs/TR" + controllerTRNumber + "/NetCloud/" + generatorName.trim() + "/TR" + this.numTestRun + "/rtgMessage.dat");
  }

  /**
   * This Method is Used for reading raw data Pkts from rtgMessage.dat file, while creating time based data.
   * 
   * @param msgData
   * @param prevMsgData
   * @param opcode
   * @param seqNum
   * @return
   */
  private data readRawDataPkt(data msgData, data prevMsgData, int opcode, int seqNum)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "readRawDataPkt", "", "", "Method called. opcode is  " + opcode + ", SeqNum is " + seqNum + ", Interval is " + interval);

      byte byteBuf[] = new byte[(int) pktSize];
      int bytesRead = dis.read(byteBuf);

      // TODO - Add loop to retry as read may be partial (See NetstormListerner.java)
      if (bytesRead != pktSize)
      {
        Log.errorLog(className, "readRawDataPkt", "", "", "Complete packet not read for Test Run " + numTestRun + ". Ignored. Opcode = " + opcode + ", SeqNum = " + seqNum + ". pktSize = " + pktSize + ", bytesRead = " + bytesRead);
        return null;
      }

      // Processing Raw Data.
      msgData.genRawData(byteBuf, prevMsgData);

      if ((msgData.getOpcode() != opcode) || (msgData.getSeqNum() != seqNum))
        Log.errorLog(className, "readRawDataPkt", "", "", "rgtMessage file may not be correct. Opcode or SeqNum not matching. Ignored");

      interval = msgData.getInterval();
      return (msgData);
    }
    catch (Exception e)
    {
      Log.errorLog(className, "readRawDataPkt", "", "", "Exception in reading Raw data pkt - " + e);
      return null;
    }
  }

  // When array of graph's index need to process for graph data
  // This method is NOT called for END_PKT
  private data readOnePkt(data msgData, data prevMsgData, int opcode, int seqNum, int[] arrIndexes, boolean minMaxFlag, boolean avgFlag, boolean stdDevFlag)
  {
    try
    {
      byte byteBuf[] = new byte[(int) pktSize];
      int bytesRead = dis.read(byteBuf);

      // TODO - Add loop to retry as read may be partial (See NetstormListerner.java)
      if (bytesRead != pktSize)
      {
        Log.errorLog(className, "readOnePkt", "", "", "Complete packet not read for Test Run " + numTestRun + ". Ignored. Opcode = " + opcode + ", SeqNum = " + seqNum + ". pktSize = " + pktSize + ", bytesRead = " + bytesRead);
        return null;
      }

      if (arrIndexes != null)
      {
        msgData.processDataForGraphs(byteBuf, prevMsgData, arrIndexes, minMaxFlag, avgFlag, stdDevFlag, graphNames);
      }
      else
      {
        // to get data for all indexes. added for offline mode
        msgData.processDataForGraphs(byteBuf, prevMsgData, graphNames);
      }

      if ((msgData.getOpcode() != opcode) || (msgData.getSeqNum() != seqNum))
      {
        Log.errorLog(className, "readOnePkt", "", "", "rgtMessage file may not be correct for Test Run " + numTestRun + ". Opcode or SeqNum not matching. Ignored. Opcode = (" + opcode + "," + msgData.getOpcode() + "), SeqNum = (" + seqNum + "," + msgData.getSeqNum() + ")");
        return null;
      }

      interval = msgData.getInterval();
      return (msgData);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "readOnePkt", "", "", "Exception in reading pkt - " + e, e);
      return null;
    }
  }

  public boolean openRTGMsgFile()
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "openRTGMsgFile", "", "", "Method Called. generatorName = " + generatorName);

      if (generatorName == null)
        return openRTGMsgFile(new GraphNames(numTestRun));
      else
        return openRTGMsgFile(new GraphNames(numTestRun, null, null, controllerTRNumber, generatorName, "", false));
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "openRTGMsgFile", "", "", "Exception - ", ex);
      return false;
    }
  }

  /**
   * This will open RTG message file and initialize objects and variables related to the test run.
   * 
   * @param grpNameObj
   * @return
   */
  public boolean openRTGMsgFile(GraphNames grpNameObj)
  {
    String rtgFile = getRTGFileNameWithPath();
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "openRTGMsgFile", "", "", "Method called. Test Run is  " + numTestRun + ", rtgFile = " + rtgFile);

      File rtgFileObj = new File(rtgFile);
      if (!rtgFileObj.exists()) // file does not exist
      {
        Log.errorLog(className, "readReport", "", "", "File " + rtgFile + " does not exist.");
        return false;
      }

      graphNames = grpNameObj;

      prevMsgData = new data();
      msgData = new data();

      msgData.initForNewTestRun(graphNames);

      pktSize = graphNames.getSizeOfMsgData();
      processedDataLen = msgData.getProcessedDataLen();

      fis = new FileInputStream(rtgFile);
      dis = new DataInputStream(fis);
      long fileSize = 0;

      /*
       * Ravi - When File size is too big means more than 2147483647 bytes then fis.available()
       * 
       * function would not work. So we need to find out the total number of bytes.
       * 
       * We are using ls -l <fileName>
       * 
       * Note: ls -l command will not work if machine is windows.
       */
      try
      {
        String osname = System.getProperty("os.name").trim().toLowerCase();
        if (!osname.startsWith("win"))
        {
          CmdExec cmdExec = new CmdExec();
          String command = "ls";
          String args = "-l " + rtgFile;
          Vector result = cmdExec.getResultByCommand(command, args, CmdExec.SYSTEM_CMD, "netstorm", "root");
          if (result != null)
          {
            String tempRecord = result.get(0).toString();
            String[] fileSizeTemp = tempRecord.split(" ");
            fileSize = Long.parseLong(fileSizeTemp[4]);
          }
        }
        else
        {
          // for windows machine
          fileSize = fis.available();
        }
      }
      catch (Exception ex)
      {
        Log.errorLog(className, "openRTGMsgFile", "", "", "Error in getting file size - " + fileSize + ". File name is " + rtgFile);
      }

      if (fileSize < pktSize) // Make sure at least full packet is in the file
      {
        Log.errorLog(className, "openRTGMsgFile", "", "", "rgtMessage file is corrupted. Size < one packet size. File name is " + rtgFile);
        System.out.println("rgtMessage file is corrupted. Size < one packet size. File name is " + rtgFile);
        return false;
      }

      totalPkts = fileSize / pktSize;
      maxSeq = totalPkts - 2; // As start and End pkts are not data packets (Valid only if test is over. Otherwise end pkt is not yet written in the file
      if (debugLevel > 0)
        Log.debugLogAlways(className, "openRTGMsgFile", "", "", "File opened for TestRun " + numTestRun + ". Total File Size = " + fileSize + ", Total Pkts = " + totalPkts + ", MaxSeq = " + maxSeq + ", Pkt Size = " + pktSize + ", processedDataLen = " + processedDataLen);

      return true;
    }
    catch (FileNotFoundException e)
    {
      Log.errorLog(className, "openRTGMsgFile", "", "", "No Data Available (rtgMessage.dat not found). File name is " + rtgFile);
      return false;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "openRTGMsgFile", "", "", "Exception while reading data file - " + e);
      return false;
    }
  }

  public void closeRTGMsgFile()
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "closeRTGMsgFile", "", "", "Method called");

      if (dis == null)
      {
        Log.debugLogAlways(className, "closeRTGMsgFile", "", "", "dis is null, file may already close.");
        return;
      }

      dis.close();
      fis.close();
      dis = null;
      fis = null;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "closeRTGMsgFile", "", "", "Exception while closing data file " + e);
    }
  }

  // This will get all data from raw message data file. Called from RptGenUsingRtpl and CmpRptTestRunData
  // This function call from rtgMsgViewer
  public void setStartSeq(long startSeq)
  {
    this.startSeq = startSeq;
  }

  // This function call from rtgMsgViewer
  public void setEndSeq(long endSeq)
  {
    this.endSeq = endSeq;
  }

  // handle the case of min, max , avg and std-dev calculation at the time of read one pkt
  public double[][] getAllGraphsDataFromRTGFile(int[] arrGraphIndex, String time, String startTime, String endTime)
  {
    return (getAllGraphsDataFromRTGFile(arrGraphIndex, time, startTime, endTime, false, false, false, false));
  }

  public double[][] getAllGraphsDataFromRTGFile(int[] arrGraphIndex, String time, String startTime, String endTime, boolean calAllIndex, boolean minMaxFlag, boolean avgFlag, boolean stdDevFlag)
  {
    try
    {
      int numGraphIndex = arrGraphIndex.length;

      if (debugLevel > 0)
        Log.debugLogAlways(className, "getAllGraphsDataFromRTGFile", "", "", "Method called. Test Run is  " + this.numTestRun + ", GraphIndexes = " + rptUtilsBean.intArrayToList(arrGraphIndex) + "arrGraphIndex.length = " + arrGraphIndex.length + ", MinMax Flag = " + minMaxFlag + ", Avg Flag = " + avgFlag + ", Std-dev flag = " + stdDevFlag + ", numGraphIndex = " + numGraphIndex);

      // Read Start Packet
      if (calAllIndex) // calculate min, max, avg & std-dev
        readOnePkt(msgData, prevMsgData, data.START_PKT, 0, null, minMaxFlag, avgFlag, stdDevFlag);
      else
        readOnePkt(msgData, prevMsgData, data.START_PKT, 0, arrGraphIndex, minMaxFlag, avgFlag, stdDevFlag);

      // calPktRange() must be called after first packet is read.
      if (!time.equals(""))
      {
        boolean success = calPktRange(time, startTime, endTime);
        if (!success)
          return null;
      }

      double arrDataValuesAll[][] = new double[numGraphIndex][(int) (endSeq - startSeq + 1)];
      arrSeqNum = new double[(int) (endSeq - startSeq + 1)];

      arrAvgDataValuesAll = new double[numGraphIndex][(int) (endSeq - startSeq + 1)];
      arrStdDevDataValuesAll = new double[numGraphIndex][(int) (endSeq - startSeq + 1)];
      arrCurCountAll = new double[numGraphIndex][(int) (endSeq - startSeq + 1)];

      int numberOfBytesToSkip = (int) (pktSize * (startSeq - 1));
      int bytesRead = dis.skipBytes(numberOfBytesToSkip);
      if (debugLevel > 0)
        Log.debugLogAlways(className, "getAllGraphsDataFromRTGFile", "", "", "Total Skip Bytes = " + bytesRead);

      // Now read pkt we need (from startSeq to endSeq) as we do not need packets after endSeq
      // Also Do not read last Packet which is END_PKT (this is already taken care on endSeq
      // This sequences are always start with 1 if we read data packet through template
      for (int seqNum = (int) startSeq; seqNum <= endSeq; seqNum++)
      {
        if (calAllIndex) // calculate min, max, avg & std-dev
        {
          if (readOnePkt(msgData, prevMsgData, data.DATA_PKT, seqNum, null, minMaxFlag, avgFlag, stdDevFlag) == null)
            continue;
        }
        else
        {
          if (readOnePkt(msgData, prevMsgData, data.DATA_PKT, seqNum, arrGraphIndex, minMaxFlag, avgFlag, stdDevFlag) == null)
            continue;
        }

        double arrGraphData[] = msgData.getGraphData();
        // this is added to calculate avg data, and std-dev by sequence
        double arrAvgData[] = msgData.getAvgData();
        double arrStdDevData[] = msgData.getStdDev();
        double arrCurCount[] = msgData.getCurCount();

        for (int i = 0, n = arrGraphIndex.length; i < n; i++)
        {
          int index = seqNum - (int) startSeq;
          arrDataValuesAll[i][index] = arrGraphData[arrGraphIndex[i]];
          arrAvgDataValuesAll[i][index] = arrAvgData[arrGraphIndex[i]];
          arrStdDevDataValuesAll[i][index] = arrStdDevData[arrGraphIndex[i]];
          arrCurCountAll[i][index] = arrCurCount[arrGraphIndex[i]];
        }
        arrSeqNum[seqNum - (int) startSeq] = msgData.getSeqNum();
      }

      closeRTGMsgFile();
      return arrDataValuesAll;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "getAllGraphsDataFromRTGFile", "", "", "Exception while reading data file - " + e);
      return null;
    }
  }

  /**
   * Method is used to get total Duration of Test Run.
   * @return
   */
  public long getTestRunDuration()
  {
    try
    {
      Log.debugLogAlways(className, "getTestRunDuration", "", "", "Method Called. testRun_Partition_Type = "+testRun_Partition_Type);
      
      if(testRun_Partition_Type > 0)
      {
	SessionReportData sessionReportData = null;
        if(controllerTRNumber.equals("NA"))
	   sessionReportData = new SessionReportData(numTestRun);
        else
          sessionReportData = new SessionReportData(Integer.parseInt(controllerTRNumber) , generatorName , String.valueOf(numTestRun));
        
        PartitionInfoUtils partitioInfoUtils = new PartitionInfoUtils();
        return partitioInfoUtils.getTestRunDurationInPartitonMode(sessionReportData);
      }
      else
	return new PartitionInfoUtils().getPartitionDurationFromRTGFile(getTestRunDirPath());
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return 0L;
    }
  }

  /**
   * Wrapper/Overloaded method for getting Time Based Data form Reporting.
   * 
   * @param hmGraphInfoDTOList
   * @param time
   * @param startTime
   * @param endTime
   * @param calAllIndex
   * @param isAveragingEnabled
   * @return
   */
  public TimeBasedTestRunData getTimeBasedDataFromRTGFile(LinkedHashMap<Integer, GraphUniqueKeyDTO> hmGraphInfoDTOList, String time, String startTime, String endTime, boolean calAllIndex, boolean isAveragingEnabled)
  {
    return getTimeBasedDataFromRTGFile(hmGraphInfoDTOList, time, startTime, endTime, calAllIndex, isAveragingEnabled, null);
  }

  /**
   * Method is used to Generate Graph Data For Selected/All Graphs.
   * 
   * @param hmGraphInfoDTOList
   * @param time
   * @param startTime
   * @param endTime
   * @param calAllIndex
   * @param isAveragingEnabled
   * @param testRunDataTypeObj
   * @return
   */
  public TimeBasedTestRunData getTimeBasedDataFromRTGFile(LinkedHashMap<Integer, GraphUniqueKeyDTO> hmGraphInfoDTOList, String time, String startTime, String endTime, boolean calAllIndex, boolean isAveragingEnabled, TestRunDataType testRunDataTypeObj)
  {
    try
    {
      Log.debugLogAlways(className, "getTimeBasedDataFromRTGFile", "", "", "Method called. Test Run is  " + this.numTestRun + ", time = " + time + ", startTime = " + startTime + ", endTime = " + endTime + ", calAllIndex = " + calAllIndex + ", Total Graphs = " + hmGraphInfoDTOList.size() + ", isAveragingEnabled = " + isAveragingEnabled + ", testRunDataTypeObj = " + testRunDataTypeObj);

      /* Enable the reporting mode. */
      reporting = true;

      /* Getting Granularity value */
      int granularity = -1;

      /* Here we need to get GraphNames object */
      if (graphNames == null)
        createGraphNamesObj();

      /* Interval of test Run. */
      // TODO - How to get interval value.
      int interval = graphNames.getInterval();

      /* Need to check if averaging is not required. */
      if (!isAveragingEnabled)
        granularity = interval / 1000;

      /* Checking Time inputs for preventing error. */
      startTime = (startTime.trim().equals("")) ? "NA" : startTime;
      endTime = (endTime.trim().equals("")) ? "NA" : endTime;

      /* Checking if Data is Requested For Selected Time Range. */
      if ((!time.equals("") && !time.trim().equals("NA") && !time.trim().equals("Total Run")) || (testRunDataTypeObj != null))
      {
        if (testRun_Partition_Type > 0)
        {
          PartitionInfoUtils partitionInfoUtilsObj = new PartitionInfoUtils();

          if (controllerTRNumber.equals("NA"))
          {
            startTime = partitionInfoUtilsObj.changeElapsedFormatTimeToAbsoluteTimeStamp(startTime, numTestRun) + "";
            endTime = partitionInfoUtilsObj.changeElapsedFormatTimeToAbsoluteTimeStamp(endTime, numTestRun) + "";
          }
          else
          {
            startTime = partitionInfoUtilsObj.changeElapsedFormatTimeToAbsoluteTimeStamp(startTime, Integer.parseInt(controllerTRNumber)) + "";
            endTime = partitionInfoUtilsObj.changeElapsedFormatTimeToAbsoluteTimeStamp(endTime, Integer.parseInt(controllerTRNumber)) + "";
          }

          Log.debugLogAlways(className, "getTimeBasedDataFromRTGFile", "", "", "startTime = " + startTime + ", endTime = " + endTime);
        }

        /* If Test Run Data Type is not available then created with Specified Time key. */
        if (testRunDataTypeObj == null)
        {
          Log.debugLogAlways(className, "getTimeBasedDataFromRTGFile", "", "", "Specified Time Key is created.");
          testRunDataTypeObj = new TestRunDataType(TestRunDataType.SPECIFIED_PHASE_OR_TIME, granularity, -1, startTime, endTime, false, false);
        }
      }
      else
      {
        Log.debugLogAlways(className, "getTimeBasedDataFromRTGFile", "", "", "Whole Scenario Key is created.");
        testRunDataTypeObj = new TestRunDataType(TestRunDataType.WHOLE_SCENARIO_DATA, granularity, -1, "NA", "NA", false, false);
      }

      /* Graph Number array, if it is null, Time Based data is created for all graphs. */
      GraphUniqueKeyDTO arrGraphInfoDTO[] = null;

      /* Here we check if request to process selected Graphs. */
      if (!calAllIndex)
      {
        arrGraphInfoDTO = new GraphUniqueKeyDTO[hmGraphInfoDTOList.size()];
        Iterator<Entry<Integer, GraphUniqueKeyDTO>> graphIterator = hmGraphInfoDTOList.entrySet().iterator();

        int k = 0;

        while (graphIterator.hasNext())
        {
          arrGraphInfoDTO[k] = graphIterator.next().getValue();
          k++;
        }
      }

      /* Getting Time Based Data object. */
      TimeBasedTestRunData timeBasedTestRunDataObj = getReqTimeBasedTestRunData(!calAllIndex, startTime, endTime, "NA", granularity, 1, true, interval, 9, "netstorm", arrGraphInfoDTO, testRunDataTypeObj, null);

      /* Checking for error case. */
      if (timeBasedTestRunDataObj == null)
      {
        Log.errorLog(className, "getTimeBasedDataFromRTGFile", "", "", "Getting Time Based Data object null. Please see error logs.");
        return null;
      }

      /* Now Getting Graph Data For Graphs. */
      return timeBasedTestRunDataObj;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getTimeBasedDataFromRTGFile", "", "", "Exception - ", e);
      return null;
    }
  }

  /**
   * Check if Test Run Data File(rtgMessage.dat) is Available on test run directory or not.
   * 
   * @param fileName
   * @param testRun
   * @return
   */
  public boolean isGraphRawDataFileAvailable(String fileName, String testRun)
  {
    try
    {
      int numTestRun = Integer.parseInt(testRun.trim());

      String curPartitionName = "";

      if (testRun_Partition_Type > 0)
      {
        /* Getting the Current Partition From Test Run Directory. */
        curPartitionName = (new PartitionInfoUtils()).getCurrentPartitionNameByTestRunNumber(numTestRun);

        if (curPartitionName == null)
        {
          Log.errorLog(className, "isGraphRawDataFileAvailable", "", "", "Current Partition Information not available.");
          return false;
        }
      }

      /* Creating the Path of RTG File. */
      String rtgFilePath = Config.getWorkPath() + "/webapps/logs/TR" + numTestRun + "/" + curPartitionName + "/" + fileName;

      File gpFile = new File(rtgFilePath);
      if (!gpFile.exists())
      {
        Log.errorLog(className, "isGraphRawDataFileAvailable", "", "", "Graph Raw Data File (" + fileName + ")  not found for the test run " + testRun);
        return false;
      }
      else
      {
        // This will check the file size when exist, if it is of size 0 then return false for no data available for report generation
        long size = gpFile.length();
        if (size == 0)
        {
          Log.errorLog(className, "isGraphRawDataFileAvailable", "", "", "Existing Graph Raw Data File (" + fileName + ") has no data to generate the report.");
          return false;
        }
      }
      return true;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "isGraphRawDataFileAvailable", "", "", "Exception - " + e);
      return false;
    }
  }

  /**
   * Method is used to create Graph Names object.
   * 
   * @return
   */
  public GraphNames createGraphNamesObj()
  {
    try
    {
      Log.debugLogAlways(className, "createGraphNamesObj", "", "", "Partition Mode = " + testRun_Partition_Type);

      if (testRun_Partition_Type > 0)
      {
        String activePartiionName = "";
        String gdfVersion = "";
        PartitionInfoUtils partitionInfoUtils = new PartitionInfoUtils();
        String partitionFilePath = null;
        
        if (controllerTRNumber.equals("NA"))
        {
          activePartiionName = partitionInfoUtils.getCurrentPartitionNameByTestRunNumber(numTestRun);
          partitionFilePath = partitionInfoUtils.getPartitionDirPath(activePartiionName, numTestRun, generatorName, -1);
        }
        else
        {
          activePartiionName = partitionInfoUtils.getCurrentPartitionNameByTestRunNumber(Integer.parseInt(controllerTRNumber));
          partitionFilePath = partitionInfoUtils.getPartitionDirPath(activePartiionName, numTestRun, generatorName, Integer.parseInt(controllerTRNumber));
        }
        
        if (activePartiionName == null)
        {
          Log.debugLogAlways(className, "createGraphNamesObj", "", "", "Active Partition Name not found.");
          return null;
        }
        
        SortedSet<Integer> arrAvailableGDFVersionList = partitionInfoUtils.getPartitionAvailableGDFVersion(partitionFilePath);
        Log.debugLogAlways(className, "createGraphNamesObj", "", "", "activePartiionName = " + activePartiionName + ", partitionFilePath = " + partitionFilePath + ", arrAvailableGDFVersionList = " + arrAvailableGDFVersionList);
        
        if(arrAvailableGDFVersionList != null && arrAvailableGDFVersionList.size() > 0)
        {
          gdfVersion = arrAvailableGDFVersionList.last() == 0 ? "" : arrAvailableGDFVersionList.last() + "";
          Log.debugLogAlways(className, "createGraphNamesObj", "", "", "got gdfVersion = " + gdfVersion);
        }
        
        graphNames = new GraphNames(numTestRun, null, null, controllerTRNumber, generatorName, activePartiionName, gdfVersion, false);
      }
      else
      {
        if (controllerTRNumber.equals("NA"))
          graphNames = new GraphNames(numTestRun);
        else
          graphNames = new GraphNames(numTestRun, null, null, controllerTRNumber, generatorName, "", false);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return graphNames;
  }

  public double[][] getAvgDataFromRTGFile()
  {
    return arrAvgDataValuesAll;
  }

  // calculate start and end seq number based paramters
  public boolean calPktRange(String time, String startTime, String endTime)
  {
    if (debugLevel > 0)
      Log.debugLogAlways(className, "calPktRange", "", "", "Method called. Time is  " + time + ", Start Time is " + startTime + ", End Time is " + endTime);

    long numStartTime = 0;
    long numEndTime = 0;
    try
    {
      if (time.equals("Total Run") || (time.equals("Run Phase Only") && phaseTimes == null))
      {
        startSeq = 1; // First data packet seq is 1
        endSeq = maxSeq;
      }
      else
      {
        if (time.equals("Run Phase Only")) // Exclude RampUp, WarmUp and RampDown packets
        {
          // phaseTimes[1] includes RampUp and WarmUp time
          numStartTime = (long) phaseTimes[1];
          // phaseTimes[2] includes RampUp, WarmUp and Run time
          numEndTime = (long) phaseTimes[2];
        }
        else
        {
          if (startTime.equals("NA"))
          {
            if (debugLevel > 0)
              Log.debugLogAlways(className, "calPktRange", "", "", "startTime is NA. So using Test Run Start Time = 00:00:00");
            numStartTime = 0L;
          }
          else
          {
            numStartTime = rptUtilsBean.convStrToMilliSec(startTime.trim());
          }

          if (endTime.equals("NA"))
          {
            numEndTime = maxSeq * interval;
            if (debugLevel > 0)
              Log.debugLogAlways(className, "calPktRange", "", "", "end time is NA. So using Test Run end Time = " + numEndTime);
          }
          else
          {
            numEndTime = rptUtilsBean.convStrToMilliSec(endTime.trim());
          }
        }

        // Make sure if startSeq is fractional, then round to next sequence so
        // that we do not include sample which is in before start time or RampUp/WarmUp phase
        startSeq = numStartTime / interval;
        if (numStartTime % interval != 0)
          startSeq++;
        // If endSeq is fractional, no need to round as it is already in time before end time
        endSeq = numEndTime / interval;

        if (numStartTime == 0)
          startSeq = 1;

        if (startSeq > (maxSeq))
        {
          if (debugLevel > 0)
            Log.debugLogAlways(className, "calPktRange", "", "", "Start Time exceeds Total Run time at Time is  " + time + ", Start Time is " + startTime + ", End Time is " + endTime + " and startSeq = " + startSeq + ", maxSeq = " + maxSeq + ", numTestRun = " + numTestRun);
          return false;
        }

        if (endSeq > (maxSeq))
        {
          endSeq = maxSeq;
        }
        else if (endSeq < startSeq)
        {
          Log.errorLog(className, "calPktRange", "", "", "End Time less than Start time");
          return false;
        }
      }

      if (phaseTimes != null)
        Log.debugLogAlways(className, "calPktRange", "", "", "Time is  " + time + ", Start Time is " + startTime + ", End Time is " + endTime + ", Start Seq is " + startSeq + ", End Seq is " + endSeq + ", Total Pkts is " + totalPkts + ", Phase Times are " + phaseTimes[0] + ", " + phaseTimes[1] + ", " + phaseTimes[2] + ", " + phaseTimes[3]);
      else
        Log.debugLogAlways(className, "calPktRange", "", "", "Time is  " + time + ", Start Time is " + startTime + ", End Time is " + endTime + ", Start Seq is " + startSeq + ", End Seq is " + endSeq + ", Total Pkts is " + totalPkts + ", Phase Times are not calculated so that global file not exist");

      return true;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "calPktRange", "", "", "Error while calculating range of packets " + e);
      return false;
    }
  }

  // To calculate report width
  public void calRptWidth(String graphType)
  {
    if (graphType.equals("Normal"))
      width = normalGraphWidth;
    else
    {
      width = 300 + ((int) endSeq - (int) startSeq) * pixelsPerDot;
      if (width > maxLongGraphWidth)
        width = maxLongGraphWidth;// This is to resolve Memory error for time being.will need to resolve later
    }
  }

  // This will result average data for over all data packets with 6 decimal places
  // Average value can be over flow if they are higher
  public double calAvgDataOverAll(double[] graphData)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "calAvgDataOverAll", "", "", "Method called.");

      double tempAvg = 0.0;
      for (int i = 0; i < graphData.length; i++)
        tempAvg = tempAvg + graphData[i];

      tempAvg = tempAvg / graphData.length;

      int decimalPlace = 3;// change for upto 3 decimal point average output
      BigDecimal bd = new BigDecimal(tempAvg);
      bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_EVEN);
      tempAvg = bd.doubleValue();

      return tempAvg;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "calAvgDataOverAll", "", "", "Error while calculating average - " + e);
      return 0.0;
    }
  }

  // Add start and end sequnce argument ?
  public int calAvgCount(String granularity, String graphType, int strtSeq, int endSeqn)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "calAvgCount", "", "", "Method called. Granularity is  " + granularity + ", Graph Type is " + graphType);
      Log.reportDebugLog(className, "calAvgCount", "", "", "Method called. Granularity is  " + granularity + ", Graph Type is " + graphType + ",strtSeq=" + strtSeq + ",endSeqn=" + endSeqn);
      int count = 0; // 0 or 1 means no averaging is required

      if (graphType.equals("Normal")) // granularity is only for Normal graphs
      {
        if (granularity.equals("0")) // This is Auto granularity
        {
          count = (int) ((endSeqn - strtSeq + 1) / maxPktsForAutoGranularity);
        }
        else
        {
          int tmpGranularity = Integer.parseInt(granularity.trim());
          count = tmpGranularity / (int) interval; // It can be 0 or 1 if granularity is <= internal
        }
      }
      if (debugLevel > 0)
        Log.debugLogAlways(className, "calAvgCount", "", "", "Average count is  " + count);
      return count;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "calAvgCount", "", "", "Error while calculating average count - " + e);
      return 0;
    }
  }

  // This will used when no need to get sequenced data only
  public double[] genAvgStampData(double arrValues[], int countAvg, int strtSeq, int endSeqn)
  {
    return (genAvgStampData(arrValues, countAvg, strtSeq, endSeqn, "No"));
  }

  // Pass agrument to get avgStamp for data for both with template and without template option
  public double[] genAvgStampData(double arrValues[], int countAvg, int strtSeq, int endSeqn, String fromRptGenFromRtpl)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "genAvgStampData", "", "", "Method called. AvgCount is  " + countAvg);
      double sumAvg = 0;
      double sumSeq = 0;

      avgArrDataVal = new double[(int) ((endSeqn - strtSeq + 1) / countAvg)];
      avgArrSeqNum = new double[(int) ((endSeqn - strtSeq + 1) / countAvg)];

      int ii = 0;
      for (int i = 0, n = arrValues.length; i < n; i++)
      {
        sumAvg = sumAvg + arrValues[i];

        if (fromRptGenFromRtpl.equals("No"))
          sumSeq = sumSeq + arrSeqNum[i];
        else
          sumSeq = sumSeq + arrSeqNumAll[i];

        if ((i + 1) % countAvg == 0)
        {
          // to show only 3 digit dicimal places
          avgArrDataVal[ii] = data.convTo3DigitDecimal(sumAvg / countAvg);
          avgArrSeqNum[ii] = (int) sumSeq / countAvg;
          sumAvg = 0;
          sumSeq = 0;
          ii++;
        }
      }
      return (avgArrDataVal);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "genAvgStampData", "", "", "Error while calculating avg packet ", e);
      return null;
    }
  }

  // This is for getting range data with calculated sequence
  public int[] calRangeForData(String time, String startTime, String endTime)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "calRangeForData", "", "", "Method called. Time is  " + time + ", Start Time is " + startTime + ", End Time is " + endTime);
      int[] tempStampRange = new int[2];

      long numStartTime = 0;
      long numEndTime = 0;

      if (time.equals("Total Run") || (time.equals("Run Phase Only") && phaseTimes == null))
      {
        tempStampRange[0] = 1; // First data packet seq is 1
        tempStampRange[1] = (int) maxSeq;
      }
      else
      {
        if (time.equals("Run Phase Only")) // Exclude RampUp, WarmUp and RampDown packets
        {
          // phaseTimes[1] includes RampUp and WarmUp time
          numStartTime = (long) phaseTimes[1];
          // phaseTimes[2] includes RampUp, WarmUp and Run time
          numEndTime = (long) phaseTimes[2];
        }
        else
        {
          numStartTime = rptUtilsBean.convStrToMilliSec(startTime.trim());
          numEndTime = rptUtilsBean.convStrToMilliSec(endTime.trim());
        }
        // Make sure if startSeq is fractional, then round to next sequence so
        // that we do not include sample which is in before start time or RampUp/WarmUp phase
        tempStampRange[0] = (int) numStartTime / (int) interval;
        if (numStartTime % interval != 0)
          tempStampRange[0]++;
        // If endSeq is fractional, no need to round as it is already in time before end time
        tempStampRange[1] = (int) numEndTime / (int) interval;

        if (numStartTime == 0)
          tempStampRange[0] = 1;

        if (tempStampRange[0] > (int) (maxSeq))
        {
          Log.errorLog(className, "calRangeForData", "", "", "Start Time exceeds Total Run time");
          return null;
        }

        if (tempStampRange[1] > (int) (maxSeq))
          tempStampRange[1] = (int) maxSeq;
        else if (tempStampRange[1] < tempStampRange[0])
        {
          Log.errorLog(className, "calRangeForData", "", "", "End Time less than Start time");
          return null;
        }
      }
      if (phaseTimes != null)
        Log.debugLogAlways(className, "calRangeForData", "", "", "Time is  " + time + ", Start Time is " + startTime + ", End Time is " + endTime + ", Start Seq is " + startSeq + ", End Seq is " + endSeq + ", Total Pkts is " + totalPkts + ", Phase Times are " + phaseTimes[0] + ", " + phaseTimes[1] + ", " + phaseTimes[2] + ", " + phaseTimes[3]);
      else
        Log.debugLogAlways(className, "calRangeForData", "", "", "Time is  " + time + ", Start Time is " + startTime + ", End Time is " + endTime + ", Start Seq is " + startSeq + ", End Seq is " + endSeq + ", Total Pkts is " + totalPkts + ", Phase Times are not calculated so that global file not exist");

      return tempStampRange;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "calRangeForData", "", "", "Error while calculating range of packets " + e);
      return null;
    }
  }

  // This will get array of data with selected start and end time stamp
  public double[] getRangeDataWithSequenceOnly(double arrValues[], int[] rangeDateForArr)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "getRangeDataWithSequenceOnly", "", "", "Method called");
      double[] tempArrseq = new double[rangeDateForArr[1] - rangeDateForArr[0] + 1];
      double[] tempArrDatVal = new double[rangeDateForArr[1] - rangeDateForArr[0] + 1];

      int j = 0;

      for (int i = rangeDateForArr[0] - 1; i < rangeDateForArr[1]; i++)
      {
        tempArrDatVal[j] = arrValues[i];
        tempArrseq[j] = arrSeqNum[i];
        j++;
      }
      arrSeqNumAll = tempArrseq;

      return tempArrDatVal;
    }
    catch (Exception e)
    {
      // If Any Exception Occurs due to data
      Log.stackTraceLog(className, "getRangeDataWithSequenceOnly", "", "", "Exception in getting data - ", e);
      return null;
    }
  }

  public boolean calDataToShowInRpt(double arrValues[], String graphType, String granularity, String startDate, String xAxisTimeFormat, boolean overrideRptOptions, String timeOption, String elapsedStartTime, String elapsedEndTime)
  {
    return (calDataToShowInRpt(arrValues, graphType, granularity, startDate, xAxisTimeFormat, overrideRptOptions, timeOption, elapsedStartTime, elapsedEndTime, -1));
  }

  public boolean calDataToShowInRpt(double arrValues[], String graphType, String granularity, String startDate, String xAxisTimeFormat, boolean overrideRptOptions, String timeOption, String elapsedStartTime, String elapsedEndTime, int avgCount)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "calDataToShowInRpt", "", "", "Method called");

      Log.reportDebugLog(className, "calDataToShowInRpt", "", "", "graphType=" + graphType + ",granularity=" + granularity + ",xAxisTimeFormat=" + xAxisTimeFormat + ",timeOption=" + timeOption + ",overrideRptOptions=" + overrideRptOptions + ",startDate=" + startDate + ",elapsedStartTime=" + elapsedStartTime + ",elapsedEndTime=" + elapsedEndTime + ",startSeq=" + (int) startSeq + ",endSeq=" + (int) endSeq);

      Date dateToday = null;
      int[] rangeDateForArr = null;

      if (overrideRptOptions == false)
      {
        if (avgCount != -1) // this is for changed granularity
        {
          rangeDateForArr = new int[2];
          rangeDateForArr[0] = (int) startSeq;
          rangeDateForArr[1] = (int) endSeq;
          arrSeqNumAll = arrSeqNum;
        }
        else
        {
          if (!xAxisTimeFormat.equals("ElapsedCompare")) // no need to calculate data for compare report
          {
            rangeDateForArr = calRangeForData(timeOption, elapsedStartTime, elapsedEndTime);
            arrValues = getRangeDataWithSequenceOnly(arrValues, rangeDateForArr);
          }
          else
          {
            rangeDateForArr = new int[2];
            rangeDateForArr[0] = (int) startSeq;
            rangeDateForArr[1] = (int) endSeq;
            arrSeqNumAll = arrSeqNum;
          }
        }

        if (avgCount == -1)
        {
          countAvg = calAvgCount(granularity, graphType, rangeDateForArr[0], rangeDateForArr[1]);
        }
        else
        {
          // when average count is specified
          countAvg = avgCount;
        }

        Log.reportDebugLog(className, "calDataToShowInRpt", "", "", "countAvg=" + countAvg);

        if (countAvg > 1)
          arrValues = genAvgStampData(arrValues, countAvg, rangeDateForArr[0], rangeDateForArr[1], "Yes");
        else
          avgArrSeqNum = arrSeqNumAll; // This will handle sequence when countAvg is 0
      }
      else
      {
        countAvg = calAvgCount(granularity, graphType, (int) startSeq, (int) endSeq);
        if (countAvg > 1)
          arrValues = genAvgStampData(arrValues, countAvg, (int) startSeq, (int) endSeq);
        else
          avgArrSeqNum = arrSeqNum; // This will handle sequence when countAvg is 0
      }

      arrDataVal = arrValues;
      Log.reportDebugLog(className, "calDataToShowInRpt", "", "", "arrDataVal.length=" + arrDataVal.length);

      if (avgCount != -1) // if average count is specified then to need to process further
        return true;

      calRptWidth(graphType);
      // This will set current time as 00:00:00 for elapsed time
      if (xAxisTimeFormat.equals("Elapsed") || xAxisTimeFormat.equals("ElapsedCompare"))
      {
        dateToday = new Date();
        dateToday.setSeconds(0);
        dateToday.setHours(0);
        dateToday.setMinutes(0);
      }
      else
      {
        dateToday = new Date(startDate);
      }

      timeInMilli = dateToday.getTime();
      /*
       * This will use for min and max axis value For Specified time,
       * 
       * start time and end time can be set so keep first sequance for time
       * 
       * setting time stamp + (avgArrSeqNum[0])*interval
       */
      long startAndEndSpaceOfChart = 0;

      if (graphType.equals("Normal"))
        startAndEndSpaceOfChart = ((long) (avgArrSeqNum[avgArrSeqNum.length - 1] - avgArrSeqNum[0]) * interval) / 40;
      else
        startAndEndSpaceOfChart = interval;

      if (xAxisTimeFormat.equals("Elapsed"))
      {
        startTimeInmilli = timeInMilli + ((long) avgArrSeqNum[0]) * interval - startAndEndSpaceOfChart;
        endTimeInmilli = timeInMilli + ((long) avgArrSeqNum[avgArrSeqNum.length - 1]) * interval + startAndEndSpaceOfChart;
      }
      else
      {
        // For Compare report, we are using all time relative to 00:00:00 on x-axis
        startTimeInmilli = timeInMilli + 0 * interval - startAndEndSpaceOfChart;
        endTimeInmilli = timeInMilli + ((long) (avgArrSeqNum[avgArrSeqNum.length - 1] - avgArrSeqNum[0])) * interval + startAndEndSpaceOfChart;
      }

      return true;
    }
    catch (Exception e)
    {
      // If Any Exception Occurs due to data
      Log.stackTraceLog(className, "calDataToShowInRpt", "", "", "Exception in adding data - ", e);
      return false;
    }
  }

  // abhishek - calculate number of avg count for cerrelated report type
  public int calCountAvgForCorrelated(String graphType, String granularity)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "calCountAvgForCorrelated", "", "", "Method called");
      int countAvg = 0;

      countAvg = calAvgCount(granularity, graphType, (int) startSeq, (int) endSeq);
      calRptWidth(graphType);
      return countAvg;
    }
    catch (Exception e)
    {
      // If Any Exception Occurs due to data
      Log.errorLog(className, "calCountAvgForCorrelated", "", "", "Exception in adding data - " + e);
      return 0;
    }
  }

  // abhishek - Update this metthod for doing averaging data array for cerrelated report type
  public double[] calDataForCorrelated(double arrValues[], int countAvg, String graphType)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "calDataForCorrelated", "", "", "Method called");
      if (countAvg > 1)
        arrValues = genAvgStampData(arrValues, countAvg, (int) startSeq, (int) endSeq);
      else
        return arrValues;

      return arrValues;
    }
    catch (Exception e)
    {
      // If Any Exception Occurs due to data
      Log.errorLog(className, "calDataForCorrelated", "", "", "Exception in adding data - " + e);
      return null;
    }
  }

  // This will read global data file and return different phases of test run
  public boolean getPhaseTimes()
  {
    String globalDataFile = getTestRunDirPath() + "/global.dat";

    try
    {
      String str1 = "";
      StringTokenizer st3;
      String str5[];

      fis = new FileInputStream(globalDataFile);

      if (debugLevel > 0)
        Log.debugLogAlways(className, "getPhaseTimes", "", "", "globalFilepath =" + globalDataFile);

      BufferedReader br = new BufferedReader(new InputStreamReader(fis));
      int j = 0;

      while ((str1 = br.readLine()) != null)
      {
        j = 0;
        if (str1.indexOf("PHASE_TIMES") != -1)
        {
          phaseTimes = new double[4];
          st3 = new StringTokenizer(str1);
          str5 = new String[st3.countTokens()];
          while (st3.hasMoreTokens())
          {
            str5[j] = st3.nextToken();
            j++;
          }
          // phaseTimes = new double[str5.length - 1];
          for (int ii = 1, mm = str5.length; ii < mm; ii++)
          {
            phaseTimes[ii - 1] = Double.parseDouble(str5[ii]) * 1000;
            if (debugLevel > 0)
              Log.debugLogAlways(className, "getPhaseTimes", "", "", "Phase Time =" + phaseTimes[ii - 1]);
          }
        }
      }
      br.close();
      fis.close();
      return true;
    }
    catch (FileNotFoundException e)
    {
      // Do not return false if file is not found. This will allow user to generate graphs for running test runs.
      Log.errorLog(className, "getPhaseTimes", "", "", "File (" + globalDataFile + ") not found. Ignored as test run may be running. Using 0 for all times");
      // Make it null so that we can check letter data is there or not.
      phaseTimes = null;
      return true;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "getPhaseTimes", "", "", "Exception while reading data file" + e);
      phaseTimes = null;
      return false;
    }
  }

  /**
   * this function return the testRunData in offline mode, Baseline TR and for tracked TR based on test run data type (0 --> Normal Test Run Data, 1 --> Baseline Test Run Data, 2 --> Tracked Test Run Data) this is called from ClientHandler.java in case of getting test run data in offline mode testViewMode --> true for offline, false for online
   * 
   * @param granularity
   * @param testRunDataType
   * @param getCompleteTestRunData
   * @param numberOfPanels
   * @param userName
   * @param testViewMode
   * @return
   */
  public TestRunData getTestRunData(int granularity, int testRunDataType, boolean getCompleteTestRunData, int numberOfPanels, String userName, boolean testViewMode)
  {
    return (getTestRunData(granularity, testRunDataType, getCompleteTestRunData, numberOfPanels, userName, graphNames, "NA", "NA", "NA", "NA", testViewMode));
  }

  /**
   * this method return the testRunData in offline mode, Baseline TR and for tracked TR based on test run data type (0 --> Normal Test Run Data, 1 --> Baseline Test Run Data, 2 --> Tracked Test Run Data) This is called from OperateServlet.java
   * 
   * @param granularity
   * @param testRunDataType
   * @param graphNamesObj
   * @param startTime
   * @param endTime
   * @param phaseName
   * @param interval
   * @return
   */
  public TestRunData getTestRunData(int granularity, int testRunDataType, GraphNames graphNamesObj, String startTime, String endTime, String phaseName, String interval)
  {
    return (getTestRunData(granularity, testRunDataType, true, 9, "", graphNamesObj, startTime, endTime, phaseName, interval, false));
  }

  /**
   * this method return the testRunData in offline mode, Baseline TR and for tracked TR based on test run data type (0 --> Normal Test Run Data, 1 --> Baseline Test Run Data, 2 --> Tracked Test Run Data)
   * 
   * @param granularity
   * @param testRunDataType
   * @param getCompleteTestRunData
   * @param numberOfPanels
   * @param userName
   * @param graphNamesObj
   * @param startTime
   * @param endTime
   * @param phaseName
   * @param interval
   * @return
   */
  public TestRunData getTestRunData(int granularity, int testRunDataType, boolean getCompleteTestRunData, int numberOfPanels, String userName, GraphNames graphNamesObj, String startTime, String endTime, String phaseName, String interval)
  {
    return (getTestRunData(granularity, testRunDataType, getCompleteTestRunData, numberOfPanels, userName, graphNamesObj, startTime, endTime, phaseName, interval, false));
  }

  /**
   * this method return the testRunData in offline mode, Baseline TR and for tracked TR based on test run data type (0 --> Normal Test Run Data, 1 --> Baseline Test Run Data, 2 --> Tracked Test Run Data) testViewMode --> true for offline, false for online
   * 
   * @param granularity
   * @param testRunDataType
   * @param getCompleteTestRunData
   * @param numberOfPanels
   * @param userName
   * @param graphNamesObj
   * @param startTime
   * @param endTime
   * @param phaseName
   * @param interval
   * @param testViewMode
   * @return
   */
  public TestRunData getTestRunData(int granularity, int testRunDataType, boolean getCompleteTestRunData, int numberOfPanels, String userName, GraphNames graphNamesObj, String startTime, String endTime, String phaseName, String interval, boolean testViewMode)
  {
    return getTestRunData(granularity, testRunDataType, getCompleteTestRunData, numberOfPanels, userName, graphNamesObj, startTime, endTime, phaseName, interval, testViewMode, null, null);
  }

  /**
   * Return Test Run Data for given graph index
   * 
   * @param granularity
   * @param testRunDataType
   * @param getCompleteTestRunData
   * @param numberOfPanels
   * @param userName
   * @param graphNamesObj
   * @param startTime
   * @param endTime
   * @param phaseName
   * @param interval
   * @param testViewMode
   * @param arrGraphIndex
   * @return
   */
  public TestRunData getTestRunData(int granularity, int testRunDataType, boolean getCompleteTestRunData, int numberOfPanels, String userName, GraphNames graphNamesObj, String startTime, String endTime, String phaseName, String interval, boolean testViewMode, GraphUniqueKeyDTO[] arrActiveGraphUniqueKeyDTO)
  {
    return getTestRunData(granularity, testRunDataType, getCompleteTestRunData, numberOfPanels, userName, graphNamesObj, startTime, endTime, phaseName, interval, testViewMode, arrActiveGraphUniqueKeyDTO, null);
  }

  /**
   * Return Requested Test Run Data
   * 
   * Note:
   * 
   * 1. Test run data type (0 --> Normal Test Run Data, 1 --> Baseline Test Run Data, 2 --> Tracked Test Run Data)
   * 
   * 2. testViewMode(true for offline, false for online)
   * 
   * @param granularity
   * @param testRunDataType
   * @param getCompleteTestRunData
   * @param numberOfPanels
   * @param userName
   * @param graphNamesObj
   * @param startTime
   * @param endTime
   * @param phaseName
   * @param interval
   * @param testViewMode
   * @param arrGraphIndexes
   * @return
   */
  public TestRunData getTestRunData(int granularity, int testRunDataType, boolean isAutoActivate, int numberOfPanels, String userName, GraphNames graphNamesObj, String startTime, String endTime, String phaseName, String interval, boolean testViewMode, GraphUniqueKeyDTO[] arrActiveGraphUniqueKeyDTO, TestRunDataType testRunDataTypeObj)
  {
    try
    {

      if (debugLevel > 0)
        Log.debugLogAlways(className, "getTestRunData", "", "", "Method Starts. Granularity = " + granularity + ", Test run data type = " + testRunDataType + ", startTime = " + startTime + ", endTime = " + endTime + ", phaseName = " + phaseName + ", testViewMode = " + testViewMode + ", testRunDataTypeObj = " + testRunDataTypeObj + ", graphNamesObj = " + graphNamesObj + ", testRun_Partition_Type = " + testRun_Partition_Type + ", nDE_Continuous_Mode = " + nDE_Continuous_Mode);

      /***************** Here checking for Partition mode keyword *********************/

      if (testRun_Partition_Type > 0)
      {
        if (testRunDataTypeObj == null)
          testRunDataTypeObj = getTestRunDataTypeByConfigSetting();

        if (debugLevel > 0)
          Log.debugLogAlways(className, "getTestRunData", "", "", "Key = " + testRunDataTypeObj.getHMapKey());

        /* Creating TestRunData From Different Partition Files. */
        return (getTestRunDataForSessions(granularity, startTime, endTime, testRunDataType, isAutoActivate, null, numberOfPanels, userName, testViewMode, testRunDataTypeObj));
      }

      /********************************************************************************/
      
      /*Checking for Test Run Data Type object.*/
      if(testRunDataTypeObj == null)
      {
	if(nDE_Continuous_Mode)
	  testRunDataTypeObj = getTestRunDataTypeByConfigSetting();
	else
          testRunDataTypeObj = new TestRunDataType(testViewMode);
      }
      
      this.graphNames = graphNamesObj;

      if (debugLevel > 0)
        Log.debugLogAlways(className, "getTestRunData", "", "", "Opening rtgMessage.dat file");

      boolean rtgFileFound = openRTGMsgFile();

      if ((startTime.equals("NA")) && (endTime.equals("NA")))
      {
        startSeq = 1;
        endSeq = maxSeq;
      }
      else
      {
        try
        {
          if (!interval.equals("NA"))
            calStartEndSequence(phaseName, startTime, endTime, Integer.parseInt(interval));
          else
            calStartEndSequence(phaseName, startTime, endTime, graphNamesObj.getInterval());
        }
        catch (Exception ex)
        {
          Log.stackTraceLog(className, "getTestRunData", "", "", "Exception - ", ex);
        }
      }
      if (debugLevel > 0)
      {
        Log.debugLogAlways(className, "getTestRunData", "", "", "StartSeq = " + startSeq + ", endSeq = " + endSeq + " to getting all data pkt from rtgMessage.dat file.");
        Log.debugLogAlways(className, "getTestRunData", "", "", "Creating testRunData for Test Run = " + numTestRun);
      }

      data tempMsgData = new data();
      TestRunData testRunDataObject = new TestRunData(testRunDataType, numTestRun, startSeq, endSeq, granularity, numberOfPanels, userName, arrActiveGraphUniqueKeyDTO, testViewMode, graphNamesObj, testRunDataTypeObj);
      TimeBasedTestRunData timeBasedTestRundata = testRunDataObject.getTimeBasedTestRunDataByKey(testRunDataTypeObj.getHMapKey());

      // Check For Derived DTO Availability.
      processDerivedGraphRequest(timeBasedTestRundata);

      if (!rtgFileFound)
      {
        testRunDataObject.insertDataInHashMap(testRunDataTypeObj.getHMapKey(), timeBasedTestRundata);

        testRunDataObject.setErrorMsg("This Test Run is not compatible with the current software (Test Run message file(rtgMessage.dat) does not exists).\nYou can not open Execution GUI for this test run.");
        Log.errorLog(className, "", "", "", "This Test Run is not compatible because rtgMessage.dat not found.");

        return testRunDataObject;
      }

      // For Generating only raw data.
      if (genOnlyRawData)
        readRawDataPkt(msgData, prevMsgData, data.START_PKT, 0);
      else
        readOnePkt(msgData, prevMsgData, data.START_PKT, 0, null, true, true, true);

      int numberOfBytesToSkip = (int) (pktSize * (startSeq - 1));
      int bytesRead = dis.skipBytes(numberOfBytesToSkip);
      if (debugLevel > 0)
        Log.debugLogAlways(className, "getTestRunData", "", "", "Total Skip Bytes = " + bytesRead);

      for (int seqNum = (int) startSeq; seqNum <= endSeq; seqNum++)
      {
        try
        {
          if (genOnlyRawData)
            tempMsgData = readRawDataPkt(msgData, prevMsgData, data.DATA_PKT, seqNum);
          else
            tempMsgData = readOnePkt(msgData, prevMsgData, data.DATA_PKT, seqNum, null, true, true, true);

          if (tempMsgData == null)
            continue;

          timeBasedTestRundata.putTimeBasedTestRunData(tempMsgData);
        }
        catch (Exception e)
        {
          Log.stackTraceLog(className, "getTestRunData", "", "", "Exception - ", e);
        }
      }

      testRunDataObject.testRunIsOver();
      closeRTGMsgFile();

      testRunDataObject.insertDataInHashMap(testRunDataTypeObj.getHMapKey(), timeBasedTestRundata);

      if (testRunDataObject.getNetCloudData() != null)
      {
        createGeneratorTBRDObject(testRunDataObject, testRunDataTypeObj, startTime, endTime, interval, phaseName, granularity, testViewMode);
      }
      return testRunDataObject;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getTestRunData", "", "", "Exception - ", e);
      return null;
    }
  }

  public TestRunData getTestRunDataBasedOnTBRDDto(RequestTimeBasedDTO reqTBRDDto)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "getTestRunDataBasedOnTBRDDto", "", "", "Method Called. RequestTimeBasedDTO Details = " + reqTBRDDto.toString());
      long startProcessingTime = System.currentTimeMillis();

      TestRunData testRunDataObject = null;
      // Getting all detail form TBRD DTO object
      RequestGraphsDTO reqGraphDto[] = reqTBRDDto.getReqGraphDto();
      boolean isAutoActivate = reqTBRDDto.isAutoActivate();
      String startTime = reqTBRDDto.getStartTime();
      String endTime = reqTBRDDto.getEndTime();
      String phaseName = reqTBRDDto.getPhaseName();
      int testRunDataType = reqTBRDDto.getTestRunDataType();
      boolean testViewMode = reqTBRDDto.isTestViewMode();
      String interval = reqTBRDDto.getInterval();
      TestRunDataType testRunDataTypeObj = reqTBRDDto.getTestRunDataTypeObj();
      int granularity = reqTBRDDto.getGranularity();
      int numberOfDisplayPanels = reqTBRDDto.getNumberOfPanels();
      String userName = reqTBRDDto.getUserName();
      controllerTRNumber = "" + reqTBRDDto.getTestRun();

      if (debugLevel > 0)
      {
        Log.debugLogAlways(className, "getTestRunDataBasedOnTBRDDto", "", "", "controllerTRNumber = " + controllerTRNumber);
        Log.debugLogAlways(className, "getTestRunDataBasedOnTBRDDto", "", "", "reqGraphDto = " + reqGraphDto);
      }

      if (reqGraphDto == null)
      {
        Log.debugLogAlways(className, "getTestRunDataBasedOnTBRDDto", "", "", "Graph Names DTO Object is null, so getting default Test Run Data for Test Run = " + numTestRun);
        testRunDataObject = getTestRunData(granularity, testRunDataType, isAutoActivate, numberOfDisplayPanels, userName, reqTBRDDto.getGraphNamesObj(), startTime, endTime, phaseName, interval, testViewMode);
      }
      else
      {

        if (debugLevel > 0)
          Log.debugLogAlways(className, "getTestRunDataBasedOnTBRDDto", "", "", "reqGraphDto.len = " + reqGraphDto.length);
        if (testRun_Partition_Type <= 0)
        {
          for (int i = 0; i < reqGraphDto.length; i++)
          {
            RequestGraphsDTO requestGraphsDTOObj = reqGraphDto[i];

            if (debugLevel > 0)
              Log.debugLogAlways(className, "getTestRunDataBasedOnTBRDDto", "", "", "requestGraphsDTOObj = " + requestGraphsDTOObj.toString());

            GraphUniqueKeyDTO[] arrActiveGraphUniqueKeyDTO = requestGraphsDTOObj.getGraphUniqueKeyDTO();
            String generatorPrefix = requestGraphsDTOObj.getGeneratorPrefix();

            if (testRunDataObject == null)
            {
              Log.debugLogAlways(className, "getTestRunDataBasedOnTBRDDto", "", "", "Creating test run data for key = " + testRunDataTypeObj.getHMapKey());
              testRunDataObject = new TestRunData(testRunDataType, numTestRun, startSeq, endSeq, granularity, numberOfDisplayPanels, userName, arrActiveGraphUniqueKeyDTO, testViewMode, graphNames, testRunDataTypeObj);
              HashMap<String, TimeBasedTestRunData> hmTimeBasedTestRunData = testRunDataObject.getAllAvailableTestRunData();
              Log.debugLogAlways(className, "getTestRunDataBasedOnTBRDDto", "", "", "Created TimeBased = " + hmTimeBasedTestRunData);
            }

            if (debugLevel > 0)
              Log.debugLogAlways(className, "getTestRunDataBasedOnTBRDDto", "", "", "generatorPrefix = " + generatorPrefix);

            if (generatorPrefix == null || generatorPrefix.length() == 0)
            {
              if (debugLevel > 0)
                Log.debugLogAlways(className, "getTestRunDataBasedOnTBRDDto", "", "", "Going to get TBRD Object for Controller Test Run = " + numTestRun + ", Key = " + testRunDataTypeObj.getHMapKey());
              generatorName = null;
              numTestRun = reqTBRDDto.getTestRun();
              TimeBasedTestRunData tbrdObj = getReqTimeBasedTestRunData(isAutoActivate, startTime, endTime, phaseName, granularity, testRunDataType, testViewMode, Integer.parseInt(interval), numberOfDisplayPanels, userName, arrActiveGraphUniqueKeyDTO, testRunDataTypeObj, testRunDataObject);
              testRunDataObject.insertDataInHashMap(testRunDataTypeObj.getHMapKey(), tbrdObj);
            }
            else
            {
              ArrayList<NetCloudDTO> list = testRunDataObject.getNetCloudDto();
              if (list != null)
              {
                for (int kk = 0; kk < list.size(); kk++)
                {
                  NetCloudDTO ncDto = testRunDataObject.getNetCloudDto().get(kk);
                  // Matching TBRD Prefix with NetCloud Generator List
                  if (ncDto.getGeneratorName().equals(generatorPrefix))
                  {
                    // Setting class variables to get TBRD object for NetCloud
                    generatorName = ncDto.getGeneratorName();
                    numTestRun = Integer.parseInt(ncDto.getGeneratorTRNumber());

                    if (debugLevel > 0)
                      Log.debugLogAlways(className, "getTestRunDataBasedOnTBRDDto", "", "", "Going to get TBRD Object for Generator Test Run = " + numTestRun + " (" + generatorName + ") " + ", Key = " + testRunDataTypeObj.getHMapKey() + ", controllerTRNumber = " + controllerTRNumber);

                    TimeBasedTestRunData tbrdObject = getReqTimeBasedTestRunData(isAutoActivate, startTime, endTime, phaseName, granularity, testRunDataType, testViewMode, Integer.parseInt(interval), numberOfDisplayPanels, userName, arrActiveGraphUniqueKeyDTO, testRunDataTypeObj, testRunDataObject);
                    testRunDataObject.insertDataInHashMap(generatorName + "_" + testRunDataTypeObj.getHMapKey(), tbrdObject);
                  }
                }
              }
              else
              {
                String tbtrdKey = testRunDataTypeObj.getHMapKey();
                if (debugLevel > 0)
                  Log.debugLogAlways(className, "getTestRunDataBasedOnTBRDDto", "", "", "Going to get TBRD Object for Controller Test Run = " + numTestRun + ", Key = " + tbtrdKey);

                TimeBasedTestRunData tbrdObj = getReqTimeBasedTestRunData(isAutoActivate, startTime, endTime, phaseName, granularity, testRunDataType, testViewMode, Integer.parseInt(interval), numberOfDisplayPanels, userName, arrActiveGraphUniqueKeyDTO, testRunDataTypeObj, testRunDataObject);

                if (debugLevel > 0)
                  Log.debugLogAlways(className, "getTestRunDataBasedOnTBRDDto", "", "", "DataItemCount in TBTRD = " + tbrdObj.getDataItemCount());

                testRunDataObject.insertDataInHashMap(tbtrdKey, tbrdObj);
              }
            }
          }
        }
        else
        {
          Log.debugLogAlways(className, "getTestRunDataBasedOnTBRDDto", "", "", "Parition mode is enable for TestRun " + numTestRun);

          for (int i = 0; i < reqGraphDto.length; i++)
          {
            RequestGraphsDTO requestGraphsDTOObj = reqGraphDto[i];

            if (debugLevel > 0)
              Log.debugLogAlways(className, "getTestRunDataBasedOnTBRDDto", "", "", "requestGraphsDTOObj = " + requestGraphsDTOObj.toString());

            GraphUniqueKeyDTO[] arrActiveGraphUniqueKeyDTO = requestGraphsDTOObj.getGraphUniqueKeyDTO();
            String generatorPrefix = requestGraphsDTOObj.getGeneratorPrefix();

            if (testRunDataObject == null)
            {
              Log.debugLogAlways(className, "getTestRunDataBasedOnTBRDDto", "", "", "Creating test run data for key = " + testRunDataTypeObj.getHMapKey());
              long absStartTime = 0L;
              long absEndTime = 0L;

              // Checking for Specified Time Request.
              if (testRunDataTypeObj.getType() == TestRunDataType.SPECIFIED_PHASE_OR_TIME)
              {
                try
                {
                  absStartTime = Long.parseLong(startTime);
                  absEndTime = Long.parseLong(endTime);
                }
                catch (Exception e)
                {
                  Log.errorLog(className, "getTestRunDataBasedOnTBRDDto", "", "", "Exception - " + e.getMessage());
                }
              }

              // Creating TestRunData object.
              testRunDataObject = new TestRunData(numTestRun, granularity, absStartTime, absEndTime, numberOfDisplayPanels, userName, arrActiveGraphUniqueKeyDTO, testViewMode, testRunDataTypeObj, "", null, derivedDataDTO, false);
              TimeBasedTestRunData tbrdObject = testRunDataObject.getTimeBasedTestRunDataByKey(testRunDataTypeObj.getHMapKey());

              /* Optimizing Time Based Object. */
              postProcessing(tbrdObject, startProcessingTime);
              Log.debugLogAlways(className, "getTestRunDataBasedOnTBRDDto", "", "", "Created TimeBased object with key = " + testRunDataTypeObj.getHMapKey());
            }

            Log.debugLogAlways(className, "getTestRunDataBasedOnTBRDDto", "", "", "generatorPrefix = " + generatorPrefix);

            if (generatorPrefix != null && generatorPrefix.length() != 0)
            {
              ArrayList<NetCloudDTO> list = testRunDataObject.getNetCloudDto();
              Log.debugLogAlways(className, "getTestRunDataBasedOnTBRDDto", "", "", "NetCloud List = " + list);
              if (list != null)
              {
                Log.debugLogAlways(className, "getTestRunDataBasedOnTBRDDto", "", "", "NetCloud List = " + list + ", Size = " + list.size());
                for (int kk = 0; kk < list.size(); kk++)
                {
                  NetCloudDTO ncDto = testRunDataObject.getNetCloudDto().get(kk);
                  // Matching TBRD Prefix with NetCloud Generator List
                  if (ncDto.getGeneratorName().equals(generatorPrefix))
                  {
                    // Setting class variables to get TBRD object for NetCloud
                    generatorName = ncDto.getGeneratorName();
                    numTestRun = Integer.parseInt(ncDto.getGeneratorTRNumber());

                    Log.debugLogAlways(className, "getTestRunDataBasedOnTBRDDto", "", "", "Going to get TBRD Object for Generator (From Parition) Test Run = " + numTestRun + " (" + generatorName + ") " + ", Key = " + testRunDataTypeObj.getHMapKey() + ", controllerTRNumber = " + controllerTRNumber);

                    TimeBasedTestRunData tbrdObject = testRunDataObject.getTimeBasedTestRunDataByKey(generatorName + "_" + testRunDataTypeObj.getHMapKey());
                    testRunDataObject.createTimeBasedDataFromPartitions(granularity, arrActiveGraphUniqueKeyDTO, testViewMode, tbrdObject);
                    postProcessing(tbrdObject, startProcessingTime);
                  }
                }
              }
            }
          }
        }
      }

      long endProcessingTime = System.currentTimeMillis();
      Log.debugLogAlways(className, "getTestRunDataBasedOnTBRDDto", "", "", "In Creating Test Run Data Total Time Taken = " + (endProcessingTime - startProcessingTime) / 1000 + " Seconds");

      if (debugLevel > 1)
      {
        Log.debugLogAlways(className, "getTestRunDataBasedOnTBRDDto", "", "", "Size of HashMap to send RTG GUI in bytes = " + PartitionInfoUtils.getObjectSizeInBytes(testRunDataObject.getAllAvailableTestRunData()));
        Log.debugLogAlways(className, "getTestRunDataBasedOnTBRDDto", "", "", "Available Time Based Object = " + testRunDataObject.getAllAvailableTestRunData().size());
      }

      return testRunDataObject;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getTestRunDataBasedOnTBRDDto", "", "", "Exception - ", e);
      return null;
    }
  }

  /**
   * This method creates time based object for generators if testrun is Necloud testrun.
   * 
   * @param testRunDataObject
   * @param testRunDataTypeObj
   * @param startTime
   * @param endTime
   * @param interval
   * @param phaseName
   */
  private void createGeneratorTBRDObject(TestRunData testRunDataObject, TestRunDataType testRunDataTypeObj, String startTime, String endTime, String interval, String phaseName, int granularity, boolean testViewMode)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "generateGeneratorTBRDObject", "", "", "method called. key = " + testRunDataTypeObj.getHMapKey() + " startTime = " + startTime + " endTime = " + endTime + " interval = " + interval + " phaseName = " + phaseName + " granularity = " + granularity + " testViewMode = " + testViewMode);

      // GraphNames instance for generators.
      GraphNames graphNamesObj = testRunDataObject.generatorGraphNames;

      // Setting current Test Run number is controller Test Run number to open generator rtgMessage.dat
      controllerTRNumber = String.valueOf(numTestRun);

      // Reading info of all available generators.
      HashMap<String, ArrayList<String>> ncInfoMap = NetCloudData.getGeneratorDetailsWithTR(testRunDataObject.getNetCloudData(), numTestRun);

      for (Map.Entry<String, ArrayList<String>> entry : ncInfoMap.entrySet())
      {
        String key = entry.getKey();

        // setting generator Test Run is current Test Run number
        numTestRun = Integer.parseInt(ncInfoMap.get(key).get(0));

        // Skipping for controller, because netcloud info also contain controller TestRun.
        if (numTestRun == -1 || key.trim().equals("-1"))
          continue;

        // Getting Generator Name.
        this.generatorName = ncInfoMap.get(key).get(1);

        // Creating key for each generator.
        String strGeneratorTBRDKey = generatorName + "_" + testRunDataTypeObj.getHMapKey();
        if (debugLevel > 0)
          Log.debugLogAlways(className, "generateGeneratorTBRDObject", "", "", "Requesting to get generator TBRD object for generator = " + generatorName + ", TBRD key For Generater = " + strGeneratorTBRDKey + " numTestRun = " + numTestRun);

        // Getting Time Based Object For Generator.
        TimeBasedTestRunData generatorTBRDObj = testRunDataObject.getTimeBasedTestRunDataByKey(strGeneratorTBRDKey);

        // Getting all Graph Numbers from Generator TR.
        // TODO - Need to implement with Auto activate

        GraphUniqueKeyDTO[] arrActiveGraphUniqueKeyDTO = generatorTBRDObj.getGraphNamesObj().getGraphUniqueKeyDTO();

        // Here we need to first initialize generator TBTRD variable by init method.
        generatorTBRDObj.initForNewTimeBasedTestRunData(granularity, arrActiveGraphUniqueKeyDTO, testViewMode);

        boolean rtgFileFound = openRTGMsgFile(graphNamesObj);

        if ((startTime.equals("NA")) && (endTime.equals("NA")))
        {
          startSeq = 1;
          endSeq = maxSeq;
        }
        else
        {
          try
          {
            if (!interval.equals("NA"))
              calStartEndSequence(phaseName, startTime, endTime, Integer.parseInt(interval));
            else
              calStartEndSequence(phaseName, startTime, endTime, graphNamesObj.getInterval());
          }
          catch (Exception ex)
          {
            ex.printStackTrace();
            Log.stackTraceLog(className, "generateGeneratorTBRDObject", "", "", "Exception - ", ex);
          }
        }
        if (debugLevel > 0)
          Log.debugLogAlways(className, "generateGeneratorTBRDObject", "", "", "StartSeq = " + startSeq + ", endSeq = " + endSeq + " to getting all data pkt from rtgMessage.dat file.");
        data genMsgData = new data();

        if (!rtgFileFound)
          testRunDataObject.setErrorMsg("This Test Run is not compatible with the current software (Generator(" + generatorName + ")Test Run message file(rtgMessage.dat) does not exists).\nYou can not open Execution GUI for this test run.");

        // Reading Start Pkt.
        readOnePkt(msgData, prevMsgData, data.START_PKT, 0, null, true, true, true);

        // Skipping header and start pkt.
        int numberOfBytesToSkip = (int) (pktSize * (startSeq - 1));

        // Calculate total skipp
        int bytesRead = dis.skipBytes(numberOfBytesToSkip);
        if (debugLevel > 0)
          Log.debugLogAlways(className, "generateGeneratorTBRDObject", "", "", "Total Skip Bytes = " + bytesRead);

        for (int seqNum = (int) startSeq; seqNum <= endSeq; seqNum++)
        {
          // Reading Data Pkt.
          if ((genMsgData = readOnePkt(msgData, prevMsgData, data.DATA_PKT, seqNum, null, true, true, true)) == null)
            continue;

          // Creating Time Based Data.
          generatorTBRDObj.putTimeBasedTestRunData(genMsgData);
        }

        // Close RTG Input Stream and file.
        closeRTGMsgFile();
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "generateGeneratorTBRDObject", "", "", "Caught exception to generator TBRD object for generators", e);
    }
  }

  /**
   * This Method Used to calculate Start and End Sequence Number on the basis of TestRunDataType key For Reading rtgMessage.dat.
   * 
   * @param startTime
   * @param endTime
   * @param phaseName
   * @param testViewMode
   * @param interval
   * @param testRunDataTypeObj
   */
  private void calculateStartAndEndSequence(String startTime, String endTime, String phaseName, boolean testViewMode, int interval, TestRunDataType testRunDataTypeObj)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "calculateStartAndEndSequence", "", "", "Method Called with Parameters : startTime = " + startTime + " endTime = " + endTime + " phaseName = " + phaseName + " testViewMode = " + testViewMode + " interval = " + interval + " TestRunDataType key = " + testRunDataTypeObj.getHMapKey());

      // In Case of Last N Minute we need to calculate start and end sequence based on Last N value.
      if (testRunDataTypeObj.getType() == TestRunDataType.LAST_N_MINUTES_DATA)
      {
        // Getting Last N Minute Duration in Millisecond.
        long LastNMinuteDuration = testRunDataTypeObj.getLastNMinutesValue() * 60 * 1000;

        if (LastNMinuteDuration > 0)
        {
          long totalSampleForLast = LastNMinuteDuration / interval;
          endSeq = maxSeq; // In Case of Last N Minute End Sequence is always Last/Max Sequence.
          // calculating start and end sequence
          if (maxSeq <= totalSampleForLast) // This Means that test run Duration exactly equal to Last N Hour.
          {
            startSeq = 1;
          }
          else
          {
            // As we are iterating loop equals to END_PKT.
            startSeq = maxSeq - (totalSampleForLast - 1);
          }
        }
      }
      else
      {
        if (startTime.equals("NA") && endTime.equals("NA"))
        {
          startSeq = 1;
          if (testViewMode) // True means test is in off line mode
            endSeq = maxSeq;
          else
            endSeq = maxSeq + 1; // As we are doing -2 in openRTGMsgFile. So need to adjust
        }
        else
        {
          // Calculate Start and End Sequence with Specified Time.
          calStartEndSequence(phaseName, startTime, endTime, interval);
        }
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "calculateStartAndEndSequence", "", "", "Exception - ", e);
    }
  }

  /**
   * 
   * @param startTime
   * @param endTime
   * @param phaseName
   * @param granularity
   * @param testRunDataType
   *          0 --> Normal Test Run Data, 1 --> Baseline Test Run Data, 2 --> Tracked Test Run Data
   * @param testViewMode
   *          true for offline, false for online
   * @param timeBasedTestRunDataType
   * @return
   */

  public TimeBasedTestRunData getReqTimeBasedTestRunData(boolean isAutoActivate, String startTime, String endTime, String phaseName, int granularity, int testRunDataType, boolean testViewMode, int interval, int numberOfDisplayPanels, String userName, GraphUniqueKeyDTO[] arrActiveGraphUniqueKeyDTO, TestRunDataType testRunDataTypeObj)
  {
    return getReqTimeBasedTestRunData(isAutoActivate, startTime, endTime, phaseName, granularity, testRunDataType, testViewMode, interval, numberOfDisplayPanels, userName, arrActiveGraphUniqueKeyDTO, testRunDataTypeObj, null);
  }

  public TimeBasedTestRunData getReqTimeBasedTestRunData(boolean isAutoActivate, String startTime, String endTime, String phaseName, int granularity, int testRunDataType, boolean testViewMode, int interval, int numberOfDisplayPanels, String userName, GraphUniqueKeyDTO[] arrActiveGraphUniqueKeyDTO, TestRunDataType testRunDataTypeObj, TestRunData testRunDataObject)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "getReqTimeBasedTestRunData", "", "", "Method Starts. Granularity = " + granularity + ", Test run data type = " + testRunDataType + ", startTime = " + startTime + ", endTime = " + endTime + ", phaseName = " + phaseName + ", testViewMode = " + testViewMode + ", interval = " + interval + ", isAutoActivate = " + isAutoActivate + ", arrActiveGraphUniqueKeyDTO = " + arrActiveGraphUniqueKeyDTO + ", testRun_Partition_Type = " + testRun_Partition_Type);

      long startProcessingTime = System.currentTimeMillis();
      boolean rtgFileFound = true;

      // Getting Time Based Data For NDE Sessions.
      // For NDE Mode we need to read data from different session files.
      if (testRun_Partition_Type > 0)
      {
	if(testRunDataTypeObj != null)
	  Log.debugLogAlways(className, "getReqTimeBasedTestRunData", "", "", "HashMap key = " + testRunDataTypeObj.getHMapKey());
	else
	  Log.debugLogAlways(className, "getReqTimeBasedTestRunData", "", "", "HashMap key = null comming");
	
        // TODO - Pre Processing Tasks.
        TestRunData testRunDataObj = getTestRunDataForSessions(granularity, startTime, endTime, testRunDataType, isAutoActivate, arrActiveGraphUniqueKeyDTO, numberOfDisplayPanels, userName, testViewMode, testRunDataTypeObj);

        
        if (testRunDataObj == null)
        {
          Log.debugLogAlways(userName, "getReqTimeBasedTestRunData", "", "", "TestRunData object must not be null. Please check error logs");
          return null;
        }
        else
        {
          // TODO - Post Processing Tasks.
          TimeBasedTestRunData timeBasedTestRunData = null;

          if (controllerTRNumber.equals("NA"))
          {
            Log.debugLog(userName, "getReqTimeBasedTestRunData", "", "", "Creating controller = " + generatorName + " ,TBRD object = " + timeBasedTestRunData);
            timeBasedTestRunData = testRunDataObj.getTimeBasedTestRunDataByKey(testRunDataTypeObj.getHMapKey());
          }
          else
          {
            Log.debugLog(userName, "getReqTimeBasedTestRunData", "", "", "Creating generator = " + generatorName + " ,TBRD object = " + timeBasedTestRunData);
            timeBasedTestRunData = testRunDataObj.getTimeBasedTestRunDataByKey(generatorName + "_" + testRunDataTypeObj.getHMapKey());
          }

          // Post Processing of time based data object.
          postProcessing(timeBasedTestRunData, startProcessingTime);
          
          if(debugLevel > 3)
            printTimeBasedTestRunData(timeBasedTestRunData);
          
          return timeBasedTestRunData;
        }
      }
      
      // If testRunData is not null, so GraphNames Object is already created
      if (testRunDataObject == null)
        rtgFileFound = openRTGMsgFile();
      else
      {
        // if generator name is null, so it is calling for controller
        if (generatorName == null)
        {
          Log.debugLogAlways(className, "getReqTimeBasedTestRunData", "", "", "Opening rtgMessage for Controller Test Run = " + numTestRun);
          rtgFileFound = openRTGMsgFile(testRunDataObject.graphNames);
        }
        else
        {
          if (debugLevel > 0)
            Log.debugLogAlways(className, "getReqTimeBasedTestRunData", "", "", "Opening rtgMessage for Generator Test Run = " + numTestRun + "(" + generatorName + ").");
          rtgFileFound = openRTGMsgFile(testRunDataObject.generatorGraphNames);
        }
      }

      if (!rtgFileFound)
        return null;

      // Here we need to calculate start and end Sequence for reading data from rtgMessage.dat file.
      calculateStartAndEndSequence(startTime, endTime, phaseName, testViewMode, interval, testRunDataTypeObj);
      // if (debugLevel > 0)
      Log.debugLogAlways(className, "getReqTimeBasedTestRunData", "", "", "Test Run = " + numTestRun + ", testViewMode = " + testViewMode + ", totalPkts = " + totalPkts + ", maxSeq = " + maxSeq + ", startSeq = " + startSeq + ", and endSeq = " + endSeq + " For TestRunDataType key = " + testRunDataTypeObj.getHMapKey() + " to getting all data pkt from rtgMessage.dat file.");
      data tempMsgData = new data();

      if (testRunDataObject == null)
      {
        if (controllerTRNumber.equals("NA"))
          testRunDataObject = new TestRunData(testRunDataType, numTestRun, startSeq, endSeq, granularity, numberOfDisplayPanels, userName, arrActiveGraphUniqueKeyDTO, testViewMode, graphNames, testRunDataTypeObj);
        else
          testRunDataObject = new TestRunData(testRunDataType, Integer.parseInt(controllerTRNumber), startSeq, endSeq, granularity, numberOfDisplayPanels, userName, arrActiveGraphUniqueKeyDTO, testViewMode, graphNames, testRunDataTypeObj);
      }

      TimeBasedTestRunData timeBasedTestRunData = new TimeBasedTestRunData(testRunDataObject, testRunDataTypeObj.getHMapKey(), testRunDataTypeObj);
      timeBasedTestRunData.initForNewTimeBasedTestRunData(granularity, arrActiveGraphUniqueKeyDTO, testViewMode);

      /* Setting Graph Names object in TBTRD object for generators. */
      if (generatorName != null && testRunDataObject.getGeneratorGraphNames() != null)
        timeBasedTestRunData.setGraphNamesObj(testRunDataObject.getGeneratorGraphNames());

      // Check For Derived DTO Availability.
      processDerivedGraphRequest(timeBasedTestRunData);

      // Now here we need to reset variable, telling the starting Sequence number for moving data in arrays while generating/updating Generating Last N Minute Data.
      if (testRunDataTypeObj.getType() == TestRunDataType.LAST_N_MINUTES_DATA)
      {
        // setting end Sequence as by activating graph/Getting Last N hour data we read data from rtgMessage.dat by calculating sequence number so no need to moving data in array.
        // but for updating time based data in online mode we need to set it to end sequence. and later again calculate it.
        timeBasedTestRunData.setMovingStartFromSeq((int) endSeq);
      }

      /**
       * Read Start Packet This flags are not use, because we are passing indexArray null so we are calculating graph stats for all index
       * 
       */
      // readOnePkt(msgData, prevMsgData, data.START_PKT, 0, null, minMaxFlag, avgFlag, stdDevFlag);
      // Here we need to ready only raw data, other stats are calculated while generating time based data.
      readRawDataPkt(msgData, prevMsgData, data.START_PKT, 0);

      int numberOfBytesToSkip = (int) (pktSize * (startSeq - 1));
      int bytesRead = dis.skipBytes(numberOfBytesToSkip);

      Log.debugLog(className, "getReqTimeBasedTestRunData", "", "", "Total Skip Bytes = " + bytesRead + ", startSeq = " + startSeq + ", endSeq = " + endSeq);

      for (int seqNum = (int) startSeq; seqNum <= endSeq; seqNum++)
      {
        if ((tempMsgData = readRawDataPkt(msgData, prevMsgData, data.DATA_PKT, seqNum)) == null)
          continue;

        timeBasedTestRunData.putTimeBasedTestRunData(tempMsgData);
      }

      // Here we need to reset Moving variable if TestRunDataType is For Last N Minute.
      if (testRunDataTypeObj.getType() == TestRunDataType.LAST_N_MINUTES_DATA)
      {
        int lastNMinutesDuration = testRunDataTypeObj.getLastNMinutesValue() * 60 * 1000;
        int movingStartFromSeq = lastNMinutesDuration / interval;

        // here we need to reset moving control variable to updating in online mode.
        timeBasedTestRunData.setMovingStartFromSeq(movingStartFromSeq);
      }

      testRunDataObject.testRunIsOver();
      closeRTGMsgFile();

      // Post Processing of time based data object.
      postProcessing(timeBasedTestRunData, startProcessingTime);

      return timeBasedTestRunData;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getReqTimeBasedTestRunData", "", "", "Exception - ", e);
      return null;
    }
  }

  
  private void printTimeBasedTestRunData(TimeBasedTestRunData timeBasedTestRunData)
  {
    try
    {
      GraphUniqueKeyDTO[] arrGraphUniqueKeyDTOs = timeBasedTestRunData.getArrGraphUniqueKeyDTO();
      for(int i = 0; i < arrGraphUniqueKeyDTOs.length; i++)
      {
	GraphUniqueKeyDTO graphUniqueKeyDTO = arrGraphUniqueKeyDTOs[i];
	TimeBasedDTO timeBasedDTO = timeBasedTestRunData.getTimeBasedDTO(graphUniqueKeyDTO);
	Log.debugLogAlways(className, "printTimeBasedTestRunData", "", "", "timeBasedDTO = " + timeBasedDTO);
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  
  /**
   * Check and Process Derived Graphs Request in {@link DerivedDataDTO}
   * 
   * @param timeBasedTestRunData
   */
  private void processDerivedGraphRequest(TimeBasedTestRunData timeBasedTestRunData)
  {
    try
    {
      if (derivedDataDTO == null)
        return;

      for (int i = 0; i < derivedDataDTO.getArrDerivedGraphFormula().length; i++)
        System.out.println("Testrun = " + numTestRun + " , Name = " + derivedDataDTO.getArrDerivedGraphName()[i] + ", Number = " + derivedDataDTO.getArrDerivedGraphNumber()[i] + ", Formula = " + derivedDataDTO.getArrDerivedGraphFormula()[i]);

      // Initializing Derived Data Processor object.
      timeBasedTestRunData.getDerivedDataProcessor().init(derivedDataDTO, timeBasedTestRunData);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Post Processing Tasks and Decorating Time Based Data object.
   * 
   * @param timeBasedTestRunData
   * @param startProcessingTime
   */
  private void postProcessing(TimeBasedTestRunData timeBasedTestRunData, long startProcessingTime)
  {
    try
    {
      /* No Need to do object values null if reporting mode is enabled. */
      if (reporting)
        return;

      /* Logging Time Based Information. */
      if (timeBasedTestRunData != null && debugLevel > 1)
        timeBasedTestRunData.logInfo();

      timeBasedTestRunData.setMsgData(null);
      timeBasedTestRunData.setTestRunDataObj(null);
      timeBasedTestRunData.setGraphNamesObj(null);
      Log.debugLog(className, "getReqTimeBasedTestRunData:postProcessing", "", "", "MsgData is not required. So making it to null in timeBasedTestRunData. Also making arrGraphNumberToSampleDataArray and testrun data null. ");
      timeBasedTestRunData.setTotalProcessingTime(startProcessingTime, System.currentTimeMillis());
      Log.debugLog(className, "getReqTimeBasedTestRunData:postProcessing", "", "", "Processing Time for creating timeBasedTestRunData = " + timeBasedTestRunData.getTotalProcessingTime() + " Secs.");

      if (debugLevel > 1)
      {
        Log.debugLogAlways(className, "getReqTimeBasedTestRunData:postProcessing", "", "", "Size of Time Based Object to send RTG GUI in bytes = " + PartitionInfoUtils.getObjectSizeInBytes(timeBasedTestRunData));

        if (timeBasedTestRunData != null)
          Log.debugLogAlways(className, "getReqTimeBasedTestRunData:postProcessing", "", "", "Size of TimeBasedDTO Object HashMap in bytes = " + PartitionInfoUtils.getObjectSizeInBytes(timeBasedTestRunData.getHmTimeBasedDTO()));
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "postProcessing", "", "", "Exception - ", e);
    }
  }

  /**
   * Getting TestRunData Object for NDE Mode include user requested data.
   * 
   * @param granularity
   * @param startTime
   * @param endTime
   * @param testRunType
   * @param isAutoActivateMode
   * @param arrActiveGraphNumbers
   * @param numberOfPanels
   * @param userName
   * @param testViewMode
   * @param testRunDataTypeObj
   * @return
   */
  public TestRunData getTestRunDataForSessions(int granularity, String startTime, String endTime, int testRunType, boolean isAutoActivateMode, GraphUniqueKeyDTO[] arrActiveGraphUniqueKeyDTO, int numberOfPanels, String userName, boolean testViewMode, TestRunDataType testRunDataTypeObj)
  {
    try
    {
      long absStartTime = 0L;
      long absEndTime = 0L;

      // Checking for Specified Time Request.
      if (testRunDataTypeObj.getType() == TestRunDataType.SPECIFIED_PHASE_OR_TIME)
      {
        try
        {
          absStartTime = Long.parseLong(startTime);
          absEndTime = Long.parseLong(endTime);
        }
        catch (Exception e)
        {
          Log.errorLog(className, "getTestRunDataForSessions", "", "", "Exception - " + e.getMessage());
        }
      }
      TestRunData testRunDataObj = null;

      // Creating TestRunData object.
      if (controllerTRNumber.equals("NA"))
        testRunDataObj = new TestRunData(numTestRun, granularity, absStartTime, absEndTime, numberOfPanels, userName, arrActiveGraphUniqueKeyDTO, testViewMode, testRunDataTypeObj, "", null, derivedDataDTO);
      else
        testRunDataObj = new TestRunData(Integer.parseInt(controllerTRNumber), granularity, absStartTime, absEndTime, numberOfPanels, userName, arrActiveGraphUniqueKeyDTO, testViewMode, testRunDataTypeObj, "", null, derivedDataDTO);

      // TODO - Post Processing Tasks.
      return testRunDataObj;

    }
    catch (Exception e)
    {
      Log.stackTraceLog(userName, "getTestRunDataForSessions", "", "", "Exception - ", e);
      return null;
    }
  }

  /**
   * calStartEndSequence: this method returns Start Seq, End Seq
   * 
   * @param time
   * @param startTime
   * @param endTime
   * @return
   */
  public boolean calStartEndSequence(String phaseName, String startTime, String endTime, int interval)
  {
    try
    {
      Log.debugLog(className, "calStartEndSequence", "", "", "Method called. phaseName is  " + phaseName + ", Start Time is " + startTime + ", End Time is " + endTime + ", interval =" + interval);
      long numStartTime = 0;
      long numEndTime = 0;

      // for total run only the start & end times not used ,only phase name used
      if ((!startTime.equals("NA")) && (endTime.equals("NA")))
      {
        numStartTime = rptUtilsBean.convStrToMilliSec(startTime.trim()); // First data packet seq is 1
        startSeq = numStartTime / interval;
        if (numStartTime % interval != 0)
          startSeq++;
        if (numStartTime == 0)
          startSeq = 1;

        endSeq = maxSeq;
      }
      else if ((startTime.equals("NA")) && (!endTime.equals("NA")))
      {
        startSeq = 1; // First data packet seq is 1
        numEndTime = rptUtilsBean.convStrToMilliSec(endTime.trim());
        endSeq = numEndTime / interval;
        Log.debugLog(className, "calStartEndSequence", "", "", "As End Time is corrupted so taking test Run End Time as specified end time");
      }
      else if ((startTime.equals("NA")) && (endTime.equals("NA")))
      {
        startSeq = 1; // First data packet seq is 1
        endSeq = maxSeq;
      }
      else
      {
        numStartTime = rptUtilsBean.convStrToMilliSec(startTime.trim());
        numEndTime = rptUtilsBean.convStrToMilliSec(endTime.trim());
        // Make sure if startSeq is fractional, then round to next sequence so
        // that we do not include sample which is in before start time or RampUp/WarmUp phase
        startSeq = numStartTime / interval;

        if (numStartTime % interval != 0)
          startSeq++;
        // If endSeq is fractional, no need to round as it is already in time before end time
        endSeq = numEndTime / interval;

        if (numStartTime == 0)
          startSeq = 1;
      }
      return true;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "calStartEndSequence", "", "", "Error while calculating range of packets " + e);
      return false;
    }
  }

  // calculate avg data on the basis of time option and graphDataType
  // this is used to show calculated avg data in legend in ReportGeneration
  public double calcAndGetAvgDataBySeq(double[][] arrGraphData, int[] arrRptGraphDataIdx, int graphDataIdx, String timeOption, String elapsedStartTime, String elapsedEndTime, int graphDataType, boolean overrideRptOptions)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "calcAndGetAvgDataBySeq", "", "", "Method called.");
      // get report data by index
      double[] arrRptData = rptUtilsBean.getDataFromArrayByIdx(arrRptGraphDataIdx, arrGraphData, graphDataIdx);
      double[] arrCurCountReportData = null;
      if ((graphDataType == GraphNameUtils.DATA_TYPE_TIMES) || (graphDataType == GraphNameUtils.DATA_TYPE_TIMES_STD))
        arrCurCountReportData = rptUtilsBean.getDataFromArrayByIdx(arrRptGraphDataIdx, arrCurCountAll, graphDataIdx);

      // get start & end seq number by time options
      int[] rangeDateForArr = calRangeForData(timeOption, elapsedStartTime, elapsedEndTime);
      int startSequence = rangeDateForArr[0];
      int endSequence = rangeDateForArr[1];

      // get curCountIndex to graphNames
      int[] curCountIndex = graphNames.getCurCountIndx();
      double avgDataValueBySeq = msgData.calcAndGetAvgDataBySeq(arrRptData, arrCurCountReportData, curCountIndex[graphDataIdx], startSequence, endSequence, graphDataType, overrideRptOptions);
      return avgDataValueBySeq;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "calcAndGetAvgDataBySeq", "", "", "Exception - ", e);
      return -1;
    }
  }

  // Calculate average data by given options (timeSelectionFormat, startTime, endTime, graphDataType)
  public double getAvgDataBySequenceOnly(double arrAvgValues[], int graphDataType)
  {
    return (getAvgDataBySequenceOnly(arrAvgValues, graphDataType, true));
  }

  // Calculate average data by given options (timeSelectionFormat, startTime, endTime, graphDataType)
  public double getAvgDataBySequenceOnly(double arrAvgValues[], int graphDataType, boolean takeDiffFlag)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "getAvgDataBySequenceOnly", "", "", "Method called. GraphDataType = " + graphDataType + ", Array of avg values = " + rptUtilsBean.doubleArrayToList(arrAvgValues) + ", takeDiffFlag = " + takeDiffFlag);
      double avgDataValueBySeq = -1;

      if (graphDataType == GraphNameUtils.DATA_TYPE_CUMULATIVE)
      {
        if (takeDiffFlag)
          avgDataValueBySeq = arrAvgValues[arrAvgValues.length - 1] - arrAvgValues[0];
        else
          avgDataValueBySeq = arrAvgValues[arrAvgValues.length - 1];
      }
      else
        avgDataValueBySeq = arrAvgValues[arrAvgValues.length - 1];

      if (debugLevel > 0)
        Log.debugLogAlways(className, "getAvgDataBySequenceOnly", "", "", "Avg data by sequence = " + avgDataValueBySeq);

      return data.convTo3DigitDecimal(avgDataValueBySeq);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getAvgDataBySequenceOnly", "", "", "Exception - ", e);
      return -1;
    }
  }

  // Calculate Std-Dev data by given options (timeSelectionFormat, startTime, endTime)
  public double getStdDevBySequenceOnly(double arrStdDevValues[])
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "getStdDevBySequenceOnly", "", "", "Method called. Array of std-dev values = " + rptUtilsBean.doubleArrayToList(arrStdDevValues));

      double stdDevDataValueBySeq = arrStdDevValues[arrStdDevValues.length - 1];

      if (debugLevel > 0)
        Log.debugLogAlways(className, "getStdDevBySequenceOnly", "", "", "Std-Dev data by sequence = " + stdDevDataValueBySeq);

      return data.convTo3DigitDecimal(stdDevDataValueBySeq);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getStdDevBySequenceOnly", "", "", "Exception - ", e);
      return -1;
    }
  }

  // get test run raw data (without processing)
  public double[][] getLoadRowData()
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "getLoadRowData", "", "", "Method Called.");

      openRTGMsgFile();
      int bytesRead = (int) pktSize;
      byte byteBuf[] = new byte[(int) pktSize];
      ArrayList<double[]> arrRawData = new ArrayList<double[]>();
      int rawDataLen = ((int) pktSize) / 8;

      if (debugLevel > 0)
        Log.debugLogAlways(className, "getLoadRowData", "", "", "Method Called. packet size =" + pktSize + " Bytes read = " + bytesRead);

      int count = 0;
      while (bytesRead != -1)
      {
        bytesRead = dis.read(byteBuf);
        ByteBuffer bb;
        bb = ByteBuffer.wrap(byteBuf);
        bb = bb.order(ByteOrder.LITTLE_ENDIAN);

        int index = 0;
        double[] rawData = new double[rawDataLen];
        for (int i = 0; i < rawDataLen; i++)
        {
          rawData[i] = bb.getDouble(index);
          index = index + 8;
        }

        if (count != 0)
          arrRawData.add(rawData);

        count++;
      }

      double[][] arrTestRunData = new double[arrRawData.size() - 1][rawDataLen];
      for (int i = 0; i < arrRawData.size() - 1; i++)
      {
        double[] arrData = arrRawData.get(i);
        for (int j = 0; j < rawDataLen; j++)
        {
          arrTestRunData[i][j] = arrData[j];
        }
      }
      closeRTGMsgFile();
      return arrTestRunData;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getLoadRowData", "", "", "Exception - ", ex);
      return null;
    }
  }

  public static void main(String[] args)
  {
    try
    {
      long startTime = System.currentTimeMillis();
      int testNum = 3661;
      // Config.addConfigParam("debugFlag", args[1]);
      ReportData rptData = new ReportData(testNum);
      GraphNames graphName = rptData.createGraphNamesObj();
      System.out.println("************** graphName = " + graphName.getGraphUniqueKeyDTO().length);
      
      // rptData.setTestRun_Partition_Type(1);

      DerivedDataDTO derivedDTO = new DerivedDataDTO();
      /*
       * String[] graphNames = {"Available Memory","Free Memory","CPU Utilization","System CPU Busy","User CPU Busy","Established","JVM Heap Used Memory","Memory Used","CPU Used"}; String[] graphFormulas = {"(10108.4.[AppServer1] + 10108.4.[AppServer2] + 10108.4.[AppServer3] + 10108.4.[AppServer4] + 10108.4.[Coherence] + 10108.4.[CSCSE] + 10108.4.[EndITL] + 10108.4.[EndMDX1] + 10108.4.[EndMDX2] + 10108.4.[EndMDX4] + 10108.4.[EndMDX5] + 10108.4.[InvMed] + 10108.4.[NSAppliance] + 10108.4.[OrderG] + 10108.4.[OrderM])/15" , "(10108.5.[AppServer1] + 10108.5.[AppServer2] + 10108.5.[AppServer3] + 10108.5.[AppServer4] + 10108.5.[Coherence] + 10108.5.[CSCSE] + 10108.5.[EndITL] + 10108.5.[EndMDX1] + 10108.5.[EndMDX2] + 10108.5.[EndMDX4] + 10108.5.[EndMDX5] + 10108.5.[InvMed] + 10108.5.[NSAppliance] + 10108.5.[OrderG] + 10108.5.[OrderM])/15", "(10108.18.[AppServer1] + 10108.18.[AppServer2] + 10108.18.[AppServer3] + 10108.18.[AppServer4] + 10108.18.[Coherence] + 10108.18.[CSCSE] + 10108.18.[EndITL] + 10108.18.[EndMDX1] + 10108.18.[EndMDX2] + 10108.18.[EndMDX4] + 10108.18.[EndMDX5] + 10108.18.[InvMed] + 10108.18.[NSAppliance] + 10108.18.[OrderG] + 10108.18.[OrderM])/15", "(10108.15.[AppServer1] + 10108.15.[AppServer2] + 10108.15.[AppServer3] + 10108.15.[AppServer4] + 10108.15.[Coherence] + 10108.15.[CSCSE] + 10108.15.[EndITL] + 10108.15.[EndMDX1] + 10108.15.[EndMDX2] + 10108.15.[EndMDX4] + 10108.15.[EndMDX5] + 10108.15.[InvMed] + 10108.15.[NSAppliance] + 10108.15.[OrderG] + 10108.15.[OrderM])/15", "(10108.14.[AppServer1] + 10108.14.[AppServer2] + 10108.14.[AppServer3] + 10108.14.[AppServer4] + 10108.14.[Coherence] + 10108.14.[CSCSE] + 10108.14.[EndITL] + 10108.14.[EndMDX1] + 10108.14.[EndMDX2] + 10108.14.[EndMDX4] + 10108.14.[EndMDX5] + 10108.14.[InvMed] + 10108.14.[NSAppliance] + 10108.14.[OrderG] + 10108.14.[OrderM])/15", "(10013.1.[AppServer1] + 10013.1.[AppServer2] + 10013.1.[AppServer3] + 10013.1.[AppServer4] + 10013.1.[Coherence] + 10013.1.[CSCSE] + 10013.1.[EndITL] + 10013.1.[EndMDX1] + 10013.1.[EndMDX2] + 10013.1.[EndMDX4] + 10013.1.[EndMDX5] + 10013.1.[InvMed] + 10013.1.[NSAppliance] + 10013.1.[OrderG] + 10013.1.[OrderM])/15", "(10014.42.[AS1_esstf02] + 10014.42.[AS2_esstf04] + 10014.42.[AS3_esstf06] + 10014.42.[AS4_esstf08] + 10014.42.[Coh_gc_01] + 10014.42.[Coh_gc_02] + 10014.42.[OrderM_1] + 10014.42.[OrderM_2] + 10014.42.[OrderM_3] + 10014.42.[OrderM_4] + 10014.42.[OrderG_1] + 10014.42.[OrderG_2] + 10014.42.[OrderG_3] + 10014.42.[OrderG_4] + 10014.42.[OrderG_5] + 10014.42.[OrderG_6])/16", "(10117.6.[AS1_esstf1-02] + 10117.6.[AS1_cmon] + 10117.6.[AS2_esstf1-04] + 10117.6.[AS2_cmon] + 10117.6.[AS3_esstf1-06] + 10117.6.[AS3_cmon] + 10117.6.[AS4_esstf1-08] + 10117.6.[AS4_cmon] + 10117.6.[EndMDX1_cmon] + 10117.6.[EndMDX2_cmon] + 10117.6.[EndMDX4_cmon] + 10117.6.[EndMDX5_cmon] + 10117.6.[InvMed_cmon] + 10117.6.[InvMed_gc_3] + 10117.6.[InvMed_gc_4] + 10117.6.[COH_cmon] + 10117.6.[AS1_esatg1_coh_dat_svr_01] + 10117.6.[AS1_esatg1_coh_dat_svr_02] + 10117.6.[NSAppliance_CMON] + 10117.6.[NSAppliance_LPS])/20", "(10117.5.[AS1_esstf1-02] + 10117.5.[AS1_cmon] + 10117.5.[AS2_esstf1-04] + 10117.5.[AS2_cmon] + 10117.5.[AS3_esstf1-06] + 10117.5.[AS3_cmon] + 10117.5.[AS4_esstf1-08] + 10117.5.[AS4_cmon] + 10117.5.[EndMDX1_cmon] + 10117.5.[EndMDX2_cmon] + 10117.5.[EndMDX4_cmon] + 10117.5.[EndMDX5_cmon] + 10117.5.[InvMed_cmon] + 10117.5.[InvMed_gc_3] + 10117.5.[InvMed_gc_4] + 10117.5.[COH_cmon] + 10117.5.[AS1_esatg1_coh_dat_svr_01] + 10117.5.[AS1_esatg1_coh_dat_svr_02] + 10117.5.[NSAppliance_CMON] + 10117.5.[NSAppliance_LPS])/20" }; int[] graphNumbers = {250002,250003,250005,250007,250009,250011,250013,250015,250017};
       */

      String[] graphNames = { "Report", "Report2" };
      String[] graphFormulas = { "1.1.NA + 1.2.NA", "1.3.NA + 1.4.NA" };
      int[] graphNumbers = { 250001, 250002 };

      derivedDTO.setArrDerivedGraphFormula(graphFormulas);
      derivedDTO.setArrDerivedGraphName(graphNames);
      derivedDTO.setArrDerivedGraphNumber(graphNumbers);
      // rptData.setDerivedDataDTO(derivedDTO);

      // DerivedGraphTesting derivedGraphTesting = new DerivedGraphTesting();
      // derivedGraphTesting.graphNames = new GraphNames(2405);

      // System.out.println("graphNames = "+derivedGraphTesting.graphNames);

      // GraphUniqueKeyDTO[] graphUniqueKeyDTO = derivedGraphTesting.getUniqueGraphNumbersFromExpressionList(graphFormulas);

      int arrGraphIndex[] = { 0, 1, 3, 4, 5 };
      GraphUniqueKeyDTO[] graphUniqueKeyDTO1 =  null;//consoleInterface.getGraphDTOArray(testNum, "");
      // System.out.println("Total Graphs in GDF = " + graphUniqueKeyDTO1.length);

      // GraphUniqueKeyDTO[] graphUniqueKeyDTO = new GraphUniqueKeyDTO[1];
      // graphUniqueKeyDTO[0] = graphUniqueKeyDTO1[208];
      // graphUniqueKeyDTO[1] = graphUniqueKeyDTO1[209];
      // for(int i = 167; i < graphUniqueKeyDTO1.length; i++)
      {
        // System.out.println(" i = " + i + ", group Id = " + graphUniqueKeyDTO1[i].getGroupId());
        // graphUniqueKeyDTO[i - 167] = graphUniqueKeyDTO1[i];
      }

      LinkedHashMap<Integer, GraphUniqueKeyDTO> hm = new LinkedHashMap<Integer, GraphUniqueKeyDTO>();

      // for(int i = 0; i < graphUniqueKeyDTO1.length; i++)
      {
        // System.out.println();
      }
      graphUniqueKeyDTO1 = graphName.getGraphUniqueKeyDTO();

      System.out.println("Total Graph DTO available = " + graphUniqueKeyDTO1.length);

      String startDateMillies = 1398894383872L + "";
      String endDateMillies = 1398899543962L + "";

      //TestRunDataType testRunDataTypeObj = new TestRunDataType(TestRunDataType.WHOLE_SCENARIO_DATA, -1, -1, startDateMillies, endDateMillies, false, false);
      TestRunDataType testRunDataTypeObj = new TestRunDataType(TestRunDataType.LAST_N_MINUTES_DATA, -1, 10, startDateMillies, endDateMillies, false, false);

      
      // rptData.setDerivedDataDTO(derivedDataDTO);
      TimeBasedTestRunData timeBasedTestRunData = rptData.getReqTimeBasedTestRunData(true, "NA", "NA", "NA", -1, 1, true, 10000, 9, "netstorm", graphUniqueKeyDTO1, testRunDataTypeObj);
      //rptData.getTimeBasedDataFromRTGFile(hm, "Total Run", "NA", "NA", true, false);
      //rptData.getTimeBasedDataFromRTGFile(hm, "Last_30_Minutes", "NA", "NA", true, false, testRunDataTypeObj);

      if (timeBasedTestRunData == null)
      {
        System.out.println("Found Null.............");
      }
      else
      {
        System.out.println("Not null");
      }

      TimeZone tm = ExecutionDateTime.getSystemTimeZoneGMTOffset();
      Calendar calender = Calendar.getInstance(tm);
      calender.setTimeInMillis(1398857034000L);
      int msec = calender.get(Calendar.MILLISECOND);
      int sec = calender.get(Calendar.SECOND);
      int minute = calender.get(Calendar.MINUTE);
      int hour = calender.get(Calendar.HOUR_OF_DAY);
      int date = calender.get(Calendar.DATE);
      int month = calender.get(Calendar.MONTH);
      int year = calender.get(Calendar.YEAR);

      System.out.println("mm/dd/yy HH:mm:ss.ms -> " + month + "/" + date + "/" + year + " " + hour + ":" + minute + ":" + sec + "." + msec);

      System.out.println("Graphs in TBTRD = " + timeBasedTestRunData.getArrGraphUniqueKeyDTO().length);

      System.out.println("Total Sample = " + timeBasedTestRunData.getDataItemCount());

      System.out.println(" ******************************************  Sequence Array **********************************************");
      for (int k = 0; k < timeBasedTestRunData.arrTimeStamp.length; k++)
      {
        System.out.print(timeBasedTestRunData.arrTimeStamp[k] + "L, ");
      }

      System.out.println();
      System.out.println(" ******************************************  Time Stamp Array **********************************************");
      for (int k = 0; k < timeBasedTestRunData.getDataItemCount(); k++)
      {
        System.out.print(ExecutionDateTime.convertDateTimeStampToFormattedString(timeBasedTestRunData.arrTimeStamp[k], "MM/dd/yy HH:mm:ss", tm) + ", ");
      }

      System.out.println("--------------------------------------------------------------");

      //RelativeDateFormat rdf = new RelativeDateFormat(timeBasedTestRunData.testRunData.trStartTimeStamp);

      for (int k = 0; k < timeBasedTestRunData.getDataItemCount(); k++)
      {
        long diffMillies = timeBasedTestRunData.arrTimeStamp[k] - 1373912141000L;
        System.out.print(ExecutionDateTime.convertTimeToFormattedString(diffMillies, ":") + ", ");
      }

      // TimeBasedDTO timeBasedDTOObj = timeBasedTestRunData.getTimeBasedDTO(timeBasedTestRunData.arrGraphUniqueKeyDTO[0]);

      // System.out.println("\n\n################## length = " + timeBasedTestRunData.hmTimeBasedDTO.size());
      System.out.println();
      System.out.println("#############################################  Derived Graph DAta .......................... ");

      for (int i = 0; i < timeBasedTestRunData.getDerivedDataProcessor().getArrDerivedGraphInfo().size(); i++)
      {
        DerivedGraphInfo derG = timeBasedTestRunData.getDerivedDataProcessor().getArrDerivedGraphInfo().get(i);
        /*
         * System.out.println("name = " + derG.getDerivedGraphName() + ", Formula = " + derG.getDerivedGraphFormula()); for(int k = 0; k < derG.getArrDerivedSampleData().length; k++) System.out.print(derG.getArrDerivedSampleData()[k] + ",\t"); System.out.println();
         */
      }

      //System.out.println("tr Time Stamp = " + timeBasedTestRunData.testRunData.trStartTimeStamp);

      System.out.println("----------------------> " + ExecutionDateTime.convertDateTimeStampToFormattedString(1398857034000L, "MM/dd/yy HH:mm:ss", tm));

      System.out.println("*********************************************** Graph Sample DAta  *********************************");
      for (int k = 0; k < timeBasedTestRunData.getArrGraphUniqueKeyDTO().length; k++)
      {
        GraphUniqueKeyDTO graphUniqueKeyDTO2 = timeBasedTestRunData.getArrGraphUniqueKeyDTO()[k];
        System.out.println("Graph = " + graphUniqueKeyDTO2);
        TimeBasedDTO timeBasedDTOObj = timeBasedTestRunData.getTimeBasedDTO(graphUniqueKeyDTO2);
        // System.out.println("");
        // System.out.println("dataIndex = " + rptData.graphNames.getGraphDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTO2) +", Graph Name = " + rptData.graphNames.getGraphNameByGraphUniqueKeyDTO(graphUniqueKeyDTO2, true) + ", Group Id = " + graphUniqueKeyDTO2.getGroupId() + ", Graph Id = " + graphUniqueKeyDTO2.getGraphId() + ", Vector Name = " + graphUniqueKeyDTO2.getVectorName());
        for (int i = 0; i < timeBasedTestRunData.getDataItemCount(); i++)
        {
          // System.out.print(timeBasedDTOObj.getArrGraphSamplesData()[i] + ",\t");
           System.out.println("Sample Data = " + timeBasedDTOObj.getArrGraphSamplesData()[i] + ", countData = " + timeBasedDTOObj.getArrCountData()[i] + ", sum sqr = " + timeBasedDTOObj.getArrSumSqrData()[i]);
        }
         System.out.println();
         System.out.println("----------------------------------------------------------");
        // System.out.println();

      }

      System.out.println("****************************************************************************************************");

      ReportDataUtils reportDataUtilsObj = new ReportDataUtils(rptData);
      System.out.println("-------------------------------------------------------------------------------------------");
      System.out.println();

      // reportDataUtilsObj.calculateParametersForAveraging(timeBasedDTOObj.getArrGraphSamplesData(), timeBasedDTOObj.getArrSumSqrData(), timeBasedDTOObj.getArrCountData(), timeBasedTestRunData, "Normal", 0, 0+"", "Elapsed", true, "Total Run", "NA", "NA", 6);

      for (int i = 0; i < reportDataUtilsObj.getSampleCount(); i++)
      {
        System.out.println(" i = " + i + ", Sample DAta = " + reportDataUtilsObj.getAverageSampleArray()[i] + ", time stamp = " + ExecutionDateTime.convertDateTimeStampToFormattedString(reportDataUtilsObj.getAveragTimeStampArray()[i], "MM/dd/yy HH:mm:ss", tm));
      }

      
      System.out.println("Total Time Taken = " + ExecutionDateTime.convertTimeToFormattedString((System.currentTimeMillis() - startTime), ":"));
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}

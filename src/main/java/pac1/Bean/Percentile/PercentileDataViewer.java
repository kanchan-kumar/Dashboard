/**
 * This class is for unit testing of percentile Graphs. As it is showing 1 - 100 percentile of graphs which are present in testrun.pdf
 * 
 * @author Ravi Kant Sharma
 * @since Netsorm Version 4.0.0
 * @Modification_History Ravi Kant Sharma - Initial Version 4.0.0
 * @version 4.0.0
 * 
 */

package pac1.Bean.Percentile;

import java.io.*;
import java.nio.*;
import java.util.*;

import pac1.Bean.Config;
import pac1.Bean.GraphPCTData;
import pac1.Bean.GraphUniqueKeyDTO;
import pac1.Bean.Log;
import pac1.Bean.PDFNames;
import pac1.Bean.rptUtilsBean;
import pac1.Bean.GraphName.GraphNames;

public class PercentileDataViewer
{
  private String className = "PercentileDataViewer";
  private int numTestRun = -1;
  private final String PCT_FILE_NAME = "pctMessage.dat";

  private int interval = -1;
  private PDFNames pdfNames = null;
  private GraphNames graphNames = null;
  private GraphPCTData[] graphPCTData = null;

  private int modeType = 0; // It is 0- Totel Run, 1- Run Phase, 2- Specified
  private int startSeq = 0;
  private int endSeq = 0;
  private int maxSeq = 0;
  private double currSeqNumber; // use to store current seqNumber

  private transient final int SEQ_NUM_LOWER_INDEX = 0;
  private transient final int SEQ_NUM_UPPER_INDEX = 1;

  // size of each granule is long long means 8 bytes
  private transient final int GRANULE_SIZE = 8;
  // size of header is 4 long long means 32 bytes
  private transient final int PROCESS_HDR_LEN = 32;

  private double rawPCTData[] = null;
  private double pctData[] = null;

  private int rawDataLen = 0;
  private int msgHdrLen = 0;
  private int processedDataLen = 0;
  private int pktSize = 0;
  private double fileSize = 0; // file size in bytes
  private int totalPkts = 0;
  private FileInputStream fis = null;
  private DataInputStream dis = null;

  public static int TIME_OPTION_EMPTY = -1;
  public static int TOTAL_RUN = 0;
  public static int RUN_PHASE_ONLY = 1;
  public static int SPECIFIED_TIME = 2;
  public static int TIME_OPTION = -1;
  private double phaseTimes[] = null;

  public PercentileDataViewer(int numTestRun)
  {
    this("Total Run", numTestRun);
  }

  public PercentileDataViewer(String timeOption, int numTestRun)
  {
    this.numTestRun = numTestRun;
    if (timeOption == null || timeOption.trim().equals(""))
      TIME_OPTION = -1;
    else if (timeOption.equals("Total Run"))
      TIME_OPTION = TOTAL_RUN;
    else if (timeOption.equals("Run Phase Only"))
      TIME_OPTION = RUN_PHASE_ONLY;

    initPercentileViewer();
    openPCTMsgFile();
  }

  private void initPercentileViewer()
  {
    try
    {
      graphNames = new GraphNames(numTestRun);
      pdfNames = graphNames.getPdfNames();
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "initPercentileViewer", "", "", "Exception - ", e);
    }
  }

  // This will open PCT message file and initialize objects and variables related to the test run.
  public boolean openPCTMsgFile()
  {
    try
    {
      String strPctFile = getPCTFileNameWithPath();
      Log.debugLogAlways(className, "openPCTMsgFile", "", "", "Method called. Test Run is  " + numTestRun);

      File pctFileObj = new File(strPctFile);
      if (!pctFileObj.exists()) // file does not exist
      {
        Log.errorLog(className, "openPCTMsgFile", "", "", "File " + strPctFile + " does not exist.");
        return false;
      }

      if (fis != null) // file is already open
        return true;

      initPercentileData();
      pktSize = pdfNames.getSizeOfPctMsgData();
      fis = new FileInputStream(strPctFile);
      dis = new DataInputStream(fis);
      fileSize = pctFileObj.length();
      if (fileSize < pktSize) // Make sure at least full packet is in the file
      {
        Log.errorLog(className, "openPCTMsgFile", "", "", "pctMessage.dat file is corrupted. Size < one packet size");
        return false;
      }

      totalPkts = (int) fileSize / pktSize;

      if (pdfNames.getEndSeqNum() == -1)
      {
        // END line not append in testrun.pdf, in mode 0(Total time) & 1(Phase time). Use totalPkts as max sequence
        maxSeq = totalPkts;
      }
      else
      {
        // This the case of specified interval mode
        maxSeq = pdfNames.getEndSeqNum();
      }

      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "openPCTMsgFile", "", "", "Exception while reading data file - ", e);
      return false;
    }
  }

  // this function close opened data streams
  public void closePCTMsgFile()
  {
    try
    {
      Log.debugLogAlways(className, "closePCTMsgFile", "", "", "Method called");

      if (dis == null)
        return;

      dis.close();
      fis.close();
      dis = null;
      fis = null;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "openPCTMsgFile", "", "", "Exception while closing data file - ", e);
    }
  }

  // Add function for returning absolute path of percentile message file
  public String getPCTFileNameWithPath()
  {
    return (Config.getWorkPath() + "/webapps/logs/TR" + this.numTestRun + "/" + PCT_FILE_NAME);
  }

  // This is initializes all variables
  private void initPercentileData()
  {
    Log.debugLogAlways(className, "initPercentileData", "", "", "Method Starts");
    rawDataLen = (pdfNames.getSizeOfPctMsgData() / GRANULE_SIZE);
    msgHdrLen = (PROCESS_HDR_LEN / GRANULE_SIZE);
    processedDataLen = rawDataLen - msgHdrLen;
    modeType = pdfNames.getModeType();
    interval = pdfNames.getInterval();
    Log.debugLogAlways(className, "initPercentileData", "", "", "Raw Data Length = " + rawDataLen + ", Processed Data Length = " + processedDataLen + ", Mode Type = " + modeType + ", Interval = " + interval);
  }

  // process header for long type pctMessage data
  public void processHdr(byte recvData[])
  {
    ByteBuffer bb = ByteBuffer.wrap(recvData, 0, PROCESS_HDR_LEN);
    bb = bb.order(ByteOrder.LITTLE_ENDIAN);
    currSeqNumber = bb.getLong(0);
    long active = bb.getLong(8);
    long timeStamp = bb.getLong(16);
    long future = bb.getLong(24);
  }

  // Treats integer as unsigned and converts to positive long
  public static long intToPosLong(int intNum)
  {
    long longNum = intNum;
    if (longNum < 0)
      longNum += Math.pow(2, 32);
    return (longNum);
  }

  // this function return long long to double
  public double conv2LToDouble(long lower, long upper)
  {
    return ((double) ((upper << 32) + lower));
  }

  // Make double value up to 3 digit decimal
  public static double convTo3DigitDecimal(double val)
  {
    double dblVal;
    dblVal = (double) Math.round(val * 1000);
    dblVal = dblVal / 1000;
    return (dblVal);
  }

  public GraphPCTData[] getAllGraphsDataFromPCTMsgFile()
  {
    GraphUniqueKeyDTO[] arrGraphUniqueKeyDTO = graphNames.getGraphUniqueKeyDTO();
    ArrayList<Integer> pdfIdList = new ArrayList<Integer>();
    ArrayList<Long> pdfDataIndexList = new ArrayList<Long>();

    if (arrGraphUniqueKeyDTO == null)
    {
      System.out.println("Test Run may not exist...");
      return null;
    }

    for (int i = 0; i < arrGraphUniqueKeyDTO.length; i++)
    {
      int pdfId = graphNames.getPDFIdByGraphUniqueKeyDTO(arrGraphUniqueKeyDTO[i]);
      if (pdfId == -1)
        continue;

      long pdfDataIndex = graphNames.getPDFDataIndexByGraphUniqueKeyDTO(arrGraphUniqueKeyDTO[i]);
      if (pdfDataIndex == -1)
        continue;

      if (pdfIdList.contains(pdfId) && pdfDataIndexList.contains(pdfDataIndex))
        continue;

      pdfIdList.add(pdfId);
      pdfDataIndexList.add(pdfDataIndex);
    }

    int[] arrPDFId = new int[pdfIdList.size()];
    for (int i = 0; i < arrPDFId.length; i++)
    {
      arrPDFId[i] = pdfIdList.get(i);
    }

    long[] arrPDFDataIndex = new long[pdfDataIndexList.size()];
    for (int i = 0; i < arrPDFDataIndex.length; i++)
    {
      arrPDFDataIndex[i] = pdfDataIndexList.get(i);
    }

    System.out.println("PDF Id Array = " + pdfIdList + ", PDF Data Index Array = " + pdfDataIndexList);
    return getAllGraphsDataFromPCTMsgFile(arrPDFDataIndex, arrPDFId, "NA", "NA");
  }

  public GraphPCTData[] getAllGraphsDataFromPCTMsgFile(long[] arrPCTGraphDataIndex, int[] arrPDFId, String startTime, String endTime)
  {
    try
    {
      graphPCTData = new GraphPCTData[arrPCTGraphDataIndex.length];

      for (int i = 0; i < graphPCTData.length; i++)
      {
        graphPCTData[i] = new GraphPCTData(arrPDFId[i], interval, pdfNames);
        graphPCTData[i].initGraphPCTData();
        if (TIME_OPTION != TIME_OPTION_EMPTY)
        {
          boolean success = calPktRangeByModeType(startTime, endTime);
          if (!success)
            continue;
        }
      }

      byte byteBuf[] = null;
      boolean seqMatched = false;
      // when start and end sequence is same and not equal to zero. This is the condition when user want to generate report for "Total Run"
      if ((startSeq == endSeq) && (startSeq != 0))
      {
        seqMatched = false;
        for (int seqNum = 1; seqNum <= startSeq;)
        {
          byteBuf = new byte[(int) pktSize];
          int bytesRead = dis.read(byteBuf);
          processHdr(byteBuf); // process header to check that any sequence number is missing or not
          if (currSeqNumber == startSeq) // when required sequence number match with header sequence then break loop
          {
            Log.debugLogAlways(className, "getAllGraphsDataFromPCTMsgFile", "", "", "Required Sequence matched: (Current Sequence number, Start Seq) = (" + currSeqNumber + ", " + startSeq + ")");
            seqMatched = true;
            break;
          }
          else if (currSeqNumber == seqNum)
          {
            Log.debugLogAlways(className, "getAllGraphsDataFromPCTMsgFile", "", "", "Sequence matched: (Current Sequence number, Seq Num) = (" + currSeqNumber + ", " + seqNum + ")");
            seqNum++;
          }
          else if (currSeqNumber > seqNum)
          {
            Log.debugLogAlways(className, "getAllGraphsDataFromPCTMsgFile", "", "", "Current sequence number greater than sequence number, Packet may be lost : (Current Sequence number, Seq Num) = (" + currSeqNumber + ", " + seqNum + ")");
            seqNum = (int) currSeqNumber; // to skip missed pkt index
          }
        }

        if (seqMatched)
        {
          readOnePkt(byteBuf, startSeq, arrPCTGraphDataIndex, arrPDFId);
        }
        else
        {
          Log.errorLog(className, "getAllGraphsDataFromPCTMsgFile", "", "", "SeqNum not matching with Sequence number of data packet so taken data packet which is coming. Using Seq Number = " + currSeqNumber);
          readOnePkt(byteBuf, (int) currSeqNumber, arrPCTGraphDataIndex, arrPDFId);
        }
      }
      else if ((startSeq == 0) && (endSeq != 0))
      {
        // when start sequence is zero and end sequence not equal to zero. This is the condition when user want to generate report for specified time and start with 00:00:00
        seqMatched = false;
        for (int seqNum = 1; seqNum <= endSeq;)
        {
          byteBuf = new byte[(int) pktSize];
          int bytesRead = dis.read(byteBuf);
          processHdr(byteBuf); // process header to check that any sequence number is missing or not
          if (currSeqNumber == endSeq) // when required sequence number match with header sequence then break loop
          {
            Log.debugLogAlways(className, "getAllGraphsDataFromPCTMsgFile", "", "", "Required Sequence matched: (Current Sequence number, End Seq) = (" + currSeqNumber + ", " + endSeq + ")");
            seqMatched = true;
            break;
          }
          else if (currSeqNumber == seqNum)
          {
            Log.debugLogAlways(className, "getAllGraphsDataFromPCTMsgFile", "", "", "Sequence matched: (Current Sequence number, Seq Num) = (" + currSeqNumber + ", " + seqNum + ")");
            seqNum++;
          }
          else if (currSeqNumber > seqNum)
          {
            Log.debugLogAlways(className, "getAllGraphsDataFromPCTMsgFile", "", "", "Current sequence number greater than sequence number, Packet may be lost : (Current Sequence number, Seq Num) = (" + currSeqNumber + ", " + seqNum + ")");
            seqNum = (int) currSeqNumber; // to skip missed pkt index
          }
        }

        if (seqMatched)
        {
          readOnePkt(byteBuf, endSeq, arrPCTGraphDataIndex, arrPDFId);
        }
        else
        {
          Log.errorLog(className, "getAllGraphsDataFromPCTMsgFile", "", "", "SeqNum not matching with Sequence number of data packet so taken data packet which is coming. Using Seq Number = " + currSeqNumber);
          readOnePkt(byteBuf, (int) currSeqNumber, arrPCTGraphDataIndex, arrPDFId);
        }
      }
      else if ((startSeq != 0) && (endSeq != 0)) // This is the condition when user want to generate report for Run Phase/Specified Time and start & end time not equal to zero
      {
        seqMatched = false;
        // Process start sequence packet first
        for (int seqNum = 1; seqNum <= startSeq;)
        {
          byteBuf = new byte[(int) pktSize];
          int bytesRead = dis.read(byteBuf);
          processHdr(byteBuf); // process header to check that any sequence number is missing or not
          if (currSeqNumber == startSeq)// when required sequence number match with header sequence then break loop
          {
            Log.debugLogAlways(className, "getAllGraphsDataFromPCTMsgFile", "", "", "Required Sequence matched: (Current Sequence number, Start Seq) = (" + currSeqNumber + ", " + startSeq + ")");
            seqMatched = true;
            break;
          }
          else if (currSeqNumber == seqNum)
          {
            Log.debugLogAlways(className, "getAllGraphsDataFromPCTMsgFile", "", "", "Sequence matched: (Current Sequence number, Seq Num) = (" + currSeqNumber + ", " + seqNum + ")");
            seqNum++;
          }
          else if (currSeqNumber > seqNum)
          {
            Log.debugLogAlways(className, "getAllGraphsDataFromPCTMsgFile", "", "", "Current sequence number greater than sequence number, Packet may be lost : (Current Sequence number, Seq Num) = (" + currSeqNumber + ", " + seqNum + ")");
            seqNum = (int) currSeqNumber; // to skip missed pkt index
          }
        }

        if (seqMatched)
          readOnePkt(byteBuf, startSeq, arrPCTGraphDataIndex, arrPDFId);
        else
        {
          Log.errorLog(className, "getAllGraphsDataFromPCTMsgFile", "", "", "SeqNum not matching with Sequence number of data packet so taken data packet which is coming. Using Seq Number = " + currSeqNumber);
          readOnePkt(byteBuf, (int) currSeqNumber, arrPCTGraphDataIndex, arrPDFId);
        }

        seqMatched = false;// must be set it false here
        for (int seqNum = (startSeq + 1); seqNum <= endSeq;) // After processing start sequence packet, process end sequence packet
        {
          byteBuf = new byte[(int) pktSize];
          int bytesRead = dis.read(byteBuf);
          processHdr(byteBuf); // process header to check that any sequence number is missing or not
          if (currSeqNumber == endSeq) // when required sequence number match with header sequence then break loop
          {
            Log.debugLogAlways(className, "getAllGraphsDataFromPCTMsgFile", "", "", "Required Sequence matched: (Current Sequence number, End Seq) = (" + currSeqNumber + ", " + endSeq + ")");
            seqMatched = true;
            break;
          }
          else if (currSeqNumber == seqNum)
          {
            Log.debugLogAlways(className, "getAllGraphsDataFromPCTMsgFile", "", "", "Sequence matched: (Current Sequence number, Seq Num) = (" + currSeqNumber + ", " + seqNum + ")");
            seqNum++;
          }
          else if (currSeqNumber > seqNum)
          {
            Log.debugLogAlways(className, "getAllGraphsDataFromPCTMsgFile", "", "", "Current sequence number greater than sequence number, Packet may be lost : (Current Sequence number, Seq Num) = (" + currSeqNumber + ", " + seqNum + ")");
            seqNum = (int) currSeqNumber; // to skip missed packet index
          }
        }

        if (seqMatched)
        {
          readOnePkt(byteBuf, endSeq, arrPCTGraphDataIndex, arrPDFId);
        }
        else
        {
          Log.errorLog(className, "getAllGraphsDataFromPCTMsgFile", "", "", "SeqNum not matching with Sequence number of data packet so taken data packet which is coming. Using Seq Number = " + currSeqNumber);
          readOnePkt(byteBuf, (int) currSeqNumber, arrPCTGraphDataIndex, arrPDFId);
        }
      }

      closePCTMsgFile();
      return graphPCTData;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getAllGraphsDataFromPCTMsgFile", "", "", "Exception while reading data file - ", e);
      return null;
    }
  }

  public boolean calPktRangeByModeType(String startTime, String endTime)
  {
    try
    {
      Log.debugLogAlways(className, "calPktRangeByModeType", "", "", "Method called. Time is  " + TIME_OPTION + ", Start Time is " + startTime + ", End Time is " + endTime + ", Mode Type = " + modeType);
      long numStartTime = 0;
      long numEndTime = 0;

      // for total run
      if (modeType == 0)
      {
        if (TIME_OPTION == TOTAL_RUN)
        {
          startSeq = 1;
          endSeq = 1;
        }
        else
        {
          Log.errorLog(className, "calPktRangeByModeType", "", "", "Mode type is 0(Total Run), this " + TIME_OPTION + " is not a valid condition.");
          return false;
        }
      }
      else if (modeType == 1) // for run phase only
      {
        if (TIME_OPTION == TOTAL_RUN || (TIME_OPTION == RUN_PHASE_ONLY && phaseTimes == null))
        {
          // last sequence have all cumulative data
          startSeq = 3;
          endSeq = 3;
        }
        else
        {
          // Exclude RampUp, WarmUp and RampDown packets
          if (TIME_OPTION == RUN_PHASE_ONLY)
          {
            // Run phase start data at sequence number 1
            // Run phase end data at sequence number 2
            startSeq = 1;
            endSeq = 2;
          }
          else
          {
            numStartTime = rptUtilsBean.convStrToMilliSec(startTime.trim());
            numEndTime = rptUtilsBean.convStrToMilliSec(endTime.trim());

            for (int i = 1; i < phaseTimes.length; i++)
            {
              if (numStartTime == phaseTimes[i])
                startSeq = i;
              if (numEndTime == phaseTimes[i])
                endSeq = i;
            }

            if (endSeq == 0)
            {
              Log.errorLog(className, "calPktRangeByModeType", "", "", "Error: Invalid condition, Mode type is 1(RunPhase Only), and startTime " + startTime + " and endTime = " + endTime + " & Phase Times are " + phaseTimes[0] + ", " + phaseTimes[1] + ", " + phaseTimes[2] + ", " + phaseTimes[3]);
              return false;
            }
          }
        }
      }
      else if (modeType == 2) // for specified interval
      {
        if (TIME_OPTION == TOTAL_RUN || (TIME_OPTION == RUN_PHASE_ONLY && phaseTimes == null))
        {
          startSeq = 0;
          endSeq = maxSeq;
        }
        else
        {
          // Exclude RampUp, WarmUp and RampDown packets
          if (TIME_OPTION == RUN_PHASE_ONLY)
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

          // Make sure if sample time is divisible by interval then only we allow to generate graph data
          long startTimeDiff = numStartTime % interval;
          long endTimeDiff = numEndTime % interval;

          startSeq = (int) numStartTime / interval;
          endSeq = (int) numEndTime / interval;

          if ((startTimeDiff != 0) || (endTimeDiff != 0))
          {
            if (TIME_OPTION == RUN_PHASE_ONLY)
            {
              if (startTimeDiff != 0)
                startSeq++;
            }
            else
            {
              Log.errorLog(className, "calPktRangeByModeType", "", "", "Mode type is 2(Specified), and startTime " + numStartTime + " and endTime = " + numEndTime + " is not divisible by interval = " + interval);
              return false;
            }
          }

          if (startSeq > (maxSeq))
          {
            Log.errorLog(className, "calPktRangeByModeType", "", "", "Start Time exceeds Total Run time");
            return false;
          }

          if (endSeq > (maxSeq))
          {
            endSeq = maxSeq;
          }
          else if (endSeq < startSeq)
          {
            Log.errorLog(className, "calPktRangeByModeType", "", "", "End Time less than Start time");
            return false;
          }
        }
      }

      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "calPktRangeByModeType", "", "", "Error while calculating range of packets ", e);
      return false;
    }
  }

  // array of pct data index, need to process for pct data only for one seq and
  // set it to GraphPCTData
  private void readOnePkt(byte[] byteBuf, int seqNum, long[] arrPCTDataIndexes, int[] arrPDFId)
  {
    try
    {
      Log.debugLogAlways(className, "readOnePkt", "", "", "Method called. SeqNum is " + seqNum);
      if (arrPCTDataIndexes != null)
      {
        for (int i = 0; i < arrPCTDataIndexes.length; i++)
        {
          processDataForPCTGraphs(byteBuf, arrPCTDataIndexes[i], arrPDFId[i]);

          calcAndSetGraphPCTData(pctData, arrPCTDataIndexes[i], i);
        }
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "readOnePkt", "", "", "Exception in reading pkt - ", e);
    }
  }

  // calculate Graph PCT data and set it
  private void calcAndSetGraphPCTData(double[] pctData, long pctGraphDataIndex, int index)
  {
    try
    {
      Log.debugLogAlways(className, "calcAndSetGraphPCTData", "", "", "Method called.");
      // for custom group data, we calculate it through rtgMessage.dat file
      if (pctGraphDataIndex > 0 && graphPCTData[index] != null)
        graphPCTData[index].calcAndSetGraphPCTData(pctData);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "calcAndSetGraphPCTData", "", "", "Exception while calculating pct data - ", e);
    }
  }

  // This will process pct data for all pct data indexes in array
  public void processDataForPCTGraphs(byte recvData[], long pctGraphDataIndexes, int pdfId)
  {
    try
    {
      Log.debugLogAlways(className, "processDataForPCTGraphs", "", "", "Method Starts, Process data for pct graph data index = " + pctGraphDataIndexes + ", PDF Id = " + pdfId);
      int numGranuale = pdfNames.getNumGranuleByPDFId(pdfId);

      int endIndex = (int) (pctGraphDataIndexes * GRANULE_SIZE + PROCESS_HDR_LEN + numGranuale * GRANULE_SIZE);
      genRawPCTData(recvData, (int) pctGraphDataIndexes, endIndex, numGranuale);
      genGraphPCTData();
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "processDataForPCTGraphs", "", "", "Exception", e);
    }
  }

  // this function generate the graph percentile data of percentile graph
  private void genGraphPCTData() throws Exception
  {
    try
    {
      pctData = new double[rawPCTData.length];

      for (int pctDataIndex = 0; pctDataIndex < rawPCTData.length;)
      {
        pctData[pctDataIndex] = rawPCTData[pctDataIndex];
        pctData[pctDataIndex] = convTo3DigitDecimal(pctData[pctDataIndex]);
        pctDataIndex++;
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "genGraphPCTData", "", "", "Exception - ", e);
    }
  }

  // generate raw pctMessage data
  private void genRawPCTData(byte recvData[], int startIndex, int endIndex, int numGranuale) throws Exception
  {
    try
    {
      int i = 0;
      int index = startIndex;
      ByteBuffer bb;

      bb = ByteBuffer.wrap(recvData);

      bb = bb.order(ByteOrder.LITTLE_ENDIAN);

      rawPCTData = new double[numGranuale];

      for (i = 0; i < numGranuale; i++)
      {
        long intNumLower = bb.getLong(index);
        rawPCTData[i] = intNumLower;
        index = index + 8;
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws Exception
  {
    System.out.print("Enter The Test Run Number : ");
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    String testRun = br.readLine();
    int numTestRun = Integer.parseInt(testRun);

    PercentileDataViewer percentileDataViewer = new PercentileDataViewer(numTestRun);
    GraphPCTData[] arrGraphPCTData = percentileDataViewer.getAllGraphsDataFromPCTMsgFile();
    if (arrGraphPCTData == null)
      return;

    for (int i = 0; i < arrGraphPCTData.length; i++)
    {
      GraphPCTData graphPCTData = arrGraphPCTData[i];
      double[] arrPercentileData = graphPCTData.getArrPercentileData();
      if (arrPercentileData == null)
        continue;
      for (int j = 0; j < arrPercentileData.length; j++)
      {
        System.out.print(arrPercentileData[j] + ",");
      }
      System.out.println();
    }
  }
}
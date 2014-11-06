/*--------------------------------------------------------------------
  @Name    : PercentileData.java
  @Author  : Prabhat
  @Purpose : Bean for getting data from percentile raw files.
             Currently following files are support by this bean
              1. percentile.dat
              2. global.dat
  @Modification History:
    01/13/09 -->   Prabhat  -->  Initial Version

----------------------------------------------------------------------*/

package pac1.Bean;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;

import pac1.Bean.GraphName.GraphNameUtils;

public class PercentileData implements java.io.Serializable
{
  //Ravi - Add serialVersionUID to avoid  java.io.InvalidClassException.
  private static final long serialVersionUID = -3401218515102950843L;
  private final String className = "PercentileData";
  private final String PCT_FILE_NAME = "pctMessage.dat";
  private final String GLOBAL_DAT_FILE_NAME = "global.dat";
  public double formulaUnitData = 1;
  public String formulaUnit = "Secs";
  int MAX_PCT_ARR_SIZE = 1048576;
  // seq number index in pct msg hdr
  private transient final int SEQ_NUM_LOWER_INDEX = 0;
  private transient final int SEQ_NUM_UPPER_INDEX = 1;

  // size of each granule is longlong means 8 bytes
  private transient final int GRANULE_SIZE = 8;
  // size of Hdr is 4longlong means 32 bytes
  private transient final int PROCESS_HDR_LEN = 32;

  public int pktSize = 0;
  private int totalPkts = 0;
  private double fileSize = 0; // file size in bytes

  private double rawPCTData[] = null;
  private double pctData[] = null;

  private int rawDataLen = 0;
  private int msgHdrLen = 0;
  private int processedDataLen = 0;

  public int startSeq = 0; // Seq number of first packet to be shown in the
  // graph
  public int endSeq = 0; // Seq number of last packet to be shown in the graph
  public int maxSeq; // Max Seq number is the seq number of last data packet in
  // the file
  private double currSeqNumber; // use to store current seqNumber

  private PDFNames pdfNames = null;
  private GraphPCTData[] graphPCTData = null;

  FileInputStream fis = null;
  DataInputStream dis = null;

  private double phaseTimes[] = null;

  private int numTestRun = 0;
  private int interval;

  private int modeType = 0; // It is 0- Totel Run, 1- Run Phase, 2- Specified

  // time

  public PercentileData(int numTestRun, PDFNames pdfNames)
  {
    this.numTestRun = numTestRun;
    this.pdfNames = pdfNames;
  }

  // Add function for returning absolute path starting with workpath
  public String getTestRunDirPath()
  {
    return (Config.getWorkPath() + "/webapps/logs/TR" + this.numTestRun);
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

  // array of pct data index, need to process for pct data only for one seq and
  // set it to GraphPCTData
  private void readOnePkt(byte[] byteBuf, int seqNum, int[] arrPCTDataIndexes, int[] arrPDFId)
  {
    Log.debugLog(className, "readOnePkt", "", "", "Method called. SeqNum is " + seqNum);

    try
    {
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

  // process header for long type pct data
  public void processHdr(byte recvData[])
  {
    ByteBuffer bb;

    // Wrap only 32 bytes of data pkt, because hdr info available on 32 bytes
    bb = ByteBuffer.wrap(recvData, 0, PROCESS_HDR_LEN);
    bb = bb.order(ByteOrder.LITTLE_ENDIAN);
    long seqNumber = bb.getLong(0);
    currSeqNumber = seqNumber;
  }

  public double[] getPercentileGraphData(ReportData rptData, GraphUniqueKeyDTO graphUniqueKeyDTO, int pdfId, int minGranual)
  {
    Log.debugLog(className, "getPercentileGraphData", "", "", "graphUniqueKeyDTO = " + graphUniqueKeyDTO);
    double[][] rtgData = null;
    try
    {
      if (pdfId != -1)
      {
        int formulaNumber = rptData.graphNames.getPdfNames().getFormulaNumberByPDFId(pdfId);
        setFormulaUnitDataByFormulaNum(formulaNumber);
      }

      int graphDataIndex = rptData.graphNames.getGraphDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTO);
      LinkedHashMap<Integer, GraphUniqueKeyDTO> hmGraphUniqueKeyLinkedHashMap = new LinkedHashMap<Integer, GraphUniqueKeyDTO>();
      hmGraphUniqueKeyLinkedHashMap.put(graphDataIndex, graphUniqueKeyDTO);
      TimeBasedTestRunData timeBasedTestRunData = rptData.getTimeBasedDataFromRTGFile(hmGraphUniqueKeyLinkedHashMap, "Total Run", "NA", "NA", false, false);
      TimeBasedDTO timeBasedDTO = timeBasedTestRunData.getTimeBasedDTO(graphUniqueKeyDTO);
      double[] arrGraphData = timeBasedDTO.getArrGraphSamplesData();
      double[] arrNewData = new double[timeBasedTestRunData.getDataItemCount()];
      for (int i = 0; i < arrNewData.length; i++)
      {
        arrNewData[i] = arrGraphData[i];
      }
      
      //rtgData = getRTGRawData(rptData, graphUniqueKeyDTO);
      if (arrNewData != null)
      {
        Arrays.sort(arrNewData);
        getLogMsgFromDoubleArray("Data in rtgmessage.dat file for graphUniqueKeyDTO " + graphUniqueKeyDTO, arrGraphData);
      }
      return getPercentile(arrNewData, arrNewData.length);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getPercentileGraphData", "", "", "Exception - ", e);
    }
    return null;
  }

  private void setFormulaUnitDataByFormulaNum(int formulaNum)
  {
    Log.debugLog(className, "setFormulaUnitDataByFormulaNum", "", "", "Method Start. Formula number = " + formulaNum);

    try
    {
      // SEC - Convert milli-sec to seconds
      if (formulaNum == GraphNameUtils.FORMULA_SEC)
      {
        double convertionUnitMilli2Sec = 1000;
        formulaUnitData = formulaUnitData / convertionUnitMilli2Sec;
      }
      // PM - Convert to Per Minute
      else if (formulaNum == GraphNameUtils.FORMULA_PM)
      {
        Log.errorLog(className, "setFormulaUnitDataByFormulaNum", "", "", "Error: Currently we are not supported this PM formula.");
      }
      // PS - Convert to Per Seconds
      else if (formulaNum == GraphNameUtils.FORMULA_PS)
      {
        Log.errorLog(className, "setFormulaUnitDataByFormulaNum", "", "", "Error: Currently we are not supported this PS formula.");
      }
      // KBPS - Convert to kilo byte per second
      else if (formulaNum == GraphNameUtils.FORMULA_KBPS)
      {
        double convertionUnitBytes2KBPS = 1024;
        formulaUnitData = formulaUnitData / convertionUnitBytes2KBPS;
      }
      // DBH - Convert to divide by 100
      else if (formulaNum == GraphNameUtils.FORMULA_DBH) // 
      {
        double convertionUnit2DBH = 100;
        formulaUnitData = formulaUnitData / convertionUnit2DBH;
      }

      Log.debugLog(className, "setFormulaUnitDataByFormulaNum", "", "", "formula unit data = " + formulaUnitData);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "setFormulaUnitDataByFormulaNum", "", "", "Exception - ", e);
    }
  }

  /**
   * Generate Log Message from rtgMessage.dat file data
   * 
   * @param arrRtgData
   * @return
   */
  private StringBuffer getLogMsgFromDoubleArray(String msg, double[] arrRtgData)
  {
    StringBuffer logMsg = new StringBuffer();
    try
    {
      for (int i = 0; i < arrRtgData.length; i++)
      {
        logMsg.append(arrRtgData[i] + ",");
      }

      Log.debugLog(className, "getLogMsgFromDoubleArray", "", "", msg + " = " + logMsg);
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getLogMsgFromDoubleArray", "", "", "Exception - ", ex);
    }

    return logMsg;
  }

  /**
   * This method return array of percentile data
   * @param rawValue
   * @param pctCalSize
   * @return
   */
  public double[] getPercentile(double[] rawValue, int pctCalSize)
  {
	    Log.debugLog(className, "getPercentile", "", "", "pctCalSize = " + pctCalSize);
	    double max = 0.0; /* Maximum value in the provided input array */
	    int first_non_zero_index = 0;

	    /* The factor with which all the values will be divided
	     * due to max value greater than the value supported by 
	     * this function, limited by MAX_PCT_ARR_SIZE macro 
	     */
	    double scale = 1;
	    int MAX_PCT_ARR_SIZE = 1048576;
	    /* This array is used to hold the count
	     * the input data with same values. The
	     * index of this array is the value.
	     */
	    int[] pct_arr = new int[MAX_PCT_ARR_SIZE];
	    double[] out_pct = new double[101];

	    for(int ii = 0; ii < pct_arr.length; ii++)
	    {
	      pct_arr[ii] = 0;
	    }
	    
	    /* Compute the max value in the data set */
	    for(int i = 0; i < pctCalSize; i++)
	    {
	      if(rawValue[i] > max)
	        max = rawValue[i];
	    }

	    Log.debugLog(className, "getPercentile", "", "", "Max value from dataset = " + max);
	    if(max == 0.0)
	    {
	      Log.debugLog(className, "getPercentile", "", "", "Max value found = " + max + " so we return 0.0 data on all indexes");
	      for(int ii = 0; ii < out_pct.length; ii++)
	      {
	        out_pct[ii] = 0.0;
	      }
	      return out_pct;//return all data values 0.0
	    }

	    /* In case the max value is greater than the supported max value (i.e.
	     * the size of pct_arr, then we shall scale down all the values. Compute
	     * the scale factor in such case 
	     */
	    if(max > MAX_PCT_ARR_SIZE)
	      scale = max / MAX_PCT_ARR_SIZE + 1;

	    /* Scale down the max value */
	    double scaled_max = max / scale;

	    Log.debugLog(className, "getPercentile", "", "", "Max scale value from dataset = " + scaled_max);
	    
	    /* This is used to multiply the data if the values (as determined from max)
	     * are small, then the decimal points vcan be entertained. The smaller the 
	     * order of values, the more the decimal point digits can be entertained.
	     * Later on when percentiles are computed, the values need to be divided 
	     * by this number.
	     */
	    int divider = 1;
	    /* Compute the divider factor. The smaller the max, the more the divider */
	    while(scaled_max <= 99999)
	    {
	      scaled_max *= 10;
	      divider *= 10;
	    }

	    /* Now scale all the values using scale and divider factors, typecast to int
	     * use this number as index and increment the pc_arr at that index
	     * This way, populate the whole pct_arr.
	     */
	    for(int i = 0; i < pctCalSize; i++)
	      pct_arr[(int)((rawValue[i] / scale) * divider)]++;

	    while(pct_arr[first_non_zero_index] == 0)
	    {
	    	first_non_zero_index++;
	    }


	    int pct = 1; /* This is the percentile number, 1 for 1st, 2 for 2nd and so on */

	    /* cmp_count is the compare count. The count of pct_arr starting from index 0 will be added to a 
	     * cumulative countr (cum_count) and as soon as the cum_count exceeds the compare count for
	     * particular pct'th percentile, the index of the pct_arr represents the scale pct'th percentile
	     */
	    double cmp_count = (((double) pctCalSize - 1) * (double) pct) / (double) 100 + 1;
	    int cmp_count_int = (int) cmp_count; // For Interpolation 
	    double cmp_count_fraction = cmp_count - (double) cmp_count_int; // For Interpolation
	    int cum_count = 0;

	    int int_scaled_max = (int)scaled_max;/* Typecast */
	    /* Compute all the percentiles */
	    for(int i = first_non_zero_index; i <= int_scaled_max; i++)
	    {
	      cum_count += pct_arr[i];
	      while(cum_count >= cmp_count_int && pct <= 100)
	      {
	        out_pct[pct] = (((double)i) * scale) / ((double)divider);
	        /* Linear interpolation - Begin */
	        int cur_idx = i + 1;
	        if(cum_count == cmp_count_int && cmp_count_fraction > 0)
	        {
	          while((pct_arr[cur_idx] == 0) && (cur_idx <= int_scaled_max)) cur_idx++;
	          out_pct[pct] += (((double)(cur_idx - i) * cmp_count_fraction * scale )/ (double) divider);
	        }
	        /* Linear interpolation - End */
	        pct++;
	        cmp_count = (((double) pctCalSize - 1) * (double) pct) / (double) 100 + 1;
	        cmp_count_int = (int) cmp_count; // For Interpolation
	        cmp_count_fraction = cmp_count - (double) cmp_count_int;  // For Interpolation
	      }
	    }
	    return out_pct;
	  }

  /**
   * This calculate the cum count
   * 
   * @param arrRawData
   * @return
   */
  private double[] getCumCountArray(double[] arrRawData)
  {
    try
    {
      Log.debugLog(className, "getCumCountArray", "", "", "Method Called.");
      double[] arrCumCountArray = new double[arrRawData.length];
      double count = 0;
      for (int i = 0; i < arrRawData.length; i++)
      {
        count = count + arrRawData[i];
        arrCumCountArray[i] = count;
      }

      return arrCumCountArray;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getCumCountArray", "", "", "Exception - ", ex);
      return arrRawData;
    }
  }

  /**
   * This method is used for creating data for percentile graphs. It reads data
   * from rtgMessage.dat file data.
   * 
   * @param arrGraphNumbers
   * @param arrPDFIds
   * @param buckets
   * @return
   */
  public double[][] getRTGRawData(ReportData rptData, GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    try
    {
      Log.debugLog(className, "getRTGRawData", "", "", "Method Called. graphUniqueKeyDTO = " + graphUniqueKeyDTO);
      int[] graphDataIndex = new int[1];
      graphDataIndex[0] = rptData.graphNames.getGraphDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTO);
      return rptData.getAllGraphsDataFromRTGFile(graphDataIndex, "Total Run", "NA", "NA", true, true, true, true);
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getRTGRawData", "", "", "Exception - ", ex);
      return null;
    }
  }

  // This will process pct data for all pct data indexes in array
  public void processDataForPCTGraphs(byte recvData[], int pctGraphDataIndexes, int pdfId)
  {
    try
    {
      Log.debugLog(className, "processDataForPCTGraphs", "", "", "Method Starts, Process data for pct graph data index = " + pctGraphDataIndexes + ", PDF Id = " + pdfId);
      int numGranuale = pdfNames.getNumGranuleByPDFId(pdfId);
      int endIndex = (pctGraphDataIndexes + numGranuale * GRANULE_SIZE);
      genRawPCTData(recvData, pctGraphDataIndexes, endIndex, numGranuale);
      genGraphPCTData();
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "processDataForPCTGraphs", "", "", "Exception", e);
    }
  }

  // generate raw pct data
  private void genRawPCTData(byte recvData[], int startIndex, int endIndex, int numGranuale)   
  {
    try
    {
      Log.debugLogAlways(className, "genRawPCTData", "", "", "startIndex = " + startIndex + ", endIndex = " + endIndex);
      int i = 0;
      int index = startIndex;
      ByteBuffer bb = ByteBuffer.wrap(recvData);
      bb = bb.order(ByteOrder.LITTLE_ENDIAN);
      rawPCTData = new double[numGranuale];
      for (i = 0; i < numGranuale; i++)
      {
        long data = bb.getLong(index);
        rawPCTData[i] = data;
        index = index + 8;
      }
      
      Log.debugLogAlways(className, "genRawPCTData", "", "", "startIndex = " + startIndex + ", endIndex = " + endIndex + ", index = " + index);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "genRawPCTData", "", "", "Exception - ", e);
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

        // make it upto 3 digit decimal
        pctData[pctDataIndex] = convTo3DigitDecimal(pctData[pctDataIndex]);

        pctDataIndex++;
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "genGraphPCTData", "", "", "Exception - ", e);
    }
  }

  // Make double value upto 3 digit decimal
  public static double convTo3DigitDecimal(double val)
  {
    double dblVal;
    dblVal = (double) Math.round(val * 1000);
    dblVal = dblVal / 1000;
    return (dblVal);
  }

  // This will open PCT message file and initialize objects and variables
  // related to the test run.
  public boolean openPCTMsgFile()
  {
    Log.debugLog(className, "openPCTMsgFile", "", "", "Method called. Test Run is  " + numTestRun);
    String strPctFile = getPCTFileNameWithPath();

    try
    {
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

      // END line not append in testrun.pdf, in mode 0(Total time) & 1(Phase
      // time)
      // Use totalPkts as max sequence
      if (pdfNames.getEndSeqNum() == -1)
        maxSeq = totalPkts;
      else
        // This the case of specified interval mode
        maxSeq = pdfNames.getEndSeqNum();

      Log.debugLog(className, "openPCTMsgFile", "", "", "File opened. Total File Size = " + fileSize + ", Total Pkts = " + totalPkts + ", Pkt Size = " + pktSize + ", processedDataLen = " + processedDataLen + ", Max Sequence number = " + maxSeq);

      return true;
    }
    catch (FileNotFoundException e)
    {
      Log.errorLog(className, "openPCTMsgFile", "", "", "No Data Available (pctMessage.dat not found). File name is " + strPctFile);
      Log.stackTraceLog(className, "openPCTMsgFile", "", "", "Exception - ", e);
      return false;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "openPCTMsgFile", "", "", "Exception while reading data file - ", e);
      return false;
    }
  }

  // this function close onened data streams
  public void closePCTMsgFile()
  {
    Log.debugLog(className, "closePCTMsgFile", "", "", "Method called");

    try
    {
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

  // This function generate all graph data from pctMessage.dat file according to
  // time specified by user
  public GraphPCTData[] getAllGraphsDataFromPCTMsgFile(int[] arrPctGraphIndex, int[] arrPDFId, String time, String startTime, String endTime)
  {
    Log.debugLog(className, "getAllGraphsDataFromPCTMsgFile", "", "", "Method called. Test Run is  " + this.numTestRun + ", PCT data Indexes = " + rptUtilsBean.intArrayToList(arrPctGraphIndex) + ", length = " + arrPctGraphIndex.length + ", PDFs id = " + rptUtilsBean.intArrayToList(arrPDFId) + ", Mode Type = " + modeType);

    int numPCTGraphIndex = arrPctGraphIndex.length;

    try
    {
      graphPCTData = new GraphPCTData[numPCTGraphIndex];

      for (int i = 0; i < graphPCTData.length; i++)
      {
        graphPCTData[i] = new GraphPCTData(arrPDFId[i], interval, pdfNames);
        graphPCTData[i].initGraphPCTData();

        // for custom reports
        if (arrPctGraphIndex[i] < 0)
          continue;
        else
        {
          // Time option is applicable for all percentile report(s) except Cutom
          // Group
          if (!time.equals(""))
          {
            boolean success = calPktRangeByModeType(time, startTime, endTime);
            if (!success)
              continue;
          }
        }
      }

      byte byteBuf[] = null;

      boolean seqMatched = false;

      // when start and end seq is same and not equal to zero
      // This is the condition when user want to generate report for "Total Run"
      if ((startSeq == endSeq) && (startSeq != 0))
      {
        seqMatched = false;

        for (int seqNum = 1; seqNum <= startSeq;)
        {
          byteBuf = new byte[(int) pktSize];

          int bytesRead = dis.read(byteBuf);

          // process header to check that any seq num is missing or not
          processHdr(byteBuf);

          // when required sequence number match with header seq then break loop
          if (currSeqNumber == startSeq)
          {
            Log.debugLog(className, "getAllGraphsDataFromPCTMsgFile", "", "", "Required Sequence matched: (Current Sequence number, Start Seq) = (" + currSeqNumber + ", " + startSeq + ")");

            seqMatched = true;
            break;
          }
          else if (currSeqNumber == seqNum)
          {
            Log.debugLog(className, "getAllGraphsDataFromPCTMsgFile", "", "", "Sequence matched: (Current Sequence number, Seq Num) = (" + currSeqNumber + ", " + seqNum + ")");
            seqNum++;
          }
          else if (currSeqNumber > seqNum)
          {
            Log.debugLog(className, "getAllGraphsDataFromPCTMsgFile", "", "", "Current sequence number greater than sequence number, Packet may be lost : (Current Sequence number, Seq Num) = (" + currSeqNumber + ", " + seqNum + ")");
            seqNum = (int) currSeqNumber; // to skip missed pkt index
          }
        }

        if (seqMatched)
          readOnePkt(byteBuf, startSeq, arrPctGraphIndex, arrPDFId);
        else
        {
          // Log.errorLog(className, "getAllGraphsDataFromPCTMsgFile", "", "",
          // "ERROR: In data calculation, pctMessage file may not be correct. SeqNum not matching, Ignored.");
          Log.errorLog(className, "getAllGraphsDataFromPCTMsgFile", "", "", "SeqNum not matching with Sequence number of data packet so taken data packet which is coming. Using Seq Number = " + currSeqNumber);
          readOnePkt(byteBuf, (int) currSeqNumber, arrPctGraphIndex, arrPDFId);
          // closePCTMsgFile();
          // return null;
        }
      }
      // when start seq is zero and end seq not equal to zero
      // This is the condition when user want to generate report for specified
      // time and start with 00:00:00
      else if ((startSeq == 0) && (endSeq != 0))
      {
        seqMatched = false;

        for (int seqNum = 1; seqNum <= endSeq;)
        {
          byteBuf = new byte[(int) pktSize];

          int bytesRead = dis.read(byteBuf);

          // process header to check that any seq num is missing or not
          processHdr(byteBuf);

          // when required sequence number match with header seq then break loop
          if (currSeqNumber == endSeq)
          {
            Log.debugLog(className, "getAllGraphsDataFromPCTMsgFile", "", "", "Required Sequence matched: (Current Sequence number, End Seq) = (" + currSeqNumber + ", " + endSeq + ")");

            seqMatched = true;
            break;
          }
          else if (currSeqNumber == seqNum)
          {
            Log.debugLog(className, "getAllGraphsDataFromPCTMsgFile", "", "", "Sequence matched: (Current Sequence number, Seq Num) = (" + currSeqNumber + ", " + seqNum + ")");
            seqNum++;
          }
          else if (currSeqNumber > seqNum)
          {
            Log.debugLog(className, "getAllGraphsDataFromPCTMsgFile", "", "", "Current sequence number greater than sequence number, Packet may be lost : (Current Sequence number, Seq Num) = (" + currSeqNumber + ", " + seqNum + ")");
            seqNum = (int) currSeqNumber; // to skip missed pkt index
          }
        }

        if (seqMatched)
          readOnePkt(byteBuf, endSeq, arrPctGraphIndex, arrPDFId);
        else
        {
          // Log.errorLog(className, "getAllGraphsDataFromPCTMsgFile", "", "",
          // "ERROR: In data calculation, pctMessage file may not be correct. SeqNum not matching. Ignored.");
          Log.errorLog(className, "getAllGraphsDataFromPCTMsgFile", "", "", "SeqNum not matching with Sequence number of data packet so taken data packet which is coming. Using Seq Number = " + currSeqNumber);
          readOnePkt(byteBuf, (int) currSeqNumber, arrPctGraphIndex, arrPDFId);
          // closePCTMsgFile();
          // return null;
        }
      }
      // This is the condition when user want to generate report for Run
      // Phase/Specified Time and start & end time not equal to zero
      else if ((startSeq != 0) && (endSeq != 0))
      {
        seqMatched = false;

        // Process start sequence pkt first
        for (int seqNum = 1; seqNum <= startSeq;)
        {
          byteBuf = new byte[(int) pktSize];

          int bytesRead = dis.read(byteBuf);

          // process header to check that any seq num is missing or not
          processHdr(byteBuf);

          // when required sequence number match with header seq then break loop
          if (currSeqNumber == startSeq)
          {
            Log.debugLog(className, "getAllGraphsDataFromPCTMsgFile", "", "", "Required Sequence matched: (Current Sequence number, Start Seq) = (" + currSeqNumber + ", " + startSeq + ")");

            seqMatched = true;
            break;
          }
          else if (currSeqNumber == seqNum)
          {
            Log.debugLog(className, "getAllGraphsDataFromPCTMsgFile", "", "", "Sequence matched: (Current Sequence number, Seq Num) = (" + currSeqNumber + ", " + seqNum + ")");
            seqNum++;
          }
          else if (currSeqNumber > seqNum)
          {
            Log.debugLog(className, "getAllGraphsDataFromPCTMsgFile", "", "", "Current sequence number greater than sequence number, Packet may be lost : (Current Sequence number, Seq Num) = (" + currSeqNumber + ", " + seqNum + ")");
            seqNum = (int) currSeqNumber; // to skip missed pkt index
          }
        }

        if (seqMatched)
          readOnePkt(byteBuf, startSeq, arrPctGraphIndex, arrPDFId);
        else
        {
          // Log.errorLog(className, "getAllGraphsDataFromPCTMsgFile", "", "",
          // "ERROR: In data calculation, pctMessage file may not be correct. SeqNum not matching, Ignored.");
          Log.errorLog(className, "getAllGraphsDataFromPCTMsgFile", "", "", "SeqNum not matching with Sequence number of data packet so taken data packet which is coming. Using Seq Number = " + currSeqNumber);
          readOnePkt(byteBuf, (int) currSeqNumber, arrPctGraphIndex, arrPDFId);
          // closePCTMsgFile();
          // return null;
        }

        // must be set it false here
        seqMatched = false;
        // After processing start sequence pkt, process end seq pkt
        for (int seqNum = (startSeq + 1); seqNum <= endSeq;)
        {
          byteBuf = new byte[(int) pktSize];

          int bytesRead = dis.read(byteBuf);

          // process header to check that any seq num is missing or not
          processHdr(byteBuf);

          // when required sequence number match with header seq then break loop
          if (currSeqNumber == endSeq)
          {
            Log.debugLog(className, "getAllGraphsDataFromPCTMsgFile", "", "", "Required Sequence matched: (Current Sequence number, End Seq) = (" + currSeqNumber + ", " + endSeq + ")");

            seqMatched = true;
            break;
          }
          else if (currSeqNumber == seqNum)
          {
            Log.debugLog(className, "getAllGraphsDataFromPCTMsgFile", "", "", "Sequence matched: (Current Sequence number, Seq Num) = (" + currSeqNumber + ", " + seqNum + ")");
            seqNum++;
          }
          else if (currSeqNumber > seqNum)
          {
            Log.debugLog(className, "getAllGraphsDataFromPCTMsgFile", "", "", "Current sequence number greater than sequence number, Packet may be lost : (Current Sequence number, Seq Num) = (" + currSeqNumber + ", " + seqNum + ")");
            seqNum = (int) currSeqNumber; // to skip missed pkt index
          }
        }

        if (seqMatched)
          readOnePkt(byteBuf, endSeq, arrPctGraphIndex, arrPDFId);
        else
        {
          // Log.errorLog(className, "getAllGraphsDataFromPCTMsgFile", "", "",
          // "ERROR: In data calculation, pctMessage file may not be correct. SeqNum not matching. Ignored.");
          Log.errorLog(className, "getAllGraphsDataFromPCTMsgFile", "", "", "SeqNum not matching with Sequence number of data packet so taken data packet which is coming. Using Seq Number = " + currSeqNumber);
          readOnePkt(byteBuf, (int) currSeqNumber, arrPctGraphIndex, arrPDFId);
          // closePCTMsgFile();
          // return null;
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

  // calculate Graph PCT data and set it
  private void calcAndSetGraphPCTData(double[] pctData, int pctGraphDataIndex, int index)
  {
    Log.debugLog(className, "calcAndSetGraphPCTData", "", "", "Method called.");

    try
    {
      // for custom group data, we calculate it through rtgMessage.dat file
      if (!(pctGraphDataIndex < 0))
        graphPCTData[index].calcAndSetGraphPCTData(pctData);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "openPCTMsgFile", "", "", "Exception while calculating pct data - ", e);
    }
  }

  // calculate start and end seq number based paramters & Mode types
  public boolean calPktRangeByModeType(String time, String startTime, String endTime)
  {
    Log.debugLog(className, "calPktRangeByModeType", "", "", "Method called. Time is  " + time + ", Start Time is " + startTime + ", End Time is " + endTime + ", Mode Type = " + modeType);

    long numStartTime = 0;
    long numEndTime = 0;
    try
    {
      // for total run
      if (modeType == 0)
      {
        // There is only one cummulative pct data pkt in case of modeType 0
        // Only Total Run is allowed
        if (time.equals("Total Run"))
        {
          // Process only this seq number pkt
          // If both seq is same then it process only this pkt
          startSeq = 1; // First data packet seq is 1
          endSeq = 1;
        }
        else
        {
          Log.errorLog(className, "calPktRangeByModeType", "", "", "Mode type is 0(Total Run), this " + time + " is not a valid condition.");
          return false;
        }
      }
      else if (modeType == 1) // for run phase only
      {
        // there are only 3 sample that have cummulative data
        // seq 1 --> RunPhase start time
        // seq 2 --> RunPhase end time
        // seq 3 --> TR end time
        if (time.equals("Total Run") || (time.equals("Run Phase Only") && phaseTimes == null))
        {
          // last seq have all cumulative data
          startSeq = 3;
          endSeq = 3;
        }
        else
        {
          if (time.equals("Run Phase Only")) // Exclude RampUp, WarmUp and
          // RampDown packets
          {
            // Run phase start data at seq number 1
            // Run phase end data at seq number 2
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
        if (time.equals("Total Run") || (time.equals("Run Phase Only") && phaseTimes == null))
        {
          startSeq = 0;
          endSeq = maxSeq;
        }
        else
        {
          if (time.equals("Run Phase Only")) // Exclude RampUp, WarmUp and
          // RampDown packets
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
          // Make sure if sample time is divisible by interval then only we
          // allow to generate graph data

          long startTimeDiff = numStartTime % interval;
          long endTimeDiff = numEndTime % interval;

          Log.debugLog(className, "calPktRangeByModeType", "", "", "Mode Type = " + modeType + ", and time = " + time + ", and startTime " + numStartTime + " and endTime = " + numEndTime);

          startSeq = (int) numStartTime / interval;
          endSeq = (int) numEndTime / interval;

          if ((startTimeDiff != 0) || (endTimeDiff != 0))
          {
            if (time.equals("Run Phase Only"))
            {
              if (startTimeDiff != 0)
                startSeq++;

              Log.debugLog(className, "calPktRangeByModeType", "", "", "Rounding up sequence number for Run Phase Only. Start sequence (current/previous) = (" + startSeq + "/" + (int) numStartTime / interval + "), End sequence (current/previous) = (" + endSeq + "/" + (int) numEndTime / interval + ").");
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
            endSeq = maxSeq;
          else if (endSeq < startSeq)
          {
            Log.errorLog(className, "calPktRangeByModeType", "", "", "End Time less than Start time");
            return false;
          }
        }
      }

      if (phaseTimes != null)
        Log.debugLog(className, "calPktRangeByModeType", "", "", "Time is  " + time + ", Start Time is " + startTime + ", End Time is " + endTime + ", Start Seq is " + startSeq + ", End Seq is " + endSeq + ", Total Pkts is " + totalPkts + ", Phase Times are " + phaseTimes[0] + ", " + phaseTimes[1] + ", " + phaseTimes[2] + ", " + phaseTimes[3]);
      else
        Log.debugLog(className, "calPktRangeByModeType", "", "", "Time is  " + time + ", Start Time is " + startTime + ", End Time is " + endTime + ", Start Seq is " + startSeq + ", End Seq is " + endSeq + ", Total Pkts is " + totalPkts + ", Phase Times are not calculated so that global file not exist");

      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "calPktRangeByModeType", "", "", "Error while calculating range of packets ", e);
      return false;
    }
  }

  // This will read global data file and return different phases of test run
  public boolean getPhaseTimes()
  {
    String globalDataFile = getTestRunDirPath() + "/" + GLOBAL_DAT_FILE_NAME;
    try
    {
      String str1 = "";
      StringTokenizer st3;
      String str5[];

      fis = new FileInputStream(globalDataFile);

      Log.debugLog(className, "getPhaseTimes", "", "", "globalFilepath =" + globalDataFile);

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
            Log.debugLog(className, "getPhaseTimes", "", "", "Phase Time =" + phaseTimes[ii - 1]);
          }
        }
      }
      br.close();
      fis.close();
      return true;
    }
    catch (FileNotFoundException e)
    {
      // Do not return false if file is not found. This will allow user to
      // generate graphs for running test runs.
      Log.errorLog(className, "getPhaseTimes", "", "", "File (" + globalDataFile + ") not found. Ignored as test run may be running. Using 0 for all times");
      // Make it null so that we can check letter data is there or not.
      phaseTimes = null;
      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getPhaseTimes", "", "", "Exception while reading data file", e);
      phaseTimes = null;
      return false;
    }
  }

  public static void main(String[] args)
  {
    /*
     * PercentileData percentileData = new PercentileData(23422, 10000); //
     * 23383, // 23415, // 23422 GraphPCTData[] graphPCTData = null;
     * 
     * int[] tempArrPctGraphIndex = {0, 101}; int[] tempPDFIDIndex = {1, 2};
     * if(percentileData.openPCTMsgFile()) { graphPCTData =
     * percentileData.getAllGraphsDataFromPCTMsgFile(tempArrPctGraphIndex,
     * tempPDFIDIndex, "Total Run", "", ""); }
     * 
     * for(int i = 0; i < graphPCTData.length; i++) { //Percentile Graph Data
     * System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"); double[]
     * arrPercentileData = graphPCTData[i].getArrPercentileData(); for(int j =
     * 0; j < arrPercentileData.length; j++) { System.out.println((j + 1) +
     * "% = " + ", Data = " + arrPercentileData[j]); }
     */

    // Slab Graph Data
    /*
     * System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"); double[]
     * arrSlabData = graphPCTData[i].getArrSlabsData(); for(int j = 0; j <
     * arrSlabData.length; j++) { System.out.println((j + 1) + "th Slab = " +
     * ", Data = " + arrSlabData[j]); }
     */

    // Frequency distribution Graph Data
    /*
     * System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"); double[]
     * arrPCTCumDataBuckets = graphPCTData[i].getArrPCTCumDataBuckets(); for(int
     * j = 0; j < arrPCTCumDataBuckets.length; j++) {
     * System.out.println("Bucket = " + j + ", Cumulative data = " +
     * arrPCTCumDataBuckets[j]); }
     */
    // }
  }
}

/*--------------------------------------------------------------------
  @Name    : CmpRptTestRunData.java
  @Author  : Prabhat
  @Purpose : To Set The Compare Report Test Run Data
  @Modification History:
    07/17/07 : Prabhat --> Initial Version
    02/03/09 : Prabhat --> Add code to handle Percentile Reports
    03/19/09 : Prabhat --> Add code for server signature

  @Issues:
    1. When we use cmpRptUsingRtpl.reportStatusLog(), class name logged in file is cmpRptUsingRtpl
----------------------------------------------------------------------*/

package pac1.Bean;
import java.util.*;
import java.util.Map.Entry;

import pac1.Bean.GraphName.GraphNameUtils;
import pac1.Bean.GraphName.GraphNames;
/**
 * Class for Compare Report Test Run Data.
 */
public class CmpRptTestRunData
{
  private String className = "CmpRptTestRunData";
  
  /*Instance of Graph Names*/
  GraphNames graphNames = null;
  
  /*Instance of Report Data.*/
  private ReportData rptData = null;
  
  /*Instance of CmpRptMsrOptions.*/
  private CmpRptMsrOptions cmpRptMsrOptions = null;
  
  /*Instance of CmpRptUsingRtpl*/
  private CmpRptUsingRtpl cmpRptUsingRtpl = null;
  
  /*Instance of ServerSignatureInfo*/
  ServerSignatureInfo serverSignatureInfo = null;
  
  /*Instance of DerivedData*/
  DerivedData derivedData = null;
  
  /*Instance of ReportDataUtils.*/
  ReportDataUtils reportDataUtilsObj = null;
  
  private String rtplName = "";

  private int testRun = 0;

  private double arrGraphData[][] = null; // Data loaded from file for all
  
  private double arrAvgDataValuesAll[][] = null;
  
  private double arrStdDevDataValuesAll[][] = null;

  /**
   * This is 2D array that contains Row1-Report
   * name,Row2-Graph Data Index,Row3-Report
   * Group Name,Row4-PDFId,Row5-PCTData
   * Idx,Row6-Graph View Type,Row7-Percentile
   * Template details
   */
  String[][] arrRptDetails = null;
  
  /*Array of all unique graph data indexes*/
  int arrRptGraphDataIdx[] = null; 
  
  /*Linked HashMap Containing the Unique Graph DTO.*/
  LinkedHashMap<Integer, GraphUniqueKeyDTO> hmGraphInfoDTO = new LinkedHashMap<Integer, GraphUniqueKeyDTO>();
  
  String strOverAllAvgData = ""; // Average data for all samples for one report.
  String strMaxDataValue = "";
  String strStdDevDataValue = ""; // Std-dev data for all samples for one
  private int graphDataIndex = -1; // Graph data index for the report. Saves for
  private String controllerTRNumber = "NA";

  private String genName;
  
  /*Time Based Data Object to fetch the required Data.*/
  TimeBasedTestRunData timeBasedTestRunDataObj = null;

  /*contains the total run time of test run.*/
  private long testRunTotalTime = 0L;
  
  private int sampleCount = 0;
  private int lastDerivedSampleCount = 0;
  private int graphDataType = 0;
  
  /**
   * Constructor Set the testRun and reportSetName
   * @param testRun
   * @param rtplName
   * @param cmpRptUsingRtpl
   * @param cmpRptMsrOptions
   */
  CmpRptTestRunData(int testRun, String rtplName, CmpRptUsingRtpl cmpRptUsingRtpl, CmpRptMsrOptions cmpRptMsrOptions)
  {
    this.testRun = testRun;
    this.rtplName = rtplName;
    this.cmpRptUsingRtpl = cmpRptUsingRtpl;
    this.cmpRptMsrOptions = cmpRptMsrOptions;
  }

  /**
   * Constructor to Net cloud Options.
   * @param testRun
   * @param rtplName
   * @param cmpRptUsingRtpl
   * @param cmpRptMsrOptions
   * @param genName
   * @param controllerTRNumber
   */
  public CmpRptTestRunData(int testRun, String rtplName, CmpRptUsingRtpl cmpRptUsingRtpl, CmpRptMsrOptions cmpRptMsrOptions,String genName ,String controllerTRNumber) 
  {
    this.testRun = testRun;
    this.rtplName = rtplName;
    this.cmpRptUsingRtpl = cmpRptUsingRtpl;
    this.cmpRptMsrOptions = cmpRptMsrOptions;
    this.controllerTRNumber = controllerTRNumber;
    this.genName = genName;
  }

  /**
   * Return the Test Run.
   * @return
   */
  public int getTestRun()
  {
    return (testRun);
  }

   /**
    * Return the array of all Report Names.
    * @return
    */
  public String[] getRtpNames()
  {
    return (arrRptDetails[0]);
  }

  /**
   * Return the array of all Report Group Names.
   * @return
   */
  public String[] getRtpGrpNames()
  {
    return (arrRptDetails[2]);
  }

  /**
   * Return the array of graph view type
   * @return
   */
  public String[] getGraphViewTypeArray()
  {
    return (arrRptDetails[5]);
  }

  /**
   * Return the array of PCT data idx.
   * @return
   */
  public int[] getPCTDataIdxArray()
  {
    return (rptUtilsBean.strArrayToIntArray(arrRptDetails[4]));
  }

  /**
   * Return the array of template detail
   * @return
   */
  public String[] getTemplateDetailsArray()
  {
    return (arrRptDetails[6]);
  }

  /**
   * Return the array of Report ID
   * @return
   */
  public int[] getGraphIDArray()
  {
    return (rptUtilsBean.strArrayToIntArray(arrRptDetails[7]));
  }

  /**
   * Return the array of Report Group ID
   * @return
   */
  public int[] getGroupIDArray()
  {
    return (rptUtilsBean.strArrayToIntArray(arrRptDetails[8]));
  }

  /**
   * Return the array of PDF ID
   * @return
   */
  public int[] getPDFIDArray()
  {
    return (rptUtilsBean.strArrayToIntArray(arrRptDetails[3]));
  }

  public void invalidateGraphDataIndex()
  {
    graphDataIndex = -1;
  }

  // this function return the rpt graph data idx array(this is unique list)
  public int[] getRptGraphDataIdxArray()
  {
    return arrRptGraphDataIdx;
  }
  
  // this function return the rpt graph data idx array(this is unique list)
  public void setRptGraphDataIdxArray(int[] arrRptGraphDataIdx)
  {
    this.arrRptGraphDataIdx = arrRptGraphDataIdx;
  }

  /**
   * this function return the rpt graph data idx array(all index in template use to store index in CmpGraphData).
   * @return
   */
  public int[] getRptTemplateGraphDataIdxArray()
  {
    return (rptUtilsBean.strArrayToIntArray(arrRptDetails[1]));
  }

  /**
   * this function return the graph data array
   * @return
   */
  public double[][] getArrayGraphData()
  {
    return arrGraphData;
  }

  /**
   * this function return the graph Avg data array
   * @return
   */
  public double[][] getArrayGraphAvgData()
  {
    return arrAvgDataValuesAll;
  }

  /**
   * this function return the graph Std-Dev data array
   * @return
   */
  public double[][] getArrayGraphStdDevData()
  {
    return arrStdDevDataValuesAll;
  }

  /**
   * create & set derived data object
   * @param derivedExpression
   */
  public void setDerivedDataObj(String derivedExpression)
  {
    derivedData = new DerivedData(derivedExpression);
  }
  
  /**
   * Method returns the total run time of test run.
   * @return
   */
  public long getTestRunTotalTime() 
  {
    return testRunTotalTime;
  }
  
  /**
   * Method returns the Graph Data Type.
   * @return
   */
  public int getGraphDataType() 
  {
    return graphDataType;
  }

  /**
   * Returns the Max sample of graph.
   * @return
   */
 public String getMaxDataValue()
 {
   Log.reportDebugLog(className, "getMaxDataValue", "", "", "Extracting Maximum Data = " + strMaxDataValue);
   if(strMaxDataValue.equals(""))
   {
     Log.reportDebugLog(className, "getMaxDataValue", "", "", "Over All Avg Data is not available.");
     strMaxDataValue = "NA";
   }

   return strMaxDataValue;
 }

 /**
  * This will returns the generator name
  * @return
  */
 public String getGenName() 
 {
   return genName;
 }
 
 /**
  * This method will return controllerTRNumber
  * @return
  */
 public String getControllerTRNumber() 
 {
   return controllerTRNumber;
 }
 
  // Return the graph data index by report name
  /*
   * public int getGraphDataIndex(String rptName) { if(graphDataIndex != -1) //
   * Already got once, so return it return(graphDataIndex); for(int i = 0; i <
   * arrRptDetails[0].length; i++) if(arrRptDetails[0][i].equals(rptName)) {
   * graphDataIndex = Integer.parseInt(arrRptDetails[1][i]); // Save it for next
   * mesaurement is using same test run return(graphDataIndex); }
   * 
   * Log.reportDebugLog(className, "getGraphDataIndex", "", "",
   * "Graph Data index not found for report name = " + rptName);
   * 
   * return -1; }
   * 
   * // Return the PDF ID by report name public int getPDFId(String rptName) {
   * int pdfID = -1; for(int i = 0; i < arrRptDetails[0].length; i++) {
   * if(arrRptDetails[0][i].equals(rptName)) { pdfID =
   * Integer.parseInt(arrRptDetails[3][i]); break; } }
   * 
   * Log.reportDebugLog(className, "getPDFId", "", "",
   * "PDF ID for report name = " + rptName + ", PDF Id = " + pdfID);
   * 
   * return pdfID; }
   * 
   * 
   * // Return the PCT Graph Data idx by report name public int
   * getPCTGraphDataIndex(String rptName) { int pctDataIdx = -1; for(int i = 0;
   * i < arrRptDetails[0].length; i++) { if(arrRptDetails[0][i].equals(rptName))
   * { pctDataIdx = Integer.parseInt(arrRptDetails[4][i]); break; } }
   * 
   * Log.reportDebugLog(className, "getPCTGraphDataIndex", "", "",
   * "PCT Idx for report name = " + rptName);
   * 
   * return pctDataIdx; }
   */

  /**
   * Function to initialize Test Run Info.
   * @return
   */
  public boolean readData()
  {
    Log.reportDebugLog(className, "readData", "", "", "Method called. Test run = " + testRun);
    try
    {
      /*Getting Graph Information in HashMap DTO.*/
      getAllUniqueRptGraphDataIdx();
      
      if(rtplName.equals("_Blank_Template"))
      {
	/*Getting Graph Index in Array.*/
	getAllUniqueGraphDataIndex();
	return true;
      }
      
      if(hmGraphInfoDTO.size() == 0)
      {
	      Log.errorLog(className, "readData", "", "", "Found Empty HashMap of Graph DTO. Please Check Logs.");
        return false;
      }
      
      /*Getting Graph Index in Array.*/
      getAllUniqueGraphDataIndex();
      
      if (!rptData.isnDE_Continuous_Mode() && getPhaseTimes() == false)
        return false;
      
      if (readRptData() == false)
        return false;

      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "readData", "", "", "Exception in Initialization ", e);
      cmpRptUsingRtpl.reportStatusLog(cmpRptUsingRtpl.reportSetName, testRun, "init", "", "", "Error: Initialization failed due to exception - " + e);
      return false;
    }
  }

  /**
   * Initialize Test Run Data
   * @return
   */
  public boolean initTestRunData()
  {
    Log.reportDebugLog(className, "initTestRunData", "", "", "Method called. Test run = " + testRun + "Generator = " + genName + " Controller = " + controllerTRNumber);
    try
    {
      //graphNames = new GraphNames(testRun ,null, null,controllerTRNumber, genName, "", false);
      rptData = new ReportData(testRun, genName, controllerTRNumber);
      graphNames = rptData.createGraphNamesObj();

      // Create server signature obj, and initialize it
      serverSignatureInfo = new ServerSignatureInfo(testRun);
      serverSignatureInfo.initServerSignatureInfo();

      Log.reportDebugLog(className, "initTestRunData", "", "", "Reading start packet from RTG file. Test run = " + testRun);

      testRunTotalTime  = rptData.getTestRunDuration();

      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "initTestRunData", "", "", "Exception in Initialization ", e);
      cmpRptUsingRtpl.reportStatusLog(cmpRptUsingRtpl.reportSetName, testRun, "initTestRunData", "", "", "Error: Initialization failed due to exception - " + e);
      return false;
    }

  }

  /**
   * This Function return the Report Graph data Index int Array </br>
   *  And Set the 2D array Row1 = Rpt Name, Row2 = Rpt graph Data Index.
   * @return
   */
  public void getAllUniqueRptGraphDataIdx()
  {
    Log.reportDebugLog(className, "getAllUniqueRptGraphDataIdx", "", "", "Getting Unique graph indexes");
    try
    {
      /*Resetting HashMap.*/
      RptInfo.hmGraphInfoDTOList.clear();
      
      arrRptDetails = RptInfo.getAllRptDetails(rtplName, graphNames);
      
      Log.reportDebugLog(className, "getAllUniqueRptGraphDataIdx", "", "", "Report Names = " + rptUtilsBean.strArrayToStr(arrRptDetails[0], "|"));
      Log.reportDebugLog(className, "getAllUniqueRptGraphDataIdx", "", "", "Graph Data Indexes = " + rptUtilsBean.strArrayToStr(arrRptDetails[1], "|"));
      Log.reportDebugLog(className, "getAllUniqueRptGraphDataIdx", "", "", "Report Group Name = " + rptUtilsBean.strArrayToStr(arrRptDetails[2], "|"));

      //int[] arrGraphDataIndexes = rptUtilsBean.strArrayToIntArray(arrRptDetails[1]);
      //int[] arrUniqueGraphDataIndexes = rptUtilsBean.getUniqueDataFromArray(arrGraphDataIndexes);
      //hmGraphInfoDTO = RptInfo.hmGraphInfoDTOList;
      hmGraphInfoDTO = RptInfo.gethmGraphInfoDTOList();
      
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getAllUniqueRptGraphDataIdx", "", "", "Exception in Initialization ", e);
      cmpRptUsingRtpl.reportStatusLog(cmpRptUsingRtpl.reportSetName, testRun, "getAllUniqueRptGraphDataIdx", "", "", "Error: Error in getting report information from the template.");
    }
  }
  
  /**
   * Method Fetch the Graph Data Index from HashMap and return the array of Graph Data Index.
   */
  public int[] getAllUniqueGraphDataIndex()
  {
    try
    {
      arrRptGraphDataIdx = new int[hmGraphInfoDTO.size()];
      
      Iterator<Entry<Integer, GraphUniqueKeyDTO>> graphIterator = hmGraphInfoDTO.entrySet().iterator();

      int k = 0;

      while(graphIterator.hasNext())
      {
	arrRptGraphDataIdx[k] = graphIterator.next().getKey();
	k++;
      }
      
      return arrRptGraphDataIdx;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }
  

  /**
   * Function read the global.dat File
   * @return
   */
  private boolean getPhaseTimes()
  {
    Log.reportDebugLog(className, "getPhaseTimes", "", "", "Reading global data file for this test run.");
    if (rptData.getPhaseTimes() == false)
    {
      cmpRptUsingRtpl.reportStatusLog(cmpRptUsingRtpl.reportSetName, testRun, "getPhaseTimes", "", "", "Warning: Report raw data file (global.dat) not found for the test run. Option for 'Run Phase Only' if used in tempalate will be converted to 'Total Time'");
    }
    return true;
  }

  /**
   * This function read the Report Data
   * This should read complete data from file as same test run can be used in
   * many measurements.
   * @return
   */
  private boolean readRptData()
  {
    Log.reportDebugLog(className, "readRptData", "", "", "Getting graph report data from rtgMessage.dat file. Test run = " + testRun);
    
    timeBasedTestRunDataObj = rptData.getTimeBasedDataFromRTGFile(hmGraphInfoDTO, cmpRptMsrOptions.timeOption, cmpRptMsrOptions.elapsedStartTime, cmpRptMsrOptions.elapsedEndTime, false, false);

    if (timeBasedTestRunDataObj == null)
    {
      cmpRptUsingRtpl.reportStatusLog(cmpRptUsingRtpl.reportSetName, testRun, "readRptData", "", "", "Error: Error in getting data from raw data file rtgMessage.dat. See error log for details.");
      return false;
    }
      
    Log.reportDebugLog(className, "readRptData", "", "", "End getting all data from raw data file. Time taken " + rptUtilsBean.convMilliSecToStr((new Date()).getTime() - cmpRptUsingRtpl.rptGenStartDate.getTime()));
    return true;
  }

  public String getOverAllAvgCmpData()
  {
    Log.reportDebugLog(className, "getOverAllAvgCmpData", "", "", "Extracting Over All Avg Data = " + strOverAllAvgData);
    if (strOverAllAvgData.equals(""))
    {
      Log.reportDebugLog(className, "getOverAllAvgCmpData", "", "", "Over All Avg Data is not available.");
      strOverAllAvgData = "NA";
    }

    return strOverAllAvgData;
  }

  public String getStdDevDataValue()
  {
    Log.reportDebugLog(className, "getStdDevDataValue", "", "", "Extracting Std Dev Data = " + strStdDevDataValue);

    if (strStdDevDataValue.equals(""))
    {
      Log.reportDebugLog(className, "getStdDevDataValue", "", "", "Std-Dev Data is not available.");
      strStdDevDataValue = "NA";
    }

    return strStdDevDataValue;
  }

  public ReportData getRptData()
  {
    return rptData;
  }
  
  public void setRptData(ReportData reportData)
  {
    this.rptData = reportData;
  }
  
  public void setGraphNames(GraphNames graphNames)
  {
    this.graphNames = graphNames;
  }

  public String getRtplName() 
  {
    return rtplName;
  }
  
  /**
   * Method is Used to get Compare Data Based on Graph Data Index.
   * @param rptName
   * @param graphDataIdx
   * @param cmpRptMsrOptions
   * @return
   */
  public boolean genCmpData(String rptName, int graphDataIdx, CmpRptMsrOptions cmpRptMsrOptions)
  {
    Log.reportDebugLog(className, "genCmpData", "", "", "Method called. MsrName = " + cmpRptMsrOptions.msrName + ", Report Name = " + rptName + ",     graphDataIdx = " + graphDataIdx + ", graphViewType =" + cmpRptMsrOptions.graphViewType + ", graphType = " + cmpRptMsrOptions.graphType + ", granularity = " + cmpRptMsrOptions.granularity + ", xAxisTimeFormat = " + cmpRptMsrOptions.xAxisTimeFormat + ", timeOption = " + cmpRptMsrOptions.timeOption + ", overrideRptOptions (Used always false) = " + cmpRptMsrOptions.overrideRptOptions + ", timeSelectionFormat = " + cmpRptMsrOptions.timeSelectionFormat + ", startDate = " + cmpRptMsrOptions.startDate + ", startTime = " + cmpRptMsrOptions.startTime + ", endDate = " + cmpRptMsrOptions.endDate + ", endTime = " + cmpRptMsrOptions.endTime);

    try
    {
      
      /*Get graph DTO for the required graph data index*/         
      GraphUniqueKeyDTO graphUniqueKeyDTOObj = hmGraphInfoDTO.get(graphDataIdx);

      /*Here Checking if Graph Index exceeds to its length.*/
      if(graphUniqueKeyDTOObj == null)
      {
	Log.errorLog(className, "genCmpData", "", "", "graph Data Index not available in testRun = "+ cmpRptMsrOptions.testRun + ", graphDataIndex = "+ graphDataIndex);
	return false;
      }
    
      /*Getting Graph Data By Graph DTO.*/
      TimeBasedDTO timeBasedDTOObj = timeBasedTestRunDataObj.getTimeBasedDTO(graphUniqueKeyDTOObj);

      if (timeBasedDTOObj == null)
      {
	Log.errorLog(className, "genCmpData", "", "", "Error: No data exist for the report - " + rptName + ", graphDataIdx = " + graphDataIdx);;
	return false;
      }
      
      reportDataUtilsObj = new ReportDataUtils(rptData);
      
      /*Getting Graph Data Type.*/
      int graphDataType = rptData.graphNames.getDataTypeNumByGraphUniqueKeyDTO(graphUniqueKeyDTOObj);
      
      boolean isAvg = reportDataUtilsObj.calculateParametersForAveraging(timeBasedDTOObj.getArrGraphSamplesData(), timeBasedDTOObj.getArrSumSqrData(), timeBasedDTOObj.getArrCountData(), timeBasedDTOObj.getArrMaxData(), timeBasedDTOObj.getArrMinData(), timeBasedTestRunDataObj, cmpRptMsrOptions.graphType, graphDataType, cmpRptMsrOptions.granularity, cmpRptMsrOptions.xAxisTimeFormat, cmpRptMsrOptions.overrideRptOptions, cmpRptMsrOptions.timeOption, cmpRptMsrOptions.elapsedStartTime, cmpRptMsrOptions.elapsedEndTime, -1);
      
      if(!isAvg)
      {
	Log.errorLog(className, "genCmpData", "", "", "Error in Averaging of Graph Sample. Please check error logs.");
	return false;
      }
      
      double avg = reportDataUtilsObj.getGraphAveragedValue();
      double stdDev = reportDataUtilsObj.getGraphStandardDeviationValue(graphDataType);
      double maxData = reportDataUtilsObj.getGraphMaxValue();
      
      /*Getting Average Data.*/
      if(isValidDoubleValue(avg))	
        strOverAllAvgData = rptUtilsBean.convertTodecimal(avg, 3);
      else
	strOverAllAvgData = "NA";
      
      /*Getting Standard deviation Data.*/
      if(isValidDoubleValue(stdDev))	
        strStdDevDataValue = rptUtilsBean.convertTodecimal(stdDev, 3);
      else
	strStdDevDataValue = "NA";
      
      /*Getting Max Data.*/
      if(isValidDoubleValue(maxData))	
        strMaxDataValue = rptUtilsBean.convertTodecimal(maxData, 3);
      else
	strMaxDataValue = "NA";

      return true;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return false;
    }
  }
  
  /**
   * Check For Valid Double Value.
   * @param doubleValue
   * @return
   */
  private boolean isValidDoubleValue(double doubleValue)
  {
    if(Double.isNaN(doubleValue) || Double.isInfinite(doubleValue))
      return false;
    
    return true;
  }
  
  /**
   * Method is used to generate Derived Data.
   * @return
   */
  private DerivedGraphInfo genDerivedData(CmpRptMsrOptions cmpRptMsrOptions, String expression)
  {
    try
    {
      Log.reportDebugLog(className, "genDerivedData", "", "", "Method Called. expression = " + expression);
      
      ArrayList<double[]> arrGraphDataList = new ArrayList<double[]>();
      ArrayList<int[]> arrGraphCountDataList = new ArrayList<int[]>();
      
      ArrayList<double[]> arrMaxDataList = new ArrayList<double[]>();     
      ArrayList<double[]> arrMinDataList = new ArrayList<double[]>();
      
      /*This will keep the last unavearged max value from the max array*/
      double maxLastDataValue = 0.0;
      /*This will keep the last unavearged min value from the min array*/
      double minLastDataValue = Double.MAX_VALUE;
      
      /*Time Stamp Array of averaged data.*/
      ArrayList<Long> avgTimeStampList = new ArrayList<Long>();
      
      DerivedDataProcessor derivedDataProcessor = new DerivedDataProcessor();
      derivedDataProcessor.init(timeBasedTestRunDataObj, timeBasedTestRunDataObj.getDataItemCount());
      DerivedGraphInfo derivedGraphInfoObj = derivedDataProcessor.genDerivedDataByGraphData("DerivedGraph", 250000, expression);
      
      for(int i = 0; i < derivedGraphInfoObj.getArrGraphsList().size(); i++)
      {
        GraphUniqueKeyDTO graphUniqueKeyDTO  = derivedGraphInfoObj.getArrGraphsList().get(i);   
        TimeBasedDTO timeBasedDTOObj = timeBasedTestRunDataObj.getTimeBasedDTO(graphUniqueKeyDTO); 
        int graphDataType = 0;
        
        try
        {
          graphDataType = graphNames.getDataTypeNumByGraphUniqueKeyDTO(graphUniqueKeyDTO);
        }
        catch(Exception e)
        {
          Log.errorLog(className, "genDerivedData", "", "", "Error in getting Data type for graph = " + graphUniqueKeyDTO);
          graphDataType = 0;
        }
        
        if(graphDataType == GraphNameUtils.DATA_TYPE_CUMULATIVE)
          this.graphDataType = GraphNameUtils.DATA_TYPE_CUMULATIVE;
               
        ReportDataUtils reportDataUtilsObj = new ReportDataUtils(rptData);
        reportDataUtilsObj.calculateParametersForAveraging(timeBasedDTOObj.getArrGraphSamplesData(), timeBasedDTOObj.getArrSumSqrData(), timeBasedDTOObj.getArrCountData(), timeBasedDTOObj.getArrMaxData(), timeBasedDTOObj.getArrMinData(), timeBasedTestRunDataObj, cmpRptMsrOptions.graphType, graphDataType, cmpRptMsrOptions.granularity, cmpRptMsrOptions.xAxisTimeFormat, cmpRptMsrOptions.overrideRptOptions, cmpRptMsrOptions.timeOption, cmpRptMsrOptions.elapsedStartTime, cmpRptMsrOptions.elapsedEndTime, -1);          
        lastDerivedSampleCount = lastDerivedSampleCount + reportDataUtilsObj.getLastCountData();
        
        double []arrSampleData = reportDataUtilsObj.getAverageSampleArray();
        int []arrCountData = reportDataUtilsObj.getAverageCountArray();
        
        double []arrMaxData = reportDataUtilsObj.getAverageMaxSampleArray();
        double []arrMinData = reportDataUtilsObj.getAverageMinSampleArray();
        
        if(reportDataUtilsObj.getSampleCount() > 0)
        {
          sampleCount = reportDataUtilsObj.getSampleCount();
          avgTimeStampList = reportDataUtilsObj.getAvgTimeStampList();
        }
        
        /*finding the max last sample for all the graphs in the applied*/
        if(maxLastDataValue < reportDataUtilsObj.getMaxLastData())
          maxLastDataValue = reportDataUtilsObj.getMaxLastData();
        
        /*finding the min last sample for all the graphs in the applied*/
        if(minLastDataValue > reportDataUtilsObj.getMinLastData())
          minLastDataValue = reportDataUtilsObj.getMinLastData();
        
        arrGraphCountDataList.add(arrCountData);
        arrGraphDataList.add(arrSampleData);
        arrMaxDataList.add(arrMaxData);
        arrMinDataList.add(arrMinData);
      }
      
      /*Generating Samples for Derived Graph using Graph Averaged Data.*/
      derivedDataProcessor.genDerivedSampleByGraphData(arrGraphDataList, arrGraphCountDataList, arrMaxDataList, arrMinDataList, maxLastDataValue, minLastDataValue, derivedGraphInfoObj, sampleCount);
      
      reportDataUtilsObj = new ReportDataUtils(rptData);   
      reportDataUtilsObj.sampleCount = sampleCount;
      reportDataUtilsObj.setAvgTimeStampList(avgTimeStampList);
      reportDataUtilsObj.setSampleDataArray(getList(derivedGraphInfoObj.getArrDerivedSampleData()));
      
      return derivedGraphInfoObj;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }
  
 /**
  * This is to calculate Compare data for derived reports.
  * @param arrDerivedData
  * @param rptName
  * @param derivedExpression
  * @param cmpRptMsrOptions
  * @return
  */
  public boolean genCmpDataForDerived(double[] arrDerivedData, String rptName, String derivedExpression, CmpRptMsrOptions cmpRptMsrOptions)
  {
    Log.reportDebugLog(className, "genCmpDataForDerived", "", "", "Method called. MsrName = " + cmpRptMsrOptions.msrName + ", Report Name = " + rptName + ", derived report expression = " + derivedExpression + ", graphViewType =" + cmpRptMsrOptions.graphViewType + ", graphType = " + cmpRptMsrOptions.graphType + ", granularity = " + cmpRptMsrOptions.granularity + ", xAxisTimeFormat = " + cmpRptMsrOptions.xAxisTimeFormat + ", timeOption = " + cmpRptMsrOptions.timeOption + ", overrideRptOptions (Used always false) = " + cmpRptMsrOptions.overrideRptOptions + ", timeSelectionFormat = " + cmpRptMsrOptions.timeSelectionFormat + ", startDate = " + cmpRptMsrOptions.startDate + ", startTime = " + cmpRptMsrOptions.startTime + ", endDate = " + cmpRptMsrOptions.endDate + ", endTime = " + cmpRptMsrOptions.endTime);

    DerivedGraphInfo derivedGraphInfo = genDerivedData(cmpRptMsrOptions, derivedExpression);
    arrDerivedData = derivedGraphInfo.getArrDerivedSampleData();
       
    if (arrDerivedData == null)
    {
      cmpRptUsingRtpl.reportStatusLog(cmpRptUsingRtpl.reportSetName, testRun, "genCmpDataForDerived", "", "", "Error: No data exist for the report - " + rptName + ", derived report expression = " + derivedExpression);
      return false;
    }

    /*Debug level value is greater than 0 than print it in guiDebuglog*/
    if ((Integer.parseInt(Config.getValue("debuglevel"))) > 1)
    {
      Log.debugLog(className, "genCmpDataForDerived", "", "", "Rpt Name = " + rptName + ", derived report expression = " + derivedExpression);
      for (int ii = 0; ii < arrDerivedData.length; ii++)
        Log.debugLog(className, "genCmpDataForDerived", "", "", "Derived Graph Data[" + ii + "] = " + arrDerivedData[ii]);
    }
     
    // Get over all average derived graph data for the required derived graph
    // expression based on the options
    // Note : If all reports that are used to derived data have cumulative data
    // type than only we treated as cumulative else do simple averaging
    double avgData = Double.NaN;
    double maxData = reportDataUtilsObj.getDerivedGraphMaxValue(arrDerivedData, sampleCount);
    
    if(derivedGraphInfo.getDerivedGraphType().get(0) == DerivedGraphInfo.DERIVED_NORMAL_AVG_TYPE_GRAPH)
      avgData = reportDataUtilsObj.getDerivedGraphAverageValue(arrDerivedData, derivedGraphInfo.getArrDerivedCountData(), sampleCount);
    else if(derivedGraphInfo.getDerivedGraphType().get(0) == DerivedGraphInfo.DERIVED_MIN_TYPE_GRAPH)
    {
      avgData = reportDataUtilsObj.getDerivedGraphMinValue(arrDerivedData, sampleCount);
      if(avgData > derivedGraphInfo.getLastDerivedMinData())
	avgData = derivedGraphInfo.getLastDerivedMinData();
      
      /*here we are checking if it is coming double max value we will treat it as zero*/
      if(avgData >= Double.MAX_VALUE)
	avgData = 0.0;
    }
    else if(derivedGraphInfo.getDerivedGraphType().get(0) == DerivedGraphInfo.DERIVED_MAX_TYPE_GRAPH)
    {
      avgData = reportDataUtilsObj.getDerivedGraphMaxValue(arrDerivedData, sampleCount);
      if(avgData < derivedGraphInfo.getLastDerivedMaxData())
	avgData = derivedGraphInfo.getLastDerivedMaxData();
    }
    else if(derivedGraphInfo.getDerivedGraphType().get(0) == DerivedGraphInfo.DERIVED_SUM_TYPE_GRAPH)
      avgData = reportDataUtilsObj.getDerivedGraphCountValue(arrDerivedData, sampleCount) + lastDerivedSampleCount;
    else if(derivedGraphInfo.getDerivedGraphType().get(0) == DerivedGraphInfo.DERIVED_SUM_COUNT_TYPE_GRAPH)
      avgData = reportDataUtilsObj.getDerivedGraphSumCountValue(arrDerivedData, sampleCount) + lastDerivedSampleCount;
    else
      avgData = reportDataUtilsObj.getDerivedGraphAverageValue(arrDerivedData, derivedGraphInfo.getArrDerivedCountData(), sampleCount);
    
    if(maxData < derivedGraphInfo.getLastDerivedMaxData())
      maxData = derivedGraphInfo.getLastDerivedMaxData();
    
    if(maxData >= Double.MAX_VALUE)
      maxData = avgData;
    
    reportDataUtilsObj.reportWidth = 900;
    
    if(isValidDoubleValue(avgData))
      strOverAllAvgData = rptUtilsBean.convertTodecimal(avgData, 3); 
    else
      strOverAllAvgData = "";
    
    /*Getting Max Data.*/
    if(isValidDoubleValue(maxData))	
      strMaxDataValue = rptUtilsBean.convertTodecimal(maxData, 3);
    else
      strMaxDataValue = "NA";

    lastDerivedSampleCount = 0;
    return true;
  }
  
  private ArrayList<Double> getList(double[] arrData)
  {
    ArrayList<Double> arrList = new ArrayList<Double>();
    
    for(int i = 0; i < arrData.length; i++)
      arrList.add(arrData[i]);
    
    return arrList;
  }

  public static void main(String[] args)
  {
    String tR = "5560";
    boolean flag;
    String[][] tmp = null;

    String[] temp = { "Msr_name1|5901|vashist|changes|0|Elapsed|Total Run|Elapsed|NA|NA|NA|NA|7/3/07 18:16:57", "Msr_name2|5902|vashist|changes|0|Elapsed|Total Run|Elapsed|NA|NA|NA|NA|7/3/07 18:17:59", "Msr_name3|6006|vashist|changes|0|Elapsed|Total Run|Elapsed|NA|NA|NA|NA|7/5/07 14:06:00" };

    String temp1 = "Msr_name1|5901|vashist|changes|0|Elapsed|Total Run|Elapsed|NA|NA|NA|NA|7/3/07 18:16:57";
    String temp2 = "Msr_name1|5902|vashist|changes|0|Elapsed|Total Run|Elapsed|NA|NA|NA|NA|7/3/07 18:17:59";
    String temp3 = "Msr_name2|6006|vashist|changes|0|Elapsed|Total Run|Elapsed|NA|NA|NA|NA|7/5/07 14:06:00";
  }
}

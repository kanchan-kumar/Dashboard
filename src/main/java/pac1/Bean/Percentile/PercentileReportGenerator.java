/**
 * Name : PercentileReportGenerator
 * Purpose : To generate percentile/slab report data
 * Author : Pydi
 */
package pac1.Bean.Percentile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import pac1.Bean.ExecutionDateTime;
import pac1.Bean.GraphUniqueKeyDTO;
import pac1.Bean.Log;
import pac1.Bean.PDFNames;
import pac1.Bean.ReportData;
import pac1.Bean.SlabInfo;
import pac1.Bean.TestRunDataType;
import pac1.Bean.TestRunInfo;
import pac1.Bean.TestRunInfoUtils;
import pac1.Bean.TestRunPhaseInfo.Phases;
import pac1.Bean.rptUtilsBean;
import pac1.Bean.GraphName.GraphNames;

public class PercentileReportGenerator 
{
  private String className = "PercentileReportGenerator";
  
  /*This variable hold testRun startData with time (MM/dd/yyyy HH:mm:ss)*/
  private String testRunStartDateTime = "";
  /*Time selection format either elapsed or absolute*/
  private String timeSelectionFormat = "";
  /**/
  private String timeOption = "";
  /*user specified start time*/
  private String startTime = "";
  /*user specified end time*/
  private String endTime = "";
  /*user specified start date*/
  private String startDate = "";
  /*user specified end date*/
  private String endDate = "";
  /*controller test run number*/
  private String controllerTRNumber = "NA";
  /*generator name*/
  private String genName = "NA";
  /*absolute user specified start time stamp*/
  String absltStartTimeStamp = "NA";
  /*absolute user specified end time stamp*/
  String absltEndTimeStamp = "NA";
  /*this will keep phase start time stamp if phase was applied*/
  private String phaseAbsStartTimeStamp = "NA";
  /*this will keep phase end time stamp if phase was applied*/
  private String phaseAbsEndTimeStamp = "NA";
  /*data units*/
  public String formulaUnit = "Secs";
  
  /*specifies graph type(0 -> percentile, 1 -> slab)*/
  private byte graphType = 0;
  
  /*test run number*/
  private int testNum = 0;
  /*debug level*/
  private int debugLevel = 0;
  /*this keeps the all the graph data indexes available in the template*/
  private int[] graphDataIndexArray = null;
  /*test run start time stamp*/
  long testRunStartTimeStamp = 0L;
  
  public double formulaUnitData = 1;
  
  /*grah names object for the test run*/
  private GraphNames graphNames = null;
  /*report data object for the test run*/
  private ReportData rptData = null;
  
  PercentileDataProcessor percentileDataProcessor = null;
  
  /**
   * Constructor for percentile report generation
   * @param testNum
   * @param graphType
   * @param graphDataIndexArray
   * @param controllerTRNumber
   * @param genName
   * @param testRunStartDateTime
   * @param timeSelectionFormat
   * @param timeOption
   * @param startTime
   * @param endTime
   * @param startDate
   * @param endDate
   * @param graphNames
   * @param rptData
   */
  public PercentileReportGenerator(int testNum, byte graphType, int debugLevel, int[] graphDataIndexArray, String controllerTRNumber, String genName, String testRunStartDateTime, String timeSelectionFormat, String timeOption, String startTime, String endTime, String startDate, String endDate, GraphNames graphNames, ReportData rptData) 
  {
    if (debugLevel > 3)
      Log.debugLogAlways(className, "PercentileReportGenerator", "", "", "testNum = " + testNum + ", graphType = " + graphType + ", graphDataIndexArray = " + rptUtilsBean.intArrayToStr(graphDataIndexArray, ",") + ", controllerTRNumber = " + controllerTRNumber + ", genName = " + genName + ", testRunStartDateTime = " + testRunStartDateTime + ", timeSelectionFormat = " + timeSelectionFormat + ", timeOption = " + timeOption + ", startTime = " + startTime + ", endTime = " + endTime + ", startDate = " + startDate + ", endDate = " + endDate);

    this.testNum = testNum;
    this.graphType = graphType;
    this.debugLevel = debugLevel;
    this.graphDataIndexArray = graphDataIndexArray;
    this.controllerTRNumber = controllerTRNumber;
    this.genName = genName;
    this.testRunStartDateTime = testRunStartDateTime;
    this.timeSelectionFormat = timeSelectionFormat;
    this.timeOption = timeOption;
    this.startTime = startTime;
    this.endTime = endTime;
    this.startDate = startDate;
    this.endDate = endDate;
    this.graphNames = graphNames;
    this.rptData = rptData;
  }
  
  /**
   * This method will generate percentile data
   */
  public void generatePercentileData()
  {
    try
    {
      if(debugLevel > 2)
	Log.debugLogAlways(className, "generatePercentileData", "", "", "Method called");
      
      PercentileDataDTO percentileDataDTO = getPercentileDataDTO();
      percentileDataProcessor = new PercentileDataProcessor(new PercentileDataDTO[]{percentileDataDTO});
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  
  
  /**
   * This method will create PercentileDataDTO for requesting percentile data
   * @return PercentileDataDTO
   */
  private PercentileDataDTO getPercentileDataDTO()
  {
    if(debugLevel > 2)
	Log.debugLogAlways(className, "getPercentileDataDTO", "", "", "Method called");
    
    try
    {
      PanelDataInfo[] panelDataInfo = new PanelDataInfo[1];
      panelDataInfo[0] = new PanelDataInfo(debugLevel);
      String dataKey =  getDataKey();
      
      panelDataInfo[0].setDataKey(dataKey);
      panelDataInfo[0].setTestRunDataType(getTestRunDataTypeForGranularityByKey(dataKey));
      panelDataInfo[0].setGraphUniqueKeyList(getGraphUniqueKeyDTOList());
      panelDataInfo[0].setTestRunStartTimeStamp(testRunStartTimeStamp);
      
      /*In case of 'Run Phase Only' we need to set time option as Duration*/
      if(timeOption.equals("Run Phase Only"))
        panelDataInfo[0].setPhaseName("Duration");
      else
	panelDataInfo[0].setPhaseName(timeOption);
      panelDataInfo[0].setStartTime(absltStartTimeStamp);
      panelDataInfo[0].setEndTime(absltEndTimeStamp);

      ArrayList<PanelDataInfo> panelDataInfoList = new ArrayList<PanelDataInfo>();
      panelDataInfoList.add(panelDataInfo[0]);

      int testNum = getTestNum();
      if(!getControllerTRNumber().equals("NA"))
	testNum = Integer.parseInt(getControllerTRNumber());
      
      PercentileDataDTO percentileDataDTO = new PercentileDataDTO(testNum, null, debugLevel);
      // Set Data to PercentileDataDTO class
      percentileDataDTO.setPanelDataInfoList(panelDataInfoList);
      percentileDataDTO.setProfileMode(debugLevel);
      percentileDataDTO.setDebugLevel(debugLevel);
      percentileDataDTO.setGraphType(graphType);
      
      if(TestRunDataType.getTestRunPartitionType(getTestNum()) > 0)
        percentileDataDTO.setPartitionModeEnabled(true);
      else
        percentileDataDTO.setPartitionModeEnabled(false);
      
      return percentileDataDTO;
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * This method sets the start/end time stamp and returns data key
   * @return
   */
  private String getDataKey()
  {
    try
    {
      if(debugLevel > 2)
	Log.debugLogAlways(className, "getDataKey", "", "", "Method called");
      String dataKey = "WholeScenario";
      if(timeOption.trim().equals("Total Run"))
      {
	return dataKey;
      }
      
      long startTimeStamp = 0L;
      long endTimeStamp = 0L;
      long trStartTimeStamp = ExecutionDateTime.convertFormattedDateToMilliscond(testRunStartDateTime.trim(), "MM/dd/yy  HH:mm:ss", null);
      
      if(timeOption.trim().equals("Run Phase Only"))
      {
	setGraphTimeForPhase(timeOption);
	absltStartTimeStamp = phaseAbsStartTimeStamp;
	absltEndTimeStamp = phaseAbsEndTimeStamp;
	testRunStartTimeStamp = trStartTimeStamp;
	return "SPECIFIED_TIME_" + absltStartTimeStamp + "_" + absltEndTimeStamp;
      }
      
      if(timeSelectionFormat.trim().equals("Elapsed"))
      {
	startTimeStamp = trStartTimeStamp + ExecutionDateTime.convertFormattedTimeToMillisecond(startTime, ":");
	endTimeStamp = trStartTimeStamp + ExecutionDateTime.convertFormattedTimeToMillisecond(endTime, ":");
      }
      else
      {
	startTimeStamp = ExecutionDateTime.convertFormattedDateToMilliscond(startDate + " " + startTime, "MM/dd/yyyy HH:mm:ss", null);
	endTimeStamp = ExecutionDateTime.convertFormattedDateToMilliscond(endDate + " " + endTime, "MM/dd/yyyy HH:mm:ss", null);
      }
      absltStartTimeStamp = startTimeStamp + "";
      absltEndTimeStamp = endTimeStamp + "";
      testRunStartTimeStamp = trStartTimeStamp;
      
      return "SPECIFIED_TIME_" + startTimeStamp + "_" + endTimeStamp;
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    return null;
  }
  
  
  /**
   * This method will set start and end time stamp if any phase applied
   * Note: from report we are only supporting 'Run Phase only' = Duration
   * @param appliedPhase
   */
  private void setGraphTimeForPhase(String appliedPhase)
  {
    try
    {
      if(debugLevel > 1)
	Log.debugLogAlways(className, "setGraphTimeForPhase", "", "", "Method called for phaseName = " + appliedPhase);
      
      if(!appliedPhase.trim().equals("Run Phase Only"))
	return;

      /*This was done to get phase start and end times*/
      TestRunInfoUtils testRunInfoUtils = new TestRunInfoUtils();
      TestRunInfo testRunInfo = (TestRunInfo)testRunInfoUtils.getTestRunInfo().get(""+ testNum);
      if(testRunInfo == null)
      {
	Log.debugLogAlways(className, "setGraphTimeForPhase", "", "", "TestRun info not found for testNum = " + testNum);
	return;
      }

      ArrayList<Phases> phaseNameTimeInfo = testRunInfo.getPhaseNameTimingInfo();
      
      if(phaseNameTimeInfo == null)
      {
	Log.debugLogAlways(className, "setGraphTimeForPhase", "", "", "phase Name info comming null . may be reason is global.dat not present in your test run");
	return;
      }

      for(int i = 0;i < phaseNameTimeInfo.size(); i++)
      {
	Phases phase = phaseNameTimeInfo.get(i);
	String tmpPhaseName = phase.getphaseName();
	
	if(tmpPhaseName.trim().startsWith("Duration"))
	{
	  String startTime = phase.getstartTime();
	  String endTime = phase.getendTime();
	  
	  phaseAbsStartTimeStamp = ExecutionDateTime.convertFormattedTimeToMillisecond(startTime, ":") + testRunInfo.getTRStartTimeStamp() + "";
	  phaseAbsEndTimeStamp = ExecutionDateTime.convertFormattedTimeToMillisecond(endTime, ":") + testRunInfo.getTRStartTimeStamp() + "";
	  Log.debugLogAlways(className, "setGraphTimeForPhase", "", "", "phase info: strDateFormat = "+ "" + ", startDate = " + startDate + ", startTime = " + startTime + ", endDate = " + endDate + ", endTime = " + endTime);
	  Log.debugLogAlways(className, "setGraphTimeForPhase", "", "", "phaseAbsStartTimeStamp = " + phaseAbsStartTimeStamp + ", phaseAbsEndTimeStamp = " + phaseAbsEndTimeStamp);
	  return;
	}
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  
  /**
   * This method returns test run data type object
   * @param dataTypeKey
   * @param passedGranularity
   * @return
   */
  private TestRunDataType getTestRunDataTypeForGranularityByKey(String dataTypeKey)
  {
    try
    {
      if(debugLevel > 2)
	Log.debugLogAlways(className, "getTestRunDataTypeForGranularityByKey", "", "", "Method called");
      
      int passedGranularity = -1;
      
      if (debugLevel > 1)
        Log.debugLogAlways(className, "getTestRunDataTypeForGranularityByKey", "", "", "Method Called for dataTypeKey = " + dataTypeKey);

      if (dataTypeKey.trim().startsWith("WholeScenario") || dataTypeKey.trim().equals(""))
      {
        if (debugLevel > 1)
          Log.debugLogAlways(className, "getTestRunDataTypeForGranularityByKey", "", "", "Whole Scenario key");
        return (new TestRunDataType(TestRunDataType.WHOLE_SCENARIO_DATA, passedGranularity, -1, "NA", "NA", false, false));
      }
      else if (dataTypeKey.trim().startsWith("SPECIFIED_TIME_"))
      {
        String tempArr[] = rptUtilsBean.split(dataTypeKey, "_");
        if (debugLevel > 1)
          Log.debugLogAlways(className, "getTestRunDataTypeForGranularityByKey", "", "", "StartTime = " + tempArr[2] + " EndTime = " + tempArr[3]);
        return (new TestRunDataType(TestRunDataType.SPECIFIED_PHASE_OR_TIME, passedGranularity, -1, tempArr[2], tempArr[3], false, false));
      }
    }
    catch (Exception e)
    {
      Log.errorLog(className, "getTestRunDataTypeForGranularityByKey", "", "", "Exception = " + e);
      return null;
    }
    
    return null;
  }
  
  /**
   * This method will return the GraphUniqueKeyDTO for the graphDataIndex taken from the template
   * @return
   */
  private ArrayList<GraphUniqueKeyDTO> getGraphUniqueKeyDTOList()
  {
    try
    {
      if(debugLevel > 2)
	Log.debugLogAlways(className, "getGraphUniqueKeyDTOList", "", "", "Method called");
      
      ArrayList<GraphUniqueKeyDTO> dtoList = new ArrayList<GraphUniqueKeyDTO>();
      
      for(int i = 0; i < graphDataIndexArray.length; i++)
      {
	if(graphDataIndexArray[i] == -1)
	  continue;
	GraphUniqueKeyDTO graphDto = graphNames.getGraphUniqueKeyDTOByGraphDataIndex(graphDataIndexArray[i]);
	if(!getControllerTRNumber().equals("NA"))
	{
	  if(getGenName() != null && !getGenName().equals("NA"))
	  {
	    graphDto.setGeneratorName(getGenName());
	    graphDto.setGeneratorTestNum(getTestNum() + "");
	  }
	}
	dtoList.add(graphDto);
      }
      return dtoList;
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    return null;
  }
  
  
  /**
   * This method will return percentile data for the given graph unique key DTO
   * @param graphUniqueKeyDTO
   * @return
   */
  public double[] getPercentileData(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    if(debugLevel > 2)
      Log.debugLogAlways(className, "getPercentileData", "", "", "Method called for graphUniqueKeyDTO = " + graphUniqueKeyDTO);
    
    double[] data = null;
    try
    {
      if(getPercentileDataProcessor() == null)
      {
	data = new double[101];
	Arrays.fill(data, 0.0);
	return data;
      }
      
      String dataKey = getDataKey();
      PercentileDataKey percentileDataKey = new PercentileDataKey(graphType, dataKey, graphUniqueKeyDTO, null, false, null);
      PercentileDataDTO percentileDataDTO = percentileDataProcessor.getArrPercentileDataDTO()[0];
      HashMap<PercentileDataKey, PercentileInfo> hMap = percentileDataDTO.getPercentileDataMap();
      
      if(hMap == null || !hMap.containsKey(percentileDataKey))
      {
	Log.errorLog(className, "getPercentileData", "", "", "data not found for graph = " + graphUniqueKeyDTO);
	data = new double[101];
	Arrays.fill(data, 0.0);
	return data;
      }
      
      return hMap.get(percentileDataKey).getArrPercentileData();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    
    Log.errorLog(className, "getPercentileData", "", "", "data not found for graph = " + graphUniqueKeyDTO);
    data = new double[101];
    Arrays.fill(data, 0.0);
    return data;
  }
  
  /**
   * This method will return slab info for the given graphUniquekeyDTO
   * @param graphUniqueKeyDTO
   * @return
   */
  public String[] getSlabInfo(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    //if(debugLevel > 2)
      Log.debugLogAlways(className, "getSlabInfo", "", "", "Method called for graphUniqueKeyDTO = " + graphUniqueKeyDTO + ", pdfIndex = " + graphNames.getPDFIdByGraphUniqueKeyDTO(graphUniqueKeyDTO));
    
    try
    {
      String dataKey = getDataKey();
      PercentileDataKey percentileDataKey = new PercentileDataKey(graphType, dataKey, graphUniqueKeyDTO, null, false, null);
      PercentileDataDTO percentileDataDTO = percentileDataProcessor.getArrPercentileDataDTO()[0];
      HashMap<PercentileDataKey, PercentileInfo> hMap = percentileDataDTO.getPercentileDataMap();
      int pdfIndex = graphNames.getPDFIdByGraphUniqueKeyDTO(graphUniqueKeyDTO);
      
      if(!hMap.containsKey(percentileDataKey) && pdfIndex == -1)
      {
	System.out.println("Error in getting slabs information");
	return null;
      }
      
      if(graphNames.getPDFIdByGraphUniqueKeyDTO(graphUniqueKeyDTO) == -1)
	return hMap.get(percentileDataKey).getArrSlabInfo();
      
      SlabInfo[] slabInfo = graphNames.getPdfNames().getArrSlabsInfoByPDFId(graphNames.getPDFIdByGraphUniqueKeyDTO(graphUniqueKeyDTO));
      
      String[] arrSlabNames = new String[slabInfo.length];
      for (int j = 0; j < slabInfo.length; j++)
	arrSlabNames[j] = slabInfo[j].getSlabName();
      
      if(debugLevel > 3)
	Log.debugLogAlways(className, "getSlabInfo", "", "", "slabInfo = " + rptUtilsBean.strArrayToStr(arrSlabNames,","));
      
      return arrSlabNames;
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    return null;
  }
  
  /**
   * This method will return the data index of slabData for the given rptName
   * @param pdfId
   * @param rptName
   * @return
   */
  public int getSlabIndex(int pdfId, String rptName, String[] slabInfo)
  {
    if(debugLevel > 3)
      Log.debugLogAlways(className, "getSlabIndex", "", "", "Method called for pdfId = " + pdfId + ", rptName = " + rptName);
    
    try
    {
      int index = -1;
      PDFNames pdfNames = graphNames.getPdfNames();
      if(pdfNames != null)
        index = graphNames.getPdfNames().getSlabIndexBySlabName(pdfId, rptName.trim());
      
      String[] str = rptName.split("-");
      Log.debugLogAlways(className, "getSlabIndex", "", "", "rptName = " + rptName + ", index = " + index + ", slabInfo = " + rptUtilsBean.strArrayToStr(slabInfo, ","));
      /*index -1 means data was calculated form rtg*/
      if(index == -1)
	index = Integer.parseInt(str[str.length - 2].trim());
      else
      {
	String tmpStr = str[str.length - 2].trim() + "-" + str[str.length - 1].trim();
	tmpStr = tmpStr.replace(" s", "");
	for(int i = 0; i < slabInfo.length; i++)
	{
	  if(slabInfo[i].contains(tmpStr))
	  {
	    index = i;
	    break;
	  }
	}
      }
      
      Log.debugLogAlways(className, "getSlabIndex", "", "", "slab index = " + index);
      return index;
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    return -1;
  }
  
  
  /**
   * As we are keeping default slabCount's in rptInfo for rtgGraphs ,this should be replace with original value 
   * @param pdfId
   * @param rptName
   * @param salbInfo
   * @return
   */
  public String getSlabRptName(int pdfId, String rptName, String salbInfo)
  {
    
    if(debugLevel > 3)
      Log.debugLogAlways(className, "getSlabRptName", "", "", "Method called for pdfId = " + pdfId + ", rptName = " + rptName);
    
    try
    {
      int index = -1;
      PDFNames pdfNames = graphNames.getPdfNames();
      if(pdfNames != null)
        index = graphNames.getPdfNames().getSlabIndexBySlabName(pdfId, rptName.trim());
      String tmpStr = "";
      /*index -1 means this is rtgGraphs*/
      if(index == -1)
	tmpStr = rptName.substring(0 , rptName.lastIndexOf(" - "))  + " - " + salbInfo + " s";
      else
	tmpStr = rptName;
      
      return tmpStr;
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    return rptName;
  }
  
  
  /**
   * Method Used for getting test run startData time
   * @return
   */
  public String getTestRunStartDateTime()
  {
    return testRunStartDateTime;
  }

  /**
   * Method Used for getting time selection format
   * @return
   */
  public String getTimeSelectionFormat()
  {
    return timeSelectionFormat;
  }

  /**
   * Method Used for getting time option
   * @return
   */
  public String getTimeOption()
  {
    return timeOption;
  }

  /**
   * Method Used for getting user specified start time
   * @return
   */
  public String getStartTime()
  {
    return startTime;
  }

  /**
   * Method Used for getting user specified end time
   * @return
   */
  public String getEndTime() 
  {
    return endTime;
  }

  /**
   * Method Used for getting user specified start date
   * @return
   */
  public String getStartDate() 
  {
    return startDate;
  }

  /**
   * Method Used for getting user specified end date
   * @return
   */
  public String getEndDate() 
  {
    return endDate;
  }

  /**
   * Method Used for getting controller test run number
   * @return
   */
  public String getControllerTRNumber() 
  {
    return controllerTRNumber;
  }

  /**
   * Method Used for getting generator name
   * @return
   */
  public String getGenName() 
  {
    return genName;
  }

  /**
   * Method Used for getting absolute start time stamp
   * @return
   */
  public String getAbsltStartTimeStamp() 
  {
    return absltStartTimeStamp;
  }

  /**
   * Method Used for getting absolute end time stamp
   * @return
   */
  public String getAbsltEndTimeStamp() 
  {
    return absltEndTimeStamp;
  }

  /**
   * Method Used for getting
   * @return
   */
  public String getFormulaUnit() 
  {
    return formulaUnit;
  }

  /**
   * Method Used for getting graph type
   * @return
   */
  public byte getGraphType() 
  {
    return graphType;
  }

  /**
   * Method Used for getting test run number
   * @return
   */
  public int getTestNum() 
  {
    return testNum;
  }

  /**
   * Method Used for getting debug level
   * @return
   */
  public int getDebugLevel() 
  {
    return debugLevel;
  }

  /**
   * Method Used for getting template graphData index array
   * @return
   */
  public int[] getGraphDataIndexArray() 
  {
    return graphDataIndexArray;
  }

  /**
   * Method Used for getting test run start time stamp
   * @return
   */
  public long getTestRunStartTimeStamp() 
  {
    return testRunStartTimeStamp;
  }

  /**
   * Method Used for getting
   * @return
   */
  public double getFormulaUnitData() 
  {
    return formulaUnitData;
  }

  /**
   * Method Used for getting graph names object
   * @return
   */
  public GraphNames getGraphNames() 
  {
    return graphNames;
  }

  /**
   * Method Used for getting report data object
   * @return
   */
  public ReportData getRptData() 
  {
    return rptData;
  }

  /**
   * Method Used for getting
   * @return
   */
  public PercentileDataProcessor getPercentileDataProcessor() 
  {
    return percentileDataProcessor;
  }
  
  @Override
  public String toString()
  {
    String str = "graphType = " + graphType + ", timeOption = " + timeOption + ", timeSelectionFormat = " + timeSelectionFormat
	  + ", startDate = " + startDate + ", startTime = " + startTime + ", endDate = " + endDate + ", endTime = " + endTime 
	  + ", testRunStartDateTime = " + testRunStartDateTime ;
    return str; 
  }
  
}

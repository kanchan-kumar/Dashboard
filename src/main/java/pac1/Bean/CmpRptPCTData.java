/*--------------------------------------------------------------------
  @Name    : CmpRptPCTData.java
  @Author  : Prabhat
  @Purpose : Calculate & generate data for percentile reports, through pctMessage.dat file
  @Modification History:
      02/01/2009 --> Prabhat --> Initial Version

----------------------------------------------------------------------*/

package pac1.Bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import pac1.Bean.Percentile.PanelDataInfo;
import pac1.Bean.Percentile.PercentileDataDTO;
import pac1.Bean.Percentile.PercentileDataKey;
import pac1.Bean.Percentile.PercentileDataProcessor;
import pac1.Bean.Percentile.PercentileInfo;


// Class for Compare Report Percentile Data
public class CmpRptPCTData
{
  private String className = "CmpRptPCTData";

  PDFNames pdfNames = null;
  PercentileData percentileData = null;

  CmpRptMsrOptions cmpRptMsrOptions = null;
  
  ReportData rptData = null;
  GraphPCTData[] graphPCTData = null;

  int numTestRun;
  int interval;
  int[] arrPDFID = null;
  int[] arrPCTGraphDataIdx = null;

  int debugLevel = 4;
  byte graphType = 0;
  
  boolean isCmpRpt = true;
  
  public double formulaUnitData = 1;
  
  public String formulaUnit = "Secs";
  String absltStartTimeStamp = "NA";
  String absltEndTimeStamp = "NA";
  long testRunStartTimeStamp = 0L;  
  
  CmpRptTestRunData cmpRptTestRunData = null;
  PercentileDataProcessor percentileDataProcessor = null;
  
  public CmpRptPCTData(int numTestRun, int interval, CmpRptMsrOptions cmpRptMsrOptions, ReportData rptData, PDFNames pdfNames)
  {
    this.numTestRun = numTestRun;
    this.interval = interval;
    this.cmpRptMsrOptions = cmpRptMsrOptions;
    this.rptData = rptData;
    this.pdfNames = pdfNames;
  }

  
  /**
   * This constructor is used in generation of compare reports
   * @param cmpRptTestRunData
   * @param cmpRptMsrOptions
   * @param graphType
   * @param isCmpRpt
   */
  public CmpRptPCTData(CmpRptTestRunData cmpRptTestRunData, CmpRptMsrOptions cmpRptMsrOptions, byte graphType, boolean isCmpRpt)
  {
    this.cmpRptMsrOptions = cmpRptMsrOptions;
    this.cmpRptTestRunData = cmpRptTestRunData;
    this.graphType = graphType;
    this.isCmpRpt = isCmpRpt;
  }
    
  private PercentileDataDTO getPercentileDataDTO()
  {
    try
    {
      PanelDataInfo[] panelDataInfo = new PanelDataInfo[1];
      panelDataInfo[0] = new PanelDataInfo(debugLevel);
      String dataKey = "";
      if(!isCmpRpt)
	dataKey = getDataKeyByRptOptions();
      else
	dataKey = getDataKeyByCmpRptMsrOptions();
      
      panelDataInfo[0].setDataKey(dataKey);
      panelDataInfo[0].setTestRunDataType(getTestRunDataTypeForGranularityByKey(dataKey, -1));
      panelDataInfo[0].setGraphUniqueKeyList(getGraphUniqueKeyDTOList());
      panelDataInfo[0].setTestRunStartTimeStamp(testRunStartTimeStamp);
      panelDataInfo[0].setStartTime(absltStartTimeStamp);
      panelDataInfo[0].setEndTime(absltEndTimeStamp);

      ArrayList<PanelDataInfo> panelDataInfoList = new ArrayList<PanelDataInfo>();
      panelDataInfoList.add(panelDataInfo[0]);

      int testNum = cmpRptTestRunData.getTestRun();
      if(!cmpRptTestRunData.getControllerTRNumber().equals("NA"))
	testNum = Integer.parseInt(cmpRptTestRunData.getControllerTRNumber());
      
      PercentileDataDTO percentileDataDTO = new PercentileDataDTO(testNum, null, debugLevel);
      // Set Data to PercentileDataDTO class
      percentileDataDTO.setPanelDataInfoList(panelDataInfoList);
      percentileDataDTO.setProfileMode(debugLevel);
      percentileDataDTO.setDebugLevel(debugLevel);
      percentileDataDTO.setGraphType(graphType);
      
      if(TestRunDataType.getTestRunPartitionType(cmpRptTestRunData.getTestRun()) > 0)
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
  
  private String getDataKeyByCmpRptMsrOptions()
  {
    try
    {
      String dataKey = "WholeScenario";
      if(cmpRptMsrOptions.timeOption.trim().equals("Total Run"))
      {
	return dataKey;
      }
      
      long startTimeStamp = 0L;
      long endTimeStamp = 0L;
      
      long trStartTimeStamp = ExecutionDateTime.convertFormattedDateToMilliscond(cmpRptMsrOptions.testRunStartDateTime.trim(), "MM/dd/yy  HH:mm:ss", null);
      
      if(cmpRptMsrOptions.timeSelectionFormat.trim().equals("Elapsed"))
      {
	startTimeStamp = trStartTimeStamp + ExecutionDateTime.convertFormattedTimeToMillisecond(cmpRptMsrOptions.startTime, ":");
	endTimeStamp = trStartTimeStamp + ExecutionDateTime.convertFormattedTimeToMillisecond(cmpRptMsrOptions.endTime, ":");
      }
      else
      {
	startTimeStamp = ExecutionDateTime.convertFormattedDateToMilliscond(cmpRptMsrOptions.startDate + " " + cmpRptMsrOptions.startTime, "MM/dd/yyyy HH:mm:ss", null);
	endTimeStamp = ExecutionDateTime.convertFormattedDateToMilliscond(cmpRptMsrOptions.endDate + " " + cmpRptMsrOptions.endTime, "MM/dd/yyyy HH:mm:ss", null);
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
  
  
  private String getDataKeyByRptOptions()
  {
    try
    {
      return "SPECIFIED_TIME_";
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    return null;
  }
  
  private TestRunDataType getTestRunDataTypeForGranularityByKey(String dataTypeKey, int passedGranularity)
  {
    try
    {
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
  
  private ArrayList<GraphUniqueKeyDTO> getGraphUniqueKeyDTOList()
  {
    try
    {
      int[] graphDataIndexArray = cmpRptTestRunData.getRptGraphDataIdxArray();
      ArrayList<GraphUniqueKeyDTO> dtoList = new ArrayList<GraphUniqueKeyDTO>();
      
      for(int i = 0; i < graphDataIndexArray.length; i++)
      {
	if(graphDataIndexArray[i] == -1)
	  continue;
	
	GraphUniqueKeyDTO graphDto = cmpRptTestRunData.graphNames.getGraphUniqueKeyDTOByGraphDataIndex(graphDataIndexArray[i]);
	if(!cmpRptTestRunData.getControllerTRNumber().equals("NA"))
	{
	  if(cmpRptTestRunData.getGenName() != null && !cmpRptTestRunData.getGenName().equals("NA"))
	  {
	    graphDto.setGeneratorName(cmpRptTestRunData.getGenName());
	    graphDto.setGeneratorTestNum(cmpRptTestRunData.getTestRun() + "");
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
  
  public PercentileDataDTO generatePercentileData()
  {
    try
    {
      PercentileDataDTO percentileDataDTO = getPercentileDataDTO();
      percentileDataProcessor = new PercentileDataProcessor(new PercentileDataDTO[]{percentileDataDTO});
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    
    return null;
  }
  
  private PercentileDataProcessor getPercentileDataProcessor() 
  {
    return percentileDataProcessor;
  }
  
  public double[] getPercentileData(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    double[] data = new double[101];
    try
    {
      if(getPercentileDataProcessor() == null)
      {
	Arrays.fill(data, 0.0);
	return data;
      }
      String dataKey = "";
      if(!isCmpRpt)
	dataKey = getDataKeyByRptOptions();
      else
	dataKey = getDataKeyByCmpRptMsrOptions();
      
      PercentileDataKey percentileDataKey = new PercentileDataKey(graphType, dataKey, graphUniqueKeyDTO, null, false, null);
      PercentileDataDTO percentileDataDTO = percentileDataProcessor.getArrPercentileDataDTO()[0];
      HashMap<PercentileDataKey, PercentileInfo> hMap = percentileDataDTO.getPercentileDataMap();
      
      if(hMap == null || !hMap.containsKey(percentileDataKey))
      {
	Arrays.fill(data, 0.0);
	return data;
      }
      
      return hMap.get(percentileDataKey).getArrPercentileData();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    
    Arrays.fill(data, 0.0);
    return data;
  }


  public void printPercentileData()
  {
    try
    {
      if(getPercentileDataProcessor() == null)
      {
	return;
      }
      
      PercentileDataDTO percentileDataDTO = percentileDataProcessor.getArrPercentileDataDTO()[0];
      System.out.println(percentileDataDTO.getPercentileDataMap());
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  // initialize percentile data
  public boolean initPercentileData()
  {
    Log.reportDebugLog(className, "initPercentileData", "", "", "Method called. Test run = " + numTestRun);

    try
    {
      percentileData = new PercentileData(numTestRun, pdfNames);

      if(!percentileData.openPCTMsgFile())
      {
        Log.errorLog(className, "readReport", "", "", "Error: In opening PCT data file.");
        return false;
      }

      if(percentileData.getPhaseTimes() == false)
        return false;

      if(readPCTRptData(arrPDFID, arrPCTGraphDataIdx) == false)
        return false;

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "initPercentileData", "", "", "Exception in Initialization ", e);
      return false;
    }
  }

  // set array PDF Id
  public void setArrayPDFId(int[] arrPDFID)
  {
    this.arrPDFID = arrPDFID;
  }

  // set array PCT graph data idx
  public void setArrayPCTGraphDataIdx(int[] arrPCTGraphDataIdx)
  {
    this.arrPCTGraphDataIdx = arrPCTGraphDataIdx;
  }

  // this function return the array of GraphPCTData
  public GraphPCTData[] getGraphPCTData()
  {
    return graphPCTData;
  }


  // read PCT data from pct data file
  private boolean readPCTRptData(int[] arrPDFID, int[] arrPCTGraphDataIdx)
  {
    Log.reportDebugLog(className, "readPCTRptData", "", "", "Getting pct data from pctMessage.dat file. Test run = " + numTestRun);

    try
    {

      // We should check if start and end time are overriden. Then only do this - To be done later
      Log.reportDebugLog(className, "readPCTRptData", "", "", "Getting percentile data from pct data file. TimeOption = " + cmpRptMsrOptions.timeOption + ", Elasped Start/End Time = " + cmpRptMsrOptions.elapsedStartTime +"/" + cmpRptMsrOptions.elapsedEndTime);

      graphPCTData = percentileData.getAllGraphsDataFromPCTMsgFile(arrPCTGraphDataIdx, arrPDFID, cmpRptMsrOptions.timeOption, cmpRptMsrOptions.elapsedStartTime, cmpRptMsrOptions.elapsedEndTime);

      if(graphPCTData == null)
      {
        Log.errorLog(className, "readReport", "", "", "Error: In gettingn graph PCT data form file.");
        return false;
      }

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "readPCTRptData", "", "", "Exception in getting percentile data - ", e);
      return false;
    }
  }
}
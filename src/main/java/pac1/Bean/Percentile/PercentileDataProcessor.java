/**
 * This class is for Generating Percentile Data 
 * @author Ravi Kant Sharma
 * @since Netsorm Version 4.0.0
 * @Modification_History Ravi Kant Sharma - Initial Version 4.0.0
 * @version 4.0.0
 * 
 */
package pac1.Bean.Percentile;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

import pac1.Bean.BinaryFileReader;
import pac1.Bean.DerivedDataDTO;
import pac1.Bean.DerivedDataProcessor;
import pac1.Bean.DerivedGraphInfo;
import pac1.Bean.ExecutionDateTime;
import pac1.Bean.GeneratorUniqueKey;
import pac1.Bean.GraphUniqueKeyDTO;
import pac1.Bean.Log;
import pac1.Bean.PDFNames;
import pac1.Bean.ReportData;
import pac1.Bean.Scenario;
import pac1.Bean.SessionReportData;
import pac1.Bean.TestRunDataType;
import pac1.Bean.TimeBasedDTO;
import pac1.Bean.TimeBasedTestRunData;
import pac1.Bean.rptUtilsBean;
import pac1.Bean.GraphName.GraphNames;

public class PercentileDataProcessor implements PercentileErrorDefinition
{
  private static String className = "PercentileDataProcessor";

  private ArrayList<PctPartitionInfo> validPartitionList = null;

  /**
   * List of PercentileFileOffset. Used in Partition and Non Partition Mode
   */
  private ArrayList<PercentileFileOffset> percentileFileOffsetList = new ArrayList<PercentileFileOffset>();

  /**
   * On the basis of Debug Level, Need to write debug logs
   */
  private int debugLevel = 0;

  /**
   * This variable is used for monitoring the time,
   * 
   * Means how much time taken by the method
   */
  private int profileMode = 0;

  /**
   * GraphType
   * 
   * 0 - PERCENTILE,
   * 
   * 1 - SLAB COUNT,
   * 
   * 2 - Frequency Distribution
   */
  private byte graphType = -1;

  /**
   * This is the request which is send by client (Dashboard, Transaction, Reporting).
   * 
   * as well as Bean will fill the percentile data.
   */
  private PercentileDataDTO[] arrPercentileDataDTO = null;

  /**
   * Used for each graph per measurement
   * 
   */
  private PercentileDataGenerator[] arrPctDataGenerator = null;

  /**
   * This is the map for storing graph names object.
   * 
   * Key - GraphNamesObjKey (Test Run Number, Generator Test Run Number and Generator Name)
   * 
   * Value - GraphNames Object
   * 
   * It is used for reuse of GraphNames object
   */
  private HashMap<GraphNamesObjKey, GraphNames> graphNamesObjMap = new HashMap<GraphNamesObjKey, GraphNames>();

  private HashMap<String, String> generatorNameAndTR = new HashMap<String, String>();

  /**
   * Used for storing current(That we are generating percentile data) test run.
   * 
   * Note - For each measurement, testRunNumber may change
   * 
   */
  private int testRunNumber = -1;

  /**
   * This PercentileDataDTO is used as temporary.
   */
  private PercentileDataDTO percentileDataDTO = null;

  // This array is used as temporary
  private ArrayList<PanelDataInfo> panelDataInfoList = null;
  
  //Object of SessionReportData.
  private SessionReportData sessionReportData = null;

  /**
   * This map is for storing separate graph list data key wise
   */
  private HashMap<String, PercentileGraphList> pctGraphListMap = new HashMap<String, PercentileGraphList>();

  /**
   * This Constructor is used for set PercentileDataDTO[] and call init() method
   * 
   * @param arrPercentileDataDTO
   */
  public PercentileDataProcessor(PercentileDataDTO[] arrPercentileDataDTO)
  {
    try
    {
      long startProcessTime = System.currentTimeMillis();

      Log.debugLogAlways(className, "Constructor:PercentileDataProcessor", "", "", "Method Called.");

      if (arrPercentileDataDTO == null || arrPercentileDataDTO.length == 0)
      {
        Log.errorLog(className, "Constructor:PercentileDataProcessor", "", "", "CRITICAL ERROR: Cannot generate percentile data as arrPercentileDataDTO is null or zero length.");
        return;
      }

      this.arrPercentileDataDTO = arrPercentileDataDTO;
      // This method is to set partition mode
      setPartitionMode();

      debugLevel = arrPercentileDataDTO[0].getDebugLevel();

      if (debugLevel > 0)
        Log.debugLogAlways(className, "Constructor:PercentileDataProcessor", "", "", "debugLevel = " + debugLevel);

      if (debugLevel > 0)
        Log.debugLogAlways(className, "PercentileDataProcessor", "", "", "arrPercentileDataDTO len = " + arrPercentileDataDTO.length);

      init();

      if (debugLevel > 0)
      {
        for (int i = 0; i < arrPercentileDataDTO.length; i++)
        {
          Log.debugLogAlways(className, "PercentileDataProcessor", "", "", "PercentileDataDTO = " + arrPercentileDataDTO[i] + ", percentileDataMap = " + arrPercentileDataDTO[i].getPercentileDataMap());
        }
      }

      if (profileMode > 1)
      {
        long endProcessTime = System.currentTimeMillis();
        Log.debugLogAlways(className, "PercentileDataProcessor", "", "", "Total Time Taken for generating data = " + (endProcessTime - startProcessTime) / 1000);
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "PercentileDataProcessor", "", "", "Exception - ", e);
    }
  }

  /**
   * This method initialize all data structures and common variables
   * 
   * before generating the percentile data.
   */
  private void init()
  {
    try
    {
      long startProcessTime = System.currentTimeMillis();

      if (debugLevel > 0)
        Log.debugLogAlways(className, "init", "", "", "Method Called.");

      for (int i = 0; i < arrPercentileDataDTO.length; i++)
      {
        percentileDataDTO = arrPercentileDataDTO[i];

        if (debugLevel > 0)
          Log.debugLogAlways(className, "init", "", "", "Start process for generating percentile for " + percentileDataDTO);

        // Each measurement can have different 2 debug level
        debugLevel = percentileDataDTO.getDebugLevel();

        // Getting Profile Mode
        profileMode = percentileDataDTO.getProfileMode();

        // Getting Graph Type
        graphType = percentileDataDTO.getGraphType();

        // Getting Test Run Number from percentileDataDTO. It is send by client
        testRunNumber = percentileDataDTO.getTestRunNumber();

        // Getting PanelDataInfo[] which is send by client.
        panelDataInfoList = percentileDataDTO.getPanelDataInfoList();

        percentileFileOffsetList.clear();
        generatorNameAndTR.clear();

        /**
         * Generate an data structure to stores graphs data key wise.
         * 
         * Data Structure Type = Hash Map
         * 
         * Data Structure Name = pctGraphListMap
         * 
         * Key -> Data Key (e.g. Whole Scenario)
         * 
         * Value -> PercentileGraphList (Used for separate list of graphs (pctMessage.dat/rtgMessage.dat))
         * 
         */
        
        if(sessionReportData == null)
        {
          Log.debugLogAlways(className, "init", "", "", "Creating new instance of SessionReportData");
          sessionReportData = new SessionReportData(testRunNumber);
        }
        fillPercentileGraphList();

        if (graphType == PercentileDataUtils.GRAPH_TYPE_PERCENTILE || graphType == PercentileDataUtils.GRAPH_TYPE_SLAB_COUNT)
          generatePercentileData();
      }

      if (profileMode > 1)
      {
        long endProcessTime = System.currentTimeMillis();
        Log.debugLogAlways(className, "init", "", "", "Total Time Taken for method init = " + (endProcessTime - startProcessTime) / 1000);
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "init", "", "", "Exception - ", e);
    }
  }

  public PercentileDataDTO[] getArrPercentileDataDTO()
  {
    return arrPercentileDataDTO;
  }

  public void setArrPercentileDataDTO(PercentileDataDTO[] arrPercentileDataDTO)
  {
    this.arrPercentileDataDTO = arrPercentileDataDTO;
  }

  /**
   * This method generate a hash map
   * 
   * Key - Data Key
   * 
   * Value - {@link PercentileGraphList}
   * 
   * 
   * @param arrPanelDataInfo
   * @return
   */
  private void fillPercentileGraphList()
  {
    try
    {
      long startProcessTime = System.currentTimeMillis();

      if (debugLevel > 0)
        Log.debugLogAlways(className, "fillPercentileGraphList", "", "", "Method Called. panelDataInfoList size = " + panelDataInfoList.size());

      for (int i = 0; i < panelDataInfoList.size(); i++)
      {
        // Getting each PanelDataInfo
        PanelDataInfo panelDataInfo = panelDataInfoList.get(i);

        // Getting Data key e.g. WholeScenario, Last N Hours
        String dataKey = panelDataInfo.getDataKey();

        // Getting total validPartitionList.
        StringBuffer errMsg = new StringBuffer();
        int modeType = getPercentileModeType();
        String startTime = panelDataInfo.getStartTime();
        String endTime = panelDataInfo.getEndTime();
        String phaseName = panelDataInfo.getPhaseName();

        validPartitionList = PercentileDataUtils.getValidPartitionList(debugLevel, modeType, testRunNumber, startTime, endTime, phaseName, percentileDataDTO.isPartitionModeEnabled(), "NA", "NA", sessionReportData, errMsg);

        if (debugLevel > 2)
          Log.debugLogAlways(className, "fillPercentileGraphList", "", "", "modeType = " + modeType + ", startTime = " + startTime + ", endTime = " + endTime + ", validPartitionList = " + validPartitionList);

        // Creating graph names object for each partition
        createGraphNamesForAllPartitions(validPartitionList);

        /**
         * Getting PercentileGraphList for panel data key.
         * 
         * if PercentileGraphList is null then create instance of PercentileGraphList
         */
        PercentileGraphList percentileGraphList = pctGraphListMap.get(dataKey);
        if (percentileGraphList == null)
          percentileGraphList = new PercentileGraphList(debugLevel);

        /**
         * add all derived expression in PercentileGraphList
         * 
         */
        // List of all derived graphs from panel
        ArrayList<String> derivedExpList = panelDataInfo.getDerivedExpList();
        if (derivedExpList != null && derivedExpList.size() != 0)
        {
          for (int j = 0; j < derivedExpList.size(); j++)
          {
            String derivedExp = derivedExpList.get(j);
            percentileGraphList.addDerivedExp(derivedExp);
          }
        }

        /**
         * Generate separate list of pctMessage.dat and rtgMessage.dat graphs
         * 
         * List of all non derived graphs from panel
         */
        ArrayList<GraphUniqueKeyDTO> graphUniqueKeyDTOs = panelDataInfo.getGraphUniqueKeyList();

        if (debugLevel > 1)
          Log.debugLogAlways(className, "fillPercentileGraphList", "", "", "graphUniqueKeyDTOs = " + graphUniqueKeyDTOs);

        if (graphUniqueKeyDTOs != null && graphUniqueKeyDTOs.size() != 0)
        {
          for (int j = 0; j < graphUniqueKeyDTOs.size(); j++)
          {
            // Getting each GraphUniqueKeyDTO from Panel
            GraphUniqueKeyDTO graphUniqueKeyDTO = graphUniqueKeyDTOs.get(j);
            graphUniqueKeyDTO = fillAndGetGeneratorsAndTRMap(graphUniqueKeyDTO);

            boolean isGraphAvailable = isGraphAvaiInAnyFilteredPartition(validPartitionList, graphUniqueKeyDTO);
            if (!isGraphAvailable)
            {
              Log.errorLog(className, "fillPercentileGraphList", "", "", "Graph " + graphUniqueKeyDTO + " is not available in any partition.");
              continue;
            }

            boolean isGraphGenFromPct = checkGraphGenFromPct(validPartitionList, graphUniqueKeyDTO);

            if (debugLevel > 0)
              Log.debugLogAlways(className, "fillPercentileGraphList", "", "", "isGraphGenFromPct = " + isGraphGenFromPct + ", graphUniqueKeyDTO = " + graphUniqueKeyDTO);

            if (isGraphGenFromPct)
            {
              if (graphUniqueKeyDTO.getGeneratorName() != null && graphUniqueKeyDTO.getGeneratorTestNum() != null && !graphUniqueKeyDTO.getGeneratorTestNum().equals("NA"))
                percentileGraphList.addGraphInPctListByGenerator(dataKey, graphUniqueKeyDTO);
              else
                percentileGraphList.addGraphInPctList(graphUniqueKeyDTO);
            }
            else
            {
              if (graphUniqueKeyDTO.getGeneratorName() != null && graphUniqueKeyDTO.getGeneratorTestNum() != null && !graphUniqueKeyDTO.getGeneratorTestNum().equals("NA"))
                percentileGraphList.addGraphInRtgListByGenerator(dataKey, graphUniqueKeyDTO);
              else
                percentileGraphList.addGraphInRtgList(graphUniqueKeyDTO);
            }

          }
        }

        if (debugLevel > 1)
          Log.debugLogAlways(className, "fillPercentileGraphList", "", "", "graphUniqueKeyDTOs = " + graphUniqueKeyDTOs + ", DataKey = " + dataKey + " -> " + percentileGraphList);

        pctGraphListMap.put(dataKey, percentileGraphList);
      }

      if (profileMode > 1)
      {
        long endProcessTime = System.currentTimeMillis();
        Log.debugLogAlways(className, "fillPercentileGraphList", "", "", "Total Time Taken By Method fillPercentileGraphList = " + (endProcessTime - startProcessTime) / 1000 + " seconds");
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "fillPercentileGraphList", "", "", "Exception - ", e);
    }
  }

  /**
   * This method fill entry in generatorNameAndTR for given graphUniqueKeyDTO
   * 
   * @param graphUniqueKeyDTO
   */
  private GraphUniqueKeyDTO fillAndGetGeneratorsAndTRMap(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    try
    {
      // Getting Generator Name from GraphUniqueKeyDTO. It is used in NetCloud
      String generator_Name = graphUniqueKeyDTO.getGeneratorName();

      // Getting generatorTestRunNum from GraphUniqueKeyDTO. It is used in NetCloud
      String generatorTestRunNum = graphUniqueKeyDTO.getGeneratorTestNum();

      if (generatorNameAndTR.containsKey(generator_Name))
        generatorTestRunNum = generatorNameAndTR.get(generator_Name);
      else
        generatorTestRunNum = PercentileDataUtils.getGeneratorTestRunNumByGeneratorName(testRunNumber, generator_Name);

      generatorNameAndTR.put(generator_Name, generatorTestRunNum);

      graphUniqueKeyDTO.setGeneratorTestNum(generatorTestRunNum);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "fillAndGetGeneratorsAndTRMap", "", "", "Exception - ", e);
    }

    return graphUniqueKeyDTO;
  }

  /**
   * Make sure - validPartitionList is not null
   * 
   * This method is for finding valid partitions for given graph
   * 
   * @param validPartitionList
   * @param graphUniqueKeyDTO
   * @return
   */
  private ArrayList<PctPartitionInfo> getFilteredPctPartitionListByGraph(ArrayList<PctPartitionInfo> validPartitionList, GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "getFilteredPctPartitionListByGraph", "", "", "Method Called. graphUniqueKeyDTO = " + graphUniqueKeyDTO);

      ArrayList<PctPartitionInfo> validPartitionListNew = new ArrayList<PctPartitionInfo>();
      for (int i = 0; i < validPartitionList.size(); i++)
      {
        PctPartitionInfo pctPartitionInfoObj = validPartitionList.get(i);
        GraphNamesObjKey graphNamesObjKey = new GraphNamesObjKey(testRunNumber, pctPartitionInfoObj.getPartitionName(), pctPartitionInfoObj.getGeneratorTestRunNumber(), pctPartitionInfoObj.getGeneratorName());
        GraphNames graphNamesObj = graphNamesObjMap.get(graphNamesObjKey);

        int pdfId = graphNamesObj.getPDFIdByGraphUniqueKeyDTO(graphUniqueKeyDTO);
        long pdfDataIndex = graphNamesObj.getPDFDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTO);
        int graphDataIndex = graphNamesObj.getGraphDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTO);

        if (debugLevel > 1)
          Log.debugLogAlways(className, "getFilteredPctPartitionListByGraph", "", "", "pdfId = " + pdfId + ", pdfDataIndex = " + pdfDataIndex + ", graphUniqueKeyDTO = " + graphUniqueKeyDTO);

        // Need to check Graph Data whether exist in pctMessage.dat
        if (pdfId <= 0 || pdfDataIndex < 0 || graphDataIndex == -1)
          continue;
        else
          validPartitionListNew.add(pctPartitionInfoObj);
      }

      return validPartitionListNew;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getFilteredPctPartitionListByGraph", "", "", "Exception - ", e);
      return validPartitionList;
    }
  }

  /**
   * This method creates graph names object for all partitions
   * 
   * @param validPartitionList
   */
  private void createGraphNamesForAllPartitions(ArrayList<PctPartitionInfo> validPartitionList)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "createGraphNamesForAllPartitions", "", "", "Method Called. validPartitionList = " + validPartitionList);

      // Create Graph Names object and put in hashMap for valid partitions
      for (int j = 0; j < validPartitionList.size(); j++)
      {
        PctPartitionInfo pctPartitionInfoObj = validPartitionList.get(j);
        GraphNamesObjKey graphNamesObjKey = new GraphNamesObjKey(testRunNumber, pctPartitionInfoObj.getPartitionName(), pctPartitionInfoObj.getGeneratorTestRunNumber(), pctPartitionInfoObj.getGeneratorName());

        GraphNames graphNamesObj = null;
        if (graphNamesObjMap.containsKey(graphNamesObjKey))
        {
          graphNamesObj = graphNamesObjMap.get(graphNamesObjKey);
        }
        else
        {
          graphNamesObj = genGraphNamesObj(pctPartitionInfoObj.getGeneratorName(), pctPartitionInfoObj.getGeneratorTestRunNumber(), testRunNumber, pctPartitionInfoObj.getPartitionName());
          graphNamesObjMap.put(graphNamesObjKey, graphNamesObj);
        }
      }

      if (debugLevel > 2)
        Log.debugLogAlways(className, "createGraphNamesForAllPartitions", "", "", "size = " + graphNamesObjMap.size() + ", graphNamesObjMap = " + graphNamesObjMap);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "createGraphNamesForAllPartitions", "", "", "Exception - ", e);
    }
  }

  /**
   * Make sure this method must call after creating GraphNames object for all given filtered partitions.
   * 
   * Purpose of this method is to check given graph is available in any given filtered partitions
   * 
   * @param validPartitionList
   * @param graphUniqueKeyDTOObj
   * 
   * @return true/false
   */
  private boolean isGraphAvaiInAnyFilteredPartition(ArrayList<PctPartitionInfo> validPartitionList, GraphUniqueKeyDTO graphUniqueKeyDTOObj)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "isGraphAvaiInAnyFilteredPartition", "", "", "Method Called - validPartitionList = " + validPartitionList + ", graphUniqueKeyDTOObj = " + graphUniqueKeyDTOObj);

      for (int i = 0; i < validPartitionList.size(); i++)
      {
        PctPartitionInfo pctPartitionInfoObj = validPartitionList.get(i);
        GraphNamesObjKey graphNamesObjKey = new GraphNamesObjKey(testRunNumber, pctPartitionInfoObj.getPartitionName(), pctPartitionInfoObj.getGeneratorTestRunNumber(), pctPartitionInfoObj.getGeneratorName());
        GraphNames graphNamesObj = graphNamesObjMap.get(graphNamesObjKey);

        int index = graphNamesObj.getGraphDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTOObj);

        if (index > 0)
          return true;
      }

      return false;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "isGraphAvaiInAnyFilteredPartition", "", "", "Exception = ", e);
      return false;
    }
  }

  /**
   * This method check is this graph need to generate from pctMessage.dat if yes return true
   * 
   * @param validPartitionList
   * @param graphUniqueKeyDTOObj
   * @return
   */
  private boolean checkGraphGenFromPct(ArrayList<PctPartitionInfo> validPartitionList, GraphUniqueKeyDTO graphUniqueKeyDTOObj)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "checkGraphGenFromPct", "", "", "Method Called for graphUniqueKeyDTOObj = " + graphUniqueKeyDTOObj);

      int counter = 0;
      for (int i = 0; i < validPartitionList.size(); i++)
      {
        PctPartitionInfo pctPartitionInfoObj = validPartitionList.get(i);
        GraphNamesObjKey graphNamesObjKey = new GraphNamesObjKey(testRunNumber, pctPartitionInfoObj.getPartitionName(), pctPartitionInfoObj.getGeneratorTestRunNumber(), pctPartitionInfoObj.getGeneratorName());
        GraphNames graphNamesObj = graphNamesObjMap.get(graphNamesObjKey);

        int pdfId = graphNamesObj.getPDFIdByGraphUniqueKeyDTO(graphUniqueKeyDTOObj);
        long pdfDataIndex = graphNamesObj.getPDFDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTOObj);

        if (debugLevel > 1)
          Log.debugLogAlways(className, "checkGraphGenFromPct", "", "", "pdfId = " + pdfId + ", pdfDataIndex = " + pdfDataIndex + ", graphUniqueKeyDTOObj = " + graphUniqueKeyDTOObj);

        // Need to check Graph Data whether exist in pctMessage.dat
        if (pdfId <= 0 || pdfDataIndex < 0)
          counter++;
      }

      if (debugLevel > 1)
        Log.debugLogAlways(className, "checkGraphGenFromPct", "", "", "counter = " + counter + ", validPartitionList size = " + validPartitionList.size());

      if (counter == validPartitionList.size())
        return false;
      else
        return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "checkGraphGenFromPct", "", "", "Exception - ", e);
      return false;
    }
  }

  /**
   * This method is used to generate percentile data for derived, non derived graphs and NetCloud Graphs.
   * 
   * Non Derived Graphs -> rtgMessage.dat/pctMessage.dat
   */
  private void generatePercentileData()
  {
    try
    {
      long startProcessTime = System.currentTimeMillis();

      if (debugLevel > 0)
        Log.debugLogAlways(className, "generatePercentileData", "", "", "Method Called.");

      for (Map.Entry<String, PercentileGraphList> entry : pctGraphListMap.entrySet())
      {
        String dataKey = entry.getKey();
        PercentileGraphList percentileDataList = entry.getValue();

        // Getting list of derived expression
        ArrayList<String> derivedExpList = percentileDataList.getDerivedExpList();

        /**
         * Generating percentile data for derived graphs.
         * 
         * Note: Percentile Date will generated from rtgMessage.dat file
         */
        if (derivedExpList != null && derivedExpList.size() != 0)
        {
          PanelDataInfo panelDataInfo = getPanelDataInfoByDataKey(dataKey);
          genPCTDataForDerivedFrmRTG(derivedExpList, panelDataInfo);
        }

        // Getting list of Graphs for which percentile data would be generated from rtgMessage.dat file.
        ArrayList<GraphUniqueKeyDTO> rtgGraphList = percentileDataList.getRtgGraphsList();

        if (debugLevel > 1)
          Log.debugLogAlways(className, "generatePercentileData", "", "", "rtgGraphList = " + rtgGraphList);

        /**
         * Generating percentile data from rtgMessage.dat file for non - derived graphs
         * 
         * Note: Percentile Data will generated from rtgMessage.dat
         */
        if (rtgGraphList != null && rtgGraphList.size() != 0)
          genPCTDataFrmRTG(dataKey, rtgGraphList, null, false);

        // Getting list of all graphs that is to be generated from rtgMessage.dat for each generator.
        HashMap<GeneratorUniqueKey, ArrayList<GraphUniqueKeyDTO>> generatorRtgGraphsMap = percentileDataList.getGeneratorRtgGraphsMap();

        if (debugLevel > 1)
          Log.debugLogAlways(className, "generatePercentileData", "", "", "generatorRtgGraphsMap = " + generatorRtgGraphsMap);

        /**
         * Generating percentile data for all generators from rtgMessage.dat file
         * 
         * Note: For now we are generating percentile data for generators from rtgMessage.dat file.
         */
        if (generatorRtgGraphsMap != null && generatorRtgGraphsMap.size() != 0)
          genPCTDataFrmRTG(dataKey, rtgGraphList, generatorRtgGraphsMap, true);

        // Getting list of all graphs that is to be generated from rtgMessage.dat for each generator.
        HashMap<GeneratorUniqueKey, ArrayList<GraphUniqueKeyDTO>> generatorPctGraphsMap = percentileDataList.getGeneratorPctGraphsMap();

        if (debugLevel > 1)
          Log.debugLogAlways(className, "generatePercentileData", "", "", "generatorPctGraphsMap = " + generatorPctGraphsMap);

        if (generatorPctGraphsMap == null || generatorPctGraphsMap.size() == 0)
        {
          /**
           * Getting list of Graphs for which percentile data would be
           * 
           * generated from pctMessage.dat file.
           */
          ArrayList<GraphUniqueKeyDTO> pctGraphList = percentileDataList.getPctGraphsList();

          if (debugLevel > 1)
            Log.debugLogAlways(className, "generatePercentileData", "", "", "pctGraphList = " + pctGraphList);

          if (pctGraphList != null && pctGraphList.size() != 0)
          {
            for (int i = 0; i < pctGraphList.size(); i++)
            {
              GraphUniqueKeyDTO graphUniqueKeyDTO = pctGraphList.get(i);
              processPCTData(dataKey, null, null, graphUniqueKeyDTO);
            }
          }
        }
        else
        {
          Iterator<GeneratorUniqueKey> iterator = generatorPctGraphsMap.keySet().iterator();
          while (iterator.hasNext())
          {
            GeneratorUniqueKey generatorUniqueKey = iterator.next();

            if (debugLevel > 1)
              Log.debugLogAlways(className, "generatePercentileData", "", "", "generatorUniqueKey = " + generatorUniqueKey);

            ArrayList<GraphUniqueKeyDTO> graphUniqueKeyDTOListForPct = generatorPctGraphsMap.get(generatorUniqueKey);
            for (int i = 0; i < graphUniqueKeyDTOListForPct.size(); i++)
            {
              GraphUniqueKeyDTO graphUniqueKeyDTO = graphUniqueKeyDTOListForPct.get(i);
              processPCTData(dataKey, generatorUniqueKey.getGeneratorTestRunNum(), generatorUniqueKey.getGeneratorName(), graphUniqueKeyDTO);
            }
          }
        }
      }

      if (profileMode > 1)
      {
        long endProcessTime = System.currentTimeMillis();
        Log.debugLogAlways(className, "generatePercentileData", "", "", "Total Time Taken for method generatePercentileData = " + (endProcessTime - startProcessTime) / 1000);
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "generatePercentileData", "", "", "Exception - ", e);
    }
  }

  /**
   * This method is for generating percentile data for derived
   * 
   * @param derivedExpList
   */
  private void genPCTDataForDerivedFrmRTG(ArrayList<String> derivedExpList, PanelDataInfo panelDataInfo)
  {
    try
    {
      long startProcessTime = System.currentTimeMillis();

      if (debugLevel > 0)
        Log.debugLogAlways(className, "genPCTDataForDerivedFrmRTG", "", "", "Method Called. derivedExpList = " + derivedExpList);

      // Getting Derived Data DTO, used for generating time based test run data.
      DerivedDataDTO derivedDataDTO = genDerivedDataDTO(derivedExpList);

      ReportData rptData = new ReportData(testRunNumber);
      rptData.setDerivedDataDTO(derivedDataDTO);

      ArrayList<GraphUniqueKeyDTO> graphUniqueKeyDTOs = PercentileDataUtils.getGraphUniqueKeyDTOListFromExpressionList(derivedExpList);
      LinkedHashMap<Integer, GraphUniqueKeyDTO> hmGraphInfoDTOList = getMapByDataIndexAndGraphUniqueKey(graphUniqueKeyDTOs);

      String startTime = panelDataInfo.getStartTime();
      String endTime = panelDataInfo.getEndTime();
      if (panelDataInfo.isBaseline())
        panelDataInfo = getChangePanelDataInfo(panelDataInfo);

      startTime = panelDataInfo.getStartTime();
      endTime = panelDataInfo.getEndTime();

      if (!percentileDataDTO.isPartitionModeEnabled())
      {
        try
        {
          if (startTime != null && !startTime.equals("NA"))
          {
            long st = Long.parseLong(startTime);
            st = st - panelDataInfo.getTestRunStartTimeStamp();

            if (st < 0)
            {
              Log.errorLog(className, "genPCTDataForDerivedFrmRTG", "", "", "For test Run" + testRunNumber + " time Zone is not correct so using Start Time as NA");
              startTime = "NA";
            }
            else
            {
              startTime = rptUtilsBean.convMilliSecToStr(st);
            }
          }

          if (endTime != null && !endTime.equals("NA"))
          {
            long et = Long.parseLong(endTime);
            et = et - panelDataInfo.getTestRunStartTimeStamp();
            if (et < 0)
            {
              Log.errorLog(className, "genPCTDataForDerivedFrmRTG", "", "", "For test Run " + testRunNumber + " time Zone is not correct so using End Time as NA");
              endTime = "NA";
            }
            else
            {
              endTime = rptUtilsBean.convMilliSecToStr(et);
            }
          }

          if (debugLevel > 1)
            Log.debugLogAlways(className, "genPCTDataForDerivedFrmRTG", "", "", "startTime = " + startTime + ", endTime = " + endTime);
        }
        catch (Exception e)
        {
          Log.errorLog(className, "genPCTDataForDerivedFrmRTG", "", "", "Exception - " + e);
        }
      }

      TimeBasedTestRunData timeBasedTestRunData = rptData.getTimeBasedDataFromRTGFile(hmGraphInfoDTOList, "", startTime, endTime, false, false, panelDataInfo.getTestRunDataType());

      DerivedDataProcessor derivedDataProcessor = timeBasedTestRunData.getDerivedDataProcessor();
      ArrayList<Integer> derivedGraphNumList = derivedDataProcessor.getDerivedGraphNumbers();
      for (int i = 0; i < derivedGraphNumList.size(); i++)
      {
        int derivedGraphNumber = derivedGraphNumList.get(i);
        DerivedGraphInfo derivedGraphInfo = derivedDataProcessor.getDerivedGraphInfoByDerivedGraphNumber(derivedGraphNumber);
        int dataItemCount = timeBasedTestRunData.getDataItemCount();
        String derivedExp = derivedGraphInfo.getDerivedGraphFormula();

        double[] rawValueNew = derivedGraphInfo.getArrDerivedSampleData();

        if (debugLevel > 2)
          Log.debugLogAlways(className, "genPCTDataForDerivedFrmRTG", "", "", "dataItemCount = " + dataItemCount + " and Raw data for graph = " + derivedExp + " -> " + rptUtilsBean.doubleArrayToList(rawValueNew));

        double[] rawValue = new double[dataItemCount];
        double[] slabRawData = new double[dataItemCount];
        for (int j = 0; j < rawValue.length; j++)
        {
          slabRawData[j] = rawValueNew[j];
          rawValue[j] = rawValueNew[j];
        }

        if (rawValue == null || rawValue.length == 0)
        {
          Log.debugLogAlways(className, "genPCTDataForDerivedFrmRTG", "", "", "rawValue may be null or zero length.");
          continue;
        }

        Arrays.sort(rawValue);

        PercentileDataKey percentileDataKey = new PercentileDataKey(graphType, panelDataInfo.getDataKey(), null, derivedExp, true, panelDataInfo.getUniqueKey());
        double[] arrPercentileData = PercentileDataUtils.getPercentileData(debugLevel, rawValue);

        if (debugLevel > 2)
          Log.debugLogAlways(className, "genPCTDataForDerivedFrmRTG", "", "", "percentileDataKey = " + percentileDataKey + ", arrPercentileData = " + rptUtilsBean.doubleArrayToList(arrPercentileData));

        PercentileInfo percentileInfo = new PercentileInfo();

        if (graphType == PercentileDataUtils.GRAPH_TYPE_PERCENTILE)
        {
          percentileInfo.setArrPercentileData(arrPercentileData);
        }
        else if (graphType == PercentileDataUtils.GRAPH_TYPE_SLAB_COUNT)
        {
          AutoSlabInfo autoSlabInfo = RTGSlabInfo.getAutoSlabInfo(debugLevel, arrPercentileData);
          double[] arrSlabData = RTGSlabInfo.getArrSlabData(slabRawData, autoSlabInfo);
          percentileInfo.setArrSlabInfo(autoSlabInfo.getArrSlabInfo());
          percentileInfo.setArrPercentileData(arrSlabData);
        }

        percentileDataDTO.addPercentileDataInMap(percentileDataKey, percentileInfo);
      }

      if (profileMode > 1)
      {
        long endProcessTime = System.currentTimeMillis();
        Log.debugLogAlways(className, "genPCTDataForDerivedFrmRTG", "", "", "Total Time Taken By Method genPCTDataForDerivedFrmRTG = " + (endProcessTime - startProcessTime) / 1000 + " seconds");
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "genPCTDataForDerivedFrmRTG", "", "", "Exception - ", e);
    }
  }

  /**
   * This method is for only generating Derived Data DTO. It is used to create
   * 
   * derived data from rtgMessage.dat file.
   * 
   * @param derivedExpList
   * @return
   */
  private DerivedDataDTO genDerivedDataDTO(ArrayList<String> derivedExpList)
  {
    long startProcessTime = System.currentTimeMillis();
    DerivedDataDTO derivedDataDTO = new DerivedDataDTO();

    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "genDerivedDataDTO", "", "", "Method Called.");

      String[] arrDerivedGraphFormula = derivedExpList.toArray(new String[derivedExpList.size()]);
      String[] arrDerivedGraphName = new String[derivedExpList.size()];
      Arrays.fill(arrDerivedGraphName, "Derived Graphs");

      int[] arrDerivedGraphNumber = new int[derivedExpList.size()];
      int derivedCounter = 250001;
      for (int i = 0; i < arrDerivedGraphNumber.length; i++)
      {
        arrDerivedGraphNumber[i] = derivedCounter;
        derivedCounter++;
      }

      // Generating DerivedDataDTO, It is must to create derived data
      derivedDataDTO.setArrDerivedGraphFormula(arrDerivedGraphFormula);
      derivedDataDTO.setArrDerivedGraphName(arrDerivedGraphName);
      derivedDataDTO.setArrDerivedGraphNumber(arrDerivedGraphNumber);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "genDerivedDataDTO", "", "", "Exception - ", e);
    }

    if (profileMode > 1)
    {
      long endProcessTime = System.currentTimeMillis();
      Log.debugLogAlways(className, "genDerivedDataDTO", "", "", "Total Time Taken for method genDerivedDataDTO = " + (endProcessTime - startProcessTime) / 1000);
    }

    return derivedDataDTO;
  }

  /**
   * This method generate percentile data for given graphUniqueKeyDTO from rtgMessage.dat
   * 
   * @param numTestRun
   * @param graphUniqueKeyDTO
   */
  private void genPCTDataFrmRTG(String panelDataKey, ArrayList<GraphUniqueKeyDTO> rtgGraphUniqueKeyDTO, HashMap<GeneratorUniqueKey, ArrayList<GraphUniqueKeyDTO>> generatorRtgGraphsMap, boolean isGeneratorCall)
  {
    try
    {
      long startProcessTime = System.currentTimeMillis();

      if (debugLevel > 0)
        Log.debugLogAlways(className, "genPCTDataFrmRTG", "", "", "rtgGraphUniqueKeyDTO = " + rtgGraphUniqueKeyDTO + ", isGeneratorCall = " + isGeneratorCall + ", generatorRtgGraphsMap = " + generatorRtgGraphsMap);

      if (isGeneratorCall)
      {
        for (Map.Entry<GeneratorUniqueKey, ArrayList<GraphUniqueKeyDTO>> entry : generatorRtgGraphsMap.entrySet())
        {
          try
          {
            GeneratorUniqueKey generatorUniqueKey = entry.getKey();

            // Generating percentileDataKey
            String dataKey = generatorUniqueKey.getDataKey();

            ArrayList<GraphUniqueKeyDTO> graphUniqueKeyDTOs = entry.getValue();
            int generatorTestNum = Integer.parseInt(generatorUniqueKey.getGeneratorTestRunNum().trim());
            ReportData reportData = new ReportData(generatorTestNum, generatorUniqueKey.getGeneratorName(), "" + testRunNumber);
            LinkedHashMap<Integer, GraphUniqueKeyDTO> hmGraphInfoDTOList = getMapByDataIndexAndGraphUniqueKey(graphUniqueKeyDTOs);

            if (debugLevel > 1)
              Log.debugLogAlways(className, "genPCTDataFrmRTG", "", "", "hmGraphInfoDTOList = " + hmGraphInfoDTOList);

            // Getting Panel Data Info
            PanelDataInfo panelDataInfo = getPanelDataInfoByDataKey(dataKey);

            if (debugLevel > 1)
              Log.debugLogAlways(className, "genPCTDataFrmRTG", "", "", "panelDataInfo = " + panelDataInfo);

            String startTime = panelDataInfo.getStartTime();
            String endTime = panelDataInfo.getEndTime();

            if (panelDataInfo.isBaseline())
              panelDataInfo = getChangePanelDataInfo(panelDataInfo);

            startTime = panelDataInfo.getStartTime();
            endTime = panelDataInfo.getEndTime();

            if (!percentileDataDTO.isPartitionModeEnabled())
            {
              try
              {
                if (startTime != null && !startTime.equals("NA"))
                {
                  long st = Long.parseLong(startTime);
                  st = st - panelDataInfo.getTestRunStartTimeStamp();
                  if (st < 0)
                  {
                    Log.errorLog(className, "genPCTDataFrmRTG", "", "", "For test Run " + testRunNumber + " time Zone is not correct so using Start Time as NA");
                    startTime = "NA";
                  }
                  else
                  {
                    startTime = rptUtilsBean.convMilliSecToStr(st);
                  }
                }

                if (endTime != null && !endTime.equals("NA"))
                {
                  long et = Long.parseLong(endTime);
                  et = et - panelDataInfo.getTestRunStartTimeStamp();

                  if (et < 0)
                  {
                    Log.errorLog(className, "genPCTDataFrmRTG", "", "", "For test Run " + testRunNumber + " time Zone is not correct so using End Time as NA");
                    endTime = "NA";
                  }
                  else
                  {
                    endTime = rptUtilsBean.convMilliSecToStr(et);
                  }
                }

                if (debugLevel > 1)
                  Log.debugLogAlways(className, "genPCTDataFrmRTG", "", "", "startTime = " + startTime + ", endTime = " + endTime);
              }
              catch (Exception e)
              {
                Log.errorLog(className, "genPCTDataFrmRTG", "", "", "Exception - " + e);
              }
            }

            /**
             * Note: We have to pass start and end time (Elapsed) always.
             */
            TimeBasedTestRunData timeBasedTestRunData = reportData.getTimeBasedDataFromRTGFile(hmGraphInfoDTOList, "", startTime, endTime, false, false, panelDataInfo.getTestRunDataType());
            for (int i = 0; i < graphUniqueKeyDTOs.size(); i++)
            {
              GraphUniqueKeyDTO graphUniqueKeyDTO = graphUniqueKeyDTOs.get(i);
              TimeBasedDTO timeBasedDTO = timeBasedTestRunData.getTimeBasedDTO(graphUniqueKeyDTO);
              double[] arrRawData = timeBasedDTO.getArrGraphSamplesData();

              if (arrRawData == null || arrRawData.length == 0)
              {
                Log.errorLog(className, "genPCTDataFrmRTG", "", "", "rawValue may be null or zero length.");
                continue;
              }

              double[] arrRawDataNew = new double[timeBasedTestRunData.getDataItemCount()];
              for (int j = 0; j < timeBasedTestRunData.getDataItemCount(); j++)
              {
                arrRawDataNew[j] = arrRawData[j];
              }

              Arrays.sort(arrRawDataNew);
              if (debugLevel > 3)
                Log.debugLogAlways(className, "genPCTDataFrmRTG", "", "", "graphUniqueKeyDTO = " + graphUniqueKeyDTO + ", arrRawData = " + rptUtilsBean.doubleArrayToList(arrRawDataNew));

              PercentileInfo percentileInfo = new PercentileInfo();

              // Calculating percentile data
              double[] arrPercentileData = PercentileDataUtils.getPercentileData(debugLevel, arrRawDataNew);
              if (graphType == PercentileDataUtils.GRAPH_TYPE_PERCENTILE)
              {
                if (debugLevel > 3)
                  Log.debugLogAlways(className, "genPCTDataFrmRTG", "", "", "Request Type = GRAPH_TYPE_PERCENTILE");

                percentileInfo.setArrPercentileData(arrPercentileData);
              }
              else if (graphType == PercentileDataUtils.GRAPH_TYPE_SLAB_COUNT)
              {
                if (debugLevel > 3)
                  Log.debugLogAlways(className, "genPCTDataFrmRTG", "", "", "Request Type = GRAPH_TYPE_SLAB_COUNT");

                AutoSlabInfo autoSlabInfo = RTGSlabInfo.getAutoSlabInfo(debugLevel, arrPercentileData);
                double[] arrSlabData = RTGSlabInfo.getArrSlabData(arrRawData, autoSlabInfo);
                percentileInfo.setArrSlabInfo(autoSlabInfo.getArrSlabInfo());
                percentileInfo.setArrPercentileData(arrSlabData);
              }

              PercentileDataKey percentileDataKey = new PercentileDataKey(graphType, dataKey, graphUniqueKeyDTO, null, false, panelDataInfo.getUniqueKey());

              if (debugLevel > 2)
                Log.debugLogAlways(className, "genPCTDataFrmRTG", "", "", "percentileDataKey = " + percentileDataKey + ", percentileDataKey = " + percentileDataKey + ", arrPercentileData = " + rptUtilsBean.doubleArrayToList(arrPercentileData));

              // filling percentile data into percentile data DTO
              percentileDataDTO.addPercentileDataInMap(percentileDataKey, percentileInfo);
            }
          }
          catch (Exception e)
          {
            Log.stackTraceLog(className, "genPCTDataFrmRTG", "", "", "Exception", e);
          }
        }
      }
      else
      {
        ReportData reportData = new ReportData(testRunNumber);
        reportData.setDebugLevel(debugLevel);

        LinkedHashMap<Integer, GraphUniqueKeyDTO> hmGraphInfoDTOList = getMapByDataIndexAndGraphUniqueKey(rtgGraphUniqueKeyDTO);

        if (debugLevel > 2)
          Log.debugLogAlways(className, "genPCTDataFrmRTG", "", "", "panelDataKey = " + panelDataKey + ", hmGraphInfoDTOList = " + hmGraphInfoDTOList);

        // Getting Panel Data Info
        PanelDataInfo panelDataInfo = getPanelDataInfoByDataKey(panelDataKey);

        if (debugLevel > 2)
          Log.debugLogAlways(className, "genPCTDataFrmRTG", "", "", "panelDataInfo = " + panelDataInfo);

        String startTime = panelDataInfo.getStartTime();
        String endTime = panelDataInfo.getEndTime();
        if (panelDataInfo.isBaseline())
          panelDataInfo = getChangePanelDataInfo(panelDataInfo);

        startTime = panelDataInfo.getStartTime();
        endTime = panelDataInfo.getEndTime();

        if (!percentileDataDTO.isPartitionModeEnabled())
        {
          try
          {
            if (startTime != null && !startTime.equals("NA"))
            {
              long st = Long.parseLong(startTime);
              st = st - panelDataInfo.getTestRunStartTimeStamp();
              if (st < 0)
              {
                Log.errorLog(className, "genPCTDataFrmRTG", "", "", "For test run " + testRunNumber + " time zone is not correct. So using start time is NA.");
                startTime = "NA";
              }
              else
              {
                startTime = rptUtilsBean.convMilliSecToStr(st);
              }
            }

            if (endTime != null && !endTime.equals("NA"))
            {
              long et = Long.parseLong(endTime);
              et = et - panelDataInfo.getTestRunStartTimeStamp();
              if (et < 0)
              {
                Log.errorLog(className, "genPCTDataFrmRTG", "", "", "For test run " + testRunNumber + " time zone is not correct. So using end time is NA.");
                endTime = "NA";
              }
              else
              {
                endTime = rptUtilsBean.convMilliSecToStr(et);
              }
            }

            if (debugLevel > 1)
              Log.debugLogAlways(className, "genPCTDataFrmRTG", "", "", "startTime = " + startTime + ", endTime = " + endTime);
          }
          catch (Exception e)
          {
            Log.errorLog(className, "genPCTDataFrmRTG", "", "", "Exception - " + e);
          }
        }

        TimeBasedTestRunData timeBasedTestRunData = reportData.getTimeBasedDataFromRTGFile(hmGraphInfoDTOList, "", startTime, endTime, false, false, panelDataInfo.getTestRunDataType());
        for (int i = 0; i < rtgGraphUniqueKeyDTO.size(); i++)
        {
          try
          {
            GraphUniqueKeyDTO graphUniqueKeyDTO = rtgGraphUniqueKeyDTO.get(i);
            TimeBasedDTO timeBasedDTO = timeBasedTestRunData.getTimeBasedDTO(graphUniqueKeyDTO);
            if (timeBasedDTO == null)
            {
              Log.errorLog(className, "genPCTDataFrmRTG", "", "", "timeBasedDTO = " + timeBasedDTO + ", graphUniqueKeyDTO = " + graphUniqueKeyDTO);
              continue;
            }

            double[] arrRawDataNew = timeBasedDTO.getArrGraphSamplesData();
            if (arrRawDataNew == null || arrRawDataNew.length == 0)
            {
              Log.errorLog(className, "genPCTDataFrmRTG", "", "", "arrRawDataNew = null graphUniqueKeyDTO = " + graphUniqueKeyDTO);
              continue;
            }
            double[] arrRawData = null;
            int dataItemCount = timeBasedTestRunData.getDataItemCount();
            arrRawData = new double[dataItemCount];
            for (int j = 0; j < dataItemCount; j++)
            {
              arrRawData[j] = arrRawDataNew[j];
            }

            if (arrRawData == null || arrRawData.length == 0)
            {
              Log.errorLog(className, "genPCTDataFrmRTG", "", "", "arrRawDataNew = null or zero len graphUniqueKeyDTO = " + graphUniqueKeyDTO);
              continue;
            }

            if (debugLevel > 2)
              Log.debugLogAlways(className, "genPCTDataFrmRTG", "", "", "for " + graphUniqueKeyDTO + ", dataItemCount = " + dataItemCount + ", raw data = " + rptUtilsBean.doubleArrayToList(arrRawData));

            Arrays.sort(arrRawData);

            // Generating percentileDataKey
            PercentileDataKey percentileDataKey = new PercentileDataKey(graphType, panelDataKey, graphUniqueKeyDTO, null, false, panelDataInfo.getUniqueKey());
            // Calculating percentile data
            double[] arrPercentileData = null;
            PercentileInfo percentileInfo = new PercentileInfo();
            arrPercentileData = PercentileDataUtils.getPercentileData(debugLevel, arrRawData);

            if (graphType == PercentileDataUtils.GRAPH_TYPE_PERCENTILE)
            {
              percentileInfo.setArrPercentileData(arrPercentileData);
            }
            else if (graphType == PercentileDataUtils.GRAPH_TYPE_SLAB_COUNT)
            {
              AutoSlabInfo autoSlabInfo = RTGSlabInfo.getAutoSlabInfo(debugLevel, arrPercentileData);
              double[] arrSlabData = RTGSlabInfo.getArrSlabData(arrRawData, autoSlabInfo);
              percentileInfo.setArrSlabInfo(autoSlabInfo.getArrSlabInfo());
              percentileInfo.setArrPercentileData(arrSlabData);
            }

            if (debugLevel > 2)
              Log.debugLogAlways(className, "genPCTDataFrmRTG", "", "", "percentileDataKey = " + percentileDataKey + ", arrPercentileData = " + rptUtilsBean.doubleArrayToList(arrPercentileData));

            // filling percentile data into percentile data DTO
            percentileDataDTO.addPercentileDataInMap(percentileDataKey, percentileInfo);
          }
          catch (Exception e)
          {
            Log.stackTraceLog(className, "genPCTDataFrmRTG", "", "", "Exception", e);
          }
        }
      }

      if (profileMode > 1)
      {
        long endProcessTime = System.currentTimeMillis();
        Log.debugLogAlways(className, "genPCTDataFrmPCT", "", "", "Total Time Taken by method genPCTDataFrmPCT = " + (endProcessTime - startProcessTime) / 1000);
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "genPCTDataFrmRTG", "", "", "Exception - ", e);
    }
  }

  private PanelDataInfo getChangePanelDataInfo(PanelDataInfo panelDataInfo)
  {
    if (debugLevel > 0)
      Log.debugLogAlways(className, "getChangePanelDataInfo", "", "", "Method Called with panelDataInfo = " + panelDataInfo + ", testRunNumber = " + testRunNumber);

    if (panelDataInfo.getStartTime() == null || panelDataInfo.getStartTime().equals("NA NA"))
      panelDataInfo.setStartTime("NA");

    if (panelDataInfo.getEndTime() == null || panelDataInfo.getEndTime().equals("NA NA"))
      panelDataInfo.setEndTime("NA");

    if (!panelDataInfo.getStartTime().startsWith("NA") && !panelDataInfo.getEndTime().startsWith("NA"))
    {
      try
      {
        TimeZone trTimeZone = ExecutionDateTime.getSystemTimeZoneGMTOffset();
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
        format.setTimeZone(trTimeZone);
        String trStartTime = Scenario.getTestRunStartTime(testRunNumber);
        long testRunStartTimeStamp = format.parse(trStartTime).getTime();
        panelDataInfo.setTestRunStartTimeStamp(testRunStartTimeStamp);

        // Convert start date time
        try
        {
          Long.parseLong(panelDataInfo.getStartTime());
        }
        catch (Exception e)
        {
          String startTime = "" + ExecutionDateTime.convertFormattedDateToMilliscond(panelDataInfo.getStartTime(), "MM/dd/yyyy HH:mm:ss", trTimeZone);
          panelDataInfo.setStartTime(startTime);
        }

        // Convert end date time
        try
        {
          Long.parseLong(panelDataInfo.getEndTime());
        }
        catch (Exception e)
        {
          String endTime = "" + ExecutionDateTime.convertFormattedDateToMilliscond(panelDataInfo.getEndTime(), "MM/dd/yyyy HH:mm:ss", trTimeZone);
          panelDataInfo.setEndTime(endTime);
        }

        // Nothing is different now.
        panelDataInfo.setBaseline(false);
      }
      catch (Exception e)
      {
        Log.stackTraceLog(className, "getChangePanelDataInfo", "", "", "Exception - ", e);
      }
    }

    if (debugLevel > 0)
      Log.debugLogAlways(className, "getChangePanelDataInfo", "", "", "Method End with panelDataInfo = " + panelDataInfo);

    return panelDataInfo;
  }

  /**
   * This function will process pctMessage.dat file for Partition Mode
   * 
   * @param percentileDataDTO
   */
  private void processPCTData(String dataKey, String generatorTestRunNum, String generator_Name, GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "processPCTData", "", "", "Method Called. dataKey = " + dataKey + ", generatorTestRunNum = " + generatorTestRunNum + ", generator_Name = " + generator_Name);

      // Getting Panel Data Info
      PanelDataInfo panelDataInfo = getPanelDataInfoByDataKey(dataKey);

      if (debugLevel > 0)
        Log.debugLogAlways(className, "processPCTData", "", "", "panelDataInfo = " + panelDataInfo);

      if (panelDataInfo.isBaseline())
        panelDataInfo = getChangePanelDataInfo(panelDataInfo);

      String phaseName = panelDataInfo.getPhaseName();
      String startTime = panelDataInfo.getStartTime();
      String endTime = panelDataInfo.getEndTime();
      boolean isPartitionModeEnabled = percentileDataDTO.isPartitionModeEnabled();
      int modeType = getPercentileModeType();

      if (debugLevel > 0)
        Log.debugLogAlways(className, "processPCTData", "", "", "startTime = " + startTime + ", endTime = " + endTime + ", phaseName = " + phaseName + ", isPartitionModeEnabled = " + isPartitionModeEnabled + ", mode type = " + modeType);

      // Getting filtered valid partition list
      StringBuffer errMsg = new StringBuffer();
      ArrayList<PctPartitionInfo> filteredAndValidPartitionList = getFilteredPctPartitionListByGraph(validPartitionList, graphUniqueKeyDTO);
      if (filteredAndValidPartitionList == null || filteredAndValidPartitionList.size() == 0)
      {
        errMsg = new StringBuffer();
        errMsg.append("ERROR: cannot generate percentile data as pctMessage.dat file does not exist in test " + testRunNumber);
        percentileDataDTO.setErrMsg(errMsg);
        return;
      }

      errMsg = new StringBuffer();
      if (isGranuleChangeInAnyPartition(filteredAndValidPartitionList, graphUniqueKeyDTO, errMsg))
      {
        percentileDataDTO.setErrMsg(errMsg);
        return;
      }

      errMsg.setLength(0);

      if (debugLevel > 2)
        Log.debugLogAlways(className, "processPCTData", "", "", "filteredAndValidPartitionList = " + filteredAndValidPartitionList);

      String timeOption = getTimeOptionByDataKey(dataKey, panelDataInfo);

      // Going to set file off set
      setFileOffSets(modeType, dataKey, timeOption, filteredAndValidPartitionList, errMsg);

      // Checking is there any error occurs during set file off set
      if (errMsg != null && errMsg.length() != 0)
      {
        Log.errorLog(className, "processPCTData", "", "", "errMsg = " + errMsg);
        percentileDataDTO.setErrMsg(errMsg);
        return;
      }

      // Checking wheter percentileFileOffsetList must not be null and size should be greater then 0
      if (percentileFileOffsetList == null || percentileFileOffsetList.size() == 0)
      {
        Log.errorLog(className, "processPCTData", "", "", "percentileFileOffsetList must not be null and size should be greater then 0");
        errMsg.setLength(0);
        errMsg.append("ERROR: cannot generate percentile data as not able to set file off set.");
        percentileDataDTO.setErrMsg(errMsg);
        return;
      }

      if (debugLevel > 0)
        Log.debugLogAlways(className, "processPCTData", "", "", "final percentileFileOffsetList = " + percentileFileOffsetList);

      String partitionName = "";
      for (int i = 0; i < percentileFileOffsetList.size(); i++)
      {
        PercentileFileOffset percentileFileOffset = percentileFileOffsetList.get(i);
        partitionName = percentileFileOffset.getEndPartitionName();
        percentileFileOffset = setAndGetStartAndEndDataPacket(percentileFileOffset, graphUniqueKeyDTO);
        percentileFileOffsetList.set(i, percentileFileOffset);
      }

      double[] arrCumulativeSum = getCumulativeSum();
      if (arrCumulativeSum == null || arrCumulativeSum.length == 0)
      {
        Log.errorLog(className, "processPCTData", "", "", "arrCumulativeSum is null.");
        return;
      }

      if (debugLevel > 2)
        Log.debugLogAlways(className, "processPCTData", "", "", "arrCumulativeSum = " + rptUtilsBean.doubleArrayToList(arrCumulativeSum));

      GraphNamesObjKey graphNamesObjKey = new GraphNamesObjKey(testRunNumber, partitionName, generatorTestRunNum, generator_Name);
      GraphNames graphNamesObj = graphNamesObjMap.get(graphNamesObjKey);
      if (graphNamesObj == null)
      {
        Log.errorLog(className, "processPCTData", "", "", "Bug:::: graphNamesObj is null.");
        return;
      }

      PctMsgInfo pctMsgInfo = new PctMsgInfo();
      pctMsgInfo.setGraphNames(graphNamesObj);

      arrPctDataGenerator = new PercentileDataGenerator[1];

      for (int i = 0; i < arrPctDataGenerator.length; i++)
      {
        setPctMsgInfo(pctMsgInfo, graphUniqueKeyDTO);
        double[] arrPCTCumDataBuckets = arrCumulativeSum;

        if (debugLevel > 0)
          Log.debugLogAlways(className, "processPCTData", "", "", "arrPCTCumDataBuckets = " + rptUtilsBean.doubleArrayToList(arrPCTCumDataBuckets) + ", arrPCTCumDataBuckets length = " + arrPCTCumDataBuckets.length);

        arrPctDataGenerator[i] = new PercentileDataGenerator(debugLevel, pctMsgInfo, graphUniqueKeyDTO);
        arrPctDataGenerator[i].setArrPCTCumDataBuckets(arrPCTCumDataBuckets);
        double[] arrPercentileData = null;

        if (graphType == PercentileDataUtils.GRAPH_TYPE_PERCENTILE)
          arrPercentileData = arrPctDataGenerator[i].getArrPercentileData();
        else if (graphType == PercentileDataUtils.GRAPH_TYPE_SLAB_COUNT)
          arrPercentileData = arrPctDataGenerator[i].getArrSlabsData();

        if (debugLevel > 0 && arrPercentileData != null)
          Log.debugLogAlways(className, "processPCTData", "", "", "Percentile data = " + rptUtilsBean.doubleArrayToList(arrPercentileData) + " and graphUniqueKeyDTO = " + graphUniqueKeyDTO);

        PercentileInfo percentileInfo = new PercentileInfo();
        percentileInfo.setArrPercentileData(arrPercentileData);
        PercentileDataKey percentileDataKey = new PercentileDataKey(graphType, dataKey, graphUniqueKeyDTO, null, false, panelDataInfo.getUniqueKey());

        if (debugLevel > 0)
          Log.debugLogAlways(className, "processPCTData", "", "", "percentileDataKey = " + percentileDataKey + ", percentileInfo = " + percentileInfo);

        percentileDataDTO.addPercentileDataInMap(percentileDataKey, percentileInfo);
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "processPCTData", "", "", "Exception - ", e);
    }
  }

  /**
   * 
   * @param filteredAndValidPartitionList
   * @param graphUniqueKeyDTO
   * @param errMsg
   * @return
   */
  private boolean isGranuleChangeInAnyPartition(ArrayList<PctPartitionInfo> filteredAndValidPartitionList, GraphUniqueKeyDTO graphUniqueKeyDTO, StringBuffer errMsg)
  {
    try
    {
      int prevGranuleSize = -1;
      for (int i = 0; i < filteredAndValidPartitionList.size(); i++)
      {
        PctPartitionInfo pctPartitionInfo = filteredAndValidPartitionList.get(i);
        GraphNamesObjKey graphNamesObjKey = new GraphNamesObjKey(testRunNumber, pctPartitionInfo.getPartitionName(), graphUniqueKeyDTO.getGeneratorTestNum(), graphUniqueKeyDTO.getGeneratorName());
        GraphNames graphNames = graphNamesObjMap.get(graphNamesObjKey);
        if (graphNames == null)
          continue;

        PDFNames pdfNames = graphNames.getPdfNames();
        if (pdfNames == null)
          continue;

        int pdfId = graphNames.getPDFIdByGraphUniqueKeyDTO(graphUniqueKeyDTO);
        long pdfDataIndex = graphNames.getPDFDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTO);
        if (pdfId <= 0 || pdfDataIndex < 0)
          continue;

        int granuleSize = pdfNames.getNumGranuleByPDFId(pdfId);
        if (prevGranuleSize != granuleSize && prevGranuleSize != -1)
        {
          String previousPartitionName = filteredAndValidPartitionList.get(i - 1).getPartitionName();
          errMsg.append("ERROR: Cannot generate percentile data as granule size is changed.\n Previous Partition Name = " + previousPartitionName + ", Previous Granule Size = " + prevGranuleSize + "" + " \n Changed Partition Name = " + pctPartitionInfo.getPartitionName() + ", Changed Granule Size = " + granuleSize);
          return true;
        }
        else
        {
          prevGranuleSize = granuleSize;
        }
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "isGranuleChangeInAnyPartition", "", "", "Exception - ", e);
    }

    return false;
  }

  /**
   * This method set start and end packet
   * 
   * @param percentileFileOffset
   * @return
   */
  private PercentileFileOffset setAndGetStartAndEndDataPacket(PercentileFileOffset percentileFileOffset, GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    try
    {
      if (debugLevel > 3)
        Log.debugLogAlways(className, "setAndGetStartAndEndDataPacket", "", "", "percentileFileOffset = " + percentileFileOffset);

      String startFileName = percentileFileOffset.getStartFileName();
      if (startFileName != null)
      {
        String partitionName = null;
        if (percentileDataDTO.isPartitionModeEnabled())
          partitionName = percentileFileOffset.getStartPartitionName();

        GraphNamesObjKey graphNamesObjKey = new GraphNamesObjKey(testRunNumber, partitionName, graphUniqueKeyDTO.getGeneratorTestNum(), graphUniqueKeyDTO.getGeneratorName());
        GraphNames graphNamesObj = graphNamesObjMap.get(graphNamesObjKey);
        if (graphNamesObj == null)
        {
          Log.errorLog(className, "setAndGetStartAndEndDataPacket", "", "", "Bug::: graphNamesObj is null.");
          return percentileFileOffset;
        }

        int startOffSet = percentileFileOffset.getStartOffset();
        double[] rawDataWholePkt = readOnePacket(graphNamesObj, startFileName, startOffSet);
        double[] arrStartPacket = getRawDataFromPktByGraph(graphNamesObj, graphUniqueKeyDTO, rawDataWholePkt);

        if (debugLevel > 3)
          Log.debugLogAlways(className, "setAndGetStartAndEndDataPacket", "", "", "arrStartPacket = " + rptUtilsBean.doubleArrayToList(arrStartPacket));

        percentileFileOffset.setStartPacket(arrStartPacket);
      }

      String endFileName = percentileFileOffset.getEndFileName();
      if (endFileName != null)
      {
        String partitionName = null;
        if (percentileDataDTO.isPartitionModeEnabled())
          partitionName = percentileFileOffset.getEndPartitionName();

        GraphNamesObjKey graphNamesObjKey = new GraphNamesObjKey(testRunNumber, partitionName, graphUniqueKeyDTO.getGeneratorTestNum(), graphUniqueKeyDTO.getGeneratorName());
        GraphNames graphNamesObj = graphNamesObjMap.get(graphNamesObjKey);
        if (graphNamesObj == null)
        {
          Log.errorLog(className, "setAndGetStartAndEndDataPacket", "", "", "Bug: graphNamesObj is null.");
          return percentileFileOffset;
        }

        int endOffSet = percentileFileOffset.getEndOffset();
        double[] rawDataWholePkt = readOnePacket(graphNamesObj, endFileName, endOffSet);
        double[] arrEndPacket = getRawDataFromPktByGraph(graphNamesObj, graphUniqueKeyDTO, rawDataWholePkt);
        percentileFileOffset.setEndPacket(arrEndPacket);

        if (debugLevel > 3)
          Log.debugLogAlways(className, "setAndGetStartAndEndDataPacket", "", "", "arrEndPacket = " + rptUtilsBean.doubleArrayToList(arrEndPacket));
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "setAndGetStartAndEndDataPacket", "", "", "Exception - ", e);
    }

    return percentileFileOffset;
  }

  private double[] getRawDataFromPktByGraph(GraphNames graphNamesObj, GraphUniqueKeyDTO graphUniqueKeyDTO, double[] rawDataWholePkt)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "getRawDataFromPktByGraph", "", "", "Method Called. graphUniqueKeyDTO = " + graphUniqueKeyDTO);

      PctMsgInfo pctMsgInfo = new PctMsgInfo();
      pctMsgInfo.setGraphNames(graphNamesObj);
      long startDataIndex = graphNamesObj.getPDFDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTO);
      setPctMsgInfo(pctMsgInfo, graphUniqueKeyDTO);
      int index = (int) ((startDataIndex - PercentileDataUtils.PROCESS_HDR_LEN) / 8);
      int numGranule = pctMsgInfo.getNumGranule();
      int endIndex = (index + numGranule);
      double[] arrEndPacket = new double[numGranule];

      if (debugLevel > 0)
        Log.debugLogAlways(className, "getRawDataFromPktByGraph", "", "", "graphUniqueKeyDTO = " + graphUniqueKeyDTO + ", start index = " + index + ", end index = " + endIndex);

      int counter = 0;
      for (int i = index; i < endIndex; i++)
      {
        arrEndPacket[counter] = rawDataWholePkt[i];
        counter++;
      }

      if (debugLevel > 2)
        Log.debugLogAlways(className, "getRawDataFromPktByGraph", "", "", "Method End. graphUniqueKeyDTO = " + graphUniqueKeyDTO + ", Raw Data = " + rptUtilsBean.doubleArrayToList(arrEndPacket));

      return arrEndPacket;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getRawDataFromPktByGraph", "", "", "Exception - ", e);
      return new double[1001];
    }
  }

  private int getPercentileModeType()
  {
    try
    {
      String partitionModeTemp = getOneValidPartition(null, null);
      return PercentileDataUtils.getPercentileModeTypeFrmTestRunPDF(debugLevel, testRunNumber, partitionModeTemp, null, null);
    }
    catch (Exception e)
    {
      Log.debugLogAlways(className, "getPercentileModeType", "", "", "in testRunNumber = " + testRunNumber + ", percentile reports are disable.");
    }

    return 0;
  }

  private String getOneValidPartition(String generatorTestRunNum, String generator_Name)
  {
    ArrayList<String> allPartitionsList = PercentileDataUtils.getAllPartitionList(debugLevel, testRunNumber, percentileDataDTO.isPartitionModeEnabled(), sessionReportData);

    String partitionModeTemp = "";
    if (percentileDataDTO.isPartitionModeEnabled())
    {
      for (int i = 0; i < allPartitionsList.size(); i++)
      {
        String partitionName = allPartitionsList.get(i);
        String filePath = PercentileDataUtils.getPartitionPdfFilePath(testRunNumber, partitionName, generatorTestRunNum, generator_Name);

        if (debugLevel > 0)
          Log.debugLogAlways(className, "getOneValidPartition", "", "", "filePath = " + filePath);

        if (PercentileDataUtils.isValidFile(debugLevel, filePath))
          return partitionName;
      }
    }

    if (partitionModeTemp == null || partitionModeTemp.trim().equals(""))
    {
      ArrayList<String> allPartitionList = PercentileDataUtils.getAllPartitionList(debugLevel, testRunNumber, percentileDataDTO.isPartitionModeEnabled(), sessionReportData);
      if (allPartitionList != null && allPartitionList.size() != 0)
      {
        int n = allPartitionList.size();
        for (int i = (n - 1); i >= 0; i++)
        {
          String partitionName = allPartitionList.get(i);
          String filePath = PercentileDataUtils.getPartitionTestRunGDFFilePath(testRunNumber, partitionName);
          if (PercentileDataUtils.isValidFile(debugLevel, filePath))
          {
            partitionModeTemp = partitionName;
            break;
          }
        }
      }
    }

    return partitionModeTemp;
  }

  private String getTimeOptionByDataKey(String dataKey, PanelDataInfo panelDataInfo)
  {
    // Getting timeOption
    String timeOption = "";
    if (dataKey.startsWith("WholeScenario"))
      timeOption = "Total Run";
    else if (panelDataInfo != null && panelDataInfo.getPhaseName() != null && panelDataInfo.getPhaseName().trim().equalsIgnoreCase("Duration"))
      timeOption = "Run Phase Only";
    else if (dataKey.startsWith("SPECIFIED") || dataKey.startsWith("Last"))
      timeOption = "Specified Time";

    return timeOption;
  }

  private double[] getCumulativeSum()
  {
    double[] arrCumSum = null;

    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "getCumulativeSum", "", "", "Method Called.");

      if (debugLevel > 2)
        Log.debugLogAlways(className, "getCumulativeSum", "", "", "percentileFileOffsetList = " + percentileFileOffsetList);

      for (int i = 0; i < percentileFileOffsetList.size(); i++)
      {
        PercentileFileOffset percentileFileOffset = percentileFileOffsetList.get(i);

        if (debugLevel > 2)
          Log.debugLogAlways(className, "getCumulativeSum", "", "", "percentileFileOffset = " + percentileFileOffset);

        double[] arrStartPacket = percentileFileOffset.getStartPacket();
        double[] arrEndPacket = percentileFileOffset.getEndPacket();

        if (debugLevel > 2)
          Log.debugLogAlways(className, "getCumulativeSum", "", "", "arrStartPacket = " + rptUtilsBean.doubleArrayToList(arrStartPacket) + ", arrEndPacket = " + rptUtilsBean.doubleArrayToList(arrEndPacket));

        double[] arrDiff = null;
        if (arrStartPacket != null && arrEndPacket != null)
        {
          arrDiff = new double[arrEndPacket.length];
          for (int j = 0; j < arrEndPacket.length; j++)
          {
            if (arrEndPacket[j] >= arrStartPacket[j])
            {
              arrDiff[j] = arrEndPacket[j] - arrStartPacket[j];
            }
            else
            {
              Log.errorLog(className, "processPCTData", "", "", "arrEndPacket must be greater then or equal arrStartPacket in PctMessage.dat.");
              arrDiff[j] = arrEndPacket[j];
            }
          }
        }
        else if (arrEndPacket != null)
        {
          arrDiff = Arrays.copyOf(arrEndPacket, arrEndPacket.length);
        }

        if (arrDiff == null)
        {
          Log.errorLog(className, "getCumulativeSum", "", "", "arrDiff must not be null.");
          continue;
        }

        if (arrCumSum == null)
        {
          arrCumSum = Arrays.copyOf(arrDiff, arrDiff.length);
        }
        else
        {
          for (int j = 0; j < arrCumSum.length; j++)
          {
            arrCumSum[j] += arrDiff[j];
          }
        }
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getCumulativeSum", "", "", "Exception - ", e);
    }

    if (arrCumSum != null && debugLevel > 2)
      Log.debugLogAlways(className, "getCumulativeSum", "", "", "arrCumSum = " + rptUtilsBean.doubleArrayToList(arrCumSum) + ",arrCumSum length = " + arrCumSum.length);

    return arrCumSum;
  }

  /**
   * This method is for read one packet from given pctMessage.dat file path
   * 
   * @param partitionName
   * @param fileName
   * @param numberOfBytesToSkip
   * @param generatorTestRunNum
   * @param generator_Name
   * @return
   */
  private double[] readOnePacket(GraphNames graphNamesObj, String fileName, int numberOfBytesToSkip)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "readOnePacket", "", "", "Method Called. numberOfBytesToSkip = " + numberOfBytesToSkip + ", fileName = " + fileName);

      PDFNames pdfNames = graphNamesObj.getPdfNames();
      int pctPacketSize = pdfNames.getSizeOfPctMsgData();
      PCTPartitionData pctPartitionData = new PCTPartitionData(testRunNumber, debugLevel);
      ByteBuffer byteBuffer = pctPartitionData.readOnePacket(fileName, pctPacketSize, numberOfBytesToSkip);
      int rawDataLen = (pctPacketSize / PercentileDataUtils.GRANULE_SIZE) - (PercentileDataUtils.PROCESS_HDR_LEN / PercentileDataUtils.GRANULE_SIZE);

      int tempIndex = 0;
      StringBuffer rawDataToLog = new StringBuffer();
      for (int j = 0; j < (pctPacketSize / PercentileDataUtils.GRANULE_SIZE); j++)
      {
        long rawData = byteBuffer.getLong(tempIndex);
        rawDataToLog.append(rawData + ",");
        tempIndex = tempIndex + 8;
      }

      if (debugLevel > 3)
        Log.debugLogAlways(className, "readOnePacket", "", "", "rawDataToLog = " + rawDataToLog);

      int index = PercentileDataUtils.PROCESS_HDR_LEN;
      double[] dataPacket = new double[rawDataLen];
      for (int j = 0; j < rawDataLen; j++)
      {
        long rawData = byteBuffer.getLong(index);
        dataPacket[j] = rawData;
        index = index + 8;
      }

      return dataPacket;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "readOnePacket", "", "", "Exception - ", e);
      return null;
    }
  }

  /**
   * This method set pctMsgInfo by using graph names object
   * 
   * @param pctMsgInfo
   * @param graphUniqueKeyDTO
   */
  private void setPctMsgInfo(PctMsgInfo pctMsgInfo, GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "setPctMsgInfo", "", "", "Method Called.");

      GraphNames graphNamesObj = pctMsgInfo.getGraphNames();
      int pdfId = graphNamesObj.getPDFIdByGraphUniqueKeyDTO(graphUniqueKeyDTO);
      pctMsgInfo.setPdfId(pdfId);

      int numGranule = graphNamesObj.getPdfNames().getNumGranuleByPDFId(pdfId);
      pctMsgInfo.setNumGranule(numGranule);

      int minGranule = graphNamesObj.getPdfNames().getMinGranuleByPDFId(pdfId);
      pctMsgInfo.setMinGranule(minGranule);

      int formulaNumber = graphNamesObj.getPdfNames().getFormulaNumberByPDFId(pdfId);
      pctMsgInfo.setFormulaNumber(formulaNumber);

      String pdfUnit = graphNamesObj.getPdfNames().getPDFUnitByPDFId(pdfId);
      pctMsgInfo.setPdfUnit(pdfUnit);

      double formulaUnitData = PercentileDataUtils.getFormulaUnitDataByFormulaNum(formulaNumber);
      pctMsgInfo.setFormulaUnitData(formulaUnitData);

      int pctPktSize = graphNamesObj.getPdfNames().getSizeOfPctMsgData();
      pctMsgInfo.setPctPktSize(pctPktSize);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "setPctMsgInfo", "", "", "Exception = ", e);
    }
  }

  /**
   * This method is used to creating Data Structure LinkedHashMap
   * 
   * Key - Data Index and Value - GraphUniqueKeyDTO
   */
  private LinkedHashMap<Integer, GraphUniqueKeyDTO> getMapByDataIndexAndGraphUniqueKey(ArrayList<GraphUniqueKeyDTO> rtgGraphUniqueKeyDTO)
  {
    try
    {
      long startProcessTime = System.currentTimeMillis();

      if (debugLevel > 1)
        Log.debugLogAlways(className, "getMapByDataIndexAndGraphUniqueKey", "", "", "Method Called. rtgGraphUniqueKeyDTO = " + rtgGraphUniqueKeyDTO);

      LinkedHashMap<Integer, GraphUniqueKeyDTO> hmGraphUniqueKeyLinkedHashMap = new LinkedHashMap<Integer, GraphUniqueKeyDTO>();

      for (int i = 0; i < rtgGraphUniqueKeyDTO.size(); i++)
      {
        GraphUniqueKeyDTO graphUniqueKeyDTO = rtgGraphUniqueKeyDTO.get(i);

        // Getting Generator Name
        String generatorName = graphUniqueKeyDTO.getGeneratorName();

        // Getting Generator test run number
        String generatorTestRunNum = graphUniqueKeyDTO.getGeneratorTestNum();
        if (generatorTestRunNum == null)
          generatorTestRunNum = "NA";

        String partitionName = getOneValidPartition(generatorTestRunNum, generatorName);

        // Generating GraphNamesObjKey as we already created GraphNames object
        GraphNamesObjKey graphNamesObjKey = new GraphNamesObjKey(testRunNumber, partitionName, generatorTestRunNum, generatorName);

        // Getting GraphNames object
        GraphNames graphNamesObj = graphNamesObjMap.get(graphNamesObjKey);

        // Check whether GraphNames object is null. It is must GraphNames object already created.
        if (graphNamesObj == null)
        {
          if (generatorNameAndTR.containsKey(generatorName))
            generatorTestRunNum = generatorNameAndTR.get(generatorName);
          else
            generatorTestRunNum = PercentileDataUtils.getGeneratorTestRunNumByGeneratorName(testRunNumber, generatorName);

          generatorNameAndTR.put(generatorName, generatorTestRunNum);

          graphNamesObj = genGraphNamesObj(generatorName, generatorTestRunNum, testRunNumber, partitionName);
          graphNamesObjMap.put(graphNamesObjKey, graphNamesObj);
        }

        int dataIndex = graphNamesObj.getGraphDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTO);
        hmGraphUniqueKeyLinkedHashMap.put(dataIndex, graphUniqueKeyDTO);
      }

      if (profileMode > 1)
      {
        long endProcessTime = System.currentTimeMillis();
        Log.debugLogAlways(className, "genPCTDataFrmPCT", "", "", "Total Time Taken by method genPCTDataFrmPCT = " + (endProcessTime - startProcessTime) / 1000);
      }

      return hmGraphUniqueKeyLinkedHashMap;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getMapByDataIndexAndGraphUniqueKey", "", "", "Exception - ", e);
      return null;
    }
  }

  public int getDebugLevel()
  {
    return debugLevel;
  }

  public void setDebugLevel(int debugLevel)
  {
    this.debugLevel = debugLevel;
  }

  /**
   * This method is for setting the file off set
   * 
   * @param modeType
   * @param timeOption
   * @param filteredAndValidPartitionList
   * @param errMsg
   */
  private void setFileOffSets(int modeType, String dataKey, String timeOption, ArrayList<PctPartitionInfo> filteredAndValidPartitionList, StringBuffer errMsg)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "setFileOffSets", "", "", "Method Called. timeOption = " + timeOption + ", dataKey = " + dataKey + ", modeType = " + modeType);

      if (debugLevel > 3)
        Log.debugLogAlways(className, "setFileOffSets", "", "", "Method Called. filteredAndValidPartitionList = " + filteredAndValidPartitionList + ", timeOption = " + timeOption + ", dataKey = " + dataKey + ", modeType = " + modeType);

      if (filteredAndValidPartitionList == null || filteredAndValidPartitionList.size() == 0)
      {
        Log.errorLog(className, "setFileOffSets", "", "", "filteredAndValidPartitionList must not be null.");
        errMsg.append("ERROR: No partition found for given time range.");
        return;
      }

      // Setting file off set for Total Run
      if (modeType == PERCENTILE_TOTAL_RUN)
      {
        if (timeOption.equals("Total Run"))
        {
          setFileOffSetsForTotalRun(dataKey, timeOption, filteredAndValidPartitionList, false, errMsg);
        }
        else
        {
          errMsg.append("ERROR: cannot generate percentile data as percentile setting in scenario is Total Run (Test Run = " + testRunNumber + ").");
          Log.errorLog(className, "setFileOffSets", "", "", "ERROR: cannot generate percentile data as percentile setting in scenario is Total Run (Test Run = " + testRunNumber + ").");
          return;
        }
      }

      // set file off set in case of Run Phase Only
      else if (modeType == PERCENTILE_RUN_PHASE)
      {
        if (timeOption.equals("Total Run"))
        {
          setFileOffSetsForTotalRun(dataKey, timeOption, filteredAndValidPartitionList, true, errMsg);
        }
        else if (timeOption.equals("Run Phase Only"))
        {
          setFileOffSetsForRunPhase(dataKey, timeOption, filteredAndValidPartitionList, errMsg);
        }
        else
        {
          errMsg.append("ERROR: cannot generate percentile data as percentile setting in scenario is Run Phase Only (Test Run = " + testRunNumber + ").");
          Log.errorLog(className, "setFileOffSets", "", "", "ERROR: cannot generate percentile data as percentile setting in scenario is Run Phase Only (Test Run = " + testRunNumber + ").");
          return;
        }
      }

      // set file off set in case of Specified Interval
      else if (modeType == PERCENTILE_SPECIFIED_TIME)
        setFileOffSetsForSpecifiedTime(dataKey, timeOption, filteredAndValidPartitionList, errMsg);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "setFileOffSets", "", "", "Exception - ", e);
    }
  }

  /**
   * This method set start and end off set for Run phase
   * 
   * @param dataKey
   * @param timeOption
   * @param filteredAndValidPartitionList
   * @param errMsg
   */
  private void setFileOffSetsForRunPhase(String dataKey, String timeOption, ArrayList<PctPartitionInfo> filteredAndValidPartitionList, StringBuffer errMsg)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "setFileOffSetsForRunPhase", "", "", "Method Called. dataKey = " + dataKey + ", timeOption = " + timeOption + ", filteredAndValidPartitionList = " + filteredAndValidPartitionList);

      PercentileFileOffset percentileFileOffset = new PercentileFileOffset();

      for (int i = 0; i < filteredAndValidPartitionList.size(); i++)
      {
        PctPartitionInfo pctPartitionInfo = filteredAndValidPartitionList.get(i);
        GraphNames graphNamesObj = null;
        String partitionName = pctPartitionInfo.getPartitionName();
        String fileName = pctPartitionInfo.getPctMessageFilePath();

        if (debugLevel > 0)
          Log.debugLogAlways(className, "setFileOffSetsForRunPhase", "", "", "fileName = " + fileName);

        GraphNamesObjKey graphNamesObjKey = new GraphNamesObjKey(testRunNumber, partitionName, "NA", null);
        graphNamesObj = graphNamesObjMap.get(graphNamesObjKey);
        if (graphNamesObj == null)
        {
          graphNamesObj = genGraphNamesObj(pctPartitionInfo.getGeneratorName(), pctPartitionInfo.getGeneratorTestRunNumber(), testRunNumber, partitionName);
          graphNamesObjMap.put(graphNamesObjKey, graphNamesObj);
        }

        if (debugLevel > 0)
          Log.debugLogAlways(className, "setFileOffSetsForRunPhase", "", "", "graphNamesObj = " + graphNamesObj);

        // Percentile Keyword - ON / OFF
        if (graphNamesObj.getPdfNames() == null)
        {
          if (debugLevel > 2)
            Log.debugLogAlways(className, "setFileOffSetsForRunPhase", "", "", "testrun.pdf is not generated as PERCENTILE_KEYWORD is OFF");

          continue;
        }

        int pktSize = graphNamesObj.getPdfNames().getSizeOfPctMsgData();
        File file = new File(fileName);
        long fileSize = file.length();

        int totalPackets = (int) (fileSize / pktSize);

        if (i == 0)
        {
          percentileFileOffset.setStartFileName(fileName);
          percentileFileOffset.setStartPartitionName(partitionName);

          if (totalPackets > 1)
          {
            if (debugLevel > 0)
              Log.debugLogAlways(className, "setFileOffSetsForRunPhase", "", "", "As totalPackets is greater then 1 so setting end file and end off set into the same file.");

            percentileFileOffset.setEndPartitionName(partitionName);
            percentileFileOffset.setEndFileName(fileName);
            percentileFileOffset.setEndOffset(pktSize);
          }
        }
        else
        {
          percentileFileOffset.setEndPartitionName(partitionName);
          percentileFileOffset.setEndFileName(fileName);
          break;
        }
      }

      if (debugLevel > 1)
        Log.debugLogAlways(className, "setFileOffSetsForRunPhase", "", "", "adding percentileFileOffset = " + percentileFileOffset);

      percentileFileOffsetList.add(percentileFileOffset);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "setFileOffSetsForRunPhase", "", "", "Exception - ", e);
    }
  }

  /**
   * 
   * @param modeType
   * @param dataKey
   * @param timeOption
   * @param filteredAndValidPartitionList
   * @param errMsg
   */
  private void setFileOffSetsForSpecifiedTime(String dataKey, String timeOption, ArrayList<PctPartitionInfo> filteredAndValidPartitionList, StringBuffer errMsg)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "setFileOffSetsForSpecifiedTime", "", "", "Method Called. dataKey = " + dataKey + ", timeOption = " + timeOption);

      // Creating instance of PercentileFileOffset
      PercentileFileOffset percentileFileOffset = new PercentileFileOffset();
      PanelDataInfo panelDataInfo = getPanelDataInfoByDataKey(dataKey);

      if (panelDataInfo.isBaseline())
        panelDataInfo = getChangePanelDataInfo(panelDataInfo);

      if (debugLevel > 3)
        Log.debugLogAlways(className, "setFileOffSetsForSpecifiedTime", "", "", "panelDataInfo = " + panelDataInfo);

      PctPartitionInfo pctPartitionInfo = filteredAndValidPartitionList.get(0);

      if (debugLevel > 0)
        Log.debugLogAlways(className, "setFileOffSetsForSpecifiedTime", "", "", "pctPartitionInfo = " + pctPartitionInfo);

      // Setting start file name and start partition
      percentileFileOffset.setStartFileName(pctPartitionInfo.getPctMessageFilePath());
      percentileFileOffset.setStartPartitionName(pctPartitionInfo.getPartitionName());

      String startPartitionName = pctPartitionInfo.getPartitionName();
      String startFileName = pctPartitionInfo.getPctMessageFilePath();

      String startTime = panelDataInfo.getStartTime();
      String endTime = panelDataInfo.getEndTime();

      if (!startTime.equals("NA"))
      {
        File partitionFile = new File(pctPartitionInfo.getPctMessageFilePath());
        long fileSize = partitionFile.length();

        GraphNamesObjKey graphNamesObjKey = new GraphNamesObjKey(testRunNumber, pctPartitionInfo.getPartitionName(), pctPartitionInfo.getGeneratorTestRunNumber(), pctPartitionInfo.getGeneratorName());
        GraphNames graphNamesObj = graphNamesObjMap.get(graphNamesObjKey);
        if (graphNamesObj == null)
        {
          Log.errorLog(className, "setFileOffSetsForSpecifiedTime", "", "", "Bug: graphNamesObj is null.");
          return;
        }

        if (graphNamesObj.getPdfNames() == null)
        {
          Log.errorLog(className, "setFileOffSetsForSpecifiedTime", "", "", "Bug: PdfNames is null.");
          return;
        }

        int pktSize = graphNamesObj.getPdfNames().getSizeOfPctMsgData();

        if (fileSize < pktSize)
        {
          Log.debugLogAlways(className, "setFileOffSetsForSpecifiedTime", "", "", "pctMessage.dat file is corrupted. Size < one packet size");
          Log.errorLog(className, "setFileOffSetsForSpecifiedTime", "", "", "pctMessage.dat file is corrupted. Size < one packet size");
          return;
        }

        int totalPkts = (int) (fileSize / pktSize);

        if (debugLevel > 1)
          Log.debugLogAlways(className, "setFileOffSetsForSpecifiedTime", "", "", "totalPkts = " + totalPkts + ", pktSize = " + pktSize + ", fileSize = " + fileSize + ", startFileName = " + pctPartitionInfo.getPctMessageFilePath());

        ByteBuffer byteBuffer = readHeaderPartOfFirstPkt(pctPartitionInfo.getPctMessageFilePath(), errMsg);
        if ((errMsg != null && errMsg.length() != 0) || (byteBuffer == null))
        {
          percentileDataDTO.setErrMsg(errMsg);
          Log.errorLog(className, "setFileOffSetsForSpecifiedTime", "", "", "errMsg = " + errMsg);
          return;
        }

        long firstPktTimeStamp = byteBuffer.getLong(PercentileDataUtils.TIME_STAMP_INDEX);

        if (debugLevel > 3)
          Log.debugLogAlways(className, "setFileOffSetsForSpecifiedTime", "", "", "firstPktTimeStamp = " + firstPktTimeStamp);

        if (firstPktTimeStamp == 0)
        {
          if (debugLevel > 0)
            Log.debugLogAlways(className, "setFileOffSetsForSpecifiedTime", "", "", "test run is non partition and very old. Need to handle based on seq number.");
        }
        else
        {
          long startTimeTemp = Long.parseLong(startTime);
          long startTimeFirstPktDiff = 0;
          if (startTimeTemp > firstPktTimeStamp)
            startTimeFirstPktDiff = startTimeTemp - firstPktTimeStamp;
          else
            startTimeFirstPktDiff = 0;

          long pctSampleInterval = graphNamesObj.getPdfNames().getInterval();

          if (debugLevel > 0)
            Log.debugLogAlways(className, "setFileOffSetsForSpecifiedTime", "", "", "pctSampleInterval = " + pctSampleInterval + ", startTimeFirstPktDiff = " + startTimeFirstPktDiff);

          if (startTimeFirstPktDiff > 0)
          {
            int firstPktNumToRead = (int) (startTimeFirstPktDiff / pctSampleInterval);
            if (startTimeFirstPktDiff % pctSampleInterval != 0)
              firstPktNumToRead++;

            if (firstPktNumToRead > 0)
            {
              if (debugLevel > 0)
                Log.debugLogAlways(className, "setFileOffSetsForSpecifiedTime", "", "", ", firstPktNumToRead = " + firstPktNumToRead);

              if (firstPktNumToRead > totalPkts && totalPkts != 0)
              {
                Log.errorLog(className, "setFileOffSetsForSpecifiedTime", "", "", "First packet to be read should not be greater than total number of packets");
                firstPktNumToRead = (totalPkts - 1);
              }

              percentileFileOffset.setStartOffset(firstPktNumToRead * pktSize);
            }
          }
        }
      }

      // Used to set for end file off set
      String endFileName = pctPartitionInfo.getPctMessageFilePath();
      String endPartition = pctPartitionInfo.getPartitionName();

      // Used when test is restarted
      String prevPartiton = pctPartitionInfo.getPartitionName();
      String prevFileName = pctPartitionInfo.getPctMessageFilePath();

      int i = 1;
      boolean firstTimeRestarted = true;
      int totalFilteredPartitionList = filteredAndValidPartitionList.size();
      while ((totalFilteredPartitionList - 1) > 1)
      {
        // This is the safety check
        if (i >= filteredAndValidPartitionList.size())
          break;

        PctPartitionInfo pctPartitionInfo2 = filteredAndValidPartitionList.get(i);
        if (debugLevel > 0)
          Log.debugLogAlways(className, "setFileOffSetsForSpecifiedTime", "", "", "pctPartitionInfo2 = " + pctPartitionInfo2);

        String currentPartition = pctPartitionInfo2.getPartitionName();
        String currentFileName = pctPartitionInfo2.getPctMessageFilePath();

        ByteBuffer byteBuffer = readHeaderPartOfFirstPkt(currentFileName, errMsg);
        long seqNumber = byteBuffer.getLong(PercentileDataUtils.SEQ_NUM_INDEX);

        if (debugLevel > 2)
          Log.debugLogAlways(className, "setFileOffSetsForSpecifiedTime", "", "", "seqNumber = " + seqNumber + ", currentFileName = " + currentFileName);

        if (seqNumber == 1)
        {
          if (debugLevel > 0)
            Log.debugLogAlways(className, "setFileOffSetsForSpecifiedTime", "", "", "Test is restarted and currentFileName = " + currentFileName + ", prevPartiton = " + prevPartiton + ", prevFileName = " + prevFileName);

          // calculate total packets
          File file = new File(endFileName);
          long fileSize = file.length();

          GraphNamesObjKey graphNamesObjKey = new GraphNamesObjKey(testRunNumber, endPartition, pctPartitionInfo2.getGeneratorTestRunNumber(), pctPartitionInfo2.getGeneratorName());
          GraphNames graphNamesObj = graphNamesObjMap.get(graphNamesObjKey);
          if (graphNamesObj == null)
          {
            Log.errorLog(className, "setFileOffSetsForSpecifiedTime", "", "", "Bug:: graphNamesObj is null.");
            return;
          }

          // Need to use old graph names object
          int pktSize = graphNamesObj.getPdfNames().getSizeOfPctMsgData();

          long totalPkts = fileSize / pktSize;
          int endFileOffSet = (int) ((totalPkts - 1) * pktSize);

          // As we already set start file name so need to set start file name when test is restarted.
          if (!firstTimeRestarted)
          {
            percentileFileOffset.setStartFileName(startFileName);
            percentileFileOffset.setStartPartitionName(startPartitionName);
          }

          firstTimeRestarted = false;

          // set end file and partition
          percentileFileOffset.setEndOffset(endFileOffSet);
          percentileFileOffset.setEndFileName(endFileName);
          percentileFileOffset.setEndPartitionName(endPartition);

          if (debugLevel > 1)
            Log.debugLogAlways(className, "setFileOffSetsForSpecifiedTime", "", "", "totalPkts = " + totalPkts + ", pktSize = " + pktSize + ", fileSize = " + fileSize + ", endFileName = " + endFileName);

          if (debugLevel > 1)
            Log.debugLogAlways(className, "setFileOffSetsForSpecifiedTime", "", "", "adding percentileFileOffset = " + percentileFileOffset);

          percentileFileOffsetList.add(percentileFileOffset);

          // Createing new instance of PercentileFileOffset
          percentileFileOffset = new PercentileFileOffset();

          startFileName = currentFileName;
          startPartitionName = currentPartition;
        }

        // Setting prev partition and prev file name
        prevPartiton = currentPartition;
        prevFileName = currentFileName;

        // setting end partition and end file name
        endPartition = currentPartition;
        endFileName = currentFileName;
        i++;
      }

      int lastIndex = filteredAndValidPartitionList.size() - 1;
      PctPartitionInfo pctPartitionInfoLast = filteredAndValidPartitionList.get(lastIndex);
      endPartition = pctPartitionInfoLast.getPartitionName();
      endFileName = pctPartitionInfoLast.getPctMessageFilePath();

      GraphNamesObjKey graphNamesObjKey = new GraphNamesObjKey(testRunNumber, endPartition, "NA", null);
      GraphNames graphNamesObj = graphNamesObjMap.get(graphNamesObjKey);
      if (graphNamesObj == null)
      {
        graphNamesObj = genGraphNamesObj(pctPartitionInfoLast.getGeneratorName(), pctPartitionInfoLast.getGeneratorTestRunNumber(), testRunNumber, endPartition);
        graphNamesObjMap.put(graphNamesObjKey, graphNamesObj);
      }

      long pktSize = graphNamesObj.getPdfNames().getSizeOfPctMsgData();
      File fileObj = new File(endFileName);
      long fileSize = fileObj.length();
      int totalPackets = (int) (fileSize / pktSize);
      long pctInterval = graphNamesObj.getPdfNames().getInterval();

      if (debugLevel > 0)
        Log.debugLogAlways(className, "setFileOffSetsForSpecifiedTime", "", "", "pctInterval = " + pctInterval + ", endTime = " + endTime + ", totalPackets = " + totalPackets + ", endFileName = " + endFileName);

      if (endTime.equals("NA"))
      {
        int endOffset = (int) ((totalPackets - 1) * pktSize);

        percentileFileOffset.setEndOffset(endOffset);
        percentileFileOffset.setEndFileName(endFileName);
        percentileFileOffset.setEndPartitionName(endPartition);

        if (debugLevel > 1)
          Log.debugLogAlways(className, "setFileOffSetsForSpecifiedTime", "", "", "totalPackets = " + totalPackets + ", pktSize = " + pktSize + ", fileSize = " + fileSize + ", endFileName = " + endFileName + ", endTime = " + endTime);
      }
      else
      {
        ByteBuffer byteBuffer = readHeaderPartOfFirstPkt(endFileName, errMsg);
        long firstPktTimeStamp = byteBuffer.getLong(PercentileDataUtils.TIME_STAMP_INDEX);

        if (debugLevel > 3)
          Log.debugLogAlways(className, "setFileOffSetsForSpecifiedTime", "", "", "Setting endFileOffset and found firstPktTimeStamp = " + firstPktTimeStamp);

        long endPktTimeStamp = firstPktTimeStamp + ((totalPackets - 1) * pctInterval);
        long endUserTime = Long.parseLong(endTime);
        if (endUserTime > endPktTimeStamp)
        {
          Log.debugLogAlways(className, "setFileOffSetsForSpecifiedTime", "", "", "endUserTime is greater then endPktTimeStamp. So assuming endPktTimeStamp = endUserTime. It means need to read last packet");
          endPktTimeStamp = endUserTime;
        }

        long endPktToRead = endUserTime / pctInterval;
        if (endUserTime % pctInterval != 0)
          endPktToRead++;

        if (endPktToRead > totalPackets)
          endPktToRead = totalPackets - 1;

        int endOffset = (int) (endPktToRead * pktSize);

        if (debugLevel > 1)
          Log.debugLogAlways(className, "setFileOffSetsForSpecifiedTime", "", "", "endPktToRead = " + (endPktToRead + 1) + ", endOffset = " + endOffset);

        percentileFileOffset.setEndOffset(endOffset);
        percentileFileOffset.setEndFileName(endFileName);
        percentileFileOffset.setEndPartitionName(endPartition);
      }

      if (filteredAndValidPartitionList.size() == 1 && (startTime == null || startTime.equals("NA")))
      {
        percentileFileOffset.setStartFileName(null);
        percentileFileOffset.setStartPartitionName(null);
      }
      else
      {
        percentileFileOffset.setStartFileName(startFileName);
        percentileFileOffset.setStartPartitionName(startPartitionName);
      }

      if (debugLevel > 1)
        Log.debugLogAlways(className, "setFileOffSetsForSpecifiedTime", "", "", "adding percentileFileOffset = " + percentileFileOffset);

      percentileFileOffsetList.add(percentileFileOffset);

      // if start off set is zero and only 1 partition or non partition, then no need to read first packet
      if (percentileFileOffsetList != null && percentileFileOffsetList.size() == 1)
      {
        PercentileFileOffset percentileFileOffset2 = percentileFileOffsetList.get(0);
        if (percentileFileOffset2.getStartFileName() != null && percentileFileOffset2.getEndFileName() != null && percentileFileOffset2.getStartFileName().equals(percentileFileOffset2.getEndFileName()) && percentileFileOffset2.getStartOffset() == 0)
          percentileFileOffset2.setStartFileName(null);
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "setFileOffSetsForSpecifiedTime", "", "", "Exception - ", e);
    }
  }

  /**
   * This method set file off set for tota run case
   * 
   * @param dataKey
   * @param timeOption
   * @param filteredAndValidPartitionList
   * @param errMsg
   */
  private void setFileOffSetsForTotalRun(String dataKey, String timeOption, ArrayList<PctPartitionInfo> filteredAndValidPartitionList, boolean isRunPhaseTR, StringBuffer errMsg)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "setFileOffSetsForTotalRun", "", "", "Method Called dataKey = " + dataKey + ", timeOption = " + timeOption + ", isRunPhaseTR = " + isRunPhaseTR);

      int lastIndex = (filteredAndValidPartitionList.size() - 1);
      PctPartitionInfo pctPartitionInfo = filteredAndValidPartitionList.get(lastIndex);
      PercentileFileOffset percentileFileOffset = new PercentileFileOffset();
      percentileFileOffset.setEndPartitionName(pctPartitionInfo.getPartitionName());
      percentileFileOffset.setEndFileName(pctPartitionInfo.getPctMessageFilePath());

      if (isRunPhaseTR)
      {
        GraphNamesObjKey graphNamesObjKey = new GraphNamesObjKey(testRunNumber, pctPartitionInfo.getPartitionName(), pctPartitionInfo.getGeneratorTestRunNumber(), pctPartitionInfo.getGeneratorName());
        GraphNames graphNamesObj = graphNamesObjMap.get(graphNamesObjKey);
        if (graphNamesObj == null)
        {
          graphNamesObj = genGraphNamesObj(pctPartitionInfo.getGeneratorName(), pctPartitionInfo.getGeneratorTestRunNumber(), testRunNumber, pctPartitionInfo.getPartitionName());
          graphNamesObjMap.put(graphNamesObjKey, graphNamesObj);
        }

        long pktSize = 0;
        if (graphNamesObj.getPdfNames() != null)
          pktSize = graphNamesObj.getPdfNames().getSizeOfPctMsgData();

        File fileObj = new File(pctPartitionInfo.getPctMessageFilePath());
        long fileSize = fileObj.length();
        int totalPackets = (int) (fileSize / pktSize);
        int endOffset = (int) ((totalPackets - 1) * pktSize);

        percentileFileOffset.setEndOffset(endOffset);
      }

      if (debugLevel > 1)
        Log.debugLogAlways(className, "setFileOffSetsForSpecifiedTime", "", "", "adding percentileFileOffset = " + percentileFileOffset);

      percentileFileOffsetList.add(percentileFileOffset);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "setFileOffSetsForTotalRun", "", "", "Exception - ", e);
    }
  }

  private ByteBuffer readHeaderPartOfFirstPkt(String fileName, StringBuffer errMsg)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "readHeaderPartOfFirstPkt", "", "", "Method Called. fileName = " + fileName);

      BinaryFileReader fileReader = new BinaryFileReader();
      fileReader.openFileStream(fileName);
      byte[] byteBuf = new byte[PercentileDataUtils.PROCESS_HDR_LEN];
      int bytesRead = fileReader.readPackets(byteBuf);
      if (bytesRead != byteBuf.length)
      {
        Log.errorLog(className, "readHeaderPartOfFirstPkt", "", "", "packet is partial in " + fileName);
        errMsg.append("packet is partial in " + fileName);
        return null;
      }

      ByteBuffer byteBuffer = ByteBuffer.wrap(byteBuf);
      byteBuffer = byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
      return byteBuffer;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "readHeaderPartOfFirstPkt", "", "", "Exception - ", e);
      return null;
    }
  }

  /**
   * This method is used for getting PanelDataInfo based on datakey
   * 
   * @param dataKey
   * @return
   */
  public PanelDataInfo getPanelDataInfoByDataKey(String dataKey)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "getPanelDataInfoByDataKey", "", "", "Method Called. dataKey = " + dataKey);

      for (int i = 0; i < panelDataInfoList.size(); i++)
      {
        PanelDataInfo panelDataInfo = panelDataInfoList.get(i);
        if (panelDataInfo.getDataKey().equals(dataKey))
          return panelDataInfo;
      }

      return null;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getPanelDataInfoByDataKey", "", "", "Exception - ", e);
      return null;
    }
  }

  private void setPartitionMode()
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "setPartitionMode", "", "", "Method called.");

      for (int i = 0; i < arrPercentileDataDTO.length; i++)
      {
        int testRunNum = arrPercentileDataDTO[i].getTestRunNumber();
        int partition = TestRunDataType.getTestRunPartitionType(testRunNum);
        if (partition > 0)
        {
          if (debugLevel > 0)
            Log.debugLogAlways(className, "setPartitionMode", "", "", "partition mode is true for testRunNum = " + testRunNum);

          arrPercentileDataDTO[i].setPartitionModeEnabled(true);
        }
        else
        {
          if (debugLevel > 0)
            Log.debugLogAlways(className, "setPartitionMode", "", "", "partition mode is false for testRunNum = " + testRunNum);

          arrPercentileDataDTO[i].setPartitionModeEnabled(false);
        }
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "setPartitionMode", "", "", "Exception - ", e);
    }
  }

  /**
   * This is the common method to generate GraphNames object
   * 
   * @param generatorName
   * @param generatorTestRun
   * @param controllerTestRun
   * @param partitionName
   * @return
   */
  private GraphNames genGraphNamesObj(String generatorName, String generatorTestRun, int controllerTestRun, String partitionName)
  {
    GraphNames graphNamesObj = null;

    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "genGraphNamesObj", "", "", "generatorName = " + generatorName + ", generatorTestRun = " + generatorTestRun + ", controllerTestRun = " + controllerTestRun + ", partitionName = " + partitionName);

      if (generatorName == null || generatorTestRun == null || generatorTestRun.equals("NA"))
        graphNamesObj = new GraphNames(testRunNumber, null, null, "NA", null, partitionName, "", false);
      else
        graphNamesObj = new GraphNames(Integer.parseInt(generatorTestRun), null, null, "" + testRunNumber, generatorName, partitionName, "", false);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "genGraphNamesObj", "", "", "Exception - ", e);
    }

    return graphNamesObj;
  }

  class GraphNamesObjKey
  {
    private int testRunNumber = -1;
    private String generatorTestRunNum = "NA";
    private String generator_Name = null;
    private String partitionName = "";

    public String getPartitionName()
    {
      return partitionName;
    }

    public void setPartitionName(String partitionName)
    {
      this.partitionName = partitionName;
    }

    public GraphNamesObjKey(int testRunNumber, String partitionName, String generatorTestRunNum, String generator_Name)
    {
      this.testRunNumber = testRunNumber;
      this.generatorTestRunNum = generatorTestRunNum;
      this.generator_Name = generator_Name;
      this.partitionName = partitionName;
    }

    public int getTestRunNumber()
    {
      return testRunNumber;
    }

    public void setTestRunNumber(int testRunNumber)
    {
      this.testRunNumber = testRunNumber;
    }

    public String getGeneratorTestRunNum()
    {
      return generatorTestRunNum;
    }

    public void setGeneratorTestRunNum(String generatorTestRunNum)
    {
      this.generatorTestRunNum = generatorTestRunNum;
    }

    public String getGenerator_Name()
    {
      return generator_Name;
    }

    public void setGenerator_Name(String generator_Name)
    {
      this.generator_Name = generator_Name;
    }

    /**
     * Check Equality of GraphNamesObjKey
     */
    @Override
    public boolean equals(Object obj)
    {
      try
      {
        if (obj == this)
          return true;

        GraphNamesObjKey graphNamesObjKey = (GraphNamesObjKey) obj;

        if (graphNamesObjKey == null)
          return false;

        // Condition for generator name
        boolean isSameGenerator = false;
        String oldGeneratorName = graphNamesObjKey.getGenerator_Name();
        if (oldGeneratorName == null && this.generator_Name == null)
          isSameGenerator = true;
        else if (oldGeneratorName != null && oldGeneratorName.equals("NA") && this.generator_Name == null)
          isSameGenerator = true;
        else if (this.generator_Name != null && this.generator_Name.equals("NA") && oldGeneratorName == null)
          isSameGenerator = true;
        else
          isSameGenerator = oldGeneratorName.equals(this.generator_Name);

        // Condition for Controller name
        boolean isSameController = false;
        String generatorTestRunNumOld = graphNamesObjKey.getGeneratorTestRunNum();
        if (generatorTestRunNumOld == null && this.generatorTestRunNum == null)
          isSameController = true;
        else if (generatorTestRunNumOld != null && generatorTestRunNumOld.equals("NA") && this.generatorTestRunNum == null)
          isSameController = true;
        else if (this.generatorTestRunNum != null && this.generatorTestRunNum.equals("NA") && generatorTestRunNumOld == null)
          isSameController = true;
        else
          isSameController = generatorTestRunNumOld.equals(this.generatorTestRunNum);

        boolean isSameTestRun = (graphNamesObjKey.getTestRunNumber() == this.testRunNumber);

        boolean isSamePartition = false;
        if (graphNamesObjKey.getPartitionName() == null && this.partitionName == null)
          isSamePartition = true;
        else if (graphNamesObjKey.getPartitionName() != null && (graphNamesObjKey.getPartitionName().equals("NA") || graphNamesObjKey.getPartitionName().equals("")) && this.partitionName == null)
          isSamePartition = true;
        else if (this.partitionName != null && (this.partitionName.equals("NA") || this.partitionName.equals("")) && graphNamesObjKey.getPartitionName() == null)
          isSamePartition = true;
        else
          isSamePartition = graphNamesObjKey.getPartitionName().equals(this.partitionName);

        if (debugLevel > 3)
          Log.debugLogAlways(className, "equals", "", "", "isSameGenerator = " + isSameGenerator + ", isSameController = " + isSameController + ", isSameTestRun = " + isSameTestRun + ", isSamePartition = " + isSamePartition);

        if (isSameGenerator && isSameController && isSameTestRun && isSamePartition)
          return true;
        else
          return false;
      }
      catch (Exception e)
      {
        Log.stackTraceLog(className, "equals", "", "", "Exception - ", e);
        return false;
      }
    }

    public int hashCode()
    {
      return 7;
    }

    public String toString()
    {
      return "testRunNumber = " + testRunNumber + ", generatorTestRunNum = " + generatorTestRunNum + ", generator_Name = " + generator_Name + ", partitionName = " + partitionName;
    }
  }
}

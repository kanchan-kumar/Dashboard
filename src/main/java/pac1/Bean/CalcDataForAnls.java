/*--------------------------------------------------------------------
@Name    : CalcDataForAnls.java
@Author  : Jaspreet
@Purpose : Calculates the data for drawing graphs of all type( in case of Analysis GUI).
           It Also returns template information in case of stored template
           information of this class is used through reportDataChangedGranularity class object
@Modification History:
    29/09/2010 -> Initial Version
----------------------------------------------------------------------*/
package pac1.Bean;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Vector;

import pac1.Bean.GraphName.GraphNameUtils;
import pac1.Bean.GraphName.GraphNames;

public class CalcDataForAnls implements java.io.Serializable
{
  private static String className = "CalcDataForAnls";
  private int testRunNumber = -1;
  private ArrayList arrListRawDataObject = new ArrayList(); // this gives data
  // objects depending
  // on number of ADD
  // requests made
  // from Anls GUI or
  // number of
  // Template lines

  // below Arralist are to save rqd fields, set via parsing the template lines
  private transient ArrayList arrListGranularity;
  private transient ArrayList arrListGraphDataIndex;
  private transient ArrayList arrListGraphNames;
  private transient ArrayList arrListTime;
  private transient ArrayList arrListXaxisFormat;
  private transient ArrayList arrListStartTimeElapsed;
  private transient ArrayList arrListEndTimeElapsed;
  private transient ArrayList arrListGraphViewType;
  private transient ArrayList arrListDerivedGraphExpression;
  private transient ArrayList arrListGraphReportDesc;
  private transient ArrayList arrListGraphCountFromTemplateLine;

  private transient ReportData rptData = null; // provide data to extract
  // depending upon testRunNumber
  public GraphNames graphNames = null; // made public , may be used on client
  // side in future

  private transient String[][] arrRtplRecs = null;
  private transient String rptName = ""; // Report name while adding graph via
  // Anls through "saved Template"
  private transient String randomRptName = "";// temporary rpt created, while
  // adding graph through anls

  public ReportDataChangedGranularity reportDataChangedGranularity;

  private transient String elapsedStartTime = "NA"; // Elasped Start Time.
  // Calculated if absolute
  // date/time used
  private transient String elapsedEndTime = "NA"; // Elasped End Time.
  // Calculated if absolute
  // date/time used
  private transient String trDurationForTemplate = "";// required to
  // calculateGranularity
  // for
  // SaveHtmlFromTemplate

  // Below field are set globally for "Percentile , Slab Count , Freq Dist" via
  // template parsing to generate data
  private transient int[] arrPDFID = null;
  private transient int[] arrPCTGraphDataIdx = null;
  private transient int[] arrGraphDataIdx = null;
  private transient int[] arrRptGraphDataIdx = null;
  private transient String[] arrPCTTemplateDetail = null;

  // Below field are set for Percentile Graph Data in
  // reportDataChangedGranularity obj
  private transient Vector vecStrLegend = new Vector();// ******* make abv 2
  // local & these 2 global
  private transient Vector vecPercentileGraphData = new Vector();

  // Below field are set for Slab Count Graph Data in
  // reportDataChangedGranularity obj
  private transient Vector vecSlabData = new Vector();
  private transient Vector vecSlabInfo = new Vector();

  // Below field are set for frequency Distribution Graph Data in
  // reportDataChangedGranularity obj
  private transient Vector vecFreqDisData = new Vector();
  private transient Vector vecFreqDisDataXAxis = new Vector();
  private transient Vector vecFreqDisLegendMsg = new Vector();

  // Below field are set globally for "Correlated Graph" via template parsing to
  // generate data
  private transient String arrRptNameAndDataIdx[][] = null;
  private transient String graphType = "";
  private transient double arrAvgDataValuesAll[][] = null;
  private transient double arrStdDevDataValuesAll[][] = null;

  // Below field are set for Correlated Graph Data in
  // reportDataChangedGranularity obj
  private transient double[] arrRptData1 = null;
  private transient double[] arrRptData2 = null;
  private transient String[] legendMsgForCorrelated = null;

  // simple multi tile
  private transient Vector vecDataValue = new Vector();
  private transient Vector vecSeqNum = new Vector();
  private transient Vector vecInterval = new Vector();

  private transient long testRunStartTime;
  private transient Vector vecTemplateLineDetail = new Vector();

  Boolean isBarChart = false;

  public CalcDataForAnls()
  {

  }

  public CalcDataForAnls(int testRun)
  {
    this.testRunNumber = testRun;
  }

  /**
   * Return raw data
   * 
   * @param vecReqObj
   * @return
   */
  public Object getAnalysisRawDataObj(Vector vecReqObj)
  {
    try
    {
      Log.debugLog(className, "getAnalysisRawDataObj", "", "", "Method Called. vecReqObj = " + vecReqObj);

      setTestRunNumber(Integer.parseInt(vecReqObj.get(1).toString()));
      rptName = vecReqObj.get(3).toString();
      randomRptName = vecReqObj.get(6).toString();
      trDurationForTemplate = vecReqObj.get(4).toString();
      Boolean isfromTemplate = Boolean.valueOf(vecReqObj.get(8).toString());
      isBarChart = Boolean.valueOf(vecReqObj.get(9).toString());
      String testrunStartDateTime = vecReqObj.get(2).toString();

      try
      {
        testRunStartTime = rptUtilsBean.convertDateToMilliSec(testrunStartDateTime);
      }
      catch (Exception ex)
      {
        testRunStartTime = Long.parseLong(testrunStartDateTime);
      }
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getAnalysisRawDataObj", "", "", "Exception - ", ex);
    }

    arrListGranularity = new ArrayList();
    arrListGraphDataIndex = new ArrayList();
    arrListGraphNames = new ArrayList();
    arrListTime = new ArrayList();
    arrListXaxisFormat = new ArrayList();
    arrListStartTimeElapsed = new ArrayList();
    arrListEndTimeElapsed = new ArrayList();
    arrListGraphViewType = new ArrayList();
    arrListDerivedGraphExpression = new ArrayList();
    arrListGraphReportDesc = new ArrayList();
    arrListGraphCountFromTemplateLine = new ArrayList();

    Log.debugLog(className, "getAnalysisRawDataObj", "", "", "rptName = " + rptName);

    if ((rptName).equals(""))
      getDataFromRTPL(randomRptName);
    else
      getDataFromRTPL(rptName);

    for (int i = 0; i < (arrRtplRecs.length - 3); i++)
    {
      try
      {
        int[] arrDataIndex = null;
        if (arrListGraphDataIndex.size() > i)
          arrDataIndex = (int[]) (arrListGraphDataIndex.get(i));
        else
          return arrListRawDataObject;

        int granualarity = -1;
        if (arrListGranularity.size() > i)
          granualarity = Integer.parseInt(arrListGranularity.get(i).toString());

        String timeFormat = "";
        if (arrListTime.size() > i)
          timeFormat = arrListTime.get(i).toString();

        String startElapseTime = "";
        if (arrListStartTimeElapsed.size() > i)
          arrListStartTimeElapsed.get(i).toString();

        String endElapsedTime = "";
        if (arrListEndTimeElapsed.size() > i)
          endElapsedTime = arrListEndTimeElapsed.get(i).toString();

        String graphViewType = "";
        if (arrListGraphViewType.size() > i)
          graphViewType = arrListGraphViewType.get(i).toString();

        String derivedExp = "";
        if (arrListDerivedGraphExpression.size() > i)
          derivedExp = arrListDerivedGraphExpression.get(i).toString();

        if (graphViewType.equals("Percentile Graph"))
        {
          String[] arr = arrRtplRecs[i];
          String tmp = "";

          for (int j = 0; j < arr.length; j++)
          {
            tmp += arr[i];
          }

          Log.debugLog(className, "getAnalysisRawDataObj", "", "", "tmp = " + tmp);
        }

        Log.debugLog(className, "getAnalysisRawDataObj", "", "", "granualarity = " + granualarity + ", arrDataIndex = " + arrDataIndex + ", timeFormat = " + timeFormat + " , startElapseTime = " + startElapseTime + ", endElapsedTime = " + endElapsedTime + ", graphViewType = " + graphViewType);

        Object obj = getFinalAnalysisRawDataObj(granualarity, arrDataIndex, timeFormat, startElapseTime, endElapsedTime, graphViewType, derivedExp, isBarChart);
        if (obj != null)
          arrListRawDataObject.add(obj);
      }
      catch (Exception ex)
      {
        Log.stackTraceLog(className, "getAnalysisRawDataObj", "", "", "Exception - ", ex);
      }
    }

    return arrListRawDataObject;
  }

  private Object getFinalAnalysisRawDataObj(int granularity, int[] graphDataIndex, String time, String startTimeElapsed, String endTimeElapsed, String graphViewType, String derivedGraphExpression, Boolean isBarChart)
  {
    Log.debugLog(className, "getFinalAnalysisRawDataObj", "", "", "Method called. granularity = " + granularity + ", time = " + time + ", startTimeElapsed = " + startTimeElapsed + ", graphViewType = " + graphViewType + ", isBarChart = " + isBarChart);

    if (graphNames == null)
      graphNames = new GraphNames(testRunNumber);

    GraphUniqueKeyDTO graphUniqueKeyDTO = graphNames.getGraphUniqueKeyDTOByGraphDataIndex(graphDataIndex[0]);
    Log.debugLog(className, "getFinalAnalysisRawDataObj", "", "", "graphUniqueKeyDTO = " + graphUniqueKeyDTO);

    int pdfId = graphNames.getPDFIdByGraphUniqueKeyDTO(graphUniqueKeyDTO);
    int pdfIndex = (int) graphNames.getPDFDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTO);
    int minGranul = 10;

    if (pdfId != -1)
      minGranul = graphNames.getPdfNames().getMinGranuleByPDFId(pdfId);

    return getFinalAnalysisRawDataObj(granularity, graphDataIndex, time, startTimeElapsed, endTimeElapsed, graphViewType, derivedGraphExpression, isBarChart, graphUniqueKeyDTO, pdfId, pdfIndex, minGranul);
  }

  public Object getFinalAnalysisRawDataObj(int granularity, int[] graphDataIndex, String time, String startTimeElapsed, String endTimeElapsed, String graphViewType, String derivedGraphExpression, Boolean isBarChart, GraphUniqueKeyDTO graphUniqueKeyDTO, int pdfId, int pdfIndex, int minGranul)
  {
    String logMsg = " granularity = " + granularity + ", time = " + time + ", startTimeElapsed = " + startTimeElapsed + ", endTimeElapsed = " + endTimeElapsed + ", graphViewType = " + graphViewType + ", derivedGraphExpression = " + derivedGraphExpression + ", isBarChart = " + isBarChart + ", graphUniqueKeyDTO = " + graphUniqueKeyDTO + ", pdfId = " + pdfId + ", pdfIndex = " + pdfIndex + ", minGranul = " + minGranul;
    Log.debugLog(className, "getFinalAnalysisRawDataObj", "", "", "Method Started. " + logMsg);

    try
    {
      if (rptData == null)
      {
        rptData = new ReportData(testRunNumber);
        rptData.openRTGMsgFile();
        rptData.getPhaseTimes();
      }

      int[] arrGraphIndex = graphDataIndex;

      Log.debugLog(className, "getFinalAnalysisRawDataObj", "", "", "arrGraphIndex = " + arrGraphIndex);

      double[] arrDerivData = null;
      double arrGraphData[][] = null;

      if ((graphViewType).equals("Derived Graph"))
      {
        arrGraphData = rptData.getAllGraphsDataFromRTGFile(arrGraphIndex, time, startTimeElapsed, endTimeElapsed, false, true, true, true);
        DerivedData derivedData = new DerivedData(derivedGraphExpression);
        if (derivedData.chkExpIsCompatible(rptData.graphNames) == false)
        {
          Log.debugLog(className, "getFinalAnalysisRawDataObj", "", "", "Method called. Test run = " + testRunNumber + ", Derived report = " + derivedGraphExpression + " is not compatible with this test run.");
          return false;
        }

        arrDerivData = derivedData.calcAndGetDerivedDataArray(arrGraphData, arrGraphIndex, rptData.graphNames);
      }
      // graph data for percentile / slabCount / Freq Distribution
      else if ((graphViewType).equals("Percentile Graph") || (graphViewType).equals("Slab Count Graph") || (graphViewType).equals("Frequency Distribution Graph"))
      {
        Log.debugLog(className, "getFinalAnalysisRawDataObj", "", "", "Method Called. graphViewType = " + graphViewType);
        if ((graphViewType).equals("Percentile Graph"))
        {
          Log.debugLog(className, "getFinalAnalysisRawDataObj", "", "", "pdfIndex = " + pdfIndex + ", pdfId = " + pdfId);

          if (pdfIndex == -1 || pdfId == -1)
          {
            arrGraphData = getPercentileDataForAll(graphUniqueKeyDTO, pdfId, minGranul);
          }
          else
          {
            LinkedHashMap<Integer, GraphUniqueKeyDTO> hmGraphUniqueKeyLinkedHashMap = new LinkedHashMap<Integer, GraphUniqueKeyDTO>();
            hmGraphUniqueKeyLinkedHashMap.put(graphDataIndex[0], graphUniqueKeyDTO);
            arrGraphData = new double[hmGraphUniqueKeyLinkedHashMap.size()][];
            TimeBasedTestRunData timeBasedTestRunData = rptData.getTimeBasedDataFromRTGFile(hmGraphUniqueKeyLinkedHashMap, time, startTimeElapsed, endTimeElapsed, false, false);
            TimeBasedDTO timeBasedDTO = timeBasedTestRunData.getTimeBasedDTO(graphUniqueKeyDTO);
            arrGraphData[0] = timeBasedDTO.getArrGraphSamplesData();
            genPercentileData(testRunNumber, rptData.graphNames, time, startTimeElapsed, endTimeElapsed, arrGraphData, graphViewType);
          }
        }
        else
        {
          arrGraphData = rptData.getAllGraphsDataFromRTGFile(arrGraphIndex, time, startTimeElapsed, endTimeElapsed, false, true, true, true);
          genPercentileData(testRunNumber, rptData.graphNames, time, startTimeElapsed, endTimeElapsed, arrGraphData, graphViewType);
        }
      }
      else if ((graphViewType).equals("Correlated Graph"))
      {
        arrGraphData = rptData.getAllGraphsDataFromRTGFile(arrGraphIndex, time, startTimeElapsed, endTimeElapsed, false, true, true, true);
        genCorrelatedGraphData(arrGraphData, time, startTimeElapsed, endTimeElapsed, granularity);
      }
      else
      {
        arrGraphData = rptData.getAllGraphsDataFromRTGFile(arrGraphIndex, time, startTimeElapsed, endTimeElapsed, false, true, true, true);
        genSimplMultiTileData(arrGraphData, rptData, granularity);
      }

      // set the selected report Data on the basis of specified granularity
      reportDataChangedGranularity = new ReportDataChangedGranularity(rptData.msgData, rptData.graphNames);

      reportDataChangedGranularity.setTemplateLinesDetailInVec(vecTemplateLineDetail);

      if (!(rptName).equals(""))
      {
        reportDataChangedGranularity.setGraphReportDescForTemplate(arrListGraphReportDesc);
        reportDataChangedGranularity.setGraphViewTypeFromTemplate(arrListGraphViewType);
        reportDataChangedGranularity.setTimeOptionFromTemplate(arrListTime);
        reportDataChangedGranularity.setTimeOptionFromTemplate(arrListXaxisFormat);
        reportDataChangedGranularity.setGraphNamesFromTemplate(arrListGraphNames);
      }

      // if arrGraphData is null means time range not matched with TR
      if (arrGraphData != null)
      {
        boolean result;
        if ((graphViewType).equals("Derived Graph"))
        {
          result = reportDataChangedGranularity.genAnalyDerivedDataToShowInPanel(rptData, arrDerivData, granularity);
        }
        else if ((graphViewType).equals("Percentile Graph"))
        {
          result = reportDataChangedGranularity.genAnlsPercentileDataToShowInPanel(vecPercentileGraphData, vecStrLegend);
        }
        else if ((graphViewType).equals("Slab Count Graph"))
        {
          result = reportDataChangedGranularity.genAnlsSlabCountDataToShowInPanel(vecSlabData, vecSlabInfo);
        }
        else if ((graphViewType).equals("Frequency Distribution Graph"))
        {
          result = reportDataChangedGranularity.genAnlsFreqDistDataToShowInPanel(vecFreqDisData, vecFreqDisDataXAxis, vecFreqDisLegendMsg);
        }
        else if ((graphViewType).equals("Correlated Graph"))
        {
          result = reportDataChangedGranularity.genAnlsCorrelatedGraphDataToShowInPanel(arrRptData1, arrRptData2, legendMsgForCorrelated);
        }
        else
        {
          result = reportDataChangedGranularity.genAnalysisDataToShowInPanel(vecSeqNum, vecDataValue, rptData.interval);
        }

        if (result)
          Log.debugLog(className, "getFinalAnalysisRawDataObj", "", "", "Changed granularity report data object is created for the testRunNumber = " + testRunNumber + " successfully.");
        else
          Log.errorLog(className, "getFinalAnalysisRawDataObj", "", "", "Changed granularity data generation failed due to error, plesae see log(s), testRun = " + testRunNumber);
      }
      else
        return null;

      return (Object) (reportDataChangedGranularity);
    }
    catch (Exception e)
    {
      String errMsg = new String("Can not get data");
      Log.stackTraceLog(className, "reportDataChangedGranularity", "", "", "Exception in reportDataChangedGranularity() - ", e);
      return (Object) errMsg;
    }
  }

  /**
   * This Method create percentile graph data from rtgMesage.dat file
   * 
   * @param graphNumber
   * @param pdfId
   * @param minGranul
   */
  public double[][] getPercentileDataForAll(GraphUniqueKeyDTO graphUniqueKeyDTO, int pdfId, int minGranul)
  {
    Log.debugLog(className, "getPercentileDataForAll", "", "", "Method called = " + graphUniqueKeyDTO + ", pdfId = " + pdfId + ", minGranul = " + minGranul);
    try
    {
      PercentileData percentileData = new PercentileData(testRunNumber, rptData.graphNames.getPdfNames());
      double[] percentileGraphData = percentileData.getPercentileGraphData(rptData, graphUniqueKeyDTO, pdfId, minGranul);
      String strLegend = getPercentilString(percentileGraphData, pdfId);
      Log.debugLog(className, "getPercentileDataForAll", "", "", "percentileGraphData size = " + percentileGraphData.length + ", strlength = " + strLegend);

      vecStrLegend.add(strLegend);
      vecPercentileGraphData.add(percentileGraphData);

      double[][] arrResult = new double[1][1];
      arrResult[0] = percentileGraphData;

      return arrResult;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getPercentileDataForAll", "", "", "Exception in getPercentileDataForAll() - ", e);
      return null;
    }
  }

  /**
   * This method create string for percentile data
   * 
   * @param arrGraphData
   * @param arrPercentileGraphData
   * @return
   */
  private String getPercentilString(double[] arrPercentileGraphData, int pdfID)
  {
    Log.debugLogAlways(className, "getPercentilString", "", "", "Method starts pdfID = " + pdfID);

    String pecentiles = "";
    DecimalFormat df = new DecimalFormat("#.###");
    String pdfFourmula = "";
    if (pdfID != -1)
    {
      try
      {
        pdfFourmula = rptData.graphNames.getPdfNames().getPDFUnitByPDFId(pdfID);
      }
      catch (Exception e)
      {
        Log.errorLog(className, "getPercentilString", "", "", "Unable to get pdfFourmula for pdfID = " + pdfID);
      }
    }

    int[] arrPercentileValues = getPercentileValueFromConfig();
    for (int i = 0; i < arrPercentileValues.length; i++)
    {
      if (i == 0)
      {
        if (pdfFourmula != "")
          pecentiles = arrPercentileValues[i] + "th = " + df.format(arrPercentileGraphData[arrPercentileValues[i]]) + " " + pdfFourmula;
        else
          pecentiles = arrPercentileValues[i] + "th = " + df.format(arrPercentileGraphData[arrPercentileValues[i]]);
      }
      else
      {
        if (pdfFourmula != "")
          pecentiles = pecentiles + ", " + arrPercentileValues[i] + "th = " + df.format(arrPercentileGraphData[arrPercentileValues[i]]);
        else
          pecentiles = pecentiles + ", " + arrPercentileValues[i] + "th = " + df.format(arrPercentileGraphData[arrPercentileValues[i]]);
      }
    }

    Log.debugLogAlways(className, "getPercentilString", "", "", "pecentiles = " + pecentiles);
    return pecentiles;
  }

  public int[] getPercentileValueFromConfig()
  {
    int[] arrDefaultValue = { 50, 80, 90, 95, 99 };
    try
    {
      Log.debugLogAlways(className, "getPercentileValueFromConfig", "", "", "Method Called. keyword = netstorm.execution.percentiles");
      String nthPercentile = Config.getValue("netstorm.execution.percentiles");
      if (nthPercentile == null || nthPercentile.equals(""))
        return arrDefaultValue;

      String[] arrNthPercentile = rptUtilsBean.strToArrayData(nthPercentile, ",");
      if (arrNthPercentile == null)
        return arrDefaultValue;

      int[] configuredNthPercentileValue = new int[arrNthPercentile.length];
      for (int i = 0; i < configuredNthPercentileValue.length; i++)
      {
        int percentileValue = Integer.parseInt(arrNthPercentile[i].trim());
        if (percentileValue > 100 || percentileValue < 1)
        {
          Log.errorLog(className, "getPercentileValueFromConfig", "", "", "percentileValue in config.ini cannot be greater then 100 and not less then 1. So using default values. Incorrect value = " + percentileValue);

          return arrDefaultValue;
        }

        configuredNthPercentileValue[i] = percentileValue;
      }

      Log.debugLogAlways(className, "getPercentileValueFromConfig", "", "", "" + rptUtilsBean.intArrayToStr(configuredNthPercentileValue, ","));
      return configuredNthPercentileValue;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getPercentileValueFromConfig", "", "", "Exception - ", e);
    }

    return arrDefaultValue;
  }

  private Boolean getDataFromRTPL(String rtplName)
  {
    Log.debugLog(className, "getDataFromRTPL", "", "", "Method starts");

    arrRtplRecs = ReportTemplate.getTemplateDetails(rtplName, false);
    if (arrRtplRecs.length < 4)
    {
      Log.errorLog(className, "getDataFromRTPL", "", "", "Error: Reporting Template does not have any reports to be generated. Template name is '" + rtplName + "'.");
      return false;
    }

    int i = 3;
    try
    {
      Log.debugLog(className, "genAllRptsAnalysis", "", "", "Method called.Report Set name = " + rtplName + " , testRunNumber = " + testRunNumber + ", graphNames = " + graphNames);
      if (graphNames == null)
        graphNames = new GraphNames(testRunNumber);

      for (i = 3; i < arrRtplRecs.length; i++)
      {
        String[][] arrRptInfo = null;
        if (!(arrRtplRecs[i][1].equals("Derived Graph")))
        {
          try
          {
            arrRptInfo = ReportTemplate.rptInfoToArr(arrRtplRecs[i][arrRtplRecs[i].length - 1], false);
            arrListDerivedGraphExpression.add(" ");
          }
          catch (Exception ex)
          {
            Log.errorLog(className, "getDataFromRTPL", "", "", "exception occurs while getting report info - " + ex);
          }
        }
        else
        {
          arrRptInfo = ReportTemplate.rptInfoToArrForDerived(arrRtplRecs[i][arrRtplRecs[i].length - 1], false);
          arrListDerivedGraphExpression.add(arrRtplRecs[i][arrRtplRecs[i].length - 1]);
        }
        vecTemplateLineDetail.add(arrRtplRecs[i][0] + "|" + arrRtplRecs[i][1] + "|" + arrRtplRecs[i][2] + "|" + arrRtplRecs[i][3] + "|" + arrRtplRecs[i][4] + "|" + arrRtplRecs[i][5] + "|" + arrRtplRecs[i][6] + "|" + arrRtplRecs[i][7] + "|" + arrRtplRecs[i][8] + "|" + arrRtplRecs[i][9] + "|" + arrRtplRecs[i][10] + "|" + arrRtplRecs[i][11]);
        arrListGraphReportDesc.add(arrRtplRecs[i][0]);

        arrListTime.add(arrRtplRecs[i][5]);
        arrListXaxisFormat.add(arrRtplRecs[i][4]);

        if ((arrRtplRecs[i][5]).equals("Specified Time"))
        {
          absoluteToElapsed((arrRtplRecs[i][5]), (arrRtplRecs[i][6]), (arrRtplRecs[i][7]), (arrRtplRecs[i][8]), (arrRtplRecs[i][9]), (arrRtplRecs[i][10]));
        }
        else
        {
          setStartTimeElapsed(arrRtplRecs[i][8]);
          setEndTimeElapsed(arrRtplRecs[i][10]);
        }

        arrListGraphViewType.add(arrRtplRecs[i][1]);

        graphType = arrRtplRecs[i][2];
        GraphUniqueKeyDTO[] arrGraphUniqueKeyDTO = new GraphUniqueKeyDTO[arrRptInfo[0].length];
        String[] graphName = new String[arrGraphUniqueKeyDTO.length];
        int[] graphIndex = new int[arrGraphUniqueKeyDTO.length];

        for (int j = 0; j < arrGraphUniqueKeyDTO.length; j++)
        {
          int groupId = Integer.parseInt(arrRptInfo[0][j].trim());
          int graphId = Integer.parseInt(arrRptInfo[1][j].trim());
          String vectorName = arrRptInfo[3][j].trim();
          if (groupId != 1001)
          {
            GraphUniqueKeyDTO graphUniqueKeyDTO = new GraphUniqueKeyDTO(groupId, graphId, vectorName);
            arrGraphUniqueKeyDTO[j] = graphUniqueKeyDTO;
            int graphDataIndex = graphNames.getGraphDataIdxByGrpIdGraphIdVecName(groupId, graphId, vectorName);
            String tempGraphName = graphNames.getGraphNameByGraphUniqueKeyDTO(graphUniqueKeyDTO, true);
            graphName[j] = tempGraphName;
            graphIndex[j] = graphDataIndex;
          }
        }

        arrRptGraphDataIdx = graphIndex;
        setGraphDataIndex(graphIndex);

        int adjustedGranularity = calculateGranularity(Long.parseLong(arrRtplRecs[i][3]), trDurationForTemplate, isBarChart, graphIndex.length, (arrRtplRecs[i][1]));
        setGranularity(adjustedGranularity);

        arrListGraphNames.add(graphName);
        setGraphCountFromTemplateLine("" + arrGraphUniqueKeyDTO.length);

        // this is for generating percentile data
        String arrRptNamePDFIdPCTDataIdxAndGraphDataIdx[][] = null;
        if (arrRtplRecs[i][1].equals("Percentile Graph") || arrRtplRecs[i][1].equals("Slab Count Graph") || arrRtplRecs[i][1].equals("Frequency Distribution Graph"))
        {
          arrRptNamePDFIdPCTDataIdxAndGraphDataIdx = RptInfo.getRptNamePDFIdPCTDataIdxAndGraphDataIdxByRptInfo(arrRptInfo, graphNames);
          String[] arrRptName = arrRptNamePDFIdPCTDataIdxAndGraphDataIdx[0];// not
          arrPDFID = rptUtilsBean.strArrayToIntArray(arrRptNamePDFIdPCTDataIdxAndGraphDataIdx[1]);
          arrPCTGraphDataIdx = rptUtilsBean.strArrayToIntArray(arrRptNamePDFIdPCTDataIdxAndGraphDataIdx[2]);

          arrGraphDataIdx = rptUtilsBean.strArrayToIntArray(arrRptNamePDFIdPCTDataIdxAndGraphDataIdx[4]);

          if (arrRtplRecs[i][1].equals("Percentile Graph")) // arrRtplRecs[1].equals("Percentile Graph")
            arrPCTTemplateDetail = arrRptNamePDFIdPCTDataIdxAndGraphDataIdx[3];
        }

        if (arrRtplRecs[i][1].equals("Correlated Graph"))
        {
          arrRptNameAndDataIdx = RptInfo.getRptNameAndGraphDataIdxByRptInfo(arrRptInfo, graphNames);
        }

      } // End of all reports in template

      return true;
    }
    catch (Exception e)
    {
      String errMsg = new String("Can not get data");
      Log.stackTraceLog(className, "getDataFromRTPL", "", "", "Exception in getDataFromRTPL() - ", e);
      return false;
    }
  }

  public Boolean genSimplMultiTileData(double[][] arrGraphData, ReportData rptData, int granularity)
  {
    try
    {
      for (int i = 0; i < arrGraphData.length; i++)
      {
        double[] reportData = arrGraphData[i];

        if (!rptData.calDataToShowInRpt(reportData, "", "", "", "", false, "", "", "", granularity))
          Log.errorLog(className, "genSimplMultiTileData", "", "", "Error occur due to wrong data calculation for graph.");

        if (rptData.arrDataVal != null)
          vecDataValue.add(rptData.arrDataVal);

        if (rptData.avgArrSeqNum != null)
          vecSeqNum.add(rptData.avgArrSeqNum);
      }
      return true;
    }
    catch (Exception e)
    {
      return false;
    }
  }

  public Boolean genPercentileData(int numTestRun, GraphNames graphNames, String timeOption, String elapsedStartTime, String elapsedEndTime, double[][] arrGraphData, String graphViewType)
  {
    PercentileData percentileData = null; // declare in global ?
    GraphPCTData[] graphPCTData = null;
    try
    {
      Log.debugLog(className, "genPercentileData", "", "", "Method called");
      percentileData = new PercentileData(numTestRun, graphNames.getPdfNames());

      if (!percentileData.openPCTMsgFile())
      {
        Log.errorLog(className, "readReport", "", "", "Error: In opening PCT data file.");
        return false;
      }

      graphPCTData = percentileData.getAllGraphsDataFromPCTMsgFile(arrPCTGraphDataIdx, arrPDFID, timeOption, elapsedStartTime, elapsedEndTime);
      if (graphPCTData == null)
      {
        Log.errorLog(className, "readPCTRptData", "", "", "Error: Error in getting data from pct data file pctMessage.dat. See error log for details.");
        return false;
      }

      for (int jj = 0; jj < graphPCTData.length; jj++)
      {
        if (arrPCTGraphDataIdx[jj] < 0)
        {
          Log.debugLog(className, "genPercentileGraph", "", "", "Graph data Idx = " + arrGraphDataIdx[jj]);

          double[] arrRptData = rptUtilsBean.getDataFromArrayByIdx(arrRptGraphDataIdx, arrGraphData, arrGraphDataIdx[jj]);

          // Must be calculate pakt range, to set startSeq & endSeq
          rptData.calPktRange(timeOption, elapsedStartTime, elapsedEndTime);

          graphPCTData[jj].calcAndSetCustomGrpData(arrRptData, (int) rptData.startSeq, (int) rptData.endSeq);
        }

        // for percentile graph
        if ((graphViewType).equals("Percentile Graph"))
        {
          String strLegend = ""; // set in reportDataChangedGranularity object
          double[] percentileGraphData = null; // set in
          // reportDataChangedGranularity
          // object

          percentileGraphData = graphPCTData[jj].getArrPercentileData();
          strLegend = getPercentilString(percentileGraphData, arrPDFID[jj]); 

          vecStrLegend.add(strLegend);
          vecPercentileGraphData.add(percentileGraphData);
        }

        // for slab count graph , make global & return with reportdata changed
        // granularity object
        if ((graphViewType).equals("Slab Count Graph"))
        {
          double[] slabData = null;
          SlabInfo[] slabInfo = null;
          slabData = graphPCTData[jj].getArrSlabsData();
          slabInfo = (graphNames.getPdfNames()).getArrSlabsInfoByPDFId(arrPDFID[jj]);

          vecSlabData.add(slabData);
          vecSlabInfo.add(slabInfo);
        }

        // for frequency Distribution graph, return fdData, arrDataXAxis &
        // legend
        if ((graphViewType).equals("Frequency Distribution Graph"))
        {
          double[] fdData = null;
          double[] arrDataXAxis = null;
          String[] freqDisLegendMsg = null;
          fdData = graphPCTData[jj].getArrPCTCumDataBuckets();
          arrDataXAxis = new double[graphPCTData[jj].numGranule];
          // convert it in formula
          double formulaUnitData = graphPCTData[jj].getFormulaUnitData();
          double convertXAxisData = 1;

          if (formulaUnitData != 1)
            convertXAxisData = graphPCTData[jj].minGranule * formulaUnitData;

          for (int ii = 0; ii < arrDataXAxis.length; ii++)
          {
            arrDataXAxis[ii] = (ii + 1) * convertXAxisData;
          }

          // set mean data value & std-dev in graph PCT data, legend Mesg
          double[] arrAvgData = rptUtilsBean.getDataFromArrayByIdx(arrRptGraphDataIdx, rptData.arrAvgDataValuesAll, arrGraphDataIdx[jj]);
          double[] arrStdDevData = rptUtilsBean.getDataFromArrayByIdx(arrRptGraphDataIdx, rptData.arrStdDevDataValuesAll, arrGraphDataIdx[jj]);

          GraphUniqueKeyDTO graphUniqueKeyDTO = graphNames.getGraphUniqueKeyDTOByGraphDataIndex(arrGraphDataIdx[jj]);
          int dataTypeNum = graphNames.getDataTypeNumByGraphUniqueKeyDTO(graphUniqueKeyDTO);
          graphPCTData[jj].setMeanPctDataVal(rptData.getAvgDataBySequenceOnly(arrAvgData, dataTypeNum));
          graphPCTData[jj].setStdDevDataVal(rptData.getStdDevBySequenceOnly(arrStdDevData));

          double meanDataValue = graphPCTData[jj].getMeanPctDataVal();
          double stdDevDataValue = graphPCTData[jj].getStdDevDataVal();
          // declare string array for legend msg
          freqDisLegendMsg = new String[1];
          freqDisLegendMsg[0] = "Mean = " + meanDataValue + " " + graphPCTData[jj].getPDFUnit() + ", Std-Dev = " + stdDevDataValue + " " + graphPCTData[jj].getPDFUnit();

          vecFreqDisData.add(fdData);
          vecFreqDisDataXAxis.add(arrDataXAxis);
          vecFreqDisLegendMsg.add(freqDisLegendMsg[0]);
        }
      }
      return true;
    }
    catch (Exception e)
    {
      String errMsg = new String("Can not get percentile data");
      Log.stackTraceLog(className, "genPercentileData", "", "", "Exception in genPercentileData() - ", e);
      return false;
    }

  }

  public Boolean genCorrelatedGraphData(double[][] arrGraphData, String timeOption, String elapsedStartTime, String elapsedEndTime, int granularity)
  {
    try
    {
      Log.debugLog(className, "genCorrelatedGraphData", "", "", "Method called");
      String[] tempGraphNames = new String[arrRptNameAndDataIdx[0].length];
      for (int jj = 0, n = arrRptNameAndDataIdx[0].length; jj < n; jj++)
        tempGraphNames[jj] = arrRptNameAndDataIdx[0][jj];

      int graphDataIdx1 = Integer.parseInt(arrRptNameAndDataIdx[1][0]);
      int graphDataIdx2 = Integer.parseInt(arrRptNameAndDataIdx[1][1]);

      // get data for one axis to show in report for correlate pattern
      arrRptData1 = rptUtilsBean.getDataFromArrayByIdx(arrRptGraphDataIdx, arrGraphData, graphDataIdx1);
      // get data for another axis to show in report for correlate pattern
      arrRptData2 = rptUtilsBean.getDataFromArrayByIdx(arrRptGraphDataIdx, arrGraphData, graphDataIdx2);

      GraphUniqueKeyDTO graphUniqueKeyDTO1 = graphNames.getGraphUniqueKeyDTOByGraphDataIndex(graphDataIdx1);
      int graphDataType1 = graphNames.getDataTypeNumByGraphUniqueKeyDTO(graphUniqueKeyDTO1);

      GraphUniqueKeyDTO graphUniqueKeyDTO2 = graphNames.getGraphUniqueKeyDTOByGraphDataIndex(graphDataIdx2);
      int graphDataType2 = graphNames.getDataTypeNumByGraphUniqueKeyDTO(graphUniqueKeyDTO2);

      double avgDataBySeqOnly1 = rptData.calcAndGetAvgDataBySeq(arrGraphData, arrRptGraphDataIdx, graphDataIdx1, timeOption, elapsedStartTime, elapsedEndTime, graphDataType1, false);

      String strLegendName1 = " (Avg = ";
      if (graphDataType1 == GraphNameUtils.DATA_TYPE_CUMULATIVE)
        strLegendName1 = " (Total = ";

      double avgDataBySeqOnly2 = rptData.calcAndGetAvgDataBySeq(arrGraphData, arrRptGraphDataIdx, graphDataIdx2, timeOption, elapsedStartTime, elapsedEndTime, graphDataType2, false);

      String strLegendName2 = " (Avg = ";
      if (graphDataType2 == GraphNameUtils.DATA_TYPE_CUMULATIVE)
        strLegendName2 = " (Total = ";

      legendMsgForCorrelated = new String[1];
      legendMsgForCorrelated[0] = tempGraphNames[0] + strLegendName1 + avgDataBySeqOnly1 + ") Vs " + tempGraphNames[1] + strLegendName2 + avgDataBySeqOnly2 + ")";

      // this method calculate count number for averaging
      String corGranularity = Integer.toString(granularity);
      int countAvg = rptData.calCountAvgForCorrelated(graphType, corGranularity);
      // this will avg data array for correlated report
      arrRptData1 = rptData.calDataForCorrelated(arrRptData1, countAvg, graphType);
      arrRptData2 = rptData.calDataForCorrelated(arrRptData2, countAvg, graphType);

      return true;
    }
    catch (Exception e)
    {
      String errMsg = new String("Can not get correlated data");
      Log.stackTraceLog(className, "genCorrelatedGraphData", "", "", "Exception in genCorrelatedGraphData() - ", e);
      return false;
    }

  }

  private void setTestRunNumber(int testNumber)
  {
    testRunNumber = testNumber;
  }

  private void setGranularity(int granularity)
  {
    arrListGranularity.add(granularity);
  }

  private void setGraphDataIndex(int[] graphDataIndex)
  {
    arrListGraphDataIndex.add(graphDataIndex);
  }

  private void setStartTimeElapsed(String startTimeElapsed)
  {
    arrListStartTimeElapsed.add(startTimeElapsed);
  }

  private void setEndTimeElapsed(String endTimeElapsed)
  {
    arrListEndTimeElapsed.add(endTimeElapsed);
  }

  private void setDerivedGraphExpression(String derivedGraphExpression)
  {
    arrListDerivedGraphExpression.add(derivedGraphExpression);
  }

  private void setGraphCountFromTemplateLine(String graphCount)
  {
    arrListGraphCountFromTemplateLine.add(graphCount);
  }

  private int calculateGranularity(long defaultGranularity, String testRunDurationInMillisec, Boolean isBarChart, int noOfGraphs, String graphViewType)
  {
    Log.debugLogAlways(className, "calculateGranularity", "", "", "Start Method. defaultGranularity = " + defaultGranularity + ", testRunDurationInMillisec =" + testRunDurationInMillisec + ", isBarChart = " + isBarChart + " , noOfGraphs = " + noOfGraphs + ", graphViewType = " + graphViewType);
    int calculatedGran = 0;
    double devider = 40 * graphNames.getInterval();

    if (isBarChart)
    {
      if (noOfGraphs > 20 && ((graphViewType).equals("Multi Graph") || (graphViewType).equals("Composite")))
      {
        devider = Double.valueOf(2 * graphNames.getInterval());
      }
      else if ((graphViewType).equals("Multi Graph") || (graphViewType).equals("Composite"))
      {
        devider = (Double.valueOf(20) / noOfGraphs) * graphNames.getInterval();
        if ((Double.valueOf(20) % noOfGraphs) >= 5 || noOfGraphs == 10)
        {
          devider = ((Double.valueOf(20) / noOfGraphs) + 1) * graphNames.getInterval();
        }
      }
      else
      {
        devider = Double.valueOf(20) * graphNames.getInterval();
      }
    }
    if (defaultGranularity <= 0)
    {
      double doubleGran = (Double.valueOf(Long.parseLong(testRunDurationInMillisec)).doubleValue()) / devider;
      calculatedGran = (int) Math.ceil(doubleGran);
    }
    else
    {
      double doubleGran = (defaultGranularity * 1000) / graphNames.getInterval();
      calculatedGran = (int) Math.ceil(doubleGran);
    }
    return calculatedGran;

  }

  private boolean absoluteToElapsed(String timeOption, String timeSelectionFormat, String startDate, String startTime, String endDate, String endTime)
  {
    Log.debugLog(className, "absoluteToElapsed", "", "", "Method called. timeOption = " + timeOption + ", timeSelectionFormat = " + timeSelectionFormat + ", startDate = " + startDate + ", startTime = " + startTime + ", endDate = " + endDate + ", endTime = " + endTime);
    long inputstartTime = 0;
    long inputendTime = 0;

    try
    {
      elapsedStartTime = "NA";
      elapsedEndTime = "NA";
      long testRunTotalTime = (Long.parseLong(trDurationForTemplate));
      Log.debugLog(className, "absoluteToElapsed", "", "", "testRunTotalTime = " + testRunTotalTime);

      if (timeOption.equals("Specified Time"))
      {
        if (timeSelectionFormat.equals("Elapsed"))
        {
          Log.debugLog(className, "absoluteToElapsed", "", "", "Checking if elapsed time specified is within the test run time range");

          inputstartTime = rptUtilsBean.convStrToMilliSec(startTime);// These
          // are
          // elapsed
          // start
          // Time,
          // end Time
          // from
          // template
          inputendTime = rptUtilsBean.convStrToMilliSec(endTime);

          if (testRunTotalTime < inputstartTime)
          {
            Log.debugLog(className, "absoluteToElapsed", "", "", "Error: Elapsed Start Time (" + startTime + ") is less than test run total run time (" + rptUtilsBean.convMilliSecToStr(testRunTotalTime) + ").");
            return false;
          }

          elapsedStartTime = startTime;
          elapsedEndTime = endTime;
        }
        else
        {
          String StartDateTime = startDate + " " + startTime;
          String endDateTime = endDate + " " + endTime;
          Log.debugLog(className, "absoluteToElapsed", "", "", "StartDateTime = " + StartDateTime + ", endDateTime = " + endDateTime);
          inputstartTime = rptUtilsBean.convertDateToMilliSec(StartDateTime); // These
          // are
          // Absolute
          // start
          // Date
          // Time,
          // end
          // Date
          // Time
          // from
          // template
          inputendTime = rptUtilsBean.convertDateToMilliSec(endDateTime);
          Log.debugLog(className, "absoluteToElapsed", "", "", "inputstartTime = " + inputstartTime + ", inputendTime = " + inputendTime + ", testRunStartTime = " + testRunStartTime);

          if (inputstartTime < testRunStartTime)
          {
            Log.debugLog(className, "absoluteToElapsed", "", "", "Warning: Absolute Start Date/Time  is < test run start date/time. Using test run start date/time as start date/time for report generation.");
            inputstartTime = 0;
          }
          else
            inputstartTime = inputstartTime - testRunStartTime;
          // This will fail when no total run time given.
          if (inputstartTime >= testRunTotalTime) // testRunDuration
          {
            Log.debugLog(className, "absoluteToElapsed", "", "", "Error: Absolute Start Date/Time is >= test run end date/time.");
            return false;
          }
          // This will check end date/time with test run's end date/time
          if (inputendTime > testRunTotalTime)
          {
            Log.debugLog(className, "absoluteToElapsed", "", "", "Error: Absolute End Date/Time is >= test run end date/time.");
          }

          elapsedStartTime = rptUtilsBean.convMilliSecToStr(inputstartTime);
          elapsedEndTime = rptUtilsBean.convMilliSecToStr(inputendTime - testRunStartTime);
        }
        setStartTimeElapsed(elapsedStartTime);
        setEndTimeElapsed(elapsedEndTime);
      }
      Log.debugLog(className, "absoluteToElapsed", "", "", "Elapsed Start/End Time = " + elapsedStartTime + "/" + elapsedEndTime);

      return true;
    }
    catch (Exception e)
    {
      Log.reportDebugLog(className, "absoluteToElapsed", "", "", "Exception - " + e);
      return false;
    }
  }

}

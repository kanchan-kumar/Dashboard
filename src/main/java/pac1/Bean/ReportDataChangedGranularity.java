/*--------------------------------------------------------------------
@Name    : ReportDataChangedGranularity.java
@Author  : Prabhat
@Purpose : Keep the selected report data on the basis of specified granularity
@Modification History:
    11/05/2008 -> Prabhat (Initial Version)

----------------------------------------------------------------------*/
package pac1.Bean;
import java.util.Vector;
import pac1.Bean.*;
import pac1.Bean.GraphName.GraphNames;

import java.util.ArrayList;

public class ReportDataChangedGranularity implements java.io.Serializable
{
  private String className = "ReportDataChangedGranularity";

  public Vector vecGraphData = new Vector();
  public Vector vecSeqNumber = new Vector();

  public Vector vecPercentileData = new Vector();
  public Vector vecPercentileStrLegend = new Vector();

  public Vector vecFreqDistData = new Vector();
  public Vector vecFreqDistDataXAxis = new Vector();
  public Vector vecFreqDisLegendMsg = new Vector();

  public Vector vecSlabCountData = new Vector();
  public Vector vecSlabCountInfo = new Vector();

  public Vector vecCorrelatedRptData1 = new Vector();
  public Vector vecCorrelatedRptData2 = new Vector();
  public Vector vecLegendMsgForCorrelated = new Vector();

  public Vector vecTemplateLineDetail = new Vector();

  public long interval = -1;
  public int avgCount = -1;
  public ArrayList arrListReportDesc = new ArrayList();
  public ArrayList arrListGraphViewType = new ArrayList();
  public ArrayList arrListTime = new ArrayList();
  public ArrayList arrListXaxisFormat = new ArrayList();
  public ArrayList arrListGrphNames = new ArrayList();

  private String totalProcessingTime = "0";

  public String[][] templateReportInfo = null; // this field is used in CaldataforAnls.java to give info about template

  public GraphNames graphNames = null;
  public data msgData = null; //this field provides msg data using (RptData) ,to be used in anlalysis GUI

  public ReportDataChangedGranularity()  {  }

  //below constructor is created for CalcDataForAnls.java to provide raw data for analysis gui
  public ReportDataChangedGranularity(data msgData, GraphNames graphNames)
  {
    this.msgData = msgData;
    this.graphNames = graphNames;
  }

  public String getTotalProcessingTime()
  {
    return totalProcessingTime;
  }

  public void setTotalProcessingTime(long startProcessingTime, long endProcessingTime)
  {
    this.totalProcessingTime = rptUtilsBean.convertMilliSecToSecs(endProcessingTime - startProcessingTime);
  }

  // this function is to generate data to show in RTG Panel
  public boolean genDataToShowInPanel(ReportData rptData, double[][] arrGraphData, int avgCount)
  {
    Log.debugLog(className, "genDataToShowInPanel", "", "", "Method Started, AvgCount = " + avgCount);

    try
    {
      this.avgCount = avgCount;

      for(int i = 0; i < arrGraphData.length; i++)
      {
        double[] reportData = arrGraphData[i];

        if(!rptData.calDataToShowInRpt(reportData, "", "", "", "", false, "", "", "", avgCount))
          Log.errorLog(className, "genDataToShowInPanel", "", "", "Error occur due to wrong data calculation for graph.");

        if(rptData.arrDataVal != null)
          setDataInVec(rptData.arrDataVal);

        if(rptData.avgArrSeqNum != null)
          setSeqNumInVec(rptData.avgArrSeqNum);
      }

      interval = rptData.interval;

      return true;
    }
    catch(Exception e)
    {
      return false;
    }
  }

  // this function is to generate data to show in Anlaysis Panel
  public boolean genAnalysisDataToShowInPanel(Vector vSeqNum, Vector vGraphData, long rptIntervel)
  {
    Log.debugLog(className, "genAnalysisDataToShowInPanel", "", "", "Method Started");

    vecGraphData = new Vector();
    vecSeqNumber = new Vector();
    try
    {

      if(vGraphData != null)
        setSimpleMultiDataInVec(vGraphData);

      if(vSeqNum!= null)
        setSimpleMultiSeqNumInVec(vSeqNum);

      interval =  rptIntervel;//Long.parseLong((String) vecIntervel.get(0));
      return true;
    }
    catch(Exception e)
    {
      return false;
    }
  }


  // this function is to generate data to show in Analysis Panel for Derived Graph
  public boolean genAnalyDerivedDataToShowInPanel(ReportData rptData, double[] arrDerivData, int avgCount)
  {
    Log.debugLog(className, "genAnalyDerivedDataToShowInPanel", "", "", "Method Started, AvgCount = " + avgCount);
    try
    {
      this.avgCount = avgCount;
      double[] reportData = arrDerivData;
      if(!rptData.calDataToShowInRpt(reportData, "", "", "", "", false, "", "", "", avgCount))
        Log.errorLog(className, "genAnalyDerivedDataToShowInPanel", "", "", "Error occur due to wrong data calculation for graph.");

      if(rptData.arrDataVal != null)
        setDataInVec(rptData.arrDataVal);

      if(rptData.avgArrSeqNum != null)
        setSeqNumInVec(rptData.avgArrSeqNum);

      interval = rptData.interval;
      return true;
    }
    catch(Exception e)
    {
      return false;
    }
  }

  public boolean genAnlsPercentileDataToShowInPanel(Vector percentileGraphData, Vector strLegend)
  {
    Log.debugLog(className, "genAnlsPercentileDataToShowInPanel", "", "", "Method Started, AvgCount " );

    vecPercentileData = new Vector();
    vecPercentileStrLegend = new Vector();
    try
    {
      if(percentileGraphData != null)
        setPercentileDataInVec(percentileGraphData);

      if(strLegend != null)
        setPercStrLegendInVec(strLegend);

      return true;
    }
    catch(Exception e)
    {
      return false;
    }
  }

  public boolean genAnlsSlabCountDataToShowInPanel(Vector slabData, Vector slabInfo)
  {
    Log.debugLog(className, "genAnlsSlabCountDataToShowInPanel", "", "", "Method Started, AvgCount = " + avgCount);

    vecSlabCountData = new Vector();
    vecSlabCountInfo = new Vector();
    try
    {
      if(slabData != null)
        setSlabCountDataInVec(slabData);

      if(slabInfo != null)
        setSlabCountInfoInVec(slabInfo);

      return true;
    }
    catch(Exception e)
    {
      return false;
    }
  }

  public boolean genAnlsFreqDistDataToShowInPanel(Vector freqDistData, Vector arrfreqDistDataXAxis, Vector freqDisLegendMsg)
  {
    Log.debugLog(className, "genAnlsFreqDistDataToShowInPanel", "", "", "Method Started, AvgCount = " + avgCount);

    vecFreqDistData = new Vector();
    vecFreqDistDataXAxis = new Vector();
    vecFreqDisLegendMsg = new Vector();

    try
    {
      if(freqDistData != null)
      setFreqDistDataInVec(freqDistData);

      if(arrfreqDistDataXAxis != null)
      setFreqDistDataXAxisInVec(arrfreqDistDataXAxis);

      if(freqDisLegendMsg != null)
      setFreqDisLegendMsgInVec(freqDisLegendMsg);

      return true;
    }
    catch(Exception e)
    {
      return false;
    }
  }

  public boolean genAnlsCorrelatedGraphDataToShowInPanel(double[] arrRptData1, double[] arrRptData2, String[] legendMsgForCorrelated)
  {
    Log.debugLog(className, "genAnlsCorrelatedGraphDataToShowInPanel", "", "", "Method Started, AvgCount = " + avgCount);

    vecCorrelatedRptData1 = new Vector();
    vecCorrelatedRptData2 = new Vector();
    vecLegendMsgForCorrelated = new Vector();

    try
    {

      if(arrRptData1 != null)
      setCorrelatedDataInVec1(arrRptData1);

      if(arrRptData2 != null)
      setCorrelatedDataInVec2(arrRptData2);

      if(legendMsgForCorrelated != null)
      setlegendMsgForCorrelated(legendMsgForCorrelated);

      return true;
    }
    catch(Exception e)
    {
      return false;
    }
  }

  public boolean setTemplateLinesDetailInVec(Vector vecTemplateLineDetail)
  {
    Log.debugLog(className, "setTemplateLinesDetailInVec", "", "", "Method Started, getting template details...");
    try
    {
      if(vecTemplateLineDetail != null)
      this.vecTemplateLineDetail = vecTemplateLineDetail;
      return true;
    }
    catch(Exception e)
    {
      return false;
    }
  }

  /************************ Setter to set data depending upon graphViewType, from CalcDataForAnls*******************************/
  private void setDataInVec(double[] arrDataVal)
  {
    vecGraphData.add(arrDataVal);
  }

  private void setSeqNumInVec(double[] arrSeqNum)
  {
    vecSeqNumber.add(arrSeqNum);
  }

  private void setSimpleMultiDataInVec(Vector vecDataVal)
  {
    vecGraphData = vecDataVal;
  }

  private void setSimpleMultiSeqNumInVec(Vector vecSeqNum)
  {
    vecSeqNumber = vecSeqNum;
  }

  private void setPercentileDataInVec(Vector percentileData)
  {
    vecPercentileData = percentileData;
  }

  private void setPercStrLegendInVec(Vector strLegendPercentile)
  {
    vecPercentileStrLegend = strLegendPercentile;
  }

  private void setSlabCountDataInVec(Vector slabData)
  {
    vecSlabCountData = slabData;
  }

  private void setSlabCountInfoInVec(Vector slabInfo)
  {
    vecSlabCountInfo = slabInfo;
  }

  private void setFreqDistDataInVec(Vector freqDistData)
  {
    vecFreqDistData = freqDistData;
  }

  private void setFreqDistDataXAxisInVec(Vector arrfreqDistDataXAxis)
  {
    vecFreqDistDataXAxis = arrfreqDistDataXAxis;
  }
  private void setFreqDisLegendMsgInVec(Vector freqDisLegendMsg)
  {
    vecFreqDisLegendMsg = freqDisLegendMsg;
  }

  private void setCorrelatedDataInVec1(double[] arrRptData1)
  {
    vecCorrelatedRptData1.add(arrRptData1);
  }
  private void setCorrelatedDataInVec2(double[] arrRptData2)
  {
    vecCorrelatedRptData2.add(arrRptData2);
  }
  private void setlegendMsgForCorrelated(String[] legendMsgForCorrelated)
  {
    vecLegendMsgForCorrelated.add(legendMsgForCorrelated);
  }

  //****************Setters to get reqd field from CalcDataForAnls************************************/

  public void setGraphReportDescForTemplate(ArrayList arrListReportDesc)
  {
    this.arrListReportDesc = arrListReportDesc;
  }

  public void setGraphViewTypeFromTemplate(ArrayList arrListGraphViewType)
  {
    this.arrListGraphViewType = arrListGraphViewType;
  }

  public void setTimeOptionFromTemplate(ArrayList arrListTime)
  {
    this.arrListTime = arrListTime;
  }

  public void setGraphNamesFromTemplate(ArrayList arrListGrphNames)
  {
    this.arrListGrphNames = arrListGrphNames;
  }

  public void setXaxisTimeFormat(ArrayList arrListXaxisFormat)
  {
    this.arrListXaxisFormat = arrListXaxisFormat;
  }

  public ArrayList getGraphReportDescForTemplate()
  {
    return arrListReportDesc;
  }

  public ArrayList getGraphViewTypeFromTemplate()
  {
    return arrListGraphViewType;
  }

  public ArrayList getTimeOptionFromTemplate()
  {
    return arrListTime;
  }

  public ArrayList getGraphNamesFromTemplate()
  {
    return arrListGrphNames;
  }

  public ArrayList getXAxisTimeFormat()
  {
    return arrListXaxisFormat;
  }
}

/*--------------------------------------------------------------------
  @Name    : CmpGraphData.java
  @Author  : Prabhat
  @Purpose : Bean for keeping all compare graph data based on measurement & testRun of single report
  @Modification History:
    Initial Version --> 05/05/09 --> Prabhat

----------------------------------------------------------------------*/
package pac1.Bean;

import java.util.Arrays;

public class CmpGraphData
{
  private final String className = "CmpGraphData";

  private int rptGroupID = -1; // report group ID
  private int rptGraphID = -1; // report graph ID
  private String rptNames = ""; // report name
  private String rptGrpNames = ""; // report group name
  private String rptGraphViewType = ""; // graphViewType (simple, multi, tile, corelated, percentile(all 3))
  private String rptTemplateDetail = ""; // rptTemplateDetail used for percentile reports & derived reports

  private int[] arrRptPDFId = null;      // associated pdf ID (for percentile reports) for each msr
  private int[] arrRptGraphDataIdx = null; // this array keep graphdataindex based on measurement(TR) of single report, keep -1 if report not present in TR
  private int[] arrRptPCTGraphDataIdx = null; // this array keep percentile graphdataindex based on measurement(TR) of single report, keep -1 if percentile report not present in TR

  public CmpGraphData(int rptGroupID, int rptGraphID, String rptGrpNames, String rptNames, String rptGraphViewType, String rptTemplateDetail, int numMsr)
  {
    this.rptGroupID = rptGroupID;
    this.rptGraphID = rptGraphID;
    this.rptGrpNames = rptGrpNames;
    this.rptNames = rptNames;
    this.rptGraphViewType = rptGraphViewType;
    this.rptTemplateDetail = rptTemplateDetail;

    // Array of graph data index & PCT data index for one report for all measurements. We are allocating for all measurements
    // but some measurement may not have the report. In that case, -1 will be stored.
    arrRptGraphDataIdx = new int[numMsr];
    arrRptPCTGraphDataIdx = new int[numMsr];
    arrRptPDFId = new int[numMsr];

    Arrays.fill(arrRptGraphDataIdx, -1);
    Arrays.fill(arrRptPCTGraphDataIdx, -1);
    Arrays.fill(arrRptPDFId, -1);
  }

  // set graph data idx, PCT data index & pdf id in array by index for each msr
  public void setGraphDataIdxPCTDataIdxAndPDFId(int indx, int graphDataIdx, int pctDataIdx, int pdfId)
  {
    this.arrRptGraphDataIdx[indx] = graphDataIdx;
    this.arrRptPCTGraphDataIdx[indx] = pctDataIdx;
    this.arrRptPDFId[indx] = pdfId;
  }

  // this function return the report name
  public String getReportName()
  {
    return rptNames;
  }

  // this function return the report group name
  public String getReportGroupName()
  {
    return rptGrpNames;
  }

  // this function return the report graphViewType
  public String getReportGraphViewType()
  {
    return rptGraphViewType;
  }

  // this function return the report template details
  public String getReportTemplateDetails()
  {
    return rptTemplateDetail;
  }

  // this function return the report graph Id
  public int getReportGraphId()
  {
    return rptGraphID;
  }

  // this function return the report group Id
  public int getReportGroupId()
  {
    return rptGroupID;
  }

  // this function return the array of pdfId for each measurement
  public int[] getReportPDFId()
  {
    return arrRptPDFId;
  }

  // this function return the array of graph data index for each measurement
  public int[] getArrGraphDataIdx()
  {
    return arrRptGraphDataIdx;
  }

  // this function return the array of pctDataIndex for each measurement
  public int[] getArrPCTDataIdx()
  {
    return arrRptPCTGraphDataIdx;
  }
}
/*--------------------------------------------------------------------
  @Name    : AnlsCmpRptData.java
  @Author   : Prabhat
  @Purpose : To keep data of Compare Reports using template, for Analysis.
  @Modification History:
    09/23/06 : Prabhat --> Initial Version
----------------------------------------------------------------------*/

package pac1.Bean;

import java.util.ArrayList;

public class AnlsCmpRptData implements java.io.Serializable
{
  private String className = "AnlsCmpRptData";
  static final long serialVersionUID = 8714069508504332700L;
  private ArrayList arrayListChart = new ArrayList();
  private ArrayList arrayListCmpData = new ArrayList();

  private ArrayList arrayListRptName = new ArrayList();
  private ArrayList arrayListRptGrpName = new ArrayList();
  private ArrayList arrayListRptGraphViewType = new ArrayList();
  private ArrayList arrayListRptTemplateDetails = new ArrayList();
  private ArrayList arrayGraphDataType = new ArrayList(); // comma(,) seperated values

  private ArrayList arrayListRptGrpIds = new ArrayList(); // comma(,) seperated values
  private ArrayList arrayListRptIds = new ArrayList(); // comma(,) seperated values
  private ArrayList arrayListRptPDFIds = new ArrayList(); // comma(,) seperated values
  private ArrayList arrayListRptDataIdx = new ArrayList(); // comma(,) seperated values
  private ArrayList arrayListPCTGraphDataIdx = new ArrayList(); // comma(,) seperated values

  public AnlsCmpRptData(){ }

  // add chart to Array list
  public void addChartToList(Object chartObj)
  {
    arrayListChart.add(chartObj);
  }

  // add compare data to Array list
  public boolean addCmpDataToList(String cmpDataLine)
  {
    arrayListCmpData.add(cmpDataLine);
    return true;
  }

  // add report name to Array list
  public void addRptNameToList(String strRptName)
  {
    arrayListRptName.add(strRptName);
  }

  // add report group name to Array list
  public void addRptGroupNameToList(String strRptGroupName)
  {
    arrayListRptGrpName.add(strRptGroupName);
  }

  // add report graph view type to Array list
  public void addRptGraphViewTypeToList(String strRptGraphViewType)
  {
    arrayListRptGraphViewType.add(strRptGraphViewType);
  }

  // add report graph data type to Array list
  public void addGraphDataTypeToList(String strGraphDataType)
  {
    arrayGraphDataType.add(strGraphDataType);
  }

  // add report template detail to Array list
  public void addRptTemplateDetailToList(String strRptTemplateDetails)
  {
    arrayListRptTemplateDetails.add(strRptTemplateDetails);
  }

  // add report Grp ID to Array list
  public void addRptGrpIdsToList(String strRptGrpIds)
  {
    arrayListRptGrpIds.add(strRptGrpIds);
  }

  // add report ID to Array list
  public void addRptIdsToList(String strRptIds)
  {
    arrayListRptIds.add(strRptIds);
  }

  // add report PDF Id to Array list (comma(,) seperated)
  public void addPDFIdsToList(String strRptPDFIds)
  {
    arrayListRptPDFIds.add(strRptPDFIds);
  }

  // add report data idx to Array list (comma(,) seperated)
  public void addRptDataIdxToList(String strRptDataIdx)
  {
    arrayListRptDataIdx.add(strRptDataIdx);
  }

  // add report PCT data idx to Array list (comma(,) seperated)
  public void addPCTGraphDataIdxToList(String strPCTGraphDataIdx)
  {
    arrayListPCTGraphDataIdx.add(strPCTGraphDataIdx);
  }

  // to get the list of JFreeChart
  public ArrayList getChartList()
  {
    return arrayListChart;
  }

  // to get the list of compare data
  public ArrayList getCmpDataList()
  {
    return arrayListCmpData;
  }

  // to get the list of report graph data type
  public ArrayList getGraphDataTypeList()
  {
    return arrayGraphDataType;
  }

  // to get the list of report group ids
  public ArrayList getRptGrpIdsList()
  {
    return arrayListRptGrpIds;
  }

  // to get the list of report ids
  public ArrayList getRptIdsList()
  {
    return arrayListRptIds;
  }

  // to get the list of report PDF ids
  public ArrayList getRptPDFIdsList()
  {
    return arrayListRptPDFIds;
  }

  // to get the list of report data idx
  public ArrayList getRptDataIdxList()
  {
    return arrayListRptDataIdx;
  }

  // to get the list of report PCT data idx
  public ArrayList getRptPCTDataIdxList()
  {
    return arrayListPCTGraphDataIdx;
  }

  // to get the list of report graphDataType
  public ArrayList getRptGraphDataTypeList()
  {
    return arrayListRptGraphViewType;
  }

  // to get the list of report template detail
  public ArrayList getRptTemplateDetailList()
  {
    return arrayListRptTemplateDetails;
  }

  // to get the list of report Name list
  public ArrayList getRptNameList()
  {
    return arrayListRptName;
  }
}


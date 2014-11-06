/*--------------------------------------------------------------------
  @Name    : RptInfo.java
  @Author  : Abhishek/Neeraj
  @Purpose : Bean for Report Group and Report Names related Information.
  @Modification History:
    01/29/07:Abhishek/Neeraj:1.4.2 - Initial Version

----------------------------------------------------------------------*/

package pac1.Bean;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Vector;

import pac1.Bean.GraphName.GraphInfoBean;
import pac1.Bean.GraphName.GraphNames;
import pac1.Bean.GraphName.GroupInfoBean;

public class RptInfo implements java.io.Serializable
{
  private static final long serialVersionUID = 3829636820129919721L;
  static String className = "RptInfo";
  static ArrayList<VectorMappingDTO> vectorMappingList = new ArrayList<VectorMappingDTO>();

  /* Linked HashMap containing the Graph DTO contains information about graphs. HashMap is taken for unique Graph Data Index. */
  static LinkedHashMap<Integer, GraphUniqueKeyDTO> hmGraphInfoDTOList = new LinkedHashMap<Integer, GraphUniqueKeyDTO>();
  
  // All methods using testRunData need to be optimized so that it does not read file again and again.

  // Purpose: Get report group details in 2D array in following format:
  // Report Group Id, Report Group Name, Number of reports in this group and Group Type
  // Arguments: Test Run Data
  //
  // This method is called from both Template JSP and Report Generation Bean (Indirectly)
  // When called from Template, testRunData is dummy (see 'forTemplate' variable in GraphNames.java)
  public static String[][] getRptGrpNames(GraphNames graphNames)
  {
    try
    {
      Log.debugLog(className, "getRptGrpNames", "", "", "Method called. Test Run = " + graphNames.getTestRun());

      int[] arrGroupId = graphNames.getGroupIdArray();
      int numberGroup = arrGroupId.length;
      String[][] tempGroupNamesArr = new String[numberGroup + 1][4]; // 1 Header

      tempGroupNamesArr[0][0] = "Rpt Grp Id";
      tempGroupNamesArr[0][1] = "Rpt Grp Name";
      tempGroupNamesArr[0][2] = "Rpt Grp Count"; // scalar or vector
      tempGroupNamesArr[0][3] = "Rpt Grp Type";

      int groupNumCounter = 0;

      for (int i = 1; i <= numberGroup; i++)
      {
        int groupId = arrGroupId[groupNumCounter];
        tempGroupNamesArr[i][0] = "" + groupId;
        tempGroupNamesArr[i][1] = "" + graphNames.getGroupNameByGroupId(groupId);
        tempGroupNamesArr[i][2] = "" + graphNames.getNumOfGraphsByGroupId(groupId);
        if (graphNames.isGroupTypeVector(groupId))
          tempGroupNamesArr[i][3] = "vector";
        else
          tempGroupNamesArr[i][3] = "scalar";
        groupNumCounter++;
      }

      return tempGroupNamesArr;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getRptGrpNames", "", "", "Exception in getting report group names", e);
      return null;
    }
  }

  // Purpose: Get all report details in 2D array in following format:
  // Report Id, Report Name, Report Type and Index in Graph Data array
  // Arguments: Test Run Data
  //
  // This method is called from both Template JSP and Report Generation Bean (Indirectly)
  // When called from Template, testRunData is dummy (see 'forTemplate' variable in GraphNames.java)
  public static String[][] getRptNames(GraphNames graphNames)
  {
    try
    {
      Log.debugLog(className, "getRptNames", "", "", "Method called. Test Run =" + graphNames.getTestRun());
      int numMiscRpt = 0;

      GraphUniqueKeyDTO[] arrGraphUniqueKeyDTO = graphNames.getGraphUniqueKeyDTO();
      int numberGraph = arrGraphUniqueKeyDTO.length;//graphNames.getTotalNumOfGraphs();
      // To Store Total graphs including Server Stats and Misc
      String[][] tempGraphNamesArr = new String[numberGraph + 1][4];

      tempGraphNamesArr[0][0] = "Rpt Id"; // Report Id in the group
      tempGraphNamesArr[0][1] = "Rpt Name"; // Report name
      tempGraphNamesArr[0][2] = "Rpt Type"; // scalar or vector
      tempGraphNamesArr[0][3] = "Graph Data Index"; // Index in graph data array in data object

      int i, grphNumCounter = 0;

      for (i = 1; i <= numberGraph; i++)
      {
        GraphUniqueKeyDTO graphUniqueKeyDTO = arrGraphUniqueKeyDTO[grphNumCounter];
        if (graphUniqueKeyDTO == null)
          continue;

        GraphInfoBean graphInfoBean = graphNames.getGraphInfoBeanByGraphUniqueKeyDTO(graphUniqueKeyDTO);
        tempGraphNamesArr[i][0] = "" + graphUniqueKeyDTO.getGraphId();
        tempGraphNamesArr[i][1] = graphInfoBean.getGraphName();
        if (graphInfoBean.isGraphTypeVector())
          tempGraphNamesArr[i][2] = "vector";
        else
          tempGraphNamesArr[i][2] = "scalar";

        tempGraphNamesArr[i][3] = "" + graphNames.getGraphDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTO);
        grphNumCounter++;
      }
      
      return tempGraphNamesArr;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getRptNames", "", "", "Exception in getting report report names", e);
      return null;
    }
  }

  /**
   * This method was used to get all report graph names for the given grpNames
   * Note: this method return only unique graph names(ie; group and its graph names)
   * @param rptGrpNames
   * @param graphNames
   * @return getRptNames
   */
  public static String[][] getRptNames(String[][] rptGrpNames, GraphNames graphNames)
  {
    try
    {
      Log.debugLog(className, "getRptNames", "", "", "Method called. Test Run =" + graphNames.getTestRun());
      
      if(rptGrpNames == null)
	return null;

      /*here we need to find the total graph count(excluding vector names)*/
      int numberGraph = getGraphCount(graphNames, false);
      String[][] tempGraphNamesArr = new String[numberGraph + 1][4];
      
      /*At o'th index always heading*/
      tempGraphNamesArr[0][0] = "Rpt Id"; // Report Id in the group
      tempGraphNamesArr[0][1] = "Rpt Name"; // Report name
      tempGraphNamesArr[0][2] = "Rpt Type"; // scalar or vector
      tempGraphNamesArr[0][3] = "Graph Data Index"; // Index in graph data array in data object

      int i, grphNumCounter = 1;

      /*loop is starting from 1 because 0'th was heading*/
      for(i = 1; i < rptGrpNames.length; i++)
      {
	int groupId = Integer.parseInt(rptGrpNames[i][0].trim());
	GroupInfoBean groupInfoBean = graphNames.getHmGroupInfoBean().get(groupId);
	if(groupInfoBean == null)
	  continue;
	
	String[] arrGraphNames = groupInfoBean.getArrGraphNames();
	int[] arrGraphId = groupInfoBean.getArrGraphIds();
	String vecName = "NA";
	
	/*as we need to keep graph data index so we are taking for first vector graph*/
	if(groupInfoBean.isGroupTypeVector())
	  vecName = groupInfoBean.getIndicesNamesArray()[0];
	
	for(int j = 0; j < arrGraphNames.length; j++)
	{
	  tempGraphNamesArr[grphNumCounter][0] = arrGraphId[j] + "";
	  tempGraphNamesArr[grphNumCounter][1] = arrGraphNames[j] + "";
	  
	  if(isVector(groupId, arrGraphId[j], graphNames))
	  tempGraphNamesArr[grphNumCounter][2] = "vector";
	  else
	    tempGraphNamesArr[grphNumCounter][2] = "scalar";
	  
	  tempGraphNamesArr[grphNumCounter][3] = graphNames.getGraphDataIdxByGrpIdGraphIdVecName(groupId, arrGraphId[j], vecName) + "";
	  grphNumCounter++;
	}
      }
      return tempGraphNamesArr;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getRptNames", "", "", "Exception in getting report report names", e);
      return getRptNames(graphNames);
    }
  }
  
  /**
   * This method gives total graph count(excluding/including vector names for false/true)
   * @param graphNames
   * @param includeVectors
   * @return graphCount
   */
  private static int getGraphCount(GraphNames graphNames, boolean includeVectors)
  {
    try
    {
      /*In case of include vector names we total graphs are graphUniqueKeyDTO*/
      if(includeVectors)
	return graphNames.getGraphUniqueKeyDTO().length;
      else
      {
	int graphCount = 0;
	int groupId[] = graphNames.getGroupIdArray();
	for(int i = 0; i < groupId.length; i++)
	{
	  GroupInfoBean groupInfoBean = graphNames.getHmGroupInfoBean().get(groupId[i]);
	  if(groupInfoBean == null)
	    continue;
	  graphCount = graphCount + groupInfoBean.getArrGraphNames().length;
	}
	
	return graphCount;
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getGraphCount", "", "", "Exception in getting getGraphCount", e);
      return 0;
    }
  }
  
  /**
   * This method will check graph type if graph is vector then it will return true
   * @param grpID
   * @param graphID
   * @param graphNames
   * @return
   */
 private static boolean isVector(int grpID, int graphID, GraphNames graphNames)
 {
   try
   {
     String strType = graphNames.getGroupTypeGraphTypeAndNumOfIndices(grpID, graphID);
     String temp[] = rptUtilsBean.strToArrayData(strType, "|");
     if (temp[0].equals("vector") || temp[2].equals("vector"))
       return true;

     return false;
   }
   catch (Exception ex)
   {
     Log.stackTraceLog(className, "isVector", "", "", "Exception in getting graph vector type", ex);
     return false;
   }
 }
 
  public static String[][] getRptNamesAnls(GraphNames graphNames)
  {
    String[][] tempGraphNamesArr = null;
    int numberGraph = 0;
    Log.debugLog(className, "getRptNamesAnls", "", "", "Method called. Test Run =" + graphNames.getTestRun());
    try
    {
      // To Store Total graphs including Server Stats and Misc
      GraphUniqueKeyDTO[] arrGraphUniqueKeyDTO = graphNames.getGraphUniqueKeyDTO();
      tempGraphNamesArr = new String[arrGraphUniqueKeyDTO.length + 1][5];

      tempGraphNamesArr[0][0] = "Rpt Id"; // Report Id in the group
      tempGraphNamesArr[0][1] = "Rpt Name"; // Report name
      tempGraphNamesArr[0][2] = "Rpt Type"; // scalar or vector
      tempGraphNamesArr[0][3] = "Graph Data Index"; // Index in graph data array in data object
      tempGraphNamesArr[0][4] = "Group Id"; // Index in graph data array in data object

      int i, graphNumCounter = 0;
      for (i = 1; i <= arrGraphUniqueKeyDTO.length; i++)
      {
        GraphUniqueKeyDTO graphUniqueKeyDTO = arrGraphUniqueKeyDTO[graphNumCounter];
        if (graphUniqueKeyDTO == null)
          continue;

        GraphInfoBean graphInfoBean = graphNames.getGraphInfoBeanByGraphUniqueKeyDTO(graphUniqueKeyDTO);

        tempGraphNamesArr[i][0] = "" + graphUniqueKeyDTO.getGraphId();
        tempGraphNamesArr[i][1] = graphInfoBean.getGraphName();

        if (graphInfoBean.isGraphTypeVector())
          tempGraphNamesArr[i][2] = "vector";
        else
          tempGraphNamesArr[i][2] = "scalar";

        tempGraphNamesArr[i][3] = "" + graphNames.getGraphDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTO);
        tempGraphNamesArr[i][4] = "" + graphUniqueKeyDTO.getGroupId();
        graphNumCounter++;
      }

      return tempGraphNamesArr;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getRptNamesAnls", "", "", "Exception in getting report report names", e);
      return null;
    }
  }

  // Return report name using report grp id and report id key
  private static String getRptFieldByIdx(int rptGrpId, int rptId, GraphNames graphNames, int idx)
  {
    Log.debugLog(className, "getRptFieldByIdx", "", "", "Method called. rptGrpId = " + rptGrpId + ", RptId = " + rptId + ", Test Run = " + graphNames.getTestRun());
    String strRptFld = null;

    try
    {
      String arrRptGrpName[][] = RptInfo.getRptGrpNames(graphNames);
      String arrRptNames[][] = RptInfo.getRptNames(graphNames);
      int endIdx = 0;
      int startIdx = 0;

      for (int i = 1; i < arrRptGrpName.length; i++)
      {
        startIdx = endIdx;
        endIdx = endIdx + Integer.parseInt(arrRptGrpName[i][2]);
        if (Integer.parseInt(arrRptGrpName[i][0]) == rptGrpId)
        {
          for (int j = startIdx + 1; j < endIdx + 1; j++)
          {
            if (Integer.parseInt(arrRptNames[j][0]) == rptId)
            {
              strRptFld = arrRptNames[j][idx];
            }
          }
        }
      }
      return strRptFld;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getRptFieldByIdx", "", "", "Exception", e);
      return null;
    }
  }

  // Return report name using report grp id and report id key
  public static String getRptName(int rptGrpId, int rptId, GraphNames graphNames)
  {
    Log.debugLog(className, "getRptName", "", "", "Method called. rptGrpId = " + rptGrpId + ", RptId = " + rptId + ", Test Run = " + graphNames.getTestRun());

    return (getRptFieldByIdx(rptGrpId, rptId, graphNames, 1));
  }

  // Get index in Graph Data using Report Grp Id and Report Id
  public static String getRptGraphIndx(int rptGrpId, int rptId, GraphNames graphNames)
  {
    Log.debugLog(className, "getRptGraphIndx", "", "", "Method called. rptGrpId = " + rptGrpId + ", RptId = " + rptId);

    return (getRptFieldByIdx(rptGrpId, rptId, graphNames, 3));
  }

  // This will get all report names and graph data indexes from array of rptGrpIDs and rptIDs
  // It will return 2D array with two rows
  // Row1 is for arrays of all Report Names
  // Row2 is for arrays of Graph Indexes
  public static String[][] getRptNameAndGraphDataIdx(int[] rptGrpId, int[] rptId, GraphNames graphNames)
  {
    // Abhishek (03/13/07)- This will return null if input groupIds or RptID are null
    if (rptGrpId == null || rptId == null)
    {
      Log.errorLog(className, "getRptNameAndGraphDataIdx", "", "", "GrpIds or RptIds are null so no Report's records will return.");
      return null;
    }

    Log.debugLog(className, "getRptNameAndGraphDataIdx", "", "", "Method called. rptGrpId = " + rptUtilsBean.intArrayToStr(rptGrpId, ",") + ", rptId = " + rptUtilsBean.intArrayToStr(rptId, ",") + ", Test Run = " + graphNames.getTestRun());
    String arrTemp = null;
    String[][] tempIndxs = null;

    try
    {
      tempIndxs = new String[2][rptGrpId.length]; // Two row - one for rtpIdx and one for rptName
      String arrRptGrpName[][] = RptInfo.getRptGrpNames(graphNames);
      String arrRptNames[][] = RptInfo.getRptNames(graphNames);

      for (int k = 0; k < rptGrpId.length; k++)
      {
        int endIdx = 0;
        int startIdx = 0;

        for (int i = 1; i < arrRptGrpName.length; i++)
        {
          startIdx = endIdx;
          endIdx = endIdx + Integer.parseInt(arrRptGrpName[i][2]);

          if (Integer.parseInt(arrRptGrpName[i][0]) == rptGrpId[k])
          {
            for (int j = startIdx + 1; j < endIdx + 1; j++)
            {
              if (Integer.parseInt(arrRptNames[j][0]) == rptId[k])
              {
                tempIndxs[0][k] = arrRptNames[j][1];
                tempIndxs[1][k] = arrRptNames[j][3];
              }
            }
          }
        }
      }
      return tempIndxs;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getRptNameAndGraphDataIdx", "", "", "Exception in getting detail", e);
      return null;
    }
  }

  // This is used for getting report graph data Indexes part of array of report Ids part of one Report Group
  // It will return 2D array with two rows
  // Row1 is for arrays of all Report Names
  // Row2 is for arrays of Graph Indexes
  public static String[][] getRptNameAndGraphDataIdxForOneRptGrp(int rptGrpId, int[] rptId, GraphNames graphNames)
  {
    Log.debugLog(className, "getRptNameAndGraphDataIdxForOneRptGrp", "", "", "Method called.  rptGrpId = " + rptGrpId + ", rptId = " + rptUtilsBean.intArrayToStr(rptId, ",") + ", Test Run = " + graphNames.getTestRun());

    int[] rptGroupsIds = new int[rptId.length];

    try
    {
      for (int i = 0; i < rptGroupsIds.length; i++)
        rptGroupsIds[i] = rptGrpId;

      return (getRptNameAndGraphDataIdx(rptGroupsIds, rptId, graphNames));
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getRptNameAndGraphDataIdxForOneRptGrp", "", "", "Exception in getting detail", e);
      return null;
    }
  }

  // --------------------------------------------------------------------------------------------------
  // Purpose: To get all Report Names, Graph Data Index and Report Group Name in graph data in a template file
  // This is used for all Reports except Misc
  // Input : Report Template Name, TestRunData
  // Return : 2D Array with Report Names, Graph Data Index and Report Group Name
  // Row1 : Report Names
  // Row2 : Graph Data Index
  // Row3 : Report Group Name
  // Row4 : PDF Id
  // Row5 : PCTData Idx
  // Row6 : Graph View Type
  // Row7 : Percentile Template details
  // Row8 : Report ID
  // Row9 : Report Group Id
  // --------------------------------------------------------------------------------------------------
  public static String[][] getAllRptDetails(String rtplName, GraphNames graphNames)
  {
    Log.reportDebugLog(className, "getAllRptDetails", "", "", "Method called. Test Run = " + graphNames.getTestRun() + ", rtplName = " + rtplName);

    Vector vcRptGrpId = new Vector();
    Vector vcRptId = new Vector();
    Vector vcGDIdx = new Vector();
    Vector vcRptName = new Vector();
    Vector vcPDFID = new Vector();
    Vector vcPCTDataIdx = new Vector();
    Vector vcPCTTemplateDetail = new Vector();
    Vector vcGraphViewType = new Vector();

    Vector vcTempRptName = new Vector();
    Vector vcTempGraphDataIdx = new Vector();
    Vector vcTempRptGrpName = new Vector();
    Vector vcTempPDFID = new Vector();
    Vector vcTempPCTDataIdx = new Vector();
    Vector vcTempGraphViewType = new Vector();
    Vector vcTempPCTTemplateDetail = new Vector();
    Vector vcTempReportId = new Vector();
    Vector vcTempReportGroupId = new Vector();
    // 2D Array store Row1-Report name,Row2-Graph Data Index,Row3-Report Group Name,Row4-PDFId,Row5-PCTData Idx,Row6-Graph View Type,Row7-Percentile Template
    // details,Row8-Report Id,Row9-ReportGroup Id
    String[][] arrRptDetails = null;

    String[][] arrRptGrpInfo = null; // 2D Array store Row1-Report name,Row2-Graph Data Index

    try
    {
      String arrRtplRecs[][] = ReportTemplate.getTemplateDetails(rtplName, false);
      int arrRtplRecsLen = arrRtplRecs.length;
      for (int i = 3; i < arrRtplRecsLen; i++) // First three lines are not for reports
      {
        String tempReportDesc = arrRtplRecs[i][0]; // used for derived reports
        String tempGraphViewType = arrRtplRecs[i][1];

        String[][] arrRptInfo = null;
        String strTemplateInfo = arrRtplRecs[i][arrRtplRecs[i].length - 1];
        if (tempGraphViewType.equals("Derived Graph"))
          arrRptInfo = ReportTemplate.rptInfoToArrForDerived(strTemplateInfo, false);
        else
          arrRptInfo = ReportTemplate.rptInfoToArr(strTemplateInfo, false);

        if (addRptGrpIdAndRptIdToVector(arrRptInfo, tempGraphViewType, tempReportDesc, vcRptGrpId, vcRptId, vcGDIdx, vcRptName, graphNames, vcPDFID, vcPCTDataIdx, vcPCTTemplateDetail, vcGraphViewType, strTemplateInfo) == false)
          return null;
      }

      // to getting grp name
      arrRptGrpInfo = getRptGrpNames(graphNames);

      for (int i = 0; i < vcRptGrpId.size(); i++)
      {
        String strRptGrpName = "";
        String tempGraphViewType = vcGraphViewType.get(i).toString();

        if (tempGraphViewType.equals("Derived Graph"))
          strRptGrpName = "Derived Group";
        else
        {
          int tempGrpId = Integer.parseInt(vcRptGrpId.get(i).toString());
          // to getting Group Name
          for (int j = 1; j < arrRptGrpInfo.length; j++)
          {
            if (tempGrpId == (Integer.parseInt(arrRptGrpInfo[j][0])))
            {
              strRptGrpName = "" + arrRptGrpInfo[j][1];
              break;
            }
          }
        }

        if (tempGraphViewType.equals("Percentile Graph"))
        {
          //if (graphNames.getPdfNames().pdfInfo != null)
          {
            String tempTemplateDetail = vcPCTTemplateDetail.get(i).toString();
            String[] percentileIdx = rptUtilsBean.strToArrayData(tempTemplateDetail, ";");

            for (int ii = 0; ii < percentileIdx.length; ii++)
            {
              vcTempRptName.add(vcRptName.get(i).toString() + " - " + percentileIdx[ii] + "th percentile");

              vcTempGraphDataIdx.add(vcGDIdx.get(i).toString());

              vcTempRptGrpName.add(strRptGrpName);

              vcTempPDFID.add(vcPDFID.get(i).toString());

              vcTempPCTDataIdx.add(vcPCTDataIdx.get(i).toString());

              vcTempGraphViewType.add(tempGraphViewType);

              vcTempPCTTemplateDetail.add(tempTemplateDetail);

              vcTempReportId.add(vcRptId.get(i).toString());

              vcTempReportGroupId.add(vcRptGrpId.get(i).toString());
            }
          }
        }

        else if (tempGraphViewType.equals("Slab Count Graph"))
        {
          //if (graphNames.getPdfNames().pdfInfo != null)
          {
            int tempPDFId = Integer.parseInt(vcPDFID.get(i).toString());
            PDFNames pdfNames = graphNames.getPdfNames();
            SlabInfo[] slabInfo = null;
            
            if(pdfNames != null)
              slabInfo = pdfNames.getArrSlabsInfoByPDFId(tempPDFId);
            
            if (slabInfo != null)
            {
              for (int ii = 0; ii < slabInfo.length; ii++)
              {
                vcTempRptName.add(vcRptName.get(i).toString() + " - " + slabInfo[ii].getSlabName());

                vcTempGraphDataIdx.add(vcGDIdx.get(i).toString());

                vcTempRptGrpName.add(strRptGrpName);

                vcTempPDFID.add(vcPDFID.get(i).toString());

                vcTempPCTDataIdx.add(vcPCTDataIdx.get(i).toString());

                vcTempGraphViewType.add(tempGraphViewType);

                vcTempPCTTemplateDetail.add(vcPCTTemplateDetail.get(i).toString());

                vcTempReportId.add(vcRptId.get(i).toString());

                vcTempReportGroupId.add(vcRptGrpId.get(i).toString());
              }
            }
            else
            {
              for (int ii = 0; ii < 10; ii++)
              {
                vcTempRptName.add(vcRptName.get(i).toString() + " - " + ii + "-" + (ii+1) + " s");

                vcTempGraphDataIdx.add(vcGDIdx.get(i).toString());

                vcTempRptGrpName.add(strRptGrpName);

                vcTempPDFID.add(vcPDFID.get(i).toString());

                vcTempPCTDataIdx.add(vcPCTDataIdx.get(i).toString());

                vcTempGraphViewType.add(tempGraphViewType);

                vcTempPCTTemplateDetail.add(vcPCTTemplateDetail.get(i).toString());

                vcTempReportId.add(vcRptId.get(i).toString());

                vcTempReportGroupId.add(vcRptGrpId.get(i).toString());
              }
            }
          }
        }

        else if (tempGraphViewType.equals("Frequency Distribution Graph"))
        {
          if (graphNames.getPdfNames().pdfInfo != null)
          {
            // For mean & std-dev
            for (int ii = 0; ii < 2; ii++)
            {
              String tempDetail = "";
              if (ii == 0)
                tempDetail = "Mean";

              if (ii == 1)
                tempDetail = "StdDev";

              vcTempRptName.add(vcRptName.get(i).toString() + " - " + tempDetail);

              vcTempGraphDataIdx.add(vcGDIdx.get(i).toString());

              vcTempRptGrpName.add(strRptGrpName);

              vcTempPDFID.add(vcPDFID.get(i).toString());

              vcTempPCTDataIdx.add(vcPCTDataIdx.get(i).toString());

              vcTempGraphViewType.add(tempGraphViewType);

              vcTempPCTTemplateDetail.add("NA");

              vcTempReportId.add(vcRptId.get(i).toString());

              vcTempReportGroupId.add(vcRptGrpId.get(i).toString());
            }
          }
        }

        else if (tempGraphViewType.equals("Derived Graph"))
        {
          vcTempRptName.add(vcRptName.get(i).toString());

          vcTempGraphDataIdx.add(vcGDIdx.get(i).toString());

          vcTempRptGrpName.add(strRptGrpName);

          vcTempPDFID.add(vcPDFID.get(i).toString());

          vcTempPCTDataIdx.add(vcPCTDataIdx.get(i).toString());

          vcTempGraphViewType.add(tempGraphViewType);

          vcTempPCTTemplateDetail.add(vcPCTTemplateDetail.get(i).toString());

          vcTempReportId.add(vcRptId.get(i).toString());

          vcTempReportGroupId.add(vcRptGrpId.get(i).toString());
        }
        else
        {
          vcTempRptName.add(vcRptName.get(i).toString());

          vcTempGraphDataIdx.add(vcGDIdx.get(i).toString());

          vcTempRptGrpName.add(strRptGrpName);

          vcTempPDFID.add(vcPDFID.get(i).toString());

          vcTempPCTDataIdx.add(vcPCTDataIdx.get(i).toString());

          vcTempGraphViewType.add(tempGraphViewType);

          vcTempPCTTemplateDetail.add("NA");

          vcTempReportId.add(vcRptId.get(i).toString());

          vcTempReportGroupId.add(vcRptGrpId.get(i).toString());
        }
      }

      arrRptDetails = new String[9][vcTempRptName.size()];

      for (int ii = 0; ii < vcTempRptName.size(); ii++)
      {
        arrRptDetails[0][ii] = vcTempRptName.get(ii).toString();

        arrRptDetails[1][ii] = vcTempGraphDataIdx.get(ii).toString();

        arrRptDetails[2][ii] = vcTempRptGrpName.get(ii).toString();

        arrRptDetails[3][ii] = vcTempPDFID.get(ii).toString();

        arrRptDetails[4][ii] = vcTempPCTDataIdx.get(ii).toString();

        arrRptDetails[5][ii] = vcTempGraphViewType.get(ii).toString();

        arrRptDetails[6][ii] = vcTempPCTTemplateDetail.get(ii).toString();

        arrRptDetails[7][ii] = vcTempReportId.get(ii).toString();

        arrRptDetails[8][ii] = vcTempReportGroupId.get(ii).toString();

        Log.debugLog(className, "getAllRptDetails", "", "", "Total Reports = " + vcTempRptName.size() + ", RptName = " + arrRptDetails[0][ii] + ", Graph data Indx = " + arrRptDetails[1][ii] + ", Rpt Grp Name = " + arrRptDetails[2][ii] + ", PDF Id = " + arrRptDetails[3][ii] + ", PCT DataIdx = " + arrRptDetails[4][ii] + ", Graph View type = " + arrRptDetails[5][ii] + ", PCT  Template detail = " + arrRptDetails[6][ii] + ", Report ID = " + arrRptDetails[7][ii] + ", Report Group ID = " + arrRptDetails[8][ii]);
      }
      
      return arrRptDetails;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getAllRptDetails", "", "", "Exception in getting report id", e);
      return null;
    }
  }
  
  public static LinkedHashMap<Integer, GraphUniqueKeyDTO> gethmGraphInfoDTOList()
  {
    return new LinkedHashMap<Integer, GraphUniqueKeyDTO>(hmGraphInfoDTOList);
  }

  // --------------------------------------------------------------------------------------------------
  // Purpose: To get all report names and their index in graph data in a template file
  // This is used for all Reports except Misc
  // Input : None
  // Return : 2D Array with Report Names and Index in Graph Data
  // --------------------------------------------------------------------------------------------------
  public static String[][] getAllRptNameAndGraphDataIdx(String rtplName, GraphNames graphNames)
  {
    Log.debugLog(className, "getAllRptNameAndGraphDataIdx", "", "", "Method called. Test Run = " + graphNames.getTestRun() + ", rtplName = " + rtplName);

    Vector vcRptGrpId = new Vector();
    Vector vcRptId = new Vector();
    Vector vcGDIdx = new Vector();
    Vector vcRptName = new Vector();
    Vector vcPDFID = new Vector();
    Vector vcPCTDataIdx = new Vector();
    Vector vcPCTTemplateDetail = new Vector();
    Vector vcGraphViewType = new Vector();

    try
    {
      String arrRtplRecs[][] = ReportTemplate.getTemplateDetails(rtplName, false);
      for (int i = 3; i < arrRtplRecs.length; i++) // First three lines are not for reports
      {
        String tempReportDesc = arrRtplRecs[i][0];
        String tempGraphViewType = arrRtplRecs[i][1];
        String derivedExpression = arrRtplRecs[i][arrRtplRecs[i].length - 1];

        String[][] arrRptInfo = null;
        if (tempGraphViewType.equals("Derived Graph"))
          arrRptInfo = ReportTemplate.rptInfoToArrForDerived(derivedExpression, false);
        else
          // Get report info in 2D array of the report which is last column in the row
          arrRptInfo = ReportTemplate.rptInfoToArr(arrRtplRecs[i][arrRtplRecs[i].length - 1], false);

        for (int j = 0; j < arrRptInfo[0].length; j++)
        {
          if (addRptGrpIdAndRptIdToVector(arrRptInfo, tempGraphViewType, tempReportDesc, vcRptGrpId, vcRptId, vcGDIdx, vcRptName, graphNames, vcPDFID, vcPCTDataIdx, vcPCTTemplateDetail, vcGraphViewType, derivedExpression) == false)
            return null;
        }
      }

      int[] rptGrpId = new int[vcRptGrpId.size()];
      int[] rptId = new int[vcRptId.size()];
      int[] graphDataIndx = new int[vcGDIdx.size()];
      String[] rptName = new String[vcRptName.size()];
      String[][] tempRptNameAndGDIdx = new String[2][vcRptName.size()];

      for (int ii = 0; ii < vcRptGrpId.size(); ii++)
      {
        rptGrpId[ii] = Integer.parseInt(vcRptGrpId.elementAt(ii).toString());
        rptId[ii] = Integer.parseInt(vcRptId.elementAt(ii).toString());

        graphDataIndx[ii] = Integer.parseInt(vcGDIdx.elementAt(ii).toString());
        rptName[ii] = vcRptName.elementAt(ii).toString();

        tempRptNameAndGDIdx[0][ii] = rptName[ii];
        tempRptNameAndGDIdx[1][ii] = "" + graphDataIndx[ii];

        Log.debugLog(className, "getAllRptNameAndGraphDataIdx", "", "", "RptGrpId = " + rptGrpId[ii] + ", RptId = " + rptId[ii] + ", Graph data Indx = " + graphDataIndx[ii] + ", Rpt Name = " + rptName[ii]);
      }

      return tempRptNameAndGDIdx;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getAllRptNameAndGraphDataIdx", "", "", "Exception in getting report id", e);
      return null;
    }
  }

  /**
   * Method used to add Graph Information in Graph DTO List.
   * 
   * @param groupId
   * @param graphId
   * @param vectorName
   * @param graphDataIndex
   */
  private static void addGraphInfoInDTOList(int groupId, int graphId, String vectorName, int graphDataIndex)
  {
    /* Adding Graph DTO to List. */
    GraphUniqueKeyDTO graphUniqueKeyDTOObj = new GraphUniqueKeyDTO(groupId, graphId, vectorName);
    graphUniqueKeyDTOObj.setGraphDataIndex(graphDataIndex);

    /* Adding Unique Data Index. */
    hmGraphInfoDTOList.put(graphDataIndex, graphUniqueKeyDTOObj);
  }
  

  // --------------------------------------------------------------------------------------------------
  // Purpose: To get all report grp Id and report Id on one report of template in two vectors.
  // For server stats report, based on number of servers, it will return Ids for all servers
  // This is used for all Reports except Misc
  // Input :
  // 1. String[][] arrRptInfo - 2D array of report info one one report in template file.
  // 2. Vector vcRptGrpId - Vector for putting report group Ids
  // 3. Vector vcRptId - Vector for putting report Ids
  // 4. Vector vcGDIdx - Vector for putting Graph Data Index
  // 5. Vector vcRptName - Vector for putting report Names
  // Return : true if sucessfile else false
  // --------------------------------------------------------------------------------------------------
  private static boolean addRptGrpIdAndRptIdToVector(String[][] arrRptInfo, String graphViewType, String reportDescription, Vector vcRptGrpId, Vector vcRptId, Vector vcGDIdx, Vector vcRptName, GraphNames graphNames, Vector vcPDFID, Vector vcPCTDataIdx, Vector vcPCTTemplateDetail, Vector vcGraphViewType, String derivedExpression)
  {
    try
    {
      Log.reportDebugLog(className, "addRptGrpIdAndRptIdToVector", "", "", "Method called. graphViewType = " + graphViewType);
      String[] arrVecGrpName = null;
      String[] arrTemp = null;
      String[] arrVecGraphName = null;
      int index = 0;
      int graphcount = 0;

      if (vectorMappingList == null || vectorMappingList.size() == 0)
        vectorMappingList = rptUtilsBean.getVectorNameFromMappedFile("VectorMapping.dat");

      int totalLen = arrRptInfo[0].length;
      for (int j = 0; j < totalLen; j++)
      {
        int groupId = Integer.parseInt(arrRptInfo[0][j]);

        // Skip Miscellaneous
        if (groupId == graphNames.getMiscRptGrpId())
          continue;

        int graphId = Integer.parseInt(arrRptInfo[1][j]);
        String groupGraphType = graphNames.getGroupTypeGraphTypeAndNumOfIndices(groupId, graphId);
        String[] arrInfo = rptUtilsBean.strToArrayData(groupGraphType, "|");
        if (arrInfo == null || arrInfo.length < 4) // If Report is not available in the test run
        {
          Log.debugLog(className, "addRptGrpIdAndRptIdToVector", "", "", "Report is Ignored, not present in the Test Run. Grp Id/Graph Id = (" + groupId + "/" + arrRptInfo[1][j] + ")");
          continue;
        }

        // for all graph count before match the Rpt Grp Id
        String groupType = arrInfo[0];
        String graphType = arrInfo[2];

        Log.reportDebugLog(className, "addRptGrpIdAndRptIdToVector", "", "", "groupId = " + groupId + ", graphId = " + graphId + ", groupGraphType = " + groupGraphType + ", groupType = " + groupType + ", graphType = " + graphType);

        // If Group is Vector
        if (groupType.equals("vector"))
        {
          graphcount = graphNames.getNumOfGraphsByGroupId(groupId);
          arrVecGrpName = graphNames.getNameOfGroupIndicesByGroupId(groupId);
          String vectorOption = arrRptInfo[2][j];
          // For option 1 add all reports Id for all Vector Group in the test run
          if (vectorOption.equals("1"))
          {
            for (int i = 0; i < arrVecGrpName.length; i++)
            {
              String vectorName = arrVecGrpName[i];
              vcRptGrpId.add(groupId);
              vcRptId.add(graphId);
              int dataIndex = graphNames.getGraphDataIdxByGrpIdGraphIdVecName(groupId, graphId, vectorName);

              vcGDIdx.add(dataIndex);
              addGraphInfoInDTOList(groupId, graphId, vectorName, dataIndex);

              GraphUniqueKeyDTO graphUniqueKeyDTO = new GraphUniqueKeyDTO(groupId, graphId, vectorName);
              // In case of derived reports use report description as Report Name
              if (graphViewType.equals("Derived Graph"))
                vcRptName.add(reportDescription);
              else
                vcRptName.add(graphNames.getGraphNameByGraphUniqueKeyDTO(graphUniqueKeyDTO, true));

              int pdfId = graphNames.getPDFIdByGraphUniqueKeyDTO(graphUniqueKeyDTO);
              long pctDataIndex = graphNames.getPDFDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTO);

              vcPDFID.add("" + pdfId);
              vcPCTDataIdx.add("" + pctDataIndex);
              vcGraphViewType.add(graphViewType);

              if (arrRptInfo.length > 4)
              {
                String strDetails = arrRptInfo[4][j];
                vcPCTTemplateDetail.add(strDetails);
              }
              else
              {
                if (graphViewType.equals("Derived Graph"))
                  vcPCTTemplateDetail.add(derivedExpression);
                else
                  vcPCTTemplateDetail.add("NA");
              }
            }
          }
          else if (vectorOption.equals("2")) // For option 2 add all reports Id for specified Vector if found in the test run
          {
            // More than one vector names seperated by ";"
            String vectorNamesList = arrRptInfo[3][j];
            arrTemp = rptUtilsBean.strToArrayData(vectorNamesList, ";");
            for (int idx = 0; idx < arrTemp.length; idx++)
            {
              String oldVector = arrTemp[idx];
              String vectorNameInTemplate = arrTemp[idx];
              int i = 0;
              for (i = 0; i < arrVecGrpName.length; i++)
              {
                String tmpVectorName = arrVecGrpName[i];
                if (vectorNameInTemplate.equals(tmpVectorName))
                {
                  vcRptGrpId.add(groupId);
                  vcRptId.add(graphId);
                  GraphUniqueKeyDTO graphUniqueKeyDTO = new GraphUniqueKeyDTO(groupId, graphId, tmpVectorName);
                  int dataIndex = graphNames.getGraphDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTO);
                  vcGDIdx.add("" + dataIndex);

                  /* Adding Graph DTO to List. */
                  addGraphInfoInDTOList(groupId, graphId, tmpVectorName, dataIndex);

                  // In case of derived reports use report description as Report Name
                  if (graphViewType.equals("Derived Graph"))
                    vcRptName.add(reportDescription);
                  else
                    vcRptName.add(graphNames.getGraphNameByGraphUniqueKeyDTO(graphUniqueKeyDTO, true));

                  int pdfId = graphNames.getPDFIdByGraphUniqueKeyDTO(graphUniqueKeyDTO);
                  long pctGraphDataIndex = graphNames.getPDFDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTO);

                  vcPDFID.add("" + pdfId);
                  vcPCTDataIdx.add("" + pctGraphDataIndex);
                  vcGraphViewType.add(graphViewType);

                  if (arrRptInfo.length > 4)
                  {
                    vcPCTTemplateDetail.add(arrRptInfo[4][j]);
                  }
                  else
                  {
                    // In case of derived reports derived expression is store in template detail
                    if (graphViewType.equals("Derived Graph"))
                      vcPCTTemplateDetail.add(derivedExpression);
                    else
                      vcPCTTemplateDetail.add("NA");
                  }

                  break;
                } // end of "if" block
              } // end of Inner for loop

              // Vector Not test run
              if (i == arrVecGrpName.length)
              {
                Log.reportDebugLog(className, "addRptGrpIdAndRptIdToVector", "", "", "vectorNameInTemplate not found in test run " + graphNames.getTestRun() + ", try to find from vector mapping file.");
                // Getting Vector Name From Mapping File.
                vectorNameInTemplate = rptUtilsBean.getVectorNameFromVectorMapping(vectorMappingList, groupId, vectorNameInTemplate);
                int ii = 0;
                for (ii = 0; ii < arrVecGrpName.length; ii++)
                {
                  String strVectorName = arrVecGrpName[ii];
                  if (vectorNameInTemplate.equals(strVectorName))
                  {
                    arrRptInfo[3][j] = arrRptInfo[3][j].replace(oldVector, vectorNameInTemplate);
                    vcRptGrpId.add(groupId);
                    vcRptId.add(graphId);

                    GraphUniqueKeyDTO graphUniqueKeyDTO = new GraphUniqueKeyDTO(groupId, graphId, strVectorName);
                    int dataIndex = graphNames.getGraphDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTO);
                    vcGDIdx.add("" + dataIndex);

                    /* Adding Graph DTO to List. */
                    addGraphInfoInDTOList(groupId, graphId, strVectorName, dataIndex);

                    // In case of derived reports use report description as Report Name
                    if (graphViewType.equals("Derived Graph"))
                      vcRptName.add(reportDescription);
                    else
                      vcRptName.add(graphNames.getGraphNameByGraphUniqueKeyDTO(graphUniqueKeyDTO, true));

                    int pdfId = graphNames.getPDFIdByGraphUniqueKeyDTO(graphUniqueKeyDTO);
                    vcPDFID.add("" + pdfId);
                    long pctDataIndex = graphNames.getPDFDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTO);
                    vcPCTDataIdx.add("" + pctDataIndex);
                    vcGraphViewType.add(graphViewType);

                    if (arrRptInfo.length > 4)
                    {
                      vcPCTTemplateDetail.add(arrRptInfo[4][j]);
                    }
                    else
                    {
                      if (graphViewType.equals("Derived Graph"))
                        vcPCTTemplateDetail.add(derivedExpression);
                      else
                        vcPCTTemplateDetail.add("NA");
                    }

                    break;
                  } // end of "if" block
                } // end of Inner for loop
                if (ii == arrVecGrpName.length)
                  Log.debugLogAlways(className, "addRptGrpIdAndRptIdToVector", "", "", "Specified Vector not found in test run data. Vector name = " + arrTemp[idx]);
              }
            } // end of Outer for loop
          } // end of "else if" block
        } // end of "if" block
        else if (graphType.equals("vector")) // If Graph is Vector
        {
          arrVecGraphName = graphNames.getNameOfIndicesByGroupIdAndGraphId(groupId, graphId);
          String vectorOption = arrRptInfo[2][j];
          if (vectorOption.equals("1"))
          {
            for (int i = 0; i < arrVecGraphName.length; i++)
            {
              vcRptGrpId.add("" + groupId);
              vcRptId.add("" + arrRptInfo[1][j]);
              String vectorName = arrVecGraphName[i];

              GraphUniqueKeyDTO graphUniqueKeyDTO = new GraphUniqueKeyDTO(groupId, graphId, vectorName);

              int graphDataIndex = graphNames.getGraphDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTO);
              vcGDIdx.add("" + graphDataIndex);

              /* Adding Graph DTO to List. */
              addGraphInfoInDTOList(groupId, graphId, vectorName, graphDataIndex);

              // In case of derived reports use report description as Report Name
              if (graphViewType.equals("Derived Graph"))
                vcRptName.add(reportDescription);
              else
                vcRptName.add(graphNames.getGraphNameByGraphUniqueKeyDTO(graphUniqueKeyDTO, true));

              vcPDFID.add("" + graphNames.getPDFIdByGraphUniqueKeyDTO(graphUniqueKeyDTO));
              vcPCTDataIdx.add("" + graphNames.getPDFDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTO));
              vcGraphViewType.add(graphViewType);

              if (arrRptInfo.length > 4)
              {
                vcPCTTemplateDetail.add(arrRptInfo[4][j]);
              }
              else
              {
                if (graphViewType.equals("Derived Graph"))
                  vcPCTTemplateDetail.add(derivedExpression);
                else
                  vcPCTTemplateDetail.add("NA");
              }

              index++;
            }
          }
          else if (vectorOption.equals("2"))
          {
            arrTemp = rptUtilsBean.strToArrayData(arrRptInfo[3][j], ";"); // More than one vector names seperated by ";"
            for (int idx = 0; idx < arrTemp.length; idx++)
            {
              int i = 0;
              for (i = 0; i < arrVecGraphName.length; i++)
              {
                if (arrTemp[idx].equals(arrVecGraphName[i]))
                {
                  vcRptGrpId.add("" + groupId);
                  vcRptId.add("" + arrRptInfo[1][j]);
                  GraphUniqueKeyDTO graphUniqueKeyDTO = new GraphUniqueKeyDTO(groupId, graphId, arrVecGraphName[i]);
                  int graphDataIndex = graphNames.getGraphDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTO);
                  vcGDIdx.add("" + graphDataIndex);

                  addGraphInfoInDTOList(groupId, graphId, arrVecGraphName[i], graphDataIndex);

                  // In case of derived reports use report description as Report Name
                  if (graphViewType.equals("Derived Graph"))
                    vcRptName.add(reportDescription);
                  else
                    vcRptName.add(graphNames.getGraphNameByGraphUniqueKeyDTO(graphUniqueKeyDTO, true));

                  vcPDFID.add("" + graphNames.getPDFIdByGraphUniqueKeyDTO(graphUniqueKeyDTO));
                  vcPCTDataIdx.add("" + graphNames.getPDFDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTO));
                  vcGraphViewType.add(graphViewType);

                  if (arrRptInfo.length > 4)
                  {
                    vcPCTTemplateDetail.add(arrRptInfo[4][j]);
                  }
                  else
                  {
                    // In case of derived reports derived expression is store in template detail
                    if (graphViewType.equals("Derived Graph"))
                      vcPCTTemplateDetail.add(derivedExpression);
                    else
                      vcPCTTemplateDetail.add("NA");
                  }

                  break;
                } // end of "if" block
              } // end of Inner for loop
              if (i == arrVecGraphName.length)
                Log.debugLog(className, "addRptGrpIdAndRptIdToVector", "", "", "Specified Vector not found in test run data. Vector name = " + arrTemp[idx]);
            } // end of Outer for loop
          } // end of "else if" block
        } // end of else block
        // If Both Group and Graph is scalar (Both vectors is not allowed)
        else
        {
          vcRptGrpId.add("" + groupId);
          vcRptId.add(arrRptInfo[1][j]);
          GraphUniqueKeyDTO graphUniqueKeyDTO = new GraphUniqueKeyDTO(groupId, Integer.parseInt(arrRptInfo[1][j].trim()), "NA");
          int graphDataIndex = graphNames.getGraphDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTO);
          vcGDIdx.add("" + graphDataIndex);

          addGraphInfoInDTOList(groupId, graphId, "NA", graphDataIndex);

          // In case of derived reports use report description as Report Name
          if (graphViewType.equals("Derived Graph"))
            vcRptName.add(reportDescription);
          else
          {
            if(graphNames.getGraphDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTO) != -1)
              vcRptName.add(graphNames.getGraphNameByGraphUniqueKeyDTO(graphUniqueKeyDTO, false));
            else
              vcRptName.add("NA");
          }

          vcPDFID.add("" + graphNames.getPDFIdByGraphUniqueKeyDTO(graphUniqueKeyDTO));
          vcPCTDataIdx.add("" + graphNames.getPDFDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTO));
          vcGraphViewType.add(graphViewType);

          if (arrRptInfo.length > 4)
            vcPCTTemplateDetail.add(arrRptInfo[4][j]);
          else
          {
            // In case of derived reports derived expression is store in template detail
            if (graphViewType.equals("Derived Graph"))
              vcPCTTemplateDetail.add(derivedExpression);
            else
              vcPCTTemplateDetail.add("NA");
          }
        }
      }
      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "addRptGrpIdAndRptIdToVector", "", "", "Exception in putting data to vector", e);
      return false;
    }
  }

  // --------------------------------------------------------------------------------------------------
  // Purpose: To get all report names and their index in graph data of one report in a template file
  // This is used for all Reports except Misc
  // Input :
  // Return : 2D Array with Report Names and Index in Graph Data
  // --------------------------------------------------------------------------------------------------
  public static String[][] getRptNameAndGraphDataIdxByRptInfo(String[][] arrRptInfo, GraphNames graphNames)
  {
    Log.debugLog(className, "getRptNameAndGraphDataIdxByRptInfo", "", "", "Method called.");

    Vector vcRptGrpId = new Vector();
    Vector vcRptId = new Vector();
    Vector vcGDIdx = new Vector();
    Vector vcRptName = new Vector();
    Vector vcPDFID = new Vector();
    Vector vcPCTDataIdx = new Vector();
    Vector vcPCTTemplateDetail = new Vector();
    Vector vcGraphViewType = new Vector();

    try
    {
      addRptGrpIdAndRptIdToVector(arrRptInfo, "", "", vcRptGrpId, vcRptId, vcGDIdx, vcRptName, graphNames, vcPDFID, vcPCTDataIdx, vcPCTTemplateDetail, vcGraphViewType, "");
      int[] rptGrpId = new int[vcRptGrpId.size()];
      int[] rptId = new int[vcRptId.size()];
      int[] graphDataIndx = new int[vcGDIdx.size()];
      String[] rptName = new String[vcRptName.size()];
      String[][] tempRptNameAndGDIdx = new String[2][vcRptName.size()];

      for (int ii = 0; ii < vcRptGrpId.size(); ii++)
      {
        rptGrpId[ii] = Integer.parseInt(vcRptGrpId.elementAt(ii).toString());
        rptId[ii] = Integer.parseInt(vcRptId.elementAt(ii).toString());

        graphDataIndx[ii] = Integer.parseInt(vcGDIdx.elementAt(ii).toString());
        rptName[ii] = vcRptName.elementAt(ii).toString();

        tempRptNameAndGDIdx[0][ii] = rptName[ii];
        tempRptNameAndGDIdx[1][ii] = "" + graphDataIndx[ii];

        Log.debugLog(className, "getRptNameAndGraphDataIdxByRptInfo", "", "", "RptGrpId = " + rptGrpId[ii] + ", RptId = " + rptId[ii] + ", Graph data Indx = " + graphDataIndx[ii] + ", Rpt Name = " + rptName[ii]);

      }

      return tempRptNameAndGDIdx;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getRptNameAndGraphDataIdxByRptInfo", "", "", "Exception in putting data to vector", e);
      return null;
    }
  }

  // --------------------------------------------------------------------------------------------------
  // Purpose: To get all report names and their PDF id and PCT data index in pct data of one report in a template file
  // This is used for all percentile Reports
  // Input :
  // Return : 2D Array with Report Names, PDF ID and PCT data Index
  // --------------------------------------------------------------------------------------------------
  public static String[][] getRptNamePDFIdPCTDataIdxAndGraphDataIdxByRptInfo(String[][] arrRptInfo, GraphNames graphNames)
  {
    Log.debugLog(className, "getRptNamePDFIdPCTDataIdxAndGraphDataIdxByRptInfo", "", "", "Method called.");

    Vector vcRptGrpId = new Vector();
    Vector vcRptId = new Vector();
    Vector vcGDIdx = new Vector();
    Vector vcRptName = new Vector();
    Vector vcPDFID = new Vector();
    Vector vcPCTDataIdx = new Vector();
    Vector vcPCTTemplateDetail = new Vector();
    Vector vcGraphViewType = new Vector();

    try
    {
      addRptGrpIdAndRptIdToVector(arrRptInfo, "", "", vcRptGrpId, vcRptId, vcGDIdx, vcRptName, graphNames, vcPDFID, vcPCTDataIdx, vcPCTTemplateDetail, vcGraphViewType, "");

      String[] rptName = new String[vcRptName.size()];
      int[] pdfId = new int[vcPDFID.size()];
      int[] pctGraphDataIdx = new int[vcPCTDataIdx.size()];
      String[] pctTemplateDetail = new String[vcPCTTemplateDetail.size()];
      int[] graphDataIdx = new int[vcGDIdx.size()];

      String[][] tempRptNamePDFIdAndPCTDataIdx = new String[5][vcRptName.size()];

      for (int ii = 0; ii < vcRptGrpId.size(); ii++)
      {
        rptName[ii] = vcRptName.elementAt(ii).toString();

        pdfId[ii] = Integer.parseInt(vcPDFID.elementAt(ii).toString());

        pctGraphDataIdx[ii] = Integer.parseInt(vcPCTDataIdx.elementAt(ii).toString());

        graphDataIdx[ii] = Integer.parseInt(vcGDIdx.elementAt(ii).toString());

        tempRptNamePDFIdAndPCTDataIdx[0][ii] = rptName[ii];
        tempRptNamePDFIdAndPCTDataIdx[1][ii] = "" + pdfId[ii];
        tempRptNamePDFIdAndPCTDataIdx[2][ii] = "" + pctGraphDataIdx[ii];
        tempRptNamePDFIdAndPCTDataIdx[4][ii] = "" + graphDataIdx[ii];

        Log.debugLog(className, "getRptNamePDFIdPCTDataIdxAndGraphDataIdxByRptInfo", "", "", "Rpt Name = " + rptName[ii] + ", PDF Id = " + pdfId[ii] + ", PCT Graph Data idx = " + pctGraphDataIdx[ii] + ", Graph Data idx = " + graphDataIdx[ii]);

        if (vcPCTTemplateDetail.size() > 0)
        {
          pctTemplateDetail[ii] = vcPCTTemplateDetail.elementAt(ii).toString();

          tempRptNamePDFIdAndPCTDataIdx[3][ii] = pctTemplateDetail[ii];

          Log.debugLog(className, "getRptNamePDFIdPCTDataIdxAndGraphDataIdxByRptInfo", "", "", "PCT Template Details = " + pctTemplateDetail[ii]);
        }
      }

      return tempRptNamePDFIdAndPCTDataIdx;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getRptNamePDFIdPCTDataIdxAndGraphDataIdxByRptInfo", "", "", "Exception in putting data to vector", e);
      return null;
    }
  }
}

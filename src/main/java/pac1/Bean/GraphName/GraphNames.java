/**
 * This class is for creating Graph Names
 * 
 * @author Ravi Sharma
 * @since Netsorm Version 3.9.2
 * @Modification_History Ravi Kant Sharma - Initial Version 3.9.2
 * @version 3.9.2
 * 
 */
package pac1.Bean.GraphName;

import java.util.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import pac1.Bean.GraphUniqueKeyDTO;
import pac1.Bean.Log;
import pac1.Bean.PDFNames;
import pac1.Bean.rptUtilsBean;
import pac1.Bean.CompareMemberDTO;


public class GraphNames implements Serializable
{
  private static String className = "GraphNames";
  private static final long serialVersionUID = 2557958793508976165L;
  private GraphNameProcessor graphNameProcessorObj = null;
  private int debugLevel = 0;

  public GraphNames()
  {
    Log.debugLogAlways(className, "GraphNames", "", "", "Default Constructor Called.");
  }

  public GraphNames(int numTestRun)
  {
    this(numTestRun, null, null, "NA", "", "", false);
  }
  
  public GraphNames(int numTestRun, Vector<String> gdfData, Vector<String> pdfData, String controllerTRNumber, String generatorName, String partitionDirName, boolean callFromMergeTestRun)
  {
    this(numTestRun, gdfData, pdfData, controllerTRNumber, generatorName, partitionDirName, "", callFromMergeTestRun);
  }

  public GraphNames(int numTestRun, Vector<String> gdfData, Vector<String> pdfData, String controllerTRNumber, String generatorName, String partitionDirName, String gdfFileVersion, boolean callFromMergeTestRun)
  {
    try
    {
      Log.debugLogAlways(className, "GraphNames", "", "", "Method Called. numTestRun = " + numTestRun + ", controllerTRNumber = " + controllerTRNumber + ", generatorName = " + generatorName + ", partitionDirName = " + partitionDirName);
      long startTime = System.currentTimeMillis();
      graphNameProcessorObj = new GraphNameProcessor(numTestRun, gdfData, pdfData, controllerTRNumber, generatorName, partitionDirName, gdfFileVersion, callFromMergeTestRun);
      long endTime = System.currentTimeMillis();
      String totalTimeTaken = "Total Time Taken In Creating Graph Names Object for Test Run " + numTestRun + " = " + (endTime - startTime) / 1000 + " Seconds";
      Log.debugLogAlways(className, "GraphNames", "", "", totalTimeTaken);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "GraphNames", "", "", "Exception - ", e);
    }
  }

  // This method returns the hierarchical component like Tier>Server>Instance by
  // passing Group Id
  public String getHirarchicalComponentByGroupId(int groupId)
  {
    return graphNameProcessorObj.getHirarchicalComponentArray(groupId);
  }

  public String getMetricsNameByGroupId(int groupId)
  {
    return graphNameProcessorObj.getMetricsNameByGroupId(groupId);
  }

  public String getGroupDescriptionByGroupId(int groupId)
  {
    return graphNameProcessorObj.getGroupDescriptionByGroupId(groupId);
  }

  // Return data packet header length
  public int getDataPktHeaderLen()
  {
    return graphNameProcessorObj.getHeaderLength();
  }

  // Remove Later
  public int getStartIndex()
  {
    return getDataPktHeaderLen();
  }

  // Return the total size of msg data
  public int getSizeOfMsgData()
  {
    return graphNameProcessorObj.getSizeOfMsgData();
  }

  // return the array of Graph formula
  public int[] getGraphFormula()
  {
    return graphNameProcessorObj.getArrGraphFormula();
  }

  // return the array of Min data Idx
  public int[] getMinDataIndx()
  {
    return graphNameProcessorObj.getArrMinDataIndx();
  }

  // return the array of graph data index
  public int[] getGraphDataIndx()
  {
    return graphNameProcessorObj.getGraphDataIndexArray();
  }

  public int[] getArrDataTypeIndx()
  {
    return graphNameProcessorObj.getArrDataTypeIndx();
  }

  // return the array of Max data Idx
  public int[] getMaxDataIndx()
  {
    return graphNameProcessorObj.getArrMaxDataIndx();
  }

  // return the array of Cur Count data Idx
  public int[] getCurCountIndx()
  {
    return graphNameProcessorObj.getArrCurCountIndx();
  }

  // return the array of data type num
  public int[] getDataTypeNumArray()
  {
    return graphNameProcessorObj.getDataTypeNumArray();
  }

  public int getDataTypeNumByGraphUniqueKeyDTO(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    return graphNameProcessorObj.getDataTypeNumByGraphUniqueKeyDTO(graphUniqueKeyDTO);
  }

  // return major version of GDF
  public int getMajorVersion()
  {
    return graphNameProcessorObj.getMajorVersionNum();
  }

  // return minor version of GDF
  public int getMinorVersion()
  {
    return graphNameProcessorObj.getMinorVersionNum();
  }

  // return data element size
  public int getDataElementSize()
  {
    return graphNameProcessorObj.getDataElementSize();
  }

  // Return the total number of Groups according to the number of vectors
  public int getTotalNumOfGroups()
  {
    return graphNameProcessorObj.getTotalNumOfGroups();
  }

  // Return the progress interval in test run
  public int getInterval()
  {
    return graphNameProcessorObj.getProgressInterval();
  }

  // Return the total number of Graphs according to the number of vectors
  public int getTotalNumOfGraphs()
  {
    return graphNameProcessorObj.getTotalNumOfGraphs();
  }

  // return TestRun number.
  public int getTestRun()
  {
    return graphNameProcessorObj.getTestRunNumber();
  }

  // return array of Group Ids
  public int[] getGroupIdArray()
  {
    return graphNameProcessorObj.getGroupIdArray();
  }

  // return array of pdf ids
  public int[] getPDFIdArray()
  {
    return graphNameProcessorObj.getPDFIdArray();
  }

  // Return array of PCT Data Index
  public long[] getPCTDataIndexArray()
  {
    return graphNameProcessorObj.getPCTDataIndexArray();
  }

  // Return array of Graph Names
  public String[] getArrayOfGraphNames(boolean withVector)
  {
    return graphNameProcessorObj.getArrayOfGraphNames(withVector);
  }

  // Return the all graphs by the group id
  public String[] getAllGraphNamesByGroupId(int groupId)
  {
    return graphNameProcessorObj.getAllGraphNamesByGroupId(groupId);
  }

  // Return the all graphs by the group id
  public String[] getAllGraphNamesWithVectorByGroupId(int groupId)
  {
    String[] arrGraphNames = graphNameProcessorObj.getAllGraphNamesByGroupId(groupId);
    String[] arrIndicesNames = graphNameProcessorObj.getAllGroupVectorNamesByGroupId(groupId);

    if (arrGraphNames != null && arrIndicesNames != null && arrIndicesNames.length == arrGraphNames.length)
    {
      String[] arrGraphNamesWithVectors = new String[arrGraphNames.length];
      for (int i = 0; i < arrIndicesNames.length; i++)
      {
        arrGraphNamesWithVectors[i] = arrGraphNames[i] + " - " + arrIndicesNames[i];
      }

      return arrGraphNamesWithVectors;
    }
    else
    {
      Log.errorLog(className, "getAllGraphNamesWithVectorByGroupId", "", "", "groupId  = " + groupId + " may be not present.");
      return null;
    }
  }

  public String getGroupNameByGroupId(int groupId)
  {
    return graphNameProcessorObj.getGroupNameByGroupId(groupId);
  }

  public boolean isGroupTypeVector(int groupId)
  {
    return graphNameProcessorObj.isGroupTypeVector(groupId);
  }

  // This will return report count by Rpt Grp Id
  public int getNumOfGraphsByGroupId(int groupId)
  {
    return graphNameProcessorObj.getNumOfGraphsByGroupId(groupId);
  }

  // Return only Group Indices
  public String[] getNameOfGroupIndicesByGroupId(int groupId)
  {
    return graphNameProcessorObj.getNameOfGroupIndicesByGroupId(groupId);
  }

  // Returns the Vector Names according to the Grp Id & Graph Id (Name of Vector Graphs).
  public String[] getNameOfIndicesByGroupIdAndGraphId(int groupId, int graphId)
  {
    return graphNameProcessorObj.getNameOfIndicesByGroupIdAndGraphId(groupId, graphId);
  }

  // Return the String like "Grp Type | numVecGrp | Graph Type | numVecGraph"
  public String getGroupTypeGraphTypeAndNumOfIndices(int groupId, int graphId)
  {
    return graphNameProcessorObj.getGroupTypeGraphTypeAndNumOfIndices(groupId, graphId);
  }

  // this function return the graphDataIdx by groupId, graphId & vector name
  public int getGraphDataIdxByGrpIdGraphIdVecName(int groupId, int graphId, String vectorName)
  {
    return graphNameProcessorObj.getGraphDataIdxByGrpIdGraphIdVecName(groupId, graphId, vectorName);
  }

  public int getGraphDataIndexByGraphUniqueKeyDTO(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    return graphNameProcessorObj.getGraphDataIdxByGrpIdGraphIdVecName(graphUniqueKeyDTO.getGroupId(), graphUniqueKeyDTO.getGraphId(), graphUniqueKeyDTO.getVectorName());
  }

  public String getReportNameForTemplateByGrpIdGraphIdVecName(int groupId, int graphId, String vecName)
  {
    return getReportNameForTemplateByGrpIdGraphIdVecName(groupId, graphId, vecName, false);
  }

  // this function return the reportname by rptGrpId, rptId & vector name
  // accoring to the template
  public String getReportNameForTemplateByGrpIdGraphIdVecName(int groupId, int graphId, String vectorName, boolean isCalledFromAnalysis)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "getReportNameForTemplateByGrpIdGraphIdVecName", "", "", "Method Called. groupId = " + groupId + ", graphId = " + graphId + ", vectorName = " + vectorName + ", isCalledFromAnalysis = " + isCalledFromAnalysis);

      String graphName = graphNameProcessorObj.getGraphNameByGroupIdGraphId(groupId, graphId);

      if (vectorName == null || vectorName.equals("NA"))
        return graphName;

      if (!isCalledFromAnalysis)
        graphName = graphName + " (" + vectorName + ")";
      else
        graphName = graphName + "[" + vectorName + "]";

      Log.debugLog(className, "getReportNameForTemplateByGrpIdGraphIdVecName", "", "", "Report name for template = " + graphName);

      return graphName;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getReportNameForTemplateByGrpIdGraphIdVecName", "", "", "Exception - ", e);
      return "";
    }
  }

  // Return The Graph data Index according to the Grp Id And Graph Id
  public int getGraphDataIdxByGroupIdAndGraphId(int groupId, int graphId)
  {
    if (debugLevel > 0)
      Log.debugLogAlways(className, "getGraphDataIdxByGroupIdAndGraphId", "", "", "Method Starts, groupId = " + groupId + ", Graph Id = " + graphId + ". Assuming Vect Name = NA");

    return getGraphDataIdxByGrpIdGraphIdVecName(groupId, graphId, "NA");
  }

  public GraphUniqueKeyDTO[] getGraphUniqueKeyDTO()
  {
    return graphNameProcessorObj.getGraphUniqueKeyDTO();
  }

  // To encrypt URL string.
  public String encriptGraphName(String changeValue)
  {
    for (int i = 0; i < GraphNameUtils.chrArr1.length; i++)
      changeValue = changeValue.replace(GraphNameUtils.chrArr1[i], GraphNameUtils.chrArr2[i]);
    return changeValue;
  }

  // To decrypt URL String.
  public String decriptGraphName(String changeValue)
  {
    for (int i = 0; i < GraphNameUtils.chrArr1.length; i++)
      changeValue = changeValue.replace(GraphNameUtils.chrArr2[i], GraphNameUtils.chrArr1[i]);
    return changeValue;
  }

  public int getGroupNumByGroupId(int groupID)
  {
    int tempIdx = 0;
    int[] arrGroupId = getGroupIdArray();
    for (int i = 0; i < arrGroupId.length; i++)
    {
      if (arrGroupId[i] == groupID)
      {
        tempIdx = i;
        break;
      }
    }
    return (tempIdx);
  }

  public int getMiscRptGrpId()
  {
    return (1001);
  }

  // for Hot Spot Group Id
  public int getHotspotGrpId()
  {
    return (10136);
  }

  // Return the the group Id by the grp Name
  public int getGroupIdByGroupName(String groupName)
  {
    int[] arrGroupId = getGroupIdArray();
    for (int i = 0; i < arrGroupId.length; i++)
    {
      int groupId = arrGroupId[i];
      String graphNameToMatch = graphNameProcessorObj.getGroupNameByGroupId(groupId);
      if (graphNameToMatch.equals(groupName))
        return groupId;
    }
    return -1;
  }

  public int ordinalIndexOf(String str, String searchStr, int ordinal, boolean lastIndex)
  {
    if (str == null || searchStr == null || ordinal <= 0)
      return -1;

    if (searchStr.length() == 0)
      return lastIndex ? str.length() : 0;

    int found = 0;
    int index = lastIndex ? str.length() : -1;
    do
    {
      if (lastIndex)
        index = str.lastIndexOf(searchStr, index);
      else
        index = str.indexOf(searchStr, index + 1);

      if (index < 0)
        return index;

      found++;
    }
    while (found < ordinal);

    return index;
  }

  // returns info line of gdf
  public String getGDFInfoLine()
  {
    return graphNameProcessorObj.getGDFInfoLine();
  }

  public String getTestRunStartDateTime()
  {
    return graphNameProcessorObj.getTestRunStartDateTime();
  }

  public ArrayList<Integer> getUniqueGroupIds()
  {
    return graphNameProcessorObj.getUniqueGroupIds();
  }

  public ArrayList getInfoAboutGraph()
  {
    return graphNameProcessorObj.getInfoAboutGraph();
  }

  public PDFNames getPdfNames()
  {
    return graphNameProcessorObj.getPdfNames();
  }

  public Vector<String> getGdfData()
  {
    return graphNameProcessorObj.getGdfData();
  }

  public Vector<String> getPdfData()
  {
    return graphNameProcessorObj.getPdfData();
  }

  // Returns the error string in PDF Validation (if any)
  public String validatePDFByGrpIdAndGraphId(String[] groupIdArray, String[] graphIdArray)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "validatePDFByGrpIdAndGraphId", "", "", "Method Starts, GrpId = " + rptUtilsBean.strArrayToStr(groupIdArray, ", ") + ", Graph Id = " + rptUtilsBean.strArrayToStr(graphIdArray, ", "));

      String reportName = "";
      String strGrpName = "";
      String strGraphName = "";

      int groupId = -1;
      int graphId = -1;

      String errorString = "";

      GraphNames allGraphNameObj = null;
      for (int jj = 0; jj < groupIdArray.length; jj++)
      {
        groupId = Integer.parseInt(groupIdArray[jj]);
        graphId = Integer.parseInt(graphIdArray[jj]);

        int pdfId = graphNameProcessorObj.getPDFIdByGroupIdAndGraphId(groupId, graphId);
        if (pdfId != -2)
        {
          if (pdfId == -1)
          {
            if (reportName.equals(""))
              reportName = reportName + graphNameProcessorObj.getGraphNameByGroupIdGraphId(groupId, graphId);
            else
              reportName = reportName + ",\n" + graphNameProcessorObj.getGraphNameByGroupIdGraphId(groupId, graphId);
          }
        }
        else
        {
          if (allGraphNameObj == null)
            allGraphNameObj = new GraphNames(-1);

          String tempGraphName = allGraphNameObj.graphNameProcessorObj.getGraphNameByGroupIdGraphId(groupId, graphId);

          if (strGraphName.equals(""))
            strGraphName = strGraphName + tempGraphName;
          else
            strGraphName = strGraphName + ",\n" + tempGraphName;
        }
      }

      if (!strGrpName.equals(""))
      {
        errorString = errorString + "Group(s) not present in the Test Run. Group(s) Names - \n" + strGrpName;
        Log.debugLog(className, "validatePDFByGrpIdAndGraphId", "", "", errorString);
      }

      else if (!strGraphName.equals(""))
      {
        errorString = errorString + "Graph(s) not present in the Test Run. Graph(s) Names - \n" + strGraphName;
        Log.debugLog(className, "validatePDFByGrpIdAndGraphId", "", "", errorString);
      }

      else if (!reportName.equals(""))
      {
        errorString = errorString + "Percentile definition file(PDF) not associated with following graph(s):\n" + reportName;
        Log.debugLog(className, "validatePDFByGrpIdAndGraphId", "", "", errorString);
      }

      return errorString;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "validatePDFByGrpIdAndGraphId", "", "", "Exception - ", e);
      return "";
    }
  }

  public int getDataTypeSize(String dataTypeName)
  {
    return graphNameProcessorObj.getDataTypeSize(dataTypeName);
  }

  public String getGraphNameByGraphUniqueKeyDTO(GraphUniqueKeyDTO graphUniqueKeyDTO, boolean withVector)
  {
    String graphName = graphNameProcessorObj.getGraphNameByGraphUniqueKeyDTO(graphUniqueKeyDTO);
    if (graphUniqueKeyDTO == null)
    {
      Log.errorLog(className, "getGraphNameByGraphUniqueKeyDTO", "", "", "graphUniqueKeyDTO is null.");
      return graphName;
    }

    String vectorName = graphUniqueKeyDTO.getVectorName();
    if (withVector && vectorName != null && !vectorName.equals("") && !vectorName.equals("NA"))
      graphName = graphName + " - " + vectorName;

    return graphName;
  }

  public String getGroupNameByGraphUniqueKeyDTO(GraphUniqueKeyDTO graphUniqueKeyDTO, boolean withVector)
  {
    try
    {
      String groupName = getGroupNameByGroupId(graphUniqueKeyDTO.getGroupId());
      String vectorName = graphUniqueKeyDTO.getVectorName();
      if (withVector && vectorName != null && !vectorName.equals("") && !vectorName.equals("NA"))
        groupName = groupName + " - " + vectorName;

      return groupName;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getGroupNameByGraphUniqueKeyDTO", "", "", "graphUniqueKeyDTO = " + graphUniqueKeyDTO + ", Exception - ", e);
      return "";
    }
  }

  public int getPDFIdByGraphUniqueKeyDTO(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    return graphNameProcessorObj.getPDFIdByGraphUniqueKeyDTO(graphUniqueKeyDTO);
  }

  public long getPDFDataIndexByGraphUniqueKeyDTO(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    return graphNameProcessorObj.getPDFDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTO);
  }

  // Ravi Need To Remove this function
  public GraphUniqueKeyDTO getGraphUniqueKeyDTOByGraphDataIndex(int graphDataIndex)
  {
    return graphNameProcessorObj.getGraphUniqueKeyDTOByGraphDataIndex(graphDataIndex);
  }

  public HashMap<Integer, GroupInfoBean> getHmGroupInfoBean()
  {
    return graphNameProcessorObj.getHmGroupInfoBean();
  }

  public HashMap<GraphUniqueKeyDTO, GraphInfoBean> getHmGraphInfoBean()
  {
    return graphNameProcessorObj.getHmGraphInfoBean();
  }

  public GraphInfoBean getGraphInfoBeanByGraphUniqueKeyDTO(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    return graphNameProcessorObj.getGraphInfoBeanByGraphUniqueKeyDTO(graphUniqueKeyDTO);
  }

  public int getIndexOfGraphUniqueKeyDTO(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    return graphNameProcessorObj.getIndexOfGraphUniqueKeyDTO(graphUniqueKeyDTO);
  }

  public String getGraphDescriptionByGraphUniqueKeyDTO(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    return graphNameProcessorObj.getGraphDescriptionByGraphUniqueKeyDTO(graphUniqueKeyDTO);
  }

  public boolean isGroupIdExist(int groupId)
  {
    return getHmGroupInfoBean().containsKey(groupId);
  }
  
//This method is used to finding out the open member for same component graphs based on the level on selected menu.
  public GraphUniqueKeyDTO[] getGraphNumArrSameInstance(GraphUniqueKeyDTO graphUniqueKeyDTO, String sameElement, String vectorDetial, int sameComponentLevel, int vectorComponentLevel)
  {
    try
    {
      if(debugLevel > 0)
        Log.debugLogAlways(className, "getGraphNumArrSameInstance", "", "", "Method Called.graphNum  graphUniqueKeyDTO = " + graphUniqueKeyDTO + "sameElement = " + sameElement);

      //In case of All tier
      String[] elementArr = sameElement.split(",");
      String sameElementKey = elementArr[0];
      String sameElementValue = elementArr[1];

      String[] sameElementValueArr = null;
      if(sameElementValue.contains(">")) // In case infoline is less than Vector Components  
        sameElementValueArr = sameElementValue.split(">");

      //This array will be used Other component of same tier otherwise will be blank
      String[] vectorArr = null;
      String vectorKey = "";
      String vectorValue = "";
      if(!vectorDetial.equals(""))
      {
        vectorArr = vectorDetial.split(",");
        vectorKey = vectorArr[0];
        vectorValue = vectorArr[1];
      }
      String graphName = getGraphNameByGraphUniqueKeyDTO(graphUniqueKeyDTO, false);
      GraphUniqueKeyDTO[] arrGraphUniqueKeyDTO = getGraphUniqueKeyDTO();
      int totalGraphs = arrGraphUniqueKeyDTO.length;
      
      ArrayList<GraphUniqueKeyDTO> arrFinalGraphUniqueKeyDTO = new ArrayList<GraphUniqueKeyDTO>();
      vectorComponentLevel = vectorComponentLevel - 1;// Finding out the indexing of vector component/ hierarchical component
      for(int k = 0; k < totalGraphs; k++)
      {
        int groupId = arrGraphUniqueKeyDTO[k].getGroupId();
        boolean isGroupTypeVector = isGroupTypeVector(groupId);

        String tmpHierarComponentName = getHirarchicalComponentByGroupId(groupId);
        String[] tmpHierarComponentNameArr = tmpHierarComponentName.split(">");

        String tmpVectorName = arrGraphUniqueKeyDTO[k].getVectorName();
        String[] tmpVectorNameArr = tmpVectorName.split(">");
        String tmpGraphName = getGraphNameByGraphUniqueKeyDTO(arrGraphUniqueKeyDTO[k], false);

        if((groupId < 10000) || !isGroupTypeVector)
          continue;

        // This condition will execute only for other components of same tier
        if(!"".equals(vectorDetial))
        {
          if(tmpHierarComponentNameArr.length > vectorComponentLevel && !tmpHierarComponentNameArr[vectorComponentLevel].trim().equals(vectorKey))
            continue;

          if(tmpVectorNameArr.length > vectorComponentLevel && !tmpVectorNameArr[vectorComponentLevel].trim().equals(vectorValue))
            continue;
        }
        // This code is execute Same Instance of All Tier
        if(tmpHierarComponentNameArr.length > sameComponentLevel && tmpHierarComponentNameArr[sameComponentLevel].trim().equals(sameElementKey))
        {
          if(sameElementValueArr == null && tmpVectorNameArr.length > sameComponentLevel && tmpVectorNameArr[sameComponentLevel].trim().equals(sameElementValue))
          {
            if(tmpGraphName.trim().equals(graphName))
            {
              if(!arrFinalGraphUniqueKeyDTO.contains(arrGraphUniqueKeyDTO[k]))
                arrFinalGraphUniqueKeyDTO.add(arrGraphUniqueKeyDTO[k]);
            }
          }
          //if infoline components are less than to vector components
          else if(sameElementValueArr != null && sameElementValueArr.length > 0 && tmpVectorNameArr.length > sameComponentLevel && tmpVectorNameArr.length == sameElementValueArr.length + sameComponentLevel)
          {
            String tmpStr = "";
            for(int i = 0; i < sameElementValueArr.length; i++)
            {
              tmpStr += ">" + tmpVectorNameArr[sameComponentLevel + i];

            }
            tmpStr = tmpStr.substring(1, tmpStr.length());
            if(tmpStr.equals(sameElementValue))
            {
              if(tmpGraphName.trim().equals(graphName))
              {
                if(!arrFinalGraphUniqueKeyDTO.contains(arrGraphUniqueKeyDTO[k]))
                  arrFinalGraphUniqueKeyDTO.add(arrGraphUniqueKeyDTO[k]);
              }
            }

          }
        }
      }
      GraphUniqueKeyDTO[] arrOfIndicsToReturn = arrFinalGraphUniqueKeyDTO.toArray(new GraphUniqueKeyDTO[arrFinalGraphUniqueKeyDTO.size()]);
      return arrOfIndicsToReturn;
    }

    catch(Exception e)
    {
      Log.stackTraceLog(className, "getGraphNumArrSameInstance", "", "", "Exception - ", e);
      return null;
    }
  }
  

  /**
   * This Method returns the Graphs at particular Hierarchical level.
   * @param graphNum
   * @param level
   * @param vectorSeparator
   * @return
   */
  public GraphUniqueKeyDTO[] getGraphUniqueKeyDTOArrByHirarchicalLevel(int groupId, String graphVectorName, String graphName, int level, String vectorSeparator)
  {
    try
    {
      Log.debugLog(className, "getGraphNumArrForGraphOfAllVectroGp", "", "", "Method Called.graphVectorName = " + graphVectorName + " level = " + level + " vectorSeparator = " + vectorSeparator);

      if (debugLevel > 0)
        Log.debugLogAlways(className, "getGraphNumArrForGraphOfAllVectroGp", "", "", "graphVector = " + graphVectorName + " groupID = " + groupId);

      // here we need to check for Tier/Server/App .. Graphs by level.
      // level 0 - Normal, Only check graph Name in each vector(No Comparison with vector name.)
      // level 1 - Check with First level i.e check till first occurrence of vectorSeparator in vector name. and so on ..
      // Getting the index by level.
      int ordinalIndex = ordinalIndexOf(graphVectorName, vectorSeparator, level, false);
      
      //Getting name to compare with vectors by level index.
      if(ordinalIndex != -1)
        graphVectorName = graphVectorName.substring(0, ordinalIndex + 1);
      
      if(debugLevel > 0)
        Log.debugLogAlways(className, "getGraphNumArrForGraphOfAllVectroGp", "", "", "groupID = " + groupId + " Search vector String = " + graphVectorName);

      TreeMap<String, GraphUniqueKeyDTO> tMap = new TreeMap<String, GraphUniqueKeyDTO>(new Comparator<String>()
      {
        public int compare(String o1, String o2)
        {
          return o1.compareToIgnoreCase(o2);
        }
      });

      if (debugLevel > 0)
        Log.debugLogAlways(className, "getGraphNumArrForGraphOfAllVectroGp", "", "", "graph Name  = " + graphName);

      GroupInfoBean groupInfoBean = graphNameProcessorObj.getHmGroupInfoBean().get(groupId);
      String[] arrNameOfGroupIndices = groupInfoBean.getIndicesNamesArray();
      int NumOfGroupIndices = arrNameOfGroupIndices.length;
      
      for(int i = 0; i < NumOfGroupIndices; i++)
      {	
	//System.out.println("arrNameOfGroupIndices[i] = " + arrNameOfGroupIndices[i]);
        //Here checking vector for Getting right Graph.
        if(level > 0 && !arrNameOfGroupIndices[i].trim().startsWith(graphVectorName))
          continue;

        int[] graphIds = groupInfoBean.getArrGraphIds();
        for(int k = 0; k < graphIds.length; k++)
        {
          String graphNString = graphNameProcessorObj.getGraphNameByGraphUniqueKeyDTO(new GraphUniqueKeyDTO(groupId, graphIds[k], arrNameOfGroupIndices[i]));
          //System.out.println("curGraphName = " + graphNString + ", graphName = "+graphName);
          if(graphNString.equals(graphName))
          {
            graphNString += " - " + arrNameOfGroupIndices[i];
            GraphUniqueKeyDTO graphUniqueKeyDTO = new GraphUniqueKeyDTO(groupId, graphIds[k], arrNameOfGroupIndices[i]);
            tMap.put(graphNString, graphUniqueKeyDTO);
          }
        }
      }

      if(debugLevel > 0)
        Log.debugLogAlways(className, "getGraphNumArrForGraphOfAllVectroGp", "", "", "Total Number of Graph Index = " + tMap.size());

      GraphUniqueKeyDTO[] arrGraphUniqueKeyDTO = new GraphUniqueKeyDTO[tMap.size()];
      Iterator it = tMap.keySet().iterator();
      int i = 0;
      while (it.hasNext())
      {
        String key = it.next().toString();
        arrGraphUniqueKeyDTO[i] = tMap.get(key);
        i++;
      }

      return arrGraphUniqueKeyDTO;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getGraphNumArrForGraphOfAllVectroGp", "", "", "Exception - ", e);
      return null;
    }
  }

  public GraphUniqueKeyDTO getGraphUniqueKeyDTOByGraphName(String graphNameWithoutVector)
  {
    HashMap<Integer, GroupInfoBean> hmGroupInfoBean = getHmGroupInfoBean();
    Set<Integer> keySet = hmGroupInfoBean.keySet();
    Iterator<Integer> itr = keySet.iterator();
    while (itr.hasNext())
    {
      int groupId = itr.next();
      GroupInfoBean groupInfo = hmGroupInfoBean.get(groupId);
      String[] arrGraphNames = groupInfo.getArrGraphNames();
      int[] arrGraphIds = groupInfo.getArrGraphIds();
      String[] arrVectorNames = groupInfo.getIndicesNamesArray();
      for (int i = 0; i < arrGraphNames.length; i++)
      {
        if (arrGraphNames[i].equals(graphNameWithoutVector))
        {
          GraphUniqueKeyDTO graphUniqueKeyDTO = new GraphUniqueKeyDTO(groupId, arrGraphIds[i], arrVectorNames[i]);
          return graphUniqueKeyDTO;
        }
      }
    }

    return null;
  }

  /******************* Below Methods are Strictly used by Analysis GUI. These may be removed in Future ***********************/

  // this function return the graph number by groupId, graphId & vector name
  public int getGraphNumberByGrpIdGraphIdVecName(int groupId, int graphId, String vectorName)
  {
    try
    {
      GraphUniqueKeyDTO graphUniqueKeyDTO = new GraphUniqueKeyDTO(groupId, graphId, vectorName);
      return getIndexOfGraphUniqueKeyDTO(graphUniqueKeyDTO);
    }
    catch (Exception e)
    {
      return -1;
    }
  }

  public String getGroupNameWithVectorByGroupNum(int groupNum)
  {
    return "";
  }

  // Return Group Id By Passing Group Number
  public int getGroupIdByGroupNum(int groupNum)
  {
    return -1;
  }

  public String getGraphCaption(int graphNum, boolean withVectorName)
  {
    return "";
  }

  // return array of graph ids
  public int[] getGraphIdArray()
  {
    return null;
  }

  // Return array of Graph Names
  public String[] getArrayOfGraphNames()
  {
    return null;
  }

  public int getGraphCountByGroupNum(int groupNum)
  {
    return -1;
  }

  // Return Group Name
  public String getGroupNameByGraphNum(int graphNum)
  {
    return "";
  }

  // Return the total graph count according to the grp id and graph id
  public int getAllGraphCount(int groupId, int graphId)
  {
    return -1;
  }

  // Returns PCT Graph Data Index on Graph number.
  public long getPCTGraphDataIndex(int graphNum)
  {
    return 0L;
  }

  // this function return the total graph count before that group
  public int getAllGraphCountByGrpId(int groupId)
  {
    return -1;
  }

  // Return Group Name By Passing Group Number
  public String getGroupNameByGroupNum(int groupNum)
  {
    return "";
  }

  public String getGroupNameGroupId(int groupId)
  {
    return "";
  }

  // return graph number array by group num
  public int[] getGraphNumberArrayByGroupNum(int groupNum)
  {
    return null;
  }

  // Returns Graph Data Index on Graph number.
  public int getGraphDataIndexByGraphNum(int graphNum)
  {
    return -1;
  }

  public String getGraphDescriptionByGraphNum(int graphNumber)
  {
    try
    {
      GraphUniqueKeyDTO graphUniqueKeyDTO = getGraphUniqueKeyDTO()[graphNumber];
      return getGraphDescriptionByGraphUniqueKeyDTO(graphUniqueKeyDTO);
    }
    catch (Exception e)
    {
      return GraphNameUtils.DEFAULT_GRAPH_DESCRIPTION;
    }
  }

  // return all data type of that graph by graphDataIndex.
  public int getGraphDataTypeByIndex(int graphDataIndex)
  {
    return -1;
  }

  // Returns Graph Number on Graph Data Index.
  public int getGraphNumberByGraphDataIndex(int graphDataIndex)
  {
    return -1;
  }

  public String getGraphNameWithVectorByGraphNum(int graphNum)
  {
    return "";
  }

  
  /*This method is used to finding out the related graphs based on the Level on selected menu.
  Finding graphs will be existing in same group and different group which matches the level.*/
  
  public GraphUniqueKeyDTO[] getGraphNumArrRelatedGraphs(String matchVector, GraphUniqueKeyDTO[] arrSelectedGraphDTO)
  {
    try
    {
      if(debugLevel > 0)
        Log.debugLogAlways(className, "getGraphNumArrRelatedGraphs", "", "", "Method Called.graphNum  matchVector = " + matchVector);

      GraphUniqueKeyDTO[] arrGraphUniqueKeyDTO = null;
      boolean graphNumArrNullFlg = false; // Use for related graph num array
      
      if(arrSelectedGraphDTO == null)
      {
        arrGraphUniqueKeyDTO = getGraphUniqueKeyDTO();// From the Related window selected Radio button is "ALL"
      }
      else
      {
        arrGraphUniqueKeyDTO = arrSelectedGraphDTO; // From the Related window selected Radio button is "Select Collection/ Selected Graphs"
        graphNumArrNullFlg = true;
      }

      ArrayList<GraphUniqueKeyDTO> arrFinalGraphUniqueKeyDTO = new ArrayList<GraphUniqueKeyDTO>();
      for(int k = 0; k < arrGraphUniqueKeyDTO.length; k++)
      {
        GraphUniqueKeyDTO graphUniqueKeyDTO = arrGraphUniqueKeyDTO[k];

        
        int groupId = graphUniqueKeyDTO.getGroupId();
        boolean isVector = isGroupTypeVector(groupId);
        String tmpVectorName = graphUniqueKeyDTO.getVectorName();

        if((groupId < 10000) || !isVector)
          continue;

        if(tmpVectorName.startsWith(matchVector + ">") || tmpVectorName.equals(matchVector))
        {
          if(!arrFinalGraphUniqueKeyDTO.contains(graphUniqueKeyDTO))
            arrFinalGraphUniqueKeyDTO.add(graphUniqueKeyDTO);
        }
      }

      GraphUniqueKeyDTO[] arrOfIndicsToReturn = arrFinalGraphUniqueKeyDTO.toArray(new GraphUniqueKeyDTO[arrFinalGraphUniqueKeyDTO.size()]);
      return arrOfIndicsToReturn;
    }

    catch(Exception e)
    {
      Log.stackTraceLog(className, "getGraphNumArrRelatedGraphs", "", "", "Exception - ", e);
      return null;
    }
}
 

public CompareMemberDTO getGraphNumArrCompareGroup(GraphUniqueKeyDTO graphUniqueKeyDTO, CompareMemberDTO compareMemberObj, boolean isLeafNode)
  {
    try
    {
      if(debugLevel > 0)
        Log.debugLogAlways(className, "getGraphNumArrCompareGroup", "", "", "Method Called.graphNum  graphUniqueKeyDTO = " + graphUniqueKeyDTO);
      HashMap<Integer, GroupInfoBean> hm = getHmGroupInfoBean();
      GroupInfoBean groupBean = hm.get(graphUniqueKeyDTO.getGroupId());
      int groupId = graphUniqueKeyDTO.getGroupId();
      String[] vectorArr = groupBean.getIndicesNamesArray();
      //int[] graphIds = groupBean.getArrGraphIds();
      int[] graphIds = null;
      if(!isLeafNode)
        graphIds = groupBean.getArrGraphIds();
      else
      {
        graphIds = new int[1];
        graphIds[0] = graphUniqueKeyDTO.getGraphId();
      }
      String[] op = compareMemberObj.getOperatorName();
      String[] opValue = compareMemberObj.getOperatorValue();
      String compareGraphArr[] = new String[opValue.length];

      for(int i = 0; i < op.length; i++)
      {
        if(!"Same".equals(op[i]))
          compareGraphArr[op.length - (i + 1)] = "NA";
        else
          compareGraphArr[op.length - (i + 1)] = opValue[i];
      }

      /*Applying Filters*/
      ArrayList<String> filteredVector = new ArrayList<String>();
      for(int i = 0; i < vectorArr.length; i++)
      {
        String vectorString = vectorArr[i];
        String[] tmpVectorNameArr = vectorString.split(">");
        boolean filterFlag = false;

        for(int j = 0; j < tmpVectorNameArr.length; j++)
        {
          if(compareGraphArr[j].equals("NA"))
          {
            filterFlag = true;
            continue;
          }
          else
          {
            if(!compareGraphArr[j].equals(tmpVectorNameArr[j]))
            {
              filterFlag = false;
              break;
            }
            else
            {
              filterFlag = true;
              continue;
            }
          }
        }

        if(filterFlag)
          filteredVector.add(vectorArr[i]);
      }

      ArrayList<GraphUniqueKeyDTO> listForGraphsOnPanel = new ArrayList<GraphUniqueKeyDTO>();
      ArrayList<ArrayList<GraphUniqueKeyDTO>> listForPanels = new ArrayList<ArrayList<GraphUniqueKeyDTO>>();
      if (debugLevel > 1)
      Log.debugLogAlways(className, "getGraphNumArrCompareGroup", "", "", "Method Called. Filter Vectors Are :"+filteredVector);
      Collections.sort(filteredVector);
      int level = 0;
      boolean isSPUsed = false;

      for(int len = 0; len < op.length; len++)
      {
        if(op[len].trim().equals("All(Separate Panel)"))
        {
          isSPUsed = true;
          level = len;
          break;
        }
      }

      if(isSPUsed)
        level = op.length - level - 1;

      //System.out.println("is Separate Panel Used = " + isSPUsed + ", level = " + level);

      for(int i = 0; i < graphIds.length; i++)
      {
        for(int j = 0; j < filteredVector.size(); j++)
        {
          if(j == 0)
          {
            if(listForGraphsOnPanel.size() > 0)
            {
              if(!listForPanels.contains(listForGraphsOnPanel))
                listForPanels.add(listForGraphsOnPanel);
            }

            String vectorName = filteredVector.get(j);
            GraphUniqueKeyDTO tmpGraphUniqueKeyDTO = new GraphUniqueKeyDTO(groupId, graphIds[i], vectorName);

            listForGraphsOnPanel = new ArrayList<GraphUniqueKeyDTO>();
            listForGraphsOnPanel.add(tmpGraphUniqueKeyDTO);
            listForPanels.add(listForGraphsOnPanel);
          }
          else
          {
            String currVectorName = filteredVector.get(j);

            GraphUniqueKeyDTO tmpGraphUniqueKeyDTO = new GraphUniqueKeyDTO(groupId, graphIds[i], currVectorName);

            if(isSPUsed)
            {
              String preVectorName = filteredVector.get(j - 1);
              String[] preVectorNameArr = preVectorName.split(">");
              String[] currVectorNameArr = currVectorName.split(">");

              if(currVectorNameArr[level].equals(preVectorNameArr[level]))
              {
                if(level != 0 && !currVectorNameArr[level - 1].equals(preVectorNameArr[level - 1]))
                {
                  listForGraphsOnPanel = new ArrayList<GraphUniqueKeyDTO>();
                  listForGraphsOnPanel.add(tmpGraphUniqueKeyDTO);
                  listForPanels.add(listForGraphsOnPanel);
                }
                else
                  listForGraphsOnPanel.add(tmpGraphUniqueKeyDTO);
              }
              else
              {
                listForGraphsOnPanel = new ArrayList<GraphUniqueKeyDTO>();
                listForGraphsOnPanel.add(tmpGraphUniqueKeyDTO);
                listForPanels.add(listForGraphsOnPanel);
              }
            }
            else
              listForGraphsOnPanel.add(tmpGraphUniqueKeyDTO);
          }
        }
      }
      compareMemberObj.setListOfGraphPanels(listForPanels);
      return compareMemberObj;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "getGraphNumArrCompareGroup", "", "", "Exception - ", e);
      return null;
    }
  }


 
  /*************************************************************************************************************************/

  public static void main(String[] args)
  {
    int numTestRun = 1601;
    GraphUniqueKeyDTO graphUniqueKeyDTO = new GraphUniqueKeyDTO(102, 1, "hpd_page1_trans1");

    GraphNames graphNames = new GraphNames(numTestRun, null, null, "", "", "", "", false);
    System.out.println(graphNames.getGraphUniqueKeyDTO().length);
    System.out.println("pdfId = " + graphNames.getPDFIdByGraphUniqueKeyDTO(graphUniqueKeyDTO));
    System.out.println("PdfDataIndex = " + graphNames.getPDFDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTO));
  }
}

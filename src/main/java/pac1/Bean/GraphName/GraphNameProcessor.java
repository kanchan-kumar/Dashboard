package pac1.Bean.GraphName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Vector;

import pac1.Bean.GDFInfoBean;
import pac1.Bean.GraphUniqueKeyDTO;
import pac1.Bean.Log;
import pac1.Bean.PDFNames;
import pac1.Bean.rptUtilsBean;

public class GraphNameProcessor implements Serializable
{
  private static final long serialVersionUID = 262369203839718890L;
  private static String className = "GraphNameProcessor";

  // All Unique Group Ids that is used in Merge Test Runs
  private ArrayList<Integer> uniqueGroupIds = new ArrayList<Integer>();

  private transient ArrayList<String> graphNamesWithOutVector = new ArrayList<String>();
  private String[] arrGraphNamesWithoutVector = null;

  private transient ArrayList<String> graphNamesWithVector = new ArrayList<String>();
  private String[] arrGraphNamesWithVector = null;

  private LinkedHashMap<Integer, GroupInfoBean> hmGroupInfoBean = new LinkedHashMap<Integer, GroupInfoBean>();
  private LinkedHashMap<GraphUniqueKeyDTO, GraphInfoBean> hmGraphInfoBean = new LinkedHashMap<GraphUniqueKeyDTO, GraphInfoBean>();
  private LinkedHashMap<Integer, HashMap<Integer, GraphNameCommonInfo>> hmGraphVectorNames = new LinkedHashMap<Integer, HashMap<Integer, GraphNameCommonInfo>>();

  private String strInfoLine = "";
  private int numTestRun = -1;
  private int graphCounter = 0;
  private int groupCount = 0;
  private int[] arrGraphDataIndx = null;
  private int[] arrDataTypeIndx = null;

  private int[] arrGraphFormula = null;//
  private int[] arrMaxDataIndx = null;
  private int[] arrCurCountIndx = null;
  private int[] dataTypeNumArray = null;

  // Storing PDF Ids
  private int[] arrPDFID = null;

  // Storing pct Data index array
  private long[] arrPctDataIndx = null;

  private GraphUniqueKeyDTO[] arrGraphUniqueKeyDTO = null;
  
  /*For Mapping the actual array index with sorted graph dto index.*/
  private int []arrMappingGraphIndex = null;

  private transient int debugLevel = 0;
  private transient String controllerTRNumber = "NA";
  private transient String generatorName = "";
  private transient String partitionDirName = "";
  private String GDFVersion = "";
  // It is true for Report Template
  private boolean forTemplate = false;

  private Vector<String> pdfData = new Vector<String>();
  private Vector<String> gdfData = new Vector<String>();

  // dont make it Transient
  private ArrayList<Object> arrInfoAbtGraph = new ArrayList<Object>();

  private transient Vector<Integer> vectorOfPDFId = new Vector<Integer>();
  private transient Vector<Long> vectorOfPCTDataIndex = new Vector<Long>();

  private transient Vector<String> vectorOfGraphFormula = new Vector<String>();

  // Storing Graph Data Type
  private transient Vector<String> vectorOfGraphDataType = new Vector<String>();

  // Storing Graph Data Index
  private transient Vector<Integer> vectorOfGraphDataIndx = new Vector<Integer>();

  public int getTotalNumOfGroups()
  {
    gdfInfoBeanObj.setTotalGroups(groupCount);
    return gdfInfoBeanObj.getTotalGroups();
  }

  public int getProgressInterval()
  {
    return gdfInfoBeanObj.getProgressInterval();
  }

  private GDFInfoBean gdfInfoBeanObj = null;

  public int getMajorVersionNum()
  {
    return gdfInfoBeanObj.getMajorVersionNum();
  }

  public int getMinorVersionNum()
  {
    return gdfInfoBeanObj.getMinorVersionNum();
  }

  public int getDataElementSize()
  {
    return gdfInfoBeanObj.getDataElementSize();
  }

  private PDFNames pdfNames = null;
  private transient int totalLinesInGDF = 0;

  private transient int[] arrMinDataIndx = null;

  public void setDebugLevel(int debugLevel)
  {
    this.debugLevel = debugLevel;
  }

  public GraphNameProcessor(int numTestRun, Vector<String> gdfData, Vector<String> pdfData, String controllerTRNumber, String generatorName, String partitionDirName, String GDFVersion, boolean callFromMergeTestRun)
  {
    try
    {
      Log.debugLogAlways(className, "GraphNameProcessor", "", "", "Method Called With gdf data and numTestRun = " + numTestRun + ", controllerTRNumber = " + controllerTRNumber + ", generatorName = " + generatorName + ", partitionDirName = " + partitionDirName + ", callFromMergeTestRun = " + callFromMergeTestRun + ", GDFVersion = " + GDFVersion);

      this.partitionDirName = partitionDirName;
      this.generatorName = generatorName;
      this.controllerTRNumber = controllerTRNumber;
      if(controllerTRNumber == null)
      {
        Log.errorLog(className, "GraphNameProcessor", "", "", "controllerTRNumber must not be null so assigning NA.");
        this.controllerTRNumber = "NA";
      }
      
      this.numTestRun = numTestRun;
      this.gdfData = gdfData;
      this.pdfData = pdfData;
      this.GDFVersion = GDFVersion;

      gdfInfoBeanObj = new GDFInfoBean();

      if (numTestRun == -1)
        forTemplate = true;

      boolean initGraphNamesFlag = initGraphNames();
      if (initGraphNamesFlag)
      {
        initArrayGraphNames();
      }
      else
      {
        Log.errorLog(className, "GraphNameProcessor", "", "", "Bug: cannot create graph number arrays due initialization failed. numTestRun = " + numTestRun);
      }

      if (callFromMergeTestRun)
        generateInfoAboutGraph();
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "GraphNameProcessor", "", "", "Exception - ", e);
    }
  }

  private void generateInfoAboutGraph()
  {
    try
    {
      MergeTestRunGraphInfo mergeTestRunGraphInfo = new MergeTestRunGraphInfo();
      arrInfoAbtGraph = mergeTestRunGraphInfo.generateInfoAboutGraph(gdfData, this);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "generateInfoAboutGraph", "", "", "Exception - ", e);
    }
  }

  private void initPDFNames()
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "initPDFNames", "", "", "Method Called.");

      if (pdfNames == null && (pdfData == null || pdfData.size() == 0))
      {
        Log.debugLogAlways(className, "initPDFNames", "", "", "Going to read pdf for " + numTestRun);
        pdfData = GraphNameUtils.readPDFFile(GraphNameUtils.getPDFFilePath(numTestRun, controllerTRNumber, generatorName, partitionDirName, GDFVersion));
      }

      if (pdfNames == null && pdfData != null && pdfData.size() != 0)
        pdfNames = new PDFNames(numTestRun, pdfData);
      else
        Log.errorLog(className, "initPDFNames", "", "", "pdf file not found at path  = " + GraphNameUtils.getPDFFilePath(numTestRun, controllerTRNumber, generatorName, partitionDirName, GDFVersion));
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "initPDFNames", "", "", "Exception - ", e);
    }
  }

  private void initGDFNames()
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "initGDFNames", "", "", "Method Called.");

      if (gdfData == null || gdfData.size() == 0)
      {
        if (numTestRun != -1)
          gdfData = GraphNameUtils.getTestRunGdf(numTestRun, controllerTRNumber, generatorName, partitionDirName, GDFVersion);
        else
          gdfData = GraphNameUtils.readAllGDFS();
      }

      if (gdfData != null)
        totalLinesInGDF = gdfData.size();
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "initGDFNames", "", "", "Exception - ", e);
    }
  }

  // This initialize all Grp and Graph Info
  public boolean initGraphNames()
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "initGraphNames", "", "", "Start method");

      // Initializing PDF Names
      if (!forTemplate)
        initPDFNames();

      // Initializing GDF Names
      initGDFNames();

      if (gdfData == null)
      {
        Log.debugLog(className, "initGraphNames", "", "", "GDF File not found, It may be correpted.");
        return false;
      }

      for (int i = 0; i < totalLinesInGDF; i++)
      {
        String gdfLine = gdfData.get(i);
        try
        {
          String[] arrGDFLine = rptUtilsBean.strToArrayData(gdfLine, GraphNameUtils.GDF_LINE_SEPARATOR);
          if (arrGDFLine == null)
          {
            Log.errorLog(className, "initGraphNames", "", "", "arrGDFLine = null.");
            continue;
          }

          String gdfLineStartWith = arrGDFLine[GraphNameUtils.HEADER_INDEX].toLowerCase();
          if (gdfLineStartWith.equals("info"))
          {
            strInfoLine = gdfLine;
            // Info|3.1|22|112|5310|11424|10000|1/8/14 19:20:21
            processGDFInfoLine(arrGDFLine);
          }
          else if (gdfLineStartWith.equals("group"))
          {
            processGDFGroupLine(arrGDFLine, i);
          }
        }
        catch (Exception ex)
        {
          Log.errorLog(className, "initGraphNames", "", "", "Error in parsing gdf line = " + gdfLine);
        }
      }

      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "initGraphNames", "", "", "Exception - ", e);
      return false;
    }
  }

  // initialize all arrays
  private void initArrayGraphNames()
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "initArrayGraphNames", "", "", "Method Called.");

      // Initializing array and fill with default values
      initArraysAndFillDefaultValues();

      // fill graph details array with gdf data
      fillGraphDetailArrays();
      fillGraphNamesArray();

      if (!forTemplate)
        setFormulaInPDFInfo();
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "initArrayGraphNames", "", "", "Exception - ", e);
    }
  }

  public void fillGraphNamesArray()
  {
    arrGraphNamesWithoutVector = rptUtilsBean.convertArrayListToStrArray(graphNamesWithOutVector);
    arrGraphNamesWithVector = rptUtilsBean.convertArrayListToStrArray(graphNamesWithOutVector);
  }

  // This method fill graph related array with data from vectors
  private void fillGraphDetailArrays()
  {
    if (debugLevel > 0)
      Log.debugLogAlways(className, "fillGraphDetailArrays", "", "", "Method Called.");

    int totalSize = hmGraphInfoBean.size();
    Set<GraphUniqueKeyDTO> keySet = hmGraphInfoBean.keySet();
    Iterator<GraphUniqueKeyDTO> itrGraphUniqueKeyDTO = keySet.iterator();
    arrGraphUniqueKeyDTO = new GraphUniqueKeyDTO[totalSize];
    arrMappingGraphIndex = new int[totalSize];
    int count = 0;
    while (itrGraphUniqueKeyDTO.hasNext())
    {
      arrGraphUniqueKeyDTO[count] = itrGraphUniqueKeyDTO.next();
      arrMappingGraphIndex[count] = count;
      count++;
    }

    /*Sorting of DTO.*/
    getSortedGraphUniqueKeyDTOArray(arrGraphUniqueKeyDTO, arrMappingGraphIndex);

    int idx = 0;
    for (int i = 0; i < totalSize; i++)
    {
      try
      {
        dataTypeNumArray[i] = GraphNameUtils.getDataTypeNum(vectorOfGraphDataType.get(i));
        arrPDFID[i] = vectorOfPDFId.get(i);

        int graphDataIndex = -1;
        long pctDataIndex = -1;

        if (!forTemplate)
        {
          graphDataIndex = (vectorOfGraphDataIndx.get(i)) / getDataElementSize() - (getHeaderLength()) / getDataElementSize();
          idx = setMinMaxCurIdxGraphState(graphDataIndex, GraphNameUtils.getDataTypeNum(vectorOfGraphDataType.get(i)), idx);
          pctDataIndex = vectorOfPCTDataIndex.get(i);
        }

        arrGraphDataIndx[i] = graphDataIndex;
        
        if (graphDataIndex != -1)
          arrDataTypeIndx[graphDataIndex] = i;
        else
          arrDataTypeIndx[i] = i;

        arrPctDataIndx[i] = pctDataIndex;
      }
      catch (Exception ex)
      {
        Log.stackTraceLog(className, "fillGraphDetailArrays", "", "", "Exception - ", ex);
      }
    }

    for (int k = 0; k < arrGraphFormula.length; k++)
    {
      arrGraphFormula[k] = GraphNameUtils.getFormulaNum(vectorOfGraphFormula.get(k));
    }
  }

  private void setFormulaInPDFInfo()
  {
    if (debugLevel > 3)
      Log.debugLogAlways(className, "setFormulaInPDFInfo", "", "", "Method Called.");

    if (pdfNames != null && pdfNames.pdfInfo != null)
    {
      int totalPDFIds = pdfNames.pdfInfo.length;
      for (int i = 0; i < totalPDFIds; i++)
      {
        try
        {
          int tempPDFID = pdfNames.pdfInfo[i].getPDFID();
          int j = 0;
          for (j = 0; j < arrPDFID.length; j++)
          {
            if (arrPDFID[j] != -1)
            {
              if (tempPDFID == arrPDFID[j])
              {
                int formulaNum = arrGraphFormula[arrGraphDataIndx[j]];
                pdfNames.pdfInfo[i].setFormulaNum(formulaNum);
                break;
              }
            }
          }
        }
        catch (Exception ex)
        {
          Log.stackTraceLog(className, "setFormulaInPDFInfo", "", "", "Exception - ", ex);
        }
      }
    }
  }

  private void initArraysAndFillDefaultValues()
  {
    try
    {
      dataTypeNumArray = new int[vectorOfGraphDataType.size()];
      arrGraphDataIndx = new int[vectorOfGraphDataIndx.size()];
      int max = Collections.max(vectorOfGraphDataIndx);

      if (max > 0)
        arrDataTypeIndx = new int[max];
      else
        arrDataTypeIndx = new int[vectorOfGraphDataIndx.size()];

      arrPDFID = new int[vectorOfPDFId.size()];
      arrPctDataIndx = new long[vectorOfPCTDataIndex.size()];

      arrMinDataIndx = new int[vectorOfGraphFormula.size()];
      Arrays.fill(arrMinDataIndx, -1);

      arrMaxDataIndx = new int[vectorOfGraphFormula.size()];
      Arrays.fill(arrMaxDataIndx, -1);

      arrCurCountIndx = new int[vectorOfGraphFormula.size()];
      Arrays.fill(arrCurCountIndx, -1);

      arrGraphFormula = new int[vectorOfGraphFormula.size()];
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "initArraysAndFillDefaultValues", "", "", "Exception - ", e);
    }
  }

  private void processGDFGroupLine(String[] arrGroupLine, int gdfCurrentLineNumber)
  {
    try
    {
      if (debugLevel > 3)
        Log.debugLogAlways(className, "processGDFGroupLine", "", "", "Method Called. gdfCurrentLineNumber = " + gdfCurrentLineNumber);

      GroupInfoBean groupInfoBean = new GroupInfoBean();

      // Getting Group Id
      int groupId = Integer.parseInt(arrGroupLine[GraphNameUtils.GROUP_ID_INDEX].trim());

      // Getting Group Type
      boolean isGroupTypeVector = GraphNameUtils.isTypeVector(arrGroupLine[GraphNameUtils.GROUP_TYPE_INDEX].trim().toLowerCase());

      // Getting number of vectors in Group
      int numOfVectors = 0;
      if (isGroupTypeVector)
        numOfVectors = Integer.parseInt(arrGroupLine[GraphNameUtils.NUM_VECTOR_GROUP_INDEX].trim());

      // Getting Hierarchical Component from Group Line
      String hierarchicalComponent = "NA";
      int totalFields = arrGroupLine.length;
      if (totalFields > GraphNameUtils.HIERARCHICAL_COMPONENT_INDEX)
        hierarchicalComponent = arrGroupLine[GraphNameUtils.HIERARCHICAL_COMPONENT_INDEX];

      // Getting Metrics Name from Group Line
      String metricsName = "NA";
      if (totalFields > GraphNameUtils.METRICS_NAME_INDEX)
        metricsName = arrGroupLine[GraphNameUtils.METRICS_NAME_INDEX];

      // Getting Number of Graphs in Group
      int numOfGraphs = Integer.parseInt(arrGroupLine[GraphNameUtils.GRAPH_COUNT_INDEX].trim());

      // Getting Group Description from Group Line
      String groupDescription = GraphNameUtils.DEFAULT_GROUP_DESCRIPTION;
      if (totalFields > GraphNameUtils.GROUP_DESCRIPTION_INDEX)
        groupDescription = arrGroupLine[GraphNameUtils.GROUP_DESCRIPTION_INDEX];

      String groupName = arrGroupLine[GraphNameUtils.GROUP_NAME_INDEX].trim();

      groupInfoBean.setHierarchicalComponent(hierarchicalComponent);
      groupInfoBean.setMetricsName(metricsName);
      groupInfoBean.setNumOfVectors(numOfVectors);
      groupInfoBean.setNumOfGraphs(numOfGraphs);
      groupInfoBean.setGroupTypeVector(isGroupTypeVector);
      groupInfoBean.setGroupName(groupName);
      groupInfoBean.setGroupDescription(groupDescription);

      hmGroupInfoBean.put(groupId, groupInfoBean);
      int grpSize = 0;
      int grpPCTDataSize = 0;
      String strGrpSizeAndPCTDataSize = "";
      if (isGroupTypeVector && numOfVectors > 0)
      {
        String[] arrGroupIndices = new String[numOfVectors];
        for (int j = 0; j < numOfVectors; j++)
        {
          int vectorIndex = gdfCurrentLineNumber + j + 1;
          String indicesNames = gdfData.get(vectorIndex);
          arrGroupIndices[j] = indicesNames;

          if (strGrpSizeAndPCTDataSize != null && strGrpSizeAndPCTDataSize.length() != 0)
          {
            String[] arrGroupSizeAndPCTDataSize = rptUtilsBean.strToArrayData(strGrpSizeAndPCTDataSize, GraphNameUtils.GDF_LINE_SEPARATOR);
            if (arrGroupSizeAndPCTDataSize.length == 2)
            {
              grpSize = Integer.parseInt(arrGroupSizeAndPCTDataSize[0].trim());
              grpPCTDataSize = Integer.parseInt(arrGroupSizeAndPCTDataSize[1].trim());
            }
          }

          strGrpSizeAndPCTDataSize = processGraphDetailsByGroup(gdfCurrentLineNumber, groupInfoBean.getNumOfGraphs(), grpSize, j, "-" + indicesNames, grpPCTDataSize, groupId, numOfVectors);
          groupCount++;
        }

        groupInfoBean.setIndicesNamesArray(arrGroupIndices);
      }
      else
      {
        strGrpSizeAndPCTDataSize = processGraphDetailsByGroup(gdfCurrentLineNumber, numOfGraphs, grpSize, 0, "", grpPCTDataSize, groupId, numOfVectors);
        groupCount++;
      }

      if (!uniqueGroupIds.contains(groupId))
        uniqueGroupIds.add(groupId);

      hmGroupInfoBean.put(groupId, groupInfoBean);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "processGDFGroupLine", "", "", "Exception - ", e);
    }
  }

  // This function add the all Graph info in Vector and return the size of the
  // Group according to the Data Type & size of PDF Data according to granules
  // (like => "GrpSize|PDFDataSize")
  private String processGraphDetailsByGroup(int gdfCurrentLineNumber, int numOfGraphs, int groupSize, int countForVecGrp, String vectorName, int pctDataSizeOfGroup, int groupId, int numOfVectors)
  {
    try
    {
      if (debugLevel > 3)
      {
        String reqLine = "gdfCurrentLineNumber = " + gdfCurrentLineNumber + ", numOfGraphs = " + numOfGraphs + ", groupSize = " + groupSize + ", groupSize = " + groupSize + ", countForVecGrp = " + countForVecGrp + ", vectorName = " + vectorName + ", pctDataSizeOfGroup = " + pctDataSizeOfGroup;
        Log.debugLogAlways(className, "processGraphDetailsByGroup", "", "", "Method Called With " + reqLine);
      }

      int count = 0;
      int grpSize = 0;
      long grpPDFDataSize = 0;
      String[] arrAllGraphNames = new String[numOfGraphs];
      int[] arrGraphIds = new int[numOfGraphs];
      
      gdfCurrentLineNumber = gdfCurrentLineNumber + (numOfVectors - 1);
      
      while (count < numOfGraphs)
      {
        gdfCurrentLineNumber++;

        if (gdfCurrentLineNumber >= totalLinesInGDF)
        {
          Log.errorLog(className, "processGraphDetailsByGroup", "", "", "Bug: gdfCurrentLineNumber >= totalLinesInGDF");
          return "";
        }

        String graphLine = gdfData.get(gdfCurrentLineNumber);
        if (!graphLine.toLowerCase().startsWith("graph"))
          continue;

        String[] arrGraphLine = rptUtilsBean.strToArrayData(graphLine, GraphNameUtils.GDF_LINE_SEPARATOR);

        String graphName = arrGraphLine[GraphNameUtils.GRAPH_NAME_INDEX];
        int graphId = Integer.parseInt(arrGraphLine[GraphNameUtils.GRAPH_ID_INDEX].trim());

        // Setting graph names
        arrAllGraphNames[count] = graphName;
        arrGraphIds[count] = graphId;

        // Checking Graph Type is vector
        boolean isGraphTypeVector = GraphNameUtils.isTypeVector(arrGraphLine[GraphNameUtils.GRAPH_TYPE_INDEX].trim());

        // Getting Number of indices
        int numOfIndicesInGraphs = Integer.parseInt(arrGraphLine[GraphNameUtils.NUM_VECTOR_GRAPH_INDEX].trim());

        String graphDataType = arrGraphLine[GraphNameUtils.DATA_TYPE_INDEX];
        int dataTypeSize = getDataTypeSize(graphDataType);

        // Getting Data Type number for Sample, Rate Cumulative, Times and
        // TimesStd
        int dataTypeNum = GraphNameUtils.getDataTypeNum(graphDataType);

        // Getting Graph Description
        String graphDescription = GraphNameUtils.DEFAULT_GRAPH_DESCRIPTION;

        int totalFieldsInGraphLine = arrGraphLine.length;
        if (isGraphTypeVector && numOfIndicesInGraphs > 0)
        {
          String[] arrGraphIndices = new String[numOfIndicesInGraphs];
          for (int j = 0; j < numOfIndicesInGraphs; j++)
          {
            int vectorIndex = gdfCurrentLineNumber + j + 1;
            String graphVectorName = gdfData.get(vectorIndex);
            arrGraphIndices[j] = graphVectorName;

            int dataIndex = -1;
            if (!forTemplate)
            {
              int msgDataIndex = Integer.parseInt(arrGraphLine[GraphNameUtils.GRAPH_DATA_INDEX].trim());
              grpSize = grpSize + dataTypeSize;
              dataIndex = ((groupSize * countForVecGrp) + (dataTypeSize * j) + msgDataIndex);
            }

            int pdfId = -1;
            long pctDataIndex = -1;

            if (totalFieldsInGraphLine > 8)
            {
              String strPdfId = arrGraphLine[GraphNameUtils.PDF_ID_INDEX];
              pdfId = Integer.parseInt(strPdfId.trim());
              if (!forTemplate && pdfId != -1)
              {
                String strPctDataIndex = arrGraphLine[GraphNameUtils.PCT_DATA_INDEX];
                int tmpPctDataIndex = Integer.parseInt(strPctDataIndex.trim());
                if (tmpPctDataIndex != -1 && pdfNames != null)
                {
                  long pdfDataSize = pdfNames.getPDFDataSizeByPDFID(strPdfId);
                  grpPDFDataSize = grpPDFDataSize + pdfDataSize;
                  pctDataIndex = (pctDataSizeOfGroup * countForVecGrp) + (pdfDataSize * j) + tmpPctDataIndex;
                }
              }

              graphDescription = arrGraphLine[GraphNameUtils.GRAPH_DESCRIPTION_INDEX];
            }

            GraphUniqueKeyDTO graphUniqueKeyDTO = new GraphUniqueKeyDTO(groupId, graphId, graphVectorName);
            GraphInfoBean graphInfoBean = new GraphInfoBean();
            graphInfoBean.setDataTypeNum(dataTypeNum);
            graphInfoBean.setGraphDescription(graphDescription);
            graphInfoBean.setGraphTypeVector(isGraphTypeVector);
            graphInfoBean.setGraphName(graphName);
            graphInfoBean.setPctDataIndex(pctDataIndex);
            graphInfoBean.setPdfId(pdfId);

            hmGraphInfoBean.put(graphUniqueKeyDTO, graphInfoBean);
            graphNamesWithOutVector.add(graphName);
            String graphNameWithVector = graphName + " - " + graphVectorName;
            graphNamesWithVector.add(graphNameWithVector);
            vectorOfGraphDataType.add(graphDataType);
            vectorOfGraphDataIndx.add(dataIndex);

            // filling Graph Formula in Vector "vectorOfGraphFormula"
            addGraphFormula(arrGraphLine, graphDataType);

            vectorOfPDFId.add(pdfId);
            vectorOfPCTDataIndex.add(pctDataIndex);
            graphCounter++;
          }

          GraphNameCommonInfo graphNameCommonInfo = new GraphNameCommonInfo();
          graphNameCommonInfo.setGraphName(graphName);
          graphNameCommonInfo.setArrGraphVectorNames(arrGraphIndices);

          insertGraphVectorsInMap(groupId, graphId, graphNameCommonInfo);
        }
        else if (isGraphTypeVector && numOfIndicesInGraphs == 0)
        {
          String tempVectorName = "NA";
          long pctDataIndex = -1;
          int pdfId = -1;

          if (forTemplate)
          {
            if (vectorName.trim().startsWith("-"))
              tempVectorName = vectorName.substring(1, vectorName.length());

            vectorOfGraphDataType.add(graphDataType);
            vectorOfGraphDataIndx.add(-1);

            addGraphFormula(arrGraphLine, graphDataType);

            if (totalFieldsInGraphLine > 8)
            {
              String strPdfId = arrGraphLine[GraphNameUtils.PDF_ID_INDEX];
              pdfId = Integer.parseInt(strPdfId.trim());
              vectorOfPDFId.add(pdfId);
              vectorOfPCTDataIndex.add(-1L);
            }
            else
            {
              vectorOfPDFId.add(-1);
              vectorOfPCTDataIndex.add(-1L);
            }

            graphCounter++;
          }

          GraphInfoBean graphInfoBean = new GraphInfoBean();
          graphInfoBean.setDataTypeNum(dataTypeNum);
          graphInfoBean.setGraphDescription(graphDescription);
          graphInfoBean.setGraphTypeVector(isGraphTypeVector);
          graphInfoBean.setGraphName(graphName);
          graphInfoBean.setPctDataIndex(pctDataIndex);
          graphInfoBean.setPdfId(pdfId);

          GraphUniqueKeyDTO graphUniqueKeyDTO = new GraphUniqueKeyDTO(groupId, graphId, tempVectorName);
          hmGraphInfoBean.put(graphUniqueKeyDTO, graphInfoBean);
          graphNamesWithOutVector.add(graphName);
          String graphNameWithVector = graphName + " - " + tempVectorName;
          graphNamesWithVector.add(graphNameWithVector);
        }
        else
        {
          String tempVectorName = "NA";
          if (vectorName.trim().startsWith("-"))
            tempVectorName = vectorName.substring(1, vectorName.length());

          int graphDataIndex = -1;
          int pdfId = -1;
          long pctDataIndex = -1;
          if (!forTemplate)
          {
            grpSize = grpSize + dataTypeSize;
            int msgDataIndex = Integer.parseInt(arrGraphLine[GraphNameUtils.GRAPH_DATA_INDEX].trim());
            graphDataIndex = (groupSize * countForVecGrp) + msgDataIndex;
          }

          if (totalFieldsInGraphLine > 8)
          {
            String strPdfId = arrGraphLine[GraphNameUtils.PDF_ID_INDEX];
            pdfId = Integer.parseInt(strPdfId.trim());
            graphDescription = arrGraphLine[GraphNameUtils.GRAPH_DESCRIPTION_INDEX];
            if (!forTemplate && pdfId != -1 && pdfNames != null)
            {
              String strPctDataIndex = arrGraphLine[GraphNameUtils.PCT_DATA_INDEX];
              if (!strPctDataIndex.equals("-1") && pdfNames != null)
              {
                long pdfDataSizeByPDFID = pdfNames.getPDFDataSizeByPDFID("" + pdfId);
                grpPDFDataSize = grpPDFDataSize + pdfDataSizeByPDFID;
                pctDataIndex = ((pctDataSizeOfGroup * countForVecGrp) + (Integer.parseInt(strPctDataIndex.trim())));
              }
            }
          }

          GraphInfoBean graphInfoBean = new GraphInfoBean();
          graphInfoBean.setDataTypeNum(dataTypeNum);
          graphInfoBean.setGraphDescription(graphDescription);
          graphInfoBean.setGraphTypeVector(isGraphTypeVector);
          graphInfoBean.setGraphName(graphName);
          graphInfoBean.setPctDataIndex(pctDataIndex);
          graphInfoBean.setPdfId(pdfId);

          GraphUniqueKeyDTO graphUniqueKeyDTO = new GraphUniqueKeyDTO(groupId, graphId, tempVectorName);
          hmGraphInfoBean.put(graphUniqueKeyDTO, graphInfoBean);
          graphNamesWithOutVector.add(graphName);
          String graphNameWithVector = graphName + " - " + tempVectorName;
          graphNamesWithVector.add(graphNameWithVector);

          vectorOfGraphDataType.add(graphDataType);
          vectorOfGraphDataIndx.add(graphDataIndex);

          // fill graph data type
          addGraphFormula(arrGraphLine, graphDataType);

          vectorOfPDFId.add(pdfId);
          vectorOfPCTDataIndex.add(pctDataIndex);
          graphCounter++;
        }

        count++;
      }

      hmGroupInfoBean.get(groupId).setArrGraphNames(arrAllGraphNames);
      hmGroupInfoBean.get(groupId).setArrGraphIds(arrGraphIds);
      return (grpSize + "|" + grpPDFDataSize);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "processGraphDetailsByGroup", "", "", "Exception - ", e);
      return "";
    }
  }

  // This method Stores all Graph Vector Names for Group Id and Graph Id
  private void insertGraphVectorsInMap(int groupId, int graphId, GraphNameCommonInfo graphNameCommonInfo)
  {
    HashMap<Integer, GraphNameCommonInfo> hmGraphNameCommonInfo = new HashMap<Integer, GraphNameCommonInfo>();
    hmGraphNameCommonInfo.put(graphId, graphNameCommonInfo);
    hmGraphVectorNames.put(groupId, hmGraphNameCommonInfo);
  }

  private void processGDFInfoLine(String[] arrGDFLine)
  {
    try
    {
      if (debugLevel > 3)
        Log.debugLog(className, "processGDFInfoLine", "", "", "Method Called.");

      gdfInfoBeanObj.setHeaderLength(getIntFromStr(arrGDFLine[GraphNameUtils.START_INDEX].trim()));
      gdfInfoBeanObj.setSizeOfMsgData(getIntFromStr(arrGDFLine[GraphNameUtils.SIZE_OF_MSG_DATA_INDEX].trim()));
      gdfInfoBeanObj.setProgressInterval(getIntFromStr(arrGDFLine[GraphNameUtils.INTERVAL_INDEX].trim()));
      gdfInfoBeanObj.setTestRunStartDateTime(arrGDFLine[GraphNameUtils.TEST_RUN_START_DATETIME_INDEX].trim());

      String versionInfo = arrGDFLine[GraphNameUtils.VERSION_INDEX];
      String[] arrVersionInfo = rptUtilsBean.strToArrayData(versionInfo, GraphNameUtils.GDF_VERSION_SEPARATOR);
      gdfInfoBeanObj.setMajorVersionNum(getIntFromStr(arrVersionInfo[0].trim()));
      gdfInfoBeanObj.setMinorVersionNum(getIntFromStr(arrVersionInfo[1].trim()));
      if (getMajorVersionNum() < 2)
        gdfInfoBeanObj.setDataElementSize(4);
      else
        gdfInfoBeanObj.setDataElementSize(8);

      if (debugLevel > 1)
        Log.debugLogAlways(className, "processGDFInfoLine", "", "", gdfInfoBeanObj.toString());
    }
    catch (Exception e)
    {
      Log.errorLog(className, "processGDFInfoLine", "", "", "Info = " + rptUtilsBean.arrToString(arrGDFLine));
      Log.stackTraceLog(className, "processGDFInfoLine", "", "", "Exception - ", e);
    }
  }

  public int getIntFromStr(String value)
  {
    try
    {
      return Integer.parseInt(value);
    }
    catch (Exception e)
    {
      Log.errorLog(className, "getIntFromStr", "", "", "Can not cast into int Value = " + value);
      return 0;
    }
  }

  // return the data type size
  public int getDataTypeSize(String dataTypeName)
  {
    int dataTypeSize = getDataElementSize();
    if (dataTypeName.equals("sample"))
      return dataTypeSize;

    if (dataTypeName.equals("rate"))
      return dataTypeSize;

    if (dataTypeName.equals("times"))
      return (dataTypeSize * 4);

    if (getMajorVersionNum() < 2)
    {
      if (dataTypeName.equals("cumulative"))
        return (dataTypeSize * 2);

      if (dataTypeName.equals("timesStd"))
        return (dataTypeSize * 6);
    }
    else
    {
      if (dataTypeName.equals("cumulative"))
        return dataTypeSize;

      if (dataTypeName.equals("timesStd"))
        return (dataTypeSize * 5);
    }

    return 0;
  }

  // Add the graph Formula in Vector and skip index according to the data type
  private void addGraphFormula(String[] arrGraphLine, String dataTypeName)
  {
    try
    {
      if (debugLevel > 3)
        Log.debugLogAlways(className, "addGraphFormula", "", "", "Method Called. dataTypeName = " + dataTypeName);

      String formulaUnit = arrGraphLine[GraphNameUtils.GRAPH_FORMULA_INDEX];
      if (dataTypeName.equals("sample"))
      {
        vectorOfGraphFormula.add(formulaUnit);
      }
      else if (dataTypeName.equals("rate"))
      {
        vectorOfGraphFormula.add(formulaUnit);
      }
      else if (dataTypeName.equals("times"))
      {
        for (int i = 0; i < 3; i++)
        {
          vectorOfGraphFormula.add(formulaUnit);
        }

        vectorOfGraphFormula.add("-");
        return;
      }
      else if (dataTypeName.equals("cumulative"))
      {
        if (getMajorVersionNum() < 2)
        {
          for (int i = 0; i < 2; i++)
          {
            vectorOfGraphFormula.add(formulaUnit);
          }
        }
        else
        {
          vectorOfGraphFormula.add(formulaUnit);
        }
      }
      else if (dataTypeName.equals("timesStd"))
      {
        if (getMajorVersionNum() < 2)
        {
          for (int i = 0; i < 3; i++)
          {
            vectorOfGraphFormula.add(formulaUnit);
          }
          for (int i = 0; i < 3; i++)
          {
            vectorOfGraphFormula.add("-");
          }
        }
        else
        {
          for (int i = 0; i < 3; i++)
          {
            vectorOfGraphFormula.add(formulaUnit);
          }

          for (int i = 0; i < 2; i++)
          {
            vectorOfGraphFormula.add("-");
          }
        }
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "addGraphFormula", "", "", "Exception - ", e);
    }
  }

  // set the Min, Max and Cur Count Idx according to the data type
  private int setMinMaxCurIdxGraphState(int graphDataIdx, int dataTypeNum, int idx)
  {
    if (dataTypeNum == GraphNameUtils.DATA_TYPE_SAMPLE)
    {
      arrMinDataIndx[idx] = arrMaxDataIndx[idx] = graphDataIdx;
      idx++;
    }
    else if (dataTypeNum == GraphNameUtils.DATA_TYPE_RATE)
    {
      arrMinDataIndx[idx] = arrMaxDataIndx[idx] = (int) graphDataIdx;
      idx++;
    }
    else if (dataTypeNum == GraphNameUtils.DATA_TYPE_TIMES)
    {
      arrMinDataIndx[idx] = (int) (graphDataIdx + 1);
      arrMaxDataIndx[idx] = (int) (graphDataIdx + 2);
      arrCurCountIndx[idx] = (int) (graphDataIdx + 3);
      idx = idx + 4;
    }
    else if (dataTypeNum == GraphNameUtils.DATA_TYPE_CUMULATIVE)
    {
      if (getMajorVersionNum() < 2)
      {
        arrMinDataIndx[idx] = graphDataIdx;
        arrMaxDataIndx[idx] = graphDataIdx;
        idx = idx + 2;
      }
      else
      {
        arrMinDataIndx[idx] = arrMaxDataIndx[idx] = (int) graphDataIdx;
        idx++;
      }
    }
    else if (dataTypeNum == GraphNameUtils.DATA_TYPE_TIMES_STD)
    {
      if (getMajorVersionNum() < 2)
      {
        arrMinDataIndx[idx] = (graphDataIdx + 1);
        arrMaxDataIndx[idx] = (graphDataIdx + 2);
        arrCurCountIndx[idx] = (graphDataIdx + 3);
        idx = idx + 6;
      }
      else
      {
        arrMinDataIndx[idx] = (int) (graphDataIdx + 1);
        arrMaxDataIndx[idx] = (int) (graphDataIdx + 2);
        arrCurCountIndx[idx] = (int) (graphDataIdx + 3);
        idx = idx + 5;
      }
    }

    return idx;
  }

  public int getHeaderLength()
  {
    try
    {
      return gdfInfoBeanObj.getHeaderLength();
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getHeaderLength", "", "", "Exception - ", ex);
      return 0;
    }
  }

  public int getSizeOfMsgData()
  {
    return gdfInfoBeanObj.getSizeOfMsgData();
  }

  public int[] getArrGraphFormula()
  {
    return arrGraphFormula;
  }

  public int[] getArrMinDataIndx()
  {
    return arrMinDataIndx;
  }

  public int[] getArrMaxDataIndx()
  {
    return arrMaxDataIndx;
  }

  public GraphUniqueKeyDTO[] getGraphUniqueKeyDTO()
  {
    return arrGraphUniqueKeyDTO;
  }

  public int[] getArrCurCountIndx()
  {
    return arrCurCountIndx;
  }

  public int[] getDataTypeNumArray()
  {
    return dataTypeNumArray;
  }

  public int getTestRunNumber()
  {
    return numTestRun;
  }

  public String getTestRunStartDateTime()
  {
    return gdfInfoBeanObj.getTestRunStartDateTime();
  }

  public int getTotalNumOfGraphs()
  {
    return graphCounter;
  }

  public int[] getPDFIdArray()
  {
    return arrPDFID;
  }

  public long[] getPCTDataIndexArray()
  {
    return arrPctDataIndx;
  }

  public int[] getGraphDataIndexArray()
  {
    return arrGraphDataIndx;
  }

  public String getGDFInfoLine()
  {
    return strInfoLine;
  }

  // used for group and graph lines of test runs
  // if group line comes in testrun.gdf then key of linked hash map will be group id
  // if graph line comes in testrun.gdf then its key will be GroupId_GraphId
  public LinkedHashMap<String, String> getGroupGraphLines()
  {
    try
    {
      // Storing Group Line for each Group Id and Graph Id
      LinkedHashMap<String, String> hmGroupGraphLine = new LinkedHashMap<String, String>();
      String groupId = "";
      for (int i = 1; i < gdfData.size(); i++)
      {
        String gdfLine = gdfData.get(i);
        if (gdfLine.toLowerCase().startsWith("group"))
        {
          String[] arrGroupLine = rptUtilsBean.strToArrayData(gdfLine, "|");
          groupId = arrGroupLine[2];
          if (hmGroupGraphLine.containsKey(groupId))
          {
            LinkedHashMap<String, String> arrResult = new LinkedHashMap<String, String>();
            arrResult.put("Error", "TestRun " + numTestRun + " contains duplicate group id " + groupId);
            return arrResult;
          }
          else
          {
            hmGroupGraphLine.put(groupId, gdfLine);
          }
        }
        if (gdfLine.toLowerCase().startsWith("graph"))
        {
          String[] arrGraphLine = rptUtilsBean.strToArrayData(gdfLine, "|");
          String graphId = arrGraphLine[2];
          String key = groupId + "_" + graphId;
          if (hmGroupGraphLine.containsKey(key))
          {
            LinkedHashMap<String, String> arrResult = new LinkedHashMap<String, String>();
            arrResult.put("Error", "TestRun " + numTestRun + " contains duplicate graph id for group id " + groupId);
            return arrResult;
          }
          else
          {
            hmGroupGraphLine.put(groupId + "_" + graphId, gdfLine);
          }
        }
      }
      return hmGroupGraphLine;
    }
    catch (Exception ex)
    {
      Log.errorLog(className, "getGroupGraphLines", "", "", "Exception - " + ex);
      LinkedHashMap<String, String> arrResult = new LinkedHashMap<String, String>();
      arrResult.put("Error", ex.toString());
      return arrResult;
    }
  }

  public ArrayList<Integer> getUniqueGroupIds()
  {
    return uniqueGroupIds;
  }

  // Returns the Vector Names according to the Group Id (Name of Vector Groups)
  public String[] getNameOfGroupIndicesByGroupId(int groupId)
  {
    if (debugLevel > 0)
      Log.debugLogAlways(className, "getNameOfGroupIndicesByGroupId", "", "", "Method Called. groupId = " + groupId);

    GroupInfoBean groupInfoBean = hmGroupInfoBean.get(groupId);
    if (groupInfoBean == null)
      return null;

    return groupInfoBean.getIndicesNamesArray();
  }

  // Returns the Vector Names according to the Grp Id & Graph Id (Name of Vector
  // Graphs)
  public String[] getNameOfIndicesByGroupIdAndGraphId(int groupId, int graphId)
  {
    if (debugLevel > 0)
      Log.debugLogAlways(className, "getNameOfIndicesByGroupIdAndGraphId", "", "", "Method Called. groupId = " + groupId + ", graphId = " + graphId);

    GroupInfoBean groupInfoBean = hmGroupInfoBean.get(groupId);
    if (groupInfoBean == null)
      return null;

    if (groupInfoBean.isGroupTypeVector())
      return groupInfoBean.getIndicesNamesArray();

    HashMap<Integer, GraphNameCommonInfo> hmGraphNameCommonInfo = hmGraphVectorNames.get(groupId);
    if (hmGraphNameCommonInfo == null)
      return null;

    GraphNameCommonInfo graphNameCommonInfo = hmGraphNameCommonInfo.get(graphId);
    if (graphNameCommonInfo == null)
      return null;

    return graphNameCommonInfo.getArrGraphVectorNames();
  }

  // this function return the graphDataIdx by groupId, graphId & vector name
  public int getGraphDataIdxByGrpIdGraphIdVecName(int groupId, int graphId, String vectorName)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "getGraphDataIdxByGrpIdGraphIdVecName", "", "", "Method Called. groupId = " + groupId + ", graphId = " + graphId + ", vectorName = " + vectorName);

      GraphUniqueKeyDTO graphUniqueKeyDTO = new GraphUniqueKeyDTO(groupId, graphId, vectorName);
      int index = BinarySearch.getLocationGraphUniqeKeyDTO(arrGraphUniqueKeyDTO, graphUniqueKeyDTO);
      if (index == -1)
        return index;

      int graphDataIndexPosition = arrMappingGraphIndex[index];
      return arrGraphDataIndx[graphDataIndexPosition];
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getGraphDataIdxByGrpIdGraphIdVecName", "", "", "Exception - ", e);
      return -1;
    }
  }

  // Return the String like "Grp Type | numVecGrp | Graph Type | numVecGraph"
  public String getGroupTypeGraphTypeAndNumOfIndices(int groupId, int graphId)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "getGroupTypeGraphTypeAndNumOfIndices", "", "", "Method Called. groupId = " + groupId + ", graphId = " + graphId);

      GroupInfoBean groupInfoBean = hmGroupInfoBean.get(groupId);
      if (groupInfoBean == null)
        return "scalar|0|scalar|0";

      if (groupInfoBean.isGroupTypeVector())
        return "vector|" + groupInfoBean.getNumOfVectors() + "|scalar|0";

      HashMap<Integer, GraphNameCommonInfo> hmGraphNameCommonInfo = hmGraphVectorNames.get(groupId);
      
      if(hmGraphNameCommonInfo == null || (hmGraphNameCommonInfo.get(graphId) == null))
        return "scalar|0|scalar|0";

      String[] arrVectors = hmGraphNameCommonInfo.get(graphId).getArrGraphVectorNames();
      if (arrVectors == null)
        return "scalar|0|scalar|0";

      return "scalar|0|vector|" + arrVectors.length;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getGroupTypeGraphTypeAndNumOfIndices", "", "", "Exception - ", e);
      return "";
    }
  }

  public ArrayList getInfoAboutGraph()
  {
    return arrInfoAbtGraph;
  }

  public PDFNames getPdfNames()
  {
    return pdfNames;
  }

  public Vector<String> getGdfData()
  {
    return gdfData;
  }

  public Vector<String> getPdfData()
  {
    return pdfData;
  }

  public String getHirarchicalComponentArray(int groupId)
  {
    try
    {
      return hmGroupInfoBean.get(groupId).getHierarchicalComponent();
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getHirarchicalComponentArray", "", "", "Exception - ", ex);
      return "";
    }
  }

  public String getMetricsNameByGroupId(int groupId)
  {
    try
    {
      return hmGroupInfoBean.get(groupId).getMetricsName();
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getMetricsNameByGroupId", "", "", "Exception - ", ex);
      return "";
    }
  }

  public String getGroupDescriptionByGroupId(int groupId)
  {
    try
    {
      return hmGroupInfoBean.get(groupId).getGroupDescription();
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getGroupDescriptionByGroupId", "", "", "Exception - ", ex);
      return GraphNameUtils.DEFAULT_GROUP_DESCRIPTION;
    }
  }

  public boolean isGroupTypeVector(int groupId)
  {
    try
    {
      return hmGroupInfoBean.get(groupId).isGroupTypeVector();
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "isGroupTypeVector", "", "", "Exception - ", ex);
      return false;
    }
  }

  public int[] getGroupIdArray()
  {
    return rptUtilsBean.convertArrayListToIntArray(uniqueGroupIds);
  }

  public String getGroupNameByGroupId(int groupId)
  {
    try
    {
      return hmGroupInfoBean.get(groupId).getGroupName();
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getGroupNameByGroupId", "", "", "Exception - ", e);
      return "";
    }
  }

  public int getNumOfGraphsByGroupId(int groupId)
  {
    try
    {
      return hmGroupInfoBean.get(groupId).getNumOfGraphs();
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getNumOfGraphsByGroupId", "", "", "Exception - ", e);
      return 0;
    }
  }

  // This method is called from Analysis/AnlsStart.java and
  // java/client/reporting/PercentileGraphs.java
  public String[] getArrayOfGraphNames(boolean withVector)
  {
    if (withVector)
      return arrGraphNamesWithVector;
    else
      return arrGraphNamesWithoutVector;
  }

  public String[] getAllGraphNamesByGroupId(int groupId)
  {
    try
    {
      return hmGroupInfoBean.get(groupId).getArrGraphNames();
    }
    catch (Exception e)
    {
      Log.errorLog(className, "getAllGraphNamesByGroupId", "", "", "groupId = " + groupId + " not found.");
      return null;
    }
  }

  public String getGraphNameByGroupIdGraphId(int groupId, int graphId)
  {
    try
    {
      HashMap<Integer, GraphNameCommonInfo> hmGraphNameCommonInfo = hmGraphVectorNames.get(groupId);
      return hmGraphNameCommonInfo.get(graphId).getGraphName();
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getGraphNameByGroupIdGraphId", "", "", "Exception - ", e);
      return "";
    }
  }

  public GraphInfoBean getGraphInfoBeanByGraphUniqueKeyDTO(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    return hmGraphInfoBean.get(graphUniqueKeyDTO);
  }

  public String getGraphNameByGraphUniqueKeyDTO(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    try
    {
      if (graphUniqueKeyDTO == null)
      {
        Log.errorLog(className, "getGraphNameByGraphUniqueKeyDTO", "", "", "graphUniqueKeyDTO is null");
        return "";
      }
      return hmGraphInfoBean.get(graphUniqueKeyDTO).getGraphName();
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getGraphNameByGraphUniqueKeyDTO", "", "", "graphUniqueKeyDTO = " + graphUniqueKeyDTO + ", Exception - ", e);
      return "";
    }
  }

  public String getGraphDescriptionByGraphUniqueKeyDTO(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    try
    {
      return hmGraphInfoBean.get(graphUniqueKeyDTO).getGraphDescription();
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getGraphDescriptionByGraphUniqueKeyDTO", "", "", "graphUniqueKeyDTO = " + graphUniqueKeyDTO + ", Exception - ", e);
      return "";
    }
  }

  public String[] getAllGroupVectorNamesByGroupId(int groupId)
  {
    try
    {
      return hmGroupInfoBean.get(groupId).getIndicesNamesArray();
    }
    catch (Exception e)
    {
      Log.errorLog(className, "getAllGroupVectorNamesByGroupId", "", "", "groupId = " + groupId + " not found.");
      return null;
    }
  }

  // Ravi - Need Binary Search
  public int getPDFIdByGroupIdAndGraphId(int groupId, int graphId)
  {
    int pdfId = -2;
    for (int i = 0; i < arrGraphUniqueKeyDTO.length; i++)
    {
      int groupIdToMatch = arrGraphUniqueKeyDTO[i].getGroupId();
      int graphIdToMatch = arrGraphUniqueKeyDTO[i].getGraphId();
      if (groupId == groupIdToMatch && graphId == graphIdToMatch)
      {
        pdfId = hmGraphInfoBean.get(arrGraphUniqueKeyDTO[i]).getPdfId();
        break;
      }
    }

    return pdfId;
  }

  public int getPDFIdByGraphUniqueKeyDTO(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    try
    {
      return hmGraphInfoBean.get(graphUniqueKeyDTO).getPdfId();
    }
    catch (Exception e)
    {
      Log.errorLog(className, "getPDFIdByGraphUniqueKeyDTO", "", "", "graphUniqueKeyDTO = " + graphUniqueKeyDTO + ", Exception -" + e);
      return -1;
    }
  }

  public long getPDFDataIndexByGraphUniqueKeyDTO(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    try
    {
      return hmGraphInfoBean.get(graphUniqueKeyDTO).getPctDataIndex();
    }
    catch (Exception e)
    {
      Log.errorLog(className, "getPDFDataIndexByGraphUniqueKeyDTO", "", "", "graphUniqueKeyDTO = " + graphUniqueKeyDTO + ", Exception -" + e);
      return -1;
    }
  }

  // Ravi - Need to remove this function. This is temporary function.
  public GraphUniqueKeyDTO getGraphUniqueKeyDTOByGraphDataIndex(int graphDataIndex)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "getGraphUniqueKeyDTOByGraphDataIndex", "", "", "Method Called. groupId = " + graphDataIndex);

      for (int i = 0; i < arrGraphUniqueKeyDTO.length; i++)
      {
        GraphUniqueKeyDTO graphUniqueKeyDTO = arrGraphUniqueKeyDTO[i];
        if (graphUniqueKeyDTO == null)
          continue;

        int matchGraphDataIndex = graphUniqueKeyDTO.getGraphDataIndex();
        if(matchGraphDataIndex == -1)
          matchGraphDataIndex = getGraphDataIdxByGrpIdGraphIdVecName(graphUniqueKeyDTO.getGroupId(), graphUniqueKeyDTO.getGraphId(), graphUniqueKeyDTO.getVectorName());
        if (matchGraphDataIndex == graphDataIndex)
          return graphUniqueKeyDTO;
      }

      Log.errorLog(className, "getGraphUniqueKeyDTOByGraphDataIndex", "", "", "graphDataIndex = " + graphDataIndex + " not found. So returning null.");
      return null;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getGraphDataIdxByGrpIdGraphIdVecName", "", "", "Exception - ", e);
      return null;
    }
  }

  public int getDataTypeNumByGraphUniqueKeyDTO(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    try
    {
      return hmGraphInfoBean.get(graphUniqueKeyDTO).getDataTypeNum();
    }
    catch (Exception e)
    {
      Log.errorLog(className, "getDataTypeNumByGraphUniqueKeyDTO", "", "", "graphUniqueKeyDTO = " + graphUniqueKeyDTO);
      return -1;
    }
  }

  public LinkedHashMap<Integer, GroupInfoBean> getHmGroupInfoBean()
  {
    return hmGroupInfoBean;
  }

  public void setHmGroupInfoBean(LinkedHashMap<Integer, GroupInfoBean> hmGroupInfoBean)
  {
    this.hmGroupInfoBean = hmGroupInfoBean;
  }

  public HashMap<GraphUniqueKeyDTO, GraphInfoBean> getHmGraphInfoBean()
  {
    return hmGraphInfoBean;
  }

  public void setHmGraphInfoBean(LinkedHashMap<GraphUniqueKeyDTO, GraphInfoBean> hmGraphInfoBean)
  {
    this.hmGraphInfoBean = hmGraphInfoBean;
  }

  private void getSortedGraphUniqueKeyDTOArray(GraphUniqueKeyDTO[] arrGraphUniqueKeyDTO, int[] arrMappingGraphIndex)
  {
    try
    {
      new SortGraphUniqueKeyDTO().QuickSort_Recursive(arrGraphUniqueKeyDTO, arrMappingGraphIndex, 0, arrGraphUniqueKeyDTO.length-1);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  static class BinarySearch
  {
    public static int getLocationGraphUniqeKeyDTO(GraphUniqueKeyDTO[] arrGraphUniqueKeyDTO, GraphUniqueKeyDTO graphUniqueKeyDTO)
    {
      try
      {
        int first = 0;
        int n = arrGraphUniqueKeyDTO.length;
        int last = n - 1;
        int middle = (first + last) / 2;

        int groupIdToSearch = graphUniqueKeyDTO.getGroupId();
        int graphIdToSearch = graphUniqueKeyDTO.getGraphId();
        String vectorNameToSearch = graphUniqueKeyDTO.getVectorName();

        while (first <= last)
        {
          int groupId = arrGraphUniqueKeyDTO[middle].getGroupId();
          int graphId = arrGraphUniqueKeyDTO[middle].getGraphId();
          String vectorName = arrGraphUniqueKeyDTO[middle].getVectorName();

          int a = vectorName.compareTo(vectorNameToSearch);

          if (groupId < groupIdToSearch)
            first = middle + 1;
          else if (groupId > groupIdToSearch)
            last = middle - 1;
          else
          {
            if (graphId < graphIdToSearch)
              first = middle + 1;
            else if (graphId > graphIdToSearch)
              last = middle - 1;
            else
            {
              if (a < 0)
                first = middle + 1;
              else if (a > 0)
                last = middle - 1;
              else
                return middle;
            }
          }
          middle = (first + last) / 2;
        }

        return -1;
      }
      catch (Exception e)
      {
        Log.stackTraceLog(className, "getLocationGraphUniqeKeyDTO", "", "", "Exception - ", e);
        return -1;
      }
    }
  }

  public int[] getArrDataTypeIndx()
  {
    return arrDataTypeIndx;
  }

  public void setArrDataTypeIndx(int[] arrDataTypeIndx)
  {
    this.arrDataTypeIndx = arrDataTypeIndx;
  }

  public int getIndexOfGraphUniqueKeyDTO(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    int index = BinarySearch.getLocationGraphUniqeKeyDTO(arrGraphUniqueKeyDTO, graphUniqueKeyDTO);
    return arrMappingGraphIndex[index];
  }

}

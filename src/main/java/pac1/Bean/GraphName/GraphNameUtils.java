package pac1.Bean.GraphName;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import pac1.Bean.Config;
import pac1.Bean.GraphUniqueKeyDTO;
import pac1.Bean.Log;
import pac1.Bean.PartitionInfoUtils;
import pac1.Bean.TestRunDataType;
import pac1.Bean.VectorMappingDTO;
import pac1.Bean.rptUtilsBean;

public class GraphNameUtils implements Serializable
{
  private static final long serialVersionUID = 8428586136902193939L;
  private static String className = "GraphNameUtils";
  private static int totalGroupsInGDF = 0;
  private static int INFO_LINE_INDEX = 0;
  private transient static int debugLevel = 4;
  public static transient final int CUSTOM_GROUP_START_ID = 10000; // custom group Start ID

  // Working directory path
  private static String workPath = Config.getWorkPath();
  private static final String GDF_FILE_EXTN = ".gdf";
  private static final String PDF_FILE_EXTN = ".pdf";
  private transient final static String[] arrGDF = new String[] { "netstorm", "smtp", "pop3", "ftp", "dns", "http_cache", "misc" }; // Array of all GDF in work/etc/ directory

  // Create request URL string converter on given characters.
  public static char[] chrArr1 = new char[] { ' ', '(', ')', '%', '/' };// replace character on.
  public static char[] chrArr2 = new char[] { '$', '~', '_', '@', '*' };// replace character by.

  // In GDF, every line start with header index
  public static transient final int HEADER_INDEX = 0;

  // Indexes of Info Line
  public static final int VERSION_INDEX = 1;
  public static final int NUM_GROUP_INDEX = 2;
  public static final int START_INDEX = 3;
  public static final int TEST_RUN_INDEX = 4;
  public static final int SIZE_OF_MSG_DATA_INDEX = 5;
  public static final int INTERVAL_INDEX = 6;
  public static final int TEST_RUN_START_DATETIME_INDEX = 7;

  // Indexes of Group Line
  public static final int GROUP_NAME_INDEX = 1;
  public static final int GROUP_ID_INDEX = 2;
  public static final int GROUP_TYPE_INDEX = 3;
  public static final int GRAPH_COUNT_INDEX = 4;
  public static final int NUM_VECTOR_GROUP_INDEX = 5;
  public static final int METRICS_NAME_INDEX = 6;
  public static final int HIERARCHICAL_COMPONENT_INDEX = 7;
  public static final int GROUP_DESCRIPTION_INDEX = 8;
  public static final String DEFAULT_GROUP_DESCRIPTION = "Group description not available";

  // Indexes of Graph Line
  // Graph|graphName|rptId|graphType|dataType|graphDataIndex|formula|numVectors|graphState|PDF_ID|Percentile_Data_Idx|future2|future3|GRAPH_DESCRIPTION
  public static final int GRAPH_NAME_INDEX = 1;
  public static final int GRAPH_ID_INDEX = 2;
  public static final int GRAPH_TYPE_INDEX = 3;
  public static final int DATA_TYPE_INDEX = 4;
  public static final int GRAPH_DATA_INDEX = 5;
  public static final int GRAPH_FORMULA_INDEX = 6;
  public static final int NUM_VECTOR_GRAPH_INDEX = 7;
  public static final int GRAPH_STATE_INDEX = 8;
  public static final int PDF_ID_INDEX = 9;
  public static final int PCT_DATA_INDEX = 10;
  public static final int GRAPH_DESCRIPTION_INDEX = 13;
  public static final String DEFAULT_GRAPH_DESCRIPTION = "Graph description not available";

  // Data Types Used in Graph Definition Files
  public static final int DATA_TYPE_SAMPLE = 0;
  public static final int DATA_TYPE_RATE = 1;
  public static final int DATA_TYPE_CUMULATIVE = 2;
  public static final int DATA_TYPE_TIMES = 3;
  public static final int DATA_TYPE_TIMES_STD = 4;

  // This Need to keep for graphs which has no formula.
  public static final int FORMULA_NONE = -1;
  public static final int FORMULA_SEC = 0;
  public static final int FORMULA_PM = 1;
  public static final int FORMULA_PS = 2;
  public static final int FORMULA_KBPS = 3;
  public static final int FORMULA_DBH = 4;

  public static String GDF_LINE_SEPARATOR = "|";
  public static String GDF_VERSION_SEPARATOR = ".";

  public static final int GRANULE_SIZE = 8; // Granule size longlong means 8 bytes
  public static final int START_PCT_GRAPH_DATA_IDX = 32; // Start pct graph data idx

  private static String getGDFFilePath(int testRun, String controllerTRNumber, String generatorName, String partitionDirName, String GDFVersion)
  {
    Log.debugLogAlways(className, "getGDFFilePath", "", "", "Method Called. controllerTRNumber " + controllerTRNumber + ", generatorName = " + generatorName + ", partitionDirName = " + partitionDirName + ", GDFVersion = " + GDFVersion);

    String gdfRptFileWithPath = "";
    String GDF_FILE = "testrun";
    
    if(GDFVersion != null && GDFVersion.length() != 0)
    {
      if(!GDFVersion.equals("0"))
        GDFVersion = "." + GDFVersion;
      else
	GDFVersion = "";
    }
    
    if (partitionDirName != null && partitionDirName.length() != 0)
    {
      if(controllerTRNumber == null || controllerTRNumber.equals("NA") || controllerTRNumber.trim().equals(""))
        gdfRptFileWithPath = workPath + "/webapps/logs/TR" + testRun + "/" + partitionDirName + "/" + GDF_FILE + GDF_FILE_EXTN + GDFVersion;
      else
        gdfRptFileWithPath = workPath + "/webapps/logs/TR" + controllerTRNumber + "/NetCloud/" + generatorName.trim() + "/TR" + testRun + "/" + partitionDirName + "/" + GDF_FILE + GDF_FILE_EXTN + GDFVersion;
    }
    else if (controllerTRNumber != null && !controllerTRNumber.equals("NA") && !controllerTRNumber.trim().equals(""))
      gdfRptFileWithPath = workPath + "/webapps/logs/TR" + controllerTRNumber + "/NetCloud/" + generatorName.trim() + "/TR" + testRun + "/" + GDF_FILE + GDF_FILE_EXTN + GDFVersion;
    else
      gdfRptFileWithPath = workPath + "/webapps/logs/TR" + testRun + "/" + GDF_FILE + GDF_FILE_EXTN + GDFVersion;

    return gdfRptFileWithPath;
  }

  // returns summary.top file path
  public static String getsummary_TopFilePath(int testRun)
  {
    Log.debugLog(className, "getsummary_TopFilePath", "", "", "Method called");
    String summary_TopFileWithPath = workPath + "/webapps/logs/TR" + testRun + "/" + "summary.top";

    // returning summary.top file path
    return summary_TopFileWithPath;
  }
  
  /**
   * Method is used to check if new GDF File Available For test run.
   * @param testRun
   * @param partitionDirName
   * @param GDFVersion
   * @return
   */
  public static boolean isGDFVersionAvailable(int testRun, String partitionDirName, String GDFVersion)
  {
    try
    {
      Log.debugLogAlways(className, "isGDFVersionAvailable", "", "", "Method Called. testRun = " + testRun + ", partitionDirName = " + partitionDirName + ", GDFVersion = " + GDFVersion);
      
      String gdfFilePath = workPath + "/webapps/logs/TR" + testRun + "/testrun" + GDF_FILE_EXTN + GDFVersion;
      
      /*Checking for Partition Name */
      if(partitionDirName != null && partitionDirName.length() != 0)
	gdfFilePath = workPath + "/webapps/logs/TR" + testRun + "/" + partitionDirName + "/testrun" + GDF_FILE_EXTN + GDFVersion;
      
      Log.debugLogAlways(className, "isGDFVersionAvailable", "", "", "Method Called. File Path = " + gdfFilePath);
      
      return (new File(gdfFilePath)).exists();
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return false;
    }
  }

  // returns summary.top file content
  public static String getSummary_TopFile(int testRun)
  {
    try
    {
      String summary_TopFile = "";
      String strLine;
      String summary_TopFileWithPath = getsummary_TopFilePath(testRun);

      File file = new File(summary_TopFileWithPath);
      if (!file.exists())
      {
        Log.errorLog(className, "getSummary_TopFile", "", "", "TestRun " + testRun + " does not have summary.top file.");
        String err = "Error|Testrun " + testRun + " does not have necessary files.";
        return err;
      }

      FileInputStream fis = new FileInputStream(summary_TopFileWithPath);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));
      while ((strLine = br.readLine()) != null)
      {
        strLine = strLine.trim();
        if (strLine.length() == 0)
          continue;
        summary_TopFile = summary_TopFile + strLine;
      }

      br.close();
      fis.close();
      return summary_TopFile;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getSummary_TopFile", "", "", "Exception - ", ex);
      return "";
    }
  }

  public static String getPDFFilePath(int testRun, String controllerTRNumber, String generatorName, String partitionDirName, String GDFVersion)
  {
    Log.debugLogAlways(className, "getPDFFilePath", "", "", "Method Called. controllerTRNumber " + controllerTRNumber + ", generatorName = " + generatorName + ", partitionDirName = " + partitionDirName);

    String pdfRptFileWithPath = "";
    // Feature Not Implemented
    generatorName = "NA";
    //controllerTRNumber = "NA";
    String PDF_FILE = "testrun";

    // here we are setting this empty as we are not supporting version of files for GDF
    GDFVersion = "";
    
    /*if(GDFVersion != null && GDFVersion.length() != 0)
      GDFVersion = "." + GDFVersion;*/
    
    if (partitionDirName != null && partitionDirName.length() != 0)
    {
      if(controllerTRNumber != null && !controllerTRNumber.equals("") && !controllerTRNumber.equals("NA"))
        pdfRptFileWithPath = workPath + "/webapps/logs/TR" + controllerTRNumber + "/" + partitionDirName + "/" + PDF_FILE + PDF_FILE_EXTN + GDFVersion;
      else
        pdfRptFileWithPath = workPath + "/webapps/logs/TR" + testRun + "/" + partitionDirName + "/" + PDF_FILE + PDF_FILE_EXTN + GDFVersion;
    }
    else if (controllerTRNumber != null && !controllerTRNumber.equals("") && !controllerTRNumber.equals("NA"))
    {
      pdfRptFileWithPath = workPath + "/webapps/logs/TR" + controllerTRNumber + "/" + PDF_FILE + PDF_FILE_EXTN + GDFVersion;
    }
    else
    {
      pdfRptFileWithPath = workPath + "/webapps/logs/TR" + testRun + "/" + PDF_FILE + PDF_FILE_EXTN + GDFVersion;
    }

    Log.debugLogAlways(className, "getPDFFilePath", "", "", "Method End pdfRptFileWithPath = " + pdfRptFileWithPath);
    return pdfRptFileWithPath;
  }

  // Get testRun.gdf Report information
  public static Vector<String> getTestRunGdf(int testRun, String controllerTRNumber, String generatorName, String partitionDirName, String GDFVersion)
  {
    try
    {
      String gdfFilePath = getGDFFilePath(testRun, controllerTRNumber, generatorName, partitionDirName, GDFVersion);

      if (debugLevel > 0)
        Log.debugLog(className, "getTestRunGdf", "", "", "Method called. testRun = " + testRun + ", GDF FIle Name = " + gdfFilePath);

      return readGDFFile(gdfFilePath);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getTestRunGdf", "", "", "Exception - ", e);
      return null;
    }
  }

  public static Vector<String> readGDFFile(String gdfFilePath)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "readGDFFile", "", "", "Method Called. gdfFilePath = " + gdfFilePath);

      File trGDF = new File(gdfFilePath);
      if (!trGDF.exists())
      {
        Log.debugLogAlways(className, "readGDFFile", "", "", "gdfFilePath = " + gdfFilePath);
        return null;
      }

      FileInputStream fis = new FileInputStream(trGDF);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));
      Vector<String> gdfFileData = new Vector<String>();
      String strLine;
      boolean infoLineFound = false;

      int count = 0;
      while ((strLine = br.readLine()) != null)
      {
        if (strLine.trim().length() == 0)
        {
          continue;
        }
        else if (strLine.trim().startsWith("#"))
        {
          continue;
        }
        else if (!infoLineFound && strLine.toLowerCase().startsWith("info"))
        {
          String[] arrGDFLines = rptUtilsBean.strToArrayData(strLine, GDF_LINE_SEPARATOR);
          int numOfGroups = Integer.parseInt(arrGDFLines[NUM_GROUP_INDEX]);
          totalGroupsInGDF += numOfGroups;
          infoLineFound = true;
          INFO_LINE_INDEX = count;
        }

        gdfFileData.add(strLine);
        count++;
      }

      br.close();
      fis.close();
      return gdfFileData;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "readGDFFile", "", "", "Exception - ", e);
      return null;
    }
  }

  public static Vector<String> readPDFFile(String pdfFilePath)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "readPDFFile", "", "", "Method Called. pdfFilePath = " + pdfFilePath);

      File trGDF = new File(pdfFilePath);
      if (!trGDF.exists())
      {
        Log.debugLog(className, "readPDFFile", "", "", "testrun.pdf not found at " + pdfFilePath);
        return null;
      }

      FileInputStream fis = new FileInputStream(trGDF);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));
      Vector<String> pdfFileData = new Vector<String>();
      String strLine;
      while ((strLine = br.readLine()) != null)
      {
        if (strLine.length() == 0)
        {
          continue;
        }
        else if (strLine.startsWith("#"))
        {
          Log.errorLog(className, "readPDFFile", "", "", "few line in gdf are commented in " + pdfFilePath + ". So ignoring...");
          continue;
        }

        pdfFileData.add(strLine);
      }

      br.close();
      fis.close();
      return pdfFileData;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "readPDFFile", "", "", "Exception - ", e);
      return null;
    }
  }

  // Read All Gdf File in work/etc dir and work/sys dir
  public static Vector<String> readAllGDFS()
  {
    String infoLine = "";
    String gdfFilePath = "";
    Vector<String> gdfFileData = null;
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "readAllGDFS", "", "", "Start method");

      ArrayList<String> arrAllGDF = loadAllGDF(); // Names of all GDF files
      Vector<String> finalVector = new Vector<String>();

      int totalGDFS = arrAllGDF.size();
      for (int i = 0; i < totalGDFS; i++)
      {
        gdfFilePath = arrAllGDF.get(i);
        gdfFileData = readGDFFile(gdfFilePath);
        if (gdfFileData == null)
        {
          Log.errorLog(className, "readAllGDFS", "", "", "gdfFileData is null for gdf = " + gdfFilePath);
          continue;
        }

        infoLine = gdfFileData.get(INFO_LINE_INDEX);
        gdfFileData.removeElementAt(INFO_LINE_INDEX);
        finalVector.addAll(gdfFileData);
      }

      String[] arrRecors = rptUtilsBean.strToArrayData(infoLine.toString(), "|");
      arrRecors[NUM_GROUP_INDEX] = "" + totalGroupsInGDF;
      String strNewInfoLine = rptUtilsBean.strArrayToStr(arrRecors, GDF_LINE_SEPARATOR);
      Log.debugLog(className, "readAllGDFS", "", "", "Updating Info line in vector. Line = " + strNewInfoLine);
      finalVector.add(0, strNewInfoLine);

      return finalVector;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "", "", "", "gdfFilePath = " + gdfFilePath + ", infoLine = " + infoLine + ", INFO_LINE_INDEX = " + INFO_LINE_INDEX);
      Log.stackTraceLog(className, "readAllGDFS", "", "", " Exception - ", e);
      return null;
    }
  }

  // load all gdf file in Array List
  private static ArrayList<String> loadAllGDF()
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLog(className, "loadAllGDF", "", "", "Start method");

      ArrayList<String> arrAllCustomGDF = new ArrayList<String>(); // Names of all Custom GDF files

      String customGDFFilePath = getCustomGDFPath();
      File customGDF = new File(customGDFFilePath);

      String sysGDFFilePath = getSystemGDFPath();
      ArrayList<String> arrAllGDF = new ArrayList<String>(); // Names of all GDF files
      for (int i = 0; i < arrGDF.length; i++)
      {
        String temp = sysGDFFilePath + arrGDF[i] + GDF_FILE_EXTN;

        if (debugLevel > 0)
          Log.debugLogAlways(className, "loadAllGDF", "", "", "Adding GDF in ArrayList = " + temp);

        arrAllGDF.add(temp);
      }

      // Enhance this to use list(FilenameFilter filter) to get only *.gdf file
      String arrayFiles[] = customGDF.list();

      // Calculate number of gdf files
      for (int j = 0; j < arrayFiles.length; j++)
      {
        // Use lastIndexOf() function instead indexOf() beacuse reading extn.
        if (arrayFiles[j].lastIndexOf(GDF_FILE_EXTN) == -1) // Skip non gdf files
          continue;

        // to check that file is .gdf or .gdf.hot
        String[] tempArr = rptUtilsBean.strToArrayData(arrayFiles[j], ".");
        if (tempArr.length == 2)
        {
          String customGDFFileWithPath = customGDFFilePath + arrayFiles[j];

          if (debugLevel > 0)
            Log.debugLogAlways(className, "loadAllGDF", "", "", "Adding Custom GDF in temp ArrayList = " + customGDFFileWithPath);

          arrAllCustomGDF.add(customGDFFileWithPath);
        }
      }

      if ((arrAllCustomGDF != null) && (arrAllCustomGDF.size() > 0))
      {
        String[] arrSortedGDFByGroupName = sortCustomGroupByGroupName(arrAllCustomGDF);
        for (int i = 0; i < arrSortedGDFByGroupName.length; i++)
        {
          arrAllGDF.add(arrSortedGDFByGroupName[i]); // add custom GDF in sorted order
        }
      }

      Log.debugLogAlways(className, "loadAllGDF", "", "", "Number of GDF files = " + arrAllGDF.size());
      return arrAllGDF;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "loadAllGDF", "", "", "Exception - ", e);
      return null;
    }
  }

  // This function return the list of gdf in sorted order by group name
  private static String[] sortCustomGroupByGroupName(ArrayList<String> arrAllCustomGDF)
  {
    try
    {
      // add only first group of custom gdf, and sort it our assumption is that every custom gdf has only one group
      String[] arrGroupNameSortedOrder = new String[arrAllCustomGDF.size()];
      String[] arrSortedGDFByGroupName = new String[arrAllCustomGDF.size()];
      int index = 0;

      Iterator<String> iteratorAllCustomGDF = arrAllCustomGDF.iterator();

      while (iteratorAllCustomGDF.hasNext())
      {
        String gdfFilePath = iteratorAllCustomGDF.next();
        Vector<String> gdfFileData = readGDFFile(gdfFilePath);
        if (gdfFileData == null)
        {
          Log.errorLog(className, "sortCustomGroupByGroupName", "", "", "Cannot read gdf file = " + gdfFilePath);
          return null;
        }

        int totalLinesInGDF = gdfFileData.size();
        for (int i = 0; i < totalLinesInGDF; i++)
        {
          String dataLine = gdfFileData.get(i);
          if (dataLine.toLowerCase().startsWith("group"))
          {
            String[] arrGDFLine = rptUtilsBean.strToArrayData(dataLine, GDF_LINE_SEPARATOR);
            arrGroupNameSortedOrder[index] = arrGDFLine[GROUP_NAME_INDEX] + "|" + index; // append group name and index position of gdf file in array list
            break;
          }
        }

        index++;
      }

      Arrays.sort(arrGroupNameSortedOrder); // sort group name

      for (int i = 0; i < arrGroupNameSortedOrder.length; i++)
      {
        String[] arrRcrd = rptUtilsBean.strToArrayData(arrGroupNameSortedOrder[i], GDF_LINE_SEPARATOR);
        int indxPos = Integer.parseInt(arrRcrd[1]);
        arrSortedGDFByGroupName[i] = arrAllCustomGDF.get(indxPos).toString();
      }

      return arrSortedGDFByGroupName;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "sortCustomGroupByGroupName", "", "", "Exception - ", e);
      return null;
    }
  }

  // This will return the Custom GDF path
  private static String getCustomGDFPath()
  {
    return (workPath + "/sys/");
  }

  // This will return the System GDF path
  private static String getSystemGDFPath()
  {
    return (workPath + "/etc/");
  }

  // Set the data type Number
  public static int getDataTypeNum(String dataTypeName)
  {
    if (dataTypeName.equals("sample"))
      return DATA_TYPE_SAMPLE;

    else if (dataTypeName.equals("rate"))
      return DATA_TYPE_RATE;

    else if (dataTypeName.equals("cumulative"))
      return DATA_TYPE_CUMULATIVE;

    else if (dataTypeName.equals("times"))
      return DATA_TYPE_TIMES;

    else if (dataTypeName.equals("timesStd"))
      return DATA_TYPE_TIMES_STD;

    else
      return -1; // -1 Means no Data Type
  }

  // Set the formula number
  public static int getFormulaNum(String formulaName)
  {
    if (formulaName.equals("SEC"))
      return FORMULA_SEC;
    else if (formulaName.equals("PM"))
      return FORMULA_PM;
    else if (formulaName.equals("PS"))
      return FORMULA_PS;
    else if (formulaName.equals("KBPS"))
      return FORMULA_KBPS;
    else if (formulaName.equals("DBH"))
      return FORMULA_DBH;
    else
      return FORMULA_NONE; // return -1 Means no formula
  }

  // This function get the msgData size by test run
  public static int getMsgDataSizeByTestNum(int testNum)
  {
    try
    {
      String gdfFilePath = getGDFFilePath(testNum, "NA", "", "", "");
      File trGDF = new File(gdfFilePath);
      if (!trGDF.exists())
      {
        Log.debugLogAlways(className, "getMsgDataSizeByTestNum", "", "", "gdfFilePath = " + gdfFilePath);
        return -1;
      }

      FileInputStream fis = new FileInputStream(trGDF);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));
      String infoLine;
      int testRunSizeOfMsgData = -1;
      while ((infoLine = br.readLine()) != null)
      {
        if (infoLine.length() == 0)
        {
          continue;
        }
        else if (infoLine.startsWith("#"))
        {
          Log.errorLog(className, "readGDFFile", "", "", "few line in gdf are commented in " + gdfFilePath + ". So ignoring...");
          continue;
        }
        else if (infoLine.toLowerCase().startsWith("info"))
        {
          String[] arrGDFLines = rptUtilsBean.strToArrayData(infoLine, GDF_LINE_SEPARATOR);
          testRunSizeOfMsgData = Integer.parseInt(arrGDFLines[SIZE_OF_MSG_DATA_INDEX]);
          break;
        }
      }

      br.close();
      fis.close();

      return testRunSizeOfMsgData;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getMsgDataSizeByTestNum", "", "", "Exception - ", e);
      return -1;
    }
  }

  /**
   * This Function Check that testrun.gdf is available in the TestRun or not
   * 
   * @param testRun
   * @return
   */
  public static boolean isGDFAvail(String testRun)
  {
    try
    {

      int numTestRun = Integer.parseInt(testRun.trim());
      String curPartitionName = "";

      if (TestRunDataType.getTestRunPartitionType(numTestRun) > 0)
      {
        /* Getting the Current Partition From Test Run Directory. */
        curPartitionName = (new PartitionInfoUtils()).getCurrentPartitionNameByTestRunNumber(numTestRun);

        if (curPartitionName == null)
        {
          Log.errorLog(className, "isGraphRawDataFileAvailable", "", "", "Current Partition Information not available.");
          return false;
        }
      }

      String gdfRptFileWithPath = workPath + "/webapps/logs/TR" + testRun + "/" + curPartitionName + "/testrun.gdf";
      File trGDF = new File(gdfRptFileWithPath);

      if (!trGDF.exists())
        return false;
      else
        return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "isGDFAvail", "", "", "Exception - ", e);
      return false;
    }
  }

  // Check for Vector
  public static boolean isTypeVector(String strRcrds)
  {
    return strRcrds.equals("vector");
  }

  public static GraphUniqueKeyDTO[] createGraphUniqueKeyDTO(String[] arrGroupIds, String[] arrGraphIds, String[] arrVectorNames, GraphNames graphNames, ArrayList<VectorMappingDTO> vectorMappingList)
  {
    if (arrGraphIds != null && arrGraphIds != null && arrVectorNames != null && arrGraphIds.length == arrGraphIds.length && arrGraphIds.length == arrVectorNames.length)
    {
      GraphUniqueKeyDTO[] arrGraphUniqueKeyDTO = new GraphUniqueKeyDTO[arrGraphIds.length];
      for (int i = 0; i < arrVectorNames.length; i++)
      {
        try
        {
          int groupId = Integer.parseInt(arrGroupIds[i].trim());
          int graphId = Integer.parseInt(arrGraphIds[i].trim());
          String vectorName = arrVectorNames[i].trim();
          GraphUniqueKeyDTO graphUniqueKeyDTO = new GraphUniqueKeyDTO(groupId, graphId, vectorName);

          int graphDataIndex = graphNames.getGraphDataIndexByGraphUniqueKeyDTO(graphUniqueKeyDTO);

          if (graphDataIndex == -1)
          {
            String strVectorName = rptUtilsBean.getVectorNameFromVectorMapping(vectorMappingList, groupId, vectorName);

            if (strVectorName != null)
              graphDataIndex = graphNames.getGraphDataIdxByGrpIdGraphIdVecName(groupId, graphId, strVectorName);

            if (graphDataIndex == -1)
              continue;

            vectorName = strVectorName;
            graphUniqueKeyDTO.setVectorName(vectorName);
          }

          arrGraphUniqueKeyDTO[i] = graphUniqueKeyDTO;
        }
        catch (Exception e)
        {
          Log.stackTraceLog(className, "createGraphUniqueKeyDTO", "", "", "Exception - ", e);
        }
      }
      return arrGraphUniqueKeyDTO;
    }

    return null;
  }
  
  // return the data type size
  public static int getDataTypeSize(int dataTypeSize, int majorVersion, String  dataTypeName)
  {
    if (dataTypeName.equals("sample"))
      return dataTypeSize;

    if (dataTypeName.equals("rate"))
      return dataTypeSize;

    if (dataTypeName.equals("times"))
      return (dataTypeSize * 4);

    if (majorVersion < 2)
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

  /**
   * Sai - This method is for checking is graphType is Response Type
   * @param graphUniqueKeyDTO
   * @return
   */
  public static boolean isResponseTypeGraph(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    if (graphUniqueKeyDTO == null)
      return false;

    boolean isResponseGraph = false;
    int groupId = graphUniqueKeyDTO.getGroupId();
    int graphId = graphUniqueKeyDTO.getGraphId();

    // URL
    if (groupId == 3 && graphId == 3)
      isResponseGraph = true;
    // Page
    else if (groupId == 4 && graphId == 3)
      isResponseGraph = true;
    // Session
    else if (groupId == 5 && graphId == 3)
      isResponseGraph = true;
    // Transaction
    else if (groupId == 6 && graphId == 3)
      isResponseGraph = true;
    // Transaction Stats
    else if (groupId == 102 && graphId == 1)
      isResponseGraph = true;
    else
      isResponseGraph = false;

    Log.debugLogAlways(className, "isResponseTypeGraph", "", "", "for " + graphUniqueKeyDTO + " isResponseGraph = " + isResponseGraph);
    return isResponseGraph;
  }
}

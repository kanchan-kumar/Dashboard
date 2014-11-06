/**
 * This class is for Utilty methods for percentile
 * @author Ravi Kant Sharma
 * @since Netsorm Version 4.0.0
 * @Modification_History Ravi Kant Sharma - Initial Version 4.0.0
 * @version 4.0.0
 * 
 */
package pac1.Bean.Percentile;

import java.io.*;
import java.util.*;

import pac1.Bean.*;
import pac1.Bean.GraphName.GraphNameUtils;

public class PercentileDataUtils implements PercentileErrorDefinition
{
  /**
   * On the basis of Debug Level, Need to write debug logs
   */
  public static int debugLevel = 0;

  private static String className = "PercentileDataUtils";

  public static final byte GRAPH_TYPE_PERCENTILE = 0;
  public static final byte GRAPH_TYPE_SLAB_COUNT = 1;
  public static final byte GRAPH_TYPE_FREQUNCY_DISTRIBUTION = 2;

  public static final byte GRANULE_SIZE = 8; // size of each GRANULE is long long means 8 bytes
  public static final int PROCESS_HDR_LEN = 32;

  public static final byte SEQ_NUM_INDEX = 0 * GRANULE_SIZE;
  public static final byte ACTIVE_INDEX = 1 * GRANULE_SIZE;
  public static final byte TIME_STAMP_INDEX = 2 * GRANULE_SIZE;
  public static final byte FUTURE_INDEX = 3 * GRANULE_SIZE;
  private static String workPath = Config.getWorkPath();
  private static String pctMessageFileName = "pctMessage.dat";
  private static String pdfFileName = "testrun.pdf";
  private static String testRunGDFFileName = "testrun.gdf";
  public static String GLOBAL_DAT_FILE_NAME = "global.dat";

  public int getDebugLevel()
  {
    return debugLevel;
  }

  public static void setDebugLevel(int debugLevel)
  {
    PercentileDataUtils.debugLevel = debugLevel;
  }

  public static int getStartIndex(int pctDataIndex)
  {
    int startIndex = (pctDataIndex * GRANULE_SIZE + PROCESS_HDR_LEN);
    return startIndex;
  }

  public static int getEndIndex(int pctDataIndex, int numGranuale)
  {
    int startIndex = getStartIndex(pctDataIndex);
    int endIndex = (startIndex + numGranuale * GRANULE_SIZE);
    return endIndex;
  }

  public static String getTestRunDirectoryPath(int testRunNumber)
  {
    return workPath + "/webapps/logs/TR" + testRunNumber;
  }

  public static String getPartitionPCTMessageFilePath(int controllerTestRunNumber, String generatorTestRunNumber, String partitionName, String generatorName)
  {
    Log.debugLogAlways(className, "", "", "", "controllerTestRunNumber = " + controllerTestRunNumber + ", generatorTestRunNumber = " + generatorTestRunNumber + ", partitionName = " + partitionName + ", generatorName = " + generatorName);

    if (generatorTestRunNumber != null && generatorName != null && !generatorTestRunNumber.trim().equals("NA"))
    {
      String testRunDirectory = getTestRunDirectoryPath(controllerTestRunNumber);
      String netCloudDirector = testRunDirectory + "/" + "NetCloud";
      String generatorDir = netCloudDirector + "/" + generatorName;
      String generatorTRDir = generatorDir + "/TR" + generatorTestRunNumber;
      if (partitionName == null || partitionName.equals(""))
        return generatorTRDir + "/" + pctMessageFileName;
      else
        return generatorTRDir + "/" + partitionName + "/" + pctMessageFileName;
    }
    else
    {
      if (partitionName == null || partitionName.trim().equals(""))
        return getTestRunDirectoryPath(controllerTestRunNumber) + "/" + pctMessageFileName;
      else
        return getTestRunDirectoryPath(controllerTestRunNumber) + "/" + partitionName + "/" + pctMessageFileName;
    }
  }

  public static String getPartitionPdfFilePath(int controllerTestRunNumber, String partitionName, String generatorTestRunNumber, String generatorName)
  {
    if (partitionName == null || partitionName.equals(""))
      return getTestRunDirectoryPath(controllerTestRunNumber) + "/" + pdfFileName;
    else
      return getTestRunDirectoryPath(controllerTestRunNumber) + "/" + partitionName + "/" + pdfFileName;
  }

  public static String getPartitionTestRunGDFFilePath(int testRunNumber, String partitionName)
  {
    if (partitionName == null || partitionName.equals(""))
      return getTestRunDirectoryPath(testRunNumber) + "/" + testRunGDFFileName;
    else
      return getTestRunDirectoryPath(testRunNumber) + "/" + partitionName + "/" + testRunGDFFileName;
  }

  public static Vector<String> readFile(String fileNameWithPath)
  {
    try
    {
      Log.debugLogAlways(className, "readFile", "", "", "fileNameWithPath = " + fileNameWithPath);
      Vector<String> vecFileLines = new Vector<String>();
      String strLine;
      FileInputStream fis = new FileInputStream(fileNameWithPath);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));
      while ((strLine = br.readLine()) != null)
      {
        strLine = strLine.trim();
        if (strLine.length() == 0)
          continue;
        if (strLine.startsWith("#"))
          continue;

        vecFileLines.add(strLine);
      }

      br.close();
      fis.close();
      return vecFileLines;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "readFile", "", "", "Exception - ", e);
      return null;
    }
  }

  // Make double value up to 3 digit decimal
  public static double convTo3DigitDecimal(double val)
  {
    double dblVal;
    dblVal = Math.round(val * 1000);
    dblVal = dblVal / 1000;
    return (dblVal);
  }

  public static double getFormulaUnitDataByFormulaNum(int formulaNum)
  {
    double formulaUnitData = 1;

    try
    {
      Log.debugLogAlways(className, "getFormulaUnitDataByFormulaNum", "", "", "Method Start. Formula number = " + formulaNum);
      // SEC - Convert milli-sec to seconds
      if (formulaNum == GraphNameUtils.FORMULA_SEC)
      {
        int convertionUnitMilli2Sec = 1000;
        formulaUnitData = formulaUnitData / convertionUnitMilli2Sec;
      }
      // PM - Convert to Per Minute
      else if (formulaNum == GraphNameUtils.FORMULA_PM)
      {
        Log.errorLog(className, "getFormulaUnitDataByFormulaNum", "", "", "Error: Currently we are not supported this PM formula.");
      }
      // PS - Convert to Per Seconds
      else if (formulaNum == GraphNameUtils.FORMULA_PS)
      {
        Log.errorLog(className, "getFormulaUnitDataByFormulaNum", "", "", "Error: Currently we are not supported this PS formula.");
      }
      // KBPS - Convert to kilo byte per second
      else if (formulaNum == GraphNameUtils.FORMULA_KBPS)
      {
        int convertionUnitBytes2KBPS = 1024;
        formulaUnitData = formulaUnitData / convertionUnitBytes2KBPS;
      }
      // DBH - Convert to divide by 100
      else if (formulaNum == GraphNameUtils.FORMULA_DBH) //
      {
        int convertionUnit2DBH = 100;
        formulaUnitData = formulaUnitData / convertionUnit2DBH;
      }

      Log.debugLogAlways(className, "getFormulaUnitDataByFormulaNum", "", "", "formula unit data = " + formulaUnitData);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getFormulaUnitDataByFormulaNum", "", "", "Exception - ", e);
    }

    return formulaUnitData;
  }

  public static int[] getPercentileValue()
  {
    int[] arrDefaultValue = { 50, 80, 90, 95, 99 };
    try
    {
      Log.debugLogAlways(className, "getPercentileValue", "", "", "Method Called. keyword = netstorm.execution.percentile");
      String nthPercentile = Config.getValue("netstorm.execution.percentile");
      if (nthPercentile == null || nthPercentile.equals(""))
        return arrDefaultValue;

      String[] arrNthPercentile = rptUtilsBean.strToArrayData(nthPercentile, ",");
      if (arrNthPercentile == null)
        return arrDefaultValue;

      int[] configuredNthPercentileValue = new int[arrNthPercentile.length];
      for (int i = 0; i < configuredNthPercentileValue.length; i++)
      {
        int percentileValue = Integer.parseInt(arrNthPercentile[i].trim());
        if (percentileValue > 100)
        {
          Log.errorLog(className, "getPercentileValue", "", "", "percentileValue in config.ini cannot be greater then 100. So using default value.");
          return arrDefaultValue;
        }

        configuredNthPercentileValue[i] = percentileValue;
      }

      Log.debugLogAlways(className, "getPercentileValue", "", "", "" + rptUtilsBean.intArrayToStr(configuredNthPercentileValue, ","));
      return configuredNthPercentileValue;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getPercentileValue", "", "", "Exception - ", e);
    }

    return arrDefaultValue;
  }

  /**
   * This method return array of percentile data
   * 
   * rawValue is in sorted order.
   * 
   * @param rawValue
   * @param pctCalSize
   * @return
   */
  public static double[] getPercentileData(int debugLevel, double[] rawValue)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "getPercentileData", "", "", "rawValue = " + rawValue.length);

      int first_non_zero_index = 0;
      double scale = 1;
      int MAX_PCT_ARR_SIZE = 1048576;
      int[] pct_arr = new int[MAX_PCT_ARR_SIZE];
      double[] out_pct = new double[101];
      int pctCalSize = rawValue.length;
      double max = rawValue[rawValue.length - 1];

      if (debugLevel > 0)
        Log.debugLogAlways(className, "getPercentileData", "", "", "Max value from dataset = " + max);

      if (max == 0.0)
        return out_pct;

      if (max > MAX_PCT_ARR_SIZE)
        scale = max / MAX_PCT_ARR_SIZE + 1;

      double scaled_max = max / scale;

      if (debugLevel > 0)
        Log.debugLogAlways(className, "getPercentileData", "", "", "Max scale value from dataset = " + scaled_max);

      int divider = 1;
      while (scaled_max <= 99999)
      {
        scaled_max *= 10;
        divider *= 10;
      }

      for (int i = 0; i < pctCalSize; i++)
      {
        pct_arr[(int) ((rawValue[i] / scale) * divider)]++;
      }

      while (pct_arr[first_non_zero_index] == 0)
      {
        first_non_zero_index++;
      }

      int pct = 1;
      double cmp_count = (((double) pctCalSize - 1) * pct) / 100 + 1;
      int cmp_count_int = (int) cmp_count;
      double cmp_count_fraction = cmp_count - cmp_count_int;
      int cum_count = 0;

      int int_scaled_max = (int) scaled_max + 1;
      for (int i = first_non_zero_index; i <= int_scaled_max; i++)
      {
        cum_count += pct_arr[i];
        while (cum_count >= cmp_count_int && pct <= 100)
        {
          out_pct[pct] = ((i) * scale) / (divider);
          int cur_idx = i + 1;
          if (cum_count == cmp_count_int && cmp_count_fraction > 0)
          {
            while ((pct_arr[cur_idx] == 0) && (cur_idx <= int_scaled_max))
            {
              cur_idx++;
            }
            out_pct[pct] += (((cur_idx - i) * cmp_count_fraction * scale) / divider);
          }

          pct++;
          cmp_count = (((double) pctCalSize - 1) * pct) / 100 + 1;
          cmp_count_int = (int) cmp_count;
          cmp_count_fraction = cmp_count - cmp_count_int;
        }
      }

      return out_pct;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "getPercentileData", "", "", "Exception - " + e);
      return null;
    }
  }

  /**
   * Method Finds all the Graph Numbers associated with Derived Expression and return a unique list of Graph Numbers.
   * 
   * @param arrDerivedExpression
   */
  public static ArrayList<GraphUniqueKeyDTO> getGraphUniqueKeyDTOListFromExpressionList(ArrayList<String> derivedExpressionList)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "getGraphUniqueKeyDTOListFromExpressionList", "", "", "Method Called. derivedExpressionList = " + derivedExpressionList);

      if (derivedExpressionList == null || derivedExpressionList.size() == 0)
      {
        Log.debugLogAlways(className, "getGraphUniqueKeyDTOListFromExpressionList", "", "", "Derived Expression List must not be null.");
        return null;
      }

      // ArrayList containing unique list of Graph Number.
      ArrayList<GraphUniqueKeyDTO> arrGraphUniqueKeyDTO = new ArrayList<GraphUniqueKeyDTO>();

      // Iterating through each Expression and finds Graph Number.
      for (int k = 0; k < derivedExpressionList.size(); k++)
      {
        String derivedExp = derivedExpressionList.get(k);
        ArrayList<GraphUniqueKeyDTO> arrExpressionGraphUniqueKeyDTOs = getGraphUniqueKeyDTOByDerivedExp(derivedExp);

        for (int i = 0; i < arrExpressionGraphUniqueKeyDTOs.size(); i++)
        {
          GraphUniqueKeyDTO graphUniqueKeyDTO = arrExpressionGraphUniqueKeyDTOs.get(i);

          // Checking For Duplicate Graph Numbers.
          if (!arrGraphUniqueKeyDTO.contains(graphUniqueKeyDTO))
            arrGraphUniqueKeyDTO.add(graphUniqueKeyDTO);
        }
      }

      return arrGraphUniqueKeyDTO;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getGraphUniqueKeyDTOListFromExpressionList", "", "", "Exception -", e);
      return null;
    }
  }

  /**
   * Method gets all Graph Numbers associated with input Derived Expression.
   * 
   * @param derivedGraphExpression
   * @return
   */
  private static ArrayList<GraphUniqueKeyDTO> getGraphUniqueKeyDTOByDerivedExp(String derivedGraphExpression)
  {
    if (debugLevel > 0)
      Log.debugLogAlways(className, "getGraphUniqueKeyDTOByDerivedExp", "", "", "derivedGraphExpression = " + derivedGraphExpression);

    // ArrayList Containing Expression Graph Numbers.
    ArrayList<GraphUniqueKeyDTO> arrExpressionGraphNumbers = new ArrayList<GraphUniqueKeyDTO>();

    try
    {
      // Getting Expression Tokens By evaluating Expression.
      String[] expressionTokens = getTokens(derivedGraphExpression);

      for (int i = 0; i < expressionTokens.length; i++)
      {
        String[] graphInfo = getStringArray(expressionTokens[i]);

        // Checking For Parsing Error.
        if (graphInfo == null)
        {
          Log.debugLogAlways(className, "getGraphUniqueKeyDTOByDerivedExp", "", "", "Error in Parsing Expression Token = " + expressionTokens[i]);
          continue;
        }

        // Getting GroupId, GraphId and vector name from Graph Info array.
        String groupId = ParseDerivedExp.replaceDerivedFunName(graphInfo[0].trim());
        String graphId = graphInfo[1].trim();
        String vectorName = graphInfo[2].trim().replace("[", "").replace("]", "");

        if (debugLevel > 0)
          Log.debugLogAlways(className, "getGraphUniqueKeyDTOByDerivedExp", "", "", "groupId = " + groupId + ", graphId = " + graphId + ", vector Name = " + vectorName);

        if (groupId == null || groupId.trim().equals(""))
          continue;

        GraphUniqueKeyDTO graphUniqueKeyDTO = new GraphUniqueKeyDTO(Integer.parseInt(groupId), Integer.parseInt(graphId), vectorName);
        arrExpressionGraphNumbers.add(graphUniqueKeyDTO);
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getGraphUniqueKeyDTOByDerivedExp", "", "", "Exception - ", e);
    }

    return arrExpressionGraphNumbers;
  }

  /**
   * Input: 1.2.[v1,v2] + 2.3.[v3] Output: str[] = {1.2.[v1,v2], 2.3.[v3]} Process: reads character by character and parse the entire string and returns a final String as 1.2.[v1,v2]#2.3.[v3]
   * 
   * @return An array by splitting this with #
   * 
   */
  private static String[] getTokens(String expression)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "getTokens", "", "", "Method Called for expression = " + expression);

      char ch;
      int dotCount = 0;
      boolean startBracketFound = false;
      StringBuffer strBuff = new StringBuffer();

      if (expression == null)
        return null;

      for (int i = 0; i < expression.length(); i++)
      {
        ch = expression.charAt(i);

        if (ch == '(' || ch == ')' || ch == '{' || ch == '}' || ch == ' ')
          continue;

        if ((ch == '+' || ch == '-' || ch == '*' || ch == '/') && !startBracketFound)
          continue;

        if (dotCount == 2 && ch != '[')
        {
          strBuff.append(ch);
          strBuff.append(expression.charAt(i + 1) + "#$");
          i++;
          dotCount = 0;
          continue;
        }

        if (dotCount == 2 && ch == '[')
          dotCount = 0;

        if (ch == '.' && !startBracketFound)
          dotCount++;

        if (ch == '[')
          startBracketFound = true;

        if (ch != ']')
          strBuff.append(ch);
        else
        {
          strBuff.append("]#$");
          dotCount = 0;
          startBracketFound = false;
        }
      }

      if (strBuff.toString().endsWith("#$"))
        strBuff.delete(strBuff.length() - 2, strBuff.length());

      return strBuff.toString().split("\\#\\$");
    }
    catch (Exception ex)
    {
      Log.errorLog(className, "getTokens", "", "", "Exception : " + expression);
      return null;
    }
  }

  /**
   * This function is used when VectorName have dot(.) then getting problem to convert string to string array(Like 7.2.T.O).
   * 
   * @param inputString
   * @return
   */
  private static String[] getStringArray(String inputString)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "getStringArray", "", "", "Method Called. inputString = " + inputString);

      int count = 0;
      String grpId = "";
      String graphId = "";
      String vectName = "";

      for (int i = 0; i < inputString.length(); i++)
      {
        if (("" + inputString.charAt(i)).equals("."))
        {
          if (count == 0)
          {
            grpId = inputString.substring(0, i);
            inputString = inputString.replaceFirst(grpId + ".", "");
            count++;
            i = 0;
          }
          else if (count == 1)
          {
            graphId = inputString.substring(0, i);
            inputString = inputString.replaceFirst(graphId + ".", "");
            count++;
            i = 0;
          }
        }

        if (count == 2)
        {
          vectName = inputString;
        }
      }

      String[] arrGrpIdGraphIdVectName = new String[3];
      arrGrpIdGraphIdVectName[0] = grpId;
      arrGrpIdGraphIdVectName[1] = graphId;
      arrGrpIdGraphIdVectName[2] = vectName;
      return arrGrpIdGraphIdVectName;
    }
    catch (Exception ex)
    {
      Log.errorLog(className, "getStringArray", "", "", "Exception - " + ex);
      return null;
    }
  }

  /**
   * Ravi - This method is returns all partition list.
   * 
   * @return
   */
  public static ArrayList<String> getAllPartitionList(int debugLevel, int controllerTestRun, boolean isPartitionModeEnabled, SessionReportData sessionReportDataObj)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "getAllPartitionList", "", "", "Method Called. testRunNumber = " + controllerTestRun + ", isPartitionModeEnabled = " + isPartitionModeEnabled);

      ArrayList<String> allPartitionsList = null;
      if (isPartitionModeEnabled)
      {
	if(sessionReportDataObj == null)
	{
	  Log.debugLogAlways(className, "getAllPartitionList", "", "", "Creating new instance of SessionReportData");
	  sessionReportDataObj = new SessionReportData(controllerTestRun);
	}
	
        allPartitionsList = sessionReportDataObj.getAvailableSessionList();
      }

      if (debugLevel > 3)
        Log.debugLogAlways(className, "getAllPartitionList", "", "", "allPartitionsList = " + allPartitionsList);

      return allPartitionsList;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getAllPartitionList", "", "", "Exception - ", e);
      return null;
    }
  }

  /**
   * Ravi - This method check file exist and not empty
   * 
   * @param debugLevel
   * @param filePath
   */
  public static boolean isValidFile(int debugLevel, String filePath)
  {
    try
    {
      if (debugLevel > 3)
        Log.debugLogAlways(className, "isValidFile", "", "", "Method Called. filePath = " + filePath);

      File fileObj = new File(filePath);
      if (fileObj.exists() && fileObj.length() != 0)
      {
        if (debugLevel > 3)
          Log.debugLogAlways(className, "isValidFile", "", "", "Method End. filePath = " + filePath + " is valid");

        return true;
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "isValidFile", "", "", "Exception - ", e);
    }

    Log.debugLogAlways(className, "isValidFile", "", "", "filePath = " + filePath + " is not valid.");
    return false;
  }

  /**
   * Ravi - This method is for filtering the partitions based on time criteria.
   * 
   * Note:
   * 
   * 1. For Run Phase, We are not filtering the partitions based on time
   * 
   * 2. If Scenario Setting is Total Run, then output list size must be 1
   * 
   * 3. If Scenario Setting is Run Phase Only, then output list size must be greater then 0
   * 
   * @return
   */
  public static ArrayList<PctPartitionInfo> getValidPartitionList(int debugLevel, int modeType, int controllerTestRun, String startTime, String endTime, String phaseName, boolean isPartitionModeEnabled, String generatorTestRun, String generatorName, SessionReportData sessionReportDataObj, StringBuffer errMsg)
  {
    ArrayList<PctPartitionInfo> filteredPartionList = new ArrayList<PctPartitionInfo>();

    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "getValidPartitionList", "", "", "Method Called StartTime = " + startTime + ", endTime = " + endTime + ", phaseName = " + phaseName + ", isPartitionModeEnabled = " + isPartitionModeEnabled + ", modeType = " + modeType + ", controllerTestRun = " + controllerTestRun);

      if (startTime == null)
        startTime = "NA";

      if (endTime == null)
        endTime = "NA";

      ArrayList<String> allPartitionsList = new ArrayList<String>();

      if (isPartitionModeEnabled)
        allPartitionsList = getAllPartitionList(debugLevel, controllerTestRun, isPartitionModeEnabled, sessionReportDataObj);

      // Condition for Percentile Setting is Specified Interval
      if (modeType == PERCENTILE_SPECIFIED_TIME)
      {
        // Generate reports for Total Run
        if (startTime.equals("NA") && endTime.equals("NA"))
        {
          if (debugLevel > 0)
            Log.debugLogAlways(className, "getValidPartitionList", "", "", "Case of report generation for Total Run");

          if (isPartitionModeEnabled)
          {
            for (int i = 0; i < allPartitionsList.size(); i++)
            {
              String partitionName = allPartitionsList.get(i);
              String pctMessageFilePath = getPartitionPCTMessageFilePath(controllerTestRun, generatorTestRun, partitionName, generatorName);
              if (isValidFile(debugLevel, pctMessageFilePath))
              {
                PctPartitionInfo pctPartitionInfo = new PctPartitionInfo(pctMessageFilePath, partitionName, generatorName, generatorTestRun);
                filteredPartionList.add(pctPartitionInfo);
              }
            }
          }
          else
          {
            String pctMessageFilePath = getPartitionPCTMessageFilePath(controllerTestRun, generatorTestRun, "", generatorName);
            if (isValidFile(debugLevel, pctMessageFilePath))
            {
              PctPartitionInfo pctPartitionInfo = new PctPartitionInfo(pctMessageFilePath, "", generatorName, generatorTestRun);
              filteredPartionList.add(pctPartitionInfo);
            }
            else
            {
              Log.errorLog(className, "getValidPartitionList", "", "", "pctMessageFilePath = " + pctMessageFilePath + " is not valid.");
            }
          }

          if (debugLevel > 0)
            Log.debugLogAlways(className, "getValidPartitionList", "", "", "Case of report generation for Total Run filteredPartionList = " + filteredPartionList);

          return filteredPartionList;
        }
        else if (!startTime.equals("NA") && !endTime.equals("NA"))
        {
          if (debugLevel > 0)
            Log.debugLogAlways(className, "getValidPartitionList", "", "", "Case of report generation for specified time start time = " + startTime + ", endTime = " + endTime + ", isPartitionModeEnabled = " + isPartitionModeEnabled);

          if (isPartitionModeEnabled)
          {
            for (int i = 0; i < allPartitionsList.size(); i++)
            {
              String partitionName = allPartitionsList.get(i);
              String pctMessageFilePath = getPartitionPCTMessageFilePath(controllerTestRun, generatorTestRun, partitionName, generatorName);
              if (isValidFile(debugLevel, pctMessageFilePath))
              {
                long partitionTimeStamp = ExecutionDateTime.convertFormattedDateToMilliscond(partitionName, DataPacketInfo.PARTITION_FILE_START_DATE_FORMAT, null);
                long startTimeAbsoluteFormat = Long.parseLong(startTime);
                long endTimeAbsoluteFormat = Long.parseLong(endTime);
                if (startTimeAbsoluteFormat <= partitionTimeStamp && endTimeAbsoluteFormat >= partitionTimeStamp)
                {
                  PctPartitionInfo pctPartitionInfo = new PctPartitionInfo(pctMessageFilePath, partitionName, generatorName, generatorTestRun);
                  filteredPartionList.add(pctPartitionInfo);
                }
              }
            }
          }
          else
          {
            String pctMessageFilePath = getPartitionPCTMessageFilePath(controllerTestRun, generatorTestRun, "", generatorName);
            if (isValidFile(debugLevel, pctMessageFilePath))
            {
              PctPartitionInfo pctPartitionInfo = new PctPartitionInfo(pctMessageFilePath, "", generatorName, generatorTestRun);
              filteredPartionList.add(pctPartitionInfo);
            }
            else
            {
              Log.errorLog(className, "getValidPartitionList", "", "", "pctMessageFilePath = " + pctMessageFilePath + " is not valid.");
            }
          }

          if (debugLevel > 0)
            Log.debugLogAlways(className, "getValidPartitionList", "", "", "Case of report generation for specified time filteredPartionList = " + filteredPartionList);

          return filteredPartionList;
        }
        else if (!startTime.equals("NA"))
        {
          if (debugLevel > 0)
            Log.debugLogAlways(className, "getValidPartitionList", "", "", "Case of report generation for specified time (2) as start time = " + startTime + ", endTime = " + endTime + ", isPartitionModeEnabled = " + isPartitionModeEnabled);

          if (isPartitionModeEnabled)
          {
            for (int i = 0; i < allPartitionsList.size(); i++)
            {
              String partitionName = allPartitionsList.get(i);
              String pctMessageFilePath = getPartitionPCTMessageFilePath(controllerTestRun, generatorTestRun, partitionName, generatorName);
              if (isValidFile(debugLevel, pctMessageFilePath))
              {
                long partitionTimeStamp = ExecutionDateTime.convertFormattedDateToMilliscond(partitionName, DataPacketInfo.PARTITION_FILE_START_DATE_FORMAT, null);
                long startTimeAbsoluteFormat = Long.parseLong(startTime);
                if (startTimeAbsoluteFormat <= partitionTimeStamp)
                {
                  PctPartitionInfo pctPartitionInfo = new PctPartitionInfo(pctMessageFilePath, partitionName, generatorName, generatorTestRun);

                  if (debugLevel > 2)
                    Log.debugLogAlways(className, "getValidPartitionList", "", "", "adding pctPartitionInfo = " + pctPartitionInfo);

                  filteredPartionList.add(pctPartitionInfo);
                }
              }
            }
          }
          else
          {
            String partitionName = "";
            String pctMessageFilePath = getPartitionPCTMessageFilePath(controllerTestRun, generatorTestRun, partitionName, generatorName);
            if (isValidFile(debugLevel, pctMessageFilePath))
            {
              PctPartitionInfo pctPartitionInfo = new PctPartitionInfo(pctMessageFilePath, partitionName, generatorName, generatorTestRun);
              filteredPartionList.add(pctPartitionInfo);
            }
            else
            {
              Log.errorLog(className, "getValidPartitionList", "", "", "pctMessageFilePath = " + pctMessageFilePath + " is not valid.");
            }
          }

          if (debugLevel > 0)
            Log.debugLogAlways(className, "getValidPartitionList", "", "", "Case of report generation for specified time (2) filteredPartionList = " + filteredPartionList);

          return filteredPartionList;
        }
        else
        {
          if (debugLevel > 0)
            Log.debugLogAlways(className, "getValidPartitionList", "", "", "Case of report generation for specified time (3) as start time = " + startTime + ", endTime = " + endTime + ", isPartitionModeEnabled = " + isPartitionModeEnabled);

          if (isPartitionModeEnabled)
          {
            for (int i = 0; i < allPartitionsList.size(); i++)
            {
              String partitionName = allPartitionsList.get(i);
              String pctMessageFilePath = getPartitionPCTMessageFilePath(controllerTestRun, generatorTestRun, partitionName, generatorName);
              if (isValidFile(debugLevel, pctMessageFilePath))
              {
                long partitionTimeStamp = ExecutionDateTime.convertFormattedDateToMilliscond(partitionName, DataPacketInfo.PARTITION_FILE_START_DATE_FORMAT, null);
                long endTimeAbsoluteFormat = Long.parseLong(endTime);
                if (endTimeAbsoluteFormat <= partitionTimeStamp)
                {
                  PctPartitionInfo pctPartitionInfo = new PctPartitionInfo(pctMessageFilePath, partitionName, generatorName, generatorTestRun);
                  filteredPartionList.add(pctPartitionInfo);
                }
              }
            }
          }
          else
          {
            String pctMessageFilePath = getPartitionPCTMessageFilePath(controllerTestRun, generatorTestRun, "", generatorName);
            if (isValidFile(debugLevel, pctMessageFilePath))
            {
              PctPartitionInfo pctPartitionInfo = new PctPartitionInfo(pctMessageFilePath, "", generatorName, generatorTestRun);
              filteredPartionList.add(pctPartitionInfo);
            }
            else
            {
              Log.errorLog(className, "getValidPartitionList", "", "", "pctMessageFilePath = " + pctMessageFilePath + " is not valid.");
            }
          }

          if (debugLevel > 0)
            Log.debugLogAlways(className, "getValidPartitionList", "", "", "Case of report generation for specified time (3) filteredPartionList = " + filteredPartionList);

          return filteredPartionList;
        }
      }

      // Condition for Percentile Setting is TOTAL RUN
      else if (modeType == PERCENTILE_TOTAL_RUN)
      {
        if (debugLevel > 0)
          Log.debugLogAlways(className, "getValidPartitionList", "", "", "Case of report generation for total run and isPartitionModeEnabled = " + isPartitionModeEnabled);

        if (isPartitionModeEnabled)
        {
          int currentPartitionIndex = allPartitionsList.size() - 1;
          for (int i = currentPartitionIndex; i >= 0; i--)
          {
            String partitionName = allPartitionsList.get(i);
            String pctMessageFilePath = getPartitionPCTMessageFilePath(controllerTestRun, generatorTestRun, partitionName, generatorName);
            if (isValidFile(debugLevel, pctMessageFilePath))
            {
              PctPartitionInfo pctPartitionInfo = new PctPartitionInfo(pctMessageFilePath, partitionName, generatorName, generatorTestRun);
              filteredPartionList.add(pctPartitionInfo);
              break;
            }
            else
            {
              Log.errorLog(className, "getValidPartitionList", "", "", "Test run does not have any valid pctMessage.dat file.");
            }
          }
        }
        else
        {
          String pctMessageFilePath = getPartitionPCTMessageFilePath(controllerTestRun, generatorTestRun, "", generatorName);
          if (isValidFile(debugLevel, pctMessageFilePath))
          {
            PctPartitionInfo pctPartitionInfo = new PctPartitionInfo(pctMessageFilePath, "", generatorName, generatorTestRun);
            filteredPartionList.add(pctPartitionInfo);
          }
          else
          {
            Log.errorLog(className, "getValidPartitionList", "", "", "Test run does not have any valid pctMessage.dat file.");
          }
        }

        if (debugLevel > 0)
          Log.debugLogAlways(className, "getValidPartitionList", "", "", "Case of report generation for total run and filteredPartionList = " + filteredPartionList);

        return filteredPartionList;
      }

      // Condition if Scenario Setting is Run Phase Only
      else if (modeType == PERCENTILE_RUN_PHASE)
      {
        if (debugLevel > 0)
          Log.debugLogAlways(className, "getValidPartitionList", "", "", "Case of report generation for run phase.");

        if (isPartitionModeEnabled)
        {
          for (int i = 0; i < allPartitionsList.size(); i++)
          {
            String partitionName = allPartitionsList.get(i);
            String pctMessageFilePath = getPartitionPCTMessageFilePath(controllerTestRun, generatorTestRun, partitionName, generatorName);
            if (isValidFile(debugLevel, pctMessageFilePath))
            {
              PctPartitionInfo pctPartitionInfo = new PctPartitionInfo(pctMessageFilePath, partitionName, generatorName, generatorTestRun);
              filteredPartionList.add(pctPartitionInfo);
            }
          }
        }
        else
        {
          String pctMessageFilePath = getPartitionPCTMessageFilePath(controllerTestRun, generatorTestRun, "", generatorName);
          if (isValidFile(debugLevel, pctMessageFilePath))
          {
            PctPartitionInfo pctPartitionInfo = new PctPartitionInfo(pctMessageFilePath, "", generatorName, generatorTestRun);
            filteredPartionList.add(pctPartitionInfo);
          }
          else
          {
            Log.errorLog(className, "getValidPartitionList", "", "", "Test run does'nt contain any valid pctMessage.dat file. file should be exist at " + pctMessageFilePath);
          }
        }

        if (debugLevel > 0)
          Log.debugLogAlways(className, "getValidPartitionList", "", "", "Case of report generation for run phase.");

        return filteredPartionList;
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getValidPartitionList", "", "", "Exception - ", e);
    }

    return filteredPartionList;
  }

  /**
   * This method is for getting percentile mode type from scenario
   * 
   * @param debugLevel
   * @param testRunNumber
   * @return
   */
  public static int getPercentileModeTypeFrmTestRunPDF(int debugLevel, int controllerTestRunNumber, String partitionName, String generatorTestRunNumber, String generatorName)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "getPercentileModeTypeFrmTestRunPDF", "", "", "Method Called. controllerTestRunNumber = " + controllerTestRunNumber + ", partitionName = " + partitionName + ", generatorTestRunNumber = " + generatorTestRunNumber + ", generatorName = " + generatorName);

      String pdfFilePth = getPartitionPdfFilePath(controllerTestRunNumber, partitionName, generatorTestRunNumber, generatorName);
      if (debugLevel > 1)
        Log.debugLogAlways(className, "getPercentileModeTypeFrmTestRunPDF", "", "", "pdfFilePth = " + pdfFilePth);

      Vector vecData = rptUtilsBean.readFileInVector(pdfFilePth);
      if (vecData == null)
      {
        Log.errorLog(className, "getPercentileModeTypeFrmTestRunPDF", "", "", "pdfFilePth = " + pdfFilePth + " does not exist.");
        return -1;
      }

      for (int i = 0; i < vecData.size(); i++)
      {
        String dataLine = vecData.get(i).toString();
        String[] arrRcrd = rptUtilsBean.strToArrayData(dataLine, "|");
        if (arrRcrd[0].equalsIgnoreCase("info"))
          return Integer.parseInt(arrRcrd[4].trim());
      }

      return 0;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getPercentileModeTypeFrmTestRunPDF", "", "", "Exception - ", e);
      return 0;
    }
  }

  /**
   * This will read global data file and return different phases of test run
   * 
   * @return true/false
   */
  public static double[] getPhaseTimes(int testRunNumber)
  {
    double[] phaseTimes = null;
    String globalDataFile = PercentileDataUtils.getTestRunDirectoryPath(testRunNumber) + "/" + PercentileDataUtils.GLOBAL_DAT_FILE_NAME;

    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "getPhaseTimes", "", "", "Method Called.");

      String str1 = "";
      StringTokenizer st3;
      String str5[];

      FileInputStream fis = new FileInputStream(globalDataFile);

      if (debugLevel > 0)
        Log.debugLogAlways(className, "getPhaseTimes", "", "", "globalFilepath =" + globalDataFile);

      BufferedReader br = new BufferedReader(new InputStreamReader(fis));
      int j = 0;

      while ((str1 = br.readLine()) != null)
      {
        j = 0;
        if (str1.indexOf("PHASE_TIMES") != -1)
        {
          phaseTimes = new double[4];
          st3 = new StringTokenizer(str1);
          str5 = new String[st3.countTokens()];
          while (st3.hasMoreTokens())
          {
            str5[j] = st3.nextToken();
            j++;
          }

          // phaseTimes = new double[str5.length - 1];
          for (int ii = 1, mm = str5.length; ii < mm; ii++)
          {
            phaseTimes[ii - 1] = Double.parseDouble(str5[ii]) * 1000;

            if (debugLevel > 0)
              Log.debugLogAlways(className, "getPhaseTimes", "", "", "Phase Time =" + phaseTimes[ii - 1]);
          }
        }
      }

      br.close();
      fis.close();
    }
    catch (FileNotFoundException e)
    {
      // Do not return false if file is not found. This will allow user to
      // generate graphs for running test runs.
      Log.errorLog(className, "getPhaseTimes", "", "", "File (" + globalDataFile + ") not found. Ignored as test run may be running. Using 0 for all times");

      // Make it null so that we can check letter data is there or not.
      phaseTimes = null;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getPhaseTimes", "", "", "Exception while reading data file", e);
      phaseTimes = null;
    }

    return phaseTimes;
  }

  public static String getGeneratorTestRunNumByGeneratorName(int testRunNumber, String generatorName)
  {
    if (generatorName == null)
      return "NA";

    Vector<String> vectNetCloudData = NetCloudData.getNetCloudData(testRunNumber);
    ArrayList<NetCloudDTO> nectCloudDtoList = NetCloudData.getNectCloudDtoList(vectNetCloudData, testRunNumber);
    for (int i = 0; i < nectCloudDtoList.size(); i++)
    {
      NetCloudDTO netCloudDTO = nectCloudDtoList.get(i);
      String generatorNameTemp = netCloudDTO.getGeneratorName();
      if (generatorNameTemp == null)
        return "NA";

      if (generatorNameTemp.equals(generatorName))
        return netCloudDTO.getGeneratorTRNumber();
    }

    return "NA";
  }
}

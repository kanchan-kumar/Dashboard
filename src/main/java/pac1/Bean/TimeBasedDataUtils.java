package pac1.Bean;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;

import pac1.Bean.GraphName.GraphNames;

/**
 * 
 * This class is used by TimeBasedTestRunData For Utilities function.
 */
public class TimeBasedDataUtils implements Serializable, Cloneable
{
  private static final long serialVersionUID = 4748669445603861023L;
  private static String className = "TimeBasedDataUtils";
  public static int debugLogLevel = 0;

  public TimeBasedDataUtils()
  {
  }

  /**
   * The method Increase the size of 2d double array with specified number of size.
   * 
   * @param arrSampleData
   * @param sizeToIncrease
   * @return
   */
  public double[][] increase2DArrayColSize(double arrSampleData[][], int sizeToIncrease)
  {

    try
    {
      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "increase2DArrayColSize", "", "", "Method Called. sizeToIncrease = " + sizeToIncrease);

      int newColSize = arrSampleData[0].length + sizeToIncrease;

      double[][] newSampleArray = new double[arrSampleData.length][newColSize];

      // Start Copying of array to new Array.
      for (int row = 0; row < newSampleArray.length; row++)
      {
        for (int col = 0; col < newSampleArray[row].length; col++)
        {
          // Fill newly created column with 0.0 value.
          if (col < arrSampleData[0].length)
            newSampleArray[row][col] = arrSampleData[row][col];
          else
            newSampleArray[row][col] = 0.0;
        }
      }

      return newSampleArray;

    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "increase2DArrayColSize", "", "", "Exception - ", e);
      return arrSampleData;
    }
  }

  /**
   * The method Increase the size of 2d int array with specified number of size.
   * 
   * @param arrSampleData
   * @param sizeToIncrease
   * @return
   */
  public int[][] increase2DIntArrayColSize(int arrSampleData[][], int sizeToIncrease)
  {

    try
    {
      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "increase2DArrayColSize", "", "", "Method Called. sizeToIncrease = " + sizeToIncrease);

      int newColSize = arrSampleData[0].length + sizeToIncrease;

      int[][] newSampleArray = new int[arrSampleData.length][newColSize];

      // Start Copying of array to new Array.
      for (int row = 0; row < newSampleArray.length; row++)
      {
        for (int col = 0; col < newSampleArray[row].length; col++)
        {
          // Fill newly created column with 0.0 value.
          if (col < arrSampleData[0].length)
            newSampleArray[row][col] = arrSampleData[row][col];
          else
            newSampleArray[row][col] = 0;
        }
      }

      return newSampleArray;

    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "increase2DArrayColSize", "", "", "Exception - ", e);
      return arrSampleData;
    }
  }

  /**
   * The method Increase the size of double array with specified number of size.
   * 
   * @param arrDoubleData
   * @param sizeToIncrease
   * @return
   */
  public double[] increaseSizeOfDoubleArray(double arrDoubleData[], int sizeToIncrease)
  {

    try
    {
      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "increaseSizeOfDoubleArray", "", "", "Method Called. sizeToIncrease = " + sizeToIncrease);

      int size = arrDoubleData.length + sizeToIncrease;

      double[] newDoubleArray = new double[size];

      // Start Copying of array to new Array.
      for (int row = 0; row < newDoubleArray.length; row++)
      {
        if (row < arrDoubleData.length)
          newDoubleArray[row] = arrDoubleData[row];
        else
          newDoubleArray[row] = 0.0;
      }

      return newDoubleArray;

    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "increaseSizeOfDoubleArray", "", "", "Exception - ", e);
      return arrDoubleData;
    }
  }

  /**
   * The method Increase the size of double array with specified number of size.
   * 
   * @param arrDoubleData
   * @param sizeToIncrease
   * @return
   */
  public long[] increaseSizeOfLongArray(long arrLongData[], int sizeToIncrease)
  {

    try
    {
      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "increaseSizeOfLongArray", "", "", "Method Called. sizeToIncrease = " + sizeToIncrease);

      int size = arrLongData.length + sizeToIncrease;

      long[] newLongArray = new long[size];

      // Start Copying of array to new Array.
      for (int row = 0; row < newLongArray.length; row++)
      {
        if (row < arrLongData.length)
          newLongArray[row] = arrLongData[row];
        else
          newLongArray[row] = 0;
      }

      return newLongArray;

    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "increaseSizeOfLongArray", "", "", "Exception - ", e);
      return arrLongData;
    }
  }

  /**
   * Method is used to get the minimum value from array.
   * 
   * @param arrData
   * @return
   */
  public double getMinValueFromArray(double[] arrData, int sizeToIterate)
  {
    try
    {
      if (arrData == null)
        return 0.0;

      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "getMinValueFromArray", "", "", "Method Called. arrData.length = " + arrData.length + ", sizeToIterate = " + sizeToIterate);

      //Initialize with first value of array.
      double minData = arrData[0];
      for (int i = 0; i < sizeToIterate; i++)
      {
	if((arrData[i]+"").equals("-0.0"))
	  continue;
	
	if((minData+"").equals("-0.0"))
	{
	  minData = arrData[i];
	}
	else
	{
          if(arrData[i] < minData)
            minData = arrData[i];
	}
      }
      return minData;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return 0.0;
    }
  }

  /**
   * Method is used to get the maximum value from array.
   * 
   * @param arrData
   * @return
   */
  public double getMaxValueFromArray(double[] arrData, int sizeToIterate)
  {
    try
    {
      if (arrData == null)
        return 0.0;

      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "getMaxValueFromArray", "", "", "Method Called. arrData.length = " + arrData.length + ", sizeToIterate = " + sizeToIterate);

      double maxData = 0.0;

      for (int i = 0; i < sizeToIterate; i++)
      {
        if (arrData[i] > maxData)
          maxData = arrData[i];
      }
      return maxData;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return 0.0;
    }
  }

  /**
   * Method is used to get sum of all elements in array.
   * 
   * @param arrData
   * @return
   */
  public int getSumOfArrayElements(int[] arrData, int sizeToIterate)
  {
    try
    {
      int total = 0;

      if (arrData == null)
        return 0;

      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "getSumOfArrayElements", "", "", "Method Called. arrData.length = " + arrData.length + ", sizeToIterate = " + sizeToIterate);

      for (int i = 0; i < sizeToIterate; i++)
      {
        total = total + arrData[i];
      }

      return total;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return 0;
    }
  }

  /**
   * Method is used to get sum of all elements in Double array.
   * 
   * @param arrData
   * @return
   */
  public double getSumOfDoubleArrayElements(double[] arrData, int sizeToIterate)
  {
    try
    {
      double total = 0;

      if (arrData == null)
        return 0.0;

      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "getSumOfDoubleArrayElements", "", "", "Method Called. arrData.length = " + arrData.length + ", sizeToIterate = " + sizeToIterate);

      for (int i = 0; i < sizeToIterate; i++)
      {
        total = total + arrData[i];
      }

      return total;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return 0.0;
    }
  }

  /**
   * Method is used to get Average sum of all elements in array.
   * 
   * @param arrData
   * @return
   */
  public double getAvgSum(double[] arrData, int[] arrCounts, int sizeToIterate)
  {
    try
    {
      double total = 0;

      if (arrData == null || arrCounts == null)
        return 0;

      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "getAvgSum", "", "", "Method Called. arrData.length = " + arrData.length + ", arrCounts.len = " + arrCounts.length + ", sizeToIterate = " + sizeToIterate);

      for (int i = 0; i < sizeToIterate; i++)
      {
        total = total + arrData[i] * arrCounts[i];
      }

      return total;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return 0;
    }
  }

  /**
   * The method calculate sum square for Derived Graph.
   * 
   * @param arrData
   * @return
   */
  public double getAvgSumSqrForDerived(double[] arrData, double avg, int sizeToIterate)
  {
    try
    {
      double sumSqr = 0;

      if (arrData == null)
        return 0;

      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "getAvgSumSqrForDerived", "", "", "Method Called. arrData.length = " + arrData.length + ", sizeToIterate = " + sizeToIterate);

      for (int i = 0; i < sizeToIterate; i++)
      {
        double diff = avg - arrData[i];
        sumSqr = sumSqr + (diff * diff);
      }

      return sumSqr;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return 0;
    }
  }

  /**
   * To Convert the double value up to specified number of decimal digit.
   * 
   * @param value
   * @param numDigit
   * @return
   */
  public static double addDecimalDigits(double value, int numDigit)
  {
    try
    {
      String number = value + ""; // convert double to string
      int index = number.indexOf(".");
      String totalDigitAfterDecimal = "#.#"; // for calculating no. of digits after decimal

      if (index != -1 && value != 0) // check if number is Integer type
      {
        String checkValueAfterDecimal = number.substring(number.indexOf(".") + 1, number.length());
        if (checkValueAfterDecimal.equals("0"))
        {
          if (number.trim().startsWith("-"))
            numDigit = numDigit + number.length() - 3;
          else
            numDigit = numDigit + number.length() - 2;

          // number = String.format("%."+numDigit+"g%n", value);
          number = String.format("%." + numDigit + "g", value);
          return Double.parseDouble(number);
        }
      }
      if (numDigit > 0)
      {
        for (int i = 1; i < numDigit; i++)
          // Calculating decimal digits.
          totalDigitAfterDecimal = totalDigitAfterDecimal + "#";
      }
      else
        totalDigitAfterDecimal = "";

      DecimalFormat df = new DecimalFormat(totalDigitAfterDecimal);
      df.setMinimumFractionDigits(numDigit);
      number = df.format(value);

      return Double.parseDouble(number);
    }
    catch (Exception e)
    {
      // e.printStackTrace();
      return 0.0;
    }
  }

  /**
   * The method Increase the size of 1d int array with specified number of size.
   * 
   * @param arrSampleData
   * @param sizeToIncrease
   * @return
   */
  public static int[] increase1DIntArraySize(int arrSampleData[], int sizeToIncrease)
  {
    try
    {
      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "increase1DIntArraySize", "", "", "Method Called. sizeToIncrease = " + sizeToIncrease);

      int newColSize = arrSampleData.length + sizeToIncrease;
      int[] newSampleArray = new int[newColSize];

      for (int i = 0; i < arrSampleData.length; i++)
      {
        newSampleArray[i] = arrSampleData[i];
      }

      return newSampleArray;

    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "increase1DIntArraySize", "", "", "Exception - ", e);
      return arrSampleData;
    }
  }

  /**
   * The method Increase the size of 1d int array with specified number of size.
   * 
   * @param arrSampleData
   * @param sizeToIncrease
   * @return
   */
  public static double[] increase1DDoubleArraySize(double[] arrSampleData, int sizeToIncrease)
  {
    try
    {
      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "increase1DDoubleArraySize", "", "", "Method Called. sizeToIncrease = " + sizeToIncrease);

      int newColSize = arrSampleData.length + sizeToIncrease;
      double[] newSampleArray = new double[newColSize];

      for (int i = 0; i < arrSampleData.length; i++)
      {
        newSampleArray[i] = arrSampleData[i];
      }

      return newSampleArray;

    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "increase1DDoubleArraySize", "", "", "Exception - ", e);
      return arrSampleData;
    }
  }

  public static int getDebugLevelFromConfig()
  {
    try
    {
      String debugLevel = Config.getValue("netstorm.execution.debugLevel");
      return Integer.parseInt(debugLevel);
    }
    catch (Exception e)
    {
      Log.debugLogAlways(className, "getDebugLevelFromConfig", "", "", "Incorrect value for netstorm.execution.debugLevel");
      return 0;
    }
  }

  public static GraphUniqueKeyDTO[] getAllGraphUniqueKeyDTOFromProfile(GraphNames graphNamesObj)
  {
    try
    {
      if (debugLogLevel > 0)
        Log.debugLogAlways(className, "getAllGraphUniqueKeyDTOFromProfile", "", "", "Method Called.");

      ArrayList<GraphUniqueKeyDTO> arrUniqueGraphUniqueKeyDTO = new ArrayList<GraphUniqueKeyDTO>();

      RTGProfileData objRTGProfileData = new RTGProfileData();
      ArrayList profileNames = objRTGProfileData.getAllEGPFile();
      for (int i = 0; i < profileNames.size(); i++)
      {
        String profileName = profileNames.get(i).toString() + ".egp";
        try
        {
          if (debugLogLevel > 0)
            Log.debugLogAlways(className, "getAllGraphUniqueKeyDTOFromProfile", "", "", "Going to read profile = " + profileName);
          String profilePath = Config.getWorkPath() + "/webapps/profiles/" + profileName;
          BufferedReader reader = new BufferedReader(new FileReader(profilePath));
          String line = null;
          while ((line = reader.readLine()) != null)
          {
            if (line.startsWith("PANEL"))
            {
              try
              {
                String[] strTempRcrd = rptUtilsBean.strToArrayData(line, "|");
                if (Integer.parseInt(strTempRcrd[2]) == 2)
                {
                  String derivedFormula = strTempRcrd[strTempRcrd.length - 1];
                  String[] arrSingleFormulas = DerivedData.getTokens(derivedFormula);
                  for (int j = 0; j < arrSingleFormulas.length; j++)
                  {
                    String singleFormula = arrSingleFormulas[j];
                    try
                    {
                      Integer.parseInt(singleFormula);
                    }
                    catch (Exception e)
                    {
                      try
                      {
                        singleFormula = singleFormula.replace("{", "");
                        singleFormula = singleFormula.replace("}", "");
                        singleFormula = singleFormula.replace("(", "");
                        singleFormula = singleFormula.replace(")", "");
                        singleFormula = singleFormula.replace("[", "");
                        singleFormula = singleFormula.replace("]", "");
                        singleFormula = ParseDerivedExp.replaceDerivedFunName(singleFormula);
                        
                        String[] arrGroupIdGraphIdVectorName = DerivedData.convertStringToStrinArray(singleFormula);

                        int groupId = Integer.parseInt(arrGroupIdGraphIdVectorName[0]);
                        int graphId = Integer.parseInt(arrGroupIdGraphIdVectorName[1]);
                        String vectorName = arrGroupIdGraphIdVectorName[2].trim();

                        int graphDataIndex = graphNamesObj.getGraphDataIdxByGrpIdGraphIdVecName(groupId, graphId, vectorName);
                        if (graphDataIndex == -1)
                          continue;

                        GraphUniqueKeyDTO graphUniqueKeyDTO = new GraphUniqueKeyDTO(groupId, graphId, vectorName);
                        graphUniqueKeyDTO.setGraphDataIndex(graphDataIndex);

                        if (!arrUniqueGraphUniqueKeyDTO.contains(graphUniqueKeyDTO))
                          arrUniqueGraphUniqueKeyDTO.add(graphUniqueKeyDTO);
                      }
                      catch (Exception e2)
                      {
                        Log.errorLog(className, "getAllGraphUniqueKeyDTOFromProfile", "", "", "Exception - " + e2);
                        continue;
                      }
                    }
                  }
                }
                else
                {
                  try
                  {
                    String[][] arrInfoTemp = rptUtilsBean.rptInfoToArr(strTempRcrd[strTempRcrd.length - 1]);
                    String[] grpIDLoadProfileMergedGrp = arrInfoTemp[0];
                    String[] graphIDLoadProfileMergedGrp = arrInfoTemp[1];
                    String[] vectorNameLoadProfileMergedGrp = arrInfoTemp[3];
                    for (int k = 0; k < grpIDLoadProfileMergedGrp.length; k++)
                    {
                      int groupId = Integer.parseInt(grpIDLoadProfileMergedGrp[k].trim());
                      int graphId = Integer.parseInt(graphIDLoadProfileMergedGrp[k].trim());
                      String vectorName = vectorNameLoadProfileMergedGrp[k].trim();
                      int graphDataIndex = graphNamesObj.getGraphDataIdxByGrpIdGraphIdVecName(groupId, graphId, vectorName);
                      if (graphDataIndex == -1)
                        continue;

                      GraphUniqueKeyDTO graphUniqueKeyDTO = new GraphUniqueKeyDTO(groupId, graphId, vectorName);
                      graphUniqueKeyDTO.setGraphDataIndex(graphDataIndex);

                      if (!arrUniqueGraphUniqueKeyDTO.contains(graphUniqueKeyDTO))
                        arrUniqueGraphUniqueKeyDTO.add(graphUniqueKeyDTO);
                    }
                  }
                  catch (Exception ex)
                  {
                    Log.errorLog(className, "getAllGraphUniqueKeyDTOFromProfile", "", "", "Error in parsing panel info = " + line);
                    continue;
                  }
                }
              }
              catch (Exception ex)
              {
                Log.stackTraceLog(className, "getAllGraphUniqueKeyDTOFromProfile", "", "", "Panel in profile is not saved in correct format - " + line, ex);
                continue;
              }
            }
          }
        }
        catch (Exception e)
        {
          Log.errorLog(className, "getAllGraphUniqueKeyDTOFromProfile", "", "", "profileName = " + profileName + " not found.");
        }
      }

      return convertArrayListToGraphUniqueKetDTOArray(arrUniqueGraphUniqueKeyDTO);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getAllGraphUniqueKeyDTOFromProfile", "", "", "Exception - ", e);
      return null;
    }
  }

  public static GraphUniqueKeyDTO[] convertArrayListToGraphUniqueKetDTOArray(ArrayList<GraphUniqueKeyDTO> arrSrc)
  {
    GraphUniqueKeyDTO[] arrResults = new GraphUniqueKeyDTO[arrSrc.size()];
    for (int i = 0; i < arrResults.length; i++)
    {
      arrResults[i] = arrSrc.get(i);
    }
    return arrResults;
  }

  public static GraphUniqueKeyDTO[] appendGraphUniqueKeyDTOArray(GraphUniqueKeyDTO[] src1, GraphUniqueKeyDTO[] src2)
  {
    int n = src1.length + src2.length;
    GraphUniqueKeyDTO[] arrSrc = new GraphUniqueKeyDTO[n];
    int count = 0;
    for (int i = 0; i < src1.length; i++)
    {
      arrSrc[count] = src1[i];
      count++;
    }

    for (int i = 0; i < src2.length; i++)
    {
      arrSrc[count] = src2[i];
      count++;
    }

    return arrSrc;
  }

  public static String convertDoubleArrayToString(double[] arrSrc, String delimeter)
  {
    String result = "";
    for (int i = 0; i < arrSrc.length; i++)
    {
      if (i != arrSrc.length - 1)
        result += arrSrc[i] + delimeter;
      else
        result += arrSrc[i];
    }

    return result;
  }

  // Ravi - This method is temporary, We will delete this method after implementing code based on GraphUniqueKeyDTO
  public static GraphUniqueKeyDTO[] convertArrayListToGraphUniqueKeyDTO(GraphNames graphNames, ArrayList<String> arrGGV)
  {
    if (arrGGV != null)
    {
      GraphUniqueKeyDTO[] arrGraphUniqueKeyDTO = new GraphUniqueKeyDTO[arrGGV.size()];
      for (int i = 0; i < arrGraphUniqueKeyDTO.length; i++)
      {
        try
        {
          String strGrpIdGraphIdVectName = arrGGV.get(i);
          String[] arrGrpIdGraphId = rptUtilsBean.strToArrayData(strGrpIdGraphIdVectName, "|");
          int groupId = Integer.parseInt(arrGrpIdGraphId[0].trim());
          int graphId = Integer.parseInt(arrGrpIdGraphId[1].trim());
          String vectorName = arrGrpIdGraphId[2].trim();
          int graphDataIndex = graphNames.getGraphDataIdxByGrpIdGraphIdVecName(groupId, graphId, vectorName);
          if (graphDataIndex == -1)
            continue;

          GraphUniqueKeyDTO graphUniqueKeyDTO = new GraphUniqueKeyDTO(groupId, graphId, vectorName);
          graphUniqueKeyDTO.setGraphDataIndex(graphDataIndex);

          arrGraphUniqueKeyDTO[i] = graphUniqueKeyDTO;
        }
        catch (Exception e)
        {
          Log.stackTraceLog(className, "convertArrayListToGraphUniqueKeyDTO", "", "", "Exception - ", e);
        }
      }

      return arrGraphUniqueKeyDTO;
    }
    else
    {
      Log.errorLog(className, "GraphUniqueKeyDTO", "", "", "arrGraphNumbers is null.");
      return null;
    }
  }

  public static int getGraphDataIndexByGraphUniqueKeyDTO(GraphUniqueKeyDTO graphUniqueKeyDTO, GraphNames graphNames, ArrayList<VectorMappingDTO> arrVectorMappingList)
  {
    try
    {
      int graphDataIndex = graphNames.getGraphDataIdxByGrpIdGraphIdVecName(graphUniqueKeyDTO.getGroupId(), graphUniqueKeyDTO.getGraphId(), graphUniqueKeyDTO.getVectorName());
      graphUniqueKeyDTO.setGraphDataIndex(graphDataIndex);

      if (graphDataIndex == -1)
      {
        if (arrVectorMappingList != null)
        {
          String mappedVectorName = rptUtilsBean.getVectorNameFromVectorMapping(arrVectorMappingList, graphUniqueKeyDTO.getGroupId(), graphUniqueKeyDTO.getVectorName());
          graphDataIndex = graphNames.getGraphDataIdxByGrpIdGraphIdVecName(graphUniqueKeyDTO.getGroupId(), graphUniqueKeyDTO.getGraphId(), mappedVectorName);
          if (graphDataIndex != -1)
            graphUniqueKeyDTO.setVectorName(mappedVectorName);
        }
      }

      return graphDataIndex;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getGraphDataIndexByGraphUniqueKeyDTO", "", "", "Exception - ", e);
      return -1;
    }
  }
  
  /**
   * Method is used to check if graph sample is empty or not by checking the no data identity which is -0.0.
   * @param graphData
   * @return
   */
  public static boolean isEmptySample(double graphData)
  {
    try
    {
      if((graphData + "").equals("-0.0"))
	return true;
      
      return false;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "isEmptySample", "", "", "Exception on checking empty graph sample availability = " + e.getMessage() , e);
      return false;
    }
  }

  public static void main(String[] args)
  {
    System.out.println("Number = " + TimeBasedDataUtils.addDecimalDigits(0.65545, 4));
    
    double arr[] = {-0.0, 0.1, 5 ,1 , 3, 6, -0.0, 0.2, 3};
    
    TimeBasedDataUtils timeBasedDataUtils = new TimeBasedDataUtils();
    System.out.println("Min Data = " + timeBasedDataUtils.getMinValueFromArray(arr, arr.length));
  }
}

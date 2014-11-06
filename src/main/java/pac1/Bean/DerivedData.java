/*--------------------------------------------------------------------
  @Name    : DerivedData.java
  @Author  : Prabhat
  @Purpose : Bean for calculating derived data
  @Modification History:
    Initial Version --> 04/29/09 --> Prabhat

----------------------------------------------------------------------*/

package pac1.Bean;

import java.util.*;

import pac1.Bean.rptUtilsBean;
import pac1.Bean.GraphName.GraphNameUtils;
import pac1.Bean.GraphName.GraphNames;

public class DerivedData implements java.io.Serializable
{
  private static String className = "DerivedData";

  private String expression = "";

  private ArrayList<String> arrListVariable = new ArrayList<String>();
  private ArrayList<String> arrListVariableValue = new ArrayList<String>();
  private ArrayList<GraphUniqueKeyDTO> arrListIdx = new ArrayList<GraphUniqueKeyDTO>();
  private ArrayList arrListDataType = new ArrayList();
  /*
   * below arraylist can contain either of the below values 0 min 1 max 2 none
   */
  private ArrayList<Integer> type = new ArrayList<Integer>();

  private ArrayList<VectorMappingDTO> vectorMappingList = new ArrayList<VectorMappingDTO>();

  // Taking TestRunData Reference.
  private TestRunData testRunData = null;

  public DerivedData(String expression)
  {
    this.expression = expression;
  }

  // this function expand expression by vector type
  /**
   * this function expand expression by vector type
   * 
   * Earlier output -- (7.2.1xx + 7.2.2xx)/2 for input -- 7.2.1xx;2xx Current Output -- 7.2.[1xx,2xx] for input -- 7.2.[1xx,2xx]
   * 
   * 
   **/
  private String expandExpByVectorType(String tmpExpression, GraphNames graphNames, HashMap hmCounter)
  {
    try
    {
      Log.debugLog(className, "expandExpByVectorType", "", "", "Method called. Derived graph tmp expression = " + tmpExpression);
      Vector vecToken = new Vector();
      Vector vecTokenReplacedString = new Vector();
      String[] arrVecNames = null;

      String[] stTemp = getTokens(tmpExpression);
      // StringTokenizer stTemp = new StringTokenizer(tmpExpression, "(+-*/)");

      for (int j = 0; j < stTemp.length; j++)
      {
        String strToken = stTemp[j].trim();
        String[] arrTemp = rptUtilsBean.strToArrayData(strToken, ".");

        String replacedExpression = "";
        // check for greater than 2 because server name can also have ".", like 192.168.18.106
        if (arrTemp.length > 2)
        {
          
          arrTemp[0] = ParseDerivedExp.replaceDerivedFunName(arrTemp[0]);
          
          int groupId = Integer.parseInt(arrTemp[0]);
          int graphId = Integer.parseInt(arrTemp[1]);
          String vectorString = "";

          // vector name also have "." like 192.168.18.1
          for (int i = 2; i < arrTemp.length; i++)
          {
            /**
             * Below condition is put so as to convert vector name from hashFormat to its original name
             * 
             */

            String strVectorName = arrTemp[i];
            /*
             * if(strVectorName.startsWith("#") && hmCounter != null && hmCounter.containsKey(strVectorName)) strVectorName = hmCounter.get(strVectorName).toString();
             */

            strVectorName = rptUtilsBean.replace(strVectorName, "[", "");
            strVectorName = rptUtilsBean.replace(strVectorName, "]", "");
            arrTemp[i] = strVectorName;

            if (vectorString.equals(""))
              vectorString = arrTemp[i];
            else
              vectorString = vectorString + "." + arrTemp[i];
          }

          // get the graph & group type(scalar or vector)
          String[] arrInfo = rptUtilsBean.strToArrayData(graphNames.getGroupTypeGraphTypeAndNumOfIndices(groupId, graphId), "|");

          if (arrInfo.length < 4) // If Report is not available in the test run
          {
            Log.debugLog(className, "expandExpByVectorType", "", "", "Report is Ignored, not present in the Test Run. Grp Id/Graph Id = (" + groupId + "/" + graphId + ")");
            continue;
          }

          // If Group is Vector
          if (arrInfo[0].equals("vector"))
            arrVecNames = graphNames.getNameOfGroupIndicesByGroupId(groupId);
          // If Graph is Vector
          else if (arrInfo[2].equals("vector"))
            arrVecNames = graphNames.getNameOfIndicesByGroupIdAndGraphId(groupId, graphId);

          int totalVecCount = 0;
          if (arrTemp[2].equals("All"))
          {
            totalVecCount = arrVecNames.length;
            for (int i = 0; i < arrVecNames.length; i++)
            {
              String vecStr = arrVecNames[i];
              if (!vecStr.equals("NA"))
                vecStr = "[" + vecStr + "]";

              if (replacedExpression.equals(""))
                replacedExpression = groupId + "." + graphId + "." + vecStr;
              else
                replacedExpression = replacedExpression + " + " + groupId + "." + graphId + "." + vecStr;
            }
          }
          else
          {
            arrTemp = rptUtilsBean.strToArrayData(vectorString, ";");
            totalVecCount = arrTemp.length;
            for (int i = 0; i < arrTemp.length; i++)
            {
              String vecStr = arrTemp[i];
              if (!vecStr.equals("NA"))
                vecStr = "[" + vecStr + "]";

              if (replacedExpression.equals(""))
                replacedExpression = groupId + "." + graphId + "." + vecStr;
              else
                replacedExpression = replacedExpression + " + " + groupId + "." + graphId + "." + vecStr;
            }
          }
          // to replace larger string first
          vecToken.add(strToken); // this token is in # format
          if ((arrInfo[0].equals("vector") || arrInfo[2].equals("vector")) && (arrTemp.length > 1))
            vecTokenReplacedString.add("(" + replacedExpression + ")/" + totalVecCount);
          else
            vecTokenReplacedString.add(replacedExpression);
        }
      }

      int[] tokenLengthArr = new int[vecToken.size()];
      for (int i = 0; i < vecToken.size(); i++)
      {
        tokenLengthArr[i] = (vecToken.get(i).toString()).length();
      }

      boolean replacedFlag[] = new boolean[tokenLengthArr.length];

      Arrays.fill(replacedFlag, false);
      Arrays.sort(tokenLengthArr);

      for (int i = (tokenLengthArr.length - 1); i >= 0; i--)
      {
        for (int jj = 0; jj < vecToken.size(); jj++)
        {
          String tokenReplacedValue = vecTokenReplacedString.get(jj).toString();
          String tokenValue = vecToken.get(jj).toString();

          int tokenLength = tokenValue.length();
          if ((tokenLengthArr[i] == tokenLength) && !replacedFlag[jj])
          {
            Log.debugLog(className, "expandExpByVectorType", "", "", "Expression token value = " + tokenValue + ", converted token value = " + tokenReplacedValue);

            tmpExpression = rptUtilsBean.replace(tmpExpression, tokenValue, tokenReplacedValue);
            replacedFlag[jj] = true;
            break;
          }
        }
      }
      Log.debugLog(className, "expandExpByVectorType", "", "", "Converted derived expression = " + tmpExpression);

      return tmpExpression;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "expandExpByVectorType", "", "", "Exception - ", e);
      return "";
    }
  }

  /**********************************************************************************************
   * This function convert derived graph expression to airthmetic expression Now we are supported only "(+-/*)" operator Input --> Expression --> ((1.2.NA + (2.1.S1)) - 3.1.S1 * 4.1.S1 + 10)/4
   * 
   * Output --> Expression --> ((a1 + (a2)) - a3 * a4 + 10)/4
   ***********************************************************************************************/
  private void convertDerivedExpToMathExp(int[] arrRptDataIdx, GraphNames graphNames)
  {
    try
    {
      Log.debugLog(className, "convertDerivedExpToMathExp", "", "", "Method called. Derived graph expression = " + expression + ", report data index = " + rptUtilsBean.intArrayToList(arrRptDataIdx));
      String[] stTemp = getTokens(expression);
      int i = 1;
      for (int j = 0; j < stTemp.length; j++)
      {
        String strToken = stTemp[j].trim();
        if (strToken == null || strToken.length() == 0)
          continue;

        String[] arrTemp = convertStringToStrinArray(strToken);// rptUtilsBean.strToArrayData(strToken, ".");

        if (arrTemp == null)
          continue;

        // check for greater than 2 because server name can also have ".", like 192.168.18.106
        if (arrTemp.length > 2)
        {
          arrListVariable.add("a" + i);
          arrListVariableValue.add(strToken);

          // converting #Num to its vector name in the array
          String strVectorName = arrTemp[2];
          /*
           * if(strVectorName.startsWith("#") && ParseDerivedExp.hmCounter != null && ParseDerivedExp.hmCounter.containsKey(strVectorName)) strVectorName =
           * ParseDerivedExp.hmCounter.get(strVectorName).toString();
           */
          strVectorName = rptUtilsBean.replace(strVectorName, "[", "");
          strVectorName = rptUtilsBean.replace(strVectorName, "]", "");
          arrTemp[2] = strVectorName;

          int rptGraphDataIdx = getGraphDataIdxByDerivedExp(arrTemp, graphNames);

          int graphDataType = graphNames.getDataTypeNumByGraphUniqueKeyDTO(new GraphUniqueKeyDTO(Integer.parseInt(arrTemp[0]), Integer.parseInt(arrTemp[1]), arrTemp[2]));//getGraphDataTypeByIndex(rptGraphDataIdx);
          
          arrListDataType.add("" + graphDataType);

          for (int idx = 0; idx < arrRptDataIdx.length; idx++)
          {
            Log.debugLog(className, "convertDerivedExpToMathExp", "", "", "Data value, report data index = " + rptGraphDataIdx + ", data index = " + arrRptDataIdx[idx] + ", index = " + idx);

            if (rptGraphDataIdx == arrRptDataIdx[idx])
            {
              Log.debugLog(className, "convertDerivedExpToMathExp", "", "", "Matched Data value, report data index = " + rptGraphDataIdx + ", array data index = " + arrRptDataIdx[idx] + ", index = " + idx);
              
              GraphUniqueKeyDTO graphUniqueKeyDTO = new GraphUniqueKeyDTO(Integer.parseInt(arrTemp[0]), Integer.parseInt(arrTemp[1]), arrTemp[2]);
              graphUniqueKeyDTO.setGraphDataIndex(idx);
              arrListIdx.add(graphUniqueKeyDTO);
              break;
            }
          }
          Log.debugLog(className, "convertDerivedExpToMathExp", "", "", "Data added to list. variable name = a" + i + ", variable value = " + strToken + ", report data index = " + rptGraphDataIdx + ", data index = " + arrListIdx.get(arrListIdx.size() - 1));
          i++;
        }
      }

      int[] tokenLengthArr = new int[arrListVariableValue.size()];
      for (i = 0; i < arrListVariableValue.size(); i++)
      {
        tokenLengthArr[i] = (arrListVariableValue.get(i)).length();
      }

      boolean replacedFlag[] = new boolean[tokenLengthArr.length];

      Arrays.fill(replacedFlag, false);
      Arrays.sort(tokenLengthArr);

      for (i = (tokenLengthArr.length - 1); i >= 0; i--)
      {
        for (int jj = 0; jj < arrListVariableValue.size(); jj++)
        {
          String variable = arrListVariable.get(jj);
          String variableValue = arrListVariableValue.get(jj);

          int tokenLength = variableValue.length();
          if ((tokenLengthArr[i] == tokenLength) && !replacedFlag[jj])
          {
            Log.debugLog(className, "convertDerivedExpToMathExp", "", "", "derived graph variable name = " + variableValue + ", converted variable name = " + variable);

            expression = rptUtilsBean.replace(expression, variableValue, variable);
            replacedFlag[jj] = true;
            break;
          }
        }
      }

      Log.debugLog(className, "convertDerivedExpToMathExp", "", "", "Converted math expression = " + expression);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "convertDerivedExpToMathExp", "", "", "Exception - ", e);
    }
  }

  /*********************************************************************************************
   * This is the main function that calculate derived data on the basis of expression and return calculated derived data. Input --> double[][] arrGraphData --> array of graph data based on index int[]
   * arrRptDataIdx --> array of report data index GraphNames graphNames --> object of graph names
   * 
   * OutPut --> double[] --> array of calculated derived data by derived expression
   **********************************************************************************************/

  public double[] calcAndGetDerivedDataArray(double[][] arrGraphData, int[] arrRptDataIdx, GraphNames graphNames)
  {
    try
    {
      Log.debugLog(className, "calcAndGetDerivedDataArray", "", "", "Method called.");
      convertDerivedExpToMathExp(arrRptDataIdx, graphNames);
      Log.debugLog(className, "calcAndGetDerivedDataArray", "", "", "Derived expressions list = " + arrListVariableValue + ", converted variables list = " + arrListVariable + ", graph data index list = " + arrListIdx + ", data type list = " + arrListDataType);

      MathEvaluator mathEvaluator = new MathEvaluator(expression);

      double[] arrDerivedData = new double[arrGraphData[0].length];

      int count = 0;
      // Sequence started from 1, so must -1 here
      for (int idx = 0; idx < arrGraphData[0].length; idx++)
      {
        for (int i = 0; i < arrListVariable.size(); i++)
        {
          GraphUniqueKeyDTO graphUniqueKeyDTO= arrListIdx.get(i);
          String variable = arrListVariable.get(i);
          double varValue = arrGraphData[graphUniqueKeyDTO.getGraphDataIndex()][idx];

          mathEvaluator.addVariable(variable, varValue);
        }
        arrDerivedData[count] = (mathEvaluator.getValue()).doubleValue();
        count++;
      }

      Log.debugLog(className, "calcAndGetDerivedDataArray", "", "", "Derived data value = " + rptUtilsBean.doubleArrayToList(arrDerivedData));

      return arrDerivedData;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "calcAndGetDerivedDataArray", "", "", "Exception - ", e);
      return null;
    }
  }
  
  /**
   * Method is used to process time based data to generate Derived Data.
   * @param arrGraphData
   * @param arrRptDataIdx
   * @param graphNames
   * @return
   */
  public double[] processAndGetDerivedDataByTimeBasedData(TimeBasedTestRunData timeBasedTestRunDataObj, LinkedHashMap<Integer, GraphUniqueKeyDTO> hmGraphInfo, int []arrRptDataIdx, GraphNames graphNames)
  {
    try
    {
      Log.debugLogAlways(className, "processAndGetDerivedDataByTimeBasedData", "", "", "Method called.");   
      convertDerivedExpToMathExp(arrRptDataIdx, graphNames);
      Log.debugLogAlways(className, "processAndGetDerivedDataByTimeBasedData", "", "", "Derived expressions list = " + arrListVariableValue + ", converted variables list = " + arrListVariable + ", graph data index list = " + arrListIdx + ", data type list = " + arrListDataType);

      MathEvaluator mathEvaluator = new MathEvaluator(expression);
      double[] arrDerivedData = new double[timeBasedTestRunDataObj.getDataItemCount()];
      int count = 0;
      
      for (int idx = 0; idx < timeBasedTestRunDataObj.getDataItemCount(); idx++)
      {
        for (int i = 0; i < arrListVariable.size(); i++)
        {
          GraphUniqueKeyDTO graphUniqueKeyDTO  = arrListIdx.get(i);     
          GraphUniqueKeyDTO graphUniqueKeyDTOObj = hmGraphInfo.get(arrRptDataIdx[graphUniqueKeyDTO.getGraphDataIndex()]);    
          TimeBasedDTO timeBasedDTOObj = timeBasedTestRunDataObj.getTimeBasedDTO(graphUniqueKeyDTOObj);     
          
          String variable = arrListVariable.get(i);
          double varValue = timeBasedDTOObj.getArrGraphSamplesData()[idx];
          mathEvaluator.addVariable(variable, varValue);
        }
        arrDerivedData[count] = (mathEvaluator.getValue()).doubleValue();
        count++;
      }

      Log.debugLog(className, "processAndGetDerivedDataByTimeBasedData", "", "", "Derived data value = " + rptUtilsBean.doubleArrayToList(arrDerivedData));

      return arrDerivedData;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "processAndGetDerivedDataByTimeBasedData", "", "", "Exception - ", e);
      return null;
    }
  }
  
  /**
   * Method is used to process time based data to generate Derived Parameters.
   * @param arrGraphData
   * @param arrRptDataIdx
   * @param graphNames
   * @return
   */
  public void processDerivedFormula(TimeBasedTestRunData timeBasedTestRunDataObj, LinkedHashMap<Integer, GraphUniqueKeyDTO> hmGraphInfo, int []arrRptDataIdx, GraphNames graphNames)
  {
    try
    {
      Log.debugLogAlways(className, "processDerivedFormula", "", "", "Method called.");   
      convertDerivedExpToMathExp(arrRptDataIdx, graphNames);
      Log.debugLogAlways(className, "processDerivedFormula", "", "", "Derived expressions list = " + arrListVariableValue + ", converted variables list = " + arrListVariable + ", graph data index list = " + arrListIdx + ", data type list = " + arrListDataType);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "processDerivedFormula", "", "", "Exception - ", e);
    }
  }
  
  /**
   * Return the Expression of Derived Graph.
   * @return
   */
  public String getExpression() 
  {
    return expression;
  }

  /**
   * Return the Variables used to parse Derived Graph.
   * @return
   */
  public ArrayList<String> getArrListVariable() 
  {
    return arrListVariable;
  }

  /**
   * Return the array of list Index.
   * @return
   */
  public ArrayList<GraphUniqueKeyDTO> getArrListIdx() 
  {
    return arrListIdx;
  }

  // this function return graph data idx by each tokens
  private int getGraphDataIdxByDerivedExp(String[] arrTemp, GraphNames graphNames)
  {
    try
    {
      Log.debugLog(className, "getGraphDataIdxByDerivedExp", "", "", "Method called.");
      int groupId = -1;
      int graphId = -1;
      String vecName = "";

      for (int i = 0; i < arrTemp.length; i++)
      {
        if (i == 0)
          groupId = Integer.parseInt(arrTemp[i]);
        else if (i == 1)
          graphId = Integer.parseInt(arrTemp[i]);
        else
        {
          if (vecName.equals(""))
            vecName = arrTemp[i];
          else
            vecName = vecName + "." + arrTemp[i];
        }
      }
      vecName = vecName.replace("[", "");
      vecName = vecName.replace("]", "");

      int graphDataIndex = graphNames.getGraphDataIdxByGrpIdGraphIdVecName(groupId, graphId, vecName);

      if (graphDataIndex != -1)
      {
        return (graphDataIndex);
      }
      else
      {
        if (vectorMappingList == null || vectorMappingList.size() == 0)
          vectorMappingList = rptUtilsBean.getVectorNameFromMappedFile("VectorMapping.dat");

        // Getting Vector Name From Mapping File.
        vecName = rptUtilsBean.getVectorNameFromVectorMapping(vectorMappingList, groupId, vecName);
        graphDataIndex = graphNames.getGraphDataIdxByGrpIdGraphIdVecName(groupId, graphId, vecName);
        
        if(graphDataIndex == -1)
          Log.errorLog(className, "genDerivedGraph", "", "", "Graph data index is -1 for groupId = " + groupId + ", graphId = " + graphId + ", vecName = " + vecName);
       
        return graphDataIndex;
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getGraphDataIdxByDerivedExp", "", "", "Exception - ", e);
      return -1;
    }
  }

  // this function return true if expression is compatible with this TR else return false
  public boolean chkExpIsCompatible(GraphNames graphNames)
  {
    try
    {
      Log.debugLog(className, "chkExpIsCompatible", "", "", "1111 Method called. Derived expression = " + expression + " , ParseDerivedExp.hmCounter = null");
      expression = expandExpByVectorType(expression, graphNames, null);// Passing hmCounter as null
      
      String[] stTemp = getTokens(expression);
      boolean result = false;

      for (int j = 0; j < stTemp.length; j++)
      {
        String strToken = stTemp[j].trim();
        strToken = strToken.replace("{", "");
        strToken = strToken.replace("}", "");
        strToken = strToken.replace("(", "");
        strToken = strToken.replace(")", "");
        strToken = strToken.replace("[", "");
        strToken = strToken.replace("]", "");
        String[] arrTemp = rptUtilsBean.strToArrayData(strToken, ".");
        // check for greater than 2 because server name can also have ".", like 192.168.18.106
        if (arrTemp.length > 2)
        {
          // converting #Num to its vector name in the array
          String strVectorName = arrTemp[2];
          /*
           * if(strVectorName.startsWith("#") && ParseDerivedExp.hmCounter != null && ParseDerivedExp.hmCounter.containsKey(strVectorName)) strVectorName =
           * ParseDerivedExp.hmCounter.get(strVectorName).toString();
           */
          strVectorName = rptUtilsBean.replace(strVectorName, "[", "");
          strVectorName = rptUtilsBean.replace(strVectorName, "]", "");
          arrTemp[2] = strVectorName;

          int graphDataIdx = getGraphDataIdxByDerivedExp(arrTemp, graphNames);
          if (graphDataIdx != -1)
            result = true;
          else
          {
            result = false;
            break;
          }
        }
      }
      Log.debugLog(className, "chkExpIsCompatible", "", "", "Method called. Result = " + result);
      return result;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "chkExpIsCompatible", "", "", "Exception - ", e);
      return false;
    }
  }

  // this function is to get the data type of derived expression, if all reports that are used in derived expression have cummulative data type than only we treated as cummulative alse we do simple
  // averaging
  public int getDataTypeByExp()
  {
    Log.debugLog(className, "getDataTypeByExp", "", "", "Method called.");

    try
    {
      // Note : If all reports that are used to derived data have cummulative data type than only we treated as cummulative else do simple averaging
      int reportDataType = GraphNameUtils.DATA_TYPE_SAMPLE;
      if ((arrListDataType != null) && (arrListDataType.size() > 0))
      {
        reportDataType = GraphNameUtils.DATA_TYPE_CUMULATIVE;
        for (int i = 0; i < arrListDataType.size(); i++)
        {
          int tempGraphDataType = (int) (Integer.parseInt(arrListDataType.get(i).toString()));
          if (tempGraphDataType != GraphNameUtils.DATA_TYPE_CUMULATIVE)
          {
            reportDataType = GraphNameUtils.DATA_TYPE_SAMPLE;
            break;
          }
        }
      }

      return reportDataType;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "chkExpIsCompatible", "", "", "Exception - ", e);
      return 0;
    }
  }

  // This function is used when VectorName have dot(.) then
  // getting problem to convert string to string array
  // Like 7.2.T.O
  public static String[] convertStringToStrinArray(String inputString)
  {
    try
    {
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

      if (grpId.equals("") || graphId.equals("") || vectName.equals(""))
      {
        Log.debugLogAlways(className, "convertStringToStrinArray", "", "", "Method End. grpId = " + grpId + ", graphId = " + graphId + ", vectName = " + vectName);
        return null;
      }

      String[] arrGrpIdGraphIdVectName = new String[3];
      arrGrpIdGraphIdVectName[0] = grpId;
      arrGrpIdGraphIdVectName[1] = graphId;
      arrGrpIdGraphIdVectName[2] = vectName;
      return arrGrpIdGraphIdVectName;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "convertStringToStrinArray", "", "", "Exception - ", ex);
      return null;
    }
  }

  /*
   * Pydi Input: 1.2.[v1,v2] + 2.3.[v3] Output: str[] = {1.2.[v1,v2], 2.3.[v3]} Process: reads character by character and parse the entire string and returns a final String as 1.2.[v1,v2]#2.3.[v3]
   * return an array by splitting this with #
   */
  public static String[] getTokens(String expression)
  {
    Log.debugLog("DerivedData", "getTokens", "", "", "Method Called for expression = " + expression);
    try
    {
      char ch;
      int dotCount = 0;
      boolean startBracketFound = false;
      boolean isDigitFound = false;
      StringBuffer strBuff = new StringBuffer();

      if (expression == null)
        return null;

      for (int i = 0; i < expression.length(); i++)
      {
        ch = expression.charAt(i);

        if ((ch == '(' || ch == ')' || ch == '{' || ch == '}' || ch == ' ') && !startBracketFound)
          continue;

        if ((ch == '+' || ch == '-' || ch == '*' || ch == '/') && !startBracketFound)
        {
          continue;
        }

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
        {
          int j = i;
          while (Character.isDigit(ch))
          {
            isDigitFound = true;
            strBuff.append(ch);
            j++;
            if (j >= expression.length())
              break;
            ch = expression.charAt(j);
          }

          if (ch != '.' && !startBracketFound && isDigitFound)
            strBuff.append("#$");
          else if (ch == '.' && !startBracketFound)
            dotCount++;

          isDigitFound = false;
          i = j;

          if (ch == ']')
          {
            strBuff.append("]#$");
            dotCount = 0;
            startBracketFound = false;
          }
          else
          {
            if ((ch == ')' || ch == '(' || ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == ' ') && !startBracketFound)
              continue;
            strBuff.append(ch);
          }
        }
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
      Log.errorLog("DerivedData", "getTokens", "", "", "Exception : " + expression);
      return null;
    }
  }

  // this function is to convert derived graph expression to math expression
  public String genDerivedExpToMathExp(String derivedGraphExpression, GraphNames graphNames, HashMap hmCounter)
  {
    try
    {
      Log.debugLog(className, "genDerivedExpToMathExp", "", "", "Method called. derivedGraphExpression = " + derivedGraphExpression + ", hmCounter = " + hmCounter);

      // StringTokenizer stTemp = new StringTokenizer(derivedGraphExpression, "(+-*/)");
      String[] stTemp = getTokens(derivedGraphExpression);

      int i = 1;
      int type = 2;// default it is none
      for (int j = 0; j < stTemp.length; j++)
      {
        String strToken = stTemp[j].trim();

        if (strToken == null || strToken.length() == 0)
          continue;

        String realToken = strToken;
        realToken = realToken.replace("{", "");
        realToken = realToken.replace("}", "");
        realToken = realToken.replace("(", "");
        realToken = realToken.replace(")", "");
        realToken = realToken.replace("[", "");
        realToken = realToken.replace("]", "");
        try
        {
          Integer.parseInt(realToken);
        }
        catch (Exception ex)
        {
          String[] arrTemp = convertStringToStrinArray(realToken);

          if (arrTemp == null)
            continue;

          if (arrTemp.length == 3)
          {
            String rptId = arrTemp[0].trim();
            if (rptId.startsWith("MIN_"))
            {
              type = 0;
              rptId = rptId.replace("MIN_", "");
            }
            else if (rptId.startsWith("MAX_"))
            {
              type = 1;
              rptId = rptId.replace("MAX_", "");
            }
            else if (rptId.startsWith("AVG_"))
            {
              type = 2;
              rptId = rptId.replace("AVG_", "");
            }
            else
              type = 3;

            String rptGrpId = arrTemp[1].trim();
            String vectName = arrTemp[2].trim();

            Log.debugLog(className, "genDerivedExpToMathExp", "", "", "vectName = " + vectName);
            vectName = rptUtilsBean.replace(vectName, "[", "");
            vectName = rptUtilsBean.replace(vectName, "]", "");

            Log.debugLog(className, "genDerivedExpToMathExp", "", "", "Group Id = " + rptId + ", Graph Id = " + rptGrpId + ", Vector Name = " + vectName);
            if (rptId.trim().equals("") || rptGrpId.trim().equals("") || vectName.trim().equals(""))
              continue;

            // check for greater than 2 because server name can also have ".", like 192.168.18.106
            if (arrTemp.length > 2)
            {
              arrListVariable.add("a" + i);
              arrListVariableValue.add(strToken);
              derivedGraphExpression = derivedGraphExpression.replace(strToken, "a" + i);
              int graphDataIndex = graphNames.getGraphDataIdxByGrpIdGraphIdVecName(Integer.parseInt(rptId), Integer.parseInt(rptGrpId), vectName);

              if (graphDataIndex == -1)
              {
                int groupId = Integer.parseInt(rptId);
                // Getting Vector Name From Mapping File.
                vectName = rptUtilsBean.getVectorNameFromVectorMapping(vectorMappingList, groupId, vectName);
                graphDataIndex = graphNames.getGraphDataIdxByGrpIdGraphIdVecName(Integer.parseInt(rptId), Integer.parseInt(rptGrpId), vectName);
              }

              Log.debugLog(className, "genDerivedExpToMathExp", "", "", "derivedGraphExpression = " + derivedGraphExpression + ", Group Id = " + rptId + ", Graph Id = " + rptGrpId + ", Vector Name = " + vectName + ", graphDataIndex = " + graphDataIndex);

              if (graphDataIndex == -1)
                continue;
              GraphUniqueKeyDTO graphUniqueKeyDTO = new GraphUniqueKeyDTO(Integer.parseInt(rptId), Integer.parseInt(rptGrpId), vectName);
              graphUniqueKeyDTO.setGraphDataIndex(graphDataIndex);
              
              arrListIdx.add(graphUniqueKeyDTO);
              this.type.add(type);
              i++;
            }
          }
        }
      }

      Log.debugLog(className, "genDerivedExpToMathExp", "", "", "derivedGraphExpression = " + derivedGraphExpression);

      return derivedGraphExpression;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "genDerivedExpToMathExp", "", "", "Exception - ", ex);
      return "";
    }
  }

  // create all derived graph data
  public double[] genAllDerivedGraphData(String derivedExpression, GraphNames graphNames, TestRunData testRunData, HashMap hmCounter, String key)
  {
    try
    {
      Log.debugLog(className, "genAllDerivedGraphData", "", "", "Method Called. derivedExpression = " + derivedExpression + ", key = " + key);

      // Inject test run data object dependency.
      this.testRunData = testRunData;

      if (testRunData != null)
        vectorMappingList = testRunData.getVectorMappingList();

      String mathExpression = genDerivedExpToMathExp(derivedExpression, graphNames, hmCounter).trim();
      Log.debugLog(className, "genAllDerivedGraphData", "", "", "mathExpression = " + mathExpression + ", arrListIdx = " + arrListIdx);

      if (!mathExpression.equals(""))
      {
        TimeBasedTestRunData timeBasedTestRunData = testRunData.getTimeBasedTestRunDataByKey(key);
        if (arrListIdx.size() != 0)
        {
          /*This is used to check derived graph is applied for average*/
          boolean isAvgFlag = false;
          /*keeping the sum of count's of average derived graphs*/
          int countSum = 0;
          
          // in this method we will pass mathExpression like a1 + a2
          MathEvaluator mathEvaluator = new MathEvaluator(mathExpression);
          Log.debugLog(className, "genAllDerivedGraphData", "", "", "dataItemCount = " + timeBasedTestRunData.getDataItemCount());

          double[] arrDerivedData = new double[timeBasedTestRunData.getDataItemCount()];

          for (int idx = 0; idx < timeBasedTestRunData.getDataItemCount(); idx++)
          {
            for (int i = 0; i < arrListVariable.size(); i++)
            {
              GraphUniqueKeyDTO gaphGraphUniqueKeyDTO = arrListIdx.get(i);
              if (gaphGraphUniqueKeyDTO.getGraphDataIndex() == -1)
              {
                Log.errorLog(className, "genAllDerivedGraphData", "", "", "rptIdx = -1");
                continue;
              }

              TimeBasedDTO timeBasedDTO = timeBasedTestRunData.getTimeBasedDTO(gaphGraphUniqueKeyDTO);
              GraphStats graphStats = timeBasedTestRunData.getGraphStatByGraphUniqueKeyDTO(gaphGraphUniqueKeyDTO);
              
              String variable = arrListVariable.get(i);
              double[] arrData = null;

              /**
               * Below code is commented as Right now design for getting Min & Max data is not ready. so in case of Min or Max data we will generate the normal derived graph assuming that we don't
               * have Min or Max keyword in the formula. In case of Min or Max data for the formula, we just need to call below methods
               **/
              // Means Min value
              if (type.get(i) == 0)
                arrData = timeBasedDTO.getArrMinData();
              else if (type.get(i) == 1) // Means Max Value
                arrData = timeBasedDTO.getArrMaxData();
              else if (type.get(i) == 2) // Case of Derived Type Avg Graph.
              {
                isAvgFlag = true;
                countSum = countSum + graphStats.getCount();
              }
              else
                arrData = timeBasedDTO.getArrGraphSamplesData();

              if (arrData == null)
              {
                Log.errorLog(className, "genAllDerivedGraphData", "", "", "Data not available into memory for gaphGraphUniqueKeyDTO = " + gaphGraphUniqueKeyDTO);
                return null;
              }

              double varValue = 0.0;
              if (idx < arrData.length)
              {
            	if(!isAvgFlag)
                  varValue = arrData[idx];
            	else
            	  varValue = arrData[idx] * graphStats.getCount();
              }

              mathEvaluator.addVariable(variable, varValue);
            }

            try
            {
              double dataValue = mathEvaluator.getValue();
              if(isAvgFlag)
                arrDerivedData[idx] = dataValue/countSum;
              else
            	arrDerivedData[idx] = dataValue;
            }
            catch (Exception ex)
            {
              Log.debugLog(className, "genAllDerivedGraphData", "", "", "Error in updating data in derived graph.");
            }
          }

          return arrDerivedData;
        }
        else
        {
          Log.errorLog(className, "genAllDerivedGraphData", "", "", "Error while getting derived graph data.");
          return null;
        }
      }
      else
      {
        Log.errorLog(className, "genAllDerivedGraphData", "", "", "Error while converting derivedExpression to math expression.");
        return null;
      }
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "genAllDerivedGraphData", "", "", "Exception - ", ex);
      return null;
    }
  }

  // for creating baseline data of derived graph
  public double[] genAllDerivedGraphDataForBaseline(String derivedExpression, GraphNames graphNames, HashMap<Integer, double[]> hmDataForBaseline, HashMap hmCounter)
  {
    try
    {
      Log.debugLog(className, "genAllDerivedGraphData", "", "", "Method Called. derivedExpression = " + derivedExpression);
      if (hmDataForBaseline.size() == 0)
      {
        Log.errorLog(className, "genAllDerivedGraphDataForBaseline", "", "", "Size of hmDataForBaseline = 0");
        return null;
      }

      if (testRunData != null)
        vectorMappingList = testRunData.getVectorMappingList();

      int graphDataLength = 0;
      Iterator iterator = hmDataForBaseline.entrySet().iterator();
      while (iterator.hasNext())
      {
        Map.Entry pairs = (Map.Entry) iterator.next();
        double[] arrData = (double[]) pairs.getValue();
        if (graphDataLength <= arrData.length)
          graphDataLength = arrData.length;
      }

      String mathExpression = genDerivedExpToMathExp(derivedExpression, graphNames, hmCounter).trim();
      if (!mathExpression.equals(""))
      {
        // in this method we will pass mathExpression like a1 + a2
        MathEvaluator mathEvaluator = new MathEvaluator(mathExpression);
        int count = 0;
        double[] arrDerivedData = new double[graphDataLength];
        for (int idx = 0; idx < graphDataLength; idx++)
        {
          for (int i = 0; i < arrListVariable.size(); i++)
          {
            GraphUniqueKeyDTO graphUniqueKeyDTO = arrListIdx.get(i);
            String variable = arrListVariable.get(i);
            double[] vectData = null;
            if (hmDataForBaseline.containsKey(graphUniqueKeyDTO.getGraphDataIndex()))
              vectData = hmDataForBaseline.get(graphUniqueKeyDTO.getGraphDataIndex());

            // checking vectData = null
            if (vectData == null)
            {
              Log.errorLog(className, "genAllDerivedGraphDataForBaseline", "", "", "vectData = " + vectData);
              return null;
            }
            if (idx < vectData.length)
            {
              double varValue = vectData[idx];
              mathEvaluator.addVariable(variable, varValue);
            }
            else
            {
              double varValue = 0.0;
              mathEvaluator.addVariable(variable, varValue);
            }
          }
          try
          {
            arrDerivedData[count] = (mathEvaluator.getValue()).doubleValue();
            count++;
          }
          catch (Exception ex)
          {
            Log.errorLog(className, "genAllDerivedGraphData", "", "", "Exception - " + ex);
            arrDerivedData[count] = 0.0;
            count++;
          }
        }
        return arrDerivedData;
      }
      else
      {
        Log.errorLog(className, "genAllDerivedGraphData", "", "", "Error while converting derivedExpression to math expression.");
        return null;
      }
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "genAllDerivedGraphData", "", "", "Exception - ", ex);
      return null;
    }
  }

  /**
   * This method Inject the TestRunData object.
   * 
   * @param testRunData
   */
  public void setTestRunData(TestRunData testRunData)
  {
    this.testRunData = testRunData;
  }

  public static void main(String[] args)
  {
    DerivedData derivedData = new DerivedData("(7.2.All + 7.2.s1,s2 + 1.1.NA)/4");

    GraphNames graphNames = new GraphNames(4151);

    String resultString = derivedData.expandExpByVectorType(derivedData.expression, graphNames, null);

    System.out.println("Resultant String = " + resultString);
  }
}

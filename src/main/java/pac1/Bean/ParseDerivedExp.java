/************************************
 * Name      : ParseDerivedExp.java
 * Author    : Cavisson System Pvt Ltd
 * Purpose   : To parse derived exp
 * Modification History:
 *            : Initial Version - Ravi Sharma 
 * 
 *********************************/
package pac1.Bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import pac1.Bean.GraphName.GraphInfoBean;
import pac1.Bean.GraphName.GraphNames;

public class ParseDerivedExp
{
  private static String className = "ParseDerivedExp";
  private static ArrayList<String> vectorNameList = null;
  /*
   * 'vecList' Added to hold Vector names of the expression
   * 
   * Eg: {A}{B}[1xx-2xx] + {C}{D}[4xx]; List = [1xx-2xx], [4xx]
   */
  private static ArrayList<String> vecList = null;
  public static int debugLevel = 0;
  private static ArrayList<VectorMappingDTO> vectorMappingList = new ArrayList<VectorMappingDTO>();

  /**
   * This method is used to set debug level for logging conditionally
   * 
   */
  public static void setDebugLevelForParseDerivedExpression(int level)
  {
    debugLevel = level;
  }

  private static ArrayList<String> parseDerivedExp(String expression, StringBuffer errMsg, GraphNames gp, boolean isCalledFromTemplate)
  {
    Log.debugLog(className, "parseDerivedExp", "", "", "method called for expression: " + expression + " , isCalledFromTemplate = " + isCalledFromTemplate);
    try
    {
      ArrayList<String> finalDerivedExpList = new ArrayList<String>();
      ArrayList<String> vecNameList = new ArrayList<String>();

      ArrayList<String[]> arrGrpGraphIdsList = new ArrayList<String[]>();
      ArrayList<String> replceTagList = new ArrayList<String>();

      String changedExp = "";
      int expLength = expression.length();
      boolean graphNamePresentInExp = false;

      int type = DerivedGraphInfo.DERIVED_NORMAL_TYPE_GRAPH;
      int isCloseParanthesis = -1;

      boolean groupNameFound = false;
      
      /*Here we are checking if expression passed was to average the data*/
      boolean isAVGDerivedGraph = expression.startsWith("AVG") ? true : false;
      
      /*Here we are checking for sum count*/
      boolean isSUMCOUNTDerivedGraph = expression.trim().startsWith(DerivedGraphInfo.DERIVED_SUM_COUNT_NAME) ? true : false;
      
      /*Here we are checking for sum count*/
      boolean isCOUNTDerivedGraph = expression.trim().startsWith(DerivedGraphInfo.DERIVED_COUNT_NAME) ? true : false;
      
      /*Here we are checking for sum count*/
      boolean isSUMDerivedGraph = expression.trim().startsWith("SUM") ? true : false;
      
      /*Here we are checking for sum count*/
      boolean isMINDerivedGraph = expression.trim().startsWith("MIN") ? true : false;
      
      /*Here we are checking for sum count*/
      boolean isMAXDerivedGraph = expression.trim().startsWith("MAX") ? true : false;
      
      /*Here we are checking for sum count*/
      boolean isMergeInstanceAll = expression.trim().startsWith(DerivedGraphInfo.DERIVED_ALLINSTANCES) ? true : false;
      
      /*we are replacing this with + because for average we only perform addition and divideBy operations*/
      if(isMINDerivedGraph || isMAXDerivedGraph || isAVGDerivedGraph || isSUMCOUNTDerivedGraph || isCOUNTDerivedGraph || isSUMDerivedGraph)
      {
    	expression = getPreParsedFormula(expression);
    	if(expression == null)
    	{
    	  errMsg.append("For AVG one only expression was allowed");
    	  return null;
    	}
      }
      /*This case is for creating merge min/max all instances from the left pane, in this case formula must start with DERIVED_ALLINSTANCES*/
      else if(isMergeInstanceAll)
      {
	expression = expression.replace(DerivedGraphInfo.DERIVED_ALLINSTANCES, "");
	expLength = expression.length();
      }
      
      for (int i = 0; i < expLength; i++)
      {
        if (isCloseParanthesis == -1)
          type = DerivedGraphInfo.DERIVED_NORMAL_TYPE_GRAPH;
        
        if (expression.charAt(i) == '{' && expLength > (i + 1) && expression.charAt(i + 1) != '{')
        {
          // check before curly braces an opertaor should be present or it should be an starting point.
          if (i != 0)
          {
            for (int j = i - 1; j >= 0; j--)
            {
              if (expression.charAt(j) == ' ' || "[{(".contains(expression.charAt(j) + ""))
              {
                if (type != DerivedGraphInfo.DERIVED_NORMAL_TYPE_GRAPH && expression.charAt(j) == '(')
                  isCloseParanthesis++;
                continue;
              }
              else if ("+-*/".contains(expression.charAt(j) + ""))
                break;
              else if (type == DerivedGraphInfo.DERIVED_NORMAL_TYPE_GRAPH)
              {
                errMsg.append("Error: Operator is missing before curly braces.");
                return null;
              }
            }
          }
          
          // Now check for group Name
          String groupName = "";
          groupNameFound = false;
          for (int j = i + 1; j < expLength; j++)
          {
            if (expression.charAt(j) == '{')
            {
              i = j - 1;
              changedExp += "{" + groupName;
              break;
            }
            if (expression.charAt(j) != '}')
              groupName += expression.charAt(j);
            else
            {
              i = j;
              groupNameFound = true;
              break;
            }
          }

          if (groupName.trim().equals("") && groupNameFound)
          {
            Log.debugLog(className, "parseDerivedExp", "", "", "Error: some expression is expected between curly braces");
            errMsg.append("Error: some expression is expected between curly braces");
            return null;
          }
          
          if (isNumeric(groupName.trim()) && groupNameFound)
            changedExp += "{" + groupName + expression.charAt(i);
          else if (groupNameFound)
          {
            int groupId = -2;
            int graphNumber = -1;
            String vecName = "NA";
            groupId = gp.getGroupIdByGroupName(groupName.trim());
            if (groupId < 0)
            {
              if (isMathExpression(groupName, errMsg))
              {
                changedExp += "{" + groupName + expression.charAt(i);
                continue;
              }
              else
                return null;
            }

            String graphName = "";
            i++;
            if (expLength > i)
            {
              if (expression.charAt(i) == ' ')
              {
                for (; i < expLength; i++)
                {
                  if (expression.charAt(i) == ' ')
                    continue;
                  else
                    break;
                }
              }
              if (i < expLength && expression.charAt(i) == '{')
              {
                for (int k = i + 1; k < expLength; k++)
                {
                  if (expression.charAt(k) != '}')
                    graphName += expression.charAt(k);
                  else
                  {
                    i = k;
                    break;
                  }
                }

                if (graphName.trim().equals(""))
                {
                  Log.debugLog(className, "parseDerivedExp", "", "", "Error: graph Name can not be blank at column.");
                  errMsg.append("Error: graph Name can not be blank.");
                  return null;
                }

                // now check for vector Name
                i++;
                // in case of space continue till any thing is found
                if (i < expLength && expression.charAt(i) == ' ')
                {
                  for (; i < expLength; i++)
                  {
                    if (expression.charAt(i) == ' ')
                      continue;
                    else
                      break;
                  }
                }
                if (i < expLength && expression.charAt(i) == '[')
                {
                  vecName = "";
                  for (int k = i + 1; k < expLength; k++)
                  {
                    if (expression.charAt(k) != ']')
                      vecName += expression.charAt(k);
                    else
                    {
                      i = k;
                      break;
                    }
                  }
                  if (vecName.trim().equals(""))
                    vecName = "NA";
                }
                else
                  i--;
                Log.debugLog(className, "parseDerivedExp", "", "", "vecName = " + vecName);
              }
              else
              {
                Log.debugLog(className, "parseDerivedExp", "", "", "Error: Graph Name not found for Group '" + groupName + "'");
                errMsg.append("Error: Graph Name not found for Group '" + groupName + "'");
                return null;
              }

              // get graphId
              int graphId = getGraphId(graphName, groupId + "", gp);
              if (graphId < 0)
              {
                Log.debugLog(className, "parseDerivedExp", "", "", "Error: graphName '" + graphName + "' not found for group name " + groupName);
                errMsg.append("Error: graphName '" + graphName + "' not found for group name " + groupName);
                return null;
              }

              /**
               * This condition is put because we don't need to validate vector name in case when this method is being used for template conditionally. we are making our String of group,graph and
               * vector name in the desired format, adding them in the final changeExp String which will be added in the final arraylist and continue for the length of the input expression.
               */

              if (isCalledFromTemplate)
              {
                String minMaxdata = getFunctionNameFromType(type);
                if (!vecName.equals("NA"))
                {
                  if(isAVGDerivedGraph || isMINDerivedGraph || isMAXDerivedGraph || isSUMCOUNTDerivedGraph || isCOUNTDerivedGraph || isSUMDerivedGraph)
                  {
                    // handle case for all
                    String arrVecName[] = null;
                    if (vecName.trim().equalsIgnoreCase("All"))
                    {
                      arrVecName = gp.getNameOfIndicesByGroupIdAndGraphId(groupId, graphId);
                      if (arrVecName.length == 1 && arrVecName[0].equals("NA"))
                	arrVecName = gp.getNameOfGroupIndicesByGroupId(groupId);
                    }
                    else
                    {
                      arrVecName = vecName.split(",");
                    }

                    for(int vecLen = 0; vecLen < arrVecName.length; vecLen++)
                    {
                      String tmpStr = minMaxdata + groupId + "." + graphId + ".[" + arrVecName[vecLen] + "]";
                      if(vecLen == arrVecName.length - 1)
                	changedExp += tmpStr;
                      else
                	changedExp += tmpStr + " + ";
                    }
                  }
                  else
                    changedExp += minMaxdata + groupId + "." + graphId + "." + "[" + vecName.replaceAll(",", ";") + "]";
                }
                else
                  changedExp += minMaxdata + groupId + "." + graphId + "." + vecName;

                if (graphId < 0)
                {
                  Log.debugLog(className, "parseDerivedExp", "", "", "Error: graphName " + graphName + " and vector name " + vecName + " not found for group name " + groupName);
                  return null;
                }
                else
                  continue;
              }

              // handle case for all
              String arrVecName[] = null;
              if (vecName.trim().equalsIgnoreCase("All"))
              {
                arrVecName = gp.getNameOfIndicesByGroupIdAndGraphId(groupId, graphId);
                if (arrVecName.length == 1 && arrVecName[0].equals("NA"))
                  arrVecName = gp.getNameOfGroupIndicesByGroupId(groupId);
              }
              else
              {
                arrVecName = vecName.split(",");
              }

              String arrGrpGraphIds[] = null;
              if (arrVecName.length > 1)
                arrGrpGraphIds = new String[arrVecName.length];

              if (arrVecName.length == 1 && !arrVecName[0].trim().equals("NA"))
                arrGrpGraphIds = new String[1];

              String arrGraphName[] = gp.getAllGraphNamesByGroupId(groupId);
              int tmpgraphId = -1; // this is compalsary to set -1 due to check
              
              for (int kk = 0; kk < arrVecName.length; kk++)
              {
                if (arrVecName[kk].trim().equals("NA"))
                  tmpgraphId = gp.getGraphDataIdxByGroupIdAndGraphId(groupId, graphId);
                else
                  tmpgraphId = gp.getGraphDataIdxByGrpIdGraphIdVecName(groupId, graphId, arrVecName[kk]);

                Log.debugLog(className, "parseDerivedExp", "", "", "Graph Index = " + tmpgraphId);
                if (tmpgraphId < 0)
                {
                  Log.debugLog(className, "parseDerivedExp", "", "", "Error: graphName " + graphName + " and vector name " + arrVecName[kk] + " not found for group name " + groupName);
                  errMsg.append("Error: graphName '" + graphName + "' and vector name '" + arrVecName[kk] + "' not found for group name " + groupName);
                  return null;
                }

                if (arrVecName.length > 1)
                  arrGrpGraphIds[kk] = arrVecName[kk].trim();

                if (arrVecName.length == 1 && !arrVecName[0].trim().equals("NA"))
                  arrGrpGraphIds[0] = arrVecName[0].trim();
              }
              
              
              String minMaxdata = getFunctionNameFromType(type);
              if (arrVecName.length == 1 && arrVecName[0].trim().equals("NA"))
              {
                String tmpStr = minMaxdata + groupId + "." + graphId + "." + arrVecName[0];
                changedExp += tmpStr;
                Log.debugLog(className, "parseDerivedExp", "", "", "tmpStr = " + tmpStr);
              }
              else if((isAVGDerivedGraph || isMINDerivedGraph || isMAXDerivedGraph || isSUMCOUNTDerivedGraph || isCOUNTDerivedGraph || isSUMDerivedGraph) && !isMergeInstanceAll)
              {
            	for(int vecLen = 0; vecLen < arrVecName.length; vecLen++)
            	{
            	  String tmpStr = minMaxdata + groupId + "." + graphId + ".[" + arrVecName[vecLen] + "]";
                  if(vecLen == arrVecName.length - 1)
            	   changedExp += tmpStr;
                  else
                   changedExp += tmpStr + " + ";
            	}
              }
              else
              {
                changedExp += minMaxdata + groupId + "." + graphId + "." + "Replace" + i + "ThisTag";
                replceTagList.add("Replace" + i + "ThisTag");
                arrGrpGraphIdsList.add(arrGrpGraphIds);
              }

              // now check after group, graph and vector name an operator should be present.
              for (int j = i + 1; j < expression.length(); j++)
              {
                if (expression.charAt(j) == ' ' || expression.charAt(j) == '}' || expression.charAt(j) == ')' || expression.charAt(j) == ']')
                {
                  if (type != DerivedGraphInfo.DERIVED_NORMAL_TYPE_GRAPH && expression.charAt(j) == ')')
                    isCloseParanthesis--;
                  continue;
                }
                else if ("+-*/".contains(expression.charAt(j) + ""))
                  break;
                else
                {
                  errMsg.append("Error: Operator is missing.");
                  return null;
                }
              }
              graphNamePresentInExp = true;
              groupNameFound = false;
            }
            else
            {
              Log.debugLog(className, "parseDerivedExp", "", "", "Error: Graph Name not found for Group '" + groupName + "'");
              errMsg.append("Error: Graph Name not found for Group '" + groupName + "'");
              return null;
            }
          }
        }
        else
        {
          try
          {
            if ((expression.charAt(i) == 'M' || expression.charAt(i) == 'A' || expression.charAt(i) == 'C' || expression.charAt(i) == 'S') && !groupNameFound)
            {
              if (expression.substring(i, i + 3).equals("MIN") || expression.substring(i, i + 3).equals("MAX") || expression.substring(i, i + 3).equals("SUM") || expression.substring(i, i + 3).equals("AVG") || expression.substring(i, i + DerivedGraphInfo.DERIVED_SUM_COUNT_NAME.length()).equals(DerivedGraphInfo.DERIVED_SUM_COUNT_NAME) || expression.substring(i, i + DerivedGraphInfo.DERIVED_COUNT_NAME.length()).equals(DerivedGraphInfo.DERIVED_COUNT_NAME))
              {
        	int[] skipANDTypeValues = skipExpByDerivedFuntionNameLength(i, expression);
        	i = skipANDTypeValues[0];
        	type = skipANDTypeValues[1];
                isCloseParanthesis++;
              }
              continue;
            }
            else if (expression.charAt(i) == ')')
              isCloseParanthesis--;
          }
          catch (Exception exc)
          {
            Log.stackTraceLog(className, "ParseDerivedExp", "", "", "Exception caught - ", exc);
          }

          if (isCloseParanthesis != -1 || type == DerivedGraphInfo.DERIVED_NORMAL_TYPE_GRAPH)
            changedExp += expression.charAt(i);
          if (isCloseParanthesis == -1)
            type = DerivedGraphInfo.DERIVED_NORMAL_TYPE_GRAPH;
        }
      }

      Log.debugLog(className, "parseDerivedExp", "", "", "changedExp = " + changedExp);
      if (changedExp.indexOf("{") > -1)
      {
        errMsg.append("Error: Invalid expression. Curly brace('{') not putted at right place.");
        return null;
      }

      if (changedExp.indexOf("}") > -1)
      {
        errMsg.append("Error: Invalid expression. Curly brace('}') not putted at right place.");
        return null;
      }

      /**
       * Below condition is put to handle the case when we are using this method for template. As we need only single partially encoded expression i.e 2.3.[Vec1,Vec2] + 3.5.[MiscErr] so by here we got
       * the expected string. we add that string in the final arrayList and return that list with size 1 always for template only
       * 
       */

      if (isCalledFromTemplate)
      {
	if(changedExp != null && changedExp.length() != 0 && isAVGDerivedGraph || isMINDerivedGraph || isMAXDerivedGraph || isSUMCOUNTDerivedGraph || isCOUNTDerivedGraph || isSUMDerivedGraph)
	{
	  String str = changedExp.trim();
	  if(str.charAt(str.length() - 1) == ')')
	    str = str.substring(0, str.length() - 1);

	  changedExp = str;
	}
	
        finalDerivedExpList.add(changedExp);
        return finalDerivedExpList;
      }

      if (!graphNamePresentInExp)
      {
        errMsg.append("Error: no graph is present in expression.");
        return null;
      }

      if (arrGrpGraphIdsList.size() > 0)
      {
        ArrayList<String> list1 = null;
        ArrayList<String> VecNameList1 = null;

        for (int i = 0; i < replceTagList.size(); i++)
        {
          list1 = new ArrayList<String>();
          list1.addAll(finalDerivedExpList);
          finalDerivedExpList = new ArrayList<String>();

          VecNameList1 = new ArrayList<String>();
          VecNameList1.addAll(vecNameList);
          vecNameList = new ArrayList<String>();

          for (int j = 0; j < arrGrpGraphIdsList.get(i).length; j++)
          {
            if (i == 0)
            {
              String vecName = arrGrpGraphIdsList.get(i)[j];
              vecNameList.add(vecName);
              /*
               * String key = "#" + counter; hmCounter.put(key, "[" + vecName + "]");
               */
              // counter++;
              String tmpStr = changedExp.replace(replceTagList.get(i), "[" + arrGrpGraphIdsList.get(i)[j] + "]");
              Log.debugLog(className, "parseDerivedExp", "", "", "i = 0 ; tmpStr = " + tmpStr);
              // tmpStr = tmpStr.replace(vecName, key);
              finalDerivedExpList.add(tmpStr);
            }
            else
            {
              for (int k = 0; k < list1.size(); k++)
              {
                String vectName = arrGrpGraphIdsList.get(i)[j];
                String tmpStr = list1.get(k).replace(replceTagList.get(i), "[" + arrGrpGraphIdsList.get(i)[j] + "]");
                Log.debugLog(className, "parseDerivedExp", "", "", "tmpStr = " + tmpStr);
                /*
                 * String key = "#" + counter; hmCounter.put(key, "[" + vectName + "]");
                 */
                // counter++;
                // tmpStr = tmpStr.replace(vectName, key);
                finalDerivedExpList.add(tmpStr);
                String vecName = VecNameList1.get(k) + "," + arrGrpGraphIdsList.get(i)[j];
                vecNameList.add(vecName);
              }
            }
          }
        }
      }
      else
      {
          finalDerivedExpList.add(changedExp);
      }

      setVectorNameList(vecNameList);

      return finalDerivedExpList;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "Parse", "", "", "", e);
      return null;
    }
  }

  private static String getFunctionNameFromType(int type)
  {
    String funName = "";
    if (type == DerivedGraphInfo.DERIVED_MIN_TYPE_GRAPH || type == DerivedGraphInfo.DERIVED_MAX_TYPE_GRAPH || type == DerivedGraphInfo.DERIVED_NORMAL_AVG_TYPE_GRAPH || type == DerivedGraphInfo.DERIVED_SUM_COUNT_TYPE_GRAPH || type == DerivedGraphInfo.DERIVED_COUNT_TYPE_GRAPH || type == DerivedGraphInfo.DERIVED_SUM_TYPE_GRAPH)
    {
      if (type == DerivedGraphInfo.DERIVED_MIN_TYPE_GRAPH)
	funName = "MIN_";
      else if(type == DerivedGraphInfo.DERIVED_MAX_TYPE_GRAPH)
	funName = "MAX_";
      else if(type == DerivedGraphInfo.DERIVED_NORMAL_AVG_TYPE_GRAPH)
	funName = "AVG_";
      else if(type == DerivedGraphInfo.DERIVED_SUM_COUNT_TYPE_GRAPH)
	funName = DerivedGraphInfo.DERIVED_SUM_COUNT_NAME + "_";
      else if(type == DerivedGraphInfo.DERIVED_SUM_TYPE_GRAPH)
	funName = DerivedGraphInfo.DERIVED_SUM_NAME + "_";
      else if(type == DerivedGraphInfo.DERIVED_COUNT_TYPE_GRAPH)
	funName = DerivedGraphInfo.DERIVED_COUNT_NAME + "_";
    }
    
    return funName;
  }
  
  private static int[] skipExpByDerivedFuntionNameLength(int currPosition, String expression)
  {
    try
    {
      int i = currPosition;
      int type = 0;
      int[] skipAndTypeValueArray = {i, type};

      if (expression.substring(i, i + DerivedGraphInfo.DERIVED_MIN_NAME.length()).equals("MIN"))
      {
	int skipCount = checkisAVGFormula(expression, i + DerivedGraphInfo.DERIVED_MIN_NAME.length());
	if(skipCount != -1)
	{
	  type = DerivedGraphInfo.DERIVED_MIN_TYPE_GRAPH;
	  i = i + skipCount;
	}
	i = i + DerivedGraphInfo.DERIVED_MIN_NAME.length();
      }
      else if (expression.substring(i, i + DerivedGraphInfo.DERIVED_MAX_NAME.length()).equals("MAX"))
      {
	int skipCount = checkisAVGFormula(expression, i + DerivedGraphInfo.DERIVED_MAX_NAME.length());
	if(skipCount != -1)
	{
	  type = DerivedGraphInfo.DERIVED_MAX_TYPE_GRAPH;
	  i = i + skipCount;
	}
	i = i + DerivedGraphInfo.DERIVED_MAX_NAME.length();
      }
      else if (expression.substring(i, i + DerivedGraphInfo.DERIVED_AVG_NAME.length()).equals("AVG"))
      {
	int skipCount = checkisAVGFormula(expression, i + DerivedGraphInfo.DERIVED_AVG_NAME.length());
	if(skipCount != -1)
	{
	  type = DerivedGraphInfo.DERIVED_NORMAL_AVG_TYPE_GRAPH;
	  i = i + skipCount;
	}
	i = i + DerivedGraphInfo.DERIVED_AVG_NAME.length();
      }
      else if(expression.substring(i, i + DerivedGraphInfo.DERIVED_SUM_COUNT_NAME.length()).equals(DerivedGraphInfo.DERIVED_SUM_COUNT_NAME))
      {
	int skipCount = checkisAVGFormula(expression, i + DerivedGraphInfo.DERIVED_SUM_COUNT_NAME.length());
	if(skipCount != -1)
	{
	  type = DerivedGraphInfo.DERIVED_SUM_COUNT_TYPE_GRAPH;
	  i = i + skipCount;
	}
	i = i + DerivedGraphInfo.DERIVED_SUM_COUNT_NAME.length();
      }
      else if (expression.substring(i, i + DerivedGraphInfo.DERIVED_SUM_NAME.length()).equals("SUM"))
      {
	int skipCount = checkisAVGFormula(expression, i + DerivedGraphInfo.DERIVED_SUM_NAME.length());
	if(skipCount != -1)
	{
	  type = DerivedGraphInfo.DERIVED_SUM_TYPE_GRAPH;
	  i = i + skipCount;
	}
	i = i + DerivedGraphInfo.DERIVED_SUM_NAME.length();
      }
      else if(expression.substring(i, i + DerivedGraphInfo.DERIVED_COUNT_NAME.length()).equals(DerivedGraphInfo.DERIVED_COUNT_NAME))
      {
	int skipCount = checkisAVGFormula(expression, i + DerivedGraphInfo.DERIVED_COUNT_NAME.length());
	if(skipCount != -1)
	{
	  type = DerivedGraphInfo.DERIVED_COUNT_TYPE_GRAPH;
	  i = i + skipCount;
	}
	i = i + DerivedGraphInfo.DERIVED_COUNT_NAME.length();
      }
      
      
      skipAndTypeValueArray[0] = i;
      skipAndTypeValueArray[1] = type;
      
      return skipAndTypeValueArray;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }
  
  /* Separator */
  public static int getDerivedFunTypeFromFormula(String formula, String separator)
  {
    if (formula.startsWith(DerivedGraphInfo.DERIVED_MIN_NAME + separator))
      return DerivedGraphInfo.DERIVED_MIN_TYPE_GRAPH;
    else if (formula.startsWith(DerivedGraphInfo.DERIVED_MAX_NAME + separator))
      return DerivedGraphInfo.DERIVED_MAX_TYPE_GRAPH;
    else if (formula.startsWith(DerivedGraphInfo.DERIVED_AVG_NAME + separator))
      return DerivedGraphInfo.DERIVED_NORMAL_AVG_TYPE_GRAPH;
    else if (formula.startsWith(DerivedGraphInfo.DERIVED_SUM_COUNT_NAME + separator))
      return DerivedGraphInfo.DERIVED_SUM_COUNT_TYPE_GRAPH;
    else if (formula.startsWith(DerivedGraphInfo.DERIVED_SUM_NAME + separator))
      return DerivedGraphInfo.DERIVED_SUM_TYPE_GRAPH;
    else if (formula.startsWith(DerivedGraphInfo.DERIVED_COUNT_NAME + separator))
      return DerivedGraphInfo.DERIVED_COUNT_TYPE_GRAPH;

    return DerivedGraphInfo.DERIVED_NORMAL_TYPE_GRAPH;
  }
  
  private static String parseSavedDerivedExpression(String expression)
  {
    try
    {
      if (debugLevel > 1)
        Log.debugLogAlways(className, "parseSavedDerivedExpression", "", "", "Method Start. Expression " + expression);
      if (vecList == null)
        vecList = new ArrayList<String>();
      else
        vecList.clear();

      String changedExp = "";
      int expLength = expression.length();
      for (int i = 0; i < expLength; i++)
      {
        if (expression.charAt(i) == '[' && expLength > (i + 1))
        {
          String vectorName = "";
          for (int j = i + 1; j < expLength; j++)
          {
            if (expression.charAt(j) == '[')
            {
              i = j - 1;
              changedExp += "{" + vectorName;
              break;
            }

            if (expression.charAt(j) != ']')
            {
              vectorName += expression.charAt(j);
            }
            else
            {
              i = j;
              /*
               * hmCounter.put("#" + counter, "[" + vectorName + "]"); changedExp += "#" + counter; counter++;
               */
              vecList.add("[" + vectorName + "]");
              break;
            }
          }
        }
        else
        {
          changedExp += expression.charAt(i);
        }
      }

      if (debugLevel > 1)
        Log.debugLogAlways(className, "parseSavedDerivedExpression", "", "", "Return changedExp = " + changedExp);

      return changedExp;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "parseSavedDerivedExpression", "", "", "Exception - ", ex);
      return expression;
    }
  }

  /**
   * This method is converting fully encoded string i.e 1.2.#1 to full expression i.e {GroupName}{GraphName}[vector1-tmp]
   * 
   * @param formula
   * @param graphNames
   * @param hmCounter
   * @return
   */

  public static String replaceExpFromPartialEncodedToFullExp(String formula, GraphNames graphNames)
  {
    Log.debugLog(className, "replaceExpFromFullyEncodedToFullExp", "", "", "method called for formula: " + formula);
    try
    {
      String fullyEncodedExp = formula;
      String[] stTemp = DerivedData.getTokens(formula);
      String[][] rptIds = RptInfo.getRptNamesAnls(graphNames);
      for (int j = 0; j < stTemp.length; j++)
      {
        String strToken = stTemp[j].trim();

        Log.debugLogAlways(className, "replaceExpFromFullyEncodedToFullExp", "", "", "strToken = " + strToken);

        String[] arrStrTokens = rptUtilsBean.strToArrayData(strToken, ".");
        // Keeping vector name. Since vector name can have dot, we need to combine are tokens
        String vectorName = "NA";
        if (arrStrTokens.length > 2)
          vectorName = arrStrTokens[2];
        if (arrStrTokens.length > 3) // Vector name has one or more dots
        {
          for (int i = 3; i < arrStrTokens.length; i++)
          {
            // Add dot after all parts except the last one
            vectorName += "." + arrStrTokens[i];
          }
        }
        /*
         * if(vectorName.startsWith("#") && hmCounter != null && hmCounter.containsKey(vectorName)) vectorName = hmCounter.get(vectorName).toString();
         */
        if (arrStrTokens.length > 1)
        {
          String groupName = arrStrTokens[0];

          /**
           * Below code is to get Group Name
           * 
           */
          int[] arrGroupIds = graphNames.getGroupIdArray();
          
          for (int i = 0; i < arrGroupIds.length; i++)
          {
            int groupId = arrGroupIds[i];
            arrStrTokens[0] = replaceDerivedFunName(arrStrTokens[0]);
            int tmpGroupId = Integer.parseInt(arrStrTokens[0]);
            if (groupId == tmpGroupId)
            {
              groupName = graphNames.getGroupNameByGroupId(groupId);
              break;
            }
          }

          /**
           * Below code is to get Graph Name
           * 
           */

          String graphName = arrStrTokens[1];
          for (int i = 0; i < rptIds.length; i++)
          {
            if (rptIds[i][4].toString().equals(arrStrTokens[0]) && rptIds[i][0].toString().equals(arrStrTokens[1]))
            {
              graphName = rptIds[i][1].toString();
              break;
            }
          }

          if (vectorName.equals("NA"))
            vectorName = " ";
          String newFormula = "{" + groupName + "}{" + graphName + "}" + vectorName.replaceAll(";", ",");
          if (debugLevel > 0)
            Log.debugLogAlways(className, "replaceExpFromFullyEncodedToFullExp", "", "", "strToken = " + strToken + ", newFormula = " + newFormula);

          fullyEncodedExp = fullyEncodedExp.replace(strToken, newFormula);
        }
      }
      Log.debugLogAlways(className, "replaceExpFromFullyEncodedToFullExp", "", "", "changed fullyEncodedExp = " + fullyEncodedExp);
      return fullyEncodedExp;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "replaceExpFromFullyEncodedToFullExp", "", "", "Exception - ", ex);
      return formula;
    }
  }

  /**
   * This method is converting fully encoded string i.e 1.2.#1 to partial encode string i.e 1.2.[vector1-tmp]
   * 
   * @param formula
   * @param hmCounter
   * @return
   */

  public static String replaceExpFromFullyEncodedToPartiallyEncode(String formula, HashMap hmCounter)
  {
    Log.debugLog(className, "replaceExpFromFullyEncodedToPartiallyEncode", "", "", "method called for formula: " + formula);

    ArrayList keys = new ArrayList(hmCounter.keySet());
    for (int i = keys.size() - 1; i >= 0; i--)
    {
      String strKey = keys.get(i).toString();
      String strKeyValue = hmCounter.get(keys.get(i)).toString();

      if (debugLevel > 1)
        Log.debugLogAlways(className, "replaceExpFromFullyEncodedToPartiallyEncode", "", "", "Key = " + strKey + ", value = " + strKeyValue);

      formula = rptUtilsBean.replace(formula, strKey, strKeyValue);

      if (debugLevel > 2)
        Log.debugLogAlways(className, "replaceExpFromFullyEncodedToPartiallyEncode", "", "", "Replaced formula = " + formula);
    }

    return formula;
  }

  /**
   * This method will validate the combination of vector names(independent of test run) given in the expression are valid or not. For different combination of valid and invalid expression : check
   * ExcutionGUIAddDerivedGraphReq.doc Here validation does not concern with the existence of vectorName for the groupId and graphId. i.e {HTTP Failures}{HTTP Failures/Sec}[1xx,2xx] + {HTTP
   * Failures}{HTTP Failures/Sec}[2xx,3xx] is not valid as vectorNames for both the group-graph Ids are different. it should be same either [1xx,2xx] or [2xx,3xx] {HTTP Failures}{HTTP
   * Failures/Sec}[All] + {HTTP Failures}{HTTP Failures/Sec}[2xx,3xx] is not valid as vectorNames for both the group-graph Ids are different. it should be same either [All] or [2xx,3xx]
   */

  public static boolean validateVectorCombinations(String formula, StringBuffer errMsg)
  {
    Log.debugLog(className, "validateVectorCombinations", "", "", "method called for formula: " + formula);
    /**
     * this integer type variable will hold the value as below: if Nothing found means all vectors are scalar or containing only one indice 0 if we get All for the first time 1 if we get multiple
     * specified vectors for the first time 2
     */
    int firstTypeOfVector = 0;
    // This array list holds the vector names which are splited by ',' for the first time ie; {A}{B}[v1]+{C}{D}[v2,v3] . this array list holds v2 and v3 for {C}{D}[v1,v2] token
    ArrayList<String> arrFirstMultipleVectorList = new ArrayList<String>();
    // this variable holds vector string
    String indiceValue = "";
    /**
     * this is a temporary hashMap. Holding original values of hmCounter and used to revert the value of hmCounter HashMap back to its original value as hmCounter is a static class variable any might
     * be used somewhere.
     * 
     */
    // LinkedHashMap tempLocalHmCounter = new LinkedHashMap();
    try
    {
      if (formula == null || formula.equals(""))
      {
        errMsg.append("Expression cannot be blank.");
        return false;
      }

      /*
       * // this needs to be clear because its a static class varibale and might be holding some different more values. it may impact the execution of method. // because we are looping through the
       * number of values hold by the hmCounter(HashMap). tempLocalHmCounter.putAll(hmCounter);
       */

      // hmCounter.clear();

      // To get vector names from the expression in a hash map because vector names may contains special characters.
      parseSavedDerivedExpression(formula);

      Iterator values = vecList.iterator();
      while (values.hasNext())
      {
        indiceValue = values.next().toString().replace("[", "");
        indiceValue = indiceValue.replace("]", "");

        // we are continuing in case of scalar graph or vector graph having one indice except ALL
        if (!indiceValue.contains(",") && !indiceValue.toString().toUpperCase().equals("ALL"))
          continue;

        if (firstTypeOfVector == 0)
        {
          Log.debugLog(className, "validateVectorCombinations", "", "", "indiceValue=" + indiceValue + " boolean = " + indiceValue.toString().toUpperCase().equals("ALL"));
          if (indiceValue.toString().toUpperCase().equals("ALL"))
            firstTypeOfVector = 1;
          else
          {
            firstTypeOfVector = 2;
            arrFirstMultipleVectorList.addAll(Arrays.asList(indiceValue.split(",")));
          }
          continue;
        }

        if (firstTypeOfVector == 1)
        {
          if (indiceValue.toString().toUpperCase().equals("ALL"))
            continue;
          if (indiceValue.contains(",")) // This condition will occur when {Grp Name1}{GraphName}[All] + {Grp Name2}{Graph Name}[v1,v2] this is invalid case
          {
            errMsg.append("Invalid combinations of vectors.\r\nEither use 'All' or same multiple specified vectors throughout the expression.");
            return false;
          }
        }
        else
        {
          // This condition will occur when {Grp Name2}{Graph Name}[v1,v2] + {Grp Name1}{GraphName}[All] .This is invalid case
          if (indiceValue.toString().toUpperCase().equals("ALL"))
          {
            errMsg.append("Invalid combinations of vectors.\r\nEither use same multiple specified vectors or 'All' throughout the expression.");
            return false;
          }
          else
          {
            String[] arrVectorNames = indiceValue.split(",");

            // This condition will occur when {Grp Name1}{GraphName}[v1,v2] + {Grp Name2}{Graph Name}[v1,v2,v3] this is invalid case
            if (arrFirstMultipleVectorList.size() != arrVectorNames.length)
            {
              errMsg.append("Number of multiple specified vectors should be equal throughout the expression.");
              return false;
            }
            else
            {
              /**
               * This condition will occur when {Grp Name1}{GraphName}[v1,v2] + {Grp Name2}{Graph Name}[v1,v2] this is valid case for a groupname and graphname, we are validating whether its all
               * specified vectors are present in the arrayList or not this condition will be executed when vector names contains ',' because if its not containing comman(',') means it a scalar graph
               * or vector graph with one indices only. for that we are already continuing the loop.
               */

              for (int i = 0; i < arrVectorNames.length; i++)
              {
                if (!arrFirstMultipleVectorList.contains(arrVectorNames[i]))
                {
                  errMsg.append("Multiple specified vectors should be same throughout the expression.");
                  return false;
                }
              }
            }
          }
        }
      }
      return true;
    }
    catch (Exception e)
    {
      errMsg.append("Invalid expression.Please check and try again.");
      Log.stackTraceLog(className, "validateVectorCombinations", "", "", "Exception Caugth - ", e);
      return false;
    }
    /*
     * finally { hmCounter = tempLocalHmCounter; }
     */
  }

  private static void setVectorNameList(ArrayList<String> vecNameList)
  {
    vectorNameList = vecNameList;
  }

  public static ArrayList<String> getVectorNameList()
  {
    return vectorNameList;
  }

  private static boolean isNumeric(String str)
  {
    Log.debugLog(className, "isNumeric", "", "", "method called for str: " + str);
    try
    {
      if (!str.trim().equals(""))
        Double.parseDouble(str);

      return true;
    }
    catch (Exception e)
    {
      return false;
    }
  }

  // This function check weather the input string is a math Exp. For Example if input string str = 2 + 3 * 4 then return true if input string str = a + b then return false with message token 'a' is
  // not recognized.
  private static boolean isMathExpression(String str, StringBuffer errMsg)
  {
    Log.debugLog(className, "isMathExpression", "", "", "method called for Str: " + str);
    String tmpStr = "";
    String opertors = "+-/*";
    str = str.replace("(", "");
    str = str.replace(")", "");
    str = str.replace("{", "");
    str = str.replace("}", "");
    str = str.replace("[", "");
    str = str.replace("]", "");
    str = str.trim();

    for (int i = 0; i < str.length(); i++)
    {
      if (opertors.contains(str.charAt(i) + "") && i == 0)
      {
        Log.debugLog(className, "isMathExpression", "", "", "Error: operators can not at be the start of the expression.");
        errMsg.append("Error: operators can not at be the start of the expression inside curly braces.");
        return false;
      }
      else if (opertors.contains(str.charAt(i) + "") && i == (str.length() - 1))
      {
        Log.debugLog(className, "isMathExpression", "", "", "Error: operators can not be at the end of the expression.");
        errMsg.append("Error: operators can not be at the end of the expression inside curly braces.");
        return false;
      }
      else if (opertors.contains(str.charAt(i) + ""))
      {
        if (tmpStr.trim().equals(""))
        {
          Log.debugLog(className, "isMathExpression", "", "", "Error: Some value is expected between operators inside curly braces.");
          errMsg.append("Error: Some value is expected between operators inside curly braces.");
          return false;
        }
        if (!isNumeric(tmpStr.trim()))
        {
          Log.debugLog(className, "isMathExpression", "", "", "Error: Can not recognize token " + tmpStr);
          errMsg.append("Error: Can not recognize token '" + tmpStr + "' Please remove it.");
          return false;
        }
        else
          tmpStr = "";
      }
      else
      {
        tmpStr += str.charAt(i);
      }
    }
    if (!isNumeric(tmpStr.trim()) || tmpStr.trim().equals(""))
    {
      Log.debugLog(className, "isMathExpression", "", "", "Error: some value is expected between inside curly braces");
      if (!tmpStr.trim().equals(""))
        errMsg.append("Error: can not recognize token '" + tmpStr + "' inside curly braces.");
      else
        errMsg.append("Error: some value is expected inside curly braces.");
      return false;
    }

    return true;
  }

  // this function return true if brackets are properly used otherwise return false.
  private static boolean validateBrakets(String str, String openBraket, String closeBraket, StringBuffer errMsg)
  {
    Log.debugLog(className, "validateBrakets", "", "", "method called for open braket " + openBraket + " and close braket " + closeBraket);
    try
    {
      int idx1 = -1; // used for index of open bracket.
      int idx2 = -1; // used for index of close bracket.
      while (true)
      {
        idx1 = str.indexOf(openBraket);
        idx2 = str.indexOf(closeBraket);
        if (idx1 >= 0 && idx2 >= 0)
        {
          if (idx1 < idx2)
          {
            str = replaceFirst(str, openBraket, " ");
            str = replaceFirst(str, closeBraket, " ");
          }
          else
          {
            Log.debugLog(className, "validateBrakets", "", "", "Error: '" + openBraket + "' and '" + closeBraket + "' brackets are not used properly.");
            errMsg.append("Error: '" + openBraket + "' and '" + closeBraket + "' brackets are not used properly.");
            return false;
          }
        }

        if (idx1 < 0 && idx2 < 0)
        {
          return true;
        }
        else if (idx1 >= 0 && idx2 < 0)
        {
          Log.debugLog(className, "validateBrakets", "", "", "Error: '" + openBraket + "' and '" + closeBraket + "' brackets are not used properly.");
          errMsg.append("Error: '" + openBraket + "' and '" + closeBraket + "' brackets are not used properly.");
          return false;
        }
        else if (idx1 < 0 && idx2 >= 0)
        {
          Log.debugLog(className, "validateBrakets", "", "", "Error: '" + openBraket + "' and '" + closeBraket + "' brackets are not used properly.");
          errMsg.append("Error: '" + openBraket + "' and '" + closeBraket + "' brackets are not used properly.");
          return false;
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      errMsg.append(e.getMessage());
    }
    return false;
  }

  private static String replaceFirst(String str, String token, String newToken)
  {
    String resultStr = "";
    boolean replaced = false;
    for (int i = 0; i < str.length(); i++)
    {
      if (token.equals(str.charAt(i) + "") && !replaced)
      {
        resultStr += newToken;
        replaced = true;
      }
      else
        resultStr += str.charAt(i);
    }
    return resultStr;
  }

  private static int getGraphId(String tempGraphName, String strGrpId, GraphNames gp)
  {
    int grphID = -1;
    String[][] rptIds = getRptNamesAnls(gp);

    for (int i = 0; i < rptIds.length; i++)
    {
      String tmpRptName = rptIds[i][1].toString();
      if (rptIds[i][4].toString().equals(strGrpId + "") && tmpRptName.trim().equals(tempGraphName.trim()))
      {
        grphID = Integer.parseInt(rptIds[i][0].toString());
        break;
      }
    }

    Log.debugLog(className, "getGraphId", "", "", "grphID = " + grphID);
    return grphID;
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

  // check the number of operators, return 0 if all operators are in right place
  private static int checkExpressionByOperator(String s)
  {
    Log.debugLog(className, "checkExpressionByOperator", "", "", "Method called, TextArea content = " + s);

    int sLength = s.length();
    int operatorCount = 0;
    int alphCount = 0;

    for (int i = 0; i < sLength; i++)
    {
      if ((s.charAt(i) == '+' || s.charAt(i) == '-' || s.charAt(i) == '*' || s.charAt(i) == '/') && operatorCount >= 0)
      {
        boolean operatorFound = true;
        // check for vector name having any operator at the end like '1xx_/'
        for (int j = i + 1; j < sLength; j++)
        {
          if (s.charAt(j) == ' ')
            continue;
          else if (s.charAt(j) == ']')
          {
            operatorFound = false;
            break;
          }
        }

        // check for vector name having any operator at the staring like '+1xx'
        if (operatorFound == false)
        {
          for (int j = i - 1; j >= 0; j--)
          {
            if (s.charAt(j) == ' ')
              continue;
            else if (s.charAt(j) == '[')
            {
              operatorFound = false;
              break;
            }
          }
        }

        if (operatorFound == false)
          continue;

        operatorCount++;
        if (alphCount == 0)
          return 1;
        Log.debugLog(className, "checkExpressionByOperator", "", "", "Operatorcount incremented = " + operatorCount);
      }
      else
      {
        int asciiVal = (int) s.charAt(i);
        if (((asciiVal >= 65 && asciiVal <= 90) || (asciiVal >= 97 && asciiVal <= 122) || (asciiVal >= 48 && asciiVal <= 57) || (asciiVal == 40)) && (operatorCount == 1 || alphCount == 0))
        {
          if (alphCount == 0)
          {
            alphCount++;
            continue;
          }
          operatorCount--;
          alphCount++;
          Log.debugLog(className, "checkExpressionByOperator", "", "", "Operatorcount decremented = " + operatorCount);
        }
      }
    }
    Log.debugLog(className, "checkExpressionByOperator", "", "", "Method called, Operator count = " + operatorCount);

    return operatorCount;
  }

  /**
   * This function takes the input string expression in form of {GrpName}{GraphName}[vecName] + {GrpName}{GraphName}[All] it parse expression and check for validation of paranthesis and braces are
   * properly used and return an arrayList having String in form 1.2.NA + 3.2.Vec1, 1.2.NA + 3.2.Vec2, 1.2.NA + 3.2.Vec3
   * 
   * @param expression
   * @param errMsg
   * @param gp
   * @return
   */

  public static ArrayList<String> getParsedDerivedExpList(String expression, StringBuffer errMsg, GraphNames gp)
  {
    return getParsedDerivedExpList(expression, errMsg, gp, false);
  }

  public static ArrayList<String> getParsedDerivedExpList(String expression, StringBuffer errMsg, GraphNames gp, boolean isCalledFromTemplate)
  {
    Log.debugLog(className, "getParsedDerivedExpList", "", "", "method called for expression: " + expression);
    validateBrakets(expression, "(", ")", errMsg);
    if (!errMsg.toString().trim().equals(""))
      return null;
    validateBrakets(expression, "{", "}", errMsg);
    if (!errMsg.toString().trim().equals(""))
      return null;
    validateBrakets(expression, "[", "]", errMsg);
    if (!errMsg.toString().trim().equals(""))
      return null;
    if (checkExpressionByOperator(expression) != 0)
    {
      Log.errorLog(className, "getParsedDerivedExpList", "", "", "Operators not putted in right place, invalid expression = " + expression);
      errMsg.append("Operators are not putted in right place, invalid expression '" + expression + "'.\nPlease enter valid derived expression.");
      return null;
    }

    if (!validateVectorCombinations(expression, errMsg))
      return null;

    ArrayList<String> result = parseDerivedExp(expression, errMsg, gp, isCalledFromTemplate);
    if (result != null && result.size() > 0)
    {
      if (!isCalledFromTemplate)
        chkOperators(result.get(0), errMsg);
      if (!errMsg.toString().trim().equals(""))
        return null;
    }

    if (result == null)
      return null;

    return result;
  }

  private static void chkOperators(String str, StringBuffer errMsg)
  {
    String[] stTemp = DerivedData.getTokens(str);

    for (int index = 0; index < stTemp.length; index++)
    {
      String strToken = stTemp[index].trim();
      strToken = strToken.replace("(", "");
      strToken = strToken.replace(")", "");
      strToken = strToken.replace("{", "");
      strToken = strToken.replace("}", "");

      if (!isNumeric(strToken))
      {
        String[] arrTemp = rptUtilsBean.strToArrayData(strToken, ".");
        arrTemp[0] = replaceDerivedFunName(arrTemp[0]);
        
        if (arrTemp.length < 3)
        {
          errMsg.append("Error: token '" + strToken + "' is not recognized. Please remove it.");
          return;
        }
        else if (arrTemp.length > 3 && (!isNumeric(arrTemp[0]) || !isNumeric(arrTemp[1])))
        {
          errMsg.append("Error: Invalid expression.");
          return;
        }
        else
        {
          if (!isNumeric(arrTemp[0]) || !isNumeric(arrTemp[1]))
          {
            errMsg.append("Error: token '" + strToken + "' is not recognized. Please remove it.");
            return;
          }
        }
      }
    }
  }

  public static String replaceDerivedFunName(String funName)
  {
    funName = funName.replace("MIN_", "");
    funName = funName.replace("MAX_", "");
    funName = funName.replace("AVG_", "");
    funName = funName.replace("SUM_", "");
    funName = funName.replace(DerivedGraphInfo.DERIVED_SUM_COUNT_NAME + "_", "");
    funName = funName.replace(DerivedGraphInfo.DERIVED_COUNT_NAME + "_", "");
    
    return funName;
  }
  
  /**
   * we are assuming that if the formula was AVG then it should as follows
   * "AVG" followed by "zero or more spaces" followed by "("
   * 
   * This method return no of spaces to skip
   * @param expression
   * @param currPos
   * @return
   */
  private static int checkisAVGFormula(String expression, int currPos)
  {
    int spaceCount = 0;
    for(;currPos < expression.length(); currPos++)
    {
  	if(expression.charAt(currPos) == ' ')
  	{
  	  spaceCount++;
  	  continue;
  	}
  	else if(expression.charAt(currPos) == '(')
  	  return spaceCount;
  	else
  	  return -1;
    }
  
    return -1;
  }
  
  /**
   * This method will convert the given ',' separated formula into '+' formula
   * Eg: input: AVG({A}{B}[a,b] , {C}{D})
   * 	 Output: AVG({A}{B}[a,b] + {C}{D})
   * 
   * Note:As yet we are only able to apply single AVG formula
   *      This method will return null if any formula added to avg
   * @param expression
   * @return
   */
  private static String getPreParsedFormula(String expression)
  {
    try
    {
      boolean isopenBracketFound = false;
      boolean startPharanthesis = false;
      int bracketCount = -1;
      String changedExp = "";
      char ch;
      for(int i = 0; i < expression.length(); i++)
      {
	ch = expression.charAt(i);

	if(ch == '[')
	  isopenBracketFound = true;

	if(ch == ']')
	  isopenBracketFound = false;

	if(ch == ',' && !isopenBracketFound)
	{
	  changedExp +=  '+';
	  isopenBracketFound = false;
	}
	else
	  changedExp += ch;

	if(startPharanthesis && bracketCount == -1)
	{
	  if(ch != ' ' && ch != '\t')
	    return null;
	}

	if(ch == '(')
	{
	  startPharanthesis = true;
	  bracketCount++;
	}

	if(ch == ')')
	  bracketCount--;
      }

      return changedExp;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }
  
  public static void main(String arg[])
  {
    /*
     * String expression = "{Transactions Failures}{Transactions Failures/Minute}[1-xx]"; StringBuffer errMsg = new StringBuffer(); GraphNames gp = new GraphNames(4872); int index =
     * gp.getIndexOfGraphDataByGrpIdGraphIdVecName("10", "2", "1-xx"); ArrayList list = getParsedDerivedExpList(expression, errMsg, gp); System.out.println("errMsg = " + errMsg); if (list != null) for
     * (int i = 0; i < list.size(); i++) System.out.println(i + " = " + list.get(i));
     */
  }
}

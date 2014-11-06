package pac1.Bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import pac1.Bean.GraphName.GraphNames;

/**
 * -------------------------------------------------------------------------
 * 
 * @Name CheckProfileUtils.java
 * 
 * @Purpose To validate check profile rule expression and encode and decode
 *          expression.
 *          ----------------------------------------------------------
 * 
 *          Encoding Algorithm:
 * 
 *          Step: 1 Tokenize the expression based on && and || tokens
 * 
 *          Step: 2 Tokenize the sub expression(from step 1) based on <= , >= ,
 *          != and ==
 * 
 *          Step:3 Tokenize the sub expression(from step 2) based on <, >
 * 
 *          Step: Validate each token and convert into respective
 *          groupId.GraphId.Vector
 *          ----------------------------------------------
 * 
 * 
 * @author Ravi Kant Sharma
 * @Modification History
 * 
 */

public class CheckProfileUtils
{
  //Class-name
  private static String className = "CheckProfileUtils";

  //Flag set for percentile attribute operator
  private boolean isPercentile = false;

  //Flag set for moving average attribute operator
  private boolean isMovingAverage = false;

  //Flag set if decoding else encoding the expression
  private boolean isDecode = false;

  //An array to hold attribute operators
  private String[] attributeOperator = {"Min", "Max", "Avg", "Percentile", "StdDev", "MovAvg", "Median"};

  /**
   * This function returns parsed expression to unparsed expression 
   * 1.2.NA + 2.5.Vec1 -> {Vusers Info}{Active Vusers} + {Network Throughput}{Ethernet Send Throughput (Kbps)}
   * 
   * @param exp The expression to be unparsed.
   * @param errMsg The error messages are appended to it.
   * @param gp Graph Names.
   */
  public String getDecodeExpression(String exp, StringBuffer errMsg, GraphNames gp)
  {
    // Flag is set for Decoding Expression.
    isDecode = true;

    Log.debugLog(className, "getDecodeExpression", "", "", "method called for expression: " + exp);

    // Validates expression.
    if(!validateExpression(exp, errMsg))
    {
      errMsg.append("Error: Invalid expression. Please enter valid expression.");
      return null;
    }

    // The expression is converted into a char array.
    char expCharArray[] = exp.toCharArray();

    int replaceValue = -1;
    // To replace <,>,= in the graph expression to @,#,% so that graph expression is not split

    for(int replaceChar = 0; replaceChar < expCharArray.length; replaceChar++)
    {
      //example - Avg (10013.1.Cavisson>NDapp>cmon) < 4.0
      //check for attributeOperator so that replacing takes palace
      if(expCharArray[replaceChar] == '(' && expCharArray[replaceChar - 1] == ' ')
      {
        //check for attribute operator fetching last three chars before ( and space 
        String attributeFirstMatch = "";
        if(replaceChar >= 4)  //for Min and Max or Avg
        {
          attributeFirstMatch =  exp.substring(replaceChar-4, replaceChar-1);
          replaceValue = getAttributeOperators(attributeFirstMatch);
        }
        else if(replaceChar >= 7)  //StdDev", "MovAvg", "Median"
        {
          attributeFirstMatch =  exp.substring(replaceChar-7, replaceChar-1);
          replaceValue = getAttributeOperators(attributeFirstMatch);
        }
        else if(replaceChar >= 10)  //Percentile
        {
          attributeFirstMatch =  exp.substring(replaceChar-10, replaceChar-1);
          replaceValue = getAttributeOperators(attributeFirstMatch);
        } 

        //Replacing the <, > sign
        if(replaceValue != -1)
        {
          //replacing the chars <, >  
          while(expCharArray[replaceChar] != ')')
          {
            if((expCharArray[replaceChar] == '<'))
              expCharArray[replaceChar] = '$';
            if((expCharArray[replaceChar] == '>'))
              expCharArray[replaceChar] = '#';
            replaceChar++;
          }//end of internal loop

        }//condition end

      }//end of graph expression

    }//end of external loop

    // The char array has now been modified into a String.
    String expressionModified = new String(expCharArray);

    // An array list containing various components of the expression.
    ArrayList<String> parsedDataModified = getParsedExp(expressionModified, errMsg);

    Log.debugLog(className, "getEncodeExpression", "", "", "After parsed operators arraylist = " + parsedDataModified);

    // An array list to store the parsed data with changes undone in graph information 
    ArrayList<String> parsedData = new ArrayList<String>();

    // To replace #,$ in the graph expression to >,< so that original graph expression is obtained
    for(int l = 0; l < parsedDataModified.size(); l++)
    {
      char parsedDataModifiedChar[] = parsedDataModified.get(l).toCharArray();
      for(int undoReplaceChar = 0; undoReplaceChar < parsedDataModifiedChar.length; undoReplaceChar++)
      {

        if(parsedDataModifiedChar[undoReplaceChar] == '$')
          parsedDataModifiedChar[undoReplaceChar] = '<';

        if(parsedDataModifiedChar[undoReplaceChar] == '#')
          parsedDataModifiedChar[undoReplaceChar] = '>';
      }

      String undoReplaceParsedData = new String(parsedDataModifiedChar);
      parsedData.add(undoReplaceParsedData);

    }

    //The expression must contain minimum 3 components
    if(parsedData.size() < 3)
    {
      errMsg.append("Error: Invalid expression. Every expression should be a valid condition. Please enter valid expression.");
      return null;
    }

    // Decoding of the expression.
    String encodedExp = evaluateExpression(parsedData, errMsg, gp, isDecode);

    return encodedExp;
  }

  /**
   * This function validates the expression for balancing of brackets and position of various operators
   * 
   * @param expression The expression to be validated.
   * @param errMsg The error messages are appended to it.
   */
  public boolean validateExpression(String expression, StringBuffer errMsg)
  {
    Log.debugLog(className, "validateExpression", "", "", "method called for expression: " + expression);

    // validates balancing of brackets ( and )
    if(!balancedBracesMethod(expression, errMsg))
    {
      errMsg.append("Error: bracket(s) are not put at right place. Please check expression.");
      return false;
    }

    // validates expression starts from operator or brackets or numbers
    if("+-*/><=!|?{}[]1234567890".contains(expression.charAt(0) + ""))
    {
      errMsg.append("Error: Expression must start from attribute name.");
      return false;
    }

    // validates expression does not start from logical operators
    if(expression.startsWith("&&") || expression.startsWith("||"))
    {
      errMsg.append("Error: Expression cannot start from logical operator. Please enter valid expression.");
      return false;
    }

    // validates expression does not start from conditional operators
    if(expression.trim().endsWith("==") || expression.trim().endsWith(">=") || expression.trim().endsWith("<=") || expression.trim().endsWith("!=") || expression.trim().endsWith("=") || expression.trim().endsWith("!") || expression.trim().endsWith("<") || expression.trim().endsWith(">"))
    {
      errMsg.append("Error: Expression cannot end with logical operator. Please enter valid expression.");
      return false;
    }

    // Expression passes all validations.
    return true;
  }

  /**
   * This function return parsed expression to unparsed expression 
   * {Vusers Info}{Active Vusers} + {Network Throughput}{Ethernet Send Throughput (Kbps)} -> 1.2.NA + 2.5.Vec1
   * 
   * @param exp The expression to be encoded.
   * @param errMsg The error messages are appended to it.
   * @param gp Graph Names.
   */
  public String getEncodeExpression(String expression, StringBuffer errMsg, GraphNames gp)
  {
    // Flag is set to false as the method encodes the expression.
    isDecode = false;
    try
    {
      Log.debugLog(className, "getEncodeExpression", "", "", "method called for expression: " + expression);
      if(gp == null)
      {
        Log.errorLog(expression, "getEncodeExpression", "", "", "Cannot encode expression - Graph Names Object is null.");
        errMsg.append("Cannot encode expression - No group graph definition file found. Please see error log.");
        return null;
      }

      // Validates the expression.
      if(!validateExpression(expression, errMsg))
      {
        errMsg.append("Error: Invalid expression. Please enter valid expression.");
        return null;
      }

      // The expression is converted into a char array.
      char expCharArray[] = expression.toCharArray();

      // To replace (,),/ in the graph expression to @,#,% so that graph expression is not split
      for(int replaceChar = 0; replaceChar < expCharArray.length; replaceChar++)
      {
        if(expCharArray[replaceChar] == '{')
        {
          while(expCharArray[replaceChar] != '}')
          {
            if((expCharArray[replaceChar] == '('))
              expCharArray[replaceChar] = '@';

            if((expCharArray[replaceChar] == ')'))
              expCharArray[replaceChar] = '#';

            if((expCharArray[replaceChar] == '/'))
              expCharArray[replaceChar] = '$';
            replaceChar++;
          }
        }
      }

      // The char array has now been modified into a String.
      String expressionModified = new String(expCharArray);

      // An array list containing various components of the expression.
      ArrayList<String> parsedDataModified = getParsedExp(expressionModified, errMsg);
      Log.debugLog(className, "getEncodeExpression", "", "", "After parsed operators arraylist = " + parsedDataModified);

      // An array list to store the parsed data with changes undone in graph information 
      ArrayList<String> parsedData = new ArrayList<String>();

      // To replace @,#,% in the graph expression to (,),/ so that original graph expression is obtained
      for(int l = 0; l < parsedDataModified.size(); l++)
      {
        char parsedDataModifiedChar[] = parsedDataModified.get(l).toCharArray();
        for(int undoReplaceChar = 0; undoReplaceChar < parsedDataModifiedChar.length; undoReplaceChar++)
        {
          if(parsedDataModifiedChar[undoReplaceChar] == '@')
            parsedDataModifiedChar[undoReplaceChar] = '(';

          if(parsedDataModifiedChar[undoReplaceChar] == '#')
            parsedDataModifiedChar[undoReplaceChar] = ')';

          if(parsedDataModifiedChar[undoReplaceChar] == '$')
            parsedDataModifiedChar[undoReplaceChar] = '/';
        }
        String undoReplaceParsedData = new String(parsedDataModifiedChar);
        parsedData.add(undoReplaceParsedData);
      }

      // The expression must contain minimum 3 components
      if(parsedData.size() < 3)
      {
        errMsg.append("Error: Invalid expression. Every expression should be a valid condition. Please enter valid expression.");
        return null;
      }

      //Encoding of the expression
      String changeExpr = evaluateExpression(parsedData, errMsg, gp, isDecode);

      return changeExpr;
    }

    catch(Exception e)
    {
      Log.errorLog(className, "getEncodeExpression", "", "", "Exception - " + e);
      errMsg.append("Error: unexpected expression found for graph name. Please enter valid expression.");
      return null;
    }
  }

  /**
   * This is a common method in both encoding and decoding for evaluating the array list 
   * 
   * @param parsedData The array list to be evaluated.
   * @param errMsg The error messages are appended to it.
   * @param gp Graph Names.
   * @param isDecode The flag is set for decoding and unset for encoding.
   */
  private String evaluateExpression(ArrayList<String> parsedData, StringBuffer errMsg, GraphNames gp, boolean isDecode)
  {
    Log.debugLog(className, "evaluateExpression", "", "", "method called for arrayList: " + parsedData);

    // String to store the evaluated expression.
    String changedExp = "";

    //Flag set if conditional operator found
    boolean isConditionalOperatorFound = false;

    //Flag set if attribute operator found
    boolean isAttributeOperatorFound = false;

    for(int i = 0; i < parsedData.size(); i++)
    {
      String tempExp = parsedData.get(i).trim();

      //The whitespace is added to expression
      if(parsedData.get(i).equals(" "))
        changedExp += parsedData.get(i);

      //The open bracket is added to expression
      if(tempExp.equals("("))
        changedExp += parsedData.get(i);

      //The close bracket is added to expression
      if(tempExp.equals(")"))
        changedExp += parsedData.get(i);

      //Attribute operators are checked
      if(getAttributeOperators(tempExp) != -1)
      {
	if(isAttributeOperatorFound)
        {
    	  errMsg.append("There should be an arithmetic operator between two attribute operators in an expression");
          return null; 
        }
        isAttributeOperatorFound = true;
        changedExp += parsedData.get(i);
        i++;
        tempExp = parsedData.get(i).trim();
        if(tempExp.equals(""))
          i++;
        tempExp = parsedData.get(i).trim();
        if(tempExp.equals("("))
        {
          changedExp += parsedData.get(i);
          i++;
        }
        else
        {
          errMsg.append("After Series operator 1 open bracket should come");
          return null;
        }
        tempExp = parsedData.get(i).trim();
        if(tempExp.equals("("))
        {
          errMsg.append("After Series operator only 1 open bracket should come");
          return null;
        }
        else
        {
          tempExp = parsedData.get(i).trim();

          //Check to see if decode method has called this function
          if(isDecode)
          {
            if(tempExp.contains("."))
            {
              // To decode the graph expression.
              String decodedExpression = getGraphExpression(tempExp, gp, errMsg);
              if(decodedExpression == null)
              {
                errMsg.append("Graph information is not correct.");
                return null;
              }
              changedExp += decodedExpression;
              i++;
              tempExp = parsedData.get(i).trim();

              //The graph expression should end with ')'. There should be no additional arguments.
              if(tempExp.equals(")"))
                changedExp += parsedData.get(i);
              else
              {
                errMsg.append("Graph information is not correct.");
                return null;
              }
            }
            else
            {
              errMsg.append("Graph information is not correct.");
              return null;
            }
          }
          else
          {
            if(tempExp.charAt(0) == '{')
            {
              // To encode the graph expression.   
              String encodedExpression = getGraphEncodedExpression(tempExp, errMsg, gp);
              if(encodedExpression == null)
              {
                errMsg.append("Graph information is not correct.");
                return null;
              }
              changedExp += encodedExpression;
              i++;
              tempExp = parsedData.get(i).trim();

              //The graph expression should end with ')'. There should be no additional arguments.
              if(tempExp.equals(")"))
                changedExp += parsedData.get(i);
              else
              {
                errMsg.append("Graph information is not correct.");
                return null;
              }
            }
            else
            {
              errMsg.append("Graph information is not correct.");
              return null;
            }
          }
        }
      }
      // To check the arithmetic operators '+,-,*,/' have a numeric value or attribute operator after them.
      if(tempExp.trim().equals("+") || tempExp.trim().equals("-") || tempExp.trim().equals("*") || tempExp.trim().equals("/"))
      {
        if(!isAttributeOperatorFound)
        {
          errMsg.append("Attribute operator should come before arithmetic operator.");
          return null;
        }
        changedExp += tempExp;
        i++;
        tempExp = parsedData.get(i).trim();
        
        if(tempExp.equals(""))
        {
            i++;
            tempExp = parsedData.get(i).trim();
        }
	if(tempExp.equals("("))
        {
        	changedExp += tempExp;
            i++;
            tempExp = parsedData.get(i).trim();
        }

        //To check if there is a number after arithmetic operator
        if(isNumeric(tempExp))
          changedExp += parsedData.get(i);

        else
        {

          //To check if there is an attribute operator after arithmetic operator
          if(getAttributeOperators(tempExp) != -1)
          {
            isAttributeOperatorFound = true;
            changedExp += parsedData.get(i);
            i++;
            tempExp = parsedData.get(i).trim();
            if(tempExp.equals(""))
              i++;
            tempExp = parsedData.get(i).trim();
            if(tempExp.equals("("))
            {
              changedExp += parsedData.get(i);
              i++;
            }
            else
            {
              errMsg.append("After Series operator 1 open bracket should come");
              return null;
            }
            tempExp = parsedData.get(i).trim();
            if(tempExp.equals("("))
            {
              errMsg.append("After Series operator only 1 open bracket should come");
              return null;
            }
            else
            {
              tempExp = parsedData.get(i).trim();

              //Check to see if decode method has called this function
              if(isDecode)
              {
                if(tempExp.contains("."))
                {
                  // To decode the graph expression.
                  String decodedExpression = getGraphExpression(tempExp, gp, errMsg);
                  if(decodedExpression == null)
                  {
                    errMsg.append("Graph information is not correct.");
                    return null;
                  }
                  changedExp += decodedExpression;
                }
                else
                {
                  errMsg.append("Graph information is not correct.");
                  return null;
                }
              }
              else
              {
                if(tempExp.charAt(0) == '{')
                {
                  // To encode the graph expression.   
                  String encodedExpression = getGraphEncodedExpression(tempExp, errMsg, gp);
                  if(encodedExpression == null)
                  {
                    errMsg.append("Graph information is not correct.");
                    return null;
                  }
                  changedExp += encodedExpression;
                }
                else
                {
                  errMsg.append("Graph information is not correct.");
                  return null;
                }
              }
            }
          }
          else
          {
            errMsg.append("No numeric value or expression after simple operators");
            return null;
          }
        }
        i++;
        tempExp = parsedData.get(i).trim();
        
        if(tempExp.equals(")"))
        {
        	changedExp += tempExp;
        }
        else
        {
        	i--;
                tempExp = parsedData.get(i).trim();
        }
      }
      // To check presence of conditional operators.
      if(tempExp.trim().equals(">") || tempExp.trim().equals("<") || tempExp.trim().equals("<=") || tempExp.trim().equals(">=") || tempExp.trim().equals("!=") || tempExp.trim().equals("=="))
      {
        isConditionalOperatorFound = true;
        changedExp += parsedData.get(i);
        i++;

        tempExp = parsedData.get(i).trim();
        if(isNumeric(tempExp.trim()))
          changedExp += parsedData.get(i);
        else
        {
          errMsg.append("No numeric value after conditional operators");
          return null;
        }
      }
      // To check presence of logical operators.
      if(tempExp.trim().equals("&&") || tempExp.trim().equals("||"))
      {
        if(!isConditionalOperatorFound)
    	{
    	  errMsg.append("No conditional operator found before logical operator.");
    	  return null;
    	}
    	if(!isAttributeOperatorFound)
    	{
    	  errMsg.append("No attribute operator found before logical operator.");
    	  return null;
    	}
	changedExp += tempExp.trim();
        ArrayList<String> parsedDataNew = new ArrayList<String>();
        for(int j = i + 1; j < parsedData.size(); j++)
          parsedDataNew.add(parsedData.get(j));
        changedExp += evaluateExpression(parsedDataNew, errMsg, gp, isDecode);
        break;
      }
    }
    if(!isConditionalOperatorFound)
    {
      errMsg.append("No conditional operator found.");
      return null;
    }
    if(!isAttributeOperatorFound)
    {
      errMsg.append("No attribute operator found.");
      return null;
    }
    return changedExp;
  }

  /**
   * This method divides the expression based on operators and returns an arrayList 
   * 
   * @param expression The expression to be divided into components.
   */
  private ArrayList<String> getParsedExp(String expression, StringBuffer errMsg)
  {
    try
    {
      Log.debugLog(className, "getParsedExp", "", "", "Method called. expression" + expression);

      StringBuffer expBuffer = new StringBuffer();
      //Expression is split by '['
      String[] arrTempSq = expression.split("\\[");
      ArrayList<String> arrSplitOpenSquare = new ArrayList<String>();
      if(arrTempSq.length == 1)
        arrSplitOpenSquare.add(expression);
      else
      {
        for(int i = 0; i < arrTempSq.length; i++)
        {
          arrSplitOpenSquare.add(arrTempSq[i]);
          if(i != arrTempSq.length - 1)
            arrSplitOpenSquare.add("[");
        }
      }
      HashMap <String, String> variableListValue = new HashMap <String, String>();
      int count =0;
      //Expression is split by ']'
      for(int i = 0; i < arrSplitOpenSquare.size(); i++)
      {
        String tempRecord = arrSplitOpenSquare.get(i);
        String[] arrTempRecord = tempRecord.split("\\]");
        if(arrTempRecord.length == 1)
          expBuffer.append(tempRecord);
	else
        {
	  expBuffer.append("a"+ count + "_");
          variableListValue.put("a"+ count + "_", arrTempRecord[0]);
          count++;
          expBuffer.append("]");
          for(int j = 1; j < arrTempRecord.length; j++)
          {
            expBuffer.append(arrTempRecord[j]);
            if(j != arrTempRecord.length - 1)
              expBuffer.append("]");
          }
        }
      }
      String replacedBrackets = expBuffer.toString();
      	 
      //Expression is split by '('
      String[] arrTemp = replacedBrackets.split("\\(");

      ArrayList<String> arrSplitOpen = new ArrayList<String>();
      if(arrTemp.length == 1)
        arrSplitOpen.add(expression);
      else
      {
        for(int i = 0; i < arrTemp.length; i++)
        {
          arrSplitOpen.add(arrTemp[i]);
          if(i != arrTemp.length - 1)
            arrSplitOpen.add("(");
        }
      }

      //Expression is split by ')'
      ArrayList<String> arrSplitClose = new ArrayList<String>();
      for(int i = 0; i < arrSplitOpen.size(); i++)
      {
        String tempRecord = arrSplitOpen.get(i);
        String[] arrTempRecord = tempRecord.split("\\)");
        if(arrTempRecord.length == 1)
          arrSplitClose.add(tempRecord);
        else
        {
          for(int j = 0; j < arrTempRecord.length; j++)
          {
            arrSplitClose.add(arrTempRecord[j]);
            if(j != arrTempRecord.length - 1)
              arrSplitClose.add(")");
          }
        }
      }

      //Expression is split by '&&'
      ArrayList<String> arrSplitAnd = new ArrayList<String>();
      for(int i = 0; i < arrSplitClose.size(); i++)
      {
        String tempRecord = arrSplitClose.get(i);
        String[] arrTempRecord = tempRecord.split("\\&&");
        if(arrTempRecord.length == 1 || arrTempRecord.length == 0)
          arrSplitAnd.add(tempRecord);
        else
        {
          for(int j = 0; j < arrTempRecord.length; j++)
          {
            arrSplitAnd.add(arrTempRecord[j]);
            if(j != arrTempRecord.length - 1)
              arrSplitAnd.add("&&");
          }
        }
      }

      //Expression is split by '||'
      ArrayList<String> arrSplitOR = new ArrayList<String>();
      for(int i = 0; i < arrSplitAnd.size(); i++)
      {
        String tempRecord = arrSplitAnd.get(i);
        String[] arrTempRecord = tempRecord.split("\\|\\|");
        if(arrTempRecord.length == 1 || arrTempRecord.length == 0)
          arrSplitOR.add(tempRecord);
        else
        {
          for(int j = 0; j < arrTempRecord.length; j++)
          {
            arrSplitOR.add(arrTempRecord[j]);
            if(j != arrTempRecord.length - 1)
              arrSplitOR.add("||");
          }
        }
      }

      //Expression is split by '<='
      ArrayList<String> arrLessThanEqualData = new ArrayList<String>();
      for(int i = 0; i < arrSplitOR.size(); i++)
      {
        String tempRecord = arrSplitOR.get(i);
        String[] arrTempRecord = null;
        if(tempRecord.indexOf("<=") != -1)
          arrTempRecord = tempRecord.split("\\<=");
        else
        {
          arrLessThanEqualData.add(tempRecord);
          continue;
        }
        for(int j = 0; j < arrTempRecord.length; j++)
        {
          arrLessThanEqualData.add(arrTempRecord[j]);
          if(j != arrTempRecord.length - 1)
            arrLessThanEqualData.add("<=");
        }
      }

      //Expression is split by '<'
      ArrayList<String> arrLessThanData = new ArrayList<String>();
      for(int i = 0; i < arrLessThanEqualData.size(); i++)
      {
        String tempRecord = arrLessThanEqualData.get(i);
        String[] arrTempRecord = null;
        if(!tempRecord.trim().equals("<=") && tempRecord.indexOf("<") != -1)
          arrTempRecord = tempRecord.split("\\<");
        else
        {
          arrLessThanData.add(tempRecord);
          continue;
        }
        for(int j = 0; j < arrTempRecord.length; j++)
        {
          arrLessThanData.add(arrTempRecord[j]);
          if(j != arrTempRecord.length - 1)
            arrLessThanData.add("<");
        }
      }

      //Expression is split by '>='
      ArrayList<String> arrGreaterThanEqualData = new ArrayList<String>();
      for(int i = 0; i < arrLessThanData.size(); i++)
      {
        String tempRecord = arrLessThanData.get(i);
        String[] arrTempRecord = null;
        if(tempRecord.indexOf(">=") != -1)
          arrTempRecord = tempRecord.split("\\>=");
        else
        {
          arrGreaterThanEqualData.add(tempRecord);
          continue;
        }
        for(int j = 0; j < arrTempRecord.length; j++)
        {
          arrGreaterThanEqualData.add(arrTempRecord[j]);
          if(j != arrTempRecord.length - 1)
            arrGreaterThanEqualData.add(">=");
        }
      }

      //Expression is split by '>'
      ArrayList<String> arrGreaterThanData = new ArrayList<String>();
      for(int i = 0; i < arrGreaterThanEqualData.size(); i++)
      {
        String tempRecord = arrGreaterThanEqualData.get(i);
        String[] arrTempRecord = null;
        if(!tempRecord.trim().equals(">=") && tempRecord.indexOf(">") != -1)
          arrTempRecord = tempRecord.split("\\>");
        else
        {
          arrGreaterThanData.add(tempRecord);
          continue;
        }
        for(int j = 0; j < arrTempRecord.length; j++)
        {
          arrGreaterThanData.add(arrTempRecord[j]);
          if(j != arrTempRecord.length - 1)
            arrGreaterThanData.add(">");
        }
      }

      //Expression is split by '=='
      ArrayList<String> arrEqualsData = new ArrayList<String>();
      for(int i = 0; i < arrGreaterThanData.size(); i++)
      {
        String tempRecord = arrGreaterThanData.get(i);
        String[] arrTempRecord = tempRecord.split("\\==");
        if(arrTempRecord.length == 1 || arrTempRecord.length == 0)
          arrEqualsData.add(tempRecord);
        else
        {
          for(int j = 0; j < arrTempRecord.length; j++)
          {
            arrEqualsData.add(arrTempRecord[j]);
            if(j != arrTempRecord.length - 1)
              arrEqualsData.add("==");
          }
        }
      }

      //Expression is split by '!='
      ArrayList<String> arrNotEqualsData = new ArrayList<String>();
      for(int i = 0; i < arrEqualsData.size(); i++)
      {
        String tempRecord = arrEqualsData.get(i);
        String[] arrTempRecord = tempRecord.split("\\!=");
        if(arrTempRecord.length == 1 || arrTempRecord.length == 0)
          arrNotEqualsData.add(tempRecord);
        else
        {
          for(int j = 0; j < arrTempRecord.length; j++)
          {
            arrNotEqualsData.add(arrTempRecord[j]);
            if(j != (arrTempRecord.length - 1))
              arrNotEqualsData.add("!=");
          }
        }
      }

    //Expression is split by '.' for decoding expressions
      ArrayList<String> arrDotData = new ArrayList<String>();
      HashMap <String, String> variableListValueForDecode = new HashMap <String, String>();
      int counter =0;
      
      for(int i = 0; i < arrNotEqualsData.size(); i++)
      {
    	StringBuffer dotbuffer = new StringBuffer();
        String tempRecord = arrNotEqualsData.get(i);
        String[] arrTempRecord = tempRecord.split("\\.");
        if(arrTempRecord.length == 1 || arrTempRecord.length == 2)
          arrDotData.add(tempRecord);
        else
        {
          dotbuffer.append("b"+ counter + "_");
	  variableListValueForDecode.put("b"+ counter + "_", tempRecord);
	  counter++;
	  arrDotData.add(dotbuffer.toString());
	}
      }
      
      //Expression is split by '+'
      ArrayList<String> arrPlusData = new ArrayList<String>();
      for(int i = 0; i < arrDotData.size(); i++)
      {
        String tempRecord = arrDotData.get(i);

        String[] arrTempRecord = tempRecord.split("\\+");
        if(arrTempRecord.length == 1 || arrTempRecord.length == 0)
          arrPlusData.add(tempRecord);
        else
        {
          for(int j = 0; j < arrTempRecord.length; j++)
          {
            arrPlusData.add(arrTempRecord[j]);
            if(j != arrTempRecord.length - 1)
              arrPlusData.add("+");
          }
        }
      }

      //Expression is split by '-'
      ArrayList<String> arrMinusData = new ArrayList<String>();
      for(int i = 0; i < arrPlusData.size(); i++)
      {
        String tempRecord = arrPlusData.get(i);
        String[] arrTempRecord = tempRecord.split("\\-");
        if(arrTempRecord.length == 1 || arrTempRecord.length == 0)
          arrMinusData.add(tempRecord);
        else
        {
          for(int j = 0; j < arrTempRecord.length; j++)
          {
            arrMinusData.add(arrTempRecord[j]);
            if(j != arrTempRecord.length - 1)
              arrMinusData.add("-");
          }
        }
      }

      //Expression is split by '*'
      ArrayList<String> arrMultiplyData = new ArrayList<String>();
      for(int i = 0; i < arrMinusData.size(); i++)
      {
        String tempRecord = arrMinusData.get(i);
        String[] arrTempRecord = tempRecord.split("\\*");
        if(arrTempRecord.length == 1 || arrTempRecord.length == 0)
          arrMultiplyData.add(tempRecord);
        else
        {
          for(int j = 0; j < arrTempRecord.length; j++)
          {
            arrMultiplyData.add(arrTempRecord[j]);
            if(j != arrTempRecord.length - 1)
              arrMultiplyData.add("*");
          }
        }
      }

      //Expression is split by '/'
      ArrayList<String> arrDivideData = new ArrayList<String>();
      for(int i = 0; i < arrMultiplyData.size(); i++)
      {
        String tempRecord = arrMultiplyData.get(i);
        String[] arrTempRecord = tempRecord.split("\\/");
        if(arrTempRecord.length == 1 || arrTempRecord.length == 0)
          arrDivideData.add(tempRecord);
        else
        {
          for(int j = 0; j < arrTempRecord.length; j++)
          {
            arrDivideData.add(arrTempRecord[j]);
            if(j != arrTempRecord.length - 1)
              arrDivideData.add("/");
          }
        }
      }
      ArrayList<String> resultant = new ArrayList<String>();
      boolean changedExp = false;
      
      for(int i = 0; i < arrDivideData.size(); i++)
      {
        for(String s : variableListValue.keySet())
    	{
	      if(arrDivideData.get(i).contains(s))
	      {
            resultant.add(arrDivideData.get(i).replace(s, variableListValue.get(s) + ""));
    	    changedExp = true;
    	  }
    	}
    	if(!changedExp)
    	{
          resultant.add(arrDivideData.get(i));
          changedExp = true;
    	}
    	changedExp = false;
      }
      
      ArrayList<String> resultantNew = new ArrayList<String>();
      boolean changedExpNew = false;
      
      for(int i = 0; i < resultant.size(); i++)
      {
        for(String s : variableListValueForDecode.keySet())
    	{
	  if(arrDivideData.get(i).contains(s))
	  {
	    resultantNew.add(resultant.get(i).replace(s, variableListValueForDecode.get(s) + ""));
	    changedExpNew = true;
    	  }
    	}
    	if(!changedExpNew)
    	{
          resultantNew.add(resultant.get(i));
          changedExpNew = true;
    	}
    	changedExpNew = false;
      }
      
      return resultantNew;

    }
    catch(Exception ex)
    {
      Log.errorLog(className, "getParsedExp", "", "", "Error in parsing expression - " + ex);
      errMsg.append("Error: Invalid expression. Please check expression.");
      return null;
    }
  }

  /**
   * This method returns the Graph Id on the basis of GraphName and Group Id
   * 
   * @param tempGraphName The Graph name.
   * @param strGrpId The group id.
   * @param gp The Graph name.
   */
  private int getGraphId(String tempGraphName, String strGrpId, GraphNames gp)
  {
    int grphID = -1;
    try
    {
      Log.debugLog(className, "getGraphId", "", "", "Method called. graph name = " + tempGraphName + ", Grp Id = " + strGrpId);
      String[][] rptIds = RptInfo.getRptNamesAnls(gp);
      for(int i = 0; i < rptIds.length; i++)
      {
        String[] rptName = rptIds[i][1].toString().split("-");
        if(rptIds[i][4].toString().equals(strGrpId + "") && rptName[0].trim().equals(tempGraphName.toString().trim()))
        {
          grphID = Integer.parseInt(rptIds[i][0].toString());
          break;
        }
      }
      Log.debugLog(className, "getGraphId", "", "", "Method End. grphID = " + grphID);
    }
    catch(Exception ex)
    {
      Log.errorLog(className, "getGraphId", "", "", "Error in getting graphId. Exception - " + ex);
    }
    return grphID;
  }

  /**
   * This method returns the index of arithmetic operators in the String array.
   * If the operator is not present then it returns -1
   * 
   * @param oper The operator whose index has to be returned.
   */
  private int getAttributeOperators(String oper)
  {
    Log.debugLog(className, "getAttributeOperators", "", "", "Method called. operator = " + oper);

    for(int i = 0; i < attributeOperator.length; i++)
    {
      if(attributeOperator[i].equalsIgnoreCase(oper))
      {
        isPercentile = false;
        isMovingAverage = false;

        if(i == 3)
          isPercentile = true;
        else if(i == 5)
          isMovingAverage = true;

        return i;
      }
    }
    return -1;
  }

  /**
   * This function convert graph expression into GrpId.GraphId.VectorName
   * {Page Failures}{Page Failures/Minute}[1xx,2xx] ------> 8.2.1xx,8.2.2xx
   * 
   * @param expression The graph expression to be converted.
   * @param sb The error messages are appended to it.
   * @param gp Graph Names.
   */
  private String getGraphEncodedExpression(String expression, StringBuffer sb, GraphNames gp)
  {
    try
    {
      Log.debugLog(className, "getGraphEncodedExpression", "", "", "Method Called. expression = " + expression);
      String changExp = "";
      String groupName = "";
      String graphName = "";
      String vectName = "";
      int openCount = 0;
      int closeCount = 0;
      int counter = 0;
      String tempStr = "";
      int vectCount = 0;
      int vectCountEnd = 0;
      String percentileValue = "";
      String movingAverageValue = "";
      boolean vectorNameFound = false;
      String[] arrExp = expression.split(",");

      if(isPercentile)
      {
        // All the components leaving the last one should not be numeric
        for(int i = 0; i < (arrExp.length - 1); i++)
        {
          if(isNumeric(arrExp[i]))
          {
            sb.append("Percentile Value is not correct");
            return null;
          }
        }

        //Check for the conditions where either there are no vector values or 'All' vector values are present 
        if(expression.indexOf("[") == -1 || expression.contains("[All]"))
        {
          // Percentile value should be between 1 to 100 inclusive
          if(arrExp.length != 2 || Integer.parseInt(arrExp[arrExp.length - 1].trim()) > 101 || Integer.parseInt(arrExp[arrExp.length - 1].trim()) < 1)
          {
            sb.append("Percentile Value is not correct");
            return null;
          }
        }

        //Check for the conditions where specified vector values are present
        else
        {
          // Percentile value should be between 1 to 100 inclusive
          if((arrExp.length > 3) || Integer.parseInt(arrExp[arrExp.length - 1].trim()) > 101 || Integer.parseInt(arrExp[arrExp.length - 1].trim()) < 1)
          {
            sb.append("Percentile Value is not correct");
            return null;
          }
        }
        percentileValue = arrExp[arrExp.length - 1];
        expression = expression.substring(0, expression.lastIndexOf(percentileValue));
      }
      else if(isMovingAverage)
      {
        // All the components leaving the last one should not be numeric
        for(int i = 0; i < (arrExp.length - 1); i++)
        {
          if(isNumeric(arrExp[i]))
          {
            sb.append("Moving Average Value is not correct");
            return null;
          }
        }
        //Check for the conditions where either there are no vector values or 'All' vector values are present
        if(expression.indexOf("[") == -1 || expression.contains("[All]"))
        {
          //Moving average value should be greater than 0
          if(arrExp.length != 2 || (Integer.parseInt(arrExp[arrExp.length - 1].trim()) < 0))
          {
            sb.append("Moving Average Value is not correct");
            return null;
          }
        }
        //Check for the conditions where specified vector values are present
        else if((arrExp.length > 3) || (Integer.parseInt(arrExp[arrExp.length - 1].trim()) < 0))
        {
          sb.append("Moving Average Value is not correct");
          return null;
        }
        movingAverageValue = arrExp[arrExp.length - 1];
        expression = expression.substring(0, expression.lastIndexOf(movingAverageValue));
      }

      else
      {
        // No component should be numeric for other attribute operators
        for(int i = 0; i < arrExp.length; i++)
        {
          if(isNumeric(arrExp[i]))
          {
            sb.append("Attribute operator value is not correct");
            return null;
          }
        }
      }
      for(int i = 0; i < expression.length(); i++)
      {
        char c = expression.charAt(i);
        if(c == '{')
          openCount++;
        else if(c == '}')
          closeCount++;
        else if(c == '[')
          vectCount++;
        else if(c == ']')
          vectCountEnd++;
        else
          tempStr += c;
        if(openCount != 0 && openCount == closeCount && c == '}' && counter == 0)
        {
          groupName = tempStr;
          tempStr = "";
          openCount = 0;
          closeCount = 0;
          counter++;
          //To check whether number appears after group name
          if((i + 1) < expression.length())
          {
            String nextChar = "" + expression.charAt(i + 1);
            if(isNumeric(nextChar))
              sb.append("Error: Invalid group name. Number appears after group name");
          }
        }
        else if(openCount != 0 && openCount == closeCount && c == '}' && counter == 1)
        {
          graphName = tempStr;
          tempStr = "";
          openCount = 0;
          closeCount = 0;
          counter++;
          //To check whether number appears after graph name
          if((i + 1) < expression.length())
          {
            String nextChar = "" + expression.charAt(i + 1);
            if(isNumeric(nextChar))
              sb.append("Error: Invalid graph name. Number appears after graph name");
          }
        }
        else if(vectCount == vectCountEnd && c == ']' && vectCount != 0)
        {
          vectName = tempStr;
          tempStr = "";
          vectCount = 0;
          vectCountEnd = 0;
          counter++;
          vectorNameFound = true;
          //To check whether number appears after vector name
          if((i + 1) < expression.length())
          {
            String nextChar = "" + expression.charAt(i + 1);
            if(isNumeric(nextChar))
              sb.append("Error: Invalid vector name. Number appears after vector name");
          }
        }
      }

      int grpId = gp.getGroupIdByGroupName(groupName.trim());

      if(grpId == -1)
        grpId = gp.getGroupIdByGroupName(groupName.trim());
      int graphId = getGraphId(graphName, grpId + "", gp);
      if(graphId == -1)
      {
        sb.append("Error: Invalid graph name - " + graphName);
        sb.append("Error: No such type graph name " + graphName + " found. Please check expression.");
        return null;
      }
      if(vectName.trim().equals("All"))
        changExp = grpId + "." + graphId + ".All";

      else if(vectName.trim().equals("") && !vectorNameFound)
        changExp = grpId + "." + graphId + ".NA";

      else
      {
        String[] arrVectorNames = rptUtilsBean.strToArrayData(vectName, ",");
        if(arrVectorNames.length == 0)
        {
          sb.append("Vector name found empty in token - " + expression + ". Please enter valid vector name.");
          return null;
        }
        for(int j = 0; j < arrVectorNames.length; j++)
        {
          boolean isVectorFound = isVectorFound(gp,  grpId, graphId, arrVectorNames[j].trim());
          if(isVectorFound)
          {
            if(j == arrVectorNames.length - 1)
              changExp += grpId + "." + graphId + "." + arrVectorNames[j].trim();
            else
              changExp += grpId + "." + graphId + "." + arrVectorNames[j].trim() + ",";
          }
          else
          {
            // vector not found
            if(arrVectorNames[j].trim().equals(""))
              sb.append("Vector name cannot be empty. Please check expression.");
            else
              sb.append("Vector - " + arrVectorNames[j].trim() + " not found for group - " + groupName + ". Please check expression.");
            return null;
          }
        }
      }
      if(isPercentile)
      {
        if(percentileValue.equals(""))
        {
          sb.append("Percentile value not found in exp - " + expression + ". Please check expression.");
          return null;
        }
        else
        {
          changExp += "," + percentileValue;
          isPercentile = false;
          return changExp;
        }
      }
      else if(isMovingAverage)
      {
        if(movingAverageValue.equals(""))
        {
          sb.append("Moving Average value not found in exp - " + expression + ". Please check expression.");
          return null;
        }
        else
        {
          changExp += "," + movingAverageValue;
          isMovingAverage = false;
          return changExp;
        }
      }
      else
      {
        isPercentile = false;
        isMovingAverage = false;
        return changExp;
      }

    }
    catch(Exception ex)
    {
      ex.printStackTrace();
      Log.errorLog(className, "getGraphEncodedExpression", "", "", "Exception - " + ex);
      sb.append("Invalid expression. Please check expression.");
      return null;
    }
  }

  /**
   * This function returns the expression of group , graph and vector name. 
   * 
   * @param series The expression to be decoded.
   * @param sb The error messages are appended to it.
   * @param gp Graph Names.
   */
  public String getGraphExpression(String series, GraphNames gp, StringBuffer sb)
  {
    try
    {
      Log.debugLog(className, "getGraphExpression", "", "", "Method Called. series = " + series);

      //To store value of percentile
      String percentileValue = "";

      //To store value of moving average
      String movingAverageValue = "";

      //Split the series by ','
      String[] arrSeries = series.split(",");

      if(isPercentile)
        percentileValue = arrSeries[arrSeries.length - 1].trim();
      else if(isMovingAverage)
        movingAverageValue = arrSeries[arrSeries.length - 1].trim();

      //Percentile value has to be between 1 to 100 inclusive
      if(isPercentile && (percentileValue.equals("") || (Integer.parseInt(percentileValue) > 101) || (Integer.parseInt(percentileValue) < 1)))
      {
        sb.append("Percentile value should be numeric. Please enter numeric percentile value.");
        return null;
      }

      //Moving average value has to be more than 0
      else if(isMovingAverage && (movingAverageValue.equals("") || Integer.parseInt(movingAverageValue) < 0))
      {
        sb.append("Moving Average value should be numeric. Please enter numeric moving Average value.");
        return null;
      }

      int groupId = -1;
      int graphId = -1;
      String vectName = "";
      String tempStr = "";
      int previousGrpId = -1;
      int previousGraphId = -1;
      if(arrSeries.length == 1)
      {
        //String[] arrGGV = rptUtilsBean.strToArrayData(arrSeries[0].trim(), ".");
    	String[] arrGGV = arrSeries[0].trim().split("\\.",3);
        if(arrGGV.length > 2)
        {
          groupId = Integer.parseInt(arrGGV[0]);
          graphId = Integer.parseInt(arrGGV[1]);
          vectName = arrGGV[2];
          GraphUniqueKeyDTO graphUniqueKeyDTO = new GraphUniqueKeyDTO(groupId, graphId, vectName);
          String groupName = gp.getGroupNameByGraphUniqueKeyDTO(graphUniqueKeyDTO, false);
          String graphName = gp.getGraphNameByGraphUniqueKeyDTO(graphUniqueKeyDTO, false);

          if(vectName.trim().equals("All"))
            tempStr = "{" + groupName + "}{" + graphName + "}[" + vectName + "]";
          else if(!vectName.trim().equals("") && !vectName.trim().equals("NA"))
          {
            if(isVectorFound(gp, groupId, graphId, vectName))
              tempStr = "{" + groupName + "}{" + graphName + "}[" + vectName + "]";
            else
            {
              sb.append("Error: Vector name not found in token - " + series + ". Please check expression.");
              return null;
            }
          }
          else
          {
            if(vectName.trim().equals("NA"))
              tempStr = "{" + groupName + "}{" + graphName + "}";
            else
            {
              sb.append("Error: Invalid token found - " + series + ". Please check expression.");
              return null;
            }
          }

          return tempStr;
        }
        else
        {
          sb.append("Error: Invalid token found - " + series + ". Please check expression.");
          return null;
        }
      }
      else
      {
        int len = arrSeries.length;
        if(isPercentile || isMovingAverage)
          len = len - 1;

        for(int ii = 0; ii < len; ii++)
        {
          String[] arrGGV = rptUtilsBean.strToArrayData(arrSeries[ii].trim(), ".");

          if(arrGGV.length > 2)
          {
            groupId = Integer.parseInt(arrGGV[0]);
            graphId = Integer.parseInt(arrGGV[1]);
            vectName = arrGGV[2];
            if(ii == 0)
            {
              GraphUniqueKeyDTO graphUniqueKeyDTO = new GraphUniqueKeyDTO(groupId, graphId, vectName);
              String groupName = gp.getGroupNameByGraphUniqueKeyDTO(graphUniqueKeyDTO, false);
              String graphName = gp.getGraphNameByGraphUniqueKeyDTO(graphUniqueKeyDTO, false);
              
              if(vectName.trim().equals("") || vectName.trim().equals("NA"))
              {
                tempStr = "{" + groupName + "}{" + graphName + "}";
              }
              else if(vectName.trim().equals("All"))
              {
                tempStr = "{" + groupName + "}{" + graphName + "}[" + vectName + "]";
              }
              else
              {
                if(isVectorFound(gp, groupId, graphId, vectName))
                {
                  tempStr = "{" + groupName + "}{" + graphName + "}[" + vectName + "]";
                }
                else
                {
                  sb.append("Error: Vector name not found in token - " + arrSeries[ii] + ". Please check expression.");
                  return null;
                }
              }

              previousGrpId = groupId;
              previousGraphId = graphId;
            }
            else
            {
              // same graph vectors
              if(previousGrpId == groupId && previousGraphId == graphId)
              {
                int tempIndex = tempStr.trim().lastIndexOf("]");
                if(tempIndex != -1)
                {
                  tempStr = tempStr.trim().substring(0, tempIndex);
                  if(isVectorFound(gp, groupId, graphId, vectName))
                    tempStr += "," + vectName + "]";
                  else
                  {
                    sb.append("Error: Vector name not found in token - " + series + ". Please check expression.");
                    return null;
                  }
                }
                else
                {
                  sb.append("Error: Invalid token found - " + series + ". Please check expression.");
                  return null;
                }
              }
              else
              // different graphs vector
              {
                sb.append("Error: Different graph's vector found in token  - " + series + ". Please check expression.");
                return null;
              }
            }
          }
          else
          {
            sb.append("Invalid expression please remove token " + series);
            return null;
          }
        }

        if(isPercentile)
          tempStr += "," + percentileValue;
        else if(isMovingAverage)
          tempStr += "," + movingAverageValue;
        Log.debugLog(className, "getGraphExpression", "", "", "Method Called. tempStr = " + tempStr);
        return tempStr;
      }
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getGraphExpression", "", "", "Exception - " , ex);
      sb.append("Error: Invalid expression. Please check expression.");
      return null;
    }
  }

  /**
   * This function checks whether vector name exists for this group and graph or not. 
   * If the vector is present then return true otherwise return false
   * 
   * @param vectName The vector name to be validated.
   * @param grpId The group id.
   * @param graphId The graph id.
   * @param gp Graph Names.
   */
  private boolean isVectorFound(GraphNames gp, int grpId, int graphId, String vectName)
  {
    boolean vectorFound = false;
    try
    {
      Log.debugLog(className, "isVectorFound", "", "", "GrpId = " + grpId + ", GraphId = " + graphId + ", vectName = " + vectName);

      // String array to store the Vector names
      String[] arrVectorNames = gp.getNameOfIndicesByGroupIdAndGraphId(grpId, graphId);

      if(arrVectorNames.length == 1 && arrVectorNames[0].equals("NA"))
        arrVectorNames = gp.getNameOfGroupIndicesByGroupId(grpId);

      //To check presence of vector
      for(int ii = 0; ii < arrVectorNames.length; ii++)
      {
    	Log.debugLog(className, "isVectorFound", "", "", "arrVectorNames[ii].trim() = " + arrVectorNames[ii].trim());
        if(vectName.trim().equals(arrVectorNames[ii].trim()))
        {
          vectorFound = true;
          break;
        }
      }
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "isVectorFound", "", "", "Error in getting vector names. Exception - " , ex);
    }

    return vectorFound;
  }

  /**
   * This function checks whether string is numeric or not. 
   * If yes return true else return false.
   * 
   * @param str The string to be checked.
   */
  private boolean isNumeric(String str)
  {
    Log.debugLog(className, "isNumeric", "", "", "method called for String: " + str);
    try
    {
      if(!str.trim().equals(""))
      {
        //Parses the string argument as signed decimal integer
        Double.parseDouble(str);
        return true;
      }
      else
        return false;

    }
    catch(Exception e)
    {
      return false;
    }
  }

  /**
   * This function checks whether brackets are balanced or not. 
   * If yes return true else return false.
   * 
   * @param str The string to be checked.
   */
  public boolean balancedBracesMethod(String s, StringBuffer errMsg)
  {
    Log.debugLog(className, "balancedBracesMethod", "", "", "method called for String: " + s);

    // Stack to check balancing of brackets
    Stack st = new Stack();
    char d;

    for(int i = 0; i < s.length(); i++)
    {
      char c = s.charAt(i);

      // Push '(' into stack
      if(c == '(')
        st.push(new Character(c));

      // Push '{' into stack
      else if(c == '{')
        st.push(new Character(c));

      // Push '[' into stack
      else if(c == '[')
        st.push(new Character(c));

      // Check presence of opening bracket for ')'
      else if(c == ')')
      {
        // If stack is empty return false
        if(st.empty() == true)
        {
          errMsg.append("Error:( and ) brackets are not used properly.");
          return false;
        }
        // Pop the first element if stack is not empty and check the brackets
        else if(st.empty() == false)
        {
          d = (Character)st.pop();
          if(d != '(')
          {
            errMsg.append("Error:( and ) brackets are not used properly.");
            return false;
          }
        }
      }

      // Check presence of opening bracket for '}'
      else if(c == '}')
      {
        // If stack is empty return false
        if(st.empty() == true)
        {
          errMsg.append("Error:{ and } brackets are not used properly.");
          return false;
        }
        // Pop the first element if stack is not empty and check the brackets
        else if(st.empty() == false)
        {
          d = (Character)st.pop();
          if(d != '{')
          {
            errMsg.append("Error:{ and } brackets are not used properly.");
            return false;
          }
        }
      }

      // Check presence of opening bracket for ']'
      else if(c == ']')
      {
        // If stack is empty return false
        if(st.empty() == true)
        {
          errMsg.append("Error:[ and ] brackets are not used properly.");
          return false;
        }
        // Pop the first element if stack is not empty and check the brackets
        else if(st.empty() == false)
        {
          d = (Character)st.pop();
          if(d != '[')
          {
            errMsg.append("Error:[ and ] brackets are not used properly.");
            return false;
          }
        }
      }
    }
    if (st.empty())
      return true;
    else
    {
      errMsg.append("Error: Brackets are not used properly.");
      return false;
    }
  }

  public static void main(String arg[])
  {
    CheckProfileUtils utils = new CheckProfileUtils();
    String expression = "StdDev ({Vuser Info}{Waiting Vusers}) - 2 < 22.0";
    //String expression = "Avg({Page Download}{Average Java Script processing Time (Secs)}) < 11.0";
    //String expression = "Avg({Vuser Info}{Thinking Vusers}) < 2";
    //String expression = "Percentile ( {HTTP Failures}{HTTP Failures/Sec}[All], 50 )"; 
    StringBuffer errMsg = new StringBuffer();
    GraphNames gp = new GraphNames(1311);
    System.out.println("expression = " + expression);
    String list = utils.getEncodeExpression(expression, errMsg, gp);
    System.out.println("errMsg = " + errMsg);
    //String list = "Avg(1.100.NA) < 2";
    System.out.println(list);
    System.out.println("*******************");
    errMsg = new StringBuffer();
    String list2 = utils.getDecodeExpression(list, errMsg, gp);
    System.out.println("errMsg = " + errMsg);
    System.out.println(list2);
  }
}

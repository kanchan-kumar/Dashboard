/**--------------------------------------------------------------------
  @Name    : LogFileData.java
  @Author  : Atul
  @Purpose : This file will contain the data of log file(event.log, debugtrace.log) accordingly 
             after reading.

  @Modification History: 20/08/09 - Initial version
----------------------------------------------------------------------*/
package pac1.Bean;

import java.util.ArrayList;
import java.util.Vector;

public class SyncPointFileData implements java.io.Serializable
{
  private static String className = "SyncPointFileData";
  private static final long serialVersionUID = 1L;
  private ArrayList<ArrayList<String>> listOfLines = new ArrayList<ArrayList<String>>();
  private ArrayList<Object> listOfObject = new ArrayList<Object>();
  int numOfFields = -1;
  String testNum = "";
  String columnNames[];
    
  public SyncPointFileData()
  {
  }
  
  public void setTestRun(String testNum)
  {
  	this.testNum = testNum;
  }
  
  public String getTestRun()
  {
  	return testNum;
  }
  
  public boolean setLineToList(String line)
  {
//    Log.debugLog(className, "setLineToList", "", "",  "Method Called. line = " + line);
    try
    {
      String strArr[] = rptUtilsBean.strToArrayData(line, "|");
      
      if(numOfFields == -1)
      {
        Log.debugLog(className, "setLineToList", "", "",  "setting number of fields = " + strArr.length);
        setNumOfFields(strArr.length);
      }
      
      if(numOfFields > strArr.length)
      {
        Log.errorLog(className, "setLineToList", "", "",  "Number of fields = " + strArr.length + " is coming less than required fields = " + numOfFields + " for line = " + line + ", so this line is ignored.");
        return true;
      }
      
//      Log.debugLog(className, "setLineToList", "", "",  "number of fields = " + getNumOfFields());
      ArrayList<String> listOfFields = new ArrayList<String>();
      for(int i = 0 ; i < strArr.length ; i++)
      {
        if(i >= numOfFields)
        {
          listOfFields.set(numOfFields - 1, listOfFields.get(numOfFields - 1) + "|" + strArr[i]);
        }
        else
        {
          listOfFields.add(strArr[i]);
        }
      }
      
      listOfLines.add(listOfFields);
      
      
      /**
       * This is done here not at the client event it is removing generalizaton for this 
       * class as we need to handle and add the object in "listOfObject" conditionally.
       * 
       * But we have to do this as we don't want to iterate again on clint side
       * that number of line read here.
       */
      
      SyncPointLogRow SyncPointLogRow = new  SyncPointLogRow(getTestRun());
      if(!SyncPointLogRow.setLogDataToLogRow(this, listOfLines.size() -1))
      {
        Log.errorLog(className, "setLineToList", "", "",  "Can not set Log data to Log Row for list = " + listOfFields + ", so removing line from list. And this is ignored.");
        listOfLines.remove(listOfLines.size() -1);
        return true;
      }
      listOfObject.add(SyncPointLogRow);
      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "setLineToList", "", "",  "Exception in setLineToList", ex);
      return false;
    }
  }
  
  public ArrayList<Object> getListOfObject()
  {
    return listOfObject;
  }
  
  public void setListOfObject(ArrayList<Object> listOfObject)
  {
    this.listOfObject = listOfObject;
  }
  
  public String[] getLineAsArray(int i)
  {
    Log.debugLog(className, "getLineAsArray", "", "",  "Method Called. Index = " + i);
    if((i < 0) || (i > listOfLines.size()))
    {
      Log.errorLog(className, "getLineAsArray", "", "",  "Error invalid index = " + i);
      return null;
    }
    else
    {
      ArrayList<String> tempListForLine = listOfLines.get(i);
      String strArr[] = new String[tempListForLine.size()];
      strArr = tempListForLine.toArray(strArr);
      return strArr;
    }
  }
  
  public ArrayList<String> getLineAsList(int i)
  {
    if((i < 0) || (i > listOfLines.size()))
    {
      Log.errorLog(className, "getLineAsList", "", "",  "Error invalid index = " + i);
      return null;
    }
    else
    {
      ArrayList<String> tempListForLine = listOfLines.get(i);
      return tempListForLine;
    }
  }
  
  public Vector<String> getLineAsVec(int i)
  {
    if((i < 0) || (i > listOfLines.size()))
    {
      Log.errorLog(className, "getLineAsVec", "", "",  "Error invalid index = " + i);
      return null;
    }
    else
    {
      ArrayList<String> tempListForLine = listOfLines.get(i);
      return new Vector<String>(tempListForLine);
    }
  }
  
  /**
   * This message will return specific field of specific line.   
   * @param lineIndex Index of line  from which field need to extraced
   * @param fieldIndex Index of field in that current line.
   * @return
   */
  public String getFieldOfLine(int lineIndex, int fieldIndex)
  {
    Log.debugLog(className, "getFieldOfLine", "", "",  "Method Called. lineIndex = " + lineIndex + ", fieldIndex = " + fieldIndex);
    if((lineIndex < 0) || (lineIndex > listOfLines.size()))
    {
      Log.errorLog(className, "getLineAsVec", "", "",  "Error invalid line index = " + lineIndex);
      return null;
    }
    ArrayList<String> listLine = listOfLines.get(lineIndex);
    if((fieldIndex < 0) || (fieldIndex > listLine.size()))
    {
      Log.errorLog(className, "getLineAsVec", "", "",  "Error invalid field index = " + lineIndex);
      return null;
    }
    return listLine.get(fieldIndex);
  }
  
  /**
   * This method will return the data 
   * in 2D array, which will also include the header.
   * @return
   */
  public String[][] getDataIn2DArray()
  {
    String[][] data = null;
    
    if(listOfLines.size() > 0)
    {
      data = new String[listOfLines.size() + 1][columnNames.length];
      data[0] = columnNames;
      for(int i = 0 ; i < listOfLines.size() ; i++)
      {
        data[i + 1] = getLineAsArray(i);
        if(data[i + 1] == null)
          return null;
      }
    }
    else
    {
      data = new String[1][columnNames.length];
      data[0] = columnNames;
    }
    
    return data;
  }
  
  public void appendList(ArrayList<ArrayList<String>> listOfLinesToAppend)
  {
	listOfLines.clear();
    listOfLines.addAll(listOfLinesToAppend);
  }
  
  public void appendList(SyncPointFileData logFileDataToAppend)
  {
	listOfLines.clear();
	listOfObject.clear();
    listOfLines.addAll(logFileDataToAppend.listOfLines);
    listOfObject.addAll(logFileDataToAppend.listOfObject);
  }

  public ArrayList<ArrayList<String>> getListOfLines()
  {
    return listOfLines;
  }
  
  public ArrayList<String []> getListOfLinesAsArray()
  {
    ArrayList list = new ArrayList<String []>();
    for (int i = 0 ; i < listOfLines.size() ; i++)
    {
      int length = listOfLines.get(i).size();
      String[] strArr = new String[length];
      
      strArr = (String[])listOfLines.get(i).toArray(strArr);
      list.add(strArr);
    }
    
    return list;
  }
  
  public Object getRowData(int index)
  {
    if((index < 0) || (index >= listOfObject.size()))
    {
      Log.errorLog(className, "getRowData", "", "",  "Invalid Index = " + index);
      return null;
    }
    else
    {
      SyncPointLogRow logRow = (SyncPointLogRow)listOfObject.get(index);
       return logRow;    	
    }
  }
  
  public int getNumOfFields()
  {
    return numOfFields;
  }

  public void setNumOfFields(int numOfFields)
  {
    this.numOfFields = numOfFields;
  }

  public String[] getColumnNames()
  {
    return columnNames;
  }

  public void setColumnNames(String[] columnNames)
  {
    this.columnNames = columnNames;
  }
}
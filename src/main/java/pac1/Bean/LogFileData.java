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

public class LogFileData implements java.io.Serializable
{
  private static String className = "LogFileData";
  private static final long serialVersionUID = 1L;
  private ArrayList<ArrayList<String>> listOfLines = new ArrayList<ArrayList<String>>();
  private ArrayList<Object> listOfObject = new ArrayList<Object>();
  private int option = -1;
  
  public static final int EVENT_LOG = 1;
  public static final int DEBUG_TRACE_LOG = 2;
  
  long currentOffset = 0;
  int maxLineRead = 0;
  int numOfFields = -1;
  String testNum = "";
  
  String columnNames[];
  
  
  private boolean isMoreDataAvail;
  
  public LogFileData(int option)
  {
    this.option = option;
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
          //Commenting this beacuse it's not the right way of hadling bug 4342.
          //it was done before but now commiting it.
          //This is to handle special character
          /*if(i == strArr.length-1)
          {
            // Input    - hello\bgoodbye
            //event.log - hello%08goodbye
            String description = strArr[i];
            description = description.replace("%08", "\\b");
            
            //Input     - hello\tgoodbye
            //event.log - hello%09goodbye
            description = description.replace("%09", "\\t");
            
            //Input     - hello\ngoodbye
            //event.log - hello%0Agoodbye
            description = description.replace("%0A", "\\n");
            
            //Input     - hello\rgoodbye
            //event.log - hello%0Dgoodbye
            description = description.replace("%0D", "\\r");
            
            //Input     - hello\fgoodbye
            //event.log - hello%0Cgoodbye
            description = description.replace("%0C", "\\f");
            
            //Input     - hello\vgoodbye
            //event.log - hello%0Bgoodbye
            description = description.replace("%0B", "\\v");
            
            listOfFields.add(description);
          }
          else*/
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
      if(option == EVENT_LOG)
      {
        EventLogRow eventLogRow = new  EventLogRow(getTestRun());
        if(!eventLogRow.setLogDataToLogRow(this, listOfLines.size() -1))
        {
          Log.errorLog(className, "setLineToList", "", "",  "Can not set Log data to Log Row for list = " + listOfFields + ", so removing line from list. And this is ignored.");
          listOfLines.remove(listOfLines.size() -1);
          return true;
        }
        listOfObject.add(eventLogRow);
      }
      else if(option == DEBUG_TRACE_LOG)
      {
        Log.debugLog(className, "setLineToList", "", "",  "Came in Debug Trace Log file.");
      }
      
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
    listOfLines.addAll(listOfLinesToAppend);
  }
  
  public void appendList(LogFileData logFileDataToAppend)
  {
    this.setMoreDataAvail(logFileDataToAppend.isMoreDataAvail());
    listOfLines.addAll(logFileDataToAppend.listOfLines);
    listOfObject.addAll(logFileDataToAppend.listOfObject);
  }

  public long getCurrentOffset()
  {
    return currentOffset;
  }

  public int getMaxLineRead()
  {
    return maxLineRead;
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
    
    if(option == EVENT_LOG)
    {
      EventLogRow logRow = (EventLogRow)listOfObject.get(index);
      return logRow;
    }
    else if(option == DEBUG_TRACE_LOG)
    {
      Log.debugLog(className, "getRowData", "", "",  "Debug Log Type");
      return null;
    }
    else
    {
      Log.errorLog(className, "getRowData", "", "",  "Invalid type = " + option);
      return null;
    }
  }
  

  public void setCurrentOffset(long currentOffset)
  {
    this.currentOffset = currentOffset;
  }

  public void setMaxLineRead(int maxLineRead)
  {
    this.maxLineRead = maxLineRead;
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
  
  public boolean isMoreDataAvail()
  {
    return isMoreDataAvail;
  }
  
  public void setMoreDataAvail(boolean isMoreDataAvail)
  {
    this.isMoreDataAvail = isMoreDataAvail;
  }
}
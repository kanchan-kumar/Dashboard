package pac1.Bean;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

public class NDGenerateCompareData 
{
  private String className = "NDGenerateCompareData";
  private String[][] dataForSeqDia = null;
  private String[][] dataForCompareTable = null;
  private int methodTimeGreaterOrEqual = -1;
  private int percentLessThan = -1;
  private int percentGreaterThan = -1;
  private int diffLessThan = -1;
  private int diffGreaterThan = -1;
  private int numTopRecords = -1;
  private boolean errorStatusForSeqData = false;
  private ArrayList<String[][]> arrDataValueList = null;
  private ArrayList<String> arrFlowPathInsList = null;
  private ArrayList<String> headerToolTip = new ArrayList<String>();
  
  public NDGenerateCompareData(ArrayList<String[][]> arrDataValueList, ArrayList<String> arrFlowPathInsList)
  {
    Log.debugLog(className, "NDGenerateCompareData", "", "", "construtor created for arrDataValueList size:" + arrDataValueList.size() + ", arrFlowPathList:" + arrFlowPathInsList);
    try
    {
      if(arrDataValueList.size() != arrFlowPathInsList.size())
      {
        Log.debugLog(className, "NDGenerateCompareData", "", "", "Error: mismatch in dataList and flowPath List.");
        return;
      }
      this.arrDataValueList = arrDataValueList;
      this.arrFlowPathInsList = arrFlowPathInsList;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "NDGenerateCompareData", "", "", "", e);
    }
  }
  
  /**
   * This function generate data for sequence diagram.
   * for generating data for sequence diagram the flow path signature for all flopwPath instances should be same
   * @param arrDataValueList
   * @param arrFlowPathInsList
   */
  public String[][] getDataForSeqDiagram()
  {
    Log.debugLog(className, "getDataForSeqDiagram", "", "", "method called");
    try
    {
      int row = arrDataValueList.get(0).length;
      int cols = arrDataValueList.get(0)[0].length + 1;
      dataForSeqDia = new String[row][cols];
      
      //Data in 2D array.
      //EventId|FPInstance|FPSignature|TimeStamp|ThreadId|TierId|TierName|MethodId|MethodName|WallTime|CpuTime|BCIArg
      for(int i = 0; i < arrDataValueList.size(); i++)
      {
        String data[][] = arrDataValueList.get(i);
        for(int j = 0; j < data.length; j++)
        {
          if(i == 0)
          {
            for(int k = 0; k < data[j].length; k++)
            {
              dataForSeqDia[j][k] = data[j][k];
            }
            if(j == 0)
              dataForSeqDia[j][cols-1] = "CompareInfo";
            else
              dataForSeqDia[j][cols-1] = data[j][9];
          }
          else if(j != 0)
          {
            dataForSeqDia[j][cols-1] += "," + data[j][9] + "," + Math.abs((Long.parseLong(dataForSeqDia[j][9]) - Long.parseLong(data[j][9])));
          }
        }
      }
      if(methodTimeGreaterOrEqual != -1)
       applyFilterMethodTimeGreaterOrEqualForSeqDiaData();
     if(diffLessThan != -1 || diffGreaterThan != -1 || percentLessThan != -1 || percentGreaterThan != -1)
          applyFilterForSeqDiaData();
    }
    catch(Exception e)
    {
      errorStatusForSeqData = true;
      //e.printStackTrace();
      //Log.stackTraceLog(className, "generateDataForSeqDia", "", "", "", e);
    }
    Log.debugLog(className, "getDataForSeqDiagram", "", "", "method end");
    return dataForSeqDia;
  }
  
  private void applyFilterMethodTimeGreaterOrEqualForSeqDiaData()
  {
    Log.debugLog(className, "applyFilterDoNotShowZeroForSeqDiaData", "", "", "method called for methodTimeGreaterOrEqual: "+methodTimeGreaterOrEqual);
    try
    {
      ArrayList<String[]> filteredData = new ArrayList<String[]>();
      int idx = dataForSeqDia[0].length -1;
      for(int i = 0; i < dataForSeqDia.length; i++)
      {
        if( i == 0)
          filteredData.add(dataForSeqDia[i]);
        else
        {
          String arrTmp[] = dataForSeqDia[i][idx].split(",");
          boolean add = true;
          int idxWallTime = 0;
          for(int j = 0; j < arrTmp.length; j++)
          {
            if(j == idxWallTime && Double.parseDouble(arrTmp[j].trim()) < methodTimeGreaterOrEqual)
            {
              add = false;
              break;
            }
            else if(j == idxWallTime)
            {
              if(j == 0)
                idxWallTime++;
              else
                idxWallTime += 2;
            }
          }
          if(add)
            filteredData.add(dataForSeqDia[i]);
        }
      }
      dataForSeqDia = filteredData.toArray(new String[0][0]);
    }
    catch(Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "applyFilterDoNotShowZeroForSeqDiaData", "", "", "", e);
    }
  }
  
  private void applyFilterForSeqDiaData()
  {
    Log.debugLog(className, "applyFilterForSeqDiaData", "", "", "method called for percent >= " + percentGreaterThan + ", percent <= "+ percentLessThan + ", diff >= " + diffGreaterThan + ", diff <= "+diffLessThan);
    try
    {
      ArrayList<String[]> filteredData = new ArrayList<String[]>();
      int idx = dataForSeqDia[0].length -1;
      for(int i = 0; i < dataForSeqDia.length; i++)
      {
        int idxDiffLess = 2;
        int idxDiffGreater = 2;
        int idxPerLess = 1;
        int idxPerGreater = 1;
        if(i == 0)
          filteredData.add(dataForSeqDia[i]);
        else
        {
          String arrTmp[] = dataForSeqDia[i][idx].split(",");
          for(int j = 0; j < arrTmp.length; j++)
          {
            if(diffLessThan != -1 && diffGreaterThan == -1)
              {
                if(j == idxDiffLess && Double.parseDouble(arrTmp[j]) <= diffLessThan)
                {
                  filteredData.add(dataForSeqDia[i]);
                  break;
                }
                else if(j == idxDiffLess)
                  idxDiffLess += 2;
              }
              
              if(diffGreaterThan != -1 && diffLessThan == -1)
              {
                if(j == idxDiffGreater && Double.parseDouble(arrTmp[j]) >= diffGreaterThan)
                {
                  filteredData.add(dataForSeqDia[i]);
                  break;
                }
                else if(j == idxDiffGreater)
                  idxDiffGreater += 2;
              }
              
              if(diffGreaterThan != -1 && diffLessThan != -1)
              {
                if(j == idxDiffGreater && Double.parseDouble(arrTmp[j]) >= diffGreaterThan && Double.parseDouble(arrTmp[j]) <= diffLessThan)
                {
                  filteredData.add(dataForSeqDia[i]);
                  break;
                }
                else if(j == idxDiffGreater)
                  idxDiffGreater += 2;
              }
              
              if(percentLessThan != -1 && percentGreaterThan == -1)
              {
                if(j == idxPerLess && (Double.parseDouble(arrTmp[j+1])*100/Double.parseDouble(arrTmp[0])) <= percentLessThan)
                {
                  filteredData.add(dataForSeqDia[i]);
                break;
                }
                else if(j == idxPerLess)
                  idxPerLess += 2;
              }
              
              if(percentGreaterThan != -1 && percentLessThan == -1)
              {
                if(j == idxPerGreater && (Double.parseDouble(arrTmp[j+1])*100/Double.parseDouble(arrTmp[0])) >= percentGreaterThan)
                {
                   filteredData.add(dataForSeqDia[i]);
                   break;
                }
                else if(j == idxPerGreater)
                  idxPerGreater += 2;
              }
              
              if(percentGreaterThan != -1 && percentLessThan != -1)
              {
                if(j == idxPerGreater && (Double.parseDouble(arrTmp[j+1])*100/Double.parseDouble(arrTmp[0])) >= percentGreaterThan && (Double.parseDouble(arrTmp[j+1])*100/Double.parseDouble(arrTmp[0])) <= percentLessThan)
                {
                   filteredData.add(dataForSeqDia[i]);
                   break;
                }
                else if(j == idxPerGreater)
                  idxPerGreater += 2;
              }
          }
        }
      }
      
      dataForSeqDia = filteredData.toArray(new String[0][0]);
    }
    catch(Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "applyFilterForSeqDiaData", "", "", "", e);
    }
  }
  
  /**
   * This function generate data for table.
   * @param arrDataValueList
   * @param arrFlowPathInsList
   */
  public String[][] getDataForTable()
  {
    Log.debugLog(className, "getDataForTable", "", "", "method start");
    try
    {
      //Data in 2D array.
      //EventId|FPInstance|FPSignature|TimeStamp|ThreadId|TierId|TierName|MethodId|MethodName|WallTime|CpuTime|BCIArg
      int idxEventId = 0;
      int idxMethodName = 8;
      int idxWallTime = 9;
      DecimalFormat df = new DecimalFormat("#.##");
      ArrayList<ArrayList<String>> tableData = new ArrayList<ArrayList<String>>();
      ArrayList<String> tmp = new ArrayList<String>();
      
      for(int i = 0; i < arrDataValueList.size(); i++)
      {
        String data[][] = arrDataValueList.get(i);
        ArrayList<String> checkDulicate =  new ArrayList<String>();
        
        for(int j = 0; j < data.length; j++)
        {
          if(i == 0)
          {
            if(j == 0) //Create header
            {
              tmp.add("Method Signature");
              headerToolTip.add("Method Signature");
              tableData.add(tmp);
              tmp = new ArrayList<String>();
              tmp.add("FP"+(i+1) + " Wall Time");
              headerToolTip.add(arrFlowPathInsList.get(i) + " Wall Time");
              tableData.add(tmp);
            }
            else if(data[j][idxEventId].equals("0"))
            {
              String methodSignature = data[j][idxMethodName];
              if(tableData.get(0).indexOf(methodSignature) == -1)
              {
                tableData.get(0).add(methodSignature);
                tableData.get(1).add(data[j][idxWallTime]);
              }
            }
          }
          else
          {
            if(j == 0) // Create Header
            {
              tmp = new ArrayList<String>();
              tmp.add("FP"+(i+1) + " Wall Time");
              headerToolTip.add(arrFlowPathInsList.get(i) + " Wall Time");
              tableData.add(tmp);
              tmp = new ArrayList<String>();
              tmp.add("FP"+(i+1) + " Change");
              headerToolTip.add(arrFlowPathInsList.get(i) + " Change");
              tableData.add(tmp);
              tmp = new ArrayList<String>();
              tmp.add("FP"+(i+1) + " Change(%)");
              headerToolTip.add(arrFlowPathInsList.get(i) + " Change(%)");
              tableData.add(tmp);
              for(int  k = 1; k < tableData.get(0).size(); k++)
              {
                tableData.get(i*3 - 1).add("NA");
                tableData.get(i*3).add("NA");
                tableData.get(i*3 + 1).add("NA");
              }
            }
            else if(data[j][idxEventId].equals("0"))
            {
              String methodSignature = data[j][idxMethodName];
              if(checkDulicate.indexOf(methodSignature) == -1)
                checkDulicate.add(methodSignature);
              else
                continue;
                  
              int idx = tableData.get(0).indexOf(methodSignature);
              if(idx == -1)
              {
                tableData.get(0).add(methodSignature);
                tableData.get(1).add("NA");
                for(int  k = 2; k < tableData.size()-3; k++)
                {
                  tableData.get(k).add("NA");
                }
                tableData.get(i*3 -1).add(data[j][idxWallTime]);
                tableData.get(i*3).add("NA");
                tableData.get(i*3 + 1).add("NA");
              }
              else
              {
                String wallTime = tableData.get(1).get(idx);
                String diff = "NA";
                String percent = "NA";
                if(!wallTime.equals("NA"))
                {
                  diff = df.format(Math.abs(Double.parseDouble(wallTime) - Double.parseDouble(data[j][idxWallTime])));
                  percent = "0.00";
                  if(!wallTime.equals("0"))
                    percent = df.format(Math.abs(Double.parseDouble(wallTime) - Double.parseDouble(data[j][idxWallTime]))*100/Double.parseDouble(wallTime));
                }
                
                tableData.get(i*3 -1).remove(idx);
                tableData.get(i*3 -1).add(idx, data[j][idxWallTime]);
                tableData.get(i*3).remove(idx);
                tableData.get(i*3).add(idx, diff);
                tableData.get(i*3 + 1).remove(idx);
                tableData.get(i*3 + 1).add(idx, percent);
              }
            }
          }
        }
      }
      
      dataForCompareTable = new String[tableData.get(0).size()][tableData.size()+3];
      for(int i = 0; i < tableData.size(); i++)
      {
        for(int j = 0; j < tableData.get(i).size(); j++)
        {
          if(i == 0)
          {
            if(j == 0)
            {
              dataForCompareTable[j][0] = "Package";
              dataForCompareTable[j][1] = "Class";
              dataForCompareTable[j][2] = "Method";
              dataForCompareTable[j][3] = "Method signature"; 
              headerToolTip.add(0, "Package");
              headerToolTip.add(1, "Class");
              headerToolTip.add(2, "Method");
            }
            else
            {
              dataForCompareTable[j][0] = getPackageName(tableData.get(i).get(j));
              dataForCompareTable[j][1] = getClassName(tableData.get(i).get(j));
              dataForCompareTable[j][2] = getMethodNameWithoutSignature(tableData.get(i).get(j));
              dataForCompareTable[j][3] = getMethodNameWithSignature(tableData.get(i).get(j));
            }
          }
          else
          {
            dataForCompareTable[j][i+3] = tableData.get(i).get(j);
          }
        }
      }
      
      
      if(methodTimeGreaterOrEqual != -1)
        applyFilterMethodTimeGreaterOrEqualForTableData();
      if(diffLessThan != -1 || diffGreaterThan != -1 || percentLessThan != -1 || percentGreaterThan != -1)
          applyFilterForTableData();
      if(numTopRecords != -1)
        filterForNumTopRecords();
    }
    catch(Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "getDataForTable", "", "", "", e);
    }
    return dataForCompareTable;
  }
  
  private void applyFilterMethodTimeGreaterOrEqualForTableData()
  {
    Log.debugLog(className, "applyFilterMethodTimeGreaterOrEqualForTableData", "", "", "method called for methodTimeGreaterOrEqual: "+methodTimeGreaterOrEqual);
    try
    {
      ArrayList<String[]> filteredData = new ArrayList<String[]>();
      for(int i = 0; i < dataForCompareTable.length; i++)
      {
        int idxMethodTime = 4;
        if(i == 0)
          filteredData.add(dataForCompareTable[i]);
        else
        {
          boolean add = true;
          for(int j = 0; j < dataForCompareTable[i].length; j++)
          {
            if(j == idxMethodTime && !dataForCompareTable[i][j].equals("NA") && Double.parseDouble(dataForCompareTable[i][j]) < methodTimeGreaterOrEqual)
            {
              add = false;
              break;
            }
            else if(j == idxMethodTime)
            {
              if(j == 4)
                idxMethodTime++;
              else
                idxMethodTime += 3;
            }
          }
          if(add)
            filteredData.add(dataForCompareTable[i]);
        }
      }
      
      dataForCompareTable = filteredData.toArray(new String[0][0]);
    }
    catch(Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "applyFilterMethodTimeGreaterOrEqualForTableData", "", "", "", e);
    }
  }
  
  private void applyFilterForTableData()
  {
    Log.debugLog(className, "applyFilterForTableData", "", "", "method called for percent >= " + percentGreaterThan + ", percent <= "+ percentLessThan + ", diff >= " + diffGreaterThan + ", diff <= "+diffLessThan);
    try
    {
      ArrayList<String[]> filteredData = new ArrayList<String[]>();
      for(int i = 0; i < dataForCompareTable.length; i++)
      {
        int idxDiffLess = 6;
        int idxDiffGreater = 6;
        int idxPerLess = 7;
        int idxPerGreater = 7;
        if(i == 0)
          filteredData.add(dataForCompareTable[i]);
        else
        {
          for(int j = 0; j < dataForCompareTable[i].length; j++)
          {
            if(diffLessThan != -1 && diffGreaterThan == -1)
              {
                if(j == idxDiffLess && !dataForCompareTable[i][j].equals("NA") && Double.parseDouble(dataForCompareTable[i][j]) <= diffLessThan)
                {
                  filteredData.add(dataForCompareTable[i]);
                  break;
                }
                else if(j == idxDiffLess)
                  idxDiffLess += 3;
              }
              
              if(diffGreaterThan != -1 && diffLessThan == -1)
              {
                if(j == idxDiffGreater && !dataForCompareTable[i][j].equals("NA") && Double.parseDouble(dataForCompareTable[i][j]) >= diffGreaterThan)
                {
                  filteredData.add(dataForCompareTable[i]);
                  break;
                }
                else if(j == idxDiffGreater)
                  idxDiffGreater += 3;
              }
              
              if(diffLessThan != -1 && diffGreaterThan != -1)
              {
                if(j == idxDiffLess && !dataForCompareTable[i][j].equals("NA") && Double.parseDouble(dataForCompareTable[i][j]) <= diffLessThan && Double.parseDouble(dataForCompareTable[i][j]) >= diffGreaterThan)
                {
                  filteredData.add(dataForCompareTable[i]);
                  break;
                }
                else if(j == idxDiffLess)
                  idxDiffLess += 3;
              }
              
              if(percentLessThan != -1 && percentGreaterThan == -1)
              {
                if(j == idxPerLess && !dataForCompareTable[i][j].equals("NA") && Double.parseDouble(dataForCompareTable[i][j]) <= percentLessThan)
                {
                  filteredData.add(dataForCompareTable[i]);
                break;
                }
                else if(j == idxPerLess)
                  idxPerLess += 3;
              }
              
              if(percentGreaterThan != -1 && percentLessThan == -1)
              {
                if(j == idxPerGreater && !dataForCompareTable[i][j].equals("NA") && Double.parseDouble(dataForCompareTable[i][j]) >= percentGreaterThan)
                {
                   filteredData.add(dataForCompareTable[i]);
                   break;
                }
                else if(j == idxPerGreater)
                  idxPerGreater += 3;
              }
              
              if(percentLessThan != -1 && percentGreaterThan != -1)
              {
              if(j == idxPerLess && !dataForCompareTable[i][j].equals("NA") && Double.parseDouble(dataForCompareTable[i][j]) <= percentLessThan && Double.parseDouble(dataForCompareTable[i][j]) >= percentGreaterThan)
              {
                filteredData.add(dataForCompareTable[i]);
                break;
              }
              else if(j == idxPerLess)
                idxPerLess += 3;
            }
          }
        }
      }
      
      dataForCompareTable = filteredData.toArray(new String[0][0]);
    }
    catch(Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "applyFilterForTableData", "", "", "", e);
    }
  }
  
  private void filterForNumTopRecords()
  {
    Arrays.sort(dataForCompareTable,  new ColumnComparator(4, false));
    String result[][] = null;
    if((numTopRecords + 1) < dataForCompareTable.length)
      result = new String[numTopRecords + 1][dataForCompareTable[0].length];
    else
      return;
    for(int i = 0; i < dataForCompareTable.length && i < (numTopRecords+1); i++)
    {
      result[i] = dataForCompareTable[i];
    }
    
    dataForCompareTable = result;
  }
  
  public String[][] groupChangesAndPerChangeTogether(String[][] data)
  {
    Log.debugLog(className, "groupChangesAndPerChangeTogether", "", "", "method called");
    try
    {
      String result[][] = new String[data.length][data[0].length];
      ArrayList<String> headerToolTipTmp = new ArrayList<String>();
      for(int i = 0; i < data.length; i++)
      {
        int idx = 0;
        for(int j = 0; j < data[i].length; j++)
        {
          if(j < 4)
          {
            result[i][idx++] = data[i][j];
            if(i == 0)
              headerToolTipTmp.add(headerToolTip.get(j));
          }
          if(data[0][j].contains("Wall Time"))
          { 
            result[i][idx++] = data[i][j];
            if(i == 0)
              headerToolTipTmp.add(headerToolTip.get(j));
          }
        }
      
        for(int j = 0; j < data[i].length; j++)
        {
          if(data[0][j].trim().endsWith("Change"))
          {
            result[i][idx++] = data[i][j];
            if(i == 0)
                headerToolTipTmp.add(headerToolTip.get(j));
          }
        }
      
        for(int j = 0; j < data[i].length; j++)
        {
          if(data[0][j].trim().endsWith("Change(%)"))
          {
            result[i][idx++] = data[i][j];
            if(i == 0)
                headerToolTipTmp.add(headerToolTip.get(j));
          }
        }
      }
    
      headerToolTip = headerToolTipTmp;
      return result;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "groupChangesAndPerChangeTogether", "", "", "", e);
    }
    return null;
  }
  
  public String[][] groupChangesAndPerChangeAsPair(String[][] data)
  {
    Log.debugLog(className, "groupChangesAndPerChangeAsPair", "", "", "method called");
    try
    {
      String result[][] = new String[data.length][data[0].length];
      ArrayList<String> headerToolTipTmp = new ArrayList<String>();
      for(int i = 0; i < data.length; i++)
      {
        int idx = 0;
        for(int j = 0; j < data[i].length; j++)
        {
          if(j < 4)
          {
            result[i][idx++] = data[i][j];
            if(i == 0)
              headerToolTipTmp.add(headerToolTip.get(j));
          }
           if(data[0][j].contains("Wall Time"))
          {
            result[i][idx++] = data[i][j];
            if(i == 0)
              headerToolTipTmp.add(headerToolTip.get(j));
          }
        }

        for(int j = 0; j < data[i].length; j++)
        {
          if(data[0][j].trim().endsWith("Change"))
          {
            result[i][idx++] = data[i][j];
            if(i == 0)
              headerToolTipTmp.add(headerToolTip.get(j));
            result[i][idx++] = data[i][j+1];
            if(i == 0)
              headerToolTipTmp.add(headerToolTip.get(j+1));
          }
        }
      }
    
      headerToolTip = headerToolTipTmp;
      return result;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "groupChangesAndPerChangeAsPair", "", "", "", e);
    }
    return null;
  }
  
  //Class that extends Comparator
  class ColumnComparator implements Comparator<Object>
  {
    int columnToSort;
    boolean ascending;
    ColumnComparator(int columnToSort, boolean ascending)
    {
      this.columnToSort = columnToSort;
      this.ascending = ascending;
    }
    //overriding compare method
    public int compare(Object o1, Object o2) 
    {
      String[] row1 = (String[]) o1;
      String[] row2 = (String[]) o2;
     //compare the columns to sort
     int cmp = 0;
     double d1 = 0.0;
     double d2 = 0.0;
     if(row1[columnToSort].trim().equals("FP1 Wall Time"))
       return 0;
     
     if(!row1[columnToSort].equals("NA"))
       d1 = Double.parseDouble(row1[columnToSort]);
     if(!row2[columnToSort].equals("NA"))
       d2 = Double.parseDouble(row2[columnToSort]);
     if (d1 < d2)
       cmp = -1;
     else if (d1 > d2)
       cmp = 1;
     else
       return 0;
     return ascending ? cmp : -cmp;
    }
  }
  
  private String getPackageName(String methodSignature)
  {
    int lastIndexOfDot = methodSignature.lastIndexOf(".");
    if(lastIndexOfDot == -1)
      return methodSignature;
    else
    {
      String tempStr = methodSignature.substring(0, lastIndexOfDot);
      lastIndexOfDot = tempStr.lastIndexOf(".");

      if(lastIndexOfDot == -1)
        return tempStr;

      tempStr = tempStr.substring(0, lastIndexOfDot);
      return tempStr;
    }
  }
  
  private String getClassName(String methodSignature)
  {
    int lastIndexOfDot = methodSignature.lastIndexOf(".");
    if(lastIndexOfDot == -1)
      return methodSignature;
    else
    {
      String tempStr = methodSignature.substring(0, lastIndexOfDot);
      lastIndexOfDot = tempStr.lastIndexOf(".");

      if(lastIndexOfDot == -1)
        return tempStr;

      tempStr = tempStr.substring(lastIndexOfDot+1, tempStr.length());
      return tempStr;
    }
  }
  
  private String getMethodNameWithoutSignature(String methodSignature)
  {
    int lastIndexOfDot = methodSignature.lastIndexOf(".");
    if(lastIndexOfDot == -1)
      return methodSignature;

    int lastIndexOfPara = methodSignature.lastIndexOf("(");
    String methodName = methodSignature.substring(lastIndexOfDot+1, lastIndexOfPara);

    return methodName+"()";
  }

  private String getMethodNameWithSignature(String methodSignature)
  {
    int lastIndexOfDot = methodSignature.lastIndexOf(".");
    if(lastIndexOfDot == -1)
      return methodSignature.substring(0, methodSignature.length()-1);

    int lastIndexOfParaStart = methodSignature.lastIndexOf("(");
    int lastIndexOfParaEnd = methodSignature.lastIndexOf(")");
    String methodName = methodSignature.substring(lastIndexOfDot+1, lastIndexOfParaStart);

    String argumentStr = methodSignature.substring(lastIndexOfParaStart, lastIndexOfParaEnd+1);
    String arguments[] = argumentStr.split(";");
    for(int i = 0; i < arguments.length; i++)
    {
      if(i == arguments.length-1)
        methodName += arguments[i].substring(arguments[i].lastIndexOf("/")+1, arguments[i].length());
      else if(i == 0 && arguments.length == 2)
        methodName += "("+arguments[i].substring(arguments[i].lastIndexOf("/")+1, arguments[i].length());
      else if(i == 0 )
        methodName += "("+arguments[i].substring(arguments[i].lastIndexOf("/")+1, arguments[i].length()) + ",";
      else if(i == arguments.length-2)
        methodName += arguments[i].substring(arguments[i].lastIndexOf("/")+1, arguments[i].length());
      else
        methodName += arguments[i].substring(arguments[i].lastIndexOf("/")+1, arguments[i].length()) + ",";
    }
    //String methodName = methodSignature.substring(lastIndexOfDot+1, methodSignature.length());

    methodSignature = methodSignature.substring(0, lastIndexOfDot);
    lastIndexOfDot = methodSignature.lastIndexOf(".");
    if(lastIndexOfDot == -1)
      return methodName;

    //methodName = methodSignature.substring(lastIndexOfDot+1, methodSignature.length()) +"."+ methodName;

    return methodName;
  }
  
  public ArrayList<String> getHeaderToolTip()
  {
    return headerToolTip;
  }
  
  public void setMethodTimeGreaterOrEqual(int value)
  {
    methodTimeGreaterOrEqual = value;
  }
  
  public void setPercentLessThan(int value)
  {
    percentLessThan = value;
  }
  
  public void setPercentGreaterThan(int value)
  {
    percentGreaterThan = value;
  }
  
  public void setDiffLessThan(int value)
  {
    diffLessThan = value;
  }
  
  public void setDiffGreaterThan(int value)
  {
    diffGreaterThan = value;
  }

  public void setNumTopRecord(int value)
  {
    numTopRecords = value;
  }
  
  public boolean getErrorStatusForSeqData()
  {
    return errorStatusForSeqData;
  }
  
  public static void main(String[] arg)
  {
    try
    {
       ArrayList<String[][]> arrDataList = new ArrayList<String[][]>();
       String files[] = {"file1.txt", "file2.txt", "file4.txt", "file5.txt"};
       for(int i = 0; i < files.length; i++)
       {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("c:/home/netstorm/work/"+ files[i])));
        Vector vec = new Vector();
        String str = null;
        while((str = br.readLine()) != null)
        {
          vec.add(str);
        }
        arrDataList.add(rptUtilsBean.getRecFlds(vec, "", "", " "));
       }
       
       ArrayList<String> arrInsList = new ArrayList<String>();
       arrInsList.add("file1");
       arrInsList.add("file2");
       arrInsList.add("file3");
       arrInsList.add("file5");
       NDGenerateCompareData ob = new NDGenerateCompareData(arrDataList, arrInsList);
       //String[][] result = ob.getDataForSeqDiagram();
       /*for(int i = 0 ; i < result.length; i++)
       {
          for(int j = 0; j < result[i].length; j++)
              System.out.print(result[i][j] + "|");
          System.out.println();
       }*/
       
       System.out.println("------------------------------");
      String[][] result = ob.getDataForTable();
      result = ob.groupChangesAndPerChangeAsPair(result);
      for(int i = 0 ; i < result.length; i++)
      {
         for(int j = 0; j < result[i].length; j++)
             System.out.print(result[i][j] + "|");
         System.out.println();
      }
      
      ArrayList<String> header = ob.getHeaderToolTip();
      for(int i = 0; i < header.size(); i++)
          System.out.println(header.get(i));
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
}

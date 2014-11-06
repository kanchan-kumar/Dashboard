package pac1.Bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

public class TransactionCompareReport
{
  private static String className = "TransactionCompareReport";
  
  public TransactionCompareReport() {}
 
  public ArrayList getCompareReport(ArrayList<String> genTRList , ArrayList<String> colNameList , String testRun, String viewMode , String userName, StringBuffer errMsg)
  {
    Log.debugLog(className, "getCompareReport", "", "", "Method called, Test Run = " + testRun + ", view mode = " + viewMode);
    
    try
    {
      if(genTRList == null || genTRList.size() == 0)
      {
        Log.errorLog(className, "getCompareReport", "", "", "Given generator Test Run is null or empty");
        return null;
      }
        
      if(colNameList == null || colNameList.size() == 0)
      {
        Log.errorLog(className, "getCompareReport", "", "", "Given column name list is null or empty");
        return null;
      }
      
      Log.debugLog(className, "getCompareReport", "", "", "Cotroller/Generator List = " + genTRList + ", Column Names = " + colNameList);
      
      Vector vecOutput = getTransSummaryDataFromCommand(testRun, viewMode, userName, genTRList ,  errMsg);
    
     // Vector vecOutput = getTestData(genTRList);
      
      if(vecOutput == null)
      {
        Log.errorLog(className, "getCompareReport", "", "", "Error is getting compare report data" + errMsg);
        

        return null;
      }
      
      HashMap<String, HashMap<String, ArrayList<String>>> reportMap = new HashMap <String, HashMap<String, ArrayList<String>>>();
      
      //Key for every generator or controller
      String key = "";
      
      Set <String> transactionSet = new TreeSet<String>();
     
      boolean istransNameAdded = false;
      for(int i = 0 ; i < vecOutput.size() ; i++)
      {
        String strLine = (String)vecOutput.get(i);
        //if line starts with generator then take it as a key
        if(strLine.startsWith("Generator") || strLine.startsWith("generator"))
        {
          //System.out.println(key);
          key = strLine.substring(strLine.indexOf("=") + 1).trim();
        }
        else
        { 
           //In else condition need to get data for Generator until second another controller or end of output
                    
          if(strLine.startsWith("ERROR:selected"))
            continue;
          
           String [] arrData = rptUtilsBean.split(strLine, "|");
           //This list will contain list for each row as list, further it will convert in to 2D Array
           HashMap<String , ArrayList<String>> controllerReport = new HashMap<String , ArrayList<String>>();
           //Contain column list which match with given column list
           ArrayList<String> colList =  new ArrayList<String>(); 
           //Contains column number which are required to parse 
           ArrayList<Integer> mapColList =  new ArrayList<Integer>();
           
           //reading header line and get column number which need to be store in report
           for(int j = 0 ; j < arrData.length ; j++ )
           {
             if(colNameList.indexOf(arrData[j]) > -1)
             {
               colList.add(arrData[j]);
               mapColList.add(j);
             }
           }
           
           //Column header is added into report
           //controllerReport.add(colList);
           
           i++;//need to increment because this line is read
           String transName = "";
           //reading every line a loop until get another report ot end of the output, below line contains data
           for(;  i < vecOutput.size() ; i++ )
           { 
             strLine = (String)vecOutput.get(i);
             
             //if line is started with Generator, then need to decrement loop variable and break the loop to read another one
             if(strLine.startsWith("Generator") || strLine.startsWith("generator"))
             {
               i--;
               istransNameAdded = true;
               break;
             }
             
             ArrayList<String> dataList = new ArrayList<String>();//this list for data row
             arrData = rptUtilsBean.split(strLine, "|");
             //System.out.println(strLine + " **");
             for(int j = 0 ; j < arrData.length ; j++)
             {
               if(j == 0 )
               {
                 transName = arrData[j];
                 if(!arrData[j].equalsIgnoreCase("All"))
                   transactionSet.add(arrData[j]);
               }
               //Only required column are added in the list
               if(mapColList.indexOf(j) > -1)
               {
                 dataList.add(arrData[j]);
               }
             }
             controllerReport.put(transName, dataList);
           }
          //Adding report in map 
          reportMap.put(key, controllerReport);
        }
      }
      
      ArrayList data = new ArrayList();
      data.add(transactionSet);
      data.add(reportMap);
      return data;
    }
    catch (Exception e) 
    {
      Log.stackTraceLog(className, "getCompareReport", "", "", "Error in generating compare report", e);
      return null;
    }
  }
  
  private Vector getTransSummaryDataFromCommand(String testRun, String testMode, String userName , ArrayList<String> genTRList , StringBuffer errMsg)
  {
    Log.debugLog(className, "getTransSummaryDataFromCommand", "", "", "Method called. TestRun Number = " + testRun + ", userName = " + userName);

    try
    {
      String cmd = "nsi_tx_summary";
      String cmdArgs = "";
      
      cmdArgs = "-t " + testRun + " -g " + testRun;
      
      for(int i = 0 ; i < genTRList.size() ; i++)
      {
        cmdArgs = cmdArgs + "," + genTRList.get(i) ;
      }
       
      cmdArgs = cmdArgs + " -m 1";
      
      String pathForCmd = "";
      String usrName = userName;
      String runAsUser = null;
      
      if(testMode.equals("offline"))
        cmdArgs = cmdArgs + " -o";
      
     
      CmdExec cmdExec = new CmdExec();
      
      Vector vecCmdOutput = cmdExec.getResultByCommand(pathForCmd + cmd, cmdArgs, CmdExec.NETSTORM_CMD, userName, runAsUser);
      
      if(vecCmdOutput == null)
      {
        Log.errorLog(className, "getTransSummaryDataFromCommand", "", "", "vecCmdOutput is comming null");
        errMsg.append("Error in getting data from command - nsi_tx_summary -t " + cmdArgs);
        return null;
      }


      if((vecCmdOutput.size() > 0) && ((String)vecCmdOutput.lastElement()).startsWith("ERROR:selected"))
      {
        return vecCmdOutput;
      }

      
      if((vecCmdOutput.size() > 0) && ((String)vecCmdOutput.lastElement()).startsWith("ERROR"))
      {
        for(int i = 0; i < (vecCmdOutput.size() - 1); i++)
          errMsg.append(vecCmdOutput.elementAt(i).toString() + "\n");
        return null;
      }

      return vecCmdOutput;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "getTransSummaryDataFromCommand", "", "", "Exception - " + e);
      return null;
    }
  }
   
  private Vector getTestData(ArrayList<String> genList)
  {
    Vector vecOutput = new Vector();
    int count = 10;
    for(int j = 0 ; j < genList.size() ; j++)
    { 
      vecOutput.add("Generator=TR" + genList.get(j)+ "\n");
      vecOutput.add("Transaction Name|Min (sec)|Avg (sec)|Max (sec)|Std Dev|Initiated|Completed|Success|Failure (%)|Completed (Per sec)|NetCache Hits (Pct)\n");
      for(int i = 0; i < 10; i++)
      {
        double minTime = 0.01 + count / 10;
        double maxTime = 1.01 + count / 10;
        double avgTime = 0.50 + count / 10;
        double stdDev = 0.41 + count / 10;
        int initiated = i + (count + 5000) * (i % 2);
        int completed = i + (count + 4500) * (i % 2);
        int sucess = i + (count + 4000) * (i % 2);
        int failures = (count + 1000) * (i % 2);

        double failuresPct = (failures * 100) / (completed + 1);
        double completedPerSec = completed / 10;
        String transName = "Trans_" + i;
        if(i == 0)
        transName = "ATrans_" + i;
        else if(i == 2)
        transName = "GTrans_" + i;
        else if( i == 3)
        transName = "ITrans_" + i;
        
        String  strTransSummary = transName + "|" + minTime + "|" + maxTime + "|" + avgTime + "|" + stdDev + "|" + initiated + "|" + completed + "|" + sucess + "|" + failuresPct + "|" + completedPerSec + "|0\n";
        vecOutput.add(strTransSummary);
        count++;
      }
      String allString = "ALL|1.123|2.345|1.111|.234|12345|34567|5678|2.234|123.123|0\n";
      vecOutput.add(allString);
    }
    
    return vecOutput;
  }
 
  /*
  public String [][] convertCompareReportIn2D(HashMap<String, HashMap<String, ArrayList<String>>> reportMap , TreeSet<String> unigueTransSet , ArrayList<String> TRList , ArrayList<String> colList , String TRNum , boolean isControllerInfoReqiured )
  {
    try
    {
      String data [][] = null;
     
      if(reportMap == null || reportMap.size() <= 0 || unigueTransSet == null || unigueTransSet.size() <= 0)
      {
        return null;
      }
      
      if(isControllerInfoReqiured)
      {
        TRList.add(0 , TRNum);
      }
      
      data = new String[unigueTransSet.size() + 1][TRList.size() * colList.size() + 1];
      int colIncrement = 0;
      int  i = 0;
      Object [] transArray = unigueTransSet.toArray();
      
      for( ; i < transArray.length; i++)
      {
        data [i][colIncrement] = (String)transArray[i];
      }
      
      data [i][colIncrement] = "All Transactions";
      
      colIncrement ++;
      for(i = 0 ; i < TRList.size() ; i++)
      {
        String key = "TR" + TRList.get(i);
        
        HashMap<String, ArrayList<String>> genMap = reportMap.get(key);
        String transkey = "";
        int j = 0;
        for(; j < transArray.length ; j++)
        {
          transkey = (String)transArray[j];
         
          ArrayList<String> dataList = genMap.get(transkey);
          
          if(dataList != null)
          {
            for(int k = 0 ; k < colList.size() ; k++)
            {
              data[j][colIncrement + k] = dataList.get(k);
            }
          }
          else
          {
            for(int k = 0 ; k < colList.size() ; k++)
            {
              data[j][colIncrement + k] = "-";
            }
          }
        }
        
        transkey = "ALL";
        ArrayList<String> dataList = genMap.get(transkey);
        
        if(dataList != null)
        {
          for(int k = 0 ; k < colList.size() ; k++)
          {
            data[j][colIncrement + k] = dataList.get(k);
          }
        }
        else
        {
          for(int k = 0 ; k < colList.size() ; k++)
          {
            data[j][colIncrement + k] = "-";
          }
        }
        
        colIncrement = colIncrement + colList.size() ;
      }
        
      for(int l = 0 ; l < data.length ; l++)
    {
      for(int j = 0 ; j < data[0].length ; j++)
        System.out.print(data[l][j] + "  ");
      System.out.println();
    }
      return data;
    }
    catch (Exception e) 
    {
     e.printStackTrace();
      return null;
    }
  }
  */
  
  public static void main(String ar [])
  {
    TransactionCompareReport reportObj =  new TransactionCompareReport();
    ArrayList genTRList = new ArrayList<String>();
    ArrayList<String> colNameList = new ArrayList<String>();
    genTRList.add("1234");
    genTRList.add("2423");
    genTRList.add("1224");
    genTRList.add("8232");
    colNameList.add("Min (sec)");
    colNameList.add("Avg (sec)");
    //colNameList.add("Completed");
    //colNameList.add("Std Dev");
   
     ArrayList revicedata = reportObj.getCompareReport(genTRList, colNameList, "1234", "offline", "netstorm", new StringBuffer());
     TreeSet<String> tranSet = (TreeSet<String>)revicedata.get(0);
     HashMap<String, HashMap<String, ArrayList<String>>>  report = (HashMap<String, HashMap<String, ArrayList<String>>>)revicedata.get(1);
     //reportObj.convertCompareReportIn2D(report, tranSet, genTRList, colNameList, "1234", true);
   /* for(Map.Entry<String, HashMap<String, ArrayList<String>>> entry : report.entrySet())
    {
      String key = entry.getKey();
      System.out.println(key);
      HashMap<String, ArrayList<String>> data = report.get(key);
      for(Map.Entry<String, ArrayList<String>> entry1 : data.entrySet())
      {
        String keyStr = entry1.getKey();
        System.out.print(keyStr + " >>  " );
        ArrayList<String> list = data.get(keyStr);
        for(int i = 0 ; i < list.size() ; i++)
        {
          System.out.print(list.get(i) + " ");
        }
        System.out.println();
      }
    } */
  }
}

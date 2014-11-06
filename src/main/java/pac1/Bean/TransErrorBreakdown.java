package pac1.Bean;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.io.File;

public class TransErrorBreakdown implements java.io.Serializable
{
  private static String className = "TransErrorBreakdown";
  public HashMap<String, ArrayList> hashMapFailedTransactions;
  private Vector vecErrorName;
  public String trans_errorsPath = "";
  public StringBuffer strBuffTransFileData = new StringBuffer();
  
  public TransErrorBreakdown()
  {
    
  }
  
  private String getTransErrorDataFileNameWithPath(String testRunNumber , String genName , String baseTRNumber)
  {
    //storing path in variable to use in TransSummaryWindow.java
    //String strCmd = "nsi_get_tx_status_count";
    //String args = "" + testRun + " > " + pieChartTranErrorBreakDown.transErrorBreakdown.trans_errorsPath;
    if(baseTRNumber.equals(""))
      trans_errorsPath = Config.getWorkPath() + "/webapps/logs/TR" + testRunNumber + "/ready_reports/trans_errors.dat";
    else
      trans_errorsPath = Config.getWorkPath() + "/webapps/logs/TR" + baseTRNumber + "/NetCloud/"+ genName +"/TR" + testRunNumber +  "/ready_reports/trans_errors.dat";
    return trans_errorsPath;
  }
  
  /**
   * This function will return Error Name for error code sent as argument
   * @param errorCode
   * @return
   */
  private String getErrorNameByErrorCode(String errorCode)
  {
    Log.debugLog(className, "getErrorNameByErrorCode", "", "", "Method called, getting Error Name for Error Code = " + errorCode);
    String errorName = "NA";
    try
    {
      for(int i = 0; i < vecErrorName.size(); i++)
      {
        String[] tempString = rptUtilsBean.strToArrayData(vecErrorName.get(i).toString(), " - ");// As vector has each line in format :ErrorCode=ErrorName 
        if(errorCode.equals(tempString[0]))
        {
          errorName = tempString[1];
          Log.debugLog(className, "getErrorNameByErrorCode", "", "", " Error Name= " +errorName + " for Error Code = " + errorCode);
          return errorName;
        }        
      }   
      return errorName;
    }
    catch(Exception ex)
    {
      return errorName;
    }    
  }
  
  /***
   * This function will populate vector "vecErrorName" with error names for corresponding error codes 
   * using command : "nsu_get_errors 2 1"
   * each vector element has "Error code - Error Name"
   * @return
   */
  private boolean getAllErrorNamesForErrorCodes()
  {
    Log.debugLog(className, "getAllErrorNamesForErrorCodes", "", "", "Method called, getting list of all Error Code with corresponding error names");
    try
    {
      String cmd = "nsu_get_errors";
      String cmdArgs = "2 1";
      String pathForCmd = "";
      String usrName = null;//what userNameto pass - null for netstorm /root as username
      String runAsUser = null;
      
      CmdExec cmdExec = new CmdExec();
      
      vecErrorName = cmdExec.getResultByCommand(pathForCmd + cmd, cmdArgs, CmdExec.NETSTORM_CMD, usrName, runAsUser);
/*      for(int i = 0; i < vecErrorName.size(); i++)
      {
        System.out.println("vecErrorName [" + i + "] = "+ vecErrorName.get(i).toString());      
      }*/
      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getAllErrorNamesForErrorCodes", "", "", "Exception : ", ex);
      Log.errorLog(className, "getAllErrorNamesForErrorCodes", "", "", "Error in executing = nsu_get_errors 2 1");
      return false;
    }  
  }
  
  /**
   * This function will read trans_errors.dat file having data in format "Transaction Name | Error Type| Count"
   * Parse file: for each line take "Transaction Name" as key and ArrayList of custom object "TransactionErrorInfo" as value 
   * in hashMap"hashMapFailedTransactions".
   * TransactionErrorInfo object carry Error code, count, and error name
   * 
   */
 
  public boolean readTransErrorsDataFile(String testRunNumber)
  {
    return readTransErrorsDataFile(testRunNumber,"Controller" , "");
  }

  public boolean readTransErrorsDataFile(String testRunNumber, String genName, String baseTRNumber)
  {
    Log.debugLog(className, "readTransErrorsDataFile", "", "", "Method called, Going to read trans_error.dat file for test run" + testRunNumber);
    try
    {
      testRunNumber = testRunNumber.trim();
      baseTRNumber = baseTRNumber.trim();
      strBuffTransFileData.setLength(0);
      boolean gotErrorNames = getAllErrorNamesForErrorCodes();
      boolean isFileIncorrectFormat = false;
      
      String trans_errors_FilePath = getTransErrorDataFileNameWithPath(testRunNumber , genName ,baseTRNumber);
      String readString = null;
      
      File checkFile = new File(trans_errors_FilePath);
      if(!checkFile.exists())
        return false;
      BufferedReader brTransErrorsData  = new BufferedReader(new FileReader(trans_errors_FilePath));
      
      hashMapFailedTransactions = new HashMap<String, ArrayList>();
      
      readString = brTransErrorsData.readLine();//this just reads first line i.e header and ignores it
      strBuffTransFileData.append(readString + "\n");
      while(((readString = brTransErrorsData.readLine()) != null))
      {
        strBuffTransFileData.append(readString + "\n");
        String[] arrayTransactionInfo = rptUtilsBean.strToArrayData(readString, "|");
        //If length is less than or greater than 3 it will return false. Eg.
        //trans5|80|37931 
        //Cancel request sent
        //ERROR:  canceling query due to user request
        
        if(arrayTransactionInfo.length != 3)
        {
          hashMapFailedTransactions.clear();
          isFileIncorrectFormat = true;
          Log.errorLog(className, "readTransErrorsDataFile", "", "", "Format of file is not correct. File path = " + trans_errors_FilePath + ", Data line = " + readString);
          continue;
          //return false;
        }
        else if(isFileIncorrectFormat)
          continue;
        
        String txName = arrayTransactionInfo[0];//transaction name of read transaction
        String errorName = "NA";
        if(gotErrorNames)//if successfully got error names list
        {
          errorName = getErrorNameByErrorCode(arrayTransactionInfo[1]);//assigns error name for error code sent for current transaction
        }
        //System.out.println("TransErrorerrorName = " +errorName);
          
        if(hashMapFailedTransactions.containsKey(txName))//if transaction key is already their in hashmap than no need to create new arraylist, just add new custom object for new error code for same transaction
        {
          TransactionErrorInfo transactions = new TransactionErrorInfo(arrayTransactionInfo[1], arrayTransactionInfo[2], errorName);
          
          ArrayList<TransactionErrorInfo> arrListTransactionDetail = hashMapFailedTransactions.get(txName);
          arrListTransactionDetail.add(transactions);
          
        }
        else // if transaction encountered first than add in hashMap
        {
          TransactionErrorInfo transactions = new TransactionErrorInfo(arrayTransactionInfo[1], arrayTransactionInfo[2], errorName);
          
          ArrayList<TransactionErrorInfo> arrListTransactionDetail = new ArrayList<TransactionErrorInfo>();
          arrListTransactionDetail.add(transactions);
          
          hashMapFailedTransactions.put(txName, arrListTransactionDetail); 
        }

      }
      return true;  
    }
    catch(Exception ex)
    {
      //filenot found or corrupted return false (and check at client side to disable hyperlink)
      Log.stackTraceLog(className, "readTransErrorsDataFile", "", "", "Exception : ", ex);
      Log.errorLog(className, "readTransErrorsDataFile", "", "", "Error in reading trans_errors.dat file : file not found or may be corrupt");
      return false;
    }

  }
  

  //Reading Error Transaction Data from Shell
  
  public boolean getTransErrorsDataThroughShell(String testRunNumber, boolean isPartitionMode, String startTime, String endTime)
  {
    Log.debugLog(className, "getTransErrorsDataThroughShell", "", "", "Method called, Going to read trans_error.dat file for test run" + testRunNumber);
    try
    {
      testRunNumber = testRunNumber.trim();
      boolean gotErrorNames = getAllErrorNamesForErrorCodes();
      String[][] arrDataValues = null;
      
      hashMapFailedTransactions = new HashMap<String, ArrayList>();
      String strCmdName = "nsi_get_tx_status_count";
      String strCmdArgs = "";
      if(isPartitionMode)
        strCmdArgs = "--testrun " + testRunNumber + " --abs_starttime " + startTime + " --abs_endtime " + endTime;
      else
	strCmdArgs = "--testrun " + testRunNumber + " --starttime " + startTime + " --endtime " + endTime;
      String pathForCmd = "";
      String usrName = null;//what userNameto pass - null for netstorm /root as username
      String runAsUser = null;
      CmdExec cmdExec = new CmdExec();
      Vector vecOutErrorName = cmdExec.getResultByCommand(pathForCmd + strCmdName, strCmdArgs, CmdExec.NETSTORM_CMD, usrName, runAsUser);
      arrDataValues = rptUtilsBean.getRecFlds(vecOutErrorName, "", "", "|");
      if(arrDataValues != null)
      {
	for (int i = 0; i < arrDataValues.length; i++)
	{
	//
        //If length is less than or greater than 3 it will return false. Eg.
        //trans5|80|37931 
        //Cancel request sent
        //ERROR:  canceling query due to user request
        
        if(arrDataValues[i].length != 3)
        {
          hashMapFailedTransactions.clear();
          Log.errorLog(className, "getTransErrorsDataThroughShell", "", "", "Command Name " + strCmdName + ", Arguments = " + strCmdArgs);
          continue;
          //return false;
        }
        
        String txName = arrDataValues[i][0];//transaction name of read transaction
        String errorName = "NA";
        if(gotErrorNames)//if successfully got error names list
        {
          errorName = getErrorNameByErrorCode(arrDataValues[i][1]);//assigns error name for error code sent for current transaction
        }
        //System.out.println("TransErrorerrorName = " +errorName);
          
        if(hashMapFailedTransactions.containsKey(txName))//if transaction key is already their in hashmap than no need to create new arraylist, just add new custom object for new error code for same transaction
        {
          TransactionErrorInfo transactions = new TransactionErrorInfo(arrDataValues[i][1], arrDataValues[i][2], errorName);
          
          ArrayList<TransactionErrorInfo> arrListTransactionDetail = hashMapFailedTransactions.get(txName);
          arrListTransactionDetail.add(transactions);
          
        }
        else // if transaction encountered first than add in hashMap
        {
          TransactionErrorInfo transactions = new TransactionErrorInfo(arrDataValues[i][1], arrDataValues[i][2], errorName);
          
          ArrayList<TransactionErrorInfo> arrListTransactionDetail = new ArrayList<TransactionErrorInfo>();
          arrListTransactionDetail.add(transactions);
          hashMapFailedTransactions.put(txName, arrListTransactionDetail); 
          
        }

        }
      return true;  
      }
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getTransErrorsDataThroughShell", "", "", "Exception : ", ex);
      return false;
    }
    Log.debugLogAlways(className, "getTransErrorsDataThroughShell" , "", "", " Error in getting Data ... Hence returning");
    return false;
  }

  
  
  public class TransactionErrorInfo implements java.io.Serializable
  {
    private String errorCode = "NA";
    private String errorCount = "NA";
    private String errorName = "NA";
    
    
    TransactionErrorInfo(String errorCode, String errorCount, String errorName)
    {
      this.errorCode = errorCode;
      this.errorCount = errorCount;
      this.errorName = errorName;
    }
        
    public String getErrorCode()
    {
      return errorCode;
    }
    public String getErrorCount()
    {
      return errorCount;
    }
    public String getErrorName()
    {
      return errorName;
    }
  }
  
/*  public static void main(String args[])
  {
    TransErrorBreakdown transErrorBreakdown = new TransErrorBreakdown();
    transErrorBreakdown.readTransErrorsDataFile("4247");
    ArrayList<TransactionErrorInfo> arrListtransErrorInfo = transErrorBreakdown.hashMapFailedTransactions.get("AddBagWsLtCkot");

    for(int i = 0; i < arrListtransErrorInfo.size(); i++)
    {
     TransactionErrorInfo transErrorInfo = arrListtransErrorInfo.get(i);
     System.out.println("ErrorCode = " + transErrorInfo.getErrorCode() + ", ErrorCount = " +transErrorInfo.getErrorCount() + ", ErrorName = " +transErrorInfo.getErrorName());
     }
  }*/
}

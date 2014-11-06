/**
 * DrillDownTxComponentQuery.java Purpose: For Implementation of Transaction Component Detail Report in DDR.
 **/

package pac1.Bean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;


public class DrillDownTxComponentQuery extends DrillDownExecuteQuery
{

  private static String className = "DrillDownTxComponentQuery";

  //Executing same query two times to get count and data,
  //so we are introducing thread concept to execute parallel two query to get count and get data
  public static String IS_EXECUTE_QUERY_IN_THREAD_ENABLE = "1"; //Default thread is on 
  
  static String NSI_DB_GET_TX_SUMMARY_DATA = "nsi_db_svc_get_data";
  static String NSI_DB_GET_TX_TIMING_DATA = "nsi_db_svc_get_comp_data";
  static String NSI_DB_GET_TXC_TIMING_DETAIL_DATA = "nsi_db_svc_get_comp_timing_details";
  static String NSI_DB_GET_TXC_GET_TX_INSTANCE_DATA = "nsi_db_svc_get_instance_data";
  static String NSI_DB_GET_TXC_SIGNATURE = "nsi_db_svc_get_data";
  static String NSI_DB_GET_TXC_CRITICAL_PATH = "nsi_db_svc_get_critical_path";
  static String NSI_DB_SVC_GET_COMP_DATA_EX = "nsi_db_svc_compare_main_screen_data";
  static String NSI_DB_SVC_GET_COMPARE_COMP_SCREEN_DATA = "nsi_db_svc_compare_comp_screen_data"; 

  public int TXC_NAME_INDEX = -1; // transaction component Name.
  public int TXC_INDEX = -1; // transaction component index
  public int TXC_INSTANCE = -1;
  
  public int AVERAGE_TIME_INDEX = -1; //average time
  public int TOTAL_TIME_INDEX = -1; // total time index.
  public int TX_COMP_STATUS = -1; // transaction component status.
  public int TX_COMP_FAIL_COUNT = -1; // transaction component fail count.
  public int TX_COMP_AVG_RESPONSE = -1;// transaction component average response.
  public int TX_COMP_SIGNATURE_ID = -1; // transaction component signature id.
  public int TX_COMP_SUCCESS_COUNT = -1; // transaction component success count
  public int TX_COMP_SIGNATURE_NAME = -1; // transaction component signature name.
  public int TX_COMP_SIGNATURE_COUNT = -1; // transaction component signature count.

  // Variables for Gnatt Chart.
  public String[][] arrDataForBar = null;
  double plottedArea = 900;
  double totalTime = 0.0;

  getCommandOutput[] getCommandOutputObj = null;
  boolean isAliveAll = false;

  
  public DrillDownTxComponentQuery(String testRun, String userName, DrillDownReportQuery drillDownReportData)
  {
    super(testRun, userName, drillDownReportData);
    setColumTypeForSorting();
    setConfigurableKeywords();
  }
  
  // method for getting arguments.
  public String addRemoveArguments(String strFilters, ArrayList addArgs, ArrayList removeArgs)
  {
    try
    {
      // split the function without removing the split value.
      String st[] = strFilters.split("(?=---*)");
      ArrayList argsList = new ArrayList();
      String outputArgs = "";

      // Initially add all arguments in arraylist of arguments.
      for (int i = 1; i < st.length; i++)
      {
        argsList.add(st[i].trim());
      }

      // Add Arguments from addArgs List.
      for (int k = 0; k < addArgs.size(); k++)
      {
        String[] innerAddToken = rptUtilsBean.split(addArgs.get(k).toString().trim(), " ");
        boolean isAdded = false;

        for (int i = 0; i < argsList.size(); i++)
        {

          String innerTokens[] = rptUtilsBean.split(argsList.get(i).toString().trim(), " ");
          if (innerAddToken[0].trim().equals(innerTokens[0].trim()))
          {
            argsList.set(i, addArgs.get(k).toString().trim());
            isAdded = true;
          }
        }
        if (!isAdded)
          argsList.add(addArgs.get(k).toString().trim());
      }

      // Removing Tokens form removeToken ArrayList.
      for (int k = 0; k < removeArgs.size(); k++)
      {
        String removeTokens[] = rptUtilsBean.split(removeArgs.get(k).toString().trim(), " ");

        for (int i = 0; i < argsList.size(); i++)
        {
          String[] innerToken = rptUtilsBean.split(argsList.get(i).toString().trim(), " ");
          if (removeTokens[0].trim().equals(innerToken[0].trim()))
          {
            argsList.remove(i);
          }
        }
      }

      // Make a argument String.
      for (int k = 0; k < argsList.size(); k++)
      {
        outputArgs = outputArgs + " " + argsList.get(k).toString();
      }
      return outputArgs;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return "";
    }
  }

//saving type of the column to sort used vy table sorting by js
  public void setColumTypeForSorting()
  {
    long startTimeStamp =  System.currentTimeMillis();
    // Map is used to get column types for sorting
    mapSort.put("TxCompName","String");
    mapSort.put("RespTime", "String");
    mapSort.put("Status", "String");
    mapSort.put("StartTime", "String");
    mapSort.put("TxcInstance", "String");
    mapSort.put("Count", "Number");
    mapSort.put("Min", "Number");
    mapSort.put("Max", "Number");
    mapSort.put("Variance", "Number");

    long endTimeStamp =  System.currentTimeMillis();

    Log.debugLog(className, "setColumTypeForSorting", "", "", "Total Time taken = " + (endTimeStamp - startTimeStamp));
  }


/*
 * This method set index and return false if user want to skip column
 */
  private boolean setColumnTypeIndex(String columnName, int index)
  {
    if(columnName.equals("SvcName"))
    {
      TXC_NAME_INDEX = index;
      return true;
    }

    else if(columnName.equals("SvcIndex"))
    {
      TXC_INDEX = index;
      return false;
    }
    else if(columnName.equals("svcInstance"))
    {
      TXC_INSTANCE = index;
      return false;
    }
    else if (columnName.equals("SignatureCount"))
    {
      TX_COMP_SIGNATURE_COUNT = index;
      return false;
    }
    else if (columnName.equals("SuccessCount"))
    {
      TX_COMP_SUCCESS_COUNT = index;
      return false;
    }
    else if (columnName.equals("FailCount"))
    {
      TX_COMP_FAIL_COUNT = index;
      return false;
    }
    else if (columnName.equals("SvcSignatureName"))
    {
      TX_COMP_SIGNATURE_NAME = index;
      return false;
    }
    else if (columnName.equals("AvgRespTime"))
    {
      TX_COMP_AVG_RESPONSE = index;
      return false;
    }
    else if (columnName.equals("Status"))
    {
      TX_COMP_STATUS = index;
      return false;
    }
    else if (columnName.equals("SvcSignatureId"))
    {
      TX_COMP_SIGNATURE_ID = index;
      return false;
    }
    else
      return true;

  }
  
  /**
   * <p>
   * This method execute_get_tx_summary_data object query of Transaction Component Report.
   * </p>
   * <br>
   * 
   * @param cmdArg
   *          : arguments for query
   * @param sortColumnType
   *          : column type sorting (String, Number, Date)
   * @param defaultSorting
   *          : Sorting indicator
   * @param totalCount
   *          : give no. of records
   * 
   * @return 2D String Array Contains output in following format.
   *         <p>
   *         TxComponent Name|Count|Signature Count|Success|Fail|Avg Resp. Time (ms)|Min (ms)|Max (ms)|VMR
   *         </p>
   */
  public String[][] execute_get_tx_summary_data(String cmdArg, StringBuffer sortColumnType, StringBuffer defaultSorting, StringBuffer totalRows, String currlimit, String currOffset, DrillDownBreadCrumb downBreadCrumb, String strCurrentURlBreadCrumb)
  {
    Log.debugLog(className, "execute_get_tx_summary_data", "", "", "Method called. = " + cmdArg);
    String arrResult[][] = null;
    try
    {
      long startTimeStamp = System.currentTimeMillis();

      if (downBreadCrumb.getFilterArument().equals("") || (!downBreadCrumb.getOffset().equals(currOffset) || !downBreadCrumb.getLimit().equals(currlimit) || !strCurrentURlBreadCrumb.trim().equals(downBreadCrumb.getBreadCrumbLabel().trim()) || !downBreadCrumb.getFilterArument().trim().equals(cmdArg.trim())))
      {
        String limitOffset = " --limit " + currlimit + " --offset " + currOffset;
        strCmdName = NSI_DB_GET_TX_SUMMARY_DATA;

        Log.debugLog(className, "execute_get_tx_summary_data", "", "", "Command Name = " + strCmdName);

        vecCmdOutput = new Vector();
        getCommandOutputObj = new getCommandOutput[2];
        Thread[] threadArr = new Thread[2];

        for (int i = 0; i < getCommandOutputObj.length; i++)
        {
          // this boolean will give all thread started or not default false
          if (i == getCommandOutputObj.length - 1)
            isAliveAll = true;

          // command with limit offset to get data
          if (i == 0)
            strCmdArgs = " --testrun " + testRun + " " + cmdArg + " " + GET_COUNT_OPTION + " " + DEFAULT_VALUE_GET_COUNT;
          else
            strCmdArgs = " --testrun " + testRun + " " + cmdArg + limitOffset;

          Log.debugLog(className, "execute_get_tx_summary_data", "", "", "Command Argument = " + strCmdArgs);

          // creating class object array
          getCommandOutputObj[i] = new getCommandOutput(i, strCmdName, strCmdArgs, this);

          // if thread is enable
          if (IS_EXECUTE_QUERY_IN_THREAD_ENABLE.equals("1"))
          {
            threadArr[i] = new Thread(getCommandOutputObj[i], "Query - " + i);
            // Starting thread
            threadArr[i].start();
          }
          else
            // if threading is not enable
            getCommandOutputObj[i].getResultByCmdToGetOutput();
        }

        // notify all thread
        // checking thread is running or not
        // All thread are not alive it stops all thread
        waitForQueryRunThreads(threadArr);

        long endTimeStamp = System.currentTimeMillis();

        Log.debugLog(className, "execute_get_tx_summary_data", "", "", "Total Time taken = " + (endTimeStamp - startTimeStamp));

        vecCmdOutput = getCommandOutputObj[0].getQueryOutput();
        if (!getCommandOutputObj[0].getQueryStatus())
        {
          totalRows.append(0);
          defaultSorting.append("0");
          sortColumnType.append("String");
          arrResult = new String[2][1];
          arrResult[0][0] = FAIL_QUERY;
          arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
          downBreadCrumb.setBreadCrumbData(arrResult);
          return arrResult;
        }
        else
        {
          totalRows.append(vecCmdOutput.get(1).toString().trim());
        }

        vecCmdOutput = new Vector();

        vecCmdOutput = getCommandOutputObj[1].getQueryOutput();

        if (!getCommandOutputObj[1].getQueryStatus())
        {
          defaultSorting.append("0");
          sortColumnType.append("String");
          arrResult = new String[2][1];
          arrResult[0][0] = FAIL_QUERY;
          arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
          downBreadCrumb.setBreadCrumbData(arrResult);
          return arrResult;
        }

        arrResult = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "-");
        downBreadCrumb.setBreadCrumbData(arrResult);
      }
      else
      {
        arrResult = downBreadCrumb.getBreadCrumbData();
        totalRows.append(downBreadCrumb.getTotalRecord());
        // sortColumnType.append(downBreadCrumb.getSortColumnType());
        // defaultSorting.append(downBreadCrumb.getDefaultSortIndicator());
        downBreadCrumb.setBreadCrumbData(arrResult);
      }


      String arrSortColumnType[] = new String[arrResult[0].length];

      //Default fill sorting Type Number
      Arrays.fill(arrSortColumnType, "DecimalNum");

      String sortColName = "SvcName";

      for(int i = 0; i < arrResult[0].length; i++)
      {
        //setting column type index and if flag false skip column to sorting
        boolean bolFlag = setColumnTypeIndex(arrResult[0][i].toString().trim(), i);

        if(!bolFlag)
          arrSortColumnType[i] = ",";
        else if(mapSort.containsKey(arrResult[0][i].toString().trim()) && bolFlag)
        {
          arrSortColumnType[i] = mapSort.get(arrResult[0][i].toString().trim()).toString();
        }

        if(arrResult[0][i].toString().trim().equals(sortColName))
          defaultSorting.append(i);
      }


      if(defaultSorting.toString().trim().equals(""))
        defaultSorting.append("0");

      String sortColumn = rptUtilsBean.strArrayToStr(arrSortColumnType, ",");
      arrSortColumnType = rptUtilsBean.strToArrayData(sortColumn, ",");
      sortColumn = rptUtilsBean.strArrayToStr(arrSortColumnType, ",");

      Log.debugLog(className, "execute_get_tx_summary_data", "", "", "Column type for sorting = " + sortColumnType);

      sortColumnType.append(sortColumn);

      return arrResult;
    }
    catch (Exception e)
    {
      totalRows.append(0);
      defaultSorting.append("0");
      e.printStackTrace();
      arrResult = new String[2][1];
      arrResult[0][0] = FAIL_QUERY;
      arrResult[1][0] = "No records are found";
      Log.stackTraceLog(className, "execute_get_tx_summary_data", "", "", "Exception - ", e);
      return arrResult;
    }
  }

  /**
   * <p>
   * This method execute_get_tx_summary_data object query of Transaction Component Report.
   * </p>
   * <br>
   * 
   * @param cmdArg
   *          : arguments for query
   * @param sortColumnType
   *          : column type sorting (String, Number, Date)
   * @param defaultSorting
   *          : Sorting indicator
   * @param totalCount
   *          : give no. of records
   * 
   * @return 2D String Array Contains output in following format.
   *         <p>
   *         TxComponent Name|Count|Signature Count|Success|Fail|Avg Resp. Time (ms)|Min (ms)|Max (ms)|VMR
   *         </p>
   */
  public String[][] execute_get_tx_summary_compare_data(String cmdArg, StringBuffer sortColumnType, StringBuffer defaultSorting, StringBuffer totalRows, String currlimit, String currOffset, DrillDownBreadCrumb downBreadCrumb, String strCurrentURlBreadCrumb)
  {
    Log.debugLog(className, "execute_get_tx_summary_data", "", "", "Method called. = " + cmdArg);
    String arrResult[][] = null;
    try
    {
      long startTimeStamp = System.currentTimeMillis();

      if (downBreadCrumb.getFilterArument().equals("") || (!downBreadCrumb.getOffset().equals(currOffset) || !downBreadCrumb.getLimit().equals(currlimit) || !strCurrentURlBreadCrumb.trim().equals(downBreadCrumb.getBreadCrumbLabel().trim()) || !downBreadCrumb.getFilterArument().trim().equals(cmdArg.trim())))
      {
        //String limitOffset = " --limit " + currlimit + " --offset " + currOffset;
        String limitOffset = "";
        strCmdName = NSI_DB_SVC_GET_COMP_DATA_EX;

        Log.debugLog(className, "execute_get_tx_summary_data", "", "", "Command Name = " + strCmdName);

        vecCmdOutput = new Vector();
        getCommandOutputObj = new getCommandOutput[1];
        Thread[] threadArr = new Thread[1];

        for (int i = 0; i < getCommandOutputObj.length; i++)
        {
          // this boolean will give all thread started or not default false
          if (i == getCommandOutputObj.length - 1)
            isAliveAll = true;

          // command with limit offset to get data
         /** if (i == 0)
            strCmdArgs = " --testrun " + testRun + " " + cmdArg + " " + GET_COUNT_OPTION + " " + DEFAULT_VALUE_GET_COUNT;
          else**/
            strCmdArgs = " --testrun " + testRun + " " + cmdArg + limitOffset;

          Log.debugLog(className, "execute_get_tx_summary_data", "", "", "Command Argument = " + strCmdArgs);

          // creating class object array
          getCommandOutputObj[i] = new getCommandOutput(i, strCmdName, strCmdArgs, this);

          // if thread is enable
          if (IS_EXECUTE_QUERY_IN_THREAD_ENABLE.equals("1"))
          {
            threadArr[i] = new Thread(getCommandOutputObj[i], "Query - " + i);
            // Starting thread
            threadArr[i].start();
          }
          else
            // if threading is not enable
            getCommandOutputObj[i].getResultByCmdToGetOutput();
        }

        // notify all thread
        // checking thread is running or not
        // All thread are not alive it stops all thread
        waitForQueryRunThreads(threadArr);

        long endTimeStamp = System.currentTimeMillis();

        Log.debugLog(className, "execute_get_tx_summary_data", "", "", "Total Time taken = " + (endTimeStamp - startTimeStamp));

       /** vecCmdOutput = getCommandOutputObj[0].getQueryOutput();
        if (!getCommandOutputObj[0].getQueryStatus())
        {
          totalRows.append(0);
          defaultSorting.append("0");
          sortColumnType.append("String");
          arrResult = new String[2][1];
          arrResult[0][0] = FAIL_QUERY;
          arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
          downBreadCrumb.setBreadCrumbData(arrResult);
          return arrResult;
        }
        else
        {
          totalRows.append(vecCmdOutput.get(1).toString().trim());
        }**/
        totalRows.append(0);

        vecCmdOutput = new Vector();

        vecCmdOutput = getCommandOutputObj[0].getQueryOutput();

        if (!getCommandOutputObj[0].getQueryStatus())
        {
          defaultSorting.append("0");
          sortColumnType.append("String");
          arrResult = new String[2][1];
          arrResult[0][0] = FAIL_QUERY;
          arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
          downBreadCrumb.setBreadCrumbData(arrResult);
          return arrResult;
        }

        arrResult = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "-");
        downBreadCrumb.setBreadCrumbData(arrResult);
      }
      else
      {
        arrResult = downBreadCrumb.getBreadCrumbData();
        totalRows.append(downBreadCrumb.getTotalRecord());
        // sortColumnType.append(downBreadCrumb.getSortColumnType());
        // defaultSorting.append(downBreadCrumb.getDefaultSortIndicator());
        downBreadCrumb.setBreadCrumbData(arrResult);
      }


      String arrSortColumnType[] = new String[arrResult[0].length];

      //Default fill sorting Type Number
      Arrays.fill(arrSortColumnType, "DecimalNum");

      String sortColName = "SvcName";

      for(int i = 0; i < arrResult[0].length; i++)
      {
        //setting column type index and if flag false skip column to sorting
        boolean bolFlag = setColumnTypeIndex(arrResult[0][i].toString().trim(), i);

        if(!bolFlag)
          arrSortColumnType[i] = ",";
        else if(mapSort.containsKey(arrResult[0][i].toString().trim()) && bolFlag)
        {
          arrSortColumnType[i] = mapSort.get(arrResult[0][i].toString().trim()).toString();
        }

        if(arrResult[0][i].toString().trim().equals(sortColName))
          defaultSorting.append(i);
      }


      if(defaultSorting.toString().trim().equals(""))
        defaultSorting.append("0");

      String sortColumn = rptUtilsBean.strArrayToStr(arrSortColumnType, ",");
      arrSortColumnType = rptUtilsBean.strToArrayData(sortColumn, ",");
      sortColumn = rptUtilsBean.strArrayToStr(arrSortColumnType, ",");

      Log.debugLog(className, "execute_get_tx_summary_data", "", "", "Column type for sorting = " + sortColumnType);

      sortColumnType.append(sortColumn);

      return arrResult;
    }
    catch (Exception e)
    {
      totalRows.append(0);
      defaultSorting.append("0");
      e.printStackTrace();
      arrResult = new String[2][1];
      arrResult[0][0] = FAIL_QUERY;
      arrResult[1][0] = "No records are found";
      Log.stackTraceLog(className, "execute_get_tx_summary_data", "", "", "Exception - ", e);
      return arrResult;
    }
  }
  
  /*
   * This return 10 length array to shoe scaling for bar
   * change Scale values upto 15.(As plotted Area change to 900).
   */
  public String[] getScaleForBarForCompare(double[] arrAvgCmpData)
  {
    try
    {
      Arrays.sort(arrAvgCmpData);
      String arrScale[] = new String[15];
      for (int i = 1; i <= 15; i++)
      {
        arrScale[i - 1] = rptUtilsBean.convertTodecimal(((i * arrAvgCmpData[arrAvgCmpData.length - 1]) / 15), 3);
      }
      return arrScale;
    }
    catch (Exception e)
    {
      return new String[]
      { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14" };
    }
  }
  // 0        1        2      3               4                5            6        7              8               9                10             11              12              13
  //TestRun SvcName Count SignatureCount  CPSignatureCount  SuccessCount  FailCount AvgTotalTime  AvgQueueWaitTime  AvgAppSelfTime  AvgSORRespTime  MinSORRespTime  MaxSORRespTime  VarianceSORRespTime

  public String[][] calculateCompareDataForBar(String[][] arrData, double totalTime)
  {
    try
    {
      String arrBarData[][] = new String[arrData.length - 1][7];
      for(int i = 1; i < arrData.length; i++)
      {
        if(arrData[i][1].trim().equals("-"))
          arrBarData[i - 1][0] = "Test Run " + arrData[i][0] + " - ";
        else
          arrBarData[i - 1][0] = "Test Run " + arrData[i][0] + " - " + arrData[i][1];
        
        if(arrData[i][9].trim().equals("-"))
          arrData[i][9] = "0";
        
        double waitTime = (Double.parseDouble(arrData[i][9])*plottedArea / totalTime);
        
        if(arrData[i][10].trim().equals("-"))
          arrData[i][10] = "0";

        double selfTime = (Double.parseDouble(arrData[i][10])*plottedArea / totalTime);
        
        if(arrData[i][11].trim().equals("-"))
          arrData[i][11] = "0";
        
        double respTime = (Double.parseDouble(arrData[i][11])*plottedArea / totalTime);
        
        if((waitTime + respTime + selfTime) > plottedArea)
        {
          waitTime = waitTime - 0.5;
          selfTime = selfTime - 0.5;
          respTime = respTime - 0.5;
        }
        arrBarData[i - 1][1] = "" + waitTime;
        arrBarData[i - 1][2] = "" + respTime;
        arrBarData[i - 1][3] = "" + selfTime;
        arrBarData[i - 1][4] = rptUtilsBean.convertTodecimal(Double.parseDouble(arrData[i][9]), 3);
        arrBarData[i - 1][5] = rptUtilsBean.convertTodecimal(Double.parseDouble(arrData[i][10]), 3);
        arrBarData[i - 1][6] = rptUtilsBean.convertTodecimal(Double.parseDouble(arrData[i][11]), 3);
      }
      return arrBarData;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return new String[0][0];
    }
  }
  
  //TestRun|SvcIndex|SvcCompName|Count|RespTime|Min|Max|Variance|CPFlagc
  // 0       1        2           3      4       5   6    7       8
  public String[][] calculateCompareDataForBarComp(String[][] arrData, double totalTime)
  {
    try
    {
      String arrBarData[][] = new String[arrData.length - 1][3];
      for(int i = 1; i < arrData.length; i++)
      {
        if(arrData[i][2].trim().equals("-"))
          arrBarData[i - 1][0] = "Test Run " + arrData[i][0] + " - ";
        else
          arrBarData[i - 1][0] = "Test Run " + arrData[i][0] + " - " + arrData[i][2];
        
        if(arrData[i][4].trim().equals("-"))
          arrData[i][4] = "0";
        double respTime = (Double.parseDouble(arrData[i][4])*plottedArea / totalTime);
        if((respTime) > plottedArea)
        {
          respTime = respTime - 0.5;
        }
        arrBarData[i - 1][1] = "" + respTime;
        arrBarData[i - 1][2] = rptUtilsBean.convertTodecimal(Double.parseDouble(arrData[i][4]), 3);
      }
      return arrBarData;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return new String[0][0];
    }
  }  
  // execute_nsi_db_get_tx_timing_data
  public String[][] execute_nsi_db_get_tx_timing_data(String cmdArg, StringBuffer sortColumnType, StringBuffer defaultSorting, DrillDownBreadCrumb breadCrumb, String strCurrentURlBreadCrumb)
  {
    Log.debugLog(className, "execute_nsi_db_get_tx_timing_data", "", "", "Method called. = " + cmdArg);
    String arrResult[][] = null;
    try
    {
      if (breadCrumb.getFilterArument().equals("") || (!breadCrumb.getFilterArument().equals(cmdArg)) || !strCurrentURlBreadCrumb.trim().equals(breadCrumb.getBreadCrumbLabel().trim()))
      {
        strCmdName = NSI_DB_GET_TX_TIMING_DATA;
        Log.debugLog(className, "execute_nsi_db_get_tx_timing_data", "", "", "Command Name = " + strCmdName);

        strCmdArgs = " --testrun " + testRun + " " + cmdArg;

        Log.debugLog(className, "execute_nsi_db_get_tx_timing_data", "", "", "Command Argument = " + strCmdArgs);

        vecCmdOutput = new Vector();
        boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);
        Log.debugLog(className, "execute_nsi_db_get_tx_timing_data", "", "", "Checking Commnad executed successfully or not. = " + bolRsltFlag);

        if (!bolRsltFlag)
        {
          defaultSorting.append("0");
          arrResult = new String[2][1];
          arrResult[0][0] = FAIL_QUERY;
          arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
          breadCrumb.setBreadCrumbData(arrResult);
          return arrResult;
        }

        arrResult = getVectorDataInArray(vecCmdOutput);
        defaultSorting.append("0");
        breadCrumb.setBreadCrumbData(arrResult);
      }
      else
      {
        arrResult = breadCrumb.getBreadCrumbData();
        breadCrumb.setBreadCrumbData(arrResult);

      }
      String arrSortColumnType[] = new String[arrResult[0].length];

      // Default fill sorting Type Number
      Arrays.fill(arrSortColumnType, "DecimalNum");

      String sortColName = "AvgRespTime";

      for (int i = 0; i < arrResult[0].length; i++)
      {
        // setting column type index and if flag false skip column to sorting
        boolean bolFlag = setColumnTypeIndex(arrResult[0][i].toString().trim(), i);

        if (!bolFlag)
          arrSortColumnType[i] = ",";
        else if (mapSort.containsKey(arrResult[0][i].toString().trim()) && bolFlag)
        {
          arrSortColumnType[i] = mapSort.get(arrResult[0][i].toString().trim()).toString();
        }

        if (arrResult[0][i].toString().trim().equals(sortColName))
          defaultSorting.append(i);
      }

      if (defaultSorting.toString().trim().equals(""))
        defaultSorting.append("0");

      String sortColumn = rptUtilsBean.strArrayToStr(arrSortColumnType, ",");
      arrSortColumnType = rptUtilsBean.strToArrayData(sortColumn, ",");
      sortColumn = rptUtilsBean.strArrayToStr(arrSortColumnType, ",");

      Log.debugLog(className, "execute_nsi_db_get_tx_timing_data", "", "", "Column type for sorting = " + sortColumnType);

      sortColumnType.append(sortColumn);

      return arrResult;
    }
    catch (Exception e)
    {
      defaultSorting.append("0");
      e.printStackTrace();
      arrResult = new String[2][1];
      arrResult[0][0] = FAIL_QUERY;
      arrResult[1][0] = "No records are found";
      Log.stackTraceLog(className, "execute_nsi_db_get_tx_timing_data", "", "", "Exception - ", e);
      return arrResult;
    }
  }

  // execute_nsi_db_get_tx_timing_data
  public String[][] execute_nsi_db_get_tx_timing_data_compare(String cmdArg, StringBuffer sortColumnType, StringBuffer defaultSorting, DrillDownBreadCrumb breadCrumb, String strCurrentURlBreadCrumb)
  {
    Log.debugLog(className, "execute_nsi_db_get_tx_timing_data_compare", "", "", "Method called. = " + cmdArg);
    String arrResult[][] = null;
    try
    {
      if (breadCrumb.getFilterArument().equals("") || (!breadCrumb.getFilterArument().equals(cmdArg)) || !strCurrentURlBreadCrumb.trim().equals(breadCrumb.getBreadCrumbLabel().trim()))
      {
        //nsi_db_svc_get_compair_comp_screen_data 
        strCmdName = NSI_DB_SVC_GET_COMPARE_COMP_SCREEN_DATA;
        Log.debugLog(className, "execute_nsi_db_get_tx_timing_data", "", "", "Command Name = " + strCmdName);

        strCmdArgs = " --testrun " + testRun + " " + cmdArg;

        Log.debugLog(className, "execute_nsi_db_get_tx_timing_data_compare", "", "", "Command Argument = " + strCmdArgs);

        vecCmdOutput = new Vector();
        boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);
        Log.debugLog(className, "execute_nsi_db_get_tx_timing_data_compare", "", "", "Checking Commnad executed successfully or not. = " + bolRsltFlag);

        if (!bolRsltFlag)
        {
          defaultSorting.append("0");
          arrResult = new String[2][1];
          arrResult[0][0] = FAIL_QUERY;
          arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
          breadCrumb.setBreadCrumbData(arrResult);
          return arrResult;
        }

        arrResult = getVectorDataInArray(vecCmdOutput);
        defaultSorting.append("0");
        breadCrumb.setBreadCrumbData(arrResult);
      }
      else
      {
        arrResult = breadCrumb.getBreadCrumbData();
        breadCrumb.setBreadCrumbData(arrResult);

      }
      String arrSortColumnType[] = new String[arrResult[0].length];

      // Default fill sorting Type Number
      Arrays.fill(arrSortColumnType, "DecimalNum");

      String sortColName = "AvgRespTime";

      for (int i = 0; i < arrResult[0].length; i++)
      {
        // setting column type index and if flag false skip column to sorting
        boolean bolFlag = setColumnTypeIndex(arrResult[0][i].toString().trim(), i);

        if (!bolFlag)
          arrSortColumnType[i] = ",";
        else if (mapSort.containsKey(arrResult[0][i].toString().trim()) && bolFlag)
        {
          arrSortColumnType[i] = mapSort.get(arrResult[0][i].toString().trim()).toString();
        }

        if (arrResult[0][i].toString().trim().equals(sortColName))
          defaultSorting.append(i);
      }

      if (defaultSorting.toString().trim().equals(""))
        defaultSorting.append("0");

      String sortColumn = rptUtilsBean.strArrayToStr(arrSortColumnType, ",");
      arrSortColumnType = rptUtilsBean.strToArrayData(sortColumn, ",");
      sortColumn = rptUtilsBean.strArrayToStr(arrSortColumnType, ",");

      Log.debugLog(className, "execute_nsi_db_get_tx_timing_data_compare", "", "", "Column type for sorting = " + sortColumnType);

      sortColumnType.append(sortColumn);

      return arrResult;
    }
    catch (Exception e)
    {
      defaultSorting.append("0");
      e.printStackTrace();
      arrResult = new String[2][1];
      arrResult[0][0] = FAIL_QUERY;
      arrResult[1][0] = "No records are found";
      Log.stackTraceLog(className, "execute_nsi_db_get_tx_timing_data_compare", "", "", "Exception - ", e);
      return arrResult;
    }
  }

  // execute_nsi_db_get_tx_timing_detail_data
  public String[][] execute_nsi_db_get_tx_timing_detail_data(String cmdArg, StringBuffer sortColumnType, StringBuffer defaultSorting, DrillDownBreadCrumb breadCrumb, String strCurrentURlBreadCrumb)
  {
    Log.debugLog(className, "execute_nsi_db_get_tx_timing_detail_data", "", "", "Method called. = " + cmdArg);
    String arrResult[][] = null;
    try
    {
      if (breadCrumb.getFilterArument().equals("") || (!breadCrumb.getFilterArument().equals(cmdArg) || !strCurrentURlBreadCrumb.trim().equals(breadCrumb.getBreadCrumbLabel().trim())))
      {
        strCmdName = NSI_DB_GET_TXC_TIMING_DETAIL_DATA;
        Log.debugLog(className, "execute_nsi_db_get_tx_timing_detail_data", "", "", "Command Name = " + strCmdName);

        strCmdArgs = " --testrun " + testRun + " " + cmdArg;

        Log.debugLog(className, "execute_nsi_db_get_tx_timing_detail_data", "", "", "Command Argument = " + strCmdArgs);

        vecCmdOutput = new Vector();
        boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);
        Log.debugLog(className, "execute_nsi_db_get_tx_timing_detail_data", "", "", "Checking Commnad executed successfully or not. = " + bolRsltFlag);

        if (!bolRsltFlag)
        {
          defaultSorting.append("0");
          arrResult = new String[2][1];
          arrResult[0][0] = FAIL_QUERY;
          arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
          breadCrumb.setBreadCrumbData(arrResult);
          return arrResult;
        }

        arrResult = getVectorDataInArray(vecCmdOutput);
        defaultSorting.append("0");
        breadCrumb.setBreadCrumbData(arrResult);
      }
      else
      {
        arrResult = breadCrumb.getBreadCrumbData();
        breadCrumb.setBreadCrumbData(arrResult);

      }
      String arrTableData[][] = new String[arrResult.length][arrResult[0].length +1];
      String trStartTimeStr = Scenario.getTestRunStartTime(Integer.parseInt(testRun));
      Date trStartDate = new Date(trStartTimeStr);
      long trStartTime =  trStartDate.getTime();
      for(int i = 0; i < arrResult.length; i++)
      {
        
        String arrHeader[] = {"Service Component Name", "Absolute Start Time", "Relative Start Time", "Queue Wait Time", "DB Response Time", "Status Index", "Status", "CPFlag"};  
       
        if(i == 0)
          arrTableData[0] = arrHeader.clone();
                
        if(i != 0)
        {
          long QueryStartTime = Long.parseLong(arrResult[i][1]);
          long diff = QueryStartTime - trStartTime;
          //System.out.println("diff = "+diff + " QueryStartTime = "+QueryStartTime + " trStartTime = "+trStartTime + " convert = "+rptUtilsBean.timeInMilliSecToString(diff+"", 0));
          arrTableData[i][0] = arrResult[i][0];
          arrTableData[i][1] = arrResult[i][1];
          arrTableData[i][2] = rptUtilsBean.timeInMilliSecToString(diff+"", 0);
          arrTableData[i][3] = arrResult[i][2];
          arrTableData[i][4] = arrResult[i][3];
          arrTableData[i][5] = arrResult[i][4];
          arrTableData[i][6] = arrResult[i][5];
          arrTableData[i][7] = arrResult[i][6];
        }
      }
      
      String arrSortColumnType[] = new String[arrTableData[0].length];

      // Default fill sorting Type Number
      Arrays.fill(arrSortColumnType, "DecimalNum");

      String sortColName = "Start Time";

      for (int i = 0; i < arrTableData[0].length; i++)
      {
        // setting column type index and if flag false skip column to sorting
        boolean bolFlag = setColumnTypeIndex(arrTableData[0][i].toString().trim(), i);

        if (!bolFlag)
          arrSortColumnType[i] = ",";
        else if (mapSort.containsKey(arrTableData[0][i].toString().trim()) && bolFlag)
        {
          arrSortColumnType[i] = mapSort.get(arrTableData[0][i].toString().trim()).toString();
        }

        if (arrTableData[0][i].toString().trim().equals(sortColName))
          defaultSorting.append(i);
      }

      if (defaultSorting.toString().trim().equals(""))
        defaultSorting.append("0");

      String sortColumn = rptUtilsBean.strArrayToStr(arrSortColumnType, ",");
      arrSortColumnType = rptUtilsBean.strToArrayData(sortColumn, ",");
      sortColumn = rptUtilsBean.strArrayToStr(arrSortColumnType, ",");

      Log.debugLog(className, "execute_nsi_db_get_tx_timing_detail_data", "", "", "Column type for sorting = " + sortColumnType);

      sortColumnType.append(sortColumn);
      return arrTableData;
    }
    catch (Exception e)
    {
      defaultSorting.append("0");
      e.printStackTrace();
      arrResult = new String[2][1];
      arrResult[0][0] = FAIL_QUERY;
      arrResult[1][0] = "No records are found";
      Log.stackTraceLog(className, "execute_nsi_db_get_tx_timing_detail_data", "", "", "Exception - ", e);
      return arrResult;
    }
  }

  public String[][] CalculateDataForBar(String[][] arrData)
  {
    try
    {
      // Initialize array's and variable.
      if (arrData == null && arrData.length == 1)
        return null;
      
      double startTime[] = new double[arrData.length - 1]; // Store start time of txComponent.
      double endTime[] = new double[arrData.length - 1]; // Store end time of txComponent.
      
      double start_Time = 0.0;
      int index = 0;

      // Calculating relative start time and end time.
      for (int i = 1; i < arrData.length; i++)
      {
        start_Time = Double.parseDouble(arrData[1][1]);
        startTime[index] = Double.parseDouble(arrData[i][1]) - start_Time;
        endTime[index] = startTime[index] + Double.parseDouble(arrData[i][4]) + Double.parseDouble(arrData[i][3]);
        index++;
      }
      
      // Calculating scale value for scale bar.
      double end_Time = 0.0;
      for (int i = 0; i < endTime.length; i++)
      {
        if (end_Time < endTime[i])
          end_Time = endTime[i];
        
        System.out.println("start = "+startTime[i]+  "  end time = "+endTime[i] + " end_Time = "+end_Time );

      }

      index = 0;

      // Array for storing bar data.
      arrDataForBar = new String[arrData.length - 1][8];
      for (int i = 1; i < arrData.length; i++)
      {
        arrDataForBar[index][0] = arrData[i][0]; // TxComponent Name.
        double startValue = (startTime[index] * 900) / end_Time;
        System.out.println("startValue = "+startValue + "  startTime = "+startTime[index]);
        arrDataForBar[index][1] = rptUtilsBean.convertTodecimal(startValue, 3) + ""; // Start Time of TxComponent.

        double endValue = (endTime[index] * 900) / end_Time;
        System.out.println("endValue = "+endValue + "  endTime = "+endTime[index]);

        double interval = endValue - startValue;
        System.out.println("interval = "+interval);
        arrDataForBar[index][2] = rptUtilsBean.convertTodecimal(endValue, 3) + ""; // End Time of TxComponent.
        
        //Recalculation of data.
        Double resEndTime = Double.parseDouble(arrData[i][4])*plottedArea/end_Time ;
        Double waitTime = Double.parseDouble(arrData[i][3])*plottedArea/end_Time ;
       
        arrDataForBar[index][3] = endTime[index] + ""; // Response time in ms.
        arrDataForBar[index][4] = resEndTime + ""; // End time in ms.
        arrDataForBar[index][5] = rptUtilsBean.convertTodecimal(interval, 3) + ""; // Interval of TxComponent.
        arrDataForBar[index][6] = arrData[i][7]; // Determining Critical Path.
        arrDataForBar[index][7] = waitTime + ""; // Determining queue wait Time.
        

        index++;
      }
      return arrDataForBar;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  /*
   * This return 10 length array to shoe scaling for bar
   * change Scale values upto 15.(As plotted Area change to 900).
   */
  public String[] getScaleForBar(double totalTime)
  {
    try
    {
      String arrScale[] = new String[15];
      for (int i = 1; i <= 15; i++)
      {
        arrScale[i - 1] = rptUtilsBean.convertTodecimal(((i * totalTime) / 15), 3);
      }
      return arrScale;
    }
    catch (Exception e)
    {
      return new String[]
      { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14" };
    }
  }

  // getting Response time for Scale
  public double getResponseTimeForBar(String[][] arrData)
  {
    try
    {
      if(arrData == null)
        return 0.0;
      
      double responseTime = 0.0;
      for (int i = 0; i < arrData.length; i++)
      {
        System.out.println("end time = "+ Double.parseDouble(arrData[i][3]));
        if (responseTime < Double.parseDouble(arrData[i][3]))
          responseTime = Double.parseDouble(arrData[i][3]);
      }
      return responseTime;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return 0.0;
    }
  }

  // execute_nsi_db_txc_get_tx_instance_data
  public String[][] execute_nsi_db_txc_get_tx_instance_data(String cmdArg, StringBuffer sortColumnType, StringBuffer defaultSorting, StringBuffer totalRows, String currLimit, String currOffset, DrillDownBreadCrumb downBreadCrumb, String strCurrentURlBreadCrumb)
  {
    Log.debugLog(className, "execute_nsi_db_txc_get_tx_instance_data", "", "", "Method called. = " + cmdArg);
    String arrResult[][] = null;
    try
    {
      long startTimeStamp = System.currentTimeMillis();
      if (downBreadCrumb.getFilterArument().equals("") || (!downBreadCrumb.getOffset().equals(currOffset) || !downBreadCrumb.getLimit().equals(currLimit) || !strCurrentURlBreadCrumb.trim().equals(downBreadCrumb.getBreadCrumbLabel().trim()) || !downBreadCrumb.getFilterArument().equals(cmdArg)))
      {
        String limitOffset = " --limit " + currLimit + " --offset " + currOffset;
        strCmdName = NSI_DB_GET_TXC_GET_TX_INSTANCE_DATA;

        Log.debugLog(className, "execute_nsi_db_txc_get_tx_instance_data", "", "", "Command Name = " + strCmdName);

        vecCmdOutput = new Vector();
        // boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);
        // creating inner class object up to 2 length to get count and data
        getCommandOutputObj = new getCommandOutput[2];
        Thread[] threadArr = new Thread[2];

        for (int i = 0; i < getCommandOutputObj.length; i++)
        {
          // this boolean will give all thread started or not default false
          if (i == getCommandOutputObj.length - 1)
            isAliveAll = true;

          // command with limit offset to get data
          if (i == 0)
            strCmdArgs = " --testrun " + testRun + " " + cmdArg + " " + GET_COUNT_OPTION + " " + DEFAULT_VALUE_GET_COUNT;
          else
            strCmdArgs = " --testrun " + testRun + " " + cmdArg + limitOffset;

          Log.debugLog(className, "execute_nsi_db_txc_get_tx_instance_data", "", "", "Command Argument = " + strCmdArgs);

          // creating class object array
          getCommandOutputObj[i] = new getCommandOutput(i, strCmdName, strCmdArgs, this);

          // if thread is enable
          if (IS_EXECUTE_QUERY_IN_THREAD_ENABLE.equals("1"))
          {
            threadArr[i] = new Thread(getCommandOutputObj[i], "Query - " + i);
            // Starting thread
            threadArr[i].start();
          }
          else
            // if threading is not enable
            getCommandOutputObj[i].getResultByCmdToGetOutput();
        }

        // notify all thread
        // checking thread is running or not
        // All thread are not alive it stops all thread
        waitForQueryRunThreads(threadArr);

        long endTimeStamp = System.currentTimeMillis();

        Log.debugLog(className, "execute_nsi_db_txc_get_tx_instance_data", "", "", "Total Time taken = " + (endTimeStamp - startTimeStamp));

        vecCmdOutput = getCommandOutputObj[0].getQueryOutput();
        if (!getCommandOutputObj[0].getQueryStatus())
        {
          totalRows.append(0);
          defaultSorting.append("0");
          sortColumnType.append("String");
          arrResult = new String[2][1];
          arrResult[0][0] = FAIL_QUERY;
          arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
          downBreadCrumb.setBreadCrumbData(arrResult);
          return arrResult;
        }
        else
        {
          totalRows.append(vecCmdOutput.get(1).toString().trim());
        }

        vecCmdOutput = new Vector();

        vecCmdOutput = getCommandOutputObj[1].getQueryOutput();

        if (!getCommandOutputObj[1].getQueryStatus())
        {
          defaultSorting.append("0");
          sortColumnType.append("String");
          arrResult = new String[2][1];
          arrResult[0][0] = FAIL_QUERY;
          arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
          downBreadCrumb.setBreadCrumbData(arrResult);
          return arrResult;
      }

        arrResult = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "-");
        downBreadCrumb.setBreadCrumbData(arrResult);
      }
      else
      {
        arrResult = downBreadCrumb.getBreadCrumbData();
        totalRows.append(downBreadCrumb.getTotalRecord());
        // sortColumnType.append(downBreadCrumb.getSortColumnType());
        // defaultSorting.append(downBreadCrumb.getDefaultSortIndicator());
        downBreadCrumb.setBreadCrumbData(arrResult);
      }

      String arrSortColumnType[] = new String[arrResult[0].length];

      // Default fill sorting Type Number
      Arrays.fill(arrSortColumnType, "DecimalNum");

      String sortColName = "StartTime";

      for (int i = 0; i < arrResult[0].length; i++)
      {
        // setting column type index and if flag false skip column to sorting
        boolean bolFlag = setColumnTypeIndex(arrResult[0][i].toString().trim(), i);

        if (!bolFlag)
          arrSortColumnType[i] = ",";
        else if (mapSort.containsKey(arrResult[0][i].toString().trim()) && bolFlag)
        {
          arrSortColumnType[i] = mapSort.get(arrResult[0][i].toString().trim()).toString();
        }

        if (arrResult[0][i].toString().trim().equals(sortColName))
          defaultSorting.append(i);
      }

      if (defaultSorting.toString().trim().equals(""))
        defaultSorting.append("0");

      String sortColumn = rptUtilsBean.strArrayToStr(arrSortColumnType, ",");
      arrSortColumnType = rptUtilsBean.strToArrayData(sortColumn, ",");
      sortColumn = rptUtilsBean.strArrayToStr(arrSortColumnType, ",");

      Log.debugLog(className, "execute_nsi_db_txc_get_tx_instance_data", "", "", "Column type for sorting = " + sortColumnType);

      sortColumnType.append(sortColumn);
      return arrResult;
    }
    catch (Exception e)
    {
      defaultSorting.append("0");
      e.printStackTrace();
      arrResult = new String[2][1];
      arrResult[0][0] = FAIL_QUERY;
      arrResult[1][0] = "No records are found";
      Log.stackTraceLog(className, "execute_nsi_db_txc_get_tx_instance_data", "", "", "Exception - ", e);
      return arrResult;
    }
  }

  // execute_nsi_db_txc_get_tx_signature
  public String[][] execute_nsi_db_txc_get_tx_signature(String cmdArg, StringBuffer sortColumnType, StringBuffer defaultSorting, StringBuffer totalRows, String currLimit, String currOffset, DrillDownBreadCrumb downBreadCrumb, String strCurrentURlBreadCrumb)
  {
    Log.debugLog(className, "execute_nsi_db_txc_get_tx_signature", "", "", "Method called. = " + cmdArg);
    String arrResult[][] = null;
    try
    {
      long startTimeStamp = System.currentTimeMillis();
      if (downBreadCrumb.getFilterArument().equals("") || (!downBreadCrumb.getOffset().equals(currOffset) || !downBreadCrumb.getLimit().equals(currLimit) || !strCurrentURlBreadCrumb.trim().equals(downBreadCrumb.getBreadCrumbLabel().trim()) || !downBreadCrumb.getFilterArument().equals(cmdArg)))
      {
        strCmdName = NSI_DB_GET_TXC_SIGNATURE;
        
        Log.debugLog(className, "execute_nsi_db_txc_get_tx_signature", "", "", "Command Name = " + strCmdName);
        String limitOffset = " --limit " + currLimit + " --offset " + currOffset;

        Log.debugLog(className, "execute_nsi_db_txc_get_tx_signature", "", "", "Command Name = " + strCmdName);

        vecCmdOutput = new Vector();
        // boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);
        // creating inner class object up to 2 length to get count and data
        getCommandOutputObj = new getCommandOutput[2];
        Thread[] threadArr = new Thread[2];

        for (int i = 0; i < getCommandOutputObj.length; i++)
        {
          // this boolean will give all thread started or not default false
          if (i == getCommandOutputObj.length - 1)
            isAliveAll = true;

          // command with limit offset to get data
          if (i == 0)
            strCmdArgs = " --testrun " + testRun + " " + cmdArg + " " + GET_COUNT_OPTION + " " + DEFAULT_VALUE_GET_COUNT;
          else
            strCmdArgs = " --testrun " + testRun + " " + cmdArg + limitOffset;

          Log.debugLog(className, "execute_nsi_db_txc_get_tx_signature", "", "", "Command Argument = " + strCmdArgs);

          // creating class object array
          getCommandOutputObj[i] = new getCommandOutput(i, strCmdName, strCmdArgs, this);

          // if thread is enable
          if (IS_EXECUTE_QUERY_IN_THREAD_ENABLE.equals("1"))
          {
            threadArr[i] = new Thread(getCommandOutputObj[i], "Query - " + i);
            // Starting thread
            threadArr[i].start();
          }
          else
            // if threading is not enable
            getCommandOutputObj[i].getResultByCmdToGetOutput();
        }

        // notify all thread
        // checking thread is running or not
        // All thread are not alive it stops all thread
        waitForQueryRunThreads(threadArr);

        long endTimeStamp = System.currentTimeMillis();

        Log.debugLog(className, "execute_nsi_db_txc_get_tx_signature", "", "", "Total Time taken = " + (endTimeStamp - startTimeStamp));

        vecCmdOutput = getCommandOutputObj[0].getQueryOutput();
        if (!getCommandOutputObj[0].getQueryStatus())
        {
          totalRows.append(0);
          defaultSorting.append("0");
          sortColumnType.append("String");
          arrResult = new String[2][1];
          arrResult[0][0] = FAIL_QUERY;
          arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
          downBreadCrumb.setBreadCrumbData(arrResult);
          return arrResult;
        }
        else
        {
          totalRows.append(vecCmdOutput.get(1).toString().trim());
        }

        vecCmdOutput = new Vector();

        vecCmdOutput = getCommandOutputObj[1].getQueryOutput();

        if (!getCommandOutputObj[1].getQueryStatus())
        {
          defaultSorting.append("0");
          sortColumnType.append("String");
          arrResult = new String[2][1];
          arrResult[0][0] = FAIL_QUERY;
          arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
          downBreadCrumb.setBreadCrumbData(arrResult);
          return arrResult;
      }

        arrResult = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "-");
        downBreadCrumb.setBreadCrumbData(arrResult);
      }
      else
      {
        arrResult = downBreadCrumb.getBreadCrumbData();
        totalRows.append(downBreadCrumb.getTotalRecord());
        downBreadCrumb.setBreadCrumbData(arrResult);
      }

      String arrSortColumnType[] = new String[arrResult[0].length];

      // Default fill sorting Type Number
      Arrays.fill(arrSortColumnType, "DecimalNum");

      String sortColName = "SvcSignatureName";

      for (int i = 0; i < arrResult[0].length; i++)
      {
        // setting column type index and if flag false skip column to sorting
        boolean bolFlag = setColumnTypeIndex(arrResult[0][i].toString().trim(), i);

        if (!bolFlag)
          arrSortColumnType[i] = ",";
        else if (mapSort.containsKey(arrResult[0][i].toString().trim()) && bolFlag)
        {
          arrSortColumnType[i] = mapSort.get(arrResult[0][i].toString().trim()).toString();
        }

        if (arrResult[0][i].toString().trim().equals(sortColName))
          defaultSorting.append(i);
      }

      if (defaultSorting.toString().trim().equals(""))
        defaultSorting.append("0");

      String sortColumn = rptUtilsBean.strArrayToStr(arrSortColumnType, ",");
      arrSortColumnType = rptUtilsBean.strToArrayData(sortColumn, ",");
      sortColumn = rptUtilsBean.strArrayToStr(arrSortColumnType, ",");

      Log.debugLog(className, "execute_nsi_db_txc_get_tx_signature", "", "", "Column type for sorting = " + sortColumnType);

      sortColumnType.append(sortColumn);
      return arrResult;
    }
    catch (Exception e)
    {
      defaultSorting.append("0");
      e.printStackTrace();
      arrResult = new String[2][1];
      arrResult[0][0] = FAIL_QUERY;
      arrResult[1][0] = "No records are found";
      Log.stackTraceLog(className, "execute_nsi_db_txc_get_tx_signature", "", "", "Exception - ", e);
      return arrResult;
    }
  }

  // execute_nsi_db_txc_get_tx_critical_path
  public String[][] execute_nsi_db_txc_get_tx_critical_path(String cmdArg, StringBuffer sortColumnType, StringBuffer defaultSorting, StringBuffer totalRows, String currLimit, String currOffset, DrillDownBreadCrumb downBreadCrumb, String strCurrentURlBreadCrumb)
  {
    Log.debugLog(className, "execute_nsi_db_txc_get_tx_critical_path", "", "", "Method called. = " + cmdArg);
    String arrResult[][] = null;
    try
    {
      long startTimeStamp = System.currentTimeMillis();
      if (downBreadCrumb.getFilterArument().equals("") || (!downBreadCrumb.getOffset().equals(currOffset) || !downBreadCrumb.getLimit().equals(currLimit) || !strCurrentURlBreadCrumb.trim().equals(downBreadCrumb.getBreadCrumbLabel().trim()) || !downBreadCrumb.getFilterArument().equals(cmdArg)))
      {
        strCmdName = NSI_DB_GET_TXC_CRITICAL_PATH;
        
        Log.debugLog(className, "execute_nsi_db_txc_get_tx_critical_path", "", "", "Command Name = " + strCmdName);
        String limitOffset = " --limit " + currLimit + " --offset " + currOffset;

        Log.debugLog(className, "execute_nsi_db_txc_get_tx_critical_path", "", "", "Command Name = " + strCmdName);

        vecCmdOutput = new Vector();
        // boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);
        // creating inner class object up to 2 length to get count and data
        getCommandOutputObj = new getCommandOutput[2];
        Thread[] threadArr = new Thread[2];

        for (int i = 0; i < getCommandOutputObj.length; i++)
        {
          // this boolean will give all thread started or not default false
          if (i == getCommandOutputObj.length - 1)
            isAliveAll = true;

          // command with limit offset to get data
          if (i == 0)
            strCmdArgs = " --testrun " + testRun + " " + cmdArg + " " + GET_COUNT_OPTION + " " + DEFAULT_VALUE_GET_COUNT;
          else
            strCmdArgs = " --testrun " + testRun + " " + cmdArg + limitOffset;

          Log.debugLog(className, "execute_nsi_db_txc_get_tx_critical_path", "", "", "Command Argument = " + strCmdArgs);

          // creating class object array
          getCommandOutputObj[i] = new getCommandOutput(i, strCmdName, strCmdArgs, this);

          // if thread is enable
          if (IS_EXECUTE_QUERY_IN_THREAD_ENABLE.equals("1"))
          {
            threadArr[i] = new Thread(getCommandOutputObj[i], "Query - " + i);
            // Starting thread
            threadArr[i].start();
          }
          else
            // if threading is not enable
            getCommandOutputObj[i].getResultByCmdToGetOutput();
        }

        // notify all thread
        // checking thread is running or not
        // All thread are not alive it stops all thread
        waitForQueryRunThreads(threadArr);

        long endTimeStamp = System.currentTimeMillis();

        Log.debugLog(className, "execute_nsi_db_txc_get_tx_critical_path", "", "", "Total Time taken = " + (endTimeStamp - startTimeStamp));

        vecCmdOutput = getCommandOutputObj[0].getQueryOutput();
        if (!getCommandOutputObj[0].getQueryStatus())
        {
          totalRows.append(0);
          defaultSorting.append("0");
          sortColumnType.append("String");
          arrResult = new String[2][1];
          arrResult[0][0] = FAIL_QUERY;
          arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
          downBreadCrumb.setBreadCrumbData(arrResult);
          return arrResult;
        }
        else
        {
          totalRows.append(vecCmdOutput.get(1).toString().trim());
        }

        vecCmdOutput = new Vector();

        vecCmdOutput = getCommandOutputObj[1].getQueryOutput();

        if (!getCommandOutputObj[1].getQueryStatus())
        {
          defaultSorting.append("0");
          sortColumnType.append("String");
          arrResult = new String[2][1];
          arrResult[0][0] = FAIL_QUERY;
          arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
          downBreadCrumb.setBreadCrumbData(arrResult);
          return arrResult;
      }

        arrResult = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "-");
        downBreadCrumb.setBreadCrumbData(arrResult);
      }
      else
      {
        arrResult = downBreadCrumb.getBreadCrumbData();
        totalRows.append(downBreadCrumb.getTotalRecord());
        downBreadCrumb.setBreadCrumbData(arrResult);
      }

      String arrSortColumnType[] = new String[arrResult[0].length];

      // Default fill sorting Type Number
      Arrays.fill(arrSortColumnType, "DecimalNum");

      String sortColName = "SvcCPSignatureId";

      for (int i = 0; i < arrResult[0].length; i++)
      {
        // setting column type index and if flag false skip column to sorting
        boolean bolFlag = setColumnTypeIndex(arrResult[0][i].toString().trim(), i);

        if (!bolFlag)
          arrSortColumnType[i] = ",";
        else if (mapSort.containsKey(arrResult[0][i].toString().trim()) && bolFlag)
        {
          arrSortColumnType[i] = mapSort.get(arrResult[0][i].toString().trim()).toString();
        }

        if (arrResult[0][i].toString().trim().equals(sortColName))
          defaultSorting.append(i);
      }

      if (defaultSorting.toString().trim().equals(""))
        defaultSorting.append("0");

      String sortColumn = rptUtilsBean.strArrayToStr(arrSortColumnType, ",");
      arrSortColumnType = rptUtilsBean.strToArrayData(sortColumn, ",");
      sortColumn = rptUtilsBean.strArrayToStr(arrSortColumnType, ",");

      Log.debugLog(className, "execute_nsi_db_txc_get_tx_critical_path", "", "", "Column type for sorting = " + sortColumnType);

      sortColumnType.append(sortColumn);
      return arrResult;
    }
    catch (Exception e)
    {
      defaultSorting.append("0");
      e.printStackTrace();
      arrResult = new String[2][1];
      arrResult[0][0] = FAIL_QUERY;
      arrResult[1][0] = "No records are found";
      Log.stackTraceLog(className, "execute_nsi_db_txc_get_tx_critical_path", "", "", "Exception - ", e);
      return arrResult;
    }
  }

    
  public static void main(String args[])
  {
    int choice = 0;

    DrillDownQueryUtils downUtils = new DrillDownQueryUtils("netstorm");
    DrillDownReportQuery daDownReportData = downUtils.getReportInfo("hhhh");


    DrillDownTxComponentQuery downQueryData = new DrillDownTxComponentQuery("21100", "netstorm", daDownReportData);
    String arrData[] = downQueryData.getScaleForBar(936.0);

    for (int i = 0; i < arrData.length; i++)
      System.out.println("arrData = " + arrData[i]);

    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    System.out.println("********Please enter the option for desired operation*********");
    System.out.println("Get Object data query : 1");

    System.out.println("*************************************************************");
    try
    {
      choice = Integer.parseInt(br.readLine());
    }
    catch(IOException e)
    {
      System.out.println("Error in entered choice: " + e);
    }

    switch(choice)
    {
      case 1:
       // String cmdArg = "--object 0 --group Session --fields 4079 --status -2";
        //String cmdArg = " --object 0 --group Page --fields 3759 --status 1xx,2xx --starttime 0 --endtime 0 --url /tours/index.html --limit 100 --offset 0";
        String cmdArg = daDownReportData.getFilterSetting();
        StringBuffer sortColumnType = new StringBuffer();
        StringBuffer defaultSorting = new StringBuffer();
        StringBuffer totalCount = new StringBuffer();

        System.out.println("\n\n");
      System.out.println("JJJJJ " + sortColumnType.toString() + ", - " + defaultSorting.toString() + ", totalCount = " + totalCount);
        break;

      default:
        System.out.println("Please select the correct option.");
    }
  }
}

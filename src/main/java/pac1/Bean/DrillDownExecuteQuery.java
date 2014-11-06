/**DrillDownExecuteQuery
 * Purpose: Handling query execution
**/

package pac1.Bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Vector;

import pac1.Bean.Log;
import pac1.Bean.rptUtilsBean;
import pac1.Bean.TestRunPhaseInfo.Phases;

public class DrillDownExecuteQuery
{
  private static String className = "DrillDownQueryData";

  String testRun = "-1";
  String userName = "";
  DrillDownReportQuery drillDownReportData = null;

  //Common variables for excute cmd
  String strCmdName = "";
  String strCmdArgs = "";
  Vector vecCmdOutput = new Vector();
  String[][] arrDataValues = null;
  CmdExec cmdExec = new CmdExec();

  LinkedHashMap mapSort = new LinkedHashMap();
  LinkedHashMap mapDataBaseSort = new LinkedHashMap();

  static String ENABLE_BROWSER_FILTER = "netstorm.reports.enableBrowser";
  static String ENABLE_LOC_AND_ACCESS_FILTER = "netstorm.reports.enableLocAccForWAN";
  static String PAGINATION_LIMIT = "netstorm.reports.defaultLimit";
  static String EXECUTE_QUERY_IN_THREAD = "netstorm.reports.queryThread";

  public static String IS_BROWSER_ENABLE = "0";
  public static String IS_LOCATION_ACCESS_ENABLE = "0"; //1 means keyword on

  //keyword to show cpu time
  public static String cpuTimeFlag = "netdiagnostics.methodTiming.cpuTime";
  public static String wallTimeFlag = "netdiagnostics.methodTiming.wallTime";
  public static String enableCpuTime = "0";
  public static String enableWallTime = "1";
  
  
  /*
   * Reason why these are static is the Method setConfigurableKeywords() is static
   * Configurable Keywords for Sequence Diagram
   * #Class Name Width of Sequence Diagram in Pixel
     netdiagnostics.seqDiagram.classWidth = 300;
         #Show Top N Methods Based on Method Time
         netdiagnostics.seqDiagram.top = 150;
         #Show Methods whose Method Time is Greater than given (in ms)
     netdiagnostics.seqDiagram.minWallTime = 1;
     #Highlight Methods whose Method Time is Greater than given (in ms)
     netdiagnostics.seqDiagram.defaultHighLight = 1500;
   */

  /**
   *  Keyword to configure Class Width
   */
  public static String classNameWidthKeyword = "netdiagnostics.seqDiagram.classWidth";

  /**
   * Keyword to configure Top N Methods
   */
  public static String topNMethodsKeyword = "netdiagnostics.seqDiagram.top";

  /**
   * Keyword to configure Minimum wall time
   */
  public static String filterGreatherThanKeyword = "netdiagnostics.seqDiagram.minWallTime";

  /**
   * Keyword to configure Default HighLight wall Time
   */
  public static String defaultHighLightKeyword = "netdiagnostics.seqDiagram.defaultHighLight";

  

  //Keyword to enable/disable compare options
  public static String disableCompareList = "0";

  /**
   * Keyword used for reading test run number from config.ini
   */
  public static String NDE_TEST_RUN_KEYWORD = "nde.testRunNum";

  /**
   * Variable used for storing configured test run number from config.ini
   */
  public static String NDE_TEST_RUN_NUMBER = "0";

  /**
   *A boolean variable that is used for checking mode is NS or NDE?
   *Default is false
   *Set to true once all the criteria is satisfied.
   */
  public static boolean IS_NDE_MODE = false;
  
  
  /**
   * Variable  to set  TOP Methods in Sequence Diagram
   */
  public static String defaultTopFilter = "150";

  /**
   * Variable  to set  Class Name Width in Pixel
   */
  public static String defaultClsNameWidthPixelFilter = "300";

  /**
   * Variable  to set  Method Time Filter
   */
  public static String defaultMethodTimeFilter = "1";

  /**
   * Variable to set Highlight Wall Time Filter
   */
  public static String defaultHighlightWallTimeFilter = "1500";



//Executing same query two times to get count and data,
  //so we are introducing thread concept to execute parallel two query to get count and get data
  public static String IS_EXECUTE_QUERY_IN_THREAD_ENABLE = "1"; //Default thread is on

  public static boolean IS_BROWSER_ENABLE_FROM_SRC_CSV = true; //1 means keyword on

  static String NSI_DB_GET_OBJ_DATA = "nsi_db_get_obj_data";
  static String NSI_DB_GET_OBJ_INSTANCE_DATA = "nsi_db_get_obj_instance_data";
  static String NSI_DB_GET_URL_COMP_DATA = "nsi_db_get_url_comp_data";
  static String NSI_DB_GET_URL_DATA = "nsi_db_get_url_data";
  static String NSI_DB_GET_USER_SESSION_DATA = "nsi_db_get_user_session_data";
  static String NSI_DB_GET_PG_COMP_DATA = "nsi_db_get_pg_comp_data";
  static String NSI_DB_GET_THINK_TIME_DATA = "nsi_db_get_think_time_data";
  static String NSI_DB_GET_TX_SESS_DATA = "nsi_db_get_tx_sess_data";
  static String NSI_DB_GET_FAILURE_DATA = "nsi_db_get_failure_data";
  static String NDI_GET_ENTITY_TIME_EX = "ndi_get_entity_time_ex";
  static String NSI_DB_GET_FLOWPATH_SUMMARY_DATA = "ndi_db_get_fp_signature_ex";
  static String NDI_DB_GET_TSA_METADATA = "ndi_db_get_tsa_metadata";
  static String NDI_DB_GET_THREAD_HOTSPOT_DATA = "ndi_db_get_thread_hotspot_data";
  static String NDI_DB_GET_THREAD_DUMP_DATA = "ndi_db_get_thread_hotspot_thread_dump";

  static String NDI_GET_FP_COUNT = "ndi_get_fp_count";
  static String NDI_GET_FP_SIGNATURE = "ndi_db_get_fp_signature_ex";
  static String NDI_GET_AGG_FP = "ndi_get_agg_fp";
  static String NDI_DB_GET_FP_DATA = "ndi_db_get_fp_data";
  static String NDI_GET_META_DATA = "ndi_get_meta_data";
  static String NDI_DB_GET_SERVICE_METHOD_TIMING = "ndi_db_get_service_method_timing";

  static String NSI_DB_GET_TR_ERROR_CODES = "nsi_db_get_tr_error_codes";
  static String NSI_DB_GET_OBJ_NAME_IDS = "nsi_db_get_obj_name_ids";
  static String NSI_DB_GET_LOCATION = "nsi_get_location";
  static String NSI_DB_GET_ACCESS = "nsi_get_access";
  static String NSI_DB_GET_USER_PROFILE_DATA = "nsi_db_get_user_profile_data";
  static String NSI_DB_GET_PHASE_LIST = "nsi_db_get_phase_list";
  static String NSI_DB_TR_STATUS = "nsi_db_tr_status";
  static String NSI_MIGRATE_TR_DATA = "nsi_migrate_tr_data";
  static String NSU_IMPORT = "nsu_import_and_gen_rep";
  static String NSU_SHOW_TEST_LOGS = "nsu_show_test_logs";
  static String NSI_DB_GET_APP_LOGS = "ndi_db_get_app_logs";
  static String NDI_DB_GET_QUERY_DATA = "ndi_db_get_query_data";
  static String NSI_GET_PAGEDUMP = "nsi_get_pagedump";
  static String NSU_GET_ERRORS = "nsu_get_errors";
  static String ND_DB_GET_URL_DETAILS_FROM_FPI = "ndi_db_get_url_details_from_fpi";
  static String NSI_DB_SVC_GET_SERVICE_INSTANCE_DATA = "nsi_db_svc_get_id_name_metadata";
  static String NDI_DB_GET_HTTP_HEADERS_INFO = "ndi_db_get_http_headers_info";
  static String NDI_DB_GET_EXCEPTION_DATA = "ndi_db_get_exception_data";
  static String NDI_DB_GET_EXCEPTION_METADATA = "ndi_db_get_exception_metadata";
  final static String GET_COUNT_OPTION = "--get_count";
  final static String DEFAULT_VALUE_GET_COUNT = "1"; //give row count
  
  //New Query For DBReport Group By Transaction
  static String NDI_DB_GET_QUERY_DATA_FOR_TRANSACTION = "ndi_db_get_query_data_for_transaction";
  
  //Query Name for Getting JMS Message Data with respect to flowpath Instance
  static String NDI_DB_GET_JMS_DATA = "ndi_db_get_jms_data";

  public final static int DEFAULT_OFFSET = 0;


  public static int DEFAULT_LIMIT = 22;

  public static String FAIL_QUERY = "1"; //give row count

  public int URL_NAME_INDEX = -1; //url
  public int URL_INDEX = -1; //url

  public int PAGE_NAME_INDEX = -1; //page
  public int PAGE_INDEX = -1; //page

  public int SESSION_NAME_INDEX = -1; //session
  public int SESSION_INDEX = -1; //session

  public int TRANSACTION_NAME_INDEX = -1; //transaction
  public int TRANSACTION_INDEX = -1; //transaction

  public int LOCATION_INDEX = -1; //location
  public int BROWSER_NAME_INDEX = -1; //browser
  public int BROWSER_INDEX = -1; //browser
  public int ACCESS_INDEX = -1; //access
  public int STATUS_INDEX = -1; //status
  public int STATUS_NAME_INDEX = -1; //status

  public int USER_ID_INDEX = -1; //user index
  public int SESSION_ID_INDEX = -1; //session index
  public int CHILD_INDEX = -1; //session index
  public int URL_INSTANCE_INDEX = -1; //session index

  public int PAGE_INSTANCE_INDEX = -1; //session index
  public int SESSION_INSTANCE_INDEX = -1; //session index
  public int TRANSACTION_INSTANCE_INDEX = -1; //session index

  public int AVERAGE_TIME_INDEX = -1; //average time
  public int TOTAL_TIME_INDEX = -1; //average time

  public int GENERATOR_NAME_INDEX = -1; // netcloud generator name
  public int GENERATOR_INDEX = -1;  //netcloud generator index field

  public int FLOWPATHINSINDEX = -1;
  public int AVERAGE_INDEX = -1;
  public int FPSIGNATUREIDX = -1;
  public int TIER_NAME_INDEX = -1;
  public int SERVER_NAME_INDEX = -1;
  public int APP_NAME_INDEX = -1;
  public int TIER_INDEX = -1;
  public int SERVER_INDEX = -1;
  public int APP_INDEX = -1;

  public int START_TIME_INDEX = -1;

  static Vector vecSrcData = new Vector();

  getCommandOutput[] getCommandOutputObj = null;
  boolean isAliveAll = false;

  private  String methName = "";
  private  String className1 = "";
  private  String pacName = ""; 

  public DrillDownExecuteQuery(String testRun, String userName, DrillDownReportQuery drillDownReportData)
  {
    this.testRun = testRun;
    this.drillDownReportData = drillDownReportData;
    this.userName = userName;

    readSrcFileInVector(testRun);
    setColumTypeForSorting();
    setConfigurableKeywords();
    setDataBaseSorting();
  }

  //Getting the Object Failure Code/Name according to the object Type
  public String getObjectErrorCodeAndName(String objectType, String objectValue, String valueType)
  {
    Log.debugLog(className, "getErrorCodes", "", "", "Method called.");
	String returnValue = "";

  	try
  	{
      strCmdName = NSU_GET_ERRORS;
      strCmdArgs = objectType + " " + "1";
      Vector vecCmdOutput = new Vector();
      boolean isQueryStatus = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, "netstorm", null);
      if(!isQueryStatus)
      {
      	return returnValue;
      }
      arrDataValues = new String[vecCmdOutput.size()][2];
      for(int vecIndex = 0; vecIndex < vecCmdOutput.size(); vecIndex++)
      {
      	String strTemp[] = rptUtilsBean.split(vecCmdOutput.get(vecIndex).toString(), "-");
      	arrDataValues[vecIndex][0] = strTemp[0].trim();
      	arrDataValues[vecIndex][1] = strTemp[1].trim();
      }
      if(valueType.equals("ErrorCode"))
  	  {
		for(int arrIndex = 0; arrIndex < arrDataValues.length; arrIndex++)
		{
		  if(arrDataValues[arrIndex][0].equals(objectValue))
		    returnValue =  arrDataValues[arrIndex][1];
		}
  	  }
	  else if(valueType.equals("ErrorName"))
	  {
		for(int arrIndex = 0; arrIndex < arrDataValues.length; arrIndex++)
		{
		  if(arrDataValues[arrIndex][1].trim().equals(objectValue.trim()))
			returnValue = arrDataValues[arrIndex][0];
		}
	  }
      return returnValue;
  	}
  	catch(Exception e)
  	{
  	  e.printStackTrace();
  	  return "";
  	}
  }
  
  /**
   * Setting Configurable filters in Sequence Diagram
   */
  private static void setSeqDiagramFilters() 
  {
     try
     {
  	   if(Integer.parseInt(defaultClsNameWidthPixelFilter) <= 300)
  	        defaultClsNameWidthPixelFilter = "300";
     }
     catch (Exception e) 
     {
    	 defaultClsNameWidthPixelFilter = "300";
     }
     
     try
     {
  	   if(Integer.parseInt(defaultTopFilter) <= 0)
  		   defaultTopFilter = "150";
     }
     catch (Exception e) 
     {
    	 defaultTopFilter = "150";
     }
     
     try
     {
  	   if(Integer.parseInt(defaultMethodTimeFilter) <= 0)
  	        defaultMethodTimeFilter = "1";
     }
     catch (Exception e) 
     {
    	 defaultMethodTimeFilter = "1";
     }
     
     try
     {
  	   if(Integer.parseInt(defaultHighlightWallTimeFilter) <= 0)
  	        defaultHighlightWallTimeFilter = "1500";
     }
     catch (Exception e) 
     {
    	 defaultHighlightWallTimeFilter = "1500";
     }
  }

  public static void setConfigurableKeywords()
  {
    long startTimeStamp =  System.currentTimeMillis();
    IS_BROWSER_ENABLE_FROM_SRC_CSV = isBrowserEnable();
    String confBrowser = Config.getValue(ENABLE_BROWSER_FILTER);
    String confLocAndAccess = Config.getValue(ENABLE_LOC_AND_ACCESS_FILTER);
    String confPaginationLimit = Config.getValue(PAGINATION_LIMIT);

    String confExecuteQueryInThread = Config.getValue(EXECUTE_QUERY_IN_THREAD);

    if(confBrowser != null && !confBrowser.equals(""))
    {
      if(confBrowser.trim().equals("1"))
        IS_BROWSER_ENABLE = confBrowser;
      else
        IS_BROWSER_ENABLE = "0";
    }

    enableCpuTime = Config.getValue(cpuTimeFlag);
    if(enableCpuTime.trim().isEmpty())
    	enableCpuTime = "0";
    
    
    enableWallTime = Config.getValue(wallTimeFlag);
    if(enableWallTime.trim().isEmpty())
    	enableWallTime = "1";
    
    //Setting Sequence Diagram filters
    defaultClsNameWidthPixelFilter = Config.getValue(classNameWidthKeyword);
    defaultTopFilter = Config.getValue(topNMethodsKeyword);
    defaultMethodTimeFilter = Config.getValue(filterGreatherThanKeyword);
    defaultHighlightWallTimeFilter = Config.getValue(defaultHighLightKeyword);
    setSeqDiagramFilters();


    //Getting the value  of nde test run num from config.ini
    NDE_TEST_RUN_NUMBER = Config.getValue(NDE_TEST_RUN_KEYWORD);

    if(confLocAndAccess != null && !confLocAndAccess.equals(""))
    {
      if(!confLocAndAccess.trim().equals(IS_LOCATION_ACCESS_ENABLE))
        IS_LOCATION_ACCESS_ENABLE = confLocAndAccess;
    }

    if(confPaginationLimit != null && !confPaginationLimit.equals(""))
    {
      if(!confPaginationLimit.trim().equals(PAGINATION_LIMIT))
        DEFAULT_LIMIT = Integer.parseInt(confPaginationLimit);
    }

    if(confExecuteQueryInThread != null && !confExecuteQueryInThread.equals(""))
    {
      if(!confExecuteQueryInThread.trim().equals(IS_EXECUTE_QUERY_IN_THREAD_ENABLE))
        IS_EXECUTE_QUERY_IN_THREAD_ENABLE = confExecuteQueryInThread;
    }
    long endTimeStamp =  System.currentTimeMillis();

    //System.out.println("setConfigurableKeywords = " + (endTimeStamp - startTimeStamp));
    Log.debugLog(className, "setConfigurableKeywords", "", "", "Total Time taken = " + (endTimeStamp - startTimeStamp));
    Log.debugLog(className, "setConfigurableKeywords", "", "", "IS_BROWSER_ENABLE_FROM_SRC_CSV = " + IS_BROWSER_ENABLE_FROM_SRC_CSV + ", IS_BROWSER_ENABLE = " + IS_BROWSER_ENABLE + ", IS_LOCATION_ACCESS_ENABLE = " + IS_LOCATION_ACCESS_ENABLE + ", DEFAULT_LIMIT = " + DEFAULT_LIMIT);
  }

  public static void readSrcFileInVector(String testRun)
  {
    long startTimeStamp =  System.currentTimeMillis();
    //vecSrcData = rptUtilsBean.getDataInVector(Config.getWorkPath() + "/webapps/logs/TR" + testRun + "/src.csv");

    try
    {
      File fileName = new File(Config.getWorkPath() + "/webapps/logs/TR" + testRun + "/src.csv");

      if(fileName.exists())
      {
        FileInputStream fis = new FileInputStream(fileName);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        String strLine = "";
        vecSrcData.clear();
        while((strLine = br.readLine()) !=  null)
        {
          strLine = strLine.trim();
          if(strLine.length() == 0)
            continue;
          if(strLine.startsWith("#"))
            continue;

          Log.debugLog(className, "readSrcFileInVector", "", "", "Adding line in vector. Line = " + strLine);
          vecSrcData.add(strLine);
          break;
        }

        br.close();
        fis.close();
      }
      long endTimeStamp =  System.currentTimeMillis();
      //System.out.println("r eadSrcFileInVector = " + (endTimeStamp - startTimeStamp));
      Log.debugLog(className, "readSrcFileInVector", "", "", "Total Time taken = " + (endTimeStamp - startTimeStamp));
    }
    catch (Exception e)
    {
    }
  }
  

  /**
   * Sets nde Mode to true if configured test run num is equal to current test
   * @param ndemode
   */
  public static void setNDEMode(boolean ndemode)
  {
      IS_NDE_MODE = ndemode;
  }

  /**
   * This method checks whether nde mode is enabled or not by comparing the configured test run num with current test run num
   */
  public static void isNDEModeEnable(boolean NDE, String testRun)
  {
      Log.debugLog(className, "isNDEModeEnable", "", "", "Method called.");
      try
      {
	 if(NDE)
	 {
	     //If test run number is equal to the configured test run num then set mode to NDE , else set to false
	     if(testRun.trim().equals(NDE_TEST_RUN_NUMBER.trim()))
	     {
	        Log.debugLog(className, "isNDEModeEnable", "", "", "Setting NDE Mode to true as Current test Run Number -  " + testRun + " Configured test run Number - " + NDE_TEST_RUN_NUMBER);
	        IS_NDE_MODE = true;
	     }
	     else
	     {
	        Log.debugLog(className, "isNDEModeEnable", "", "", "Setting NDE Mode to false as Current test Run Number -  " + testRun + " Configured test run Number - " + NDE_TEST_RUN_NUMBER);
	        IS_NDE_MODE = false;
	     }
	 }
	 else//setting to false if NDE is not set in cav.conf
	 {
	     Log.debugLog(className, "isNDEModeEnable", "", "", "Disabling NDE Mode as NDE Mode is " + NDE + "Current test Run Number -  " + testRun + " Configured test run Number - " + NDE_TEST_RUN_NUMBER);
	     IS_NDE_MODE = false;
	 }
      }
      catch(Exception e)
      {
	  Log.debugLog(className, "isNDEModeEnable", "", "", "Exception - " + e);
	  IS_NDE_MODE = false;
      }
  }



  public static boolean isBrowserEnable()
  {
    Log.debugLog(className, "isBrowserEnable", "", "", "Method called.");
    try
    {
      if(vecSrcData != null)
      {
        if(vecSrcData.size() > 0)
        {
          String strTemp[] = rptUtilsBean.split(vecSrcData.get(0).toString(), ",");

          if(strTemp.length > 9 && strTemp[9].trim().equals("NA"))
          {
            Log.debugLog(className, "isBrowserEnable", "", "", "strTemp[9] = " + strTemp[9]);
            return false;
          }
        }
      }
      else
        return false;

      return true;
    }
    catch (Exception e)
    {
      return false;
    }
  }

/*
 * Checking phase id in scr.csv file is -1 or number
 * if -1 exclude phases
 */
  public boolean isPhaseListEnable()
  {
    Log.debugLog(className, "isPhaseListEnable", "", "", "Method called.");
    try
    {
      if(vecSrcData != null)
      {
        if(vecSrcData.size() > 0)
        {
          String strTemp[] = rptUtilsBean.split(vecSrcData.get(0).toString(), ",");

          if(strTemp.length > 17 && strTemp[17].trim().equals("-1"))
          {
            Log.debugLog(className, "isPhaseListEnable", "", "", "strTemp[17]" + strTemp[17]);
            return false;
          }
        }
      }
      else
        return false;

      return true;
    }
    catch (Exception e)
    {
      return false;
    }
  }

/*
 * This method calculate current page and total number of pages of records
 */
  public int[] calculatePageNum(int offSet, int limit, int totalRecord)
  {
    Log.debugLog(className, "calculatePageNum", "", "", "Method called. Offset = " + offSet + ", limit = " + ", totalRecord = " + totalRecord);

    int arrPageInfo[] = new int[4];
    int currPageNum = 1;
    int numOfPages = 1;
    int spinnerPreFlag = 0;
    int spinnerNextFlag = 0;

    try
    {
      currPageNum = (offSet / limit) + 1; //current Page
      int remainderRecord = totalRecord % limit;
      numOfPages = totalRecord / limit; //Number of Page

      //if records is greater than limit and one is odd we are adding 1;
      if(remainderRecord != 0)
        numOfPages = numOfPages + 1;

      if(offSet < limit || offSet == 0)
        spinnerPreFlag = 1;

      if((offSet + limit) >= totalRecord)
        spinnerNextFlag = 1;


      Log.debugLog(className, "calculatePageNum", "", "", "Current Page = " + currPageNum + ", numOfPages = " + numOfPages);
      if(numOfPages == 0)
        numOfPages = 1;
      arrPageInfo[0] = currPageNum;
      arrPageInfo[1] = numOfPages;
      arrPageInfo[2] = spinnerPreFlag;
      arrPageInfo[3] = spinnerNextFlag;

      return arrPageInfo;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "calculatePageNum", "", "", "Exception - ", e);
      arrPageInfo[0] = currPageNum;
      arrPageInfo[1] = numOfPages;

      return arrPageInfo;
    }
  }

//saving type of the column to sort used vy table sorting by js
  public void setColumTypeForSorting()
  {
    long startTimeStamp =  System.currentTimeMillis();
    // Map is used to get column types for sorting
    mapSort.put("URL Name","String");
    mapSort.put("Page Name","String");
    mapSort.put("Transaction Name","String");
    mapSort.put("Session Name","String");
    mapSort.put("Script Name","String");
    mapSort.put("Page Count","Number");
    mapSort.put("Session Count","Number");
    mapSort.put("Location","String");
    mapSort.put("Access","String");
    mapSort.put("Browser","String");
    mapSort.put("Success","Number");
    mapSort.put("Status","String");
    mapSort.put("Tried","Number");
    mapSort.put("Fail","Number");
    mapSort.put("FailPct","Number");
    mapSort.put("%Fail","Number");
    mapSort.put("Min","String");
    mapSort.put("Average","String");
    mapSort.put("Max","String");
    mapSort.put("Median","String");
    mapSort.put("80%","String");
    mapSort.put("90%","String");
    mapSort.put("95%","String");
    mapSort.put("99%","String");
    mapSort.put("HTTP Code","Number");
    mapSort.put("Start Time","String");
    mapSort.put("Response Time","String");
    mapSort.put("Session Duration","String");
    mapSort.put("Total Time","String");
    mapSort.put("User Id","NumColon");
    mapSort.put("Session Id","NumColon");
    mapSort.put("generatorname", "String");

    long endTimeStamp =  System.currentTimeMillis();

    //System.out.println("setColumTypeForSorting = " + (endTimeStamp - startTimeStamp));
    Log.debugLog(className, "setColumTypeForSorting", "", "", "Total Time taken = " + (endTimeStamp - startTimeStamp));
  }

 //checking internal order by value and map with original name to find actual column
  public void setDataBaseSorting()
  {
    mapDataBaseSort.put("url", "URL Name");
    mapDataBaseSort.put("page", "Page Name");
    mapDataBaseSort.put("session", "Session Name");
    mapDataBaseSort.put("session", "Script Name");
    mapDataBaseSort.put("transaction", "Transaction Name");
    mapDataBaseSort.put("stime", "Start Time");
    mapDataBaseSort.put("rtime", "Response Time");
    mapDataBaseSort.put("rtimedesc", "Response Time");
    mapDataBaseSort.put("access", "Access");
    mapDataBaseSort.put("location", "Location");
    mapDataBaseSort.put("status", "Status");
    //mapDataBaseSort.put("status", "Status Name");
    //mapDataBaseSort.put("status", "ErrorName");
    mapDataBaseSort.put("browser", "Browser");
    mapDataBaseSort.put("flowinstance", "FlowPathInstance");
    mapDataBaseSort.put("generator", "generatorname");
  }
  //Location,Access
  public ArrayList getSelectedGroupBy()
  {
    String strGrpBy[] = rptUtilsBean.strToArrayData(drillDownReportData.getGroupBy(), ",");
    ArrayList arrGroupList = new ArrayList(rptUtilsBean.strArrayToList(strGrpBy));
    return arrGroupList;
  }

  //Min,Avg
  public ArrayList getSelectedShowData()
  {
    String strShowData[] = rptUtilsBean.strToArrayData(drillDownReportData.showDataSelectedLabel(), ",");
    ArrayList arrShowDataList =  new ArrayList(rptUtilsBean.strArrayToList(strShowData));
    return arrShowDataList;
  }

  //Getting URL, page, transaction and session of the basis of object type
  //0 - URl --> urlindex:sessionname:pagename:urlname
  //1 - Page --> pageindex:sessionname:pagename
  //2 - Transaction --> transactionindex:transactionname
  //3 - Session --> sessionindex:sessionname
  //4 - Flow Path -- > same as URL
  public String[][] getObjectNameIds(String arguments)
  {
    Log.debugLog(className, "getObjectNameIds", "", "", "Method called.");
    try
    {
      strCmdName = NSI_DB_GET_OBJ_NAME_IDS;
      strCmdArgs = "--testrun " + testRun + " " + arguments;
      vecCmdOutput = new Vector();

      boolean isQueryStatus = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);
      if(!isQueryStatus)
      {
        arrDataValues = new String[2][1];
        arrDataValues[0][0] = FAIL_QUERY;
        arrDataValues[1][0] = getDataInStringBuff(vecCmdOutput).toString();
        return arrDataValues;
      }

      arrDataValues = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "");

      if(arrDataValues == null)
      {
        arrDataValues = new String[1][1];
        arrDataValues[0][0] = FAIL_QUERY;
      }

      return arrDataValues;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getObjectNameIds", "", "", "Exception - ", e);
      arrDataValues = new String[2][1];
      arrDataValues[0][0] = FAIL_QUERY;
      arrDataValues[1][0] = "No records are found";
      return arrDataValues;
    }
  }

  public String[][] getExceptionMetaData(String arguments)
  {
    Log.debugLog(className, "getExceptionMetaData", "", "", "Method called.");
    try
    {
      strCmdName = NDI_DB_GET_EXCEPTION_METADATA;
      strCmdArgs = "--testrun " + testRun + " " + arguments;
      vecCmdOutput = new Vector();

      boolean isQueryStatus = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);
      if(!isQueryStatus)
      {
        arrDataValues = new String[2][1];
        arrDataValues[0][0] = FAIL_QUERY;
        arrDataValues[1][0] = getDataInStringBuff(vecCmdOutput).toString();
        return arrDataValues;
      }

      arrDataValues = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "");

      if(arrDataValues == null)
      {
        arrDataValues = new String[1][1];
        arrDataValues[0][0] = FAIL_QUERY;
      }

      return arrDataValues;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getExceptionMetaData", "", "", "Exception - ", e);
      arrDataValues = new String[2][1];
      arrDataValues[0][0] = FAIL_QUERY;
      arrDataValues[1][0] = "No records are found";
      return arrDataValues;
    }
  }


  //Getting Tier, Server data on the basis of object type
  public String[][] getOtherObjects(String arguments)
  {
    Log.debugLog(className, "getOtherObjects", "", "", "Method called.");
    try
    {
      strCmdName = NDI_DB_GET_TSA_METADATA;
      strCmdArgs = "--testrun " + testRun + " " + arguments;
      vecCmdOutput = new Vector();

      boolean isQueryStatus = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);
      if(!isQueryStatus)
      {
        arrDataValues = new String[2][1];
        arrDataValues[0][0] = FAIL_QUERY;
        arrDataValues[1][0] = getDataInStringBuff(vecCmdOutput).toString();
        return arrDataValues;
      }

      arrDataValues = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "");

      if(arrDataValues == null)
      {
        arrDataValues = new String[1][1];
        arrDataValues[0][0] = FAIL_QUERY;
      }

      return arrDataValues;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getOtherObjects", "", "", "Exception - ", e);
      arrDataValues = new String[2][1];
      arrDataValues[0][0] = FAIL_QUERY;
      arrDataValues[1][0] = "No records are found";
      return arrDataValues;
    }
  }

  //Getting access name of selected TR
  public String[][] getAccessName()
  {
    Log.debugLog(className, "getAccessName", "", "", "Method called.");
    try
    {
      //strCmdName = NSI_DB_GET_ACCESS;
      strCmdName = NSI_DB_GET_USER_PROFILE_DATA;
      strCmdArgs = "--testrun " + testRun + " --type access";
      vecCmdOutput = new Vector();
      boolean isQueryStatus = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

      if(!isQueryStatus)
      {
        arrDataValues = new String[2][1];
        arrDataValues[0][0] = FAIL_QUERY;
        arrDataValues[1][0] = getDataInStringBuff(vecCmdOutput).toString();
        return arrDataValues;
      }

      arrDataValues = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "");

      if(arrDataValues == null)
      {
        arrDataValues = new String[1][1];
        arrDataValues[0][0] = FAIL_QUERY;
      }

      return arrDataValues;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getAccessName", "", "", "Exception - ", e);
      arrDataValues = new String[2][1];
      arrDataValues[0][0] = FAIL_QUERY;
      arrDataValues[1][0] = "No records are found";
      return arrDataValues;
    }
  }

  //Getting Location Name
  public String[][] getLocationName(String arguments)
  {
    Log.debugLog(className, "getLocationName", "", "", "Method called.");
    try
    {
      //strCmdName = NSI_DB_GET_LOCATION;
      strCmdName = NSI_DB_GET_USER_PROFILE_DATA;
      strCmdArgs = "--testrun " + testRun + " --type location";
      vecCmdOutput = new Vector();
      boolean isQueryStatus = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

      if(!isQueryStatus)
      {
        arrDataValues = new String[2][1];
        arrDataValues[0][0] = FAIL_QUERY;
        arrDataValues[1][0] = getDataInStringBuff(vecCmdOutput).toString();
        return arrDataValues;
      }
      //for(int i = 0; i < )

      if(arrDataValues == null)
      {
        arrDataValues = new String[1][1];
        arrDataValues[0][0] = FAIL_QUERY;
      }

      return arrDataValues;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getLocationName", "", "", "Exception - ", e);
      arrDataValues = new String[2][1];
      arrDataValues[0][0] = FAIL_QUERY;
      arrDataValues[1][0] = "No records are found";
      return arrDataValues;
    }
  }

//Getting Location Name
  public String[][] getBrowserName(String arguments)
  {
    Log.debugLog(className, "getLocationName", "", "", "Method called.");
    try
    {
      //strCmdName = NSI_DB_GET_LOCATION;
      strCmdName = NSI_DB_GET_USER_PROFILE_DATA;
      strCmdArgs = "--testrun " + testRun + " --type browser";
      vecCmdOutput = new Vector();
      boolean isQueryStatus = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

      if(!isQueryStatus)
      {
        arrDataValues = new String[2][1];
        arrDataValues[0][0] = FAIL_QUERY;
        arrDataValues[1][0] = getDataInStringBuff(vecCmdOutput).toString();
        return arrDataValues;
      }
      arrDataValues = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "");

      if(arrDataValues == null)
      {
        arrDataValues = new String[1][1];
        arrDataValues[0][0] = FAIL_QUERY;
      }

      return arrDataValues;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getLocationName", "", "", "Exception - ", e);
      arrDataValues = new String[2][1];
      arrDataValues[0][0] = FAIL_QUERY;
      arrDataValues[1][0] = "No records are found";
      return arrDataValues;
    }
  }

  //Getting Service Instance Name of selected TR
  /**
   * Getting the Service Instance Id and Name of Selected Test Run for service report in DDR.
   * @return
   * instanceid:instancename <br/>
   * 0:APS2<br/>
   * 1:APS4<br/>
   * @author Kanchan.
   */
  public String[][] getServiceInstanceName()
  {
    Log.debugLog(className, "getServiceInstanceName", "", "", "Method called.");
    try
    {
      //strCmdName = NSI_DB_GET_ACCESS;
      strCmdName = NSI_DB_SVC_GET_SERVICE_INSTANCE_DATA;
      strCmdArgs = "--testrun " + testRun;
      vecCmdOutput = new Vector();
      boolean isQueryStatus = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

      if(!isQueryStatus)
      {
        arrDataValues = new String[2][1];
        arrDataValues[0][0] = FAIL_QUERY;
        arrDataValues[1][0] = getDataInStringBuff(vecCmdOutput).toString();
        return arrDataValues;
      }

      arrDataValues = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "");

      if(arrDataValues == null)
      {
        arrDataValues = new String[1][1];
        arrDataValues[0][0] = FAIL_QUERY;
      }

      return arrDataValues;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getServiceInstanceName", "", "", "Exception - ", e);
      arrDataValues = new String[2][1];
      arrDataValues[0][0] = FAIL_QUERY;
      arrDataValues[1][0] = "No records are found";
      return arrDataValues;
    }
  }

//Getting access name of selected TR
/*
 *  Getting phases from query and from global.dat file
 *  Mapping phases name and chech start time is NA or time
 *  if NA exclude phases
 */

  public String[][] getPhasesList()
  {
    Log.debugLog(className, "getAccessName", "", "", "Method called.");
    try
    {
      if(!isPhaseListEnable())
        return new String[][] {{"PhaseId","PhaseName","PhaseType","PhaseGroup","PhaseStartTime","PhaseEndTime"}};

      //strCmdName = NSI_DB_GET_ACCESS;
      strCmdName = NSI_DB_GET_PHASE_LIST;
      strCmdArgs = "--testrun " + testRun;
      vecCmdOutput = new Vector();
      boolean isQueryStatus = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

      if(!isQueryStatus)
      {
        arrDataValues = new String[2][1];
        arrDataValues[0][0] = FAIL_QUERY;
        arrDataValues[1][0] = getDataInStringBuff(vecCmdOutput).toString();
        return arrDataValues;
      }

      //arrDataValues = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "");
      arrDataValues = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "", 0, 0, 0, 2);

      if(arrDataValues == null)
      {
        arrDataValues = new String[1][1];
        arrDataValues[0][0] = FAIL_QUERY;
      }

      //If we pass scenario name it will give customize phase name in simple secnario case
      //otherwise it will read phase name from global.dat file
      /**String scenarioFile = Scenario.getSortedTRScenFileWithAbsolutePath(Integer.parseInt(testRun));

      File file = new File(scenarioFile);
      if(file.getName().lastIndexOf(".") > -1)
        scenarioFile = file.getName().substring(0, file.getName().lastIndexOf("."));
      else
        scenarioFile = file.getName();

      Log.debugLog(className, "getPhasesList", "", "", "scenarioFile = " + scenarioFile);**/

      LinkedHashMap hashMapPhase = new LinkedHashMap();

      TestRunPhaseInfo testRunPhaseInfo = new TestRunPhaseInfo(Integer.parseInt(testRun), "", -1);
      ArrayList arrListPhasesObj = testRunPhaseInfo.getPhaseInfo();
      Phases phaseInfo;
      for(int k = 0 ; k < arrListPhasesObj.size(); k++)
      {
        phaseInfo = (Phases) arrListPhasesObj.get(k);

        String startTime = phaseInfo.getstartTime().trim();
        String endTime = phaseInfo.getendTime().trim();
        if(!startTime.equals("NA"))
          startTime = "" + rptUtilsBean.convStrToMilliSec(startTime);

        if(!endTime.equals("NA"))
          endTime = "" + rptUtilsBean.convStrToMilliSec(endTime);

        hashMapPhase.put(phaseInfo.getphaseName(), startTime + "|" + endTime);
      }

      for(int i = 0; i < arrDataValues.length; i++)
      {
        if(hashMapPhase.containsKey(arrDataValues[i][3].trim()))
        {
          String arrTemp[] = rptUtilsBean.split(hashMapPhase.get(arrDataValues[i][3].trim()).toString(), "|");
          arrDataValues[i][4] = arrTemp[0];
          arrDataValues[i][5] = arrTemp[1];
        }
        else
        {
          arrDataValues[i][4] = "NA";
          arrDataValues[i][5] = "NA";
        }
      }
      return arrDataValues;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "getAccessName", "", "", "Exception - ", e);
      arrDataValues = new String[2][1];
      arrDataValues[0][0] = FAIL_QUERY;
      arrDataValues[1][0] = "No records are found";
      return arrDataValues;
    }
  }


  public String[][] getErrorCodes()
  {
    DrillDownBreadCrumb breadCrumb = new DrillDownBreadCrumb(testRun, drillDownReportData.getReportName(), -1);
    return getErrorCodes("", breadCrumb);
  }
  /**
   * This method name to get error name and code
   * @return
   */
  public String[][] getErrorCodes(String cmdArg, DrillDownBreadCrumb breadCrumb)
  {
    Log.debugLog(className, "getErrorCodes", "", "", "Method called.");
    try
    {
      if(breadCrumb.getFilterArument().trim().equals("") || !breadCrumb.getFilterArument().trim().equals(cmdArg.trim()))
      {
        strCmdName = NSI_DB_GET_TR_ERROR_CODES;
        String objectType = drillDownReportData.getObjectType();

        if(objectType.equals("4") || objectType.equals("5") || objectType.equals("6") || objectType.equals("15"))
          objectType = "0";

        strCmdArgs = " --testrun " + testRun + " --object " + objectType;
        vecCmdOutput = new Vector();
        boolean isQueryStatus = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

        if(!isQueryStatus)
        {
          arrDataValues = new String[2][1];
          arrDataValues[0][0] = FAIL_QUERY;
          arrDataValues[1][0] = getDataInStringBuff(vecCmdOutput).toString();
	  breadCrumb.setBreadCrumbDataFirst(arrDataValues);
          return arrDataValues;
        }
        arrDataValues = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "");
        breadCrumb.setBreadCrumbDataFirst(arrDataValues);
	  }
      else
      {
        arrDataValues = breadCrumb.getBreadCrumbDataFirst();
        breadCrumb.setBreadCrumbDataFirst(arrDataValues);
      }
      if(arrDataValues == null)
      {
        arrDataValues = new String[1][1];
        arrDataValues[0][0] = FAIL_QUERY;
      }
      return arrDataValues;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getErrorCodes", "", "", "Exception - ", e);
      arrDataValues = new String[2][1];
      arrDataValues[0][0] = FAIL_QUERY;
      arrDataValues[1][0] = "No records are found";
      return arrDataValues;
    }
  }

  public StringBuffer getDataInStringBuff(Vector vecData)
  {
    StringBuffer strBuff = new StringBuffer();

    for(int i = 0; i < vecData.size(); i++)
    {
      strBuff.append(vecData.get(i).toString() + "<br>");
    }

    return strBuff;
  }

/*
 * This method set index and return false if user want to skip column
 */
  private boolean setColumnTypeIndex(String columnName, int index)
  {
    if(columnName.equals("URL Name") || columnName.equalsIgnoreCase("URLName"))
    {
      URL_NAME_INDEX = index;
      return true;
    }

    else if(columnName.equalsIgnoreCase("UrlIndex"))
    {
      URL_INDEX = index;
      return false;
    }

    else if(columnName.equals("FlowPathInstance"))
    {
      FLOWPATHINSINDEX = index;
      return false;
    }

    else if(columnName.equals("FPDuration") || columnName.equals("AvgFPDuration"))
    {
      AVERAGE_INDEX = index;
      return true;
    }

    else if(columnName.equals("FlowpathSignature"))
    {
      FPSIGNATUREIDX = index;
      return false;
    }

    else if(columnName.equals("StartTime"))
    {
      START_TIME_INDEX = index;
      return true;
    }

    else if(columnName.equalsIgnoreCase("TierName") || columnName.equalsIgnoreCase("Tier Name"))
    {
      TIER_NAME_INDEX = index;
      return true;
    }

    else if(columnName.equalsIgnoreCase("GeneratorName"))
    {
      GENERATOR_NAME_INDEX = index;
      return true;
    }

    else if(columnName.equalsIgnoreCase("GeneratorId"))
    {
      GENERATOR_INDEX = index;
      return true;
    }

    else if(columnName.equalsIgnoreCase("ServerName") || columnName.equalsIgnoreCase("Server Name"))
    {
      SERVER_NAME_INDEX = index;
      return true;
    }

    else if(columnName.equalsIgnoreCase("AppName") || columnName.equalsIgnoreCase("App Name"))
    {
      APP_NAME_INDEX = index;
      return true;
    }

    else if(columnName.equalsIgnoreCase("Tierid"))
    {
      TIER_INDEX = index;
      return false;
    }

    else if(columnName.equalsIgnoreCase("Serverid"))
    {
      SERVER_INDEX = index;
      return false;
    }

    else if(columnName.equalsIgnoreCase("Appid"))
    {
      APP_INDEX = index;
      return false;
    }


    else if(columnName.equals("Page Name") || columnName.equalsIgnoreCase("PageName"))
    {
      PAGE_NAME_INDEX = index;
      return true;
    }

    else if(columnName.equalsIgnoreCase("pageindex"))
    {
      PAGE_INDEX = index;
      return false;
    }

    else if((columnName.equals("Session Name")) || (columnName.equals("Script Name"))|| (columnName.equalsIgnoreCase("SessionName")))
    {
      SESSION_NAME_INDEX = index;
      return true;
    }

    else if(columnName.equalsIgnoreCase("sessionindex"))
    {
      SESSION_INDEX = index;
      return false;
    }

    else if(columnName.equals("Transaction Name") || columnName.equalsIgnoreCase("transactionName"))
    {
      TRANSACTION_NAME_INDEX = index;
      return true;
    }

    else if(columnName.equalsIgnoreCase("transactionindex"))
    {
      TRANSACTION_INDEX = index;
      return false;
    }

    else if((columnName.equals("Location")) || (columnName.equals("User Location")))
    {
      LOCATION_INDEX = index;
      if(drillDownReportData.getWAN_ENV().equals("0") && IS_LOCATION_ACCESS_ENABLE.equals("0"))
        return false;
      return true;
    }

    else if((columnName.equals("Access")) || (columnName.equals("User Access")))
    {
      ACCESS_INDEX = index;
      if(drillDownReportData.getWAN_ENV().equals("0") && IS_LOCATION_ACCESS_ENABLE.equals("0"))
        return false;
      return true;
    }

    else if((columnName.equals("Browser")) || (columnName.equals("Browser Name")))
    {
      BROWSER_INDEX = index;

      if((!IS_BROWSER_ENABLE_FROM_SRC_CSV) || IS_BROWSER_ENABLE.equals("0"))
        return false;

      return true;
    }

    else if(columnName.equals("User Id"))
    {
      USER_ID_INDEX = index;
      return true;
    }
    else if(columnName.equals("Average"))
    {
      AVERAGE_TIME_INDEX = index;
      return true;
    }
    else if(columnName.equals("Session Id"))
    {
      SESSION_ID_INDEX = index;
      return true;
    }
    else if(columnName.equals("Child Index"))
    {
      CHILD_INDEX = index;
      return false;
    }
    else if(columnName.equals("URL Index"))
    {
      URL_INSTANCE_INDEX = index;
      return false;
    }

    else if(columnName.equals("Page Instance"))
    {
      PAGE_INSTANCE_INDEX = index;
      return false;
    }
    else if(columnName.equals("Tx Instance"))
    {
      TRANSACTION_INSTANCE_INDEX = index;
      return false;
    }
    else if((columnName.equals("Session Duration")) || (columnName.equals("Response Time")) || (columnName.equals("Total Time")))
    {
      TOTAL_TIME_INDEX = index;
      return true;
    }
    else if(columnName.equals("Status"))
    {
      STATUS_INDEX = index;
      return true;
    }
    else if(columnName.equals("ErrorName") || columnName.equals("Error Name") || columnName.equals("StatusName") || columnName.equals("Status Name"))
    {
      STATUS_NAME_INDEX = index;
      if(columnName.equals("Status Name"))
        return true;

      return false;
    }
    else if(columnName.equals("classname"))
    {
      return false;
    }
    else
      return true;

  }

  /**public synchronized void stopThread(Thread[] arrthread)
  {
    Log.debugLog(className, "stopThread", "", "", "Method called." + arrthread.length);
    try
    {
      do //infinite loop
      {
        //notifyAll();
        int counter = arrthread.length;
        //checking each thread is alive or not
        //If not, stopping thread and decrease counter
        //then break from the loop
        for(int ii = 0; ii < arrthread.length; ii++)
        {
          if(!arrthread[ii].isAlive())
          {
            counter--;
            //System.out.println("arrthread[ii].isAlive() " + arrthread[ii].isAlive());
            //arrthread[ii].notify();
            //Log.debugLog(className, "stopThread", "", "", "Stopping thread. Thread name = " + arrthread[ii].getName());
            arrthread[ii].stop();
          }
        }
        if (counter == 0)
        {
          isAliveAll = false;
          break;
        }
      } while (true);
    }
    catch (Exception e) {
      System.out.println("Exception in thread = " + e);
      // TODO: handle exception
    }
  }**/


  //for joining two or more threads
  public void JoinRunningThreads(Thread []threads)
  {
    if(threads == null)
      return;


    for(int i = 0; i < threads.length; i++)
    {
      try
      {
	 threads[i].join();     //join
      }
      catch(Exception e)
      {
	 e.printStackTrace();
      }
    }
  }

  /**
   * This method set the handler invoked when this thread abruptly terminates due to an uncaught exception.
   *
   * @return void
   */
  public void setUncaughtExceptionHandlerDDR(Thread currentThread)
  {
    Log.debugLog(className, "setUncaughtExceptionHandlerDDR", "", "", "Method Called.");
    try
    {
      //Method Implementation for catching exception.
      currentThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

        public void uncaughtException(Thread t, Throwable e) {
          // System.out.println(t.getName() + " throws exception := " + e.getMessage());

          String msg = "KKKK = The Thread named "+ t.getName() + "\n The output and command args of both thread is as follows \n";
          if(getCommandOutputObj != null)
          {
            try
            {
              for(int i = 0; i < getCommandOutputObj.length; i++)
              {
        	msg = msg + "The "+i+"th thread args = "+ getCommandOutputObj[i].strArgs + " \n";
        	msg = msg + "And vector length = "+getCommandOutputObj[i].getQueryOutput().size()+"\n";
        	msg = msg + "Thread Number for identifying error thread = "+getCommandOutputObj[i].threadNum+ "\n";
        	msg = msg + "------------------------------------------------------";

        	Log.stackTraceLog(className, "setUncaughtExceptionHandlerDDR", "", "", " Error is : "+msg, new Exception(e));
              }
            }
            catch(Exception ex)
            {
              e.printStackTrace();
              Log.stackTraceLog(className, "setUncaughtExceptionHandlerDDR", "", "", "Error in generating error msg : KKKK", ex);
            }
          }
        }
     });
    }
    catch(Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "setUncaughtExceptionHandlerDDR", "", "", "Exception comming due to := ", e);
    }
  }

  public synchronized void waitForQueryRunThreads(Thread[] arrthread)
  {
    Log.debugLog(className, "waitForQueryRunThreads", "", "", "Method called." + arrthread.length);
    try
    {
      int pendingThreadCount = arrthread.length;

      //joining threads
      if(IS_EXECUTE_QUERY_IN_THREAD_ENABLE.equals("1"))
	   JoinRunningThreads(arrthread);


      //comment for avoiding nullPointerException
      /* do //infinite loop
      {
        Log.debugLog(className, "waitForQueryRunThreads", "", "", "Waiting for notify from threads ...");
        // wait();
        Log.debugLog(className, "waitForQueryRunThreads", "", "", "Got notify ...");

        if(arrthread == null)
          break;

        //checking each thread is alive or not
        //If not, stopping thread and decrease counter
        //then break from the loop
        for(int ii = 0; ii < arrthread.length; ii++)
        {
          if((arrthread[ii] != null) && (!arrthread[ii].isAlive()))
          {
            Log.debugLog(className, "waitForQueryRunThreads", "", "", "Thread[" + ii + "]" + arrthread[ii].getName() + " is not alive");
            pendingThreadCount--;
          }
          else
            Log.debugLog(className, "waitForQueryRunThreads", "", "", "Thread[" + ii + "]" + arrthread[ii].getName() + " is alive");
        }
       // Thread.sleep(1000); // Just for testing.
        if (counter == 0)
        {
          isAliveAll = false;
          break;
        }
      } while (pendingThreadCount > 0); */
    }
    catch (Exception e) {
      System.out.println("Exception in thread = " + e);
      e.printStackTrace();
      // TODO: handle exception
    }
  }

/*
 * This method execute get_obj_data object query
 *
 * Arguments
 * cmdArg: arguments for query
 * sortColumnType: column type sorting (String, Number, Date)
 * defaultSorting: Sorting indicator
 * totalCount: give no. of records
 *
 */
  public String[][] execute_get_obj_data(String cmdArg, StringBuffer sortColumnType, StringBuffer defaultSorting, StringBuffer totalRows, String currlimit, String currOffset, DrillDownBreadCrumb downBreadCrumb)
  {
    Log.debugLog(className, "execute_get_obj_data", "", "", "Method called. = " + cmdArg);
    String arrResult[][] = null;
    try
    {
      if(downBreadCrumb.getFilterArument().equals("") || (!downBreadCrumb.getOffset().equals(currOffset) || !downBreadCrumb.getLimit().equals(currlimit) || !downBreadCrumb.getFilterArument().trim().equals(cmdArg.trim())))
      {
        long startTimeStamp =  System.currentTimeMillis();

        String limitOffSet = " --limit " + currlimit + " --offset " + currOffset;
        strCmdName = NSI_DB_GET_OBJ_DATA;
        Log.debugLog(className, "execute_get_obj_data", "", "", "Command Name = " + strCmdName);

        vecCmdOutput = new Vector();

        //creating inner class object up to 2 length to get count and data
        getCommandOutputObj = new getCommandOutput[2];
        Thread[] threadArr = new Thread[2];

        for(int i = 0; i < getCommandOutputObj.length; i++)
        {
          //this boolean will give all thread started or not default false
          if(i == getCommandOutputObj.length - 1)
            isAliveAll = true;

          //command with limit offset to get data
          if(i == 0)
            strCmdArgs = " --testrun " + testRun + " " + cmdArg + " " + GET_COUNT_OPTION + " " + DEFAULT_VALUE_GET_COUNT;
          else
            strCmdArgs = " --testrun " + testRun + " " + cmdArg + limitOffSet;

          Log.debugLog(className, "execute_get_obj_data", "", "", "Command Argument = " + strCmdArgs);

          //creating class object array
          getCommandOutputObj[i] = new getCommandOutput(i, strCmdName, strCmdArgs, this);

          //if thread is enable
          if(IS_EXECUTE_QUERY_IN_THREAD_ENABLE.equals("1"))
          {
            threadArr[i] = new Thread(getCommandOutputObj[i], "Query - " + i);

            //adding uncaughtException Handler
            setUncaughtExceptionHandlerDDR(threadArr[i]);

            //Starting thread
            threadArr[i].start();
          }
          else //if threading is not enable
            getCommandOutputObj[i].getResultByCmdToGetOutput();
        }

        //wait untill all thread started
        //if(!isAliveAll && IS_EXECUTE_QUERY_IN_THREAD_ENABLE.equals("1"))
        //  wait();

        // notify all thread
        //checking thread is running or not
        //All thread are not alive it stops all thread
        waitForQueryRunThreads(threadArr);

        long endTimeStamp =  System.currentTimeMillis();

        Log.debugLog(className, "execute_get_obj_data", "", "", "Total Time taken = " + (endTimeStamp - startTimeStamp));

        //boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

        vecCmdOutput = getCommandOutputObj[0].getQueryOutput();

        if(!getCommandOutputObj[0].getQueryStatus())
        {
          totalRows.append(0);
          defaultSorting.append("0");
          arrResult = new String[2][1];
          arrResult[0][0] = FAIL_QUERY;
          arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
          downBreadCrumb.setBreadCrumbData(arrResult);
          return arrResult;
        }
        else
        {
          totalRows.append(vecCmdOutput.get(1).toString().trim());
          //totalRows.append(10);
        }

        //strCmdArgs = " --testrun " + testRun + " " + cmdArg + limitOffSet;

        //Log.debugLog(className, "execute_get_obj_data", "", "", "Command Argument = " + strCmdArgs);

        vecCmdOutput = new Vector();
        //bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);
        vecCmdOutput = getCommandOutputObj[1].getQueryOutput();

        //Log.debugLog(className, "execute_get_obj_data", "", "", "Checking Commnad executed successfully or not. = " + bolRsltFlag);
        if(!getCommandOutputObj[1].getQueryStatus())
        {
          totalRows.append(0);
          defaultSorting.append("0");
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
        //sortColumnType.append(downBreadCrumb.getSortColumnType());
        //defaultSorting.append(downBreadCrumb.getDefaultSortIndicator());
        downBreadCrumb.setBreadCrumbData(arrResult);
      }

      //System.out.println("vecCmdOutput" + vecCmdOutput.toString());
      //System.out.println("arrResult[0].length" + arrResult[0].length);
      String arrSortColumnType[] = new String[arrResult[0].length];

      //Default fill sorting Type Number
      Arrays.fill(arrSortColumnType, "String");

      //Getting Selected Group By
      //ArrayList arrGroupByList = getSelectedGroupBy();

      //Getting Selected show data
      //ArrayList arrShowDataList = getSelectedShowData();

      //int objectType = Integer.parseInt(drillDownReportData.getObjectType());

      String sortColName = "";

      String[] sortDataBase = rptUtilsBean.split(drillDownReportData.getOrderBy(), ",");

      if(sortDataBase != null && sortDataBase.length > 0)
      {
        if(mapDataBaseSort.containsKey(sortDataBase[0].trim().toLowerCase()))
        {
          sortColName = mapDataBaseSort.get(sortDataBase[0].toLowerCase()).toString();
        }
      }

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

      Log.debugLog(className, "execute_get_obj_data", "", "", "Column type for sorting = " + sortColumnType);

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
      Log.stackTraceLog(className, "execute_get_obj_data", "", "", "Exception - ", e);
      return arrResult;
    }
  }

  //nsi_db_get_obj_instance_data
  public String[][] execute_get_obj_instance_data(String cmdArg, StringBuffer sortColumnType, StringBuffer defaultSorting, StringBuffer totalRows, String currLimit, String currOffset, DrillDownBreadCrumb downBreadCrumb)
  {
    Log.debugLog(className, "execute_get_obj_instance_data", "", "", "Method called. = " + cmdArg);
    String arrResult[][] = null;
    try
    {
      long startTimeStamp =  System.currentTimeMillis();

      if(downBreadCrumb.getFilterArument().equals("") || (!downBreadCrumb.getOffset().equals(currOffset) || !downBreadCrumb.getLimit().equals(currLimit) || !downBreadCrumb.getFilterArument().equals(cmdArg)))
      {
        String limitOffset = " --limit " + currLimit + " --offset " + currOffset;
        strCmdName = NSI_DB_GET_OBJ_INSTANCE_DATA;
        Log.debugLog(className, "execute_get_obj_instance_data", "", "", "Command Name = " + strCmdName);

        vecCmdOutput = new Vector();
        //boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);
        //creating inner class object up to 2 length to get count and data
        getCommandOutputObj = new getCommandOutput[2];
        Thread[] threadArr = new Thread[2];

        for(int i = 0; i < getCommandOutputObj.length; i++)
        {
          //this boolean will give all thread started or not default false
          if(i == getCommandOutputObj.length - 1)
            isAliveAll = true;

          //command with limit offset to get data
          if(i == 0)
            strCmdArgs = " --testrun " + testRun + " " + cmdArg + " " + GET_COUNT_OPTION + " " + DEFAULT_VALUE_GET_COUNT;
          else
            strCmdArgs = " --testrun " + testRun + " " + cmdArg + limitOffset;

          Log.debugLog(className, "execute_get_obj_instance_data", "", "", "Command Argument = " + strCmdArgs);

          //creating class object array
          getCommandOutputObj[i] = new getCommandOutput(i, strCmdName, strCmdArgs, this);

          //if thread is enable
          if(IS_EXECUTE_QUERY_IN_THREAD_ENABLE.equals("1"))
          {
            threadArr[i] = new Thread(getCommandOutputObj[i], "Query - " + i);

            //adding uncaughtException Handler
            setUncaughtExceptionHandlerDDR(threadArr[i]);

            //Starting thread
            threadArr[i].start();
          }
          else //if threading is not enable
            getCommandOutputObj[i].getResultByCmdToGetOutput();
        }

        //wait untill all thread started
        //if(!isAliveAll && IS_EXECUTE_QUERY_IN_THREAD_ENABLE.equals("1"))
         // wait();

        // notify all thread
        //checking thread is running or not
        //All thread are not alive it stops all thread
        waitForQueryRunThreads(threadArr);

        long endTimeStamp =  System.currentTimeMillis();

        Log.debugLog(className, "execute_get_obj_instance_data", "", "", "Total Time taken = " + (endTimeStamp - startTimeStamp));

        vecCmdOutput = getCommandOutputObj[0].getQueryOutput();
        if(!getCommandOutputObj[0].getQueryStatus())
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

       // strCmdArgs = " --testrun " + testRun + " " + cmdArg + limitOffset;

        //Log.debugLog(className, "execute_get_obj_instance_data", "", "", "Command Argument = " + strCmdArgs);

        vecCmdOutput = new Vector();
        //bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

        //Log.debugLog(className, "execute_get_obj_instance_data", "", "", "Checking Commnad executed successfully or not. = " + bolRsltFlag);
        vecCmdOutput = getCommandOutputObj[1].getQueryOutput();

        if(!getCommandOutputObj[1].getQueryStatus())
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
        //sortColumnType.append(downBreadCrumb.getSortColumnType());
        //defaultSorting.append(downBreadCrumb.getDefaultSortIndicator());
        downBreadCrumb.setBreadCrumbData(arrResult);
      }

      //System.out.println("vecCmdOutput" + vecCmdOutput.toString());
      //System.out.println("arrResult[0].length" + arrResult[0].length);
      String arrSortColumnType[] = new String[arrResult[0].length];

      //Default fill sorting Type String
      Arrays.fill(arrSortColumnType, "String");

      int sortIndex = -1;
      String sortColName = "Response Time";

      if(drillDownReportData.getObjectType().equals("3"))
      {
        mapDataBaseSort.put("rtime", "Session Duration");
        mapDataBaseSort.put("rtimedesc", "Session Duration");
        sortColName = "Session Duration";
      }
      mapDataBaseSort.put("status", "Status Name");

      String[] sortOrderBy = rptUtilsBean.split(drillDownReportData.getOrderBy(), ",");
      String[] sortDataBase = rptUtilsBean.split(drillDownReportData.getSortUsingDataBaseQuery(), ",");

      if(sortDataBase != null && sortDataBase.length > 0)
      {
        if(mapDataBaseSort.containsKey(sortDataBase[0].trim().toLowerCase()))
        {
          sortColName = mapDataBaseSort.get(sortDataBase[0].toLowerCase()).toString();
        }
      }
      else if(sortOrderBy != null && sortOrderBy.length > 0)
      {
        if(mapDataBaseSort.containsKey(sortOrderBy[0].trim().toLowerCase()))
        {
          sortColName = mapDataBaseSort.get(sortOrderBy[0].toLowerCase()).toString();
        }
      }

      for(int i = 0; i < arrResult[0].length; i++)
      {
        //setting column type index and if flag false skip column to sorting
        boolean bolFlag = setColumnTypeIndex(arrResult[0][i].toString().trim(), i);
        sortIndex++;

        if(!bolFlag)
          arrSortColumnType[i] = ",";
        else if(mapSort.containsKey(arrResult[0][i].toString().trim()) && bolFlag)
        {
          arrSortColumnType[i] = "," + mapSort.get(arrResult[0][i].toString().trim()).toString();
        }

        if(arrResult[0][i].toString().trim().equals(sortColName))
          defaultSorting.append(i);
      }

      if(defaultSorting.toString().trim().equals(""))
        defaultSorting.append("0");

      //String sortColumn = rptUtilsBean.strArrayToStr(arrSortColumnType, ",");

      String sortColumn = rptUtilsBean.strArrayToStr(arrSortColumnType, ",");
      arrSortColumnType = rptUtilsBean.strToArrayData(sortColumn, ",");
      sortColumn = rptUtilsBean.strArrayToStr(arrSortColumnType, ",");

      Log.debugLog(className, "execute_get_obj_instance_data", "", "", "Column type for sorting = " + sortColumnType);
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
      Log.stackTraceLog(className, "execute_get_obj_instance_data", "", "", "Exception - ", e);
      return arrResult;
    }
  }

  public String[][] getDataUptoLimit(String[][] arrData, int limit, int offSet)
  {
    Log.debugLog(className, "getDataUptoLimit", "", "", "limit = " + limit + ", offSet = " + offSet + ", arrData.length " + arrData.length);
    try
    {
      offSet = offSet + 1;
      int rows = 0;
      int numRows = limit;
      int totalRecords = arrData.length;
      int numCol = arrData[0].length;
      //if limit = 10, offset 10 and total records = 15
      //numRows = 15 - 10 = 5
      if(limit + offSet > totalRecords)
        numRows = totalRecords - offSet;

      Log.debugLog(className, "getDataUptoLimit", "", "", "Total Records = " + totalRecords + ", numRows = " + numRows + ", (offSet + numRows) = " + offSet + numRows);
      String  arrTempData[][] = new String[numRows + 1][numCol];

      int rowIndex = 0;
      arrTempData[0] = arrData[0];
      for(int i = offSet; i < offSet + numRows; i++)
      {
        rowIndex++;
        String arrTempCol[] = arrData[i];
        for(int ii = 0; ii < arrTempCol.length; ii++)
          arrTempData[rowIndex][ii] =  arrTempCol[ii];
      }

      return arrTempData;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return new String[0][0];
    }
  }

  public Object [] execute_get_url_data(String cmdArg, StringBuffer sortColumnType , StringBuffer defaultSorting, StringBuffer totalRows, String currLimit, String currOffset, DrillDownBreadCrumb downBreadCrumb)
  {
    Log.debugLog(className, "execute_get_url_data", "", "", "Method called. = " + cmdArg);
    String arrResult[][] = null;

    Object[] objResult = new Object[2];
    try
    {
      //if(downBreadCrumb.getFilterArument().equals("") || (!downBreadCrumb.getOffset().equals(currOffset) || !downBreadCrumb.getLimit().equals(currLimit) || !downBreadCrumb.getFilterArument().equals(cmdArg)))
      if(downBreadCrumb.getFilterArument().equals("") || !downBreadCrumb.getFilterArument().equals(cmdArg))
      {
        strCmdName = NSI_DB_GET_URL_COMP_DATA;
        String limitOffset = " --limit " + currLimit + " --offset " + currOffset;
        Log.debugLog(className, "execute_get_url_data", "", "", "Command Name = " + strCmdName);

        strCmdArgs = " --testrun " + testRun + " " + cmdArg;

        Log.debugLog(className, "execute_get_url_data", "", "", "Command Argument = " + strCmdArgs);

        vecCmdOutput = new Vector();
        boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

        Log.debugLog(className, "execute_get_url_data", "", "", "Checking Commnad executed successfully or not. = " + bolRsltFlag);

        if(!bolRsltFlag)
        {
          totalRows.append(0);
          defaultSorting.append("0");
          arrResult = new String[2][1];
          arrResult[0][0] = FAIL_QUERY;
          arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
          objResult[0] = arrResult;
          objResult[1] = arrResult;
          downBreadCrumb.setBreadCrumbData(arrResult);
          downBreadCrumb.setBreadCrumbDataFirst(arrResult);
          return objResult;
        }
        else
          totalRows.append(vecCmdOutput.size() - 1);

        arrResult = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "-");
        objResult[0] = arrResult;
        downBreadCrumb.setBreadCrumbData(arrResult);

        /**strCmdArgs = " --testrun " + testRun + " " + cmdArg + limitOffset;

        Log.debugLog(className, "execute_get_url_data", "", "", "Command Argument = " + strCmdArgs);

        vecCmdOutput = new Vector();
        bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

        Log.debugLog(className, "execute_get_url_data", "", "", "Checking Commnad executed successfully or not. = " + bolRsltFlag);

        if(!bolRsltFlag)
        {
          totalRows.append(0);
          defaultSorting.append("0");
          arrResult = new String[2][1];
          arrResult[0][0] = FAIL_QUERY;
          arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
          downBreadCrumb.setBreadCrumbDataFirst(arrResult);
          objResult[0] = downBreadCrumb.getBreadCrumbData();
          objResult[1] = downBreadCrumb.getBreadCrumbData();
          return objResult;
        }
        arrResult = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "-");**/
        String[][] arrLimitData = getDataUptoLimit(arrResult, Integer.parseInt(currLimit), Integer.parseInt(currOffset));
        objResult[1] = arrLimitData;
        downBreadCrumb.setBreadCrumbDataFirst(arrLimitData);
      }
      else
      {
        //arrResult = downBreadCrumb.getBreadCrumbDataFirst();
        //downBreadCrumb.setBreadCrumbDataFirst(arrResult);
        totalRows.append(downBreadCrumb.getTotalRecord());

        arrResult = downBreadCrumb.getBreadCrumbData();
        //sortColumnType.append(downBreadCrumb.getSortColumnType());
        //defaultSorting.append(downBreadCrumb.getDefaultSortIndicator());
        downBreadCrumb.setBreadCrumbData(arrResult);
        objResult[0] = arrResult;
        arrResult = getDataUptoLimit(arrResult, Integer.parseInt(currLimit), Integer.parseInt(currOffset));
        objResult[1] = arrResult;
      }

      String arrSortColumnType[] = new String[arrResult[0].length];

      //Default fill sorting Type String
      Arrays.fill(arrSortColumnType, "String");

      defaultSorting.append("0");

      for(int i = 0; i < arrResult[0].length; i++)
      {
        //setting column type index and if flag false skip column to sorting
        boolean bolFlag = setColumnTypeIndex(arrResult[0][i].toString().trim(), i);

        if(mapSort.containsKey(arrResult[0][i].toString().trim()) && bolFlag)
        {
          arrSortColumnType[i] = "," + mapSort.get(arrResult[0][i].toString().trim()).toString();
        }
      }

      String sortColumn = rptUtilsBean.strArrayToStr(arrSortColumnType, ",");

      Log.debugLog(className, "execute_get_url_data", "", "", "Column type for sorting = " + sortColumnType);

      sortColumnType.append(sortColumn);

      return objResult;
    }

    catch (Exception e)
    {
      e.printStackTrace();
      totalRows.append("0");
      defaultSorting.append("0");
      arrResult = new String[2][1];
      arrResult[0][0] = FAIL_QUERY;
      arrResult[1][0] = "No records are found";
      objResult[0] = arrResult;
      objResult[1] = arrResult;
      Log.stackTraceLog(className, "execute_get_url_data", "", "", "Exception - ", e);
      return objResult;
    }
  }

  //nsi_db_get_obj_Failure_data
  public String[][] execute_nsi_db_get_failure_data(String cmdArg, StringBuffer sortColumnType, StringBuffer defaultSorting, DrillDownBreadCrumb breadCrumb)
  {
    Log.debugLog(className, "execute_nsi_db_get_failure_data", "", "", "Method called. = " + cmdArg);
    String arrResult[][] = null;
    try
    {
      if(breadCrumb.getFilterArument().equals("") || !breadCrumb.getFilterArument().equals(cmdArg))
      {
        strCmdName = NSI_DB_GET_FAILURE_DATA;
        Log.debugLog(className, "execute_nsi_db_get_failure_data", "", "", "Command Name = " + strCmdName);

        strCmdArgs = " --testrun " + testRun + " " + cmdArg ;

        Log.debugLog(className, "execute_nsi_db_get_failure_data", "", "", "Command Argument = " + strCmdArgs);

        vecCmdOutput = new Vector();
        boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);
        Log.debugLog(className, "execute_nsi_db_get_failure_data", "", "", "Checking Commnad executed successfully or not. = " + bolRsltFlag);

        if(!bolRsltFlag)
        {
	  defaultSorting.append("0");
          arrResult = new String[2][1];
          arrResult[0][0] = FAIL_QUERY;
          arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
          breadCrumb.setBreadCrumbData(arrResult);
          return arrResult;
        }

        arrResult = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "-");
        defaultSorting.append("0");
        breadCrumb.setBreadCrumbData(arrResult);
      }
      else
      {
        arrResult = breadCrumb.getBreadCrumbData();
        breadCrumb.setBreadCrumbData(arrResult);
      }
      return arrResult;
    }
    catch (Exception e)
    {
      defaultSorting.append("0");
      e.printStackTrace();
      arrResult = new String[2][1];
      arrResult[0][0] = FAIL_QUERY;
      arrResult[1][0] = "No records are found";
      Log.stackTraceLog(className, "execute_nsi_db_get_failure_data", "", "", "Exception - ", e);
      return arrResult;
    }
  }


  //nsi_db_get_url_comp_data
  public String [][] execute_get_url_comp_data(String cmdArg, StringBuffer sortColumnType, DrillDownBreadCrumb downBreadCrumb)
  {
    Log.debugLog(className, "execute_get_url_comp_data", "", "", "Method called. = " + cmdArg);
    String arrResult[][] = null;

    try
    {
      if(downBreadCrumb.getFilterArument().equals("") || !downBreadCrumb.getFilterArument().equals(cmdArg))
      {
        strCmdName = NSI_DB_GET_URL_COMP_DATA;
        Log.debugLog(className, "execute_get_url_comp_data", "", "", "Command Name = " + strCmdName);

        strCmdArgs = " --testrun " + testRun + " " + cmdArg;

        Log.debugLog(className, "execute_get_url_comp_data", "", "", "Command Argument = " + strCmdArgs);

        vecCmdOutput = new Vector();
        boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

        Log.debugLog(className, "execute_get_url_comp_data", "", "", "Checking Commnad executed successfully or not. = " + bolRsltFlag);

        if(!bolRsltFlag)
        {
          arrResult = new String[2][1];
          arrResult[0][0] = FAIL_QUERY;
          arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
          downBreadCrumb.setBreadCrumbData(arrResult);
          return arrResult;
        }

        // arrResult = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "-");
        arrResult = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "-", 0, 1, 0, 0);
        downBreadCrumb.setBreadCrumbData(arrResult);
      }
      else
      {
        arrResult = downBreadCrumb.getBreadCrumbData();
        downBreadCrumb.setBreadCrumbData(arrResult);
      }

     // Swap row and column of 2D array as query returns data as rows (not columns)
     arrResult = rptUtilsBean.swapRowCol(arrResult);
     // Set value for table header
     arrResult[0][0] = "Component";
     arrResult[0][1] = "Avg Response Time";
     arrResult[0][2] = "Percentage Response Time";

      String arrSortColumnType[] = new String[arrResult[0].length];

      //Default fill sorting Type String
      Arrays.fill(arrSortColumnType, "String");

      // defaultSorting.append("0");

      //System.out.println("Column = " + arrResult[0].length + ", Rows = " + arrResult.length);
      for(int i = 0; i < arrResult[0].length; i++)
      {
        //System.out.println("HHHHHHH " + arrResult[0][i].toString().trim());
        //setting column type index and if flag false skip column to sorting
        boolean bolFlag = setColumnTypeIndex(arrResult[0][i].toString().trim(), i);

        if(mapSort.containsKey(arrResult[0][i].toString().trim()) && bolFlag)
        {
          arrSortColumnType[i] = "," + mapSort.get(arrResult[0][i].toString().trim()).toString();
        }
      }

      String sortColumn = rptUtilsBean.strArrayToStr(arrSortColumnType, ",");

      Log.debugLog(className, "execute_get_url_comp_data", "", "", "Column type for sorting = " + sortColumnType);

      sortColumnType.append(sortColumn);

      return arrResult;
    }

    catch (Exception e)
    {
      arrResult = new String[2][1];
      arrResult[0][0] = FAIL_QUERY;
      arrResult[1][0] = "No records are found";
      Log.stackTraceLog(className, "execute_get_url_comp_data", "", "", "Exception - ", e);
      return arrResult;
    }
  }


  //nsi_db_get_url_comp_data
  public String [][] execute_get_pg_comp_data(String cmdArg, StringBuffer sortColumnType,StringBuffer defaultSorting, DrillDownBreadCrumb breadCrumb, int prevTotalTried)
  {
    Log.debugLog(className, "execute_get_pg_comp_data", "", "", "Method called. = " + cmdArg);
    String arrResult[][] = null;

    try
    {
      //System.out.println("From canche =|" + breadCrumb.getFilterArument() + "|new= |" + cmdArg.trim());
      if(breadCrumb.getFilterArument().trim().equals("") || !breadCrumb.getFilterArument().trim().equals(cmdArg.trim()))
      {
        strCmdName = NSI_DB_GET_PG_COMP_DATA;
        Log.debugLog(className, "execute_get_pg_comp_data", "", "", "Command Name = " + strCmdName);

        strCmdArgs = " --testrun " + testRun + " " + cmdArg;

        Log.debugLog(className, "execute_get_pg_comp_data", "", "", "Command Argument = " + strCmdArgs);

        vecCmdOutput = new Vector();
        boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

        Log.debugLog(className, "execute_get_pg_comp_data", "", "", "Checking Commnad executed successfully or not. = " + bolRsltFlag);

        if(!bolRsltFlag)
        {
          defaultSorting.append("0");
          arrResult = new String[2][1];
          arrResult[0][0] = FAIL_QUERY;
          arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
          breadCrumb.setBreadCrumbData(arrResult);
          return arrResult;
        }

        // Convert records into 2D Fields array.
        // Pass extra argument to add 1 row at bottom for 'Embedded Objects'
        // and 1 column at end for 'Percentage of Page Response Time'
        arrResult  = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "-", 0, 0, 0, 1);
        breadCrumb.setBreadCrumbData(arrResult);
      }
      else
      {
        arrResult = breadCrumb.getBreadCrumbData();
        breadCrumb.setBreadCrumbData(arrResult);
      }

      String[][] arrResultAddCol = new String[arrResult.length][5];
      // Swap row and column of 2D array as query returns data as rows (not columns)
     // arrResult = rptUtilsBean.swapRowCol(arrResult);

      // Set value for table header
      arrResultAddCol[0][0] = arrResult[0][1]; //Component
      arrResultAddCol[0][1] = arrResult[0][2];//Actual Average Time
      arrResultAddCol[0][2] = "Percentage of Page Response Time";
      arrResultAddCol[0][3] = arrResult[0][0];//Count
      arrResultAddCol[0][4] = arrResult[0][2];//Actual Average Time Coming from Query

      int embeddedAvgRespTime = 0;


      //These index store index id index is -1 means this URL type is not coming
      int mainIndex = -1; //Main URL Index
      int redirectIndex = -1; //Redirected URL
      int embedIndex = -1; //Embedded URL
      String arrURLSequence[][] = new String[4][5];
      arrURLSequence[0] = arrResultAddCol[0].clone();

      //Sequence always 1,2,3 b'coz query implicitly doing order by URL Type
      //
      for(int j = 1; j < arrResult.length; j++)
      {
        // Check AvgValue, if negative place 0
        if(arrResult[j].length > 1 && Integer.parseInt(arrResult[j][2]) < 0)
          arrResultAddCol[j][2] = "0";

        //calculating Total time
        //Count[0] Component[1] Avg Time[2]  ---- Total Time  Avg
        //Calculate average count of page * Average time coming from query
        arrResultAddCol[j][1] = "" + (int)Math.round((Double.parseDouble(arrResult[j][0]) * Double.parseDouble(arrResult[j][2]))/ prevTotalTried);

        //For Main
        if(arrResult[j][1].equals("1"))
        {
          mainIndex = j;
          //Sum resp time to calculate Embedded time
          //adding Average resp time coming from query
          embeddedAvgRespTime = embeddedAvgRespTime + Integer.parseInt(arrResult[j][2]);

          //Now summing calculated resp time
          //embeddedAvgRespTime = embeddedAvgRespTime + Integer.parseInt(arrResultAddCol[j][1]);
          arrResultAddCol[j][0] = "Main URL";
        }
        else if(arrResult[j][1].equals("3"))
        {
          redirectIndex = j;
          //Sum resp time to calculate Embedded time
          //adding Average resp time coming from query
          embeddedAvgRespTime = embeddedAvgRespTime + Integer.parseInt(arrResult[j][2]);

          //Now summing calculated resp time
          //embeddedAvgRespTime = embeddedAvgRespTime + Integer.parseInt(arrResultAddCol[j][1]);
          arrResultAddCol[j][0] = "Main Redirect URL(s)";
        }
        else
        {
          embedIndex = j;
          arrResultAddCol[j][0] = "Embedded URL(s)";
        }

        arrResultAddCol[j][3] = arrResult[j][0]; //count
        arrResultAddCol[j][4] = arrResult[j][2]; //Actual Average time
      }

      //Here we are maintaining sequence to show in the GUI
      //Main --> Redirect --> Embedded
      for(int j = 1; j < arrResult.length; j++)
      {
        if(mainIndex == j)
          arrURLSequence[1] = arrResultAddCol[mainIndex].clone();
        if(redirectIndex == j)
          arrURLSequence[2] = arrResultAddCol[redirectIndex].clone();
        if(embedIndex == j)
        {
          arrURLSequence[3] = arrResultAddCol[embedIndex].clone();
          //Here we assign sum of Main and redirect response time
          //In JSp we are subtracting with average time of Page

          //calculating Total time
          //Count[0] Component[1] Avg Time[2]  ---- Total Time  Avg
          //Calculate average count of page * Average time coming from query
          //arrURLSequence[3][1] = "" + ((Integer.parseInt(arrResultAddCol[embedIndex][3]) * embeddedAvgRespTime)/prevTotalTried);
          //arrURLSequence[3][4] =  embeddedAvgRespTime + ""; //Actual Average time comming from query
        }
      }

      int index = 1;
      //If zeroth index is null means this URL type is not present
      for(int i = 1; i < arrURLSequence.length; i++)
      {
        if(arrURLSequence[i][0] != null)
        {
          arrResultAddCol[index] = arrURLSequence[i].clone();
          index++;
        }
      }

      String arrSortColumnType[] = new String[arrResultAddCol[0].length];

      //Default fill sorting Type String
      Arrays.fill(arrSortColumnType, "String");

      defaultSorting.append("0");

      for(int i = 0; i < arrResultAddCol[0].length; i++)
      {
        //setting column type index and if flag false skip column to sorting
        boolean bolFlag = setColumnTypeIndex(arrResultAddCol[0][i].toString().trim(), i);

        if(mapSort.containsKey(arrResultAddCol[0][i].toString().trim()) && bolFlag)
        {
          arrSortColumnType[i] = "," + mapSort.get(arrResultAddCol[0][i].toString().trim()).toString();
        }
      }

      String sortColumn = rptUtilsBean.strArrayToStr(arrSortColumnType, ",");

      Log.debugLog(className, "execute_get_pg_comp_data", "", "", "Column type for sorting = " + sortColumnType);

      sortColumnType.append(sortColumn);

      return arrResultAddCol;
    }

    catch (Exception e)
    {
      e.printStackTrace();
      defaultSorting.append("0");
      arrResult = new String[2][1];
      arrResult[0][0] = FAIL_QUERY;
      arrResult[1][0] = "No records are found";
      Log.stackTraceLog(className, "execute_get_url_comp_data", "", "", "Exception - ", e);
      return arrResult;
    }
  }

  /*
   * This method execute object query
   *
   * Arguments
   * cmdArg: arguments for query
   * sortColumnType: column type sorting (String, Number, Date)
   * totalCount: give no. of records
   *
   */
   public String[][] execute_nsi_db_get_user_session_data(String cmdArg, StringBuffer sortColumnType, StringBuffer defaultSorting, StringBuffer totalRows, String currlimit, String currOffset, DrillDownBreadCrumb breadCrumb)
   {
     Log.debugLog(className, "execute_nsi_db_get_user_session_data", "", "", "Method called. = " + cmdArg);
     String arrResult[][] = null;
     String strFldList = "";

     try
     {
       long startTimeStamp =  System.currentTimeMillis();
       if(breadCrumb.getFilterArument().trim().equals("") || (!breadCrumb.getOffset().equals(currOffset) || !breadCrumb.getLimit().equals(currlimit) || !breadCrumb.getFilterArument().trim().equals(cmdArg.trim())))
       {
         String limitOffSet = " --limit " + currlimit + " --offset " + currOffset;
         strCmdName = NSI_DB_GET_USER_SESSION_DATA;
         Log.debugLog(className, "execute_nsi_db_get_user_session_data", "", "", "Command Name = " + strCmdName);

         vecCmdOutput = new Vector();

         //creating inner class object up to 2 length to get count and data
         getCommandOutputObj = new getCommandOutput[2];
         Thread[] threadArr = new Thread[2];

         for(int i = 0; i < getCommandOutputObj.length; i++)
         {
           //this boolean will give all thread started or not default false
           if(i == getCommandOutputObj.length - 1)
             isAliveAll = true;

           //command with limit offset to get data
           if(i == 0)
             strCmdArgs = " --testrun " + testRun + " " + cmdArg + " " + GET_COUNT_OPTION + " " + DEFAULT_VALUE_GET_COUNT;
           else
             strCmdArgs = " --testrun " + testRun + " " + cmdArg + limitOffSet;

           Log.debugLog(className, "execute_nsi_db_get_user_session_data", "", "", "Command Argument = " + strCmdArgs);

           //creating class object array
           getCommandOutputObj[i] = new getCommandOutput(i, strCmdName, strCmdArgs, this);

           //if thread is enable
           if(IS_EXECUTE_QUERY_IN_THREAD_ENABLE.equals("1"))
           {
             threadArr[i] = new Thread(getCommandOutputObj[i], "Query - " + i);

             //adding uncaughtException Handler
             setUncaughtExceptionHandlerDDR(threadArr[i]);

             //Starting thread
             threadArr[i].start();
           }
           else //if threading is not enable
             getCommandOutputObj[i].getResultByCmdToGetOutput();
         }

         //wait untill all thread started
         //if(!isAliveAll && IS_EXECUTE_QUERY_IN_THREAD_ENABLE.equals("1"))
          // wait();

         // notify all thread
         //checking thread is running or not
         //All thread are not alive it stops all thread
         waitForQueryRunThreads(threadArr);

         long endTimeStamp =  System.currentTimeMillis();

         Log.debugLog(className, "execute_nsi_db_get_user_session_data", "", "", "Total Time taken = " + (endTimeStamp - startTimeStamp));

         //boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

         vecCmdOutput = getCommandOutputObj[0].getQueryOutput();

         if(!getCommandOutputObj[0].getQueryStatus())
         {
           totalRows.append(0);
           defaultSorting.append("0");
           arrResult = new String[2][1];
           arrResult[0][0] = FAIL_QUERY;
           arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
           breadCrumb.setBreadCrumbData(arrResult);
           return arrResult;
         }
         else
         {
           totalRows.append(vecCmdOutput.get(1).toString().trim());
         }

         //strCmdArgs = " --testrun " + testRun + " " + cmdArg + limitOffSet;

         //Log.debugLog(className, "execute_nsi_db_get_user_session_data", "", "", "Command Argument = " + strCmdArgs);

         vecCmdOutput = new Vector();
         //bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

         //Log.debugLog(className, "execute_nsi_db_get_user_session_data", "", "", "Checking Commnad executed successfully or not. = " + bolRsltFlag);


         vecCmdOutput = getCommandOutputObj[1].getQueryOutput();

         if(!getCommandOutputObj[1].getQueryStatus())
         {
           arrResult = new String[1][1];
           arrResult[0][0] = getDataInStringBuff(vecCmdOutput).toString();
           breadCrumb.setBreadCrumbData(arrResult);
           return arrResult;
         }

         arrResult = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "-");
         breadCrumb.setBreadCrumbData(arrResult);
       }
       else
       {
         arrResult = breadCrumb.getBreadCrumbData();
         totalRows.append(breadCrumb.getTotalRecord());
         breadCrumb.setBreadCrumbData(arrResult);
       }
       String arrSortColumnType[] = new String[arrResult[0].length];

       //Default fill sorting Type Number
       Arrays.fill(arrSortColumnType, "String");

       //Default Sorting would be on Start Time

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

         if(arrResult[0][i].toString().trim().equals("Start Time"))
           defaultSorting.append("" + i);
       }

       if(defaultSorting.toString().equals(""))
         defaultSorting.append("0");


       String sortColumn = rptUtilsBean.strArrayToStr(arrSortColumnType, ",");
       arrSortColumnType = rptUtilsBean.strToArrayData(sortColumn, ",");
       sortColumn = rptUtilsBean.strArrayToStr(arrSortColumnType, ",");

       Log.debugLog(className, "execute_nsi_db_get_user_session_data", "", "", "Column type for sorting = " + sortColumnType);

       sortColumnType.append(sortColumn);
       return arrResult;
    }
    catch(Exception e)
    {
      totalRows.append(0);
      arrResult = new String[2][1];
      arrResult[0][0] = FAIL_QUERY;
      arrResult[1][0] = "No records are found";
      Log.stackTraceLog(className, "execute_nsi_db_get_user_session_data", "", "", "Exception - ", e);
      return arrResult;
    }
  }

  public String [][] execute_get_think_time_data(String cmdArg, DrillDownBreadCrumb breadCrumb)
  {
    Log.debugLog(className, "execute_get_think_time_data", "", "", "Method called. = " + cmdArg);
    String arrResult[][] = null;

    try
    {
      if(breadCrumb.getFilterArumentFirst().equals("") || !breadCrumb.getFilterArumentFirst().equals(cmdArg))
      {
        strCmdName = NSI_DB_GET_THINK_TIME_DATA;
        Log.debugLog(className, "execute_get_think_time_data", "", "", "Command Name = " + strCmdName);

        strCmdArgs = " --testrun " + testRun + " " + cmdArg;

        Log.debugLog(className, "execute_get_think_time_data", "", "", "Command Argument = " + strCmdArgs);

        vecCmdOutput = new Vector();
        boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

        Log.debugLog(className, "execute_get_think_time_data", "", "", "Checking Commnad executed successfully or not. = " + bolRsltFlag);

        if(!bolRsltFlag)
        {
          arrResult = new String[2][1];
          arrResult[0][0] = FAIL_QUERY;
          arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
          breadCrumb.setBreadCrumbDataFirst(arrResult);
          return arrResult;
        }

        // Convert records into 2D Fields array.
        // Pass extra argument to add 1 row at bottom
        arrResult  = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "0", 0, 0, 0, 1);
        breadCrumb.setBreadCrumbDataFirst(arrResult);
      }
      else
      {
        arrResult = breadCrumb.getBreadCrumbDataFirst();
        breadCrumb.setBreadCrumbDataFirst(arrResult);
      }
      return arrResult;
    }

    catch (Exception e)
    {
      e.printStackTrace();
      arrResult = new String[2][1];
      arrResult[0][0] = FAIL_QUERY;
      arrResult[1][0] = "No records are found";
      breadCrumb.setBreadCrumbDataFirst(arrResult);
      Log.stackTraceLog(className, "execute_get_think_time_data", "", "", "Exception - ", e);
      return arrResult;
    }
  }

   //nsi_db_get_tx_sess_data
   public String [][] execute_get_tx_sess_data(String cmdArg, StringBuffer sortColumnType,StringBuffer defaultSorting, DrillDownBreadCrumb breadCrumb)
   {
     double sumOfPWT = 0.0; // For calculate Sum of PWT
     Log.debugLog(className, "execute_get_tx_sess_data", "", "", "Method called. = " + cmdArg);
     String arrResult[][] = null;

     try
     {
       if(breadCrumb.getFilterArument().equals("") || !breadCrumb.getFilterArument().equals(cmdArg))
       {
         strCmdName = NSI_DB_GET_TX_SESS_DATA;
         Log.debugLog(className, "execute_get_tx_sess_data", "", "", "Command Name = " + strCmdName);

         strCmdArgs = " --testrun " + testRun + " " + cmdArg;

         Log.debugLog(className, "execute_get_tx_sess_data", "", "", "Command Argument = " + strCmdArgs);

         vecCmdOutput = new Vector();
         boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

         Log.debugLog(className, "execute_get_tx_sess_data", "", "", "Checking Commnad executed successfully or not. = " + bolRsltFlag);

         if(!bolRsltFlag)
         {
           defaultSorting.append("0");
           arrResult = new String[2][1];
           arrResult[0][0] = FAIL_QUERY;
           arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
           breadCrumb.setBreadCrumbData(arrResult);
           return arrResult;
         }

         // Convert records into 2D Fields array.
         // Add 2 Extra column For Page Weigthed Time and Percentage Time
         arrResult  = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "-", 0, 0, 0, 2);
         breadCrumb.setBreadCrumbData(arrResult);
      }
      else
      {
        arrResult = breadCrumb.getBreadCrumbData();
        breadCrumb.setBreadCrumbData(arrResult);
      }

     // Set value for table header

       arrResult[0][4] = "Page Weighted Time";
       arrResult[0][5] = "Page Weighted Time Percentage";

       String arrSortColumnType[] = new String[arrResult[0].length];

       //Default fill sorting Type String
       Arrays.fill(arrSortColumnType, "String");

       defaultSorting.append("0");

       for(int i = 1; i < arrResult[0].length; i++)
       {
         //setting column type index and if flag false skip column to sorting
         boolean bolFlag = setColumnTypeIndex(arrResult[0][i].toString().trim(), i);

         if(mapSort.containsKey(arrResult[0][i].toString().trim()) && bolFlag)
         {
           arrSortColumnType[i] = "," + mapSort.get(arrResult[0][i].toString().trim()).toString();
         }
       }

       String sortColumn = rptUtilsBean.strArrayToStr(arrSortColumnType, ",");

       Log.debugLog(className, "execute_get_tx_sess_data", "", "", "Column type for sorting = " + sortColumnType);

       sortColumnType.append(sortColumn);

       return arrResult;
     }

     catch (Exception e)
     {
       defaultSorting.append("0");
       arrResult = new String[2][1];
       arrResult[0][0] = FAIL_QUERY;
       arrResult[1][0] = "No records are found";
       Log.stackTraceLog(className, "execute_get_tx_sess_data", "", "", "Exception - ", e);
       return arrResult;
     }
   }

/*
 * This method add the args and remove args
 * String - filter string
 * ArrayList - add in the filter format (--fields 5095)
 * ArrayList - remove in the filter format (--fields 5095)
 */
  public String removeAddArgs(String strFilters, ArrayList addArgs, ArrayList removeArgs)
  {
    Log.debugLog(className, "removeAddArgs", "", "", "Method called. Filter String= " + strFilters + ", Add arguments arrAddArgs = " + addArgs.toString() + ", Remove arguments arrRemoveArgs = " + removeArgs.toString());
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

        if((innerAddToken.length <= 1) || (innerAddToken.length > 1 && (innerAddToken[1].trim().equals("NA"))))
          continue;

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
      Log.stackTraceLog(className, "removeAddArgs", "", "", "", e);
      e.printStackTrace();
      return "";
    }
  }

  /**
   * Getting Filters Information which is applied on current page query to get resultant data, and show in Gui.
   * @param strFilter
   * @param otherFilter
   * @return
   */

  public String showFilterCriteria(String strFilter, String otherFilter)
  {
    Log.debugLog(className, "showFilterCriteria", "", "", "Method called. Filter String= " + strFilter + ", additional filter = " + otherFilter);

    try
    {
      String[] arrSplitFilter = rptUtilsBean.split(strFilter, " ");
      String strFilterCritera = "";
      int count = 0;

      boolean commaFlag = false;

      for(int i = 0; i < arrSplitFilter.length - 1; i++)
      {
        String strTemp = arrSplitFilter[i].trim();
        String strNextTemp = arrSplitFilter[i + 1].trim();

        if(!strFilterCritera.trim().equals("") && commaFlag)
          strFilterCritera = strFilterCritera + ", ";
        commaFlag = false;
        //Error code
        String ObjectLabel = "URL";
        if (Integer.parseInt(drillDownReportData.getObjectType()) <= 3 || Integer.parseInt(drillDownReportData.getObjectType()) == 8)
          ObjectLabel = drillDownReportData.getObjectLabel();

        if(strTemp.equals(drillDownReportData.STATUS_OPTION))
        {
          if(strNextTemp.equals("-2"))
            strFilterCritera = ObjectLabel + " Status = All";
          else if(strNextTemp.equals("0"))
            strFilterCritera = ObjectLabel + " Status = Success ";
          else if(strNextTemp.equals("-1"))
            strFilterCritera = ObjectLabel + " Status = All Failures ";
          else
          {
            String[][] arrErrorCode = getErrorCodes();
            String[] arrStatusCode = rptUtilsBean.split(strNextTemp, ",");
            String errorTemp = "";
            for(int k = 0; k < arrErrorCode.length; k++)
            {
              for(int kk = 0; kk < arrStatusCode.length; kk++)
              {
                if(arrStatusCode[kk].equals(arrErrorCode[k][1].trim()))
                {
                  if(errorTemp.equals(""))
                  {
                    strFilterCritera = ObjectLabel + " Status = " + arrErrorCode[k][2];
                    errorTemp = arrErrorCode[k][2];
                  }
                  else
                    strFilterCritera = strFilterCritera + ", " +  arrErrorCode[k][2];
                }
              }
            }
          }

          commaFlag = true;
        }
        else if(strTemp.equals(drillDownReportData.START_TIME_OPTION))
        {
          if(!drillDownReportData.getNDETimeLabel().trim().equals(""))
            continue;

          strFilterCritera = strFilterCritera + "Start Time = " + rptUtilsBean.convMilliSecToStr(Long.parseLong(strNextTemp));
          commaFlag = true;
        }
        else if(strTemp.equals(drillDownReportData.END_TIME_OPTION))
        {
          if(!drillDownReportData.getNDETimeLabel().trim().equals(""))
            continue;

          strFilterCritera = strFilterCritera + "End Time = " + rptUtilsBean.convMilliSecToStr(Long.parseLong(strNextTemp));
          commaFlag = true;
        }
        else if(strTemp.equals(drillDownReportData.URL_OPTION))
        {
          String trimURl = rptUtilsBean.urlSubString(strNextTemp).trim();

          //if(trimURl.startsWith("'") && (trimURl.lastIndexOf("'") != -1))
            //trimURl = trimURl.substring(1, (trimURl.length() - 1));
          strFilterCritera = strFilterCritera + "URL = " + trimURl;
          commaFlag = true;
        }
        else if(strTemp.equals(drillDownReportData.PAGE_OPTION) && !otherFilter.contains("Page Name"))
        {
          strFilterCritera = strFilterCritera + "Page = " + strNextTemp;
          commaFlag = true;
        }
        else if(strTemp.equals(drillDownReportData.TRANSACTION_OPTION) && !otherFilter.contains("Transaction Name"))
        {
          strFilterCritera = strFilterCritera + "Transaction = " + strNextTemp;
          commaFlag = true;
        }
        else if(strTemp.equals(drillDownReportData.GENERATOR_ID_OPTION) && !otherFilter.contains("Generator Name"))
        {
          strFilterCritera = strFilterCritera + "Generator Name = " + drillDownReportData.getGeneratorFilter();
          commaFlag = true;
        }
        else if(strTemp.equals(drillDownReportData.SCRIPT_OPTION)  && !otherFilter.contains("Script Name"))
        {
          strFilterCritera = strFilterCritera + "Script = " + strNextTemp;
          commaFlag = true;
        }
        else if(strTemp.equals(drillDownReportData.LOCATION_OPTION))
        {
          strFilterCritera = strFilterCritera + "Location = " + strNextTemp;
          commaFlag = true;
        }
        else if(strTemp.equals(drillDownReportData.ACCESS_OPTION))
        {
          strFilterCritera = strFilterCritera + "Access = " + strNextTemp;
          commaFlag = true;
        }
        else if(strTemp.equals(drillDownReportData.BROWSER_OPTION))
        {
          strFilterCritera = strFilterCritera + "Browser = " + strNextTemp;
          commaFlag = true;
        }
        else if(strTemp.equals(drillDownReportData.LOG_SEVERITY_OPTION))
        {
          strFilterCritera = strFilterCritera + "Severity = " + strNextTemp;
          commaFlag = true;
        }
        else if(strTemp.equals(drillDownReportData.QUERY_TYPE_OPTION))
        {
          strFilterCritera = strFilterCritera + "Query Type = " + getQueryTypeValue("QueryName", strNextTemp);
          commaFlag = true;
        }

        else if(strTemp.equals(drillDownReportData.LOG_MATCH_TEXT))
        {
          strFilterCritera = strFilterCritera + "Match Text = " + rptUtilsBean.replace(drillDownReportData.getLogMatchText(), "\"", "");
          commaFlag = true;
        }

        else if(strTemp.equals(drillDownReportData.PHASE_INDEX_OPTION))
        {
          strFilterCritera = strFilterCritera + "Phase Name = " + drillDownReportData.getPhaseName();
          commaFlag = true;
        }

        else if(strTemp.equals(drillDownReportData.SERVER_INDEX_OPTION))
        {
          if(!drillDownReportData.getServerName().trim().equals("") && !otherFilter.contains("Server Name"))
          {
            strFilterCritera = strFilterCritera + "Server Name = " + drillDownReportData.getServerName();
            commaFlag = true;
          }
        }

        else if(strTemp.equals(drillDownReportData.TIER_INDEX_OPTION) && !otherFilter.contains("Tier Name"))
        {
          if(!drillDownReportData.getTierName().trim().equals(""))
          {
            strFilterCritera = strFilterCritera + "Tier Name = " + drillDownReportData.getTierName();
            commaFlag = true;
          }
        }

        else if(strTemp.equals(drillDownReportData.APP_INDEX_OPTION) && !otherFilter.contains("App Name"))
        {
          if(!drillDownReportData.getAppName().trim().equals(""))
          {
            strFilterCritera = strFilterCritera + "App Name = " + drillDownReportData.getAppName();
            commaFlag = true;
          }
        }

        else if(strTemp.equals(drillDownReportData.SVC_INTANCE_ID_OPTION))
        {
          if(!drillDownReportData.getInstanceName().trim().equals(""))
          {
            strFilterCritera = strFilterCritera + "Service Instance = " + drillDownReportData.getInstanceName();
            commaFlag = true;
          }
        }

        else if(strTemp.equals(drillDownReportData.FLOWPATH_MIN_METHOD_OPTION))
        {
          if(!drillDownReportData.getMinMethods().trim().equals(""))
          {
            strFilterCritera = strFilterCritera + "Min Methods Count = " + drillDownReportData.getMinMethods();
            commaFlag = true;
          }
        }

        else if(strTemp.equals(drillDownReportData.FLOWPATH_MIN_EXCEPTION_OPTION))
        {
          if(!drillDownReportData.getMinExceptions().trim().equals(""))
          {
            strFilterCritera = strFilterCritera + "Min Exceptions Count = " + drillDownReportData.getMinExceptions();
            commaFlag = true;
          }
        }
        else if(strTemp.equals(drillDownReportData.EXCEPTION_CLASS))
        {
        	if(!drillDownReportData.getExceptionClass().trim().equals(""))
        	{
        		strFilterCritera = strFilterCritera + " Exception Class = " + drillDownReportData.getExceptionClass();
        		commaFlag = true;
        	}
        }

        else if(strTemp.equals(drillDownReportData.EXCEPTION_THROWING_CLASS))
        {
        	if(!drillDownReportData.getExceptionThrowingClass().trim().equals(""))
        	{
        		strFilterCritera = strFilterCritera + " Exception Throwing Class = " + drillDownReportData.getExceptionThrowingClass();
        		commaFlag = true;
        	}
        }

        else if(strTemp.equals(drillDownReportData.EXCEPTION_THROWING_METHOD))
        {
        	if(!drillDownReportData.getExceptionThrowingMethod().trim().equals(""))
        	{
        		strFilterCritera = strFilterCritera + " Exception Throwing Class = " + drillDownReportData.getExceptionThrowingMethod();
        		commaFlag = true;
        	}
        }

        else if(strTemp.equals(drillDownReportData.EXCEPTION_MESSAGE))
        {
        	if(!drillDownReportData.getExceptionMessage().trim().equals(""))
        	{
        		strFilterCritera = strFilterCritera + " Exception Message = " + drillDownReportData.getExceptionMessage();
        		commaFlag = true;
        	}
        }

        else if(strTemp.equals(drillDownReportData.EXCEPTION_CAUSE))
        {
        	if(!drillDownReportData.getExceptionCause().trim().equals(""))
        	{
        		strFilterCritera = strFilterCritera + " Exception Cause = " + drillDownReportData.getExceptionCause();
        		commaFlag = true;
        	}
        }

        else if(strTemp.equals(drillDownReportData.EXCEPTION_STACKTRACE))
        {
        	if(!drillDownReportData.getExceptionStackTrace().trim().equals(""))
        	{
        		strFilterCritera = strFilterCritera + " Exception StackTrace = " + drillDownReportData.getExceptionStackTrace();
        		commaFlag = true;
        	}
        }

        else if(strTemp.equals(drillDownReportData.HTTP_HEADER_OPTION))
        {
            String subTitle = "";
            String headerType = drillDownReportData.getHttpHeader();
            String headerName = drillDownReportData.getHeaderName();
            String operatorName = drillDownReportData.getOperatorName();
            String comparisonValue = drillDownReportData.getComparisionValue();

          if(count == 0 && !headerName.trim().equals(""))
          {
              subTitle  =  "Header Type : "+ headerType + ", HeaderName :" + headerName;

              if(!operatorName.trim().equals(""))
        	  subTitle = subTitle + ", Operator Name :" +operatorName;

              if(!comparisonValue.trim().equals(""))
        	  subTitle = subTitle + ", Comparison Value :" +comparisonValue;
          }
          else
          {
              String[] tempArr = strNextTemp.split(":");
              for(int j = 0;j < tempArr.length;j++)
              {
        	  if(j==0)
                     subTitle = "Header Type : " + tempArr[j] +", HeaderName :" + tempArr[j+1];

                  if(j==2 && !tempArr[j].trim().equals(""))
                      subTitle = subTitle + ", Operator Name : " + tempArr[j];

                  if(j==3 && !tempArr[j].trim().equals(""))
                      subTitle = subTitle + ", Comparison Value : " + tempArr[j];
              }//END OF FOR LOOP

          }//END OF ELSE BLOCK
          strFilterCritera = strFilterCritera + subTitle;
          commaFlag = true;
          count = count + 1;
        }
        else if(strTemp.equals(drillDownReportData.URL_SEARCH_PATTERN))
        {
        	if(!drillDownReportData.getUrlSearchPattern().trim().equals(""))
        	{
        		if(drillDownReportData.getUrlSearchPatternType().trim().equals("0"))
        			strFilterCritera = strFilterCritera + " URL Search Pattern : " + drillDownReportData.getUrlSearchPattern();
        		else if(drillDownReportData.getUrlSearchPatternType().trim().equals("1"))
        			strFilterCritera = strFilterCritera + " URL Search Pattern (Beginning With) : " + drillDownReportData.getUrlSearchPattern();
        		else if(drillDownReportData.getUrlSearchPatternType().trim().equals("2"))
        			strFilterCritera = strFilterCritera + " URL Search Pattern (Ending With) : " + drillDownReportData.getUrlSearchPattern();
        			commaFlag = true;		
        	}
        }        
      }

      if((!strFilterCritera.trim().equals("")) && strFilterCritera.trim().lastIndexOf(",") == strFilterCritera.trim().length() - 1)
        strFilterCritera = strFilterCritera.trim().substring(0, (strFilterCritera.trim().length() -1));

      if(!drillDownReportData.getRespTimeFilter().equals(""))
        strFilterCritera = strFilterCritera + ", " + drillDownReportData.getRespTimeFilter();

      if(!drillDownReportData.getNDETimeLabel().trim().equals(""))
       strFilterCritera = strFilterCritera +  ", " + drillDownReportData.getNDETimeLabel();

      if(!drillDownReportData.getQueryTimeFilter().equals(""))
        strFilterCritera = strFilterCritera + ", " + drillDownReportData.getQueryTimeFilter();

      if(!otherFilter.trim().equals(""))
      {
        if(strFilterCritera.trim().equals(""))
          strFilterCritera = otherFilter;
        else
          strFilterCritera = strFilterCritera + ", " + otherFilter;
      }

      if(strFilterCritera.equals(""))
        strFilterCritera = "No Filter Applied";
      
      //Checking if started with comma then remove comma
      if(strFilterCritera.startsWith(","))
    	  strFilterCritera = strFilterCritera.substring(1);

      Log.debugLog(className, "showFilterCriteria", "", "", "strFilterCritera = " + strFilterCritera);

      return strFilterCritera;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "showFilterCriteria", "", "", "Exception - ", e);
      return drillDownReportData.getFilterTitle();
    }
  }

  /**
   * Getting Index Baased Filter Criteria.
   * @param argName
   * @param strFilterSetting
   * @return
   */

  public String getIndexBasedFilterCriteria(String filterArgs, String filterTitle, boolean isCommaAdded)
  {
	String filterName = "";
	try
	{
      if(!filterArgs.trim().equals("") && !filterArgs.trim().equals("NA"))
      {
    	 if(isCommaAdded)
    	   filterName =", " + filterTitle + " = "+ filterArgs;
    	 else
    	   filterName = filterTitle + " = "+ filterArgs;
      }
	}
	catch(Exception e)
	{
	  Log.stackTraceLog(className, "getIndexBasedFilterCriteria", "", "", "Error in getting index Based Filter", e);
	}
	return filterName;
  }

  public String getArgumentValue(String argName, String strFilterSetting)
  {
    Log.debugLog(className, "getArgumentValue", "", "", "Method called");
    String strNextTemp = "";
    try
    {
      String[] arrSplitFilter =  rptUtilsBean.split(strFilterSetting, " ");
      for(int i = 0; i < arrSplitFilter.length - 1; i++)
      {
        String strTemp = arrSplitFilter[i].trim();
        strNextTemp = arrSplitFilter[i + 1].trim();

        if(strTemp.trim().equals(argName.trim()))
          break;
        else
          strNextTemp = "";
      }

      Log.debugLog(className, "getArgumentValue", "", "", " " + argName + " = " + strNextTemp);
      return strNextTemp;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getArgumentValue", "", "", "Exception - ", e);
      return strNextTemp;
    }
  }

  public String[][] execute_ndi_get_fp_count(String cmdArg ,StringBuffer sortColumnType, StringBuffer defaultSorting ,StringBuffer totalCount, StringBuffer totalQueryCount, StringBuffer totalAvg, String currlimit, String currOffset)
  {
    DrillDownBreadCrumb breadCrumb = new DrillDownBreadCrumb(testRun, drillDownReportData.getReportName(), -1);
    return execute_ndi_get_fp_count(cmdArg, sortColumnType, defaultSorting, totalCount, totalQueryCount, totalAvg, currlimit, currOffset, breadCrumb);
  }

  /**
   * Method to Execute Query for Http Headers
   * @param cmdArg
   * @return
   */
  public String[][] execute_ndi_db_get_http_headers_info(String cmdArg)
  {
      Log.debugLog(className, "execute_ndi_db_get_http_headers_info", "", "", "Method called");
      String arrResult[][] = null;
      try
      {
         strCmdName = NDI_DB_GET_HTTP_HEADERS_INFO;
         strCmdArgs = " " + cmdArg;
         Log.debugLog(className, "execute_ndi_db_get_http_headers_info", "", "","Command Name With Arguments :" + strCmdName + strCmdArgs );
         vecCmdOutput = new Vector();
         boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);
         Log.debugLog(className, "execute_ndi_db_get_http_headers_info", "", "", "Executing Query. Flag After Execution :" + bolRsltFlag);
         if(!bolRsltFlag)
         {
           arrResult = new String[2][1];
           arrResult[0][0] = FAIL_QUERY;
           arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
           return arrResult;
         }

         arrResult = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", " ");
         Log.debugLog(className, "execute_ndi_db_get_http_headers_info", "", "", "Returning Array of Length : " + arrResult.length);
         return arrResult;
      }
      catch(Exception e)
      {
	 Log.stackTraceLog(className, "execute_ndi_db_get_http_headers_info", "", "", "Exception - ", e);
         e.printStackTrace();
         return arrResult;
      }
  }
  
  /**
   * Method to Execute Data for JMS Messages
   * @param cmdArg
   * @return
   */
  public String[][] execute_ndi_db_get_jms_data(String cmdArg)
  {
      Log.debugLog(className, "execute_ndi_db_get_jms_data", "", "", "Method called");
      String arrResult[][] = null;
      try
      {
         strCmdName = NDI_DB_GET_JMS_DATA;
         strCmdArgs = " " + cmdArg;
         Log.debugLog(className, "execute_ndi_db_get_jms_data", "", "","Command Name With Arguments :" + strCmdName + strCmdArgs );
         vecCmdOutput = new Vector();
         boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);
         Log.debugLog(className, "execute_ndi_db_get_jms_data", "", "", "Executing Query. Flag After Execution :" + bolRsltFlag);
         if(!bolRsltFlag)
         {
           arrResult = new String[2][1];
           arrResult[0][0] = FAIL_QUERY;
           arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
           return arrResult;
         }

         arrResult = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", " ");
         Log.debugLog(className, "execute_ndi_db_get_http_headers_info", "", "", "Returning Array of Length : " + arrResult.length);
         return arrResult;
      }
      catch(Exception e)
      {
	 Log.stackTraceLog(className, "execute_ndi_db_get_http_headers_info", "", "", "Exception - ", e);
         e.printStackTrace();
         return arrResult;
      }
  }


  /**
   * This Method is used to get the exception data
   * @param cmdArg
   * @param totalRows
   * @param currLimit
   * @param currOffset
   * @param downBreadCrumb
   * @return
   */
  public String[][] execute_ndi_db_get_exception_data(String cmdArg, StringBuffer totalRows, String currLimit, String currOffset, DrillDownBreadCrumb downBreadCrumb)
  {
    Log.debugLog(className, "execute_ndi_db_get_exception_data", "", "", "Method called. = " + cmdArg);
    String arrResult[][] = null;
    try
    {
      long startTimeStamp =  System.currentTimeMillis();
      if(downBreadCrumb.getFilterArument().equals("") || (!downBreadCrumb.getOffset().equals(currOffset) || !downBreadCrumb.getLimit().equals(currLimit) || !downBreadCrumb.getFilterArument().equals(cmdArg)))
      {
        String limitOffset = " --limit " + currLimit + " --offset " + currOffset;
        strCmdName = NDI_DB_GET_EXCEPTION_DATA;

        Log.debugLog(className, "execute_ndi_db_get_exception_data", "", "", "Command Name = " + strCmdName);

        vecCmdOutput = new Vector();

        //creating inner class object up to 2 length to get count and data
        getCommandOutputObj = new getCommandOutput[2];
        Thread[] threadArr = new Thread[2];

        for(int i = 0; i < getCommandOutputObj.length; i++)
        {
          //this boolean will give all thread started or not default false
          if(i == getCommandOutputObj.length - 1)
            isAliveAll = true;

          //command with limit offset to get data
          if(i == 0)
            strCmdArgs = " --testrun " + testRun + " " + cmdArg + " " + GET_COUNT_OPTION + " " + DEFAULT_VALUE_GET_COUNT;
          else
            strCmdArgs = " --testrun " + testRun + " " + cmdArg + limitOffset;

          Log.debugLog(className, "execute_ndi_db_get_exception_data", "", "", "Command Argument = " + strCmdArgs);

          //creating class object array
          getCommandOutputObj[i] = new getCommandOutput(i, strCmdName, strCmdArgs, this);

          //if thread is enable
          if(IS_EXECUTE_QUERY_IN_THREAD_ENABLE.equals("1"))
          {
            threadArr[i] = new Thread(getCommandOutputObj[i], "Query - " + i);

            //adding uncaughtException Handler
            setUncaughtExceptionHandlerDDR(threadArr[i]);

            //Starting thread
            threadArr[i].start();
          }
          else //if threading is not enable
            getCommandOutputObj[i].getResultByCmdToGetOutput();
        }

        //wait untill all thread started
        //if(!isAliveAll && IS_EXECUTE_QUERY_IN_THREAD_ENABLE.equals("1"))
        //  wait();

        // notify all thread
        //checking thread is running or not
        //All thread are not alive it stops all thread
        waitForQueryRunThreads(threadArr);

        long endTimeStamp =  System.currentTimeMillis();

        Log.debugLog(className, "execute_ndi_db_get_exception_data", "", "", "Total Time taken = " + (endTimeStamp - startTimeStamp));

        vecCmdOutput = getCommandOutputObj[0].getQueryOutput();
        if(!getCommandOutputObj[0].getQueryStatus())
        {
          totalRows.append(0);
          arrResult = new String[2][1];
          arrResult[0][0] = FAIL_QUERY;
          arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
          downBreadCrumb.setBreadCrumbData(arrResult);
          return arrResult;
        }
        else
        {
           totalRows.append(vecCmdOutput.get(1).toString().trim());
           //totalRows.append("100");
        }

        vecCmdOutput = new Vector();
        vecCmdOutput = getCommandOutputObj[1].getQueryOutput();

        if(!getCommandOutputObj[1].getQueryStatus())
        {
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
      return arrResult;
    }
    catch (Exception e)
    {
      totalRows.append(0);
      e.printStackTrace();
      arrResult = new String[2][1];
      arrResult[0][0] = FAIL_QUERY;
      arrResult[1][0] = "No records are found";
      Log.stackTraceLog(className, "execute_ndi_db_get_exception_data", "", "", "Exception - ", e);
      return arrResult;
    }
  }

  /*
   * Method get the no of Flow path for particular URL
   * Arugment :urlIndex
              :StrBuffer for String handling
   */
  public String[][] execute_ndi_get_fp_count(String cmdArg ,StringBuffer sortColumnType, StringBuffer defaultSorting ,StringBuffer totalRows, StringBuffer totalQueryCount, StringBuffer totalAvg, String currlimit, String currOffset, DrillDownBreadCrumb downBreadCrumb)
  {
    Log.debugLog(className, "execute_ndi_get_fp_count", "", "", "Method called");

    String arrResult[][] = null;

    try
    {
      if(downBreadCrumb.getFilterArument().equals("") || (!downBreadCrumb.getOffset().equals(currOffset) || !downBreadCrumb.getLimit().equals(currlimit) || !downBreadCrumb.getFilterArument().trim().equals(cmdArg.trim())))
      {

	long startTimeStamp =  System.currentTimeMillis();
        strCmdName = NDI_GET_FP_COUNT;
        String limitOffSet = " --limit " + currlimit + " --offset " + currOffset;
        Log.debugLog(className, "execute_ndi_get_fp_count", "", "", "Command Name = " + strCmdName);
        strCmdArgs = "--testrun " + testRun + " " + cmdArg;

        Log.debugLog(className, "execute_ndi_get_fp_count", "", "", "Command Argument = " + cmdArg);

        vecCmdOutput = new Vector();

        getCommandOutputObj = new getCommandOutput[2];
        Thread[] threadArr = new Thread[2];

        //execution of thread for count.
        for(int i = 0; i < getCommandOutputObj.length; i++)
        {
          //this boolean will give all thread started or not default false
          if(i == getCommandOutputObj.length - 1)
            isAliveAll = true;

          //command with limit offset to get data
          if(i == 0)
            strCmdArgs = " --testrun " + testRun + " " + cmdArg + " " + GET_COUNT_OPTION + " " + DEFAULT_VALUE_GET_COUNT;
          else
            strCmdArgs = " --testrun " + testRun + " " + cmdArg + limitOffSet;

          Log.debugLog(className, "execute_get_app_logs", "", "", "Command Argument = " + strCmdArgs);
          //creating class object array
          getCommandOutputObj[i] = new getCommandOutput(i, strCmdName, strCmdArgs, this);

          //if thread is enable
          if(IS_EXECUTE_QUERY_IN_THREAD_ENABLE.equals("1"))
          {
            threadArr[i] = new Thread(getCommandOutputObj[i], "Query - " + i);

            //adding uncaughtException Handler
            setUncaughtExceptionHandlerDDR(threadArr[i]);

            //Starting thread
            threadArr[i].start();
          }
          else //if threading is not enable
            getCommandOutputObj[i].getResultByCmdToGetOutput();
        }

        //wait until all thread started
        //if(!isAliveAll && IS_EXECUTE_QUERY_IN_THREAD_ENABLE.equals("1"))
        //  wait();

        // notify all thread
        //checking thread is running or not
        //All thread are not alive it stops all thread
        waitForQueryRunThreads(threadArr);

        long endTimeStamp =  System.currentTimeMillis();

        Log.debugLog(className, "execute_ndi_db_get_query_data", "", "", "Total Time taken = " + (endTimeStamp - startTimeStamp));

        vecCmdOutput = getCommandOutputObj[0].getQueryOutput();
        if(!getCommandOutputObj[0].getQueryStatus())
        {
          totalRows.append(0);
          totalQueryCount.append(0);
          totalAvg.append(0);
          defaultSorting.append("0");
          arrResult = new String[2][1];
          arrResult[0][0] = FAIL_QUERY;
          arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
          downBreadCrumb.setBreadCrumbData(arrResult);
          return arrResult;
        }
        else
        {
          //totalRows.append(vecCmdOutput.get(1).toString().trim());
          String arrCounts[] = rptUtilsBean.split(vecCmdOutput.get(1).toString().trim(), "|");
          totalRows.append(arrCounts[0]);
          if(arrCounts.length > 2)
          {
            totalQueryCount.append(arrCounts[1]);
            totalAvg.append(arrCounts[2]);
            //totalRows.append(10);
          }
        }

        vecCmdOutput = getCommandOutputObj[1].getQueryOutput();

        if(!getCommandOutputObj[1].getQueryStatus())
        {
          totalRows.append(0);
          totalQueryCount.append(0);
          totalAvg.append(0);
          defaultSorting.append("0");
          arrResult = new String[2][1];
          arrResult[0][0] = FAIL_QUERY;
          arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
          downBreadCrumb.setBreadCrumbData(arrResult);
          return arrResult;
        }
        arrResult = getVectorDataInArray(vecCmdOutput);
        downBreadCrumb.setBreadCrumbData(arrResult);
      }
      else
      {
        arrResult = downBreadCrumb.getBreadCrumbData();
        totalRows.append(downBreadCrumb.getTotalRecord());
        totalQueryCount.append(downBreadCrumb.getTotalRecordSum());
        totalAvg.append(downBreadCrumb.getTotalAvg());
        downBreadCrumb.setBreadCrumbData(arrResult);
      }

      String arrHeader[][] = new String[arrResult.length][6];
      //FPSignature|FPCount|Min|Max|Avg|Variance
      //268758402|156|61|114|74|40

      //Set value for table header
      arrHeader[0][0] = "FlowPath Signature";
      arrHeader[0][1] = "FlowPath Count";
      arrHeader[0][2] = "Minimum (ms)";
      arrHeader[0][3] = "Maximum (ms)";
      arrHeader[0][4] = "Average";
      arrHeader[0][5] = "VMR";

      String arrSortColumnType[] = new String[arrResult[0].length];

      //Default fill sorting Type String
      Arrays.fill(arrSortColumnType, "String");

      defaultSorting.append("4");

      for(int i = 0; i < arrResult[0].length; i++)
      {
        //setting column type index and if flag false skip column to sorting
        boolean bolFlag = setColumnTypeIndex(arrResult[0][i].toString().trim(), i);

        if(mapSort.containsKey(arrResult[0][i].toString().trim()) && bolFlag)
        {
          arrSortColumnType[i] = "," + mapSort.get(arrResult[0][i].toString().trim()).toString();
        }
      }

      String arrSortResult[][] = new String[0][0];
      //Calculate aggregate average time (ms)
      if(arrResult.length > 1)
      {
        arrSortResult = new String[arrResult.length - 1][6];
        for(int q = 1, qq = 0; q < arrResult.length; q++, qq++)
        {
          arrSortResult[qq][0] = arrResult[q][0];
          arrSortResult[qq][1] = arrResult[q][1];
          arrSortResult[qq][2] = arrResult[q][2];
          arrSortResult[qq][3] = arrResult[q][3];
          arrSortResult[qq][4] = arrResult[q][4];
          //arrSortResult[qq][5] = arrResult[q][5];
          //arrSortResult[qq][2] = rptUtilsBean.convTo3DigitDecimal(((Double.parseDouble(arrResult[q][2]))/Double.parseDouble(arrResult[q][1])));
          if(Integer.parseInt(arrResult[q][4]) == 0 || arrResult[q][5] == null || arrResult[q][5] == "" )
            arrSortResult[qq][5] = "0";
          else
            arrSortResult[qq][5] = rptUtilsBean.convertTodecimal(((Double.parseDouble(arrResult[q][5]))/Double.parseDouble(arrResult[q][4])), 2);
        }
      }

      String sortColumn = rptUtilsBean.strArrayToStr(arrSortColumnType, ",");

      Log.debugLog(className, "execute_ndi_get_fp_count", "", " ", "Column type for sorting = " + sortColumnType);

      sortColumnType.append(sortColumn);

      //CorrelationService correlationService = new CorrelationService();
      //arrSortResult = correlationService.sortArray(arrSortResult, 2,1, "DECIMAL");

      System.arraycopy(arrSortResult, 0, arrHeader, 1, arrSortResult.length);

      return arrHeader;
    }

    catch(Exception e)
    {
      totalRows.append(0);
      totalQueryCount.append(0);
      totalAvg.append(0);
      defaultSorting.append("0");
      arrResult = new String[2][1];
      arrResult[0][0] = FAIL_QUERY;
      arrResult[1][0] = "No records are found";

      e.printStackTrace();
      Log.stackTraceLog(className, "execute_ndi_get_fp_count", "", "", "Exception - ", e);
      return arrResult;
    }
  }

  /*
   * Method get the no of Flow path for particular URL
   * Arugment :cmdArg , breadCrumb
   * This method is used on URL Component Detail Screen
   */
  public String get_fp_count(String cmdArg , DrillDownBreadCrumb breadCrumb)
  {
    Log.debugLog(className, "get_fp_count()", "", "", "Method called");
    String totalRecord = "";
    try
    {
      if(breadCrumb.getFilterArument().trim().equals("") || !breadCrumb.getFilterArument().trim().equals(cmdArg.trim()))
      {
        strCmdName = NDI_GET_FP_COUNT;

        Log.debugLog(className, "get_fp_count", "", "", "Command Name = " + strCmdName);

        strCmdArgs = " --testrun " + testRun + " " + cmdArg + " " + GET_COUNT_OPTION + " " + DEFAULT_VALUE_GET_COUNT;

        Log.debugLog(className, "get_fp_count()", "", "", "Command Argument = " + cmdArg);

        vecCmdOutput = new Vector();
        boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput , strCmdName, strCmdArgs, 0, userName, null);

        Log.debugLog(className, "get_fp_count()", "", "", "Command Argument = " + cmdArg + "Command Executed Successfully ?" +bolRsltFlag);

        if(!bolRsltFlag)
        {
          totalRecord = "0";
          breadCrumb.setTotalRecord(totalRecord);
          return totalRecord;
        }

        else
        {
          //totalRecord = vecCmdOutput.get(1).toString().trim();
          String arrCounts[] = rptUtilsBean.split(vecCmdOutput.get(1).toString().trim(), "|");
          totalRecord = arrCounts[0];
          breadCrumb.setTotalRecord(totalRecord);
        }
      }

      else
      {
        totalRecord = breadCrumb.getTotalRecord();
        breadCrumb.setTotalRecord(totalRecord.toString());
      }
      return totalRecord;
    }

    catch(Exception e)
    {
      totalRecord = "0";
      e.printStackTrace();
      Log.stackTraceLog(className, "get_fp_count()", "", "", "Exception - ", e);
      return totalRecord;
    }
  }

   //ndi_get_fp_signature
 public String[][] execute_ndi_get_fp_signature(String cmdArg, StringBuffer sortColumnType, StringBuffer defaultSorting, StringBuffer totalRows, String currlimit, String currOffset, DrillDownBreadCrumb downBreadCrumb)
 {
   Log.debugLog(className, "execute_ndi_get_fp_signature", "", "", "Method called. = " + cmdArg);
   String arrResult[][] = null;
   try
   {
     if(downBreadCrumb.getFilterArument().equals("") || (!downBreadCrumb.getOffset().equals(currOffset) || !downBreadCrumb.getLimit().equals(currlimit) || !downBreadCrumb.getFilterArument().trim().equals(cmdArg.trim())))
     {
       long startTimeStamp =  System.currentTimeMillis();
       String limitOffSet = " --limit " + currlimit + " --offset " + currOffset;

       strCmdName = NDI_GET_FP_SIGNATURE;
       Log.debugLog(className, "execute_ndi_get_fp_signature", "", "", "Command Name = " + strCmdName);

       vecCmdOutput = new Vector();

       strCmdArgs = "--testrun " + testRun + " " + cmdArg + " " + GET_COUNT_OPTION + " " + DEFAULT_VALUE_GET_COUNT;

       Log.debugLog(className, "execute_ndi_get_fp_signature", "", "", "Command Argument = " + strCmdArgs);

       long endTimeStamp =  System.currentTimeMillis();

       boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

       Log.debugLog(className, "execute_ndi_get_fp_signature", "", "", "Total Time taken = " + (endTimeStamp - startTimeStamp));

       if(!bolRsltFlag)
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

       strCmdArgs = "--testrun " + testRun + " " + cmdArg + limitOffSet;

       Log.debugLog(className, "execute_ndi_get_fp_signature", "", "", "Command Argument = " + strCmdArgs);

       vecCmdOutput = new Vector();

       bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

       Log.debugLog(className, "execute_ndi_get_fp_signature", "", "", "Checking Commnad executed successfully or not. = " + bolRsltFlag);

       if(!bolRsltFlag)
       {
         defaultSorting.append("0");
         sortColumnType.append("String");
         arrResult = new String[2][1];
         arrResult[0][0] = FAIL_QUERY;
         arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
         downBreadCrumb.setBreadCrumbData(arrResult);
         return arrResult;
       }
       arrResult = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "0");
       downBreadCrumb.setBreadCrumbData(arrResult);
     }
     else
     {
       arrResult = downBreadCrumb.getBreadCrumbData();
       totalRows.append(downBreadCrumb.getTotalRecord());
       //sortColumnType.append(downBreadCrumb.getSortColumnType());
       //defaultSorting.append(downBreadCrumb.getDefaultSortIndicator());
       downBreadCrumb.setBreadCrumbData(arrResult);
     }

     String trStartTimeStr = Scenario.getTestRunStartTime(Integer.parseInt(testRun));
     Date trStartDate = new Date(trStartTimeStr);
     long trStartTime =  trStartDate.getTime();

     String arrResultColumn[][] = new String[arrResult.length][10];

     //create header
     //TierName|Tierid|ServerName|Serverid|AppName|Appid|URLName|URLIndex|FlowPathInstance|StartTime|FPDuration|Methodscount|URLQueryParmStr|StatusCode
     arrResultColumn[0][0] = "Relative Start Time";
     arrResultColumn[0][1] = "Absolute Start Time";
     arrResultColumn[0][2] = "FlowPath Instance";
     arrResultColumn[0][3] = "FlowPath Duration (ms)";
     //arrResultColumn[0][4] = "CPU Time (ms)";
     //arrResultColumn[0][5] = "Log Count";
     //arrResultColumn[0][6] = "Query Count";
     arrResultColumn[0][4] = "URL Name";
     arrResultColumn[0][5] = "URL Query Parameter";
     arrResultColumn[0][6] = "Status Code";
     arrResultColumn[0][7] = "Tier Name";
     arrResultColumn[0][8] = "Server Name";
     arrResultColumn[0][9] = "App Name";


     //error case
     if(arrResult != null && arrResult[0].length == 1)
     {
	   return arrResult;
     }

     //Calculate aggregate average time (ms)
     //FlowPathInstance|FlowPathSegmentTier|BeginSequenceNumber|MetricWindow|FlowPathSignature|ResponseTime|FlowPathBeginTimeStamp|FlowPathEndTimeStamp|LogCount|SQLCount

     if(arrResult.length > 1)
     {
       for(int q = 1 ; q < arrResult.length; q++)
       {
         //calculate start time
    	 // Assuming that query gives start time as Relative Start Time
         long flowPathStartTime = Long.parseLong(arrResult[q][9]);
         long diff = flowPathStartTime + trStartTime;
         //if(diff < 0)
         //  diff = 0;

         arrResultColumn[q][0] = rptUtilsBean.timeInMilliSecToString(arrResult[q][9], 0);
         arrResultColumn[q][1] = rptUtilsBean.setDateFormat("MM/dd/yy HH:mm:ss",diff);
         arrResultColumn[q][2] = arrResult[q][8];
         arrResultColumn[q][3] = rptUtilsBean.convTo3DigitDecimal(Double.parseDouble(arrResult[q][10])) + ""; //this is to convert response time in seconds.
         arrResultColumn[q][4] = arrResult[q][6];
         arrResultColumn[q][5] = arrResult[q][12];
         arrResultColumn[q][6] = arrResult[q][13];
         arrResultColumn[q][7] = arrResult[q][0];
         arrResultColumn[q][8] = arrResult[q][2];
         arrResultColumn[q][9] = arrResult[q][4];
       }
     }

     mapDataBaseSort.put("fpduration", "FlowPath Duration (ms)");
     mapDataBaseSort.put("fpduration_desc", "FlowPath Duration (ms)");
     mapDataBaseSort.put("fpinstance", "FlowPath Instance");

     int sortIndex = -1;

     String sortColName = "ResponseTime";

     String[] sortDataBase = rptUtilsBean.split(drillDownReportData.getSortUsingDataBaseQuery(), ",");

     if(sortDataBase != null && sortDataBase.length > 0)
     {
       if(mapDataBaseSort.containsKey(sortDataBase[0].trim().toLowerCase()))
       {
         sortColName = mapDataBaseSort.get(sortDataBase[0].toLowerCase()).toString();
       }
     }

     String arrSortColumnType[] = new String[arrResultColumn[0].length];

     //Default fill sorting Type String
     Arrays.fill(arrSortColumnType, "String");

     for(int i = 0; i < arrResultColumn[0].length; i++)
     {
       //setting column type index and if flag false skip column to sorting
       boolean bolFlag = setColumnTypeIndex(arrResultColumn[0][i].toString().trim(), i);

       if(mapSort.containsKey(arrResultColumn[0][i].toString().trim()) && bolFlag)
       {
         arrSortColumnType[i] = "," + mapSort.get(arrResultColumn[0][i].toString().trim()).toString();
       }

       if(arrResultColumn[0][i].toString().trim().equals(sortColName))
        defaultSorting.append(i);
     }

     if(defaultSorting.toString().trim().equals(""))
       defaultSorting.append("3");

     String sortColumn = rptUtilsBean.strArrayToStr(arrSortColumnType, ",");

     Log.debugLog(className, "execute_ndi_get_fp_signature", "", "", "Column type for sorting = " + sortColumnType);

     sortColumnType.append(sortColumn);

     return arrResultColumn;
   }
   catch (Exception e)
   {
     totalRows.append(0);
     defaultSorting.append("0");
     e.printStackTrace();
     arrResult = new String[2][1];
     arrResult[0][0] = FAIL_QUERY;
     arrResult[1][0] = "No records are found";
     Log.stackTraceLog(className, "execute_ndi_get_fp_signature", "", "", "Exception - ", e);
     return arrResult;
   }
 }
   //this is to show Application logs reports
    public String[][] execute_ndi_db_get_app_logs(String cmdArg, StringBuffer defaultSorting, DrillDownBreadCrumb downBreadCrumb)
    {
      Log.debugLog(className, "execute_ndi_db_get_app_logs", "", "", "Method called. = " + cmdArg);
      String arrResult[][] = null;
      try
      {
        strCmdName = NSI_DB_GET_APP_LOGS;
        Log.debugLog(className, "execute_ndi_db_get_app_logs", "", "", "Command Name = " + strCmdName);

        strCmdArgs = " --testrun " + testRun + " " + cmdArg;

        Log.debugLog(className, "execute_ndi_db_get_app_logs", "", "", "Command Argument = " + strCmdArgs);

        vecCmdOutput = new Vector();
        boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

        Log.debugLog(className, "execute_ndi_db_get_app_logs", "", "", "Checking Commnad executed successfully or not. = " + bolRsltFlag);
        if(!bolRsltFlag)
        {
          arrResult = new String[2][1];
          arrResult[0][0] = FAIL_QUERY;
          arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
          downBreadCrumb.setBreadCrumbData(arrResult);
          defaultSorting.append(0);
          return arrResult;
        }

        arrResult = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "0");
        downBreadCrumb.setBreadCrumbData(arrResult);
        defaultSorting.append(0);

        String arrResultColumn[][] = new String[arrResult.length][3];

        //create header
        arrResultColumn[0][0] = "Date/Time";
        arrResultColumn[0][1] = "Log Severity";
        arrResultColumn[0][2] = "Log Message";

        //flowpathinstance|urlname|date|severity|classname|methodname|filename|linenumber|message
        if(arrResult.length > 1)
        {
          for(int q = 1 ; q < arrResult.length; q++)
          {
            arrResultColumn[q][0] = arrResult[q][2];
            arrResultColumn[q][1] = arrResult[q][3];
            arrResultColumn[q][2] = arrResult[q][8];
          }
        }

        return arrResultColumn;
      }
      catch (Exception e)
      {
        e.printStackTrace();
        arrResult = new String[2][1];
        arrResult[0][0] = FAIL_QUERY;
        arrResult[1][0] = "No records are found";
        Log.stackTraceLog(className, "execute_ndi_db_get_app_logs", "", "", "Exception - ", e);
        return arrResult;
      }
   }

    public String[][] execute_ndi_db_get_app_query(String cmdArg, StringBuffer defaultSorting, DrillDownBreadCrumb downBreadCrumb)
    {
      Log.debugLog(className, "execute_ndi_db_get_app_query", "", "", "Method called. = " + cmdArg);
      String arrResult[][] = null;
      try
      {
        //strCmdName = NSI_DB_GET_APP_LOGS;
        strCmdName = NDI_DB_GET_QUERY_DATA;
        Log.debugLog(className, "execute_ndi_db_get_app_query", "", "", "Command Name = " + strCmdName);

        strCmdArgs = " --testrun " + testRun + " " + cmdArg;

        Log.debugLog(className, "execute_ndi_db_get_app_query", "", "", "Command Argument = " + strCmdArgs);

        vecCmdOutput = new Vector();
        boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

        Log.debugLog(className, "execute_ndi_db_get_app_query", "", "", "Checking Commnad executed successfully or not. = " + bolRsltFlag);
        if(!bolRsltFlag)
        {
          arrResult = new String[2][1];
          arrResult[0][0] = FAIL_QUERY;
          arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
          downBreadCrumb.setBreadCrumbData(arrResult);
          defaultSorting.append(0);
          return arrResult;
        }

        arrResult = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "0");
        downBreadCrumb.setBreadCrumbData(arrResult);
        defaultSorting.append(0);

        String arrResultColumn[][] = new String[arrResult.length][5];

        //create header
        arrResultColumn[0][0] = "Relative Time";
        arrResultColumn[0][1] = "Absolute Time";
        arrResultColumn[0][2] = "Execution Time (ms)";
        arrResultColumn[0][3] = "Query Type";
        arrResultColumn[0][4] = "Query";

        /*1-Select
        2-Insert
        3-Update
        4-Delete
        5-Create
        6-Drop*/
        String arrForQueryType[] = {"Unknown", "Select", "Insert", "Update", "Delete", "Create", "Drop"};
        String trStartTimeStr = Scenario.getTestRunStartTime(Integer.parseInt(testRun));
        Date trStartDate = new Date(trStartTimeStr);
        long trStartTime =  trStartDate.getTime();

        if(arrResult.length > 1)
        {
          for(int q = 1 ; q < arrResult.length; q++)
          {
            //flowpathinstance|sqlbegintimestamp|querytype|sqlexectime|sqlquery
            long QueryStartTime = Long.parseLong(arrResult[q][1]);
            long diff = QueryStartTime - trStartTime;
            arrResultColumn[q][0] = rptUtilsBean.timeInMilliSecToString(diff+"", 0);
            arrResultColumn[q][1] = rptUtilsBean.setDateFormat("MM/dd/yy HH:mm:ss", Long.parseLong(arrResult[q][1]));
            arrResultColumn[q][2] = arrResult[q][3];
            //map the query type
            if(Integer.parseInt(arrResult[q][2]) <= 6 && Integer.parseInt(arrResult[q][2]) >= 0)
              arrResultColumn[q][3] = arrForQueryType[Integer.parseInt(arrResult[q][2])];
            else
              arrResultColumn[q][3] = "Unknown";

            arrResultColumn[q][4] = arrResult[q][4];
            /*arrResultColumn[q][0] = arrResult[q][1];
            arrResultColumn[q][1] = arrResult[q][2];
            arrResultColumn[q][2] = arrResult[q][7];
            arrResultColumn[q][3] = arrResult[q][0];*/
          }
        }

        return arrResultColumn;
      }
      catch (Exception e)
      {
        e.printStackTrace();
        arrResult = new String[2][1];
        arrResult[0][0] = FAIL_QUERY;
        arrResult[1][0] = "No records are found";
        Log.stackTraceLog(className, "execute_ndi_db_get_app_query", "", "", "Exception - ", e);
        return arrResult;
      }
   }

    public String[][] execute_ndi_get_fp_data_for_adding_to_compare(String cmdArg)
    {
      Log.debugLog(className, "execute_ndi_get_fp_data_for_adding_to_compare", "", "", "Method called. = " + cmdArg);
      String arrResult[][] = null;
      try
      {
        strCmdName = NDI_DB_GET_FP_DATA;
        Log.debugLog(className, "execute_ndi_get_fp_data_for_adding_to_compare", "", "", "Command Name = " + strCmdName);

        strCmdArgs = " " + cmdArg;

        Log.debugLog(className, "execute_ndi_get_fp_data_for_adding_to_compare", "", "", "Command Argument = " + strCmdArgs);

        vecCmdOutput = new Vector();
        boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

        if(vecCmdOutput != null)
          arrResult = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", " ");

        return arrResult;
      }
      catch(Exception e)
      {
    	Log.stackTraceLog(className, "execute_ndi_get_fp_data_for_adding_to_compare", "", "", "", e);
      }
      return null;
    }

   /*
    * Method Name : execute_ndi_get_fp_data
    * Arugment :urlIndex
               :StrBuffer
    */
   //nsi_db_get_obj_instance_data
    public String[][] execute_ndi_get_fp_data(String cmdArg, DrillDownBreadCrumb downBreadCrumb, boolean isForCsv)
    {
      Log.debugLog(className, "execute_ndi_get_fp_data", "", "", "Method called. = " + cmdArg);
      String arrResult[][] = null;
      try
      {
        if(downBreadCrumb.getFilterArument().equals("") || !downBreadCrumb.getFilterArument().equals(cmdArg))
        {
          strCmdName = NDI_DB_GET_FP_DATA;
          Log.debugLog(className, "execute_ndi_get_fp_data", "", "", "Command Name = " + strCmdName);

          strCmdArgs = " " + cmdArg;

          if(isForCsv)
            strCmdArgs = strCmdArgs + " --outputmode 1";

          Log.debugLog(className, "execute_ndi_get_fp_data", "", "", "Command Argument = " + strCmdArgs);

          vecCmdOutput = new Vector();
          boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

          Log.debugLog(className, "execute_ndi_get_fp_data", "", "", "Checking Commnad executed successfully or not. = " + bolRsltFlag);
          if(!bolRsltFlag)
          {
            arrResult = new String[2][1];
            arrResult[0][0] = FAIL_QUERY;
            arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
            downBreadCrumb.setBreadCrumbData(arrResult);
            return arrResult;
          }

          if(isForCsv)
          {
 	        if(vecCmdOutput == null)
 	          return null;
 	        else
 	        {
 	          int colsize = rptUtilsBean.split(vecCmdOutput.get(0).toString(), ",").length;

 	          arrResult = new String[vecCmdOutput.size()][colsize];
		      for(int i = 0; i < vecCmdOutput.size(); i++)
		      {
		        String arrCsv[] = rptUtilsBean.split(vecCmdOutput.get(i).toString(), ",");
		        arrResult[i] = arrCsv.clone();
		      }
		      downBreadCrumb.setBreadCrumbDataFirst(arrResult);
		      return arrResult;
		   }
		 }
		 arrResult = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", " ");
		 downBreadCrumb.setBreadCrumbData(arrResult);
	   }
	   else
	   {
		 if(isForCsv)
		    arrResult = downBreadCrumb.getBreadCrumbDataFirst();
		 else
		 {
           arrResult = downBreadCrumb.getBreadCrumbData();
           downBreadCrumb.setBreadCrumbData(arrResult);
 	     }
       }
       return arrResult;
     }
     catch (Exception e)
     {
        e.printStackTrace();
        arrResult = new String[2][1];
        arrResult[0][0] = FAIL_QUERY;
        arrResult[1][0] = "No records are found";
        Log.stackTraceLog(className, "execute_ndi_get_fp_data", "", "", "Exception - ", e);
        return arrResult;
     }
   }

    /**
     * For Exporting Method Calling Tree to HTML
     * @param cmdArg
     * @param downBreadCrumb
     * @param isForCsv
     * @return
     */
    public String[][] execute_ndi_get_fp_data_html(String cmdArg, DrillDownBreadCrumb downBreadCrumb)
    {
      Log.debugLog(className, "execute_ndi_get_fp_data_html", "", "", "Method called. = " + cmdArg);
      String arrResult[][] = null;
      try
      {
        if(downBreadCrumb.getFilterArument().equals("") || !downBreadCrumb.getFilterArument().equals(cmdArg))
        {
          strCmdName = NDI_DB_GET_FP_DATA;
          Log.debugLog(className, "execute_ndi_get_fp_data_html", "", "", "Command Name = " + strCmdName);

          strCmdArgs = " " + cmdArg;

          strCmdArgs = strCmdArgs + " --outputmode 3";

          Log.debugLog(className, "execute_ndi_get_fp_data_html", "", "", "Command Argument = " + strCmdArgs);

          vecCmdOutput = new Vector();
          boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

          Log.debugLog(className, "execute_ndi_get_fp_data_html", "", "", "Checking Commnad executed successfully or not. = " + bolRsltFlag);
          if(!bolRsltFlag)
          {
            arrResult = new String[2][1];
            arrResult[0][0] = FAIL_QUERY;
            arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
            downBreadCrumb.setBreadCrumbData(arrResult);
            return arrResult;
          }

 	        if(vecCmdOutput == null)
 	          return null;
 	        else
 	        {
 	          int colsize = rptUtilsBean.split(vecCmdOutput.get(0).toString(), ",").length;

 	          arrResult = new String[vecCmdOutput.size()][colsize];
		      for(int i = 0; i < vecCmdOutput.size(); i++)
		      {
		        String arrCsv[] = rptUtilsBean.split(vecCmdOutput.get(i).toString(), ",");
		        arrResult[i] = arrCsv.clone();
		      }
		      downBreadCrumb.setBreadCrumbDataThird(arrResult);
		      return arrResult;
		   }
		 }
		   arrResult = downBreadCrumb.getBreadCrumbDataThird();
       return arrResult;
     }
     catch (Exception e)
     {
        e.printStackTrace();
        arrResult = new String[2][1];
        arrResult[0][0] = FAIL_QUERY;
        arrResult[1][0] = "No records are found";
        Log.stackTraceLog(className, "execute_ndi_get_fp_data_html", "", "", "Exception - ", e);
        return arrResult;
     }
   }


   public String[][] execute_nd_db_get_URL_details_from_fpi(String cmdArg, DrillDownBreadCrumb downBreadCrumb)
   {
     Log.debugLog(className, "execute_nd_db_get_URL_details_from_fpi", "", "", "Method called. = " + cmdArg);
     String arrResult[][] = null;
     try
     {
       if(downBreadCrumb.getFilterArument().equals("") || !downBreadCrumb.getFilterArument().equals(cmdArg))
       {
         strCmdName = ND_DB_GET_URL_DETAILS_FROM_FPI;
         Log.debugLog(className, "execute_nd_db_get_URL_details_from_fpi", "", "", "Command Name = " + strCmdName);

         strCmdArgs = " " + cmdArg;

         Log.debugLog(className, "execute_nd_db_get_URL_details_from_fpi", "", "", "Command Argument = " + strCmdArgs);

         vecCmdOutput = new Vector();
         boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

         Log.debugLog(className, "execute_nd_db_get_URL_details_from_fpi", "", "", "Checking Commnad executed successfully or not. = " + bolRsltFlag);
         if(!bolRsltFlag)
         {
           arrResult = new String[2][1];
           arrResult[0][0] = FAIL_QUERY;
           arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
           downBreadCrumb.setBreadCrumbDataSecond(arrResult);
           return arrResult;
         }

         arrResult = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", " ");
         downBreadCrumb.setBreadCrumbDataSecond(arrResult);
       }
       else
       {
         arrResult = downBreadCrumb.getBreadCrumbDataSecond();
         downBreadCrumb.setBreadCrumbDataSecond(arrResult);
       }
       return arrResult;
     }
     catch (Exception e)
     {
       e.printStackTrace();
       arrResult = new String[2][1];
       arrResult[0][0] = FAIL_QUERY;
       arrResult[1][0] = "No records are found";
       Log.stackTraceLog(className, "execute_nd_db_get_URL_details_from_fpi", "", "", "Exception - ", e);
       return arrResult;
     }
   }


   //For getting hotspot data.
   public String[][] execute_nsi_db_get_flowpath_thread_hotspot_data(String cmdArg, DrillDownBreadCrumb downBreadCrumb)
   {
     Log.debugLog(className, "execute_nsi_db_get_flowpath_thread_hotspot_data", "", "", "Method called. = " + cmdArg);
     String arrResult[][] = null;
     try
     {
       if(downBreadCrumb.getFilterArument().equals("") || !downBreadCrumb.getFilterArument().equals(cmdArg))
       {
         strCmdName = NDI_DB_GET_THREAD_HOTSPOT_DATA;
         Log.debugLog(className, "execute_nsi_db_get_flowpath_thread_hotspot_data", "", "", "Command Name = " + strCmdName);

         strCmdArgs =  "--testrun " + testRun + " " + cmdArg;

         Log.debugLog(className, "execute_nsi_db_get_flowpath_thread_hotspot_data", "", "", "Command Argument = " + strCmdArgs);

         vecCmdOutput = new Vector();
         boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

         Log.debugLog(className, "execute_nsi_db_get_flowpath_thread_hotspot_data", "", "", "Checking Commnad executed successfully or not. = " + bolRsltFlag);
         if(!bolRsltFlag)
         {
           arrResult = new String[2][1];
           arrResult[0][0] = FAIL_QUERY;
           arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
           downBreadCrumb.setBreadCrumbDataSecond(arrResult);
           return arrResult;
         }

         arrResult = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", " ");
         downBreadCrumb.setBreadCrumbDataSecond(arrResult);
       }
       else
       {
         arrResult = downBreadCrumb.getBreadCrumbDataSecond();
         downBreadCrumb.setBreadCrumbDataSecond(arrResult);
       }
       return arrResult;
     }
     catch (Exception e)
     {
       e.printStackTrace();
       arrResult = new String[2][1];
       arrResult[0][0] = FAIL_QUERY;
       arrResult[1][0] = "No records are found";
       Log.stackTraceLog(className, "execute_nsi_db_get_flowpath_thread_hotspot_data", "", "", "Exception - ", e);
       return arrResult;
     }
   }

   /*
   *Commenting due to not in use now.
   *Method Name : execute_ndi_get_fp_data
   *Arugment :urlIndex
             :StrBuffer
   */
   //nsi_db_get_obj_instance_data
/*   public void  execute_ndi_get_meta_data(String[][] blobData  , String imageName)
   {
     Log.debugLog(className, "execute_ndi_get_meta_data", "", "", "Method called. blobData " + blobData);

     //we are using hash map for mathod name and id because one id will have one method name
     //HashMap<String, String> mapForMethodIdName = new HashMap<String, String>();

     //String blobData = arrDataValues[1][7];

     NDGenerateSequenceDiagram ndGenerateSequenceDiagram = new NDGenerateSequenceDiagram();

     ArrayList arrMethodId = ndGenerateSequenceDiagram.getMethodIdsFromBlobInfo(blobData);

     for(int i = 0; i < arrMethodId.size(); i++)
     {
       strCmdName = NDI_GET_META_DATA;
       vecCmdOutput = new Vector();
       strCmdArgs = " -t " + testRun + " -m " + arrMethodId.get(i) + " -e 0" ;
       vecCmdOutput = cmdExec.getResultByCommand(strCmdName, strCmdArgs, 0, userName, null);

       if(vecCmdOutput.size() == 1)
         mapForMethodIdName.put(arrMethodId.get(i).toString(), "NameNotFoundForMethodId");
       else
         mapForMethodIdName.put(arrMethodId.get(i).toString(), vecCmdOutput.get(vecCmdOutput.size() - 1).toString());
     }

     ndGenerateSequenceDiagram.createSequenceDiagram(blobData, imageName);
   }*/

   /*
    * Method Name : execute_ndi_get_agg_fp
    * Arugment :urlIndex

   */
   //nsi_db_get_obj_instance_data
   public String[][] execute_ndi_get_agg_fp(String cmdArg,DrillDownBreadCrumb downBreadCrumb)
   {
     Log.debugLog(className, "execute_ndi_get_agg_fp", "", "", "Method called. = " + cmdArg);
     String arrResult[][] = null;
     try
     {
       if(downBreadCrumb.getFilterArument().equals("") || !downBreadCrumb.getFilterArument().equals(cmdArg))
       {
         strCmdName = NDI_GET_AGG_FP;
         Log.debugLog(className, "execute_ndi_get_agg_fp", "", "", "Command Name = " + strCmdName);

         strCmdArgs = " " + cmdArg;

         Log.debugLog(className, "execute_ndi_get_agg_fp", "", "", "Command Argument = " + strCmdArgs);

         vecCmdOutput = new Vector();
         boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

         Log.debugLog(className, "execute_ndi_get_agg_fp", "", "", "Checking Commnad executed successfully or not. = " + bolRsltFlag);
         if(!bolRsltFlag)
         {
           arrResult = new String[2][1];
           arrResult[0][0] = FAIL_QUERY;
           arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
           downBreadCrumb.setBreadCrumbData(arrResult);
           return arrResult;
         }

         arrResult = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "0");
         downBreadCrumb.setBreadCrumbData(arrResult);
       }
       else
       {
         arrResult = downBreadCrumb.getBreadCrumbData();
         downBreadCrumb.setBreadCrumbData(arrResult);
       }
       return arrResult;
     }
     catch (Exception e)
     {
       e.printStackTrace();
       arrResult = new String[2][1];
       arrResult[0][0] = FAIL_QUERY;
       arrResult[1][0] = "No records are found";
       Log.stackTraceLog(className, "execute_ndi_get_agg_fp", "", "", "Exception - ", e);
       return arrResult;
     }
   }

  //Method to check keyword G_ENABLE_NET_DIAGNOSTICS is enabled in scenario or not.
  //if G_ENABLE_NET_DIAGNOSTICS keyword is enabled then it will return 1 otherwise it will return 0.
  public boolean getNDKeyValue()
  {
    Log.debugLog(className, "getNDKeyValue", "", "", "Method called.");

    String ndKeyValue = "0";
    boolean finalValue = true;
    try
    {
      String keyValues[] = Scenario.getKeywordValues("NET_DIAGNOSTICS_SERVER", Integer.parseInt(testRun));

      if(keyValues == null)
	return false;

      if(keyValues.length >= 1)
        ndKeyValue = keyValues[0];

      if(ndKeyValue.trim().equals("0"))
        finalValue = false;

      return finalValue;
    }

    catch(Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "getNDKeyValue", "", "", "Exception - ", e);
      return false;
    }
  }

  /*
   * Method Name : execute_ndi_get_entity_time_ex
   * Arugment1   : cmdArg
     Argument2   : sortColumnType
     Argument3   : defaultSorting(use to show default sorting indicator)
     Argument4   : imageOfPackagePieChart(name of Package Pie Chart name pass from GUI)
     Argument5   : imageOfClassPieChart(name of Class Pie Chart name pass from GUI)
     Argument6   : imageOfMethodPieChart(name of Method Pie Chart name pass from GUI)
  */
  //execute_ndi_get_entity_time_ex
  public String[][] execute_ndi_get_entity_time_ex(String cmdArg , StringBuffer sortColumnType, StringBuffer defaultSorting, StringBuffer totalRecords, String imageOfPackagePieChart, String imageOfClassPieChart , String imageOfMethodPieChart, String imageOfFunctGrp, String pageName, DrillDownBreadCrumb breadCrumb)
  {
    Log.debugLog(className, "ndi_get_entity_time_ex", "", "", "Method called. = " + cmdArg + ", pageNmae = "+pageName);

    String[] arrEntityName = null;
    String[] arrCumulativeSelfTime = null;

    Vector colorListForMethod = new Vector();

    String arrResult[][] = null;
    String arrResultColumn2[][] = null;

    GenerateChart generatePieChart = new GenerateChart();
    double totalCumulativeSelfTime = 0.0;
    int arrLength = 0;
    try
    {
      if(breadCrumb.getFilterArument().trim().equals("") || !breadCrumb.getFilterArument().trim().equals(cmdArg.trim()))
      {
        try
        {
          File fileImageObj = new File(Config.getWorkPath() + "/webapps/netstorm/temp/" + imageOfPackagePieChart);
          fileImageObj.delete();

          fileImageObj = new File(Config.getWorkPath() + "/webapps/netstorm/temp/" + imageOfClassPieChart);
          fileImageObj.delete();

          fileImageObj = new File(Config.getWorkPath() + "/webapps/netstorm/temp/" + imageOfMethodPieChart);
          fileImageObj.delete();

          fileImageObj = new File(Config.getWorkPath() + "/webapps/netstorm/temp/" + imageOfFunctGrp);
          fileImageObj.delete();
        }
        catch (Exception e) {
          // TODO: handle exception
        }

        strCmdName = NDI_GET_ENTITY_TIME_EX;
        Log.debugLog(className, "execute_ndi_get_entity_time_ex", "", "", "Command Name = " + strCmdName);

        //to use entity -1 option
        strCmdArgs = "--testrun " + testRun + " " + "--entity -1" + " " + cmdArg;

        Log.debugLog(className, "execute_ndi_get_entity_time_ex", "", "", "Command Argument = " + strCmdArgs);

        vecCmdOutput = new Vector();
        boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

        Log.debugLog(className, "execute_ndi_get_entity_time_ex", "", "", "Checking Commnad executed successfully or not. = " + bolRsltFlag);

        if(!bolRsltFlag)
        {
          defaultSorting.append("0");
          arrResult = new String[2][1];
          arrResult[0][0] = FAIL_QUERY;
          arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
          totalRecords.append("0");
          breadCrumb.setBreadCrumbData(arrResult);
          breadCrumb.setBreadCrumbDataFirst(arrResult);
          breadCrumb.setBreadCrumbDataSecond(arrResult);
          breadCrumb.setBreadCrumbDataThird(arrResult);
          return arrResult;
        }

        Vector vecCmdOutput0 = new Vector();  //for method EntityType 0
        Vector vecCmdOutput1 = new Vector();  //for class EntityType 1
        Vector vecCmdOutput2 = new Vector();  //for packege EntityType 2
        Vector vecCmdOutput4 = new Vector();  //for functioal grp EntityType 4

        for(int i =0; i < vecCmdOutput.size(); i++)
        {
          if(i == 0)
          {
            vecCmdOutput0.add(vecCmdOutput.get(i));
            vecCmdOutput1.add(vecCmdOutput.get(i));
            vecCmdOutput2.add(vecCmdOutput.get(i));
            vecCmdOutput4.add(vecCmdOutput.get(i));
          }
          else
          {
            if(vecCmdOutput.get(i).toString().startsWith("0|"))
              vecCmdOutput0.add(vecCmdOutput.get(i));
            else if(vecCmdOutput.get(i).toString().startsWith("1|"))
              vecCmdOutput1.add(vecCmdOutput.get(i));
            if(vecCmdOutput.get(i).toString().startsWith("2|"))
              vecCmdOutput2.add(vecCmdOutput.get(i));
            if(vecCmdOutput.get(i).toString().startsWith("4|"))
              vecCmdOutput4.add(vecCmdOutput.get(i));
          }
        }

        //setTotalRecords to Breadcrum object
        if(vecCmdOutput0.size() > 0)
          breadCrumb.setTotalRecordFirst((vecCmdOutput0.size()-1)+"");
        if(vecCmdOutput1.size() > 0)
          breadCrumb.setTotalRecordSecond((vecCmdOutput1.size()-1)+"");
        if(vecCmdOutput2.size() > 0)
          breadCrumb.setTotalRecordThird((vecCmdOutput2.size()-1)+"");
        if(vecCmdOutput4.size() > 0)
          breadCrumb.setTotalRecordFourth((vecCmdOutput4.size()-1)+"");


        //for functional grp EntityType 4
        arrResult = rptUtilsBean.getRecFlds(vecCmdOutput4, "", "", "0");
        totalCumulativeSelfTime = 0.0;
        if(arrResult.length > 1)
        {
          arrEntityName = new String[arrResult.length-1];
          arrCumulativeSelfTime = new String[arrResult.length-1];
          for(int i = 1; i < arrResult.length; i++)
          {
            arrEntityName[i-1] = arrResult[i][2];
            totalCumulativeSelfTime += Double.parseDouble(arrResult[i][4]);
            arrCumulativeSelfTime[i-1] = (Double.parseDouble(arrResult[i][4])/1000d) + "";
          }
          colorListForMethod = generatePieChart.generate3DPieChart(arrEntityName, arrCumulativeSelfTime, testRun, "", imageOfFunctGrp, 1000, 500);
        }

        //set data to breadCrum object.
        arrLength = arrResult.length;
        //if(arrLength > 11)
          //arrLength = 11;

        String[][] arrFunctionalGrp = new String[arrLength][11];
        arrFunctionalGrp[0][0] = "Color";
        arrFunctionalGrp[0][1] = "Functional Group";
        arrFunctionalGrp[0][2] = "Percentage";
        arrFunctionalGrp[0][3] = "Cumulative Self Time (Seconds)";
        arrFunctionalGrp[0][4] = "Average Self Time (ms)";
        arrFunctionalGrp[0][5] = "Cumulative CPU Time (ms)";
        arrFunctionalGrp[0][6] = "Average CPU Time (ms)";
        arrFunctionalGrp[0][7] = "Cumulative Wall Time (Seconds)";
        arrFunctionalGrp[0][8] = "Average Wall Time (ms)";
        arrFunctionalGrp[0][9] = "Execution Count";
        arrFunctionalGrp[0][10] = "MethodId";
        //arrFunctionalGrp[0][8] = "Min";
        //arrFunctionalGrp[0][9] = "Max";
        //arrFunctionalGrp[0][10] = "VMR";

        //EntityType|ID|Entity Name|FunctionalGroup|total_self_time_in_ms|total_cpu_time_in_ns|Execution Count|avgselftime|Min|Max|Variance
        for(int i = 1; i < arrResult.length; i++)
        {
          arrFunctionalGrp[i][0] = colorListForMethod.get(i-1).toString();
          arrFunctionalGrp[i][1] = arrResult[i][2];
          if(totalCumulativeSelfTime == 0.0)
            arrFunctionalGrp[i][2] = 0+"";
          else
            arrFunctionalGrp[i][2] = rptUtilsBean.convertTodecimal((Double.parseDouble(arrResult[i][4]))*100/totalCumulativeSelfTime, 3);
          
          arrFunctionalGrp[i][3] = rptUtilsBean.convertTodecimal((Double.parseDouble(arrResult[i][4])/1000d), 3) + "";
          arrFunctionalGrp[i][4] = rptUtilsBean.convertTodecimal((Double.parseDouble(arrResult[i][4]))/Double.parseDouble(arrResult[i][6]), 3);;
          arrFunctionalGrp[i][5] = rptUtilsBean.convertTodecimal((Double.parseDouble(arrResult[i][5])/1000000000d), 3)+ "";
          arrFunctionalGrp[i][6] = rptUtilsBean.convertTodecimal(Double.parseDouble(arrResult[i][5])/(Double.parseDouble(arrResult[i][6])*1000000), 3);
          //Average Wall Time
          arrFunctionalGrp[i][7] = rptUtilsBean.convertTodecimal((Double.parseDouble(arrResult[i][11])/1000d), 3) + "";
          arrFunctionalGrp[i][8] = rptUtilsBean.convertTodecimal((Double.parseDouble(arrResult[i][11]))/(Double.parseDouble(arrResult[i][6])),3) + "";
          //arrFunctionalGrp[i][8] = rptUtilsBean.convertNumberToCommaSeparate(arrResult[i][8]);
          arrFunctionalGrp[i][9] = rptUtilsBean.convertNumberToCommaSeparate(arrResult[i][6]);
          arrFunctionalGrp[i][10] = arrResult[i][1];
          //arrFunctionalGrp[i][9] = rptUtilsBean.convertNumberToCommaSeparate(arrResult[i][9]);
          //if(arrResult[i][7].equals("0"))
            //arrFunctionalGrp[i][10] = rptUtilsBean.convertNumberToCommaSeparate(arrResult[i][10]);
          //else
            //arrFunctionalGrp[i][10] = rptUtilsBean.convertNumberToCommaSeparate(rptUtilsBean.convertTodecimal(Double.parseDouble(arrResult[i][10])/Double.parseDouble(arrResult[i][7]),3));
        }

        totalCumulativeSelfTime = 0.0;
        //for package EntityType 2
        arrResult = rptUtilsBean.getRecFlds(vecCmdOutput2, "", "", "0");

        if(arrResult.length > 1)
        {
          arrEntityName = new String[arrResult.length-1];
          arrCumulativeSelfTime = new String[arrResult.length-1];
          for(int i = 1; i < arrResult.length; i++)
          {
//          due to change index of Name and self time.
            arrEntityName[i-1] = arrResult[i][2];
            totalCumulativeSelfTime += Double.parseDouble(arrResult[i][4]);
            arrCumulativeSelfTime[i-1] = (Double.parseDouble(arrResult[i][4])/1000d) + "";
          }

          colorListForMethod = generatePieChart.generate3DPieChart(arrEntityName, arrCumulativeSelfTime, testRun, "", imageOfPackagePieChart, 1000, 500);
        }

        arrLength = arrResult.length;
        //if(arrLength > 11)
          //arrLength = 11;

        String[][] arrPackageData = new String[arrLength][11];
        arrPackageData[0][0] = "Color";
        arrPackageData[0][1] = "PackageName";
        arrPackageData[0][2] = "Percentage";
        arrPackageData[0][3] = "Cumulative Self Time (Seconds)";
        arrPackageData[0][4] = "Average Self Time (ms)";
        arrPackageData[0][5] = "Cumulative CPU Time (ms)";
        arrPackageData[0][6] = "Average CPU Time (ms)";
        arrPackageData[0][7] = "Cumulative Wall Time (Seconds)";
        arrPackageData[0][8] = "Average Wall Time (ms)";
        arrPackageData[0][9] = "Execution Count";
        arrPackageData[0][10] = "MethodId";
        //arrPackageData[0][8] = "Min";
        //arrPackageData[0][9] = "Max";
        //arrPackageData[0][10] = "VMR";

        for(int i = 1; i < arrResult.length; i++)
        {
          arrPackageData[i][0] = colorListForMethod.get(i-1).toString();
          arrPackageData[i][1] = arrResult[i][2];
          if(totalCumulativeSelfTime == 0.0)
            arrPackageData[i][2] = 0+"";
          else
            arrPackageData[i][2] = rptUtilsBean.convertTodecimal((Double.parseDouble(arrResult[i][4]))*100/totalCumulativeSelfTime, 3);
          arrPackageData[i][3] = rptUtilsBean.convertTodecimal((Double.parseDouble(arrResult[i][4])/1000d), 3) + "";
          arrPackageData[i][4] = rptUtilsBean.convertTodecimal((Double.parseDouble(arrResult[i][4]))/Double.parseDouble(arrResult[i][6]), 3);;
          arrPackageData[i][5] = rptUtilsBean.convertTodecimal((Double.parseDouble(arrResult[i][5])/1000000000d), 3)+ "";
          arrPackageData[i][6] = rptUtilsBean.convertTodecimal(Double.parseDouble(arrResult[i][5])/(Double.parseDouble(arrResult[i][6])*1000000), 3);
          //Average Wall Time
          arrPackageData[i][7] = rptUtilsBean.convertTodecimal((Double.parseDouble(arrResult[i][11])/1000d), 3) + "";
          arrPackageData[i][8] = rptUtilsBean.convertTodecimal((Double.parseDouble(arrResult[i][11]))/(Double.parseDouble(arrResult[i][6])),3) + "";
          //arrPackageData[i][8] = rptUtilsBean.convertNumberToCommaSeparate(arrResult[i][8]);
          arrPackageData[i][9] = rptUtilsBean.convertNumberToCommaSeparate(arrResult[i][6]);
          arrPackageData[i][10] = arrResult[0][1];
          //arrPackageData[i][9] = rptUtilsBean.convertNumberToCommaSeparate(arrResult[i][9]);
          //if(arrResult[i][7].equals("0"))
            //arrPackageData[i][10] = rptUtilsBean.convertNumberToCommaSeparate(arrResult[i][10]);
          //else
            //arrPackageData[i][10] = rptUtilsBean.convertNumberToCommaSeparate(rptUtilsBean.convertTodecimal(Double.parseDouble(arrResult[i][10])/Double.parseDouble(arrResult[i][7]),3));
        }

        totalCumulativeSelfTime = 0.0;
        //for class EntityType 1
        arrResult = rptUtilsBean.getRecFlds(vecCmdOutput1, "", "", "0");

        if(arrResult.length > 1)
        {
          arrEntityName = new String[arrResult.length-1];
          arrCumulativeSelfTime = new String[arrResult.length-1];
          for(int i = 1; i < arrResult.length; i++)
          {
            arrEntityName[i-1] = arrResult[i][2];
            totalCumulativeSelfTime += Double.parseDouble(arrResult[i][4]);
            arrCumulativeSelfTime[i-1] = (Double.parseDouble(arrResult[i][4])/1000d) + "";
          }
          colorListForMethod = generatePieChart.generate3DPieChart(arrEntityName, arrCumulativeSelfTime, testRun, "", imageOfClassPieChart, 1000, 500);
        }

        arrLength = arrResult.length;
        //if(arrLength > 11)
          //arrLength = 11;

        String[][] arrClassData = new String[arrLength][12];
        arrClassData[0][0] = "Color";
        arrClassData[0][1] = "PackageName";
        arrClassData[0][2] = "ClassName";
        arrClassData[0][3] = "Percentage";
        arrClassData[0][4] = "Cumulative Self Time (Seconds)";
        arrClassData[0][5] = "Average Self Time (ms)";
        arrClassData[0][6] = "Cumulative CPU Time (ms)";
        arrClassData[0][7] = "Average CPU Time (ms)";
        arrClassData[0][8] = "Cumulative Wall Time (Seconds)";
        arrClassData[0][9] = "Average Wall Time (ms)";
        arrClassData[0][10] = "Execution Count";
        arrClassData[0][11] = "MethodId";
        //arrClassData[0][9] = "Min";
        //arrClassData[0][10] = "Max";
        //arrClassData[0][11] = "VMR";

        for(int i = 1; i < arrResult.length; i++)
        {
          arrClassData[i][0] = colorListForMethod.get(i-1).toString();
          arrClassData[i][1] = generatePieChart.getPackageName(arrResult[i][2]+". ");
          arrClassData[i][2] = generatePieChart.getClassName(arrResult[i][2]+". ");
          if(totalCumulativeSelfTime == 0.0)
            arrClassData[i][3] = 0+"";
          else
            arrClassData[i][3] = rptUtilsBean.convertTodecimal((Double.parseDouble(arrResult[i][4]))*100/totalCumulativeSelfTime, 3);
          arrClassData[i][4] = rptUtilsBean.convertTodecimal((Double.parseDouble(arrResult[i][4])/1000d), 3) + "";
          arrClassData[i][5] = rptUtilsBean.convertTodecimal((Double.parseDouble(arrResult[i][4]))/Double.parseDouble(arrResult[i][6]), 3);;
          arrClassData[i][6] = rptUtilsBean.convertTodecimal((Double.parseDouble(arrResult[i][5])/1000000000d), 3)+ "";
          arrClassData[i][7] = rptUtilsBean.convertTodecimal(Double.parseDouble(arrResult[i][5])/(Double.parseDouble(arrResult[i][6])*1000000), 3);
          //Wall Time
          arrClassData[i][8] = rptUtilsBean.convertTodecimal((Double.parseDouble(arrResult[i][11])/1000d), 3) + "";
          arrClassData[i][9] = rptUtilsBean.convertTodecimal((Double.parseDouble(arrResult[i][11]))/(Double.parseDouble(arrResult[i][6])),3) + "";
          //arrClassData[i][9] = rptUtilsBean.convertNumberToCommaSeparate(arrResult[i][8]);
          arrClassData[i][10] = rptUtilsBean.convertNumberToCommaSeparate(arrResult[i][6]);
          arrClassData[i][11] = arrResult[i][1];
          
          //arrClassData[i][10] = rptUtilsBean.convertNumberToCommaSeparate(arrResult[i][9]);
          //if(arrResult[i][7].equals("0"))
            //arrClassData[i][11] = rptUtilsBean.convertNumberToCommaSeparate(arrResult[i][10]);
          //else
            //arrClassData[i][11] = rptUtilsBean.convertNumberToCommaSeparate(rptUtilsBean.convertTodecimal(Double.parseDouble(arrResult[i][10])/Double.parseDouble(arrResult[i][7]),3));
        }

        totalCumulativeSelfTime = 0.0;
        //for method EntityType 0
        arrResult = rptUtilsBean.getRecFlds(vecCmdOutput0, "", "", "0");
        if(arrResult.length > 1)
        {
          arrEntityName = new String[arrResult.length-1];
          arrCumulativeSelfTime = new String[arrResult.length-1];
          for(int i = 1; i < arrResult.length; i++)
          {
            arrEntityName[i-1] = arrResult[i][2];
            totalCumulativeSelfTime += Double.parseDouble(arrResult[i][4]);
            arrCumulativeSelfTime[i-1] = (Double.parseDouble(arrResult[i][4])/1000d) + "";
          }
          colorListForMethod = generatePieChart.generate3DPieChart(arrEntityName, arrCumulativeSelfTime, testRun, "", imageOfMethodPieChart, 1000, 500);
        }

        arrLength = arrResult.length;
        //if(arrLength > 11)
          //arrLength = 11;

        String[][] arrMethodData = new String[arrLength][18];
        arrMethodData[0][0] = "Color";
        arrMethodData[0][1] = "PackageName";
        arrMethodData[0][2] = "ClassName";
        arrMethodData[0][3] = "MethodName";
        arrMethodData[0][4] = "MethodToolTipName";
        arrMethodData[0][5] = "FunctionalGroup";
        arrMethodData[0][6] = "Percentage";
        arrMethodData[0][7] = "Cumulative Self Time (Seconds)";
        arrMethodData[0][8] = "Average Self Time (ms)";
        arrMethodData[0][9] = "Cumulative CPU Time (ms)";
        arrMethodData[0][10] = "Average CPU Time (ms)";
        arrMethodData[0][11] = "Cumulative Wall Time (Seconds)";
        arrMethodData[0][12] = "Average Wall Time (ms)";
        arrMethodData[0][13] = "Execution Count";
        arrMethodData[0][14] = "Min";
        arrMethodData[0][15] = "Max";
        arrMethodData[0][16] = "VMR";
        arrMethodData[0][17] = "MethodId";

        for(int i = 1; i < arrResult.length; i++)
        {
          arrMethodData[i][0] = colorListForMethod.get(i-1).toString();
          arrMethodData[i][1] = generatePieChart.getPackageName(arrResult[i][2]);
          arrMethodData[i][2] = generatePieChart.getClassName(arrResult[i][2]);
          arrMethodData[i][3] = generatePieChart.getMethodNameWithSignature(arrResult[i][2]);
          arrMethodData[i][4] = generatePieChart.getMethodNameWithoutSignature(arrResult[i][2]);
          arrMethodData[i][5] = arrResult[i][3];
          if(totalCumulativeSelfTime == 0.0)
            arrMethodData[i][6] = 0+"";
          else
            arrMethodData[i][6] = rptUtilsBean.convertTodecimal((Double.parseDouble(arrResult[i][4]))*100/totalCumulativeSelfTime, 3);
          
          arrMethodData[i][7] = rptUtilsBean.convertTodecimal((Double.parseDouble(arrResult[i][4])/1000d), 3) + "";
          arrMethodData[i][8] = rptUtilsBean.convertTodecimal((Double.parseDouble(arrResult[i][4]))/Double.parseDouble(arrResult[i][6]), 3);;
          arrMethodData[i][9] = rptUtilsBean.convertTodecimal((Double.parseDouble(arrResult[i][5])/1000000000d), 3)+ "";
          arrMethodData[i][10] = rptUtilsBean.convertTodecimal(Double.parseDouble(arrResult[i][5])/(Double.parseDouble(arrResult[i][6])*1000000), 3);
          //Wall Time
          arrMethodData[i][11] = rptUtilsBean.convertTodecimal((Double.parseDouble(arrResult[i][11])/1000d), 3) + "";
          arrMethodData[i][12] = rptUtilsBean.convertTodecimal((Double.parseDouble(arrResult[i][11]))/(Double.parseDouble(arrResult[i][6])),3) + "";
        
          arrMethodData[i][13] = rptUtilsBean.convertNumberToCommaSeparate(arrResult[i][6]);
          arrMethodData[i][14] = rptUtilsBean.convertNumberToCommaSeparate(arrResult[i][8]);
          arrMethodData[i][15] = rptUtilsBean.convertNumberToCommaSeparate(arrResult[i][9]);
          if(Double.parseDouble(arrResult[i][7]) == 0.0)
            arrMethodData[i][16] = rptUtilsBean.convertNumberToCommaSeparate(rptUtilsBean.convertTodecimal(Double.parseDouble(arrResult[i][10]),3));
          else
            arrMethodData[i][16] = rptUtilsBean.convertNumberToCommaSeparate(rptUtilsBean.convertTodecimal(Double.parseDouble(arrResult[i][10])/Double.parseDouble(arrResult[i][7]),3));

          arrMethodData[i][17] = arrResult[i][1];
        }

        breadCrumb.setBreadCrumbData(arrMethodData);
        breadCrumb.setBreadCrumbDataFirst(arrClassData);
        breadCrumb.setBreadCrumbDataSecond(arrPackageData);
        breadCrumb.setBreadCrumbDataThird(arrFunctionalGrp);
      }

      if(pageName.equals("method"))
      {
        arrResultColumn2 = breadCrumb.getBreadCrumbData();
        totalRecords.append(breadCrumb.getTotalRecordFirst());
        defaultSorting.append("8_method");
      }
      else if(pageName.equals("class"))
      {
        arrResultColumn2 = breadCrumb.getBreadCrumbDataFirst();
        totalRecords.append(breadCrumb.getTotalRecordSecond());
        defaultSorting.append("5_class");
      }
      else if(pageName.equals("package"))
      {
        arrResultColumn2 = breadCrumb.getBreadCrumbDataSecond();
        totalRecords.append(breadCrumb.getTotalRecordThird());
        defaultSorting.append("4_package");
      }
      else if(pageName.equals("functionalGrp"))
      {
        arrResultColumn2 = breadCrumb.getBreadCrumbDataThird();
        totalRecords.append(breadCrumb.getTotalRecordFourth());
        defaultSorting.append("4_funGrp");
      }

      String arrSortColumnType[] = new String[arrResultColumn2[0].length];

      //Default fill sorting Type Number
      Arrays.fill(arrSortColumnType, "String");

      for(int i = 0; i < arrResultColumn2[0].length; i++)
      {
        //setting column type index and if flag false skip column to sorting
        boolean bolFlag = setColumnTypeIndex(arrResultColumn2[0][i].toString().trim(), i);

        if(mapSort.containsKey(arrResultColumn2[0][i].toString().trim()) && bolFlag)
        {
          arrSortColumnType[i] = mapSort.get(arrResultColumn2[0][i].toString().trim()).toString();
        }
      }

      String sortColumn = rptUtilsBean.strArrayToStr(arrSortColumnType, ",");

      Log.debugLog(className, "execute_ndi_get_entity_time_ex", "", "", "Column type for sorting = " + sortColumnType);

      sortColumnType.append(sortColumn);
      return arrResultColumn2;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      defaultSorting.append("0");
      totalRecords.append("0");
      arrResult = new String[2][1];
      arrResult[0][0] = FAIL_QUERY;
      arrResult[1][0] = "No records are found";
      Log.stackTraceLog(className, "execute_ndi_get_entity_time_ex", "", "", "Exception - ", e);
      return arrResult;
    }
  }

  
  /**
   * Method to Execute Query for Method Timing in Sequence Diagram
   * @param cmdArg
   * @return
   */
  public String[][] execute_ndi_get_entity_time_ex(String cmdArg, DrillDownBreadCrumb breadCrumb)
  {
      Log.debugLog(className, "execute_ndi_get_entity_time_ex", "", "", "Method called");
      String arrResult[][] = null;
      String arrMethdata[][] =  null;
      double totalCumulativeSelfTime = 0.0;
      try
      {
    	if(breadCrumb.getFilterArument().trim().equals("") || !breadCrumb.getFilterArument().trim().equals(cmdArg.trim()))
    	{
          strCmdName = NDI_GET_ENTITY_TIME_EX;
          strCmdArgs = " " + cmdArg;
          Log.debugLog(className, "execute_ndi_get_entity_time_ex", "", "","Command Name With Arguments :" + strCmdName);
          vecCmdOutput = new Vector();
          boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);
          Log.debugLog(className, "execute_ndi_get_entity_time_ex", "", "", "Executing Query. Flag After Execution :" + bolRsltFlag);
          if(!bolRsltFlag)
          {
           arrResult = new String[2][1];
           arrResult[0][0] = FAIL_QUERY;
           arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
           breadCrumb.setBreadCrumbDataFourth(arrResult);
           return arrResult;
          }
         arrResult = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", " ");
         
         if(arrResult.length > 1)
         {
           for(int i = 1; i < arrResult.length; i++)
           {
             totalCumulativeSelfTime += Double.parseDouble(arrResult[i][4]);
           }
         }

         arrMethdata = new String[arrResult.length][arrResult[0].length + 11];
         
         for(int i=0;i<arrMethdata[0].length;i++)
         {
        	 if(i <= 11)
        	   arrMethdata[0][i] = arrResult[0][i];
        	
        	 if(i > 11)
        	 {
        		 arrMethdata[0][12] = "Package Name";
        		 arrMethdata[0][13] = "Class Name";
        		 arrMethdata[0][14] = "Method Name";
        		 arrMethdata[0][15] = "Percentage";
        		 arrMethdata[0][16] = "Cumulative Self Time (Seconds)";
        		 arrMethdata[0][17] = "Average Self Time (ms)";
        		 arrMethdata[0][18] = "Cumulative CPU Time (Seconds)";
        		 arrMethdata[0][19] = "Average CPU Time (ms)";
        		 arrMethdata[0][20] = "Cumulative Wall Time (Seconds)";
        		 arrMethdata[0][21] = "Average Wall Time (ms)";
        		 arrMethdata[0][22] = "Execution Count";
        	 }
        	 
         }
         
         
         for(int i=1;i<arrMethdata.length;i++)
         {
        	 for(int j = 0; j < arrMethdata[0].length; j++)
        	 {
        		 //loop through each row
        		 if(j <= 11)
        		 {
        			 arrMethdata[i][j] = arrResult[i][j];
        			 
        			 if(j == 2)
        				getMethClassPacName(arrResult[i][j]);
        			 if(j == 4)
        			    arrMethdata[i][7] = rptUtilsBean.convertTodecimal((Double.parseDouble(arrResult[i][4])/1000d), 3) + "";
        			 if(j == 6)
        			    arrMethdata[i][22] = arrResult[i][6];// execution Count	
        			 
        		 }
        		 else
        		 {
        			 if(j == 12)
        			   arrMethdata[i][j] = methName;
        			 if(j == 13)
        			   arrMethdata[i][j] = className1;
        			 if(j == 14)
        			   arrMethdata[i][j] = pacName;
        			 
        			 if(totalCumulativeSelfTime == 0.0)
        			   arrMethdata[i][15] = 0+"";
        		     else
        		     {
        		    	 arrMethdata[i][15] = rptUtilsBean.convertTodecimal((Double.parseDouble(arrResult[i][4]))*100/totalCumulativeSelfTime, 3);
        		     }
        			
        			 if(j == 16)
        			 arrMethdata[i][16] = rptUtilsBean.convertTodecimal((Double.parseDouble(arrResult[i][4])/1000d), 3) + ""; // Cumulative Self Time (Seconds)
        			 
        			 if(j == 17)
        			 arrMethdata[i][17] = rptUtilsBean.convertTodecimal((Double.parseDouble(arrResult[i][4]))/Double.parseDouble(arrResult[i][6]), 3); // Average Self Time (ms)
        			 
        			 if(j == 18)
        			 arrMethdata[i][18] = rptUtilsBean.convertTodecimal((Double.parseDouble(arrResult[i][5])/1000000000d), 3)+ ""; // Cumulative CPU Time (Seconds)
        			 
        			 if(j == 19) 
        			 arrMethdata[i][19] = rptUtilsBean.convertTodecimal(Double.parseDouble(arrResult[i][5])/(Double.parseDouble(arrResult[i][6])*1000000), 3);// Average CPU Time (ms)

        			 if(j == 20) 
           			 arrMethdata[i][20] = rptUtilsBean.convertTodecimal((Double.parseDouble(arrResult[i][11])/1000d), 3) + ""; // Cumulative Wall Time (Seconds)
        			 
        			 if(j == 21) 
        			 arrMethdata[i][21] = rptUtilsBean.convertTodecimal((Double.parseDouble(arrResult[i][11]))/Double.parseDouble(arrResult[i][6]), 3);// Average Wall Time (ms)
        			 
        			 breadCrumb.setBreadCrumbDataFourth(arrMethdata);	 
        		 }
        	 }
          }
    	}
    	else
    	{
    		//Getting from Cached Object
    		arrMethdata = breadCrumb.getBreadCrumbDataFourth();
    	}
         Log.debugLog(className, "execute_ndi_get_entity_time_ex", "", "", "Returning Array of Length : " + arrResult.length);
         return arrMethdata;
      }
      catch(Exception e)
      {
	     Log.stackTraceLog(className, "execute_ndi_get_entity_time_ex", "", "", "Exception - ", e);
         return arrMethdata;
      }
  }
  
  private void getMethClassPacName(String sign)
  {
	  Log.debugLog(className, "getMethClassPacName", "", "", "Method called.");
	  
	  String wholeSign = "";
	  try
	  {
		  if(sign != null && !sign.equals(""))
		  {
			wholeSign = sign;
			methName = wholeSign.substring(wholeSign.lastIndexOf(".") + 1, wholeSign.length());
			wholeSign = wholeSign.substring(0, wholeSign.lastIndexOf("."));
			className1 = wholeSign.substring(wholeSign.lastIndexOf(".") + 1, wholeSign.length());
			pacName = wholeSign.substring(0, wholeSign.lastIndexOf("."));
			
		  }
	  }
	  catch(Exception e)
	  {
		  Log.stackTraceLog(className, "getMethClassPacName", "", "", "Exception - ", e);  
	  }
	  
  }
  
  public String[][] execute_nsi_db_tr_status()
  {
    Log.debugLog(className, "execute_nsi_db_tr_status", "", "", "Method called.");

    String arrResult[][] = null;
    try
    {
      strCmdName = NSI_DB_TR_STATUS;
      Log.debugLog(className, "execute_nsi_db_tr_status", "", "", "Command Name = " + strCmdName);

      strCmdArgs = " --testrun " + testRun;

      Log.debugLog(className, "execute_nsi_db_tr_status", "", "", "Command Argument = " + strCmdArgs);

      vecCmdOutput = new Vector();
      boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

      Log.debugLog(className, "execute_nsi_db_tr_status", "", "", "Checking Commnad executed successfully or not. = " + bolRsltFlag);

      if(!bolRsltFlag)
      {
        arrResult = new String[2][1];
        arrResult[0][0] = FAIL_QUERY;
        arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
        return arrResult;
      }

      arrResult = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "");

      if(arrResult == null)
      {
        arrResult = new String[1][1];
        arrResult[0][0] = FAIL_QUERY;
      }
      return arrResult;
    }
    catch (Exception e)
    {
      arrResult = new String[2][1];
      arrResult[0][0] = FAIL_QUERY;
      arrResult[1][0] = "No records are found";
      Log.stackTraceLog(className, "execute_nsi_db_tr_status", "", "", "Exception - ", e);
      return arrResult;
    }
  }

  public Object[] checkStatusPostgreSQL()
  {
    Log.debugLog(className, "checkStatusPostgreSQL", "", "", "Method called.");

    Object[] arrResult = new Object[2];
    try
    {
      strCmdName = "/etc/init.d/postgresql" ;
      Log.debugLog(className, "checkStatusPostgreSQL", "", "", "Command Name = " + strCmdName);

      strCmdArgs = "status";

      Log.debugLog(className, "checkStatusPostgreSQL", "", "", "Command Argument = " + strCmdArgs);

      vecCmdOutput = new Vector();
      boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, CmdExec.SYSTEM_CMD, userName, "root");

      Log.debugLog(className, "checkStatusPostgreSQL", "", "", "Checking Commnad executed successfully or not. = " + bolRsltFlag);

      if(!bolRsltFlag)
      {
        arrResult[0] = false;
        arrResult[1] = "Postgress database is not running. Start database using /etc/init.d/postgresql start";
      }
      else
      {
        arrResult[0] = true;
        arrResult[1] = "Postgress database is running.";
      }

      return arrResult;
    }
    catch (Exception e)
    {
      arrResult[0] = false;
      arrResult[1] = "Error in restart Data base";
      Log.stackTraceLog(className, "checkStatusPostgreSQL", "", "", "Exception - ", e);
      return arrResult;
    }
  }

  public Object[] restartPostgreSQL()
  {
    Log.debugLog(className, "restartPostgreSQL", "", "", "Method called.");

    Object[] arrResult = new Object[2];
    try
    {
      strCmdName = "/etc/init.d/postgresql" ;
      Log.debugLog(className, "restartPostgreSQL", "", "", "Command Name = " + strCmdName);

      strCmdArgs = "restart";

      Log.debugLog(className, "restartPostgreSQL", "", "", "Command Argument = " + strCmdArgs);

      vecCmdOutput = new Vector();
      boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, CmdExec.SYSTEM_CMD, userName, "root");

      Log.debugLog(className, "restartPostgreSQL", "", "", "Checking Commnad executed successfully or not. = " + bolRsltFlag);

      if(!bolRsltFlag)
        arrResult[0] = false;
      else
        arrResult[0] = true;

      arrResult[1] = getDataInStringBuff(vecCmdOutput).toString();
      return arrResult;
    }
    catch (Exception e)
    {
      arrResult[0] = false;
      arrResult[1] = "Error in restart Data base";
      Log.stackTraceLog(className, "restartPostgreSQL", "", "", "Exception - ", e);
      return arrResult;
    }
  }

  public Object[] execute_nsi_migrate_tr_data()
  {
    Log.debugLog(className, "execute_nsi_migrate_tr_data", "", "", "Method called.");

    Object[] arrResult = new Object[2];
    try
    {
      strCmdName = NSI_MIGRATE_TR_DATA;
      Log.debugLog(className, "execute_nsi_migrate_tr_data", "", "", "Command Name = " + strCmdName);

      strCmdArgs = "--testrun" + " " + testRun;

      Log.debugLog(className, "execute_nsi_migrate_tr_data", "", "", "Command Argument = " + strCmdArgs);

      vecCmdOutput = new Vector();
      boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, "root");

      Log.debugLog(className, "execute_nsi_migrate_tr_data", "", "", "Checking Commnad executed successfully or not. = " + bolRsltFlag);

      if(!bolRsltFlag)
        arrResult[0] = false;
      else
        arrResult[0] = true;

      arrResult[1] = getDataInStringBuff(vecCmdOutput).toString();

      return arrResult;
    }

    catch (Exception e)
    {
      arrResult[0] = false;
      arrResult[1] = "Error in migrate test run";
      Log.stackTraceLog(className, "execute_nsi_migrate_tr_data", "", "", "Exception - ", e);
      return arrResult;
    }
  }

  public String[][] execute_nsi_get_page_dump(String cmdArg, DrillDownBreadCrumb downBreadCrumb)
  {
    return execute_nsi_get_page_dump(cmdArg, downBreadCrumb, false);
  }

  public String[][] execute_nsi_get_page_dump(String cmdArg, DrillDownBreadCrumb downBreadCrumb, boolean isNewFormat)
  {
    Log.debugLog(className, "execute_nsi_get_page_dump", "", "", "Method called. = " + cmdArg + "isNewFormat =" + isNewFormat);
    String arrResult[][] = null;
    try
    {
      if(downBreadCrumb.getFilterArument().equals("") || !downBreadCrumb.getFilterArument().equals(cmdArg))
      {
        if(isNewFormat)
	  strCmdName = "nsi_db_get_pagedump_data";
        else
          strCmdName = NSI_GET_PAGEDUMP;
         
        Log.debugLog(className, "execute_nsi_get_page_dump", "", "", "Command Name = " + strCmdName);

        strCmdArgs = " " + cmdArg;

        Log.debugLog(className, "execute_nsi_get_page_dump", "", "", "Command Argument = " + strCmdArgs);

        vecCmdOutput = new Vector();
        boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

        Log.debugLog(className, "execute_nsi_get_page_dump", "", "", "Checking Commnad executed successfully or not. = " + bolRsltFlag);
        if(!bolRsltFlag)
        {
          arrResult = new String[2][1];
          arrResult[0][0] = FAIL_QUERY;
          arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
          downBreadCrumb.setBreadCrumbData(arrResult);
          return arrResult;
        }
        
        if(isNewFormat)
        {
          arrResult = new String[vecCmdOutput.size()][];
          
          for(int k = 0; k < vecCmdOutput.size() ; k++)
            arrResult[k] = vecCmdOutput.get(k).toString().split(",");
        }
        else       
          arrResult = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "-");
        
        downBreadCrumb.setBreadCrumbData(arrResult);
      }
      else
      {
        arrResult = downBreadCrumb.getBreadCrumbData();
        downBreadCrumb.setBreadCrumbData(arrResult);
      }
      return arrResult;
    }

    catch (Exception e)
    {
      e.printStackTrace();
      arrResult = new String[2][1];
      arrResult[0][0] = FAIL_QUERY;
      arrResult[1][0] = "No records are found";
      Log.stackTraceLog(className, "execute_ndi_get_fp_data", "", "", "Exception - ", e);
      return arrResult;
    }
  }
  public Object[] execute_nsu_import()
  {
    Log.debugLog(className, "execute_nsu_import", "", "", "Method called.");

    Object[] arrResult = new Object[2];
    try
    {
      strCmdName = NSU_IMPORT ;
      Log.debugLog(className, "execute_nsu_import", "", "", "Command Name = " + strCmdName);

      strCmdArgs = testRun;

      Log.debugLog(className, "execute_nsu_import", "", "", "Command Argument = " + strCmdArgs);

      vecCmdOutput = new Vector();
      boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

      Log.debugLog(className, "execute_nsu_import", "", "", "Checking Commnad executed successfully or not. = " + bolRsltFlag);

      if(!bolRsltFlag)
        arrResult[0] = false;
      else
        arrResult[0] = true;

      arrResult[1] = getDataInStringBuff(vecCmdOutput).toString();
      return arrResult;
    }
    catch (Exception e)
    {
      arrResult[0] = false;
      arrResult[1] = "Error in import test run";
      Log.stackTraceLog(className, "execute_nsu_import", "", "", "Exception - ", e);
      return arrResult;
    }
  }

  //Function to convert Vector into 2d array.
  public static String[][]  getVectorDataInArray(Vector vecRecs)
  {
    String strRecord = null;          //variable to store the vector row.
    String arrDataValues[][] = null;  //2D array used to store vector data.
    int numFlds;                      //Store number of fields/columns.
    int numRows;                      //Store number of rows.
    int messageColIndex = -1;            //used to track the message having delimiters
    String arrSplitData[] = null;       //Temporary array to store the row data.

    try
    {
      //checking for empty or null vector.
      if((vecRecs == null) || (vecRecs.size() == 0))
      {
        Log.errorLog(className, "getRecFlds", "", "", "Vector is null or size is 0");
        return null;
      }
      Log.debugLog(className, "", "", "Vector size = " + vecRecs.size(),"");

      numRows = vecRecs.size();
      strRecord = new String((vecRecs.elementAt(0).toString())); //taking first record from vector.
      arrSplitData = rptUtilsBean.split(strRecord, "|");       //split header data.

      for(int i = 0; i < arrSplitData.length; i++)
      {
        if(arrSplitData[i].trim().equals("message"))
        {
           messageColIndex = i;                  //Tracking message column index.
           break;
        }
      }
      numFlds = arrSplitData.length;
      arrDataValues = new String[numRows][numFlds]; //declare array with vector size.
      String strTemp = "";                          //for storing piped message data.

      for(int i = 0; i < vecRecs.size(); i++)
      {
        strRecord = new String((vecRecs.elementAt(i).toString()));
        strRecord = "\t" + strRecord;
        arrSplitData = rptUtilsBean.split(strRecord, "|");
        strTemp = "";
        boolean isFirstMasg = true;
        for(int j = 0; j < arrSplitData.length; j++)
        {
          if(messageColIndex > -1 && j >= messageColIndex )
          {
            if(isFirstMasg)
            {
              isFirstMasg = false;
              strTemp = strTemp + arrSplitData[j];  //regenerate message.
            }
             else
              strTemp = strTemp + "|" + arrSplitData[j];
          }
          else
           arrDataValues[i][j] = arrSplitData[j];
        }
        if(messageColIndex > -1)
          arrDataValues[i][messageColIndex] = strTemp.trim();
      }
      return arrDataValues;
    }

    catch (Exception e)
    {
      Log.errorLog(className, "getRecFlds", "", "", "Exception - " + e);
    }
    return null;
  }

  /*
   * This method execute ndi_db_get_app_logs object query
   *
   * Arguments
   * cmdArg: arguments for query
   * sortColumnType: column type sorting (String, Number, Date)
   * defaultSorting: Sorting indicator
   * totalCount: give no. of records
   *
   */
    public String[][] execute_get_app_logs(String cmdArg, StringBuffer sortColumnType, StringBuffer defaultSorting, StringBuffer totalRows, String currlimit, String currOffset, DrillDownBreadCrumb downBreadCrumb)
    {
      Log.debugLog(className, "execute_get_app_logs", "", "", "Method called. = " + cmdArg);
      String arrResult[][] = null;
      try
      {
        if(downBreadCrumb.getFilterArument().equals("") || (!downBreadCrumb.getOffset().equals(currOffset) || !downBreadCrumb.getLimit().equals(currlimit) || !downBreadCrumb.getFilterArument().trim().equals(cmdArg.trim())))
        {
          long startTimeStamp =  System.currentTimeMillis();
          String limitOffSet = " --limit " + currlimit + " --offset " + currOffset;
          strCmdName = NSI_DB_GET_APP_LOGS;
          Log.debugLog(className, "execute_get_app_logs", "", "", "Command Name = " + strCmdName);

          vecCmdOutput = new Vector();
          //boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

          //long endTimeStamp =  System.currentTimeMillis();
          //Log.debugLog(className, "execute_get_app_logs", "", "", "Total Time taken to get count = " + (endTimeStamp - startTimeStamp));

          //creating inner class object up to 2 length to get count and data
          getCommandOutputObj = new getCommandOutput[2];
          Thread[] threadArr = new Thread[2];

          for(int i = 0; i < getCommandOutputObj.length; i++)
          {
            //this boolean will give all thread started or not default false
            if(i == getCommandOutputObj.length - 1)
              isAliveAll = true;

            //command with limit offset to get data
            if(i == 0)
              strCmdArgs = " --testrun " + testRun + " " + cmdArg + " " + GET_COUNT_OPTION + " " + DEFAULT_VALUE_GET_COUNT;
            else
              strCmdArgs = " --testrun " + testRun + " " + cmdArg + limitOffSet;

            Log.debugLog(className, "execute_get_app_logs", "", "", "Command Argument = " + strCmdArgs);
            //creating class object array
            getCommandOutputObj[i] = new getCommandOutput(i, strCmdName, strCmdArgs, this);

            //if thread is enable
            if(IS_EXECUTE_QUERY_IN_THREAD_ENABLE.equals("1"))
            {
              threadArr[i] = new Thread(getCommandOutputObj[i], "Query - " + i);

              //adding uncaughtException Handler
              setUncaughtExceptionHandlerDDR(threadArr[i]);

              //Starting thread
              threadArr[i].start();
            }
            else //if threading is not enable
              getCommandOutputObj[i].getResultByCmdToGetOutput();
          }

          //wait untill all thread started
          //if(!isAliveAll && IS_EXECUTE_QUERY_IN_THREAD_ENABLE.equals("1"))
           // wait();

          // notify all thread
          //checking thread is running or not
          //All thread are not alive it stops all thread
          waitForQueryRunThreads(threadArr);

          long endTimeStamp =  System.currentTimeMillis();

          Log.debugLog(className, "execute_get_app_logs", "", "", "Total Time taken = " + (endTimeStamp - startTimeStamp));

          vecCmdOutput = getCommandOutputObj[0].getQueryOutput();
          if(!getCommandOutputObj[0].getQueryStatus())
          {
            totalRows.append(0);
            defaultSorting.append("0");
            arrResult = new String[2][1];
            arrResult[0][0] = FAIL_QUERY;
            arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
            downBreadCrumb.setBreadCrumbData(arrResult);
            return arrResult;
          }
          else
          {
            totalRows.append(vecCmdOutput.get(1).toString().trim());
            //totalRows.append(10);
          }

          //startTimeStamp =  System.currentTimeMillis();

          //strCmdArgs = " --testrun " + testRun + " " + cmdArg + limitOffSet;

          //Log.debugLog(className, "execute_get_app_logs", "", "", "Command Argument = " + strCmdArgs);

          vecCmdOutput = new Vector();
          //bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

          //endTimeStamp =  System.currentTimeMillis();
          //Log.debugLog(className, "execute_get_app_logs", "", "", "Total Time taken to get count = " + (endTimeStamp - startTimeStamp));

          //Log.debugLog(className, "execute_get_app_logs", "", "", "Checking Commnad executed successfully or not. = " + bolRsltFlag);

          vecCmdOutput = getCommandOutputObj[1].getQueryOutput();

          if(!getCommandOutputObj[1].getQueryStatus())
          {
            totalRows.append(0);
            defaultSorting.append("0");
            arrResult = new String[2][1];
            arrResult[0][0] = FAIL_QUERY;
            arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
            downBreadCrumb.setBreadCrumbData(arrResult);
            return arrResult;
          }
          arrResult = getVectorDataInArray(vecCmdOutput);
          downBreadCrumb.setBreadCrumbData(arrResult);
        }
        else
        {
          arrResult = downBreadCrumb.getBreadCrumbData();
          totalRows.append(downBreadCrumb.getTotalRecord());
          downBreadCrumb.setBreadCrumbData(arrResult);
        }

        String arrSortColumnType[] = new String[arrResult[0].length];

        //Default fill sorting Type Number
        Arrays.fill(arrSortColumnType, "String");

        String sortColName = "";
        mapSort.put("flowpathinstance", "None");
        mapSort.put("count", "Number");
        mapSort.put("date", "Date");
        mapSort.put("methodname", "String");
        mapSort.put("filename", "String");
        mapSort.put("linenumber", "Number");
        mapSort.put("summary", "String");
        mapSort.put("severity", "String");
        mapSort.put("TierName", "String");
        mapSort.put("ServerName", "String");
        mapSort.put("AppName", "String");
        mapSort.put("transactionname", "String");
        mapSort.put("pagename", "String");
        mapSort.put("urlname", "String");
        mapSort.put("sessionname", "String");

        mapDataBaseSort.put("date ", "date");
        mapDataBaseSort.put("severity", "severity");
        mapDataBaseSort.put("flowpathinstance", "fpinstance");
        mapDataBaseSort.put("tier", "TierName");
        mapDataBaseSort.put("server", "ServerName");
        mapDataBaseSort.put("app", "AppName");
        mapDataBaseSort.put("transaction", "transactionname");
        mapDataBaseSort.put("page", "pagename");
        mapDataBaseSort.put("url", "urlname");
        mapDataBaseSort.put("session", "sessionname");

        String[] sortDataBase = rptUtilsBean.split(drillDownReportData.getOrderBy(), ",");

        if(sortDataBase != null && sortDataBase.length > 0)
        {
          if(mapDataBaseSort.containsKey(sortDataBase[0].trim().toLowerCase()))
          {
            sortColName = mapDataBaseSort.get(sortDataBase[0].toLowerCase()).toString();
          }
        }

        for(int i = 0; i < arrResult[0].length; i++)
        {
          //setting column type index and if flag false skip column to sorting
          boolean bolFlag = setColumnTypeIndex(arrResult[0][i].toString().trim(), i);

          if(!bolFlag && !arrResult[0][i].contains("Name"))
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

        Log.debugLog(className, "execute_get_app_logs", "", "", "Column type for sorting = " + sortColumnType);

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
        Log.stackTraceLog(className, "execute_get_app_logs", "", "", "Exception - ", e);
        return arrResult;
      }
    }

    //Method to get Query Type and Query Value.
    public static String getQueryTypeValue(String valueType, String queryTypeValue)
    {
      String queryType = "";
      String arrForQueryType[] = {"Unknown", "Select", "Insert", "Update", "Delete", "Create", "Drop"};
      try
      {
        if(valueType.equals("QueryValue"))
        {
          for(int i = 0; i < arrForQueryType.length; i++)
          {
            if(arrForQueryType[i].equalsIgnoreCase(queryTypeValue))
              return i+"";
          }
        }
        else if(valueType.equals("QueryName") && Integer.parseInt(queryTypeValue) <= 6 && Integer.parseInt(queryTypeValue) >= 0)
          return   arrForQueryType[Integer.parseInt(queryTypeValue)];
      }
      catch(Exception e)
      {
        return queryTypeValue;
      }
      return queryType;
    }

    /*
     * This method execute execute_ndi_db_get_query_data object query
     *
     * Arguments
     * cmdArg: arguments for query
     * sortColumnType: column type sorting (String, Number, Date)
     * defaultSorting: Sorting indicator
     * totalCount: give no. of records
     *
     */
      public String[][] execute_ndi_db_get_query_data(String cmdArg, StringBuffer sortColumnType, StringBuffer defaultSorting, StringBuffer totalRows, StringBuffer totalQueryCount, StringBuffer totalAvg, String currlimit, String currOffset, DrillDownBreadCrumb downBreadCrumb)
      {
    	boolean isDBRequestGroupByTransactionOnly = false; 
        Log.debugLog(className, "execute_get_ndi_db_get_query_data", "", "", "Method called. = " + cmdArg);
        String arrResult[][] = null;
        try
        {
          if(downBreadCrumb.getFilterArument().equals("") || (!downBreadCrumb.getOffset().equals(currOffset) || !downBreadCrumb.getLimit().equals(currlimit) || !downBreadCrumb.getFilterArument().trim().equals(cmdArg.trim())))
          {
            long startTimeStamp =  System.currentTimeMillis();
            String limitOffSet = " --limit " + currlimit + " --offset " + currOffset;
            
            //As Mantis 559 - This is a new enhancement when DBRequest is grouped by transaction   
            isDBRequestGroupByTransactionOnly = isDBRequestOnlyGroupByTransaction(cmdArg); 
            
            if(isDBRequestGroupByTransactionOnly)
                strCmdName = NDI_DB_GET_QUERY_DATA_FOR_TRANSACTION;             
            else
                strCmdName = NDI_DB_GET_QUERY_DATA;
            
            Log.debugLog(className, "execute_ndi_db_get_query_data", "", "", "Command Name = " + strCmdName);

            vecCmdOutput = new Vector();
            //boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

            //long endTimeStamp =  System.currentTimeMillis();
            //Log.debugLog(className, "execute_ndi_db_get_query_data", "", "", "Total Time taken to get count = " + (endTimeStamp - startTimeStamp));

          //creating inner class object up to 2 length to get count and data
            getCommandOutputObj = new getCommandOutput[2];
            Thread[] threadArr = new Thread[2];

            for(int i = 0; i < getCommandOutputObj.length; i++)
            {
              //this boolean will give all thread started or not default false
              if(i == getCommandOutputObj.length - 1)
                isAliveAll = true;

              //command with limit offset to get data
              if(i == 0)
                strCmdArgs = " --testrun " + testRun + " " + cmdArg + " " + GET_COUNT_OPTION + " " + DEFAULT_VALUE_GET_COUNT;
              else
                strCmdArgs = " --testrun " + testRun + " " + cmdArg + limitOffSet;

              Log.debugLog(className, "execute_get_app_logs", "", "", "Command Argument = " + strCmdArgs);
              //creating class object array
              getCommandOutputObj[i] = new getCommandOutput(i, strCmdName, strCmdArgs, this);

              //if thread is enable
              if(IS_EXECUTE_QUERY_IN_THREAD_ENABLE.equals("1"))
              {
                threadArr[i] = new Thread(getCommandOutputObj[i], "Query - " + i);

                //adding uncaughtException Handler
                setUncaughtExceptionHandlerDDR(threadArr[i]);

                //Starting thread
                threadArr[i].start();
              }
              else //if threading is not enable
                getCommandOutputObj[i].getResultByCmdToGetOutput();
            }

            //wait untill all thread started
           // if(!isAliveAll && IS_EXECUTE_QUERY_IN_THREAD_ENABLE.equals("1"))
            //  wait();

            // notify all thread
            //checking thread is running or not
            //All thread are not alive it stops all thread
            waitForQueryRunThreads(threadArr);

            long endTimeStamp =  System.currentTimeMillis();

            Log.debugLog(className, "execute_ndi_db_get_query_data", "", "", "Total Time taken = " + (endTimeStamp - startTimeStamp));

            vecCmdOutput = getCommandOutputObj[0].getQueryOutput();
            if(!getCommandOutputObj[0].getQueryStatus())
            {
              totalRows.append(0);
              totalQueryCount.append(0);
              totalAvg.append(0);

              defaultSorting.append("0");
              arrResult = new String[2][1];
              arrResult[0][0] = FAIL_QUERY;
              arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
              downBreadCrumb.setBreadCrumbData(arrResult);
              return arrResult;
            }
            else
            {
              String arrCounts[] = rptUtilsBean.split(vecCmdOutput.get(1).toString().trim(), "|");
              totalRows.append(arrCounts[0]);
              if(arrCounts.length > 2)
              {
                totalQueryCount.append(arrCounts[1]);
                totalAvg.append(arrCounts[2]);
                //totalRows.append(10);
              }
            }

            //startTimeStamp =  System.currentTimeMillis();

            //strCmdArgs = " --testrun " + testRun + " " + cmdArg + limitOffSet;

            //Log.debugLog(className, "execute_ndi_db_get_query_data", "", "", "Command Argument = " + strCmdArgs);

            vecCmdOutput = new Vector();
            //bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

            //endTimeStamp =  System.currentTimeMillis();
            //Log.debugLog(className, "execute_ndi_db_get_query_data", "", "", "Total Time taken to get count = " + (endTimeStamp - startTimeStamp));

            //Log.debugLog(className, "execute_ndi_db_get_query_data", "", "", "Checking Commnad executed successfully or not. = " + bolRsltFlag);

            vecCmdOutput = getCommandOutputObj[1].getQueryOutput();

            if(!getCommandOutputObj[1].getQueryStatus())
            {
              totalRows.append(0);
              totalQueryCount.append(0);
              totalAvg.append(0);
              defaultSorting.append("0");
              arrResult = new String[2][1];
              arrResult[0][0] = FAIL_QUERY;
              arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
              downBreadCrumb.setBreadCrumbData(arrResult);
              return arrResult;
            }
            arrResult = getVectorDataInArray(vecCmdOutput);
            downBreadCrumb.setBreadCrumbData(arrResult);
          }
          else
          {
            arrResult = downBreadCrumb.getBreadCrumbData();
            totalRows.append(downBreadCrumb.getTotalRecord());
            totalQueryCount.append(downBreadCrumb.getTotalRecordSum());
            totalAvg.append(downBreadCrumb.getTotalAvg());
            downBreadCrumb.setBreadCrumbData(arrResult);
          }

          String arrSortColumnType[] = new String[arrResult[0].length];

          //Default fill sorting Type Number
          Arrays.fill(arrSortColumnType, "String");
          String sortColName = "";
          //mapSort.put("flowpathinstance", "None");
          mapSort.put("count", "Number");
          mapSort.put("sqlbegintimestamp", "Date");
          mapSort.put("querytype", "String");
          mapSort.put("sqlquery", "String");
          mapSort.put("sqlexectime", "Number");
          mapSort.put("avg", "DecimalNum");
          mapSort.put("min", "DecimalNum");
          mapSort.put("max", "DecimalNum");
          mapSort.put("variance", "DecimalNum");

          mapSort.put("tiername", "String");
          mapSort.put("servername", "String");
          mapSort.put("appname", "String");

          mapSort.put("transactionname", "String");
          mapSort.put("pagename", "String");
          mapSort.put("urlname", "String");
          mapSort.put("sessionname", "String");

          mapDataBaseSort.put("fpinstance ", "flowpathinstance");
          mapDataBaseSort.put("query_type", "querytype");
          mapDataBaseSort.put("exec_time", "sqlexectime");
          mapDataBaseSort.put("start_time_stamp", "sqlbegintimestamp");
          mapDataBaseSort.put("exec_time_desc", "sqlexectime");
          mapDataBaseSort.put("tier", "tiername");
          mapDataBaseSort.put("server", "servername");
          mapDataBaseSort.put("app", "appname");
          mapDataBaseSort.put("transaction", "transactionname");
          mapDataBaseSort.put("page", "pagename");
          mapDataBaseSort.put("url", "urlname");
          mapDataBaseSort.put("session", "sessionname");

          String[] sortDataBase = rptUtilsBean.split(drillDownReportData.getOrderBy(), ",");

          if(sortDataBase != null && sortDataBase.length > 0)
          {
            if(mapDataBaseSort.containsKey(sortDataBase[0].trim().toLowerCase()))
            {
              sortColName = mapDataBaseSort.get(sortDataBase[0].toLowerCase()).toString();
            }
          }

          for(int i = 0; i < arrResult[0].length; i++)
          {
            //setting column type index and if flag false skip column to sorting
            boolean bolFlag = setColumnTypeIndex(arrResult[0][i].toString().trim(), i);

            if(!bolFlag && !arrResult[0][i].contains("name"))
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

          Log.debugLog(className, "execute_ndi_db_get_query_data", "", "", "Column type for sorting = " + sortColumnType);

          sortColumnType.append(sortColumn);

          return arrResult;
        }
        catch (Exception e)
        {
          totalRows.append(0);
          totalQueryCount.append(0);
          totalAvg.append(0);
          defaultSorting.append("0");
          e.printStackTrace();
          arrResult = new String[2][1];
          arrResult[0][0] = FAIL_QUERY;
          arrResult[1][0] = "No records are found";
          Log.stackTraceLog(className, "execute_ndi_db_get_query_data", "", "", "Exception - ", e);
          return arrResult;
        }
      }
      
      /**
       * This Method checks whether DBRequest is only grouped by transaction
       * @param strCmdArgs
       * @return
       */
      public boolean isDBRequestOnlyGroupByTransaction(String arguments) 
      {
    	  //Checking Basic Criteria
    	  if(arguments.indexOf("--group transaction ") == -1)
    		return false;
    	  else
    	  {
    		  if(arguments.indexOf("--url") != -1)
    		    return false;
    		  else if(arguments.indexOf("--page") != -1)
    			return false;
    		  else if(arguments.indexOf("--script") != -1)
    			return false;
    		  else if(arguments.indexOf("--order") != -1)
    			return false;
    		  else if(arguments.indexOf("--http") != -1)
    			return false;
    		  else if(arguments.indexOf("--exc") != -1)
    			return false;
    		  else if(arguments.indexOf("--location") != -1)
    		    return false;
    		  else if(arguments.indexOf("--access") != -1)
    			return false;
    		  else if(arguments.indexOf("--browser") != -1)
    			return false;
    		  else if(arguments.indexOf("--min_methods") != -1)
    			return false;
    		  else if(arguments.indexOf("--query") != -1)
    		    return false;
    		  else if(arguments.indexOf("--starttime") != -1)
    			return false;
    		  else if(arguments.indexOf("--abs_starttime") != -1)
      			return false;
    		  else if(arguments.indexOf("--phaseidx") != -1)
    		    return false;
    		  else if(arguments.indexOf("--resptimeqmode") != -1)
    			return false;
    		  else if(arguments.indexOf("--responsetime") != -1)
    			return false;
    		  else
    			 return true;
    	  }	
      }


/**
 *
 * @author cavisson user
 *
 */
  class getCommandOutput implements Runnable
  {
    private final String className = "getCommandOutput";
    int threadNum = -1;
    String strCmd = "";
    String strArgs = "";
    DrillDownExecuteQuery downExecuteQuery = null;

    private Vector vecData = new Vector();
    private boolean bolRsltFlag = false;

    public getCommandOutput(int threadNum, String strCmd, String strArgs, DrillDownExecuteQuery downExecuteQuery)
    {
      Log.debugLog(className, "getCommandOutput", "", "", "Thread Number = " + threadNum);

      this.threadNum = threadNum;
      this.strCmd = strCmd;
      this.strArgs = strArgs;
      this.downExecuteQuery = downExecuteQuery;
    }

    public void run()
    {
      try
      {
        String threadName = Thread.currentThread().getName();

        Log.debugLog(className, "run", "", "", "Thread Name = " + threadName);
        getResultByCmdToGetOutput();
        downExecuteQuery.notifyAll(); // Notify the parent object
      }
      catch (Exception e) {
	      //e.printStackTrace();
        // TODO: handle exception
      }
    }

    public void getResultByCmdToGetOutput()
    {
      Log.debugLog(className, "getResultByCmdToGetOutput", "", "", "Method Called");
      bolRsltFlag = cmdExec.getResultByCommand(vecData, strCmd, strArgs, 0, userName, null);
      //System.out.println("bolRsltFlag = "+bolRsltFlag);
    }

    public Vector getQueryOutput()
    {
      return vecData;
    }

    public boolean getQueryStatus()
    {
      return bolRsltFlag;
    }
  }

  //execute_nsi_db_get_flowpath_instance_data
  public String[][] execute_nsi_db_get_flowpath_instance_data(String cmdArg, StringBuffer sortColumnType, StringBuffer defaultSorting, StringBuffer totalRows, String currLimit, String currOffset, DrillDownBreadCrumb downBreadCrumb)
  {
    Log.debugLog(className, "execute_get_obj_instance_data", "", "", "Method called. = " + cmdArg);
    String arrResult[][] = null;
    try
    {
      long startTimeStamp =  System.currentTimeMillis();

      if(downBreadCrumb.getFilterArument().equals("") || (!downBreadCrumb.getOffset().equals(currOffset) || !downBreadCrumb.getLimit().equals(currLimit) || !downBreadCrumb.getFilterArument().equals(cmdArg)))
      {

        String limitOffset = " --limit " + currLimit + " --offset " + currOffset;
        strCmdName = NSI_DB_GET_FLOWPATH_SUMMARY_DATA;

        Log.debugLog(className, "execute_nsi_db_get_flowpath_instance_data", "", "", "Command Name = " + strCmdName);

        vecCmdOutput = new Vector();

        //creating inner class object up to 2 length to get count and data
        getCommandOutputObj = new getCommandOutput[2];
        Thread[] threadArr = new Thread[2];

        for(int i = 0; i < getCommandOutputObj.length; i++)
        {
          //this boolean will give all thread started or not default false
          if(i == getCommandOutputObj.length - 1)
            isAliveAll = true;

          //command with limit offset to get data
          if(i == 0)
            strCmdArgs = " --testrun " + testRun + " " + cmdArg + " " + GET_COUNT_OPTION + " " + DEFAULT_VALUE_GET_COUNT;
          else
            strCmdArgs = " --testrun " + testRun + " " + cmdArg + limitOffset;

          Log.debugLog(className, "execute_nsi_db_get_flowpath_instance_data", "", "", "Command Argument = " + strCmdArgs);

          //creating class object array
          getCommandOutputObj[i] = new getCommandOutput(i, strCmdName, strCmdArgs, this);

          //if thread is enable
          if(IS_EXECUTE_QUERY_IN_THREAD_ENABLE.equals("1"))
          {
            threadArr[i] = new Thread(getCommandOutputObj[i], "Query - " + i);

            //adding uncaughtException Handler
            setUncaughtExceptionHandlerDDR(threadArr[i]);

            //Starting thread
            threadArr[i].start();
          }
          else //if threading is not enable
            getCommandOutputObj[i].getResultByCmdToGetOutput();
        }

        //wait untill all thread started
        //if(!isAliveAll && IS_EXECUTE_QUERY_IN_THREAD_ENABLE.equals("1"))
        //  wait();

        // notify all thread
        //checking thread is running or not
        //All thread are not alive it stops all thread
        waitForQueryRunThreads(threadArr);

        long endTimeStamp =  System.currentTimeMillis();

        Log.debugLog(className, "execute_nsi_db_get_flowpath_instance_data", "", "", "Total Time taken = " + (endTimeStamp - startTimeStamp));

        vecCmdOutput = getCommandOutputObj[0].getQueryOutput();
        if(!getCommandOutputObj[0].getQueryStatus())
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
           //totalRows.append("100");
        }

        vecCmdOutput = new Vector();
        vecCmdOutput = getCommandOutputObj[1].getQueryOutput();

        if(!getCommandOutputObj[1].getQueryStatus())
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
        strCmdArgs = " --testrun " + testRun + " " + cmdArg + " " + GET_COUNT_OPTION + " " + DEFAULT_VALUE_GET_COUNT;
        
        if(strCmdArgs.trim().contains("urlsearchpattern"))
          totalRows.append(execute_ndi_db_get_fp_url_pattern_count(strCmdArgs));
        else
          totalRows.append(downBreadCrumb.getTotalRecord());
        downBreadCrumb.setBreadCrumbData(arrResult);
      }

      String arrSortColumnType[] = new String[arrResult[0].length];

      //Default fill sorting Type String
      Arrays.fill(arrSortColumnType, "String");

      int sortIndex = -1;
      String sortColName = "Average";

      if(drillDownReportData.getObjectType().equals("4"))
      {
        sortColName = "AvgFPDuration";
      }

      String[] sortOrderBy = rptUtilsBean.split(drillDownReportData.getOrderBy(), ",");
      String[] sortDataBase = rptUtilsBean.split(drillDownReportData.getSortUsingDataBaseQuery(), ",");

      if(sortDataBase != null && sortDataBase.length > 0)
      {
        if(mapDataBaseSort.containsKey(sortDataBase[0].trim().toLowerCase()))
        {
          sortColName = mapDataBaseSort.get(sortDataBase[0].toLowerCase()).toString();
        }
      }
      else if(sortOrderBy != null && sortOrderBy.length > 0)
      {
        if(sortOrderBy[0].trim().equals("url"))
          sortColName = "URLName";
        else if(sortOrderBy[0].trim().equals("avgfpduration") || sortOrderBy[0].trim().equals("avgfpduration_desc"))
          sortColName = "AvgFPDuration";
        else if(sortOrderBy[0].trim().equals("fpduration") || sortOrderBy[0].trim().equals("fpduration_desc"))
          sortColName = "FPDuration";
        else if(sortOrderBy[0].trim().equals("stime"))
          sortColName = "StartTime";
        else if(sortOrderBy[0].trim().equals("server"))
          sortColName = "ServerName";
        else if(sortOrderBy[0].trim().equals("tier"))
          sortColName = "TierName";
        else if(sortOrderBy[0].trim().equals("app"))
          sortColName = "AppName";
        else if(sortOrderBy[0].trim().equals("transaction"))
          sortColName = "TransactionName";
        else if(sortOrderBy[0].trim().equals("session"))
          sortColName = "SessionName";
        else if(sortOrderBy[0].trim().equals("page"))
          sortColName = "PageName";
        else if(sortOrderBy[0].trim().equals("generator"))
          sortColName = "GeneratorName";
      }

      for(int i = 0; i < arrResult[0].length; i++)
      {
        //setting column type index and if flag false skip column to sorting
        boolean bolFlag = setColumnTypeIndex(arrResult[0][i].toString().trim(), i);
        sortIndex++;

        if(!bolFlag)
          arrSortColumnType[i] = ",";
        else if(mapSort.containsKey(arrResult[0][i].toString().trim()) && bolFlag)
        {
          arrSortColumnType[i] = "," + mapSort.get(arrResult[0][i].toString().trim()).toString();
        }

        if(arrResult[0][i].toString().trim().equalsIgnoreCase(sortColName))
          defaultSorting.append(i);
      }

      if(defaultSorting.toString().trim().equals(""))
        defaultSorting.append("0");

      String sortColumn = rptUtilsBean.strArrayToStr(arrSortColumnType, ",");
      arrSortColumnType = rptUtilsBean.strToArrayData(sortColumn, ",");
      sortColumn = rptUtilsBean.strArrayToStr(arrSortColumnType, ",");

      Log.debugLog(className, "execute_nsi_db_get_flowpath_instance_data", "", "", "Column type for sorting = " + sortColumnType);
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
      Log.stackTraceLog(className, "execute_nsi_db_get_flowpath_instance_data", "", "", "Exception - ", e);
      return arrResult;
    }
  }

  /**
   * This Method runs a query to get the count of all URL w.r.t to Specified Pattern
   * @param strCmdArgs
   * @return
   */
  private String execute_ndi_db_get_fp_url_pattern_count(String strCmdArgs) 
  {
	  String strTotalRecords = "0|0|0";
	  Log.debugLog(className, "execute_ndi_db_get_fp_url_pattern_count", "", "", "Method called");
      try
      {
         strCmdName = NSI_DB_GET_FLOWPATH_SUMMARY_DATA;
         
         Log.debugLog(className, "execute_ndi_db_get_fp_url_pattern_count", "", "","Command Name With Arguments :" + strCmdName + strCmdArgs );
         
         vecCmdOutput = new Vector();
         
         boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);
         Log.debugLog(className, "execute_ndi_db_get_fp_url_pattern_count", "", "", "Executing Query. Flag After Execution :" + bolRsltFlag);
         
         if(!bolRsltFlag)
         {
           return strTotalRecords;
         }
         strTotalRecords = vecCmdOutput.get(1).toString().trim();
         Log.debugLog(className, "execute_ndi_db_get_fp_url_pattern_count", "", "", "Returning Total Records : " + strTotalRecords);
         return strTotalRecords;
      }
      catch(Exception e)
      {
	     Log.stackTraceLog(className, "execute_ndi_db_get_fp_url_pattern_count", "", "", "Exception - ", e);
         return strTotalRecords;
      }
  }

//execute_ndi_db_get_service_method_timing
  /**
   * This Method gets data for Sevice Method Timing Screen.
   */
  public String[][] execute_ndi_db_get_service_method_timing(String cmdArg, StringBuffer sortColumnType, StringBuffer defaultSorting, StringBuffer totalRows, String currLimit, String currOffset, DrillDownBreadCrumb downBreadCrumb)
  {
    Log.debugLog(className, "execute_ndi_db_get_service_method_timing", "", "", "Method called. = " + cmdArg);
    String arrResult[][] = null;
    try
    {
      long startTimeStamp =  System.currentTimeMillis();

      if(downBreadCrumb.getFilterArument().equals("") || (!downBreadCrumb.getOffset().equals(currOffset) || !downBreadCrumb.getLimit().equals(currLimit) || !downBreadCrumb.getFilterArument().equals(cmdArg)))
      {

        String limitOffset = " --limit " + currLimit + " --offset " + currOffset;
        strCmdName = NDI_DB_GET_SERVICE_METHOD_TIMING;

        Log.debugLog(className, "execute_ndi_db_get_service_method_timing", "", "", "Command Name = " + strCmdName);

        vecCmdOutput = new Vector();

        //creating inner class object up to 2 length to get count and data
        getCommandOutputObj = new getCommandOutput[2];
        Thread[] threadArr = new Thread[2];

        for(int i = 0; i < getCommandOutputObj.length; i++)
        {
          //this boolean will give all thread started or not default false
          if(i == getCommandOutputObj.length - 1)
            isAliveAll = true;

          //command with limit offset to get data
          if(i == 0)
            strCmdArgs = " --testrun " + testRun + " " + cmdArg + " " + GET_COUNT_OPTION + " " + DEFAULT_VALUE_GET_COUNT;
          else
            strCmdArgs = " --testrun " + testRun + " " + cmdArg + limitOffset;

          Log.debugLog(className, "execute_ndi_db_get_service_method_timing", "", "", "Command Argument = " + strCmdArgs);

          //creating class object array
          getCommandOutputObj[i] = new getCommandOutput(i, strCmdName, strCmdArgs, this);

          //if thread is enable
          if(IS_EXECUTE_QUERY_IN_THREAD_ENABLE.equals("1"))
          {
            threadArr[i] = new Thread(getCommandOutputObj[i], "Query - " + i);

            //adding uncaughtException Handler
            setUncaughtExceptionHandlerDDR(threadArr[i]);

            //Starting thread
            threadArr[i].start();
          }
          else //if threading is not enable
            getCommandOutputObj[i].getResultByCmdToGetOutput();
        }

        // notify all thread
        //checking thread is running or not
        //All thread are not alive it stops all thread
        waitForQueryRunThreads(threadArr);

        long endTimeStamp =  System.currentTimeMillis();

        Log.debugLog(className, "execute_ndi_db_get_service_method_timing", "", "", "Total Time taken = " + (endTimeStamp - startTimeStamp));

        vecCmdOutput = getCommandOutputObj[0].getQueryOutput();
        if(!getCommandOutputObj[0].getQueryStatus())
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
           //totalRows.append("100");
        }

        vecCmdOutput = new Vector();
        vecCmdOutput = getCommandOutputObj[1].getQueryOutput();

        if(!getCommandOutputObj[1].getQueryStatus())
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
      if(defaultSorting.toString().trim().equals(""))
        defaultSorting.append("0");

      String sortColumn = rptUtilsBean.strArrayToStr(arrSortColumnType, ",");
      arrSortColumnType = rptUtilsBean.strToArrayData(sortColumn, ",");
      sortColumn = rptUtilsBean.strArrayToStr(arrSortColumnType, ",");

      Log.debugLog(className, "execute_ndi_db_get_service_method_timing", "", "", "Column type for sorting = " + sortColumnType);
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
      Log.stackTraceLog(className, "execute_ndi_db_get_service_method_timing", "", "", "Exception - ", e);
      return arrResult;
    }
  }

  //parameters tier, server and apps mapping with id.
  public String getFlowpathMetaDataMappedIds(String strArgs, String tierName, String serverName, String appName)
  {
	String idVal = "NA";
	try
	{
	  String arrDataList[][] = getOtherObjects("--object "+strArgs);
	  if(arrDataList == null)
		return idVal;

	 /* int index = 1;
	  if(strArgs.trim().equals("11"))
		index = 3;
	  else if(strArgs.trim().equals("12"))
	    index = 2;*/

	  //mapping values and get id.
	  for(int i = 0; i < arrDataList.length; i++)
	  {
		String arrData[] = arrDataList[i][0].split(":");

		if(arrData != null)
		{
		  if(strArgs.trim().equals("11"))
		  {
			if((arrData[1].trim().equals(tierName)) && (arrData[2].trim().equals(serverName)) && (arrData[3].trim().equals(appName)))
			  return arrData[0];
		  }
		  else if(strArgs.trim().equals("12"))
		  {
		    if((arrData[1].trim().equals(tierName)) && (arrData[2].trim().equals(serverName)))
			  return arrData[0];
		  }
		  else if(strArgs.trim().equals("13"))
		  {
		    if(arrData[1].trim().equals(tierName))
			  return arrData[0];
		  }
		}
	  }
	  return idVal;
	}
	catch(Exception e)
	{
	  e.printStackTrace();
	  return idVal;
	}
  }


  //For parsing stack Trace Data.
  public Vector<String[]> getParsedData(String threadStackTrace)
  {
    Vector<String[]> vectParsedData = new Vector<String[]>();
    try
    {
      String[] arrThreadStackTrace = rptUtilsBean.strToArrayData(threadStackTrace, "|");
      for (int i = 0; i < arrThreadStackTrace.length; i++)
      {
        String[] arrData = new String[4];
        // used for java class name
        String sourceFile = "";
        // used for java method name
        String methodName = "";
        // used for package name
        String classNameToShow = "";
        // used for line number
        String lineNumber = "";
        // used for stack trace line
        String stackLine = arrThreadStackTrace[i];
        int index = stackLine.indexOf("(");
        String tempLineToParse = "";
        if (index == -1)
        {
          continue;
        }
        else
        {
          sourceFile = stackLine.substring((index + 1), (stackLine.length() - 1));
          tempLineToParse = stackLine.substring(0, index);
          if (sourceFile.equals("Native Method"))
          {
            sourceFile = "-";
            lineNumber = "-";
          }
          else
          {
            int lineNumberIndex = sourceFile.indexOf(":");
            if (lineNumberIndex != -1)
            {
              lineNumber = sourceFile.substring((lineNumberIndex + 1), (sourceFile.length()));
              sourceFile = sourceFile.substring(0, lineNumberIndex);
            }
            else
            {
              lineNumber = "-";
              if (sourceFile.trim().equals(""))
                sourceFile = "-";
            }
          }
        }

        int indexMethod = tempLineToParse.lastIndexOf(".");
        if (indexMethod == -1)
          continue;
        else
        {
          methodName = tempLineToParse.substring((indexMethod + 1), tempLineToParse.length());
          classNameToShow = tempLineToParse.substring(0, indexMethod);
        }

        // used for class name
        arrData[0] = classNameToShow;
        // used for method name
        arrData[1] = methodName;
        // used for line number
        arrData[2] = lineNumber;
        // used for source file
        arrData[3] = sourceFile;

        vectParsedData.add(arrData);
      }
    }
    catch (Exception ex)
    {

      Log.errorLog(className, "getParsedData", "", "", "Exception - " + ex);
      ex.printStackTrace();
    }

    return vectParsedData;
  }

//For getting hotspot data.
  public String[][] execute_nsi_db_get_flowpath_thread_dump_data(String cmdArg, DrillDownBreadCrumb downBreadCrumb)
  {
    Log.debugLog(className, "execute_nsi_db_get_flowpath_thread_dump_data", "", "", "Method called. = " + cmdArg);
    String arrResult[][] = null;
    Vector<String[]> vecData = new Vector<String[]>();
    try
    {
      if(downBreadCrumb.getFilterArument().equals("") || !downBreadCrumb.getFilterArument().equals(cmdArg))
      {
        strCmdName = NDI_DB_GET_THREAD_DUMP_DATA;
        Log.debugLog(className, "execute_nsi_db_get_flowpath_thread_dump_data", "", "", "Command Name = " + strCmdName);

        strCmdArgs =  "--testrun " + testRun + " " + cmdArg;

        Log.debugLog(className, "execute_nsi_db_get_flowpath_thread_dump_data", "", "", "Command Argument = " + strCmdArgs);

        vecCmdOutput = new Vector();
        boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);

        Log.debugLog(className, "execute_nsi_db_get_flowpath_thread_dump_data", "", "", "Checking Commnad executed successfully or not. = " + bolRsltFlag);
        if(!bolRsltFlag)
        {
          //vecData.add(new String[]{"No Record Available"});
          arrResult = new String[1][1];
          arrResult[0][0] = "No Record Available";
          downBreadCrumb.setBreadCrumbData(arrResult);
          return arrResult;
        }

        //arrResult = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", " ");
        //System.out.println("vecCmdOutput.get(1).toString() = "+vecCmdOutput.get(1).toString());
        vecData = getParsedData(vecCmdOutput.get(1).toString());
        //downBreadCrumb.setBreadCrumbVectorData(vecData);
        arrResult = new String[vecData.size()][11];
        for(int i = 0 ; i < vecData.size(); i++ )
        {
          arrResult[i] = vecData.get(i);
        }

        downBreadCrumb.setBreadCrumbData(arrResult);
      }
      else
      {
	//vecData = downBreadCrumb.getBreadCrumbVectorData();
	arrResult = downBreadCrumb.getBreadCrumbData();
        downBreadCrumb.setBreadCrumbData(arrResult);
      }
      return arrResult;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      arrResult = new String[1][1];
      arrResult[0][0] = "No Record Available";
      //vecData.add(new String[]{"No Record Available"});
      Log.stackTraceLog(className, "execute_nsi_db_get_flowpath_thread_dump_data", "", "", "Exception - ", e);
      return arrResult;
    }
  }

//encoding special charecters by html encoding.
public String encodeHTMLCharecters(String textString)
{
  final StringBuilder result = new StringBuilder();
  try
  {
    StringCharacterIterator iterator = new StringCharacterIterator(textString);
    char character = iterator.current();

    while (character != StringCharacterIterator.DONE)
    {
      if (character == '\"')
        addCharEntity(34, result);

      else if(character == '%')
        addCharEntity(37, result);

      else if(character == '#')
        addCharEntity(35, result);

      else if(character == '@')
        addCharEntity(64, result);

      else if(character == '$')
        addCharEntity(36, result);

      else if(character == '&')
        addCharEntity(38, result);

      else if(character == '<')
        addCharEntity(60, result);

      else if (character == '>')
        addCharEntity(62, result);

      else if (character == '\t')
        addCharEntity(9, result);

      else if (character == '\'')
        addCharEntity(39, result);

      else if (character == '*')
        addCharEntity(42, result);

      else if (character == '/')
        addCharEntity(47, result);

      else if (character == '?')
        addCharEntity(63, result);

      else if (character == '\\')
        addCharEntity(92, result);

      else if (character == '^')
        addCharEntity(94, result);

      else if (character == '`')
        addCharEntity(96, result);

      else if (character == '|')
        addCharEntity(124, result);

      else if (character == '~')
        addCharEntity(126, result);
      else
        result.append(character);

      character = iterator.next();
    }
  }
  catch(Exception e)
  {
	e.printStackTrace();
	Log.stackTraceLog(className, "encodeHTMLCharecters", "", "", "Exception in HTML Encoding = ", e);
  }
  return result.toString();
}

//Method for adding prefix for making html encoding chars.
private static void addCharEntity(Integer aIdx, StringBuilder aBuilder)
{
  String padding = "";
  if( aIdx <= 9 )
    padding = "00";
  else if( aIdx <= 99 )
   padding = "0";

  String number = padding + aIdx.toString();
  aBuilder.append("&#" + number + ";");
}

//Method for converting absolute time to relative time according to given timeStamp.
public Long getAbsoluteTimeStamp(String dateTimeStamp, String netstormTimeStamp)
{
  try
  {
    SimpleDateFormat trDateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
    Date userDate = trDateFormat.parse(dateTimeStamp);

    Date netstormDate = trDateFormat.parse(netstormTimeStamp);
    Long absoluteDateInMillies = (userDate.getTime() - netstormDate.getTime());

    if(absoluteDateInMillies < 0)
    return 0L;

    return absoluteDateInMillies;

  }
  catch(Exception e)
  {
    e.printStackTrace();
    return 0L;
  }
 }

/**
 * Method to get the time Stamp - Used in rptTimeStatCustom.jsp
 * @param dateTimeStamp
 * @return
 */
public Long getAbsoluteTimeStamp(String dateTimeStamp)
{
    try
    {
      SimpleDateFormat trDateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
      Date userDate = trDateFormat.parse(dateTimeStamp);

      Long absoluteDateInMillies = userDate.getTime();

      if(absoluteDateInMillies < 0)
      return 0L;

      return absoluteDateInMillies;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return 0L;
    }
}
  public static void main(String args[])
  {
    int choice = 0;

    DrillDownQueryUtils downUtils = new DrillDownQueryUtils("netstorm");
    DrillDownReportQuery daDownReportData = downUtils.getReportInfo("hhhh");

    DrillDownExecuteQuery downQueryData = new DrillDownExecuteQuery("21100", "netstorm", daDownReportData);

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

        //String arrTemp[][] = downQueryData.execute_get_obj_data(cmdArg, sortColumnType, defaultSorting, totalCount);
        String arrTemp[][] = downQueryData.execute_get_obj_data(cmdArg, sortColumnType, defaultSorting,new StringBuffer(), "", "", new DrillDownBreadCrumb("-1", "hh", 0));

        if(arrTemp != null)
        for(int i = 0; i < arrTemp.length; i++)
        {
          for (int j = 0; j < arrTemp[i].length; j++)
          {
            System.out.print(arrTemp[i][j] + " ");
          }
          System.out.println("\n");
        }

        System.out.println("\n\n");
        System.out.println("GGGGG " +sortColumnType.toString() + ", - " + defaultSorting.toString() + ", totalCount = " + totalCount);
        break;
      case 2:
        ArrayList addArgs = new ArrayList();
        addArgs.add("--matchtext 'k kk'");

        ArrayList removeArgs = new ArrayList();
        //removeArgs.add("--fields 4079");
        String cmdArg1 = " --object 5 --fields 0 --status -2 --matchtext sleep time";
        System.out.println("cmdArg1 = " + cmdArg1);
        System.out.println("KKKKK " + downQueryData.removeAddArgs(cmdArg1, addArgs, removeArgs));
        break;

      case 3:
        //removeArgs.add("--fields 4079");
        String cmdArg3 = " --urlidx 0";
        //System.out.println("cmdArg1 = " + cmdArg3);
        //System.out.println("KKKKK " + downQueryData.showFilterCriteria(cmdArg3, ""));


        String[][] arrTempNEw = downQueryData.execute_get_pg_comp_data("", new StringBuffer(), new StringBuffer(), new DrillDownBreadCrumb("", "",-1), 12);
        if(arrTempNEw != null)
          for(int i = 0; i < arrTempNEw.length; i++)
          {
            for (int j = 0; j < arrTempNEw[i].length; j++)
            {
              System.out.print(arrTempNEw[i][j] + " ");
            }
            System.out.println("\n");
          }

        break;

      case 4:
        //Vector vecDatVector = rptUtilsBean.readFileInVector("C:\\home\\netstorm\\test.txt");
        String arrTemparr[][] = rptUtilsBean.getFileDataIn2D("C:\\home\\netstorm\\test.txt");
        String[][] arrjj = downQueryData.getDataUptoLimit(arrTemparr, 3, 5);
        for(int i = 0; i < arrjj.length; i++)
        {
          for (int j = 0; j < arrjj[i].length; j++)
          {
            System.out.print(arrjj[i][j] + " ");
          }
          System.out.println("\n");
        }

        //DrillDownExecuteQuery.setConfigurableKeywords();
        System.out.println("KKKKK " + DrillDownExecuteQuery.IS_LOCATION_ACCESS_ENABLE);
        System.out.println("KKKKK " + DrillDownExecuteQuery.DEFAULT_LIMIT);
        break;


      default:




        System.out.println("Please select the correct option.");
    }
  }

}

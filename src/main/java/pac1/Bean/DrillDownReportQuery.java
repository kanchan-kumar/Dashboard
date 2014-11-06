/**File Name: DrillDownReportQuery.java
 * Purpose: Filter contain DrillDownReportData object
**/
package pac1.Bean;

import java.util.HashMap;


public class DrillDownReportQuery implements java.io.Serializable
{
  private static final long serialVersionUID = -5365699128856068664L;

  private static String className = "DrillDownReportData";

  public final static String OBJECT_OPTION = "--object";
  public final static String GROUP_BY_OPTION = "--group";
  public final static String ORDER_BY_OPTION = "--order";
  public final static String SHOW_DATA_OPTION = "--fields";

  //Filters arguments
  public final static String STATUS_OPTION = "--status";
  public final static String START_TIME_OPTION = "--starttime";
  public final static String END_TIME_OPTION = "--endtime";

  /**
   * Query for passing absolute start time stamp in case of NDE Mode
   */
  public final static String ABS_START_TIME_OPTION = "--abs_starttime";

  /**
   * Query for passing absolute end time stamp in case of NDE Mode
   */
  public final static String ABS_END_TIME_OPTION = "--abs_endtime";

  public final static String RESP_TIME_MODE_OPTION = "--resptimeqmode";
  public final static String RESP_TIME_OPTION = "--responsetime";
  //public final static String MEDIAN_TIME_OPTION = "--mediantime";
  public final static String MEDIAN_TIME_OPTION = "--responsetime2";

  //for query type and related filters
  public final static String QUERY_EXEC_TIME_MODE_OPTION = "--querytimemode";
  public final static String QUERY_EXEC_TIME_OPTION = "--querytime";
  public final static String QUERY_MEDIAN_TIME_OPTION = "--querytime2";

  public final static String URL_OPTION = "--url";
  public final static String PAGE_OPTION = "--page";
  public final static String TRANSACTION_OPTION = "--trans";
  public final static String SCRIPT_OPTION = "--script";
  public final static String ACCESS_OPTION = "--access";
  public final static String LOCATION_OPTION = "--location";
  public final static String BROWSER_OPTION = "--browser";

  public final static String GENERATOR_OPTION = "--generator";
  public final static String GENERATOR_ID_OPTION = "--generatorid";

  public final static String SERVER_OPTION = "--server";
  public final static String TIER_OPTION = "--tier";
  public final static String APP_OPTION = "--app";

  public final static String SVC_INSTANCE = "--instancename";
  public final static String SVC_INTANCE_ID_OPTION = "--instanceid";

  public final static String ACCESS_INDEX_OPTION = "--accessidx";
  public final static String LOCATION_INDEX_OPTION = "--locationidx";
  public final static String BROWSER_INDEX_OPTION = "--browseridx";

  public final static String PHASE_OPTION = "--phase";
  public final static String PHASE_INDEX_OPTION = "--phaseidx";

  public final static String URL_INDEX_OPTION = "--urlidx";
  public final static String PAGE_INDEX_OPTION = "--transidx";
  public final static String TRANSACTION_INDEX_OPTION = "--pageidx";
  public final static String SCRIPT_INDEX_OPTION = "--scriptidx";

  public final static String TIER_INDEX_OPTION = "--tierid";
  public final static String SERVER_INDEX_OPTION = "--serverid";
  public final static String APP_INDEX_OPTION = "--appid";

  public final static String WAN_ENV_OPTION = "--wanenv";
  public final static String TEST_RUN_OPTION = "--testrun";

  public final static String LOG_SEVERITY_OPTION = "--severity";
  public final static String LOG_MATCH_TEXT = "--matchtext";

  public final static String QUERY_TYPE_OPTION = "--querytype";
  public final static String QUERY_MATCH_TEXT = "--matchtext";
  
  //Exception Filters
  public final static String EXCEPTION_CLASS = "--excclsid";
  public final static String EXCEPTION_THROWING_CLASS = "--excthrclsid";
  public final static String EXCEPTION_THROWING_METHOD = "--excthrmtdid";
  public final static String EXCEPTION_CAUSE = "--exccauseid";
  public final static String EXCEPTION_MESSAGE = "--excmsgid";
  public final static String EXCEPTION_STACKTRACE = "--excstacktrace";
  
  public final static String URL_SEARCH_PATTERN = "--urlsearchpattern";
  public final static String URL_SEARCH_PATTERN_TYPE = "--urlsearchpatterntype";

  /**
   * Flag for passing query parameter as absolute or relative time stamp
   * if set to 1,pass absolute time stamp , else  pass realtive time stamp to the query
   */
  public static int TIME_STAMP_FLAG = 0;


  //for flowpath method count filter.
  public final static String FLOWPATH_MIN_METHOD_OPTION = "--min_methods";

  //for http header filter
  public final static String HTTP_HEADER_OPTION = "--httphdr";
  
  //for Exception Count Filter
  public final static String FLOWPATH_MIN_EXCEPTION_OPTION = "--min_exceptions";
  
  final static String DEFAULT_OBJECT_TYPE = "0";
  final static String DEFAULT_OBJECT_LABEL = "URL";
  final static String DEFAULT_OBJECT_STATUS = "-2";
  final static String DEFAULT_WAN_ENV = "0";
  final static String DEFAULT_SHOW_DATA = "4095";
  final static String DEFAULT_LOCATION = "";
  final static String DEFAULT_ACCESS = "";
  final static String DEFAULT_BROWSER = "";

  private String objectType = DEFAULT_OBJECT_TYPE; //default URL
  private String objectLabel = DEFAULT_OBJECT_LABEL;

  private String groupBy = "";
  private String orderBy = "";
  private String sortDataBaseQuery = "";

  private String showData_Sum = DEFAULT_SHOW_DATA;

  //Contain showData Label
  private String showData_Min = "";
  private String showData_Max = "";
  private String showData_Avg = "";
  private String showData_Median = "";
  private String showData_80 = "";
  private String showData_90 = "";
  private String showData_95 = "";
  private String showData_99 = "";
  private String showData_Tried = "";
  private String showData_Success = "";
  private String showData_Failure = "";
  private String showData_FailPct = "";

  //Contain showData Value
  private String showData_Min_value = "";
  private String showData_Max_value = "";
  private String showData_Avg_value = "";
  private String showData_Median_value = "";
  private String showData_80_value = "";
  private String showData_90_value = "";
  private String showData_95_value = "";
  private String showData_99_value = "";
  private String showData_Tried_value = "";
  private String showData_Success_value = "";
  private String showData_Failure_value = "";
  private String showData_FailPct_value = "";

  private String showDataSelectedLabel = "";

  private String objectStatus = DEFAULT_OBJECT_STATUS;

  //for http filters
  private String httpHeader = "";
  private String headerName = "";
  private String operatorName = "";
  private String comparisionValue = "";

  //For Multiple Http Filters
  private String[] httpMulFilters = null;

  private String timeOption = "";

  private String phaseIndex = "";
  private String phaseGroup = "";
  private String phaseType = "";
  private String phaseName = "";
  private String phaseStartTime = "";
  private String phaseEndTime = "";
  private String phaseObject = "";

  private String startTime = "";
  private String endTime = "";
  private String respMode = "";
  private String respModeLabel = "All";
  private String respTime = "";
  private String respTimeValue1 = "";
  private String respTimeValue2 = "";
  private String respTimeValue3 = "";
  private String respTimeFilter = "";
  private String medianTime = "";
  private String respRange = "";
  private String rangeType = "";

  private String URL = "";
  private String URLPage = "";
  private String URLSession = "";
  private String URLIndex = "";
  private String page = "";
  private String pageSession = "";
  private String pageIndex = "";
  private String transaction = "";
  private String transactionIndex = "";
  private String generator = "";
  private String generatorIndex = "";
  private String script = "";
  private String scriptIndex = "";
  private String location = DEFAULT_LOCATION;
  private String access = DEFAULT_ACCESS;
  private String browser = DEFAULT_BROWSER;

  private String logSeverity = "";
  private String logMatchText = "";

  private String locationIndex = "";
  private String accessIndex = "";
  private String browserIndex = "";

  //Initialize the Query Data.
  private String queryType = "";
  private String queryMatchText = "";

  //Initialize Query Execution time and related entries.
  private String queryExecTimeMode = "";
  private String queryExecTime = "";
  private String queryExecRange = "";
  private String queryExecRangeType = "";
  private String queryExecMedianTime = "";
  private String queryModeLabel = "";
  private String queryExecTimeForUpdate = "";
  private String queryTimeFilter = "";

  private String reportName = "";
  private String reportType = "";
  private String userName = "";
  private String lastModified = "";
  private String description = "NA";
  private String testRunTime = "";


  //filters for flowpath
  private String serverName = "";
  private String serverId = "";
  private String appName = "";
  private String appId = "";
  private String tierName = "";
  private String tierId = "";

  //filters for service Report.
  private String instanceName = "";
  private String instanceId = "";

  //Using for filter title criteria
  private String filterTitle = "";

  private String testRun = "-1";
  //WAN_ENV is not part of the specification it is the property of the test run
  private String WAN_ENV = DEFAULT_WAN_ENV;

  //for flowpath method count option.
  private String minMethods = "";
  
  //for flowpath exception count option
  private String minExceptions = "";
  
  //for Exception Filters
  private String exceptionClass = "";
  private String exceptionClassIndex = "";


  private String exceptionThrowingClass = "";
  private String exceptionThrowingClassIndex = "";
  private String exceptionThrowingMethod = "";
  private String exceptionThrowingMethodIndex = "";
  private String exceptionMessage = "";
  private String exceptionMessageIndex = "";
  private String exceptionCause = "";
  private String exceptionCauseIndex = "";


  private String exceptionStackTrace = "";

  //URL Pattern Applied For Flowpath Filter
  private String urlSearchPattern = "";
  
 //URL Pattern Type 0 - Any Where 1 - Starting 2 - Ending
  private String urlSearchPatternType = "";


  //Showing Abs time in NDE
  private String ndeTimeLabel = "";

  //For NS/NDE mode.
  private String guiMode = "NS";
  //0 - NS, 1 - only ND, 2 - ND + continous Monitoring
  private int NDEMode = 0;
  private String NDEPartition = "";

  //For Abs start/end Date.
  private String startDate = "";
  private String endDate = "";
  
  //Used For Creating Tree Based Sequence Diagram
  private String[][] arrTreeBasedData = null; 

  /**
   * This is Used for Exception StackTrace Report
   * Key - StackTraceID Value - StackTrace
   */
  private HashMap<Integer,String> stackTraceMap = null;
  
  /**
   * Used for getting Blob Data of Sequence Diagram in to 2-D Array
   */
  public static String[][] arrSequenceDiagram = null;
  
  /**
   * Used for appending errorMessages while generation of sequence diagram
   */
  public static StringBuffer errorMsgBuffer = new StringBuffer();
  
  /**
   * Used for Storing Method Summary Table Data for a Specific Flowpath Instance
   */
  public static String[][] arrMethodSummaryData = null;

  //Constructor
  public DrillDownReportQuery()
  {
    setLastModifiedDate();
  }

  /*******************************Setting Info *********************************************/

  public void setReportName(String reportName)
  {
    if(reportName != null)
      this.reportName = reportName;
    else
      this.reportName = "";
  }

  public void setReportType(String reportType)
  {
    if(reportType != null)
      this.reportType = reportType;
    else
      this.reportType = "1";
  }

  public void setUserName(String userName)
  {
    if(userName != null)
      this.userName = userName;
    else
      this.userName = "netstorm";
  }

  public void setLastModifiedDate()
  {
    this.lastModified = rptUtilsBean.getCurDateTime();
  }

  public void setDescription(String description)
  {
    if(description != null)
      this.description = description;
    else
      this.description = "NA";
  }

  public void setTestRun(String testRun)
  {
    this.testRun = testRun;
  }

  public void setWAN_ENV(String WAN_ENV)
  {
    this.WAN_ENV = WAN_ENV;
  }

  public void setHttpHeader(String httpHeader)
  {
      if(httpHeader != null)
        this.httpHeader = httpHeader;
  }

  public void setHeaderName(String headerName)
  {
      if(headerName != null)
        this.headerName = headerName;
  }

  public void setOperatorName(String operatorName)
  {
      if(operatorName != null)
       this.operatorName = operatorName;
  }

  public void setComparisionValue(String comparisionValue)
  {
      if(comparisionValue != null)
         this.comparisionValue = comparisionValue;
  }

  public void setHttpMulFilters(String[] httpMulFilters)
  {
      if(httpMulFilters != null)
      {
        this.httpMulFilters = new String[httpMulFilters.length];
        for(int i=0; i < httpMulFilters.length; i++)
        {
         if(httpMulFilters[i] != null)
           this.httpMulFilters[i] = httpMulFilters[i];
        }
      }
  }

  public void setStackTraceMap(HashMap<Integer, String> stackMapObj)
  {
     if(stackTraceMap == null)
        stackTraceMap = new HashMap<Integer, String>(stackMapObj.size());

     stackTraceMap.putAll(stackMapObj);
  }

  public void setMinExceptions(String minExceptions) 
  {
	if(minExceptions != null)
	   this.minExceptions = minExceptions;
  }
  
  public void setExceptionMessageIndex(String exceptionMessageIndex) 
  {
	  if(exceptionMessageIndex != null)
	   this.exceptionMessageIndex = exceptionMessageIndex;
  }
  public void setExceptionCauseIndex(String exceptionCauseIndex) 
  {
	  if(exceptionCauseIndex != null)
	    this.exceptionCauseIndex = exceptionCauseIndex;
  }
  
  public void setExceptionThrowingClassIndex(String exceptionThrowingClassIndex) 
  {
	  if(exceptionThrowingClassIndex != null)
	    this.exceptionThrowingClassIndex = exceptionThrowingClassIndex;
  }
  
  public void setExceptionClassIndex(String exceptionClassIndex) 
  {
	  if(exceptionClassIndex != null)
	    this.exceptionClassIndex = exceptionClassIndex;
  }
  
  public void setExceptionThrowingMethodIndex(String exceptionThrowingMethodIndex) 
  {
	  if(exceptionThrowingMethodIndex != null)
	    this.exceptionThrowingMethodIndex = exceptionThrowingMethodIndex;
  }
  
  public void setArrTreeBasedData(String[][] arrData) 
  {
	  this.arrTreeBasedData = arrData;
  }  

  public void setUrlSearchPattern(String urlSearchPattern) 
  {
	if(urlSearchPattern != null)
	 this.urlSearchPattern = urlSearchPattern;
  }

  public void setUrlSearchPatternType(String urlSearchPatternType) 
  {
   if(urlSearchPatternType != null)
	 this.urlSearchPatternType = urlSearchPatternType;
  }
  
  /*********************************************Getting Info ***************************/
  public String[][] getArrTreeBasedData() {
	  return arrTreeBasedData;
  }
  
  public String getExceptionMessageIndex() {
	  return exceptionMessageIndex;
  }
  
  public String getExceptionThrowingMethodIndex() {
	  return exceptionThrowingMethodIndex;
  }
  public String getExceptionClassIndex() {
	  return exceptionClassIndex;
  }
  
  public String getExceptionThrowingClassIndex() {
	  return exceptionThrowingClassIndex;
  }

  public String getExceptionCauseIndex() {
	  return exceptionCauseIndex;
  }
  
  public String getReportName()
  {
    return reportName;
  }

  /**
   * Getting StackTrace Map
   * @return
   */
  public HashMap<Integer, String> getStackTraceMap()
  {
    return stackTraceMap;
  }

  public String getUserName()
  {
    return userName;
  }

  public String getAccessUserName()
  {
    return userName;
  }

  public String getReportType()
  {
    return reportType;
  }

  public String getLastModifiedDate()
  {
    return lastModified;
  }

  public String getDescription()
  {
    return description;
  }

  public String getTestRun()
  {
    return testRun;
  }

  public String getWAN_ENV()
  {
    return WAN_ENV;
  }

  public String getHttpHeader()
  {
      return httpHeader;
  }

  public String getHeaderName()
  {
      return headerName;
  }

  public String getOperatorName()
  {
      return operatorName;
  }
  public String getComparisionValue()
  {
      return comparisionValue;
  }

  public static int getTimeStampFlag()
  {
      return TIME_STAMP_FLAG;
  }


  public String getHttpMulFilters()
  {
    String str = "";
    for (int i = 0; i < httpMulFilters.length; i++)
    {
      if (httpMulFilters[i] != null)
      {
        str = str + " " + HTTP_HEADER_OPTION + " " + httpMulFilters[i];
      }
    }
    return str;
  }
  
  public String getMinExceptions() 
  {
	return minExceptions;
  }
  
  public String getExceptionClass() {
	return exceptionClass;
  }

  public String getExceptionThrowingClass() {
	return exceptionThrowingClass;
  }

  public String getExceptionThrowingMethod() {
	return exceptionThrowingMethod;
  }

  public String getExceptionMessage() {
	return exceptionMessage;
  }

  public String getExceptionCause() {
	return exceptionCause;
  }

  public String getExceptionStackTrace() {
	return exceptionStackTrace;
  }
  
  public String getUrlSearchPatternType() {
	return urlSearchPatternType;
  }
	  
  public String getUrlSearchPattern() {
     return urlSearchPattern;
  }

/*******************************Setter*******************************************/

	public void setExceptionClass(String exceptionClass) 
	{
		if(exceptionClass != null)
		  this.exceptionClass = exceptionClass;
		else
		  this.exceptionClass = "";
	}

	public void setExceptionThrowingClass(String exceptionThrowingClass) 
	{
		if(exceptionThrowingClass != null)
		  this.exceptionThrowingClass = exceptionThrowingClass;
		else
		  this.exceptionThrowingClass = "";	
	}

	public void setExceptionThrowingMethod(String exceptionThrowingMethod) 
	{
		if(exceptionThrowingMethod != null)
		  this.exceptionThrowingMethod = exceptionThrowingMethod;
		else
			this.exceptionThrowingMethod = "";	
	}

	public void setExceptionMessage(String exceptionMessage) 
	{
		if(exceptionMessage != null)
		  this.exceptionMessage = exceptionMessage;
		else
			this.exceptionMessage = "";	
	}

	public void setExceptionCause(String exceptionCause) 
	{
		if(exceptionCause != null)
		  this.exceptionCause = exceptionCause;
		else
		  this.exceptionCause = "";	
	}

	public void setExceptionStackTrace(String exceptionStackTrace) 
	{
		if(exceptionStackTrace != null)
		   this.exceptionStackTrace = exceptionStackTrace;
		else
			this.exceptionStackTrace = "";	
	}

  public void setObjectType(String objectType)
  {
    if(objectType != null)
    {
      if(objectType.equals("1"))
        objectLabel = "Page";
      else if(objectType.equals("2"))
        objectLabel = "Transaction";
      else if(objectType.equals("3"))
        objectLabel = "Session";
      //else if(objectType.equals("4"))
       // objectLabel = "Flow Path";
      else if(objectType.equals("5"))
        objectLabel = "Logs";
      else if(objectType.equals("6"))
        objectLabel = "Queries";
      else if (objectType.equals("8"))
        objectLabel = "Service";
      else
        objectLabel = "URL";

      this.objectType = objectType;
      this.objectLabel = objectLabel;
    }
    else
      this.objectType = DEFAULT_OBJECT_TYPE;
  }

  public void setGroupBy(String groupBy)
  {
    if(groupBy != null)
      this.groupBy = groupBy;
    else
      this.groupBy = "";
  }

  public void setOrderBy(String orderBy)
  {
    if(orderBy != null)
      this.orderBy = orderBy;
    else
      this.orderBy = "";
  }

  public void setSortUsingDataBaseQuery(String sortDataBaseQuery)
  {
    if(sortDataBaseQuery != null)
      this.sortDataBaseQuery = sortDataBaseQuery;
    else
      this.sortDataBaseQuery = "";
  }

  public void setShowData(String showData_Sum)
  {
    if(showData_Sum != null)
      this.showData_Sum = showData_Sum;
    else
      this.showData_Sum = "";
  }

  public void setShowDataLabelandValue(String showDataValue)
  {
    String arrTemp[] = showDataValue.split(",");

    for(int i = 0; i < arrTemp.length; i++)
    {
      if(!showDataSelectedLabel.equals(""))
        showDataSelectedLabel = showDataSelectedLabel + ",";

      if(arrTemp[i].equals("16"))
      {
        showData_Min = "Min";
        showData_Min_value = "16";
        showDataSelectedLabel = showDataSelectedLabel + showData_Min;
      }
      else if(arrTemp[i].equals("64"))
      {
        showData_Max = "Max";
        showData_Max_value = "64";
        showDataSelectedLabel = showDataSelectedLabel + showData_Max;
      }
      else if(arrTemp[i].equals("32"))
      {
        showData_Avg = "Avg";
        showData_Avg_value = "32";
        showDataSelectedLabel = showDataSelectedLabel + showData_Avg;
      }
      else if(arrTemp[i].equals("128"))
      {
        showData_Median = "Median";
        showData_Median_value = "128";
        showDataSelectedLabel = showDataSelectedLabel + showData_Median;
      }
      else if(arrTemp[i].equals("256"))
      {
        showData_80 = "80%";
        showData_80_value = "256";
        showDataSelectedLabel = showDataSelectedLabel + showData_80;
      }
      else if(arrTemp[i].equals("512"))
      {
        showData_90 = "90%";
        showData_90_value = "512";
        showDataSelectedLabel = showDataSelectedLabel + showData_90;
      }
      else if(arrTemp[i].equals("1024"))
      {
        showData_95 = "95%";
        showData_95_value = "1024";
        showDataSelectedLabel = showDataSelectedLabel + showData_95;
      }
      else if(arrTemp[i].equals("2048"))
      {
        showData_99 = "99%";
        showData_99_value = "2048";
        showDataSelectedLabel = showDataSelectedLabel + showData_99;
      }
      else if(arrTemp[i].equals("1"))
      {
        showData_Tried = "Tried";
        showData_Tried_value = "1";
        showDataSelectedLabel = showDataSelectedLabel + showData_Tried;
      }
      else if(arrTemp[i].equals("4"))
      {
        showData_Success = "Success";
        showData_Success_value = "4";
        showDataSelectedLabel = showDataSelectedLabel + showData_Success;
      }
      else if(arrTemp[i].equals("8"))
      {
        showData_FailPct = "FailPct";
        showData_FailPct_value = "8";
        showDataSelectedLabel = showDataSelectedLabel + showData_FailPct;
      }
      else if(arrTemp[i].equals("2"))
      {
        showData_Failure = "Failure";
        showData_Failure_value = "2";
        showDataSelectedLabel = showDataSelectedLabel + showData_Failure;
      }
    }
    this.showDataSelectedLabel = showDataSelectedLabel;
  }

  public void setObjectStatus(String objectStatus)
  {
    if(objectStatus != null)
      this.objectStatus = objectStatus;
    else
      this.objectStatus = "-2";
  }

  public void setLogSeverity(String logSeverity)
  {
    if(logSeverity != null)
      this.logSeverity = logSeverity;
    else
      this.logSeverity = "";
  }

  public void setLogMatchText(String logMatchText)
  {
    if(logMatchText != null)
      this.logMatchText = logMatchText;
    else
      this.logMatchText = "";
  }

  public void setPhaseType(String phaseType)
  {
    if(phaseType != null)
      this.phaseType = phaseType;
    else
      this.phaseType = "";
  }

  public void setTimeOption(String timeOption)
  {
    if(timeOption != null)
      this.timeOption = timeOption;
    else
      this.timeOption = "";
  }

  public void setPhaseName(String phaseName)
  {
    if(phaseName != null)
      this.phaseName = phaseName;
    else
      this.phaseName = "";
  }


  public void setPhaseStartTime(String phaseStartTime)
  {
    if(phaseStartTime != null)
      this.phaseStartTime = phaseStartTime;
    else
      this.phaseStartTime = "";
  }

  public void setPhaseEndTime(String phaseEndTime)
  {
    if(phaseEndTime != null)
      this.phaseEndTime = phaseEndTime;
    else
      this.phaseEndTime = "";
  }

  public void setPhaseObject(String phaseObject)
  {
    if(phaseObject != null)
      this.phaseObject = phaseObject;
    else
      this.phaseObject = "";
  }

  public void setPhaseGroup(String phaseGroup)
  {
    if(phaseGroup != null)
      this.phaseGroup = phaseGroup;
    else
      this.phaseGroup = "";
  }

  public void setPhaseIndex(String phaseIndex)
  {
    if(phaseIndex != null)
      this.phaseIndex = phaseIndex;
    else
      this.phaseIndex = "";
  }

  public void setStartTime(String startTime)
  {
    if(startTime != null)
      this.startTime = startTime;
    else
      this.startTime = "";
  }

  public void setEndTime(String endTime)
  {
    if(endTime != null)
      this.endTime = endTime;
    else
      this.endTime = "";
  }

  public void setRespMode(String respMode)
  {
    if(respMode != null)
      this.respMode = respMode;
    else
      this.respMode = "";

    if(respMode.equals("1"))
      this.respModeLabel = "<=";
    else if(respMode.equals("2"))
      this.respModeLabel = ">=";
    else if(respMode.equals("3"))
      this.respModeLabel = "=";
    else
      this.respModeLabel = "All";
  }

  public void setRespTime(String respTime)
  {
    if(respTime != null)
      this.respTime = respTime;
    else
      this.respTime = "";
  }

  public void setResponseTimeValue1(String respTimeValue1)
  {
    if(respTimeValue1 != null)
      this.respTimeValue1 = respTimeValue1;
    else
      this.respTimeValue1 = "";
  }

  public void setResponseTimeValue2(String respTimeValue2)
  {
    if(respTimeValue2 != null)
      this.respTimeValue2 = respTimeValue2;
    else
      this.respTimeValue2 = "";
  }

  public void setResponseTimeValue3(String respTimeValue3)
  {
    if(respTimeValue3 != null)
      this.respTimeValue3 = respTimeValue3;
    else
      this.respTimeValue3 = "";
  }

  public void setMedianTime(String medianTime)
  {
    if(medianTime != null)
      this.medianTime = medianTime;
    else
      this.medianTime = "";
  }

  public void setRespRange(String respRange)
  {
    if(respRange != null)
    this.respRange = respRange;
  else
      this.respRange = "";
  }

  public void setRangeType(String rangeType)
  {
    if(rangeType != null)
    this.rangeType = rangeType;
  else
      this.rangeType = "";
  }


  //Setting query type and Execution time data.
  public void setQueryType(String queryType)
  {
    if(queryType != null)
      this.queryType = queryType;
    else
      this.queryType = "";
  }

  public void setQueryMatchText(String queryMatchText)
  {
    if(queryMatchText != null)
      this.queryMatchText = queryMatchText;
    else
      this.queryMatchText = "";
  }

  public void setQueryExecTimeMode(String queryExecTimeMode )
  {
    if(queryExecTimeMode != null)
      this.queryExecTimeMode = queryExecTimeMode;
    else
      this.queryExecTimeMode = "";

    if(queryExecTimeMode.equals("1"))
      this.queryModeLabel = "<=";
    else if(queryExecTimeMode.equals("2"))
      this.queryModeLabel = ">=";
    else if(queryExecTimeMode.equals("3"))
      this.queryModeLabel = "=";
    else
      this.queryModeLabel = "";
  }

  public void setQueryExecTime(String queryExecTime)
  {
    if(queryExecTime != null)
      this.queryExecTime = queryExecTime;
    else
      this.queryExecTime = "";
  }

  public void setQueryExecTimeForUpdate(String queryExecTimeForUpdate)
  {
    if(queryExecTimeForUpdate != null)
      this.queryExecTimeForUpdate = queryExecTimeForUpdate;
    else
      this.queryExecTimeForUpdate = "";
  }

  public void setQueryExecRange(String queryExecRange)
  {
    if(queryExecRange != null)
      this.queryExecRange = queryExecRange ;
    else
      this.queryExecRange = "";
  }

  public void setQueryExecRangeType(String queryExecRangeType)
  {
    if(queryExecRangeType != null)
      this.queryExecRangeType = queryExecRangeType  ;
    else
      this.queryExecRangeType = "";
  }

  public void setQueryExecMedianTime(String queryExecMedianTime )
  {
    if(queryExecMedianTime  != null)
      this.queryExecMedianTime  = queryExecMedianTime;
    else
      this.queryExecMedianTime = "";
  }

  public void setTestRunTime(String testRunTime)
  {
    if(testRunTime != null)
     this.testRunTime = testRunTime;
   else
     this.testRunTime = "";
  }


  public void setURLFilter(String URL)
  {
    if(URL != null)
      this.URL = URL;
    else
      this.URL = "";
  }

  public void setURLIndexFilter(String URLIndex)
  {
    if(URLIndex != null)
      this.URLIndex = URLIndex;
    else
      this.URLIndex = "";
  }

  public void setURLPageFilter(String URLPage)
  {
    if(URLPage != null)
      this.URLPage = URLPage;
    else
      this.URLPage = "";
  }

  public void setURLSessionFilter(String URLSession)
  {
    if(URLSession != null)
      this.URLSession = URLSession;
    else
      this.URLSession = "";
  }

  public void setPageFilter(String page)
  {
    if(page != null)
      this.page = page;
    else
      this.page = "";
  }

  public void setPageSessionFilter(String pageSession)
  {
    if(pageSession != null)
      this.pageSession = pageSession;
    else
      this.pageSession = "";
  }

  public void setPageIndexFilter(String pageIndex)
  {
    if(pageIndex != null)
      this.pageIndex = pageIndex;
    else
      this.pageIndex = "";
  }

  public void setTransactionFilter(String transaction)
  {
    if(transaction != null)
      this.transaction = transaction;
    else
      this.transaction = "";
  }

  public void setTransactionIndexFilter(String transactionIndex)
  {
    if(transactionIndex != null)
      this.transactionIndex = transactionIndex;
    else
      this.transactionIndex = "";
  }

  public void setGeneratorFilter(String generator)
  {
    if(generator != null)
      this.generator = generator;
    else
      this.generator = "";
  }

  public void setGeneratorIndexFilter(String generatorIndex)
  {
    if(generatorIndex != null)
      this.generatorIndex = generatorIndex;
    else
      this.generatorIndex = "";
  }

  public void setScriptFilter(String script)
  {
    if(script != null)
      this.script = script;
    else
      this.script = "";
  }

  public void setScriptIndexFilter(String scriptIndex)
  {
    if(scriptIndex != null)
      this.scriptIndex = scriptIndex;
    else
      this.scriptIndex = "";
  }

  public void setLocationFilter(String location)
  {
    if(location != null)
      this.location = location;
    else
      this.location = "";
  }

  public void setLocationIndexFilter(String locationIndex)
  {
    if(locationIndex != null)
      this.locationIndex = locationIndex;
    else
      this.locationIndex = "";
  }

  public void setAccessFilter(String access)
  {
    if(access != null)
      this.access = access;
    else
      this.access = "";
  }

  public void setAccessIndexFilter(String accessIndex)
  {
    if(accessIndex != null)
      this.accessIndex = accessIndex;
    else
      this.accessIndex = "";
  }

  public void setBrowserFilter(String browser)
  {
    if(browser != null)
      this.browser = browser;
    else
      this.browser = "";
  }

  public void setBrowserIndexFilter(String browserIndex)
  {
    if(browserIndex != null)
      this.browserIndex = browserIndex;
    else
      this.browserIndex = "";
  }


  //filters for flowpath object.
  public void setServerName(String serverName)
  {
    if(serverName != null)
      this.serverName = serverName;
  }

  public void setTierName(String tierName)
  {
    if(tierName != null)
      this.tierName = tierName;
  }

  public void setAppName(String appName)
  {
    if(appName != null)
      this.appName = appName;
  }

  public void setServerId(String serverId)
  {
    if(serverId != null)
      this.serverId = serverId;
  }

  public void setTierId(String tierId)
  {
    if(tierId != null)
      this.tierId = tierId;
  }

  public void setAppId(String appId)
  {
    if(appId != null)
      this.appId = appId;
  }

  public void setInstanceName(String instanceName)
  {
    if(instanceName != null)
      this.instanceName = instanceName;
  }

  public void setInstanceId(String instanceId)
  {
    if(instanceId != null)
      this.instanceId = instanceId;
  }

  public void setMinMethods(String minMethods)
  {
    if(minMethods != null)
      this.minMethods = minMethods;
  }

  public void setNDETimeLabel(String ndeTimeLabel)
  {
    this.ndeTimeLabel = ndeTimeLabel;
  }

  public void setGuiMode(String guiMode)
  {
	this.guiMode = guiMode;
  }

  public void setStartDate(String startDate)
  {
	this.startDate = startDate;
  }

  public void setEndDate(String endDate)
  {
	this.endDate = endDate;
  }

  /**********************************Getter Method *************************************/

  public String getObjectType()
  {
    return objectType;
  }

  public String getObjectLabel()
  {
    return objectLabel;
  }


  public String getGroupBy()
  {
    return groupBy;
  }

  public String getOrderBy()
  {
    return orderBy;
  }

  public String getSortUsingDataBaseQuery()
  {
    return sortDataBaseQuery;
  }

  public String getShowData()
  {
    return showData_Sum;
  }

  public String getShowDataLabel()
  {
    //String showDataLabel = showData_Min + "," +  showData_Max + ", " + showData_Avg + ", " + showData_Median + ", " + showData_80 + ", " + showData_90 + ", " + showData_95 + ", " + showData_99 + ", " + showData_Tried + ", " + showData_Success + ", " + showData_Failure + ", " + showData_FailPct;
    String showDataLabel = showData_Tried + ", " + showData_Success + ", " + showData_Failure + ", " + showData_FailPct + ", " + showData_Min + "," +  showData_Max + ", " + showData_Avg + ", " + showData_Median + ", " + showData_80 + ", " + showData_90 + ", " + showData_95 + ", " + showData_99;
    return showDataLabel;
  }

  public String showDataSelectedLabel()
  {
    return showDataSelectedLabel;
  }
  public String getShowDataValue()
  {
    //String showDataValues = showData_Min_value + "," +  showData_Max_value + ", " + showData_Avg_value + ", " + showData_Median_value + ", " + showData_80_value + ", " + showData_90_value + ", " + showData_95_value + ", " + showData_99_value + ", " + showData_Tried_value + ", " + showData_Success_value + ", " + showData_Failure_value + ", " + showData_FailPct_value;
    String showDataValues = showData_Tried_value + ", " + showData_Success_value + ", " + showData_Failure_value + ", " + showData_FailPct_value + ", " + showData_Min_value + "," +  showData_Max_value + ", " + showData_Avg_value + ", " + showData_Median_value + ", " + showData_80_value + ", " + showData_90_value + ", " + showData_95_value + ", " + showData_99_value;
    return showDataValues;
  }

  public String getObjectStatus()
  {
    return objectStatus;
  }

  public String getLogSeverity()
  {
    if(logSeverity == null)
      return "";
    return logSeverity;
  }

  public String getLogMatchText()
  {
    if(logMatchText == null)
      return "";
    return logMatchText;
  }

  public String getTimeOption()
  {
    if(timeOption == null)
      return "";
    return timeOption;
  }

  public String getPhaseName()
  {
    if(phaseName == null)
      return "";
    return phaseName;
  }

  public String getPhaseGroup()
  {
    if(phaseGroup == null)
      return "";
    return phaseGroup;
  }

  public String getPhaseStartTime()
  {
    if(phaseStartTime == null)
      return "";
    return phaseStartTime;
  }

  public String getPhaseEndTime()
  {
    if(phaseEndTime == null)
      return "";
    return phaseEndTime;
  }

  public String getPhaseObject()
  {
    if(phaseObject == null)
      return "";
    return phaseObject;
  }

  public String getPhaseIndex()
  {
    if(phaseIndex == null)
      return "";
    return phaseIndex;
  }
  public String getPhaseType()
  {
    return phaseType;
  }

  public String getStartTime()
  {
    return startTime;
  }

  public String getEndTime()
  {
    return endTime;
  }

  public String getRespMode()
  {
    return respMode;
  }

  public String getRespTime()
  {
    return respTime;
  }

  public String getMedianTime()
  {
    return medianTime;
  }

  public String getRespRange()
  {
    return respRange;
  }

  public String getRangeType()
  {
    return rangeType;
  }

  //Getting Query Type and related entries.
  public String getQueryType()
  {
    if(queryType == null)
       queryType = "";
     return queryType;
  }

  public String getQueryMatchText()
  {
    if(queryMatchText == null)
      queryMatchText = "";
    return queryMatchText;
  }

  public String getTestRunTime()
  {
    if(testRunTime == null)
      testRunTime = "";
    return testRunTime;
  }

  public String getQueryExecTimeMode()
  {
    if(queryExecTimeMode == null)
      queryExecTimeMode = "";
    return queryExecTimeMode;
  }

  public String getQueryExecTime()
  {
    if(queryExecTime == null)
      queryExecTime = "";
    return queryExecTime;
  }

  public String getQueryExecTimeForUpdate()
  {
    if(queryExecTimeForUpdate == null)
      queryExecTimeForUpdate = "";
    return queryExecTimeForUpdate;
  }

  public String getQueryExecRange()
  {
    if(queryExecRange == null)
      queryExecRange = "";
    return queryExecRange;
  }

  public String getQueryExecRangeType()
  {
    if(queryExecRangeType == null)
      queryExecRangeType = "";
    return queryExecRangeType;
  }

  public String getQueryExecMedianTime()
  {
    if(queryExecMedianTime == null)
      queryExecMedianTime = "";
    return queryExecMedianTime;
  }

  public String getURLFilter()
  {
    return URL;
  }

  public String getURLPageFilter()
  {
    return URLPage;
  }

  public String getURLSessionFilter()
  {
    return URLSession;
  }

  public String getURLIndexFilter()
  {
    if(URLIndex == null)
      return "";

    return URLIndex;
  }

  public String getPageFilter()
  {
    return page;
  }

  public String getPageSessionFilter()
  {
    return pageSession;
  }

  public String getPageIndexFilter()
  {
    if(pageIndex == null)
      return "";
    return pageIndex;
  }

  public String getTransactionFilter()
  {
    return transaction;
  }

  public String getTransactionIndexFilter()
  {
    if(transactionIndex == null)
      return "";

    return transactionIndex;
  }

  public String getGeneratorFilter()
  {
    return generator;
  }

  public String getGeneratorIndexFilter()
  {
    return generatorIndex;
  }

  public String getScriptFilter()
  {
    return script;
  }

  public String getScriptIndexFilter()
  {
    if(scriptIndex == null)
      return "";

    return scriptIndex;
  }

  public String getLocationFilter()
  {
    return location;
  }

  public String getLocationIndexFilter()
  {
    if(locationIndex == null)
      return "";
    return locationIndex;
  }

  public String getAccessFilter()
  {
    return access;
  }

  public String getAccessIndexFilter()
  {
    if(accessIndex == null)
      return "";

    return accessIndex;
  }

  public String getBrowserFilter()
  {
    return browser;
  }

  public String getBrowserIndexFilter()
  {
    if(browserIndex == null)
      return "";

    return browserIndex;
  }

  public String forDebugLog()
  {
    return getFilterSetting() + "  " + getShowDataLabel();
  }

  public void setFilterTitle(String filterTitle)
  {
    this.filterTitle = filterTitle;
  }

  public String getFilterTitle()
  {
    return filterTitle;
  }

  public void setRespTimeFilter(String respTimeFilter)
  {
    this.respTimeFilter = respTimeFilter;
  }

  public String getRespTimeFilter()
  {
    if(respTimeFilter == null)
      return "";

    return respTimeFilter;
  }

  public void setQueryTimeFilter(String queryTimeFilter)
  {
    this.queryTimeFilter = queryTimeFilter;
  }

  public String getQueryTimeFilter()
  {
    if(queryTimeFilter == null)
      return "";

    return queryTimeFilter;
  }

  public String getServerName()
  {
    return serverName;
  }

  public String getServerId()
  {
    return serverId;
  }

  public String getAppName()
  {
    return appName;
  }

  public String getAppId()
  {
    return appId;
  }

  public String getTierName()
  {
    return tierName;
  }

  public String getTierId()
  {
    return tierId;
  }

  public String getInstanceName()
  {
    return instanceName;
  }

  public String getInstanceId()
  {
    return instanceId;
  }

  public String getMinMethods()
  {
    return minMethods;
  }

  public String getNDETimeLabel()
  {
    return ndeTimeLabel;
  }

  public String getGuiMode()
  {
	return guiMode;
  }

  public String getStartDate()
  {
	return startDate;
  }

  public String getEndDate()
  {
	return endDate;
  }

  public int getNDEMode()
  {
    return NDEMode;
  }

  public void setNDEMode(int NDEMode)
  {
    this.NDEMode = NDEMode;
  }

  public String getNDEActivePartition()
  {
    return NDEPartition;
  }

  public void setNDEActivePartition(String NDEPartition)
  {
    this.NDEPartition = NDEPartition;
  }

  public String getFilterSetting()
  {
    try
    {
    String queryLine = "";
    String filterTitle = "";

    if(!getObjectType().equals(""))
    {
      queryLine = queryLine + " " + OBJECT_OPTION + " " + getObjectType();
      //filterTitle = "Report Type is " + getObjectLabel();
    }

    if(!getGroupBy().equals(""))
    {
      queryLine = queryLine + " " + GROUP_BY_OPTION + " " + getGroupBy();
      //filterTitle = filterTitle + " with group by " + getGroupBy();
    }

    if(!getOrderBy().equals(""))
    {
      queryLine = queryLine + " " + ORDER_BY_OPTION + " " + getOrderBy();
      //filterTitle = filterTitle + " and order by " + getOrderBy();
    }

    //filterTitle = filterTitle + ".";

    if(!getShowData().equals(""))
    {
      queryLine = queryLine + " " + SHOW_DATA_OPTION + " " + getShowData();
      //filterTitle = filterTitle + " Applied data = " + showDataSelectedLabel() + ". ";
    }

    if(!getObjectStatus().equals(""))
    {
      queryLine = queryLine + " " + STATUS_OPTION + " " + getObjectStatus();
      filterTitle = filterTitle + "Status = " + getObjectStatus();
    }

    if(!getPhaseIndex().equals(""))
    {
      queryLine = queryLine + " " + PHASE_INDEX_OPTION + " " + getPhaseIndex();
      filterTitle = filterTitle + "Phase Name " + getPhaseName();
    }

    if(!getStartTime().equals(""))
    {
      if(DrillDownReportQuery.TIME_STAMP_FLAG == 1)
      {
	  queryLine = queryLine + " " + ABS_START_TIME_OPTION + " " + getStartTime();
	  filterTitle = filterTitle + ", for duration " + getStartTime();
      }
      else
      {
	  queryLine = queryLine + " " + START_TIME_OPTION + " " + getStartTime();
	  filterTitle = filterTitle + ", for duration " + getStartTime();
      }
    }

    if(!getEndTime().equals(""))
    {
	  if(DrillDownReportQuery.TIME_STAMP_FLAG == 1)
	  {
            queryLine = queryLine + " " + ABS_END_TIME_OPTION + " " + getEndTime();
	    filterTitle = filterTitle + ", for duration " + getEndTime();
	  }
	  else
	  {
        queryLine = queryLine + " " + END_TIME_OPTION + " " + getEndTime();
	    filterTitle = filterTitle + " to " + getEndTime();
	  }
    }

    //Checking based on conditions
    if(httpHeader != null && !getHttpHeader().equals(""))
    {
	if(!getOperatorName().trim().equals(""))
	{
	    if(getOperatorName().trim().equals("PR") || getOperatorName().trim().equals("NP"))
	       queryLine = queryLine + " " + HTTP_HEADER_OPTION + " " + getHttpHeader() + ":" + getHeaderName() + ":" +getOperatorName();
	    else
	       queryLine = queryLine + " " + HTTP_HEADER_OPTION + " " + getHttpHeader() + ":" + getHeaderName() + ":" +getOperatorName() + ":" +getComparisionValue();
	}
	else
	 queryLine = queryLine + " " + HTTP_HEADER_OPTION + " " + getHttpHeader() + ":" + getHeaderName();

	if(!getHttpMulFilters().equals(""))
	    queryLine = queryLine + " " +getHttpMulFilters();

         filterTitle = filterTitle + "Header Type : "+ getHttpHeader() + ", HeaderName :" + getHeaderName();
    }
    
    if(exceptionClass != null && !getExceptionClass().equals(""))
    {
    	queryLine = queryLine + " " + EXCEPTION_CLASS + " " + getExceptionClassIndex();
    	filterTitle = filterTitle + ", Exception Class : " + getExceptionClass();
    }
    
    if(exceptionThrowingClass != null && !getExceptionThrowingClass().equals(""))
    {
    	queryLine = queryLine + " " + EXCEPTION_THROWING_CLASS + " " + getExceptionThrowingClassIndex();
    	filterTitle = filterTitle + ", Exception Throwing Class : " + getExceptionThrowingClass();
    }
    
    if(exceptionThrowingMethod != null && !getExceptionThrowingMethod().equals(""))
    {
    	queryLine = queryLine + " " + EXCEPTION_THROWING_METHOD + " " + getExceptionThrowingMethodIndex();
    	filterTitle = filterTitle + ", Exception Throwing Method : " + getExceptionThrowingMethod();
    }
    
    if(exceptionMessage != null && !getExceptionMessage().equals(""))
    {
    	queryLine = queryLine + " " + EXCEPTION_MESSAGE + " " + getExceptionMessageIndex();
    	filterTitle = filterTitle + ", Exception Message : " + getExceptionMessage();
    }
    
    if(exceptionCause != null && !getExceptionCause().equals(""))
    {
    	queryLine = queryLine + " " + EXCEPTION_CAUSE + " " + getExceptionCauseIndex();
    	filterTitle = filterTitle + ", Exception Cause : " + getExceptionCause();
    }
    
    if(exceptionStackTrace != null && !getExceptionStackTrace().equals(""))
    {
    	queryLine = queryLine + " " + EXCEPTION_STACKTRACE + " " + getExceptionStackTrace();
    	filterTitle = filterTitle + ", Exception Stack Trace : " + getExceptionStackTrace();
    }

    if(!getRespMode().equals(""))
    {
      queryLine = queryLine + " " + RESP_TIME_MODE_OPTION + " " + getRespMode();

      if(respModeLabel != null)
      {
        String strTempObjectLabel = "URL Response Time " + respModeLabel;

        if(Integer.parseInt(getObjectType()) == 3)
          respTimeFilter = "Session Duration " + respModeLabel;
        else if (Integer.parseInt(getObjectType()) == 8)
          respTimeFilter = "Service Response Time " + respModeLabel;
        else
          respTimeFilter = getObjectLabel() + " Response Time " + respModeLabel;

        if(getRespMode().equals("1") || getRespMode().equals("2"))
          respTimeFilter = respTimeFilter + " " + respTimeValue1 + " ms";
        else if(getRespMode().equals("3"))
          respTimeFilter = respTimeFilter + " " + respTimeValue1 + " ms &plusmn; " + respTimeValue2 + " " + respTimeValue3;
        setRespTimeFilter(respTimeFilter);
      }
    }

    if(!getRespTime().equals(""))
      queryLine = queryLine + " " + RESP_TIME_OPTION + " " + getRespTime();

    if(!getMedianTime().equals(""))
      queryLine = queryLine + " " + MEDIAN_TIME_OPTION + " " + getMedianTime();

    if(!getURLFilter().equals(""))
    {
      queryLine = queryLine + " " + URL_OPTION + " " + getURLFilter();
      filterTitle = filterTitle + ", URL =" + getURLFilter();
    }

    if(!getURLIndexFilter().equals(""))
    {
      queryLine = queryLine + " " + URL_INDEX_OPTION + " " + getURLIndexFilter();
      //filterTitle = filterTitle + ", URL =" + getURLFilter();
    }

    if(!getPageFilter().equals(""))
    {
      queryLine = queryLine + " " + PAGE_OPTION + " " + getPageFilter();
      filterTitle = filterTitle + ", Page = " + getPageFilter();
    }

    if(!getPageIndexFilter().equals(""))
    {
      queryLine = queryLine + " " + PAGE_INDEX_OPTION + " " + getPageIndexFilter();
      //filterTitle = filterTitle + ", Page = " + getPageFilter();
    }

    if(!getScriptFilter().equals(""))
    {
      queryLine = queryLine + " " + SCRIPT_OPTION + " " + getScriptFilter();
      filterTitle = filterTitle + ", Script = " + getScriptFilter();
    }

    if(!getScriptIndexFilter().equals(""))
    {
      queryLine = queryLine + " " + SCRIPT_INDEX_OPTION + " " + getScriptIndexFilter();
      //filterTitle = filterTitle + ", Script = " + getScriptFilter();
    }

    if(!getTransactionFilter().equals(""))
    {
      queryLine = queryLine + " " + TRANSACTION_OPTION + " " + getTransactionFilter();
      filterTitle = filterTitle + ", Transaction = " + getTransactionFilter();
    }

    if(!getTransactionIndexFilter().equals(""))
    {
      queryLine = queryLine + " " + TRANSACTION_INDEX_OPTION + " " + getTransactionIndexFilter();
      //filterTitle = filterTitle + ", Transaction = " + getTransactionFilter();
    }

    if(!getGeneratorIndexFilter().equals(""))
    {
      queryLine = queryLine + " " + GENERATOR_ID_OPTION + " " + getGeneratorIndexFilter();
      filterTitle = filterTitle + ", Generator = " + getGeneratorFilter();
    }

    if(!getAccessFilter().equals(""))
    {
      queryLine = queryLine + " " + ACCESS_OPTION + " " + getAccessFilter();
      filterTitle = filterTitle + ", Access = " + getAccessFilter();
    }

    if(!getLocationFilter().equals(""))
    {
      queryLine = queryLine + " " + LOCATION_OPTION + " " + getLocationFilter();
      filterTitle = filterTitle + ", Location = " + getLocationFilter();
    }

    if(!getBrowserFilter().equals(""))
    {
      queryLine = queryLine + " " + BROWSER_OPTION + " " + getBrowserFilter();
      filterTitle = filterTitle + ", Browser = " + getBrowserFilter();
    }

    if(!getServerId().equals(""))
    {
      queryLine = queryLine + " " + SERVER_INDEX_OPTION + " " + getServerId();
      filterTitle = filterTitle + ", Server Name = " + getServerName();
    }

    if(!getTierId().equals(""))
    {
      queryLine = queryLine + " " + TIER_INDEX_OPTION + " " + getTierId();
      filterTitle = filterTitle + ", Tier Name = " + getTierId();
    }

    if(!getAppId().equals(""))
    {
      queryLine = queryLine + " " + APP_INDEX_OPTION + " " + getAppId();
      filterTitle = filterTitle + ", App Name = " + getAppName();
    }

    if(!getInstanceId().equals(""))
    {
      queryLine = queryLine + " " + SVC_INTANCE_ID_OPTION + " " + getInstanceId();
      filterTitle = filterTitle + ", Service Instance = " + getInstanceName();
    }

    if(!getMinMethods().equals(""))
    {
      queryLine = queryLine + " " + FLOWPATH_MIN_METHOD_OPTION + " " + getMinMethods();
      filterTitle = filterTitle + ", Min Methods Count = " + getMinMethods();
    }

    if(!getLogSeverity().equals(""))
    {
      queryLine = queryLine + " " + LOG_SEVERITY_OPTION + " " + getLogSeverity();
      filterTitle = filterTitle + ", Log Severity = " + getLogSeverity();
    }

    if(!getLogMatchText().equals(""))
    {
      queryLine = queryLine + " " + LOG_MATCH_TEXT + " " + getLogMatchText();
      filterTitle = filterTitle + ", Log match text = " + rptUtilsBean.replace(getLogMatchText(), "'", "");
    }

    if(!getQueryType().equals("") && !getQueryType().equals("nothing"))
    {
      queryLine = queryLine + " " + QUERY_TYPE_OPTION + " " + getQueryType();
      filterTitle = filterTitle + ", Query Type = " + rptUtilsBean.replace(getQueryType(), "'", "");
    }

    if(!getQueryExecTimeMode().equals("") && !getQueryExecTimeMode().equals("nothing"))
    {
      queryLine = queryLine + " " + QUERY_EXEC_TIME_MODE_OPTION + " " + getQueryExecTimeMode();

      if(queryModeLabel != null)
      {
        queryTimeFilter = "Query Execution Time " + queryModeLabel;

        if(getQueryExecTimeMode().equals("1") || getQueryExecTimeMode().equals("2"))
          queryTimeFilter = queryTimeFilter + " " + queryExecTimeForUpdate + " ms";
        else if(getQueryExecTimeMode().equals("3"))
          queryTimeFilter = queryTimeFilter + " " + queryExecTimeForUpdate + " ms &plusmn; " + queryExecRange + " " + queryExecRangeType;
        setQueryTimeFilter(queryTimeFilter);
      }
    }
    
    if(minExceptions != null && !getMinExceptions().equals(""))
    {
    	queryLine = queryLine + " " + FLOWPATH_MIN_EXCEPTION_OPTION + " " + getMinExceptions();
    	filterTitle = filterTitle + ", Minimum Exceptions Count = " + getMinExceptions();
    }
    
    if(urlSearchPattern != null && !getUrlSearchPattern().equals(""))
    {
    	queryLine = queryLine + " " + URL_SEARCH_PATTERN + " " + getUrlSearchPattern(); 
    	filterTitle = filterTitle + ", URL Search Pattern = " + getUrlSearchPattern();
    }
    
    if(urlSearchPatternType != null && !getUrlSearchPatternType().equals(""))
    {
    	queryLine = queryLine + " " + URL_SEARCH_PATTERN_TYPE + " " + getUrlSearchPatternType(); 
    }

    if(!getQueryExecTime().equals("") && !getQueryExecTime().equals("nothing"))
      queryLine = queryLine + " " + QUERY_EXEC_TIME_OPTION + " " + getQueryExecTime();

    if(!getQueryExecMedianTime().equals("") && !getQueryExecMedianTime().equals("nothing"))
      queryLine = queryLine + " " + QUERY_MEDIAN_TIME_OPTION + " " + getQueryExecMedianTime();



    setFilterTitle(filterTitle);

    Log.debugLog(className, "getFilterSetting", "", "", "Filter Setting = " + queryLine + ", " + getShowDataLabel());

    //queryLine = getObjectStatus() + getStartTime() + getEndTime() + getRespMode() + getRespTime() + getMedianTime() + getURLFilter() + getPageFilter() + getTransactionFilter() + getScriptFilter() + getAccessFilter() + getLocationFilter() + getBrowserFilter();
    return queryLine;
  }
  catch(Exception e)
  {
    e.printStackTrace();
    return "";
  }
 }
}

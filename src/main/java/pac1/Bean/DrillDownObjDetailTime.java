/**DrillDownObjDetailTime
* Purpose: Convert nsi_gen_src8 c program into java program
* Author: Richa/Kanchan
**/

package pac1.Bean;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Vector;

import pac1.Bean.VUserTrace.Page;

public class DrillDownObjDetailTime
{
  String testRun = "-1";
  String userName = "";
  boolean ndKeywordValue = false;   //Use to check ND keyword is enable or not if it is enable then 2 more coluumns (eq : Flowpath Instance, FlowPath Signature would be also added )
  int isNDEContinuousEnable = 0;
  int objType = 0;
 
  double totalTime = 0;
  double totalTimeMax = 0;//Use to calculate total bytes of all records 
  
  double plottedArea = 900;
  
  //Common variables for executing a command
  String strCmdName = "";
  String strCmdArgs = "";
  Vector vecData = new Vector();          //Used to get Data From shell
  CmdExec cmdExec = new CmdExec();

  int counter = 0; //Tell how many url will come for particular page
  private static String className = "DrillDownObjectTimeDetail";
  public static String FAIL_QUERY = "1";           //give row count

  ArrayList arrListURL = new ArrayList();      // It will store url data for Table and Bar in a Object Format.
  ArrayList arrListPage = new ArrayList(); 
  double arrPagesReqCompTime[] = null;
  String[][] arrResultPageStatus = null;
  long totalBytesSummation = 0; //using to show total bytes on screen left top
  
  static String NSI_DB_GET_OBJ_TIMING_DETAIL = "nsi_db_get_obj_timing_detail";
  static String NSI_DB_GET_PAGE_STATUS = "nsi_db_get_page_status";

  // These two variables are used for calculation of URL Component times
  boolean bolLastComp;
  double prevTimeStampSave; // Used to find first byte time 
  
  String[] dnsLookupData = null;
 
  //Constructor of Class used to initalize testRun ,userName
  public DrillDownObjDetailTime(String testRun, String userName, boolean ndKeyValue, int objType, int isNDEContinuousEnable)
  {
    this.testRun = testRun; //test run number
    this.userName = userName; //user name used to execute query
    this.ndKeywordValue = ndKeyValue;   //Set the ND keyword Value
    this.objType = objType;             // Set the Object Type
    this.isNDEContinuousEnable = isNDEContinuousEnable;
  }
   
  //setter of DnsLookup array
  public void setDnsLookupData(String[] dnsLookupData)
  {
    this.dnsLookupData = dnsLookupData;
  }
  
  //getter of DnsLookup
  public String[] getDnsLookupData()
  {
    return dnsLookupData;
  }

  /**
   * Name: calculateComponentTime
   * Purpose : Method to calculate the Time for each Component of URL
   * Input :
   *         component1 :
   *         component2 :
   * Output :  component_time
   * For eq : connectTime = calculateComponentTime(strConnectDoneTime, strStartTime);
   * In C Code concept for this is as given below same Approach we are following for the current record
   * connectstamp = atoi(ptr);    here connectstamp  = strConnectDoneTime , sstamp = strStartTime 
   if (connectstamp < sstamp) connectstamp = sstamp;
     urlInfo[cur_url_idx].ConnectIntvl = connectstamp - sstamp;
   *
   * @return
   */
  public double calculateComponentTime(double component1, double component2, String compName)
  {
    Log.debugLog(className, "calculateComponentTime", "", "", "Method called. For " + compName + ", Component1 Value is  = " + component1 + ", Component2  Value is = "+component2);
    try
    {
      double compTime = (component1 - component2)/1000.0;

      Log.debugLog(className, "calculateComponentTime", "", "", "Component " + compName + " = " + compTime);

      return(Double.parseDouble(rptUtilsBean.convTo3DigitDecimal(compTime)));
    }
    catch(Exception e)
    {
      Log.errorLog(className, "calculateComponentTime", "", "", "Exception - " + e);
      e.printStackTrace();
      return 0;

    }
  }

  /**
   * Name: calculateTotalTimeOfURL
   * Purpose : Method to calculate TotalTime of each URL
   * Input :  connect_time , ssl_time , req_time , firstByte , reqCompTime
   * It is a summation  of connect_time , ssl_time ,req_time ,firstBytes ,reqCompTime  
   * Output :  total_time
   * @return
   */
  public double calculateTotalTimeOfURL(double connect_time, double ssl_time, double req_time, double firstByte, double reqCompTime, double dnsLookupTime)
  {
    Log.debugLog(className, "calculateTotalTimeOfURL", "", "", "Method called. connect_time = " + connect_time + ", ssl_time = " + ssl_time + ", req_time = " + req_time +  ", firstByte = " + firstByte + ", reqCompTime = " + reqCompTime);
    try
    {
      double totalTime = Double.parseDouble(rptUtilsBean.convTo3DigitDecimal(connect_time + ssl_time + req_time + firstByte + reqCompTime + dnsLookupTime));
      Log.debugLog(className, "calculateTotalTimeOfURL", "", "", "Total time = " + totalTime);

      return totalTime;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "calculateTotalTimeOfURL", "", "", "Exception - " + e);
      e.printStackTrace();
      return 0;
    }
  }

  public double getCompTime(double prevTimeStamp, double currTimeStamp, double endTimeStamp, String compName)
  {
    Log.debugLog(className, "getCompTime", "", "", "Method called. For " + compName + ", prevTimeStamp  = " + prevTimeStamp + ", currTimeStamp = " + currTimeStamp + ", endTimeStamp = " + endTimeStamp);
    try
    {
      double compTime;

      if(bolLastComp)
      {
        Log.debugLog(className, "getCompTime", "", "", "Component " + compName + " = 0.0 as last component is already over");
        return(0.0);
      }

      if(currTimeStamp > 0) prevTimeStampSave = currTimeStamp;

      if((currTimeStamp == 0) && (prevTimeStamp != 0))
      {
        compTime = endTimeStamp - prevTimeStamp;
        bolLastComp = true;
      }
      else if(prevTimeStamp >= currTimeStamp)  // Also case when both are 0
      {
        compTime = 0;
        Log.errorLog(className, "getCompTime", "", "", "prevTimeStamp is greater than currTimeStamp");
      }
      else
        compTime = currTimeStamp - prevTimeStamp;
    
      compTime = compTime / 1000.0;
    
      Log.debugLog(className, "getCompTime", "", "", "Component " + compName + " = " + compTime);
      return(Double.parseDouble(rptUtilsBean.convTo3DigitDecimal(compTime)));
      
    }
    catch (Exception e) 
    {
      Log.errorLog(className, "getCompTime", "", "", "Exception - " + e);
      e.printStackTrace();
      return 0;
    }
  }
 
  /**
   * Name: nsi_db_get_url_timing_detail
   * Purpose : Method which will return 2 array List (where data in arrayList would be in object format)
   *           1: URL
   *           2: Page
   * Input  :   cmdArg, int objType, StringBuffer sortColumnType, defaultSorting, breadCrumb
   *         component2 :
   * Output :  2 ArrayList : arrListURL For URL and arrListPage For Page
   * For eq :
 * @param arrPageStatus
   * @return
   * 
   * Confail:--
   *   pageinstance|pagename|urlname|starttime|connectstarttime|connectdonetime|sslhandshakedone|writecompletime|firstbytercdtime|requestcompletedtime|errorname|httpresponsecode|appbytesrcd|conreused|httsreqreused|connectionnumber|flowpathinstance
   *   0|Weblogic_Level4Page|/ns_samples/checkOutAndPlaceOrder?sleepTimeForCC=6&sleepTimeForValidate=40&level=4|14116|14116|0|0|0|0|14116|ConFail|0|0|f|f|0|-1
   *   
   * T.O
   *   pageinstance|pagename|urlname|starttime|connectstarttime|connectdonetime|sslhandshakedone|writecompletime|firstbytercdtime|requestcompletedtime|errorname|httpresponsecode|appbytesrcd|conreused|httsreqreused|connectionnumber|flowpathinstance
   *    38|RegOrderPlacedGiftPage|/media/css/checkout_v1_m56577569836926792.css|1988734|1988735|1989010|1989282|1989283|0|2048734|T.O|0|0|f|t|5|-1
   */
  public void execute_get_obj_timing_detail(String cmdArg, int objType, StringBuffer sortColumnType, StringBuffer defaultSorting, DrillDownBreadCrumb breadCrumb)
  {
    Log.debugLog(className, "execute_get_obj_timing_detail", "", "", "Method called. = " + cmdArg + "objType = " +objType+ "");

    /*
     * It is a object Array that will store 2 Arraylist we will furture convert into 2-d Array used to show in GUI
     * 1: Page
         It contain Table Info and Bar Info for Page
       2: URL
     *   It contain Table Info and Bar Info for URL
     */

    try
    {
      boolean bolRsltFlag = true;
      int pageInstance = -1;
      boolean bolRsltFlagStatus = false;
      if(breadCrumb.getFilterArument().equals("")  || !breadCrumb.getFilterArument().trim().equals(cmdArg.trim()))
      {
        strCmdName = NSI_DB_GET_OBJ_TIMING_DETAIL;
        Log.debugLog(className, "execute_Get_Obj_Timing_Detail", "", "", "Command Name = " + strCmdName);

        strCmdArgs = cmdArg;
        Log.debugLog(className, "execute_Get_Obj_Timing_Detail", "", "", "Command Argument = " + strCmdArgs);

        vecData = new Vector();
        bolRsltFlag = cmdExec.getResultByCommand(vecData, strCmdName, strCmdArgs, 0, userName, null);
        if(bolRsltFlag)
          breadCrumb.setBreadCrumbVectorData(vecData);
        
        strCmdName = NSI_DB_GET_PAGE_STATUS;
        Log.debugLog(className, "execute_Get_Obj_Timing_Detail", "", "", "Command Name = " + strCmdName);
        Vector vecDataStatus = new Vector();
        
        /**
         * To do List -  Currently commenting this code because query is take very long time to execute due to which GUI hang 
          and user is not able to view Object Timing Detail Screen.
         */
        //bolRsltFlagStatus = cmdExec.getResultByCommand(vecDataStatus, strCmdName, strCmdArgs, 0, userName, null);
        
        if(bolRsltFlagStatus)
        {
          arrResultPageStatus = rptUtilsBean.getRecFlds(vecDataStatus, "", "", "-");
          breadCrumb.setBreadCrumbData(arrResultPageStatus);
        }
        /**bolRsltFlag = true;
        vecData = new Vector();
        vecData = rptUtilsBean.readFileInVector("C:\\home\\netstorm\\work\\webapps\\netstorm\\testy\\aaaansi_get_8x.out.9729");
        **/
      }
      else
      {
        vecData = breadCrumb.getBreadCrumbVectorData();
        arrResultPageStatus = breadCrumb.getBreadCrumbData();
        //sortColumnType.append(downBreadCrumb.getSortColumnType());
        //defaultSorting.append(downBreadCrumb.getDefaultSortIndicator());
        breadCrumb.setBreadCrumbVectorData(vecData);
      }
      
      //In case Query Fail due to some reasion
      if((!bolRsltFlag) || (vecData == null))
      {
        arrListURL.add(FAIL_QUERY);
        arrListPage.add(FAIL_QUERY);
        return;
      }
      
      //not a error case
      if (vecData.size() == 1) //means only header will pass
      {
        return;
      }
      
      String strDataLine = ""; //get data in loop
      String start_time = "0"; //start time of first url of the record
      int connIDcounter = 1;
      LinkedHashMap<Integer, Integer> linkedHashMapConnId = new LinkedHashMap<Integer, Integer> ();
      
      //Skipping header
      for(int i = 1; i < vecData.size(); i++)
      {
        strDataLine = vecData.get(i).toString();
        
        Log.debugLog(className, "execute_Get_Obj_Timing_Detail", "", "", "URL Line" + i + " = " + strDataLine);
        //only using this line when we are getting frim batch file
        
        //strDataLine = strDataLine.substring(1, (strDataLine.length() -1 ));
        String arrTempCol[] = rptUtilsBean.split(strDataLine, "|");

        //If length is not 17 it will return
        if(arrTempCol.length < 17)
        {
          // TODO - Show query error
          return;
        }

        String strPageInstance = arrTempCol[0];
        String strPageName = arrTempCol[1];
        String strURLName = arrTempCol[2];
        String strStartTime = arrTempCol[3];
        String strAbsStartTime = arrTempCol[4];
        //if(isNDEContinuousEnable == 2)
          //strStartTime = strAbsStartTime;
        String strDNSStartTime = arrTempCol[5];
        String strDNSEndTime = arrTempCol[6];
        String strConnectStartTime = arrTempCol[7];
        String strConnectDoneTime = arrTempCol[8];
        String strSSLlHandShakeDone = arrTempCol[9];
        String strWriteCompleTime = arrTempCol[10];
        String strFirstByte = arrTempCol[11];
        String strRequestCompletedTime = arrTempCol[12];
        String strErrorname = arrTempCol[13];
        String strHttpResponseCode = arrTempCol[14];
        String strAppBytes = arrTempCol[15];
        String strConReused = arrTempCol[16];
        String strHttpsReused = arrTempCol[17];
        String strConnectionNumber = arrTempCol[18];
        String strFlowPathInstance = arrTempCol[19];
        String flowpathsignature = "-1";


        int connNumber = Integer.parseInt(strConnectionNumber);
        //connection ID not contain we will put in map
        if(!linkedHashMapConnId.containsKey(connNumber))
        {
          //If return FD return -1 in case of connection fail
          //we will assign counter ID to connection number
          if(connNumber >= 0)
            linkedHashMapConnId.put(connNumber, connIDcounter);
          
          strConnectionNumber = connIDcounter + "";
          connIDcounter++;
        }
        else
        {
          //If already contain get counter ID of connection number
          strConnectionNumber = linkedHashMapConnId.get(connNumber).toString();
        }
          
        
         if(ndKeywordValue) //If ND enable this field will come
           flowpathsignature = "1";

        //this is start time of first url of the record
        //using in to calculate start pixel 
        if(i == 1)
          start_time = strStartTime;
        
        // Calculate time for each component in seconds by taking
        // diff from start time
        
        // convert timestamps from string to double
        double startStampTime = Double.parseDouble(strStartTime); 
        double connect_start_Time = Double.parseDouble(strConnectStartTime); // Not used
        double connect_Time = Double.parseDouble(strConnectDoneTime);
        double ssl_Time = Double.parseDouble(strSSLlHandShakeDone);
        double write_Time = Double.parseDouble(strWriteCompleTime);
        double first_Byte = Double.parseDouble(strFirstByte);
        double req_CompTime = Double.parseDouble(strRequestCompletedTime);
        //double dnsLookupTime = Double.parseDouble(strDnsLookup);
        double dnsStartTime =  Double.parseDouble(strDNSStartTime);
        double dnsEndTime = Double.parseDouble(strDNSEndTime);
        
     
        double connect_Time_intvl = 0.0, ssl_Time_intvl = 0.0, write_Time_intvl = 0.0, first_Byte_intvl = 0.0, req_CompTime_intvl = 0.0, dnsLookup_intvl=0.0;
      
        bolLastComp = false;
        prevTimeStampSave = startStampTime;// Used to find first byte time 
        
        if(dnsStartTime > 0)
        {
          dnsLookup_intvl = getCompTime(prevTimeStampSave, dnsEndTime, req_CompTime, "DNS Lookup Time");
        }

        //Connect time inlcudes any time spend from start of request to connect start if any
        connect_Time_intvl = getCompTime(prevTimeStampSave, connect_Time, req_CompTime, "ConnectTime");
        
        ssl_Time_intvl = getCompTime(prevTimeStampSave, ssl_Time, req_CompTime, "SSLTime");
        
        write_Time_intvl = getCompTime(prevTimeStampSave, write_Time, req_CompTime, "RequestTime");
        
        // If there is Timeout/Error while waiting for the first byte of response, then first_byte timestamp is 0
        // In this case time spend after write it accounted in first byte time
        if(first_Byte == 0) 
        {
          first_Byte_intvl = getCompTime(prevTimeStampSave, req_CompTime, req_CompTime, "FirstByteTime");
        }
        else // First byte was recieved
        {
          // First byte timestamp should be always > write timestamp. This check is for safety only
          if(first_Byte > write_Time) 
            first_Byte_intvl = getCompTime(prevTimeStampSave, first_Byte, req_CompTime, "FirstByteTime");
        
          // Req complete timestamp should be always > first byte timestamp. This check is for safety only
          if(req_CompTime > first_Byte)
            req_CompTime_intvl = getCompTime(prevTimeStampSave, req_CompTime, req_CompTime, "ContentDownloadTime");
        }
        
        //dnsLookup_intvl = getCompTime(prevTimeStampSave, dnsLookupTime, req_CompTime, "RequestTime"); //not used
        
        //System.out.println("prevTimeStampSave = "+prevTimeStampSave + " dnsstartTime = "+dnsStartTime + " dnsendTime = "+dnsEndTime + " req_CompTime = "+req_CompTime + " dnsLookup_intvl = "+dnsLookup_intvl);
        String connection_Reused = calculateReused(strConReused);
        String https_Resued = calculateReused(strHttpsReused);
        double total_Time = calculateTotalTimeOfURL(connect_Time_intvl, ssl_Time_intvl, write_Time_intvl, first_Byte_intvl, req_CompTime_intvl,dnsLookup_intvl);

        //using to show total bytes on table header
        totalBytesSummation = totalBytesSummation + Integer.parseInt(strAppBytes);
        
        URLInfo  urlInfoObj =  new URLInfo();

        urlInfoObj.setURLInstance(strPageInstance);
        urlInfoObj.setURLConnectionNumber(strConnectionNumber); //connection number like page 0
        urlInfoObj.setURLName(strURLName); //url name
        urlInfoObj.setURLStatus(strErrorname); //status
        urlInfoObj.setURLHTTPCode(strHttpResponseCode); //http code
        urlInfoObj.setRequestCompTime(Double.parseDouble(strRequestCompletedTime));
        urlInfoObj.setURLConReused(connection_Reused); //connection reuse
        urlInfoObj.setURLHttpReused(https_Resued); //SSL reused
        urlInfoObj.setURLtotalTime(rptUtilsBean.convTo3DigitDecimal(total_Time)); //total time
        urlInfoObj.setURLConnectTime(rptUtilsBean.convTo3DigitDecimal(connect_Time_intvl)); //connect time
        urlInfoObj.setURLSSLTime(rptUtilsBean.convTo3DigitDecimal(ssl_Time_intvl));  //SSL time
        urlInfoObj.setURLRequestTime(rptUtilsBean.convTo3DigitDecimal(write_Time_intvl)); // request time
        urlInfoObj.setURLFirstByte(rptUtilsBean.convTo3DigitDecimal(first_Byte_intvl)); //First byte
        urlInfoObj.setURLRequestCompletedTime(rptUtilsBean.convTo3DigitDecimal(req_CompTime_intvl)); //Download time
        urlInfoObj.setURLAppBytes(rptUtilsBean.convTo3DigitDecimal(Double.parseDouble(strAppBytes))); //app total byte
        urlInfoObj.setFlowPathInstance(strFlowPathInstance);
        urlInfoObj.setFlowPathSignature(flowpathsignature);
        //urlInfoObj.setDNSLookup(rptUtilsBean.convTo3DigitDecimal(dnsLookupTime));
        urlInfoObj.setDNSLookupTime(rptUtilsBean.convTo3DigitDecimal(dnsLookup_intvl));
       
        double startInterval = (Double.parseDouble(strStartTime) - Double.parseDouble(start_time)) * plottedArea;
        urlInfoObj.setURLStartInterval(startInterval);
        
        urlInfoObj.setURLStartTime(strStartTime);  //Saving startTime of URL so that to calculate startPix of URL
        urlInfoObj.setURLAbsStartTime(strAbsStartTime);
        //calculating total time of all URL
        if(totalTimeMax < urlInfoObj.getRequestCompTime())
        {
          totalTimeMax = urlInfoObj.getRequestCompTime();
          totalTime = urlInfoObj.getRequestCompTime() - Double.parseDouble(start_time) ;
        }
        arrListURL.add(urlInfoObj);
        
        //If page instance match with previous it will do entry in arrList of pageinfo
        if(pageInstance != Integer.parseInt(strPageInstance))
        {
          pageInstance = Integer.parseInt(strPageInstance);
          PageInfo pageInfo = new PageInfo();
          pageInfo.setPageInstance(strPageInstance);
          pageInfo.setPageName(strPageName);
          //Start time first url of the record
          pageInfo.setStartTime(Double.parseDouble(start_time));
          //start time first url of each page
          pageInfo.setPageStartTime(Double.parseDouble(strStartTime));
          pageInfo.setPageAbsStartTime(Double.parseDouble(strAbsStartTime));
          
          pageInfo.setStartInterval(startInterval);
          //Storing starting index of url of that page
          pageInfo.setURLStartIndex(i + counter);
          arrListPage.add(pageInfo);
          counter++;
        }
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
      arrListPage.add(FAIL_QUERY);
      arrListURL.add(FAIL_QUERY);
      Log.stackTraceLog(className, "nsi_db_get_url_timing_detail", "", "", "Exception - ", e);
    }
  }

  /*
   * This method to get the data for table
   */
  public String[][] getDataForTable()
  {
    Log.debugLog(className, "getDataForTable", "", "", "Method called");
    
    //Creating row sum of number of url + number of pages + header
    String[][] arrResult = new String[arrListURL.size() + arrListPage.size() + 1][17];
    String[]arrDnsLookup = new String[arrListURL.size() + arrListPage.size() + 1];
    
    //This stored the highest value of req complete time in per page
    arrPagesReqCompTime = new double[arrListPage.size()];
    
    try
    {
      //If any case we are getting status FAIL_QUERY it will return with error message
      if(arrListURL.size() == 1)
      {
        //Only zero present means error from query
        if(arrListURL.get(0).toString().trim().equals("1"))
        {
          arrResult = new String[2][1];
          arrResult[0][0] = FAIL_QUERY;
          arrResult[1][0] = "Query Fail";
          return arrResult;
        }
      }
      
      //If no data only header will return
      arrResult[0][0] = "Object Name";
      arrResult[0][1] = "Status";
      arrResult[0][2] = "HTTP Code";
      arrResult[0][3] = "Connection Reused?";
      arrResult[0][4] = "SSL Reused?";
      arrResult[0][5] = "Start Time";
      arrResult[0][6] = "Total Time";
      arrResult[0][7] = "Connect Time";
      arrResult[0][8] = "SSL Time";
      arrResult[0][9] = "Request Time";
      arrResult[0][10] = "1st Byte";
      arrResult[0][11] = "Content Download";
      arrResult[0][12] = "Total Bytes";
      arrResult[0][13] = "Flow Path";
      arrResult[0][14] = "Flow Path Signature";
      arrResult[0][15] = "Instance";
      arrResult[0][16] = "Object";
      arrDnsLookup[0] = "DNS Lookup";
      
      //use to set data on index
      int rowIndex = 1;
      int urlCounter = 0; //user to get url information from array list
      for(int i = 0; i < arrListPage.size(); i++)
      {
        //getting pageinfo object to get data of page one by one
        PageInfo pageInfo = (PageInfo) arrListPage.get(i);
        //calculate total byte with in page
        int pageTotalBytes = 0;
        //calculate highest total time with in page
        double pageTotalTime = 0;
        //string higher water mark with in page
        double pageRequestCompletedTime = 0;
        
        rowIndex = pageInfo.getURLStartIndex() + 1;
        
        for(; urlCounter < arrListURL.size(); urlCounter++)
        {
          URLInfo urlInfo = (URLInfo) arrListURL.get(urlCounter);
          //If page instance does not match it will exit 
          //so we information of the next page of urls
          if(!pageInfo.getPageInstance().equals(urlInfo.getURLInstance()))
          {
            break;
          }

          arrResult[rowIndex][0] = "Conn" + urlInfo.getURLConnectionNumber() + ": " + urlInfo.getURLName();
          arrResult[rowIndex][1] = urlInfo.getURLStatus();
          arrResult[rowIndex][2] = urlInfo.getURLHTTPCode();
          arrResult[rowIndex][3] = urlInfo.getURLConReused();
          arrResult[rowIndex][4] = urlInfo.getURLHttpReused();
          if(isNDEContinuousEnable == 2)
            arrResult[rowIndex][5] = rptUtilsBean.setDateFormat("MM/dd/yy HH:mm:ss", Long.parseLong(urlInfo.getURLAbsStartTime()));
          else
            arrResult[rowIndex][5] = rptUtilsBean.timeInMilliSecToString(urlInfo.getURLStartTime(), 0);
          arrResult[rowIndex][6] = rptUtilsBean.convertTodecimal(Double.parseDouble(urlInfo.getURLtotalTime()), 3);
          arrResult[rowIndex][7] = rptUtilsBean.convertTodecimal(Double.parseDouble(urlInfo.getURLConnectTime()), 3);
          arrResult[rowIndex][8] = rptUtilsBean.convertTodecimal(Double.parseDouble(urlInfo.getURLSSLTime()), 3);
          arrResult[rowIndex][9] = rptUtilsBean.convertTodecimal(Double.parseDouble(urlInfo.getURLRequestTime()), 3);
          arrResult[rowIndex][10] = rptUtilsBean.convertTodecimal(Double.parseDouble(urlInfo.getURLFirstByte()), 3);
          arrResult[rowIndex][11] = rptUtilsBean.convertTodecimal(Double.parseDouble(urlInfo.getURLRequestCompletedTime()), 3);
          arrResult[rowIndex][12] = rptUtilsBean.convertNumberToCommaSeparate(urlInfo.getURLAppBytes());
          arrResult[rowIndex][13] = urlInfo.getFlowPathInstance();
          arrResult[rowIndex][14] = urlInfo.getFlowPathSignature();
          arrResult[rowIndex][15] = "Conn" + urlInfo.getURLConnectionNumber();
          arrResult[rowIndex][16] =  urlInfo.getURLName();
          arrDnsLookup[rowIndex] = rptUtilsBean.convertTodecimal(Double.parseDouble(urlInfo.getDNSLookupTime()), 3);
          
          //calulate total time used in page
          if(pageTotalTime < Double.parseDouble(urlInfo.getURLtotalTime()))
            pageTotalTime = Double.parseDouble(urlInfo.getURLtotalTime());
          
          //calculate total by per page
          pageTotalBytes = pageTotalBytes + Integer.parseInt(urlInfo.getURLAppBytes());
          
          //highest value if req comp time with in page
          if(pageRequestCompletedTime < urlInfo.getRequestCompTime())
          {
            pageRequestCompletedTime = urlInfo.getRequestCompTime();
          }
          arrPagesReqCompTime[i] = pageRequestCompletedTime;
          
          rowIndex++;
        }
        
        arrResult[pageInfo.getURLStartIndex()][0] = "Page" + pageInfo.getPageInstance() + " : " + pageInfo.getPageName();
        //Status
        arrResult[pageInfo.getURLStartIndex()][1] = "";
        
        if(arrResultPageStatus != null) 
        {
          for(int k = 0; k < arrResultPageStatus.length; k++)
          {
            if(arrResultPageStatus[k][0].trim().equals(pageInfo.getPageInstance()))
            {
              arrResult[pageInfo.getURLStartIndex()][1] = arrResultPageStatus[k][2];
              break;
            }
          }
        }
        arrResult[pageInfo.getURLStartIndex()][2] = "";
        arrResult[pageInfo.getURLStartIndex()][3] = "";
        arrResult[pageInfo.getURLStartIndex()][4] = "";
        arrResult[pageInfo.getURLStartIndex()][5] = "";
        arrResult[pageInfo.getURLStartIndex()][6] =rptUtilsBean.convertTodecimal(pageTotalTime,3) + "";
        arrResult[pageInfo.getURLStartIndex()][7] = "";
        arrResult[pageInfo.getURLStartIndex()][8] = "";
        arrResult[pageInfo.getURLStartIndex()][9] = "";
        arrResult[pageInfo.getURLStartIndex()][10] = "";
        
        pageInfo.setPageTotalBytes(pageTotalBytes + "");
        arrResult[pageInfo.getURLStartIndex()][11] = "";
        
        arrResult[pageInfo.getURLStartIndex()][12] = rptUtilsBean.convertNumberToCommaSeparate(pageTotalBytes + ""); 
        arrResult[pageInfo.getURLStartIndex()][13] = "";
        arrResult[pageInfo.getURLStartIndex()][14] = "";   
        arrResult[pageInfo.getURLStartIndex()][15] = "Page" + pageInfo.getPageInstance(); 
        arrResult[pageInfo.getURLStartIndex()][16] = pageInfo.getPageName(); 
        arrDnsLookup[pageInfo.getURLStartIndex()] = "";
        
      }
      setDnsLookupData(arrDnsLookup);
      return arrResult;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "getTableData", "", "", "Exception - ", e);
      arrResult = new String[2][1];
      arrResult[0][0] = FAIL_QUERY;
      arrResult[1][0] = "No records are found";
      return arrResult;
    }
  }
  
  /**
   * this method return bar information to plot in GUI
   * @return
   */
  public String[][] getDataForBar()
  {
    Log.debugLog(className, "getBarData", "", "", "Method called");
    String[][] arrResult = new String[arrListURL.size() + arrListPage.size() + 1][18];
    
    try
    {
    //If any case we are getting status FAIL_QUERY it will return with error message
      if(arrListURL.size() == 1)
      {
        //Only zero present means error from query
        if(arrListURL.get(0).toString().trim().equals("1"))
        {
          arrResult = new String[2][1];
          arrResult[0][0] = FAIL_QUERY;
          arrResult[1][0] = "Query Fail";
          return arrResult;
        }
      }
      
      //If no data only header will return
      arrResult[0][0] = "Object Name";
      arrResult[0][1] = "Start pix";
      arrResult[0][2] = "Intvl pix";
      arrResult[0][3] = "Intvl Sec";
      arrResult[0][4] = "Connect Pix";
      arrResult[0][5] = "Connect Sec";
      arrResult[0][6] = "SSL Pix";
      arrResult[0][7] = "SSL Sec";
      arrResult[0][8] = "Write Pix";
      arrResult[0][9] = "Write Sec";
      arrResult[0][10] = "First_pix";
      arrResult[0][11] = "First Sec";
      arrResult[0][12] = "Content Pix";
      arrResult[0][13] = "Content Sec";
      arrResult[0][14] = "Instance";
      arrResult[0][15] = "Object";
      arrResult[0][16] = "DNSLookup Pix";
      arrResult[0][17] = "DNSLookup Sec";
      
      int rowIndex = 1;
      int urlCounter = 0;
      double start_pix = 0;
      
      for(int i = 0; i < arrListPage.size(); i++)
      {
        PageInfo pageInfo = (PageInfo) arrListPage.get(i);
        int pageTotalBytes = 0;
        double pageTotalTime = 0;
        
        rowIndex = pageInfo.getURLStartIndex() + 1;
        
        for(; urlCounter < arrListURL.size(); urlCounter++)
        {
          URLInfo urlInfo = (URLInfo) arrListURL.get(urlCounter);
          
          //If page instance does not match it will exit 
          //so we information of the next page of urls
          if(!pageInfo.getPageInstance().equals(urlInfo.getURLInstance()))
          {
            break;
          }
          
          start_pix = (urlInfo.getURLStartInterval() / totalTime);
          double connect_pix = (roundValue(((Double.parseDouble(urlInfo.getURLConnectTime())) * plottedArea) / totalTime,3)*1000);
          double ssl_pix = (roundValue(((Double.parseDouble(urlInfo.getURLSSLTime())) * plottedArea) / totalTime,3)*1000);
          double write_pix = (roundValue(((Double.parseDouble(urlInfo.getURLRequestTime())) * plottedArea) / totalTime,3)*1000);
          double first_pix = (roundValue(((Double.parseDouble(urlInfo.getURLFirstByte())) * plottedArea) / totalTime,3)*1000);
          double content_pix = (roundValue(((Double.parseDouble(urlInfo.getURLRequestCompletedTime())) * plottedArea) / totalTime,3)*1000);
          double dnsLookupPix = (roundValue(((Double.parseDouble(urlInfo.getDNSLookupTime())) * plottedArea) / totalTime,3)*1000);  
          if (start_pix + connect_pix + ssl_pix + write_pix + first_pix + content_pix + dnsLookupPix > plottedArea)
          {
            if (content_pix > 0) content_pix--;
            else if (first_pix > 0) first_pix--;
            else if (write_pix > 0) write_pix--;
            else if (ssl_pix > 0) ssl_pix--;
            else if (connect_pix > 0) connect_pix--;
            else if (start_pix > 0) start_pix--;
            else if (dnsLookupPix > 0) dnsLookupPix--;
         }

          arrResult[rowIndex][0] = "Conn" + urlInfo.getURLConnectionNumber() + ": " + urlInfo.getURLName();
          if(totalTime == 0)  //In Case total time is 0 then to calculate start pix and interval pix not dividing by total time directly assigning 0
          {
            arrResult[rowIndex][1] = "0";
            arrResult[rowIndex][2] = "0";
          }
          else
          {
            arrResult[rowIndex][1] = start_pix + "";
            arrResult[rowIndex][2] = (((arrPagesReqCompTime[i] - pageInfo.getPageStartTime()) * plottedArea) /totalTime) + "";
          }
          
          arrResult[rowIndex][3] = ((arrPagesReqCompTime[i] - pageInfo.getPageStartTime()) / 1000) + "";
          //Connect Time
          arrResult[rowIndex][4] =  rptUtilsBean.convTo3DigitDecimal(connect_pix);
          arrResult[rowIndex][5] = urlInfo.getURLConnectTime();
          
          //SSL Time
          arrResult[rowIndex][6] = rptUtilsBean.convTo3DigitDecimal(ssl_pix);
          arrResult[rowIndex][7] = urlInfo.getURLSSLTime();
          //Request time
          arrResult[rowIndex][8] = rptUtilsBean.convTo3DigitDecimal(write_pix);
          arrResult[rowIndex][9] = urlInfo.getURLRequestTime();
          //First byte
          arrResult[rowIndex][10] = rptUtilsBean.convTo3DigitDecimal(first_pix);
          arrResult[rowIndex][11] = urlInfo.getURLFirstByte();
          //Download time
          arrResult[rowIndex][12] = rptUtilsBean.convTo3DigitDecimal(content_pix);
          arrResult[rowIndex][13] = urlInfo.getURLRequestCompletedTime();      
          
          arrResult[rowIndex][14] = "Conn" + urlInfo.getURLConnectionNumber();
          arrResult[rowIndex][15] = urlInfo.getURLName();
          
          arrResult[rowIndex][16] = rptUtilsBean.convTo3DigitDecimal(dnsLookupPix);
          arrResult[rowIndex][17] = urlInfo.getDNSLookupTime();
          
          //calulate total time used in page
          if(pageTotalTime < Double.parseDouble(urlInfo.getURLtotalTime()))
            pageTotalTime = Double.parseDouble(urlInfo.getURLtotalTime());
          
          rowIndex++;
        }
        
        start_pix = (pageInfo.getStartInterval()/ totalTime);
        double intvl_pix = (((arrPagesReqCompTime[i] - pageInfo.getPageStartTime()) * plottedArea) / totalTime);
        
        if (start_pix + intvl_pix > plottedArea ) intvl_pix--;
        
        arrResult[pageInfo.getURLStartIndex()][0] = "Page" + pageInfo.getPageInstance() + " : " + pageInfo.getPageName();
        
        if(totalTime == 0)  //In Case total time is 0 then to calculate start pix and interval pix not dividing by total time directly assigning 0
        {
          arrResult[pageInfo.getURLStartIndex()][1] = "0";
          arrResult[pageInfo.getURLStartIndex()][2] = "0";
        }

        else
        {
          arrResult[pageInfo.getURLStartIndex()][1] =  start_pix + "";
          arrResult[pageInfo.getURLStartIndex()][2] =  intvl_pix + "";
        }
        
        //arrResult[pageInfo.getURLStartIndex()][3] = ((arrPagesReqCompTime[i] - pageInfo.getPageStartTime()) /1000) + "";;
        arrResult[pageInfo.getURLStartIndex()][3] = rptUtilsBean.convertTodecimal(pageTotalTime,3) + "";
        arrResult[pageInfo.getURLStartIndex()][4] = "Connect Pix";
        arrResult[pageInfo.getURLStartIndex()][5] = "Connect Sec";
        arrResult[pageInfo.getURLStartIndex()][6] = "SSL Pix";
        arrResult[pageInfo.getURLStartIndex()][7] = "SSL Sec";
        arrResult[pageInfo.getURLStartIndex()][8] = "Write Pix";
        arrResult[pageInfo.getURLStartIndex()][9] = "Write Sec";
        arrResult[pageInfo.getURLStartIndex()][10] = "First_pix";
        arrResult[pageInfo.getURLStartIndex()][11] = "First Sec";
        arrResult[pageInfo.getURLStartIndex()][12] = "Content Pix";
        arrResult[pageInfo.getURLStartIndex()][13] = "Content Sec";
        arrResult[pageInfo.getURLStartIndex()][14] = "Page" + pageInfo.getPageInstance();
        arrResult[pageInfo.getURLStartIndex()][15] =  pageInfo.getPageName();
        arrResult[pageInfo.getURLStartIndex()][16] =  "";
        arrResult[pageInfo.getURLStartIndex()][17] =  "";
      }

      return arrResult;
    }
    catch (Exception e) 
    {
      arrResult = new String[2][1];
      arrResult[0][0] = FAIL_QUERY;
      arrResult[1][0] = "No records are found";
      return arrResult;
    }
  }
  
  /**
   * This method insert 1D array in 2D array at specified index and shift the position of others.
   * 
   * @return 2D array
   */
  
  public String[][] insertInTwoDimArray(String [][]arrData, int index, String []arrInsertedData)
  {
    String [][]arrFinalArray = null;
    try
    {
      if(arrInsertedData == null || arrData == null)
	return arrData;
      
      //creating dimension.
      arrFinalArray = new String[arrData.length][arrData[0].length+1];
      for(int i = 0; i < arrData.length; i++)
      {
	int kk = 0;
	for(int k = 0; k < arrData[0].length; k++)
	{
	  if(k == index)
	  {
	    arrFinalArray[i][kk] = arrInsertedData[i];
	    kk++;
	  }
	   	  
	  arrFinalArray[i][kk] = arrData[i][k];
	  kk++;
	}
      }
      return arrFinalArray;    
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return arrData;
    }
  }

  /*
   * This return 10 length array to shoe scaling for bar
   * change Scale values upto 15.(As plotted Area change to 900).
   */
  public String[] getScaleForBar()
  {
    try
    {
      String arrScale[] = new String[15];
      for(int i = 1; i <= 15; i++)
      {
        arrScale[i - 1] = rptUtilsBean.convertTodecimal(((i * totalTime)/15000), 3);
      }
      return arrScale;
    }
    catch (Exception e) 
    {
      return new String[]{"0","1","2","3","4","5","6","7","8","9","10","11","12","13","14"};
    }
  }

  public String getTotalByte()
  {
    return totalBytesSummation + "";
  }
  /*
   * ***  Round Function  use to calculate Pix for URL BAR
   */
  float roundValue(double num, int Rpl)
  {
    float p = (float)Math.pow(10,Rpl);
    num = num * p;
    float tmp = Math.round(num);
    return (float)tmp/p;    
  }


  //Check whether connection is resued or not
  private String calculateReused(String Reused)
  {
    String strReused = "";
    if(Reused.equals("t"))
      strReused = "Y";
    else
      strReused = "N";

    return strReused;
  }

  //Method to calculate TotalTime for Bar
  public double calculateBarTotalTime(String hightestValueOfReqCompTime, String strFirstRowOfStartTime)
  {
    double totalTime = Double.parseDouble(hightestValueOfReqCompTime) - Double.parseDouble(strFirstRowOfStartTime);
    return totalTime;
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
  Name : URLInfo
  **  This class is use to Store Complete data
  *   It has 2 method  To Set Data and To get Data
  *   and Use to store Some other variable which will be used furter
  *
  *   Variables
  *   Get methods
  *   Set Methods
  */
  private class URLInfo
  {
  //This Variables will be use to generate  both Table Data and Bar
    String pageInstance;         //For mapping purpose between arrListURL and arrListPage we have same variable name for Page Instance and url Instance
    String urlConnectionNumber;
    String urlName;

    //This Variables will be use to generate Table Data only
    String urlStartTime;
    String urlAbsStartTime;
    String urlStatus;
    String urlHttpCode;
    String urlConnectionReused;
    String urlSSLlReused;
    String urlconnectTime;
    String urlSSLTime;
    String urlRequestTime;
    String urlFirstByte;
    String urlContentDownload;
    String urlTotalTime;
    String urlTotalBytes;
    String urlFlowPathInstance;
    String urlFlowPathSignature;
    String urlDnsLookup;
    String urlDnsLookupTime;

    //This variables will be use to generate Bar Data
    
    double reqCompTime = 0;
    double startInterval = 0;
    String urlBarConnectPix;
    String urlBarConnectSec;
    String urlBarSSLTime;
    String urlBarSSLSec;
    String urlBarReqTime;
    String urlBarReqSec;
    String urlBarFirstByteTime;
    String urlBarFirstByteSec;
    String urlBarReqCompTime;
    String urlBarReqCompSec;
    


    /*******************************Setting Info For URL Table data *********************************************/
    public void setURLInstance(String urlinstance)
    {
      if(urlinstance != null)
        this.pageInstance = urlinstance;
      else
        this.pageInstance = "";
    }

    public void setURLConnectionNumber(String urlconnectionnumber)
    {
      if(urlconnectionnumber != null)
        this.urlConnectionNumber = urlconnectionnumber;
      else
        this.urlConnectionNumber = "";
    }

    public void setURLName(String urlname)
    {
      if(urlname != null)
        this.urlName = urlname;
      else
        this.urlName = "";
    }


    public void setURLStartTime(String startTime)
    {
      if(startTime != null)
        this.urlStartTime = startTime;
      else
        this.urlStartTime = "";
    }

    public void setURLAbsStartTime(String absStartTime)
    {
      if(absStartTime != null)
        this.urlAbsStartTime = absStartTime;
      else
        this.urlAbsStartTime = "";
    }
    
    public String getURLAbsStartTime()
    {
      return urlAbsStartTime;
    }
    
    public void setURLStatus(String urlstatus)
    {
      if(urlstatus != null)
        this.urlStatus = urlstatus;
      else
        this.urlStatus = "";
    }

    public void setURLHTTPCode(String httpCode)
    {
      if(httpCode != null)
        this.urlHttpCode = httpCode;
      else
        this.urlHttpCode = "";
    }
    
    public void setRequestCompTime(double reqCompTime)
    {
      this.reqCompTime = reqCompTime;
    }

    public void setURLConReused(String conReused)
    {
      if(conReused != null)
        this.urlConnectionReused = conReused;
      else
        this.urlConnectionReused = "";
    }

    public void setURLHttpReused(String httpReused)
    {
      if(httpReused != null)
        this.urlSSLlReused = httpReused;
      else
        this.urlSSLlReused = "";
    }

    public void setURLConnectTime(String connectTime)
    {
      if(connectTime != null)
        this.urlconnectTime = connectTime;
      else
        this.urlconnectTime = "";
    }

    public void setURLSSLTime(String sslTime)
    {
      if(sslTime != null)
        this.urlSSLTime = sslTime;
      else
        this.urlSSLTime = "";
    }

    public void setURLRequestTime(String requestTime)
    {
      if(requestTime != null)
        this.urlRequestTime = requestTime;
      else
        this.urlRequestTime = "";
    }

    public void setURLFirstByte(String firstByte)
    {
      if(firstByte != null)
        this.urlFirstByte = firstByte;
      else
        this.urlFirstByte= "";
    }

    public void setURLRequestCompletedTime(String requestCompletedTime)
    {
      this.urlContentDownload = requestCompletedTime;
    }

    public void setURLtotalTime(String totalTime)
    {
      if(totalTime != null)
        this.urlTotalTime = totalTime;
      else
        this.urlTotalTime = "";
    }

  public void setURLAppBytes(String appBytes)
  {
    if(appBytes != null)
      this.urlTotalBytes = appBytes;
    else
      this.urlTotalBytes = appBytes;

  }

    public void setFlowPathInstance(String flowPathInstance)
    {
      if(flowPathInstance != null)
        this.urlFlowPathInstance = flowPathInstance;
      else
        this.urlFlowPathInstance = "";
    }

    public void setFlowPathSignature(String flowPathSignature)
    {
      if(flowPathSignature != null)
        this.urlFlowPathSignature = flowPathSignature;
      else
        this.urlFlowPathSignature = "";
    }
    
    public void setDNSLookup(String dnsLookup)
    {
      if(dnsLookup != null)
        this.urlDnsLookup = dnsLookup;
      else
        this.urlDnsLookup = "";
    }
    
    public void setDNSLookupTime(String dnsLookupTime)
    {
      if(dnsLookupTime != null)
        this.urlDnsLookupTime = dnsLookupTime;
      else
        this.urlDnsLookupTime = "";
    }

    /*******************************Setting Info For URL Bar Data *********************************************/
    
    public void setURLStartInterval(double startInterval)
    {
      this.startInterval = startInterval;
    }
    public void setURLConnectPix(String connectPix)
    {
      if(connectPix != null)
        this.urlBarConnectPix = connectPix;
      else
        this.urlBarConnectPix = "";
    }

    public void setURLConnectSec(String connectSec)
    {
      if(connectSec != null)
        this.urlBarConnectSec = connectSec;
      else
        this.urlBarConnectSec = "";
    }

    public void setURLBarSSLTime(String sslTime)
    {
      if(sslTime != null)
        this.urlBarSSLTime = sslTime;
      else
        this.urlBarSSLTime = "";
    }

    public void setURLSSLSec(String connectSec)
    {
      if(connectSec != null)
        this.urlBarSSLSec = connectSec;
      else
        this.urlBarSSLSec = "";
    }

    public void setURLReqTime(String connectSec)
    {
      if(connectSec != null)
        this.urlBarReqTime = connectSec;
      else
        this.urlBarReqTime = "";
    }

    public void setURLReqSec(String connectSec)
    {
      if(connectSec != null)
        this.urlBarReqSec = connectSec;
      else
        this.urlBarReqSec = "";
    }


    public void setURLFirstByteTime(String connectSec)
    {
      if(connectSec != null)
        this.urlBarFirstByteTime = connectSec;
      else
        this.urlBarFirstByteTime = "";
    }

    public void setURLFirstByteSec(String connectSec)
    {
      if(connectSec != null)
        this.urlBarFirstByteSec = connectSec;
      else
        this.urlBarFirstByteSec = "";
    }

    public void setURLReqCompTime(String reqCompTime)
    {
      if(reqCompTime != null)
        this.urlBarReqCompTime = reqCompTime;
      else
        this.urlBarReqCompTime = "";
    }

    public void setURLReqCompSec(String reqCompSec)
    {
      if(reqCompSec != null)
        this.urlBarReqCompSec = reqCompSec;
      else
        this.urlBarReqCompSec = "";
    }


    /*******************************Getting Info For URL Table Data *********************************************/

    public String getURLInstance()
    {
      return pageInstance;
    }

    public String getURLConnectionNumber()
    {
      return urlConnectionNumber;
    }

    public String getURLName()
    {
      return urlName;
    }

    public String getURLStartTime()
    {
      return urlStartTime;
    }

    public double getRequestCompTime()
    {
      return reqCompTime;
    } 
    
    public String getURLStatus()
    {
      return urlStatus;
    }
    public String getURLHTTPCode()
    {
      return urlHttpCode;
    }

    public String getURLConReused()
    {
      return urlConnectionReused;
    }

    public String getURLHttpReused()
    {
      return urlSSLlReused;
    }

    public String getURLConnectTime()
    {
      return urlconnectTime;
    }

    public String getURLSSLTime()
    {
      return urlSSLTime;
    }

    public String getURLRequestTime()
    {
      return urlRequestTime;
    }

    public String getURLFirstByte()
    {
      return urlFirstByte;
    }

    public String getURLRequestCompletedTime()
    {
      return urlContentDownload;
    }

    public String getURLAppBytes()
    {
      return urlTotalBytes;
    }
  
    public double getURLStartInterval()
    {
      return startInterval;
    }

    public String getURLtotalTime()
    {
      return urlTotalTime;
    }

    public String getFlowPathInstance()
    {
      return urlFlowPathInstance;
    }

    public String getFlowPathSignature()
    {
      return urlFlowPathSignature;
    }
    
    public String getDNSLookup()
    {
      return urlDnsLookup;
    }
    
    public String getDNSLookupTime()
    {
      return urlDnsLookupTime;
    }

    /*******************************Getting Info For URL Bar Data *********************************************/
    public String getURLConnectPix()
    {
      return urlBarConnectPix;
    }

    public String getURLConnectSec()
    {
      return urlBarConnectSec;
    }

    public String getURLBarSSLTime()
    {
      return urlBarSSLTime;
    }

    public String getURLSSLSec()
    {
      return urlBarSSLSec;
    }

    public String getURLReqTime()
    {
      return urlBarReqTime;
    }

    public String getURLReqSec()
    {
      return urlBarReqSec;
    }

    public String getURLFirstByteTime()
    {
      return urlBarFirstByteTime;
    }

    public String getURLFirstByteSec()
    {
      return urlBarFirstByteSec;
    }

    public String getURLReqCompTime()
    {
      return urlBarReqCompTime;
    }

    public String getURLReqCompSec()
    {
      return urlBarReqCompSec;
    }

  }

  /*
  Name : PageInfo
  **  This class is use to Store Complete data of Page
  *   It has 2 method   Set Data and  get Data
  *   and Use to store Some other variable which will be used furter
  *
  *   startEntryURL : It will from which postion it should get data of url of particular Page
  *   numOfEntriesURL : It will tell that how many entries it should get data of url of particular page
  *
  *   Variables
  *   Get methods
  *   Set Methods
  */
  private class PageInfo
  {
  //this variables will be use for both Table Data and Bar data
    String pageInstance;
    String pageName;

    //It will be use for Table data
    String pageTotalBytes;
    String pageStatus;

    //It will be use for Bar Data
    String pageStartPix;
    double pageStartTime = 0;                            //It is use to calculate Start Pix of URL for BAR pageStartTime - currentRecord Start Time
    String pageIntvlPix;
    String pageIntvlSec;
    String pageTotalTime;

    double pageStartInterval = 0; 
    int URLStartIndex = 0;
    //tell the start point and End point of all URL for particular page
    String startEntryURL;
    String numOfEntriesURL;

    String totalTimeForBar;
    String intervalForBar;
    String startTimeForBar;

    double start_time = 0;
    double abs_start_time = 0;
    /*******************************Getting Info For Page*********************************************/
    public void setPageInstance(String pageinstance)
    {
      this.pageInstance = pageinstance;
    }

    public void setPageName(String pagename)
    {
      this.pageName = pagename;
    }
    public void setStartTime(double start_time)
    {
      this.start_time = start_time;
    }

    public void setPageAbsStartTime(double abs_start_time)
    {
      this.abs_start_time = abs_start_time;
    }
    
    public double setPageAbsStartTime()
    {
      return abs_start_time;
    }
    
    public void setPageTotalBytes(String pagetotalbytes)
    {
      this.pageTotalBytes = pagetotalbytes;
    }

    public void setPageStatus(String pagestatus)
    {
      this.pageStatus = pagestatus;
    }

    public void setPageStartPix(String startpix)
    {
      this.pageStartPix = startpix;
    }

    public void setPageIntvlPix(String intvlpix)
    {
      this.pageIntvlPix = intvlpix;
    }
    
    public void setPageIntvlSec (String intvlsec)
    {
      this.pageIntvlSec = intvlsec;
    }
    
    public void setPageTotalTime(String totaltime)
    {
      this.pageTotalTime = totaltime;
    }

    public void setStartEntryOfURL(String startentry)
    {
      this.startEntryURL = startentry;
    }

    public void setNumOfEntryOfURL(String noofentry)
    {
      if(noofentry != null)
        this.numOfEntriesURL = noofentry;
      else
      this.numOfEntriesURL = "";
    }

    public void setPageStartTime(double pageStartTime)
    {
      this.pageStartTime = pageStartTime;
    }
    
    public void setStartInterval(double pageStartInterval)
    {
      this.pageStartInterval = pageStartInterval;
    }

    public void setURLStartIndex(int URLStartIndex)
    {
      this.URLStartIndex = URLStartIndex;
    }
    
    //Storing Data so that we can use it while creating object of URL and can use then which is calculated in Page
    public void setTotalTimeForBar(String totaltimeforbar)
    {
      if(totaltimeforbar != null)
        this.totalTimeForBar =  totaltimeforbar;
      else
        this.totalTimeForBar =  "";
    }

    public void setIntervalForBar(String totaltimeforbar)
    {
      if(totaltimeforbar != null)
        this.intervalForBar =  totaltimeforbar;
      else
        this.intervalForBar =  "";
    }

    public void setStartTimeForBar(String totaltimeforbar)
    {
      if(totaltimeforbar != null)
        this.startTimeForBar =  totaltimeforbar;
      else
        this.startTimeForBar =  "";
    }

    /*******************************Getting Info For Page*********************************************/
    public String getPageInstance()
    {
      return pageInstance;
    }

    public String getPageName()
    {
      return pageName;
    }
    
    public double getStartTime()
    {
      return start_time;
    }

    public double getStartInterval()
    {
      return pageStartInterval;
    }
    
    public String getPageTotalBytes()
    {
      return pageTotalBytes;
    }

    public String getPageStatus()
    {
      return pageStatus;
    }
    
    public int getURLStartIndex()
    {
      return URLStartIndex;
    }    

    public String getPageStartPix()
    {
      return pageStartPix;
    }

    public String getPageIntvlPix()
    {
      return pageIntvlPix;
    }

    public String getPageIntvlSec()
    {
      return pageIntvlSec;
    }

    public String getPageTotalTime()
    {
      return pageTotalTime;
    }

    public double getPageStartTime()
    {
      return pageStartTime;
    }
    
    //Storing Data so that we can use it while creating object of URL and can use then which is calculated in Page
    public String getTotalTimeForBar()
    {
      return totalTimeForBar;
    }

    public String getIntervalForBar()
    {
      return intervalForBar;
    }

    public String getStartTimeForBar()
    {
      return startTimeForBar;
    }

    public String getStartEntryOfURL()
    {
      return startEntryURL;
    }

    public String getNoOfEntriesOfURL()
    {
      return numOfEntriesURL;
    }

  }
  
  
  public static void main(String args[])
  {
  boolean ndKeyValue = false;
  int objType = 1;
  DrillDownObjDetailTime DrillDownObjDetailTime = new DrillDownObjDetailTime("21100", "netstorm",  ndKeyValue , objType, 2);
  String cmdArg = "/cgi-bin/findflight 0 2 26249 106 5 53 0";
  Vector vecTemp = new Vector();
  
  String[][] arrDataValuesFinalTable = null;
  String[][] arrPageStatus = null;
  StringBuffer sortColumnType = new StringBuffer();
  StringBuffer bytesSummation = new StringBuffer();                   //Use to get Total no of Bytes of all records and Total Time to generateScale
  StringBuffer defaultSorting = new StringBuffer();

  DrillDownBreadCrumb breadCrumb = new DrillDownBreadCrumb("-1", "query1", 0);
  DrillDownObjDetailTime.execute_get_obj_timing_detail(cmdArg,objType, sortColumnType, defaultSorting, breadCrumb);

  /**arrDataValuesFinalTable = (String[][]) arrTemp[0];
  
  for(int i=1; i <= 10; i++)
    System.out.println((double)530*i/10000.0);
   
  System.out.println("Summation is " + bytesSummation.toString());
  **/
  
  String[][] getDataForTable = DrillDownObjDetailTime.getDataForTable();
  for(int q =0; q < getDataForTable.length; q++)
  {
    for(int qq =0 ; qq < getDataForTable[q].length; qq++)
    {
      System.out.print(getDataForTable[q][qq] + "|");
    }
    System.out.println("\n");
  }

  System.out.println("---------------------------------------------------------------------------------");
  /**String[][] arrDataValuesFinalBar = DrillDownObjDetailTime.getDataForBar();
  for(int q =0; q < arrDataValuesFinalBar.length; q++)
  {
    for(int qq =0 ; qq < arrDataValuesFinalBar[q].length; qq++)
    {
      System.out.print(arrDataValuesFinalBar[q][qq] + "|");
    }
    System.out.println("\n");
  }**/
  }
}

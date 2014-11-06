package pac1.Bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

public class NTPBean
{
  //Filter|Type|Method|URL|Count|Txn-Name|Host|FF1|bodyPath|URL-Pattern
  private final int filterInx = 0;
  private final int typeIdx = 1;
  private final int methodIdx = 2;
  private final int urlIdx = 3;
  private final int countIdx = 4;
  private final int txnIdx = 5;
  private final int hostIdx = 6;
  private final int ff1Idx = 7;
  private final int bodyIdx = 8;
  private final int urlPtnIdx = 9;
  private final int totalMatchCountIdx = 10;
  private final int rowIdIdx = 11;

  private String className = "NTPBean";
  private String userName = "";
  private String scenarioName = null;
  private String URL_LIST_FILE_PATH = null;
  private String URL_LIST_DIR_PATH = null;
  private String SUMMARY_REPORT_PATH = null;
  final private String profileDirPath = Config.getWorkPath() + "/replay_profiles/";
  private CmdExec cmdExec = new CmdExec();

  //variable list to be set and get and used as agruments for shell
  private String fieldNum = "";
  private String startDateTime = "";
  private String endDateTime = "";
  private String file[] = null;
  private String protocol[] = null;
  private String fileSize[] = null;
  private String fileCount[] = null;
  private String body[] = null;
  private String fieldValue[] = new String[]{"4" , "6" , "10" , "12" , "13" , "9" , "11", "15"};
  private String inline_mode = "";
  private String cookie = "";
  private String num_nvm = "";
  private String cookieMode = "Cookie";
  private String profileName = "";
  private String httpHost = "";
  private String ignore_URLs = "";
  private String httpsHost = "";
  private String tooltipString = "";
  private boolean isAutoRedirect = false;
  String []arrPatterns = null;
  String processedRequests = "";

  ArrayList<ArrayList> findMatchingURL = new ArrayList<ArrayList>();

  //Variables used for pagination----------------
  private String[][] arrUrlList = null;
  private ArrayList<String[]> arrFilteredUrlList = null;
  private int pageNum = -1;
  private int numOfRowsPerPage = -1;
  private int totalVisiblePage = -1;
  private int totalActualPage = -1;
  private int pageOfFirstMatching = -1;
  private int numOfMatching = -1;
  private int totalRowCount = 0;
  private int totalVisibleRowCount = 0;
  private int totalRequests = 0;
  private int totalRedirected = 0;
  private int totalTimeBasedFiltered = 0;
  private int totalNonSessionRequest = 0;
  private int totalBadRequest = 0;
  private int totalFiltered = 0;
  private int totalInline = 0;
  private int totalTransDefined = 0;
  private int totalMainWithoutTX = 0;
  private int totalInlineFilteredRequest = 0;
  private int successStatus = -1;
  private HashMap<Integer, String> rowIdPathMap = new HashMap<Integer, String>();
  private HashMap<Integer, String> rowIdBodyMap = new HashMap<Integer, String>();
  HashMap<Integer, ArrayList> arrQueryList = new HashMap<Integer, ArrayList>();
  
  //for taking filter data.
  ArrayList<String[]> filterList = new ArrayList<String[]>();
  //---------------------------------------------------

  public NTPBean(String scenarioName, String userName)
  {
    Log.debugLog(className, "NTPBean", "", "", "constructer created with scenarioName:"+scenarioName +" userName:"+userName);
    this.scenarioName = scenarioName.trim();
    this.userName = userName;
    URL_LIST_FILE_PATH = Config.getWorkPath() + "/ReplayAccessLogs/"+ scenarioName +"/config/url.list";
    URL_LIST_DIR_PATH = Config.getWorkPath() + "/ReplayAccessLogs/"+ scenarioName +"/config";
    SUMMARY_REPORT_PATH = Config.getWorkPath() + "/ReplayAccessLogs/"+ scenarioName +"/summary.report";
  }

  //setter methods to pass as a arguments to shell.
  public void setFieldNum(String arg)
  {
    fieldNum = arg;
  }

  // for auto redirect.
  public void setAutoRedirect(boolean isAutoRedirect)
  {
    this.isAutoRedirect = isAutoRedirect;
  }

  public void setStartDateTime(String arg)
  {
    startDateTime = arg;
  }

  public void setEndDateTime(String arg)
  {
    endDateTime = arg;
  }

  public void setFile(String[] arg)
  {
    file = arg;
  }
  
  public void setFileSize(String[] arg)
  {
    fileSize = arg;
  }

  public void setFileCount(String[] arg)
  {
    fileCount = arg;
  }


  public void setProtocol(String[] arg)
  {
    protocol = arg;
  }

  public void setBody(String[] arg)
  {
    body = arg;
  }

  public void setFieldValue(String[] arg)
  {
      fieldValue = arg;
  }

  public void setInlineMode(String arg)
  {
    inline_mode = arg;
  }

  public void setCookie(String arg)
  {
    cookie = arg;
  }

  public void setNumNvm(String arg)
  {
    num_nvm = arg;
  }

  public void setCookieMode(String arg)
  {
    cookieMode = arg;
  }

  public void setHttpHost(String arg)
  {
    httpHost = arg;
  }

  public void setHttpsHost(String arg)
  {
    httpsHost = arg;
  }

  public void setIgnoreURLs(String arg)
  {
    ignore_URLs = arg;
  }
  

  //getter methods to pass as a arguments to shell.
  public String getFieldNum()
  {
    return fieldNum;
  }
  
  public String getProcessedReq()
  {
    return processedRequests;
  }

  public String getStartDateTime()
  {
    return startDateTime;
  }

  public boolean getAutoRedirect()
  {
    return isAutoRedirect;
  }

  public String getEndDateTime()
  {
    return endDateTime;
  }

  public String[] getFile()
  {
    return file;
  }
  
  public String[] getFileSize()
  {
    return fileSize;
  }
  
  public String[] getFileCount()
  {
    return fileCount;
  }

  public String[] getProtocol()
  {
    return protocol;
  }

  public String[] getBody()
  {
    return body;
  }

  public String[] getFieldValue()
  {
    return fieldValue;
  }

  public String getInlineMode()
  {
    return inline_mode;
  }

  public String getCookie()
  {
    return cookie;
  }

  public String getNumNvm()
  {
    return num_nvm;
  }

  public String getCookieMode()
  {
    return cookieMode;
  }

  public void setProfileName(String arg)
  {
    profileName = arg;
  }

  public String getProfileName()
  {
    return profileName;
  }

  //getters used for pagination--------------------------
  public int getPageNum()
  {
    return pageNum;
  }

  public int getNumOfRowsPerPage()
  {
    return numOfRowsPerPage;
  }

  public int getToatlVisiblePage()
  {
    return totalVisiblePage;
  }

  public int getTotalActualPage()
  {
    return totalActualPage;
  }

  public int getPageOfFirstMatch()
  {
    return pageOfFirstMatching;
  }

  public int getNumberOfMatching()
  {
    return numOfMatching;
  }

  public String getHTTPHost()
  {
    return httpHost;
  }

  public String getHTTPSHost()
  {
    return httpsHost;
  }

  public String getIgnoreURLs()
  {
    return ignore_URLs;
  }

  public String getTooltipOfProcessedRequest()
  {
    return tooltipString;
  }


  // Method to get String data in buffer.
  public StringBuffer getVectorDataInStringBuff(Vector vecData)
  {
    StringBuffer strBuff = new StringBuffer();

    for (int i = 0; i < vecData.size(); i++)
    {
      strBuff.append(vecData.get(i).toString() + "<br>");
    }
    return strBuff;
  }
  
  //encoding special charecters by html encoding.
  public String encodeHTMLCharecters(String url)
  {
    try
    {
      final StringBuilder result = new StringBuilder();
      
      StringCharacterIterator iterator = new StringCharacterIterator(url);
      char character = iterator.current();
      
      while (character != StringCharacterIterator.DONE)
      {	      
      
       if (character == '\"') 
         result.append("&quot;");
       
       else if(character == '<') 
	 result.append("&lt;");
	    
       else if (character == '>') 
         result.append("&gt;");
       
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
      return result.toString();    
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return "";
    }
  }
  
  //for regular expression.
  public static String forRegex(String aRegexFragment)
  {
    final StringBuilder result = new StringBuilder();
    final StringCharacterIterator iterator =  new StringCharacterIterator(aRegexFragment);
    char character =  iterator.current();
    while (character != StringCharacterIterator.DONE )
    {
      /*
       All literals need to have backslashes doubled.
      */
      if (character == '.') {
        result.append("\\.");
      }
      else if (character == '\\') {
        result.append("\\\\");
      }
      else if (character == '?') {
        result.append("\\?");
      }
      else if (character == '*') {
        result.append("\\*");
      }
      else if (character == '+') {
        result.append("\\+");
      }
      else if (character == '&') {
        result.append("\\&");
      }
      else if (character == ':') {
        result.append("\\:");
      }
      else if (character == '{') {
        result.append("\\{");
      }
      else if (character == '}') {
        result.append("\\}");
      }
      else if (character == '[') {
        result.append("\\[");
      }
      else if (character == ']') {
        result.append("\\]");
      }
      else if (character == '(') {
        result.append("\\(");
      }
      else if (character == ')') {
        result.append("\\)");
      }
      else if (character == '^') {
        result.append("\\^");
      }
      else if (character == '$') {
        result.append("\\$");
      }
      else {
      //the char is not a special one
      //add it to the result as is
       result.append(character);   
      }
      character = iterator.next();
    }
    return result.toString();
  }
  
  //for URL Encoding.
  public static String URLEncoding(String aURLFragment)
  {
    String result = null;
    try
    {
      result = URLEncoder.encode(aURLFragment, "UTF-8");
    }
    catch (UnsupportedEncodingException ex)
    {
      throw new RuntimeException("UTF-8 not supported", ex);
    }
    return result;
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
  
  public void setBody(int rowId, String body)
  {
    //set path only in first time because user can update path in the mid by saves or update profile option.
    if(rowIdPathMap.get(rowId) == null)
      rowIdPathMap.put(rowId, arrUrlList[rowId][bodyIdx]);
    rowIdBodyMap.put(rowId, body);
  }

  public String getBody(int rowId)
  {
    ReplayUrlFilterPattern replayUrlPtn = new ReplayUrlFilterPattern("", userName);
    if(rowIdBodyMap.get(rowId) == null)
    {
      if(arrUrlList[rowId][bodyIdx].equalsIgnoreCase("NA"))
        return "";
      else
        return replayUrlPtn.getRequestPostBody(arrUrlList[rowId][bodyIdx]);
    }
    else
    {
      return rowIdBodyMap.get(rowId);
    }
  }
  
  public ArrayList<String> getQueryURLList(int rowId)
  {
    if(arrQueryList.containsKey(rowId))
    {
      return arrQueryList.get(rowId);	    
    }
    else
    {
      return new ArrayList<String>();    
    }
  }

  public int getToatlRowCount()
  {
    return totalRowCount;
  }

  public int getTotalVisibleRowCount()
  {
    return totalVisibleRowCount;
  }
  
  public int getSuccessStatus()
  {
    return successStatus;
  }

  //--------------------------------------------------

  /**
   * Function genUrlList exceute shell 'ntp_al_gen_url_list'
   * and return data in 2D array having fields 'methodName|URL|Count'
   * optaions are describe below
   * --field_number
   * --start_date
   * --end_date
   * --file <file1,file2>
   * --protocol <http/https>
   * --body <yes/no>
   * --inline_mode <yes/no>
   * --cookie <cookie-name>
   * --scenario <proj/sub - proj/sce>
   * @return
   */
  public void genUrlList(StringBuffer errMsg)
  {
    Log.debugLog(className, "genUrlList", "", "", "method called");
    try
    {
      String strCmdArgs = "";
      final String strCmdName = "ntp_al_gen_url_list";

      strCmdArgs = " --scenario " + scenarioName;

      Log.debugLog(className, "genUrlList", "", "", "Executing command: "+ strCmdName + " with arguments: "+strCmdArgs);

      Vector vecCmdOutput = new Vector();
      boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);
      if(!bolRsltFlag)
      {
        Log.debugLog(className, "genUrlList", "", "", "Error in excetuing command.");
        for(int i = 0; i < vecCmdOutput.size(); i++)
        {
          String str = vecCmdOutput.get(i).toString();
          errMsg.append(str);
          
          if(i != vecCmdOutput.size() - 1)
            errMsg.append("\n");
          
          totalRowCount = 1;
        }
      }
      else
      {

      }
      
      //Reading url.list file.
      Long startTime = System.currentTimeMillis();
      Log.debugLog(className, "genUrlList", "", "", "reading file start = "+System.currentTimeMillis());
      
      readUrlListFile(); // +1 for including header.
      
      Log.debugLog(className, "genUrlList", "", "", "file readed in ms = "+(System.currentTimeMillis()-startTime));
  }
  catch(Exception e)
  {
    e.printStackTrace();
    Log.stackTraceLog(className, "genUrlList", "", "", "", e);
  }
}
  
  private void readUrlListFile()
  {
    try
    {
      int length = 1;	    
	          	    
      BufferedReader url_reader = new BufferedReader(new FileReader(URL_LIST_FILE_PATH));   
      String file_row = "";
      int rows = 0;
      
      String countInfo = url_reader.readLine();
      
      //Reading first row from file for getting total Records information.
      if(countInfo != null)
      {
        String lastRow = countInfo.trim();
        
        if(lastRow.toLowerCase().startsWith("total"))
        {
           String[] temp1 = rptUtilsBean.split(lastRow, ";");

           String[] tempReq = rptUtilsBean.split(temp1[0], "=");
           totalRequests = Integer.parseInt(tempReq[1]);

           String[] tempRedirected = rptUtilsBean.split(temp1[1], "=");
           totalRedirected = Integer.parseInt(tempRedirected[1]);

           String[] tempFilter = rptUtilsBean.split(temp1[2], "=");
           totalTimeBasedFiltered = Integer.parseInt(tempFilter[1]);

           String[] tempBadRequest = rptUtilsBean.split(temp1[3], "=");
           totalBadRequest = Integer.parseInt(tempBadRequest[1]);

           String[] tempNonSession = rptUtilsBean.split(temp1[4], "=");
           totalNonSessionRequest = Integer.parseInt(tempNonSession[1]);

           String[] tempFiteredInline = rptUtilsBean.split(temp1[5], "=");
           totalInlineFilteredRequest = Integer.parseInt(tempFiteredInline[1]);
           
           String tempTotal[] = rptUtilsBean.split(temp1[6], "=");
           totalRowCount = Integer.parseInt(tempTotal[1].trim());     
        }
      }
      
      length = totalRowCount + 1; //  +1 for including header.
      
      //setting dimension.
      arrUrlList = new String[length][10];
      
      while((file_row = url_reader.readLine()) != null)
      {
	//for error handling
	if(rows > length-1)
	  break;
	 
	String rowData[] = rptUtilsBean.split(file_row, "|");
	arrUrlList[rows] = rowData.clone();
	rows++;
	
      }
      url_reader.close();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  private void saveBody()
  {
    Log.debugLog(className, "saveBody", "", "", "method called");
    try
    {
      Iterator it = rowIdPathMap.entrySet().iterator();
      while (it.hasNext())
      {
        Map.Entry pairs = (Map.Entry)it.next();
        if(pairs.getValue().toString().equalsIgnoreCase("NA"))
        {
          File file = new File(URL_LIST_DIR_PATH + "/post_data/" + "body_" + pairs.getKey());
          if(file.exists())
              file.delete();

          File dir = new File(URL_LIST_DIR_PATH + "/post_data");
          if(!dir.exists())
            dir.mkdirs();

          FileWriter fw = new FileWriter(URL_LIST_DIR_PATH + "/post_data/" + "body_" + pairs.getKey());
          fw.write(rowIdBodyMap.get(pairs.getKey()));
          fw.flush();
          fw.close();

          arrUrlList[Integer.parseInt(pairs.getKey().toString())][bodyIdx] = URL_LIST_DIR_PATH + "/post_data/" + "body_" + pairs.getKey();
		  ChangeOwnerOfFile(file.getAbsolutePath(),"netstorm");
        }
        else
        {
          File file = new File(pairs.getValue().toString());
          if(file.exists())
            file.delete();

          FileWriter fw = new FileWriter(pairs.getValue().toString());
          fw.write(rowIdBodyMap.get(pairs.getKey()));
          fw.flush();
          fw.close();
		  
		  ChangeOwnerOfFile(file.getAbsolutePath(),"netstorm");
        }
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "saveBody", "", "", "", e);
    }
  }

  /**
   * Function saveURLPattern accepts urlList and save in path URL_LIST_FILE_PATH with pipe separedted data.
   * Filter|Type|Method|URL|Count|Transaction Name|Host|URL Patter
   * @param urlList
   */
  public void saveURLPattern()
  {
    Log.debugLog(className, "saveURLPattern", "", "", "method called");
    try
    {
      saveBody();

      File url_dir_file = new File(URL_LIST_DIR_PATH);
      if(!url_dir_file.exists())
      {
        Log.debugLog(className, "saveURLPattern", "", "", "path: "+URL_LIST_DIR_PATH +" does not exist. Create Dirs");
        url_dir_file.mkdirs();
      }

      File url_file = new File(URL_LIST_FILE_PATH);
      if(url_file.exists())
      {
        Log.debugLog(className, "saveURLPattern", "", "", "file: "+URL_LIST_FILE_PATH +" already present so deleting this file.");
        url_file.delete();
      }

      PrintWriter pw = new PrintWriter(new FileWriter(url_file));
      String line = "";

      if(arrUrlList != null)
      {
        for(int i = 0; i <  arrUrlList.length; i++)
        {
          for(int j = 0; j < arrUrlList[i].length; j++)
          {
            if(j == ff1Idx)
              continue;

            if(line.trim().equals(""))
              line = arrUrlList[i][j];
            else
              line += "|" + arrUrlList[i][j];
          }
          pw.println(line);
          pw.flush();
          line = "";
        }
        Log.debugLog(className, "saveURLPattern", "", "", "data is written to file.");
      }
      else
        Log.debugLog(className, "saveURLPattern", "", "", "url_List is null. nothing is saved");

      pw.close();
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "saveURLPattern", "", "", "", e);
    }
  }
  
  //Method for getting list of Warnings after creating scripts.
  public ArrayList<String> getWarningList()
  {
     ArrayList<String> arrWarningList = new ArrayList<String>();
     try
     {
       String warningFilePath = Config.getWorkPath() + "/ReplayAccessLogs/"+ scenarioName +"/warning.dat";
       File warning_File = new File(warningFilePath);
       BufferedReader file_buf = new BufferedReader(new FileReader(warning_File));
       String warning_data = "";
       while((warning_data = file_buf.readLine()) != null)
       {
	 arrWarningList.add(warning_data);
       }
       file_buf.close();
       return arrWarningList;
     }
     catch(Exception e)
     {
       e.printStackTrace();
       Log.stackTraceLog(className, "getWarningList", "", "", "Exception in reading warning list = ", e);
       return arrWarningList;
     }
  }
  
  /**
   * Function genData accept followig arguments and exceute shell 'ntp_al_gen_data'
   * optaions are describe below
   * --mode <gui>
   * --field_number
   * --start_date
   * --end_date
   * --file <file1,file2>
   * --protocol <http/https>
   * --body <yes/no>
   * --inline_mode <yes/no>
   * --cookie <cookie-name>
   * --scenario <proj/sub - proj/sce>
   * --num_nvm
   * @return
   */
  public void genData(String httpHost, String httpsHost, StringBuffer errMsg)
  {
    Log.debugLog(className, "genData", "", "", "method called");
    try
    {
      String strCmdArgs = " --mode gui";
      final String strCmdName = "ntp_al_gen_data";


      //for http host
      if(!httpHost.equals(""))
	 strCmdArgs += " --http_host " + httpHost ;

      //for https host
      if(!httpsHost.equals(""))
	 strCmdArgs += " --https_host " + httpsHost ;

      strCmdArgs += " --scenario " + scenarioName;

      Log.debugLog(className, "genData", "", "", "Executing command: "+ strCmdName + " with arguments: "+strCmdArgs);

      Vector vecCmdOutput = new Vector();
      int bolRsltFlag = cmdExec.getIntResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null, "");
      this.successStatus = bolRsltFlag;
      
      Log.debugLog(className, "genData", "", "", "Error in executing command.");
      if(vecCmdOutput != null && vecCmdOutput.size() > 0)
      {
          for(int i = 0; i < vecCmdOutput.size(); i++)
          {
            String str = vecCmdOutput.get(i).toString();
            errMsg.append(str);
            if(i != vecCmdOutput.size() - 1)
               errMsg.append("\n");
          }
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "genData", "", "", "", e);
    }
  }

  /**
   * This function apply the filter to arrUrlList[][] and return data for given pages based on numOfRowsPerPage
   * Input parameter:
   * 1- main
   *   if it is true it means consider only that row which type is main.
   * 2-inline
   *   if it is true it means consider only those row which type is inline.
   * 3-filter
   *   if it is true it means consider only those row which filter is Yes
   *   if it is no it means consider only those row which filter is No.
   * 4-showOnlyUniqePtn
   *   if it is true it means put only first match of urlPttern in result and put the total match available in whole data.
   * 5- pageNum
   *   Return the data for given page.
   * 6-numOfRowsPerPage
   *   it decides the number of rows in a page.
   *
   *  it returns an arrayList having a 1D array of
   *  Filter|Type|Method|URL|Count|Txn-Name|Host|FF1|FF2|URL-Pattern|totalMatch|rowId
   */
  public void getPageData(boolean main, boolean main_without_tx, boolean inline, boolean filter, boolean non_Processed,  boolean showOnlyUniqePtn, int pageNum, int numOfRowsPerPage)
  {
    Log.debugLog(className, "getPageData", "", "", "Method called for main: "+main + ", main without TX: " + main_without_tx + ", inline: "+inline + ", filter: "+filter + ", showOnlyUniquePtn: "+showOnlyUniqePtn + ", pageNum: "+pageNum + ", numOfRowsPerPage: "+numOfRowsPerPage);
    
    int count = 0;
    try
    {
      if(pageNum < 1 || numOfRowsPerPage < 1)
      {
        Log.debugLog(className, "getPageData", "", "", "neither pageNum nor numOfRowsPerPage can be less than 1");
        return;
      }
      else
      {
        if(arrUrlList == null)
        {
          Log.debugLog(className, "getPageData", "", "", "no data available");
          return;
        }
        
        //clean priviously stored data.
        filterList.clear();
        
        ArrayList<String> addedUrlPtn = new ArrayList<String>();
        ArrayList<String> addedMethod = new ArrayList<String>();
        ArrayList<String> addedHost = new ArrayList<String>();
        int totalFiltered = 0;
	int totalInline = 0;
	int totalTransDefined = 0;
	int totalMainWithoutTX = 0;

        for(int i = 1; i < arrUrlList.length; i++)
        {
           count += Integer.parseInt(arrUrlList[i][countIdx]);
           if(arrUrlList[i][filterInx].equalsIgnoreCase("yes"))
           {
               totalFiltered += Integer.parseInt(arrUrlList[i][countIdx]);
             
	       if(!filter)
	         continue;
	   }
           else if(arrUrlList[i][typeIdx].equalsIgnoreCase("main"))
           {
             if(!arrUrlList[i][txnIdx].trim().equals("NA"))
               totalTransDefined += Integer.parseInt(arrUrlList[i][countIdx]);
             
             if(!main && !non_Processed)
               continue;
           }
           else if(arrUrlList[i][typeIdx].equalsIgnoreCase("main_without_tx"))
           {
             totalMainWithoutTX += Integer.parseInt(arrUrlList[i][countIdx]);
             
             if(!main_without_tx)
               continue;
           }
           else if(arrUrlList[i][typeIdx].equalsIgnoreCase("inline"))
           {
             totalInline += Integer.parseInt(arrUrlList[i][countIdx]);
             
             if(!inline)
	        continue;
           }
           
           if(!arrUrlList[i][urlPtnIdx].equalsIgnoreCase("NA"))
           {             
             if(non_Processed)
	        continue;
           }

	      if(showOnlyUniqePtn)
	      {
	        int idx = -1;
	        if(!arrUrlList[i][urlPtnIdx].equalsIgnoreCase("NA") && (idx = checkAvailability(addedUrlPtn, addedMethod, addedHost, arrUrlList[i][urlPtnIdx], arrUrlList[i][methodIdx], arrUrlList[i][hostIdx])) >= 0)
	        {
	          String rowData[] = filterList.get(idx);
	          int totalCount = Integer.parseInt(rowData[countIdx]) + Integer.parseInt(arrUrlList[i][countIdx]);
	          rowData[countIdx] = String.valueOf(totalCount);
	          int matchCount = Integer.parseInt(rowData[totalMatchCountIdx]) + 1;
	          rowData[totalMatchCountIdx] = String.valueOf(matchCount);
	        }
	        else
	        {
	          addedUrlPtn.add(arrUrlList[i][urlPtnIdx]);
	          addedMethod.add(arrUrlList[i][methodIdx]);
	          addedHost.add(arrUrlList[i][hostIdx]);
	          String rowData[] = new String[12];
	          for(int j = 0; j < arrUrlList[i].length; j++)
		    rowData[j] = arrUrlList[i][j];
	          
	          rowData[totalMatchCountIdx] = "1";
	          rowData[rowIdIdx] = "" + i;
	          filterList.add(rowData);
	        }
	      }
	      else
	      {
		String rowData[] = new String[12];
	        for(int j = 0; j < arrUrlList[i].length; j++)
	          rowData[j] = arrUrlList[i][j];
                rowData[totalMatchCountIdx] = "1";
	        rowData[rowIdIdx] = "" + i;
	        filterList.add(rowData);
	      }
        }

        int totalVisiblePage = 0;
        int totalActalPage = 0;
        if(filterList.size() > 0)
        {
          if((filterList.size() % numOfRowsPerPage) > 0)
            totalVisiblePage = filterList.size() / numOfRowsPerPage + 1;
          else
            totalVisiblePage = filterList.size() / numOfRowsPerPage;
        }

        if(arrUrlList.length > 1)
        {
          if(((arrUrlList.length-1) % numOfRowsPerPage) > 0)  //-1 to avoid header
            totalActalPage = (arrUrlList.length-1) / numOfRowsPerPage + 1;
          else
            totalActalPage = (arrUrlList.length-1) / numOfRowsPerPage;
        }

        Log.debugLog(className, "getPageData", "", "", "totalVisiblePage: "+totalVisiblePage + ", totalActalPage: "+totalActalPage);

        //Assign result to class object variables
        this.pageNum = pageNum;
        this.numOfRowsPerPage = numOfRowsPerPage;
        this.totalVisiblePage = totalVisiblePage;
        this.totalActualPage = totalActalPage;
        this.totalVisibleRowCount = filterList.size();
        this.arrFilteredUrlList = filterList;
        
        this.totalFiltered = totalFiltered;
        this.totalInline = totalInline;
	this.totalTransDefined = totalTransDefined;
        this.totalMainWithoutTX = totalMainWithoutTX;

        int processed = totalFiltered + totalInline + totalTransDefined + totalMainWithoutTX + totalRedirected + totalTimeBasedFiltered + totalBadRequest + totalNonSessionRequest + totalInlineFilteredRequest;
        tooltipString = "Main = "+ rptUtilsBean.convertNumberToCommaSeparate(totalTransDefined + "") + ", MainWithoutTx = "+rptUtilsBean.convertNumberToCommaSeparate(totalMainWithoutTX + "") + ", InLine = "+rptUtilsBean.convertNumberToCommaSeparate(totalInline + "") + ", Filtered = "+rptUtilsBean.convertNumberToCommaSeparate(totalFiltered + "") + ", Redirected = "+rptUtilsBean.convertNumberToCommaSeparate(totalRedirected + "") + ", TimeBasedFiltered = "+rptUtilsBean.convertNumberToCommaSeparate(totalTimeBasedFiltered + "") + ", BadRequest = "+rptUtilsBean.convertNumberToCommaSeparate(totalBadRequest + "") + ", NonSessionRequest = "+rptUtilsBean.convertNumberToCommaSeparate(totalNonSessionRequest + "") + ", InlineFilteredRequest = "+rptUtilsBean.convertNumberToCommaSeparate(totalInlineFilteredRequest + "");
        processedRequests = rptUtilsBean.convertNumberToCommaSeparate(processed + "") + " of " + rptUtilsBean.convertNumberToCommaSeparate(totalRequests+"");
        
        
       }
    }
    catch(Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "getPageData", "", "", "", e);
    }
  }
  
  //For returning arrayList to jsp.
  public ArrayList<String[]> getFilteredPageData(int pageNum)
  {
    
    //System.out.println("array lenght = "+ filterList.size() + " pageNum = "+pageNum);
    ArrayList<String[]> result = new ArrayList<String[]>();
    try
    {
      //Return data on basis of pageNum and numOfRowsPerPage
      int startIdx = pageNum * numOfRowsPerPage - numOfRowsPerPage + 1;
      int endIdx = pageNum * numOfRowsPerPage;
      for(int i = startIdx-1; i < endIdx && i < filterList.size(); i++)
      {
	 result.add(filterList.get(i));
      }
      return result;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return result;
    }
  }
  
  private int checkAvailability(ArrayList<String> addedUrlPtn, ArrayList<String> addedMethod, ArrayList<String> addedHost, String urlPtn, String method, String host)
  {
    for(int i = 0 ; i < addedUrlPtn.size(); i++)
    {
      if(addedUrlPtn.get(i).equals(urlPtn) && addedMethod.get(i).equals(method) && addedHost.get(i).equals(host))
          return i;
    }

    return -1;
  }

  /**
   * This function works on based of action.
   * action parameter has 5 values
   * 1- ApplyPattern
   *   it put the patterns and txnName for all matching urls.
   * 2-ChangeType
   *   it put type and pattern for all matching urls.
   * 3-Filter
   *   it match url and put filter as yes.
   * 4-FilterById
   *   Change filter to given filter for that rowId and copy url to url pattern if urlPten is NA otherwise
   *   it will search for all matching url and will put the filter and ptn..
   * 5- ChangeTypeById
   *   Change the type for that rowId.
   * @param action
   * @param pattern
   * @param type
   * @param txnName
   * @param rowId
   * @param errMsg
   */
  public void accecptPattern(String action, String pattern, String type, String txnName, int rowId, String filter, StringBuffer errMsg)
  {
    Log.debugLog(className, "acceptPattern", "", "", "Method called for action:"+action + ", ptn: "+pattern + ", type: "+type + ", txnName: "+ txnName + ", rowid: "+rowId + ", filter: "+filter);
    try
    {
      if(action.equalsIgnoreCase("ApplyPattern"))
      {
        boolean found = false;
        String tmpPattern = pattern.replace("*", ".*");
        Pattern expression = Pattern.compile(tmpPattern);
        for(int i = 1; i < arrUrlList.length; i++)
        {
          if(expression.matcher(arrUrlList[i][urlIdx]).matches())
          {
             arrUrlList[i][urlPtnIdx] = pattern;
             arrUrlList[i][txnIdx] = txnName;
             arrUrlList[i][typeIdx] = type;
             found = true;
          }
        }

        if(!found)
          errMsg.append("No Matching found");
      }
      else if(action.equalsIgnoreCase("ChangeType"))
      {
        boolean found = false;
        String tmpPattern = pattern.replace("*", ".*");
        Pattern expression = Pattern.compile(tmpPattern);
        for(int i = 1; i < arrUrlList.length; i++)
        {
          if(expression.matcher(arrUrlList[i][urlIdx]).matches())
          {
            arrUrlList[i][urlPtnIdx] = pattern;
            arrUrlList[i][typeIdx] = type;
            found = true;

          }
        }

        if(!found)
          errMsg.append("No Matching found");
      }
      else if(action.equalsIgnoreCase("Filter"))
      {
        boolean found = false;
        String tmpPattern = pattern.replace("*", ".*");
        Pattern expression = Pattern.compile(tmpPattern);
        for(int i = 1; i < arrUrlList.length; i++)
        {
          if(expression.matcher(arrUrlList[i][urlIdx]).matches())
          {
            arrUrlList[i][filterInx] = "YES";
            arrUrlList[i][urlPtnIdx] = pattern;
            found = true;
          }
        }

        if(!found)
          errMsg.append("No Matching found");
      }
      else if(action.equalsIgnoreCase("FilterById"))
      {
        if(arrUrlList[rowId][urlPtnIdx].equalsIgnoreCase("NA"))
        {
          arrUrlList[rowId][filterInx] = filter;
          arrUrlList[rowId][urlPtnIdx] = arrUrlList[rowId][urlIdx];
        }
        else
        {
          String tmpPattern = pattern.replace("*", ".*");
          Pattern expression = Pattern.compile(tmpPattern);
          if(filter.equalsIgnoreCase("YES"))
          {
            for(int i = 1; i < arrUrlList.length; i++)
            {
              if(expression.matcher(arrUrlList[i][urlIdx]).matches())
              {
                arrUrlList[i][filterInx] = filter;
                arrUrlList[i][urlPtnIdx] = pattern;
              }
            }
          }
          else
          {
             for(int i = 1; i < arrUrlList.length; i++)
             {
              if(expression.matcher(arrUrlList[i][urlIdx]).matches())
              {
                 arrUrlList[i][filterInx] = filter;
                 arrUrlList[i][urlPtnIdx] = "NA";
                 arrUrlList[i][txnIdx] = "NA";

               }
             }
          }
        }

      }
      else if(action.equalsIgnoreCase("ChangeTypeById"))
      {
        if(arrUrlList[rowId][urlPtnIdx].equalsIgnoreCase("NA"))
        {
          arrUrlList[rowId][typeIdx] = type;
          if(arrUrlList[rowId][typeIdx].equalsIgnoreCase("inline") || arrUrlList[rowId][typeIdx].equalsIgnoreCase("main_without_tx"))
            arrUrlList[rowId][urlPtnIdx] = arrUrlList[rowId][urlIdx];
        }
        else
        {
          String tmpPattern = pattern.replace("*", ".*");
          Pattern expression = Pattern.compile(tmpPattern);
          for(int i = 1; i < arrUrlList.length; i++)
          {
            if(expression.matcher(arrUrlList[i][urlIdx]).matches())
            {
              arrUrlList[i][typeIdx] = type;
            }
          }
        }
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "applyPattern", "", "", "", e);
      errMsg.append("Error in appying changes. Please check error log.");
    }
  }

  /**
   * This function will make a arraylist with all URL which matched with the given pattern
   * This arraylist will be use in pagination
   * It will return a ArrayList which will have two element
   * First element is number of URL matched
   * Second element is number of pages
   * Input parameter:
   * 1- pattern
   *   this is the string which may have regular expression as a pattern of multiple URLs.
   * 2-numOfRowsPerPage
   *   This will use to make pagination for this list.
   */
  public ArrayList findMatchingURLByPattern(String pattern, int numOfRowsPerPage, boolean main, boolean main_without_tx, boolean inline, boolean filter)
  {
    Log.debugLog(className, "findMatchingURLByPattern", "", "", "Method called for pattern: "+pattern + ", numOfRowsPerPage: " + numOfRowsPerPage);
    ArrayList<Integer> result = new ArrayList<Integer>();
    findMatchingURL = new ArrayList<ArrayList>();

    try
    {
      if(arrUrlList == null)
      {
        Log.debugLog(className, "getPageData", "", "", "no data available");
        result.add(0);
        result.add(0);
        return result;
      }

      String tmpPattern = pattern.replace("*", ".*");
      Pattern expression = Pattern.compile(tmpPattern);
      int numberOfMatchedURL = 0;
      int ctr = 0;
      ArrayList<String[]> specificRow = new ArrayList<String[]>();
      
      for(int i = 1; i < arrUrlList.length; i++)
      {
        if(arrUrlList[i][filterInx].equalsIgnoreCase("yes"))
        {
	   if(!filter)
	    continue;
	}
        else if(arrUrlList[i][typeIdx].equalsIgnoreCase("main"))
        {
          if(!main)
            continue;
        }
        else if(arrUrlList[i][typeIdx].equalsIgnoreCase("main_without_tx"))
        {
          if(!main_without_tx)
            continue;
        }
        else if(arrUrlList[i][typeIdx].equalsIgnoreCase("inline"))
        {
          if(!inline)
	    continue;
        }

        if(expression.matcher(arrUrlList[i][urlIdx]).matches())
        {
          numberOfMatchedURL++;
	  String rowData[] = new String[12];
	  for(int j = 0; j < arrUrlList[i].length; j++)
	    rowData[j] = arrUrlList[i][j];
	  rowData[totalMatchCountIdx] = "1";
	  rowData[rowIdIdx] = "" + i;
	  
          specificRow.add(rowData.clone());
          ctr++;
        }

        if(ctr >= numOfRowsPerPage || i == arrUrlList.length - 1)
        {
	   findMatchingURL.add((ArrayList)specificRow.clone());
	   specificRow = new ArrayList<String[]>();
           ctr = 0;
        }
      }
      
      if(findMatchingURL.size() == 0 || specificRow.size() > 0)
      {
         findMatchingURL.add((ArrayList)specificRow.clone());    
      }

      result.add(numberOfMatchedURL);
      result.add(findMatchingURL.size());
      this.totalVisiblePage = findMatchingURL.size();
      this.totalVisibleRowCount = numberOfMatchedURL;
      return result;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "findMatchingURLByPattern", "", "", "", e);
    }

    result.add(0);
    return result;
  }

  public ArrayList getMatchingURLPageData(int pageNumber)
  {
    if(findMatchingURL.size() >= pageNumber)
      return findMatchingURL.get(pageNumber-1);
    else 
     return new ArrayList();
  }


  /*
  * Method to make all URLs 'Mark_Without_TX' which are not processed yet
  * Condition for not processed URL
  * URL which are not 'Inline' not Filtered and not Main with any transaction
  *
  */
  public void markURLWithoutPattern()
  {
    for(int i = 1; i < arrUrlList.length; i++)
    {
      if(arrUrlList[i][urlPtnIdx].equalsIgnoreCase("NA"))
      {
        arrUrlList[i][typeIdx] = "Main_Without_TX";
        arrUrlList[i][urlPtnIdx] = arrUrlList[i][urlIdx];
      }
    }
  }

  /**
   * This function accepts a pattern and find the first matching url and the total count of all matching urls.
   * and returns give two values:
   * 1- Page of matching first url
   * 2- Number of machings.
   * and return an arrayList of matching rowIds to hightLight Rows in GUI
   * @param pattern
   * @param numOfRowsPerPage
   * @param errMsg
   */
  public ArrayList<Integer> findMatchingPattern(String pattern, int numOfRowsPerPage, StringBuffer errMsg)
  {
    Log.debugLog(className, "findMatchingPattern", "", "", "method called for pattern: "+pattern + ", numOfRowsPerPage: "+numOfRowsPerPage);
    try
    {
      int firstMatchIdx = -1;
      int matchCount = 0;
      ArrayList<Integer> rowIdList = new ArrayList<Integer>();
      String tmpPattern = pattern.replace("*", ".*");
      Pattern expression = Pattern.compile(tmpPattern);
      for(int i = 1; i < arrUrlList.length; i++)
      {
        if(expression.matcher(arrUrlList[i][urlIdx]).matches())
        {
          if(firstMatchIdx == -1)
            firstMatchIdx = i;
          matchCount++;
          rowIdList.add(i);
        }
      }

      if(firstMatchIdx == -1)
      {
        errMsg.append("No matching url found.");
        return null;
      }

      //System.out.println("firstMatchIdx = "+firstMatchIdx + ", arrFilteredUrlList size = "+arrFilteredUrlList.size());
      boolean foundInFilter = false;
      for(int i = 0; i < arrFilteredUrlList.size(); i++)
      {
        if(firstMatchIdx == Integer.parseInt(arrFilteredUrlList.get(i)[rowIdIdx]))
        {
          firstMatchIdx = i+1;
          foundInFilter = true;
          break;
        }
      }

      int pageOfMatchingFirstUrl = 1;
      if(foundInFilter)
      {
        if(firstMatchIdx % numOfRowsPerPage > 0)
          pageOfMatchingFirstUrl = firstMatchIdx / numOfRowsPerPage + 1;
        else
          pageOfMatchingFirstUrl = firstMatchIdx / numOfRowsPerPage;
      }

      //Assign result to class object variables
      this.pageOfFirstMatching = pageOfMatchingFirstUrl;
      this.numOfMatching = matchCount;

      return rowIdList;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "findMatchingPattern", "", "", "", e);
      errMsg.append("Error in applying changes. Please check error log.");
    }

    return null;
  }

  /**
   * This method return the data of that rows only which have the unique patterns.
   * if two or more rows have the same pattern then only first one will be pick in resultList.
   * resultList has a 1D array of
   * Filter|Type|Method|URL|Count|Txn-Name|Host|FF1|FF2|URL-Pattern
   * @return
   */
  public ArrayList<String[]> getUniquePatternList()
  {
    Log.debugLog(className, "getUniquePatternList", "", "", "Method called");
    try
    {
      if(arrUrlList == null)
      {
        Log.debugLog(className, "getUniquePatternList", "", "", "no data available.");
        return null;
      }

      ArrayList<String> uniquePatternList = new ArrayList<String>();
      ArrayList<String[]> resultList = new ArrayList<String[]>();
      ArrayList<String> hostList = new ArrayList<String>();
      ArrayList<String> methodList = new ArrayList<String>();

      for(int i = 1; i < arrUrlList.length; i++)
      {

        /*if(!arrUrlList[i][urlPtnIdx].equalsIgnoreCase("NA") && (!uniquePatternList.contains(arrUrlList[i][urlPtnIdx])))
        {
          uniquePatternList.add(arrUrlList[i][urlPtnIdx]);
          hostList.add(arrUrlList[i][hostIdx]);
          methodList.add(arrUrlList[i][methodIdx]);
          resultList.add(arrUrlList[i]);
        }
        else
        {
           if(arrUrlList[i][urlPtnIdx].equalsIgnoreCase("NA"))
             continue;

           if(!(hostList.contains(arrUrlList[i][hostIdx])))
           {
             uniquePatternList.add(arrUrlList[i][urlPtnIdx]);
             hostList.add(arrUrlList[i][hostIdx]);
             methodList.add(arrUrlList[i][methodIdx]);
             resultList.add(arrUrlList[i]);
           }
           else
           {
              if(!(methodList.contains(arrUrlList[i][methodIdx])))
              {
                uniquePatternList.add(arrUrlList[i][urlPtnIdx]);
                hostList.add(arrUrlList[i][hostIdx]);
                methodList.add(arrUrlList[i][methodIdx]);
                resultList.add(arrUrlList[i]);
              }
          }*/

	  int idx = -1;
	  if(arrUrlList[i][urlPtnIdx].equalsIgnoreCase("NA"))
	    continue;

	  if(!arrUrlList[i][urlPtnIdx].equalsIgnoreCase("NA") && (idx = checkAvailability(uniquePatternList, methodList, hostList, arrUrlList[i][urlPtnIdx], arrUrlList[i][methodIdx], arrUrlList[i][hostIdx])) >= 0)
	  {
	    continue;
	  }
	  else
	  {
	     uniquePatternList.add(arrUrlList[i][urlPtnIdx]);
             hostList.add(arrUrlList[i][hostIdx]);
	     methodList.add(arrUrlList[i][methodIdx]);
	     resultList.add(arrUrlList[i]);
	  }
      }

      return resultList;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getUniquePatternList", "", "", "", e);
    }

    return null;
  }


  /**
   * This function update the body path for which user has set the body by setBody(int rowId, String body) method.
   * and now want to save this then all updated files will be moved to given profiles.
   * @param profileName
   */
  public void updateBodyPathForProfile(String profileName)
  {
    Log.debugLog(className, "saveBody", "", "", "method called for profileName: "+profileName);
    try
    {
      Iterator it = rowIdPathMap.entrySet().iterator();
      while (it.hasNext())
      {
        Map.Entry pairs = (Map.Entry)it.next();
        //System.out.println(pairs.getKey() + " = " + pairs.getValue());
        if(pairs.getValue().toString().equalsIgnoreCase("NA"))
        {
          arrUrlList[Integer.parseInt(pairs.getKey().toString())][bodyIdx] = profileDirPath + "/" + profileName + "/body_" + pairs.getKey();
        }
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "saveBody", "", "", "", e);
    }
  }

  public void updateProfileData(String profileName, StringBuilder errMsg, String mode)
  {
    Log.debugLog(className, "updateProfileData", "", "", "method called for profileName: "+profileName + " and mode: "+mode);
    try
    {
      updateBodyPathForProfile(profileName);
      ArrayList<String[]> filterList = new ArrayList<String[]>();
      ArrayList<String[]> arrDatalist = new ArrayList<String[]>();
      ArrayList<String[]> uniquePtnList = getUniquePatternList();

      for(int i = 0; i < uniquePtnList.size(); i++)
      {
        String[] rowData = uniquePtnList.get(i);
        if(rowData[filterInx].equalsIgnoreCase("YES"))
        {
           String[] str = new String[5];
           str[0] = "Active";
           str[1] = rowData[methodIdx];
           str[2] = rowData[urlIdx];
           str[3] = rowData[hostIdx];
           str[4] = rowData[urlPtnIdx];

           filterList.add(str);
        }
        else
        {
          String[] str = new String[9];
          str[0] = "Active";
          str[1] = rowData[typeIdx];
          str[2] = rowData[methodIdx];
          str[3] = rowData[urlIdx];
          str[4] = rowData[txnIdx];
          str[5] = rowData[hostIdx];
          str[6] = "NA";
          str[7] = rowData[bodyIdx];
          str[8] = rowData[urlPtnIdx];

          arrDatalist.add(str);
        }
      }

      ReplayUrlFilterPattern obj = new ReplayUrlFilterPattern(profileName, userName);
      for(int i = 0; i < filterList.size(); i++)
      {
	String []str1 = filterList.get(i);
	String strs = "";
	for(int k = 0; k < str1.length; k++)
          strs = strs + str1[k] + "|";
	
      }
      obj.saveReplayUrlFilterPattern(filterList.toArray(new String[0][0]));
      if(mode.equalsIgnoreCase("CreateNew"))
      {
        obj.saveAsProfileForURLPtn(profileName, arrDatalist.toArray(new String[0][0]), errMsg);
      }
      else
      {
        obj.saveReplayUrlPattern(arrDatalist.toArray(new String[0][0]));
      }

      saveBodyFilesToProfiles(profileName);
      if(mode.equalsIgnoreCase("CreateNew"))
        errMsg.append("The Profile "+ profileName + " created successfully");
      else
        errMsg.append("The Profile " + profileName + " updated successfully");
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "updateProfileData", "", "", "", e);
      errMsg.append("Error in saving profile. Please check error log");
    }
  }

  /**
   *    * This function set the default host for rows
   * if flagForAll is true then it copy the hostName to all rows data
   * otherwise it copy host for those only rows which hostName is "NA".
   * @param hostName
   * @param flagForAll
   */
  public void setDefaultHost(String hostName, boolean flagForAll)
  {
    Log.debugLog(className, "setDefaultHost", "", "", "Method called for hostName: "+ hostName + ", flagForAll: "+flagForAll);
    try
    {
      if(flagForAll)
      {
        for(int i = 1; i < arrUrlList.length; i++)
        {
          arrUrlList[i][hostIdx] = hostName;
        }
      }
      else
      {
        for(int i = 1; i < arrUrlList.length; i++)
        {
          if(arrUrlList[i][hostIdx].equalsIgnoreCase("NA"))
            arrUrlList[i][hostIdx] = hostName;
        }
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "setDefaultHost", "", "", "", e);
    }
  }

  /**
   * This function set Filter as YES and put url to urlPattern for only those rows which urlPattern is "NA".
   */
  public void filterRemainingUrls()
  {
    Log.debugLog(className, "filterRemainingUrls", "", "", "method start");
    try
    {
      if(arrUrlList == null)
      {
        Log.debugLog(className, "filterRemainingUrls", "", "", "no data availble.");
        return;
      }

      for(int i = 1; i < arrUrlList.length; i++)
      {
        if(arrUrlList[i][urlPtnIdx].equalsIgnoreCase("NA"))
        {
          arrUrlList[i][filterInx] = "YES";
          arrUrlList[i][urlPtnIdx] = arrUrlList[i][urlIdx];
        }
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "filterRemainingUrls", "", "", "", e);
    }
  }

  /**
   * This function save the updated body to user profile
   * @param profileName
   */
  public void saveBodyFilesToProfiles(String profileName)
  {
     Log.debugLog(className, "saveBodyFilesToProfiles", "", "", "Method strat");
     try
     {
       Iterator it = rowIdPathMap.entrySet().iterator();
       while (it.hasNext())
       {
         Map.Entry pairs = (Map.Entry)it.next();
         //System.out.println(pairs.getKey() + " = " + pairs.getValue());
         if(pairs.getValue().toString().equalsIgnoreCase("NA"))
         {
           File file = new File(profileDirPath + "/" + profileName +  "/body_" + pairs.getKey());
           if(file.exists())
             file.delete();

           File dir = new File(profileDirPath);
           if(!dir.exists())
             dir.mkdirs();

           FileWriter fw = new FileWriter(profileDirPath + "/" + profileName + "/body_" + pairs.getKey());
           fw.write(rowIdBodyMap.get(pairs.getKey()));
           fw.flush();
           fw.close();
		   ChangeOwnerOfFile(file.getAbsolutePath(),"netstorm");
         }
         else
         {
           File file = new File(pairs.getValue().toString());
           if(file.exists())
             file.delete();

           FileWriter fw = new FileWriter(pairs.getValue().toString());
           fw.write(rowIdBodyMap.get(pairs.getKey()));
           fw.flush();
           fw.close();
		   ChangeOwnerOfFile(file.getAbsolutePath(),"netstorm");
         }
       }
     }
     catch(Exception e)
     {
       Log.stackTraceLog(className, "saveBodyFilesToProfiles", "", "", "", e);
     }
  }

  public ArrayList<String> getProfileList()
  {
    Log.debugLog(className, "getProfileList", "", "", "method called");
    ArrayList<String> profiles = new ArrayList<String>();
    try
    {
      File profileDirFile = new File(profileDirPath);
      if(!profileDirFile.exists())
        profileDirFile.mkdirs();

      File defaultProfile = new File(profileDirFile + "/default");
      if(!defaultProfile.exists())
      {
        ReplaySetting replaySetting = new ReplaySetting("default", userName);
        replaySetting.saveReplaySettings();
      }
      String files[] = profileDirFile.list();
      for(int i = 0; i < files.length; i++)
      {
        File file = new File(profileDirFile + "/" + files[i]);
        if(file.isDirectory())
          profiles.add(files[i]);
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getProfileList", "", "", "", e);
    }
    return profiles;
  }

  public StringBuilder deleteProfile(String profileName)
  {
     Log.debugLog(className, "deleteProfile", "", "", "method called for profile: "+profileName);
     StringBuilder errMsg = new StringBuilder();
     try
     {
      File file = new File(profileDirPath + "/" + profileName);
      Log.debugLog(className, "deleteProfile", "", "", "going to delete file: "+profileDirPath + "/" + profileName);
      if(file.exists())
      {
        deleteDirecoty(file);
        file.delete();
        Log.debugLog(className, "deleteProfile", "", "", "profile deleted successfully.");
      }
      else
        Log.debugLog(className, "deleteProfile", "", "", "Profile does not exist.");
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "deleteProfile", "", "", "", e);
      errMsg.append("Error in deleting profile. Please check error log.");
    }
    return errMsg;
  }

  private void deleteDirecoty(File file)
  {
    File dirFiles[] = file.listFiles();
    for(int i = 0 ; i < dirFiles.length; i++)
    {
      if(dirFiles[i].isFile())
        dirFiles[i].delete();
      else
      {
        deleteDirecoty(dirFiles[i]);
        dirFiles[i].delete();
      }
    }
  }
  
  private boolean ChangeOwnerOfFile(String filePath, String owner)
  {
    try
    {
      Log.debugLog(className, "ChangeOwnerOfFile", "", "", "method called");

      Runtime r = Runtime.getRuntime();
      String strCmd = "chown" + " " + "netstorm" + "." + "netstorm" + " " + filePath;
      Process changePermissions = r.exec(strCmd);
      int exitValue = changePermissions.waitFor();

      if (exitValue == 0)
        return true;
      else
        return false;

    }
    catch (Exception e)
    {
      // e.printStackTrace();
      Log.stackTraceLog(className, "ChangeOwnerOfFile", "", "", "Exception = ", e);
      return false;
    }
  }
  
  
  //Getting the List for QueryList.
  public ArrayList<String> getQueryList(int index)
  { 
    ArrayList<String> arrQueriesList = new ArrayList<String>();
    try
    {
      
      String filePath = URL_LIST_DIR_PATH + "/query_url.list";
      File query_file = new File(filePath);
      
      if(!query_file.exists())
	return arrQueriesList;
      
      //object of random access file.
      RandomAccessFile access = new RandomAccessFile(query_file, "rw");
      //Getting channel from random access.
      //FileChannel f_channel = access.getChannel();
      
      //Reading file from position.
      Long file_pos = Long.parseLong(arrUrlList[index][7].trim());
      int row_count = Integer.parseInt(arrUrlList[index][countIdx].trim());
      
      if(file_pos != -1)
      {          
        access.seek(file_pos);
        int count = 0;
          
        while(count < row_count) 
        {
          String lineData = access.readLine();
          
          if(!lineData.trim().equals(""))
            arrQueriesList.add(lineData);
          
          count++;
        }          
      }
      //closing the streams.
      access.close();  
      return arrQueriesList;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return arrQueriesList;
    }
  }

  public boolean createHostList(ArrayList<String> hostList)
  {
    boolean bool = true;
    try
    {
      ArrayList<String> uniqueList = new ArrayList<String>();
      File filePath = new File(URL_LIST_FILE_PATH);

      if(hostList.size() <= 0)
      {
	//System.out.println("inside .... ");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
        String strLine = null;
        int i = 0;
        while((strLine = br.readLine()) != null)
        {
          if(!strLine.trim().equals("") && i != 0)
          {
            String tmp[] = (strLine+" ").split("\\|");
            if(tmp.length > 6)
            {
             uniqueList.add(tmp[6]);
            }
          }
          i++;
        }
        br.close();
      }
      else
      {
         for(int i = 0; i< hostList.size(); i++)
           uniqueList.add(hostList.get(i));
      }

      ArrayList<String> uniqueHostList = new ArrayList<String>();

      if(uniqueList == null)
        return false;

      for(int i = 1 ; i < uniqueList.size() ; i++ )
      {
        String hostName = uniqueList.get(i);

        if(uniqueHostList.size() > 0)
        {
          if(uniqueHostList.indexOf(hostName) < 0)
          {
            uniqueHostList.add(hostName);
          }
        }
          else
        uniqueHostList.add(hostName);
      }

      if(URL_LIST_DIR_PATH == null)
       return false;

      File uniqueHostListFile = new File(URL_LIST_DIR_PATH + "/host.list");

      if(uniqueHostListFile.exists())
      {
        uniqueHostListFile.delete();
      }

      String strHostList = "";
      if(hostList.size() > 0)
      {
	uniqueHostList = new ArrayList<String>();
        for(int i = 0; i< hostList.size(); i++)
         uniqueHostList.add(hostList.get(i));
      }

      for(int i = 0 ; i < uniqueHostList.size() ; i++)
      {
      if(i == 0 )
        strHostList = uniqueHostList.get(i);
      else
        strHostList = strHostList + "\n" + uniqueHostList.get(i);
      }

      FileWriter fw = new FileWriter(uniqueHostListFile);

      fw.write(strHostList);
      fw.flush();
      fw.close();
      
      //change owner to netstorm.
      ChangeOwnerOfFile(URL_LIST_DIR_PATH + "/host.list", "");

      return true;

 }
 catch(Exception e)
 {
   Log.stackTraceLog(className, "createHostList", "", "", "", e);
   return false;
 }
  }

  public ArrayList<String> getUniqueTransactionNameList()
  {
    Log.debugLog(className, "getUniqueTransactionNameList", "", "", "method start");
    try
    {
      ArrayList<String> trxNameList = new ArrayList<String>();
      for(int i = 0; i < arrUrlList.length; i++)
      {
        if(!trxNameList.contains(arrUrlList[i][txnIdx]))
          trxNameList.add(arrUrlList[i][txnIdx]);
      }

      return trxNameList;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getUniqueTransactionNameList", "", "", "", e);
    }

    return null;
  }

  public ArrayList<String> getSummaryReportOfReplay()
  {
    ArrayList<String> arrSummaryInfoList = new ArrayList<String>();
    try
    {
      Log.debugLog(className, "getSummaryReportOfReplay", "", "", "method called");
      File summaryFile = new File(SUMMARY_REPORT_PATH);
      if(summaryFile.exists())
      {
	 BufferedReader buffData = new BufferedReader(new FileReader(summaryFile));
	 String summary_data = "";
	 while((summary_data = buffData.readLine()) != null)
	 {
           
	   arrSummaryInfoList.add(summary_data);
	 }
      }
      return arrSummaryInfoList;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getSummaryReportOfReplay", "", "", "Exception in getting data = ", e);
      return arrSummaryInfoList;
    }
  }
  
  //method for creating invalid file list.
  public void createPostProcessingFileList(StringBuffer errMsg, String params)
  {
    Log.debugLog(className, "createPostProcessingFileList", "", "", "method called");
    try
    {
      String strCmdArgs = " -s " + scenarioName + " " + params;
      final String strCmdName = "ntp_gen_error_file";

      Vector vecCmdOutput = new Vector();
      int bolRsltFlag = cmdExec.getIntResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null, "");
      this.successStatus = bolRsltFlag;
      
      if(vecCmdOutput != null && vecCmdOutput.size() > 0)
      {
          for(int i = 0; i < vecCmdOutput.size(); i++)
          {
            String str = vecCmdOutput.get(i).toString();
            errMsg.append(str);
            if(i != vecCmdOutput.size() - 1)
               errMsg.append("\n");
          }
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();   
      Log.stackTraceLog(className, "createPostProcessingFileList", "", "", "", e);
    }
  }
  
  //function for swapping the cols data.
  private boolean SwapArrayCols(String arrData[])
  {
    if(arrData == null || arrData.length < 4)
     return false;
    
    String temp = arrData[2];
    arrData[2] = arrData[4];
    arrData[4] = temp;
    
    return true;
  }
  
  //Read the file contain Invalid Request.
  public ArrayList<String[]> readReplayListFile(String fileToRead)
  {
    Log.debugLog(className, "readReplayListFile", "", "", "method called");
    
    ArrayList<String[]> arrInvalidFileList = new ArrayList<String[]>();	 
    try
    {
      File invalidFileList = new File(Config.getWorkPath() + "/ReplayAccessLogs/"+ scenarioName + "/" + fileToRead);
      
      if(!invalidFileList.exists())
	return arrInvalidFileList;
      
      BufferedReader  b_reader = new BufferedReader(new FileReader(invalidFileList));
      String str = "";
      while((str = b_reader.readLine()) != null)
      {
	      
	if(str.trim().equals(""))
	  continue;
	
	String arrData[] = rptUtilsBean.split(str, "|");
	
	SwapArrayCols(arrData);
	
	arrInvalidFileList.add(arrData.clone());
      }
      b_reader.close();
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "readReplayListFile", "", "", "", e);
      e.printStackTrace();    
    }
    return arrInvalidFileList;
  }
  
    
  public static void main(String arg[])
  {
    NTPBean ntpBean = new NTPBean("x/y/abc", "netstorm");
    String file[] = {"file1", "file2"};
    String body[] = {"b1", "b2"};
    String[] protocol = {"p1", "p2"};
    
    System.out.println(" result = "+ntpBean.URLEncoding("/\"><script>alert(document.domain)</script>.jsp"));
    System.out.println(" result = "+ntpBean.encodeHTMLCharecters("/\"><script>alert(document.domain)</script>.jsp"));
    //ntpBean.genData();
    /*ArrayList urlList = new ArrayList();
    urlList.add("bzjx|123|sdkd");
    urlList.add("bzjx|123|sdkd");
    ntpBean.saveURLPattern(urlList);*/

    try
    {
      BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("c:/home/netstorm/work/replay_access")));
      String line = null;
      NTPBean bean = new NTPBean("123", "net");
      bean.arrUrlList = new String[100][10];
      int i = 0;
      while((line = br.readLine())!= null && i < 100)
      {
        bean.arrUrlList[i] = line.split("\\|");
        i++;
      }

      StringBuffer errMsg = new StringBuffer();
      //bean.findMatchingPattern("//store*", 10, errMsg);
      //bean.getPageData(true, true, false, false, 1, 10);
      //bean.accecptPattern("ChangeTypeById", "//store*", "INLINE", "trx1", 9, errMsg);
      //bean.accecptPattern("FilterById", "//store*", "INLINE", "trx1", 9, errMsg);
      //bean.getPageData(true, false, false, false, 1, 20);
      //System.out.println("ErrMsg: "+errMsg);
      //bean.getBody(10);
      //bean.setBody(10, "Hiiiiii");
      //bean.saveBody();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
}

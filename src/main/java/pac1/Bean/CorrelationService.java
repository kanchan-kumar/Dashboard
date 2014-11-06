//Name    : CorrelationService.java
//Author  : Arun Goel
//Purpose : Utility Bean for NetOscean GUI
//Modification History:
//03/30/10 Arun Goel: Initial Version

package pac1.Bean;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class CorrelationService
{
  private static String className = "CorrelationService";
  static String hpdPath = null;
  static String hpdWork = null;
  static String correlationPath = "";
  public final static int SEARCH_VAR = 1; // for nsl_search_var
  public final static int DECLARE_VAR = 2; // for nsl_decl_var
  public final static int HTTP_HEADER = 3; // for SET_CR_URL_HEADER
  public final static int SVC_TIME = 4; // SVC_TIME
  public final static int QUERY_VAR = 5; // QUERY_VAR
  public final static int FILE_PARAMETERS = 6; // for File Parameters
  public final static int INDEX_FILE_PARAMETER = 7;
  public final static int DATE_TIME_PARAMETER = 8;
  public final static int RANDOM_NUMBER = 9;
  public final static int RANDOM_STRING = 10;
  public final static int UNIQUE_NUMBER = 11;
  public final static int XML_PARAMETER = 12;
  public final static int UNIQUE_NUMBER_VUSER = 13;
  public final static int COOKIE_VAR = 14; //for nsl_cookie_var
  public final static int DECLARE_ARRAY_VAR = 15;
  public final static int GLOBAL_INDEXED_DATA_SOURCE = 16;
  public final static int REQUEST_VAR = 17;

  public final String REQUEST_EXTENTION = ".req";
  public final String RESPONSE_EXTENTION = ".rep";
  public final String CAPTURED_EXTENTION = ".captured";
  public final String JDBC_URL = "/cavisson/jdbc/service_";
  public final String SPRING_URL = "/cavisson/springremoting/service_";
  public final String JAVA_CLASS_URL = "/cavisson/JavaClass/service_";
  // this is use to formatting of XML
  private final String indent = "  ";

  //this is declared to pass the jsp so JSP strict the variable names should not start with any keyword
  //because we are parsing the line using keyword.
  public final static String[] keywordsUsedInFile = new String[]{"FILE", "REFRESH", "MODE", "VAR_VALUE"};
  public final static String[] keywordsUsedInIndexFile = new String[]{"FILE", "INDEXVAR", "VAR_VALUE"};
  public String userName = "";
  public String[] fileNames = null;

  public CorrelationService()
  {}

  public CorrelationService(String userName)
  {
    this.userName = userName;
  }

  public String[] getFileRestrictKeywords()
  {
    return keywordsUsedInFile;
  }

  public String[] getIndexFileRestrictKeywords()
  {
    return keywordsUsedInIndexFile;
  }

  // This will return the hpd work name
  public String getHPDWork()
  {
    Log.debugLog(className, "getHPDWork", "", "", "Method started hpdWork = " + hpdWork);
    if(hpdWork != null)
      return hpdWork;

    hpdWork = System.getProperty("HPD_CMD");
    if(hpdWork == null)
    {
      hpdWork = "hpd";
    }

    Log.debugLog(className, "getHPDWork", "", "", "returning hpdWork = " + hpdWork);
    return hpdWork;
  }

  //Method to check if any port is used in controller
  //It was needed when user assign any recorder port which is used by controller (hpd)
  //So we need to check by reading hpd.conf and return a message as string if the port is used
  //If port is not in use then this method will return blank string
  public String checkRecorderPortIsUsedByController(String[] recorderPort)
  {
    Log.debugLog(className, "checkRecorderPortIsUsedByController", "", "", "Method started recorderPort = " + recorderPort.length);
    String strOutput = "";
    try
    {
      //check if hpd is already stop then ports can be used.
      if(!showHPD())
        return strOutput;

      Vector hpdConfData = readFile(getHPDPath() + ".hpd_sp/hpd.conf", true);
      if(hpdConfData == null)
      {
        Log.debugLog(className, "getHPDPort", "", "", "hpd.conf File not found, It may be corrupted.");
        return "";
      }

      for(int i = 0; i < hpdConfData.size(); i++)
      {
        String strLine = hpdConfData.elementAt(i).toString();
        if(strLine.indexOf("HPD_PORT") > -1 || strLine.indexOf("HPD_SPORT") > -1 || strLine.indexOf("HPD_FTP_PORT") > -1 || strLine.indexOf("HPD_SMTP_PORT") > -1 || strLine.indexOf("HPD_POP3_PORT") > -1 || strLine.indexOf("HPD_DNS_PORT") > -1)
        {
          String[] arrLine = rptUtilsBean.strToArrayData(hpdConfData.elementAt(i).toString(), " ");
          if(arrLine == null || arrLine.length <= 1)
            continue;

          String[] strPorts = rptUtilsBean.strToArrayData(arrLine[1], ",");

          for(int j = 0; j < strPorts.length; j++)
          {
            for(int x = 0; x < recorderPort.length; x++)
            {
              if(recorderPort[x].trim().equals(strPorts[j].trim()))
              {
                if(strOutput.equals(""))
                {
                  strOutput = "Recorder port " + recorderPort[x].trim() + " ";
                }
                else
                {
                  //if port already added in string it will not add again in the string
                  //maintain unique ness
                  if((strOutput.contains(recorderPort[x].trim() + " ")) || (strOutput.contains(recorderPort[x].trim() + ",")))
                  {
                    continue;
                  }
                  else
                    strOutput = strOutput.trim() + ", " + recorderPort[x].trim() + " ";
                }
              }
            }
          }
        }
      }
      if(!strOutput.equals(""))
        strOutput = strOutput.trim() + " is already in use by controller. Do you want to stop controller and start recording?";

      return strOutput;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "checkRecorderPortIsUsedByController", "", "", "Exception - ", ex);
      return "Exception - " + ex;
    }
  }

  // This will return the hdf.conf file path
  public String getHPDPath()
  {
    Log.debugLog(className, "getHPDPath", "", "", "Method started hpdPath = " + hpdPath);
    if(hpdPath != null)
      return hpdPath;

    String osname = System.getProperty("os.name").trim().toLowerCase();
    hpdPath = System.getProperty("HPD_ROOT");
    if(hpdPath == null)
    {
      if(osname.startsWith("win"))
        hpdPath = "C:/var/www/hpd/";
      else
        hpdPath = "/var/www/hpd/";
    }
    else
    {
      if(!hpdPath.endsWith("/"))
        hpdPath = hpdPath + "/";
    }
    Log.debugLog(className, "getHPDPath", "", "", "returning hpdPath = " + hpdPath);
    return hpdPath;
  }

  /*
   * This will return the correlation dir path Will search CORRELATION_DIR in hpd.conf. If found then value of this will be relative correlation path next to the hpd path + /Correlation/+ path defined in value
   */
  public String getCorrelationPath()
  {
    Log.debugLog(className, "getCorrelationPath", "", "", "Method started");
    try
    {
      Log.debugLog(className, "getCorrelationPath", "", "", "correlationPath = " + correlationPath);
      //if user change CORRELATION_DIR but gui is not update this static variable to we are commentting this condition
      //if(!correlationPath.trim().equals(""))
      //return correlationPath;

      correlationPath = getHPDPath() + "correlation/";

      Vector hpdConfData = readFile(getHPDPath() + "correlation/correlation.conf", true);
      if(hpdConfData == null)
      {
        Log.debugLog(className, "getCorrelationPath", "", "", "correlation.conf File not found, It may be corrupted.");
        return correlationPath;
      }

      boolean correlationDirFound = false;
      for(int i = 0; i < hpdConfData.size(); i++)
      {
        if(hpdConfData.elementAt(i).toString().indexOf("CORRELATION_DIR") > -1)
        {
          String[] arrLine = rptUtilsBean.strToArrayData(hpdConfData.elementAt(i).toString(), " ");
          if(arrLine != null && arrLine.length > 1)
          {
            Log.debugLog(className, "getCorrelationPath", "", "", "CORRELATION_DIR found in correlation.conf file CORRELATION_DIR = " + arrLine[1]);
            correlationPath = getHPDPath() + "correlation/" + arrLine[1] + "/";
            correlationDirFound = true;
          }
        }
      }

      if(!correlationDirFound)
        correlationPath = getHPDPath() + "correlation/default/";

      Log.debugLog(className, "getCorrelationPath", "", "", "returning  correlationPath = " + correlationPath);
      return correlationPath;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getCorrelationPath", "", "", "Exception - ", ex);
      return getHPDPath() + "correlation/";
    }
  }

  //Method to get all HOST Names
  public String[] getHOSTNames()
  {
    Log.debugLog(className, "getHOSTNames", "", "", "Method started");
    try
    {
      ArrayList alHostNames = new ArrayList();
      File corrDir = new File(getCorrelationPath());
      File[] files = corrDir.listFiles();
      if(files == null || files.length <= 0)
        return new String[0];

      for(int i = 0; i < files.length; i++)
      {
        if(files[i].isDirectory())
          alHostNames.add(files[i].getName());
      }

      String[] arrHostNames = new String[alHostNames.size()];
      for(int j = 0; j < alHostNames.size(); j++)
      {
        arrHostNames[j] = alHostNames.get(j).toString();
        Log.debugLog(className, "getHOSTNames", "", "", "Host Name found = " + arrHostNames[j]);
      }
      return arrHostNames;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getHOSTNames", "", "", "Exception - ", ex);
      return new String[]{""};
    }
  }

  //Method to get all Services by Host Name
  public String[] getServicesByHostName(String hostName)
  {
    Log.debugLog(className, "getServicesByHostName", "", "", "Method started");
    try
    {
      ArrayList alServiceName = new ArrayList();
      File serviceDir = new File(getCorrelationPath() + hostName + "/services/");

      File[] files = serviceDir.listFiles();
      if(files == null || files.length <= 0)
        return new String[0];
      for(int i = 0; i < files.length; i++)
      {
        //hidden services and startswith '__' service will not show
        if(files[i].isDirectory() && !files[i].getName().trim().startsWith(".") && !files[i].getName().trim().startsWith("__"))
          alServiceName.add(files[i].getName());
      }

      String[] arrServiceNames = new String[alServiceName.size()];
      for(int j = 0; j < alServiceName.size(); j++)
      {
        arrServiceNames[j] = alServiceName.get(j).toString();
        Log.debugLog(className, "getServicesByHostName", "", "", "Service Name found = " + arrServiceNames[j]);
      }

      return arrServiceNames;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getServicesByHostName", "", "", "Exception - ", ex);
      return null;
    }
  }

  //Method to get all Services
  public String[] getAllServices()
  {
    Log.debugLog(className, "getAllServices", "", "", "Method started");
    try
    {
      ArrayList alServiceName = new ArrayList();
      String[] hostNames = getHOSTNames();
      if(hostNames == null || hostNames.length <= 0)
        return new String[0];
      for(int i = 0; i < hostNames.length; i++)
      {
        String[] serviceNames = getServicesByHostName(hostNames[i]);
        for(int j = 0; j < serviceNames.length; j++)
        {
          alServiceName.add(serviceNames[j]);
        }
      }

      String[] arrServiceNames = new String[alServiceName.size()];
      for(int j = 0; j < alServiceName.size(); j++)
      {
        arrServiceNames[j] = alServiceName.get(j).toString();
        Log.debugLog(className, "getAllServices", "", "", "Service Name found = " + arrServiceNames[j]);
      }
      return arrServiceNames;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getAllServices", "", "", "Exception - ", ex);
      return null;
    }
  }

  //method to get service path
  public String getServicePath(String hostName, String serviceName)
  {
    Log.debugLog(className, "getServicePath", "", "", "Method started");
    String servicePath = "";
    try
    {
      servicePath = getCorrelationPath() + hostName + "/services/" + serviceName + "/";
      return servicePath;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getServicePath", "", "", "Exception - ", ex);
      return servicePath;
    }
  }

  //method to get services folder path
  public String getServicesPath(String hostName)
  {
    Log.debugLog(className, "getServicesPath", "", "", "Method started");
    String servicesPath = "";
    try
    {
      servicesPath = getCorrelationPath() + hostName + "/services/";
      return servicesPath;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getServicesPath", "", "", "Exception - ", ex);
      return servicesPath;
    }
  }

  //method to get service path
  public String getServiceConfFilePath(String hostName, String serviceName)
  {
    Log.debugLog(className, "getServiceConfFilePath", "", "", "Method started");
    String servicePath = "";
    try
    {
      servicePath = getServicePath(hostName, serviceName) + "/service.conf";
      return servicePath;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getServiceConfFilePath", "", "", "Exception - ", ex);
      return servicePath;
    }
  }

  //method to get service log path
  public String getServiceLogFilePath(String hostName, String serviceName)
  {
    Log.debugLog(className, "getServiceLogFilePath", "", "", "Method started");
    String servicePath = "";
    try
    {
      servicePath = getServicePath(hostName, serviceName) + "/service.log";
      return servicePath;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getServiceLogFilePath", "", "", "Exception - ", ex);
      return servicePath;
    }
  }

  //method to get the port number for HTTP or HTTPS
  public String getHPDPort(String strHTTP, String allOrOne)
  {
    Log.debugLog(className, "getHPDPort", "", "", "Method started");
    String searchString = "";
    String portNumber = "";
    if(strHTTP.toUpperCase().equals("HTTP"))
    {
      searchString = "HPD_PORT";
      portNumber = "80";
    }
    else if(strHTTP.toUpperCase().equals("HTTPS"))
    {
      searchString = "HPD_SPORT";
      portNumber = "443";
    }
    try
    {
      Vector hpdConfData = readFile(getHPDPath() + "conf/hpd.conf", true);
      if(hpdConfData == null)
      {
        Log.debugLog(className, "getHPDPort", "", "", "hpd.conf File not found, It may be corrupted.");
        return portNumber;
      }

      for(int i = 0; i < hpdConfData.size(); i++)
      {
        if(hpdConfData.elementAt(i).toString().indexOf(searchString) > -1)
        {
          String[] arrLine = rptUtilsBean.strToArrayData(hpdConfData.elementAt(i).toString(), " ");
          if(arrLine == null || arrLine.length <= 1)
            return portNumber;
          if(allOrOne.equals("all"))
            return arrLine[1];
          else if(allOrOne.equals("one"))
          {
            String[] ports = rptUtilsBean.strToArrayData(arrLine[1], ",");
            if(ports == null || ports.length < 1)
              return portNumber;
            else
              return ports[0];
          }
        }
      }
      return portNumber;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getHPDPort", "", "", "Exception - ", ex);
      return portNumber;
    }
  }

  public String getHPDIP()
  {
    Log.debugLog(className, "getHPDIP", "", "", "Method started");
    String searchString = "HPD_SERVER_ADDRESS";
    String HPD_IP = "localhost";
    try
    {
      Vector hpdConfData = readFile(getHPDPath() + "conf/hpd.conf", true);
      if(hpdConfData == null)
      {
        Log.debugLog(className, "getHPDIP", "", "", "hpd.conf File not found, It may be corrupted.");
        return HPD_IP;
      }

      for(int i = 0; i < hpdConfData.size(); i++)
      {
        if(hpdConfData.elementAt(i).toString().indexOf(searchString) > -1)
        {
          String[] arrLine = rptUtilsBean.strToArrayData(hpdConfData.elementAt(i).toString(), " ");
          if(arrLine == null || arrLine.length <= 1)
            return HPD_IP;

          HPD_IP = arrLine[1];
        }
      }

      return HPD_IP;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getHPDIP", "", "", "Exception - ", ex);
      return HPD_IP;
    }
  }

  //Method to get Request Trace
  public String[][] getRequestTraceLogs(int sortOnCol, int sortPrefrence, int numberOfRows)
  {
    Log.debugLog(className, "getRequestTraceLogs", "", "", "Method started");
    String[][] requestTraceTemp = null;
    String[][] requestTrace = null;
    try
    {
      String cmdName = "nou_list_req_resp_log";
      String cmdArgs = "-n " + numberOfRows;
      CmdExec objCmdExec = new CmdExec();
      Vector vecCmdOutPut = objCmdExec.getResultByCommand(cmdName, cmdArgs, CmdExec.SYSTEM_CMD, null, "root");

      if((vecCmdOutPut == null) || (vecCmdOutPut.size() <= 0))
      {
        Log.debugLog(className, "getRequestTraceLogs", "", "", "No Logs found");
        return requestTrace;
      }
      vecCmdOutPut.removeElementAt(0);

      requestTraceTemp = rptUtilsBean.getRecFlds(vecCmdOutPut, "", "", "-");
      ArrayList arrList = new ArrayList();

      if(vecCmdOutPut != null)
      {
        for(int i = 0; i < vecCmdOutPut.size(); i++)
        {
          int fisrtPipe = vecCmdOutPut.get(i).toString().indexOf("|");
          int SecondPipe = fisrtPipe + 1 + vecCmdOutPut.get(i).toString().substring(fisrtPipe + 1).indexOf("|");
          Vector vecLogData = getLogFromRequestTraceFile(vecCmdOutPut.get(i).toString().substring(fisrtPipe + 1, SecondPipe));
          String[] arrTemp = null;
          if(vecLogData != null && vecLogData.size() > 0)
          {
            arrTemp = new String[14];
            String serviceName = "NA";
            String parameterString = "NA";
            String Request_File = "NA";
            String URL = "NA";
            String Response_File = "NA";
            String Client_IP = "NA";
            String Request_Time = "NA";
            String Response_Time = "NA";
            String Response_Template = "NA";
            String ReqMiliSeconds = "0";
            String ResMiliSeconds = "0";
            String ConvertedReqTimeMiliSec = "0";
            String ConverTedResTimeMiliSec = "0";

            for(int ii = 0; ii < vecLogData.size(); ii++)
            {
              String TraceFile = vecCmdOutPut.get(i).toString().substring(fisrtPipe + 1, SecondPipe);

              String[] arrData = rptUtilsBean.split(vecLogData.get(ii).toString(), "|");
              if(arrData.length < 3)
                continue;

              serviceName = arrData[0];
              if(arrData[1].equals("Request File"))
                Request_File = arrData[2]; //set Request file
              if(arrData[1].equals("Request Time"))
              {
                Request_Time = arrData[2]; //set Request Time
                if(arrData.length > 3)
                {
                  ReqMiliSeconds = arrData[3];
                }

                if(!Request_Time.equals("NA"))
                {
                  Date timeDate = new Date(Request_Time);
                  ConvertedReqTimeMiliSec = String.valueOf(timeDate.getTime());
                }
              }
              if(arrData[1].equals("URL"))
                URL = arrData[2];//set URL
              if(arrData[1].equals("Client IP/port"))
                Client_IP = arrData[2]; // set client IP/ports
              if(arrData[1].equals("Response File"))
                Response_File = arrData[2]; //set response file name
              if(arrData[1].equals("Response Time"))
              {
                Response_Time = arrData[2]; //set Response Time
                if(arrData.length > 3)
                {
                  ResMiliSeconds = arrData[3];
                }

                if(!Response_Time.equals("NA"))
                {
                  Date timeDate = new Date(Response_Time);
                  ConverTedResTimeMiliSec = String.valueOf(timeDate.getTime());
                }

              }
              if(arrData[1].equals("Response Template Name"))
                Response_Template = arrData[2];//set Template name
              if(arrData[1].equals("Parameter"))
              {
                if(arrData.length > 4)
                {
                  if(parameterString.equals("NA"))
                    parameterString = arrData[3] + "=" + URLDecoder.decode(arrData[4]);
                  else
                    parameterString = parameterString + ", " + arrData[3] + "=" + URLDecoder.decode(arrData[4]);
                }
              }
              arrTemp[8] = parameterString;//set Parameters
              arrTemp[9] = TraceFile;
              arrTemp[1] = Request_File;
              arrTemp[2] = Request_Time;
              arrTemp[3] = URL;
              arrTemp[4] = Client_IP;
              arrTemp[5] = Response_File;
              arrTemp[6] = Response_Time;
              arrTemp[7] = Response_Template;
              arrTemp[0] = serviceName;
              arrTemp[10] = ReqMiliSeconds;
              arrTemp[11] = ResMiliSeconds;
              arrTemp[12] = ConvertedReqTimeMiliSec;
              arrTemp[13] = ConverTedResTimeMiliSec;
            }
          }
          if(arrTemp != null)
          {
            arrList.add(arrTemp);
          }
        }
      }
      if(arrList.size() > 0)
      {
        requestTrace = new String[arrList.size()][14];
        for(int i = 0; i < arrList.size(); i++)
        {
          String[] arr = (String[])arrList.get(i);
          requestTrace[i][8] = arr[8];//set Parameters
          requestTrace[i][9] = arr[9];
          requestTrace[i][1] = arr[1];
          requestTrace[i][2] = arr[2];
          requestTrace[i][3] = arr[3];
          requestTrace[i][4] = arr[4];
          requestTrace[i][5] = arr[5];
          requestTrace[i][6] = arr[6];
          requestTrace[i][7] = arr[7];
          requestTrace[i][0] = arr[0];
          requestTrace[i][10] = arr[10];
          requestTrace[i][11] = arr[11];
          requestTrace[i][12] = arr[12];
          requestTrace[i][13] = arr[13];
        }

        if((sortOnCol == 6))
        {
          sortOnCol = 13;
          requestTrace = sortArrayForReqTraceTime(requestTrace, sortOnCol, sortPrefrence, 11);
        }
        else if(sortOnCol == 2)
        {
          sortOnCol = 12;
          requestTrace = sortArrayForReqTraceTime(requestTrace, sortOnCol, sortPrefrence, 10);
        }
        else
          requestTrace = sortArray(requestTrace, sortOnCol, sortPrefrence, "STRING");
      }
      return requestTrace;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getRequestTraceLogs", "", "", "Exception - ", ex);
      return requestTrace;
    }
  }

  public String getServiceNameByURL(String URL)
  {
    Log.debugLog(className, "getServiceNameByURL", "", "", "Method Start. URL = " + URL);
    String serviceName = "";
    try
    {
      String[] hostNames = getHOSTNames();
      if(hostNames == null || hostNames.length <= 0)
        return "";
      for(int xx = 0; xx < hostNames.length; xx++)
      {
        String[] serviceNames = getServicesByHostName(hostNames[xx]);
        for(int yy = 0; yy < serviceNames.length; yy++)
        {
          String urlByServiceName = getURLByHostAndService(hostNames[xx], serviceNames[yy]);
          Log.debugLog(className, "getServiceNameByURL", "", "", "urlByServiceName = " + urlByServiceName);
          if(urlByServiceName.equals(URL))
          {
            Log.debugLog(className, "getServiceNameByURL", "", "", "URL matched serviceName = " + serviceNames[yy]);
            serviceName = serviceNames[yy];
            break;
          }
        }
      }

      return serviceName;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getServiceNameByURL", "", "", "Exception", ex);
      return "";
    }
  }

  /* public String[][] sortArray(String[][] dataArray, final int colNumberToSort, final int sortPrefrenc)
   {
     return sortArray(dataArray, colNumberToSort, sortPrefrenc, "STRING");
   }*/

  public String[][] sortArray(String[][] dataArray, final int colNumberToSort, final int sortPrefrence, String dataType)
  {
    if(dataType.equals("NEUMERIC"))
    {
      Arrays.sort(dataArray, new Comparator<String[]>()
      {
        public int compare(String[] s1, String[] s2)
        {
          if(sortPrefrence == 0)
            return Integer.valueOf(s1[colNumberToSort]).compareTo(Integer.valueOf(s2[colNumberToSort]));
          else
            return Integer.valueOf(s2[colNumberToSort]).compareTo(Integer.valueOf(s1[colNumberToSort]));
        }
      });
    }
    else if(dataType.equals("LONG"))
    {
      Arrays.sort(dataArray, new Comparator<String[]>()
      {
        public int compare(String[] s1, String[] s2)
        {
          if(sortPrefrence == 0)
            return Long.valueOf(s1[colNumberToSort]).compareTo(Long.valueOf(s2[colNumberToSort]));
          else
            return Long.valueOf(s2[colNumberToSort]).compareTo(Long.valueOf(s1[colNumberToSort]));
        }
      });
    }
    else if(dataType.equals("DATE"))
    {
      dataArray = sortArray(dataArray, colNumberToSort, sortPrefrence, "STRING");

      Arrays.sort(dataArray, new Comparator<String[]>()
      {
        // @Override
        public int compare(final String[] entry1, final String[] entry2)
        {
          final DateFormat fmt = new SimpleDateFormat("MM/dd/yyyy hh:mm");
          try
          {
            if(entry1[colNumberToSort].equals("NA") || entry2[colNumberToSort].equals("NA"))
              return 0;
            if(sortPrefrence == 0)
              return fmt.parse(entry1[colNumberToSort]).compareTo(fmt.parse(entry2[colNumberToSort]));
            else
              return fmt.parse(entry2[colNumberToSort]).compareTo(fmt.parse(entry1[colNumberToSort]));
          }
          catch(ParseException e)
          {
            Log.debugLog(className, "sortDateArray", "", "", "Exception - " + e);
            throw new RuntimeException(e);
          }
        }
      });
    }
    else if(dataType.equals("DECIMAL"))
    {
      Arrays.sort(dataArray, new Comparator<String[]>()
      {
        public int compare(String[] s1, String[] s2)
        {
          if(sortPrefrence == 0)
            return Float.valueOf(s1[colNumberToSort]).compareTo(Float.valueOf(s2[colNumberToSort]));
          else
            return Float.valueOf(s2[colNumberToSort]).compareTo(Float.valueOf(s1[colNumberToSort]));
        }
      });
    }
    else
    {
      Arrays.sort(dataArray, new Comparator<String[]>()
      {
        public int compare(final String[] entry1, final String[] entry2)
        {
          final String time1 = entry1[colNumberToSort];
          final String time2 = entry2[colNumberToSort];
          if(sortPrefrence == 0)
            return time1.compareToIgnoreCase(time2);
          else
            return time2.compareToIgnoreCase(time1);
        }
      });
    }

    return dataArray;
  }

  /*
   * this method is used to sort req and res time in request time
   * dataArray : data in 2D array
   * colNumberToSort : column number for sorting (req time column)
   * anotherSortCol : if req amd res time is same then use anthoe column to sort(given miliseconds colmun)
   * sortPrefrence : order for sorting(inrease or decrease)
   */
  public String[][] sortArrayForReqTraceTime(String[][] dataArray, final int colNumberToSort, final int sortPrefrence, final int anotherSortCol)
  {
    Arrays.sort(dataArray, new Comparator<String[]>()
    {
      public int compare(String[] s1, String[] s2)
      {
        if(sortPrefrence == 0)
        {
          if(Long.valueOf(s1[colNumberToSort]) > Long.valueOf(s2[colNumberToSort]))
            return Long.valueOf(s1[colNumberToSort]).compareTo(Long.valueOf(s2[colNumberToSort]));
          else if(Long.valueOf(s1[colNumberToSort]) < Long.valueOf(s2[colNumberToSort]))
            return Long.valueOf(s1[colNumberToSort]).compareTo(Long.valueOf(s2[colNumberToSort]));
          else
          {
            return Long.valueOf(s1[anotherSortCol]).compareTo(Long.valueOf(s2[anotherSortCol]));
          }
        }
        else
        {
          if(Long.valueOf(s1[colNumberToSort]) > Long.valueOf(s2[colNumberToSort]))
            return Long.valueOf(s2[colNumberToSort]).compareTo(Long.valueOf(s1[colNumberToSort]));
          else if(Long.valueOf(s1[colNumberToSort]) < Long.valueOf(s2[colNumberToSort]))
            return Long.valueOf(s2[colNumberToSort]).compareTo(Long.valueOf(s1[colNumberToSort]));
          else
          {
            return Long.valueOf(s2[anotherSortCol]).compareTo(Long.valueOf(s1[anotherSortCol]));
          }
        }
      }
    });

    return dataArray;
  }

  public String[][] sortDateArray(String[][] dataArray, final int colNumberToSort, final int sortPrefrence)
  {
    String dateFormat = "MM/dd/yyyy hh:mm";
    return sortDateArray(dataArray, colNumberToSort, sortPrefrence, dateFormat);
  }

  //this function sort the array which contain the Date value on the 12th index.
  public String[][] sortDateArray(String[][] dataArray, final int colNumberToSort, final int sortPrefrence, final String userDefindDateFormat)
  {
    dataArray = sortArray(dataArray, colNumberToSort, sortPrefrence, "STRING");

    Arrays.sort(dataArray, new Comparator<String[]>()
    {
      // @Override
      public int compare(final String[] entry1, final String[] entry2)
      {
        final DateFormat fmt = new SimpleDateFormat(userDefindDateFormat);
        try
        {
          if(entry1[colNumberToSort].equals("NA") || entry2[colNumberToSort].equals("NA"))
            return 0;

          if(sortPrefrence == 0)
            return fmt.parse(entry1[colNumberToSort]).compareTo(fmt.parse(entry2[colNumberToSort]));
          else
            return fmt.parse(entry2[colNumberToSort]).compareTo(fmt.parse(entry1[colNumberToSort]));
        }
        catch(ParseException e)
        {
          Log.debugLog(className, "sortDateArray", "", "", "Exception - " + e);
          throw new RuntimeException(e);
        }
      }
    });

    return dataArray;
  }

  //Method to get HPD Logs
  public StringBuffer getHPDLogs()
  {
    Log.debugLog(className, "getHPDLogs", "", "", "Method started");
    StringBuffer fileContents = new StringBuffer();
    try
    {
      String cmdName = "nou_list_req_resp_log";
      String cmdArgs = "-n";
      CmdExec objCmdExec = new CmdExec();
      Vector vecCmdOutPut = objCmdExec.getResultByCommand(cmdName, cmdArgs, CmdExec.SYSTEM_CMD, null, "root");

      if((vecCmdOutPut == null) || (vecCmdOutPut.size() <= 0))
      {
        Log.debugLog(className, "getHPDLogs", "", "", "No Logs found");
        return fileContents;
      }

      for(int ds = 0; ds < vecCmdOutPut.size(); ds++)
      {
        fileContents.append(vecCmdOutPut.elementAt(ds).toString() + "\n");
      }
      return fileContents;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getHPDLogs", "", "", "Exception - ", ex);
      return fileContents;
    }
  }

  public String[][] getNOServices()
  {
    return getNOServices(12, 1, "");
  }

  public String[][] getNOServices(int sortOnCol, int sortPrefrence, String filterKeyword)
  {
    Log.debugLog(className, "getNOServices", "", "", "Method started");
    try
    {
      String[] hostNames = getHOSTNames();
      if(hostNames == null || hostNames.length <= 0)
      {
        Log.debugLog(className, "getNOServices", "", "", "No Host Name found ");
        return new String[0][0];
      }
      Log.debugLog(className, "getNOServices", "", "", "Number of host name found = " + hostNames.length);

      ArrayList alHostService = new ArrayList();
      for(int h = 0; h < hostNames.length; h++)
      {
        String[] serviceNames = getServicesByHostName(hostNames[h]);
        for(int s = 0; s < serviceNames.length; s++)
        {
          String[][] arrTemp = new String[1][2];
          arrTemp[0][0] = hostNames[h];
          arrTemp[0][1] = serviceNames[s];
          alHostService.add(arrTemp);
        }
      }
      Log.debugLog(className, "getNOServices", "", "", "Number of services found = " + alHostService.size());
      String[][] arrNOServices = new String[alHostService.size()][18];

      for(int hs = 0; hs < alHostService.size(); hs++)
      {
        String[][] serviceHost = (String[][])alHostService.get(hs);
        String host = serviceHost[0][0];
        String service = serviceHost[0][1];
        arrNOServices[hs][0] = host;
        arrNOServices[hs][1] = service;

        Vector hpdURLData = readFile(getServiceConfFilePath(host, service), true);
        Vector serivceLogData = readFile(getServiceLogFilePath(host, service), true);

        if(hpdURLData == null)
        {
          Log.debugLog(className, "getNOServices", "", "", "URL File not found, It may be corrupted. HOST=" + host + ", service=" + service);
          arrNOServices[hs][2] = "";
          arrNOServices[hs][3] = "";
          arrNOServices[hs][4] = "NA";
          arrNOServices[hs][5] = "NA";
          arrNOServices[hs][6] = "0";
          arrNOServices[hs][7] = "0";
          arrNOServices[hs][8] = "0";
          arrNOServices[hs][9] = "No";
          arrNOServices[hs][10] = "0";
          arrNOServices[hs][11] = "0";
          arrNOServices[hs][12] = "NA";
          arrNOServices[hs][13] = "NA";
          arrNOServices[hs][14] = "NA";
          arrNOServices[hs][15] = "NA";
          arrNOServices[hs][16] = "NA";
          arrNOServices[hs][17] = "NA";
          continue;
        }

        String URL = "";
        int responseFiles = 0;
        String onRequest = "NA";
        String afterRequest = "NA";
        int searchVar = 0;
        int httpHeaders = 0;
        int scratchVar = 0;
        String serviceTime = "No";
        int queryVar = 0;
        int numberOfParameters = 0;
        int cookieVar = 0;
        String URLStatus = "";
        String CurrentVersion = "";
        String ServiceMode = "";
        String ServiceType = "HTTP";
        boolean activeURL = false;
        String firstfetchDisableURL = "";
        for(int k = 0; k < hpdURLData.size(); k++)
        {
          String[] arrURLLine = rptUtilsBean.strToArrayData(hpdURLData.elementAt(k).toString(), " ");
          if(arrURLLine.length <= 0)
            continue;
          if(arrURLLine[0].equals("URL"))
          {
            if(activeURL)
              continue;
            if(arrURLLine.length < 2)
              continue;
            URL = arrURLLine[1];
            URLStatus = "enabled";
            activeURL = true;
          }
          else if(arrURLLine[0].equals("#URL") || arrURLLine[0].equals("#"))
          {
            if(!activeURL)
            {
              if(arrURLLine[0].equals("#URL"))
              {
                URLStatus = "disabled";
                if(arrURLLine.length < 2)
                  continue;
                URL = arrURLLine[1];
                if(firstfetchDisableURL.equals(""))
                  firstfetchDisableURL = arrURLLine[1];
              }
              else
              {
                URLStatus = "disabled";
                if(arrURLLine.length < 3)
                  continue;
                URL = arrURLLine[2];
                if(firstfetchDisableURL.equals(""))
                  firstfetchDisableURL = arrURLLine[1];
              }
            }
          }
          else if(arrURLLine[0].equals("RESPONSE_TEMPLATE"))
          {
            responseFiles++;
          }
          else if(arrURLLine[0].equals("nsl_search_var"))
          {
            searchVar++;
            numberOfParameters++;
          }
          else if(arrURLLine[0].equals("SET_CR_URL_HEADER"))
            httpHeaders++;
          else if(arrURLLine[0].equals("POST_RECV_FN"))
            afterRequest = arrURLLine[1];
          else if(arrURLLine[0].equals("PRE_SEND_FN"))
            onRequest = arrURLLine[1];
          else if(arrURLLine[0].equals("nsl_decl_var"))
          {
            scratchVar++;
            numberOfParameters++;
          }
          else if(arrURLLine[0].equals("SVC_TIME") && !arrURLLine[1].equals("0"))
            serviceTime = "Yes";
          else if(arrURLLine[0].equals("QUERY_VAR"))
          {
            queryVar++;
            numberOfParameters++;
          }
          else if(arrURLLine[0].equals("nsl_cookie_var"))
          {
            cookieVar++;
            numberOfParameters++;
          }
          else if(arrURLLine[0].startsWith("nsl_request_var") || arrURLLine[0].startsWith("nsl_decl_array") || arrURLLine[0].equals("nsl_static_var") || arrURLLine[0].equals("nsl_index_file_var") || arrURLLine[0].equals("nsl_date_var") || arrURLLine[0].equals("nsl_random_number_var") || arrURLLine[0].equals("nsl_random_string_var") || arrURLLine[0].equals("nsl_unique_number_var") || arrURLLine[0].equals("nsl_xml_var") || arrURLLine[0].equals("nsl_select_index_datasource"))
          {
            numberOfParameters++;
          }

        }

        String ResponseTemplatesInfo[][] = getResponseTemplateInfoByURL("default", arrNOServices[hs][1], rptUtilsBean.replaceSpecialCharacter(URL));

        ArrayList<Integer> actCol = new ArrayList<Integer>();

        for(int i = 0; i < ResponseTemplatesInfo.length; i++)
        {
          if(ResponseTemplatesInfo[i][5].trim().equals("active"))
          {
            actCol.add(i);
          }
        }
        String templateType = "Simulate";

        if(actCol.size() > 0)
          templateType = ResponseTemplatesInfo[actCol.get(0)][1];

        if(templateType.contains("Forward"))
          ServiceMode = "Forward";
        else if(templateType.contains("FileBased"))
          ServiceMode = "FileBased";
        else if(templateType.contains("RequestBased"))
          ServiceMode = "RequestBased";
        else
          ServiceMode = "Simulate";

        for(int i = 1; i < actCol.size(); i++)
        {
          if(!ResponseTemplatesInfo[actCol.get(i)][1].contains(ServiceMode))
          {
            ServiceMode = "Mixed";
          }
        }
        
        if(!activeURL)
          URL = firstfetchDisableURL;

        arrNOServices[hs][2] = rptUtilsBean.replaceSpecialCharacter(URL);
        arrNOServices[hs][3] = responseFiles + "";
        arrNOServices[hs][4] = afterRequest;
        arrNOServices[hs][5] = onRequest;
        arrNOServices[hs][6] = searchVar + "";
        arrNOServices[hs][7] = httpHeaders + "";
        arrNOServices[hs][8] = scratchVar + "";
        arrNOServices[hs][9] = serviceTime;
        arrNOServices[hs][10] = queryVar + "";
        arrNOServices[hs][11] = numberOfParameters + "";
        arrNOServices[hs][14] = URLStatus;
        arrNOServices[hs][16] = ServiceMode;
        ServiceType = getURLType(rptUtilsBean.replaceSpecialCharacter(URL));
        if(ServiceType.trim().equals(""))
          ServiceType = "HTTP";
        if(ServiceType.trim().equals("SPRING"))
          ServiceType = "SPRING REMOTING";
        if(ServiceType.equals("JAVACLASS"))
          ServiceType = "Java Class";

        arrNOServices[hs][17] = ServiceType;

        String lastModifiedDate = "NA";
        String lastModifiedBy = "NA";
        if(serivceLogData != null)
        {
          for(int jj = 0; jj < serivceLogData.size(); jj++)
          {
            Log.debugLog(className, "getNOServices", "", "", "serivceLogData=" + serivceLogData.get(jj).toString());
            if(serivceLogData.get(jj).toString().trim().toUpperCase().startsWith("LMD"))
            {
              String[] temp = rptUtilsBean.strToArrayData(serivceLogData.get(jj).toString().trim(), "|");
              if(temp != null && temp.length > 0)
              {
                String[] temp1 = rptUtilsBean.strToArrayData(temp[0], "=");
                lastModifiedDate = temp1[1];
              }
              if(temp != null && temp.length > 1)
              {
                Log.debugLog(className, "getNOServices", "", "", "temp[1]=" + temp[1]);
                String[] temp2 = rptUtilsBean.strToArrayData(temp[1], "=");
                if(temp2.length > 1)
                  lastModifiedBy = temp2[1];
              }
            }
          }
        }

        arrNOServices[hs][12] = lastModifiedDate;
        arrNOServices[hs][13] = lastModifiedBy;

        /*String cmdName = "nsi_cvs";
        String servicePath =  getServicePath(host, service);
        String cmdArgs = "-o currentversion -s " + servicePath ;

        CmdExec objCmdExec = new CmdExec();

        boolean result = true;

        Vector vecCmdOutPut = objCmdExec.getResultByCommand(cmdName, cmdArgs, CmdExec.SYSTEM_CMD, null, "root");

        if((vecCmdOutPut.size() > 0) && ((String)vecCmdOutPut.lastElement()).startsWith("ERROR"))
        {
          arrNOServices[hs][15] = "NA" ;
        }
        else if(vecCmdOutPut.size() > 0)
        {
          vecCmdOutPut.removeElementAt(0);
          
          if(vecCmdOutPut.size() > 0)
          {
            String [] tempOutput = rptUtilsBean.split(vecCmdOutPut.get(0).toString(),"|");
            
            if(tempOutput.length > 0 && !tempOutput[0].equals("") && !tempOutput[0].equals("NA"))
              arrNOServices[hs][15] = tempOutput[0];
            else
              arrNOServices[hs][15] = "NA";
          }
        }
        else*/
        arrNOServices[hs][15] = "NA";
      }

      //code for filter if filterKeyword is not blank
      if(!filterKeyword.trim().equals(""))
      { 
        String[] arrFilterValues = filterKeyword.split("%%");
        String filterMode = arrFilterValues[0];
        String filterServiceType = arrFilterValues[1];
        String filterURLStatus = arrFilterValues[2];
        String filterLastModified = arrFilterValues[3];
        if(arrFilterValues.length > 4)
          filterKeyword = arrFilterValues[4];
        else
          filterKeyword = "";
        
        ArrayList arrFilteredNOServices = new ArrayList();
        
        for(int ni = 0; ni < arrNOServices.length; ni++)
        {
          if(!filterKeyword.equals(""))
          {
            if(arrNOServices[ni][1].toLowerCase().contains(filterKeyword.toLowerCase()) || arrNOServices[ni][2].toLowerCase().contains(filterKeyword.toLowerCase()))
              arrFilteredNOServices.add(arrNOServices[ni]);
          }
          else
            arrFilteredNOServices.add(arrNOServices[ni]);
        }
        
        if(!filterMode.equals("All"))
        {
          ArrayList<Integer> removeIndexs = new ArrayList<Integer>();
          
          for(int v = 0 ; v < arrFilteredNOServices.size() ; v++ )
          {
            String [] tempArrValues = (String [])arrFilteredNOServices.get(v);
            if(!tempArrValues[16].trim().equals(filterMode.trim()))
            { 
              removeIndexs.add(v);
            }
          }
          
          ArrayList temp = new ArrayList();
          for(int r = 0 ; r < arrFilteredNOServices.size() ; r++)
          {
            if(removeIndexs.indexOf(r) < 0)
            {
              temp.add(arrFilteredNOServices.get(r));
            }
          }
          arrFilteredNOServices.clear();
          
          for(int r = 0 ; r < temp.size() ; r++)
            arrFilteredNOServices.add(temp.get(r));
        }
        
        if(!filterServiceType.equals("All"))
        {
          ArrayList<Integer> removeIndexs = new ArrayList<Integer>();
        
          for(int v = 0 ; v < arrFilteredNOServices.size() ; v++ )
          {
            String [] tempArrValues = (String [])arrFilteredNOServices.get(v);
            if(!tempArrValues[17].trim().equals(filterServiceType.trim()))
              removeIndexs.add(v);
          }
        
          ArrayList temp = new ArrayList();
          for(int r = 0 ; r < arrFilteredNOServices.size() ; r++)
          {
            if(removeIndexs.indexOf(r) < 0)
            {
              temp.add(arrFilteredNOServices.get(r));
            }
          }
          arrFilteredNOServices.clear();
          
          for(int r = 0 ; r < temp.size() ; r++)
            arrFilteredNOServices.add(temp.get(r));
        }
        
        if(!filterURLStatus.equals("All"))
        {
          ArrayList<Integer> removeIndexs = new ArrayList<Integer>();
      
          for(int v = 0 ; v < arrFilteredNOServices.size() ; v++ )
          {
            String [] tempArrValues = (String [])arrFilteredNOServices.get(v);
            if(!tempArrValues[14].trim().equals(filterURLStatus.trim()))
              removeIndexs.add(v);
          }
      
          ArrayList temp = new ArrayList();
          for(int r = 0 ; r < arrFilteredNOServices.size() ; r++)
          {
            if(removeIndexs.indexOf(r) < 0)
            {
              temp.add(arrFilteredNOServices.get(r));
            }
          }
          arrFilteredNOServices.clear();
          
          for(int r = 0 ; r < temp.size() ; r++)
            arrFilteredNOServices.add(temp.get(r));
        }
        
        if(!filterLastModified.equals("") && !filterLastModified.trim().equals("0"))
        {
          long filterdate = previousDate(Integer.parseInt(filterLastModified));
          ArrayList<Integer> removeIndexs = new ArrayList<Integer>();
          for(int v = 0 ; v < arrFilteredNOServices.size() ; v++ )
          {
            String [] tempArrValues = (String [])arrFilteredNOServices.get(v);
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy hh:mm");
            
            long totalMiliSec = 0;
            
            if(!tempArrValues[12].equals("NA"))
            {
              cal.setTime(df.parse(tempArrValues[12]));
              totalMiliSec = cal.getTimeInMillis();
            }
            if(!(totalMiliSec > filterdate ))
              removeIndexs.add(v);
          }
          
          ArrayList temp = new ArrayList();
          
          for(int r = 0 ; r < arrFilteredNOServices.size() ; r++)
          {
            if(removeIndexs.indexOf(r) < 0)
            {
              temp.add(arrFilteredNOServices.get(r));
            }
          }
          arrFilteredNOServices.clear();
          
          for(int r = 0 ; r < temp.size() ; r++)
            arrFilteredNOServices.add(temp.get(r));
        }
            
        String[][] filteredNOServices = new String[arrFilteredNOServices.size()][14];
        for(int shi = 0; shi < arrFilteredNOServices.size(); shi++)
        {
          filteredNOServices[shi] = (String[])arrFilteredNOServices.get(shi);
        }

        if(sortOnCol == 12)
          filteredNOServices = sortArray(filteredNOServices, sortOnCol, sortPrefrence, "DATE");
        else if(sortOnCol == 11 || sortOnCol == 3)
          filteredNOServices = sortArray(filteredNOServices, sortOnCol, sortPrefrence, "NEUMERIC");
        else
          filteredNOServices = sortArray(filteredNOServices, sortOnCol, sortPrefrence, "STRING");

        return filteredNOServices;
      }

      if(sortOnCol == 12)
        arrNOServices = sortArray(arrNOServices, sortOnCol, sortPrefrence, "DATE");
      else if(sortOnCol == 11 || sortOnCol == 3)
        arrNOServices = sortArray(arrNOServices, sortOnCol, sortPrefrence, "NEUMERIC");
      else
        arrNOServices = sortArray(arrNOServices, sortOnCol, sortPrefrence, "STRING");

      return arrNOServices;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getNOServices", "", "", "Exception - ", ex);
      return null;
    }
  }

  public long previousDate(int days)throws ParseException
  {
     Log.debugLog(className, "previousDate", "", "", "method called.");
     try
     {
       Calendar origDay = Calendar.getInstance();
       Calendar prevDay = (Calendar) origDay.clone();
       prevDay.add (Calendar.DAY_OF_YEAR, -days);
       return prevDay.getTimeInMillis(); 
     }
     catch(Exception e)
     {
       Log.stackTraceLog(className, "previousDate", "", "", "Exception ", e);
       return 0;
     }
  } 
  
  //method to get the URL from service.conf by host name and service name
  public String getURLByHostAndService(String hostName, String serviceName)
  {
    Log.debugLog(className, "getURLByHostAndService", "", "", "Method started host=" + hostName + ", service=" + serviceName);
    try
    {
      Vector hpdURLData = readFile(getServiceConfFilePath(hostName, serviceName), true);

      if((hpdURLData == null) || (hpdURLData.size() <= 0))
      {
        Log.debugLog(className, "getURLByHostAndService", "", "", "Service File not found, It may be corrupted. Host=" + hostName + ", Service=" + serviceName);
        return "";
      }

      for(int k = 0; k < hpdURLData.size(); k++)
      {
        String[] arrURLLine = rptUtilsBean.strToArrayData(hpdURLData.elementAt(k).toString(), " ");
        if(arrURLLine.length <= 0)
          continue;
        if((arrURLLine[0].trim().equals("URL")) || arrURLLine[0].trim().equals("#URL") || arrURLLine[0].trim().equals("# URL"))
        {
          return rptUtilsBean.replaceSpecialCharacter(arrURLLine[1]);
        }
      }
      return "";
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getURLByHostAndService", "", "", "Exception - ", ex);
      return "";
    }
  }

  // Method to get search var data for specified URL
  public String[][] getSearchVarByURL(String hostName, String serviceName, String URL, int sortOnCol, int sortPrefrence)
  {
    Log.debugLog(className, "getSearchVarByURL", "", "", "Method started");
    try
    {
      ArrayList alSearchVar = new ArrayList();

      Vector hpdURLData = readFile(getServiceConfFilePath(hostName, serviceName), true);

      if((hpdURLData == null) || (hpdURLData.size() <= 0))
      {
        Log.debugLog(className, "getSearchVarByURL", "", "", "Service File not found, It may be corrupted. Host=" + hostName + ", Service=" + serviceName);
        String[][] dummy = new String[0][9];
        return dummy;
      }
      int rowId = -1;
      String[][] arrSearchVar = null;
      for(int k = 0; k < hpdURLData.size(); k++)
      {
        String line = hpdURLData.elementAt(k).toString();
        String temp = line.trim();

        if(temp.startsWith("nsl_search_var")) // Method found
        {
          int indexForClose = line.lastIndexOf(")");
          if((indexForClose == -1) || (indexForClose != line.length() - 2))
            continue;

          String strParam = line.substring(line.indexOf("(") + 1, indexForClose);
          String paramArr[] = strParam.split(",");

          String varName = "";
          String checkLB = "";
          String checkRB = "";
          String checkORD = "1";
          String checkSaveOffset = "0";
          String checkSaveLength = "0";
          String method = "";
          String ignoreCase = "No";

          try
          {
            Vector vecForSplitStr = new Vector();
            varName = paramArr[0];
            for(int i = 1; i < paramArr.length; i++)
            {
              paramArr[i] = paramArr[i].trim();
              if(paramArr[i].toUpperCase().startsWith("LB"))
              {
                String LBArr[] = paramArr[i].split("=");
                LBArr[1] = LBArr[1].trim();

                if(LBArr.length > 2)
                {
                  for(int j = 2; j < LBArr.length; j++)
                  {
                    LBArr[1] = LBArr[1] + "=" + LBArr[j];
                  }
                }

                if(!LBArr[1].startsWith("\""))
                  continue;

                if(!isEndWithQuotes(LBArr[1], true))
                {
                  String checkArr[] = new String[]{"PAGE", "RB", "ORD", "SaveOffset", "SaveLen"};
                  LBArr[1] = getStrToMatch(paramArr, checkArr, LBArr[1], i, vecForSplitStr);
                  if(LBArr[1] == null)
                    continue;
                }

                LBArr[1] = LBArr[1].substring(1, (LBArr[1].length() - 1));
                checkLB = LBArr[1];
                checkLB = checkLB.replace("\\\"", "\"");
              }
              else if(paramArr[i].toUpperCase().startsWith("RB"))
              {
                String RBArr[] = paramArr[i].split("=");
                RBArr[1] = RBArr[1].trim();

                if(RBArr.length > 2)
                {
                  for(int j = 2; j < RBArr.length; j++)
                  {
                    RBArr[1] = RBArr[1] + "=" + RBArr[j];
                  }
                }

                if(!RBArr[1].startsWith("\""))
                  continue;

                if(!isEndWithQuotes(RBArr[1], true))
                {
                  String checkArr[] = new String[]{"PAGE", "LB", "ORD", "SaveOffset", "SaveLen"};
                  RBArr[1] = getStrToMatch(paramArr, checkArr, RBArr[1], i, vecForSplitStr);
                  if(RBArr[1] == null)
                    continue;
                }

                RBArr[1] = RBArr[1].substring(1, (RBArr[1].length() - 1));
                checkRB = RBArr[1];
                checkRB = checkRB.replace("\\\"", "\"");
              }
              else if(paramArr[i].toUpperCase().startsWith("ORD"))
              {
                String ORDArr[] = paramArr[i].split("=");
                ORDArr[1] = ORDArr[1].trim();
                if(!isValidORDValue(ORDArr[1]))
                  continue;

                checkORD = ORDArr[1];
              }
              else if(paramArr[i].toUpperCase().startsWith("SAVEOFFSET"))
              {
                String saveOffsetArr[] = paramArr[i].split("=");
                saveOffsetArr[1] = saveOffsetArr[1].trim();
                try
                {
                  checkSaveOffset = saveOffsetArr[1];
                }
                catch(Exception e)
                {
                  continue;
                }
              }
              else if(paramArr[i].toUpperCase().startsWith("IGNORECASE"))
              {
                String ignoreCaseArr[] = paramArr[i].split("=");
                ignoreCaseArr[1] = ignoreCaseArr[1].trim();
                try
                {
                  ignoreCase = ignoreCaseArr[1];
                }
                catch(Exception e)
                {
                  continue;
                }
              }
              else if(paramArr[i].toUpperCase().startsWith("SAVELEN"))
              {
                String saveLenArr[] = paramArr[i].split("=");
                saveLenArr[1] = saveLenArr[1].trim();
                try
                {
                  checkSaveLength = saveLenArr[1];
                }
                catch(Exception e)
                {
                  continue;
                }
              }
              else if(paramArr[i].toUpperCase().startsWith("METHOD"))
              {
                String methodArr[] = paramArr[i].split("=");
                if(methodArr.length > 1)
                  method = methodArr[1].trim();
              }
              else
              {
                Log.debugLog(className, "getSearchVarByURL", "", "", "Invalid Parameter = " + paramArr[i]);
                continue;
              }
            }
          }
          catch(Exception e)
          {
            Log.stackTraceLog(className, "getSearchVarByURL", "", "", "Exception - ", e);
            continue;
          }

          if(varName.equals(""))
          {
            Log.debugLog(className, "getSearchVarByURL", "", "", "Varibale name is blank");
            continue;
          }

          String[] arrRowValue = new String[9];
          arrRowValue[0] = varName;
          arrRowValue[1] = FileBean.escapeHTML(checkLB);
          arrRowValue[2] = FileBean.escapeHTML(checkRB);
          arrRowValue[3] = checkORD;
          arrRowValue[4] = checkSaveOffset;
          arrRowValue[5] = checkSaveLength;
          arrRowValue[6] = method;
          ++rowId;
          arrRowValue[7] = String.valueOf(rowId);
          arrRowValue[8] = ignoreCase;
          alSearchVar.add(arrRowValue);
        }
        arrSearchVar = new String[alSearchVar.size()][9];
        for(int ii = 0; ii < alSearchVar.size(); ii++)
        {
          String[] strRowValue = (String[])alSearchVar.get(ii);
          for(int jj = 0; jj < strRowValue.length; jj++)
          {
            arrSearchVar[ii][jj] = strRowValue[jj];
          }
        }
      }
      arrSearchVar = sortArray(arrSearchVar, sortOnCol, sortPrefrence, "STRING");
      return arrSearchVar;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getSearchVarByURL", "", "", "Exception - ", ex);
      String[][] dummy = new String[0][9];
      return dummy;
    }
  }

  //Method to get cookie var data for specified URL
  public String[][] getCookieVarByURL(String hostName, String serviceName, String URL, int sortOnCol, int sortPrefrence)
  {
    Log.debugLog(className, "getCookieVarByURL", "", "", "Method started");
    try
    {
      ArrayList alCookieVar = new ArrayList();
      //create the object of Parsing.java, for parsing the api
      ParameterAPIParsing parse = new ParameterAPIParsing();

      Vector hpdURLData = readFile(getServiceConfFilePath(hostName, serviceName), true);

      if((hpdURLData == null) || (hpdURLData.size() <= 0))
      {
        Log.debugLog(className, "getCookieVarByURL", "", "", "Service File not found, It may be corrupted. Host=" + hostName + ", Service=" + serviceName);
        String[][] dummy = new String[0][10];
        return dummy;
      }

      String[][] arrCookieVar = null;
      int rowId = -1;
      for(int k = 0; k < hpdURLData.size(); k++)
      {
        String line = hpdURLData.elementAt(k).toString();
        String temp = line.trim();

        if(temp.startsWith("nsl_cookie_var")) // Method found
        {
          String varName = "";
          String CookieName = "";
          String checkSaveOffset = "0";
          String checkSaveLength = "0";
          String SpecifiedChars = "";
          String ActionOnNotFound = "";
          String DefaultValue = "";
          String method = "";
          String Encode = "";
          String Decode = "";
          String Position = "";
          ArrayList cookieVarrableList = new ArrayList();
          cookieVarrableList = parse.parseAPI(temp);
          cookieVarrableList.remove(0);
          cookieVarrableList.remove(0);
          varName = ((ArrayList)cookieVarrableList.get(0)).get(0).toString();
          try
          {
            Iterator itr = cookieVarrableList.listIterator();
            while(itr.hasNext())
            {
              ArrayList keyValue = new ArrayList();
              //type cast the list value into ArrayList, because value will be ArrayList
              keyValue = (ArrayList)itr.next();
              if(keyValue.size() > 0)
              {
                if(keyValue.get(0).toString().trim().equals("CookieName") && (keyValue.size() > 1))
                {
                  CookieName = keyValue.get(1).toString();
                }
                else if(keyValue.get(0).toString().trim().equals("StartOffset") && (keyValue.size() > 1))
                {
                  checkSaveOffset = keyValue.get(1).toString();
                }
                else if(keyValue.get(0).toString().trim().equals("SaveLen") && (keyValue.size() > 1))
                {
                  checkSaveLength = keyValue.get(1).toString();
                }
                else if(keyValue.get(0).toString().trim().equals("ActionOnNotFound") && (keyValue.size() > 1))
                {
                  ActionOnNotFound = keyValue.get(1).toString();
                }
                else if(keyValue.get(0).toString().trim().equals("DefaultValue") && (keyValue.size() > 1))
                {
                  DefaultValue = keyValue.get(1).toString();
                  DefaultValue = DefaultValue.replace("\\\"", "\"");
                  DefaultValue = DefaultValue.replace("\\\\", "\\");
                }
                else if(keyValue.get(0).toString().trim().equals("Method") && (keyValue.size() > 1))
                {
                  method = keyValue.get(1).toString();
                }
                else if(keyValue.get(0).toString().trim().equals("Encode") && (keyValue.size() > 1))
                {
                  Encode = keyValue.get(1).toString();
                }
                else if(keyValue.get(0).toString().trim().equals("Decode") && (keyValue.size() > 1))
                {
                  Decode = keyValue.get(1).toString();
                }
                else if(keyValue.get(0).toString().trim().equals("SpecifiedChars") && (keyValue.size() > 1))
                {
                  SpecifiedChars = keyValue.get(1).toString();
                  SpecifiedChars = SpecifiedChars.replace("\\\"", "\"");
                  SpecifiedChars = SpecifiedChars.replace("\\\\", "\\");
                }
                else
                {
                  Log.debugLog(className, "getCookieVarByURL", "", "", "Invalid Parameter = " + keyValue.get(0).toString());
                  continue;
                }
              }
            }
          }
          catch(Exception e)
          {
            Log.stackTraceLog(className, "getCookieVarByURL", "", "", "Exception - ", e);
            continue;
          }

          if(varName.equals(""))
          {
            Log.debugLog(className, "getCookieVarByURL", "", "", "Varibale name is blank");
            continue;
          }
          //System.out.println("CookieName =" +CookieName);
          if(CookieName.equals(""))
            CookieName = varName;
          String[] arrRowValue = new String[11];
          arrRowValue[0] = varName;
          arrRowValue[1] = CookieName;
          arrRowValue[2] = checkSaveOffset;
          arrRowValue[3] = checkSaveLength;
          arrRowValue[4] = SpecifiedChars;
          arrRowValue[5] = ActionOnNotFound;
          arrRowValue[6] = DefaultValue;
          arrRowValue[7] = method;
          arrRowValue[8] = Encode;
          arrRowValue[9] = Decode;
          ++rowId;
          arrRowValue[10] = String.valueOf(rowId);
          alCookieVar.add(arrRowValue);
        }
        arrCookieVar = new String[alCookieVar.size()][11];
        for(int ii = 0; ii < alCookieVar.size(); ii++)
        {
          String[] strRowValue = (String[])alCookieVar.get(ii);
          for(int jj = 0; jj < strRowValue.length; jj++)
          {
            arrCookieVar[ii][jj] = strRowValue[jj];
          }
        }
      }
      arrCookieVar = sortArray(arrCookieVar, sortOnCol, sortPrefrence, "STRING");
      return arrCookieVar;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getCookieVarByURL", "", "", "Exception - ", ex);
      String[][] dummy = new String[0][11];
      return dummy;
    }
  }

  // Method to get Headers for specified URL
  public String[] getHeadersByURL(String hostName, String serviceName, String URL)
  {
    Log.debugLog(className, "getHeadersByURL", "", "", "Method started");
    try
    {
      ArrayList alHeaders = new ArrayList();
      Vector hpdURLData = readFile(getServiceConfFilePath(hostName, serviceName), true);
      if(hpdURLData == null)
      {
        Log.debugLog(className, "getHeadersByURL", "", "", "Service File not found, It may be corrupted. Host=" + hostName + ", Service=" + serviceName);
        return null;
      }

      for(int k = 0; k < hpdURLData.size(); k++)
      {
        String[] arrURLLine = rptUtilsBean.strToArrayData(hpdURLData.elementAt(k).toString(), " ");
        if(arrURLLine.length <= 0)
          continue;
        if(arrURLLine[0].equals("SET_CR_URL_HEADER"))
        {
          String strHeader = "";
          for(int xx = 1; xx < arrURLLine.length; xx++)
          {
            if(xx == 1)
              strHeader = arrURLLine[xx];
            else
              strHeader = strHeader + " " + arrURLLine[xx];
          }
          alHeaders.add(strHeader);
        }
      }
      String[] arrHeaders = new String[alHeaders.size()];
      for(int xy = 0; xy < alHeaders.size(); xy++)
      {
        arrHeaders[xy] = alHeaders.get(xy).toString();
      }
      return arrHeaders;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getHeadersByURL", "", "", "Exception - ", ex);
      return null;
    }
  }

  // Method to get Scratch var vlues for specified URL
  //change return type b'coz in 3.8.4 added default value of scratch parameter
  public String[][] getScratchVarByURL(String hostName, String serviceName, String URL)
  {
    Log.debugLog(className, "getScratchVarByURL", "", "", "Method started");
    try
    {
      ArrayList alScratchBar = new ArrayList();
      int scratchVarCtr = 0;

      Vector hpdURLData = readFile(getServiceConfFilePath(hostName, serviceName), true);
      if(hpdURLData == null)
      {
        Log.debugLog(className, "getScratchVarByURL", "", "", "Service File not found, It may be corrupted. Host=" + hostName + ", Service=" + serviceName);
        return new String[0][0];
      }

      for(int k = 0; k < hpdURLData.size(); k++)
      {
        String line = hpdURLData.elementAt(k).toString();
        String temp = line.trim();
        String defaultValue = "";

        if(temp.startsWith("nsl_decl_var")) // Method found
        {
          int indexForClose = line.lastIndexOf(")");
          if((indexForClose == -1) || (indexForClose != line.length() - 2))
            continue;

          String strParam = line.substring(line.indexOf("(") + 1, indexForClose);
          strParam = strParam.trim();

          if(strParam.equals(""))
            continue;
          strParam = rptUtilsBean.replace(strParam, "\\\"", "\"");
          String paramArr[] = rptUtilsBean.split(strParam, ",");
          String varName = paramArr[0].trim(); //parameter name
          boolean startDoubleQuote = false;
          if(paramArr.length > 1) //if length is  greater than one means default value is present
            for(int i = 1; i < paramArr.length; i++)
            {
              paramArr[i] = paramArr[i].trim();
              if(paramArr[i].toUpperCase().startsWith("DEFAULTVALUE"))
              {
                startDoubleQuote = true;
                String defaultValuetArr[] = paramArr[i].split("=");
                defaultValuetArr[1] = defaultValuetArr[1].trim();

                if(defaultValuetArr.length > 2)
                {
                  for(int j = 2; j < defaultValuetArr.length; j++)
                  {
                    defaultValuetArr[1] = defaultValuetArr[1] + "=" + defaultValuetArr[j];
                  }
                }

                defaultValue = defaultValuetArr[1]; //default value
              }
              else if(startDoubleQuote)
              {
                defaultValue = defaultValue + "," + paramArr[i].trim();
              }
              else
              {
                paramArr[i] = paramArr[i].trim();
                Log.debugLog(className, "getScratchVarByURL", "", "", "Invalid Parameter = " + paramArr[i]);
                continue;
              }
            }

          String[] arrRowValue = new String[2];
          arrRowValue[0] = varName;
          arrRowValue[1] = defaultValue;

          alScratchBar.add(arrRowValue);
        }
      }
      String[][] arrScratchVar = new String[alScratchBar.size()][2];

      for(int xy = 0; xy < alScratchBar.size(); xy++)
      {
        String[] strRowValue = (String[])alScratchBar.get(xy);
        for(int jj = 0; jj < strRowValue.length; jj++)
        {
          arrScratchVar[xy][jj] = strRowValue[jj];
        }
      }
      return arrScratchVar;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getScratchVarByURL", "", "", "Exception - ", ex);
      return new String[0][0];
    }
  }

  //Method to get Scratch array  var values for specified URL
  public String[][] getScratchArrayByURL(String hostName, String serviceName, String URL, int sortOnCol, int sortPrefrence)
  {
    Log.debugLog(className, "getScratchArrayByURL", "", "", "Method started");
    try
    {
      ArrayList alScratchArrayBar = new ArrayList();
      int scratchArrayCtr = 0;

      Vector hpdURLData = readFile(getServiceConfFilePath(hostName, serviceName), true);
      if(hpdURLData == null)
      {
        Log.debugLog(className, "getScratchArrayByURL", "", "", "Service File not found, It may be corrupted. Host=" + hostName + ", Service=" + serviceName);
        return new String[0][0];
      }
      int rowId = -1;

      for(int k = 0; k < hpdURLData.size(); k++)
      {
        String line = hpdURLData.elementAt(k).toString();
        String temp = line.trim();
        String defaultValue = "";
        String size = "";

        if(temp.startsWith("nsl_decl_array")) // Method found
        {

          int indexForClose = line.lastIndexOf(")");
          if((indexForClose == -1))
            continue;
          String strParam = line.substring(line.indexOf("(") + 1, indexForClose);
          strParam = strParam.trim();
          if(strParam.equals(""))
            continue;
          strParam = rptUtilsBean.replace(strParam, "\\\"", "\"");
          String paramArr[] = rptUtilsBean.split(strParam, ",");
          String varName = paramArr[0].trim(); //parameter name

          boolean startDoubleQuote = false;
          if(paramArr.length > 1) //if length is  greater than one means default value is present
            for(int i = 1; i < paramArr.length; i++)
            {
              paramArr[i] = paramArr[i].trim();
              if(paramArr[i].toUpperCase().startsWith("SIZE"))
              {
                String arrTemp[] = paramArr[i].split("=");
                if(arrTemp.length > 1)
                  size = arrTemp[1];
              }
              else if(paramArr[i].toUpperCase().startsWith("DEFAULTVALUE"))
              {
                startDoubleQuote = true;
                String defaultValuetArr[] = paramArr[i].split("=");
                if(defaultValuetArr.length > 1)
                {
                  defaultValuetArr[1] = defaultValuetArr[1].trim();

                  if(defaultValuetArr.length > 2)
                  {
                    for(int j = 2; j < defaultValuetArr.length; j++)
                    {
                      defaultValuetArr[1] = defaultValuetArr[1] + "=" + defaultValuetArr[j];
                    }
                  }

                  defaultValue = defaultValuetArr[1]; //default value
                }
              }
              else if(startDoubleQuote)
              {
                defaultValue = defaultValue + "," + paramArr[i].trim();
              }
              else
              {
                paramArr[i] = paramArr[i].trim();
                Log.debugLog(className, "getScratchArrayByURL", "", "", "Invalid Parameter = " + paramArr[i]);
                continue;
              }
            }
          String[] arrRowValue = new String[4];
          arrRowValue[0] = varName;
          arrRowValue[1] = size;
          arrRowValue[2] = defaultValue;
          ++rowId;
          arrRowValue[3] = String.valueOf(rowId);

          alScratchArrayBar.add(arrRowValue);
        }
      }
      String[][] arrScratchVar = new String[alScratchArrayBar.size()][4];

      for(int xy = 0; xy < alScratchArrayBar.size(); xy++)
      {
        String[] strRowValue = (String[])alScratchArrayBar.get(xy);
        for(int jj = 0; jj < strRowValue.length; jj++)
        {
          arrScratchVar[xy][jj] = strRowValue[jj];
        }
      }
      arrScratchVar = sortArray(arrScratchVar, sortOnCol, sortPrefrence, "STRING");

      return arrScratchVar;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getScratchArrayByURL", "", "", "Exception - ", ex);
      return new String[0][0];
    }
  }

  // Method to get query var data for specified URL
  public String[][] getQueryVarByURL(String hostName, String serviceName, String URL, int sortOnCol, int sortPrefrence)
  {
    Log.debugLog(className, "getQueryVarByURL", "", "", "Method started");
    try
    {
      ArrayList alQuery = new ArrayList();
      Vector hpdURLData = readFile(getServiceConfFilePath(hostName, serviceName), true);

      if((hpdURLData == null) || (hpdURLData.size() <= 0))
      {
        Log.debugLog(className, "getQueryVarByURL", "", "", "Service File not found, It may be corrupted. Host=" + hostName + ", Service=" + serviceName);
        String[][] dummy = new String[0][2];
        return dummy;
      }
      int rowId = -1;
      for(int k = 0; k < hpdURLData.size(); k++)
      {
        String[] arrURLLine = rptUtilsBean.strToArrayData(hpdURLData.elementAt(k).toString(), " ");
        if(arrURLLine.length <= 0)
          continue;
       
        //for supporting both old and new type of api 
        if(arrURLLine[0].equals("QUERY_VAR") || arrURLLine[0].equals("nsl_query_var"))
        {
          String strQuery = "";
          if(arrURLLine[0].equals("QUERY_VAR"))
          {
            for(int xx = 1; xx < arrURLLine.length; xx++)
            {
              if(xx == 1)
                strQuery = arrURLLine[xx];
              else
                strQuery = strQuery + " " + arrURLLine[xx];
            }
            alQuery.add(strQuery);
          }
          else
          {
            for(int xx = 1; xx < arrURLLine.length; xx++)
            {
              strQuery = strQuery + arrURLLine[xx];
            }
            Log.debugLog(className, "getQueryVarByURL", "" ,"", "The str query = " + strQuery);
            alQuery.add(strQuery);
          }
        }
      }
      String[][] arrQuery = new String[alQuery.size()][5];
      for(int xy = 0; xy < alQuery.size(); xy++)
      {
        //for old apis because old api does not contains ( and ); 
        if(!alQuery.get(xy).toString().trim().startsWith("(") && !alQuery.get(xy).toString().trim().endsWith(");"))
        {
          String[] tempArr = rptUtilsBean.strToArrayData(alQuery.get(xy).toString(), " ");
          // if(tempArr.length>0)
          arrQuery[xy][0] = tempArr[0];
          // if(tempArr.length>1)
          arrQuery[xy][1] = tempArr[1];
          arrQuery[xy][2] = "1";
          arrQuery[xy][3] = "None";
          ++rowId;
          arrQuery[xy][4] = String.valueOf(rowId);
        }
        else  //this is for new api
        {
          String line = alQuery.get(xy).toString().trim();
          int indexForClose = line.lastIndexOf(")");
          if((indexForClose == -1) || (indexForClose != line.length() - 2))
            continue;

          String strParam = line.substring(line.indexOf("(") + 1, indexForClose);
          String paramArr[] = strParam.split(",");  //because the values are , separated

          String varName = "";
          String queryName = "";
          String ordValue = "";
          String methodValue = "";
          varName = paramArr[0];
          for(int i = 1; i < paramArr.length; i++)
          {
            paramArr[i] = paramArr[i].trim();
            if(paramArr[i].toUpperCase().startsWith("QUERY")) //getting the query name
            {
              String queryArr[] = paramArr[i].split("=");
              queryName = queryArr[1];
            }
            else if(paramArr[i].toUpperCase().startsWith("ORD")) //getting the ORD value
            {
              String ordArr[] = paramArr[i].split("=");
              ordValue = ordArr[1].trim();
            }
            else if(paramArr[i].toUpperCase().startsWith("METHOD")) //getting the METHOD value
            {
              String methodArr[] = paramArr[i].split("=");
              methodValue = methodArr[1].trim();
            }
            else
            {
              paramArr[i] = paramArr[i].trim();
              Log.debugLog(className, "getQueryVarByURL", "", "", "Invalid Parameter = " + paramArr[i]);
              continue;
            }
          } //end of inner for
          
          arrQuery[xy][0] = varName;   //first index contains the parameter name
          arrQuery[xy][1] = queryName; //second index contains the queryName
          arrQuery[xy][2] = ordValue;  //third index contains the ord value
          arrQuery[xy][3] = methodValue; //fourth index contains the method value
          ++rowId;
          arrQuery[xy][4] = String.valueOf(rowId); //fifth index stored the rowId
        }  //end of else
      }
      arrQuery = sortArray(arrQuery, sortOnCol, sortPrefrence, "STRING");
      return arrQuery;
    }//end of outer for
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getQueryVarByURL", "", "", "Exception - ", ex);
      return null;
    }
  }

  // Method to get Service Time parameters by URL. It will return String Array which will contain minimum 1 and maximum 3 values.
  // If first value is 0 then don't need to search 2 and 3 values
  public String[] getServiceTimeByURL(String hostName, String serviceName, String URL)
  {
    Log.debugLog(className, "getSearchVarByURL", "", "", "Service File not found, It may be corrupted. Host=" + hostName + ", Service=" + serviceName);
    try
    {
      Vector hpdURLData = readFile(getServiceConfFilePath(hostName, serviceName), true);
      if(hpdURLData == null)
      {
        Log.debugLog(className, "getServiceTimeByURL", "", "", "Service File not found, It may be corrupted. Host=" + hostName + ", Service=" + serviceName);
        return null;
      }
      ArrayList alServiceTime = new ArrayList();
      alServiceTime.add("0");
      for(int k = 0; k < hpdURLData.size(); k++)
      {
        String[] arrURLLine = rptUtilsBean.strToArrayData(hpdURLData.elementAt(k).toString(), " ");
        if(arrURLLine.length <= 0)
          continue;
        if(arrURLLine[0].toUpperCase().equals("SVC_TIME"))
        {
          if(!arrURLLine[1].equals("0"))
          {
            alServiceTime.set(0, arrURLLine[1]);
            alServiceTime.add(arrURLLine[2]);
            if(arrURLLine.length > 3)
              alServiceTime.add(arrURLLine[3]);
          }
        }
      }
      String[] arrServiceTime = new String[alServiceTime.size()];
      for(int xy = 0; xy < alServiceTime.size(); xy++)
      {
        arrServiceTime[xy] = alServiceTime.get(xy).toString();
      }
      return arrServiceTime;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getServiceTimeByURL", "", "", "Exception - ", ex);
      return null;
    }
  }

  // Method to get file parameter data for specified URL
  public String[][] getFileParameterByURL(String hostName, String serviceName, String URL, int sortOnCol, int sortPrefrence)
  {
    Log.debugLog(className, "getFileParameterByURL", "", "", "Method started");
    try
    {
      ArrayList alSearchVar = new ArrayList();

      Vector hpdURLData = readFile(getServiceConfFilePath(hostName, serviceName), true);

      if((hpdURLData == null) || (hpdURLData.size() <= 0))
      {
        Log.debugLog(className, "getFileParameterByURL", "", "", "Service File not found, It may be corrupted. Host=" + hostName + ", Service=" + serviceName);
        String[][] dummy = new String[0][9];
        return dummy;
      }
      int rowId = -1;
      String[][] arrFileParameter = null;
      for(int k = 0; k < hpdURLData.size(); k++)
      {
        String line = hpdURLData.elementAt(k).toString();
        String temp = line.trim();

        if(temp.startsWith("nsl_static_var")) // Method found
        {
          Log.debugLog(className, "getFileParameterByURL", "", "", "found nsl_static_var block.");

          int indexForClose = line.lastIndexOf(")");
          if((indexForClose == -1) || (indexForClose != line.length() - 2))
            continue;

          String strParam = line.substring(line.indexOf("(") + 1, indexForClose);
          String paramArr[] = strParam.split(",");

          String varName = "";
          // ArrayList varValue = new ArrayList();
          String FILE = "";
          String REFRESH = "";
          String MODE = "";
          String varValue = "";
          String headerLine = "";
          String columnDeliminiator = ",";
          String firstDataLine = "1";

          try
          {
            Vector vecForSplitStr = new Vector();
            for(int i = 0; i < paramArr.length; i++)
            {
              paramArr[i] = paramArr[i].trim();
              if(paramArr[i].toUpperCase().startsWith("FILE"))
              {
                Log.debugLog(className, "getFileParameterByURL", "", "", "found FILE = " + paramArr[i]);
                String FILEArr[] = paramArr[i].split("=");
                FILEArr[1] = FILEArr[1].trim();

                if(FILEArr.length > 2)
                {
                  for(int j = 2; j < FILEArr.length; j++)
                  {
                    FILEArr[1] = FILEArr[1] + "=" + FILEArr[j];
                  }
                }

                // FILEArr[1] = FILEArr[1].substring(1, (FILEArr[1].length() - 1));
                FILE = FILEArr[1];
              }
              else if(paramArr[i].toUpperCase().startsWith("REFRESH"))
              {
                Log.debugLog(className, "getFileParameterByURL", "", "", "found REFRESH = " + paramArr[i]);
                String REFRESHArr[] = paramArr[i].split("=");
                REFRESHArr[1] = REFRESHArr[1].trim();

                if(REFRESHArr.length > 2)
                {
                  for(int j = 2; j < REFRESHArr.length; j++)
                  {
                    REFRESHArr[1] = REFRESHArr[1] + "=" + REFRESHArr[j];
                  }
                }

                // REFRESHArr[1] = REFRESHArr[1].substring(1, (REFRESHArr[1].length() - 1));
                REFRESH = REFRESHArr[1];
              }
              else if(paramArr[i].toUpperCase().startsWith("MODE"))
              {
                Log.debugLog(className, "getFileParameterByURL", "", "", "found MODE = " + paramArr[i]);
                String MODEArr[] = paramArr[i].split("=");
                MODEArr[1] = MODEArr[1].trim();

                if(MODEArr.length > 2)
                {
                  for(int j = 2; j < MODEArr.length; j++)
                  {
                    MODEArr[1] = MODEArr[1] + "=" + MODEArr[j];
                  }
                }

                // MODEArr[1] = MODEArr[1].substring(1, (MODEArr[1].length() - 1));
                MODE = MODEArr[1];
              }
              else if(paramArr[i].toUpperCase().startsWith("VAR_VALUE"))
              {
                Log.debugLog(className, "getFileParameterByURL", "", "", "found VAR_VALUE = " + paramArr[i]);
                String varValueArr[] = paramArr[i].split("=");
                varValueArr[1] = varValueArr[1].trim();

                if(varValueArr.length > 2)
                {
                  for(int j = 2; j < varValueArr.length; j++)
                  {
                    varValueArr[1] = varValueArr[1] + "=" + varValueArr[j];
                  }
                }

                // MODEArr[1] = MODEArr[1].substring(1, (MODEArr[1].length() - 1));
                varValue = varValueArr[1];
              }
              else if(paramArr[i].toUpperCase().startsWith("HEADERLINE"))
              {
                String headerLineArr[] = paramArr[i].split("=");
                headerLineArr[1] = headerLineArr[1].trim();

                if(headerLineArr.length > 2)
                {
                  for(int j = 2; j < headerLineArr.length; j++)
                  {
                    headerLineArr[1] = headerLineArr[1] + "=" + headerLineArr[j];
                  }
                }
                // MODEArr[1] = MODEArr[1].substring(1, (MODEArr[1].length() - 1));
                headerLine = headerLineArr[1];
              }
              else if(paramArr[i].toUpperCase().startsWith("COLUMNDELIMITER"))
              {
                String columnDelimiterArr[] = paramArr[i].split("=");
                //String columnDelimiter = "";
                if(columnDelimiterArr.length < 2 || columnDelimiterArr[1] == null)
                {
                  columnDeliminiator = ",";
                  i++;
                }
                else
                  columnDeliminiator = columnDelimiterArr[1].trim();

                if(columnDelimiterArr.length > 2)
                {
                  for(int j = 2; j < columnDelimiterArr.length; j++)
                  {
                    columnDeliminiator = columnDeliminiator + "=" + columnDelimiterArr[j];
                  }
                }
              }
              else if(paramArr[i].toUpperCase().startsWith("FIRSTDATALINE"))
              {
                String firstDataLineArr[] = paramArr[i].split("=");
                firstDataLineArr[1] = firstDataLineArr[1].trim();

                if(firstDataLineArr.length > 2)
                {
                  for(int j = 2; j < firstDataLineArr.length; j++)
                  {
                    firstDataLineArr[1] = firstDataLineArr[1] + "=" + firstDataLineArr[j];
                  }
                }

                // MODEArr[1] = MODEArr[1].substring(1, (MODEArr[1].length() - 1));
                firstDataLine = firstDataLineArr[1];
              }
              else
              {
                Log.debugLog(className, "getFileParameterByURL", "", "", "found variable = " + paramArr[i]);
                paramArr[i] = paramArr[i].trim();
                if(!vecForSplitStr.contains(paramArr[i]))
                {
                  if(varName.equals(""))
                    varName = paramArr[i];
                  else
                    varName = varName + "," + paramArr[i];
                }
              }
            }
          }
          catch(Exception e)
          {
            Log.stackTraceLog(className, "getFileParameterByURL", "", "", "Exception - ", e);
            continue;
          }

          if(varName.equals(""))
            continue;

          String[] arrRowValue = new String[9];
          arrRowValue[0] = varName;
          arrRowValue[1] = FileBean.escapeHTML(FILE);
          arrRowValue[2] = FileBean.escapeHTML(REFRESH);
          arrRowValue[3] = FileBean.escapeHTML(MODE);
          arrRowValue[4] = FileBean.escapeHTML(varValue);
          arrRowValue[5] = firstDataLine;
          arrRowValue[6] = columnDeliminiator;
          arrRowValue[7] = headerLine;
          ++rowId;
          arrRowValue[8] = String.valueOf(rowId);
          alSearchVar.add(arrRowValue);
        }

        Log.debugLog(className, "getFileParameterByURL", "", "", "alSearchVar.size() = " + alSearchVar.size());

        arrFileParameter = new String[alSearchVar.size()][9];
        for(int ii = 0; ii < alSearchVar.size(); ii++)
        {
          String[] strRowValue = (String[])alSearchVar.get(ii);
          for(int jj = 0; jj < strRowValue.length; jj++)
          {
            arrFileParameter[ii][jj] = strRowValue[jj];
            Log.debugLog(className, "getFileParameterByURL", "", "", "arrFileParameter[ii][jj] = " + arrFileParameter[ii][jj]);
          }
        }
      }
      arrFileParameter = sortArray(arrFileParameter, sortOnCol, sortPrefrence, "STRING");
      return arrFileParameter;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getFileParameterByURL", "", "", "Exception - ", ex);
      String[][] dummy = new String[0][9];
      return dummy;
    }
  }

  public String getReqRespFileContents(String fileName)
  {
    return getReqRespFileContents(fileName, true);
  }

  /*Method to get file Contents for request or response from hpd logs.
   * parameter.File name which content need to get '/'
   * if boolean isHeaderWithBody is true then read the header otherwise read only body of request and response
   *
   * Case 1: if request and response file is not hessian then this method read the given file and return the file contents
   * Case 2: if request and response file is hessian and isHeaderWithBody is true then method check the content type if content type is hessian then read
   *         header from given file and body from xml file.
   * Case 3: if request and response file is hessian and isHeaderWithBody is false then method check the content type if content type is hessian then read
   *         only body from xml file.
   * Case 4: if request and response file is hessian and isHeaderWithBody is true then method check the content type if content type is hessian then read
   *         only body from xml file, if xml file not exists then read header.
   * Case 5: if request and response file is hessian and isHeaderWithBody is false then method check the content type if content type is hessian then read
   *         only body from xml file, if xml file not exists then read given file.
   */
  public String getReqRespFileContents(String fileName, boolean isHeaderWithBody)
  {
    Log.debugLog(className, "getReqRespFileContents", "", "", "Method started");
    String fileContents = "";
    try
    {
      String fileWithPath = "";
      if(!fileName.trim().startsWith(getHPDPath() + "logs/"))
        fileWithPath = getHPDPath() + "logs/" + fileName;
      else
        fileWithPath = fileName;

      File dataFile = new File(fileWithPath);

      if(!dataFile.exists())
      {
        Log.errorLog(className, "getReqRespFileContents", "", "", "File not found, filename - " + fileWithPath);
        return fileContents;
      }

      String strLine;
      FileInputStream fis = new FileInputStream(fileWithPath);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));

      while((strLine = br.readLine()) != null)
      {
        //strLine.replaceAll("\\n", "\\\\n");
        if(fileContents.equals(""))
          fileContents = strLine;
        else
          fileContents = fileContents + "\\n" + strLine;
      }

      br.close();
      fis.close();

      // if request and response is hessian then read header from given file and the body from xml file.
      if(fileContents.contains("x-application/hessian") || fileContents.contains("application/x-hessian"))
      {
        Log.debugLog(className, "getReqRespFileContents", "", "", "Content type - hessian. fileContents = " + fileContents);
        String hessianContents = "";
        fileWithPath = fileWithPath.substring(0, fileWithPath.lastIndexOf(".dat"));
        fileWithPath = fileWithPath + ".hessian.xml";

        Log.debugLog(className, "getReqRespFileContents", "", "", "Hessian xml file name. fileWithPath = " + fileWithPath);
        dataFile = new File(fileWithPath);

        if(!dataFile.exists())
        {
          Log.debugLog(className, "getReqRespFileContents", "", "", "File not found, filename - " + fileWithPath);
          if(isHeaderWithBody)
          {
            int subIndex = fileContents.indexOf("\\n\\n");

            Log.debugLog(className, "getReqRespFileContents", "", "", "header part does not exist. subIndex = " + subIndex);

            if(subIndex > -1)
              fileContents = fileContents.substring(0, subIndex);
          }

          return fileContents;
        }

        fis = new FileInputStream(fileWithPath);
        br = new BufferedReader(new InputStreamReader(fis));

        while((strLine = br.readLine()) != null)
        {
          if(hessianContents.equals(""))
            hessianContents = strLine;
          else
            hessianContents = hessianContents + "\\n" + strLine;
        }

        br.close();
        fis.close();

        if(isHeaderWithBody)
        {
          int subIndex = fileContents.indexOf("\\n\\n");

          Log.debugLog(className, "getReqRespFileContents", "", "", "header part does not exist. isHeaderWithBody " + isHeaderWithBody + ", subIndex = " + subIndex);

          if(subIndex > -1)
          {
            fileContents = fileContents.substring(0, subIndex);
            fileContents = fileContents + "\\n\\n" + hessianContents;
          }

        }
        else
          fileContents = hessianContents;
      }
      return fileContents;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getReqRespFileContents", "", "", "Exception - ", ex);
      return fileContents;
    }
  }

  /*Method to get file Contents. This method is created the file contents for FILE in File parameter and Index file
   * parameter.
   * File name should be absolute means should start with '/'
   */
  public String getFileContents(String hostName, String serviceName, String URL, String fileName)
  {
    Log.debugLog(className, "getFileContents", "", "", "Method started");
    String fileContents = "";
    try
    {
      String fileWithPath = fileName;
      if(!fileWithPath.startsWith("/"))
      {
        if(!serviceName.equals(""))
          fileWithPath = getServicePath(hostName, serviceName) + fileName;
        else
          fileWithPath = getCorrelationPath() + "data/" + fileName;
      }

      File dataFile = new File(fileWithPath);

      if(!dataFile.exists())
      {
        Log.errorLog(className, "getFileContents", "", "", "File not found, filename - " + fileWithPath);
        return fileContents;
      }
      String strLine;

      FileInputStream fis = new FileInputStream(fileWithPath);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));

      while((strLine = br.readLine()) != null)
      {
        //strLine.replaceAll("\\n", "\\\\n");
        if(fileContents.equals(""))
          fileContents = strLine;
        else
          fileContents = fileContents + "\\n" + strLine;
      }

      br.close();
      fis.close();

      return fileContents;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getFileContents", "", "", "Exception - ", ex);
      return fileContents;
    }
  }

  public static Vector readXLSFile(String filepath, int sheetID)
  {
    Vector cellVectorHolder = new Vector();
    try
    {
      File file = new File(filepath);
      if(!file.exists())
      {
        return cellVectorHolder;
      }

      return cellVectorHolder;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return cellVectorHolder;
    }
  }

  public String printCellDataToConsoleForHLXS(Vector dataHolder, String delimeter)
  {
    if(delimeter.equals(""))
      delimeter = ",";

  
    return null;
  }

  // Method to get Index file parameter data for specified URL
  public String[][] getIndexFileParameterByURL(String hostName, String serviceName, String URL, int sortOnCol, int sortPrefrence)
  {
    Log.debugLog(className, "getIndexFileParameterByURL", "", "", "Method started");
    try
    {
      ArrayList alIndexFileParameter = new ArrayList();

      Vector hpdURLData = readFile(getServiceConfFilePath(hostName, serviceName), true);

      if((hpdURLData == null) || (hpdURLData.size() <= 0))
      {
        Log.debugLog(className, "getIndexFileParameterByURL", "", "", "Service File not found, It may be corrupted. Host=" + hostName + ", Service=" + serviceName);
        String[][] dummy = new String[0][8];
        return dummy;
      }

      int rowId = -1;
      String[][] arrIndexFileParameter = null;
      for(int k = 0; k < hpdURLData.size(); k++)
      {
        String line = hpdURLData.elementAt(k).toString();
        String temp = line.trim();

        if(temp.startsWith("nsl_index_file_var")) // Method found
        {
          int indexForClose = line.lastIndexOf(")");
          if((indexForClose == -1) || (indexForClose != line.length() - 2))
            continue;

          String strParam = line.substring(line.indexOf("(") + 1, indexForClose);
          String paramArr[] = strParam.split(",");

          String varName = "";
          // ArrayList varValue = new ArrayList();
          String FILE = "";
          String IndexVar = "";
          // String REFRESH = "";
          // String mode = "";
          String varValue = "";
          String headerLine = "";
          String columnDeliminiator = ",";
          String firstDataLine = "1";
          try
          {
            Vector vecForSplitStr = new Vector();
            for(int i = 0; i < paramArr.length; i++)
            {
              paramArr[i] = paramArr[i].trim();
              if(paramArr[i].toUpperCase().startsWith("FILE"))
              {
                String FILEArr[] = paramArr[i].split("=");
                FILEArr[1] = FILEArr[1].trim();

                if(FILEArr.length > 2)
                {
                  for(int j = 2; j < FILEArr.length; j++)
                  {
                    FILEArr[1] = FILEArr[1] + "=" + FILEArr[j];
                  }
                }

                // FILEArr[1] = FILEArr[1].substring(1, (FILEArr[1].length() - 1));
                FILE = FILEArr[1];
              }
              /*
               * else if (paramArr[i].toUpperCase().startsWith("REFRESH")) { String REFRESHArr[] = paramArr[i].split("="); REFRESHArr[1] = REFRESHArr[1].trim();
               *
               * if(REFRESHArr.length > 2) { for(int j = 2 ; j < REFRESHArr.length ; j++) { REFRESHArr[1] = REFRESHArr[1] + "=" + REFRESHArr[j]; } }
               *
               * //REFRESHArr[1] = REFRESHArr[1].substring(1, (REFRESHArr[1].length() - 1)); REFRESH = REFRESHArr[1]; }
               */
              else if(paramArr[i].toUpperCase().startsWith("INDEXVAR"))
              {
                String indexVarArr[] = paramArr[i].split("=");
                indexVarArr[1] = indexVarArr[1].trim();

                if(indexVarArr.length > 2)
                {
                  for(int j = 2; j < indexVarArr.length; j++)
                  {
                    indexVarArr[1] = indexVarArr[1] + "=" + indexVarArr[j];
                  }
                }

                // indexVarArr[1] = indexVarArr[1].substring(1, (indexVarArr[1].length() - 1));
                IndexVar = indexVarArr[1];
              }
              /*
               * else if (paramArr[i].toUpperCase().startsWith("MODE")) { String MODEArr[] = paramArr[i].split("="); MODEArr[1] = MODEArr[1].trim();
               *
               * if(MODEArr.length > 2) { for(int j = 2 ; j < MODEArr.length ; j++) { MODEArr[1] = MODEArr[1] + "=" + MODEArr[j]; } }
               *
               * //MODEArr[1] = MODEArr[1].substring(1, (MODEArr[1].length() - 1)); mode = MODEArr[1]; }
               */
              else if(paramArr[i].toUpperCase().startsWith("VAR_VALUE"))
              {
                String varValueArr[] = paramArr[i].split("=");
                varValueArr[1] = varValueArr[1].trim();

                if(varValueArr.length > 2)
                {
                  for(int j = 2; j < varValueArr.length; j++)
                  {
                    varValueArr[1] = varValueArr[1] + "=" + varValueArr[j];
                  }
                }
                // MODEArr[1] = MODEArr[1].substring(1, (MODEArr[1].length() - 1));
                varValue = varValueArr[1];
              }
              else if(paramArr[i].toUpperCase().startsWith("HEADERLINE"))
              {
                String headerLineArr[] = paramArr[i].split("=");
                headerLineArr[1] = headerLineArr[1].trim();

                if(headerLineArr.length > 2)
                {
                  for(int j = 2; j < headerLineArr.length; j++)
                  {
                    headerLineArr[1] = headerLineArr[1] + "=" + headerLineArr[j];
                  }
                }
                // MODEArr[1] = MODEArr[1].substring(1, (MODEArr[1].length() - 1));
                headerLine = headerLineArr[1];
              }
              else if(paramArr[i].toUpperCase().startsWith("COLUMNDELIMITER"))
              {
                String columnDelimiterArr[] = paramArr[i].split("=");
                //String columnDelimiter = "";
                if(columnDelimiterArr.length < 2 || columnDelimiterArr[1] == null)
                {
                  columnDeliminiator = ",";
                  i++;
                }
                else
                  columnDeliminiator = columnDelimiterArr[1].trim();

                if(columnDelimiterArr.length > 2)
                {
                  for(int j = 2; j < columnDelimiterArr.length; j++)
                  {
                    columnDeliminiator = columnDeliminiator + "=" + columnDelimiterArr[j];
                  }
                }

                // MODEArr[1] = MODEArr[1].substring(1, (MODEArr[1].length() - 1));
                //columnDeliminiator = columnDelimiterArr[1];
              }
              else if(paramArr[i].toUpperCase().startsWith("FIRSTDATALINE"))
              {
                String firstDataLineArr[] = paramArr[i].split("=");
                firstDataLineArr[1] = firstDataLineArr[1].trim();

                if(firstDataLineArr.length > 2)
                {
                  for(int j = 2; j < firstDataLineArr.length; j++)
                  {
                    firstDataLineArr[1] = firstDataLineArr[1] + "=" + firstDataLineArr[j];
                  }
                }
                firstDataLine = firstDataLineArr[1];
              }
              else
              {
                paramArr[i] = paramArr[i].trim();
                if(!vecForSplitStr.contains(paramArr[i]))
                {
                  if(varName.equals(""))
                    varName = paramArr[i];
                  else
                    varName = varName + "," + paramArr[i];
                }
              }
            }
          }
          catch(Exception e)
          {
            continue;
          }

          if(varName.equals(""))
            continue;

          String[] arrRowValue = new String[8];
          arrRowValue[0] = varName;
          arrRowValue[1] = FileBean.escapeHTML(FILE);
          arrRowValue[2] = FileBean.escapeHTML(IndexVar);
          // arrRowValue[3] = FileBean.escapeHTML(REFRESH);
          // arrRowValue[4] = FileBean.escapeHTML(mode);
          arrRowValue[3] = FileBean.escapeHTML(varValue);
          arrRowValue[4] = firstDataLine;
          arrRowValue[5] = columnDeliminiator;
          arrRowValue[6] = headerLine;
          ++rowId;
          arrRowValue[7] = String.valueOf(rowId);
          alIndexFileParameter.add(arrRowValue);
        }
        arrIndexFileParameter = new String[alIndexFileParameter.size()][8];
        for(int ii = 0; ii < alIndexFileParameter.size(); ii++)
        {
          String[] strRowValue = (String[])alIndexFileParameter.get(ii);
          for(int jj = 0; jj < strRowValue.length; jj++)
          {
            arrIndexFileParameter[ii][jj] = strRowValue[jj];
          }
        }
      }
      arrIndexFileParameter = sortArray(arrIndexFileParameter, sortOnCol, sortPrefrence, "STRING");
      return arrIndexFileParameter;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getIndexFileParameterByURL", "", "", "Exception - ", ex);
      String[][] dummy = new String[0][8];
      return dummy;
    }
  }

  // Method to get DateTime parameter data for specified URL
  public String[][] getDateTimeParameterByURL(String hostName, String serviceName, String URL, int sortOnCol, int sortPrefrence)
  {
    Log.debugLog(className, "getDateTimeParameterByURL", "", "", "Method started");
    try
    {
      ArrayList alDateTimeParameter = new ArrayList();

      Vector hpdURLData = readFile(getServiceConfFilePath(hostName, serviceName), true);

      if((hpdURLData == null) || (hpdURLData.size() <= 0))
      {
        Log.debugLog(className, "getDateTimeParameterByURL", "", "", "Service File not found, It may be corrupted. Host=" + hostName + ", Service=" + serviceName);
        String[][] dummy = new String[0][5];
        return dummy;
      }

      int rowId = -1;
      String[][] arrDateTimeParameter = null;
      for(int k = 0; k < hpdURLData.size(); k++)
      {
        String line = hpdURLData.elementAt(k).toString();
        String temp = line.trim();

        if(temp.startsWith("nsl_date_var")) // Method found
        {
          int indexForClose = line.lastIndexOf(")");
          if((indexForClose == -1) || (indexForClose != line.length() - 2))
            continue;

          String strParam = line.substring(line.indexOf("(") + 1, indexForClose);
          String paramArr[] = strParam.split(",");

          String varName = "";
          // ArrayList varValue = new ArrayList();
          String format = "";
          String offset = "";
          String days = "";
          String refresh = "";

          try
          {
            Vector vecForSplitStr = new Vector();

            varName = paramArr[0];

            for(int i = 1; i < paramArr.length; i++)
            {
              paramArr[i] = paramArr[i].trim();

              Log.debugLog(className, "getDateTimeParameterByURL", "", "", "paramArr[" + i + "] = " + paramArr[i] + ", Uppercase = " + paramArr[i].toUpperCase());

              if(paramArr[i].toUpperCase().startsWith("FORMAT"))
              {
                String formatArr[] = paramArr[i].split("=");
                formatArr[1] = formatArr[1].trim();

                if(formatArr.length > 2)
                {
                  for(int j = 2; j < formatArr.length; j++)
                  {
                    formatArr[1] = formatArr[1] + "=" + formatArr[j];
                  }
                }

                if(!formatArr[1].startsWith("\""))
                  continue;

                // formatArr[1] = formatArr[1].substring(1, (formatArr[1].length() - 1));
                format = formatArr[1];
              }
              else if(paramArr[i].toUpperCase().startsWith("OFFSET"))
              {
                String offsetArr[] = paramArr[i].split("=");
                offsetArr[1] = offsetArr[1].trim();

                if(offsetArr.length > 2)
                {
                  for(int j = 2; j < offsetArr.length; j++)
                  {
                    offsetArr[1] = offsetArr[1] + "=" + offsetArr[j];
                  }
                }

                // offsetArr[1] = offsetArr[1].substring(1, (offsetArr[1].length() - 1));
                offset = offsetArr[1];
              }
              else if(paramArr[i].toUpperCase().startsWith("REFRESH"))
              {
                String refreshArr[] = paramArr[i].split("=");
                refreshArr[1] = refreshArr[1].trim();

                if(refreshArr.length > 2)
                {
                  for(int j = 2; j < refreshArr.length; j++)
                  {
                    refreshArr[1] = refreshArr[1] + "=" + refreshArr[j];
                  }
                }

                // refreshArr[1] = refreshArr[1].substring(1, (refreshArr[1].length() - 1));
                refresh = refreshArr[1];
              }
              else
              {
                paramArr[i] = paramArr[i].trim();
                Log.debugLog(className, "getDateTimeParameterByURL", "", "", "Invalid Parameter = " + paramArr[i]);
                continue;
              }
            }
          }
          catch(Exception e)
          {
            continue;
          }

          if(varName.equals(""))
            continue;

          String[] arrRowValue = new String[5];
          arrRowValue[0] = varName;
          arrRowValue[1] = FileBean.escapeHTML(format);
          arrRowValue[2] = FileBean.escapeHTML(offset);
          //arrRowValue[3] = FileBean.escapeHTML(days);
          arrRowValue[3] = FileBean.escapeHTML(refresh);
          ++rowId;
          arrRowValue[4] = String.valueOf(rowId);
          alDateTimeParameter.add(arrRowValue);
        }
        arrDateTimeParameter = new String[alDateTimeParameter.size()][5];
        for(int ii = 0; ii < alDateTimeParameter.size(); ii++)
        {
          String[] strRowValue = (String[])alDateTimeParameter.get(ii);
          for(int jj = 0; jj < strRowValue.length; jj++)
          {
            arrDateTimeParameter[ii][jj] = strRowValue[jj];
          }
        }
      }
      arrDateTimeParameter = sortArray(arrDateTimeParameter, sortOnCol, sortPrefrence, "STRING");
      return arrDateTimeParameter;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getDateTimeParameterByURL", "", "", "Exception - ", ex);
      String[][] dummy = new String[0][5];
      return dummy;
    }
  }

  // Method to get RANDOM Number for specified URL
  public String[][] getRandomNumberByURL(String hostName, String serviceName, String URL, int sortOnCol, int sortPrefrence)
  {
    Log.debugLog(className, "getRandomNumberByURL", "", "", "Method started");
    try
    {
      ArrayList alRandomNumber = new ArrayList();

      Vector hpdURLData = readFile(getServiceConfFilePath(hostName, serviceName), true);

      if((hpdURLData == null) || (hpdURLData.size() <= 0))
      {
        Log.debugLog(className, "getRandomNumberByURL", "", "", "Service File not found, It may be corrupted. Host=" + hostName + ", Service=" + serviceName);
        String[][] dummy = new String[0][6];
        return dummy;
      }

      int rowId = -1;
      String[][] arrRandomNumber = null;
      for(int k = 0; k < hpdURLData.size(); k++)
      {
        String line = hpdURLData.elementAt(k).toString();
        String temp = line.trim();

        if(temp.startsWith("nsl_random_number_var")) // Method found
        {
          int indexForClose = line.lastIndexOf(")");
          if((indexForClose == -1) || (indexForClose != line.length() - 2))
            continue;

          String strParam = line.substring(line.indexOf("(") + 1, indexForClose);
          String paramArr[] = strParam.split(",");

          String varName = "";
          // ArrayList varValue = new ArrayList();
          String min = "";
          String max = "";
          String format = "";
          String refresh = "";

          try
          {
            Vector vecForSplitStr = new Vector();
            varName = paramArr[0];

            for(int i = 1; i < paramArr.length; i++)
            {
              paramArr[i] = paramArr[i].trim();
              if(paramArr[i].toUpperCase().startsWith("MIN"))
              {
                String minArr[] = paramArr[i].split("=");
                minArr[1] = minArr[1].trim();

                if(minArr.length > 2)
                {
                  for(int j = 2; j < minArr.length; j++)
                  {
                    minArr[1] = minArr[1] + "=" + minArr[j];
                  }
                }

                // minArr[1] = minArr[1].substring(1, (minArr[1].length() - 1));
                min = minArr[1];
              }
              else if(paramArr[i].toUpperCase().startsWith("MAX"))
              {
                String maxArr[] = paramArr[i].split("=");
                maxArr[1] = maxArr[1].trim();

                if(maxArr.length > 2)
                {
                  for(int j = 2; j < maxArr.length; j++)
                  {
                    maxArr[1] = maxArr[1] + "=" + maxArr[j];
                  }
                }

                // maxArr[1] = maxArr[1].substring(1, (maxArr[1].length() - 1));
                max = maxArr[1];
              }
              else if(paramArr[i].toUpperCase().startsWith("FORMAT"))
              {
                String formatArr[] = paramArr[i].split("=");
                formatArr[1] = formatArr[1].trim();

                if(formatArr.length > 2)
                {
                  for(int j = 2; j < formatArr.length; j++)
                  {
                    formatArr[1] = formatArr[1] + "=" + formatArr[j];
                  }
                }

                // formatArr[1] = formatArr[1].substring(1, (formatArr[1].length() - 1));
                format = formatArr[1];
              }
              else if(paramArr[i].toUpperCase().startsWith("REFRESH"))
              {
                String refreshArr[] = paramArr[i].split("=");
                refreshArr[1] = refreshArr[1].trim();

                if(refreshArr.length > 2)
                {
                  for(int j = 2; j < refreshArr.length; j++)
                  {
                    refreshArr[1] = refreshArr[1] + "=" + refreshArr[j];
                  }
                }

                // refreshArr[1] = refreshArr[1].substring(1, (refreshArr[1].length() - 1));
                refresh = refreshArr[1];
              }
              else
              {
                paramArr[i] = paramArr[i].trim();
                Log.debugLog(className, "getRandomNumberByURL", "", "", "Invalid Parameter = " + paramArr[i]);
                continue;
              }
            }
          }
          catch(Exception e)
          {
            continue;
          }

          if(varName.equals(""))
            continue;

          String[] arrRowValue = new String[6];
          arrRowValue[0] = varName;
          arrRowValue[1] = FileBean.escapeHTML(min);
          arrRowValue[2] = FileBean.escapeHTML(max);
          arrRowValue[3] = FileBean.escapeHTML(format);
          arrRowValue[4] = FileBean.escapeHTML(refresh);
          ++rowId;
          arrRowValue[5] = String.valueOf(rowId);
          alRandomNumber.add(arrRowValue);
        }
        arrRandomNumber = new String[alRandomNumber.size()][6];
        for(int ii = 0; ii < alRandomNumber.size(); ii++)
        {
          String[] strRowValue = (String[])alRandomNumber.get(ii);
          for(int jj = 0; jj < strRowValue.length; jj++)
          {
            arrRandomNumber[ii][jj] = strRowValue[jj];
          }
        }
      }
      arrRandomNumber = sortArray(arrRandomNumber, sortOnCol, sortPrefrence, "STRING");
      return arrRandomNumber;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getRandomNumberByURL", "", "", "Exception - ", ex);
      String[][] dummy = new String[0][6];
      return dummy;
    }
  }

  // Method to get RANDOM String between X to Y Characters for specified URL
  public String[][] getRandomStringByURL(String hostName, String serviceName, String URL, int sortOnCol, int sortPrefrence)
  {
    Log.debugLog(className, "getRandomStringByURL", "", "", "Method started");
    try
    {
      ArrayList alRandomString = new ArrayList();

      Vector hpdURLData = readFile(getServiceConfFilePath(hostName, serviceName), true);

      if((hpdURLData == null) || (hpdURLData.size() <= 0))
      {
        Log.debugLog(className, "getRandomStringByURL", "", "", "Service File not found, It may be corrupted. Host=" + hostName + ", Service=" + serviceName);
        String[][] dummy = new String[0][7];
        return dummy;
      }

      int rowId = -1;
      String[][] arrRandomString = null;
      for(int k = 0; k < hpdURLData.size(); k++)
      {
        String line = hpdURLData.elementAt(k).toString();
        String temp = line.trim();

        if(temp.startsWith("nsl_random_string_var")) // Method found
        {
          int indexForClose = line.lastIndexOf(")");
          if((indexForClose == -1) || (indexForClose != line.length() - 2))
            continue;

          String strParam = line.substring(line.indexOf("(") + 1, indexForClose);
          String paramArr[] = strParam.split(",");

          String varName = "";
          // ArrayList varValue = new ArrayList();
          String min = "";
          String max = "";
          String charSet = "";
          String refresh = "";
          String enable_encoding = "YES";
          try
          {
            Vector vecForSplitStr = new Vector();
            varName = paramArr[0];
            for(int i = 1; i < paramArr.length; i++)
            {
              paramArr[i] = paramArr[i].trim();
              if(paramArr[i].toUpperCase().startsWith("MIN"))
              {
                String minArr[] = paramArr[i].split("=");
                minArr[1] = minArr[1].trim();

                if(minArr.length > 2)
                {
                  for(int j = 2; j < minArr.length; j++)
                  {
                    minArr[1] = minArr[1] + "=" + minArr[j];
                  }
                }

                // minArr[1] = minArr[1].substring(1, (minArr[1].length() - 1));
                min = minArr[1];
              }
              else if(paramArr[i].toUpperCase().startsWith("MAX"))
              {
                String maxArr[] = paramArr[i].split("=");
                maxArr[1] = maxArr[1].trim();

                if(maxArr.length > 2)
                {
                  for(int j = 2; j < maxArr.length; j++)
                  {
                    maxArr[1] = maxArr[1] + "=" + maxArr[j];
                  }
                }

                // maxArr[1] = maxArr[1].substring(1, (maxArr[1].length() - 1));
                max = maxArr[1];
              }
              else if(paramArr[i].toUpperCase().startsWith("CHARSET"))
              {
                String charSetArr[] = paramArr[i].split("=");
                charSetArr[1] = charSetArr[1].trim();

                if(charSetArr.length > 2)
                {
                  for(int j = 2; j < charSetArr.length; j++)
                  {
                    charSetArr[1] = charSetArr[1] + "=" + charSetArr[j];
                  }
                }

                // charSetArr[1] = charSetArr[1].substring(1, (charSetArr[1].length() - 1));
                charSet = charSetArr[1];
              }
              else if(paramArr[i].toUpperCase().startsWith("REFRESH"))
              {
                String refreshArr[] = paramArr[i].split("=");
                refreshArr[1] = refreshArr[1].trim();

                if(refreshArr.length > 2)
                {
                  for(int j = 2; j < refreshArr.length; j++)
                  {
                    refreshArr[1] = refreshArr[1] + "=" + refreshArr[j];
                  }
                }

                // refreshArr[1] = refreshArr[1].substring(1, (refreshArr[1].length() - 1));
                refresh = refreshArr[1];
              }
              else if(paramArr[i].toUpperCase().startsWith("ENABLE_ENCODING"))
              {
                String encodeArr[] = paramArr[i].split("=");
                encodeArr[1] = encodeArr[1].trim();

                if(encodeArr.length > 2)
                {
                  for(int j = 2; j < encodeArr.length; j++)
                  {
                    encodeArr[1] = encodeArr[1] + "=" + encodeArr[j];
                  }
                }

                // refreshArr[1] = refreshArr[1].substring(1, (refreshArr[1].length() - 1));
                enable_encoding = encodeArr[1];
              }
              else
              {
                paramArr[i] = paramArr[i].trim();
                Log.debugLog(className, "getRandomStringByURL", "", "", "Invalid Parameter = " + paramArr[i]);
                continue;
              }
            }
          }
          catch(Exception e)
          {
            continue;
          }

          if(varName.equals(""))
            continue;

          String[] arrRowValue = new String[7];
          arrRowValue[0] = varName;
          arrRowValue[1] = FileBean.escapeHTML(min);
          arrRowValue[2] = FileBean.escapeHTML(max);
          arrRowValue[3] = FileBean.escapeHTML(charSet);
          arrRowValue[4] = FileBean.escapeHTML(refresh);
          ++rowId;
          arrRowValue[5] = String.valueOf(rowId);
          arrRowValue[6] = enable_encoding;
          alRandomString.add(arrRowValue);
        }
        arrRandomString = new String[alRandomString.size()][7];
        for(int ii = 0; ii < alRandomString.size(); ii++)
        {
          String[] strRowValue = (String[])alRandomString.get(ii);
          for(int jj = 0; jj < strRowValue.length; jj++)
          {
            arrRandomString[ii][jj] = strRowValue[jj];
          }
        }
      }
      arrRandomString = sortArray(arrRandomString, sortOnCol, sortPrefrence, "STRING");
      return arrRandomString;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getRandomStringByURL", "", "", "Exception - ", ex);
      String[][] dummy = new String[0][7];
      return dummy;
    }
  }

  // Method to get Unique Number for specified URL
  public String[][] getUniqueNumberVUserByURL(String hostName, String serviceName, String URL, int sortOnCol, int sortPrefrence)
  {
    Log.debugLog(className, "getUniqueNumberVUserByURL", "", "", "Method started");
    try
    {
      ArrayList alUniqueNumber = new ArrayList();

      Vector hpdURLData = readFile(getServiceConfFilePath(hostName, serviceName), true);

      if((hpdURLData == null) || (hpdURLData.size() <= 0))
      {
        Log.debugLog(className, "getUniqueNumberVUserByURL", "", "", "Service File not found, It may be corrupted. Host=" + hostName + ", Service=" + serviceName);
        String[][] dummy = new String[0][6];
        return dummy;
      }

      String[][] arrUniqueNumber = null;
      for(int k = 0; k < hpdURLData.size(); k++)
      {
        String line = hpdURLData.elementAt(k).toString();
        String temp = line.trim();

        if(temp.startsWith("nsl_unique_number_vuser_var")) // Method found
        {
          int indexForClose = line.lastIndexOf(")");
          if((indexForClose == -1) || (indexForClose != line.length() - 2))
            continue;

          String strParam = line.substring(line.indexOf("(") + 1, indexForClose);
          String paramArr[] = strParam.split(",");

          String varName = "";
          // ArrayList varValue = new ArrayList();
          String start = "";
          String end = "";
          String format = "";
          String refresh = "";
          String outOfValue = "";

          try
          {
            Vector vecForSplitStr = new Vector();
            varName = paramArr[0];
            for(int i = 1; i < paramArr.length; i++)
            {
              paramArr[i] = paramArr[i].trim();
              if(paramArr[i].toUpperCase().startsWith("START"))
              {
                String startArr[] = paramArr[i].split("=");
                startArr[1] = startArr[1].trim();

                if(startArr.length > 2)
                {
                  for(int j = 2; j < startArr.length; j++)
                  {
                    startArr[1] = startArr[1] + "=" + startArr[j];
                  }
                }

                // startArr[1] = startArr[1].substring(1, (startArr[1].length() - 1));
                start = startArr[1];
              }
              else if(paramArr[i].toUpperCase().startsWith("END"))
              {
                String endArr[] = paramArr[i].split("=");
                endArr[1] = endArr[1].trim();

                if(endArr.length > 2)
                {
                  for(int j = 2; j < endArr.length; j++)
                  {
                    endArr[1] = endArr[1] + "=" + endArr[j];
                  }
                }

                // endArr[1] = endArr[1].substring(1, (endArr[1].length() - 1));
                end = endArr[1];
              }
              else if(paramArr[i].toUpperCase().startsWith("FORMAT"))
              {
                String formatArr[] = paramArr[i].split("=");
                formatArr[1] = formatArr[1].trim();

                if(formatArr.length > 2)
                {
                  for(int j = 2; j < formatArr.length; j++)
                  {
                    formatArr[1] = formatArr[1] + "=" + formatArr[j];
                  }
                }

                // formatArr[1] = formatArr[1].substring(1, (formatArr[1].length() - 1));
                format = formatArr[1];
              }
              else if(paramArr[i].toUpperCase().startsWith("REFRESH"))
              {
                String refreshArr[] = paramArr[i].split("=");
                refreshArr[1] = refreshArr[1].trim();

                if(refreshArr.length > 2)
                {
                  for(int j = 2; j < refreshArr.length; j++)
                  {
                    refreshArr[1] = refreshArr[1] + "=" + refreshArr[j];
                  }
                }

                // refreshArr[1] = refreshArr[1].substring(1, (refreshArr[1].length() - 1));
                refresh = refreshArr[1];
              }
              else if(paramArr[i].toUpperCase().startsWith("OUTOFVALUE"))
              {
                String outOfValueArr[] = paramArr[i].split("=");
                outOfValueArr[1] = outOfValueArr[1].trim();

                if(outOfValueArr.length > 2)
                {
                  for(int j = 2; j < outOfValueArr.length; j++)
                  {
                    outOfValueArr[1] = outOfValueArr[1] + "=" + outOfValueArr[j];
                  }
                }

                // outOfValueArr[1] = outOfValueArr[1].substring(1, (outOfValueArr[1].length() - 1));
                outOfValue = outOfValueArr[1];
              }
              else
              {
                paramArr[i] = paramArr[i].trim();
                Log.debugLog(className, "getUniqueNumberVUserByURL", "", "", "Invalid Parameter = " + paramArr[i]);
                continue;
              }
            }
          }
          catch(Exception e)
          {
            continue;
          }

          if(varName.equals(""))
            continue;

          String[] arrRowValue = new String[6];
          arrRowValue[0] = varName;
          arrRowValue[1] = FileBean.escapeHTML(start);
          arrRowValue[2] = FileBean.escapeHTML(end);
          arrRowValue[3] = FileBean.escapeHTML(format);
          arrRowValue[4] = FileBean.escapeHTML(refresh);
          arrRowValue[5] = FileBean.escapeHTML(outOfValue);
          alUniqueNumber.add(arrRowValue);
        }
        arrUniqueNumber = new String[alUniqueNumber.size()][6];
        for(int ii = 0; ii < alUniqueNumber.size(); ii++)
        {
          String[] strRowValue = (String[])alUniqueNumber.get(ii);
          for(int jj = 0; jj < strRowValue.length; jj++)
          {
            arrUniqueNumber[ii][jj] = strRowValue[jj];
          }
        }
      }
      arrUniqueNumber = sortArray(arrUniqueNumber, sortOnCol, sortPrefrence, "STRING");
      return arrUniqueNumber;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getUniqueNumberVUserByURL", "", "", "Exception - ", ex);
      String[][] dummy = new String[0][6];
      return dummy;
    }
  }

  // Method to get Unique Number for specified URL
  public String[][] getUniqueNumberByURL(String hostName, String serviceName, String URL, int sortOnCol, int sortPrefrence)
  {
    Log.debugLog(className, "getUniqueNumberByURL", "", "", "Method started");
    try
    {
      ArrayList alUniqueNumber = new ArrayList();

      Vector hpdURLData = readFile(getServiceConfFilePath(hostName, serviceName), true);

      if((hpdURLData == null) || (hpdURLData.size() <= 0))
      {
        Log.debugLog(className, "getUniqueNumberByURL", "", "", "Service File not found, It may be corrupted. Host=" + hostName + ", Service=" + serviceName);
        String[][] dummy = new String[0][3];
        return dummy;
      }

      int rowId = -1;
      String[][] arrUniqueNumber = null;
      for(int k = 0; k < hpdURLData.size(); k++)
      {
        String line = hpdURLData.elementAt(k).toString();
        String temp = line.trim();

        if(temp.startsWith("nsl_unique_number_var")) // Method found
        {
          int indexForClose = line.lastIndexOf(")");
          if((indexForClose == -1) || (indexForClose != line.length() - 2))
            continue;

          String strParam = line.substring(line.indexOf("(") + 1, indexForClose);
          String paramArr[] = strParam.split(",");

          String varName = "";
          // ArrayList varValue = new ArrayList();
          //String start = "";
          //String end = "";
          String format = "";
          String refresh = "";

          try
          {
            Vector vecForSplitStr = new Vector();
            varName = paramArr[0];
            for(int i = 1; i < paramArr.length; i++)
            {
              paramArr[i] = paramArr[i].trim();
              /*if(paramArr[i].toUpperCase().startsWith("MIN"))
              {
                String startArr[] = paramArr[i].split("=");
                startArr[1] = startArr[1].trim();

                if(startArr.length > 2)
                {
                  for(int j = 2; j < startArr.length; j++)
                  {
                    startArr[1] = startArr[1] + "=" + startArr[j];
                  }
                }

                // startArr[1] = startArr[1].substring(1, (startArr[1].length() - 1));
                start = startArr[1];
              }
              else if(paramArr[i].toUpperCase().startsWith("MAX"))
              {
                String endArr[] = paramArr[i].split("=");
                endArr[1] = endArr[1].trim();

                if(endArr.length > 2)
                {
                  for(int j = 2; j < endArr.length; j++)
                  {
                    endArr[1] = endArr[1] + "=" + endArr[j];
                  }
                }

                // endArr[1] = endArr[1].substring(1, (endArr[1].length() - 1));
                end = endArr[1];
              }*/
              if(paramArr[i].toUpperCase().startsWith("FORMAT"))
              {
                String formatArr[] = paramArr[i].split("=");
                formatArr[1] = formatArr[1].trim();

                if(formatArr.length > 2)
                {
                  for(int j = 2; j < formatArr.length; j++)
                  {
                    formatArr[1] = formatArr[1] + "=" + formatArr[j];
                  }
                }

                // formatArr[1] = formatArr[1].substring(1, (formatArr[1].length() - 1));
                format = formatArr[1];
              }
              else if(paramArr[i].toUpperCase().startsWith("REFRESH"))
              {
                String refreshArr[] = paramArr[i].split("=");
                refreshArr[1] = refreshArr[1].trim();

                if(refreshArr.length > 2)
                {
                  for(int j = 2; j < refreshArr.length; j++)
                  {
                    refreshArr[1] = refreshArr[1] + "=" + refreshArr[j];
                  }
                }

                // refreshArr[1] = refreshArr[1].substring(1, (refreshArr[1].length() - 1));
                refresh = refreshArr[1];
              }
              else
              {
                paramArr[i] = paramArr[i].trim();
                Log.debugLog(className, "getUniqueNumberByURL", "", "", "Invalid Parameter = " + paramArr[i]);
                continue;
              }
            }
          }
          catch(Exception e)
          {
            continue;
          }

          if(varName.equals(""))
            continue;

          String[] arrRowValue = new String[4];
          arrRowValue[0] = varName;
          //arrRowValue[1] = FileBean.escapeHTML(start);
          //arrRowValue[2] = FileBean.escapeHTML(end);
          arrRowValue[1] = FileBean.escapeHTML(format);
          arrRowValue[2] = FileBean.escapeHTML(refresh);
          ++rowId;
          arrRowValue[3] = String.valueOf(rowId);
          alUniqueNumber.add(arrRowValue);
        }
        arrUniqueNumber = new String[alUniqueNumber.size()][4];
        for(int ii = 0; ii < alUniqueNumber.size(); ii++)
        {
          String[] strRowValue = (String[])alUniqueNumber.get(ii);
          for(int jj = 0; jj < strRowValue.length; jj++)
          {
            arrUniqueNumber[ii][jj] = strRowValue[jj];
          }
        }
      }
      arrUniqueNumber = sortArray(arrUniqueNumber, sortOnCol, sortPrefrence, "STRING");
      return arrUniqueNumber;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getUniqueNumberByURL", "", "", "Exception - ", ex);
      String[][] dummy = new String[0][4];
      return dummy;
    }
  }

  // Method to get XML Parameters for specified URL
  public String[][] getXMLParametersByURL(String hostName, String serviceName, String URL, int sortOnCol, int sortPrefrence)
  {
    Log.debugLog(className, "getXMLParametersByURL", "", "", "Method started");
    try
    {
      ArrayList alXMLParameter = new ArrayList();

      Vector hpdURLData = readFile(getServiceConfFilePath(hostName, serviceName), true);
      int rowId = -1;
      if((hpdURLData == null) || (hpdURLData.size() <= 0))
      {
        Log.debugLog(className, "getXMLParametersByURL", "", "", "Service File not found, It may be corrupted. Host=" + hostName + ", Service=" + serviceName);
        String[][] dummy = new String[0][10];
        return dummy;
      }

      String[][] arrXMLParameter = null;
      for(int k = 0; k < hpdURLData.size(); k++)
      {
        String line = hpdURLData.elementAt(k).toString();
        String temp = line.trim();

        if(temp.startsWith("nsl_xml_var")) // Method found
        {
          int indexForClose = line.lastIndexOf(")");
          if((indexForClose == -1) || (indexForClose != line.length() - 2))
            continue;

          String strParam = line.substring(line.indexOf("(") + 1, indexForClose);
          String paramArr[] = strParam.split(",");

          String varName = "";
          // ArrayList varValue = new ArrayList();
          String node = "";
          String value = "";
          String where = "";
          String ord = "";
          String conversion = "";
          String retainPreValue = "0";
          String bodySkipStartBytes = "0";
          String bodySkipEndBytes = "0";
          try
          {
            Vector vecForSplitStr = new Vector();
            for(int i = 0; i < paramArr.length; i++)
            {
              if(paramArr[i].toUpperCase().trim().startsWith("NODE"))
              {
                String startArr[] = paramArr[i].split("=");
                startArr[1] = startArr[1].trim();

                if(startArr.length > 2)
                {
                  for(int j = 2; j < startArr.length; j++)
                  {
                    startArr[1] = startArr[1] + "=" + startArr[j];
                  }
                }

                node = startArr[1];
              }
              else if(paramArr[i].toUpperCase().trim().startsWith("VALUE"))
              {
                String endArr[] = paramArr[i].split("=");
                endArr[1] = endArr[1].trim();

                if(endArr.length > 2)
                {
                  for(int j = 2; j < endArr.length; j++)
                  {
                    endArr[1] = endArr[1] + "=" + endArr[j];
                  }
                }

                value = endArr[1];
              }
              else if(paramArr[i].toUpperCase().trim().startsWith("WHERE"))
              {
                String formatArr[] = paramArr[i].split("=");
                formatArr[1] = formatArr[1].trim();

                if(formatArr.length > 2)
                {
                  for(int j = 2; j < formatArr.length; j++)
                  {
                    formatArr[1] = formatArr[1] + "=" + formatArr[j];
                  }
                }
                if(where.equals(""))
                  where = formatArr[1];
                else
                  where = where + "|" + formatArr[1];
              }
              else if(paramArr[i].toUpperCase().trim().startsWith("ORD"))
              {
                String refreshArr[] = paramArr[i].split("=");
                refreshArr[1] = refreshArr[1].trim();

                if(refreshArr.length > 2)
                {
                  for(int j = 2; j < refreshArr.length; j++)
                  {
                    refreshArr[1] = refreshArr[1] + "=" + refreshArr[j];
                  }
                }
                // refreshArr[1] = refreshArr[1].substring(1, (refreshArr[1].length() - 1));
                ord = refreshArr[1];
              }
              else if(paramArr[i].toUpperCase().trim().startsWith("CONVERT"))
              {
                String convertArr[] = paramArr[i].split("=");
                convertArr[1] = convertArr[1].trim();

                if(convertArr.length > 2)
                {
                  for(int j = 2; j < convertArr.length; j++)
                  {
                    convertArr[1] = convertArr[1] + "=" + convertArr[j];
                  }
                }
                conversion = convertArr[1];
              }
              else if(paramArr[i].toUpperCase().trim().startsWith("RETAINPREVALUE"))
              {
                String retainPreValuetArr[] = paramArr[i].split("=");
                retainPreValuetArr[1] = retainPreValuetArr[1].trim();

                if(retainPreValuetArr.length > 2)
                {
                  for(int j = 2; j < retainPreValuetArr.length; j++)
                  {
                    retainPreValuetArr[1] = retainPreValuetArr[1] + "=" + retainPreValuetArr[j];
                  }
                }
                retainPreValue = retainPreValuetArr[1];
              }
              else if(paramArr[i].toUpperCase().trim().startsWith("BODYSKIPSTARTBYTES"))
              {
                String bodySkipStartBytesArr[] = paramArr[i].split("=");
                bodySkipStartBytesArr[1] = bodySkipStartBytesArr[1].trim();

                if(bodySkipStartBytesArr.length > 2)
                {
                  for(int j = 2; j < bodySkipStartBytesArr.length; j++)
                  {
                    bodySkipStartBytesArr[1] = bodySkipStartBytesArr[1] + "=" + bodySkipStartBytesArr[j];
                  }
                }
                bodySkipStartBytes = bodySkipStartBytesArr[1];
              }
              else if(paramArr[i].toUpperCase().trim().startsWith("BODYSKIPENDBYTES"))
              {
                String bodySkipEndBytesArr[] = paramArr[i].split("=");
                bodySkipEndBytesArr[1] = bodySkipEndBytesArr[1].trim();

                if(bodySkipEndBytesArr.length > 2)
                {
                  for(int j = 2; j < bodySkipEndBytesArr.length; j++)
                  {
                    bodySkipEndBytesArr[1] = bodySkipEndBytesArr[1] + "=" + bodySkipEndBytesArr[j];
                  }
                }
                bodySkipEndBytes = bodySkipEndBytesArr[1];
              }
              else
              {
                paramArr[i] = paramArr[i].trim();
                varName = paramArr[i];
              }
            }
          }
          catch(Exception e)
          {
            continue;
          }

          if(varName.equals(""))
            continue;

          String[] arrRowValue = new String[10];
          arrRowValue[0] = varName;
          arrRowValue[1] = node;
          arrRowValue[2] = value;
          arrRowValue[3] = where;
          arrRowValue[4] = ord;
          arrRowValue[5] = conversion;
          arrRowValue[6] = retainPreValue;
          ++rowId;
          arrRowValue[7] = String.valueOf(rowId);
          arrRowValue[8] = bodySkipStartBytes;
          arrRowValue[9] = bodySkipEndBytes;

          alXMLParameter.add(arrRowValue);
        }

        arrXMLParameter = new String[alXMLParameter.size()][10];
        for(int ii = 0; ii < alXMLParameter.size(); ii++)
        {
          String[] strRowValue = (String[])alXMLParameter.get(ii);
          for(int jj = 0; jj < strRowValue.length; jj++)
          {
            arrXMLParameter[ii][jj] = strRowValue[jj];
          }
        }
      }

      arrXMLParameter = sortArray(arrXMLParameter, sortOnCol, sortPrefrence, "STRING");

      return arrXMLParameter;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getXMLParametersByURL", "", "", "Exception - ", ex);
      String[][] dummy = new String[0][10];
      return dummy;
    }
  }

  //Method to get Request Parameters for specified URL
  public String[][] getRequestParametersByURL(String hostName, String serviceName, String URL, int sortOnCol, int sortPrefrence)
  {
    Log.debugLog(className, "getRequestParametersByURL", "", "", "Method started");
    try
    {
      ArrayList alRequestParameter = new ArrayList();

      Vector hpdURLData = readFile(getServiceConfFilePath(hostName, serviceName), true);
      int rowId = -1;
      if((hpdURLData == null) || (hpdURLData.size() <= 0))
      {
        Log.debugLog(className, "getRequestParametersByURL", "", "", "Service File not found, It may be corrupted. Host=" + hostName + ", Service=" + serviceName);
        String[][] dummy = new String[0][12];
        return dummy;
      }

      String[][] arrRequestParameter = null;
      for(int k = 0; k < hpdURLData.size(); k++)
      {
        String line = hpdURLData.elementAt(k).toString();
        String temp = line.trim();

        if(temp.startsWith("nsl_request_var")) // Method found
        {
          int indexForClose = line.lastIndexOf(")");
          if((indexForClose == -1) || (indexForClose != line.length() - 2))
            continue;

          String strParam = line.substring(line.indexOf("(") + 1, indexForClose);
          String paramArr[] = strParam.split(",");

          String varName = "";
          String attributeName = "";
          String occCount = "1";
          String DefaultValue = "";
          String SpecifiedChars = "";
          String startOffset = "0";
          String maxLength = "0";
          String method = "None";
          String ActionOnNotFound = "None";
          String Encode = "None";
          String Decode = "No";
          try
          {
            Vector vecForSplitStr = new Vector();
            for(int i = 0; i < paramArr.length; i++)
            {
              if(paramArr[i].toUpperCase().trim().startsWith("ASSOCIATEDOBJECT="))
              {
                String startAtt[] = paramArr[i].split("=");
                startAtt[1] = startAtt[1].trim();

                if(startAtt.length > 2)
                {
                  for(int j = 2; j < startAtt.length; j++)
                  {
                    startAtt[1] = startAtt[1] + "=" + startAtt[j];
                  }
                }

                attributeName = startAtt[1].trim();
                if(attributeName.startsWith("\"") && attributeName.endsWith("\""))
                  attributeName = attributeName.substring(1, attributeName.length() - 1);

              }
              else if(paramArr[i].toUpperCase().trim().startsWith("ORD="))
              {
                String ordArr[] = paramArr[i].split("=");
                ordArr[1] = ordArr[1].trim();

                if(ordArr.length > 2)
                {
                  for(int j = 2; j < ordArr.length; j++)
                  {
                    ordArr[1] = ordArr[1] + "=" + ordArr[j];
                  }
                }

                occCount = ordArr[1].trim();
                if(occCount.startsWith("\"") && occCount.endsWith("\""))
                  occCount = occCount.substring(1, occCount.length() - 1);
              }
              else if(paramArr[i].toUpperCase().trim().startsWith("STARTOFFSET="))
              {
                String offSetArr[] = paramArr[i].split("=");
                offSetArr[1] = offSetArr[1].trim();

                if(offSetArr.length > 2)
                {
                  for(int j = 2; j < offSetArr.length; j++)
                  {
                    offSetArr[1] = offSetArr[1] + "=" + offSetArr[j];
                  }
                }
                startOffset = offSetArr[1].trim();
                if(startOffset.startsWith("\"") && startOffset.endsWith("\""))
                  startOffset = startOffset.substring(1, startOffset.length() - 1);
              }
              else if(paramArr[i].toUpperCase().trim().startsWith("SAVELEN="))
              {
                String saveLenArr[] = paramArr[i].split("=");
                saveLenArr[1] = saveLenArr[1].trim();

                if(saveLenArr.length > 2)
                {
                  for(int j = 2; j < saveLenArr.length; j++)
                  {
                    saveLenArr[1] = saveLenArr[1] + "=" + saveLenArr[j];
                  }
                }
                maxLength = saveLenArr[1].trim();
                if(maxLength.startsWith("\"") && maxLength.endsWith("\""))
                  maxLength = maxLength.substring(1, maxLength.length() - 1);
              }
              else if(paramArr[i].toUpperCase().trim().startsWith("ACTIONONNOTFOUND="))
              {
                String actionNotFoundArr[] = paramArr[i].split("=");
                actionNotFoundArr[1] = actionNotFoundArr[1].trim();

                if(actionNotFoundArr.length > 2)
                {
                  for(int j = 2; j < actionNotFoundArr.length; j++)
                  {
                    actionNotFoundArr[1] = actionNotFoundArr[1] + "=" + actionNotFoundArr[j];
                  }
                }

                ActionOnNotFound = actionNotFoundArr[1].trim();
                if(ActionOnNotFound.startsWith("\"") && ActionOnNotFound.endsWith("\""))
                  ActionOnNotFound = ActionOnNotFound.substring(1, ActionOnNotFound.length() - 1);
              }
              else if(paramArr[i].toUpperCase().trim().startsWith("DEFAULTVALUE="))
              {
                String defaultValueArr[] = paramArr[i].split("=");
                defaultValueArr[1] = defaultValueArr[1].trim();

                if(defaultValueArr.length > 2)
                {
                  for(int j = 2; j < defaultValueArr.length; j++)
                  {
                    defaultValueArr[1] = defaultValueArr[1] + "=" + defaultValueArr[j];
                  }
                }
                DefaultValue = defaultValueArr[1].trim();
                DefaultValue = DefaultValue.replace("\\\"", "\"");
                DefaultValue = DefaultValue.replace("\\\\", "\\");
                if(DefaultValue.startsWith("\"") && DefaultValue.endsWith("\""))
                  DefaultValue = DefaultValue.substring(1, DefaultValue.length() - 1);
              }
              else if(paramArr[i].toUpperCase().trim().startsWith("METHOD="))
              {
                String methodArr[] = paramArr[i].split("=");
                methodArr[1] = methodArr[1].trim();

                if(methodArr.length > 2)
                {
                  for(int j = 2; j < methodArr.length; j++)
                  {
                    methodArr[1] = methodArr[1] + "=" + methodArr[j];
                  }
                }
                method = methodArr[1].trim();
                if(method.startsWith("\"") && method.endsWith("\""))
                  method = method.substring(1, method.length() - 1);
              }
              else if(paramArr[i].toUpperCase().trim().startsWith("DECODE="))
              {
                String decodeArr[] = paramArr[i].split("=");
                decodeArr[1] = decodeArr[1].trim();

                if(decodeArr.length > 2)
                {
                  for(int j = 2; j < decodeArr.length; j++)
                  {
                    decodeArr[1] = decodeArr[1] + "=" + decodeArr[j];
                  }
                }
                Decode = decodeArr[1].trim();
                if(Decode.startsWith("\"") && Decode.endsWith("\""))
                  Decode = Decode.substring(1, Decode.length() - 1);
              }
              else if(paramArr[i].toUpperCase().trim().startsWith("ENCODE="))
              {
                String encodeArr[] = paramArr[i].split("=");
                encodeArr[1] = encodeArr[1].trim();

                if(encodeArr.length > 2)
                {
                  for(int j = 2; j < encodeArr.length; j++)
                  {
                    encodeArr[1] = encodeArr[1] + "=" + encodeArr[j];
                  }
                }
                Encode = encodeArr[1].trim();
                if(Encode.startsWith("\"") && Encode.endsWith("\""))
                  Encode = Encode.substring(1, Encode.length() - 1);
              }
              else if(paramArr[i].toUpperCase().trim().startsWith("SPECIFIEDCHARS="))
              {
                String specifiedArr[] = paramArr[i].split("=");
                specifiedArr[1] = specifiedArr[1].trim();

                if(specifiedArr.length > 2)
                {
                  for(int j = 2; j < specifiedArr.length; j++)
                  {
                    specifiedArr[1] = specifiedArr[1] + "=" + specifiedArr[j];
                  }
                }
                SpecifiedChars = specifiedArr[1].trim();

                if(SpecifiedChars.startsWith("\"") && SpecifiedChars.endsWith("\""))
                  SpecifiedChars = SpecifiedChars.substring(1, SpecifiedChars.length() - 1);

                SpecifiedChars = SpecifiedChars.replace("\\\"", "\"");
                SpecifiedChars = SpecifiedChars.replace("\\\\", "\\");
              }
              else
              {
                paramArr[i] = paramArr[i].trim();
                varName = paramArr[i];
              }
            }
          }
          catch(Exception e)
          {
            continue;
          }

          if(varName.equals(""))
            continue;

          String[] arrRowValue = new String[12];
          arrRowValue[0] = varName;
          arrRowValue[1] = attributeName;
          arrRowValue[2] = occCount;
          arrRowValue[3] = startOffset;
          arrRowValue[4] = maxLength;
          arrRowValue[5] = ActionOnNotFound;
          arrRowValue[6] = DefaultValue;
          arrRowValue[7] = method;
          arrRowValue[8] = Decode;
          arrRowValue[9] = Encode;
          arrRowValue[10] = SpecifiedChars;
          ++rowId;
          arrRowValue[11] = String.valueOf(rowId);

          alRequestParameter.add(arrRowValue);
        }

        arrRequestParameter = new String[alRequestParameter.size()][12];
        for(int ii = 0; ii < alRequestParameter.size(); ii++)
        {
          String[] strRowValue = (String[])alRequestParameter.get(ii);
          for(int jj = 0; jj < strRowValue.length; jj++)
          {
            arrRequestParameter[ii][jj] = strRowValue[jj];
          }
        }
      }

      arrRequestParameter = sortArray(arrRequestParameter, sortOnCol, sortPrefrence, "STRING");

      return arrRequestParameter;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getRequestParametersByURL", "", "", "Exception - ", ex);
      String[][] dummy = new String[0][12];
      return dummy;
    }
  }

  //Method to get XML Parameters for specified URL
  public String[][] getIndexedDataSourceByURL(String hostName, String serviceName, String URL, int sortOnCol, int sortPrefrence)
  {
    Log.debugLog(className, "getIndexedDataSourceByURL", "", "", "Method started");
    try
    {
      ArrayList alIndexedDataSourceParameter = new ArrayList();

      Vector hpdURLData = readFile(getServiceConfFilePath(hostName, serviceName), true);
      int rowId = -1;
      if((hpdURLData == null) || (hpdURLData.size() <= 0))
      {
        Log.debugLog(className, "getIndexedDataSourceByURL", "", "", "Service File not found, It may be corrupted. Host=" + hostName + ", Service=" + serviceName);
        String[][] dummy = new String[0][4];
        return dummy;
      }

      String[][] arrIndexDataSourceParameter = null;
      for(int k = 0; k < hpdURLData.size(); k++)
      {
        String line = hpdURLData.elementAt(k).toString();
        String temp = line.trim();

        if(temp.startsWith("nsl_select_index_datasource")) // Method found
        {
          int indexForClose = line.lastIndexOf(")");
          if((indexForClose == -1) || (indexForClose != line.length() - 2))
            continue;

          String strParam = line.substring(line.indexOf("(") + 1, indexForClose);
          String paramArr[] = strParam.split(",");

          String dataSource = "";
          String indexVar = "";
          String ColumnNames = "";
          try
          {
            Vector vecForSplitStr = new Vector();
            for(int i = 0; i < paramArr.length; i++)
            {
              if(paramArr[i].toUpperCase().trim().startsWith("INDEX_VAR"))
              {
                String indexVarArr[] = paramArr[i].split("=");
                indexVarArr[1] = indexVarArr[1].trim();

                if(indexVarArr.length > 2)
                {
                  for(int j = 2; j < indexVarArr.length; j++)
                  {
                    indexVarArr[1] = indexVarArr[1] + "=" + indexVarArr[j];
                  }
                }
                indexVar = indexVarArr[1].trim();
                if(indexVar.startsWith("\"") && indexVar.startsWith("\""))
                  indexVar = indexVar.substring(1, indexVar.length() - 1);
              }
              else
              {
                paramArr[i] = paramArr[i].trim();
                dataSource = paramArr[i];
              }
            }
          }
          catch(Exception e)
          {
            continue;
          }

          if(dataSource.equals(""))
            continue;
          NFUtils nfUtils = new NFUtils();
          String[][] arrDataValue = nfUtils.getIndexDataSourceDetails(1, 0);
          for(int i = 0; i < arrDataValue.length; i++)
          {
            if(arrDataValue[i][2].equals(dataSource))
            {
              ColumnNames = arrDataValue[i][7] + "," + arrDataValue[i][0];
              break;
            }
          }
          String[] arrRowValue = new String[4];
          arrRowValue[0] = dataSource;
          arrRowValue[1] = indexVar;
          arrRowValue[2] = ColumnNames;
          ++rowId;
          arrRowValue[3] = String.valueOf(rowId);
          alIndexedDataSourceParameter.add(arrRowValue);
        }

        arrIndexDataSourceParameter = new String[alIndexedDataSourceParameter.size()][4];
        for(int ii = 0; ii < alIndexedDataSourceParameter.size(); ii++)
        {
          String[] strRowValue = (String[])alIndexedDataSourceParameter.get(ii);
          for(int jj = 0; jj < strRowValue.length; jj++)
          {
            arrIndexDataSourceParameter[ii][jj] = strRowValue[jj];
          }
        }
      }

      arrIndexDataSourceParameter = sortArray(arrIndexDataSourceParameter, sortOnCol, sortPrefrence, "STRING");

      return arrIndexDataSourceParameter;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getXMLParametersByURL", "", "", "Exception - ", ex);
      String[][] dummy = new String[0][4];
      return dummy;
    }
  }

  //method to check if all templates are inactive or not. It will return "" if no template is inactive else will return the template name which is active
  public String isAllTemplatesInactive(String hostName, String serviceName, String URL, String templateNameNotToCheck)
  {
    String activeTemplate = "";

    String[][] arrDataValues = getResponseTemplateInfoByURL(hostName, serviceName, URL);
    for(int i = 0; i < arrDataValues.length; i++)
    {
      if(!arrDataValues[i][0].equalsIgnoreCase(templateNameNotToCheck))
      {
        //String templateStatus = isActiveInactiveTemplate(hostName, serviceName,  URL, allTemplates[i]);
        if(arrDataValues[i][5].equalsIgnoreCase("active"))
        {
          activeTemplate = arrDataValues[i][0];
          break;
        }
      }
    }
    return activeTemplate;
  }

  //method to get default template without condtion is active or not
  public String isDefaultTemplateInactive(String hostName, String serviceName, String URL, String templateNameNotToCheck)
  {
    String activeTemplate = "";

    String[][] arrDataValues = getResponseTemplateInfoByURL(hostName, serviceName, URL);
    //String[] allTemplates = getResponseTemplateNamesByURL(hostName, serviceName, URL);
    for(int i = 0; i < arrDataValues.length; i++)
    {
      if(!arrDataValues[i][0].equalsIgnoreCase(templateNameNotToCheck))
      {
        //String templateStatus = isActiveInactiveTemplate(hostName, serviceName,  URL, allTemplates[i]);
        //String templateCondition = getResponseTemplateCondition(hostName, serviceName,  URL, allTemplates[i]);
        if(arrDataValues[i][5].equalsIgnoreCase("active") && arrDataValues[i][6].equals("NA"))
        {
          activeTemplate = arrDataValues[i][0];
          break;
        }
      }
    }
    return activeTemplate;
  }

  // method to get the response template names by URL
  public String[] getResponseTemplateNamesByURL(String hostName, String serviceName, String URL)
  {
    Log.debugLog(className, "getResponseTemplateNamesByURL", "", "", "Method started");
    try
    {
      ArrayList alResponseTemplateName = new ArrayList();

      Vector hpdURLData = readFile(getServiceConfFilePath(hostName, serviceName), true);

      if((hpdURLData == null) || (hpdURLData.size() <= 0))
      {
        Log.debugLog(className, "getResponseTemplateNamesByURL", "", "", "Service File not found, It may be corrupted. HOST=" + hostName + ", Service=" + serviceName);
        return new String[]{""};
      }

      for(int k = 0; k < hpdURLData.size(); k++)
      {
        String[] arrURLLine = rptUtilsBean.strToArrayData(hpdURLData.elementAt(k).toString(), " ");
        if(arrURLLine.length <= 0)
          continue;

        if(arrURLLine.length < 5)
        {
          Log.debugLog(className, "getResponseTemplateNamesByURL", "", "", "format of response template is not proper at line = " + k);
          continue;
        }

        if(arrURLLine[0].toUpperCase().equals("RESPONSE_TEMPLATE"))
        {
          alResponseTemplateName.add(arrURLLine[1]);
        }
      }

      String[] arrResponseTemplate = new String[alResponseTemplateName.size()];
      for(int xy = 0; xy < alResponseTemplateName.size(); xy++)
      {
        arrResponseTemplate[xy] = alResponseTemplateName.get(xy).toString();
      }
      return arrResponseTemplate;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getResponseTemplateNamesByURL", "", "", "Exception - ", ex);
      return new String[]{""};
    }
  }

  //Method to get the all extension types to create the response Template Names.
  //we will get these from hpd directory /conf/extension.type
  public String[][] getAllResponseExtensionTypes()
  {
    Log.debugLog(className, "getAllResponseExtensionTypes", "", "", "Method started");
    try
    {
      Vector hpdExtensionData = readFile(getHPDPath() + "conf/extensionGUI.type", true);

      if((hpdExtensionData == null) || (hpdExtensionData.size() <= 0))
      {
        Log.debugLog(className, "getAllResponseExtensionTypes", "", "", "extension.type not found in conf directory");
        String[][] dummy = new String[0][2];
        return dummy;
      }
      TreeSet<String[]> sortedSet = new TreeSet<String[]>(new CustomComparator());
      for(int k = 0; k < hpdExtensionData.size(); k++)
      {
        String[] arrURLLine = rptUtilsBean.strToArrayData(hpdExtensionData.elementAt(k).toString(), "|");
        String tempStr[] = new String[2];
        if(arrURLLine.length >= 2)
        {
          tempStr[0] = arrURLLine[0];
          tempStr[1] = arrURLLine[1];
        }
        sortedSet.add(tempStr);
      }

      String[][] arrExtensions = new String[sortedSet.size()][2];
      Iterator ite = sortedSet.iterator();
      int i = 0;
      while(ite.hasNext())
      {
        arrExtensions[i] = (String[])ite.next();
        i++;
      }

      return arrExtensions;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getAllResponseExtensionTypes", "", "", "Exception - ", ex);
      String[][] dummy = new String[0][2];
      return dummy;
    }
  }

  //method to check if template name exist. if template is already exit then generate new template name
  //by adding suffix
  public String generateTemplateNameIfDuplicate(String hostName, String serviceName, String URL, String templateName)
  {
    Log.debugLog(className, "generateTemplateNameIfDuplicate", "", "", "Method started service = " + serviceName + ", templateName = " + templateName);
    try
    {
      String newTemplateName = templateName;
      int ctr = 1;
      while(isTemplateNameExist(hostName, serviceName, URL, newTemplateName))
      {
        newTemplateName = templateName + "_" + ctr;
        Log.debugLog(className, "generateTemplateNameIfDuplicate", "", "", "new generated template name = " + newTemplateName);
        ctr++;
      }
      return newTemplateName;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "generateTemplateNameIfDuplicate", "", "", "Exception - ", ex);
      return templateName;
    }
  }

  //method to check if template name already exist.
  public boolean isTemplateNameExist(String hostName, String serviceName, String URL, String templateName)
  {
    Log.debugLog(className, "isTemplateNameExist", "", "", "Method started service = " + serviceName + ", templateName = " + templateName);
    try
    {
      String newTemplateName = templateName;
      Vector completeControlFile = readFile(getServiceConfFilePath(hostName, serviceName), true);
      if(completeControlFile == null)
      {
        Log.debugLog(className, "isTemplateNameExist", "", "", "URL File not found, It may be corrupted.");
        return false;
      }

      for(int i = 0; i < completeControlFile.size(); i++)
      {
        if(completeControlFile.elementAt(i).toString().indexOf("RESPONSE_TEMPLATE") > -1)
        {
          String[] arrLine = rptUtilsBean.strToArrayData(completeControlFile.elementAt(i).toString(), " ");
          Log.debugLog(className, "isTemplateNameExist", "", "", "arrLine[1] = " + arrLine[1] + ", templateName = " + templateName);
          if(arrLine.length > 1)
          {
            if(arrLine[1].equals(templateName))
            {
              return true;
            }
          }
        }
      }
      return false;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "isTemplateNameExist", "", "", "Exception - ", ex);
      return false;
    }
  }

  public void makeDirs(String strPath)
  {
    Log.debugLog(className, "makeDirs", "", "", "Method started strPath=" + strPath);
    try
    {
      int lastURLIndex = strPath.lastIndexOf("/");
      String newURLPath = strPath;
      if(lastURLIndex > -1)
        newURLPath = strPath.substring(0, lastURLIndex);

      File fileCreateURLDirs = new File(newURLPath);
      if(!fileCreateURLDirs.mkdirs())
      {
        Log.debugLog(className, "makeDirs", "", "", "Unable to create directory or it is already there. path = " + newURLPath);
        // return false;
      }
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "makeDirs", "", "", "Exception - ", ex);
    }
  }

  public boolean addResponseTemplate(String hostName, String serviceName, String URL, String templateName, String condition, String responseExtension, StringBuffer responseFileContents, StringBuffer requestFileContents, String activeInactive, String templateType)
  {
    return addResponseTemplate(hostName, serviceName, URL, templateName, condition, responseExtension, responseFileContents, requestFileContents, null, null, false, activeInactive, templateType);
  }

  // method to get the response template names by URL
  public boolean addResponseTemplate(String hostName, String serviceName, String URL, String templateName, String condition, String responseExtension, StringBuffer responseFileContents, StringBuffer requestFileContents, StringBuffer requestBodyFileContents, StringBuffer responseBodyFileContents, boolean isRecording, String activeInactive, String TemplateType)
  {
    Log.debugLog(className, "addResponseTemplate", "", "", "Method started");
    try
    {
      if(condition.equals(""))
        condition = "NA";

      if(isRecording)// this condition is only to be check in case of recording.
      {
        // 2 Template should not have NA value in condition block, so we are forcing next template to inactive
        if(condition.equals("NA"))
          activeInactive = "inactive";
      }

      //method call to generate unique template name if duplicate
      //by adding suffix "_n"
      templateName = generateTemplateNameIfDuplicate(hostName, serviceName, URL, templateName);
      String strURLPath = getServiceConfFilePath(hostName, serviceName);
      int lastURLIndex = strURLPath.lastIndexOf("/");
      String newURLPath = "";

      if(lastURLIndex > -1)
        newURLPath = strURLPath.substring(0, lastURLIndex);

      File fileCreateURLDirs = new File(newURLPath);
      if(!fileCreateURLDirs.mkdirs())
      {
        Log.debugLog(className, "addResponseTemplate", "", "", "Unable to create directory or it is already there. path = " + newURLPath);
        // return false;
      }

      File checkFile = new File(strURLPath);
      if(!checkFile.exists())
        checkFile.createNewFile();

      String responseFileName = generateNewResponseFileName(hostName, serviceName, URL, responseExtension, templateName);

      String strToAdd = "";
      if(!TemplateType.startsWith("RequestBased"))
        strToAdd = "RESPONSE_TEMPLATE " + templateName + " " + TemplateType + " " + responseFileName + " " + activeInactive + " " + condition;
      else
        strToAdd = "RESPONSE_TEMPLATE " + templateName + " " + TemplateType + " " + responseFileName + " " + activeInactive;

      FileWriter fstream = new FileWriter(strURLPath, true);
      BufferedWriter out = new BufferedWriter(fstream);
      out.write("\n" + strToAdd + "\n");
      out.close();

      File fileResponseBody = new File(getServicePath(hostName, serviceName) + responseFileName);
      if(!fileResponseBody.exists())
        fileResponseBody.createNewFile();

      File fileRequestBody = new File(getServicePath(hostName, serviceName) + responseFileName + REQUEST_EXTENTION);
      if(!fileRequestBody.exists())
        fileRequestBody.createNewFile();

      //responseFileName = generateNewResponseFileName(hostName, serviceName, URL, responseExtension, templateName);

      File fileResponse = null, fileRequest = null;
      if(isRecording) // this is to handle case of Recording.
      {
        fileResponse = new File(getServicePath(hostName, serviceName) + responseFileName + RESPONSE_EXTENTION + CAPTURED_EXTENTION);
        if(!fileResponse.exists())
          fileResponse.createNewFile();

        fileRequest = new File(getServicePath(hostName, serviceName) + responseFileName + REQUEST_EXTENTION + CAPTURED_EXTENTION);
        if(!fileRequest.exists())
          fileRequest.createNewFile();
      }

      // this is because request and response body content can be null if call from override method i.e. addResponseTemplate.
      if(requestBodyFileContents != null && responseBodyFileContents != null)
      {
        FileOutputStream out1 = new FileOutputStream(fileResponseBody);
        PrintStream responseBodyFile = new PrintStream(out1);
        responseBodyFile.print(responseBodyFileContents);
        responseBodyFile.close();

        FileOutputStream out2 = new FileOutputStream(fileRequestBody);
        PrintStream requestBodyFile = new PrintStream(out2);
        requestBodyFile.print(requestBodyFileContents);
        requestBodyFile.close();
      }
      else
      {
        if(requestBodyFileContents == null)
        {
          Log.debugLog(className, "addResponseTemplate", "", "", "Content of request body file is null.");
          // return false;
        }
        else if(responseBodyFileContents == null)
        {
          Log.debugLog(className, "addResponseTemplate", "", "", "Content of response body file is null.");
          // return false;
        }
      }

      FileOutputStream out3 = null, out4 = null;
      if(!isRecording) // this is case of adding service manually.
      {
        out3 = new FileOutputStream(fileResponseBody);
        out4 = new FileOutputStream(fileRequestBody);
      }
      else
      // this is case of capturing and creating service.
      {
        out3 = new FileOutputStream(fileResponse);
        out4 = new FileOutputStream(fileRequest);
      }

      //FileOutputStream out3 = new FileOutputStream(fileResponse);
      PrintStream responseFile = new PrintStream(out3);
      responseFile.print(responseFileContents.toString());
      responseFile.close();

      //FileOutputStream out4 = new FileOutputStream(fileRequest);
      PrintStream requestFile = new PrintStream(out4);
      if(requestFileContents != null) //in case of add template request content can be null.
      {
        requestFile.print(requestFileContents.toString());
      }
      else
        Log.debugLog(className, "addResponseTemplate", "", "", "request data is null.");

      requestFile.close();

      writeToLogFile(hostName, serviceName);

      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "addResponseTemplate", "", "", "Exception - ", ex);
      return false;
    }
  }

  public String prettyXMLFormat(String input)
  {
    Log.debugLog(className, "prettyXMLFormat", "", "", "Method started");
    int indent = 2;
    try
    {
      input.replaceAll("\"", "'");
      Source xmlInput = new StreamSource(new StringReader(input));
      StringWriter stringWriter = new StringWriter();
      StreamResult xmlOutput = new StreamResult(stringWriter);
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      transformerFactory.setAttribute("indent-number", indent);
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.transform(xmlInput, xmlOutput);
      return xmlOutput.getWriter().toString();
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "prettyXMLFormat", "", "", "Exception - ", e);
      return input;
    }
  }

  /**
   * This method delete the parameter on the basis of index
   * @param hostName
   * @param serviceName
   * @param URL
   * @param arrDelIndex
   * @return
   */
  public boolean deleteAllParameter(String hostName, String serviceName, String URL, String arrDelIndex[])
  {
    Log.debugLog(className, "deleteAllParameter", "", "", "Method started. hostName = " + hostName + ", serviceName = " + serviceName + ", URL = " + URL);

    try
    {
      int intarray[] = new int[arrDelIndex.length];
      //convert into integer array and sort
      for(int i = 0; i < arrDelIndex.length; i++)
      {
        Log.debugLog(className, "deleteAllParameter", "", "", "Getting Index for delete = " + arrDelIndex[i]);
        intarray[i] = Integer.parseInt(arrDelIndex[i]);
      }
      Arrays.sort(intarray);

      //Each parameter have own array list
      ArrayList<Integer> intArrSearchBody = new ArrayList();
      ArrayList<Integer> intArrScratch = new ArrayList();
      ArrayList<Integer> intArrQuery = new ArrayList();
      ArrayList<Integer> intArrDataset = new ArrayList();
      ArrayList<Integer> intArrDateTime = new ArrayList();
      ArrayList<Integer> intArrRandomNumber = new ArrayList();
      ArrayList<Integer> intArrRandomString = new ArrayList();
      ArrayList<Integer> intArrUniqueNumber = new ArrayList();
      ArrayList<Integer> intArrIndexDataSet = new ArrayList();
      ArrayList<Integer> intArrCookie = new ArrayList();
      ArrayList<Integer> intArrXml = new ArrayList();
      ArrayList<Integer> intArrScratchArray = new ArrayList();
      ArrayList<Integer> intArrIndexedDataSource = new ArrayList();
      ArrayList<Integer> intArrRequest = new ArrayList();
      String[][] arrAllParam = getAllParameters(hostName, serviceName, URL, 1, 0);
      boolean flag = true;

      //Each parameter have own counter
      int countSearch = -1;
      int countScratch = -1;
      int countQuery = -1;
      int countDataset = -1;
      int countDateTime = -1;
      int countRandomNumber = -1;
      int countRandomString = -1;
      int countUniqueNumber = -1;
      int countIndexDataSet = -1;
      int countCookie = -1;
      int countXml = -1;
      int countScratchAraay = -1;
      int countIndexedDataSource = -1;
      int countRequestBody = -1;

      //loop of all parameter
      for(int i = 0; i < arrAllParam.length; i++)
      {
        int tempI = i;
        //loop of selected index array
        for(int ii = 0; ii < intarray.length; ii++)
        {
          if(arrAllParam[i][1].equals("Search"))
          {
            //if parameter match it increase the counter one time
            //increment the tempI b'coz inner again come un this condition not increment the counter
            if(tempI == i)
            {
              countSearch++;
              tempI++;
            }
            //If index match add in the array list
            if(intarray[ii] == i)
              intArrSearchBody.add(countSearch);
          }
          else if(arrAllParam[i][1].equals("Scratch"))
          {
            if(tempI == i)
            {
              countScratch++;
              tempI++;
            }

            if(intarray[ii] == i)
              intArrScratch.add(countScratch);
          }
          else if(arrAllParam[i][1].equals("Scratch Array"))
          {
            if(tempI == i)
            {
              countScratchAraay++;
              tempI++;
            }
            if(intarray[ii] == i)
              intArrScratchArray.add(countScratchAraay);
          }
          else if(arrAllParam[i][1].equals("Dataset"))
          {
            if(tempI == i)
            {
              countDataset++;
              tempI++;
            }
            if(intarray[ii] == i)
              intArrDataset.add(countDataset);
          }
          else if(arrAllParam[i][1].equals("Date Time"))
          {
            if(tempI == i)
            {
              countDateTime++;
              tempI++;
            }
            if(intarray[ii] == i)
              intArrDateTime.add(countDateTime);
          }
          else if(arrAllParam[i][1].equals("Query"))
          {
            if(tempI == i)
            {
              countQuery++;
              tempI++;
            }
            if(intarray[ii] == i)
              intArrQuery.add(countQuery);
          }
          else if(arrAllParam[i][1].equals("Random Number"))
          {
            if(tempI == i)
            {
              countRandomNumber++;
              tempI++;
            }
            if(intarray[ii] == i)
              intArrRandomNumber.add(countRandomNumber);
          }
          else if(arrAllParam[i][1].equals("Random String"))
          {
            if(tempI == i)
            {
              countRandomString++;
              tempI++;
            }
            if(intarray[ii] == i)
              intArrRandomString.add(countRandomString);
          }
          else if(arrAllParam[i][1].equals("Unique Number"))
          {
            if(tempI == i)
            {
              countUniqueNumber++;
              tempI++;
            }
            if(intarray[ii] == i)
              intArrUniqueNumber.add(countUniqueNumber);
          }
          else if(arrAllParam[i][1].equals("Indexed Dataset"))
          {
            if(tempI == i)
            {
              countIndexDataSet++;
              tempI++;
            }

            if(intarray[ii] == i)
              intArrIndexDataSet.add(countIndexDataSet);
          }
          else if(arrAllParam[i][1].equals("Cookie"))
          {
            if(tempI == i)
            {
              countCookie++;
              tempI++;
            }

            if(intarray[ii] == i)
              intArrCookie.add(countCookie);
          }
          else if(arrAllParam[i][1].trim().equals("XML"))
          {
            if(tempI == i)
            {
              countXml++;
              tempI++;
            }

            if(intarray[ii] == i)
              intArrXml.add(countXml);
          }
          else if(arrAllParam[i][1].trim().equals("Global Indexed DataSource"))
          {
            if(tempI == i)
            {
              countIndexedDataSource++;
              tempI++;
            }

            if(intarray[ii] == i)
              intArrIndexedDataSource.add(countIndexedDataSource);
          }
          else if(arrAllParam[i][1].trim().equals("Request Body"))
          {
            if(tempI == i)
            {
              countRequestBody++;
              tempI++;
            }

            if(intarray[ii] == i)
              intArrRequest.add(countRequestBody);
          }
        }
      }

      //getting arraylist size of each parameter
      //convert into array and that array pass to delete the parameter
      if(intArrSearchBody.size() > 0)
      {
        Object[] obj = intArrSearchBody.toArray();
        int intTemp[] = new int[intArrSearchBody.size()];
        String arrDataValues[][] = getSearchVarByURL(hostName, serviceName, URL, 1, 0);
        for(int kk = 0; kk < obj.length; kk++)
        {
          intTemp[kk] = Integer.parseInt(arrDataValues[Integer.parseInt(obj[kk].toString())][7]);
          //System.out.println(intTemp[kk]  + " = Response Body");
        }
        flag = updateServiceObj(hostName, serviceName, URL, SEARCH_VAR, "delete", intTemp, null);
      }

      if(intArrScratch.size() > 0)
      {
        Object[] obj = intArrScratch.toArray();
        int intTemp[] = new int[intArrScratch.size()];

        for(int kk = 0; kk < obj.length; kk++)
        {
          intTemp[kk] = Integer.parseInt(obj[kk].toString());
          //System.out.println(intTemp[kk]  + " = Scratch");
        }
        flag = updateServiceObj(hostName, serviceName, URL, DECLARE_VAR, "delete", intTemp, null);
      }

      if(intArrDataset.size() > 0)
      {
        Object[] obj = intArrDataset.toArray();
        int intTemp[] = new int[intArrDataset.size()];

        String arrDataValues[][] = getFileParameterByURL(hostName, serviceName, URL, 1, 0);
        for(int kk = 0; kk < obj.length; kk++)
        {
          intTemp[kk] = Integer.parseInt(arrDataValues[Integer.parseInt(obj[kk].toString())][8]);
          //System.out.println(intTemp[kk]  + " = DataSet");
        }
        flag = updateServiceObj(hostName, serviceName, URL, FILE_PARAMETERS, "delete", intTemp, null);
      }

      //System.out.println(intArrDateTime.size() + "  ");
      if(intArrDateTime.size() > 0)
      {
        Object[] obj = intArrDateTime.toArray();
        int intTemp[] = new int[intArrDateTime.size()];
        String arrDataValues[][] = getDateTimeParameterByURL(hostName, serviceName, URL, 1, 0);
        for(int kk = 0; kk < obj.length; kk++)
        {
          intTemp[kk] = Integer.parseInt(arrDataValues[Integer.parseInt(obj[kk].toString())][4]);
          //System.out.println(intTemp[kk]  + " = Date Time");
        }
        flag = updateServiceObj(hostName, serviceName, URL, DATE_TIME_PARAMETER, "delete", intTemp, null);
      }

      if(intArrQuery.size() > 0)
      {
        Object[] obj = intArrQuery.toArray();
        int intTemp[] = new int[intArrQuery.size()];
        String arrDataValues[][] = getQueryVarByURL(hostName, serviceName, URL, 1, 0);
        for(int kk = 0; kk < obj.length; kk++)
        {
          intTemp[kk] = Integer.parseInt(arrDataValues[Integer.parseInt(obj[kk].toString())][4]);
          //System.out.println(intTemp[kk]  + " = Query Param");
        }
        flag = updateServiceObj(hostName, serviceName, URL, QUERY_VAR, "delete", intTemp, null);
      }

      if(intArrRandomNumber.size() > 0)
      {
        Object[] obj = intArrRandomNumber.toArray();
        int intTemp[] = new int[intArrRandomNumber.size()];

        String arrDataValues[][] = getRandomNumberByURL(hostName, serviceName, URL, 1, 0);
        for(int kk = 0; kk < obj.length; kk++)
        {
          intTemp[kk] = Integer.parseInt(arrDataValues[Integer.parseInt(obj[kk].toString())][5]);
          //System.out.println(intTemp[kk]  + " = Randam Number");
        }
        flag = updateServiceObj(hostName, serviceName, URL, RANDOM_NUMBER, "delete", intTemp, null);
      }

      if(intArrRandomString.size() > 0)
      {
        Object[] obj = intArrRandomString.toArray();
        int intTemp[] = new int[intArrRandomString.size()];

        String arrDataValues[][] = getRandomStringByURL(hostName, serviceName, URL, 1, 0);
        for(int kk = 0; kk < obj.length; kk++)
        {
          intTemp[kk] = Integer.parseInt(arrDataValues[Integer.parseInt(obj[kk].toString())][5]);
          //System.out.println(intTemp[kk]  + " = Randam String");
        }
        flag = updateServiceObj(hostName, serviceName, URL, RANDOM_STRING, "delete", intTemp, null);
      }

      if(intArrUniqueNumber.size() > 0)
      {
        Object[] obj = intArrUniqueNumber.toArray();
        int intTemp[] = new int[intArrUniqueNumber.size()];

        String arrDataValues[][] = getUniqueNumberByURL(hostName, serviceName, URL, 1, 0);
        for(int kk = 0; kk < obj.length; kk++)
        {
          intTemp[kk] = Integer.parseInt(arrDataValues[Integer.parseInt(obj[kk].toString())][3]);
          //System.out.println(intTemp[kk]  + " = Unique number");
        }
        flag = updateServiceObj(hostName, serviceName, URL, UNIQUE_NUMBER, "delete", intTemp, null);
      }

      if(intArrIndexDataSet.size() > 0)
      {
        Object[] obj = intArrIndexDataSet.toArray();
        int intTemp[] = new int[intArrIndexDataSet.size()];

        String arrDataValues[][] = getIndexFileParameterByURL(hostName, serviceName, URL, 1, 0);
        for(int kk = 0; kk < obj.length; kk++)
        {
          intTemp[kk] = Integer.parseInt(arrDataValues[Integer.parseInt(obj[kk].toString())][7]);
          //System.out.println(intTemp[kk]  + " = Index Data set");
        }
        flag = updateServiceObj(hostName, serviceName, URL, INDEX_FILE_PARAMETER, "delete", intTemp, null);
      }
      if(intArrCookie.size() > 0)
      {
        Object[] obj = intArrCookie.toArray();
        int intTemp[] = new int[intArrCookie.size()];
        String arrDataValues[][] = getCookieVarByURL(hostName, serviceName, URL, 1, 0);
        for(int kk = 0; kk < obj.length; kk++)
        {
          intTemp[kk] = Integer.parseInt(arrDataValues[Integer.parseInt(obj[kk].toString())][10]);
          //System.out.println(intTemp[kk]  + " = Cookie");
        }
        flag = updateServiceObj(hostName, serviceName, URL, COOKIE_VAR, "delete", intTemp, null);
      }
      if(intArrXml.size() > 0)
      {
        Object[] obj = intArrXml.toArray();
        int intTemp[] = new int[intArrXml.size()];
        String arrDataValues[][] = getXMLParametersByURL(hostName, serviceName, URL, 1, 0);

        for(int kk = 0; kk < obj.length; kk++)
        {
          intTemp[kk] = Integer.parseInt(arrDataValues[Integer.parseInt(obj[kk].toString())][7]);
          //System.out.println(intTemp[kk]  + " = Xml");
        }
        flag = updateServiceObj(hostName, serviceName, URL, XML_PARAMETER, "delete", intTemp, null);
      }
      if(intArrScratchArray.size() > 0)
      {
        Object[] obj = intArrScratchArray.toArray();
        int intTemp[] = new int[intArrScratchArray.size()];
        String arrDataValues[][] = getScratchArrayByURL(hostName, serviceName, URL, 1, 0);

        for(int kk = 0; kk < obj.length; kk++)
        {
          intTemp[kk] = Integer.parseInt(arrDataValues[Integer.parseInt(obj[kk].toString())][3]);
          //System.out.println(intTemp[kk]  + " = Scratch array");
        }
        flag = updateServiceObj(hostName, serviceName, URL, DECLARE_ARRAY_VAR, "delete", intTemp, null);
      }
      if(intArrIndexedDataSource.size() > 0)
      {
        Object[] obj = intArrIndexedDataSource.toArray();
        int intTemp[] = new int[intArrIndexedDataSource.size()];
        String arrDataValues[][] = getIndexedDataSourceByURL(hostName, serviceName, URL, 1, 0);

        for(int kk = 0; kk < obj.length; kk++)
        {
          intTemp[kk] = Integer.parseInt(arrDataValues[Integer.parseInt(obj[kk].toString())][3]);
          //System.out.println(intTemp[kk]  + " = Scratch array");
        }
        flag = updateServiceObj(hostName, serviceName, URL, GLOBAL_INDEXED_DATA_SOURCE, "delete", intTemp, null);
      }
      if(intArrRequest.size() > 0)
      {
        Object[] obj = intArrRequest.toArray();
        int intTemp[] = new int[intArrRequest.size()];
        String arrDataValues[][] = getRequestParametersByURL(hostName, serviceName, URL, 1, 0);

        for(int kk = 0; kk < obj.length; kk++)
        {
          intTemp[kk] = Integer.parseInt(arrDataValues[Integer.parseInt(obj[kk].toString())][11]);
          //System.out.println(intTemp[kk]  + " = Scratch array");
        }
        flag = updateServiceObj(hostName, serviceName, URL, REQUEST_VAR, "delete", intTemp, null);
      }
      //countIndexedDataSource + intArrIndexedDataSource
      return true;
    }
    catch(Exception e)
    {
      // TODO: handle exception
      //System.out.println("Exception = " +e);
      Log.stackTraceLog(className, "deleteAllParameter", "", "", "Exception - ", e);
      return false;
    }
  }

  /*
   *    * Method to get 2D array. which will have all parameter names, their types and specifications
   *       * @URL - will define the service. using URL we can find the file where we will get the service objects
   *          */
  public String[][] getAllParameters(String hostName, String serviceName, String URL, int sortOnCol, int sortPrefrence)
  {

    String[][] searchVar = getSearchVarByURL(hostName, serviceName, URL, sortOnCol, sortPrefrence);
    String[][] listOfSearchParameters = new String[searchVar.length][3];
    for(int sv = 0; sv < searchVar.length; sv++)
    {
      listOfSearchParameters[sv][0] = searchVar[sv][0];
      listOfSearchParameters[sv][1] = "Search";
      String strSpecRequestParam = "Prefix=" + searchVar[sv][1] + ", Suffix=" + searchVar[sv][2] + ", Occurrence=" + searchVar[sv][3] + ", StartOffset=" + searchVar[sv][4] + ", MaxLength=" + searchVar[sv][5] + ", Transform=" + searchVar[sv][6] + ", IgnoreCase=" + searchVar[sv][8];
      listOfSearchParameters[sv][2] = strSpecRequestParam;
    }

    String[][] queryVar = getQueryVarByURL(hostName, serviceName, URL, sortOnCol, sortPrefrence);
    String[][] listOfQueryParameters = new String[queryVar.length][3];
    for(int sv = 0; sv < queryVar.length; sv++)
    {
      listOfQueryParameters[sv][0] = queryVar[sv][0];
      listOfQueryParameters[sv][1] = "Query";
      String specQueryParam = "Query=" + queryVar[sv][1] + ", ORD=" + (queryVar[sv][2].toString().equals("")?"1":queryVar[sv][2]) + ", Method=" + (queryVar[sv][3].toString().equals("")?"None":queryVar[sv][3]);
      ;
      listOfQueryParameters[sv][2] = specQueryParam;
    }

    String[][] scratchVar = getScratchVarByURL(hostName, serviceName, URL);
    String[][] listOfScratchParameters = new String[scratchVar.length][3];
    for(int sv = 0; sv < scratchVar.length; sv++)
    {
      listOfScratchParameters[sv][0] = scratchVar[sv][0];
      listOfScratchParameters[sv][1] = "Scratch";
      String specScratchParam = "";
      if(scratchVar[sv][1].trim().equals(""))
        specScratchParam = "NA";
      else
        specScratchParam = "DefaultValue=" + scratchVar[sv][1];

      listOfScratchParameters[sv][2] = specScratchParam;
    }
    String[][] scratchArray = getScratchArrayByURL(hostName, serviceName, URL, 1, 0);
    String[][] listOfScratchArrayParameters = new String[scratchArray.length][3];
    for(int sv = 0; sv < scratchArray.length; sv++)
    {
      listOfScratchArrayParameters[sv][0] = scratchArray[sv][0];
      listOfScratchArrayParameters[sv][1] = "Scratch Array";
      String specScratchParam = "";

      if(scratchArray[sv][2].trim().equals(""))
        specScratchParam = "DefaultValue=NA";
      else
        specScratchParam = "DefaultValue=" + scratchArray[sv][2];

      listOfScratchArrayParameters[sv][2] = "Size=" + scratchArray[sv][1] + ", " + specScratchParam;
    }

    String[][] datasetVar = getFileParameterByURL(hostName, serviceName, URL, sortOnCol, sortPrefrence);
    String[][] listOfDatasetParameters = new String[datasetVar.length][3];
    for(int sv = 0; sv < datasetVar.length; sv++)
    {
      listOfDatasetParameters[sv][0] = datasetVar[sv][0];
      listOfDatasetParameters[sv][1] = "Dataset";
      String specDatasetParam = "Filename=" + datasetVar[sv][1] + ", HeaderLine=" + datasetVar[sv][7] + ", FirstDataLine=" + datasetVar[sv][5] + ", ColumnDelimeter=" + datasetVar[sv][6] + ", UpdateOn=" + datasetVar[sv][2] + ", RowSelectionMode=" + datasetVar[sv][3] + ", ParameterValue(s)=" + datasetVar[sv][4];
      listOfDatasetParameters[sv][2] = specDatasetParam;
    }

    String[][] indexedDatasetVar = getIndexFileParameterByURL(hostName, serviceName, URL, sortOnCol, sortPrefrence);
    String[][] listOfIndexedDatasetParameters = new String[indexedDatasetVar.length][3];
    for(int sv = 0; sv < indexedDatasetVar.length; sv++)
    {
      listOfIndexedDatasetParameters[sv][0] = indexedDatasetVar[sv][0];
      listOfIndexedDatasetParameters[sv][1] = "Indexed Dataset";
      String specIndexParam = "Index=" + indexedDatasetVar[sv][2] + ", Filename =" + indexedDatasetVar[sv][1] + ", HeaderLine=" + indexedDatasetVar[sv][6] + ", FirstDataLine=" + indexedDatasetVar[sv][4] + ", ColumnDelimeter=" + indexedDatasetVar[sv][5] + ", ParameterValue(s)=" + indexedDatasetVar[sv][3];
      listOfIndexedDatasetParameters[sv][2] = specIndexParam;
    }

    String[][] dateTimeVar = getDateTimeParameterByURL(hostName, serviceName, URL, sortOnCol, sortPrefrence);
    String[][] listOfDateTimeParameters = new String[dateTimeVar.length][3];
    for(int sv = 0; sv < dateTimeVar.length; sv++)
    {
      listOfDateTimeParameters[sv][0] = dateTimeVar[sv][0];
      listOfDateTimeParameters[sv][1] = "Date Time";
      String[][] dateValues = rptUtilsBean.getDateFormatArray();
      String formatExp = "";

      for(int jj = 0; jj < dateValues.length; jj++)
      {
        String value = dateTimeVar[sv][1];
        value = value.replaceAll("\"", "");
        value = value.replaceAll("&quot;", "");
        if(dateValues[jj][0].equals(value))
        {
          formatExp = dateValues[jj][1];
        }
      }
      String specDatetimeParam = "Format=" + dateTimeVar[sv][1] + ", Sample=" + formatExp + ", Offset=" + dateTimeVar[sv][2] + ", UpdateOn=" + dateTimeVar[sv][3];
      listOfDateTimeParameters[sv][2] = specDatetimeParam;
    }

    String[][] randomNumberVar = getRandomNumberByURL(hostName, serviceName, URL, sortOnCol, sortPrefrence);
    String[][] listOfRandomNumberParameters = new String[randomNumberVar.length][3];
    for(int sv = 0; sv < randomNumberVar.length; sv++)
    {
      listOfRandomNumberParameters[sv][0] = randomNumberVar[sv][0];
      listOfRandomNumberParameters[sv][1] = "Random Number";
      String specRandomNumParam = "Minimum=" + randomNumberVar[sv][1] + ", Maximum=" + randomNumberVar[sv][2] + ", Format=" + randomNumberVar[sv][3] + ", UpdateOn=" + randomNumberVar[sv][4];
      listOfRandomNumberParameters[sv][2] = specRandomNumParam;
    }

    String[][] randomStringVar = getRandomStringByURL(hostName, serviceName, URL, sortOnCol, sortPrefrence);
    String[][] listOfRandomStringParameters = new String[randomStringVar.length][3];
    for(int sv = 0; sv < randomStringVar.length; sv++)
    {
      listOfRandomStringParameters[sv][0] = randomStringVar[sv][0];
      listOfRandomStringParameters[sv][1] = "Random String";
      String specRandomStringParam = "Minimum=" + randomStringVar[sv][1] + ", Maximum=" + randomStringVar[sv][2] + ", CharacterSet=" + randomStringVar[sv][3] + ", UpdateOn=" + randomStringVar[sv][4];
      // if(randomStringVar[sv].length > 6)
      // {
      //   specRandomStringParam = specRandomStringParam + ", Enable Encoding=" + randomStringVar[sv][6];
      // }
      listOfRandomStringParameters[sv][2] = specRandomStringParam;
    }

    String[][] uniqueVar = getUniqueNumberByURL(hostName, serviceName, URL, sortOnCol, sortPrefrence);
    String[][] listOfUniqueNumberParameters = new String[uniqueVar.length][3];
    for(int sv = 0; sv < uniqueVar.length; sv++)
    {
      listOfUniqueNumberParameters[sv][0] = uniqueVar[sv][0];
      listOfUniqueNumberParameters[sv][1] = "Unique Number";
      String specUniqueNumberParam = "Format=" + uniqueVar[sv][1] + ", UpdateOn=" + uniqueVar[sv][2];
      listOfUniqueNumberParameters[sv][2] = specUniqueNumberParam;
    }

    String[][] cookieVar = getCookieVarByURL(hostName, serviceName, URL, sortOnCol, sortPrefrence);
    String[][] listOfCookieParameters = new String[cookieVar.length][3];

    for(int sv = 0; sv < cookieVar.length; sv++)
    {
      listOfCookieParameters[sv][0] = cookieVar[sv][0];
      listOfCookieParameters[sv][1] = "Cookie";
      String decodeValue = cookieVar[sv][9];
      if(decodeValue.trim().toLowerCase().equals("yes"))
        decodeValue = "Decode";
      else
        decodeValue = "Do not decode";

      String EncodeValue = cookieVar[sv][8];
      if(EncodeValue.trim().toLowerCase().equals("all"))
        EncodeValue = "Encode All character";
      else if(EncodeValue.trim().toLowerCase().equals("specified"))
        EncodeValue = "Encode specified character";
      else
        EncodeValue = "Do not encode";

      String ActionValue = cookieVar[sv][5];
      if(ActionValue.trim().toLowerCase().equals("warning"))
        ActionValue = "Log massage";
      else
        ActionValue = "Do not log massage";

      String specCookieParam = "CookieName=" + cookieVar[sv][1] + ", StartOffset=" + cookieVar[sv][2] + ", MaxLength=" + cookieVar[sv][3] + ", DecodeCookieValue=" + decodeValue + ", Transform=" + cookieVar[sv][7] + ", EncodeInResponseTemplate=" + EncodeValue + ", ActionOnNotFound=" + ActionValue + ", DefaultValue=" + cookieVar[sv][6];
      listOfCookieParameters[sv][2] = specCookieParam;
    }

    String[][] xmlVar = getXMLParametersByURL(hostName, serviceName, URL, sortOnCol, sortPrefrence);
    String[][] listOfxmlParameters = new String[xmlVar.length][3];
    for(int sv = 0; sv < xmlVar.length; sv++)
    {
      listOfxmlParameters[sv][0] = xmlVar[sv][0];
      listOfxmlParameters[sv][1] = "XML";
      String valueType = "";
      if(xmlVar[sv][2].equals("<>"))
        valueType = "Node Value";
      else if(!xmlVar[sv][2].equals(""))
      {
        String attName = xmlVar[sv][2];
        attName = attName.substring(1, attName.length() - 1);
        valueType = "Attribute Value, AttributeName=" + attName;
      }

      String whereClause = ", NodeSelectionCriteria=None";
      if(!xmlVar[sv][3].equals(""))
      {
        String[] arrClause = rptUtilsBean.split(xmlVar[sv][3], "|");
        whereClause = ", NodeSelectionCriteria=";
        for(int jk = 0; jk < arrClause.length; jk++)
        {
          if(jk == 0)
            whereClause = whereClause + arrClause[jk];
          else
            whereClause = whereClause + " AND " + arrClause[jk];
        }
      }

      String specXMLParam = "NodePath=" + FileBean.escapeHTML(xmlVar[sv][1]) + ", Occurrence=" + xmlVar[sv][4] + ", Conversion=" + xmlVar[sv][5] + ", ValueType=" + FileBean.escapeHTML(valueType) + FileBean.escapeHTML(whereClause);
      listOfxmlParameters[sv][2] = specXMLParam;
    }

    String[][] indexedDataSourceVar = getIndexedDataSourceByURL(hostName, serviceName, URL, sortOnCol, sortPrefrence);
    String[][] listOfIndexedDataSourceParameters = new String[indexedDataSourceVar.length][3];
    for(int sv = 0; sv < indexedDataSourceVar.length; sv++)
    {
      listOfIndexedDataSourceParameters[sv][0] = indexedDataSourceVar[sv][2];
      listOfIndexedDataSourceParameters[sv][1] = "Global Indexed DataSource";
      String specIndexedDataSource = "Dataset=" + indexedDataSourceVar[sv][0] + ", Index Parameter=" + indexedDataSourceVar[sv][1];
      listOfIndexedDataSourceParameters[sv][2] = specIndexedDataSource;
    }
    
    String[][] requestVar = getRequestParametersByURL(hostName, serviceName, URL, sortOnCol, sortPrefrence);
    String[][] listOfRequestParameters = new String[requestVar.length][3];
    for(int sv = 0 ; sv < requestVar.length ; sv++)
    {
      listOfRequestParameters[sv][0] = requestVar[sv][0];
      listOfRequestParameters[sv][1] = "Request Body";
      String decodeValue = requestVar[sv][8];
      if(decodeValue.trim().toLowerCase().equals("yes"))
        decodeValue = "Decode";
      else
        decodeValue = "Do not decode";

      String EncodeValue = requestVar[sv][9];
      
      if(EncodeValue.trim().toLowerCase().equals("all"))
        EncodeValue = "Encode All character";
      else if(EncodeValue.trim().toLowerCase().equals("specified"))
        EncodeValue = "Encode specified character";
      else
        EncodeValue = "Do not encode";

      String ActionValue = requestVar[sv][5];
      if(ActionValue.trim().toLowerCase().equals("warning"))
        ActionValue = "Log massage";
      else
        ActionValue = "Do not log massage";
      
      String specRequestParam = "AttributeName=" + requestVar[sv][1] + ", Occurrence=" + requestVar[sv][2] + ", StartOffset=" 
      + requestVar[sv][3] + ", MaxLength=" + requestVar[sv][4] + ", ActionOnNotFound=" + ActionValue + ", DefaultValue=" + requestVar[sv][6] + 
      ", Transform=" + requestVar[sv][7] + ", DecodeAttributeValue=" + decodeValue + ", EncodeInResponseTemplate=" + EncodeValue;
      listOfRequestParameters[sv][2] = specRequestParam;
     
    }

    int lgth = listOfSearchParameters.length + listOfQueryParameters.length + listOfScratchParameters.length + listOfDatasetParameters.length + listOfIndexedDatasetParameters.length + listOfDateTimeParameters.length + listOfRandomNumberParameters.length + listOfRandomStringParameters.length + listOfUniqueNumberParameters.length + listOfCookieParameters.length + listOfxmlParameters.length + listOfScratchArrayParameters.length + listOfIndexedDataSourceParameters.length + listOfRequestParameters.length;
    String[][] listOfAllParameters = new String[lgth][3];
    int ctr = 0;

    for(int jk = 0; jk < listOfSearchParameters.length; jk++)
    {
      listOfAllParameters[ctr][0] = listOfSearchParameters[jk][0];
      listOfAllParameters[ctr][1] = listOfSearchParameters[jk][1];
      listOfAllParameters[ctr][2] = listOfSearchParameters[jk][2];
      ctr++;
    }

    for(int jk = 0; jk < listOfScratchArrayParameters.length; jk++)
    {
      listOfAllParameters[ctr][0] = listOfScratchArrayParameters[jk][0];
      listOfAllParameters[ctr][1] = listOfScratchArrayParameters[jk][1];
      listOfAllParameters[ctr][2] = listOfScratchArrayParameters[jk][2];
      ctr++;
    }

    for(int jk = 0; jk < listOfQueryParameters.length; jk++)
    {
      listOfAllParameters[ctr][0] = listOfQueryParameters[jk][0];
      listOfAllParameters[ctr][1] = listOfQueryParameters[jk][1];
      listOfAllParameters[ctr][2] = listOfQueryParameters[jk][2];
      ctr++;
    }

    for(int jk = 0; jk < listOfScratchParameters.length; jk++)
    {
      listOfAllParameters[ctr][0] = listOfScratchParameters[jk][0];
      listOfAllParameters[ctr][1] = listOfScratchParameters[jk][1];
      listOfAllParameters[ctr][2] = listOfScratchParameters[jk][2];
      ctr++;
    }

    for(int jk = 0; jk < listOfDatasetParameters.length; jk++)
    {
      listOfAllParameters[ctr][0] = listOfDatasetParameters[jk][0];
      listOfAllParameters[ctr][1] = listOfDatasetParameters[jk][1];
      listOfAllParameters[ctr][2] = listOfDatasetParameters[jk][2];
      ctr++;
    }

    for(int jk = 0; jk < listOfIndexedDatasetParameters.length; jk++)
    {
      listOfAllParameters[ctr][0] = listOfIndexedDatasetParameters[jk][0];
      listOfAllParameters[ctr][1] = listOfIndexedDatasetParameters[jk][1];
      listOfAllParameters[ctr][2] = listOfIndexedDatasetParameters[jk][2];
      ctr++;
    }

    for(int jk = 0; jk < listOfDateTimeParameters.length; jk++)
    {
      listOfAllParameters[ctr][0] = listOfDateTimeParameters[jk][0];
      listOfAllParameters[ctr][1] = listOfDateTimeParameters[jk][1];
      listOfAllParameters[ctr][2] = listOfDateTimeParameters[jk][2];
      ctr++;
    }

    for(int jk = 0; jk < listOfRandomNumberParameters.length; jk++)
    {
      listOfAllParameters[ctr][0] = listOfRandomNumberParameters[jk][0];
      listOfAllParameters[ctr][1] = listOfRandomNumberParameters[jk][1];
      listOfAllParameters[ctr][2] = listOfRandomNumberParameters[jk][2];
      ctr++;
    }

    for(int jk = 0; jk < listOfRandomStringParameters.length; jk++)
    {
      listOfAllParameters[ctr][0] = listOfRandomStringParameters[jk][0];
      listOfAllParameters[ctr][1] = listOfRandomStringParameters[jk][1];
      listOfAllParameters[ctr][2] = listOfRandomStringParameters[jk][2];
      ctr++;
    }

    for(int jk = 0; jk < listOfUniqueNumberParameters.length; jk++)
    {
      listOfAllParameters[ctr][0] = listOfUniqueNumberParameters[jk][0];
      listOfAllParameters[ctr][1] = listOfUniqueNumberParameters[jk][1];
      listOfAllParameters[ctr][2] = listOfUniqueNumberParameters[jk][2];
      ctr++;
    }

    for(int jk = 0; jk < listOfCookieParameters.length; jk++)
    {
      listOfAllParameters[ctr][0] = listOfCookieParameters[jk][0];
      listOfAllParameters[ctr][1] = listOfCookieParameters[jk][1];
      listOfAllParameters[ctr][2] = listOfCookieParameters[jk][2];
      ctr++;
    }

    for(int jk = 0; jk < listOfxmlParameters.length; jk++)
    {
      listOfAllParameters[ctr][0] = listOfxmlParameters[jk][0];
      listOfAllParameters[ctr][1] = listOfxmlParameters[jk][1];
      listOfAllParameters[ctr][2] = listOfxmlParameters[jk][2];
      ctr++;
    }

    for(int jk = 0; jk < listOfIndexedDataSourceParameters.length; jk++)
    {
      listOfAllParameters[ctr][0] = listOfIndexedDataSourceParameters[jk][0];
      listOfAllParameters[ctr][1] = listOfIndexedDataSourceParameters[jk][1];
      listOfAllParameters[ctr][2] = listOfIndexedDataSourceParameters[jk][2];
      ctr++;
    }
    
    for(int jk = 0; jk < listOfRequestParameters.length; jk++)
    {
      listOfAllParameters[ctr][0] = listOfRequestParameters[jk][0];
      listOfAllParameters[ctr][1] = listOfRequestParameters[jk][1];
      listOfAllParameters[ctr][2] = listOfRequestParameters[jk][2];
      ctr++;
    }
    //listOfAllParameters = sortArray(listOfAllParameters, sortOnCol, sortPrefrence);
    return listOfAllParameters;
  }

  public String getSampleValueByNumberFormat(String format)
  {
    if(format.equalsIgnoreCase("%01lu"))
      return "1";
    else if(format.equalsIgnoreCase("%02lu"))
      return "01";
    else if(format.equalsIgnoreCase("%03lu"))
      return "001";
    else if(format.equalsIgnoreCase("%04lu"))
      return "0001";
    else if(format.equalsIgnoreCase("%05lu"))
      return "00001";
    else if(format.equalsIgnoreCase("%06lu"))
      return "000001";
    else if(format.equalsIgnoreCase("%07lu"))
      return "0000001";
    else if(format.equalsIgnoreCase("%08lu"))
      return "00000001";
    else if(format.equalsIgnoreCase("%09lu"))
      return "000000001";
    else
      return "01";

  }

  /*
   * This function is used to delete the response template
   */
  public boolean deleteTemplate(String hostName, String serviceName, String URL, String[] templateName)
  {
    Log.debugLog(className, "deleteTemplate", "", "", "Method started service=" + serviceName);

    String[][] arrDataValues = getResponseTemplateInfoByURL(hostName, serviceName, URL);
    boolean flag = false;
    //fetch the all related value with the template, which is required for delete the template
    for(int i = 0; i < templateName.length; i++)
    {
      Log.debugLog(className, "deleteTemplate", "", "", "get the respective detail of template = " + templateName[i]);
      for(int j = 0; j < arrDataValues.length; j++)
      {
        if(arrDataValues[j][0].equals(templateName[i]))
        {
          String strResponseFileName = arrDataValues[j][2];
          String strConditionalTemp = arrDataValues[j][6];
          StringBuffer strBufResponse = getRequestResponseFileContents(hostName, serviceName, URL, "response", templateName[i]);
          StringBuffer strBufRequest = getRequestResponseFileContents(hostName, serviceName, URL, "request", templateName[i]);
          String strActiveIn = arrDataValues[j][5];
          String TemplateType = arrDataValues[j][1];
          flag = updateResponseTemplate(hostName, serviceName, URL, "delete", templateName[i], "", strResponseFileName, strConditionalTemp, strBufResponse, strBufRequest, strActiveIn, TemplateType);
          flag = updateTemplateBasedHttpHeaders(hostName, serviceName, URL, templateName[i], templateName[i], "");
          flag = updateTemplateBasedStatusCode(hostName, serviceName, URL, templateName[i], templateName[i], "", "");

          Log.debugLog(className, "deleteTemplate", "", "", "template name' " + templateName[i] + "has deleted");
          break;
        }
      }
    }
    Log.debugLog(className, "deleteTemplate", "", "", " template deleted");
    return flag;
  }

  /*
   * This function is used to active or inactive the response template
   */
  public boolean activeOrInactiveTemplate(String hostName, String serviceName, String URL, String[] templateName, String stractive)
  {
    Log.debugLog(className, "activeOrInactiveTemplate", "", "", "Method started service=" + serviceName);
    boolean flag = false;
    String[][] arrDataValues = getResponseTemplateInfoByURL(hostName, serviceName, URL);

    //fetch the all related value with the template, which is required for update the template
    for(int i = 0; i < templateName.length; i++)
    {
      Log.debugLog(className, "activeOrInactiveTemplate", "", "", "get the respective detail of template = " + templateName[i]);
      for(int j = 0; j < arrDataValues.length; j++)
      {
        if(arrDataValues[j][0].equals(templateName[i]))
        {
          String strResponseFileName = arrDataValues[j][2];
          String strConditionalTemp = arrDataValues[j][6];
          String extension = arrDataValues[j][4];
          StringBuffer strBufResponse = getRequestResponseFileContents(hostName, serviceName, URL, "response", templateName[i]);
          StringBuffer strBufRequest = getRequestResponseFileContents(hostName, serviceName, URL, "request", templateName[i]);
          String TemplateType = arrDataValues[j][1];
          String strActiveIn = stractive;

          flag = updateResponseTemplate(hostName, serviceName, URL, "update", templateName[i], templateName[i], extension, strConditionalTemp, strBufResponse, null, strActiveIn, TemplateType);
          Log.debugLog(className, "activeOrInactiveTemplate", "", "", "template name' " + templateName[i] + "has updated");
          break;
        }
      }
    }
    Log.debugLog(className, "activeOrInactiveTemplate", "", "", " template updated");
    return flag;
  }

  /*
   * Method to update or delete any response template.
   * @URL - will define the service. using URL we can find the file where we will get the service objects
   * @operation - it can be 'delete' or 'update'
   * @oldTemplateName - it is the template name which is dfined earlier
   * @newTemplateName - it is new template name in case of update. for operation delete it will be blank
   * @condition - it is the changed condition in case of update. for operation delete it will be blank.
   */
  public boolean updateResponseTemplate(String hostName, String serviceName, String URL, String operation, String oldTemplateName, String newTemplateName, String responseExtension, String condition, StringBuffer responseFileContents, StringBuffer requestFileContents, String activeInactive, String TemplateType)
  {
    Log.debugLog(className, "updateResponseTemplate", "", "", "Method started service=" + serviceName + ", Template=" + oldTemplateName + ", operation=" + operation);
    try
    {
      if(TemplateType.equals(""))
        TemplateType = "FileBased";

      if(condition.equals(""))
        condition = "NA";

      if(responseExtension.equals("None"))
        responseExtension = "";

      requestFileContents = getRequestResponseFileContents(hostName, serviceName, URL, "request", oldTemplateName);
      Vector completeControlFile = readFile(getServiceConfFilePath(hostName, serviceName), false);
      Vector modifiedVector = new Vector();
      boolean encode = isFileEncoded(hostName, serviceName, URL);
      if(completeControlFile == null)
        return false;

      String strSearch = "RESPONSE_TEMPLATE";
      String oldResponseFile = "";
      String newResponseFile = "";
      for(int yy = 0; yy < completeControlFile.size(); yy++)
      {
        String line = completeControlFile.elementAt(yy).toString().trim();

        if(line.startsWith(strSearch)) // Method found
        {
          String[] arrURLLine = rptUtilsBean.strToArrayData(line, " ");
          boolean matchToUpdate = false;
          if(arrURLLine.length < 2)
            continue;
          if(arrURLLine[1].equals(oldTemplateName))
          {
            matchToUpdate = true;
            if(operation.equals("update"))
            {
              newResponseFile = "";
              int oldResponseFileIndex = 2;
              if(!line.contains(" FileBased ") && !line.contains(" Forward ") && !line.contains(" RequestBased") && !line.contains(" Simulate"))
              {
                oldResponseFileIndex = 2;
              }
              else if(line.contains(" FileBased "))
              {
                oldResponseFileIndex = 3;
              }
              else if(line.contains(" Forward ") || line.contains(" Simulate"))
              {
                oldResponseFileIndex = 4;
              }
              else if(line.contains(" RequestBased"))
              {
                oldResponseFileIndex = 4;
              }

              oldResponseFile = arrURLLine[oldResponseFileIndex];

              if(arrURLLine[oldResponseFileIndex].lastIndexOf(".") > -1)
                newResponseFile = arrURLLine[oldResponseFileIndex].substring(0, arrURLLine[oldResponseFileIndex].lastIndexOf(".")) + responseExtension;
              else
                newResponseFile = arrURLLine[oldResponseFileIndex] + responseExtension;

              String strToUpdate = "";
              //System.out.println("###" + newResponseFile);
              if(!TemplateType.startsWith("RequestBased"))
                strToUpdate = "RESPONSE_TEMPLATE " + newTemplateName + " " + TemplateType + " " + newResponseFile + " " + activeInactive + " " + condition;
              else
                strToUpdate = "RESPONSE_TEMPLATE " + newTemplateName + " " + TemplateType + " " + newResponseFile + " " + activeInactive;

              modifiedVector.add(strToUpdate);
            }
            else
            {
              if(yy + 1 < completeControlFile.size())
              {
                String blankline = completeControlFile.elementAt(yy + 1).toString().trim();
                if(blankline.equals(""))
                {
                  yy++;
                }
              }
            }
          }

          if(!matchToUpdate)
            modifiedVector.add(line);
        }
        else
          modifiedVector.add(line);
      }

      Vector modifiedConf = new Vector();
      if(!writeToFile(getServiceConfFilePath(hostName, serviceName), modifiedVector, hostName, serviceName))
        return false;

      String responseFileName = oldResponseFile;
      if(!oldResponseFile.equals(newResponseFile))
      {
        File F1 = new File(getServicePath(hostName, serviceName) + oldResponseFile);
        if(F1.exists())
          F1.delete();

        File F2 = new File(getServicePath(hostName, serviceName) + oldResponseFile + ".req");
        if(F2.exists())
          F2.delete();
        responseFileName = newResponseFile;
      }
      //update response and request file contents
      File fileResponse = new File(getServicePath(hostName, serviceName) + responseFileName);
      if(!fileResponse.exists())
        fileResponse.createNewFile();

      File fileRequest = new File(getServicePath(hostName, serviceName) + responseFileName + ".req");
      if(!fileRequest.exists())
        fileRequest.createNewFile();

      FileOutputStream out1 = new FileOutputStream(fileResponse);
      PrintStream responseFile = new PrintStream(out1);
      responseFile.print(responseFileContents);
      responseFile.close();

      if(requestFileContents != null)
      {
        FileOutputStream out2 = new FileOutputStream(fileRequest);
        PrintStream requestFile = new PrintStream(out2);
        if(encode)
          requestFile.print(encodeString(requestFileContents.toString()));
        else
          requestFile.print(requestFileContents);
        requestFile.close();
      }

      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "updateResponseTemplate", "", "", "Exception - ", ex);
      return false;
    }
  }

  //method to get  DECODE_CONTENTS settings
  public int getDecodeSettings(String hostName, String serviceName, String URL)
  {
    Log.debugLog(className, "getDecodeSettings", "", "", "Method started");
    try
    {
      int decodeValue = 0;
      Vector completeControlFile = readFile(getServiceConfFilePath(hostName, serviceName), false);

      for(int yy = 0; yy < completeControlFile.size(); yy++)
      {
        String line = completeControlFile.elementAt(yy).toString().trim();

        if(line.startsWith("DECODE_CONTENTS"))
        {
          String[] arrURLLine = rptUtilsBean.strToArrayData(line, " ");
          decodeValue = Integer.parseInt(arrURLLine[1]);
        }
      }
      return decodeValue;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getDecodeSettings", "", "", "Exception - ", ex);
      return 0;
    }
  }
  
  /**
   *This method is used to add the tracelevel to the service.conf file
   *@param hostName
   *@param serviceName
   *@param URL
   *@param traceLevelValue
   *@return
   */
  public boolean updateTraceLevelSettings(String hostName, String serviceName, String URL, int traceLevelValue)
  {
    Log.debugLog(className, "updateTraceLevelSettings", "", "", "Method started");
    try
    {
      Vector completeControlFile = readFile(getServiceConfFilePath(hostName, serviceName), false);
      Vector modifiedVector = new Vector();

      boolean traceFound = false;
      for(int yy = 0; yy < completeControlFile.size(); yy++)
      {
        String line = completeControlFile.elementAt(yy).toString().trim();

        if(line.startsWith("HPD_DEBUG_TRACE"))
        {
          traceFound = true;
          modifiedVector.add("HPD_DEBUG_TRACE " + traceLevelValue);
        }
        else
          modifiedVector.add(line);
      }

      if(!traceFound)
        modifiedVector.add("HPD_DEBUG_TRACE " + traceLevelValue);

      if(!writeToFile(getServiceConfFilePath(hostName, serviceName), modifiedVector, hostName, serviceName))
        return false;

      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "updateTraceLevelSettings", "", "", "Exception - ", ex);
      return false;
    }
  }
   
   /**
    *This method is used to get the TraceLevelSettingsvalue 
    *@param hostName
    *@param serviceName
    *@param URL
    *@return String
    */
  public String getTraceLevelSettings(String hostName, String serviceName, String URL)
  {
    Log.debugLog(className, "getTraceLevelSettings", "", "", "Method started");
    try
    {
      String traceLevelValue = "";
      Vector completeControlFile = readFile(getServiceConfFilePath(hostName, serviceName), false);

      for(int yy = 0; yy < completeControlFile.size(); yy++)
      {
        String line = completeControlFile.elementAt(yy).toString().trim();
        if(line.startsWith("HPD_DEBUG_TRACE"))
        {
          String[] arrURLLine = rptUtilsBean.strToArrayData(line, " ");
          traceLevelValue = arrURLLine[1];
        }
      }
      return traceLevelValue;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getTraceLevelSettings", "", "", "Exception - ", ex);
      return "";
    }
  }

  //method to get keep alive timeout settings
  public String getKeepAliveTimeoutSettings(String hostName, String serviceName, String URL)
  {
    Log.debugLog(className, "getKeepAliveTimeoutSettings", "", "", "Method started");
    try
    {
      String keepaliveTimeOut = "450000";
      Vector completeControlFile = readFile(getServiceConfFilePath(hostName, serviceName), false);

      for(int yy = 0; yy < completeControlFile.size(); yy++)
      {
        String line = completeControlFile.elementAt(yy).toString().trim();

        if(line.startsWith("KEEP_ALIVE_TIMEOUT"))
        {
          String[] arrURLLine = rptUtilsBean.strToArrayData(line, " ");
          keepaliveTimeOut = arrURLLine[1];
        }
      }
      return keepaliveTimeOut;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getKeepAliveTimeoutSettings", "", "", "Exception - ", ex);
      return "0";
    }
  }

  //method to update DECODE_CONTENTS settings
  public boolean updateKeepAliveTimeoutSettings(String hostName, String serviceName, String URL, String keepAliveTimeout)
  {
    Log.debugLog(className, "updateKeepAliveTimeoutSettings", "", "", "Method started");
    try
    {
      Vector completeControlFile = readFile(getServiceConfFilePath(hostName, serviceName), false);
      Vector modifiedVector = new Vector();

      boolean decodeFound = false;
      for(int yy = 0; yy < completeControlFile.size(); yy++)
      {
        String line = completeControlFile.elementAt(yy).toString().trim();

        if(line.startsWith("KEEP_ALIVE_TIMEOUT"))
        {
          decodeFound = true;
          modifiedVector.add("KEEP_ALIVE_TIMEOUT " + keepAliveTimeout);
        }
        else
          modifiedVector.add(line);
      }

      if(!decodeFound)
        modifiedVector.add("KEEP_ALIVE_TIMEOUT " + keepAliveTimeout);

      if(!writeToFile(getServiceConfFilePath(hostName, serviceName), modifiedVector, hostName, serviceName))
        return false;

      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "updateKeepAliveTimeoutSettings", "", "", "Exception - ", ex);
      return false;
    }
  }

  //method to update DECODE_CONTENTS settings
  public boolean updateDecodeSettings(String hostName, String serviceName, String URL, int decodeValue)
  {
    Log.debugLog(className, "updateDecodeSettings", "", "", "Method started");
    try
    {
      Vector completeControlFile = readFile(getServiceConfFilePath(hostName, serviceName), false);
      Vector modifiedVector = new Vector();

      boolean decodeFound = false;
      for(int yy = 0; yy < completeControlFile.size(); yy++)
      {
        String line = completeControlFile.elementAt(yy).toString().trim();

        if(line.startsWith("DECODE_CONTENTS"))
        {
          decodeFound = true;
          modifiedVector.add("DECODE_CONTENTS " + decodeValue);
        }
        else
          modifiedVector.add(line);
      }

      if(!decodeFound)
        modifiedVector.add("DECODE_CONTENTS " + decodeValue);

      if(!writeToFile(getServiceConfFilePath(hostName, serviceName), modifiedVector, hostName, serviceName))
        return false;

      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "updateDecodeSettings", "", "", "Exception - ", ex);
      return false;
    }
  }
  
  /*
   * Method to check the response file is encoded or not. It will check 'DECODE_CONTENTS' in control file.if it is found and its value is 0 then it is encoded and return true otherwise it will return false.
   */
  public boolean isFileEncoded(String hostName, String serviceName, String URL)
  {
    Log.debugLog(className, "isFileEncoded", "", "", "Method started");
    Log.debugLog(className, "isFileEncoded", "", "", "host name = " + hostName);
    try
    {
      Vector hpdURLData = readFile(getServiceConfFilePath("default", serviceName), true);

      if((hpdURLData == null) || (hpdURLData.size() <= 0))
      {
        Log.debugLog(className, "isFileEncoded", "", "", "Service File not found, It may be corrupted. Service=" + serviceName);
        return false;
      }

      for(int k = 0; k < hpdURLData.size(); k++)
      {
        String[] arrURLLine = rptUtilsBean.strToArrayData(hpdURLData.elementAt(k).toString(), " ");
        if(arrURLLine.length <= 0)
          continue;
        if(arrURLLine[0].toUpperCase().equals("DECODE_CONTENTS"))
        {
          if(arrURLLine[1].equals("1"))
            return true;
          else
            return false;
        }
      }

      return false;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "isFileEncoded", "", "", "Exception - ", ex);
      return false;
    }
  }

  /*
   * method to get the contents of Response or Request File. User need to pass the URl operation name can be 'request' or 'response'
   */
  public StringBuffer getRequestResponseFileContents(String hostName, String serviceName, String URL, String objectName, String templateName)
  {
    Log.debugLog(className, "getRequestResponseFileContents", "", "", "Method started service=" + serviceName);
    boolean decode = isFileEncoded(hostName, serviceName, URL);
    StringBuffer sbRequestFile = new StringBuffer();
    try
    {
      String[][] arrDataValues = getResponseTemplateInfoByURL(hostName, serviceName, URL);
      String responseFile = "";

      for(int i = 0; i < arrDataValues.length; i++)
      {
        if(arrDataValues[i][0].equals(templateName))
        {
          responseFile = arrDataValues[i][2];
        }
      }

      if(responseFile.equals(""))
      {
        Log.debugLog(className, "getRequestResponseFileContents", "", "", "Response File name didn't found");
        return sbRequestFile;
      }

      Log.debugLog(className, "getRequestResponseFileContents", "", "", "Response File found responseFile = " + responseFile);

      if(objectName.equals("request"))
        responseFile = responseFile + ".req";
      else
      {
        decode = false;
      }

      if(!responseFile.equals(""))
      {
        Vector vecReqFile = readFile(getServicePath(hostName, serviceName) + responseFile, false);
        if(vecReqFile != null)
        {
          // String newLine = System.getProperty("line.separator");
          for(int ds = 0; ds < vecReqFile.size(); ds++)
          {
            if(!decode)
              sbRequestFile.append(vecReqFile.elementAt(ds).toString() + "\n");
            else
            {
              sbRequestFile.append(decodeString(vecReqFile.elementAt(ds).toString()) + "\n");
            }
          }
        }
      }

      return sbRequestFile;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getRequestResponseFileContents", "", "", "Exception - ", ex);
      return sbRequestFile;
    }
  }

  /*
   * method to get the comments about service.
   */
  public StringBuffer getServiceComments(String hostName, String serviceName, String URL)
  {
    Log.debugLog(className, "getServiceComments", "", "", "Method started service=" + serviceName);
    StringBuffer sbComments = new StringBuffer();
    try
    {
      Vector commentsFile = readFile(getServicePath(hostName, serviceName) + "service.comments", false);
      if(commentsFile == null)
      {
        Log.debugLog(className, "getServiceComments", "", "", "service.comments File not found, It may be corrupted. service=" + serviceName);
        return sbComments;
      }

      for(int ds = 0; ds < commentsFile.size(); ds++)
        sbComments.append(commentsFile.elementAt(ds).toString() + "\n");

      return sbComments;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getServiceComments", "", "", "Exception - ", ex);
      return sbComments;
    }
  }

  /*
   * method to update the comments about service.
   */
  public boolean updateServiceComments(String hostName, String serviceName, String URL, StringBuffer serviceComments)
  {
    Log.debugLog(className, "updateServiceComments", "", "", "Method started service=" + serviceName);
    try
    {
      File fileComments = new File(getServicePath(hostName, serviceName) + "service.comments");
      if(!fileComments.exists())
        fileComments.createNewFile();

      FileOutputStream out1 = new FileOutputStream(fileComments);
      PrintStream fileToUpdate = new PrintStream(out1);

      fileToUpdate.println(serviceComments.toString().replaceAll("\\r", ""));
      fileToUpdate.close();
      writeToLogFile(hostName, serviceName);
      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "updateServiceComments", "", "", "Exception - ", ex);
      return false;
    }
  }

  /*
   * method to update the contents of Response or Request File. User need to pass the URl operation name can be 'request' or
   * 'response' and template name
   */
  public boolean updateRequestResponseFileContents(String hostName, String serviceName, String URL, String objectName, String templateName, StringBuffer fileContents)
  {
    Log.debugLog(className, "updateRequestResponseFileContents", "", "", "Method started service=" + serviceName + ", objectName=" + objectName + ", templateName=" + templateName);
    boolean encode = isFileEncoded(hostName, serviceName, URL);
    try
    {
      Vector completeControlFile = readFile(getServiceConfFilePath(hostName, serviceName), false);
      if(completeControlFile == null)
      {
        Log.debugLog(className, "updateRequestResponseFileContents", "", "", "service File not found, It may be corrupted. service=" + serviceName);
        return false;
      }

      String[][] arrDataValues = getResponseTemplateInfoByURL(hostName, serviceName, URL);
      String responseFile = "";

      for(int i = 0; i < arrDataValues.length; i++)
      {
        if(arrDataValues[i][0].equals(templateName))
        {
          responseFile = arrDataValues[i][2];
        }
      }

      if(responseFile.equals(""))
      {
        Log.debugLog(className, "updateRequestResponseFileContents", "", "", "Response File name didn't found");
        return false;
      }

      Log.debugLog(className, "updateRequestResponseFileContents", "", "", "Response File found responseFile = " + responseFile);

      if(objectName.equals("request"))
        responseFile = responseFile + ".req";
      else
      {
        encode = false;
      }

      if(!responseFile.equals(""))
      {
        FileOutputStream out1 = new FileOutputStream(new File(getServicePath(hostName, serviceName) + responseFile));
        PrintStream fileToUpdate = new PrintStream(out1);
        if(!encode)
          fileToUpdate.print(fileContents.toString().replaceAll("\\r", ""));
        else
          fileToUpdate.print(encodeString(fileContents.toString()).replaceAll("\\r", ""));

        fileToUpdate.close();
      }
      writeToLogFile(hostName, serviceName);

      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "updateRequestResponseFileContents", "", "", "Exception - ", ex);
      return false;
    }
  }

  //method to check if service name is already exist in all hosts
  public boolean isServiceAlreadyExist(String serviceName)
  {
    Log.debugLog(className, "isServiceAlreadyExist", "", "", "Method started service=" + serviceName);
    try
    {
      String[] hostNames = getHOSTNames();
      for(int i = 0; i < hostNames.length; i++)
      {
        String[] serviceNames = getServicesByHostName(hostNames[i]);
        for(int j = 0; j < serviceNames.length; j++)
        {
          if(serviceNames[j].equals(serviceName))
            return true;
        }
      }
      return false;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "isServiceAlreadyExist", "", "", "Exception - ", ex);
      return false;
    }
  }

  public String isHOSTAndURLAlreadyExist(String hostName, String URL)
  {
    return isHOSTAndURLAlreadyExist(hostName, URL, "");
  }

  //method to check if host name and URL is already exit
  public String isHOSTAndURLAlreadyExist(String hostName, String URL, String IgnoreCase)
  {
    Log.debugLog(className, "isHOSTAndServiceAlreadyExist", "", "", "Method started host=" + hostName + ", URL=" + URL);
    String serviceName = "";
    try
    {
      String[] hostNames = getHOSTNames();
      for(int i = 0; i < hostNames.length; i++)
      {
        if(hostNames[i].equals(hostName))
        {
          String[] serviceNames = getServicesByHostName(hostName);
          for(int j = 0; j < serviceNames.length; j++)
          {
            Vector hpdURLData = readFile(getServiceConfFilePath(hostName, serviceNames[j]), true);
            if(hpdURLData == null)
              continue;

            for(int k = 0; k < hpdURLData.size(); k++)
            {
              String[] arrURLLine = rptUtilsBean.strToArrayData(hpdURLData.elementAt(k).toString(), " ");
              if(arrURLLine.length <= 0)
                continue;
              if((arrURLLine[0].equals("URL")) && arrURLLine[1].equals(URL))
                return serviceNames[j];
              if(IgnoreCase.equals("duplicate"))
              {
                if((arrURLLine[0].equals("#URL") || arrURLLine[0].equals("# URL")) && arrURLLine[1].equals(URL))
                  return serviceNames[j];
              }
            }
          }
        }
      }
      return serviceName;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "isHOSTAndServiceAlreadyExist", "", "", "Exception - ", ex);
      return serviceName;
    }
  }

  // Method to add new service in hpd.conf file as well as the response file
  public boolean addNOService(String hostName, String serviceName, String URL, String templateName, String responseFileExtension, StringBuffer responseFileContents, StringBuffer requestFileContents, String activeInactive, StringBuffer errMsg, String ignoreCase, String strRegEX, String TemplateType)
  {
    return addNOService(hostName, serviceName, URL, templateName, responseFileExtension, responseFileContents, requestFileContents, activeInactive, null, null, false, errMsg, ignoreCase, strRegEX, TemplateType);
  }

  public boolean addNOService(String hostName, String serviceName, String URL, String templateName, String responseFileExtension, StringBuffer responseFileContents, StringBuffer requestFileContents, String activeInactive, StringBuffer requestBodyFileContents, StringBuffer responseBodyFileContents, boolean isRecording, StringBuffer errMsg)
  {
    return addNOService(hostName, serviceName, URL, templateName, responseFileExtension, responseFileContents, requestFileContents, activeInactive, null, null, false, errMsg, "NA", "", "Simulate NA");
  }

  public boolean addNOService(String hostName, String serviceName, String URL, String templateName, String responseFileExtension, StringBuffer responseFileContents, StringBuffer requestFileContents, String activeInactive, StringBuffer requestBodyFileContents, StringBuffer responseBodyFileContents, boolean isRecording, StringBuffer errMsg, String IgnoreCase, String strRegEX, String TemplateType)
  {
    try
    {
      Log.debugLog(className, "addNOServices", "", "", "Method started service=" + serviceName + ", responseFileExtension=" + responseFileExtension);

      //from recording gui it is possible that it may have multiple response templates for same URL.
      //Than it will try to create multiple URL with same name.
      //Now we will add it as new template of existing url
      if(!URL.startsWith("/"))
        URL = "/" + URL;

      String existServiceName = isHOSTAndURLAlreadyExist(hostName, URL, IgnoreCase);

      if(!existServiceName.equals(""))
      {
        Log.debugLog(className, "addNOServices", "", "", "Host and Service already exist. Calling addResponseTemplate method");

        if(addResponseTemplate(hostName, existServiceName, URL, templateName, "NA", responseFileExtension, responseFileContents, requestFileContents, requestBodyFileContents, responseBodyFileContents, isRecording, activeInactive, TemplateType))
        {
          if(!IgnoreCase.equals("duplicate"))
            errMsg.append("Service - '" + serviceName + "', URL - '" + URL + "'\nStatus - URL is already there. So added as a new template in existing service - '" + existServiceName + "'\n");
          else
            errMsg.append("Service - '" + serviceName + "', URL - '" + URL + "'\nStatus - URL is already there and ENABLE_URL_IGNORE_CASE is enabled. So added as a new template in existing disabled service - '" + existServiceName + "'\n");
          return true;
        }
        else
        {
          errMsg.append("Service - '" + serviceName + "', URL - '" + URL + "'\nStatus - Not added succussfully due to some problem\n");
          return false;
        }
      }

      String responseFileName = generateNewResponseFileName(hostName, serviceName, URL, responseFileExtension, templateName);

      String strControlPath = getServiceConfFilePath(hostName, serviceName);
      Log.debugLog(className, "addNOServices", "", "", "strControlPath=" + strControlPath);
      int lastControlIndex = strControlPath.lastIndexOf("/");
      String newControlPath = "";
      if(lastControlIndex > -1)
        newControlPath = strControlPath.substring(0, lastControlIndex);

      File fileCreateControlDirs = new File(newControlPath);
      if(!fileCreateControlDirs.mkdirs())
      {
        Log.debugLog(className, "addNOServiceObj", "", "", "Unable to create directory or it is already there. path = " + newControlPath);
        // return false;
      }

      File checkControlFile = new File(strControlPath);
      if(!checkControlFile.exists())
        checkControlFile.createNewFile();

      String strControlLine = "RESPONSE_TEMPLATE " + templateName + " " + TemplateType + " " + responseFileName + " active NA";

      FileWriter fControlstream = new FileWriter(strControlPath, true);
      BufferedWriter outControl = new BufferedWriter(fControlstream);
      String[][] arrDataValuesURL = getNOServices();

      //this flag is used to get service is with unique URL or not
      boolean enableUrlFlag = true;

      for(int i = 0; i < arrDataValuesURL.length; i++)
      {
        if(URL.trim().equals((arrDataValuesURL[i][2]).trim()))
        {
          if((arrDataValuesURL[i][14]).trim().equals("enabled"))
          {
            enableUrlFlag = false;
            break;
          }
        }
      }

      String strURL = URL;
      if(!strRegEX.equals(""))
      {
        strURL = strURL + " " + strRegEX;
      }

      if(enableUrlFlag)
        outControl.write("\nURL " + strURL + "\n");
      else
        outControl.write("\n#URL " + strURL + "\n");

      outControl.write("\n" + strControlLine + "\n");
      outControl.close();

      File fileResponseBody = new File(getServicePath(hostName, serviceName) + responseFileName);
      if(!fileResponseBody.exists())
        fileResponseBody.createNewFile();

      File fileRequestBody = new File(getServicePath(hostName, serviceName) + responseFileName + REQUEST_EXTENTION);
      if(!fileRequestBody.exists())
        fileRequestBody.createNewFile();

      File fileResponse = null, fileRequest = null;
      if(isRecording) // this is case of adding service manually.
      {
        //responseFileName = generateNewResponseFileName(hostName, serviceName, URL, responseFileExtension, templateName);

        fileResponse = new File(getServicePath(hostName, serviceName) + responseFileName + RESPONSE_EXTENTION + CAPTURED_EXTENTION);
        if(!fileResponse.exists())
          fileResponse.createNewFile();

        fileRequest = new File(getServicePath(hostName, serviceName) + responseFileName + REQUEST_EXTENTION + CAPTURED_EXTENTION);
        if(!fileRequest.exists())
          fileRequest.createNewFile();
      }

      // this is because request and response body content can be null if call from override method i.e. addResponseTemplate.
      if(requestBodyFileContents != null && responseBodyFileContents != null)
      {
        FileOutputStream out1 = new FileOutputStream(fileResponseBody);
        PrintStream responseBodyFile = new PrintStream(out1);
        responseBodyFile.print(responseBodyFileContents);
        responseBodyFile.close();

        FileOutputStream out2 = new FileOutputStream(fileRequestBody);
        PrintStream requestBodyFile = new PrintStream(out2);
        requestBodyFile.print(requestBodyFileContents);
        requestBodyFile.close();
      }

      FileOutputStream out3 = null, out4 = null;

      if(!isRecording) // this is case of adding service manually.
      {
        out3 = new FileOutputStream(fileResponseBody);
        out4 = new FileOutputStream(fileRequestBody);
      }
      else
      {
        out3 = new FileOutputStream(fileResponse);
        out4 = new FileOutputStream(fileRequest);
      }

      PrintStream responseFile = new PrintStream(out3);
      responseFile.print(responseFileContents);
      responseFile.close();

      PrintStream requestFile = new PrintStream(out4);
      requestFile.print(requestFileContents);
      requestFile.close();

      if(!IgnoreCase.equals("duplicate"))
        errMsg.append("Service - '" + serviceName + "', URL - '" + URL + "'\nStatus - Successfully added\n");
      else
        errMsg.append("Service - '" + serviceName + "', URL - '" + URL + "'\nStatus - ENABLE_URL_IGNORE_CASE is enabled. So Successfully added in disable mode.\n");

      writeToLogFile(hostName, serviceName);
      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "addNOServices", "", "", "Exception - ", ex);
      errMsg.append("Service - '" + serviceName + "', URL - '" + URL + "'\nStatus - Not added successfully due to some problem\n");
      return false;
    }
  }

  //mthod to genarate response file name by URL.
  public String generateNewResponseFileName(String hostName, String serviceName, String URL, String responseExtension, String templateName)
  {
    Log.debugLog(className, "generateNewResponseFileName", "", "", "Method Started. serivce = " + serviceName);
    try
    {
      String responseFile = "";
      responseFile = "service_" + templateName + responseExtension;
      Log.debugLog(className, "generateNewResponseFileName", "", "", "generated response body File = " + responseFile);

      File fileToCheck = new File(getServicePath(hostName, serviceName) + responseFile);
      int ctr = 1;
      while(fileToCheck.exists())
      {
        responseFile = "service_" + templateName + ctr + responseExtension;
        Log.debugLog(className, "generateNewResponseFileName", "", "", "new generated responseFile = " + responseFile);

        fileToCheck = new File(getServicePath(hostName, serviceName) + responseFile);
        ctr++;
      }

      return responseFile;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "generateNewResponseFileName", "", "", "Exception in generating response file name", e);
      return "";
    }
  }

  public boolean deleteNOServices(String hostName, String serviceName, String URL)
  {
    Log.debugLog(className, "deleteNOServices", "", "", "Method started hostname=" + hostName + ", Service Name=" + serviceName);
    try
    {
      String cmdName = "nou_recycle";
      String cmdArgs = "-o delete -s " + getServicePath(hostName, serviceName);
      CmdExec objCmdExec = new CmdExec();
      Vector vecCmdOutPut = objCmdExec.getResultByCommand(cmdName, cmdArgs, CmdExec.SYSTEM_CMD, null, "root");
      //deleteDirectory(new File(getServicePath(hostName, serviceName)));
      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "deleteNOServices", "", "", "Exception - ", ex);
      return false;
    }
  }

  public boolean deleteDirectory(File path)
  {
    Log.debugLog(className, "deleteDirectory", "", "", "path=" + path);
    try
    {
      if(path.exists())
      {
        File[] files = path.listFiles();
        for(int i = 0; i < files.length; i++)
        {
          if(files[i].isDirectory())
          {
            deleteDirectory(files[i]);
          }
          else
          {
            files[i].delete();
          }
        }
        path.delete();
      }
      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "deleteDirectory", "", "", "Exception - ", ex);
      return false;
    }
  }

  public boolean addNOServiceObj(String hostName, String serviceName, String URL, int objectName, String[] fieldsValue)
  {
    return addNOServiceObj(hostName, serviceName, URL, objectName, fieldsValue, null);
  }

  /* Method to add value in control file of any URL for different type of service objects.
   * fileContents will carry the the file contents if FILE arugument is there.
   * currently FILE argument is in File and Index File parameterers.
   * For other paramteres fileContents will be null or blank
   */
  public boolean addNOServiceObj(String hostName, String serviceName, String URL, int objectName, String[] fieldsValue, StringBuffer fileContents)
  {
    try
    {
      Log.debugLog(className, "addNOServiceObj", "", "", "Method started service=" + serviceName + ", objectName=" + objectName);
      String strToAdd = "";
      if(objectName == SEARCH_VAR) // request to add for Search var
      {
        String LBStr = "";
        String RBStr = "";
        if(!fieldsValue[1].equals(""))
          LBStr = ", LB=\"" + fieldsValue[1] + "\"";
        if(!fieldsValue[2].equals(""))
          RBStr = ", RB=\"" + fieldsValue[2] + "\"";

        strToAdd = "nsl_search_var (" + fieldsValue[0] + LBStr + RBStr + ", ORD=" + fieldsValue[3] + ", SaveOffset=" + fieldsValue[4] + ", SaveLen=" + fieldsValue[5] + ", Method=" + fieldsValue[6] + ", IgnoreCase=" + fieldsValue[7] + ");";
      }
      else if(objectName == DECLARE_VAR) // request to add for declare var
        strToAdd = "nsl_decl_var (" + fieldsValue[0] + ");";
      else if(objectName == HTTP_HEADER) // request to add for http header
        strToAdd = "SET_CR_URL_HEADER " + fieldsValue[0].replaceAll("\\r", "").replaceAll("\\n", "");
      else if(objectName == SVC_TIME) // request to add for service time
      {
        strToAdd = "SVC_TIME";
        if(fieldsValue[0].equals("0"))
          strToAdd = strToAdd + " 0 ";
        else
        {
          for(int jm = 0; jm < fieldsValue.length; jm++)
            strToAdd = strToAdd + " " + fieldsValue[jm];
        }
      }
      else if(objectName == DECLARE_ARRAY_VAR)
      {
        strToAdd = "nsl_decl_array (" + fieldsValue[0] + ", Size=" + fieldsValue[1] + ", DefaultValue=" + fieldsValue[2] + ");";
      }
      else if(objectName == QUERY_VAR) // request to add QUERY_VAR
      {
        strToAdd = "nsl_query_var " + "("+fieldsValue[0] + ", QUERY=" + fieldsValue[1] + ", ORD=" + fieldsValue[2] + ", METHOD=" + fieldsValue[3] + ");";
      }
      else if(objectName == FILE_PARAMETERS)
      {
        String strVarValue = "";
        if(!fieldsValue[4].trim().equals(""))
          strVarValue = ", VAR_VALUE=" + fieldsValue[4];
        //String deliminator = "";
        //if(fieldsValue[6].trim().equals(","))
        //  deliminator = "Comma";
        //else
        //  deliminator = fieldsValue[6];
        strToAdd = "nsl_static_var (" + fieldsValue[0] + ", FILE=" + fieldsValue[1] + ", REFRESH=" + fieldsValue[2] + ", MODE=" + fieldsValue[3] + strVarValue + ", FirstDataLine=" + fieldsValue[5] + ", ColumnDelimiter=" + fieldsValue[6] + ", HeaderLine=" + fieldsValue[7] + ");";
      }
      /*
       * else if(objectName == INDEX_FILE_PARAMETER) strToAdd = "nsl_index_file_var (" + fieldsValue[0] + ", FILE=" + fieldsValue[1] + ", IndexVar=" + fieldsValue[2] + ", Refresh=" + fieldsValue[3] + ", MODE=" + fieldsValue[4] + ", VAR_VALUE=" + fieldsValue[5] + ");";
       */
      else if(objectName == INDEX_FILE_PARAMETER)
      {
        String strVarValue = "";
        if(!fieldsValue[3].trim().equals(""))
          strVarValue = ", VAR_VALUE=" + fieldsValue[3];
        //String deliminator = "";
        //if(fieldsValue[5].trim().equals(","))
        //  deliminator = "Comma";
        //else
        //  deliminator = fieldsValue[5];
        //strToAdd = "nsl_index_file_var (" + fieldsValue[0] + ", FILE=" + fieldsValue[1] + ", IndexVar=" + fieldsValue[2] + strVarValue + ");";
        strToAdd = "nsl_index_file_var (" + fieldsValue[0] + ", FILE=" + fieldsValue[1] + ", IndexVar=" + fieldsValue[2] + strVarValue + ", FirstDataLine=" + fieldsValue[4] + ", ColumnDelimiter=" + fieldsValue[5] + ", HeaderLine=" + fieldsValue[6] + ");";
      }
      else if(objectName == DATE_TIME_PARAMETER)
        strToAdd = "nsl_date_var (" + fieldsValue[0] + ", Format=\"" + fieldsValue[1] + "\", Offset=" + fieldsValue[2] + ", Refresh=" + fieldsValue[3] + ");";
      else if(objectName == RANDOM_NUMBER)
        strToAdd = "nsl_random_number_var (" + fieldsValue[0] + ", Min=" + fieldsValue[1] + ", Max=" + fieldsValue[2] + ", Format=" + fieldsValue[3] + ", Refresh=" + fieldsValue[4] + ");";
      else if(objectName == RANDOM_STRING)
      {
        strToAdd = "nsl_random_string_var (" + fieldsValue[0] + ", Min=" + fieldsValue[1] + ", Max=" + fieldsValue[2] + ", CharSet=" + fieldsValue[3] + ", Refresh=" + fieldsValue[4];
        if(fieldsValue.length > 5)
        {
          strToAdd = strToAdd + ", Enable_Encoding=" + fieldsValue[5];
        }
        strToAdd = strToAdd + ");";
      }
      else if(objectName == UNIQUE_NUMBER)
        strToAdd = "nsl_unique_number_var (" + fieldsValue[0] + ", Format=" + fieldsValue[1] + ", Refresh=" + fieldsValue[2] + ");";
      else if(objectName == UNIQUE_NUMBER_VUSER)
        strToAdd = "nsl_unique_number_vuser_var (" + fieldsValue[0] + ", Start=" + fieldsValue[1] + ", End=" + fieldsValue[2] + ", Format=" + fieldsValue[3] + ", Refresh=" + fieldsValue[4] + ", OutOfvalue=" + fieldsValue[5] + ");";
      else if(objectName == GLOBAL_INDEXED_DATA_SOURCE)
      {
        strToAdd = "nsl_select_index_datasource (" + fieldsValue[0] + ", index_var=\"" + fieldsValue[1] + "\");";
      }
      else if(objectName == XML_PARAMETER)
      {
        String parameterString = "nsl_xml_var (" + fieldsValue[0] + ", NODE=" + fieldsValue[1] + ", VALUE=" + fieldsValue[3];
        String nodeSelectionCriteria = fieldsValue[2];

        if(!nodeSelectionCriteria.equals(""))
        {
          String[] arrSelectionCriteria = rptUtilsBean.split(nodeSelectionCriteria, "|");
          for(int i = 0; i < arrSelectionCriteria.length; i++)
          {
            parameterString = parameterString + ", WHERE=" + arrSelectionCriteria[i];
          }
        }

        parameterString = parameterString + ", ORD=" + fieldsValue[4];
        if(!fieldsValue[5].equals(""))
          parameterString = parameterString + ", Convert=" + fieldsValue[5];
        if(!fieldsValue[6].equals(""))
          parameterString = parameterString + ", RetainPreValue=" + fieldsValue[6];
        if(fieldsValue.length > 7)
        {
          if(!fieldsValue[7].equals(""))
            parameterString = parameterString + ", BodySkipStartBytes=" + fieldsValue[7];
        }
        if(fieldsValue.length > 8)
        {
          if(!fieldsValue[8].equals(""))
            parameterString = parameterString + ", BodySkipEndBytes=" + fieldsValue[8];
        }

        strToAdd = parameterString + ");";
      }
      else if(objectName == COOKIE_VAR) // request to add for Search var
      {
        String parameterString = "nsl_cookie_var (" + fieldsValue[0] + ", CookieName=" + "\"" + fieldsValue[1] + "\"";
        String encode = "";
        String defaultValue = "";

        if(fieldsValue[8].equals("Specified"))
        {
          fieldsValue[4] = fieldsValue[4].replace("\\", "\\\\");
          fieldsValue[4] = fieldsValue[4].replace("\"", "\\\"");
          encode = ", Encode=" + "\"" + fieldsValue[8] + "\"" + ", SpecifiedChars=" + "\"" + fieldsValue[4] + "\"";
        }
        else
          encode = ", Encode=" + "\"" + fieldsValue[8] + "\"";
        if(!fieldsValue[6].equals(""))
        {
          fieldsValue[6] = fieldsValue[6].replace("\\", "\\\\");
          fieldsValue[6] = fieldsValue[6].replace("\"", "\\\"");
          defaultValue = ", DefaultValue=" + "\"" + fieldsValue[6] + "\"";
        }

        if(!fieldsValue[2].equals("0"))
          parameterString = parameterString + ", StartOffset=" + "\"" + fieldsValue[2] + "\"";

        if(!fieldsValue[3].equals("0"))
          parameterString = parameterString + ", SaveLen=" + "\"" + fieldsValue[3] + "\"";

        if(!fieldsValue[5].equals("None"))
          parameterString = parameterString + ", ActionOnNotFound=" + "\"" + fieldsValue[5] + "\"";

        if(!fieldsValue[6].equals(""))
          parameterString = parameterString + defaultValue;

        if(!fieldsValue[7].equals("None"))
          parameterString = parameterString + ", Method=" + "\"" + fieldsValue[7] + "\"";

        if(!fieldsValue[8].equals("None"))
          parameterString = parameterString + encode;

        if(!fieldsValue[9].equals("No"))
          parameterString = parameterString + ", Decode=" + "\"" + fieldsValue[9] + "\"";

        strToAdd = parameterString + ");";
      }
      else if(objectName == REQUEST_VAR)
      {
        if(!fieldsValue[6].equals(""))
        {
          fieldsValue[6] = fieldsValue[6].replace("\\", "\\\\");
          fieldsValue[6] = fieldsValue[6].replace("\"", "\\\"");
        }

        String parameterString = "nsl_request_var (" + fieldsValue[0] + ", AssociatedObject=" + "\"" + fieldsValue[1] + "\"" + ", ORD=\"" 
        + fieldsValue[2] + "\", StartOffset=\"" + fieldsValue[3] + "\", SaveLen=\"" + fieldsValue[4] + "\", ActionOnNotFound=\"" + fieldsValue[5] + 
         "\", Method=\"" + fieldsValue[7] + "\", Decode=\"" + fieldsValue[8] +  "\"";
         
        if(!fieldsValue[6].trim().equals(""))
          parameterString = parameterString +  ", DefaultValue=\"" + fieldsValue[6] + "\"";
         
        String encode = "";

        if(fieldsValue[9].equals("Specified"))
        {
          fieldsValue[10] = fieldsValue[10].replace("\\", "\\\\");
          fieldsValue[10] = fieldsValue[10].replace("\"", "\\\"");
          encode = ", Encode=" + "\"" + fieldsValue[9] + "\"" + ", SpecifiedChars=" + "\"" + fieldsValue[10] + "\"";
        }
        else
          encode = ", Encode=" + "\"" + fieldsValue[9] + "\"";

        if(!encode.equals(""))
        {
          parameterString = parameterString + encode;
        }
        strToAdd = parameterString + ");";
      }

      Log.debugLog(className, "addNOServiceObj", "", "", "strToAdd=" + strToAdd);

      File fileCreateDirs = new File(getServicePath(hostName, serviceName));
      if(!fileCreateDirs.mkdirs())
      {
        Log.debugLog(className, "addNOServiceObj", "", "", "Unable to create directory or it is already there. path");
        // return false;
      }

      File checkFile = new File(getServiceConfFilePath(hostName, serviceName));
      if(!checkFile.exists())
        checkFile.createNewFile();

      FileWriter fstream = new FileWriter(getServiceConfFilePath(hostName, serviceName), true);
      BufferedWriter out = new BufferedWriter(fstream);
      out.write("\n" + strToAdd + "\n");
      out.close();

      //code to create/update file contents for FILE argument used in 'File' and 'Index File' parameters
      if((objectName == FILE_PARAMETERS || objectName == INDEX_FILE_PARAMETER) && fileContents != null)
      {
        File fileFILE = null;
        if(fieldsValue[1].startsWith("/"))
          fileFILE = new File(fieldsValue[1]);
        else
          fileFILE = new File(getServicePath(hostName, serviceName) + fieldsValue[1]);

        int lastFILEIndex = fieldsValue[1].lastIndexOf("/");
        String newFILEPath = "";
        if(lastFILEIndex > -1)
          newFILEPath = fieldsValue[1].substring(0, lastFILEIndex);

        File fileFILECreateDirs = new File(newFILEPath);
        if(!fileFILECreateDirs.mkdirs())
        {
          Log.debugLog(className, "addNOServices", "", "", "Unable to create directory path or it is already there. = " + newFILEPath);
          // return false;
        }

        if(!fileFILE.exists())
          fileFILE.createNewFile();

        FileOutputStream out1 = new FileOutputStream(fileFILE);
        PrintStream responseFile = new PrintStream(out1);
        responseFile.println(fileContents.toString().replaceAll("\\r", ""));
        responseFile.close();

      }
      writeToLogFile(hostName, serviceName);
      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "addNOServiceObj", "", "", "Exception - ", ex);
      return false;
    }
  }

  //this function is used to enable url service
  public boolean enableServiceURL(String hostName, String serviceName, String URL, String operation)
  {
    Log.debugLog(className, "enableServiceURL", "", "", "Method started service=" + serviceName + " operation=" + operation);
    try
    {
      Vector completeControlFile = readFile(getServiceConfFilePath(hostName, serviceName), false);
      Vector modifiedVector = new Vector();
      String strSearch = "";
      Log.debugLog(className, "enableServiceURL", "", "", "operation= " + operation);
      if(operation.equals("enabled"))
        strSearch = "#";
      else
        strSearch = "URL";
      Log.debugLog(className, "enableServiceURL", "", "", "strSearch= " + strSearch);
      for(int yy = 0; yy < completeControlFile.size(); yy++)
      {
        String line = completeControlFile.elementAt(yy).toString();
        String[] arrURLLine = rptUtilsBean.strToArrayData(line, " ");

        if(line.trim().startsWith(strSearch)) // Method found
        {
          String strURL = "";
          if(operation.equals("enabled"))
          {
            if(arrURLLine[0].equals("#") && arrURLLine.length > 1 && arrURLLine[1].equals("URL"))
            {
              strURL = "URL " + arrURLLine[2];
              if(arrURLLine.length > 3)
                strURL = strURL + " " + arrURLLine[3];
            }
            else if(arrURLLine[0].equals("#URL"))
            {
              strURL = "URL " + arrURLLine[1];
              if(arrURLLine.length > 2)
                strURL = strURL + " " + arrURLLine[2];
            }
          }
          else
          {
            strURL = "#URL " + arrURLLine[1];
            if(arrURLLine.length > 2)
              strURL = strURL + " " + arrURLLine[2];
          }

          if(!strURL.equals(""))
            line = strURL;

          Log.debugLog(className, "enableServiceURL", "", "", "URL_SERVICE= " + strURL);
          modifiedVector.add(line);
        }
        else
          modifiedVector.add(line);
      }

      if(!writeToFile(getServiceConfFilePath(hostName, serviceName), modifiedVector, hostName, serviceName))
        return false;

      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "enableServiceURL", "", "", "Exception - ", ex);
      return false;
    }

  }

  public String getRegexKeyword(String hostName, String serviceName, String URL)
  {
    Log.debugLog(className, "getRegexKeyword", "", "", "Method started");
    String regex = "";
    try
    {
      Vector completeControlFile = readFile(getServiceConfFilePath(hostName, serviceName), true);

      for(int yy = 0; yy < completeControlFile.size(); yy++)
      {
        String line = completeControlFile.elementAt(yy).toString();
        String[] arrURLLine = rptUtilsBean.strToArrayData(line, " ");
        if(line.trim().startsWith("URL") || line.trim().startsWith("#URL")) // Method found
        {
          if(arrURLLine.length > 2)
            regex = arrURLLine[2];
        }
        else if(line.trim().startsWith("#"))
        {
          if(arrURLLine.length > 2)
            regex = arrURLLine[2];
        }
      }
      return regex;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getRegexKeyword", "", "", "Exception - ", ex);
      return "";
    }

  }

  /*
   * Method to get the state (enable/disable) and mode (Forward/Simulate/Mixed) mode of any service
   */
  public String[] getStateAndModeOfService(String hostName, String serviceName)
  {
    Log.debugLog(className, "getStateAndModeOfService", "", "", "Method started");
    String[] stateAndMode = new String[]{"NA", "NA"};
    try
    {
      String URL = "";
      Vector completeControlFile = readFile(getServiceConfFilePath(hostName, serviceName), true);
      if(completeControlFile == null || completeControlFile.size() <= 0)
        return stateAndMode;

      for(int yy = 0; yy < completeControlFile.size(); yy++)
      {
        String line = completeControlFile.elementAt(yy).toString();
        String[] arrURLLine = rptUtilsBean.strToArrayData(line, " ");
        if(arrURLLine.length <= 0)
          continue;
        if(arrURLLine[0].equals("URL"))
        {
          stateAndMode[0] = "enabled";
          URL = arrURLLine[1];
        }
        else if(arrURLLine[0].equals("#URL"))
        {
          stateAndMode[0] = "disabled";
          URL = arrURLLine[1];
        }

        String ResponseTemplatesInfo[][] = getResponseTemplateInfoByURL("default", serviceName, rptUtilsBean.replaceSpecialCharacter(URL));

        ArrayList<Integer> actCol = new ArrayList<Integer>();

        for(int i = 0; i < ResponseTemplatesInfo.length; i++)
        {
          if(ResponseTemplatesInfo[i][5].trim().equals("active"))
          {
            actCol.add(i);
          }
        }
        String templateType = "Simulate";

        if(actCol.size() > 0)
          templateType = ResponseTemplatesInfo[actCol.get(0)][1];

        if(templateType.contains("Forward"))
          stateAndMode[1] = "Forward";
        else
          stateAndMode[1] = "Simulate";

        for(int i = 1; i < actCol.size(); i++)
        {
          String tempMode = "";

          if(ResponseTemplatesInfo[actCol.get(i)][1].contains("Forward"))
            tempMode = "Forward";
          else
            tempMode = "Simulate";
          if(!tempMode.contains(stateAndMode[1]))
          {
            stateAndMode[1] = "Mixed";
            break;
          }
        }

      }
      return stateAndMode;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getStateAndModeOfService", "", "", "Exception - ", ex);
      return stateAndMode;
    }

  }

  public boolean updateServiceAndUrl(String hostName, String serviceName, String URL, String newURL, String newService, String isDuplicate, String Regex)
  {
    Log.debugLog(className, "updateServiceAndUrl", "", "", "Method started new service name =" + newService + " new url=" + newURL);
    try
    {
      Vector completeControlFile = readFile(getServiceConfFilePath(hostName, serviceName), false);
      Vector modifiedVector = new Vector();

      if(!newURL.startsWith("/"))
        newURL = "/" + newURL;

      for(int yy = 0; yy < completeControlFile.size(); yy++)
      {
        String line = completeControlFile.elementAt(yy).toString();

        if(line.trim().startsWith("URL")) // Method found
        {
          //String[] arrURLLine = rptUtilsBean.strToArrayData(line, " ");
          String strURL = "";
          if(isDuplicate.equals("Yes"))
            strURL = "#URL " + newURL + " " + Regex;
          else
            strURL = "URL " + newURL + " " + Regex;

          modifiedVector.add(strURL);
          Log.debugLog(className, "updateServiceAndUrl", "", "", "URL_SERVICE= " + newURL);
        }
        else if(line.trim().startsWith("#URL"))
        {
          //String[] arrURLLine = rptUtilsBean.strToArrayData(line, " ");
          String strURL = "#URL " + newURL + " " + Regex;

          modifiedVector.add(strURL);
          Log.debugLog(className, "updateServiceAndUrl", "", "", "URL_SERVICE= " + newURL);
        }
        else if(line.trim().startsWith("# URL"))
        {
          //String[] arrURLLine = rptUtilsBean.strToArrayData(line, " ");
          String strURL = "#URL " + newURL + " " + Regex;

          modifiedVector.add(strURL);
          Log.debugLog(className, "updateServiceAndUrl", "", "", "URL_SERVICE= " + newURL);
        }
        else
          modifiedVector.add(line);
      }

      if(!writeToFile(getServiceConfFilePath(hostName, serviceName), modifiedVector, hostName, serviceName))
        return false;

      File filePath = new File(getServicePath(hostName, serviceName));

      if(filePath.exists())
      {
        filePath.renameTo(new File(getServicePath(hostName, newService)));
        Log.debugLog(className, "updateServiceAndUrl", "", "", "New_SERVICE= " + newService);
      }

      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "updateServiceAndUrl", "", "", "Exception - ", ex);
      return false;
    }
  }

  public boolean updateServiceObj(String hostName, String serviceName, String URL, int objectName, String operation, int[] rowsToUpdate, String[] fieldsValue)
  {
    return updateServiceObj(hostName, serviceName, URL, objectName, operation, rowsToUpdate, fieldsValue, null, null);
  }

  /*
   * Method to update or delete any service object.
   * @URL - will define the service. using URL we can find the file where we will get the service objects
   * @objectName - it will define that which type of service object need to update/delete
   * @operation - it can be 'delete' or 'update'
   * @rowsToUpdate - it is an interger array which will define the row indexs to which we need to update/delete
   * @fieldsValue - it is a string array which will define the new values to be updated, it will be null in case of delete
   * @oldFILEValue - it is to get the old file value of File or Index File parameter in case of it is chnanged
   * otherwise it will be blank.
   */
  public boolean updateServiceObj(String hostName, String serviceName, String URL, int objectName, String operation, int[] rowsToUpdate, String[] fieldsValue, StringBuffer fileContents, String oldFILEValue)
  {
    Log.debugLog(className, "updateServiceObj", "", "", "Method started service=" + serviceName + ", objectName=" + objectName + ", operation=" + operation);
    try
    {
      Vector completeControlFile = readFile(getServiceConfFilePath(hostName, serviceName), false);
      Vector modifiedVector = new Vector();

      if(completeControlFile == null)
      {
        File fileCreateDirs = new File(getServicePath(hostName, serviceName));
        if(!fileCreateDirs.mkdirs())
        {
          Log.debugLog(className, "updateServiceObj", "", "", "Unable to create directory or it is already there. path = " + getServicePath(hostName, serviceName));
          // return false;
        }
        if(objectName == SVC_TIME) // request to add for service time
        {
          String strToUpdate = "SVC_TIME";
          if(fieldsValue[0].equals("0"))
            strToUpdate = strToUpdate + " 0 ";
          else
          {
            for(int jm = 0; jm < fieldsValue.length; jm++)
              strToUpdate = strToUpdate + " " + fieldsValue[jm];
          }
          File fileControl = new File(getServiceConfFilePath(hostName, serviceName));
          fileControl.createNewFile();

          FileWriter fstream = new FileWriter(getServiceConfFilePath(hostName, serviceName), true);
          BufferedWriter out = new BufferedWriter(fstream);
          out.write("\n" + strToUpdate + "\n");
          out.close();
        }

        return true;
      }

      String strSearch = "";
      if(objectName == SEARCH_VAR)
        strSearch = "nsl_search_var";
      else if(objectName == DECLARE_VAR)
        strSearch = "nsl_decl_var";
      else if(objectName == HTTP_HEADER)
        strSearch = "SET_CR_URL_HEADER";
      else if(objectName == SVC_TIME)
        strSearch = "SVC_TIME";
      else if(objectName == QUERY_VAR)
        strSearch = "nsl_query_var";
      else if(objectName == FILE_PARAMETERS)
        strSearch = "nsl_static_var";
      else if(objectName == INDEX_FILE_PARAMETER)
        strSearch = "nsl_index_file_var";
      else if(objectName == DATE_TIME_PARAMETER)
        strSearch = "nsl_date_var";
      else if(objectName == RANDOM_NUMBER)
        strSearch = "nsl_random_number_var";
      else if(objectName == RANDOM_STRING)
        strSearch = "nsl_random_string_var";
      else if(objectName == UNIQUE_NUMBER)
        strSearch = "nsl_unique_number_var";
      else if(objectName == UNIQUE_NUMBER_VUSER)
        strSearch = "nsl_unique_number_vuser_var";
      else if(objectName == XML_PARAMETER)
        strSearch = "nsl_xml_var";
      else if(objectName == COOKIE_VAR)
        strSearch = "nsl_cookie_var";
      else if(objectName == DECLARE_ARRAY_VAR)
        strSearch = "nsl_decl_array";
      else if(objectName == GLOBAL_INDEXED_DATA_SOURCE)
        strSearch = "nsl_select_index_datasource";
      else if(objectName == REQUEST_VAR)
        strSearch = "nsl_request_var";

      int objectCtr = -1;
      boolean serviceTimeFound = false;
      for(int yy = 0; yy < completeControlFile.size(); yy++)
      {
        String line = completeControlFile.elementAt(yy).toString();

        if(line.trim().startsWith(strSearch) || (strSearch.equals("nsl_query_var") && line.trim().startsWith("QUERY_VAR"))) // Method found
        {
          objectCtr++;
          boolean matchToUpdate = false;
          for(int bb = 0; bb < rowsToUpdate.length; bb++)
          {
            if(rowsToUpdate[bb] == objectCtr)
            {
              matchToUpdate = true;
              if(operation.equals("update"))
              {
                String strToUpdate = "";
                if(objectName == SEARCH_VAR) // request to add for Search var
                {
                  String LBStr = "";
                  String RBStr = "";
                  if(!fieldsValue[1].equals(""))
                    LBStr = ", LB=\"" + fieldsValue[1] + "\"";
                  if(!fieldsValue[2].equals(""))
                    RBStr = ", RB=\"" + fieldsValue[2] + "\"";

                  strToUpdate = "nsl_search_var (" + fieldsValue[0] + LBStr + RBStr + ", ORD=" + fieldsValue[3] + ", SaveOffset=" + fieldsValue[4] + ", SaveLen=" + fieldsValue[5] + ", Method=" + fieldsValue[6] + ", IgnoreCase=" + fieldsValue[7] + ");";
                }
                else if(objectName == DECLARE_VAR) // request to add for declare var
                  strToUpdate = "nsl_decl_var (" + fieldsValue[0] + ");";
                else if(objectName == DECLARE_ARRAY_VAR)
                {
                  strToUpdate = "nsl_decl_array (" + fieldsValue[0] + ", Size=" + fieldsValue[1] + ", DefaultValue=" + fieldsValue[2] + ");";
                }
                else if(objectName == HTTP_HEADER) // request to add for http header
                  strToUpdate = "SET_CR_URL_HEADER " + fieldsValue[0].replaceAll("\\r", "");
                else if(objectName == SVC_TIME) // request to add for service time
                {
                  serviceTimeFound = true;
                  strToUpdate = "SVC_TIME";
                  if(fieldsValue[0].equals("0"))
                    strToUpdate = strToUpdate + " 0 ";
                  else
                  {
                    for(int jm = 0; jm < fieldsValue.length; jm++)
                      strToUpdate = strToUpdate + " " + fieldsValue[jm];
                  }
                }
                else if(objectName == QUERY_VAR) // request to add for QUERY_VAR
                  strToUpdate = "nsl_query_var (" + fieldsValue[0] + ", QUERY=" + fieldsValue[1] + ", ORD=" + fieldsValue[2] + ", METHOD=" + fieldsValue[3] + ");";
                else if(objectName == FILE_PARAMETERS) // request to add QUERY_VAR
                {
                  String strVarValue = "";
                  if(!fieldsValue[4].trim().equals(""))
                    strVarValue = ", VAR_VALUE=" + fieldsValue[4];
                  //String deliminator = "";
                  //if(fieldsValue[6].trim().equals(","))
                  //  deliminator = "Comma";
                  //else
                  //  deliminator = fieldsValue[6];
                  strToUpdate = "nsl_static_var (" + fieldsValue[0] + ", FILE=" + fieldsValue[1] + ", REFRESH=" + fieldsValue[2] + ", MODE=" + fieldsValue[3] + strVarValue + ", FirstDataLine=" + fieldsValue[5] + ", ColumnDelimiter=" + fieldsValue[6] + ", HeaderLine=" + fieldsValue[7] + ");";
                }
                /*
                 * else if(objectName == INDEX_FILE_PARAMETER) //request to add QUERY_VAR strToUpdate = "nsl_index_file_var (" + fieldsValue[0] + ", FILE=" + fieldsValue[1] + ", IndexVar=" + fieldsValue[2] + ", Refresh=" + fieldsValue[3] + ", MODE=" + fieldsValue[4] + ", VAR_VALUE=" + fieldsValue[5] + ");";
                 */
                else if(objectName == INDEX_FILE_PARAMETER) // request to add Index File
                {
                  String strVarValue = "";
                  if(!fieldsValue[3].trim().equals(""))
                    strVarValue = ", VAR_VALUE=" + fieldsValue[3];
                  //String deliminator = "";
                  //if(fieldsValue[5].trim().equals(","))
                  //  deliminator = "Comma";
                  //else
                  //  deliminator = fieldsValue[5];
                  strToUpdate = "nsl_index_file_var (" + fieldsValue[0] + ", FILE=" + fieldsValue[1] + ", IndexVar=" + fieldsValue[2] + strVarValue + ", FirstDataLine=" + fieldsValue[4] + ", ColumnDelimiter=" + fieldsValue[5] + ", HeaderLine=" + fieldsValue[6] + ");";
                }
                else if(objectName == DATE_TIME_PARAMETER) // request to add QUERY_VAR
                  strToUpdate = "nsl_date_var (" + fieldsValue[0] + ", Format=\"" + fieldsValue[1] + "\", Offset=" + fieldsValue[2] + ", Refresh=" + fieldsValue[3] + ");";
                else if(objectName == RANDOM_NUMBER) // request to add QUERY_VAR
                  strToUpdate = "nsl_random_number_var (" + fieldsValue[0] + ", Min=" + fieldsValue[1] + ", Max=" + fieldsValue[2] + ", Format=" + fieldsValue[3] + ", Refresh=" + fieldsValue[4] + ");";
                else if(objectName == RANDOM_STRING)
                {
                  strToUpdate = "nsl_random_string_var (" + fieldsValue[0] + ", Min=" + fieldsValue[1] + ", Max=" + fieldsValue[2] + ", CharSet=" + fieldsValue[3] + ", Refresh=" + fieldsValue[4];
                  if(fieldsValue.length > 5)
                  {
                    strToUpdate = strToUpdate + ", Enable_Encoding=" + fieldsValue[5];
                  }
                  strToUpdate = strToUpdate + ");";
                }
                else if(objectName == UNIQUE_NUMBER)
                  strToUpdate = "nsl_unique_number_var (" + fieldsValue[0] + ", Format=" + fieldsValue[1] + ", Refresh=" + fieldsValue[2] + ");";
                else if(objectName == UNIQUE_NUMBER_VUSER)
                  strToUpdate = "nsl_unique_number_vuser_var (" + fieldsValue[0] + ", Start=" + fieldsValue[1] + ", End=" + fieldsValue[2] + ", Format=" + fieldsValue[3] + ", Refresh=" + fieldsValue[4] + ", OutOfvalue=" + fieldsValue[5] + ");";
                else if(objectName == GLOBAL_INDEXED_DATA_SOURCE)
                {
                  strToUpdate = "nsl_select_index_datasource (" + fieldsValue[0] + ", index_var=\"" + fieldsValue[1] + "\");";
                }
                else if(objectName == XML_PARAMETER)
                {
                  String parameterString = "nsl_xml_var (" + fieldsValue[0] + ", NODE=" + fieldsValue[1] + ", VALUE=" + fieldsValue[3];
                  String nodeSelectionCriteria = fieldsValue[2];

                  if(!nodeSelectionCriteria.equals(""))
                  {
                    String[] arrSelectionCriteria = rptUtilsBean.split(nodeSelectionCriteria, "|");
                    for(int i = 0; i < arrSelectionCriteria.length; i++)
                    {
                      parameterString = parameterString + ", WHERE=" + arrSelectionCriteria[i];
                    }
                  }

                  parameterString = parameterString + ", ORD=" + fieldsValue[4];
                  if(!fieldsValue[5].equals(""))
                    parameterString = parameterString + ", Convert=" + fieldsValue[5];

                  if(!fieldsValue[6].equals(""))
                    parameterString = parameterString + ", RetainPreValue=" + fieldsValue[6];

                  if(fieldsValue.length > 7)
                  {
                    if(!fieldsValue[7].equals(""))
                      parameterString = parameterString + ", BodySkipStartBytes=" + fieldsValue[7];
                  }
                  if(fieldsValue.length > 8)
                  {
                    if(!fieldsValue[8].equals(""))
                      parameterString = parameterString + ", BodySkipEndBytes=" + fieldsValue[8];
                  }
                  strToUpdate = parameterString + ");";
                }
                else if((objectName == COOKIE_VAR))
                {
                  String parameterString = "nsl_cookie_var (" + fieldsValue[0] + ", CookieName=" + "\"" + fieldsValue[1] + "\"";
                  String encode = "";
                  String defaultValue = "";

                  if(fieldsValue[8].equals("Specified"))
                  {
                    fieldsValue[4] = fieldsValue[4].replace("\\", "\\\\");
                    fieldsValue[4] = fieldsValue[4].replace("\"", "\\\"");
                    encode = ", Encode=" + "\"" + fieldsValue[8] + "\"" + ", SpecifiedChars=" + "\"" + fieldsValue[4] + "\"";
                  }
                  else
                    encode = ", Encode=" + "\"" + fieldsValue[8] + "\"";
                  if(!fieldsValue[6].equals(""))
                  {
                    fieldsValue[6] = fieldsValue[6].replace("\\", "\\\\");
                    fieldsValue[6] = fieldsValue[6].replace("\"", "\\\"");
                    defaultValue = ", DefaultValue=" + "\"" + fieldsValue[6] + "\"";
                  }

                  if(!fieldsValue[2].equals("0"))
                    parameterString = parameterString + ", StartOffset=" + "\"" + fieldsValue[2] + "\"";

                  if(!fieldsValue[3].equals("0"))
                    parameterString = parameterString + ", SaveLen=" + "\"" + fieldsValue[3] + "\"";

                  if(!fieldsValue[5].equals("None"))
                    parameterString = parameterString + ", ActionOnNotFound=" + "\"" + fieldsValue[5] + "\"";

                  if(!fieldsValue[6].equals(""))
                    parameterString = parameterString + defaultValue;

                  if(!fieldsValue[7].equals("None"))
                    parameterString = parameterString + ", Method=" + "\"" + fieldsValue[7] + "\"";

                  if(!fieldsValue[8].equals("None"))
                    parameterString = parameterString + encode;

                  if(!fieldsValue[9].equals("No"))
                    parameterString = parameterString + ", Decode=" + "\"" + fieldsValue[9] + "\"";

                  strToUpdate = parameterString + ");";
                  //strToUpdate = "nsl_cookie_var (" + parameterName + ", CookieName="+ cookieName  + ", StartOffset=" + fieldsValue[2]  + ", SaveLen=" + fieldsValue[3] + actionOnNotFound + ", Method=" + fieldsValue[7]  + encode +  ", Decode=" + fieldsValue[9]  +  ");";
                }
                else if(objectName == REQUEST_VAR)
                {
                  if(!fieldsValue[6].equals(""))
                  {
                    fieldsValue[6] = fieldsValue[6].replace("\\", "\\\\");
                    fieldsValue[6] = fieldsValue[6].replace("\"", "\\\"");
                  }

                  String parameterString = "nsl_request_var (" + fieldsValue[0] + ", AssociatedObject=" + "\"" + fieldsValue[1] + "\"" + ", ORD=\"" 
                  + fieldsValue[2] + "\", StartOffset=\"" + fieldsValue[3] + "\", SaveLen=\"" + fieldsValue[4] + "\", ActionOnNotFound=\"" + fieldsValue[5] + 
                   "\", Method=\"" + fieldsValue[7] + "\", Decode=\"" + fieldsValue[8] +  "\"";
                   
                  if(!fieldsValue[6].trim().equals(""))
                    parameterString = parameterString +  ", DefaultValue=\"" + fieldsValue[6] + "\"";
                  
                  String encode = "";

                  if(fieldsValue[9].equals("Specified"))
                  {
                    fieldsValue[10] = fieldsValue[10].replace("\\", "\\\\");
                    fieldsValue[10] = fieldsValue[10].replace("\"", "\\\"");
                    encode = ", Encode=" + "\"" + fieldsValue[9] + "\"" + ", SpecifiedChars=" + "\"" + fieldsValue[10] + "\"";
                  }
                  else
                    encode = ", Encode=" + "\"" + fieldsValue[9] + "\"";

                  if(!encode.equals(""))
                  {
                    parameterString = parameterString + encode;
                  }

                  strToUpdate = parameterString + ");";
                }
                modifiedVector.add(strToUpdate);
              }
              else
              {
                if(yy + 1 < completeControlFile.size())
                {
                  String blankline = completeControlFile.elementAt(yy + 1).toString().trim();
                  if(blankline.equals(""))
                  {
                    yy++;
                  }
                }
              }
              break;
            }
          }
          if(!matchToUpdate)
          {
            modifiedVector.add(line);
          }
        }
        else
          modifiedVector.add(line);
      }
      if(objectName == SVC_TIME && !serviceTimeFound)
      {
        Log.debugLog(className, "updateServiceObj", "", "", "Service Time is not already exist then add process started");
        String strToAdd = "SVC_TIME";
        if(fieldsValue[0].equals("0"))
          strToAdd = strToAdd + " 0 ";
        else
        {
          for(int jm = 0; jm < fieldsValue.length; jm++)
            strToAdd = strToAdd + " " + fieldsValue[jm];
        }
        Log.debugLog(className, "updateServiceObj", "", "", "Service Time line to be added = " + strToAdd);
        modifiedVector.add(strToAdd);
      }
      if(!writeToFile(getServiceConfFilePath(hostName, serviceName), modifiedVector, hostName, serviceName))
        return false;

      //code to create/update file contents for FILE argument used in 'File' and 'Index File' parameters
      if((objectName == FILE_PARAMETERS || objectName == INDEX_FILE_PARAMETER) && fileContents != null)
      {
        if(!oldFILEValue.equals(""))
        {
          File oldFILE = new File(oldFILEValue);
          if(oldFILE.exists())
            oldFILE.delete();
        }

        File fileFILE = null;
        if(fieldsValue[1].startsWith("/"))
          fileFILE = new File(fieldsValue[1]);
        else
          fileFILE = new File(getServicePath(hostName, serviceName) + fieldsValue[1]);

        int lastFILEIndex = fieldsValue[1].lastIndexOf("/");
        String newFILEPath = "";
        if(lastFILEIndex > -1)
          newFILEPath = fieldsValue[1].substring(0, lastFILEIndex);

        File fileFILECreateDirs = new File(newFILEPath);
        if(!fileFILECreateDirs.mkdirs())
        {
          Log.debugLog(className, "addNOServices", "", "", "Unable to create directory path or it is already there. = " + newFILEPath);
          // return false;
        }
        if(!fileFILE.exists())
          fileFILE.createNewFile();

        FileOutputStream out1 = new FileOutputStream(fileFILE);
        PrintStream responseFile = new PrintStream(out1);
        responseFile.println(fileContents.toString().replaceAll("\\r", ""));
        responseFile.close();
      }

      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "updateServiceObj", "", "", "Exception - ", ex);
      return false;
    }
  }

  public boolean fileUpload(String hostName, String serviceName, String destFileWithPath, StringBuffer fileContents)
  {
    Log.debugLog(className, "fileUpload", "", "", "Method started destination file=" + destFileWithPath);
    try
    {
      File fileFILE = null;
      if(destFileWithPath.startsWith("/"))
        fileFILE = new File(destFileWithPath);
      else
        fileFILE = new File(getServicePath(hostName, serviceName) + destFileWithPath);

      int lastFILEIndex = destFileWithPath.lastIndexOf("/");
      String newFILEPath = "";
      if(lastFILEIndex > -1)
        newFILEPath = destFileWithPath.substring(0, lastFILEIndex);

      File fileFILECreateDirs = new File(newFILEPath);
      if(!fileFILECreateDirs.mkdirs())
      {
        Log.debugLog(className, "fileUpload", "", "", "Unable to create directory path or it is already there. = " + newFILEPath);
        // return false;
      }
      if(!fileFILE.exists())
        fileFILE.createNewFile();

      FileOutputStream out1 = new FileOutputStream(fileFILE);
      PrintStream responseFile = new PrintStream(out1);
      responseFile.println(fileContents.toString().replaceAll("\\r", ""));
      responseFile.close();
      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "fileUpload", "", "", "Exception - ", ex);
      return false;
    }
  }

  /*
   * method to get the filename of 'on request' or 'after request' function. User need to pass the URl operation name can be 'onRequest' or 'afterRequest'
   */
  public String getCommandFunctionName(String hostName, String serviceName, String URL, String objectName)
  {
    Log.debugLog(className, "getCommandFunctionName", "", "", "Method started");
    String functionName = "";
    try
    {
      String strSearch = "";
      if(objectName.equals("onRequest"))
        strSearch = "POST_RECV_FN";
      else
        strSearch = "PRE_SEND_FN";

      Vector hpdURLData = readFile(getServiceConfFilePath(hostName, serviceName), true);

      for(int k = 0; k < hpdURLData.size(); k++)
      {
        String[] arrURLLine = rptUtilsBean.strToArrayData(hpdURLData.elementAt(k).toString(), " ");
        if(arrURLLine.length <= 0)
          continue;
        if(arrURLLine[0].equals(strSearch))
          functionName = arrURLLine[1];
      }
      return functionName;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getCommandFunctionName", "", "", "Exception - ", ex);
      return functionName;
    }
  }

  /*
   * method to get the contents of on request or after request file contents.
   * User need to pass the host name and service name
   */
  public StringBuffer getCommandContents(String hostName, String serviceName, String URL)
  {
    Log.debugLog(className, "getCommandContents", "", "", "Method started");
    StringBuffer requestCode = new StringBuffer();
    try
    {
      String strFile = getServicePath(hostName, serviceName) + "service.c";
      File f1 = new File(strFile);
      if(!f1.exists())
      {
        Log.debugLog(className, "getCommandContents", "", "", "File doesn't exist");
        return requestCode;
      }

      Vector vecCode = readFile(strFile, false);
      // String newLine = System.getProperty("line.separator");
      for(int ds = 0; ds < vecCode.size(); ds++)
      {
        requestCode.append(vecCode.elementAt(ds).toString() + "\n");
        // requestCode.append(newLine);
      }
      return requestCode;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getCommandContents", "", "", "Exception - ", ex);
      return requestCode;
    }
  }

  /*
   * method to write the contents of on request or after request in to file.
   * User need to pass the host name and service name
   */
  public boolean updateCommandFunctionCode(String hostName, String serviceName, String URL, String onReqFunctionName, String afterReqFunctionName, StringBuffer fileContents)
  {
    Log.debugLog(className, "updateCommandFunctionCode", "", "", "Method started onReqFunctionName=" + onReqFunctionName + ", afterReqFunctionName=" + afterReqFunctionName);
    try
    {
      String strOn = "";
      String strAfter = "";
      if(!onReqFunctionName.equals(""))
        strOn = "POST_RECV_FN";
      if(!afterReqFunctionName.equals(""))
        strAfter = "PRE_SEND_FN";

      File service = new File(getServicePath(hostName, serviceName) + "service.c");
      if(!service.exists())
        service.createNewFile();

      FileOutputStream out2 = new FileOutputStream(service);
      PrintStream requestFile = new PrintStream(out2);
      requestFile.println(fileContents.toString().replaceAll("\\r", ""));
      requestFile.close();

      Vector completeControlFile = readFile(getServiceConfFilePath(hostName, serviceName), false);
      Vector modifiedVector = new Vector();
      if(completeControlFile == null)
      {
        String strControlPath = getServiceConfFilePath(hostName, serviceName);
        int lastControlIndex = strControlPath.lastIndexOf("/");
        String newControlPath = "";
        if(lastControlIndex > -1)
          newControlPath = strControlPath.substring(0, lastControlIndex);
        File fileControlCreateDirs = new File(newControlPath);
        if(!fileControlCreateDirs.mkdirs())
        {
          Log.debugLog(className, "updateServiceObj", "", "", "Unable to create directory or it is already there. path = " + newControlPath);
          // return false;
        }

        String strAddOn = strOn + " " + onReqFunctionName;
        String strAddAfter = strAfter + " " + afterReqFunctionName;

        File fileControl = new File(strControlPath);
        fileControl.createNewFile();

        FileWriter fstream = new FileWriter(strControlPath, true);
        BufferedWriter out = new BufferedWriter(fstream);
        if(!strAddOn.trim().equals("") && !onReqFunctionName.equals(""))
          out.write("\n" + strAddOn + "\n");
        if(!strAddAfter.trim().equals("") && !afterReqFunctionName.equals(""))
          out.write(strAddAfter + "\n");
        out.close();

        return true;
      }
      else
      {
        boolean needToAddOn = true;
        boolean needToAddAfter = true;
        for(int yy = 0; yy < completeControlFile.size(); yy++)
        {
          String line = completeControlFile.elementAt(yy).toString().trim();

          if(line.trim().startsWith("POST_RECV_FN")) // Method found
          {
            if(!onReqFunctionName.equals(""))
            {
              needToAddOn = false;
              String strAdd = strOn + " " + onReqFunctionName;
              modifiedVector.add(strAdd);
            }
          }
          else if(line.trim().startsWith("PRE_SEND_FN")) // Method found
          {
            if(!afterReqFunctionName.equals(""))
            {
              needToAddAfter = false;
              String strAdd = strAfter + " " + afterReqFunctionName;
              modifiedVector.add(strAdd);
            }
          }
          else
            modifiedVector.add(line);
        }

        if(needToAddOn && !onReqFunctionName.equals(""))
          modifiedVector.add(strOn + " " + onReqFunctionName);
        if(needToAddAfter && !afterReqFunctionName.equals(""))
          modifiedVector.add(strAfter + " " + afterReqFunctionName);

        if(!writeToFile(getServiceConfFilePath(hostName, serviceName), modifiedVector, hostName, serviceName))
          return false;
      }

      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "updateCommandFunctionCode", "", "", "Exception - ", ex);
      return false;
    }
  }

  /* replace multiple whitespaces between words with single blank */
  public static String itrim(String source)
  {
    // return source.replaceAll("\\b\\s{2,}\\b", " ");
    return source.replaceAll(" ", "");
  }

  public String[] getAllVeriablesName(String hostName, String serviceName, String URL)
  {
    Log.debugLog(className, "getAllVeriablesName", "", "", "Method started");
    ArrayList alAllVariables = new ArrayList();
    try
    {
      Vector hpdURLData = readFile(getServiceConfFilePath(hostName, serviceName), true);

      if((hpdURLData == null) || (hpdURLData.size() <= 0))
      {
        Log.debugLog(className, "getAllVeriablesName", "", "", "Service File not found, It may be corrupted. service=" + serviceName);
        return new String[]{""};
      }

      for(int k = 0; k < hpdURLData.size(); k++)
      {
        String line = hpdURLData.elementAt(k).toString();
        String temp = line.trim();

        if(temp.startsWith("nsl_search_var")) // Method found
        {
          int indexForClose = line.lastIndexOf(")");
          if((indexForClose == -1) || (indexForClose != line.length() - 2))
            continue;

          String strParam = line.substring(line.indexOf("(") + 1, indexForClose);

          String paramArr[] = itrim(strParam).split(",");
          for(int i = 0; i < paramArr.length; i++)
          {
            paramArr[i] = paramArr[i].trim();
            if(!paramArr[i].toUpperCase().startsWith("LB=") && !paramArr[i].toUpperCase().startsWith("RB=") && !paramArr[i].toUpperCase().startsWith("ORD=") && !paramArr[i].toUpperCase().startsWith("SAVEOFFSET=") && !paramArr[i].toUpperCase().startsWith("SAVELEN=") && !paramArr[i].toUpperCase().startsWith("METHOD=") && !paramArr[i].toUpperCase().startsWith("IGNORECASE="))
            {
              //Log.debugLog(className, "getAllVeriablesName", "", "", "Adding variable for nsl_search_var name = " + paramArr[i].trim());
              if(!paramArr[i].trim().equals(""))
                alAllVariables.add(paramArr[i].trim());
            }
          }
        }
        if(temp.startsWith("nsl_cookie_var")) // Method found
        {
          int indexForClose = line.lastIndexOf(")");
          if((indexForClose == -1) || (indexForClose != line.length() - 2))
            continue;

          String strParam = line.substring(line.indexOf("(") + 1, indexForClose);

          String paramArr[] = itrim(strParam).split(",");
          for(int i = 0; i < paramArr.length; i++)
          {
            paramArr[i] = paramArr[i].trim();
            if(!paramArr[i].startsWith("CookieName=") && !paramArr[i].startsWith("ActionOnNotFound=") && !paramArr[i].startsWith("DefaultValue=") && !paramArr[i].startsWith("StartOffset=") && !paramArr[i].startsWith("SaveLen=") && !paramArr[i].startsWith("Method=") && !paramArr[i].startsWith("Encode=") && !paramArr[i].startsWith("SpecifiedChars=") && !paramArr[i].startsWith("Decode="))
            {
              //Log.debugLog(className, "getAllVeriablesName", "", "", "Adding variable for nsl_search_var name = " + paramArr[i].trim());
              if(!paramArr[i].trim().equals(""))
                alAllVariables.add(paramArr[i].trim());
            }
          }
        }
        else if(temp.startsWith("nsl_decl_var"))
        {
          int indexForClose = line.lastIndexOf(")");
          if((indexForClose == -1) || (indexForClose != line.length() - 2))
            continue;

          String strParam = line.substring(line.indexOf("(") + 1, indexForClose);
          //Log.debugLog(className, "getAllVeriablesName", "", "", "Adding variable for nsl_decl_var name = " + strParam);
          //alAllVariables.add(strParam);
          String paramArr[] = itrim(strParam).split(",");
          String paramName = paramArr[0].trim();
          alAllVariables.add(paramName);
        }
        else if(temp.startsWith("nsl_index_file_var"))
        {
          int indexForClose = line.lastIndexOf(")");
          if((indexForClose == -1) || (indexForClose != line.length() - 2))
            continue;

          String strParam = line.substring(line.indexOf("(") + 1, indexForClose);

          String paramArr[] = itrim(strParam).split(",");
          for(int i = 0; i < paramArr.length; i++)
          {
            paramArr[i] = paramArr[i].trim();
            if(!paramArr[i].toUpperCase().startsWith("FILE=") && !paramArr[i].toUpperCase().startsWith("REFRESH=") && !paramArr[i].toUpperCase().startsWith("INDEXVAR=") && !paramArr[i].toUpperCase().startsWith("MODE=") && !paramArr[i].toUpperCase().startsWith("VAR_VALUE=") && !paramArr[i].toUpperCase().startsWith("FIRSTDATALINE=") && !paramArr[i].toUpperCase().startsWith("COLUMNDELIMITER=") && !paramArr[i].toUpperCase().startsWith("HEADERLINE="))
            {
              //Log.debugLog(className, "getAllVeriablesName", "", "", "Adding variable for nsl_index_file_var name = " + paramArr[i].trim());
              if(!paramArr[i].trim().equals(""))
                alAllVariables.add(paramArr[i].trim());
            }
          }
        }
        else if(temp.startsWith("QUERY_VAR") || temp.startsWith("nsl_query_var"))  //for new api of query_var
        {
          String[] arrURLLine = rptUtilsBean.strToArrayData(temp, " ");
          //Log.debugLog(className, "getAllVeriablesName", "", "", "Adding variable for QUERY_VAR name = " + arrURLLine[1]);
          if(temp.startsWith("QUERY_VAR"))  //for old api we get the parameter name like this 
            alAllVariables.add(arrURLLine[1]);
          else    //for new api we get the parameter name like this 
          {
            String paramName = arrURLLine[1].substring(arrURLLine[1].indexOf("(") + 1, arrURLLine[1].indexOf(","));
            alAllVariables.add(paramName);
          }
        }
        else if(temp.startsWith("nsl_decl_array"))
        {
          int indexForClose = line.lastIndexOf(")");
          if((indexForClose == -1) || (indexForClose != line.length() - 2))
            continue;

          String strParam = line.substring(line.indexOf("(") + 1, indexForClose);

          String paramArr[] = itrim(strParam).split(",");
          for(int i = 0; i < paramArr.length; i++)
          {
            paramArr[i] = paramArr[i].trim();
            if(!paramArr[i].startsWith("Size=") && !paramArr[i].startsWith("DefaultValue="))
            {
              if(!paramArr[i].trim().equals(""))
                alAllVariables.add(paramArr[i].trim());
            }
          }
        }
        else if(temp.startsWith("nsl_static_var"))
        {
          int indexForClose = line.lastIndexOf(")");
          if((indexForClose == -1) || (indexForClose != line.length() - 2))
            continue;

          String strParam = line.substring(line.indexOf("(") + 1, indexForClose);

          String paramArr[] = itrim(strParam).split(",");
          for(int i = 0; i < paramArr.length; i++)
          {
            paramArr[i] = paramArr[i].trim();
            if(!paramArr[i].toUpperCase().startsWith("FILE=") && !paramArr[i].toUpperCase().startsWith("REFRESH=") && !paramArr[i].toUpperCase().startsWith("MODE=") && !paramArr[i].toUpperCase().startsWith("VAR_VALUE=") && !paramArr[i].toUpperCase().startsWith("FIRSTDATALINE=") && !paramArr[i].toUpperCase().startsWith("COLUMNDELIMITER=") && !paramArr[i].toUpperCase().startsWith("HEADERLINE="))
            {
              //Log.debugLog(className, "getAllVeriablesName", "", "", "Adding variable for nsl_static_var name = " + paramArr[i].trim());
              if(!paramArr[i].trim().equals(""))
                alAllVariables.add(paramArr[i].trim());
            }
          }
        }
        else if(temp.startsWith("nsl_date_var"))
        {
          int indexForClose = line.lastIndexOf(")");
          if((indexForClose == -1) || (indexForClose != line.length() - 2))
            continue;

          String strParam = line.substring(line.indexOf("(") + 1, indexForClose);

          String paramArr[] = itrim(strParam).split(",");
          for(int i = 0; i < paramArr.length; i++)
          {
            paramArr[i] = paramArr[i].trim();
            if(!paramArr[i].toUpperCase().startsWith("FORMAT=") && !paramArr[i].toUpperCase().startsWith("OFFSET=") && !paramArr[i].toUpperCase().startsWith("DAYS=") && !paramArr[i].toUpperCase().startsWith("REFRESH="))
            {
              //Log.debugLog(className, "getAllVeriablesName", "", "", "Adding variable for nsl_date_var name = " + paramArr[i].trim());
              if(!paramArr[i].trim().equals(""))
                alAllVariables.add(paramArr[i].trim());
            }
          }
        }
        else if(temp.startsWith("nsl_random_number_var"))
        {
          int indexForClose = line.lastIndexOf(")");
          if((indexForClose == -1) || (indexForClose != line.length() - 2))
            continue;

          String strParam = line.substring(line.indexOf("(") + 1, indexForClose);

          String paramArr[] = itrim(strParam).split(",");
          for(int i = 0; i < paramArr.length; i++)
          {
            paramArr[i] = paramArr[i].trim();
            if(!paramArr[i].toUpperCase().startsWith("MIN=") && !paramArr[i].toUpperCase().startsWith("MAX=") && !paramArr[i].toUpperCase().startsWith("FORMAT=") && !paramArr[i].toUpperCase().startsWith("REFRESH="))
            {
              //Log.debugLog(className, "getAllVeriablesName", "", "", "Adding variable for nsl_random_number_var name = " + paramArr[i].trim());
              if(!paramArr[i].trim().equals(""))
                alAllVariables.add(paramArr[i].trim());
            }
          }
        }
        else if(temp.startsWith("nsl_random_string_var"))
        {
          int indexForClose = line.lastIndexOf(")");
          if((indexForClose == -1) || (indexForClose != line.length() - 2))
            continue;

          String strParam = line.substring(line.indexOf("(") + 1, indexForClose);

          String paramArr[] = itrim(strParam).split(",");
          for(int i = 0; i < paramArr.length; i++)
          {
            paramArr[i] = paramArr[i].trim();
            if(!paramArr[i].toUpperCase().startsWith("MIN=") && !paramArr[i].toUpperCase().startsWith("MAX=") && !paramArr[i].toUpperCase().startsWith("CHARSET=") && !paramArr[i].toUpperCase().startsWith("REFRESH=") && !paramArr[i].toUpperCase().startsWith("ENABLE_ENCODING="))
            {
              //Log.debugLog(className, "getAllVeriablesName", "", "", "Adding variable for nsl_random_string_var name = " + paramArr[i].trim());
              if(!paramArr[i].trim().equals(""))
                alAllVariables.add(paramArr[i].trim());
            }
          }
        }
        else if(temp.startsWith("nsl_unique_number_var"))
        {
          int indexForClose = line.lastIndexOf(")");
          if((indexForClose == -1) || (indexForClose != line.length() - 2))
            continue;

          String strParam = line.substring(line.indexOf("(") + 1, indexForClose);

          String paramArr[] = itrim(strParam).split(",");
          for(int i = 0; i < paramArr.length; i++)
          {
            paramArr[i] = paramArr[i].trim();
            if(!paramArr[i].toUpperCase().startsWith("MIN=") && !paramArr[i].toUpperCase().startsWith("MAX=") && !paramArr[i].toUpperCase().startsWith("FORMAT=") && !paramArr[i].toUpperCase().startsWith("REFRESH="))
            {
              //Log.debugLog(className, "getAllVeriablesName", "", "", "Adding variable for nsl_unique_number_var name = " + paramArr[i].trim());
              if(!paramArr[i].trim().equals(""))
                alAllVariables.add(paramArr[i].trim());
            }
          }
        }
        else if(temp.startsWith("nsl_unique_number_vuser_var"))
        {
          int indexForClose = line.lastIndexOf(")");
          if((indexForClose == -1) || (indexForClose != line.length() - 2))
            continue;

          String strParam = line.substring(line.indexOf("(") + 1, indexForClose);

          String paramArr[] = itrim(strParam).split(",");
          for(int i = 0; i < paramArr.length; i++)
          {
            paramArr[i] = paramArr[i].trim();
            if(!paramArr[i].toUpperCase().startsWith("START=") && !paramArr[i].toUpperCase().startsWith("END=") && !paramArr[i].toUpperCase().startsWith("FORMAT=") && !paramArr[i].toUpperCase().startsWith("REFRESH=") && !paramArr[i].toUpperCase().startsWith("OUTOFVALUE="))
            {
              //Log.debugLog(className, "getAllVeriablesName", "", "", "Adding variable for nsl_unique_number_vuser_var name = " + paramArr[i].trim());
              if(!paramArr[i].trim().equals(""))
                alAllVariables.add(paramArr[i].trim());
            }
          }
        }
        else if(temp.startsWith("nsl_xml_var"))
        {
          int indexForClose = line.lastIndexOf(")");
          if((indexForClose == -1) || (indexForClose != line.length() - 2))
            continue;

          String strParam = line.substring(line.indexOf("(") + 1, indexForClose);

          String paramArr[] = itrim(strParam).split(",");
          for(int i = 0; i < paramArr.length; i++)
          {
            paramArr[i] = paramArr[i].trim();
            if(!paramArr[i].toUpperCase().trim().startsWith("BODYSKIPSTARTBYTES=") && !paramArr[i].toUpperCase().trim().startsWith("BODYSKIPENDBYTES=") && !paramArr[i].toUpperCase().trim().startsWith("NODE=") && !paramArr[i].toUpperCase().trim().startsWith("CONVERT=") && !paramArr[i].toUpperCase().trim().startsWith("RETAINPREVALUE=") && !paramArr[i].toUpperCase().trim().startsWith("VALUE=") && !paramArr[i].toUpperCase().trim().startsWith("WHERE=") && !paramArr[i].toUpperCase().trim().startsWith("ORD="))
            {
              //Log.debugLog(className, "getAllVeriablesName", "", "", "Adding variable for nsl_xml_var name = " + paramArr[i].trim());
              if(!paramArr[i].trim().equals(""))
                alAllVariables.add(paramArr[i].trim());
            }
          }
        }
        else if(temp.startsWith("nsl_request_var"))
        {
          int indexForClose = line.lastIndexOf(")");
          if((indexForClose == -1) || (indexForClose != line.length() - 2))
            continue;

          String strParam = line.substring(line.indexOf("(") + 1, indexForClose);

          String paramArr[] = itrim(strParam).split(",");
          for(int i = 0; i < paramArr.length; i++)
          {
            paramArr[i] = paramArr[i].trim();
            if(!paramArr[i].toUpperCase().trim().startsWith("ENCODE=") && !paramArr[i].toUpperCase().trim().startsWith("SPECIFIEDCHARS=") && !paramArr[i].toUpperCase().trim().startsWith("ASSOCIATEDOBJECT=") && !paramArr[i].toUpperCase().trim().startsWith("ORD=") && !paramArr[i].toUpperCase().trim().startsWith("STARTOFFSET=") && !paramArr[i].toUpperCase().trim().startsWith("SAVELEN=") && !paramArr[i].toUpperCase().trim().startsWith("ACTIONONNOTFOUND=") && !paramArr[i].toUpperCase().trim().startsWith("DEFAULTVALUE=") && !paramArr[i].toUpperCase().trim().startsWith("METHOD=") && !paramArr[i].toUpperCase().trim().startsWith("DECODE="))
            {
              //Log.debugLog(className, "getAllVeriablesName", "", "", "Adding variable for nsl_xml_var name = " + paramArr[i].trim());
              if(!paramArr[i].trim().equals(""))
                alAllVariables.add(paramArr[i].trim());
            }
          }
        }
      }

      String[] arrAllVariables = new String[alAllVariables.size()];
      for(int x = 0; x < alAllVariables.size(); x++)
        arrAllVariables[x] = alAllVariables.get(x).toString();

      return arrAllVariables;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getAllVeriablesName", "", "", "Exception - ", ex);
      return new String[]{""};
    }
  }

  public String[] getSearchVeriablesName(String hostName, String serviceName, String URL)
  {
    Log.debugLog(className, "getSearchVeriablesName", "", "", "Method started");
    ArrayList alSearchVariables = new ArrayList();
    try
    {
      Vector hpdURLData = readFile(getServiceConfFilePath(hostName, serviceName), true);

      if((hpdURLData == null) || (hpdURLData.size() <= 0))
      {
        Log.debugLog(className, "getSearchVeriablesName", "", "", "Service File not found, It may be corrupted. service=" + serviceName);
        return new String[]{""};
      }

      for(int k = 0; k < hpdURLData.size(); k++)
      {
        String line = hpdURLData.elementAt(k).toString();
        String temp = line.trim();

        if(temp.startsWith("nsl_search_var")) // Method found
        {
          int indexForClose = line.lastIndexOf(")");
          if((indexForClose == -1) || (indexForClose != line.length() - 2))
            continue;

          String strParam = line.substring(line.indexOf("(") + 1, indexForClose);

          String paramArr[] = itrim(strParam).split(",");
          for(int i = 0; i < paramArr.length; i++)
          {
            paramArr[i] = paramArr[i].trim();
            if(!paramArr[i].toUpperCase().startsWith("LB=") && !paramArr[i].toUpperCase().startsWith("RB=") && !paramArr[i].toUpperCase().startsWith("ORD=") && !paramArr[i].toUpperCase().startsWith("SAVEOFFSET=") && !paramArr[i].toUpperCase().startsWith("SAVELEN=") && !paramArr[i].toUpperCase().startsWith("METHOD=") && !paramArr[i].toUpperCase().startsWith("IGNORECASE="))
            {
              //Log.debugLog(className, "getSearchVeriablesName", "", "", "Adding variable for nsl_search_var name = " + paramArr[i].trim());
              if(!paramArr[i].trim().equals(""))
                alSearchVariables.add(paramArr[i].trim());
            }
          }
        }
      }

      String[] arrSearchVariables = new String[alSearchVariables.size()];
      for(int x = 0; x < alSearchVariables.size(); x++)
        arrSearchVariables[x] = alSearchVariables.get(x).toString();

      return arrSearchVariables;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getSearchVeriablesName", "", "", "Exception - ", ex);
      return new String[]{""};
    }
  }

  // Methods for reading the File
  public Vector readFile(String fileWithPath, boolean ignoreComments)
  {
    Log.debugLog(className, "readFile", "", "", "Method called. FIle Name = " + fileWithPath);

    try
    {
      Vector vecFileData = new Vector();
      String strLine;

      File confFile = openFile(fileWithPath);

      if(!confFile.exists())
      {
        Log.debugLog(className, "readFile", "", "", "File not found, filename - " + fileWithPath);
        return null;
      }

      FileInputStream fis = new FileInputStream(fileWithPath);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));

      while((strLine = br.readLine()) != null)
      {
        if(ignoreComments && strLine.startsWith("#") && !strLine.startsWith("#URL") && !strLine.startsWith("# URL"))
          continue;
        if(ignoreComments && strLine.length() == 0)
          continue;

        //Log.debugLog(className, "readFile", "", "", "Adding line in vector. Line = " + strLine);
        vecFileData.add(strLine);
      }

      br.close();
      fis.close();

      return vecFileData;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "readFile", "", "", "Exception - ", e);
      return null;
    }
  }

  public boolean writeToFile(String fileWithPath, Vector vecModified, String hostName, String serviceName)
  {
    Log.debugLog(className, "writeToFile", "", "", "Method called. FIle Name = " + fileWithPath);

    try
    {
      FileOutputStream out2 = new FileOutputStream(new File(fileWithPath));
      PrintStream requestFile = new PrintStream(out2);
      for(int ad = 0; ad < vecModified.size(); ad++)
        requestFile.println(vecModified.get(ad).toString());

      requestFile.close();
      out2.close();
      writeToLogFile(hostName, serviceName);
      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "writeToFile", "", "", "Exception - ", e);
      return false;
    }
  }

  public boolean writeToLogFile(String hostName, String serviceName)
  {
    Log.debugLog(className, "writeToLogFile", "", "", "Method called");

    try
    {
      if(serviceName.equals(""))
        return true;

      FileOutputStream out2 = new FileOutputStream(new File(getServicePath(hostName, serviceName) + "/service.log"));
      PrintStream requestFile = new PrintStream(out2);

      SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd/yyyy");
      SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");

      Date now = new Date();

      String strDate = sdfDate.format(now);
      String strTime = sdfTime.format(now);

      requestFile.println("LMD=" + strDate + " " + strTime + "|LMB=" + userName);
      requestFile.close();
      out2.close();
      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "writeToLogFile", "", "", "Exception - ", e);
      return false;
    }
  }

  // Open file and return as File object
  private File openFile(String fileName)
  {
    try
    {
      File tempFile = new File(fileName);
      return(tempFile);
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "openFile", "", "", "Exception - ", e);
      return null;
    }
  }

  private boolean isEndWithQuotes(String str, boolean isFirstParse)
  {
    if(isFirstParse)
    {
      if(str.length() < 2)
        return false;
    }

    if(str.endsWith("\""))
    {
      if(str.length() > 1)
      {
        if(str.charAt(str.length() - 2) != '\\')
          return true;
      }
      else
        return true;
    }
    return false;
  }

  /**
   * This method is called when Token value which takes value is string format like(TEXT, ID in below example) does ends with '"' then it will tell that it is end or not.
   *
   * Need to check because of ',' in it value.
   *
   * Used for example: nsl_web_find(TEXT="what=is=this", PAGE=*, FAIL=NOTFOUND, ID="-");
   *
   * @param splited
   *          - contains string in TEXT="what=is=this" and others splited with ','
   *
   * @param toCheck
   *          - as example: new String[]{"PAGE", "FAIL", "ID"};
   *
   * @param firstPart
   *          - as example: "what=is=this"
   *
   * @param index
   *          - as for this example: 0
   *
   * @param vecForSplitStr
   * @return
   */
  private String getStrToMatch(String splited[], String toCheck[], String firstPart, int index, Vector vecForSplitStr)
  {
    String temp = firstPart;
    if(temp.equals(""))
      temp = "\"";
    try
    {
      boolean getDoubleQuotes = false;
      while((splited.length > index) && (!getDoubleQuotes))
      {
        // To ignore current one
        index = index + 1;
        String tempStr = splited[index].trim();
        for(int i = 0; i < toCheck.length; i++)
        {
          if(tempStr.toUpperCase().startsWith(toCheck[i].toUpperCase()))
          {
            return null;
          }
        }
        vecForSplitStr.add(splited[index]);
        temp = temp + "," + splited[index];

        if(isEndWithQuotes(splited[index], false))
          getDoubleQuotes = true;
      }
      if(!getDoubleQuotes)
      {
        return null;
      }
    }
    catch(Exception e)
    {
      return null;
    }
    return temp;
  }

  private boolean isValidORDValue(String ordValue)
  {
    if(ordValue.equals("ALL"))
      return true;
    else if(ordValue.equals("ANY"))
      return true;
    else
    // Here must be any integer more than 0(zero)
    {
      long ord;
      try
      {
        ord = Long.parseLong(ordValue);
        if(ord < 1)
          return false;
      }
      catch(Exception e)
      {
        return false;
      }
    }
    return true;
  }

  // Method to restart the HPD
  public boolean restartHPD()
  {
    Log.debugLog(className, "restartHPD", "", "", "Method Start.");
    try
    {
      String cmdName = "/etc/init.d/" + getHPDWork();
      Log.debugLog(className, "restartHPD", "", "", "cmdName = " + cmdName);
      String cmdArgs = "restart";
      CmdExec objCmdExec = new CmdExec();
      Vector vecCmdOutPut = objCmdExec.getResultByCommand(cmdName, cmdArgs, CmdExec.SYSTEM_CMD, null, "root");
      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "restartHPD", "", "", "Exception", ex);
      return false;
    }
  }

  // Method to stop the HPD
  public boolean stopHPD()
  {
    Log.debugLog(className, "stopHPD", "", "", "Method Start.");
    try
    {
      String cmdName = "/etc/init.d/" + getHPDWork();
      Log.debugLog(className, "stopHPD", "", "", "cmdName = " + cmdName);
      String cmdArgs = "stop";
      CmdExec objCmdExec = new CmdExec();
      Vector vecCmdOutPut = objCmdExec.getResultByCommand(cmdName, cmdArgs, CmdExec.SYSTEM_CMD, null, "root");
      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "stopHPD", "", "", "Exception", ex);
      return false;
    }
  }

  // Method to stop the HPD
  public boolean startHPD()
  {
    Log.debugLog(className, "startHPD", "", "", "Method Start.");
    try
    {
      String cmdName = "/etc/init.d/" + getHPDWork();
      Log.debugLog(className, "startHPD", "", "", "cmdName = " + cmdName);
      String cmdArgs = "start";
      CmdExec objCmdExec = new CmdExec();
      Vector vecCmdOutPut = objCmdExec.getResultByCommand(cmdName, cmdArgs, CmdExec.SYSTEM_CMD, null, "root");
      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "startHPD", "", "", "Exception", ex);
      return false;
    }
  }

  // Method to check that HPD started successfully or not
  public boolean showHPD()
  {
    Log.debugLog(className, "showHPD", "", "", "Method Start.");
    String error = "";
    try
    {
      String cmdName = "/etc/init.d/" + getHPDWork();
      String cmdArgs = "show";
      CmdExec objCmdExec = new CmdExec();
      Vector vecCmdOutPut = new Vector();
      boolean runningFlag = objCmdExec.getResultByCommand(vecCmdOutPut, cmdName, cmdArgs, CmdExec.SYSTEM_CMD, null, "root");

      Log.debugLog(className, "showHPD", "", "", "HPD running or not = " + runningFlag);
      return runningFlag;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "showHPD", "", "", "Exception", ex);
      return false;
    }
  }

  // Method to check that HPD started successfully or not
  public String checkHPD()
  {
    Log.debugLog(className, "showHPD", "", "", "Method Start.");
    String error = "";
    try
    {
      String cmdName = "/etc/init.d/" + getHPDWork();
      String cmdArgs = "check_after_restart";
      CmdExec objCmdExec = new CmdExec();
      Vector vecCmdOutPut = objCmdExec.getResultByCommand(cmdName, cmdArgs, CmdExec.SYSTEM_CMD, null, "root");

      Log.debugLog(className, "checkHPD", "", "", "vecCmdOutPut.size() = " + vecCmdOutPut.size());
      //In error case it will collect output from vector
      if(vecCmdOutPut.size() > 1)
      {
        for(int jk = 0; jk < (vecCmdOutPut.size() - 1); jk++)
        {
          if(error.equals(""))
            error = vecCmdOutPut.elementAt(jk).toString();
          else
            error = error + "\\n" + vecCmdOutPut.elementAt(jk).toString();
        }
        return error;
      }
      else
      //hpd start successfully
      {
        return "Changes Activated Successfully";
      }
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getResponse", "", "", "Exception", ex);
      return "";
    }
  }

  //This method is to make a copy of service
  public boolean createServiceCopy(String hostName, String serviceName, String URL, String newServiceName)
  {
    Log.debugLog(className, "createServiceCopy", "", "", "Method Start serviceName=" + serviceName + ", newServiceName=" + newServiceName);

    try
    {
      File sourceLocation = new File(getCorrelationPath() + hostName + "/services/" + serviceName + "/");
      File targetLocation = new File(getCorrelationPath() + hostName + "/services/" + newServiceName + "/");
      Log.debugLog(className, "createServiceCopy", "", "", "Start Copy Directory");
      copyDirectory(sourceLocation, targetLocation);

      Vector completeControlFile = readFile(getServiceConfFilePath(hostName, newServiceName), false);
      Vector modifiedVector = new Vector();

      if(completeControlFile == null)
      {
        Log.debugLog(className, "createServiceCopy", "", "", "service.conf file not found, It may be corrupted.");
        return false;
      }

      for(int yy = 0; yy < completeControlFile.size(); yy++)
      {
        String line = completeControlFile.elementAt(yy).toString();

        if(line.trim().startsWith("URL") || line.trim().startsWith("#URL") || line.trim().startsWith("# URL")) // Method found
        {
          //this flag is used to get service is with unique URL or not
          boolean enableUrlFlag = true;
          String[][] arrDataValuesURL = getNOServices();
          for(int i = 0; i < arrDataValuesURL.length; i++)
          {
            if(URL.trim().equals((arrDataValuesURL[i][2]).trim()))
            {
              if((arrDataValuesURL[i][14]).trim().equals("enabled"))
              {
                enableUrlFlag = false;
                break;
              }
            }
          }

          if(enableUrlFlag)
            modifiedVector.add("URL " + URL);
          else
            modifiedVector.add("#URL " + URL);
        }
        else
        {
          modifiedVector.add(line);
        }
      }

      if(!writeToFile(getServiceConfFilePath(hostName, newServiceName), modifiedVector, hostName, newServiceName))
        return false;

      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "createServiceCopy", "", "", "Exception", ex);
      return false;
    }
  }

  public void copyDirectory(File sourceLocation, File targetLocation)
  {
    try
    {
      if(sourceLocation.isDirectory())
      {
        if(!targetLocation.exists())
        {
          targetLocation.mkdir();
        }

        String[] children = sourceLocation.list();
        for(int i = 0; i < children.length; i++)
        {
          copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
        }
      }
      else
      {
        InputStream in = new FileInputStream(sourceLocation);
        OutputStream out = new FileOutputStream(targetLocation);

        // Copy the bits from instream to outstream
        byte[] buf = new byte[1024];
        int len;
        while((len = in.read(buf)) > 0)
        {
          out.write(buf, 0, len);
        }
        in.close();
        out.close();
      }
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "copyDirectory", "", "", "Exception", ex);
    }
  }

  public String[] getAllURLByHost(String hostName)
  {
    Log.debugLog(className, "getAllURLByHost", "", "", "Method Start.");
    ArrayList alURL = new ArrayList();
    try
    {
      String[] hostNames = getHOSTNames();
      if(hostNames == null || hostNames.length <= 0)
        return new String[0];
      for(int i = 0; i < hostNames.length; i++)
      {
        String[] serviceNames = getServicesByHostName(hostNames[i]);
        for(int j = 0; j < serviceNames.length; j++)
        {
          alURL.add(getURLByHostAndService(hostNames[i], serviceNames[j]));
        }
      }

      String[] arrURL = new String[alURL.size()];
      for(int k = 0; k < alURL.size(); k++)
      {
        arrURL[k] = alURL.get(k).toString();
      }
      return arrURL;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getAllURLByHost", "", "", "Exception", ex);
      return new String[0];
    }
  }

  //Method to commit any service like CVS.
  //It will create a version for service directory.
  //there will be a .verion directory and version.dat in each service
  //.version dir will have the folders like 1.1, 1.2  etc. for versions
  //commit will create a new dir for next version for example in above case it will create new dir as 1.3
  //and copy all files from service folder except .version.dat file in to 1.3
  public ArrayList cvsCommitService(String hostName, String serviceName, String author, String comments)
  {
    Log.debugLog(className, "cvsCommitService", "", "", "Method started");
    ArrayList outPut = new ArrayList();
    Vector vecCmdOutPut = new Vector();
    String servicePath = getServicePath(hostName, serviceName);
    try
    {
      String cmdName = "nsi_cvs";
      String cmdArgs = "-o commit -s " + servicePath + " -u " + author + " -m \"" + comments + "\"";

      Log.debugLog(className, "cvsCommitService", "", "", "cmdArgs = " + cmdArgs);

      CmdExec objCmdExec = new CmdExec();

      boolean result = true;

      vecCmdOutPut = objCmdExec.getResultByCommand(cmdName, cmdArgs, CmdExec.SYSTEM_CMD, null, "root");

      if((vecCmdOutPut.size() > 0) && ((String)vecCmdOutPut.lastElement()).startsWith("ERROR"))
      {
        result = false;
      }

      if(!result)
      {
        Log.debugLog(className, "cvsCommitService", "", "", "nsi_cvs commit failed");
        outPut.add("Error");
        outPut.add(vecCmdOutPut);
        return outPut;
      }

      outPut.add("Success");
      outPut.add(vecCmdOutPut);
      return outPut;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "cvsCommitService", "", "", "Exception - ", e);
      outPut.add("Error");
      vecCmdOutPut = new Vector();
      vecCmdOutPut.add("Exception: " + e);
      outPut.add(vecCmdOutPut);
      return outPut;
    }
  }

  public ArrayList cvsDeleteService(String hostName, String serviceName, String version)
  {
    Log.debugLog(className, "cvsDeleteService", "", "", "Method started");
    ArrayList outPut = new ArrayList();
    Vector vecCmdOutPut = new Vector();
    String servicePath = getServicePath(hostName, serviceName);
    try
    {
      String cmdName = "nsi_cvs";
      String cmdArgs = "-o delete -s " + servicePath + " -r " + version;

      Log.debugLog(className, "cvsDeleteService", "", "", "cmdArgs = " + cmdArgs);

      CmdExec objCmdExec = new CmdExec();

      boolean result = true;

      vecCmdOutPut = objCmdExec.getResultByCommand(cmdName, cmdArgs, CmdExec.SYSTEM_CMD, null, "root");

      if((vecCmdOutPut.size() > 0) && ((String)vecCmdOutPut.lastElement()).startsWith("ERROR"))
      {
        result = false;
      }

      if(!result)
      {
        Log.debugLog(className, "cvsDeleteService", "", "", "nsi_cvs delete failed");
        outPut.add("Error");
        outPut.add(vecCmdOutPut);
        return outPut;
      }

      outPut.add("Success");
      outPut.add(vecCmdOutPut);
      return outPut;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "cvsCommitService", "", "", "Exception - ", e);
      outPut.add("Error");
      vecCmdOutPut = new Vector();
      vecCmdOutPut.add("Exception: " + e);
      outPut.add(vecCmdOutPut);
      return outPut;
    }
  }

  //Method to update any service from specified varsion.
  //It will get the files from specified version dir to service directory.
  public ArrayList cvsUpdateService(String hostName, String serviceName, String version)
  {
    Log.debugLog(className, "cvsUpdateService", "", "", "Method started");
    ArrayList outPut = new ArrayList();
    Vector vecCmdOutPut = new Vector();
    String servicePath = getServicePath(hostName, serviceName);

    try
    {
      String cmdName = "nsi_cvs";
      String cmdArgs = "-o update -s " + servicePath;

      if(!version.equals(""))
        cmdArgs = cmdArgs + " -r " + version;

      Log.debugLog(className, "cvsUpdateService", "", "", "cmdArgs = " + cmdArgs);

      CmdExec objCmdExec = new CmdExec();

      boolean result = true;

      vecCmdOutPut = objCmdExec.getResultByCommand(cmdName, cmdArgs, CmdExec.SYSTEM_CMD, null, "root");

      if((vecCmdOutPut.size() > 0) && ((String)vecCmdOutPut.lastElement()).startsWith("ERROR"))
      {
        result = false;
      }

      if(!result)
      {
        Log.debugLog(className, "cvsUpdateService", "", "", "nsi_cvs update failed");
        outPut.add("Error");
        outPut.add(vecCmdOutPut);
        return outPut;
      }

      outPut.add("Success");
      outPut.add(vecCmdOutPut);
      return outPut;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "cvsUpdateService", "", "", "Exception - ", e);
      outPut.add("Error");
      vecCmdOutPut = new Vector();
      vecCmdOutPut.add("Exception: " + e);
      outPut.add(vecCmdOutPut);
      return outPut;
    }
  }

  //this method is used to change the sequence of response template in conf file
  public boolean changeSequenceOfTemplate(String hostName, String serviceName, String URL, String template, String sequence)
  {
    Log.debugLog(className, "changeSequenceOfTemplate", "", "", "Method started");
    String[] arrDatavalue = getResponseTemplateNamesByURL(hostName, serviceName, URL);
    ArrayList arrTemplateList = new ArrayList();

    for(int i = 0; i < arrDatavalue.length; i++)
    {
      arrTemplateList.add(arrDatavalue[i]);
    }

    arrTemplateList.remove(template);
    arrTemplateList.add(Integer.parseInt(sequence) - 1, template);

    Vector completeControlFile = readFile(getServiceConfFilePath(hostName, serviceName), false);
    Vector modifiedVector = new Vector();
    Vector removeTemplate = new Vector();
    boolean encode = isFileEncoded(hostName, serviceName, URL);

    if(completeControlFile == null)
      return false;

    String strSearch = "RESPONSE_TEMPLATE";

    for(int yy = 0; yy < completeControlFile.size(); yy++)
    {
      String line = completeControlFile.elementAt(yy).toString().trim();

      if(line.startsWith(strSearch))
        removeTemplate.add(line);
      else
        modifiedVector.add(line);
    }

    Vector newModified = new Vector();
    for(int i = 0; i < arrTemplateList.size(); i++)
    {
      for(int j = 0; j < removeTemplate.size(); j++)
      {
        String line = removeTemplate.elementAt(j).toString().trim();
        String[] arrURLLine = rptUtilsBean.strToArrayData(line, " ");
        if(arrURLLine[1].equals(arrTemplateList.get(i).toString()))
          modifiedVector.add(line);
      }
    }

    if(!writeToFile(getServiceConfFilePath(hostName, serviceName), modifiedVector, hostName, serviceName))
      return false;

    return true;
  }

  public static int countOccurrences(String find, String str)
  {
    int count = 0;
    int lastIndex = 0;
    //int count = 0;

    while(lastIndex != -1)
    {
      lastIndex = str.indexOf(find, lastIndex);

      if(lastIndex != -1)
      {
        count++;
        lastIndex += find.length();
      }
    }

    return count;
  }

  /*
   * this method is used to change the parameter name
   */
  public boolean changeParameterName(String hostName, String serviceName, String URL, String oldParameterName, String newParameterName, StringBuffer occurenceCount, StringBuffer strCallBackMatch)
  {
    boolean bool = true;
    int occurCount = 0;
    int callBackOccurCount = 0;
    ArrayList responseFile = new ArrayList();
    String strSearch = "RESPONSE_TEMPLATE";
    Log.debugLog(className, "changeParameterName", "", "", "Method started");

    Vector completeControlFile = readFile(getServiceConfFilePath(hostName, serviceName), false);
    if(completeControlFile == null)
    {
      occurenceCount.append(occurCount);
      strCallBackMatch.append(callBackOccurCount);
      return false;
    }

    Vector modifiedFile = new Vector();

    for(int yy = 0; yy < completeControlFile.size(); yy++)
    {
      String line = completeControlFile.elementAt(yy).toString().trim();

      if(!line.startsWith("# URL") && !line.startsWith("#URL") && !line.startsWith("URL") && !line.startsWith("nsl_decl_array") && !line.startsWith("nsl_xml_var") && !line.startsWith("nsl_search_var") && !line.startsWith("QUERY_VAR") && !line.startsWith("nsl_query_var") && !line.startsWith("nsl_decl_var") && !line.startsWith("nsl_static_var") && !line.startsWith("nsl_index_file_var") && !line.startsWith("nsl_date_var") && !line.startsWith("nsl_index_file_var") && !line.startsWith("nsl_random_number_var ") && !line.startsWith("nsl_random_string_var ") && !line.startsWith("nsl_unique_number_var ") && !line.startsWith("nsl_cookie_var ") && !line.startsWith("nsl_select_index_datasource ") && !line.startsWith("nsl_request_var"))
      {
        occurCount = occurCount + countOccurrences("Variable(" + oldParameterName.trim() + ",", line);
        occurCount = occurCount + countOccurrences("Variable(" + oldParameterName.trim() + ")" + ",", line);
        line = line.replace("Variable(" + oldParameterName.trim() + ",", "Variable(" + newParameterName.trim() + ",");
        line = line.replace("Variable(" + oldParameterName.trim() + ")", "Variable(" + newParameterName.trim() + ")");
        StringBuffer countParameterInNewFormat = new StringBuffer();
        line = replacePattern(line, oldParameterName, newParameterName, countParameterInNewFormat);
        occurCount = occurCount + Integer.parseInt(countParameterInNewFormat.toString());
      }

      if(line.startsWith("nsl_index_file_var"))
      {
        occurCount = occurCount + countOccurrences("IndexVar=" + oldParameterName.trim() + ",", line);
        line = line.replace("IndexVar=" + oldParameterName.trim() + ",", "IndexVar=" + newParameterName.trim() + ",");
      }

      String[][] arrDataValues = getResponseTemplateInfoByURL(hostName, serviceName, URL);
      for(int i = 0; i < arrDataValues.length; i++)
      {
        responseFile.add(arrDataValues[i][2]);
      }

      modifiedFile.add(line);
    }
    if(!writeToFile(getServiceConfFilePath(hostName, serviceName), modifiedFile, hostName, serviceName))
      bool = false;

    //reading the service.c file and if the value will get , then value will be replace
    File service = new File(getServicePath(hostName, serviceName) + "service.c");
    if(service.exists())
    {
      Vector completeServiceFile = readFile(getServicePath(hostName, serviceName) + "service.c", false);
      Vector modifiedServiceFile = new Vector();
      if(completeServiceFile != null)
      {
        for(int yy = 0; yy < completeServiceFile.size(); yy++)
        {
          String line = completeServiceFile.elementAt(yy).toString().trim();
          occurCount = occurCount + countOccurrences("{" + oldParameterName.trim() + "}", line);
          line = line.replace("{" + oldParameterName.trim() + "}", "{" + newParameterName.trim() + "}");
          callBackOccurCount = callBackOccurCount + countOccurrences(oldParameterName.trim(), line);
          modifiedServiceFile.add(line);
        }
      }
      if(!writeToFile(getServicePath(hostName, serviceName) + "service.c", modifiedServiceFile, hostName, serviceName))
        bool = false;
    }
    //reading all request and response file
    for(int i = 0; i < responseFile.size(); i++)
    {
      File F1 = new File(getServicePath(hostName, serviceName) + responseFile.get(i).toString());
      if(F1.exists())
      {
        Vector completeServiceFile = readFile(getServicePath(hostName, serviceName) + responseFile.get(i).toString(), false);
        Vector modifiedServiceFile = new Vector();
        if(completeServiceFile != null)
        {
          for(int yy = 0; yy < completeServiceFile.size(); yy++)
          {
            String line = completeServiceFile.elementAt(yy).toString().trim();
            occurCount = occurCount + countOccurrences("{" + oldParameterName.trim() + "}", line);
            occurCount = occurCount + countOccurrences("Value(" + oldParameterName.trim() + ")", line);
            occurCount = occurCount + countOccurrences("Count(" + oldParameterName.trim() + ")", line);
            line = line.replace("{" + oldParameterName.trim() + "}", "{" + newParameterName.trim() + "}");
            line = line.replace("Value(" + oldParameterName.trim() + ")", "Value(" + newParameterName.trim() + ")");
            line = line.replace("Count(" + oldParameterName.trim() + ")", "Count(" + newParameterName.trim() + ")");
            modifiedServiceFile.add(line);
          }
        }
        if(!writeToFile(getServicePath(hostName, serviceName) + responseFile.get(i).toString(), modifiedServiceFile, hostName, serviceName))
          bool = false;
      }

      File F2 = new File(getServicePath(hostName, serviceName) + responseFile.get(i).toString() + ".req");
      if(F2.exists())
      {
        Vector completeServiceFile = readFile(getServicePath(hostName, serviceName) + responseFile.get(i).toString() + ".req", false);
        Vector modifiedServiceFile = new Vector();
        if(completeServiceFile != null)
        {
          for(int yy = 0; yy < completeServiceFile.size(); yy++)
          {
            String line = completeServiceFile.elementAt(yy).toString().trim();
            occurCount = occurCount + countOccurrences("{" + oldParameterName.trim() + "}", line);
            occurCount = occurCount + countOccurrences("Value(" + oldParameterName.trim() + ")", line);
            occurCount = occurCount + countOccurrences("Count(" + oldParameterName.trim() + ")", line);
            line = line.replace("{" + oldParameterName.trim() + "}", "{" + newParameterName.trim() + "}");
            line = line.replace("Value(" + oldParameterName.trim() + ")", "Value(" + newParameterName.trim() + ")");
            line = line.replace("Count(" + oldParameterName.trim() + ")", "Count(" + newParameterName.trim() + ")");
            modifiedServiceFile.add(line);
          }
        }
        if(!writeToFile(getServicePath(hostName, serviceName) + responseFile.get(i).toString() + ".req", modifiedServiceFile, hostName, serviceName))
          bool = false;
      }
    }

    occurenceCount.append(occurCount);
    strCallBackMatch.append(callBackOccurCount);
    return bool;
  }

  /*
   * this method is used to change the parameter name
   */
  public boolean usedParameter(String hostName, String serviceName, String URL, String oldParameterName, StringBuffer occurenceCount, StringBuffer strCallBackMatch)
  {
    boolean bool = true;
    int occurCount = 0;
    int callBackOccurCount = 0;
    ArrayList responseFile = new ArrayList();
    String strSearch = "RESPONSE_TEMPLATE";
    Log.debugLog(className, "usedParameter", "", "", "Method started");

    Vector completeControlFile = readFile(getServiceConfFilePath(hostName, serviceName), false);
    if(completeControlFile == null)
    {
      occurenceCount.append(occurCount);
      strCallBackMatch.append(callBackOccurCount);
      return false;
    }

    for(int yy = 0; yy < completeControlFile.size(); yy++)
    {
      String line = completeControlFile.elementAt(yy).toString().trim();

      if(!line.startsWith("URL") && !line.startsWith("nsl_decl_array") && !line.startsWith("nsl_xml_var") && !line.startsWith("nsl_search_var") && !line.startsWith("QUERY_VAR") && !line.startsWith("nsl_decl_var") && !line.startsWith("nsl_static_var") && !line.startsWith("nsl_index_file_var") && !line.startsWith("nsl_date_var") && !line.startsWith("nsl_index_file_var") && !line.startsWith("nsl_random_number_var ") && !line.startsWith("nsl_random_string_var ") && !line.startsWith("nsl_unique_number_var ") && !line.startsWith("nsl_cookie_var "))
      {
        occurCount = occurCount + countOccurrences("Variable(" + oldParameterName.trim() + ",", line);
        occurCount = occurCount + countOccurrences("Variable(" + oldParameterName.trim() + ")" + ",", line);
        StringBuffer countParameterInNewFormat = new StringBuffer();
        replacePattern(line, oldParameterName, "", countParameterInNewFormat);
        occurCount = occurCount + Integer.parseInt(countParameterInNewFormat.toString());
      }

      if(line.startsWith("nsl_index_file_var"))
      {
        occurCount = occurCount + countOccurrences("IndexVar=" + oldParameterName.trim() + ",", line);
      }

      String[][] arrDataValues = getResponseTemplateInfoByURL(hostName, serviceName, URL);
      for(int i = 0; i < arrDataValues.length; i++)
      {
        responseFile.add(arrDataValues[i][2]);
      }
    }

    //reading the service.c file and if the value will get , then value will be replace
    File service = new File(getServicePath(hostName, serviceName) + "service.c");
    if(service.exists())
    {
      Vector completeServiceFile = readFile(getServicePath(hostName, serviceName) + "service.c", false);
      if(completeServiceFile != null)
      {
        for(int yy = 0; yy < completeServiceFile.size(); yy++)
        {
          String line = completeServiceFile.elementAt(yy).toString().trim();
          occurCount = occurCount + countOccurrences("{" + oldParameterName.trim() + "}", line);
          callBackOccurCount = callBackOccurCount + countOccurrences(oldParameterName.trim(), line);
        }
      }
    }
    //reading all request and response file
    for(int i = 0; i < responseFile.size(); i++)
    {
      File F1 = new File(getServicePath(hostName, serviceName) + responseFile.get(i).toString());
      if(F1.exists())
      {
        Vector completeServiceFile = readFile(getServicePath(hostName, serviceName) + responseFile.get(i).toString(), false);
        if(completeServiceFile != null)
        {
          for(int yy = 0; yy < completeServiceFile.size(); yy++)
          {
            String line = completeServiceFile.elementAt(yy).toString().trim();
            occurCount = occurCount + countOccurrences("{" + oldParameterName.trim() + "}", line);
            occurCount = occurCount + countOccurrences("Value(" + oldParameterName.trim() + ")", line);
            occurCount = occurCount + countOccurrences("Count(" + oldParameterName.trim() + ")", line);
          }
        }
      }

      File F2 = new File(getServicePath(hostName, serviceName) + responseFile.get(i).toString() + ".req");
      if(F2.exists())
      {
        Vector completeServiceFile = readFile(getServicePath(hostName, serviceName) + responseFile.get(i).toString() + ".req", false);
        if(completeServiceFile != null)
        {
          for(int yy = 0; yy < completeServiceFile.size(); yy++)
          {
            String line = completeServiceFile.elementAt(yy).toString().trim();
            occurCount = occurCount + countOccurrences("{" + oldParameterName.trim() + "}", line);
            occurCount = occurCount + countOccurrences("Value(" + oldParameterName.trim() + ")", line);
            occurCount = occurCount + countOccurrences("Count(" + oldParameterName.trim() + ")", line);
          }
        }
      }
    }

    occurenceCount.append(occurCount);
    strCallBackMatch.append(callBackOccurCount);
    return bool;
  }

  public String replacePattern(String wholeStr, String ptrnStr, String newPtrn, StringBuffer countOccurence)
  {
    Log.debugLog(className, "replacePattern", "", "", "Method started");
    Log.debugLog(className, "replacePattern", "", "", "replacePatthern method start for wholeStr = " + wholeStr + " and ptrnStr = " + ptrnStr);
    int count = 1;
    int countVariable = 0;
    for(int i = 0; i < wholeStr.length(); i++)
    {
      if(wholeStr.charAt(i) == '"')
      {
        if(count % 2 == 0 && wholeStr.charAt(i - 1) == '\\')
          continue;
        count++;
      }
      if(count % 2 == 1 && wholeStr.substring(i, wholeStr.length()).startsWith(ptrnStr))
      {
        if((i + ptrnStr.length()) < wholeStr.length() && "+-*/%><!= ".contains(wholeStr.charAt(i + ptrnStr.length()) + ""))
        {
          if(i > 0)
          {
            if("+-*/%><!= ".contains(wholeStr.charAt(i - 1) + ""))
            {
              String leftPart = wholeStr.substring(0, i);
              String rightPart = wholeStr.substring(i + ptrnStr.length(), wholeStr.length());
              wholeStr = leftPart + newPtrn + rightPart;
              countVariable++;
            }
          }
          else if(i == 0)
          {
            String rightPart = wholeStr.substring(i + ptrnStr.length(), wholeStr.length());
            wholeStr = newPtrn + rightPart;
            countVariable++;
          }
        }
        else if((i + ptrnStr.length()) == wholeStr.length())
        {
          if(i > 0 && "+-*/%><!= ".contains(wholeStr.charAt(i - 1) + ""))
          {
            String leftPart = wholeStr.substring(0, i);
            wholeStr = leftPart + newPtrn;
            countVariable++;
          }
          else
          {
            String leftPart = wholeStr.substring(0, i);
            wholeStr = leftPart + newPtrn;
            countVariable++;
          }
        }
      }
    }
    countOccurence.append(countVariable);
    return wholeStr;
  }

  //Method to diff to show the difference between two version in services like CVS.
  public ArrayList cvsDiffService(String hostName, String serviceName, String version, String compareVersion)
  {
    Log.debugLog(className, "cvsDiffService", "", "", "Method started");
    ArrayList outPut = new ArrayList();
    Vector vecCmdOutPut = new Vector();
    String servicePath = getServicePath(hostName, serviceName);
    try
    {
      String cmdName = "nsi_cvs";
      String cmdArgs = "-o diff -s " + servicePath + " -r " + version + " -r " + compareVersion;

      Log.debugLog(className, "cvsDiffService", "", "", "cmdArgs = " + cmdArgs);

      CmdExec objCmdExec = new CmdExec();

      boolean result = true;

      vecCmdOutPut = objCmdExec.getResultByCommand(cmdName, cmdArgs, CmdExec.SYSTEM_CMD, null, "root");

      if((vecCmdOutPut.size() > 0) && ((String)vecCmdOutPut.lastElement()).startsWith("ERROR"))
      {
        result = false;
      }

      if(!result)
      {
        Log.debugLog(className, "cvsDiffService", "", "", "nsi_cvs diff failed");
        outPut.add("Error");
        outPut.add(vecCmdOutPut);
        return outPut;
      }

      outPut.add("Success");
      outPut.add(vecCmdOutPut);
      return outPut;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "cvsDiffService", "", "", "Exception - ", e);
      outPut.add("Error");
      vecCmdOutPut = new Vector();
      vecCmdOutPut.add("Exception: " + e);
      outPut.add(vecCmdOutPut);
      return outPut;
    }
  }

  //Method to get the version history of any service
  public ArrayList cvsLogService(String hostName, String serviceName)
  {
    Log.debugLog(className, "cvsLogService", "", "", "Method started");
    ArrayList outPut = new ArrayList();
    Vector vecCmdOutPut = new Vector();
    String servicePath = getServicePath(hostName, serviceName);

    try
    {
      String cmdName = "nsi_cvs";
      String cmdArgs = "-o history -s " + servicePath;

      Log.debugLog(className, "cvsLogService", "", "", "cmdArgs = " + cmdArgs);

      CmdExec objCmdExec = new CmdExec();

      boolean result = true;

      vecCmdOutPut = objCmdExec.getResultByCommand(cmdName, cmdArgs, CmdExec.SYSTEM_CMD, null, "root");

      if((vecCmdOutPut.size() > 0) && ((String)vecCmdOutPut.lastElement()).startsWith("ERROR"))
      {
        result = false;
      }

      if(!result)
      {
        Log.debugLog(className, "cvsLogService", "", "", "nsi_cvs history failed");
        outPut.add("Error");
        outPut.add(vecCmdOutPut);
        return outPut;
      }

      outPut.add("Success");

      vecCmdOutPut.removeElementAt(0);

      String[][] serviceCVSHistory = new String[vecCmdOutPut.size()][4];

      for(int i = 0; i < vecCmdOutPut.size(); i++)
      {
        String[] strTemp = rptUtilsBean.strToArrayData(vecCmdOutPut.elementAt(i).toString(), "|");
        serviceCVSHistory[i] = strTemp;
      }

      outPut.add(serviceCVSHistory);
      return outPut;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "cvsLogService", "", "", "Exception - ", e);
      outPut.add("Error");
      vecCmdOutPut = new Vector();
      vecCmdOutPut.add("Exception: " + e);
      outPut.add(vecCmdOutPut);
      return outPut;
    }
  }

  // ********************* Methods for Test Service ******************************************************************

  /*
   * This fucntion is to get response from HPD by wget command
   */
  public boolean getResponseUsingWget(String serviceName, String hostName, String URL, String reqProtocol, String requestData, String additionalHeaders, String userName, StringBuffer outputMsg)
  {
    Log.debugLog(className, "getResponseUsingWget", "", "", "Method Start.");

    try
    {
      String cmdName = "wget";
      String cmdArgs = "";
      boolean boolValue = false;

      String tempDirPath = "";
      String osname = System.getProperty("os.name").trim().toLowerCase();
      if(osname.startsWith("win"))
        tempDirPath = System.getProperty("java.io.tmpdir");
      else
        tempDirPath = "/tmp";

      String tempReqFileNamePath = "";
      // create name of temp response file
      String tempRspFileNamePath = tempDirPath + "/rspFile_" + (new Random()).nextDouble();

      cmdArgs = "--tries=2 --save-headers -O " + tempRspFileNamePath + " --no-check-certificate";

      // create additional header array
      if(!additionalHeaders.equals(""))
      {
        Log.debugLog(className, "getResponseUsingWget", "", "", "additionalHeaders = " + additionalHeaders);
        String[] arrAdditionalHeaders = rptUtilsBean.strToArrayData(additionalHeaders, "\r\n");

        if(arrAdditionalHeaders != null)
        {
          Log.debugLog(className, "getResponseUsingWget", "", "", "Number of headers = " + arrAdditionalHeaders.length);
          for(int i = 0; i < arrAdditionalHeaders.length; i++)
            cmdArgs = cmdArgs + " --header=\"" + arrAdditionalHeaders[i] + "\"";
        }
      }

      // create temp request file
      if(!requestData.equals(""))
      {
        tempReqFileNamePath = createTempReqFile(hostName, serviceName, URL, requestData, tempDirPath);
        if(!tempReqFileNamePath.equals(""))
          cmdArgs = cmdArgs + " --post-file=" + tempReqFileNamePath;
      }

      // now create the complete URL

      String completeURL = reqProtocol + "://" + hostName + URL;

      cmdArgs = cmdArgs + " " + "\"" + completeURL + "\"";

      Log.debugLog(className, "getResponseUsingWget", "", "", "Cmd Args = " + cmdArgs);

      // Now Execute the command
      CmdExec objCmdExec = new CmdExec();
      Vector vecCmdOutPut = objCmdExec.getResultByCommand(cmdName, cmdArgs, CmdExec.SYSTEM_CMD, userName, userName);

      Log.debugLog(className, "getResponse", "", "", "Command output size = " + vecCmdOutPut.size());

      if((vecCmdOutPut.size() == 0) || (objCmdExec.getExitValue(vecCmdOutPut) == 0)) // command excuted successfully
      {
        File tempRspFileObj = new File(tempRspFileNamePath);

        if(!tempRspFileObj.exists())
        {
          Log.debugLog(className, "getResponseUsingWget", "", "", "File " + tempRspFileNamePath + " does not exist.");
          outputMsg.append("Response file " + tempRspFileNamePath + " does not exist.");
        }

        FileInputStream fis = new FileInputStream(tempRspFileNamePath);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));

        String strLine = "";
        String newLine = System.getProperty("line.separator");
        while((strLine = br.readLine()) != null)
        {
          outputMsg.append(strLine);
          outputMsg.append(newLine);
        }

        br.close();
        fis.close();

        boolValue = true;
      }
      else
      {
        Log.errorLog(className, "getResponseUsingWget", "", "", "Error in execution of command.");

        String newLine = System.getProperty("line.separator");
        for(int i = 0; i < vecCmdOutPut.size() - 1; i++)
        {
          outputMsg.append(vecCmdOutPut.get(i).toString());
          outputMsg.append(newLine);
        }

        if(outputMsg.toString().equals(""))
          outputMsg.append("Error in execution of command.");

        boolValue = false;
      }

      // now remove temp req and rsp files
      removeTempFiles(tempReqFileNamePath, tempRspFileNamePath, "");

      return boolValue;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getResponseUsingWget", "", "", "Exception", e);
      outputMsg.append("Exception occur while processing request. Please see error log for more detail(s).");
      return false;
    }
  }

  /*
   *  Now for test service use 'curl' command.
   *  --retry <count> (for retry count)
   *  -D <header file> (write response header in given file)
   *  -o <output file> (write response body in given response file)
   *  --insecure (for no check certificate)
   *  -d @<request file> (get request file)
   *  URL (URL for response )
   *  -H <headers> (additional headers)
   *  Command : curl --retry <count> -D <response header file> -o <response file> --insecure -d @<request file> URL -H <request headers>
   */
  public boolean getResponseUsingCurl(String serviceName, String hostName, String URL, String reqProtocol, String requestData, String additionalHeaders, String userName, StringBuffer outputMsg)
  {
    Log.debugLog(className, "getResponseUsingCurl", "", "", "Method Start.");

    try
    {
      String cmdName = "curl";
      String cmdArgs = "";
      boolean boolValue = false;

      String tempDirPath = "";
      String osname = System.getProperty("os.name").trim().toLowerCase();
      if(osname.startsWith("win"))
        tempDirPath = System.getProperty("java.io.tmpdir");
      else
        tempDirPath = "/tmp";

      String tempReqFileNamePath = "";
      // create name of temp response file
      String tempRspFileNamePath = tempDirPath + "/rspFile_" + (new Random()).nextDouble();
      String tempRspHeaderNamePath = tempDirPath + "/rspFile_Header_" + (new Random()).nextDouble();

      cmdArgs = "-D " + tempRspHeaderNamePath + " -o " + tempRspFileNamePath + " --insecure";

      // create additional header array
      if(!additionalHeaders.equals(""))
      {
        Log.debugLog(className, "getResponseUsingCurl", "", "", "additionalHeaders = " + additionalHeaders);
        String[] arrAdditionalHeaders = rptUtilsBean.strToArrayData(additionalHeaders, "\r\n");

        if(arrAdditionalHeaders != null)
        {
          Log.debugLog(className, "getResponseUsingCurl", "", "", "Number of headers = " + arrAdditionalHeaders.length);
          for(int i = 0; i < arrAdditionalHeaders.length; i++)
            cmdArgs = cmdArgs + " -H \"" + arrAdditionalHeaders[i] + "\"";
        }
      }

      // create temp request file
      if(!requestData.equals(""))
      {
        tempReqFileNamePath = createTempReqFile(hostName, serviceName, URL, requestData, tempDirPath);
        if(!tempReqFileNamePath.equals(""))
          cmdArgs = cmdArgs + " --data-binary @" + tempReqFileNamePath;
      }

      // now create the complete URL

      String completeURL = reqProtocol + "://" + hostName + URL;

      cmdArgs = cmdArgs + " " + "\"" + completeURL + "\"";

      cmdArgs = cmdArgs + " -H \"Expect:\"";

      Log.debugLog(className, "getResponseUsingCurl", "", "", "Cmd Args = " + cmdArgs);

      // Now Execute the command
      CmdExec objCmdExec = new CmdExec();
      Vector vecCmdOutPut = objCmdExec.getResultByCommand(cmdName, cmdArgs, CmdExec.SYSTEM_CMD, userName, userName);

      Log.debugLog(className, "getResponseUsingCurl", "", "", "Command output size = " + vecCmdOutPut.size());

      if((vecCmdOutPut.size() == 0) || (objCmdExec.getExitValue(vecCmdOutPut) == 0)) // command excuted successfully
      {
        File temFileResHeader = new File(tempRspHeaderNamePath);
        File tempRspFileObj = new File(tempRspFileNamePath);

        if(!tempRspFileObj.exists())
        {
          Log.debugLog(className, "getResponseUsingCurl", "", "", "File " + tempRspHeaderNamePath + " does not exist.");
          outputMsg.append("Response file " + tempRspFileNamePath + " does not exist.");
        }

        if(!tempRspFileObj.exists())
        {
          Log.debugLog(className, "getResponseUsingCurl", "", "", "File " + tempRspFileNamePath + " does not exist.");
          outputMsg.append("Response file " + tempRspFileNamePath + " does not exist.");
        }

        FileInputStream fis = new FileInputStream(temFileResHeader);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));

        String strLine = "";
        String newLine = System.getProperty("line.separator");
        while((strLine = br.readLine()) != null)
        {
          outputMsg.append(strLine);
          outputMsg.append(newLine);
        }

        br.close();
        fis.close();

        outputMsg.append(newLine);

        fis = new FileInputStream(tempRspFileNamePath);
        br = new BufferedReader(new InputStreamReader(fis));

        strLine = "";
        while((strLine = br.readLine()) != null)
        {
          outputMsg.append(strLine);
          outputMsg.append(newLine);
        }

        br.close();
        fis.close();

        boolValue = true;
      }
      else
      {
        Log.errorLog(className, "getResponseUsingCurl", "", "", "Error in execution of command.");

        String newLine = System.getProperty("line.separator");
        for(int i = 0; i < vecCmdOutPut.size() - 1; i++)
        {
          outputMsg.append(vecCmdOutPut.get(i).toString());
          outputMsg.append(newLine);
        }

        if(outputMsg.toString().equals(""))
          outputMsg.append("Error in execution of command.");

        boolValue = false;
      }

      // now remove temp req and rsp files
      removeTempFiles(tempReqFileNamePath, tempRspFileNamePath, tempRspHeaderNamePath);

      return boolValue;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getResponseUsingCurl", "", "", "Exception", e);
      outputMsg.append("Exception occur while processing request. Please see error log for more detail(s).");
      return false;
    }
  }

  // this function is to create the temp file by request data
  private String createTempReqFile(String hostName, String serviceName, String URL, String requestData, String tempDirPath)
  {
    Log.debugLog(className, "createTempReqFile", "", "", "Method Start. Temp dir path = " + tempDirPath);

    try
    {
      String tempReqFileNamePath = tempDirPath + "/" + "reqFile_" + (new Random()).nextDouble();

      Log.debugLog(className, "createTempReqFile", "", "", "Temp ReqFileNamePath = " + tempReqFileNamePath);

      File reqFileObj = new File(tempReqFileNamePath);

      FileOutputStream fout = new FileOutputStream(reqFileObj, true); // Append mode
      PrintStream printStream = new PrintStream(fout);
      if(isFileEncoded(hostName, serviceName, URL))
        printStream.print(encodeString(requestData).replaceAll("\\r", ""));
      else
        printStream.print(requestData.replaceAll("\\r", ""));

      // now close the streams
      printStream.close();
      fout.close();

      return tempReqFileNamePath;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "createTempReqFile", "", "", "Exception occur while creating temporary request file.", e);
      return "";
    }
  }

  // this is to remove temp req and rsp files
  private void removeTempFiles(String tempReqFileNamePath, String tempRspFileNamePath, String tempHeaderFileNamePath)
  {
    Log.debugLog(className, "removeTempFiles", "", "", "Method Start.");

    try
    {
      if(tempReqFileNamePath != null)
      {
        File reqFileObj = new File(tempReqFileNamePath);

        // if file already exist then delete it
        if(reqFileObj.exists())
          reqFileObj.delete();
      }

      if(tempRspFileNamePath != null)
      {
        File rspFileObj = new File(tempRspFileNamePath);

        // if file already exist then delete it
        if(rspFileObj.exists())
          rspFileObj.delete();
      }

      if(tempHeaderFileNamePath != null && !tempHeaderFileNamePath.equals(""))
      {
        File rspFileObj = new File(tempHeaderFileNamePath);

        // if file already exist then delete it
        if(rspFileObj.exists())
          rspFileObj.delete();
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "removeTempFiles", "", "", "Exception ", e);
    }
  }

  /**  scriptName consist of project and subproject
   *
   *   @param type (either request or response), scriptName
   *   @returns the path of request and response directory
   **/
  public String getScript_RequestResponsePath(String type, String scriptName)
  {
    String workPath = Config.getWorkPath();
    String osname = System.getProperty("os.name").trim().toLowerCase();
    if(osname.startsWith("win"))
      workPath = "C:/home/netstorm/work/webapps";

    String path = "";
    if(type.equalsIgnoreCase("request"))
      path = workPath + "/scripts/" + scriptName + "/request/";
    else if(type.equalsIgnoreCase("response"))
      path = workPath + "/scripts/" + scriptName + "/response/";

    return path;
  }

  public ReqResFileContent[][] getListOfReqAndResponse(String scriptName)
  {
    return getListOfReqAndResponse(scriptName, "NUMERIC");
  }

  /**
   * Script Name will come project and subprojetc
   *
   * @param scriptName
   * @return
   */
  public ReqResFileContent[][] getListOfReqAndResponse(String scriptName, String sortingType)
  {
    Log.debugLog(className, "getListOfReqAndResponse", "", "", "Method started scriptName = " + scriptName);
    try
    {
      /**String workPath = Config.getWorkPath();
      String osname = System.getProperty("os.name").trim().toLowerCase();
      if(osname.startsWith("win"))
        workPath = "C:/home/netstorm/work/webapps";

      String requestPath = workPath + "/scripts/" + scriptName + "/request/";
      String responsePath = workPath + "/scripts/" + scriptName + "/response/";**/

      String requestPath = getScript_RequestResponsePath("request", scriptName);
      String responsePath = getScript_RequestResponsePath("response", scriptName);

      File fileRequest = new File(requestPath);
      if(!fileRequest.exists())
      {
        Log.errorLog(className, "getListOfReqAndResponse", "", "", "Request directory not found. - " + requestPath);
        return null;
      }

      File fileResponse = new File(responsePath);
      if(!fileResponse.exists())
      {
        Log.errorLog(className, "getListOfReqAndResponse", "", "", "Response directory not found. - " + responsePath);
        return null;
      }
      /**
       * I assume here that request and respnose file only contain request and
       * response files only, nothing else.
       */

      String[] arrFileList = fileRequest.list();
      String[][] arrFile = getSortedListOfReqRespFile(arrFileList);

      Log.debugLog(className, "getListOfReqAndResponse", "", "", "Number of files in Request Directory = " + arrFile.length);

      int reqFileNumOnly = getUniqueRequestFileNum(arrFileList);
      Log.debugLog(className, "getListOfReqAndResponse", "", "", "Number of Only Request files in Request Dir = " + reqFileNumOnly);

      /**
      *   data[][0] = content of Request File.
      *   data[][1] = content of Request Body File.
      *   data[][2] = content of Response File.
      *   data[][3] = content of Response Body File.
      *   data[][4] = content of Service File.
      **/

      ReqResFileContent data[][] = new ReqResFileContent[reqFileNumOnly][5];
      fileNames = new String[reqFileNumOnly];

      String contentTypeExtension[][] = getAllResponseExtensionTypes();
      if(contentTypeExtension == null)
        Log.errorLog(className, "getListOfReqAndResponse", "", "", "content-type to extension array is coming null");
      else if(contentTypeExtension.length <= 0)
        Log.errorLog(className, "getListOfReqAndResponse", "", "", "content-type to extension array is coming empty");

      int j = 0;
      for(int i = 0; i < arrFile.length; i++)
      {
        if(arrFile[i][0].startsWith("request") && ((!arrFile[i][0].startsWith("request_body")) && (!arrFile[i][0].startsWith("request_body_hessian")) && (!arrFile[i][0].startsWith("request_body_amf")) && (!arrFile[i][0].startsWith("request_body_gzip")) && (!arrFile[i][0].startsWith("request_body_deflate"))))
        {
          boolean isFileExist = true;

          ReqResFileContent reqObj = null;
          ReqResFileContent reqBodyObj = null;

          ReqResFileContent resObj = null;
          ReqResFileContent resBodyObj = null;

          //service file object
          ReqResFileContent serviceObj = null;

          String reqFileName = arrFile[i][0];
          String reqBodyFileName = arrFile[i][0].replace("request", "request_body");
          String resFileName = arrFile[i][0].replace("request", "response");
          String resBodyFileName = arrFile[i][0].replace("request", "response_body");
          String serFileName = arrFile[i][0].replace("request", "service");

          String requestFilePath = requestPath + reqFileName;
          Log.debugLog(className, "getListOfReqAndResponse", "", "", "request file path = " + requestFilePath);

          String requestBodyFilePath = requestPath + reqBodyFileName;
          Log.debugLog(className, "getListOfReqAndResponse", "", "", "request body file path = " + requestBodyFilePath);

          String responseFilePath = responsePath + resFileName;
          Log.debugLog(className, "getListOfReqAndResponse", "", "", "response file path = " + responseFilePath);

          String responseBodyFilePath = responsePath + resBodyFileName;
          Log.debugLog(className, "getListOfReqAndResponse", "", "", "response body file path = " + responseBodyFilePath);

          String serviceFilePath = responsePath + serFileName;
          Log.debugLog(className, "getListOfReqAndResponse", "", "", "service file path = " + serviceFilePath);

          File tempFileRequest = new File(requestFilePath);
          File tempFileRequestBody = new File(requestBodyFilePath);

          File tempFileResponse = new File(responseFilePath);
          File tempFileResponseBody = new File(responseBodyFilePath);

          File tempFileService = new File(serviceFilePath);

          if(!tempFileRequest.exists())
            isFileExist = false;

          reqObj = new ReqResFileContent(scriptName, requestFilePath, ReqResFileContent.REQUEST_FILE, reqFileName, isFileExist);
          if(isFileExist)
          {
            if(!reqObj.getRequestResponseFileContentsForScript(contentTypeExtension))
            {
              Log.errorLog(className, "getListOfReqAndResponse", "", "", "Error in setting body and URL for request file at " + requestFilePath);
              return null;
            }
          }
          isFileExist = true;

          if(!tempFileRequestBody.exists())
            isFileExist = false;

          reqBodyObj = new ReqResFileContent(scriptName, requestBodyFilePath, ReqResFileContent.REQUEST_BODY_FILE, reqBodyFileName, isFileExist);
          if(isFileExist)
          {
            if(!reqBodyObj.getRequestResponseFileContentsForScript(contentTypeExtension))
            {
              Log.errorLog(className, "getListOfReqAndResponse", "", "", "Error in setting body and URL for request body file at " + requestBodyFilePath);
              return null;
            }
          }
          isFileExist = true;

          if(!tempFileResponse.exists())
            isFileExist = false;

          resObj = new ReqResFileContent(scriptName, responseFilePath, ReqResFileContent.RESPONSE_FILE, resFileName, isFileExist);
          if(isFileExist)
          {
            if(!resObj.getRequestResponseFileContentsForScript(contentTypeExtension))
            {
              Log.errorLog(className, "getListOfReqAndResponse", "", "", "Error in setting body and URL for response file at " + responseFilePath);
              return null;
            }
          }
          isFileExist = true;

          if(!tempFileResponse.exists())
            isFileExist = false;

          resBodyObj = new ReqResFileContent(scriptName, responseBodyFilePath, ReqResFileContent.RESPONSE_BODY_FILE, resBodyFileName, isFileExist);
          if(isFileExist)
          {
            if(!resBodyObj.getRequestResponseFileContentsForScript(contentTypeExtension))
            {
              Log.errorLog(className, "getListOfReqAndResponse", "", "", "Error in setting body for response body file at " + responseBodyFilePath);
              return null;
            }
          }

          //service file data 
          isFileExist = true;

          if(!tempFileService.exists())
            isFileExist = false;

          serviceObj = new ReqResFileContent(scriptName, serviceFilePath, ReqResFileContent.SERVICE_FILE, serFileName, isFileExist);
          if(isFileExist)
          {
            if(!serviceObj.getRequestResponseFileContentsForScript(contentTypeExtension))
            {
              Log.errorLog(className, "getListOfReqAndResponse", "", "", "Error in setting service for response file at " + serviceFilePath);
              return null;
            }
          }

          data[j][0] = reqObj;
          data[j][1] = reqBodyObj;
          data[j][2] = resObj;
          data[j][3] = resBodyObj;
          data[j][4] = serviceObj;
          setFileName(j, arrFile[i][0]);
          j++;
        }
      }
      if(sortingType.equals("REQUEST-TIME"))
        return getSortedDataAccordingToTimeStamp(data);
      else
        return data;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getListOfReqAndResponse", "", "", "Exception ", e);
      return null;
    }
  }

  public void setFileName(int index, String tempFileName)
  {
    fileNames[index] = tempFileName;
  }

  private int getUniqueRequestFileNum(String[] arrTemp)
  {
    Log.debugLog(className, "getUniqueRequestFileNum", "", "", "Method Started.");

    int count = 0;
    for(int i = 0; i < arrTemp.length; i++)
    {
      if(arrTemp[i].startsWith("request") && ((!arrTemp[i].startsWith("request_body")) && (!arrTemp[i].startsWith("request_body_hessian")) && (!arrTemp[i].startsWith("request_body_amf")) && (!arrTemp[i].startsWith("request_body_gzip")) && (!arrTemp[i].startsWith("request_body_deflate"))))
      {
        count++;
      }
    }

    Log.debugLog(className, "getUniqueRequestFileNum", "", "", "Method Started. Number of Request Files only is: " + count);
    return count;
  }

  /*
   * this method is used to sort the request and response file on the basis of request time stamp 
   */
  public ReqResFileContent[][] getSortedDataAccordingToTimeStamp(ReqResFileContent[][] dataFileList)
  {
    Log.debugLog(className, "getSortedDataAccordingToTimeStamp", "", "", "Method started.");

    String arrList[][] = null;
    ReqResFileContent[][] dataFileListTemp = null;
    try
    {
      arrList = new String[dataFileList.length][2];

      for(int i = 0; i < dataFileList.length; i++)
      {
        String strTemp = "";
        if(dataFileList[i][4].fileName.startsWith("service"))
        {
          strTemp = String.valueOf(dataFileList[i][4].requestTimeStamp);
          arrList[i][0] = dataFileList[i][4].fileName;
          arrList[i][1] = strTemp;
        }
      }
      //sorting on the basis of request time
      for(int j = 0; j < arrList.length; j++)
        Log.debugLog(className, "getSortedDataAccordingToTimeStamp", "", "", "name = " + arrList[j][0] + "time = " + arrList[j][1]);

      String arrListNew[][] = sortArray(arrList, 1, 0, "LONG");

      for(int k = 0; k < arrListNew.length; k++)
        Log.debugLog(className, "getSortedDataAccordingToTimeStamp", "", "", "sorted name = " + arrList[k][0] + ", and time = " + arrList[k][1]);

      dataFileListTemp = new ReqResFileContent[arrList.length][5];

      //arrange rquest and response on time stamp
      for(int i = 0; i < arrListNew.length; i++)
        dataFileListTemp[i] = getRequestAndResponceContentFromRequestAndResponseArray(arrListNew[i][0], dataFileList);

      return dataFileListTemp;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getSortedListOfReqRespFile", "", "", "Exception ", e);
      return dataFileListTemp;
    }
  }

  /*
   * Method to return request and response content object from requset and response array 
   * @param fileName - service file name
   * @param dataFileList - request and response data array
   * return - requestAndResponse Content Object
   */
  private ReqResFileContent[] getRequestAndResponceContentFromRequestAndResponseArray(String fileName, ReqResFileContent[][] dataFileList)
  {
    Log.debugLog(className, "getRequestAndResponceContentFromRequestAndResponseArray", "", "", "method start");
    try
    {
      for(int i = 0; i < dataFileList.length; i++)
      {
        if(fileName.equals(dataFileList[i][4].fileName))
        {
          Log.debugLog(className, "getRequestAndResponceContentFromRequestAndResponseArray", "", "", "file name found = " + fileName);
          return dataFileList[i];
        }
      }
    }
    catch(NullPointerException npe)
    {
      Log.stackTraceLog(className, "getRequestAndResponceContentFromRequestAndResponseArray", "", "", "Exception ", npe);
      return null;
    }
    return null;
  }

  public String getURLFromRequestFile(String reqFileName)
  {
    Log.debugLog(className, "getURLFromRequestFile", "", "", "Method started reuqest File Name = " + reqFileName);
    try
    {
      String fileWithPath = getHPDPath() + "logs/" + reqFileName;
      String urlOfReq = "";

      File dataFile = new File(fileWithPath);

      if(!dataFile.exists())
      {
        Log.errorLog(className, "getURLFromRequestFile", "", "", "File not found, filename - " + fileWithPath);
        return "";
      }
      File fileObj = new File(fileWithPath);
      byte data[] = new byte[(1024 * 4)];
      FileInputStream fis = new FileInputStream(fileWithPath);
      BufferedInputStream bis = new BufferedInputStream(fis);
      StringBuffer sbData = new StringBuffer();

      String strLine = null;
      int dataReadLength = -1;
      while((dataReadLength = bis.read(data)) > 0)
      {
        strLine = new String(data, 0, dataReadLength);
        sbData.append(strLine);
      }

      if(sbData.length() <= 0)
      {
        Log.errorLog(className, "getURLFromRequestFile", "", "", "Empty Buffer no data found for file = " + fileWithPath);
        return "";
      }

      String str = sbData.substring(0, sbData.indexOf("\r\n"));
      Log.debugLog(className, "getURLFromRequestFile", "", "", "First line = " + str + ", for request file = " + fileWithPath);
      urlOfReq = str.split(" ")[1];

      bis.close();
      fis.close();
      return urlOfReq;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getListOfReqAndResponse", "", "", "Exception ", e);
      return "";
    }
  }

  /*
   * this method is used to read the trace file.
   * It will return Vector.
   */
  public Vector getLogFromRequestTraceFile(String reqFileName)
  {
    Log.debugLog(className, "getLogFromRequestTraceFile", "", "", "Method started reuqest File Name = " + reqFileName);
    Vector vecFileData = null;
    try
    {
      String fileWithPath = getHPDPath() + "logs/" + reqFileName;
      String urlOfReq = "";
      File dataFile = new File(fileWithPath);

      if(!dataFile.exists())
      {
        Log.errorLog(className, "getLogFromRequestTraceFile", "", "", "File not found, filename - " + fileWithPath);
        return vecFileData;
      }

      vecFileData = rptUtilsBean.readFileInVector(fileWithPath);
      return vecFileData;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getLogFromRequestTraceFile", "", "", "Exception ", e);
      return vecFileData;
    }
  }

  //Method to decode contents by URL decode.
  //it will return same value what we are getting in case of exception
  public static String decodeString(String contentsToDecode)
  {
    Log.debugLog("CorrelationService", "decodeString", "", "", "Method Called");
    try
    {
      String decodedString = URLDecoder.decode(contentsToDecode);
      return decodedString;
    }
    catch(Exception ex)
    {
      Log.errorLog("CorrelationService", "decodeString", "", "", "Exception is coming in decoding so returing same string as input - " + ex);
      return contentsToDecode;
    }
  }

  //Method to encode contents by URL encode.
  //it will return same value what we are getting in case of exception
  public static String encodeString(String contentsToEncode)
  {
    Log.debugLog("CorrelationService", "encodeString", "", "", "Method Called");
    try
    {
      String encodedString = URLEncoder.encode(contentsToEncode);
      return encodedString;
    }
    catch(Exception ex)
    {
      Log.errorLog("CorrelationService", "encodeString", "", "", "Exception is coming in encoding so returing same string as input - " + ex);
      return contentsToEncode;
    }
  }

  // this function is to save the Conditional Logic block in control file
  public boolean saveCondLogicBlockInCtrlFileByService(String hostName, String serviceName, String URL, String strCondData)
  {
    Log.debugLog(className, "saveCondLogicBlockInCtrlFileByService", "", "", "Method started service = " + serviceName);

    try
    {
      String cntrlFileNameWithPath = getServiceConfFilePath(hostName, serviceName);

      File cntrlFileObj = new File(cntrlFileNameWithPath);

      Vector hpdCntrlData = null;

      // if file not exist then create it
      if(!cntrlFileObj.exists())
      {
        Log.debugLog(className, "saveCondLogicBlockInCtrlFileByService", "", "", "File " + cntrlFileNameWithPath + " does not exist, going to create new file.");
        cntrlFileObj.createNewFile();
      }
      else
      {
        hpdCntrlData = readFile(cntrlFileNameWithPath, false);
        if(hpdCntrlData == null)
        {
          Log.debugLog(className, "saveCondLogicBlockInCtrlFileByService", "", "", "File " + cntrlFileNameWithPath + " not found, It may be corrupted.");
          // if file not found than create new file
          cntrlFileObj.createNewFile();
        }
        else
        {
          Log.debugLog(className, "saveCondLogicBlockInCtrlFileByService", "", "", "Deleting file " + cntrlFileNameWithPath);
          // if file not found than create new file
          cntrlFileObj.delete();
        }
      }

      // Now create file o/p stream & printstream
      FileOutputStream fout = new FileOutputStream(cntrlFileObj, true); // Append mode
      PrintStream printStream = new PrintStream(fout);

      if(hpdCntrlData != null)
      {
        boolean flagWriteFile = true;
        String dataLine = "";
        for(int i = 0; i < hpdCntrlData.size(); i++)
        {
          dataLine = hpdCntrlData.get(i).toString();

          //Log.debugLog(className, "saveCondLogicBlockInCtrlFileByURL", "", "", "dataline = " + dataLine);

          if(dataLine.startsWith("#BEGIN_CONDITIONAL_LOGIC_BLOCK"))
          {
            Log.debugLog(className, "saveCondLogicBlockInCtrlFileByURL", "", "", "BEGIN_CONDITIONAL_LOGIC_BLOCK found in the file " + cntrlFileNameWithPath + ".");
            flagWriteFile = false;
          }
          if(dataLine.startsWith("#END_CONDITIONAL_LOGIC_BLOCK"))
          {
            Log.debugLog(className, "saveCondLogicBlockInCtrlFileByURL", "", "", "END_CONDITIONAL_LOGIC_BLOCK found in the file " + cntrlFileNameWithPath + ".");
            flagWriteFile = true;
            continue; // this is to avoid END_CONDITIONAL_LOGIC_BLOCK line
          }

          if(flagWriteFile)
          {
            Log.debugLog(className, "saveCondLogicBlockInCtrlFileByURL", "", "", "Writing dataline in the file = " + dataLine);
            printStream.println(dataLine);
          }
        }
      }
      else
        printStream.println(); // it needed only first time

      // this is to remove ^M at the end of line
      strCondData = strCondData.replaceAll("\\r", "");

      // Now write the conditional logic block in file
      if(!strCondData.trim().equals(""))
      {
        printStream.println("#BEGIN_CONDITIONAL_LOGIC_BLOCK");
        printStream.println(strCondData);
        printStream.print("#END_CONDITIONAL_LOGIC_BLOCK");
      }

      // now close the streams
      printStream.close();
      fout.close();

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "saveCondLogicBlockInCtrlFileByURL", "", "", "Exception ", e);
      return false;
    }
  }

  // this function is to get Conditional Logic block data from control file
  public StringBuffer getCondLogicBlockFromCtrlFileByService(String hostName, String serviceName, String URL)
  {
    Log.debugLog(className, "getCondLogicBlockFromCtrlFileByService", "", "", "Method started service = " + serviceName);

    try
    {
      String cntrlFileNameWithPath = getServiceConfFilePath(hostName, serviceName);

      File cntrlFileObj = new File(cntrlFileNameWithPath);

      Vector hpdCntrlData = readFile(cntrlFileNameWithPath, false);

      StringBuffer strBufCntrlLogicBlkData = new StringBuffer();

      if(hpdCntrlData == null)
      {
        Log.debugLog(className, "getCondLogicBlockFromCtrlFileByService", "", "", "File " + cntrlFileNameWithPath + " not found, It may be corrupted.");
        return null;
      }

      String dataLine = "";
      boolean flagCondData = false;
      for(int i = 0; i < hpdCntrlData.size(); i++)
      {
        dataLine = hpdCntrlData.get(i).toString();

        if(dataLine.startsWith("#BEGIN_CONDITIONAL_LOGIC_BLOCK"))
        {
          Log.debugLog(className, "getCondLogicBlockFromCtrlFileByService", "", "", "BEGIN_CONDITIONAL_LOGIC_BLOCK found in the file " + cntrlFileNameWithPath + ".");
          flagCondData = true;
          continue; // Avoid BEGIN_CONDITIONAL_LOGIC_BLOCK data line
        }
        if(dataLine.startsWith("#END_CONDITIONAL_LOGIC_BLOCK"))
        {
          Log.debugLog(className, "getCondLogicBlockFromCtrlFileByService", "", "", "END_CONDITIONAL_LOGIC_BLOCK found in the file " + cntrlFileNameWithPath + ".");

          flagCondData = false;
        }

        if(flagCondData)
        {
          Log.debugLog(className, "getCondLogicBlockFromCtrlFileByService", "", "", "Adding dataline in the list = " + dataLine);
          strBufCntrlLogicBlkData.append(dataLine);
          strBufCntrlLogicBlkData.append("\n");
        }
      }

      return strBufCntrlLogicBlkData;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getCondLogicBlockFromCtrlFileByService", "", "", "Exception ", e);
      return null;
    }
  }

  // this function is to generate the random script name
  public static String genRandomScriptName()
  {
    Log.debugLog(className, "genRandomScriptName", "", "", "Method Called ");

    try
    {
      SimpleDateFormat simpleDateFormatObj = new SimpleDateFormat("MMddyyHHmmssSSS");
      String scriptName = "capture" + simpleDateFormatObj.format(new Date());

      Log.debugLog(className, "genRandomScriptName", "", "", "Random Script Name = " + scriptName);

      return scriptName;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "genRandomScriptName", "", "", "Exception ", e);
      return "";
    }
  }

  // This function is to format the XML data
  public String formatXML(String xmlContent)
  {
    Log.debugLog(className, "formatXML", "", "", "Method Called ");

    String result = "";
    try
    {
      for(int indentLevel = 0, index = 0; index < xmlContent.length(); index++)
      {
        //Seek to next "<"
        index = xmlContent.indexOf("<", index);

        if(index < 0 || index >= xmlContent.length())
          break;

        //Trim out XML block
        String section = xmlContent.substring(index, xmlContent.indexOf(">", index) + 1);

        if(section.matches("<!--.*-->"))
        {
          //Is comment <!--....-->
          result = indent(result, indentLevel);
        }
        else if(section.matches("<!.*>"))
        {
          //Directive
          result = indent(result, indentLevel);
        }
        else if(section.matches("<\\?.*\\?>"))
        {
          //Is directive <?...?>
          result = indent(result, indentLevel);
        }
        else if(section.matches("<[\\s]*[/\\\\].*>"))
        {
          //Is closing tag </...>
          result = indent(result, --indentLevel);
        }
        else if(section.matches("<.*[/\\\\][\\s]*>"))
        {
          //Is standalone tag <.../>
          result = indent(result, indentLevel);
        }
        else
        {
          //Is begin tag <....>
          result = indent(result, indentLevel++);
        }

        result += section + "\n";
      }

      return result;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "genRandomScriptName", "", "", "Exception ", e);
      return xmlContent;
    }
  }

  // to indent the XML data
  private String indent(String text, int indentLevel)
  {
    //Log.debugLog(className, "indent", "", "", "Method Called ");

    for(int count = indentLevel; count > 0; count--)
      text += indent;
    return text;
  }

  //This method is to check the status of contoller. Controller is the work or hpd earlier we used to call hpd, hpd2, hpd3...
  //We need to return this message
  //Controller <controller name> is running on IP <IP> and port(s) <port1,port2>, activated on <date time>
  //We need to run hpd shell with start_time arugument. This shell will read the file /var/<controller name>/hpd/.tmp/.hpd.pid.
  //This shell will return "HPD is not running." if controller is not running. In this case method will return the same message
  //otherwise it will return date time like this "2012-02-09 15:24:58"
  //Now we need to get the IP on which controller is running by value of HPD_SERVER_ADDRESS
  //and get the value of keyword HPD_PORT
  //After that we need to make complete message for GUI.
  public String controllerStatus()
  {
    Log.debugLog(className, "controllerStatus", "", "", "Method Start.");
    try
    {
      String checkHPD = checkHPD();
      if(!checkHPD.contains("Activated Successfully"))
        return "Controller is not running";
      String cmdName = "/etc/init.d/" + getHPDWork();
      Log.debugLog(className, "restartHPD", "", "", "cmdName = " + cmdName);
      String cmdArgs = "start_time";
      CmdExec objCmdExec = new CmdExec();
      Vector vecCmdOutPut = objCmdExec.getResultByCommand(cmdName, cmdArgs, CmdExec.SYSTEM_CMD, null, "root");
      String activatedOn = "";
      if(vecCmdOutPut != null && vecCmdOutPut.size() > 0)
      {
        activatedOn = vecCmdOutPut.get(0).toString();
        if(activatedOn.toLowerCase().contains("not running"))
        {
          return activatedOn;
        }
      }
      else
      {
        return "";
      }

      String IP = getKeywordValueFromActiavtedHPD("HPD_SERVER_ADDRESS");

      if(IP.trim().equals(""))
        IP = "ALL";

      String ports = getKeywordValueFromActiavtedHPD("HPD_PORT");
      Log.debugLog(className, "controllerStatus", "", "", "activatedOn = " + activatedOn);
      DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      Date date = (Date)formatter.parse(activatedOn);
      Log.debugLog(className, "controllerStatus", "", "", "date = " + date);
      DateFormat formatter2 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
      String date1 = formatter2.format(date);
      Log.debugLog(className, "controllerStatus", "", "", "date = " + date1);
      String workName = getHPDWork();
      if(workName.startsWith("hpd_"))
        workName = workName.substring(4);

      String finalMessage = "Controller " + workName + " is running on IP " + IP + " and port(s) " + ports + " activated on " + date1;
      return finalMessage;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "controllerStatus", "", "", "Exception", ex);
      return "";
    }
  }

  public String getKeywordValueFromActiavtedHPD(String keyword)
  {
    Log.debugLog(className, "getKeywordValueFromActiavtedHPD", "", "", "Method Start for keyword = " + keyword);
    try
    {
      Vector hpdConfData = readFile(getHPDPath() + ".hpd_sp/hpd.conf", true);
      if(hpdConfData == null)
      {
        Log.debugLog(className, "getKeywordValueFromActiavtedHPD", "", "", "hpd.conf File not found, It may be corrupted.");
        return "";
      }

      for(int i = 0; i < hpdConfData.size(); i++)
      {
        if(hpdConfData.elementAt(i).toString().indexOf(keyword) > -1)
        {
          String[] arrLine = rptUtilsBean.strToArrayData(hpdConfData.elementAt(i).toString(), " ");
          if(arrLine == null || arrLine.length <= 1)
            return "";

          return arrLine[1];
        }
      }
      return "";
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getKeywordValueFromActiavtedHPD", "", "", "Exception", ex);
      return "";
    }
  }

  public String getKeywordValue(String keyword)
  {
    Log.debugLog(className, "getKeywordValue", "", "", "Method Start for keyword = " + keyword);
    try
    {
      Vector hpdConfData = readFile(getHPDPath() + "conf/hpd.conf", true);
      if(hpdConfData == null)
      {
        Log.debugLog(className, "getKeywordValue", "", "", "hpd.conf File not found, It may be corrupted.");
        return "";
      }

      for(int i = 0; i < hpdConfData.size(); i++)
      {
        if(hpdConfData.elementAt(i).toString().indexOf(keyword) > -1)
        {
          String[] arrLine = rptUtilsBean.strToArrayData(hpdConfData.elementAt(i).toString(), " ");
          if(arrLine == null || arrLine.length <= 1)
            return "";

          return arrLine[1];
        }
      }
      return "";
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getKeywordValue", "", "", "Exception", ex);
      return "";
    }
  }

  //Method to get number of deleted services in recyclebin of services
  public int getNumberOfItemsInRecycleBin(String hostName)
  {
    Log.debugLog(className, "getNumberOfItemsInRecycle", "", "", "Method started");
    int count = 0;
    try
    {
      File corrDir = new File(getCorrelationPath() + hostName + "/.deletedservices/");
      File[] files = corrDir.listFiles();
      if(files == null || files.length <= 0)
        return count;
      for(int i = 0; i < files.length; i++)
      {
        if(files[i].isDirectory())
          count++;
      }
      return count;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getNumberOfItemsInRecycle", "", "", "Exception - ", ex);
      return count;
    }
  }

  //Method to restore services from recyclebin to its original location
  public String restoreServices(String hostName, String[] servicesToRestore)
  {
    Log.debugLog(className, "restoreServices", "", "", "Method started");
    String result = "";
    try
    {
      for(int i = 0; i < servicesToRestore.length; i++)
      {
        String cmdName = "nou_recycle";
        String cmdArgs = "-o restore -s " + getCorrelationPath() + hostName + "/.deletedservices/" + servicesToRestore[i] + "/";
        CmdExec objCmdExec = new CmdExec();
        Vector vecCmdOutPut = objCmdExec.getResultByCommand(cmdName, cmdArgs, CmdExec.SYSTEM_CMD, null, "root");
        if(vecCmdOutPut != null && vecCmdOutPut.size() > 0 && vecCmdOutPut.get(0).toString().contains("already exists"))
        {
          String output = vecCmdOutPut.get(0).toString();
          int startIndex = output.indexOf("'") + 1;
          int lastIndex = output.lastIndexOf("'") - 1;
          if((startIndex != -1) && (lastIndex != -1))
          {
            if(result.equals(""))
              result = output.substring(startIndex, lastIndex);
            else
              result = result + ", " + output.substring(startIndex, lastIndex);
          }
          //servicesAlreadyExist.add(output.substring(13, output.length()-29));
        }

        String[][] arrDataValuesURL = getNOServices();
        String URL = "";
        String ServiceName = "";
        for(int j = 0; j < arrDataValuesURL.length; j++)
        {
          if(servicesToRestore[i].equals(arrDataValuesURL[j][1]))
          {
            URL = arrDataValuesURL[j][2];
            ServiceName = arrDataValuesURL[j][1];
          }
        }
        boolean enableUrlFlag = true;

        for(int j = 0; j < arrDataValuesURL.length; j++)
        {
          if(URL.trim().equals((arrDataValuesURL[j][2]).trim()) && !ServiceName.equals(arrDataValuesURL[j][1]))
          {
            if((arrDataValuesURL[j][14]).trim().equals("enabled"))
            {
              enableUrlFlag = false;
              break;
            }
          }
        }
        if(!enableUrlFlag)
          enableServiceURL("default", servicesToRestore[i], URL, "disabled");

        //It was needed to migrate template file of service when we did changes in format.
        //Now we are disabling it assuming all templates are migrated.
        //MigrateTemplateConditionBasedOnService("default", servicesToRestore[i]);
      }
      return result;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "restoreServices", "", "", "Exception - ", ex);
      return "Error";
    }
  }

  /*
   * this method is used to sort the request and response file 
   */
  public String[][] getSortedListOfReqRespFile(String[] arrFileList)
  {
    return getSortedListOfReqRespFile(arrFileList, "NEUMERIC");
  }

  /*
   * this method is used to sort the request and response file on the basis of sorting type 
   */
  public String[][] getSortedListOfReqRespFile(String[] arrFileList, String sortingType)
  {
    Log.debugLog(className, "getSortedListOfReqRespFile", "", "", "Method started.");

    String arrList[][] = null;

    try
    {
      arrList = new String[arrFileList.length][2];
      for(int i = 0; i < arrFileList.length; i++)
      {
        String strTemp = "";
        if(arrFileList[i].startsWith("request_body"))
        {
          if(arrFileList[i].startsWith("request_body_hessian") || arrFileList[i].startsWith("request_body_gzip") || arrFileList[i].startsWith("request_body_amf") || arrFileList[i].startsWith("request_body_deflate"))
          {
            String tempStr = arrFileList[i].substring(arrFileList[i].indexOf("_") + 1);
            tempStr = tempStr.substring(tempStr.indexOf("_") + 1);
            tempStr = (tempStr.substring(tempStr.indexOf("_") + 1));
            strTemp = rptUtilsBean.replace(validateReqResFileSequence(tempStr), "_", "");
          }
          else
          {
            String tempStr = arrFileList[i].substring(arrFileList[i].indexOf("_") + 1);
            tempStr = tempStr.substring(tempStr.indexOf("_") + 1);
            strTemp = rptUtilsBean.replace(validateReqResFileSequence(tempStr), "_", "");
          }
        }
        else
        {
          String tempStr = (arrFileList[i].substring(arrFileList[i].indexOf("_") + 1));
          strTemp = rptUtilsBean.replace(validateReqResFileSequence(tempStr), "_", "");
        }
        arrList[i][0] = arrFileList[i];
        arrList[i][1] = strTemp;
      }
      arrList = sortArray(arrList, 1, 0, sortingType);
      return arrList;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getSortedListOfReqRespFile", "", "", "Exception ", e);
      return arrList;
    }
  }

  /*
   * This method checked the sequence of files, if the last digit of sequence is not three digits then add zero make last digit
   * of three numbers.
   */
  public String validateReqResFileSequence(String tempStr)
  {
    Log.debugLog(className, "validateReqResFileSequence", "", "", "Method started.");

    String BeginStr = tempStr.substring(0, tempStr.lastIndexOf("_") + 1);
    String EndStr = tempStr.substring(tempStr.lastIndexOf("_") + 1);

    if(EndStr.length() == 2)
      EndStr = "0" + EndStr;
    else if(EndStr.length() == 1)
      EndStr = "00" + EndStr;

    tempStr = BeginStr + EndStr;
    Log.debugLog(className, "validateReqResFileSequence", "", "", tempStr + " = last sequence returned");

    return tempStr;
  }

  public String getXMLPath(String fileName, String fetchStringPath, StringBuffer errorMassege)
  {
    Log.debugLog(className, "getXMLPath", "", "", "Method started");
    try
    {
      File file = new File(fileName);

      if(file.exists())
      {
        // Create dom factory
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Use the factory to create a document builder
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file);
        fetchStringPath = fetchStringPath.trim();
        if(fetchStringPath.startsWith("<") && fetchStringPath.endsWith(">"))
          fetchStringPath = fetchStringPath.substring(fetchStringPath.indexOf("<") + 1, fetchStringPath.indexOf(">"));
        // Get a list of all elements in the document
        NodeList list = doc.getElementsByTagName(fetchStringPath);

        if(list == null || list.getLength() <= 0)
        {
          errorMassege.append("Invalid Node \"" + fetchStringPath + "\".");
          Log.debugLog(className, "getXMLPath", "", "", "Invalid Node \"" + fetchStringPath + "\".");
          return "";
        }
        // Get element
        Element element = (Element)list.item(0);
        String parentNameG = element.getParentNode().toString();

        String nodePath = "";
        if(element.getNodeName().indexOf(":") > -1)
        {
          nodePath = "<" + element.getNodeName().substring(element.getNodeName().indexOf(":") + 1) + ">";
        }
        else
          nodePath = "<" + element.getNodeName() + ">";

        while(!parentNameG.startsWith("[#document"))
        {
          String[] parentName = parentNameG.split(" ");
          parentName[0] = parentName[0].substring(0, parentName[0].lastIndexOf(":"));
          while(parentName[0].indexOf(":") > -1)
          {
            parentName[0] = parentName[0].substring(parentName[0].indexOf(":") + 1);
          }
          if(parentName[0].startsWith("["))
            parentName[0] = parentName[0].substring(1);

          nodePath = "<" + parentName[0] + ">" + nodePath;
          element = (Element)element.getParentNode();
          parentNameG = element.getParentNode().toString();
        }

        Log.debugLog(className, "getXMLPath", "", "", "nodePath = " + nodePath);
        return nodePath;
      }
      else
      {
        errorMassege.append("File not found!");
        Log.debugLog(className, "getXMLPath", "", "", "File not found!");
        return "";
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getXMLPath", "", "", "Exception - ", e);
      errorMassege.append("Invalid XML format.");
      return "";
    }
  }

  //Method to get all Services by Host Name
  public String[][] getServiceFromRecycleBin(String hostName)
  {
    Log.debugLog(className, "getServiceFromRecycleBin", "", "", "Method started");
    try
    {
      ArrayList alServiceName = new ArrayList();
      String osname = System.getProperty("os.name").trim().toLowerCase();
      String deleteDir = "/.deletedservices/";
      if(osname.startsWith("win"))
        deleteDir = "/deletedservices/";

      File recycleBinDir = new File(getCorrelationPath() + hostName + deleteDir);
      Log.debugLog(className, "getServiceFromRecycleBin", "", "", "recycle path = " + getCorrelationPath() + hostName + deleteDir);
      File[] files = recycleBinDir.listFiles();
      if(files == null || files.length <= 0)
        return new String[0][0];
      for(int i = 0; i < files.length; i++)
      {
        if(files[i].isDirectory())
        {
          String[] tempArray = new String[2];
          tempArray[0] = files[i].getName();

          long datetime = files[i].lastModified();
          Date d = new Date(datetime);
          SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
          String dateString = sdf.format(d);

          tempArray[1] = dateString;

          alServiceName.add(tempArray);
        }
      }

      String[][] arrServiceNames = new String[alServiceName.size()][2];
      for(int j = 0; j < alServiceName.size(); j++)
      {
        arrServiceNames[j] = (String[])alServiceName.get(j);
      }
      return arrServiceNames;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getServiceFromRecycleBin", "", "", "Exception - ", ex);
      return null;
    }
  }

  /*
  * This method is used to add or update the service time from capture script, when user create the service after recording
  * @scriptName: to get script name, example: default/default/capture3125790
  * @res_svc_time_file: to get service_{parent_id}_{child_id}_{subchild_id} file for every response, this file exist in response folder
  * @hostName: to get host name for services
  * @serviceName: to get service name, which will be updat
  */
  public boolean updateServiceTimeFromCaptureService(String scriptName, String res_svc_time_file, String hostName, String serviceName)
  {
    Log.debugLog(className, "updateServiceTimeFromCaptureService", "", "", "Method started");
    String scriptWorkPath = getScript_RequestResponsePath("response", scriptName);
    String svcTime = "";
    String svcMode = "2";
    try
    {
      Log.debugLog(className, "updateServiceTimeFromCaptureService", "", "", " service time file -" + res_svc_time_file);
      Vector vecResponseConfData = readFile(scriptWorkPath + res_svc_time_file, true);
      Vector completeControlFile = readFile(getServiceConfFilePath(hostName, serviceName), false);
      if(vecResponseConfData == null)
      {
        Log.debugLog(className, "updateServiceTimeFromCaptureService", "", "", " service time file does not exist");
        return false;
      }

      if(completeControlFile == null)
      {
        Log.debugLog(className, "updateServiceTimeFromCaptureService", "", "", " service does not exist");
        return false;
      }

      for(int i = 0; i < vecResponseConfData.size(); i++)
      {
        String arrURLLine[] = rptUtilsBean.strToArrayData(vecResponseConfData.elementAt(i).toString(), " ");

        if(arrURLLine.length <= 0)
          continue;
        if(arrURLLine[0].toUpperCase().equals("SVC_TIME"))
        {
          if(arrURLLine[1].equals("2"))
          {
            svcTime = arrURLLine[2];
            Log.debugLog(className, "updateServiceTimeFromCaptureService", "", "", " service time -" + svcTime);
          }
        }
      }

      Vector modifiedVector = new Vector();
      String strSearch = "SVC_TIME";
      boolean matchFound = false;

      if(svcTime.equals(""))
      {
        Log.debugLog(className, "updateServiceTimeFromCaptureService", "", "", " service time does not exist");
        return false;
      }

      for(int yy = 0; yy < completeControlFile.size(); yy++)
      {
        String line = completeControlFile.elementAt(yy).toString();
        String[] arrURLLine = rptUtilsBean.strToArrayData(line, " ");

        if(line.trim().startsWith(strSearch)) // Method found
        {
          String svcTimeKeyword = "SVC_TIME " + svcMode + " " + svcTime;
          modifiedVector.add(svcTimeKeyword);
          Log.debugLog(className, "updateServiceTimeFromCaptureService", "", "", "Add service time in service.conf = " + svcTimeKeyword);
          matchFound = true;
        }
        else
          modifiedVector.add(line);
      }

      if(!matchFound)
      {
        String svcTimeKeyword = "SVC_TIME " + svcMode + " " + svcTime;
        Log.debugLog(className, "updateServiceTimeFromCaptureService", "", "", "Add service time in service.conf = " + svcTimeKeyword);
        modifiedVector.add(svcTimeKeyword);
      }

      if(!writeToFile(getServiceConfFilePath(hostName, serviceName), modifiedVector, hostName, serviceName))
        return false;

      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "updateServiceTimeFromCaptureService", "", "", "Exception - ", ex);
      return false;
    }
  }

  public String getForwardLocationFromCaptureService(String scriptName, String res_svc_time_file, String URL)
  {
    Log.debugLog(className, "getForwardLocationFromCaptureService", "", "", "Method started");
    String scriptWorkPath = getScript_RequestResponsePath("response", scriptName);
    String location = "";

    try
    {
      Log.debugLog(className, "getForwardLocationFromCaptureService", "", "", " service time file -" + res_svc_time_file);
      Vector vecResponseConfData = readFile(scriptWorkPath + res_svc_time_file, true);
      if(vecResponseConfData == null)
      {
        Log.debugLog(className, "getForwardLocationFromCaptureService", "", "", " service time file does not exist");
        return "NA";
      }

      for(int i = 0; i < vecResponseConfData.size(); i++)
      {
        String arrURLLine[] = rptUtilsBean.strToArrayData(vecResponseConfData.elementAt(i).toString(), " ");

        if(arrURLLine.length <= 0)
          continue;
        if(arrURLLine[0].toUpperCase().equals("RECORDING_PARAMETERS"))
        {
          int valueReadFrom = 2;
          if(getURLType(URL).trim().equals("JDBC"))
            valueReadFrom = 1;
          if(arrURLLine.length > valueReadFrom)
          {
            location = arrURLLine[valueReadFrom];
            for(int k = valueReadFrom + 1; k < arrURLLine.length; k++)
            {
              location = location + arrURLLine[k];
              //System.out.println("location = " + location);
            }
            Log.debugLog(className, "getForwardLocationFromCaptureService", "", "", " service time -" + location);
          }
        }
      }

      return location;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "updateServiceTimeFromCaptureService", "", "", "Exception - ", ex);
      return "NA";
    }
  }

  /* This function get the recoder port list
   * First it will check RECORDER_PORT in the controller specific hpd.conf file
   * if exist it will give the list
   * else it will take from the /etc/cav_domain_controller.conf file
   */
  public Vector getRecoderPortList()
  {
    Log.debugLog(className, "getRecoderPortList", "", "", "Method called");

    String hpdPath = getHPDPath();
    Vector vecResult = new Vector();
    boolean flag = true;
    //getting data from hpd.conf file
    Vector vecData = rptUtilsBean.readFileInVector(hpdPath + "/conf/hpd.conf");
    int recPortCount = 50;

    DomainController domainController = new DomainController(recPortCount);

    String recString = "RECORDER_PORT ";

    if(vecData != null)
    {
      for(int i = 0; i < vecData.size(); i++)
      {
        String dataLine = vecData.get(i).toString().trim();
        //checking RECORDER_PORT keyword is exist or not
        if(dataLine.startsWith(recString))
        {
          flag = false; //checking setting port from hpd.conf
          String recPort = dataLine.substring(recString.length());
          vecResult = domainController.splitPortWithCommaDash(recPort.trim());
          break;
        }
      }
    }

    //If keyword does not exist in hpd.conf
    //so we are getting from /etc/cav_domain_controller.conf file
    if(flag)
    {
      String port[] = domainController.getApplianceAndContInfo();
      vecResult = domainController.getPortFromController(port[2], port[1], recString.trim());
    }
    return vecResult;
  }

  /*
   * This function is used to split the condition based on AND,OR
   * Thit will return arraylist which condtain first element is condition and second element is logical operator (AND or OR)
   */
  public static ArrayList<String> splitFromANDandOR(String str)
  {
    Log.debugLog(className, "splitFromANDandOR", "", "", "Method started");
    ArrayList<String> arrCondition = new ArrayList<String>();
    int count = 1; //this is used for ingnoring between ""
    int index = 0; //This is used to keep track of splited str
    int i = 0;
    for(i = 0; i < str.length(); i++)
    {
      if(str.charAt(i) == ' ')
        continue;

      if(str.charAt(i) == '"')
      {
        if(count % 2 == 0 && str.charAt(i - 1) == '\\')
          continue;
        count++;
      }

      if(i != 0 && count % 2 != 0 && (str.charAt(i) == 'A' || str.charAt(i) == 'O') && (str.charAt(i - 1) == ' ' || str.charAt(i - 1) == ')'))
      {
        if(str.charAt(i) == 'A')
        {
          String tmpStr = str.substring(i, i + 3);
          if(tmpStr.equals("AND") && (i + 3) < str.length() && (str.charAt(i + 3) == ' ' || str.charAt(i + 3) == '('))
          {
            String strLeft = str.substring(index, i);
            if(strLeft.trim().equals(""))
            {
              Log.debugLog(className, "splitFromANDandOR", "", "", "Error in AND Oper Nothing is in Left Side");
              break;
            }
            else
            {
              arrCondition.add(strLeft.trim());
              arrCondition.add("AND");
              index = i + 3;
              i = i + 3;
            }
          }
          else if(tmpStr.equals("AND") && (i + 3) >= str.length())
          {
            Log.debugLog(className, "splitFromANDandOR", "", "", "Error: AND at end");
            break;
          }
        }
        else if(str.charAt(i) == 'O')
        {
          String tmpStr = str.substring(i, i + 2);
          if(tmpStr.equals("OR") && (i + 2) < str.length() && (str.charAt(i + 2) == ' ' || str.charAt(i + 2) == '('))
          {
            String strLeft = str.substring(index, i);
            if(strLeft.trim().equals(""))
            {
              Log.debugLog(className, "splitFromANDandOR", "", "", "Error in OR Oper Nothing is in Left Side");
              break;
            }
            else
            {
              arrCondition.add(strLeft.trim());
              arrCondition.add("OR");
              index = i + 2;
              i = i + 2;
            }
          }
          else if(tmpStr.equals("OR") && (i + 2) >= str.length())
          {
            Log.debugLog(className, "splitFromANDandOR", "", "", "Error: OR at the end");
            break;
          }
        }
      }
    }
    if(i == str.length())
    {
      String strLeft = str.substring(index, i);
      if(strLeft.trim().equals("") && index == 0)
      {
        Log.debugLog(className, "splitFromANDandOR", "", "", "Error: Please enter the condition.");
      }
      else if(strLeft.trim().equals(""))
      {
        Log.debugLog(className, "splitFromANDandOR", "", "", "Error: Please enter the right side condition after logical operator AND or OR.");
      }
      else
      {
        arrCondition.add(strLeft.trim());
      }
    }
    return arrCondition;
  }

  /*
   * This function is used to convert the old format of template condition convert into new design
   */
  public String newFormatTemplateCondition(String str)
  {
    Log.debugLog(className, "newFormatTemplateCondition", "", "", "Method started");
    ArrayList list = splitFromANDandOR(str);
    String newCondition = "";
    for(int i = 0; i < list.size(); i = i + 2)
    {
      newCondition = newCondition + convertCondition(list.get(i).toString()) + " ";
      Log.debugLog(className, "newFormatTemplateCondition", "", "", "Converted String = " + newCondition);
      if((i + 1) < list.size())
      {
        newCondition = newCondition + list.get(i + 1).toString() + " ";
      }
    }
    Log.debugLog(className, "newFormatTemplateCondition", "", "", "Upgrade condition = " + newCondition);
    return newCondition;
  }

  /*
   * This method is used to convert one condition in new format
   */

  public String convertCondition(String str)
  {
    Log.debugLog(className, "convertCondition", "", "", "Method started");

    if(str.startsWith("(") && str.endsWith(")"))
      str = str.substring(1, str.length() - 1).trim();

    str = str.replace("Not Equal To", "!=");
    str = str.replace("Less Than Equal To", "<=");
    str = str.replace("Greater Than Equal TO", ">=");
    str = str.replace("Less Than", "<");
    str = str.replace("Greater Than", ">");
    str = str.replace("Equal To", "=");

    String leftVarriable = "";
    String rightVarriable = "";
    String oprator = "";
    String variableType = "String";

    // < , > , != , = , <= , >=

    for(int i = 0; i < str.length(); i++)
    {
      if(i < str.length() && str.charAt(i) == '<' || str.charAt(i) == '>' || str.charAt(i) == '!' || str.charAt(i) == '=')
      {
        if(str.charAt(i + 1) == '=')
        {
          leftVarriable = str.substring(0, i).trim();
          rightVarriable = str.substring(i + 1).trim();
          oprator = str.charAt(i) + "" + str.charAt(i + 1);
          break;
        }
        else
        {
          leftVarriable = str.substring(0, i).trim();
          rightVarriable = str.substring(i).trim();
          oprator = str.charAt(i) + "";
          break;
        }
      }
    }

    if(leftVarriable.startsWith("Variable"))
    {
      int startIndex = leftVarriable.indexOf("(");
      int endIndex = leftVarriable.indexOf(",");
      int lastIndex = leftVarriable.indexOf(")");
      if(endIndex > -1 && lastIndex > -1)
        variableType = leftVarriable.substring(endIndex + 1, lastIndex).trim();
      if(startIndex > -1 && endIndex > -1)
        leftVarriable = leftVarriable.substring(startIndex + 1, endIndex);
    }

    if(rightVarriable.startsWith("Variable"))
    {
      int startIndex = rightVarriable.indexOf("(");
      int endIndex = rightVarriable.indexOf(")");
      if(startIndex > -1 && endIndex > -1)
        rightVarriable = rightVarriable.substring(startIndex + 1, endIndex);
    }
    else
    {
      int startIndex = rightVarriable.indexOf("(");
      int endIndex = rightVarriable.indexOf(")");
      if(startIndex > -1 && endIndex > -1)
        rightVarriable = rightVarriable.substring(startIndex + 1, endIndex);
    }

    if(rightVarriable.startsWith("\""))
    {
      if(variableType.equals("String"))
        str = leftVarriable + " " + oprator + " " + rightVarriable;
      else
      {
        boolean bool = false;
        String tempVariable = rightVarriable.trim().substring(1, rightVarriable.length() - 1);
        bool = isNumeric(tempVariable);
        if(bool)
          str = leftVarriable + " " + oprator + " " + tempVariable;
        else
          str = leftVarriable + " " + oprator + " " + rightVarriable;
      }
    }
    else
    {
      if(variableType.equals("String"))
        str = leftVarriable + " (String) " + oprator + " " + rightVarriable;
      else
        str = leftVarriable + " " + oprator + " " + rightVarriable;
    }

    Log.debugLog(className, "convertCondition", "", "", "new condition = " + str);
    return str;
  }

  /*
   * This method is used to migrate all or particular service in new template condition design
   * @strHost : used to provide host Name
   * @strService :used to provide service name, if this arugment is blank then all services will be migrate in new template condition format
   */
  public boolean MigrateTemplateConditionBasedOnService(String strHost, String strService)
  {
    Log.debugLog(className, "MigrateTemplateConditionBasedOnService", "", "", "Method started");
    boolean bool = true;
    boolean isAllMigrate = false;

    if(strService.trim().equals(""))
      isAllMigrate = true;

    if(isAllMigrate)
    {
      String[] hostNames = getHOSTNames();
      if(hostNames == null || hostNames.length <= 0)
      {
        Log.debugLog(className, "MigrateTemplateConditionBasedOnService", "", "", "No Host Name found ");
        return false;
      }

      Log.debugLog(className, "MigrateTemplateConditionBasedOnService", "", "", "Number of host name found = " + hostNames.length);

      ArrayList alHostService = new ArrayList();
      for(int h = 0; h < hostNames.length; h++)
      {
        String[] serviceNames = getServicesByHostName(hostNames[h]);
        for(int s = 0; s < serviceNames.length; s++)
        {
          String[][] arrTemp = new String[1][2];
          arrTemp[0][0] = hostNames[h];
          arrTemp[0][1] = serviceNames[s];
          alHostService.add(arrTemp);
        }
        for(int hs = 0; hs < alHostService.size(); hs++)
        {
          String[][] serviceHost = (String[][])alHostService.get(hs);
          String host = serviceHost[0][0];
          String service = serviceHost[0][1];
          Vector hpdURLData = readFile(getServiceConfFilePath(host, service), false);
          boolean isCreateVersion = true;
          String strSearch = "RESPONSE_TEMPLATE";
          String URL = "";

          Vector modifiedFile = new Vector();

          for(int yy = 0; yy < hpdURLData.size(); yy++)
          {
            String line = hpdURLData.elementAt(yy).toString();

            String condition = "";

            if(line.startsWith(strSearch))
            {
              line = line.trim();
              if(isCreateVersion && line.contains("(Variable"))
              {
                cvsCommitService(host, service, "NetStorm", "Migration to new template format");
                isCreateVersion = false;
              }
              line = upgradeTemplateCondition(line);
            }
            if(!line.equals(""))
            {
              String[] arrURLLine = rptUtilsBean.strToArrayData(line, " ");
              if(arrURLLine.length > 0)
              {
                if(arrURLLine[0].equals("URL"))
                {
                  URL = arrURLLine[1];
                }
                else if(arrURLLine[0].equals("#URL") || arrURLLine[0].equals("#"))
                {
                  if(arrURLLine[0].equals("#URL"))
                    URL = arrURLLine[1];
                  else
                    URL = arrURLLine[2];
                }
              }
            }
            modifiedFile.add(line);
          }

          if(!writeToFile(getServiceConfFilePath(host, service), modifiedFile, host, service))
            bool = false;

          String conditionalBlocks = getCondLogicBlockFromCtrlFileByService(host, service, URL).toString();
          if(conditionalBlocks.contains("(Variable"))
          {
            conditionalBlocks = migrateConditionBlocks(conditionalBlocks);
            if(isCreateVersion)
            {
              cvsCommitService(host, service, "NetStorm", "Migration to new template format");
              isCreateVersion = false;
            }
            saveCondLogicBlockInCtrlFileByService(host, service, URL, conditionalBlocks);
          }
        }
      }
    }
    else
    {
      Vector hpdURLData = readFile(getServiceConfFilePath(strHost, strService), false);
      String strSearch = "RESPONSE_TEMPLATE";
      Vector modifiedFile = new Vector();
      boolean isCreateVersion = true;
      String URL = "";
      for(int yy = 0; yy < hpdURLData.size(); yy++)
      {
        String line = hpdURLData.elementAt(yy).toString();
        String condition = "";
        if(line.trim().startsWith(strSearch))
        {
          line = line.trim();
          if(isCreateVersion && line.contains("(Variable"))
          {
            cvsCommitService(strHost, strService, "NetStorm", "Migration to new template format");
            isCreateVersion = false;
          }

          line = upgradeTemplateCondition(line);
        }

        if(!line.equals(""))
        {
          String[] arrURLLine = rptUtilsBean.strToArrayData(line, " ");
          if(arrURLLine.length > 0)
          {
            if(arrURLLine[0].equals("URL"))
            {
              URL = arrURLLine[1];
            }
            else if(arrURLLine[0].equals("#URL") || arrURLLine[0].equals("#"))
            {
              if(arrURLLine[0].equals("#URL"))
                URL = arrURLLine[1];
              else if(line.trim().startsWith("# URL"))
                URL = arrURLLine[2];
            }
          }
        }
        modifiedFile.add(line);
      }

      if(!writeToFile(getServiceConfFilePath(strHost, strService), modifiedFile, strHost, strService))
        bool = false;

      String conditionalBlocks = getCondLogicBlockFromCtrlFileByService(strHost, strService, URL).toString();

      if(conditionalBlocks.contains("(Variable"))
      {
        conditionalBlocks = migrateConditionBlocks(conditionalBlocks);
        if(isCreateVersion)
        {
          cvsCommitService(strHost, strService, "NetStorm", "Migration to new template format");
          isCreateVersion = false;
        }
        saveCondLogicBlockInCtrlFileByService(strHost, strService, URL, conditionalBlocks);
      }
    }

    return bool;
  }

  public boolean copyFileXLSToCSV(String sourceFile, String destFile, String delimiter)
  {
    OutputStream outStream = null;
    boolean bool = true;
    try
    {
      File sfile = new File(sourceFile);
      Vector vec = readXLSFile(sourceFile, 0);
      String strData = printCellDataToConsoleForHLXS(vec, delimiter);
      //System.out.println("sourceFile " + sourceFile);
      System.out.println("destFile" + destFile);
      System.out.println(strData);
      byte buffer[] = strData.getBytes();

      File dfile = new File(destFile);
      if(!dfile.exists())
        dfile.createNewFile();
      outStream = new FileOutputStream(dfile);
      outStream.write(buffer);
      outStream.close();
      if(sfile.exists())
        sfile.delete();

      return true;
    }
    catch(IOException e)
    {
      e.printStackTrace();
      return false;
    }
  }

  /*
   * This function will fetch the template condtion form response template line in service.conf
   * and call for migrate into new format and return reponse template line with new format.
   */
  public String upgradeTemplateCondition(String line)
  {
    Log.debugLog(className, "upgradeTemplateCondition", "", "", "Method started");
    Log.debugLog(className, "upgradeTemplateCondition", "", "", "line = " + line);
    String condition = "";

    String[] arrURLLine = rptUtilsBean.strToArrayData(line, " ");
    if(!line.contains(" Forward ") && !line.contains(" FileBased "))
    {
      for(int zz = 4; zz < arrURLLine.length; zz++)
      {
        if(zz == 4)
          condition = arrURLLine[zz];
        else
          condition = condition + " " + arrURLLine[zz];
      }
      if(condition.trim().startsWith("(Variable"))
      {
        condition = newFormatTemplateCondition(condition);
        line = "";
        for(int i = 0; i < 4; i++)
        {
          line = line + arrURLLine[i] + " ";
        }
        line = line + condition;
        return line;
      }
    }
    if(line.contains(" FileBased "))
    {
      for(int zz = 5; zz < arrURLLine.length; zz++)
      {
        if(zz == 5)
          condition = arrURLLine[zz];
        else
          condition = condition + " " + arrURLLine[zz];
      }
      if(condition.trim().startsWith("(Variable("))
      {
        condition = newFormatTemplateCondition(condition);
        line = "";
        for(int i = 0; i < 5; i++)
        {
          line = line + arrURLLine[i] + " ";
        }

        line = line + condition;
        return line;
      }
    }
    if(line.contains(" Forward "))
    {
      for(int zz = 6; zz < arrURLLine.length; zz++)
      {
        if(zz == 6)
          condition = arrURLLine[zz];
        else
          condition = condition + " " + arrURLLine[zz];
      }
      if(condition.trim().startsWith("(Variable("))
      {
        condition = newFormatTemplateCondition(condition);
        line = "";
        for(int i = 0; i < 6; i++)
        {
          line = line + arrURLLine[i] + " ";
        }

        line = line + condition;
        return line;
      }
    }
    return line;
  }

  public boolean isNumeric(String str)
  {
    try
    {
      Integer i = Integer.parseInt(str);
    }
    catch(NumberFormatException nfe)
    {
      return false;
    }
    return true;
  }

  /*
   * This function parse the whole conditional logic and add into list;
   * Like if we get 'if' 'Else if' and condition with statement and statement
   * ['IF', 'condition with statement' ,'Else' , 'If' , 'condition with statement'..]
   * retrun arraylist which contains sequencly Element as we get in parse to conditional blocks.
   */
  public ArrayList parseConditionalBlock(String str)
  {
    Log.debugLog(className, "parseConditionalBlock", "", "", "Method started");
    int firstIndex = 0;
    ArrayList list = new ArrayList();

    for(int i = 0; i < str.length(); i++)
    {
      if(str.charAt(i) == ' ')
      {
        firstIndex++;
        continue;
      }
      if(str.charAt(i) == '\n')
      {
        firstIndex++;
      }
      else
        break;
    }

    str = str.substring(firstIndex, str.length()).trim() + " ";

    if(str.startsWith("If ") || str.startsWith("If(") || str.startsWith("If\n"))
    {
      Log.debugLog(className, "parseConditionalBlock", "", "", "'If' found");
      int count = 1; // used to do nothing between " ".
      int index = 0; //used to get one condition or statement.
      boolean findIfFlag = false;

      for(int i = 0; i < str.length(); i++)
      {
        if(str.charAt(i) == '"')
        {
          if(count % 2 == 0 && str.charAt(i) == '"' && str.charAt(i - 1) == '\\')
            continue;
          count++;
        }

        if(count % 2 == 1 && str.charAt(i) == 'E' && !findIfFlag)
        {
          if(str.charAt(i - 1) == ' ' || str.charAt(i - 1) == '\n')
          {
            if((i + 5) <= str.length() && (str.substring(i, i + 5).equals("Else ") || str.substring(i, i + 5).equals("Else\n")))
            {
              boolean newLineFlag = false;
              for(int j = i - 1; j >= 0; j--)
              {
                if(str.charAt(j) == ' ')
                  continue;
                if(str.charAt(j) == '\n')
                {
                  newLineFlag = true;
                  break;
                }
                if(str.charAt(j) != '\n')
                  break;
              }

              String conditionStr = str.substring(index, i);
              list.add(conditionStr);
              Log.debugLog(className, "parseConditionalBlock", "", "", "Condition is :" + conditionStr);
              findIfFlag = true;
              index = i + 4;
              list.add("Else");

            }
          }
        }

        if(count % 2 == 1 && str.charAt(i) == 'I' && i != 0)
        {
          if(str.charAt(i - 1) == ' ' || str.charAt(i - 1) == '\n')
          {
            if((i + 3) <= str.length() && (str.substring(i, i + 3).equals("If ") || str.substring(i, i + 3).equals("If(") || str.substring(i, i + 3).equals("If\n")))
            {
              Log.debugLog(className, "parseConditionalBlock", "", "", "'If' found");
              boolean newLineFlag = false;
              for(int j = i - 1; j >= 0; j--)
              {
                if(str.charAt(j) == ' ')
                  continue;
                if(str.charAt(j) == '\n')
                {
                  newLineFlag = true;
                  break;
                }
                if(str.charAt(j) == 'e')
                {
                  if((j - 4) > 0 && (str.substring(j - 4, j + 1).equals(" Else") || str.substring(j - 4, j + 1).equals("\nElse")))
                  {
                    newLineFlag = true;
                  }
                  break;
                }
                if(str.charAt(j) != '\n')
                  break;
              }

              findIfFlag = false;
              String conditionStr = "";

              conditionStr = str.substring(index, i);

              if(!conditionStr.trim().equals(""))
                list.add(conditionStr);
              Log.debugLog(className, "parseConditionalBlock", "", "", "Condition is :" + conditionStr);
              index = i;

            }
          }
        }
      }

      String conditionStr = str.substring(index, str.length());
      Log.debugLog(className, "parseConditionalBlock", "", "", "Remaining condition is :" + conditionStr);
      list.add(conditionStr);
    }
    else
    {
      Log.debugLog(className, "parseConditionalBlock", "", "", "condition block should start with if statement.");
      return list;
    }

    ArrayList complateList = new ArrayList();

    for(int i = 0; i < list.size(); i++)
    {
      String getString = list.get(i).toString().trim();

      if(getString.startsWith("If"))
      {
        int indexIf = getString.indexOf("If");
        if(indexIf > -1)
        {
          complateList.add("If");
          complateList.add(getString.substring(indexIf + 2));
        }
      }
      else
      {
        complateList.add(getString);
      }
    }
    return complateList;
  }

  /*
   * This function will migrate the conditional logic into new format.
   */
  public String migrateConditionBlocks(String str)
  {
    Log.debugLog(className, "migrateConditionBlocks", "", "", "Method started");
    String newConditionBlock = "";
    //parse conditional logic and get all element into the arraylist
    ArrayList list = parseConditionalBlock(str.trim() + " ");

    for(int i = 0; i < list.size(); i++)
    {
      String getString = list.get(i).toString().trim();
      if(getString.equals("If"))
      {
        newConditionBlock = newConditionBlock + "  " + getString;
        continue;
      }
      else if(getString.equals("Else") && (i + 1 < list.size() && list.get(i + 1).toString().trim().equals("If")))
      {
        newConditionBlock = newConditionBlock + "  " + getString + " " + list.get(i + 1).toString().trim();
        i = i + 1;
        continue;
      }
      else if(getString.equals("Else"))
      {
        newConditionBlock = newConditionBlock + "  " + getString + "\n";
        continue;
      }
      else if(getString.trim().startsWith("(Variable"))
      {
        int count = 1;
        String condAndStatement = getString.replace("\n", " ");
        //evaluate condition and statement saparetly
        for(int k = 0; k < condAndStatement.length(); k++)
        {
          if(condAndStatement.charAt(k) == '"')
          {
            if(count % 2 == 0 && condAndStatement.charAt(k) == '"' && condAndStatement.charAt(k - 1) == '\\')
              continue;
            count++;
          }

          if(count % 2 == 1 && condAndStatement.charAt(k) == 'S')
          {
            if(condAndStatement.charAt(k - 1) == ' ' || condAndStatement.charAt(k - 1) == '\n' || condAndStatement.charAt(k - 1) == ')')
            {
              if((k + 4) <= condAndStatement.length() && condAndStatement.substring(k, k + 4).equals("Set "))
              {
                for(int j = i - 1; j >= 0; j--)
                {
                  if(condAndStatement.charAt(j) == ' ')
                    continue;

                  if(condAndStatement.charAt(j) != ' ')
                    break;
                }
                //convert condition into new condition foramt
                String remainCondition = newFormatTemplateCondition(condAndStatement.substring(0, k));

                newConditionBlock = newConditionBlock + "( " + remainCondition + ")\n";

                ArrayList setList = parseSetStatements(condAndStatement.substring(k));
                for(int l = 0; l < setList.size(); l++)
                {
                  String statement = convertStatement(setList.get(l).toString().trim());
                  newConditionBlock = newConditionBlock + "    " + statement + "\n";
                }
                break;
              }
            }
          }
        }
        continue;
      }
      else
      {
        ArrayList setList = parseSetStatements(getString);
        for(int l = 0; l < setList.size(); l++)
        {
          String statement = convertStatement(setList.get(l).toString().trim());
          newConditionBlock = newConditionBlock + "    " + statement + "\n";
        }
      }
    }
    Log.debugLog(className, "migrateConditionBlocks", "", "", "new Format of conditional block :" + newConditionBlock);
    return newConditionBlock;
  }

  /*
   * This function convert old statement of conditional statement into new format
   */
  public String convertStatement(String str)
  {
    Log.debugLog(className, "convertStatement", "", "", "Method started");
    if(!str.trim().startsWith("Set"))
      return str;
    str = str.substring(str.indexOf("Set") + 3).trim();
    String leftVarriable = "";
    String rightVarriable = "";
    String operator = "";
    for(int i = 0; i < str.length(); i++)
    {
      if(str.charAt(i) == '=')
      {
        leftVarriable = str.substring(0, i).trim();
        rightVarriable = str.substring(i + 1).trim();
        operator = str.charAt(i) + "";
        break;
      }
    }

    leftVarriable = leftVarriable.replace(" ", "");
    int indexVariable = leftVarriable.indexOf("Variable(");
    int lastIndex = leftVarriable.lastIndexOf(")");
    if(indexVariable > -1 && lastIndex > -1 && indexVariable < lastIndex)
    {
      leftVarriable = leftVarriable.substring(indexVariable + 9, lastIndex);
    }

    if(rightVarriable.startsWith("Constant"))
    {
      indexVariable = rightVarriable.indexOf("Constant(");
      lastIndex = rightVarriable.lastIndexOf(")");
      if(indexVariable > -1 && lastIndex > -1 && indexVariable < lastIndex)
      {
        rightVarriable = rightVarriable.substring(indexVariable + 9, lastIndex);
      }
    }
    else if(rightVarriable.startsWith("Variable("))
    {
      rightVarriable = rightVarriable.replace(" ", "");
      indexVariable = rightVarriable.indexOf("Variable(");
      lastIndex = rightVarriable.lastIndexOf(")");

      if(indexVariable > -1 && lastIndex > -1 && indexVariable < lastIndex)
      {
        rightVarriable = rightVarriable.substring(indexVariable + 9, lastIndex);
      }
    }

    str = "Set" + " " + leftVarriable + " = " + rightVarriable;
    Log.debugLog(className, "convertStatement", "", "", "New format of Statement is = " + str);

    return str;
  }

  /*
   * If conditional blocks contains mulitple statement
   * Then parse every statement and add into in list.
   * return arraylist with statement.
   */
  public static ArrayList parseSetStatements(String str)
  {
    Log.debugLog(className, "parseSetStatements", "", "", "Method started");
    int count = 1;
    int index = 0;
    ArrayList list = new ArrayList();

    for(int i = 0; i < str.length(); i++)
    {
      if(str.charAt(i) == '"')
      {
        if(count % 2 == 0 && str.charAt(i) == '"' && str.charAt(i - 1) == '\\')
          continue;
        count++;
      }

      if(count % 2 == 1 && str.charAt(i) == 'S' && i != 0)
      {
        if(str.charAt(i - 1) == ' ' || str.charAt(i - 1) == '\n' || str.charAt(i - 1) == ')')
        {
          if(((i + 4) <= str.length() && str.substring(i, i + 4).equals("Set ")) || (i + 3) == str.length() && str.substring(i, i + 3).equals("Set"))
          {
            boolean newLineFlag = false;
            for(int j = i - 1; j >= 0; j--)
            {
              if(str.charAt(j) == ' ')
                continue;
              if(str.charAt(j) == '\n')
              {
                newLineFlag = true;
                break;
              }
              if(str.charAt(j) != '\n')
                break;
            }
            String sigleSetStat = str.substring(index, i);
            Log.debugLog(className, "parseSetStatements", "", "", "Statement is = " + sigleSetStat);
            list.add(sigleSetStat);
            index = i;
          }
        }
      }
    }
    String sigleSetStat = str.substring(index, str.length());
    Log.debugLog(className, "parseSetStatements", "", "", "Statement is = " + sigleSetStat);
    list.add(sigleSetStat);

    return list;
  }

  /*
   * This function is used to get All template information in particular service
   * @return 2-D array of template infromation.
   */
  public String[][] getResponseTemplateInfoByURL(String hostName, String serviceName, String URL)
  {
    Log.debugLog(className, "getResponseTemplateInfoByURL", "", "", "Method started");
    try
    {
      ArrayList alResponseTemplateName = new ArrayList();
      String arrDataValues[][] = null;
      Vector hpdURLData = readFile(getServiceConfFilePath(hostName, serviceName), true);

      if((hpdURLData == null) || (hpdURLData.size() <= 0))
      {
        Log.debugLog(className, "getResponseTemplateInfoByURL", "", "", "Service File not found, It may be corrupted. HOST=" + hostName + ", Service=" + serviceName);
        return new String[0][0];
      }

      for(int k = 0; k < hpdURLData.size(); k++)
      {
        String templateName = "";
        String templateType = "FileBased";
        String templateResFile = "";
        String templateReqFile = "";
        String templateExtension = "None";
        String Status = "";
        int templateConditionIndex = 4;
        String TemplateCondition = "NA";
        String arrTemplateData[] = new String[7];

        String[] arrURLLine = rptUtilsBean.strToArrayData(hpdURLData.elementAt(k).toString(), " ");
        if(arrURLLine.length <= 0)
          continue;
        if(arrURLLine[0].toUpperCase().equals("RESPONSE_TEMPLATE"))
        {
          if(arrURLLine.length > 1)
            templateName = arrURLLine[1];

          if(hpdURLData.elementAt(k).toString().contains(" FileBased "))
          {
            if(arrURLLine.length < 6)
            {
              Log.debugLog(className, "getResponseTemplateInfoByURL", "", "", "format of response template is not proper at line = " + k);
              continue;
            }
            templateType = arrURLLine[2];
            templateResFile = arrURLLine[3];
            templateReqFile = templateResFile + ".req";
            Status = arrURLLine[4];
            templateConditionIndex = 5;
          }
          else if(hpdURLData.elementAt(k).toString().contains(" Simulate "))
          {
            if(arrURLLine.length < 7)
            {
              Log.debugLog(className, "getResponseTemplateInfoByURL", "", "", "format of response template is not proper at line = " + k);
              continue;
            }

            templateType = arrURLLine[2] + " " + arrURLLine[3];
            templateResFile = arrURLLine[4];
            templateReqFile = templateResFile + ".req";
            Status = arrURLLine[5];
            templateConditionIndex = 6;
          }
          else if(hpdURLData.elementAt(k).toString().contains(" Forward "))
          {
            if(arrURLLine.length < 7)
            {
              Log.debugLog(className, "getResponseTemplateInfoByURL", "", "", "format of response template is not proper at line = " + k);
              continue;
            }

            templateType = arrURLLine[2] + " " + arrURLLine[3];
            templateResFile = arrURLLine[4];
            templateReqFile = templateResFile + ".req";
            templateConditionIndex = 6;
            Status = arrURLLine[5];
          }
          else if(hpdURLData.elementAt(k).toString().contains(" RequestBased"))
          {
            if(arrURLLine.length < 5)
            {
              Log.debugLog(className, "getResponseTemplateInfoByURL", "", "", "format of response template is not proper at line = " + k);
              continue;
            }

            templateType = "Simulate";
            templateResFile = arrURLLine[3];
            templateReqFile = templateResFile + ".req";
            Status = arrURLLine[4];
            templateConditionIndex = 0;
          }
          else
          {
            if(arrURLLine.length < 5)
            {
              Log.debugLog(className, "getResponseTemplateInfoByURL", "", "", "format of response template is not proper at line = " + k);
              continue;
            }

            templateResFile = arrURLLine[2];
            templateReqFile = templateResFile + ".req";
            if(arrURLLine.length > 3)
              Status = arrURLLine[3];
          }

          if(templateResFile.lastIndexOf(".") > -1)
            templateExtension = templateResFile.substring(templateResFile.lastIndexOf("."));
          else
            templateExtension = "None";

          if(templateConditionIndex == 0)
          {
            TemplateCondition = "RequestBased";
          }
          else
          {
            for(int zz = templateConditionIndex; zz < arrURLLine.length; zz++)
            {
              if(zz == templateConditionIndex)
                TemplateCondition = arrURLLine[zz];
              else
                TemplateCondition = TemplateCondition + " " + arrURLLine[zz];
            }
          }
          if(templateName.trim().equals(""))
            continue;

          arrTemplateData[0] = templateName;
          arrTemplateData[1] = templateType;
          arrTemplateData[2] = templateResFile;
          arrTemplateData[3] = templateReqFile;
          arrTemplateData[4] = templateExtension;
          arrTemplateData[5] = Status;
          arrTemplateData[6] = TemplateCondition;
          alResponseTemplateName.add(arrTemplateData);
        }
      }

      arrDataValues = new String[alResponseTemplateName.size()][7];
      for(int xy = 0; xy < alResponseTemplateName.size(); xy++)
      {
        String[] arrTempData = (String[])alResponseTemplateName.get(xy);

        arrDataValues[xy][0] = arrTempData[0];
        arrDataValues[xy][1] = arrTempData[1];
        arrDataValues[xy][2] = arrTempData[2];
        arrDataValues[xy][3] = arrTempData[3];
        arrDataValues[xy][4] = arrTempData[4];
        arrDataValues[xy][5] = arrTempData[5];
        arrDataValues[xy][6] = arrTempData[6];
      }

      return arrDataValues;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getResponseTemplateNamesByURL", "", "", "Exception - ", ex);
      return new String[0][0];
    }
  }

  /*
   * This function is used to delete , update and add HTTP Status code template based
   * To delete: pass statusCode and description as blank
   * To update: pass new statusCode and description
   * To add: pass status code and description
   * if template name is updated then so need to pass old and new template name
   */
  public boolean updateTemplateBasedStatusCode(String hostName, String serviceName, String URL, String oldTemplateName, String newTemplateName, String statusCode, String description)
  {
    Log.debugLog(className, "updateTemplateBasedStatusCode", "", "", "Method started");
    try
    {
      Vector completeControlFile = readFile(getServiceConfFilePath(hostName, serviceName), false);
      Vector modifiedVector = new Vector();

      boolean statusCodeFound = false;//this is used to check status code is exist for particular template or not
      for(int yy = 0; yy < completeControlFile.size(); yy++)
      {
        String line = completeControlFile.elementAt(yy).toString().trim();

        if(line.startsWith("TEMPLATE_BASED_STATUS_CODE"))
        {
          String[] arrTempStatusCodeData = rptUtilsBean.split(line, " ");
          if(arrTempStatusCodeData[1].equals(oldTemplateName))
          {
            statusCodeFound = true;//status code is find for particular template

            if(!statusCode.equals(""))
            {
              String lineToUpdate = "TEMPLATE_BASED_STATUS_CODE " + newTemplateName + " " + statusCode;
              if(!description.equals(""))
                lineToUpdate = lineToUpdate + " " + description;
              modifiedVector.add(lineToUpdate);
            }
          }
          else
            modifiedVector.add(line);
        }
        else
        {
          modifiedVector.add(line);
        }
      }

      if(!statusCodeFound && !statusCode.equals(""))
      {
        String lineToUpdate = "TEMPLATE_BASED_STATUS_CODE " + newTemplateName + " " + statusCode;
        if(!description.equals(""))
          lineToUpdate = lineToUpdate + " " + description;
        modifiedVector.add(lineToUpdate);
      }

      if(!writeToFile(getServiceConfFilePath(hostName, serviceName), modifiedVector, hostName, serviceName))
        return false;

      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "updateTemplateBasedStatusCode", "", "", "Exception - ", ex);
      return false;
    }
  }

  /*
   * This function is used to add, update and delete Template Based HTTP Headers
   * If user pass as blank http Headers then this function will delete the HTTP header for particular template
   * If user pass http header and http header is not exist for the template then it will add the HTTP Header for the template otherwise it will update HTTP
   * headers.
   */
  public boolean updateTemplateBasedHttpHeaders(String hostName, String serviceName, String URL, String oldTemplateName, String newTemplateName, String headers)
  {
    Log.debugLog(className, "updateTemplateBasedHttpHeaders", "", "", "Method started");
    try
    {
      Vector completeControlFile = readFile(getServiceConfFilePath(hostName, serviceName), false);
      Vector modifiedVector = new Vector();

      boolean statusCodeFound = false;
      for(int yy = 0; yy < completeControlFile.size(); yy++)
      {
        String line = completeControlFile.elementAt(yy).toString().trim();

        if(line.startsWith("TEMPLATE_BASED_HEADER"))
        {
          String[] arrTempStatusCodeData = rptUtilsBean.split(line, " ");
          if(!arrTempStatusCodeData[1].equals(oldTemplateName))
          {
            modifiedVector.add(line);
          }
        }
        else
        {
          modifiedVector.add(line);
        }
      }

      if(!headers.equals(""))
      {
        String[] arrHeaders = rptUtilsBean.split(headers, "\n");
        for(int i = 0; i < arrHeaders.length; i++)
        {
          if(!arrHeaders[i].equals(""))
          {
            String lineToUpdate = "TEMPLATE_BASED_HEADER " + newTemplateName + " " + arrHeaders[i].replace("\r", "");
            modifiedVector.add(lineToUpdate);
          }
        }
      }
      if(!writeToFile(getServiceConfFilePath(hostName, serviceName), modifiedVector, hostName, serviceName))
        return false;

      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "updateTemplateBasedHttpHeaders", "", "", "Exception - ", ex);
      return false;
    }
  }

  public String[] getTemplateStatusCode(String hostName, String serviceName, String URL, String templateName)
  {
    Log.debugLog(className, "getTemplateStatusCode", "", "", "Method started");
    try
    {
      String[] arrStatusCodeInfo = new String[2];
      String StatusCode = "";
      String Description = "";
      Vector completeControlFile = readFile(getServiceConfFilePath(hostName, serviceName), true);

      for(int yy = 0; yy < completeControlFile.size(); yy++)
      {
        String line = completeControlFile.elementAt(yy).toString().trim();

        if(line.startsWith("TEMPLATE_BASED_STATUS_CODE"))
        {
          String[] arrURLLine = rptUtilsBean.strToArrayData(line, " ");
          if(arrURLLine.length > 1)
          {
            if(arrURLLine[1].equals(templateName))
            {
              StatusCode = arrURLLine[2];
              if(arrURLLine.length > 3)
                Description = arrURLLine[3];
              if(arrURLLine.length > 4)
              {
                for(int i = 4; i < arrURLLine.length; i++)
                {
                  Description = Description + " " + arrURLLine[i];
                }
              }
              break;
            }
          }
        }
      }
      arrStatusCodeInfo[0] = StatusCode;
      arrStatusCodeInfo[1] = Description;
      return arrStatusCodeInfo;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getTemplateStatusCode", "", "", "Exception - ", ex);
      return new String[]{"", ""};
    }
  }

  public String[] getTemplateBasedHttpHeaders(String hostName, String serviceName, String URL, String templateName)
  {
    Log.debugLog(className, "getTemplateBasedHttpHeaders", "", "", "Method started");
    try
    {
      String[] arrHttpHeader = null;
      ArrayList list = new ArrayList();

      Vector completeControlFile = readFile(getServiceConfFilePath(hostName, serviceName), true);

      for(int yy = 0; yy < completeControlFile.size(); yy++)
      {
        String line = completeControlFile.elementAt(yy).toString().trim();

        if(line.startsWith("TEMPLATE_BASED_HEADER"))
        {
          String[] arrURLLine = rptUtilsBean.strToArrayData(line, " ");
          if(arrURLLine.length > 1)
          {
            if(arrURLLine[1].equals(templateName))
            {
              String headerLine = "";
              if(arrURLLine.length > 2)
                headerLine = arrURLLine[2];
              if(arrURLLine.length > 3)
              {
                for(int i = 3; i < arrURLLine.length; i++)
                {
                  headerLine = headerLine + " " + arrURLLine[i];
                }
              }
              list.add(headerLine);
            }
          }
        }
      }
      arrHttpHeader = new String[list.size()];
      for(int i = 0; i < list.size(); i++)
      {
        arrHttpHeader[i] = list.get(i).toString();
      }
      return arrHttpHeader;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getDecodeSettings", "", "", "Exception - ", ex);
      return new String[]{""};
    }
  }

  /*
   * Method to check whether the URL is for the specific type.
   * Right now we are doing for JDBC or Spring call.
   */
  public String getURLType(String URL)
  {
    Log.debugLog(className, "getURLType", "", "", "Method started");
    try
    {
      if(URL.toLowerCase().startsWith(JDBC_URL.toLowerCase()))
        return "JDBC";
      else if(URL.toLowerCase().startsWith(SPRING_URL.toLowerCase()))
        return "SPRING";
      else if(URL.toLowerCase().startsWith(JAVA_CLASS_URL.toLowerCase()))
        return "JAVACLASS";
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getURLType", "", "", "Exception - ", ex);
    }
    return "";
  }

  /*
   * Method to change all template types for a service
   * This method should be called when all templates are of same type
   */
  public boolean changeModeForService(String hostName, String serviceName, String newTemplateMode, StringBuffer errMsg)
  {
    Log.debugLog(className, "changeModeForService", "", "", "Method started");
    try
    {
      Vector completeControlFile = readFile(getServiceConfFilePath(hostName, serviceName), false);
      Vector modifiedVector = new Vector();
      String strSearch = "RESPONSE_TEMPLATE";

      for(int yy = 0; yy < completeControlFile.size(); yy++)
      {
        String line = completeControlFile.elementAt(yy).toString().trim();

        if(line.startsWith(strSearch)) // Method found
        {
          String[] arrURLLine = rptUtilsBean.strToArrayData(line, " ");
          String templateLine = "";
          for(int xx = 0; xx < arrURLLine.length; xx++)
          {
            if(xx == 2)
            {
              if(arrURLLine[xx].trim().equalsIgnoreCase("FileBased") || arrURLLine[xx].trim().equalsIgnoreCase("Forward") || arrURLLine[xx].trim().equalsIgnoreCase("RequestBased") || arrURLLine[xx].trim().equalsIgnoreCase("Simulate"))
              {
                if(arrURLLine[xx].trim().equalsIgnoreCase("Simulate"))
                {

                  if(!arrURLLine[xx + 1].trim().equals("NA"))
                  {
                    templateLine = templateLine + " " + newTemplateMode;
                  }
                  else
                  {
                    if(errMsg.toString().trim().equals(""))
                      errMsg.append(serviceName);
                    else
                      errMsg.append("," + serviceName);

                    templateLine = templateLine + " " + arrURLLine[xx];
                  }
                }
                else
                  templateLine = templateLine + " " + newTemplateMode;
              }
              else
              {
                templateLine = templateLine + " " + newTemplateMode;
                templateLine = templateLine + " " + arrURLLine[xx];
              }
            }
            else
            {
              if(templateLine.equals(""))
                templateLine = arrURLLine[xx];
              else
              {
                if(arrURLLine[xx].equals("\\n"))
                  templateLine = templateLine + " " + arrURLLine[xx] + "  ";
                else
                  templateLine = templateLine + " " + arrURLLine[xx];
              }
            }
          }
          modifiedVector.add(templateLine);
        }
        else
          modifiedVector.add(line);
      }
      if(!writeToFile(getServiceConfFilePath(hostName, serviceName), modifiedVector, hostName, serviceName))
        return false;
      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "changeModeForService", "", "", "Exception - ", ex);
      return false;
    }
  }

  /*
   * Method to get layout path.
   * Layout path will have the XMLs for layouts created for NetFunction
   */
  public String getLayoutPath(String hostName)
  {
    Log.debugLog(className, "getLayoutPath", "", "", "Method started");
    String layoutPath = "";
    try
    {
      layoutPath = getCorrelationPath() + hostName + "/layouts/";
      return layoutPath;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getLayoutPath", "", "", "Exception - ", ex);
      return layoutPath;
    }
  }

  public static void main(String[] args)
  {
    //This is called form migrate shell to convert template condition and condition logic to new format.
    CorrelationService cs = new CorrelationService();
    System.out.println("Start Migration to convert old response template Condition and Conditional Logic to new format ...");
    cs.MigrateTemplateConditionBasedOnService("default", "");
    cs.getAllParameters("default", "new_service1", "/home/net", 1, 0);
    System.out.println("Migration done ...");
   
  }
}

class CustomComparator implements Comparator
{
  public int compare(Object strArr1, Object strArr2)
  {
    String key1 = ((String[])strArr1)[1];
    String key2 = ((String[])strArr2)[1];
    return key1.compareToIgnoreCase(key2);
  }
}

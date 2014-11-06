/**
 * This class write a file 'replay_settings.dat' on path '$NS_WDIR/sys/replay/profiles/profileName' that have some keywords.
 * @author Sangeeta
 *
 */

package pac1.Bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Vector;

public class ReplaySetting 
{
  private String className = "ReplaySetting";
  final private String fileName = "replay_settings.dat";
  final private String fileDirPath = Config.getWorkPath() + "/replay_profiles/";
  private final String NSU_SHOW_PROFILES = "nsu_show_replay_profiles";
  private String userSession = "cookie";
  private String cookieName = "JSESSIONID";
  private String secondaryCookie = "";
  private String ConsiderResponseTime = "";
  private String ResponseTime = "";
  private String Unit = "";
  private String RandomizeTime = "";
  private String redirectDepth = "0";
  private String inlineObjectMode = "asperlog";
  private String defaultHost = "";
  private String ignoreUrlParam = "1";
  private String dateAndTime = "";
  private String requestLine = "";
  private String userAjent = "";
  private String requestCookie = "";
  private String setCookie = "";
  private String referer = "";
  private String contentType = "";
  private String location = "";
  private String client_ip = "";
  private String host_header = "";
  private String status_code = "";
  private String profileName = null;
  private String userName = null;
  private String ignore_urls = "";
  
  private String url = "";
  private String time = "";
  private String method = "";
  private String date = "";
  private String query_string = "";
  private String http_version = "";
  private String access_log_type = "APACHE";
  private String num_NVMs = "0";
  
  
  //--------------Keywords------------------
  final private String USER_SESSION = "USER_SESSION_KEY";
  final private String COOKIE_NAME = "COOKIE_NAME";
  final private String REDIRECT_DEPTH = "AUTO_REDIRECT";
  final private String INLINE_OBJECT_MODE = "INLINE_OBJECT_MODE";
  final private String DEFAULT_HOST = "DEFAULT_HOST";
  final private String IGNORE_URL_QUERY_PARAM = "IGNORE_URL_QUERY_PARAM";
  final private String SECONDARY_COOKIE = "SECONDARY_COOKIE";
  final private String CONSIDER_RESPONSE_TIME = "CONSIDER_RESPONSE_TIME";
  final private String AL_FIELD_RESPONSE_TIME = "AL_FIELD_RESPONSE_TIME";
  final private String AL_FIELD_RESPONSE_TIME_USEC = "AL_FIELD_RESPONSE_TIME_USEC";
  final private String SEC_TO_MSEC_CONVERSION_MODE  = "SEC_TO_MSEC_CONVERSION_MODE";
  
  final private String DATE_AND_TIME = "AL_FIELD_DATE_AND_TIME";
  final private String REQUEST_LINE = "AL_FIELD_REQUEST_LINE";
  final private String USER_AGENT = "AL_FIELD_USER_AGENT";
  final private String REQUEST_COOKIE = "AL_FIELD_REQUEST_COOKIE";
  final private String SET_COOKIE = "AL_FIELD_SET_COOKIE";
  final private String REFERER = "AL_FIELD_REFERER";
  final private String CONTENT_TYPE = "AL_FIELD_CONTENT_TYPE";
  final private String LOCATION = "AL_FIELD_LOCATION";
  final private String CLIENT_IP = "AL_FIELD_CLIENT_IP";
  final private String HOST_HEADER = "AL_FIELD_HOST_HEADER";
  final private String STATUS_CODE = "AL_FIELD_STATUS_CODE";
  
  //extra fields for replay as IIS logs.
  final private String DATE = "AL_FIELD_DATE";
  final private String TIME = "AL_FIELD_TIME";
  final private String METHOD = "AL_FIELD_METHOD";
  final private String URL = "AL_FIELD_URL";
  final private String QUERY_STRING = "AL_FIELD_QUERY_STRING";
  final private String HTTP_VERSION = "AL_FIELD_HTTP_VERSION";
  
  final private String ACCESS_LOG_TYPE = "ACCESS_LOG_TYPE";
  final private String NUM_NVM = "NUM_NVM";
  
  
  final private String IGNORE_URLS = "IGNORE_NON_SESSION_URLS";
  //---------------------------------------------
  
  private CmdExec cmdExec = new CmdExec();
  final private String profileDirPath = Config.getWorkPath() + "/replay_profiles/";
  
  public ReplaySetting(String arg1, String arg2)
  {
    profileName = arg1;
    userName = arg2;
  }
  
  public void setUserSession(String arg)
  {
    userSession = arg;
  }
  
  public String getUserSession()
  {
    return userSession;
  }
  
  public void setCookieName(String arg)
  {
    cookieName = arg;
  }
  
  public String getCookieName()
  {
    return cookieName;
  }
  
 public void setSecondaryCookie(String arg)
  {
    secondaryCookie = arg;
  }
  
  public String getSecondaryCookie()
  {
    return secondaryCookie;
  }

  public void setConsiderResponseTime(String arg)
  {
    ConsiderResponseTime = arg;
  }
  
  public String getConsiderResponseTime()
  {
    return ConsiderResponseTime;
  }

  public void setAllFieldResponseTime(String arg)
  {
    ResponseTime = arg;
  }
  
  public String getAllFieldResponseTime()
  {
    return ResponseTime;
  }

  public void setUnit(String arg)
  {
    Unit = arg;
  }
  
  public String getUnit()
  {
    return Unit;
  }

  public void setSecToMsecConversionMode(String arg)
  {
    RandomizeTime = arg;
  }
  
  public String getSecToMsecConversionMode()
  {
    return RandomizeTime;
  }

  public void setDefaultHost(String arg)
  {
    defaultHost = arg;
  }
  
  public String getDefaultHost()
  {
    return defaultHost;
  }
  
  public void setRedirectdepth(String arg)
  {
    redirectDepth = arg;
  }
  
  public String getRedirectDepth()
  {
    return redirectDepth;
  }
  
  public void setIgnoreUrlParam(String arg)
  {
    ignoreUrlParam = arg;
  }
  
  public String getIgnoreUrlParam()
  {
    return ignoreUrlParam;
  }
  
  public void setInlineObjectMode(String arg)
  {
    inlineObjectMode = arg;
  }
  
  public String getInlineObjectMode()
  {
    return inlineObjectMode;
  }
  
  public void setDateAndTime(String arg)
  {
    dateAndTime = arg;
  }
  
  public String getDateAndTime()
  {
    return dateAndTime;
  }
  
  public void setRequestLine(String arg)
  {
    requestLine = arg;
  }
  
  public String getRequestLine()
  {
    return requestLine;
  }
  
  public void setUserAjet(String arg)
  {
    userAjent = arg;
  }
  
  public String getUserAjent()
  {
    return userAjent;
  }
  
  public void setRequestCookie(String arg)
  {
    requestCookie = arg;
  }
  
  public void setIgnoreURL(String arg)
  {
    ignore_urls = arg;
  }
  
  public String getRequestCookie()
  {
    return requestCookie;
  }
  
  public void setSetCookie(String arg)
  {
    setCookie = arg;
  }
  
  public String getSetCookie()
  {
    return setCookie;
  }
  
  public String getIgnoreURL()
  {
    return ignore_urls;
  }
  
  public void setReferer(String arg)
  {
    referer = arg;
  }
  
  public String getReferer()
  {
    return referer;
  }
  
  public void setContentType(String arg)
  {
    contentType = arg;
  }
  
  public String getContentType()
  {
    return contentType;
  }
  
  public void setLocation(String arg)
  {
    location = arg;
  }
  
  public String getLocation()
  {
    return location;
  }
  
  public void setClientIP(String arg)
  {
    client_ip = arg;
  }
  
  public String getClientIP()
  {
    return client_ip;
  }
  
  public void setHostHeader(String arg)
  {
    host_header = arg;
  }
  
  public String getHostHeader()
  {
    return host_header;
  }
  
  public void setStatusCode(String arg)
  {
    status_code = arg;
  }
  
  public String getStatusCode()
  {
    return status_code;
  }
  
  public void setURL(String arg)
  {
    url = arg;
  }
  
  public String getURL()
  {
    return url;
  }
  
  //for time
  public void setTime(String arg)
  {
    time = arg;
  }
  
  public String getTime()
  {
    return time;
  }
  
  public void setMethod(String arg)
  {
    method = arg;
  }
  
  public String getMethod()
  {
    return method;
  }
  
  //for date
  public void setDate(String arg)
  {
    date = arg;
  }
  
  public String getDate()
  {
    return date;
  }
  
  //for query string  
  public void setQueryString(String arg)
  {
    query_string = arg;
  }
  
  public String getQueryString()
  {
    return query_string;
  }
   
  //for http version
  public void setHttpVersion(String arg)
  {
    http_version = arg;
  }
  
  public String getHttpVersion()
  {
    return http_version;
  }
  
  
  //for http version
  public void setAccessLogType(String arg)
  {
    access_log_type = arg;
  }
  
  public String getAccessLogType()
  {
    return access_log_type;
  }
    
  //for Num_NVMs 
  public void setNumNVM(String arg)
  {
    num_NVMs = arg;
  }
  
  public String getNumNVM()
  {
    return num_NVMs;
  }
    
  
  //getting all profiles of work.
  public ArrayList<String> getProfilesList(String args)
  {
    ArrayList<String> arrProfilesList = new ArrayList<String>();
    try
    {
      Log.debugLog(className, "getProfilesList", "", "", "method called"); 
      String arrData[][] = null;

      String strCmdName = NSU_SHOW_PROFILES; // command Name
      String strCmdArgs = args;
      
      strCmdArgs = " -A ";

      // System.out.println("strCmdArgs ==" + strCmdArgs);
      Vector vecCmdOutput = new Vector();

      boolean isQueryStatus = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, "netstorm", null);

      if (!isQueryStatus)
      {
        arrProfilesList.add("Error in Command Execution");
        return arrProfilesList;
      }
      arrData = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "-");
      for(int i = 1; i < arrData.length; i++)
      {
	//for(int j = 0; j < arrData[i].length; j++)
        arrProfilesList.add(arrData[i][0] + "/" + arrData[i][1] + "/"+arrData[i][2]); //add index of profiles.
      }
      return arrProfilesList;
    }
    catch(Exception e)
    {
      //e.printStackTrace();
      return arrProfilesList;
    }
  }
  
  public boolean checkProfileExist(String profileName)
  {
    try
    {
      if(profileName.trim().equals(""))
	return false;
      
      //System.out.println("path = "+profileDirPath+"/"+profileName);
      File profile = new File(profileDirPath+"/"+profileName);
      if(profile.exists())
	return true;
      else
        return false;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "checkProfileExist", "", "", "", e);
      return false;
    }
  }
  
  /**
   * saveReplaySetting method save all keywords to repaly_setting.dat file
   * @return StringBuilder
   */
  public StringBuilder saveReplaySettings()
  {
    Log.debugLog(className, "saveReplaySettings", "", "", "method Called");
    StringBuilder errMsg = new StringBuilder();
    try
    {
      String replaySettingfileName = fileDirPath + profileName + "/" + fileName;
      File file = new File(replaySettingfileName);
    
      if(file.exists())
      {
        Log.debugLog(className, "saveReplaySettings", "", "", "file replay_settings.dat already present for profile: "+profileName + ". deleting it.");
        file.delete();
      }
      
      File urlSettingDirFile = new File(fileDirPath + profileName);
      if(!urlSettingDirFile.exists())
      {
        Log.debugLog(className, "saveReplaySettings", "", "", "Profile dir for '" + profileName + "' doest exist. creating directory.");
        urlSettingDirFile.mkdirs();
      }
      
      
      if(access_log_type.trim().equals("APACHE"))
      {
	date = "";
        method = "";
        url = "";
        query_string = "";
        http_version = "";
        time = "";
      }
      else
      {
	dateAndTime = "";    
	requestLine = "";
	cookieName = "ASP.NET_SessionId";
      }
      
      StringBuilder fileData = new StringBuilder();
      if(!access_log_type.trim().equals(""))
	fileData.append(ACCESS_LOG_TYPE + " " + access_log_type + "\n");
      if(!userSession.trim().equals(""))
        fileData.append(USER_SESSION + " " + userSession + "\n");
      if(!cookieName.trim().equals(""))
        fileData.append(COOKIE_NAME + " " + cookieName + "\n");
	  if(!secondaryCookie.trim().equals(""))
		fileData.append(SECONDARY_COOKIE + " " + secondaryCookie + "\n");
	  if(!ConsiderResponseTime.trim().equals(""))
		fileData.append(CONSIDER_RESPONSE_TIME  + " " + ConsiderResponseTime + "\n");
	  if(!ResponseTime.trim().equals("") && Unit.trim().equals("0"))
		fileData.append(AL_FIELD_RESPONSE_TIME  + " " + ResponseTime + "\n");
	  if(!ResponseTime.trim().equals("") && Unit.trim().equals("1"))
		fileData.append(AL_FIELD_RESPONSE_TIME_USEC  + " " + ResponseTime + "\n");
	  if(!RandomizeTime.trim().equals(""))
        fileData.append(SEC_TO_MSEC_CONVERSION_MODE + " " + RandomizeTime + "\n");
      if(!ignore_urls.trim().equals(""))
	fileData.append(IGNORE_URLS + " " + ignore_urls + "\n");
      if(!redirectDepth.trim().equals(""))
        fileData.append(REDIRECT_DEPTH + " " + redirectDepth + "\n");
      if(!inlineObjectMode.trim().equals(""))
        fileData.append(INLINE_OBJECT_MODE + " " + inlineObjectMode + "\n");
      if(!defaultHost.trim().equals(""))
        fileData.append(DEFAULT_HOST + " " + defaultHost + "\n");
      if(!ignoreUrlParam.trim().equals(""))
        fileData.append(IGNORE_URL_QUERY_PARAM + " " + ignoreUrlParam + "\n");
      if(!dateAndTime.trim().equals(""))
        fileData.append(DATE_AND_TIME + " " + dateAndTime + "\n");
      if(!requestLine.trim().equals(""))
        fileData.append(REQUEST_LINE + " " + requestLine + "\n");
      if(!userAjent.trim().equals(""))
        fileData.append(USER_AGENT + " " + userAjent + "\n");
      if(!requestCookie.trim().equals(""))
        fileData.append(REQUEST_COOKIE + " " + requestCookie + "\n");
      if(!setCookie.trim().equals(""))
        fileData.append(SET_COOKIE + " " + setCookie + "\n");
      if(!referer.trim().equals(""))
        fileData.append(REFERER + " " + referer + "\n");
      if(!contentType.trim().equals(""))
        fileData.append(CONTENT_TYPE + " " + contentType + "\n");
      if(!location.trim().equals(""))
        fileData.append(LOCATION + " " + location + "\n");
      if(!client_ip.trim().equals(""))
	fileData.append(CLIENT_IP + " " + client_ip + "\n");
      if(!host_header.trim().equals(""))
	fileData.append(HOST_HEADER + " " + host_header + "\n");
      if(!status_code.trim().equals(""))
	fileData.append(STATUS_CODE + " " + status_code + "\n");
      if(!date.trim().equals(""))
	fileData.append(DATE + " " + date + "\n");
      if(!url.trim().equals(""))
	fileData.append(URL + " " + url + "\n");      
      if(!method.trim().equals(""))
	fileData.append(METHOD + " " + method + "\n");  
      if(!query_string.trim().equals(""))
	fileData.append(QUERY_STRING + " " + query_string + "\n");      
      if(!http_version.trim().equals(""))
	fileData.append(HTTP_VERSION + " " + http_version + "\n");
      if(!time.trim().equals(""))
	fileData.append(TIME + " " + time + "\n");
      if(!num_NVMs.trim().equals(""))
	fileData.append(NUM_NVM + " " + num_NVMs + "\n");      
      
      FileWriter fw = new FileWriter(file);
      fw.write(fileData.toString());
      fw.flush();
      fw.close();
      
      ChangeOwnerOfFile(replaySettingfileName, userName);
      ChangeOwnerOfFile(fileDirPath + profileName, userName);
      Log.debugLog(className, "saveReplaySettings", "", "", "replay settings saved successfully.");
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "saveReplaySettings", "", "", "", e);
      errMsg.append("Error in saving replay_setting.dat file. Please check error log.");
    }
    return errMsg;
  }
  
  
  public boolean createEmplyFiles(String filePath)
  {
    try
    {
      filePath = fileDirPath + "/" + filePath + "/";
      File file = new File(filePath);
      //System.out.println("filePath = "+filePath);
      if(!file.exists())
      {
	 file.mkdirs();  
	 Runtime r = Runtime.getRuntime();
	 String strCmd = "chown -R" + " " + "netstorm" + "." + "netstorm" + " " + fileDirPath;
	 r.exec(strCmd); 
      }

      File replaysetting = new File(filePath + "/replay_settings.dat");	
      File replayFilters = new File(filePath +"/replay_url_filter_patterns.dat");
      File replayPatterns = new File(filePath+"/replay_url_patterns.dat");
      
      if(!replaysetting.exists())
      {
	replaysetting.createNewFile();
	ChangeOwnerOfFile(filePath + "/replay_settings.dat", userName);
      }
      
      if(!replayFilters.exists())
      {
	replayFilters.createNewFile();
	ChangeOwnerOfFile(filePath +"/replay_url_filter_patterns.dat", userName);
      }
      
      if(!replayPatterns.exists())
      {
	replayPatterns.createNewFile();
	ChangeOwnerOfFile(filePath+"/replay_url_patterns.dat", userName);
      }
      
      return false;
      
    }
    catch(Exception e)
    {
      //e.printStackTrace();
      return false;
    }
  }
  //to delete the existing file 
  public boolean deleteExitsFile(String filePath){
	try
	{
		filePath = fileDirPath + "/" + filePath + "/";
		File file = new File(filePath);	
		if(file.exists())
		{
			File replaysetting = new File(filePath + "/replay_settings.dat");	
			File replayFilters = new File(filePath +"/replay_url_filter_patterns.dat");
			File replayPatterns = new File(filePath+"/replay_url_patterns.dat");
			if (replaysetting.exists())
			{
				replaysetting.delete();
			}
			if (replayFilters.exists())
			{
				replayFilters.delete();
			}
			if (replayPatterns.exists())
			{
				replayPatterns.delete();
			}
		}
		return false;
	}
	catch (Exception e)
	{
		//e.printStackTrace();
		return false;
	}
  }
  
  public boolean copyProfile(String profileToCopy, String owner, String copy_from, String copy_To)
  {
    try
    {     
      Log.debugLog(className, "profileToCopy", "", "", "method called"); 
      copy_from = fileDirPath + "/" + copy_from + "/";
      copy_To = fileDirPath + "/" + copy_To+"/";
      File newFile = new File(copy_To);
      if(!newFile.exists())
      {
	 newFile.mkdirs();
	 Runtime r = Runtime.getRuntime();
	 String strCmd = "chown -R" + " " + "netstorm" + "." + "netstorm" + " " + fileDirPath;
	 r.exec(strCmd); 
      }
      
      //System.out.println("profileToCopy = "+profileToCopy + "owner = "+owner + "copy_from = "+ copy_from + " copy_to ="+copy_To);
      Runtime r = Runtime.getRuntime();
      
      String strCmd = "cp -ar " + " " +copy_from +"/replay_settings.dat " + " " + copy_To;
      //System.out.println("strcmd = "+strCmd);
      Process copyfile1 = r.exec(strCmd);
      strCmd = "cp -ar " + " " +copy_from +"/replay_url_filter_patterns.dat " + " " + copy_To;
      Process copyfile2 = r.exec(strCmd);
      strCmd = "cp -ar " + " " +copy_from +"/replay_url_patterns.dat " + " " + copy_To;
      Process copyfile3 = r.exec(strCmd);
      
      int exitValue1 = copyfile1.waitFor();
      int exitValue2 = copyfile2.waitFor();
      int exitValue3 = copyfile3.waitFor();
      
      if (exitValue1 == 0 && exitValue2 == 0 && exitValue3 == 0)
        return true;
      else
        return false;

    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "profileToCopy", "", "", "", e);
      //e.printStackTrace();
      return false;
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
  
  /**
   * readReplaySetting function read all keywords form replay_setting.dat file and set in the class object setter methods.
   * @param profileName
   * @return StringBuilder
   */
  public StringBuilder readReplaySetting()
  {
    Log.debugLog(className, "readReplaySetting", "", "", "method called");
    StringBuilder errMsg = new StringBuilder();
    try
    {
      String replaySettingFile = fileDirPath + profileName + "/" + fileName;
      BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(replaySettingFile)));
      String strLine = null;
      while((strLine = br.readLine()) != null)
      {
        int idx = strLine.indexOf(" ");
        if(idx > 0 && strLine.length() > idx)
        {
          String key = strLine.substring(0, idx);
          String value = strLine.substring(idx+1, strLine.length()); 
          
          if(key.equals(USER_SESSION))
            this.setUserSession(value);
          else if(key.equals(COOKIE_NAME))
            this.setCookieName(value);
		  else if(key.equals(SECONDARY_COOKIE ))
			this.setSecondaryCookie(value);
		  else if(key.equals(CONSIDER_RESPONSE_TIME))
			this.setConsiderResponseTime(value);
		  else if(key.equals(AL_FIELD_RESPONSE_TIME))
		  {
			this.setAllFieldResponseTime(value);
			this.setUnit(0+"");
		  }
		  else if(key.equals(AL_FIELD_RESPONSE_TIME_USEC))
		  {
			this.setAllFieldResponseTime(value);
			this.setUnit(1+"");
		  }
		  else if(key.equals(SEC_TO_MSEC_CONVERSION_MODE ))
			this.setSecToMsecConversionMode(value);
          else if(key.equals(IGNORE_URLS))
            this.setIgnoreURL(value);
          else if(key.equals(DEFAULT_HOST))
            this.setDefaultHost(value);
          else if(key.equals(REDIRECT_DEPTH))
            this.setRedirectdepth(value);
          else if(key.equals(IGNORE_URL_QUERY_PARAM))
            this.setIgnoreUrlParam(value);
          else if(key.equals(INLINE_OBJECT_MODE))
            this.setInlineObjectMode(value);
          else if(key.equals(DATE_AND_TIME))
            this.setDateAndTime(value);
          else if(key.equals(REQUEST_LINE))
            this.setRequestLine(value);
          else if(key.equals(USER_AGENT))
            this.setUserAjet(value);
          else if(key.equals(REQUEST_COOKIE))
            this.setRequestCookie(value);
          else if(key.equals(SET_COOKIE))
            this.setSetCookie(value);
          else if(key.equals(REFERER))
            this.setReferer(value);
          else if(key.equals(CONTENT_TYPE))
            this.setContentType(value);
          else if(key.equals(LOCATION))
            this.setLocation(value);
          else if(key.equals(CLIENT_IP))
            this.setClientIP(value);          
          else if(key.equals(HOST_HEADER))
            this.setHostHeader(value);
          else if(key.equals(STATUS_CODE))
            this.setStatusCode(value);
          else if(key.equals(DATE))
           this.setDate(value);
          else if(key.equals(TIME))
           this.setTime(value);
          else if(key.equals(METHOD))
           this.setMethod(value);         
          else if(key.equals(URL))
           this.setURL(value);
          else if(key.equals(QUERY_STRING))
           this.setQueryString(value);
          else if(key.equals(HTTP_VERSION))
           this.setHttpVersion(value);          
          else if(key.equals(ACCESS_LOG_TYPE))
           this.setAccessLogType(value);      
          else if(key.equals(NUM_NVM))
            this.setNumNVM(value); 
        }
      }
      br.close();
    }
    catch(Exception e)
    {
      //e.printStackTrace();
      errMsg.append("Error in reading repaly_setting.dat for profile '" + profileName + "'. Please check error log.");
    }
    return errMsg;
  }
  
  /**
   * This function creates the copy of this profile to a new profile
   * @param newProfileName
   * @return void
   */
  public void saveAsReplaySetting(String newProfileName)
  {
    Log.debugLog(className, "saveAsReplaySetting", "", "", "method called");
    try
    {
      String temp = profileName;
      readReplaySetting();
      this.profileName = newProfileName;
      saveReplaySettings();
      this.profileName = temp;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "saveAsReplaySetting", "", "", "", e);
    }
  }
  
  public static void main(String arg[])
  {
    ReplaySetting rs = new ReplaySetting("abc", "");
    rs.setUserSession("Cookie");
    rs.setCookieName("JSESSIONID");
    rs.setRedirectdepth("13");
    rs.setInlineObjectMode("asperlog");
    rs.saveReplaySettings();
  }
}

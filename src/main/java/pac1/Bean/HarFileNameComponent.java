  /**
   * Inner class to store the components of name of HarFile
   *                                 0                 1         2                  3        4       5
   * New format of Har file:   P_index_html(600x400)+prof1+www.mycav.com-9001+2014-02-12+11-44-09+<UserID>.har
   * Old format of Har file:   P_index_html(600x400)+prof1+www.mycav.com-9001+2014-02-12+11-44-09.har
   *               0           1          2          3              4                5                        6
   * User id - child_idx, user_index, sess_inst, page_instance, group_num, GET_SESS_ID_BY_NAME(vptr), GET_PAGE_ID_BY_NAME(vptr)
   * @author Shruti
   *
   */
package pac1.Bean;

public class HarFileNameComponent
{
  //Class name
  private static String ClassName = "HarFileNameComponent";
  
  //Page file name
  private String pageFileName;
  
  //Parse page file name to check whether the format of HAR file is new or old
  private String[] arrParsePageFileName = null;
  
  //To change first character of domain name to upper character
  private String domainName = "";
  
  //Page name for HAR file
  private String pageNameWithHost = "";
  
  //Environment name
  private String envName = "NA";
  
  //Domain name
  private String hostName = "";
  
  //page Name
  private String pageName = "";
  
  //Screen Size
  private String screenSize = "-";
  
  //profile name
  private String profileName = "";
  
  //date
  private String date = "";
  private String time = "";
  
  //User Id
  private String userId = "";
  
  int debugLevel = 0;
  
  public HarFileNameComponent(int debugLevel, String pageFileName)
  {
    this.pageFileName = pageFileName;
    this.debugLevel = debugLevel;
    
    arrParsePageFileName = pageFileName.split("\\+");
    setHostName(); // host name and domain Name
    setPageNameWithHost(); //hostName_PageName
    setEnvName(); // profileName..hostName
    setUserID(); //user Id
    setDate(arrParsePageFileName[3]); //Date
    if(isValidHarFileName())
      setTime(arrParsePageFileName[4]);
    else
      setTime(arrParsePageFileName[4].substring(0, arrParsePageFileName[4].lastIndexOf("."))); //time
  }
  
  public String getPageFileName()
  {
    return pageFileName;
  }
  
  public String getProfileName()
  {
    return profileName;
  }

  public void setProfileName(String profileName)
  {
    this.profileName = profileName;
  }

  public String getDate()
  {
    return date;
  }

  public void setDate(String date)
  {
    this.date = date;
  }

  public String getTime()
  {
    return time;
  }

  public void setTime(String time)
  {
    this.time = time;
  }

  public boolean isValidHarFileName()
  {
    if(arrParsePageFileName.length > 5)
      return true;
    
    return false;
  }
  
  public String getHostName()
  {
    return hostName;
  }
  
  public String getDomainName()
  {
    return domainName;
  }
  
  /**
   * To find the domain name
   * @return
   */
  public void setHostName()
  {
    hostName = arrParsePageFileName[2].toString();
    
    //Execute the block if hostname does not contain ip address
    if(!"1234567890".contains(hostName.charAt(0) + ""))
    {
      String[] splitDomainName = hostName.split("\\.");
      
      if(debugLevel > 0)
        Log.debugLog(ClassName, "getHostName", "", "", "hostName = " +  hostName + ", splitDomainName. = " + splitDomainName.length);
      
      if(splitDomainName.length > 1)  
        hostName = splitDomainName[1];
      
    }
    
    //The first character is changed to upper case
    char[] stringArray = hostName.toCharArray();
    stringArray[0] = Character.toUpperCase(stringArray[0]);
    domainName = new String(stringArray);
    
    if(debugLevel > 0)
      Log.debugLog(ClassName, "getDomainName", "", "", "domainName. = " + domainName);
  }
  
  public void setPageNameWithHost()
  {
    //pageNameForHarFile = pageFileName.substring(2, pageFileName.indexOf("+"));
    pageNameWithHost = arrParsePageFileName[0].substring(2);
    
    int openBIndex = pageNameWithHost.indexOf("(");
    int openCIndex = pageNameWithHost.indexOf(")");
    
    if(openBIndex == -1)
    {
      pageName = pageNameWithHost;
    }
    else
    {
      pageName = pageNameWithHost.substring(0, openBIndex);
      screenSize = pageNameWithHost.substring(openBIndex + 1, openCIndex);
    }
    
    pageNameWithHost = domainName + "_" + pageNameWithHost;
    
    if(debugLevel > 0)
      Log.debugLog(ClassName, "getPageNameForHarFile", "", "", "pageNameWithHost. = " + pageNameWithHost);
  }
  
  public String getPageNameWithHost()
  {
    return pageNameWithHost;
  }
  
  public String getEnvName()
  {
    return envName;
  }
  
  public void setEnvName()
  {
    envName = arrParsePageFileName[1] + "." + arrParsePageFileName[2];
    
    setProfileName(arrParsePageFileName[1]);
    
    if(debugLevel > 0)
      Log.debugLog(ClassName, "getEnvName", "", "", "envName. = " + envName);
  }
  
  
  public String getScreenSize()
  {
    return screenSize;
  }

  
  public String getPageName()
  {
    return pageName;
  }

  public void setUserID()
  {
    if(isValidHarFileName())
      userId = arrParsePageFileName[5].substring(0, arrParsePageFileName[5].lastIndexOf("."));
  }
  
  public String getUserId()
  {
    return userId;
  }
  
  public static void main(String args[])
  {
    HarFileNameComponent component = new HarFileNameComponent(0, "P_PersonalInfo_RepeatView+Wal_demo_8+m-perf.walgreens.com+2014-07-18+06-43-37.har");
    System.out.println("Pagen Name = " + component.getPageName());
    System.out.println("Host Name = " + component.getHostName());
    System.out.println("Screen Name = " + component.getScreenSize());
    System.out.println("Date = " + component.getDate());
    System.out.println("Time = " + component.getTime());
    System.out.println("Profile Name = " + component.getProfileName());
    System.out.println("Page File NAmw = " + component.getPageFileName());
    System.out.println("Domain Name = " + component.getDomainName());
    System.out.println("Host Pagn Name = " + component.getPageNameWithHost());
    System.out.println("Env Name = " + component.getEnvName());
    System.out.println("User Id = " + component.getUserId());
  }
}

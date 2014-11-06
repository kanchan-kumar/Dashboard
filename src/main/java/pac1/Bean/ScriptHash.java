package pac1.Bean;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.StringTokenizer;

/*
 Post with body in embedded URL (detail file)
---- TX_RAT:0 RX_RAT:0
POST /bharosauio/flashFingerprint.do? HTTP/1.1^M
Host: 98.207.110.84:9090^M
Accept-Language: en-us,en;q=0.5^M
Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7^M
Cookie: vsc=3359_4ba533aefb5a5aa2e2cdf933062b451e; isCookieEnabled=checkIt; JSESSIONID=BE9107E4A35E796BB12FDFBA6393918E; BUGLIST=232%3A233; VERSION-NetStorm=3.0; LASTORDER=bugs.bug_status%2C%20bugs.priority%2C%20map_assigned_to.login_name%2C%20bugs.bug_id^M
Content-Type: application/x-www-form-urlencoded^M
Content-Length: 426^M
^M
id=1216019714585%2E26&fp=A%3Dt%26SA%3Dt%26SV%3Dt%26EV%3Dt%26MP3%3Dt%26AE%3Dt%26VE%3Dt%26ACC%3Df%26PR%3Dt%26SP%3Dt%26SB%3Df%26DEB%3Df%26V%3DLNX%25209%252C0%252C115%252C0%26M%3DAdobe%2520Linux%26R%3D1280x1024%26DP%3D98%26COL%3Dcolor%26AR%3D0%2E941265%26OS%3DLinux%25202%2E6%2E17%2DFC4%5FCAV6%26L%3Den%26PT%3DPlugIn%26AVD%3Df%26LFD%3Df%26WD%3Dt%26TLS%3Df&v=3360%5F999a0def8a3e220ed11dd0f21a6592ae%0D%0A%0D%0A&client=vfc&action=fp

*/

/*
 *       case registerQuestions_do_3:
        think_time = pre_page_registerQuestions_do_3();
        web_url (registerQuestions_do_3,
          METHOD=POST,
          URL=http://98.207.110.84:9090/bharosauio/registerQuestions.do,
          HEADER=Accept-Language: en-us,en;q=0.5,
          HEADER=Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7,
          HEADER=Content-Type: application/x-www-form-urlencoded,
          HEADER=Content-Length: 191,
          BODY=showView=saveQuestions&numQuestions=3&question0=2&question1=116&question2=179&Bharosa_Register_Pad0DataField=answer&Bharosa_Register_Pad1DataField=answer&Bharosa_Register_Pad2DataField=answer,
          NUM_EMBED=0);
        next_page = check_page_registerQuestions_do_3();
        break;
 */

public class ScriptHash
{
  private String className = "ScriptHash";
  private int MAX_PAGE_BASE_NAME = 28;
  public HashMap map = new HashMap();//This field changed to non-static and it is not syncronized:Atul

  //This will not contain the hostName
  private String path = "";
  private String hostName = "";
  private String rootPath = "";

  public ScriptHash()
  {}

  public String getUniqPageName(String page)
  {
    Log.debugLog(className, "getUniqPageName", "", "", "Method called. page = " + page);
    if(map.size() == 0)
    {
      Log.debugLog(className, "getUniqPageName", "", "", "hashMap size is zero");
      map.put(page, "1");
    }
    else
    {
      if(map.containsKey(page))
      {
        Log.debugLog(className, "getUniqPageName", "", "", "URL already exist in the hash map. Page = " + page);
        int num = Integer.parseInt(map.get(page).toString());
        num++;
        //this condition checks existence of unique page names : Saloni
        while(true)
        {
          Log.debugLog(className, "getUniqPageName", "", "", "URL already exist in the hash map. Page = " + page + "_" + num);
          if(map.containsKey(page + "_" + num))
            num = num + 1;
          else
            break;
        }
        map.put(page, num + "");
        page = page + "_" + num;
        Log.debugLog(className, "getUniqPageName", "", "", "Setting the name of the page = " + page + " and freq of the page = " + num);
      }
      else
      {
        Log.debugLog(className, "getUniqPageName", "", "", "the is get first time. page = " + page);
        map.put(page, "1");
      }
    }
    Log.debugLog(className, "getUniqPageName", "", "", "returning the page = " + page);
    return page;
  }

  // Input 
  //    srcURL - URL without protocol and host (e.g. / or checkmail.html?x=y&a=b
  public String getPageFromURL(String srcURL)
  {
    Log.debugLog(className, "getPageFromURL", "", "", "Method Started. URL = " + srcURL);
    URL urlObj = null;
    int port = -1;
    try
    {
      urlObj = new URL(srcURL);
    }
    catch(MalformedURLException e)
    {
      Log.stackTraceLog(className, "getPageFromURL", "", "", "Exception in creating URL", e);
    }
    hostName = urlObj.getHost();
    port = urlObj.getPort();
    if(port != -1)
      hostName = hostName + ":" + port;

    /**************
     * This will return the file name for the below example like below
     * 1)url = http://mail.google.com/mail/?hl=etc
     *   then file name will be
     *   urlFileNameWithPath = /mail/?hl=etc
     *   
     * 2)url = http://www.google.com/accounts/ServiceLogin?service=login etc..
     *   then file name will be
     *   urlFileNameWithPath = /accounts/ServiceLogin?service=login etc..
     *   
     * 3)url = http://www.google.com/mail.html
     *   then file name will be
     *   urlFileNameWithPath = /mail.html
     *   
     * 4)url = http:/www.google.com/
     *   then file name will be
     *   urlFileNameWithPath = /www.google.com/
     **************/

    String urlFileNameWithPath = urlObj.getFile();
    if(urlFileNameWithPath == null)
    {
      Log.errorLog(className, "getPageFromURL", "", "", "urlFileNameWithPath is comming null from the url");
    }

    Log.debugLog(className, "getPageFromURL", "", "", "Url file name with path  = " + urlFileNameWithPath + " from the url = " + srcURL);

    String urlFileName = "";
    String pageName = "";
    if((urlFileNameWithPath.equals("/")) || (urlFileNameWithPath.equals("")))
    {
      Log.debugLog(className, "getPageFromURL", "", "", "urlFileNameWithPath is / Or empty");
      pageName = "index";
      path = "/";
      Log.debugLog(className, "getPageFromURL", "", "", "setting the page name = " + pageName + " and path = " + path);
    }
    else
    {
      int index = urlFileNameWithPath.lastIndexOf("/");
      if(index == -1)
        Log.debugLog(className, "getPageFromURL", "", "", "character / can not be found in the url = " + urlFileNameWithPath);
      urlFileName = urlFileNameWithPath.substring(index + 1, urlFileNameWithPath.length());
      Log.debugLog(className, "getPageFromURL", "", "", "index of / = " + index + " and length = " + urlFileNameWithPath.length());
      Log.debugLog(className, "getPageFromURL", "", "", "setting the urlFileName = " + urlFileName);

      if(urlFileName.equals(""))
      {
        Log.debugLog(className, "getPageFromURL", "", "", "no string after the /.pageName = " + pageName);
        pageName = "index";
        //path = "/";
        path = urlFileNameWithPath.substring(0, (index + 1));
        Log.debugLog(className, "getPageFromURL", "", "", "setting the page name = " + pageName + " and path = " + path);
      }
      else if((urlFileName.startsWith("?")) || (urlFileName.startsWith("#")))
      {
        Log.debugLog(className, "getPageFromURL", "", "", "url starting wilh ? Or #. url = " + urlFileName);
        pageName = "index";
        path = urlFileNameWithPath.substring(0, (index + 1));//one is added to include / itself
        Log.debugLog(className, "getPageFromURL", "", "", "setting the pageName = " + pageName + " and path = " + path);
      }
      else
      {
        StringTokenizer strTkn = new StringTokenizer(urlFileName, "?#");
        pageName = strTkn.nextToken();
        path = urlFileNameWithPath.substring(0, (index + 1));//one is added to include / itself
        Log.debugLog(className, "getPageFromURL", "", "", "token = " + pageName + " from the string = " + urlFileNameWithPath);
        Log.debugLog(className, "getPageFromURL", "", "", "setting the page name = " + pageName + " and path = " + path);
      }
    }
    if(pageName.length() > MAX_PAGE_BASE_NAME)
    {
      Log.debugLog(className, "getPageFromURL", "", "", "url length is more than MAX_PAGE_BASE_NAME. MAX_PAGE_BASE_NAME = " + MAX_PAGE_BASE_NAME);
      pageName = pageName.substring(0, MAX_PAGE_BASE_NAME);
    }

    int len = pageName.length();
    Log.debugLog(className, "getPageFromURL", "", "", "length of pageName = " + pageName + " is = " + len);
    String strTemp = new String();
    Log.debugLog(className, "getPageFromURL", "", "", "The page name before parsing each charater is = " + pageName);
    for(int i = 0; i < len; i++)
    {
      if((!Character.isLetterOrDigit(pageName.charAt(i))) && (pageName.charAt(i) != '_'))
        strTemp = strTemp + "_";
      else
        strTemp = strTemp + pageName.charAt(i);
    }

    pageName = strTemp;
    Log.debugLog(className, "getPageFromURL", "", "", "The page name after parsing each charater is = " + pageName);
    if(!Character.isLetter(pageName.charAt(0)))
      pageName = "X" + pageName.substring(1, pageName.length());
    setRootPath(path);
    return getUniqPageName(pageName);
  }

  //This will set the rootPath to be used in the <Script Name>/dump/index file
  //Presently it is not used because when redirection is done on the main url, then we need to get the root path 
  //as per the redirected page not for the main url, while this is set only for the main url.
  public void setRootPath(String path)
  {
    rootPath = "./";
    if(!path.equals("/"))
    {
      int length = path.length();
      for(int i = 1; i < length; i++)
      {
        if(path.charAt(i) == '/')
          rootPath = rootPath + "../";
      }
    }
  }

  //This method was previously used for the entry in the index file, but now it is not used because this path also include the "," character.
  //No parsing is done for the path in this method, now path is getting from the ScriptRecorder's parseFileNameAndPath().
  public String getPath()
  {
    return path;
  }

  public String getHostName()
  {
    return hostName;
  }

  public String getRootPath()
  {
    return rootPath;
  }
}

/**----------------------------------------------------------------------------
 * Name       ScriptUrlInfo.java
 * Purpose    This is to generate url_info.list in recorder directory
 * @author    Saloni Tyagi
 * Modification History
 *---------------------------------------------------------------------------**/
package pac1.Bean;

import java.io.Serializable;

public class ScriptUrlInfo implements Serializable
{
  private String pageName = "NA";
  private boolean isMainUrl;
  private String url = "NA";
  private int redirectionDepth = 0;
  private String dumpPath = "NA"; //dump file name
  private StringBuffer dumpContent = new StringBuffer();
  private String ContentType = "NA";
  private int inlineUrlId = -1;
  private int pageIndex = -1;
  private String redirectUrl = "NA";
  private String urlFilePath = "NA";
  private String host = "";
  private String filePath = "NA";
  private String rootPath = "NA";
  private String statusCode = "0";
  private String time = "0"; //in second
  private String redirectLocation = "NA";
  private int bytesRecieved = 0;
  private int bodySize = -1;
  private String ScriptURL = "";
  private String method = "GET";
  private String future1 = "NA";
  private String recordedUrl = "NA";
  private String future2 = "NA";
  private String future3 = "NA";
  private String future4 = "NA";
  private String future5 = "NA";
  private String future6 = "NA";
  private String snapShotBeforeAction = "NA";
  private String snapShotAfterAction = "NA";
  private String snapShotClickAction = "NA";
  private String comments = "NA";
  private String clickAPIName = "NA";
 
 /* public String getScriptURL() 
  {
	return ScriptURL;
  }

  public void setScriptURL(String scriptURL) 
  {
	ScriptURL = scriptURL;
  }*/
  
 /* public String getRecordedUrl()
  {
		return recordedUrl;
  }

  public void setRecordedUrl(String recordedUrl)
  {
	 this.recordedUrl = recordedUrl;
  }
*/
   
  public String getRecordedUrl()
  {
		return recordedUrl;
  }

  public void setRecordedUrl(String recordedUrl)
  {
	 this.recordedUrl = recordedUrl;
  }

  public String getPageName()
  {
    return pageName;
  }

  public void setPageName(String pageName)
  {
    this.pageName = pageName;
  }

  public boolean getUrlType()
  {
    return isMainUrl;
  }

  public void setUrlType(boolean isMainUrl)
  {
    this.isMainUrl = isMainUrl;
  }

  public String getUrl()
  {
    return url;
  }

  public void setUrl(String url)
  {
    //In url_info.list file pipe was coming so replacing pipe by underscore
    this.url = url.replace('|', '_');
  }

  public int getRedirectionDepth()
  {
    return redirectionDepth;
  }

  public void setRedirectionDepth(int redirectionDepth)
  {
    this.redirectionDepth = redirectionDepth;
  }

  public String getDumpPath()
  {
    return dumpPath;
  }

  public void setDumpPath(String dumpPath)
  {
    this.dumpPath = dumpPath;
  }

  public StringBuffer getDumpFileContents()
  {
    return dumpContent;
  }

  public void setDumpFileContents(StringBuffer dumpContents)
  {
    this.dumpContent = dumpContents;
  }

  public int getPageIndex()
  {
    return pageIndex;
  }

  public void setPageIndex(int pageIndex)
  {
    this.pageIndex = pageIndex;
  }

  public String getHost()
  {
    return host;
  }

  public void setHost(String host)
  {
    this.host = host;
  }

  public String getFilePath()
  {
    return filePath;
  }

  public void setFilePath(String filePath)
  {
    this.filePath = filePath;
  }

  public String getRootPath()
  {
    return rootPath;
  }

  public void setRootPath(String rootPath)
  {
    this.rootPath = rootPath;
  }

  public String getStatusCode()
  {
    return statusCode;
  }

  public void setStatusCode(String statusCode)
  {
    this.statusCode = statusCode;
  }

  public String getTime()
  {
    return time;
  }

  public void setTime(String time)
  {
    this.time = time;
  }

  public String getRedirectLocation()
  {
    return redirectLocation;
  }

  public void setRedirectLocation(String redirectLocation)
  {
    this.redirectLocation = redirectLocation;
  }

  public int getBytesRecieved()
  {
    return bytesRecieved;
  }

  public void setBytesRecieved(int bytesRecieved)
  {
    this.bytesRecieved = bytesRecieved;
  }

  public String getMethod()
  {
    return method;
  }

  public void setMethod(String method)
  {
    this.method = method;
  }

  public String getFuture1()
  {
    return future1;
  }

  public void setFuture1(String future1)
  {
    this.future1 = future1;
  }

  public String getFuture2()
  {
    return future2;
  }

  public void setFuture2(String future2)
  {
    this.future2 = future2;
  }

  public String getFuture3()
  {
    return future3;
  }

  public void setFuture3(String future3)
  {
    this.future3 = future3;
  }

  public String getFuture4()
  {
    return future4;
  }

  public void setFuture4(String future4)
  {
    this.future4 = future4;
  }

  public String getFuture5()
  {
    return future5;
  }

  public void setFuture5(String future5)
  {
    this.future5 = future5;
  }

  public String getFuture6()
  {
    return future6;
  }

  public void setFuture6(String future6)
  {
    this.future6 = future6;
  }
  
  public String getSnapShotClickAction()
  {
    return snapShotClickAction;
  }

  public void setSnapShotClickAction(String snapShotClickAction)
  {
    this.snapShotClickAction = snapShotClickAction;
  }

  public String getSnapShotBeforAction()
  {
    return snapShotBeforeAction;
  }

  public void setSnapShotBeforAction(String snapShotBeforeAction)
  {
    this.snapShotBeforeAction = snapShotBeforeAction;
  }

  public String getSnapShotAfterAction()
  {
    return snapShotAfterAction;
  }

  public void setSnapShotAfterAction(String snapShotAfterAction)
  {
    this.snapShotAfterAction = snapShotAfterAction;
  }

  public String getComments()
  {
    return comments;
  }

  public void setComments(String comments)
  {
    this.comments = comments;
  }

  public String getContentType()
  {
    return ContentType;
  }

  public void setContentType(String contentType)
  {
    ContentType = contentType;
  }

  public int getInlineUrlId()
  {
    return inlineUrlId;
  }

  public void setInlineUrlId(int inlineUrlId)
  {
    this.inlineUrlId = inlineUrlId;
  }

  public String getRedirectUrl()
  {
    return redirectUrl;
  }

  public void setRedirectUrl(String redirectUrl)
  {
    this.redirectUrl = redirectUrl;
  }

  public String getUrlFilePath()
  {
    return urlFilePath;
  }

  public void setUrlFilePath(String urlFilePath)
  {
    this.urlFilePath = urlFilePath;
  }

  public int getBodySize()
  {
    return bodySize;
  }

  public void setBodySize(int bodySize)
  {
    this.bodySize = bodySize;
  }

  public String getClickAPIName()
  {
    return clickAPIName;
  }

  public void setClickAPIName(String clickAPIName)
  {
    this.clickAPIName = clickAPIName;
  }  
  
  public String toString(int i)
  {
    //* is optional, we have removed it 
    //SeqNum*|pageName|urlType|inlineUrlId|Url|dumpFileName|ContentTpe|redirectionDepth|redirectUrl|UrlHost|urlFilePath|rootPath|statusCode|time(Sec.)|bytesRecieved|bodySize|method|future1|future2|future3|future4|future5|future6|future7|future8|future9|future10|comments
    return(pageName + "|" + (isMainUrl ? "Main" : "Inline") + "|" + inlineUrlId + "|" + url + "|" + urlFilePath + "|" + ContentType + "|" + redirectionDepth + "|" + redirectUrl + "|" + host + "|" + dumpPath + "|" + rootPath + "|" + statusCode + "|" + time + "|" + bytesRecieved + "|" + bodySize + "|" + method + "|" + snapShotBeforeAction + "|" + snapShotAfterAction + "|" + snapShotClickAction + "|" + clickAPIName + "|" + future1 + "|" + future2 + "|" + future3 + "|" + future4 + "|" + future5 + "|" + future6 + "|" + comments);

    //return(i + "|" + pageName + "|" + (isMainUrl ? "Main" : "Embded") + "|" + url + "|" + redirectionDepth + "|" + dumpPath);
  }
  
}

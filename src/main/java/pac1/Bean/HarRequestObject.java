package pac1.Bean;

import java.util.Comparator;

public class HarRequestObject
{
  private String URL = "";
  private String fileName = "";
  private String hostName = "";
  
  private String contentType = "";
  private long startOffSet = 0;
  
  //timing tag
  private long reponseTime = 0; //reponse time
  private long dnsLookUp = 0; //DNS
  private long initialConnection = 0; //connect
  private long timeToFirstByte = 0; //send - receive
  private long contentDownload = 0; // receive
  private long byteDownLoad = 0; //body size
  private long Wait = 0; //wait
  private long blocked = 0; //blocked
  private long totalTime = 0; //Send + Wait + Receive
  
  private long fileSize = 0;
  
  private int statusCode = 0;
  private String statusText = "";
  private String IPAddress = "-";
  private String location = "Origin";
  private String state = "";
  private int cache = 0;
  
  private long startTime = 0;
  private long endTime = 0;
  
  private String httpVersion = "HTTP/1.1";
  private String connection = "-";
  private String proxyConnection = "-";
  private String acceptEncoding = "-";

private int sequenceNumber = 0;
  
  private long responseTime = 0;
  
  public String getURL() 
  {
    return URL;
  }

  public void setURL(String uRL)
  {
    URL = uRL;
  }
  
  public String getFileName() 
  {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getHostName() 
  {
    return hostName;
  }

  public void setHostName(String hostName)
  {
    this.hostName = hostName;
  }

  public String getContentType() 
  {
    return contentType;
  }

  public void setContentType(String contentType)
  {
    this.contentType = contentType;
  }
  
  public long getReponseTime() 
  {
    return reponseTime;
  }

  public void setReponseTime(long reponseTime) 
  {
    this.reponseTime = reponseTime;
  }

  public long getStartOffSet()
  {
    return startOffSet;
  }

  public void setStartOffSet(long startOffSet)
  {
    this.startOffSet = startOffSet;
  }

  public long getDnsLookUp()
  {
    return dnsLookUp;
  }

  public void setDnsLookUp(long dnsLookUp)
  {
    this.dnsLookUp = dnsLookUp;
  }

  public long getInitialConnection()
  {
    return initialConnection;
  }

  public void setInitialConnection(long initialConnection)
  {
    this.initialConnection = initialConnection;
  }

  public long getTimeToFirstByte()
  {
    return timeToFirstByte;
  }

  public void setTimeToFirstByte(long timeToFirstByte)
  {
    this.timeToFirstByte = timeToFirstByte;
  }

  public long getContentDownload() 
  {
    return contentDownload;
  }

  public void setContentDownload(long contentDownload) 
  {
    this.contentDownload = contentDownload;
  }

  public long getByteDownLoad() 
  {
    return byteDownLoad;
  }

  public void setByteDownLoad(long byteDownLoad) 
  {
    this.byteDownLoad = byteDownLoad;
  }

  public long getWait() 
  {
    return Wait;
  }

  public void setWait(long wait) 
  {
    Wait = wait;
  }

  public long getBlocked() {
    return blocked;
  }

  public void setBlocked(long blocked) {
    this.blocked = blocked;
  }

  public int getStatusCode() 
  {
    return statusCode;
  }

  public void setStatusCode(int statusCode)
  {
    this.statusCode = statusCode;
  }

  public String getStatusText()
  {
    return statusText;
  }

  public void setStatusText(String statusText)
  {
    this.statusText = statusText;
  }

  public String getIPAddress() 
  {
    return IPAddress;
  }

  public void setIPAddress(String iPAddress) 
  {
    IPAddress = iPAddress;
  }

  public String getLocation()
  {
    return location;
  }

  public void setLocation(String location)
  {
    this.location = location;
  }

  public int getCache()
  {
    return cache;
  }

  public void setCache(int cache) 
  {
    this.cache = cache;
  }
  
  public String getState() 
  {
    return state;
  }

  public void setState(String state) 
  {
    this.state = state;
  }

  public long getStartTime()
  {
    return startTime;
  }

  public void setStartTime(long startTime) 
  {
    this.startTime = startTime;
  }

  public long getEndTime()
  {
    return endTime;
  }

  public void setEndTime(long endTime)
  {
    this.endTime = endTime;
  }

  public long getTotalTime()
  {
    return totalTime;
  }

  public void setTotalTime(long totalTime)
  {
    this.totalTime = totalTime;
  }

  public String getHttpVersion()
  {
    return httpVersion;
  }

  public void setHttpVersion(String httpVersion)
  {
    this.httpVersion = httpVersion;
  }

  public String getConnection()
  {
    return connection;
  }

  public void setConnection(String connection)
  {
    this.connection = connection;
  }

  public String getProxyConnection()
  {
    return proxyConnection;
  }

  public void setProxyConnection(String proxyConnection) 
  {
    this.proxyConnection = proxyConnection;
  }

  public String getAcceptEncoding() 
  {
    return acceptEncoding;
  }

  public void setAcceptEncoding(String acceptEncoding)
  {
    this.acceptEncoding = acceptEncoding;
  }

  public long getFileSize() 
  {
    return fileSize;
  }

  public void setFileSize(long fileSize) 
  {
    this.fileSize = fileSize;
  }

  public long getResponseTime()
  {
    return responseTime;
  }

  public void setResponseTime(long responseTime)
  {
    this.responseTime = responseTime;
  }

  public int getSequenceNumber()
  {
    return sequenceNumber;
  }

  public void setSequenceNumber(int sequenceNumber)
  {
    this.sequenceNumber = sequenceNumber;
  }


  /**
   * Comparator to sort HarRequestObject list or array in order of dns
   */
  public static Comparator<HarRequestObject> dnsComparator = new Comparator<HarRequestObject>()
  {
      //@Override
      public int compare(HarRequestObject e1, HarRequestObject e2) {
          return (int) (e2.getDnsLookUp() - e1.getDnsLookUp());
      }
  };

  /**
   * Comparator to sort HarRequestObject list or array in order of dns
   */
  public static Comparator<HarRequestObject> waitComparator = new Comparator<HarRequestObject>()
  {
      //@Override
      public int compare(HarRequestObject e1, HarRequestObject e2) {
          return (int) (e2.getWait() - e1.getWait());
      }
  };
  
  /**
   * Comparator to sort HarRequestObject list or array in order of dns
   */
  public static Comparator<HarRequestObject> connectComparator = new Comparator<HarRequestObject>()
  {
      //@Override
      public int compare(HarRequestObject e1, HarRequestObject e2) {
          return (int) (e2.getInitialConnection() - e1.getInitialConnection());
      }
  };  
  

  /**
   * Comparator to sort HarRequestObject list or array in order of dns
   */
  public static Comparator<HarRequestObject> blockedComparator = new Comparator<HarRequestObject>()
  {
      //@Override
      public int compare(HarRequestObject e1, HarRequestObject e2) {
          return (int) (e2.getBlocked() - e1.getBlocked());
      }
  };
  
  /**
   * Comparator to sort HarRequestObject list or array in order of dns
   */
  public static Comparator<HarRequestObject> loadComparator = new Comparator<HarRequestObject>()
  {
      //@Override
      public int compare(HarRequestObject e1, HarRequestObject e2) {
          return (int) (e2.getContentDownload() - e1.getContentDownload());
      }
  };

  public static Comparator<HarRequestObject> totalTimeComparator = new Comparator<HarRequestObject>()
  {
       //@Override
      public int compare(HarRequestObject e1, HarRequestObject e2) {
          return (int) (e2.getTotalTime() - e1.getTotalTime());
      }
  };  
}

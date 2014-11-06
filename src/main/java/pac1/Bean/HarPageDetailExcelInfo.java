package pac1.Bean;


public class HarPageDetailExcelInfo
{
  /**Fields for excel sheet**/
  //Variance to Mean Ratio for DOM
  private String vmrDOM = "";
  //Variance to Mean Ratio for OnLoad
  private String vmrOnload = "";
  //Variance to Mean Ratio for Requests
  private String vmrReq = "";
  //Variance to Mean Ratio for Bytes received
  private String vmrByteRec = "";
  //Variance to Mean Ratio for Bytes sent
  private String vmrByteSent = "";
  //Variance to Mean Ratio for Page load
  private String vmrPageLoad = "";
  
  //Standard deviation for DOM
  private String stdDevDOM = "";
  //Standard deviation for OnLoad
  private String stdDevOnload = "";
  //Standard deviation for Requests
  private String stdDevReq = "";
  //Standard deviation for Bytes received
  private String stdDevByteRec = "";
  //Standard deviation for Bytes sent
  private String stdDevByteSent = "";
  //Standard deviation for Page load
  private String stdDevPageLoad = "";
  
  //Median for DOM
  private String medianDOM = "";
  //Median for OnLoad
  private String medianOnLoad = "";
  //Median for Page load
  private String medianPageLoad = "";
  //Median for Data received
  private String medianByteRec = "";
  //Median for Data Sent
  private String medianByteSent = "";
  //Median for Avg Request
  private String medianAvgReq = "";
  
  //90th percentile for DOM
  private String ninetyPercDOM = "";
  //90th percentile for OnLoad
  private String ninetyPercOnLoad = "";
  //90th percentile for Page load
  private String ninetyPercPageLoad = "";
  //90th percentile for Byte received
  private String ninetyPercByteRec = "";
  //90th percentile for Byte Sent
  private String ninetyPercByteSent = "";
  //90th percentile for Avg Request
  private String ninetyPercAvgReq = "";
  
  
  //95th percentile for DOM
  private String ninetyFifthPercDOM = "";
  //95th percentile for OnLoad
  private String ninetyFifthPercOnLoad = "";
  //95th percentile for Page load
  private String ninetyFifthPercPageLoad = "";
//95th percentile for Data Rec
  private String ninetyFifthPercByteRec = "";
//95th percentile for Data Sent
  private String ninetyFifthPercByteSent = "";
//95th percentile for Request
  private String ninetyFifthPercReq = "";
// 95thPercbrowser
  private String ninetyFifthPercbrowser = "";
// 95PercStartRender
  private String ninetyFifthPercStartRender = "";
// VMR Browser
  private String vmrBrowser = "";
// Start Render
  private String vmrStartRender = "";
// Standard deviation for Browser
  private String stdDevBrowser = "";
// Standard deviation for Page load
  private String stdDevStartRender = "";
  //95PercEndRender
  private String ninetyFifthPercEndRender = "";
  //End Render
  private String vmrEndRender = "";
  
  private String stdDevEndRender = "";

  
                                                         /**Setter  and getter methods **/
  
  public String getVmrDOM()
  {
    return vmrDOM;
  }

  public void setVmrDOM(String vmrDOM)
  {
    this.vmrDOM = vmrDOM;
  }

  public String getVmrOnload()
  {
    return vmrOnload;
  }

  public void setVmrOnload(String vmrOnload)
  {
    this.vmrOnload = vmrOnload;
  }

  public String getVmrReq()
  {
    return vmrReq;
  }

  public void setVmrReq(String vmrReq)
  {
    this.vmrReq = vmrReq;
  }

  public String getVmrByteRec()
  {
    return vmrByteRec;
  }

  public void setVmrByteRec(String vmrByteRec)
  {
    this.vmrByteRec = vmrByteRec;
  }

  public String getVmrByteSent()
  {
    return vmrByteSent;
  }

  public void setVmrByteSent(String vmrByteSent)
  {
    this.vmrByteSent = vmrByteSent;
  }

  public String getVmrPageLoad()
  {
    return vmrPageLoad;
  }

  public void setVmrPageLoad(String vmrPageLoad)
  {
    this.vmrPageLoad = vmrPageLoad;
  }

  public String getStdDevDOM()
  {
    return stdDevDOM;
  }

  public void setStdDevDOM(String stdDevDOM)
  {
    this.stdDevDOM = stdDevDOM;
  }

  public String getStdDevOnload()
  {
    return stdDevOnload;
  }

  public void setStdDevOnload(String stdDevOnload)
  {
    this.stdDevOnload = stdDevOnload;
  }

  public String getStdDevReq()
  {
    return stdDevReq;
  }

  public void setStdDevReq(String stdDevReq)
  {
    this.stdDevReq = stdDevReq;
  }

  public String getStdDevByteRec()
  {
    return stdDevByteRec;
  }

  public void setStdDevByteRec(String stdDevByteRec)
  {
    this.stdDevByteRec = stdDevByteRec;
  }

  public String getStdDevByteSent()
  {
    return stdDevByteSent;
  }

  public void setStdDevByteSent(String stdDevByteSent)
  {
    this.stdDevByteSent = stdDevByteSent;
  }

  public String getStdDevPageLoad()
  {
    return stdDevPageLoad;
  }

  public void setStdDevPageLoad(String stdDevPageLoad)
  {
    this.stdDevPageLoad = stdDevPageLoad;
  }

  public String getMedianDOM()
  {
    return medianDOM;
  }

  public void setMedianDOM(String medianDOM)
  {
    this.medianDOM = medianDOM;
  }

  public String getMedianOnLoad()
  {
    return medianOnLoad;
  }

  public void setMedianOnLoad(String medianOnLoad)
  {
    this.medianOnLoad = medianOnLoad;
  }

  public String getMedianPageLoad()
  {
    return medianPageLoad;
  }

  public void setMedianPageLoad(String medianPageLoad)
  {
    this.medianPageLoad = medianPageLoad;
  }

  public String getMedianByteRec()
  {
    return medianByteRec;
  }

  public void setMedianByteRec(String medianByteRec)
  {
    this.medianByteRec = medianByteRec;
  }

  public String getMedianByteSent()
  {
    return medianByteSent;
  }

  public void setMedianByteSent(String medianByteSent)
  {
    this.medianByteSent = medianByteSent;
  }

  public String getNinetyPercDOM()
  {
    return ninetyPercDOM;
  }

  public void setNinetyPercDOM(String ninetyPercDOM)
  {
    this.ninetyPercDOM = ninetyPercDOM;
  }

  public String getNinetyPercOnLoad()
  {
    return ninetyPercOnLoad;
  }

  public void setNinetyPercOnLoad(String ninetyPercOnLoad)
  {
    this.ninetyPercOnLoad = ninetyPercOnLoad;
  }

  public String getNinetyPercPageLoad()
  {
    return ninetyPercPageLoad;
  }

  public void setNinetyPercPageLoad(String ninetyPercPageLoad)
  {
    this.ninetyPercPageLoad = ninetyPercPageLoad;
  }

  public String getNinetyPercByteRec()
  {
    return ninetyPercByteRec;
  }

  public void setNinetyPercByteRec(String ninetyPercByteRec)
  {
    this.ninetyPercByteRec = ninetyPercByteRec;
  }

  public String getNinetyPercByteSent()
  {
    return ninetyPercByteSent;
  }

  public void setNinetyPercByteSent(String ninetyPercByteSent)
  {
    this.ninetyPercByteSent = ninetyPercByteSent;
  }

  public String getNinetyFifthPercDOM()
  {
    return ninetyFifthPercDOM;
  }

  public void setNinetyFifthPercDOM(String ninetyFifthPercDOM)
  {
    this.ninetyFifthPercDOM = ninetyFifthPercDOM;
  }

  public String getNinetyFifthPercOnLoad()
  {
    return ninetyFifthPercOnLoad;
  }

  public void setNinetyFifthPercOnLoad(String ninetyFifthPercOnLoad)
  {
    this.ninetyFifthPercOnLoad = ninetyFifthPercOnLoad;
  }

  public String getNinetyFifthPercPageLoad()
  {
    return ninetyFifthPercPageLoad;
  }

  public void setNinetyFifthPercPageLoad(String ninetyFifthPercPageLoad)
  {
    this.ninetyFifthPercPageLoad = ninetyFifthPercPageLoad;
  }

public String getNinetyFifthPercByteRec() 
{
  return ninetyFifthPercByteRec;
 }

public void setNinetyFifthPercByteRec(String ninetyFifthPercByteRec) 
{
 this.ninetyFifthPercByteRec = ninetyFifthPercByteRec;
}

public String getNinetyFifthPercByteSent() 
{
	return ninetyFifthPercByteSent;
}

public void setNinetyFifthPercByteSent(String ninetyFifthPercByteSent) 
{
  this.ninetyFifthPercByteSent = ninetyFifthPercByteSent;
}

public String getNinetyFifthPercReq() 
{
  return ninetyFifthPercReq;
}

public void setNinetyFifthPercReq(String ninetyFifthPercReq) 
{
  this.ninetyFifthPercReq = ninetyFifthPercReq;
}

public String getVmrBrowser()
{
  return vmrBrowser;
}

public void setVmrBrowser(String vmrBrowser)
{
  this.vmrBrowser = vmrBrowser;
}

public String getVmrStartRender()
{
  return vmrStartRender;
}

public void setVmrStartRender(String vmrStartRender)
{
  this.vmrStartRender = vmrStartRender;
}

public String getNinetyFifthPercbrowser()
{
  return ninetyFifthPercbrowser;
}

public void setNinetyFifthPercbrowser(String ninetyFifthPercbrowser)
{
  this.ninetyFifthPercbrowser = ninetyFifthPercbrowser;
}

public String getNinetyFifthPercStartRender()
{
  return ninetyFifthPercStartRender;
}

public void setNinetyFifthPercStartRender(String ninetyFifthPercStartRender)
{
  this.ninetyFifthPercStartRender = ninetyFifthPercStartRender;
}

public String getStdDevBrowser()
{
  return stdDevBrowser;
}

public void setStdDevBrowser(String stdDevBrowser)
{
  this.stdDevBrowser = stdDevBrowser;
}

public String getStdDevStartRender()
{
  return stdDevStartRender;
}

public void setStdDevStartRender(String stdDevStartRender)
{
  this.stdDevStartRender = stdDevStartRender;
}

public String getNinetyFifthPercEndRender()
{
  return ninetyFifthPercEndRender;
}

public void setNinetyFifthPercEndRender(String ninetyFifthPercEndRender)
{
  this.ninetyFifthPercEndRender = ninetyFifthPercEndRender;
}

public String getVmrEndRender()
{
  return vmrEndRender;
}

public void setVmrEndRender(String vmrEndRender)
{
  this.vmrEndRender = vmrEndRender;
}

public String getStdDevEndRender()
{
  return stdDevEndRender;
}

public void setStdDevEndRender(String stdDevEndRender)
{
  this.stdDevEndRender = stdDevEndRender;
}

  public String getMedianAvgReq()
  {
    return medianAvgReq;
  }

  public void setMedianAvgReq(String medianAvgReq)
  {
    this.medianAvgReq = medianAvgReq;
  }

  public String getNinetyPercAvgReq()
  {
    return ninetyPercAvgReq;
  }

  public void setNinetyPercAvgReq(String ninetyPercAvgReq)
  {
    this.ninetyPercAvgReq = ninetyPercAvgReq;
  }

}

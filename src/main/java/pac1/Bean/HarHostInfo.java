package pac1.Bean;

public class HarHostInfo
{
  private int numRequest = 0;
  private int numBrowserRequest = 0;
  private int numAkamaiHIT = 0;
  private int numCloudFrontHIT = 0;
  private int numStrangeLoopHIT = 0;
  private int numOrigRequest = 0;
  private int numThirdPartyRequest = 0;
  
  private String vendor = "Origin"; 
  private String state = "";
  
  private long fileSize = 0;
  
  public int getNumRequest()
  {
    return numRequest;
  }
  
  public void setNumRequest(int numRequest) 
  {
    this.numRequest = numRequest;
  }

  public String getVendor() 
  {
    return vendor;
  }

  public void setVendor(String vendor) 
  {
    this.vendor = vendor;
  }

  public String getState()
  {
    return state;
  }

  public void setState(String state) 
  {
    this.state = state;
  }

  public int getNumBrowserRequest() 
  {
    return numBrowserRequest;
  }

  
  public int getNumThirdPartyRequest()
  {
    return numThirdPartyRequest;
  }

  public void setNumThirdPartyRequest(int numThirdPartyRequest) 
  {
    this.numThirdPartyRequest = numThirdPartyRequest;
  }

  public void setNumBrowserRequest(int numBrowserRequest) 
  {
    this.numBrowserRequest = numBrowserRequest;
  }

  public int getNumAkamaiHIT()
  {
    return numAkamaiHIT;
  }

  public void setNumAkamaiHIT(int numAkamaiHIT)
  {
    this.numAkamaiHIT = numAkamaiHIT;
  }

  
  public int getNumCloudFrontHIT() 
  {
    return numCloudFrontHIT;
  }

  public void setNumCloudFrontHIT(int numCloudFrontHIT) 
  {
    this.numCloudFrontHIT = numCloudFrontHIT;
  }

  public int getNumStrangeLoopHIT() 
  {
    return numStrangeLoopHIT;
  }

  public void setNumStrangeLoopHIT(int numStrangeLoopHIT) 
  {
    this.numStrangeLoopHIT = numStrangeLoopHIT;
  }

  public int getNumOrigRequest()
  {
    return numOrigRequest;
  }

  public void setNumOrigRequest(int numOrigRequest)
  {
    this.numOrigRequest = numOrigRequest;
  }

  public long getFileSize() 
  {
    return fileSize;
  }

  public void setFileSize(long fileSize) 
  {
    this.fileSize = fileSize;
  }
  
  
  
}

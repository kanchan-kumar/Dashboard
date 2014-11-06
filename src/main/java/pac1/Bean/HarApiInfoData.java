package pac1.Bean;

import java.util.ArrayList;

public class HarApiInfoData
{
  private String apiStr = null;
  private double respTime = 0.0;
  private double dataSize = 0.0;
  private String pageName = null;
  private int apiCount = 0;
  private String hostName = null;
  private String screenSize = null;
    
  //arraylist of responce time of API based on URL on page
  private ArrayList<Double> respTimeList = null;

  /**
   * @return the apiStr
   */
    public String getScreenSize() 
    {
		return screenSize;
	}

	public void setScreenSize(String screenSize) 
	{
		this.screenSize = screenSize;
	}  
  
    public String getHostName() 
    {
		return hostName;
	}

	public void setHostName(String hostName) 
	{
		this.hostName = hostName;
	}

  public String getApiStr()
  {
    return apiStr;
  }

  /**
   * @param apiStr the apiStr to set
   */
  public void setApiStr(String apiStr)
  {
    this.apiStr = apiStr;
  }

  /**
   * @return the respTime
   */
  public double getRespTime()
  {
    return respTime;
  }

  /**
   * @param respTime the respTime to set
   */
  public void setRespTime(double respTime)
  {
    this.respTime = respTime;
  }

  /**
   * @return the dataSize
   */
  public double getDataSize()
  {
    return dataSize;
  }

  /**
   * @param dataSize the dataSize to set
   */
  public void setDataSize(double dataSize)
  {
    this.dataSize = dataSize;
  }

  /**
   * @return the pageName
   */
  public String getPageName()
  {
    return pageName;
  }

  /**
   * @param pageName the pageName to set
   */
  public void setPageName(String pageName)
  {
    this.pageName = pageName;
  }

  /**
   * @return the apiCount
   */
  public int getApiCount()
  {
    return apiCount;
  }

  /**
   * @param apiCount the apiCount to set
   */
  public void setApiCount(int apiCount)
  {
    this.apiCount = apiCount;
  }

  /**
   * @return the respTimeList
   */
  public ArrayList<Double> getRespTimeList()
  {
    return respTimeList;
  }
  
  /**
   * @param respTimeList the respTimeList to set
   */
   public void setRespTimeList(ArrayList<Double> respTimeList)
   {
     this.respTimeList = respTimeList;
   }
}

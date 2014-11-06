package pac1.Bean;

import java.io.Serializable;

public class GDFInfoBean implements Serializable
{
  private static final long serialVersionUID = 26797129374259717L;
  private int majorVersionNum = 0;
  private int totalGroups = 0;
  private int headerLength = 0;
  private int sizeOfMsgData = 0;
  private int progressInterval = -1;
  private String testRunStartDateTime = null;
  private int minorVersionNum = 0;
  private int dataElementSize = 0;

  public int getMajorVersionNum()
  {
    return majorVersionNum;
  }

  public void setMajorVersionNum(int majorVersionNum)
  {
    this.majorVersionNum = majorVersionNum;
  }

  public int getTotalGroups()
  {
    return totalGroups;
  }

  public void setTotalGroups(int totalGroups)
  {
    this.totalGroups = totalGroups;
  }

  public int getHeaderLength()
  {
    return headerLength;
  }

  public void setHeaderLength(int headerLength)
  {
    this.headerLength = headerLength;
  }

  public int getSizeOfMsgData()
  {
    return sizeOfMsgData;
  }

  public void setSizeOfMsgData(int sizeOfMsgData)
  {
    this.sizeOfMsgData = sizeOfMsgData;
  }

  public int getProgressInterval()
  {
    return progressInterval;
  }

  public void setProgressInterval(int progressInterval)
  {
    this.progressInterval = progressInterval;
  }

  public String getTestRunStartDateTime()
  {
    return testRunStartDateTime;
  }

  public void setTestRunStartDateTime(String testRunStartDateTime)
  {
    this.testRunStartDateTime = testRunStartDateTime;
  }

  public int getMinorVersionNum()
  {
    return minorVersionNum;
  }

  public void setMinorVersionNum(int minorVersionNum)
  {
    this.minorVersionNum = minorVersionNum;
  }

  public int getDataElementSize()
  {
    return dataElementSize;
  }

  public void setDataElementSize(int dataElementSize)
  {
    this.dataElementSize = dataElementSize;
  }

  public String tostString()
  {
    return "No Of Groups = " + totalGroups + ", Major Version = " + majorVersionNum + ", Minor Version = " + minorVersionNum + ", Data Element Size = " + dataElementSize + ", Size Of Msg Data = " + sizeOfMsgData + ", Progress Interval = " + progressInterval + ", Header Len = " + headerLength;
  }

  public static void main(String[] args)
  {
    // TODO Auto-generated method stub

  }

}

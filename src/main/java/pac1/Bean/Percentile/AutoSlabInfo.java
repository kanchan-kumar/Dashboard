/**
 * This class is for Generating Percentile Data 
 * @author Akshay Garg
 * @since Netsorm Version 4.0.0
 * @Modification_History Akshay Garg - Initial Version 4.0.0
 * @version 4.0.0
 * 
 */
package pac1.Bean.Percentile;

import pac1.Bean.rptUtilsBean;

public class AutoSlabInfo
{
  public static String className = "AutoSlabInfo";

  private String[] arrSlabInfo;
  private double minValue;
  private double maxValue;
  private long incrementValue;
  private int maxGranule = 10;
  private long[] arrMin = null;
  private long[] arrMax = null;

  public long[] getArrMin()
  {
    return arrMin;
  }

  public void setArrMin(long[] arrMin)
  {
    this.arrMin = arrMin;
  }

  public long[] getArrMax()
  {
    return arrMax;
  }

  public void setArrMax(long[] arrMax)
  {
    this.arrMax = arrMax;
  }

  public int getMaxGranule()
  {
    return maxGranule;
  }

  public void setMaxGranule(int maxGranule)
  {
    this.maxGranule = maxGranule;
  }

  public String[] getArrSlabInfo()
  {
    return arrSlabInfo;
  }

  public void setArrSlabInfo(String[] arrSlabInfo)
  {
    this.arrSlabInfo = arrSlabInfo;
  }

  public double getMinValue()
  {
    return minValue;
  }

  public void setMinValue(double minValue)
  {
    this.minValue = minValue;
  }

  public double getMaxValue()
  {
    return maxValue;
  }

  public void setMaxValue(double maxValue)
  {
    this.maxValue = maxValue;
  }

  public long getIncrementValue()
  {
    return incrementValue;
  }

  public void setIncrementValue(long incrementValue)
  {
    this.incrementValue = incrementValue;
  }

  public String toString()
  {
    try
    {
      return "Slabs (" + rptUtilsBean.strArrayToStr(arrSlabInfo, ",") + "), minValue (" + minValue + "), maxValue(" + maxValue + "), maxGranule (" + maxGranule + "), incrementValue (" + incrementValue + "), Min Array (" + rptUtilsBean.longArrayToStr(arrMin, ",") + "), Max Array (" + rptUtilsBean.longArrayToStr(arrMax, ",") + ")";
    }
    catch (Exception e)
    {
      return super.toString();
    }
  }
}
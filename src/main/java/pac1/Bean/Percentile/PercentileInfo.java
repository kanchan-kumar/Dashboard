package pac1.Bean.Percentile;

import java.io.Serializable;

import pac1.Bean.Log;
import pac1.Bean.rptUtilsBean;

public class PercentileInfo implements Serializable
{
  private String className = "PercentileInfo";
  private static final long serialVersionUID = 8150524439171281430L;
  private double[] arrPercentileData = null;
  private String[] arrSlabInfo = null;

  public String[] getArrSlabInfo()
  {
    return arrSlabInfo;
  }

  public void setArrSlabInfo(String[] arrSlabInfo)
  {
    this.arrSlabInfo = arrSlabInfo;
  }

  public double[] getArrPercentileData()
  {
    return arrPercentileData;
  }

  public void setArrPercentileData(double[] arrPercentileData)
  {
    this.arrPercentileData = arrPercentileData;
  }

  public String toString()
  {
    if (arrPercentileData != null)
    {
      return "Percentile Data = " + rptUtilsBean.doubleArrayToList(arrPercentileData);
    }
    else
    {
      Log.errorLog(className, "PercentileInfo", "", "", "arrPercentileData = " + arrPercentileData);
      return "";
    }
  }

  public static void main(String[] args)
  {

  }
}

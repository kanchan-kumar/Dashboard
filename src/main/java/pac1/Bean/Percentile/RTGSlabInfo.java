package pac1.Bean.Percentile;

import pac1.Bean.Log;
import pac1.Bean.rptUtilsBean;

public class RTGSlabInfo
{
  public static String className = "RTGSlabInfo";
  static int debugLevel = 0;
  private static int maxGranule = 10;

  // This is for which percentile is useful for generating slabs, It is default value
  private static int whichPercentile = 90;

  public static AutoSlabInfo getAutoSlabInfo(int debugLevel, double[] arrPercentileData)
  {
    AutoSlabInfo autoSlabInfo = new AutoSlabInfo();

    try
    {
      RTGSlabInfo.debugLevel = debugLevel;

      if (debugLevel > 0)
        Log.debugLogAlways(className, "getAutoSlabInfo", "", "", "Method Called. maxGranule  = " + maxGranule);

      if (arrPercentileData == null || arrPercentileData.length == 0)
      {
        Log.errorLog(className, "getAutoSlabInfo", "", "", "arrPercentileData is null so cannot generate auto slabs.");
        return null;
      }

      double minValue = arrPercentileData[0];
      double maxValue = arrPercentileData[arrPercentileData.length - 1];

      if (maxValue == 0)
      {
        long[] arrMinData = new long[maxGranule];
        long[] arrMaxData = new long[maxGranule];
        String[] arrSlabInfo = new String[maxGranule];
        for (int i = 0; i < maxGranule; i++)
        {
          arrMinData[i] = i;
          arrMaxData[i] = (i + 1);
          arrSlabInfo[i] = i + "-" + (i + 1);
        }

        autoSlabInfo.setArrMax(arrMaxData);
        autoSlabInfo.setArrMin(arrMinData);
        autoSlabInfo.setArrSlabInfo(arrSlabInfo);
        autoSlabInfo.setMaxGranule(maxGranule);
        autoSlabInfo.setMinValue(minValue);
        autoSlabInfo.setMaxValue(maxValue);
        autoSlabInfo.setIncrementValue(1);
      }
      else
      {
        double diff = maxValue - minValue;
        long bucketSize = (long) (diff / (maxGranule - 1));
        if (bucketSize == 0)
          bucketSize = 1;

        String[] arrSlabInfo = new String[maxGranule];
        long[] startSlab = new long[maxGranule];
        long[] endSlab = new long[maxGranule];

        long endValue = 0;
        for (int i = 0; i < maxGranule; i++)
        {
          if (i == 0)
          {
            startSlab[i] = (long) minValue;
            endValue = (long) (minValue + bucketSize);
            endSlab[i] = endValue;
            arrSlabInfo[i] = startSlab[i] + "-" + endSlab[i];
          }
          else
          {
            startSlab[i] = endValue;
            endValue = (endValue + bucketSize);
            endSlab[i] = endValue;

            arrSlabInfo[i] = startSlab[i] + "-" + endSlab[i];
          }
        }

        autoSlabInfo.setArrMax(endSlab);
        autoSlabInfo.setArrMin(startSlab);
        autoSlabInfo.setArrSlabInfo(arrSlabInfo);
        autoSlabInfo.setMaxGranule(maxGranule);
        autoSlabInfo.setMinValue(minValue);
        autoSlabInfo.setMaxValue(maxValue);
        autoSlabInfo.setIncrementValue(bucketSize);
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getAutoSlabInfo", "", "", "Exception - ", e);
    }

    if (debugLevel > 2)
      Log.debugLogAlways(className, "getAutoSlabInfo", "", "", "autoSlabInfo = " + autoSlabInfo);

    return autoSlabInfo;
  }

  /**
   * This method is for generating slab count data for slabs
   * 
   * @param arrRawData
   * @param autoSlabInfo
   * @return
   */
  public static double[] getArrSlabData(double[] arrRawData, AutoSlabInfo autoSlabInfo)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLog(className, "getArrSlabData", "", "", "Method Called.");

      long[] arrMin = autoSlabInfo.getArrMin();
      long[] arrMax = autoSlabInfo.getArrMax();

      double[] arrSlabData = new double[arrMin.length];

      for (int i = 0; i < arrRawData.length; i++)
      {
        double dataValue = arrRawData[i];
        for (int j = 0; j < arrMin.length; j++)
        {
          long minValue = arrMin[j];
          long maxValue = arrMax[j];

          if (debugLevel > 2)
            Log.debugLogAlways(className, "getArrSlabData", "", "", "minValue = " + minValue + ", maxValue = " + maxValue + ", dataValue = " + dataValue);

          if (dataValue >= minValue && dataValue <= maxValue)
          {
            arrSlabData[j]++;
            break;
          }
        }
      }

      return arrSlabData;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getArrSlabData", "", "", "Exception - ", e);
      return null;
    }
  }

  public static void main(String[] args)
  {
    double[] arrPercentileData = new double[101];
    AutoSlabInfo auto = getAutoSlabInfo(1, arrPercentileData);
    System.out.println("auto = " + auto);
  }
}

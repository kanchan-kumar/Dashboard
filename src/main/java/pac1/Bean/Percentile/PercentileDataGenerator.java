/**
 * This class is for Storing Generate and Contain Percentile Data Temporary 
 * @author Ravi Kant Sharma
 * @since Netsorm Version 4.0.0
 * @Modification_History Ravi Kant Sharma - Initial Version 4.0.0
 * @version 4.0.0
 * 
 */
package pac1.Bean.Percentile;

import java.util.Arrays;

import pac1.Bean.*;

public class PercentileDataGenerator
{
  private final String className = "PercentileDataGenerator";
  private double[] arrPCTCumDataBuckets = null;
  private GraphUniqueKeyDTO graphUniqueKeyDTO = null;
  private PctMsgInfo pctMsgInfo = null;
  private int debugLevel = 0;

  public PercentileDataGenerator(int debugLevel, PctMsgInfo pctMsgInfo, GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    this.debugLevel = debugLevel;
    this.pctMsgInfo = pctMsgInfo;
    this.graphUniqueKeyDTO = graphUniqueKeyDTO;
  }

  public void setArrPCTCumDataBuckets(double[] arrPCTCumDataBuckets)
  {
    this.arrPCTCumDataBuckets = arrPCTCumDataBuckets;
  }

  public GraphUniqueKeyDTO getGraphUniqueKeyDTO()
  {
    return graphUniqueKeyDTO;
  }

  public void setGraphUniqueKeyDTO(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    this.graphUniqueKeyDTO = graphUniqueKeyDTO;
  }

  // this function return the array of cumulative PCT Data count
  public double[] getArrPCTCumDataBuckets()
  {
    return arrPCTCumDataBuckets;
  }

  // This function return the cum counts of each slabs for Slab Graph
  public double[] getArrSlabsData()
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "getArrSlabsData", "", "", "Method Start.");

      int pdfId = pctMsgInfo.getPdfId();
      SlabInfo[] slabInfo = pctMsgInfo.getGraphNames().getPdfNames().getArrSlabsInfoByPDFId(pdfId);
      double[] arrSlabsData = new double[slabInfo.length];
      for (int i = 0; i < slabInfo.length; i++)
      {
        int minS = slabInfo[i].getSlabMinValue();
        int maxS = slabInfo[i].getSlabMaxValue();
        double count = 0;
        if (maxS != -1) // If not tends to infinity
        {
          for (int j = (minS / pctMsgInfo.getMinGranule()); j < (maxS / pctMsgInfo.getMinGranule()); j++)
          {
            count += arrPCTCumDataBuckets[j];
          }
        }
        else
        {
          for (int j = (minS / pctMsgInfo.getMinGranule()); j < arrPCTCumDataBuckets.length; j++)
          {
            count += arrPCTCumDataBuckets[j];
          }
        }

        arrSlabsData[i] = count;
      }

      if (debugLevel > 0)
        Log.debugLogAlways(className, "getArrSlabsData", "", "", "Slab Data value = " + rptUtilsBean.doubleArrayToList(arrSlabsData) + ", length = " + arrSlabsData.length);

      return arrSlabsData;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getArrSlabsData", "", "", "Exception - ", e);
      return null;
    }
  }

  public double getPercentileValue(int index, int fractionIndex, double fractionValue)
  {
    double percentileData = 0;

    if (fractionIndex == 0)
    {
      if ((index != (pctMsgInfo.getNumGranule() - 1)))
        percentileData = ((index + 1) * pctMsgInfo.getMinGranule()) * pctMsgInfo.getFormulaUnitData();
      else
        percentileData = (index * pctMsgInfo.getMinGranule()) * pctMsgInfo.getFormulaUnitData();
    }
    else
    {
      percentileData = ((fractionIndex - index) * fractionValue * pctMsgInfo.getMinGranule()) * pctMsgInfo.getFormulaUnitData();
    }
    return percentileData;

    // return getArrPercentileData()[percentileIndex] = PercentileDataUtils.convTo3DigitDecimal(percentileData);
  }

  // This function return the array of percentile data for Percentile Graph
  public double[] getArrPercentileData()
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "getArrPercentileData", "", "", "Method Start. graphUniqueKeyDTO = " + graphUniqueKeyDTO);

      double[] arrPercentileData = new double[100];
      double[] arrCumCount = new double[pctMsgInfo.getNumGranule()];
      double[] out_pct = new double[pctMsgInfo.getNumGranule()];

      double cumSum = 0;
      for (int i = 0; i < arrPCTCumDataBuckets.length; i++)
      {
        cumSum += arrPCTCumDataBuckets[i];
        if (i == 0)
          arrCumCount[i] = arrPCTCumDataBuckets[i];
        else
          arrCumCount[i] = arrCumCount[i - 1] + arrPCTCumDataBuckets[i];
      }

      if (debugLevel > 0)
        Log.debugLogAlways(className, "getArrPercentileData", "", "", "Cumulative sum = " + cumSum + ", Cumulative Count = " + rptUtilsBean.doubleArrayToList(arrCumCount) + ", length = " + arrCumCount.length);

      if (cumSum == 0)
      {
        Log.debugLogAlways(className, "getArrPercentileData", "", "", "Cumulative sum is zero so all percentile will be zero.");
        return new double[101];
      }

      for (int i = 0; i < arrPercentileData.length; i++)
      {
        double tempNthPercentileDataValueCount = (((i + 1) * (cumSum - 1)) / 100) + 1; // Modified

        int cmp_count_int = (int) tempNthPercentileDataValueCount; // Stores integer part
        double cmp_count_fraction = tempNthPercentileDataValueCount - cmp_count_int; // Stores fraction part

        if (debugLevel > 3)
          Log.debugLogAlways(className, "getArrPercentileData", "", "", "tempNthPercentileDataValueCount = " + tempNthPercentileDataValueCount + ", cmp_count_int = " + cmp_count_int + ", cmp_count_fraction = " + cmp_count_fraction);

        int cur_idx = 0;
        int j = 0;
        double percentileDataTem = 0;
        double data1 = 0;
        for (j = 0; j < arrCumCount.length; j++)
        {
          if (cmp_count_int <= arrCumCount[j])
          {
            data1 = getPercentileValue(j, 0, 0.0);
            cur_idx = j + 1;
            break;
          }
        }

        if (debugLevel > 3)
          Log.debugLogAlways(className, "getArrPercentileData", "", "", "data1 = " + data1 + ", cur_idx = " + cur_idx);

        if (cur_idx >= arrCumCount.length)
        {
          if (debugLevel > 0)
            Log.debugLogAlways(className, "getArrPercentileData", "", "", "cur_idx = " + cur_idx + ", arrCumCount.length = " + arrCumCount.length + ". So taking last index.");

          cur_idx = arrCumCount.length - 1;
        }

        double data2 = 0;
        if (arrCumCount[cur_idx] == cmp_count_int && cmp_count_fraction > 0)
        {
          while (arrCumCount[cur_idx] <= arrCumCount[j] && cur_idx < arrCumCount.length)
          {
            cur_idx++;
          }

          out_pct[cur_idx] += ((double) (cur_idx - j) * cmp_count_fraction * arrPCTCumDataBuckets.length);

          data2 = getPercentileValue(j, cur_idx, cmp_count_fraction);
        }

        if (debugLevel > 3)
          Log.debugLogAlways(className, "getArrPercentileData", "", "", "data2 = " + data2);

        percentileDataTem = data1 + data2;

        if (debugLevel > 3)
          Log.debugLogAlways(className, "getArrPercentileData", "", "", "percentileDataTem = " + percentileDataTem);

        arrPercentileData[i] = percentileDataTem;
        arrPercentileData[i] = PercentileDataUtils.convTo3DigitDecimal(percentileDataTem);
      }

      double[] arrFinalData = new double[101];
      int count = 1;
      for (int i = 0; i < arrPercentileData.length; i++)
      {
        arrFinalData[count] = arrPercentileData[i];
        count++;
      }

      if (debugLevel > 0)
        Log.debugLogAlways(className, "getArrPercentileData", "", "", "Percentile data = " + rptUtilsBean.doubleArrayToList(arrFinalData) + ", length = " + arrFinalData.length);

      return arrFinalData;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getArrPercentileData", "", "", "Exception - ", e);
      return null;
    }
  }
}
/*--------------------------------------------------------------------
@Name    : GraphPCTData.java
@Author  : Prabhat
@Purpose : To store all calculated data
@Modification History:
    01/13/2009 --> Prabhat  -->  Initial Version

----------------------------------------------------------------------*/

package pac1.Bean;

import java.util.*;

import pac1.Bean.GraphName.GraphNameUtils;

public class GraphPCTData implements java.io.Serializable
{
  private final String className = "GraphPCTData";
  private double[] arrPCTCumDataBuckets = null;
  public int numGranule;
  public int minGranule;
  private double meanDataVal = -1;
  private double stdDevDataValue = -1;
  private int formulaNumber;
  private double formulaUnitData = 1;
  private int pdfId;
  private String pdfUnit;
  private PDFNames pdfNames;
  private SlabInfo[] slabInfo = null;

  public GraphPCTData(int pdfId, int interval, PDFNames pdfNames)
  {
    this.pdfId = pdfId;
    this.pdfNames = pdfNames;
  }

  public void initGraphPCTData()
  {
    Log.debugLog(className, "initGraphPCTData", "", "", "Method Start. PDF Id = " + pdfId);

    try
    {
      numGranule = pdfNames.getNumGranuleByPDFId(pdfId);

      minGranule = pdfNames.getMinGranuleByPDFId(pdfId);

      arrPCTCumDataBuckets = new double[numGranule];

      Arrays.fill(arrPCTCumDataBuckets, 0);

      formulaNumber = pdfNames.getFormulaNumberByPDFId(pdfId);

      pdfUnit = pdfNames.getPDFUnitByPDFId(pdfId);

      setFormulaUnitDataByFormulaNum(formulaNumber);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "initGraphPCTData", "", "", "Exception - ", e);
    }
  }

  // this function calculate graph PCT Data and set cummulative pct data
  public void calcAndSetGraphPCTData(double[] pctData)
  {
    Log.debugLog(className, "calcAndSetGraphPCTData", "", "", "Method Start.");

    try
    {
      for (int i = 0; i < numGranule; i++)
      {
        if (pctData[i] >= arrPCTCumDataBuckets[i])
          arrPCTCumDataBuckets[i] = pctData[i] - arrPCTCumDataBuckets[i];
        else
        {
          Arrays.fill(arrPCTCumDataBuckets, 0);
          Log.errorLog(className, "calcAndSetGraphPCTData", "", "", "ERROR: this seq number data is less than to previous one, not a valid condition.");
        }
      }

      Log.debugLog(className, "calcAndSetGraphPCTData", "", "", "PCT cumulative data = " + rptUtilsBean.doubleArrayToList(arrPCTCumDataBuckets) + ", length = " + arrPCTCumDataBuckets.length);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "calcAndSetGraphPCTData", "", "", "Exception - ", e);
    }
  }

  // This is the function to calculate and set custom group graph's data
  public void calcAndSetCustomGrpData(double[] arrRptData, int startSeq, int endSeq)
  {
    Log.debugLog(className, "calcAndSetCustomGrpData", "", "", "Method Start. Start Seq = " + startSeq + ", End Seq = " + endSeq);

    try
    {
      double[] arrCountBucket = new double[numGranule];

      Arrays.fill(arrCountBucket, 0);

      for (int i = startSeq; i < endSeq; i++)
      {
        int granuleIndex = (int) arrRptData[i] / minGranule;

        if (granuleIndex > numGranule) // For greater than bucket
          arrCountBucket[arrCountBucket.length - 1]++;
        else
          arrCountBucket[granuleIndex]++;
      }

      Log.debugLog(className, "calcAndSetCustomGrpData", "", "", "Custom Group, Cumulative Count = " + rptUtilsBean.doubleArrayToList(arrCountBucket) + ", length = " + arrCountBucket.length);

      arrPCTCumDataBuckets = arrCountBucket;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "calcAndSetCustomGrpData", "", "", "Exception - ", e);
    }
  }

  private void setFormulaUnitDataByFormulaNum(int formulaNum)
  {
    Log.debugLog(className, "setFormulaUnitDataByFormulaNum", "", "", "Method Start. Formula number = " + formulaNum);

    try
    {
      // SEC - Convert milli-sec to seconds
      if (formulaNum == GraphNameUtils.FORMULA_SEC)
      {
        double convertionUnitMilli2Sec = 1000;
        formulaUnitData = formulaUnitData / convertionUnitMilli2Sec;
      }
      // PM - Convert to Per Minute
      else if (formulaNum == GraphNameUtils.FORMULA_PM)
      {
        Log.errorLog(className, "setFormulaUnitDataByFormulaNum", "", "", "Error: Currently we are not supported this PM formula.");
      }
      // PS - Convert to Per Seconds
      else if (formulaNum == GraphNameUtils.FORMULA_PS)
      {
        Log.errorLog(className, "setFormulaUnitDataByFormulaNum", "", "", "Error: Currently we are not supported this PS formula.");
      }
      // KBPS - Convert to kilo byte per second
      else if (formulaNum == GraphNameUtils.FORMULA_KBPS)
      {
        double convertionUnitBytes2KBPS = 1024;
        formulaUnitData = formulaUnitData / convertionUnitBytes2KBPS;
      }
      // DBH - Convert to divide by 100
      else if (formulaNum == GraphNameUtils.FORMULA_DBH) // 
      {
        double convertionUnit2DBH = 100;
        formulaUnitData = formulaUnitData / convertionUnit2DBH;
      }

      Log.debugLog(className, "setFormulaUnitDataByFormulaNum", "", "", "formula unit data = " + formulaUnitData);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "setFormulaUnitDataByFormulaNum", "", "", "Exception - ", e);
    }
  }

  // this function return the array of cumulative PCT Data count
  public double[] getArrPCTCumDataBuckets()
  {
    return arrPCTCumDataBuckets;
  }

  // this function return the mean value of PCT Data for frequency distribution
  // graph
  public double getMeanPctDataVal()
  {
    Log.debugLog(className, "getMeanPctDataVal", "", "", "Method Start.");

    try
    {
      Log.debugLog(className, "getMeanPctDataVal", "", "", "Mean Value = " + meanDataVal);
      return (PercentileData.convTo3DigitDecimal(meanDataVal));
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getMeanPctDataVal", "", "", "Exception - ", e);
      return -1;
    }
  }

  /**
   * this function return the Std dev value of PCT Data for frequency
   * distribution graph Std-Dev = square root of((sum of square - (sum *
   * avg))/(N-1)) where N = number of buckets
   * 
   * @return
   */
  public double getStdDevDataVal()
  {
    Log.debugLog(className, "getStdDevDataVal", "", "", "Method Start.");

    try
    {
      Log.debugLog(className, "getStdDevDataVal", "", "", "Std dev Value = " + stdDevDataValue);
      return (PercentileData.convTo3DigitDecimal(stdDevDataValue));
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getStdDevDataVal", "", "", "Exception - ", e);
      return -1;
    }
  }

  // This function retrun the cum counts of each slabs for Slab Graph
  public double[] getArrSlabsData()
  {
    Log.debugLog(className, "getArrSlabsData", "", "", "Method Start.");

    try
    {
      slabInfo = pdfNames.getArrSlabsInfoByPDFId(pdfId);

      double[] arrSlabsData = new double[slabInfo.length];

      for (int i = 0; i < slabInfo.length; i++)
      {
        int minS = slabInfo[i].getSlabMinValue();
        int maxS = slabInfo[i].getSlabMaxValue();

        double count = 0;
        // Note --> Prabhat --> our assumption is if (maxS = -1) then it is
        // treated as infinity
        if (maxS != -1) // If not tends to infinity
        {
          for (int j = (minS / minGranule); j < (maxS / minGranule); j++)
          {
            count += arrPCTCumDataBuckets[j];
          }
        }
        else
        // If tends to infinity
        {
          for (int j = (minS / minGranule); j < arrPCTCumDataBuckets.length; j++)
          {
            count += arrPCTCumDataBuckets[j];
          }
        }

        arrSlabsData[i] = count;
      }

      Log.debugLog(className, "getArrSlabsData", "", "", "Slab Data value = " + rptUtilsBean.doubleArrayToList(arrSlabsData) + ", length = " + arrSlabsData.length);

      return arrSlabsData;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getArrSlabsData", "", "", "Exception - ", e);
      return null;
    }
  }

  // This function retrun the arrr of percentile data for Percentile Graph
  public double[] getArrPercentileData()
  {
    Log.debugLog(className, "getArrPercentileData", "", "", "Method Start.");

    try
    {
      double[] arrPercentileData = new double[101];

      double[] arrCumCount = new double[numGranule];

      double cumSum = 0;

      Arrays.fill(arrPercentileData, 0);
      Arrays.fill(arrCumCount, 0);

      // Calculate cumulative sum (summetion of all buckets(cum count)) &
      // calculate cum count of each bucket(nth cum count = nth cum count +
      // (n-1)th cum count)
      for (int i = 0; i < arrPCTCumDataBuckets.length; i++)
      {
        cumSum += arrPCTCumDataBuckets[i];

        if (i == 0)
          arrCumCount[i] = arrPCTCumDataBuckets[i];
        else
          arrCumCount[i] = arrCumCount[i - 1] + arrPCTCumDataBuckets[i];
      }

      Log.debugLog(className, "getArrPercentileData", "", "", "Cumulative sum = " + cumSum + ", Cumulative Count = " + rptUtilsBean.doubleArrayToList(arrCumCount) + ", length = " + arrCumCount.length);

      for (int i = 0; i < arrPercentileData.length; i++)
      {
        double tempNthPercentileDataValueCount = ((i + 1) * cumSum) / 100;

        int j = 0;
        for (j = 0; j < arrCumCount.length; j++)
        {
          // Nth percentile lie in jth bucket
          if (tempNthPercentileDataValueCount <= arrCumCount[j])
          {
            break;
          }
        }

        double percentileData = 0;

        // Percentile not lie in last bucket then get percentile value to Max
        // bucket range
        if ((j != (numGranule - 1)))
          percentileData = ((j + 1) * minGranule) * formulaUnitData;
        else
          percentileData = (j * minGranule) * formulaUnitData;

        arrPercentileData[i] = PercentileData.convTo3DigitDecimal(percentileData);
      }

      Log.debugLogAlways(className, "getArrPercentileData", "", "", "Percentile data = " + rptUtilsBean.doubleArrayToList(arrPercentileData) + ", length = " + arrPercentileData.length);

      return arrPercentileData;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getArrPercentileData", "", "", "Exception - ", e);
      return null;
    }
  }

  // This function return the PDF unit
  public String getPDFUnit()
  {
    return pdfUnit;
  }

  // This function return formula unit data
  public double getFormulaUnitData()
  {
    return formulaUnitData;
  }

  /**
   * This function return SlabInfo Array
   * 
   * @return
   */
  public SlabInfo[] getSlabInfo()
  {
    return slabInfo;
  }

  /**
   * set the mean data value that is calculated by rtgMessage.dat file
   * 
   * @param meanDataVal
   */
  public void setMeanPctDataVal(double meanDataVal)
  {
    Log.debugLog(className, "setMeanPctDataVal", "", "", "Method Start. Mean data value = " + meanDataVal);

    this.meanDataVal = meanDataVal;
  }

  /**
   * set the std-dev data value that is calculated by rtgMessage.dat file
   * 
   * @param stdDevDataValue
   */
  public void setStdDevDataVal(double stdDevDataValue)
  {
    Log.debugLog(className, "setStdDevDataVal", "", "", "Method Start. Std-dev data value = " + stdDevDataValue);

    this.stdDevDataValue = stdDevDataValue;
  }

  /**
   * this function return the mean value for graph to show mean line of PCT Data
   * for frequency distribution graph
   * 
   * @return
   */
  public double getMeanPctDataValForMeanLine()
  {
    Log.debugLog(className, "getMeanPctDataValForMeanLine", "", "", "Method Start.");

    try
    {
      double meanPctDataValLine = getMeanPctDataVal();
      Log.debugLog(className, "getMeanPctDataValForMeanLine", "", "", "Method Start. meanPctDataValLine = " + meanPctDataValLine);
      return meanPctDataValLine;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getMeanPctDataValForMeanLine", "", "", "Exception - ", e);
      return -1;
    }
  }
}

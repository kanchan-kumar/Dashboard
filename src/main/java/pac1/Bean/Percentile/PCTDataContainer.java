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
import pac1.Bean.GraphName.GraphNames;

public class PCTDataContainer
{
  private final String className = "PCTDataContainer";
  private double[] arrPCTCumDataBuckets = null;
  public int numGranule;
  public int minGranule;
  private double meanDataVal = -1;
  private double stdDevDataValue = -1;
  private int formulaNumber;
  private double formulaUnitData = 1;
  private String pdfUnit;
  private SlabInfo[] slabInfo = null;
  private GraphUniqueKeyDTO graphUniqueKeyDTO = null;
  private GraphNames graphNames = null;

  public PCTDataContainer(GraphUniqueKeyDTO graphUniqueKeyDTO, GraphNames graphNames)
  {
    this.graphUniqueKeyDTO = graphUniqueKeyDTO;
    this.graphNames = graphNames;
  }

  public GraphNames getGraphNames()
  {
    return graphNames;
  }

  public void setGraphNames(GraphNames graphNames)
  {
    this.graphNames = graphNames;
  }

  public void initPCTDataContainer()
  {
    try
    {
      Log.debugLogAlways(className, "initPCTDataContainer", "", "", "Method Called. graphUniqueKeyDTO = " + graphUniqueKeyDTO);
      int pdfId = this.graphNames.getPDFIdByGraphUniqueKeyDTO(graphUniqueKeyDTO);
      numGranule = this.graphNames.getPdfNames().getNumGranuleByPDFId(pdfId);
      minGranule = this.graphNames.getPdfNames().getMinGranuleByPDFId(pdfId);
      arrPCTCumDataBuckets = new double[numGranule];
      Arrays.fill(arrPCTCumDataBuckets, 0);
      formulaNumber = this.graphNames.getPdfNames().getFormulaNumberByPDFId(pdfId);
      pdfUnit = this.graphNames.getPdfNames().getPDFUnitByPDFId(pdfId);
      formulaUnitData = PercentileDataUtils.getFormulaUnitDataByFormulaNum(formulaNumber);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "initPCTDataContainer", "", "", "Exception - ", e);
    }
  }

  public GraphUniqueKeyDTO getGraphUniqueKeyDTO()
  {
    return graphUniqueKeyDTO;
  }

  public void setGraphUniqueKeyDTO(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    this.graphUniqueKeyDTO = graphUniqueKeyDTO;
  }

  // this function calculate graph PCT Data and set cummulative pct data
  public void calcAndSetPCTDataContainer(double[] pctData)
  {
    try
    {
      Log.debugLog(className, "calcAndSetPCTDataContainer", "", "", "Method Start.");
      for (int i = 0; i < numGranule; i++)
      {
        if (pctData[i] >= arrPCTCumDataBuckets[i])
        {
          arrPCTCumDataBuckets[i] = pctData[i] - arrPCTCumDataBuckets[i];
        }
        else
        {
          Arrays.fill(arrPCTCumDataBuckets, 0);
          Log.errorLog(className, "calcAndSetPCTDataContainer", "", "", "ERROR: this seq number data is less than to previous one, not a valid condition.");
        }
      }

      Log.debugLog(className, "calcAndSetPCTDataContainer", "", "", "PCT cumulative data = " + rptUtilsBean.doubleArrayToList(arrPCTCumDataBuckets) + ", length = " + arrPCTCumDataBuckets.length);
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "calcAndSetPCTDataContainer", "", "", "Exception - ", e);
    }
  }

  // This is the function to calculate and set custom group graph's data
  public void calcAndSetCustomGrpData(double[] arrRptData, int startSeq, int endSeq)
  {
    try
    {
      Log.debugLog(className, "calcAndSetCustomGrpData", "", "", "Method Start. Start Seq = " + startSeq + ", End Seq = " + endSeq);
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

  // this function return the array of cumulative PCT Data count
  public double[] getArrPCTCumDataBuckets()
  {
    return arrPCTCumDataBuckets;
  }

  // this function return the mean value of PCT Data for frequency distribution graph
  public double getMeanPctDataVal()
  {
    try
    {
      Log.debugLog(className, "getMeanPctDataVal", "", "", "Method Start.");
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
   * this function return the Std dev value of PCT Data for frequency distribution graph Std-Dev = square root of((sum of square - (sum * avg))/(N-1)) where N = number of buckets
   * 
   * @return
   */
  public double getStdDevDataVal()
  {
    try
    {
      Log.debugLog(className, "getStdDevDataVal", "", "", "Method Start.");
      Log.debugLog(className, "getStdDevDataVal", "", "", "Std dev Value = " + stdDevDataValue);
      return (PercentileData.convTo3DigitDecimal(stdDevDataValue));
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getStdDevDataVal", "", "", "Exception - ", e);
      return -1;
    }
  }

  // This function return the cum counts of each slabs for Slab Graph
  public double[] getArrSlabsData()
  {
    try
    {
      Log.debugLog(className, "getArrSlabsData", "", "", "Method Start.");
      int pdfId = this.graphNames.getPDFIdByGraphUniqueKeyDTO(graphUniqueKeyDTO);
      slabInfo = this.graphNames.getPdfNames().getArrSlabsInfoByPDFId(pdfId);
      double[] arrSlabsData = new double[slabInfo.length];
      for (int i = 0; i < slabInfo.length; i++)
      {
        int minS = slabInfo[i].getSlabMinValue();
        int maxS = slabInfo[i].getSlabMaxValue();
        double count = 0;
        if (maxS != -1) // If not tends to infinity
        {
          for (int j = (minS / minGranule); j < (maxS / minGranule); j++)
          {
            count += arrPCTCumDataBuckets[j];
          }
        }
        else
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

  // This function return the array of percentile data for Percentile Graph
  public double[] getArrPercentileData()
  {
    try
    {
      Log.debugLog(className, "getArrPercentileData", "", "", "Method Start.");
      double[] arrPercentileData = new double[100];
      double[] arrCumCount = new double[numGranule];
      double cumSum = 0;
      Arrays.fill(arrPercentileData, 0);
      Arrays.fill(arrCumCount, 0);
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

        // Percentile not lie in last bucket then get percentile value to Max bucket range
        if ((j != (numGranule - 1)))
          percentileData = ((j + 1) * minGranule) * formulaUnitData;
        else
          percentileData = (j * minGranule) * formulaUnitData;

        arrPercentileData[i] = PercentileDataUtils.convTo3DigitDecimal(percentileData);
      }

      Log.debugLog(className, "getArrPercentileData", "", "", "Percentile data = " + rptUtilsBean.doubleArrayToList(arrPercentileData) + ", length = " + arrPercentileData.length);
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
   * this function return the mean value for graph to show mean line of PCT Data for frequency distribution graph
   * 
   * @return
   */
  public double getMeanPctDataValForMeanLine()
  {
    try
    {
      Log.debugLog(className, "getMeanPctDataValForMeanLine", "", "", "Method Start.");
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
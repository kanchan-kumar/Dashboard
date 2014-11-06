package pac1.Bean;

import java.io.Serializable;

/**
 * This Class is used to Contain Information of Graph Data mapped to Graph DTO.<br>
 * Contains Data Arrays of Graph Sample Data, Min, Max, Count and Sum Square.<br>
 * Contains Graph Last Sample Values.<hr>
 * @version 1.0
 */
public class TimeBasedDTO implements Serializable
{
  private static final long serialVersionUID = 7676357283699600184L;
  private String className = "TimeBasedDTO";
    
  /*Keeping average data for each active Graph. This data will show in gui*/
  private double[] arrGraphSamplesData = null;
  private double[] arrMinData = null; /*array to store Min Data*/
  private double[] arrMaxData = null; /*array to store Max Data*/
  private int[] arrCountData = null; /*array to store counts/sample count*/
  private double[] arrSumSqrData = null; /*array to store sum square*/
  
  public double[] arrSeqNumber = null; /*keeping Sequence Number array*/
  public long[] arrTimeStamp = null; /*Keeping time stamp in array*/
  
  private GraphUniqueKeyDTO graphUniqueKeyDTO = null; /*Reference of Graph DTO.*/
  
  private double lastMinData = Double.MAX_VALUE; /*to store Last Min Data.*/
  private double lastMaxData; /*To store Last Max Data.*/
  private int lastCountData; /*To store Last counts/sample count.*/
  private double lastSumSqrData; /*To store Last sum square Data*/
  private double lastSampleData; /*Array to store Last Sample data For all active Graphs.*/
  private double lastAvgSampleData; /*This keep last sample data into memory*/

  /**
   * Initialize the DTO arrays with specified length.
   * @param arrSize
   */
  public void initTimeBasedDTO(int arrSize)
  {
    try
    {
      arrGraphSamplesData = new double[arrSize];
      arrMinData = new double[arrSize];
      arrMaxData = new double[arrSize];
      arrCountData = new int[arrSize];
      arrSumSqrData = new double[arrSize];
      
      /*Fill Arrays with identifier values.*/
      fillArrays();
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "initTimeBasedDTO", "", "", "Exception - ", e);
    }
  }

  /**
   * Return the Last Averaged Sample of Graph.
   * @return
   */
  public double getLastAvgSampleData()
  {
    return lastAvgSampleData;
  }

  /**
   * Sets the Last Averaged Sample of Graph.
   * @param lastAvgSampleData
   */
  public void setLastAvgSampleData(double lastAvgSampleData)
  {
    this.lastAvgSampleData = lastAvgSampleData;
  }

  /**
   * Return the Last Max Sample of Graph.
   * @return
   */
  public double getLastMaxData()
  {
    return lastMaxData;
  }

  /**
   * Set the last Max Sample of Graph.
   * @param lastMaxData
   */
  public void setLastMaxData(double lastMaxData)
  {
    this.lastMaxData = lastMaxData;
  }

  /**
   * Return the Last count sample of Graph.
   * @return
   */
  public int getLastCountData()
  {
    return lastCountData;
  }

  /**
   * Set the Last Count Sample of Graph.
   * @param lastCountData
   */
  public void setLastCountData(int lastCountData)
  {
    this.lastCountData = lastCountData;
  }

  /**
   * Return the Last Sum Square Sample of graph.
   * @return
   */
  public double getLastSumSqrData()
  {
    return lastSumSqrData;
  }

  /**
   * Set the last Sum Square Sample of Graph.
   * @param lastSumSqrData
   */
  public void setLastSumSqrData(double lastSumSqrData)
  {
    this.lastSumSqrData = lastSumSqrData;
  }

  /**
   * Return the Last Sample of Graph.
   * @return
   */
  public double getLastSampleData()
  {
    return lastSampleData;
  }

  /**
   * Set the Last Sample of Graph.
   * @param lastSampleData
   */
  public void setLastSampleData(double lastSampleData)
  {
    this.lastSampleData = lastSampleData;
  }

  /**
   * Return the last Min Sample of Graph.
   * @return
   */
  public double getLastMinData()
  {
    return lastMinData;
  }

  /**
   * Set the Last Min Sample of Graph.
   * @param lastMinData
   */
  public void setLastMinData(double lastMinData)
  {
    this.lastMinData = lastMinData;
  }

  /**
   * Return the DTO object of Graph, contains vector name , graph Id and group Id.
   * @return
   */
  public GraphUniqueKeyDTO getGraphUniqueKeyDTO()
  {
    return graphUniqueKeyDTO;
  }

  /**
   * Set the DTO object of Graph, contains vector name, graph Id and group Id.
   * @param graphUniqueKeyDTO
   */
  public void setGraphUniqueKeyDTO(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    this.graphUniqueKeyDTO = graphUniqueKeyDTO;
  }

  /**
   * Return the Average Sample Data array of Graph.
   * @return
   */
  public double[] getArrGraphSamplesData()
  {
    return arrGraphSamplesData;
  }

  /**
   * Set the Average Sample Data Array of Graph.
   * @param arrGraphSamplesData
   */
  public void setArrGraphSamplesData(double[] arrGraphSamplesData)
  {
    this.arrGraphSamplesData = arrGraphSamplesData;
  }

  /**
   * Return the Min Data Array of Graph.
   * @return
   */
  public double[] getArrMinData()
  {
    return arrMinData;
  }

  /**
   * Sets the Min Data Array of Graph.
   * @param arrMinData
   */
  public void setArrMinData(double[] arrMinData)
  {
    this.arrMinData = arrMinData;
  }

  /**
   * Return the Max Data Array of Graph.
   * @return
   */
  public double[] getArrMaxData()
  {
    return arrMaxData;
  }

  /**
   * Set the Max Data Array of Graph.
   * @param arrMaxData
   */
  public void setArrMaxData(double[] arrMaxData)
  {
    this.arrMaxData = arrMaxData;
  }

  /**
   * Return the Count Data Array of Graph.
   * @return
   */
  public int[] getArrCountData()
  {
    return arrCountData;
  }

  /**
   * Return the Count Data Array of Graph.
   * @param arrCountData
   */
  public void setArrCountData(int[] arrCountData)
  {
    this.arrCountData = arrCountData;
  }

  /**
   * Return the Sum Square Data Array of Graph.
   * @return
   */
  public double[] getArrSumSqrData()
  {
    return arrSumSqrData;
  }

  /**
   * Set the Sum Square Data Array of Graph.
   * @param arrSumSqrData
   */
  public void setArrSumSqrData(double[] arrSumSqrData)
  {
    this.arrSumSqrData = arrSumSqrData;
  }

  /**
   * Increase the Array Size with provided size.
   * @param incremented_size
   */
  public void increase1DArraySize(int incremented_size)
  {
    arrCountData = TimeBasedDataUtils.increase1DIntArraySize(arrCountData, incremented_size);
    arrGraphSamplesData = TimeBasedDataUtils.increase1DDoubleArraySize(arrGraphSamplesData, incremented_size);
    arrSumSqrData = TimeBasedDataUtils.increase1DDoubleArraySize(arrSumSqrData, incremented_size);
    arrMinData = TimeBasedDataUtils.increase1DDoubleArraySize(arrMinData, incremented_size);
    arrMaxData = TimeBasedDataUtils.increase1DDoubleArraySize(arrMaxData, incremented_size);
  }

  /**
   * Method is used to update the Sample Arrays and respected properties of Graph.
   * @param dataItemCount
   */
  public void updateAvgSampleData(int dataItemCount, int graphDataIndex)
  {
    try
    {     
      /*Here checking for graph with No Data value.*/
      if((graphDataIndex == -1) || (TimeBasedDataUtils.isEmptySample(lastAvgSampleData)))
      {	
	/*The Graph Value with No Data Identity shows, no sample Data available for this Graph.*/
	arrGraphSamplesData[dataItemCount] = lastAvgSampleData;
      }
      else
      {
	if (lastCountData != 0) /*Count can be 0 for times/timesSTD*/
	  arrGraphSamplesData[dataItemCount] = lastAvgSampleData / lastCountData;
	else
	  arrGraphSamplesData[dataItemCount] = 0;
      }
      
      /*Filling Min/Max/Count/Sum Square Averaged Data into Sample Data Arrays.*/
      arrMinData[dataItemCount] = lastMinData;
      arrMaxData[dataItemCount] = lastMaxData;
      arrCountData[dataItemCount] = lastCountData;
      arrSumSqrData[dataItemCount] = lastSumSqrData;

      /*Resetting All Arrays.*/
      lastAvgSampleData = 0.0;
      lastMinData = Double.MAX_VALUE;
      lastMaxData = 0.0;
      lastCountData = 0;
      lastSumSqrData = 0.0;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "updateAvgSampleData", "", "", "Exception - ", e);
    }
  }

  /**
   * Repeat last Sample Data in arrays with provided count/length. 
   * @param dataItemCount
   * @param repeatCount
   */
  public void repeatSamples(int dataItemCount, int repeatCount)
  {
    try
    {
      for(int j = dataItemCount; j < (dataItemCount + repeatCount); j++)
      {
        arrGraphSamplesData[j] = lastSampleData; /*assigning last sample value in req graph Sample data array*/
        arrMinData[j] = lastMinData; /*assigning last MinData value in req MinData array*/
        arrMaxData[j] = lastMaxData; /*assigning last MaxData value in req MaxData array*/
        arrCountData[j] = lastCountData; /*assigning last CountData value in req CountData array*/
        arrSumSqrData[j] = lastSumSqrData; /*assigning last SumSqrData value in req SumSqrData array*/
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "repeatSamples", "", "", "Exception - ", e);
    }
  }

  /**
   * Move Data Sample within arrays to keep data for specified Time only. 
   */
  public void mvDataValuesInArray()
  {
    try
    {
      int n = arrGraphSamplesData.length - 1;
      for(int j = 0; j < n; j++)
      {
        arrGraphSamplesData[j] = arrGraphSamplesData[j + 1];
        arrMinData[j] = arrMinData[j + 1];
        arrMaxData[j] = arrMaxData[j + 1];
        arrCountData[j] = arrCountData[j + 1];
        arrSumSqrData[j] = arrSumSqrData[j + 1];
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "mvDataValuesInArray", "", "", "Exception - ", e);
    }
  }
  
  /**
   * Method is used to Fill the NOT_AVAILABLE Identifier values in array.
   */
  private void fillArrays()
  {
    /*Filling arrays with identifier values.*/
    for(int i = 0; i < arrGraphSamplesData.length; i++)
    {
      arrGraphSamplesData[i] = DataPacketInfo.DOUBLE_NO_DATA_IDENTITY;
      arrCountData[i] = DataPacketInfo.INT_NO_DATA_IDENTITY;
      arrMinData[i] = DataPacketInfo.DOUBLE_NO_DATA_IDENTITY;
      arrMaxData[i] = DataPacketInfo.DOUBLE_NO_DATA_IDENTITY;
      arrSumSqrData[i] = DataPacketInfo.DOUBLE_NO_DATA_IDENTITY;
    }
  }

  /**
   * Override toString() method of object class for providing object specific information.
   */
  public String toString()
  {
    String s1 = graphUniqueKeyDTO + ", lastSampleData = " + lastSampleData + ", lastMinData = " + lastMinData + ", lastMaxData = " + lastMaxData;
    String s2 = "lastCountData = " + lastCountData + ", lastSumSqrData = " + lastSumSqrData + ", lastAvgSampleData = " + lastAvgSampleData;
    return s1 + "," + s2;
  }

  public static void main(String[] args)
  {

  }

}

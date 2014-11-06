package pac1.Bean;


/**
 * This Class is used for storing Graph Data Stats.</br>
 * This Class is used for dual purpose.</br>
 * 1. It is used to read graph Statistics form msgData object for each packet data while creating time based data. </br>
 * 2. It is used for creating lower pane data while loading graph in dashboard gui.
 *
 */
public class GraphStats implements java.io.Serializable, Cloneable
{
  private static final long serialVersionUID = -2937281645924316978L;

  //Used For Graph Data/averaged sample
  private double graphValue;
  
  //Used For Min Data.
  private double minData;
  
  //Used For Max Data. 
  private double maxData;
  
  //Used For count
  private int count;
  
  //Used For sum square
  private double sumSquare;
  
  //Used For Standard deviation.
  private double stdDev;
  
  //Used For Last Sample.
  private double lastSample;
  
  //Used For average.
  private double avgData;
  
  //Used For Graph Data Type (Sample/Rate/...)
  private int graphDataType;
  
  //Optional used to send Graph Name.
  private String graphName = "";
  
  /**
   * Used For getting Graph Data Value.
   * @return
   */
  public double getGraphValue() 
  {
    return graphValue;
  }

  /**
   * Used To Set Graph Data Value.
   * @param graphValue
   */
  public void setGraphValue(double graphValue) 
  {
    this.graphValue = graphValue;
  }


  /**
   * Used To get Graph Min Value.
   * @return
   */
  public double getMinData() 
  {
    return minData;
  }

 /**
 * Used To Set Graph Min Value.
 * @param minData
 */
  public void setMinData(double minData) 
  {
    this.minData = minData;
  }

  /**
  * Used To get Graph Max Value. 
  * @return
  */
  public double getMaxData() 
  {
    return maxData;
  }

  /**
   * Used To Set Graph Max Value.
   * @param maxData
   */
  public void setMaxData(double maxData) 
  {
    this.maxData = maxData;
  }


  /**
   * Used To Get Count.
   * @return
   */
  public int getCount() 
  {
    return count;
  }

  /**
   * Used To Set Count.
   * @param count
   */
  public void setCount(int count) 
  {
    this.count = count;
  }

  /**
   * Used To Get Sum Square value.
   * @return
   */
  public double getSumSquare() 
  {
    return sumSquare;
  }

  /**
   * Used To Set Sum Square value.
   * @param sumSquare
   */
  public void setSumSquare(double sumSquare) 
  {
    this.sumSquare = sumSquare;
  }

  /**
   * Used to get Standard Deviation.
   * @return
   */
  public double getStdDev() 
  {
    return stdDev;
  }

  /**
   * Used To Set Standard Deviation.
   * @param stdDev
   */
  public void setStdDev(double stdDev) 
  {
    this.stdDev = stdDev;
  }
  
  /**
   * Getting Last Sample Data.
   * @return
   */
  public double getLastSample() 
  {
    return lastSample;
  }

  /**
   * Setting Last Sample Data.
   * @param lastSample
   */
  public void setLastSample(double lastSample) 
  {
    this.lastSample = lastSample;
  }

  /**
   * Getting Average Data.
   * @return
   */
  public double getAvgData() 
  {
    return avgData;
  }

  /**
   * Setting Average Data.
   * @param avgData
   */
  public void setAvgData(double avgData) 
  {
    this.avgData = avgData;
  }
  
  /**
   * Getting Graph Data Type.
   * @return
   */
  public int getGraphDataType()
  {
    return graphDataType;
  }

  /**
   * Setting Graph Data Type.
   * @param graphDataType
   */
  public void setGraphDataType(int graphDataType) 
  {
    this.graphDataType = graphDataType;
  }
  
  /**
   * Getting Graph Name.
   * @return
   */
  public String getGraphName() 
  {
    return graphName;
  }

  /**
   * Setting Graph Name.
   * @param graphName
   */
  public void setGraphName(String graphName) 
  {
    this.graphName = graphName;
  }

  public String toString()
  {
    return "Graph Value = "+ graphValue + ", min = "+ minData + ", max = "+ maxData + ", count = "+ count + " sumSqr = "+sumSquare;  
  }

  public static void main(String[] args)
  {

  }

}

/**
 * This class is for Generating Percentile Data 
 * 
 * @author Itisha
 * 
 * @since Netsorm Version 4.0.0
 * @Modification_History Itisha - Initial Version 4.0.0
 * @version 4.0.0
 * 
 */
package pac1.Bean.Percentile;

public class PercentileFileOffset
{
  /**
   * This is for first file name from where we have read the first data packet(C1).
   * 
   * If it is null, then we dont need to read first data packet (C1 = 0).
   */
  private String startFileName = null;

  // This is for start partition name
  private String startPartitionName = null;

  // This is for end partition name
  private String endPartitionName = null;

  /**
   * This is the file name from where we have to read the last data packet(C2 / CL).
   * 
   * This must not be null.
   */
  private String endFileName = null;

  /**
   * This is for indicating which data packet is to be read from startFileName.
   * 
   * By default, it will be 0. It means we need to read first data packet from startFileName.
   */
  private int startOffset = 0;

  /**
   * This is for indicating which data packet is to be read from endFileName.
   * 
   * By default, it will be 0 means we need to read first data packet from endFileName.
   */
  private int endOffset = 0;

  /**
   * This is for storing start packet raw data. By default all values will be zero.
   * 
   * We need to initialize the all data values with zero.
   */
  private double[] startPacket = null;

  /**
   * This is for storing end packet raw data. By default all values will be zero.
   * 
   * We need to initialize the all data values with zero.
   */
  private double[] endPacket = null;

  public String getStartFileName()
  {
    return startFileName;
  }

  public void setStartFileName(String startFileName)
  {
    this.startFileName = startFileName;
  }

  public String getEndFileName()
  {
    return endFileName;
  }

  public void setEndFileName(String endFileName)
  {
    this.endFileName = endFileName;
  }

  public int getStartOffset()
  {
    return startOffset;
  }

  public void setStartOffset(int startOffset)
  {
    this.startOffset = startOffset;
  }

  public int getEndOffset()
  {
    return endOffset;
  }

  public void setEndOffset(int endOffset)
  {
    this.endOffset = endOffset;
  }

  public double[] getStartPacket()
  {
    return startPacket;
  }

  public void setStartPacket(double[] startPacket)
  {
    this.startPacket = startPacket;
  }

  public double[] getEndPacket()
  {
    return endPacket;
  }

  public void setEndPacket(double[] endPacket)
  {
    this.endPacket = endPacket;
  }

  public String getStartPartitionName()
  {
    return startPartitionName;
  }

  public void setStartPartitionName(String startPartitionName)
  {
    this.startPartitionName = startPartitionName;
  }

  public String getEndPartitionName()
  {
    return endPartitionName;
  }

  public void setEndPartitionName(String endPartitionName)
  {
    this.endPartitionName = endPartitionName;
  }

  public String toString()
  {
    return "startFileName = " + startFileName + ", startOffset = " + startOffset + ", endFileName = " + endFileName + ", endOffset = " + endOffset;
  }
}

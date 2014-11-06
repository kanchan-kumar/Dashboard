package pac1.Bean;

import java.util.ArrayList;
import java.util.SortedSet;

/**
 * This Class is used to keep data files information of Partition.
 * @version 1.0
 */
public class PartitionDataProperties 
{
  
  /*Duration of Partition.*/
  private long partitionDuration = 0L;
  
  /*List of Sequences/Versions of RTG/GDF Files.*/
  private ArrayList<Integer> arrPartitionSequenceFileList = null;
  
  /*List of size of all sequence/versions file corresponding to Sequence Number list.*/
  private ArrayList<Long> arrPartitionSequenceFileSizeList = null;

  /**
   * Getting the Duration of Partition.
   * @return
   */
  public long getPartitionDuration() 
  {
    return partitionDuration;
  }

  /**
   * Setting the duration of Partition.
   * @param partitionDuration
   */
  public void setPartitionDuration(long partitionDuration) 
  {
    this.partitionDuration = partitionDuration;
  }

  /**
   * Getting the list of sequence/versions of files inside Partition.
   * @return
   */
  public ArrayList<Integer> getArrPartitionSequenceFileList() 
  {
    return arrPartitionSequenceFileList;
  }

  /**
   * Setting the list of sequences/versions of files inside Partitions.
   * @param arrPartitionSequenceFileList
   */
  public void setArrPartitionSequenceFileList(
      ArrayList<Integer> arrPartitionSequenceFileList) 
  {
    this.arrPartitionSequenceFileList = arrPartitionSequenceFileList;
  }

  /**
   * Getting the list of size of sequences/versions of files inside Partitions.
   * @return
   */
  public ArrayList<Long> getArrPartitionSequenceFileSizeList() 
  {
    return arrPartitionSequenceFileSizeList;
  }

  /**
   * Setting the list of size of sequences/versions of files inside Partitions.
   * @param arrPartitionSequenceFileSizeList
   */
  public void setArrPartitionSequenceFileSizeList(
      ArrayList<Long> arrPartitionSequenceFileSizeList) 
  {
    this.arrPartitionSequenceFileSizeList = arrPartitionSequenceFileSizeList;
  }
  
  @Override
  public String toString()
  {
    return "Total Patition Duration = " + partitionDuration + ", Partition file sequence list = " + arrPartitionSequenceFileList + ", Partition sequence file size list = " + arrPartitionSequenceFileSizeList;   
  }
}

package pac1.Bean;

import java.io.Serializable;
import java.util.Vector;

/**
 * Method is used to get TestRun GDF and PDF information from Server.
 * @version 1.0
 */
public class GDFInfoDTO implements Serializable
{
  /*Serial Version UID.*/
  private static final long serialVersionUID = -7158542666024322860L;
  
  /*Test Run Number.*/
  private int testRun = -1;
  
  /*Active Partition Directory Name.*/
  private String activePartitionDirName = "NA";
  
  /*latest sequence number in active partition directory*/
  private int currentGDFVersion = 0;
  
  /*Vector List containing testrun.gdf information.*/
  private Vector<String> testRunGDFDataList = null;
  
  /*Vector List containing testrun.pdf information.*/
  private Vector<String> testRunPDFDataList = null;

  /**
   * Getting Test Run.
   * @return
   */
  public int getTestRun() 
  {
    return testRun;
  }

  /**
   * Setting Test Run.
   * @param testRun
   */
  public void setTestRun(int testRun) 
  {
    this.testRun = testRun;
  }

  /**
   * Getting Active Partition Directory Name.
   * @return
   */
  public String getActivePartitionDirName() 
  {
    return activePartitionDirName;
  }

  /**
   * Setting Active Partition Directory Name.
   * @param activePartitionDirName
   */
  public void setActivePartitionDirName(String activePartitionDirName) 
  {
    this.activePartitionDirName = activePartitionDirName;
  }

  /**
   * Getting Test Run GDF Information.
   * @return
   */
  public Vector<String> getTestRunGDFDataList() 
  {
    return testRunGDFDataList;
  }

  /**
   * Setting Test Run GDF Information.
   * @param testRunGDFDataList
   */
  public void setTestRunGDFDataList(Vector<String> testRunGDFDataList) 
  {
    this.testRunGDFDataList = testRunGDFDataList;
  }

  /**
   * Getting Test Run PDF Information.
   * @return
   */
  public Vector<String> getTestRunPDFDataList() 
  {
    return testRunPDFDataList;
  }

  /**
   * Setting Test Run PDF Information.
   * @param testRunPDFDataList
   */
  public void setTestRunPDFDataList(Vector<String> testRunPDFDataList) 
  {
    this.testRunPDFDataList = testRunPDFDataList;
  }

  /**
   * Getting current sequence number
   * @return
   */
  public int getCurrentGDFVersion() 
  {
    return currentGDFVersion;
  }

  /**
   * Setting current sequence number
   * @param currentSeqNum
   */
  public void setCurrentGDFVersion(int currentGDFVersion) 
  {
    this.currentGDFVersion = currentGDFVersion;
  }
  
  
}

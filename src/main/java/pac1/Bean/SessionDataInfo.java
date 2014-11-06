package pac1.Bean;

/**
 * This Class is used to keep information of session data files. <br>
 * How much data we need to read and from which file. 
 * It also keep track the start and end time stamp which we need to read from different session rtgMessge files.
 *
 */
public class SessionDataInfo
{
  /* This is containing the name of session data file.*/
  private String activeSessionDataFile = "";
  
  /* Keeping the information about the starting time stamp of session data file */
  private long startTimeStamp = 0L;
  
  /* This variable is used for starting data file which we need to read.
   * It contain startTimeStamp.*/
  private String firstSessionDataFile = "";
  
  /* This variable is used for ending session data file which we need to read.
   * It contain endTimeStamp.*/
  private String lastSessionDataFile = "";
  
  /* Keeping the information about the ending time stamp of session data file */
  private long endTimeStamp = 0L;
  
  /**
   * This Constructor initialize the object for storing information for reading/creating data.
   */
  public SessionDataInfo()
  {
  }

  /**
   * Gets the name of session data files.
   * @return
   */
  public String getActiveSessionDataFile() 
  {
    return activeSessionDataFile;
  }

  /**
   * Sets the name of active session data file.
   * @param sessionDataFile
   */
  public void setSessionDataFile(String sessionDataFile) 
  {
    this.activeSessionDataFile = sessionDataFile;
  }

  /**
   * Get the start Time stamp for reading first session data file.
   * @return
   */
  public long getStartTimeStamp() 
  {
    return startTimeStamp;
  }

  /**
   * Sets the start time stamp for reading first session data file.
   * @param startTimeStamp
   */
  public void setStartTimeStamp(long startTimeStamp) 
  {
    this.startTimeStamp = startTimeStamp;
  }

  /**
   * Getting Starting Session Data File to read.
   * @return
   */
  public String getFirstSessionDataFile() 
  {
    return firstSessionDataFile;
  }

  /**
   * Setting Starting Session data file to read.
   * @param firstSessionDataFile
   */
  public void setFirstSessionDataFile(String firstSessionDataFile) 
  {
    this.firstSessionDataFile = firstSessionDataFile;
  }

  /**
   * Getting Last Session data file to read.
   * @return
   */
  public String getLastSessionDataFile() 
  {
    return lastSessionDataFile;
  }

  /**
   * Setting Last Session data file to read.
   * @param lastSessionDataFile
   */
  public void setLastSessionDataFile(String lastSessionDataFile) 
  {
    this.lastSessionDataFile = lastSessionDataFile;
  }

  /**
   * Gets the end time stamp for reading last session data file.
   * @return
   */
  public long getEndTimeStamp() 
  {
    return endTimeStamp;
  }

  /**
   * Sets the end time stamp reading last session data file.
   * @param endTimeStamp
   */
  public void setEndTimeStamp(long endTimeStamp) 
  {
    this.endTimeStamp = endTimeStamp;
  }   
}

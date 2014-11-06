/**----------------------------------------------------------------------------
 * Name       TestControlData.java
 * Purpose    To contain pause/Resume status of running test run.
 *
 * @author    Atul
 * @version   3.5.0
 *
 * Modification History
 *   21/09/09:Atul:3.5.0 - Initial Version.
 *---------------------------------------------------------------------------**/

package pac1.Bean;

import java.io.Serializable;
import java.util.Date;

public class TestControlData implements Serializable
{
  private static final long serialVersionUID = -3948832150803169615L;
  private boolean isPaused;
  private int testRunNum;
  private Date prevTime;
  private Date curTime;
  private boolean isGDFChanged = false;
  private String activeParitionName = "NA";
  private int versionNumber = 0;
  
  public void setIsPaused(boolean isPaused)
  {
    this.isPaused = isPaused;
  }
  
  public void setTestRunNum(int testRunNum)
  {
    this.testRunNum = testRunNum;
  }
  
  public int getTestRunNum()
  {
    return this.testRunNum;
  }
  
  public boolean isPaused()
  {
    return this.isPaused;
  }
  
  public void setCurTime(long curTime)
  {
    this.prevTime = this.curTime;
    this.curTime = new Date(curTime);
  }
  
  public Date getPrevTime()
  {
    return this.prevTime;
  }
  
  public Date getCurTime()
  {
    return this.curTime;
  }
  
  /**
   * Method is used to check if GDF is changed in online test run.
   * @return
   */
  public boolean isGDFChanged() 
  {
    return isGDFChanged;
  }

  /**
   * Method is used to set the flag if GDF file in changed.
   * @param isGDFChanged
   */
  public void setGDFChanged(boolean isGDFChanged) 
  {
    this.isGDFChanged = isGDFChanged;
  }
  
  /**
   * Method is used to get Active Partition Name.
   * @return
   */
  public String getActiveParitionName() 
  {
    return activeParitionName;
  }

  /**
   * Method is used to set Active Partition Name.
   * @param activeParitionName
   */
  public void setActiveParitionName(String activeParitionName) 
  {
    this.activeParitionName = activeParitionName;
  }
  
  /**
   * Getting Partition File Version Number.
   * @return
   */
  public int getVersionNumber() 
  {
    return versionNumber;
  }

  /**
   * Setting Partition File Version Number.
   * @param versionNumber
   */
  public void setVersionNumber(int versionNumber) 
  {
    this.versionNumber = versionNumber;
  }

  /**
   * This will be used for setting data to the client 
   * side with updated data came from server.
   * 
   * As we CANNOT created new TestControlData object because then it will 
   * NOT reflect data in progress Bar.
   * 
   * This will called on server TestControlData and client PasuedData 
   * will be passed.
   * 
   * @param destiPausedData
   */
  public static void copyData(TestControlData srcTestCtrlData, TestControlData destiTestCtrlData)
  {
    destiTestCtrlData.isPaused = srcTestCtrlData.isPaused;
    destiTestCtrlData.curTime = srcTestCtrlData.curTime;
    destiTestCtrlData.prevTime = srcTestCtrlData.prevTime;
    destiTestCtrlData.isGDFChanged = srcTestCtrlData.isGDFChanged;   
    destiTestCtrlData.activeParitionName = srcTestCtrlData.activeParitionName;
    destiTestCtrlData.versionNumber = srcTestCtrlData.versionNumber;
  }
}
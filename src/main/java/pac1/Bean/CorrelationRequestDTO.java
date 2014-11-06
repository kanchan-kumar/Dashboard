package pac1.Bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class is used to Sending/Requesting/Getting Correlation Graphs.
 * @version 1.0
 */
public class CorrelationRequestDTO implements Serializable, Cloneable
{
  
  /*Serial Version Id.*/
  private static final long serialVersionUID = -1800289600050686541L;

  /*Identify request for all Graphs.*/
  private boolean calcAllIndex = false;
  
  /*Identify request to skip cumulative Graphs.*/
  private boolean skipCumGraph = false;
  
  /*Identify request to skip times/timesSTD Graphs.*/
  private boolean skipTimesAndTimesStd = false;
  
  /*Identify request to include Anti Correlation.*/
  private boolean isIncludeAntiFlag = false;
  
  /*Variable store correlation Threshold value.*/
  private double corrThreshold = 0.0;
  
  /*Variable store the Test Run Number.*/
  private int testRun = 0;
  
  /*Variable Stores the NAR File Name.*/
  private String narFileName = null;
  
  /*Variable Stores the Start Time.*/
  private String startTime = null;
  
  /*Variable Stores the End Time.*/
  private String endTime = null;
  
  /*Object Stores the Baseline Graph DTO.*/
  private GraphUniqueKeyDTO baselineGraphUniqueKeyDTO = null;
  
  /*Graph DTO array includes selected Graphs.*/
  private GraphUniqueKeyDTO []arrGraphUniqueKeyDTO = null;
  
  /*Containing object of Time Based with graph Data.*/
  private TimeBasedTestRunData timeBasedTestRunDataObj = null;
  
  /*Correlation Requested Graph Detail.*/
  private String corrRequestedGraphDetail = null;
  
  /*Flag for checking the correlation status.*/
  private boolean isSuccessful = false;
  
  /*Error Message.*/
  private String errorMsg = null;
  
  /*Graph DTO of Processed Graphs.*/
  private ArrayList<GraphUniqueKeyDTO> arrProcessedGraphDTOList = new ArrayList<GraphUniqueKeyDTO>();
  
  /*Alert Data Array.*/
  private String[][] arrAlertData = null;
    
  /**
   * Checking is Correlation Request for all Graphs.
   * @return
   */
  public boolean isCalcAllIndex() {
    return calcAllIndex;
  }
  
  /**
   * Setting Correlation Request For all Graphs.
   * @param calcAllIndex
   */
  public void setCalcAllIndex(boolean calcAllIndex) {
    this.calcAllIndex = calcAllIndex;
  }

  /**
   * Checking For Skipping Cumulative Graphs.
   * @return
   */
  public boolean isSkipCumGraph() {
    return skipCumGraph;
  }

  /**
   * Setting value to skip Cumulative Graphs.
   * @param skipCumGraph
   */
  public void setSkipCumGraph(boolean skipCumGraph) {
    this.skipCumGraph = skipCumGraph;
  }

  /**
   * Checking For Skipping Times/TimesSTD Graphs.
   * @return
   */
  public boolean isSkipTimesAndTimesStd() {
    return skipTimesAndTimesStd;
  }

  /**
   * Setting For Skipping Times/TimesSTD Graphs
   * @param skipTimesAndTimesStd
   */
  public void setSkipTimesAndTimesStd(boolean skipTimesAndTimesStd) {
    this.skipTimesAndTimesStd = skipTimesAndTimesStd;
  }

  /**
   * Checking For Including Anti Correlation.
   * @return
   */
  public boolean isIncludeAntiFlag() {
    return isIncludeAntiFlag;
  }

  /**
   * Setting For Including Anti Correlation.
   * @param isIncludeAntiFlag
   */
  public void setIncludeAntiFlag(boolean isIncludeAntiFlag) {
    this.isIncludeAntiFlag = isIncludeAntiFlag;
  }

  /**
   * Getting Correlation Threshold value.
   * @return
   */
  public double getCorrThreshold() {
    return corrThreshold;
  }

  /**
   * Setting Correlation Threshold value.
   * @param corrThreshold
   */
  public void setCorrThreshold(double corrThreshold) {
    this.corrThreshold = corrThreshold;
  }

 /**
  * Getting Test Run.
  * @return
  */
  public int getTestRun() {
    return testRun;
  }

  /**
   * Setting Test Run.
   * @param testRun
   */
  public void setTestRun(int testRun) {
    this.testRun = testRun;
  }

  /**
   * Getting Nar File Name.
   * @return
   */
  public String getNarFileName() {
    return narFileName;
  }

  /**
   * Setting Nar File Name.
   * @param narFileName
   */
  public void setNarFileName(String narFileName) {
    this.narFileName = narFileName;
  }

  /**
   * Getting Start Time.
   * @return
   */
  public String getStartTime() {
    return startTime;
  }

  /**
   * Setting Start Time.
   * @param startTime
   */
  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  /**
   * Getting End Time.
   * @return
   */
  public String getEndTime() {
    return endTime;
  }

  /**
   * Setting End Time.
   * @param endTime
   */
  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }

  /**
   * Getting Baseline Graph DTO.
   * @return
   */
  public GraphUniqueKeyDTO getBaselineGraphUniqueKeyDTO() {
    return baselineGraphUniqueKeyDTO;
  }

  /**
   * Setting Baseline Graph DTO.
   * @param baselineGraphUniqueKeyDTO
   */
  public void setBaselineGraphUniqueKeyDTO(
      GraphUniqueKeyDTO baselineGraphUniqueKeyDTO) {
    this.baselineGraphUniqueKeyDTO = baselineGraphUniqueKeyDTO;
  }

  /**
   * Getting Array of Correlated Graphs.
   * @return
   */
  public GraphUniqueKeyDTO[] getArrGraphUniqueKeyDTO() {
    return arrGraphUniqueKeyDTO;
  }

  /**
   * Setting Array of Correlated Graphs.
   * @param arrGraphUniqueKeyDTO
   */
  public void setArrGraphUniqueKeyDTO(GraphUniqueKeyDTO []arrGraphUniqueKeyDTO) {
    this.arrGraphUniqueKeyDTO = arrGraphUniqueKeyDTO;
  }

  /**
   * Getting Time Based Object.
   * @return
   */
  public TimeBasedTestRunData getTimeBasedTestRunDataObj() {
    return timeBasedTestRunDataObj;
  }

  /**
   * Setting Time Based Object.
   * @param timeBasedTestRunDataObj
   */
  public void setTimeBasedTestRunDataObj(
      TimeBasedTestRunData timeBasedTestRunDataObj) {
    this.timeBasedTestRunDataObj = timeBasedTestRunDataObj;
  }
  
  /**
   * Checking the status of Correlation Operation.
   * @return
   */
  public boolean isSuccessful() {
    return isSuccessful;
  }

  /**
   * Setting the status of Correlation Operation.
   * @param isSuccessful
   */
  public void setSuccessful(boolean isSuccessful) {
    this.isSuccessful = isSuccessful;
  }

  /**
   * Getting Error Message.
   * @return
   */
  public String getErrorMsg() {
    return errorMsg;
  }

  /**
   * Setting Error Message.
   * @param errorMsg
   */
  public void setErrorMsg(String errorMsg) {
    this.errorMsg = errorMsg;
  }
  
  /**
   * Getting Graph Detail.
   * @return
   */
  public String getCorrRequestedGraphDetail() {
    return corrRequestedGraphDetail;
  }

  /**
   * Setting Graph Detail.
   * @param corrRequestedGraphDetail
   */
  public void setCorrRequestedGraphDetail(String corrRequestedGraphDetail) {
    this.corrRequestedGraphDetail = corrRequestedGraphDetail;
  }
  
  /**
   * Getting Graph DTO of Processed Graph.
   * @return
   */
  public ArrayList<GraphUniqueKeyDTO> getArrProcessedGraphDTOList() {
    return arrProcessedGraphDTOList;
  }

  /**
   * Setting Graph DTO of Processed Graph.
   * @param arrProcessedGraphDTOList
   */
  public void setArrProcessedGraphDTOList(
      ArrayList<GraphUniqueKeyDTO> arrProcessedGraphDTOList) {
    this.arrProcessedGraphDTOList = arrProcessedGraphDTOList;
  }
  
  /**
   * Getting Alert File Data.
   * @return
   */
  public String[][] getArrAlertData() {
    return arrAlertData;
  }

  /**
   * Setting Alert File Data.
   * @param arrAlertData
   */
  public void setArrAlertData(String[][] arrAlertData) {
    this.arrAlertData = arrAlertData;
  }

  /**
   * Cloning Object through Clonable interface.
   */
  public DerivedDataDTO clone() 
  {
    try 
    {
       return (DerivedDataDTO) super.clone();
    } 
    catch (CloneNotSupportedException e) 
    {        
      Log.errorLog("CorrelationRequestDTO", "CorrelationRequestDTO:clone", "", "", "Error in Cloning CorrelationRequestDTO object"+ e.getMessage());
      return null;
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }
}

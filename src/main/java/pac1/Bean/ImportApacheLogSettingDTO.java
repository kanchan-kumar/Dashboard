/**----------------------------------------------------------------------------
 * Name      ImportApacheLogSetting.java
 * Purpose   set all data Object
 * @author   Rohit Kumar Tiwari
 *---------------------------------------------------------------------------**/
package pac1.Bean;

import java.util.ArrayList;
import java.io.Serializable;
public class ImportApacheLogSettingDTO implements Serializable
{
  private ArrayList selectedFileList;
  private int urlField = 6;
  private int responseTimeField = 8;
  private int statusField = 7;
  private int dataSizeField = 5;
  private int dateTimeField = 4;
  private boolean isForAllUrl;
  private String vectorName;
  private boolean isForSpecificUrl;
  private ArrayList urlNameArrayList;
  private ArrayList urlVectorNameArrayList;
  private String dateTimeFormat;
  private String startTestRunTime;
  private String endTestRunTime;
  private String sepratorField = "space";
  private boolean standerd;
  private String timeZoneForTestRun;
  private String dateTimeFormatForTestRun;
  private String importData;
  private int importDataFlag;
  private String timeZone;
  private String timeFormat;
  private int dateTimeSelectionZoneFlag;
  private boolean isRequestLine = true;
  private boolean isFormClient = true;
  private int interval = 10000;
  //static variable for import data
  public static final int IMPORT_DATA_TEST_RUN = 0;
  public static final int IMPORT_DATA_FOR_PERCENTAGE_TEST_RUN = 1;
  public static final int IMPORT_DATA_FOR_SPECFICTIME_TEST_RUN = 2;

  //static variable for Time Zone
  public static final int SYNCHRONIZE_TIME_ZONE = 0;
  public static final int USER_DEFINED_TIME = 1;
  public static final int SAME_TIME_ZONE = 2;
  public static final int SYNCHRONIZE_WITH_TESTRUN = 3;
  public static final int SYNCHRONIZE_WITH_LOGFILE = 4;;


  /*---------------- Starting of getter methods ---------------*/

  public boolean getStanderd()
  {
    return standerd;
  }

  public String getDateTimeFormatForTestRun()
  {
    return dateTimeFormatForTestRun;
  }

  public ArrayList getSelectedFileList()
  {
    return selectedFileList;
  }

  public int getUrlField()
  {
    return urlField;
  }

  public int getResponseTimeField()
  {
    return responseTimeField;
  }

  public int getStatusField()
  {
    return statusField;
  }

  public int getDataSizeField()
  {
    return dataSizeField;
  }

  public int getDateTimeField()
  {
    return dateTimeField;
  }

  public boolean isForAllUrl()
  {
    return isForAllUrl;
  }

  public String getVectorName()
  {
    return vectorName;
  }

  public boolean isForSpecificUrl()
  {
    return isForSpecificUrl;
  }

  public ArrayList getUrlVectorName()
  {
    return urlVectorNameArrayList;
  }

  public ArrayList getUrlName()
  {
    return urlNameArrayList;
  }

  public String getDateTimeFormat()
  {
    return dateTimeFormat;
  }

  public String getStartTestRunTime()
  {
    return startTestRunTime;
  }

  public String getEndTestRunTime()
  {
    return endTestRunTime;
  }

  public String getSepratorField()
  {
    return sepratorField;
  }

  public String getImportDataTimeSelection()
  {
    return importData;
  }

  public int getImportDataTimeSelectionFlag()
  {
    return importDataFlag;
  }

  public String getTimeZoneForTestRun()
  {
    return timeZoneForTestRun;
  }

  public int getDateTimeSelectionZoneFlag()
  {
    return dateTimeSelectionZoneFlag;
  }

  public String getDateTimeSelectionZone()
  {
    return timeZone;
  }

  public String getResponseTimeUnit()
  {
    return timeFormat;
  }
  public boolean isRequestLineUsed()
  {
   return isRequestLine;
  }
 
  public int getInterval()
  {
    return interval;
  }
 
  public boolean isFormClientUsed()
  {
   return isFormClient;
  }

  /***************** End of getter methods *************************/


  /***************** Start of setter methods **********************/
  
  public void isRequestLineUsed(boolean isRequestLine)
  {
    this.isRequestLine = isRequestLine;
  }

  public void setTimeZoneForTestRun(String timeZoneForTestRun)
  {
    this.timeZoneForTestRun = timeZoneForTestRun;
  }

  public void setDateTimeFormatForTestRun(String dateTimeFormatForTestRun)
  {
    this.dateTimeFormatForTestRun = dateTimeFormatForTestRun;
  }

  public void setStanderd(boolean standerd)
  {
    this.standerd = standerd;
  }

  public void setSelectedFileList(ArrayList selectedFileList)
  {
    this.selectedFileList = selectedFileList;
  }

  public void setImportDataTimeSelectionFlag(int importDataFlag)
  {
    this.importDataFlag = importDataFlag;
  }

  public void setImportDataTimeSelection(String importData)
  {
    this.importData = importData;
  }

  public void setSepratorField(String sepratorField)
  {
    this.sepratorField = sepratorField;
  }

  public void setEndTestRunTime(String endTestRunTime)
  {
    this.endTestRunTime = endTestRunTime;
  }

  public void setStartTestRunTime(String startTestRunTime)
  {
    this.startTestRunTime = startTestRunTime;
  }

  public void setDateTimeFormat(String dateTimeFormat)
  {
    this.dateTimeFormat = dateTimeFormat;
  }

  public void setUrlName(ArrayList urlNameArrayList)
  {
    this.urlNameArrayList = urlNameArrayList;
  }

  public void setUrlVectorName(ArrayList urlVectorNameArrayList)
  {
    this.urlVectorNameArrayList = urlVectorNameArrayList;
  }

  public void setUrlField(int urlField)
  {
    this.urlField = urlField;
  }

  public void setResponseTimeField(int responseTimeField)
  {
    this.responseTimeField = responseTimeField;
  }

  public void setForSpecificUrl(boolean isForSpecificUrl)
  {
    this.isForSpecificUrl = isForSpecificUrl;
  }

  public void setStatusField(int statusField)
  {
    this.statusField = statusField;
  }

  public void setDataSizeField(int dataSizeField)
  {
    this.dataSizeField = dataSizeField;
  }

  public void setDateTimeField(int dateTimeField)
  {
    this.dateTimeField = dateTimeField;
  }

  public void setForAllUrl(boolean isForAllUrl)
  {
    this.isForAllUrl = isForAllUrl;
  }

  public void setVectorName(String vectorName)
  {
    this.vectorName = vectorName;
  }

  public void setDateTimeSelectionZoneFlag(int dateTimeSelectionZoneFlag)
  {
    this.dateTimeSelectionZoneFlag = dateTimeSelectionZoneFlag;
  }

  public void setDateTimeSelectionZone(String timeZone)
  {
    this.timeZone = timeZone;
  }

  public void setResponseTimeUnit(String timeFormat)
  {
    this.timeFormat = timeFormat;
  }
  
  public void setInterval(int interval)
  {
    this.interval = interval;
  }
  
  public void isFormClientUsed(boolean isFormClient)
  {
    this.isFormClient = isFormClient;
  }

  /************************ End of Setter method *****************/

}

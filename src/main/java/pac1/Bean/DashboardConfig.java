/**----------------------------------------------------------------------------
 * Name       DashboardConfig.java
 * Purpose    This is to keep all dashboard config variables.
 *
 * @author    Arun Goel
 * @version   3.9.2.
 *
 * Modification History
 *
 *
 * Notes
 *---------------------------------------------------------------------------**/

package pac1.Bean;

import java.util.ArrayList;

public class DashboardConfig implements java.io.Serializable
{
  private static final long serialVersionUID = 1828378429004263921L;
  private static String className = "DashboardConfig";
  // By Default, Sorting will be enable
  private int treeSortingMode = 1;

  // 0 - disable 1 - enable
  private int enalbelGenerator = 0;
  // Vairbale to keep debug level. By this level debug logs will print
  int debugLevel = 0;

  // This is for percentile numbers for dashboard GUI
  private int[] arrDashboardPercentileNumbers = new int[] {50,80,90,95,99};
  
  private int[] arrTxPercentileNumbers = new int[0];
  
  // This is for percentile graph refresh interval
  private int percentileGraphRefreshInterval = 5;
  
  // Variable to keep profile mode. It can have different levels by which different timing size logs will print
  int profileMode = 1;

  // Variables to define the default values of High/Low water mark of graph time series hash map
  int gtsHighWaterMark = 500;
  int gtsLowWaterMark = 100;

  // Variable to define if Auto Activate is on or off/
  // If this keyword is on then we dont get the data for all graphs.
  // We only get the data for graphs which are in all favorties
  String enableAutoActivate = "off";

  // Variable to define whether user can change the quantity in online test or not
  String enableRunTimeChangeQuantity = "on";

  // Variable to get the image of water mark which wiil show in background of chart
  String waterMarkImage = "waterMarkImage.png";

  // Variable to get the value of maxAddRunTimeChangeQuantity.It is used to define how much maximum quantity user can change at run time
  int maxAddRunTimeChangeQuantity = 10000;

  // Variable to get the the value of mininum bubble size in GEO Map
  int geoMapMinBubbleSize = 5;

  // Variable to get the value of maximum bubble size in GEO Map
  int geoMapMaxBubbleSize = 20;

  // Variable to get the value of High Water mark for TestRunData
  int activeOnlineGraphsHWM = 5000;

  // Variable to get the value of Low Water mark for TestRunData
  int activeOnlineGraphsLWM = 3000;

  // Variable to get value of alert interval. System will check the alerts after every this time.
  String alertScheduleInterval = "NA";

  // variable to keep the value of RTG_MAX_DATA_SAMPLES_IN_ONE_GRAPH in config.ini.
  // This keyword to define the number of maximum samples need to show in graph of dashboard
  int rtgMaxDataSamplesInOneGraph = 40;

  // Used to identify the test run number of NDE Continuous Mode.
  private int nDE_Test_Run = -1;

  // Used to set Requested N Minute Time which is configured with netstorm.execution.defaultDataView keyword.
  private String dataView = "Last_240_Minutes";

  // how many types of tbtrd may keep in hash map. default 10
  private int tbtrdLimitInMemory = 10;
  
  private int breadCrumbsLevel = 5;
  
  // This variable is used for Tree Color/Icon enable on Hierarchical view.
  // Default is 0 and 1 - enable
  private int enableTreeColorIcon = 0;
  
  //This variable is used to wait for 1 secs to getting response from the shell
  //in Thread dump, Heap dump, Memory leak and flight recorder
  private int waitTime = 300;

  /**
   * @return the tbtrdLimitInMemory
   */
  public int getTbtrdLimitInMemory()
  {
    return tbtrdLimitInMemory;
  }
  
  public int getBreadCrumbsLevel()
  {
    return breadCrumbsLevel;
  }

  /**
   * @param tbtrdLimitInMemory the tbtrdLimitInMemory to set
   */
  public void setTbtrdLimitInMemory(int tbtrdLimitInMemory)
  {
    this.tbtrdLimitInMemory = tbtrdLimitInMemory;
  }

  public DashboardConfig()
  {
    try
    {
      Log.debugLog(className, "DashboardConfig", "", "", "Default contructor Called.");
      loadDashboardConfigurations();
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "DashboardConfig", "", "", "Exception - ", ex);
    }
  }

  // Method to get all dashboard valiable values from config file
  private void loadDashboardConfigurations()
  {
    Config.loadConfigFile();
   
    breadCrumbsLevel = getConfigIntVal("netstorm.execution.breadCrumbsLevel", 5);
    
    tbtrdLimitInMemory = getConfigIntVal("netstorm.execution.maxTBTRD", 10); 
    
    // getting the value of debugLevel
    debugLevel = getConfigIntVal("netstorm.execution.debugLevel", 0);

    percentileGraphRefreshInterval = getConfigIntVal("netstorm.execution.percentiles.refreshInterval", 5);
    
    if(percentileGraphRefreshInterval <= 0)
      percentileGraphRefreshInterval = 5;
    
    // To set percentile numbers for dashboard GUI
    setDashboardPercentileNumbers();
    
    // to set percentile numbers for Tx GUI
    setTxPercentileNumbers();
    
    treeSortingMode = getConfigIntVal("netstorm.execution.treeSortingMode", 1);

    enalbelGenerator = getConfigIntVal("execution.enableGenerators", 0);

    // getting the value of profileMode
    profileMode = getConfigIntVal("netstorm.execution.profileMode", 1);

    // getting the value for High water mark of graph time series
    gtsHighWaterMark = getConfigIntVal("netstorm.execution.GTS.highWaterMark", 500);

    // getting the value for Low water mark of graph time series
    gtsLowWaterMark = getConfigIntVal("netstorm.execution.GTS.lowWaterMark", 100);

    // getting the value for Auto Active keyword
    enableAutoActivate = getConfigStringVal("netstorm.execution.enableAutoActivate", "off");

    // getting the value of keyword enableRunTimeChangeQuantity
    enableRunTimeChangeQuantity = getConfigStringVal("netstorm.dashboard.enableRunTimeChangeQuantity", "on");

    // getting image name of water mark. It is used show in the background of every graph
    waterMarkImage = getConfigStringVal("netstorm.common.waterMarkImage", "waterMarkImage.png");

    // getting maxAddRunTimeChangeQuantity value. It will define how much maximum quantity we can change at run time
    maxAddRunTimeChangeQuantity = getConfigIntVal("netstorm.dashboard.maxAddRunTimeChangeQuantity", 10000);

    // getting value of mininum bubble size in GEO Map
    geoMapMinBubbleSize = getConfigIntVal("netstorm.geoMap.minBubbleSize", 5);

    // getting value of maximum bubble size in GEO Map
    geoMapMaxBubbleSize = getConfigIntVal("netstorm.geoMap.maxBubbleSize", 20);

    // getting value of High Water mark for TestRunData
    activeOnlineGraphsHWM = getConfigIntVal("netstorm.execution.activeOnlineGraphsHWM", 5000);

    // getting value of Low Water mark for TestRunData
    activeOnlineGraphsLWM = getConfigIntVal("netstorm.execution.activeOnlineGraphsLWM", 3000);

    // getting value of alert interval
    alertScheduleInterval = getConfigStringVal("netstorm.alert.schedule.interval", "NA");

    // geting the calue of RTG_MAX_DATA_SAMPLES_IN_ONE_GRAPH
    rtgMaxDataSamplesInOneGraph = getConfigIntVal("RTG_MAX_DATA_SAMPLES_IN_ONE_GRAPH", 40);

    // Getting testrun value for NDE Continuous mode.
    nDE_Test_Run = getConfigIntVal("nde.testRunNum", -1);

    dataView = getConfigStringVal("netstorm.execution.defaultDataView", "Last_240_Minutes");

    // Getting the value of enableTreeColorIcon
    enableTreeColorIcon = getConfigIntVal("execution.enableTreeColorIcon", 0);
    
    waitTime = getConfigIntVal("netstorm.execution.waitTime", 300);
    
    if (debugLevel > 0)
    {
      Log.debugLogAlways(className, "loadDashboardConfigurations", "", "", "profileMode = " + profileMode + ", gtsHighWaterMark = " + gtsHighWaterMark + ", gtsLowWaterMark = " + gtsLowWaterMark + ", enableAutoActivate = " + enableAutoActivate + ", enableRunTimeChangeQuantity = " + enableRunTimeChangeQuantity + ", waterMarkImage = " + waterMarkImage + ", maxAddRunTimeChangeQuantity = " + maxAddRunTimeChangeQuantity + ", geoMapMinBubbleSize = " + geoMapMinBubbleSize + ", geoMapMaxBubbleSize = " + geoMapMaxBubbleSize + ", activeOnlineGraphsHWM = " + activeOnlineGraphsHWM + ", activeOnlineGraphsLWM = " + activeOnlineGraphsLWM + ", alertScheduleInterval = " + alertScheduleInterval + ", rtgMaxDataSamplesInOneGraph = " + rtgMaxDataSamplesInOneGraph + ", waitTime = " + waitTime);
    }
  }

  // common method to get config value of any keyword in integer.
  // If the value is not numeric then we will return its default value in catch
  private int getConfigIntVal(String name, int defaultValue)
  {
    try
    {
      return Integer.parseInt(((Config.getValue(name)).equals("")) ? defaultValue + "" : Config.getValue(name));
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getConfigIntVal", "", "", "", ex);
      Log.errorLog(className, "getConfigIntVal", "", "", "in config.ini, " + name + " value is not correct.");
      return defaultValue;
    }
  }

  /**
   * This method is for getting percentile numbers from config.ini
   * 
   * @return
   */
  public void setDashboardPercentileNumbers()
  {
    try
    {
      String percentileValues = getConfigStringVal("netstorm.execution.percentiles", "50,80,90,95,99");
      
      if(debugLevel > 0)
        Log.debugLogAlways(className, "setDashboardPercentileNumbers", "", "", "Method Called. percentileValues = " + percentileValues);

      String[] arrNthPercentile = rptUtilsBean.strToArrayData(percentileValues, ",");
      ArrayList<Integer> arrPercentileNum = new ArrayList<Integer>();
      int[] configuredNthPercentileValue = new int[arrNthPercentile.length];

      for (int i = 0; i < configuredNthPercentileValue.length; i++)
      {
        int percentileNumber = Integer.parseInt(arrNthPercentile[i].trim());
        if (percentileNumber > 100 && percentileNumber <= 0)
        {
          Log.errorLog(className, "setDashboardPercentileNumbers", "", "", "percentileValue in config.ini cannot be greater then 100. So using default value.");
          continue;
        }

        if (arrPercentileNum.contains(percentileValues))
          continue;
        
        arrPercentileNum.add(percentileNumber);
      }

      Log.debugLogAlways(className, "setDashboardPercentileNumbers", "", "", "Percentile Numbers = " + arrPercentileNum);
      
      if(arrPercentileNum.size() != 0)
      {
        configuredNthPercentileValue = rptUtilsBean.convertArrayListToIntArray(arrPercentileNum);
        arrDashboardPercentileNumbers = configuredNthPercentileValue;
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "setDashboardPercentileNumbers", "", "", "Exception - ", e);
    }
  }
  
  /**
   * This method is for getting percentile numbers from config.ini for transaction
   * 
   * @return
   */
  public void setTxPercentileNumbers()
  {
    try
    {
      String percentileValues = getConfigStringVal(ConfigKeywordDefinition.PERCENTILE_KEYWORD_TRANSACTION, null);
      
      if(debugLevel > 0)
        Log.debugLogAlways(className, "setTxPercentileNumbers", "", "", "Method Called. percentileValues = " + percentileValues);

      if(percentileValues == null)
      {
        Log.debugLogAlways(className, "setTxPercentileNumbers", "", "", "Percentile keyword for tx is off.");
        return;
      }
      
      String[] arrNthPercentile = rptUtilsBean.strToArrayData(percentileValues, ",");
      ArrayList<Integer> arrPercentileNum = new ArrayList<Integer>();
      int[] configuredNthPercentileValue = new int[arrNthPercentile.length];

      for (int i = 0; i < configuredNthPercentileValue.length; i++)
      {
        int percentileNumber = Integer.parseInt(arrNthPercentile[i].trim());
        if (percentileNumber > 100 && percentileNumber <= 0)
        {
          Log.errorLog(className, "setTxPercentileNumbers", "", "", "percentileValue in config.ini cannot be greater then 100. So using default value.");
          continue;
        }

        if (arrPercentileNum.contains(percentileValues))
          continue;
        
        arrPercentileNum.add(percentileNumber);
      }

      Log.debugLogAlways(className, "setTxPercentileNumbers", "", "", "Percentile Numbers = " + arrPercentileNum);
      
     if(arrPercentileNum.size() != 0)
      {
        configuredNthPercentileValue = rptUtilsBean.convertArrayListToIntArray(arrPercentileNum);
        arrTxPercentileNumbers = configuredNthPercentileValue;
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "setTxPercentileNumbers", "", "", "Exception - ", e);
    }
  }
  
  public int getPercentileGraphRefreshInterval()
  {
    return percentileGraphRefreshInterval;
  }

  public void setPercentileGraphRefreshInterval(int percentileGraphRefreshInterval)
  {
    this.percentileGraphRefreshInterval = percentileGraphRefreshInterval;
  }

  public int[] getTxPercentileNumbers()
  {
    return arrTxPercentileNumbers;
  }

  public void setTxPercentileNumbers(int[] arrTxPercentileNumbers)
  {
    this.arrTxPercentileNumbers = arrTxPercentileNumbers;
  }
  
  public int[] getDashboardPercentileNumbers()
  {
    return arrDashboardPercentileNumbers;
  }

  public void setDashboardPercentileNumbers(int[] arrDashboardPercentileNumbers)
  {
    this.arrDashboardPercentileNumbers = arrDashboardPercentileNumbers;
  }

  // common method to get config value of any keyword in String.
  private String getConfigStringVal(String name, String defaultValue)
  {
    return ((Config.getValue(name)).equals("")) ? defaultValue : Config.getValue(name);
  }

  // Method to get debugLevel
  public int getDebugLevel()
  {
    return debugLevel;
  }

  public int getTreeSortingMode()
  {
    return treeSortingMode;
  }

  // Method to get profileMode
  public int getProfileMode()
  {
    return profileMode;
  }

  // Method to get high water mark value of Graph Time Series Hash Map
  // It is used to define how many maximum series we need to keep in hash map
  public int getGTSHighWaterMark()
  {
    return gtsHighWaterMark;
  }

  // Method to get Low water mark value of Graph Time Series Hash Map
  public int getGTSLowWaterMark()
  {
    return gtsLowWaterMark;
  }

  // Method to get the value of netstorm.execution.enableAutoActivate keyword from config.ini
  public String getAutoActivate()
  {
    return enableAutoActivate;
  }

  // Method to get the the value of enableRunTimeChangeQuantity
  public String getEnableRunTimeQuantity()
  {
    return enableRunTimeChangeQuantity;
  }

  // Method to get the name of watermark image
  public String getWaterMarkImage()
  {
    return waterMarkImage;
  }

  // Method to get Maximum quantity user can change at run time
  public int getMaxAddRunTimeChangeQuantity()
  {
    return maxAddRunTimeChangeQuantity;
  }

  // Method to get minimum bubble size in GEO Map
  public int getMinBubbleSize()
  {
    return geoMapMinBubbleSize;
  }

  // Method to get maximum bubble size in GEO Map
  public int getMaxBubbleSize()
  {
    return geoMapMaxBubbleSize;
  }

  // Mehtod to get value of High Water Mark for TestRunData
  public int getHighWaterMarkForTRD()
  {
    return activeOnlineGraphsHWM;
  }

  // Mehtod to get value of Low Water Mark for TestRunData
  public int getLowWaterMarkForTRD()
  {
    return activeOnlineGraphsLWM;
  }

  // Method to get alert interval from config.ini
  public String getAlertScheduleInterval()
  {
    return alertScheduleInterval;
  }

  // Method to get the value of RtgMaxDataSamplesInOneGraph
  public int getRtgMaxDataSamplesInOneGraph()
  {
    return rtgMaxDataSamplesInOneGraph;
  }

  public int getEnalbelGeneratorValue()
  {
    return enalbelGenerator;
  }

  /**
   * Getting the value of NDE test run in continuous mode.
   * 
   * @return
   */
  public int getNDETestRunNum()
  {
    return nDE_Test_Run;
  }

  /**
   * Getting Data View.
   * 
   * @return
   */
  public String getDataView()
  {
    return dataView;
  }
  
  public int getEnableTreeColorIcon()
  {
    return enableTreeColorIcon;
  }

  public int getWaitTime()
  {
    return waitTime;
  }

}

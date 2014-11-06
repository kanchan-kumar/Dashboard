package pac1.Bean;

import java.io.Serializable;
import java.util.Vector;

public class TestRunDataType implements Serializable
{
  private static final long serialVersionUID = -2053979548023162381L;
  public static String className = "TestRunDataType";
  public static String dataViewKeywordName = "netstorm.execution.defaultDataView";
  public static int defaultMinuteViewNDE = 240;
  public static String PARTITION_SETTINGS = "PARTITION_SETTINGS";

  public static final int WHOLE_SCENARIO_DATA = 0;
  public static final int LAST_N_MINUTES_DATA = 1;
  public static final int SPECIFIED_PHASE_OR_TIME = 2;
  // type can be 0 -> WHOLE_SCENARIO_DATA, 1 -> LAST_N_MINUTES_DATA, 2 ->
  // SPECIFIED_PHASE_OR_TIME
  private int type = 0;
  private int lastNMinutesValue = 0;
  private String startDateTime = "NA";
  private String endDateTime = "NA";
  private int granularity = -1; // auto -> -1
  public boolean isAbsoluteDateTime = false;
  private boolean isPhase = false;
  private String phaseName = "NA";
  public static String NSDefaultData = "WholeScenario";
  public static String NDEDefaultData = "Last_240_Minutes";

  /*
   * Tells the partition structure type. 0 - No Partition. 1 - Time Based Partition 2 - Size Based Partition.
   */
  public static int partitionSettingType = -1;

  public TestRunDataType(boolean testViewMode)
  {
    this(getProductName(), getDataTypeValueFromConfig(), testViewMode);
  }

  public TestRunDataType(boolean testViewMode, String productName)
  {
    this(productName, getDataTypeValueFromConfig(), testViewMode);
  }

  public TestRunDataType(String productName, String keywordValue, boolean testViewMode)
  {
    try
    {
      Log.debugLog(className, "TestRunDataType", "", "", "Method Called. keywordValue = " + keywordValue + ", productName = " + productName + ", testViewMode = " + testViewMode);

      // This condition sets the off line mode in whole scenario mode.
      if (testViewMode && productName.equals("NDE"))
      {
        setModeForOffline(testViewMode, productName);
      }
      else
      {
        if (keywordValue.equals("") || !validateKeywordValue(keywordValue))
        {
          if (productName.equals("NDE"))
          {
            this.type = TestRunDataType.LAST_N_MINUTES_DATA;
            this.lastNMinutesValue = defaultMinuteViewNDE;
            this.isAbsoluteDateTime = true;
          }
          else
          {
            this.type = WHOLE_SCENARIO_DATA;
          }
        }
        else
        {
          if (keywordValue.startsWith("Last"))
          {
            this.type = TestRunDataType.LAST_N_MINUTES_DATA;
            String[] arrValue = keywordValue.split("_");
            if (arrValue.length >= 2)
            {
              try
              {
                int minutes = Integer.parseInt(arrValue[1].trim());
                this.lastNMinutesValue = minutes;
              }
              catch (Exception ex)
              {
                if (productName.equals("NDE"))
                  this.lastNMinutesValue = defaultMinuteViewNDE;
                else
                  this.type = WHOLE_SCENARIO_DATA;

                Log.errorLog(className, "TestRunDataType", "", "", "Exception - " + ex);
              }
            }
            else
            {
              Log.debugLog(className, "TestRunDataType", "", "", "Assigning default value for productName = " + productName);

              if (productName.equals("NDE"))
              {
                this.lastNMinutesValue = defaultMinuteViewNDE;
                this.isAbsoluteDateTime = true;
              }
              else
              {
                this.type = WHOLE_SCENARIO_DATA;
              }
            }
          }
          else
          {
            this.type = WHOLE_SCENARIO_DATA;
            this.isAbsoluteDateTime = true;
          }
        }
      }
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "TestRunDataType", "", "", "Exception - ", ex);
    }
  }

  public TestRunDataType(int type, int granularity, int lastNMinutesValue, String startDateTime, String endDateTime, boolean isAbsoluteDateTime, boolean isPhase)
  {
    this.type = type;
    this.granularity = granularity;
    this.lastNMinutesValue = lastNMinutesValue;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.isAbsoluteDateTime = isAbsoluteDateTime;
    this.isPhase = isPhase;
  }

  /**
   * This function return false , if keyword is invalid.
   */
  public boolean validateKeywordValue(String keyword)
  {
    try
    {
      String arrSplitKey[] = rptUtilsBean.split(keyword, "_");
      if (!keyword.trim().equals(NSDefaultData) && (arrSplitKey.length != 3) && (!keyword.trim().startsWith("Last_") || !keyword.trim().startsWith("SPECIFIED_TIME_")))
        return false;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "validateKeywordValue", "", "", "Exception - ", e);
    }

    return true;
  }

  public static String getProductName()
  {
    String strServerCheck = "NS";

    try
    {
      String strCmdName = "nsi_show_config";
      String strCmdArgs = "-t";
      CmdExec cmdExec = new CmdExec();
      Vector vecCmdOut = cmdExec.getResultByCommand(strCmdName, strCmdArgs, 0, "netstorm", "root");
      if (vecCmdOut.size() > 0)
        strServerCheck = vecCmdOut.get(0).toString().trim();
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getProductName", "", "", "Exception - ", ex);
    }

    return strServerCheck;
  }

  // get keyword value form config.ini
  public static String getDataTypeValueFromConfig()
  {
    String keywordValue = Config.getValue(dataViewKeywordName);
    return keywordValue;
  }

  public String getPhaseName()
  {
    return phaseName;
  }

  public void setPhaseName(String phaseName)
  {
    this.phaseName = phaseName;
  }

  public int getLastNMinutesValue()
  {
    return lastNMinutesValue;
  }

  public void setLastNMinutesValue(int lastNMinutesValue)
  {
    this.lastNMinutesValue = lastNMinutesValue;
  }

  public int getGranularity()
  {
    return granularity;
  }

  public void setGranularity(int granularity)
  {
    this.granularity = granularity;
  }

  public int getType()
  {
    return type;
  }

  public void setType(int type)
  {
    this.type = type;
  }

  public boolean isPhase()
  {
    return isPhase;
  }

  public void setPhase(boolean isPhase)
  {
    this.isPhase = isPhase;
  }

  public String getStartDateTime()
  {
    return startDateTime;
  }

  public void setStartDateTime(String startDateTime)
  {
    this.startDateTime = startDateTime;
  }

  public String getEndDateTime()
  {
    return endDateTime;
  }

  public void setEndDateTime(String endDateTime)
  {
    this.endDateTime = endDateTime;
  }

  // Return default key
  public static String getDefaultKey(String product)
  {
    String defaultDataView = getDataTypeValueFromConfig();

    if (!defaultDataView.equals(""))
      return defaultDataView;

    if (product.equals("NDE"))
      return NDEDefaultData;
    else
      return NSDefaultData;
  }

  public static int getHistoryCountFromKey(String panelDataType)
  {
    if (panelDataType.startsWith(NSDefaultData))
    {
      return 0;
    }
    else if (panelDataType.startsWith("Last"))
    {
      String[] arrValue = panelDataType.split("_");
      if (arrValue.length >= 2)
      {
        try
        {
          return Integer.parseInt(arrValue[1].trim());
        }
        catch (Exception ex)
        {
          return 0;
        }
      }
      else
        return 0;
    }
    else
      return 0;
  }

  // Return isElapsed true for NS and false for NDE
  public static boolean isElapsed(String product)
  {
    if (product.equals("NDE"))
      return false;
    else
      return true;
  }

  // This method returns the hash map key
  public String getHMapKey()
  {
    String txtGranularity = "";
    if (granularity != -1)
      txtGranularity = "_" + granularity;

    if (type == WHOLE_SCENARIO_DATA)
      return "WholeScenario" + txtGranularity;
    else if (type == LAST_N_MINUTES_DATA)
      return "Last_" + lastNMinutesValue + "_Minutes" + txtGranularity;
    else if (type == SPECIFIED_PHASE_OR_TIME)
      return "SPECIFIED_TIME_" + startDateTime + "_" + endDateTime + txtGranularity;
    else
    {
      Log.errorLog(className, "getHMapKey", "", "", "Invalid Request receive for key.");
      return "";
    }
  }

  public static String getStringFromMinutes(int nMinutesValue)
  {
    if (nMinutesValue == 1)
      return nMinutesValue + " Minute";

    if (nMinutesValue >= 60)
    {
      int hours = nMinutesValue / 60;
      int remainingMinutes = nMinutesValue % 60;
      String tmpText = hours + " hour ";
      if (hours > 1)
        tmpText = hours + " hours ";

      if (remainingMinutes == 0)
        return tmpText;
      else if (remainingMinutes == 1)
        return tmpText + remainingMinutes + " Minute";
      else
        return tmpText + remainingMinutes + " Minutes";
    }
    else
    {
      return nMinutesValue + " Minutes";
    }
  }

  /**
   * Returns the object of TestRunDataType with specified last n key.
   * 
   * @return
   */
  public static TestRunDataType getTestRunDataTypeObjByLastNKey(String key)
  {
    // Default view in NDE Continuous Monitoring.
    int lastNMinutesValue = 240;

    try
    {
      Log.debugLog(className, "getTestRunDataTypeObjByKey", "", "", "Method Called. key = " + key);
      lastNMinutesValue = Integer.parseInt(rptUtilsBean.split(key, "_")[1]);
    }
    catch (Exception e)
    {
      Log.errorLog(className, "getTestRunDataTypeObjByLastNKey", "", "", "Invalid Requested Time Based Key. Key = " + key);
      // e.printStackTrace();
    }
    // If exception is coming while parsing keyword, then return NDE default.
    return (new TestRunDataType(LAST_N_MINUTES_DATA, -1, lastNMinutesValue, "NA", "NA", false, false));
  }

  /**
   * This Method is used to check weather or not current configuration of machine is for NDE Continuous Mode. <br>
   * Note : Use only when code executes on server.
   * 
   * @return
   */
  public static boolean isNDE_Continuous_Mode()
  {
    try
    {
      Log.debugLogAlways(className, "isNDE_Continuous_Mode", "", "", "Method Called.");

      String nDE_TestRun = Config.getValue("nde.testRunNum");

      if (nDE_TestRun == null || nDE_TestRun.trim().equals("") || Integer.parseInt(nDE_TestRun) < 0)
        return false;

      if (getProductName().trim().equals("NDE"))
      {
        Log.debugLogAlways(className, "isNDE_Continuous_Mode", "", "", "NDE Continuous Mode is detected.");
        return true;
      }
    }
    catch (Exception e)
    {
      Log.errorLog(className, "isNDE_Continuous_Mode", "", "", "Error in Checking NDE Continuous Mode.");
    }
    return false;
  }

  /**
   * Method Tells the partition structure type. </br> 0 - No Partition. 1 - Time Based Partition 2 - Size Based Partition.
   * 
   * @return
   */
  public static int getTestRunPartitionType(int testRun)
  {
    try
    {
      Log.debugLogAlways(className, "getTestRunPartitionType", "", "", "Method Called.");
      // Getting the value of keyword from scenario file.
      String keywordValues[] = Scenario.getKeywordValues(PARTITION_SETTINGS, testRun);

      // Getting the keyword value.
      if (keywordValues == null || keywordValues.length == 0)
        partitionSettingType = 0;
      else
      {
        // Getting value of partition_setting keyword from scenario file.
        partitionSettingType = Integer.parseInt(keywordValues[0]);
      }
    }
    catch (Exception e)
    {
      // If Invalid value of keyword then treated as net_storm mode.
      partitionSettingType = 0;
      e.printStackTrace();
    }
    return partitionSettingType;
  }

  public void setModeForOffline(boolean testViewMode, String productName)
  {
    try
    {
      Log.debugLog(className, "setModeForOffline", "", "", "Method Called. testViewMode = " + testViewMode + ", productName = " + productName);
      if (testViewMode)
      {
        this.type = WHOLE_SCENARIO_DATA;
        if (productName.equals("NDE"))
          this.isAbsoluteDateTime = true;
      }
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "setModeForOffline", "", "", "Exception - ", ex);
    }
  }

  public String toString()
  {
    return "type = " + type + " (Whole Scenario = 0, LAST_N_MINUTES_DATA = 1, SPECIFIED_PHASE_OR_TIME = 2)";
  }
}

/**----------------------------------------------------------------------------
 * Name       ProfileData.java
 * Purpose    To load each profile data
 * @author    Prabhat
 * Modification History
 *        10/28/2010 - Prabhat - Initial Version
 *---------------------------------------------------------------------------**/
package pac1.Bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import pac1.Bean.Log;

public class ProfileData implements java.io.Serializable
{
  private static String className = "ProfileData";
  private static final String DATE_FORMAT_NOW = "MM/dd/yy HH:mm:ss";
  
  private String profileName = "";
  private String profileNameWithPath = "";
  private String profileDescription = "";
  private String profileUpdatedUser = "NA";
  private String profileModifiedDate = "NA";
  
  private Properties propProfile = null;
  private Vector vecProfileData = null;
  private HashMap dialChartPropertMap = new HashMap();
  public ProfileData()
  {
  }

  public ProfileData(String profileName, String profileNameWithPath)
  {
    this.profileName = profileName;
    initProfile(profileNameWithPath);
  }

  public HashMap getDialChartPropertMap()
  {
    return dialChartPropertMap;
  }
  
  private void initProfile(String profileNameWithPath)
  {
    this.vecProfileData = readProfile(profileNameWithPath);
    this.propProfile = getSelectedProfileData(vecProfileData);
    this.profileDescription = propProfile.getProperty("PROFILE_DESC", "NA");
    this.profileUpdatedUser = propProfile.getProperty("UPDATED_BY", "netstorm");
    this.profileModifiedDate = getProfileModifiedDateByProfileName(profileNameWithPath);
  }

  // this function is to read profile data
  public Vector readProfile(String profileNameWithPath)
  {
    Log.debugLog(className, "readProfile", "", "", "Method Started. ProfileName = " + profileNameWithPath);

    String errMsg = "";
    try
    {
      Vector vecProfileData = new Vector();
      String strLine;

      File profileFileObj = new File(profileNameWithPath);

      if(!profileFileObj.exists())
      {
        Log.errorLog(className, "readProfile", "", "", "Profile not found - " + profileNameWithPath);
        return null;
      }

      FileInputStream fis = new FileInputStream(profileNameWithPath);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));

      int panelNumber = -1;
      
      while((strLine = br.readLine()) !=  null)
      {
        strLine = strLine.trim();
        if(strLine.startsWith("#"))
          continue;
        if(strLine.length() == 0)
          continue;

        if(strLine.startsWith("PANEL"))
        {
          try
          {
            panelNumber++;
            
            String[] arrPanelData = rptUtilsBean.strToArrayData(strLine, "|");
            
            //e.g Dial_10.0_20.0_<_14.0_17.0
            //e.g Meter_10.0_20.0_<_14.0_17.0
            String panelProperty = arrPanelData[1].trim();
            // Check If Panel Type is Dial or Meter
            if((panelProperty.startsWith(PanelTypeUtils.dialGraphName) || panelProperty.startsWith(PanelTypeUtils.meterGraphName)))
           	{
            	boolean ispanelPropertyValid = false;
            	DialGraphPropertiesBean dialGraphPropertiesBean = new DialGraphPropertiesBean(profileName, strLine, panelProperty);
            	ispanelPropertyValid = dialGraphPropertiesBean.parsedDialPanelInfo(panelProperty);
            	if(ispanelPropertyValid)
            		dialChartPropertMap.put(panelNumber, dialGraphPropertiesBean);
            	else
              	Log.errorLog(className, "readFile", "", "", "Invalid Panel Info for - profileName = " + profileName + ", strLine = " + strLine + ", panelProperty = " + panelProperty);
           	}
          } 
          catch(Exception ex)
          {
            Log.errorLog(className, "readFile", "", "", "profileName = " + profileName + ", strLine = " + strLine);
          }
        }
        
        vecProfileData.add(strLine);
      }

      br.close();
      fis.close();
      return vecProfileData;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "readProfile", "", "", "Exception in reading profile = " + profileName, e);
      return null;
    }
  }

  // this function return the Properties Object of selected profile
  private Properties getSelectedProfileData(Vector vecProfileData)
  {
    Log.debugLog(className, "getSelectedProfileData", "", "", "Method Started. Profile Name = " + profileNameWithPath);

    Properties profileProp = new Properties();
    try
    {
      if(vecProfileData == null)
        return profileProp;
      else
      {
        for(int index = 0; index < vecProfileData.size(); index++)
        {
          String dataLine = vecProfileData.get(index).toString();
          if(dataLine.contains("="))
          {
            String arrData[] = dataLine.split("=");
            if(arrData.length == 2)
              profileProp.put(arrData[0].trim(), arrData[1].trim());
            else
              continue;
          }
          else
            continue;
        }
      }
      return profileProp;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getSelectedProfileData", "", "", "Exception occur while fetching profile data - ", e);
      return profileProp;
    }
  }

  // return profile Name
  public String getProfileName()
  {
    return profileName;
  }

  // return profile description
  public String getProfileDescription()
  {
    return profileDescription;
  }

  public String getProfileUpdatedUser()
  {
    return profileUpdatedUser;
  }

  public String getProfileModifiedDateByProfileName(String profileNameWithPath)
  {
    File profileFileObj = new File(profileNameWithPath);
    
    long dateModified = profileFileObj.lastModified();
    
    Date dateObjLastModified = new Date(dateModified);
    SimpleDateFormat dateFormatObjLastModified = new SimpleDateFormat(DATE_FORMAT_NOW);
    
    return dateFormatObjLastModified.format(dateObjLastModified);
  }
  
  public String getProfileModifiedDate()
  {
    return profileModifiedDate;
  }

  // return profile property
  public Properties getPropProfile()
  {
    return propProfile;
  }

  // return profile data in vector
  public Vector getVecProfileData()
  {
    return vecProfileData;
  }

  // set profile Name
  public void setProfileName(String profileName)
  {
    this.profileName = profileName;
  }

  // set profile description
  public void setProfileDescription(String profileDescription)
  {
    this.profileDescription = profileDescription;
  }

  public void setProfileUpdatedUser(String profileUpdatedUser)
  {
    this.profileUpdatedUser = profileUpdatedUser;
  }
  
  public void setProfileModifiedDate(String profileModifiedDate)
  {
    this.profileModifiedDate = profileModifiedDate;
  }
  
  // set profile property
  public void setPropProfile(Properties propProfile)
  {
    this.propProfile = propProfile;
  }

  // set profile data in vector
  public void setVecProfileData(Vector vecProfileData)
  {
    this.vecProfileData = vecProfileData;
  }
  
  //Damini -> This class keep the dial graph properties as per panel
  public class DialGraphPropertiesBean implements java.io.Serializable
  {
    public static final String className = "DialGraphPropertiesBean";
    private float minValue;
    private float maxValue;
    private int condition;
    private String panelProperty;
    private String profileName;
    private String strLine;
    
    private float warningThreshold;
    private float criticalThreshold;

    public static final int LESS_THAN_CONDITION_VALUE = 1;
    public static final int GREATER_THAN_CONDITION_VALUE = 2;
    private static final String CONDITION_LESS_THAN = "<";
    private static final String CONDITION_GREATER_THAN = ">";
 
    private static final int PANEL_TYPE_INDEX = 0;
    private static final int MIN_VALUE_INDEX = 1;
    private static final int MAX_VALUE_INDEX = 2;
    private static final int CONDITION_INDEX = 3;
    private static final int WARNING_THRESHOLD_INDEX = 4;
    private static final int CRITICAL_THRESHOLD_INDEX = 5;

    public DialGraphPropertiesBean(String profileName, String strLine, String panelProperty)
    {
      this.profileName = profileName;
      this.strLine = strLine;
      this.panelProperty = panelProperty;
    }
    
    // Damini - Here We are parsing the Dial & Meter Graph Type
    // For Dial Graph -> 
    // Format -> Dial_Min_Max_Condition_WariningThreshold_CriticalThreshold
    // Example -> Dial_10.0_20.0_<_14.0_17.0
    // For Meter Graph -> 
    // Format -> Meter_Min_Max_Condition_WariningThreshold_CriticalThreshold
    // Example -> Meter_10.0_20.0_<_14.0_17.0
    public boolean parsedDialPanelInfo(String panelProperty)
    {
    	try
      {
        Log.debugLog(className, "parsedDialPanelInfo", "", "", "profileName = " + profileName + ", strLine = " + strLine + ", panelProperty = "+ panelProperty);
        String[] arrParsedDialInfo = rptUtilsBean.strToArrayData(panelProperty, "_");
        if (arrParsedDialInfo != null && arrParsedDialInfo.length > 0)
        {
          if(arrParsedDialInfo.length > 5)
          {
            float minValue = Float.parseFloat(arrParsedDialInfo[MIN_VALUE_INDEX].trim());
            float maxValue = Float.parseFloat(arrParsedDialInfo[MAX_VALUE_INDEX].trim());
            // Keeping Condition Value
            String condition = arrParsedDialInfo[CONDITION_INDEX].trim();
            int conditionValue;
            if(condition.equals(CONDITION_LESS_THAN))
              conditionValue =  LESS_THAN_CONDITION_VALUE;
            else if(condition.equals(CONDITION_GREATER_THAN))
              conditionValue =  GREATER_THAN_CONDITION_VALUE;
            else
              conditionValue = -1;
       
            float warningThreshold = Float.parseFloat(arrParsedDialInfo[WARNING_THRESHOLD_INDEX].trim());
            float criticalThreshold = Float.parseFloat(arrParsedDialInfo[CRITICAL_THRESHOLD_INDEX].trim());
       
            setCondition(conditionValue);
            setCriticalThreshold(criticalThreshold);
            setWarningThreshold(warningThreshold);
            setMaxValue(maxValue);
            setMinValue(minValue);
            return true;
          }
          else
          {
            Log.errorLog(className, "parsedDialPanelInfo", "", "", "Invalid Info for dial. profileName = " + profileName + ", strLine = " + strLine + ", panelProperty = "+ panelProperty);
            return false;
          }
        }
        else
        {
        	Log.debugLog(className, "parsedDialPanelInfo", "", "", "Invalid Info for dial. profileName = " + profileName + ", strLine = " + strLine + ", panelProperty = "+ panelProperty);
        	return false;
        }
      }
      catch (Exception ex)
      {
        Log.stackTraceLog(className, "parsedDialPanelInfo", "", "", "panelProperty = " + panelProperty + ", Exception - ", ex);
        return false;
      }
    }

    public float getMinValue()
    {
      return minValue;
    }
 
    public void setMinValue(float minValue)
    {
      this.minValue = minValue;
    } 

    public float getMaxValue()
    {
      return maxValue;
    }

    public void setMaxValue(float maxValue)
    {
      this.maxValue = maxValue;
    }

    public int getCondition()
    {
      return condition;
    }

    public void setCondition(int condition)
    {
      this.condition = condition;
    }

    public float getWarningThreshold()
    {
      return warningThreshold;
    }

    public void setWarningThreshold(float warningThreshold)
    {
      this.warningThreshold =  warningThreshold;
    }
 
    public float getCriticalThreshold()
    {
      return criticalThreshold;
    }

    public void setCriticalThreshold(float criticalThreshold)
    {
      this.criticalThreshold = criticalThreshold;
    }
    
    public String toString()
    {
      return "Min Value = " + minValue + ", Max Value = " + maxValue + ", Condition = " + condition + ", Warning = " + warningThreshold + ", Critical = " + criticalThreshold;
    }
  }
}

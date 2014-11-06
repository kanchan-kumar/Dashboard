package pac1.Bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

  /*
     Line will came in below format
       
     Group|Type|Name|Active|Parcipating Vusers Pct|Release Target Vusers|Current Vusers|Release Count|Last Release Time|Last Release Reason|Scripts
     G1|0|Start Transaction|1|10|100|20|0|00:00:00|-|hpd_tours,google
     G2|1|Start Script|1|30|100|30|2|00:00:00|-|hpd_tours,macys
     ALL|1|Start Transaction|0|60|100|20|7|00:00:00|Released forcefully|hpd_tours
   */
  public class SyncPointLogRow implements java.io.Serializable
  {
    private String className = "SyncPointLogRow";
    private String GroupValue;
    private String TypeValue;
    private String NameValue;
    private String ActiveValue;
    private String ParcipatingVuserPctValue;
    private String ReleaseTargetVuserValue;
    private String CurrentVuserValue;
    private String ReleaseCountValue;
    private String ReleaseTimeValue;
    private String ReleaseReasonValue;
    private String listScriptValue;
    
    private String absoluteTime;
    private int index;

    String testRunNum = "";
    
    public static final String TYPE_0 = "Start Transaction";
    public static final String TYPE_1 = "Start Page";
    public static final String TYPE_2 = "Start Script";
    public static final String TYPE_3 = "Start SyncPoint";
    
    public static final String Active = "Yes";
    public static final String InActive = "No";
    public static final String Deleted = "Deleted";
    
    public SyncPointLogRow()
    {	
    }
    
    public SyncPointLogRow(String testRunNum)
    {
      this.testRunNum = testRunNum;
    }
    
      
    // ************************ getter starts **********************
    
    public long getStartTime()
    {
      String arrTempSplit[] = Scenario.getTestRunStartTime(Integer.parseInt(testRunNum)).split("  ");
      long startMilliSecs = rptUtilsBean.convStrToMilliSec(arrTempSplit[1].trim());
      return startMilliSecs;
    }
    
    public int getIndex()
    {
      return index;
    }

    public String getAbsoluteTime()
    {
      return absoluteTime;
    }

    public String getGroupName()
    {
      return GroupValue;
    }

    public String getType()
    {
      return TypeValue;
    }

    public String getName()
    {
      return NameValue;
    }

    public String getActive()
    {
      return ActiveValue;
    }

    public String getParcipatingVuserPct()
    {
      return ParcipatingVuserPctValue;
    }

    public String getReleaseTargetVuser()
    {
      return ReleaseTargetVuserValue;
    }

    public String getCurrentVuser()
    {
      return CurrentVuserValue;
    }

    public String getReleaseCountValue()
    {
      return ReleaseCountValue;
    }
    
    public String getReleaseTimeValue()
    {
      return ReleaseTimeValue;
    }

    public String getReleaseReason()
    {
      return ReleaseReasonValue;
    }

    public String getListScripts()
    {
      return listScriptValue;
    }
    
    private String getTwoDigitFmt(int i)
    {
      return ((i >= 10)) ? (i + "") : ("0" + i);
    }
   
    // ************************ getter ends **********************
    
    
    // ************************ setter starts **********************
    
    public void setIndex(int index)
    {
      this.index = index;
    }

    public void setAbsoluteTime(String absoluteTime)
    {
      this.absoluteTime = absoluteTime;
    }
    
    public void setCumulativeAbsoluteReleaseTimeValue(String ReleaseTimeValue)
    {
      long absoluteReleaseTimeValue = rptUtilsBean.convStrToMilliSec(ReleaseTimeValue) + getStartTime();
      absoluteTime = rptUtilsBean.convMilliSecToStr(absoluteReleaseTimeValue);
    }
    
    public void setGroupName(String GroupValue)
    {
      this.GroupValue = GroupValue;
    }
    
    public void setType(String TypeValue)
    {
      this.TypeValue = TypeValue;
    }
    
    public void setName(String NameValue)
    {
      this.NameValue = NameValue;
    }
    
    public void setActive(String ActiveValue)
    {
      this.ActiveValue = ActiveValue;
    }
    
    public void setParcipatingVuserPct(String ParcipatingVuserPctValue)
    {
      this.ParcipatingVuserPctValue = ParcipatingVuserPctValue;
    }
    
    public void setReleaseTargetVuser(String ReleaseTargetVuserValue)
    {
      this.ReleaseTargetVuserValue = ReleaseTargetVuserValue;
    }
    
    public void setCurrentVuser(String CurrentVuserValue)
    {
      this.CurrentVuserValue = CurrentVuserValue;
    }
    
    public void setReleaseCountValue(String ReleaseCountValue)
    {
      this.ReleaseCountValue = ReleaseCountValue;
    }
      
    public void setReleaseTimeValue(String ReleaseTimeValue)
    {
      String strArr[] = rptUtilsBean.split(ReleaseTimeValue, ":");
      
      int hh = Integer.parseInt(strArr[0]);
      int mm = Integer.parseInt(strArr[1]);
      int ss = Integer.parseInt(strArr[2]);
      
      Calendar calender = Calendar.getInstance();
      calender.add(Calendar.HOUR, hh);
      calender.add(Calendar.MINUTE, mm);
      calender.add(Calendar.SECOND, ss);
      
      Date dateObj = calender.getTime();
      setAbsoluteTime(getTwoDigitFmt(dateObj.getHours()) + ":" + getTwoDigitFmt(dateObj.getMinutes()) + ":" + getTwoDigitFmt(dateObj.getSeconds()));
      setCumulativeAbsoluteReleaseTimeValue(ReleaseTimeValue);
      this.ReleaseTimeValue = ReleaseTimeValue;
    }
    
    public void setReleaseReason(String ReleaseReasonValue)
    {
      this.ReleaseReasonValue = ReleaseReasonValue;
    }
    
    public void setListScripts(String listScriptNames)
    {
      this.listScriptValue = listScriptNames;
    }
    
 // ************************ setter Ends **********************
    
    /**
       Line will came in below format
       
     (0)Group|(1)Type|(2)Name|(3)Active|(4)Parcipating Vusers Pct|(5)Release Target Vusers|(6)Current Vusers|(7)Release Count|(8)Last Release Time|(9)Last Release Reason|(10)Scripts
     G1|0|Start Transaction|1|10|100|20|0|00:00:00|-|hpd_tours,google
     G2|1|Start Script|1|30|100|30|2|00:00:00|-|hpd_tours,macys
     ALL|1|Start Transaction|0|60|100|20|7|00:00:00|Released forcefully|hpd_tours
         
     */
    
    public boolean setLogDataToLogRow(SyncPointFileData logFileData, int index)
    {
      try
      {
        ArrayList<String> listOfFields = logFileData.getLineAsList(index);
        
        setGroupName(listOfFields.get(0).toString());
        
        if(listOfFields.get(1).toString().equals("0"))
          setType(TYPE_0);
        else if(listOfFields.get(1).toString().equals("1"))
          setType(TYPE_1);
        if(listOfFields.get(1).toString().equals("2"))
          setType(TYPE_2);
        if(listOfFields.get(1).toString().equals("3"))
          setType(TYPE_3);
        
        setName(listOfFields.get(2).toString());
        
        if(listOfFields.get(3).toString().equals("0"))
          setActive(InActive);
        else if(listOfFields.get(3).toString().equals("1"))
          setActive(Active);
        else if(listOfFields.get(3).toString().equals("91"))
          setActive(Deleted);
        else if(listOfFields.get(3).toString().equals("90"))
          setActive(InActive);
        
        
        String Pct = rptUtilsBean.convertTodecimal(Double.parseDouble(listOfFields.get(4).toString()), 2);
        setParcipatingVuserPct(Pct + "%");
        setReleaseTargetVuser(listOfFields.get(5).toString());
        setCurrentVuser(listOfFields.get(6).toString());
        setReleaseCountValue(listOfFields.get(7).toString());
        setReleaseTimeValue(listOfFields.get(8).toString());
        setReleaseReason(listOfFields.get(9).toString());
        setListScripts(listOfFields.get(10).trim());
        String ScriptName = listOfFields.get(10).trim();
        setIndex(index);
        return true;
      }
      catch(Exception ex)
      {
        Log.stackTraceLog(className, "setLogDataToViewer", "", "", "Exception in setting line to Event Row", ex);
        return false;
      }
    }
  }
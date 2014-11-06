package pac1.Bean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

  /*
     New Version:
     
     Line will came in below format
       
     Time Stamp|Event Id|Severity|User Session Data|Data Source Type|Attributes Name|Attribute Values|Event Description
     
     00:10:00|1001|Critical|1:4294967295:4294967295|Monitor|Server,File|192.168.1.1,abx.txt|GC file not found
     395|10165|Warning|1:4294967295:4294967295|Core|Script,Page,Url|XX,YY,ZZ|Error in Parent (HTTP).
     442|10106|Warning|0:0:0|Core|Script,Page,Url|static1K,class1_0_html,GET /file_set/dir00000/clss1_0.html HTTP/1.1|Error in Request (HTTP).
     
     Old Version:
     
     "Host", "Data" 
   */
  public class EventLogRow implements java.io.Serializable
  {
    private String className = "EventLogRow";
    private String timeStamp;
    private int EventId;
    private String severity;
    private String userData;
    private String source;
    private ArrayList<String> listAttributeNames;
    private ArrayList<String> listAttributeValues;
    private String desc;
    private String absoluteTime;
    private String absTimeString;
    private String data;
    private String host;
    private String hostField;
    private int index;
    private boolean isUserDataExist = false;
    private boolean isAttributeExist = false;
    String testRunNum = "";
    
    public static final int OLDER_NUM_OF_COLUMNS = 8;
    public static final int NEWER_NUM_OF_COLUMNS = 9;
    public static final int NEWER_NUM_OF_COLUMNS_IN_TABLE = 7;
    
    private String trStartTime = null;
    private TimeZone trTimeZone = null;
    private long trStartTimeStamp = 0L;
    
    public EventLogRow()
    {
      initTestRunTime();
    }
    public EventLogRow(String testRunNum)
    {
      this.testRunNum = testRunNum;
      initTestRunTime();
    }
    
   /**
    * Method is used to initialize the Test Run Time.
    */
   private void initTestRunTime()
   {
     try
     {
       /*Reading Test Run Start Time*/
       trStartTime = Scenario.getTestRunStartTime(Integer.parseInt(testRunNum));
       
       /*Getting Time Zone.*/
       trTimeZone = ExecutionDateTime.getSystemTimeZoneGMTOffset();
      
       /*Getting Time Stamp of Test Run.*/
       trStartTimeStamp = ExecutionDateTime.convertFormattedDateToMilliscond(trStartTime, "MM/dd/yy HH:mm:ss", trTimeZone);
     }
     catch(Exception e)
     {
       e.printStackTrace();
     }
   }
    
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
    public void setIndex(int index)
    {
      this.index = index;
    }
    
    public String getData()
    {
      return data;
    }
    public String getHost()
    {
      return host;
    }
    public String getHostField()
    {
      return hostField;
    }
    public void setData(String data)
    {
      this.data = data;
    }
    public void setHost(String host)
    {
      this.host = host;
    }
    public void setHostField(String hostVal)
    {
      this.hostField = hostVal;
    }
    public String getAbsoluteTime()
    {
      return absoluteTime;
    }
    public void setAbsoluteTime(String absoluteTime)
    {
      this.absoluteTime = absoluteTime;
    }
    
    public void setCumulativeAbsoluteTimeStamp(String timeStamp)
    {
      long absoluteTimeStamp = ExecutionDateTime.convertFormattedTimeToMillisecond(timeStamp, ":") + trStartTimeStamp;
      absTimeString = ExecutionDateTime.convertDateTimeStampToFormattedString(absoluteTimeStamp, "MM/dd/yy HH:mm:ss", trTimeZone);
    }
    
    public String getTimeStamp()
    {
      return timeStamp;
    }
    
    /**
     * Method return the Absolute Time Stamp in String format.
     * @return
     */
    public String getAbsTimeString()
    {
      return absTimeString;
    }
    
    public int getEventId()
    {
      return EventId;
    }
    public String getSeverity()
    {
      return severity;
    }
    public String getUserData()
    {
      return userData;
    }
    public String getSource()
    {
      return source;
    }
    public ArrayList<String> getListAttributeNames()
    {
      return listAttributeNames;
    }
    public ArrayList<String> getListAttributeValues()
    {
      return listAttributeValues;
    }
    public String getDesc()
    {
      return desc;
    }

    private String getTwoDigitFmt(int i)
    {
      return ((i >= 10)) ? (i + "") : ("0" + i);
    }
    public boolean isUserDataExist()
    {
      return isUserDataExist;
    }
    public boolean isAttributeExist()
    {
      return isAttributeExist;
    }
    
    public void setTimeStamp(String timeStamp)
    {
      /*String strArr[] = rptUtilsBean.split(timeStamp, ":");
      
      int hh = Integer.parseInt(strArr[0]);
      int mm = Integer.parseInt(strArr[1]);
      int ss = Integer.parseInt(strArr[2]);
      
      Calendar calender = Calendar.getInstance();
      calender.add(Calendar.HOUR, hh);
      calender.add(Calendar.MINUTE, mm);
      calender.add(Calendar.SECOND, ss);*/
      
      //Date dateObj = calender.getTime();
      //setAbsoluteTime(getTwoDigitFmt(dateObj.getHours()) + ":" + getTwoDigitFmt(dateObj.getMinutes()) + ":" + getTwoDigitFmt(dateObj.getSeconds()));
      
      setCumulativeAbsoluteTimeStamp(timeStamp);
      this.timeStamp = timeStamp;
    }
    
    public void setEventId(int eventId)
    {
      EventId = eventId;
    }
    public void setSeverity(String severity)
    {
      this.severity = severity;
    }
    
    //To set virtual users attributes (IP:NVMID:UserID:SessionID)
    public void setUserData(String userData)
    {
      this.userData = userData.replaceAll("-1", "NA").replace("255", "Parent");
      if((this.userData.equalsIgnoreCase("NA:NA:NA:NA")) || (this.userData.equalsIgnoreCase("-:-:-:-")))
        isUserDataExist = false;
      else
        isUserDataExist = true;
    }
    public void setSource(String source)
    {
      this.source = source;
    }
    public void setListAttributeNames(ArrayList<String> listAttributeNames)
    {
      this.listAttributeNames = listAttributeNames;
      if(listAttributeNames.size() > 0)
        isAttributeExist = true;
    }
    public void setListAttributeValues(ArrayList<String> listAttributeValues)
    {
      this.listAttributeValues = listAttributeValues;
    }
    public void setDesc(String desc)
    {
      this.desc = desc;
    }
    
    
    /**
       Line will came in below format
         
       (0)Time Stamp|(1)Event Id|(2)Severity|(3)User Session Data|(4)Data Source Type|(5)Attributes Name|(6)Attribute Values|(7)Event Description
       00:10:00|1001|Critical|1:4294967295:4294967295|Monitor|Server,File|192.168.1.1,abx.txt|GC file not found
       395|10165|Warning|1:4294967295:4294967295|Core|Script,Page,Url|XX,YY,ZZ|Error in Parent (HTTP).
       442|10106|Warning|0:0:0|Core|Script,Page,Url|static1K,class1_0_html,GET /file_set/dir00000/clss1_0.html HTTP/1.1|Error in Request (HTTP).
     */
    public boolean setLogDataToLogRow(LogFileData logFileData, int index)
    {
      try
      {
        ArrayList<String> listOfFields = logFileData.getLineAsList(index);
        if(logFileData.getNumOfFields() == NEWER_NUM_OF_COLUMNS)
        {
          setTimeStamp(listOfFields.get(0));
          setEventId(Integer.parseInt(listOfFields.get(1)));
          setSeverity(listOfFields.get(2));
          setUserData(listOfFields.get(3));
          setSource(listOfFields.get(4));
          setHostField(listOfFields.get(5));
          String attributeName = listOfFields.get(6).trim();
          
          if((attributeName.equals("-")) || (attributeName.equals("NA")))
          {
            setListAttributeNames(new ArrayList<String>());
            setListAttributeValues(new ArrayList<String>());
          }
          else
          {
            setListAttributeNames(new ArrayList<String>(Arrays.asList(rptUtilsBean.split(attributeName, ","))));
            
            
            String attributeValues = listOfFields.get(7).trim();
            
            attributeValues = attributeValues.replaceAll("%7[Cc]", "|").replaceAll("%0[Dd]", "\r").replaceAll("%0[Aa]", "\n");
            
            ArrayList<String> listOfAttributes =  new ArrayList<String>(Arrays.asList(rptUtilsBean.split(attributeValues, ",")));
            
            for (int i = 0 ; i < listOfAttributes.size() ; i++)
              listOfAttributes.set(i, listOfAttributes.get(i).replaceAll("%2[Cc]", ","));
            
            int lengthOfAttributeNames = getListAttributeNames().size(); 
            int lengthOfAttributeValuess = listOfAttributes.size(); 
            
            if(lengthOfAttributeValuess != lengthOfAttributeNames)
            {
//              Log.errorLog(className, "setLogDataToViewer", "", "", "Error number of name of attributes and values are not same. Number of attributes names = " + getListAttributeNames().size() + ", number of attributes values = " + listOfAttributes.size());
//              return false;
              
              //Number of attributes values are more that number of attributes name
              if(lengthOfAttributeValuess > lengthOfAttributeNames)
              {
                String str = "";
                for(int i = lengthOfAttributeNames - 1 ; i < lengthOfAttributeValuess ; i++)
                {
                  if(str.equals(""))
                    str = listOfAttributes.get(i);
                  else
                    str = str + "," + listOfAttributes.get(i);
                }
                listOfAttributes.set(lengthOfAttributeNames - 1, str);
                listOfAttributes = new ArrayList(listOfAttributes.subList(0, lengthOfAttributeNames));
              }
              else
              {
                for(int i = 0; i < lengthOfAttributeNames - lengthOfAttributeValuess ; i++)
                  listOfAttributes.add("Not Found");
              }
            }
            setListAttributeValues(listOfAttributes);
          }
          
          setDesc(listOfFields.get(8).replaceAll("%7[Cc]", "|").replaceAll("%0[Dd]", "\r").replaceAll("%0[Aa]", "\n").replaceAll("%2[Cc]", ","));
        }
        else if(logFileData.getNumOfFields() == OLDER_NUM_OF_COLUMNS)//This is for Older Version compatibiltiy
        {
          setTimeStamp(listOfFields.get(0));
          setEventId(Integer.parseInt(listOfFields.get(1)));
          setSeverity(listOfFields.get(2));
          setUserData(listOfFields.get(3));
          setSource(listOfFields.get(4));
          String attributeName = listOfFields.get(5).trim();
          
          if((attributeName.equals("-")) || (attributeName.equals("NA")))
          {
            setListAttributeNames(new ArrayList<String>());
            setListAttributeValues(new ArrayList<String>());
          }
          else
          {
            setListAttributeNames(new ArrayList<String>(Arrays.asList(rptUtilsBean.split(attributeName, ","))));
            
            
            String attributeValues = listOfFields.get(6).trim();
            
            attributeValues = attributeValues.replaceAll("%7[Cc]", "|").replaceAll("%0[Dd]", "\r").replaceAll("%0[Aa]", "\n");
            
            ArrayList<String> listOfAttributes =  new ArrayList<String>(Arrays.asList(rptUtilsBean.split(attributeValues, ",")));
            
            for (int i = 0 ; i < listOfAttributes.size() ; i++)
              listOfAttributes.set(i, listOfAttributes.get(i).replaceAll("%2[Cc]", ","));
            
            int lengthOfAttributeNames = getListAttributeNames().size(); 
            int lengthOfAttributeValuess = listOfAttributes.size(); 
            
            if(lengthOfAttributeValuess != lengthOfAttributeNames)
            {
//              Log.errorLog(className, "setLogDataToViewer", "", "", "Error number of name of attributes and values are not same. Number of attributes names = " + getListAttributeNames().size() + ", number of attributes values = " + listOfAttributes.size());
//              return false;
              
              //Number of attributes values are more that number of attributes name
              if(lengthOfAttributeValuess > lengthOfAttributeNames)
              {
                String str = "";
                for(int i = lengthOfAttributeNames - 1 ; i < lengthOfAttributeValuess ; i++)
                {
                  if(str.equals(""))
                    str = listOfAttributes.get(i);
                  else
                    str = str + "," + listOfAttributes.get(i);
                }
                listOfAttributes.set(lengthOfAttributeNames - 1, str);
                listOfAttributes = new ArrayList(listOfAttributes.subList(0, lengthOfAttributeNames));
              }
              else
              {
                for(int i = 0; i < lengthOfAttributeNames - lengthOfAttributeValuess ; i++)
                  listOfAttributes.add("Not Found");
              }
            }
            setListAttributeValues(listOfAttributes);
          }
          
          setDesc(listOfFields.get(7).replaceAll("%7[Cc]", "|").replaceAll("%0[Dd]", "\r").replaceAll("%0[Aa]", "\n").replaceAll("%2[Cc]", ","));
        }
        else
        {
          Log.errorLog(className, "setLogDataToViewer", "", "", "Error size of list must be equal to 8 Or 6. Current Size = " + listOfFields.size());
          return false;
        }
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
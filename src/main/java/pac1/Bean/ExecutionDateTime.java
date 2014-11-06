package pac1.Bean;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;
import java.util.regex.Pattern;


/**
 * This class is used by execution GUI for calculate all Date/Time Specific methods.
 *
 */
public class ExecutionDateTime 
{
  //Default Time zone GMT offset according to US/Pacific.
  public static String DEFAULT_TIMEZONE_GMT_OFFSET = "GMT-0500";
  
  /**
   * This method is used to get Formatted Date/Time in millisecond.
   * @param dateTimeStamp
   * @param dateFormat
   * @return
   */
  public static long convertFormattedDateToMilliscond(String dateTime, String dateFormat, TimeZone timeZone)
  {
    try
    {
      SimpleDateFormat trDateFormat = new SimpleDateFormat(dateFormat);
      
      //If TimeZone is null then it use default.
      if(timeZone != null)
        trDateFormat.setTimeZone(timeZone);
      
      Date userDate = trDateFormat.parse(dateTime); 
      long absoluteDateInMillies = userDate.getTime();	 
      return absoluteDateInMillies;	   
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return 0L;
    }
  }
  
  /**
   * This method is used to get Formatted Date/Time from millisecond.
   * @param dateTimeStamp
   * @param dateFormat
   * @return
   */
  public static String convertDateTimeStampToFormattedString(long dateTimeStamp, String dateFormat, TimeZone timeZone)
  {
    try
    {
      SimpleDateFormat trDateFormat = new SimpleDateFormat(dateFormat);
      
      //If TimeZone is null then it use default.
      if(timeZone != null)
        trDateFormat.setTimeZone(timeZone);
      
      Date userDate = new Date(dateTimeStamp);
      String formattedDate = trDateFormat.format(userDate);
      return formattedDate;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return "";
    }
  }
  
  /**
   * This method is used to convert millisecond to formatted Time String text.
   * @param timeInMillies
   * @param seperater
   * @return
   */
  public static String convertTimeToFormattedString(long timeInMillies, String seperater)
  {
    try
    {
      //First convert the millisecond to second.
      long seconds = timeInMillies/1000;
      
      //Calculate Hours.
      int hours = (int) (seconds / 3600);
      seconds = seconds % 3600;
      
      //Calculate Minutes.
      int minutes = (int) (seconds / 60);
      seconds = seconds % 60;
      
      
      //here we need to check if hour length is less than 2 then we need to decorate it.      
      String formattedString = timeDecorator(hours + "") + seperater + timeDecorator(minutes + "") + seperater + timeDecorator(seconds + "");
      
      return formattedString;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return "";
    }
  }
  
  
  /**
   * This method converts the formatted time string into millisecond.
   * @param formattedString
   * @param seperater
   * @return
   */
  public static long convertFormattedTimeToMillisecond(String formattedString, String seperater)
  {
    try
    {
      String arrTime[] = formattedString.split(seperater);
      
      //Validation for checking if time array size is not equals to 3, then return from here.
      if(arrTime == null || arrTime.length != 3)
      {
	return 0L;
      }
      
      //First calculate in seconds.
      int hourInSecond = Integer.parseInt(arrTime[0]) * 3600;
      int minuteInSecond = Integer.parseInt(arrTime[1]) * 60;
      int second = Integer.parseInt(arrTime[2]);
      
      long totalMilliscond = (hourInSecond + minuteInSecond + second) * 1000;
      
      return totalMilliscond;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return 0L;
    }
  }
   
  /**
   * This method is used to decorate time to display in format.
   * @param time
   * @return
   */
  private static String timeDecorator(String time)
  {
    if(time.length() == 1)
      return ("0" + time);
    else
      return time;
  }
  
  /**
   * Method used to get Epoch Start TimeStamp.
   */
  public static long getEpochTimeStamp(String epochYear, TimeZone timeZone)
  {
    try
    {
      String dateTimeFormat = "01/01/" + epochYear + " " + "00:00:00";
      return convertFormattedDateToMilliscond(dateTimeFormat, "MM/dd/yyyy HH:mm:ss", timeZone);
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return 0L;
    }
  }
  
  /**
   * Getting System Time Zone GMT offset.
   * @return
   */
  public static TimeZone getSystemTimeZoneGMTOffset()
  {
    try
    {
      //Initializing variable for executing command.
      CmdExec cmdExec = new CmdExec();            
      Vector result = cmdExec.getResultByCommand("date", "+%z #GMT", CmdExec.SYSTEM_CMD, "netstorm" , "root");
      
      if(result != null && !result.get(0).toString().contains("ERROR"))
      {
	return TimeZone.getTimeZone("GMT" + result.get(0).toString());
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    return TimeZone.getTimeZone(DEFAULT_TIMEZONE_GMT_OFFSET);
  }
  
  /**
   * Method is used to validate Date/Time Format. It will take a string and make sure it's in the proper
   * format as provided as argument, and it will also make sure that
   * it's a legal date.
   */
  public static boolean isValidDate(String date, String format)
  {

    SimpleDateFormat sdf = new SimpleDateFormat(format);

    Date testDate = null;

    // we will now try to parse the string into date form.
    try
    {
      testDate = sdf.parse(date);
    }

    // if the format of the string provided doesn't match the format we
    // declared in SimpleDateFormat() we will get an exception
    catch (ParseException e)
    {
      return false;
    }

    // dateformat.parse will accept any date as long as it's in the format
    // as defined, it simply rolls dates over, for example, December 32
    // becomes jan 1 and December 0 becomes November 30
    // This statement will make sure that once the string
    // has been checked for proper formatting that the date is still the
    // date that was entered, if it's not, it assume that the date is invalid
    if (!sdf.format(testDate).equals(date))
    {
      return false;
    }

    // if we make it to here without getting an error it is assumed that
    // the date was a valid one and that it's in the proper format
    return true;

  } 
  
  /**
   * Method is used to check the date pattern which is in (HH:MM:SS.sss) format.
   * @param matchString
   * @return
   */
  public static boolean isValidTimeFormatInHHMMSSsss(String matchString)
  {
    try
    {           
      /*Pattern for matching time HH:mm:SS.sss*/
      String datePattern = "^(\\d{2,})\\:?([0-5]?\\d)\\:?([0-5]?\\d).(\\d{3})";
      
      /*Compile date pattern.*/
      Pattern pattern = Pattern.compile(datePattern);
      
      /*Checking for Matching Pattern with input string.*/
      if(pattern.matcher(matchString).matches())
      {
	    return true;
      }   
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    return false;
  }  
  
  /**
   * Method is used to check the date pattern which is in (HH:mm:ss) format.
   * @param matchString
   * @return
   */
  public static boolean isValidTimeFormatInHHMMSS(String matchString)
  {
    try
    {           
      /*Pattern for matching time HH:mm:ss*/
      String datePattern = "^(\\d{2,})\\:?([0-5]?\\d)\\:?([0-5]?\\d)";
      
      /*Compile date pattern.*/
      Pattern pattern = Pattern.compile(datePattern);
      
      /*Checking for Matching Pattern with input string.*/
      if(pattern.matcher(matchString).matches())
      {
	return true;
      }   
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    return false;
  }
  
  
  /**
   * Checking weather or not string is in time format.
   * @param isTimeString
   * @return
   */
  public static boolean isTimeString(String isTimeString)
  {
    try
    {
      if(rptUtilsBean.split(isTimeString, ":").length == 3)
	return true;
      else
	return false;
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    
    return false;
  }
  

  public static void main(String[] args)
  {
   String dateString = ExecutionDateTime.convertDateTimeStampToFormattedString(1409052600000L, "MM/dd/yy HH:mm:ss", null);
   System.out.println("output = "+ isValidDate("03/04/14 12:13:59", "MM/dd/yy HH:mm:ss"));
   System.out.println("dateString = "+dateString);
   System.out.println("isVAlidDate in ms "+isValidTimeFormatInHHMMSSsss("12:06:56.075") );
   System.out.println("isValidDAte = " + isValidTimeFormatInHHMMSS("0000034324:00:00:"));   
   System.out.println("isTime == " + isTimeString("00000034:00:00:"));   
  }

}

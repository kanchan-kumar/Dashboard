//
// Name    : rptUtilsBean.java
// Author  : Neeraj/Prateek
// Purpose : Utility Bean for Analyze GUI
// Modification History:
//  09/03/05:1.4:Neeraj: Added getRecFlds() method
//  09/14/05:1.4:Abhishek: Added urlSubString() method
//  10/06/05:1.4:Abhishek: Added genTitle() method

package pac1.Bean;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFileChooser;


public class rptUtilsBean
{
  static String className = "rptUtilsBean";
  static CmdExec cmdExec = new CmdExec();
  // These are used to replce characters in graph file name as there are not allowed in filename.
  static char[] chrArr1 = new char[] { ' ', '(', ')', '%', '/' };
  static char[] chrArr2 = new char[] { '$', '~', '_', '@', '*' };
  private static final String GLOBAL_DAT_FILE_NAME = "global.dat";
  final static int size = 1024;

  // These will be used in Date Parameter
  private static final String[] arrDateFormatC = new String[] { "%m/%d/%Y", "%m/%d/%y", "%Y/%m/%d", "%d/%m/%Y", "%d/%m/%y", "%y/%d/%m", "%m-%d-%Y", "%m-%d-%y", "%Y-%m-%d", "%d-%m-%Y", "%d-%m-%y", "%y-%d-%m", "%m-%d-%y/%H:%M", "%H:%M/%m-%d-%y", "%r/%m-%d-%y", "%R/%m-%d-%y" };
  private static final String[] arrDateFormatJava = new String[] { "MM/dd/yyyy", "MM/dd/yy", "yyyy/MM/dd", "dd/MM/yyyy", "dd/MM/yy", "yy/dd/MM", "MM-dd-yyyy", "MM-dd-yy", "yyyy-MM-dd", "dd-MM-yyyy", "dd-MM-yy", "yy-dd-MM", "MM-dd-yy/HH:mm", "HH:mm/MM-dd-yy", "hh:mm:ss a/MM-dd-yy", "HH:mm/MM-dd-yy" };

  // This is removed from ScriptUtilis.java file
  // So make it comman in recorder and Analysis GUI move in this file
  private static String toEncodeDecode[][] = { { " ", "%20" }
      // ,{"<","%3C"}
      // ,{">", "%3E"}
      // ,{"#", "%23"}
      // ,{"%", "%25"}
  , { "{", "%7B" }, { "}", "%7D" }, { "|", "%7C" }
      // ,{"\\", "%5C"}
      // ,{"^", "%5E"}
  , { "~", "%7E" }, { "[", "%5B" }, { "]", "%5D" }, { "`", "%60" }
      // ,{";", "%3B"}
      // ,{"/", "%2F"}
      // ,{"?","%3F"}
      // ,{":", "%3A"}
      // ,{"@", "%40"}
  , { "=", "%3D" }
      // decode the %26 to & and %24 to $ in URL used in script mgt
  , { "&", "%26" }, { "$", "%24" } };

  // this is to encode/decode only for VUserTrace.
  private static String[][] toEncodeDecodeVUserTrace = { { "<", "%3C" }, { ">", "%3E" }, { "&", "%26" } };

  public rptUtilsBean()
  {
    // System.out.println("rptUtilsBean called");
  }

// This is used for getDateTime from FileName
  public String getDateTimeFromHarFile(String pageFileName)
  {
	Log.debugLog(className, "getDateTimeFromHarFile", "", "", "Method Called. File Name = " + pageFileName);
	try
	{
	  pageFileName = pageFileName.replace(".har","");
	  String[] dateTime = pageFileName.split("\\+");
	  dateTime[0] = dateTime[dateTime.length - 1];
	  dateTime[0]=dateTime[0].replace("-",":");
	  dateTime[1] = dateTime[dateTime.length - 2];
	  dateTime[1]=dateTime[1].replace("-","/");
	  String arrDateTime=dateTime[0]+" "+dateTime[1];
	  return arrDateTime;
	}
	catch(Exception e)
	{
	  Log.stackTraceLog(className, "getDateTime", "", "", "Exception in getting detail -", e);
	  return "";
    }
  }


// This is used for GetHostName
  public String getHostNameFromHarFile(String pageFileName)
  {
	Log.debugLog(className, "getHostNameFromHarFile", "", "", "Method Called. File Name = " + pageFileName);
	try
	{
	  String[] parsePageFileName = pageFileName.split("\\+");
	  String hostName;
      //For new format of HAR file
        if(parsePageFileName.length > 3)
        {
           hostName = parsePageFileName[2].toString();
           //Execute the block if hostname does not contain ip address
           if(!"1234567890".contains(hostName.charAt(0) + ""))
           {
             String[] splitDomainName = hostName.split("\\.");
             if(splitDomainName.length > 1)
             hostName = splitDomainName[1];
           }
        }
    //For old format of HAR file
        else
        {
          String[] domain = pageFileName.split("\\.");
          hostName = domain[domain.length - 3].toString();
          //Execute the block if hostname contains ip address
          if("1234567890".contains(hostName.charAt(0) + ""))
          {
           hostName = domain[2] + "." + domain[3] + "." + domain[4] + "." + domain[5].substring(0, domain[5].indexOf("+"));
          }
        }
    //The first character is changed to upper case
      char[] stringArray = hostName.toCharArray();
      stringArray[0] = Character.toUpperCase(stringArray[0]);
      hostName = new String(stringArray);
      return hostName;
	}
	catch(Exception e)
	{
      Log.stackTraceLog(className, "getHostName", "", "", "Exception in getting detail -", e);
      return "";
	}
  }

  // It is Used for get Page Name
	public String getPageNameFromHarFile(String pageFileName)
	{
	  Log.debugLog(className, "getPageNameFromHarFile", "", "", "Method Called. File Name = " + pageFileName);
	  try
	  {
	     String[] parsePageFileName = pageFileName.split("\\+");
	     String pageNameForHarFile;
	      //For new format of HAR file
	      if(parsePageFileName.length > 3)
	      {
	        pageNameForHarFile = pageFileName.substring(2, pageFileName.indexOf("+"));
	      }
	      //For old format of HAR file
	      else
	      {
	        pageNameForHarFile = pageFileName.substring(2, pageFileName.indexOf("."));
	      }

	      //The first character is changed to upper case
	      char[] stringArray = pageNameForHarFile.toCharArray();
	      stringArray[0] = Character.toUpperCase(stringArray[0]);
	      pageNameForHarFile = new String(stringArray);
	      return pageNameForHarFile;
	  	}
	  	catch(Exception e)
	  	{
		  Log.stackTraceLog(className, "getPageNameForHarFile", "", "", "Exception in getting detail -", e);
	  	  return "";
	    }
	 }

// It is Used for get ScreenSize and Page Name

    public String[] getPageNameScreenSizeFromHarFile(String pageNameForHarFile )
	{
		Log.debugLog(className, "getPageNameScreenSizeFromHarFile", "", "", "Method Called. File Name = " + pageNameForHarFile);
		try
		{
		  String[] parseScreenSize=null;
	      if(pageNameForHarFile.indexOf("(")>0)
	      {
	    	  pageNameForHarFile= pageNameForHarFile.replace("(", "#");
	    	  pageNameForHarFile=pageNameForHarFile.replace(")", "#");
	    	  parseScreenSize=pageNameForHarFile.split("#");
	      }
	      if(parseScreenSize[0].startsWith("P_"))
	      {
			  parseScreenSize[0]= parseScreenSize[0].substring(2);
			  char[] stringArray = parseScreenSize[0].toCharArray();
		      stringArray[0] = Character.toUpperCase(stringArray[0]);
	      	  parseScreenSize[0] = new String(stringArray);
		  }
       		return parseScreenSize;
   	 	}
	 	catch(Exception e)
		{
		  Log.stackTraceLog(className, "getPageName_ScreenSize", "", "", "Exception in getting detail -", e);
		  return null;
		}
    }

  // This will convert Info of a report in 2D Array
  public static String[][] rptInfoToArr(String rptInfo)
  {
    // Log.debugLog(className, "rptInfoToArr", "", "", "Getting graph details from Panels.");
    String[][] arrTemp = null;

    try
    {
      String[] strGrpGraphDetl = strToArrayData(rptInfo, ":");
      arrTemp = new String[strGrpGraphDetl.length][];

      for (int i = 0; i < strGrpGraphDetl.length; i++)
      {
        String[] strGrpGraphToken = strToArrayData(strGrpGraphDetl[i], "=");
        arrTemp[i] = strToArrayData(strGrpGraphToken[1], ",");
      }
      return arrTemp;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "rptInfoToArr", "", "", "Exception in getting detail -", e);
      return null;
    }
  }

  public static boolean isBitOn(int value, int bit)
  {
    int flag = 0;
    flag = flag | bit;
    if ((flag & value) == bit)
      return true;
    else
      return false;
  }

  // Name: rec2FldArr
  // Purpose: Extract fields from a record (pipe spearated) and fill these
  // fields in the array. This function also stores fields in the
  // desired index as speficied in the arrFldList. If field is emmpty,
  // default value is filled.
  // Arguments:
  // Arg1: Record with fields separated by pipe.
  // Arg2: Field array
  // Arg3: Number of expected fields in the record.
  // Arg4: Start index in the array from where field need to be filled.
  // This is to handle case when we are adding extra column at the start.
  // Arg5: Pipe separated list of of field Ids to be put in the array. This
  // is used if you want change the sequence of fileds in the array.
  // For example, if you want field Ids 1,5,2,3,4 in the array, then
  // pass this as "1|5|2|3|4".

  // Arg6: Default value of empty fields.
  // Return: none
  //

  // This moved from ScriptUtilis.java file
  public static String decode(String strUrl)
  {
    for (int i = 0; i < toEncodeDecode.length; i++)
    {
      strUrl = replace(strUrl, toEncodeDecode[i][1], toEncodeDecode[i][0]);
    }
    return strUrl;
  }

  // This is moved from ScriptUtilis.java file
  public static String encode(String strUrl)
  {
    for (int i = 0; i < toEncodeDecode.length; i++)
    {
      strUrl = replace(strUrl, toEncodeDecode[i][0], toEncodeDecode[i][1]);
    }
    return strUrl;
  }

  // this is to encode/decode only for VUserTrace.
  public static String decodeVUserTrace(String strUrl)
  {
    for (int i = 0; i < toEncodeDecodeVUserTrace.length; i++)
    {
      strUrl = replace(strUrl, toEncodeDecodeVUserTrace[i][1], toEncodeDecodeVUserTrace[i][0]);
    }
    return strUrl;
  }

  // this is to encode/decode only for VUserTrace.
  public static String encodeVUserTrace(String strUrl)
  {
    for (int i = 0; i < toEncodeDecodeVUserTrace.length; i++)
    {
      strUrl = replace(strUrl, toEncodeDecodeVUserTrace[i][0], toEncodeDecodeVUserTrace[i][1]);
    }
    return strUrl;
  }

  // This is to decode any character in Encoded Format except ' as this is causing a problem while loading jsp page
  public static String decodeAll(String url)
  {
    url = URLDecoder.decode(url);
    if (url.contains("'"))
      url = url.replaceAll("'", "%27");
    return url;
  }

  // This is to encode any character
  public static String encodeAll(String url)
  {
    url = URLEncoder.encode(url);
    return url;
  }

  // This is moved from ScriptUtilis.java file
  public static String replace(String source, String toReplace, String replaceWith)
  {
    Log.debugLog(className, "replace", "", "", "Method called. source = " + source + ", toReplace = " + toReplace + ", replaceWith = " + replaceWith);
    if (source != null)
    {
      final int len = toReplace.length();
      StringBuffer sb = new StringBuffer();
      int found = -1;
      int start = 0;
      while ((found = source.indexOf(toReplace, start)) != -1)
      {
        sb.append(source.substring(start, found));
        sb.append(replaceWith);
        start = found + len;
      }
      sb.append(source.substring(start));
      return sb.toString();
    }
    else
      return "";
  }

  public static void rec2FldArr(String strRecord, String arrFlds[], int numFlds, int startFldIdx, int arrFldList[], String strDefaultValue)
  {
    int fldNum = 0;
    String strRecSave = new String(strRecord);
    while (strRecord != null)
    {
      if (fldNum >= numFlds)
      {
        Log.errorLog(className, "rec2FldArr", "", "", "Number of fields in record are more than expected number of fields (extra fields ignored). Record  = " + strRecSave);
        break;
      }
      String fldVal;
      int index = strRecord.indexOf("|");
      if (index < 0)
      {
        fldVal = strRecord.trim();
        strRecord = null;
      }
      else
      {
        fldVal = strRecord.substring(0, index).trim();
        strRecord = strRecord.substring(index + 1);
      }
      // Replacing null with user defined string
      if (!strDefaultValue.equals("") && fldVal.equals(""))
        arrFlds[startFldIdx + arrFldList[fldNum]] = new String(strDefaultValue);
      else
        arrFlds[startFldIdx + arrFldList[fldNum]] = fldVal;
      fldNum++;
    }
    // if record have less fields, fillremaining fields with default value.
    if (fldNum != numFlds)
    {
      Log.errorLog(className, "rec2FldArr", "", "", "Number of fields in record are less than expected number of fields (missing fields filled with default). Record = " + strRecSave);
      for (; fldNum < numFlds; fldNum++)
      {
        if (!strDefaultValue.equals(""))
          arrFlds[startFldIdx + arrFldList[fldNum]] = new String(strDefaultValue);
        else
          arrFlds[startFldIdx + arrFldList[fldNum]] = new String("-");
      }
    }
  }

  // Name: getRecFlds
  // Purpose: Extract fields from all records (pipe spearated) and fill these
  // fields in 2D string array. This function also stores fields in the
  // desired index as speficied in the arrFldList. If field is emmpty,
  // default value is filled.
  // This function also allocate extra row at top and bottom and extra
  // columns at start and end. This is very used for JSP to fill
  // additional rows and columns based on it's need.
  // Arguments:
  // Arg1: Vector containing all Records with fields separated by pipe.
  // Arg2: Colum Names. If empty, first record in vector is not used
  // and this argument is used.
  // Arg3: Pipe separated list of of field Ids to be put in the array. This
  // is used if you want change the sequence of fileds in the array.
  // For example, if you want field Ids 1,5,2,3,4 in the array, then
  // pass this as "1|5|2|3|4".
  // Arg4: Default value of empty fields.
  // Arg5: Number of extra row to be allocated in 2D array at the top.
  // This is not vey useful and 0 will passed.
  // Arg6: Number of extra row to be allocated in 2D array at the bottom.
  // Arg7: Number of extra columns to be allocated in 2D array before column 0.
  // This is not vey useful and 0 will passed.
  // Arg8: Number of extra columns to be allocated in 2D array at the end.
  // Return: 2D Array or null if error.
  //

  // This method is for cases where last 4 args are all 0.
  public static String[][] getRecFlds(Vector vecRecs, String strColNames, String strFldList, String strDefaultValue)
  {
    return getRecFlds(vecRecs, strColNames, strFldList, strDefaultValue, 0, 0, 0, 0);

  }

  public static String[][] getRecFlds(Vector vecRecs, String strColNames, String strFldList, String strDefaultValue, int addRowTop, int addRowBottom, int addColStart, int addColEnd)
  {
    try
    {
      String strRecord = null;
      int arrFldList[] = null;
      String arrDataValues[][] = null;
      int numFlds, numRecs;
      int recNum, fldNum;

      Log.debugLog(className, "getRecFlds", "", "", "Vector size = " + vecRecs.size() + ", ColNames = " + strColNames + ", FldList = " + strFldList + ", DefaultValue = " + strDefaultValue);

      if ((vecRecs == null) || ((numRecs = vecRecs.size()) == 0))
      {
        Log.errorLog(className, "getRecFlds", "", "", "Vector is null or size is 0");
        return null;
      }
      // First generate the array of all field Ids to be returned in the array
      String arrTmpList[] = null;
      // Assumptiom is header does not have empty fields (like ||). So split can be used
      strRecord = new String((vecRecs.elementAt(0).toString()));
      arrTmpList = split(strRecord, "|");
      numFlds = arrTmpList.length;
      arrFldList = new int[numFlds];
      for (recNum = 0; recNum < numFlds; recNum++)
        arrFldList[recNum] = recNum;

      if (!strFldList.equals(""))
      {
        arrTmpList = split(strFldList, "|");
        // System.out.println("FldList  - " + arrToString(arrTmpList));
        if (arrTmpList.length != numFlds)
        {
          Log.errorLog(className, "getRecFlds", "", "", "Field List length (" + arrTmpList.length + ") is not same as number of fields in the record (" + numFlds + ")");
          return null;
        }
        for (recNum = 0; recNum < numFlds; recNum++)
          arrFldList[Integer.parseInt(arrTmpList[recNum])] = recNum;
      }

      arrDataValues = new String[numRecs + addRowTop + addRowBottom][numFlds + addColStart + addColEnd];
      for (recNum = 0; recNum < numRecs; recNum++)
      {
        strRecord = vecRecs.elementAt(recNum).toString();
        if ((recNum == 0) && (!strColNames.equals(""))) // Take column names from argument
          strRecord = strColNames;

        if ((strRecord.charAt(0) == '"')) // This is for windows as we get " at the start and end
          strRecord = strRecord.substring(1, (strRecord.length() - 1));

        rec2FldArr(strRecord, arrDataValues[recNum + addRowTop], numFlds, addColStart, arrFldList, strDefaultValue);
      }
      return arrDataValues;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "getRecFlds", "", "", "Exception - " + e);
      e.printStackTrace();
    }
    return null;
  }

  // Name: getNeededFlds
  // Purpose: Extract needed columns from 2D array.
  // Arguments:
  // Arg1: 2D Array
  // Arg2: Pipe separated list of of field Ids to be rturned in the array.
  // Return: 2D Array or null if error.
  //
  public static String[][] getNeededFlds(String arrInp[][], String strFldList)
  {
    try
    {
      int arrFldList[] = null;
      String arrDataValues[][] = null;
      int numFlds, numRecs;
      int recNum, fldNum;

      Log.debugLog(className, "getNeededFlds", "", "", "Input array size is " + arrInp.length + ", " + arrInp[0].length + ", FldList = " + strFldList);

      // First generate the array of all field Ids to be returned in the array
      String arrTmpList[] = null;
      arrTmpList = split(strFldList, "|");
      numFlds = arrTmpList.length;
      arrFldList = new int[numFlds];

      for (fldNum = 0; fldNum < numFlds; fldNum++)
        arrFldList[fldNum] = Integer.parseInt(arrTmpList[fldNum]);

      numRecs = arrInp.length;
      Log.debugLog(className, "getNeededFlds", "", "", "New Array size is " + numRecs + ", " + numFlds);
      arrDataValues = new String[numRecs][numFlds];
      for (recNum = 0; recNum < numRecs; recNum++)
      {
        for (fldNum = 0; fldNum < numFlds; fldNum++)
          arrDataValues[recNum][fldNum] = arrInp[recNum][arrFldList[fldNum]];
      }
      return arrDataValues;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "getNeededFlds", "", "", "Exception - " + e);
      e.printStackTrace();
    }
    return null;
  }

  // Name: swapRowCol
  // Purpose: Swap rows and columns in a 2D Array.
  // Arguments:
  // Arg1: 2D Array
  // Return: 2D Array or null if error.
  //

  /*
   * This method is used to get the current date and time from the server
   *
   * @param userDate
   *
   * @param userFormat
   *
   * @return String
   */
  public String getDatafromServer(String userDate, String userFormat)
  {
    Log.debugLog(className, "getDatafromServer", "", "", "method called . The userDate and userFormat =  " + userDate + ", " + userFormat);
    try
    {
      String strCurrDate = "";
      if (userFormat.equals(""))
        userFormat = "yyyy-MM-dd HH:mm";

      SimpleDateFormat sdf = new SimpleDateFormat(userFormat);
      Date date = new Date();

      strCurrDate = sdf.format(date);
      Date currDate = sdf.parse(strCurrDate);
      Date inputDate = sdf.parse(userDate);

      if (inputDate.compareTo(currDate) >= 0)
      {
        Log.debugLog(className, "getDatafromServer", "", "", "The input date is greater than the currentDate");
        return "";
      }

      return strCurrDate;
    }
    catch (ParseException ex)
    {
      Log.errorLog(className, "getDatafromServer", "", "", "Exception - " + ex);
      // ex.printStackTrace();
      return userDate;
    }
  }

  public static String[][] swapRowCol(String arrInp[][])
  {
    try
    {
      String arrDataValues[][] = null;
      int numRow, numCol;
      int rowNum, colNum;

      Log.debugLog(className, "swapRowCol", "", "", "Method called");

      numRow = arrInp.length;
      numCol = arrInp[0].length;
      arrDataValues = new String[numCol][numRow];
      for (rowNum = 0; rowNum < numRow; rowNum++)
      {
        for (colNum = 0; colNum < numCol; colNum++)
          arrDataValues[colNum][rowNum] = arrInp[rowNum][colNum];
      }
      return arrDataValues;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "swapRowCol", "", "", "Exception - " + e);
      e.printStackTrace();
    }
    return null;
  }

  public static String getFldList(int bitFldList, int startFldId)
  {
    String strFldList = "";
    int fldId = startFldId;
    while (bitFldList != 0)
    {
      if ((bitFldList & 1) != 0)
      {
        if (strFldList.equals(""))
          strFldList = "" + fldId;
        else
          strFldList = strFldList + "|" + fldId;
      }
      fldId++;
      bitFldList = bitFldList >> 1;
    }
    return strFldList;
  }

  // max input it for future use. Pass 0 for now.
  public static String timeInMilliSecToString(String strTime, double max)
  {
    long hh, mm, ss, msec, temp;
    double time = Double.parseDouble(strTime);
    time = Math.round(time);
    msec = (long) (time % 1000);
    time = (time - msec) / 1000;
    hh = (long) (time / 3600);
    time = time % 3600;
    mm = (long) (time / 60);
    time = time % 60;
    ss = (long) time;
    return (appendStringToTime(hh, mm, ss, msec));
  }

  public static String appendStringToTime(long hh, long mm, long ss, long msec)
  {
    String strMsec = "" + msec;
    String strSS = "" + ss;
    String strMM = "" + mm;
    String strHH = "" + hh;

    if (msec < 10)
      strMsec = "00" + msec; // Make msec 3 digit string
    if ((msec < 100) && (msec > 9))
      strMsec = "0" + msec; // Make msec 3 digit string
    if (ss < 10)
      strSS = "0" + ss; // Make sec 2 digit string
    if (mm < 10)
      strMM = "0" + mm; // Make minute 2 digit string
    if (hh < 10)
      strHH = "0" + hh; // Make hrs 2 digit string
    return ("" + strHH + ":" + strMM + ":" + strSS + "." + strMsec);
  }

  public static String calPct(String value, String total)
  {
    double per = (Double.parseDouble(value) * 100) / Double.parseDouble(total);
    // next lines are to make sure per is upto 2 decimal points
    // next lines are to make sure per is upto 2 decimal points
    per = per * 100;
    per = Math.round(per);
    per = per / 100;

    return ("" + per);
  }

  // Truncates URL after ? or : or max 64 characters
  public static String urlSubString(String urlName)
  {
    if (urlName.indexOf('?') != -1)
      urlName = urlName.substring(0, urlName.indexOf('?')) + "...";
    // Check for : from index 6 to skip http: or https:
    else if (urlName.indexOf(':', 6) != -1)
      urlName = urlName.substring(0, urlName.indexOf(':', 6)) + "...";

    if (urlName.length() > 64)
      urlName = urlName.substring(0, 64) + "...";

    return urlName;
  }

  // Following methods are copied from FileBean.java
  public static String[] split(String strList, String strSeparator)
  {
    StringTokenizer st = new StringTokenizer(strList, strSeparator);
    String arrDataValues[] = new String[st.countTokens()];
    int recNum = 0;
    while (st.hasMoreTokens())
      arrDataValues[recNum++] = st.nextToken().trim();
    return (arrDataValues);
  }

  public static String arrToString(String arrString[])
  {
    String strTmp = arrString[0];
    for (int recNum = 1; recNum < arrString.length; recNum++)
      strTmp = strTmp + "," + arrString[recNum];
    return (strTmp);
  }

  // End - From FileBean.java
  // Generate title. This is similar to genTitle() method in rptUtils.js
  // Any changes in this method should also be done in rptUtils.js.
  // This title is used for pdf/excel file for now.
  // We still need getTitle() in rptUtils.js as there is a hyperlink for Test run.
  public static String genTitle(String title, String testRun, String runPhase)
  {
    String strTitle = "";
    if (testRun == "All")
      strTitle = title;
    else if (runPhase.equals(""))
    {
      strTitle = title + " - Test Run Number : " + testRun;
    }
    else if (runPhase.equals("0"))
    {
      strTitle = title + " - Test Run Number : " + testRun + " (Accounting Over All Test Run)";
    }
    else if (runPhase.equals("1"))
    {
      strTitle = title + " - Test Run Number : " + testRun + " (Accounting Over Run Phase Only)";
    }
    return strTitle;
  }

  // To getting current date
  public static String getCurDateTime()
  {
    SimpleDateFormat smt = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
    return (smt.format(new Date()));
  }

  // To convert date in milli second
  public static long convertDateToMilliSec(String strDateTime)
  {
    Date timeDate = new Date(strDateTime);
    long timeInmilli = timeDate.getTime();
    return timeInmilli;
  }

  // Set date from string form to specific date format
  public static String setDateFormat(String format, String date)
  {
    Log.debugLog(className, "setDateFormat", "", "", "Start method");
    String tempStr = "";

    try
    {
      SimpleDateFormat smt = new SimpleDateFormat(format);
      tempStr = smt.format(new Date(date));

      return tempStr;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "setDateFormat", "", "", "Exception in Setting format -" + e);
      return "";
    }
  }

  // Set date from milli sec to specific date format
  public static String setDateFormat(String format, long date)
  {
    Log.debugLog(className, "setDateFormat", "", "", "Start method");
    String tempStr = "";

    try
    {
      SimpleDateFormat smt = new SimpleDateFormat(format);
      tempStr = smt.format(new Date(date));

      return tempStr;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "setDateFormat", "", "", "Exception in Setting format -" + e);
      return "";
    }
  }

  // Convert record from array to string through saperator
  public static String strArrayToStr(String[] tempRecord, String separator)
  {
    Log.debugLog(className, "strArrayToStr", "", "", "Start method");
    String tempStr = null;
    try
    {
      for (int i = 0; i < tempRecord.length; i++)
      {
        if (i == 0)
          tempStr = tempRecord[i];
        else
          tempStr = tempStr + separator + tempRecord[i];
      }

      return tempStr;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "strArrayToStr", "", "", "Exception in change from array to string -" + e);
      return null;
    }
  }

  // Convert record from array to string through saperator
  public static String intArrayToStr(int[] tempRecord, String separator)
  {
    Log.debugLog(className, "intArrayToStr", "", "", "Method called");
    String tempStr = null;
    try
    {
      for (int i = 0; i < tempRecord.length; i++)
      {
        if (i == 0)
          tempStr = "" + tempRecord[i];
        else
          tempStr = tempStr + separator + tempRecord[i];
      }

      return tempStr;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "intArrayToStr", "", "", "Exception in change from int array to string -" + e);
      return null;
    }
  }

  // Convert record from array to string through saperator
  public static String[] updateRtplDetail(String[] arrRtplRecs, String updateOptions)
  {
    Log.debugLog(className, "updateRtplDetail", "", "", "updateOptions =" + updateOptions);
    String[] tempArr = null;

    try
    {
      tempArr = strToArrayData(updateOptions, "|");
      arrRtplRecs[4] = tempArr[0];
      arrRtplRecs[5] = tempArr[1];
      arrRtplRecs[6] = tempArr[2];
      arrRtplRecs[7] = tempArr[3];
      arrRtplRecs[8] = tempArr[4];
      arrRtplRecs[9] = tempArr[5];
      arrRtplRecs[10] = tempArr[6];

      return arrRtplRecs;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "updateRtplDetail", "", "", "Exception in updating array -" + e);
      return null;
    }
  }

  public static String longArrayToStr(long[] tempRecord, String separator)
  {
    Log.debugLog(className, "longArrayToStr", "", "", "Method called");
    
    String tempStr = null;
    try
    {
      for (int i = 0; i < tempRecord.length; i++)
      {
        if (i == 0)
          tempStr = "" + tempRecord[i];
        else
          tempStr = tempStr + separator + tempRecord[i];
      }

      return tempStr;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "longArrayToStr", "", "", "Exception in change from int array to string -" + e);
      return null;
    }
  }

  
  // Convert record from array to string through saperator
  public static String[] strToArrayData(String tempRecord, String separator)
  {
    // Log.debugLog(className, "strToArrayData", "", "", "tempRecord =" + tempRecord);
    String[] tempArr = null;
    try
    {
      StringTokenizer stTemp = new StringTokenizer(tempRecord, separator);
      tempArr = new String[stTemp.countTokens()];

      int i = 0;
      while (stTemp.hasMoreTokens())
      {
        tempArr[i] = stTemp.nextToken();
        i++;
      }

      return tempArr;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "strToArrayData", "", "", "Exception in change from string to array -", e);
      return null;
    }
  }

  // Convert string to array of integer
  public static int[] strToArrayOfInt(String strArray, String separator)
  {
    StringTokenizer st = new StringTokenizer(strArray, separator);
    int[] tempArr = new int[st.countTokens()];
    int j = 0;
    while (st.hasMoreTokens())
    {
      tempArr[j] = Integer.parseInt((st.nextToken()).toString());
      j++;
    }
    return tempArr;
  }

  // For changing file permisson
  public static boolean changeFilePerm(String dirOrfile, String owner, String group, String permision)
  {
    try
    {
      Log.debugLog(className, "changeGraphFilePerm", "", "", "DirOrfile =" + dirOrfile + ", Owner=" + owner + ", Group=" + group + ", Permision=" + permision);
      String strCmdArgs = "'" + dirOrfile + "'" + " " + owner + " " + group + " " + permision;

      // Run as root
      Vector vecCmdOut = cmdExec.getResultByCommand("nsi_change_perm", strCmdArgs, CmdExec.NETSTORM_CMD, "", "root");
      if ((vecCmdOut.size() > 0) && ((String) vecCmdOut.lastElement()).startsWith("ERROR"))
      {
        Log.errorLog(className, "changeGraphFilePerm", "", "", "Error in changing Dir/File permission of  " + dirOrfile);
        return false;
      }
      return true;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "changeGraphFilePerm", "", "", "Exception while changing Dir/File permission -" + e);
      return false;
    }
  }

  // change time in hh:mm:ss format
  public static String convMilliSecToStr(long timeStr)
  {
    // Log.debugLog(className, "convMilliSecToStr", "", "", "Method called. Time is  " + timeStr);
    long milliSecTmp = 0;
    String tempSec = "";
    String tempMin = "";
    String tempHrs = "";

    try
    {
      int secTmp = (int) timeStr / 1000;
      int minuteTmp = secTmp / 60;
      secTmp = secTmp % 60;
      int hourTmp = minuteTmp / 60;
      minuteTmp = minuteTmp % 60;

      if (secTmp < 10)
        tempSec = 0 + "" + secTmp;
      else
        tempSec = "" + secTmp;

      if (minuteTmp < 10)
        tempMin = 0 + "" + minuteTmp;
      else
        tempMin = "" + minuteTmp;

      if (hourTmp < 10)
        tempHrs = 0 + "" + hourTmp;
      else
        tempHrs = "" + hourTmp;

      return (tempHrs + ":" + tempMin + ":" + tempSec);
    }
    catch (Exception e)
    {
      Log.errorLog(className, "convMilliSecToStr", "", "", "Error while converting time from Milli sec to String - " + e);
      return "";
    }
  }


   // change time in hh:mm:ss,SSS format
  public static String convMilliSecToStrFullFormat(long timeStr)
  {
    // Log.debugLog(className, "convMilliSecToStr", "", "", "Method called. Time is  " + timeStr);
    long milliSecTmp = 0;
    String tempMilliSec = "";
    String tempSec = "";
    String tempMin = "";
    String tempHrs = "";

    try
    {
      int milliSec = (int) (timeStr % 1000);
      int secTmp = (int) timeStr / 1000;
      int minuteTmp = secTmp / 60;
      secTmp = secTmp % 60;
      int hourTmp = minuteTmp / 60;
      minuteTmp = minuteTmp % 60;
     
      if(milliSec < 10)
        tempMilliSec = "00" + milliSec;
      else if(milliSec < 100)
        tempMilliSec = "0" + milliSec;
      else 
        tempMilliSec = "" + milliSec;

      if (secTmp < 10)
        tempSec = 0 + "" + secTmp;
      else
        tempSec = "" + secTmp;

      if (minuteTmp < 10)
        tempMin = 0 + "" + minuteTmp;
      else
        tempMin = "" + minuteTmp;

      if (hourTmp < 10)
        tempHrs = 0 + "" + hourTmp;
      else
        tempHrs = "" + hourTmp;

      return (tempHrs + ":" + tempMin + ":" + tempSec+ "." + tempMilliSec);
    }
    catch (Exception e)
    {
      Log.errorLog(className, "convMilliSecToStrFullFormat", "", "", "Error while converting time from Milli sec to String - " + e);
      return "NA";
    }
  }
  // Converts time in HH:MM:SS in milli-seconds
  public static long convStrToMilliSec(String timeStr)
  {
    // Log.debugLog(className, "convStrToMilliSec", "", "", "Method called. Time is  " + timeStr);
    long milliSecTmp = 0;
    try
    {
      if (timeStr.trim().equals("NA"))
      {
        Log.debugLog(timeStr, "convStrToMilliSec", "", "", "timeStr = NA, then milliSecTmp = 0");
        return milliSecTmp;
      }

      StringTokenizer st = new StringTokenizer(timeStr, ":");
      long hourTmp = 0;
      long minuteTmp = 0;
      long secTmp = 0;

      if (st.countTokens() == 3)
      {
        hourTmp = Long.parseLong(st.nextToken().trim());
        minuteTmp = Long.parseLong(st.nextToken().trim());
        secTmp = Long.parseLong(st.nextToken().trim());
      }
      else if (st.countTokens() == 2)
      {
        Log.debugLog(className, "convStrToMilliSec", "", "", "Concatinating :00 for seconds in time " + timeStr);
        hourTmp = Long.parseLong(st.nextToken().trim());
        minuteTmp = Long.parseLong(st.nextToken().trim());
      }

      secTmp = hourTmp * 3600 + minuteTmp * 60 + secTmp;
      milliSecTmp = secTmp * 1000;

      return milliSecTmp;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "convStrToMilliSec", "", "", "Error while converting time (" + timeStr + ") from String to Milli sec - " + e);
      return 0;
    }
  }

  // This will used for debug log detail of All graph names
  public static java.util.List strArrayToList(String[] strArray)
  {
    java.util.List strList = Arrays.asList(strArray);
    return strList;
  }

  // This will used for debug log detail of All graph indexes
  public static java.util.List intArrayToList(int[] intArray)
  {
    String[] strArray = new String[intArray.length];
    for (int i = 0, n = intArray.length; i < n; i++)
    {
      strArray[i] = "" + intArray[i];
    }
    java.util.List strList = Arrays.asList(strArray);

    return strList;
  }

  // This will used for debug log detail of All data value
  public static java.util.List doubleArrayToList(double[] doubleArray)
  {
    String[] strArray = new String[doubleArray.length];
    for (int i = 0, n = doubleArray.length; i < n; i++)
    {
      strArray[i] = "" + doubleArray[i];
    }
    java.util.List strList = Arrays.asList(strArray);

    return strList;
  }

  // Converts time in HHMMSS to HH:MM:SS (with colon)
  // Note that time can be NA also or only MMSS or HMMSS
  public static String timeWithColon(String timeStr)
  {
    Log.debugLog(className, "timeWithColon", "", "", "Method called. Time is  " + timeStr);

    try
    {
      String strTime;
      int len = timeStr.length();

      if (len > 4)
        strTime = (timeStr.substring(0, len - 4) + ":" + timeStr.substring(len - 4, len - 2) + ":" + timeStr.substring(len - 2));
      else if (len > 2)
        strTime = (timeStr.substring(0, len - 2) + ":" + timeStr.substring(len - 2));
      else
        strTime = (timeStr);
      Log.debugLog(className, "timeWithColon", "", "", "Time with colon is  " + strTime);
      return (strTime);
    }
    catch (Exception e)
    {
      Log.errorLog(className, "timeWithColon", "", "", "Error while converting time (" + timeStr + ") - " + e);
      return "";
    }
  }

  // Converts time in HH:MM:SS to HHMMSS (without colon)
  // Note time can come as NA also. (or only MM:SS). So keep logic generic
  public static String timeWithoutColon(String timeStr)
  {
    Log.debugLog(className, "timeWithoutColon", "", "", "Method called. Time is  " + timeStr);

    try
    {
      StringTokenizer st = new StringTokenizer(timeStr, ":");
      String strTime = "";
      while (st.hasMoreTokens())
        strTime = strTime + st.nextToken();

      return (strTime);
    }
    catch (Exception e)
    {
      Log.errorLog(className, "timeWithoutColon", "", "", "Error while converting time (" + timeStr + ") - " + e);
      return ""; // Neeraj - How to handle this error?
    }
  }

  // To Convert the double value upto specified number of decimal digit.
  public static String convertTodecimal(double value, int numDigit)
  {
    Log.debugLog(className, "convertTodecimal", "", "", "Method called. value is  " + value);

    try
    {
      String number = value + ""; // convert double to string
      int index = number.indexOf(".");
      String totalDigitAfterDecimal = "#.#"; // for calculating no. of digits after decimal

      if (index != -1 && value != 0) // check if number is Integer type
      {
        String checkValueAfterDecimal = number.substring(number.indexOf(".") + 1, number.length());
        if (checkValueAfterDecimal.equals("0"))
        {
          if (number.trim().startsWith("-"))
            numDigit = numDigit + number.length() - 3;
          else
            numDigit = numDigit + number.length() - 2;
          // number = String.format("%."+numDigit+"g%n", value);
          number = String.format("%." + numDigit + "g", value);
          return number;
        }
      }
      if (numDigit > 0)
      {
        for (int i = 1; i < numDigit; i++)
          // Calculating decimal digits.
          totalDigitAfterDecimal = totalDigitAfterDecimal + "#";
      }
      else
        totalDigitAfterDecimal = "";
      DecimalFormat df = new DecimalFormat(totalDigitAfterDecimal);
      df.setMinimumFractionDigits(numDigit);
      number = df.format(value);
      return number;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "convertTodecimal", "", "", "Error while converting to decimal digits(" + value + ") - " + e);
      return value + ""; // return same value if exception is occur.
    }
  }

  // This will return report's index data from array of report indexes
  public static double[] getDataFromArrayByIdx(int[] rptIndx, double[][] arrGraphData, int rptIdx)
  {
    Log.debugLog(className, "getDataFromArrayByIdx", "", "", "Method called. Report index = " + rptIdx + ", All Index = " + intArrayToList(rptIndx));

    try
    {
      for (int i = 0; i < rptIndx.length; i++)
        if (rptIndx[i] == rptIdx)
          return (arrGraphData[i]);

      Log.errorLog(className, "getDataFromArrayByIdx", "", "", "Invalid report index - " + rptIdx);
      return null;

    }
    catch (Exception e)
    {
      Log.errorLog(className, "getDataFromArrayByIdx", "", "", "Exception while getting idx data -" + e);
      return null;
    }
  }

  // This will calculate start and end time when Absolute time format exist

  // This will convert Report Info from array to 2D array Array
  public static String[][] rptDetailArrTo2DArr(String[] rptInfo)
  {
    Log.debugLog(className, "rptDetailArrTo2DArr", "", "", "Getting report details.");
    String[][] arrTemp = new String[4][rptInfo.length];

    try
    {
      for (int i = 0; i < rptInfo.length; i++)
      {
        String[] strGrpGrhDetl = rptUtilsBean.strToArrayData(rptInfo[i], "|");
        arrTemp[0][i] = strGrpGrhDetl[0];
        arrTemp[1][i] = strGrpGrhDetl[1];
        arrTemp[2][i] = strGrpGrhDetl[2];
        arrTemp[3][i] = strGrpGrhDetl[3];
      }

      return arrTemp;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "rptDetailArrTo2DArr", "", "", "Exception in getting detail -" + e);
      return null;
    }
  }

  // To convert string array to integer array
  public static int[] strArrayToIntArray(String[] strArray)
  {

    String strDebug = strArrayToStr(strArray, "|");
    Log.debugLog(className, "strArrayToIntArray", "", "", "Method Called. Array is " + strDebug);

    int[] tempArr = new int[strArray.length];
    for (int i = 0; i < tempArr.length; i++)
      tempArr[i] = Integer.parseInt(strArray[i]);

    return tempArr;
  }

  // This will remove duplicate values
  public static int[] getUniqueDataFromArray(int[] arrOfValue)
  {
    Log.debugLog(className, "getUniqueDataFromArray", "", "", "Method called. ");
    Vector tempIdx = new Vector();
    int[] rptIds = null;

    try
    {
      Object[] obj = new Object[arrOfValue.length];
      for (int k = 0; k < arrOfValue.length; k++)
      {
        obj[k] = "" + arrOfValue[k];
      }
      SortedSet sortedSet = new TreeSet(Arrays.asList(obj));

      Iterator it = sortedSet.iterator();

      while (it.hasNext())
      {
        tempIdx.add(it.next());
      }

      rptIds = new int[tempIdx.size()];

      for (int i = 0; i < rptIds.length; i++)
      {
        rptIds[i] = Integer.parseInt(tempIdx.elementAt(i).toString());
      }
      return rptIds;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "getUniqueDataFromArray", "", "", "Exception in getting report indexes - " + e);
      return null;
    }
  }

  // This will remove duplicate values
  public static String[] getUniqueValArray(Vector fileName)
  {
    Log.debugLog(className, "getUniqueValArray", "", "", "Method called. ");
    Vector tempIdx = new Vector();
    String[] tempFiles = null;

    try
    {
      Object[] obj = new Object[fileName.size()];
      for (int k = 0; k < fileName.size(); k++)
      {
        String str = fileName.elementAt(k).toString();
        StringTokenizer st = new StringTokenizer(str, "-");
        obj[k] = "" + st.nextToken();
      }
      SortedSet sortedSet = new TreeSet(Arrays.asList(obj));

      Iterator it = sortedSet.iterator();

      while (it.hasNext())
      {
        tempIdx.add(it.next());
      }

      tempFiles = new String[tempIdx.size()];

      for (int i = 0; i < tempFiles.length; i++)
      {
        tempFiles[i] = tempIdx.elementAt(i).toString();
      }

      return tempFiles;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "getUniqueValArray", "", "", "Exception in getting report indexes - " + e);
      return null;
    }
  }

  // This will replace special charactor from string name
  public static String doReplaceName(String rptName)
  {
    for (int ii = 0, nn = chrArr1.length; ii < nn; ii++)
    {
      rptName = rptName.replace(chrArr1[ii], chrArr2[ii]);
    }
    return rptName;
  }

  // This will undo replace special charactor from string name
  public static String undoReplaceName(String rptName)
  {
    for (int ii = 0, nn = chrArr1.length; ii < nn; ii++)
    {
      rptName = rptName.replace(chrArr2[ii], chrArr1[ii]);
    }
    return rptName;
  }

  // This will convert Report Info of a report in 2D Array
  public static int noOfRptInInfo(String rptInfo)
  {
    Log.debugLog(className, "noOfRptInInfo", "", "", "Getting no of reports.");
    String[][] arrTemp = null;

    try
    {
      String[] strGrpGrhDetl = rptUtilsBean.strToArrayData(rptInfo, ":");
      arrTemp = new String[strGrpGrhDetl.length][];

      for (int i = 0; i < strGrpGrhDetl.length; i++)
      {
        String[] strGrpGrhToken = rptUtilsBean.strToArrayData(strGrpGrhDetl[i], "=");
        arrTemp[i] = rptUtilsBean.strToArrayData(strGrpGrhToken[1], ",");
      }

      return arrTemp[0].length;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "noOfRptInInfo", "", "", "Exception in getting rpt number -" + e);
      return 1;
    }
  }

  // This will decode URL string get from browser
  public static String unescapeURL(String s)
  {
    StringBuffer sbuf = new StringBuffer();
    int l = s.length();
    int ch = -1;
    int b, sumb = 0;
    for (int i = 0, more = -1; i < l; i++)
    {
      /* Get next byte b from URL segment s */
      switch (ch = s.charAt(i))
      {
        case '%':
          ch = s.charAt(++i);
          int hb = (Character.isDigit((char) ch) ? ch - '0' : 10 + Character.toLowerCase((char) ch) - 'a') & 0xF;
          ch = s.charAt(++i);
          int lb = (Character.isDigit((char) ch) ? ch - '0' : 10 + Character.toLowerCase((char) ch) - 'a') & 0xF;
          b = (hb << 4) | lb;
          break;
        case '+':
          b = ' ';
          break;
        default:
          b = ch;
      }
      /* Decode byte b as UTF-8, sumb collects incomplete chars */
      if ((b & 0xc0) == 0x80)
      { // 10xxxxxx (continuation byte)

        sumb = (sumb << 6) | (b & 0x3f); // Add 6 bits to sumb
        if (--more == 0)
          sbuf.append((char) sumb); // Add char to sbuf
      }
      else if ((b & 0x80) == 0x00)
      { // 0xxxxxxx (yields 7 bits)
        sbuf.append((char) b); // Store in sbuf
      }
      else if ((b & 0xe0) == 0xc0)
      { // 110xxxxx (yields 5 bits)
        sumb = b & 0x1f;
        more = 1; // Expect 1 more byte
      }
      else if ((b & 0xf0) == 0xe0)
      { // 1110xxxx (yields 4 bits)
        sumb = b & 0x0f;
        more = 2; // Expect 2 more bytes
      }
      else if ((b & 0xf8) == 0xf0)
      { // 11110xxx (yields 3 bits)
        sumb = b & 0x07;
        more = 3; // Expect 3 more bytes
      }
      else if ((b & 0xfc) == 0xf8)
      { // 111110xx (yields 2 bits)
        sumb = b & 0x03;
        more = 4; // Expect 4 more bytes
      }
      else
      /* if ((b & 0xfe) == 0xfc) */
      { // 1111110x (yields 1 bit)
        sumb = b & 0x01;
        more = 5; // Expect 5 more bytes
      }
      /* We don't test if the UTF-8 encoding is well-formed */
    }
    return sbuf.toString();
  }

  public static boolean isRTGFileAvail(String numTestRun)
  {
    String rtgMsgFilePath = Config.getWorkPath() + "/webapps/logs/TR" + numTestRun + "/rtgMessage.dat";

    Log.debugLog(className, "isRTGFileAvail", "", "", "RTG data file path - " + rtgMsgFilePath);

    File rtgMsgFileObj = new File(rtgMsgFilePath);
    if (rtgMsgFileObj.exists())
      return true;
    else
      return false;
  }

  // This function check that directory is exist or not
  public static boolean isDirPathExist(String dirPath)
  {
    File fileObj = new File(dirPath);

    if (fileObj.isDirectory())
      return true;
    else
      return false;
  }

  // This function check that file is exist or not
  public static boolean isFileExist(String filePath)
  {
    Log.debugLog(className, "isFileExist", "", "", "Method Called. File Path = " + filePath);

    File fileObj = new File(filePath);

    if (fileObj.exists())
      return true;
    else
      return false;
  }

  // This will read global data file and return different phases of test run
  public static double[] getPhaseTimes(String numTestRun)
  {
    Log.debugLog(className, "getPhaseTimes", "", "", "Method Called. Test Run = " + numTestRun);

    double[] phaseTimes = null;
    String globalDataFile = Config.getWorkPath() + "/webapps/logs/TR" + numTestRun + "/" + GLOBAL_DAT_FILE_NAME;
    FileInputStream fis = null;

    try
    {
      String str1 = "";
      StringTokenizer st3;
      String str5[];

      fis = new FileInputStream(globalDataFile);

      Log.debugLog(className, "getPhaseTimes", "", "", "globalFilepath =" + globalDataFile);

      BufferedReader br = new BufferedReader(new InputStreamReader(fis));
      int j = 0;

      while ((str1 = br.readLine()) != null)
      {
        j = 0;
        if (str1.indexOf("PHASE_TIMES") != -1)
        {
          phaseTimes = new double[4];
          st3 = new StringTokenizer(str1);
          str5 = new String[st3.countTokens()];
          while (st3.hasMoreTokens())
          {
            str5[j] = st3.nextToken();
            j++;
          }
          // phaseTimes = new double[str5.length - 1];
          for (int ii = 1, mm = str5.length; ii < mm; ii++)
          {
            phaseTimes[ii - 1] = Double.parseDouble(str5[ii]) * 1000;
            Log.debugLog(className, "getPhaseTimes", "", "", "Phase Time =" + phaseTimes[ii - 1]);
          }
        }
      }
      br.close();
      fis.close();
      return phaseTimes;
    }
    catch (FileNotFoundException e)
    {
      // Do not return false if file is not found. This will allow user to generate graphs for running test runs.
      Log.errorLog(className, "getPhaseTimes", "", "", "File (" + globalDataFile + ") not found. Ignored as test run may be running. Using 0 for all times");
      // Make it null so that we can check letter data is there or not.
      phaseTimes = null;
      return phaseTimes;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "getPhaseTimes", "", "", "Exception while reading data file" + e);
      phaseTimes = null;
      return phaseTimes;
    }
  }

  // copy fileSrc to fileDest
  public static boolean copyToFile(String fileSrcName, String fileDestName)
  {
    try
    {
      File fileSrc = new File(fileSrcName);
      File fileDest = new File(fileDestName);
      if (!fileSrc.exists())
      {
        Log.errorLog(className, "copyToFile", "", "", "Source file does not exits. Filename = " + fileSrcName);
        return false;
      }

      /*
       * Remove this check because FileOutputStream will create file if it was not there.: Atul 04/02/09
       *
       * if(!fileDest.exists()) { Log.errorLog(className, "appendToFile", "", "", "Destination file does not exits. Filename = " + fileDestName); return false; }
       */

      FileInputStream fin = new FileInputStream(fileSrc);
      BufferedReader br = new BufferedReader(new InputStreamReader(fin));
      FileOutputStream fout = new FileOutputStream(fileDest, false); // not append mode
      PrintStream pw = new PrintStream(fout);
      String str;
      while ((str = br.readLine()) != null)
        pw.println(str);

      pw.close();
      br.close();
      fin.close();
      fout.close();
      return true;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "copyToFile", "", "", "Exception in appendToFile()" + e);
      return false;
    }
  }

  /**
   * Filter the specified string for characters that are sensitive to HTML interpreters, returning the string with these characters replaced by the corresponding character entities.
   *
   * @param s
   * @return String
   */
  public static final String escapeHTML(String s)
  {
    StringBuffer sb = new StringBuffer();
    if (s == null)
      return s;
    int n = s.length();
    for (int i = 0; i < n; i++)
    {
      char c = s.charAt(i);
      escapeHtmlChar(sb, c, false);
    }
    return sb.toString();
  }

  public static final String escapeHTML(String s, boolean escapeSpace)
  {
    StringBuffer sb = new StringBuffer();
    if (s == null)
      return s;
    int n = s.length();
    for (int i = 0; i < n; i++)
    {
      char c = s.charAt(i);
      escapeHtmlChar(sb, c, escapeSpace);
    }
    return sb.toString();
  }

  public static final String handleSpecialChars(String s)
  {
    StringBuffer sb = new StringBuffer();
    if (s == null)
      return s;
    int n = s.length();
    for (int i = 0; i < n; i++)
    {
      char c = s.charAt(i);
      if (c == '"')
        sb.append("\\\"");
      else if (c == '\\')
        sb.append("\\\\");
      else if (c == '\'')
        sb.append("\\\'");
      else if (c == '\t')
        sb.append(" ");
      else if (c == '\f')
        sb.append(" ");
      else
        sb.append(c);
    }
    return sb.toString();
  }

  public static void escapeHtmlChar(StringBuffer sb, char c, boolean escapeSpace)
  {
    switch (c)
    {
      case '<':
        sb.append("&lt;");
        break;
      case '>':
        sb.append("&gt;");
        break;
      case '&':
        sb.append("&amp;");
        break;
      case '"':
        sb.append("&quot;");
        break;
      case ' ':
        if (escapeSpace)
        {
          sb.append("&nbsp;");
        }
        else
        {
          sb.append(c);
        }
        break;

      default:
        sb.append(c);
        break;
    }
  }

  public static final ArrayList getDiffOfServerSignature(ArrayList compareList)
  {
    ArrayList result = new ArrayList();

    if (compareList.size() <= 1)
      return result;

    // Breaking String on New Line
    ArrayList arrFirstTestData = new ArrayList(Arrays.asList(compareList.get(0).toString().split("\n")));

    boolean isFirstComparision = true;
    int rowCount1 = arrFirstTestData.size();

    for (int k = 1; k < compareList.size(); k++)
    {
      ArrayList arr2 = new ArrayList(Arrays.asList(compareList.get(k).toString().split("\n")));

      int rowCount2 = arr2.size();
      int maxCount = 0;

      boolean checkTR1 = false;
      boolean checkTR2 = false;

      if (rowCount1 > rowCount2)
        maxCount = rowCount1;
      else
        maxCount = rowCount2;

      if (k > 1)
        isFirstComparision = false;

      ArrayList modified1 = new ArrayList();
      ArrayList modified2 = new ArrayList();

      boolean TR1Flag = false;
      boolean TR2Flag = false;

      for (int i = 0; i < maxCount; i++)
      {
        String[] str2 = new String[2];
        String[] str1 = new String[2];

        if (i >= arrFirstTestData.size())
        {
          str2[0] = arr2.get(i).toString();

          for (int j = 0; j < rowCount1; j++)
          {
            if (str2[0].equals(arrFirstTestData.get(j).toString()))
            {
              TR2Flag = true;
              str2[1] = "S";
              break;
            }
          }

          if (!TR2Flag)
          {
            str2[1] = "N";
          }

          if (isFirstComparision)
          {
            str1[0] = "-------------------";
            str1[1] = "D";
            modified1.add(i, str1);
          }

          modified2.add(i, str2);
          continue;
        }
        if (i >= arr2.size())
        {
          if (isFirstComparision)
          {
            str1[0] = arrFirstTestData.get(i).toString();

            for (int j = 0; j < rowCount2; j++)
            {
              if (str1[0].equals(arr2.get(j).toString()))
              {
                TR1Flag = true;
                str1[1] = "S";
                break;
              }
            }

            if (!TR1Flag)
              str1[1] = "N";
            modified1.add(i, str1);
          }

          str2[0] = "-------------------";
          str2[1] = "D";

          modified2.add(i, str2);
          continue;
        }

        checkTR1 = false;
        checkTR2 = false;

        String strLine1 = arrFirstTestData.get(i).toString();
        String strLine2 = arr2.get(i).toString();
        if (strLine1.equals(strLine2))
        {
          if (isFirstComparision)
          {
            str1[0] = arrFirstTestData.get(i).toString();
            str1[1] = "S";
            modified1.add(i, str1);
          }
          str2[0] = arr2.get(i).toString();
          str2[1] = "S";

          modified2.add(i, str2);
        }
        else
        {
          for (int j = 0; j < rowCount2; j++)
          {
            if (strLine1.equals(arr2.get(j).toString()))
            {
              str1[0] = strLine1;
              str1[1] = "S";
              checkTR1 = true;
              break;
            }
          }
          for (int j = 0; j < rowCount1; j++)
          {
            if (strLine2.equals(arrFirstTestData.get(j).toString()))
            {
              str2[0] = strLine2;
              str2[1] = "S";
              checkTR2 = true;
              break;
            }
          }

          if (!checkTR1)
          {
            str1[0] = strLine1;
            str1[1] = "M";
          }

          if (!checkTR2)
          {
            str2[0] = strLine2;
            str2[1] = "M";
          }

          if (isFirstComparision)
            modified1.add(i, str1);
          modified2.add(i, str2);
        }
      }

      if (isFirstComparision)// this for first Test Run
        result.add(modified1);
      result.add(modified2);

    }

    return result;
  }

  /**
   * Method to replace special characters in a String and then convert them into HTML format
   *
   * @param s
   * @return
   */
  public static final String escapeHTMLWithSpecialChars(String s)
  {
    if (s == null)
      return s;

    String str1 = handleSpecialChars(s);
    String str2 = escapeHTML(str1, false);

    return str2;
  }

  /**
   * This method will insert <br>
   * elements after number of characters passed as afterChars
   *
   * @param sourceString
   * @param afterChars
   * @return
   */
  public static final String insertHTMLLineBreaks(String sourceString, int afterChars)
  {
    String strResult = "";
    String HTMLLineBreak = "<br>";

    // If Source String is less than or equal to afterChars
    if (sourceString.length() <= afterChars)
      return sourceString;

    // Breaking String on New Line
    StringTokenizer tokenizer = new StringTokenizer(sourceString, "\n");

    while (tokenizer.hasMoreTokens())
    {
      String strLine = tokenizer.nextToken();

      // if String token length is less than equal to afterChars then append HTML Line Break
      if (strLine.length() <= afterChars)
        strResult = strResult + strLine + HTMLLineBreak;
      else
      {
        int i = 0;
        // Inserting HTML Line Breaks after each afterChars characters
        while (i < (strLine.length() / afterChars))
        {
          strResult = strResult + strLine.substring(i * afterChars, (i + 1) * afterChars) + HTMLLineBreak;
          i++;
        }

        // Insert HTML Line Break if any characters left in String token
        if ((i * afterChars) != strLine.length())
          strResult = strResult + strLine.substring(i * afterChars, strLine.length()) + HTMLLineBreak;
      }
    }

    // Removing last HTML Line Break fron Result String
    strResult = strResult.substring(0, (strResult.length() - HTMLLineBreak.length()));

    return strResult;
  }

  // Methods for reading the File
  public static Vector readFileInVector(String fileNameWithPath)
  {
    Log.debugLog(className, "readFileInVector", "", "", "Method called. File Name = " + fileNameWithPath);

    try
    {
      Vector vecFileLines = new Vector();
      String strLine;

      File fileName = new File(fileNameWithPath);

      if (!fileName.exists())
      {
        Log.debugLog(className, "readFileInVector", "", "", "File " + fileNameWithPath + " does not exist.");
        String path = fileNameWithPath;
        int i = path.lastIndexOf("/");
        String chekDir = path.substring(0, i);

        // this is to create directory if it is not present by sangeeta sahu.
        File file = new File(chekDir);
        if (!file.exists())
        {
          boolean success = new File(chekDir).mkdir();
          if (success)
            Log.debugLog(className, "readFileInVector", "", "", "dir " + chekDir + " created.");
        }
        return null;
      }

      FileInputStream fis = new FileInputStream(fileNameWithPath);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));

      while ((strLine = br.readLine()) != null)
      {
        strLine = strLine.trim();
        if (strLine.length() == 0)
          continue;
        if (strLine.startsWith("#"))
          continue;

        Log.debugLog(className, "readFileInVector", "", "", "Adding line in vector. Line = " + strLine);
        vecFileLines.add(strLine);
      }

      br.close();
      fis.close();

      return vecFileLines;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "readFileInVector", "", "", "Exception - ", e);
      return null;
    }
  }

  // this function is to get the pipe seperated file data in 2D array
  public static String[][] getFileDataIn2D(String fileWithPath)
  {
    Log.debugLog(className, "getFileDataIn2D", "", "", "Method called, file name with path = " + fileWithPath);

    try
    {
      // Reading file
      Vector vecFileData = readFileInVector(fileWithPath);

      // Initializing array
      String[][] arrFileData = new String[vecFileData.size()][];

      // If file not found it will return null.
      if (vecFileData == null)
      {
        Log.debugLog(className, "getFileDataIn2D", "", "", "File not found, It may be corrupted. File name = " + fileWithPath);
        return null;
      }

      for (int i = 0; i < vecFileData.size(); i++)
        arrFileData[i] = rptUtilsBean.strToArrayData(vecFileData.elementAt(i).toString(), "|");

      return arrFileData;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getFileDataIn2D", "", "", "Exception - ", e);
      return null;
    }
  }

  // This function return the 2D array of date format and current date example
  /**
   * Moved from CorrelationServices.java Example(s) - [0, 1] [%Y-%m-%d, 2010-04-28] [%m-%d-%Y, 04-28-2010] [%d-%m-%Y, 28-04-2010] [%m-%d-%y, 04-28-10] [%d-%m-%y, 28-04-10] [%y-%d-%m, 10-28-04]
   * [%Y/%m/%d, 2010/04/28] [%m/%d/%Y, 04/28/2010] [%d/%m/%Y, 28/04/2010] [%m/%d/%y, 04/28/10] [%d/%m/%y, 28/04/10] [%y/%d/%m, 10/28/04] [%m-%d-%y/%H:%M, 04-28-10/17:45] [%H:%M/%m-%d-%y,
   * 17:45/04-28-10] [%r/%m-%d-%y, 05:45:49 PM/04-28-10] [%R/%m-%d-%y, 17:45/04-28-10]
   */
  public static String[][] getDateFormatArray()
  {
    Log.debugLog(className, "getDateFormatArray", "", "", "Method Called ");
    String[][] arr2DDateFormat = new String[arrDateFormatC.length][2];

    try
    {
      for (int i = 0; i < arrDateFormatJava.length; i++)
      {
        SimpleDateFormat simpleDateFormatObj = new SimpleDateFormat(arrDateFormatJava[i]);
        arr2DDateFormat[i][0] = arrDateFormatC[i];
        arr2DDateFormat[i][1] = simpleDateFormatObj.format(new Date());

        Log.debugLog(className, "getDateFormatArray", "", "", "Format = [" + arr2DDateFormat[i][0] + ", " + arr2DDateFormat[i][1] + "]");
      }
      return arr2DDateFormat;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getDateFormatArray", "", "", "Exception ", e);
      return null;
    }
  }

  /**
   * This method is called only by jsp files, as jsp not able to open when such char(s) came in any input fields.
   *
   * @param s
   * @return
   */
  public static String replaceSpecialCharacter(String s)
  {
    Log.debugLog(className, "replaceSpecialCharacter", "", "", "Method Called. String = " + s);
    if ((s == null) || (s.length() == 0))
      return s;
    StringBuffer sb = new StringBuffer();
    int n = s.length();
    for (int i = 0; i < n; i++)
    {
      char c = s.charAt(i);
      switch (c)
      {
        case '\\':
          sb.append("\\\\");
          break;
        case '\'':
          sb.append("\\\'");
          break;
        case '"':
          sb.append("\\\"");
          break;
        case '\n':
          sb.append("\\n");
          break;
        case '\r':
          sb.append("\\\\r");
          break;
        case '\t':
          sb.append("\\\\t");
          break;
        case '\b':
          sb.append("\\\\b");
          break;
        case '\f':
          sb.append("\\\\f");
          break;
        case '&':
          sb.append("\\&");
          break;

        default:
          sb.append(c);
          break;
      }
    }
    Log.debugLog(className, "replaceSpecialCharacter", "", "", "returning String = " + sb.toString());
    return sb.toString();
  }

  // This method return the current Version of the Specific file or directory
  public static String getCurrentVersion(String filePath)
  {
    Log.debugLog(className, "getCurrentVersion", "", "", "Method started");
    String currentVersion = "";
    Vector vecCmdOutPut = new Vector();
    try
    {
      String cmdName = "nsi_cvs";
      String cmdArgs = "-o currentversion -s " + filePath;
      Log.debugLog(className, "getCurrentVersion", "", "", "cmdArgs = " + cmdArgs);
      CmdExec objCmdExec = new CmdExec();

      boolean result = true;
      vecCmdOutPut = objCmdExec.getResultByCommand(cmdName, cmdArgs, CmdExec.SYSTEM_CMD, null, "root");

      if ((vecCmdOutPut.size() > 0) && ((String) vecCmdOutPut.lastElement()).startsWith("ERROR"))
      {
        currentVersion = "NA";
      }
      else if (vecCmdOutPut.size() > 0)
      {
        vecCmdOutPut.removeElementAt(0);
        if (vecCmdOutPut.size() > 0)
        {
          String[] tempOutput = rptUtilsBean.split(vecCmdOutPut.get(0).toString(), "|");
          ;
          currentVersion = tempOutput[0];

        }
      }
      else
        currentVersion = "NA";
      return currentVersion;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getCurrentVersion", "", "", "Exception - ", e);
      currentVersion = "Error";
      vecCmdOutPut = new Vector();
      vecCmdOutPut.add("Exception: " + e);
      currentVersion = vecCmdOutPut.toString();
      return currentVersion;
    }
  }

  // Method to commit any file or directory like CVS.
  // It will create a version for each file or directory.
  // there will be a .verion or .version_fileNmae directory and version.dat in each service
  // .version or .version_fileName dir will have the folders like 1.1, 1.2 etc. for versions
  // commit will create a new dir for next version for example in above case it will create new dir as 1.3
  // and copy all files from folder except .version.dat file in to 1.3 or in case of
  public static ArrayList cvsCommit(String filePath, String author, String comments)
  {
    Log.debugLog(className, "cvsCommit", "", "", "Method started");
    ArrayList outPut = new ArrayList();
    Vector vecCmdOutPut = new Vector();
    try
    {
      String cmdName = "nsi_cvs";
      String cmdArgs = "-o commit -s " + filePath + " -u " + author + " -m \"" + comments + "\"";

      Log.debugLog(className, "cvsCommit", "", "", "cmdArgs = " + cmdArgs);

      CmdExec objCmdExec = new CmdExec();

      boolean result = true;

      vecCmdOutPut = objCmdExec.getResultByCommand(cmdName, cmdArgs, CmdExec.SYSTEM_CMD, null, "root");

      if ((vecCmdOutPut.size() > 0) && ((String) vecCmdOutPut.lastElement()).startsWith("ERROR"))
      {
        result = false;
      }

      if (!result)
      {
        Log.debugLog(className, "cvsCommit", "", "", "nsi_cvs commit failed");
        outPut.add("Error");
        outPut.add(vecCmdOutPut);
        return outPut;
      }

      outPut.add("Success");
      outPut.add(vecCmdOutPut);
      return outPut;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "cvsCommit", "", "", "Exception - ", e);
      outPut.add("Error");
      vecCmdOutPut = new Vector();
      vecCmdOutPut.add("Exception: " + e);
      outPut.add(vecCmdOutPut);
      return outPut;
    }
  }

  public static ArrayList cvsDelete(String filePath, String version)
  {
    Log.debugLog(className, "cvsDelete", "", "", "Method started");
    ArrayList outPut = new ArrayList();
    Vector vecCmdOutPut = new Vector();
    try
    {
      String cmdName = "nsi_cvs";
      String cmdArgs = "-o delete -s " + filePath + " -r " + version;

      Log.debugLog(className, "cvsDelete", "", "", "cmdArgs = " + cmdArgs);

      CmdExec objCmdExec = new CmdExec();

      boolean result = true;

      vecCmdOutPut = objCmdExec.getResultByCommand(cmdName, cmdArgs, CmdExec.SYSTEM_CMD, null, "root");

      if ((vecCmdOutPut.size() > 0) && ((String) vecCmdOutPut.lastElement()).startsWith("ERROR"))
      {
        result = false;
      }

      if (!result)
      {
        Log.debugLog(className, "cvsDelete", "", "", "nsi_cvs delete failed");
        outPut.add("Error");
        outPut.add(vecCmdOutPut);
        return outPut;
      }

      outPut.add("Success");
      outPut.add(vecCmdOutPut);
      return outPut;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "cvsDelete", "", "", "Exception - ", e);
      outPut.add("Error");
      vecCmdOutPut = new Vector();
      vecCmdOutPut.add("Exception: " + e);
      outPut.add(vecCmdOutPut);
      return outPut;
    }
  }

  // Method to update any File or directory from specified varsion.
  // It will get the files from specified version dir to the specified file or directory.
  public static ArrayList cvsUpdate(String filePath, String version)
  {
    Log.debugLog(className, "cvsUpdate", "", "", "Method started");
    ArrayList outPut = new ArrayList();
    Vector vecCmdOutPut = new Vector();

    try
    {
      String cmdName = "nsi_cvs";
      String cmdArgs = "-o update -s " + filePath;

      if (!version.equals(""))
        cmdArgs = cmdArgs + " -r " + version;

      Log.debugLog(className, "cvsUpdate", "", "", "cmdArgs = " + cmdArgs);

      CmdExec objCmdExec = new CmdExec();

      boolean result = true;

      vecCmdOutPut = objCmdExec.getResultByCommand(cmdName, cmdArgs, CmdExec.SYSTEM_CMD, null, "root");

      if ((vecCmdOutPut.size() > 0) && ((String) vecCmdOutPut.lastElement()).startsWith("ERROR"))
      {
        result = false;
      }

      if (!result)
      {
        Log.debugLog(className, "cvsUpdate", "", "", "nsi_cvs update failed");
        outPut.add("Error");
        outPut.add(vecCmdOutPut);
        return outPut;
      }

      outPut.add("Success");
      outPut.add(vecCmdOutPut);
      return outPut;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "cvsUpdate", "", "", "Exception - ", e);
      outPut.add("Error");
      vecCmdOutPut = new Vector();
      vecCmdOutPut.add("Exception: " + e);
      outPut.add(vecCmdOutPut);
      return outPut;
    }
  }

  // Method to show the difference between two version in between files and directory .
  public static ArrayList cvsDiff(String filePath, String version, String compareVersion)
  {
    Log.debugLog(className, "cvsDiff", "", "", "Method started");
    ArrayList outPut = new ArrayList();
    Vector vecCmdOutPut = new Vector();
    try
    {
      String cmdName = "nsi_cvs";
      String cmdArgs = "-o diff -s " + filePath + " -r " + version + " -r " + compareVersion;

      Log.debugLog(className, "cvsDiff", "", "", "cmdArgs = " + cmdArgs);

      CmdExec objCmdExec = new CmdExec();

      boolean result = true;

      vecCmdOutPut = objCmdExec.getResultByCommand(cmdName, cmdArgs, CmdExec.SYSTEM_CMD, null, "root");

      if ((vecCmdOutPut.size() > 0) && ((String) vecCmdOutPut.lastElement()).startsWith("ERROR"))
      {
        result = false;
      }

      if (!result)
      {
        Log.debugLog(className, "cvsDiff", "", "", "nsi_cvs diff failed");
        outPut.add("Error");
        outPut.add(vecCmdOutPut);
        return outPut;
      }

      outPut.add("Success");
      outPut.add(vecCmdOutPut);
      return outPut;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "cvsDiff", "", "", "Exception - ", e);
      outPut.add("Error");
      vecCmdOutPut = new Vector();
      vecCmdOutPut.add("Exception: " + e);
      outPut.add(vecCmdOutPut);
      return outPut;
    }
  }

  // Method to get the version history of any file or directory
  public static ArrayList cvsLog(String filePath)
  {
    Log.debugLog(className, "cvsLog", "", "", "Method started");
    ArrayList outPut = new ArrayList();
    Vector vecCmdOutPut = new Vector();

    try
    {
      String cmdName = "nsi_cvs";
      String cmdArgs = "-o history -s " + filePath;

      Log.debugLog(className, "cvsLog", "", "", "cmdArgs = " + cmdArgs);

      CmdExec objCmdExec = new CmdExec();

      boolean result = true;

      vecCmdOutPut = objCmdExec.getResultByCommand(cmdName, cmdArgs, CmdExec.SYSTEM_CMD, null, "root");

      if ((vecCmdOutPut.size() > 0) && ((String) vecCmdOutPut.lastElement()).startsWith("ERROR"))
      {
        result = false;
      }

      if (!result)
      {
        Log.debugLog(className, "cvsLog", "", "", "nsi_cvs history failed");
        outPut.add("Error");
        outPut.add(vecCmdOutPut);
        return outPut;
      }

      outPut.add("Success");

      vecCmdOutPut.removeElementAt(0);

      String[][] serviceCVSHistory = new String[vecCmdOutPut.size()][4];

      for (int i = 0; i < vecCmdOutPut.size(); i++)
      {
        String[] strTemp = rptUtilsBean.strToArrayData(vecCmdOutPut.elementAt(i).toString(), "|");
        serviceCVSHistory[i] = strTemp;
      }

      outPut.add(serviceCVSHistory);
      return outPut;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "cvsLog", "", "", "Exception - ", e);
      outPut.add("Error");
      vecCmdOutPut = new Vector();
      vecCmdOutPut.add("Exception: " + e);
      outPut.add(vecCmdOutPut);
      return outPut;
    }
  }

  private static boolean createDirs(String path)
  {
    Log.debugLog(className, "createDirs", "", "", "Method called. path = " + path);
    int index = path.indexOf("/dump/");

    String strDump = path.substring(0, index + 6);
    String remain = path.substring(index + 6, path.length());

    try
    {
      /*
       * It will create the dirs till dump because till dump no need to check exiting dir OR file, so create all dirs in one shot
       */
      File fileTillDump = new File(strDump);
      fileTillDump.mkdirs();

      String strDir[] = remain.split("/");

      for (int i = 0; i < strDir.length; i++)
      {
        if (i != 0)
          strDump = strDump + "/" + strDir[i];
        else
          strDump = strDump + strDir[i];

        File temp = new File(strDump);

        if (temp.exists())
        {
          if (!temp.isDirectory())
          {
            Log.debugLog(className, "createDirs", "", "", "File already exist = " + strDir[i]);
            if (!temp.delete())
              return false;
            if (!temp.mkdir())
              return false;
          }
          else
            Log.debugLog(className, "createDirs", "", "", "Directory already exist = " + strDir[i]);
        }
        else
        {
          if (!temp.mkdir())
            return false;
        }
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "createDirs", "", "", "Exception in creating the directory. = " + path, e);
      return false;
    }
    return true;
  }

  private static boolean createDirForExistedName(String path, Boolean[] statusAndModifiedPath)
  {
    Log.debugLog(className, "createDirForExistedName", "", "", "Method called. path = " + path);
    int index1 = path.indexOf("/dump/");

    String strDump = path.substring(0, index1 + 6);
    String remain = path.substring(index1 + 6, path.length());
    String dumpRename = path.substring(0, index1 + 5);
    try
    {
      /*
       * It will create the dirs till dump because till dump no need to check exiting dir OR file, so create all dirs in one shot
       */
      File fileTillDump = new File(strDump);
      fileTillDump.mkdirs();
      String strDir[] = remain.split("/");
      for (int i = 0; i < strDir.length; i++)
      {
        if (i != 0)
        {
          dumpRename = dumpRename + "/" + strDir[i - 1];
          strDump = strDump + "/" + strDir[i];
        }
        else
          strDump = strDump + strDir[i];

        File temp = new File(strDump);
        File f1 = new File(dumpRename + File.separator + (strDir[i] + "index"));

        if (temp.exists())
        {
          if (!temp.isDirectory())
          {
            Log.debugLog(className, "createDirForExistedName", "", "", "File already exist = " + strDir[i]);
            // instade of deleting the file we rename it
            temp.renameTo(f1);
            statusAndModifiedPath[1] = new Boolean(true);

            /*
             * if(!temp.delete()) return false;
             */

            if (!temp.mkdir())
              return false;
          }
          else
            Log.debugLog(className, "createDirForExistedName", "", "", "Directory already exist = " + strDir[i]);
        }
        else
        {
          if (!temp.mkdir())
            return false;
        }
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "createDirForExistedName", "", "", "Exception in creating the directory. = " + path, e);
      return false;
    }
    statusAndModifiedPath[0] = new Boolean(true);
    return true;
  }

  public static boolean addDumpDirs(String path, String fileName, byte[] data, String pageName)
  {
    Log.debugLog(className, "addDumpDirs", "", "", "Method called. path = " + path + ", FileName = " + fileName);

    String fileNameWithPath = path + "/" + fileName;
    if (!createDirs(path))
    {
      Log.errorLog(className, "addDumpDirs", "", "", "Directory can not be created. for path = " + path);
      return false;
    }

    Log.debugLog(className, "addDumpDirs", "", "", "Creating the File for the FileName = " + path + fileName);
    File fileObj = new File(path, fileName);
    try
    {
      if (!fileObj.exists())
      {
        Log.debugLog(className, "addDumpDirs", "", "", "creating the file. file name = " + fileObj.getName());
        if (!fileObj.createNewFile())
        {
          Log.errorLog(className, "addDumpDirs", "", "", "File for dump can not be created. file name = " + fileObj.getName());
          return false;
        }
      }
      else
      // Skip if file already exist
      {
        Log.debugLog(className, "addDumpDirs", "", "", "File already exist. fileName = " + fileObj.getName());
        // to append the data into existing file for NSBrowser recording
        // return true;
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "addDumpDirs", "", "", "Exception in creating the file. file = " + fileObj.getName(), e);
      return false;
    }

    FileOutputStream fileFos = null;
    try
    {
      // opening file in append mode
      fileFos = new FileOutputStream(fileObj, true);
    }
    catch (FileNotFoundException e)
    {
      Log.stackTraceLog(className, "addDumpDirs", "", "", "FileNotFoundException in creating the FileOutputStream for the " + fileObj.getName(), e);
      return false;
    }

    try
    {
      fileFos.write(data);
      fileFos.flush();
      fileFos.close();
    }
    catch (IOException e)
    {
      Log.stackTraceLog(className, "addDumpDirs", "", "", "IOException in writing the data to the " + fileObj.getName(), e);
    }
    return true;
  }

  public static Boolean[] addDumpDirForRename(String path, String fileName, byte[] data, String pageName)
  {
    Log.debugLog(className, "addDumpDirForRename", "", "", "Method called. path = " + path + ", FileName = " + fileName);

    // Boolean array contains the status of dump creation and dump path modifiaction in case of directory conficts
    // at 0 index - status
    // at 1 index - modification
    Boolean[] statusAndModifiedPath = new Boolean[] { new Boolean(false), new Boolean(false) };

    String fileNameWithPath = path + "/" + fileName;
    if (!createDirForExistedName(path, statusAndModifiedPath))
    {
      Log.errorLog(className, "addDumpDirForRename", "", "", "Directory can not be created. for path = " + path);
      return statusAndModifiedPath;
    }

    Log.debugLog(className, "addDumpDirForRename", "", "", "Creating the File for the FileName = " + path + fileName);
    File fileObj = new File(path, fileName);
    try
    {
      if (!fileObj.exists())
      {
        Log.debugLog(className, "addDumpDirForRename", "", "", "creating the file. file name = " + fileObj.getName());
        if (!fileObj.createNewFile())
        {
          Log.errorLog(className, "addDumpDirForRename", "", "", "File for dump can not be created. file name = " + fileObj.getName());
          return statusAndModifiedPath;
        }
      }
      else
      // Skip if file already exist
      {
        Log.debugLog(className, "addDumpDirForRename", "", "", "File already exist. fileName = " + fileObj.getName());
        // to append the data into existing file for NSBrowser recording
        // return true;
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "addDumpDirForRename", "", "", "Exception in creating the file. file = " + fileObj.getName(), e);
      return statusAndModifiedPath;
    }

    FileOutputStream fileFos = null;
    try
    {
      // opening file in append mode
      fileFos = new FileOutputStream(fileObj, true);
    }
    catch (FileNotFoundException e)
    {
      Log.stackTraceLog(className, "addDumpDirForRename", "", "", "FileNotFoundException in creating the FileOutputStream for the " + fileObj.getName(), e);
      return statusAndModifiedPath;
    }

    try
    {
      fileFos.write(data);
      fileFos.flush();
      fileFos.close();
    }
    catch (IOException e)
    {
      Log.stackTraceLog(className, "addDumpDirForRename", "", "", "IOException in writing the data to the " + fileObj.getName(), e);
    }
    return statusAndModifiedPath;
  }

  public static Vector getDataInVector(String fileWithPath)
  {
    Vector vecFileData = readFileInVector(fileWithPath);

    return vecFileData;
  }

  /*
  *//**
   * This method is copied from ScriptStop.java file
   *
   * @param strUrl
   *          - For which it have to create all fields
   * @param wdir
   *          - This will be till <NS_WDIR>/scripts/<Project>/<SubProject>/<Script Name>/dump/
   * @param curPageName
   * @param mapForDump
   * @return
   */
  /*
   * public static ScriptParsingData parseFileNameAndPath(String strUrl, String wdir, String curPageName, HashMap<String, String> mapForDump) { return parseFileNameAndPath(strUrl, wdir, curPageName,
   * mapForDump, false); }
   *//**
   * This method is used in GenerateScript.java for nsu_gen_script
   *
   * @param strUrl
   *          - For which it have to create all fields
   * @param wdir
   *          - This will be till <NS_WDIR>/scripts/<Project>/<SubProject>/<Script Name>/dump/
   * @param curPageName
   * @param mapForDump
   * @param isForGenScript
   *          - whether called for nsu_gen_script
   * @return
   */
  /*
   * public static ScriptParsingData parseFileNameAndPath(String strUrl, String wdir, String curPageName, HashMap<String, String> mapForDump, boolean isForGenScript) { Log.debugLog(className,
   * "parseFileNameAndPath", "", "", "Method called. URL = " + strUrl); String fileName = ""; String unParsedPath = ""; String parsedPath = ""; String urlForFile = ""; String unParsedFileName = "";
   * String parsedFileName = ""; boolean isParsed = false;
   *
   * ScriptParsingData scriptParsingData = new ScriptParsingData(); scriptParsingData.setParsed(false); scriptParsingData.setCheckParsed(false);
   *
   * URL urlObj = null; try { Log.debugLog(className, "parseFileNameAndPath", "", "", "URL before Decoding. URL = " + strUrl); strUrl = rptUtilsBean.decode(strUrl); Log.debugLog(className,
   * "parseFileNameAndPath", "", "", "URL after Decoding. URL = " + strUrl); urlObj = new URL(strUrl); } catch (MalformedURLException e) { Log.stackTraceLog(className, "parseFileNameAndPath", "", "",
   * "Exception in creating the url for the strUrl = " + strUrl, e); return scriptParsingData; } String hostName = urlObj.getHost(); int port = urlObj.getPort(); if(port != -1) hostName = hostName +
   * ":" + port; //This will not contain the hostName String fileNameWithPath = urlObj.getFile(); Log.debugLog(className, "parseFileNameAndPath", "", "", "fileNameWithPath = " + fileNameWithPath); int
   * fromIndex = fileNameWithPath.length();
   *
   * Url = http://urs.microsoft.com/mail/urs.asmx?mspu-lock/file.html
   *
   * then file name will be "urs.asmx?mspu-lock/file.html" not only file.html then from this file name invalid character will be replaced.
   *
   * int indexForQuesMark = fileNameWithPath.indexOf("?");
   *
   * if(indexForQuesMark != -1) fromIndex = indexForQuesMark;
   *
   * int index = fileNameWithPath.lastIndexOf("/", fromIndex); fileName = fileNameWithPath.substring((index + 1), fileNameWithPath.length()); Log.debugLog(className, "parseFileNameAndPath", "", "",
   * "getting the fileName = " + fileName ); String path = fileNameWithPath.substring(0, index + 1); Log.debugLog(className, "parseFileNameAndPath", "", "", "getting the path to the file = " + path);
   * //"," is not allowed in gperf and ';' is said by the vaibhav, '%' character is added for the url which contain %20 for space //"'" is added to resolve the bug(239) as in macys url name contain
   * "'" like char[] inValidCharForFile = {'/', '\\', ':', '*', '?', '"', '<', '>', '|', '-', ',', '^', '#', ';', '$','%', '&', ',', '\''}; char[] inValidCharForDir = {'\\', ':', '*', '"', '<', '>',
   * '|', '?', ',', '#', ';', '%'};
   *
   * //path = path.substring(0, path.length()); unParsedPath = path; path = hostName + path; Log.debugLog(className, "parseFileNameAndPath", "", "", "The path name before the parsing = " +
   * unParsedPath); for(int i = 0 ; i < inValidCharForDir.length ; i++) { path = path.replace(inValidCharForDir[i], '_'); }
   *
   * //To check any specific path have more than 253 character, It MUST NOT cross that limit. if so then this is erroneous case. String seperatedPaths[] = strToArrayData(path, "/"); for (String
   * specificPath : seperatedPaths) { if(specificPath.length() > 253) { Log.errorLog(className, "parseFileNameAndPath", "", "",
   * "The path length is exceding the limit of 253 character, for directory = " + specificPath); return scriptParsingData; } }
   *
   * parsedPath = path; urlForFile = path; path = wdir + "/" + path;//Just to avoid the ":", being replace with "_" as in the above loop
   *
   * int pathLength = path.length();
   *
   * //Check for the path length, In window it must be in the 255 character if((pathLength > 253) && !(isForGenScript)) { Log.errorLog(className, "parseFileNameAndPath", "", "",
   * "The path length is exceding the limit of 253 character, This url is ignored. path = " + path); return scriptParsingData; }
   *
   * Log.debugLog(className, "parseFileNameAndPath", "", "", "The path name after the parsing = " + parsedPath); unParsedFileName = fileName; Log.debugLog(className, "parseFileNameAndPath", "", "",
   * "The file name before the parsing = " + unParsedFileName); for(int i = 0 ; i < inValidCharForFile.length ; i++) { fileName = fileName.replace(inValidCharForFile[i], '_'); }
   *
   * if(fileName.length() > 253) { Log.debugLog(className, "parseFileNameAndPath", "", "", "The file name length is exceding the limit of 253 character, fileName = " + fileName); int indexFrmDeduce =
   * 253 - pathLength; fileName = fileName.substring(0, indexFrmDeduce); }
   *
   * //This is to temporary test for the file name if(fileName.equals("")) { Log.debugLog(className, "parseFileNameAndPath", "", "",
   * "The file name is empty, setting file name as current page name. name = " + curPageName); fileName = curPageName; }
   *
   * int fileNameLength = fileName.length(); int fullPathWithFileNameLength = pathLength + fileNameLength; Log.debugLog(className, "parseFileNameAndPath", "", "", "The path length = " + pathLength +
   * ", and file Name length = " + fileNameLength);
   *
   * if((fullPathWithFileNameLength > 253) && !(isForGenScript)) { Log.debugLog(className, "parseFileNameAndPath", "", "",
   * "The file Name with full path is exceding the limit of length, so truncating the file name. FullPath = " + fullPathWithFileNameLength); int indexFrmDeduce = 253 - pathLength; fileName =
   * fileName.substring(0, indexFrmDeduce); } Log.debugLog(className, "parseFileNameAndPath", "", "", "The length of the file name after truncating = " + fileName.length());
   *
   * if(!fileName.equals("")) {
   *
   * when filename came with last (.) OR (" ") then window create the file without dot then we have to handle for that character in the url here the space may came after removal of (.) and (.) may
   * came after removal of removal of space, handled both cases.
   *
   * fileName = fileName.trim(); while((fileName.length() > 0) && (fileName.charAt(fileName.length() - 1) == '.')) { fileName = fileName.substring(0, (fileName.length() - 1)); fileName =
   * fileName.trim(); }
   *
   * if(fileName.equals("")) fileName = curPageName; }
   *
   *
   * //Check for the extension length, this is not used presently //fileName = checkForExtension(fileName); parsedFileName = fileName; Log.debugLog(className, "parseFileNameAndPath", "", "",
   * "The file name after the parsing = " + parsedFileName + " and the length = " + parsedFileName.length());
   *
   * fileName = path + fileName;//At last the fileName will contain the full name of file with path Log.debugLog(className, "parseFileNameAndPath", "", "", "parsedPath = " + parsedPath +
   * ", unParsedPath = " + unParsedPath); Log.debugLog(className, "parseFileNameAndPath", "", "", "parsedPath = " + parsedFileName + ", unParsedFileName = " + unParsedFileName);
   *
   * String strForMap = parsedPath + "/" + parsedFileName; if(mapForDump.size() == 0) { Log.debugLog(className, "addDumpDirs", "", "", "hashMap size is zero"); mapForDump.put(strForMap, "1"); } else {
   * if(mapForDump.containsKey(strForMap)) { Log.debugLog(className, "addDumpDirs", "", "", "URL already exist in the hash mapForFileName. fileNameWithPath = " + fileNameWithPath); int num =
   * Integer.parseInt(mapForDump.get(strForMap).toString()); num++; parsedFileName = parsedFileName + "_" + num + ""; mapForDump.put(strForMap, num + ""); fileNameWithPath = fileNameWithPath + "_" +
   * num + ""; Log.debugLog(className, "addDumpDirs", "", "", "Setting the name of the fileNameWithPath = " + fileNameWithPath + " and freq of the fileNameWithPath = " + num); } else {
   * Log.debugLog(className, "addDumpDirs", "", "", "the is get first time. fileNameWithPath = " + strForMap); mapForDump.put(strForMap, "1"); } }
   *
   * urlForFile = urlForFile + parsedFileName; urlForFile = rptUtilsBean.encode(urlForFile); Log.debugLog(className, "parseFileNameAndPath", "", "", "Setting the urlForFile = " + urlForFile);
   *
   * if((!unParsedFileName.equals(parsedFileName)) || (!(hostName + unParsedPath).equals(parsedPath))) isParsed = true;//isParsed is class variable, which has to accessed before to write the line in
   * changed_url.txt file Log.debugLog(className, "parseFileNameAndPath", "", "", "returning the fileName with full Path = " + fileName);
   *
   *
   * scriptParsingData.setUnParsedPath(unParsedPath); scriptParsingData.setUnParsedFileName(unParsedFileName); scriptParsingData.setParsedPath(parsedPath);
   * scriptParsingData.setParsedFileName(parsedFileName); scriptParsingData.setParsed(isParsed); scriptParsingData.setCheckParsed(true); scriptParsingData.setUrlForFile(urlForFile);
   *
   * return scriptParsingData; }
   */

  public static File[] getListOfFilesWithMatchingPatteren(String filePath, String patternForFileName)
  {
    // This is needed as per for file name filteration
    class CustomFileFilter implements FilenameFilter
    {
      String fileNamePattern = "";

      public CustomFileFilter(String fileNamePattern)
      {
        this.fileNamePattern = fileNamePattern.replace("*", "[a-zA-z0-9\\s\\W.]*");
      }

      // @Override
      public boolean accept(File dir, String name)
      {
        // Presently it is case insensitive
        Pattern tempPattern = Pattern.compile(fileNamePattern);
        Matcher m = tempPattern.matcher(name);
        return m.matches();
      }
    }

    File pathObj = new File(filePath);

    if (!pathObj.exists())
    {
      Log.errorLog(className, "getListOfFilesWithMatchingPatteren", "", "", "Path Not exist = " + filePath);
      return null;
    }

    File[] fileArr = pathObj.listFiles(new CustomFileFilter(patternForFileName));
    if ((fileArr == null) || (fileArr.length <= 0))
    {
      Log.errorLog(className, "getListOfFilesWithMatchingPatteren", "", "", "Not found any matching file with expression = " + patternForFileName + ", in path = " + filePath);
      return null;
    }
    else
      return fileArr;
  }

  public static ArrayList getListOfDirs(String folderPath)
  {
    File pathObj = new File(folderPath);
    ArrayList listFolders = new ArrayList();

    if (!pathObj.exists())
    {
      Log.errorLog(className, "getListOfDirs", "", "", "Path Not exist = " + folderPath);
      return listFolders;
    }

    File[] fileArr = pathObj.listFiles();
    if ((fileArr == null) || (fileArr.length <= 0))
    {
      Log.errorLog(className, "getListOfDirs", "", "", "Not found any folder in path = " + folderPath);
      return listFolders;
    }
    for (int i = 0; i < fileArr.length; i++)
    {
      if (fileArr[i].isDirectory())
        listFolders.add(fileArr[i].getName());
    }
    return listFolders;
  }

  public static String convTo3DigitDecimal(double digit)
  {
    String num = "";
    DecimalFormat df = new DecimalFormat("#.###");
    num = df.format(digit);
    return num;
  }

  // convert millisecs to seconds.
  public static String convertMilliSecToSecs(long timeInMilliSecs)
  {
    String timeTaken = convTo3DigitDecimal(((double) (timeInMilliSecs)) / 1000);
    return timeTaken;
  }

  public static boolean getResponse(String url, String currUrl)
  {
    Log.debugLog(className, "getResponse", "", "", "Method Starts url=" + url + ", currUrl = " + currUrl);
    StringBuffer strBuf = new StringBuffer();
    OutputStream outStream = null;
    URLConnection uCon = null;

    InputStream is = null;
    try
    {
      URL Url;
      byte[] buf;
      int ByteRead, ByteWritten = 0;

      Url = new URL(url);
      outStream = new BufferedOutputStream(new FileOutputStream(currUrl));

      uCon = Url.openConnection();
      System.out.println(url);
      is = uCon.getInputStream();
      buf = new byte[size];
      while ((ByteRead = is.read(buf)) != -1)
      {
        outStream.write(buf, 0, ByteRead);
        ByteWritten += ByteRead;
      }
      return true;
    }
    catch (Exception e)
    {
      // e.printStackTrace();
      Log.stackTraceLog(className, "fileUrl", "", "", "Exception ", e);
      return false;
    }
    finally
    {
      try
      {
        if (is != null)
          is.close();
        if (outStream != null)
          outStream.close();
      }
      catch (IOException e)
      {
        // e.printStackTrace();
        Log.stackTraceLog(className, "fileUrl", "", "", "Exception ", e);
      }
    }
  }

  // this is get list of file and directories and their properties like file type , size and last modified in the given path.
  public static Object[][] getListOfFilesAndDirOfSelectedDir(String Path)
  {
    Log.debugLog(className, "getListOfFilesAndDirOfSelectedDir", "", "", "Method Starts for Path=" + Path);

    Object[][] list = null;
    File file = null;
    JFileChooser chooser = new JFileChooser();
    File dir = new File(Path);
    if (!dir.isDirectory())
      return list;

    // Get the (filtered) directory entries
    String[] files = dir.list();
    if (files == null)
    {
      list = new String[1][6];
      list[0][0] = "D";
      list[0][1] = "...";
      list[0][2] = "folder-closed.gif";
      list[0][3] = "";
      list[0][4] = "";
      list[0][5] = "";
      return list;
    }

    // for(int j =0; j<files.length; j++)
    // System.out.println(""+j+" = "+files[j]);

    // Sort the list of filenames.
    java.util.Arrays.sort(files, new Comparator()
    {
      public int compare(Object o1, Object o2)
      {
        String elt1 = (String) o1;
        String elt2 = (String) o2;
        return elt1.compareToIgnoreCase(elt2);
      }
    });

    list = new String[files.length + 1][6];
    list[0][0] = "D";
    list[0][1] = "...";
    list[0][2] = "folder-closed.gif";
    list[0][3] = "";
    list[0][4] = "";
    list[0][5] = "";

    for (int i = 0; i < files.length; i++)
    {
      file = new File(Path, files[i]); // Convert to a File
      if (file.isDirectory())
      {
        list[i + 1][0] = "D"; // List dir
      }
      else
        list[i + 1][0] = "F"; // list File

      list[i + 1][1] = files[i]; // Name of file or dir

      String fileImage = "txtFile.png";
      if (file.isDirectory())
        fileImage = "folder-closed.gif";
      else if (files[i].endsWith(".pdf"))
        fileImage = "iconPDF.gif";
      else if (files[i].endsWith(".xls") || files[i].endsWith(".xlsx"))
        fileImage = "iconExcel.gif";
      else if (files[i].endsWith(".doc"))
        fileImage = "iconWord.gif";
      else if (files[i].endsWith(".exe"))
        fileImage = "EXE File.png";
      else if (files[i].endsWith(".ini"))
        fileImage = "INF File.png";
      else if (files[i].endsWith(".bat"))
        fileImage = "BAT File.png";

      list[i + 1][2] = fileImage; // Image related to file.

      if (file.isDirectory())
        list[i + 1][3] = "";
      else
        list[i + 1][3] = file.length() + ""; // length of file.

      if (file.isDirectory())
      {
        list[i + 1][4] = "File Folder"; // Type of file.
      }
      else
      {
        list[i + 1][4] = chooser.getTypeDescription(file);
      }
      Date lastModified = new Date(file.lastModified());
      SimpleDateFormat ft = new SimpleDateFormat("yyyy/MM/dd  hh:mm:ss a");
      // new SimpleDateFormat ("E yyyy.MM.dd 'at' hh:mm:ss a zzz");

      list[i + 1][5] = ft.format(lastModified) + "";
    }

    Arrays.sort(list, new Comparator()
    {
      public int compare(Object o1, Object o2)
      {
        String[] elt1 = (String[]) o1;
        String[] elt2 = (String[]) o2;
        return elt1[0].compareToIgnoreCase(elt2[0]);
      }
    });
    return list;
  }

  public static Object[][] getListOfFilesFromServer(String serverName, String path, StringBuffer errMsg)
  {
    
    return null;
  }

  // This function will check the data files for generating Misc reports
  public static boolean checkDataFilesToGenerateMisc(int rptId, int testRunNo)
  {
    Log.debugLog(className, "checkDataFilesToGenerateMisc", "", "", "Method called. test Run = " + testRunNo);
    ReportData rptData = new ReportData(testRunNo);
    //ReportSetControl reportSetControl = new ReportSetControl();
    try
    {
      boolean isGlobalFileAval = rptData.isGraphRawDataFileAvailable("global.dat", testRunNo + "");

      if (rptId == 1 || rptId == 2)
      {
        boolean isRTGMsgFileAval = rptData.isGraphRawDataFileAvailable("rtgMessage.dat", testRunNo + "");

        if (isRTGMsgFileAval == false)
        {
          return false;
        }
      }
      if (rptId == 1 || rptId == 2 || rptId == 3 || rptId == 4 || rptId == 5)
      {
        if (isGlobalFileAval == false)
        {
          return false;
        }
      }
      if (rptId == 3)
      {
        // This will check the internet simulation for used scenario
        boolean islocWithAcsRespFileAval = rptData.isGraphRawDataFileAvailable("locWithAccessAndRespTime.dat", testRunNo + "");
        if (islocWithAcsRespFileAval == false)
        {
          return false;
        }

      }
      if (rptId == 4)
      {
        // This will check the internet simulation for used scenario
        boolean isPagesRespFileAval = rptData.isGraphRawDataFileAvailable("pagesRespTime.dat", testRunNo + "");

        if (isPagesRespFileAval == false)
        {
          return false;
        }
      }

      return true;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "miscDataFiles", "", "", "Exception - " + e);

      return false;
    }
  }

  public static String getUpdatedSessVariable(String strFilterLine, String strLocation, String strAccess, String strStatus)
  {
    Log.debugLog(className, "getUpdatedSessVariable", "", "", "Method called. strFilterLine = " + strFilterLine + ", strLocation = " + strLocation + ", strAccess = " + strAccess + ", strStatus = " + strStatus);
    try
    {
      String[] arrTemp = rptUtilsBean.strToArrayData(strFilterLine, " ");
      String drillLoc = "--location";
      String drillAccess = "--access";
      String drillStatus = "--status";

      boolean locFlag = false;
      boolean accessFlag = false;
      boolean statusFlag = false;

      if (strLocation == null || strLocation.trim().equals("") || strLocation.trim().equals("undefined") || strLocation.trim().toLowerCase().equals("All".toLowerCase()))
        strLocation = "All";

      if (strAccess == null || strAccess.trim().equals("") || strAccess.trim().equals("undefined") || strAccess.trim().toLowerCase().equals("All".toLowerCase()))
        strAccess = "All";

      if (strStatus == null || strStatus.trim().equals("") || strStatus.trim().equals("undefined") || strStatus.trim().toLowerCase().equals("All".toLowerCase()))
        strStatus = "-2";

      for (int i = 0; i < arrTemp.length; i++)
      {
        if (arrTemp[i].trim().equals(drillLoc))
        {
          locFlag = true;
          if (strLocation.trim().equals("NA"))
            continue;
          if (strLocation.trim().equals(""))
          {
            arrTemp[i + 1] = "";
            arrTemp[i] = "";
          }
          else
            arrTemp[i + 1] = strLocation;
        }

        if (arrTemp[i].trim().equals(drillAccess))
        {
          accessFlag = true;
          if (strAccess.trim().equals("NA"))
            continue;
          if (strAccess.trim().equals(""))
          {
            arrTemp[i + 1] = "";
            arrTemp[i] = "";
          }
          else
            arrTemp[i + 1] = strAccess;
        }

        if (arrTemp[i].trim().equals(drillStatus))
        {
          statusFlag = true;
          if (strStatus.trim().equals("NA"))
            continue;
          if (strStatus.trim().equals(""))
          {
            arrTemp[i + 1] = "";
            arrTemp[i] = "";
          }
          else
            arrTemp[i + 1] = strStatus;
        }
      }

      String strFilterLineOld = "";
      for (int ii = 0; ii < arrTemp.length; ii++)
      {
        if (!arrTemp[ii].trim().equals(""))
          strFilterLineOld = strFilterLineOld + " " + arrTemp[ii];
      }

      if (!locFlag && !strLocation.trim().equals("") && !strLocation.trim().equals("NA"))
        strFilterLineOld = strFilterLineOld + " " + drillLoc + " " + strLocation;

      if (!accessFlag && !strAccess.trim().equals("") && !strAccess.trim().equals("NA"))
        strFilterLineOld = strFilterLineOld + " " + drillAccess + " " + strAccess;

      if (!statusFlag && !strStatus.trim().equals("") && !strStatus.trim().equals("NA"))
        strFilterLineOld = strFilterLineOld + " " + drillStatus + " " + strStatus;
      strFilterLine = strFilterLineOld;

      return strFilterLine;
    }
    catch (Exception e)
    {
      Log.errorLog(className, "getUpdatedSessVariable", "", "", "Exception - " + e);
      return strFilterLine;
    }
  }

  /**
   * This method will return information about server name and controller name
   *
   * @return
   */
  public static String getControllerSpecificInfo()
  {
    Log.debugLog(className, "getControllerSpecificInfo", "", "", "Method called.");
    String controllerInfo = "";
    String displayName = "";
    String ControllerName = "";
    try
    {
      Vector vecCmdOutPut = new Vector();
      CmdExec objCmdExec = new CmdExec();

      boolean result = true;
      vecCmdOutPut = objCmdExec.getResultByCommand("nsi_show_config", "-d", CmdExec.SYSTEM_CMD, null, "root");
      if ((vecCmdOutPut.size() > 0) && ((String) vecCmdOutPut.lastElement()).startsWith("ERROR"))
        result = false;
      if (result)
      {
        displayName = vecCmdOutPut.get(0).toString().trim();
        vecCmdOutPut.removeAllElements();
        vecCmdOutPut.clear();
        vecCmdOutPut = objCmdExec.getResultByCommand("echo", "$HPD_CMD", CmdExec.SYSTEM_CMD, null, "root");
        if ((vecCmdOutPut.size() > 0) && ((String) vecCmdOutPut.lastElement()).startsWith("ERROR"))
          ControllerName = "hpd";
        else
          ControllerName = vecCmdOutPut.get(0).toString().trim();

        if (ControllerName.indexOf("hpd_") > -1)
          ControllerName = ControllerName.substring(ControllerName.indexOf("hpd_") + 4);

        if (ControllerName.equals("hpd"))
          ControllerName = "work";

        if (ControllerName.equals("hpd2"))
          ControllerName = "work2";

        if (ControllerName.equals("hpd3"))
          ControllerName = "work3";
      }
      else
        Log.debugLog(className, "getControllerSpecificInfo", "", "", "Error in executing nsi_show_config");

      controllerInfo = displayName + "-" + ControllerName;
      if (!displayName.equals("") && !ControllerName.equals("work"))
        controllerInfo = displayName + "-" + ControllerName;
      if (!displayName.equals("") && ControllerName.equals("work"))
        controllerInfo = displayName;
      if (displayName.equals("") && !ControllerName.equals("work"))
        controllerInfo = ControllerName;
      if (displayName.equals("") && ControllerName.equals("work"))
        controllerInfo = "";

      Log.debugLog(className, "getControllerSpecificInfo", "", "", "ControllerName = " + ControllerName + " , controllerInfo = " + controllerInfo + ", OS name = " + System.getProperty("os.name"));
      return controllerInfo.trim();
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getControllerSpecificInfo", "", "", "Exception caught - ", ex);
      return controllerInfo.trim();
    }
  }

  public static String escapeCharacter(String cmdDataLine)
  {
    try
    {
      ArrayList arryListCmdTokenData = new ArrayList();

      // String[] strData = rptUtilsBean.strToArrayData(cmdDataLine, " "); // split cmd line by space
      String[] strData = cmdDataLine.split(" "); // split cmd line by space

      int tokenCounter = 0;
      String strToken = "";

      String strConst = "Constant";
      int constLength = strConst.length();

      boolean startFlag = false;
      boolean endFlag = false;
      boolean spaceDouble = false;

      for (int i = 0; i < strData.length; i++)
      {
        int index = strData[i].indexOf(strConst);
        int endIndex = strData[i].lastIndexOf("\")");

        String temp = "";
        // strData[i] = strData[i].trim();
        // start and last character is ". startsWith
        if ((strData[i].startsWith(strConst + "(\"")) && ((endIndex = strData[i].lastIndexOf("\")")) > -1))
        {
          temp = strData[i].substring(strConst.length() + 2, endIndex);
          temp = rptUtilsBean.replace(temp, "\"", "\\\"");
          temp = rptUtilsBean.replace(temp, "(", "\\(");
          temp = rptUtilsBean.replace(temp, ")", "\\)");

          String endTemp = strData[i].substring(endIndex + 2);
          strToken = strConst + "(\"" + temp + "\")" + endTemp;

          System.out.println("  strToken  start and end " + strToken);
        }
        else if (index != -1) // if " sub string found
        {
          startFlag = true;
          endFlag = true;
          if (strData[i].startsWith(strConst + "(\""))
          {
            temp = strData[i].substring(strConst.length() + 2, strData[i].length());
            temp = rptUtilsBean.replace(temp, "\"", "\\\"");
            temp = rptUtilsBean.replace(temp, "(", "\\(");
            temp = rptUtilsBean.replace(temp, ")", "\\)");

            strToken = strConst + "(\"" + temp;
          }
          else if (strData[i].startsWith(strConst + "("))
          {
            temp = strData[i].substring(strConst.length() + 1, strData[i].length());
            temp = rptUtilsBean.replace(temp, "\"", "\\\"");
            temp = rptUtilsBean.replace(temp, "(", "\\(");
            temp = rptUtilsBean.replace(temp, ")", "\\)");

            spaceDouble = true;
            strToken = strConst + "(" + temp;
          }
          else if (strData[i].startsWith(strConst))
          {
            temp = strData[i].substring(strConst.length(), strData[i].length());
            temp = rptUtilsBean.replace(temp, "\"", "\\\"");
            temp = rptUtilsBean.replace(temp, "(", "\\(");
            temp = rptUtilsBean.replace(temp, ")", "\\)");

            spaceDouble = true;
            strToken = strConst + "(" + temp;
          }
          System.out.println("  strToken  start  " + strData[i] + "|" + strToken);
        }
        else if (startFlag && endIndex > -1)
        {
          System.out.println("  strToken  MeetEnd  " + strData[i] + "|" + strToken + "|" + endIndex);
          startFlag = false;
          endFlag = false;
          if (spaceDouble)
            temp = strData[i].substring(1, endIndex);
          else
            temp = strData[i].substring(0, endIndex);
          temp = rptUtilsBean.replace(temp, "\"", "\\\"");
          temp = rptUtilsBean.replace(temp, "(", "\\(");
          temp = rptUtilsBean.replace(temp, ")", "\\)");
          String endTemp = strData[i].substring(endIndex + 2);
          if (spaceDouble)
          {
            spaceDouble = false;
            strToken = "\"" + temp + "\")" + endTemp;
          }
          else
            strToken = temp + "\")" + endTemp;
        }
        else if (startFlag && endIndex < 0)// if " substring not found
        {
          System.out.println("  strToken  End norprp|" + strData[i] + "|" + strToken);
          startFlag = true;
          endFlag = true;
          if (!strData[i].equals(""))
          {
            if (spaceDouble)
              temp = strData[i].substring(1, strData[i].length());
            else
              temp = strData[i].substring(0, strData[i].length());
            temp = rptUtilsBean.replace(temp, "\"", "\\\"");
            temp = rptUtilsBean.replace(temp, "(", "\\(");
            temp = rptUtilsBean.replace(temp, ")", "\\)");
          }

          if (strData[i].trim().equals(""))
            strToken = strData[i] + " ";
          else if (spaceDouble)
          {
            spaceDouble = false;
            strToken = "\"" + temp;
          }
          else
            strToken = strData[i];
        }
        else
        {
          if (strData[i].equals(""))
            strToken = strData[i] + " ";
          else
            strToken = strData[i];
        }

        arryListCmdTokenData.add(strToken);
      }

      String strResult = "";
      for (int k = 0; k < arryListCmdTokenData.size(); k++)
      {
        strResult = strResult + " " + arryListCmdTokenData.get(k).toString();
      }

      return strResult;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  // This function convert number to comma separated number.
  // Eg: - 0000 - 0
  // 1 = 1
  // 22 = 22
  // 333 = 333
  // 4444 = 4,444
  // 666666 = 666,666
  public static String convertNumberToCommaSeparate(String strNum)
  {
    try
    {
      String strTemp[] = split(strNum, ".");

      strNum = NumberFormat.getInstance().format(Long.parseLong(strTemp[0]));
      if (strTemp.length > 1)
        strNum = strNum + "." + strTemp[1];

      return strNum;
    }
    catch (Exception e)
    {
      return strNum;
    }
  }

  // This function convert comma separated number to number format
  // Eg:- 4,666 = 4666
  public static String convertCommaSeparatorToNumber(String strNum)
  {
    try
    {
      strNum = strNum.replace(",", "");
      return strNum;
    }
    catch (Exception e)
    {
      return strNum;
    }
  }

  public static String[] sortArray(String[] array, int len)
  {
    int i, j;
    String temp;
    int sortTheStrings = len - 1;

    for (i = 0; i < sortTheStrings; ++i)
    {
      for (j = 0; j < sortTheStrings; ++j)
      {
        if (array[j].toUpperCase().compareTo(array[j + 1].toUpperCase()) > 0)
        {
          temp = array[j];
          array[j] = array[j + 1];
          array[j + 1] = temp;
        }
      }
    }

    return array;
  }

  /**
   * To update alert Schedule time interval
   *
   * @param alertTime
   * @return
   */
  public static boolean updateAlertScheduleIntervalTime(ArrayList<String[]> alertList)
  {
    Log.debugLog(className, "updateAlertScheduleIntervalTime", "", "", "alertTime = " + alertList + ", size = " + alertList.size());
    StringBuffer configBuffer = new StringBuffer();
    boolean alertInterval = false;
    boolean alertMail = false;
    boolean alertHost = false;
    boolean alertPort = false;
    boolean alertReceiver = false;
    boolean alertSender = false;
    boolean alertpassword = false;
    boolean alertReplyTo = false;
    BufferedReader br;
    try
    {
      br = new BufferedReader(new java.io.FileReader(Config.getWorkPath() + "/webapps/sys/config.ini"));

      String str = null;

      while ((str = br.readLine()) != null)
      {
        if (str.startsWith("netstorm.alert.schedule.interval"))
        {
          configBuffer.append("netstorm.alert.schedule.interval = " + getKeywordValueFromList(alertList, "netstorm.alert.schedule.interval"));
          configBuffer.append("\n");
          alertInterval = true;
          Log.debugLog(className, "updateAlertScheduleIntervalTime", "", "", "alertTime = " + alertInterval);
        }
        else if (str.startsWith("netstorm.execution.alert_email"))
        {
          if (alertList.size() >= 2)
          {
            configBuffer.append("netstorm.execution.alert_email = " + getKeywordValueFromList(alertList, "netstorm.execution.alert_email"));
            configBuffer.append("\n");
            alertMail = true;
            Log.debugLog(className, "updateAlertScheduleIntervalTime", "", "", "alertMail = " + alertMail);
          }
          else
          {
            configBuffer.append(str);
            configBuffer.append("\n");
          }

        }
        else if (alertList.size() > 2) // means mail options are also provided.
        {
          if (str.startsWith("netstorm.execution.alert.email_host"))
          {
            configBuffer.append("netstorm.execution.alert.email_host = " + getKeywordValueFromList(alertList, "netstorm.execution.alert.email_host"));
            configBuffer.append("\n");
            alertHost = true;
          }
          else if (str.startsWith("netstorm.execution.alert.email_port"))
          {
            configBuffer.append("netstorm.execution.alert.email_port = " + getKeywordValueFromList(alertList, "netstorm.execution.alert.email_port"));
            configBuffer.append("\n");
            alertPort = true;
          }
          else if (str.startsWith("netstorm.execution.alert.email_TO"))
          {
            configBuffer.append("netstorm.execution.alert.email_TO = " + getKeywordValueFromList(alertList, "netstorm.execution.alert.email_TO"));
            configBuffer.append("\n");
            alertReceiver = true;
          }
          else if (str.startsWith("netstorm.execution.alert.email_from"))
          {
            configBuffer.append("netstorm.execution.alert.email_from = " + getKeywordValueFromList(alertList, "netstorm.execution.alert.email_from"));
            configBuffer.append("\n");
            alertSender = true;
          }
          else if (str.startsWith("netstorm.execution.alert.email_password"))
          {
            configBuffer.append("netstorm.execution.alert.email_password = " + getKeywordValueFromList(alertList, "netstorm.execution.alert.email_password"));
            configBuffer.append("\n");
            alertpassword = true;
          }
          else if (str.startsWith("netstorm.execution.alert.email_replyto"))
          {
            String replyToVal = getKeywordValueFromList(alertList, "netstorm.execution.alert.email_replyto");
            if (!replyToVal.equals("") && !replyToVal.equalsIgnoreCase("NA"))
            {
              configBuffer.append("netstorm.execution.alert.email_replyto = " + getKeywordValueFromList(alertList, "netstorm.execution.alert.email_replyto"));
              configBuffer.append("\n");
              alertReplyTo = true;
            }
          }
          else
          {
            configBuffer.append(str);
            configBuffer.append("\n");
          }
        }
        else
        {
          configBuffer.append(str);
          configBuffer.append("\n");
        }
      }
      // if alert intreval does not present
      if (!alertInterval)
      {
        configBuffer.append("\n#alert schedule interval in miliseconds\n");
        configBuffer.append("netstorm.alert.schedule.interval = " + getKeywordValueFromList(alertList, "netstorm.alert.schedule.interval") + "\n");
        // configBuffer.append("\n");
        alertInterval = true;
      }
      if (!alertMail && alertList.size() >= 2)
      {
        configBuffer.append("\n#alert email option on/off \n");
        configBuffer.append("netstorm.execution.alert_email = " + getKeywordValueFromList(alertList, "netstorm.execution.alert_email") + "\n");
        // configBuffer.append("\n");
        alertMail = true;
      }
      if (alertList.size() > 2)
      {
        if (!alertHost)
        {
          configBuffer.append("\n#alert email host address\n");
          configBuffer.append("netstorm.execution.alert.email_host = " + getKeywordValueFromList(alertList, "netstorm.execution.alert.email_host") + "\n");
          // configBuffer.append("\n");
          alertHost = true;
        }
        if (!alertPort)
        {
          configBuffer.append("\n#alert email host port\n");
          configBuffer.append("netstorm.execution.alert.email_port = " + getKeywordValueFromList(alertList, "netstorm.execution.alert.email_port") + "\n");
          // configBuffer.append("\n");
          alertPort = true;
        }
        if (!alertReceiver)
        {
          configBuffer.append("\n#alert email send to address\n");
          configBuffer.append("netstorm.execution.alert.email_TO = " + getKeywordValueFromList(alertList, "netstorm.execution.alert.email_TO") + "\n");
          // configBuffer.append("\n");
          alertReceiver = true;
        }
        if (!alertSender)
        {
          configBuffer.append("\n#alert email send from address\n");
          configBuffer.append("netstorm.execution.alert.email_from = " + getKeywordValueFromList(alertList, "netstorm.execution.alert.email_from") + "\n");
          // configBuffer.append("\n");
          alertSender = true;
        }
        if (!alertpassword)
        {
          configBuffer.append("\n#alert email sender password\n");
          configBuffer.append("netstorm.execution.alert.email_password = " + getKeywordValueFromList(alertList, "netstorm.execution.alert.email_password") + "\n");
          // configBuffer.append("\n");
          alertpassword = true;
        }
        if (!alertReplyTo)
        {
          String replyToValue = getKeywordValueFromList(alertList, "netstorm.execution.alert.email_replyto").trim();
          if (!replyToValue.equals("") && !replyToValue.equalsIgnoreCase("NA"))
          {
            configBuffer.append("\n#alert email replyTo address\n");
            configBuffer.append("netstorm.execution.alert.email_replyto = " + replyToValue);
            // configBuffer.append("\n");
            alertReplyTo = true;
          }
        }
      }

      final File fileTrendHTML = new File(Config.getWorkPath() + "/webapps/sys/config.ini");
      final java.io.OutputStream outStrem = new java.io.BufferedOutputStream(new java.io.FileOutputStream(fileTrendHTML));
      final java.io.PrintWriter writer = new java.io.PrintWriter(outStrem);
      writer.println(configBuffer);
      writer.close();
      outStrem.close();

      return true;
    }
    catch (FileNotFoundException fE)
    {
      Log.errorLog(className, "updateAlertScheduleIntervalTime", "", "", "Exception in updating alert time - " + fE);
      return false;
    }
    catch (IOException ioE)
    {
      Log.errorLog(className, "updateAlertScheduleIntervalTime", "", "", "Exception in updating alert time - " + ioE);
      return false;
    }

  }

  /**
   * Return the keyword value from list
   *
   * @param listArr
   * @param keyword
   * @return
   */
  private static String getKeywordValueFromList(ArrayList<String[]> listArr, String keyword)
  {
    Log.debugLog(className, "getKeywordValueFromList", "", "", "method called");
    Iterator itr = listArr.iterator();
    while (itr.hasNext())
    {
      String[] arrValues = (String[]) itr.next();
      if (arrValues[0].equals(keyword))
        return arrValues[1];
    }
    return "NA";
  }

  /**
   * Return the keyword value from config.ini
   *
   */
  public static String[] getKeyWordValue(String[] keyWord)
  {
    Log.debugLog(className, "getKeywordValue", "", "", "method called");
    java.util.Properties configProp = new java.util.Properties();
    String configPath = Config.getWorkPath() + "/webapps/sys/config.ini";

    File configFile = new File(configPath);

    try
    {
      configProp.load(new java.io.FileInputStream(configFile));
    }
    catch (FileNotFoundException fE)
    {
      Log.stackTraceLog(className, "getKeywordValue", "", "", "Exception in getKeywordValue ", fE);
    }
    catch (IOException ioE)
    {
      Log.stackTraceLog(className, "getKeywordValue", "", "", "Exception in getKeywordValue ", ioE);
    }

    // keywords value
    String[] keywordsValue = new String[keyWord.length];

    for (int i = 0; i < keyWord.length; i++)
    {
      String keywordValue = configProp.getProperty(keyWord[i]);

      if (keywordValue != null && !keywordValue.equals("NA"))
      {
        Log.debugLog(className, "getKeywordValue", "", "", "keyWord=" + keyWord[i] + " , keywordValue=" + keywordValue);
        keywordsValue[i] = keywordValue;
      }
      else
        keywordsValue[i] = "NA";
    }
    return keywordsValue;
  }

  // Ravi - This function change date time format
  public static String convertCorrectFormatOfDate(String tempDateTime)
  {
    try
    {
      Log.debugLog(className, "convertCorrectFormatOfDate", "", "", "Method Called. Date Time = " + tempDateTime);
      String[] arrDateTime = tempDateTime.split(" ");
      String onlyDate = arrDateTime[0];
      String[] arrOnlyDate = onlyDate.split("/");
      String mm = arrOnlyDate[0];
      String dd = arrOnlyDate[1];
      if (mm.length() != 2)
        mm = "0" + mm;
      if (dd.length() != 2)
        dd = "0" + dd;
      String newDate = mm + "/" + dd + "/" + arrOnlyDate[2];
      String oldTime = arrDateTime[1];
      String newDateTime = newDate + " " + oldTime;
      Log.debugLog(className, "convertCorrectFormatOfDate", "", "", "Changed Date Time = " + newDateTime);
      return newDateTime;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "convertCorrectFormatOfDate", "", "", "Exception - ", ex);
      return tempDateTime;
    }
  }

  /**
   ** This method get all TR directory from webapps/logs
   **
   * @return
   **/
  public static String[] getAllTestRun()
  {
    Log.debugLog(className, "getAllTestRun", "", "", "Method Called");
    try
    {
      String trDirPath = Config.getWorkPath() + "/webapps/logs/";
      File TRDir = new File(trDirPath);
      Log.debugLog(className, "getAllFileName", "", "", "Method Called TRDir = " + TRDir.toString());
      String[] arrProfileDir = TRDir.list(new FilenameFilter()
      {
        @Override
        public boolean accept(File dir, String name)
        {
          File fileObj = new File(dir, name);
          if (fileObj.isDirectory())
          {
            if (fileObj.getName().startsWith("TR"))
            {  
              File TRDir = new File(fileObj.toString());
              if (TRDir.list() != null && TRDir.list().length > 1)
              {
                return fileObj.isDirectory();
              }
            }
          }
          return false;
        }
      });
      return arrProfileDir;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getAllTestRun", "", "", "Exception - ", e);
      return null;
    }
  }

  /**
   ** This method get files with specific extinction from given dir
   **
   * @param fileExt
   ** @param dirName
   ** @return
   **/
  public File[] getAllFileName(String dirName, final String fileExt)
  {
    Log.debugLog(className, "getAllFileName", "", "", "Method Called dirName = " + dirName + ", fileExt = " + fileExt);
    try
    {
      String trDirPath = Config.getWorkPath() + "/webapps/logs/" + dirName + "/harp_files/";
      Log.debugLog(className, "getAllFileName", "", "", "Method Called trDirPath = " + trDirPath);
      File dir = new File(trDirPath);

      return dir.listFiles(new FilenameFilter()
      {
        public boolean accept(File dir, String filename)
        {
          return (filename.startsWith("P_")&&filename.endsWith(fileExt));
        }
      });
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getAllFileName", "", "", "Exception - ", e);
      return null;
    }
  }

  /**
   * Read a file line by line into a vector
   *
   * @author Ravi
   * @param gdfFileWithPath
   * @return file content in a vector
   */
  public static Vector<String> readReport(String filePath)
  {
    Log.debugLog(className, "readReport", "", "", "Method called.  FIle Name = " + filePath);

    try
    {
      Vector<String> vecReport = new Vector<String>();
      String strLine;
      File fileObj = new File(filePath);

      if (!fileObj.exists())
      {
        Log.errorLog(className, "readReport", "", "", "File " + filePath + " is not exist.");
        return null;
      }

      FileInputStream fis = new FileInputStream(filePath);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));

      while ((strLine = br.readLine()) != null)
      {
        strLine = strLine.trim();
        if (strLine.startsWith("#"))
          continue;
        if (strLine.length() == 0)
          continue;

        vecReport.add(strLine);
      }
      br.close();
      fis.close();
      return vecReport;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "readReport", "", "", "Exception - ", e);
      return null;
    }
  }

  /**
   * @author Ravi
   * @param arr
   * @return
   */
  public static double[] skipElementsByNumber(double[] arr, int numOfElementToSkipFromStart)
  {
    double[] arrResult = new double[arr.length - numOfElementToSkipFromStart];
    int count = 0;
    for (int i = numOfElementToSkipFromStart; i < arr.length; i++)
    {
      arrResult[count] = arr[i];
      count++;
    }

    return arrResult;
  }

  /**
   * This function convert array list to array
   *
   * @param arrGraphIndex
   * @return
   */
  public static int[] convertArrayListToIntArray(ArrayList arrGraphIndex)
  {
    try
    {
      int[] arrResult = new int[arrGraphIndex.size()];
      for (int i = 0; i < arrGraphIndex.size(); i++)
      {
        arrResult[i] = Integer.parseInt(arrGraphIndex.get(i).toString());
      }

      return arrResult;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "convertArrayListToIntArray", "", "", "Exception - ", ex);
      return null;
    }
  }

  /**
   * This function convert array list to array
   *
   * @param arrGraphIndex
   * @return
   */
  public static String[] convertArrayListToStrArray(ArrayList arrGraphIndex)
  {
    try
    {
      String[] arrResult = new String[arrGraphIndex.size()];
      for (int i = 0; i < arrGraphIndex.size(); i++)
      {
        arrResult[i] = arrGraphIndex.get(i).toString();
      }

      return arrResult;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "convertArrayListToIntArray", "", "", "Exception - ", ex);
      return null;
    }
  }

  /**
   * used for converting 3 digit value
   *
   * @param val
   * @return
   */
  public static double convNumberTo3DigitDecimal(double val)
  {
    double dblVal;
    dblVal = (double) Math.round(val * 1000);
    dblVal = dblVal / 1000;
    return (dblVal);
  }

  /**
   * Copy arrDest into arrSrc as row wise
   *
   * @param arrSrc
   * @param arrDest
   * @return
   */
  public static double[][] mergeArrays(double[][] arrSrc, double[][] arrDest)
  {
    double[][] arrResult = new double[arrSrc.length + arrDest.length][arrSrc[0].length];

    int row = 0;
    for (int i = 0; i < arrSrc.length; i++)
    {
      for (int j = 0; j < arrSrc[i].length; j++)
      {
        arrResult[i][j] = arrSrc[i][j];
      }

      row++;
    }

    for (int i = 0; i < arrDest.length; i++)
    {
      for (int j = 0; j < arrDest[i].length; j++)
      {
        arrResult[row][j] = arrDest[i][j];
      }

      row++;
    }

    return arrResult;
  }

  /**
   * Copy arrDest into arrSrc as row wise of Integer Type Arrays
   *
   * @param arrSrc
   * @param arrDest
   * @return
   */
  public static int[][] merge2DIntArrays(int[][] arrSrc, int[][] arrDest)
  {
    int[][] arrResult = new int[arrSrc.length + arrDest.length][arrSrc[0].length];

    int row = 0;
    for (int i = 0; i < arrSrc.length; i++)
    {
      for (int j = 0; j < arrSrc[i].length; j++)
      {
        arrResult[i][j] = arrSrc[i][j];
      }

      row++;
    }

    for (int i = 0; i < arrDest.length; i++)
    {
      for (int j = 0; j < arrDest[i].length; j++)
      {
        arrResult[row][j] = arrDest[i][j];
      }

      row++;
    }

    return arrResult;
  }

  public static int[] mergeIntArrays(int[] arrSrc, int[] arrDest)
  {
    int[] arrResult = new int[arrSrc.length + arrDest.length];

    int row = 0;
    for (int i = 0; i < arrSrc.length; i++)
    {
      arrResult[i] = arrSrc[i];
      row++;
    }

    for (int i = 0; i < arrDest.length; i++)
    {
      arrResult[row] = arrDest[i];
      row++;
    }

    return arrResult;
  }

  public static double[] mergeDoubleArrays(double[] arrSrc, double[] arrDest)
  {
    double[] arrResult = new double[arrSrc.length + arrDest.length];

    int row = 0;
    for (int i = 0; i < arrSrc.length; i++)
    {
      arrResult[i] = arrSrc[i];
      row++;
    }

    for (int i = 0; i < arrDest.length; i++)
    {
      arrResult[row] = arrDest[i];
      row++;
    }

    return arrResult;
  }

  /**
   * Ravi -> Used for templates only. There is no problem if someone want to use somewhere.
   *
   * Get Vector Separator Mapping from file.
   *
   * @param fileName
   * @return
   */
  public static ArrayList<VectorMappingDTO> getVectorNameFromMappedFile(String fileName)
  {
    try
    {
      Log.debugLog(className, "getVectorNameFromMappedFile", "", "", "Method Called. File Name = " + fileName);
      ArrayList<VectorMappingDTO> vectorSeparator = new ArrayList<VectorMappingDTO>();

      // Creating instance of File.
      File vectorMappedFile = new File(Config.getWorkPath() + "/webapps/sys/" + fileName);

      // Check for file existence.
      if (!vectorMappedFile.exists())
      {
        Log.debugLog(className, "getVectorNameFromMappedFile", "", "", "vectorCompartibility file not found at location = " + vectorMappedFile.getAbsolutePath());
        return null;
      }

      // Creating reader object to read file.
      BufferedReader fileReader = new BufferedReader(new FileReader(vectorMappedFile));
      String line = "";

      // Read whole file line by line excluding comment lines.
      while ((line = fileReader.readLine()) != null)
      {
        if (!line.startsWith("#") && !line.trim().equals("")) // Check for commented Lines.
        {
          // Tokenize line to get separator.
          String[] tokens = rptUtilsBean.split(line, "|");
          Log.debugLog(className, "getVectorNameFromMappedFile", "", "", "line = " + line + " Tokens = " + tokens.length);

          // Check for valid Number of parameters.
          if (tokens.length == VectorMappingDTO.VECTOR_MAPPING_LINE_LENGTH)
          {
            VectorMappingDTO vectorMappingDTO = new VectorMappingDTO();
            int oldGroupId = Integer.parseInt(tokens[VectorMappingDTO.OLD_GROUP_ID_INDEX]);
            int newGroupId = Integer.parseInt(tokens[VectorMappingDTO.NEW_GROUP_ID_INDEX]);
            String oldVectorName = tokens[VectorMappingDTO.OLD_VECTOR_NAME];
            String newVectorName = tokens[VectorMappingDTO.NEW_VECTOR_NAME];
            vectorMappingDTO.setOldGroupId(oldGroupId);
            vectorMappingDTO.setNewGroupId(newGroupId);
            vectorMappingDTO.setOldVectorName(oldVectorName);
            vectorMappingDTO.setNewVectorName(newVectorName);
            vectorSeparator.add(vectorMappingDTO);
          }
        }
      }

      // If records not available in file then return null.
      if (vectorSeparator == null || vectorSeparator.size() == 0)
        return null;

      Log.debugLog(className, "getVectorNameFromMappedFile", "", "", "Reading Vector Mapping File Completed. list Size = " + vectorSeparator.size());
      return vectorSeparator;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getVectorSeparatorByMappedFile", "", "", "Error in reading vector compartibility File.", e);
      return null;
    }
  }

  /**
   * This Method returns the vector Name Specific to Current TR from Vector Mapping File.
   *
   * @param vecName
   * @return
   */
  public static String getVectorNameFromVectorMapping(ArrayList<VectorMappingDTO> vectorMappingList, int groupId, String vecName)
  {
    try
    {
      Log.debugLog(className, "getVectorNameFromVectorMapping", "", "", "Method Called. for groupId = " + groupId + ", vecName = " + vecName);

      if (vectorMappingList == null || vectorMappingList.size() == 0)
        return vecName;

      String foundVectorName = null;
      for (int i = 0; i < vectorMappingList.size(); i++)
      {
        VectorMappingDTO vectorMappingDTO = vectorMappingList.get(i);
        int oldGroupId = vectorMappingDTO.getOldGroupId();
        int newGroupId = vectorMappingDTO.getNewGroupId();
        String oldVectorName = vectorMappingDTO.getOldVectorName();
        String newVectorName = vectorMappingDTO.getNewVectorName();

        if ((oldGroupId == -1 || newGroupId == -1) && oldVectorName.equals(vecName))
        {
          Log.debugLog(className, "getVectorNameFromVectorMapping", "", "", "Method Called. for groupId = " + groupId + ", vecName = " + vecName + ", found vector name = " + newVectorName);
          return newVectorName;
        }
        else if ((oldGroupId == -1 || newGroupId == -1) && newVectorName.equals(vecName))
        {
          Log.debugLog(className, "getVectorNameFromVectorMapping", "", "", "Method Called. for groupId = " + groupId + ", vecName = " + vecName + ", found vector name = " + oldVectorName);
          return oldVectorName;
        }
        else if (groupId == oldGroupId && oldVectorName.equals(vecName))
        {
          Log.debugLog(className, "getVectorNameFromVectorMapping", "", "", "Method Called. for groupId = " + groupId + ", vecName = " + vecName + ", found vector name = " + newVectorName);
          return newVectorName;
        }
        else if (groupId == newGroupId && newVectorName.equals(vecName))
        {
          Log.debugLog(className, "getVectorNameFromVectorMapping", "", "", "Method Called. for groupId = " + groupId + ", vecName = " + vecName + ", found vector name = " + oldVectorName);
          return oldVectorName;
        }
      }

      Log.debugLog(className, "getVectorNameFromVectorMapping", "", "", "Method Called. for groupId = " + groupId + ", vecName = " + vecName + ", Not found vector and returning = " + vecName);
      return vecName;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getVectorNameFromVectorMapping", "", "", "Exception - ", e);
      return vecName;
    }
  }

  /**
   * This Method is used to check whether text is matching to string with case sensitive
   *
   * @param str
   * @param searchStr
   * @return
   */
  public static boolean containsIgnoreCase(String str, String searchStr)
  {
    try
    {
      if (str == null || searchStr == null)
      {
        return false;
      }
      int len = searchStr.length();
      int max = str.length() - len;
      for (int i = 0; i <= max; i++)
      {
        if (str.regionMatches(true, i, searchStr, 0, len))
        {
          return true;
        }
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "containsIgnoreCase", "", "", "Error in containsIgnoreCase ", e);
    }
    return false;
  }

  /**
   * This method will change date format form one to another(This is used for setting date in datepicker) Eg; Inputs: "8/7/13  05:01:09" , "MM/dd/yy  HH:mm:ss", "MM/dd/yyyy" Output: 08/07/2013
   */
  public String changeDateFormat(String strDateTime, String frmFormat, String toFormat)
  {
    try
    {
      long trStartTime = ExecutionDateTime.convertFormattedDateToMilliscond(strDateTime, frmFormat, null);
      String conDate = ExecutionDateTime.convertDateTimeStampToFormattedString(trStartTime, toFormat, null);

      return conDate;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * To delete all lock file available inside a script
   * @param filePath
   * @return 
   */
  public static void deleteAllLockFile(String filePath)
  {
    Log.debugLog(className, "deleteAllLockFile", "", "", "Method called. The file path = " + filePath );
    File pathTillScriptName =  null;
    String toModifyFilePath = filePath;
    String[] strForAllFile = null;
    pathTillScriptName = new File(toModifyFilePath);
    if(pathTillScriptName.isDirectory())
      strForAllFile = pathTillScriptName.list();
    for(int i = 0; i<strForAllFile.length;i++)
    {
      String str = strForAllFile[i];
      Log.debugLog(className, "deleteAllLockFile", "", "", "The str value for file = "+str);
      File f = new File(filePath + "/" +str);
      if(str.contains(".lock")) //if a lock file
      {
        f.delete();
      }
      else if(str.equals("dump") || str.equals("temp"))  //if dump ot temp directory
      {
        continue;
      }
      else if(f.isDirectory()) //if any other directory found
      {
        deleteAllLockFile(filePath + "/" + str);
      }
    }
  }
  
 // return Double Value with third decimal
 public static double getDoubleValue(double input)
 {
   try
   {
     int decimalPlace = 3;
     BigDecimal bd = new BigDecimal(input);
     bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
     return (bd.doubleValue());
   }
   catch (Exception ex)
   {
     Log.debugLogAlways(className, "getDoubleValue", "", "", "input = " + input);
     return 0.0;
   }
 }
   /** This method replaces the string and returns the parameter
   * @param string
   * @return
   */
  public static String replaceParameterizationSpecialCharacters(String parameter) {
    Log.debugLog(className, "replaceParameterizationSpecialCharacters", "", "", "Method Started" + parameter);
   
    try
    {
      parameter = parameter.replaceAll("\\$Cav_%0D", "\\\\r");
      parameter = parameter.replaceAll("\\$Cav_%2C", ",");
      parameter = parameter.replaceAll("\\$Cav_%09", "\\\\n");
      parameter = parameter.replaceAll("\\$Cav_%7C", "|");
      parameter = parameter.replaceAll("\\$Cav_%22", "\"");
      parameter = parameter.replaceAll("\\$Cav_%26", "&");
      parameter = parameter.replaceAll("\\$Cav_%3C", "<");
      parameter = parameter.replaceAll("\\$Cav_%3E", ">");
      return parameter;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "replaceParameterizationSpecialCharacters", "", "", "Exception in replacing parameters", ex);
      return "";
    }

  }
 
  // For Testing only
  public static void main(String args[])
  {
    getControllerSpecificInfo();
    String strFilterLine = "--access Chicago --location London --status 0";
    String result = "";
    ArrayList aa = rptUtilsBean.getListOfDirs("c:\\Arun\\");
    for (int recNum = 0; recNum < aa.size(); recNum++)
      System.out.println("Dir Name " + aa.get(recNum).toString());

    System.out.println(rptUtilsBean.convertNumberToCommaSeparate("66666.4"));
    /*
     * Vector v = new Vector(10, 2); int recNum; v.add("\"TRNum|ScenName|StartTime\""); v.add("\"|11|12|13|999\""); v.add("\"20|21|22|23\""); v.add("\"30|31|32\""); v.add("|||");
     *
     * for(recNum = 0; recNum < v.size(); recNum++) System.out.println("Vector " + recNum + " - " + v.elementAt(recNum));
     *
     * String arrRecFlds[][] = getRecFlds(v, "A|B|C|D", "0|3|2|1", "Empty"); if(arrRecFlds == null) { System.out.println("Returned null"); return; }
     *
     * for(recNum = 0; recNum < arrRecFlds.length; recNum++) System.out.println("Record " + recNum + " - " + arrToString(arrRecFlds[recNum]));
     */
    result = getUpdatedSessVariable(strFilterLine, "undefined", "All", "0");
    System.out.println("result is " + result);
  }
}
// -- end of class -------------------------------------------------------------

/*--------------------------------------------------------------------
  @Name      :
  @Author    :
  @Purpose   :
  @Modification History:

----------------------------------------------------------------------*/

package pac1.Bean;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Vector;

public class summaryData
{
  private String className = "SummaryData";
  private String workPath = null;
  private final static String SUMMARY_FILE_NAME = "summary";
  private final static String SUMMARY_FILE_EXTN = ".data";
  public static final int DATA_VALUE_INDEX = 4;
  private String testRun = "";
  private LinkedHashMap mapResult;

  public summaryData(String testRun)
  {
    //numTestRun = Integer.parseInt(testRun);
    workPath = Config.getWorkPath();
    this.testRun = testRun;
  }
  public void initMap(String args[])
  {
  	try
  	{
  		mapResult = new LinkedHashMap();
  		String value = "";
  		for (int i = 0 ; i < args.length ; i++)
  		{
  			String key = args[i];
  			mapResult.put(key, value);
  		}
  	}
  	catch(Exception ex)
  	{

  	}
  }

  private boolean checkLineForToken(String line)
  {
  	Log.debugLog(className, "checkLineForToken", "", "", "Method called. line = " + line);
  	try
  	{
  		Set keySet = mapResult.keySet();
  		for (Object obj : keySet)
  		{
  			String key = obj.toString();
      	int index = line.indexOf("|" + key + "|");
      	String strToAdd = rptUtilsBean.strToArrayData(line, "|")[DATA_VALUE_INDEX];
        if(index != -1)
        {
        	String value = mapResult.get(key).toString();
        	if (!value.equals(""))
        		Log.debugLog(className, "checkLineForToken", "", "", "Found duplicate entry for token - " + key + ", ignoring previos");
        	mapResult.put(key, strToAdd);
        	Log.debugLog(className, "checkLineForToken", "", "", "Found duplicate entry for token - " + key);
        }
  		}
  		return true;
  	}
  	catch(Exception ex)
  	{
  		return false;
  	}
  }

  //This returns full path of passed profile
  private String getSummaryDataPath()
  {
    return (workPath + "/webapps/logs/TR" + testRun + "/" + SUMMARY_FILE_NAME + SUMMARY_FILE_EXTN);
  }

// Methods for reading the File
  // Move to rptUtils.java
  private boolean readFileInVector(String fileNameWithPath)
  {
    Log.debugLog(className, "readFileInVector", "", "", "Method called. File Name = " + fileNameWithPath);

    try
    {
      String strLine;

      File fileName = new File(fileNameWithPath);

      if(!fileName.exists())
      {
        Log.debugLog(className, "readFileInVector", "", "", "File " + fileNameWithPath + " does not exist.");
        return false;
      }

      FileInputStream fis = new FileInputStream(fileNameWithPath);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));

      while((strLine = br.readLine()) !=  null)
      {
        strLine = strLine.trim();
        if(strLine.length() == 0)
          continue;
        if(!checkLineForToken(strLine))
        	return false;
      }

      br.close();
      fis.close();

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "readFileInVector", "", "", "Exception - ", e);
      return false;
    }
  }

  private boolean isSummaryDataFilePresent()
  {
    Log.debugLog(className, "isSummaryDataFilePresent", "", "", "Method called.");

    //getting path of note file
    String summaryDataFileWithPath = getSummaryDataPath();

    File summaryDataFileObj = new File(summaryDataFileWithPath);

     //checking file existing or not
    if(!summaryDataFileObj.exists())
    {
      Log.debugLog(className, "isSummaryDataFilePresent", "", "", summaryDataFileWithPath + " file does not exist");
      return false;
    }
    else
    {
      Log.debugLog(className, "isSummaryDataFilePresent", "", "",summaryDataFileWithPath + " file exist");
      return true;
    }
  }

  public LinkedHashMap readSpecificSummaryData()
  {
    Log.debugLog(className, "readSpecificSummaryData", "", "", "Method called");
    try
    {
      // Check if summary data file is present or not
      if(!isSummaryDataFilePresent())
        return null;  // In this case all members of vector will null

      //getting path of note file
      String summaryDataFileWithPath = getSummaryDataPath();

      //reading file and store in vector
      if(!readFileInVector(summaryDataFileWithPath))
      	return null;

      return mapResult;

    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "readSpecificSummaryData", "", "", "Exception - ", e);
      return null;
    }
  }

/*************************************Main Method*******************************/
/*
  public static void main(String[] args)
  {
  	Vector vecData = new Vector(Arrays.asList(args));
    summaryData SummaryData_obj = new summaryData("29232");
    SummaryData_obj.initMap(args);
    LinkedHashMap map = SummaryData_obj.readSpecificSummaryData();

    if (map == null)
    {
    	//show error here and exit
    }
    for(Object obj : map.keySet())
    {
    	System.out.println(map.get(obj.toString()));

    }
  }
  */
}
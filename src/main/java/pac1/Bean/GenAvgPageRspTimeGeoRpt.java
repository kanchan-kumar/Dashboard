/*--------------------------------------------------------------------
  @Name    : GenAvgPageRspTimeGeoRpt.java
  @Author   : Rohit Kumar Tiwari
  @Purpose : Generate the locations.xml file that is used to generate WebGeo Rpt
  @Modification History:

  Pending Tasks:

----------------------------------------------------------------------*/

package pac1.Bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/*This class make Object for Per location and set  
 * maximum response time
 * overall reponce time
*/
class GetLocationData
{
  private String className = "GetLocationData";

  // Contains access name and the response time in seconds for all accceses used by this location 
  public HashMap locWithAccandRespTimeHashMap = new HashMap(); //This map contain the key(location name) & value(HashMap(Which contain the access name and response time))
  public HashMap accessRespMap = new HashMap(); // This map contain key as (access name) & value (Responce time)

  private double maxRespTime = 0.0; //This is default maxTime 
  private double sumOfAllRespTime = 0.0; //This is contain the sum of all response time of page 

  private String locName = "";
  private int count = 0;

  private Vector locData = null;
  private int countOfNumberOfAccess = 0;

  GetLocationData(String locName, Vector locData)
  {
    this.locName = locName;
    this.locData = locData;
    this.maxRespTime = maxRespTime;
    this.sumOfAllRespTime = sumOfAllRespTime;
    this.countOfNumberOfAccess = countOfNumberOfAccess;
    init();
  }

  private void init()
  {
    Log.debugLog(className, "init", "", "", "Method Starts. ");

    try
    {
      for(int i = 0; i < locData.size(); i++)
      {
        // <location> <access> <response time in secs>
        if(locData.elementAt(i).toString().startsWith(locName))
        {
          String[] arrTemp = rptUtilsBean.strToArrayData(locData.elementAt(i).toString(), " ");
          Log.debugLog(className, "init", "", "", ", Acess type = " + arrTemp[1] + ", Responce time in sec = " + arrTemp[2]);
          accessRespMap.put(arrTemp[1], arrTemp[2]);//create map with key(Access type) ,value(Response time)
          double maxTime = new Double(arrTemp[2]);
          Log.debugLog(className, "init", "", "", ", max time = " + maxTime);
          sumOfAllRespTime = sumOfAllRespTime + maxTime;
          countOfNumberOfAccess++;
          Log.debugLog(className, "init", "", "", ", sum of all resp time  = " + sumOfAllRespTime);
          //Here we take maximum response time
          if(maxRespTime < maxTime)
          {
            maxRespTime = maxTime;
          }
        }
      }
      locWithAccandRespTimeHashMap.put(locName, accessRespMap);//add location as a key and hash map as a value which contain <access> and <responce time>
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "init", "", "", "Exception - ", e);
    }
  }

  public String getRespTimeByAccess(String accessName)
  {
    return accessRespMap.get(accessName).toString();
  }

  public String getLocName()
  {
    return locName;
  }

  public double getMaxRespTime()
  {
    return maxRespTime;
  }

  public int getCount()
  {
    return count;
  }

  public void setCount()
  {
    count++;
  }

  public double getSumOfAllRespTime()
  {
    return sumOfAllRespTime;
  }

  public void setSumOfAllRespTime(double sumOfAllRespTime)
  {
    this.sumOfAllRespTime = sumOfAllRespTime;
  }

  public int getCountOfNumberOfAccess()
  {
    return countOfNumberOfAccess;
  }

  public void setCountOfNumberOfAccess(int countOfNumberOfAccess)
  {
    this.countOfNumberOfAccess = countOfNumberOfAccess;
  }
}

public class GenAvgPageRspTimeGeoRpt
{
  private static String className = "GenAvgPageRspTimeGeoRpt";
  private String workPath = Config.getWorkPath();
  private final static String LOC_FILE = "locWithAccessAndRespTime";
  private final static String LOC_FILE_EXTN = ".dat";
  private final static String VENDOR_DATA_FILE = "VendorData";
  private final static String VENDOR_DATA_FILE_EXTN = ".default";
  private final static String DEFAULT_SERVER_LOC = "SanFrancisco";

  private CmdExec cmdExec = null;
  private static GetLocationData[] getLocationData = null;
  private String testRun = "";
  private Vector locData = null;
  private Vector vendorData = null;
  private Vector vecSortedScenarioData = null;

  private String[][] arrLocLatLong = null;
  private ArrayList arrActualServerLoc = new ArrayList();
  private String defaultServerLocationKeywordVal = DEFAULT_SERVER_LOC;

  public ArrayList locationArr = new ArrayList();
  public ArrayList accessLocation = new ArrayList();
  public ArrayList actualSerLoc = new ArrayList();

  public ArrayList getActualSerLoc()
  {
    return actualSerLoc;
  }

  public void setActualSerLoc(ArrayList actualSerLoc)
  {
    this.actualSerLoc = actualSerLoc;
  }

  public ArrayList getLocationArr()
  {
    return locationArr;
  }

  public void setLocationArr(ArrayList locationArr)
  {
    locationArr = locationArr;
  }

  public ArrayList getAccessLocation()
  {
    return accessLocation;
  }

  public void setAccessLocation(ArrayList accessLocation)
  {
    accessLocation = accessLocation;
  }

  /* Get locWithAccessAndRespTime.dat file information
   * Example oflocWithAccessAndRespTime.dat
   * <Location name><Access type><Responce Time>
   *     Chennai       MyAccess1     3.890
   *     Mumbai           T1         0.033 
   */
  private Vector getLocFileData()
  {
    Log.debugLog(className, "getLocFileData", "", "", "Method called");

    String locFileWithPath = workPath + "/webapps/logs/TR" + testRun + "/" + LOC_FILE + LOC_FILE_EXTN;

    return(readReport(locFileWithPath));
  }

  /*Get VendorData.default file information
   * Example of Vendor.default file 
   */
  private Vector getVendorFileData()
  {
    Log.debugLog(className, "getVendorFileData", "", "", "Method called");

    String vendorDataFileWithPath = workPath + "/include/" + VENDOR_DATA_FILE + VENDOR_DATA_FILE_EXTN;

    return(readReport(vendorDataFileWithPath));
  }

  // Get scenario file information
  private Vector getScenarioFileData()
  {
    Log.debugLog(className, "getScenarioFileData", "", "", "Method called");

    String TRNUMWithFullPath = "";

    // First read scenario kewords from test run sorted file
    TRNUMWithFullPath = workPath + "/webapps/logs/TR" + testRun;
    // File fileArr[] = rptUtilsBean.getListOfFilesWithMatchingPatteren(TRNUMWithFullPath, "sorted_*" + fname.substring(fname.lastIndexOf("/") + 1) + ".conf");
    File fileArr[] = rptUtilsBean.getListOfFilesWithMatchingPatteren(TRNUMWithFullPath, "sorted_*.conf");
    if(fileArr != null)
    {
      String sortedFileName = fileArr[0].getAbsolutePath();
      Log.debugLog(className, "getScenarioFileData", "", "", "Sorted file Name = " + sortedFileName);
      return(readReport(sortedFileName));
    }
    else
    // If soreted file is not found then read from scenrio file
    {
      String scenFileWithPath = workPath + "/webapps/logs/TR" + testRun + "/scenario";
      return(readReport(scenFileWithPath));
    }
  }

  // Methods for reading the File
  private Vector readReport(String locFileWithPath)
  {
    Log.debugLog(className, "readReport", "", "", "Method called. Loc FIle Name = " + locFileWithPath);

    try
    {
      Vector vecReport = new Vector();
      String strLine;

      File locFile = openFile(locFileWithPath);

      if(!locFile.exists())
      {
        Log.errorLog(className, "readReport", "", "", "Test Location data file not found in the Test Run - " + testRun);
        return null;
      }

      FileInputStream fis = new FileInputStream(locFileWithPath);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));

      while((strLine = br.readLine()) != null)
      {
        strLine = strLine.trim();
        if(strLine.startsWith("#"))
          continue;
        if(strLine.length() == 0)
          continue;
        vecReport.add(strLine);
      }

      br.close();
      fis.close();

      return vecReport;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "readReport", "", "", "Exception - ", e);
      return null;
    }
  }

  // this function initialize all location data
  private boolean getAllLocationData()
  {
    Log.debugLog(className, "getLocation", "", "", "Method Starts. ");

    try
    {
      //Run as netstorm now. To be changed later
      locData = getLocFileData();

      //Vector vecLocation = cmdExec.getResultByCommand("nsi_get_location", testRun, CmdExec.NETSTORM_CMD, null, "netstorm");
      Vector vecLocation = getLocationData(locData);

      vendorData = getVendorFileData();

      vecSortedScenarioData = getScenarioFileData();

      if(vecLocation.elementAt(0).toString().startsWith("ERROR|"))
        return false;

      String[] arrLocation = new String[vecLocation.size()];
      getLocationData = new GetLocationData[vecLocation.size()];

      for(int i = 0; i < vecLocation.size(); i++)
      {
        arrLocation[i] = vecLocation.elementAt(i).toString();
        getLocationData[i] = new GetLocationData(arrLocation[i], locData);
      }

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getLocation", "", "", "Exception - ", e);
      return false;
    }
  }

  /**
   * This method create need to remove dependency of nsi_get_location shall it take time
   * we get locations name from locWithAccessAndRespTime.dat file
   * @return loationName vector
   */
  private Vector<String> getLocationData(Vector locationLine)
  {
    Vector locationArr = new Vector();
    for(int i = 0; i < locationLine.size(); i++)
    {
      String str = locationLine.get(i).toString();
      String[] tmpArr = str.split(" ");
      if(!locationArr.contains(tmpArr[0]))
        locationArr.add(tmpArr[0]);
    }
    return locationArr;
  }

  // Open file and return as File object
  private File openFile(String fileName)
  {
    Log.debugLog(className, "openFile", "", "", "Open Input file = " + fileName);

    try
    {
      File dataFile = new File(fileName);
      return(dataFile);
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "openFile", "", "", "Exception - ", e);
      return null;
    }
  }

  // This function set the location latitude longitude array
  private void setLocLatLongArr()
  {
    Log.debugLog(className, "setLocLatLongArr", "", "", "Method Starts.");
    Vector vecVendorDataLocation = new Vector();

    try
    {
      for(int i = 0; i < vendorData.size(); i++)
      {
        if(vendorData.elementAt(i).toString().startsWith("LOCATION"))
        {
          vecVendorDataLocation.add(vendorData.elementAt(i).toString());
          Log.debugLog(className, "setLocLatLongArr", "", "", "vendor data fieAdding line in vector. Line = " + vendorData.elementAt(i).toString());
        }
      }

      //It will read location keyword from scenario file and add in vector
      for(int i = 0; i < vecSortedScenarioData.size(); i++)
      {
        if(vecSortedScenarioData.elementAt(i).toString().startsWith("LOCATION"))
        {
          vecVendorDataLocation.add(vecSortedScenarioData.elementAt(i).toString());
          Log.debugLog(className, "setLocLatLongArr", "", "", "Sorted file line Adding line in vector. Line = " + vecSortedScenarioData.elementAt(i).toString());
        }
      }

      arrLocLatLong = new String[vecVendorDataLocation.size()][3];

      for(int i = 0; i < vecVendorDataLocation.size(); i++)
      {
        String[] arrTemp = rptUtilsBean.strToArrayData(vecVendorDataLocation.elementAt(i).toString(), " ");

        arrLocLatLong[i][0] = arrTemp[1]; //This array contain the location name 
        arrLocLatLong[i][1] = arrTemp[2];// this array contain the access name 
        arrLocLatLong[i][2] = arrTemp[3];//this array contain the responce time
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "setLocLatLongArr", "", "", "Exception - ", e);
    }
  }

  // This function set the actual server array
  private void setActualServerArr()
  {
    Log.debugLog(className, "setActualServerArr", "", "", "Method Starts.");
    String[] arrTemp = null;

    try
    {

      // First find the deafult server location as it may be after SERVER_HOST
      for(int i = 0; i < vecSortedScenarioData.size(); i++)
      {
        if(vecSortedScenarioData.elementAt(i).toString().startsWith("DEFAULT_SERVER_LOCATION"))
        {
          arrTemp = rptUtilsBean.strToArrayData(vecSortedScenarioData.elementAt(i).toString(), " ");
          defaultServerLocationKeywordVal = arrTemp[1]; //add default server location i.e SanFrancisco
          arrActualServerLoc.add(defaultServerLocationKeywordVal); //add default server location in actual server vector
          Log.debugLog(className, "setActualServerArr", "", "", "Default server location = " + defaultServerLocationKeywordVal);
        }
      }

      // Scenario can have multiple SERVER_HOST keywords
      for(int i = 0; i < vecSortedScenarioData.size(); i++)
      {
        if(vecSortedScenarioData.elementAt(i).toString().startsWith("SERVER_HOST"))
        {
          arrTemp = rptUtilsBean.strToArrayData(vecSortedScenarioData.elementAt(i).toString(), " ");
          addServersToActualServersList(arrTemp);
        }
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "setActualServerArr", "", "", "Exception - ", e);
    }
  }

  //This method contain all server from server list
  private void addServersToActualServersList(String[] arrTemp)
  {
    String actualServer = "";
    // SERVER_HOST <recorded_server> <actual_server> <location> [<recorded_server> <actual_server> <location>] ....
    for(int i = 3; i < arrTemp.length; i = i + 2)
    {
      Log.debugLog(className, "setActualServerArr", "", "", "recorded server = " + arrTemp[0].toString() + ", Actual server = " + arrTemp[1].toString() + ", server location = " + arrTemp[2].toString());
      if(arrTemp[i].equals("-"))
      {
        actualServer = defaultServerLocationKeywordVal;
        Log.debugLog(className, "setActualServerArr", "", "", "Adding default serever in actual Server in vector = " + defaultServerLocationKeywordVal);
      }
      else
      {
        actualServer = arrTemp[i].toString();
        Log.debugLog(className, "setActualServerArr", "", "", "Adding Actual Server in vector = " + arrTemp[i].toString());
      }

      if(arrActualServerLoc.contains(actualServer))
        Log.debugLog(className, "setActualServerArr", "", "", "array list Already contain this Server ");
      else
        arrActualServerLoc.add(actualServer);
    }

  }

  //This method is use for genrateXML for actual server
  private void generateActualServerPartOfXMLFile()
  {
    Log.debugLog(className, "generateActualServerPartOfXMLFile", "", "Method call ", " Actual server size = " + arrActualServerLoc.size());
    try
    {
      for(int i = 0; i < arrActualServerLoc.size(); i++)
      {
        Log.debugLog(className, "generateActualServerPartOfXMLFile", "", "", "Line ***** " + arrActualServerLoc.get(i).toString() + "|" + getLatByLoacation(arrActualServerLoc.get(i).toString()) + "|" + getLongByLoacation(arrActualServerLoc.get(i).toString()));
        actualSerLoc.add(arrActualServerLoc.get(i).toString() + "|" + getLatByLoacation(arrActualServerLoc.get(i).toString()) + "|" + getLongByLoacation(arrActualServerLoc.get(i).toString()));
      }
    }
    catch(Exception e)
    {
      Log.debugLog(className, "generateActualServerPartOfXMLFile", "", "", "Exception :" + e);
    }
  }

  //This method is use for XML for user location 
  private void generateUserLocationPartOfXMLFile()
  {
    try
    {
      for(int i = 0; i < getLocationData.length; i++)
      {
        int setLatPosition = 5;

        setLatPosition = (setLatPosition * getLocationData[i].getCount() * (-1));

        double latPosition = setLatPosition + Double.parseDouble(getLatByLoacation(getLocationData[i].getLocName()));

        Log.debugLog(className, "generateAccessPartOfXMLFile", "", "", "Location Data =" + getLocationData[i].getLocName() + "|" + getColorByDataVal(getLocOverallRspTime(i)) + "|" + latPosition + "|" + getLongByLoacation(getLocationData[i].getLocName()) + "|" + getLocOverallRspTime(i));
        locationArr.add(getLocationData[i].getLocName() + "|" + getColorByDataVal(getLocOverallRspTime(i)) + "|" + latPosition + "|" + getLongByLoacation(getLocationData[i].getLocName()) + "|" + getLocOverallRspTime(i));
        getLocationData[i].setCount();
      }
    }
    catch(Exception e)
    {
      Log.debugLog(className, "generateAccessPartOfXMLFile", "", "", "Exception =" + e);
    }
  }

  //This method is use for generate the access part of XML
  private void generateAccessPartOfXMLFilePerXML(Double latPosition, String strAccessName, String strRespTime, String strLocName)
  {
    Log.debugLog(className, "generateAccessPartOfXMLFilePerXML", "", "", "Method Starts.");
  }

  private void generateAccessPartOfXMLFile(String strAccessName, String strRespTime, String strLocName, int i)
  {
    Log.debugLog(className, "generateAccessPartOfXMLFile", "", "", "Method Starts." + ", Access name = " + strAccessName + ", Responce time = " + strRespTime + ", location name = " + strLocName);
    try
    {
      // For each user locations,
      int setLatPosition = 5;
      if(!(strRespTime.equals("NA")))
      {
        setLatPosition = (setLatPosition * getLocationData[i].getCount() * (-1));
        double latPosition = setLatPosition + Double.parseDouble(getLatByLoacation(strLocName));
        generateAccessPartOfXMLFilePerXML(latPosition, strAccessName, strRespTime, strLocName);
        Log.debugLog(className, "generateAccessPartOfXMLFile", "", "", "Location Name= " + strLocName + "|" + strAccessName + "|" + latPosition + "|" + getLongByLoacation(strLocName) + "|" + getColorByDataVal(Double.parseDouble(strRespTime)));
        accessLocation.add(strLocName + "|" + strAccessName + "|" + latPosition + "|" + getLongByLoacation(strLocName) + "|" + strRespTime + "|" + getColorByDataVal(Double.parseDouble(strRespTime)));
        getLocationData[i].setCount();
      }
    }
    catch(Exception e)
    {
      Log.debugLog(className, "generateAccessPartOfXMLFile", "", "", "Exception= " + e);
    }
  }

  public void getLocationDataFromFiles(String trNum)
  {
    Log.debugLog(className, "getLocationDataFromFiles", "", "", "Method Starts. trNum = " + trNum);
    testRun = trNum;
    cmdExec = new CmdExec();
    getAllLocationData();
    setLocLatLongArr();
    setActualServerArr();
  }

  //This function generate the locations.xml file
  public Vector generateGeoMapData(String testRun)
  {
    Log.debugLog(className, "generateXMLFile", "", "", "Method Starts.");
    getLocationDataFromFiles(testRun);
    boolean checkAccessNotFound = false;
    //This map contain the accessName through key of access name 
    HashMap xmlFileNameAccessName = new HashMap();
    xmlFileNameAccessName.put("56K", "Telephone(56K)");
    xmlFileNameAccessName.put("28.8K", "Telephone(28K)");
    xmlFileNameAccessName.put("Cable", "Cable");
    xmlFileNameAccessName.put("DSL", "DSL");
    xmlFileNameAccessName.put("T1", "T1");
    xmlFileNameAccessName.put("1M_DSL", "1M_DSL");
    xmlFileNameAccessName.put("2.5M_DSL", "2.5M_DSL");
    xmlFileNameAccessName.put("384K_DSL", "384K_DSL");
    xmlFileNameAccessName.put("FastEthernet", "FastEthernet");

    try
    {
      // Actual Servers
      generateActualServerPartOfXMLFile();

      // User Locations
      generateUserLocationPartOfXMLFile();

      //Access part of xml
      Log.debugLog(className, "generateXMLFile", "", "", "location array size = " + getLocationData.length);
      for(int xx = 0; xx < getLocationData.length; xx++)
      {
        String strLocName = getLocationData[xx].getLocName();
        Log.debugLog(className, "generateXMLFile", "", "", "location  name = " + strLocName);
        //This map contain <Location Name> <Access Type> <Resp Time> and return the map of <acessType> <Responce Time>
        HashMap accessRespTimeHashMap = (HashMap)getLocationData[xx].locWithAccandRespTimeHashMap.get(strLocName);
        ArrayList arrOfAllAccess = new ArrayList();
        arrOfAllAccess.addAll(accessRespTimeHashMap.keySet());//add all value of key set in array list
        Log.debugLog(className, "generateXMLFile", "", "", "array size of all acess = " + arrOfAllAccess.size());
        for(int j = 0; j < arrOfAllAccess.size(); j++)
        {

          String xmlFileAcceName = "";
          String acessName = arrOfAllAccess.get(j).toString();
          if(!xmlFileNameAccessName.containsKey(acessName))
            checkAccessNotFound = true;
          else
            xmlFileAcceName = xmlFileNameAccessName.get(acessName).toString();
          String strOfAccess = arrOfAllAccess.get(j).toString();
          String strOfAccessRespTime = accessRespTimeHashMap.get(strOfAccess).toString();
          if(checkAccessNotFound)
          {
            xmlFileAcceName = acessName; //This is a sccess name in case of when we use user profile
            strLocName = getLocationData[xx].getLocName();
            strOfAccessRespTime = accessRespTimeHashMap.get(xmlFileAcceName).toString();
          }
          generateAccessPartOfXMLFile(xmlFileAcceName, strOfAccessRespTime, strLocName, xx);
          //generateAccessPartOfXMLFile(xmlFileAcceName, strOfAccessRespTime, strLocName, j);
        }
      }

      setLocationArr(locationArr);
      setAccessLocation(accessLocation);
      setActualSerLoc(actualSerLoc);
      Vector dataVector = new Vector();

      dataVector.add(getLocationArr());
      dataVector.add(getAccessLocation());
      dataVector.add(getActualSerLoc());

      Log.debugLog(className, "getLocFileData", "", "", " locationArr = " + getLocationArr().size() + " accessLocation = " + getAccessLocation().size());
      Log.debugLog(className, "getLocFileData", "", "", "data = " + getLocationArr());
      Log.debugLog(className, "getLocFileData", "", "", "data = " + getAccessLocation());

      return dataVector;
    }
    catch(Exception e)
    {
      System.out.println("Error: Error in generateXMLFile()");
      Vector vec = new Vector();
      vec.add("Eception:-:");
      return vec;
    }
  }

  private void getLocationLatCount(String locName)
  {
    Log.debugLog(className, "getLocationLatCount", "", "", "Method Starts.");

    int count = 0;
    for(int i = 0; i < getLocationData.length; i++)
    {
      if(getLocationData[i].getLocName().equals(locName))
      {
        count = getLocationData[i].getCount();
        getLocationData[i].setCount();
        break;
      }
    }
  }

  // This function return the location id by location name
  private String getLocIdByLoc(String locName)
  {
    Log.debugLog(className, "getLocIdByLoc", "", "", "location name = " + locName);
    String workPath = Config.getWorkPath();

    workPath = workPath + "/etc/longlat.default";

    Log.debugLog(className, "getLocIdByLoc", "", "", "work path = " + workPath);
    String[][] arrOfLongLatiLocId = rptUtilsBean.getFileDataIn2D(workPath);
    for(int i = 0; i < arrOfLongLatiLocId.length; i++)
    {
      String locationName = arrOfLongLatiLocId[i][2].toString();//This is read file from path work/etc/longlat.default file
      locationName = locationName.replaceAll(" ", "");

      if(locationName.equals(locName))
      {
        return arrOfLongLatiLocId[i][5]; //arrOfLongLatiLocId[i][5] contain the location Id 
      }
    }
    return "";
  }

  // This function check that Latitude is positive or negative
  private boolean isLatPositive(String value)
  {
    Log.debugLog(className, "isLatPositive", "", "", "Method Starts.");

    double dataVal = Double.parseDouble(value);

    if(dataVal < 0)
      return false;
    else
      return true;
  }

  // This function return the Latitude by location name
  private String getLatByLoacation(String location)
  {
    Log.debugLog(className, "getLatByLoacation", "", "", "Method Starts." + " Location = " + location);

    boolean rtnFlag = false;
    int i = 0;
    for(i = 0; i < arrLocLatLong.length; i++)
    {
      if(arrLocLatLong[i][0].equals(location))
      {
        rtnFlag = true;
        break;
      }
    }
    Log.debugLog(className, "getLatByLoacation", "", "", "rtnFlag value = " + rtnFlag + " return value = " + arrLocLatLong[i][1]);
    if(rtnFlag)
      return(arrLocLatLong[i][1]);
    else
      return "";
  }

  // This function return the Longitude by location name
  private String getLongByLoacation(String location)
  {
    Log.debugLog(className, "getLongByLoacation", "", "", "Method Starts.");

    boolean rtnFlag = false;
    int i = 0;
    for(i = 0; i < arrLocLatLong.length; i++)
    {
      if(arrLocLatLong[i][0].equals(location))
      {
        rtnFlag = true;
        break;
      }
    }
    if(rtnFlag)
      return(arrLocLatLong[i][2]);
    else
      return "";
  }

  private String getLocColorByAvgRspTime(int idx)
  {
    return(getColorByDataVal(getLocationData[idx].getMaxRespTime()));
  }

  private double getLocOverallRspTime(int idx)
  {
    Log.debugLog(className, "getLocOverallRspTime", "", "", "Method Starts. Index = " + idx);
    int count = getLocationData[idx].getCountOfNumberOfAccess();
    double sumOfAllRspTime = getLocationData[idx].getSumOfAllRespTime();
    double overallAvgData = sumOfAllRspTime / count;
    return((Math.round(overallAvgData * 100)) / 100.00);
  }

  private String getColorByDataVal(double dataVal)
  {
    int[] thresholdMinMaxValue = getThresHoldValue();
    Log.debugLog(className, "getColorByDataVal", "", "", "dataVal = " + dataVal + "Min value = " + thresholdMinMaxValue[0] + "Max value = " + thresholdMinMaxValue[1]);
    if(dataVal <= thresholdMinMaxValue[0])
      return NSColor.HX_VALUE_GREEN;

    else if(dataVal > thresholdMinMaxValue[0] && dataVal <= thresholdMinMaxValue[1])
      return NSColor.HX_VALUE_YELOW;

    else if(dataVal > thresholdMinMaxValue[1])
      return NSColor.HX_VALUE_RED;

    else
      return "";
  }

  /**
   * This method read sorted file data from vecSortedScenarioData vector and get THRESHOLD
   * value from it.
   * @return Min Max value
   */
  private int[] getThresHoldValue()
  {
    Log.debugLog(className, "getThresHoldValue", "", "", "Method Starts.");
    String[] vecScenarioData = new String[2];
    int[] value = new int[2];
    boolean ThresholdKeyWordFound = false;
    vecScenarioData = Scenario.getKeywordValues("THRESHOLD", Integer.parseInt(testRun));
    
    if(vecScenarioData != null)
    {
      Log.debugLog(className, "getThresHoldValue", "", "", "Key word Min value = " + vecScenarioData[0] + "Key word Max value = " + vecScenarioData[1]);
      value[0] = (Integer.parseInt(vecScenarioData[0]) / 1000);
      value[1] = (Integer.parseInt(vecScenarioData[1]) / 1000);
      Log.debugLog(className, "getThresHoldValue", "", "", "Min = " + value[0] + "Max = " + value[1]);
      ThresholdKeyWordFound = true;
    }

    if(!ThresholdKeyWordFound)
    {
      value[0] = (4000 / 1000);
      value[1] = (8000 / 1000);
    }

    return value;
  }

  //main method is only for testing 
  public static void main(String[] args)
  {
    GenAvgPageRspTimeGeoRpt genAvgPageRspTimeGeoRpt = new GenAvgPageRspTimeGeoRpt();
  }
}

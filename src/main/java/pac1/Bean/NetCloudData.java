/**************************************************************
 * @author Ravi
 * @purpose used for netcloud data
 * @modification history
 * 
 * 25/09/2013 Ravi Kant Sharma ---> Initial Version
 ************************************************************/
package pac1.Bean;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Vector;

public class NetCloudData
{
  public static String className = "NetCloudData";

  // Reading net cloud data
  public static Vector<String> getNetCloudData(int testNum)
  {
    Log.debugLog(className, "getNetCloudData", "", "", "method Called." + testNum);
    if (testNum == -1)
      return null;
   
    String netCloudFilePath = getNetCloudDataFilePath(testNum);
    
    File filePath = new File(netCloudFilePath);
    
    if(!filePath.exists())
      return null;

    Log.debugLog(className, "getNetCloudData", "", "", "File Path = " + netCloudFilePath );
    return rptUtilsBean.readReport(netCloudFilePath);
  }

  // NetCloud Data file path
  private static String getNetCloudDataFilePath(int testNum)
  {
    try
    {
      String gdfRptFileWithPath = Config.getWorkPath() + "/webapps/logs/TR" + testNum + "/NetCloud/NetCloud.data";
      return gdfRptFileWithPath;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getNetCloudDataPath", "", "", "Exception - ", ex);
      return "";
    }
  }
  
  /**
   * This method accepts a controller testrun , vector containing data of NetCloud.data file and parses it and creates a linked hash map containing
   * key as generator id and value as ArrayList of containing generator test run and generator name
   * it parses following line 
   *    NETCLOUD_GENERATOR_TRUN 6726|India|192.168.1.91|7891|/home/netstorm/work|IPV4:192.168.1.91|0
   * generatoes hashmap with key =  0 and valu e= Arraylist containing 6726 at 0 the pos and India at 1st position
   * @param vecData
   * @param testNum
   * @return LinkedHashMap containing key   = generator id , value = ArrayList of testrun and generator name
   */
  
  public static LinkedHashMap<String, ArrayList<String>> getGeneratorDetailsWithTR(Vector<String> vecData, int controllerTestNum)
  {
    Log.debugLog(className, "getGeneratorNamesWithTR", "", "", "Method called");
    try
    {
      LinkedHashMap<String, ArrayList<String>> generatorMap = new LinkedHashMap<String, ArrayList<String>>();
      
      if(vecData == null && vecData.size() == 0)
      {
        Log.errorLog(className, "getGeneratorDetailsWithTR", "", "", "Not able to read NetCloud info for given Test Run number(" + controllerTestNum+ ").");
        return null;
      }
      
      ArrayList<String [] > list = new ArrayList<String[]>();
      
      for(int i = 0 ; i < vecData.size() ; i++ )
      {
        String [] tempArr = new String[3];

        String dataLine = vecData.get(i);
        
        if(dataLine.trim().startsWith("#"))
          continue;
        
        String[] arrData = rptUtilsBean.split(dataLine, "|");
        
        if(arrData.length >= 7)
        {
          //value will be NETCLOUD_GENERATOR_TRUN 6669, need to get TR Number
          String trNumber = arrData[0].substring(arrData[0].lastIndexOf(" ")).trim();
          String generatorId = arrData[6].trim();
          String generatorName = arrData[1].trim();
          tempArr[0] = generatorId;
          tempArr[1] = trNumber;
          tempArr[2] = generatorName;
        }
        list.add(tempArr);
      }
      
      String [][] dataIn2D = new String [list.size()][3];
      
      for(int j = 0 ; j < list.size() ; j++)
      {
        dataIn2D [j] = list.get(j);
      }
      
      Arrays.sort(dataIn2D, new Comparator<String[]>()
      {
        public int compare(final String[] entry1, final String[] entry2)
        {
           final String time1 = entry1[0];
           final String time2 = entry2[0];
           
           return time1.compareToIgnoreCase(time2);
        }
      });
      
      for(int i = 0 ; i < dataIn2D.length ; i++)
      {
        ArrayList<String> detailsOfController = new ArrayList<String>();
        detailsOfController.add(dataIn2D[i][1]);
        detailsOfController.add(dataIn2D[i][2]);
        generatorMap.put(dataIn2D[i][0] ,detailsOfController);
      }
      
      ArrayList<String> detailsOfController = new ArrayList<String>();//adding details of generator
      detailsOfController.add(controllerTestNum + "");
      detailsOfController.add("Controller");
      
      generatorMap.put("-1" ,detailsOfController);
      
      return generatorMap;
    }
    catch (Exception ex) 
    {
      Log.stackTraceLog(className, "getGeneratorDetailsWithTR", "", "", "Exception - ", ex);
      return null;
    }
  }
  
  public static ArrayList<NetCloudDTO> getNectCloudDtoList(Vector<String> vecData, int controllerTestNum)
  {
    Log.debugLog(className, "getGeneratorNamesWithTR", "", "", "Method called");
    try
    {
      ArrayList<NetCloudDTO> ncDtoList = new ArrayList<NetCloudDTO>();
      
      if(vecData == null && vecData.size() == 0)
      {
        Log.errorLog(className, "getGeneratorDetailsWithTR", "", "", "Not able to read NetCloud info for given Test Run number(" + controllerTestNum+ ").");
        return null;
      }
      
      ArrayList<String [] > list = new ArrayList<String[]>();
      
      for(int i = 0 ; i < vecData.size() ; i++ )
      {
        String [] tempArr = new String[3];

        String dataLine = vecData.get(i);
        
        if(dataLine.trim().startsWith("#"))
          continue;
        
        String[] arrData = rptUtilsBean.split(dataLine, "|");
        
        if(arrData.length >= 7)
        {
          //value will be NETCLOUD_GENERATOR_TRUN 6669, need to get TR Number
          String trNumber = arrData[0].substring(arrData[0].lastIndexOf(" ")).trim();
          String generatorId = arrData[6].trim();
          String generatorName = arrData[1].trim();
          tempArr[0] = generatorId;
          tempArr[1] = trNumber;
          tempArr[2] = generatorName;
        }
        list.add(tempArr);
      }
      
      String [][] dataIn2D = new String [list.size()][3];
      
      for(int j = 0 ; j < list.size() ; j++)
      {
        dataIn2D [j] = list.get(j);
      }
      
      Arrays.sort(dataIn2D, new Comparator<String[]>()
      {
        public int compare(final String[] entry1, final String[] entry2)
        {
           final String time1 = entry1[0];
           final String time2 = entry2[0];
           
           return time1.compareToIgnoreCase(time2);
        }
      });
      
      for(int i = 0 ; i < dataIn2D.length ; i++)
      {
        NetCloudDTO ncDto = new NetCloudDTO();
        ncDto.setGeneratorID(dataIn2D[i][0]);
        ncDto.setGeneratorName(dataIn2D[i][2]);
        ncDto.setGeneratorTRNumber(dataIn2D[i][1]);
        ncDtoList.add(ncDto);
      }
           
      return ncDtoList;
    }
    catch (Exception ex) 
    {
      Log.stackTraceLog(className, "getGeneratorDetailsWithTR", "", "", "Exception - ", ex);
      return null;
    }
  }
}

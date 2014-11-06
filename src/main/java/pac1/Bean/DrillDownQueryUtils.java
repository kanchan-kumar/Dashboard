/***File Name : DrillDownQueryUtils.java
 * Purpose: Utility and management of drill down report
****/

package pac1.Bean;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Vector;

public class DrillDownQueryUtils
{
  private static String className = "DrillDownUtils"; //Class Name
  String controllerPath = Config.getWorkPath(); //work path
  final String DRILL_DOWN_FOLDER = "drilldown_queries"; //Folder Name
  final String DRILL_DOWN_REPORT_EXTENSION = ".ddrq"; //Folder Name
  private String LIMIT = "0";
  private String OFFSET = "0";

  String userName = "";

  //Constructor with with argument
  public DrillDownQueryUtils(String userName)
  {
    this.userName = userName;
  }

  //Folder Path Ex: /home/netstorm/work/webapps/sys/drilldown_queries
  public String getDrillDownFilterFolder()
  {
    String filterFile = controllerPath + "/webapps/sys/" +  DRILL_DOWN_FOLDER;
    return filterFile;
  }

  //Filter file name
  //No extension
  public String getDrillDownFilterFile(String fileName)
  {
    String filterFile = getDrillDownFilterFolder() + "/" + fileName + DRILL_DOWN_REPORT_EXTENSION;
    return filterFile;
  }

  public ArrayList getAvailableGroupAndOrderByList(String strWAN_ENV)
  {
    Log.debugLog(className, "getAvailableGroupAndOrderByList", "", "", "Method called.");
    ArrayList arrListGroupBy = new ArrayList();

    try
    {
      DrillDownExecuteQuery.setConfigurableKeywords();
      arrListGroupBy.add("URL");
      arrListGroupBy.add("Page");
      arrListGroupBy.add("Transaction");
      arrListGroupBy.add("Session");

      if(DrillDownExecuteQuery.IS_LOCATION_ACCESS_ENABLE.equals("1") || strWAN_ENV.equals("1"))
      {
        arrListGroupBy.add("Location");
        arrListGroupBy.add("Access");
      }

      if((DrillDownExecuteQuery.IS_BROWSER_ENABLE_FROM_SRC_CSV) && (DrillDownExecuteQuery.IS_BROWSER_ENABLE.equals("1")))
        arrListGroupBy.add("Browser");

      arrListGroupBy.add("Status");
      return arrListGroupBy;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getAvailableGroupAndOrderByList", "", "", "Exception - ", e);
      return arrListGroupBy;
    }
  }

/*
 * This method add filters.
 * create file same as filter name which contains Object of  drillDownReportData
 *
 * @params
 * Args1:- Filter name
 * Args2:- Object of DrillDownReportData (which contain getter setter
 * Args3:- true or update and false for add
 *
 * @return Object array of length 2
 * Object[0] = true/false (Success or not)
 * Object[1] = Message
 *
 */
  public Object[] addUserDefineFilter(String filterName, DrillDownReportQuery drillDownReportData, boolean isOverride)
  {
    Log.debugLog(className, "addUserDefineFilter", "", "", "Method called. filterName = " + filterName);
    Object[] objResult = new Object[2];
    try
    {
      //create directory from user ownner
      SSLManagementNO SSLObj = new SSLManagementNO(userName);

      // Get Filter Path
      File fileQueryObj = new File(getDrillDownFilterFile(filterName));

      //If file exists and isOverride is true
      //delete and create filter file
      //Else return already message
      if(fileQueryObj.exists())
      {
        if(!isOverride)
        {
          Log.debugLog(className, "addUserDefineFilter", "", "", "Filter name already exists");
          objResult[0] = false;
          objResult[1] = "Filter name already exists";
          return objResult;
        }
        fileQueryObj.delete(); //delete file
      }
      SSLObj.mkdirWithOutRoot(getDrillDownFilterFolder()); //create directory from user ownner

      fileQueryObj.createNewFile();

      FileOutputStream fout = new FileOutputStream(fileQueryObj);
      ObjectOutputStream oos = new ObjectOutputStream(fout);

      oos.writeObject(drillDownReportData); //add object in the filter file

      oos.flush();
      fout.flush();

      oos.close();
      fout.close();

      if(drillDownReportData != null)
      {
        Log.debugLog(className, "addUserDefineFilter", "", "", "Filter Setting = " + drillDownReportData.getFilterSetting());
      }

      objResult[0] = true;

      if(isOverride)
        objResult[1] = "Filter " + filterName + " is updated sucessfully";
      else
        objResult[1] = "Filter " + filterName + " is added sucessfully";

      return objResult;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "addUserDefineFilter", "", "", "Exception - ", e);

      objResult[0] = false;
      objResult[1] = "Error in adding filter";
      return objResult;
    }
  }


  /**
   * This Method clones String 2d array.
   * @param filterName
   * @return
   */

  public String[][] getCloned2DArray(String [][]arrData)
  {
    try
    {
      if(arrData == null)
        return null;

      String [][] arrShadowArray = (String[][])arrData.clone();
      for(int i = 0; i < arrData.length; i++)
      {
    	arrShadowArray[i] = (String[])arrData[i].clone();
      }
      return arrShadowArray;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getCloned2DArray", "", "", "Exception = ", e);
      return null;
    }
  }

  /**
   * This Method return the time in ss.ms format.
   * @param filterName
   * @return
   */
  public String getFormattedDate(String time)
  {
    try
    {
      long absoluteTimeStamp = Long.parseLong(time);
      long milisecond = absoluteTimeStamp % 1000;
      long seconds = (absoluteTimeStamp - milisecond) / 1000;
      String ms = "" + milisecond;
      String ss = "" + seconds;
      if (milisecond < 10)
        ms = "00" + milisecond;

      if (milisecond < 100)
        ms = "0" + milisecond;

      if (seconds < 10)
        ss = "0" + seconds;

      String tmp = ss + "." + ms;
      return tmp;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getFormattedDate", "", "", "Exception - ", ex);
      return "00.00";
    }
  }

  /*
   * This method add filters.
   * Delete filter file
   *
   * @params
   * Args1:- Filter name
   *
   * @return Object array of length 2
   * Object[0] = true/false (Success or not)
   * Object[1] = Message
   *
   */

  public Object[] deleteUserDefineFilter(String filterName)
  {
    Log.debugLog(className, "deleteUserDefineFilter", "", "", "Method called. filterName = " + filterName);

    Object[] objResult = new Object[2];
    try
    {
      File fileQueryObj = new File(getDrillDownFilterFile(filterName));
      if(!fileQueryObj.delete()) //checking file delete or not
      {
        Log.debugLog(className, "deleteUserDefineFilter", "", "", "Filter " + filterName + " is not deleted successfully");

        objResult[0] = false;
        objResult[1] = "Filter " + filterName + " is not deleted successfully";
        return objResult;
      }

      Log.debugLog(className, "deleteUserDefineFilter", "", "", "Filter " + filterName + " deleted successfully");
      objResult[0] = true;
      objResult[1] = "Filter " + filterName + " deleted successfully";
      return objResult;
    }
    catch (Exception e)
    {
      objResult[0] = false;
      objResult[1] = "Error in in deleting filter";
      Log.stackTraceLog(className, "deleteUserDefineFilter", "", "", "Exception - ", e);
      return objResult;
    }
  }

  /**
   * This method show available user define filter
   * System define filter
   * Flow Path
   *0- filter name
   *1- Created By
   *2- Last modified date
   *3- status
   *4- Description
   * @return
   */
  public String[][] getAvailableDrillDownFilters()
  {
    Log.debugLog(className, "getAvailableDrillDownFilters", "", "", "Method called.");

    try
    {
      String arrResult[][] = null;

      int userDefineIndex = 0; //read count user define filter

      Vector vecAddObj = new Vector();
      File fileFilterObject = new File(getDrillDownFilterFolder());

      //If drill down folder does not exists set user define index = 0
      if(!fileFilterObject.exists())
        userDefineIndex = 0;
      else
      {
        //Get list of file from drill down folder
        String arrFilterTemp[] = fileFilterObject.list();

        for(int i = 0; i < arrFilterTemp.length; i++)
        {
          try
          {
            if(arrFilterTemp[i].lastIndexOf(DRILL_DOWN_REPORT_EXTENSION) == -1)
              continue;

            //Getting file name with extension
            //so we are removing extension
            String strTempFileNameWithoutExt = arrFilterTemp[i].substring(0, arrFilterTemp[i].lastIndexOf(DRILL_DOWN_REPORT_EXTENSION));
            //Checking its DrillDownReportData object or not
            DrillDownReportQuery drillDownReportData = getReportInfo(strTempFileNameWithoutExt);
            //If null we will assume its not a filter file
            if(drillDownReportData != null)
              vecAddObj.add(drillDownReportData);
          }
          catch (Exception ex)
          {
            //System.out.println("sssss" + ex);
          }

        }
      }

      //If user define filters present, here we are setting in the array
      if(vecAddObj != null)
      {
        userDefineIndex = vecAddObj.size();
        arrResult = new String[userDefineIndex][6];
        //arrResult = new String[userDefineIndex + 5][5];
        for(int i = 0; i < userDefineIndex; i++)
        {
          DrillDownReportQuery drillDownReportData = (DrillDownReportQuery)vecAddObj.get(i);
          arrResult[i][0] = drillDownReportData.getReportName();
          arrResult[i][1] = drillDownReportData.getUserName();
          arrResult[i][2] = drillDownReportData.getLastModifiedDate();
          arrResult[i][3] = drillDownReportData.getReportType();
          arrResult[i][4] = drillDownReportData.getDescription();
          arrResult[i][5] = drillDownReportData.getObjectType();
        }
	      CorrelationService correlationService = new CorrelationService();
	      arrResult = correlationService.sortArray(arrResult, 2,1, "DATE");
      }

      //else //size for system define
        //arrResult = new String[5][5];

      /**for(int k = userDefineIndex, j = 0; k < arrResult.length; k++,j++)
      {
        if(j == 0)
        {
          arrResult[k][0] = "URL";
          arrResult[k][4] = "URL Description";
        }
        else if(j == 1)
        {
          arrResult[k][0] = "Page";
          arrResult[k][4] = "Page Description";
        }
        else if(j == 2)
        {
          arrResult[k][0] = "Transaction";
          arrResult[k][4] = "Transaction Description";
        }
        else if(j == 3)
        {
          arrResult[k][0] = "Session";
          arrResult[k][4] = "Session Description";
        }
        else if(j == 4)
        {
          arrResult[k][0] = "Flow Path";
          arrResult[k][4] = "Flow Description";
        }
        arrResult[k][1] = "netstorm";

        arrResult[k][2] = DrillDownReportData.getCurrentDate();

        arrResult[k][3] = "0";
      }**/

      return arrResult;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getAvailableDrillDownFilters", "", "", "Exception - ", e);
      return null;
    }
  }

  /*
   * This method read filter file and check DrillDownReportData Object or not
   * if present return DrillDownReportData object
   */
  public DrillDownReportQuery getReportInfo(String filterName)
  {
    Log.debugLog(className, "getReportInfo", "", "", "Method called.");

    DrillDownReportQuery downReportData = null;
    try
    {
      FileInputStream fis = new FileInputStream(getDrillDownFilterFile(filterName));

      ObjectInputStream ois = new ObjectInputStream(fis);
      Object objDto = ois.readObject();

      //If Object null
      if(objDto == null)
        return null;

      //Checking file contain instance of DrillDownReportQuery or not
      if(objDto instanceof DrillDownReportQuery)
      {
        //Type cast readobject
        downReportData = (DrillDownReportQuery)(objDto);
        //checking Object not or
        if(downReportData != null )
        {
          ois.close();
          fis.close();
          return downReportData;
        }
      }
      ois.close();
      fis.close();
      return downReportData; //return null Object
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getAvailableDrillDownFilters", "", "", "Exception - ", e);
      return downReportData;
    }

  }

  public void setSystemDefineQueries()
  {
    Log.debugLog(className, "setSystemDefineQueries", "", "", "Method called.");

    try
    {
      DrillDownReportQuery drillDownReportDataURL = new DrillDownReportQuery();
      drillDownReportDataURL.setReportName("URL");
      drillDownReportDataURL.setReportType("0");
      drillDownReportDataURL.setUserName("netstorm");
      drillDownReportDataURL.setDescription("URL Description");
      drillDownReportDataURL.setObjectType("0");
      drillDownReportDataURL.setShowData("4095");
      drillDownReportDataURL.setShowDataLabelandValue("16,64,32,128,256,512,1024,2048,1,4,2,8");
      drillDownReportDataURL.setObjectStatus("-2");
      addUserDefineFilter("URL", drillDownReportDataURL, true);

      Log.debugLog(className, "setSystemDefineQueries", "", "", "For URL = " + drillDownReportDataURL.getFilterSetting());

      DrillDownReportQuery drillDownReportDataPage = new DrillDownReportQuery();
      drillDownReportDataPage.setReportName("Page");
      drillDownReportDataPage.setReportType("0");
      drillDownReportDataPage.setUserName("netstorm");
      drillDownReportDataPage.setDescription("Page Description");
      drillDownReportDataPage.setObjectType("1");
      drillDownReportDataPage.setShowData("4095");
      drillDownReportDataPage.setShowDataLabelandValue("16,64,32,128,256,512,1024,2048,1,4,2,8");
      drillDownReportDataPage.setObjectStatus("-2");
      addUserDefineFilter("Page", drillDownReportDataPage, true);

      Log.debugLog(className, "setSystemDefineQueries", "", "", "For Page = " + drillDownReportDataPage.getFilterSetting());

      DrillDownReportQuery drillDownReportDataTrans = new DrillDownReportQuery();
      drillDownReportDataTrans.setReportName("Session");
      drillDownReportDataTrans.setReportType("0");
      drillDownReportDataTrans.setUserName("netstorm");
      drillDownReportDataTrans.setDescription("Session Description");
      drillDownReportDataTrans.setObjectType("3");
      drillDownReportDataTrans.setShowData("4095");
      drillDownReportDataTrans.setShowDataLabelandValue("16,64,32,128,256,512,1024,2048,1,4,2,8");
      drillDownReportDataTrans.setObjectStatus("-2");
      addUserDefineFilter("Session", drillDownReportDataTrans, true);

      Log.debugLog(className, "setSystemDefineQueries", "", "", "For Session = " + drillDownReportDataTrans.getFilterSetting());

      DrillDownReportQuery drillDownReportDataSession = new DrillDownReportQuery();
      drillDownReportDataSession.setReportName("Transaction");
      drillDownReportDataSession.setReportType("0");
      drillDownReportDataSession.setUserName("netstorm");
      drillDownReportDataSession.setDescription("Transaction Description");
      drillDownReportDataSession.setObjectType("2");
      drillDownReportDataSession.setShowData("4095");
      drillDownReportDataSession.setShowDataLabelandValue("16,64,32,128,256,512,1024,2048,1,4,2,8");
      drillDownReportDataSession.setObjectStatus("-2");
      addUserDefineFilter("Transaction", drillDownReportDataSession, true);

      Log.debugLog(className, "setSystemDefineQueries", "", "", "For Transaction = " + drillDownReportDataSession.getFilterSetting());

      DrillDownReportQuery drillDownReportDataFlowPath = new DrillDownReportQuery();
      drillDownReportDataFlowPath.setReportName("FlowPath");
      drillDownReportDataFlowPath.setReportType("0");
      drillDownReportDataFlowPath.setUserName("netstorm");
      drillDownReportDataFlowPath.setDescription("Flow Path Description");
      drillDownReportDataFlowPath.setObjectType("4");
      drillDownReportDataFlowPath.setObjectStatus("-2");
      addUserDefineFilter("FlowPath", drillDownReportDataFlowPath, true);

      Log.debugLog(className, "setSystemDefineQueries", "", "", "For Flow Path = " + drillDownReportDataFlowPath.getFilterSetting());

      DrillDownReportQuery drillDownReportDataMethodTiming = new DrillDownReportQuery();
      drillDownReportDataMethodTiming.setReportName("Method Timing");
      drillDownReportDataMethodTiming.setReportType("0");
      drillDownReportDataMethodTiming.setUserName("netstorm");
      drillDownReportDataMethodTiming.setDescription("Method Self Time Description");
      drillDownReportDataMethodTiming.setObjectType("7");
      drillDownReportDataMethodTiming.setObjectStatus("-2");
      addUserDefineFilter("Method Timing", drillDownReportDataMethodTiming, true);

      Log.debugLog(className, "setSystemDefineQueries", "", "", "For Method Timing = " + drillDownReportDataFlowPath.getFilterSetting());


      DrillDownReportQuery drillDownReportDataLogs = new DrillDownReportQuery();
      drillDownReportDataLogs.setReportName("Logs");
      drillDownReportDataLogs.setReportType("0");
      drillDownReportDataLogs.setUserName("netstorm");
      drillDownReportDataLogs.setDescription("Logs Description");
      drillDownReportDataLogs.setObjectType("5");
      drillDownReportDataLogs.setObjectStatus("-2");
      drillDownReportDataLogs.setGroupBy("tier,server,app");
      addUserDefineFilter("Logs", drillDownReportDataLogs, true);

      Log.debugLog(className, "setSystemDefineQueries", "", "", "For Logs = " + drillDownReportDataLogs.getFilterSetting());

      DrillDownReportQuery drillDownReportDataQueries = new DrillDownReportQuery();
      drillDownReportDataQueries.setReportName("DB Requests");
      drillDownReportDataQueries.setReportType("0");
      drillDownReportDataQueries.setUserName("netstorm");
      drillDownReportDataQueries.setDescription("DB Requests Description");
      drillDownReportDataQueries.setObjectType("6");
      drillDownReportDataQueries.setObjectStatus("-2");
      drillDownReportDataQueries.setShowData("");
      drillDownReportDataQueries.setGroupBy("query,tier,server,app");
      addUserDefineFilter("DB Requests", drillDownReportDataQueries, true);

      Log.debugLog(className, "setSystemDefineQueries", "", "", "For DB Requests = " + drillDownReportDataQueries.getFilterSetting());

      DrillDownReportQuery drillDownReportDataService = new DrillDownReportQuery();
      drillDownReportDataService.setReportName("Service");
      drillDownReportDataService.setReportType("0");
      drillDownReportDataService.setUserName("netstorm");
      drillDownReportDataService.setDescription("Service Report Description");
      drillDownReportDataService.setObjectType("8");
      drillDownReportDataService.setObjectStatus("-2");
      drillDownReportDataService.setShowData("");
      addUserDefineFilter("Service", drillDownReportDataService, true);

      Log.debugLog(className, "setSystemDefineQueries", "", "", "For Service = " + drillDownReportDataService.getFilterSetting());

      DrillDownReportQuery drillDownReportDataException = new DrillDownReportQuery();
      drillDownReportDataException.setReportName("Exception");
      drillDownReportDataException.setReportType("0");
      drillDownReportDataException.setUserName("netstorm");
      drillDownReportDataException.setDescription("Exception Report Description");
      drillDownReportDataException.setObjectType("15");
      drillDownReportDataException.setObjectStatus("-2");
      addUserDefineFilter("Exception", drillDownReportDataException, true);

      Log.debugLog(className, "setSystemDefineQueries", "", "", "For Exception = " + drillDownReportDataException.getFilterSetting());

    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getAvailableDrillDownFilters", "", "", "Exception - ", e);
    }
  }

  //Method used to Decode HTML Encoded characters in String.
  public static String decodeHTMLEncoding(String str)
  {
    Log.debugLog(className, "decodeHTMLEncoding", "", "", "method called");
    try
    {
      if(str.equals(""))
	return "";

      str = str.replaceAll("&#039;", "\'");
      str = str.replaceAll("&#044;", ",");
      str = str.replaceAll("&#010;", "\n");
      str = str.replaceAll("&#034;", "\"");
      str = str.replaceAll("&#092;", "\\");

      return str;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "decodeHTMLEncoding", "", "", "Error in decoding", e);
      return str;
    }
  }

  public static void main(String args[])
  {
    int choice = 0;
    DrillDownQueryUtils DrillDownObj = new DrillDownQueryUtils("userName");

    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    System.out.println("********Please enter the option for desired operation*********");
    System.out.println("Show Available Filter info. Enter : 1");
    System.out.println("Add Filter info. Enter : 2");
    System.out.println("Getting Report Info. Enter : 3");
    System.out.println("Delete Filter info. Enter : 4");
    System.out.println("*************************************************************");
    try
    {
      choice = Integer.parseInt(br.readLine());
    }
    catch(IOException e)
    {
      System.out.println("Error in entered choice: " + e);
    }

    switch(choice)
    {
      case 1:
        String arrTemp[][] = DrillDownObj.getAvailableDrillDownFilters();
        if(arrTemp != null)
        for(int i = 0; i < arrTemp.length; i++)
        {
          for (int j = 0; j < arrTemp[i].length; j++)
          {
            System.out.print(arrTemp[i][j] + " ");
          }
          System.out.println("\n");
        }
        break;

      case 2:
        DrillDownReportQuery drillDownReportData = new DrillDownReportQuery();
        drillDownReportData.setReportName("hhhh");
        drillDownReportData.setUserName("jyoo");
        drillDownReportData.setReportType("1");
        drillDownReportData.setObjectType("0");
        drillDownReportData.setGroupBy("Page,Session,Location");
        drillDownReportData.setShowData("4095");
        drillDownReportData.setShowDataLabelandValue("16,64,32,128,256,512,1024,2048,1,4,2,8");

        Object arrTemp1[] = DrillDownObj.addUserDefineFilter("hhhh", drillDownReportData, false);
        drillDownReportData.setReportName("delete");
        Object arrTemp2[] = DrillDownObj.addUserDefineFilter("delete", drillDownReportData, false);
        if(arrTemp1 != null)
        for(int i = 0; i < arrTemp1.length; i++)
        {
          System.out.print(arrTemp1[i] + " ");
        }

        DrillDownObj.setSystemDefineQueries();

        break;

      case 3:
        DrillDownReportQuery drillDownReportData1 = DrillDownObj.getReportInfo("hhhh");

        if(drillDownReportData1 != null)
          System.out.print(drillDownReportData1.getFilterSetting());
        break;

      case 4:
        Object[] arrTemp4 = DrillDownObj.deleteUserDefineFilter("delete");
        System.out.println("arrTemp4[0] = " + arrTemp4[0] + ", arrTemp4[1] = " + arrTemp4[1]);
        break;
      default:
        System.out.println("Please select the correct option.");
    }
  }
}

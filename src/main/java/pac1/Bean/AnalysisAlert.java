package pac1.Bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Vector;


public class AnalysisAlert implements java.io.Serializable
{
  private final String className = "AnalysisAlert";
  public final String ANLYSIS_ALERT_FILE = "alert_history_scheduler";
  public final String ANLYSIS_ALERT_FILE_EXTN = ".dat";

  private String[][] strArrAlertFileData =  null;
  private String numTestRun = "-1";
  private boolean isNDE = false;
  private String filePath = null;
  
  public AnalysisAlert(String numTestRun)
  {
    this(numTestRun, null);
  }
  
  public AnalysisAlert(String numTestRun, String filePath)
  {
    try
    {
      this.filePath = filePath;
      
      CmdExec cmdExec = new CmdExec();
      String command = "nsi_show_config";
      String args = " -t";
      try
      {
        Vector result = cmdExec.getResultByCommand(command, args, CmdExec.SYSTEM_CMD, "netstorm", "root");
        String ndeMode = result.get(0).toString().trim();
        Log.debugLog(className, "generateEvents", "", "", "ndeMode = " + ndeMode);
        if (ndeMode.equals("NDE"))
          isNDE = true;
      }
      catch(Exception ex)
      {
        Log.errorLog(className, "AnalysisAlert", "", "", "Exception - " + ex);
      }
    }
    catch (Exception e)
    {
      Log.errorLog(className, "generateEvents", "", "", "Exception - " + e);
    }
    this.numTestRun = numTestRun;
    init();
  }

  private void init()
  {
    Log.debugLog(className, "init", "", "", "Method called");
    
    try
    {
      strArrAlertFileData = getAlertDataFromServer();
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "init", "", "", "Exception - ", e);
    }
  }
  
  public String[][] getStrArrAlertFileData()
  {
    return strArrAlertFileData;
  }
  
  //get path to /webapps/logs/TRXXXX/alert.log
  private String getAlertLogFileWithPath()
  {
    if(isNDE)
    {
      return (Config.getWorkPath() + "/webapps/logs/advisoryRules" + "/" + ANLYSIS_ALERT_FILE + ANLYSIS_ALERT_FILE_EXTN);
    }
    else
    {
      return (Config.getWorkPath() + "/webapps/logs/TR" + numTestRun + "/" + ANLYSIS_ALERT_FILE + ANLYSIS_ALERT_FILE_EXTN);
    }
  }

  //reading file line by line.
  private ArrayList readAlertFile(boolean ignoreComment, boolean ignoreBlankLine)
  {
    Log.debugLog(className, "readAlertFile", "", "", "Method Called.");
    try
    {
      ArrayList arrData = new ArrayList();

      String analysisAlertNameWithPath = getAlertLogFileWithPath();
      
      //PropFileData objPropFileData = null;

      File analysisAlertFileName = new File(analysisAlertNameWithPath);

      if(!analysisAlertFileName.exists())
      {
        Log.debugLog(className, "readAlertFile", "", "", " File does not exist = " + analysisAlertNameWithPath);
      }
      else
      {
        if(isNDE)
        {
          String filePath = (Config.getWorkPath() + "/webapps/logs/advisoryRules" + "/" + ANLYSIS_ALERT_FILE + ANLYSIS_ALERT_FILE_EXTN);
          analysisAlertFileName = new File(filePath);
        }
        else
        {
          analysisAlertFileName = new File((Config.getWorkPath() + "/webapps/logs/TR" + numTestRun + "/" + ANLYSIS_ALERT_FILE + ANLYSIS_ALERT_FILE_EXTN));
        }
      }
      
      if(filePath != null)
      {
	analysisAlertFileName = new File(filePath);
	analysisAlertNameWithPath = filePath;
      }
      
      // Still file does not exist
      if(!analysisAlertFileName.exists())
      {
        Log.debugLog(className, "readAlertFile", "", "", " File does not exist = " + analysisAlertNameWithPath + ", filePath = " + filePath);
        return null;
      }
      
      FileInputStream fis = new FileInputStream(analysisAlertFileName);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));

      String strLine = "";
      while((strLine = br.readLine()) != null)
      {
        strLine = strLine.trim();

        if((strLine.startsWith("#")) && (ignoreComment))
          continue;
        if((strLine.length() == 0) && (ignoreBlankLine))
          continue;

        arrData.add(strLine);

      }

      Log.debugLog(className, "readAlertFile", "", "", "Data in Alert File" + arrData.size());
      br.close();
      fis.close();

      return arrData;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "readAlertFile", "", "", "Exception - ", e);
      return null;
    }
  }

  private String[][] getAlertDataFromServer()
  {
    Log.debugLog(className, "getAlertDataFromServer", "", "", "Method Called.");

    String[][] arrAlertData;

    try
    {
      ArrayList arrReadDataList = readAlertFile(true, true);
      if(arrReadDataList != null)
      {
        arrAlertData = new String[arrReadDataList.size()][18];
        for(int i = 0; i < arrReadDataList.size(); i++)
        {
          String[] arrTemp = rptUtilsBean.split(arrReadDataList.get(i).toString(), "|");
          for(int j = 0; j < arrTemp.length; j++)
          {
            arrAlertData[i][j] = arrTemp[j];
          }
        }
      }
      else
        return null;
      
      return arrAlertData;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getAlertDataFromServer", "", "", "Exception - ", ex);
      return null;
    }
  }

  public static void main(String args[])
  {
    AnalysisAlert AnalysisAlert_obj = new AnalysisAlert("20142");
    ArrayList arrList = AnalysisAlert_obj.readAlertFile(true, true);

    /*for(int i = 0; i< arrList.size(); i++)
    {
      System.out.println("---------" + arrList.get(i));
    }*/

    String arrfileList[][] = AnalysisAlert_obj.getAlertDataFromServer();
     for(int i = 0; i< arrfileList.length; i++)
    {
       for(int j = 0; j< arrfileList[i].length; j++)
      System.out.println("---------\n" + arrfileList[i][j]);
    }
  }
}


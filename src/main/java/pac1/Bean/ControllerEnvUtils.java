package pac1.Bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class ControllerEnvUtils 
{
  private String className = "ControllerEnvUtils";
  
  private String controllerConfPath = Config.getWorkPath() + "/sys/controllers.config";
  
  public boolean isControllersDashboardEnabled()
  {
    try
    {
      Log.debugLog(className, "isControllersDashboardEnabled", "", "", "method called");
    
      File fileObj = new File(controllerConfPath);
      
      if(!fileObj.exists())
      {
        Log.debugLog(className, "isControllersDashboardEnabled", "", "", "Controller config file does not exist.");
        return false;
      }
      
      Log.debugLogAlways(className, "isControllersDashboardEnabled", "", "", "Controller config file exist.");
      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "isControllersDashboardEnabled", "", "", "Exception in checking controller conf file", e);
      return false;
    }
  }
  
  /*
   * #Controller Name|Host|Tomcat Port| 
     NS91|192.168.1.91|80
     NS68|192.168.1.68|8001
   */
  public String [][]  getControllerConfigInfo()
  {
    try
    {
      String data[][] = null;
      
      File fileobj = new File(controllerConfPath);
      
      if(!fileobj.exists())
      {
        Log.errorLog(className, "getControllerConfigInfo", "", "", "Config file ( "+  controllerConfPath+ ")does not exist.");
        return null ;
      }
      
      FileReader fr = new FileReader(fileobj);
      BufferedReader br = new BufferedReader(fr);
      String strLine = "";
      ArrayList<String [] > listData = new ArrayList<String[]>();
      
      while((strLine = br.readLine()) != null)
      {
        if(strLine.startsWith("#"))
          continue;
        String arrData []  = rptUtilsBean.split(strLine, "|");
        if(arrData.length >= 3)
        {
           listData.add(arrData);
        }
      }
      
      data = new String[listData.size()][3];
      
      for(int i = 0 ; i < listData.size() ; i++)
      {
        data[i] = listData.get(i);
      }
      
      return data;
    }
    catch (Exception e)
    {
      return null;
    }
  }
}

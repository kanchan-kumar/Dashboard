/*--------------------------------------------------------------------
@Name    : DomainController.java
@Author  :
@Purpose : This is class to maintain the controller information. Add, Delete controller

Notes:
   cav_controllers.conf file format
     ControllerId|ControllerName|owner|password|ApplianceName|UI_IP|UI_Port|ServiceEndPoint_IP|ServiceEndPoint_Port|hpd_IP_ports
     1|hpd1|netstorm|netstorm|MCom|123.12.10.35|82|123.1.1.18|80|IP=192.168.1.41;HPD_PORT=82;HPD_SPORT=444;
   cav_appliance.conf file format
     ApplianceName|AdminIP|AdminPort|UI_IP_range|UI_Port_range|ServiceEndpoint_IP_range|ServiceEndpoint_port_range
     MCOM|123.12.10.34|7891|123.12.10.35-60|80-100|123.1.1.18-50|80-100
     DCOM|123.12.10.34|7891|123.12.10.35,123.12.10.36|80-100|123.1.1.18,123.1.1.19,123.1.1.20|80-100
@Modification History:
  07/06/12:Jyoti - Initial Version
----------------------------------------------------------------------*/

package pac1.Bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.Collection;

public class DomainController
{
  private static String className = "DomainController";
  static String etcPath = "/etc/";
  private final static String CONF_EXT = ".conf";
  private final static String CAV_DOMAIN = "cav_domain_appliance" + CONF_EXT;
  private final static String CAV_CONTROLLERS = "cav_domain_controllers" + CONF_EXT;
  String[][] arrControllerTemp = null;
  int portCount  = 50;

  Vector vecDomainData = null;
  LinkedHashMap mapForPortWithDefaultValue = new LinkedHashMap();
  
  public DomainController(int portCount)
  {
    arrControllerTemp = getControllerInfo();
    vecDomainData = rptUtilsBean.readFileInVector(getDomainPath());
    setDefaultValueOfPorts();
    
    this.portCount = portCount;
  }

  // This will return domain path
  private static String getDomainPath()
  {
    String osname = System.getProperty("os.name").trim().toLowerCase();

    if(osname.startsWith("win"))
      return ("C:" + etcPath + CAV_DOMAIN);
    else
      return (etcPath + CAV_DOMAIN);
  }

  //This will return Controller path
  private static String getControllerPath()
  {
    String osname = System.getProperty("os.name").trim().toLowerCase();

    if(osname.startsWith("win"))
      return ("C:" + etcPath + CAV_CONTROLLERS);
    else
      return (etcPath + CAV_CONTROLLERS);
  }

  /**
   * Ports with default value
   */
  public void setDefaultValueOfPorts()
  {
    //mapForPortWithDefaultValue.put("TOMCAT_HTTP_PORT", "8000");
    //mapForPortWithDefaultValue.put("TOMCAT_SHUTDOWN_PORT", "8005");
    mapForPortWithDefaultValue.put("JAVA_SERVER_TCP_PORT", "7001");
    mapForPortWithDefaultValue.put("JAVA_SERVER_UDP_PORT", "7000");
    mapForPortWithDefaultValue.put("HPD_SPORT", "443");
    mapForPortWithDefaultValue.put("HPD_FTP_PORT", "21");
    mapForPortWithDefaultValue.put("HPD_SMTP_PORT", "25");
    mapForPortWithDefaultValue.put("HPD_POP3_PORT", "110");
    mapForPortWithDefaultValue.put("HPD_DNS_PORT", "53");
    mapForPortWithDefaultValue.put("PROXY_BASED_RECORDER_PORT", "7890");
  }
  
  public String[][] getDomainInfo()
  {
    Log.debugLog(className, "getDomainInfo", "", "", "Method called.");
    try
    {
      String arrDomainInfo[][] = null;

      Vector vecFileData = rptUtilsBean.readFileInVector(getDomainPath());
      if(vecFileData == null)
        return null;

      int rowCount = 0;
      int colCount = 10;
      if(vecFileData.size() > 1)
      {
        rowCount = vecFileData.size();
        colCount = rptUtilsBean.strToArrayData(vecFileData.elementAt(0).toString(), "|").length;
      }
      
      arrDomainInfo = new String[rowCount][colCount];
      for(int i = 0; i < vecFileData.size(); i++)
      {
        String[] arrRcrd = rptUtilsBean.strToArrayData(vecFileData.elementAt(i).toString(), "|");
        
        for(int ii = 0; ii < arrRcrd.length; ii++)
        {
          arrDomainInfo[i][ii] = arrRcrd[ii];
        }
      }
      return arrDomainInfo;
    }
    catch (Exception e) 
    {
      Log.stackTraceLog(className, "getDomainInfo", "", "", "Exception - ", e);
      return null;
    }
  }
  
/*
 * This function retrieve the information of controller
 * Data in file :- ControllerId|ControllerName|owner|password|ApplianceName|UI_IP|UI_Port|ServiceEndPoint_IP|ServiceEndPoint_Port|hpd_IP_ports
 * show in gui : -Controller Name, Owner, Url(UI_IP:UI_Port), Service End Point(ServiceEndPoint_IP/ServiceEndPoint_Port)
 *
 */
  public String[][] getControllerInfo()
  {
    Log.debugLog(className, "getControllerInfo", "", "", "Method called.");

    try
    {
      String arrControllerInfo[][] = null;

      String contollerPath = getControllerPath();
      Vector vecFileData = rptUtilsBean.readFileInVector(contollerPath);

      if(vecFileData == null)
        return null;

      int rowCount = 0;
      int colCount = 10;
      if(vecFileData.size() > 1)
      {
        rowCount = vecFileData.size();
        colCount = rptUtilsBean.strToArrayData(vecFileData.elementAt(0).toString(), "|").length;
      }
      
      arrControllerInfo = new String[rowCount][colCount];
      for(int i = 0; i < vecFileData.size(); i++)
      {
        String[] arrRcrd = rptUtilsBean.strToArrayData(vecFileData.elementAt(i).toString(), "|");

        arrControllerInfo[i][0] = arrRcrd[1]; //controller name
        arrControllerInfo[i][1] = arrRcrd[2]; //owner
        arrControllerInfo[i][2] = arrRcrd[4]; //appliance name
        arrControllerInfo[i][3] = "http://" + arrRcrd[5] + ":" + arrRcrd[6] ; //<UI_IP>:<UI_Port>
        arrControllerInfo[i][4] = arrRcrd[7]; //<ServiceEndPoint_IP>:<ServiceEndPoint_Port>
        arrControllerInfo[i][5] = arrRcrd[12]; //hpd Ip ports
        arrControllerInfo[i][6] = arrRcrd[5]; //UI_IP
        arrControllerInfo[i][7] = arrRcrd[6]; //UI_Port
        arrControllerInfo[i][8] = arrRcrd[7]; //ServiceEndPoint_IP
        arrControllerInfo[i][9] = arrRcrd[8]; //ServiceEndPoint_Port
        arrControllerInfo[i][10] = arrRcrd[0]; //Controller id
        arrControllerInfo[i][11] = arrRcrd[3]; //password
      }
      return arrControllerInfo;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getControllerInfo", "", "", "Exception - ", e);
      return null;
    }
  }

  /*
   * Handling following cases
      1) 22
      2) 22,33
      3) 22-33
      4) 22,33,44-45,66
      5) 22,44-22,22,21-23
      6) 22- ,88
      7) 22, - ,88
      8) If duplicate port exist it will remove from the list
   */  
  public Vector splitPortWithCommaDash(String port)
  {
    Vector vecData = new Vector(portCount);
    try
    {
      String strSeparator = "";

      //splitting with comma
      String arrCommaSplit[] = rptUtilsBean.split(port.trim(), ",");
        
      for(int i = 0; i < arrCommaSplit.length; i++)
      {
      //splitting with Dash
        String[] strSplitDash = rptUtilsBean.split(arrCommaSplit[i].trim(), "-"); 
          
        //If string -
        if(strSplitDash.length == 0)
          continue;
        int startIndex = Integer.parseInt(strSplitDash[0]);
          
        //If length greater than 1 means dash is exist
        if(strSplitDash.length > 1)
        {
          int endIndex = Integer.parseInt(strSplitDash[1]);
          
          //If range in reverse order means - 22-11 
          if(startIndex > endIndex)
          {
            int temp = startIndex;
            startIndex = endIndex;
            endIndex = temp;
          }
            
          for(int ii = startIndex; ii <= endIndex; ii++)
          {
            //checking duplicate value 
            //if port already exists it will not in the vector
            if(!vecData.contains(ii))
              vecData.add(ii);
            //if(vecData.size() > portCount)
              //break;
          }
        }
        else
        {
          //checking duplicate value 
          //if port already exists it will not in the vector
          if(!vecData.contains(startIndex))
            vecData.add(startIndex);
          //if(vecData.size() > portCount)
            //break;
        }
      }
      return vecData;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "splitPortWithCommaDash", "", "", "Exception - ", e);
      return vecData;
    }
  }

  public Vector splitIpWithCommaDash(String IpPort)
  {
    Vector vecData = new Vector();
    try
    {
      String strSeparator = "";

      int indexComma = -1;
      int indexDash = -1;

      if((indexComma = IpPort.indexOf(",")) > -1)
        strSeparator = ",";
      else if((indexDash = IpPort.indexOf("-")) > -1)
        strSeparator = "-";

      String arrTemp[] = rptUtilsBean.split(IpPort, strSeparator);

      if(indexDash != -1)
      {
        String IpStartIndex = arrTemp[0].substring(arrTemp[0].lastIndexOf(".") + 1);
        String IpStartValue = arrTemp[0].substring(0, arrTemp[0].lastIndexOf("."));

        String arrTempIp[] = rptUtilsBean.split(IpPort, strSeparator);
        for(int ii = Integer.parseInt(IpStartIndex.trim()); ii <= Integer.parseInt(arrTemp[1].trim()); ii++)
          vecData.add(IpStartValue + "." + ii);
      }
      else
      {
        for(int i = 0; i < arrTemp.length; i++)
        {
          if(arrTemp[i] != "")
            vecData.add(arrTemp[i]);
        }
      }
      return vecData;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "splitPortWithCommaDash", "", "", "Exception - ", e);
      return vecData;
    }
  }

  /*
   * This method get the Ip and port option 2 in main
   * Get the list of Ips from cav_appliance.conf file
   * return available Ip which are no added in the controller file
   *
   * Format of domain file
   * ApplianceName|AdminIP|AdminPort|UI_IP_range|UI_Port_range|ServiceEndpoint_IP_range|ServiceEndpoint_port_range
     MCOM|123.12.10.34|7891|123.12.10.35-60|80-100|123.1.1.18-50|80-100
     DCOM|123.12.10.34|7891|123.12.10.35,123.12.10.36|80-100|123.1.1.18,123.1.1.19,123.1.1.20|80-100
   */

  public String[][] getUIPORT()
  {
    Log.debugLog(className, "getUIPORT", "", "", "Method called.");

    try
    {
      Vector vecPort = new Vector();

      if(vecDomainData == null)
        return null;
      
      for(int i = 0; i < vecDomainData.size(); i++)
      {
        String[] arrRcrd = rptUtilsBean.strToArrayData(vecDomainData.elementAt(i).toString(), "|");
        Vector vecData = splitPortWithCommaDash(arrRcrd[4]);

        if(arrControllerTemp != null)
        for(int ii = 0; ii < arrControllerTemp.length; ii++)
        {
          if(vecData.contains(arrControllerTemp[ii][7].trim()))
            vecData.remove(arrControllerTemp[ii][7].trim());
        }

        for(int k = 0; k < vecData.size(); k++)
          vecPort.add(arrRcrd[0] + "|" + vecData.get(k).toString());
      }

      String arrPort[][] = new String[vecPort.size()][2];

      for(int i = 0; i < vecPort.size(); i++)
      {
        String[] arrRcrd = rptUtilsBean.strToArrayData(vecPort.elementAt(i).toString(), "|");
        arrPort[i][0] = arrRcrd[0];
        arrPort[i][1] = arrRcrd[1];
      }
      return arrPort;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getUIPORT", "", "", "Exception - ", e);
      return null;
    }
  }

  /*
   * This method get the Ip with appliance name option 4 in main
   * Get the list of Ips from cav_appliance.conf file
   * return available Ip which are no added in the controller file
   *
   * Format of domain file
   * ApplianceName|AdminIP|AdminPort|UI_IP_range|UI_Port_range|ServiceEndpoint_IP_range|ServiceEndpoint_port_range
     MCOM|123.12.10.34|7891|123.12.10.35-60|80-100|123.1.1.18-50|80-100
     DCOM|123.12.10.34|7891|123.12.10.35,123.12.10.36|80-100|123.1.1.18,123.1.1.19,123.1.1.20|80-100
   */

  public String[][] getUIIp()
  {
    Log.debugLog(className, "getUIIp", "", "", "Method called.");

    try
    {
      Vector vecPort = new Vector();

      if(vecDomainData == null)
        return null;
      
      for(int i = 0; i < vecDomainData.size(); i++)
      {
        String[] arrRcrd = rptUtilsBean.strToArrayData(vecDomainData.elementAt(i).toString(), "|");
        Vector vecData = splitIpWithCommaDash(arrRcrd[3]);

        /**if(arrControllerTemp != null)
        for(int ii = 0; ii < arrControllerTemp.length; ii++)
        {
          if(vecData.contains(arrControllerTemp[ii][6].trim()))
          {
            vecData.remove(arrControllerTemp[ii][6].trim());
          }
        }**/

        for(int k = 0; k < vecData.size(); k++)
          vecPort.add(arrRcrd[0] + "|" + vecData.get(k).toString());
      }

      String arrIp[][] = new String[vecPort.size()][2];

      for(int i = 0; i < vecPort.size(); i++)
      {
        String[] arrRcrd = rptUtilsBean.strToArrayData(vecPort.elementAt(i).toString(), "|");
        arrIp[i][0] = arrRcrd[0];
        arrIp[i][1] = arrRcrd[1];
      }
      return arrIp;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getUIIp", "", "", "Exception - ", e);
      return null;
    }
  }

  /*
   * This method get the Ip with appliance name option 5 in main
   * Get the list of Ips from cav_appliance.conf file
   * return available Ip which are no added in the controller file
   *
   * Format of domain file
   * ApplianceName|AdminIP|AdminPort|UI_IP_range|UI_Port_range|ServiceEndpoint_IP_range|ServiceEndpoint_port_range
     MCOM|123.12.10.34|7891|123.12.10.35-60|80-100|123.1.1.18-50|80-100
     DCOM|123.12.10.34|7891|123.12.10.35,123.12.10.36|80-100|123.1.1.18,123.1.1.19,123.1.1.20|80-100
   */

  public String[][] getServiceEndPointIp()
  {
    Log.debugLog(className, "getServiceEndPointIp", "", "", "Method called.");

    try
    {
      Vector vecPort = new Vector();

      if(vecDomainData == null)
        return null;
      
      for(int i = 0; i < vecDomainData.size(); i++)
      {
        String[] arrRcrd = rptUtilsBean.strToArrayData(vecDomainData.elementAt(i).toString(), "|");
        Vector vecData = splitIpWithCommaDash(arrRcrd[5]);
        
        /**if(arrControllerTemp != null)
        for(int ii = 0; ii < arrControllerTemp.length; ii++)
        {
          if(vecData.contains(arrControllerTemp[ii][8].trim()))
          {
            vecData.remove(arrControllerTemp[ii][8].trim());
          }
        }**/

        for(int k = 0; k < vecData.size(); k++)
          vecPort.add(arrRcrd[0] + "|" + vecData.get(k).toString());
      }

      String arrIp[][] = new String[vecPort.size()][2];

      for(int i = 0; i < vecPort.size(); i++)
      {
        String[] arrRcrd = rptUtilsBean.strToArrayData(vecPort.elementAt(i).toString(), "|");
        arrIp[i][0] = arrRcrd[0];
        arrIp[i][1] = arrRcrd[1];
      }
      return arrIp;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getServiceEndPointIp", "", "", "Exception - ", e);
      return null;
    }
  }


  /*
   * This method get the Ip with appliance name
   * Get the list of Ips from cav_appliance.conf file
   * return available Ip which are no added in the controller file
   *
   * Format of domain file
   * ApplianceName|AdminIP|AdminPort|UI_IP_range|UI_Port_range|ServiceEndpoint_IP_range|ServiceEndpoint_port_range
     MCOM|123.12.10.34|7891|123.12.10.35-60|80-100|123.1.1.18-50|80-100
     DCOM|123.12.10.34|7891|123.12.10.35,123.12.10.36|80-100|123.1.1.18,123.1.1.19,123.1.1.20|80-100
   */

  public String[][] getServiceEndPointPort()
  {
    Log.debugLog(className, "getServiceEndPointPort", "", "", "Method called.");

    try
    {
      Vector vecPort = new Vector();

      if(vecDomainData == null)
        return null;
      
      for(int i = 0; i < vecDomainData.size(); i++)
      {
        String[] arrRcrd = rptUtilsBean.strToArrayData(vecDomainData.elementAt(i).toString(), "|");
        Vector vecData = splitPortWithCommaDash(arrRcrd[6]);
        
        if(arrControllerTemp != null)
        for(int ii = 0; ii < arrControllerTemp.length; ii++)
        {
          if(vecData.contains(arrControllerTemp[ii][9].trim()))
          {
            vecData.remove(arrControllerTemp[ii][9].trim());
          }
        }

        for(int k = 0; k < vecData.size(); k++)
          vecPort.add(arrRcrd[0] + "|" + vecData.get(k).toString());
      }

      String arrIp[][] = new String[vecPort.size()][2];

      for(int i = 0; i < vecPort.size(); i++)
      {
        String[] arrRcrd = rptUtilsBean.strToArrayData(vecPort.elementAt(i).toString(), "|");
        arrIp[i][0] = arrRcrd[0];
        arrIp[i][1] = arrRcrd[1];
      }
      return arrIp;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getServiceEndPointIp", "", "", "Exception - ", e);
      return null;
    }
  }

  /*
   * This method get the Ip with appliance name
   * Get the list of Ips from cav_appliance.conf file
   * return available Ip which are no added in the controller file
   *
   * Format of domain file
   ApplianceName|AdminIP|AdminPort|UI_IP_range|UI_Port_range|ServiceEndpoint_IP_range|ServiceEndpoint_port_range|TomcatOtherPorts|JavaServerPorts|JavaClientPorts
   Dev41|192.168.1.41|7890|192.168.1.41|8005-8104|192.168.1.41|9001-9100|8201-8400|7001-7200|7000,7201-7400
   */

  public String[][] getJavaServerPort()
  {
    Log.debugLog(className, "getJavaServerPort", "", "", "Method called.");

    try
    {
      Vector vecPort = new Vector();

      if(vecDomainData == null)
        return null;
      
      for(int i = 0; i < vecDomainData.size(); i++)
      {
        String[] arrRcrd = rptUtilsBean.strToArrayData(vecDomainData.elementAt(i).toString(), "|");
        Vector vecData = splitPortWithCommaDash(arrRcrd[8]);
        
        for(int k = 0; k < vecData.size(); k++)
          vecPort.add(arrRcrd[0] + "|" + vecData.get(k).toString());
      }

      String arrIp[][] = new String[vecPort.size()][2];

      for(int i = 0; i < vecPort.size(); i++)
      {
        String[] arrRcrd = rptUtilsBean.strToArrayData(vecPort.elementAt(i).toString(), "|");
        arrIp[i][0] = arrRcrd[0];
        arrIp[i][1] = arrRcrd[1];
      }
      return arrIp;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getJavaServerPort", "", "", "Exception - ", e);
      return null;
    }
  }
 
  /*
   * This method get the Ip with appliance name
   * Get the list of Ips from cav_appliance.conf file
   * return available Ip which are no added in the controller file
   *
   * Format of domain file
   ApplianceName|AdminIP|AdminPort|UI_IP_range|UI_Port_range|ServiceEndpoint_IP_range|ServiceEndpoint_port_range|TomcatOtherPorts|JavaServerPorts|JavaClientPorts
   Dev41|192.168.1.41|7890|192.168.1.41|8005-8104|192.168.1.41|9001-9100|8201-8400|7001-7200|7000,7201-7400
   */

  public String[][] getJavaClientPort()
  {
    Log.debugLog(className, "getJavaServerPort", "", "", "Method called.");

    try
    {
      Vector vecPort = new Vector();

      if(vecDomainData == null)
        return null;
      
      for(int i = 0; i < vecDomainData.size(); i++)
      {
        String[] arrRcrd = rptUtilsBean.strToArrayData(vecDomainData.elementAt(i).toString(), "|");
        Vector vecData = splitPortWithCommaDash(arrRcrd[9]);
        
        for(int k = 0; k < vecData.size(); k++)
          vecPort.add(arrRcrd[0] + "|" + vecData.get(k).toString());
      }

      String arrIp[][] = new String[vecPort.size()][2];

      for(int i = 0; i < vecPort.size(); i++)
      {
        String[] arrRcrd = rptUtilsBean.strToArrayData(vecPort.elementAt(i).toString(), "|");
        arrIp[i][0] = arrRcrd[0];
        arrIp[i][1] = arrRcrd[1];
      }
      return arrIp;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getJavaServerPort", "", "", "Exception - ", e);
      return null;
    }
  }
  
  /*
   * This method get the Ip with appliance name
   * Get the list of Ips from cav_appliance.conf file
   * return available Ip which are no added in the controller file
   *
   * Format of domain file
   ApplianceName|AdminIP|AdminPort|UI_IP_range|UI_Port_range|ServiceEndpoint_IP_range|ServiceEndpoint_port_range|TomcatOtherPorts|JavaServerPorts|JavaClientPorts
   Dev41|192.168.1.41|7890|192.168.1.41|8005-8104|192.168.1.41|9001-9100|8201-8400|7001-7200|7000,7201-7400
   */

  public String[][] getRecoderPort()
  {
    Log.debugLog(className, "getRecoderPort", "", "", "Method called.");

    try
    {
      Vector vecPort = new Vector();

      if(vecDomainData == null)
        return null;

      for(int i = 0; i < vecDomainData.size(); i++)
      {
        String[] arrRcrd = rptUtilsBean.strToArrayData(vecDomainData.elementAt(i).toString(), "|");
        Vector vecData = splitPortWithCommaDash(arrRcrd[10]);

        for(int k = 0; k < vecData.size(); k++)
          vecPort.add(arrRcrd[0] + "|" + vecData.get(k).toString());
      }

      String arrIp[][] = new String[vecPort.size()][2];

      for(int i = 0; i < vecPort.size(); i++)
      {
        String[] arrRcrd = rptUtilsBean.strToArrayData(vecPort.elementAt(i).toString(), "|");
        arrIp[i][0] = arrRcrd[0];
       arrIp[i][1] = arrRcrd[1];
      }
      return arrIp;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getRecoderPort", "", "", "Exception - ", e);
      return null;
    }
  }
        
  /*
   * This method get the Ip with appliance name
   * Get the list of Ips from cav_appliance.conf file
   * return available Ip which are no added in the controller file
   *
   * Format of domain file
   ApplianceName|AdminIP|AdminPort|UI_IP_range|UI_Port_range|ServiceEndpoint_IP_range|ServiceEndpoint_port_range|TomcatOtherPorts|JavaServerPorts|JavaClientPorts
   Dev41|192.168.1.41|7890|192.168.1.41|8005-8104|192.168.1.41|9001-9100|8201-8400|7001-7200|7000,7201-7400
   */

  public String[][] getTomacatOtherPort()
  {
    Log.debugLog(className, "getJavaServerPort", "", "", "Method called.");

    try
    {
      Vector vecPort = new Vector();

      if(vecDomainData == null)
        return null;
      
      for(int i = 0; i < vecDomainData.size(); i++)
      {
        String[] arrRcrd = rptUtilsBean.strToArrayData(vecDomainData.elementAt(i).toString(), "|");
        Vector vecData = splitPortWithCommaDash(arrRcrd[7]);
        
        for(int k = 0; k < vecData.size(); k++)
          vecPort.add(arrRcrd[0] + "|" + vecData.get(k).toString());
      }

      String arrIp[][] = new String[vecPort.size()][2];

      for(int i = 0; i < vecPort.size(); i++)
      {
        String[] arrRcrd = rptUtilsBean.strToArrayData(vecPort.elementAt(i).toString(), "|");
        arrIp[i][0] = arrRcrd[0];
        arrIp[i][1] = arrRcrd[1];
      }
      return arrIp;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getJavaServerPort", "", "", "Exception - ", e);
      return null;
    }
  }
  
  //This method is used to get all port list assigned to the specified controller
  //This is used on Available Controllers screen when will double click on any controller then we need to show the port names below
  public ArrayList getAllUsedEndPointPortsByControllerName(String controllerName)
  {
    Log.debugLog(className, "getAllUsedEndPointPortsByControllerName", "", "", "Method called.");

    try
    {
      ArrayList allUsedEndPointPorts = new ArrayList();

      if(arrControllerTemp == null)
        return null;
      
      for(int i = 0; i < arrControllerTemp.length; i++)
      {
        if(arrControllerTemp[i][0].equals(controllerName))
        {
          String endPointPorts = arrControllerTemp[i][5];
          String[] arrRcrd = rptUtilsBean.strToArrayData(endPointPorts, ";");
          for(int j = 0; j < arrRcrd.length; j++)
          {
            String[] arrTemp = rptUtilsBean.strToArrayData(arrRcrd[j].trim(), "=");
            if(!arrTemp[0].equals("TOMCAT_IP") && !arrTemp[0].equals("HPD_SERVER_ADDRESS"))
            {
              allUsedEndPointPorts.add(arrTemp);
            }
         }
         break;
       }
    }
      return allUsedEndPointPorts;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getServiceEndPointIp", "", "", "Exception - ", e);
      return null;
    }
  }

  public ArrayList addContoller(String dataInputLine)
  {
    Log.debugLog(className, "addContoller", "", "", "Method called. dataInputLine = " + dataInputLine);
    ArrayList outPut = new ArrayList();
    Vector vecCmdOutPut = new Vector();

    try
    {
      String[] arrRcrd = rptUtilsBean.strToArrayData(dataInputLine, "|");

      ArrayList userExist = isOwnerIsAlreadyExit(arrRcrd[2], ""); //to check if owner is used by other controller or not.

      String cmdName = "nsu_manage_controller";
      String cmdArgs = "";
      if(userExist.size() <= 0)
        cmdArgs = "-o add -n " + arrRcrd[1] + " -u " + arrRcrd[2] + " -p " + arrRcrd[3] + " -P \"" + arrRcrd[12] + "\"";
      else
        cmdArgs = "-o add -n " + arrRcrd[1] + " -u " + arrRcrd[2] + " -P \"" + arrRcrd[12] + "\"";

      Log.debugLog(className, "addContoller", "", "", "cmdArgs = " + cmdArgs);

      CmdExec objCmdExec = new CmdExec();

      boolean result = true;
      
      vecCmdOutPut = objCmdExec.getResultByCommand(cmdName, cmdArgs, CmdExec.SYSTEM_CMD, null, "root");

      if((vecCmdOutPut.size() > 0) && ((String)vecCmdOutPut.lastElement()).startsWith("ERROR"))
      {
        result = false;
      }
      
      if(!result)
      {
        if(vecCmdOutPut.size()>0)
        {
          Log.debugLog(className, "addContoller", "", "", "nsu_manage_controller failed - " + vecCmdOutPut.get(0).toString());
        }
        else
        {
          Log.debugLog(className, "addContoller", "", "", "nsu_manage_controller failed - no error recieved from server");
        }
        outPut.add("Error");
        String errMsg = "";
        for(int k = 0; k < (vecCmdOutPut.size() -1); k++)
          errMsg = errMsg + "\\n" + vecCmdOutPut.get(k).toString(); 
        
        outPut.add(errMsg);
        return outPut;
      }

      Vector vecFileData = rptUtilsBean.readFileInVector(getControllerPath());

      File controllerFileObj = new File(getControllerPath());

      controllerFileObj.delete(); // Delete controller file
      controllerFileObj.createNewFile(); // Create new controller file

      if(!controllerFileObj.exists())
      {
        Log.errorLog(className, "addContoller", "", "", "Controller file does not exist. Controller filename is - " + controllerFileObj);
        outPut.add("Error");
        vecCmdOutPut = new Vector();
        vecCmdOutPut.add("Unable to add in Controller file - " + getControllerPath() + ". But Controller is created in backend");
        
        String errMsg = "";
        for(int k = 0; k < vecCmdOutPut.size(); k++)
          errMsg = errMsg + "\\n" + vecCmdOutPut.get(k).toString(); 
        outPut.add(errMsg);
        
        outPut.add(vecCmdOutPut);
        return outPut;
      }

      FileOutputStream fout = new FileOutputStream(controllerFileObj, true);  // append mode
      PrintStream pw = new PrintStream(fout);

      String dataLine = "ControllerId|ControllerName|owner|password|ApplianceName|UI_IP|UI_Port|ServiceEndPoint_IP|ServiceEndPoint_Port|future1|future2|future3|hpd_IP_ports";

      if(vecFileData != null)
      for(int i = 0; i < vecFileData.size(); i++)
      {
        dataLine = vecFileData.get(i).toString();

        pw.println(dataLine);
      }

      if(vecFileData == null || vecFileData.size() < 1)
      {
        pw.println(dataLine);
        pw.println(dataInputLine);
      }
      else
      {
        //String temp = vecFileData.get(vecFileData.size() - 1).toString().substring(0, vecFileData.get(vecFileData.size() - 1).toString().indexOf("|"));
        pw.println(dataInputLine); // Append the new controller to the end of file
      }

      pw.close();
      fout.close();
      outPut.add("Success");
      return outPut;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "addContoller", "", "", "Exception - ", e);
      outPut.add("Error");
      vecCmdOutPut = new Vector();
      vecCmdOutPut.add("Exception: " + e);
      outPut.add(vecCmdOutPut);
      return outPut;
    }
  }

  //Method to delete the controller. First it will delete the controller phisically from server then it will delete
  //from cm_controller.conf file.
  public ArrayList deleteContoller(String controllerName)
  {
    Log.debugLog(className, "deleteContoller", "", "", "Method called. controllerName = " + controllerName);
    ArrayList outPut = new ArrayList();
    Vector vecCmdOutPut = new Vector();

    try
    {
      String owner = "";
      
      if(arrControllerTemp == null)
        return outPut;
        
      for(int ij = 0; ij < arrControllerTemp.length; ij++)
      {
        if(arrControllerTemp[ij][0].equals(controllerName))
          owner =  arrControllerTemp[ij][1];
      }
      ArrayList deleteUser = isOwnerIsAlreadyExit(owner, controllerName); //to check if owner is used by other controller or not.

      String cmdName = "nsu_manage_controller";
      String cmdArgs = "-o delete -n " + controllerName;

      if(deleteUser.size() <= 0) //if deleteUser is blank means user is not used by other controller then add -u option to delete the user
        cmdArgs = cmdArgs + " -u " + owner;

      CmdExec objCmdExec = new CmdExec();
      boolean result = true;
      
      vecCmdOutPut = objCmdExec.getResultByCommand(cmdName, cmdArgs, CmdExec.SYSTEM_CMD, null, "root");

      if((vecCmdOutPut.size() > 0) && ((String)vecCmdOutPut.lastElement()).startsWith("ERROR"))
      {
        result = false;
      }
      
      if(!result)
      {
        Log.debugLog(className, "deleteContoller", "", "", "nsu_manage_controller failed");
        outPut.add("Error");
        
        String errMsg = "";
        for(int k = 0; k < (vecCmdOutPut.size() -1); k++)
          errMsg = errMsg + "\\n" + vecCmdOutPut.get(k).toString(); 
        outPut.add(errMsg);
        
        return outPut;
      }

      Vector vecFileData = rptUtilsBean.readFileInVector(getControllerPath());

      File controllerFileObj = new File(getControllerPath());

      controllerFileObj.delete(); // Delete controller file
      controllerFileObj.createNewFile(); // Create new controller file

      if(!controllerFileObj.exists())
      {
        Log.errorLog(className, "deleteContoller", "", "", "Controller file does not exist. Controller filename is - " + controllerFileObj);
        outPut.add("Error");
        vecCmdOutPut = new Vector();
        vecCmdOutPut.add("Unable to delete from Controller file - " + getControllerPath() + ". But Controller is delete from backend");
        outPut.add(vecCmdOutPut);
        return outPut;
      }

      FileOutputStream fout = new FileOutputStream(controllerFileObj, true);  // append mode
      PrintStream pw = new PrintStream(fout);

      //String dataLine = "ControllerId|ControllerName|owner|password|ApplianceName|UI_IP|UI_Port|ServiceEndPoint_IP|ServiceEndPoint_Port|future1|future2|future3|hpd_IP_ports";

      for(int i = 0; i < vecFileData.size(); i++)
      {
        String dataLine = vecFileData.get(i).toString();
        String[] arrRcrd = rptUtilsBean.strToArrayData(dataLine, "|");
        if(i == 0 || !arrRcrd[1].equals(controllerName))
          pw.println(dataLine);
      }

      pw.close();
      fout.close();
      outPut.add("Success");
      return outPut;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "deleteContoller", "", "", "Exception - ", e);
      outPut.add("Error");
      vecCmdOutPut = new Vector();
      vecCmdOutPut.add("Exception: " + e);
      outPut.add(vecCmdOutPut);
      return outPut;
    }
  }

  //Method will check if user is used in any controller.
  //ignoreController will user to make sure user exist other then this controller name. this will used when we are deleting controller then
  //if user is not used other then that controller we need to delete the user also
  public ArrayList isOwnerIsAlreadyExit(String user, String ignoreController)
  {
    Log.debugLog(className, "isOwnerIsAlreadyExit", "", "", "Method called.");
    ArrayList ownerDetails = new ArrayList();
    try
    {
      if(arrControllerTemp == null)
        return ownerDetails;
      
      for(int ij = 0; ij < arrControllerTemp.length; ij++)
      {
        if(ignoreController.equals(""))
        {
          if(arrControllerTemp[ij][1].equals(user))
          {
            ownerDetails.add(arrControllerTemp[ij][11]);
            ownerDetails.add("Appliance: " + arrControllerTemp[ij][2] + " Controller: " + arrControllerTemp[ij][0]);
            return ownerDetails;
          }
        }
        else
        {
          if(arrControllerTemp[ij][1].equals(user) && !arrControllerTemp[ij][0].equals(ignoreController))
          {
            ownerDetails.add(arrControllerTemp[ij][11]);
            ownerDetails.add("Appliance: " + arrControllerTemp[ij][2] + " Controller: " + arrControllerTemp[ij][0]);
            return ownerDetails;
          }
        }
      }
      return ownerDetails;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "isOwnerIsAlreadyExit", "", "", "Exception - ", e);
      ownerDetails.add("Error");
      return ownerDetails;
    }
  }

  //Method to get new controller id
  //we will take last controller id and increase by 1
  public int generateControllerId()
  {
    Log.debugLog(className, "generateControllerId", "", "", "Method called.");
    try
    {
      if(arrControllerTemp == null || arrControllerTemp.length <= 0)
        return 1;
      else
      {
        ArrayList<Integer> arrControllId = new ArrayList<Integer>();
        
        for(int i = 1; i < arrControllerTemp.length; i++)
        {
          arrControllId.add(Integer.parseInt(arrControllerTemp[i][10]));
        }
        
        Collections.sort(arrControllId);//sort the list
        
        String controllerId = "" + arrControllId.get(arrControllId.size() - 1);
        Log.debugLog(className, "generateControllerId", "", "", "Last Controller Id = " + controllerId);
        
        if(controllerId.equals(""))
          return 1;
        else
          return Integer.parseInt(controllerId) + 1;
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "generateControllerId", "", "", "Exception - ", e);
      return 1;
    }
  }

  public ArrayList getAllApplianceName()
  {
    Log.debugLog(className, "getAllApplianceName", "", "", "Method called.");
    ArrayList allApplianceName = new ArrayList();

    try
    {
      if(vecDomainData != null)
      for(int i = 1; i < vecDomainData.size(); i++)
      {
        String[] arrRcrd = rptUtilsBean.strToArrayData(vecDomainData.elementAt(i).toString(), "|");
        if(!allApplianceName.contains(arrRcrd[0]));
          allApplianceName.add(arrRcrd[0]);
      }
      return allApplianceName;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getAllApplianceName", "", "", "Exception - ", e);
      return allApplianceName;
    }
  }

  /*
   * This function chech appliance is admin or not 
   * if cav_applinace.conf exist in etc it returns true means admin
   */
  public boolean isAdminDomainAppliance()
  {
    Log.debugLog(className, "isAdminDomainAppliance", "", "", "Method called.");
    try
    {
      File domainFileObj = new File(getDomainPath());

      String osname = System.getProperty("os.name").trim().toLowerCase();
      String workPath = "/home/netstorm/work";
      if(osname.startsWith("win"))
        workPath = "C:/home/netstorm/work";
      
      if(domainFileObj.exists())
      {
        Log.debugLog(className, "isAdminDomainAppliance", "", "", "Config.getWorkPath() = " + Config.getWorkPath() + ", workPath = " + workPath);
        if(Config.getWorkPath().equals(workPath))
          return true;
        else
          return false;
      }
      return false;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "isAdminDomainAppliance", "", "", "Exception - ", e);
      return false;
    }
  }

  /**
   * This function check selected appliance in contollers using UI/Sercive Ip
   * @param applianceName
   * @param portName
   * @return
   */
  public Vector getUsedUIPort(String applianceName, String portName)
  {
    Log.debugLog(className, "getUsedUIPort", "", "", "Method called. applianceName = " + applianceName + ", portName = " + portName);
    try
    {
      Vector vecUsedPort = new Vector();
      
      //get controller Info
      String arrController[][] = getControllerInfo();
      
      if(arrController != null)
      {
        for(int i = 0; i < arrController.length; i++)
        {
          //check availability appliance name
          if(arrController[i][2].equals(applianceName))
          {
            int index = -1;
            String strTemp = "";
            
            //arrController[i][5] = IP=192.168.1.41;HPD_PORT=82;HPD_SPORT=445;
            //portName = HPD_PORT
            //Port name may be HPD_PORT_SL, so we search with =
            
            String arrTempSemiColon[] = rptUtilsBean.split(arrController[i][5], ";");
            
            for(int ii = 0; ii < arrTempSemiColon.length; ii++)
            {
              String arrTemmpEqual[] = rptUtilsBean.split(arrTempSemiColon[ii], "=");
              Vector vecPortTemp = splitPortWithCommaDash(arrTemmpEqual[1]);

              for(int kk = 0; kk < vecPortTemp.size(); kk++)
                vecUsedPort.add(vecPortTemp.get(kk).toString());
            }
            
            
            /**if((index = arrController[i][5].indexOf(portName + "=")) > -1)
            {
              //strTemp = 82;HPD_SPORT=445;
              strTemp = arrController[i][5].substring(index + portName.length() + 1);
              
              if((index = strTemp.indexOf(";")) > -1)
              {
                //82
                strTemp = strTemp.substring(0, index);
                vecUsedPort.add(strTemp);
              }
              else //strTemp = 82
              {
                vecUsedPort.add(strTemp);
              }
            }**/
          }
        }
      }
      return vecUsedPort;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getUsedUIPort", "", "", "Exception - ", e);
      return null;
    }
  }
  
  
  /*
   * get available ports for specific port name
   */
  public Vector getAvailableUIPorts(String applianceName, String portName, String UIServicePort)
  {
    Log.debugLog(className, "getAvailableUIPorts", "", "", "Method called.");
    
    try
    {
      String[] arrAvailablePortList = null;
      //get All UI Port
      
      String arrUIPort[][] = null;
      
      if(UIServicePort.equals("UI"))
        arrUIPort = getUIPORT();
      else if(UIServicePort.equals("JAVA_SERVER"))
        arrUIPort = getJavaServerPort();
      else if(UIServicePort.equals("JAVA_CLIENT"))
        arrUIPort = getJavaClientPort();
      else if(UIServicePort.equals("RECORDER_PORT"))
        arrUIPort = getRecoderPort();
      else if(UIServicePort.equals("OTHER_PORT"))
        arrUIPort = getTomacatOtherPort();        
      else
        arrUIPort = getServiceEndPointPort();
      Vector vecPort = new Vector();
      
      //getting all UI ports for specific appliance 
      if(arrUIPort == null)
        return null;
        
      for(int i = 0; i < arrUIPort.length; i++)
      {
        if(arrUIPort[i][0].equals(applianceName))
          vecPort.add(arrUIPort[i][1]); //ports
      }
      
      //getting used port
      Vector vecUsedPort = getUsedUIPort(applianceName, portName);
      
      String portWithDefaultValue = "";
      if(vecUsedPort != null)
      {
        Log.debugLog(className, "getAvailableUIPorts", "", "", "Used port in Appliance name = " + vecUsedPort.toString());
        
        //checking default value contain or not
        if((mapForPortWithDefaultValue.get(portName) != null) && (!vecUsedPort.contains(mapForPortWithDefaultValue.get(portName).toString())))
          portWithDefaultValue = mapForPortWithDefaultValue.get(portName).toString();
        
        for(int i = 0; i < vecUsedPort.size(); i++)
        {
          //If used port exist in the available port it remove remove from actual list
          if(vecPort.contains(vecUsedPort.get(i).toString()))
            vecPort.remove(vecUsedPort.get(i).toString());
        }
      }

      //set value on first index
      if(!vecPort.contains(portWithDefaultValue) && (!portWithDefaultValue.equals("")))
        vecPort.insertElementAt(portWithDefaultValue, 0);
      
      /**arrAvailablePortList = new String[vecPort.size()];
      //Is no ports available it returns blank list
      for(int ii = 0; ii < vecPort.size(); ii++)
        arrAvailablePortList[ii] = vecPort.get(ii).toString();**/
      
      //Last port is used by tomcat shut down so removing from list
      //if(UIServicePort.equals("UI"))
        //vecPort.remove(vecPort.size() - 1);
      
      if(vecPort.size() > portCount)
        vecPort.setSize(portCount);
      
      return vecPort;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getAvailableUIPorts", "", "", "Exception - ", e);
      return null;
    }
  }
  
/*
 * In tomcat other ports:
 * last port of available ports is used for TOMCAT_SHUTDOWN_PORT
 * Second last port of available ports is used for LPS_LISTEN_PORT
 */
  public String getUniquePortFromUI(String applianceName, String portName, String UIServicePort)
  {
    try
    {
      String uniquePort = getAvailableUIPorts(applianceName, portName, UIServicePort).lastElement().toString();
      
      if(portName.equals("LPS_LISTEN_PORT"))
      {  
    	 Vector uniquePortVec = getAvailableUIPorts(applianceName, portName, UIServicePort);
    	 uniquePort = uniquePortVec.get(uniquePortVec.size() - 2 ).toString();
      }	  
      return uniquePort;
    }
    catch (Exception e)
    {
      return "8005";
    }
  }
  
  
  
  public String getUniquePortFromUI(String applianceName, String portName)
  {
    try
    {
      String uniquePort = getAvailableUIPorts(applianceName, portName, "UI").lastElement().toString();
      return uniquePort;
    }
    catch (Exception e)
    {
      return "8005";
    }
  }

  public Vector splitWithSemiColonEqualCommaORDash(String strLine, String portLable)
  {
    Log.debugLog(className, "splitWithSemiColonEqualCommaORDash", "", "", "Method called. strLine = " +strLine + ", portLable = " + portLable);
    //arrController[i][5] = IP=192.168.1.41;HPD_PORT=82;HPD_SPORT=445-223;
    //portName = HPD_PORT
    //Port name may be HPD_PORT_SL, so we search with =

    Vector vecDaVector = new Vector();
    try
    {
      String arrTempSemiColon[] = rptUtilsBean.split(strLine, ";");

      for(int ii = 0; ii < arrTempSemiColon.length; ii++)
      {
        if(arrTempSemiColon[ii].startsWith(portLable))
        {
          String arrTemmpEqual[] = rptUtilsBean.split(arrTempSemiColon[ii], "=");
          vecDaVector = splitPortWithCommaDash(arrTemmpEqual[1]);
          break;
        }
      }
      return vecDaVector;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "splitWithSemiColonEqualCommaORDash", "", "", "Exception - ", e);
      return vecDaVector;
    }
  }

  public Vector getPortFromController(String applianceName, String contollerName, String portLable)
  {
    Log.debugLog(className, "getPortFromController", "", "", "Method called.");
    String availRecPort[] = null;
    Vector vecPort = new Vector();

    try
    {
      //getting all controller list
      String arrController[][] = getControllerInfo();


      if(arrController != null)
      {
        for(int i = 0; i < arrController.length; i++)
        {
          //check availability appliance name and controller name
          if((arrController[i][2].equals(applianceName)) && (arrController[i][0].equals(contollerName)))
          {
            //splitting string semi colon, equal and comma or dash
            vecPort = splitWithSemiColonEqualCommaORDash(arrController[i][5], portLable);
            break;
          }
        }
      }
      
      return vecPort;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getPortFromController", "", "", "Exception - ", e);
      return vecPort;
    }
  }
 
  /*
   * In this method we are getting controller id, controller name and appliance name
   */
  public String[] getApplianceAndContInfo()
  {
    Log.debugLog(className, "getApplianceAndContInfo", "", "", "Method called");
    
    String arrTemp[] = new String[3];
    
    String controllerName = Config.getWorkPath(); //get controller name from config path
    controllerName = controllerName.substring(controllerName.lastIndexOf("/") + 1);
    arrTemp[1] = controllerName;
       
    //on the basis of controller name we are getting 
    //appliance name and controller id from /etc/cav_domain_controller.conf
    if(arrControllerTemp != null)
      for(int i = 0; i < arrControllerTemp.length; i++)
      {
        if(arrControllerTemp[i][0].trim().equals(controllerName.trim()))
        {
          arrTemp[0] = arrControllerTemp[i][10];
          arrTemp[2] = arrControllerTemp[i][2];
          break;
        }
      }
    return arrTemp;
  }
  
  public static void main(String args[])
  {
    boolean resultFlag;
    int choice = 0;
    int portCount = 10;
    DomainController domainController = new DomainController(portCount);

    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    System.out.println("********Please enter the option for desired operation*********");
    System.out.println("Get Controller information. Enter : 1");
    System.out.println("Get Available Port. Enter : 2");
    System.out.println("Get Test function. Enter : 3");
    System.out.println("Get Available UP Ip. Enter : 4");
    System.out.println("Get Available Service Ip. Enter : 5");
    System.out.println("Get Available Service port. Enter : 6");
    System.out.println("Add contoller. Enter : 7");
    System.out.println("Get Contoller ID. Enter : 9");
    System.out.println("Get Domain Info. Enter : 10");
    System.out.println("Get Used Port Of UI. Enter : 11");
    System.out.println("Get Available Port for UI. Enter : 12");
    System.out.println("Get Unique Port from UI. Enter : 13");
    System.out.println("Get Available Tomcat ports. Enter : 14");
    System.out.println("Get Available ports in Controller. Enter : 15");
    System.out.println("Get Controller and applianceName. Enter : 16");

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
        String arrControllerTemp[][] = domainController.arrControllerTemp;

        if(arrControllerTemp != null)
        for(int i = 0; i < arrControllerTemp.length; i++)
        {
          for(int k = 0; k < arrControllerTemp[i].length; k++)
          {
            System.out.print(arrControllerTemp[i][k] + ", ");
          }
          System.out.println("\n");
        }
        break;

      case 2:
        String arrPortTemp[][] = domainController.getUIPORT();

        if(arrPortTemp != null)
        for(int i = 0; i < arrPortTemp.length; i++)
        {
          for(int k = 0; k < arrPortTemp[i].length; k++)
          {
            System.out.print(arrPortTemp[i][k] + ", ");
          }
          System.out.println("\n");
        }
        break;

      case 3:
       Vector vecSplitTemp = domainController.splitIpWithCommaDash("123.12.10.35-60");

        if(vecSplitTemp != null)
        for(int i = 0; i < vecSplitTemp.size(); i++)
        {
          System.out.println(vecSplitTemp.get(i).toString());
        }
        break;

      case 4:
        String arrIpTemp[][] = domainController.getUIIp();

        if(arrIpTemp != null)
        for(int i = 0; i < arrIpTemp.length; i++)
        {
          for(int k = 0; k < arrIpTemp[i].length; k++)
          {
            System.out.print(arrIpTemp[i][k] + ", ");
          }
          System.out.println("\n");
        }
        break;

      case 5:
        String arrServiceIpTemp[][] = domainController.getServiceEndPointIp();

        if(arrServiceIpTemp != null)
        for(int i = 0; i < arrServiceIpTemp.length; i++)
        {
          for(int k = 0; k < arrServiceIpTemp[i].length; k++)
          {
            System.out.print(arrServiceIpTemp[i][k] + ", ");
          }
          System.out.println("\n");
        }
        break;

      case 6:
        String arrServicePortTemp[][] = domainController.getServiceEndPointPort();

        if(arrServicePortTemp != null)
        for(int i = 0; i < arrServicePortTemp.length; i++)
        {
          for(int k = 0; k < arrServicePortTemp[i].length; k++)
          {
            System.out.print(arrServicePortTemp[i][k] + ", ");
          }
          System.out.println("\n");
        }
        break;

      case 7:
        String strTemp = "hpd33|netstorm|netstorm|MCom|123.12.10.35|82|123.1.1.18|80|IP=192.168.1.41;HPD_PORT=82;HPD_SPORT=444;";
        ArrayList flag = domainController.addContoller(strTemp);

        break;
    case 8:
      ArrayList allApplianceName = domainController.getAllApplianceName();
      for(int i = 0; i < allApplianceName.size(); i++)
      {
        System.out.print("ApplianceName =" + allApplianceName.get(i).toString() + "\n");
      }
    break;

    case 9:
      int aa = domainController.generateControllerId();
      System.out.print("New Controller Id = " + aa);
    break;

    case 10:
      String[][] arrDomainTemp = domainController.getDomainInfo();
     
      if(arrDomainTemp != null)
      for(int i = 0; i < arrDomainTemp.length; i++)
      {
        for(int k = 0; k < arrDomainTemp[i].length; k++)
        {
          System.out.print(arrDomainTemp[i][k] + ", ");
        }
        System.out.println("\n");
      }
    break;
  
    case 11:
      Vector vecUsedPort = domainController.getUsedUIPort("Appliance1", "HPD_PORT");
      for(int i = 0; i < vecUsedPort.size(); i++)
        System.out.println(vecUsedPort.get(i).toString());
     
    break;
 
    case 12:
      Vector vecAvailablePort = domainController.getAvailableUIPorts("Appliance1", "RECORDER_PORT", "RECORDER_PORT");

      if(vecAvailablePort != null)
      for(int i = 0; i < vecAvailablePort.size(); i++)
      {
        System.out.println(vecAvailablePort.get(i).toString());
      }
      System.out.println("---------------------------------" + vecAvailablePort.size());
    break;

    case 13:
      String uniquePort = domainController.getUniquePortFromUI("Appliance1", "JAVA_SERVER_UDP_PORT", "JAVA_CLIENT");
     
      System.out.println("uniquePort = " + uniquePort);
    break;
 
    case 14:
      String[][] arrServerPortTemp = domainController.getTomacatOtherPort();
     
      if(arrServerPortTemp != null)
      for(int i = 0; i < arrServerPortTemp.length; i++)
      {
        for(int k = 0; k < arrServerPortTemp[i].length; k++)
        {
          System.out.print(arrServerPortTemp[i][k] + ", ");
        }
        System.out.println("\n");
      }
    break;
    case 15:
      //Vector vecData = domainController.getPortFromController("Dev41", "Controller_2", "RECORDER_PORT");
      Vector vecData = domainController.getPortFromController("Appliance1", "work", "RECORDER_PORT");
      if(vecData != null)
      for(int i = 0; i < vecData.size(); i++)
      {
        System.out.println(vecData.get(i).toString());
      }
    break;
    
    case 16:
      String arrTemp[] = domainController.getApplianceAndContInfo();
      System.out.println("Controller ID = " + arrTemp[0] + ", Controller Name = " + arrTemp[1] + ", Appliance Name = " + arrTemp[2]);
    break;  
    
      default:
        System.out.println("Please select the correct option.");
    }
  }
}

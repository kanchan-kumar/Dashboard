/*--------------------------------------------------------------------
  @Name    : ServerSignatureInfo.java
  @Author  : Prabhat
  @Purpose : To retrive all the information, from server signatures in TRxxxx dir
  @Modification History:
    03/19/09 : Prabhat --> Initial Version


----------------------------------------------------------------------*/

package pac1.Bean;

import java.io.*;
import java.util.*;

import pac1.Bean.*;

// Class for ServerSignature Utility
public class ServerSignatureInfo
{
  private static String className = "ServerSignatureInfo";
  public final String SS_FILE_EXTN = ".ssf";

  private int numTestRun = -1;

  private ArrayList arrListServerSignature = null;

  public ServerSignatureInfo(int testRun)
  {
    this.numTestRun = testRun;
  }


  // This will return the server signatures path
  public String getServerSignaturesPath()
  {
    return (Config.getWorkPath() + "/webapps/logs/TR" + numTestRun + "/server_signatures/");
  }

  // This will return the server signatures path relative to logs
  public String getServerSignaturesPathRelative()
  {
    return ("/webapps/logs/TR" + numTestRun + "/server_signatures/");
  }

  // this willl initialize all server signature info
  public void initServerSignatureInfo()
  {
    Log.debugLog(className, "initServerSignatureInfo", "", "", "Start method");

    arrListServerSignature = loadServerSignature();
  }


  // load all Server Signature file in Array List
  private ArrayList loadServerSignature()
  {
    Log.debugLog(className, "loadServerSignature", "", "", "Start method");

    ArrayList arrListTemp = new ArrayList();  // Names of all Server Signature files

    File signatureObj = new File(getServerSignaturesPath());

    try
    {
      if(!signatureObj.exists())
        return null;

      String arrayFiles[] = signatureObj.list();

      // get all server signature file
      for(int j = 0; j < arrayFiles.length; j++)
      {
        // Use lastIndexOf() function instead indexOf() beacuse reading extn.
        if(arrayFiles[j].lastIndexOf(SS_FILE_EXTN) == -1)  // Skip non ssf files
          continue;

        // this is to remove server signature file extension
        // for remove profile file extension
        String ssNameOnly = arrayFiles[j].substring(0, arrayFiles[j].lastIndexOf("."));

        Log.debugLog(className, "loadServerSignature", "", "", "Adding Server Signature in ArrayList = " + ssNameOnly);
        arrListTemp.add(ssNameOnly);
      }

      Log.debugLog(className, "loadServerSignature", "", "", "Number of Server Signature files = " + arrListTemp.size());

      return arrListTemp;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "loadServerSignature", "", "", "Exception - ", e);
      return null;
    }
  }


  // return the all available server signature list
  public ArrayList getListServerSignature()
  {
    return arrListServerSignature;
  }


  // this function return true if server signature available in TR else false
  public boolean isServerSignatuerAvail(String strServerSignatureName)
  {
    Log.debugLog(className, "isServerSignatuerAvail", "", "", "Start method");

    try
    {
      boolean ssAvail = false;
      if((arrListServerSignature != null) && (arrListServerSignature.size() > 0))
      {
        for(int i = 0; i < arrListServerSignature.size(); i++)
        {
          String strTempSSName = arrListServerSignature.get(i).toString();

          if(strTempSSName.equals(strServerSignatureName))
          {
            ssAvail = true;
            Log.debugLog(className, "isServerSignatuerAvail", "", "", "Server signature = " + strServerSignatureName + " is found in the TestRun = " + numTestRun);

            break;
          }
        }
      }

      return ssAvail;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "isServerSignatuerAvail", "", "", "Exception - ", e);
      return false;
    }
  }

  // Methods for reading the File
  private static Vector readReport(String fileWithPath)
  {
    Log.debugLog(className, "readReport", "", "", "Method called. FIle Name = " + fileWithPath);

    try
    {
      Vector vecReport = new Vector();
      String strLine;

      File fileObj = new File(fileWithPath);

      if(!fileObj.exists())
      {
        Log.errorLog(className, "readReport", "", "", "Server Signature file not found in the Test Run");
        return null;
      }

      FileInputStream fis = new FileInputStream(fileWithPath);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));

      while((strLine = br.readLine()) !=  null)
      {
        strLine = strLine.trim();
        if(strLine.startsWith("#"))
          continue;
        if(strLine.length() == 0)
          continue;
        Log.debugLog(className, "readReport", "", "", "Adding line in vector. Line = " + strLine);
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


  /**
   * 
   * This function reads data from Server Signature file 
   * and return it in form of String
   * 
   * It also takes into consideration a Max Content Limit config Param
   * and returns that much to data
   * 
   * @param strServerSignaturePath
   * @return
   */
  public static String getDataToShowInRpt(String strServerSignaturePath)
  {
    Log.debugLog(className, "getDataToShowInRpt", "", "", "Start method. File name = " + strServerSignaturePath);

    try
    {
      Vector vecData = readReport(strServerSignaturePath);

      String strDataVal = "NA";
      
      // Deducting 4 chars for space and 3 dots at the end
      int maxSSContents = (Integer.parseInt(Config.getValue("SS_MAX_CONTENTS"))) - 4;
      
      Log.debugLog(className, "getDataToShowInRpt", "", "", "maxSSContents = " + maxSSContents);

      if((vecData != null) && (vecData.size() > 0))
      {
        for(int i = 0; i < vecData.size(); i++)
        {
          if(i == 0)
            strDataVal = vecData.get(i).toString();
          else
            strDataVal = strDataVal + "NEW_LINE_%09" + vecData.get(i).toString();
        }
      }

      if(vecData.size() == 0)
      {
        strDataVal = "Empty SS";

        Log.debugLog(className, "getDataToShowInRpt", "", "", "Server Signature name = " + strServerSignaturePath + " file is empty.");
      }

      // replacing pipe symbol with encoded symbol
      strDataVal = strDataVal.replaceAll("\\|", "PIPE_REPLACED_%7C");
      
      // Adding 3 dots if SS file size is bigger than maximum value
      if(strDataVal.length() >= maxSSContents)
      {
    	  strDataVal = strDataVal.substring(0, maxSSContents);
    	  strDataVal = strDataVal + " ...";
      }

      Log.debugLog(className, "getDataToShowInRpt", "", "", "Server Signature name = " + strServerSignaturePath + ", & data = " + strDataVal);
      
      return strDataVal;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getDataToShowInRpt", "", "", "Exception - ", e);
      return "NA";
    }
  }
}

//Name    : HPDConfigurations.java
//Author  :
//Purpose : Utility Bean for NetOcean GUI
//Modification History:
//03/30/10 Arun Goel: Initial Version
////////////////////////////////////////////////////////////////////

package pac1.Bean;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Vector;

public class HPDConfigurations
{
  private static String className = "hpdConfiguration";
  private String hpdPath = "";

  private final String FOLDER = "conf";
  private final String HPD_FILE = "hpd";
  private final String HPD_FILE_EXTN = ".conf";
  private static final String[] arrAvailableKeyword = new String[]{"HPD_ERROR", "MAX_DEBUG_LOG_FILE_SIZE", "MAX_ERROR_LOG_FILE_SIZE", "MODULEMASK", "HPD_DEBUG", "KEEP_ALIVE_TIMEOUT", "FIRST_REQ_TIMEOUT", "OPTIMIZE_ETHER_FLOW", "MAX_CON_PER_CHILD", "NUM_PROCESS", "WAN_ENV", "WAN_SHARED_MODEM", "WAN_ADVERSE_FACTOR", "WAN_JITTER", "WAN_LATENCY", "WAN_PKT_LOSS", "WAN_BANDWIDTH", "SVC_TIME", "SVC_CGI_TIME", "HPD_PORT", "HPD_SPORT", "HPD6_PORT", "HPD6_SPORT", "ENABLE_PIPELINING", "DirectoryIndex", "HPD_DEBUG_MASK", "HPD_MEM_DEBUG", "TIME_STAMP", "START_UP_MODE", "SSL_CLIENT_AUTHENTICATION", "SSLCertificateChainFile", "SSLCACertificateFile", "SSLCARevocationFile", "SSLVerifyDepth", "CONTENT_LENGTH_INDICATOR", "HTTP_LOG","HPD_SERVER_ADDRESS","HPD_DEBUG_TRACE","KEEP_ALIVE_TIMEOUT","ENABLE_URL_IGNORE_CASE" ,"ENABLE_RECORDING", "RECORDING_PARAMETERS", "ENABLE_TRAFFIC_STAT" , "DEFAULT_SERVICE_TEMPLATE_MODE"};
  CorrelationService correlationService_obj;

  //Constuctor
  public HPDConfigurations()
  {
    correlationService_obj = new CorrelationService();
    hpdPath = correlationService_obj.getHPDPath();
  }

  //getting hpd.conf file path
  public String getHpdConfFilePath()
  {
    return (hpdPath + FOLDER + "/" + HPD_FILE + HPD_FILE_EXTN);
  }

  // Open file and return as File object
  private File openFile(String fileName)
  {
    try
    {
      File tempFile = new File(fileName);
      return(tempFile);
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "openFile", "", "", "Exception - ", e);
      return null;
    }
  }


  // get total counts of related keywords in hpd files.
  public int getCountOfKeyword(String objectName)
  {
    Log.debugLog(className, "getCountOfKeyword", "", "", "Method started Keyword=" + objectName);
    int count = 0;

    try
    {
      String tempPath = getHpdConfFilePath();
      Vector completeFile = readFile(tempPath, false);

      if (completeFile == null)
        return count;

      for (int yy = 0; yy < completeFile.size(); yy++)
      {
        String line = completeFile.elementAt(yy).toString().trim();

        if (line.startsWith(objectName + " ")) // Method found
        {
          count++;
        }
      }
      return count;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return count;
    }
  }

//Methods for reading the File
  private Vector readFile(String fileWithPath, boolean ignoreComments)
  {
    Log.debugLog(className, "readFile", "", "", "Method called. FIle Name = " + fileWithPath);

    try
    {
      Vector vecFileData = new Vector();
      String strLine;

      File confFile = openFile(fileWithPath);

      if(!confFile.exists())
      {
        Log.errorLog(className, "readFile", "", "", "File not found, filename - " + fileWithPath);
        return null;
      }

      FileInputStream fis = new FileInputStream(fileWithPath);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));

      while((strLine = br.readLine()) !=  null)
      {
        strLine = strLine.trim();
        if(ignoreComments && strLine.startsWith("#"))
          continue;
        if(ignoreComments && strLine.length() == 0)
          continue;

        Log.debugLog(className, "readFile", "", "", "Adding line in vector. Line = " + strLine);
        vecFileData.add(strLine);
      }

      br.close();
      fis.close();

      return vecFileData;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "readFile", "", "", "Exception - ", e);
      return null;
    }
  }

  private boolean writeToFile(String fileWithPath, Vector vecModified)
  {
    Log.debugLog(className, "readFile", "", "", "Method called. FIle Name = " + fileWithPath);

    try
    {
      FileOutputStream out2 = new FileOutputStream(new File(fileWithPath));
      PrintStream requestFile = new PrintStream(out2);
      for(int ad = 0; ad < vecModified.size(); ad++)
        requestFile.println(vecModified.get(ad).toString());
      requestFile.close();
      out2.close();
      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "writeToFile", "", "", "Exception - ", e);
      return false;
    }
  }
  
  //Function to delete keyword from hpd configuration file.
  public boolean deleteKeywordFromHPDFile(String objectName)
  {
    Log.debugLog(className, "deleteKeywordFromHPDFile", "", "", "Method started Keyword= " + objectName);
    try
    {
      String tempPath = getHpdConfFilePath();
      Vector completeFile = readFile(tempPath, false);
      Vector modifiedVector = new Vector();

      if (completeFile == null)
        return false;

      for (int yy = 0; yy < completeFile.size(); yy++)
      {
        String line = completeFile.elementAt(yy).toString().trim();

        if (!line.startsWith(objectName + " ")) // Method found
        {
          modifiedVector.add(line);
        }
      }

      if (!writeToFile(tempPath, modifiedVector))
        return false;

      return true;
    }
    catch (Exception ex)
    {
      // ex.printStackTrace();
      Log.stackTraceLog(className, "deleteKeywordFromHPDFile", "", "", "Exception - ", ex);
      return false;
    }
  }


//Method to add value in conf file of any URL for different type of service objects.
  public boolean addKeyword(String objectName, String[] fieldsValue)
  {
    try
    {
      Log.debugLog(className, "addKeyword", "", "", "Method started Keyword=" + objectName);

      String strKeyword = "";
      for(int jj = 0; jj < arrAvailableKeyword.length; jj++)
      {
        if(arrAvailableKeyword[jj].equals(objectName))
          strKeyword = arrAvailableKeyword[jj];
      }

      if(strKeyword.equals(""))
        return false;

      String strToAdd = "";
      if(objectName.equals(strKeyword)) //request to add for Search var
      {
        strToAdd = objectName;
        for(int i = 0; i < fieldsValue.length; i++)
          strToAdd = strToAdd + " " + fieldsValue[i];

        if(fieldsValue.length < 0)
          strToAdd = "";
      }

      if(!strToAdd.equals(""))
        strToAdd = strToAdd + "\n";

      Log.debugLog(className, "addKeyword", "", "", "strToAdd=" + strToAdd);

      String tempPath = getHpdConfFilePath();

      FileWriter fstream = new FileWriter(tempPath, true);

      BufferedWriter out = new BufferedWriter(fstream);
      out.write(strToAdd);
      out.close();
      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "addKeyword", "", "", "Exception - ", ex);
      return false;
    }
  }

  /*Method to update or delete any service object.
  //@objectName - it will define that which type of service object need to update/delete
  //@operation - it can be 'delete' or 'update'
  //@rowsToUpdate - it is an interger array which will define the row indexs to which we need to update/delete
  //@fieldsValue - it is a string array which will define the new values to be updated, it will be null in case of delete*/
  public boolean updateKeywordFields(String objectName, String operation, int[] rowsToUpdate, String[] fieldsValue)
  {
    Log.debugLog(className, "updateKeywordFields", "", "", "Method started Keyword= " + objectName);
    try
    {
      String tempPath = getHpdConfFilePath();

      Vector completeFile = readFile(tempPath, false);
      Vector modifiedVector = new Vector();

      if(completeFile == null)
        return false;

      int objectCtr = -1;
      String strToUpdate = "";
      boolean keywordFound = false;

      String strKeyword = "";
      for(int jj = 0; jj < arrAvailableKeyword.length; jj++)
      {
        if(arrAvailableKeyword[jj].equals(objectName))
          strKeyword = arrAvailableKeyword[jj];
      }
      if(strKeyword.equals(""))
        return false;

      for(int yy = 0; yy < completeFile.size(); yy++)
      {
        String line = completeFile.elementAt(yy).toString().trim();

        if(line.startsWith(objectName + " ")) // Method found
        {
          objectCtr++;
          boolean matchToUpdate = false;
          for(int bb = 0; bb < rowsToUpdate.length; bb++)
          {
            if(rowsToUpdate[bb] == objectCtr)
            {
              matchToUpdate = true;
              if(operation.equals("update"))
              {
                keywordFound = true;
                strToUpdate = objectName;
                if(operation.equals("update"))
                  for(int jm = 0; jm < fieldsValue.length; jm++)
                    strToUpdate = strToUpdate + " " + fieldsValue[jm];

               if((fieldsValue.length == 0) || ( fieldsValue[0].equals("")))
                  modifiedVector.remove(strToUpdate);
               else
                  modifiedVector.add(strToUpdate);
              }
              if(operation.equals("delete"))
                modifiedVector.remove(objectName);
              break;
            }
          }
          if(!matchToUpdate)
            modifiedVector.add(line);
        }
        else
        {
          modifiedVector.add(line);
        }
      }
      if((!keywordFound) && (!operation.equals("delete")))
      {
        String temp = objectName;
        for(int jm = 0; jm < fieldsValue.length; jm++)
          temp = temp + " " + fieldsValue[jm];
        //System.out.print(" fieldsValue [0] " + fieldsValue[0] + "    " + fieldsValue.length);
        if((fieldsValue.length > 0) && (!fieldsValue[0].trim().equals("")))
          modifiedVector.add(temp);
      }
      if(!writeToFile(tempPath, modifiedVector))
        return false;

      return true;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "updateKeywordFields", "", "", "Exception - ", ex);
      return false;
    }
  }



  public String[] getKeywordFields(String objectName)
  {
    Log.debugLog(className, "getKeywordFields", "", "", "Method started Keyword=" + objectName);
    try
    {
      String arrFlds[] = {""};
      String tempPath = getHpdConfFilePath();

      Vector completeFile = readFile(tempPath, false);

      if(completeFile == null)
        return new String[]{""};

      for(int yy = 0; yy < completeFile.size(); yy++)
      {
        String line = completeFile.elementAt(yy).toString().trim();

        if(line.startsWith(objectName + " ")) // Method found
        {
          arrFlds = rptUtilsBean.strToArrayData(line, " ");
        }
      }
      return arrFlds;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getKeywordFields", "", "", "Exception - ", ex);
      return new String[]{""};
    }
  }

//Name: getKeywordFields
  // Purpose: get all fields
  // Arguments:
  //  Arg1: Keyword.
  //  Arg2,3,4,5,6,7: StringBuffer
  // Return: Integer
  /////////////////////////////////////////////////////////////////////

  public int getKeywordFields(String objectName, StringBuffer strFld1, StringBuffer strFld2, StringBuffer strFld3, StringBuffer strFld4, StringBuffer strFld5, StringBuffer strFld6)
  {
    Log.debugLog(className, "getKeywordFields", "", "", "Method called. Keyword = " + objectName);

    int numFld = 0;
    String arrFlds[] = {""};
    try
    {
      String tempPath = getHpdConfFilePath();

      Vector completeFile = readFile(tempPath, false);

      if(completeFile == null)
        return numFld;

      for(int yy = 0; yy < completeFile.size(); yy++)
      {
        String line = completeFile.elementAt(yy).toString().trim();

        if(line.startsWith(objectName + " ")) // Method found
        {
          arrFlds = rptUtilsBean.strToArrayData(line, " ");
        }
      }

      for(int indexValue = 0; indexValue < arrFlds.length; indexValue++)
      {
         if(indexValue == 1)
         {
           strFld1.setLength(0);
           strFld1.append(arrFlds[indexValue]);
         }
         else if((indexValue == 2)  && (strFld2.toString() != null))
         {
           strFld2.setLength(0);
           strFld2.append(arrFlds[indexValue]);
         }
         else if(indexValue == 3)
         {
           strFld3.setLength(0);
           strFld3 = strFld3.append(arrFlds[indexValue]);
         }
         else if(indexValue == 4)
         {
           strFld4.setLength(0);
           strFld4 = strFld4.append(arrFlds[indexValue]);
         }
         else if(indexValue == 5)
         {
           strFld5.setLength(0);
           strFld5 = strFld5.append(arrFlds[indexValue]);
         }
         else if(indexValue == 6)
         {
           strFld6.setLength(0);
           strFld6 = strFld6.append(arrFlds[indexValue]);
         }
      }
      return arrFlds.length;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getKeywordFields", "", "", "Exception - ", ex);
      return numFld;
    }
  }

  public String[][] copy2DArray(String[][] arrOldTemp, String[][] arrNewTemp)
  {
    Log.debugLog(className, "copy2DArray", "", "", "Method started");
    try
    {
      System.arraycopy(arrOldTemp, 0, arrNewTemp, 0, arrOldTemp.length);

      /*for(String[] strArr : arrNewTemp)
      {
        for (String str : strArr)
          System.out.println("XXX = " + str);
      }*/
      return arrNewTemp;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "copy2DArray", "", "", "Exception - ", ex);
      return null;
    }
  }

  public String[][] getVectorKeywordFields(String objectName)
  {
    Log.debugLog(className, "getVectorKeywordFields", "", "", "Method started Keyword=" + objectName);
    String[][] arrFlds = null;
    try
    {
      Vector vecForSplitStr = new Vector();

      String tempPath = getHpdConfFilePath();

      Vector completeFile = readFile(tempPath, false);

      if(completeFile == null)
        return new String[0][0];
      for(int yy = 0; yy < completeFile.size(); yy++)
      {
        String line = completeFile.elementAt(yy).toString().trim();

        if(line.startsWith(objectName + " ")) // Method found
        {
          vecForSplitStr.add(line);
        }
      }
      if(vecForSplitStr.size() > 0)
      {
        arrFlds = new String[vecForSplitStr.size()][vecForSplitStr.get(0).toString().split(" ").length];
        for(int k = 0; k < vecForSplitStr.size(); k++)
        {
          String temp = vecForSplitStr.elementAt(k).toString();

          String[] arrTemp = temp.split(" ");
          //arrFlds = new String[vecForSplitStr.size()][arrTemp.length];
          if(arrTemp.length > arrFlds[k].length)
          {
            Log.debugLog(className, "getVectorKeywordFields", "", "", "Length 0f new array=" + arrTemp.length);
            String arrNewTemp[][] = new String[vecForSplitStr.size()][arrTemp.length];
            arrFlds = copy2DArray(arrFlds, arrNewTemp);
          }

        arrFlds[k] = arrTemp;
/*        for(int kk = 0; kk < arrTemp.length; kk++)
        {
          arrFlds[k][kk] = arrTemp[kk].toString();
          //System.out.println(" arrFlds[k][kk]  " + arrFlds[k][kk]);
        }
*/      }
      }

      //System.out.println(" lenfth " + arrFlds.length);
       if(arrFlds == null)
         return new String[0][0];

      return arrFlds;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getVectorKeywordFields", "", "", "Exception - ", ex);
      return new String[0][0];
    }
  }

  public static String replaceSpecialCharacter(String s)
  {
    Log.debugLog(className, "replaceSpecialCharacter", "", "",  "Method Called. String = " + s);
    if((s == null) || (s.length() == 0))
      return s;
    StringBuffer sb = new StringBuffer();
    int n = s.length();
    for (int i = 0; i < n; i++)
    {
       char c = s.charAt(i);
       switch (c)
       {
          case '\\': sb.append("\\\\"); break;
          case '\'': sb.append("\\\'"); break;
          case '"': sb.append("\\\""); break;
          case '\n': sb.append("\\n"); break;
          case '\r': sb.append("\\\\r"); break;
          case '\t': sb.append("\\\\t"); break;
          case '\b': sb.append("\\\\b"); break;
          case '\f': sb.append("\\\\f"); break;
          case '&': sb.append("\\&"); break;

          default:  sb.append(c); break;
       }
    }
    Log.debugLog(className, "replaceSpecialCharacter", "", "",  "returning String = " + sb.toString());
    return sb.toString();
}

  /*************************************Main Method*******************************/

  public static void main(String[] args)
  {
    HPDConfigurations hpdConfiguration_obj = new HPDConfigurations();

    String hpdPath = hpdConfiguration_obj.getHpdConfFilePath();
    //System.out.println(" hpdPath  " + hpdPath);

    String[] arrData = {"HTTP", "POLL"};
    boolean bool2 = false;

    //StringBuffer strHealthMonitor = new StringBuffer("");

    //hpdConfiguration_obj.initMap();
    boolean bool = hpdConfiguration_obj.updateKeywordFields("MODULEMASK", "update", new int[]{0}, arrData);

    //int k = hpdConfiguration_obj.getKeywordFields("MODULEMASK", strHealthMonitor, strHealthMonitor, null, null, null, null);
    //arrData = hpdConfiguration_obj.getKeywordFields("MODULEMASK");
    //System.out.println(" strHealthMonitor " + arrData[0] + "  " + arrData[1]);

  }
}

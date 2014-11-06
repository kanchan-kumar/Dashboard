//Name    : NFUtils.java
//Author  : Ritesh Sharma
//Purpose : Utility Bean for NetOscean and NetFunction GUI
//Modification History:
//11/26/12 Ritesh Sharma: Initial Version

package pac1.Bean;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;
import java.util.HashMap;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NFUtils
{
  private static String className = "NFUtils";
  public final static int INDEX_DATA_SOURCE = 1; // for nsl_search_var
  CorrelationService corrServiceObj = new CorrelationService();
  public static String correlationPath = "";
  public static boolean  isWSDLServiceUsed = false;
  public NFUtils(){
    
    Config conf = new Config();
    String str = conf.getValue("netfunction.wsdl.service").trim();
    if(!str.equals(""))
    {
      if(str.equals("1"))
        isWSDLServiceUsed = true;
    }
    correlationPath = corrServiceObj.getCorrelationPath();
  }
  
  public boolean addIndexDataSource( int DataSourceType, String[] fieldsValue, StringBuffer fileContents)
  {
    try
    {
      Log.debugLog(className, "addIndexDataSource", "", "", "Method started");
      String strToAdd = "";
      if(DataSourceType == INDEX_DATA_SOURCE) // request to add for Search var
      {  
        String strVarValue = "";
        if(!fieldsValue[6].trim().equals(""))
          strVarValue = " VAR_VALUE=\"" + fieldsValue[6] + "\", ";
        
        strToAdd = "nsl_index_datasource( " + fieldsValue[0] + ", DatasetName=\"" + fieldsValue[1] + "\", FILE=\"" + fieldsValue[2] 
                   + "\", ColumnDelimiter=\"" + fieldsValue[3] + "\", FirstDataLine=\"" + fieldsValue[4] + "\", HeaderLine=\"" 
                   + fieldsValue[5] + "\", " + strVarValue + "PersistenceMode=\""+ fieldsValue[7] + "\" );"; 
      }

      Log.debugLog(className, "addIndexDataSource", "", "", "strToAdd=" + strToAdd);

      File fileCreateDirs = new File(correlationPath);
      if(!fileCreateDirs.mkdirs())
      {
        Log.debugLog(className, "addIndexDataSource", "", "", "Unable to create directory or it is already there. path");
      }

      File checkFile = new File(getCorrelationConfFilePath());
      if(!checkFile.exists())
        checkFile.createNewFile();

      FileWriter fstream = new FileWriter(getCorrelationConfFilePath(), true);
      BufferedWriter out = new BufferedWriter(fstream);
      out.write("\n" + strToAdd + "\n");
      out.close();

      if((DataSourceType == INDEX_DATA_SOURCE) && fileContents != null)
      {
        File fileFILE = null;
        if(fieldsValue[1].startsWith("/"))
          fileFILE = new File(fieldsValue[2]);
        else
          fileFILE = new File(correlationPath + "data/" + fieldsValue[2]);

        int lastFILEIndex = fieldsValue[2].lastIndexOf("/");
        String newFILEPath = "";
        if(lastFILEIndex > -1)
          newFILEPath = fieldsValue[2].substring(0, lastFILEIndex);
        if(newFILEPath.equals(""))
          newFILEPath = correlationPath + "data/";
       
        File fileFILECreateDirs = new File(newFILEPath);
        if(!fileFILECreateDirs.mkdirs())
        {
          Log.debugLog(className, "addIndexDataSource", "", "", "Unable to create directory path or it is already there. = " + newFILEPath);
        }
        
        if(!fileFILE.exists())
          fileFILE.createNewFile();

        FileOutputStream out1 = new FileOutputStream(fileFILE);
        PrintStream responseFile = new PrintStream(out1);
        responseFile.print(fileContents.toString().replaceAll("\\r", ""));
        responseFile.close();

      }
      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "addIndexDataSource", "", "", "Exception - ", ex);
      return false;
    }
  }
  
  public boolean updateIndexDataSource(int DataSourceType, String operation, int[] rowsToUpdate, String[] fieldsValue, StringBuffer fileContents, String oldFILEValue)
  {
    Log.debugLog(className, "updateIndexDataSource", "", "", "Method started,  DataSourceType=" + DataSourceType + ", operation=" + operation);
    try
    {
      Vector completeControlFile = corrServiceObj.readFile(getCorrelationConfFilePath(), false);
      Vector modifiedVector = new Vector();

      if(completeControlFile == null)
      {
        File fileCreateDirs = new File(correlationPath);
        if(!fileCreateDirs.mkdirs())
        {
          Log.debugLog(className, "updateIndexDataSource", "", "", "Unable to create directory or it is already there. path = " + correlationPath);
          // return false;
        }
        return true;
      }

      String strSearch = "";
      if(DataSourceType == INDEX_DATA_SOURCE)
        strSearch = "nsl_index_datasource";
     
      int objectCtr = -1;
      boolean serviceTimeFound = false;
      for(int yy = 0; yy < completeControlFile.size(); yy++)
      {
        String line = completeControlFile.elementAt(yy).toString();

        if(line.trim().startsWith(strSearch)) // Method found
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
                String strToUpdate = "";
                if(DataSourceType == INDEX_DATA_SOURCE) // request to add for Search var
                {
                  String strVarValue = "";
                  if(!fieldsValue[6].trim().equals(""))
                    strVarValue = " VAR_VALUE=\"" + fieldsValue[6] + "\", ";
                  
                  strToUpdate = "nsl_index_datasource( " + fieldsValue[0] + ", DatasetName=\"" + fieldsValue[1] + "\", FILE=\"" + fieldsValue[2] 
                             + "\", ColumnDelimiter=\"" + fieldsValue[3] + "\", FirstDataLine=\"" + fieldsValue[4] + "\", HeaderLine=\"" 
                             + fieldsValue[5] + "\", " + strVarValue + "PersistenceMode=\""+ fieldsValue[7] + "\" );"; 
                }
                modifiedVector.add(strToUpdate);
              }
              else
              {
                if(yy + 1 < completeControlFile.size())
                {
                  String blankline = completeControlFile.elementAt(yy + 1).toString().trim();
                  if(blankline.equals(""))
                  {
                    yy++;
                  }
                }
              }
              break;
            }
          }
          if(!matchToUpdate)
          {
            modifiedVector.add(line);
          }
        }
        else
          modifiedVector.add(line);
      }
     
      if(!corrServiceObj.writeToFile(getCorrelationConfFilePath(), modifiedVector, "", ""))
        return false;
      

      //code to create/update file contents for FILE argument used in 'File' and 'Index File' parameters
      if((DataSourceType == INDEX_DATA_SOURCE)&& fileContents != null)
      {
        if(!oldFILEValue.equals(""))
        {
          File oldFILE = new File(oldFILEValue);
          if(oldFILE.exists())
            oldFILE.delete();
        }

        File fileFILE = null;
        if(fieldsValue[2].startsWith("/"))
          fileFILE = new File(fieldsValue[2]);
        else
          fileFILE = new File(correlationPath + "data/" + fieldsValue[2]);

        int lastFILEIndex = fieldsValue[2].lastIndexOf("/");
        String newFILEPath = "";
        if(lastFILEIndex > -1)
          newFILEPath = fieldsValue[2].substring(0, lastFILEIndex);
        
        if(newFILEPath.equals(""))
          newFILEPath = correlationPath + "data/";
        
        File fileFILECreateDirs = new File(newFILEPath);
        if(!fileFILECreateDirs.mkdirs())
        {
          Log.debugLog(className, "updateIndexDataSource", "", "", "Unable to create directory path or it is already there. = " + newFILEPath);
        }
        if(!fileFILE.exists())
          fileFILE.createNewFile();

        FileOutputStream out1 = new FileOutputStream(fileFILE);
        PrintStream responseFile = new PrintStream(out1);
        responseFile.println(fileContents.toString().replaceAll("\\r", ""));
        responseFile.close();
      }

      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "updateIndexDataSource", "", "", "Exception - ", ex);
      return false;
    }
  }
  
  //Method to get Indexed data source details
  public String[][] getIndexDataSourceDetails(int sortOnCol, int sortPrefrence)
  {
    Log.debugLog(className, "getIndexDataSourceDetails", "", "", "Method started");
    try
    {
      ArrayList alIndexFileParameter = new ArrayList();

      Vector hpdURLData = corrServiceObj.readFile(getCorrelationConfFilePath(), true);

      if((hpdURLData == null) || (hpdURLData.size() <= 0))
      {
        Log.debugLog(className, "getIndexDataSourceDetails", "", "", "Service File not found, It may be corrupted. ");
        String[][] dummy = new String[0][8];
        return dummy;
      }

      int rowId = -1;
      String[][] arrIndexedDataSourceDetails = null;
      for(int k = 0; k < hpdURLData.size(); k++)
      {
        String line = hpdURLData.elementAt(k).toString();
        String temp = line.trim();

        if(temp.startsWith("nsl_index_datasource")) // Method found
        {
          int indexForClose = line.lastIndexOf(")");
          if((indexForClose == -1) || (indexForClose != line.length() - 2))
            continue;
          String strParam = line.substring(line.indexOf("(") + 1, indexForClose);
          String paramArr[] = strParam.split(",");

          String varName = "";
          String FILE = "";
          String DatasetName = "";
          String varValue = "";
          String headerLine = "0";
          String columnDeliminiator = ",";
          String firstDataLine = "1";
          String keyParameter = "";
          String PersistenceMode = "SaveOnHPDStop";
          try
          {
            Vector vecForSplitStr = new Vector();
            for(int i = 0; i < paramArr.length; i++)
            {
              paramArr[i] = paramArr[i].trim();
              if(paramArr[i].toUpperCase().startsWith("FILE"))
              {
                String FILEArr[] = paramArr[i].split("=");
                FILEArr[1] = FILEArr[1].trim();

                if(FILEArr.length > 2)
                {
                  for(int j = 2; j < FILEArr.length; j++)
                  {
                    FILEArr[1] = FILEArr[1] + "=" + FILEArr[j];
                  }
                }

                FILE = FILEArr[1].trim();
                if(FILE.trim().startsWith("\"") && FILE.trim().endsWith("\""))
                  FILE = FILE.substring(1,FILE.length()-1);
              }
              else if(paramArr[i].toUpperCase().startsWith("DATASETNAME"))
              {
                String datasetVar[] = paramArr[i].split("=");
                datasetVar[1] = datasetVar[1].trim();

                if(datasetVar.length > 2)
                {
                  for(int j = 2; j < datasetVar.length; j++)
                  {
                    datasetVar[1] = datasetVar[1] + "=" + datasetVar[j];
                  }
                }
                DatasetName = datasetVar[1];
                if(DatasetName.trim().startsWith("\"") && DatasetName.trim().endsWith("\""))
                  DatasetName = DatasetName.substring(1,DatasetName.length()-1);
              }
              else if(paramArr[i].toUpperCase().startsWith("VAR_VALUE"))
              {
                String varValueArr[] = paramArr[i].split("=");
                varValueArr[1] = varValueArr[1].trim();

                if(varValueArr.length > 2)
                {
                  for(int j = 2; j < varValueArr.length; j++)
                  {
                    varValueArr[1] = varValueArr[1] + "=" + varValueArr[j];
                  }
                }
                varValue = varValueArr[1];
                if(varValue.trim().startsWith("\"") && varValue.trim().endsWith("\""))
                  varValue = varValue.substring(1,varValue.length()-1);
              }
              else if(paramArr[i].toUpperCase().startsWith("HEADERLINE"))
              {
                String headerLineArr[] = paramArr[i].split("=");
                headerLineArr[1] = headerLineArr[1].trim();

                if(headerLineArr.length > 2)
                {
                  for(int j = 2; j < headerLineArr.length; j++)
                  {
                    headerLineArr[1] = headerLineArr[1] + "=" + headerLineArr[j];
                  }
                }
                headerLine = headerLineArr[1];
                if(headerLine.trim().startsWith("\"") && headerLine.trim().endsWith("\""))
                  headerLine = headerLine.substring(1,headerLine.length()-1);

              }
              else if(paramArr[i].toUpperCase().startsWith("COLUMNDELIMITER"))
              {
                String columnDelimiterArr[] = paramArr[i].split("=");
                if(columnDelimiterArr.length < 2 || columnDelimiterArr[1] == null)
                {
                  columnDeliminiator = ",";
                  i++;
                }
                else
                  columnDeliminiator = columnDelimiterArr[1].trim();
                  
                if(columnDelimiterArr.length > 2)
                {
                  for(int j = 2; j < columnDelimiterArr.length; j++)
                  {
                    columnDeliminiator = columnDeliminiator + "=" + columnDelimiterArr[j];
                  }
                }
                
                if(columnDeliminiator.equals("\""))
                  columnDeliminiator = ",";
                
                if(columnDeliminiator.trim().startsWith("\"") && columnDeliminiator.trim().endsWith("\""))
                  columnDeliminiator = columnDeliminiator.substring(1,columnDeliminiator.length()-1);
              }
              else if(paramArr[i].toUpperCase().startsWith("FIRSTDATALINE"))
              {
                String firstDataLineArr[] = paramArr[i].split("=");
                firstDataLineArr[1] = firstDataLineArr[1].trim();

                if(firstDataLineArr.length > 2)
                {
                  for(int j = 2; j < firstDataLineArr.length; j++)
                  {
                    firstDataLineArr[1] = firstDataLineArr[1] + "=" + firstDataLineArr[j];
                  }
                }
                firstDataLine = firstDataLineArr[1];
                if(firstDataLine.trim().startsWith("\"") && firstDataLine.trim().endsWith("\""))
                  firstDataLine = firstDataLine.substring(1,firstDataLine.length()-1);
              }
              else if(paramArr[i].toUpperCase().startsWith("PERSISTENCEMODE"))
              {
                String persistenceModeArr[] = paramArr[i].split("=");
                persistenceModeArr[1] = persistenceModeArr[1].trim();

                if(persistenceModeArr.length > 2)
                {
                  for(int j = 2; j < persistenceModeArr.length; j++)
                  {
                    persistenceModeArr[1] = persistenceModeArr[1] + "=" + persistenceModeArr[j];
                  }
                }
                PersistenceMode = persistenceModeArr[1];
                if(PersistenceMode.trim().startsWith("\"") && PersistenceMode.trim().endsWith("\""))
                  PersistenceMode = PersistenceMode.substring(1,PersistenceMode.length()-1);
              }
              else
              {
                paramArr[i] = paramArr[i].trim();
                if( paramArr[i].equals("\""))
                  continue;
                
                if(!vecForSplitStr.contains(paramArr[i]))
                {
                  if(varName.equals(""))
                    varName = paramArr[i];
                  else
                    varName = varName + "," + paramArr[i];
                }
              }
            }
          }
          catch(Exception e)
          {
            continue;
          }

          if(varName.equals(""))
            continue;
           
          if(varName.indexOf(",") > -1)
          {
            keyParameter = varName.substring(0 , varName.indexOf(","));
            varName = varName.substring(varName.indexOf(",") + 1);
          }
          else
          {
            keyParameter = varName;
            varName = "";
          }
          
          String[] arrRowValue = new String[10];
          arrRowValue[0] = varName;
          arrRowValue[1] = FileBean.escapeHTML(FILE);
          arrRowValue[2] = DatasetName;
          arrRowValue[3] = FileBean.escapeHTML(varValue);
          arrRowValue[4] = firstDataLine;
          arrRowValue[5] = columnDeliminiator;
          arrRowValue[6] = headerLine;
          arrRowValue[7] = keyParameter;
          arrRowValue[8] = PersistenceMode;
          ++rowId;
          arrRowValue[9] = String.valueOf(rowId);
          alIndexFileParameter.add(arrRowValue);
        }
        
        arrIndexedDataSourceDetails = new String[alIndexFileParameter.size()][10];
        for(int ii = 0; ii < alIndexFileParameter.size(); ii++)
        {
          String[] strRowValue = (String[])alIndexFileParameter.get(ii);
          for(int jj = 0; jj < strRowValue.length; jj++)
          {
            arrIndexedDataSourceDetails[ii][jj] = strRowValue[jj];
          }
        }
      }
      arrIndexedDataSourceDetails = corrServiceObj.sortArray(arrIndexedDataSourceDetails, sortOnCol, sortPrefrence, "STRING");
      return arrIndexedDataSourceDetails;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getIndexDataSourceDetails", "", "", "Exception - ", ex);
      System.out.println(ex);
      String[][] dummy = new String[0][10];
      return dummy;
    }
  }
  
 //method to get correaltion.conf path
  public String getCorrelationConfFilePath()
  {
    Log.debugLog(className, "getCorrelationConfFilePath", "", "", "Method started");
    String correaltionPath = "";
    try
    {
      correaltionPath = correlationPath + "correlation.conf";
      System.out.println(correaltionPath);
      return correaltionPath;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getCorrelationConfFilePath", "", "", "Exception - ", ex);
      return correaltionPath;
    }
  }

  /*
   * This function parse the xml file /home/netstorm/controller_ritesh/webapps/vjdbc/WEB-INF/classes/vjdbc-config.xml
   * and retuns a arrylist of an array having 5 fields
   * 0-id
   * 1-driver
   * 2-url
   * 3-user
   * 4-password
   */
  public ArrayList<String[]> getConnectioInfoFromXML()
  {
    try
    {
      ArrayList<String[]> xmlInfo = new ArrayList<String[]>();

      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      docBuilderFactory.setNamespaceAware(false);
      docBuilderFactory.setValidating(false);
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      File configFile = new File(Config.getWorkPath() + "/webapps/vjdbc/WEB-INF/classes/vjdbc-config.xml");

      // Returns null if file is not exist.
      if (!configFile.exists())
        return null;

      Document doc = docBuilder.parse(configFile);
      doc.getDocumentElement().normalize();
      NodeList connectionNode = doc.getElementsByTagName("connection");
      int totalConnectionTag = connectionNode.getLength();

      for (int i = 0; i < totalConnectionTag; i++)
      {
        Node jarNode = connectionNode.item(i);

        if (jarNode.getNodeType() == Node.ELEMENT_NODE)
        {
          String connectionInfo[] = new String[5];
          Element connectionEle = (Element) jarNode;
          connectionInfo[0] = connectionEle.getAttribute("id");
          connectionInfo[1] = connectionEle.getAttribute("driver");
          connectionInfo[2] = connectionEle.getAttribute("url");
          connectionInfo[3] = connectionEle.getAttribute("user");
          connectionInfo[4] = connectionEle.getAttribute("password");
          xmlInfo.add(connectionInfo);
        }
      }
      return xmlInfo;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  public String ImportDataBaseTable(String Driver , String connectionURL, String user, String  pasword , String query, String delimiter ,StringBuffer errorMsg )
  {
    String dbData = "";
    Connection conn = null;
    Statement stmt = null;
    ResultSet rs = null;
    if(delimiter.trim().equals(""))
      delimiter = ",";
    
    try
    {
      Class.forName(Driver);
      conn = DriverManager.getConnection(connectionURL , user , pasword);
      stmt = conn.createStatement();
	  
	  //Decode the query taking from jsp.
      String queryString = java.net.URLDecoder.decode(query, "UTF-8");
      rs = stmt.executeQuery(queryString);
	  
      ResultSetMetaData rsmd = rs.getMetaData();
      int colCount = rsmd.getColumnCount();
      
      for(int i = 1; i <= colCount; i++)
      {
        if(i == 1)
          dbData = rsmd.getColumnName(i);
        else
          dbData = dbData + delimiter + rsmd.getColumnName(i);
      }
      dbData = dbData + "\n" ;
      
      while(rs.next())
      {
        for(int i = 1; i <= colCount; i++)
        {
          Object value = rs.getObject(i);
         
          if(i == 1)
            dbData = dbData +  value.toString();
          else
            dbData = dbData + delimiter +  value.toString();
          
        }
        dbData = dbData + "\n" ;
      }
      return dbData;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "ImportDataBaseTable", "", "", "Exception - ", ex);
      errorMsg.append("Error in getting data from database.");
      return dbData;
    }
  }
  
  // Create file to store DB Result.
  public String CreateTempDBFile(String fileData, String filePathName, String file_Name)
  {

    FileWriter fileWriter = null;
    BufferedWriter bufferWriter = null;

    try
    {
      Log.debugLog(className, "CreateTempDBFile", "", "", "method called");

      if (fileData.equals(""))
        return "";

      Date date = new Date();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmssSSS");

      String createFilePath = filePathName;
      if (file_Name.equals("NA"))
        createFilePath = createFilePath + "/" + "DB" + "_" + sdf.format(date.getTime());
      else
        createFilePath = createFilePath + "/" + file_Name;

      // String fileName = "DB" + sdf.format(date.getTime());
      File newFile = new File(createFilePath);

      if (!newFile.exists())
        newFile.createNewFile();

      fileWriter = new FileWriter(newFile);
      bufferWriter = new BufferedWriter(fileWriter);

      // write data in file

      bufferWriter.write(fileData);
      bufferWriter.flush();
      bufferWriter.close();
      bufferWriter.close();
	  
      if (filePathName.equals(""))
        return newFile.getAbsolutePath();
      else
        return newFile.getName();
		
	}
    catch (Exception e)
    {
      // e.printStackTrace();
      Log.stackTraceLog(className, "CreateTempDBFile", "", "", "Exception = ", e);
      return "";
    }
    finally
    {
      try
      {
        if (bufferWriter != null)
          bufferWriter.close();
        if (bufferWriter != null)
          bufferWriter.close();

      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }
  
  // get the large data in chunks in hashMap.
  public HashMap getDataInLines(StringBuffer bufferData, int lines)
  {
    HashMap chunkHashMap = new HashMap();
    int bufferLength = 0;
    try
    {
      if (bufferData == null)
        return null;

      String arrResponseData[] = bufferData.toString().split("\n");
      // String arrResponseChunk[] = null;
      // Getting data.
      // int totalChunks = (bufferData.length() + chunkSize - 1) / chunkSize;
      // System.out.println("total chunks =" + totalChunks);
      // System.out.println("total buffer size == " + bufferData.length());
      int k = 0;
      int j = 1;
      int arrayLength = 1;
      bufferLength = arrResponseData.length;
      while (k < arrResponseData.length)
      {
        int i = 0;

        // System.out.println("length %%% = " + bufferLength);
        if (arrayLength > 0)
          arrayLength = Math.min(bufferLength, lines);
        else
          arrayLength = lines;

        String arrResponseChunk[] = new String[arrayLength];
        while (i < lines && k < arrResponseData.length)
        {
          arrResponseChunk[i] = arrResponseData[k];
          // System.out.print(arrResponseData[k]);
          k++;
          i++;
        }
        // System.out.println("length of array == " + arrResponseChunk.length);

        chunkHashMap.put("chunk" + j, arrResponseChunk);
        j++;
        bufferLength = bufferLength - lines;
        // int start = i * chunkSize;
        // System.out.println("start pos ==" + start + "   end pos ===" + Math.min(bufferData.length(), start + chunkSize));
        // if (start < bufferData.length())
        // chunkHashMap.put("chunk" + i, bufferData.substring(start, Math.min(bufferData.length(), start + chunkSize)));
      }
      return chunkHashMap;

    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  // get the large data in chunks in hashMap.
  public HashMap getDataInChunks(StringBuffer bufferData, int chunkSize)
  {
    HashMap chunkHashMap = new HashMap();
    try
    {
      if (bufferData == null)
        return null;

      // Getting data.
      int totalChunks = (bufferData.length() + chunkSize - 1) / chunkSize;
      // System.out.println("total chunks =" + totalChunks);
      // System.out.println("total buffer size == " + bufferData.length());
      for (int i = 0; i < totalChunks; i++)
      {
        int start = i * chunkSize;
        // System.out.println("start pos ==" + start + "   end pos ===" + Math.min(bufferData.length(), start + chunkSize));
        if (start < bufferData.length())
          chunkHashMap.put("chunk" + i, bufferData.substring(start, Math.min(bufferData.length(), start + chunkSize)));
      }
      return chunkHashMap;

    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  // get HashMap data into String Buffer.
  public StringBuffer getHashDataIntoBuffer(HashMap hmap)
  {
    StringBuffer sbuffer = new StringBuffer();

    try
    {
      // Checking for null condition.
      if (hmap == null)
        return sbuffer;

      for (int i = 1; i <= hmap.size(); i++)
      {
        String[] arrData = (String[]) hmap.get("chunk" + i);
        for (int k = 0; k < arrData.length; k++)
        {
          sbuffer.append(arrData[k]);
          sbuffer.append("\n");
        }
      }
      return sbuffer;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }
   
  public static void main(String args [])
  {
    String data[] = new String[]{"Key,v1,v2","datase", "file1.tx", ",","1", "0", "asdas" , "saveonstop"};
    NFUtils nfUtils = new NFUtils();
    StringBuffer s =new StringBuffer();
    int i [] = new int[2];
    data = new String[]{"Keyddddddddddddddddd,v1,v2","dataset", "file1.txt", ",","1", "0", "tttttt" , "saveonstop"};
    i[0] = 1;
    i[1] = 2;
   // nfUtils.addIndexDataSource(NFUtils.INDEX_DATA_SOURCE, data, s.append("dsddddddddddddsafsdgdfgdfh"));
    //nfUtils.updateIndexDataSource(NFUtils.INDEX_DATA_SOURCE, "delete", i , data, s.append("dddddddddddddasdsafddasdasfdsfsdfdddddddddddd"), "");
    //nfUtils.getIndexDataSourceDetails(1, 0);
  }
}

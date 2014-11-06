package pac1.Bean;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.RowSetMetaData;
import javax.sql.rowset.WebRowSet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.sun.rowset.WebRowSetImpl;

public class JDBCCommandData
{
  static String className = "JDBCCommandData";
  File file = null;
  String filepath = "";
  String arrDataValues[][] = null;
  String arrAttributesValues[][] = null;
  static String fileName = "";
  static int counter = 0;
  public static String FAIL_QUERY = "1"; // give row count
  String strColumnCount = "";

  public JDBCCommandData()
  {
  }

  public JDBCCommandData(String filePath)
  {
    this.filepath = filePath;
  }

  // Read the File content and Store in String Buffer
  public String readFile(String filename) throws IOException
  {
    String lineSep = System.getProperty("line.separator");
    BufferedReader br = new BufferedReader(new FileReader(filename));
    String nextLine = "";
    StringBuffer sb = new StringBuffer();
    while ((nextLine = br.readLine()) != null)
    {
      sb.append(nextLine);
      sb.append(lineSep);
    }
    CreateBackupFile(filename + "_bak", sb);
    return sb.toString();
  }

  // Create backup file.
  public boolean CreateBackupFile(String fileName, StringBuffer sb)
  {
    try
    {
      File backup_file = new File(fileName);
      if (backup_file.exists())
        return true;

      WriteXmlFile(fileName, sb);
      return true;

    }
    catch (Exception e)
    {
      e.printStackTrace();
      return false;
    }
  }

  // restore xml data from backup file.
  public boolean RestoreXMLData()
  {
    try
    {
      String lineSep = System.getProperty("line.separator");
      BufferedReader br = new BufferedReader(new FileReader(filepath + "_bak"));
      String nextLine = "";
      StringBuffer sb = new StringBuffer();
      while ((nextLine = br.readLine()) != null)
      {
        sb.append(nextLine);
        sb.append(lineSep);
      }

      // Write to original file.
      WriteXmlFile(filepath, sb);
      return true;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return false;
    }
  }

  // Use to save multiple response of same request
  public ArrayList saveBundleResponse()
  {
    ArrayList list = new ArrayList();
    try
    {
      String strOutput = readFile(filepath);
      Pattern p = Pattern.compile("CmdResponse");
      Matcher matcher = null;
      matcher = p.matcher(strOutput);
      int startIndex = 0;
      int endIndex = 0;
      String cmdOutPut;

      while (matcher.find())
      {
        if (matcher.start() == 0)
          continue;

        endIndex = matcher.start();
        cmdOutPut = strOutput.substring(startIndex, endIndex);
        JDBCVirtualData obj = saveCmdResponse(cmdOutPut, startIndex, endIndex);
        list.add(obj);
        startIndex = endIndex;
      }
      if (!strOutput.trim().equals(""))
      {
        cmdOutPut = strOutput.substring(startIndex);
        if (!cmdOutPut.trim().equals(""))
          ;
        {
          JDBCVirtualData obj = saveCmdResponse(cmdOutPut, startIndex, strOutput.length());
          if (!obj.getCmdName().trim().equals(""))
            list.add(obj);
        }
      }
      return list;
    }

    catch (Exception e)
    {
      e.printStackTrace();
      return list;
    }
  }

  // return true in case result type is xml else return false
  public boolean getCommandResult(String cmdResult)
  {
    try
    {
      Pattern p = Pattern.compile("<?xml");
      Matcher matcher = null;
      matcher = p.matcher(cmdResult);

      if (matcher.find() == true)
        return true;
      else
        return false;
    }

    catch (Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "saveCmdResponse", "", "", "Exception - ", e);
      return false;
    }

  }

  // Get the Response of XML File Data
  public JDBCVirtualData saveCmdResponse(String strTemp, int startIndex, int endIndex)
  {
    JDBCVirtualData virtualDataObj = new JDBCVirtualData();
    try
    {

      String value = "";
      boolean flagCmdResult = false;
      String cmdName = strTemp.substring(strTemp.indexOf("ReqCmd=") + 7, strTemp.indexOf("ReqCmdHashCode=") - 1);
      int dotIndex = cmdName.lastIndexOf(".");
      cmdName = cmdName.substring((dotIndex + 1), cmdName.length());
      virtualDataObj.setCmdName(cmdName.trim());

      String cmdHashCode = strTemp.substring(strTemp.indexOf("ReqCmdHashCode=") + "ReqCmdHashCode=".length(), strTemp.indexOf("Serialized=") - 1);

      virtualDataObj.setCmdHashCode(cmdHashCode.trim());

      // Serialized=Yes or No:
      String Serialized = strTemp.substring(strTemp.indexOf("Serialized=") + "Serialized=".length(), strTemp.indexOf("ResultClass=") - 1);
      virtualDataObj.setIsSerialized(Serialized);

      String resultClass = strTemp.substring(strTemp.indexOf("ResultClass=") + "ResultClass=".length(), strTemp.indexOf("Result=") - 1);
      virtualDataObj.setResultClass(resultClass.trim());

      flagCmdResult = getCommandResult(strTemp.substring(strTemp.indexOf("Result=") + "Result=".length(), strTemp.length()));

      String cmdResult = strTemp.substring(strTemp.indexOf("Result=") + "Result=".length(), strTemp.length());

      // In case Result type is XML we are storing a XML Data
      if (flagCmdResult == true)
      {
        virtualDataObj.setCmdOutput("xml");
        virtualDataObj.setXmlData(cmdResult);
      }
      else
      {
        virtualDataObj.setCmdOutput(cmdResult.trim());
        virtualDataObj.setXmlData("");
      }

      return virtualDataObj;
    }

    catch (Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "saveCmdResponse", "", "", "Exception - ", e);
      return virtualDataObj;
    }
  }

  public ArrayList printTableHeaders(String xmlData, StringBuffer errorMsg) throws SQLException
  {
    ArrayList list = new ArrayList();
    try
    {
      String xmlPropertiesAndMetaData = xmlData.substring(0, xmlData.lastIndexOf("</metadata>") + 11) + "\n</webRowSet>";
      String xmlRowsData = xmlData.substring(xmlData.lastIndexOf("</metadata>") + 11, xmlData.lastIndexOf("</webRowSet>"));
      StringReader reader = new StringReader(xmlPropertiesAndMetaData);

      WebRowSet webRS = new WebRowSetImpl();
      webRS.readXml(reader);
      RowSetMetaData metaData = (RowSetMetaData) webRS.getMetaData();

      int size = metaData.getColumnCount();
      strColumnCount = metaData.getColumnCount() + "";
      arrAttributesValues = new String[size + 1][10];

      // Set the Header
      arrAttributesValues[0][0] = "Name";
      arrAttributesValues[0][1] = "Label";
      arrAttributesValues[0][2] = "Type";
      arrAttributesValues[0][3] = "Size";
      arrAttributesValues[0][4] = "Precision";
      arrAttributesValues[0][5] = "Scale";
      arrAttributesValues[0][6] = "CalalogName";
      arrAttributesValues[0][7] = "ClassName";
      arrAttributesValues[0][8] = "SchemaName";
      arrAttributesValues[0][9] = "TableName";

      for (int i = 1; i <= metaData.getColumnCount(); i++)
      {
        arrAttributesValues[i][0] = metaData.getColumnName(i);
        arrAttributesValues[i][1] = metaData.getColumnLabel(i);
        arrAttributesValues[i][2] = metaData.getColumnTypeName(i);
        
        arrAttributesValues[i][3] = metaData.getColumnDisplaySize(i) + "";
        arrAttributesValues[i][4] = metaData.getPrecision(i) + "";
        arrAttributesValues[i][5] = metaData.getScale(i) + "";
        arrAttributesValues[i][6] = metaData.getCatalogName(i);
        arrAttributesValues[i][7] = metaData.getColumnClassName(i);
        arrAttributesValues[i][8] = metaData.getSchemaName(i);
        arrAttributesValues[i][9] = metaData.getTableName(i);

      }

      list.add(arrAttributesValues);

      int columns = metaData.getColumnCount();
      int count = 1;
      int sizeForRow = 0;

      StringBuffer newXmlData = new StringBuffer();
      newXmlData.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      ArrayList RepeatBlocksInfo = getRepeatBlockInfo(xmlRowsData, newXmlData);

      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      docBuilderFactory.setNamespaceAware(false);
      docBuilderFactory.setValidating(false);
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

      InputSource is = new InputSource(new StringReader(newXmlData.toString()));
      Document doc = docBuilder.parse(is);
      doc.getDocumentElement().normalize();
      NodeList rowNode = doc.getElementsByTagName("currentRow");
      int totalRowNodeTag = rowNode.getLength();
      boolean isDateParse = false;
      arrDataValues = new String[totalRowNodeTag + 1][columns];
      webRS.beforeFirst();

      for (int i = 1; i < arrAttributesValues.length; i++)
      {
        arrDataValues[0][i - 1] = arrAttributesValues[i][0];
      }

      int ii = 1;

      for (int i = 0; i < totalRowNodeTag; i++)
      {
        int jj = 0;
        Node rowNode1 = rowNode.item(i);

        if (rowNode1.getNodeType() == Node.ELEMENT_NODE)
        {
          NodeList colsNode = rowNode1.getChildNodes();
          for (int j = 0; j < colsNode.getLength(); j++)
          {
            Node col = colsNode.item(j);

            if (col.getNodeType() == Node.ELEMENT_NODE)
            {
              Element el = (Element) col;
              arrDataValues[ii][jj] = el.getFirstChild().getNodeValue();
              
              if(arrAttributesValues[jj +1][2].trim().equals("date"))
              {
                if(isNumeric(arrDataValues[ii][jj]))
                {
                  isDateParse = true;
                  arrDataValues[ii][jj] = getDate(Long.parseLong(arrDataValues[ii][jj]), "MM/dd/yyyy");
                }
              }
            
              jj++;
            }
          }
        }
        ii++;
      }

      // webRS.close();
      list.add(arrDataValues);
      list.add(RepeatBlocksInfo);
      list.add(isDateParse);
     
      return list;
    }

    catch (Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "printTableHeaders", "", "", "Exception - ", e);
      RestoreXMLData();
      errorMsg.append("The format XML file is not Correct, Do you want to restore. ");
      return list;
    }
  }
  
  public  String getDate(long milliseconds, String format)
  {
      SimpleDateFormat sdf = new SimpleDateFormat(format);
      return sdf.format(milliseconds);
  }
  
  public long getDateInMilis(String date , String format)
  {
    try
    {
      Calendar cal = Calendar.getInstance();

      SimpleDateFormat dateFormat = new SimpleDateFormat(format);
 
      cal.setTime(dateFormat.parse(date.trim()));
     
      return  cal.getTimeInMillis();
    }
    catch(Exception e)
    {
      Log.debugLog(className, "convertIntoMiliSec", "", "", "Exception getting in convert time: " + date + " to milsec,dateformat=" + format);
      return 0;
    }
  }
  
  public boolean isNumeric(String str)
  {
    try
    {
      Long i = Long.parseLong(str);
    }
    catch(NumberFormatException nfe)
    {
      return false;
    }
    return true;
  }

  // parse the repeat blocks information form xml data.
  public ArrayList ParseRepeatBlockInfo(String xmlData)
  {
    String repeatBlockInfo[] = null;

    int totalCount = countOccurrences("<currentRow>", xmlData);
    int previousCount = 0;

    ArrayList arrList = new ArrayList();
    String orignalXML = xmlData;
    try
    {

      while (!xmlData.equals(""))
      {
        if (xmlData.trim().startsWith("{$CAVREPEAT_BLOCK_START"))
        {
          int endIndex = xmlData.indexOf("}");

          arrList.add(xmlData.substring(0, endIndex + 1));
          xmlData = xmlData.substring(endIndex + 1).trim();
        }
        else if (xmlData.trim().startsWith("{$CAVREPEAT_BLOCK_END}"))
        {
          arrList.add("{$CAVREPEAT_BLOCK_END}");
          xmlData = xmlData.substring(22).trim();
        }
        else if (xmlData.indexOf("{$CAVREPEAT_BLOCK_START") == -1 && xmlData.indexOf("{$CAVREPEAT_BLOCK_END}") == -1)
        {
          arrList.add(xmlData);
          xmlData = "";
        }
        else
        {
          int index = -1;
          if (xmlData.indexOf("{$CAVREPEAT_BLOCK_START") != -1 && xmlData.indexOf("{$CAVREPEAT_BLOCK_END}") == -1)
            index = xmlData.indexOf("{$CAVREPEAT_BLOCK_START");
          else if (xmlData.indexOf("{$CAVREPEAT_BLOCK_START") == -1 && xmlData.indexOf("{$CAVREPEAT_BLOCK_END}") != -1)
            index = xmlData.indexOf("{$CAVREPEAT_BLOCK_END}");
          else
            index = (xmlData.indexOf("{$CAVREPEAT_BLOCK_START") < xmlData.indexOf("{$CAVREPEAT_BLOCK_END}")) ? xmlData.indexOf("{$CAVREPEAT_BLOCK_START") : xmlData.indexOf("{$CAVREPEAT_BLOCK_END}");
          arrList.add(xmlData.substring(0, index));
          xmlData = xmlData.substring(index);
        }
      }

      return arrList;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      arrList.add(orignalXML);
      return arrList;
    }
  }

  // function to return the repeat block information from the xml data.
  public ArrayList getRepeatBlockInfo(String xmlData, StringBuffer xmlToParse) throws Exception
  {
    ArrayList info = new ArrayList();
    try
    {
      ArrayList list = ParseRepeatBlockInfo(xmlData);
      int totalCount = countOccurrences("<currentRow>", xmlData);
      int previousCount = 0;
      for (int i = 0; i < list.size(); i++)
      {

        String str = list.get(i).toString();

        if (!str.trim().startsWith("{$CAVREPEAT_BLOCK_START") && !str.trim().equals("{$CAVREPEAT_BLOCK_END}"))
          xmlToParse.append(str);

        if (str.trim().startsWith("{$CAVREPEAT_BLOCK_START"))
        {
          String repeatInfo[] = new String[3];
          int StartCount = previousCount;
          int endCount = previousCount;
          String keyWord = str;

          endCount = EvaluateEndBlock(list, i);
          endCount = endCount + previousCount;
          repeatInfo[0] = keyWord;
          repeatInfo[1] = String.valueOf(StartCount);
          repeatInfo[2] = String.valueOf(endCount);
          info.add(repeatInfo);
        }
        else
        {
          previousCount = previousCount + countOccurrences("<currentRow>", str);
        }
      }
      return info;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return info;
    }
  }

  // Function to evaluate repeat blocks index's.
  public int EvaluateEndBlock(ArrayList list, int index)
  {
    int blockInfo = 0;
    try
    {
      int j = index + 1;
      int skipCount = 0;
      for (; j < list.size(); j++)
      {
        String inStr = list.get(j).toString();
        if (inStr.trim().equals("{$CAVREPEAT_BLOCK_END}") && skipCount == 0)
          break;

        if (inStr.trim().equals("{$CAVREPEAT_BLOCK_END}") && skipCount != 0)
          --skipCount;

        if (inStr.trim().startsWith("{$CAVREPEAT_BLOCK_START"))
          ++skipCount;

        blockInfo = blockInfo + countOccurrences("<currentRow>", inStr);
      }
      return blockInfo;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return blockInfo;
    }
  }

  public static int countOccurrences(String find, String str)
  {
    int count = 0;
    int lastIndex = 0;
    // int count = 0;

    while (lastIndex != -1)
    {
      lastIndex = str.indexOf(find, lastIndex);

      if (lastIndex != -1)
      {
        count++;
        lastIndex += find.length();
      }
    }

    return count;
  }

  public String[][] insertRowIntoXMLData(String[][] xmlData, String[] rowData, String operationName, int index)
  {
    try
    {
      // check for empty array.
      if (xmlData == null)
        return null;

      // check for row data.
      if (rowData == null)
        return xmlData;

      String modifiedData[][] = null;

      // insert data into 2d array.
      if (operationName.equals("insert"))
      {
        modifiedData = new String[xmlData.length + 1][xmlData[0].length];
        for (int i = 0; i < xmlData.length; i++)
        {

          for (int j = 0; j < xmlData[0].length; j++)
          {
            modifiedData[i][j] = xmlData[i][j];
          }
        }
        for (int k = 0; k < rowData.length; k++)
          modifiedData[modifiedData.length - 1][k] = rowData[k];

        return modifiedData;
      }
      else
      {
        // update Case
        modifiedData = new String[xmlData.length][xmlData[0].length];
        for (int i = 0; i < xmlData.length; i++)
        {
          // updating the data at index.
          if (index == i)
          {
            for (int k = 0; k < rowData.length; k++)
              modifiedData[index][k] = rowData[k];

            continue;
          }

          for (int j = 0; j < xmlData[0].length; j++)
          {
            modifiedData[i][j] = xmlData[i][j];
          }
        }
        return modifiedData;
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return xmlData;
    }
  }

  public String[][] DeleteRowsOfXmlData(String[][] xmlData, String[] deleteRows)
  {
    String[][] arrXmlData = null;
    try
    {
      if (xmlData == null)
        return null;

      if (deleteRows == null)
        return xmlData;

      int reducedLength = deleteRows.length;
      arrXmlData = new String[xmlData.length - reducedLength][xmlData[0].length];
      int rows = 0;
      int cols = 0;
      boolean isSkip = false;
      for (int i = 0; i < xmlData.length; i++)
      {
        isSkip = false;
        cols = 0;
        for (int k = 0; k < deleteRows.length; k++)
        {
          if (i == Integer.parseInt(deleteRows[k]))
          {
            isSkip = true;
            break;
          }
        }
        if (isSkip)
        {
          // rows++;
          continue;
        }

        for (int j = 0; j < xmlData[0].length; j++)
        {
          arrXmlData[rows][cols] = xmlData[i][j];
          cols++;
        }
        rows++;
      }
      return arrXmlData;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  public ArrayList SortArrayListObjects(ArrayList arrObjects)
  {
    try
    {

      for (int i = 0; i < arrObjects.size(); i++)
      {
        int min = i;
        for (int j = i + 1; j < arrObjects.size(); j++)
        {
          String arrElement1[] = (String[]) arrObjects.get(min);
          String arrElement2[] = (String[]) arrObjects.get(j);
          if (Integer.parseInt(arrElement1[1]) > Integer.parseInt(arrElement2[1]))
          {
            min = j;
          }
        }
        if (min != i)
        {
          Object temp = arrObjects.get(min);
          arrObjects.set(min, arrObjects.get(i));
          arrObjects.set(i, temp);
        }
      }
      return arrObjects;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return arrObjects;
    }
  }

  // Function to modify repeat blocks.
  public ArrayList ModifyRepeatBlockIndex(ArrayList inputList, int index)
  {
    try
    {
      for (int k = 0; k < inputList.size(); k++)
      {
        String[] arrInfo = (String[]) inputList.get(k);

        // Case if selected line occurs in b/w repeat block.
        if (index <= Integer.parseInt(arrInfo[1]))
        {
          arrInfo[1] = String.valueOf(Integer.parseInt(arrInfo[1]) - 1);
        }
        if (index <= Integer.parseInt(arrInfo[2]))
        {
          arrInfo[2] = String.valueOf(Integer.parseInt(arrInfo[2]) - 1);
        }
        inputList.set(k, arrInfo);
      }

      // Searching of repeat blocks without any column.
      for (int k = 0; k < inputList.size(); k++)
      {
        String[] search = (String[]) inputList.get(k);
        if (search[1].trim().equals(search[2].trim()))
        {
          inputList.remove(k);
        }
      }
      return inputList;

    }
    catch (Exception e)
    {
      e.printStackTrace();
      return inputList;
    }
  }

  // Function to evaluate the nested repeat blocks.
  public ArrayList EvaluateNestedRepeatBlocks(ArrayList arrRepeatList, String[] arrIndex)
  {
    try
    {
      for (int k = 0; k < arrIndex.length; k++)
      {
        int count = 0;
        ArrayList indexList = new ArrayList();
        for (int i = 0; i < arrRepeatList.size(); i++)
        {
          String[] arrInfo = (String[]) arrRepeatList.get(i);
          int diff = Integer.parseInt(arrInfo[2]) - Integer.parseInt(arrInfo[1]);
          if (diff == 1 && arrIndex[k].equals(arrInfo[2]))
          {
            ++count;
            indexList.add(i);
          }
        }
        while (count > 1)
        {
          arrRepeatList.remove(Integer.parseInt(indexList.get(0).toString()));
          indexList.remove(0);
          --count;
        }
      }
      return arrRepeatList;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return arrRepeatList;
    }
  }

  // function to recalculate the repeat blocks indexes.
  public ArrayList ReCalculateRepeatBlockIndex(ArrayList arrRepeatBlocks, String[] arrArgsList)
  {
    try
    {
      if (arrArgsList == null)
        return arrRepeatBlocks;

      // checking for nested repeat blocks
      arrRepeatBlocks = EvaluateNestedRepeatBlocks(arrRepeatBlocks, arrArgsList);

      for (int k = 0; k < arrRepeatBlocks.size(); k++)
      {
        String[] arr = (String[]) arrRepeatBlocks.get(k);
      }

      for (int i = 0; i < arrArgsList.length; i++)
      {
        arrRepeatBlocks = ModifyRepeatBlockIndex(arrRepeatBlocks, Integer.parseInt(arrArgsList[i]));
      }
      return arrRepeatBlocks;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return arrRepeatBlocks;
    }
  }

  public StringBuffer SaveXMLData(String metaData[][], String[][] xmlData, boolean isDateParse,  ArrayList repeatInfo, String[] rowData, String operationName, int index)
  {
    try
    {
      
      String saveData[][] = null;
      StringBuffer xmlFileData = new StringBuffer();
      String lineSep = System.getProperty("line.separator");
      if (operationName.equals("insert") || operationName.equals("update"))
      {
        saveData = insertRowIntoXMLData(xmlData, rowData, operationName, index);
      }

      if (operationName.equals("delete"))
      {
        repeatInfo = ReCalculateRepeatBlockIndex(repeatInfo, rowData);
        saveData = DeleteRowsOfXmlData(xmlData, rowData);
      }

      if (operationName.equals("RepeatBlock"))
      {
        saveData = xmlData;
        // repeatInfo.add(rowData);
      }
      ArrayList arrRepeatKeyword = new ArrayList();
      ArrayList arrStartIndex = new ArrayList();
      ArrayList arrEndIndex = new ArrayList();

      if (saveData != null)
      {
        xmlFileData.append("<data>");
        xmlFileData.append(lineSep);
        repeatInfo = SortArrayListObjects(repeatInfo);
        for (int i = 0; i < repeatInfo.size(); i++)
        {
       
          String[] arr = (String[]) repeatInfo.get(i);
        }
        if (repeatInfo != null && repeatInfo.size() > 0)
        {
          for (int i = 0; i < repeatInfo.size(); i++)
          {
            String[] repeatStr = (String[]) repeatInfo.get(i);
            arrRepeatKeyword.add(repeatStr[0]);
            arrStartIndex.add(String.valueOf(Integer.parseInt(repeatStr[1]) + 1));
            arrEndIndex.add(String.valueOf(Integer.parseInt(repeatStr[2]) + 1));

          }
        }

        for (int i = 1; i < saveData.length; i++)
        {
          while (arrEndIndex.contains(String.valueOf(i)))
          {
            xmlFileData.append("\n{$CAVREPEAT_BLOCK_END}\n");
            arrEndIndex.remove(String.valueOf(i));
          }

          while (arrStartIndex.contains(String.valueOf(i)))
          {
            xmlFileData.append("\n" + arrRepeatKeyword.get(arrStartIndex.indexOf(String.valueOf(i))) + "\n");
            arrRepeatKeyword.remove(arrStartIndex.indexOf(String.valueOf(i)));
            arrStartIndex.remove(String.valueOf(i));

          }

          xmlFileData.append("  <currentRow>");
          xmlFileData.append(lineSep);
          for (int j = 0; j < saveData[i].length; j++)
          {
            if(metaData[j+1][2].trim().equalsIgnoreCase("date") && isDateParse)
            {
              saveData[i][j] = String.valueOf(getDateInMilis(saveData[i][j] , "MM/dd/yyyy"));
            }
            
            xmlFileData.append("    <columnValue>" + saveData[i][j] + "</columnValue>");
            xmlFileData.append(lineSep);
          }

          xmlFileData.append("  </currentRow>");

          xmlFileData.append(lineSep);
        }

        if (arrEndIndex.size() > 0)
        {
          xmlFileData.append("\n{$CAVREPEAT_BLOCK_END}\n");
          arrEndIndex.remove(0);
          // arrStartIndex = removeElement(arrStartIndex, 0);

          // arrRepeatKeyword = removeElement(arrRepeatKeyword, 0);
        }
        xmlFileData.append("</data>");
        return xmlFileData;
      }
      return null;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  public int IndexOf(String str[], String find)
  {
    int result = -1;

    for (int i = 0; i < str.length; i++)
    {
      if (str[i].equals(find))
      {
        return i;
      }
    }

    return result;
  }

  public String[] removeElement(String[] str, int inx)
  {
    String[] modify = new String[str.length - 1];
    int x = 0;
    for (int i = 0; i < str.length; i++)
    {
      if (inx != i)
      {
        modify[x] = str[i];
        x++;
      }
    }

    return modify;
  }

  public boolean insertDataInVitualObj(int virtualObjIndex, String rows[], String filepath1, String operName, int index)
  {
    boolean bool = true;
    try
    {
      StringBuffer fileData = new StringBuffer();
      StringBuffer updateRows = new StringBuffer();
      JDBCVirtualData tempVirObj = null;
      JDBCCommandData JDBCCmdObj = new JDBCCommandData(filepath1);
      ArrayList arrListData = JDBCCmdObj.saveBundleResponse();
      JDBCVirtualData virObj = (JDBCVirtualData) arrListData.get(virtualObjIndex);

      if (!operName.equals("changeResult"))
      {
        String xmlResponseData = virObj.getXmlData();

        String xmlPropertiesAndMetaData = xmlResponseData.substring(0, xmlResponseData.lastIndexOf("</metadata>") + 11);
        ArrayList Resultlist = printTableHeaders(virObj.getXmlData(), new StringBuffer());

        ArrayList repeatBlockInfo = (ArrayList) Resultlist.get(2);

        if (operName.equals("insert"))
          updateRows = SaveXMLData((String[][]) Resultlist.get(0) , (String[][]) Resultlist.get(1),(Boolean) Resultlist.get(3) , repeatBlockInfo, rows, operName, 0);
        else if (operName.equals("update"))
          updateRows = SaveXMLData((String[][]) Resultlist.get(0) , (String[][]) Resultlist.get(1),(Boolean) Resultlist.get(3), repeatBlockInfo, rows, operName, index);
        else if (operName.equals("RepeatBlock"))
        {
          repeatBlockInfo.add(rows);
          updateRows = SaveXMLData((String[][]) Resultlist.get(0) , (String[][]) Resultlist.get(1),(Boolean) Resultlist.get(3), repeatBlockInfo, rows, operName, 0);

        }
        else
          updateRows = SaveXMLData((String[][]) Resultlist.get(0) , (String[][]) Resultlist.get(1),(Boolean) Resultlist.get(3), repeatBlockInfo, rows, operName, 0);

        String modifyResponseData = xmlPropertiesAndMetaData + "\n" + updateRows.toString() + "\n</webRowSet>";

        virObj.setXmlData(modifyResponseData);
      }
      else if (operName.endsWith("changeResult"))
      {
        if (!rows.equals(""))
          virObj.setCmdOutput(rows[0]);
      }
      for (int i = 0; i < arrListData.size(); i++)
      {
        String hashCode = "";
        String cmdName = "";
        String CmdOutput = "";
        String Serialized = "Yes";
        String resultClass = "";
        String XmlData = "";

        if (i != virtualObjIndex)
        {
          tempVirObj = (JDBCVirtualData) arrListData.get(i);
          hashCode = tempVirObj.getCmdHashCode();
          cmdName = tempVirObj.getCmdName();
          CmdOutput = tempVirObj.getCmdOutput();
          boolean isSerialized = tempVirObj.getIsSerialized();
          if (!isSerialized)
            Serialized = "No";
          resultClass = tempVirObj.getResultClass();
          XmlData = tempVirObj.getXmlData();
        }
        else
        {
          hashCode = virObj.getCmdHashCode();
          cmdName = virObj.getCmdName();
          CmdOutput = virObj.getCmdOutput();
          boolean isSerialized = virObj.getIsSerialized();
          if (!isSerialized)
            Serialized = "No";
          resultClass = virObj.getResultClass();
          XmlData = virObj.getXmlData();
        }

        if (!CmdOutput.trim().equals("xml"))
          fileData.append("CmdResponse:ReqCmd=de.simplicit.vjdbc.command." + cmdName + ":ReqCmdHashCode=" + hashCode + ":Serialized=" + Serialized + ":ResultClass=" + resultClass + ":Result=" + CmdOutput);
        else
          fileData.append("CmdResponse:ReqCmd=de.simplicit.vjdbc.command." + cmdName + ":ReqCmdHashCode=" + hashCode + ":Serialized=" + Serialized + ":ResultClass=" + resultClass + ":Result=" + XmlData);
        fileData.append("\n");
      }

      WriteXmlFile(filepath1, fileData);
      return bool;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return false;
    }
  }

  public boolean WriteXmlFile(String filePath, StringBuffer fileData)
  {
    File xmlFile = null;
    FileWriter xmlWriter = null;
    BufferedWriter xmlBuffer = null;
    try
    {
      xmlFile = new File(filePath);

      if (xmlFile.exists())
        xmlFile.delete();

      if (fileData.length() == 0)
        return false;

      xmlFile.createNewFile();

      xmlWriter = new FileWriter(xmlFile);
      xmlBuffer = new BufferedWriter(xmlWriter);

      // write data in file
      xmlBuffer.write(fileData.toString());

      xmlBuffer.flush();
      xmlWriter.close();
      xmlBuffer.close();

      return true;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return false;
    }
    finally
    {
      try
      {
        if (xmlWriter != null)
          xmlWriter.close();
        if (xmlBuffer != null)
          xmlBuffer.close();

      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  public static void main(String ar[]) throws Exception
  {
    JDBCCommandData cmdData = new JDBCCommandData("C:/Users/compass/Desktop/Qselect1/service_template1");
    // cmdData.RestoreXMLData();
    ArrayList list = cmdData.saveBundleResponse();
    System.out.println(list.size());
    int j;
    for (j = 0; j < list.size(); j++)
    {
      JDBCVirtualData virObj = (JDBCVirtualData) list.get(j);
      //System.out.println(virObj.getCmdHashCode());
      //System.out.println(virObj.getCmdName());
      //System.out.println(virObj.getCmdOutput());
      //System.out.println(virObj.getIsSerialized());
      //System.out.println(virObj.getResultClass());
      
      if (virObj.getCmdOutput().equals("xml"))
      {
        String col[] = new String[1];
        col[0] = "?column?";
        String row[] = new String[1];
        row[0] = "0";

        ArrayList getHeaderData = cmdData.printTableHeaders(virObj.getXmlData(), new StringBuffer());
        // System.out.println(virObj.getXmlData().substring(0 , virObj.getXmlData().lastIndexOf("</metadata>")+11));
        // System.out.println(virObj.getXmlData().substring( virObj.getXmlData().lastIndexOf("</metadata>")+11));
        StringBuffer s = cmdData.SaveXMLData((String[][]) getHeaderData.get(0) , (String[][]) getHeaderData.get(1),(Boolean) getHeaderData.get(3), (ArrayList) getHeaderData.get(2), row, "delete", 1);
        System.out.println(s);
        // cmdData.insertDataInVitualObj(j, row, "C:/home/netstorm/work/webapps/netocean/demo.xml", "insert", 0);

        /*for(int i = 0; i < getHeaderData.size(); i++)
        {
          String arr[][] = (String[][])getHeaderData.get(1);

          for(int k = 0; k < arr.length; k++)
          {
            for(int l = 0; l < arr[0].length; l++)
            {
              System.out.print(arr[k][l] + " ");
            }
            System.out.println();
          }

        }*/
      }
    }
  }
}

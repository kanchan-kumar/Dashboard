/*--------------------------------------------------------------------
 @Name    : ImportExternalData.java
 @Author  : Ravi Kant Sharma
 @Purpose : To read external data from csv file
 @Modification History:
 Ravi Kant Sharma --> Initial Version
 
 ----------------------------------------------------------------------*/
package pac1.Bean;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import pac1.Bean.GraphName.*;

public class ImportExternalCSVFileData
{
  private static String className = "ImportExternalCSVFileData";

  // get workpath
  private static String workPath = Config.getWorkPath();
  private int msgDataIndex = 0;
  private int totalGraphs = 0;
  private int lastDataTypeSize = 0;
  private int testNum = -1;
  GraphNames graphNames;
  long interval = 0;
  boolean isAcessLogStats = false;

  public ImportExternalCSVFileData(int testNum)
  {
    this.testNum = testNum;
    graphNames = new GraphNames(testNum);
    this.msgDataIndex = graphNames.getSizeOfMsgData();
    this.interval = graphNames.getInterval();
  }

  public double[] getHeaderByOpcode(int opcode)
  {
    try
    {
      int count = 0;
      double[] arrHeader = getHeaderVer2(opcode, testNum, (int) interval, 0);
      double[] arrTemp = new double[(msgDataIndex - 112) / 8];
      double[] arrResulted = new double[msgDataIndex / 8];
      if (arrHeader != null)
      {
        for (int i = 0, n = arrHeader.length; i < n; i++)
        {
          arrResulted[i] = arrHeader[i];
          count = i;
        }

        for (int i = 0, n = arrTemp.length; i < n; i++)
        {
          arrResulted[count] = 0.0;
          count++;
        }
        return arrResulted;
      }
      else
      {
        Log.errorLog(className, "getHeaderByOpcode", "", "", "unable to getting header for merge rtgMessage.dat file. ");
        return null;
      }
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getHeaderByOpcode", "", "", "Exception - ", ex);
      ex.printStackTrace();
      return null;
    }
  }

  // this function reads gdf for apache access log
  public Vector getApacheTestRunGDF(boolean isStandard, ArrayList vectNamesList)
  {
    try
    {
      Log.debugLog(className, "getApacheTestRunGDF", "", "", "Method Called. isStandard = " + isStandard);
      GDFBean gdfBean = new GDFBean();
      Vector gdf = new Vector();
      Vector gdfInfo = new Vector();
      int dataTypeSize = 0;
      int totalGraphDataTypeSize = 0;
      String gdfFilePath = gdfBean.getCustomGDFPath();

      if (isStandard)
        gdfFilePath = gdfFilePath + "cm_access_log_stats.gdf";
      else
        gdfFilePath = gdfFilePath + "cm_access_log_stats_ex.gdf";

      gdf = gdfBean.readFile(gdfFilePath);
      if (gdf != null)
      {
        int majorVersion = 3;
        int dataTypeElementSize = 8;
        
        for (int i = 1; i < gdf.size(); i++)
        {
          String gdfLine = gdf.get(i).toString();
          if(gdfLine.startsWith("#"))
            continue;
          
          String[] arrGDFLine = rptUtilsBean.strToArrayData(gdfLine, "|");

          try
          {
            if(gdfLine.toLowerCase().startsWith("info"))
            {
              majorVersion = Integer.parseInt(arrGDFLine[1].split("\\.")[0]);
              continue;
            } 
          }
          catch (Exception e)
          {
            Log.errorLog(className, "getApacheTestRunGDF", "", "", "version of gdf is not found.");
            continue;
          }

          if (majorVersion < 2)
            dataTypeElementSize = 4;
          else
            dataTypeElementSize = 8;
          
          if (gdfLine.startsWith("Group"))
          {
            arrGDFLine[5] = "" + vectNamesList.size();
            String gdfNewLine = rptUtilsBean.strArrayToStr(arrGDFLine, "|");
            gdfInfo.add(gdfNewLine + "\n");
            gdfInfo.add("\n");
            for (int j = 0; j < vectNamesList.size(); j++)
            {
              gdfInfo.add(vectNamesList.get(j).toString() + "\n");
            }
          }
          else
          {
            arrGDFLine[5] = "" + msgDataIndex;
            String dataTypeName = arrGDFLine[4].trim();
            dataTypeSize = GraphNameUtils.getDataTypeSize(dataTypeElementSize, majorVersion, dataTypeName);
            
            totalGraphDataTypeSize = totalGraphDataTypeSize + dataTypeSize;
            msgDataIndex = msgDataIndex + dataTypeSize;
            String gdfNewLine = rptUtilsBean.strArrayToStr(arrGDFLine, "|");
            gdfInfo.add(gdfNewLine + "\n");
          }
        }
      }
      else
      {
        Log.errorLog(className, "getApacheTestRunGDF", "", "", "GDF not found, filename - " + gdfFilePath);
        return null;
      }

      if(vectNamesList.size() > 1)
      {
        msgDataIndex = msgDataIndex + (totalGraphDataTypeSize * (vectNamesList.size() - 1));
      }
      return gdfInfo;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getApacheTestRunGDF", "", "", "Exception - ", ex);
      return null;
    }
  }

  // this function called for importing apache acccess log data
  public boolean importApacheAccessLogData(double[][] arrApacheLogData, ArrayList vectNamesList, boolean isStandard)
  {
    boolean importStatus = false;
    isAcessLogStats = true;
    try
    {
      Log.debugLogAlways(className, "importAccessLogData", "", "", "Method Called. Start process to import access log data in Test Run");
      Log.debugLogAlways(className, "importAccessLogData", "", "", "Acess Log Samples, rows = " + arrApacheLogData.length + " columns = " + arrApacheLogData[0].length + " ,Vector List = " + vectNamesList.toString());
      Vector gdf = getApacheTestRunGDF(isStandard, vectNamesList);
      
      if (gdf == null)
      {
        String errorMsg = "";
        if (isStandard)
          errorMsg = "Standard graphs GDF not found.";
        else
          errorMsg = "Extended graphs GDF not found.";

        Log.errorLog(className, "importAccessLogData", "", "", "Error in getting GDF - " + errorMsg);
        return importStatus;
      }

      ReportData reportData = new ReportData(testNum);
      Log.debugLogAlways(className, "importAccessLogData", "", "", "Getting raw data from rtgMessage.dat.");
      double[][] arrTestRunData = reportData.getLoadRowData();
      Log.debugLogAlways(className, "importAccessLogData", "", "", "Raw data, rows = " + arrTestRunData.length + " columns = " + arrTestRunData[0].length);
      importStatus = genNewTestRunGDF(testNum, gdf);
      Log.debugLogAlways(className, "importAccessLogData", "", "", "New GDF is updated. ImportStatus = " + importStatus);
      
      if (!importStatus)
        return importStatus;
      Log.debugLogAlways(className, "importAccessLogData", "", "", "Combind acess log data with Test Run.");
      importStatus = genNewRTG(arrTestRunData, arrApacheLogData);
      Log.debugLogAlways(className, "importAccessLogData", "", "", "New rtgMessage.dat is generated. ImportStatus = " + importStatus);
      if (!importStatus)
        return importStatus;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "importApacheAccessLogData", "", "", "Exception - ", ex);
      importStatus = false;
    }
    return importStatus;
  }

  private boolean genNewRTG(double[][] arrDataPackets, double[][] arrExternaldata)
  {
    try
    {
      Log.debugLog(className, "genNewRTG", "", "", "Method Called.");
      double[][] combinedData = getCombinedData(arrDataPackets, arrExternaldata);

      double[] arrStartPacket = getHeaderByOpcode(0);
      String rtgFilePath = workPath + "/webapps/logs/TR" + testNum + "/rtgMessage.dat";
      renameFile(rtgFilePath);
      if (!rtgFilePath.equals(""))
      {
        File file = new File(rtgFilePath);
        if (!file.exists())
          file.createNewFile();

        if (rptUtilsBean.changeFilePerm(file.getAbsolutePath(), "netstorm", "netstorm", "777") == false)
        {
          Log.debugLog(className, "importCSVData", "", "", "Permisson for TestRun " + testNum + "is not changed.");
        }
        else
        {
          Log.debugLog(className, "createTestRunDir", "", "", "Permisson for TestRun " + testNum + "is changed.");
        }

        FileChannel out = new FileOutputStream(file).getChannel();
        ByteBuffer bb = ByteBuffer.allocate(8); // allocates 8 bytes
        bb.order(ByteOrder.LITTLE_ENDIAN); // setting byte order
        for (int i = 0, n = arrStartPacket.length; i < n; i++)
        {
          bb.putDouble(arrStartPacket[i]);
          bb.flip();
          out.write(bb);
          bb.clear();
        }

        // writting data packets
        for (int i = 0; i < combinedData.length; i++)
        {
          int opcode = 1;
          if(i == combinedData.length - 1)
            opcode = 2;

          double[] arrEndPacket = getHeaderVer2(opcode, testNum, (int) interval, (i + 1));

          // writting header pkt
          for (int j = 0; j < arrEndPacket.length; j++)
          {
            bb.putDouble(arrEndPacket[j]);
            bb.flip();
            out.write(bb);
            bb.clear();
          }

          // writting data packets
          for (int j = 0; j < combinedData[i].length; j++)
          {
            bb.putDouble(combinedData[i][j]);
            bb.flip();
            out.write(bb);
            bb.clear();
          }
        }

        out.force(true);
        out.close();
      }
      return true;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "genNewRTG", "", "", "Exception - ", ex);
      return false;
    }
  }

  public HashMap importCSVData(int groupId, ArrayList arrGraphInfo, double[][] arrExternaldata)
  {
    try
    {
      HashMap hmDataImportStatus = new HashMap();
      Log.debugLog(className, "importCSVData", "", "", "Method Called.");
      ReportData reportData = new ReportData(testNum);
      double[][] arrDataPackets = reportData.getLoadRowData();
      if (arrDataPackets == null)
      {
        Log.errorLog(className, "importCSVData", "", "", "Error in getting test run data.");
        hmDataImportStatus.put("UNSUCCESS", "Error in getting test run data.");
        return hmDataImportStatus;
      }

      Vector csvGDF = genGDFFromCSV(arrGraphInfo, groupId);
      boolean flagGDF = genNewTestRunGDF(testNum, csvGDF);
      if (!flagGDF)
      {
        hmDataImportStatus.put("ERROR", "Unable to write testrun.gdf. Please see error log.");
        return hmDataImportStatus;
      }
      boolean isRTGWritten = genNewRTG(arrDataPackets, arrExternaldata);
      if (!isRTGWritten)
      {
        hmDataImportStatus.put("ERROR", "Unable to write rtgMessage.dat file. Please see error log.");
        return hmDataImportStatus;
      }
      hmDataImportStatus.put("SUCCESS", "");
      return hmDataImportStatus;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "genRtgMessageFile", "", "", "Exception - ", ex);
      HashMap hmDataImportStatus = new HashMap();
      hmDataImportStatus.put("SUCCESS", "Exception - " + ex);
      return hmDataImportStatus;
    }
  }

  // creating rtgMessage.dat file header
  private double[] getHeaderVer2(int OPCODE, int TEST_RUN, int INTERVAL, int SEQ_NUM)
  {
    try
    {
      double[] arrHeader = new double[14];
      arrHeader[0] = OPCODE;
      arrHeader[1] = TEST_RUN;
      arrHeader[2] = INTERVAL;
      arrHeader[3] = SEQ_NUM;

      // 10 fields for future use
      arrHeader[4] = 0;
      arrHeader[5] = 0;
      arrHeader[6] = 0;
      arrHeader[7] = 0;
      arrHeader[8] = 0;
      arrHeader[9] = 0;
      arrHeader[10] = 0;
      arrHeader[11] = 0;
      arrHeader[12] = 0;
      arrHeader[13] = 0;
      return arrHeader;
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      return null;
    }
  }

  // this will return combined data (testrun data and external data)
  private double[][] getCombinedData(double[][] arrTestRunData, double[][] arrExternalData)
  {
    try
    {
      Log.debugLogAlways(className, "getCombinedData", "", "", "Method Called.");
      ArrayList finalArrayList = new ArrayList();
      int mainLength = (arrTestRunData.length > arrExternalData.length) ? arrTestRunData.length : arrExternalData.length;
      double[][] arrDataList = new double[mainLength][];
      for (int mainArray = 0; mainArray < mainLength; mainArray++)
      {
        ArrayList<Double> iteratorList = new ArrayList<Double>();
        if (arrTestRunData.length > mainArray)
        {
          for (int firstArray = 14; firstArray < arrTestRunData[mainArray].length; firstArray++)
          {
            iteratorList.add(arrTestRunData[mainArray][firstArray]);
          }
        }
        else
        {
          for (int SecondArray = 14; SecondArray < arrTestRunData[0].length; SecondArray++)
          {
            iteratorList.add(0.0);
          }
        }

        if (arrExternalData.length > mainArray)
        {
          for (int SecondArray = 0; SecondArray < arrExternalData[mainArray].length; SecondArray++)
          {
            iteratorList.add(arrExternalData[mainArray][SecondArray]);
          }
        }
        else
        {
          for (int SecondArray = 0; SecondArray < arrExternalData[0].length; SecondArray++)
          {
            iteratorList.add(0.0);
          }
        }
        finalArrayList.add(iteratorList);
      }

      for (int i = 0; i < finalArrayList.size(); i++)
      {
        ArrayList arrIterator = (ArrayList) finalArrayList.get(i);
        arrDataList[i] = new double[arrIterator.size()];
        for (int j = 0; j < arrIterator.size(); j++)
        {
          arrDataList[i][j] = Double.parseDouble(arrIterator.get(j).toString());
        }
      }
      return arrDataList;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getCombinedData", "", "", "Exception - ", ex);
      return null;
    }
  }

  // 
  public Vector genGDFFromCSV(ArrayList graphsInfo, int groupId)
  {
    try
    {
      Log.debugLog(className, "genGDFFromCSV", "", "", "Method called.");
      Vector gdf = new Vector();
      String groupType = "vector";
      int numVectors = 1;

      String groupName = graphsInfo.get(0).toString();
      String groupDesc = graphsInfo.get(1).toString();
      String vectorName = graphsInfo.get(2).toString();
      ArrayList arrGraphsInfo = (ArrayList) graphsInfo.get(3);
      totalGraphs = arrGraphsInfo.size();
      String groupLine = "Group|" + groupName + "|" + groupId + "|" + groupType + "|" + totalGraphs + "|" + numVectors + "|-|-" + "|" + groupDesc + "\n";
      gdf.add(groupLine);
      gdf.add("\n");
      String vectNameLine = vectorName + "\n";
      gdf.add(vectNameLine);
      int graphId = 1;

      String formula = "-";
      // it will be zero always
      String numVectOfGraphs = "0";
      String graphState = "AS";
      int pdfId = -1;
      int pdfDataIndex = -1;
      for (int i = 0; i < arrGraphsInfo.size(); i++)
      {
        String strGraphsInfo = arrGraphsInfo.get(i).toString();
        String[] arrGraphs = rptUtilsBean.strToArrayData(strGraphsInfo, "|");
        String graphName = arrGraphs[0].trim();
        String dataTypeName = arrGraphs[1].trim();
        String graphDescription = arrGraphs[2].trim();
        lastDataTypeSize = graphNames.getDataTypeSize(dataTypeName);
        String graphNameLine = "Graph|" + graphName + "|" + graphId + "|" + "scalar" + "|" + dataTypeName + "|" + msgDataIndex + "|" + formula + "|" + numVectOfGraphs + "|" + graphState + "|" + pdfId + "|" + pdfDataIndex + "|" + "NA|NA|" + graphDescription + "\n";
        msgDataIndex = msgDataIndex + lastDataTypeSize;
        graphId++;
        gdf.add(graphNameLine);
      }

      return gdf;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "genGDFFromCSV", "", "", "Exception - ", ex);
      return null;

    }
  }

  // generating new testrun.gdf file
  public boolean genNewTestRunGDF(int testRun, Vector csvGDF)
  {
    Log.debugLog(className, "genNewTestRunGDF", "", "", "Method Called.");
    boolean gdfStatus = false;
    try
    {
      Vector oldTestRunGDF = graphNames.getGdfData();
      // Ravi -> Bug: If first line is not info line then we cant import csv data. need to fix
      String oldInfoLine = oldTestRunGDF.get(0).toString();
      String[] arrInfoLine = rptUtilsBean.strToArrayData(oldInfoLine, "|");
      int totalGroups = Integer.parseInt(arrInfoLine[2]) + 1;
      String newInfoLine = arrInfoLine[0] + "|" + arrInfoLine[1] + "|" + totalGroups + "|" + arrInfoLine[3] + "|" + arrInfoLine[4] + "|" + msgDataIndex;
      newInfoLine = newInfoLine + "|" + arrInfoLine[6] + "|" + arrInfoLine[7] + "\n";
      String gdfFilePath = workPath + "/webapps/logs/TR" + testNum + "/testrun.gdf";
      renameFile(gdfFilePath);
      File file = new File(gdfFilePath);
      if (!file.exists())
        file.createNewFile();

      // change testrun permisson
      // 777 means 1 is execute , 2 is write and 4 is read
      if (rptUtilsBean.changeFilePerm(file.getAbsolutePath(), "netstorm", "netstorm", "777") == false)
        Log.debugLog(className, "importCSVData", "", "", "Permisson for TestRun " + testNum + "is not changed.");
      else
        Log.debugLog(className, "importCSVData", "", "", "Permisson for TestRun " + testNum + "is changed.");

      FileWriter gdfFileStream = new FileWriter(file);
      BufferedWriter out = new BufferedWriter(gdfFileStream);
      out.write(newInfoLine);
      
      boolean isGroupAlreadyExist = false;
      
      if(!isAcessLogStats)
        isGroupAlreadyExist = addVectorNameIfGraphAlreadyExist(csvGDF, oldTestRunGDF);

      for (int i = 1; i < oldTestRunGDF.size(); i++)
      {
        String gdfLine = oldTestRunGDF.get(i).toString().trim() + "\n";
        if (gdfLine.startsWith("Group"))
        {
          if (i != 1)
            gdfLine = "\n" + gdfLine;
          out.write(gdfLine);
          out.write("\n");
        }
        else
        {
          out.write(gdfLine);
        }
      }

      if (!isGroupAlreadyExist)
      {
        for (int i = 0; i < csvGDF.size(); i++)
        {
          String gdfLine = csvGDF.get(i).toString().trim() + "\n";
          if (i == 0)
            gdfLine = "\n" + gdfLine;
          out.write(gdfLine);
        }
      }
      out.flush();
      out.close();
      gdfFileStream.close();
      gdfStatus = true;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "genNewTestRunGDF", "", "", "Exception - ", ex);
      gdfStatus = false;
      ex.printStackTrace();
    }

    return gdfStatus;
  }
  
  /**
   * This method wite new uploaded csv graph info if info already exist then write only vector name and increase id of that group
   * @param csvGDF
   * @param oldTestRunGDF
   * @return
   */
  private boolean addVectorNameIfGraphAlreadyExist(Vector csvGDF, Vector oldTestRunGDF)
  {
    Log.debugLog(className, "addVectorNameIfGraphAlreadyExist", "", "", "Method Call csvGDF = " + csvGDF);
    String matchGrpId = "";//contain new graphs group ID
    String vectName = "";//contain vector Name
    ArrayList<String> arrGrpId = getArrOfGroupId(oldTestRunGDF);//This method return array of All group ID
    boolean isGroupFound = false;
    int lastGroupIndex = 0;
    
    try
    {
      for (int i = 0; i < csvGDF.size(); i++)
      {
        String line = csvGDF.get(i).toString();
        if (line.toLowerCase().startsWith("group"))
        {
          String[] arrGroupId = rptUtilsBean.strToArrayData(line, "|");
          matchGrpId = arrGroupId[2];
          break;
        }
      }
   
      Log.debugLog(className, "addVectorNameIfGraphAlreadyExist", "", "", "arrGrpId  " + arrGrpId);
      
      //Here we check new group which already added and added at last
      //fix
      if (!arrGrpId.get(arrGrpId.size()-1).toString().equals(matchGrpId))
      {
        Log.debugLog(className, "addVectorNameIfGraphAlreadyExist", "", "", "Add direct Graph" + vectName);
        return false;//return because group not added at last  
      }
      
      vectName = csvGDF.get(2).toString();
      
      //This loop get last Group index
      for (int ii = 0; ii < oldTestRunGDF.size(); ii++)
      {
        String strLine = oldTestRunGDF.get(ii).toString();
        if (strLine.startsWith("Group"))
        {
          String[] tempStr = strLine.split("\\|");
          if (matchGrpId.equals(tempStr[2]))
          {
            lastGroupIndex = ii;
          }
        }
      }
      
      Log.debugLog(className, "addVectorNameIfGraphAlreadyExist", "", "", "Vector Name = " + vectName);
      for (int i = 0; i < oldTestRunGDF.size(); i++)
      {
        String line = oldTestRunGDF.get(i).toString();
        String[] tempArr = line.split("\\|");
        if (line.startsWith("Group"))
        {
          //here we check Group Line contain same group ID and it must added at last in testrun.gdf
          if (matchGrpId.equals(tempArr[2]) && (lastGroupIndex == i))
          {
            int vecCount = Integer.parseInt(tempArr[5]);//get vector count
            vecCount++;//increment by 1 for add new vector for same group
            String groupLine = "Group|" + tempArr[1] + "|" + tempArr[2] + "|" + tempArr[3] + "|" + tempArr[4] + "|" + vecCount + "|-|-" + "|" + tempArr[8] + "\n";
            oldTestRunGDF.remove(i);
            oldTestRunGDF.add(i, groupLine);
            Log.debugLog(className, "addVectorNameIfGraphAlreadyExist", "", "", "Add vector at index = " + (i + vecCount) + ", Graph name add = " + vectName);
            oldTestRunGDF.add((i + vecCount), vectName);
            isGroupFound = true;
            break;
          }
        }
      }
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "addVectorNameIfGraphAlreadyExist", "", "", "Exception - ", e);
    }
    return isGroupFound;
  }

  /**
   * This method create for get all graph graph id array from testrun.gdf file
   * @param oldTestRunGDF
   * @return
   */
  private ArrayList<String> getArrOfGroupId(Vector oldTestRunGDF)
  {
    ArrayList arrGrpId = new ArrayList();
    for (int i = 0; i < oldTestRunGDF.size(); i++)
    {
      String line = oldTestRunGDF.get(i).toString();
      if (line.startsWith("Group"))
      {
        String[] arrGaphId = line.split("\\|");
        arrGrpId.add(arrGaphId[2]);
      }
    }
    return arrGrpId;
  }

  /*
   * this will return backup file name counter
   */
  private int getBackUpFileNameCounter(String backupFilePath, String fileName)
  {
    try
    {
      File folder = new File(backupFilePath);
      if(!folder.exists())
      {
        folder.mkdir();
        return -1;
      }

      HashMap<Integer, String> hmFile = new HashMap<Integer, String>();
      String files;
      File[] listOfFiles = folder.listFiles();

      for (int i = 0; i < listOfFiles.length; i++)
      {
        if (listOfFiles[i].isFile())
        {
          files = listOfFiles[i].getName();
          if (files.startsWith(fileName))
          {
            String[] arrTemp = files.split(fileName);
            if (arrTemp.length == 2)
            {
              String[] arrTempBak = arrTemp[1].split(".bak");
              if(arrTempBak.length == 2)
                hmFile.put(Integer.parseInt(arrTempBak[1]), files);
              else
                hmFile.put(0, files);
            }
            else
              hmFile.put(-1, files);
          }
        }
      }

      if(hmFile.size() == 0)
        return -1;

      int maxValueInMap = Integer.parseInt((Collections.max(hmFile.keySet())).toString());
      return maxValueInMap;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getBackUpFileNameCounter", "", "", "Exception - ", ex);
      return -1;
    }
  }

  // this is for renaming the file
  public boolean renameFile(String filePath)
  {
    boolean flagRename = false;
    try
    {
      Log.debugLog(className, "renameFile", "", "", "Method Called. filePath = " + filePath);
      int index = filePath.lastIndexOf("/");
      String fileName = filePath.substring((index + 1), filePath.length());
      String backupLogFilePath = filePath.substring(0, index) + "/Import_Backup";
      File dirObj = new File(backupLogFilePath);
      rptUtilsBean.changeFilePerm(dirObj.getAbsolutePath(), "netstorm", "netstorm", "775");    
      int counter = getBackUpFileNameCounter(backupLogFilePath, fileName) + 1;
      String newFilePath = backupLogFilePath + "/" + fileName + ".bak";
      if(counter > 0)
        newFilePath = newFilePath + "" + counter;
      File oldfile = new File(filePath);
      File newfile = new File(newFilePath);
      if (oldfile.renameTo(newfile))
        flagRename = true;
      else
        flagRename = false;
      return flagRename;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "renameFile", "", "", "Exception - ", ex);
      return flagRename;
    }
  }

  public static void main(String[] args)
  {
    ImportExternalCSVFileData obj = new ImportExternalCSVFileData(2533);
    ArrayList arr = new ArrayList();
    arr.add("Vector 1");
    arr.add("Vector 2");
    Vector gdf = obj.getApacheTestRunGDF(false, arr);
    System.out.println("gdf = " + gdf);
  }
}

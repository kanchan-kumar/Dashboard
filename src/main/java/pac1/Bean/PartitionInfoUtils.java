package pac1.Bean;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import pac1.Bean.GraphName.GraphNameUtils;
import pac1.Bean.GraphName.GraphNames;

/**
 * This class is a utility class used for getting and processing Partitions related operations.
 */
public class PartitionInfoUtils 
{
  private static String className = "PartitionInfoUtils";
  private static String PARTITION_INFO_FILE_NAME = ".curPartition";
  
  private BinaryFileReader fileReader = null;
  
  /**
   * Default Constructor creating instance of File Reader.
   */
  public PartitionInfoUtils()
  {
    /*Creating instance of file reader.*/
    fileReader = new BinaryFileReader();
  }
  
  /**
   * Parameterized Constructor with instance of File Reader.
   * @param fileReader
   */
  public PartitionInfoUtils(BinaryFileReader fileReader)
  {
    this.fileReader = fileReader;
  }
  
  /**
   * This method is used to get Path of active partition directory.
   * @param sessionDirName
   * @return
   */
  public String getPartitionDirPath(String sessionDirName, int testRunNum, String generatorName, int controllerTR)
  {
    if(generatorName == null)
    {
      Log.debugLogAlways(className, "SessionReportData:getSessionDirPath", "", "", "testRunNum = " + testRunNum + " sessionDirName = " + sessionDirName);
      return (Config.getWorkPath() + "/webapps/logs/TR" + testRunNum + "/" + sessionDirName);
    }
    else
    {
      Log.debugLogAlways(className, "SessionReportData:getSessionDirPath", "", "", "controllerTR = " + controllerTR + ", Generator Name = " + generatorName +"(" + testRunNum + ")" + " sessionDirName = " + sessionDirName);
      return (Config.getWorkPath() + "/webapps/logs/TR" + controllerTR + "/NetCloud/" + generatorName.trim() + "/TR" + testRunNum + "/" + sessionDirName);
    }
  }
  
  /**
   * Create GraphNames object for partition due to changes in GDF.
   * @param testRunNum
   * @param activeSessionDirName
   * @param sessionGDFVersion
   * @return
   */  
  public GraphNames createGraphNamesObj(int testRunNum, String activeSessionDirName, String generatorName, String controllerTR, String sessionGDFVersion)
  {
    if(generatorName == null)
    {
      Log.debugLogAlways(className, "createGraphNamesObj", "", "", "Method Called. activeSessionDirName = " + activeSessionDirName + " testRunNum = " + testRunNum + " sessionGDFVersion = " + sessionGDFVersion);
      return new GraphNames(testRunNum, null, null, "NA", "", activeSessionDirName, sessionGDFVersion, false);
    }
    else
    {
      Log.debugLogAlways(className, "createGraphNamesObj", "", "", "Method Called. activeSessionDirName = " + activeSessionDirName + ", " +" sessionGDFVersion = " + sessionGDFVersion);
      return new GraphNames(testRunNum , null, null, controllerTR ,generatorName, activeSessionDirName, sessionGDFVersion, false);
    }
  }
  
  /**
   * Method is used to get the active partition name from test run .curParitition file.
   * @param testRun
   * @return
   */
  public String getCurrentPartitionNameByTestRunNumber(int testRun)
  {
    Log.debugLogAlways(className, "getCurrentPartitionNameByTestRunNumber", "", "", "Method Called. Test Run = "+ testRun);
    
    try
    {
      /*Creating instance of binary file reader.*/
      fileReader = new BinaryFileReader();
      
      /*Creating the path of file.*/
      String filePath = Config.getWorkPath() + "/webapps/logs/TR" + testRun + "/" + PARTITION_INFO_FILE_NAME;
      
      /*Getting object of file.*/
      File partitionInfoFile = fileReader.getFileObj(filePath);
      
      if(!fileReader.isFileAvailable(partitionInfoFile))
      {
	Log.debugLogAlways(className, "getCurrentPartitionNameByTestRunNumber", "", "", "Partition Information File " + PARTITION_INFO_FILE_NAME + " is not available in test run "+ testRun + ".");
	return null;
      }
      
      /*Creating the object of properties.*/
      Properties properties = new Properties();
      
      /*Loading File Content to Properties through Stream.*/
      properties.load(FileUtils.openInputStream(partitionInfoFile));
      
      /*Reading File in List.*/
      //List<String> partitionFileContents = FileUtils.readLines(partitionInfoFile); 
      
      if(properties.containsKey("CurPartitionIdx"))
      {
	return properties.getProperty("CurPartitionIdx");
      }
      else
      {
	Log.debugLogAlways(className, "getCurrentPartitionNameByTestRunNumber", "", "", "Current partition information not available in Test Run "+testRun);
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    return null;
  }
   
  /**
   * This method returns the duration of RTG file.
   * 
   * @param partitonDirPath
   * @return
   */
  public Long getPartitionDurationFromRTGFile(String partitonDirPath)
  {
    try
    {      
      //Getting Info Line from partition testrun.gdf file.
      String headerLine = getTestRunGDFInfoLine(fileReader.getFileObj(partitonDirPath + "/testrun.gdf"));

      if(headerLine == null)
      {
        Log.errorLog(className, "getPartitionDurationFromRTGFile", "", "", "testrun.gdf is not available for partition = " + partitonDirPath);
        return 0L;
      }

      String[] headerInfo = rptUtilsBean.split(headerLine, "|");

      //Getting the packet size.
      int packetSize = Integer.parseInt(headerInfo[DataPacketInfo.GDF_PACKET_DATA_SIZE_INDEX]);

      //Getting interval.
      int interval = Integer.parseInt(headerInfo[DataPacketInfo.GDF_INTERVAL_INDEX]);

      //Getting size of GDF File.
      long gdfFileSize = getSizeOfSessionDataFile(partitonDirPath + "/rtgMessage.dat");

      //Getting Total Packets.
      int totalPkts = (int) gdfFileSize / packetSize;

      long duration = interval * (totalPkts - 1); //As not including start Packet.

      return duration;

    }
    catch (Exception e)
    {
      e.printStackTrace();
      return 0L;
    }
  }
    
  /**
   * This method returns the duration of RTG file.
   * @param partitonDirPath
   * @return
   */
  public Long getDurationOfRTGSequenceFile(String rtgSequenceFileName, long rtgFileSize, int interval, int packetSize)
  {
    try
    {
      Log.debugLog(className, "getDurationOfRTGSequenceFile", "", "", "rtgSequenceFileName = " + rtgSequenceFileName + ", interval = "+ interval + ", packetSize = " + packetSize + ", RTG File Size = " + rtgFileSize);
        
      /*Getting Total Packets.*/
      int totalPkts = (int) rtgFileSize / packetSize;

      /*As not including start Packet.*/
      long duration = interval * (totalPkts - 1); 

      return duration;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return 0L;
    }
  }
  
  /**
   * This method reads the TestRun.gdf file and returns the header info line.
   * 
   * @param gdfFile
   * @return
   */
  public String getTestRunGDFInfoLine(File gdfFile)
  {
    try
    {
      Log.debugLogAlways(className, "getTestRunGDFInfoLine", "", "", "Method Called.");

      if (!fileReader.isFileAvailable(gdfFile))
      {
        return null;
      }

      //Reading line from TestRun.gdf file.
      List<String> gdfFileLines = FileUtils.readLines(gdfFile);

      for (String line : gdfFileLines)
      {
        if (line.trim().startsWith("Info"))
          return line;
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return null;
  }
  
  /**
   * Getting size of rtgMessage.dat file for NDE partition.
   * @param sessionDataFileName
   * @return
   */
  public long getSizeOfSessionDataFile(String sessionDataFile)
  {
    try
    {
      Log.debugLogAlways(className, "getSizeOfSessionDataFile", "", "", "Method Called. sessionDataFile = " + sessionDataFile);

      String osName = System.getProperty("os.name").trim().toLowerCase();

      if (osName.startsWith("win"))
      {
        return FileUtils.sizeOf(new File(sessionDataFile));
      }
      else
      {
        return getFileSizeByCmd(sessionDataFile);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return 0;
    }
  }

  /**
   * Getting size of File by run linux 'ls' command.
   * @param sessionDataFileName
   * @return
   */
  private long getFileSizeByCmd(String fileName)
  {
    try
    {
      Log.debugLogAlways(className, "getFileSizeByCmd", "", "", "Method Called. fileName = " + fileName);

      //Initializing variable for executing command.
      CmdExec cmdExec = new CmdExec();

      String command = "ls";

      //Creating arguments.
      String args = "-l " + fileName;

      Vector result = cmdExec.getResultByCommand(command, args, CmdExec.SYSTEM_CMD, "netstorm", "root");

      if (result != null)
      {
        String tempRecord = result.get(0).toString();
        String[] fileSizeTemp = tempRecord.split(" ");
        return Long.parseLong(fileSizeTemp[4]);
      }
    }
    catch (Exception e)
    {
      Log.errorLog(className, "getFileSizeByCmd", "", "", "Error in Getting size of fileName = " + fileName);
      e.printStackTrace();
    }
    return 0;
  }
  
  /**
   * Method is used to check the difference between two GDF file.
   * @param newPartitionGDFPath
   * @param oldPartitionGDFPath
   * @return
   */
  public boolean isGDFChanged(String newGDFPath, String oldGDFPath)
  {
    try
    {
      Log.debugLogAlways(className, "isGDFChanged", "", "", "method called. newGDFPath = " + newGDFPath + ", oldGDFPath = " + oldGDFPath);

      Runtime r = Runtime.getRuntime();
      String strCmd = "diff -b " + " " + newGDFPath + " " + oldGDFPath;
      
      Log.debugLogAlways(className, "isGDFChanged", "", "", "Command = " + strCmd);
      
      Process process = r.exec(strCmd);
      DataInputStream dataInputStream = new DataInputStream(process.getInputStream());
      
      String line = null;
      ArrayList<String> contentList = new ArrayList<String>();
      while((line = dataInputStream.readLine()) !=  null)
      {
	contentList.add(line);
      }
      
      int exitValue = process.waitFor();
      
      Log.debugLogAlways(className, "isGDFChanged", "", "", "Exit Value of Diff Command = " + exitValue);

      if(exitValue == 0)
      {
	Log.debugLogAlways(className, "isGDFChanged", "", "", "GDF files are same.");
        return false;
      }
      else
      {	
        for(String lines : contentList)
        {
          /*Ignore the Info Line changes.*/
          if(lines.contains("Info"))
            continue;
          
          Log.debugLogAlways(className, "isGDFChanged", "", "", "Changed GDF Lines = " + lines);
          
          /*If Any other line found in diff except header then return true.*/
          return true;
        }
        return false;
      }
    }
    catch (Exception e)
    {
      // e.printStackTrace();
      Log.stackTraceLog(className, "isGDFChanged", "", "", "Exception = ", e);
      return false;
    }
  }
  

  /**
   * This method returns the duration of partition from summary.top file.
   * @return
   */
  public String getDurationOfPartitionFromSummaryFile(String partitionDirPath)
  {
    try
    {
      // Creating instance of summary.top file.
      File summaryFileObj = new File(partitionDirPath + "/summary.top");

      // Checking file existence.
      if (!fileReader.isFileAvailable(summaryFileObj))
      {
        Log.errorLog(className, "getDurationOfPartitionFromSummaryFile", "", "", "Summary.top file not exist in partition = " + partitionDirPath);
        return "NA";
      }

      // Reading data from file
      String fileContent = FileUtils.readFileToString(summaryFileObj);

      // Getting the fields by splitting string.
      String[] fileFields = rptUtilsBean.split(fileContent, "|");

      if (fileFields == null || fileFields.length < 14)
      {
        Log.errorLog(className, "getDurationOfPartitionFromSummaryFile", "", "", "Summary.top contains partial information");
        return "NA";
      }

      return fileFields[14];
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    return "NA";
  }
  
  /**
   * This Method returns the First TimeStamp
   * @param fileName
   * @param totalBytesToRead
   * @return
   */
  public String getFirstPacketTimeStampFomRTGFile(String fileName, int totalBytesToRead)
  {
    try
    {
      Log.debugLogAlways(className, "getFirstTimeStampFomRTGFile", "", "", "Method Called. totalBytesToRead = " + totalBytesToRead + " fileName = " + fileName);

      File file = fileReader.getFileObj(fileName);

      byte[] byteArr = new byte[totalBytesToRead];

      fileReader.readRTGFile(file, totalBytesToRead, byteArr);

      ByteBuffer byteBuffer = ByteBuffer.wrap(byteArr);
      byteBuffer = byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

      double[] packetInfo = new double[6];

      for (int i = 0; i < DataPacketInfo.HEADER_INFO_COUNT; i++)
      {
        packetInfo[i] = byteBuffer.getDouble(i * 8);
      }

      int opcode = (int) packetInfo[DataPacketInfo.OPCODE_INDEX];
      int testRun = (int) packetInfo[DataPacketInfo.TEST_RUN_INDEX];
      int interval = (int) packetInfo[DataPacketInfo.INTERVAL_INDEX];
      int seqNum = (int) packetInfo[DataPacketInfo.SEQ_NUM_INDEX];
      double partition_Name = packetInfo[DataPacketInfo.PARTITION_NAME_IDX];
      double absTimeStamp = (long) packetInfo[DataPacketInfo.TIMESTAMP_IDX];

      // absTimeStamp = epochTimeStamp + absTimeStamp;

      String absoluteTimeStamp = new DecimalFormat("#").format(absTimeStamp);
      String partition_Name_Format = new DecimalFormat("#").format(partition_Name);

      Log.debugLogAlways(className, "getFirstPacketTimeStampFomRTGFile", "", "", "testRun = " + testRun + " opcode = " + opcode + " interval = " + interval + " seqNum = " + seqNum + " Partition_Name = " + partition_Name_Format + " absoluteTimeStamp = " + absoluteTimeStamp + " Formatted Time = " + ExecutionDateTime.convertDateTimeStampToFormattedString((long) absTimeStamp, "MM/dd/yy HH:mm:ss", null));

      if (absoluteTimeStamp.trim().equals("-1"))
        return null;

      return absoluteTimeStamp;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }
  
  /**
   * Method is used to convert elapsed user time input in (HH:mm:ss) format to Absolute Millisecond for supporting Partition Mode.
   * @param inputTime
   * @return
   */
  public long changeElapsedFormatTimeToAbsoluteTimeStamp(String inputTime, int testRun)
  {
    try
    {
      Log.debugLogAlways(className, "changeElapsedFormatTimeToAbsoluteTimeStamp", "", "", "Method Called. inputTime = "+ inputTime + ", testRun = " + testRun);
      
      /*Checking for string numeric values.*/
      if(PartitionInfoUtils.isNumeric(inputTime))
      {
	Log.debugLogAlways(className, "changeElapsedFormatTimeToAbsoluteTimeStamp", "", "", "Input Time " + inputTime + " is already in numeric format.");
	return Long.parseLong(inputTime);
      }
      
      if(!ExecutionDateTime.isValidTimeFormatInHHMMSS(inputTime))
      {
	Log.errorLog(className, "changeElapsedFormatTimeToAbsoluteTimeStamp", "", "", "Bad Format  of Elapsed Input String. inputTime = " + inputTime);
	return 0L;
      }
      
      /*Getting Elapsed Time in Millisecond.*/
      long elapsedMillies = ExecutionDateTime.convertFormattedTimeToMillisecond(inputTime, ":");
      
      String testStartTime = Scenario.getTestRunStartTime(testRun);
      
      /*Getting Absolute Time By Adding Test Start Time.*/
      long absoluteTime = elapsedMillies + ExecutionDateTime.convertFormattedDateToMilliscond(testStartTime, "MM/dd/yy HH:mm:ss", null);
      
      return absoluteTime;
      
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return 0L;
    }
  }
  
  /**
   * Method is used to get the GDF file data in vector.
   * @param testRun
   * @param activePartitionDirName
   */
  public Vector<String> getTestRunGDF(int testRun, String activePartitionDirName, String gdfVersion)
  {
    try
    {
      Log.debugLogAlways(className, "getTestRunGDF", "", "", "Method Called. testRun = " + testRun + ", activePartitionDirName = " + activePartitionDirName + ", gdfVersion = " + gdfVersion);    
      return GraphNameUtils.getTestRunGdf(testRun, "NA", "NA", activePartitionDirName, gdfVersion);
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }
  
  /**
   * Method is used to get the GDF file data in vector.
   * @param testRun
   * @param activePartitionDirName
   */
  public Vector<String> getTestRunPDF(int testRun, String activePartitionDirName, String gdfVersion)
  {
    try
    {
      Log.debugLogAlways(className, "getTestRunPDF", "", "", "Method Called. testRun = " + testRun + ", activePartitionDirName = " + activePartitionDirName + ", gdfVersion = " + gdfVersion);    
      String pdfPath = GraphNameUtils.getPDFFilePath(testRun, "NA", "NA", activePartitionDirName, gdfVersion);
      return GraphNameUtils.readPDFFile(pdfPath);
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }
  
  /**
   * Method used to get the size of all rtgMessage.dat file inside.
   * @param partitionDirPath
   * @return
   */
  public PartitionDataProperties getPartitionDataProperties(String partitionDirPath, SortedSet<Integer> arrPartitionSequenceList)
  {
    try
    {
      Log.debugLogAlways(className, "getPartitionDataProperties", "", "", "Method Called. partitionDirPath = " + partitionDirPath);
           
      /*Creating Instance of Partition Data Properties Object.*/
      PartitionDataProperties partitionDataPropertiesObj = new PartitionDataProperties();
      
      /*Sum of all RTG file size.*/
      long partitionFileDuration = 0L;
      
      /*Managing Partition File Sequence/Versions*/
      ArrayList<Integer> arrPartitionFileSequenceList = new ArrayList<Integer>();
      
      /*Managing Partition File Sequence/Versions Size list.*/
      ArrayList<Long> arrPartitionFileSequenceSizeList = new ArrayList<Long>();
            
      /*Iterating through each Sequence/Version.*/
      for(Integer sequenceNumber : arrPartitionSequenceList)
      {
	/*Default Test Run GDF Name.*/
	String gdfFileSequenceName = partitionDirPath + "/" + "testrun.gdf";
	
	/*Default Test Run RTG File Name.*/
	String rtgFileSequenceName = partitionDirPath + "/" + "rtgMessage.dat";
	
	/*Skip Default File and Include Sequence Number to Other versions.*/
	if(sequenceNumber > 0)
	{
	  /*Appending Version/Sequence to GDF file.*/
	  gdfFileSequenceName = gdfFileSequenceName + "." + sequenceNumber;
	  
	  /*Appending Version/Sequence to RTG file.*/
	  rtgFileSequenceName = rtgFileSequenceName + "." + sequenceNumber;
	}
	
	/*Checking for Availability of GDF File Sequence/Version.*/
	if(!fileReader.isFileAvailable(new File(gdfFileSequenceName)))
	{
	  Log.errorLog(className, "getPartitionDirAllRTGFileSize", "", "", "GDF File not available in following path. " + gdfFileSequenceName);
	  continue;
	}
	
	/*Checking for Availability of RTG File Sequence/Version.*/
	if(!fileReader.isFileAvailable(new File(rtgFileSequenceName)))
	{
	  Log.errorLog(className, "getPartitionDirAllRTGFileSize", "", "", "RTG File not available in following path. " + rtgFileSequenceName);
	  continue;
	}
	  	
	/*Getting Info Line from partition testrun.gdf file.*/
	String headerLine = getTestRunGDFInfoLine(fileReader.getFileObj(partitionDirPath + "/testrun.gdf"));

	/*Checking for Availability of GDF content.*/
	if(headerLine == null)
	{
	  Log.errorLog(className, "getPartitionDurationFromRTGFile", "", "", "testrun.gdf is not available/Empty for partition = " + partitionDirPath + ", Version/Sequence Number = " + sequenceNumber);
	  continue;
	}

	/*Splitting GDF header data.*/
	String[] headerInfo = rptUtilsBean.split(headerLine, "|");

	/*Getting the packet size.*/
	int packetSize = Integer.parseInt(headerInfo[DataPacketInfo.GDF_PACKET_DATA_SIZE_INDEX]);
	
	/*Getting size of RTG File.*/
	long rtgFileSize = getSizeOfSessionDataFile(rtgFileSequenceName);
		
	/*Checking for Size of RTG File.*/
	if(rtgFileSize <= 0)
	{
	  Log.errorLog(className, "getPartitionDurationFromRTGFile", "", "", "Unable to get size of RTG Sequence file, Skipping Partition Sequence RTG File. File Path = " + rtgFileSequenceName);
	  continue;
	}

	/*Getting interval.*/
	int interval = Integer.parseInt(headerInfo[DataPacketInfo.GDF_INTERVAL_INDEX]);
		
	/*Getting RTG Sequence File Duration.*/
	long partitionSequenceFileDuration = getDurationOfRTGSequenceFile(rtgFileSequenceName, rtgFileSize, interval, packetSize);
	
	/*Adding the duration of partition sequence file to partition size to get total partition duration.*/
	partitionFileDuration = partitionFileDuration + partitionSequenceFileDuration;
	
	/*Storing Partition Sequence/Version Number and Size in List.*/
	arrPartitionFileSequenceList.add(sequenceNumber);
	arrPartitionFileSequenceSizeList.add(rtgFileSize);
      }
      
      /*Adding to Properties object.*/
      partitionDataPropertiesObj.setArrPartitionSequenceFileList(arrPartitionFileSequenceList);
      partitionDataPropertiesObj.setArrPartitionSequenceFileSizeList(arrPartitionFileSequenceSizeList);
      partitionDataPropertiesObj.setPartitionDuration(partitionFileDuration);
         
      return partitionDataPropertiesObj;
      
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }
  
  /**
   * Method is used to get all versions of GDF in Partition.
   * @param partitionFilePath
   * @return
   */
  public SortedSet<Integer> getPartitionAvailableGDFVersion(String partitionFilePath)
  {
    try
    {
      Log.debugLogAlways(className, "getPartitionAvailableGDFVersion", "", "", "Method Called. Partition File Path = " + partitionFilePath);      
      
      /*List of Available Partition version/sequences of files.*/
      SortedSet<Integer> arrAvailableGDFVersionList = new TreeSet<Integer>();
      
      /*Getting the Name of all GDF available in Partition.*/
      ArrayList<String> arrAvailableGDFFilesList = getPartitionAvailableGDFFiles(partitionFilePath);
      
      /*Checking for available GDF files in Partition.*/
      if(arrAvailableGDFFilesList == null)
      {
	Log.errorLog(className, "getPartitionAvailableGDFVersion", "", "", "GDF Files are not available in Partition " + partitionFilePath);
	return null;
      }
      
      /*Checking for GDF File Name.*/
      for(String gdfFileName : arrAvailableGDFFilesList)
      {
	/*Get Tokens of GDF Name.*/
	String nameTokens[] = rptUtilsBean.split(gdfFileName, ".");
	
	/*Checking For invalid GDF Name.*/
	if(nameTokens == null || nameTokens.length < 2)
	  continue;
	
	int gdfVersion = 0;
	
	if(nameTokens.length == 2)
	  arrAvailableGDFVersionList.add(0);
	else
	{	  
	  try
	  {
	    /*Getting GDF version form GDF file.*/
	    gdfVersion = Integer.parseInt(nameTokens[2]);
	  }
	  catch(Exception e)
	  {
	    Log.errorLog(className, "getPartitionAvailableGDFVersion", "", "", "GDF Name is not valid. GDF File Name = " + gdfFileName + ", in Partition " + partitionFilePath);
	    continue;
	  }
	  
	  /*Adding GDF version to list.*/
	  arrAvailableGDFVersionList.add(gdfVersion);
	}
      }
            
      return arrAvailableGDFVersionList;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }
  
  /**
   * Method is used to get all available GDF files of partition. 
   * @param partitionDirPath
   * @return
   */
  public ArrayList<String> getPartitionAvailableGDFFiles(String partitionDirPath)
  {
    try
    {
      Log.debugLogAlways(className, "getPartitionAvailableGDFFiles", "", "", "Method Called. Partition Directory Path = " + partitionDirPath);      
      
      /*Getting the instance of Partition Directory.*/
      File partitionDir = new File(partitionDirPath);
      
      /*Getting All GDF Files inside Partition.*/
      List<File> testRunGDFFileList = Arrays.asList(partitionDir.listFiles(partitionDirGDFFileFilter));
      
      /*Checking of Existence of GDF files in partition.*/
      if(testRunGDFFileList == null)
      {
	Log.errorLog(className, "getPartitionAvailableGDFFiles", "", "", "TestRun.gdf Files and its versions are not available in Partition File = " + partitionDirPath);
	return null;
      }
            
      /*List of GDF Names in partition.*/
      ArrayList<String> arrAvailableGDFFileList = new ArrayList<String>();
      
      /*Iterating through each GDF file instance and get its name.*/
      for(File gdfFile : testRunGDFFileList)
      {
	/*Adding the GDF file Name in List.*/
	arrAvailableGDFFileList.add(gdfFile.getName());
      }
      
      return arrAvailableGDFFileList;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }
  
  /**
   * Method is used to get all available rtgMessage.dat files of partition. 
   * @param partitionDirPath
   * @return
   */
  public ArrayList<String> getPartitionAvailableRTGFiles(String partitionDirPath)
  {
    try
    {
      Log.debugLogAlways(className, "getPartitionAvailableRTGFiles", "", "", "Method Called. Partition Directory Path = " + partitionDirPath);      
      
      /*Getting the instance of Partition Directory.*/
      File partitionDir = new File(partitionDirPath);
      
      /*Getting All RTG Files inside Partition.*/
      List<File> testRunRTGFileList = Arrays.asList(partitionDir.listFiles(partitionDirRTGFileFilter));
      
      /*Checking of Existence of RTG files in partition.*/
      if(testRunRTGFileList == null)
      {
	Log.errorLog(className, "getPartitionAvailableRTGFiles", "", "", "rtgMessage.dat Files and its versions are not available in Partition File = " + partitionDirPath);
	return null;
      }
            
      /*List of RTG file name in partition.*/
      ArrayList<String> arrAvailableRTGFileList = new ArrayList<String>();
      
      /*Iterating through each RTG file instance and get its name.*/
      for(File rtgFile : testRunRTGFileList)
      {
	/*Adding the RTG file Name in List.*/
	arrAvailableRTGFileList.add(rtgFile.getName());
      }
      
      return arrAvailableRTGFileList;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }
  
  /**
   * This filter only returns testrun.gdf and its associated version files.
   */
  FileFilter partitionDirGDFFileFilter = new FileFilter()
  {
    @Override
    public boolean accept(File file)
    {
      //Pattern for only numeric name file.
      Pattern pattern = Pattern.compile("testrun.gdf.*");
      
      /*Validate through Partition File Name Format.*/
      String partitionFileName = file.getName();

      /*Validate the Partition Name for valid Partition Directory.*/
      if(pattern.matcher(partitionFileName).matches())
        return true;
      else
        return false;
    }
  };
  
  
  /**
   * This filter only returns rtgMessage.dat and its associated version files.
   */
  FileFilter partitionDirRTGFileFilter = new FileFilter()
  {
    @Override
    public boolean accept(File file)
    {
      //Pattern for only numeric name file.
      Pattern pattern = Pattern.compile("rtgMessage.dat.*");
      
      /*Validate through Partition File Name Format.*/
      String partitionFileName = file.getName();

      /*Validate the Partition Name for valid Partition Directory.*/
      if(pattern.matcher(partitionFileName).matches())
        return true;
      else
        return false;
    }
  };
 
  
  /**
   * Method is used to get the size of object in bytes.
   * @param objToGetSize
   * @return
   */
  public static long getObjectSizeInBytes(Object objToGetSize)
  {
    try
    {
      Log.debugLogAlways(className, "getObjectSizeInBytes", "", "", "Method Called.");
      
      /*Getting system temporary directory path.*/
      String objectFilePath = FileUtils.getTempDirectoryPath() + "/executionObject.dat";
      
      Log.debugLogAlways(className, "getObjectSizeInBytes", "", "", "objectFilePath = " + objectFilePath);
      File fileExist = new File(objectFilePath);
        
      /*Checking for existence of file.*/
      if(fileExist.exists())
        fileExist.delete();

      File newFile = new File(objectFilePath);
      newFile.createNewFile();

      /*Creating object output stream.*/
      ObjectOutputStream objectWriter = new ObjectOutputStream(new FileOutputStream(objectFilePath));
      
      /*Writing object to file.*/
      objectWriter.writeObject(objToGetSize);
      
      /*Closing the stream.*/
      objectWriter.close();
      
      Log.debugLogAlways(className, "getObjectSizeInBytes", "", "", "Object Serialized Successfully at " + newFile.getAbsolutePath());
      return FileUtils.sizeOf(newFile);
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getObjectSizeInBytes", "", "", "Exception - ", ex);
      return 0;
    }
  }
  
  /**
   * Method is used to convert double value with fraction to string.
   * @return
   */
  public static String convertDoubleAsString(double value)
  {
    return new DecimalFormat("#").format(value);
  }
  
  /**
   * Pydi:- This method will return test run duration in partition mode
   * Logic:in current partition
   *   if rtgMessage.dat not exit then we return time taken from summary.top
   *   else time taken from summary.top + duration from rtgMessage.dat
   * Note: if summary.top not exists then we calculate duration from all the available partition's
   * @param sessionReportData
   * @return
   */
  public Long getTestRunDurationInPartitonMode(SessionReportData sessionReportData)
  {
    try
    {
      Log.debugLogAlways(className, "getTestRunDurationInPartitonMode", "", "", "Method Called.");
      
      /*Getting all the available partition list*/
      ArrayList<String> sessionList = sessionReportData.getAvailableSessionList();
      
      Log.debugLogAlways(className, "getTestRunDurationInPartitonMode", "", "", "StartPartition fileName = " + sessionList.get(0) + ", endPartition fileName = " + sessionList.get(sessionList.size() - 1));
      
      /* Getting the start Date time stamp of partition data file. */
      long startSessionFileTimeStamp = ExecutionDateTime.convertFormattedDateToMilliscond(sessionList.get(0), DataPacketInfo.PARTITION_FILE_START_DATE_FORMAT, null);
      
      /* Getting the start Date time stamp of partition data file. */
      long endSessionFileTimeStamp = ExecutionDateTime.convertFormattedDateToMilliscond(sessionList.get(sessionList.size() - 1), DataPacketInfo.PARTITION_FILE_START_DATE_FORMAT, null);
      
      String rtgFilePath = Config.getWorkPath() + "/webapps/logs/TR" + sessionReportData.testRunNum + "/" + sessionList.get(sessionList.size() - 1);
      
      Log.debugLogAlways(className, "getTestRunDurationInPartitonMode", "", "", "startSessionFileTimeStamp = " + startSessionFileTimeStamp + ", endSessionFileTimeStamp = " + endSessionFileTimeStamp + ", rtgFilePath = " + rtgFilePath);
      
      /*duration = end - start*/
      long testRunDuration = endSessionFileTimeStamp - startSessionFileTimeStamp;

      File file = new File(rtgFilePath);
      /*if rtgMessage file not exists then return duration taken from partition files*/
      if(!file.exists())
	return testRunDuration;
      
      long rtgFileDuration = 0L;
      PartitionDataProperties partitionDataProperties = sessionReportData.getAvailableSessionDuration().get(sessionList.get(sessionList.size() - 1));
      
      if(partitionDataProperties != null)
	rtgFileDuration = partitionDataProperties.getPartitionDuration();
      
      /*if rtgMessage file exists then duration = testRunDuration + rtgMessgae.dat time's*/
      testRunDuration = testRunDuration + rtgFileDuration;
      Log.debugLogAlways(className, "getTestRunDurationInPartitonMode", "", "", "calculated duration = " + testRunDuration);
      return testRunDuration;
    }
    catch(Exception e)
    {
     e.printStackTrace();
     Log.errorLog(className, "getTestRunDurationInPartitonMode", "", "", "Error in Getting duration.");
     return 0L;
    }
  }
  
  /**
   * Getting Correlated Data requested by client GUI.
   * @param correlationRequestDTOObj
   */
  public void getCorrelatedData(CorrelationRequestDTO correlationRequestDTOObj)
  {
    try
    {
      Log.debugLogAlways(className, "getCorrelatedData", "", "", "Method Called.");
      
      /*Taking start Time.*/
      long startTimeMillies = System.currentTimeMillis();
      
      /*Checking for Correlation DTO object.*/
      if(correlationRequestDTOObj == null)
      {
	Log.errorLog(className, "getCorrelatedData", "", "", "Getting Correlation DTO object null.");
	return;
      }
   
      /*Creating object of event advisory.*/
      EventGeneratorAdvisory eventGeneratorAdvisoryObj = new EventGeneratorAdvisory(correlationRequestDTOObj.getTestRun(), correlationRequestDTOObj.getNarFileName(), 2, correlationRequestDTOObj.getStartTime(), correlationRequestDTOObj.getEndTime(), -1);
            
      /*Creating Instance of PearsonCorrelation class.*/
      PearsonCorrelation pearsonCorrelationObj = new PearsonCorrelation(correlationRequestDTOObj, eventGeneratorAdvisoryObj);
      
      /*Processing and Getting Correlated values.*/
      pearsonCorrelationObj.generatePearsonCorrelation();
      
      Log.debugLogAlways(className, "getCorrelatedData", "", "", "Correlation Data generated. Going to read data from file. status = " + correlationRequestDTOObj.isSuccessful());
      
      if(correlationRequestDTOObj.isSuccessful())
      {
	Log.debugLogAlways(className, "getCorrelatedData", "", "", "Getting Analysis Alert Data for test run = " + correlationRequestDTOObj.getTestRun());
	String fileNameWithPath = Config.getWorkPath() + "/webapps/logs/TR" + correlationRequestDTOObj.getTestRun() + "/" + "correlation_alert_history.dat";
	AnalysisAlert analysisAlertObj = new AnalysisAlert(correlationRequestDTOObj.getTestRun() + "", fileNameWithPath);
	
	String arrAlertData[][] = analysisAlertObj.getStrArrAlertFileData();
	
	if(arrAlertData == null || arrAlertData.length == 0)
	{
	  Log.errorLog(className, "getCorrelatedData", "", "", "Error in Reading Alert data.");
	  correlationRequestDTOObj.setSuccessful(false);
	  correlationRequestDTOObj.setErrorMsg("Error in Processing Correlation Request. Error in reading alert file.");
	}	
	
	correlationRequestDTOObj.setArrAlertData(arrAlertData);
	Log.debugLogAlways(className, "getCorrelatedData", "", "", "Creating Graph DTO of correlated Request.");
	
	/*Creating Array of Graph DTO.*/
	ArrayList<GraphUniqueKeyDTO> arrActiveRequestDTOList = getActiveCorrelationRequestDTOList(arrAlertData[arrAlertData.length - 1]);
	
	if(arrActiveRequestDTOList == null)
	{
	  Log.errorLog(className, "getCorrelatedData", "", "", "Error in Getting Active Request Graph DTO from File.");
	  correlationRequestDTOObj.setSuccessful(false);
	  correlationRequestDTOObj.setErrorMsg("Error in Processing Correlation Request. Please check error logs.");
	  return;
	}

	correlationRequestDTOObj.setArrProcessedGraphDTOList(arrActiveRequestDTOList);
	Log.debugLogAlways(className, "getCorrelatedData", "", "", "Filtering Time Based data based on active request.");
	
	HashMap<GraphUniqueKeyDTO, TimeBasedDTO> hmTimeBasedDTO = new HashMap<GraphUniqueKeyDTO, TimeBasedDTO>(); // To store averaged graph data by GroupId.GraphId.VectorName
	
	/*Adding Active Graph DTO only in HashMap.*/
	for(int k = 0; k < arrActiveRequestDTOList.size(); k++)
	{
	  hmTimeBasedDTO.put(arrActiveRequestDTOList.get(k), correlationRequestDTOObj.getTimeBasedTestRunDataObj().getTimeBasedDTO(arrActiveRequestDTOList.get(k)));
	}
	
	/*Setting Data into time based object of DTO.*/
	correlationRequestDTOObj.getTimeBasedTestRunDataObj().setHmTimeBasedDTO(hmTimeBasedDTO);
      }   
      Log.debugLogAlways(className, "getCorrelatedData", "", "", "Method Completed. Total Time Taken in secs = " + ((System.currentTimeMillis() - startTimeMillies)/1000));
    }
    catch(Exception e)
    {
      e.printStackTrace();
      correlationRequestDTOObj.setSuccessful(false);
      correlationRequestDTOObj.setErrorMsg("Error in Processing Correlation Request. Please check error logs.");
    }
  }
  
  /**
   * Method is used to get Graph DTO from alert correlation request.
   * @param arrAlertData
   * @return
   */
  public ArrayList<GraphUniqueKeyDTO> getActiveCorrelationRequestDTOList(String []arrAlertData)
  {
    try
    {
      Log.debugLogAlways(className, "getActiveCorrelationRequestDTOList", "", "", "Getting Active Correlation Request Graph DTO List.");
      
      if(arrAlertData == null)
      {
	Log.errorLog(className, "getActiveCorrelationRequestDTOList", "", "", "getting alert data null.");
	return null;
      }
      
      ArrayList<GraphUniqueKeyDTO> arrCorrReqGraphList = new ArrayList<GraphUniqueKeyDTO>();     
      String groupGraphVectorList = arrAlertData[7];
      
      Log.debugLogAlways(className, "getActiveCorrelationRequestDTOList", "", "", "groupGraphVectorList of Baseline Graph = " + groupGraphVectorList);
      
      /*Graph Information of GGV.*/
      String graphInfo[] = rptUtilsBean.split(groupGraphVectorList, ";");
      
      if(graphInfo.length < 3)
      {
	Log.errorLog(className, "getActiveCorrelationRequestDTOList", "", "", "Error in processing due to groupGraphVectorList = " + groupGraphVectorList);
      }
      
      GraphUniqueKeyDTO graphUniqueKeyDTO = new GraphUniqueKeyDTO(Integer.parseInt(graphInfo[0]), Integer.parseInt(graphInfo[1]), graphInfo[2]); 
      arrCorrReqGraphList.add(graphUniqueKeyDTO);
      
      String corrGraphInfo[] = rptUtilsBean.split(arrAlertData[8], ",");     
      if(corrGraphInfo == null || corrGraphInfo.length == 0)
      {
	Log.errorLog(className, "getActiveCorrelationRequestDTOList", "", "", "Error in reading correlated Graph information from file.");
	return arrCorrReqGraphList;
      }
      
      /*Getting Correlated Graphs.*/
      for(int k = 0; k < corrGraphInfo.length; k++)
      {
	groupGraphVectorList = corrGraphInfo[k];
	Log.debugLogAlways(className, "getActiveCorrelationRequestDTOList", "", "", "groupGraphVectorList of correlated Graph = " + groupGraphVectorList);
	
	/*Graph Information of GGV.*/
	graphInfo = rptUtilsBean.split(groupGraphVectorList, ";");

	if(graphInfo.length < 3)
	{
	  Log.errorLog(className, "getActiveCorrelationRequestDTOList", "", "", "Error in processing due to groupGraphVectorList = " + groupGraphVectorList);
	  continue;
	}
	
	graphUniqueKeyDTO = new GraphUniqueKeyDTO(Integer.parseInt(graphInfo[0]), Integer.parseInt(graphInfo[1]), graphInfo[2]); 
	arrCorrReqGraphList.add(graphUniqueKeyDTO);
      }
      
      return arrCorrReqGraphList;      
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }
  
  /**
   * Method is used to write in a alert file.
   * @param correlatedEventData
   * @return
   */
  public boolean addCorrelatedEventDataToFile(String correlatedEventData, String testRunNum)
  {
    Log.debugLog(className, "addCorrelatedEventDataToFile", "", "", "Method called, Event Data = " + correlatedEventData);
    
    try
    {
      String fileNameWithPath = Config.getWorkPath() + "/webapps/logs/TR" + testRunNum + "/" + "correlation_alert_history.dat";
      File analysisAlertFileObj = new File(fileNameWithPath);

      if(!analysisAlertFileObj.exists())
      {
	analysisAlertFileObj.createNewFile();
	if(analysisAlertFileObj.exists())
	{
	  Log.debugLog(className, "addCorrelatedEventDataToFile", "", "", "Analysis Alert file, " + fileNameWithPath + " created successfully.");
	}
      }

      FileOutputStream fout = new FileOutputStream(analysisAlertFileObj, true);
      PrintStream printStream = new PrintStream(fout);
      Log.debugLog(className, "addCorrelatedEventDataToFile", "", "", "Adding correlated data to file " + fileNameWithPath + ", data = " + correlatedEventData);
      printStream.println(correlatedEventData);
      printStream.flush();
      printStream.close();
      fout.close();

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "addCorrelatedEventDataToFile", "", "", "Exception - ", e);
      e.printStackTrace();
      return false;
    }
  }
  
  /**
   * This method will return last sequence number for the current partition.
   * @param partitionName
   * @return
   */
  public String getLatestGDFVersion(String partitionName, String partitionPath)
  {
    try
    {
      PartitionDataProperties partitionDataProperties = getPartitionDataProperties(partitionName, getPartitionAvailableGDFVersion(partitionPath));
      
      if(partitionDataProperties == null || partitionDataProperties.getArrPartitionSequenceFileList() == null || partitionDataProperties.getArrPartitionSequenceFileList().size() == 0)
	return "";
      
      int seqNum = partitionDataProperties.getArrPartitionSequenceFileList().get(partitionDataProperties.getArrPartitionSequenceFileList().size() - 1);
      
      return ((seqNum == 0) ? "" : seqNum + "");
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    return "";
  }
  
  
  /**
   * Method is used to check if number is numeric or not.
   * @param str
   * @author Itisha
   * @return
   */
  public static boolean isNumeric(String str)
  {
    return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
  }
 
  
  /**
   * @param args
   */
  public static void main(String[] args) 
  {
    PartitionInfoUtils partitionInfoUtils = new PartitionInfoUtils();
    SortedSet<Integer> arrVersions = partitionInfoUtils.getPartitionAvailableGDFVersion("F:\\Example\\TR2\\123456"); 
    
    for(Integer version : arrVersions)
    {
      System.out.println("Available Versions = " + version);
    }
    
    ArrayList<String> arrPartitionRTGFileList = partitionInfoUtils.getPartitionAvailableRTGFiles("F:\\Example\\TR2\\123456");
    
    for(String rtgFileName : arrPartitionRTGFileList)
    {
      System.out.println("RTG File Name = " + rtgFileName);
    } 
    
    // TODO Auto-generated method stub
    //System.out.println("value = "+partitionInfoUtils.getCurrentPartitionNameByTestRunNumber(1001));
       
  }
}

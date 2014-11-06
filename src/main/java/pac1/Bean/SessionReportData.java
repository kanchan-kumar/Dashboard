package pac1.Bean;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.commons.io.comparator.NameFileComparator;

import pac1.Bean.GraphName.GraphNames;

/**
 * This Class is used to create data from different partition based on user request. </br>
 * @version 1.0
 * 
 */
public class SessionReportData
{

  /* This is used for logging purpose. */
  String className = "SessionReportData";

  /* The Test Run Number of directory. */
  int testRunNum = 1;
  
  String generatorName = null;
  
  String generatorTRNum = null;

  /* Active partition Directory Name. */
  String activeSessionDirName = "";

  /* Initialize Graph Names object */
  GraphNames graphNamesObj = null;

  /* ArrayList containing the all available partition file name inside TestRun Directory in sorted order */
  private ArrayList<String> arrPartitionFileList = new ArrayList<String>();

  /*Linked HashMap containing all partition directory name with its Properties list like runtime(Total Duration of Partition.) */
  private LinkedHashMap<String, PartitionDataProperties> hmPartitionPropsList = new LinkedHashMap<String, PartitionDataProperties>();

  /* This object is used to read partition files. */
  private BinaryFileReader fileReader = null;

  /* This variable contain the packet size */
  private int packetSize = 0;

  /* This variable is used to store total packets in current partition data file. */
  private int totalPackets = 0;

  /* This object stores the information related to time zone. */
  private TimeZone DEFAULT_TIME_ZONE = null;

  /* This variable contains TestRun GDF version. */
  private String sessionGDFVersion = "";

  /* This variable is used for Getting net storm time EPOCH year from scenario file. */
  private String epochYear = "2014"; // Default.

  /* This variable contains the epoch time stamp. */
  private long epochTimeStamp = 0L;

  /* This variable contains the overall runtime of all available partitions. */
  private long totalDurationOfAllPartitions = 0L;
  
  /* Instance of PartitionInfoUtils for partition specific utilities.*/
  private PartitionInfoUtils partitionInfoUtilsObj = null;
  
  /**
   * This is default constructor used to initialize object.
   * 
   * @param testRunNum
   */
  public SessionReportData(int testRunNum)
  {
    this.testRunNum = testRunNum;

    /*Initializing reader for reading files.*/
    fileReader = new BinaryFileReader();
    
    /*Creating instance of Partition Utilities.*/
    partitionInfoUtilsObj = new PartitionInfoUtils(fileReader);
    
    /*Initialize and Getting Partition Data and information.*/
    initSessionsData();
  }
  
  public SessionReportData(int testRunNum , String generatorName , String generatorTRNum)
  {
    this.testRunNum = testRunNum;

    this.generatorName = generatorName;
    
    this.generatorTRNum = generatorTRNum;
    
    /*Initializing reader for reading files.*/
    fileReader = new BinaryFileReader();
    
    /*Creating instance of Partition Utilities.*/
    partitionInfoUtilsObj = new PartitionInfoUtils(fileReader);
    
    /*Initialize and Getting Partition Data and information.*/
    initSessionsData();
  }

  /**
   * Initialize sessions data and getting related information.
   */
  private void initSessionsData()
  {
    try
    {
      Log.debugLogAlways(className, "initSessionsData", "", "", "Getting sessions and initializing sessions data");

      /*Getting all available sessions.*/
      listAllSessionDirs();

      /*Getting duration of available sessions.*/
      getAllSessionDuration();

      /*Getting Epoch TimeStamp.*/
      epochTimeStamp = ExecutionDateTime.getEpochTimeStamp(epochYear, DEFAULT_TIME_ZONE);

    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   * This method is used to get Path of active partition directory.
   * @param sessionDirName
   * @return
   */
  private String getSessionDirPath(String sessionDirName)
  {
    if(generatorName == null)
    {
      Log.debugLogAlways(className, "SessionReportData:getSessionDirPath", "", "", "testRunNum = " + testRunNum + " sessionDirName = " + sessionDirName);
      return (Config.getWorkPath() + "/webapps/logs/TR" + testRunNum + "/" + sessionDirName);
    }
    else
    {
      Log.debugLogAlways(className, "SessionReportData:getSessionDirPath", "", "", "testRunNum = " + testRunNum + ", Generator Name = " + generatorName +"(" + generatorTRNum + ")" + " sessionDirName = " + sessionDirName);
      return (Config.getWorkPath() + "/webapps/logs/TR" + testRunNum + "/NetCloud/" + generatorName.trim() + "/TR" + generatorTRNum + "/" + sessionDirName);
    }
  }

  /**
   * This Method is used to get path of partition data files.
   * @param sessionDirName
   * @param rtgFileName
   * @return
   */
  private String getSessionDataFilePath(String sessionDirName, String rtgFileName)
  {
    Log.debugLogAlways(className, "SessionReportData:getSessionDataFilePath", "", "", "Method Called. sessionDirName = " + sessionDirName + " rtgFileName = " + rtgFileName);

    // Here first we get the file path of active partition rtgMessage.dat
    // TODO - one partition directory may contain multiple rtgMessage.dat we need to dynamically handle it.
    return getSessionDirPath(sessionDirName) + "/" + rtgFileName;

  }
  
  /**
   * This Method is used to get path of partition GDF data files.
   * @param partitionDirName
   * @return
   */
  private String getPartitionGDFFilePath(String partitionDirName)
  {
    Log.debugLogAlways(className, "SessionReportData:getPartitionGDFFilePath", "", "", "Method Called. partitionDirName = " + partitionDirName);

    // Here first we get the file path of partition
    // TODO - one partition directory may contain multiple testrun.gdf we need to dynamically handle it.
    return getSessionDirPath(partitionDirName) +  "/testrun.gdf";

  }

  /**
   * Getting the Details of partition data files for creating the data, for last N minutes.
   */
  public SessionDataInfo getSessionInfoForLastNMinute(int lastNMinutes)
  {
    try
    {
      Log.debugLogAlways(className, "getSessionInfoForLastNMinute", "", "", "Method Called. Total partition Files = " + arrPartitionFileList.size() + " lastNMinutes = " + lastNMinutes + ", totalDurationOfAllPartitions = " + ExecutionDateTime.convertTimeToFormattedString(totalDurationOfAllPartitions, ":"));

      // Getting the absolute start and end millies for last n minutes.
      String activeSessionName = arrPartitionFileList.get(arrPartitionFileList.size() - 1); // Sorted By Name.

      // Getting the start Date time stamp of partition data file.
      long startTimeStamp = ExecutionDateTime.convertFormattedDateToMilliscond(activeSessionName, DataPacketInfo.PARTITION_FILE_START_DATE_FORMAT, DEFAULT_TIME_ZONE);

      // Getting the total duration in millies for Last n Minutes.
      long LastNInMillies = lastNMinutes * 60 * 1000;

      // Getting the duration of partition data file.
      long durationInMillies = hmPartitionPropsList.get(activeSessionName).getPartitionDuration();

      Log.debugLogAlways(className, "getSessionInfoForLastNMinute", "", "", "NDE Session Running Time = " + ExecutionDateTime.convertDateTimeStampToFormattedString(startTimeStamp + durationInMillies, "MM/dd/yy HH:mm:ss", DEFAULT_TIME_ZONE) + " , From Date = " + ExecutionDateTime.convertDateTimeStampToFormattedString(ExecutionDateTime.convertFormattedDateToMilliscond(arrPartitionFileList.get(0), DataPacketInfo.PARTITION_FILE_START_DATE_FORMAT, DEFAULT_TIME_ZONE), "MM/dd/yy HH:mm:ss", DEFAULT_TIME_ZONE));

      // Getting the end time stamp of active partition data file
      long endTimeStampForLastNMinute = 0L;

      // Now calculate the Absolute Start Time for making/creating data for Last N Minute.
      long startTimeStampForLastNMinute = 0L;

      // Calculating End Time For Requested Last N Minutes.
      if (totalDurationOfAllPartitions >= LastNInMillies)
      {
        endTimeStampForLastNMinute = getEndTimeMilliesOfSession(startTimeStamp, durationInMillies);
        startTimeStampForLastNMinute = endTimeStampForLastNMinute - LastNInMillies;
      }
      else
      {
        long timeStampOfFirstSession = ExecutionDateTime.convertFormattedDateToMilliscond(arrPartitionFileList.get(0), DataPacketInfo.PARTITION_FILE_START_DATE_FORMAT, DEFAULT_TIME_ZONE);
        endTimeStampForLastNMinute = timeStampOfFirstSession + LastNInMillies;
        startTimeStampForLastNMinute = timeStampOfFirstSession;
      }

      Log.debugLogAlways(className, "getSessionInfoForLastNMinute", "", "", "start = " + startTimeStamp + ", duration = " + durationInMillies + ", end = " + endTimeStampForLastNMinute + " LastNInMillies = " + LastNInMillies);

      // This method search the file which contains the input time stamp.
      String startSessionFile = getSessionFileContainsTimeStamp(startTimeStampForLastNMinute);

      // Creating the instance of partition data info.
      SessionDataInfo sessionDataInfoObj = new SessionDataInfo();

      // Setting the info in object.
      sessionDataInfoObj.setEndTimeStamp(endTimeStampForLastNMinute);
      sessionDataInfoObj.setStartTimeStamp(startTimeStampForLastNMinute);
      sessionDataInfoObj.setSessionDataFile(activeSessionName);
      sessionDataInfoObj.setLastSessionDataFile(activeSessionName);

      if (startSessionFile.trim().equals("NA"))
      {
        Log.errorLog(className, "getSessionInfoForLastNMinute", "", "", "Last " + lastNMinutes + " Minutes data not available. Taking First partition as start partition.");

        // TODO - Handle other cases like missing sessions or paused sessions.
        startSessionFile = arrPartitionFileList.get(0);
      }

      Log.debugLogAlways(className, "getSessionInfoForLastNMinute", "", "", "Last N Start Time = " + ExecutionDateTime.convertDateTimeStampToFormattedString(startTimeStampForLastNMinute, "MM/dd/yy HH:mm:ss", DEFAULT_TIME_ZONE));
      Log.debugLogAlways(className, "getSessionInfoForLastNMinute", "", "", "Last N end Time = " + ExecutionDateTime.convertDateTimeStampToFormattedString(endTimeStampForLastNMinute, "MM/dd/yy HH:mm:ss", DEFAULT_TIME_ZONE));
      Log.debugLogAlways(className, "getSessionInfoForLastNMinute", "", "", "start session file = " + startSessionFile + " endSession file = " + activeSessionName);

      // Setting First partition file to read.
      sessionDataInfoObj.setFirstSessionDataFile(startSessionFile);

      return sessionDataInfoObj;

    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Getting the Details of partition data files for creating the data between two specified dates.
   * @param startTimeStamp
   * @param endTimeStamp
   * @return
   */
  public SessionDataInfo getSessionInfoForSpecifiedDates(long startTimeStamp, long endTimeStamp)
  {
    try
    {
      Log.debugLogAlways(className, "getSessionInfoForSpecifiedDates", "", "", "Method Called. Total partition Files = " + arrPartitionFileList.size() + " startTimeStamp = " + startTimeStamp + " endTimeStamp = " + endTimeStamp);

      // Creating the instance of partition data info.
      SessionDataInfo sessionDataInfoObj = new SessionDataInfo();

      // Setting the info in object.
      sessionDataInfoObj.setEndTimeStamp(endTimeStamp);
      sessionDataInfoObj.setStartTimeStamp(startTimeStamp);

      // Search the partition file which contains the input time stamp.
      String startSessionFile = getSessionFileContainsTimeStamp(startTimeStamp);
      
      /*If Start partition file is not found then assuming it to as first partition file.*/
      if(startSessionFile.trim().equals("NA"))
	startSessionFile = arrPartitionFileList.get(0);

      // Setting First partition file to read.
      sessionDataInfoObj.setFirstSessionDataFile(startSessionFile);

      // Search the partition file which contains the input time stamp.
      String endSessionFile = getSessionFileContainsTimeStamp(endTimeStamp);
      
      /*If End partition file is not found then assuming it to as last active partition file.*/
      if(endSessionFile.trim().equals("NA"))
	endSessionFile = arrPartitionFileList.get(arrPartitionFileList.size() - 1);

      // No Need to save active partition for specified minutes.
      sessionDataInfoObj.setSessionDataFile("NA");

      sessionDataInfoObj.setLastSessionDataFile(endSessionFile);

      Log.debugLogAlways(className, "getSessionInfoForSpecifiedDates", "", "", "Specified Start Time = " + ExecutionDateTime.convertDateTimeStampToFormattedString(startTimeStamp, "MM/dd/yy HH:mm:ss", DEFAULT_TIME_ZONE));
      Log.debugLogAlways(className, "getSessionInfoForSpecifiedDates", "", "", "Specified end Time = " + ExecutionDateTime.convertDateTimeStampToFormattedString(endTimeStamp, "MM/dd/yy HH:mm:ss", DEFAULT_TIME_ZONE));
      Log.debugLogAlways(className, "getSessionInfoForSpecifiedDates", "", "", "start session file = " + startSessionFile + " endSession file = " + endSessionFile);

      return sessionDataInfoObj;

    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }
  
  
  /**
   * This mode is only applicable with NS/NDE partition Mode, Invalid Request For Continuous Monitoring.
   * @return
   */
  public SessionDataInfo getSessionInfoForWholeScenario()
  {
    try
    {
      Log.debugLogAlways(className, "getSessionInfoForWholeScenario", "", "", "Method Called. Total partition Files = " + arrPartitionFileList.size());
      
      //Creating the instance of partition data info.
      SessionDataInfo sessionDataInfoObj = new SessionDataInfo();
      
      long startTimeStamp = ExecutionDateTime.convertFormattedDateToMilliscond(arrPartitionFileList.get(0), DataPacketInfo.PARTITION_FILE_START_DATE_FORMAT, DEFAULT_TIME_ZONE);
      String endSessionFile = arrPartitionFileList.get(arrPartitionFileList.size() - 1);
      long endTimeStamp = ExecutionDateTime.convertFormattedDateToMilliscond(endSessionFile, DataPacketInfo.PARTITION_FILE_START_DATE_FORMAT, DEFAULT_TIME_ZONE) + hmPartitionPropsList.get(endSessionFile).getPartitionDuration();
      
      //Setting the info in object.
      sessionDataInfoObj.setEndTimeStamp(endTimeStamp);
      sessionDataInfoObj.setStartTimeStamp(startTimeStamp);
      
      //Setting First and Last partition file to read.
      sessionDataInfoObj.setFirstSessionDataFile(arrPartitionFileList.get(0));
      sessionDataInfoObj.setLastSessionDataFile(endSessionFile);
      
      Log.debugLogAlways(className, "getSessionInfoForWholeScenario", "", "", "Specified Start Time = " + ExecutionDateTime.convertDateTimeStampToFormattedString(startTimeStamp, "MM/dd/yy HH:mm:ss", DEFAULT_TIME_ZONE));
      Log.debugLogAlways(className, "getSessionInfoForWholeScenario", "", "", "Specified end Time = " + ExecutionDateTime.convertDateTimeStampToFormattedString(endTimeStamp, "MM/dd/yy HH:mm:ss", DEFAULT_TIME_ZONE));
      Log.debugLogAlways(className, "getSessionInfoForWholeScenario", "", "", "start session file = " + arrPartitionFileList.get(0) + " endSession file = " + endSessionFile);
      
      return sessionDataInfoObj;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * This method find out the partition file name which we need to read.
   * 
   * @param timeStamp
   * @return
   */
  private String getSessionFileContainsTimeStamp(long timeStamp)
  {
    try
    {
      Log.debugLogAlways(className, "getSessionFileContainsTimeStamp", "", "", "Method Called with input time stamp = " + timeStamp);

      //Checking sessions.
      if (arrPartitionFileList.size() == 0)
      {
        Log.debugLogAlways(className, "getSessionFileContainsTimeStamp", "", "", "Sessions is not available in TestRun Directory.");
        return "NA";
      }

      //To Check Time stamp between two partitions.
      long prevEndTimeStamp = Long.MAX_VALUE;

      //Checking time stamp in each partition.
      for(int i = 0; i < arrPartitionFileList.size(); i++)
      {
        String sessionFile = arrPartitionFileList.get(i);

        //Getting Start time stamp of partition.
        long sessionStartTimeStamp = ExecutionDateTime.convertFormattedDateToMilliscond(sessionFile, DataPacketInfo.PARTITION_FILE_START_DATE_FORMAT, DEFAULT_TIME_ZONE);

        //Getting End Time Stamp of partition.
        long sessionEndTimeStamp = sessionStartTimeStamp + hmPartitionPropsList.get(sessionFile).getPartitionDuration();

        if (sessionEndTimeStamp >= timeStamp && sessionStartTimeStamp <= timeStamp)
          return sessionFile;
        else if (timeStamp >= prevEndTimeStamp && timeStamp <= sessionStartTimeStamp) //Handling the session stop case(gap between two sessions).
          return arrPartitionFileList.get(i - 1);

        prevEndTimeStamp = sessionEndTimeStamp;
      }

      Log.debugLogAlways(className, "getSessionFileContainsTimeStamp", "", "", "Time stamp not found in partition files. TimeStamp = " + timeStamp);
      return "NA";

    }
    catch (Exception e)
    {
      e.printStackTrace();
      return "NA";
    }
  }

  /**
   * Getting the endTime of partition in millies.
   * 
   * @param startTimeMillies
   * @param durationMillies
   */
  private long getEndTimeMilliesOfSession(long startTimeMillies, long durationMillies)
  {
    Log.debugLogAlways(className, "getEndTimeMilliesOfSession", "", "", "Method Called. startTimeMillies = " + startTimeMillies + " durationMillies = " + durationMillies);
    return startTimeMillies + durationMillies;
  }

  /**
   * Create GraphNames object for partition.
   * 
   * @param testRunNum
   * @param activeSessionDirName
   * @param sessionGDFVersion
   * @return
   */
  
  public GraphNames createGraphNamesObj(int testRunNum, String activeSessionDirName, String sessionGDFVersion)
  {
    if(generatorName == null)
    {
      Log.debugLogAlways(className, "createGraphNamesObj", "", "", "Method Called. activeSessionDirName = " + activeSessionDirName + " testRunNum = " + testRunNum + " sessionGDFVersion = " + sessionGDFVersion);
      return new GraphNames(testRunNum, null, null, "NA", "", activeSessionDirName, sessionGDFVersion, false);
    }
    else
    {
      Log.debugLogAlways(className, "createGraphNamesObj", "", "", "Method Called. activeSessionDirName = " + activeSessionDirName + ", " + getTRTitle() +" sessionGDFVersion = " + sessionGDFVersion);
      return new GraphNames(Integer.parseInt(generatorTRNum) , null, null, String.valueOf(testRunNum) ,generatorName, activeSessionDirName, sessionGDFVersion, false);
    }
  }

  /**
   * This filter only returns directories.
   */
  FileFilter sessionDirFilter = new FileFilter()
  {
    @Override
    public boolean accept(File file)
    {
      //Pattern for only numeric name file.
      Pattern pattern = Pattern.compile(".*[0-9].*");
      
      /*Validate through Partition File Name Format.*/
      String partitionFileName = file.getName();

      /*Validate the Partition Name for valid Partition Directory.*/
      if(file.isDirectory() && ExecutionDateTime.isValidDate(partitionFileName, DataPacketInfo.PARTITION_FILE_START_DATE_FORMAT))
        return true;
      else
        return false;
    }
  };

  private String getTRTitle() 
  {
    if(generatorName == null)
      return "Test Run  = " + testRunNum;
    else
      return "TestRun = " + testRunNum + ", Generator = " + generatorName + "("+generatorTRNum+")";
  }
  /**
   * This method is used to list all the directories of partition in sorted order.
   */
  private void listAllSessionDirs()
  {
    try

    {
      // Getting the instance of active TestRun Directory.
      File testRunDir = new File(getSessionDirPath(""));

      // Getting All Directories inside active test run dirs.
      List<File> sessionDirs = Arrays.asList(testRunDir.listFiles(sessionDirFilter));

      // Checking for sessions.
      if (sessionDirs == null || sessionDirs.size() == 0)
      {
        Log.errorLog(className, "listAllSessionDirs", "", "", "No partition available inside active " + getTRTitle() + ".");
        return;
      }

      // Sort the Directories according to name.
      NameFileComparator nameComparator = new NameFileComparator();
      sessionDirs = nameComparator.sort(sessionDirs);

      //Gets all the partition directory name in sorted order.
      for (File dir : sessionDirs)
      {
        //TODO - Is test run contains any other directory other than partition directories. If yes then we need to filter it.
        arrPartitionFileList.add(dir.getName());
      }

    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Gets the duration for each partition.
   */
  private void getAllSessionDuration()
  {
    try
    {
      //Getting the Duration of each partition from summary.top file.
      for (int i = 0; i < arrPartitionFileList.size(); i++)
      {
        //Get Path of partition directory.
        String sessionDirPath = getSessionDirPath(arrPartitionFileList.get(i));

        //Getting Partition Properties like size, sequence/version list etc.
        PartitionDataProperties partitionDataProperties = getPartitionDataPropertiesFromSessionDir(sessionDirPath);
                  
        // For Getting all available run time of test run.
        totalDurationOfAllPartitions = totalDurationOfAllPartitions + partitionDataProperties.getPartitionDuration();
        
        //Saving properties in hashMap
        hmPartitionPropsList.put(arrPartitionFileList.get(i), partitionDataProperties);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   * This method is used to get duration from partition Directory, Summary.top file.
   * 
   * @param partitonDirPath
   * @return
   */
  private PartitionDataProperties getDurationFromSessionDir(String partitonDirPath)
  {
    try
    {
      Log.debugLogAlways(className, "getDurationFromSessionDir", "", "", "Method Called. partitonDirPath = " + partitonDirPath);

      // TODO - Not using it as it may be possible that duration is not correct in summary.top file.
      // duration = getDurationOfPartitionFromSummaryFile(partitonDirPath);
      // long durationInMillies = ExecutionDateTime.convertFormattedTimeToMillisecond(duration, ":");
      return partitionInfoUtilsObj.getPartitionDataProperties(partitonDirPath, partitionInfoUtilsObj.getPartitionAvailableGDFVersion(partitonDirPath));
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * This method was used to get sequence/version, size of each sequence/version files for the given session directory path
   * @param partitonDirPath
   * @return
   */
  private PartitionDataProperties getPartitionDataPropertiesFromSessionDir(String partitonDirPath)
  {
    try
    {
      Log.debugLogAlways(className, "getPartitionDataPropertiesFromSessionDir", "", "", "Method Called. partitonDirPath = " + partitonDirPath);
      
      /*Getting Available Sequences/Version of Partition.*/
      SortedSet<Integer> partitionFileSequenceList = partitionInfoUtilsObj.getPartitionAvailableGDFVersion(partitonDirPath);
      
      /*Checking for Availability of Sequences/Version inside partition.*/
      if(partitionFileSequenceList == null || partitionFileSequenceList.size() == 0)
	return new PartitionDataProperties();
      else
        return partitionInfoUtilsObj.getPartitionDataProperties(partitonDirPath, partitionFileSequenceList);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }
  
  /**
   * This method checks the existence of partition Data Files.
   * 
   * @param sessionDataFilePath
   * @return
   */
  private boolean checkSessionRTGMessageFile(String sessionDataFilePath)
  {
    try
    {
      return (new File(sessionDataFilePath)).exists();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return false;
    }
  }
  
  
  /**
   * This method is used to read partition data files and creates time based data by reading the filters provided as SessionDataInfo.
   * Method Ignores Bad Partition(if testrun.gdf or rtgMessage.dat not available).
   * @param sessionDataInfo
   * @param timeBasedTestRunDataObj
   * @return
   */
  public TimeBasedTestRunData readFileAndCreateTimeBasedData(SessionDataInfo sessionDataInfo, TimeBasedTestRunData timeBasedTestRunDataObj)
  {
    try
    {
      Log.debugLogAlways(className, "readFileAndCreateTimeBasedData", "", "", "Method Called.");

      if(sessionDataInfo == null)
      {
        Log.errorLog(className, "readFileAndCreateTimeBasedData", "", "", "partition Data Info object must not be null.");
        return null;
      }

      if(timeBasedTestRunDataObj == null)
      {
        Log.errorLog(className, "readFileAndCreateTimeBasedData", "", "", "TimeBasedTestRunData object must not be null.");
        return null;
      }

      /*Getting index of starting partition file.*/
      int startFileIndex = arrPartitionFileList.indexOf(sessionDataInfo.getFirstSessionDataFile());

      /*Getting index of last partition file.*/
      int endFileIndex = arrPartitionFileList.indexOf(sessionDataInfo.getLastSessionDataFile());

      Log.debugLogAlways(className, "readFileAndCreateTimeBasedData", "", "", "FirstSessionDataFile = " + sessionDataInfo.getFirstSessionDataFile() + ", lastSessionDataFile = " + sessionDataInfo.getLastSessionDataFile() + ", startFileIndex = " + startFileIndex + ", endFileIndex = " + endFileIndex);
      
      if(startFileIndex == -1 || endFileIndex == -1)
      {
        Log.errorLog(className, "readFileAndCreateTimeBasedData", "", "", "partition Data File Not Found. startFileIndex = " + startFileIndex + " endFileIndex = " + endFileIndex);
        return null;
      }

      Log.debugLogAlways(className, "readFileAndCreateTimeBasedData", "", "", "startFileIndex = " + startFileIndex + " endFileIndex = " + endFileIndex);

      /*Creating objects of data for reading binary(byte) data and processing it.*/
      data msgObj = new data();
      data prevMsgObj = new data();

      /*Taking the end time stamp.*/
      long endTimeStamp = sessionDataInfo.getEndTimeStamp();
      
      /*Keeping Track of previous partition file.*/
      String prevPartitionFile = "";
      
      /*Variable used to keep track of no.of packets left to skip in start partition*/
      int skipPackets = 0;
      
      /*Used to calculate no.of packets to skip only once*/
      boolean isCalculateNumPackets = false;
      
      /*Tracking of one packet time stamp*/
      long packetTimeStamp = 0L;
      
      /*Keeping Track of previous file sequence/version number.*/
      int prevFileVersionNumber = 0;
      
      /*Keeping Track on Partition Change Status.*/
      boolean isPartitionChanged = false;
      
      /*Now Start Creating data.*/
      for(int k = startFileIndex; (k <= endFileIndex && k < arrPartitionFileList.size()); k++)
      {
	/*Getting Partition Name.*/
        String partitionFileName = arrPartitionFileList.get(k);
        
        /*Setting the Partition Changed status.*/
        isPartitionChanged = true;
        
        /*File Version of Previous Partition.*/
        String prevPartitionFileVersion = "";
        
        /*Checking the version of previous partition file.*/
        if(prevFileVersionNumber > 0)
          prevPartitionFileVersion = "." + prevFileVersionNumber;
        
        /*Initialize it on partition changed.*/
        prevFileVersionNumber = 0;
                
        /*Getting Partition Properties.*/
        PartitionDataProperties partitionDataProperties = hmPartitionPropsList.get(partitionFileName);
        
        /*Checking For Partition Files Availability.*/
        if((partitionDataProperties == null) || (partitionDataProperties.getArrPartitionSequenceFileList() == null) || (partitionDataProperties.getArrPartitionSequenceFileList().size() == 0))
        {
          Log.errorLog(className, "readFileAndCreateTimeBasedData", "", "", "partition " + partitionFileName + " doesn't contains either rtg,GDF file or both.");
          continue;
        }
        
        /*Getting the starting time stamp of partition start.*/
        //TODO - Need to get TimeStamp From partition File.
        long sessionStartTimeStamp = ExecutionDateTime.convertFormattedDateToMilliscond(partitionFileName, DataPacketInfo.PARTITION_FILE_START_DATE_FORMAT, DEFAULT_TIME_ZONE);
        
        /*Getting Partition Sequence File List.*/
        ArrayList<Integer> partitionFileVersionList = partitionDataProperties.getArrPartitionSequenceFileList();
        
        /*Iterating through Partition File Sequence/version list.*/
        for (int i = 0; i < partitionFileVersionList.size(); i++) 
        {
          /*Getting File Version Number.*/
          int fileVersionNumber = partitionFileVersionList.get(i);
          
          String gdfFileSequencePath = getPartitionGDFFilePath(partitionFileName);
          String rtgFileSequencePath = getSessionDataFilePath(partitionFileName, "rtgMessage.dat");
          
          /*Checking for file Version.*/
          if(fileVersionNumber > 0)
          {
            /*Appending Version/Sequence to GDF file.*/
            gdfFileSequencePath = gdfFileSequencePath + "." + fileVersionNumber;

            /*Appending Version/Sequence to RTG file.*/
            rtgFileSequencePath = rtgFileSequencePath + "." + fileVersionNumber;
          }
          
          Log.debugLogAlways(className, "readFileAndCreateTimeBasedData", "", "", "GDF File Path = " + gdfFileSequencePath + ", RTG File Path = " + rtgFileSequencePath + ", sequenceNumber = " + fileVersionNumber + ", prevPartitionFileVersion = " + prevPartitionFileVersion);
          
          /*Checking for TestRun GDF file Existence in Partition.*/
          if(!fileReader.isFileAvailable(new File(gdfFileSequencePath)))
          {
            Log.errorLog(className, "readFileAndCreateTimeBasedData", "", "", "partition " + partitionFileName + " GDF File (testrun.gdf) is not exist OR length is 0. Skipping Partition File Version. GDF File Path = " + gdfFileSequencePath);
            continue;
          }
          
          /*Checking for rtgMessage file Existence in Partition.*/
          if(!fileReader.isFileAvailable(new File(rtgFileSequencePath)) || (partitionInfoUtilsObj.getSizeOfSessionDataFile(rtgFileSequencePath) <= 0))
          {
            Log.errorLog(className, "readFileAndCreateTimeBasedData", "", "", "partition " + partitionFileName + " , and File Version/Sequence = " + fileVersionNumber + " rtg File is not exist OR length is 0. At path = " + rtgFileSequencePath + ". Skipping Partition File Version.");
            continue;
          }
                             
          /*Checking for graph Names object.*/
          /*TODO - Here we need to check if testrun.gdf version is changed.*/
          /*Create Graph Names object for First Partition, and not created for others, till the GDF is changed.*/
          if((graphNamesObj == null) || (k == startFileIndex) || (fileVersionNumber != prevFileVersionNumber) || (isPartitionChanged && partitionInfoUtilsObj.isGDFChanged(gdfFileSequencePath, getPartitionGDFFilePath(prevPartitionFile) + prevPartitionFileVersion)))
          {
            Log.debugLogAlways(className, "readFileAndCreateTimeBasedData", "", "", "Creating Graph Names object and initializing data object For Partition " + partitionFileName + ", and File Version Number = " + fileVersionNumber);
            long startTime = System.currentTimeMillis();
            
            /*Creating GraphNames object.*/
            graphNamesObj = createGraphNamesObj(testRunNum, partitionFileName, fileVersionNumber+"");
                        
            /*Checking for Graph Names object.*/
            if(graphNamesObj == null)
            {
              Log.errorLog(className, "readFileAndCreateTimeBasedData", "", "", "Error in creating GraphNames object for Partition " + partitionFileName + " and sequence number = " + fileVersionNumber + ". Skipping Partition File Version.");
              continue;
            }
            
            /*Setting GDF and PDF files based on Controller/Generator Setting.*/
            if(generatorName == null)
            {
              timeBasedTestRunDataObj.getTestRunDataObj().setTestRunGDF(graphNamesObj.getGdfData());
              timeBasedTestRunDataObj.getTestRunDataObj().setTestRunPDF(graphNamesObj.getPdfData());
              timeBasedTestRunDataObj.getTestRunDataObj().graphNames = graphNamesObj;
            }
            else
            {
              timeBasedTestRunDataObj.getTestRunDataObj().setVectGenGDF(graphNamesObj.getGdfData());
              timeBasedTestRunDataObj.getTestRunDataObj().setVectGenPDF(graphNamesObj.getPdfData());
              timeBasedTestRunDataObj.getTestRunDataObj().generatorGraphNames = graphNamesObj;
            }
            
            Log.debugLogAlways(className, "readFileAndCreateTimeBasedData", "", "", "Total Time Taken to create Graph Names object for partition = " + activeSessionDirName + " is = " + ExecutionDateTime.convertTimeToFormattedString((System.currentTimeMillis() - startTime), ":"));
            
            /*Setting Graph Names object to time based object also.*/
            timeBasedTestRunDataObj.setGraphNamesObj(graphNamesObj);
            
            /*Invalidate The Available Graph Numbers.*/
            timeBasedTestRunDataObj.invalidateGraphDataIndex();

            /*Initialization of data object.*/
            msgObj.initForNewTimeBasedData(graphNamesObj);
            
            Log.debugLogAlways(className, "readFileAndCreateTimeBasedData", "", "", "Updation of Graph Names object dependency done.");
          }
          
          /*Getting Packet Size.*/
          packetSize = graphNamesObj.getSizeOfMsgData();
          
          /*Set Active Partition Name in Time Based.*/
          timeBasedTestRunDataObj.setActiveParitionName(partitionFileName);
          
          /*Set Active partition GDF version*/
          timeBasedTestRunDataObj.setCurrentPartitionGDFVersion(fileVersionNumber);
          
          /*Getting Total Packets of partition Data File.*/
          if(partitionDataProperties.getArrPartitionSequenceFileSizeList().get(i) > 0)
            totalPackets = (int) ((partitionDataProperties.getArrPartitionSequenceFileSizeList().get(i) / packetSize) - 1); /*Excluding Start Packet.*/
          else
          {
            Log.errorLog(className, "readFileAndCreateTimeBasedData", "", "", "Error in Getting Size of Partition RTG File Version. File Path = " + rtgFileSequencePath + ", Initailzation Failed. Skipping Partition Sequence File.");
            continue;
          }
          
          /*opening streams for reading file.*/
          fileReader.openFileStream(rtgFileSequencePath);

          /*Processing header of partition data file.*/
          readPacketDataFromSessionFile(msgObj, prevMsgObj, data.START_PKT, sessionStartTimeStamp, partitionFileName);
                    
          /*Now iterating with each partition until End Time stamp found.*/
          packetTimeStamp = sessionStartTimeStamp;

          /*Skipping the Number of bytes if it is starting partition file.*/
          if(partitionFileName.trim().equals(sessionDataInfo.getFirstSessionDataFile()))
          {
            /*Need to calculate no.of packets to skip only once for request*/
            if(!isCalculateNumPackets)
            {
              skipPackets = getNumberOfSkipPackets(sessionStartTimeStamp, sessionDataInfo.getStartTimeStamp(), msgObj.getInterval());             
              Log.debugLogAlways(className, "readFileAndCreateTimeBasedData", "", "", "Total Packets to skip = " + skipPackets);
              isCalculateNumPackets = true;
            }
            
            /*Checking for Partition File Packets to skip.*/
            if(skipPackets >= totalPackets)
            {
              skipPackets = skipPackets - totalPackets;
              Log.debugLogAlways(className, "readFileAndCreateTimeBasedData", "", "", "Skipping Partition File Sequence Due to Remaining Total Skip Packets = " + skipPackets + ", Partition Sequence/version File Packets = " + totalPackets);
              continue;
            }
            
            /*Checking For Skipping Packets from Partition Version/Sequence File.*/
            if(skipPackets > 0)
            {        
              /*Calculate total number of bytes to skip.*/
              int skipBytes = skipPackets * packetSize;

              /*skipping number of bytes from files.*/
              fileReader.skipBytesFromInputStream(skipBytes);
              
              /*Reinitialize Skip Packets.*/
              skipPackets = 0;

              /*If some packets are skipped then set the start time stamp as requested time stamp.*/
              packetTimeStamp = sessionDataInfo.getStartTimeStamp();

              Log.debugLogAlways(className, "readFileAndCreateTimeBasedData", "", "", "Total Skipping bytes = " + skipBytes + " from current partition Sequence/Version File = " + rtgFileSequencePath);
            }
          }
          
          do  /*Now start putting data into time based object.*/
          {
            /*Reading packet.*/
            if(readPacketDataFromSessionFile(msgObj, prevMsgObj, data.DATA_PKT, sessionStartTimeStamp, partitionFileName))
            {
              /*Putting data object to time based object to create time based data.*/
              timeBasedTestRunDataObj.putTimeBasedTestRunData(msgObj);
            }
            else
            {
              continue;
            }
            
            packetTimeStamp = (long) msgObj.getAbsTimeStamp();
          }
          while((packetTimeStamp <= endTimeStamp) && (!fileReader.getEOF()));
          
          //TODO - Post Processing Tasks.
          /*Close all opened File Stream.*/
          fileReader.closeFileStream();
          
          /*Setting Previous Partition Value.*/
          prevPartitionFile = partitionFileName;
          
          /*Setting Previous Partition Sequence Number.*/
          prevFileVersionNumber = fileVersionNumber;
          
          /*Tracking Partition Changed Status.*/
          isPartitionChanged = false;
          
          /*Now we have multiple version/sequence files in one partition directory so, on getting end time stamp we no need to read remaining partition version/sequence files*/
          if(packetTimeStamp >= endTimeStamp)
            return timeBasedTestRunDataObj;
	}
      }
      return timeBasedTestRunDataObj;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Getting Total Packets in partition Data File.
   * @param sessionDataFileName
   */
  private void getTotalPackets(String sessionDirFile)
  {
    try
    {
      long sessionDataFileSize = partitionInfoUtilsObj.getSizeOfSessionDataFile(getSessionDataFilePath(sessionDirFile, "rtgMessage.dat"));

      /*Calculating total packets.*/
      if (sessionDataFileSize > 0)
      {
        totalPackets = (int) (sessionDataFileSize / packetSize);
      }
    }
    catch (Exception e)
    {
      Log.errorLog(className, "getTotalPackets", "", "", "Error in Getting Total Packets for sessionDirFile = " + sessionDirFile);
      e.printStackTrace();
    }
  }

  /**
   * Getting Number of bytes to skip from partition data file.
   * @param sessionTimeStamp
   * @param userTimeStamp
   * @param interval
   * @return
   */
  private int getNumberOfSkipPackets(long sessionTimeStamp, long userTimeStamp, long interval)
  {
    try
    {
      Log.debugLogAlways(className, "getNumberOfSkipPackets", "", "", "Method Called. sessionTimeStamp = " + sessionTimeStamp + " userTimeStamp = " + userTimeStamp + " interval = " + interval);

      /*Getting difference in time between time stamp.*/
      long timeStampDiff = userTimeStamp - sessionTimeStamp;

      /*Checking for error case.*/
      if (timeStampDiff <= 0)
      {
        Log.errorLog(className, "getNumberOfSkipPackets", "", "", "timeStampDiff = " + timeStampDiff);
        return 0;
      }

      return (int) (timeStampDiff / interval);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return 0;
    }
  }

  /**
   * This method read packet data from rtgMessage.dat of different partition files.
   * @param msgObj
   * @param prevMsgObj
   * @param opcode
   * @param timeStamp
   * @param sessionDataFile
   * @return
   */
  public boolean readPacketDataFromSessionFile(data msgObj, data prevMsgObj, int opcode, long timeStamp, String sessionDataFile)
  {
    try
    {
      Log.debugLogAlways(className, "readPacketDataFromSessionFile", "", "", "Method called. packet Opcode = " + opcode + ",  TimeStamp = " + timeStamp);

      byte arrBytes[] = new byte[packetSize];

      /*Read bytes in byte array and validate it with packet size.*/
      if(fileReader.readPackets(arrBytes) != packetSize)
      {
        Log.errorLog(className, "readPacketDataFromSessionFile", "", "", "Complete packet not read for " + getTRTitle() + ". Ignored. Opcode = " + opcode + ", timeStamp = " + timeStamp + ". pktSize = " + packetSize + " sessionDataFile = " + sessionDataFile + ", seqNum = " + msgObj.getSeqNum());
        return false;
      }
      
      /*Processing bytes and get raw data.*/
      msgObj.genRawData(arrBytes, prevMsgObj);
      
      /*Checking opcode and sequence number.*/
      if(msgObj.getOpcode() != opcode)
        Log.errorLog(className, "readPacketDataFromSessionFile", "", "", "rgtMessage file may not be correct. Opcode not matching. Ignored");

      return true;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return false;
    }
  }
  
  /**
   * Checking for valid partition with GDF.
   * @param partitionName
   * @return
   */
  public boolean isValidPartitionWithGDF(String partitionName)
  {
    try
    {
      PartitionDataProperties partitionDataProperties = hmPartitionPropsList.get(partitionName);
      if((partitionDataProperties == null) || (partitionDataProperties.getArrPartitionSequenceFileList() == null) || (partitionDataProperties.getArrPartitionSequenceFileList().size() == 0))
	return false;
      else
	return true; 
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * This method will return last sequence number for the current partition.
   * @param partitionName
   * @return
   */
  public String getLatestGDFVersion(String partitionName)
  {
    try
    {
      PartitionDataProperties partitionDataProperties = hmPartitionPropsList.get(partitionName);
      
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
  
  /************************************************ Setters and Getters ***************************************************/

  /**
   * Setting the time zone.
   * @param timeZone
   */
  public void setTimeZone(TimeZone timeZone)
  {
    DEFAULT_TIME_ZONE = timeZone;
  }

  /**
   * This method returns the current GDF version which active partition used.
   * @return
   */
  public String getSessionGDFVersion()
  {
    return sessionGDFVersion;
  }

  /**
   * This method sets the GDF version of partition.
   * @param sessionGDFVersion
   */
  public void setSessionGDFVersion(String sessionGDFVersion)
  {
    this.sessionGDFVersion = sessionGDFVersion;
  }

  /**
   * This method returns the list of available sessions.
   * @return
   */
  public ArrayList<String> getAvailableSessionList()
  {
    return arrPartitionFileList;
  }

  /**
   * This method return duration of available Partition Properties mapped with partition name.
   * @return
   */
  public LinkedHashMap<String, PartitionDataProperties> getAvailableSessionDuration()
  {
    return hmPartitionPropsList;
  }
  
  /**
   * Returns the total duration of all partitions.
   * @return
   */
  public long getTotalDurationOfAllPartitions() 
  {
    return totalDurationOfAllPartitions;
  }
  
  /**
   * Method is used to get the first partition Name of test run.
   * @return
   */
  public String getFirstParitionName()
  {
    if(arrPartitionFileList.size() > 0)
      return arrPartitionFileList.get(0);
    else
      return null;
      
  }

  /**
   * This method will print information of this class
   */
  public void printSessionReportDataInfo(String msg)
  {
    try
    {
      System.out.println("********************Message = " + msg + " *******************");
      LinkedHashMap<String, PartitionDataProperties> map = getAvailableSessionDuration();
      Iterator<String> iterator = map.keySet().iterator();
      while(iterator.hasNext())
      {
        String key = iterator.next();
        System.out.println("----------------- Partition = " + key + " ----------------");
        PartitionDataProperties props = map.get(key);
        ArrayList<Integer> seqList = props.getArrPartitionSequenceFileList();
        ArrayList<Long> fileSizeList = props.getArrPartitionSequenceFileSizeList();
        System.out.println("seqList =  " + seqList);
        System.out.println("fileSizeList = " + fileSizeList);
        for(int i = 0; i < seqList.size(); i++)
        {
  	if(seqList.get(i) == 0)
  	  continue;
  	
  	System.out.println("seq = " + seqList.get(i) + ", size = " + (fileSizeList.get(i))/(1024 * 1024) + "mb ," + (fileSizeList.get(i))/(1024) + "kb , " + fileSizeList.get(i) + "bytes");
  	GraphNames graphNames = new GraphNames(testRunNum, null, null, null, null, key, seqList.get(i) + "", false);
  	System.out.println("size of message data = " + graphNames.getSizeOfMsgData() + ", interval = " + graphNames.getInterval()) ;
  	System.out.println("duratiton = " +  ((fileSizeList.get(i) / graphNames.getSizeOfMsgData()) - 1) * graphNames.getInterval());
  		
        }
      }
      System.out.println("total duration = " + getTotalDurationOfAllPartitions());
      System.out.println("***************************------------******************************");
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public static void main(String[] args)
  {
    int trNum = 1111;
    String partitionName = "201403070421";
    SessionReportData sessionReportData = new SessionReportData(trNum);
    LinkedHashMap<String, PartitionDataProperties> map = sessionReportData.getAvailableSessionDuration();
    Iterator<String> iterator = map.keySet().iterator();
    while(iterator.hasNext())
    {
      String key = iterator.next();
      System.out.println("----------------- Partition = " + key + " ----------------");
      PartitionDataProperties props = map.get(key);
      ArrayList<Integer> seqList = props.getArrPartitionSequenceFileList();
      ArrayList<Long> fileSizeList = props.getArrPartitionSequenceFileSizeList();
      System.out.println("seqList =  " + seqList);
      System.out.println("fileSizeList = " + fileSizeList);
      for(int i = 0; i < seqList.size(); i++)
      {
	if(seqList.get(i) == 0)
	  continue;
	
	System.out.println("seq = " + seqList.get(i) + ", size = " + (fileSizeList.get(i))/(1024 * 1024) + "mb ," + (fileSizeList.get(i))/(1024) + "kb , " + fileSizeList.get(i) + "bytes");
	GraphNames graphNames = new GraphNames(trNum, null, null, null, null, key, seqList.get(i) + "", false);
	System.out.println("size of message data = " + graphNames.getSizeOfMsgData() + ", interval = " + graphNames.getInterval()) ;
	System.out.println("duratiton = " +  ((fileSizeList.get(i) / graphNames.getSizeOfMsgData()) - 1) * graphNames.getInterval());
		
      }
    }
    
    System.out.println("total duration = " + sessionReportData.getTotalDurationOfAllPartitions());
    
    /*GraphNames graphNames = new GraphNames(trNum, null, null, "NA", "", partitionName, false);
    int dataElementSize = graphNames.getDataElementSize();
    int rawDataLen = (graphNames.getSizeOfMsgData() / dataElementSize);
    int msgHdrLen = (graphNames.getStartIndex() / dataElementSize);
    long processedDataLen = rawDataLen - msgHdrLen;
    int sizeOfMsgData = graphNames.getSizeOfMsgData() / 8;*/
  }
}

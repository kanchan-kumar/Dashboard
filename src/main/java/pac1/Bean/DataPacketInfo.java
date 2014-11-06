package pac1.Bean;

/**
 * This Class contains some static variables containing the index of information provided in row data. </br>
 * If any changes in index OR new information is added just add one entry on it.
 */
public class DataPacketInfo 
{
    
  /********************************** Identify the UDP packet Information coming from Net storm ***********/
  
  /*Contains the index of opcode coming in DataGram Packet.*/
  public final static int OPCODE_INDEX = 0;
  
  /*Contains the index of test run number*/
  public final static int TEST_RUN_INDEX = 1;
  
  /*Contains the index of interval.*/
  public final static int INTERVAL_INDEX = 2;
  
  /* Contains the index of sequence number in DataGram/Raw packet*/
  public final static int SEQ_NUM_INDEX = 3;
  
  /*Contains the index of partition/session name in DataGram/Raw packet*/
  public final static int PARTITION_NAME_IDX = 4;
  
  /*Contains the index of time stamp in DataGram/Raw packet */
  public final static int TIMESTAMP_IDX = 5;
  
  /*Tells the index of partition File Sequence/Version.*/
  public static int PARTITION_FILE_SEQUENCE_INDEX = 6;
  
  /********************* Identifier to Identify Opcode ****************************************/
  /*Opcode Identifier to identify start packet*/
  public final static int START_PKT = 0;
  
  /*Opcode Identifier to identify Data packet*/
  public final static int DATA_PKT  = 1;
  
  /*Opcode Identifier to identify End packet*/
  public final static int END_PKT   = 2;
  
  
  /***************** Identifier of Pause/Resume packets *************************************/

  /*control message identify the pause state */
  public final static int PAUSED_MSG = 11;
  
  /*control message identify the resume state */
  public final static int RESUME_MSG = 12;
  
  /*control message identify the GDF change state */
  public final static int GDF_CHANGE_MSG = 13;
  
  /***************** Information about the packet Data *************************************/
  
  /*process header length of data. Only for NDE Mode(Continuous Monitoring) it takes 48 bit.*/
  public static int PROCESS_HDR_LEN = 48;
  
  /*Contains total available information contained by packet data. it must be Sync with header length. */
  public static int HEADER_INFO_COUNT = 6;
  
  
  /**************** Information about the testrun.gdf header info line data *********************/
    
  /*Tells the index of GDF version in info line of GDF file*/
  public static int GDF_VERSION_INDEX = 1;
  
  /*Tells the index of test run in info line of GDF file*/
  public static int GDF_TEST_RUN_INDEX = 4;
  
  /*Tells the index of packet data size in info line of GDF file*/
  public static int GDF_PACKET_DATA_SIZE_INDEX = 5;
  
  /*Tells the index of progress interval in info line of GDF file*/
  public static int GDF_INTERVAL_INDEX = 6;
  
  /*Tells the index of start date/time in info line of GDF file*/
  public static int GDF_DATE_TIME_INDEX = 7;
  
  /*Tells the index of partition name in info line of GDF file*/
  public static int GDF_PARTITION_FILE_INDEX = 8;
    
  /*************** Information to Identify the Availability Sample Data In Array ****************/
  
  /*Tells Not a valid Double Sample data.*/
  public final static double DOUBLE_NO_DATA_IDENTITY = -0.0;
  
  /*Tells Not a valid Integer Sample data.*/
  public final static int INT_NO_DATA_IDENTITY = -0;
  
  /*************** Identify Common Information used to processing partition data ****************/
  
  /*This variable store the file Name format(In Date Format) of partition Data files */
  public static String PARTITION_FILE_START_DATE_FORMAT = "yyyyMMddHHmmss";
    
}

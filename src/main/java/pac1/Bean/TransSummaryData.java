/**----------------------------------------------------------------------------
 * Name       TransSummaryData.java
 * Purpose    Connect to the Server, make a request to server and receive data to show on transaction page
 * @author    Chitra
 * Modification History
 *---------------------------------------------------------------------------**/

package pac1.Bean;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Vector;

public class TransSummaryData
{
  private static String className = "TransSummaryData";
  private Socket sc;
  private OutputStream os;
  private InputStream is;
  private BufferedReader br;
  private final int GET_ALL_TX_DATA = 101;
  private final int SIZE_OF_RCV_PKT_IDX = 0;
  // allocate memory similar to PARENT_CHILD_STRUCTURE_SIZE_IN_NETSTORM presently it is 13*4
  private final int PARENT_CHILD_STRUCTURE_SIZE_IN_NETSTORM = 16;

  private Vector vecTransSummaryData;
  private int port;
  private String serverName = "127.0.0.1";
  private int testRun = -1;
  private String testViewMode = "";

  /*
   *Constructor
   */
  public TransSummaryData()
  {
  }

  private boolean initReqData(Vector vceReqData)
  {
    Log.debugLog(className, "initReqData", "", "", "Method Starts.");
    try
    {
      // 0 index is request type (TRANS_SUMMARY_DATA)

      this.testRun = Integer.parseInt(vceReqData.get(1).toString());
      Log.debugLog(className, "initReqData", "", "", " Test Run = "  + testRun);

      this.serverName = vceReqData.get(2).toString();
      Log.debugLog(className, "initReqData", "", "", " serverName = "  + serverName);

      this.port = Integer.parseInt(vceReqData.get(3).toString());
      Log.debugLog(className, "initReqData", "", "", " Port Number = "  + port);

      this.testViewMode = vceReqData.get(4).toString();
      Log.debugLog(className, "initReqData", "", "", " testViewMode = "  + testViewMode);

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "initReqData", "", "", "Exception in set connection -", e);
      return false;
    }
  }

  /*
   *Called by main method.
   */
  public Vector getTransDetailData(int testRun, String serverName, int port, String testViewMode, StringBuffer errMsg)
  {
    Log.debugLog(className, "getTransDetailData", "", "", "Method Starts. testRun = ");
    Vector vecReqData = new Vector();
    vecReqData.add("TRANS_SUMMARY_DATA");
    vecReqData.add("" + testRun);
    vecReqData.add(serverName);
    vecReqData.add("" + port);
    vecReqData.add(testViewMode);
    return(getTransSummaryData(vecReqData, errMsg));
  }

  /*
   *This method will set connection, send request, to the server, and get response from Server. called by OperateServlet.
   */
  public Vector getTransSummaryData(Vector vecReqData, StringBuffer errMsg)
  {
    Log.debugLog(className, "getTransSummaryData", "", "", "Method Starts.");

    Vector vecTransSummaryData = null;

    if(initReqData(vecReqData) == false)
    {
      errMsg.append("Error in getting transaction data.");
      Log.errorLog(className, "getTransSummaryData", "", "", "Error in getting transaction data.");
      closeSocket();
      return null;
    }
    
    testViewMode = "offline";//Now in offline and online shell command will execute
    
    if(testViewMode.equals("online"))     //To get transaction detail in online mode, communicate through server.
    {
      if(!(setConnection(port, errMsg)))
      {
        Log.errorLog(className, "getTransSummaryData", "", "", "Unable to connect to server.");
        closeSocket();
        return null;
      }

      if(!(sendReqMsgToServer(errMsg)))
      {
        Log.errorLog(className, "getTransSummaryData", "", "", "Unable to send request packet to server.");
        closeSocket();
        return null;
      }

      vecTransSummaryData = getRespMsgToServer(errMsg);
      if(vecTransSummaryData == null)
      {
        Log.errorLog(className, "getTransSummaryData", "", "", "Unable to receive response packet from server.");
        closeSocket();
        return null;
      }
      closeSocket();
    }
    else    //To get transaction detail in offline mode, execute command nsi_tx_summary.
    {
      String userName = vecReqData.get(5).toString();
      String testMode = vecReqData.get(4).toString();
      String genTR = "";
      if(vecReqData.size() >= 7)
        genTR  = vecReqData.get(6).toString();
       
      vecTransSummaryData = getTransSummaryDataFromCommand(testRun, testMode, userName, errMsg , genTR);
      if(vecTransSummaryData == null)
      {
        Log.errorLog(className, "getTransSummaryData", "", "", "Unable to receive ???.");
        return null;
      }
    }

    return vecTransSummaryData;
  }
  
 /**
  * Method to execute the nsi_tx_summary command to get Transactions 
  * in off line.. this is done at the place of reading the file transdetail.dat 
  * @param testRun
  * @param userName
  * @param errMsg
  * @return
  */
  private Vector getTransSummaryDataFromCommand(int testRun, String testMode, String userName, StringBuffer errMsg , String genTR)
  {
    Log.debugLog(className, "getTransSummaryDataFromCommand", "", "", "Method called. TestRun Number = " + testRun + ", userName = " + userName);

    try
    {
      /***
       * Here need to execute the command rather than reading from file
       */
      String cmd = "nsi_tx_summary";
      String cmdArgs = "";
      
      if(!genTR.equals(""))
        cmdArgs = "-t " + testRun  +  " -g " + genTR ;
      else
        cmdArgs = "-t " + testRun;
      
      String pathForCmd = "";
      String usrName = userName;
      String runAsUser = null;
      
      if(testMode.equals("offline"))
      	cmdArgs = cmdArgs + " -o";
      
     
      CmdExec cmdExec = new CmdExec();
      
      Vector vecCmdOutput = cmdExec.getResultByCommand(pathForCmd + cmd, cmdArgs, CmdExec.NETSTORM_CMD, userName, runAsUser);
      
      if(vecCmdOutput == null)
      {
        Log.errorLog(className, "getTransSummaryDataFromCommand", "", "", "vecCmdOutput is comming null");
        errMsg.append("Error in getting data from command - nsi_tx_summary -t " + cmdArgs);
        return null;
      }

      if((vecCmdOutput.size() > 0) && ((String)vecCmdOutput.lastElement()).startsWith("ERROR"))
      {
        for(int i = 0; i < (vecCmdOutput.size() - 1); i++)
          errMsg.append(vecCmdOutput.elementAt(i).toString() + "\n");
        return null;
      }

      return vecCmdOutput;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "getTransSummaryDataFromCommand", "", "", "Exception - " + e);
      return null;
    }
  }

  /*
   *This method will set connection to NS Server.
   */
  private boolean setConnection(int port, StringBuffer errMsg)
  {
    Log.debugLog(className, "setConnection", "", "", "Method Starts.");

    try
    {
      Log.debugLog(className, "setConnection", "", "", "Server Host = " + serverName + ", port = " + port);

      InetSocketAddress inet = new InetSocketAddress(serverName, port);

      SocketAddress sockAddr = (SocketAddress)inet;

      SocketChannel channel = SocketChannel.open(sockAddr);

      sc = channel.socket();

      is = sc.getInputStream();

      os = sc.getOutputStream();

      br = new BufferedReader(new InputStreamReader(sc.getInputStream()));

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "setConnection", "", "", "Exception in set connection -", e);
      errMsg.append("Error in making connection to netstorm due to following error:\n" + e);
      closeSocket();
      return false;
    }
  }

  /*
   *This method will send request to NSserver.
   */
  private boolean sendReqMsgToServer(StringBuffer errMsg)
  {
    Log.debugLog(className, "sendReqMsgToServer", "", "", "Method starts");

    // allocate memory similar to PARENT_CHILD_STRUCTURE_SIZE_IN_NETSTORM presently it is 13*4
    int[] msg = new int[PARENT_CHILD_STRUCTURE_SIZE_IN_NETSTORM];

    Arrays.fill(msg, 0);
    msg[0] = GET_ALL_TX_DATA;

    String binData = Integer.toBinaryString(msg[0]);
    msg[0] = msg[0] << (31 - binData.length()); // leftshift with zero  to convert LSB to MSB

    try
    {
      //allocate (msg.length * 4) byte to buffer.
      ByteBuffer bb = ByteBuffer.allocate(msg.length * 4);
      //make buffer int view
      IntBuffer ff = bb.asIntBuffer();
      //put message to buffer
      ff.put(msg);
      byte byteArr[] = bb.array();

      os.write(byteArr, 0, msg.length * 4);
      os.flush();

      return true;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "sendReqMsgToServer", "", "", "Exception in sendReqMsgToServer - " , e);
      errMsg.append("Error in sending request packet to netstorm due to following error:\n - " + e);
      closeSocket();
      return false;
    }
  }

  /*
   *This method will get response from Server.
   */
  private Vector getRespMsgToServer(StringBuffer errMsg)
  {
    Log.debugLog(className, "getRespMsgToServer", "", "", "Method starts");
    vecTransSummaryData = new Vector();

    try
    {
      byte b[] = new byte[4];
      byte c[] = new byte[4];

      int ii = is.read(b, 0, b.length);

      if(ii == -1) // -1 is there is no more data because the end of the stream has been reached
      {
        Log.debugLog(className, "getRespMsgToServer", "", "", "there is no more data because the end of the stream has been reached");
        errMsg.append("Error in receiving data packet from netstorm due to following error:\nThere is no more data, the end of the stream has been reached.");

        closeSocket();
        return null;
      }

      // To convert LSB to MSB
      c[0] = b[3];
      c[1] = b[2];
      c[2] = b[1];
      c[3] = b[0];

      ByteBuffer bb;

      bb = ByteBuffer.wrap(c);

      int sizeOfPkt = bb.getInt(4 * SIZE_OF_RCV_PKT_IDX);

      Log.debugLog(className, "getRespMsgToServer", "", "", "Size Of response packet = " + sizeOfPkt);

      while((sizeOfPkt - 1) > 0)
      {
        Log.debugLog(className, "getRespMsgToServer", "", "", "Reading line. Size left = " + sizeOfPkt);
        String transSummaryLine = br.readLine();
        if(transSummaryLine == null)
        {
          Log.errorLog(className, "getRespMsgToServer", "", "", "Error in reading transaction summary data. Connection may be closed or test run may be stopped");
          errMsg.append("Error in reading transaction summary data due to following error:\nConnection may be closed or test run may be stopped");
          closeSocket();
          return null;
        }

        sizeOfPkt = sizeOfPkt - (transSummaryLine.length() + 1); // Check if lenght will have new line
        Log.debugLog(className, "getRespMsgToServer", "", "", "Received line. Size left = " + sizeOfPkt);
        Log.debugLog(className, "getRespMsgToServer", "", "", "Adding transaction summary data line in vector = " + transSummaryLine);

        vecTransSummaryData.add(transSummaryLine);

      }

      return vecTransSummaryData;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getRespMsgToServer", "", "", "Exception in processing data packet", e);
      errMsg.append("Error in receiving data packet from netstorm due to following error:\n" + e);
      closeSocket();
      return null;
    }
  }

  /*
   *This method will close connection.
   */

  private void closeSocket()
  {
    try
    {
      Log.debugLog(className, "closeSocket", "", "", "Method called");

      if(sc != null)
      {
        sc.close();
        sc = null;
      }
      if(os != null)
      {
        os.close();
        os = null;
      }
      if(is != null)
      {
        is.close();
        is = null;
      }
      if(br != null)
      {
        br.close();
        br = null;
      }
    }
    catch(Exception e)
    {
      Log.debugLog(className, "closeSocket", "", "", "Exception - " + e + ". Address = " + sc.getInetAddress().toString() + ":" + sc.getPort());
      Log.stackTraceLog(className, "closeSocket", "", "", "Exception", e);
    }
  }

  public static void main(String[] args)
  {
    TransSummaryData transSummaryData = new TransSummaryData();
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    int testRun = -1;
    int port = 0;
    String serverName = "127.0.0.1";
    String testViewMode = "";

    StringBuffer errMsg = new StringBuffer();

    Vector vecTransSummaryData = null;

    System.out.println("Enter testRun Number ");
    try
    {
      testRun = Integer.parseInt(br.readLine());
    }
    catch(IOException e)
    {
      System.out.println("Error in entered testRun: " + e);
    }

    System.out.println("Enter server name ");
    try
    {
      serverName = br.readLine();
    }
    catch(IOException e)
    {
      System.out.println("Error in entered server name : " + e);
    }

    System.out.println("Enter Port Number ");
    try
    {
      port = Integer.parseInt(br.readLine());
    }
    catch(IOException e)
    {
      System.out.println("Error in entered port: " + e);
    }
    System.out.println("Enter testViewMode, it will either onLine or offLine");
    try
    {
      testViewMode = br.readLine();
    }
    catch(IOException e)
    {
      System.out.println("Error in entered argument: " + e);
    }
    vecTransSummaryData = transSummaryData.getTransDetailData(testRun, serverName, port, testViewMode, errMsg);

    if(vecTransSummaryData != null)
    {
      for(int i = 0; i < vecTransSummaryData.size(); i++)
        System.out.println("vecTransSummaryData[" + i + "] = " + vecTransSummaryData.elementAt(i).toString());
    }
    else
    {
      System.out.println("vecTransSummaryData = null");
    }
  }
}

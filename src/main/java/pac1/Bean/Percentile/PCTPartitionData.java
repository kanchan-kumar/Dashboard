package pac1.Bean.Percentile;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import pac1.Bean.BinaryFileReader;
import pac1.Bean.Log;

/**
 * This Class is used for calculation of Percentile in Partition Mode.
 * 
 * @author Itisha
 * 
 */
public class PCTPartitionData
{
  private String className = "PCTPartitionData";

  /* This object is used to read partition files. */
  private BinaryFileReader fileReader = null;

  /**
   * On the basis of Debug Level, Need to write debug logs
   */
  private int debugLevel = 0;

  public PCTPartitionData(int testRunNumber, int debugLevel)
  {
    Log.debugLogAlways(className, "PCTPartitionData", "", "", "testRunNumber = " + testRunNumber + ", debugLevel = " + debugLevel);
    this.debugLevel = debugLevel;
    fileReader = new BinaryFileReader();
  }

  /**
   * This method reads packet data from pctMessage.dat
   */
  public ByteBuffer readOnePacket(String filePath, long pctPacketSize, int numberOfBytesToSkip)
  {
    try
    {
      if (debugLevel > 0)
        Log.debugLogAlways(className, "readOnePacket", "", "", "Method called. filePath = " + filePath + ", pctPacketSize = " + pctPacketSize + ", numberOfBytesToSkip = " + numberOfBytesToSkip);

      // Opening stream
      fileReader.openFileStream(filePath);

      if (numberOfBytesToSkip != 0)
      {
        if (debugLevel > 0)
          Log.debugLogAlways(className, "readOnePacket", "", "", "numberOfBytesToSkip = " + numberOfBytesToSkip);

        fileReader.skipBytesFromInputStream(numberOfBytesToSkip);
      }

      byte[] byteBuf = new byte[(int) pctPacketSize];

      if (fileReader.readPackets(byteBuf) != pctPacketSize)
      {
        Log.errorLog(className, "readOnePacket", "", "", "Incomplete data packet, so packet not read for pktSize = " + pctPacketSize + " filePath = " + filePath);
        fileReader.closeFileStream();
        return null;
      }

      ByteBuffer byteBuffer = ByteBuffer.wrap(byteBuf);
      byteBuffer = byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

      fileReader.closeFileStream();
      return byteBuffer;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "readOnePacket", "", "", "Exception - ", e);
      return null;
    }
  }
}

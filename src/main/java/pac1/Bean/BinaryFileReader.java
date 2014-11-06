package pac1.Bean;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * This Class is used to read binary files through io streams.
 * <br>
 *@version 1.0
 */
public class BinaryFileReader 
{
  /* This is used for logging purpose. */
  String className = "BinaryFileReader";
  
  /*Variable to Identify End Of File.*/
  private final static int EOF = -1;
  
  /* Instance of file input stream to read a file.*/
  private FileInputStream inputStream = null;
  
  /*Instance of data input stream to read a file.*/
  private DataInputStream dataReader = null;
  
  /*variable tells about end of file reached.*/
  private boolean endOfFile = false;
  
  /*Instance of RandomAccessFile to read file randomly.*/
  private RandomAccessFile randomAccessFileObj = null;
  
  /*Instance of File Channel for reading file faster.*/
  private FileChannel fileReader = null;
  
  /**
   * This method is used to open a file stream for reading file.
   * @param fileName - contain the file name with path.
   */
  public void openFileStream(String fileName)
  {
    Log.debugLogAlways(className, "openFileStream", "", "", "Method Called. fileName = "+fileName);
    
    try
    {      
      //Creating new input stream.
      inputStream = new FileInputStream(fileName);
      
      //Creating new data stream.
      dataReader = new DataInputStream(inputStream);
      
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }   
  }
  
  /**
   * Open File in the given mode (r - Read, w - Write, etc) through Random Access File.
   * @param fileName
   * @param mode
   */
  public void openRTGFile(String fileName, String mode)
  {
    try
    {
      //Creating instance of random access file.
      randomAccessFileObj = new RandomAccessFile(fileName, mode);
    }
    catch(Exception e)
    {
      e.printStackTrace();
      Log.errorLog(className, "openFileChannel", "", "", "Exception comming in reading file through random access class." + e.getMessage());
    }
  }
  
  /**
   * Close the File Channel. 
   */
  public void closeFileChannel()
  {
    try
    {
      if(fileReader != null)
      {
	fileReader.close();
	fileReader = null;
      }
    }
    catch(Exception e)
    {
      Log.errorLog(className, "closeFileChannel", "", "", "Error in Closing File Channel.");
      e.printStackTrace();
    }
  }
  
  /**
   * Close the Random Access File Stream. 
   */
  public void closeRandomAcessFile()
  {
    try
    {
      if(randomAccessFileObj != null)
      {
	randomAccessFileObj.close();
	randomAccessFileObj = null;
      }
      
      setEOF(false);
    }
    catch(Exception e)
    {
      Log.errorLog(className, "closeFileChannel", "", "", "Error in Closing Random Access File Stream.");
      e.printStackTrace();
    }
  }
  
  
  /**
   * Return the file channel of random access file.
   * @return
   */
  public FileChannel getFileChannel()
  {
    try
    {
      fileReader = randomAccessFileObj.getChannel();
      return fileReader;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      Log.errorLog(className, "getFileChannel", "", "", "Exception comming in getting File channel. " + e.getMessage());
      return null;
    }
  }
  
  /**
   * Read File From File Channel in Byte Buffer and returns the total number of byte read.
   * @param bufferedFileContent
   * @return
   */
  public int readFileInByteBuffer(ByteBuffer bufferedFileContent)
  {
    try
    {
      //Reading file through File channel in buffer.
      int status = fileReader.read(bufferedFileContent);
        
      if(status == EOF)
	setEOF(true);
      
      return status;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "readFileInByteBuffer", "", "", "Error in reading file through file channel. "+ e.getMessage());
      setEOF(true);
      e.printStackTrace();
      return -1;
    }
  }
  
  
  /**
   * This method is used to skip specified number of bytes from input stream.
   * @param numberOfBytes
   * @return
   */
  public int skipBytesFromInputStream(int numberOfBytes)
  {
    try
    {
      return dataReader.skipBytes(numberOfBytes);
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return 0;
    }
  }
  
  /**
   * Read data packets from file. reads till byte array full. 
   * @param arrBytes
   * @return
   */
  public int readPackets(byte []arrBytes)
  {
    try
    {
      int totalRead = dataReader.read(arrBytes);
      
      //File reached to end.
      if(totalRead == EOF)
	setEOF(true);
      
      return totalRead;
    }
    catch(EOFException e)
    {
      e.printStackTrace();
      setEOF(true);
      return -1;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return -1;
    }
  }
  
  /**
   * This Method is used to close opened stream.
   */
  public void closeFileStream()
  {
    try
    {
      inputStream.close();
      dataReader.close();      
      setEOF(false);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  
  /**
   * Check to see if file is available.
   * @param file
   * @return
   */
  public boolean isFileAvailable(File file)
  {
    return file.exists();
  }
  
  /**
   * Getting the instance of file.
   * @param fileName
   * @return
   */
  public File getFileObj(String fileName)
  {
    try
    {
      return new File(fileName);
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }
  
  /**
   * Tells that File is reached to End.
   * @return
   */
  public boolean getEOF() 
  {
    return endOfFile;
  }

  /**
   * setting end of file is reached.
   * @param endOfFile
   */
  public void setEOF(boolean endOfFile) 
  {
    this.endOfFile = endOfFile;
  }
  
  /**
   * This method returns the size of opened File.
   * @return
   */
  public long getFileSize()
  {
    try 
    {
      return dataReader.available();
    } 
    catch (IOException e) 
    {
      e.printStackTrace();
      return 0;
    }
  }
  
  /**
   * Check if file channel is available or not.
   * @return
   */
  public boolean isFileChannelAvailable()
  {
    if(fileReader == null)
      return false;
    else
      return true;
  }
  
  /**
   * Check if Random Access File Stream available. 
   * @return
   */
  public boolean isFileStreamAvailable()
  {
    if(randomAccessFileObj == null)
      return false;
    else
      return true;
  }
  
  
  /**
   * Read the RTG file(Binary File).
   * @param file
   * @param totalBytesToRead
   * @return
   */
  public void readRTGFile(File file, int totalBytesToRead, byte[]byteArr)
  {
    try
    {
      Log.debugLogAlways(className, "readRTGFile", "", "", "Method Called. totalBytesToRead = "+totalBytesToRead);    
      IOUtils.read(FileUtils.openInputStream(file), byteArr, 0, totalBytesToRead-1);
    }
    catch(Exception e)
    {
      e.printStackTrace();
      Log.errorLog(className, "readRTGFile", "", "", "Error in Reading RTG File");
    }
  }

  public static void main(String[] args) 
  {
    // TODO Auto-generated method stub
  }
}

/**----------------------------------------------------------------------------
* Name     ScriptRename.java
* Purpose  This file is used to create the lock file for the requested File .
*          creates the file data .
*          validate that the lock available is valid or not .
*          return lock status .
*          compare the time between lock aquires.
* @author  Rajesh
* Modification History
*
*---------------------------------------------------------------------------**/
package pac1.Bean;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

public class ScriptFileDataDTO implements Serializable
{
  private final String className = "ScriptFileDataDTO";
  // contains the file data
  private StringBuffer bufferFileData;
  // store the lock status
  private String openFileWithLockMode = "";
  // for ownership
  private String owner = "";
  // flag for lock validation
  private boolean chkIsValidLockAvail = false;

  /**
   * This method is used to get the object of this class that contains the
   * file data, lock status, 
   * @param vectorRequest
   * @return Object(ScriptFileDataDTO)
   */
  
  public ScriptFileDataDTO getFileDataObjWithLock(Vector vectorRequest)
  {
    Log.debugLog(className, "getFileDataObjWithLock", "", "", "Method called.");
    // getting the current work path
    String filePath = getFilePath(vectorRequest.get(1).toString());
    this.owner = vectorRequest.get(5).toString();
    //this will only check that the lock file correspond to its is available or valid or not
    if((Boolean)vectorRequest.get(6))
    {
      checkLockValidityForFile(vectorRequest);
    }
    else //this will set the file data to the buffer and validate the lock file and create a lock file if required.
    { 
      setFileDataInBuffer(filePath);
      valiDateLockReq(vectorRequest);
    }
    return this;  //returning a object of this class.
  }
  
  /**
   * This method is used to get the complet file path from where we will read the data.
   * @param filePath
   * @return String
   */
  private String getFilePath(String filePath)
  {
    Log.debugLog(className, "getFilePath", "", "", "Method called.");

    String strPath = filePath;
    int index;
    //if the file is opened from script management
    if(strPath.contains("../../scripts"))
    {
      index = strPath.indexOf("../../scripts");
      strPath = strPath.substring(index+6);
    }
    //if the script is opened from any TR
    else if(strPath.contains("../../logs"))
    {
      index = strPath.indexOf("../../logs");
      strPath = strPath.substring(index+6);
    }
    //creating the full path for the requested file.
    strPath = Config.getWorkPath() + "/webapps/" + strPath;
    return strPath;
  }
  
  /**
   * This method is used to read the requested file and store the file data in a buffer.
   * @param filePath
   * @return 
   */
  private void setFileDataInBuffer(String filePath)
  {
    Log.debugLog(className, "setFileDataInBuffer", "", "", "Method called. The file path = "+filePath);
    
    //creating the file for the file path
    File dataFilePath = new File(filePath);
    
    //String strLine = null;
    FileInputStream fin = null;
    if(dataFilePath.exists())
    {
      try
      {
        //creating the file input stream for the file.
        fin = new FileInputStream(dataFilePath);
        //creating the byte array accordinhg to the length of the file.
        byte fileContent[] = new byte[(int)dataFilePath.length()];
        //inserting the file content to the byte array.
        fin.read(fileContent);
        bufferFileData = new StringBuffer();
        //appending the byte data into a buffer.
        bufferFileData.append(new String(fileContent));
      }
      catch(FileNotFoundException e)
      {
        Log.errorLog(className, "setFileDataInBuffer", "", "", "Getting file not found Exception during reading the file.");
      }
      catch(IOException ioe)
      {
        Log.errorLog(className, "setFileDataInBuffer", "", "", "Getting input out Exception during reading the file.");
      }
      finally
      {
        try
        {
          if(fin != null)
          {
            fin.close();
          }
        }
        catch(IOException ioe)
        {
          Log.errorLog(className, "setFileDataInBuffer", "", "", "Exception occured while closing the input stream." + ioe);
        }
      }
    }
    else
    {
      Log.errorLog(className, "setFileDataInBuffer", "", "", "The file path is not exist. The path is - " + dataFilePath);
      return;
    }
  }
  
  /**
   * This method is used to validate the the lock requested by the user is available or not.
   * @param vectorRequest
   * @return 
   */
  private void valiDateLockReq(Vector vectorRequest)
  {
    Log.debugLog(className, "valiDateLockReq", "", "", "Method called.");
    
    String openMode = vectorRequest.get(4).toString();
    //if the request is of Read mode then do not check the availability of lock
    if(openMode.equals("Read Mode"))
    {
      return;
    }
    else
    {
      //else validate the lock is available or not.
      getLockForEditMode(vectorRequest);
    }
  }
  
  /**
   * This method will check that the requested user should get the lock for this file or not
   * @param vectorRequest
   * @return 
   */
  private void checkLockValidityForFile(Vector vectorRequest)
  {
    Log.debugLog(className, "checkLockValidityForFile", "", "", "Method called.");
    //file path for the lock
    String filePath = getFilePath(vectorRequest.get(1).toString());
    filePath = filePath.substring(0, filePath.lastIndexOf("/"));
    //creating the lock file for the requested file.
    String lockFileName = filePath + "/" + "." + vectorRequest.get(2).toString() + ".lock";

    Log.debugLog(className, "checkLockValidityForFile", "", "", "Method called. The lockfile path = " + lockFileName);
    
    File lockFile = new File(lockFileName);
    //if the lock file already exist
    if(lockFile.exists())  //means that the lock is aquired by another system or user.
    {
      String strLine = "";
      try
      {
        //reading the lock file to check that previous
        BufferedReader buffDataForLockFile = new BufferedReader(new FileReader(lockFile));
        while((strLine = buffDataForLockFile.readLine()) != null)
        {
          if(strLine.startsWith("LOCK TIME"))
          {
            String[] data = strLine.split("=");
            if(data.length == 2)
            {
              long prevLockTime = Long.parseLong(data[1].trim());
              long currentLockTime = System.currentTimeMillis();
              //validate that the existed lock not more than 5 hours
              long maxiMumLockHours = 5 * 60 * 60 * 1000; //converted to milisecs
              if(prevLockTime < currentLockTime)
              {
                //if the lock is more than 3 hours then get the lock
                if((currentLockTime - prevLockTime) > maxiMumLockHours)
                {
                  //invalid lock
                  Log.debugLog(className, "checkLockValidityForFile", "", "", "The lock time is more than 3 hours. So it is not a valid lock.");
                  chkIsValidLockAvail = false;
                }
                else //valid lock
                {
                  chkIsValidLockAvail = true;
                }
              }
            }
          }
        }
      }
      catch(Exception e)
      {
        Log.errorLog(className, "checkLockValidityForFile", "", "", "Error in reading file data - " + e);
        chkIsValidLockAvail = false;
      }
    }
  }
  
  /**
   * if the request is of Edit Mode then this method checks that current user is able to open the file or not
   * @param vectorRequest
   * @return 
   */
  private void getLockForEditMode(Vector vectorRequest)
  {
    Log.debugLog(className, "getLockForEditMode", "", "", "Method called.");
    
    String filePath = getFilePath(vectorRequest.get(1).toString());
    filePath = filePath.substring(0, filePath.lastIndexOf("/"));
    //creating the lock file path
    String lockFileName = filePath + "/" + "." + vectorRequest.get(2).toString() + ".lock";
    File lockFile = new File(lockFileName);
    //if the lock file is not present then it creates the lock file for the user
    if(!lockFile.exists())
    {
      createLockFile(lockFile, vectorRequest);
    }
    else //if the lock file is already available.
    {
      if(isValidLock(lockFile, vectorRequest)) //check that the lock file is valid or not
      {
        openFileWithLockMode = "The file is already opened in edit mode";
      }
    }
  }
  
  /**
   * This method will check that the lock file previously available is no longer valid or not
   * @param lockFile
   * @param vectorRequest
   * @return boolean
   */
  private boolean isValidLock(File lockFile, Vector vectorRequest)
  {
    Log.debugLog(className, "isValidLock", "", "", "Method called.");
    
    String strLine = ""; 
    ArrayList<String > lockFileData = new ArrayList<String>();
    Vector vecFileData =  new Vector();
    try
    {
      //reading the lock file data.
      BufferedReader brforLockFile = new BufferedReader(new FileReader(lockFile));
      while((strLine = brforLockFile.readLine()) != null)
      {
        String[] data = strLine.split("=");
        if(data.length == 2)
          lockFileData.add(data[1]);
      }
      if(lockFileData.size() == 3)
      {
        long prevLockTime = Long.parseLong(lockFileData.get(2).trim());
        long currentLockTime = System.currentTimeMillis();
        long maxiMumLockHours = 5 * 60 * 60 * 1000; //converted to milisecs
        if(prevLockTime < currentLockTime) 
        {
          //if the lock is more than 5 hours then get the lock
          if((currentLockTime - prevLockTime) > maxiMumLockHours)
          {
            createLockFile(lockFile, vectorRequest);
            return false;
          }
          return true;
        }
      }
    }
    catch(Exception e)
    {
      Log.errorLog(className, "modifyFiles", "", "", "pageList.html file not exist in the " + e);
      return false;
    }
    return false;
  }
  
  /**
   * This method is used to create the lock file for the requested file
   * @param lockFile
   * @param vectorRequest
   * @return 
   */
  private void createLockFile(File lockFile, Vector vectorRequest)
  {
    try
    {
      BufferedWriter output = new BufferedWriter(new FileWriter(lockFile));
      output.write(getDataToWriteIntoLockFile(vectorRequest));
      output.close();
      //changing the ownership of the file
      ChangeOwnerOfFile(lockFile.getAbsolutePath(), owner);
      //changing the permission of the lock file
      ChangePermissionOfFile(lockFile.getAbsolutePath(), "664");
    }
    catch(IOException e)
    {
      Log.stackTraceLog(className, "createLockFile", "", "", "Exception in creating lock file = ", e);
    }
  }
 
  /**
   * This method is used to ownership of a file 
   * @param filePath
   * @param owner
   * @return boolean
   */
  private boolean ChangeOwnerOfFile(String filePath, String owner)
  {
    try
    {
      Log.debugLog(className, "ChangeOwnerOfFile", "", "", "method called");
      
      Runtime r = Runtime.getRuntime();
      String strCmd = "chown" + " " + owner + "." + "netstorm" + " " + filePath;
      Process changePermissions = r.exec(strCmd);
      int exitValue = changePermissions.waitFor();
      if (exitValue == 0)
        return true;
      else
      return false;
    }   
    catch (Exception e)
    {
      // e.printStackTrace();
      Log.stackTraceLog(className, "ChangeOwnerOfFile", "", "", "Exception = ", e);
      return false;
    }
  }
  /**
   * This method is used to change the permission of a file
   * @param filePath
   * @param cmdVal
   * @return booblean
   */
  private boolean ChangePermissionOfFile(String filePath, String cmdVal)
  {
    String filePermission = "775";
    if (filePath.equals(""))
      return false;
    if (!cmdVal.equals(""))
      filePermission = cmdVal;
    try
    {
      Log.debugLog(className, "ChangePermissionOfFile", "", "", "method called");
      
      Runtime r = Runtime.getRuntime();
      String strCmd = "chmod" + " " + filePermission + " " + filePath;
      Process changePermissions = r.exec(strCmd);
      int exitValue = changePermissions.waitFor();
      if (exitValue == 0)
        return true;
      else
       return false;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "ChangePermissionOfFile", "", "", "method called", e);
      return false;
    }
  } 

 /**
  * used to get lock file data
  * @param vectorRequest
  * @return String
  */
  private String getDataToWriteIntoLockFile(Vector vectorRequest)
  {
    return "MODE = EDIT MODE\nUSER = "+ vectorRequest.get(5)+ "\nLOCK TIME = " + System.currentTimeMillis();
  }

  /**
   * used to store the file data in a buffer
   * @param 
   * @return bufferFileData
   */
  public StringBuffer getFileData()
  {
    return bufferFileData;
  }
  
  /**
   * used to return that the requested file got the lock or not
   * @param 
   * @return String
   */
  public String getFileModeDataMsg()
  {
    return openFileWithLockMode;
  }
  
  /**
   * used to store that the lock is valid or not.
   * @param
   * @return boolean
   */
  public boolean getIsValidLock()
  {
    return chkIsValidLockAvail; 
  }
}

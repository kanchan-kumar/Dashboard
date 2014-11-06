/*--------------------------------------------------------------------
  @Name      : VUTReadXMLData.java
  @Author    : Ankit Khanijau
  @Purpose   : To read XML File under path workPath + "/webapps/logs/TRXXXX/user_trace/groupname_NVMId_UserId_SessionNumber/User_Trace.xml
  @Modification History:
      07/13/2011 -> Ankit Khanijau (Initial Version)

----------------------------------------------------------------------*/

package pac1.Bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import pac1.Bean.VUserTrace.UserTrace;

public class VUserTraceReadXmlFile implements Serializable
{
  private static String className = "VUserTraceReadXmlFile";
  private static String workPath = Config.getWorkPath();
  private final static String XML_FILE_EXT = ".xml";
  private final static String USER_TRACE_FILE_NAME = "User_Trace";

  public long vUserTraceXMLFileLastModified = -1;
  public long vUserTraceXMLFileSize = -1;

  private String vUserXmlPath = null;
  static String testRunNum = null;
  public static String workPathFromRoot = null;
  private static File vUserTraceXMLFile = null;

  public static String pathForImgOfPageThink = Config.getWorkPath() + "/webapps/netstorm/images/iconProcessing2.png";

  //constructor
  public VUserTraceReadXmlFile()
  {

  }

  private static String getPath()
  {
    return (workPath + "/webapps/logs/");
  }

  public String setPathInOfflineMode(String testNum , String groupName)
  {
    Log.debugLog(className, "setPathInOfflineMode", "", "", "Method Start.");
	  
    String path = Config.getWorkPath()+"/webapps/logs/TR"+testNum+"/vuser_trace/"+groupName+"/vuser_trace.xml";
	  setVUserXmlPath(path);
	  
	  Log.debugLog(className, "setPathInOfflineMode", "", "", "Method End.");
	  return path;
  }
  
  public String getVUserTraceXMLPath(String testNum, String groupName)
  {
    Log.debugLog(className, "getVUserTraceXMLPath", "", "", "Method Start.");

    try
    {
      testRunNum = testNum;

      CmdExec cmdExec = new CmdExec();

      String cmd = "nsu_vuser_trace";
      String args = " -t " + testNum + " -g " + groupName;

      Vector vecCmdOut = cmdExec.getResultByCommand(cmd, args, 0, "root", "root");

      Log.debugLog(className, "getVUserTraceXMLPath", "", "", "VECTOR = " + vecCmdOut.get(0).toString());
      
      String user_traceXMLPath = null;
      
      /* path return by nsu_vuser_trace
       *  from /home/netstorm/.../TRXXXX/user_trace/groupname_NVMId_UserId_SessionNumber/user_trace.xml
       *  format of return as
       *  PASS : <path>
       */
      String cmdResult = null;
      String[] arrCmdResult = null;
      
      boolean result = false;
      
      if (vecCmdOut != null && vecCmdOut.size() > 0)
      {
        if(vecCmdOut.get(0).toString().trim().toLowerCase().startsWith("pass"))
          result = true;
        else if((vecCmdOut.lastElement().toString()).startsWith("ERROR") || (vecCmdOut.lastElement().toString()).startsWith("Error") || (vecCmdOut.lastElement().toString()).startsWith("FAIL") || (vecCmdOut.lastElement().toString()).startsWith("Fail"))
        {
          Log.errorLog(className, "getVUserTraceXMLPath", "", "", "Error while executing 'nsu_vuser_trace' command as it is unable to give Group Name, NVM Id, User Id and Session Number.");
          result = false;
        }

        /* cmd result can have following
         * FAIL: error messaqe
         * PASS: XML_FILE=<xml file with full path>
         * 
         * Examples:  
         *  PASS: XML_FILE=/home/netstorm/work/webapps/TRXXXX/vuser_trace/<groupName>_<NVM_ID>_<User_Id>_<Session_Id>/vuser_trace.xml
         *  FAIL: There is no active user in the netstorm for group_idx <id>. 
         */
        cmdResult = vecCmdOut.get(0).toString();
        arrCmdResult = rptUtilsBean.split(cmdResult, ":");  // Split to get pass or fail
      }
      else
        Log.errorLog(className, "getVUserTraceXMLPath", "", "", "Problem in executing - " + cmd + " " + args);

      if(result) // Command is successful
      {
        Log.debugLogAlways(className, "getVUserTraceXMLPath", "", "", "cmdResult = " + cmdResult);
        String cmdToken = arrCmdResult[0].trim();
        if(cmdToken.equalsIgnoreCase("Pass")) // Since cmd is successful, it should be always Pass
        {
          String cmdFilePathResult = arrCmdResult[1].trim();
          
          String[] arrCmdFilePath = rptUtilsBean.split(cmdFilePathResult, "="); // Split to get XML_FILE and xmlPath.
          
          String filePathToken = arrCmdFilePath[0].trim();

          Log.debugLogAlways(className, "getVUserTraceXMLPath", "", "", "filePathToken = " + filePathToken);
          if(filePathToken.equalsIgnoreCase("XML_FILE")) // Since cmd is   
          {
            user_traceXMLPath = arrCmdFilePath[1].trim();
            setVUserXmlPath(user_traceXMLPath);
            Log.debugLogAlways(className, "getVUserTraceXMLPath", "", "", "user_traceXMLPath = " + user_traceXMLPath);
          }
          else
            user_traceXMLPath = cmdFilePathResult;
        }
        else
          user_traceXMLPath = cmdResult;
     
        Log.debugLogAlways(className, "getVUserTraceXMLPath", "", "", "Path of user_trace.xml = " + user_traceXMLPath);
      }
      else
      {
        /*String cmdToken = arrCmdResult[0].trim();
        if(cmdToken.equalsIgnoreCase("Error") || cmdToken.equals("Fail"))
          user_traceXMLPath = cmdResult;
        else*/
        user_traceXMLPath = cmdResult;
       
        Log.debugLogAlways(className, "getVUserTraceXMLPath", "", "", "result = " + result + " " + user_traceXMLPath);
      }
      
      Log.debugLog(className, "getVUserTraceXMLPath", "", "", "Method End.");
      return user_traceXMLPath;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "", "", "", "Exception - ", e);
      return null;
    }
  }

  //Open file and returns File object
  private static File openFile(String fileName)
  {
    Log.debugLog(className, "openFile", "", "", "Method Start.");

    try
    {
       File tempFile = new File(fileName);

       Log.debugLog(className, "openFile", "", "", "Method End.");
       
       return(tempFile);
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "openFile", "", "", "Exception - ", e);
      return null;
    }
  }

/**
 * @method name readFile
 * @pupose Read Unmarshal(for reading)
 * @param xmlFileNameWithPath, xsdFileNameWithPath
 * @return Unmarshaller
 */
  public UserTrace readFile(String vUserTraceXMLFileNameWithPath)
  {
    Log.debugLog(className, "readFile", "", "", "Method called. Virtual User Trace XML File Name = " + vUserTraceXMLFileNameWithPath);

    try
    {
      if(vUserTraceXMLFileNameWithPath != null)
      {
        vUserTraceXMLFile = openFile(vUserTraceXMLFileNameWithPath);

        if(!vUserTraceXMLFile.exists())
        {
          Log.errorLog(className, "readFile", "", "", USER_TRACE_FILE_NAME + XML_FILE_EXT + " File not found, on following path - " + vUserTraceXMLFileNameWithPath);
          return null;
        }

        try
        {
          UserTrace userTraceObj = null;
          if(vUserTraceXMLFile.canRead())
          {
            userTraceObj = vUserTraceXMLParser(vUserTraceXMLFile);
            setVUserTraceXMLFileLastModified(vUserTraceXMLFile.lastModified());
            setVUserTraceXMLFileSize(vUserTraceXMLFile.length());
          }
          
          Log.debugLog(className, "readFile", "", "", "Method End.");
          
          return userTraceObj;
        }
        catch(Exception e)
        {
          Log.stackTraceLog(className, "", "", "", "Exception - ", e);
          return null;
        }
      }
      else
      {
        Log.errorLog(className, "readFile", "", "", "Path for user_trace.xml is null");
        return null;
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "readFile", "", "", "Exception - ", e);
      return null;
    }
  }

  public long getVUserTraceXMLFileLastModified()
  {
    return vUserTraceXMLFileLastModified;
  }

  public long getVUserTraceXMLFileSize()
  {
    return vUserTraceXMLFileSize;
  }

  private void setVUserTraceXMLFileLastModified(long xmlFileLastModified)
  {
    vUserTraceXMLFileLastModified = xmlFileLastModified;
  }

  private void setVUserTraceXMLFileSize(long xmlFileSize)
  {
    vUserTraceXMLFileSize = xmlFileSize;
  }

  private static UserTrace vUserTraceXMLParser(File vUserTraceXMLFile)
  {
    Log.debugLog(className, "vUserTraceXMLParser", "", "", "Method Start.");
    
    try
    {
      //JAXBContext jc = JAXBContext.newInstance( "com.netstorm.virtualtrace" );
      JAXBContext jaxbObj = JAXBContext.newInstance("pac1.Bean.VUserTrace");
      Unmarshaller unmarshalObj = jaxbObj.createUnmarshaller();

      UserTrace userTraceObj = (UserTrace)unmarshalObj.unmarshal(new FileInputStream(vUserTraceXMLFile));

      Log.debugLog(className, "vUserTraceXMLParser", "", "", "Method End.");
      
      return userTraceObj;
    }
    catch(JAXBException e)
    {
      Log.stackTraceLog(className, "", "", "", "JAXBException - ", e);
      return null;
    }
    catch(FileNotFoundException e)
    {
      Log.stackTraceLog(className, "", "", "", "FileNotFoundException - ", e);
      return null;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "", "", "", "Exception - ", e);
      return null;
    }
  }

  public String getVUserXmlPath()
  {
    return vUserXmlPath;
  }

  public void setVUserXmlPath(String vUserXmlPath)
  {
    Log.debugLogAlways(className, "setVUserXmlPath", "", "", "vUserXmlPath = " + vUserXmlPath);
    this.vUserXmlPath = vUserXmlPath;
  }

  public static void main(String a[])
  {
    VUserTraceReadXmlFile reob = new VUserTraceReadXmlFile();
    reob.readFile(reob.getVUserTraceXMLPath("4247", "g1"));
  }

}

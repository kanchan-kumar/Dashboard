/**----------------------------------------------------------------------------
 * Name       ScriptStop.java
 * Purpose    This will save request and response file content for correlation
 * @author    Atul
 * Modification History
 *
 *---------------------------------------------------------------------------**/
package pac1.Bean;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

public class ReqResFileContent
{
  private String className = "ReqResFileContent";
  public String urlOfReq = null;
  public String body = "";
  public String decodedBody = "";
  public boolean isEncoded = false;
  public String fileName = null;
  public int fileType = -1;
  public String filePath = null;
  public boolean isFileExist;
  public String contentType = "";
  public String extension = "";
  public String scriptName = null;
  public String upperData = null;
  public long requestTimeStamp = 0L;
  public byte[] responseData = null;

  public static final int REQUEST_FILE = 0;
  public static final int REQUEST_BODY_FILE = 1;
  public static final int RESPONSE_FILE = 2;
  public static final int RESPONSE_BODY_FILE = 3;
  public static final int SERVICE_FILE = 4;  //service file  

  public ReqResFileContent(String scriptName, String filePath, int fileType, String fileName, boolean isFileExist)
  {
    this.scriptName = scriptName;
    this.filePath = filePath;
    this.fileType = fileType;
    this.fileName = fileName;
    this.isFileExist = isFileExist;

    if(!isFileExist)
      fileName = "Not Found";
  }

  public boolean getRequestResponseFileContentsForScript(String contentTypeExtension[][])
  {
    Log.debugLog(className, "getRequestResponseFileContentsForScript", "", "", "Method started scriptName = " + scriptName + ", fileName = " + fileName + ", fileType = " + fileType);
    try
    {
      File fileObj = new File(filePath);
      byte data[] = new byte[(1024 * 4)];
      FileInputStream fis = new FileInputStream(fileObj);
      BufferedInputStream bis = new BufferedInputStream(fis);
      StringBuffer sbData = new StringBuffer();

      String strLine = null;
      int dataReadLength = -1;
      while((dataReadLength = bis.read(data)) > 0)
      {
        strLine = new String(data, 0, dataReadLength);
        sbData.append(strLine);
      }
      
      // this is commented because body files of request and response can be empty.
      /**if(sbData.length() <= 0)
      {
        Log.errorLog(className, "getRequestResponseFileContentsForScript", "", "", "Empty Buffer no data found for file = " + filePath);
        return false;
      }**/

      if(fileType == REQUEST_FILE)
      {
        String str  = sbData.substring(0, sbData.indexOf("\r\n"));
        Log.debugLog(className, "getRequestResponseFileContentsForScript", "", "", "First line = " + str + ", for request file = " + filePath);
        urlOfReq = str.split(" ")[1];
      }
      else if(fileType == REQUEST_BODY_FILE)
      {
        Log.debugLog(className, "getRequestResponseFileContentsForScript", "", "", " Data read for request_body file from " + filePath);
      }
      else if(fileType == RESPONSE_BODY_FILE)
      {
        Log.debugLog(className, "getRequestResponseFileContentsForScript", "", "", " Data read for response_body file from " + filePath);
      }
      else if(fileType == SERVICE_FILE)
      {
        int indexOfRequestTime = sbData.toString().toUpperCase().indexOf("REQ_TIMESTAMP");
        if(indexOfRequestTime == -1)
          Log.debugLog(className, "getRequestResponseFileContentsForScript", "", "", "indexOfRequestTime not found in the service file = " + filePath);
        else
        {
          String requestTime = sbData.substring((indexOfRequestTime + "REQ_TIMESTAMP".length() + 1)).trim();
          Log.debugLog(className, "getRequestResponseFileContentsForScript", "", "", "requestTime = " + requestTime + " for file = " + filePath);
          requestTimeStamp = Long.parseLong(requestTime);
        }
      }
      else//Response File
      {
        int indexOfContentType = sbData.toString().toUpperCase().indexOf("CONTENT-TYPE:");
        if(indexOfContentType == -1)
          Log.debugLog(className, "getRequestResponseFileContentsForScript", "", "", "content-type not found in the response file = " + filePath);
        else
        {
          contentType = sbData.substring((indexOfContentType + "CONTENT-TYPE:".length() + 1), sbData.indexOf("\r\n", indexOfContentType)).trim();
          Log.debugLog(className, "getRequestResponseFileContentsForScript", "", "", "Content-Type = " + contentType + " for file = " + filePath);
        }

        if(contentTypeExtension == null || contentTypeExtension.length <= 0)
          Log.debugLog(className, "getRequestResponseFileContentsForScript", "", "", "content-type to extension array is coming null OR empty");
        else
        {
          if(!contentType.equals(""))
          {
            boolean isFoundExtension = false;
            for(int i = 0 ; i < contentTypeExtension.length ; i++)
            {
              String content = contentTypeExtension[i][1];
              String ext = contentTypeExtension[i][0];
              if(content.equalsIgnoreCase(contentType))
              {
                extension = ext;
                isFoundExtension = true;
                Log.debugLog(className, "getRequestResponseFileContentsForScript", "", "", "Extension found = " + extension + " for content-type = " + contentType + " response file = " + filePath);
                break;
              }
            }
            if(!isFoundExtension)
              Log.debugLog(className, "getRequestResponseFileContentsForScript", "", "", "Extension not found for content-type = " + contentType + " for response file = " + filePath);
          }
        }
      }

      if(fileType != REQUEST_BODY_FILE && fileType != RESPONSE_BODY_FILE && fileType != SERVICE_FILE)
      {
        //Index of will return index of first \r, so we need to add 4
        body = sbData.substring(sbData.indexOf("\r\n\r\n") + 4, sbData.length());

        upperData = sbData.substring(0, sbData.indexOf("\r\n\r\n"));

        int indexOfEncodeedHeader = upperData.indexOf("Content-type: application/x-www-form-urlencoded");

        if(indexOfEncodeedHeader != -1)
        {
          isEncoded = true;
          decodedBody = CorrelationService.decodeString(body);
        }
        else
          decodedBody = body;// this to show body with upper data, in case body is not encoded, if click on show 'Decode body with header'.

        Log.debugLog(className, "getRequestResponseFileContentsForScript", "", "", "Data added for fileName = " + fileName + ", fileType = " + fileType);
      }
      else
      {
        // in case of request & response body, no header exist so whole content is copied into body object from StringBuffer sbData.
        body = sbData.toString();

        //if response content may be image or any binary data like images data 
        byte dataByte[] = new byte[(1024 * 4)];
        FileInputStream fisResponse = new FileInputStream(fileObj);
        java.io.ByteArrayOutputStream bosResponse = new java.io.ByteArrayOutputStream();
        int bytesRead = 0;
        while((bytesRead = fisResponse.read(dataByte)) != -1)
        {
          bosResponse.write(dataByte, 0, bytesRead);
        }
        responseData = bosResponse.toByteArray();
        fisResponse.close();
        bosResponse.close();

        Log.debugLog(className, "getRequestResponseFileContentsForScript", "", "", "Data added for fileName = " + fileName + ", fileType = " + fileType);
      }

      fis.close();
      bis.close();
      return true;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getRequestResponseFileContentsForScript", "", "", "Exception - ", ex);
      return false;
    }
  }

  public String getDecodeFileData()
  {
    return upperData + "\r\n\r\n" + decodedBody;
  }

  public String getFileData()
  {
    return upperData + "\r\n\r\n" + body;
  }
}
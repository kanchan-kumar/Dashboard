
package pac1.Bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public class ReplayUrlFilterPattern 
{
  final private String className = "ReplayUrlFilterPattern";
  final private String urlFilterPtnFileName = "replay_url_filter_patterns.dat";
  final private String urlPtnFileName = "replay_url_patterns.dat";
  final private String fileDirPath = Config.getWorkPath() + "/replay_profiles/";
  private String profileName = null;
  private String userName = null;
  String lineSeparator = System.getProperty("line.separator");
  
  public ReplayUrlFilterPattern(String arg1, String arg2)
  {
    profileName = arg1;
    userName = arg2;
  }
  
  /**
   * function saveReplayUrlFilterPattern save the data in file replay_url_filter_pattern.dat .
   * @param data
   * @return StringBuilder
   */
  public StringBuilder saveReplayUrlFilterPattern(String[][] data)
  {
    StringBuilder errMsg = new StringBuilder();
    Log.debugLog(className, "saveReplayUrlFilterPattern", "", "", "method called");
    try
    {
      File replayUrlFilterFile = new File(fileDirPath + "/" + profileName + "/" + urlFilterPtnFileName);
      if(replayUrlFilterFile.exists())
      {
        Log.debugLog(className, "saveReplayUrlFilterPattern", "", "", "repaly url filter pattern file alreagy present. Deleting it");
        replayUrlFilterFile.delete();
      }
      
      File replayUrlFilterDir = new File(fileDirPath + "/" + profileName);
      if(!replayUrlFilterDir.exists())
      {
        Log.debugLog(className, "saveReplayUrlFilterPattern", "", "", "Director for profilr '" + profileName +"' is not present. Creating it.");
        replayUrlFilterDir.mkdirs();
      }
      
      FileWriter fw = new FileWriter(replayUrlFilterFile);
      fw.write("Status|Method|SampleURL|Host|Pattern");
      fw.write(lineSeparator);
      if(data != null)
      {
        for(int i = 0; i < data.length; i++)
        {
          for(int j = 0; j < data[i].length; j++)
          {
            if(j == 0)
              fw.write(data[i][j].trim().replaceAll("\r", ""));
            else
              fw.write("|" + data[i][j].trim().replaceAll("\r", ""));
          }
          fw.write(lineSeparator);
        }
      }
      fw.flush();
      fw.close();
      
      ChangeOwnerOfFile(fileDirPath + "/" + profileName + "/" + urlFilterPtnFileName, userName);
      ChangeOwnerOfFile(fileDirPath + "/" + profileName, userName);
      Log.debugLog(className, "saveReplayUrlFilterPattern", "", "", "replay url pattens saved successfully");
    }
    catch(Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "saveReplayUrlFilterPattern", "", "", "", e);
      errMsg.append("Error in saving replay url filter settings. Please check error log.");
    }
    return errMsg;
  }
  
  /**
   * This function returns data of file 'replay_url_filter_patterns.dat' having pipe separated data.
   * Status|method |Sample URL|Host|Pattern
   * Active |xxxx.ping|*.ping
   * @param errMsg
   * @return ArrayList<String[]>
   */
  public ArrayList<String[]> getReplayUrlFilterPattern(StringBuilder errMsg)
  {
    Log.debugLog(className, "getReplayUrlFilterPattern", "", "", "method called for profileName: "+profileName);
    ArrayList<String[]> data = new ArrayList<String[]>();
    try
    {
      File relayUrlPatternFile = new File(fileDirPath + "/" + profileName + "/" + urlFilterPtnFileName); 
      BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(relayUrlPatternFile)));
      String strLine = null;
      while((strLine = br.readLine()) != null)
      {
        if(!strLine.trim().equals(""))
        {
          String tmp[] = (strLine+" ").split("\\|");
          if(tmp.length > 4)
          {
            String urlInfo[] = new String[5];
            urlInfo[0] = tmp[0];
            urlInfo[1] = tmp[1];
            urlInfo[2] = tmp[2];
            urlInfo[3] = tmp[3];
            //String url = tmp[4];
            //for(int i = 4; i < tmp.length; i++)
             //url += "|" + tmp[i];
            urlInfo[4] = tmp[4];
            data.add(urlInfo);
          }
        }
      }
      br.close();
      Log.debugLog(strLine, "getReplayUrlFilterPattern", "", "", "data is readed successfully");
      return data;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "getReplayUrlFilterPattern", "", "", "", e);
      errMsg.append("Error in getting url filter pattern list. Please check error log.");
    }
    return null;
  }
  
  /**
   * function saveReplayUrlPattern save the data in file replay_url_pattern.dat .
   * @param data
   * @return StringBuilder
   */
  public StringBuilder saveReplayUrlPattern(String[][] data)
  {
    StringBuilder errMsg = new StringBuilder();
    Log.debugLog(className, "saveReplayUrlPattern", "", "", "method called");
    try
    {
      File replayUrlFilterFile = new File(fileDirPath + "/" + profileName + "/" + urlPtnFileName);
      if(replayUrlFilterFile.exists())
      {
        Log.debugLog(className, "saveReplayUrlPattern", "", "", "repaly url filter pattern file alreagy present. Deleting it");
        replayUrlFilterFile.delete();
      }
      
      File replayUrlFilterDir = new File(fileDirPath + "/" + profileName);
      if(!replayUrlFilterDir.exists())
      {
        Log.debugLog(className, "saveReplayUrlPattern", "", "", "Director for profilr '" + profileName +"' is not present. Creating it.");
        replayUrlFilterDir.mkdirs();
      }
      
      FileWriter fw = new FileWriter(replayUrlFilterFile);
      fw.write("Status|Type|method|SampleURL|TxName|HostName|Comments|PostRequestFile|Pattern");
      fw.write(lineSeparator);
      if(data != null)
      {
        for(int i = 0; i < data.length; i++)
        {
          for(int j = 0; j < data[i].length; j++)
          {
            if(j == 0)
              fw.write(data[i][j].trim().replaceAll("\r", ""));
            else
              fw.write("|" + data[i][j].trim().replaceAll("\r", ""));
          }
          fw.write(lineSeparator);
        }
      }
      fw.flush();
      fw.close();
      
      ChangeOwnerOfFile(fileDirPath + "/" + profileName + "/" + urlPtnFileName, userName);
      ChangeOwnerOfFile(fileDirPath + "/" + profileName, userName);
      Log.debugLog(className, "saveReplayUrlPattern", "", "", "replay url pattens saved successfully");
    }
    catch(Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "saveReplayUrlPattern", "", "", "", e);
      errMsg.append("Error in saving replay url filter settings. Please check error log.");
    }
    return errMsg;
  }
  
  /**
   * This function return the data of file 'replay_url_patterns.dat' having pipe separated data.
   * Status|Type|method|Sample URL|Tx Name|HostName|Comments|Pattern
   * Active|InLine|get |/xxx/xx.png|NA|images.walgreens.com|PNG Files|*.png
   * Active|InLine|get |/xxx/xx.png?x=y|NA|images.walgreens.com| PNG Files with query|*.png?*
   * @param errMsg
   * @return ArrayList<String[]>
   */
  
  
  
  public ArrayList<String[]> getReplayUrlPattern(StringBuilder errMsg)
  {
    Log.debugLog(className, "getReplayUrlPattern", "", "", "method called for profileName: "+profileName);
    ArrayList<String[]> data = new ArrayList<String[]>();
    try
    {
      File relayUrlPatternFile = new File(fileDirPath + "/" + profileName + "/" + urlPtnFileName); 
      BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(relayUrlPatternFile)));
      String strLine = null;
      while((strLine = br.readLine()) != null)
      {
        if(!strLine.trim().equals(""))
        {
          String tmp[] = (strLine+" ").split("\\|");
          if(tmp.length > 6)
          {
            String urlInfo[] = new String[9];
            urlInfo[0] = tmp[0];
            urlInfo[1] = tmp[1];
            urlInfo[2] = tmp[2];
            urlInfo[3] = tmp[3];
            urlInfo[4] = tmp[4];
            urlInfo[5] = tmp[5];
            urlInfo[6] = tmp[6];
            urlInfo[7] = tmp[7];
            int Pattern_Index = 8;
               
            //String url = tmp[Pattern_Index];
           // for(int i = (Pattern_Index + 1); i < tmp.length; i++)
            // url += "|" + tmp[i];
            urlInfo[8] = tmp[8];
            data.add(urlInfo);
          }
        }
      }
      br.close();
      Log.debugLog(strLine, "getReplayUrlPattern", "", "", "data is readed successfully");
      return data;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "getReplayUrlPattern", "", "", "", e);
      errMsg.append("Error in getting url filter pattern list. Please check error log.");
    }
    return null;
  }
  
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
      e.printStackTrace();
      Log.stackTraceLog(className, "ChangeOwnerOfFile", "", "", "Exception = ", e);
      return false;
    }
  }
  
  /**
   * This function creates the copy of replay_url_filter_pattern.dat and replay_url_pattern.dat for new profile
   * @param newProfileName
   * @return
   */
  public StringBuilder saveAsProfile(String newProfileName)
  {
    Log.debugLog(className, "saveAsProfile", "", "", "method called for ProfileName: "+ profileName + " and newProfileName: "+newProfileName);
    StringBuilder errMsg = new StringBuilder();
    try
    {
      ReplayUrlFilterPattern replayUrlFilterPtn = new ReplayUrlFilterPattern(newProfileName, userName);
      ArrayList<String[]> data1 = getReplayUrlFilterPattern(errMsg);
      replayUrlFilterPtn.saveReplayUrlFilterPattern(getArrayFromList(data1));
      ArrayList<String[]> data2 = getReplayUrlPattern(errMsg);
      replayUrlFilterPtn.saveReplayUrlPattern(getArrayFromList(data2));
    }
    catch(Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "saveAsProfile", "", "", "", e);
    }
    return errMsg;
  }
  
  /**
   * This function returns the 2D array from ArrayList and ignoring header.
   * @param data
   * @return
   */
  private String[][] getArrayFromList(ArrayList<String[]> data)
  {
    String[][] arr = null;
    if(data != null)
    {
      arr = new String[data.size()-1][]; // -1 to ignore header
      if(data.size() == 1)
        arr = new String[0][0];
      for(int i = 1; i < data.size(); i++)
        arr[i-1] = data.get(i);
    }
    else
      arr = new String[0][0];
    return arr;
  }
  
  /**
   * This function crates the copy of replay_seeting.dat , replay_url_filter_pattern.dat and replay_url_pattern.dat for a new profile
   * and save new changes of replay_url_paatern.dat to new file.
   * @param newProfile
   * @param data
   * @param errMsg
   */
  public void saveAsProfileForURLPtn(String newProfile, String[][] data, StringBuilder errMsg)
  {
    Log.debugLog(className, "saveAsProfileForURLPtn", "", "", "method called for oldProfile: "+profileName + " newProfile: "+newProfile);
    try
    {
      ReplaySetting rs = new ReplaySetting(profileName, userName);
      rs.saveAsReplaySetting(newProfile);
      saveAsProfile(newProfile);
      ReplayUrlFilterPattern newRup = new ReplayUrlFilterPattern(newProfile, userName);
      newRup.saveReplayUrlPattern(data);
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "saveAsProfileForURLPtn", "", "", "", e);
      errMsg.append("Error in creating new profile. Please check error log.");
    }
  }
  
  public String getRequestPostBody(String fileName)
  {
    StringBuffer body = new StringBuffer();
    Log.debugLog(className, "getRequestPostBody", "", "", "method called");
    try
    {
      File requestBodyFile = new File( fileName);
      if(!requestBodyFile.exists())
      {
        return body.toString();
      }
      BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(requestBodyFile)));
      String strLine = null;
      while((strLine = br.readLine()) != null)
      {
         body.append(strLine + "\n");
      }
      br.close();
    }
    catch(Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "getRequestPostBody", "", "", "", e);
      return "";
    }
    return body.toString();
  }

  public boolean savePostFile(String postRequestData , String fileName)
  {
    boolean bool = false;
    Log.debugLog(className, "savePostFile", "", "", "method called");
    try
    {
      File requestBodyFile = new File(fileName);
      if(requestBodyFile.exists())
      {
        Log.debugLog(className, "savePostFile", "", "", "repaly url filter pattern file alreagy present. Deleting it");
        requestBodyFile.delete();
      }
      
      File replayUrlFilterDir = new File(fileDirPath + "/" + profileName);
      if(!replayUrlFilterDir.exists())
      {
        Log.debugLog(className, "savePostFile", "", "", "Director for profilr '" + profileName +"' is not present. Creating it.");
        replayUrlFilterDir.mkdirs();
      }
      
      FileWriter fw = new FileWriter(requestBodyFile);
      fw.write(postRequestData);
      fw.flush();
      fw.close();
      
      ChangeOwnerOfFile(fileDirPath + "/" + profileName + "/" + requestBodyFile, userName);
      ChangeOwnerOfFile(fileDirPath + "/" + profileName, userName);
	  ChangeOwnerOfFile(requestBodyFile.getAbsolutePath(),"netstorm");
      Log.debugLog(className, "savePostFile", "", "", "replay url pattens saved successfully");
	  
    }
    catch(Exception e)
    {
      e.printStackTrace();
      Log.stackTraceLog(className, "savePostFile", "", "", "", e);
      return false;
    }
    return true;
  }
}


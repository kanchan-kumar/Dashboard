/**----------------------------------------------------------------------------
 * Name       RTGProfileData.java
 * Purpose    To load all profile data
 * @author    Prabhat
 * Modification History
 *        10/28/2010 - Prabhat - Initial Version
 *---------------------------------------------------------------------------**/
package pac1.Bean;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Set;
import java.util.Iterator;

public class RTGProfileData implements java.io.Serializable
{
  private static String className = "RTGProfileData";
  private final String EGP_FILE_EXTN = ".egp";
  private TreeMap<String, ProfileData> treeMapProfileData = new TreeMap<String, ProfileData>();

  private String totalProcessingTime;

  public RTGProfileData()
  {
    initRTGProfileData();
  }

  public String getTotalProcessingTime()
  {
    return totalProcessingTime;
  }

  public void setTotalProcessingTime(long startProcessingTime, long endProcessingTime)
  {
    this.totalProcessingTime = rptUtilsBean.convertMilliSecToSecs(endProcessingTime - startProcessingTime);
  }

  // this function is to load all profile data and set it in TreeMap
  private void initRTGProfileData()
  {
    Log.debugLog(className, "initRTGProfileData", "", "", "Start method");

    String profilePath = getEGPPath();
    ArrayList arrAllEGPFiles = getAllEGPFile();
    for (int i = 0; i < arrAllEGPFiles.size(); i++)
    {
      String profileName = arrAllEGPFiles.get(i).toString();
      String profileNameWithPath = profilePath + profileName + EGP_FILE_EXTN;
      Log.debugLogAlways(className, "initRTGProfileData", "", "", "profileNameWithPath = " + profileNameWithPath);
      ProfileData tempProfileData = new ProfileData(profileName, profileNameWithPath);
      treeMapProfileData.put(profileName, tempProfileData);
    }
  }

  public TreeMap<String, ProfileData> getTreeMapProfileData()
  {
    return treeMapProfileData;
  }

  // return EGP file path
  private String getEGPPath()
  {
    return (Config.getWorkPath() + "/webapps/profiles/");
  }

  /**
   * This method is used to return file names with its path
   * 
   * @param fileObj
   */

  private ArrayList getFilesOfFolderRecursively(File fileObj, ArrayList listFileNamesWithPath)
  {
    Log.debugLog(className, "getFilesOfFolderRecursively", "", "", "Start method");
    try
    {
      File[] files = fileObj.listFiles();
      if (files != null)
      {
        for (File file : files)
        {
          if (file.isFile())
          {
            String fileName = file.getName();
            if (fileName.lastIndexOf(EGP_FILE_EXTN) == -1)
              continue;
            String[] tempArr = rptUtilsBean.strToArrayData(fileName, ".");
            if (!(tempArr.length == 2))
              continue;

            String filePath = file.getAbsolutePath();
            if (filePath.toUpperCase().contains("PROFILES"))
            {
              filePath = filePath.substring(filePath.indexOf("profiles") + 9, filePath.lastIndexOf("."));
              if (!listFileNamesWithPath.contains(filePath))
                listFileNamesWithPath.add(filePath);
            }
            else
              Log.debugLog(className, "getFilesOfFolderRecursively", "", "", "This filepath does not contain profiles keyword = " + filePath);
          }
          else
            getFilesOfFolderRecursively(file, listFileNamesWithPath);
        }
      }
      return listFileNamesWithPath;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getFilesOfFolderRecursively", "", "", "Exception caught - ", ex);
      return listFileNamesWithPath;
    }
  }

  // load all egp file in Array List
  public ArrayList getAllEGPFile()
  {
    Log.debugLog(className, "getAllEGPFile", "", "", "Start method");

    ArrayList arrAllEGPFiles = new ArrayList(); // Names of all EGP files
    File objEGPFile = new File(getEGPPath());

    try
    {
      arrAllEGPFiles = getFilesOfFolderRecursively(objEGPFile, arrAllEGPFiles);
      Log.debugLog(className, "getAllEGPFile", "", "", "Number of EGP files = " + arrAllEGPFiles.size() + " , EGPFiles = " + arrAllEGPFiles);

      return arrAllEGPFiles;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getAllEGPFile", "", "", "Exception - ", e);
      return null;
    }
  }

  public static void main(String[] args)
  {

  }
}

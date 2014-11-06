/**----------------------------------------------------------------------------
 * Name       UserProp.java
 * Purpose    The purpose of this class to keep the user property that is use in default profile selection
 *
 * @author    Ankit Khanijau
 * @version   3.7.6
 *
 * Modification History
 *   12/18/10:Ankit Khanijau Vashist:3.7.6 - Initial Version.
 *---------------------------------------------------------------------------**/
package pac1.Bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class UserProp implements Serializable
{
  private static String className = "UserProp";
  private final String PROP_FILE_EXTN = ".prop";
  private final String FAV_FILE_EXTN = ".egp";
  public final String SYSTEM_PROP_FILE = "System";
  static final String DEFAULT_PROFILE_NAME = "_default";

  private HashMap hashMapObject = new HashMap();

  public UserProp() {}

  public HashMap getHashMapObject()
  {
    return hashMapObject;
  }

  //get path to /webapps/sys/properties/ under which all ".prop" files are exist.
  private String getUserPropFilePath()
  {
    return (Config.getWorkPath() + "/webapps/sys/properties/");
  }


  // use to load all ".prop" files and return in arraylist
  private ArrayList loadAllPropFiles()
  {
    ArrayList propFileNames = new ArrayList();

    String propFilePath = getUserPropFilePath();
    File objFileName = new File(propFilePath);
    if(!objFileName.exists())
    {
      Log.errorLog(className, "loadAllPropFiles", "", "", "Error: Property file is not exist at path " + propFilePath);
      return null;
    }

    try
    {
      String[] tempArray = objFileName.list();

      for(int i = 0; i < tempArray.length; i++)
      {
        if(tempArray[i].lastIndexOf(PROP_FILE_EXTN) == -1)
          continue;

        String[] tempPropFileName = rptUtilsBean.strToArrayData(tempArray[i], ".");

        if(tempPropFileName.length == 2)
        {
          propFileNames.add(tempPropFileName[0]);
          Log.debugLog(className, "loadAllPropFiles", "", "", "Adding '.prop' file name in ArrayList = " + tempPropFileName[0]);
        }
      }

      return propFileNames;
     }
     catch(Exception e)
     {
       Log.stackTraceLog(className, "loadAllPropFiles", "", "", "Exception - ", e);
       return null;
     }
   }

  // retrieve all data of user's ".prop" file in structured way(as required.)
  /*
   * favoriteData[0] contain Project Name.
   * favoriteData[1] contain Sub-Project Name.
   * favoriteData[2] contain
   * 
   * i.e FAVORITE default/default TestFolder/BlankFolder_2/testGlobal
   *     FAVORITE All/All _default
   *     FAVORITE default/default Fav
   */
  private String[] getFavoriteData(String getLine)
  {
    Log.debugLog(className, "getFavoriteData", "", "", "Method Called.");
    String[] favoriteData = new String[3];

    try
    {
      String[] tempData = getLine.split(" ");

      if((tempData != null) && (tempData.length == 3))
      {
        try
        {
          String[] tempProject = rptUtilsBean.strToArrayData(tempData[1], "/");

          if((tempProject != null) && (tempProject.length == 2))
          {
            favoriteData[0] = tempProject[0];
            favoriteData[1] = tempProject[1];
            favoriteData[2] = tempData[2];
          }
        }
        catch(Exception e)
        {
          Log.errorLog(className, "getFavoriteData", "", "", "Error occuring while readin data (Project and FAVORITE): " + e);
          return null;
        }
        
        return favoriteData;
      }
      else
        return null;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "getFavoriteData", "", "", "Error occuring while readin data (subProject and project): " + e);
      return null;
    }
    
  }

  // reading file line by line.
  public void readFile()
  {
    Log.debugLog(className, "readFile", "", "", "Method Called.");
    try
    {
      String profileNameWithPath = getUserPropFilePath();
      ArrayList arrListPropFileNames = loadAllPropFiles();
      PropFileData objPropFileData = null;

      if(arrListPropFileNames != null)
      {
        for(int i = 0; i < arrListPropFileNames.size(); i++)
        {
          File propFileName = new File(profileNameWithPath + arrListPropFileNames.get(i).toString() + PROP_FILE_EXTN);

          FileInputStream fis = new FileInputStream(propFileName);
          BufferedReader br = new BufferedReader(new InputStreamReader(fis));

          objPropFileData = new PropFileData();//object of customize class is created.

          String strLine = "";
          while((strLine = br.readLine()) != null)
          {
            strLine = strLine.trim();
            if(strLine.length() == 0)//if line is empty.
              continue;

            if(!strLine.startsWith("FAVORITE"))//if '.prop' file does not contain 'FAVORITE' then it skip that line.
              continue;
            else
            {
              try
              {
                String[] data = getFavoriteData(strLine);
                
                if(data != null)
                {
                  objPropFileData.projectNamesList.add(data[0]); //Project Name.
                  objPropFileData.subProjectNamesList.add(data[1]);//Sub-Project Name.
                  objPropFileData.favoriteNamesList.add(data[2]); //name of '.egp' file.
                }
              }
              catch(Exception e)
              {
                Log.errorLog(className, "readFile", "", "", "Error in readin data.");
              }
            }
          }

          hashMapObject.put(arrListPropFileNames.get(i).toString(), objPropFileData);
          Log.debugLog(className, "readFile", "", "", "Object added in the map for user " + arrListPropFileNames.get(i).toString());
        }
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "readFile", "", "", "Exception - ", e);
    }
  }

  // this function is to update user & system prop file(s)
  public boolean updatePropFiles(String userName, String userDefaultProfile, String systemDefaultProfile, String projectName, String subProjectName)
  {
    Log.debugLog(className, "updatePropFiles", "", "", "Method Called. User name = " + userName + ", User default profile name = " + userDefaultProfile + ", System default profile name = " + systemDefaultProfile + ", Project name = " + projectName + ", Sub-Project name = " + subProjectName);

    try
    {
      String dataLine = "";
      String propDirPath = getUserPropFilePath();
      boolean updateFileFlag = true;

      // Create Properties directory first.
      File objFile = new File(propDirPath);
      
      if(!objFile.exists())
      {
        Log.debugLogAlways(className, "updatePropFiles", "", "", "User Property directory not exist going to create Properties directory.");
        objFile.mkdirs();
      }
      
      // Update User prop file first
      File fileObj = new File(propDirPath + userName + PROP_FILE_EXTN);

      if(!fileObj.exists())
      {
        Log.debugLog(className, "updatePropFiles", "", "", "User property file not exist going to create new file.");
        fileObj.createNewFile();
        updateFileFlag = false;
      }

      dataLine = "FAVORITE " + projectName + "/" + subProjectName + " " + userDefaultProfile;

      if(updateFileInfo(fileObj, dataLine, projectName, subProjectName, updateFileFlag))
        Log.debugLog(className, "updatePropFiles", "", "", "File " + fileObj.getAbsolutePath() + " updated successfully with data line = " + dataLine);
      else
        Log.debugLog(className, "updatePropFiles", "", "", "Error occur while updating file " + fileObj.getAbsolutePath() + " with data line = " + dataLine);

      // Now Update System File
      fileObj = new File(propDirPath + SYSTEM_PROP_FILE + PROP_FILE_EXTN);
      updateFileFlag = true;

      boolean systemFileCreationFlag = false;
      if(!fileObj.exists())
      {
        Log.debugLog(className, "updatePropFiles", "", "", "System property file not exist going to create new file.");
        fileObj.createNewFile();
        systemFileCreationFlag = true;
        updateFileFlag = false;
      }

      // if file not there going to create file first time
      if(systemFileCreationFlag)
      {
        dataLine = "FAVORITE All/All " + DEFAULT_PROFILE_NAME;

        if(updateFileInfo(fileObj, dataLine, projectName, subProjectName, updateFileFlag))
          Log.debugLog(className, "updatePropFiles", "", "", "File " + fileObj.getAbsolutePath() + " updated successfully with data line = " + dataLine);
        else
          Log.debugLog(className, "updatePropFiles", "", "", "Error occur while updating file " + fileObj.getAbsolutePath() + " with data line = " + dataLine);
      }

      dataLine = "FAVORITE " + projectName + "/" + subProjectName + " " + systemDefaultProfile;

      if(updateFileInfo(fileObj, dataLine, projectName, subProjectName, updateFileFlag))
        Log.debugLog(className, "updatePropFiles", "", "", "File " + fileObj.getAbsolutePath() + " updated successfully with data line = " + dataLine);
      else
        Log.debugLog(className, "updatePropFiles", "", "", "Error occur while updating file " + fileObj.getAbsolutePath() + " with data line = " + dataLine);

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "updatePropFiles", "", "", "Exception - ", e);
      return false;
    }
  }


  // this funtion is add or update file info
  private boolean updateFileInfo(File fileObj, String dataLine, String projName, String subProjName, boolean updateFileFlag)
  {
    Log.debugLog(className, "updateFileInfo", "", "", "Method Called.");

    try
    {
      Vector vecFileData = new Vector();

      if(updateFileFlag)
      {
        FileInputStream fis = new FileInputStream(fileObj);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));

        String strLine = "";
        String[] arrData = null;
        while((strLine = br.readLine()) != null)
        {
          if(strLine.trim().startsWith("FAVORITE"))
          {
            arrData = rptUtilsBean.strToArrayData(strLine.trim(), " ");

            // format of this line "FAVORITE All/All _default"
            if(arrData.length == 3)
            {
              arrData = rptUtilsBean.strToArrayData(arrData[1], "/");

              // if project/subproject entry already exist
              if((arrData[0].equals(projName)) && (arrData[1].equals(subProjName)))
                continue;
              else
                vecFileData.add(strLine);
            }
          }
          else
            vecFileData.add(strLine);
        }

        br.close();
        fis.close();

        // first delete exiisting file
        if(!fileObj.delete())
          Log.errorLog(className, "updatePropFiles", "", "", "Error in deleting " + fileObj.getAbsolutePath() + " file.");

        // create new file
        if(!fileObj.createNewFile())
          Log.errorLog(className, "updatePropFiles", "", "", "Error in creating " + fileObj.getAbsolutePath() + " file.");
      }

      FileOutputStream fout = new FileOutputStream(fileObj, true);  // Append mode
      PrintStream printStream = new PrintStream(fout);

      for(int i = 0; i < vecFileData.size(); i++)
      {
        printStream.println(vecFileData.get(i).toString());
        Log.debugLog(className, "updatePropFiles", "", "", "Data line '" + vecFileData.get(i).toString() + "' added to " + fileObj.getAbsolutePath() + " file.");
      }

      // Update data line now
      printStream.println(dataLine);

      Log.debugLog(className, "updatePropFiles", "", "", "Data line '" + dataLine + "' updated to " + fileObj.getAbsolutePath() + " file.");

      printStream.close();
      fout.close();

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "updateFileInfo", "", "", "Exception - ", e);
      return false;
    }
  }

  public static void main(String a[])
  {
    UserProp objUserProp = new UserProp();//Creating profileName class object.
    objUserProp.readFile();

    HashMap hashMapObject = objUserProp.getHashMapObject();

    Set userNameSet = hashMapObject.keySet();

    Iterator iterator = userNameSet.iterator();
    while(iterator.hasNext())
    {
      String userName = (String)iterator.next();
      PropFileData objPropFileData = (PropFileData)hashMapObject.get(userName);

      ArrayList projectNamesList = objPropFileData.getProjectNamesList();
      ArrayList subProjectNamesList  = objPropFileData.getSubProjectNamesList();
      ArrayList favoriteNamesList = objPropFileData.getFavoriteNamesList();

      for(int jj = 0; jj < projectNamesList.size(); jj++)
      {
        System.out.println("userName = " + userName + ", Project Name = " + projectNamesList.get(jj).toString() + ", subProjectNames = " + subProjectNamesList.get(jj).toString() + ", favoriteNames = "+ favoriteNamesList.get(jj).toString());
      }
    }
  }
}



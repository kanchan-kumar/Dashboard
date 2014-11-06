/**----------------------------------------------------------------------------
 * Name       ProfileTree.java
 * Purpose    Contains method to make tree structure of profile directory
 * @author    Manish Kumar Gupta
 * Modification History
 *---------------------------------------------------------------------------**/
package pac1.Bean;

import java.io.File;
import java.util.Collections;
import java.util.Vector;

public class ProfileTree implements java.io.Serializable
{
  public static String className = "";
  ProfileNode rootNode;

  public ProfileTree()
  {
  }

  /** This method is used to getRoot node for ProfileTree structure view.
   * 
   * @return
   */
  public Object getProfileTreeRootNode()
  {
    Log.debugLog(className, "getProfileTreeRootNode", "", "","Method called.");
    try
    {
      rootNode = new ProfileNode("Available Favorites");
      String absolutePathOfDir = Config.getWorkPath() + "/webapps/profiles/";
      File profileDirAsFile = new File(absolutePathOfDir);
      return (Object) getProfileNode(null, profileDirAsFile);
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getScriptTreeRootNode", "", "","Exception in getScriptTreeRootNode() -", ex);
      String errMsg = "Error in getting profile tree..";
      return (Object) errMsg;
    }
  }

  /** This method is used to make profile tree by recursively calling this method
   * 
   * @param profileNodeObject
   * @param fileObjectToMakeNode
   * @return ProfileNode
   */
  
	public ProfileNode getProfileNode(ProfileNode profileNodeObject, File fileObjectToMakeNode)
    {
      String curPath = fileObjectToMakeNode.getPath();
      ProfileNode profNodeObj = new ProfileNode(fileObjectToMakeNode.getName());
      
      if(!fileObjectToMakeNode.getName().toUpperCase().equals("WEB-INF".toUpperCase()))
      {
        if(fileObjectToMakeNode.isDirectory())
          profNodeObj.setType(1);
        else
          profNodeObj.setType(0);
        
        if (profileNodeObject != null)
          profileNodeObject.add(profNodeObj);

        Vector<String> vecFilesAndDirectory = new Vector<String>();
        String[] arrListAndFiles = fileObjectToMakeNode.list();

        for (int i = 0; i < arrListAndFiles.length; i++)
        {
          if(arrListAndFiles[i].contains(".egp") && arrListAndFiles[i].endsWith(".egp"))
            vecFilesAndDirectory.addElement(arrListAndFiles[i]);
          else if(!arrListAndFiles[i].contains("."))
            vecFilesAndDirectory.addElement(arrListAndFiles[i]);
        }
        Collections.sort(vecFilesAndDirectory, String.CASE_INSENSITIVE_ORDER);

        File file;
        Vector<String> vecFiles = new Vector<String>();

        for (int i = 0; i < vecFilesAndDirectory.size(); i++)
        {
          String fileObject = (String) vecFilesAndDirectory.elementAt(i);
          String newPath;
          if(curPath.equals("."))
            newPath = fileObject;
          else
            newPath = curPath + File.separator + fileObject;

          if((file = new File(newPath)).isDirectory() && !newPath.contains("."))
            getProfileNode(profNodeObj, file);
          else
          {
        	if(fileObject.contains(".egp") && fileObject.endsWith(".egp"))
        	  fileObject = fileObject.substring(0, fileObject.indexOf(".egp"));
            vecFiles.addElement(fileObject);
          }
        }
        for (int index = 0; index < vecFiles.size(); index++)
    	  profNodeObj.add(new ProfileNode(vecFiles.elementAt(index).toString()));
      }
      return profNodeObj;
	}
}

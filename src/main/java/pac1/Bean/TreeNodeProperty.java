/**----------------------------------------------------------------------------
 * Name       TreeNodeProperty.java
 * Purpose    The purpose of this class to keep the tree node property like name of parent
 *
 * @author    Prabhat Vashist
 * @version   3.7.6
 *
 * Modification History
 *   12/14/10:Prabhat Vashist:3.7.6 - Initial Version.
 *---------------------------------------------------------------------------**/

package pac1.Bean;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.Set;

public class TreeNodeProperty implements java.io.Serializable
{
  private static final String className = "TreeNodeProperty";
  private HashMap hashMapForNodeInfo = new HashMap();
  private LinkedHashSet linkedHashSetForNode = new LinkedHashSet();

  public TreeNodeProperty()
  {
  }

  // this function is to set node info data in hashMapForNodeInfo Map
  public void init()
  {
    Log.debugLog(className, "init", "", "", "Method called.");

    try
    {
      Vector vecFileData = readFile(getTreeNodePropertyFilePath());

      if((vecFileData == null) || (vecFileData.size() == 0))
      {
        Log.errorLog(className, "init", "", "", "Error: Error in getting node info.");
        return;
      }

      String dataLine = "";
      for(int i = 0; i < vecFileData.size(); i++)
      {
        dataLine = vecFileData.get(i).toString();

        String[] tempArrData = rptUtilsBean.strToArrayData(dataLine, "=");

        if(tempArrData.length == 2)
        {
          String tempNodeName = tempArrData[0];

          tempArrData = rptUtilsBean.strToArrayData(tempArrData[1], ",");

          for(int j = 0; j < tempArrData.length; j++)
          {
            // put unique node name in set
            linkedHashSetForNode.add(tempNodeName.trim());

            // put <grp id, node name> in hash map
            hashMapForNodeInfo.put(tempArrData[j].trim(), tempNodeName.trim());
            Log.debugLog(className, "init", "", "", "Adding value in node info map (Group Id/Node Name) = (" + tempArrData[j] + "/" + tempNodeName + ").");
          }
        }
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "init", "", "", "Exception - ", e);
    }
  }

  // This function return the TreeNodeProperty File Path
  private String getTreeNodePropertyFilePath()
  {
    return(Config.getWorkPath() + "/webapps/sys/executionTree.prop");
  }

  // Methods for reading the File
  private Vector readFile(String fileWithPath)
  {
    Log.debugLog(className, "readFile", "", "", "Method called. FIle Name = " + fileWithPath);

    try
    {
      Vector vecFileData = new Vector();
      String strLine;

      File fileObj = new File(fileWithPath);

      if(!fileObj.exists())
      {
        Log.errorLog(className, "readFile", "", "", "Tree Node Property File not found, filename - " + fileWithPath);
        return null;
      }

      FileInputStream fis = new FileInputStream(fileWithPath);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));

      while((strLine = br.readLine()) !=  null)
      {
        strLine = strLine.trim();
        if(strLine.startsWith("#"))
          continue;
        if(strLine.length() == 0)
          continue;

        Log.debugLog(className, "readFile", "", "", "Adding line in vector. Line = " + strLine);
        vecFileData.add(strLine);
      }

      br.close();
      fis.close();

      return vecFileData;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "readFile", "", "", "Exception - ", e);
      return null;
    }
  }

  // this function is to return hashMapForNodeInfo
  public HashMap getHashMapForNodeInfo()
  {
    return hashMapForNodeInfo;
  }


  // this function is to return linkedHashSetForNode
  public LinkedHashSet getLinkedHashSetForNode()
  {
    return linkedHashSetForNode;
  }

  // this is for local testing
  public static void main(String[] args)
  {
    TreeNodeProperty objTreeNodeProperty = new TreeNodeProperty();
    objTreeNodeProperty.init();

    HashMap hashMapForNodeInfo = objTreeNodeProperty.getHashMapForNodeInfo();

    Set allGroupIdSet = hashMapForNodeInfo.keySet();

    Iterator iterator = allGroupIdSet.iterator();
    while(iterator.hasNext())
    {
      String grpId = (String)iterator.next();
      String nodeName = (String)hashMapForNodeInfo.get(grpId);

      System.out.println("grpId = " + grpId + ", Node name = " + nodeName);
    }

    Set setNodeName = objTreeNodeProperty.getLinkedHashSetForNode();
    iterator = setNodeName.iterator();
    while(iterator.hasNext())
    {
      String nodeName = (String)iterator.next();

      System.out.println("Node name = " + nodeName);
    }
  }
}

/**----------------------------------------------------------------------------
 * Name       UserProp.java
 * Purpose    The purpose of this class to keep the arraylist of project name, subproject name and default profile name
 *
 * @author    Ankit Khanijau
 * @version   3.7.6
 *
 * Modification History
 *   12/18/10:Ankit Khanijau Vashist:3.7.6 - Initial Version.
 *---------------------------------------------------------------------------**/
package pac1.Bean;

import java.io.Serializable;
import java.util.ArrayList;

public class PropFileData implements Serializable
{
  ArrayList projectNamesList, subProjectNamesList, favoriteNamesList;

  public PropFileData()
  {
    projectNamesList = new ArrayList();
    subProjectNamesList = new ArrayList();
    favoriteNamesList = new ArrayList();
  }

  public ArrayList getProjectNamesList()
  {
    return projectNamesList;
  }

  public ArrayList getSubProjectNamesList()
  {
    return subProjectNamesList;
  }

  public ArrayList getFavoriteNamesList()
  {
    return favoriteNamesList;
  }
}

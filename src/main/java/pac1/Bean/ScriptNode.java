/**----------------------------------------------------------------------------
 * Name       ScriptNode.java
 * Purpose    contain information for each node in the
 *            script tree.
 *            extends DefaultMutableTreeNode
 *            methods 
 * @author    Atul
 * Modification History
 *---------------------------------------------------------------------------**/
package pac1.Bean;
import javax.swing.tree.DefaultMutableTreeNode;

public class ScriptNode extends DefaultMutableTreeNode
{
  private String className = "ScriptNode";
  private String name = "";
  private int type;
  private boolean canWrite;
  private boolean canRead;
  private boolean canExecute;
  private boolean canDelete;
  
  private boolean isPermSet;

  public static final int NONE = -2;
  public static final int UNKNOWN = -1;
  public static final int DIR = 0;
  public static final int C = 1;
  public static final int CAPTURE = 2;
  public static final int DETAIL = 3;
  public static final int H = 4;
  public static final int RUN_LOGIC = 5;
  public static final int LIB = 6;
  public static final int FLOW_INIT = 7;
  public static final int FLOW_EXIT = 8;
  public static final int FLOW_FILE = 9;
  public static final int REG_SPEC = 10;
  public static final int USER_INIT = 11;
  public static final int USER_EXIT = 12;
  public static final int USER_RUNLOGICNODE = 13;
  
  DefaultMutableTreeNode defaultMutableTreeNode = null;
  
  
  public static final int SCRIPT_LEVEL = 3;
  public static final int SUB_PROJECT_LEVEL = 2;
  public static final int PROJECT_LEVEL = 1;
  public static final int ROOT_LEVEL = 0;
  public static final int FILE_LEVEL = 4;
  public static final int HTTP_REQ_LEVEL = 4;
  public static final int HTTP_BODY_FILE = 5;

  public ScriptNode(DefaultMutableTreeNode node)
  {
    this.defaultMutableTreeNode = node;
  }

  public ScriptNode(String name)
  {
    super(name);
    this.name = name;
  }

  public void setType(int type)
  {
    this.type = type;
  }
  
  public void setCanWrite(boolean canWrite)
  {
    this.canWrite = canWrite;
  }
  
  public void setCanRead(boolean canRead)
  {
    this.canRead = canRead;
  }
  
  public void setCanDelete(boolean canDelete)
  {
    this.canDelete = canDelete;
  }

  
  public void setCanExecute(boolean canExecute)
  {
    this.canExecute = canExecute;
  }
  
  public void setIsPermSet(boolean isPermSet)
  {
    this.isPermSet = isPermSet;
  }

  public boolean getCanWrite()
  {
    return this.canWrite;
  }
  
  public boolean getCanRead()
  {
    return this.canRead;
  }
  
  public boolean getCanDelete()
  {
    return this.canDelete;
  }
  
  public boolean getCanExecute()
  {
    return this.canExecute;
  }
  
  public boolean getIsPermSet()
  {
    return this.isPermSet;
  }

  public String getName()
  {
    return name;
  }
  public void setName(String name)
  {
    this.name = name;
  }

  public int getType()
  {
    return type;
  }
}

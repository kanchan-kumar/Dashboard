/*--------------------------------------------------------------------
  @Name    : UserPermission.java
  @Author  : Atul
  @Purpose : Contain the user information as per login 

  @Modification History: 22/01/09 - Initial version
----------------------------------------------------------------------*/
package pac1.Bean;
import java.util.Vector;

import pac1.Bean.CmdExec;

public class UserPermission
{
  private String userName;
  private String groupName;
  private String userType;
  private CmdExec cmdExec;

  public UserPermission(String userName, String groupName, String userType)
  {
    this.userName = userName;
    this.groupName = groupName;
    this.userType = userType;
    this.cmdExec = new CmdExec();
  }

  public boolean isUserPermitted(String toCheck)
  {
    if (userType.equalsIgnoreCase("Observers"))
    {
      if (toCheck.equals("r"))
        return true;
      else return false;
    }
    else
    // means user is engineer type
    {
      if (toCheck.equals("r"))
        return true;
      else if (toCheck.equals("w"))
        return true;
      /*
       * else if(toCheck.equals("x")) return true;
       */
      else
      {
        return true;
      }
    }
  }

  /**
   * @param objType -
   *          [-f] <for file> OR [-d] for directory
   * @param fullPath -
   *          full path for the file OR directory
   * @param toCheck -
   *          to be check for permission like <r>, <w> OR <x>
   * @return
   */
  public boolean isObjectPermitted(String objType, String toCheck, String fullPath, StringBuffer errMsg)
  {
    String cmdName = "nsi_check_perm";
    String cmdArgs = "-p" + " " + toCheck + " -" + objType + " " + fullPath;
    // Vector vecCmdOutput = servletReport.runCmd(ScriptMain.urlCodeBase,
    // "../NetstormServlet", "nsu_copy_script", args, "",
    // CmdExec.NETSTORM_CMD,scriptMain.userName, null, errMsg);
    Vector vecCmdOutput = null;
      vecCmdOutput = cmdExec.getResultByCommand(cmdName, cmdArgs, CmdExec.NETSTORM_CMD, userName, null);

    if (vecCmdOutput == null)
    {
      System.out.println("vecCmdOutput = " + null);
      // JOptionPane.showMessageDialog(null, errMsg.toString(), "Error",
      // JOptionPane.ERROR_MESSAGE);
      return false;
    }
    if ((vecCmdOutput.size() > 0) && ((String) vecCmdOutput.lastElement()).startsWith("ERROR"))
    {
      errMsg = new StringBuffer();
      for (int i = 0; i < (vecCmdOutput.size() - 1); i++)
        errMsg.append(vecCmdOutput.elementAt(i).toString() + "\n");
      // JOptionPane.showMessageDialog(null, errMsg.toString(), "Error",
      // JOptionPane.ERROR_MESSAGE);
      return false;
    }
    return true;
  }
}

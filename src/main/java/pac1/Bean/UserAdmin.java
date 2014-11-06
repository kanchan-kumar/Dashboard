//
// Name    : UserAdmin.java
// Author  : Prateek/Neeraj
// Purpose : Provide functionality
// 1 Add new UserID and Password
// 2 Delete existing user
// 3 validate user
//
// It use MD5 algorithim tech to generate encrypted password
// and also manage data persistently

package pac1.Bean;
import java.io.*;
import java.util.*;

import pac1.Bean.*;

public class UserAdmin
{
  public static String[] arrUname = null;
  static String className = "UserAdmin";

  public UserAdmin()
  {

  }

  // Name: md5Hash
  // Purpose: create string(password) into encryted form.Use MD5Algo class
  // Arguments:
  //  Arg1: String to be encrypted
  // Return: Encrypted String
  // Example: To add new user for access netstorm
  //   md5Hash("monitor")
  //
  public static String md5Hash(String password)
  {
    String strTestData;
    char chrTestData[] = new char[64];
    char chrTestBuffer[] = new char[1000];
    MD5Algo md5Test = new MD5Algo();
    strTestData = new String(password);
    chrTestData = strTestData.toCharArray();
    md5Test.update(chrTestData,chrTestData.length);
    md5Test.md5final();
    return(md5Test.toHexString().toString());
  }

  public static boolean writeFile(String uname, String pass, String param)
  {
    String str = "";
    FileInputStream fin = null;
    FileOutputStream fout = null;
    BufferedReader br = null;
    PrintStream ps = null;
    String encrpytPass = "";
    try
    {
      String Path = Config.getWorkPath() + "/webapps/sys/passwd";
      Path=Path.trim();

      if(pass != "")
      {
        encrpytPass = md5Hash(pass);
        str = uname + "|" + encrpytPass;
        str = str.trim();
      }

      File file = new File(Path);

      if(file.exists())
      {
        fin = new FileInputStream(file);
        br = new BufferedReader(new InputStreamReader(fin));
        String readLine;
        Vector vecData = new Vector(10,3);
        Vector vecUname = new Vector(10,3);
        int flag = 0, k = 0;

        if(param.equals("userName"))
        {

          while((readLine = br.readLine()) != null)
          {
            if((readLine.trim().equals("")) || (readLine.trim().startsWith("#")))
            {}
            else
              vecUname.addElement(readLine);
          }
          arrUname = new String[vecUname.size()];
          for(int count = 0 ; count < vecUname.size() ; count++)
          {
            String parseData = (vecUname.elementAt(count).toString());
            StringTokenizer data = new StringTokenizer(parseData, "|");
            while(data.hasMoreElements())
            {
              arrUname[k] = data.nextToken();
              data.nextToken();
              k++;
            }
          }
          if(arrUname.length > 0)
            return true;
          else
            return false;
        }

        while((readLine = br.readLine()) != null)
        {
          if((readLine.trim().equals("")) || (readLine.trim().startsWith("#")))
          {
            vecData.addElement(readLine);
          }
          else
          {
            StringTokenizer value = new StringTokenizer(readLine, "|");
            String[] arrValid = new String[value.countTokens()];
            int j = 0;
            vecData.addElement(readLine);

            while(value.hasMoreTokens())
            {
              arrValid[j] = value.nextToken();

              if(uname.equals(arrValid[j]) && param.equals("add"))
                return false;
              if(uname.equals(arrValid[j]) && param.equals("delete"))
              {
                flag = 1;
                vecData.remove(readLine);
              }
              if(uname.equals(arrValid[j]) && param.equals("validate"))
              {
                String checkData = readLine;
                StringTokenizer st = new StringTokenizer(checkData, "|");
                String[] arrData = new String[st.countTokens()];
                while(st.hasMoreTokens())
                {
                  arrData[0] = st.nextToken();
                  arrData[1] = st.nextToken();
                }
                if(encrpytPass.equals(arrData[1]))
                  return true;
                else
                  Log.debugLog(className, "writeFile", "", "", "Password not match");
              }
              j++;
            }
          }
        }
        if(param.equals("delete"))
        {
          if(flag == 0)
            return false;
          fout = new FileOutputStream(file, false);
          ps = new PrintStream(fout);
          for(int count = 0 ; count < vecData.size() ; count++)
          {
            String rewriteData = (vecData.elementAt(count).toString());
            ps.println(rewriteData);
          }
          return true;
        }
        else if(param.equals("add"))
        {
          ps = new PrintStream(new FileOutputStream(file, true));
          ps.println(str);
          return true;
        }
      }
      else
      {
        file.createNewFile();
        Log.debugLog(className, "writeFile", "", "", "File not found, but created succesfully");
      }

    }
    catch(FileNotFoundException foe)
    {
      System.out.println("validation file not found - "+foe);
      Log.errorLog(className, "writeFile", "", "", "FileNotFoundException - " + foe);
    }
    catch(IOException ioe)
    {
      System.out.println("Error in reading validation file - "+ioe);
      Log.errorLog(className, "writeFile", "", "", "IOException - " + ioe);
    }
    catch(Exception e)
    {
      System.out.println("Exception in userAdmin - "+e);
      Log.errorLog(className, "writeFile", "", "", "Exception - " + e);
    }
    finally
    {
      try
      {
        if(fout != null)
        {
          fin.close();
          br.close();
          fout.close();
          ps.close();
          fin = null;br = null;
          fout = null;ps = null;
        }
      }
      catch (Exception e)
      {
        Log.errorLog(className, "writeFile", "", "", "Exception in final block- " + e);
      }
    }
    return false;
  }

  public static String [] userName()
  {
    if(writeFile("", "", "userName"))
      return arrUname;
    else
      return arrUname;
  }

  // Name: validate
  // Purpose: To validate existing UserID and Password
  // Arguments:
  //  Arg1: UserID
  //  Arg2: Pasword
  // Return: true if UserID and Password matches else return false

  // Example: To validate existing user for access netstorm
  //   validate("netstorm", "monitor")
  //
  public static boolean validate(String uname, String pass)
  {
    if(writeFile(uname, pass, "validate"))
    {
      Log.debugLog(className, "validate", "", "", "User is valid");
      return true;
    }
    else
      Log.debugLog(className, "validate", "", "", "Invalid User");
    return false;
  }

  // Name: addUser
  // Purpose: To add new UserID and Password
  // Arguments:
  //  Arg1: UserID
  //  Arg2: Pasword
  // Return: true if User added succesfully else return false

  // Example: To add new user for access netstorm
  //   addUser("netstorm", "monitor")
  //
  public static boolean addUser(String uname, String pass)
  {
    if(writeFile(uname, pass, "add"))
    {
      Log.debugLog(className, "addUser", "", "", uname+" created succesfully");
      return true;
    }
    else
      Log.debugLog(className, "addUser", "", "", uname+" already exist");
    return false;
  }

  // Name: deleteUser
  // Purpose: To delete existing UserID
  // Arguments:
  //  Arg1: UserID
  // Return: true if User deleted succesfully else return false

  // Example: To delete above added user
  //   addUser("netstorm", "monitor")
  //
  public static boolean deleteUser(String uname)
  {
    if(writeFile(uname, "", "delete"))
    {
      Log.debugLog(className, "deleteUser", "", "", uname+" deleted succesfully");
      return true;
    }
    else
      Log.debugLog(className, "deleteUser", "", "", "No such User exist");
    return false;
  }

  /*
   * Method to get the role of specified user.
   * We need to get the info for all users by running the command nsu_show_user
   * We can find out the role by matching the user name. 
   * We also need to check the number of fields returning by nsu_show_user command for backward compatibility
   * If number of field are 9 then it means its a old file which doesn't have role so we need to return 'Standard'
   */
  public static String getRoleByUser(String userName)
  {
    Log.debugLog(className, "getRoleByUser", "", "", "Method Started");
    String role = "Standard";
    try
    {
      String cmdName = "nsu_show_user";
      CmdExec objCmdExec = new CmdExec();

      Vector vecCmdOutPut = objCmdExec.getResultByCommand(cmdName, "", CmdExec.NETSTORM_CMD, null, "root");
      
      Log.debugLog(className, "getRoleByUser", "", "", "Command nsu_show_user executed");
      
      if(vecCmdOutPut != null && vecCmdOutPut.size() > 0)
      {
        Log.debugLog(className, "getRoleByUser", "", "", "size of result vector = " + vecCmdOutPut.size());
        for(int i = 1; i < vecCmdOutPut.size(); i++)
        {
          String [] tempOutput = rptUtilsBean.split(vecCmdOutPut.get(i).toString(),"|");
          //check the user name. We are also checking the length of number of fields to handle backward compatibility
          if(tempOutput[0].trim().equals(userName) && tempOutput.length > 9)
          {
            role = tempOutput[6];
            Log.debugLog(className, "getRoleByUser", "", "", "role found = " + role);
          }
        }
      } 
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getRoleByUser", "", "", "Exception - ", ex);
    }
    return role;
  }
  
  public static void main(String args[])
  {
    if(args.length > 0)
    {
      if(args[0] != "" && args[0].equals("add") && args.length == 3)
      {
        String uname = args[1];
        String pass = args[2];
        addUser(uname, pass);
      }
      else if(args[0] != "" && args[0].equals("delete") && args.length == 2)
      {
        String uname = args[1];
        deleteUser(uname);
      }
      else if(args[0] != "" && args[0].equals("validate") && args.length == 3)
      {
        String uname = args[1];
        String pass = args[2];
        validate(uname, pass);
      }
      else
      {
        System.out.println("Invalid arguments.\n");
        System.out.println("**********************Valid Arguments********************");
        System.out.println("Adding new user type - java UserAdmin add UserID Password");
        System.out.println("Deleting user - java UserAdmin delete UserID");
        System.out.println("Validating user - java UserAdmin validate UserID Password");
        System.out.println("*********************************************************");
      }
    }
    else
      System.out.println("No arguments");
  }
}

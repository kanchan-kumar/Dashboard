/**
 *  * Purpose    Its copy of config.java. The main difference is contain non static variable.
 *               Static variable configProp its creating problem in jsp. one screen loading properties and other try to get value that time its giving null pointer exception.
 */
package pac1.Bean;
import java.util.*;
import java.io.*;
import pac1.Bean.*;

public class nsConfig
{
  Properties configProp = null;
  static String className = "nsConfig";
  static String workPath = null;
  static String hpdRootPath = null;
  //public static boolean bolServer = true; // True - If Config is used from Server or Bean. False - Used from client

  // Note - Prabhat 05/03/2008
  // This variable logFilePrefix have following value that must be set for logging
  // 1- "gui" for bean,
  // 2- "rtg" for server
  // 3- "" for applet(client)
  public String logFilePrefix = "gui";

  public static String getWorkPath()
  {
    if(workPath != null)
      return workPath;

    // Changed in 1.2.1 by Neeraj
    // String configPath = System.getProperty("user.dir") + "/webapps/netstorm/config/config.ini";
    String osname = System.getProperty("os.name").trim().toLowerCase();
    workPath = System.getProperty("NS_WDIR");
    if(workPath == null)
    {
      if(osname.startsWith("win"))
        workPath = "C:/home/netstorm/work";
      else
        workPath = "/home/netstorm/work";
    }
    return workPath;
  }

  public static String getHPDRootPath()
  {
    if(hpdRootPath != null)
      return hpdRootPath;

    String osname = System.getProperty("os.name").trim().toLowerCase();
    hpdRootPath = System.getProperty("HPD_ROOT");
    if(hpdRootPath == null)
    {
      if(osname.startsWith("win"))
        hpdRootPath = "C:/var/www/hpd";
      else
        hpdRootPath = "/var/www/hpd";
    }
    return hpdRootPath;
  }

  // this is to load the properties from sys path
  public static String pathToSys()
  {
    return getWorkPath() + "/webapps/sys/" ;
  }

  public static String pathToConfig()
  {
    return getWorkPath() + "/webapps/netstorm/config/" ;
  }


  public void loadConfigFile()
  {
    //Commenting these SOP's cause while using some common functions of bean files in utilities
    //these SOP's are coming for those utilities  : Saloni
    //System.out.println("loadConfigFile() method called.");
    try
    {
      if(logFilePrefix.equals(""))
      {
        if(configProp == null)
        {
          configProp = new Properties();
          //System.out.println("Creating new properties for applet....");
        }

        //System.out.println("No need to load properties for applet...");
        return;
      }

      configProp = new Properties();
      String configPath = pathToSys() + "config.ini";
      //System.out.println("Loading properties from " + configPath);
      // For Applet, it will give exception. To be handled later
      File configFile = new File(configPath);
      configProp.load(new FileInputStream(configFile));
    }
    catch(Exception e)
    {
      System.out.println("Exception in loadConfigFile() - " + e);
      // use Client API as this is used by both client and server
      //Log.errorLog(className, "loadConfigFile", "", "", "Exception - " + e);
    }
  }

  public String getValue(String key)
  {
    String value;

    if(configProp == null)
      loadConfigFile();

    value = configProp.getProperty(key);
    if(value == null)
      value = "";

    return (value.trim());
  }
  
  public String getIntValue(String key)
  {
     return getIntValue(key, "1");
  }
  
  /** 
   *  Method to get Integer value of keyword defined in config.ini.
   *  Parameters -> keyname and keyvalue
   *  returntype -> String
    **/
  
  public String getIntValue(String key, String DefaultValue)
  {
    String value;

    if(configProp == null)
      loadConfigFile();

    value = configProp.getProperty(key);
    if(value == null)
       value = DefaultValue;

    return (value.trim());
   }

  public String getValueWithPath(String key)
  {
    String value;

    value = getWorkPath() + "/" + getValue(key);

    return (value.trim());
  }

  public String getValueWithHPDPath(String key)
  {
    String value;

    value = getHPDRootPath() + "/" + getValue(key);

    return (value.trim());
  }

  public void addConfigParam(String key, String value)
  {
    if(configProp == null)
      loadConfigFile();
    configProp.setProperty(key, value);
  }

  public static void main(String[] args)
  {
    System.out.println("scenarioPath is " + Config.getValueWithPath("scenarioPath"));
  }
}

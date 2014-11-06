package pac1.Bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.Map.Entry;

public class TagSequenceDiagram 
{
  private final static String className = "TagSequenceDiagram";
  private String TAG_FILE_PATH;
  private final static String TAG_FILE_EXTENSION = ".tag";
  private String userName = "NA";
  private String flowPathInstance = "NA";
  private String testRun;
  private String tagName;
  private HashMap<String, String> FilterInfo = new HashMap<String, String>();
  private String[][] data = null;
  
  public TagSequenceDiagram(String testRun, String tagName)
  {
    this.testRun = testRun;
    this.tagName = tagName;
    TAG_FILE_PATH = Config.getWorkPath() + "/webapps/logs/TR" + testRun + "/ready_reports/tagged_flowpaths/";
  }
  
  public void setUserName(String userName)
  {
    this.userName = userName;
  }
  
  public String getUserName()
  {
    return userName;
  }
  
  public void setFlowPathInstance(String value)
  {
    flowPathInstance = value;
  }
  
  public String getFlowPathInstance()
  {
    return flowPathInstance;
  }
  
  public void setAttribute(String key, String value)
  {
    FilterInfo.put(key, value);
  }
  
  public String getAttribute(String key)
  {
    return FilterInfo.get(key);
  }
  
  /**
   * @return This function returns all filters applied on the page as comma separated.
   */
  public String getFilterInfo()
  {
    String info = "";
    Iterator<Entry<String, String>> itr = FilterInfo.entrySet().iterator();
    while(itr.hasNext())
    {
      Entry<String, String> pair = itr.next();
      if(info.equals(""))
        info += pair.getKey() + " = " + pair.getValue();
      else
        info += ", " + pair.getKey() + " = " + pair.getValue();
    }
    
    if(info.equals(""))
      info = "No Filter Applied";
    
    return info;
  }
  
  public void setData(String[][] data)
  {
    this.data = data;
  }
  
  public String[][] getData()
  {
    return data;
  }
  
  /**
   * This function save data of sequence diagram in a file named as tagName.
   * @param errMsg
   */
  public void saveTagData(StringBuffer errMsg)
  {
    Log.debugLog(className, "SaveData", "", "", "method called for tagName: "+tagName);
    try
    {
      if(tagName == null || tagName.trim().equals(""))
      {
        errMsg.append("Error in tagging sequence diagram. Please give tag name.");
        return;
      }
      
      //Create Directory structure if not available.
      File tagFileDir = new File(TAG_FILE_PATH);
      if(!tagFileDir.exists())
        tagFileDir.mkdirs();
      
      File tagFile = new File(TAG_FILE_PATH + tagName + TAG_FILE_EXTENSION);
      FileWriter fw = new FileWriter(tagFile);
      writePropertiesToTagFile(fw, errMsg);
      if(errMsg.toString().equals(""))
        writeAttributeToTagFile(fw, errMsg);
      if(errMsg.toString().equals(""))
        writeSeqDiaData(fw, errMsg);
      fw.close();
    }
    catch(Exception e)
    {
      e.printStackTrace();
      errMsg.append("Error in tagging sequence diagram. Please check error log.");
    }
  }
  
  private void writePropertiesToTagFile(FileWriter fw, StringBuffer errMsg)
  {
    Log.debugLog(className, "writePropertiesToTagFile", "", "", "method called");
    try
    {
      fw.write("UserName: " + userName + "\n");
      fw.write("FlowpathInstance: " + flowPathInstance + "\n");
      fw.write("TestRun: "+testRun + "\n");
    }
    catch(Exception e)
    {
      errMsg.append("Error in tagging sequence diagram. Please check error log.");
      Log.stackTraceLog(className, "writePropertiesToTagFile", "", "", "", e);
      e.printStackTrace();
    }
  }
  
  private void writeAttributeToTagFile(FileWriter fw, StringBuffer errMsg)
  {
    Log.debugLog(className, "writeAttributeToTagFile", "", "", "method called");
    try
    {
      Iterator<Entry<String, String>> itr = FilterInfo.entrySet().iterator();
      while(itr.hasNext())
      {
        Entry<String, String> pair = itr.next();
        fw.write(pair.getKey() + ": " + pair.getValue() + "\n");
      }
    }
    catch(Exception e)
    {
      errMsg.append("Error in tagging sequence diagram. Please check error log.");
      Log.stackTraceLog(className, "writeAttributeToTagFile", "", "", "", e);
      e.printStackTrace();
    }
  }
  
  private void writeSeqDiaData(FileWriter fw, StringBuffer errMsg)
  {
    Log.debugLog(className, "writeSeqDiaData", "", "", "method start");
    try
    {
      fw.write("SequenceDiagramData" + "\n");
      if(data != null)
      {
        for(int i = 0; i < data.length; i++)
        {
          for(int j = 0; j < data[i].length; j++)
          {
            if(j == 0)
             fw.write(data[i][j]);
            else
            {
              fw.write("|" + data[i][j]);
            }
          }
          fw.write("\n");
        }
      }
    }
    catch(Exception e)
    {
      errMsg.append("Error in tagging sequence diagram. Please check error log.");
      Log.stackTraceLog(className, "writeSeqDiaData", "", "", "", e);
      e.printStackTrace();
    }
  }
  
  /**
   * This function read all the properties and sequence diagram info from tag file.
   * @param errMsg
   */
  public void readTagData(StringBuffer errMsg)
  {
    Log.debugLog(className, "readTagData", "", "", "method called");
    try
    {
      File tagFile = new File(TAG_FILE_PATH + "/" + tagName + TAG_FILE_EXTENSION);
      BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(tagFile)));
      String str = null;
      boolean dataStart = false;
      ArrayList<String[]> seqData = new ArrayList<String[]>();
      while((str = br.readLine()) != null)
      {
        if(dataStart)
        {
          if(!str.trim().equals("") && !str.trim().startsWith("#"))
          {
            seqData.add(str.split("\\|"));
          }
        }
        else if(str.trim().equalsIgnoreCase("SequenceDiagramData"))
        {
          dataStart = true;
        }
        else
        {
          String[] tmpStr = str.split(":");
          if(tmpStr.length == 2)
          {
            if(tmpStr[0].equalsIgnoreCase("UserName"))
              userName = tmpStr[1];
            else if(tmpStr[0].equalsIgnoreCase("FlowpathInstance"))
                flowPathInstance = tmpStr[1];
            else if(tmpStr[0].equalsIgnoreCase("TestRun"))
              testRun = tmpStr[1];
            else
              FilterInfo.put(tmpStr[0], tmpStr[1]);
          }
        }
      }
      
      br.close();
      data = seqData.toArray(new String[0][0]);
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "readTagData", "", "", "", e);
      errMsg.append("Error in getting tag data. Please check error log.");
      e.printStackTrace();
    }
  }
  
  /**
   * This function returns the list of available tags under given testRun.
   * @param testRun
   * @return
   */
  public static ArrayList<String[]> getAllTaggedSequenceDiagramInfo()
  {
    Log.debugLog(className, "getAllTaggedSequenceDiagramInfo", "", "", "method called");
    ArrayList<String[]> taggedSeqDia = new ArrayList<String[]>();
    
    try
    {
      CmdExec cmdExec = new CmdExec();
      SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
      String strCmdName = "nsu_show_test_logs";
      String strCmdArgs = " -r ";
      Vector vecCmdOutput = new Vector();
      boolean isQueryStatus = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, "netstorm", null);
	  
      if(isQueryStatus && vecCmdOutput != null && vecCmdOutput.size() > 1)
      {
    	String arrDataValues[][] = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "");
    	for(int k = 1; k < arrDataValues.length; k++) //starting from 1 to exclude header
    	{
          File tagDir = new File(Config.getWorkPath() + "/webapps/logs/TR" + arrDataValues[k][2] + "/ready_reports/tagged_flowpaths/");
          if(tagDir.exists())
          {
            String fileList[] = tagDir.list();
            //taggedSeqDia.clear();
            if(fileList != null)
            {
              for(int i = 0; i < fileList.length; i++)
              {
                if(fileList[i].endsWith(TAG_FILE_EXTENSION))
                {
                  String data[] = new String[3];
                  int idx = fileList[i].lastIndexOf(".");
                  data[0] = fileList[i].substring(0, idx);
                  File f = new File(tagDir + "/" +fileList[i]);
                  data[1] = df.format(new Date(f.lastModified()));
                  data[2] = arrDataValues[k][2];
                  taggedSeqDia.add(data);
                }
              }
            }
          }
    	}
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getAllTaggedSequenceDiagramInfo", "", "", "", e);
      e.printStackTrace();
    }
    
    return taggedSeqDia;
  }
  
  /**
   * This function deletes tag file.
   * @param testRun, tagName
   */
  public static void deleteEntries(String testRun, String tagName){
    Log.debugLog(className, "deleteEntries", "", "", "method called");
    
    File tagFile = new File(Config.getWorkPath() + "/webapps/logs/TR" + testRun + "/ready_reports/tagged_flowpaths/" + tagName + ".tag");
    
    if(tagFile.exists()){
      tagFile.delete();
    }
  }
  
  public static void main(String arg[])
  {
    TagSequenceDiagram ob = new TagSequenceDiagram("1848", "tag2");
    StringBuffer errMsg = new StringBuffer();
    //save tag data
    /*ob.setUserName("SANGEETA");
    ob.setFlowPathInstance("23455465657554");
    ob.setAttribute("appName", "app1");
    String data[][] = new String[2][2];
    data[0][0] = "a1";
    data[0][1] = "a2";
    data[1][0] = "b1";
    data[1][1] = "b2";
    ob.setData(data);
    ob.SaveTagData(errMsg);
	*/
    
    //get all tagged seq dia 
    ArrayList<String[]> info = ob.getAllTaggedSequenceDiagramInfo();
    for(int i = 0; i < info.size(); i++)
    {
      String dt[] = info.get(i);
     /* for(int j = 0; j < dt.length; j++)
        System.out.print(dt[j] + "|");
      System.out.println();
	  */
    }
    
    //read tag data
    /*ob.readTagData(errMsg);
    System.out.println("useName: " + ob.getUserName());
    System.out.println("FlowPathIns: " +ob.getFlowPathInstance());
    System.out.println("filter: "+ob.getFilterInfo());
    String data[][] = ob.getData();
    for(int i = 0; i < data.length; i++)
    {
      for(int j = 0; j < data[i].length; j++)
       System.out.print(data[i][j] + "-");
      System.out.println();
    }*/
  } 
}

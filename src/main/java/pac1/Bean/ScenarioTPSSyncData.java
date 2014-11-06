package pac1.Bean;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ScenarioTPSSyncData implements java.io.Serializable
{

  private LinkedHashMap grpLinkedHashMap = new LinkedHashMap();
  private ArrayList arrTransList = new ArrayList();
  private ArrayList arrPageList = new ArrayList();
  private ArrayList arrSyncList = new ArrayList();
  
  //0 - new, 1 - in use, 2 delete
  private int isScriptDeleteOrNot = 2;
  private String scriptType = "";
  
  public LinkedHashMap getGrpLinkedHashMap()
  {
    return grpLinkedHashMap;
  }
  
  public ArrayList getTransList()
  {
    return arrTransList;
  }
  
  public ArrayList getPageList()
  {
    return arrPageList;
  }
  
  public ArrayList getSyncList()
  {
    return arrSyncList;
  }
  
  public int getIsScriptDeleteOrNot()
  {
    return isScriptDeleteOrNot;
  }
  
  public String getScriptType()
  {
    return scriptType;
  }
  
  /**************************setter method *******************************/
  
  public void setGrpLinkedHashMap(LinkedHashMap grpLinkedHashMap)
  {
    this.grpLinkedHashMap = grpLinkedHashMap;
  }
  
  public void setTransList(ArrayList arrTransList)
  {
    this.arrTransList = arrTransList;
  }
  
  public void setSyncList(ArrayList arrSyncList)
  {
    this.arrSyncList = arrSyncList;
  }
  
  public void setPageName(ArrayList arrPageList)
  {
    this.arrPageList = arrPageList;
  }
  
  public void setIsScriptDeleteOrNot(int isScriptDeleteOrNot)
  {
    this.isScriptDeleteOrNot = isScriptDeleteOrNot;
  }
  
  public void setScriptType(String scriptType)
  {
    this.scriptType = scriptType;
  }  
}

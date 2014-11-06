/**----------------------------------------------------------------------------
 * Name       ScriptFlowData.java
 * Purpose    This file is used to keep data while getting values from ScriptFlowUtils
 * @author    Saloni Tyagi
 * Modification History
 *
 *---------------------------------------------------------------------------**/
package pac1.Bean;

import java.io.Serializable;
import java.util.Vector;

public class ScriptFlowData implements Serializable
{
  private String scriptType = null;
  //this HTTP is required by nsu_create_scen tool as default script sub type : Saloni Tyagi
  private String subScriptType = "HTTP";
  private String projName = null;
  private String subProjName = null;
  private String scriptName = null;
  private String nsApiList[] = {"ns_web_url", "ns_smtp_send", "ns_pop_get", "ns_pop_stat", "ns_pop_list", "ns_ftp_get", "ns_dns_query", "ns_browser", "ns_link", "ns_button", "ns_check_box", "ns_edit_field", "ns_form", "ns_img_link", "ns_map_area", "ns_image_submit", "ns_radio_group", "ns_element", "ns_list", "ns_span", "ns_ext_browser_url"};

  private Vector<String> allFlowNames = null;
  private Vector<String> usedFlowNames = null;

  private Vector<String> allPageNames = null;
  private Vector<String> usedPageNames = null;

  private Vector<String> allTxNames = null;
  private Vector<String> usedTxNames = null;
  private Vector<String> flowTxNames = null;
  private String errMsg = null;
  private boolean isRunLogicModified = false;
  private boolean isRunLogicDataPresent = false;
  private String scriptAbsolPath = "";
  private Vector paramList = null;
  //make a new getter setter for get registration.spec file.
  private StringBuffer registrationsSpecFile;

  public StringBuffer getRegistrationsSpecFile()
  {
    return registrationsSpecFile;
  }

  public void setRegistrationsSpecFile(StringBuffer registrationsSpecFile)
  {
    this.registrationsSpecFile = registrationsSpecFile;
  }

  public boolean isRunLogicModified()
  {
    return isRunLogicModified;
  }

  public void setRunLogicModified(boolean isRunLogicModified)
  {
    this.isRunLogicModified = isRunLogicModified;
  }

  public String[] getNsApiList()
  {
    return nsApiList;
  }

  public String getScriptType()
  {
    return scriptType;
  }

  public String getSubScriptType()
  {
    return subScriptType;
  }

  public String getProjName()
  {
    return projName;
  }

  public String getSubProjName()
  {
    return subProjName;
  }

  public String getScriptName()
  {
    return scriptName;
  }

  public String[] setNsApiList(String[] nsApiList)
  {
    return nsApiList;
  }

  public void setScriptType(String scriptType)
  {
    this.scriptType = scriptType;
  }

  public void setSubScriptType(String subScriptType)
  {
    this.subScriptType = subScriptType;
  }

  public void setProjName(String projName)
  {
    this.projName = projName;
  }

  public void setSubProjName(String subProjName)
  {
    this.subProjName = subProjName;
  }

  public void setScriptName(String scriptName)
  {
    this.scriptName = scriptName;
  }

  public Vector<String> getAllFlowNames()
  {
    return allFlowNames;
  }

  public Vector<String> getUsedFlowNames()
  {
    return usedFlowNames;
  }

  public Vector<String> getAllTxNames()
  {
    return allTxNames;
  }

  public Vector<String> getUsedTxNames()
  {
    return usedTxNames;
  }

  public Vector<String> getFlowTxNames()
  {
    return flowTxNames;
  }

  public void setAllFlowNames(Vector<String> allFlowNames)
  {
    this.allFlowNames = allFlowNames;
  }

  public void setUsedFlowNames(Vector<String> usedFlowNames)
  {
    this.usedFlowNames = usedFlowNames;
  }

  public void setAllTxNames(Vector<String> allTxNames)
  {
    this.allTxNames = allTxNames;
  }

  public void setUsedTxNames(Vector<String> usedTxNames)
  {
    this.usedTxNames = usedTxNames;
  }

  public void setFlowTxNames(Vector<String> flowTxNames)
  {
    this.flowTxNames = flowTxNames;
  }

  public Vector<String> getAllPageNames()
  {
    return allPageNames;
  }

  public Vector<String> getUsedPageNames()
  {
    return usedPageNames;
  }

  public void setAllPageNames(Vector<String> allPageNames)
  {
    this.allPageNames = allPageNames;
  }

  public void setUsedPageNames(Vector<String> usedPageNames)
  {
    this.usedPageNames = usedPageNames;
  }

  public String getErrMsg()
  {
    return errMsg;
  }

  public void setErrMsg(String errMsg)
  {
    this.errMsg = errMsg;
  }

  public boolean isRunLogicDataPresent()
  {
    return isRunLogicDataPresent;
  }

  public void setRunLogicDataPresent(boolean isRunLogicDataPresent)
  {
    this.isRunLogicDataPresent = isRunLogicDataPresent;
  }

  public Vector getParamList()
  {
    return paramList;
  }

  public void setParamList(Vector paramList)
  {
    this.paramList = paramList;
  }

  public String getScriptAbsolPath()
  {
    return scriptAbsolPath;
  }

  public void setScriptAbsolPath(String scriptAbsolPath)
  {
    this.scriptAbsolPath = scriptAbsolPath;
  }
}

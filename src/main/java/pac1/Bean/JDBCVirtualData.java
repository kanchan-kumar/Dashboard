/**
 * JDBCVirtualData Use to Store data of each Command Response
 * **/

package pac1.Bean;

import java.io.Serializable;

public class JDBCVirtualData implements Serializable
{
  private String cmdName = "";
  private String cmdHashCode = "";
  private String resultClass = "";
  private boolean isSerialized = true;
  private String xmlData = "";
  private String cmdOutput = ""; 

  /*********************Getter************************/
  public String getCmdHashCode()
  {
    return cmdHashCode;
  }

  public String getCmdName()
  {
    return cmdName;
  }

  public String getResultClass()
  {  
    return resultClass;
  }
 
  public String getCmdOutput()
  {
    return cmdOutput;
  }
 
  public boolean getIsSerialized()
  {
    return isSerialized;
  }
 
  /*********************Setting ***************/

  public void setCmdHashCode(String cmdHashCode)
  {
    this.cmdHashCode = cmdHashCode;
  }

  public void setCmdName(String cmdName)
  {
    this.cmdName = cmdName;
  }
 
  public void setResultClass(String resultClass)
  {
    this.resultClass = resultClass;
  }

  public void setCmdOutput(String cmdOutput)
  {
    this.cmdOutput = cmdOutput;
  }

  public void setIsSerialized(String strSerialized)
  {
    if(strSerialized.equals("No"))
      isSerialized = false;
    else
      isSerialized = true;
  }

  public String getXmlData()
  {
    return xmlData;
  }

  public void setXmlData(String xmlData)
  {
    this.xmlData = xmlData;
  }
}


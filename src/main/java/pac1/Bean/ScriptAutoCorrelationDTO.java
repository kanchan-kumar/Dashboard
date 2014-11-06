package pac1.Bean;

/**----------------------------------------------------------------------------
 * Name       ScriptUrlInfo.java
 * Purpose    This is to generate to get Dump File info
 * @author    Ritesh Sharme
 * Modification History
 *---------------------------------------------------------------------------**/

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ScriptAutoCorrelationDTO implements Serializable
{
  private ArrayList PageList = null;//this contains list of pages
  private ArrayList URLList = null;//this contains list of URLs
  private ArrayList dumpPathList = null;//this contains list of dumpPath
  private StringBuffer dumpFileContents = new StringBuffer(); //this contains dumppath contains
  private boolean Status = false; //check status
  private StringBuffer errMassage = new StringBuffer(); //use for error message
  private StringBuffer RegistrationSpecContents = new StringBuffer();//contains data of registration.spec
  private LinkedHashMap pageUrlDumpMap = new LinkedHashMap();
  
  public LinkedHashMap getPageUrlDumpMap()
  {
    return pageUrlDumpMap;
  }
  public void setpageUrlDumpMap(LinkedHashMap dumpInfo)
  {
    this.pageUrlDumpMap = dumpInfo;
  }
  public ArrayList getPageList()
  {
    return PageList;
  }
  public void setPageList(ArrayList pageList)
  {
    PageList = pageList;
  }
  public ArrayList getURLList()
  {
    return URLList;
  }
  public void setURLList(ArrayList uRLList)
  {
    URLList = uRLList;
  }
  public ArrayList getDumpPathList()
  {
    return dumpPathList;
  }
  public void setDumpPathList(ArrayList dumpPathList)
  {
    this.dumpPathList = dumpPathList;
  }
  public StringBuffer getDumpFileContents()
  {
    return dumpFileContents;
  }
  public void setDumpFileContents(StringBuffer dumpFileContents)
  {
    this.dumpFileContents = dumpFileContents;
  }
  public boolean isStatus()
  {
    return Status;
  }
  public void setStatus(boolean status)
  {
    Status = status;
  }
  public StringBuffer getErrMassage()
  {
    return errMassage;
  }
  public void setErrMassage(StringBuffer errMassage)
  {
    this.errMassage = errMassage;
  }
  public StringBuffer getRegistrationSpecContents()
  {
    return RegistrationSpecContents;
  }
  public void setRegistrationSpecContents(StringBuffer registrationSpecContents)
  {
    RegistrationSpecContents = registrationSpecContents;
  }
}

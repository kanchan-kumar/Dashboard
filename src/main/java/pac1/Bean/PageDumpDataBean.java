/**
 * @author : Bala Sudheer
 * @purpose: stores clicked parameter value details in pagedump, these values are filled up by ajax call
 */
package pac1.Bean;

public class PageDumpDataBean {

  
  private static final String className = "PageDumpDataBean";
  private String userId = "";
  private String sessionId = "";
  private String scriptName = "";
  private String groupName = "";
  private String sessionStatus = "";
  private String absTime = "";
  private String pageName = "";
  private String pageStatus = "";
  private String parmeterization = "";
  private String generatorName = "";
  private String pageResponseTime = "";

  public String getGeneratorName()
  {
    return generatorName;
  }  

 
  public void setGeneratorName(String generatorName)
  {
    this.generatorName = generatorName;     
  }  

  public String getUserId() {
    return userId;
  }


  public void setUserId(String userId) {
    this.userId = userId;
  }


  public String getSessionId() {
    return sessionId;
  }


  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }


  public String getScriptName() {
    return scriptName;
  }


  public void setScriptName(String scriptName) {
    this.scriptName = scriptName;
  }


  public String getGroupName() {
    return groupName;
  }


  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }


  public String getSessionStatus() {
    return sessionStatus;
  }


  public void setSessionStatus(String sessionStatus) {
    this.sessionStatus = sessionStatus;
  }


  public String getAbsTime() {
    return absTime;
  }


  public void setAbsTime(String absTime) {
    this.absTime = absTime;
  }


  public String getPageName() {
    return pageName;
  }


  public void setPageName(String pageName) {
    this.pageName = pageName;
  }


  public String getPageStatus() {
    return pageStatus;
  }


  public void setPageStatus(String pageStatus) {
    this.pageStatus = pageStatus;
  }


  public String getParmeterization() {
    return parmeterization;
  }


  public void setParmeterization(String parmeterization) {
    this.parmeterization = parmeterization;
  }


  /** This method is to check from main
   * @param args
   */
  public static void main(String[] args) {
    Log.debugLog(className, "main", "", "", "Method Started");

  }


  /**
   * @param pageResponseTime the pageResponseTime to set
   */
  public void setPageResponseTime(String pageResponseTime) {
    this.pageResponseTime = pageResponseTime;
  }


  /**
   * @return the pageResponseTime
   */
  public String getPageResponseTime() {
    return pageResponseTime;
  }

}

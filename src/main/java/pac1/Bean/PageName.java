/**-----------------------------------------------------------------------------
 Name    : PageName.java
 Author  : Atul
 Purpose : This file will contain information for pages parsed after capture file,
           created for SMTP. 
           Presently containing page name and page type
 Modification History:
   09/01/10:Atul:3.5.3 - Initial Version
-----------------------------------------------------------------------------*/
package pac1.Bean;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageName
{
  public static final int HTTP = 0;
  
  public static final int SMTP = 1;
  public static final int POP_GET = 2;
  public static final int POP_STAT = 3;
  public static final int POP_LIST = 4;
  public static final int FTP_GET = 5;
  public static final int DNS_QUERY = 6;
  
  private int pageType;
  private String pageName;
  private String className = "PageName";
  
  public String getPageName()
  {
    return this.pageName;
  }
  
  public int getPageType()
  {
    return this.pageType;
  }
  
  public void setPageName(String pageName)
  {
    this.pageName = pageName;
  }
  
  public void setPageType(int pageType)
  {
    this.pageType = pageType;
  }
  /**
   * This method will read below - 
   *   web_url (index_html,
   *   smtp_send (index_html,
   *   
   * and return page name.
   * @param value
   * @param token
   * @return - if string not matched then it will return null.
   */
  public static String getPageNameFromRegix(String value, String token)
  {
    try
    {
      Pattern tempPattern = Pattern.compile("^" + token + "[ ]*[(][ ]*(([a-zA-Z]{1}[a-zA-Z0-9_]{0,31}))[ ]*[,][ ]*$");
      Matcher m = tempPattern.matcher(value);
      boolean matchFound = m.matches();

      if (matchFound)
        return  m.group(1);
      else
        return null;
    }
    catch(Exception ex)
    {
      return null;
    }
  }
}
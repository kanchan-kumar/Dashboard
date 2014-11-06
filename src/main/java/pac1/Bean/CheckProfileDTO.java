//-------------------------------------------------------//
//  Name   : CheckProfileDTO.java
//  Author : Ravi Kant Sharma
//  Purpose: DTO for Check Profile
//  Notes  : 
//  Modification History:
//   17 April 2013: Ravi Kant Sharma: - Initial Version
//-----------------------------------------------------//
package pac1.Bean;

import java.io.Serializable;
import java.util.HashMap;

public class CheckProfileDTO implements Serializable
{
  private static final long serialVersionUID = 1L;
  private String profileName = null;
  private String profileDesc = null;
  private int checkRuleCount = 0;
  private int compareRuleCount = 0;
  private String lastUpdatedDate = null;
  private String updatedBy = null;
  private HashMap<String, CheckRuleDTO> chkRuleDTO = new HashMap<String, CheckRuleDTO>();

  public HashMap<String, CheckRuleDTO> getChkRuleDTO()
  {
    return chkRuleDTO;
  }

  public void setChkRuleDTO(HashMap<String, CheckRuleDTO> chkRuleDTO)
  {
    this.chkRuleDTO = chkRuleDTO;
  }

  public String getProfileName()
  {
    return profileName;
  }

  public void setProfileName(String profileName)
  {
    this.profileName = profileName;
  }

  public String getProfileDesc()
  {
    return profileDesc;
  }

  public void setProfileDesc(String profileDesc)
  {
    this.profileDesc = profileDesc;
  }

  public int getCheckRuleCount()
  {
    return checkRuleCount;
  }

  public void setCheckRuleCount(int checkRuleCount)
  {
    this.checkRuleCount = checkRuleCount;
  }

  public int getCompareRuleCount()
  {
    return compareRuleCount;
  }

  public void setCompareRuleCount(int compareRuleCount)
  {
    this.compareRuleCount = compareRuleCount;
  }

  public String getLastUpdatedDate()
  {
    return lastUpdatedDate;
  }

  public void setLastUpdatedDate(String lastUpdatedDate)
  {
    this.lastUpdatedDate = lastUpdatedDate;
  }

  public String getUpdatedBy()
  {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy)
  {
    this.updatedBy = updatedBy;
  }

  public static void main(String[] args)
  {
    // TODO Auto-generated method stub

  }

}

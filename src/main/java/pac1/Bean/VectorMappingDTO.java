/*--------------------------------------------------------------------
  @Name    : VectorMappingDTO.java
  @Author  : Ravi Kant Sharma
  @Purpose : Bean for keeping VectorMapping.dat file
  @Modification History:
    27/03/14 Initial Version

----------------------------------------------------------------------*/

package pac1.Bean;

import java.io.Serializable;

public class VectorMappingDTO implements Serializable
{
  private static final long serialVersionUID = 1466580403580046107L;
  public static int VECTOR_MAPPING_LINE_LENGTH = 4;
  public static int OLD_GROUP_ID_INDEX = 0;
  public static int OLD_VECTOR_NAME = 1;
  public static int NEW_GROUP_ID_INDEX = 2;
  public static int NEW_VECTOR_NAME = 3;

  private int oldGroupId;
  private int newGroupId;
  private String oldVectorName;
  private String newVectorName;

  public int getOldGroupId()
  {
    return oldGroupId;
  }

  public void setOldGroupId(int oldGroupId)
  {
    this.oldGroupId = oldGroupId;
  }

  public int getNewGroupId()
  {
    return newGroupId;
  }

  public void setNewGroupId(int newGroupId)
  {
    this.newGroupId = newGroupId;
  }

  public String getOldVectorName()
  {
    return oldVectorName;
  }

  public void setOldVectorName(String oldVectorName)
  {
    this.oldVectorName = oldVectorName;
  }

  public String getNewVectorName()
  {
    return newVectorName;
  }

  public void setNewVectorName(String newVectorName)
  {
    this.newVectorName = newVectorName;
  }
}

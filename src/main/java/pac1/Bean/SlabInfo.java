/*--------------------------------------------------------------------
@Name    : SlabInfo.java
@Author  : Prabhat
@Purpose : To store all Information of each slabs in pdf's
@Modification History:
    01/12/2009 --> Prabhat  -->  Initial Version

----------------------------------------------------------------------*/

package pac1.Bean;

public class SlabInfo implements java.io.Serializable
{
  private final String className = "SlabInfo";
  // Indexes of Slab Line
  private transient final int SLAB_NAME_INDEX = 1;
  private transient final int SLAB_ID_INDEX = 2;
  private transient final int MIN_VALUE_INDEX = 3;
  private transient final int MAX_VALUE_INDEX = 4;
  private transient final int COLOR_INDEX = 5;

  private String slabName = "";
  private int slabID;
  private int slabMinValue;
  private int slabMaxValue;
  private String strColor = "";

  private String slabStatus = ""; // either it is valid or invalid

  private int minGranule = -1;
  private int maxGranule = -1;

  public SlabInfo(int minGranule, int maxGranule)
  {
    this.minGranule = minGranule;
    this.maxGranule = maxGranule;
  }


  // set all slab info
  public void setSlabsInfo(String dataLine)
  {
    try
    {
      String[] arrRcrd = rptUtilsBean.strToArrayData(dataLine.trim(), "|");
      if((dataLine.trim().startsWith("Slab")) && (arrRcrd.length == 6))
      {
        slabName = arrRcrd[SLAB_NAME_INDEX];
        String tempSlabId = arrRcrd[SLAB_ID_INDEX].trim();
        slabID = Integer.parseInt(tempSlabId);
        String tempSlabMinValue = arrRcrd[MIN_VALUE_INDEX].trim();
        slabMinValue = Integer.parseInt(tempSlabMinValue);
        String tempSlabMaxValue = arrRcrd[MAX_VALUE_INDEX].trim();
        slabMaxValue = Integer.parseInt(tempSlabMaxValue);
        strColor = arrRcrd[COLOR_INDEX].trim();
        setSlabStatus();
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "setSlabsInfo", "", "", "Exception - ", e);
    }
  }


  // set Slab status, on the basis of Slab validation
  private void setSlabStatus()
  {
    try
    {
      /*
      if Slab meat all this condition then set status valid else invalid
        1- MinSlabValue & MaxSlabValue must be divisible by MinGranule
        2- MinSlabValue should be less than to MaxSlabValue(-1(infinity) is exception case)
      */

      if(slabMaxValue == -1) // if slab tends to infinity
      {
        if(((slabMinValue % minGranule) == 0))
          slabStatus = "valid";
        else
          slabStatus = "invalid";
      }
      else
      {
        if((((slabMinValue % minGranule) == 0) && ((slabMaxValue % minGranule) == 0)) && (slabMinValue < slabMaxValue))
          slabStatus = "valid";
        else
          slabStatus = "invalid";
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "setSlabStatus", "", "", "Exception - ", e);
    }
  }

  // this function return the Slab Name
  public String getSlabName()
  {
    return slabName;
  }

  // this function return the Slab ID
  public int getSlabID()
  {
    return slabID;
  }

  // this function return the Slab min value
  public int getSlabMinValue()
  {
    return slabMinValue;
  }

  // this function return the Slab max value
  public int getSlabMaxValue()
  {
    return slabMaxValue;
  }

  // this function return the min Granuale value
  public int getMinGranuale()
  {
    return minGranule;
  }

  // this function return the color Name of slab
  public String getColorName()
  {
    return strColor;
  }

  // this function return the Slab status
  public String getSlabStatus()
  {
    return slabStatus;
  }
}

package pac1.Bean;

import java.util.ArrayList;
import java.util.Vector;

import pac1.Bean.GraphName.*;

public class ControlGraphStats implements java.io.Serializable
{
  private final String className = "ControlGraphStats";

  public transient GraphNames graphNames = null;

  private String eventType = "";
  private String messageToDisplay = "";
  private GraphUniqueKeyDTO[] arrGraphUniqueKeyDTOs = null;
  private int[] graphIndexs = null;
  private String srcUserName = "";
  private String srcHostName = "";

  private ArrayList arrayListAvgDataValue = new ArrayList(); // contains average data
  private ArrayList arrayListDataValue = new ArrayList(); // contains vector of graphData
  private ArrayList arrayListDataValueLast = new ArrayList(); // contains vector of last graphData
  private ArrayList arrayListIndex = new ArrayList(); // contains index of change graph(s) state

  public ControlGraphStats()
  {
  }

  public ControlGraphStats(GraphNames graphNames)
  {
    this.graphNames = graphNames;
  }

  // this function is to reset object
  public void resetObj()
  {
    this.messageToDisplay = "";
    this.arrayListAvgDataValue = new ArrayList();
    this.arrayListDataValue = new ArrayList();
    this.arrayListDataValueLast = new ArrayList();
    this.arrayListIndex = new ArrayList();
  }

  // this function is to create display Msg
  public void createMsgToDisplay()
  {
    String displayState = "Active";
    StringBuffer graphNamesBuff = new StringBuffer();
    String strGraphName = "";
    int count = 0;

    for(int i = 0; i < arrGraphUniqueKeyDTOs.length; i++)
    {
      if(count == 2)
      {
        graphNamesBuff.append(",\n");
        count = 0;
      }

      strGraphName = graphNames.getGraphNameByGraphUniqueKeyDTO(arrGraphUniqueKeyDTOs[i], true);
      if(count == 0)
        graphNamesBuff.append(strGraphName);
      else
        graphNamesBuff.append(", " + strGraphName);

      count++;
    }

    this.messageToDisplay = "Following graph(s) state changed to '" + displayState + "' successfully by " + srcUserName + " user.\n" + graphNamesBuff.toString();

    Log.debugLog(className, "createMsgToDisplay", "", "", messageToDisplay.toString());
  }

  public void addIndexToList(int index)
  {
    arrayListIndex.add(index + "");
  }

  public void addAvgDataToList(double avgData)
  {
    arrayListAvgDataValue.add(avgData + "");
  }

  public void addVecGraphDataToList(Vector vecDataValue)
  {
    arrayListDataValue.add(vecDataValue);
  }

  public void addVecGraphDataLastToList(Vector vecDataValueLast)
  {
    arrayListDataValueLast.add(vecDataValueLast);
  }

  public ArrayList getIndexList()
  {
    return arrayListIndex;
  }

  public ArrayList getAvgDataList()
  {
    return arrayListAvgDataValue;
  }

  public ArrayList getVecGraphDataList()
  {
    return arrayListDataValue;
  }

  public ArrayList getVecGraphDataLastList()
  {
    return arrayListDataValueLast;
  }


  public int[] getGraphIndexs()
  {
    return graphIndexs;
  }

  public GraphUniqueKeyDTO[] getarGraphUniqueKeyDTOs()
  {
    return arrGraphUniqueKeyDTOs;
  }

  public String getEventType()
  {
    return eventType;
  }

  public String getSrcHostName()
  {
    return srcHostName;
  }

  public String getSrcUserName()
  {
    return srcUserName;
  }

  public String getMessageToDisplay()
  {
    return messageToDisplay;
  }


  public void setGraphIndexs(int[] graphIndexs)
  {
    this.graphIndexs = graphIndexs;
  }

  public void setGraphUniqueKeyDTOs(GraphUniqueKeyDTO[] graphUniqueKeyDTOs)
  {
    this.arrGraphUniqueKeyDTOs = graphUniqueKeyDTOs;
  }

  public void setEventType(String eventType)
  {
    this.eventType = eventType;
  }

  public void setSrcHostName(String srcHostName)
  {
    this.srcHostName = srcHostName;
  }

  public void setSrcUserName(String srcUserName)
  {
    this.srcUserName = srcUserName;
  }

  public void setMessageToDisplay(String messageToDisplay)
  {
    this.messageToDisplay = messageToDisplay;
  }
}
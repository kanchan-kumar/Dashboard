package pac1.Bean;

import java.io.Serializable;

/**
 * Keeps Information about Graph. <br>
 * Graph Id, Group Id and Vector Name are unique identifier of Graph. <br>
 * 
 * @version 1.0
 * @see If u want to add any information related to graph, then create property here.
 */
public class GraphUniqueKeyDTO implements Serializable
{
  private static final long serialVersionUID = -2849228129588777456L;

  /* Identifier For Graph. */
  public static byte MERGE_GRAPH = 0; /* Identifier for Merge Graph. */
  public static byte MONITOR_GRAPH = 1; /* Identifier for Monitor Graph. */
  public static byte DERIVED_GRAPH = 2; /* Identifier For Derived Graph. */
  public static byte CUSTOM_METRICS_NODE = 3; /* Identifier For Custom Metrics. */
  public static byte PARENT_NODE = 4; /* Identifier For Custom Metrics. */
  public static byte CHILD_NODE = 5; /* Identifier For Custom Metrics. */

  private int groupId = -1; /* Group Id mentioned in testrun.gdf. */
  private int graphId = -1; /* Graph Id mentioned in testrun.gdf */
  private String vectorName = "NA"; /* Taken from testrun.gdf */
  private byte graphType = -1; /* Graph Type */
  private int graphDataIndex = -1; /* Graph Data Index for fetching data from packets. */
  private int graphNumber = -1; /* Used to Identify Graphs by Numbers. */
  private String generatorName = null;
  private String generatorTestNum = null;

  public String getGeneratorName()
  {
    return generatorName;
  }

  public void setGeneratorName(String generatorName)
  {
    this.generatorName = generatorName;
  }

  public String getGeneratorTestNum()
  {
    return generatorTestNum;
  }

  public void setGeneratorTestNum(String generatorTestNum)
  {
    if(generatorTestNum != null && generatorTestNum.equals("NA"))
      generatorTestNum = null;
    
    this.generatorTestNum = generatorTestNum;
  }

  /**
   * Overloaded Constructor to create object with Graph Type.
   * 
   * @param graphType
   */
  public GraphUniqueKeyDTO(byte graphType)
  {
    this.graphType = graphType;
  }

  /**
   * Overloaded Constructor to create object with Group Id, Graph Id and Vector Name.
   * 
   * @param groupId
   * @param graphId
   * @param vectorName
   */
  public GraphUniqueKeyDTO(int groupId, int graphId, String vectorName)
  {
    this.groupId = groupId;
    this.graphId = graphId;

    /* Check For object Equality/Compare Objects. As Vector Name is identified as NA(default), so taking vector name as "NA" default. */
    if (vectorName.trim().length() == 0)
      vectorName = "NA";

    this.vectorName = vectorName;
  }

  /**
   * Overloaded Constructor to Create Object with Group Id, Graph Id, vector name and Graph Type.
   * 
   * @param groupId
   * @param graphId
   * @param vectorName
   * @param graphType
   */
  public GraphUniqueKeyDTO(int groupId, int graphId, String vectorName, byte graphType)
  {
    this.groupId = groupId;
    this.graphId = graphId;

    /* Check For object Equality/Compare Objects. As Vector Name is identified as NA(default), so taking vector name as "NA" default. */
    if (vectorName.trim().length() == 0)
      vectorName = "NA";

    this.vectorName = vectorName;
    this.graphType = graphType;
  }

  /**
   * Returns the Graph Number Associated with Graph.
   * 
   * @return
   */
  public int getGraphNumber()
  {
    return graphNumber;
  }

  /**
   * Sets the Graph Number Associated with Graph.
   * 
   * @param graphNumber
   */
  public void setGraphNumber(int graphNumber)
  {
    this.graphNumber = graphNumber;
  }

  /**
   * Return the Graph Data Index of Graph.
   * 
   * @return
   */
  public int getGraphDataIndex()
  {
    return graphDataIndex;
  }

  /**
   * Set the Graph Data Index of Graph.
   * 
   * @param graphDataIndex
   */
  public void setGraphDataIndex(int graphDataIndex)
  {
    this.graphDataIndex = graphDataIndex;
  }

  /**
   * Return the Graph Type of Graph.
   * 
   * @return
   */
  public byte getGraphType()
  {
    return graphType;
  }

  /**
   * Sets the Graph Type of Graph.
   * 
   * @param graphType
   */
  public void setGraphType(byte graphType)
  {
    this.graphType = graphType;
  }

  /**
   * Return the Group Id of Graph.
   * 
   * @return
   */
  public int getGroupId()
  {
    return groupId;
  }

  /**
   * Set the Group Id of Graph.
   * 
   * @param groupId
   */
  public void setGroupId(int groupId)
  {
    this.groupId = groupId;
  }

  /**
   * Return the Graph Id of Graph.
   * 
   * @return
   */
  public int getGraphId()
  {
    return graphId;
  }

  /**
   * Set the Graph Id of Graph.
   * 
   * @param graphId
   */
  public void setGraphId(int graphId)
  {
    this.graphId = graphId;
  }

  /**
   * Return the vector name of Graph.
   * 
   * @return
   */
  public String getVectorName()
  {
    return vectorName;
  }

  /**
   * Set the vector name of graph.
   * 
   * @param vectorName
   */
  public void setVectorName(String vectorName)
  {
    this.vectorName = vectorName;
  }

  /**
   * Return the GGV(GroupId.GraphId.vectorName), separated by dot.
   * 
   * @return
   */
  public String getGroupIdGraphIdVectName()
  {
    return groupId + "." + graphId + "." + vectorName;
  }

  /**
   * Return the Information associated with object.
   */
  public String toString()
  {
    return groupId + "." + graphId + "." + vectorName + ", Idx = " + graphDataIndex + ", Gen Name = " + generatorName + ", Gen TR = " + generatorTestNum;
  }

  /**
   * Override the equals method to compare objects.
   */
  public boolean equals(Object graphUniqueKeyDTO)
  {
    /* Check the equality with same reference. */
    if (graphUniqueKeyDTO == this)
      return true;

    /* Check for object type. */
    if (!(graphUniqueKeyDTO instanceof GraphUniqueKeyDTO))
      return false;

    try
    {
      /* Casting Object to same class object. */
      GraphUniqueKeyDTO graphUniqueKeyDTO2 = (GraphUniqueKeyDTO) graphUniqueKeyDTO;

      /* Check Group Id. */
      boolean isGroupIdEqual = (groupId == graphUniqueKeyDTO2.getGroupId());

      /* Check Graph Id. */
      boolean isGraphIdEqual = (graphId == graphUniqueKeyDTO2.getGraphId());

      /* Check Vector Name. */
      String vectName2 = graphUniqueKeyDTO2.getVectorName();
      boolean isVectorNameEqual = ((vectorName == null && vectName2 == null) || (vectorName != null && vectorName.equals(graphUniqueKeyDTO2.getVectorName())));

      return (isGroupIdEqual && isGraphIdEqual && isVectorNameEqual);
    }
    catch (ClassCastException cx)
    {
      cx.printStackTrace();
      return false;
    }
  }

  /**
   * Override the HashCode method.
   */
  public int hashCode()
  {
    if (vectorName != null)
      return groupId + graphId + vectorName.hashCode();
    else
      return groupId + graphId;
  }
}

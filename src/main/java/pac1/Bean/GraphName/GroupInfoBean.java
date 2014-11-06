package pac1.Bean.GraphName;

import java.io.Serializable;

public class GroupInfoBean implements Serializable
{
  private static final long serialVersionUID = -2033930363495130108L;
  private String groupName = null;

  private String[] arrGraphNames = null;
  private int[] arrGraphIds = null;

  public int[] getArrGraphIds()
  {
    return arrGraphIds;
  }

  public void setArrGraphIds(int[] arrGraphIds)
  {
    this.arrGraphIds = arrGraphIds;
  }

  public String[] getArrGraphNames()
  {
    return arrGraphNames;
  }

  public void setArrGraphNames(String[] arrGraphNames)
  {
    this.arrGraphNames = arrGraphNames;
  }

  // To check graph type is scalar/vector
  private boolean isGroupTypeVector = false;

  // Storing Hirarchical Component
  private String hierarchicalComponent;

  // Storing Metrics Names
  private String metricsName;

  // Number of vectors in Group
  private int numOfVectors = 0;

  // Number of graphs
  private int numOfGraphs = 0;

  // Storing Group Description
  private String groupDescription;

  // Storing Vector Names
  private String[] arrIndiceNames;

  public String getGroupName()
  {
    return groupName;
  }

  public void setGroupName(String groupName)
  {
    this.groupName = groupName;
  }

  public String getHierarchicalComponent()
  {
    return hierarchicalComponent;
  }

  public void setHierarchicalComponent(String hierarchicalComponent)
  {
    this.hierarchicalComponent = hierarchicalComponent;
  }

  public String getMetricsName()
  {
    return metricsName;
  }

  public void setMetricsName(String metricsName)
  {
    this.metricsName = metricsName;
  }

  public int getNumOfVectors()
  {
    return numOfVectors;
  }

  public void setNumOfVectors(int numOfVectors)
  {
    this.numOfVectors = numOfVectors;
  }

  public int getNumOfGraphs()
  {
    return numOfGraphs;
  }

  public void setNumOfGraphs(int numOfGraphs)
  {
    this.numOfGraphs = numOfGraphs;
  }

  public String getGroupDescription()
  {
    return groupDescription;
  }

  public void setGroupDescription(String groupDescription)
  {
    this.groupDescription = groupDescription;
  }

  public String[] getIndicesNamesArray()
  {
    return arrIndiceNames;
  }

  public void setIndicesNamesArray(String[] arrIndiceNames)
  {
    this.arrIndiceNames = arrIndiceNames;
  }

  public boolean isGroupTypeVector()
  {
    return isGroupTypeVector;
  }

  public void setGroupTypeVector(boolean isGroupTypeVector)
  {
    this.isGroupTypeVector = isGroupTypeVector;
  }

  public String toString()
  {
    return "Group Name = " + groupName + ", Metrics Name =" + metricsName + ", isGroupTypeVector = " + isGroupTypeVector + ", Total Graphs = " + numOfGraphs;
  }
}

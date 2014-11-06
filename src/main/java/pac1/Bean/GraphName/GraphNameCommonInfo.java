/**
 * This class is Store info based on Group Id and Graph Id
 * 
 * @author Ravi Sharma
 * @since Netsorm Version 3.9.7
 * @Modification_History Ravi Kant Sharma - Initial Version 3.9.7
 * @version 3.9.7
 * 
 */
package pac1.Bean.GraphName;

import java.io.Serializable;

public class GraphNameCommonInfo implements Serializable
{
  private static final long serialVersionUID = 2484563566110542316L;
  private String[] arrGraphVectorNames = null;
  private String graphName = null;

  public String[] getArrGraphVectorNames()
  {
    return arrGraphVectorNames;
  }

  public void setArrGraphVectorNames(String[] arrGraphVectorNames)
  {
    this.arrGraphVectorNames = arrGraphVectorNames;
  }

  public String getGraphName()
  {
    return graphName;
  }

  public void setGraphName(String graphName)
  {
    this.graphName = graphName;
  }

}

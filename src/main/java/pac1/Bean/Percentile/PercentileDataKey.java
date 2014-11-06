package pac1.Bean.Percentile;

import java.io.Serializable;

import pac1.Bean.GraphUniqueKeyDTO;

public class PercentileDataKey implements Serializable
{
  private static final long serialVersionUID = 4264642769092601562L;

  // To Store Data key (e.g. Whole Scenario, Last 4 Hours, Specified Time)
  private String dataKey = null;

  private int graphType = -1;

  // To Store Graph Info (See GraphUniqueKeyDTO.java)
  private GraphUniqueKeyDTO graphUniqueKeyDTO = null;

  // To Store derived expression
  private String derivedExp = null;

  private boolean isDerived = false;

  // This is for unique key of graph
  private String uniqueKey = null;

  public String getUniqueKey()
  {
    return uniqueKey;
  }

  public void setUniqueKey(String uniqueKey)
  {
    this.uniqueKey = uniqueKey;
  }

  public PercentileDataKey(int graphType, String dataKey, GraphUniqueKeyDTO graphUniqueKeyDTO, String derivedExp, boolean isDerived, String uniqueKey)
  {
    this.graphType = graphType;
    this.dataKey = dataKey;
    this.uniqueKey = uniqueKey;
    this.graphUniqueKeyDTO = graphUniqueKeyDTO;

    if (derivedExp != null && derivedExp.equals(""))
      this.derivedExp = null;
    else
      this.derivedExp = derivedExp;

    this.isDerived = isDerived;
  }

  public boolean isDerived()
  {
    return isDerived;
  }

  public void setDerived(boolean isDerived)
  {
    this.isDerived = isDerived;
  }

  public String getDataKey()
  {
    return dataKey;
  }

  public void setDataKey(String dataKey)
  {
    this.dataKey = dataKey;
  }

  public GraphUniqueKeyDTO getGraphUniqueKeyDTO()
  {
    return graphUniqueKeyDTO;
  }

  public void setGraphUniqueKeyDTO(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    this.graphUniqueKeyDTO = graphUniqueKeyDTO;
  }

  public String getDerivedExp()
  {
    return derivedExp;
  }

  public void setDerivedExp(String derivedExp)
  {
    if (derivedExp != null && derivedExp.equals(""))
      this.derivedExp = null;
    else
      this.derivedExp = derivedExp;
  }

  public int hashCode()
  {
    return 6;
  }

  public int getGraphType()
  {
    return graphType;
  }

  public void setGraphType(int graphType)
  {
    this.graphType = graphType;
  }

  public boolean equals(Object obj)
  {
    PercentileDataKey percentileDataKey = (PercentileDataKey) obj;

    String derivedExpTemp = percentileDataKey.getDerivedExp();
    GraphUniqueKeyDTO graphUniqueKeyDTOTemp = percentileDataKey.getGraphUniqueKeyDTO();
    boolean isDerivedTemp = percentileDataKey.isDerived();
    boolean isSameGraphType = this.graphType == percentileDataKey.getGraphType();
    
    boolean isSameUniqueKey = false;
    if (uniqueKey == null && percentileDataKey.getUniqueKey() == null)
      isSameUniqueKey = true;
    else if (uniqueKey == null && percentileDataKey.getUniqueKey() != null)
      isSameUniqueKey = false;
    else if (uniqueKey != null && percentileDataKey.getUniqueKey() == null)
      isSameUniqueKey = false;
    else
      isSameUniqueKey = uniqueKey.equals(percentileDataKey.getUniqueKey());

    // If graph is derived
    if (isDerivedTemp)
    {
      if (derivedExpTemp == null && this.derivedExp == null)
        return true;
      else if (derivedExpTemp == null || this.derivedExp == null)
        return false;
      else
        return isSameGraphType && derivedExpTemp.equals(this.derivedExp) && isSameUniqueKey;
    }
    else
    {
      if (graphUniqueKeyDTOTemp == null && this.graphUniqueKeyDTO == null)
        return true;
      else if (graphUniqueKeyDTOTemp == null || this.graphUniqueKeyDTO == null)
        return false;
      else
        return isSameGraphType && graphUniqueKeyDTOTemp.equals(this.graphUniqueKeyDTO) && isSameUniqueKey;
    }
  }

  public String toString()
  {
    return "DataKey(" + dataKey + "), DerivedExp(" + derivedExp + "), graphUniqueKeyDTO(" + graphUniqueKeyDTO + "), uniqueKey(" + uniqueKey + ")";
  }
}

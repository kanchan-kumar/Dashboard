package pac1.Bean.GraphName;

import java.io.Serializable;

public class GraphInfoBean implements Serializable
{
  private static final long serialVersionUID = 1195977804061586211L;

  // Storing Graph Name
  private String graphNameWithoutVector = null;

  // To check graph type is scalar/vector
  private boolean isGraphTypeVector = false;

  /**
   * Storing Graph Formula
   * 
   * SEC -> 0, PM -> 1, PS -> 2, KBPS -> 3 and DBH -> 4
   */
  private int graphFormulaNum = GraphNameUtils.FORMULA_NONE;

  /**
   * Storing Data Type Number.
   * 
   * SAMPLE = 0; RATE = 1; CUMULATIVE = 2; TIMES = 3; TIMES_STD = 4;
   */
  private int dataTypeNum = -1;

  // Storing Graph Description
  private String graphDescription = null;

  // Storing pct data index
  private long pctDataIndex = -1;

  // Storing pdf Id
  private int pdfId = -1;

  public long getPctDataIndex()
  {
    return pctDataIndex;
  }

  public void setPctDataIndex(long pctDataIndex)
  {
    this.pctDataIndex = pctDataIndex;
  }

  public int getPdfId()
  {
    return pdfId;
  }

  public void setPdfId(int pdfId)
  {
    this.pdfId = pdfId;
  }

  public String getGraphDescription()
  {
    return graphDescription;
  }

  public void setGraphDescription(String graphDescription)
  {
    this.graphDescription = graphDescription;
  }

  public int getDataTypeNum()
  {
    return dataTypeNum;
  }

  public void setDataTypeNum(int dataTypeNum)
  {
    this.dataTypeNum = dataTypeNum;
  }

  public boolean isGraphTypeVector()
  {
    return isGraphTypeVector;
  }

  public void setGraphTypeVector(boolean isGraphTypeVector)
  {
    this.isGraphTypeVector = isGraphTypeVector;
  }

  public int getGraphFormulaNum()
  {
    return graphFormulaNum;
  }

  public void setGraphFormulaNum(int graphFormulaNum)
  {
    this.graphFormulaNum = graphFormulaNum;
  }

  public String getGraphName()
  {
    return graphNameWithoutVector;
  }

  public void setGraphName(String graphNameWithoutVector)
  {
    this.graphNameWithoutVector = graphNameWithoutVector;
  }

  public static void main(String[] args)
  {

  }
}

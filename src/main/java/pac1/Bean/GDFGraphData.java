package pac1.Bean;

import java.io.Serializable;
import java.util.ArrayList;

public class GDFGraphData implements Serializable
{
  private static final long serialVersionUID = 2787697398409320178L;
  // Graph Data for each graph
  // Graph|Number of successful requests (Pct)|2|scalar|sample|-|None|0|NA|-1|-1|NA|NA|-
  public String graphName;
  public String graphId;
  public String graphType;
  public String graphDataType;
  public String graphFormula;
  public String graphState;
  public String pdfName;
  public String graphDescription;
  
  public GDFGraphData ()
  {

  }
}
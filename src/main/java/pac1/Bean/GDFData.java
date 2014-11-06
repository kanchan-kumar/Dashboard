package pac1.Bean;

import java.io.Serializable;
import java.util.ArrayList;

public class GDFData implements Serializable
{
  private static final long serialVersionUID = 2787697398409320177L;

  // GDF Group line
  // Group|Actimize Request Stats|10027|vector|12|0|NA|NA|-
  public String gdfName;
  public String groupName;
  public String groupId;
  public String groupType; // Vector or scaler
  public String graphCount;   //Number of graphs in this group
  public String groupDescription;
  public ArrayList groupVectorNames = new ArrayList();
  public ArrayList <GDFGraphData> GDFGraphData = new ArrayList <GDFGraphData>();

  public GDFData()
  {

  }
}

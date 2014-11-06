package pac1.Bean;

import java.io.Serializable;

public class RequestGraphsDTO implements Serializable
{
  private static final long serialVersionUID = 6110074260461517314L;
  private static String className = "RequestGraphsDTO";
  private GraphUniqueKeyDTO[] arrGraphUniqueKeyDTO;

  private String generatorPrefix = null;

  public GraphUniqueKeyDTO[] getGraphUniqueKeyDTO()
  {
    return arrGraphUniqueKeyDTO;
  }

  public void setGraphUniqueKeyDTO(GraphUniqueKeyDTO[] arrGraphUniqueKeyDTO)
  {
    this.arrGraphUniqueKeyDTO = arrGraphUniqueKeyDTO;
  }

  public String getGeneratorPrefix()
  {
    return generatorPrefix;
  }

  public void setGeneratorPrefix(String generatorPrefix)
  {
    this.generatorPrefix = generatorPrefix;
  }

  public String toString()
  {
    return "GeneratorPrefix = " + generatorPrefix;
  }
}

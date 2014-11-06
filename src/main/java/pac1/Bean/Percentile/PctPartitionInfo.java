package pac1.Bean.Percentile;

public class PctPartitionInfo
{
  private String pctMessageFilePath = "";
  private String partitionName = "";
  private String generatorName = null;
  private String generatorTestRunNumber = "NA";

  public PctPartitionInfo(String pctMessageFilePath, String partitionName, String generatorName, String generatorTestRunNumber)
  {
    this.pctMessageFilePath = pctMessageFilePath;
    this.partitionName = partitionName;
    this.generatorName = generatorName;
    this.generatorTestRunNumber = generatorTestRunNumber;
  }

  public String getPctMessageFilePath()
  {
    return pctMessageFilePath;
  }

  public void setPctMessageFilePath(String pctMessageFilePath)
  {
    this.pctMessageFilePath = pctMessageFilePath;
  }

  public String getPartitionName()
  {
    return partitionName;
  }

  public void setPartitionName(String partitionName)
  {
    this.partitionName = partitionName;
  }

  public String getGeneratorName()
  {
    return generatorName;
  }

  public void setGeneratorName(String generatorName)
  {
    this.generatorName = generatorName;
  }

  public String getGeneratorTestRunNumber()
  {
    return generatorTestRunNumber;
  }

  public void setGeneratorTestRunNumber(String generatorTestRunNumber)
  {
    this.generatorTestRunNumber = generatorTestRunNumber;
  }

  public String toString()
  {
    return "pctMessageFilePath = " + pctMessageFilePath + ", generatorName = " + generatorName + ", generatorTestRunNumber = " + generatorTestRunNumber + ", partitionName = " + partitionName;
  }
}

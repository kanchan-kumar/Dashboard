package pac1.Bean;

public class GeneratorUniqueKey
{
  private String className = "GeneratorUniqueKey";
  private String generatorName = null;
  private String generatorTestRunNum = null;
  private String dataKey = null;

  public GeneratorUniqueKey(String dataKey, String generatorName, String generatorTestRunNum)
  {
    this.generatorTestRunNum = generatorTestRunNum;
    this.generatorName = generatorName;
    this.dataKey = dataKey;
  }

  public String getDataKey()
  {
    return dataKey;
  }

  public void setDataKey(String dataKey)
  {
    this.dataKey = dataKey;
  }

  public String getGeneratorName()
  {
    return generatorName;
  }

  public void setGeneratorName(String generatorName)
  {
    this.generatorName = generatorName;
  }

  public String getGeneratorTestRunNum()
  {
    return generatorTestRunNum;
  }

  public void setGeneratorTestRunNum(String generatorTestRunNum)
  {
    this.generatorTestRunNum = generatorTestRunNum;
  }

  /**
   * This method is override for check equality of keys
   */
  public boolean equals(Object obj)
  {
    try
    {
      GeneratorUniqueKey generatorUniqueKey = (GeneratorUniqueKey) obj;

      String oldDataKey = generatorUniqueKey.getDataKey();

      // Getting generator name
      String oldGeneratorName = generatorUniqueKey.getGeneratorName();

      // Getting generator test run number
      String oldGeneratorTestRunNumber = generatorUniqueKey.getGeneratorTestRunNum();

      // Logic for checking same DataKey
      boolean isSameDataKey = false;
      if (oldDataKey == null && this.dataKey == null)
        isSameDataKey = true;
      else if (oldDataKey == null && this.dataKey == null)
        isSameDataKey = false;
      else
        isSameDataKey = oldDataKey.equals(this.dataKey);

      // Logic for checking same generator name
      boolean isSameGenerator = false;
      if (oldGeneratorName == null && this.generatorName == null)
        isSameGenerator = true;
      else if (oldGeneratorName == null || this.generatorName == null)
        isSameGenerator = false;
      else
        isSameGenerator = oldGeneratorName.equals(this.generatorName);

      // Logic for checking same generator test run number
      boolean isSameGeneratorTestRun = false;
      if (oldGeneratorTestRunNumber == null && this.generatorTestRunNum == null)
        isSameGeneratorTestRun = true;
      else if (oldGeneratorTestRunNumber == null || this.generatorTestRunNum == null)
        isSameGeneratorTestRun = false;
      else
        isSameGeneratorTestRun = oldGeneratorTestRunNumber.equals(this.generatorTestRunNum);

      if (isSameDataKey && isSameGenerator && isSameGeneratorTestRun)
        return true;
      else
        return false;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "equals", "", "", "Exception - ", ex);
      return false;
    }
  }

  public int hashCode()
  {
    return 7;
  }

  public String toString()
  {
    return "dataKey(" + dataKey + "), generatorName(" + generatorName + "), generatorTestRunNum(" + generatorTestRunNum + ")";
  }
}

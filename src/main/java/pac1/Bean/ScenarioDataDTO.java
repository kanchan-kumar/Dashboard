package pac1.Bean;

import java.io.Serializable;
import java.util.ArrayList;

public class ScenarioDataDTO implements Serializable
{
  private static final long serialVersionUID = -3989046055039059228L;
  private ArrayList<Integer> testRunNumberList = null;
  private ArrayList<StringBuffer> scenarioData = null;

  public ArrayList<Integer> getTestRunNumberList()
  {
    return testRunNumberList;
  }

  public void setTestRunNumberList(ArrayList<Integer> testRunNumberList)
  {
    this.testRunNumberList = testRunNumberList;
  }

  public ArrayList<StringBuffer> getScenarioData()
  {
    return scenarioData;
  }

  public void setScenarioData(ArrayList<StringBuffer> scenarioData)
  {
    this.scenarioData = scenarioData;
  }
}

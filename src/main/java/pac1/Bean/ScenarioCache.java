package pac1.Bean;

public class ScenarioCache
{
  boolean isCollapsed1; // for Reporting
  boolean isCollapsed2; // for Tracing
  boolean isCollapsed3; // for Logging
  
  // creating setter
  
  public void setCollapsed1(boolean isCollapsed1)
  {
    this.isCollapsed1 = isCollapsed1;
  }
  
  public void setCollapsed2(boolean isCollapsed2)
  {
    this.isCollapsed2 = isCollapsed2;
  }
  
  public void setCollapsed3(boolean isCollapsed3)
  {
    this.isCollapsed3 = isCollapsed3;
  }
  
  // creating getter
  
  public boolean getCollapsed1()
  {
    return this.isCollapsed1;
  }
  
  public boolean getCollapsed2()
  {
    return this.isCollapsed2;
  }
  
  public boolean getCollapsed3()
  {
    return this.isCollapsed3;
  }
}

/**
 * Name   : UpdateDashBoardConfig.java
 * Author : Rohit Jha
 * Purpose: Used for send config info to NS Server
 * 
 * Modification History:
 * 
 * Initial Version --> Rohit Jha (25/09/2014)
 */

package pac1.Bean;

import java.io.Serializable;

public class UpdateDashBoardConfig implements Serializable
{
  private static final long serialVersionUID = 3177839746317945063L;
  private DashboardConfig DashboardConfig = new DashboardConfig();
  private String requestLine = null;

  public DashboardConfig getDashboardConfig()
  {
    return DashboardConfig;
  }

  public void setDashboardConfig(DashboardConfig dashboardConfig)
  {
    DashboardConfig = dashboardConfig;
  }

  public String getRequestLine()
  {
    return requestLine;
  }

  public void setRequestLine(String requestLine)
  {
    this.requestLine = requestLine;
  }
}

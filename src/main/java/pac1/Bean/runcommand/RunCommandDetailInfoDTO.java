/*
 * This class contains info about command detail, details is reading from run_command.dat
 * Group|CommnadName|ActualCommand|Role|ServerType|SearchKeyword|ViewType|Colomn Header|CommanUIArguments|Future1|Future2|Future3|Future4|Future5|Description
 */

package pac1.Bean.runcommand;

import java.io.Serializable;

public class RunCommandDetailInfoDTO implements Serializable
{
  private String groupName;
  private String commandName;
  private String actualCommand;
  private String role;
  private String serverType;
  private String viewType = "Text" ;
  private String description = "NA";
  private String searchKeyword = "NA";
  boolean isColumnContains = false;
  String cmdUIArgs = "NA";
  String actualCmdArgs = "";
  String maxInLineArguments = "4";
  String separator = "Space";
 
  public String getSeparator()
  {
    return separator;
  }

  public void setSeparator(String separator)
  {
    this.separator = separator;
  }

  public String getMaxInLineArguments() 
  {
    return maxInLineArguments;
  }

  public void setMaxInLineArguments(String maxNoOfArgumentsInLine)
  {
    this.maxInLineArguments = maxNoOfArgumentsInLine;
  }

  public String toString()
  {
    return "Group = " + groupName + ", Command name = " + ", Actual command = " + actualCommand + ", User Role = " + role + ", Server Type = " +
           serverType + ", View Type = " + viewType + ", Search Keyword = " + searchKeyword + ", Output Contains Column = " + isColumnContains +
           ", Description = " + description + ",maxLine arguments =" + maxInLineArguments +", Command UI Arguments = " + cmdUIArgs + ", Separator = " 
           + separator;
  }
  
  public void setCmdUIArgs(String cmdUIArgs) 
  {
    this.cmdUIArgs = cmdUIArgs;
  }

  public String getActualCmdArgs() 
  {
    return actualCmdArgs;
  }
  
  public void clearActualCmdArgs()
  {
    this.actualCmdArgs = "";
  }
  
  public void setActualCmdArgs(String actualCmdArgs)
  {
    this.actualCmdArgs = actualCmdArgs;
  }

  public boolean isColumnContains()
  {
    return isColumnContains;
  }

  public String getCmdUIArgs()
  {
    return cmdUIArgs;
  }

  public void setColumnContains(boolean isColumnContains) 
  {
    this.isColumnContains = isColumnContains;
  }

  public String getGroupName() 
  {
    return groupName;
  }
  
  public void setGroupName(String groupName)
  {
    this.groupName = groupName;
  }
  
  public String getCommandName()
  {
    return commandName;
  }
  
  public void setCommandName(String commandName) 
  {
    this.commandName = commandName;
  }
  
  public String getActualCommand()
  {
    return actualCommand;
  }
  
  public void setActualCommand(String actualCommand)
  {
    this.actualCommand = actualCommand;
  }
  
  public String getRole()
  {
    return role;
  }
  
  public void setRole(String role) 
  {
    this.role = role;
  }
  
  public String getServerType() 
  {
    return serverType;
  }
  
  public void setServerType(String serverType)
  {
    this.serverType = serverType;
  }
  
  public String getViewType()
  {
    return viewType;
  }
  
  public void setViewType(String viewType) 
  {
    this.viewType = viewType;
  }
  
  public String getDescription() 
  {
    return description;
  }
  
  public void setDescription(String description)
  {
    this.description = description;
  }
  
  public String getSearchKeyword()
  {
    return searchKeyword;
  }
  
  public void setSearchKeyword(String searchKeyword)
  {
    this.searchKeyword = searchKeyword;
  }
}

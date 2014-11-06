/**----------------------------------------------------------------------------
 * Name       CSVFileDataSettings.java
 * Purpose    To hold the properties for Importing External data files in TR
 * @author    Manish Kumar Gupta
 *---------------------------------------------------------------------------**/
package pac1.Bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class CSVFileDataSettings implements Serializable
{
  private String className = "CSVFileDataSettings";
  ArrayList ListOfImportedFiles = new ArrayList();         // this is used to hold list list of imported files in TR
  ArrayList ListOfFilesWithGraphSelected = new ArrayList();    // this is used to hold graphs selected from each files in TR
  ArrayList ListOfGraphs = new ArrayList();    // this is used to hold all graphs for importing in TR
  String DateFormat = "";    // this is used to hold date format string
  String TimeZone = "";   // this is used to hold time format string
  String importPercent = "";   // this is used to hold percent value of data to import
  int radioOption = -1;   // this is used to hold which import option is selected to import data
  int userDefinedAddOrSubtract = -1;   // this is used to hold whether to add or subtract the user defined time
  String GDFName = "";   // this is used to hold GDF name. if exist or if newly created both.
  String GrpName = "";   // this is used to hold Group name
  String GrpDesc = "";   // this is used to hold group description
  String VectorName = "";   // this is used to hold vector name
  String TRStartTime = "";   // this is used to hold TR start time with date
  String TRCompletionDateTime = "";  // this is used to hold TR end time with date
  Date userDefined = new Date(0L * 0L * 0L * 0L * 0L);   // this is used to hold user defined time in date format
  Date importDuration = new Date(0L * 0L * 0L * 0L * 0L);  // this is used to hold time in date for Import data for specific duration
  String TRElapsedTime = "";  // this is used to hold TR elapsed time i.e HH:MM:SS
  String strUserDefineTime = "";  // this is used to hold user defined time in String format
  String strImportTime = "";  // this is used to hold time in string format for import data for specific time

  public CSVFileDataSettings()
  {
    userDefined.setHours(0);
    userDefined.setMinutes(0);
    userDefined.setSeconds(0);

    importDuration.setHours(0);
    importDuration.setMinutes(0);
    importDuration.setSeconds(0);
  }

  // setter

  public void setTRCompletionDateTime(String TRCompleteDateTime)
  {
    this.TRCompletionDateTime = TRCompleteDateTime;
  }

  public String getTRCompletionDateTime()
  {
    return this.TRCompletionDateTime;
  }

  public String getUserDefinedTimeInString()
  {
    return this.strUserDefineTime;
  }

  public String getImportTimeInString()
  {
    return this.strImportTime;
  }

  public void setImportPercentage(String importdata)
  {
    this.importPercent = importdata;
  }

  public void setImportOption(int option)
  {
    this.radioOption = option;
  }

  public void setUserDefinedTimeZoneAddorUpdate(int userDefinedAddorSubtractOption)
  {
    this.userDefinedAddOrSubtract = userDefinedAddorSubtractOption;
  }

  public void setUserDefinedTime(Date userDefinedTime)
  {
    this.userDefined = userDefinedTime;
  }

  public void setImportDuration(Date duration)
  {
    this.importDuration = duration;
  }

  public void setListOfImportedFiles(ArrayList listFiles)
  {
    this.ListOfImportedFiles = listFiles;
  }

  public void setListOfFilesWithGraphs(ArrayList listFiles)
  {
    this.ListOfFilesWithGraphSelected = listFiles;
  }

  public void setListOfGraphsSelectedToImport(ArrayList listGraphs)
  {
    this.ListOfGraphs = listGraphs;
  }

  public void setUserDefinedTimeInString(String userdefinedTime)
  {
    this.strUserDefineTime = userdefinedTime;
  }

  public void setImportTimeInString(String ImportTime)
  {
    this.strImportTime = ImportTime;
  }

  public void setDateFormat(String dateFormat)
  {
    this.DateFormat = dateFormat;
  }

  public void setTimeZone(String timeZone)
  {
    this.TimeZone = timeZone;
  }

  public void setGroupName(String grpName)
  {
    this.GrpName = grpName;
  }

  public void setTRStartTime(String grpName)
  {
    this.TRStartTime = grpName;
  }

  public void setTRElapsedTime(String elapsedTime)
  {
    this.TRElapsedTime = elapsedTime;
  }

  public void setGDFName(String gdfName)
  {
    this.GDFName = gdfName;
  }

  public void setGroupDesc(String grpDesc)
  {
    this.GrpDesc = grpDesc;
  }

  public void setVectorName(String vecName)
  {
    this.VectorName = vecName;
  }

  // Getter

  public String getTRElapsedTime()
  {
    return this.TRElapsedTime;
  }

  public int getImportOption()
  {
    return this.radioOption;
  }

  public int getUserDefinedTimeZoneAddorUpdate()
  {
    return this.userDefinedAddOrSubtract;
  }

  public ArrayList getListOfImportedFiles()
  {
    return this.ListOfImportedFiles;
  }

  public ArrayList getListOfFilesWithGraphs()
  {
    return this.ListOfFilesWithGraphSelected;
  }

  public ArrayList getListOfGraphsSelectedToImport()
  {
    return this.ListOfGraphs;
  }

  public String getDateFormat()
  {
    return this.DateFormat;
  }

  public String getTimeZone()
  {
    return this.TimeZone;
  }

  public String getGroupName()
  {
    return this.GrpName;
  }

  public String getTRStartTime()
  {
    return this.TRStartTime;
  }

  public String getGDFName()
  {
    return this.GDFName;
  }

  public String getGroupDesc()
  {
    return this.GrpDesc;
  }

  public String getVectorName()
  {
    return this.VectorName;
  }

  public String getImportPercentage()
  {
    return this.importPercent;
  }

  public Date getUserDefinedTime()
  {
    return this.userDefined;
  }

  public Date getImportDuration()
  {
    return this.importDuration;
  }
}

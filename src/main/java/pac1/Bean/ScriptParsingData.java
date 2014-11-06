/**----------------------------------------------------------------------------
 * Name       ScriptParsingData.java
 * Purpose    This file will contain the data for URL while recording 
 *            through scriptRecording and nsu_gen_script.
 *            This will contain parsed and unparsed data. 
 * @author    Atul
 * Modification History
 *
 * 31/8/2010- Atul
 *---------------------------------------------------------------------------**/
package pac1.Bean;
public class ScriptParsingData
{
//This will contain the Unparsed path from the relative directory it will not contain hostName.
  private String unParsedPath;
  //This will contain the parsed path from the relative directory with hostName.
  private String parsedPath;
  private String unParsedFileName;
  private String parsedFileName;
  /*This will contain the file name with full path, contain hostName, path and fileName.
  All will be parsed but with out space, means in encoded form*/
  private String urlForFile;
  
//This is to check that name is paresed or not, means there is need to parse the name or not.
  private boolean isParsed;
  
//This is to check that successfully parsed or not
  private boolean checkParsed;

  public boolean isCheckParsed()
  {
    return checkParsed;
  }

  public void setCheckParsed(boolean checkParsed)
  {
    this.checkParsed = checkParsed;
  }

  public String getUnParsedPath()
  {
    return unParsedPath;
  }

  public String getParsedPath()
  {
    return parsedPath;
  }

  public String getUnParsedFileName()
  {
    return unParsedFileName;
  }

  public String getParsedFileName()
  {
    return parsedFileName;
  }

  public String getUrlForFile()
  {
    return urlForFile;
  }

  public boolean isParsed()
  {
    return isParsed;
  }

  public void setUnParsedPath(String unParsedPath)
  {
    this.unParsedPath = unParsedPath;
  }

  public void setParsedPath(String parsedPath)
  {
    this.parsedPath = parsedPath;
  }

  public void setUnParsedFileName(String unParsedFileName)
  {
    this.unParsedFileName = unParsedFileName;
  }

  public void setParsedFileName(String parsedFileName)
  {
    this.parsedFileName = parsedFileName;
  }

  public void setUrlForFile(String urlForFile)
  {
    this.urlForFile = urlForFile;
  }

  public void setParsed(boolean isParsed)
  {
    this.isParsed = isParsed;
  }
}
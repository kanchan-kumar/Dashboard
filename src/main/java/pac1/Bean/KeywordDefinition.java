/*--------------------------------------------------------------------
  @Name    : KeywordDefinition.java
  @Author  : Atul
  @Purpose : This file is to parse etc/KeywordDefinition.dat file
             and contain hash table for all keywords to point the 
             inner class Keywords.   

  @Modification History: 21/05/09 - Initial version
----------------------------------------------------------------------*/

package pac1.Bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class KeywordDefinition
{
  private String className = "KeywordDefinition";
  private LinkedHashMap hashKeywords = new LinkedHashMap();

  // This will contain all keywords and its value space separated define in the
  // keyword definition file
  private Object arrKeywordsWithDefaultValue[];

  // This will contain only GUI keywords define in the keyword definition file
  private Object arrGUIKeyword[];
  
  // This will contain the string as "order|keyword" in sorted manner, presently used by netstorm
  private Object arrSortedKeyword[];

  public KeywordDefinition()
  {
    String keywordDefinitionFile = Config.getValueWithPath("keywordDefinitionFile");
    if(!loadFileInHashtable(keywordDefinitionFile))
      Log.errorLog(className, "KeywordDefinition", "", "", "Error in loading file in the hashTable");
  }

  public LinkedHashMap getHashForKeywords()
  {
    return this.hashKeywords;
  }

  public Object[] getKeywordsWithDefaultValue()
  {
    return this.arrKeywordsWithDefaultValue;
  }

  public Object[] getGUIKeywords()
  {
    return this.arrGUIKeyword;
  }
  
  public Object[] getArrSortedKeyword()
  {
    return this.arrSortedKeyword;
  }

  public boolean loadFileInHashtable(String nameOfFile)
  {
    Log.debugLog(className, "loadFileInHashtable", "", "", "Method called.nameOfFile = " + nameOfFile);
    try
    {
      File file = new File(nameOfFile.trim());

      if(!file.exists())
      {
        Log.errorLog(className, "loadFileInHashtable", "", "", "File Does not exits. Filename = " + nameOfFile);
        return false;
      }

      FileInputStream fin = new FileInputStream(file);
      BufferedReader in = new BufferedReader(new InputStreamReader(fin));
      String s_line;

      ArrayList listOfKeywordWithValue = new ArrayList();
      ArrayList listOfGUIKeywords = new ArrayList();
      ArrayList listOfSortedKeyword = new ArrayList();
      while((s_line = in.readLine()) != null)
      {
        if((s_line.trim().equals("")) || (s_line.trim().startsWith("#")))
          continue;

        // Keyword|Order|Type|GroupBased|RunTimeChangable|InGUI|key fields|Default Value|Future|Future|Future|Comments
        Keywords keywords = createKeywordObjFromLine(s_line, listOfKeywordWithValue, listOfGUIKeywords, listOfSortedKeyword);
        if(keywords == null)
          return false;
        String key = keywords.getKeyword();
        if(Integer.parseInt(Config.getIntValue("debuglevel")) > 1)
          Log.debugLog(className, "loadFileInHashtable", "", "", "keyword name = " + key);

        if(hashKeywords.containsKey(key))
          Log.errorLog(className, "loadFileInHashtable", "", "", "Duplicate keyword = " + key + ". Updating with last value");

        hashKeywords.put(key, keywords);
      }
      
      arrKeywordsWithDefaultValue = listOfKeywordWithValue.toArray();
      arrGUIKeyword = listOfGUIKeywords.toArray();
      arrSortedKeyword = listOfSortedKeyword.toArray();
      in.close();
      fin.close();
      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "loadFileInHashtable", "", "", "Exception in loadFileInArray()", e);
      return false;
    }
  }

  private Keywords createKeywordObjFromLine(String line, ArrayList listOfAllKeywordWithValue, ArrayList guiKeywords, ArrayList listOfSortedKeywords)
  {
    Log.debugLog(className, "createKeywordObjFromLine", "", "", "Method called.line = " + line);
    Keywords keyword = new Keywords();
    String arrTemp[] = rptUtilsBean.split(line, "|");

    if(arrTemp.length < 12)
    {
      Log.errorLog(className, "createKeywordObjFromLine", "", "", "corrupt keyword definition file. fields for line = " + line + " is not 12");
      return null;
    }
    String strKeywordWithDefaultValue = arrTemp[0].trim();
    String strForSortedList = "";

    // keyword name
    keyword.setKeyword(arrTemp[0].trim());

    // order
    keyword.setOrder(arrTemp[1].trim());
    strForSortedList = arrTemp[1].trim() + "|" + arrTemp[0].trim();

    // Type
    if(arrTemp[2].trim().equals("Scalar"))
      keyword.setType(Keywords.SCALAR);
    else if(arrTemp[2].trim().equals("Vector"))
      keyword.setType(Keywords.VECTOR);
    else
    {
      Log.errorLog(className, "createKeywordObjFromLine", "", "", "Unknown value (" + arrTemp[2] + ") for type field found for key = " + arrTemp[0]);
      return null;
    }

    // Is group based
    if(arrTemp[3].trim().equals("Yes"))
      keyword.setIsGroupBased(true);
    else if(arrTemp[3].trim().equals("No"))
      keyword.setIsGroupBased(false);
    else
    {
      Log.errorLog(className, "createKeywordObjFromLine", "", "", "Unknown value (" + arrTemp[3] + ") for group based field found for key = " + arrTemp[0]);
      return null;
    }

    // Is run time changeable
    if(arrTemp[4].trim().equals("Yes"))
      keyword.setIsRunTimeChangeable(true);
    else if((arrTemp[4].trim().equals("No")) || (arrTemp[4].trim().equals("RunTimeChangable")))//Temporary check for 'RunTimeChangable'
      keyword.setIsRunTimeChangeable(false);
    else 
    {
      Log.errorLog(className, "createKeywordObjFromLine", "", "", "Unknown value (" + arrTemp[4] + ") for is run time changeable field found for key = " + arrTemp[0]);
      return null;
    }

    // Is in GUI
    if(arrTemp[5].trim().equals("Yes"))
    {
      keyword.setIsInGUI(true);
      guiKeywords.add(arrTemp[0].trim());
    }
    else if(arrTemp[5].trim().equals("No"))
      keyword.setIsInGUI(false);
    else
    {
      Log.errorLog(className, "createKeywordObjFromLine", "", "", "Unknown value (" + arrTemp[5] + ") for is inGUI field found for key = " + arrTemp[0]);
      return null;
    }

    // Key fields
    keyword.setKeyFields(arrTemp[6].trim());

    // Default value
    keyword.setDefaultValue(arrTemp[7].trim());
    strKeywordWithDefaultValue = strKeywordWithDefaultValue + " " + arrTemp[7].trim();

    // Future1
    keyword.setFuture1(arrTemp[8].trim());

    // Future2
    keyword.setFuture2(arrTemp[9].trim());

    // Future3
    keyword.setFuture3(arrTemp[10].trim());

    // Comments
    keyword.setComments(arrTemp[11].trim());

    //Check for Default value, it should not be empty OR '-'
    if((!arrTemp[7].trim().equals("")) && (!arrTemp[7].trim().equals("-")))
      listOfAllKeywordWithValue.add(strKeywordWithDefaultValue);
    listOfSortedKeywords.add(strForSortedList);

    return keyword;
  }

  class Keywords
  {
    // Keyword|Order|Type|GroupBased|RunTimeChangable|InGUI|key fields|Default
    // Value|Future|Future|Future|Comments
    public static final int SCALAR = 0;
    public static final int VECTOR = 1;

    private String keyword;
    private String order;
    private int type;
    private boolean isGroupBased;
    private boolean isRunTimeChangeable;
    private boolean isInGUI;
    private String keyFields;
    private String defaultValue;
    private String future1;
    private String future2;
    private String future3;
    private String comments;

    /** ***** Getters ********* */

    public String getKeyword()
    {
      return this.keyword;
    }

    public String getOrder()
    {
      return this.order;
    }

    public int getType()
    {
      return this.type;
    }

    public boolean getIsGroupBased()
    {
      return this.isGroupBased;
    }

    public boolean getIsRunTimeChangeable()
    {
      return this.isRunTimeChangeable;
    }

    public boolean getIsInGUI()
    {
      return this.isInGUI;
    }

    public String getKeyFields()
    {
      return this.keyFields;
    }

    public String getDefaultValue()
    {
      return this.defaultValue;
    }

    public String getFuture1()
    {
      return this.future1;
    }

    public String getFuture2()
    {
      return this.future2;
    }

    public String getFuture3()
    {
      return this.future3;
    }

    public String getComments()
    {
      return this.comments;
    }

    /** ******** Setters *********** */

    public void setKeyword(String keyword)
    {
      this.keyword = keyword;
    }

    public void setOrder(String order)
    {
      this.order = order;
    }

    public void setType(int type)
    {
      this.type = type;
    }

    public void setIsGroupBased(boolean isGroupBased)
    {
      this.isGroupBased = isGroupBased;
    }

    public void setIsRunTimeChangeable(boolean isRunTimeChangeable)
    {
      this.isRunTimeChangeable = isRunTimeChangeable;
    }

    public void setIsInGUI(boolean isInGUI)
    {
      this.isInGUI = isInGUI;
    }

    public void setKeyFields(String keyFields)
    {
      this.keyFields = keyFields;
    }

    public void setDefaultValue(String defaultValue)
    {
      this.defaultValue = defaultValue;
    }

    public void setFuture1(String future1)
    {
      this.future1 = future1;
    }

    public void setFuture2(String future2)
    {
      this.future2 = future2;
    }

    public void setFuture3(String future3)
    {
      this.future3 = future3;
    }

    public void setComments(String comments)
    {
      this.comments = comments;
    }
  }
}

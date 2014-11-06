/*--------------------------------------------------------------------
  @Name    : MergeSortScen.java
  @Author  : Atul
  
  @Purpose : This file is to Merge scenario file , vendor default file, 
             site default file(if there) and KeywordDefinition file.
             and then sort all the keyword as per order field given in
             KeywordDefinition file.
             
             Merging of keyword is optional.In present time this file 
             is used only by netstorm.
             
             Note : If option is given "Sort" then it will NOT merge keyword from 
             vendor default file, site default file and keywordDefinition file.
             
  @Example : java MergeSortScen <Merge/MergeAndSort> <scenario file name with path> 
             <sorted file name with path>            
                 

  @Modification History: 27/05/09 - Initial version
----------------------------------------------------------------------*/
package pac1.Bean;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Properties;


public class MergeSortScen
{
  private String className = "MergeSortScen";
  
  private KeywordDefinition keywordDefinition;
  private FileBean fileBean;
  private Properties p;
  
  private boolean shouldMerge = false;
  private String destScenFile;
  private String srcScenFile;
  
  public MergeSortScen(String userName)
  {
    /*
     * Here we have to set log files prefix
     * because when debug OR error file is created with 
     * root then it show error permisson denied 
     * 
     * so creating the file as user name prefix
     */
    
    Config.logFilePrefix = userName;
    keywordDefinition = new KeywordDefinition();
    fileBean = new FileBean(keywordDefinition);
    p = new Properties();
  }
  
  protected void usage()
  {
    System.err.println("Error: Usage - " + className + " [Option - MergeAndSort/Sort] [Source Scenario file name with path] [Destination scenario file name with path]");
    System.exit(-1);
  }
  
  protected void errMsg(String errMsg)
  {
    System.err.println("Error in parsing scenario file " + this.srcScenFile +" due to following errors:");
    System.err.print(errMsg);
    
    System.exit(-1);
  }
  
  protected boolean isValidOption(String option)
  {
    Log.debugLog(className, "isValidOption", "", "", "Method Called. option = " + option);
    if((!option.equals("MergeAndSort")) && (!option.equals("Sort")))
      return false;
    else
      return true;
  }
  
  protected boolean setValueToProperty(String option, String srcScenFile, String destiScenFile)
  {
    Log.debugLog(className, "setValueToProperty", "", "", "Method Called. option = " + option + ", srcScenFile = " + srcScenFile + ", destiScenFile = " + destiScenFile);
    try
    {
      this.destScenFile = destiScenFile;
      this.srcScenFile = srcScenFile;
      if(!isValidOption(option))
        usage();
      else
      {
        if(option.equals("MergeAndSort"))
          shouldMerge = true;
      }
      StringBuffer errMsg = new StringBuffer();
  //    if(!fileBean.getKeyValues(p, srcScenFile, null, shouldMerge, errMsg))
    //   errMsg(errMsg.toString()); 
 //this function always return true, so we are checking through length of error message 
      fileBean.getKeyValues(p, srcScenFile, null, shouldMerge, errMsg);
      if(errMsg.length() > 1)
        errMsg(errMsg.toString());

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "setValueToProperty", "", "", "Exception in setValueToProperty()", e);
      
      System.err.println("Error in setting key to property due to following exception : \n");
      e.printStackTrace();
      System.exit(-1);
      
      return false;
    }
  }
  
  private Object[] sortKeywords(Object[] arrToSortKeywordAsPerOrder)
  {
    try
    {
      if(arrToSortKeywordAsPerOrder == null)
        return null;
      
      Arrays.sort(arrToSortKeywordAsPerOrder, new ToCompare());
      
      return arrToSortKeywordAsPerOrder;
    }
    catch(Exception e) 
    {
      Log.stackTraceLog(className, "sortKeywords", "", "", "Exception in sortKeywords()", e);
      
      System.err.println("Error in sorting keywords due to following exception : \n");
      e.printStackTrace();
      System.exit(-1);

      return null;
    }
  }
  
  protected boolean saveToTempFile()
  {
    int debugLevel = Integer.parseInt(Config.getValue("debuglevel"));
    
    if(debugLevel > 1)
      Log.debugLog(className, "saveToTempFile", "", "", "Method Called.");
    try
    {
      FileOutputStream fout = new FileOutputStream(destScenFile);
      PrintStream pw = new PrintStream(fout);
      
      //This will contain the string as "order|keyword" format
      Object[] arrSortedKeywords = sortKeywords(keywordDefinition.getArrSortedKeyword());
      
      if(arrSortedKeywords == null)
      {
        Log.errorLog(className, "saveToTempFile", "", "", "String array of sorted keyword is comming null");
        return false;
      }
      
      for (int i = 0 ; i < arrSortedKeywords.length ; i++)
      {
        String strArrOrderWithValue[] = rptUtilsBean.split((String)arrSortedKeywords[i], "|");
        
        String order = strArrOrderWithValue[0];
        String keywordName = strArrOrderWithValue[1];
        
        
        if(debugLevel > 1)
          Log.debugLog(className, "saveToTempFile", "", "", "keywordName = " + keywordName);
          
        /*
         keywordValue will have '|' separated values IF more than one values
         like for G_KA_PCT keyword  - 'ALL 70|G1 60|G2 50'
         */
        
        String keywordValue = p.getProperty(keywordName);
        
        if(keywordValue == null)
        {
          Log.debugLog(className, "saveToTempFile", "", "", "Keyword = " + keywordName + " not found in the property.");
          continue;
        }
        
        String strArrKeyValues[] = rptUtilsBean.split(keywordValue, "|");
        
        for(int ii = 0 ; ii < strArrKeyValues.length ; ii++)
        {
          String strToWrite = keywordName + " " + strArrKeyValues[ii];
          pw.println(strToWrite);
        }
        
        //Remove the key from the property object
        p.remove(keywordName);
      }
      
      Enumeration e = p.propertyNames();
      
      while(e.hasMoreElements())
        Log.errorLog(className, "saveToTempFile", "", "", "Keyword = " + e.nextElement() + " not found in the KeywordDefinition file");
      
      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "saveToTempFile", "", "", "Exception in sortAndSave()", e);
      
      System.err.println("Error in saving file due to following exception : \n");
      e.printStackTrace();
      System.exit(-1);
      
      return false;
    }
  }
  
  public static void main(String []args)
  {
    
    if(args.length != 4)
    {
      System.err.println("Error: Usage - MergeSortScen [Option - MergeAndSort/Sort] [Source Scenario file name with path] [Destination scenario file name with path]");
      System.exit(-1);
    }
    
    MergeSortScen mergeSortScen = new MergeSortScen(args[3].trim());
    mergeSortScen.setValueToProperty(args[0].trim(), args[1].trim(), args[2].trim());
    
    mergeSortScen.saveToTempFile();
    
    System.exit(0);
  }
}

class ToCompare implements Comparator
{
  public int compare(Object first, Object second)
  {
    String strFirst[] = rptUtilsBean.split(first.toString(), "|");
    int firstData = Integer.parseInt(strFirst[0]);
    
    String strSecond[] = rptUtilsBean.split(second.toString(), "|");
    int secondData = Integer.parseInt(strSecond[0]);
    
    if(firstData < secondData)
      return -1;
    else if(firstData == secondData)
      return 0;
    else
      return 1;
  }
}

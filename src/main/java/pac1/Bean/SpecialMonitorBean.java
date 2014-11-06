/*--------------------------------------------------------------------
@Name    : SpecialMonitorBean.java
@Author  : Prabhat
@Purpose : Provided the utility function(s) to custom GDF GUI(jsp)
           - monType : FileData, LogParser
@Modification History:
    09/08/2008 -> Prabhat (Initial Version)

----------------------------------------------------------------------*/
package pac1.Bean;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;
import java.net.URLEncoder;

public class SpecialMonitorBean
{
  private static String className = "SpecialMonitorBean";
  private static String workPath = Config.getWorkPath();
  GDFBean gdfBean = new GDFBean();
  MonitorProfile monitorProfile = new MonitorProfile();
  FileBean FileBean = new FileBean();
  public static final String FILE_DATA_MON = "FileData";
  public static final String LOG_PARSER_MON = "LogParser";

  boolean bolResult = true;
  String strGDFName = "";
  String strGroupDesc = "";

  //constructor
  public SpecialMonitorBean(){ }

  // this method create format for '-F' option as P:"<pattern>*<pattern>",N:<fieldNum>
  private String[] createPatternAsPerFileType(String[] arrSearchPattern, String[] arrFieldType)
  {
	String[] fieldPatterns = null;

	if(arrFieldType.length == arrSearchPattern.length)
	{
      fieldPatterns = new String[arrFieldType.length];
      int k = 0;
      for(String fieldType : arrFieldType)
      {
        if(fieldType.equals("1")) // for Pattern
        {
          String pattern = "P:\"" + URLEncoder.encode(arrSearchPattern[k]) + "\"";
          Log.debugLog(className, "createPatternAsPerFileType", "", "", k + " pattern is: " + pattern);
          fieldPatterns[k++] = pattern;
        }
        else if(fieldType.equals("2")) //For Field number & Pattern
        {
          String[] arrFieldPattern = arrSearchPattern[k].split("%%");
          String pattern = "NP:" + arrFieldPattern[0] + ":\"" + URLEncoder.encode(arrFieldPattern[1]) + "\"";
          Log.debugLog(className, "createPatternAsPerFileType", "", "", k + " Field Num and pattern is: " + pattern);
          fieldPatterns[k++] = pattern;
        }
        else if(fieldType.equals("0")) // for Field Number
        {
          String fieldNum = "N:" + arrSearchPattern[k];
          Log.debugLog(className, "createPatternAsPerFileType", "", "", k + " Field Num is: " + fieldNum);
          fieldPatterns[k++] = fieldNum;
        }

      }//end of for loop
    }//end of if
    else
      Log.errorLog(className, "createPatternAsPerFileType", "", "", "Error: Field Type, '" + arrFieldType.length + "', are not equal to FieldNum/Patterns, '" + arrSearchPattern.length + "' length.");

    return fieldPatterns;
  }

  public boolean addSearchPattForLogParser(String strGroupName, String[] arrSearchPattern, String[] arrGraphName, String[] arrGraphDataType, String strMPROFName, String strMPROFDesc, String monitorKeywordLine, String monType, String argType, String[] arrFieldType, String strMetricsName)
  {
    return addSearchPattForLogParser(strGroupName, arrSearchPattern, arrGraphName, arrGraphDataType, strMPROFName, strMPROFDesc, monitorKeywordLine, monType, argType, arrFieldType, null, strMetricsName);
  }

  public boolean addSearchPattForLogParser(String strGroupName, String[] arrSearchPattern, String[] arrGraphName, String[] arrGraphDataType, String strMPROFName, String strMPROFDesc, String monitorKeywordLine, String monType, String argType, String[] arrFieldType, String realGdfName, String strMetricsName)
  {
    if(realGdfName != null)
      Log.debugLog(className, "addSearchPattForLogParser", "", "", "Start method. Group Name = " + strGroupName + ", strMPROFName = " + strMPROFName + ", strMPROFDesc = " + strMPROFDesc  + ", realGdfName: " + realGdfName + ", monitorKeywordLine = " + monitorKeywordLine);
    else
      Log.debugLog(className, "addSearchPattForLogParser", "", "", "Start method. Group Name = " + strGroupName + ", strMPROFName = " + strMPROFName + ", strMPROFDesc = " + strMPROFDesc + ", monitorKeywordLine = " + monitorKeywordLine);

    try
    {
      String SearchPattern = "";
      String strDataType = "";
      String startGDF = "";
      if(monType.equals("FileData"))
      {
        startGDF = "cm_file_";
        // this method create format for '-F' option as P:"<pattern>*<pattern>",N:<fieldNum>
        String[] patterns = createPatternAsPerFileType(arrSearchPattern, arrFieldType);

        if(patterns != null)
        {
          for(int k = 0; k < patterns.length; k++)
          {
            if(k == 0)
              SearchPattern = patterns[k];
            else
              SearchPattern = SearchPattern + "," + patterns[k];
          }

          //this is put in double quotes as to consider -F arguments as one token.
          SearchPattern = " -F \"" + SearchPattern + "\"";
        }
      }
      else
      {
        if(monType.equals("LogParser"))
          startGDF = "cm_log_parser_";

        for(int k = 0; k < arrSearchPattern.length; k++)
        {
         /**encode pattern.
          * The alphanumeric characters "a" through "z", "A" through "Z" and "0" through "9" remain the same.
          * The special characters ".", "-", "*", and "_" remain the same.
          * The plus sign "+" is converted into a space character " " .
          * A sequence of the form "%xy" will be treated as representing a byte where xy is the two-digit hexadecimal representation of the 8 bits. Then, all substrings that contain one or more of these byte sequences consecutively will be replaced by the character(s) whose encoding would result in those consecutive bytes. The encoding scheme used to decode these characters may be specified, or if unspecified, the default encoding of the platform will be used.
          **/

          int startIndex = -1;
          int endIndex = -1;

          if(((startIndex = arrSearchPattern[k].indexOf("\"")) != -1) && ((endIndex = arrSearchPattern[k].lastIndexOf("\"")) != -1) && startIndex != endIndex)
            arrSearchPattern[k] = arrSearchPattern[k].substring(1, arrSearchPattern[k].length() - 1);


          //if(arrSearchPattern[k].startsWith("\"") && arrSearchPattern[k].endsWith("\""))
          //  arrSearchPattern[k] = arrSearchPattern[k].substring(1, arrSearchPattern[k].length() - 1);

		  Log.debugLog(className, "addSearchPattForLogParser", "", ""," k = " + k + "SearchPattern" + SearchPattern + " " + argType + " " + arrSearchPattern[k]);

          String encodedPattern = URLEncoder.encode(arrSearchPattern[k]);

		  Log.debugLog(className, "addSearchPattForLogParser", "", ""," k = " + k + "SearchPattern" + SearchPattern + " " + argType + " " + encodedPattern);

         SearchPattern = SearchPattern + " " + argType + " \"" + encodedPattern + "\"";

        }
      }

      Log.debugLog(className, "addSearchPattForLogParser", "", "","SearchPattern" + SearchPattern);

      bolResult = monitorProfile.addMonitorToMPROF(strMPROFName, monitorKeywordLine + SearchPattern);
      if(!bolResult) { return false; }

      bolResult = monitorProfile.saveMPROF(strMPROFName, strMPROFDesc);
      if(!bolResult) { return false; }

      if(!monType.equals("EventData"))
      {
        //Start to create GDF
        //strGDFName = startGDF + strGroupName.replaceAll(" ", "_");
        //Earlier all spl characters are removed from the name, but know they are all replaced by '_', underscore so as to decrease the probability of file over writing having same Group name but consist of spl charcters.
        //strGDFName = startGDF + strGroupName.replaceAll("[^a-zA-Z0-9]+","");

        //this is to not to change GDF name but only Group Name is change in GDF, if Group Name is changed from GUI.
        if(realGdfName != null && !realGdfName.equals("undefined"))
          strGDFName = startGDF + realGdfName.replaceAll("[^a-zA-Z0-9]+", "_");
        else
          strGDFName = startGDF + strGroupName.replaceAll("[^a-zA-Z0-9]+", "_");

        strGroupDesc = strGroupName;

        gdfBean.deleteGDF(strGDFName);
        // Create new GDF (this is created as bak file)
        bolResult = gdfBean.createNewGDF(strGDFName, strGroupName, strGroupDesc, false, true, strMetricsName);
        if(!bolResult)
        {
          Log.debugLog(className, "addSearchPattForLogParser", "", "", "GDF bak file not created successfully = " + strGDFName);
          //return false;
        }

        //jyoti12|scalar|sample|-|A|None|rr
        //jyoti134|scalar|cumulative|-|A|None|rr
        String arrformulaType [] = null;
        if(monType.equals("LogParser"))
        {
         if(!monitorKeywordLine.equals(""))
         {
            boolean formulaKeyword = false;
            String arrTemp[] =  rptUtilsBean.split(monitorKeywordLine, " ");
            for(int kk = 0 ; kk < arrTemp.length ; kk++)
            {
              String value = arrTemp[kk].trim();
              if(value.equals("-C"))
              {
                formulaKeyword = true;
                kk++;
              }
              
              if(formulaKeyword && !arrTemp[kk].trim().equals("") )
              {
                arrformulaType =  rptUtilsBean.split(arrTemp[kk].trim(), ",");
                break;
              }
            }
         }
        }
        
        for(int i = 0; i < arrGraphName.length; i++)
        {
          String graphLine = "";
          String GraphName = "";
          //for sample

          if(monType.equals("LogParser"))
          {
            //Now "Current " is written in GDF instead of "Current_".
            GraphName =  arrGraphName[i].trim();
            if(arrformulaType!= null)
            {
              if(arrformulaType[i].equals("PerMin"))
                GraphName =  GraphName + "/Min";
              else 
                GraphName =  GraphName + "/Sec";
            }
            //GraphName = "Current_" + arrGraphName[i].trim();
            strDataType = "rate";
            
            if(arrformulaType[i].equals("PerMin"))
              graphLine = "" + GraphName + "|" + "scalar"  + "|" + strDataType + "|-|A|None|Number of " + arrGraphName[i] + " per minute";
            else
              graphLine = "" + GraphName + "|" + "scalar"  + "|" + strDataType + "|-|A|None|Number of " + arrGraphName[i] + " per second";
          }
          else
          {
            GraphName = arrGraphName[i];
            strDataType = arrGraphDataType[i].trim();
            graphLine = "" + GraphName + "|" + "scalar"  + "|" + strDataType + "|-|A|None|" + arrGraphName[i];
          }

          
          Log.debugLog(className, "addSearchPattForLogParser", "", "", "GDF Input Line for Sample = " + graphLine);
          bolResult = gdfBean.addGraphToGDF(strGDFName, graphLine, false, false, "-1");

          if(monType.equals("LogParser"))
          {
            //Now "Total " is written in GDF instead of "Total_".
            GraphName = "Total " + arrGraphName[i].trim();
            //GraphName = "Total_" + arrGraphName[i].trim();
            //For cumulative
            graphLine = "" + GraphName + "|" + "scalar"  + "|" + "cumulative" + "|-|A|None|Total number of " + arrGraphName[i] + " since start of the monitoring";
            Log.debugLog(className, "addSearchPattForLogParser", "", "", "GDF Input Line for cumulative = " + graphLine);
            bolResult = gdfBean.addGraphToGDF(strGDFName, graphLine, false, false, "-1");
          }
        }

        bolResult = gdfBean.saveGDF(strGDFName, strGroupName, strGroupDesc, strMetricsName);
        if(!bolResult)
        {
          Log.debugLog(className, "addSearchPattForLogParser", "", "", "GDF not created successfully = " + strGDFName);
          //return false;
        }
      }
      //End created GDF

      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "addSearchPattForLogParser", "", "", "Exception - ", ex);
      return false;
    }
  }

  public String[][] getSpecialMonData(String strMPROFName, int monIndex, String strGDFName, String monType, String argsType, String argsDataType)
  {
    Log.debugLog(className, "getSpecialMonData", "", "", "Start method. strMPROFName = " + strMPROFName + ", monIndex = " + monIndex + ", strGDFName = " + strGDFName + "argsType = " + argsType);

    try
    {
      String[][] arrRecordFlds = gdfBean.getGDFGraphDetails(strGDFName);
      if(arrRecordFlds != null)
      {
        String[] arrSearchPattern = new String[arrRecordFlds.length - 1];
        String[] arrMonDataType = new String[arrRecordFlds.length - 1];
        String[] arrFormulaTypeValue = new String[arrRecordFlds.length - 1];

        String[][] arrDataValues = monitorProfile.getMPROFDetails(strMPROFName);
        /** Earlier LOG_MONITOR is used as SPECIAL_MONITOR
         *  but to handle old mprof following condition is there
         *  As it is to be remove after all old mprof is replaced to LOG_MONITOR.
         */
        if(arrDataValues[monIndex][0].equals("SPECIAL_MONITOR") || arrDataValues[monIndex][0].equals("LOG_MONITOR"))
        {
          String strName = "";

          for(int j = 4; j < arrDataValues[monIndex].length; j++) // Concat all words in name
            strName = strName + " " + arrDataValues[monIndex][j];

          //Here we are replacing back slash(\) and single code(') to \\ and \' and passing to js variable because for js \ equal to \\ and ' to \'.
          strName = FileBean.replace(strName, "\\", "\\\\");
          strName = FileBean.replace(strName, "\'", "\\'");

          ArrayList arrFileOption = new ArrayList();

          arrFileOption = monitorProfile.getCommandTokensList(strName);

          int k = 0;
          int kk = 0;
          int formulaIndex = 0;
          for(int j = 0; j < arrFileOption.size(); j++)
          {
            if(arrFileOption.get(j).equals(argsType))
            {
              arrSearchPattern[k] = arrFileOption.get(j + 1).toString();
              k++;
            }
            else if (arrFileOption.get(j).equals(argsDataType))
            {
              arrMonDataType[kk] = arrFileOption.get(j + 1).toString();
              kk++;
            }
            else if (arrFileOption.get(j).equals("-C"))
            {
              arrFormulaTypeValue[formulaIndex] = arrFileOption.get(j + 1).toString();
              formulaIndex++;
            }
          }
        }
        String[] arrTemp = null;
        String[] arrTempMonType = null;
        String[] arrTempFormulaTypeValue = null;
        String[][] arrSearchPattGroupName = null;

        if(monType.equals("FileData"))
        {
          arrTemp = rptUtilsBean.split(arrSearchPattern[0], ",");
          if(arrMonDataType[0] != null)
            arrTempMonType = rptUtilsBean.split(arrMonDataType[0], ",");
          if(arrFormulaTypeValue[0] != null)
            arrTempFormulaTypeValue = rptUtilsBean.split(arrFormulaTypeValue[0], ",");
          arrSearchPattGroupName = new String[arrTemp.length][6];
        }
        else
          arrSearchPattGroupName = new String[arrSearchPattern.length][2];

        // this is to handle GUI, if arguments for Log monitors is uneven
        int patternLength = -1;
        if(arrTemp != null && (arrRecordFlds.length - 1) != arrTemp.length)
          patternLength = arrTemp.length;
        else
          patternLength = arrRecordFlds.length - 1;

        Log.debugLog(className, "getSpecialMonData", "", "", "patternLength: " + patternLength);

        for(int i = 0; i < patternLength; i++)
        //for(int i = 0; i < arrRecordFlds.length - 1; i++)
        {
          if(monType.equals("FileData"))
          {
            arrSearchPattGroupName[i][0] = arrTemp[i];
            arrSearchPattGroupName[i][2] = arrRecordFlds[i + 1][3];
            if(arrTempMonType.length > i)
              arrSearchPattGroupName[i][3] = arrTempMonType[i];
            else
              arrSearchPattGroupName[i][3] = getGraphToMonDataType(arrRecordFlds[i + 1][3].trim());

            if(arrTempFormulaTypeValue == null)
            {
              arrSearchPattGroupName[i][4] = "NA";
              arrSearchPattGroupName[i][5] = "";
            }
            else if(arrTempFormulaTypeValue.length > i)
            {
              if(arrTempFormulaTypeValue[i] != null)
              {
                String[] arrFV = arrTempFormulaTypeValue[i].split("_");
                if(arrFV.length > 1)
                {
                  arrSearchPattGroupName[i][4] = arrFV[0];
                  arrSearchPattGroupName[i][5] = arrFV[1];
                }
                else
                {
                  arrSearchPattGroupName[i][4] = arrFV[0];
                  arrSearchPattGroupName[i][5] = "";
                }
              }
              else
              {
                arrSearchPattGroupName[i][4] = "NA";
                arrSearchPattGroupName[i][5] = "";
              }
            }
          }
          else
            arrSearchPattGroupName[i][0] = arrSearchPattern[i];
          arrSearchPattGroupName[i][1] = arrRecordFlds[i + 1][0];
        }
        return arrSearchPattGroupName;
      }
      else
        return new String[0][0];
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "addSearchPattForLogParser", "", "", "Exception - ", ex);
      return new String[0][0];
    }
  }

  public String getGraphToMonDataType(String graphType)
  {
    String monType = "0";
    if(graphType.equals("sample"))
      monType = "0";
   else if(graphType.equals("rate"))
      monType = "1";
   else if(graphType.equals("cumulative"))
      monType = "2";
   else if(graphType.equals("times"))
      monType = "3";
   else if(graphType.equals("timesStd"))
      monType = "4";

    return monType;
  }
  public String[][] getGDFOfSpecialMon(String monType)
  {
    ArrayList arrGDFList = new ArrayList();
    String arrGDFName[][] = null;
    try
    {
      String serachGDF = "";
      if(monType.equals(FILE_DATA_MON))
        serachGDF = "cm_file";
      else
        serachGDF = "cm_log_parser";

      String arrTemp[][] = gdfBean.getGDFs();
      for(int i = 1; i < arrTemp.length; i++)
      {
        if(arrTemp[i][0] == null)
          continue;
        if(arrTemp[i][0].trim().startsWith(serachGDF))
        //if(arrTemp[i][0].trim().startsWith("Log_Parser"))
        {
          String arrGDFDetail[][] = gdfBean.getGDFGraphDetails(arrTemp[i][0]);
          for(int k = 1; k < arrGDFDetail.length; k++)
          {
            String strTemp  = "";
            for(int kk = 0; kk < arrGDFDetail[k].length; kk++)
              strTemp = strTemp + "|" + arrGDFDetail[k][kk];

            Log.debugLog(className, "getGDFOfSpecialMon", "", "", "data = " + arrTemp[i][0] + "|" + arrGDFDetail[0][0] + "|" + arrGDFDetail[0][1] + "|" + strTemp);
             arrGDFList.add(arrTemp[i][0] + "|" + arrGDFDetail[0][0] + "|" + arrGDFDetail[0][1] + strTemp);
          }
        }
      }
      arrGDFName = new String[arrGDFList.size()][(rptUtilsBean.strToArrayData(arrGDFList.get(0).toString().trim(), "|").length)];
      for(int i = 0; i < arrGDFList.size(); i++)
      {
        String arrTempData[] = rptUtilsBean.strToArrayData(arrGDFList.get(i).toString().trim(), "|");
        for(int k = 0; k < arrTempData.length; k++)
          arrGDFName[i][k] = arrTempData[k];
      }

      return arrGDFName;
    }
    catch (Exception e)
    {
      return arrGDFName;
      // TODO: handle exception
    }
  }

  public static void main(String[] args)
  {
    SpecialMonitorBean specialMonitorBean = new SpecialMonitorBean();
    boolean resultFlag;

    int choice = 0;

    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    System.out.println("********Please enter the option for desired operation*********");
    System.out.println("Add Special mon : 1");
    System.out.println("Get Special mon : 2");
    System.out.println("Get Group name with Graph details : 3");
    System.out.println("*************************************************************");
    try
    {
      choice = Integer.parseInt(br.readLine());
    }
    catch(IOException e)
    {
      System.out.println("Error in entered choice: " + e);
    }

    switch(choice)
    {
      case 1:
        String[] arrSearchPattern = new String[1];
        String[] arrGraphDataType = new String[1];
        arrSearchPattern[0] = "1,2,3";
        arrGraphDataType[0] = "rate";
        String monitorKeywordLine = "SPECIAL_MONITOR NS logprser vecname";
        // Give the list of all gdf(s)
        //specialMonitorBean.addSearchPattForLogParser("specialMon", arrSearchPattern, arrSearchPattern, arrGraphDataType, "vector", "test Special Mon", monitorKeywordLine, "LogParser", "-F");
        break;

      case 2:
        //String[][] arrTemp = specialMonitorBean.getSpecialMonData("LogParser", 1, "FileData", "FileData", "-F", "-t");
        String[][] arrTemp = specialMonitorBean.getSpecialMonData("fileDta", 1, "aaa1", "FileData", "-F", "-t");

        for(int i = 0; i < arrTemp.length; i++)
        {
          for(int j = 0; j < arrTemp[i].length; j++)
            System.out.println(arrTemp[i][j]);
        }
        break;
      case 3:
         String arrTempData[][] = specialMonitorBean.getGDFOfSpecialMon("FileData");
         for(int i = 0; i < arrTempData.length; i++)
         {
           for(int ii = 0; ii < arrTempData[i].length; ii++)
             System.out.println(arrTempData[i][ii]);
           System.out.println("--------------\n");
         }
        break;

      default:
        System.out.println("Please select the correct option.");
    }
  }
}

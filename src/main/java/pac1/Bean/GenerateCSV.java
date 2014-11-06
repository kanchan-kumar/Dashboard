/*********************************************************************************************************
 @ Name    : GenerateCSV.java

 @ Author  :
 @ Purpose : This file generates report in the form of csv, called by different JSP and Java applet code.
 @ Modification History:
**********************************************************************************************************/
package pac1.Bean;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class GenerateCSV
{
  static String className = "CSVDoc";
  static int counter = 1;
  static BufferedWriter out = null;
  static FileWriter  fout = null;

  public static String generateCSVDoc(String strTitle, String strSessionId, String strSrcFileName, String[] para1, String chartImgPath, String[][] rowColumns, int column, String strSkipIndex, String[] para2, String chartImgPathForSecond, String[][] rowColumnsForSecond, int columnForSecond , String strSkipIndex2, String logoPath)
  {
    Log.debugLog(className, "generateCvsDoc", "", "", "Method Called");
    String strFileName = "";

    try
    {
      //SessionId is used to give an unique name to every csv file, when this method is called by JSP pages, but via java code we give independence to the user to give name of their choice, hence we don't need to pass SessionId, in that case.
      if(strSessionId.equals(""))    // This code section is used by applet java code.
      {
        strFileName = strSrcFileName;
        fout = new FileWriter(strFileName);
      }
      else                           // This code section is used by JSP code.
      {
        strFileName = strSessionId + strSrcFileName + counter + ".csv";
        fout = new FileWriter(Config.getValueWithPath("tempFilePath") + "/" + strFileName);
        counter++;
      }

      out = new BufferedWriter(fout);

      if(rowColumns!= null)
        addData(rowColumns , strSkipIndex);

      if(rowColumnsForSecond != null)
      {
    	out.write("\n\n");
        addData(rowColumnsForSecond , strSkipIndex2);
      }
      out.close();
    }

    catch (Exception e)
    {
      Log.stackTraceLog(className, "generateCSVDoc", "", "", "Exception", e);
    }
    return strFileName;
  }

  private static void addData(String[][] rowColumns , String strSkipIndex) throws IOException
  {
    Log.debugLog(className, "addData", "", "", "Method Called");
    String arrSkip[] = null;
    int k = 0;
    arrSkip = rptUtilsBean.split(strSkipIndex, "|");

    if(arrSkip.length == 0)
    {
      arrSkip = new String[1];
      arrSkip[0] = "-1";
    }

    for(int j = 0; j < rowColumns.length; j++)
    {
      for(int jj = 0; jj < rowColumns[j].length; jj++)
      {
        //checking the condition that if user want to skip some columns then skip them while writing in a file
	if(jj != Integer.parseInt(arrSkip[k]))
	{
    rowColumns[j][jj] = rptUtilsBean.convertCommaSeparatorToNumber(rowColumns[j][jj]);	  
          out.write(" " + rowColumns[j][jj]);
          if(jj < rowColumns[j].length - 1)
            out.write(",");
	}

        else
	{
          k++;
          if(k > arrSkip.length - 1)
          k = 0;
        }
      }
      out.write("\n");
    }
  }

  public static void main(String[] args) throws IOException
  {
    int i , j;
    String[][] arrData = new String[10][6];
    for(i = 0; i < 10; i++)
    {
      for(j = 0; j < 6; j++)
      {
        if(j == 3)
          arrData[i][j] = "skip";
        else if(j == 4)
	  arrData[i][j] = "richa";
	else if(j == 5)
	  arrData[i][j] = "shastri";
        else
	  arrData[i][j] = "xyz";
       }
     }
     
     String[][] arrData1 = new String[10][6];
     for(i = 0; i < 10; i++)
     {
       for(j = 0; j < 6; j++)
         arrData1[i][j] = "abc";
     }
     String[] para1 = {"This", "is", "a", "test."};
     String csvFile = GenerateCSV.generateCSVDoc("Local Testing of rtf generation", "", "/home/netstorm/Chitra/MyLocalRTF.csv", para1, "xyz.png", arrData, 6, "3|4", para1, "xyz.png", arrData1, 6, "2|3", "");
     System.out.println("GenerateCSV.java class is called");
  }
}

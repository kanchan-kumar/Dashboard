package pac1.Bean.runcommand;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Vector;

import pac1.Bean.Log;
import pac1.Bean.rptUtilsBean;

public class RunCommandDataParser
{
  private static String className = "RunCommandDataParser";
  
  public ArrayList getCustomPSTabularData(Vector vecData, String separator , boolean isContainsColumn)
  {
    Log.debugLog(className, "getCustomPSTabularData", "", "", "method called");
    String data [][] = null;
    ArrayList list = null;
    try
    {
      list = new ArrayList();
      String [] arrTempHeader = rptUtilsBean.split( vecData.get(0).toString(), separator) ;
          
      
      Log.debugLog(className, "getCustomPSTabularData", "", "", "Data contains " + vecData.size() + " rows, " + arrTempHeader.length + " columns.");
      
      int dataRow = 1;
      if(!isContainsColumn)
      {
        data = new String[vecData.size()][arrTempHeader.length];

        for(int i = 0 ; i < arrTempHeader.length ; i++)
          data[0][i] = arrTempHeader[i];
      }
      else
      {
        list.add(arrTempHeader);
        dataRow = 0;
        data = new String[vecData.size() - 1][arrTempHeader.length];
      }
       
      for(int i = 1 ; i < vecData.size() ; i++ )
      {
        arrTempHeader = rptUtilsBean.split( vecData.get(i).toString(), separator) ;
        
        for(int j = 0 ; j < arrTempHeader.length ; j++)
        {
          if((j + 1 ) == data[0].length )
          {
            StringBuffer strBuff = new StringBuffer();
            
            for(int k = j ; k < arrTempHeader.length ; k++)
              strBuff.append(arrTempHeader[k]).append(" ");
            
            data[dataRow][j] = strBuff.toString().trim();
            break;
          }
          else
          {
            data[dataRow][j] = arrTempHeader[j];
          }
        }
        
         dataRow++;
      }
      
      list.add(data);
      return list;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "getCustomPSTabularData", "", "", "Exception caught in parsing ps data - " + e.getMessage());
      return null;
    }
  }
  
  public Vector readFile(String filePath)
  {
    try
    {
      Vector vecData = new Vector();
      FileInputStream  fis = new FileInputStream(filePath);
      BufferedReader  br = new BufferedReader(new InputStreamReader(fis));
      String strLine = "";
      while((strLine = br.readLine()) != null)
      {
        vecData.add(strLine);
      }
      
      br.close();
      fis.close();
      
      return vecData;
    }
    catch(Exception e)
    {
      return null;
    }
  }
  
  public static void main(String args[])
  {
    RunCommandDataParser parser = new RunCommandDataParser();
    ArrayList list = parser.getCustomPSTabularData(parser.readFile("C:\\Users\\compass-56\\Desktop\\ps_output.txt") , " " , false);
    
    String data [][]= (String [][])list.get(1);
    
    if(data != null)
    {
      for(int i = 0 ; i < data.length ; i++)
      {
        for(int j= 0 ; j < data[0].length ; j++)
        {
          System.out.print(data[i][j] + "  *********************  ");
        }
        System.out.println();
      }
    }
  }
}

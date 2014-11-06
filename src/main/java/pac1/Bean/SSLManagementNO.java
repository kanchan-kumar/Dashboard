/*--------------------------------------------------------------------
@Name    : SSLManagementNO.java
@Author  : Jyoti
@Purpose : This is class to maintain the SSL certificate information. Add, Delete SSL certificate
@Modification History:
  04/16/12:Jyoti - Initial Version
----------------------------------------------------------------------*/

package pac1.Bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

public class SSLManagementNO
{
  private static String className = "SSLManagementNO";
  static String controllerPath = Config.getWorkPath();
  static String hpdPath = null;
  String userName = "";
  
  CorrelationService correlationService = null;
  
  //Constructor
  public SSLManagementNO(String userName)
  {
    this.userName = "netstorm";
    correlationService = new CorrelationService();
  }
  
  //getting hpd path
  public String getHPDPath()
  {
    hpdPath = correlationService.getHPDPath();
    return hpdPath;
  }

  //getting controller path
  public String getControllerPath()
  {
    controllerPath = Config.getWorkPath();
    return controllerPath;
  }

  /*
   * This function will show the available information of the certificate 
   * Default search keyword is blank
   * 
   * "server.pem|PEM|CA|Server|-|Cavisson Systems, Inc.|-|California|US|-|Aug  9 14:27:43 2019 GMT|1024"
   * "root.pem|PEM|Self|Root|-|Cavisson Systems, Inc.|-|California|US|-|Aug  9 14:27:43 2019 GMT|-"
   */
  public String[][] showAvailableCertInfo(String filterKeyword)
  {
    Log.debugLog(className, "showAvailableCertInfo", "", "", "Method called.");
    
    try
    {
      String arrResult[][] = null;
      String currDate = "";
      
      //getting current date of the system
      String strCmd = "";
      String strArg = "";
      
      CmdExec cmdExec = new CmdExec();
      /**Vector vecCmdOutput = cmdExec.getResultByCommand(strCmd, strArg, CmdExec.SYSTEM_CMD, userName, null);
      if(vecCmdOutput != null && vecCmdOutput.size() > 0)
        currDate = vecCmdOutput.get(0).toString();**/

      Date curDateObj = new Date();
      currDate = curDateObj.toGMTString();
      
      strCmd = "nsu_show_cert";
      strArg = "";
    
      Vector vecCmdOutput = cmdExec.getResultByCommand(strCmd, strArg, CmdExec.NETSTORM_CMD, userName, null);
    
      //If output is null
      if(vecCmdOutput == null)
      {
        //vector is null
        return null;
      }

      if((vecCmdOutput.size() > 0) && ((String)vecCmdOutput.lastElement()).startsWith("ERROR")) //Error Case
      {
        //If there is error in error in execute command
        return null;
      }
      
      vecCmdOutput.remove(0);//remove header line
      arrResult = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "-");
      String arrTemp[][] = new String[arrResult.length][14];
      for(int i = 0; i < arrResult.length; i++)
      {
        for(int k = 0; k < arrResult[i].length; k++)
        {
          arrTemp[i][k] = arrResult[i][k];
          arrTemp[i][12] = "Key";
          if(!arrResult[i][1].equals("-") && !arrResult[i][11].equals("-"))
            arrTemp[i][12] = "Certificate/Key";
          else if(!arrResult[i][1].equals("-") && arrResult[i][11].equals("-"))
            arrTemp[i][12] = "Certificate"; 
          arrTemp[i][13] = "" + getExpiraryDate(arrResult[i][10], currDate);
        }
      }

      arrResult = getFilterData(arrTemp, filterKeyword, "0|3|5|9|10|11|12");
      
      return arrResult;
    }
    catch (Exception e) 
    {
      Log.stackTraceLog(className, "showAvailableCertInfo", "", "", "Exception - ", e);
      return null;
    }
  }

  public String[][] getFilterData(String arrData[][], String filterKeyword, String SelectedIndex)
  {
    String[][] arrResultData = null;
    String[] indexField = rptUtilsBean.split(SelectedIndex, "|");
    int colLength = 0;
    
    try
    {
      if(arrData == null)
        return arrData;
      else if(arrData.length < 1)
        return arrData;
      else if(filterKeyword.trim().equals(""))
        return arrData;
      
      Vector filterResult = new Vector();
      for(int i = 0; i < arrData.length; i++)
      {
        boolean searchSuccess = false;
        colLength = indexField.length;
        for(int ii = 0; ii < colLength; ii++)
        {
          if(indexField[ii].trim().equals(""))
            continue;
          if(arrData[i].length < Integer.parseInt(indexField[ii]))
            continue;
          
          if(!searchSuccess)
          {
            if(arrData[i][ii].toLowerCase().contains(filterKeyword.toLowerCase()))
              searchSuccess = true;
          }
        }
        if(searchSuccess)
          filterResult.add(arrData[i]);
      } 
      
      arrResultData = new String[filterResult.size()][colLength];
      for(int shi=0; shi < filterResult.size(); shi++)
      {
        arrResultData[shi] = (String[])filterResult.get(shi);
      } 
      
      return arrResultData;
    }
    catch (Exception e) 
    {
      Log.stackTraceLog(className, "getFilterData", "", "", "Exception - ", e);
      return null;
    }
  }
  
  public boolean getExpiraryDate(String strDate, String strCurrentDate)
  {
    Log.debugLog(className, "getExpiraryDate", "", "", "Method called. strDate = " + strDate + ", strCurrentDate = " + strCurrentDate);
    
    try
    {
      if((strDate.trim().equals("")) || (strDate.trim().equals("-")))
        return false;
      if((strCurrentDate.trim().equals("")) || (strCurrentDate.trim().equals("-")))
        return false;     
      
      Date date = new Date(strDate);
      Date currDate = new Date(strCurrentDate);
      
      Date dateGMT = new Date(date.toGMTString());
      Date currDateGMT = new Date(currDate.toGMTString());      
      
      long longDate = dateGMT.UTC(dateGMT.getYear(), dateGMT.getMonth(), dateGMT.getDate(), dateGMT.getHours(), dateGMT.getMinutes(), dateGMT.getSeconds());
      long longCurrDate = currDateGMT.UTC(currDateGMT.getYear(), currDateGMT.getMonth(), currDateGMT.getDate(), currDateGMT.getHours(), currDateGMT.getMinutes(), currDateGMT.getSeconds());
      if(longCurrDate > longDate)
        return true;
      else
        return false;
    }
    catch (Exception e) 
    {
      Log.stackTraceLog(className, "getExpiraryDate", "", "", "Exception - ", e);
      return false;
    }
  }
  
  /*
   * this function add certificate in the cert file
   * strArgs arguments = inputLine -f jyoti -I 0 -K 2048 -subj /CN=common/DIV=division/O=org/L=locality/ST=state/C=country/emailAddress=/days=33
   * return array object 
   *    0- true or false
   *    1- msg given by shell
   */
  public Object[] createSSLCertificate(String strArgs, String fileName)
  {
    Log.debugLog(className, "createSSLCertificate", "", "", "Method called.");
    
    Object[] objResult = new Object[2];
    StringBuffer strBuff = new StringBuffer();
    try
    {
      mkdirWithOutRoot(getControllerPath() + "/" + "cert/");
      File fileObj = new File(getControllerPath() + "/" + "cert/" + fileName);
      
      if(fileObj.exists())
      {
        objResult[0] = false;
        objResult[1] = fileName + " Certificate already exists";
        
        return objResult;               
      }
      
      String strCmd = "nsu_gen_cert";
      String strArg = strArgs;
      
      CmdExec cmdExec = new CmdExec();
      Vector vecCmdOutput = cmdExec.getResultByCommand(strCmd, strArg, CmdExec.NETSTORM_CMD, userName, null);
      
      //If output is null
      if(vecCmdOutput == null)
      {
        objResult[0] = false;
        objResult[1] = "Error in adding SSL certificate";
        
        return objResult;
      }

      int vecLen = 0;
      if((vecCmdOutput.size() > 0) && ((String)vecCmdOutput.lastElement()).startsWith("ERROR")) //Error Case
      {
        vecLen= vecCmdOutput.size() - 1;
        objResult[0] = false;
      }
      else
      {
        vecLen = vecCmdOutput.size(); //Success Case
        objResult[0] = true;
      }
      
      for(int i = 0; i < vecLen; i++)
      {
        String line = rptUtilsBean.replaceSpecialCharacter(vecCmdOutput.elementAt(i).toString());
        strBuff.append(line + "\\n");
      }

      objResult = addCertificate(fileName, "PEM", "");
      String strTemp = (String)objResult[1]; 
      strBuff.append("\\n" + strTemp);
      
      objResult[1] = strBuff.toString();  
      
      return objResult;
    }
    catch (Exception e) 
    {
      Log.stackTraceLog(className, "createSSLCertificate", "", "", "Exception - ", e);
      objResult[0] = false;
      objResult[1] = "Error in adding SSL certificate";           
      return objResult;
    }
    
  }

  /*
   * Delete the certificate on the basis of fileName
   * return array object 
   *    0- true or false
   *    1- msg given by shell   * 
   */
  public Object[] deleteSSLCertificate(String fileName)
  {
    Log.debugLog(className, "deleteSSLCertificate", "", "", "Method called.");
    Object[] objResult = new Object[2];
    StringBuffer strBuff = new StringBuffer();
    
    try
    {
      String strCmd = "nsu_del_cert";
      String strArg = fileName;
      
      CmdExec cmdExec = new CmdExec();
      Vector vecCmdOutput = cmdExec.getResultByCommand(strCmd, strArg, CmdExec.NETSTORM_CMD, userName, "root");
      
      //If output is null
      if(vecCmdOutput == null)
      {
        objResult[0] = false;
        objResult[1] = "Error in deleting SSL certificate";
        
        return objResult;
      }

      int vecLen = 0;
      if((vecCmdOutput.size() > 0) && ((String)vecCmdOutput.lastElement()).startsWith("ERROR")) //Error Case
      {
        vecLen= vecCmdOutput.size() - 1;
        objResult[0] = false;
      }
      else
      {
        vecLen = vecCmdOutput.size(); //Success Case
        objResult[0] = true;
      }
      
      for(int i = 0; i < vecLen; i++)
      {
        String line = rptUtilsBean.replaceSpecialCharacter(vecCmdOutput.elementAt(i).toString());
        strBuff.append(line + "\\n");
      }
        
      objResult[1] = strBuff.toString();   
      return objResult;
      
    }
    catch (Exception e) 
    {
      Log.stackTraceLog(className, "deleteSSLCertificate", "", "", "Exception - ", e);
      objResult[0] = false;
      objResult[1] = "Error in deleting SSL certificate";      
      return objResult;
    }
  }
  
  public Object[] importSSLCertificate(String fileName, String fileType, String strPwdPhrase, String importFrom, String textareaContents, boolean bolOverwrite)
  {
    Log.debugLog(className, "importSSLCertificate", "", "", "Method called.");
    Object[] objResult = new Object[2];
    StringBuffer strBuff = new StringBuffer();
    
    try
    {
      fileType = fileType.toUpperCase();
      
      mkdirWithOutRoot(getControllerPath() + "/" + "cert/");
      
      File fileObj = new File(getControllerPath() + "/" + "cert/" + fileName);
      
      
      if(fileObj.exists())
      {
        if(!bolOverwrite)
        {
          objResult[0] = false;
          objResult[1] = fileName + " Certificate already exists";
          
          return objResult;          
        }
        //fileObj.delete();
        deleteSSLCertificate(fileName);
      }
      
      if(importFrom.equals("0"))
      {
        String strCmd = "cp";
        String strArg = getControllerPath() + "/cert/.cert/" + fileName + " " + getControllerPath() + "/cert/";
        
        CmdExec cmdExec = new CmdExec();
        Vector vecCmdOutput = cmdExec.getResultByCommand(strCmd, strArg, CmdExec.SYSTEM_CMD, userName, null);        
      }
      else if(importFrom.equals("1"))
      {
        String strCmd = "nsu_import_file";
        String strArg = textareaContents;
        
        CmdExec cmdExec = new CmdExec();
        Vector vecCmdOutput = cmdExec.getResultByCommand(strCmd, strArg, CmdExec.NETSTORM_CMD, userName, null);   
        if(vecCmdOutput == null)
        {
          objResult[0] = false;
          objResult[1] = "Error in importing remotely SSL certificate";
          
          return objResult;
        }

        int vecLen = 0;
        if((vecCmdOutput.size() > 0) && ((String)vecCmdOutput.lastElement()).startsWith("ERROR")) //Error Case
        {
          vecLen= vecCmdOutput.size() - 1;
          for(int i = 0; i < vecLen; i++)
          {
            String line = rptUtilsBean.replaceSpecialCharacter(vecCmdOutput.elementAt(i).toString());
            strBuff.append(line + "\\n");
          }
          
          objResult[0] = false;
          objResult[1] = strBuff.toString();
          return objResult;
        }        
      }
      else
      {
        fileObj.createNewFile();
        FileOutputStream out2 = new FileOutputStream(fileObj);
        PrintStream certFile = new PrintStream(out2);
        certFile.println(textareaContents);
        certFile.close();
        out2.close();
      }
      
      objResult = addCertificate(fileName, fileType, strPwdPhrase);
      return objResult;   
    }
    catch (Exception e) 
    {
      Log.stackTraceLog(className, "importSSLCertificate", "", "", "Exception - ", e);
      objResult[0] = false;
      objResult[1] = "Error in importing SSL certificate";      
      return objResult;
    }
  }
  
  public Object[] addCertificate(String fileName, String fileType, String strPwdPhrase)
  {
    Object[] objResult = new Object[2];
    StringBuffer strBuff = new StringBuffer();
    
    if(!fileType.trim().toLowerCase().equals("pfx"))
    {
      strPwdPhrase = "";
    }
    
    String strCmd = "nsu_add_cert";
    String strArg = fileName + " " + fileType + strPwdPhrase;
    
    CmdExec cmdExec = new CmdExec();
    Vector vecCmdOutput = cmdExec.getResultByCommand(strCmd, strArg, CmdExec.NETSTORM_CMD, userName, null);
    
    //If output is null
    if(vecCmdOutput == null)
    {
      objResult[0] = false;
      objResult[1] = "Error in importing SSL certificate";
      
      return objResult;
    }

    int vecLen = 0;
    if((vecCmdOutput.size() > 0) && ((String)vecCmdOutput.lastElement()).startsWith("ERROR")) //Error Case
    {
      vecLen= vecCmdOutput.size() - 1;
      objResult[0] = false;
    }
    else
    {
      vecLen = vecCmdOutput.size(); //Success Case
      objResult[0] = true;
    }
    
    for(int i = 0; i < vecLen; i++)
    {
      String line = rptUtilsBean.replaceSpecialCharacter(vecCmdOutput.elementAt(i).toString());
      strBuff.append(line + "\\n");
    }
      
    objResult[1] = strBuff.toString();   
    return objResult; 
  }
  
  public String[][] showAvailableCertRevocationInfo(String filterKeyword)
  {
    String arrResult[][] = null;
    try
    {
      String crlPath = getControllerPath() + "/cert/crl";
      File fileObj = new File(crlPath);
      
      //If file does not exist it will return null
      if(!fileObj.exists())
        return null;
      
      File[] fileList = fileObj.listFiles();
      ArrayList arrList = new ArrayList();
      ArrayList arrListIssuer = new ArrayList();
      
      for(int i = 0; i < fileList.length; i++)
      {
        if(fileList[i].isFile())
        {
          arrList.add(fileList[i].getName());
          arrListIssuer.add("-");
        }
      }
      
      arrResult = new String[arrList.size()][2];
      for(int i = 0; i < arrList.size(); i++)
      {
        arrResult[i][0] = arrList.get(i).toString();
        arrResult[i][1] = arrListIssuer.get(i).toString();
      }
      
      arrResult = getFilterData(arrResult, filterKeyword, "1");
      return arrResult;
    }
    catch (Exception e) 
    {
      Log.stackTraceLog(className, "showAvailableCertRevocationInfo", "", "", "Exception - ", e);
      return arrResult;
    }
  }
  
  public Object[] deleteCertRevocation(String fileName)
  {
    Log.debugLog(className, "deleteCertRevocation", "", "", "Method called.");
    Object[] objResult = new Object[2];
    StringBuffer strBuff = new StringBuffer();
    
    try
    {
      String strCmd = "rm";
      String strArg = " -f " + getControllerPath() + "/cert/crl/" + fileName;
      
      CmdExec cmdExec = new CmdExec();
      Vector vecCmdOutput = cmdExec.getResultByCommand(strCmd, strArg, CmdExec.SYSTEM_CMD, userName, "root");
      
      //If output is null
      if(vecCmdOutput == null)
      {
        objResult[0] = false;
        objResult[1] = "Error in deleting SSL revocation certificate";
        
        return objResult;
      }

      int vecLen = 0;
      if((vecCmdOutput.size() > 0) && ((String)vecCmdOutput.lastElement()).startsWith("ERROR")) //Error Case
      {
        vecLen= vecCmdOutput.size() - 1;
        objResult[0] = false;
      }
      else
      {
        vecLen = vecCmdOutput.size(); //Success Case
        objResult[0] = true;
      }
      
      for(int i = 0; i < vecLen; i++)
      {
        String line = rptUtilsBean.replaceSpecialCharacter(vecCmdOutput.elementAt(i).toString());
        strBuff.append(line + "\\n");
      }
        
      objResult[1] = strBuff.toString();   
      return objResult;
      
    }
    catch (Exception e) 
    {
      Log.stackTraceLog(className, "deleteCertRevocation", "", "", "Exception - ", e);
      objResult[0] = false;
      objResult[1] = "Error in deleting SSL revocation certificate";      
      return objResult;
    }   
  }
  
  public void mkdirWithOutRoot(String dirName)
  {
    File fileObj = new File(dirName);
    fileObj.mkdir();
    
    String strCmd = "chown";
    String strArg = "netstorm.netstorm -R " + dirName;
    
    CmdExec cmdExec = new CmdExec();
    Vector vecCmdOutput = cmdExec.getResultByCommand(strCmd, strArg, CmdExec.SYSTEM_CMD, userName, "root");       
  }
  
  public Object[] importSSLRevocationCertificate(String fileName, String importFrom, boolean bolOverwrite)
  {
    Log.debugLog(className, "importSSLRevocationCertificate", "", "", "Method called." + bolOverwrite);
    Object[] objResult = new Object[2];
    StringBuffer strBuff = new StringBuffer();
    
    try
    {
      mkdirWithOutRoot(getControllerPath() + "/" + "cert/crl/");
      
      File fileObj = new File(getControllerPath() + "/" + "cert/crl/" + fileName);
      
      if(fileObj.exists())
      {
        if(!bolOverwrite)
        {
          objResult[0] = false;
          objResult[1] = fileName + " Revocation Certificate already exists";
          
          return objResult;          
        }
        deleteCertRevocation(fileName);
      }
      
      Vector vecCmdOutput = null;
      
      if(importFrom.equals("0"))
      {
        String strCmd = "cp";
        String strArg = getControllerPath() + "/cert/.crl/" + fileName + " " + getControllerPath() + "/cert/crl";
        
        CmdExec cmdExec = new CmdExec();
        vecCmdOutput = cmdExec.getResultByCommand(strCmd, strArg, CmdExec.SYSTEM_CMD, userName, null);        
      }
      //If output is null
      if(vecCmdOutput == null)
      {
        objResult[0] = false;
        objResult[1] = "Error in importing SSL revocation certificate";
        
        return objResult;
      }

      int vecLen = 0;
      if((vecCmdOutput.size() > 0) && ((String)vecCmdOutput.lastElement()).startsWith("ERROR")) //Error Case
      {
        vecLen= vecCmdOutput.size() - 1;
        objResult[0] = false;
      }
      else
      {
        vecLen = vecCmdOutput.size(); //Success Case
        objResult[0] = true;
      }
      
      for(int i = 0; i < vecLen; i++)
      {
        String line = rptUtilsBean.replaceSpecialCharacter(vecCmdOutput.elementAt(i).toString());
        strBuff.append(line + "\\n");
      }
        
      objResult[1] = strBuff.toString();   
      return objResult;   
    }
    catch (Exception e) 
    {
      Log.stackTraceLog(className, "importSSLRevocationCertificate", "", "", "Exception - ", e);
      objResult[0] = false;
      objResult[1] = "Error in importing SSL revocation certificate";      
      return objResult;
    }
  }  
  
  public static void main(String args[])
  {
    int choice = 0;
    SSLManagementNO SSLManagementNOObj = new SSLManagementNO("userName");

    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    System.out.println("********Please enter the option for desired operation*********");
    System.out.println("Show Available SSL certificate info. Enter : 1");
    System.out.println("Show Available Revocation info. Enter : 3");
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
        String arrControllerTemp[][] = SSLManagementNOObj.showAvailableCertInfo(""); 
        
        for(int i = 0; i < arrControllerTemp.length; i++)
        {
          for(int ii = 0; ii < arrControllerTemp[i].length; ii++)
            System.out.print(arrControllerTemp[i][ii] + "| " );
          System.out.println("\n");
        }
        System.out.println("\n");
        System.out.println("\n");
        arrControllerTemp = SSLManagementNOObj.showAvailableCertInfo("Self"); 
        
        for(int i = 0; i < arrControllerTemp.length; i++)
        {
          for(int ii = 0; ii < arrControllerTemp[i].length; ii++)
            System.out.print(arrControllerTemp[i][ii] + "| " );
          System.out.println("\n");
        }        
        break; 
        
      case 2:
        String hh = "" + SSLManagementNOObj.getExpiraryDate("Feb 21 00:55:42 2003 GMT", "Fri Feb 19 04:57:21 EDT 2003");
        System.out.println(hh);
        hh = "" + SSLManagementNOObj.getExpiraryDate("Fri Apr 20 04:57:21 EDT 2012", "Aug 9 14:27:43 2019 GMT");
        System.out.println(hh);        
        break; 
        
      case 3:
        String[][] revocationList = SSLManagementNOObj.showAvailableCertRevocationInfo("");
        
        if(revocationList != null)
        for(int i = 0; i < revocationList.length; i++)
        {
          for(int ii = 0; ii < revocationList[i].length; ii++)
            System.out.print(revocationList[i][ii] + "| " );
          System.out.println("\n");
        }  
        break;        
        
      default:
        System.out.println("Please select the correct option.");
    }
  }
}

/*--------------------------------------------------------------------
  @Name      : ADFBean.java
  @Author    : Jyoti
  @Purpose   : Provided the utility function(s) to alert definition file GUI(jsp)
  @Modification History:
      08/05/2010 -> Jyoti (Initial Version)

----------------------------------------------------------------------*/
package pac1.Bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

public class ADFBean
{
  private static String className = "ADFBean";
  private static String workPath = Config.getWorkPath();
  private final static String ADF_FILE_EXTN = ".adf";
  private final static String ADF_BAK_EXTN = ".hot"; // This is used to save the adf for backup.
  private transient final String LAST_MODIFIED_DATE = "LAST_MODIFIED_DATE";

  //constructor
  public ADFBean(){ }

  // This will return the sys path
  private static String getADFPath()
  {
    return (workPath + "/adf/");
  }

  //This returns full path of with .adf extension
  private static String getADFNameWithEXTN(String fileName)
  {
    return (getADFPath() + fileName + ADF_FILE_EXTN);
  }
  // This will return the bak adf name
  private String getBAKADFName(String adfName)
  {
    return (getADFPath() + "." + adfName + ADF_FILE_EXTN + ADF_BAK_EXTN);
  }

  private static String getTestRunDIR(String testNum)
  {
    return (workPath + "/webapps/logs/TR" + testNum);
  }

  public static String getTestRunADFNameWithEXTN()
  {
    return (workPath + "/adf/netstorm.adf");
  }

  //Open file and returns File object
  private static File openFile(String fileName)
  {
    try
    {
       File tempFile = new File(fileName);
       return(tempFile);
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "openFile", "", "", "Exception - ", e);
      return null;
    }
  }

/**
 * @method name readFile
 * @pupose Read file in vector
 * @param adfFileNameWithPath
 * @return vector
 */
  private static Vector readFile(String adfFileNameWithPath)
  {
    Log.debugLog(className, "readFile", "", "", "Method called.Alert Definition File Name = " + adfFileNameWithPath);

    try
    {
      Vector vecFileData = new Vector();
      String strLine;

      File adfFile = openFile(adfFileNameWithPath);

      if(!adfFile.exists())
      {
        Log.errorLog(className, "readFile", "", "", "Alert Definition File not found, filename - " + adfFileNameWithPath);
        return null;
      }

      FileInputStream fis = new FileInputStream(adfFile);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));

      while((strLine = br.readLine()) !=  null)
      {
        strLine = strLine.trim();
        //if(strLine.startsWith("#"))
        // continue;
        if(strLine.length() == 0)
          continue;

        Log.debugLog(className, "readFile", "", "", "Adding line in vector. Line = " + strLine);
        vecFileData.add(strLine);
      }

      br.close();
      fis.close();

      return vecFileData;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "readFile", "", "", "Exception - ", e);
      return null;
    }
  }

 /////////////////////////////////////Available ADF /////////////////////////////////////

/**
 * @method name loadAllADF
 * @pupose getting list of all .adf File
 * @return ArrayList
 */
  public ArrayList loadAllADF()
  {
    Log.debugLog(className, "loadAllADF", "", "", "Method called");

    ArrayList arrListAllADF = new ArrayList();  // Names of all adf files
    ArrayList arrListAllADFSortedOrder = new ArrayList();  // Names of all ADF files in sorted order

    File fileName = new File(getADFPath());

    if(!fileName.exists())
    {
      fileName.mkdirs();
      //return null;
    }

    try
    {
      String arrAvailFiles[] = fileName.list();

      for(int j = 0; j < arrAvailFiles.length; j++)
      {
        String[] tempArr = rptUtilsBean.strToArrayData(arrAvailFiles[j], "."); // for remove profile file extension
        if(tempArr.length == 2)
        {
          if(arrAvailFiles[j].lastIndexOf(ADF_FILE_EXTN) != -1)  // Skip non .adf files
          {
            String tmpStr = arrAvailFiles[j].substring(0, arrAvailFiles[j].lastIndexOf(ADF_FILE_EXTN));
            arrListAllADF.add(tmpStr);
            Log.debugLog(className, "loadAllADF", "", "", "Adding '.adf' file name in ArrayList = " + tmpStr);
          }
        }
        else
        {
          Log.debugLog(className, "loadAllADF", "", "", "Skiping file name  = " + arrAvailFiles[j]);
        }
      }

      Object[] arrTemp = arrListAllADF.toArray();
      Arrays.sort(arrTemp);

      for(int i = 0; i < arrTemp.length; i++)
        arrListAllADFSortedOrder.add(arrTemp[i].toString());

      return arrListAllADFSortedOrder;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "loadAllADF", "", "", "Exception - ", e);
      return arrListAllADFSortedOrder;
    }
  }

/**
 * @method name getADFDescription
 * @pupose getting ADF description and ADF last modification date & time of paticular adf
 * @param adfName
 * @return String
 */
  private String getADFDescription(String adfName)
  {
    Log.debugLog(className, "getADFDescription", "", "", "Method called. File Name = " + adfName);
    Properties tempADFProp = new Properties();
    String descLastMd = "NA%%%NA";
    try
    {
      String tempADFWithPath = getADFNameWithEXTN(adfName);
      File tempADFFile = new File(tempADFWithPath);
      FileInputStream fis = new FileInputStream(tempADFFile);
      tempADFProp.load(fis);

      String value1 = tempADFProp.getProperty("ADF_DESC");
      if(value1 == null)
      {
        value1 = adfName;
      }

      String value2 = tempADFProp.getProperty(LAST_MODIFIED_DATE);
      if(value2 == null)
      {
        long lastModifiedTime = tempADFFile.lastModified();
        Date dateLastModified = new Date(lastModifiedTime);
        SimpleDateFormat smt = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
        value2 = smt.format(dateLastModified);
      }
      fis.close();
      descLastMd = value1.trim()+ "%%%" + value2.trim();

      Log.debugLog(className, "getADFDescription", "", "", "ADF Name " + adfName + " description = " + value1.trim()+ " LAST MODIFICATION TIME = " + value2.trim());
      return (descLastMd);
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getADFDescription", "", "", "Exception - ", e);
      return descLastMd;
    }
  }
  //Get list of all adf name, modification date & time and no. of adf
 /**
  * @method name getAllADFs
  * @pupose getting all ADF name, ADF description and ADF last modification date & time
  * @param adfName
  * @return 2D Array
  */
  public String[][] getAllADFs()
  {
    Log.debugLog(className, "getAllADFs", "", "", "Method called");
    try
    {
      ArrayList arrListAllADF = loadAllADF();  // Names of all MPROF files

      if(arrListAllADF == null)
        return null;

      String[][] arrADFs = new String[arrListAllADF.size() + 1][3];

      arrADFs[0][0] = "ADF Name";
      arrADFs[0][1] = "ADF Description";
      arrADFs[0][2] = "Modified Date";
      //arrADFs[0][3] = "Number Of ADF";

      for(int i = 0; i < arrListAllADF.size(); i++)
      {
        arrADFs[i+1][0] = arrListAllADF.get(i).toString();

        String tempDLM[] = getADFDescription(arrListAllADF.get(i).toString()).split("%%%");
        arrADFs[i+1][1] = tempDLM[0];
        arrADFs[i+1][2] = tempDLM[1];
        //arrADFs[i+1][3] = "Test3";
      }

      return arrADFs;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getAllADFs", "", "", "Exception - ", e);
      return new String[0][0];
    }
  }

 /**
  * @method name getAllADFs
  * @pupose remove particular adf file and bak file
  * @param adfNameWithPath
  * @return boolean
  */
  private boolean deleteADFsMain(String adfNameWithPath)
  {
    Log.debugLog(className, "deleteADFsMain", "", "", "ADF File = " + adfNameWithPath);

    try
    {
      File fileObj = new File(adfNameWithPath);
      if(fileObj.exists())
      {
        boolean success = fileObj.delete();
        if (!success)
        {
          Log.errorLog(className, "deleteADFsMain", "", "", "Error in deleting adf file (" + adfNameWithPath + ")");
          return false;
        }
      }
      else
      {
        Log.errorLog(className, "deleteADFsMain", "", "", "adf file (" + adfNameWithPath + ") does not exist.");
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "deleteADFsMain", "", "", "Exception - ", e);
      return false;
    }
    return true;
  }

  /**
   * @method name deleteADFs
   * @pupose remove particular adf file and bak file
   * @param adfName
   * @return boolean
   */
  public boolean deleteADFs(String adfName)
  {
    Log.debugLog(className, "deleteADFs", "", "", "ADF Name = " +  adfName);
    deleteADFsMain(getADFNameWithEXTN(adfName));
    return(deleteADFsMain(getBAKADFName(adfName)));
  }

  ///////////////////////////////////////END OF SECTION /////////////////////////////////////////

/**
 * @method name getADFDetails
 * @purpose get all detail of adf file
 * @param adfName
 * @return 2D Array
 */
  public String[][] getADFDetails(String adfName)
  {
    Log.debugLog(className, "getADFDetails", "", "", "Method called, file name = " +  adfName);
    String fileWithPath = getBAKADFName(adfName);
    try
    {
      Vector vecDataFile = readFile(fileWithPath);

      if(vecDataFile == null)
      {
        Log.debugLog(className, "getADFDetails", "", "", "ADF File not found, It may be correpted., file name = " + fileWithPath);
        return null;
      }

      ArrayList arrayList = new ArrayList();

      String[] arrADFDetail = new String[3];
      String[] arrTemp = null;

      String dataLine = "";
      int count = 1;

      boolean bolDesc = false;
      boolean bolMod = false;
      arrADFDetail[0] = adfName;
      for(int i = 0; i < vecDataFile.size(); i++)    //Profile description wiil be on the zeroth index and last modified date on oneth index.
      {
        dataLine = vecDataFile.elementAt(i).toString();

        if(dataLine.startsWith("ADF_DESC"))
        {
          arrTemp = null;
          arrTemp = rptUtilsBean.strToArrayData(dataLine, "=");
          arrADFDetail[1] = arrTemp[1].trim();
          bolDesc = true;
        }
        else if(dataLine.startsWith(LAST_MODIFIED_DATE))
        {
          arrTemp = null;
          arrTemp = rptUtilsBean.strToArrayData(dataLine, "=");
          arrADFDetail[2] = arrTemp[1].trim();
          bolMod = true;
        }
        else if((!dataLine.startsWith("ADF_DESC")) && (!dataLine.startsWith(LAST_MODIFIED_DATE)) && (!dataLine.startsWith("#")))
        {
          String arrAdfDetails[] = rptUtilsBean.strToArrayData(dataLine, "|");
          //for(int k = 0; k < arrAdfDetails.length; k++)
          arrayList.add(arrAdfDetails);
            //arrCustomMonitorDetails[count] = arrAdfDetails;
          //count++;
        }
      }
      if(!bolDesc)
      {
        arrADFDetail[1] = adfName;
      }
      if(!bolMod)
      {
        arrADFDetail[2] = rptUtilsBean.getCurDateTime();;
      }
       // append profile desc and last modified date at first index

      String[][] arrCustomMonitorDetails = new String[arrayList.size() + 1][9];  //Skipping Profile description and last modified date and add mprof detail(profile details and last modified date).


      //get an Iterator object for ArrayList using iterator() method.

      for(int ii = 0; ii < arrayList.size(); ii++)
      {
        String[] strRowValue = (String[])arrayList.get(ii);
        int indexCount = 2;
        if(strRowValue.length == 7)
        {
          for(int jj = 0; jj < strRowValue.length; jj++)
          {
            if((jj == 4) || (jj == 5) || (jj == 6))
            {
              if(jj == 4)
                arrCustomMonitorDetails[ii + 1][jj] = "0";
              else if(jj == 5)
                arrCustomMonitorDetails[ii + 1][jj] = "100";
              arrCustomMonitorDetails[ii + 1][jj + indexCount] = strRowValue[jj];
            }
            else
              arrCustomMonitorDetails[ii + 1][jj] = strRowValue[jj];
          }
        }
        else
        {
          for(int jj = 0; jj < strRowValue.length; jj++)
          {
            arrCustomMonitorDetails[ii + 1][jj] = strRowValue[jj];
          }
        }
      }

      arrCustomMonitorDetails[0] = arrADFDetail;
      return arrCustomMonitorDetails;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getADFDetails", "", "", "Exception - ", e);
      return null;
    }
  }


 /**
  * @method name createNewADF
  * @purpose create bak file
  * @param adfName
  * @param adfDesc
  * @return boolean
  **/

  public boolean createNewADF(String adfName, String adfDesc)
  {
    Log.debugLog(className, "createNewADF", "", "", "ADF Name =" + adfName + ", ADF description = " + adfDesc);
    return(createNewADF(adfName, adfDesc, false));
  }

 /**
  * @method name createNewADF
  * @purpose create bak file and .adf file
  * @param adfName
  * @param adfDesc
  * @param checkFileType
  * @return boolean
  */
  public boolean createNewADF(String adfName, String adfDesc, boolean checkFileType)
  {
    Log.debugLog(className, "createNewADF", "", "", "ADF Name =" + adfName + ", ADF desc = " + adfDesc);
    String adfNameWithPath = "";

    try
    {
      // This will allow user to create mprof file with .mprof extn.
      if(checkFileType)
        adfNameWithPath = getADFNameWithEXTN(adfName);
      else
        adfNameWithPath = getBAKADFName(adfName);

      File mprofFileObj = new File(adfNameWithPath);

      if(mprofFileObj.exists())
      {
        Log.errorLog(className, "createNewADF", "", "", "ADF already exists. ADF name = " + adfName);
        return false;
      }

      mprofFileObj.createNewFile();

      if(rptUtilsBean.changeFilePerm(mprofFileObj.getAbsolutePath(), "netstorm", "netstorm", "664") == false)
        return false;

      FileOutputStream fout = new FileOutputStream(mprofFileObj, true);  // append mode
      PrintStream pw = new PrintStream(fout);

      String profileDescLine = "ADF_DESC = " + adfDesc;
      pw.println(profileDescLine);
      pw.println(LAST_MODIFIED_DATE + " = " + rptUtilsBean.getCurDateTime());
      pw.close();
      fout.close();
      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "createNewADF", "", "", "Exception - ", e);
      return false;
    }
  }

/**
 * @method name saveADF
 * @purpose Save mprof will delete mprof file and copy bak file to mprof file.
 *          Bak is not deleted as GUI needs this file. Mod date/time is also updated in adf file.
 * @param adfName
 * @param adfDesc
 * @return boolean
 */
  public boolean saveADF(String adfName, String adfDesc)
  {
    Log.debugLog(className, "saveADF", "", "", "MPROF File =" + adfName + ", ADF description = " + adfDesc);

    try
    {
      // First check if Bak file is existing or not. If not create new adf Bak file
      String adfBakFileName = getBAKADFName(adfName);

      File adfBakFileObj = new File(adfBakFileName);

      if(!adfBakFileObj.exists())
        createNewADF(adfName, adfDesc);

      // Now copy Bak file to mprof file to save the changes
      String adfFileName = getADFNameWithEXTN(adfName);

      if(copyADFFile(adfName, adfBakFileName, adfFileName, "", adfDesc, false, true) == false)
      {
        Log.errorLog(className, "saveADF", "", "", "Error in copying Bak to adf (" + adfFileName + ")");
        return false;
      }

      // Now copy mprof to Bak file so that Bak is same as mprof file
      if(copyADFFile(adfName, adfFileName, adfBakFileName, "", "", false, false) == false)
      {
        Log.errorLog(className, "saveADF", "", "", "Error in copying mprof to Bak (" + adfFileName + ")");
        return false;
      }

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "saveMPROF", "", "", "Exception - ", e);
      return false;
    }
  }

/**
 * @method name copyADFFile
 * @purpose Copy source to destination file.
 * @param adfName
 * @param srcFileName
 * @param destFileName
 * @param destName
 * @param destDesc
 * @param updCreDate
 * @param updModDate
 * @return boolean
 */
  private boolean copyADFFile(String adfName, String srcFileName, String destFileName, String destName, String destDesc, boolean updCreDate, boolean updModDate)
  {
    Log.debugLog(className, "copyADFFile", "", "", "Method called");

    try
    {
      File fileSrc = new File(srcFileName);
      File fileDest = new File(destFileName);

      Vector vecSourceFileData = readFile(srcFileName);
      boolean lastModDateFlag = false;
      boolean adfDescFlag = false;

      if(!fileSrc.exists())
      {
        Log.errorLog(className, "copyADFFile", "", "", "Source file does not exists. Filename = " + srcFileName);
        return false;
      }

      if(fileDest.exists())
        fileDest.delete();
      fileDest.createNewFile();

      if(rptUtilsBean.changeFilePerm(fileDest.getAbsolutePath(), "netstorm", "netstorm", "664") == false)
        return false;

      String dataLine = "";
      for(int i = 0; i < vecSourceFileData.size(); i++)
      {
        dataLine = vecSourceFileData.elementAt(i).toString();
        if(dataLine.startsWith(LAST_MODIFIED_DATE))
          lastModDateFlag = true;
        if(dataLine.startsWith("ADF_DESC"))
          adfDescFlag = true;
      }


      FileInputStream fin = new FileInputStream(fileSrc);
      BufferedReader br = new BufferedReader(new InputStreamReader(fin));

      FileOutputStream fout = new FileOutputStream(fileDest, true);  // append mode
      PrintStream pw = new PrintStream(fout);

      if(!lastModDateFlag && !adfDescFlag) //Last modified date & adf desc is not in the adf, then write adf desc and current date in the file in the file.
      {
        pw.println("ADF_DESC = " + adfName);
        pw.println(LAST_MODIFIED_DATE + " = " + rptUtilsBean.getCurDateTime());
      }

      String str;

      while((str = br.readLine()) != null)
      {
        if(str.startsWith(LAST_MODIFIED_DATE))
        {
          if(updModDate)
            str = LAST_MODIFIED_DATE + " = " + rptUtilsBean.getCurDateTime();
        }
        if(str.startsWith("ADF_DESC"))
        {
          if(!destDesc.equals(""))
            str = "ADF_DESC = " + destDesc;
        }

        pw.println(str);
      }

      pw.close();
      br.close();
      fin.close();
      fout.close();
      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "copyADFFile", "", "", "Exception - ", e);
      return false;
    }
  }

/**
 * @method name addAlertToADF
 * @purpose Copy source to destination file.
 * @param mprofName
 * @param monitorDetails
 * @return
 */
  public boolean addAlertToADF(String adfName, String adfDesc, String adfDetails)
  {
    Log.debugLog(className, "addAlertToADF", "", "", "ADF Name =" + adfName);
    return(addAlertToADF(adfName, adfDesc, adfDetails, false));
  }

/**
  * @method name addAlertToADF
  * @purpose Add data in adf file.
  * @param adfName
  * @param adfDetails
  * @param fileType
  * @return boolean
*/
  public boolean addAlertToADF(String adfName, String adfDesc, String adfDetails, boolean fileType)
  {
    Log.debugLog(className, "addAlertToADF", "", "", "ADF Name = " + adfName + ", adfDesc = " + adfDesc + ", adfDetails = " + adfDetails);

    String adfNameWithPath = "";

    if(fileType)
      adfNameWithPath = getADFNameWithEXTN(adfName);
    else
      adfNameWithPath = getBAKADFName(adfName);

    try
    {
      // Read file's content
      Vector vecData = readFile(adfNameWithPath);

      File adfFileObj = new File(adfNameWithPath);

      adfFileObj.delete(); // Delete adf file
      adfFileObj.createNewFile(); // Create new adf file

      if(rptUtilsBean.changeFilePerm(adfFileObj.getAbsolutePath(), "netstorm", "netstorm", "664") == false)
        return false;

      if(!adfFileObj.exists())
      {
        Log.errorLog(className, "addAlertToADF", "", "", "ADF file does not exist. ADF filename is - " + adfNameWithPath);
        return(false);
      }

      FileOutputStream fout = new FileOutputStream(adfFileObj, true);  // append mode
      PrintStream pw = new PrintStream(fout);

      String dataLine = "";
      boolean bolLASTDATE = false;
      boolean bolADFDesc = false;

      for(int i = 0; i < vecData.size(); i++)
      {
        dataLine = vecData.get(i).toString();
        if(dataLine.startsWith(LAST_MODIFIED_DATE))
        {
          dataLine = LAST_MODIFIED_DATE + " = " + rptUtilsBean.getCurDateTime();
          bolLASTDATE = true;
        }

        if(dataLine.startsWith("ADF_DESC"))
        {
          dataLine = "ADF_DESC" + " = " + adfDesc;
          bolADFDesc = true;
        }
        pw.println(dataLine);
      }

      if(!bolLASTDATE)
        pw.println(LAST_MODIFIED_DATE + " = " + rptUtilsBean.getCurDateTime());
      if(!bolADFDesc)
        pw.println("ADF_DESC" + " = " + adfName);

      pw.println(adfDetails);  // Append the new monitor to the end of file
      pw.close();
      fout.close();
      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "addAlertToADF", "", "", "Exception - ", e);
      return false;
    }
  }

/**
 * @method name createADFBackup
 * @purpose Create backup file from main file.
 *          Edit/changes are done in Backup file
 * @param adfName
 * @return boolean
 */
  public boolean createADFBackup(String adfName)
  {
    Log.debugLog(className, "createADFBackup", "", "", "ADF Name = " +  adfName);
    String adfFileName = "";

    try
    {
      adfFileName = getADFNameWithEXTN(adfName);
      File mprofFileObj = new File(adfFileName);

      if(!mprofFileObj.exists())
      {
        Log.errorLog(className, "createADFBackup", "", "", "Source mprof does not exist. ADF name = " + adfFileName);
        return false;
      }

      String adfBakFileName = getBAKADFName(adfName);

      if(copyADFFile(adfName, adfFileName, adfBakFileName, "", "", false, false) == false)
      {
        Log.errorLog(className, "createADFBackup", "", "", "Error in creating Bakup of ADF. ADF file is " + adfFileName + ". Backup file is " + adfBakFileName);
        return false;
      }
      return(true);
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "createADFBackup", "", "", "Exception - ", e);
      return false;
    }
  }

  // Make copy of a mprof with new name and profile desc
  // Also creation date and modification date is new mprof is set to current date/time
/**
 *
 * @param strSrcADFName
 * @param strDestADFName
 * @param strADFDesc
 * @return
 */
  public boolean copyADF(String strSrcADFName, String strDestADFName, String strADFDesc)
  {
    Log.debugLog(className, "copyADF", "", "", "SrcName =" +  strSrcADFName + ", DestName = " + strDestADFName + ", ADF Description = " + strADFDesc);

    try
    {
      String adfSrcFileName = getADFNameWithEXTN(strSrcADFName);
      String adfDestFileName = getADFNameWithEXTN(strDestADFName);

      File adfSrcFileObj = new File(adfSrcFileName);
      if(!adfSrcFileObj.exists())
      {
        Log.errorLog(className, "copyADF", "", "", "Source adf does not exist. ADF name = " + strSrcADFName);
        return false;
      }

      File adfDestFileObj = new File(adfDestFileName);
      if(adfDestFileObj.exists())
      {
        Log.errorLog(className, "copyADF", "", "", "Destination adf already exists. ADF name = " + strDestADFName);
        return false;
      }

      FileInputStream fin = new FileInputStream(adfSrcFileName);
      BufferedReader br = new BufferedReader(new InputStreamReader(fin));

      FileOutputStream fout = new FileOutputStream(adfDestFileName, true);  // append mode
      PrintStream pw = new PrintStream(fout);

      String dataLine = "";
      while((dataLine = br.readLine()) != null)
      {
        if(dataLine.startsWith("ADF_DESC"))
        {
          dataLine = "ADF_DESC = " + strADFDesc;
        }
        if(dataLine.startsWith(LAST_MODIFIED_DATE))
        {
          dataLine = LAST_MODIFIED_DATE + " = " + rptUtilsBean.getCurDateTime();
        }
        pw.println(dataLine);
      }

      pw.close();
      br.close();
      fin.close();
      fout.close();


      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "copyADF", "", "", "Exception - ", e);
      return false;
    }
  }

  // This will delete selected custom monitor from mprof
  // selected custom monitor are passed as array of index.
/**
 *
 * @param adfName
 * @param arrADFIdx
 * @return
 */
  public boolean deleteMonitorFromADFMain(String adfName, String[] arrADFIdx)
  {
    Log.debugLog(className, "deleteMonitorFromADFMain", "", "", "ADF Name = " + adfName);

    String adfNameWithPath = getBAKADFName(adfName);
    try
    {
      Vector vecData = readFile(adfNameWithPath);

      File adfFileObj = new File(adfNameWithPath);

      if(adfFileObj.exists())
        adfFileObj.delete(); // Delete mprof bak file

      adfFileObj.createNewFile(); // Create new mprof bak file

      if(rptUtilsBean.changeFilePerm(adfFileObj.getAbsolutePath(), "netstorm", "netstorm", "664") == false)
        return false;

      FileOutputStream fout = new FileOutputStream(adfFileObj, true);  // append mode
      PrintStream pw = new PrintStream(fout);
      String status = "";

      String dataLine = "";

      pw.println(vecData.elementAt(0).toString()); // for profile description

      for(int i = 1; i < vecData.size(); i++)
      {
        dataLine = vecData.elementAt(i).toString();

        if(dataLine.startsWith(LAST_MODIFIED_DATE))
        {
          dataLine = LAST_MODIFIED_DATE + " = " + rptUtilsBean.getCurDateTime();
          pw.println(dataLine);
        }
        else
        {
          status = "";
          for(int k = 0; k < arrADFIdx.length; k++)
          {
            if((i - 2) == (int)Integer.parseInt(arrADFIdx[k])) // substratct by 2 because first line is description and second is last modified date of profile
            {
              status = "true";
              break;
            }
          }
          if(!status.equals("true"))
            pw.println(vecData.elementAt(i).toString());
        }
      }

      pw.close();
      fout.close();

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "deleteMonitorFromADFMain", "", "", "Exception - ", e);
      return false;
    }
  }

  // To check if mprof is modified or not after last save was done
  public boolean isADFModified(String adfName)
  {
    Log.debugLog(className, "isADFModified", "", "", "Method called. Profile Name = " + adfName);
    try
    {

      String adfFileName = getADFNameWithEXTN(adfName);
      String adfBakFileName = getBAKADFName(adfName);

      Vector vecData = readFile(adfFileName);
      Vector vecBakData = readFile(adfBakFileName);

      String strDate = "";
      String strBakDate = "";

      boolean bolDate = false;
      String dataLine = "";
      for(int i = 0; i < vecData.size(); i++)
      {
        dataLine = vecData.get(i).toString();

        if(dataLine.startsWith(LAST_MODIFIED_DATE))
        {
          String[] arrTemp = rptUtilsBean.strToArrayData(dataLine, "=");
          strDate = arrTemp[1].trim();
          bolDate = true;
          break;
        }
      }

      if(!bolDate)
      {
        File fileName = new File(adfFileName);
        long lastModifiedTime = fileName.lastModified();
        Date dateLastModified = new Date(lastModifiedTime);
        SimpleDateFormat smt = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
        strDate = smt.format(dateLastModified);
      }

      for(int i = 0; i < vecBakData.size(); i++)
      {
        dataLine = vecBakData.get(i).toString();

        if(dataLine.startsWith(LAST_MODIFIED_DATE))
        {
          String[] arrTemp = rptUtilsBean.strToArrayData(dataLine, "=");
          strBakDate = arrTemp[1].trim();
          break;
        }
      }

      Log.debugLog(className, "isADFModified", "", "", "Modification Date/Time for ADF and Bak file are - " + strDate + " and " + strBakDate);

      // Compare modified file
      if((rptUtilsBean.convertDateToMilliSec(strDate)) == (rptUtilsBean.convertDateToMilliSec(strBakDate)))
        return false;

      return true;

    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "isADFModified", "", "", "Exception - ", e);
      return false;
    }
  }

  /**
   *
   * @param adfName
   * @param arrADFIdx
   * @return
   */
    public boolean deleteMonitorFromADF(String adfName, String[] arrADFIdx)
    {
      Log.debugLog(className, "deleteMonitorFromADF", "", "", "ADF Name = " + adfName);

      String adfNameWithPath = getBAKADFName(adfName);
      try
      {
        Vector vecData = readFile(adfNameWithPath);

        File adfFileObj = new File(adfNameWithPath);

        if(adfFileObj.exists())
          adfFileObj.delete(); // Delete mprof bak file

        adfFileObj.createNewFile(); // Create new mprof bak file

        if(rptUtilsBean.changeFilePerm(adfFileObj.getAbsolutePath(), "netstorm", "netstorm", "664") == false)
          return false;

        FileOutputStream fout = new FileOutputStream(adfFileObj, true);  // append mode
        PrintStream pw = new PrintStream(fout);
        String status = "";

        String dataLine = "";

        int j = 0;
        int count = 0;
        //pw.println(vecData.elementAt(0).toString()); // for profile description

         for(int i = 0; i < vecData.size(); i++)
        {
          dataLine = vecData.elementAt(i).toString();

          if(dataLine.startsWith(LAST_MODIFIED_DATE))
          {
            dataLine = LAST_MODIFIED_DATE + " = " + rptUtilsBean.getCurDateTime();
            pw.println(dataLine);
            j++;
          }
          else if((dataLine.startsWith("ADF_DESC")) || (dataLine.startsWith("#")))
          {
            pw.println(vecData.elementAt(i).toString());
            j++;
          }
          else
          {
            status = "";
            for(int k = 0; k < arrADFIdx.length; k++)
            {
              if((count) == (int)Integer.parseInt(arrADFIdx[k])) // substratct by 2 because first line is description and second is last modified date of profile
              {
                status = "true";
                break;
              }
            }
            count++;
            if(!status.equals("true"))
              pw.println(vecData.elementAt(i).toString());
          }
        }

        pw.close();
        fout.close();

        return true;
      }
      catch(Exception e)
      {
        Log.stackTraceLog(className, "deleteMonitorFromADF", "", "", "Exception - ", e);
        return false;
      }
    }

    // read netstorm.adf file and store data in object and return that object
    public Object getTestRunADFInfo()
    {
      Log.debugLog(className, "getTestRunADFInfo", "", "", "Method called.");

      TestRunAdfInfo testRunAdfInfo_obj = new TestRunAdfInfo();
      try
      {
        Vector vecData = readFile(getTestRunADFNameWithEXTN());

        if(vecData == null)
          return null;

        String dataLine = "";

        for(int ii = 0; ii < vecData.size(); ii++)
        {
          dataLine = vecData.get(ii).toString();
          if(dataLine.trim().startsWith("#"))
            continue;
          String arrAdfDetails[] = rptUtilsBean.strToArrayData(dataLine, "|");

          if(arrAdfDetails.length < 9)
            continue;
          testRunAdfInfo_obj.setADFArrayListInfo(arrAdfDetails[0], arrAdfDetails[1], arrAdfDetails[2], arrAdfDetails[3], arrAdfDetails[4], arrAdfDetails[5], arrAdfDetails[6], arrAdfDetails[7], arrAdfDetails[8]);
        }
        return testRunAdfInfo_obj;
      }
      catch (Exception e) {
        Log.stackTraceLog(className, "getTestRunADFInfo", "", "", "Exception - ", e);
        return testRunAdfInfo_obj;
      // TODO: handle exception
     }
    }
  ///////////////////////////////////////Start Main Method /////////////////////////////////////////
  public static void main(String args[])
  {
    ADFBean ADFBean_obj = new ADFBean();
    //String filePath = ADFBean_obj.getADFNameWithEXTN("test");
    //Vector vecADFBean = ADFBean_obj.readFile(filePath);

    /**ArrayList arrLAADF = ADFBean_obj.loadAllADF();
    for(int i = 0; i< arrLAADF.size(); i++)
    {
      System.out.println("---------" + arrLAADF.get(i));
    }**/

    /**String arrfileList[][] = ADFBean_obj.getAllADFs();
    for(int i = 0; i< arrfileList.length; i++)
    {
      System.out.println("---------\n" + arrfileList[i][0] + "\n" + arrfileList[i][1] + "\n" + arrfileList[i][2]);
    } **/

   // String arrData[][] = ADFBean_obj.getADFDetails("TEDELETE");
    //for(int i = 0; i < arrData.length; i++)
      //for(int j = 0; j < arrData[i].length; j++)
        //System.out.println("data value[" + i + "][" + j + "] = " + arrData[i][j]);

    //ADFBean_obj.createNewADF("TEDELETE", "DESCP1");
    //ADFBean_obj.saveADF("TEDELETE", "DESCP1");

    //ADFBean_obj.addAlertToADF("TEDELETE", "TEST2", "1|3|vecName3|+|8|9|3|4|5" );
    //ADFBean_obj.saveADF("TEDELETE", "DESCP1");

    //String arrTemp[] = new String[]{"3"};
    //ADFBean_obj.deleteMonitorFromADF("TEDELETE", arrTemp);
    //boolean bolMod =  false;
    //bolMod = ADFBean_obj.isADFModified("TEDELETE");
    //System.out.print("Check last modify date " + bolMod);
    TestRunAdfInfo testRunAdfInfo = (TestRunAdfInfo)ADFBean_obj.getTestRunADFInfo();

    if(testRunAdfInfo != null)
    {
      for(int k = 0; k < testRunAdfInfo.getAlertTypeInfo().size(); k++)
      {
        System.out.print(" Group Id = " + testRunAdfInfo.getGroupIdInfo().get(k).toString());
        System.out.print(" Graph Id = " + testRunAdfInfo.getGraphIdInfo().get(k).toString());
        System.out.print(" Vec Name = " + testRunAdfInfo.getVecNameInfo().get(k).toString());
        System.out.print(" Alert Type = " + testRunAdfInfo.getAlertTypeInfo().get(k).toString());
        System.out.print(" Minimum = " + testRunAdfInfo.getMinimumInfo().get(k).toString());
        System.out.print(" Maximum = " + testRunAdfInfo.getMaximumInfo().get(k).toString());
        System.out.print(" Warning = " + testRunAdfInfo.getWarningInfo().get(k).toString());
        System.out.print(" Major = " + testRunAdfInfo.getMajorInfo().get(k).toString());
        System.out.print(" Critical = " + testRunAdfInfo.getCriticalInfo().get(k).toString());
        System.out.println();
      }
    }
    else
      System.out.println("Data object having null value");

    testRunAdfInfo.saveDataToFile();
  }
}
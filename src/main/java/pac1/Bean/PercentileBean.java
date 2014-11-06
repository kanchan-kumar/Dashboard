/*-------------------------------------------------------------------------------------------------
@Name    : PercentileBean.java
@Author  : Prabhat
@Purpose : Provided the utility function(s) to PDF(Percentile description Graph) Management GUI(jsp)
@Modification History:

--------------------------------------------------------------------------------------------------*/
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

public class PercentileBean
{
  private String className = "PercentileBean";
  private String workPath = Config.getWorkPath();
  private final String PDF_FILE_EXTN = ".pdf";
  private final String PDF_BAK_EXTN = ".hot";

  private transient final int HEADER_INDEX = 0; // Header info index

  // PDF|PDF_Id|Min_Granule|Max_Granule|numSlabs|PDF_UNIT|PDF_Description
  // Indexes of PDF Line
  private transient final int PDF_ID_INDEX = 1;
  private transient final int MIN_GRANULE_INDEX = 2;
  private transient final int MAX_GRANULE_INDEX = 3;
  private transient final int SLAB_COUNT_INDEX = 4;
  private transient final int PDF_UNIT_INDEX = 5;
  private transient final int PDF_DESC_INDEX = 6;

  //Slab|Slab_Name|Slab_Id|Min_Value|Max_Value|Color
  // Indexes of Slab Line
  private transient final int SLAB_NAME_INDEX = 1;
  private transient final int SLAB_ID_INDEX = 2;
  private transient final int SLAB_MIN_VALUE_INDEX = 3;
  private transient final int SLAB_MAX_VALUE_INDEX = 4;
  private transient final int COLOR_INDEX = 5;

  private int numPDF;

  // Info|pctMsgVersion|numPDF|sizeOfPctMsgData|Mode|Interval
  private transient final String PDF_INFO_HDR_LINE = "Info|1.0|1|-1|-1|-1";  //fix it when number of PDF is more than one in future.
  private transient final String LAST_MODIFIED_DATE = "#LAST_MODIFIED_DATE = ";

  //We allow user to assign id for user defined PDF to 20000
  private transient final int USER_DEFINED_PDF_ID = 20000;
  private transient final String LAST_MAX_ASSIGNED_SLAB_ID = "#LAST_MAX_ASSIGNED_SLAB_ID = 0";
  public PercentileBean(){ }

  // This will return the PDF path
  private String getPDFPath()
  {
    return (workPath + "/pdf/");
  }

  // This will return the bak pdf name
  private String getBAKPDFName(String pdfName)
  {
    return ("." + pdfName + PDF_FILE_EXTN + PDF_BAK_EXTN);
  }

  // load all pdf file in Array List
  public String[] getAllPDF()
  {
    Log.debugLog(className, "getAllPDF", "", "", "Start method");

    ArrayList arrAllPDF = new ArrayList();  // To contain names of all PDF files
    ArrayList arrListOfAllPDFSortedOrder = new ArrayList(); //To keep name of all pdf files in sorted order
    File PDF = new File(getPDFPath());
    Log.debugLog(className, "getAllPDF", "", "", "getPDFPath = " + getPDFPath());
    String temp;
    String tempPath;
    String[] strListOfPDF = null;

    try
    {
      // For PDF
      if(PDF == null)
        return null;

      String arrFiles[] = PDF.list();
      tempPath = getPDFPath();

      // Calculate number of pdf files
      for(int j = 0; j < arrFiles.length; j++)
      {
        temp = "";

        if(arrFiles[j].lastIndexOf(PDF_FILE_EXTN) == -1)  // Skip non pdf files
          continue;

        temp = tempPath + arrFiles[j];

        String[] tempArr = rptUtilsBean.strToArrayData(arrFiles[j], "."); // for remove pdf file extension
        if(tempArr.length == 2) // add only <filename>.pdf file
        {
          arrAllPDF.add(tempArr[0]);
          Log.debugLog(className, "getAllPDF", "", "", "Adding PDF in ArrayList = " + temp);
        }
        else
        {
          Log.debugLog(className, "getAllPDF", "", "", "Skip file = " + temp);
        }
      }

      Log.debugLog(className, "getAllPDF", "", "", "Number of PDF files = " + arrAllPDF.size());

      Object[] arrTemp = arrAllPDF.toArray();
      Arrays.sort(arrTemp);

      strListOfPDF = new String[arrTemp.length];

      for(int i = 0; i < arrTemp.length; i++)
      {
        arrListOfAllPDFSortedOrder.add(arrTemp[i].toString());
        strListOfPDF[i] = arrTemp[i].toString();
      }

      return strListOfPDF;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getAllPDF", "", "", "Exception - ", e);
      return null;
    }
  }


  // Open file and return as File object
  private File openFile(String fileName)
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

  // Methods for reading the File
  private Vector readFile(String pdfFileWithPath)
  {
    Log.debugLog(className, "readFile", "", "", "Method called. PDF FIle Name = " + pdfFileWithPath);

    try
    {
      Vector vecFileData = new Vector();
      String strLine;

      File customPDF = openFile(pdfFileWithPath);

      if(!customPDF.exists())
      {
        Log.errorLog(className, "readFile", "", "", "PDF not found, filename - " + pdfFileWithPath);
        return null;
      }

      FileInputStream fis = new FileInputStream(pdfFileWithPath);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));

      while((strLine = br.readLine()) !=  null)
      {
        strLine = strLine.trim();
        //if(strLine.startsWith("#"))
          //continue;
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

  private Vector readSlabData(Vector vecData)
  {
    Log.debugLog(className, "readSlabData", "", "", "Method called");

    try
    {
      Vector vecSlabDetails = new Vector();
      for(int i = 0; i < vecData.size(); i++)
      {
        if((vecData.elementAt(i).toString()).startsWith("Slab"))
        {
          vecSlabDetails.add(vecData.elementAt(i).toString());
          Log.debugLog(className, "readSlabData", "", "", "Adding Slab details to vector = " + vecData.elementAt(i).toString());
        }
      }
      return vecSlabDetails;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "readSlabData", "", "", "Exception - ", e);
      return null;
    }
  }

  // Get pdf file information
  private Vector getDataFromPdf(String pdfFileName)
  {
    Log.debugLog(className, "getDataFromPdf", "", "", "Method called");

    String pdfFileWithPath = getPDFPath() + pdfFileName + PDF_FILE_EXTN;

    return(readFile(pdfFileWithPath));
  }

  // PDF|PDF_Id|Min_Granule|Max_Granule|numSlabs|PDF_UNIT|PDF_Description
  //Chitra- index 1-> PDFName, index 2-> min_granule, index 3-> max_granule, index 4-> numSlabs, index 5-> PDF_UNIT, index 6-> PDF_Description, index 7-> Last Modified Date
  public String[][] getPDFs()
  {
    Log.debugLog(className, "getPDFs", "", "", "Method Starts.");
    try
    {
      String[] arrListOfPDF = getAllPDF();
      String[][] tempArrPDFDetails = new String[arrListOfPDF.length + 1][7]; // add 1 for header
      Vector vecData = null;
      String[] arrRcrd = null;
      String pdfFileName;
      String customPDFPath = getPDFPath();

      tempArrPDFDetails[0][0] = "PDF Name";
      tempArrPDFDetails[0][1] = "Min Granule";
      tempArrPDFDetails[0][2] = "Max Granule";
      tempArrPDFDetails[0][3] = "Number Of Slabs";
      tempArrPDFDetails[0][4] = "PDF Unit";
      tempArrPDFDetails[0][5] = "PDF Description";
      tempArrPDFDetails[0][6] = "Modified Date";

      for(int i = 0; i < arrListOfPDF.length; i++)
      {
        String lastModDateTime = "";
        pdfFileName = customPDFPath + arrListOfPDF[i] + PDF_FILE_EXTN;

        vecData = getDataFromPdf(arrListOfPDF[i]);

        if(vecData == null)
        {
          Log.debugLog(className, "getPDFs", "", "", "PDF File not found, It may be correpted, file name = " + tempArrPDFDetails[i + 1][0]);
          continue;
        }

        tempArrPDFDetails[i + 1][0] = arrListOfPDF[i];
        Log.debugLog(className, "getPDFs", "", "", "Adding PDF name in array = " + tempArrPDFDetails[i + 1][0]);
        int countSlab = 0;
        for (int j = 0; j < vecData.size(); j++)
        {
          arrRcrd = null;
          arrRcrd = rptUtilsBean.strToArrayData(vecData.elementAt(j).toString(), "|");

          if((vecData.elementAt(j).toString()).startsWith("LAST_MODIFIED_DATE"))
          {
             String[] arrTemp = rptUtilsBean.strToArrayData(vecData.elementAt(j).toString(), "=");
             lastModDateTime = arrTemp[1].trim();
          }

          if(arrRcrd[HEADER_INDEX].equals("PDF"))
          {
            tempArrPDFDetails[i + 1][0] = arrListOfPDF[i];  //PDF Name
            Log.debugLog(className, "getPDFs", "", "", "Adding PDF name in array = " + tempArrPDFDetails[i + 1][1]);

            String[] arrTemp = rptUtilsBean.strToArrayData(vecData.elementAt(j).toString(), "|");

            tempArrPDFDetails[i + 1][1] = arrTemp[MIN_GRANULE_INDEX];
            tempArrPDFDetails[i + 1][2] = arrTemp[MAX_GRANULE_INDEX];
            tempArrPDFDetails[i + 1][3] = arrTemp[SLAB_COUNT_INDEX];
            tempArrPDFDetails[i + 1][4] = arrTemp[PDF_UNIT_INDEX];
            tempArrPDFDetails[i + 1][5] = arrTemp[PDF_DESC_INDEX];

            if(lastModDateTime.equals(""))
            {
              File fileObj = openFile(pdfFileName);
              long lastModifiedTime = fileObj.lastModified();
              Date dateLastModified = new Date(lastModifiedTime);
              SimpleDateFormat smt = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
              lastModDateTime = smt.format(dateLastModified);
            }

            tempArrPDFDetails[i + 1][6] = lastModDateTime;
          }
        }
      }//end of for
      return tempArrPDFDetails;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getPDFs", "", "", "Exception - ", e);
      return null;
    }
  }

  // Return the PDF details in 2d array
  public String[][] getPDFDetails(String pdfName)
  {
    Log.debugLog(className, "getPDFDetails", "", "", "Method Starts. PDF Name = " + pdfName);

    try
    {
      Vector vecCustomPDFData = getCustomPDFDetailsInVec(pdfName);
      if(vecCustomPDFData == null)
      {
        Log.debugLog(className, "getPDFDetails", "", "", "PDF File not found, It may be correpted.");
        return null;
      }

      // this array contain PDF info(0 -> PDF name, 1 -> PDF ID, 2 -> min_granule, 3 -> max_granule, 4 -> #_Slab, 5 -> PDF_Unit, 6 -> PDF_Description
      String[][] arrCustomPDFDetails = new String[vecCustomPDFData.size()][7];
      String[] arrRcrd = null;

      for(int i = 0; i < vecCustomPDFData.size(); i++)
      {
        arrRcrd = null;
        arrRcrd = rptUtilsBean.strToArrayData(vecCustomPDFData.elementAt(i).toString(), "|");

        arrCustomPDFDetails[i][0] = arrRcrd[0];
        arrCustomPDFDetails[i][1] = arrRcrd[1];
        arrCustomPDFDetails[i][2] = arrRcrd[2];
        arrCustomPDFDetails[i][3] = arrRcrd[3];
        arrCustomPDFDetails[i][4] = arrRcrd[4];
        arrCustomPDFDetails[i][5] = arrRcrd[5];
        arrCustomPDFDetails[i][6] = arrRcrd[6];
      }

      return arrCustomPDFDetails;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getPDFDetails", "", "", "Exception - ", e);
      return null;
    }
  }

  // Return the PDF Graph details in vector
  private Vector getCustomPDFDetailsInVec(String customPDFName)
  {
    Log.debugLog(className, "getCustomPDFDetailsInVec", "", "", "Method Starts. PDF Name = " + customPDFName);

    Vector vecData = null;
    Vector vecCustomPDFData = new Vector();
    String[] arrRcrd = null;

    try
    {
      String pdfFileWithPath = getPDFPath() + getBAKPDFName(customPDFName);

      vecData = readFile(pdfFileWithPath);

      if(vecData == null)
      {
        Log.debugLog(className, "getCustomPDFDetailsInVec", "", "", "GDF File not found, It may be correpted.");
        return null;
      }

      int countGraph = 0;
      for (int j = 0; j < vecData.size(); j++)
      {
        arrRcrd = null;
        arrRcrd = rptUtilsBean.strToArrayData(vecData.elementAt(j).toString(), "|");

        String dataLine = "";
        if(arrRcrd[HEADER_INDEX].equals("PDF"))
        {
          dataLine = customPDFName + "|" + arrRcrd[PDF_ID_INDEX] + "|" + arrRcrd[MIN_GRANULE_INDEX] + "|" + arrRcrd[MAX_GRANULE_INDEX] + "|" + arrRcrd[SLAB_COUNT_INDEX] + "|" + arrRcrd[PDF_UNIT_INDEX] + "|" + arrRcrd[PDF_DESC_INDEX];
          vecCustomPDFData.add(dataLine);

          Log.debugLog(className, "getCustomPDFDetailsInVec", "", "", "Adding PDF detail to vector = " + dataLine);
        }
        else if(arrRcrd[HEADER_INDEX].equals("Slab"))
        {
          dataLine = arrRcrd[SLAB_NAME_INDEX] + "|" + arrRcrd[SLAB_ID_INDEX] + "|" + arrRcrd[SLAB_MIN_VALUE_INDEX] + "|" + arrRcrd[SLAB_MAX_VALUE_INDEX] + "|NA|NA|NA";//added NA fields to make array size consistent (7)

          vecCustomPDFData.add(dataLine);
          Log.debugLog(className, "getCustomPDFDetailsInVec", "", "", "Adding Slab data line to vector = " + dataLine);
        }
      }
      return vecCustomPDFData;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getCustomPDFDetailsInVec", "", "", "Exception - ", e);
      return null;
    }
  }

  //return the updated slab data line
  private String updateSlabLine(String pdfDataLine, int updatedSlabCount)
  {
    Log.debugLog(className, "updateSlabLine", "", "", "Method Starts. Slab data line = " + pdfDataLine + ", updated group count = " + updatedSlabCount);

    try
    {
      String strUpdatedDataLine = "";
      String[] arrPDFData = rptUtilsBean.strToArrayData(pdfDataLine, "|");

      arrPDFData[SLAB_COUNT_INDEX] = "" + updatedSlabCount;

      for(int i = 0; i < arrPDFData.length; i++)
      {
        if(i == 0)
          strUpdatedDataLine = arrPDFData[i];
        else
          strUpdatedDataLine = strUpdatedDataLine + "|" + arrPDFData[i];
      }

      return strUpdatedDataLine;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "updateSlabLine", "", "", "Exception - ", e);
      return null;
    }
  }


  // return the PDF info line
  private String getPDFInfoLine(String pdfName, String minGranule, String maxGranule, String pdfUnit, String pdfDesc)
  {
    Log.debugLog(className, "getPDFInfoLine", "", "", "Method Starts. PDF name = " + pdfName + ", minGranule = " + minGranule + ", maxGranule = " + maxGranule + ", pdfUnit = " + pdfUnit + ", pdfDesc = " + pdfDesc);

    try
    {
      //PDF|PDF_Id|Min_Granule|Max_Granule|numSlabs|PDF_UNIT|PDF_Description
      String pdfLine = "PDF|" + getUniquePDFId() + "|" + minGranule + "|" + maxGranule + "|0|" + pdfUnit + "|" + pdfDesc;
      return pdfLine;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getPDFInfoLine", "", "", "Exception - ", e);
      return null;
    }
  }


  // This function return the unique group ID of PDF
  // We allow user to assign id for user defined PDF to 20000
  private String getUniquePDFId()
  {
    Log.debugLog(className, "getUniquePDFId", "", "", "Method Starts.");

    try
    {
      String[] arrListOfCustomPDF = getAllPDF();
      ArrayList arrListPDFId = new ArrayList();
      String uniqueGroupId = "";
      String[] arrRcrd = null;

      for(int i = 0; i < arrListOfCustomPDF.length; i++)
      {
        Vector vecData = getDataFromPdf(arrListOfCustomPDF[i].toString());

        if(vecData == null)
        {
          Log.debugLog(className, "getUniquePDFId", "", "", "PDF File not found, It may be correpted, file name = " + arrListOfCustomPDF[i].toString());
          continue;
        }

        for (int j = 0; j < vecData.size(); j++)
        {
          arrRcrd = null;
          arrRcrd = rptUtilsBean.strToArrayData(vecData.elementAt(j).toString(), "|");

          if(arrRcrd[HEADER_INDEX].equals("PDF"))
          {
            arrListPDFId.add(arrRcrd[PDF_ID_INDEX]);
            Log.debugLog(className, "getUniquePDFId", "", "", "Adding group ID to array list = " + arrRcrd[PDF_ID_INDEX]);
            break;
          }
        }
      }

      if((arrListPDFId != null) && (arrListPDFId.size() > 0))
      {
        int[] arrStrTmp = new int[arrListPDFId.size()];
        for(int i = 0; i < arrListPDFId.size(); i++)
          arrStrTmp[i] = Integer.parseInt(arrListPDFId.get(i).toString());

        Arrays.sort(arrStrTmp);

        int maxGroupId = arrStrTmp[arrStrTmp.length - 1];

        if(maxGroupId < USER_DEFINED_PDF_ID)
          uniqueGroupId = "" + (USER_DEFINED_PDF_ID + 1);
        else
          uniqueGroupId = "" + (maxGroupId + 1);
      }
      else
        uniqueGroupId = "" + (USER_DEFINED_PDF_ID + 1);

      return uniqueGroupId;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getUniquePDFId", "", "", "Exception - ", e);
      return null;
    }
  }
  /**
   * @param pdfName : pdf file name, in which we want to add slab.
   * @return : slab id that will be one more then slab IDs in the pdf
   */
  private String getUniqueSlabId(Vector vecPDFData)
  {
    Log.debugLog(className, "getUniqueSlabId", "", "", "Method Starts.");

    try
    {
      ArrayList arrListSlabId = new ArrayList();
      String uniqueSlabId = "";
      String[] arrRcrd = null;

      for (int j = 0; j < vecPDFData.size(); j++)
      {
        arrRcrd = null;
        arrRcrd = rptUtilsBean.strToArrayData(vecPDFData.elementAt(j).toString(), "|");

        if(arrRcrd[HEADER_INDEX].equals("Slab"))
        {
          arrListSlabId.add(arrRcrd[SLAB_ID_INDEX]);
          Log.debugLog(className, "getUniqueSlabId", "", "", "Adding Slab ID to array list = " + arrRcrd[SLAB_ID_INDEX]);
        }//end of if
      }//end of for

      if((arrListSlabId != null) && (arrListSlabId.size() > 0))
      {
        int[] arrStrTmp = new int[arrListSlabId.size()];
        for(int i = 0; i < arrListSlabId.size(); i++)
          arrStrTmp[i] = Integer.parseInt(arrListSlabId.get(i).toString());

        Arrays.sort(arrStrTmp);

        uniqueSlabId = "" + (arrStrTmp[arrStrTmp.length - 1] + 1);
      }//end of if
      else
        uniqueSlabId = "" + 1;  //put slab id 1 for first slab.

      return uniqueSlabId;
    }//end of try
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getUniqueSlabId", "", "", "Exception - ", e);
      return null;
    }
  }


  /***********************************************************************************************/
  //                              Utility function(s) called from JSP                            //
  /***********************************************************************************************/

  // Delete methods
  // This delete the existing pdf
  public boolean deletePDF(String pdfName)
  {
    Log.debugLog(className, "deletePDF", "", "", "PDF Name = " +  pdfName);
    deletePDF(pdfName, PDF_BAK_EXTN);
    return(deletePDF(pdfName, PDF_FILE_EXTN));
  }

  // This delete the  gdf backup if present
  public boolean deleteBakPDF(String gdfName)
  {
    Log.debugLog(className, "deleteBakPDF", "", "", "PDF Name = " +  gdfName);
    return(deletePDF(gdfName, PDF_BAK_EXTN));
  }

  // This delete the existing PDF
  private boolean deletePDF(String pdfName, String fileExtn)
  {
    Log.debugLog(className, "deletePDF", "", "", "PDF File = " + pdfName + "." + fileExtn);
    String tmptFile = "";

    try
    {
      if(fileExtn.equals(PDF_FILE_EXTN))
        tmptFile = getPDFPath() + pdfName + fileExtn;
      else if(fileExtn.equals(PDF_BAK_EXTN))
        tmptFile = getPDFPath() + getBAKPDFName(pdfName);

      File fileObj = new File(tmptFile);
      if(fileObj.exists())
      {
        boolean success = fileObj.delete();
        if (!success)
        {
          Log.errorLog(className, "deletePDF", "", "", "Error in deleting gdf file (" + tmptFile + ")");
          return false;
        }
      }
      else
      {
        Log.errorLog(className, "deletePDF", "", "", "PDF file (" + tmptFile + ") does not exist.");
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "deletePDF", "", "", "Exception in deletion of gdf (" + tmptFile + ") - ", e);
      return false;
    }
    return true;
  }


  // Make Bak file of PDF
  // All edit/changes are done in Backup file
  public boolean createPDFBackup(String pdfName)
  {
    Log.debugLog(className, "createPDFBackup", "", "", "PDF Name = " +  pdfName);
    String pdfFileName = "";

    try
    {
      pdfFileName = getPDFPath() + pdfName + PDF_FILE_EXTN;
      File pdfFileObj = new File(pdfFileName);

      if(!pdfFileObj.exists())
      {
        Log.errorLog(className, "createPDFBackup", "", "", "Source pdf does not exist. PDF name = " + pdfName);
        return false;
      }

      String pdfBakFileName = getPDFPath() + getBAKPDFName(pdfName);

      if(copyPDFFile(pdfFileName, pdfBakFileName, "", "", "", "", false, false) == false)
      {
        Log.errorLog(className, "createPDFBackup", "", "", "Error in creating Bak of PDF. PDF file is " + pdfFileName + ". Backup file is " + pdfBakFileName);
        return false;
      }
      return(true);
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "createPDFBackup", "", "", "Exception in creating pdf backup (" + pdfFileName + ") - ", e);
      return false;
    }
  }

  // Copy source pdf file to pdf file.
  private boolean copyPDFFile(String srcFileName, String destFileName, String minGranule, String maxGranule, String pdfUnit, String pdfDesc, boolean updCreDate, boolean updModDate)
  {
    Log.debugLog(className, "copyPDFFile", "", "", "Method called, source file name = " + srcFileName + ", destination file name = " + destFileName + ", Min Granule = " + minGranule + ", Max Granule = " + maxGranule + ", PDF Unit = " + pdfUnit + ", PDF Description" + pdfDesc);

    try
    {
      File fileSrc = new File(srcFileName);
      File fileDest = new File(destFileName);

      Vector vecSourceFileData = readFile(srcFileName);
      ArrayList arrListSlabId = new ArrayList();
      boolean lastModDateFlag = false;
      boolean maxSlabId = false;

      if(!fileSrc.exists())
      {
        Log.errorLog(className, "copyPDFFile", "", "", "Source file does not exists. Filename = " + srcFileName);
        return false;
      }

      if(fileDest.exists())
        fileDest.delete();
      fileDest.createNewFile();

      if(rptUtilsBean.changeFilePerm(fileDest.getAbsolutePath(), "netstorm", "netstorm", "775") == false)
        return false;

      // To check pdf is compatible with current software or not
      String dataLine = "";
      for(int i = 0; i < vecSourceFileData.size(); i++)
      {
        dataLine = vecSourceFileData.elementAt(i).toString();

        if(dataLine.startsWith(LAST_MODIFIED_DATE))
          lastModDateFlag = true;
        else if(dataLine.startsWith("#LAST_MAX_ASSIGNED_SLAB_ID"))
          maxSlabId = true;
        else if(dataLine.startsWith("Slab"))
        {
          String[] arrTemp = rptUtilsBean.strToArrayData(dataLine, "|");

          arrListSlabId.add(arrTemp[SLAB_ID_INDEX]);
          Log.debugLog(className, "copyPDFFile", "", "", "Adding graph id in arraylist = " + arrTemp[SLAB_ID_INDEX]);
        }
      }
      FileInputStream fin = new FileInputStream(fileSrc);
      BufferedReader br = new BufferedReader(new InputStreamReader(fin));

      FileOutputStream fout = new FileOutputStream(fileDest, true);  // append mode
      PrintStream pw = new PrintStream(fout);

      if(!lastModDateFlag && !maxSlabId)
      {
        if(arrListSlabId.size() > 0)
        {
          int[] arrStrTmp = new int[arrListSlabId.size()];
          for(int i = 0; i < arrListSlabId.size(); i++)
            arrStrTmp[i] = Integer.parseInt(arrListSlabId.get(i).toString());

          Arrays.sort(arrStrTmp);

          int maxAssignedSlabId = arrStrTmp[arrStrTmp.length - 1];
          pw.println("#LAST_MAX_ASSIGNED_SLAB_ID = " + maxAssignedSlabId);
        }
        else
        {
          pw.println(LAST_MAX_ASSIGNED_SLAB_ID);
        }
        pw.println(LAST_MODIFIED_DATE + rptUtilsBean.getCurDateTime());
      }

      String str;
      while((str = br.readLine()) != null)
      {
        if(str.startsWith(LAST_MODIFIED_DATE))
        {
          if(updModDate)
            str = LAST_MODIFIED_DATE + rptUtilsBean.getCurDateTime();
        }
        if(str.startsWith("PDF"))
        {
          if(!pdfDesc.equals(""))
          {
            String[] arrTemp = rptUtilsBean.strToArrayData(str, "|");
            //PDF|PDF_Id|Min_Granule|Max_Granule|numSlabs|PDF_UNIT|PDF_Description
            str = arrTemp[0] + "|" + arrTemp[1] + "|" + minGranule + "|" + maxGranule + "|" + arrTemp[4] + "|" + pdfUnit + "|" + pdfDesc;
          }
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
      Log.stackTraceLog(className, "copyPDFFile", "", "", "Exception - ", e);
      return false;
    }
  }

  // This will delete selected Slabs from a pdf
  // selected graphs are passed as array of index.
  public boolean deleteSlabFromPDF(String pdfName, String[] arrSlabIdx)
  {
    Log.debugLog(className, "deleteSlabFromPDF", "", "", "PDF Name = " + pdfName);

    String pdfNameWithPath = getPDFPath() + getBAKPDFName(pdfName);

    try
    {
      Vector vecData = readFile(pdfNameWithPath);
      Vector vecSlabData = readSlabData(vecData);

      File pdfFileObj = new File(pdfNameWithPath);

      if(pdfFileObj.exists())
        pdfFileObj.delete(); // Delete pdf file

      pdfFileObj.createNewFile(); // Create new pdf file

      if(rptUtilsBean.changeFilePerm(pdfFileObj.getAbsolutePath(), "netstorm", "netstorm", "775") == false)
        return false;

      FileOutputStream fout = new FileOutputStream(pdfFileObj, true);  // append mode
      PrintStream pw = new PrintStream(fout);
      String status = "";

      String dataLine = "";
      for(int i = 0; i < vecData.size(); i++)
      {
        dataLine = vecData.elementAt(i).toString();

        if(dataLine.startsWith("Slab"))
          continue;

        if(dataLine.startsWith(LAST_MODIFIED_DATE))
        {
          dataLine = LAST_MODIFIED_DATE + rptUtilsBean.getCurDateTime();
          pw.println(dataLine);
        }
        else if(dataLine.startsWith("PDF"))
        {
          String updatedPDFLine = updateSlabLine(dataLine, (vecSlabData.size() - arrSlabIdx.length));
          pw.println(updatedPDFLine);
        }
        else
          pw.println(dataLine);
      }

      // for Slab
      for(int i = 0; i < vecSlabData.size(); i++)
      {
        status = "";
        for(int k = 0; k < arrSlabIdx.length; k++)
        {
          if(i == ((int)Integer.parseInt(arrSlabIdx[k])))
          {
            status = "true";
            break;
          }
        }

        if(!status.equals("true"))
          pw.println(vecSlabData.elementAt(i).toString());
      }

      pw.close();
      fout.close();

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "deleteSlabFromPDF", "", "", "Exception in deleting Slab from pdf of (" + pdfNameWithPath + ") - ", e);
      return false;
    }
  }

  /**
   * To add slab in PDF.
   * @param pdfName
   * @param slabDetail
   * @param before : to add slab after or before the selected slab in GUI, by default it is false. we assume value of this flag true
   *                 for adding slab before, and false for adding slab after the selected slab.
   * @param reSequence : true - do resequence of all the ID of slabs in PDF.
   * @param rowId : id of the selected slab. when we click simply on add button is will be -1.
   * @return
   */
  public boolean addSlabToPDF(String pdfName, String slabDetail, boolean before, boolean reSequence, String rowId)
  {
    Log.debugLog(className, "addSlabToPDF", "", "", "PDF Name = " + pdfName + ", row id = " + rowId);
    return(addSlabToPDF(pdfName, slabDetail, before, reSequence, rowId, false));
  }

  // Add slab to pdf file. Add is done to backup file only
  public boolean addSlabToPDF(String pdfName, String slabDetail, boolean before, boolean reSequence, String rowId, boolean fileType)
  {
    Log.debugLog(className, "addSlabToPDF", "", "", "PDF Name =" + pdfName + ", slab detail = " + slabDetail);

    String pdfNameWithPath = "";
    boolean isSlabAdded = false;
    if(fileType)
      pdfNameWithPath = getPDFPath() + pdfName + PDF_FILE_EXTN;
    else
      pdfNameWithPath = getPDFPath() + getBAKPDFName(pdfName);

    try
    {
      // Read file's content
      Vector vecRtpl = readFile(pdfNameWithPath);

      File pdfFileObj = new File(pdfNameWithPath);

      pdfFileObj.delete(); // Delete pdf file
      pdfFileObj.createNewFile(); // Create new pdf file

      if(rptUtilsBean.changeFilePerm(pdfFileObj.getAbsolutePath(), "netstorm", "netstorm", "775") == false)
        return false;

      if(!pdfFileObj.exists())
      {
        Log.errorLog(className, "addSlabToPDF", "", "", "PDF file does not exist. PDF filename is - " + pdfNameWithPath);
        return(false);
      }

      FileOutputStream fout = new FileOutputStream(pdfFileObj, true);  // append mode
      PrintStream pw = new PrintStream(fout);

      slabDetail = getSlabDetail(slabDetail, vecRtpl);

      String dataLine = "";
      String slabId = "";

      for(int i = 0; i < vecRtpl.size(); i++)
      {
        dataLine = vecRtpl.get(i).toString();
        if(dataLine.startsWith("#LAST_MAX_ASSIGNED_SLAB_ID"))
        {
          String[] arrDataTemp = rptUtilsBean.strToArrayData(dataLine, "=");
          slabId = "" + (Integer.parseInt(arrDataTemp[1].trim()) + 1);

          pw.println("#LAST_MAX_ASSIGNED_SLAB_ID = " + slabId);
        }
        else if(dataLine.startsWith(LAST_MODIFIED_DATE))
        {
          dataLine = LAST_MODIFIED_DATE + rptUtilsBean.getCurDateTime();
          pw.println(dataLine);
        }
        else if(dataLine.startsWith("Info"))
        {
          pw.println(dataLine);
        }
        else if(dataLine.startsWith("PDF"))
        {
          String[] arrSlabDataTemp = rptUtilsBean.strToArrayData(dataLine, "|");
          int updatedSlabCount = (Integer.parseInt(arrSlabDataTemp[SLAB_COUNT_INDEX]) + 1);

          String updatedPDFLine = updateSlabLine(dataLine, updatedSlabCount);
          pw.println(updatedPDFLine);
        }
        else
        {
          if(!rowId.equals("-1"))    //if row id is not -1 then either graph is to be added before or after from the selected graph, this will be desided on the basis of before flag.
          {
            String[] arrSlabDataTemp = rptUtilsBean.strToArrayData(dataLine, "|");

            if(arrSlabDataTemp[SLAB_ID_INDEX].equals(rowId + ""))
            {
              if(!before)  //we have to add a new slab after the selected slab
              {
                pw.println(dataLine);
                pw.println(slabDetail);  // Append the new slab after to the selected slab.

                Log.debugLog(className, "addSlabToPDF", "", "", "adding new slab after slab id = " + rowId + ". New slab detail = " + slabDetail);
                isSlabAdded = true;
              }
              else  //we have to add a new graph before the selected graph
              {
                pw.println(slabDetail);  // Append the new slab before than the selected slab.

                Log.debugLog(className, "addSlabToPDF", "", "", "adding new slab before slab id = " + rowId + ". New slab detail = " + slabDetail);
                isSlabAdded = true;

                pw.println(dataLine);
              }
            }
            else
              pw.println(dataLine);
          }
          else   //if Add button is clicked from GUI, then it simply add new slab in the end of the PDF.
          {
            pw.println(dataLine);
          }
        }
      }

        if(!isSlabAdded)
          pw.println(slabDetail);  // Append the new slab to the end of pdf

        pw.close();
        fout.close();

        if(reSequence)
        {
          Log.debugLog(className, "addGraphToGDF", "", "", "resequencing all graph id.");
          if(!reSequenceSlabId(pdfNameWithPath))
          {
            Log.debugLog(className, "addGraphToGDF", "", "", "Error in resequencing graph id in gdf.");
            return false;
          }
        }

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "addSlabToPDF", "", "", "Exception in adding Slab to pdf (" + pdfNameWithPath + ") - ", e);
      return false;
    }
  }

  /**
   * To get graph line, that is to be added for new graph
   * @param graphDetail
   * @param graphId
   * @param gdfName
   * @return
   */
  private String getSlabDetail(String slabDetail, Vector vecRtpl)
  {
    Log.debugLog(className, "getSlabDetail", "", "", "Method Called. slab detail = " + slabDetail);
    String[] arrSlabDataTemp = rptUtilsBean.strToArrayData(slabDetail, "|");

    if(arrSlabDataTemp == null || arrSlabDataTemp.length != 3)
    {
      Log.errorLog(className, "addSlabToPDF", "", "", "Slab data line has insufficient data = " + slabDetail);
      return "";   //handel this condition from addSlabToPdf.
    }
    //Slab|SlabName|slabID|Min_Value|Max_value|color
    slabDetail = "Slab|" + arrSlabDataTemp[0] + "|" + getUniqueSlabId(vecRtpl) + "|" + arrSlabDataTemp[1] + "|" + arrSlabDataTemp[2] + "|-";

    return slabDetail;
  }

  /**
   * To resequence all the slab id in the PDF.
   * @param pdfNameWithPath
   * @return
   */
  private boolean reSequenceSlabId(String pdfNameWithPath)
  {
    Log.debugLog(className, "reSequenceSlabId", "", "", "Method Called. pdf name = " + pdfNameWithPath);

    try
    {
      // Read file's content
      Vector vecData = readFile(pdfNameWithPath);
      Vector vecResequenceData = new Vector();
      int slabIdCounter = 1;

      File pdfFileObj = new File(pdfNameWithPath);

      pdfFileObj.delete(); // Delete pdf file
      pdfFileObj.createNewFile(); // Create new pdf file

      if(rptUtilsBean.changeFilePerm(pdfFileObj.getAbsolutePath(), "netstorm", "netstorm", "775") == false)
        return false;

      if(!pdfFileObj.exists())
      {
        Log.errorLog(className, "reSequenceSlabId", "", "", "PDF file does not exist. PDF filename is - " + pdfNameWithPath);
        return false;
      }

      String dataLine = "";

      for(int i = 0; i < vecData.size(); i++)
      {
        dataLine = vecData.get(i).toString();

        if(dataLine.startsWith("Slab")) //serching for graph id and keep adding them in an ArrayList.
        {
          String[] arrDataTemp = rptUtilsBean.strToArrayData(dataLine, "|");
          arrDataTemp[SLAB_ID_INDEX] = "" + slabIdCounter; // reassign slab Id

          vecResequenceData.add(rptUtilsBean.strArrayToStr(arrDataTemp, "|"));
          slabIdCounter++;
        }
      }

      FileOutputStream fout = new FileOutputStream(pdfFileObj, true);  // append mode
      PrintStream pw = new PrintStream(fout);

      for(int i = 0; i < vecData.size(); i++)
      {
        String fileDataLine = vecData.get(i).toString().trim();

        if(fileDataLine.startsWith("#LAST_MAX_ASSIGNED_SLAB_ID"))
        {
          String[] arrDataTemp = rptUtilsBean.strToArrayData(fileDataLine, "=");
          pw.println("#LAST_MAX_ASSIGNED_SLAB_ID = " + (slabIdCounter - 1));
        }
        else if(!fileDataLine.startsWith("Slab"))
        {
          pw.println(fileDataLine);
          Log.debugLog(className, "reSequenceSlabId", "", "", "Adding fileDataLine in pdf = " + fileDataLine);
        }
      }

      // add updated (resequenced) slab id in file
      for(int i = 0; i < vecResequenceData.size(); i++)
      {
        String fileDataLine = vecResequenceData.get(i).toString().trim();

        pw.println(fileDataLine);
        Log.debugLog(className, "reSequenceSlabId", "", "", "Adding updated graph line in gdf = " + fileDataLine);
      }

      pw.close();
      fout.close();
      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "reSequenceSlabId", "", "", "Exception = ", e);
      return false;
    }
  }


  public boolean createNewPDF(String pdfName, String minGranule, String maxGranule, String pdfUnit, String pdfDesc)
  {
    Log.debugLog(className, "createNewPDF", "", "", "PDF Name =" + pdfName + ", minGranule = " + minGranule + ", maxGranule = " + maxGranule + ", pdfUnit = " + pdfUnit + ", pdfDesc = " + pdfDesc);
    return(createNewPDF(pdfName, minGranule, maxGranule, pdfUnit, pdfDesc, false));
  }


  // Create new pdf. New pdf is always create as bak file.
  public boolean createNewPDF(String pdfName, String minGranule, String maxGranule, String pdfUnit, String pdfDesc, boolean checkFileType)
  {
    Log.debugLog(className, "createNewPDF", "", "", "PDF Name =" + pdfName + ", minGranule = " + minGranule + ", maxGranule = " + maxGranule + ", pdfUnit = " + pdfUnit + ", pdfDesc = " + pdfDesc);
    String pdfNameWithPath = "";

    try
    {
      // This will allow user to create pdf file with .pdf extn.
      if(checkFileType == true)
        pdfNameWithPath = getPDFPath() + pdfName + PDF_FILE_EXTN;
      else
        pdfNameWithPath = getPDFPath() + getBAKPDFName(pdfName);

      File pdfFileObj = new File(pdfNameWithPath);

      if(pdfFileObj.exists())
      {
        Log.errorLog(className, "createNewPDF", "", "", "PDF already exists. PDF name = " + pdfName);
        return false;
      }

      pdfFileObj.createNewFile();

      if(rptUtilsBean.changeFilePerm(pdfFileObj.getAbsolutePath(), "netstorm", "netstorm", "775") == false)
        return false;

      FileOutputStream fout = new FileOutputStream(pdfFileObj, true);  // append mode
      PrintStream pw = new PrintStream(fout);

      pw.println(LAST_MAX_ASSIGNED_SLAB_ID);
      pw.println(LAST_MODIFIED_DATE + rptUtilsBean.getCurDateTime());
      pw.println(PDF_INFO_HDR_LINE);

      pw.println(getPDFInfoLine(pdfName, minGranule, maxGranule, pdfUnit, pdfDesc));

      pw.close();
      fout.close();
      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "createNewPDF", "", "", "Exception in creating new pdf (" + pdfName + ") - ", e);
      return false;
    }
  }


  // Save pdf will delete pdf file and copy bak file to pdf file
  // Bak is not deleted as GUI needs this file. Mod date/time is also updated in pdf file.
  public boolean savePDF(String pdfName, String minGranule, String maxGranule, String pdfUnit, String pdfDesc)
  {
    Log.debugLog(className, "savePDF", "", "", "PDF File =" + pdfName + ", minGranule = " + minGranule + ", maxGranule = " + maxGranule + ", pdfUnit = " + pdfUnit + ", pdfDesc = " + pdfDesc);

    try
    {
      // First check if Bak file is existing or not. If not create new pdf Bak file
      String pdfBakFileName = getPDFPath() + getBAKPDFName(pdfName);

      File pdfBakFileObj = new File(pdfBakFileName);

      if(!pdfBakFileObj.exists())
        createNewPDF(pdfName, minGranule, maxGranule, pdfUnit, pdfDesc);
      else
      {
        Vector vecData = readFile(pdfBakFileName);
      }

      // Now copy Bak file to pdf file to save the changes
      String pdfFileName = getPDFPath() + pdfName + PDF_FILE_EXTN;

      if(copyPDFFile(pdfBakFileName, pdfFileName, minGranule, maxGranule, pdfUnit, pdfDesc, false, true) == false)
      {
        Log.errorLog(className, "savePDF", "", "", "Error in copying Bak to pdf (" + pdfFileName + ")");
        return false;
      }

      // Now copy pdf to Bak file so that Bak is same as pdf file
      if(copyPDFFile(pdfFileName, pdfBakFileName, "", "", "", "", false, false) == false)
      {
        Log.errorLog(className, "savePDF", "", "", "Error in copying pdf to Bak (" + pdfFileName + ")");
        return false;
      }

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "savePDF", "", "", "Exception in saving pdf (" + pdfName + ") - ", e);
      return false;
    }
  }


  // To check if pdf is modified or not after last save was done
  public boolean isPDFModified(String pdfName)
  {
    Log.debugLog(className, "isPDFModified", "", "", "Method called. PDF Name = " + pdfName);
    try
    {
      String pdfFileName = getPDFPath() + pdfName + PDF_FILE_EXTN;
      String pdfBakFileName = getPDFPath() + getBAKPDFName(pdfName);

      Vector vecPDFData = readFile(pdfFileName);
      Vector vecBakPDFData = readFile(pdfBakFileName);

      String strPDFDate = "";
      String strBakPDFDate = "";

      String dataLine = "";
      for(int i = 0; i < vecPDFData.size(); i++)
      {
        dataLine = vecPDFData.get(i).toString();

        if(dataLine.startsWith(LAST_MODIFIED_DATE))
        {
          String[] arrTemp = rptUtilsBean.strToArrayData(dataLine, "=");
          strPDFDate = arrTemp[1].trim();
          break;
        }
      }

      for(int i = 0; i < vecBakPDFData.size(); i++)
      {
        dataLine = vecBakPDFData.get(i).toString();

        if(dataLine.startsWith(LAST_MODIFIED_DATE))
        {
          String[] arrTemp = rptUtilsBean.strToArrayData(dataLine, "=");
          strBakPDFDate = arrTemp[1].trim();
          break;
        }
      }

      Log.debugLog(className, "isPDFModified", "", "", "Modification Date/Time for PDF and Bak file are - " + strPDFDate + " and " + strBakPDFDate);

      // Compare modified file
      if((rptUtilsBean.convertDateToMilliSec(strPDFDate)) == (rptUtilsBean.convertDateToMilliSec(strBakPDFDate)))
        return false;

      return true;

    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "isPDFModified", "", "", "Exception - ", e);
      return false;
    }
  }

  // Make copy of a pdf with new name
  // Also creation date and modification date is new pdf is set to current date/time
  public boolean copyPDF(String strSrcPDFName, String strDestPDFName, String destDesc)
  {
    Log.debugLog(className, "copyPDF", "", "", "SrcName =" +  strSrcPDFName + ", DestName = " + strDestPDFName + " Description = " + destDesc);

    try
    {
      String pdfSrcFileName = getPDFPath() + strSrcPDFName + PDF_FILE_EXTN;
      String pdfDestFileName = getPDFPath() + strDestPDFName + PDF_FILE_EXTN;

      File pdfSrcFileObj = new File(pdfSrcFileName);
      if(!pdfSrcFileObj.exists())
      {
        Log.errorLog(className, "copyPDF", "", "", "Source pdf does not exist. PDF name = " + strSrcPDFName);
        return false;
      }

      File pdfDestFileObj = new File(pdfDestFileName);
      if(pdfDestFileObj.exists())
      {
        Log.errorLog(className, "copyPDF", "", "", "Destination pdf already exists. PDF name = " + pdfDestFileName);
        return false;
      }

      FileInputStream fin = new FileInputStream(pdfSrcFileObj);
      BufferedReader br = new BufferedReader(new InputStreamReader(fin));

      FileOutputStream fout = new FileOutputStream(pdfDestFileObj, true);  // append mode
      PrintStream pw = new PrintStream(fout);

      String dataLine = "";
      while((dataLine = br.readLine()) != null)
      {
        if(dataLine.startsWith(LAST_MODIFIED_DATE))
        {
          dataLine = LAST_MODIFIED_DATE + rptUtilsBean.getCurDateTime();
        }
        if(dataLine.startsWith("PDF"))
        {
          String[] arrTemp = rptUtilsBean.strToArrayData(dataLine, "|");
          dataLine = "PDF" + "|" + getUniquePDFId() + "|" + arrTemp[MIN_GRANULE_INDEX] + "|" + arrTemp[MAX_GRANULE_INDEX] + "|" + arrTemp[SLAB_COUNT_INDEX] + "|" + arrTemp[PDF_UNIT_INDEX] + "|" + destDesc;
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
      Log.stackTraceLog(className, "copyPDF", "", "", "Exception in copying pdf (" + strSrcPDFName + ") - ", e);
      return false;
    }
  }

  /**
   * To update slab in the pdf.
   * @param pdfName : name of the pdf.
   * @param id : id of the slab, that is to be updated
   * @param dataLine : passed through jsp, format of the line would be slabName|slabMinValue|slabMaxValue.
   * @return
   */
  public boolean updatePDF(String pdfName, String id, String dataLine)
  {
    Log.debugLog(className, "updatePDF", "", "", "pdf name = " + pdfName + ", slab id = " + id + ", data line = " + dataLine);

    String pdfNameWithPath = getPDFPath() + getBAKPDFName(pdfName);

    Vector vecData = readFile(pdfNameWithPath);

    File pdfFileObj = new File(pdfNameWithPath);

    try
    {
      if(!pdfFileObj.exists())
      {
        Log.debugLog(className, "updateGDF", "", "", "gdf name = " + pdfNameWithPath + " does not exist.");
        return false;
      }
      else
      {
        pdfFileObj.delete();
        pdfFileObj.createNewFile();
      }

      if(rptUtilsBean.changeFilePerm(pdfFileObj.getAbsolutePath(), "netstorm", "netstorm", "775") == false)
        return false;

      FileOutputStream fout = new FileOutputStream(pdfFileObj, true);  // append mode
      PrintStream pw = new PrintStream(fout);

      for(int i = 0; i < vecData.size(); i++)
      {
        if(vecData.elementAt(i).toString().startsWith("Slab"))
        {
          String[] arrTemp = rptUtilsBean.strToArrayData(vecData.elementAt(i).toString(), "|");
          String tempID = arrTemp[SLAB_ID_INDEX].trim();

          if(tempID.equals(id))
          {
            String[] arrSlabDataTemp = rptUtilsBean.strToArrayData(dataLine, "|");

            String slabDetail = "Slab|" + arrSlabDataTemp[0] + "|" + id + "|" + arrSlabDataTemp[1] + "|" + arrSlabDataTemp[2] + "|-";
            Log.debugLog(className, "updatePDF", "", "", "writing updated line in file = " + slabDetail);
            pw.println(slabDetail);
          }
          else
          {
            Log.debugLog(className, "updatePDF", "", "", "writing line in file = " + vecData.elementAt(i).toString());
            pw.println(vecData.elementAt(i).toString());
          }
        }
        else
        {
          Log.debugLog(className, "updatePDF", "", "", "writing line in file = " + vecData.elementAt(i).toString());
          pw.println(vecData.elementAt(i).toString());
        }
      }
      pw.close();
      fout.close();
      return true;
    }catch(Exception e)
    {
      Log.stackTraceLog(className, "updatePDF", "", "", "Exception = ", e);
      return false;
    }
  }

  /**
   * This method return array of percentile data
   * @param rawValue
   * @param pctCalSize
   * @return
   */
  public double[] getPercentile(double[] rawValue, int pctCalSize)
  {
    Log.debugLog(className, "getPercentile", "", "", "pctCalSize = " + pctCalSize);
    double max = 0.0; /* Maximum value in the provided input array */
    int first_non_zero_index = 0;

    /* The factor with which all the values will be divided
     * due to max value greater than the value supported by 
     * this function, limited by MAX_PCT_ARR_SIZE macro 
     */
    double scale = 1;
    int MAX_PCT_ARR_SIZE = 1048576;
    /* This array is used to hold the count
     * the input data with same values. The
     * index of this array is the value.
     */
    int[] pct_arr = new int[MAX_PCT_ARR_SIZE];
    double[] out_pct = new double[101];

    for(int ii = 0; ii < pct_arr.length; ii++)
    {
      pct_arr[ii] = 0;
    }
    
    /* Compute the max value in the data set */
    for(int i = 0; i < pctCalSize; i++)
    {
      if(rawValue[i] > max)
        max = rawValue[i];
    }

    Log.debugLog(className, "getPercentile", "", "", "Max value from dataset = " + max);
    if(max == 0.0)
    {
      Log.debugLog(className, "getPercentile", "", "", "Max value found = " + max + " so we return 0.0 data on all indexes");
      for(int ii = 0; ii < out_pct.length; ii++)
      {
        out_pct[ii] = 0.0;
      }
      return out_pct;//return all data values 0.0
    }

    /* In case the max value is greater than the supported max value (i.e.
     * the size of pct_arr, then we shall scale down all the values. Compute
     * the scale factor in such case 
     */
    if(max > MAX_PCT_ARR_SIZE)
      scale = max / MAX_PCT_ARR_SIZE + 1;

    /* Scale down the max value */
    double scaled_max = max / scale;

    Log.debugLog(className, "getPercentile", "", "", "Max scale value from dataset = " + scaled_max);
    
    /* This is used to multiply the data if the values (as determined from max)
     * are small, then the decimal points vcan be entertained. The smaller the 
     * order of values, the more the decimal point digits can be entertained.
     * Later on when percentiles are computed, the values need to be divided 
     * by this number.
     */
    int divider = 1;
    /* Compute the divider factor. The smaller the max, the more the divider */
    while(scaled_max <= 99999)
    {
      scaled_max *= 10;
      divider *= 10;
    }

    /* Now scale all the values using scale and divider factors, typecast to int
     * use this number as index and increment the pc_arr at that index
     * This way, populate the whole pct_arr.
     */
    for(int i = 0; i < pctCalSize; i++)
      pct_arr[(int)((rawValue[i] / scale) * divider)]++;

    while(pct_arr[first_non_zero_index] == 0)
    {
    	first_non_zero_index++;
    }


    int pct = 1; /* This is the percentile number, 1 for 1st, 2 for 2nd and so on */

    /* cmp_count is the compare count. The count of pct_arr starting from index 0 will be added to a 
     * cumulative countr (cum_count) and as soon as the cum_count exceeds the compare count for
     * particular pct'th percentile, the index of the pct_arr represents the scale pct'th percentile
     */
    double cmp_count = (((double) pctCalSize - 1) * (double) pct) / (double) 100 + 1;
    int cmp_count_int = (int) cmp_count; // For Interpolation 
    double cmp_count_fraction = cmp_count - (double) cmp_count_int; // For Interpolation
    int cum_count = 0;

    int int_scaled_max = (int)scaled_max;/* Typecast */
    /* Compute all the percentiles */
    for(int i = first_non_zero_index; i <= int_scaled_max; i++)
    {
      cum_count += pct_arr[i];
      while(cum_count >= cmp_count_int && pct <= 100)
      {
        out_pct[pct] = (((double)i) * scale) / ((double)divider);
        /* Linear interpolation - Begin */
        int cur_idx = i + 1;
        if(cum_count == cmp_count_int && cmp_count_fraction > 0)
        {
          while((pct_arr[cur_idx] == 0) && (cur_idx <= int_scaled_max)) cur_idx++;
          out_pct[pct] += (((double)(cur_idx - i) * cmp_count_fraction * scale )/ (double) divider);
        }
        /* Linear interpolation - End */
        pct++;
        cmp_count = (((double) pctCalSize - 1) * (double) pct) / (double) 100 + 1;
        cmp_count_int = (int) cmp_count; // For Interpolation
        cmp_count_fraction = cmp_count - (double) cmp_count_int;  // For Interpolation
      }
    }
    return out_pct;
  }
  public static void main(String[] args)
  {
    PercentileBean pdfBean = new PercentileBean();
    boolean resultFlag;

    int choice = 0;

    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    System.out.println("********Please enter the option for desired operation*********");
    System.out.println("For list of all pdf(s) Enter : 1");
    System.out.println("For slab details of all pdf(s) Enter : 2");
    System.out.println("To Create New PDF (bak file) Enter : 3");
    System.out.println("To Save PDF (Save bak to pdf file) Enter : 4");
    System.out.println("To add slab to pdf Enter : 5");
    System.out.println("To Delete slab from PDF Enter : 6");
    System.out.println("To check that PDF is modified from previous change or not Enter: 7");
    System.out.println("To Copy PDF Enter : 8");
    System.out.println("To get details of all PDF Enter : 9");
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
        // Give the list of all gdf(s)
        String[] strArray = pdfBean.getAllPDF();
        System.out.println("strArray.length = " + strArray.length);
        for(int i = 0; i < strArray.length; i++)
        {
          System.out.print("PDF Name = " + strArray[i]);
          System.out.println();
        }
        break;

      case 2:
        String pdfName = null;
        System.out.println("Enter the PDF Name : ");
        try
        {
          pdfName = br.readLine();
        }
        catch(IOException e)
        {
          System.out.println("Error in entered argument : " + e);
        }
        // Give the slab details of all pdf(s)
        String[][] arrCustomPDFDetails = pdfBean.getPDFDetails(pdfName);
        System.out.println("arrCustomPDFDetails.length = " + arrCustomPDFDetails.length);
        for(int i = 0; i < arrCustomPDFDetails.length; i++)
        {
          System.out.print("Name = " + arrCustomPDFDetails[i][0] + ", ID = " + arrCustomPDFDetails[i][1] + ", Min Value = " + arrCustomPDFDetails[i][2] + ", Max value = " + arrCustomPDFDetails[i][3] + ", Number of Slab = " + arrCustomPDFDetails[i][4] + ", PDF Unit = " + arrCustomPDFDetails[i][5] + ", PDF Description = " + arrCustomPDFDetails[i][6]);
           System.out.println();
        }
        break;

      case 3:
        String pdfNameToBeCreated = null;
        String minGranule = null, maxGranule = null, pdfUnit = null, pdfDesc = null;
        try
        {
          System.out.println("Enter the PDF Name : ");
          pdfNameToBeCreated = br.readLine();
        }
        catch(IOException e)
        {
          System.out.println("Error in entered argument : " + e);
        }
        try
        {
          System.out.println("Enter the minGranule : ");
          minGranule = br.readLine();
        }
        catch(IOException e)
        {
          System.out.println("Error in entered argument : " + e);
        }
        try
        {
          System.out.println("Enter the maxGranule : ");
          maxGranule = br.readLine();
        }
        catch(IOException e)
        {
          System.out.println("Error in entered argument : " + e);
        }
        try
        {
          System.out.println("Enter the pdfUnit : ");
          pdfUnit = br.readLine();
        }
        catch(IOException e)
        {
          System.out.println("Error in entered argument : " + e);
        }
        try
        {
          System.out.println("Enter the pdfDesc : ");
          pdfDesc = br.readLine();
        }
        catch(IOException e)
        {
          System.out.println("Error in entered argument : " + e);
        }
        // Create New PDF (bak file)
        //String pdfName, String minGranule, String maxGranule, String pdfUnit, String pdfDesc
        resultFlag = pdfBean.createNewPDF(pdfNameToBeCreated, minGranule, maxGranule, pdfUnit, pdfDesc);
        System.out.println("Result = " + resultFlag);
        break;

      case 4:
        String pdfNameToBeSaved = null, minGranule1 = null, maxGranule1 = null, pdfUnit1 = null, pdfDesc1 = null;
        try
        {
          System.out.println("Enter the PDF Name : ");
          pdfNameToBeSaved = br.readLine();
        }
        catch(IOException e)
        {
          System.out.println("Error in entered arguments : " + e);
        }
        try
        {
          System.out.println("Enter the minGranule : ");
          minGranule1 = br.readLine();
        }
        catch(IOException e)
        {
          System.out.println("Error in entered argument : " + e);
        }
        try
        {
          System.out.println("Enter the maxGranule : ");
          maxGranule1 = br.readLine();
        }
        catch(IOException e)
        {
          System.out.println("Error in entered argument : " + e);
        }
        try
        {
          System.out.println("Enter the pdfUnit : ");
          pdfUnit1 = br.readLine();
        }
        catch(IOException e)
        {
          System.out.println("Error in entered argument : " + e);
        }
        try
        {
          System.out.println("Enter the pdfDesc : ");
          pdfDesc1 = br.readLine();
        }
        catch(IOException e)
        {
          System.out.println("Error in entered argument : " + e);
        }
        // Save PDF (Save bak to pdf file)
        resultFlag = pdfBean.savePDF(pdfNameToBeSaved, minGranule1, maxGranule1, pdfUnit1, pdfDesc1);
        System.out.println("Result = " + resultFlag);
        break;

      case 5:
        String pdfNameToBeEdited = null;
        String slabNameToBeAdded = null;
        String rowId = "-1";
        try
        {
          System.out.println("Enter the PDF Name : ");
          pdfNameToBeEdited = br.readLine();

          System.out.println("Enter the Slab Name : ");
          slabNameToBeAdded = br.readLine();

          System.out.println("Enter the Slab's min value : ");
          slabNameToBeAdded = slabNameToBeAdded + "|" + br.readLine();

          System.out.println("Enter the Slab's max value : ");
          slabNameToBeAdded = slabNameToBeAdded + "|" + br.readLine();

          System.out.println("Enter the row ID : ");
          rowId = br.readLine();
        }
        catch(IOException e)
        {
          System.out.println("Error in entered arguments : " + e);
        }
        // add data to gdf
        resultFlag = pdfBean.addSlabToPDF(pdfNameToBeEdited, slabNameToBeAdded, true, true, "-1");
        System.out.println("Result = " + resultFlag);
        break;

      case 6:
        String pdfNameToBeModified = null;
        int numberOfSlabs = 0;
        String[] strSlab = null;
        try
        {
          System.out.print("Enter pdf Name : ");
          pdfNameToBeModified = br.readLine();

          System.out.println("Enter how many slabs you want to delete : ");
          numberOfSlabs = Integer.parseInt(br.readLine());

          strSlab = new String[numberOfSlabs];
          for(int i = 0; i < numberOfSlabs; i++)
          {
            System.out.println("Enter " + i + "th id : ");
            strSlab[i] = br.readLine();
          }
        }
        catch(IOException e)
        {
          System.out.println("Error in entered string : " + e);
        }

        // delete slab from pdf
        resultFlag = pdfBean.deleteSlabFromPDF(pdfNameToBeModified, strSlab);
        System.out.println("Result = " + resultFlag);
        break;

      case 7:
        String pdfname = null;
        try
        {
          System.out.println("Enter pdf Name : ");
          pdfname = br.readLine();
        }
        catch(IOException e)
        {
          System.out.println("Error in entered argument : " + e);
        }
        // check if pdf file is modified from last change or not.
        resultFlag = pdfBean.isPDFModified(pdfname);
        System.out.println("Result = " + resultFlag);
        break;

      case 8:
        String srcPDFName = null;
        String destPDFName = null;
        String destPDFDesc = null;
        try
        {
          System.out.println("Enter Source PDF Name : ");
          srcPDFName = br.readLine();

          System.out.println("Enter Destination PDF Name : ");
          destPDFName = br.readLine();

          System.out.println("Enter Destination PDF Description : ");
          destPDFDesc = br.readLine();
        }
        catch(IOException e)
        {
          System.out.println("Error in entered string : " + e);
        }
        // copy pdf
        resultFlag = pdfBean.copyPDF(srcPDFName, destPDFName, destPDFDesc);
        System.out.println("Result = " + resultFlag);
        break;
      case 9:
        String[][] dataAllPDF = pdfBean.getPDFs();

        for(int i = 0; i < dataAllPDF.length; i++)
          for(int j = 0; j < dataAllPDF[i].length; j++)
            System.out.println("i = " + i + ", j = " + dataAllPDF[i][j]);
        break;

      default:
        System.out.println("Please select the correct option.");
    }
  }
}

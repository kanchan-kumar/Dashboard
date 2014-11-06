/*--------------------------------------------------------------------
@Name    : PDFNames.java
@Author  : Prabhat
@Purpose : To read "testRun.pdf" file in TRxxx
@Modification History:
    01/12/2009 --> Prabhat  -->  Initial Version

----------------------------------------------------------------------*/

package pac1.Bean;

import pac1.Bean.*;

import java.io.*;
import java.util.*;

public class PDFNames implements java.io.Serializable
{
  private final String className = "PDFNames";
  private String workPath = Config.getWorkPath();
  private final String PDF_FILE = "testrun";
  private final String PDF_FILE_EXTN = ".pdf";

  // General information line
  /*****               Info|msgVersion|numPDF|sizeOfPctMsgData          *****/
  // Indexes of Info Line
  private transient final int VERSION_INDEX = 1;
  private transient final int NUM_PDF_INDEX = 2; // Number of pdf index in info line
  private transient final int SIZE_OF_PCT_MSG_DATA_INDEX = 3; // Size of percentile message data index in info line
  private transient final int MODE_INDEX = 4; // Mode index 0(total run), 1(run phase only), 2(specified interval)
  private transient final int INTERVAL_INDEX = 5;

  private transient final int HEADER_INDEX = 0; // Header info index

  // Indexes of PDF Line
  private transient final int PDF_NAME_INDEX = 1;

  // Indexes of END Line
  private transient final int END_SEQ_INDEX = 1;

  private String testRun = "";
  private int numTestRun = -1;
  private Vector pdfVecData = new Vector();
  
  private transient Vector vecAllPDFData = new Vector(); // Vector to store all PDF file record
  private ArrayList arrListAllSortedPDF = null; // Array list of all PDF in sorted order

  private boolean forTemplate = false; // It is true for Report Template
  private transient boolean infoFlagFirstTime = false; // use for adding info line first time in vector
  private transient int numPDFs = 0; // total number of PDFs in all PDF files

  private int majorVersionNum = 0; // Use to store the major version of pdf
  private int minorVersionNum = 0; // Use to store the minor version of pdf
  private int totalPDFs = 0; // Use to store total number of pdf's
  private int sizeOfPctMsgData = 0; // Use to store size of percentile message data
  private int modeType = 0; // use to store mode (total run, run phase only, specified interval)
  private int interval = -1; // use to store mode (total run, run phase only, specified interval)
  private int endSequenceNum = -1; // use to store end sequence number

  public PDFInfo[] pdfInfo = null;

  // Set all the arrays.
  public PDFNames(int numTestRun)
  {
    this.numTestRun = numTestRun;
    this.testRun = "" + numTestRun;
    if(numTestRun == -1)
      forTemplate = true;

    initPDFNames();
  }

  public PDFNames(int numTestRun, Vector vecPdfData)
  {
    this.pdfVecData = vecPdfData;
    this.numTestRun = numTestRun;
    this.testRun = "" + numTestRun;
    if(numTestRun == -1)
      forTemplate = true;

    initPDFNames();
  }

  // This will return the Test Run path
  private String getTestRunPath(int numTestRun)
  {
    return (workPath + "/webapps/logs/TR" + numTestRun);
  }


  // This will return the PDF path
  private String getPDFPath()
  {
    return (workPath + "/pdf/");
  }


  // This Function Check that testrun.pdf is available in the TestRun or not
  public static boolean isPDFAvail(String testRun)
  {
    String pdfFileWithPath = Config.getWorkPath() + "/webapps/logs/TR" + testRun + "/testrun.pdf";

    File trPDF = new File(pdfFileWithPath);

    if(!trPDF.exists())
      return false;
    else
      return true;
  }


  // Copy this function to rptUtilsBean.java
  // Convert record from array to string through saperator
  public String[] strToArrayData(String tempRecord, String separator)
  {
    //Log.debugLog(className, "strToArrayData", "", "", "tempRecord =" + tempRecord);
    String[] tempArr = null;
    try
    {
      StringTokenizer stTemp = new StringTokenizer(tempRecord, separator);
      tempArr = new String[stTemp.countTokens()];

      int i = 0;
      while(stTemp.hasMoreTokens())
      {
        tempArr[i] = stTemp.nextToken();
        i++;
      }

      return tempArr;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "strToArrayData", "", "", "Exception in change from string to array -", e);
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
  private Vector readReport(String fileWithPath)
  {
    //Log.debugLog(className, "readReport", "", "", "Method called. PDF FIle Name = " + fileWithPath);

    try
    {
      Vector vecData = new Vector();
      String strLine;

      File fileObj = openFile(fileWithPath);

      if(!fileObj.exists())
      {
        Log.debugLog(className, "readReport", "", "", "Requested file not found - " + fileWithPath);
        return null;
      }

      FileInputStream fis = new FileInputStream(fileWithPath);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));

      while((strLine = br.readLine()) !=  null)
      {
        strLine = strLine.trim();
        if(strLine.startsWith("#"))
          continue;
        if(strLine.length() == 0)
          continue;

        //Log.debugLog(className, "readReport", "", "", "Adding line in vector. Line = " + strLine);
        vecData.add(strLine);
      }

      br.close();
      fis.close();

      return vecData;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "readReport", "", "", "Exception - ", e);
      return null;
    }
  }


  // Get testRun.pdf Report information
  private Vector getTestRunPDF()
  {
    Log.debugLog(className, "getTestRunPDF", "", "", "Method called");

    String pdfFileWithPath = workPath + "/webapps/logs/TR" + testRun + "/" + PDF_FILE + PDF_FILE_EXTN;

    return(readReport(pdfFileWithPath));
  }


  // load all pdf file in Array List
  private ArrayList loadAllPDF()
  {
    Log.debugLog(className, "loadAllPDF", "", "", "Start method");

    ArrayList arrListAllPDF = new ArrayList();  // Names of all PDF files
    ArrayList arrAllPDFSortedOrder = new ArrayList();  // to store names of all PDF files in sorted order

    String temp;
    String pdfPath;

    try
    {
      File pdfDirObj = new File(getPDFPath());

      if(pdfDirObj == null)
        return null;

      if(!pdfDirObj.exists())
      {
        Log.errorLog(className, "initPDFNames", "", "", "PDF directory path not exists = " + getPDFPath());
        return null;
      }

      pdfPath = getPDFPath();

      // Enhance this to use list(FilenameFilter filter) to get only *.pdf file
      String arrayFiles[] = pdfDirObj.list();

      // Calculate number of pdf files
      for(int j = 0; j < arrayFiles.length; j++)
      {
        temp = "";

        // Use lastIndexOf() function instead indexOf() beacuse reading extn.
        if(arrayFiles[j].lastIndexOf(PDF_FILE_EXTN) == -1)  // Skip non pdf files
          continue;

        String[] tempArr = strToArrayData(arrayFiles[j], "."); // to chk that file is .pdf or .pdf.hot

        if(tempArr.length == 2)
        {
          temp = pdfPath + arrayFiles[j];
          Log.debugLog(className, "loadAllPDF", "", "", "Adding PDF in temp ArrayList = " + temp);
          arrListAllPDF.add(temp);
        }
      }

      if((arrListAllPDF != null) && (arrListAllPDF.size() > 0))
      {
        String[] arrSortedPDFByPDFName = sortPDFByPDFName(arrListAllPDF);

        for(int i = 0; i < arrSortedPDFByPDFName.length; i++)
        {
          arrAllPDFSortedOrder.add(arrSortedPDFByPDFName[i]); // add *.pdf in sorted order
        }
      }

      Log.debugLog(className, "loadAllPDF", "", "", "Number of PDF files = " + arrAllPDFSortedOrder.size());
      return arrAllPDFSortedOrder;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "loadAllPDF", "", "", "Exception - ", e);
      return null;
    }
  }


  // Read All pdf File in work/pdf dir
  private Vector readAllPDF()
  {
    Log.debugLog(className, "readPDF", "", "", "Start method");

    String[] strPDFRcrd = null;
    Vector vecTempPDFData = null;

    try
    {
      arrListAllSortedPDF = loadAllPDF();  // Names of all PDF files

      if(arrListAllSortedPDF == null)
        return null;

      if(arrListAllSortedPDF.size() == 0)
      {
        Log.debugLog(className, "readAllPDF", "", "", "PDF File not found in pdf directory.");
        return null;
      }

      Iterator iteratorAllPDF = arrListAllSortedPDF.iterator();

      while(iteratorAllPDF.hasNext())
      {
        vecTempPDFData = readReport(iteratorAllPDF.next().toString());

        if(vecTempPDFData == null)
          return null;

        getAndSetNumPDFs(vecTempPDFData);

        vecTempPDFData = null;
      }

      strPDFRcrd = strToArrayData(vecAllPDFData.elementAt(0).toString(), "|");

      strPDFRcrd[NUM_PDF_INDEX] = "" + numPDFs;

      Log.debugLog(className, "readPDF", "", "", "Updating Info line in All PDF data vector. Line = " + rptUtilsBean.strArrayToStr(strPDFRcrd, "|"));
      vecAllPDFData.set(0, rptUtilsBean.strArrayToStr(strPDFRcrd, "|"));

      return vecAllPDFData;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "readPDF", "", "", "Exception - ", e);
      return null;
    }
  }


  // This function return the list of pdf in sorted order by PDF name
  private String[] sortPDFByPDFName(ArrayList arrListAllPDF)
  {
    Log.debugLog(className, "sortPDFByPDFName", "", "", "Start method");

    try
    {
      Iterator iteratorAllPDF = arrListAllPDF.iterator();

      Vector vecTempPDFData = null;

      // add only first group of pdf, and sort it
      // our assumption is that every pdf has only one PDF entry
      String[] arrPDFNameSortedOrder = new String[arrListAllPDF.size()];

      // Array of sorted pdf by PDF Name
      String[] arrSortedPDFByPDFName = new String[arrListAllPDF.size()];

      int index = 0;
      while(iteratorAllPDF.hasNext())
      {
        vecTempPDFData = readReport(iteratorAllPDF.next().toString());

        if(vecTempPDFData == null)
          return null;

        String dataLine = "";
        for(int i = 0; i < vecTempPDFData.size(); i++)
        {
          dataLine = vecTempPDFData.get(i).toString();

          if(dataLine.startsWith("PDF"))
          {
            String[] arrRcrd = strToArrayData(dataLine, "|");

            arrPDFNameSortedOrder[index] = arrRcrd[PDF_NAME_INDEX] + "|" + index; // append PDF name and index position of pdf file in array list
            break;
          }
        }
        vecTempPDFData = null;
        index++;
      }

      Arrays.sort(arrPDFNameSortedOrder); // sort PDF name

      for(int i = 0; i < arrPDFNameSortedOrder.length; i++)
      {
        String[] arrRcrd = strToArrayData(arrPDFNameSortedOrder[i], "|");
        int indxPos = Integer.parseInt(arrRcrd[1]);

        Log.debugLog(className, "sortPDFByPDFName", "", "", "Sorted PDF name = " + arrRcrd[0] + ", index position = " + arrRcrd[1] + ", pdf name = " + arrListAllPDF.get(indxPos).toString());

        arrSortedPDFByPDFName[i] = arrListAllPDF.get(indxPos).toString();
      }

      return arrSortedPDFByPDFName;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "sortPDFByPDFName", "", "", "Exception - ", e);
      return null;
    }
  }


  // Set the Total PDF Number
  private void getAndSetNumPDFs(Vector vecData)
  {
    Log.debugLog(className, "getAndSetNumPDFs", "", "", "Start method");

    String[] strPDFRcrd = null;

    try
    {
      for(int k = 0; k < vecData.size(); k++)
      {
        if(vecData.elementAt(k).toString().startsWith("#"))
          continue;
        else
        {
          if(vecData.elementAt(k).toString().startsWith("Info|"))
          {
            strPDFRcrd = strToArrayData(vecData.elementAt(k).toString(), "|");

            if(!infoFlagFirstTime)
            {
            //  Log.debugLog(className, "getAndSetNumPDFs", "", "", "Adding Info line in All PDF data vector. Data Line = " + vecData.elementAt(k).toString());
              vecAllPDFData.add(vecData.elementAt(k).toString());

              infoFlagFirstTime = true;
            }
            //Log.debugLog(className, "getAndSetNumPDFs", "", "", "Updating number Of PDFs = " + (numPDFs + Integer.parseInt(strPDFRcrd[NUM_PDF_INDEX])));
            numPDFs = numPDFs + Integer.parseInt(strPDFRcrd[NUM_PDF_INDEX]);
          }
          else
          {
            //Log.debugLog(className, "getAndSetNumPDFs", "", "", "Adding data line in All PDF data vector. Data Line = " + vecData.elementAt(k).toString());
            vecAllPDFData.add(vecData.elementAt(k).toString());
          }
        }
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getAndSetNumPDFs", "", "", "Exception - ", e);
    }
  }


  // This initialize all PDF and Slab Info
  public boolean initPDFNames()
  {
    //Log.debugLog(className, "initPDFNames", "", "", "Start method");

    try
    {
      Vector vecData = null;
      String[] arrRcrd = null;
      int pdfCount = 0;
      String dataLine = "";

      if(pdfVecData == null || pdfVecData.size() == 0)
      {
        if(numTestRun != -1)
          vecData = getTestRunPDF(); // if (testRun != -1) then get data from testrun.pdf
        else
          vecData = readAllPDF(); // else get data from all pdf, in pdf dir
      }
      else
      {
        vecData = pdfVecData;
      }

      if(vecData == null)
      {
        Log.debugLog(className, "initPDFNames", "", "", "PDF File not found.");
        return false;
      }

      for (int i = 0; i < vecData.size(); i++)
      {
        arrRcrd = null;
        dataLine = "";
        dataLine = vecData.get(i).toString();
        arrRcrd = strToArrayData(dataLine, "|");

        if(arrRcrd[HEADER_INDEX].equals("Info"))
        {
          String versionInfo = arrRcrd[VERSION_INDEX];
          String[] arrVersionInfo = strToArrayData(versionInfo, ".");

          majorVersionNum = Integer.parseInt(arrVersionInfo[0].trim());
          minorVersionNum = Integer.parseInt(arrVersionInfo[1].trim());

          totalPDFs = Integer.parseInt(arrRcrd[NUM_PDF_INDEX].trim());

          sizeOfPctMsgData = Integer.parseInt(arrRcrd[SIZE_OF_PCT_MSG_DATA_INDEX].trim());

          modeType = Integer.parseInt(arrRcrd[MODE_INDEX].trim());

          interval = Integer.parseInt(arrRcrd[INTERVAL_INDEX].trim());

          //Log.debugLog(className, "initPDFNames", "", "", "Version information = " + versionInfo + ", Major version number = " + majorVersionNum + ", Minor version number = " + minorVersionNum + "Total number of PDF's = " + totalPDFs + ", Size of percentile message data = " + sizeOfPctMsgData + ", Mode Type = " + modeType + ", Interval = " + interval);

          pdfInfo = new PDFInfo[totalPDFs];
        }

        else if(arrRcrd[HEADER_INDEX].equals("PDF"))
        {
          //Log.debugLog(className, "initPDFNames", "", "", "Create PDFInfo object for, dataLine = " + dataLine);

          String pdfName = "";
          if((arrListAllSortedPDF == null) || (arrListAllSortedPDF.size() == 0))
            pdfName = PDF_FILE + PDF_FILE_EXTN;
          else
            pdfName = arrListAllSortedPDF.get(pdfCount).toString();

          pdfInfo[pdfCount] = new PDFInfo(pdfName);
          pdfInfo[pdfCount].setPDFInfo(arrRcrd);

          for(int j = 0; j < pdfInfo[pdfCount].getNumOfSlabs(); j++)
          {
            dataLine = "";
            dataLine = vecData.get(i + j + 1).toString(); // to get Slab Info line

            pdfInfo[pdfCount].setSlabsInfo(dataLine, j);
            //Log.debugLog(className, "initPDFNames", "", "", "Create SlabInfo object for, dataLine = " + dataLine);
          }

          i = i + pdfInfo[pdfCount].getNumOfSlabs(); // skip slab info lines in vector
          pdfCount++;
        }

        else if(arrRcrd[HEADER_INDEX].equals("END"))
        {
          //Log.debugLog(className, "initPDFNames", "", "", "Getting end seq from END line, dataLine = " + dataLine);

          endSequenceNum = Integer.parseInt(arrRcrd[END_SEQ_INDEX].trim());

          //Log.debugLog(className, "initPDFNames", "", "", "End Sequence Number = " + endSequenceNum);
        }
      }

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "initPDFNames", "", "", "Exception - ", e);
      return false;
    }
  }


  // this function return the size of pct msg data
  public int getSizeOfPctMsgData()
  {
    return sizeOfPctMsgData;
  }


  // this function return the mode type of percentile data
  public int getModeType()
  {
    return modeType;
  }


  // this function return the interval of percentile data, it is -1 in case of (0-Total Run & 1-Run phase)
  public int getInterval()
  {
    return interval;
  }

  // this function return the end sequence number
  public int getEndSeqNum()
  {
    return endSequenceNum;
  }


  // this function return number of granule by PDF Id
  public int getNumGranuleByPDFId(int pdfId)
  {
    //Log.debugLog(className, "getNumGranuleByPDFId", "", "", "Start method, PDF Id = " + pdfId);

    try
    {
      int numGranule = -1;
      if(pdfInfo != null)
      {
        for(int i = 0; i < pdfInfo.length; i++)
        {
          if(pdfInfo[i].getPDFID() == pdfId)
          {
            numGranule = pdfInfo[i].getNumGranule();
            break;
          }
        }
      }

      return numGranule;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getNumGranuleByPDFId", "", "", "Exception - ", e);
      return -1;
    }
  }


  // this function return min granule by PDF Id
  public int getMinGranuleByPDFId(int pdfId)
  {
    //Log.debugLog(className, "getMinGranuleByPDFId", "", "", "Start method, PDF Id = " + pdfId);

    try
    {
      int minGranule = -1;
      if(pdfInfo != null)
      {
        for(int i = 0; i < pdfInfo.length; i++)
        {
          if(pdfInfo[i].getPDFID() == pdfId)
          {
            minGranule = pdfInfo[i].getMinGranule();
            break;
          }
        }
      }

      return minGranule;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getMinGranuleByPDFId", "", "", "Exception - ", e);
      return -1;
    }
  }


  // this function return array of Slab Info By PDF ID
  public SlabInfo[] getArrSlabsInfoByPDFId(int pdfId)
  {
    //Log.debugLog(className, "getArrSlabsInfoByPDFId", "", "", "Start method, PDF Id = " + pdfId);

    try
    {
      SlabInfo[] tempSlabInfo = null;
      if(pdfInfo != null)
      {
        for(int i = 0; i < pdfInfo.length; i++)
        {
          if(pdfInfo[i].getPDFID() == pdfId)
          {
            tempSlabInfo = pdfInfo[i].slabInfo;
            break;
          }
        }
      }

      return tempSlabInfo;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getArrSlabsInfoByPDFId", "", "", "Exception - ", e);
      return null;
    }
  }


  // this function return size of PDF Data By PDF ID
  public long getPDFDataSizeByPDFID(String strPDFId)
  {
    //Log.debugLog(className, "getPDFDataSizeByPDFID", "", "", "Start method, PDF Id = " + strPDFId);

    try
    {
      int pdfId = Integer.parseInt(strPDFId.trim());

      long pdfDataSize = 0;

      if(pdfInfo != null)
      {
        for(int i = 0; i < pdfInfo.length; i++)
        {
          if(pdfInfo[i].getPDFID() == pdfId)
          {
            pdfDataSize = pdfInfo[i].getPDFDataSize();
            break;
          }
        }
      }

      return pdfDataSize;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getPDFDataSizeByPDFID", "", "", "Exception - ", e);
      return -1;
    }
  }


  // this function return size of PDF Data By PDF ID
  public int getFormulaNumberByPDFId(int pdfId)
  {
    //Log.debugLog(className, "getFormulaNumberByPDFId", "", "", "Start method, PDF Id = " + pdfId);

    try
    {
      int formulaNum = -1;

      if(pdfInfo != null)
      {
        for(int i = 0; i < pdfInfo.length; i++)
        {
          if(pdfInfo[i].getPDFID() == pdfId)
          {
            formulaNum = pdfInfo[i].getFormulaNumber();
            break;
          }
        }
      }

      return formulaNum;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getFormulaNumberByPDFId", "", "", "Exception - ", e);
      return -1;
    }
  }


  // this function return the PDF unit By PDF ID
  public String getPDFUnitByPDFId(int pdfId)
  {
    //Log.debugLog(className, "getPDFUnitByPDFId", "", "", "Start method, PDF Id = " + pdfId);

    try
    {
      String pdfUnit = "";

      if(pdfInfo != null)
      {
        for(int i = 0; i < pdfInfo.length; i++)
        {
          if(pdfInfo[i].getPDFID() == pdfId)
          {
            pdfUnit = pdfInfo[i].getPDFUnit();
            break;
          }
        }
      }

      return pdfUnit;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getPDFUnitByPDFId", "", "", "Exception - ", e);
      return "";
    }
  }


  // this function return the slab index by pdfId & slabName
  // Assuming that slab name is unique
  // Issue - If Slab name is same then it always give the first slab index, Use Slab ID
  public int getSlabIndexBySlabName(int pdfId, String strSlabRptName)
  {
    //Log.debugLog(className, "getSlabIndexBySlabName", "", "", "Start method, PDF Id = " + pdfId + ", Slab Report Name = " + strSlabRptName);

    try
    {
      int slabIndex = -1;
      SlabInfo[] slabInfo = null;

      if(pdfInfo != null)
      {
        for(int i = 0; i < pdfInfo.length; i++)
        {
          if(pdfInfo[i].getPDFID() == pdfId)
          {
            slabInfo = pdfInfo[i].slabInfo;
            break;
          }
        }
      }

      if(slabInfo != null)
      {
        for(int i = 0; i < slabInfo.length; i++)
        {
          if(strSlabRptName.indexOf(slabInfo[i].getSlabName()) != -1)
          {
            slabIndex = i;
            break;
          }
        }
      }

      return slabIndex;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getSlabIndexBySlabName", "", "", "Exception - ", e);
      return -1;
    }
  }


  // This is for local testing to print result on console
  public void printResult()
  {
    System.out.println("PrintResult Method called.");

    try
    {
      System.out.println("Major Version Number = " + majorVersionNum);
      System.out.println("Minor Version Number = " + minorVersionNum);
      System.out.println("Total Number of PDFs = " + totalPDFs);
      System.out.println("Size Of Percentile Msg Data = " + sizeOfPctMsgData);

      if(pdfInfo != null)
      {
        for(int i = 0; i < pdfInfo.length; i++)
        {
          System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
          System.out.println("PDF Name = " + pdfInfo[i].getPDFName());
          System.out.println("PDF ID = " + pdfInfo[i].getPDFID());
          System.out.println("Number of slabs = " + pdfInfo[i].getNumOfSlabs());
          System.out.println("PDF Description = " + pdfInfo[i].getPDFDescription());
          System.out.println("Number of granule = " + pdfInfo[i].getNumGranule());
          System.out.println("PDF Data Size = " + pdfInfo[i].getPDFDataSize());
          System.out.println("PDF Status = " + pdfInfo[i].getPDFStatus());

          for(int j = 0; j < pdfInfo[i].slabInfo.length; j++)
          {
            System.out.println("-------------------------------------");
            System.out.println("Slab Name = " + pdfInfo[i].slabInfo[j].getSlabName());
            System.out.println("Slab ID = " + pdfInfo[i].slabInfo[j].getSlabID());
            System.out.println("Slab Min value = " + pdfInfo[i].slabInfo[j].getSlabMinValue());
            System.out.println("Slab Max value = " + pdfInfo[i].slabInfo[j].getSlabMaxValue());
            System.out.println("Color = " + pdfInfo[i].slabInfo[j].getColorName());
            System.out.println("Slab Status = " + pdfInfo[i].slabInfo[j].getSlabStatus());
          }
        }
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }


  public static void main(String[] args)
  {
    PDFNames pdfNames = new PDFNames(-1);// 23494
    pdfNames.printResult();
  }
}

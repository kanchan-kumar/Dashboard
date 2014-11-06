/*--------------------------------------------------------------------
@Name    : PDFInfo.java
@Author  : Prabhat
@Purpose : To store all Information of each pdf
@Modification History:
    01/12/2009 --> Prabhat  -->  Initial Version

----------------------------------------------------------------------*/

package pac1.Bean;

import pac1.Bean.*;

import java.io.*;
import java.util.*;

public class PDFInfo implements java.io.Serializable
{
  private final String className = "PDFInfo";

  // size of each granule is longlong means 8 bytes
  private transient final int GRANULE_SIZE = 8;

  // PDF information line
  /*****            PDF|PDF_Id|Min_Granule|Max_Granule|numSlabs|PDF_UNIT|PDF_Description   *****/

  // Indexes of PDF Line
  //private transient final int PDF_NAME_INDEX = 1;
  private transient final int PDF_ID_INDEX = 1;
  private transient final int MIN_GRANULE_INDEX = 2;
  private transient final int MAX_GRANULE_INDEX = 3;
  private transient final int NUM_SLABS_INDEX = 4;
  private transient final int PDF_UNIT_INDEX = 5;
  private transient final int PDF_DESCRIPTION_INDEX = 6;

  private String pdfName = "";
  private int pdfID;
  private int minGranule;
  private int maxGranule;
  private int numSlabs;
  private String pdfUnit = "";
  private String pdfDescription = "";

  private int formulaNum = -1;

  private int numGranule = -1;
  private long pdfDataSize = -1;

  private String pdfStatus = ""; // Either valid or invalid

  public SlabInfo[] slabInfo = null;

  public PDFInfo(String pdfName)
  {
    this.pdfName = pdfName;
  }

  // Set PDF Info
  public void setPDFInfo(String[] arrRcrd)
  {
    //Log.debugLog(className, "setPDFInfo", "", "", "Start method, PDF Name = " + pdfName);

    try
    {
      pdfID = Integer.parseInt(arrRcrd[PDF_ID_INDEX].trim());

      minGranule = Integer.parseInt(arrRcrd[MIN_GRANULE_INDEX].trim());

      maxGranule = Integer.parseInt(arrRcrd[MAX_GRANULE_INDEX].trim());

      numSlabs = Integer.parseInt(arrRcrd[NUM_SLABS_INDEX].trim());

      pdfUnit = arrRcrd[PDF_UNIT_INDEX];

      pdfDescription = arrRcrd[PDF_DESCRIPTION_INDEX];

      slabInfo = new SlabInfo[numSlabs];

      // calculate number of granules & set it
      calcAndSetNumGranule();

      // calculate PDF data size & set it
      calcAndSetPDFDataSize();

      // set PDf status, on the basis of PDF validation
      setPDFStatus();

    //  Log.debugLog(className, "setPDFInfo", "", "", "Set all PDF Info, PDF Id = " + pdfID + ", Min Granule = " + minGranule + ", Max Granule = " + maxGranule + ", Number of Slabs = " + numSlabs + ", PDF Unit = " + pdfUnit + ", PDF Description = " + pdfDescription + ", Number of Granules = " + numGranule + "PDF Data Size = " + pdfDataSize + ", PDF Status = " + pdfStatus);
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "setPDFInfo", "", "", "Exception - ", e);
    }
  }

  // Set Slabs Info
  public void setSlabsInfo(String dataLine, int indexSlab)
  {
    //Log.debugLog(className, "setSlabsInfo", "", "", "Start method, Data Line = " + dataLine);

    try
    {
      slabInfo[indexSlab] = new SlabInfo(minGranule, maxGranule);
      slabInfo[indexSlab].setSlabsInfo(dataLine);
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "setSlabsInfo", "", "", "Exception - ", e);
    }
  }


  // calculate and set number of granules
  private void calcAndSetNumGranule()
  {
    //Log.debugLog(className, "calcAndSetNumGranule", "", "", "Start method. PDF Name = " + pdfName);

    try
    {
      // MinGranule should be greater than 0 & less than or equal to MaxGranule
      // our assumption is that maxGranule is divisible by minGranule
      if((minGranule > 0) && (minGranule <= maxGranule))
      {
        numGranule = ((maxGranule/minGranule) + 1);
      }
      else
        Log.errorLog(className, "calcAndSetNumGranule", "", "", "Min & Max Granule is invalid in PDF, PDF Id = " + pdfID);

    //  Log.debugLog(className, "calcAndSetNumGranule", "", "", "Set Number of Granule = " + numGranule);
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "calcAndSetNumGranule", "", "", "Exception - ", e);
    }
  }


  // calculate and set PDF Data Size
  private void calcAndSetPDFDataSize()
  {
    //Log.debugLog(className, "calcAndSetPDFDataSize", "", "", "Start method. PDF Name = " + pdfName);

    try
    {
      if(numGranule != -1)
        pdfDataSize = numGranule * GRANULE_SIZE;
      else
        Log.errorLog(className, "calcAndSetPDFDataSize", "", "", "Number of Granule is invalid in PDF, PDF Id = " + pdfID);

    //  Log.debugLog(className, "calcAndSetPDFDataSize", "", "", "Set PDF data size = " + pdfDataSize);
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "calcAndSetPDFDataSize", "", "", "Exception - ", e);
    }
  }


  // set PDf status, on the basis of PDF validation
  private void setPDFStatus()
  {
    //Log.debugLog(className, "setPDFStatus", "", "", "Start method. PDF Name = " + pdfName);

    try
    {
      /*
      if PDF meat all this condition then set status valid else invalid
        1- MaxGranule must be multiple of MinGranule
        2- MinGranule should be greater than 0 & less than or equal to MaxGranule
        3- Number of slabs define in PDF should be <= Number of Granule
      */

      if((((maxGranule % minGranule) == 0) && ((minGranule > 0) && (minGranule <= maxGranule))) && ((numGranule != -1) && (numSlabs <= numGranule)))
      {
        pdfStatus = "valid";
      }
      else
        pdfStatus = "invalid";

    //  Log.debugLog(className, "setPDFStatus", "", "", "Set PDF Status = " + pdfStatus);
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "setPDFStatus", "", "", "Exception - ", e);
    }
  }


  // set formula number same as graph data
  public void setFormulaNum(int formulaNumber)
  {
    //Log.debugLog(className, "setFormulaNum", "", "", "Start method. PDF Name = " + pdfName + ", PDF Id = " + pdfID + ", Formula Number = " + formulaNumber);

    try
    {
      this.formulaNum = formulaNumber;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "setFormulaNum", "", "", "Exception - ", e);
    }
  }


  // this function return the PDF Name
  public String getPDFName()
  {
    return pdfName;
  }

  // this function return the PDF ID
  public int getPDFID()
  {
    return pdfID;
  }

  // this function return the number of slabs in that PDF
  public int getNumOfSlabs()
  {
    return numSlabs;
  }

  // this function return the PDF unit
  public String getPDFUnit()
  {
    if(pdfUnit.equals("-"))
      return "";
    else
      return pdfUnit;
  }

  // this function return the PDF description
  public String getPDFDescription()
  {
    return pdfDescription;
  }

  // this function return the number of granule
  public int getNumGranule()
  {
    return numGranule;
  }

  // this function return the min granule value
  public int getMinGranule()
  {
    return minGranule;
  }

  // this function return the PDF data size
  public long getPDFDataSize()
  {
    return pdfDataSize;
  }

  // this function return the PDF status
  public String getPDFStatus()
  {
    return pdfStatus;
  }

  // this function return the formula number
  public int getFormulaNumber()
  {
    return formulaNum;
  }
}

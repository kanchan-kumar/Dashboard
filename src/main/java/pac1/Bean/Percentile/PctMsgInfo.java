package pac1.Bean.Percentile;

import pac1.Bean.GraphName.GraphNames;

public class PctMsgInfo
{
  private GraphNames graphNames = null;

  private int pdfId, numGranule, minGranule, formulaNumber;
  private String pdfUnit;
  private double formulaUnitData;

  /**
   * Size of pctMessage.dat file packet (header + data packet)
   */
  private long pctPktSize = 0;

  /**
   * Size of Data Packet (pctPktSize - Header Size)
   */
  private long pctDataPktSize = 0;

  /**
   * Size of pctMessage.dat file
   */
  private long pctFileSize = 0;

  /**
   * Number of packets in pctMessage.dat file
   */
  private int numOfPkts = 0;

  /**
   * It is 0- Total Run, 1- Run Phase, 2- Specified time
   */
  private int pctModeType = 0;

  /**
   * 1.PERCENTILE_REPORT 1 0 for percentile report at end
   * 
   * 2.PERCENTILE_REPORT 1 1 for phases
   * 
   * 3.PERCENTILE_REPORT 1 2 10000(time slab)
   */

  private int pctInterval = 10000;

  public long getPctPktSize()
  {
    return pctPktSize;
  }

  public void setPctPktSize(long pctPktSize)
  {
    this.pctPktSize = pctPktSize;
  }

  public long getPctDataPktSize()
  {
    return pctDataPktSize;
  }

  public void setPctDataPktSize(long pctDataPktSize)
  {
    this.pctDataPktSize = pctDataPktSize;
  }

  public long getPctFileSize()
  {
    return pctFileSize;
  }

  public void setPctFileSize(long pctFileSize)
  {
    this.pctFileSize = pctFileSize;
  }

  public int getNumOfPkts()
  {
    return numOfPkts;
  }

  public void calculateNumOfPkts()
  {
    if (pctPktSize != 0)
      this.numOfPkts = (int) (pctFileSize / pctPktSize);
  }

  public void setNumOfPkts(int numOfPkts)
  {
    this.numOfPkts = numOfPkts;
  }

  public int getPctModeType()
  {
    return pctModeType;
  }

  public void setPctModeType(int pctModeType)
  {
    this.pctModeType = pctModeType;
  }

  public int getPctInterval()
  {
    return pctInterval;
  }

  public void setPctInterval(int pctInterval)
  {
    this.pctInterval = pctInterval;
  }

  public GraphNames getGraphNames()
  {
    return graphNames;
  }

  public void setGraphNames(GraphNames graphNames)
  {
    this.graphNames = graphNames;
  }

  public int getPdfId()
  {
    return pdfId;
  }

  public void setPdfId(int pdfId)
  {
    this.pdfId = pdfId;
  }

  public int getNumGranule()
  {
    return numGranule;
  }

  public void setNumGranule(int numGranule)
  {
    this.numGranule = numGranule;
  }

  public int getMinGranule()
  {
    return minGranule;
  }

  public void setMinGranule(int minGranule)
  {
    this.minGranule = minGranule;
  }

  public int getFormulaNumber()
  {
    return formulaNumber;
  }

  public void setFormulaNumber(int formulaNumber)
  {
    this.formulaNumber = formulaNumber;
  }

  public String getPdfUnit()
  {
    return pdfUnit;
  }

  public void setPdfUnit(String pdfUnit)
  {
    this.pdfUnit = pdfUnit;
  }

  public double getFormulaUnitData()
  {
    return formulaUnitData;
  }

  public void setFormulaUnitData(double formulaUnitData)
  {
    this.formulaUnitData = formulaUnitData;
  }

  public String toString()
  {
    return "pctPktSize = " + pctPktSize + ", pctDataPktSize = " + pctDataPktSize + ", pctFileSize = " + pctFileSize + ", numOfPkts = " + numOfPkts + ", pctModeType = " + pctModeType + ", pctInterval = " + pctInterval;
  }
}
/*--------------------------------------------------------------------
@Name    : ReportDataSelected.java
@Author  : Prabhat
@Purpose : Keep the report data of selected time period and store data in 2D and msgData
@Modification History:
    11/12/2008 -> Prabhat (Initial Version)

----------------------------------------------------------------------*/
package pac1.Bean;
import java.util.Vector;
import pac1.Bean.*;

public class ReportDataSelected implements java.io.Serializable
{
  private String className = "ReportDataSelected";

  private double[][] selectedData = null;
  private data selectedMsgData = null;

  private Vector vecSelectedData2DArr = new Vector();
  private Vector vecSelectedMsgDataObj = new Vector();
  private Vector vecGraphNamesPostFix = new Vector();

  private String totalProcessingTime;

  public ReportDataSelected()
  {
    Log.debugLog(className, "ReportDataSelected", "", "", "Method Started");
  }

  public String getTotalProcessingTime()
  {
    return totalProcessingTime;
  }

  public void setTotalProcessingTime(long startProcessingTime, long endProcessingTime)
  {
    this.totalProcessingTime = rptUtilsBean.convertMilliSecToSecs(endProcessingTime - startProcessingTime);
  }

  // this function is to get the selected MsgData object (only for home TR)
  public data getSelectedMsgData()
  {
    Log.debugLog(className, "getSelectedMsgData", "", "", "Method Started");

    return selectedMsgData;
  }

  // this function is to get the selected data in 2D array (only for home TR)
  public double[][] getSelectedData()
  {
    Log.debugLog(className, "getSelectedData", "", "", "Method Started");

    return selectedData;
  }

  // this function is to get the selected msgData Vector(for all requested TR)
  public Vector getSelectedMsgDataVector()
  {
    Log.debugLog(className, "getSelectedMsgDataVector", "", "", "Method Started");

    return vecSelectedMsgDataObj;
  }

  // this function is to get the selected data Vector(for all requested TR)
  public Vector getSelectedDataVector()
  {
    Log.debugLog(className, "getSelectedDataVector", "", "", "Method Started");

    return vecSelectedData2DArr;
  }

  // this function is to get the vector of graphName postfix string
  public Vector getVectorGraphNamePostfixStr()
  {
    Log.debugLog(className, "getVectorGraphNamePostfixStr", "", "", "Method Started");

    return vecGraphNamesPostFix;
  }

  // this function is to set the selected msgData
  public void setSelectedMsgData(data selectedMsgData)
  {
    Log.debugLog(className, "setSelectedMsgData", "", "", "Method Started");

    this.selectedMsgData = selectedMsgData;
  }

  // this function is to add selected msgData in vector(for all TR)
  public void addSelectedMsgDataInVector(data selectedMsgData)
  {
    Log.debugLog(className, "addSelectedMsgDataInVector", "", "", "Method Started");

    // add selected msg data obj in vector
    vecSelectedMsgDataObj.add(selectedMsgData);
  }

  // this function is to set the selected data in 2D Array
  public void setSelectedData(double[][] selectedData)
  {
    Log.debugLog(className, "setSelectedData", "", "", "Method Started");

    this.selectedData = selectedData;
  }

  // this function is to add selected data in Vector
  public void addSelectedDatainVector(double[][] selectedData)
  {
    Log.debugLog(className, "addSelectedDatainVector", "", "", "Method Started");

    // add selected data 2D double array in vector
    vecSelectedData2DArr.add(selectedData);
  }

  public void addGraphNamePostfixInVector(String graphNamesPostFix)
  {
    Log.debugLog(className, "addGraphNamePostfixInVector", "", "", "Method Started");

    // add selected data 2D double array in vector
    vecGraphNamesPostFix.add(graphNamesPostFix);
  }
}

package pac1.Bean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

public class TestRunAdfInfo implements java.io.Serializable
{
  private String className = "TestRunInfo";
  private ArrayList arrGroupIdInfo = new ArrayList();
  private ArrayList arrGraphIdInfo = new ArrayList();
  private ArrayList arrVecNameInfo = new ArrayList();
  private ArrayList arrAlertTypeInfo = new ArrayList();
  private ArrayList arrMinimumInfo = new ArrayList();
  private ArrayList arrMaximumInfo = new ArrayList();
  private ArrayList arrWarningInfo = new ArrayList();
  private ArrayList arrMajorInfo = new ArrayList();
  private ArrayList arrCriticalInfo = new ArrayList();

/**
 * Constructor
 *
 */
  public TestRunAdfInfo() {  }

  ///////////////////////////////Getter methods///////////////////////////////

  public ArrayList getGroupIdInfo()
  {
    return arrGroupIdInfo;
  }

  public ArrayList getGraphIdInfo()
  {
    return arrGraphIdInfo;
  }

  public ArrayList getVecNameInfo()
  {
    return arrVecNameInfo;
  }

  public ArrayList getAlertTypeInfo()
  {
    return arrAlertTypeInfo;
  }

  public ArrayList getMinimumInfo()
  {
    return arrMinimumInfo;
  }

  public ArrayList getMaximumInfo()
  {
    return arrMaximumInfo;
  }

  public ArrayList getWarningInfo()
  {
    return arrWarningInfo;
  }

  public ArrayList getMajorInfo()
  {
    return arrMajorInfo;
  }

  public ArrayList getCriticalInfo()
  {
    return arrCriticalInfo;
  }

  /////////////////////////////Setter Methods///////////////////////////////

  public void setADFArrayListInfo(String strGroupId, String strGraphId, String strVecName, String strAlertType, String strMinimum, String strMaximum, String strWarning, String strMajor, String strCritical)
  {
    this.arrGroupIdInfo.add(strGroupId);
    this.arrGraphIdInfo.add(strGraphId);
    this.arrVecNameInfo.add(strVecName);
    this.arrAlertTypeInfo.add(strAlertType);
    this.arrMinimumInfo.add(strMinimum);
    this.arrMaximumInfo.add(strMaximum);
    this.arrWarningInfo.add(strWarning);
    this.arrMajorInfo.add(strMajor);
    this.arrCriticalInfo.add(strCritical);
  }

  // this is use to save data to file ($NS_WDIR/adf/netstorm.adf)
  public boolean saveDataToFile()
  {
    Log.debugLog(className, "saveDataToFile", "", "", "Method Called.");

    try
    {
      String strFileNameWithPath = "";//ADFBean.getTestRunADFNameWithEXTN();

      File adfFileObj = new File(strFileNameWithPath);

      // if file is already there then first delete this
      if(adfFileObj.exists())
        adfFileObj.delete();

      FileOutputStream fout = new FileOutputStream(adfFileObj, true);  // Append mode
      PrintStream printStream = new PrintStream(fout);

      String grpID, graphID, vecName, alertType, minData, maxData, warningData, majorData, criticalData = "";
      String dataLine = "";

      printStream.println("ADF_DESC = Alert definition file for dial graph.");
      printStream.println("LAST_MODIFIED_DATE = " + rptUtilsBean.getCurDateTime());

      for(int i = 0; i < arrGroupIdInfo.size(); i++)
      {
        grpID = arrGroupIdInfo.get(i).toString();
        graphID = arrGraphIdInfo.get(i).toString();
        vecName = arrVecNameInfo.get(i).toString();
        alertType = arrAlertTypeInfo.get(i).toString();
        minData = arrMinimumInfo.get(i).toString();
        maxData = arrMaximumInfo.get(i).toString();
        warningData = arrWarningInfo.get(i).toString();
        majorData = arrMajorInfo.get(i).toString();
        criticalData = arrCriticalInfo.get(i).toString();

        dataLine = grpID + "|" + graphID + "|" + vecName + "|" + alertType + "|" + minData + "|" + maxData + "|" + warningData + "|" + majorData + "|" + criticalData;

        printStream.println(dataLine);
      }

      printStream.close();
      fout.close();

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getTestRunADFInfo", "", "", "Exception - ", e);
      return false;
    }
  }
}


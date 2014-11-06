/*--------------------------------------------------------------------
@Name    : GDFBean.java
@Author  : Prabhat
@Purpose : Provided the utility function(s) to custom GDF GUI(jsp)
@Modification History:
    09/08/2008 -> Prabhat (Initial Version)

----------------------------------------------------------------------*/
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
import java.util.Collections;
import java.util.Date;
import java.util.Vector;

public class GDFBean
{
  private String className = "GDFBean";
  private String workPath = Config.getWorkPath();
  private final String GDF_FILE_EXTN = ".gdf";
  private final String PDF_FILE_EXTN = ".pdf";
  private final String GDF_BAK_EXTN = ".hot";
  private final String HIDDEN_FILE_DOT = ".";
  private boolean isReadFromHotFile = true;

  // Indexes of Info Line
  private transient final int HEADER_INDEX = 0; // Header info index

  //index for PDF id in $NS_WDIR/pdf/<pdfName>.pdf
  private transient final int PDF_ID_INDEX = 1;

  //index for PDF id in $NS_WDIR/sys/<gdfName>.gdf
  private transient final int PDF_ID_INDEX_IN_GDF = 9;

  // Indexes of Group Line
  private transient final int GROUP_NAME_INDEX = 1;
  private transient final int GROUP_ID_INDEX = 2;
  private transient final int GROUP_TYPE_INDEX = 3;
  private transient final int GRAPH_COUNT_INDEX = 4;
  private transient final int NUM_VECTOR_GROUP_INDEX = 5;
  private transient final int METRICS_NAME_INDEX = 6;
  private transient final int HIERARCHICAL_PATH_INDEX = 7;
  private transient final int GROUP_DESC_INDEX = 8;
  private transient final String DEFAULT_GROUP_DESCRIPTION = "Group description not available";


  // Indexes of Graph Line
  private transient final int GRAPH_NAME_INDEX = 1;
  private transient final int GRAPH_ID_INDEX = 2;
  private transient final int GRAPH_TYPE_INDEX = 3;
  private transient final int DATA_TYPE_INDEX = 4;
  private transient final int GRAPH_FORMULA_INDEX = 6;
  private transient final int GRAPH_STATE = 8;  
  private transient final int PDF_NAME_INDEX = 8;
  private transient final int Graph_DESC_INDEX = 13;
  private transient final String DEFAULT_GRAPH_DESCRIPTION = "Graph description not available";

  /* Change Header version for providing Metrics Name
   * 1.0 - Initial version
   * 2.0 - Change done for match Long_data and Long_long_data to double
   * 3.0 - Changes done for Dynamic data in Execution GUI
   * 3.1 - Use future1 field of group line for Metric group name
   */
  private transient final String GDF_INFO_HDR_LINE = "Info|3.1|1|-|-|-1|-1|-";
  private transient final String LAST_MAX_ASSIGNED_GRAPH_ID = "#LAST_MAX_ASSIGNED_GRAPH_ID = 0";
  private transient final String LAST_MODIFIED_DATE = "#LAST_MODIFIED_DATE = ";

  // our convention is to assign custom monitors group id to 10000
  // but we allow user to assign id for user defined custom monitor to 20000
  private transient final int USER_DEFINED_CUSTOM_MON_ID = 20000;
  private String hieraricalPath = "Tier>Server>Instance";

  public GDFBean(){ }

  public GDFBean(boolean isReadFromHotFile)
  {
    // if false, then gdf's details is read from cold files.
    this.isReadFromHotFile = isReadFromHotFile;
  }

  // This will return the Custom GDF path
  public String getCustomGDFPath()
  {
    return (workPath + "/sys/");
  }

  // This will return the PDF path, that is $NS_WDIR/pdf.
  private String getPDFPath()
  {
    return (workPath + "/pdf/");
  }

  //This will return standard gdf path
  public String getStandardGDFPath()
  {
    return (workPath + "/etc/");
  }

  // This will return the bak gdf name
  private String getBAKGDFName(String gdfName)
  {
    return (HIDDEN_FILE_DOT + gdfName + GDF_FILE_EXTN + GDF_BAK_EXTN);
  }

  private String getGDFName(String gdfName)
  {
    return (gdfName + GDF_FILE_EXTN);
  }
  private ArrayList getAllCustomGDF()
  {
    return getAllCustomGDF(getCustomGDFPath());
  }
  
  // load all Custom gdf file in Array List
  private ArrayList getAllCustomGDF(String strGDFPath)
  {
    Log.debugLog(className, "getAllCustomGDF", "", "", "Start method");

    ArrayList arrAllCustomGDF = new ArrayList();  // Names of all GDF files
    ArrayList arrListOfAllCustomGDFSortedOrder = new ArrayList(); // Names of all Custom gdf files in sorted order
    File customGDF = new File(strGDFPath);
    String temp;
    String tempPath;

    try
    {
      // For Custom GDF
      if(customGDF == null)
        return null;

      String arrFiles[] = customGDF.list();
      tempPath = getCustomGDFPath();

      // Calculate number of gdf files
      for(int j = 0; j < arrFiles.length; j++)
      {
        temp = "";
        // Use lastIndexOf() function instead indexOf() beacuse reading extn.
        if(arrFiles[j].lastIndexOf(GDF_FILE_EXTN) == -1)  // Skip non gdf files
          continue;

        temp = tempPath + arrFiles[j];

        String[] tempArr = rptUtilsBean.strToArrayData(arrFiles[j], "."); // for remove profile file extension
        if(tempArr.length == 2) // add only <filename>.gdf file
        {
          arrAllCustomGDF.add(tempArr[0]);
          Log.debugLog(className, "getAllCustomGDF", "", "", "Adding GDF in ArrayList = " + temp);
        }
        else
        {
          Log.debugLog(className, "getAllCustomGDF", "", "", "Skip file = " + temp);
        }
      }

      Log.debugLog(className, "getAllCustomGDF", "", "", "Number of GDF files = " + arrAllCustomGDF.size());

      Object[] arrTemp = arrAllCustomGDF.toArray();
      Arrays.sort(arrTemp);

      for(int i = 0; i < arrTemp.length; i++)
        arrListOfAllCustomGDFSortedOrder.add(arrTemp[i].toString());

      return arrListOfAllCustomGDFSortedOrder;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getAllCustomGDF", "", "", "Exception - ", e);
      return null;
    }
  }

  // load all pdf file in Array List
  public String[] getPDFs()
  {
    Log.debugLog(className, "getPDFs", "", "", "Start method");

    ArrayList arrAllPDF = new ArrayList();  // To contain names of all PDF files
    ArrayList arrListOfAllPDFSortedOrder = new ArrayList(); //To keep name of all pdf files in sorted order
    File PDF = new File(getPDFPath());
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
        // Use lastIndexOf() function instead indexOf() beacuse reading extn.
        if(arrFiles[j].lastIndexOf(PDF_FILE_EXTN) == -1)  // Skip non pdf files
          continue;

        temp = tempPath + arrFiles[j];

        String[] tempArr = rptUtilsBean.strToArrayData(arrFiles[j], "."); // for remove pdf file extension
        if(tempArr.length == 2) // add only <filename>.pdf file
        {
          arrAllPDF.add(tempArr[0]);
          Log.debugLog(className, "getPDFs", "", "", "Adding PDF in ArrayList = " + temp);
        }
        else
        {
          Log.debugLog(className, "getPDFs", "", "", "Skip file = " + temp);
        }
      }

      Log.debugLog(className, "getPDFs", "", "", "Number of PDF files = " + arrAllPDF.size());

      Object[] arrTemp = arrAllPDF.toArray();
      Arrays.sort(arrTemp);

      strListOfPDF = new String[arrTemp.length];

      for(int i = 0; i < arrTemp.length; i++)
      {
        arrListOfAllPDFSortedOrder.add(arrTemp[i].toString());
        strListOfPDF[i] = arrTemp[i].toString();
      }


      //return arrListOfAllPDFSortedOrder;
      return strListOfPDF;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getPDFs", "", "", "Exception - ", e);
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
  public Vector readFile(String gdfFileWithPath)
  {
    Log.debugLog(className, "readFile", "", "", "Method called. GDF FIle Name = " + gdfFileWithPath);

    try
    {
      Vector vecFileData = new Vector();
      String strLine;

      File customGDF = openFile(gdfFileWithPath);

      if(!customGDF.exists())
      {
        Log.errorLog(className, "readFile", "", "", "GDF not found, filename - " + gdfFileWithPath);
        return null;
      }

      FileInputStream fis = new FileInputStream(gdfFileWithPath);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));

      while((strLine = br.readLine()) !=  null)
      {
        strLine = strLine.trim();
        //if(strLine.startsWith("#"))
        //  continue;
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


  private Vector readGraphData(Vector vecData)
  {
    Log.debugLog(className, "readGraphData", "", "", "Method called");

    try
    {
      Vector vecGraphDetails = new Vector();
      for(int i = 0; i < vecData.size(); i++)
      {
        if((vecData.elementAt(i).toString()).startsWith("Graph"))
        {
          vecGraphDetails.add(vecData.elementAt(i).toString());
          Log.debugLog(className, "readGraphData", "", "", "Adding Graph details to vector = " + vecData.elementAt(i).toString());
        }
      }
      return vecGraphDetails;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "readGraphData", "", "", "Exception - ", e);
      return null;
    }
  }


  // Get gdf file information
  private Vector getDataFromGdf(String gdfFileName)
  {
    Log.debugLog(className, "getDataFromGdf", "", "", "Method called");

    String gdfFileWithPath = getCustomGDFPath() + gdfFileName + GDF_FILE_EXTN;

    return(readFile(gdfFileWithPath));
  }


  // Get gdf details as 2D Array
  // Indx 1 -> GDF Name, Indx 2 -> Group Name, Indx 3 -> Last Modified Date, Indx 4 -> # of Graphs.
  public String[][] getGDFs()
  {
    Log.debugLog(className, "getGDFs", "", "", "Method Starts.");

    try
    {
      ArrayList arrListOfCustomGDF = getAllCustomGDF();
      String[][] tempArrGDFDetails = new String[arrListOfCustomGDF.size() + 1][4]; // add 1 for header
      Vector vecData = null;
      String[] arrRcrd = null;
      String gdfFileName;
      String customGDFPath = getCustomGDFPath();

      tempArrGDFDetails[0][0] = "GDF Name";
      tempArrGDFDetails[0][1] = "Group Name";
      tempArrGDFDetails[0][2] = "Modified Date";
      tempArrGDFDetails[0][3] = "Number Of Graphs";

      for(int i = 0; i < arrListOfCustomGDF.size(); i++)
      {
        String lastModDateTime = "";
        gdfFileName = customGDFPath + arrListOfCustomGDF.get(i).toString() + GDF_FILE_EXTN;

        vecData = getDataFromGdf(arrListOfCustomGDF.get(i).toString());

        if(vecData == null)
        {
          Log.debugLog(className, "getGDFs", "", "", "GDF File not found, It may be correpted, file name = " + tempArrGDFDetails[i + 1][0]);
          continue;
        }

        tempArrGDFDetails[i + 1][0] = arrListOfCustomGDF.get(i).toString();
        Log.debugLog(className, "getGDFs", "", "", "Adding GDF name in array = " + tempArrGDFDetails[i + 1][0]);

        int countGraph = 0;
        for (int j = 0; j < vecData.size(); j++)
        {
          arrRcrd = null;
          arrRcrd = rptUtilsBean.strToArrayData(vecData.elementAt(j).toString(), "|");

          if((vecData.elementAt(j).toString()).startsWith(LAST_MODIFIED_DATE))
          {
             String[] arrTemp = rptUtilsBean.strToArrayData(vecData.elementAt(j).toString(), "=");
             lastModDateTime = arrTemp[1].trim();
          }

          if(arrRcrd[HEADER_INDEX].equals("Group"))
          {
            tempArrGDFDetails[i + 1][1] = arrRcrd[GROUP_NAME_INDEX];
            Log.debugLog(className, "getGDFs", "", "", "Adding Group name in array = " + tempArrGDFDetails[i + 1][1]);
          }
          else if(arrRcrd[HEADER_INDEX].equals("Graph"))
          {
            countGraph++;
            Log.debugLog(className, "getGDFs", "", "", "Updating Graph count = " + countGraph);
          }
        }

        if(lastModDateTime.equals(""))
        {
          File fileObj = openFile(gdfFileName);
          long lastModifiedTime = fileObj.lastModified();
          Date dateLastModified = new Date(lastModifiedTime);
          SimpleDateFormat smt = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
          lastModDateTime = smt.format(dateLastModified);
        }

        tempArrGDFDetails[i + 1][2] = lastModDateTime;
        Log.debugLog(className, "getGDFs", "", "", "Adding last modified date in array = " + tempArrGDFDetails[i + 1][2]);

        tempArrGDFDetails[i + 1][3] = "" + countGraph;
        Log.debugLog(className, "getGDFs", "", "", "Adding Graph count in array = " + tempArrGDFDetails[i + 1][3]);
      }
      return tempArrGDFDetails;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getGDFs", "", "", "Exception - ", e);
      return null;
    }
  }


  // Return the GDF Graph details in 2d array
  public String[][] getGDFGraphDetails(String customGDFName)
  {
    Log.debugLog(className, "getGDFGraphDetails", "", "", "Method Starts. GDF Name = " + customGDFName);

    try
    {
      Vector vecCustomGDFData = getCustomGDFDetailsInVec(customGDFName);
      if(vecCustomGDFData == null)
      {
        Log.debugLog(className, "getGDFGraphDetails", "", "", "GDF File not found, It may be correpted.");
        return null;
      }

      // this array contain group info(0 -> group name, 1 -> group ID, 3 -> group type, 4 -> # of group, 5 -> NA) at zeroth index
      // NA is to keep array size consistent
      String[][] arrCustomGDFDetails = new String[vecCustomGDFData.size()][8];
      String[] arrRcrd = null;

      for(int i = 0; i < vecCustomGDFData.size(); i++)
      {
        arrRcrd = null;
        arrRcrd = rptUtilsBean.strToArrayData(vecCustomGDFData.elementAt(i).toString(), "|");

        arrCustomGDFDetails[i][0] = arrRcrd[0];
        arrCustomGDFDetails[i][1] = arrRcrd[1];
        arrCustomGDFDetails[i][2] = arrRcrd[2];
        arrCustomGDFDetails[i][3] = arrRcrd[3];
        arrCustomGDFDetails[i][4] = arrRcrd[4];
        arrCustomGDFDetails[i][5] = arrRcrd[5];
        arrCustomGDFDetails[i][6] = arrRcrd[6];
        arrCustomGDFDetails[i][7] = arrRcrd[7];
      }

      return arrCustomGDFDetails;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getGDFGraphDetails", "", "", "Exception - ", e);
      return null;
    }
  }


  // Return the GDF Graph details in vector
  private Vector getCustomGDFDetailsInVec(String customGDFName)
  {
    Log.debugLog(className, "getCustomGDFDetailsInVec", "", "", "Method Starts. GDF Name = " + customGDFName);

    Vector vecData = null;
    Vector vecCustomGDFData = new Vector();
    String[] arrRcrd = null;

    try
    {
      String gdfFileWithPath = "";

      Log.debugLog(className, "getCustomGDFDetailsInVec", "", "", "Is reading from hot file: " + isReadFromHotFile);

      if(isReadFromHotFile)
        gdfFileWithPath = getCustomGDFPath() + getBAKGDFName(customGDFName);
      else
        gdfFileWithPath = getCustomGDFPath() + getGDFName(customGDFName);

      vecData = readFile(gdfFileWithPath);

      if(vecData == null)
      {
        Log.debugLog(className, "getCustomGDFDetailsInVec", "", "", "GDF File not found, It may be correpted.");
        return null;
      }

      int countGraph = 0;
      for (int j = 0; j < vecData.size(); j++)
      {
        arrRcrd = null;
        arrRcrd = rptUtilsBean.strToArrayData(vecData.elementAt(j).toString(), "|");

        String dataLine = "";
        if(arrRcrd[HEADER_INDEX].equals("Group"))
        {
          //This code is added for new version of gdf(#2.1) for group description
          if(arrRcrd.length > 6)
          {
            dataLine = arrRcrd[GROUP_NAME_INDEX] + "|" + arrRcrd[GROUP_ID_INDEX] + "|" + arrRcrd[GROUP_TYPE_INDEX] + "|" + arrRcrd[GRAPH_COUNT_INDEX] + "|" + arrRcrd[GROUP_DESC_INDEX] + "|" + arrRcrd[METRICS_NAME_INDEX] +"|" + arrRcrd[HIERARCHICAL_PATH_INDEX] + "|NA";
          }
          else
          {
            dataLine = arrRcrd[GROUP_NAME_INDEX] + "|" + arrRcrd[GROUP_ID_INDEX] + "|" + arrRcrd[GROUP_TYPE_INDEX] + "|" + arrRcrd[GRAPH_COUNT_INDEX] + "|" + DEFAULT_GROUP_DESCRIPTION + "|NA|NA|NA";
          }
          vecCustomGDFData.add(dataLine);
          Log.debugLog(className, "getCustomGDFDetailsInVec", "", "", "Adding Group detail to vector = " + dataLine);
        }
        else if(arrRcrd[HEADER_INDEX].equals("Graph"))
        {
          // This code added for new version of gdf(#2.1) for group description
          if(arrRcrd.length > 8)
          {
            String pdfName = getPDFNameFromPDFId(arrRcrd[PDF_ID_INDEX_IN_GDF]);

            dataLine = arrRcrd[GRAPH_NAME_INDEX] + "|" + arrRcrd[GRAPH_ID_INDEX] + "|" + arrRcrd[GRAPH_TYPE_INDEX] + "|" + arrRcrd[DATA_TYPE_INDEX] + "|" + arrRcrd[GRAPH_FORMULA_INDEX] + "|" + arrRcrd[GRAPH_STATE] + "|" + pdfName + "|" + arrRcrd[Graph_DESC_INDEX];
          }
          else
          {
            dataLine = arrRcrd[GRAPH_NAME_INDEX] + "|" + arrRcrd[GRAPH_ID_INDEX] + "|" + arrRcrd[GRAPH_TYPE_INDEX] + "|" + arrRcrd[DATA_TYPE_INDEX] + "|" + arrRcrd[GRAPH_FORMULA_INDEX] + "|NA|" + "-" + "|" + DEFAULT_GRAPH_DESCRIPTION;
          }
          vecCustomGDFData.add(dataLine);
          Log.debugLog(className, "getCustomGDFDetailsInVec", "", "", "Adding data line to vector = " + dataLine);
        }
      }

      return vecCustomGDFData;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getCustomGDFDetailsInVec", "", "", "Exception - ", e);
      return null;
    }
  }

  //This method returns PDF name from PDF id, PDF name will come from $NS_WDIR/pdf/ directory.
  private String getPDFNameFromPDFId(String pdfId)
  {
    Log.debugLog(className, "getPDFNameFromPDFId", "", "", "Method Starts. PDF Id = " + pdfId);

    String[] arrListOfPDF = getPDFs();
    String pdfName = "None";

    for(int i = 0; i < arrListOfPDF.length; i++)
    {
      Vector vecTempPDFData = readFile(getPDFPath() + arrListOfPDF[i] + PDF_FILE_EXTN);

      String dataLine = "";
      for(int ii = 0; ii < vecTempPDFData.size(); ii++)
      {
        dataLine = vecTempPDFData.elementAt(ii).toString();

        if(dataLine.startsWith("PDF"))
        {
          String[] arrTemp = rptUtilsBean.strToArrayData(dataLine, "|");
          String tmpPDFId = arrTemp[PDF_ID_INDEX];

          if(tmpPDFId.equals(pdfId))
          {
            pdfName = arrListOfPDF[i];
            Log.debugLog(className, "getPDFNameFromPDFId", "", "", "got mapping of PDF File = " + pdfName + ",  with " + pdfId);
            break;
          }//end of inner if
        }//end of external if
      }//end of inner for
    }//end of external for
    Log.debugLog(className, "getPDFNameFromPDFId", "", "", "returning PDF Name = " + pdfName);
    return pdfName;
  }

  // return the updated group data line
  private String updateGroupLine(String groupDataLine, int updatedGraphCount)
  {
    Log.debugLog(className, "updateGroupLine", "", "", "Method Starts. Group data line = " + groupDataLine + ", updated group count = " + updatedGraphCount);

    try
    {
      String strUpdatedDataLine = "";
      String[] arrGroupData = rptUtilsBean.strToArrayData(groupDataLine, "|");

      arrGroupData[GRAPH_COUNT_INDEX] = "" + updatedGraphCount;

      for(int i = 0; i < arrGroupData.length; i++)
      {
        if(i == 0)
          strUpdatedDataLine = arrGroupData[i];
        else
          strUpdatedDataLine = strUpdatedDataLine + "|" + arrGroupData[i];
      }

      return strUpdatedDataLine;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "updateGroupLine", "", "", "Exception - ", e);
      return null;
    }
  }

  // return the group info line
  // this is to create Vector Group type for Log Monitors
  private String getGroupInfoLine(String groupName, String groupDesc, boolean isCreatingForLogMonitor, String strMetricsName)
  {
    Log.debugLog(className, "getGroupInfoLine", "", "", "Method Starts. Group name = " + groupName + ", group description = " + groupDesc + ", isCreatingForLogMonitor: " + isCreatingForLogMonitor);

    try
    {
      String groupLine = "";
      if(isCreatingForLogMonitor)
        groupLine = "Group|" + groupName + "|" + getUniqueGroupId() + "|vector|0|0|" + strMetricsName + "|" + getHieraricalPath() + "|"  + groupDesc + "\n\n";
      else
        groupLine = "Group|" + groupName + "|" + getUniqueGroupId() + "|scalar|0|0|" + strMetricsName + "|" + getHieraricalPath() + "|"  + groupDesc + "\n\n";

      return groupLine;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getGroupInfoLine", "", "", "Exception - ", e);
      return null;
    }
  }


  // This function return the unique group ID of custom monitor
  // our convention is to assign custom monitors group id to 10000
  // but we allow user to assign id for user defined custom monitor to 20000
  private String getUniqueGroupId()
  {
    Log.debugLog(className, "getUniqueGroupId", "", "", "Method Starts.");

    try
    {
      ArrayList arrListOfCustomGDF = getAllCustomGDF();
      ArrayList arrListGroupId = new ArrayList();
      String uniqueGroupId = "";
      String[] arrRcrd = null;

      for(int i = 0; i < arrListOfCustomGDF.size(); i++)
      {
        Vector vecData = getDataFromGdf(arrListOfCustomGDF.get(i).toString());

        if(vecData == null)
        {
          Log.debugLog(className, "getUniqueGroupId", "", "", "GDF File not found, It may be correpted, file name = " + arrListOfCustomGDF.get(i).toString());
          continue;
        }

        for (int j = 0; j < vecData.size(); j++)
        {
          arrRcrd = null;
          arrRcrd = rptUtilsBean.strToArrayData(vecData.elementAt(j).toString(), "|");

          if(arrRcrd[HEADER_INDEX].equals("Group"))
          {
            arrListGroupId.add(arrRcrd[GROUP_ID_INDEX]);
            Log.debugLog(className, "getUniqueGroupId", "", "", "Adding group ID to array list = " + arrRcrd[GROUP_ID_INDEX]);
            break;
          }
        }
      }

      if((arrListGroupId != null) && (arrListGroupId.size() > 0))
      {
        int[] arrStrTmp = new int[arrListGroupId.size()];
        for(int i = 0; i < arrListGroupId.size(); i++)
          arrStrTmp[i] = Integer.parseInt(arrListGroupId.get(i).toString());

        Arrays.sort(arrStrTmp);

        int maxGroupId = arrStrTmp[arrStrTmp.length - 1];

        if(maxGroupId < USER_DEFINED_CUSTOM_MON_ID)
          uniqueGroupId = "" + (USER_DEFINED_CUSTOM_MON_ID + 1);
        else
          uniqueGroupId = "" + (maxGroupId + 1);
      }
      else
        uniqueGroupId = "" + (USER_DEFINED_CUSTOM_MON_ID + 1);

      return uniqueGroupId;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getUniqueGroupId", "", "", "Exception - ", e);
      return null;
    }
  }

  /***********************************************************************************************/
  //                              Utility function(s) called from JSP                            //
  /***********************************************************************************************/

  // Delete methods
  // This delete the existing gdf
  public boolean deleteGDF(String gdfName)
  {
    Log.debugLog(className, "deleteGDF", "", "", "GDF Name = " +  gdfName);
    deleteGDF(gdfName, GDF_BAK_EXTN);
    return(deleteGDF(gdfName, GDF_FILE_EXTN));
  }

  // This delete the  gdf backup if present
  public boolean deleteBakGDF(String gdfName)
  {
    Log.debugLog(className, "deleteBakGDF", "", "", "GDF Name = " +  gdfName);
    return(deleteGDF(gdfName, GDF_BAK_EXTN));
  }

  // This delete the existing GDF
  private boolean deleteGDF(String gdfName, String fileExtn)
  {
    Log.debugLog(className, "deleteGDF", "", "", "GDF File = " + gdfName + "." + fileExtn);
    String tmptFile = "";

    try
    {
      if(fileExtn.equals(GDF_FILE_EXTN))
        tmptFile = getCustomGDFPath() + gdfName + fileExtn;
      else if(fileExtn.equals(GDF_BAK_EXTN))
        tmptFile = getCustomGDFPath() + getBAKGDFName(gdfName);

      File fileObj = new File(tmptFile);
      if(fileObj.exists())
      {
        boolean success = fileObj.delete();
        if (!success)
        {
          Log.errorLog(className, "deleteGDF", "", "", "Error in deleting gdf file (" + tmptFile + ")");
          return false;
        }
      }
      else
      {
        Log.errorLog(className, "deleteGDF", "", "", "GDF file (" + tmptFile + ") does not exist.");
      }
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "deleteGDF", "", "", "Exception in deletion of gdf (" + tmptFile + ") - ", e);
      return false;
    }
    return true;
  }


  // Make Bak file of GDF
  // All edit/changes are done in Backup file
  public boolean createGDFBackup(String gdfName)
  {
    Log.debugLog(className, "createGDFBackup", "", "", "GDF Name = " +  gdfName);
    String gdfFileName = "";

    try
    {
      gdfFileName = getCustomGDFPath() + gdfName + GDF_FILE_EXTN;
      File gdfFileObj = new File(gdfFileName);

      if(!gdfFileObj.exists())
      {
        Log.errorLog(className, "createGDFBackup", "", "", "Source gdf does not exist. GDF name = " + gdfName);
        return false;
      }

      String gdfBakFileName = getCustomGDFPath() + getBAKGDFName(gdfName);

      if(copyGDFFile(gdfFileName, gdfBakFileName, "", "", "", false, false, "") == false)
      {
        Log.errorLog(className, "createGDFBackup", "", "", "Error in creating Bak of GDF. GDF file is " + gdfFileName + ". Backup file is " + gdfBakFileName);
        return false;
      }
      return(true);
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "createGDFBackup", "", "", "Exception in creating gdf backup (" + gdfFileName + ") - ", e);
      return false;
    }
  }


  // Copy source gdf file to gdf file.
  private boolean copyGDFFile(String srcFileName, String destFileName, String destName, String groupName, String destDesc, boolean updCreDate, boolean updModDate, String strMetricsName)
  {
    Log.debugLog(className, "copyGDFFile", "", "", "Method called. source file name = " + srcFileName + ", destination file name = " + destFileName + ", destination name = " + destName + ", destination description = " + destDesc);

    try
    {
      File fileSrc = new File(srcFileName);
      File fileDest = new File(destFileName);

      Vector vecSourceFileData = readFile(srcFileName);
      ArrayList arrListGraphId = new ArrayList();
      boolean lastModDateFlag = false;
      boolean maxGraphId = false;

      if(!fileSrc.exists())
      {
        Log.errorLog(className, "copyGDFFile", "", "", "Source file does not exists. Filename = " + srcFileName);
        return false;
      }

      if(fileDest.exists())
        fileDest.delete();
      fileDest.createNewFile();

      if(rptUtilsBean.changeFilePerm(fileDest.getAbsolutePath(), "netstorm", "netstorm", "775") == false)
        return false;

      // To check gdf is compatible with current software or not
      String dataLine = "";
      for(int i = 0; i < vecSourceFileData.size(); i++)
      {
        dataLine = vecSourceFileData.elementAt(i).toString();

        if(dataLine.startsWith(LAST_MODIFIED_DATE))
          lastModDateFlag = true;
        else if(dataLine.startsWith("#LAST_MAX_ASSIGNED_GRAPH_ID"))
          maxGraphId = true;
        else if(dataLine.startsWith("Graph"))
        {
          String[] arrTemp = rptUtilsBean.strToArrayData(dataLine, "|");

          arrListGraphId.add(arrTemp[GRAPH_ID_INDEX]);
          Log.debugLog(className, "copyGDFFile", "", "", "Adding graph id in arraylist = " + arrTemp[GRAPH_ID_INDEX]);
        }
      }

      FileInputStream fin = new FileInputStream(fileSrc);
      BufferedReader br = new BufferedReader(new InputStreamReader(fin));

      FileOutputStream fout = new FileOutputStream(fileDest, true);  // append mode
      PrintStream pw = new PrintStream(fout);

      if(!lastModDateFlag && !maxGraphId)
      {
        if(arrListGraphId.size() > 0)
        {
          int[] arrStrTmp = new int[arrListGraphId.size()];
          for(int i = 0; i < arrListGraphId.size(); i++)
            arrStrTmp[i] = Integer.parseInt(arrListGraphId.get(i).toString());

          Arrays.sort(arrStrTmp);

          int maxAssignedGraphId = arrStrTmp[arrStrTmp.length - 1];

          pw.println("#LAST_MAX_ASSIGNED_GRAPH_ID = " + maxAssignedGraphId);
        }
        else
        {
          pw.println(LAST_MAX_ASSIGNED_GRAPH_ID);
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

        if(str.startsWith("Group"))
        {
          if(!destDesc.equals(""))
          {
            String[] arrTemp = rptUtilsBean.strToArrayData(str, "|");

            if(arrTemp.length > 6)
            {
              if(strMetricsName.trim().equals(""))
                strMetricsName = arrTemp[6];
              str = arrTemp[0] + "|" + groupName + "|" + arrTemp[2] + "|" + arrTemp[3] + "|" +  arrTemp[4]+ "|" + arrTemp[5] + "|" + strMetricsName + "|" + getHieraricalPath() + "|" + destDesc;
            }
            else
            {
              if(strMetricsName.trim().equals(""))
                strMetricsName = arrTemp[6];
              
              str = arrTemp[0] + "|" + groupName + "|" + arrTemp[2] + "|" + arrTemp[3] + "|" +  arrTemp[4]+ "|" + arrTemp[5] + "|" + strMetricsName + "|NA|" + DEFAULT_GROUP_DESCRIPTION;
            }
            Log.debugLog(className, "copyGDFFile", "", "", "After str = " + str);
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
      Log.stackTraceLog(className, "copyGDFFile", "", "", "Exception - ", e);
      return false;
    }
  }

  // This will delete selected graphs from a gdf
  // selected graphs are passed as array of index.
  public boolean deleteGraphFromGDF(String gdfName, String[] arrGraphIdx, boolean reSequence)
  {
    Log.debugLog(className, "deleteGraphsFromGDF", "", "", "GDF Name = " + gdfName);

    String gdfNameWithPath = getCustomGDFPath() + getBAKGDFName(gdfName);

    try
    {
      Vector vecData = readFile(gdfNameWithPath);
      Vector vecGraphData = readGraphData(vecData);

      File gdfFileObj = new File(gdfNameWithPath);

      if(gdfFileObj.exists())
        gdfFileObj.delete(); // Delete gdf file

      gdfFileObj.createNewFile(); // Create new gdf file

      if(rptUtilsBean.changeFilePerm(gdfFileObj.getAbsolutePath(), "netstorm", "netstorm", "775") == false)
        return false;

      FileOutputStream fout = new FileOutputStream(gdfFileObj, true);  // append mode
      PrintStream pw = new PrintStream(fout);
      String status = "";

      String dataLine = "";
      for(int i = 0; i < vecData.size(); i++)
      {
        dataLine = vecData.elementAt(i).toString();

        if(dataLine.startsWith("Graph"))
          continue;

        if(dataLine.startsWith(LAST_MODIFIED_DATE))
        {
          dataLine = LAST_MODIFIED_DATE + rptUtilsBean.getCurDateTime();
          pw.println(dataLine);
        }
        else if(dataLine.startsWith("Group"))
        {
          String updatedGroupLine = updateGroupLine(dataLine, (vecGraphData.size() - arrGraphIdx.length));
          pw.println(updatedGroupLine);
        }
        else
          pw.println(dataLine);
      }

      // for Graphs
      for(int i = 0; i < vecGraphData.size(); i++)
      {
        status = "";
        for(int k = 0; k < arrGraphIdx.length; k++)
        {
          if(i == (int)Integer.parseInt(arrGraphIdx[k]))
          {
            status = "true";
            break;
          }
        }

        if(!status.equals("true"))
          pw.println(vecGraphData.elementAt(i).toString());
      }

      pw.close();
      fout.close();

      // for resequence graph Id
      if(reSequence)
      {
        Log.debugLog(className, "deleteGraphsFromGDF", "", "", "resequencing all graph id.");
        if(!reSequenceGraphId(gdfNameWithPath))
        {
          Log.debugLog(className, "deleteGraphsFromGDF", "", "", "Error in resequencing graph id in gdf.");
          return false;
        }
      }

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "deleteGraphsFromGDF", "", "", "Exception in deleting graph from gdf of (" + gdfNameWithPath + ") - ", e);
      return false;
    }
  }

  /**
   * To add graph in GDF.
   * @param gdfName
   * @param graphDetail
   * @param before : to add graph after or before the selected graph in GUI, by default it is false. we assume value of this flag true
   *                 for adding graph before, and false for adding graph after the selected graph.
   * @param reSequence : true - do resequence of all the ID of graphs in GDF.
   * @param rowId : id of the selected graph. when we click simply on add button is will be -1.
   * @return
   */
  public boolean addGraphToGDF(String gdfName, String graphDetail, boolean before, boolean reSequence, String rowId)
  {
    Log.debugLog(className, "addGraphToGDF", "", "", "GDF Name = " + gdfName);
    return(addGraphToGDF(gdfName, graphDetail, before, reSequence, rowId, false));
  }

  // Add graph to gdf file. Add is done to backup file only
  public boolean addGraphToGDF(String gdfName, String graphDetail, boolean before, boolean reSequence, String rowId, boolean fileType)
  {
    Log.debugLog(className, "addGraphToGDF", "", "", "GDF Name = " + gdfName + ", graph detail = " + graphDetail + ", Row Id = " + rowId);
    String gdfNameWithPath = "";
    boolean isGraphAdded = false;

    if(fileType)
      gdfNameWithPath = getCustomGDFPath() + gdfName + GDF_FILE_EXTN;
    else
      gdfNameWithPath = getCustomGDFPath() + getBAKGDFName(gdfName);

    try
    {
      // Read file's content
      Vector vecRtpl = readFile(gdfNameWithPath);
      File gdfFileObj = new File(gdfNameWithPath);

      gdfFileObj.delete(); // Delete gdf file
      gdfFileObj.createNewFile(); // Create new gdf file

      if(rptUtilsBean.changeFilePerm(gdfFileObj.getAbsolutePath(), "netstorm", "netstorm", "775") == false)
        return false;

      if(!gdfFileObj.exists())
      {
        Log.errorLog(className, "addGraphToGDF", "", "", "GDF file does not exist. GDF filename is - " + gdfNameWithPath);
        return(false);
      }

      FileOutputStream fout = new FileOutputStream(gdfFileObj, true);  // append mode
      PrintStream pw = new PrintStream(fout);

      String dataLine = "";
      String graphId = "";

      String pdfName = "";
      String pdfId = "";

      for(int i = 0; i < vecRtpl.size(); i++)
      {
        dataLine = vecRtpl.get(i).toString();

        if(dataLine.startsWith("#LAST_MAX_ASSIGNED_GRAPH_ID"))
        {
          String[] arrDataTemp = rptUtilsBean.strToArrayData(dataLine, "=");
          graphId = "" + (Integer.parseInt(arrDataTemp[1].trim()) + 1);

          pw.println("#LAST_MAX_ASSIGNED_GRAPH_ID = " + graphId);

          // create graph line here
          graphDetail = getGraphDetail(graphDetail, graphId, gdfName);
        }
        else if(dataLine.startsWith(LAST_MODIFIED_DATE))
        {
          dataLine = LAST_MODIFIED_DATE + rptUtilsBean.getCurDateTime();
          pw.println(dataLine);
        }
        else if(dataLine.startsWith("Group"))
        {
          String[] arrGroupDataTemp = rptUtilsBean.strToArrayData(dataLine, "|");
          int updatedGraphCount = (Integer.parseInt(arrGroupDataTemp[GRAPH_COUNT_INDEX]) + 1);

          String updatedGroupLine = updateGroupLine(dataLine, updatedGraphCount);
          pw.println(updatedGroupLine);
        }
        else if(dataLine.startsWith("Info"))
        {
          pw.println(dataLine);
        }
        else if(dataLine.startsWith("Graph"))
        {
          if((!rowId.equals("-1")) && (!isGraphAdded))    //if row id is not -1 then either graph is to be added before or after from the selected graph, this will be desided on the basis of before flag.
          {
            String[] arrGroupDataTemp = rptUtilsBean.strToArrayData(dataLine, "|");

            if(arrGroupDataTemp[GRAPH_ID_INDEX].equals(rowId + ""))
            {
              if(!before)  //we have to add a new graph after the selected graph
              {
                pw.println(dataLine);
                pw.println(graphDetail);  // Append the new graph after to the selected graph.

                Log.debugLog(className, "addGraphToGDF", "", "", "adding new graph after graph id = " + rowId + ". New graph detail = " + graphDetail);

                isGraphAdded = true;
              }
              else  //we have to add a new graph before the selected graph
              {
                pw.println(graphDetail);  // Append the new graph before than the selected graph.

                Log.debugLog(className, "addGraphToGDF", "", "", "adding new graph before graph id = " + rowId + ". New graph detail = " + graphDetail);

                isGraphAdded = true;
                pw.println(dataLine);
              }
            }
            else
              pw.println(dataLine);
          }
          else   //if Add button is clicked from GUI, then it simply add new graph in the end of the GDF.
          {
            pw.println(dataLine);
          }
        }
        else
        {
          pw.println(dataLine);
        }
      }

      if(!isGraphAdded)
        pw.println(graphDetail);  // Append the new graph to the end of file

      pw.close();
      fout.close();

      // for resequence graph Id
      if(reSequence)
      {
        Log.debugLog(className, "addGraphToGDF", "", "", "resequencing all graph id.");
        if(!reSequenceGraphId(gdfNameWithPath))
        {
          Log.debugLog(className, "addGraphToGDF", "", "", "Error in resequencing graph id in gdf.");
          return false;
        }
      }

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "addGraphToGDF", "", "", "Exception in adding graph to gdf (" + gdfNameWithPath + ") - ", e);
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
  private String getGraphDetail(String graphDetail, String graphId, String gdfName)
  {
    Log.debugLog(className, "getGraphDetail", "", "", "Method Called.");
    String pdfId = "";
    String[] arrGraphDataTemp = rptUtilsBean.strToArrayData(graphDetail, "|");

    if(arrGraphDataTemp == null || arrGraphDataTemp.length != 7)
    {
      Log.errorLog(className, "getGraphDetail", "", "", "Graph data line has insufficient data = " + graphDetail);
      return "";  //handel this condition from addGraphInGDF
    }

    if(arrGraphDataTemp[4].equals("None"))  //if pdf is not associated with GDF.
    {
      pdfId = "-1";
      Log.debugLog(className, "getGraphDetail", "", "", "pdf is not associated with " + gdfName + " hence, setting PDF Id = " + pdfId);
    }
    else   //if pdf is associated with GDF, then fetch PDF's id from the associated PDF's Name.
    {
      pdfId = getPDFIDFromPDFName(arrGraphDataTemp[5]);
    }

    graphDetail = "Graph|" + arrGraphDataTemp[0] + "|" + graphId + "|" + arrGraphDataTemp[1] + "|" + arrGraphDataTemp[2] + "|-|" + arrGraphDataTemp[3] + "|0|" + arrGraphDataTemp[4] + "|" + pdfId + "|-1|NA|NA" + "|" + arrGraphDataTemp[6];

    Log.debugLog(className, "getGraphDetail", "", "", "adding  = " + graphDetail + ", in = " + gdfName);

    return graphDetail;
  }

  /**
   * To resequence all the graph id in the GDF.
   * @param gdfNameWithPath
   * @return
   */
  private boolean reSequenceGraphId(String gdfNameWithPath)
  {
    Log.debugLog(className, "reSequenceGraphId", "", "", "Method Called. gdf name = " + gdfNameWithPath);

    try
    {
      // Read file's content
      Vector vecData = readFile(gdfNameWithPath);
      Vector vecResequenceData = new Vector();
      int graphIdCounter = 1;

      File gdfFileObj = new File(gdfNameWithPath);

      gdfFileObj.delete(); // Delete gdf file
      gdfFileObj.createNewFile(); // Create new gdf file

      if(rptUtilsBean.changeFilePerm(gdfFileObj.getAbsolutePath(), "netstorm", "netstorm", "775") == false)
        return false;

      if(!gdfFileObj.exists())
      {
        Log.errorLog(className, "reSequenceGraphId", "", "", "GDF file does not exist. GDF filename is - " + gdfNameWithPath);
        return false;
      }

      String dataLine = "";

      for(int i = 0; i < vecData.size(); i++)
      {
        dataLine = vecData.get(i).toString();

        if(dataLine.startsWith("Graph")) //serching for graph id and keep adding them in an ArrayList.
        {
          String[] arrDataTemp = rptUtilsBean.strToArrayData(dataLine, "|");
          arrDataTemp[GRAPH_ID_INDEX] = "" + graphIdCounter; // reassign graph Id

          vecResequenceData.add(rptUtilsBean.strArrayToStr(arrDataTemp, "|"));
          graphIdCounter++;
        }
      }

      FileOutputStream fout = new FileOutputStream(gdfFileObj, true);  // append mode
      PrintStream pw = new PrintStream(fout);

      for(int i = 0; i < vecData.size(); i++)
      {
        String fileDataLine = vecData.get(i).toString().trim();

        if(fileDataLine.startsWith("#LAST_MAX_ASSIGNED_GRAPH_ID"))
        {
          String[] arrDataTemp = rptUtilsBean.strToArrayData(fileDataLine, "=");
          pw.println("#LAST_MAX_ASSIGNED_GRAPH_ID = " + (graphIdCounter - 1));
        }
        else if(!fileDataLine.startsWith("Graph"))
        {
          pw.println(fileDataLine);
          Log.debugLog(className, "reSequenceGraphId", "", "", "Adding fileDataLine in gdf = " + fileDataLine);
        }
      }

      // add updated (resequenced) graph id in file
      for(int i = 0; i < vecResequenceData.size(); i++)
      {
        String fileDataLine = vecResequenceData.get(i).toString().trim();

        pw.println(fileDataLine);
        Log.debugLog(className, "reSequenceGraphId", "", "", "Adding updated graph line in gdf = " + fileDataLine);
      }

      pw.close();
      fout.close();
      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "reSequenceGraphId", "", "", "Exception = ", e);
      return false;
    }
  }

  /**
   * To get pdf id from pdf name
   * @param pdfName
   * @return : pdf id of the passed pdf.
   */
  private String getPDFIDFromPDFName(String pdfName)
  {
    Log.debugLog(className, "getPDFIDFromPDFName", "", "", "Method called.");
    String pdfDataLine = "";
    try
    {
      if(pdfName.equals("None"))
        return "-1";

      String tempPDFName = getPDFPath() + pdfName + PDF_FILE_EXTN;
      Vector vecPDFInfo = readFile(tempPDFName);
      Log.debugLog(className, "getPDFIDFromPDFName", "", "", "reading " + tempPDFName);

      String pdfId = "-1";
      for(int i = 0; i < vecPDFInfo.size(); i++)
      {
        pdfDataLine = vecPDFInfo.elementAt(i).toString();

        if(pdfDataLine.startsWith("PDF"))
        {
          String[] arrTemp = rptUtilsBean.strToArrayData(pdfDataLine, "|");
          pdfId = arrTemp[PDF_ID_INDEX];

          Log.debugLog(className, "getPDFIDFromPDFName", "", "", "PDF Name = " + pdfName + ", PDF Id = " + pdfId);
          break;
        }
      }
      return pdfId;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getPDFIDFromPDFName", "", "", "Exception = ", e);
      return "-1";
    }
  }

  public boolean createNewGDF(String gdfName, String groupName, String groupDesc, String strMetricsName)
  {
    return(createNewGDF(gdfName, groupName, groupDesc, false, strMetricsName));
  }

  // Create new gdf. New gdf is always create as bak file.
  public boolean createNewGDF(String gdfName, String groupName, String groupDesc, boolean checkFileType, String strMetricsName)
  {
    return createNewGDF(gdfName, groupName, groupDesc, checkFileType, false, strMetricsName);
  }

  // this is to create Vector Group type for Log Monitors
  public boolean createNewGDF(String gdfName, String groupName, String groupDesc,  boolean checkFileType, boolean isCreatingForLogMonitor, String strMetricsName)
  {
    Log.debugLog(className, "createNewGDF", "", "", "GDF Name =" + gdfName + ", Group Name = " + groupName + ", Group Description = " + groupDesc + ", isCreatingForLogMonitor: " + isCreatingForLogMonitor);
    String gdfNameWithPath = "";

    try
    {
      // This will allow user to create gdf file with .gdf extn.
      if(checkFileType == true)
        gdfNameWithPath = getCustomGDFPath() + gdfName + GDF_FILE_EXTN;
      else
        gdfNameWithPath = getCustomGDFPath() + getBAKGDFName(gdfName);

      File gdfFileObj = new File(gdfNameWithPath);

      if(gdfFileObj.exists())
      {
        Log.errorLog(className, "createNewGDF", "", "", "GDF already exists. GDF name = " + gdfName);
        return false;
      }

      gdfFileObj.createNewFile();

      if(rptUtilsBean.changeFilePerm(gdfFileObj.getAbsolutePath(), "netstorm", "netstorm", "775") == false)
        return false;

      FileOutputStream fout = new FileOutputStream(gdfFileObj, true);  // append mode
      PrintStream pw = new PrintStream(fout);

      pw.println(LAST_MAX_ASSIGNED_GRAPH_ID);
      pw.println(LAST_MODIFIED_DATE + rptUtilsBean.getCurDateTime());
      pw.println(GDF_INFO_HDR_LINE);

      pw.println(getGroupInfoLine(groupName, groupDesc, isCreatingForLogMonitor, strMetricsName));

      pw.close();
      fout.close();
      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "createNewGDF", "", "", "Exception in creating new gdf (" + gdfName + ") - ", e);
      return false;
    }
  }


  // Save gdf will delete gdf file and copy bak file to gdf file
  // Bak is not deleted as GUI needs this file. Mod date/time is also updated in gdf file.
  public boolean saveGDF(String gdfName, String groupName, String groupDesc, String strMetricsName)
  {
    Log.debugLog(className, "saveGDF", "", "", "GDF File =" + gdfName + ", group name = " + groupName + ", group description = " + groupDesc + ", strMetricsName = " + strMetricsName);

    try
    {
      // First check if Bak file is existing or not. If not create new gdf Bak file
      String gdfBakFileName = getCustomGDFPath() + getBAKGDFName(gdfName);

      File gdfBakFileObj = new File(gdfBakFileName);

      if(!gdfBakFileObj.exists())
        createNewGDF(gdfName, groupName, groupDesc, strMetricsName);

      // Now copy Bak file to gdf file to save the changes
      String gdfFileName = getCustomGDFPath() + gdfName + GDF_FILE_EXTN;

      if(copyGDFFile(gdfBakFileName, gdfFileName, "", groupName, groupDesc, false, true, strMetricsName) == false)
      {
        Log.errorLog(className, "saveGDF", "", "", "Error in copying Bak to gdf (" + gdfFileName + ")");
        return false;
      }

      // Now copy gdf to Bak file so that Bak is same as gdf file
      if(copyGDFFile(gdfFileName, gdfBakFileName, "", "", "", false, false, strMetricsName) == false)
      {
        Log.errorLog(className, "saveGDF", "", "", "Error in copying gdf to Bak (" + gdfFileName + ")");
        return false;
      }

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "saveGDF", "", "", "Exception in saving gdf (" + gdfName + ") - ", e);
      return false;
    }
  }


  // To check if gdf is modified or note after last save was done
  public boolean isGDFModified(String gdfName)
  {
    Log.debugLog(className, "isGDFModified", "", "", "Method called. GDF Name = " + gdfName);
    try
    {
      String gdfFileName = getCustomGDFPath() + gdfName + GDF_FILE_EXTN;
      String gdfBakFileName = getCustomGDFPath() + getBAKGDFName(gdfName);

      Vector vecGDFData = readFile(gdfFileName);
      Vector vecBakGDFData = readFile(gdfBakFileName);

      String strGDFDate = "";
      String strBakGDFDate = "";

      String dataLine = "";
      for(int i = 0; i < vecGDFData.size(); i++)
      {
        dataLine = vecGDFData.get(i).toString();

        if(dataLine.startsWith(LAST_MODIFIED_DATE))
        {
          String[] arrTemp = rptUtilsBean.strToArrayData(dataLine, "=");
          strGDFDate = arrTemp[1].trim();
          break;
        }
      }

      for(int i = 0; i < vecBakGDFData.size(); i++)
      {
        dataLine = vecBakGDFData.get(i).toString();

        if(dataLine.startsWith(LAST_MODIFIED_DATE))
        {
          String[] arrTemp = rptUtilsBean.strToArrayData(dataLine, "=");
          strBakGDFDate = arrTemp[1].trim();
          break;
        }
      }

      Log.debugLog(className, "isGDFModified", "", "", "Modification Date/Time for GDF and Bak file are - " + strGDFDate + " and " + strBakGDFDate);

      if(!strGDFDate.equals(""))  //in old gdf modification date was not added, so our assumption is that if bak file exits, GDF was being modified.
      {
        // Compare modified file
        if((rptUtilsBean.convertDateToMilliSec(strGDFDate)) == (rptUtilsBean.convertDateToMilliSec(strBakGDFDate)))
          return false;
      }

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "isGDFModified", "", "", "Exception - ", e);
      return false;
    }
  }


  // Make copy of a gdf with new name and group name
  // Also creation date and modification date is new gdf is set to current date/time
  public boolean copyGDF(String strSrcGDFName, String strDestGDFName, String strGroupName, String strGroupDesc)
  {
    Log.debugLog(className, "copyGDF", "", "", "Source GDF Name =" +  strSrcGDFName + ", Destination GDF Name = " + strDestGDFName + ", Group Name = " + strGroupName + ", Group Description = " + strGroupDesc);

    try
    {
      String gdfSrcFileName = getCustomGDFPath() + strSrcGDFName + GDF_FILE_EXTN;
      String gdfDestFileName = getCustomGDFPath() + strDestGDFName + GDF_FILE_EXTN;

      File gdfSrcFileObj = new File(gdfSrcFileName);
      if(!gdfSrcFileObj.exists())
      {
        Log.errorLog(className, "copyGDF", "", "", "Source gdf does not exist. GDF name = " + strSrcGDFName);
        return false;
      }

      File gdfDestFileObj = new File(gdfDestFileName);
      if(gdfDestFileObj.exists())
      {
        Log.errorLog(className, "copyGDF", "", "", "Destination gdf already exists. GDF name = " + gdfDestFileName);
        return false;
      }

      FileInputStream fin = new FileInputStream(gdfSrcFileObj);
      BufferedReader br = new BufferedReader(new InputStreamReader(fin));

      FileOutputStream fout = new FileOutputStream(gdfDestFileObj, true);  // append mode
      PrintStream pw = new PrintStream(fout);

      String dataLine = "";
      while((dataLine = br.readLine()) != null)
      {
        if(dataLine.startsWith(LAST_MODIFIED_DATE))
        {
          dataLine = LAST_MODIFIED_DATE + rptUtilsBean.getCurDateTime();
        }
        if(dataLine.startsWith("Group"))
        {
          String[] arrTemp = rptUtilsBean.strToArrayData(dataLine, "|");
          dataLine = "Group|" + strGroupName + "|" + getUniqueGroupId() + "|" + arrTemp[GROUP_TYPE_INDEX] + "|" + arrTemp[GRAPH_COUNT_INDEX] + "|" + arrTemp[NUM_VECTOR_GROUP_INDEX] + "|" + arrTemp[METRICS_NAME_INDEX] + "|" + arrTemp[HIERARCHICAL_PATH_INDEX] +"|"+ strGroupDesc;
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
      Log.stackTraceLog(className, "copyGDF", "", "", "Exception in copying gdf (" + strSrcGDFName + ") - ", e);
      return false;
    }
  }

  /**
   * To update graphs in the gdf.
   * @param gdfName : name of the gdf.
   * @param id : id of the graph, that is to be updated
   * @param dataLine : passed through jsp, format of the line would be graphName|scalar|DataType|formula|pdfName|graphDesc.
   * @return
   */
  public boolean updateGDF(String gdfName, String id, String dataLine)
  {
    Log.debugLog(className, "updateGDF", "", "", "gdf name = " + gdfName + ", graph id = " + id + ", data line = " + dataLine);

    String gdfNameWithPath = getCustomGDFPath() + getBAKGDFName(gdfName);

    Vector vecData = readFile(gdfNameWithPath);

    File gdfFileObj = new File(gdfNameWithPath);

    try
    {
      if(!gdfFileObj.exists())
      {
        Log.debugLog(className, "updateGDF", "", "", "gdf name = " + gdfNameWithPath + " does not exist.");
        return false;
      }
      else
      {
        gdfFileObj.delete();
        gdfFileObj.createNewFile();
      }

      if(rptUtilsBean.changeFilePerm(gdfFileObj.getAbsolutePath(), "netstorm", "netstorm", "775") == false)
        return false;

      FileOutputStream fout = new FileOutputStream(gdfFileObj, true);  // append mode
      PrintStream pw = new PrintStream(fout);

      for(int i = 0; i < vecData.size(); i++)
      {
        if(vecData.elementAt(i).toString().startsWith("Graph"))
        {
          String[] arrTemp = rptUtilsBean.strToArrayData(vecData.elementAt(i).toString(), "|");
          String tempID = arrTemp[GRAPH_ID_INDEX].trim();

          if(tempID.equals(id))
          {
            String[] arrGraphDataTemp = rptUtilsBean.strToArrayData(dataLine, "|");
            String pdfId = getPDFIDFromPDFName(arrGraphDataTemp[5]);

            String graphDetail = "Graph|" + arrGraphDataTemp[0] + "|" + id + "|" + arrGraphDataTemp[1] + "|" + arrGraphDataTemp[2] + "|-|" + arrGraphDataTemp[3] + "|0|" + arrGraphDataTemp[4] + "|" + pdfId + "|-1|NA|NA" + "|" + arrGraphDataTemp[6];

            Log.debugLog(className, "updateGDF", "", "", "writing updated line in file = " + graphDetail);
            pw.println(graphDetail);
          }
          else
          {
            Log.debugLog(className, "updateGDF", "", "", "writing line in file = " + vecData.elementAt(i).toString());
            pw.println(vecData.elementAt(i).toString());
          }
        }
        else
        {
          Log.debugLog(className, "updateGDF", "", "", "writing line in file = " + vecData.elementAt(i).toString());
          pw.println(vecData.elementAt(i).toString());
        }
      }

      pw.close();
      fout.close();
      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "updateGDF", "", "", "Exception = ", e);
      return false;
    }
  }

  private Vector getGDFDataInVector(Vector vecaddGroup, ArrayList arrGDFList, String strGDFPath)
  {
    Log.debugLog(className, "getGDFDataInVector", "", "", "");
    try
    {
      Vector vecRedFile = new Vector();

      for(int i = 0; i < arrGDFList.size(); i++)
      {
        String strPath = strGDFPath + arrGDFList.get(i).toString() + GDF_FILE_EXTN;
        vecRedFile  = readFile(strPath);

         if(vecRedFile == null)
          continue;
        String groupName = "";
        for(int k = 0; k < vecRedFile.size(); k++)
        {
          if(vecRedFile.get(k).toString().startsWith("Group"))
          {
            String[] tempSplit = rptUtilsBean.strToArrayData(vecRedFile.get(k).toString().trim(), "|");
            groupName = tempSplit[1] + "|" + tempSplit[2] + "|";
          }
          else if(vecRedFile.get(k).toString().startsWith("Graph"))
          {
            String[] tempString = rptUtilsBean.strToArrayData(vecRedFile.get(k).toString().trim(), "|");
            vecaddGroup.add(groupName + tempString[1] + "|" + tempString[2]);
          }
        }
      }
      Collections.sort(vecaddGroup);

      return vecaddGroup;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getGDFDataInVector", "", "", "Exception = ", e);
      return null;
    }
  }

  /*
   * Method to get the GDF by a prefix.
   * If prefix string is blank then it will get all details of all gdfs
   * prefix string can be a complete name of GDF. in this case it return the details of one GDF
   */
  public GdfDTO getGDFData (GdfDTO gdfDTO)
  {
    Log.debugLog(className, "getGDFData", "", "", "Method Starts.");
    //gdfDTOOutPut.gdfPrefix = gdfDTOInput.gdfPrefix;
    try
    {
      ArrayList arrListOfCustomGDF = getAllCustomGDF();
      ArrayList <GDFData> arrGDFData = new ArrayList <GDFData> ();

      for(int i = 0; i < arrListOfCustomGDF.size(); i++)
      {
        if(gdfDTO.gdfPrefix.trim().equals("") || arrListOfCustomGDF.get(i).toString().startsWith(gdfDTO.gdfPrefix))
        {
          GDFData gdfData = new GDFData();

          Vector vecData = null;
          Vector vecCustomGDFData = new Vector();
          String[] arrRcrd = null;
          String gdfFileWithPath = "";

          gdfFileWithPath = getCustomGDFPath() + getGDFName(arrListOfCustomGDF.get(i).toString());
          gdfData.gdfName = arrListOfCustomGDF.get(i).toString();
          vecData = readFile(gdfFileWithPath);

          if(vecData == null)
          {
            Log.debugLog(className, "getCustomGDFDetailsInVec", "", "", "GDF File not found, It may be correpted.");
            continue;
          }

          ArrayList <GDFGraphData> arrGraphData = new ArrayList <GDFGraphData> ();
          for (int j = 0; j < vecData.size(); j++)
          {
            GDFGraphData gdfGraphData = new GDFGraphData();
            arrRcrd = null;
            arrRcrd = rptUtilsBean.strToArrayData(vecData.elementAt(j).toString(), "|");

            String dataLine = "";
            if(arrRcrd[HEADER_INDEX].equals("Group"))
            {
              //This code is added for new version of gdf(#2.1) for group description
              if(arrRcrd.length > 6)
              {
                gdfData.groupName = arrRcrd[GROUP_NAME_INDEX];
                gdfData.groupId = arrRcrd[GROUP_ID_INDEX];
                gdfData.groupType = arrRcrd[GROUP_TYPE_INDEX];
                gdfData.graphCount = arrRcrd[GRAPH_COUNT_INDEX];
                gdfData.groupDescription = arrRcrd[GROUP_DESC_INDEX];
              }
              else
              {
                gdfData.groupName = arrRcrd[GROUP_NAME_INDEX];
                gdfData.groupId = arrRcrd[GROUP_ID_INDEX];
                gdfData.groupType = arrRcrd[GROUP_TYPE_INDEX];
                gdfData.graphCount = arrRcrd[GRAPH_COUNT_INDEX];
                gdfData.groupDescription = gdfData.groupName;
              }
            }
            else if(arrRcrd[HEADER_INDEX].equals("Graph"))
            {
              // This code added for new version of gdf(#2.1) for group description
              if(arrRcrd.length > 8)
              {
                String pdfName = getPDFNameFromPDFId(arrRcrd[PDF_ID_INDEX_IN_GDF]);
                gdfGraphData.graphName = arrRcrd[GRAPH_NAME_INDEX];
                gdfGraphData.graphId = arrRcrd[GRAPH_ID_INDEX];
                gdfGraphData.graphType = arrRcrd[GRAPH_TYPE_INDEX];
                gdfGraphData.graphDataType = arrRcrd[DATA_TYPE_INDEX];
                gdfGraphData.graphFormula = arrRcrd[GRAPH_FORMULA_INDEX];
                gdfGraphData.graphState = arrRcrd[GRAPH_STATE];
                gdfGraphData.pdfName = pdfName;
                gdfGraphData.graphDescription = arrRcrd[Graph_DESC_INDEX];
              }
              else
              {
                //String pdfName = getPDFNameFromPDFId(arrRcrd[PDF_ID_INDEX_IN_GDF]);
                gdfGraphData.pdfName = "-";
                gdfGraphData.graphName = arrRcrd[GRAPH_NAME_INDEX];
                gdfGraphData.graphId = arrRcrd[GRAPH_ID_INDEX];
                gdfGraphData.graphType = arrRcrd[GRAPH_TYPE_INDEX];
                gdfGraphData.graphDataType = arrRcrd[DATA_TYPE_INDEX];
                gdfGraphData.graphFormula = arrRcrd[GRAPH_FORMULA_INDEX];
                gdfGraphData.graphState = "NA";
                gdfGraphData.graphDescription = gdfGraphData.graphName;
              }
              arrGraphData.add(gdfGraphData);
            }
            else
            {
              gdfData.groupVectorNames.add(vecData.elementAt(j).toString());
            }
          }
          gdfData.GDFGraphData = arrGraphData;
          arrGDFData.add(gdfData);
        }
      }
      gdfDTO.GdfData = arrGDFData;
      return gdfDTO;
    }
    catch(Exception ex)
    {
      gdfDTO.status = false;
      gdfDTO.errorMsg = ex.getMessage();
      gdfDTO.detailErrorMsg = ex.toString();
      Log.stackTraceLog(className, "getGDFData", "", "", "Exception - ", ex);
      return gdfDTO;
    }
  }

  public String createExternalMonitorGDF(GdfDTO gdfDTO)
  {
    Log.debugLog(className, "createExternalMonitorGDF", "", "", "Method Called.");
    String GrpId = "";
    try
    {
      ArrayList <GDFData> arrGDFData = gdfDTO.GdfData;
      GDFData gdfData = arrGDFData.get(0);
      String gdfNameWithPath = getCustomGDFPath() + "ed_" + gdfData.groupName + GDF_FILE_EXTN;

      File gdfFileObj = new File(gdfNameWithPath);

      GrpId = getUniqueGroupId();
      if(gdfFileObj.exists())
      {
        Log.errorLog(className, "createNewGDF", "", "", "GDF already exists. GDF name = " + "ed_" + gdfData.groupName);
        return GrpId;
      }

      gdfFileObj.createNewFile();

      if(rptUtilsBean.changeFilePerm(gdfFileObj.getAbsolutePath(), "netstorm", "netstorm", "775") == false)
      {
        Log.errorLog(className, "createExternalMonitorGDF", "", "", "Error in changing permission.");
      }

      FileOutputStream fout = new FileOutputStream(gdfFileObj, true);  // append mode
      PrintStream pw = new PrintStream(fout);
      pw.println(GDF_INFO_HDR_LINE);
      String groupLine = "Group|" + gdfData.groupName + "|" + GrpId + "|" + gdfData.groupType + "|" + gdfData.graphCount + "|0|-|-|" + gdfData.groupDescription;
      pw.println(groupLine);
      pw.println("");
      for(int jx = 0; jx < gdfData.GDFGraphData.size(); jx++)
      {
        GDFGraphData gdfGraphData = gdfData.GDFGraphData.get(jx);
        String graphDetail = "Graph|" + gdfGraphData.graphName + "|" + (jx+1) + "|scalar|" + gdfGraphData.graphDataType + "|-|-|0|NA|-1|-1|NA|NA|" + gdfGraphData.graphDescription;
        pw.println(graphDetail);
      }

      pw.close();
      fout.close();
      return GrpId;
    }
    catch (Exception ex)
    {
      Log.stackTraceLog(className, "createExternalMonitorGDF", "", "", "Exception - ", ex);
      return GrpId;
    }
  }

/**
 * @method name getGroupGraphIDs
 * @purpose This method return Group Name, Group ID, Graph Name and Graph ID.
 * @return 2D Array
 */
  public String[][] getGroupGraphIDs()
  {
    Log.debugLog(className, "getGroupGraphIDs", "", "", "");

    try
    {
      Vector vecaddGroup = new Vector();

      vecaddGroup = getGDFDataInVector(vecaddGroup, getAllCustomGDF(), getCustomGDFPath());

      if(vecaddGroup == null)
        vecaddGroup.setSize(0);

      vecaddGroup = getGDFDataInVector(vecaddGroup, getAllCustomGDF(getStandardGDFPath()), getStandardGDFPath());

      if(vecaddGroup == null)
        vecaddGroup.setSize(0);

      String[][] arrDetails = new String [vecaddGroup.size()][vecaddGroup.get(0).toString().trim().split("|").length];
      for (int ii = 0; ii < vecaddGroup.size(); ii++)
      {
        String[] tempString = rptUtilsBean.strToArrayData(vecaddGroup.get(ii).toString().trim(), "|");

        for (int kk = 0; kk < tempString.length; kk++)
        {
          arrDetails[ii][kk] = tempString[kk];
        }
      }

      return arrDetails;
    }
    catch (Exception e)
    {
      Log.stackTraceLog(className, "getGroupGraphIDs", "", "", "Exception = ", e);
      return new String[0][0];
    }
  }
  
  /**
   * This method checks whether gdf file exists or not if exists then it return true else false
   * @param gdfFileName
   * @return
   */
   public boolean isGDFExists(String gdfFileName)
   {
     Log.debugLog(className, "isGDFExists", "", "", "Method called");
     try
     {
       String gdfFileWithPath = getCustomGDFPath() + gdfFileName + GDF_FILE_EXTN;
       File fileObj = new File(gdfFileWithPath);
     
       if(fileObj.exists())
         return true;
       
       return false;
     }
     catch(Exception ex)
     {
       Log.stackTraceLog(className, "isGDFExists", "", "", "Exception - ", ex);
       return false;
     }
   }
   
   /**
    * This method checks whether gdf file exists or not if exists then it return true else false
    * @param gdfFileName
    * @return
    */
    public String getGDFVectorName(String gdfFileName)
    {
      Log.debugLog(className, "isGDFExists", "", "", "Method called");
      try
      {
        String gdfFileWithPath = getCustomGDFPath() + gdfFileName + GDF_FILE_EXTN;
        Vector vecData = rptUtilsBean.readReport(gdfFileWithPath);
        
        if(vecData == null)
          return null;
        else
        {
          for(int i = 0; i < vecData.size() ; i++)
          {
            if(vecData.elementAt(i).toString().startsWith("#"))
              continue;
   
            String[] gdfDetails = rptUtilsBean.split(vecData.elementAt(i).toString(), "|");
            
            if(gdfDetails[0].equals("Group"))
            {
              if(gdfDetails.length != 9)
                continue;
              return gdfDetails[7];
            }
          }
        } 
        return null;
      }
      catch(Exception ex)
      {
        Log.stackTraceLog(className, "isGDFExists", "", "", "Exception - ", ex);
        return null;
      }
    }
   
    
  public static void main(String[] args)
  {
    
  }

  /**
   * @param hieraricalPath the hieraricalPath to set
   */
  public void setHieraricalPath(String hieraricalPath) {
    this.hieraricalPath = hieraricalPath;
  }

  /**
   * @return the hieraricalPath
   */
  public String getHieraricalPath() {
    
    if(hieraricalPath == null || hieraricalPath.trim().equals(""))
      return "NA";
    return hieraricalPath;
  }
}

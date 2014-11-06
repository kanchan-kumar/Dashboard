/*--------------------------------------------------------------------
 @Name    : ImportExternalData.java
 @Author  : Ravi Kant Sharma
 @Purpose : To read external data from csv file
 @Modification History:
 Ravi Kant Sharma --> Initial Version
 
 ----------------------------------------------------------------------*/
package pac1.Bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

import pac1.Bean.Log;
import pac1.Bean.rptUtilsBean;

public class CSVFileData
{
  private String fileCSVPath = "";
  private String graphDataType = "";
  private double trInterval = 10000.0;
  // Indexes of Info Line
  private transient final int HEADER_INDEX = 0; // Header info index
  private String[] arrGraphNames;
  // graph name must start from 2 index
  private transient final int GRAPH_NAME_START_INDEX = 2;
  private transient final int START_DATE_FIELD = 0;
  private transient final int DATE_TIME_FIELD = 1;
  private String[] arrStartDate;
  private Date externalDataStartTime;
  private Date externalDataEndTime;
  private int HOUR_INDEX = 0;
  private int MINUTE_INDEX = 1;
  private int SECOND_INDEX = 2;
  private String[] arrDateTime;
  private static String className = "CSVFileData";
  private int startIndex = 1;
  private int endIndex = -1;
  private double sampleCount = 0;
  private StringBuffer error;
  String externalDataDateFormat;

  public CSVFileData()
  {
    
  }

  // check added file is binary
  public static boolean isBinaryFile(File f) throws FileNotFoundException, IOException
  {
    try
    {
      FileInputStream in = new FileInputStream(f);
      int size = in.available();
      if (size > 1024)
        size = 1024;
      byte[] data = new byte[size];
      in.read(data);
      in.close();

      int ascii = 0;
      int other = 0;

      for (int i = 0; i < data.length; i++)
      {
        if (i == 0)
        {
          byte b = data[i];
          if (b < 0x09)
            return true;

          if (b == 0x09 || b == 0x0A || b == 0x0C || b == 0x0D)
            ascii++;
          else if (b >= 0x20 && b <= 0x7E)
            ascii++;
          else
            other++;
        }
        else
          break;
      }

      if (other == 0)
        return false;

      return (ascii + other) * 100 / other > 95;
    }
    catch(Exception ex)
    {
      return true;
    }
  }
  
  public CSVFileData(String fileCSVPath)
  {
    try
    {
      Log.debugLog(className, "CSVFileData", "", "", "Method Called. FilePath = " + fileCSVPath);
      this.fileCSVPath = fileCSVPath;
      error = new StringBuffer();
      File csvFile = new File(fileCSVPath);
      if (csvFile == null || !csvFile.exists())
      {
        error.append("File " + fileCSVPath + " not found, Please select another file.");
        setCSVError(error);
        return;
      }
      boolean isBinary = isBinaryFile(csvFile);
      if(isBinary)
      {
        error.append("File is not a csv file, Please check file.");
        setCSVError(error);
        return;
      }  
      
      FileInputStream fis = new FileInputStream(csvFile);
      InputStreamReader isr = new InputStreamReader(fis);
      BufferedReader br = new BufferedReader(isr);
      int count = 0;
      String strLine;
      while ((strLine = br.readLine()) != null)
      {
        if (count == 0)
        {
          if (strLine.startsWith("#"))
          {
            Log.debugLog(className, "CSVFileData", "", "", "Line number " + count + " is commented.");
            count = count - 1;
            continue;
          }

          if (strLine.length() == 0 || strLine.trim().equals(""))
          {
            count = count - 1;
            continue;
          }

          if (count == HEADER_INDEX)
          {
            String[] tempGraphNames = strLine.split(",");
            
            if (tempGraphNames != null && tempGraphNames.length != 0)
            {
              arrGraphNames = new String[tempGraphNames.length - GRAPH_NAME_START_INDEX];
              int graphCount = 0;
              for (int i = GRAPH_NAME_START_INDEX; i < tempGraphNames.length; i++)
              {
                String graphName = tempGraphNames[i].trim();
                if (graphName.equals(""))
                {
                  Log.errorLog(className, "CSVFileData", "", "", "unable to process header line. column " + (i + 2) + " is empty.");
                  error.append("Data cannot to process header line. column " + (i + 1) + " is empty.");
                  setCSVError(error);
                  return;
                }
                arrGraphNames[graphCount] = graphName;
                graphCount++;
              }
            }
            else
            {
              error.append("Error in process header line. File may be corrupt or invalid format.");
              setCSVError(error);
            }
            
            if(arrGraphNames != null)
              setArrGraphNames(arrGraphNames);
            else
            {
              error.append("Header line is not correct. file may be corrupt or invalid format.  Please see error logs.");
              setCSVError(error);
              Log.errorLog(className, "CSVFileData", "", "", "Header line is not correct. file may be corrupt or invalid format.");
              setCSVError(error);
            }
          }
          else
          {
            error.append("Header line is not correct. file may be corrupt or invalid format.  Please see error logs.");
            setCSVError(error);
            Log.errorLog(className, "CSVFileData", "", "", "Header line is not correct. file may be corrupt or invalid format.");
          }
        }
        else
        {
          br.close();
          fis.close();
          if(arrGraphNames != null)
            setArrGraphNames(arrGraphNames);
          else
            error.append("Header line is not correct. file may be corrupt or invalid format.");
          return;
        }
        count++;
      }
    }
    catch (Exception ex)
    {
      error.append("No Graphs are available to add. File may be corrupt or in invalid format. Please check csv file");
      setCSVError(error);
      //Log.stackTraceLog(className, "CSVFileData", "", "", "Exception - ", ex);
    }
  }

  // used for setting csv info
  public CSVFileData(int trInterval, String filePath, String strTRTimeZone, String externalDataTimeZone, String externalDataDateFormat)
  {
    try
    {
      Log.debugLog(className, "CSVFileData", "", "", "Constructor Called. filePath = " + filePath + ", TestRun Time Zone = " + strTRTimeZone + ", File Time Zone = " + externalDataTimeZone + ", File Date format = " + externalDataDateFormat);
      this.fileCSVPath = filePath;
      error = new StringBuffer();
      this.trInterval = trInterval;

      this.externalDataDateFormat = externalDataDateFormat;

      if (externalDataTimeZone.trim().equals("Same time Zone"))
      {
        externalDataTimeZone = strTRTimeZone;
      }
      else if (externalDataTimeZone.trim().equals("Synchronize with test start time"))
      {
        externalDataTimeZone = strTRTimeZone;
      }
      else if(externalDataTimeZone.trim().equals("Synchronize with test start time (With offset)"))
      {
        externalDataTimeZone = strTRTimeZone;
      }
      
      // converting timezone as per user selected option
      //convertTimeByTimeZone(strTRTimeZone, externalDataTimeZone);
    
      initCSVFile();

      // validate all date and time in file are they correct
      //dateTimeValidation();
      /*if(!error.toString().trim().equals(""))
      {
        Log.errorLog(className, "CSVFileData", "", "", "Error in validating csv file date and time. Date and time format in csv file may not be correct.\nPlease check added csv file.");
        error.append("Error in validating csv file date and time. Date and time format in csv file may not be correct.\nPlease check added csv file.");
        setCSVError(error);
        return;
      }*/
      
      setArrGraphNames(arrGraphNames);
      setArrDateTime(arrDateTime);
      setArrStartDate(arrStartDate);
      setCSVError(error);
      setTrInterval(trInterval);
    }
    catch (Exception ex)
    {
      //Log.stackTraceLog(className, "CSVFileData", "", "", "Exception - ", ex);
      error.append("Error in process data, CSV file may not be correct. Please check csv file date and time format.");
    }
  }

  // loading csv data in memory
  public void initCSVFile()
  {
    try
    {
      Log.debugLog(className, "initCSVFile", "", "", "Method Called.");
      Vector externalFileData = readFile();
      if (!error.toString().trim().equals(""))
      {
        Log.errorLog(className, "initCSVFile", "", "", "Error in loading csv file - " + error.toString());
        return;
      }
      if (externalFileData == null)
      {
        Log.errorLog(className, "initCSVFile", "", "", "No data found in csv file.");
        error.append("No graph data found, File may be corrupt or empty. Please check added csv file.");
        return;
      }

      arrStartDate = new String[externalFileData.size() - 1];
      arrDateTime = new String[externalFileData.size() - 1];
      double[][] arrExternalGraphData = new double[externalFileData.size() - 1][arrGraphNames.length];
      setSampleCount(arrExternalGraphData.length);
      int col = 0;
      int dataLineLength = arrGraphNames.length + 2;
      int dateCount = 0;
      int timeCount = 0;
      for (int i = 1; i < externalFileData.size(); i++)
      {
        String data = externalFileData.get(i).toString();
        String[] arrDataFields = rptUtilsBean.strToArrayData(data, ",");
        // check for bad header line
        if (dataLineLength <= arrDataFields.length)
        {
          for (int j = 0; j < arrDataFields.length; j++)
          {
            if (j == START_DATE_FIELD)
            {
              arrStartDate[dateCount] = arrDataFields[j];
              dateCount++;
            }
            else if (j == DATE_TIME_FIELD)
            {
              arrDateTime[timeCount] = arrDataFields[j];
              timeCount++;
            }
            else if (j >= GRAPH_NAME_START_INDEX)
            {
              try
              {
                arrExternalGraphData[i - 1][col] = Double.parseDouble(arrDataFields[j].trim());
                col++;
                if (col == arrGraphNames.length)
                  col = 0;

              }
              catch (Exception ex)
              {
                error.append("Bad data line found, CSV file data may be corrupt. Please See line number " + (i + 1));
                return;
              }
            }
          }
        }
        else
        {
          Log.errorLog(className, "initCSVFile", "", "", "Cannot process data, Line " + (i - 1) + " is invalid.");
          error.append("Bad data line found, CSV file may be corrupt. Please See line number " + (i + 1));
          return;
        }
      }
    }
    catch (Exception ex)
    {
      //Log.stackTraceLog(className, "initCSVFile", "", "", "Exception - ", ex);
      error.append("No data found, file may be corrupt or invalid format. Please check added csv file.");
    }
  }

  public String getGraphDataType()
  {
    return graphDataType;
  }
  
  public void setGraphDataType(String graphDataType)
  {
    this.graphDataType = graphDataType;
  }
  
  // Methods for reading the CSV File
  private Vector readFile()
  {
    Log.debugLog(className, "readFile", "", "", "Method called. ");
    try
    {
      Vector vecFileData = new Vector();
      String strLine;
      File customGDF = new File(fileCSVPath);
      if (customGDF == null || !customGDF.exists())
      {
        Log.errorLog(className, "readFile", "", "", "CSV file not found.");
        return null;
      }

      FileInputStream fis = new FileInputStream(customGDF);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));
      int count = 0;
      while ((strLine = br.readLine()) != null)
      {
        if (strLine.startsWith("#"))
        {
          Log.debugLog(className, "", "", "", "Line number " + count + " is commented.");
          continue;
        }

        if (strLine.length() == 0)
          continue;

        vecFileData.add(strLine);
        if (count == HEADER_INDEX)
        {
          String[] tempGraphNames = strLine.split(",");
          arrGraphNames = new String[tempGraphNames.length - GRAPH_NAME_START_INDEX];
          int graphCount = 0;
          for (int i = GRAPH_NAME_START_INDEX; i < tempGraphNames.length; i++)
          {
            String graphName = tempGraphNames[i].trim();
            if (graphName.equals(""))
            {
              Log.errorLog(className, "readFile", "", "", "unable to process header line. column " + (i + 2) + " is empty.");
              error.append("Cannot to process header line. column " + (i + 1) + " is empty.");
              return null;
            }
            arrGraphNames[graphCount] = graphName;
            graphCount++;
          }
          count++;
        }
      }

      br.close();
      fis.close();

      return vecFileData;
    }
    catch (Exception e)
    {
      //Log.stackTraceLog(className, "readFile", "", "", "Exception - ", e);
      error.append("Error in reading csv file. File may be corrupt or invalid format. Please check csv file.");
      return null;
    }
  }

  public String[] getArrGraphNames()
  {
    return arrGraphNames;
  }

  public void setArrGraphNames(String[] arrGraphNames)
  {
    this.arrGraphNames = arrGraphNames;
  }

  public String[] getArrStartDate()
  {
    return arrStartDate;
  }

  public void setArrStartDate(String[] arrStartDate)
  {
    this.arrStartDate = arrStartDate;
  }

  public String[] getArrDateTime()
  {
    return arrDateTime;
  }

  public void setArrDateTime(String[] arrDateTime)
  {
    this.arrDateTime = arrDateTime;
  }

  public StringBuffer getCSVError()
  {
    return error;
  }

  public void setCSVError(StringBuffer error)
  {
    this.error = error;
  }

  public int getEndIndex()
  {
    return endIndex;
  }

  public void setEndIndex(int endIndex)
  {
    this.endIndex = endIndex;
  }

  public int getStartIndex()
  {
    return startIndex;
  }

  public void setStartIndex(int startIndex)
  {
    this.startIndex = startIndex;
  }

  public double getSampleCount()
  {
    return sampleCount;
  }

  public void setSampleCount(double sampleCount)
  {
    this.sampleCount = sampleCount;
  }

  public double getTrInterval()
  {
    return trInterval;
  }

  public void setTrInterval(double trInterval)
  {
    this.trInterval = trInterval;
  }

  // validation check for date and time is correct in external data file
  public void dateTimeValidation()
  {
    try
    {
      Log.debugLog(className, "dateTimeValidation", "", "", "Method Called.");
      if (arrStartDate != null && arrDateTime != null && arrStartDate.length == arrDateTime.length)
      {
        for (int i = 0; i < arrStartDate.length; i++)
        {
          String date = arrStartDate[i];
          String time = arrDateTime[i];
          String[] arrTimearray = rptUtilsBean.strToArrayData(time, ":");
          if(arrTimearray == null)
          {
            error.append("Date and time may not be in correct format. Please check csv file.");
            return;
          }
          
          for (int j = 0; j < arrTimearray.length; j++)
          {
            if (j == HOUR_INDEX)
            {
              int hours = Integer.parseInt(arrTimearray[j]);
              if (hours > 24)
              {
                error.append("Hours can not be greater than 23, CSV file may be corrupt.\nPlease See line number " + (i + 2));
                return;
              }
            }
            else if (j == MINUTE_INDEX)
            {
              int minutes = Integer.parseInt(arrTimearray[j]);
              if (minutes > 60)
              {
                error.append("minutes can not be greater than 59, CSV file may be corrupt.\nPlease See line number " + (i + 2));
                return;
              }
            }
            else if (j == SECOND_INDEX)
            {
              int seconds = Integer.parseInt(arrTimearray[j]);
              if (seconds > 60)
              {
                error.append("Seconds can not be greater than 59, CSV file may be corrupt.\nPlease See line number " + (i + 2));
                return;
              }
            }
          }

          String tempStartDateTime = arrStartDate[0] + " " + arrDateTime[0];
          if(tempStartDateTime.trim().equals(""))
          {
            error.append("Invalid date time format in file. Please check added csv file.");
            return;
          }
          
          DateFormat formatter = new SimpleDateFormat(externalDataDateFormat, Locale.ENGLISH);
          externalDataStartTime = (Date) formatter.parse(tempStartDateTime);

          String tempEndDateTime = arrStartDate[arrStartDate.length - 1] + " " + arrDateTime[arrDateTime.length - 1];
          externalDataEndTime = (Date) formatter.parse(tempEndDateTime);

          String dateTime = date + " " + time;
          int nextTimeToken = i + 1;
          Date tempDateTime = (Date) formatter.parse(dateTime);
          long miliseconds1 = 0;
          long miliseconds = tempDateTime.getTime();
          if (nextTimeToken < arrStartDate.length)
          {
            String nextDateTime = arrStartDate[nextTimeToken] + " " + arrDateTime[nextTimeToken];
            Date tempNextDateTime = (Date) formatter.parse(nextDateTime);
            miliseconds1 = tempNextDateTime.getTime();
            if (miliseconds <= miliseconds1)
            {
              // System.out.println("Valid Time ");
            }
            else
            {
              error.append("Data line is not correct, csv file may be corrupt. Please See data line number " + (i + 2));
              return;
            }
          }
          else
          {
            if (miliseconds <= miliseconds1)
            {
              // System.out.println("Valid Data");
            }
            else
            {
              if (i != (arrStartDate.length - 1))
              {
                error.append("Data line is not correct, csv file may be corrupt. Please See data line number " + (i + 2));
                return;
              }
            }
          }
        }
      }
      else
      {
        error.append("No data found for selected graphs. file may be corrupt or invalid format. Please check added file or error log.");
        Log.errorLog(className, "dateTimeValidation", "", "", "Invalid file format, Number of columns of date and time should be same.");
        return;
      }
    }
    catch (Exception ex)
    {
      //Log.stackTraceLog(className, "dateTimeValidation", "", "", "csv file data is not valid, csv file may be corrupt. Exception - ", ex);
      error.append("Date and time format in file are not correct. Please check csv file.");
      return;
    }
  }

  /*
   * This function read second data line from csv file
   */
  public String getSecondLine(File file)
  {
    try
    {
      FileInputStream fs = new FileInputStream(file);
      BufferedReader br = new BufferedReader(new InputStreamReader(fs));

      br.readLine();// ignore first line
      String secondLine = br.readLine();
      br.close();
      fs.close();
      return secondLine;
    }
    catch (java.io.FileNotFoundException e)
    {
      e.printStackTrace();
      return null;
    }
    catch (java.io.IOException e)
    {
      e.printStackTrace();
      return null;
    }
  }

  /*
   * This function read last line data line from csv file
   */
  public String getlastLine(File file)
  {
    try
    {
      Log.debugLog(className, "getlastLine", "", "", "Method Called.");
      RandomAccessFile fileHandler = new RandomAccessFile(file, "r");
      long fileLength = file.length() - 1;
      StringBuilder sb = new StringBuilder();

      for (long filePointer = fileLength; filePointer != -1; filePointer--)
      {
        fileHandler.seek(filePointer);
        int readByte = fileHandler.readByte();

        if (readByte == 0xA)
        {
          if (filePointer == fileLength)
          {
            continue;
          }
          else
          {
            break;
          }
        }
        else if (readByte == 0xD)
        {
          if (filePointer == fileLength - 1)
          {
            continue;
          }
          else
          {
            break;
          }
        }
        sb.append((char) readByte);
      }

      String lastLine = sb.reverse().toString();
      fileHandler.close();

      return lastLine;
    }
    catch (java.io.FileNotFoundException e)
    {
      e.printStackTrace();
      return null;
    }
    catch (java.io.IOException e)
    {
      e.printStackTrace();
      return null;
    }
  }

  private int[] getColIndex(String graphName, String[] dataType, String[] arrGraphNames)
  {
    int arrColIndx[] = null;
    try
    {
      int incrementindex = -1;
      for (int i = 0; i < arrGraphNames.length; i++)
      {
      	setGraphDataType(dataType[i]);
        if (arrGraphNames[i].equals(graphName))
        {
          if (dataType[i].equals("times"))
          {
            arrColIndx = new int[4];
            for (int j = 0; j < 4; j++)
            {
              arrColIndx[j] = ++incrementindex;
            }
          }
          if (dataType[i].equals("cumulative"))
          {
            arrColIndx = new int[1];
            ++incrementindex;
            arrColIndx[0] = incrementindex;
          }
          if (dataType[i].equals("rate"))
          {
            arrColIndx = new int[1];
            ++incrementindex;
            arrColIndx[0] = incrementindex;
          }
          if (dataType[i].equals("sample"))
          {
            arrColIndx = new int[1];
            incrementindex = ++incrementindex;
            arrColIndx[0] = incrementindex;
          }
        }
        else
        {
          if (dataType[i].equals("times"))
          {
            incrementindex = incrementindex + 4;
          }
          if (dataType[i].equals("cumulative"))
          {
            ++incrementindex;
          }
          if (dataType[i].equals("rate"))
          {
            ++incrementindex;
          }
          if (dataType[i].equals("sample"))
          {
            ++incrementindex;
          }
        }
      }
      return arrColIndx;
    }
    catch (Exception ex)
    {
      return null;
    }

  }

  /*
   * this function is for getting external graphs data by duration ArrayList
   * timeCriteria contains 0th index -> startDate, 1st index -> startTime, 2nd
   * Index -> endDate, 3rd Index -> endTime
   */
  public double[][] getGraphsDataByDuration(ArrayList timeCriteria, ArrayList graphsInfo)
  {
    try
    {
      Log.debugLog(className, "getGraphsDataByDuration", "", "", "Method Called. timeCriteria = " + timeCriteria + ", externalDataDateFormat = " + externalDataDateFormat);
      File CSVFiles = new File(fileCSVPath);
      String readingStartDateTime = "";
      boolean flagDiff = false;
      String readingEndDateTime = "";
      long diff = 0;

      if (timeCriteria.size() == 4)
      {
        readingStartDateTime = timeCriteria.get(0).toString().trim();
        flagDiff = Boolean.parseBoolean(timeCriteria.get(1).toString().trim());
        diff = Long.parseLong(timeCriteria.get(2).toString().trim());
        readingEndDateTime = timeCriteria.get(3).toString().trim();
      }
      else
      {
        Log.errorLog(className, "getGraphsDataByDuration", "", "", "No data match for selected graphs.");
        error = new StringBuffer();
        error.append("No data match for the selected graphs. Date and time of file may be not correct or invalid date line.\nPlease check added csv file.");
        setCSVError(error);
        return null;
      }

      if (readingStartDateTime.trim().equals("") || readingEndDateTime.trim().equals(""))
      {
        Log.errorLog(className, "getGraphsDataByDuration", "", "", "Reading Date Time = " + readingStartDateTime + ", Reading End Date = " + readingEndDateTime);
        error.append("No data match for selcted graphs. Date and time format of file may be not match. Please check added csv file.");
        setCSVError(error);
        return null;
      }

      DateFormat formatter = new SimpleDateFormat(externalDataDateFormat, Locale.ENGLISH);
      Date startDateFile = (Date) formatter.parse(readingStartDateTime);
      long readingStarttartTimeInMiliSeconds = startDateFile.getTime();
      Date endDateFile = (Date) formatter.parse(readingEndDateTime);
      long endTimeOfLastLine = endDateFile.getTime();
      long difference = endTimeOfLastLine - readingStarttartTimeInMiliSeconds;
      sampleCount = (double) difference / trInterval;
      sampleCount = Math.ceil(sampleCount);

      if (sampleCount <= 0)
      {
        Log.errorLog(className, "getGraphsDataByDuration", "", "", "Total Samaples = 0, No Data found for selected criteria.");
        error.append("No data match for selected graphs. Start time and end time of file may not be correct.\nPlease check csv file format.");
        setCSVError(error);
        return null;
      }

      String[] arrGraphNames = getArrGraphNames();
      String[] arrSelectedGraphs = null;
      String[] arrSelectedGraphDataType = null;
      if (graphsInfo.size() == 2)
      {
        arrSelectedGraphs = (String[]) graphsInfo.get(0);
        arrSelectedGraphDataType = (String[]) graphsInfo.get(1);
      }
      else
      {
        Log.errorLog(className, "getGraphsDataByDuration", "", "", "graphsInfo.size() = " + graphsInfo.size());
        error = new StringBuffer();
        error.append("No data match for selected graphs. Date and time fomat of file may not be correct.\nPlease check csv file format.");
        setCSVError(error);
        return null;
      }

      if (arrSelectedGraphDataType == null || arrSelectedGraphs == null)
      {
        Log.errorLog(className, "getGraphsDataByDuration", "", "", "arrSelectedGraphDataType = " + arrSelectedGraphDataType + ", arrSelectedGraphs = " + arrSelectedGraphs);
        error.append("No graphs are selected. Please select at least one graph.");
        setCSVError(error);
        return null;
      }

      int size = 0;
      for (int jj = 0; jj < arrSelectedGraphDataType.length; jj++)
      {
        if (arrSelectedGraphDataType[jj].equals("times"))
          size = size + 4;
        else
          size = size + 1;
      }

      if (sampleCount <= 0)
      {
        Log.errorLog(className, "getGraphsDataByDuration", "", "", "No data found for selected criteria.");
        error = new StringBuffer();
        error.append("No data match for selected graphs. Date and time format in file are not correct. Please check csv file.");
        setCSVError(error);
        return null;
      }

      if (size <= 0)
      {
        Log.errorLog(className, "getGraphsDataByDuration", "", "", "No data found for selected criteria.");
        error = new StringBuffer();
        error.append("No data match for selected graphs. Data type of graph/s are not correct. Please select correct data type for graph/s.");
        setCSVError(error);
        return null;
      }

      Log.debugLog(className, "getGraphsDataByDuration", "", "", "sampleCount = " + sampleCount + ", size = " + size);
      double[][] arrResultedData = new double[(int) sampleCount][size];
      // filling zero at every array location
      for (int i = 0; i < arrResultedData.length; i++)
      {
        Arrays.fill(arrResultedData[i], 0);
      }

      for (int jj = 0; jj < arrSelectedGraphDataType.length; jj++)
      {
        int minCount = 0;
        if (arrSelectedGraphDataType[jj].equals("times"))
        {
          String graphName = arrSelectedGraphs[jj];
          int[] arrcol = getColIndex(graphName, arrSelectedGraphDataType, arrSelectedGraphs);
          for (int kk = 0; kk < arrcol.length; kk++)
          {
            if (minCount == 1)
            {
              for (int j = 0; j < sampleCount; j++)
                arrResultedData[j][arrcol[kk]] = Double.MAX_VALUE;
            }
            
            minCount++;
          }
        }
      }

      FileInputStream fis = new FileInputStream(CSVFiles);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));
      int count = 0;
      String strLine = "";
      long nextSampleTime = readingStarttartTimeInMiliSeconds + ((long) trInterval);
      
      int SampleNumber = 0;
      ArrayList<Integer> arrSample = new ArrayList<Integer>();
      int temSampleCount = 0;
      int prevSample = 0;
      while ((strLine = br.readLine()) != null)
      {
        if (strLine.startsWith("#"))
        {
          Log.debugLog(className, "getGraphsDataByDuration", "", "", "Line number " + count + " is commented.");
          continue;
        }

        if (strLine.length() == 0 || strLine.trim().equals(""))
          continue;

        if (count != 0)
        {
          String[] arrDataLine = rptUtilsBean.strToArrayData(strLine, ",");
          if (arrDataLine.length <= 2)
          {
            Log.errorLog(className, "getGraphsDataByDuration", "", "", "File cannot processed, Bad data line found in file. Please check line number " + count + " in csv file.");
            return null;
          }
          
          String tempDate = arrDataLine[0].trim() + " " + arrDataLine[1].trim();
          
          Date dataDate = (Date) formatter.parse(tempDate);
          long nextDateInFile = dataDate.getTime();

          if (flagDiff)
            nextDateInFile = nextDateInFile + diff;
          else
            nextDateInFile = nextDateInFile - diff;

          if (endTimeOfLastLine < nextDateInFile)
            continue;

          if (nextDateInFile > nextSampleTime)
          {
            nextSampleTime = nextSampleTime + ((long) trInterval);
            prevSample = SampleNumber;
            SampleNumber++;
            arrSample.add(temSampleCount);
            temSampleCount = 0;
            
            //Checking data line is in current sample or not
            if(nextDateInFile > nextSampleTime)
            {
              while(true)
              {
                nextSampleTime = nextSampleTime + ((long) trInterval);
                SampleNumber++;
                arrSample.add(temSampleCount);
                temSampleCount = 0;
                if(nextDateInFile <= nextSampleTime)
                 break;
              }
            }
          }
          
          temSampleCount++;
 
          if (readingStarttartTimeInMiliSeconds <= nextDateInFile)
          {
            // check for bad data line file
            for (int ii = 2; ii < (arrGraphNames.length + 2); ii++)
            {
              String graphName = arrGraphNames[ii - 2];
              int[] arr = getColIndex(graphName, arrSelectedGraphDataType, arrSelectedGraphs);
              if (arr != null)
              {
                double dataValue = Double.parseDouble(arrDataLine[ii]);
                for (int kk = 0; kk < arr.length; kk++)
                {
                  if (graphDataType.equals("times"))
                  {
                    if (kk == 0)
                    {
                      if (SampleNumber < arrResultedData.length)
                        arrResultedData[SampleNumber][arr[kk]] = (arrResultedData[SampleNumber][arr[kk]] + dataValue);
                    }
                    else if (kk == 1)
                    {
                      // setting min value
                      if ((SampleNumber < arrResultedData.length) && arrResultedData[SampleNumber][arr[kk]] > dataValue)
                      {
                        arrResultedData[SampleNumber][arr[kk]] = dataValue;
                      }
                    }
                    else if (kk == 2)
                    {
                      // setting max value
                      if (SampleNumber < arrResultedData.length && arrResultedData[SampleNumber][arr[kk]] < dataValue)
                      {
                        arrResultedData[SampleNumber][arr[kk]] = dataValue;
                      }
                    }
                    else
                    {
                      if (SampleNumber < arrResultedData.length)
                      {
                        ++arrResultedData[SampleNumber][arr[kk]];
                      }
                    }
                  }
                  else if (graphDataType.equals("cumulative"))
                  {
                    if (SampleNumber < arrResultedData.length)
                      arrResultedData[SampleNumber][arr[kk]] = dataValue;
                  }
                  else
                  {
                    if (SampleNumber < arrResultedData.length)
                      arrResultedData[SampleNumber][arr[kk]] = (arrResultedData[SampleNumber][arr[kk]] + dataValue);
                  }
                }
              }
            }
          }
          else
          {
            continue;
          }
        }
        count++;
      }

      arrSample.add(temSampleCount);
      
      for (int jj = 0; jj < arrSelectedGraphDataType.length; jj++)
      {
        int minCount = 0;
        if (arrSelectedGraphDataType[jj].equals("times"))
        {
          String graphName = arrSelectedGraphs[jj];
          int[] arrcol = getColIndex(graphName, arrSelectedGraphDataType, arrSelectedGraphs);
          for (int j = 0; j < sampleCount; j++)
          {
            double requestCount = 0;
            for(int kk = arrcol.length ; kk >= 0 ; kk--)
            {
              if(kk == 3)
              {
                requestCount = arrResultedData[j][arrcol[kk]];
                
                //if request count is zero and this is not first sample then previous sample will repeat
                if(requestCount == 0  && j!= 0  )
                {
                  if(j < arrSample.size())
                  {
                    arrResultedData[j][arrcol[kk]] = 1;
                    arrResultedData[j][arrcol[kk - 1 ]] = arrResultedData[j - 1][arrcol[kk -3]];
                    arrResultedData[j][arrcol[kk - 2 ]] = arrResultedData[j - 1][arrcol[kk -3]];
                    arrResultedData[j][arrcol[kk - 3 ]] = arrResultedData[j - 1][arrcol[kk -3]];
                  }
                }
              }
              
              if(kk == 1)
              {
                //if request count is zero then set min value to zero
                if(arrResultedData[j][arrcol[kk]] == Double.MAX_VALUE)
                {
                  arrResultedData[j][arrcol[kk]] = 0;
                }
              }
              
              if(kk == 0)
              {
                if(requestCount > 0)
                  arrResultedData[j][arrcol[kk]] = arrResultedData[j][arrcol[kk]]/requestCount;
              }
            }
          }
        }
        else if (arrSelectedGraphDataType[jj].equals("sample"))
        {
          String graphName = arrSelectedGraphs[jj];
          int[] arrcol = getColIndex(graphName, arrSelectedGraphDataType, arrSelectedGraphs);
          for (int j = 0; j < sampleCount; j++)
          {
            for (int kk = 0; kk < arrcol.length; kk++)
            {
               if (j < arrSample.size() && (arrSample.get(j) != 0))
                 arrResultedData[j][arrcol[kk]] = arrResultedData[j][arrcol[kk]] / (arrSample.get(j));
               else
               {
                 if(j != 0 && j < arrSample.size())
                 {
                   arrResultedData[j][arrcol[kk]] = arrResultedData[j -1 ][arrcol[kk]];
                 }
               }
            }
          }
        }
        else if (arrSelectedGraphDataType[jj].equals("cumulative"))
        {
          String graphName = arrSelectedGraphs[jj];
          int[] arrcol = getColIndex(graphName, arrSelectedGraphDataType, arrSelectedGraphs);
          for (int j = 0; j < sampleCount; j++)
          {
            for (int kk = 0; kk < arrcol.length; kk++)
            {
              if (j != 0 && j < arrSample.size() &&  arrResultedData[j][arrcol[kk]] == 0)
                arrResultedData[j][arrcol[kk]] = arrResultedData[j - 1][arrcol[kk]];
            }
          }
        }
      }
      
     // No need of data to show in log, need when data to be verify
     /*StringBuffer graphsData = new StringBuffer();
      graphsData.append("\n");
      for (int j = 0; j < arrResultedData.length; j++)
      {
        for (int k = 0; k < arrResultedData[j].length; k++)
        {
          graphsData.append(arrResultedData[j][k] + "|");
        }
        graphsData.append("\n");
      }
      
      Log.debugLog(className, "getGraphsDataByDuration", "", "", "External Graphs Data = " + graphsData);
      */
      
      return arrResultedData;
    }
    catch (Exception ex)
    {
      Log.errorLog(className, "getGraphsDataByDuration", "", "", "No data match for selected graphs. File may be corrupt or invalid format. Please check csv file.");
      error = new StringBuffer();
      error.append("No data match for selected graphs. File may be corrupt or invalid format. Please check csv file.");
      return null;
    }
  }

  // function to change date and time according to time zone
  private void convertTimeByTimeZone(String strTRTimeZone, String externalDataTimeZone)
  {
    try
    {
      Log.debugLog(className, "convertTimeByTimeZone", "", "", "Method Called.");
      DateFormat externalDataDatedFormat = new SimpleDateFormat(externalDataDateFormat);
      TimeZone externalDataTime = TimeZone.getTimeZone(externalDataTimeZone);
      externalDataDatedFormat.setTimeZone(externalDataTime);
      TimeZone trTimeZone = TimeZone.getTimeZone(strTRTimeZone);

      if (arrStartDate.length == arrDateTime.length)
      {
        for (int i = 0; i < arrStartDate.length; i++)
        {
          String date = arrStartDate[i];
          String time = arrDateTime[i];
          String dateTime = date + " " + time;
          TimeZone.setDefault(trTimeZone);
          Date tempDateTime = (Date) externalDataDatedFormat.parse(dateTime);
          DateFormat df = new SimpleDateFormat(externalDataDateFormat);
          String convertedTime = df.format(tempDateTime);
          String[] arrDateValues = convertedTime.split(" ");
          if (arrDateValues != null && arrDateValues.length == 2)
          {
            String tempDate = arrDateValues[0];
            String tempTime = arrDateValues[1];
            arrStartDate[i] = tempDate;
            arrDateTime[i] = tempTime;
          }
        }
      }
    }
    catch (Exception ex)
    {
      //Log.stackTraceLog(className, "convertTimeByTimeZone", "", "", "Exception - ", ex);
      //ex.printStackTrace();
      return;
    }
  }

  public static void main(String[] args)
  { 
    
  }
}

/*--------------------------------------------------------------------
  @Name    : GenMsrInfo.java
  @Author   : Prabhat
  @Purpose : Generate Measurement  Info Data file for Well Fargo using their input file.
  @Modification History:

  Pending Tasks:

  1. nsi_get_test_runs command is run from FileBean which needs this program to run as root
  2.
----------------------------------------------------------------------*/

package pac1.Bean;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Vector;

import pac1.Bean.CmdExec;


public class GenMsrInfo
{
  private String className = "GenMsrInfo";

  private String inputFile = "";
  private String outPutFile = "";
  long minTimeInMilliSec;
  CmdExec cmdExec = null;

  public GenMsrInfo(String inputFile, String outPutFile, String minTime)
  {
    this.inputFile = inputFile;
    this.outPutFile = outPutFile;
    this.minTimeInMilliSec = Integer.parseInt(minTime) * 60 * 1000;
    cmdExec = new CmdExec();
  }

  public boolean genMsrFile()
  {
    Log.debugLog(className, "genMsrFile", "", "", "Method Starts" );

    String[] arrTemp = null;
    String[] arrTestRunRcrd = null;
    String[] arrTRDateTime = null;

    String msrData = "";

    long msrStartTime = 0;
    long msrEndTime = 0;
    long tRStartTime = 0;
    long tREndTime = 0;

    try
    {
      Vector vecTempMsrData = addMsrDataToVector();
      Vector vecAllData = new Vector();

      if(vecTempMsrData == null)
        return false;

      //This (nsi_get_test_runs) command has been renamed as (nsu_show_test_logs) 
      String cmdName = "nsu_show_test_logs";
      String cmdArgs = "-r";
      
      
      //public Vector getResultByCommand(String cmd, String args, int cmdType, String userName, String runAsUser)
      //Run as netstorm now. To be changed later
      Vector vecAllTestRun = cmdExec.getResultByCommand(cmdName, cmdArgs, CmdExec.NETSTORM_CMD, null, "netstorm");

      for(int i = 0; i < vecTempMsrData.size(); i++)
      {
        arrTemp = rptUtilsBean.strToArrayData(vecTempMsrData.elementAt(i).toString(), " ");

        msrStartTime = getTimeInMillis(arrTemp[1], arrTemp[2]);

        msrEndTime = getTimeInMillis(arrTemp[3], arrTemp[4]);

        if(msrStartTime == -1 || msrEndTime == -1)
        {
          System.out.println("Measurement (" + arrTemp[0] + ") is ignored. Specified time format is not correct. Line number = " + i);
          continue;
        }

        int j = 0;
        for (j = vecAllTestRun.size() - 1; j > 0; j-- )
        {
          arrTestRunRcrd = rptUtilsBean.strToArrayData(vecAllTestRun.elementAt(j).toString(), "|");
          
          //To get <Start Time> field at index [5] 
          arrTRDateTime = rptUtilsBean.strToArrayData(arrTestRunRcrd[5], " ");

          tRStartTime = getTimeInMillis(arrTRDateTime[0], arrTRDateTime[1]);

          if(tRStartTime == -1)
          {
            System.out.println("Test Run (" + arrTemp[0] + ") is ignored. Start Time format is not correct. = " + vecAllTestRun.elementAt(j).toString());
            continue;
          }

          if(!(arrTestRunRcrd.length >= 9))
          {
            System.out.println("Test Run (" + arrTemp[0] + ") is ignored. Test Run Info Line format is not correct. = " + vecAllTestRun.elementAt(j).toString());
            continue;
          }

          //To get <Run Time> at index [6]
          if(getRunTime(arrTestRunRcrd[6]) == -1)
          {
            System.out.println("Test Run (" + arrTemp[0] + ") is ignored. End Time format is not correct. = " + vecAllTestRun.elementAt(j).toString());
            continue;
          }

          tREndTime = tRStartTime + getRunTime(arrTestRunRcrd[6]);

          if((msrStartTime >= tRStartTime) && (msrEndTime <= tREndTime))
          {
          	//To get arrTestRunRcrd[0] <Test Run> at index [2],  to get arrTestRunRcrd[2]  <Start Time> field at index [5] 
            msrData = arrTemp[0] + "|" + arrTestRunRcrd[2] + "|" + arrTestRunRcrd[5] + "|1|Elapsed|Specified Time|Absolute|" + arrTemp[1] + "|" + arrTemp[2] + "|" + arrTemp[3] + "|" + arrTemp[4];
            vecAllData.add(msrData);
            break;
          }
        }
        if(j == 0)
          System.out.println("Measurement (" + arrTemp[0] +") is ignored as no test run found for this measurement.");
      }

      return(genMsrInfoFile(vecAllData));
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "genMsrFile", "", "", "Exception - ", e);
      System.out.println("Error: Error in genMsrFile()");
      return false;
    }
  }

  private Vector addMsrDataToVector()
  {
    Log.debugLog(className, "addMsrDataToVector", "", "", "Method Starts" );

    try
    {
      Vector vecTemp = readInputFile(inputFile);
      Vector vecMsrData = new Vector();

      String[] arrTemp = null;
      long startTime = 0;
      long endTime = 0;
      String temp = "";

      String fileWithPath = outPutFile + ".tmp";

      File tmpFile = openFile(fileWithPath);
      if(tmpFile.exists())
        tmpFile.delete();

      tmpFile.createNewFile();

      FileOutputStream fout = new FileOutputStream(tmpFile, true);  // append mode
      PrintStream pw = new PrintStream(fout);

      for (int i = 0; i < vecTemp.size(); i++)
      {
        arrTemp = rptUtilsBean.strToArrayData(vecTemp.elementAt(i).toString(), " ");

        startTime = getTimeInMillis(arrTemp[1], arrTemp[2]);

        endTime = getTimeInMillis(arrTemp[3], arrTemp[4]);

        if(startTime == -1 || endTime == -1)
        {
          System.out.println("Measurement (" + arrTemp[0] + ") is ignored. Specified time format is not correct. Line number = " + (i + 1));
          continue;
        }

        if((endTime - startTime) < minTimeInMilliSec)
        {
          System.out.println("Measurement (" + arrTemp[0] + ") is ignored as duration of this measurement is less than minimum time specified. Line number = " + (i + 1));
          continue;
        }

        temp = arrTemp[0] + "|AddTestRunHere|AddTestRunStartDateTimeHere|1|Elapsed|Specified Time|Absolute|" + arrTemp[1] + "|" + arrTemp[2] + "|" + arrTemp[3] + "|" + arrTemp[4];

        pw.println(temp);

        vecMsrData.add(vecTemp.elementAt(i).toString());
      }

      pw.close();
      fout.close();

      System.out.println("Temporary file is created successfuly. File Name = " + fileWithPath);

      if(vecMsrData.size() != 0)
        return vecMsrData;

      System.out.println("There are no measurements in the input file.");
      return null;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "addMsrDataToVector", "", "", "Exception - ", e);
      System.out.println("Error: Error in addMsrDataToVector()");
      return null;
    }
  }

  // Open file and return as File object
  private File openFile(String fileName)
  {
    Log.debugLog(className, "openFile", "", "", "Open Input file = " + fileName);

    try
    {
      File dataFile = new File(fileName);
      return(dataFile);
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "openFile", "", "", "Exception - ", e);
      System.out.println("Error: Error in openFile()");
      return null;
    }
  }


  // Methods for reading the File
  private Vector readInputFile(String fileWithPath)
  {
    Log.debugLog(className, "readInputFile", "", "", "Method called. Compare Report FIle Name = " + fileWithPath);

    try
    {
      Vector vecReport = new Vector();
      String strLine;

      FileInputStream fis = new FileInputStream(fileWithPath);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));

      while((strLine = br.readLine()) !=  null)
      {
        strLine = strLine.trim();

        if(strLine.length() == 0)
          continue;

        Log.debugLog(className, "readInputFile", "", "", "Adding line in vector. Line = " + strLine);
        vecReport.add(strLine);
      }

      br.close();
      fis.close();

      return vecReport;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "readInputFile", "", "", "Exception - ", e);
      System.out.println("Error: Error in readInputFile()");
      return null;
    }
  }

  //Add Data to output File
  public boolean genMsrInfoFile(Vector vecAllData)
  {
    Log.debugLog(className, "genMsrInfoFile", "", "", "Adding record to file. ");

    String fileWithPath = outPutFile;

    try
    {
      // Read file's content
      File dataFile = openFile(outPutFile);
      if(dataFile.exists())
        dataFile.delete();

      dataFile.createNewFile();

      FileOutputStream fout = new FileOutputStream(dataFile, true);  // append mode
      PrintStream pw = new PrintStream(fout);

      for (int i = 0; i < vecAllData.size(); i++)
        pw.println(vecAllData.elementAt(i).toString());

      pw.close();
      fout.close();

      System.out.println("OutPut File(" + outPutFile +") file is created successfuly.");

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "genMsrInfoFile", "", "", "Exception in adding record in  file (" + fileWithPath + ") - ", e);
      System.out.println("Error: Error in genMsrInfoFile()");
      return false;
    }
  }

  private long getTimeInMillis(String date, String time)
  {
    Log.debugLog(className, "getTimeInMillis", "", "", "Convert Start Date/Time in Millis. Date/Time = " + date + "/" + time);

    try
    {
      long totalTime = rptUtilsBean.convertDateToMilliSec(date) + rptUtilsBean.convStrToMilliSec(time);

      return totalTime;
    }
    catch (Exception e)
    {
      System.out.println("The Date/Time format is not Correct. Date/Time = " + date + "/" + time);
      return -1;
    }
  }

  private long getRunTime(String time)
  {
    Log.debugLog(className, "getRunTime", "", "", "Convert Time in Millis. Time = " + time);

    try
    {
      return (rptUtilsBean.convStrToMilliSec(time));
    }
    catch (Exception e)
    {
      System.out.println("The Time format is not Correct = " + time);
      return -1;
    }
  }

  private static void  usage(String error)
  {
    System.out.println(error);
    /*
    System.out.println("Usage: javac CmpRptUsingRtpl with following arguments");
    System.out.println("\ttestRun - Test Run Number where compare report set is created");
    System.out.println("\trtplName - Report Template Name");
    System.out.println("\treportSetName - Report Set Name");
    System.out.println("\t-f <msrDataFileName> - File containing all Measuremet data");
    System.out.println("\t  OR");
    System.out.println("\tmsrData - One or more Measuremet data");
    System.out.println("\tWhere measurement data in the argument or file should be in the following format:");

    System.out.println("\t  MsrName|TestRun|TestRunStartDateTime|Override|XAxisTimeFormat|TimeOption|TimeFormat|StartDate|StartTime|EndDate|EndTime");
    System.out.println("\t    MsrName is measurement name (no space allowed)");
    System.out.println("\t    TestRunStartDateTime is Test Run start date/time in MM/DD/YYYY HH:MM:SS format");
    System.out.println("\t    Override is always 1");
    System.out.println("\t    XAxisTimeFormat is always Elapsed");
    System.out.println("\t    TimeOption is 'Total Run', 'Run Phase Only' or 'Specified Time'. Next fields are required for Specified Time. For other, use NA");
    System.out.println("\t    TimeFormat is 'Elasped' or 'Absolute'");
    System.out.println("\t    StartDate is start date in MM/DD/YYYY for the measurement for 'Absolute' else 'NA'");
    System.out.println("\t    StartTime is start time in HH:MM:SS for the measurement");
    System.out.println("\t    EndDate is end date in MM/DD/YYYY for the measurement for 'Absolute' else 'NA'");
    System.out.println("\t    EndTime is end time in HH:MM:SS for the measurement");
   */

    System.exit(1);

  }

  // Sample Input
  // <Input File> <Output File> <Min Time (in minutes)>
  public static void main(String[] args)
  {
    if(args.length < 3)
      usage("Invalid number of arguments");

    try
    {
      String msrData[] = null;
      int idx = 0;
      String inputFile = args[idx++];
      String outPutFile = args[idx++];
      String minTime = args[idx++];

      GenMsrInfo genMsrInfo = new GenMsrInfo(inputFile, outPutFile, minTime);

      if(genMsrInfo.genMsrFile() == false)
      {
        System.out.println("Error: Error in generating measurement info file");
        System.out.println("See $NS_WDIR/webapps/netstorm/logs/guiError.log and $NS_WDIR/webapps/netstorm/logs/reportDebug.log for more details");
        System.exit(-1);
      }
      System.out.println("Measurement info file generated successfully.");
      System.exit(0);
    }
    catch (Exception e)
    {
      System.out.println("Error: Error in generating measurement info file");
      System.out.println("See $NSWDIR/webapps/netstorm/logs/guiError.log and $NSWDIR/webapps/netstorm/logs/reportDebug.log for more details");
      e.printStackTrace();
      System.exit(-1);
    }
  }
}

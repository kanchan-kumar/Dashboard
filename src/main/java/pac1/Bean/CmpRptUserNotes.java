/*--------------------------------------------------------------------
@Name    : CmpRptUserNotes.java
@Author  : Jyoti Jain
@Purpose : Bean for managing user notes in compare reports
@Modification History:

----------------------------------------------------------------------*/
package pac1.Bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Vector;


public class CmpRptUserNotes
{
  private static String className = "CmpRptUserNotes";
  private String workPath = Config.getWorkPath();

  private final String USER_NOTE_FILE = "compare.report";
  private final String USER_NOTE_FILE_EXTN = ".notes";

  private int numTestRun = -1;
  private String reportSetName = "";
  private int numReports = -1;  // Number of reports in compare report including header line

  /***********************************************************
  Constructor - Used from JSP
    Arguments:
      numTestRun: test run number
      reportSetName: Report Set Name
      numReports: Number of reports in compare report
  ***************************************************************/

  public CmpRptUserNotes(String testRun, String reportSetName, String numReport)
  {
    numTestRun = Integer.parseInt(testRun);
    this.reportSetName = reportSetName;
    numReports = Integer.parseInt(numReport);
  }

  // This will return the reportset  Base path
  private String getReportSetBasePath(int numTestRun)
  {
    return (workPath + "/webapps/logs/TR" + numTestRun + "/reports/reportSet");
  }

  // This will return the reportset path
  private String getReportSetPath(String reportSetName, int numTestRun)
  {
    return (getReportSetBasePath(numTestRun) + "/" + reportSetName);
  }

  private String getCmpRptUserNotesFileName()
  {
    return (getReportSetPath(reportSetName, numTestRun) +"/"+ USER_NOTE_FILE +USER_NOTE_FILE_EXTN);
  }

  // Methods for reading the File
  // Move to rptUtils.java
  private Vector readFileInVector(String fileNameWithPath)
  {
    Log.debugLog(className, "readFileInVector", "", "", "Method called. File Name = " + fileNameWithPath);

    try
    {
      Vector vecFileLines = new Vector();
      String strLine;

      File fileName = new File(fileNameWithPath);

      if(!fileName.exists())
      {
        Log.debugLog(className, "readFileInVector", "", "", "File " + fileNameWithPath + " does not exist.");
        return null;
      }

      FileInputStream fis = new FileInputStream(fileNameWithPath);
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));

      while((strLine = br.readLine()) !=  null)
      {
        strLine = strLine.trim();
        if(strLine.length() == 0)
          continue;
        Log.debugLog(className, "readFileInVector", "", "", "Adding line in vector. Line = " + strLine);
        vecFileLines.add(strLine);
      }

      br.close();
      fis.close();

      return vecFileLines;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "readFileInVector", "", "", "Exception - ", e);
      return null;
    }
  }

  private boolean isCmpRptUserNotesFilePresent()
  {
    Log.debugLog(className, "isCmpRptUserNotesFilePresent", "", "", "Method called.");

    //getting path of note file
    String userNoteFileWithPath = getCmpRptUserNotesFileName();

    File userNoteFileObj = new File(userNoteFileWithPath);

     //checking file existing or not
    if(!userNoteFileObj.exists())
    {
      Log.debugLog(className, "isCmpRptUserNotesFilePresent", "", "", userNoteFileWithPath + " file does not exist");
      return false;
    }
    else
    {
      Log.debugLog(className, "isCmpRptUserNotesFilePresent", "", "",userNoteFileWithPath + " file exist");
      return true;
    }
  }

  // Create User Note File. This method will call only when file already not exist.
  private boolean createUserNotesFile()
  {
    Log.debugLog(className, "createUserNotesFile", "", "", "Method called.");

    try
    {
      //getting path of note file
      String userNoteFileWithPath = getCmpRptUserNotesFileName();

      File userNoteFileObj = new File(userNoteFileWithPath);

      boolean createFlag =  userNoteFileObj.createNewFile();

      if(!createFlag) // File creation failed
      {
        Log.errorLog(className, "createUserNotesFile", "", "", "Error in creating in file " + userNoteFileWithPath);
        return false;
      }

      // these streams for User notes
      FileOutputStream foutUserNote = new FileOutputStream(userNoteFileObj, true);  // append mode
      PrintStream pwUserNote = new PrintStream(foutUserNote);

      // this is to add default value in vector
      for(int i = 0; i < numReports; i++)
      {
        if(i == 0)
          pwUserNote.println("User Notes");
        else
          pwUserNote.println("NA");
      }

      //closing print stream
      pwUserNote.close();

      //closing file
      foutUserNote.close();

      return true;

    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "createUserNotesFile", "", "", "Exception - ", e);
      return false;
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////
  /**
   * Purpose: To check user note present or not in compare notes file.
   * Return:
   *   It will return 1D array.
   *   Each array element will by null (Note not preset) or Yes (User note present)
   **/

  public String[] chkCmpRptUserNotes(int[] arrRptRowId)
  {
    Log.debugLog(className, "chkCmpRptUserNotes", "", "", "Method called");

    String arrChkCmp[] = new String[numReports];
    //int reportId[] = getReportRowIdArray(); //filter row Id

    try
    {
      // Check if compare report notes file is present or not
      if(!isCmpRptUserNotesFilePresent())
        return arrChkCmp;  // In this case all members of array will null

      //getting path of note file
      String userNoteFileWithPath = getCmpRptUserNotesFileName();

      //reading file and store in vector
      Vector vecUserNote = readFileInVector(userNoteFileWithPath);
      if(vecUserNote == null) // This should not happen as we have checked if file is present or not
      {
        Log.errorLog(className, "chkCmpRptUserNotes", "", "", "Error in reading compare user notes file.");
        return arrChkCmp;  // In this case all members of array will null
      }

      if(vecUserNote.size() != numReports) // Something wrong as file as should have numReports lines
      {
        Log.errorLog(className, "chkCmpRptUserNotes", "", "", "Number of lines (" + vecUserNote.size() + ") in compare user notes file is not same as number of lines (" + numReports + ") in compare report file.");

        return arrChkCmp;  // In this case all members of array will null
      }

      // Now check user notes for all report and see if present or not
      for(int i = 0; i < arrRptRowId.length; i++)
      {
        if(i == 0) // First line is header line only
          arrChkCmp[i] = "User Notes";
        else if(!vecUserNote.get(arrRptRowId[i]).equals("NA"))
          arrChkCmp[i] = "Yes";
        // else // Keep it null for no notes
          // arrChkCmp[i] = "Yes";
        Log.debugLog(className, "chkCmpRptUserNotes", "", "", "User notes for report Id = " + arrChkCmp[i]);
      }

      return arrChkCmp;

    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "chkCmpRptUserNotes", "", "", "Exception - ", e);
      return arrChkCmp;
    }
  }


  //////////////////////////////////////////////////////////////////////////////////////
  /**
   * Purpose: To add, update user note in compare notes file.
   * Arguments:
   *   rptRowId: RowId compare note record
   *   userNote: User Note data (Can be multiple lines)
   * Return:
   *   It will return true or false.
   **/

  public boolean addUserNote(String rptRowId, String userNote)
  {
    Log.debugLog(className, "addUserNote", "", "", "rptRowId = " + rptRowId + ", User Note = " + userNote);

    int RowId = Integer.parseInt(rptRowId.trim());

    Vector vecUserNote = new Vector();

    try
    {
      if((RowId < 1) || (RowId >= numReports))
      {
        Log.errorLog(className, "addUserNote", "", "", "Invalid Row Id");
        return false;
      }

      //getting path of compare note file
      String userNoteFileWithPath = getCmpRptUserNotesFileName();

      File userNoteFileObj = new File(userNoteFileWithPath);

      // Check if compare report notes file is present or not
      if(!isCmpRptUserNotesFilePresent()) // File is not present
      {
        if(!createUserNotesFile())
          return false;
      }

      //reading file and store in vector
      vecUserNote = readFileInVector(userNoteFileWithPath);
      if(vecUserNote == null) // This should not happen as we have checked if file is present or not
      {
        Log.errorLog(className, "addUserNote", "", "", "Error in reading compare user notes file.");
        return false;
      }

      if(vecUserNote.size() != numReports) // Something wrong as file as should have numReports lines
      {
        Log.errorLog(className, "addUserNote", "", "", "Number of lines (" + vecUserNote.size() + ") in compare user notes file is not same as number of lines (" + numReports + ") in compare report file.");

        return false;  // In this case all members of array will null
      }

      // Replace new line by NEW_LINE_%08 so that note become one line

      userNote = userNote.replaceAll("\r\n", "NEW_LINE_%09");
      userNote = userNote.replaceAll("\n", "NEW_LINE_%09");

      // delete file so that we can replace whole file with all notes except the changed one from old file.
      userNoteFileObj.delete();
      userNoteFileObj.createNewFile();

      // these streams for User notes
      FileOutputStream foutUserNote = new FileOutputStream(userNoteFileObj, true);  // append mode
      PrintStream pwUserNote = new PrintStream(foutUserNote);

      for(int i = 0; i < vecUserNote.size(); i++)
      {
        //searching rowId and update user note and note of compare report of that row
        if(i == RowId)
        {
          Log.debugLog(className, "addUserNote", "", "", "Writing updated line in file = " + userNoteFileWithPath);

          if(userNote.equals("")) // This means note is being deleted
            pwUserNote.println("NA"); // We need to keep NA otherwise line becomes empty and is ignored by read file in vector method
          else
            pwUserNote.println(userNote);
        }
        else //Rest of rowId data print as it is
        {
          Log.debugLog(className, "addUserNote", "", "", "Writing line in file = " + vecUserNote.elementAt(i).toString());

          pwUserNote.println(vecUserNote.elementAt(i).toString());
        }

      }

      //closing print stream
      pwUserNote.close();

      //closing file
      foutUserNote.close();

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "addUserNote", "", "", "Exception = ", e);
      return false;
    }
  }

  ////////////////////////////////////End of method/////////////////////////////////


  //////////////////////Get user note detail//////////////////////////////////////

  //Method will return 1D array of compare user note data
  public String getUserNote(String rptRowId)
  {
    Log.debugLog(className, "getUserNote", "", "", "Method called");

    try
    {
      String strUserNote = "";
      Vector vecUserNote = new Vector();

      //type string to integer
      int rowId = Integer.parseInt(rptRowId.trim());

      String userNoteFileWithPath = getCmpRptUserNotesFileName();

      // checking valid row Id
      if((rowId < 1) || (rowId >= numReports))
      {
        Log.errorLog(className, "getUserNote", "", "", "Invalid Row Id");
        return "";
      }

      // Check if compare report notes file is present or not
      if(!isCmpRptUserNotesFilePresent())
        return "";

      //reading file and store in vector
      vecUserNote = readFileInVector(userNoteFileWithPath);
      if(vecUserNote == null) // This should not happen as we have checked if file is present or not
      {
        Log.errorLog(className, "getUserNote", "", "", "Error in reading compare user notes file.");
        return "";  // In this case all members of array will null
      }

      // Check size of vector with numReprot
      if(vecUserNote.size() != numReports) // Something wrong as file as should have numReports lines
      {
        Log.errorLog(className, "getUserNote", "", "", "Number of lines (" + vecUserNote.size() + ") in compare user notes file is not same as number of lines (" + numReports + ") in compare report file.");

        return "";  // In this case all members of array will null
      }

      // Now check user notes for all report and see if present or not
      for(int i = 0; i < numReports; i++)
      {
        if(i == rowId)
        {
          strUserNote = vecUserNote.elementAt(i).toString();
          break;
        }
        Log.debugLog(className, "getUserNote", "", "", "User notes for report Id " + i + " = " + vecUserNote.elementAt(i).toString());
      }

      if(strUserNote.equals("NA"))
        return("");

      // replace new line to NEW_LINE_%09
      //strUserNote = strUserNote.replaceAll("NEW_LINE_%09", "\r\n");
      //strUserNote = rptUtilsBean.replace(strUserNote, "NEW_LINE_%09", "\r\n");

      //Here we are replacing back slash(\) and single code(') to \\ and \' and passing to js variable because for js \ equal to \\ and ' to \'.
      strUserNote = rptUtilsBean.replace(strUserNote, "\\", "\\\\");
      strUserNote = rptUtilsBean.replace(strUserNote, "'", "\\'");

      strUserNote = strUserNote.trim();

      return strUserNote;
    }

    catch(Exception e)
    {
      Log.stackTraceLog(className, "getUserNote", "", "", "Exception - ", e);
      return null;
    }
  }

  //////////////////////////////End of method/////////////////////////////////


 /*************************************Main Method*******************************/

  public static void main(String[] args)
  {
    CmpRptUserNotes CmpRptUserNotes_obj = new CmpRptUserNotes("29232","AAAA","4");

    /**boolean flag = CmpRptUserNotes_obj.addUserNote("2", "Test");

    if(flag)
     System.out.println("true");
    else
      System.out.println("false");**/

    String strUserNote = CmpRptUserNotes_obj.getUserNote("3");
    System.out.println(" strUserNote  " + strUserNote);

    /**String[] arrData = CmpRptUserNotes_obj.chkCmpRptUserNotes();

    System.out.println(" arrData.length  " + arrData.length);

    for (int i = 0; i < arrData.length; i++)
    {
      System.out.print(" " + arrData[i]);
    }
    System.out.println();**/
  }

}

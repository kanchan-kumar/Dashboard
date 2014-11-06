
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

 public class EventLogFiles
 {
   private static String className = "EventLogFile";
   private static String workPath = Config.getWorkPath();
   private final static String EDF_FILE_EXTN = ".dat";
   private final static String EDF_FILE_BAK_EXTN = ".dat";
   FileBean filebean_obj = new FileBean();

   public EventLogFiles() 
   {
  	  
   }

   private static String getEDFPath()
   {
     return (workPath + "/events/");
   }

   //This returns full path of with .adf extension
   private static String getEDFNameWithEXTN(String fileName)
   {
     return (getEDFPath() + fileName);
   }

  // This will return the bak edf name
   private String getBAKEDFName(String edfName)
   {
     return (getEDFPath() + edfName + EDF_FILE_BAK_EXTN);
   }
   
  // Open file and returns File object
   private static File openFile(String fileName)
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
    
   public Vector readFile(String FileNameWithPath, boolean ignoreComments, boolean ignoreBlankLine)
   {
     Log.debugLog(className, "readFile", "", "", "Method called." + FileNameWithPath);

     FileNameWithPath = getEDFNameWithEXTN(FileNameWithPath);

     //System.out.println("Path - " + FileNameWithPath);

     Vector vecFileData = new Vector();
     try
  	 {
  		String strLine = "";
  		
  		FileInputStream fis = new FileInputStream(openFile(FileNameWithPath));
  		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
  		while((strLine = br.readLine()) != null)
  		{
  			strLine = strLine.trim();
  			if(ignoreComments && strLine.startsWith("#"))
  				continue;
  			if(ignoreBlankLine && strLine.length() == 0)
  				continue;
  			vecFileData.add(strLine);
  		}
	   	fis.close();
  		return vecFileData;
  	 }
  	 catch (Exception ex)
  	 {
  		Log.stackTraceLog(className, "readFile", "", "", "Exception - ", ex);
  		return vecFileData;
  	 }
  }

  public ArrayList loadAllConf()
  {
  	Log.debugLog(className, "loadAllConf", "", "", "Method called");

  	String filePath = workPath + "/" + "events";

    ArrayList arrListAllConf = new ArrayList();  // Names of all adf files
    ArrayList arrListAllConfSortedOrder = new ArrayList();  // Names of all conf files in sorted order

    File fileName = new File(filePath);

    if(!fileName.exists())
    {
    	fileName.mkdirs();
    	//return null;
    }

    try
    {
      String arrAvailFiles[] = fileName.list();

      for(int j = 0; j < arrAvailFiles.length; j++)
      {
      	String[] tempArr = rptUtilsBean.strToArrayData(arrAvailFiles[j], "."); // for remove profile file extension
        if(tempArr.length == 2)
        {
          if(arrAvailFiles[j].lastIndexOf(EDF_FILE_EXTN) != -1)  // Skip non .adf files
          {
         	  //String tmpStr = arrAvailFiles[j].substring(0, arrAvailFiles[j].lastIndexOf(EDF_FILE_EXTN));
         	 arrListAllConf.add(arrAvailFiles[j]);
      	    Log.debugLog(className, "loadAllADF", "", "", "Adding '.dat' file name in ArrayList = " + arrAvailFiles[j]);
          }
        }
        else
        {
      	  Log.debugLog(className, "loadAllADF", "", "", "Skiping file name  = " + arrAvailFiles[j]);
        }      	
        //arrListAllConf.add(arrAvailFiles[j]);
        //Log.debugLog(className, "loadAllConf", "", "", "Adding '.conf' file name in ArrayList = " + arrAvailFiles[j]);
      }

      Object[] arrTemp = arrListAllConf.toArray();
      Arrays.sort(arrTemp);

      for(int i = 0; i < arrTemp.length; i++)
      	arrListAllConfSortedOrder.add(arrTemp[i].toString());

      return arrListAllConfSortedOrder;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "loadAllConf", "", "", "Exception - ", e);
      return arrListAllConfSortedOrder;
    }
  }
  
  public int getEDFcount(String fileName)
  {
  	Vector vecData = readFile(fileName, false, false);
  	
  	return vecData.size();
  }
/**
 * Get all EDF name, number of EDF and Modification date
 * @return
 */  
  public String[][] getAllEDFs()
  {
  	Log.debugLog(className, "getAllEDFs", "", "", "Method called.");
  	try
  	{
      ArrayList arrListAllEDF = loadAllConf();  // Names of all MPROF files

      if(arrListAllEDF == null)
        return null;

  		String[][] arrADFs = new String[arrListAllEDF.size() + 1][3];

  		arrADFs[0][0] = "EDF Name";
  		arrADFs[0][1] = "Number of EDF";
  		arrADFs[0][2] = "Modified Date";
  		//arrADFs[0][3] = "Number Of ADF";

  		for(int i = 0; i < arrListAllEDF.size(); i++)
  		{
  			Log.debugLog(className, "getAllEDFs", "", "", "EDF Name : " + arrListAllEDF.get(i).toString());  			
  			arrADFs[i+1][0] = arrListAllEDF.get(i).toString();

  			arrADFs[i+1][1] = "" + getEDFcount(arrListAllEDF.get(i).toString());
  			
        long lastModifiedTime = openFile(getEDFNameWithEXTN(arrListAllEDF.get(i).toString())).lastModified();
        Date dateLastModified = new Date(lastModifiedTime);
        SimpleDateFormat smt = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
        arrADFs[i+1][2] = smt.format(dateLastModified);
  		}

  	  return arrADFs;  		
  	}
   	catch(Exception e)
  	{
      Log.stackTraceLog(className, "getAllEDFs", "", "", "Exception - ", e);
      return new String[0][0];
  	}  	
  }
  
  public boolean addEDF(String EDFName)
  {
  	Log.debugLog(className, "addEDF", "", "", "Method called. EDF Name" + EDFName);  	
  	try
  	{
  		File fileName = new File(getEDFNameWithEXTN(EDFName));
  		//File fileNameBak = new File(getBAKEDFName(EDFName));
  		
  		if(fileName.exists())
  		{
  			Log.debugLog(className, "addEDF", "", "", "File is already exist" + EDFName);
  		}
  		else
  		{
  			Log.debugLog(className, "addEDF", "", "", "File is created" + EDFName);
  			fileName.createNewFile();
  			//fileNameBak.createNewFile();
  		}
  		return true;
  	}
  	catch(Exception ex)
  	{
  		Log.stackTraceLog(className, "addEDF", "", "", "Exception - ", ex);
      return false; 		
  	}
  }
 
  public boolean deleteEDF(String EDFName)
  {
  	Log.debugLog(className, "deleteEDF", "", "", "Method called. EDF Name - " + EDFName);
  	
    try
    {
		  File fileName = new File(getEDFNameWithEXTN(EDFName));
		  //System.out.println(" Delete Path =  " + getEDFNameWithEXTN(EDFName));
		  //File fileNameBak = new File(getBAKEDFName(EDFName));
		  
		  boolean success = fileName.delete();
	    if (!success){
  			Log.debugLog(className, "deleteEDF", "", "", "Deletion failed. EDF Name = " + EDFName);
  			return false;
  		}
  		else
  		{
  			//fileNameBak.delete();
  			Log.debugLog(className, "deleteEDF", "", "", "File deleted successfully. EDF Name = " + EDFName);
  		}    	
    	return true;
    }
    catch(Exception ex)
    {
  		Log.stackTraceLog(className, "deleteEDF", "", "", "Exception - ", ex);
      return false;    	
    }
  }
 
  public boolean renameEDF(String EDFNameOld, String EDFNameNew)
  {
  	Log.debugLog(className, "renameEDF", "", "", "Method called. Old EDF Name - " + EDFNameOld + " New EDF Name - " + EDFNameNew);
    try
    {
    	File fileNameOld = new File(getEDFPath() + EDFNameOld);
    	File fileNameNew = new File(getEDFPath() + EDFNameNew);
    	//File fileNameNewBak = new File(getBAKEDFName(EDFNameNew));
    
    	if(!fileNameOld.exists())
    	{
      	Log.debugLog(className, "renameEDF", "", "", "File does not exist. - " + EDFNameOld); 
      	return false;
    	}
    	else
    	{
    		boolean Rename = fileNameOld.renameTo(fileNameNew);
    		if(!Rename)
    			Log.debugLog(className, "renameEDF", "", "", "File does not rename successfully. - " + EDFNameNew);
    		else
    		{
    			//fileNameOld.renameTo(fileNameNewBak);
    			Log.debugLog(className, "renameEDF", "", "", "File rename is successfully. - " + EDFNameNew);
    		}
    	}
    	
    	return true;
    }
    catch(Exception ex)
    {
    	Log.stackTraceLog(className, "renameEDF", "", "", "Exception - ", ex);    	
    	return false;
    }
  }
 
  public String[][] getAllDetailEDFs(String EDFfileName)
  {
    Log.debugLog(className, "getAllDetailEDFs", "", "", "Method called, file name = " +  EDFfileName);
 
    try
    {
      /**String fileWithPath = getBAKEDFName(EDFfileName);
      
      if(!openFile(getBAKEDFName(EDFfileName)).exists())
      {
      	Log.debugLog(className, "getAllDetailEDFs", "", "", "Creating bak file " +  EDFfileName);
      	openFile(fileWithPath).createNewFile();
      	filebean_obj.copyToFile(getEDFNameWithEXTN(EDFfileName), fileWithPath);
      }  **/
      
      Vector vecDataFile = readFile(EDFfileName, true, true);
      ArrayList arrayList = new ArrayList();
      String dataLine = "";
 
      if(vecDataFile == null)
      {
        Log.debugLog(className, "getADFDetails", "", "", "EDF File not found, It may be correpted., file name = " + EDFfileName);
        return null;
      }
  
      for(int i = 0; i < vecDataFile.size(); i++)    //Profile description wiil be on the zeroth index and last modified date on oneth index.
      {
      	Log.debugLog(className, "getADFDetails", "", "", " Read File" + vecDataFile.elementAt(i).toString());      	
        dataLine = vecDataFile.elementAt(i).toString();
        String arrEdfDetails[] = rptUtilsBean.strToArrayData(dataLine, "|");
        arrayList.add(arrEdfDetails);        
      }
      
      String[][] arrEDFDetails = new String[arrayList.size()][10];  //Skipping Profile description and last modified date and add mprof detail(profile details and last modified date).
      
      //get an Iterator object for ArrayList using iterator() method.
  
      for(int ii = 0; ii < arrayList.size(); ii++)
      {
        String[] strRowValue = (String[])arrayList.get(ii);
        for(int jj = 0; jj < strRowValue.length; jj++)
        {
        	arrEDFDetails[ii][jj] = strRowValue[jj];
        }
      }
     
      return arrEDFDetails;      
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getADFDetails", "", "", "Exception - ", e);
      return new String[0][0];    	
    }
  }
  
  public boolean addEventToEDF(String EDFName, String strDetail)
  {
  	Log.debugLog(className, "getAllDetailEDFs", "", "", "Method called, file name = " +  EDFName);  	
  	try
  	{
//  	 Read file's content
      Vector vecData = readFile(EDFName, false, false);

      File edfFileObj = new File(getEDFNameWithEXTN(EDFName));

      boolean bolDel = deleteEDF(EDFName);
      //edfFileObj.delete(); // Delete adf file
      if(bolDel)
      {
	      edfFileObj.createNewFile(); // Create new adf file
	
	      if(rptUtilsBean.changeFilePerm(edfFileObj.getAbsolutePath(), "netstorm", "netstorm", "664") == false)
	        return false;
	
	      if(!edfFileObj.exists())
	      {
	        Log.errorLog(className, "getAllDetailEDFs", "", "", "EDF file does not exist. EDF filename is - " + edfFileObj);
	        return(false);
	      }
	
	      FileOutputStream fout = new FileOutputStream(edfFileObj, true);  // append mode
	      PrintStream pw = new PrintStream(fout);
	
	      String dataLine = "";
	      
	      for(int i = 0; i < vecData.size(); i++)
	      {
	        dataLine = vecData.get(i).toString();
	        pw.println(dataLine);
	      }
	
	      pw.println(strDetail);  // Append the new monitor to the end of file
	      pw.close();
	      fout.close();
	      return true; 
      }
      return false;
  	}
  	catch(Exception e)
  	{
      Log.stackTraceLog(className, "getAllDetailEDFs", "", "", "Exception - ", e);
      return false; 		
  	}
  }
  
  public boolean delEventToEDF(String edfName, String[] arrEDFIdx)
  {
  	Log.debugLog(className, "delEventToEDF", "", "", "EDF Name = " + edfName);
   
    try
    {
      Vector vecData = readFile(edfName, false, false);
      
      File edfFileObj = new File(getEDFNameWithEXTN(edfName));

      boolean bolDelFile = false;
      if(edfFileObj.exists())
      {
        bolDelFile = edfFileObj.delete();
      }
      
      if(bolDelFile)
      {
        edfFileObj.createNewFile(); // Create new mprof bak file

        if(rptUtilsBean.changeFilePerm(edfFileObj.getAbsolutePath(), "netstorm", "netstorm", "664") == false)
          return false;

        FileOutputStream fout = new FileOutputStream(edfFileObj, true);  // append mode
        PrintStream pw = new PrintStream(fout);
        String status = "";

        String dataLine = "";
        int count = 0;
        
        for(int i = 0; i < vecData.size(); i++)
        {
          dataLine = vecData.elementAt(i).toString();
          Log.debugLog(className, "delEventToEDF", "", "", "dataLine = " + i + "  --  "+ vecData.elementAt(i).toString());        
        
          status = "";
          if((dataLine.startsWith("#")) || (dataLine.length() == 0))
          {
          	pw.println(vecData.elementAt(i).toString());
          	continue;
          }
          
          for(int k = 0; k < arrEDFIdx.length; k++)
          {
            if((count) == (int)Integer.parseInt(arrEDFIdx[k])) // substratct by 2 because first line is description and second is last modified date of profile
            {
              status = "true";
              break;
            }
          }
          count ++;
          if(!status.equals("true"))
          {
            pw.println(vecData.elementAt(i).toString());
            status = "";
          }
        }

        pw.close();
        fout.close();

        return true;
      }
      return false;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "delEventToEDF", "", "", "Exception - ", e);
      return false;
    }
  }
  
  public static void main(String arg[])
  {
	  EventLogFiles EventLogFile_obj = new EventLogFiles();

	  int choice = 0;
	  String path;
	  Vector vecData = new Vector();
	  ArrayList arrListAllConfNew = new ArrayList();
	  String[][] arrTestSuiteDetail;
	
	  BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    System.out.println("********Please enter the option for desired operation*********");
    System.out.println("Get all Event file : 1");
    System.out.println("Get data of Event file : 2");   
    System.out.println("Get Detail (getAllEDFs) : 3");
    System.out.println("Create EDF file : 4");
    System.out.println("Delete File : 5");
    System.out.println("Rename File : 6");
    System.out.println("Get Detail of EDF File : 7");
    System.out.println("Add EDF File : 8");
    System.out.println("Delete EDF File : 9");    
    System.out.println("*************************");

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
    	  arrListAllConfNew =  EventLogFile_obj.loadAllConf();

    	  for(int i = 0; i < arrListAllConfNew.size(); i++)
    	  {
    		  System.out.println("----- " + arrListAllConfNew.get(i).toString());
    	  }
  		  break;
  	
      case 2:	
    	  Vector vecTemp = EventLogFile_obj.readFile("Jyoti.dat", true, true);

    	  for(int i = 0; i < vecTemp.size(); i++)
    	  {
    		  System.out.println("----- " + vecTemp.get(i).toString());
    	  }
    	  break;
 
      case 3:	
    	  String arrEDFDetail[][] = EventLogFile_obj.getAllEDFs();

      	for(int i = 0; i< arrEDFDetail.length; i++)
      	{
      		System.out.println("---------\n" + arrEDFDetail[i][0] + "\n" + arrEDFDetail[i][1] + "\n" + arrEDFDetail[i][2]);
      	}   
    	  break;
 
      case 4:	
    	  boolean bolAdd = EventLogFile_obj.addEDF("Test_Delete1.dat");
      	System.out.println(" File is created successfully:- " + bolAdd);
    	  break;

      case 5:	
    	  boolean bolDel = EventLogFile_obj.deleteEDF("Test_Delete.dat");
      	System.out.println(" File deleted :- " + bolDel);
    	  break;
  
      case 6:	
    	  boolean bolRename = EventLogFile_obj.renameEDF("Test_Delete1.dat", "Test_Delete.dat");
      	if(bolRename)
    	    System.out.println(" File rename successfully :- " + bolRename);
      	else
      		System.out.println(" Fail to rename");
    	  break;
 
      case 7:	
    	  String arrEDFDetails[][] = EventLogFile_obj.getAllDetailEDFs("Test_Delete.dat");

    	  for(int i = 0; i < arrEDFDetails.length; i++)
    	  {
          for(int j = 0; j < arrEDFDetails[i].length; j++)
            System.out.println("arrEDFDetails " + i + " " + j + " " + arrEDFDetails[i][j]);
          System.out.println("\n\n");
    	  }
     	  break;
  
      case 8:	
    	  boolean bol = EventLogFile_obj.addEventToEDF("Test_Delete.dat", "1|jyoti|Script,Page,Server|3|10000|NA|NA|NA|NA|Undef42");
    	  break;

      case 9:	
      	String[] arrId = new String[]{"0","2"};
    	  boolean bolEDFDel = EventLogFile_obj.delEventToEDF("Test_Delete.dat", arrId);
    	  System.out.println("  bolEDFDel  " + bolEDFDel);
    	  break;
    	  
    	  
      default:
        System.out.println("Please select the correct option.");       	

    }  
  }
}
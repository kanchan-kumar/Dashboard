//Name    : HPDURLBean.java
//Author  : Jyoti
//Purpose : Utility Bean for NetOcean GUI
//Modification History:
//03/30/10 Arun Goel: Initial Version
////////////////////////////////////////////////////////////////////

package pac1.Bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Vector;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HPDURLBean
{
  private static String className = "HPDURLBean";
  private String hpdPath = "";

  private final String CONTROL_FOLDER = "control";
  private final String REAL_FOLDER = "real";
  // Available keywords 
  private static final String[] arrAvailableKeyword = new String[]{"MODULEMASK", "HPD_DEBUG", "SVC_TIME", "SVC_CGI_TIME", "CONTENT_LENGTH_INDICATOR", "HTTP_LOG"};
	
  CorrelationService correlationService_obj;

  //Constuctor
  public HPDURLBean()
  {
    correlationService_obj = new CorrelationService();
    hpdPath = correlationService_obj.getHPDPath();
  }

  //getting control directory path
  public String getControlFolder()
  {
    return (hpdPath + CONTROL_FOLDER);
  }

  //getting real directory path
  public String getRealFolder()
  {
    return (hpdPath + REAL_FOLDER);
  }

//Name: getHostNameFromReal
  // Purpose: get host name from real
  // Arguments: 
  // Return: Array
  /////////////////////////////////////////////////////////////////////
  public String[] getHostNameFromReal()
  {
    Log.debugLog(className, "getKeywordFields", "", "", "Method called.");

    try
    {
      ArrayList alHostNames = new ArrayList();

      String hostDir = getRealFolder(); //get real path
      File dir = new File(hostDir); // file object

      //get list of files and directories
      File[] files = dir.listFiles(); 
      String[] hostNameList = dir.list();

      if(files == null)
      {
        Log.debugLog(className, "getHostFromReal", "", "", "Host is not present in real");
        return new String[0];
      }
      else
      {
        for(int index = 0; index < files.length; index++)
        {
          //checking is directory adding in arraylist
          if(files[index].isDirectory()) 
          {
            alHostNames.add(hostNameList[index]);
          }
        }
      }
      
      //convert into 1D array
      String[] hostList = new String[alHostNames.size()]; 
      for(int j = 0; j < alHostNames.size(); j++)
      {
        hostList[j] = alHostNames.get(j).toString();
        Log.debugLog(className, "getHostFromReal", "", "", "Host Name found = " + hostList[j]);
      }
      return hostList;
    }

    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getHostFromReal", "", "", "Exception - ", ex);
      return new String[] {""};
    }
  }
  
  //Name: openFile
  // Purpose: Open file and return as File object
  // Arguments: fileName
  // Return: Array
  /////////////////////////////////////////////////////////////////////  
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
  
  //Name: openFile
  // Purpose: Methods for reading the File in vector
  // Arguments: 
  //     arg0: fileWithPath
  //     arg1: for future use
  // Return: vector
  /////////////////////////////////////////////////////////////////////    
	private Vector readFile(String fileWithPath, boolean ignoreComments)
	{
		Log.debugLog(className, "readFile", "", "", "Method called. FIle Name = " + fileWithPath);

		try
		{
			Vector vecFileData = new Vector();
			String strLine;
      //open file 
			File confFile = openFile(fileWithPath);

			//checking existing or not 
			if(!confFile.exists())
			{
				Log.errorLog(className, "readFile", "", "", "File not found, filename - " + fileWithPath);
				return null;
			}

			FileInputStream fis = new FileInputStream(fileWithPath);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
      
			//reading file
			while((strLine = br.readLine()) !=  null)
			{
				strLine = strLine.trim();
				if(ignoreComments && strLine.startsWith("#")) //ignore #
					continue;
				if(ignoreComments && strLine.length() == 0)  //ignore blank line
					continue;

				Log.debugLog(className, "readFile", "", "", "Adding line in vector. Line = " + strLine);
				vecFileData.add(strLine);
			}

			br.close(); //close buffer reader
			fis.close(); //file stream

			return vecFileData;
		}
		catch(Exception e)
		{
			Log.stackTraceLog(className, "readFile", "", "", "Exception - ", e);
			return null;
		}
	}
	
  //Name: addUrlBaseKeyword
  // Purpose: Method to add keyword in control/real file of any URL on the bases of host name.
  // Arguments: 
  //     arg0: host name
  //     arg1: url
	//     arg0: destination folder(control/real)
  //     arg1: Keyword name 
	//     arg0: field value of keyword in array
  // Return: boolean
  /////////////////////////////////////////////////////////////////////    	
  public boolean addUrlBaseKeyword(String hostName, String URL, String destFolder, String objectName, String[] fieldsValue)
  {
  	try
    {
	    Log.debugLog(className, "addUrlBaseKeyword", "", "", "Method started URL=" + URL + ", objectName=" + objectName + ", destFolder=" + destFolder);
	    
	    //Checking valid keyword or not
	    String strKeyword = "";
			for(int jj = 0; jj < arrAvailableKeyword.length; jj++)
			{
				// append space after keyword b'coz 
				//if keyword is NUM_PROCESS123 0
				if(arrAvailableKeyword[jj].equals(objectName))
				  strKeyword = arrAvailableKeyword[jj];
			}
			
			//If non existing keyword return false
			if(strKeyword.equals(""))
				return false;
			
	    String strToAdd = "";
	    if(objectName.equals(strKeyword)) 
	    {
	    	strToAdd = objectName;
	    	for(int i = 0; i < fieldsValue.length; i++)
	    		strToAdd = "\n" + strToAdd + " " + fieldsValue[i];
	      
        //If field value length less than 0.
	    	//It will add keyword in the file
	    	if(fieldsValue.length < 0) 
	    		strToAdd = "";
	    }
	        
	    Log.debugLog(className, "addUrlBaseKeyword", "", "", "strToAdd=" + strToAdd);
	    String tempPath = "";
	    
	    if(destFolder.equals("default"))
	    	tempPath = getRealFolder() + "/" + hostName + "/" + URL; //real
	    else
	    	tempPath = getControlFolder() + "/" + hostName + "/" + URL; //control
	    
	    FileWriter fstream = new FileWriter(tempPath, true);
 	
	    BufferedWriter out = new BufferedWriter(fstream);
	    out.write(strToAdd); //adding keyword 
	    out.close();
	    return true;
    }
    catch(Exception ex)
    {
	    Log.stackTraceLog(className, "addUrlBaseKeyword", "", "", "Exception - ", ex);
	    return false;
    }
  }
 
  //Name: writeToFile
  // Purpose: vector write in file.
  // Arguments: 
  //     arg0: fileWithPath
  //     arg1: vecModified
  //Return: boolean  
  /////////////////////////////////////////////////////////////////////    	
  private boolean writeToFile(String fileWithPath, Vector vecModified)
  {
    Log.debugLog(className, "readFile", "", "", "Method called. FIle Name = " + fileWithPath);

    try
    {
      FileOutputStream out2 = new FileOutputStream(new File(fileWithPath));
      PrintStream requestFile = new PrintStream(out2);
      for(int ad = 0; ad < vecModified.size(); ad++)
        requestFile.println(vecModified.get(ad).toString());
      requestFile.close();
      out2.close();
      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "writeToFile", "", "", "Exception - ", e);
      return false;
    }
  }
 
  //Name: chkkeywordPresentInControl
  // Purpose: Checking keyword present in the file or not.
  // Arguments: 
  //     arg0: host name
  //     arg1: URL
  //     arg2: destination folder(control/real)
  //     arg3: Keyword name  
  //Return: boolean
  /////////////////////////////////////////////////////////////////////    	
  public boolean chkkeywordPresentInControl(String hostName, String URL, String destFolder, String objectName)
	{
		Log.debugLog(className, "chkkeywordPresentInControl", "", "", "Method started URL=" + URL + ", objectName=" + objectName + ", destFolder=" + destFolder);
		try
		{
	    String tempPath = "";
	    
	    if(destFolder.equals("default"))
	    	tempPath = getRealFolder() + "/" + hostName + "/" + URL; //real
	    else
	    	tempPath = getControlFolder() + "/" + hostName + "/" + URL; //control
	    
			Vector completeFile = readFile(tempPath, false);
			
			if(completeFile == null)
				return false;
			
			for(int yy = 0; yy < completeFile.size(); yy++)
			{
				String line = completeFile.elementAt(yy).toString().trim();

        //append space after keyword b'coz 
				//if keyword is NUM_PROCESS123 0
				if(line.startsWith(objectName + " ")) // Method found
				{			
					return true;
  			}
		  }
			return false;
		}
		catch (Exception ex)
		{
			Log.stackTraceLog(className, "chkkeywordPresentInControl", "", "", "Exception - ", ex);
			return false;
		}
	}
 
  //Name: getUrlBasedScalarKeyword
  // Purpose: Getting keyword from the file in 1D Array.
  // Arguments: 
  //     arg0: host name
  //     arg1: URL
  //     arg2: destination folder(control/real)
  //     arg3: Keyword name  
  //Return: 1D Array
  /////////////////////////////////////////////////////////////////////  
  public String[] getUrlBasedScalarKeyword(String hostName, String URL, String destFolder, String objectName)
	{
		Log.debugLog(className, "getUrlBasedScalarKeyword", "", "", "Method started URL=" + URL + ", objectName=" + objectName + ", destFolder=" + destFolder);
		try
		{
			String arrFlds[] = {""};
      String tempPath = "";
	    
	    if(destFolder.equals("default"))
	    	tempPath = getRealFolder() + "/" + hostName + "/" + URL; //real
	    else
	    	tempPath = getControlFolder() + "/" + hostName + "/" + URL; //control
	    
	    //reading file in vector
			Vector completeFile = readFile(tempPath, false);
			
			//If file is null it return blank array
			if(completeFile == null)
				return new String[]{""};
			
			for(int yy = 0; yy < completeFile.size(); yy++)
			{
				String line = completeFile.elementAt(yy).toString().trim();

        //append space after keyword b'coz 
				//if keyword is NUM_PROCESS123 0
				if(line.startsWith(objectName + " ")) // Method found
				{			
					//Convert into array
					arrFlds = rptUtilsBean.strToArrayData(line, " ");
  			}
		  }
			return arrFlds;
		}
		catch (Exception ex)
		{
			Log.stackTraceLog(className, "getUrlBasedScalarKeyword", "", "", "Exception - ", ex);
			return new String[]{""};
		}
	}
 
  //Name: getUrlBasedScalarKeyword
  // Purpose: Getting keyword from the file in StrinBuffer.
  // Arguments: 
  //     arg0: host name
  //     arg1: URL
  //     arg2: destination folder(control/real)
  //     arg3: Keyword name  
  //     arg4,5,6,7,8,9: StringBuffer (field values)
  //Return: integer
        //Example: To get all fields of RUN_TIME keyword, pass following arguments:
       // getUrlBasedScalarKeyword("default", "/URLFolder/test/file.txt", "default", "HPD_DEBUG", strHpdDebug, null, null, null, null, null);
  /////////////////////////////////////////////////////////////////////  
  public int getUrlBasedScalarKeyword(String hostName, String URL, String destFolder, String objectName, StringBuffer strFld1, StringBuffer strFld2, StringBuffer strFld3, StringBuffer strFld4, StringBuffer strFld5, StringBuffer strFld6)
	{
		Log.debugLog(className, "getUrlBasedScalarKeyword", "", "", "Method started URL=" + URL + ", objectName=" + objectName + ", destFolder=" + destFolder);

		int numFld = 0;
		String arrFlds[] = {""};
		
		try
		{
      String tempPath = "";
	    
	    if(destFolder.equals("default"))
	    	tempPath = getRealFolder() + "/" + hostName + "/" + URL; //real
	    else
	    	tempPath = getControlFolder() + "/" + hostName + "/" + URL; //control
	    
			Vector completeFile = readFile(tempPath, false);
			
			if(completeFile == null)
				return numFld;
			
			for(int yy = 0; yy < completeFile.size(); yy++)
			{
				String line = completeFile.elementAt(yy).toString().trim();

        //append space after keyword b'coz 
				//if keyword is NUM_PROCESS123 0
				if(line.startsWith(objectName + " ")) // Method found
				{			
					arrFlds = rptUtilsBean.strToArrayData(line, " ");
  			}
		  }
			
			//appending the String buffer
			for(int indexValue = 0; indexValue < arrFlds.length; indexValue++)
      {
         if(indexValue == 1)
         {
        	 strFld1.setLength(0);
           strFld1.append(arrFlds[indexValue]);
         }
         else if((indexValue == 2)  && (strFld2.toString() != null))
         {
        	 strFld2.setLength(0);
      	   strFld2 = strFld2.append(arrFlds[indexValue]);
         }
         else if(indexValue == 3)
         {
        	 strFld3.setLength(0);
           strFld3 = strFld3.append(arrFlds[indexValue]);
         }
         else if(indexValue == 4)
         {
        	 strFld4.setLength(0);
           strFld4 = strFld4.append(arrFlds[indexValue]);
         }
         else if(indexValue == 5)
         {
        	 strFld5.setLength(0);
           strFld5 = strFld5.append(arrFlds[indexValue]);
         }
         else if(indexValue == 6)
         {
        	 strFld6.setLength(0);
           strFld6 = strFld6.append(arrFlds[indexValue]);
         }
      }
			
			return arrFlds.length;
		}
		catch (Exception ex)
		{
			Log.stackTraceLog(className, "getUrlBasedScalarKeyword", "", "", "Exception - ", ex);
			return numFld;
		}
	}
 
  //Name: copy2DArray
  // Purpose: Mainly for vector keyword. If first row has 2 column and second row has 3 column.
  // Arguments: 
  //     arg0: Old Array
  //     arg1: New array with exceed length
  //Return: 2D Array
  /////////////////////////////////////////////////////////////////////  
  public String[][] copy2DArray(String[][] arrOldTemp, String[][] arrNewTemp)
  {
  	Log.debugLog(className, "copy2DArray", "", "", "Method started");
  	try
  	{
  		System.arraycopy(arrOldTemp, 0, arrNewTemp, 0, arrOldTemp.length);

  		/**for(String[] strArr : arrNewTemp)
  		{
  			for (String str : strArr)
  				System.out.println("XXX = " + str);
  		}**/
  		return arrNewTemp;
  	}
  	catch(Exception ex)
  	{
  		Log.stackTraceLog(className, "copy2DArray", "", "", "Exception - ", ex);
  		return null;
  	}
  }
 
  //Name: getUrlBasedVectorKeyword
  // Purpose: Mainly for vector keyword. If first row has 2 column and second row has 3 column.
  // Arguments: 
  //     arg0: host name
  //     arg1: URL
  //     arg2: destination folder(control/real)
  //     arg3: Keyword name  
  //Return: 2D Array
  /////////////////////////////////////////////////////////////////////  
  public String[][] getUrlBasedVectorKeyword(String hostName, String URL, String destFolder, String objectName)
	{
		Log.debugLog(className, "getUrlBasedVectorKeyword", "", "", "Method started URL=" + URL + ", objectName=" + objectName + ", destFolder=" + destFolder);
		String[][] arrFlds = null;
		try
		{
		  String tempPath = "";
      Vector vecForSplitStr = new Vector();
      
	    if(destFolder.equals("default"))
	    	tempPath = getRealFolder() + "/" + hostName + "/" + URL; //real
	    else
	    	tempPath = getControlFolder() + "/" + hostName + "/" + URL; //control
	    
			Vector completeFile = readFile(tempPath, false);
			
			if(completeFile == null)
				return new String[0][0];
			for(int yy = 0; yy < completeFile.size(); yy++)
			{
				String line = completeFile.elementAt(yy).toString().trim();

	       //append space after keyword b'coz 
				//if keyword is NUM_PROCESS123 0
				if(line.startsWith(objectName + " ")) // Method found
				{			
				  vecForSplitStr.add(line);
  			}
		  }
			//Initalizing array length
			arrFlds = new String[vecForSplitStr.size()][vecForSplitStr.get(0).toString().split(" ").length];
		  for(int k = 0; k < vecForSplitStr.size(); k++)
		  {
		    String temp = vecForSplitStr.elementAt(k).toString();
		    
		    String[] arrTemp = temp.split(" ");
		    //arrFlds = new String[vecForSplitStr.size()][arrTemp.length];
		    if(arrTemp.length > arrFlds[k].length)
		    {
		    	Log.debugLog(className, "getUrlBasedVectorKeyword", "", "", "Length 0f new array=" + arrTemp.length);
		    	String arrNewTemp[][] = new String[vecForSplitStr.size()][arrTemp.length];
		    	
		    	//If array exceed from the previous value
		    	arrFlds = copy2DArray(arrFlds, arrNewTemp);
		    }

		    arrFlds[k] = arrTemp;
/*		    for(int kk = 0; kk < arrTemp.length; kk++)
		    {
		    	arrFlds[k][kk] = arrTemp[kk].toString();
		    	//System.out.println(" arrFlds[k][kk]  " + arrFlds[k][kk]);
  	    }
*/		  }
		  
		  //System.out.println(" length " + arrFlds.length); 
		  
		  /*int count = 0;
	    for(int uu = 0; uu < arrFlds.length; uu++)
	    {
	      System.out.println(" arrFlds  " + arrFlds[uu][0] + " " + arrFlds[uu][1] + " " + arrFlds[uu][2]);
	      count++;
	    }*/
			return arrFlds;
		}
		catch (Exception ex)
		{
			Log.stackTraceLog(className, "getUrlBasedVectorKeyword", "", "", "Exception - ", ex);
			return arrFlds;
		}
	}
  
  //Name: updateUrlBasedKeyword
  // Purpose: Mainly for update/delete keyword.
  // Arguments: 
  //     arg0: host name
  //     arg1: URL
  //     arg2: destination folder(control/real)
  //     arg3: Keyword name  
  //     arg4: operation name (update/delete). If scalar keyword it will add as well as update that keyword
  //     arg5: row Id. If scalar keyword it will be 0.    
  //     arg6: Keyword values
  //Return: boolean
  /////////////////////////////////////////////////////////////////////  
  public boolean updateUrlBasedKeyword(String hostName, String URL, String destFolder, String objectName, String operation, int[] rowsToUpdate, String[] fieldsValue)
	{
		Log.debugLog(className, "updateUrlBasedKeyword", "", "", "Method started URL=" + URL + ", objectName=" + objectName + ", destFolder=" + destFolder);
		try
		{
      String tempPath = "";
	    
	    if(destFolder.equals("default"))
	    	tempPath = getRealFolder() + "/" + hostName + "/" + URL; //real
	    else
	    	tempPath = getControlFolder() + "/" + hostName + "/" + URL; //control
	    
			Vector completeFile = readFile(tempPath, false);
			Vector modifiedVector = new Vector();
			
			if(completeFile == null)
				return false;
	
			int objectCtr = -1;
			String strToUpdate = "";	
     
			//If keyword found in the file otherwise add
			boolean keywordFound = false; 
			String strKeyword = "";
			
			//Checking valid keyword or not
			for(int jj = 0; jj < arrAvailableKeyword.length; jj++)
			{
				if(arrAvailableKeyword[jj].equals(objectName))
				  strKeyword = arrAvailableKeyword[jj];
			}
			
			//If non existing keyword
			if(strKeyword.equals(""))
				return false;	
			
			for(int yy = 0; yy < completeFile.size(); yy++)
			{
				String line = completeFile.elementAt(yy).toString().trim();
				
        //append space after keyword b'coz 
				//if keyword is NUM_PROCESS123 0
				if(line.startsWith(objectName + " ")) // Method found
				{
					objectCtr++;
					boolean matchToUpdate = false;
					for(int bb = 0; bb < rowsToUpdate.length; bb++)
					{
						if(rowsToUpdate[bb] == objectCtr)
						{
							matchToUpdate = true;
							if(operation.equals("update"))
							{
						    if(objectName.equals(strKeyword)) //request to add
						    {
						    	keywordFound = true;
						    	strToUpdate = objectName;
						    	if(operation.equals("update"))
						    	for(int jm = 0; jm < fieldsValue.length; jm++)
						    		strToUpdate = strToUpdate + " " + fieldsValue[jm];
						    }
  					    if((fieldsValue.length == 0) || (fieldsValue[0].equals("")))
						    	modifiedVector.remove(strToUpdate);
						    else
							    modifiedVector.add(strToUpdate);
							}
							if(operation.equals("delete"))
					    	modifiedVector.remove(objectName);							
							break;
						}
					}
					if(!matchToUpdate)
						modifiedVector.add(line);
				}
				else
					modifiedVector.add(line);
			}
			if((!keywordFound) && (!operation.equals("delete")))
			{
				String temp = objectName;
				for(int jm = 0; jm < fieldsValue.length; jm++)
					temp = temp + " " + fieldsValue[jm];	
				
				if((fieldsValue.length > 0)&& (!fieldsValue[0].trim().equals("")))
				  modifiedVector.add(temp);				
			}
			if(!writeToFile(tempPath, modifiedVector))
				return false;
			
			return true;
		}
		catch (Exception ex)
		{
			Log.stackTraceLog(className, "updateUrlBasedKeyword", "", "", "Exception - ", ex);
			return false;
		}
	}
	
  //Name: getHostNameFromControl
  // Purpose: get host name from control
  // Arguments:
  // Return: Array
  /////////////////////////////////////////////////////////////////////
  public String[] getHostNameFromControl()
  {
    Log.debugLog(className, "getHostNameFromControl", "", "", "Method called.");

    try
    {
      ArrayList alHostNames = new ArrayList();

      String hostDir = getControlFolder();
      File dir = new File(hostDir);

      //getting list files and directories
      File[] files = dir.listFiles();
      String[] hostNameList = dir.list();

      if(files == null)
      {
        Log.debugLog(className, "getHostNameFromControl", "", "", "Host is not present in control");
        return new String[]{""};
      }
      else
      {
        for(int index = 0; index < files.length; index++)
        {
        	//checking directory
          if(files[index].isDirectory()) 
          {
            alHostNames.add(hostNameList[index]);
          }
        }
      }

      //conver into 1D array
      String[] hostList = new String[alHostNames.size()];
      for(int j = 0; j < alHostNames.size(); j++)
      {
        hostList[j] = alHostNames.get(j).toString();
        Log.debugLog(className, "getHostNameFromControl", "", "", "Host Name found = " + hostList[j]);
      }
      return hostList;
    }

    catch (Exception ex)
    {
      Log.stackTraceLog(className, "getHostNameFromControl", "", "", "Exception - ", ex);
      return new String[] {""};
    }
  }
  
//Name: addHostDirs
  // Purpose: add host name in real
  // Arguments: hostname
  // Return:
  /////////////////////////////////////////////////////////////////////
  public void addHostDirs(String hostName)
  {
    Log.debugLog(className, "addHostDirs", "", "", "Method started hostName= " + hostName);
    try
    {
      String hostDir = getRealFolder();
      hostDir = hostDir + "/" + hostName;

      File fileCreateHostsDirs = new File(hostDir);

      if(!fileCreateHostsDirs.mkdirs()) //making directories
      {
        Log.debugLog(className, "addHostDirs", "", "", "Unable to create directory or it is already there. path = " + hostDir);
      }

    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "addHostDirs", "", "", "Exception - ", ex);
    }
  }

//Name: deleteHostDirs
  // Purpose: delete host name in real
  // Arguments: hostname
  // Return:
  /////////////////////////////////////////////////////////////////////
  public boolean deleteHostDirs(String hostName, String destFolder)
  {
    Log.debugLog(className, "deleteHostDirs", "", "", "Method started hostname=" + hostName);
    String tempPath = "";
    
    if(destFolder.equals("default"))
    	tempPath = getRealFolder() + "/" + hostName; //real
    else
    	tempPath = getControlFolder() + "/" + hostName; //control    
    try
    {
      correlationService_obj.deleteDirectory(new File(tempPath));
      return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "deleteHostDirs", "", "", "Exception - ", ex);
      return false;
    }
  }

//Name: getDirectory
  // Purpose: getting directories
  // Arguments: 
  //      arg0: path
  //      arg1: file path
  // Return:
  /////////////////////////////////////////////////////////////////////
  public void getDirectory(File path, Vector vecFilePaths)
  {
    Log.debugLog(className, "getDirectory", "", "", "path=" + path);
    try
    {
      //String strUrlPath = "";

      if( path.exists() )
      {
        File[] files = path.listFiles();

        CustomURLByHost tempCustomUrl = new CustomURLByHost();

        if(files.length <= 0)
        {
          tempCustomUrl.setUrl(path);
          tempCustomUrl.setUrlSize(path.length());

          long lastModifiedTime = path.lastModified();
          Date dateLastModified = new Date(lastModifiedTime);
          tempCustomUrl.setmodifiedDate(dateLastModified);

          vecFilePaths.add(tempCustomUrl);
          return;
        }

        for(int i=0; i<files.length; i++)
        {
           if(files[i].isDirectory())
           {
             getDirectory(files[i], vecFilePaths);
             //strUrlPath = strUrlPath + "/ " + temp;
           }
           else
           {
             File strUrlPath = new File(path + "/" + files[i].getName());
             tempCustomUrl.setUrl(strUrlPath);
             tempCustomUrl.setUrlSize(files[i].length());

             long lastModifiedTime = strUrlPath.lastModified();
             Date dateLastModified = new Date(lastModifiedTime);
             tempCustomUrl.setmodifiedDate(dateLastModified);

             vecFilePaths.add(tempCustomUrl);
           }
        }
      }


     // return strUrlPath;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "deleteDirectory", "", "", "Exception - ", ex);
     // return "";
    }
  }

//Name: getUrlsByHostName
  // Purpose: 2D Array
  // Arguments: hostname
  // Return: 2D Array
  /////////////////////////////////////////////////////////////////////
  public String[][] getUrlsByHostName(String hostName)
  {
    Log.debugLog(className, "getUrlsByHostName", "", "", "Method started hostname=" + hostName);
    try
    {
      Vector vecURL = new Vector();

      String hostDir = getRealFolder();
      hostDir = hostDir + "/" + hostName;

      File dir = new File(hostDir);
      getDirectory(dir, vecURL);
      String[][] data = new String[vecURL.size()][3];
      for(int i = 0 ; i < vecURL.size(); i++)
      {
      	CustomURLByHost temp = (CustomURLByHost)vecURL.get(i);
        data[i] = temp.getData();
        data[i][0] = temp.getUrlSelectedPath(hostName);
        System.out.println("  " + i + "  =  " + data[i][0] + "  " + data[i][1]);
      }
      return data;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "getUrlsByHostName", "", "", "Exception - ", ex);
      String[][] dummy = new String[0][3];
      return dummy;
    }
  }
 

//Name: chkHostNameInControl
  // Purpose: checking host name in control file and creating
  // Arguments: 
  //      arg0: hostname
  //      arg1: URL
  // Return: boolean
  /////////////////////////////////////////////////////////////////////
  public boolean chkHostNameInControl(String hostName, String URL)
  {
  	Log.debugLog(className, "chkHostNameInControl", "", "", "Method started hostName= " + hostName);
    try
    {
    	//URL = URL.substring(0, URL.lastIndexOf("."));   
      String hostDir = getControlFolder();
      hostDir = hostDir + "/" + hostName + "/" + URL;

      File fileCreateHostsDirs = new File(hostDir);

      if(!fileCreateHostsDirs.exists())
      {
        Log.debugLog(className, "chkHostNameInControl", "", "", "Unable to create directory or it is already there. path = " + hostDir);
        return false;
      }
      else
       return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "chkHostNameInControl", "", "", "Exception - ", ex);
      return false;
    }
  }
 
//Name: createHostNameInControl
  // Purpose: add host name in control
  // Arguments: 
  //      arg0: hostname
  //      arg1: URL  
  // Return:
  /////////////////////////////////////////////////////////////////////
  public boolean createHostNameInControl(String hostName, String URL)
  {
    Log.debugLog(className, "createHostNameInControl", "", "", "Method started hostName= " + hostName + ", URL= " + URL);
    try
    {
    	String osname = System.getProperty("os.name").trim().toLowerCase();
    	String fileName = "";
    	String seperator = "";
    	int lastIndexOf = -1;
    	if(osname.startsWith("win"))
    		seperator = "\\";
    	else	
    		seperator = "/";
    	
    	lastIndexOf = URL.lastIndexOf(seperator);
    	
    	fileName = URL.substring(lastIndexOf + 1);
    	
    	URL = URL.substring(0, lastIndexOf + 1);
  	
      String hostDir = getControlFolder();
      hostDir = hostDir + seperator + hostName + seperator + URL;
      
      File fileCreateHostsDirs = new File(hostDir);
      File fileCreate = new File(hostDir + seperator + fileName);
      
      if(!fileCreate.exists())
      {
      	fileCreateHostsDirs.mkdirs();
      	fileCreate.createNewFile();      	
        Log.debugLog(className, "createHostNameInControl", "", "", "Unable to create directory or it is already there. path = " + hostDir);
        return false;
      }
      else
       return true;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "createHostNameInControl", "", "", "Exception - ", ex);
      return false;
    }
  }
  
  public String[][] getUrlsByHostNameFromShell(String hostName)
  {
    Log.debugLog(className, "getUrlsByHostNameFromShell", "", "", "Method started hostName= " + hostName);
    try
    {
      String[][] arrDataValues = null;
    	String strCmdName = "noi_get_url"; 
      String strCmdArgs = "-h " + hostName + " -t real";
      
      CmdExec CmdExec_Obj = new CmdExec();
      Vector vecCmdOut = CmdExec_Obj.getResultByCommand(strCmdName, strCmdArgs, 0, null, "root");
      arrDataValues = rptUtilsBean.getRecFlds(vecCmdOut, "", "0|1|2", "-");
      
      if(arrDataValues != null)
      {
        return arrDataValues;
      }
      else
      	return new String[0][0];
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "createHostNameInControl", "", "", "Exception - ", ex);
      return new String[0][0];    	
    }
 
  }
 
  
  /*************************************Main Method*******************************/

  public static void main(String[] args)
  {
  	HPDURLBean hpdUrlBean_obj = new HPDURLBean();

    String hpdPath = hpdUrlBean_obj.getControlFolder();
    System.out.println(" hpdPath  " + hpdPath);

    String[] arrData = null;
    String[][] arrData2D = null;
    boolean bool2 = false;
  
    //arrData = hpdUrlBean_obj.getHostNameFromControl();
    
    //for(int i= 0; i < arrData.length; i++)
    //{
      //System.out.println("Host Name From Control--- " + arrData[i]);
    //}
    bool2 = hpdUrlBean_obj.chkkeywordPresentInControl("file_set", "JYOTI/kkk.txt", "urlBased", "HPD_DEBUG");
    System.out.println(" boolean present = " + bool2);
    
    arrData2D = hpdUrlBean_obj.getUrlBasedVectorKeyword("file_set", "JYOTI/kkk.txt", "urlBased", "SVC_CGI_TIME");
    String[] arrDataValue = new String[]{"224"};
    String strFld1 = "223";
    String[]data = new String[]{strFld1};
    
		for(String[] strArr : arrData2D)
		{
			for (String str : strArr)
				System.out.println("Result = " + str);
		}
     bool2 =  hpdUrlBean_obj.chkHostNameInControl("file_set", "JYOTI/ggg.docx");
    //bool2 = hpdUrlBean_obj.createHostNameInControl("default", "atul_test\\headers\\page_6.txt");
   
     //hpdUrlBean_obj.addUrlBaseKeyword("file_set", "JYOTI/kkk.txt", "urlBased", "SVC_CGI_TIME", arrDataValue);
     hpdUrlBean_obj.updateUrlBasedKeyword("atul_test", "pagesList.html", "default", "MODULEMASK", "update", new int[]{3}, data);     
   /** arrData = hpdUrlBean_obj.getUrlBasedScalarKeyword("file_set", "JYOTI/kkk.txt", "urlBased", "MODULEMASK");
    
    for(int i= 0; i < arrData.length; i++)
    {
      System.out.println("Keyword Values  --- " + arrData[i]);
    }    

   arrData = hpdUrlBean_obj.getUrlBasedScalarKeyword("file_set", "JYOTI/kkk.txt", "urlBased", "HPD_DEBUG");
   
    for(int i= 0; i < arrData.length; i++)
    {
      System.out.println("Keyword Values  --- " + arrData[i]);
    }    **/
    //System.out.println(" bool2 " + bool2);
    /**hpdConfiguration_obj.addHostDirs("JYOTI/jyoti1");
    arrData = hpdConfiguration_obj.getHostNameFromReal();

    for(int i= 0; i < arrData.length; i++)
    {
      System.out.println("Host Name  --- " + arrData[i]);
    }**/

   /** String[][] arrData2D = null;
    //arrData2D = hpdConfiguration_obj.getUrlsByHostName("file_set");

    //for(int i= 0; i < arrData2D.length; i++)
    //{
      //System.out.println("URL  --- " + arrData2D[i][0]);
      //System.out.println("Size  --- " + arrData2D[i][1]);
      //System.out.println("Modification Date  --- " + arrData2D[i][2]);
    //}**/

  }
}

//Creating another class from the getting URL, size and last modification time
class CustomURLByHost
{
  private File url;
  private long urlSize;
  private Date modifiedDate;

  public File getUrl()
  {
    return url;
  }

  public String getUrlSelectedPath(String hostName)
  {
    String strUrl = url.toString();
    strUrl = strUrl.substring(strUrl.indexOf(hostName)+ (hostName.length()+1));
    return strUrl;
  }

  public long getUrlSize()
  {
    return urlSize;
  }

  public Date getmodifiedDate()
  {
    return modifiedDate;
  }

  public String getFormatedDate()
  {
    SimpleDateFormat smt = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
    return smt.format(modifiedDate);
  }

  public String[] getData()
  {
    String data[] = new String[3];
    data[0] = url.toString();
    data[1] = urlSize + "";
    data[2] = getFormatedDate();

    return data;
  }

  public void setUrl(File url)
  {
    this.url = url;
  }

  public void setUrlSize(long urlSize)
  {
    this.urlSize = urlSize;
  }

  public void setmodifiedDate(Date modifiedDate)
  {
    this.modifiedDate = modifiedDate;
  }
}
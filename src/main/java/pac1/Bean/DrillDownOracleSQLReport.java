/**
 * Name : DrillDownOracleSQLReport.java
 * Purpose : This Class contains all the bean operations related to Oracle SQL Reports
 * Author : Sai Manohar
 * Modification History :
 */
package pac1.Bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import pac1.Bean.DrillDownExecuteQuery.getCommandOutput;

public class DrillDownOracleSQLReport 
{
	/**
	 * Variable used to store class Name 
	 */
	static String className = "DrillDownOracleSQLReport";
	
	/**
	 * Variable used to store the name of login user
	 */
	private String userName = "";
	
	/**
	 * Variable used to store the testRun Number
	 */
	private String testRun = "";
	
	/**
	 * Variable to store Name of Command
	 */
	String strCmdName = "";
	
	/**
	 * Variable to store Arguments of Command
	 */
	String strCmdArgs = "";
	
	/**
	 * Object for CmdExec bean
	 */
	CmdExec cmdExec = new CmdExec();

	/**
	 * Vector for storing the output of query
	 */
	Vector vecCmdOutput = new Vector();
	
	/**
	 * Query Name for SQL Order By Execution Time
	 */
	static String NSI_ORL_GET_SQL_STMT_ORD_BY_ELAPSED_TIME = "nsi_orl_get_sql_stmt_ord_by_elapsed_time";
	
	/**
	 * Query Name for SQL Order By CPU Time
	 */
	static String NSI_ORL_GET_SQL_STMT_ORD_BY_CPU_TIME = "nsi_orl_get_sql_stmt_ord_by_cpu_time";
	
	/**
	 * Query Name for SQL Order By I/O Time
	 */
	static String NSI_ORL_GET_SQL_STMT_ORD_BY_USER_IO_WAIT_TIME = "nsi_orl_get_sql_stmt_ord_by_user_io_wait_time";
	
	/**
	 * Query Name for SQL Order By Gets
	 */
	static String NSI_ORL_GET_SQL_STMT_ORD_BY_GETS = "nsi_orl_get_sql_stmt_ord_by_gets";
    
	/**
	 * Query Name for SQL Order By Reads
	 */
	static String NSI_ORL_GET_SQL_STMT_ORD_BY_READS = "nsi_orl_get_sql_stmt_ord_by_reads";
	
	/**
	 * Query Name for SQL Order By Executions
	 */
	static String NSI_ORL_GET_SQL_STMT_ORD_BY_EXECUTIONS = "nsi_orl_get_sql_stmt_ord_by_executions";
	
	/**
	 * Query Name for SQL Order By Parse Calls
	 */
	static String NSI_ORL_GET_SQL_STMT_ORD_BY_PARSE_CALLS = "nsi_orl_get_sql_stmt_ord_by_parse_calls";
	
	/**
	 * Query Name for SQL Order By Sharable Memory
	 */
	static String NSI_ORL_GET_SQL_STMT_ORD_BY_SHARABLE_MEMORY = "nsi_orl_get_sql_stmt_ord_by_sharable_memory";
	
	
	/**
	 * Query Name for SQL Oder By Version Count
	 */
	static String NSI_ORL_GET_SQL_STMT_ORD_BY_VERSION_COUNT = "nsi_orl_get_sql_stmt_ord_by_version_count";
	
	/**
	 * Query Name for SQL Order By Cluster Wait Time
	 */
	static String NSI_ORL_GET_SQL_STMT_ORD_BY_CLUSTER_WAIT_TIME = "nsi_orl_get_sql_stmt_ord_by_cluster_wait_time";
	
	
	/**
	 * Query Name for For Getting SQL Text	   
	 */
	static String NSI_ORL_GET_SQL_TEXT = "nsi_orl_get_sql_text";
	
	/**
	 * Query Name for Getting SQL Meta Data
	 */
	static String NSI_ORL_GET_SNAP_INFO = "nsi_orl_get_snap_info"; 
	
	getCommandOutput[] getCommandOutputObj = null;
	
	boolean isAliveAll = false;
	
	final static String GET_COUNT_OPTION = "--get_count";
	
	final static String DEFAULT_VALUE_GET_COUNT = "1"; //give row count

	static final String NSI_ORL_GET_SQL_STMT_ORD_BY_PHYSICAL_READS = "nsi_orl_get_sql_stmt_ord_by_physical_reads";
	  
	public static String IS_EXECUTE_QUERY_IN_THREAD_ENABLE = "1"; //Default thread is on
	
	public static String FAIL_QUERY = "1"; //give row count
	
	/**
	 * Array with set of colors
	 */
	public String[] arrColors = {"#7BB6FF","#C2C2C2","#E5E696","#FFFF88","#A7B41E","#DF3D82","#F3FAB6","#6B609A","#EB3DEE","#4BA614","#FFA443","#E13552","#ECECEA","#404040","#6DBDD6","#FFE658","#B71427","#118C4E","#C1E1A6","#FF9009"}; 
	
	/**
	 * Variable for reference of colors
	 */
	int usedColorIndex = -1;
	
	/**
	 * This is a hashmap that contains normalised ID with its color
	 * If normalization ID is same for two records then color should be same such that color must be unique.
	 */
	HashMap<String, String> normalIDcolorMap = new HashMap<String,String>();



    
	/**
	 * Getter for NormalIDColorMap
	 * @return
	 */
	public HashMap<String, String> getNormalIDcolorMap() 
    {
		return normalIDcolorMap;
	}

	/**
     * Constructor of this Class 
     * @Called when object is created
     * @param testRun
     * @param userName
     */
	public DrillDownOracleSQLReport(String testRun, String userName) 
	{
		this.testRun = testRun;
		this.userName = userName;
	}
	
	/**
	 * It takes the 2-D array (containing query ouput) as the input and returns a 2-D Array containing Table row number and its color    
	 * @param arrDataValues
	 * @return
	 */
	public HashMap<String, String> createNormalisedColors(String[][] arrDataValues)
	{
		Log.debugLog(className, "createNormalisedColors", "", "", "Method Starts , Length of arrDataValues = " + arrDataValues.length);
		try
		{
			for (int i = 1; i < arrDataValues.length; i++) 
			{
			   String currentNormId = 	arrDataValues[i][9];
			   if(normalIDcolorMap.containsKey(currentNormId))
			   {
				   if(normalIDcolorMap.get(currentNormId).equals(""))
				   {
					   usedColorIndex++;
					   try
					   {
					    normalIDcolorMap.put(currentNormId, arrColors[usedColorIndex]);
					   }
					   catch(ArrayIndexOutOfBoundsException aie)
					   {
						   normalIDcolorMap.put(currentNormId, ""); 
						   Log.debugLog(className, "createNormalisedColors", "", "", "Exception in  createNormalisedColors as all colors are used . Hence no color is assigned to normalization Id -  " + currentNormId);
						   Log.stackTraceLog(className, "createNormalisedColors", "", "", "Exception in  createNormalisedColors as all colors are used . Hence no color is assigned to normalization Id -  " + currentNormId  , aie);
					   }
				   }
			   }
			   else
				   normalIDcolorMap.put(currentNormId, ""); 
			}
		}
		catch(Exception e)
		{
			Log.stackTraceLog(className, "createNormalisedColors", "", "", "Exception in creating normalised Colors - ", e);
			return null;
		}
		return normalIDcolorMap;
	}
	
	/**
	 * This method is used to execute and get the data for Oracle/SQL Reports called for rptOracleSQLReport.jsp
	 * @param cmdArgs
	 * @param drillDownBreadCrumb
	 * @return
	 */
    public String[][] execute_nsi_get_oracle_sql_data(String cmdArg, String currLimit, StringBuffer totalRows, String currOffset, DrillDownBreadCrumb downBreadCrumb, String currentTab)
	{
		String[][] arrResult = null;
		Log.debugLog(className, "execute_nsi_get_oracle_sql_data", "", "", "Method Starts , Command Arguments = " + cmdArg + " ,  Limit = " + currLimit + " , offSet = " + currOffset);
		try
		{
	      long startTimeStamp =  System.currentTimeMillis();
	      if(downBreadCrumb.getFilterArument().equals("") || (!downBreadCrumb.getOffset().equals(currOffset) || !downBreadCrumb.getLimit().equals(currLimit) || !downBreadCrumb.getFilterArument().equals(cmdArg)))
	      {
	        String limitOffset = " --limit " + currLimit + " --offset " + currOffset;
	        
	        if(currentTab.trim().equals("1"))
	          strCmdName = NSI_ORL_GET_SQL_STMT_ORD_BY_ELAPSED_TIME;
	        else if(currentTab.trim().equals("2"))
	        	strCmdName = NSI_ORL_GET_SQL_STMT_ORD_BY_CPU_TIME;
	        else if(currentTab.trim().equals("3"))
	           strCmdName = NSI_ORL_GET_SQL_STMT_ORD_BY_USER_IO_WAIT_TIME;
	        else if(currentTab.trim().equals("4"))
	           strCmdName = NSI_ORL_GET_SQL_STMT_ORD_BY_GETS;
	        else if(currentTab.trim().equals("5"))
	           strCmdName = NSI_ORL_GET_SQL_STMT_ORD_BY_READS;
	        else if(currentTab.trim().equals("6"))
		           strCmdName = NSI_ORL_GET_SQL_STMT_ORD_BY_PHYSICAL_READS;
	        else if(currentTab.trim().equals("7"))
		           strCmdName = NSI_ORL_GET_SQL_STMT_ORD_BY_EXECUTIONS;
	        else if(currentTab.trim().equals("8"))
		           strCmdName = NSI_ORL_GET_SQL_STMT_ORD_BY_PARSE_CALLS;
	        else if(currentTab.trim().equals("9"))
		           strCmdName = NSI_ORL_GET_SQL_STMT_ORD_BY_SHARABLE_MEMORY;
	        else if(currentTab.trim().equals("10"))
		           strCmdName = NSI_ORL_GET_SQL_STMT_ORD_BY_VERSION_COUNT;
	        else if(currentTab.trim().equals("11"))
		           strCmdName = NSI_ORL_GET_SQL_STMT_ORD_BY_CLUSTER_WAIT_TIME;

	        Log.debugLog(className, "execute_nsi_get_oracle_sql_data", "", "", "Command Name = " + strCmdName);

	        vecCmdOutput = new Vector();

	        //creating inner class object up to 2 length to get count and data
	        getCommandOutputObj = new getCommandOutput[2];
	        Thread[] threadArr = new Thread[2];

	        for(int i = 0; i < getCommandOutputObj.length; i++)
	        {
	          //this boolean will give all thread started or not default false
	          if(i == getCommandOutputObj.length - 1)
	            isAliveAll = true;

	          //command with limit offset to get data
	          if(i == 0)
	            strCmdArgs = " --testrun " + testRun + " " + cmdArg + " " + GET_COUNT_OPTION + " " + DEFAULT_VALUE_GET_COUNT;
	          else
	            strCmdArgs = " --testrun " + testRun + " " + cmdArg + limitOffset;

	          Log.debugLog(className, "execute_nsi_get_oracle_sql_data", "", "", "Command Argument = " + strCmdArgs);

	          //creating class object array
	          getCommandOutputObj[i] = new getCommandOutput(i, strCmdName, strCmdArgs, this);

	          //if thread is enable
	          if(IS_EXECUTE_QUERY_IN_THREAD_ENABLE.equals("1"))
	          {
	            threadArr[i] = new Thread(getCommandOutputObj[i], "Query - " + i);

	            //adding uncaughtException Handler
	            setUncaughtExceptionHandlerDDR(threadArr[i]);

	            //Starting thread
	            threadArr[i].start();
	          }
	          else //if threading is not enable
	            getCommandOutputObj[i].getResultByCmdToGetOutput();
	        }

	        //wait untill all thread started
	        waitForQueryRunThreads(threadArr);

	        long endTimeStamp =  System.currentTimeMillis();

	        Log.debugLog(className, "execute_nsi_get_oracle_sql_data", "", "", "Total Time taken = " + (endTimeStamp - startTimeStamp));

	        vecCmdOutput = getCommandOutputObj[0].getQueryOutput();
	        if(!getCommandOutputObj[0].getQueryStatus())
	        {
	          totalRows.append(0);
	          arrResult = new String[2][1];
	          arrResult[0][0] = FAIL_QUERY;
	          arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
	          downBreadCrumb.setBreadCrumbData(arrResult);
	          return arrResult;
	        }
	        else
	        {
	           totalRows.append(vecCmdOutput.get(1).toString().trim());
	           //totalRows.append("100");
	        }

	        vecCmdOutput = new Vector();
	        vecCmdOutput = getCommandOutputObj[1].getQueryOutput();

	        if(!getCommandOutputObj[1].getQueryStatus())
	        {
	          arrResult = new String[2][1];
	          arrResult[0][0] = FAIL_QUERY;
	          arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
	          downBreadCrumb.setBreadCrumbData(arrResult);
	          return arrResult;
	        }
	        arrResult = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "-");
	        downBreadCrumb.setBreadCrumbData(arrResult);
	      }
	      else
	      {
	        arrResult = downBreadCrumb.getBreadCrumbData();
	        totalRows.append(downBreadCrumb.getTotalRecord());
	        downBreadCrumb.setBreadCrumbData(arrResult);
	      }
	      return arrResult;
	    }
	    catch (Exception e)
	    {
	      totalRows.append(0);
	      e.printStackTrace();
	      arrResult = new String[2][1];
	      arrResult[0][0] = FAIL_QUERY;
	      arrResult[1][0] = "No records are found";
	      Log.stackTraceLog(className, "execute_nsi_get_oracle_sql_data", "", "", "Exception - ", e);
	      return arrResult;
	    }
	  }
    
    /*//For Testing with manual query
    public String[][] execute_nsi_get_oracle_sql_data(String cmdArg, String currLimit, StringBuffer totalRows, String currOffset, DrillDownBreadCrumb downBreadCrumb, String currentTab)
	{
		String[][] arrResult = null;
		Log.debugLog(className, "execute_nsi_get_oracle_sql_data", "", "", "Method Starts , Command Arguments = " + cmdArg + " ,  Limit = " + currLimit + " , offSet = " + currOffset);
		try
		{
	      long startTimeStamp =  System.currentTimeMillis();
	      if(downBreadCrumb.getFilterArument().equals("") || (!downBreadCrumb.getOffset().equals(currOffset) || !downBreadCrumb.getLimit().equals(currLimit) || !downBreadCrumb.getFilterArument().equals(cmdArg)))
	      {
	        String limitOffset = " --limit " + currLimit + " --offset " + currOffset;
	        
	        if(currentTab.trim().equals("1"))
	          strCmdName = NSI_ORL_GET_SQL_STMT_ORD_BY_ELAPSED_TIME;
	        else if(currentTab.trim().equals("2"))
	        	strCmdName = NSI_ORL_GET_SQL_STMT_ORD_BY_CPU_TIME;
	        else if(currentTab.trim().equals("3"))
	           strCmdName = NSI_ORL_GET_SQL_STMT_ORD_BY_USER_IO_WAIT_TIME;
	        else if(currentTab.trim().equals("4"))
	           strCmdName = NSI_ORL_GET_SQL_STMT_ORD_BY_GETS;
	        else if(currentTab.trim().equals("5"))
	           strCmdName = NSI_ORL_GET_SQL_STMT_ORD_BY_READS;
	        else if(currentTab.trim().equals("6"))
		           strCmdName = NSI_ORL_GET_SQL_STMT_ORD_BY_PHYSICAL_READS;
	        else if(currentTab.trim().equals("7"))
		           strCmdName = NSI_ORL_GET_SQL_STMT_ORD_BY_EXECUTIONS;
	        else if(currentTab.trim().equals("8"))
		           strCmdName = NSI_ORL_GET_SQL_STMT_ORD_BY_PARSE_CALLS;
	        else if(currentTab.trim().equals("9"))
		           strCmdName = NSI_ORL_GET_SQL_STMT_ORD_BY_SHARABLE_MEMORY;
	        else if(currentTab.trim().equals("10"))
		           strCmdName = NSI_ORL_GET_SQL_STMT_ORD_BY_VERSION_COUNT;
	        else if(currentTab.trim().equals("11"))
		           strCmdName = NSI_ORL_GET_SQL_STMT_ORD_BY_CLUSTER_WAIT_TIME;
	        
	        
	        Log.debugLog(className, "execute_nsi_get_oracle_sql_data", "", "", "Command Name = " + strCmdName);

	        vecCmdOutput = new Vector();

	        //creating inner class object up to 2 length to get count and data
	        getCommandOutputObj = new getCommandOutput[1];
	        Thread[] threadArr = new Thread[1];

	        for(int i = 0; i < getCommandOutputObj.length; i++)
	        {
	          //this boolean will give all thread started or not default false
	          //if(i == getCommandOutputObj.length - 1)
	            //isAliveAll = true;

	          //command with limit offset to get data
	          //if(i == 0)
	        	 totalRows.append(100);
	            //strCmdArgs = " --testrun " + testRun + " " + cmdArg + " " + GET_COUNT_OPTION + " " + DEFAULT_VALUE_GET_COUNT;
	          //else
	          //{
	            strCmdArgs = " --testrun " + testRun + " " + cmdArg + limitOffset;

	          Log.debugLog(className, "execute_nsi_get_oracle_sql_data", "", "", "Command Argument = " + strCmdArgs);

	          //creating class object array
	          getCommandOutputObj[i] = new getCommandOutput(0, strCmdName, strCmdArgs, this);

	          //if thread is enable
	          if(IS_EXECUTE_QUERY_IN_THREAD_ENABLE.equals("1"))
	          {
	            threadArr[0] = new Thread(getCommandOutputObj[0], "Query - " + 0);

	            //adding uncaughtException Handler
	            setUncaughtExceptionHandlerDDR(threadArr[0]);

	            //Starting thread
	            threadArr[0].start();
	          }
	          else //if threading is not enable
	            getCommandOutputObj[1].getResultByCmdToGetOutput();
	          //}
	        }

	        //wait untill all thread started
	        waitForQueryRunThreads(threadArr);

	        long endTimeStamp =  System.currentTimeMillis();

	        Log.debugLog(className, "execute_nsi_get_oracle_sql_data", "", "", "Total Time taken = " + (endTimeStamp - startTimeStamp));

	        vecCmdOutput = getCommandOutputObj[0].getQueryOutput();
	        if(!getCommandOutputObj[0].getQueryStatus())
	        {
	          totalRows.append(0);
	          arrResult = new String[2][1];
	          arrResult[0][0] = FAIL_QUERY;
	          arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
	          downBreadCrumb.setBreadCrumbData(arrResult);
	          return arrResult;
	        }
	        else
	        {
	           //totalRows.append(vecCmdOutput.get(1).toString().trim());
	           //totalRows.append("100");
	        }

	        vecCmdOutput = new Vector();
	        vecCmdOutput = getCommandOutputObj[0].getQueryOutput();

	        if(!getCommandOutputObj[0].getQueryStatus())
	        {
	          arrResult = new String[2][1];
	          arrResult[0][0] = FAIL_QUERY;
	          arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
	          downBreadCrumb.setBreadCrumbData(arrResult);
	          return arrResult;
	        }
	        arrResult = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", "-");
	        downBreadCrumb.setBreadCrumbData(arrResult);
	      }
	      else
	      {
	        arrResult = downBreadCrumb.getBreadCrumbData();
	        totalRows.append(downBreadCrumb.getTotalRecord());
	        downBreadCrumb.setBreadCrumbData(arrResult);
	      }
	      return arrResult;
	    }
	    catch (Exception e)
	    {
	      totalRows.append(0);
	      e.printStackTrace();
	      arrResult = new String[2][1];
	      arrResult[0][0] = FAIL_QUERY;
	      arrResult[1][0] = "No records are found";
	      Log.stackTraceLog(className, "execute_nsi_get_oracle_sql_data", "", "", "Exception - ", e);
	      return arrResult;
	    }
	  }
*/
	
	/**
	 * This method returns the 2-D array containing SQL Text
	 * @param cmdArg
	 * @return
	 */
	 public String[][] execute_nsi_orl_get_sql_text(String cmdArg)
	  {
	      Log.debugLog(className, "execute_nsi_orl_get_sql_text", "", "", "Method called");
	      String arrResult[][] = null;
	      try
	      {
	         strCmdName = NSI_ORL_GET_SQL_TEXT;
	         strCmdArgs = " " + cmdArg;
	         Log.debugLog(className, "execute_nsi_orl_get_sql_text", "", "","Command Name With Arguments :" + strCmdName + strCmdArgs );
	         vecCmdOutput = new Vector();
	         boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);
	         Log.debugLog(className, "execute_nsi_orl_get_sql_text", "", "", "Executing Query. Flag After Execution :" + bolRsltFlag);
	         if(!bolRsltFlag)
	         {
	           arrResult = new String[2][1];
	           arrResult[0][0] = FAIL_QUERY;
	           arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
	           return arrResult;
	         }

	         arrResult = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", " ");
	         Log.debugLog(className, "execute_nsi_orl_get_sql_text", "", "", "Returning Array of Length : " + arrResult.length);
	         return arrResult;
	      }
	      catch(Exception e)
	      {
		     Log.stackTraceLog(className, "execute_nsi_orl_get_sql_text", "", "", "Exception - ", e);
	         return arrResult;
	      }
	  }
	 
	  /**
		* This method returns the 2-D array containing Oracle SQL Metadata
		* @param cmdArg
		* @return
		*/
	    public String[][] execute_nsi_orl_get_snap_info(String cmdArg)
		{
		  Log.debugLog(className, "execute_nsi_orl_get_snap_info", "", "", "Method called");
		  String arrResult[][] = null;
		  try
		  {
	         strCmdName = NSI_ORL_GET_SNAP_INFO;
	         strCmdArgs = " " + cmdArg;
	         Log.debugLog(className, "execute_nsi_orl_get_snap_info", "", "","Command Name With Arguments :" + strCmdName + strCmdArgs );
	         vecCmdOutput = new Vector();
	         boolean bolRsltFlag = cmdExec.getResultByCommand(vecCmdOutput, strCmdName, strCmdArgs, 0, userName, null);
	         Log.debugLog(className, "execute_nsi_orl_get_sql_text", "", "", "Executing Query. Flag After Execution :" + bolRsltFlag);
	         if(!bolRsltFlag)
	         {
	           arrResult = new String[2][1];
	           arrResult[0][0] = FAIL_QUERY;
	           arrResult[1][0] = getDataInStringBuff(vecCmdOutput).toString();
	           return arrResult;
		     }
	         arrResult = rptUtilsBean.getRecFlds(vecCmdOutput, "", "", " ");
	         Log.debugLog(className, "execute_nsi_orl_get_sql_text", "", "", "Returning Array of Length : " + arrResult.length);
	         return arrResult;
		   }
	      catch(Exception e)
	      {
		     Log.stackTraceLog(className, "execute_nsi_orl_get_sql_text", "", "", "Exception - ", e);
	         return arrResult;
	      }
	  }


	
	  /**
	   * This Method used to get Data In String Buffer from a vector
	   * @param vecData
	   * @return
	   */
	  public StringBuffer getDataInStringBuff(Vector vecData)
	  {
	    StringBuffer strBuff = new StringBuffer();

	    for(int i = 0; i < vecData.size(); i++)
	    {
	      strBuff.append(vecData.get(i).toString() + "<br>");
	    }

	    return strBuff;
	  }
	
	/**
	   * This method set the handler invoked when this thread abruptly terminates due to an uncaught exception.
	   *
	   * @return void
	   */
	  public void setUncaughtExceptionHandlerDDR(Thread currentThread)
	  {
	    Log.debugLog(className, "setUncaughtExceptionHandlerDDR", "", "", "Method Called.");
	    try
	    {
	      //Method Implementation for catching exception.
	      currentThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

	        public void uncaughtException(Thread t, Throwable e) {
	          // System.out.println(t.getName() + " throws exception := " + e.getMessage());

	          String msg = "KKKK = The Thread named "+ t.getName() + "\n The output and command args of both thread is as follows \n";
	          if(getCommandOutputObj != null)
	          {
	            try
	            {
	              for(int i = 0; i < getCommandOutputObj.length; i++)
	              {
	        	msg = msg + "The "+i+"th thread args = "+ getCommandOutputObj[i].strArgs + " \n";
	        	msg = msg + "And vector length = "+getCommandOutputObj[i].getQueryOutput().size()+"\n";
	        	msg = msg + "Thread Number for identifying error thread = "+getCommandOutputObj[i].threadNum+ "\n";
	        	msg = msg + "------------------------------------------------------";

	        	Log.stackTraceLog(className, "setUncaughtExceptionHandlerDDR", "", "", " Error is : "+msg, new Exception(e));
	              }
	            }
	            catch(Exception ex)
	            {
	              e.printStackTrace();
	              Log.stackTraceLog(className, "setUncaughtExceptionHandlerDDR", "", "", "Error in generating error msg : KKKK", ex);
	            }
	          }
	        }
	     });
	    }
	    catch(Exception e)
	    {
	      e.printStackTrace();
	      Log.stackTraceLog(className, "setUncaughtExceptionHandlerDDR", "", "", "Exception comming due to := ", e);
	    }
	  }

	  public synchronized void waitForQueryRunThreads(Thread[] arrthread)
	  {
	    Log.debugLog(className, "waitForQueryRunThreads", "", "", "Method called." + arrthread.length);
	    try
	    {
	      int pendingThreadCount = arrthread.length;

	      //joining threads
	      if(IS_EXECUTE_QUERY_IN_THREAD_ENABLE.equals("1"))
		   JoinRunningThreads(arrthread);
	    }
	    catch (Exception e) {
	      System.out.println("Exception in thread = " + e);
	      e.printStackTrace();
	      // TODO: handle exception
	    }
	  }
	  
	  //for joining two or more threads
	  public void JoinRunningThreads(Thread []threads)
	  {
	    if(threads == null)
	      return;


	    for(int i = 0; i < threads.length; i++)
	    {
	      try
	      {
		 threads[i].join();     //join
	      }
	      catch(Exception e)
	      {
		 e.printStackTrace();
	      }
	    }
	  }


	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		DrillDownOracleSQLReport drillDownOracleSQLReport = new DrillDownOracleSQLReport("2345","netstorm");

	}
	
	/**
	 *
	 * @author cavisson user
	 *
	 */
	  class getCommandOutput implements Runnable
	  {
	    private final String className = "getCommandOutput";
	    int threadNum = -1;
	    String strCmd = "";
	    String strArgs = "";
	    DrillDownOracleSQLReport drillDownOracleSQLReport = null;

	    private Vector vecData = new Vector();
	    private boolean bolRsltFlag = false;

	    public getCommandOutput(int threadNum, String strCmd, String strArgs, DrillDownOracleSQLReport drillDownOracleSQLReport)
	    {
	      Log.debugLog(className, "getCommandOutput", "", "", "Thread Number = " + threadNum);

	      this.threadNum = threadNum;
	      this.strCmd = strCmd;
	      this.strArgs = strArgs;
	      this.drillDownOracleSQLReport = drillDownOracleSQLReport;
	    }

	    public void run()
	    {
	      try
	      {
	        String threadName = Thread.currentThread().getName();

	        Log.debugLog(className, "run", "", "", "Thread Name = " + threadName);
	        getResultByCmdToGetOutput();
	        drillDownOracleSQLReport.notifyAll(); // Notify the parent object
	      }
	      catch (Exception e) {
		      //e.printStackTrace();
	        // TODO: handle exception
	      }
	    }

	    public void getResultByCmdToGetOutput()
	    {
	      Log.debugLog(className, "getResultByCmdToGetOutput", "", "", "Method Called");
	      bolRsltFlag = cmdExec.getResultByCommand(vecData, strCmd, strArgs, 0, userName, null);
	      //System.out.println("bolRsltFlag = "+bolRsltFlag);
	    }

	    public Vector getQueryOutput()
	    {
	      return vecData;
	    }

	    public boolean getQueryStatus()
	    {
	      return bolRsltFlag;
	    }
	  }

}

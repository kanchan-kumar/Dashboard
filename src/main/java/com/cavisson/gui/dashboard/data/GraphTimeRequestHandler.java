/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cavisson.gui.dashboard.data;

import java.io.Serializable;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import pac1.Bean.GraphUniqueKeyDTO;
import pac1.Bean.TestRunDataType;

public class GraphTimeRequestHandler implements Serializable
{
  private final static Logger logger = Logger.getLogger(GraphTimeBasedData.class.getName());
   
  private final String default_Key = "Last_240_Minutes";
  
  /*Object of TestRunDataType.*/
  private TestRunDataType testRunDataTypeObj = null;
  
  /*variable stores Start Time*/
  private String startTime = "NA";
  
  /*variable stores End Time*/
  private String endTime = "NA";
  
  /*variable stores Phase Name*/
  private String phaseName = "NA";
  
  /*variable stores Granularity.*/
  private int granularity = -1;
  
  /*variable for interval.*/
  private int interval = -1;
  
  /*variable for test run number.*/
  private int testRun;
  
  /*Array of Graph DTO object*/
  private GraphUniqueKeyDTO []arrActiveGraphUniqueKeyDTO = null;
   
   /**
    * Constructor to Make Request for applying Graph Time. 
   * @param testRun
    */
   public GraphTimeRequestHandler(int testRun)
   {
      ConsoleHandler handler = new ConsoleHandler();
      handler.setLevel(Level.ALL);
      logger.addHandler(handler);    
      
      this.testRun = testRun;
   }
   
   /**
    * Initialize Graph Time Request.
    * @param testRunDataTypeObj
    * @param startTime
    * @param endTime
    * @param phaseName
    * @param granularity 
   * @param interval 
   * @param arrActiveGraphUniqueKeyDTO 
    */
   public void initRequest(TestRunDataType testRunDataTypeObj, String startTime, String endTime, String phaseName, int granularity, int interval, GraphUniqueKeyDTO []arrActiveGraphUniqueKeyDTO)
   {
      logger.log(Level.FINE, "initRequest Method Called. startTime = {0}, endTime = {1}, phaseName = {2}, granularity = {3}, TimeBasedKey = {4}", new Object[]{startTime, endTime, phaseName, granularity, testRunDataTypeObj.getHMapKey()});
      
      this.testRunDataTypeObj = testRunDataTypeObj;
      this.startTime = startTime;
      this.endTime = endTime;
      this.phaseName = phaseName;
      this.granularity = granularity;
      this.interval = interval;
      this.arrActiveGraphUniqueKeyDTO = arrActiveGraphUniqueKeyDTO;
   }
   
   /**
    * Method is used to initialize with default.
    */
   public void initDefaultRequest()
   {
     logger.log(Level.FINE, "initDefaultRequest Method Called.");
     
     testRunDataTypeObj = TestRunDataType.getTestRunDataTypeObjByLastNKey(default_Key);
     
   }

  public TestRunDataType getTestRunDataTypeObj()
  {
    return testRunDataTypeObj;
  }

  public void setTestRunDataTypeObj(TestRunDataType testRunDataTypeObj)
  {
    this.testRunDataTypeObj = testRunDataTypeObj;
  }

  public String getStartTime()
  {
    return startTime;
  }

  public void setStartTime(String startTime)
  {
    this.startTime = startTime;
  }

  public String getEndTime()
  {
    return endTime;
  }

  public void setEndTime(String endTime)
  {
    this.endTime = endTime;
  }

  public String getPhaseName()
  {
    return phaseName;
  }

  public void setPhaseName(String phaseName)
  {
    this.phaseName = phaseName;
  }

  public int getGranularity()
  {
    return granularity;
  }

  public void setGranularity(int granularity)
  {
    this.granularity = granularity;
  }

  public int getTestRun()
  {
    return testRun;
  }

  public void setTestRun(int testRun)
  {
    this.testRun = testRun;
  }

  public int getInterval()
  {
    return interval;
  }

  public void setInterval(int interval)
  {
    this.interval = interval;
  }
 
  public GraphUniqueKeyDTO[] getArrActiveGraphUniqueKeyDTO()
  {
    return arrActiveGraphUniqueKeyDTO;
  }

  public void setArrActiveGraphUniqueKeyDTO(GraphUniqueKeyDTO[] arrActiveGraphUniqueKeyDTO)
  {
    this.arrActiveGraphUniqueKeyDTO = arrActiveGraphUniqueKeyDTO;
  } 
}

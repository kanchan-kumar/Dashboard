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
import pac1.Bean.GraphName.GraphNames;
import pac1.Bean.GraphUniqueKeyDTO;
import pac1.Bean.ReportData;
import pac1.Bean.TestRunDataType;
import pac1.Bean.TimeBasedTestRunData;

public class GraphTimeDataLayer implements Serializable
{
   private final static Logger logger = Logger.getLogger(GraphTimeBasedData.class.getName());
     
  /*Graph Time Request Handler Object.*/
  private GraphTimeRequestHandler graphTimeRequestHandlerObj = null;
  
  /*Object of Time Based Data.*/
  private TimeBasedTestRunData timeBasedTestRunDataObj = null;
  
  /*GraphNames object.*/
  private GraphNames graphNamesObj = null;
   
   /**
    * Constructor to Make Request for applying Graph Time. 
    */
   public GraphTimeDataLayer()
   {
      ConsoleHandler handler = new ConsoleHandler();
      handler.setLevel(Level.ALL);
      logger.addHandler(handler);        
   }
   
   /**
    * Initialize Graph Time Request.
    * @param graphTimeRequestHandlerObj 
    */
   public void initRequest(GraphTimeRequestHandler graphTimeRequestHandlerObj, GraphNames graphNamesObj)
   {
      logger.log(Level.FINE, "initRequest Method Called.");
      this.graphTimeRequestHandlerObj = graphTimeRequestHandlerObj;
      this.graphNamesObj = graphNamesObj;

   }
      
   /**
    * Method is used to process and get time based data.
    * @return 
    */
   public boolean processTimeBasedRequest()
   {
     try
     {
       logger.log(Level.FINE, "processTimeBasedRequest Method Called.");
       
       
       if(graphTimeRequestHandlerObj == null)
       {
         logger.log(Level.SEVERE, "Graph Time Request DTO must not be null.");
         return false;
       }
       
       ReportData reportDataObj = new ReportData(graphTimeRequestHandlerObj.getTestRun());
             
       if(graphTimeRequestHandlerObj.getInterval() == -1)
         graphTimeRequestHandlerObj.setInterval(graphNamesObj.getInterval());
       
       if(graphTimeRequestHandlerObj.getArrActiveGraphUniqueKeyDTO() == null)
         graphTimeRequestHandlerObj.setArrActiveGraphUniqueKeyDTO(graphNamesObj.getGraphUniqueKeyDTO());
      
       timeBasedTestRunDataObj = reportDataObj.getReqTimeBasedTestRunData(true, graphTimeRequestHandlerObj.getStartTime(), graphTimeRequestHandlerObj.getEndTime(), graphTimeRequestHandlerObj.getPhaseName(), graphTimeRequestHandlerObj.getGranularity(), 1, true, graphTimeRequestHandlerObj.getInterval(), 9, "netstorm", graphTimeRequestHandlerObj.getArrActiveGraphUniqueKeyDTO(), graphTimeRequestHandlerObj.getTestRunDataTypeObj());
            
       if(timeBasedTestRunDataObj == null)
       {
         logger.log(Level.SEVERE, "Error in Getting Time Based Data Object.");
         return false;
       }
       
       timeBasedTestRunDataObj.logInfo();
       
       return true;
     }
     catch(Exception e)
     {
       e.printStackTrace();
       return false;
     }
   }

  public TimeBasedTestRunData getTimeBasedTestRunDataObj()
  {
    return timeBasedTestRunDataObj;
  }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cavisson.gui.dashboard.data;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import pac1.Bean.GraphStats;
import pac1.Bean.GraphUniqueKeyDTO;
import pac1.Bean.TimeBasedDTO;
import pac1.Bean.TimeBasedTestRunData;

public class GraphTimeBasedData
{
  private final static Logger logger = Logger.getLogger(GraphTimeBasedData.class.getName());
  private TimeBasedTestRunData timeBasedTestRunDataObj = null;
  
  public GraphTimeBasedData(TimeBasedTestRunData timeBasedTestRunDataObj)
  {
    this.timeBasedTestRunDataObj = timeBasedTestRunDataObj;
    ConsoleHandler handler = new ConsoleHandler();
    handler.setLevel(Level.ALL);
    logger.addHandler(handler);
  }
  
  public TimeBasedDTO getTimeBasedDTOOfSelectedGraph(GraphUniqueKeyDTO graphUniqueKeyDTOObj)
  {
    try
    {
      logger.log(Level.FINE, "Inside getTimeBasedDTOOfSelectedGraph Method. DTO = {0}", graphUniqueKeyDTOObj);    
      return timeBasedTestRunDataObj.getTimeBasedDTO(graphUniqueKeyDTOObj);
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }
  
  public int getGraphSampleCount()
  {
    logger.log(Level.FINE, "Inside getGraphSampleCount Method.");    
    return timeBasedTestRunDataObj.getDataItemCount();
  }
  
  public long[] getTimeStampArray()
  {
    return timeBasedTestRunDataObj.getTimeStampArray();
  }
  
  public GraphStats getGraphStatsOfGraph(GraphUniqueKeyDTO graphUniqueKeyDTO)
  {
    return timeBasedTestRunDataObj.getGraphStatByGraphUniqueKeyDTO(graphUniqueKeyDTO);
  }
  
  public static void main(String []args)
  {
    
  }
  
}

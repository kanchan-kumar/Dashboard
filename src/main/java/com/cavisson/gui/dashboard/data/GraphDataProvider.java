/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cavisson.gui.dashboard.data;

import com.cavisson.gui.dashboard.components.charts.Impl.AreaChart;
import com.cavisson.gui.dashboard.components.charts.Impl.MultiAxisLineBar;
import com.cavisson.gui.dashboard.components.charts.Impl.StackedBarChart;
import com.vaadin.addon.charts.Chart;
import com.vaadin.ui.Component;
import java.io.Serializable;
import pac1.Bean.GraphName.GraphNames;
import pac1.Bean.ReportData;
import pac1.Bean.TestRunData;
import pac1.Bean.TimeBasedTestRunData;

public class GraphDataProvider implements Serializable
{
  
  private TimeBasedTestRunData timeBasedTestRunData = null;
  
  private GraphNames graphNames = null;
  
  private TestRunData testRunData = null;
  
  private int testRun = -1;
  
  public void initStart()
  {
    getAndSetConfiguredTestRun();
    configureTestRunSettings();
    getDefaultTimeBasedData();
    
  }
  
  private void getAndSetConfiguredTestRun()
  {
    this.testRun = 3661;
  }
  
  private void configureTestRunSettings()
  {
    ReportData reportDataObj = new ReportData(testRun);
    
    graphNames = reportDataObj.createGraphNamesObj();
  }
  
  
  private void getDefaultTimeBasedData()
  {
    try
    {
      GraphTimeRequestHandler graphTimeRequestHandlerObj = new GraphTimeRequestHandler(testRun);
      graphTimeRequestHandlerObj.initDefaultRequest();   
      
      GraphTimeDataLayer graphTimeDataLayer = new GraphTimeDataLayer();
      graphTimeDataLayer.initRequest(graphTimeRequestHandlerObj, graphNames);
      
      graphTimeDataLayer.processTimeBasedRequest();
      
      timeBasedTestRunData = graphTimeDataLayer.getTimeBasedTestRunDataObj();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public Component getAreaChart()
  {
    AreaChart areaChart = new AreaChart();
    return areaChart.getChart();
  }
  
  public Component getStackedBarChart()
  {
    StackedBarChart stackedChart = new StackedBarChart();
    return stackedChart.getChart();
  }
  
  public Component getMultiAxisLineBar()
  {
    MultiAxisLineBar multiAxis = new MultiAxisLineBar();
    return multiAxis.getChart();
  }
   
  public TimeBasedTestRunData getTimeBasedTestRunData()
  {
    return timeBasedTestRunData;
  }

  public GraphNames getGraphNames()
  {
    return graphNames;
  }

  public int getTestRun()
  {
    return testRun;
  }
}

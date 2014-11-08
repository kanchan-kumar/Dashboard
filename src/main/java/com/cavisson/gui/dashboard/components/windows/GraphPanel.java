/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cavisson.gui.dashboard.components.windows;

import com.cavisson.gui.dashboard.components.charts.Impl.AreaChart;
import com.cavisson.gui.dashboard.components.charts.Impl.LineChart;
import com.cavisson.gui.dashboard.components.charts.Impl.MultiAxisLineBar;
import com.cavisson.gui.dashboard.components.charts.Impl.MultipleTimeSeries;
import com.cavisson.gui.dashboard.components.charts.Impl.ResizeInsideVaadinComponent;
import com.cavisson.gui.dashboard.components.charts.Impl.StackedBarChart;
import com.vaadin.addon.charts.model.Axis;
import com.vaadin.addon.charts.model.AxisType;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.DataSeriesItem;
import com.vaadin.addon.charts.model.DateTimeLabelFormats;
import com.vaadin.addon.charts.model.Labels;
import com.vaadin.addon.charts.model.Marker;
import com.vaadin.addon.charts.model.MarkerSymbol;
import com.vaadin.addon.charts.model.MarkerSymbolEnum;
import com.vaadin.addon.charts.model.PlotLine;
import com.vaadin.addon.charts.model.PlotOptionsLine;
import com.vaadin.addon.charts.model.PlotOptionsSpline;
import com.vaadin.addon.charts.model.Title;
import com.vaadin.addon.charts.model.YAxis;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.server.AbstractClientConnector;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.vaadin.addon.leaflet.shared.Point;
import pac1.Bean.ExecutionDateTime;
import pac1.Bean.GraphUniqueKeyDTO;
import pac1.Bean.TimeBasedDTO;
import pac1.Bean.TimeBasedTestRunData;

/**
 *
 * @author pydi
 */
public class GraphPanel extends Panel implements Serializable
{

  int panelNumber = 0;
  int graphType = 0;
  GraphUniqueKeyDTO []arrGraphUniqueKeyDTO = null;
  RightPane rightPane = null;
  DataSeries dataSeries[] = null;
  Configuration configuration = null;

  /**
   * Create Panel with Specified Graph.
   *
   * @param panelNumber
   * @param graphUniqueKeyDTO
   */
  public GraphPanel(int panelNumber, GraphUniqueKeyDTO[] graphUniqueKeyDTO, RightPane rightPane)
  {
    this.panelNumber = panelNumber;
    this.arrGraphUniqueKeyDTO = graphUniqueKeyDTO;
    this.rightPane = rightPane;
    setContent(getGraphPanelChart());
    setSizeFull();
  }

  public Component getGraphPanelChart()
  {
    try
    {
      if (graphType == 1)
      {
        return getResizeInsideVaadinComponent();
      }
      else if (graphType == 2)
      {
        return getLineChart();
      }
      else if (graphType == 3)
      {
        return getStackedBarChart();
      }

      return getLineChart();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  private Component getMultiAxisLineBar()
  {
    MultiAxisLineBar multiAxis = new MultiAxisLineBar();
    return multiAxis.getChart();
  }

  private Component getResizeInsideVaadinComponent()
  {
    ResizeInsideVaadinComponent graphComponent = new ResizeInsideVaadinComponent();
    return graphComponent.createChart();
  }

  private Component getLineChart()
  {
    try
    {
      LineChart lineChart = new LineChart();   
      TimeBasedTestRunData timeBasedTestRunData = rightPane.getGraphDataProvider().getTimeBasedTestRunData();
      
      configuration = getLineChartConfiguration(timeBasedTestRunData);
      
      return lineChart.getChart(configuration);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  private Component getStackedBarChart()
  {
    StackedBarChart stackedChart = new StackedBarChart();
    return stackedChart.getChart();
  }

  private Component getAreaChart()
  {
    AreaChart areaChart = new AreaChart();
    return areaChart.getChart();
  }

  private Component getMultipleTimeSeries()
  {
    MultipleTimeSeries timeSeries = new MultipleTimeSeries();
    return timeSeries.getChart();
  }
    
  public Configuration getLineChartConfiguration(TimeBasedTestRunData timeBasedTestRunData)
  {
    try
    {
      dataSeries = new DataSeries[arrGraphUniqueKeyDTO.length];
      Configuration configuration = new Configuration();
      configuration.getChart().setType(ChartType.LINE);

      for(int i = 0; i < arrGraphUniqueKeyDTO.length; i++)
      {
         dataSeries[i] = getDataSeriesForGraph(arrGraphUniqueKeyDTO[i], timeBasedTestRunData);
         
         PlotOptionsLine plotOptions = new PlotOptionsLine();
         Marker marker = new Marker();
         marker.setSymbol(MarkerSymbolEnum.CIRCLE);
         plotOptions.setMarker(marker);
         
         dataSeries[i].setPlotOptions(plotOptions);
         configuration.addSeries(dataSeries[i]);
      }
         
      if(arrGraphUniqueKeyDTO.length == 1)
        configuration.getTitle().setText(rightPane.getGraphNamesObj().getGraphNameByGraphUniqueKeyDTO(arrGraphUniqueKeyDTO[0], true));
      else
      {
        String chartName = rightPane.getGraphNamesObj().getGroupNameByGraphUniqueKeyDTO(arrGraphUniqueKeyDTO[0], true);
        configuration.getTitle().setText(chartName);
      }
       
      Axis xAxis = configuration.getxAxis();
      xAxis.setType(AxisType.DATETIME);
      //xAxis.setTickPixelInterval(150);
      xAxis.setTitle("Time: Absolute");
      
      DateTimeLabelFormats dateFormat = new DateTimeLabelFormats();
      xAxis.setDateTimeLabelFormats(dateFormat);

      YAxis yAxis = configuration.getyAxis();
      yAxis.setTitle(new Title("Value"));
      //yAxis.setMin(0);
      yAxis.setStartOnTick(true);
      yAxis.setEndOnTick(true);
      
      PlotLine plotLine = new PlotLine(0, 1, new SolidColor("#808080"));
      yAxis.setPlotLines(plotLine);
      configuration.getTooltip().setEnabled(true);
      configuration.getLegend().setEnabled(false);
      configuration.getTooltip().setxDateFormat("%m:%d:%Y %H:%M:%S");
     
      //configuration.setPlotOptions(plotOptions);
         
      return configuration;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return new Configuration();
    }
  }
  
  private DataSeries getDataSeriesForGraph(GraphUniqueKeyDTO graphUniqueKeyDTO, TimeBasedTestRunData timeBasedTestRunData)
  {
    try
    {
      DataSeries dataSeries = new DataSeries();
      dataSeries.setPlotOptions(new PlotOptionsSpline());
      dataSeries.setName(rightPane.getGraphNamesObj().getGraphNameByGraphUniqueKeyDTO(graphUniqueKeyDTO, true));
      
      TimeBasedDTO timeBasedDTO = timeBasedTestRunData.getTimeBasedDTO(graphUniqueKeyDTO);
      
      for (int i = 0; i < timeBasedTestRunData.getDataItemCount(); i++)
      {
        DataSeriesItem item = getDataSeriesItem(timeBasedTestRunData.getTimeStampArray()[i], timeBasedDTO.getArrGraphSamplesData()[i]);
        dataSeries.add(item);
      }
     
      return dataSeries;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }
  
  public DataSeriesItem getDataSeriesItem(long timeStamp, double data)
  {
    try
    {
      Calendar calender = Calendar.getInstance(TimeZone.getDefault());
      calender.setTimeInMillis(timeStamp);
      DataSeriesItem dataItem = new DataSeriesItem(calender.getTime(), getDoubleValue(data));
      return dataItem;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }
  
  private double getDoubleValue(double input)
  {
    try
    {
      int decimalPlace = 3;
      BigDecimal bd = new BigDecimal(input);
      bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
      return (bd.doubleValue());
    }
    catch (Exception ex)
    {
      return 0;
    }
  }

  public int getGraphType()
  {
    return graphType;
  }

  public void setGraphType(int graphType)
  {
    this.graphType = graphType;
  }

  public GraphUniqueKeyDTO[] getPanelArrGraphUniqueKeyDTO()
  {
    return arrGraphUniqueKeyDTO;
  }

  public void setPanelArrGraphUniqueKeyDTO(GraphUniqueKeyDTO[] graphUniqueKeyDTO)
  {
    this.arrGraphUniqueKeyDTO = graphUniqueKeyDTO;
  }

  public RightPane getRightPane()
  {
    return rightPane;
  }

  public void setRightPane(RightPane rightPane)
  {
    this.rightPane = rightPane;
  }
}

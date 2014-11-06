/*--------------------------------------------------------------------
  @Name    : ReportGenerate.java
  @Author  : Abhishek
  @Purpose : Bean methods for genrration of graphs.
             Called from other bean files
  @Modification History:
    02/05/07 :  Abhishek:1.4.2 - Initial Version
    03/12/07 :  Abhishek:1.4.2 - Add more color pattern for scales of Axises.
    03/13/07 :  Abhishek:1.4.2 - Add color pattern upto 75 from 54

----------------------------------------------------------------------*/

package pac1.Bean;



import java.math.BigDecimal;
import javax.servlet.http.HttpSession;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.util.RelativeDateFormat;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.TextAnchor;

import java.awt.Color;
import java.io.*;
import java.util.*;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.awt.BasicStroke;
import java.awt.geom.Ellipse2D; // add to shape data in ovel for graph
import java.awt.*;

public class ReportGenerate implements java.io.Serializable
{

  class CustomRenderer extends BarRenderer3D {

      /** The colors. */
      private Paint[] colors;

      /**
       * Creates a new renderer.
       *
       * @param colors  the colors.
       */
      public CustomRenderer(final Paint[] colors) {
          this.colors = colors;
      }

      /**
       * Returns the paint for an item.  Overrides the default behaviour inherited from
       * AbstractSeriesRenderer.
       *
       * @param row  the series.
       * @param column  the category.
       *
       * @return The item color.
       */
      public Paint getItemPaint(final int row, final int column) {
          return this.colors[column % this.colors.length];
      }
  }

  String className = "ReportGenerate";
  int numTestRun = 0;
  private Vector vecXValue = new Vector();
  private Vector vecYValue = new Vector();
  private Vector vecRptName = new Vector();
  private int counter = 0;
  
  long testStartTimeInMillies = 0L;

  boolean bolCmpRpt = false; // This is true for compare report

  public ReportGenerate(int numTestRun)
  {
    this.numTestRun = numTestRun;    
    getTestStartTime();
  }
  
  /**
   * Method used to read test start time.
   */
  private void getTestStartTime()
  {
    /*Read Test Run Start Time.*/
    String strTestStartTime = Scenario.getTestRunStartTime(numTestRun);
    
    if(strTestStartTime.length() != 0)
      testStartTimeInMillies = ExecutionDateTime.convertFormattedDateToMilliscond(strTestStartTime, "MM/dd/yy HH:mm:ss", null);
    
  }

  public ReportGenerate(int numTestRun, boolean bolCmpRpt)
  {
    this.numTestRun = numTestRun;
    this.bolCmpRpt = bolCmpRpt;
    getTestStartTime();
  }

  private DateFormat getFormat(String timeFormat)
  {
    DateFormat formatter = null;
    if(timeFormat.equals("Elapsed") || bolCmpRpt)
    {
      RelativeDateFormat rdf = null; 

      if(bolCmpRpt)
	 rdf = new RelativeDateFormat();
      else
	rdf = new RelativeDateFormat(testStartTimeInMillies);
      
      rdf.setShowZeroDays(false);
      NumberFormat numberFormat = new DecimalFormat("0");
      numberFormat.setMinimumIntegerDigits(2);
      rdf.setSecondFormatter(numberFormat);
      rdf.setHourSuffix(":");
      rdf.setMinuteSuffix(":");
      rdf.setSecondSuffix("");
      rdf.setNumberFormat(numberFormat);
      
      return rdf;
    }
    else
      formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    
    return formatter;
  }

  private void setXYPlotProp(XYPlot plot)
  {
    plot.setBackgroundPaint(Color.white);
    plot.setDomainGridlinePaint(NSColor.graphHorizontalinecolor());
    plot.setRangeGridlinePaint(NSColor.graphHorizontalinecolor());

    plot.setDomainCrosshairVisible(true);
    plot.setRangeCrosshairVisible(true);
  }

  private void setCategoryPlotProp(CategoryPlot plot)
  {
    plot.setBackgroundPaint(Color.white);
    plot.setDomainGridlinePaint(NSColor.graphHorizontalinecolor());
    plot.setRangeGridlinePaint(NSColor.graphHorizontalinecolor());

    //plot.setDomainCrosshairVisible(true);
    //plot.setRangeCrosshairVisible(true);
  }

  private void setRenderer(StandardXYItemRenderer rr)
  {
    //rr.setPlotShapes(true);-------------------------------------------
    rr.setShapesFilled(true);
    rr.setSeriesStroke(0, new BasicStroke(1.0f));
    // Compare to RTG panels graphs, ovel size are 4 to 6 for large shape in DownLoad and Long graph
    rr.setSeriesShape(0, new Ellipse2D.Double(-2.0, -2.0, 4.0, 4.0));
  }

  private void setNumberAxis(NumberAxis rangeAxis, int i)
  {
    rangeAxis.setAutoRangeIncludesZero(false);
    rangeAxis.setLabelPaint(NSColor.lineColor(i));
    rangeAxis.setTickLabelPaint(NSColor.lineColor(i));
  }

  private void setStartAndEndDate(DateAxis axis, long startTimeInmilli, long endTimeInmilli)
  {
    try
    {
      Log.debugLog(className, "setStartAndEndDate", "", "", "Method called.");
      Log.debugLog(className, "setStartAndEndDate", "", "", "startTimeInmilli = " + startTimeInmilli);
      Log.debugLog(className, "setStartAndEndDate", "", "", "endTimeInmilli = " + endTimeInmilli);
      
      Date setEnddate = new Date(endTimeInmilli);
      // before setting maximum date value, need to check upper bound value
      axis.setMaximumDate(setEnddate);
      // before setting minimum date, need to check lower bound value
      Date setStartdate = new Date(startTimeInmilli);
      axis.setMinimumDate(setStartdate);
    }
    catch(Exception ex)
    {
      Log.errorLog(className, "setStartAndEndDate", "", "", "Exception Occurs while setting start and end date - " + ex);
    }
  }

  private double setScale(double tmpValues)
  {
    int decimalPlace = 3;// change for upto 3 decimal point average output
    if(Double.isNaN(tmpValues) || Double.isInfinite(tmpValues))
      tmpValues = 0;
    
    BigDecimal bd = new BigDecimal(tmpValues);
    bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_EVEN);
    tmpValues = bd.doubleValue();
    return tmpValues;
  }

  private LegendItemCollection getLegendItemCollection(String[] legendMsg)
  {
    LegendItemCollection lic = new LegendItemCollection();
    Paint paintObj = null;
    for(int i = 0; i < legendMsg.length; i++)
    {
      Log.debugLog(className, "getLegendItemCollection", "", "", "index = " + i + ", msg = "+ legendMsg[i]);

      paintObj = NSColor.lineColor(i);
      // create legend item
      LegendItem legendItem = new LegendItem(legendMsg[i], paintObj);
      // add legend item to legend item collection
      lic.add(legendItem);
    }
    return lic;
  }

  private XYDataset createXYDataset(XYSeriesCollection xySeriesCollection) {
    return xySeriesCollection;
  }

  private XYDataset createDataset1(TimeSeriesCollection dataset) {
    return dataset;
  }

  // for generating correlated chart
  public String generateChartCorrelate(String groupName, String graphFileName, String[] graphName, XYSeriesCollection xySeriesCollection, String reportSetName, int width, int heigth, String rptFilePath, String[] legendMsg)
  {
    String filename = "";
    Log.debugLog(className, "generateChartCorrelate", "", "", "Method called. Test Run is  " + numTestRun);
    try
    {
      JFreeChart chart = null;

      final XYDataset data = createXYDataset(xySeriesCollection);
      addDatasetForCSV(data, graphName);

      //XYItemRenderer renderer = new StandardXYItemRenderer();
      XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
      //renderer.setShapesFilled(true);
      renderer.setSeriesStroke(0, new BasicStroke(1.0f));
      renderer.setSeriesShape(0, new Ellipse2D.Double(-2.0, -2.0, 4.0, 4.0));
        //setRenderer(rr);

      final NumberAxis domainAxis = new NumberAxis(graphName[0]); // x-Axis
      setNumberAxis(domainAxis, 0);

      final NumberAxis rangeAxis = new NumberAxis(graphName[1]); // y-Axis
      setNumberAxis(rangeAxis, 0);

      XYPlot plot = new XYPlot(data, domainAxis, rangeAxis, renderer);
      setXYPlotProp(plot);

      chart = new JFreeChart(groupName, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
      chart.setBackgroundPaint(NSColor.ChartBgColor()); //change for background
      //final StandardLegend legend = (StandardLegend) chart.getLegend();
      //legend.setDisplaySeriesShapes(true);

      // set the legend to increase the size of bullet
      plot = chart.getXYPlot();
      plot.setRenderer(renderer);
      plot.setFixedLegendItems(getLegendItemCollection(legendMsg));

      ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
      filename = saveChartAsJPEG(chart, width, heigth, info, null, graphFileName, reportSetName, rptFilePath);
    }
    catch(Exception e)
    {
      Log.errorLog(className, "generateChartCorrelate", "", "", "Exception while Generating chart" + e);
      filename = "";
      e.printStackTrace();
    }
    return filename;
  }

  // for generating simple, multi, tile(with one rpt) & derived graphs
  public Object generateChart(String groupName, String graphFileName, TimeSeriesCollection[] dataset, String[] graphName, String graphType, String reportSetName, String timeFormat, int width, int heigth, long startTimeInmilli, long endTimeInmilli, String rptFilePath, String[] legendMsg, boolean anlsFlag)
  {
    String filename = "";
    DateFormat formatter = null;
    width = width + 60*(graphName.length - 1);

    Log.debugLog(className, "generateChart", "", "", "Method called. Test Run is  " + numTestRun);

    try
    {
      final XYDataset datasetXY = createDataset1(dataset[0]);

      for(int i = 0; i < dataset.length; i++)
        addDatasetForCSV(createDataset1(dataset[i]), graphName);

      final JFreeChart chart = ChartFactory.createTimeSeriesChart(
            groupName,
            timeFormat +" Time (Hour:Min:Sec)",
            "",
            datasetXY,
            true,
            true,
            false
        );

      //final StandardLegend legend = (StandardLegend) chart.getLegend();
      //legend.setDisplaySeriesShapes(true);

      formatter = getFormat(timeFormat);

      XYPlot plot = chart.getXYPlot();

      setXYPlotProp(plot);

      /*ValueAxis domainAxis = plot.getDomainAxis();

      if (domainAxis instanceof DateAxis)
      {
        DateAxis axis = (DateAxis) domainAxis;
        setStartAndEndDate(axis, startTimeInmilli, endTimeInmilli);
      }*/

      ValueAxis rangeAxis = plot.getRangeAxis();

      if (rangeAxis instanceof NumberAxis)
      {
        NumberAxis axis = (NumberAxis)rangeAxis;

        // for compare report use graph file name to range axis label
        if(bolCmpRpt)
        {
          String rangeAxisName = rptUtilsBean.undoReplaceName(graphFileName);
          axis.setLabel(rangeAxisName);
        }
        else
        {
          axis.setLabel(graphName[0]);
          setNumberAxis(axis, 0);
        }
      }

      //final XYItemRenderer renderer = plot.getRenderer();
      XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
      renderer.setToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());
      //renderer.setShapesFilled(true);
      renderer.setSeriesStroke(0, new BasicStroke(1.0f));
      renderer.setSeriesShape(0, new Ellipse2D.Double(-2.0, -2.0, 4.0, 4.0));
      renderer.setSeriesPaint(0, NSColor.lineColor(0));
      plot.setRenderer(renderer);
      
      for(int i = 1, n = graphName.length; i < n; i++)
      {
        // for compare report we want to show only one scale, for all graph(s)
        if(!bolCmpRpt)
        {
          NumberAxis axis2 = new NumberAxis(graphName[i]);
          setNumberAxis(axis2, i);
          plot.setRangeAxis(i, axis2);

          if(i%2 == 0)
            plot.setRangeAxisLocation(i, AxisLocation.BOTTOM_OR_LEFT);
          else
            plot.setRangeAxisLocation(i, AxisLocation.BOTTOM_OR_RIGHT);
        }

        XYDataset dataset11 = createDataset1(dataset[i]);
        plot.setDataset(i, dataset11);

        // for compare report don't set the data
        if(!bolCmpRpt)
          plot.mapDatasetToRangeAxis(i, i);

        //XYItemRenderer renderer2 = new StandardXYItemRenderer();
        XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer();

        renderer2.setSeriesStroke(0, new BasicStroke(1.0f));
        renderer2.setSeriesShape(0, new Ellipse2D.Double(-2.0, -2.0, 4.0, 4.0));
        renderer2.setSeriesPaint(0, NSColor.lineColor(i));
        plot.setRenderer(i, renderer2);
      }

      // set the legend to increse the size of bullet
      if(legendMsg != null)
      {
        plot = chart.getXYPlot();
        plot.setFixedLegendItems(getLegendItemCollection(legendMsg));
      }

      final DateAxis dateAxis = (DateAxis) plot.getDomainAxis();
      dateAxis.setVerticalTickLabels(false);
      dateAxis.setDateFormatOverride(formatter);

      chart.setBackgroundPaint(NSColor.ChartBgColor()); //change for background

      if(anlsFlag)
        return (Object)chart;

      ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
      filename = saveChartAsJPEG(chart, width, heigth, info, null, graphFileName, reportSetName, rptFilePath);
    }
    catch(Exception e)
    {
      Log.errorLog(className, "generateChart", "", "", "Exception while Generating chart-" + e);
      filename = "";
      e.printStackTrace();
    }
    return (Object)filename;
  }


  // for generating percentile graph
  public Object generatePercentileChart(String groupName, String graphFileName, XYSeriesCollection[] xySeriesCollection, String[] graphName, int width, int height, String reportSetName, String rptFilePath, String[] legendMsg, boolean anlsFlag)
  {
    String filename = "";
    width = 900;

    Log.debugLog(className, "generatePercentileChart", "", "", "Method called. Test Run is  " + numTestRun + ", graphFileName = " + graphFileName + ", groupName = " + groupName + ", rptFilePath = " + rptFilePath + ", reportSetName = " + reportSetName);

    try
    {
      final XYDataset datasetXY = createXYDataset(xySeriesCollection[0]);

      XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
      renderer.setSeriesPaint(0, NSColor.lineColor(0));
      renderer.setSeriesStroke(0, new BasicStroke(1.0f));
      renderer.setSeriesShape(0, new Ellipse2D.Double(-2.0, -2.0, 4.0, 4.0));

      final NumberAxis domainAxis = new NumberAxis("Percentile"); // x-Axis
      setNumberAxis(domainAxis, 0);

      String rangeAxisName = rptUtilsBean.undoReplaceName(graphFileName);
      final NumberAxis rangeAxis = new NumberAxis(rangeAxisName); // y-Axis
      setNumberAxis(rangeAxis, 0);

      XYPlot plot = new XYPlot(datasetXY, domainAxis, rangeAxis, renderer);
      setXYPlotProp(plot);

      JFreeChart chart = new JFreeChart(groupName, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
      plot = chart.getXYPlot();
      plot.setRenderer(renderer);
      
      for(int i = 1, n = xySeriesCollection.length; i < n; i++)
      {
        XYDataset datasetXY1 = createXYDataset(xySeriesCollection[i]);
        plot.setDataset(i, datasetXY1);
        XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer();
        renderer2.setSeriesStroke(0, new BasicStroke(1.0f));
        renderer2.setSeriesShape(0, new Ellipse2D.Double(-2.0, -2.0, 4.0, 4.0));
        renderer2.setSeriesPaint(0, NSColor.lineColor(i));
        plot.setRenderer(i, renderer2);
      }

      if(legendMsg != null)
      {
        plot = chart.getXYPlot();
        plot.setFixedLegendItems(getLegendItemCollection(legendMsg));
      }

      chart.setBackgroundPaint(NSColor.ChartBgColor()); //change for background

      if(anlsFlag)
        return (Object)chart;

      ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
      filename = saveChartAsJPEG(chart, width, height, info, null, graphFileName, reportSetName, rptFilePath);
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "generatePercentileChart", "", "", "Exception while Generating chart-" , e);
      filename = "";
    }
    return (Object)filename;
  }


  //This method is added on 13 Jan 2009 by Arun Goel
  //Adding three new graphs view type Percentile, Slab and Frequency Disrtibution.
  //This method is to generate Slab count graph. It is a bar chart.
  public Object generateSlabChart(String groupName, String graphFileName, CategoryDataset datasetSlab, String[] graphName, Paint[] barColors, int width, int height, String reportSetName, String rptFilePath, boolean anlsFlag)
  {
    Log.debugLog(className, "generateSlabChart", "", "", "TestNum = " + numTestRun);
    String filename = "";
    JFreeChart chart = null;
    try
    {
      boolean legendFlag = false;
      if(bolCmpRpt) // for compare reports keep legend on
        legendFlag = true;

        chart = ChartFactory.createBarChart3D(
            "",       // chart title
            "Slabs",               // domain axis label
            "Number Of Samples",                  // range axis label
            datasetSlab,                  // data
            PlotOrientation.VERTICAL, // orientation
            legendFlag,                    // include legend
            true,                     // tooltips?
            false                     // URLs?
        );

      chart.setBackgroundPaint(NSColor.ChartBgColor()); //change for background
      chart.setBorderVisible(false);

      final CategoryPlot plot = chart.getCategoryPlot();
      plot.setBackgroundPaint(Color.white);

      BarRenderer3D renderer1 = (BarRenderer3D)plot.getRenderer();
      //renderer1.setDrawBarOutline(true);
      renderer1.setMaximumBarWidth(0.1);
      renderer1.setItemMargin(0.0);
      plot.setForegroundAlpha(0.75f);

      final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
      setNumberAxis(rangeAxis, 0);
      rangeAxis.setUpperMargin(0.15);

      CategoryItemRenderer renderer = null;
      if(bolCmpRpt) // for compare reports keep same colors for same series
        renderer = renderer1;
      else
        renderer = new CustomRenderer(barColors);

      renderer.setSeriesItemLabelsVisible(0, Boolean.TRUE);

      Font ft = new Font("Tahoma", Font.BOLD, 11);
      renderer.setBaseItemLabelFont(ft);
      renderer.setBaseItemLabelPaint((Paint)Color.black);

     // renderer.setItemLabelGenerator((CategoryItemLabelGenerator)(new GraphsGenerateReport.LabelGenerator()));
      renderer.setItemLabelsVisible(true);

      final ItemLabelPosition p = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.TOP_LEFT, TextAnchor.TOP_LEFT, -Math.PI / 2.0);

      renderer.setPositiveItemLabelPosition(p);
      chart.getCategoryPlot().setRenderer(renderer);
      final CategoryAxis domainAxis = plot.getDomainAxis();
      domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));

      if(anlsFlag)
        return (Object)chart;

      // Here we save image
      ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
      filename = saveChartAsJPEG(chart, 900, height, info, null, graphFileName, reportSetName, rptFilePath);
    }
    catch(Exception e)
    {
      Log.errorLog(className, "generateSlabChart", "", "", "Exception while Generating chart" + e);
      Log.stackTraceLog(className, "generateSlabChart", "", "", "Exception - ", e);
      filename = "";
    }
    return (Object)filename;
  }


  //This method is added on 13 Jan 2009 by Arun Goel
  //Adding three new graphs view type Percentile, Slab and Frequency Disrtibution.
  //This method is to generate Frequency Distribution graph. It is a scatter chart.
  public Object generateFrequencyDist(String groupName, String graphFileName, XYSeriesCollection[] datasetFD, String[] graphName, double[] mean, int width, int height, String reportSetName, String rptFilePath, String[] legendMsg, boolean anlsFlag)
  {
    Log.debugLog(className, "generateFrequencyDist", "", "", "Method called. Test Run is  " + numTestRun);
    String filename = "";
    try
    {
      String fileName = rptUtilsBean.undoReplaceName(graphFileName);
      int index = fileName.indexOf("-");

      String domainAxisName = "";
      if(index > -1)
        domainAxisName = (fileName.substring(0, index)).trim();
      else
        domainAxisName = fileName;

      final XYDataset data = createXYDataset(datasetFD[0]);
      final JFreeChart chart = ChartFactory.createScatterPlot(
            groupName,
            domainAxisName,
            "Number Of Samples",
            data,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );

      XYPlot plot = (XYPlot)chart.getPlot();
      setXYPlotProp(plot);

      //XYItemRenderer r = plot.getRenderer();
      XYLineAndShapeRenderer r = new XYLineAndShapeRenderer();

      r.setSeriesPaint(0, NSColor.lineColor(0));
      r.setSeriesStroke(0, new BasicStroke(1.0f));
      r.setSeriesShape(0, new Ellipse2D.Double(-2.0, -2.0, 4.0, 4.0));
      plot.setRenderer(r);
      
      final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
      setNumberAxis(rangeAxis, 0);

      plot = chart.getXYPlot();
      for(int i = 1, n = datasetFD.length; i < n; i++)
      {
        XYDataset datasetXY1 = createXYDataset(datasetFD[i]);
        plot.setDataset(i, datasetXY1);

        Object obj = plot.clone();
        //XYItemRenderer renderer2 = ((XYPlot)obj).getRenderer();
        XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer();

        //XYItemRenderer rr = (XYItemRenderer)renderer2;

        renderer2.setSeriesStroke(0, new BasicStroke(1.0f));
        renderer2.setSeriesShape(0, new Ellipse2D.Double(-2.0, -2.0, 4.0, 4.0));
        renderer2.setSeriesPaint(0, NSColor.lineColor(i));

        plot.setRenderer(i, renderer2);
      }

      if(mean != null)
      {
        for(int i = 0; i < mean.length; i++)
        {
          //XYLineAnnotation L1 = new XYLineAnnotation(mean[i],0.0,mean[i],chart.getXYPlot().getRangeAxis().getMaximumAxisValue());
          XYLineAnnotation L1 = new XYLineAnnotation(mean[i],0.0,mean[i],chart.getXYPlot().getRangeAxis().getAutoRangeMinimumSize());
          chart.getXYPlot().addAnnotation(L1);
        }
      }

      chart.setBackgroundPaint(NSColor.ChartBgColor()); //change for background
      //chart.getXYPlot().getDomainAxis().setMinimumAxisValue(0);
      chart.getXYPlot().getDomainAxis().setAutoRangeMinimumSize(0);

      // set the legend to increse the size of bullet
      if(legendMsg != null)
      {
        plot = chart.getXYPlot();
        plot.setFixedLegendItems(getLegendItemCollection(legendMsg));
      }

      if(anlsFlag)
        return (Object)chart;

      ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
      filename = saveChartAsJPEG(chart, 900, height, info, null, graphFileName, reportSetName, rptFilePath);
    }
    catch(Exception e)
    {
      Log.errorLog(className, "generateFrequencyDist", "", "", "Exception while Generating chart-" + e);
      e.printStackTrace();
      filename = "";
    }
    return (Object)filename;
  }

  public String generateMultiChart(String groupName, String graphFileName, String[] graphName, TimeSeriesCollection[] dataset, String reportSetName, String timeFormat, int width, int heigth, String rptFilePath, String[] legendMsg)
  {
    String filename = "";
    Log.debugLog(className, "generateMultiChart", "", "", "Method called. Test Run is  " + numTestRun);

    DateFormat formatter = null;
    try
    {
      JFreeChart chart = null;
      formatter = getFormat(timeFormat);

      final DateAxis valueAxis = new DateAxis(timeFormat + " (Hour:Min:Sec)");
      final CombinedDomainXYPlot parent = new CombinedDomainXYPlot(valueAxis);
      parent.setGap(10.0);

      ValueAxis domainAxis = parent.getDomainAxis();

      /*if (domainAxis instanceof DateAxis)
      {
        DateAxis axis = (DateAxis) domainAxis;
      }*/
      DateAxis dateAxis = (DateAxis)parent.getDomainAxis();
      dateAxis.setVerticalTickLabels(false);
      dateAxis.setDateFormatOverride(formatter);

      // add subplot 1...
      for(int i = 0, n = dataset.length; i < n; i++)
      {
        final NumberAxis rangeAxis1 = new NumberAxis(graphName[i]);
        setNumberAxis(rangeAxis1, i);

        addDatasetForCSV(createDataset1(dataset[i]), graphName);

        //XYPlot plot = new XYPlot(dataset[i], domainAxis, rangeAxis1, new StandardXYItemRenderer());
        XYPlot plot = new XYPlot(dataset[i], domainAxis, rangeAxis1, new XYLineAndShapeRenderer());

        setXYPlotProp(plot);

        //XYItemRenderer renderer = plot.getRenderer();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesStroke(0, new BasicStroke(1.0f));
        renderer.setSeriesShape(0, new Ellipse2D.Double(-2.0, -2.0, 4.0, 4.0));
        plot.setRenderer(renderer);
        parent.add(plot, 1);
      }

      chart = new JFreeChart(groupName, JFreeChart.DEFAULT_TITLE_FONT, parent, true);
      chart.setBackgroundPaint(NSColor.ChartBgColor()); //change for background
      //final StandardLegend legend = (StandardLegend) chart.getLegend();
      //legend.setDisplaySeriesShapes(true);

      // set the legend to increse the size of bullet
      if(legendMsg != null)
      {
        XYPlot xyp = chart.getXYPlot();
        xyp.setFixedLegendItems(getLegendItemCollection(legendMsg));
      }

      ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
      filename = saveChartAsJPEG(chart, width, heigth*dataset.length, info, null, graphFileName, reportSetName, rptFilePath);
      chart = null;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "generateMultiChart", "", "", "Exception while Generating chart" + e);
      filename = "";
      e.printStackTrace();
    }
    return filename;
  }

  public String saveChartAsJPEG(JFreeChart jfreechart, int i, int j, ChartRenderingInfo chartrenderinginfo, HttpSession httpsession, String graphFileName, String reportSetName, String rptFilePath) throws IOException
  {
    if(jfreechart == null)
    {
      throw new IllegalArgumentException("Null 'chart' argument.");
    }
    else
    { // get absolute path through getGraphFileNameWithReportSetPath() function
      File file = new File(rptFilePath, graphFileName + ".jpeg");
      ChartUtilities.saveChartAsJPEG(file, jfreechart, i, j, chartrenderinginfo);
      return file.getName();
    }
  }

  public boolean addDataToXYSeriesAll(double[] arrRptData, double[] arrRptData1, XYSeries xySeries, XYSeriesCollection xySeriesCollection)
  {
    Log.debugLog(className, "addDataToXYSeriesAll", "", "", "Method called");

    int size = (arrRptData.length < arrRptData1.length)?arrRptData.length:arrRptData1.length;

    try
    {
      for(int i = 0; i < size; i++)
      {
        arrRptData[i] = setScale(arrRptData[i]);
        arrRptData1[i] = setScale(arrRptData1[i]);

        xySeries.add(arrRptData[i], arrRptData1[i]);
      }
      xySeriesCollection.addSeries(xySeries);

      return true;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "addDataToXYSeriesAll", "", "", "Exception in adding data - " + e);
      return false;
    }
  }
  
  /**
   * Method used to generate Data Set for Graph.
   * @param timeSeries
   * @param dataset
   * @param reportDataUtilsObj
   * @return
   */
  public boolean createDataSetForGraph(TimeSeries timeSeries, TimeSeriesCollection dataset, ReportDataUtils reportDataUtilsObj)
  {
    Log.debugLog(className, "createDataSetForGraph", "", "", "Method called.");
    try
    {
      
      if(reportDataUtilsObj == null)
      {
	Log.errorLog(className, "createDataSetForGraph", "", "", "Averaged Data is not Available For Graph.");
	return false;
      }
             
      double []arrSampleData = reportDataUtilsObj.getAverageSampleArray();
      long []arrTimeStamp = reportDataUtilsObj.getAveragTimeStampArray();
      
      for(int i = 0; i < reportDataUtilsObj.getSampleCount(); i++)
      {
	arrSampleData[i] = setScale(arrSampleData[i]);
	
	Millisecond time = null;
	
        if(bolCmpRpt) // this means, we need to plot x-axis starting from 00:00:00 (used in compare)
        {
          time = getElapsedTime(arrTimeStamp[i], arrTimeStamp[0]);
        }
        else
        {
          time = getElapsedTime(arrTimeStamp[i], 0L);
        }

        /*Adding value in Time Series.*/       
        timeSeries.addOrUpdate(time, arrSampleData[i]);
      }
      dataset.addSeries(timeSeries);
      return true;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      Log.errorLog(className, "createDataSetForGraph", "", "", "Exception in adding data - " + e);
      return false;
    }
  }
  
  /**
   * This function return the Millisecond object.
   * @param seqNum
   * @param interval
   * @return
   */
  public Millisecond getElapsedTime(long timeStamp, long startTime)
  {
    try
    {
      long diff = timeStamp - startTime;
      Date newDate = new Date(diff);
      int msec = (int) diff % 1000;
      int sec = newDate.getSeconds();
      int minute = newDate.getMinutes();
      int hour = newDate.getHours();
      Millisecond elapsedMSecond = new Millisecond(msec, sec, minute, hour, newDate.getDate(), (newDate.getMonth() + 1), (newDate.getYear() + 1900));
      return (elapsedMSecond);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }
  
  public boolean addDataToTimeSeriesAll(double arrValues[], TimeSeries timeSeries, TimeSeriesCollection dataset, int ii, double[] avgArrSeqNum, long interval, long timeInMilli)
  {
    Log.debugLog(className, "addDataToTimeSeriesAll", "", "", "Method called");
    try
    {
      for(int i = 0, n = arrValues.length; i < n; i++)
      {
        arrValues[i] = setScale(arrValues[i]);
        long seqNum = i;
        if(bolCmpRpt) // this means, we need to plot x-axis starting from 00:00:00 (used in compare)
        {
          if(i == 0)
            seqNum = 0;
          else
            seqNum = i * (long)(avgArrSeqNum[i] - avgArrSeqNum[i - 1]);
        }
        else
          seqNum = (long)avgArrSeqNum[i];


        Date newDate = new Date(timeInMilli + (seqNum)*interval);

        int msec = (int)((seqNum)*interval)%1000;
        int sec = newDate.getSeconds();
        int minute = newDate.getMinutes();
        int hour = newDate.getHours();
        /* Month index of calendar Compare to jFree charts SerialDate month index differ by 1.
        because Calendar month indexed from 0 to 11 while SerialDate month indexed from 1 to 12.
        so here is one index increased in Month of calendar.
        */
        timeSeries.addOrUpdate(new Millisecond(msec, sec, minute, hour, newDate.getDate(), (newDate.getMonth() + 1), (newDate.getYear() + 1900)), arrValues[i]);
      }
      dataset.addSeries(timeSeries);
      return true;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      Log.errorLog(className, "addDataToTimeSeriesAll", "", "", "Exception in adding data - " + e);
      return false;
    }
  }


  // This function add data value in X & Y Vector for ".csv" file
  private void addDatasetForCSV(XYDataset datasetXY, String[] graphNames)
  {
    Log.debugLog(className, "addDatasetForCSV", "", "", "Method called");

    for(int k = 0; k < graphNames.length; k++)
    {
      vecRptName.add(graphNames[k]);
      //Log.debugLog(className, "addDatasetForCSV", "", "", "Adding Report Name to vector = " + graphNames[k]);
    }

    for(int i = 0; i < datasetXY.getSeriesCount(); i++)
    {
      //Log.debugLog(className, "addDatasetForCSV", "", "", "Number of Sample = " + datasetXY.getItemCount(i));

      for(int j = 0; j < datasetXY.getItemCount(i); j++)
      {

        vecXValue.add("" + datasetXY.getX(i, j));
        //Log.debugLog(className, "addDatasetForCSV", "", "", "Adding X value to vector = " + datasetXY.getX(i, j));

        vecYValue.add("" + datasetXY.getY(i, j));
        //Log.debugLog(className, "addDatasetForCSV", "", "", "Adding Y value to vector = " + datasetXY.getY(i, j));
      }
    }
    counter++;
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
      return null;
    }
  }

  // This function Generate the ".csv" file for Simple, Multi & Tile Graph
  public boolean generateCSV(String fileName, String xAxisTimeFormat, String rptFilePath)
  {
    Log.debugLog(className, "generateCSV", "", "", "Method Starts.");

    String fileWithPath = rptFilePath + "/" + fileName + ".csv";

    try
    {
      // Read file's content
      File dataFile = openFile(fileWithPath);
      if(dataFile.exists())
        dataFile.delete();

      dataFile.createNewFile();

      FileOutputStream fout = new FileOutputStream(dataFile, true);  // append mode
      PrintStream pw = new PrintStream(fout);

      int numSample = (vecYValue.size()/counter);
      String rptInfoLine = "";
      String rptHdrLine = xAxisTimeFormat;

      for(int k = 0; k < counter; k++)
      {
        rptHdrLine = rptHdrLine + "," + vecRptName.elementAt(k).toString();
      }

      pw.println(rptHdrLine);

      for (int i = 0; i < numSample; i++)
      {
        Date df = new Date((long)(Double.parseDouble(vecXValue.elementAt(i).toString())));
        Log.debugLog(className, "generateCSV", "", "", "Date = " + df);

        String[] temp = null;
        rptInfoLine = "" + df;


        temp = rptUtilsBean.strToArrayData(rptInfoLine, " ");
        if(xAxisTimeFormat.equals("Elapsed"))
        {
          rptInfoLine = temp[3];
        }
        else
        {
          String temp1 = "" + (df.getMonth() + 1) + "/" + df.getDate() + "/" + (df.getYear() + 1900) + " " + temp[3];
          rptInfoLine = temp1;
        }

        for(int j = 0; j < counter; j++)
        {
          rptInfoLine = rptInfoLine + "," + vecYValue.elementAt(i + j * numSample).toString();
        }

        pw.println(rptInfoLine);
      }

      pw.close();
      fout.close();

      vecYValue = new Vector();
      vecXValue = new Vector();
      vecRptName = new Vector();
      counter = 0;

      Log.debugLog(className, "generateCSV", "", "", "OutPut File(" + fileWithPath + ") file is created successfuly.");

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "generateCSV", "", "", "Exception in adding record in  file (" + fileWithPath + ") - ", e);
      return false;
    }
  }


  // This function Generate the ".csv" file for Correlated Graph
  public boolean generateCSVForCorr(String fileName, String rptFilePath)
  {
    Log.debugLog(className, "generateCSVForCorr", "", "", "Method Starts.");

    String fileWithPath = rptFilePath + "/" + fileName + ".csv";

    try
    {
      // Read file's content
      File dataFile = openFile(fileWithPath);
      if(dataFile.exists())
        dataFile.delete();

      dataFile.createNewFile();

      FileOutputStream fout = new FileOutputStream(dataFile, true);  // append mode
      PrintStream pw = new PrintStream(fout);

      int numSample = (vecYValue.size()/counter);
      String rptInfoLine = "";
      String rptHdrLine = "";

      for(int k = 0; k < vecRptName.size(); k++)
      {
        if(k == 0)
          rptHdrLine = rptHdrLine + vecRptName.elementAt(k).toString();
        else
          rptHdrLine = rptHdrLine + "," + vecRptName.elementAt(k).toString();
      }

      pw.println(rptHdrLine);

      for (int i = 0; i < numSample; i++)
      {
        rptInfoLine = vecXValue.elementAt(i).toString() + "," + vecYValue.elementAt(i).toString();

        pw.println(rptInfoLine);
      }

      pw.close();
      fout.close();

      vecYValue = new Vector();
      vecXValue = new Vector();
      vecRptName = new Vector();
      counter = 0;

      Log.debugLog(className, "generateCSVForCorr", "", "", "OutPut File(" + fileWithPath + ") file is created successfuly.");

      return true;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "generateCSVForCorr", "", "", "Exception in adding record in  file (" + fileWithPath + ") - ", e);
      return false;
    }
  }


  /**************************************************************************************************
                      All these function created for Analysis GUI
  **************************************************************************************************/

  public JFreeChart generateMultiChartAna(String groupName, String[] graphName, TimeSeriesCollection[] dataset, String timeFormat, boolean legendFlag, int width, int heigth)
  {
    Log.debugLog(className, "generateMultiChartAna", "", "", "Method called. Test Run is  " + numTestRun);

    try
    {
      JFreeChart chart = null;
      final DateAxis valueAxis = new DateAxis(timeFormat + " (Hour:Min:Sec)");
      final CombinedDomainXYPlot parent = new CombinedDomainXYPlot(valueAxis);
      parent.setGap(10.0);

      ValueAxis domainAxis = parent.getDomainAxis();
      for(int i = 0, n = dataset.length; i < n; i++)
      {
        if(graphName[i] != "Hide")
        {
          final NumberAxis rangeAxis1 = new NumberAxis(graphName[i]);
          setNumberAxis(rangeAxis1, i);

          addDatasetForCSV(createDataset1(dataset[i]), graphName);

          //final XYPlot plot = new XYPlot(dataset[i], domainAxis, rangeAxis1, new StandardXYItemRenderer());
          final XYPlot plot = new XYPlot(dataset[i], domainAxis, rangeAxis1, new XYLineAndShapeRenderer());

          //XYItemRenderer renderer = plot.getRenderer();
          XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
          renderer.setSeriesPaint(0, NSColor.lineColor(i));
          renderer.setSeriesStroke(0, new BasicStroke(1.0f));
          renderer.setSeriesShape(0, new Ellipse2D.Double(-2.0, -2.0, 4.0, 4.0));
          plot.setRenderer(renderer);
          parent.add(plot, 1);
        }
      }

      chart = new JFreeChart(groupName, JFreeChart.DEFAULT_TITLE_FONT, parent, true);

      //final StandardLegend legend = (StandardLegend) chart.getLegend();--------------
      //legend.setDisplaySeriesShapes(true);

      if(!legendFlag)
        //chart.setLegend(null);
        chart.getLegend().visible = false;

      return chart;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "generateMultiChartAna", "", "", "Exception while Generating chart" + e);
      e.printStackTrace();
      return null;
    }
  }

  //This method is added on 15 Jan 2009 by Arun Goel
  //Adding three new graphs view type Percentile, Slab and Frequency Disrtibution.
  //This method is to generate Percentile graph. It is a line chart.
  public JFreeChart generatePercentileChartAna(String groupName, XYSeriesCollection[] xySeriesCollection, String[] graphName, boolean legendFlag)
  {
    Log.debugLog(className, "generatePercentileChartAna", "", "", "Method called. Test Run is  " + numTestRun);
    try
    {
      JFreeChart chart = null;

      final XYDataset data = createXYDataset(xySeriesCollection[0]);
      XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
      renderer.setSeriesPaint(0, NSColor.lineColor(0));
      renderer.setSeriesStroke(0, new BasicStroke(1.0f));
      renderer.setSeriesShape(0, new Ellipse2D.Double(-2.0, -2.0, 4.0, 4.0));

      final NumberAxis domainAxis = new NumberAxis("Percentile"); // x-Axis
      setNumberAxis(domainAxis, 0);

      final NumberAxis rangeAxis = new NumberAxis(graphName[0]); // y-Axis
      setNumberAxis(rangeAxis, 0);

      final XYPlot plot = new XYPlot(data, domainAxis, rangeAxis, renderer);
      setXYPlotProp(plot);
      plot.setRenderer(renderer);
      
      chart = new JFreeChart(groupName, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
      if(!legendFlag)
        chart.getLegend().visible = false;

      return chart;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "generatePercentileChartAna", "", "", "Exception while Generating chart" , e);
      return null;
    }
  }


  //This method is added on 13 Jan 2009 by Arun Goel
  //Adding three new graphs view type Percentile, Slab and Frequency Disrtibution.
  //This method is to generate Slab count graph. It is a bar chart.

  public JFreeChart generateSlabChartAna(CategoryDataset datasetSlab, int intTestNum, String strGraphName, Paint[] barColors)
  {
    Log.debugLog(className, "generateSlabChartAna", "", "", "TestNum="+intTestNum+", GraphName="+strGraphName);
    JFreeChart chart = null;
    try
    {
      chart = ChartFactory.createBarChart3D("", "Slabs", "Number Of Samples", datasetSlab, PlotOrientation.VERTICAL, false, true, false);

      chart.setBackgroundPaint(Color.white);
      chart.setBorderVisible(false);

      final CategoryPlot plot = chart.getCategoryPlot();
      plot.setBackgroundPaint(Color.white);

      BarRenderer3D renderer1 = (BarRenderer3D)plot.getRenderer();
      //renderer1.setDrawBarOutline(true);
      renderer1.setMaximumBarWidth(0.1);
      plot.setForegroundAlpha(0.75f);

      final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
      rangeAxis.setUpperMargin(0.15);
      final CategoryItemRenderer renderer = new CustomRenderer(barColors);
      renderer.setSeriesItemLabelsVisible(0, Boolean.TRUE);

      Font ft = new Font("Tahoma", Font.BOLD, 11);
      renderer.setBaseItemLabelFont(ft);

      //renderer.setItemLabelGenerator((CategoryItemLabelGenerator)(new GraphsGenerateReport.LabelGenerator()));
      renderer.setItemLabelsVisible(true);

      final ItemLabelPosition p = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.TOP_LEFT, TextAnchor.TOP_LEFT, -Math.PI / 2.0);

      renderer.setPositiveItemLabelPosition(p);
      chart.getCategoryPlot().setRenderer(renderer);
      final CategoryAxis domainAxis = plot.getDomainAxis();
      domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "generateSlabChartAna", "", "", "Exception while Generating chart" , e);
      chart = null;
    }
    return chart;
  }


  //This method is added on 13 Jan 2009 by Arun Goel
  //Adding three new graphs view type Percentile, Slab and Frequency Disrtibution.
  //This method is to generate Frequency Distribution graph. It is a scatter chart.
  public JFreeChart generateFrequencyDistAna(String groupName, XYSeriesCollection[] datasetFD, String[] graphName, boolean legendFlag, double mean)
  {
    Log.debugLog(className, "generateFrequencyDistAna", "", "", "Method called. Test Run is  " + numTestRun);

    try
    {
      int index = graphName[0].indexOf("-");
      String domainAxisName = "";
      if(index > -1)
        domainAxisName = (graphName[0].substring(0, index)).trim();
      else
        domainAxisName = graphName[0];

      final XYDataset data = createXYDataset(datasetFD[0]);

      final JFreeChart chart = ChartFactory.createScatterPlot(
            groupName,
            domainAxisName,
            "Number Of Samples",
            data,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );

      XYPlot plot = (XYPlot)chart.getPlot();
      XYItemRenderer r = plot.getRenderer();
      r.setSeriesPaint(0, NSColor.lineColor(0));
      r.setSeriesStroke(0, new BasicStroke(1.0f));
      r.setSeriesShape(0, new Ellipse2D.Double(-2.0, -2.0, 4.0, 4.0));
      //XYLineAnnotation L1 = new XYLineAnnotation(mean,0.0,mean,chart.getXYPlot().getRangeAxis().getMaximumAxisValue());
      XYLineAnnotation L1 = new XYLineAnnotation(mean,0.0,mean,chart.getXYPlot().getRangeAxis().getAutoRangeMinimumSize());
      chart.getXYPlot().addAnnotation(L1);
      return chart;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "generateFrequencyDistAna", "", "", "Exception while Generating chart-" + e);
      e.printStackTrace();
      return null;
    }
  }

  public JFreeChart generateChartAna(String groupName, TimeSeriesCollection[] dataset, String[] graphName, String timeFormat, boolean legendFlag, int width, int heigth)
  {
    DateFormat formatter = null;
    width = width + 60*(graphName.length - 1);

    Log.debugLog(className, "generateChartAna", "", "", "Method called. Test Run is  " + numTestRun);

    try
    {
      final XYDataset datasetXY = createDataset1(dataset[0]);

      final JFreeChart chart = ChartFactory.createTimeSeriesChart(
            groupName,
            timeFormat +" Time (Hour:Min:Sec)",
            "",
            datasetXY,
            true,
            true,
            false
        );

      //final StandardLegend legend = (StandardLegend) chart.getLegend();---------------
      //legend.setDisplaySeriesShapes(true);
      if(!legendFlag)
        //chart.setLegend(null);
        chart.getLegend().visible = false;
      
      XYPlot plot = chart.getXYPlot();

      setXYPlotProp(plot);

      for(int i = 1, n = graphName.length; i < n; i++)
      {
        if(i%2 == 0)
          plot.setRangeAxisLocation(i, AxisLocation.BOTTOM_OR_LEFT);//This is not done in the AnlsGraphPanel.java:Atul
        else
          plot.setRangeAxisLocation(i, AxisLocation.BOTTOM_OR_RIGHT);

        XYDataset dataset11 = createDataset1(dataset[i]);//This is not done in the AnlsGraphPanel.java:Atul
        plot.setDataset(i, dataset11);
        plot.mapDatasetToRangeAxis(i, i);

      }

      final DateAxis dateAxis = (DateAxis) plot.getDomainAxis();//This is not done in the AnlsGraphPanel.java:Atul
      dateAxis.setVerticalTickLabels(false);
      dateAxis.setDateFormatOverride(formatter);
      return chart;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "generateChartAna", "", "", "Exception while Generating chart-" + e);
      e.printStackTrace();
      return null;
    }
  }

  public JFreeChart generateChartCorrelateAna(String groupName, String[] graphName, XYSeriesCollection xySeriesCollection, boolean legendFlag, int width, int heigth)
  {
    Log.debugLog(className, "generateChartCorrelateAna", "", "", "Method called. Test Run is  " + numTestRun);
    try
    {
      JFreeChart chart = null;

      final XYDataset data = createXYDataset(xySeriesCollection);
      addDatasetForCSV(data, graphName);

      //XYItemRenderer renderer = new StandardXYItemRenderer();
      XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
      renderer.setSeriesPaint(0, NSColor.lineColor(0));
      renderer.setSeriesStroke(0, new BasicStroke(1.0f));
      renderer.setSeriesShape(0, new Ellipse2D.Double(-2.0, -2.0, 4.0, 4.0));

      final NumberAxis domainAxis = new NumberAxis(graphName[0]); // x-Axis
      setNumberAxis(domainAxis, 0);

      final NumberAxis rangeAxis = new NumberAxis(graphName[1]); // y-Axis
      setNumberAxis(rangeAxis, 0);

      final XYPlot plot = new XYPlot(data, domainAxis, rangeAxis, renderer);
      setXYPlotProp(plot);
      plot.setRenderer(renderer);
      
      chart = new JFreeChart(groupName, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
      //final StandardLegend legend = (StandardLegend) chart.getLegend();
      //legend.setDisplaySeriesShapes(true);
      if(!legendFlag)
        //chart.setLegend(null);----------------------------------------
        chart.getLegend().visible = false;
      return chart;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "generateChartCorrelateAna", "", "", "Exception while Generating chart" + e);
      e.printStackTrace();
      return null;
    }
  }

  public static void main(String[] args)
  {
  }
}



// Name    : GenerateChart.java
// Author  : Vinit Tyagi
// Purpose : Add purpose of this java file
// Modification History:
//  09/03/05:1.4:Vinit: Initial Version
//  10/03/05:1.4:Vinit Modified use the chartColor.ini for customized colors
//  10/17/05:1.4:Vinit Modified remove truncation of String
//                     add a function to make a x axis value Unique
//  10/18/05:1.4:Vinit Modified adding % sign on label of pie chart


package pac1.Bean;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Image;
import java.awt.Paint;
import java.awt.geom.Ellipse2D;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.Vector;

import javax.servlet.http.HttpSession;
import javax.swing.ImageIcon;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryAxis3D;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberAxis3D;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.imagemap.ImageMapUtilities;
import org.jfree.chart.imagemap.StandardToolTipTagFragmentGenerator;
import org.jfree.chart.imagemap.StandardURLTagFragmentGenerator;
import org.jfree.chart.imagemap.ToolTipTagFragmentGenerator;
import org.jfree.chart.imagemap.URLTagFragmentGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.GroupedStackedBarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer3D;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.ui.TextAnchor;
import org.jfree.util.Rotation;
import org.jfree.util.StringUtils;

import pac1.Bean.GraphName.*;

public class GenerateChart
{
  static String className = "GenerateChart";
  static Map map = null;
  static Color bgcolor = NSColor.ChartBgColor();
  static float barTransparancy = 1.0f;
  static float pieTransparancy = 0.9f;
  static boolean toolTip = false;
  private static boolean disableLegend = false;
  private static boolean writetooltipDataIntoFile = false;

  public static boolean isIgnoreCase = true;
 // Color colorList[] = {Color.yellow, Color.red, Color.blue, Color.green, Color.cyan, Color.magenta, Color.ORANGE, Color.darkGray};
  //these colors are taken from NSColor
  String colorListForJspTable[] = {"000000", "FF6600", "6600CC", "CC9900", "FF0000", "008080", "800000", "FF2850", "0099CC", "CD5B45", "33CC33", "A0522D", "844200", "000066", "FF6347", "A52A2A"};
  //static PrintWriter pw=new PrintWriter(Writer());

  public static double[][] getShare(double[][] dataValues,double[] total)
  {
    Log.debugLog(className, "getShare", "", "", "Method Called, dataValues.length = " + dataValues.length + ", total.length = " + total.length);

    try
    {
      double share[][] = new double[dataValues.length][dataValues[0].length];
      for(int i=0;i<dataValues.length;i++)
        for(int j=0;j<dataValues[0].length;j++)
        {
          share[i][j] = ((dataValues[i][j]*100)/total[i]);

          Log.debugLog(className, "getShare", "", "", "share[" + i + "][" + j + "] = " + share[i][j] + ", dataValues = " + dataValues[i][j] + ", total = " + total[i]);

          int decimalPlace = 2;
          // this chk is to avoid the Nan value
          if(total[i] > 0)
          {
            BigDecimal bd = new BigDecimal(share[i][j]);
            bd = bd.setScale(decimalPlace,BigDecimal.ROUND_HALF_UP);
            share[i][j] = bd.doubleValue();
          }
          else
            share[i][j] = 0;
        }
      return share;
    }
    catch(Exception e)
    {
      Log.stackTraceLog(className, "getShare", "", "", "Exception - ", e);
      return null;
    }
  }
  //truncate the String upto 30 characters
  //because upto 34 characters it comes on chart fine
  //2 characters used for ..
  //2 characters used for counter
  public static String stringTruncate(String str)
  {
    if(str.length()>30)
    {
      str = str.substring(0,30);
      str=str+"..";
    }
    return str;
  }

  public static DefaultCategoryDataset getDataSet(double[][] share,String[] strSubCategory, String[] strCategory)
  {
    Log.debugLog(className, "getDataSet", "", "", "Method Called, share.length = " + share.length + ", strSubCategory.length = " + strSubCategory.length + ", strCategory.length = " + strCategory.length);
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();

    for(int i=0;i<strCategory.length-1;i++)
    {
      map = new TreeMap();
      for(int j=0;j<share.length;j++)
      {
        Log.debugLog(className, "getDataSet", "", "", "Adding value to data set share[j][i]=" + share[j][i] + ", strCategory[i+1]=" + strCategory[i+1] + ", strSubCategory[j]=" + strSubCategory[j]);
        //dataset.addValue(new Double(share[j][i]),strCategory[i+1],makeXAxisUnique(stringTruncate(strSubCategory[j])));

        //changing row key nad column key so as to get it in bar chart
        //This change does not have any impact on pie chart: Saloni
        dataset.addValue(new Double(share[j][i]), makeXAxisUnique(stringTruncate(strSubCategory[j])), strCategory[i+1]);
      }

    }
    return dataset;
  }

  public static String[] split(String strList, String strSeparator)
  {
    StringTokenizer st = new StringTokenizer(strList, strSeparator);
    String arrTmp[] = new String[st.countTokens()];
    int i = 0;
    while(st.hasMoreTokens())
      arrTmp[i++] = st.nextToken().trim();
    return(arrTmp);
  }
  //Set the Transparancy of Pie Chart
  public static void setBarTransparancy(float value)
  {
    barTransparancy = value;
  }
  public static void setPieTransparancy(float value)
  {
    pieTransparancy = value;
  }
  //Set the Tooltip
  public static void setToolTip(boolean flag)
  {
    toolTip = flag;
  }

  //Enable/Disable tooltip.
  public static void disableImageLegend(boolean state)
  {
    disableLegend = state;
  }

  //writing tooltip data into a file.
  public static void writetooltipDataIntoFile(boolean state)
  {
    writetooltipDataIntoFile = state;
  }

  /**
  set the backgroung color of chart area
  @params int r -red value of RGB
  @params int g -green value of RGB
  @params int b -blue value of RGB
  */
  public static void setChartAreaBackGroundColor(int r, int g, int b)
  {
    bgcolor = new Color(r,g,b);
  }
  //overloaded
  public static String generateBarChart(String[][] components, int[] colorIndex, String rowOrCol, String ComponentsIndex, String dataValueIndex, String title, String YTitle, String XTitle, String name, HttpSession session, PrintWriter pw, String typeOfChart, int height,int width)
  {
    String filename = null;
    //ChartColor.configPath = Config.pathToConfig()+"chartColor.ini";--------
    CategoryAxis categoryAxis = null;
    org.jfree.chart.axis.ValueAxis valueAxis = null;
    BarRenderer renderer = null;
    if(components.length <=1 )
      return "";
    try
    {
      DefaultCategoryDataset dataset = new DefaultCategoryDataset();
      String arrComponents[][] = components;
      String strSubCategory[] = null;
      double dataValues[][] = null;
      double share[][] = null;
      String compIndex[] = split(ComponentsIndex,"|");
      String dataIndex[] = split(dataValueIndex,"|");
      String strCategory[] = new String[dataIndex.length+compIndex.length];
      int l=0;
      int k=0;
      for(int i=0;i<strCategory.length;i++)
      {
        if(l< compIndex.length)
        {
          strCategory[i]=arrComponents[0][Integer.parseInt(compIndex[l])];
          l++;
        }
        else
        {
          if(k< dataIndex.length)
          {
            strCategory[i]=arrComponents[0][Integer.parseInt(dataIndex[k])];
            k++;
          }
        }
      }
      double[] total = null;
      if(rowOrCol.equals("column"))
      {
        strSubCategory = new String[arrComponents.length - 1];
        dataValues = new double[arrComponents.length - 1][dataIndex.length];
        share=new double[arrComponents.length-1][dataIndex.length];
        total = new double[arrComponents.length-1];

        for(int i=0;i<strSubCategory.length;i++)
        {
          for(int m = 0;m < compIndex.length; m++)
          {
            strSubCategory[i] = arrComponents[i + 1][Integer.parseInt(compIndex[m])];
          }
        }
        for(int j=1; j<arrComponents.length; j++)
        {
          for(int i=0; i<dataIndex.length; i++)
          {
            dataValues[j-1][i] = Double.parseDouble(arrComponents[j][Integer.parseInt(dataIndex[i])]);
            total[j-1] += dataValues[j-1][i];
          }
        }
      }
      share = getShare(dataValues,total);
      //for percentage Share for Stack chart
      if(strCategory.length>2)
        dataset = getDataSet(share,strSubCategory,strCategory);
      else
        dataset = getDataSet(dataValues,strSubCategory,strCategory);

      if(typeOfChart.equals("3d"))
      {
        categoryAxis = new CategoryAxis3D(XTitle);
        valueAxis = new NumberAxis3D(YTitle);
        renderer = new StackedBarRenderer3D();

      }else
      {
        categoryAxis = new CategoryAxis(XTitle);
        valueAxis = new NumberAxis(YTitle);
        renderer = new StackedBarRenderer();

      }

      //format tooltip text for image.
      renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator("URL = {0}, {1} = {2} %",NumberFormat.getInstance()));
      renderer.setBarPainter(new StandardBarPainter());
      org.jfree.chart.plot.CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis, renderer);
      plot.setForegroundAlpha( barTransparancy );
      plot.setBackgroundPaint(bgcolor);

      JFreeChart chart = null;
      //disable chart legend if disableLegend variable is true.
      if(!disableLegend)
	chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
      else
	chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, false);

      //set the maximum Bar Width
      renderer.setMaximumBarWidth(0.2);
      for (int j = 1; j < arrComponents.length; j++)
      {
        plot.getRenderer().setSeriesPaint(j-1, NSColor.lineColor(colorIndex[j]));
      }
      //change the background color of chart area
      chart.setBackgroundPaint(NSColor.ChartBgColor());//change for background
      categoryAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));
      ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
      //filename = ServletUtilities.saveChartAsJPEG(chart,width, height, info, session, name);
      //filename = ServletUtilities.saveChartAsJPEG(chart,width, height, info, session);
      filename = name;
      File file1 = new File(Config.getWorkPath()+"/webapps/netstorm/temp"+"/"+name);
      ChartUtilities.saveChartAsPNG(file1, chart, width, height, info);
      if(toolTip == true)
      {
	if(!writetooltipDataIntoFile)
	{
          ChartUtilities.writeImageMap(pw, filename, info, true);
          pw.flush();
	}
	else
	{
          try
          {
	    String imageMapData = getImageMapForCharts("imageMapData", info);
	    File tooltipFile =  new File(Config.getWorkPath() + "/webapps/netstorm/temp/imageMapData");
	    if(tooltipFile.exists())
	    {
	       tooltipFile.delete();
	       tooltipFile.createNewFile();
	    }
	    FileWriter fw = new FileWriter(tooltipFile);
	    if(imageMapData != null)
	      fw.write(imageMapData);

	    fw.close();
	  }
          catch(Exception e)
          {
             e.printStackTrace();
             Log.stackTraceLog(className, "generateBarChart", "", "", "Exception in getting Image Map data - ", e);
          }
        }
      }
    }
    catch(Exception e)
    {
      //e.printStackTrace();
      //Log.errorLog(className, "generateBarChart Overloaded", "", "", "Exception - " + e);
      Log.stackTraceLog(className, "generateBarChart", "", "", "Exception - ", e);
      filename = "public_error_500x300.png";
    }//end of try
    return filename;
  }

  //Getting ImageMap Data for tooltip on charts.
  /**
   * The Method Taking Charts Information from ChartRenderingInfo object and generate tooltip for each entity.
   * </br>
   *
   * @param name - Name used for Map name.
   * @param info - ChartRenderingInfo object for getting entities.
   * @return ImageMap Data in string format.
   * @author kanchan.
   */
  public static String getImageMapForCharts(String name, ChartRenderingInfo info)
  {

    StringBuffer sb = new StringBuffer();
    sb.append("<map id=\"" + ImageMapUtilities.htmlEscape(name) + "\" name=\"" + ImageMapUtilities.htmlEscape(name) + "\">");
    sb.append(StringUtils.getLineSeparator());

    ToolTipTagFragmentGenerator toolTipTagFragmentGenerator = new StandardToolTipTagFragmentGenerator();
    URLTagFragmentGenerator urlTagFragmentGenerator = new StandardURLTagFragmentGenerator();

    EntityCollection entities = info.getEntityCollection();
    if (entities != null)
    {
      int count = entities.getEntityCount();
      for (int i = count - 1; i >= 0; i--)
      {
	ChartEntity entity = entities.getEntity(i);
	if (entity.getToolTipText() != null || entity.getURLText() != null)
	{
	  String area = entity.getImageMapAreaTag(toolTipTagFragmentGenerator, urlTagFragmentGenerator);
	  if (area.length() > 0)
	  {
	    sb.append(area);
	    sb.append(StringUtils.getLineSeparator());
	  }
	}
      }
    }
    sb.append("</map>");
    return sb.toString();
  }

  /**
   * This Method Creates DataSet for Stacked Bar Chart.
   * @param timeSeries
   * @param trStartTime
   * @param elapsedTime
   * @return
   */
  private CategoryDataset createStackedBarDataSet(TimeSeriesCollection stackedTimeSeriesCollection, long trStartTime, boolean elapsedTime, TimeZone timeZone)
  {
    try
    {

      Log.debugLog(className, "createStackedBarDataSet", "", "", "Method Called.");

      final DefaultCategoryDataset defaultcategorydataset = new DefaultCategoryDataset();

      for(int k = 1; k < stackedTimeSeriesCollection.getSeriesCount(); k++)
      {
        TimeSeries stackedTimeSeries = stackedTimeSeriesCollection.getSeries(k);
        for (int i = 0; i < stackedTimeSeries.getItemCount(); i++)
        {
          TimeSeriesDataItem dt = stackedTimeSeries.getDataItem(i);
          double sampleValue = Double.parseDouble(dt.getValue().toString());

          if(elapsedTime)
          {
            String category = rptUtilsBean.convMilliSecToStr(dt.getPeriod().getFirstMillisecond() - trStartTime);
            defaultcategorydataset.addValue(sampleValue, stackedTimeSeries.getKey(), category);
          }
          else
          {
            long time = dt.getPeriod().getFirstMillisecond();
            //String category = getAbsoluteTime(time);

            String dateFormatted = getAbsoluteTimeWithTimeZone(time, timeZone);
            defaultcategorydataset.addValue(sampleValue, stackedTimeSeries.getKey(), dateFormatted);
          }
        }
      }
      return defaultcategorydataset;
    }
    catch(Exception ex)
    {
      Log.errorLog(className, "createStackedBarDataSet", "", "", "Exception - " + ex);
      ex.printStackTrace();
      return null;
    }
  }

  /**
   * Method Used to Generate Stacked Bar Chart with Line Chart.
   * @param currGraphIndexes
   * @param hmTimeSeries
   * @param timeFormat
   * @param title
   * @param graphNames
   * @param imageURL
   * @param trStartTime
   * @param elapsedTime
   * @param timeSeriesColor
   * @param barColor
   * @return
   */
  public JFreeChart genLineStackedBarChart(GraphUniqueKeyDTO[] currGraphUniqueKeyDTOs, TimeSeriesCollection stackedTimeSeriesCollection, String timeFormat,String title, GraphNames graphNames, URL imageURL, long trStartTime, boolean elapsedTime, ArrayList<Paint> stackedBarColor, TimeZone timeZone, String userType)
  {
    try
    {
      Log.debugLog(className, "genLineStackedBarChart", "", "", "Method Called.");

      //Get TimeSeries for
      TimeSeries timeseries1 = null;
      if (stackedTimeSeriesCollection.getSeriesCount() > 0)
        timeseries1 = stackedTimeSeriesCollection.getSeries(0);

      //Change Title based on timeFormat.
      if(!elapsedTime)
        timeFormat = "Time: Absolute";
      else
        timeFormat = "Time: " + timeFormat;

      //Getting Data Set For Line Chart for First Selected Graph.
      CategoryDataset categoryLineChartDataSet = createBarDataset(timeseries1, trStartTime, elapsedTime, timeZone);

      CategoryDataset categoryStackedDataSet = createStackedBarDataSet(stackedTimeSeriesCollection, trStartTime, elapsedTime, timeZone);
      JFreeChart chart = ChartFactory.createStackedBarChart(title, timeFormat, "", categoryStackedDataSet, PlotOrientation.VERTICAL, false, true, false);

      if(chart == null)
      {
        Log.errorLog(className, "genLineStackedBarChart", "", "", "chart = null.");
        return null;
      }

      final CategoryPlot plot = chart.getCategoryPlot();
      if(Config.getValue("imageFlag").equals("on"))
      {
        Image im = new ImageIcon(imageURL).getImage();
        plot.setBackgroundImage(im);
      }

      Paint timeSeriesColor = stackedBarColor.get(0);

      plot.setDataset(1, categoryLineChartDataSet);
      plot.setBackgroundPaint(Color.white);
      plot.setDomainGridlinePaint(NSColor.graphHorizontalinecolor());
      plot.setRangeGridlinePaint(NSColor.graphHorizontalinecolor());
      plot.getDomainAxis().setLabelFont(NSColor.mediumMicrosoftSansSerifPlainFont());
      plot.getRangeAxis().setLabelFont(NSColor.mediumMicrosoftSansSerifPlainFont());
      plot.getRangeAxis().setAxisLineVisible(false);
      plot.getDomainAxis().setAxisLineVisible(false);
      plot.getDomainAxis().setTickLabelFont(NSColor.mediumMicrosoftSansSerifPlainFont());
      plot.getRangeAxis().setTickLabelFont(NSColor.mediumMicrosoftSansSerifPlainFont());

      //Renderer for Group Stacked Bar Chart.
      GroupedStackedBarRenderer stackedRenderer = new GroupedStackedBarRenderer();

      //Getting all TimeSeries Colors.
      for(int k = 1; k < stackedBarColor.size(); k++)
      {
	stackedRenderer.setSeriesPaint(k-1, stackedBarColor.get(k));
      }

      CategoryAxis categoryaxis = plot.getDomainAxis();
      categoryaxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
      categoryaxis.setAxisLineVisible(false);
      categoryaxis.setTickLabelFont(NSColor.mediumMicrosoftSansSerifPlainFont());
      categoryaxis.setLabelFont(NSColor.mediumMicrosoftSansSerifPlainFont());

      LineAndShapeRenderer lineandshaperenderer = new LineAndShapeRenderer();
      lineandshaperenderer.setSeriesStroke(0, new BasicStroke(1.0f));
      lineandshaperenderer.setSeriesShape(0, new Ellipse2D.Double(-2.0, -2.0, 4.0, 4.0));
      if(userType.equalsIgnoreCase("Business"))
        lineandshaperenderer.setSeriesShapesVisible(0, false);
      lineandshaperenderer.setSeriesPaint(0, timeSeriesColor);
      lineandshaperenderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
      plot.setRenderer(1, lineandshaperenderer);
      plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

      stackedRenderer.setDrawBarOutline(true);
      plot.getDomainAxis().setLabelFont(NSColor.mediumMicrosoftSansSerifPlainFont());
      plot.getDomainAxis().setTickLabelFont(NSColor.mediumMicrosoftSansSerifPlainFont());
      //plot.setDomainGridlinesVisible(true);
      plot.setRenderer(stackedRenderer);
      return chart;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "genLineStackedBarChart", "", "", "Exception - " , ex);
      ex.printStackTrace();
      return null;
    }
  }

//to create line bar chart
  public JFreeChart genLineBarChart(GraphUniqueKeyDTO[] currGraphUniqueKeyDTO, HashMap<GraphUniqueKeyDTO, TimeSeries> hmTimeSeries, String timeFormat,String title, GraphNames graphNames, URL imageURL, long trStartTime, boolean elapsedTime, Paint timeSeriesColor, Paint barColor, String userType, TimeZone timeZone, String[] graphCaptions)
  {
    try
    {
      Log.debugLog(className, "genLineBarChart", "", "", "Method Called.");
      // timeseries for first graph
      TimeSeries timeseries1 = null;
      if (hmTimeSeries.containsKey(currGraphUniqueKeyDTO[0]))
        timeseries1 = hmTimeSeries.get(currGraphUniqueKeyDTO[0]);

      // timeseries for second graph
      TimeSeries timeSeries2 = null;
      if (hmTimeSeries.containsKey(currGraphUniqueKeyDTO[1]))
        timeSeries2 = hmTimeSeries.get(currGraphUniqueKeyDTO[1]);

      if(timeseries1 == null || timeSeries2 == null)
        Log.errorLog(className, "genLineBarChart", "", "", "timeseries1 = " + timeseries1 + ", timeSeries2 = " + timeSeries2);

      if(!elapsedTime)
        timeFormat = "Time: Absolute";
      else
        timeFormat = "Time: " + timeFormat;
      
      CategoryDataset  categorydataset1 = createBarDataset(timeseries1, trStartTime, elapsedTime, timeZone);
      CategoryDataset categorydataset2 = createBarDataset(timeSeries2, trStartTime, elapsedTime, timeZone);
      JFreeChart chart = ChartFactory.createBarChart(title, timeFormat, graphCaptions[0], categorydataset1, PlotOrientation.VERTICAL, false, true, false);

      if(chart == null)
      {
        Log.errorLog(className, "genLineBarChart", "", "", "chart = null.");
        return null;
      }

      final CategoryPlot plot = chart.getCategoryPlot();
      if(Config.getValue("imageFlag").equals("on"))
      {
        Image im = new ImageIcon(imageURL).getImage();
        plot.setBackgroundImage(im);
      }

      plot.setDataset(1, categorydataset2);
      plot.setBackgroundPaint(Color.white);
      plot.setDomainGridlinePaint(NSColor.graphHorizontalinecolor());
      plot.setRangeGridlinePaint(NSColor.graphHorizontalinecolor());
      plot.getDomainAxis().setLabelFont(NSColor.mediumMicrosoftSansSerifPlainFont());
      plot.getRangeAxis().setLabelFont(NSColor.mediumMicrosoftSansSerifPlainFont());
      plot.getRangeAxis().setAxisLineVisible(false);
      plot.getDomainAxis().setAxisLineVisible(false);
      plot.getDomainAxis().setTickLabelFont(NSColor.mediumMicrosoftSansSerifPlainFont());
      plot.getRangeAxis().setTickLabelFont(NSColor.mediumMicrosoftSansSerifPlainFont());
      plot.mapDatasetToRangeAxis(1, 1);

      CategoryAxis categoryaxis = plot.getDomainAxis();
      categoryaxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
      categoryaxis.setAxisLineVisible(false);
      categoryaxis.setTickLabelFont(NSColor.mediumMicrosoftSansSerifPlainFont());
      categoryaxis.setLabelFont(NSColor.mediumMicrosoftSansSerifPlainFont());
      
      
      NumberAxis numberaxis = new NumberAxis(graphCaptions[1]);
      numberaxis.setAxisLineVisible(false);
      numberaxis.setLabelFont(NSColor.mediumMicrosoftSansSerifPlainFont());
      numberaxis.setTickLabelFont(NSColor.mediumMicrosoftSansSerifPlainFont());
      numberaxis.setLabelPaint(timeSeriesColor);
      plot.setRangeAxis(1, numberaxis);

      LineAndShapeRenderer lineandshaperenderer = new LineAndShapeRenderer();
      lineandshaperenderer.setSeriesStroke(0, new BasicStroke(1.0f));
      lineandshaperenderer.setSeriesShape(0, new Ellipse2D.Double(-2.0, -2.0, 4.0, 4.0));
      if(userType.equalsIgnoreCase("Business"))
        lineandshaperenderer.setSeriesShapesVisible(0, false);
      lineandshaperenderer.setSeriesPaint(0, timeSeriesColor);
      lineandshaperenderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
      plot.setRenderer(1, lineandshaperenderer);
      plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
      if(barColor == null)
      {
	int indexOfGraphUniqueKeyDTO = graphNames.getIndexOfGraphUniqueKeyDTO(currGraphUniqueKeyDTO[0]);
        barColor = createPaint(indexOfGraphUniqueKeyDTO)[0];
      }

      Paint apaint[] = new Paint[]{barColor};
      CustomBarRenderer custombarrenderer = new CustomBarRenderer(apaint);
      custombarrenderer.setDrawBarOutline(true);
      plot.getDomainAxis().setLabelFont(NSColor.mediumMicrosoftSansSerifPlainFont());
      plot.getRangeAxis().setLabelFont(NSColor.mediumMicrosoftSansSerifPlainFont());
      plot.getRangeAxis().setTickLabelFont(NSColor.mediumMicrosoftSansSerifPlainFont());
      plot.getDomainAxis().setTickLabelFont(NSColor.mediumMicrosoftSansSerifPlainFont());
      plot.getRangeAxis().setLabelPaint(barColor);
      custombarrenderer.setBarPainter(new StandardBarPainter());
      custombarrenderer.setShadowVisible(false);
      plot.setRenderer(custombarrenderer);
      return chart;
    }
    catch(Exception ex)
    {
      Log.stackTraceLog(className, "genLineBarChart", "", "", "Exception - " , ex);
      return null;
    }
  }

  static class CustomBarRenderer extends BarRenderer
  {

    private Paint colors[];

    public Paint getItemPaint(int i, int j)
    {
      return colors[j % colors.length];
    }

    public CustomBarRenderer(Paint apaint[])
    {
      colors = apaint;
    }
  }

  // method for bar color
  private static Paint[] createPaint(int currGraphIndexes)
  {
    Color[] barColors = new Color[]{
        new Color(64,105,157),  new Color(159,65,62),  new Color(127,155,72), new Color(248,151,70),  new Color(105,81,134),
        new Color(171,187,216), new Color(205,123,56), new Color(0,153,204),new Color(204,153,0), new Color(128,0,0),
        new Color(121,91,51), new Color(81,35,115),new Color(21,255,255) , new Color(136,206,66)
        };
    Paint apaint[] = new Paint[1];
    int index = currGraphIndexes % (barColors.length);
    Color barColor = barColors[index];
    apaint[0] = new GradientPaint(0.0F, 0.0F, barColor, 0.0F, 0.0F, barColor);
    return apaint;
  }

  //create bar chart dataset
  private CategoryDataset createBarDataset(TimeSeries timeSeries, long trStartTime, boolean elapsedTime, TimeZone timeZone)
  {
    try
    {
      final DefaultCategoryDataset defaultcategorydataset = new DefaultCategoryDataset();
      int totalElement = timeSeries.getItemCount();
      for (int i = 0; i < totalElement; i++)
      {
        //TimeSeriesDataItem dt = timeSeries.getDataPair(i);
        TimeSeriesDataItem dt = timeSeries.getDataItem(i);
        double sampleValue = Double.parseDouble(dt.getValue().toString());
        // add datavalue to series
        //String category = createElapsedTimeForBar(i);
        if(elapsedTime)
        {
          String category = rptUtilsBean.convMilliSecToStr(dt.getPeriod().getFirstMillisecond() - trStartTime);
          //defaultcategorydataset.addValue(sampleValue, timeSeries.getName(), category);
          defaultcategorydataset.addValue(sampleValue, timeSeries.getKey(), category);
        }
        else
        {
          long time = dt.getPeriod().getFirstMillisecond();
          //String category = getAbsoluteTime(time);
          //String category = rptUtilsBean.convMilliSecToStr(time);
          //defaultcategorydataset.addValue(sampleValue, timeSeries.getName(), category);

          String dateFormatted = getAbsoluteTimeWithTimeZone(time, timeZone);
          defaultcategorydataset.addValue(sampleValue, timeSeries.getKey(), dateFormatted);
        }
      }
      return defaultcategorydataset;
    }
    catch(Exception ex)
    {
      Log.errorLog(className, "createBarDataset", "", "", "Exception - " + ex);
      return null;
    }
  }

  /**
   * Method For Getting Time in HH:mm:ss format based on timezone.
   * @param timeStamp
   * @param timeZone
   * @return
   */

  public String getAbsoluteTimeWithTimeZone(long timeStamp, TimeZone timeZone)
  {
    try
    {
       Date date = new Date(timeStamp);
       DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
       formatter.setTimeZone(timeZone);
       String dateFormatted = formatter.format(date);
       return dateFormatted;
    }
    catch(Exception e)
    {
      Log.errorLog(className, "getAbsoluteTimeWithTimeZone", "", "", "Exception - " + e);
      return "";
    }
  }

  public String getAbsoluteTime(long time)
  {
    Date tempDate = new Date(time);
    Calendar myCal = Calendar.getInstance();
    int hours = tempDate.getHours();
    int minutes = tempDate.getMinutes();
    int seconds = tempDate.getSeconds();
    String h1 = "" , m1 = "", s1 = "";

    if(hours > 9)
      h1 = "" + hours;
    else
      h1 = "0" + hours;

    // minute
    if(minutes > 9)
      m1 = "" + minutes;
    else
      m1 = "0" + minutes;

    // second
    if(seconds > 9)
      s1 = "" + seconds;
    else
      s1 = "0" + seconds;

    return h1 + ":" + m1 + ":" + s1;
  }

  public static String generatePieChart(String[][] components, int[] colorIndex, String rowOrCol, String ComponentsIndex, String dataValueIndex, String title, String name, HttpSession session, PrintWriter pw, String typeOfChart, int height, int width)
  {
    String filename = null;
    org.jfree.chart.plot.PiePlot3D plot3d=null;
    //ChartColor.configPath = Config.pathToConfig()+"chartColor.ini";---------------
    org.jfree.chart.plot.PiePlot plot=null;
    JFreeChart chart =null;
    String arrDataValues[][]=components;
    if(components == null)
      return "nodata.jpg";
    try
    {
      DefaultPieDataset data = new DefaultPieDataset();
      double total = 0.0;
      //  Throw a custom NoDataException if there is no data
      // Neeraj - Commented this code as this need to check size based
      //   on row or column. Need to take care later
/**
      if (components[Integer.parseInt(dataValueIndex)].length == 0) {
        throw new NoDataException();
      }
**/
      if(rowOrCol.equals("row"))
      {
        int size =  arrDataValues[Integer.parseInt(dataValueIndex)].length;
        double share[] =new double[size];

        for(int z = 0; z < size; z++)
        {
          total += Double.parseDouble(arrDataValues[Integer.parseInt(dataValueIndex)][z]);
        }
        double tempValue;
        for(int n = 0; n < size; n++)
        {
          tempValue = Double.parseDouble(arrDataValues[Integer.parseInt(dataValueIndex)][n]);
          share[n]=((tempValue*100)/total);
          int decimalPlace = 2;
          BigDecimal bd = new BigDecimal(share[n]);
          bd = bd.setScale(decimalPlace,BigDecimal.ROUND_HALF_UP);
          share[n] = bd.doubleValue();
        }//end of for loop

        for(int i = 0; i < size; i++)
        {
          data.setValue(arrDataValues[Integer.parseInt(ComponentsIndex)][i],share[i]);
        }//end of For loop
      }//end of If
      else
      {
        int size =  arrDataValues.length;
        double share[] =new double[size];
        for(int z = 1; z < size; z++)
        {
          total += Double.parseDouble(arrDataValues[z][Integer.parseInt(dataValueIndex)]);
        }
        double tempValue;
        for(int n = 1; n < size; n++)
        {
          tempValue=Double.parseDouble(arrDataValues[n][Integer.parseInt(dataValueIndex)]);
          share[n]=((tempValue*100)/total);
          int decimalPlace = 2;
          BigDecimal bd = new BigDecimal(share[n]);
          bd = bd.setScale(decimalPlace,BigDecimal.ROUND_HALF_UP);
          share[n] = bd.doubleValue();
        }//end of for loop
        map = new TreeMap();
        for(int i = 1; i < size; i++)
        {
          data.setValue(makeXAxisUnique(arrDataValues[i][Integer.parseInt(ComponentsIndex)]),share[i]);
        }//end of For loop
      }//end of If


      if(typeOfChart.equals("3d"))
      {
        plot3d = new PiePlot3D(data);
        //change the background color of chart area
        plot3d.setBackgroundPaint(bgcolor);
        //plot3d.setInsets(new Insets(0, 5, 5, 5));---------------------
        //plot3d.setInsets(new RectangleInsets(null, 0, 5, 5, 5));
        for(int ii = 1; ii < arrDataValues.length; ii++)
          plot.setSectionPaint(ii-1, NSColor.lineColor(colorIndex[ii]));

        //  plot3d.setExplodePercent( 3, 0.25 );
        //used for transparancy
        plot3d.setForegroundAlpha( pieTransparancy );
        /*plot3d.setToolTipGenerator(new StandardPieItemLabelGenerator());--------------
        //Set % in label of Pie chart
        plot3d.setLabelGenerator(new StandardPieItemLabelGenerator("{0} = {1}%"));*/
        plot3d.setToolTipGenerator(new StandardPieToolTipGenerator());
        //Set % in label of Pie chart
        plot3d.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} = {1}%"));
        chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot3d, true);
      }
      else
      {
        plot = new PiePlot(data);
        //plot.setInsets(new Insets(0, 5, 5, 5));--------------
        for(int ii = 1; ii < arrDataValues.length; ii++)
          plot.setSectionPaint(ii-1, NSColor.lineColor(colorIndex[ii]));

        //change the background color of chart area
        plot.setBackgroundPaint(bgcolor);
        //used for transparancy
        plot.setForegroundAlpha( pieTransparancy );
        /*plot.setToolTipGenerator(new StandardPieItemLabelGenerator());-----------------
        //Set % in label of Pie chart
        plot.setLabelGenerator(new StandardPieItemLabelGenerator("{0} = {1}%"));*/
        plot.setToolTipGenerator(new StandardPieToolTipGenerator());
        //Set % in label of Pie chart
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} = {1}%"));
        chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
      }

      chart.setBackgroundPaint(NSColor.ChartBgColor());//change for background
      ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
      //filename = ServletUtilities.saveChartAsJPEG(chart, width, height, info, session, name);-----
      //filename = ServletUtilities.saveChartAsJPEG(chart, width, height, info, session);
      filename = name;
      File file1 = new File(Config.getWorkPath()+"/webapps/netstorm/temp"+"/"+name);
      ChartUtilities.saveChartAsPNG(file1, chart, width, height, info);
      if(toolTip == true)
      {
        ChartUtilities.writeImageMap(pw, filename, info, true);
        pw.flush();
      }
    }
/** Neeraj
    catch (NoDataException e) {
      Log.errorLog(className, "generatePieChart", "", "", "NoDataException - " + e);
      filename = "public_nodata_500x300.png";
    }
**/
    catch (Exception e) {
      //Log.errorLog(className, "generatePieChart", "", "", "Exception - " + e);
      Log.stackTraceLog(className, "generatePieChart", "", "", "Exception - ", e);
      //e.printStackTrace(System.out);
      filename = "public_error_500x300.png";
    }
    return filename;
  }

  

  /**
   * Returns a sample dataset.
   *
   * @return The dataset.
   */
  //private CategoryDataset createDataset(ArrayList arrBarDataSet)
  private CategoryDataset createDataset(ArrayList arrBarDataSet)
  {
    //String barDataSet[] = new String[arrBarDataSet.size()];
    String barDataSet[] = new String[arrBarDataSet.size()];
    final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    for(int k = 0; k < arrBarDataSet.size(); k++)
      barDataSet[k] = arrBarDataSet.get(k).toString();//.substring(arrBarDataSet.get(k).toString().indexOf("{") + 1, arrBarDataSet.get(k).toString().indexOf("}"));
    for(int i = 0; i < barDataSet.length; i++)
    {
      String arrTemp[] = barDataSet[i].split(",");
      dataset.addValue(Double.parseDouble(arrTemp[2]), arrTemp[1], arrTemp[0]);
    }
    return dataset;

  }

 

  /**
   * Creates a sample chart.
   *
   * @param dataset  the dataset.
   *
   * @return The chart.
   */
  private JFreeChart createChart(final CategoryDataset dataset, String title)
  {
    // create the chart...
    final JFreeChart chart = ChartFactory.createBarChart3D(title, // chart title
        "", // domain axis label
        "", // range axis label
        dataset, // data
        PlotOrientation.VERTICAL, // orientation
        true, // include legend
        true, // tooltips?
        false // URLs?
        );

    // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

    // set the background color for the chart...
    chart.setBackgroundPaint(Color.white);
    // get a reference to the plot for further customisation...
    final CategoryPlot plot = chart.getCategoryPlot();
    plot.setBackgroundPaint(Color.white);
    plot.setDomainGridlinePaint(Color.BLACK);
    plot.setRangeGridlinePaint(Color.BLACK);
    plot.setForegroundAlpha(1.0f);

    // set the range axis to display integers only...
    final NumberAxis rangeAxis = (NumberAxis)plot.getRangeAxis();
    rangeAxis.setAutoRangeIncludesZero(false);
    //rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

    // disable bar outlines...
    final BarRenderer renderer = (BarRenderer)plot.getRenderer();
    renderer.setDrawBarOutline(false);
    renderer.setMaximumBarWidth(0.05);
    renderer.setItemMargin(0.0);
    renderer.setSeriesItemLabelsVisible(0, Boolean.TRUE);

    Font ft = new Font("Vardana", Font.BOLD, 10);
    renderer.setBaseItemLabelFont(ft);
    renderer.setBaseItemLabelPaint((Paint)Color.black);

    Paint[] barColors = new Paint[]{new Color(64, 105, 157), new Color(159, 65, 62), new Color(127, 155, 72), new Color(248, 151, 70), new Color(105, 81, 134), new Color(171, 187, 216), new Color(205, 123, 56), new Color(0, 153, 204), new Color(204, 153, 0), new Color(128, 0, 0), new Color(121, 91, 51), new Color(81, 35, 115), new Color(21, 255, 255), new Color(136, 206, 66)};
    // set up gradient paints for series...
    for(int i = 0; i < barColors.length; i++)
    {
      renderer.setSeriesPaint(i, barColors[i]);

    }
    final ItemLabelPosition p = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.TOP_LEFT, TextAnchor.TOP_LEFT, -Math.PI / 2.0);
    renderer.setPositiveItemLabelPosition(p);
    chart.getCategoryPlot().setRenderer(renderer);
    final CategoryAxis domainAxis = plot.getDomainAxis();
    domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));
    // Here we save image
    ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
    // OPTIONAL CUSTOMISATION COMPLETED.

    return chart;

  }

  /*
   *
  * This method is to create stacked bar chart : Saloni Tyagi
  * @param components
  * @param colorIndex
  * @param rowOrCol
  * @param ComponentsIndex
  * @param dataValueIndex
  * @param title
  * @param YTitle
  * @param XTitle
  * @param name
  * @param session
  * @param pw
  * @param typeOfChart
  * @param height
  * @param width
  * @return
  */
  public static String generateStackedBarChart(String[][] components, int[] colorIndex, String rowOrCol, String ComponentsIndex, String dataValueIndex, String title, String YTitle, String XTitle, String name, HttpSession session, PrintWriter pw, String typeOfChart, int height,int width)
  {
    Log.debugLog(className, "generateStackedBarChart", "", "", "rowOrCol = " + rowOrCol + ", ComponentsIndex = " + ComponentsIndex + ", dataValueIndex = " + dataValueIndex + ", title = " + title + ", name = " + name + ", typeOfChart = " + typeOfChart);
    String filename = null;
    //ChartColor.configPath = Config.pathToConfig()+"chartColor.ini";----------
    CategoryAxis categoryAxis = null;
    org.jfree.chart.axis.ValueAxis valueAxis = null;
    //StackedBarRenderer renderer = null;
    if(components.length <=1 )
      return "";
    try
    {
      DefaultCategoryDataset dataset = new DefaultCategoryDataset();

      String arrComponents[][] = components;
      String strSubCategory[] = null;
      double dataValues[][] = null;
      double share[][] = null;
      String compIndex[] = split(ComponentsIndex,"|");
      String dataIndex[] = split(dataValueIndex,"|");
      String strCategory[] = new String[dataIndex.length+compIndex.length];
      int l=0;
      int k=0;

      //getting data values from 2d array taken from table
      for(int i=0;i<strCategory.length;i++)
      {
        Log.debugLog(className, "generateStackedBarChart", "", "", "Inside for loop");
        if(l< compIndex.length)
        {
          strCategory[i]=arrComponents[0][Integer.parseInt(compIndex[l])];
          Log.debugLog(className, "generateStackedBarChart", "", "", "strCategory[i]=" + strCategory[i]);
          l++;
        }
        else
        {
          if(k< dataIndex.length)
          {
            strCategory[i]=arrComponents[0][Integer.parseInt(dataIndex[k])];
            Log.debugLog(className, "generateStackedBarChart", "", "", "@@@strCategory[i]=" + strCategory[i]);
            k++;
          }
        }

      }
      double[] total = null;
      if(rowOrCol.equals("column"))
      {
        Log.debugLog(className, "generateStackedBarChart", "", "", "Initializing data structure.");
        strSubCategory = new String[arrComponents.length - 1];
        dataValues = new double[arrComponents.length - 1][dataIndex.length];
        share=new double[arrComponents.length-1][dataIndex.length];
        total = new double[arrComponents.length-1];

        for(int i=0;i<strSubCategory.length;i++)
        {
          for(int m = 0;m < compIndex.length; m++)
          {
            strSubCategory[i] = arrComponents[i + 1][Integer.parseInt(compIndex[m])];
            Log.debugLog(className, "generateStackedBarChart", "", "", "strSubCategory[i]=" + strSubCategory[i] + ", compIndex[m]=" + compIndex[m]);
          }
        }
        for(int j=1; j<arrComponents.length; j++)
        {
          for(int i=0; i<dataIndex.length; i++)
          {
            dataValues[j-1][i] = Double.parseDouble(arrComponents[j][Integer.parseInt(dataIndex[i])]);
            total[j-1] += dataValues[j-1][i];
            Log.debugLog(className, "generateStackedBarChart", "", "", "dataValues[j-1][i]=" + dataValues[j-1][i] + ", dataIndex[i]=" + dataIndex[i] + ", total[j-1]=" + total[j-1]);
          }
        }
      }
      share = getShare(dataValues,total);
      //for percentage Share for Stack chart
      if(strCategory.length>2)
        dataset = getDataSet(share,strSubCategory,strCategory);
      else
        dataset = getDataSet(dataValues,strSubCategory,strCategory);

      Log.debugLog(className, "generateStackedBarChart", "", "", "typeOfChart=" + typeOfChart);
      /*if(typeOfChart.equals("3d"))
      {
        categoryAxis = new CategoryAxis3D(XTitle);
        valueAxis = new NumberAxis3D(YTitle);
        renderer = new StackedBarRenderer3D();

      }else*/
      {
        categoryAxis = new CategoryAxis(XTitle);
        valueAxis = new NumberAxis(YTitle);
        //renderer = new StackedBarRenderer();

      }
      //renderer.setToolTipGenerator(new StandardCategoryToolTipGenerator());
      /*org.jfree.chart.plot.CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis, renderer);
      plot.setForegroundAlpha( barTransparancy );
      plot.setBackgroundPaint(bgcolor);*/

      //JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
      JFreeChart chart = ChartFactory.createStackedBarChart(
          "Time Split Chart",  // chart title
          "",                  // domain axis label
          "Seconds",                     // range axis label
          dataset,                     // data
          PlotOrientation.HORIZONTAL,  // the plot orientation
          true,                        // legend
          true,                        // tooltips
          false                        // urls
      );
      CategoryPlot plot = (CategoryPlot) chart.getPlot();
     // final PiePlot3D plot = (PiePlot3D) chart.getPlot();
      plot.setBackgroundPaint(bgcolor);
      plot.setForegroundAlpha( barTransparancy );
      for (int j = 1; j < arrComponents.length; j++)
      {
        plot.getRenderer().setSeriesPaint(j-1, NSColor.lineColor(colorIndex[j]));
      }
      //plot.setRangeGridlinePaint(Color.white);

      StackedBarRenderer renderer = (StackedBarRenderer) plot.getRenderer();
      renderer.setBarPainter(new StandardBarPainter());
      renderer.setDrawBarOutline(false);
      //renderer.setMaxBarWidth(0.33);

      //set the maximum Bar Width to approx one third of the chart
      renderer.setMaximumBarWidth(0.30);

      //change the background color of chart area
      chart.setBackgroundPaint(NSColor.ChartBgColor());//change for background
      //categoryAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));
      ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
      //filename = ServletUtilities.saveChartAsJPEG(chart,width, height, info, session, name);
      //filename = ServletUtilities.saveChartAsJPEG(chart,width, height, info, session);
      filename = name;
      File file1 = new File(Config.getWorkPath()+"/webapps/netstorm/temp"+"/"+name);
      ChartUtilities.saveChartAsPNG(file1, chart, width, height, info);
      if(toolTip == true)
      {
        ChartUtilities.writeImageMap(pw, filename, info, true);
        pw.flush();
      }
    }
    catch(Exception e)
    {
      //e.printStackTrace();
      //Log.errorLog(className, "generateBarChart Overloaded", "", "", "Exception - " + e);
      Log.stackTraceLog(className, "generateBarChart", "", "", "Exception - ", e);
      filename = "public_error_500x300.png";
    }//end of try
    return filename;
  }

  //This function is used to make Unique value on X axis
  //@param xAxis value of x Axis
  public static String makeXAxisUnique(String xAxis)
  {

   Iterator it = map.keySet().iterator();
   Object obj;
   boolean duplicate = false;
   String count = "0";
   while (it.hasNext())
   {
     obj = it.next();
     String temp= ""+obj;
     if(isIgnoreCase && temp.equalsIgnoreCase(xAxis))
     {
       duplicate = true;
       break;
     }
     else if(temp.equals(xAxis))
     {
       duplicate = true;
       break;
     }
   }

   if(duplicate == true)
   {
     count = map.get(xAxis).toString();
     map.remove(new String(xAxis));
     map.put(new String(xAxis),(Integer.parseInt(count)+1)+"");
     xAxis = xAxis+map.get(xAxis);
   }
   else
   {
     map.put(new String(xAxis),count);
   }
   return xAxis;

  }

  /**
   * This method is to create 3D pie chart by sangeeta sahu
   * @param arrDataValues- list of names of method class or package
   * @param arrCumulativeTime - respective cumulative time of method class or package
   * @param testRun
   * @param title
   * @param imageName
   * return fuction will return the list of color used in pie chart.
   */
  public Vector generate3DPieChart(String arrDataValues[], String arrCumulativeTime[], String testRun, String title, String imageName, int width, int height)
  {
    Log.debugLog(className, "generatePieChart", "", "", "Method called");

    Vector vecColorList = new Vector();
    PieDataset dataset = null;
    final ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
    JFreeChart chart = null;

    dataset = createSampleDataset(arrDataValues, arrCumulativeTime);
    chart = createChart(dataset, arrDataValues, vecColorList, title);
    chart.getLegend().visible = false;
    chart.setTitle(new org.jfree.chart.title.TextTitle(title, new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12)));
    chart.setBackgroundPaint(new Color(232,239,250));

    try
    {
      final File file1 = new File(Config.getWorkPath()+"/webapps/netstorm/temp"+"/"+imageName);
      //ChartUtilities.saveChartAsPNG(file1, chart, 400, 300, info);
      //ChartUtilities.saveChartAsPNG(file1, chart, 800, 700, info);
      ChartUtilities.saveChartAsPNG(file1, chart, width, height, info);
    }
    catch (Exception e)
    {
      Log.errorLog(className, "getChartDetails", "", "", "Exception - " + e);
      return null;
    }

    return vecColorList;
  }

  private PieDataset createSampleDataset(String arrDataValues[], String arrCumulativeTime[])
  {
    final DefaultPieDataset result = new DefaultPieDataset();
    Vector tempVecToRemoveDuplicacy = new Vector();

    double totalCumulativeTime = 0.0;
    for(int i = 0; i < arrCumulativeTime.length; i++)
      totalCumulativeTime += Double.parseDouble(arrCumulativeTime[i]);

    if(totalCumulativeTime == 0.0)
      totalCumulativeTime = 0.0001; //this is to handle divide by zero case.

    double percentageValue = 0;
    double sumOfTenRecord = 0;
    for(int i = 0; i < arrDataValues.length; i++)
    {
      percentageValue = getPercenatgeValue(Double.parseDouble(arrCumulativeTime[i]), totalCumulativeTime);

      if(tempVecToRemoveDuplicacy.contains(arrDataValues[i]))
        arrDataValues[i] = arrDataValues[i] + " ";

      tempVecToRemoveDuplicacy.add(arrDataValues[i]);
      result.setValue(arrDataValues[i] + " = " + rptUtilsBean.convTo3DigitDecimal(percentageValue)+"%", percentageValue);
      sumOfTenRecord += Double.parseDouble(arrCumulativeTime[i]);

      if(i == 9 && arrDataValues.length > 10)
      {
        percentageValue = getPercenatgeValue((totalCumulativeTime - sumOfTenRecord), totalCumulativeTime);
        result.setValue("other" + " = " + rptUtilsBean.convTo3DigitDecimal(percentageValue)+"%", percentageValue);
        break;
      }
    }
    return result;
  }

  private double getPercenatgeValue(double value, double totalValue)
  {
    double percentage = 0d;
  //  if(value == 0.0)
    //  percentage = (((value + 0.0001)*100)/totalValue);
   // else
      percentage = (value*100)/totalValue;
    return(percentage);
  }

  private JFreeChart createChart(final PieDataset dataset, String arrDataValues[], Vector vecColoList, String title)
  {
    final JFreeChart chart = ChartFactory.createPieChart(
        title,  // chart title
        dataset,                // data
        true,                   // include legend
        true,
        false
    );

    PiePlot plot = (PiePlot) chart.getPlot();
    plot.setStartAngle(290);
    //plot.setLabelGenerator(new StandardPieItemLabelGenerator("{0} = {2}", NumberFormat.getNumberInstance(), NumberFormat.getPercentInstance()));
    //plot.setLabelGenerator(null);
    plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}"));
    plot.setDirection(Rotation.CLOCKWISE);
    plot.setForegroundAlpha(pieTransparancy);
    plot.setNoDataMessage("No data to display");
    //plot.setLabelPaint(Color.blue);
    plot.setLabelFont(new Font("sansserif",Font.ITALIC,12));
    plot.setBackgroundPaint(Color.white);
    vecColoList.clear();

    //here i < 11 because we are showing maximun 10 items on the pie chart.
    int j = 0;
    for(int i = 0; i < arrDataValues.length; i++)
    {
      j = i%10;
      if(i < 10)
        plot.setSectionPaint(i, NSColor.lineColor(j+1));
      vecColoList.add(colorListForJspTable[j+1]);
      j++;
    }
    //plot.setLabelGenerator(new CustomLabelGenerator());
    return chart;
  }

  public String getMethodNameWithoutSignature(String methodSignature)
  {
    int lastIndexOfDot = methodSignature.lastIndexOf(".");
    if(lastIndexOfDot == -1)
      return methodSignature;

    int lastIndexOfPara = methodSignature.lastIndexOf("(");
    String methodName = methodSignature.substring(lastIndexOfDot+1, lastIndexOfPara);

    return methodName+"()";
  }

  public String getMethodNameWithSignature(String methodSignature)
  {
    int lastIndexOfDot = methodSignature.lastIndexOf(".");
    if(lastIndexOfDot == -1)
      return methodSignature.substring(0, methodSignature.length()-1);

    int lastIndexOfParaStart = methodSignature.lastIndexOf("(");
    int lastIndexOfParaEnd = methodSignature.lastIndexOf(")");
    String methodName = methodSignature.substring(lastIndexOfDot+1, lastIndexOfParaStart);

    String argumentStr = methodSignature.substring(lastIndexOfParaStart, lastIndexOfParaEnd+1);
    String arguments[] = argumentStr.split(";");
    for(int i = 0; i < arguments.length; i++)
    {
      if(i == arguments.length-1)
        methodName += arguments[i].substring(arguments[i].lastIndexOf("/")+1, arguments[i].length());
      else if(i == 0 && arguments.length == 2)
        methodName += "("+arguments[i].substring(arguments[i].lastIndexOf("/")+1, arguments[i].length());
      else if(i == 0 )
        methodName += "("+arguments[i].substring(arguments[i].lastIndexOf("/")+1, arguments[i].length()) + ",";
      else if(i == arguments.length-2)
        methodName += arguments[i].substring(arguments[i].lastIndexOf("/")+1, arguments[i].length());
      else
        methodName += arguments[i].substring(arguments[i].lastIndexOf("/")+1, arguments[i].length()) + ",";
    }
    //String methodName = methodSignature.substring(lastIndexOfDot+1, methodSignature.length());

    methodSignature = methodSignature.substring(0, lastIndexOfDot);
    lastIndexOfDot = methodSignature.lastIndexOf(".");
    if(lastIndexOfDot == -1)
      return methodName;

    //methodName = methodSignature.substring(lastIndexOfDot+1, methodSignature.length()) +"."+ methodName;

    return methodName;
  }

  public String getClassName(String methodSignature)
  {
    int lastIndexOfDot = methodSignature.lastIndexOf(".");
    if(lastIndexOfDot == -1)
      return methodSignature;
    else
    {
      String tempStr = methodSignature.substring(0, lastIndexOfDot);
      lastIndexOfDot = tempStr.lastIndexOf(".");

      if(lastIndexOfDot == -1)
        return tempStr;

      tempStr = tempStr.substring(lastIndexOfDot+1, tempStr.length());
      return tempStr;
    }
  }

  /*
   * This method is to get class name from class signature.
   */
  public String getClsName(String clsSignature)
  {
    try
    {
      int index = clsSignature.lastIndexOf(".");
      if(index < 0)
      {
        return clsSignature;
      }
      else
      {
        return clsSignature.substring(index + 1, clsSignature.length());
      }
    }
    catch(Exception e)
    {
      Log.errorLog(className, "getClsName", "", "", e.getMessage());
    }
    return clsSignature;
  }

  public String getPackageName(String methodSignature)
  {
    int lastIndexOfDot = methodSignature.lastIndexOf(".");
    if(lastIndexOfDot == -1)
      return methodSignature;
    else
    {
      String tempStr = methodSignature.substring(0, lastIndexOfDot);
      lastIndexOfDot = tempStr.lastIndexOf(".");

      if(lastIndexOfDot == -1)
        return tempStr;

      tempStr = tempStr.substring(0, lastIndexOfDot);
      return tempStr;
    }
  }

  public static void main(String[] args)
  {
    try
    {
      String[][] Components = new String[3][4];
      Components[0][0]="Wheat";
      Components[0][1]="13";
      Components[0][2]="12";
      Components[0][3]="111";
      Components[1][0]="Pulse";
      Components[1][1]="10";
      Components[1][2]="40";
      Components[1][3]="20";
      Components[2][0]="Pulse1";
      Components[2][1]="101";
      Components[2][2]="401";
      Components[2][3]="201";
      PrintWriter pw = new PrintWriter(System.out);
      HttpSession session=null;
      //String filename = GenerateChart.generatePieChart(Components, "row", "0", "1", "URL Average Time", "AnalyzePie", session, pw ,"3d",500,500);
      //String filename1 = GenerateChart.generateBarChart(Components, "column", "1", "1", "URL Average Time", "YTitle", "XTtitle", "AnalyzeBar", null,pw,"3d",500,400);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return;
  }
}

class LabelGenerator extends StandardPieSectionLabelGenerator
{
  public String generateItemLabel(final CategoryDataset dataset, final int series, final int category)
  {
    return dataset.getRowKey(category).toString();
  }
}

class NoDataException extends Exception {

    public NoDataException() {
    super();
    }
}

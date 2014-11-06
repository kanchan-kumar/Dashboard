/******************************************************************
 *  Name   : PanelTypeUtils.java
 *  Author : Ravi Kant Sharma
 *  Purpose: Keeping Panel Graph Type To Remind and Meaningfull declaration
 *
 *
 ******************************************************************/

package pac1.Bean;

public class PanelTypeUtils
{
  // These are graph specific properties
	public static int NORMAL_GRAPH_TYPE = 0;
  public static int MERGE_GRAPH_TYPE = 1;
  public static int DERIVED_GRAPH_TYPE = 2;
  public static int DUAL_AXIS_LINE_GRAPH_TYPE = 3;
  public static int DUAL_AXIS_BAR_LINE_GRAPH_TYPE = 4;
  
  // These are panel specific properties
  public static int PANEL_TYPE_STACK = 5;
  public static int PANEL_TYPE_AREA = 6;
  public static int PANEL_TYPE_METER = 7;
  public static int PANEL_TYPE_DIAL = 8;
  public static int PANEL_TYPE_PERCENTILE = 9;
  public static int PANEL_TYPE_SLAB = 10;
  
  // These Names are saved in favourite file (.egp)
  public static String timeSeriesName = "TimeSeries";
  public static String dialGraphName = "Dial";
  public static String meterGraphName = "Meter";
  public static String percentileGraphName = "Percentile";
  public static String barGraphName = "Bar";
  
  public static void main(String[] args)
  {

  }

}

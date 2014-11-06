package com.cavisson.gui.dashboard.components.charts.Impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.vaadin.addon.charts.Chart;
import com.cavisson.gui.dashboard.components.charts.model.AbstractVaadinChartExample;
import com.vaadin.addon.charts.model.Axis;
import com.vaadin.addon.charts.model.AxisType;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.DataSeriesItem;
import com.vaadin.addon.charts.model.DateTimeLabelFormats;
import com.vaadin.addon.charts.model.PlotOptionsSpline;
import com.vaadin.addon.charts.model.Title;
import com.vaadin.ui.Component;

public class MultipleTimeSeries extends AbstractVaadinChartExample
{

  @Override
  public String getDescription()
  {
    return "Time data";
  }

  @Override
  public Component getChart()
  {
    Chart chart = new Chart();
    chart.setSizeFull();
    
    Configuration configuration = new Configuration();
    configuration.getChart().setType(ChartType.SPLINE);

    configuration.getTitle().setText(
      "Multiple Time Series Chart");
    configuration.getTooltip().setFormatter("");

    configuration.getxAxis().setType(AxisType.DATETIME);
    configuration.getxAxis().setDateTimeLabelFormats(
      new DateTimeLabelFormats("%e. %b", "%b"));

    Axis yAxis = configuration.getyAxis();
    yAxis.setTitle(new Title("Snow depth (m)"));
    yAxis.setMin(0);

    DataSeries ls = new DataSeries();
    ls.setPlotOptions(new PlotOptionsSpline());
    ls.setName("Winter 2007-2008");

    Object[][] data1 = getData1();
    for (int i = 0; i < data1.length; i++)
    {
      Object[] ds = data1[i];
      DataSeriesItem item = new DataSeriesItem((Date) ds[0],
        (Double) ds[1]);
      ls.add(item);
    }

    configuration.addSeries(ls);

    ls = new DataSeries();
    ls.setPlotOptions(new PlotOptionsSpline());
    ls.setName("Winter 2008-2009");

    Object[][] data2 = getData2();
    for (int i = 0; i < data2.length; i++)
    {
      Object[] ds = data2[i];
      DataSeriesItem item = new DataSeriesItem((Date) ds[0],
        (Double) ds[1]);
      ls.add(item);
    }

    configuration.addSeries(ls);

    ls = new DataSeries();
    ls.setPlotOptions(new PlotOptionsSpline());
    ls.setName("Winter 2009-2010");
    Object[][] data3 = getData3();
    for (int i = 0; i < data3.length; i++)
    {
      Object[] ds = data3[i];
      DataSeriesItem item = new DataSeriesItem((Date) ds[0],
        (Double) ds[1]);
      ls.add(item);
    }
    configuration.addSeries(ls);
    chart.drawChart(configuration);
    return chart;
  }

  private Object[][] getData3()
  {
    return new Object[][]
    {
      {
        d("1970,9,9"), 0d
      },
      {
        d("1970,9,14"), 0.15
      }, 
      {
        d("1970,10,28"), 0.35
      },
      {
        d("1970,11,12"), 0.46
      }, 
      {
        d("1971,0,1"), 0.59
      },
      {
        d("1971,0,24"), 0.58
      }, 
      {
        d("1971,1,1"), 0.62
      },
      {
        d("1971,1,7"), 0.65
      }, 
      {
        d("1971,1,23"), 0.77
      },
      {
        d("1971,2,8"), 0.77
      }, 
      {
        d("1971,2,14"), 0.79
      },
      {
        d("1971,2,24"), 0.86
      }, 
      {
        d("1971,3,4"), 0.8
      },
      {
        d("1971,3,18"), 0.94
      }, 
      {
        d("1971,3,24"), 0.9
      },
      {
        d("1971,4,16"), 0.39
      }, 
      {
        d("1971,4,21"), 0d
      }
    };
  }

  private Object[][] getData2()
  {
    return new Object[][]
    {
      {
        d("1970,9,9"), 0.7d
      },
      {
        d("1970,9,14"), 0.215
      }, 
      {
        d("1970,10,28"), 0.335
      },
      {
        d("1970,11,12"), 0.146
      }, 
      {
        d("1971,0,1"), 0.759
      },
      {
        d("1971,0,24"), 0.958
      }, 
      {
        d("1971,1,1"), 0.62
      },
      {
        d("1971,1,7"), 0.365
      }, 
      {
        d("1971,1,23"), 0.77
      },
      {
        d("1971,2,8"), 0.77
      }, 
      {
        d("1971,2,14"), 0.179
      },
      {
        d("1971,2,24"), 0.86
      }, 
      {
        d("1971,3,4"), 0.8
      },
      {
        d("1971,3,18"), 0.494
      }, 
      {
        d("1971,3,24"), 0.9
      },
      {
        d("1971,4,16"), 0.239
      }, 
      {
        d("1971,4,21"), 0.5d
      }
    };
  }

  private Object[][] getData1()
  {
    return new Object[][]
    {
      {
        d("1970,9,9"), 0.2d
      },
      {
        d("1970,9,14"), 0.5
      }, 
      {
        d("1970,10,28"), 0.35
      },
      {
        d("1970,11,12"), 0.426
      }, 
      {
        d("1971,0,1"), 0.159
      },
      {
        d("1971,0,24"), 0.58
      }, 
      {
        d("1971,1,1"), 0.62
      },
      {
        d("1971,1,7"), 0.35
      }, 
      {
        d("1971,1,23"), 0.77
      },
      {
        d("1971,2,8"), 0.77
      }, 
      {
        d("1971,2,14"), 0.59
      },
      {
        d("1971,2,24"), 0.86
      }, 
      {
        d("1971,3,4"), 0.8
      },
      {
        d("1971,3,18"), 0.914
      }, 
      {
        d("1971,3,24"), 0.9
      },
      {
        d("1971,4,16"), 0.339
      }, 
      {
        d("1971,4,21"), 1d
      }
    };
  }

  private final DateFormat df = new SimpleDateFormat("yyyy,MM,dd");

  /**
   * Helper method to convert Date string YYYY,MM,dd to Date
   *
   * @param dateString
   * @return
   */
  private Date d(String dateString)
  {
    df.setTimeZone(TimeZone.getTimeZone("EET"));
    try
    {
      return df.parse(dateString);
    }
    catch (ParseException e)
    {
      throw new RuntimeException(e);
    }
  }
}

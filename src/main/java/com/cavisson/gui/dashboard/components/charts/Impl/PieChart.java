/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cavisson.gui.dashboard.components.charts.Impl;

import com.cavisson.gui.dashboard.components.charts.model.AbstractVaadinChartExample;
import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.LegendItemClickEvent;
import com.vaadin.addon.charts.LegendItemClickListener;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.Cursor;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.DataSeriesItem;
import com.vaadin.addon.charts.model.Labels;
import com.vaadin.addon.charts.model.PlotOptionsPie;
import com.vaadin.addon.charts.model.style.GradientColor;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;

/**
 *
 * @author Kanchan
 */
@SuppressWarnings("serial")
public class PieChart extends AbstractVaadinChartExample
{

    @Override
    public String getDescription()
    {
        return "Pie Chart";
    }

    @Override
    public Component getChart()
    {
        Chart chart = new Chart(ChartType.PIE);

        Configuration conf = chart.getConfiguration();
        chart.setSizeFull();
        conf.setTitle("Browser usage year 2010");

        PlotOptionsPie plotOptions = new PlotOptionsPie();
        plotOptions.setCursor(Cursor.POINTER);
        Labels dataLabels = new Labels();
        dataLabels.setEnabled(true);
        dataLabels.setColor(SolidColor.BLACK);
        dataLabels.setConnectorColor(SolidColor.BLACK);
        dataLabels
          .setFormatter("''+ this.point.name +': '+ this.percentage +' %'");
        //plotOptions.setDataLabels(dataLabels);
        //conf.setPlotOptions(plotOptions);

        conf.setSeries(getBrowserMarketShareSeries());

        chart.addLegendItemClickListener(new LegendItemClickListener() {
            @Override
            public void onClick(LegendItemClickEvent event) {
                Notification.show("Legend item click"
                        + " : "
                        + event.getSeriesItemIndex()
                        + " : "
                        + ((DataSeries) event.getSeries())
                                .get(event.getSeriesItemIndex()).getName());
            }
        });
        chart.drawChart();
        return chart;
    }

    private DataSeries getBrowserMarketShareSeries()
    {
        DataSeriesItem firefox = new DataSeriesItem("Firefox", 45.0);
        firefox.setColor(createRadialGradient(new SolidColor(255, 128, 0),
          new SolidColor(128, 64, 0)));

        DataSeriesItem ie = new DataSeriesItem("IE", 26.8);
        ie.setColor(createRadialGradient(new SolidColor(0, 255, 255),
          new SolidColor(0, 128, 128)));

        DataSeriesItem chrome = new DataSeriesItem("Chrome", 12.8);
        chrome.setColor(createRadialGradient(new SolidColor(255, 255, 0),
          new SolidColor(128, 128, 0)));
        chrome.setSliced(true);
        chrome.setSelected(true);

        DataSeriesItem safari = new DataSeriesItem("Safari", 8.5);
        safari.setColor(createRadialGradient(new SolidColor(0, 128, 255),
          new SolidColor(0, 64, 128)));

        DataSeriesItem opera = new DataSeriesItem("Opera", 6.2);
        opera.setColor(createRadialGradient(new SolidColor(255, 0, 0),
          new SolidColor(128, 0, 0)));

        DataSeriesItem others = new DataSeriesItem("Others", 0.7);
        others.setColor(createRadialGradient(new SolidColor(0, 128, 0),
          new SolidColor(0, 64, 0)));

        return new DataSeries(firefox, ie, chrome, safari, opera, others);
    }

    /**
     * Creates a radial gradient with the specified start and end colors.
     */
    private GradientColor createRadialGradient(SolidColor start, SolidColor end)
    {
        GradientColor color = GradientColor.createRadial(0.5, 0.3, 0.7);
        color.addColorStop(0, start);
        color.addColorStop(1, end);
        return color;
    }

}

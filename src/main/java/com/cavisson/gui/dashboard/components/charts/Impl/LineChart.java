/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cavisson.gui.dashboard.components.charts.Impl;

import java.util.Random;

import com.vaadin.addon.charts.Chart;
import com.cavisson.gui.dashboard.components.charts.model.AbstractVaadinChartExample;
import com.vaadin.addon.charts.model.Axis;
import com.vaadin.addon.charts.model.AxisType;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.DataSeriesItem;
import com.vaadin.addon.charts.model.PlotLine;
import com.vaadin.addon.charts.model.PlotOptionsSpline;
import com.vaadin.addon.charts.model.Title;
import com.vaadin.addon.charts.model.YAxis;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.ui.Component;

public class LineChart extends AbstractVaadinChartExample {

    @Override
    public String getDescription() {
        return "Spline Updating Each Seconds";
    }

    @Override
    public Component getChart() {
        final Random random = new Random();

        final Chart chart = new Chart();
        chart.setSizeFull();

        final Configuration configuration = new Configuration();
        configuration.getChart().setType(ChartType.SPLINE);
        configuration.getTitle().setText("Live Time Series Chart");

        Axis xAxis = configuration.getxAxis();
        xAxis.setType(AxisType.DATETIME);
        xAxis.setTickPixelInterval(150);

        YAxis yAxis = configuration.getyAxis();
        yAxis.setTitle(new Title("Value"));
        yAxis.setPlotLines(new PlotLine(0, 1, new SolidColor("#808080")));

        configuration.getTooltip().setEnabled(false);
        configuration.getLegend().setEnabled(false);

        final DataSeries series = new DataSeries();
        series.setPlotOptions(new PlotOptionsSpline());
        series.setName("Random data");
        for (int i = -19; i <= 0; i++) {
            series.add(new DataSeriesItem(
                    System.currentTimeMillis() + i * 1000, random.nextDouble()));
        }
              
        System.out.println("Taken next sample ....");
        
        runWhileAttached(chart, new Runnable() {

            @Override
            public void run() {
                final long x = System.currentTimeMillis();
                final double y = random.nextDouble();
                
                System.out.println("x == " + x + ", y = " + y);
                
                series.add(new DataSeriesItem(x, y), true, true);
            }
        }, 2000, 1000);

        configuration.setSeries(series);

        chart.drawChart(configuration);
        return chart;
    }
}
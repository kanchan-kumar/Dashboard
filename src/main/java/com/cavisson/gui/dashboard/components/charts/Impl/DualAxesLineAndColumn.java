/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cavisson.gui.dashboard.components.charts.Impl;

import com.vaadin.addon.charts.Chart;
import com.cavisson.gui.dashboard.components.charts.model.AbstractVaadinChartExample;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.HorizontalAlign;
import com.vaadin.addon.charts.model.LayoutDirection;
import com.vaadin.addon.charts.model.Legend;
import com.vaadin.addon.charts.model.PlotOptionsColumn;
import com.vaadin.addon.charts.model.PlotOptionsSpline;
import com.vaadin.addon.charts.model.Tooltip;
import com.vaadin.addon.charts.model.VerticalAlign;
import com.vaadin.addon.charts.model.XAxis;
import com.vaadin.addon.charts.model.YAxis;
import com.vaadin.addon.charts.model.ZoomType;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.addon.charts.model.style.Style;
import com.vaadin.ui.Component;

@SuppressWarnings("serial")
public class DualAxesLineAndColumn extends AbstractVaadinChartExample
{

    @Override
    public String getDescription()
    {
        return "Dual axes, line and column";
    }

    @Override
    public Component getChart()
    {
        Chart chart = new Chart();

        chart.setSizeFull();
        Configuration conf = chart.getConfiguration();

        conf.getChart().setZoomType(ZoomType.XY);

        conf.setTitle("Dual Axis with Export Feature");
        //conf.setSubTitle("Source: WorldClimate.com");

        XAxis x = new XAxis();
        x.setCategories("Jan", "Feb", "Mar", "Apr", "May", "Jun");
        conf.addxAxis(x);

        YAxis primary = new YAxis();
        primary.setTitle("Temperature");
        Style style = new Style();
        style.setColor(new SolidColor("#89A54E"));
        primary.getTitle().setStyle(style);
        conf.addyAxis(primary);

        YAxis snd = new YAxis();
        snd.setTitle("Rainfall");
        snd.setOpposite(true);
        style = new Style();
        style.setColor(new SolidColor("#4572A7"));
        snd.getTitle().setStyle(style);
        conf.addyAxis(snd);

        Tooltip tooltip = new Tooltip();
        tooltip.setFormatter("this.x +': '+ this.y + (this.series.name == 'Rainfall' ? ' mm' : '°C')");
        conf.setTooltip(tooltip);

        Legend legend = new Legend();
        legend.setLayout(LayoutDirection.VERTICAL);
        legend.setHorizontalAlign(HorizontalAlign.LEFT);
        legend.setX(120);
        legend.setVerticalAlign(VerticalAlign.TOP);
        legend.setY(100);
        legend.setFloating(true);
        legend.setBackgroundColor("#FFFFFF");
        //conf.setLegend(legend);

        DataSeries series = new DataSeries();
        series.setPlotOptions(new PlotOptionsColumn());
        series.setName("Rainfall");
        series.setData(49.9, 71.5, 106.4, 129.2, 144.0, 176.0);
        series.setyAxis(1);
        conf.addSeries(series);

        series = new DataSeries();
        PlotOptionsSpline plotOptions = new PlotOptionsSpline();
        series.setPlotOptions(plotOptions);
        series.setName("Temperature");
        series.setData(7.0, 6.9, 9.5, 14.5, 18.2, 21.5);
        plotOptions.setColor(new SolidColor("#89A54E"));
        conf.addSeries(series);

        return chart;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cavisson.gui.dashboard.components.charts.Impl;

import com.cavisson.gui.dashboard.components.charts.model.AbstractVaadinChartExample;
import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.Exporting;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class MultiAxisLineBar extends AbstractVaadinChartExample
{

    @Override
    public String getDescription()
    {
        return "";
    }

    @Override
    public Component getChart()
    {
        //verticalLayout.addComponent(new Label(getDescription()));
        Chart chart = (Chart) new DualAxesLineAndColumn().getChart();
        chart.setSizeFull();
        // Enabling exporting adds a button to UI via users can download the
        // chart e.g. for printing
        Exporting exporting = new Exporting(true);

        // One can customize the filename
        exporting.setFilename("mychartfile.pdf");

        // and choose whether to post raster images to exporting server
        exporting.setEnableImages(true);

        // Exporting is by default done on highcharts public servers, but you
        // can also use your own server
        // exporting.setUrl("http://my.own.server.com");
        // Actually use these settings in the chart
        chart.getConfiguration().setExporting(exporting);

        // Simpler boolean API can also be used to just toggle the service
        // on/off
        // chart.getConfiguration().setExporting(true);
        return chart;

    }
}

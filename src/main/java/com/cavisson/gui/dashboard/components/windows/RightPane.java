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
import com.cavisson.gui.dashboard.components.charts.model.DefaultPanelGraphs;
import com.cavisson.gui.dashboard.data.GraphDataProvider;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import java.io.Serializable;
import pac1.Bean.GraphName.GraphNames;
import pac1.Bean.GraphUniqueKeyDTO;

/**
 *
 * @author pydi
 */
public class RightPane implements Serializable
{

  int rows = 2;
  int columns = 2;
  int screenWidth = 360;
  int screenHeight = 640;
  int maxNumPanles = 6;
  GraphPanel[] graphPanels = null;
  GraphDataProvider graphDataProvider = null;
  GraphNames graphNamesObj = null;

  public RightPane(int screenWidth, int screenHeight, GraphDataProvider graphDataProvider)
  {
    this.screenWidth = screenWidth;
    this.screenHeight = screenHeight;
    this.graphDataProvider = graphDataProvider;
    this.graphNamesObj = graphDataProvider.getGraphNames();
    initGraphPanels();
  }

  private void initGraphPanels()
  {
    graphPanels = new GraphPanel[maxNumPanles];
    DefaultPanelGraphs defaultPanelGraphs = new DefaultPanelGraphs();
    for (int i = 0; i < graphPanels.length; i++)
    {
      GraphUniqueKeyDTO graphUniqueKeyDTO = new GraphUniqueKeyDTO(1, 1, "NA");
      GraphUniqueKeyDTO arrGraphUniqueKeyDTO[] = defaultPanelGraphs.getGraphDTOArrayByPanel(i);//new GraphUniqueKeyDTO[]{graphUniqueKeyDTO};
      graphPanels[i] = new GraphPanel(i, arrGraphUniqueKeyDTO, this);
    }
  }

  public VerticalLayout getRightPanelLayout(int screenWidth, int screenHeight)
  {
    setRowAndColumns(screenWidth, screenHeight);
    System.out.println("For screenWidth = " + screenWidth + ", screenHeight = " + screenHeight + ", rows = " + rows + ", cols = " + columns);
    if (rows == 1 && columns == 1)
    {
      return getRightPanel1X1Layout();
    }
    else if (rows == 1 && columns == 2)
    {
      return getRightPanel1X2Layout();
    }
    else if (rows == 1 && columns == 3)
    {
      return getRightPanel1X3Layout();
    }
    else if (rows == 2 && columns == 2)
    {
      return getRightPanel2X2Layout();
    }
    else if (rows == 2 && columns == 3)
    {
      return getRightPanel2X3Layout();
    }
    else if (rows == 2 && columns == 1)
    {
      return getRightPanel2X1Layout();
    }
    else if (rows == 3 && columns == 1)
    {
      return getRightPanel3X1Layout();
    }
    else
    {
      return getRightPanel2X3Layout();
    }
  }

  private VerticalLayout getRightPanel1X1Layout()
  {
    try
    {
      VerticalLayout verticalLayout = new VerticalLayout();
      verticalLayout.setSizeFull();
      ResizeInsideVaadinComponent graphComponent = new ResizeInsideVaadinComponent();
      verticalLayout.addComponent(graphPanels[0]);

      return verticalLayout;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  private VerticalLayout getRightPanel1X2Layout()
  {
    try
    {
      VerticalLayout verticalLayout = new VerticalLayout();
      verticalLayout.setSizeFull();

      HorizontalSplitPanel h1 = new HorizontalSplitPanel();
      h1.setSplitPosition(50);
      ResizeInsideVaadinComponent graphComponent = new ResizeInsideVaadinComponent();
      h1.setFirstComponent(graphPanels[0]);
      MultiAxisLineBar multiAxis = new MultiAxisLineBar();
      h1.setSecondComponent(graphPanels[1]);

      verticalLayout.addComponent(h1);

      return verticalLayout;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  private VerticalLayout getRightPanel2X1Layout()
  {
    try
    {
      VerticalLayout verticalLayout = new VerticalLayout();
      verticalLayout.setSizeFull();
      VerticalSplitPanel v1 = new VerticalSplitPanel();

      v1.setSplitPosition(50);
      ResizeInsideVaadinComponent graphComponent = new ResizeInsideVaadinComponent();
      v1.setFirstComponent(graphPanels[0]);
      MultiAxisLineBar multiAxis = new MultiAxisLineBar();
      v1.setSecondComponent(graphPanels[1]);

      verticalLayout.addComponent(v1);

      return verticalLayout;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  private VerticalLayout getRightPanel3X1Layout()
  {
    try
    {
      VerticalLayout verticalLayout = new VerticalLayout();
      verticalLayout.setSizeFull();

      VerticalSplitPanel v1 = new VerticalSplitPanel();
      VerticalSplitPanel v2 = new VerticalSplitPanel();

      v1.setSplitPosition(50);
      ResizeInsideVaadinComponent graphComponent = new ResizeInsideVaadinComponent();
      v1.setFirstComponent(graphPanels[0]);
      MultiAxisLineBar multiAxis = new MultiAxisLineBar();
      v1.setSecondComponent(graphPanels[1]);

      v2.setSplitPosition(50);
      LineChart lineChart = new LineChart();
      v2.setFirstComponent(graphPanels[2]);
      v2.setSecondComponent(v1);

      verticalLayout.addComponent(v2);

      return verticalLayout;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  private VerticalLayout getRightPanel1X3Layout()
  {
    try
    {
      VerticalLayout verticalLayout = new VerticalLayout();
      verticalLayout.setSizeFull();

      HorizontalSplitPanel h1 = new HorizontalSplitPanel();
      HorizontalSplitPanel h2 = new HorizontalSplitPanel();
      h1.setSplitPosition(50);
      h2.setSplitPosition(50);

      ResizeInsideVaadinComponent graphComponent = new ResizeInsideVaadinComponent();
      h1.setFirstComponent(graphPanels[0]);
      MultiAxisLineBar multiAxis = new MultiAxisLineBar();
      h1.setSecondComponent(graphPanels[1]);

      AreaChart areaChart = new AreaChart();
      h1.setFirstComponent(graphPanels[2]);
      h1.setSecondComponent(h2);
      verticalLayout.addComponent(h1);
      return verticalLayout;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  private VerticalLayout getRightPanel2X2Layout()
  {
    try
    {
      VerticalLayout verticalLayout = new VerticalLayout();
      verticalLayout.setSizeFull();

      HorizontalSplitPanel h1 = new HorizontalSplitPanel();
      h1.setSplitPosition(50);

      VerticalSplitPanel v1 = new VerticalSplitPanel();
      VerticalSplitPanel v2 = new VerticalSplitPanel();

      v1.setSplitPosition(50);
      ResizeInsideVaadinComponent graphComponent = new ResizeInsideVaadinComponent();
      v1.setFirstComponent(graphPanels[0]);
      MultiAxisLineBar multiAxis = new MultiAxisLineBar();
      v1.setSecondComponent(graphPanels[1]);

      v2.setSplitPosition(50);
      LineChart lineChart = new LineChart();
      v2.setFirstComponent(graphPanels[2]);
      StackedBarChart stackedChart = new StackedBarChart();
      v2.setSecondComponent(graphPanels[3]);

      h1.setFirstComponent(v1);
      h1.setSecondComponent(v2);
      verticalLayout.addComponent(h1);

      return verticalLayout;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  private VerticalLayout getRightPanel2X3Layout()
  {
    try
    {
      VerticalLayout verticalLayout = new VerticalLayout();
      verticalLayout.setSizeFull();

      HorizontalSplitPanel h1 = new HorizontalSplitPanel();
      HorizontalSplitPanel h2 = new HorizontalSplitPanel();
      h1.setSplitPosition(33);

      VerticalSplitPanel v1 = new VerticalSplitPanel();
      VerticalSplitPanel v2 = new VerticalSplitPanel();
      VerticalSplitPanel v3 = new VerticalSplitPanel();

      v1.setSplitPosition(50);
      ResizeInsideVaadinComponent graphComponent = new ResizeInsideVaadinComponent();
      v1.setFirstComponent(graphPanels[0]);
      MultiAxisLineBar multiAxis = new MultiAxisLineBar();
      v1.setSecondComponent(graphPanels[1]);

      v2.setSplitPosition(50);
      LineChart lineChart = new LineChart();
      v2.setFirstComponent(graphPanels[2]);
      StackedBarChart stackedChart = new StackedBarChart();
      v2.setSecondComponent(graphPanels[3]);

      v3.setSplitPosition(50);
      AreaChart areaChart = new AreaChart();
      v3.setFirstComponent(graphPanels[4]);

      MultipleTimeSeries timeSeries = new MultipleTimeSeries();
      v3.setSecondComponent(graphPanels[5]);

            //v1.setStyleName(Reindeer.SPLITPANEL_SMALL);
      //v2.setStyleName(Reindeer.SPLITPANEL_SMALL);
      //v3.setStyleName(Reindeer.SPLITPANEL_SMALL);
            //h1.setStyleName(Reindeer.SPLITPANEL_SMALL);
      //h2.setStyleName(Reindeer.SPLITPANEL_SMALL);
      h2.setSplitPosition(50);
      h2.setFirstComponent(v2);
      h2.setSecondComponent(v3);

      h1.setFirstComponent(v1);
      h1.setSecondComponent(h2);
      verticalLayout.addComponent(h1);

      return verticalLayout;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }
  
  public VerticalSplitPanel getRightPanelLayoutComponents(int rows, int columns)
  {
    try
    {
      System.out.println("rows = " + rows + ", columns = " + columns);
      VerticalLayout verticalLayout = new VerticalLayout();
      verticalLayout.setSizeFull();

      VerticalSplitPanel v1 = new VerticalSplitPanel();

      v1.setSplitPosition(50);

      v1.setFirstComponent(new Label("Hello"));
      return getRecurciveVerticalSplitPanel(v1, columns - 1);

    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return null;
  }

  private VerticalSplitPanel getRecurciveVerticalSplitPanel(VerticalSplitPanel verticalSplitPanel, int columns)
  {
    try
    {
      System.out.println("getRecurciveVerticalSplitPanel = " + columns);
      if (columns < 1)
      {
        return verticalSplitPanel;
      }

      if (columns == 1)
      {
        verticalSplitPanel.setSecondComponent(new Label("One"));
      }
      else
      {
        VerticalSplitPanel tmpVerSplitPane = new VerticalSplitPanel();
        tmpVerSplitPane.setSplitPosition(50);
        tmpVerSplitPane.setFirstComponent(new Label("Three"));
        columns--;
        if (columns > 1)
        {
          verticalSplitPanel.setSecondComponent(tmpVerSplitPane);
          getRecurciveVerticalSplitPanel(tmpVerSplitPane, columns);
        }
        else
        {
          tmpVerSplitPane.setSecondComponent(new Label("Four"));
          verticalSplitPanel.setSecondComponent(tmpVerSplitPane);
          return verticalSplitPanel;
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    return verticalSplitPanel;
  }

  private void setRowAndColumns(int screenWidth, int screenHeight)
  {
    try
    {
      if (screenWidth < screenHeight)
      {

        if (screenHeight <= 400)
        {
          rows = 1;
          columns = 1;
        }
        else if (screenHeight >= 400 && screenWidth < 400)
        {
          rows = 2;
          columns = 1;
        }
        else if (screenHeight > 700 && screenWidth < 400)
        {
          rows = 3;
          columns = 1;
        }
        else if (screenHeight < 700 && screenWidth >= 400)
        {
          rows = 2;
          columns = 2;
        }
        else
        {
          rows = 2;
          columns = 3;
        }
      }
      else
      {
        if (screenWidth <= 400)
        {
          rows = 1;
          columns = 1;
        }
        else if (screenWidth >= 400 && screenHeight < 400)
        {
          rows = 1;
          columns = 2;
        }
        else if (screenWidth > 700 && screenHeight < 400)
        {
          rows = 1;
          columns = 3;
        }
        else if (screenWidth < 700 && screenHeight >= 400)
        {
          rows = 2;
          columns = 2;
        }
        else
        {
          rows = 2;
          columns = 3;
        }
      }

    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public int getScreenWidth()
  {
    return screenWidth;
  }

  public int getScreenHeight()
  {
    return screenHeight;
  }

  public GraphDataProvider getGraphDataProvider()
  {
    return graphDataProvider;
  }
  
  public GraphNames getGraphNamesObj()
  {
    return graphNamesObj;
  }
}

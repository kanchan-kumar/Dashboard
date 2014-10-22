/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cavisson.gui.dashboard.env.lunch;

import com.cavisson.gui.dashboard.components.charts.Impl.LineChart;
import com.cavisson.gui.dashboard.components.charts.Impl.ResizeInsideVaadinComponent;
import com.cavisson.gui.dashboard.components.controls.MenuBars;
import com.cavisson.gui.dashboard.components.controls.ValoThemeSessionInitListener;
import com.cavisson.gui.dashboard.components.controls.ValoThemeUI;
import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.FieldEvents;
import com.vaadin.server.ClassResource;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Notification;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.themes.Reindeer;
import java.util.Date;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

/**
 * The Application's "main" class.
 * @author Kanchan
 */
@SuppressWarnings("serial")
@JavaScript("prettify.js")
@Theme("tests-valo-facebook")
@Title("Netstorm Dashboard")
//@PreserveOnRefresh
public class InitEnvSetup extends UI 
{
       
    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = InitEnvSetup.class, widgetset = "com.cavisson.gui.dashboard.env.lunch.AppWidgetSet")
    public static class Servlet extends VaadinServlet 
    {
        @Override
        protected void servletInitialized() throws ServletException 
        {
            super.servletInitialized();
            getService().addSessionInitListener(
                    new ValoThemeSessionInitListener());
        }
    }
    
    @Override
    protected void init(VaadinRequest request) 
    {
        final TabSheet tabSheet = new TabSheet();
        tabSheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() 
        {
            @Override
            public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) 
            {
              com.vaadin.ui.JavaScript.eval("setTimeout(function(){prettyPrint();},300);");
            }
        });
        tabSheet.setSizeFull();
        
        CssLayout logoc = new CssLayout() 
        {
            @Override
            protected String getCss(Component c) 
            {
                if(c instanceof CssLayout) 
                {
                    return "background: #007ea8; border-bottom: 1px solid #004e68;padding-left:6px;";
                }
                return null;
            }
        };
        logoc.setId("logoc");

        String cssString = "#logoc {position:relative; width:100%} #links {position:absolute; top:5px; right: 5px;}  #links a span {text-decoration: none;} #links .v-icon {height:25px;}";

        String script = "if ('\\v'=='v') /* ie only */ {\n"
                + "        document.createStyleSheet().cssText = '"
                + cssString
                + "';\n"
                + "    } else {var tag = document.createElement('style'); tag.type = 'text/css';"
                + " document.getElementsByTagName('head')[0].appendChild(tag);tag[ (typeof "
                + "document.body.style.WebkitAppearance=='string') /* webkit only */ ? 'innerText' "
                + ": 'innerHTML'] = '" + cssString + "';}";

        com.vaadin.ui.JavaScript.eval(script);
               
        VerticalSplitPanel outerVerticalSplitPanel = new VerticalSplitPanel();
        VerticalSplitPanel innerVerticalSplitPanel = new VerticalSplitPanel();
        
        Panel upperPanel = new Panel(); 
        FormLayout uppperPanelLayout = new FormLayout();
       
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setSizeFull();
        horizontalLayout.addComponent(logoc);
        
        uppperPanelLayout.addStyleName("mypanelcontent");
        uppperPanelLayout.addComponent(logoc);
        uppperPanelLayout.setSizeUndefined(); // Shrink to fit
        upperPanel.setContent(getMenuBar());
        
        HorizontalSplitPanel horizontalSplitPanel = new HorizontalSplitPanel();
        horizontalSplitPanel.setSecondComponent(getRightPanelLayout());
        horizontalSplitPanel.setSplitPosition(20);
        
        //VerticalLayout verticalLayout = new VerticalLayout();  
        
        innerVerticalSplitPanel.setFirstComponent(upperPanel);
        innerVerticalSplitPanel.setSecondComponent(horizontalSplitPanel);
        innerVerticalSplitPanel.setSplitPosition(15);
        
        outerVerticalSplitPanel.setFirstComponent(innerVerticalSplitPanel);
        
        Panel lowerPanel = new Panel();   
        lowerPanel.setSizeFull();
        
        VerticalLayout lowerPanelLayout = new VerticalLayout();
        lowerPanelLayout.setSizeFull();
        //lowerPanelLayout.addComponent(lowerPanel);
     
        lowerPanel.setContent(createLowerPanelTable());
        
        outerVerticalSplitPanel.setSecondComponent(lowerPanel);
        outerVerticalSplitPanel.setSplitPosition(85);
        setContent(outerVerticalSplitPanel);
        
        VerticalLayout content = new VerticalLayout();
        content.setMargin(true);
        content.setSpacing(true);

        // Don't change the field type to search on IE8 #14211
        if (!isIE8()) {
            com.vaadin.ui.JavaScript
                    .eval("document.getElementById('search').type = 'search';");
        }

        horizontalSplitPanel.setFirstComponent(content);

        Page.getCurrent().addUriFragmentChangedListener(
                new Page.UriFragmentChangedListener() 
                {
                    @Override
                    public void uriFragmentChanged(Page.UriFragmentChangedEvent event) {
                    }
                });

    }

    private boolean isIE8() 
    {
        if (getPage().getWebBrowser().isIE()) {
            if (getPage().getWebBrowser().getBrowserMajorVersion() == 8) {
                return true;
            }
        }
        return false;
    }

    private HierarchicalContainer getContainer() 
    {
        HierarchicalContainer hierarchicalContainer = new HierarchicalContainer();
        hierarchicalContainer.addContainerProperty("displayName", String.class,
                "");
        return hierarchicalContainer;
    }
    
    private GridLayout getRighPanelGrid()
    {
      try
      {
          GridLayout grid = new GridLayout(3,2);

          // Layout containing relatively sized components must have
          // a defined size, here is fixed size.
          grid.setSizeFull();

          // Add some content
         String labels [] = {
          "Shrinking column<br/>Shrinking row",
          "Expanding column (1:)<br/>Shrinking row",
          "Expanding column (5:)<br/>Shrinking row",
          "Shrinking column<br/>Expanding row",
          "Expanding column (1:)<br/>Expanding row",
          "Expanding column (5:)<br/>Expanding row"
         };
         
         for(int i=0; i<labels.length; i++) 
         {
           Label label = new Label(labels[i], com.vaadin.shared.ui.label.ContentMode.HTML);
           label.setWidth(null); // Set width as undefined
           grid.addComponent(label);
         }

         // Set different expansion ratios for the two columns
         grid.setColumnExpandRatio(1, 1);
         grid.setColumnExpandRatio(2, 5);

         // Set the bottom row to expand
         grid.setRowExpandRatio(1, 1);

         // Align and size the labels.
         for (int col=0; col<grid.getColumns(); col++) 
         {
           for (int row=0; row<grid.getRows(); row++) 
           {
             Component c = grid.getComponent(col, row);
             grid.setComponentAlignment(c, Alignment.TOP_CENTER);
        
             // Make the labels high to illustrate the empty
             // horizontal space.
             if (col != 0 || row != 0)
               c.setHeight("100%");
           }
         }
         return grid;
      }
      catch(Exception e)
      {
         e.printStackTrace();
         return new GridLayout(3,2);
      }
    }
    
    
    private VerticalLayout getRightPanelLayout()
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
            v1.setFirstComponent(graphComponent.createChart());
            
            v2.setSplitPosition(50);
            LineChart lineChart = new LineChart();
            v2.setFirstComponent(lineChart.getChart());
            
            v3.setSplitPosition(50);
                        
            v1.setStyleName(Reindeer.SPLITPANEL_SMALL);
            v2.setStyleName(Reindeer.SPLITPANEL_SMALL);
            v3.setStyleName(Reindeer.SPLITPANEL_SMALL);
            
            //h1.setStyleName(Reindeer.SPLITPANEL_SMALL);
            h2.setStyleName(Reindeer.SPLITPANEL_SMALL);
            
            h2.setSplitPosition(50);
            h2.setFirstComponent(v2);
            h2.setSecondComponent(v3);
            
            h1.setFirstComponent(v1);
            h1.setSecondComponent(h2);       
            verticalLayout.addComponent(h1);
            
            return verticalLayout;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
     
    private VerticalLayout createLowerPanelTable()
    {
      try
      {
        Table table = new Table();
        //add table columns
        table.addContainerProperty("Graph Color Code", Integer.class, null);
        table.addContainerProperty("Graph Name", String.class, null);
        table.addContainerProperty("Date/Time", Date.class, null);
        table.addContainerProperty("Min", Double.class, null);
        table.addContainerProperty("Max", Double.class, null);
        table.addContainerProperty("Average", Double.class, null);
        table.addContainerProperty("Std-dev", Double.class, null);
        table.addContainerProperty("Last Sample", Double.class, null);
        
        table.setColumnWidth("Graph Name", 120);
        
        //add table data (rows)
        table.addItem(new Object[]{new Integer(100500), "Graph 1", new Date(), new Double(0.0), new Double(0.0), new Double(0.0), new Double(0.0), new Double(0.0)}, new Integer(1));
        table.addItem(new Object[]{new Integer(100501), "Graph 2", new Date(), new Double(0.0), new Double(0.0), new Double(0.0), new Double(0.0), new Double(0.0)}, new Integer(2));
        table.addItem(new Object[]{new Integer(100502), "Graph 3", new Date(), new Double(0.0), new Double(0.0), new Double(0.0), new Double(0.0), new Double(0.0)}, new Integer(3));
        table.addItem(new Object[]{new Integer(100503), "Graph 4", new Date(), new Double(0.0), new Double(0.0), new Double(0.0), new Double(0.0), new Double(0.0)}, new Integer(4));
        table.addItem(new Object[]{new Integer(100504), "Graph 5", new Date(), new Double(0.0), new Double(0.0), new Double(0.0), new Double(0.0), new Double(0.0)}, new Integer(5));
        table.addItem(new Object[]{new Integer(100505), "Graph 6", new Date(), new Double(0.0), new Double(0.0), new Double(0.0), new Double(0.0), new Double(0.0)}, new Integer(6));
        
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        table.setSizeFull();
        
        verticalLayout.addComponent(table);
        return verticalLayout;
      }
      catch(Exception e)
      {
        e.printStackTrace();
        return new VerticalLayout();
      }
    }
    
    private VerticalLayout getMenuBar()
    {
      try
      {
          VerticalLayout verticalLayout = new VerticalLayout();
          
          MenuBar topMenu = new MenuBar();
          topMenu.addStyleName("topmenu");
          
          MenuBar.MenuItem fileMenu = topMenu.addItem("File", null, null);
          fileMenu.addItem("Exit", null, mycommand);
          
          MenuBar.MenuItem viewMenu = topMenu.addItem("View", null, null);
          viewMenu.addItem("View Scenario", null, mycommand);
          viewMenu.addItem("Test Output", null, mycommand);
          viewMenu.addItem("Event Log", null, mycommand);
          viewMenu.addItem("Virtual User Trace", null, mycommand);
          viewMenu.addItem("DebugTrace Log", null, mycommand);
          viewMenu.addItem("PauseResume Log", null, mycommand);

          MenuBar.MenuItem favorites = topMenu.addItem("Favorites", null, null);
          favorites.addItem("_default", null, mycommand);
          favorites.addItem("_Netstorm", null, mycommand);
          
          MenuBar.MenuItem settings = topMenu.addItem("Settings", null, null);
          settings.addItem("Configuration", null, mycommand);
          
          MenuBar.MenuItem help = topMenu.addItem("Help", null, null);
          help.addItem("Help Topics", null, mycommand);
          help.addItem("About Netstorm", null, mycommand);
                    
          verticalLayout.addComponent(new MenuBars());
          
          verticalLayout.addStyleName("wrapping");
          verticalLayout.setSizeFull();
          return verticalLayout;
      }
      catch(Exception e)
      {
        e.printStackTrace();
        return null;
      }
    }
    
//Define a common menu command for all the menu items.
MenuBar.Command mycommand = new MenuBar.Command() 
{
    @Override
    public void menuSelected(MenuBar.MenuItem selectedItem) 
    {
        Notification.show("This is the caption",
                  "Ordered a " +
                           selectedItem.getText() +
                           " from menu.",
                  Notification.Type.WARNING_MESSAGE);
    }  
};   
}

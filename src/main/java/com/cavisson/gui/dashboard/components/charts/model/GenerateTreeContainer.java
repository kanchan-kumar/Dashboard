/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cavisson.gui.dashboard.components.charts.model;

import com.cavisson.gui.dashboard.components.controls.StringGenerator;
import com.cavisson.gui.dashboard.components.controls.TestIcon;
import com.vaadin.data.Container;
import com.vaadin.data.Container.Hierarchical;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.Action;
import com.vaadin.server.Resource;
import com.vaadin.ui.Notification;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 *
 * @author Kanchan
 */
public class GenerateTreeContainer
{

    public static final String CAPTION_PROPERTY = "caption";
    public static final String DESCRIPTION_PROPERTY = "description";
    public static final String ICON_PROPERTY = "icon";
    public static final String INDEX_PROPERTY = "index";
    public static final String TREE_NODE_WITHOUT_CHILDREN_STYLE="no-children";


    @SuppressWarnings("unchecked")
    public static Container generateTreeContainer(final int size, final boolean hierarchical) throws IOException
    {
        try
        {
            TestIcon testIcon = new TestIcon(110);
            final IndexedContainer container = hierarchical ? new HierarchicalContainer() : new IndexedContainer();
            final StringGenerator sg = new StringGenerator();

            container.addContainerProperty(CAPTION_PROPERTY, String.class, null);
            container.addContainerProperty(ICON_PROPERTY, Resource.class, null);
            container.addContainerProperty(INDEX_PROPERTY, Integer.class, null);
            container.addContainerProperty(DESCRIPTION_PROPERTY, String.class, null);

            Properties groupNamesProp = new Properties();
            InputStream inputStream = GenerateTreeContainer.class.getResourceAsStream("/com/cavisson/gui/dashboard/data/properties/GroupNames.properties");
            groupNamesProp.load(inputStream);
            inputStream.close();
            
            LinkedHashMap<String, String> arrGraphNamesMap = new LinkedHashMap<String, String>();
            //BufferedReader br = new BufferedReader(new FileReader("/com/cavisson/gui/dashboard/data/properties/GroupNames.properties"));
            inputStream = GenerateTreeContainer.class.getResourceAsStream("/com/cavisson/gui/dashboard/data/properties/GraphNames.properties");

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
               // process the line.
                //System.out.println("line === " + line);
                String arrTokens[] = split(line, "=");
                
                if(arrTokens == null || arrTokens.length < 2)
                  continue;
                
                arrGraphNamesMap.put(arrTokens[0], arrTokens[1]);
                
            }
            reader.close(); 
            inputStream.close();
            
//            Iterator itr = arrGraphNamesMap.entrySet().iterator();
//            while(itr.hasNext())
//            {
//               Map.Entry<String, String> pairs = (Map.Entry) itr.next();
//                System.out.println("--key == " + pairs.getKey() + ", value == "+ pairs.getValue());
//            }

            
            Iterator it = groupNamesProp.entrySet().iterator();
            int count = 0;
            
            Item parent = container.addItem("parent_Node");
            parent.getItemProperty(CAPTION_PROPERTY).setValue("Test Matrices");
            parent.getItemProperty(ICON_PROPERTY).setValue(null);

            while (groupNamesProp.size() > count)
            {
                Map.Entry<String, String> pairs = (Map.Entry) it.next();
                String value = groupNamesProp.getProperty("GROUP" + count);
                //System.out.println("key = " + pairs.getKey() + ", value = " + pairs.getValue() + ", key value = " + value);
                final Item item = container.addItem("GROUP"+count);
                System.out.println("value == " + value);

                item.getItemProperty(CAPTION_PROPERTY).setValue(value);
                item.getItemProperty(INDEX_PROPERTY).setValue(count);
                item.getItemProperty(DESCRIPTION_PROPERTY).setValue(value);
                item.getItemProperty(ICON_PROPERTY).setValue(testIcon.get());
                
                ((Container.Hierarchical) container).setParent("GROUP" + count, "parent_Node");

                count++;
            }

            testIcon = new TestIcon(11);
            //container.getItem(container.getIdByIndex(0)).getItemProperty(ICON_PROPERTY).setValue(testIcon.get());
            if (hierarchical)
            {
                for (int i = 0; i < groupNamesProp.size(); i++)
                {                    
                    String groupNameIdentity = "GROUP" + i;
                    String groupName = groupNamesProp.getProperty(groupNameIdentity);
                    System.out.println("group name === " + groupName);

                    int gc = 0;
                    while(arrGraphNamesMap.get(groupName + "" + gc) != null)
                    {                    
                        String graphName = arrGraphNamesMap.get(groupName + "" + gc);
                        String graphNameIdentity = groupName + "" + gc;
                        Item child = container.addItem(graphNameIdentity);
                        System.out.println("graph name === " + graphName);
                        child.getItemProperty(CAPTION_PROPERTY).setValue(graphName);
                        child.getItemProperty(ICON_PROPERTY).setValue(testIcon.get());
                        ((Hierarchical) container).setChildrenAllowed(graphNameIdentity, false);
                        ((Container.Hierarchical) container).setParent(graphNameIdentity, groupNameIdentity);
                        gc++;
                    }
                }
            }
            return container;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static Action.Handler actionHandler = new Action.Handler()
    {
        private final Action ACTION_ONE = new Action("Action One");
        private final Action ACTION_TWO = new Action("Action Two");
        private final Action ACTION_THREE = new Action("Action Three");
        private final Action[] ACTIONS = new Action[]
        {
            ACTION_ONE, ACTION_TWO,
            ACTION_THREE
        };

        @Override
        public void handleAction(final Action action, final Object sender,
          final Object target)
        {
            Notification.show(action.getCaption());
        }

        @Override
        public Action[] getActions(final Object target, final Object sender)
        {
            return ACTIONS;
        }
    };

    public static Action.Handler getActionHandler()
    {
        return actionHandler;
    }
    
    public static String[] split(String str, String delim)
    {
       try
       {
          StringTokenizer strTokenSpace = new StringTokenizer(str, delim);
          String []arrTokens = new String[strTokenSpace.countTokens()];
          int count = 0;
           while (strTokenSpace.hasMoreTokens()) 
           {
             arrTokens[count++] = strTokenSpace.nextElement().toString();
           }         
           return arrTokens;
       }
       catch(Exception e)
       {
           e.printStackTrace();
           return null;
       }
    }
}
